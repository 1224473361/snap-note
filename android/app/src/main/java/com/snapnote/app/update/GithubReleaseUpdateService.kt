package com.snapnote.app.update

import android.content.Context
import java.io.File

class GithubReleaseUpdateService(
    private val context: Context,
    private val apiClient: GithubReleaseApiClient = GithubReleaseApiClient()
) {
    fun fetchLatest(): GithubReleaseSummary = apiClient.fetchLatestRelease()

    fun downloadLatestApk(summary: GithubReleaseSummary): File {
        if (!summary.hasApk) {
            throw IllegalStateException("该发行版未附带 APK，请前往 GitHub Releases 页面手动下载")
        }
        val safeName = (summary.apkAssetName ?: "snap-note-latest.apk")
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
        val dest = File(context.cacheDir, "github_updates/$safeName")
        apiClient.downloadApk(summary, dest)
        if (!dest.exists() || dest.length() <= 0L) {
            throw IllegalStateException("下载失败：文件为空")
        }
        return dest
    }
}
