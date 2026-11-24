package com.snapnote.app.model

enum class AppFontPreset(val label: String, val subtitle: String) {
    SYSTEM("系统默认", "跟随手机系统字体"),
    APP("程序预设", "朗圆体（字魂 144 号）");

    companion object {
        fun fromStored(raw: String?): AppFontPreset {
            if (raw.isNullOrBlank()) return APP
            return runCatching { valueOf(raw) }.getOrDefault(APP)
        }
    }
}
