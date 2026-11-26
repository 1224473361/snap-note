package com.hushnote.app.update

data class GithubReleaseSummary(
    val releaseId: Long,
    val tagName: String,
    val displayName: String,
    val dateLabel: String,
    val apkAssetName: String?,
    val apkDownloadUrl: String?,
    val htmlUrl: String
) {
    val hasApk: Boolean get() = !apkDownloadUrl.isNullOrBlank()
}

data class GithubReleaseAsset(
    val name: String,
    val downloadUrl: String,
    val size: Long
)
