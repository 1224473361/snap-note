package com.hushnote.app.update

import com.hushnote.app.BuildConfig

/**
 * GitHub Releases 配置。
 *
 * 仓库 owner / repo 通过 local.properties 注入（见 local.properties.example），
 * 便于 fork 者改成自己的仓库；未配置时使用本项目默认值。
 * GitHub 公开仓库的 Releases API 不需要令牌。
 */
object GithubReleaseConfig {
    val owner: String = BuildConfig.GITHUB_RELEASE_OWNER.ifBlank { DEFAULT_OWNER }
    val repo: String = BuildConfig.GITHUB_RELEASE_REPO.ifBlank { DEFAULT_REPO }

    val releasesPageUrl: String get() = "https://github.com/$owner/$repo/releases"
    val apiLatestUrl: String get() = "https://api.github.com/repos/$owner/$repo/releases/latest"

    private const val DEFAULT_OWNER = "1224473361"
    private const val DEFAULT_REPO = "hush-note"
}
