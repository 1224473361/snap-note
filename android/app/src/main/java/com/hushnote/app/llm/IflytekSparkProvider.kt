package com.hushnote.app.llm

import com.google.gson.Gson
import com.hushnote.app.asr.IflytekAuthUtil
import com.hushnote.app.asr.IflytekConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class IflytekSparkProvider : SparkChatProvider {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun complete(
        system: String,
        user: String,
        onDelta: (accumulated: String) -> Unit
    ): String {
        if (!IflytekSparkConfig.isConfigured()) {
            throw IllegalStateException(IflytekConfig.NOT_CONFIGURED_HINT)
        }
        val url = IflytekAuthUtil.buildWebSocketUrl(
            host = IflytekSparkConfig.HOST,
            path = IflytekSparkConfig.PATH,
            apiKey = IflytekSparkConfig.apiKey,
            apiSecret = IflytekSparkConfig.apiSecret
        )
        return requestOnce(url, system, user, onDelta)
    }

    private suspend fun requestOnce(
        url: String,
        system: String,
        user: String,
        onDelta: (accumulated: String) -> Unit
    ): String = suspendCancellableCoroutine { cont ->
        val result = StringBuilder()
        var finished = false

        fun finish(error: Throwable? = null, text: String? = null) {
            if (finished) return
            finished = true
            when {
                error != null -> cont.resumeWithException(error)
                text.isNullOrBlank() -> cont.resumeWithException(
                    IllegalStateException("未生成内容")
                )
                else -> cont.resume(text.trim())
            }
        }

        val request = Request.Builder().url(url).build()
        val socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val payload = buildRequestPayload(system, user)
                val sent = webSocket.send(payload)
                if (!sent) {
                    finish(IllegalStateException("发送星火请求失败"))
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val chunk = SparkChunkParser.parse(text)
                    if (chunk.code != 0) {
                        webSocket.close(1000, null)
                        finish(
                            IllegalStateException(
                                "讯飞星火失败：${chunk.code} ${chunk.message}"
                            )
                        )
                        return
                    }
                    if (chunk.contentDelta.isNotEmpty()) {
                        result.append(chunk.contentDelta)
                        onDelta(result.toString())
                    }
                    if (chunk.isLast) {
                        webSocket.close(1000, null)
                        finish(text = result.toString())
                    }
                } catch (e: Exception) {
                    webSocket.close(1000, null)
                    finish(e)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                finish(IllegalStateException("网络错误：${t.message ?: "无法连接讯飞服务"}"))
            }
        })

        cont.invokeOnCancellation { socket.cancel() }
    }

    private fun buildRequestPayload(system: String, user: String): String {
        val body = mapOf(
            "header" to mapOf(
                "app_id" to IflytekSparkConfig.appId,
                "uid" to IflytekSparkConfig.UID
            ),
            "parameter" to mapOf(
                "chat" to mapOf(
                    "domain" to IflytekSparkConfig.DOMAIN,
                    "temperature" to 0.5,
                    "top_k" to 4,
                    "max_tokens" to IflytekSparkConfig.MAX_TOKENS
                )
            ),
            "payload" to mapOf(
                "message" to mapOf(
                    "text" to listOf(
                        mapOf("role" to "system", "content" to system),
                        mapOf("role" to "user", "content" to user)
                    )
                )
            )
        )
        return gson.toJson(body)
    }
}
