package com.snapnote.app.asr

import android.util.Base64
import com.google.gson.Gson
import com.snapnote.app.audio.AudioPcmReader
import com.snapnote.app.data.AsrLogRepository
import com.snapnote.app.model.AsrLogEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class IflytekAsrProvider(
    private val logRepository: AsrLogRepository
) : SpeechToTextProvider {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override suspend fun transcribe(audioPath: String): String {
        val startedAt = System.currentTimeMillis()
        val logId = startedAt.toString()
        if (!IflytekConfig.isConfigured()) {
            logError(logId, audioPath, 0, startedAt, IflytekConfig.NOT_CONFIGURED_HINT)
            throw IllegalStateException(IflytekConfig.NOT_CONFIGURED_HINT)
        }
        val pcm = readPcmForAsr(audioPath)
        if (pcm.isEmpty()) {
            val fileSizeKb = File(audioPath).takeIf { it.exists() }?.length()?.div(1024) ?: 0
            val message = if (fileSizeKb > 0) {
                "录音解码失败（文件 ${fileSizeKb}KB），请重试"
            } else {
                "录音为空"
            }
            logError(logId, audioPath, 0, startedAt, message)
            throw IllegalStateException(message)
        }
        val url = IflytekAuthUtil.buildWebSocketUrl()
        return try {
            recognize(url, pcm)
        } catch (e: Exception) {
            logError(logId, audioPath, pcm.size, startedAt, e.message ?: "转写失败")
            throw e
        }.also { text ->
            logSuccess(logId, audioPath, pcm.size, startedAt, text)
        }
    }

    private suspend fun readPcmForAsr(audioPath: String): ByteArray {
        val file = File(audioPath)
        if (!file.exists() || file.length() == 0L) return ByteArray(0)

        val wavSidecar = File(file.parent, "${file.nameWithoutExtension}.wav")
        if (wavSidecar.exists() && wavSidecar.length() > 44) {
            return AudioPcmReader.readPcm(wavSidecar)
        }

        var pcm = AudioPcmReader.readPcm(file)
        if (pcm.isEmpty() && file.length() > 512) {
            delay(300)
            pcm = AudioPcmReader.readPcm(file)
        }
        return pcm
    }

    private suspend fun recognize(url: String, pcm: ByteArray): String = suspendCancellableCoroutine { cont ->
        val result = StringBuilder()
        var finished = false

        fun finish(error: Throwable? = null, text: String? = null) {
            if (finished) return
            finished = true
            when {
                error != null -> cont.resumeWithException(error)
                text.isNullOrBlank() -> cont.resumeWithException(
                    IllegalStateException("未识别到语音，请靠近麦克风重试")
                )
                else -> cont.resume(text.trim())
            }
        }

        val request = Request.Builder().url(url).build()
        val socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                scope.launch {
                    try {
                        sendFrames(webSocket, pcm)
                    } catch (e: Exception) {
                        finish(e)
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                val parsed = gson.fromJson(text, IflytekWsResponse::class.java)
                if (parsed.code != 0) {
                    webSocket.close(1000, null)
                    finish(IllegalStateException("讯飞识别失败：${parsed.code} ${parsed.message}"))
                    return
                }
                val partial = IflytekResponseParser.extractText(parsed)
                if (partial.isNotEmpty()) {
                    result.append(partial)
                }
                if (parsed.data?.status == 2) {
                    webSocket.close(1000, null)
                    finish(text = result.toString())
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                finish(IllegalStateException("网络错误：${t.message ?: "无法连接讯飞服务"}"))
            }
        })

        cont.invokeOnCancellation { socket.cancel() }
    }

    private suspend fun sendFrames(webSocket: WebSocket, pcm: ByteArray) {
        val chunkSize = IflytekConfig.CHUNK_SIZE
        val totalChunks = (pcm.size + chunkSize - 1) / chunkSize

        if (totalChunks <= 1) {
            webSocket.send(buildFrame(0, pcm, includeMeta = true))
            delay(FRAME_INTERVAL_MS)
            webSocket.send(gson.toJson(mapOf("data" to mapOf("status" to 2))))
            return
        }

        var offset = 0
        for (index in 0 until totalChunks) {
            val end = minOf(offset + chunkSize, pcm.size)
            val chunk = pcm.copyOfRange(offset, end)
            offset = end
            val status = when {
                index == 0 -> 0
                index == totalChunks - 1 -> 2
                else -> 1
            }
            webSocket.send(buildFrame(status, chunk, includeMeta = index == 0))
            if (index < totalChunks - 1) {
                delay(FRAME_INTERVAL_MS)
            }
        }
    }

    private fun buildFrame(status: Int, chunk: ByteArray, includeMeta: Boolean): String {
        val data = linkedMapOf<String, Any>(
            "status" to status,
            "format" to "audio/L16;rate=16000",
            "encoding" to "raw",
            "audio" to Base64.encodeToString(chunk, Base64.NO_WRAP)
        )
        val frame = linkedMapOf<String, Any>("data" to data)
        if (includeMeta) {
            frame["common"] = mapOf("app_id" to IflytekConfig.appId)
            frame["business"] = mapOf(
                "language" to "zh_cn",
                "domain" to "iat",
                "accent" to "mandarin"
            )
        }
        return gson.toJson(frame)
    }

    private fun logSuccess(
        logId: String,
        audioPath: String,
        pcmBytes: Int,
        startedAt: Long,
        text: String
    ) {
        logRepository.append(
            AsrLogEntry(
                id = logId,
                timestamp = startedAt,
                audioPath = audioPath,
                pcmBytes = pcmBytes,
                status = "success",
                resultText = text,
                elapsedMs = System.currentTimeMillis() - startedAt
            )
        )
    }

    private fun logError(
        logId: String,
        audioPath: String,
        pcmBytes: Int,
        startedAt: Long,
        message: String
    ) {
        logRepository.append(
            AsrLogEntry(
                id = logId,
                timestamp = startedAt,
                audioPath = audioPath,
                pcmBytes = pcmBytes,
                status = "error",
                errorMessage = message,
                elapsedMs = System.currentTimeMillis() - startedAt
            )
        )
    }

    companion object {
        private const val FRAME_INTERVAL_MS = 40L
    }
}
