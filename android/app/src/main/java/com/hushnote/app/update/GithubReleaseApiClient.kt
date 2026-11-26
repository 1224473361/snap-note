package com.hushnote.app.update

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * GitHub Releases API 客户端。公开仓库无需令牌即可读取最新发行版与下载资产。
 */
class GithubReleaseApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    fun fetchLatestRelease(): GithubReleaseSummary {
        val request = Request.Builder()
            .url(GithubReleaseConfig.apiLatestUrl)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "hush-note-android")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            val raw = response.body?.string()
            if (!response.isSuccessful) {
                val msg = runCatching {
                    JsonParser.parseString(raw.orEmpty()).asJsonObject.get("message")?.asString
                }.getOrNull() ?: "HTTP ${response.code}"
                throw IllegalStateException("GitHub API 错误：$msg")
            }
            val root = parseObject(raw)
            return releaseSummaryFrom(root)
        }
    }

    fun downloadApk(summary: GithubReleaseSummary, destFile: File) {
        val url = summary.apkDownloadUrl
            ?: throw IllegalStateException("该发行版未附带 APK 资产")
        destFile.parentFile?.mkdirs()
        if (destFile.exists()) destFile.delete()
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/octet-stream")
            .header("User-Agent", "hush-note-android")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("下载失败 (HTTP ${response.code})")
            }
            val body = response.body ?: throw IllegalStateException("下载失败：无响应体")
            FileOutputStream(destFile).use { out ->
                body.byteStream().use { input -> input.copyTo(out) }
            }
        }
    }

    private fun parseObject(raw: String?): JsonObject {
        if (raw.isNullOrBlank()) throw IllegalStateException("GitHub API 无响应")
        val element = JsonParser.parseString(raw)
        if (!element.isJsonObject) throw IllegalStateException("GitHub API 响应格式异常")
        return element.asJsonObject
    }

    private fun releaseSummaryFrom(root: JsonObject): GithubReleaseSummary {
        val releaseId = readLong(root, "id") ?: 0L
        val tagName = readString(root, "tag_name").orEmpty()
        val name = readString(root, "name").orEmpty()
        val displayName = name.ifBlank { tagName }.ifBlank { "最新发行版" }
        val rawTime = readString(root, "published_at") ?: readString(root, "created_at")
        val dateLabel = formatReleaseTime(rawTime)
        val htmlUrl = readString(root, "html_url") ?: GithubReleaseConfig.releasesPageUrl

        var apkName: String? = null
        var apkUrl: String? = null
        root.get("assets")?.takeIf { it.isJsonArray }?.asJsonArray?.let { assets: JsonArray ->
            for (element in assets) {
                if (!element.isJsonObject) continue
                val obj = element.asJsonObject
                val assetName = readString(obj, "name") ?: continue
                if (!assetName.endsWith(".apk", ignoreCase = true)) continue
                val downloadUrl = readString(obj, "browser_download_url") ?: continue
                apkName = assetName
                apkUrl = downloadUrl
                break
            }
        }
        return GithubReleaseSummary(
            releaseId = releaseId,
            tagName = tagName,
            displayName = displayName,
            dateLabel = dateLabel,
            apkAssetName = apkName,
            apkDownloadUrl = apkUrl,
            htmlUrl = htmlUrl
        )
    }

    private fun readString(obj: JsonObject, key: String): String? {
        val value = obj.get(key) ?: return null
        if (value.isJsonNull) return null
        return value.asString.trim().takeIf { it.isNotEmpty() }
    }

    private fun readLong(obj: JsonObject, key: String): Long? {
        val value = obj.get(key) ?: return null
        if (value.isJsonNull) return null
        return when {
            value.isJsonPrimitive && value.asJsonPrimitive.isNumber -> value.asLong
            value.isJsonPrimitive -> value.asString.toLongOrNull()
            else -> null
        }
    }

    private fun formatReleaseTime(iso: String?): String {
        if (iso.isNullOrBlank()) return "—"
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ssX",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss"
        )
        for (pattern in patterns) {
            try {
                val parser = SimpleDateFormat(pattern, Locale.getDefault())
                if (pattern.endsWith("'Z'")) parser.timeZone = TimeZone.getTimeZone("UTC")
                val date = parser.parse(iso) ?: continue
                return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date)
            } catch (_: Exception) {
                // try next pattern
            }
        }
        return iso.take(16).replace('T', ' ')
    }
}
