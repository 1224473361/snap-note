package com.snapnote.app.asr

import android.util.Base64
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object IflytekAuthUtil {
    fun buildWebSocketUrl(
        host: String = IflytekConfig.HOST,
        path: String = IflytekConfig.PATH,
        apiKey: String = IflytekConfig.apiKey,
        apiSecret: String = IflytekConfig.apiSecret
    ): String {
        val date = toRfc1123(Date())
        val signatureOrigin = "host: $host\ndate: $date\nGET $path HTTP/1.1"
        val signature = hmacSha256Base64(apiSecret, signatureOrigin)
        val authorizationOrigin =
            "api_key=\"$apiKey\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"$signature\""
        val authorization = Base64.encodeToString(
            authorizationOrigin.toByteArray(StandardCharsets.UTF_8),
            Base64.NO_WRAP
        )
        return "wss://$host$path?authorization=${encode(authorization)}&date=${encode(date)}&host=${encode(host)}"
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())

    private fun toRfc1123(date: Date): String {
        val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
        format.timeZone = TimeZone.getTimeZone("GMT")
        return format.format(date)
    }

    private fun hmacSha256Base64(secret: String, content: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        val raw = mac.doFinal(content.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(raw, Base64.NO_WRAP)
    }
}
