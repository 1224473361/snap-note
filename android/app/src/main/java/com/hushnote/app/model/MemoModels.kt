package com.hushnote.app.model

enum class MemoStatus {
    PROCESSING,
    DONE
}

data class MemoItem(
    val id: String,
    val content: String,
    val audioPath: String,
    val timestamp: Long,
    val dateKey: String,
    val durationSeconds: Int = 0,
    val status: MemoStatus = MemoStatus.DONE
)

data class MemoGroup(
    val dateKey: String,
    val items: List<MemoItem>
)

object MemoConstants {
    const val PREF_NAME = "hush_note_prefs"
    const val PREF_KEY_MEMO_LIST = "memo_list"
    const val PREF_KEY_TRASH_LIST = "trash_list"
    const val PREF_KEY_ASR_LOG_LIST = "asr_log_list"
    const val PREF_KEY_HOME_MODE = "home_mode"
    const val PREF_KEY_APP_FONT_SIZE = "app_font_size"
    const val PREF_KEY_APP_FONT_PRESET = "app_font_preset"
    const val PREF_KEY_REDUCE_MOTION = "app_reduce_motion"
    const val PREF_KEY_SHOW_SECONDS_IN_LIST = "app_show_seconds_in_list"
    const val PREF_KEY_HAPTIC_FEEDBACK = "app_haptic_feedback"
    const val PREF_KEY_FEEDING_STATS_CARD_STYLE = "feeding_stats_card_style"
    const val PREF_KEY_APP_THEME_MODE = "app_theme_mode"
    const val PREF_KEY_APP_LANGUAGE = "app_language"
    const val AUDIO_DIR = "audio"
    const val MAX_ASR_LOG_COUNT = 100
    const val UNRECOGNIZED = "（未识别）"
    const val PROCESSING = "处理中…"
    const val CANCEL_SLIDE_THRESHOLD_PX = 80f
    const val MIN_RECORDING_DURATION_SECONDS = 1
}
