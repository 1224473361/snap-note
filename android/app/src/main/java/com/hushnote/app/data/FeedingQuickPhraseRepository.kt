package com.hushnote.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hushnote.app.model.FeedingConstants
import com.hushnote.app.model.MemoConstants

class FeedingQuickPhraseRepository(context: Context) {
    private val prefs = context.getSharedPreferences(MemoConstants.PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val listType = object : TypeToken<List<String>>() {}.type

    fun load(): List<String> {
        val raw = prefs.getString(FeedingConstants.PREF_KEY_QUICK_PHRASES, null)
        if (raw.isNullOrBlank()) {
            return FeedingConstants.DEFAULT_QUICK_NOTE_CHIPS
        }
        return runCatching { gson.fromJson<List<String>>(raw, listType) }
            .getOrDefault(FeedingConstants.DEFAULT_QUICK_NOTE_CHIPS)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { FeedingConstants.DEFAULT_QUICK_NOTE_CHIPS }
    }

    fun save(phrases: List<String>) {
        val cleaned = phrases.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        prefs.edit().putString(FeedingConstants.PREF_KEY_QUICK_PHRASES, gson.toJson(cleaned)).apply()
    }
}
