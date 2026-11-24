package com.snapnote.app.model

object FeedingConstants {
    const val PREF_KEY_FEEDING_LIST = "feeding_list"
    const val PREF_KEY_FEEDING_TRASH_LIST = "feeding_trash_list"
    const val PREF_KEY_QUICK_PHRASES = "feeding_quick_phrases"

    val DEFAULT_QUICK_NOTE_CHIPS = listOf(
        "左母乳",
        "右母乳",
        "吐奶多",
        "体重",
        "睡觉",
        "一直哼唧",
        "大便",
        "拍嗝",
        "乖",
        "不乖"
    )

    fun appendQuickNote(note: String, chip: String): String {
        val trimmed = note.trim()
        return if (trimmed.isEmpty()) chip else "$trimmed $chip"
    }

    fun appendVoiceNote(note: String, transcribed: String): String {
        val trimmed = note.trim()
        val text = transcribed.trim()
        if (text.isEmpty()) return trimmed
        return if (trimmed.isEmpty()) text else "$trimmed $text"
    }
}
