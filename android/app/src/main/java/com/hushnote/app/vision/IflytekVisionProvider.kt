package com.hushnote.app.vision

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.hushnote.app.asr.IflytekAuthUtil
import com.hushnote.app.asr.IflytekConfig
import com.hushnote.app.model.MealConstants
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

class IflytekVisionProvider : VisionProvider {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun recognizeDishes(imageFile: File): List<String> {
        if (!IflytekVisionConfig.isConfigured()) {
            throw IllegalStateException(IflytekConfig.NOT_CONFIGURED_HINT)
        }
        if (!imageFile.exists() || imageFile.length() == 0L) {
            throw IllegalStateException("图片文件无效")
        }
        if (imageFile.length() > MealConstants.MAX_UPLOAD_BYTES) {
            throw IllegalStateException("图片超过 4MB，请压缩后重试")
        }

        val base64 = Base64.encodeToString(imageFile.readBytes(), Base64.NO_WRAP)
        val url = IflytekAuthUtil.buildWebSocketUrl(
            host = IflytekVisionConfig.HOST,
            path = IflytekVisionConfig.PATH,
            apiKey = IflytekVisionConfig.apiKey,
            apiSecret = IflytekVisionConfig.apiSecret
        )
        val reply = requestOnce(url, base64)
        val dishes = VisionResponseParser.parseDishList(reply)
        if (dishes.isEmpty()) {
            throw IllegalStateException("未能识别出菜品")
        }
        return dishes
    }

    private suspend fun requestOnce(url: String, imageBase64: String): String =
        suspendCancellableCoroutine { cont ->
            val result = StringBuilder()
            var finished = false

            fun finish(error: Throwable? = null, text: String? = null) {
                if (finished) return
                finished = true
                when {
                    error != null -> cont.resumeWithException(error)
                    text.isNullOrBlank() -> cont.resumeWithException(
                        IllegalStateException("图片理解无返回内容")
                    )
                    else -> cont.resume(text.trim())
                }
            }

            val request = Request.Builder().url(url).build()
            val socket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    val payload = buildRequestPayload(imageBase64)
                    val sent = webSocket.send(payload)
                    if (!sent) {
                        finish(IllegalStateException("发送图片理解请求失败"))
                    }
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    try {
                        val root = gson.fromJson(text, JsonObject::class.java)
                        val header = root.getAsJsonObject("header")
                        val code = header?.get("code")?.asInt ?: -1
                        if (code != 0) {
                            val message = header?.get("message")?.asString ?: "未知错误"
                            webSocket.close(1000, null)
                            finish(IllegalStateException("讯飞图片理解失败：$code $message"))
                            return
                        }

                        val choices = root.getAsJsonObject("payload")
                            ?.getAsJsonObject("choices")
                        val texts = choices?.getAsJsonArray("text")
                        if (texts != null) {
                            for (element in texts) {
                                val content = element.asJsonObject.get("content")?.asString
                                if (!content.isNullOrEmpty()) {
                                    result.append(content)
                                }
                            }
                        }

                        val headerStatus = header?.get("status")?.asInt
                        val choicesStatus = choices?.get("status")?.asInt
                        if (headerStatus == 2 || choicesStatus == 2) {
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

    private fun buildRequestPayload(imageBase64: String): String {
        val body = mapOf(
            "header" to mapOf(
                "app_id" to IflytekVisionConfig.appId,
                "uid" to "hush-note-meal"
            ),
            "parameter" to mapOf(
                "chat" to mapOf(
                    "domain" to IflytekVisionConfig.DOMAIN,
                    "temperature" to 0.5,
                    "top_k" to 4,
                    "max_tokens" to 2048
                )
            ),
            "payload" to mapOf(
                "message" to mapOf(
                    "text" to listOf(
                        mapOf(
                            "role" to "user",
                            "content" to imageBase64,
                            "content_type" to "image"
                        ),
                        mapOf(
                            "role" to "user",
                            "content" to MealConstants.VISION_PROMPT,
                            "content_type" to "text"
                        )
                    )
                )
            )
        )
        return gson.toJson(body)
    }
}
