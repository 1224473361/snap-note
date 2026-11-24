package com.snapnote.app.model

/** 应用内文字缩放（在系统字体缩放之上叠加）。 */
enum class AppFontSize(val scale: Float, val label: String) {
    SMALL(1f, "小"),
    MEDIUM(1.14f, "中"),
    LARGE(1.2f, "大"),
    EXTRA_LARGE(1.5f, "特大");

    val scaleLabel: String
        get() = if (scale == 1f) "1×" else "${scale}×"

    companion object {
        fun fromStored(raw: String?): AppFontSize {
            if (raw.isNullOrBlank()) return MEDIUM
            return runCatching { valueOf(raw) }.getOrDefault(MEDIUM)
        }
    }
}
