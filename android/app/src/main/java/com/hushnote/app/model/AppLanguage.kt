package com.hushnote.app.model

/**
 * 应用语言。默认中文，同时提供 English 与跟随系统选项。
 *
 * [storedValue] 用于 SharedPreferences 持久化，[localeTag] 用于 BCP-47 语言标签。
 */
enum class AppLanguage(
    val label: String,
    val storedValue: String,
    val localeTag: String
) {
    CHINESE("中文", "zh", "zh"),
    ENGLISH("English", "en", "en"),
    SYSTEM("跟随系统", "system", "");

    companion object {
        /** 未配置或无法识别时回退到中文，保证「默认中文」。 */
        fun fromStored(raw: String?): AppLanguage {
            if (raw.isNullOrBlank()) return CHINESE
            return entries.firstOrNull { it.storedValue.equals(raw, ignoreCase = true) }
                ?: runCatching { valueOf(raw) }.getOrDefault(CHINESE)
        }
    }
}
