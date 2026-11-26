package com.hushnote.app.feishu

/**
 * @param pullRemote 为 false 时跳过列举/拉取远端（保存后上传用）
 * @param pushOnlyLocalIds 非空时只上传这些本地 id，避免整日记录逐个比对
 */
data class FeishuSyncOptions(
    val pullRemote: Boolean = true,
    val pushOnlyLocalIds: Set<String>? = null
)
