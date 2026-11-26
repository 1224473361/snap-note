package com.hushnote.app.model

enum class AppThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色"),
    DARK("深色");

    companion object {
        fun fromStored(raw: String?): AppThemeMode {
            if (raw.isNullOrBlank()) return SYSTEM
            return runCatching { valueOf(raw) }.getOrDefault(SYSTEM)
        }
    }
}

/** 与 AppCompatDelegate.MODE_NIGHT_* 对齐，供窗口/弹层跟随应用主题。 */
fun AppThemeMode.toUiNightMode(): Int = when (this) {
    AppThemeMode.SYSTEM -> -1
    AppThemeMode.LIGHT -> 1
    AppThemeMode.DARK -> 2
}

