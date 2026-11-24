package com.snapnote.app.model

enum class FeedingStatsCardStyle(val label: String, val subtitle: String) {
    STANDARD("标准", "日期与总奶量 + 2×2 四格统计"),
    COMPACT("紧凑", "缩小字号与间距，少占列表高度");

    companion object {
        fun fromStored(raw: String?): FeedingStatsCardStyle {
            if (raw.isNullOrBlank()) return STANDARD
            return runCatching { valueOf(raw) }.getOrDefault(STANDARD)
        }
    }
}
