package com.hushnote.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hushnote.app.model.AsrLogEntry
import com.hushnote.app.model.MemoConstants

class AsrLogRepository(context: Context) {
    private val prefs = context.getSharedPreferences(MemoConstants.PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val logs = mutableListOf<AsrLogEntry>()

    fun load() {
        val raw = prefs.getString(MemoConstants.PREF_KEY_ASR_LOG_LIST, "[]") ?: "[]"
        val type = object : TypeToken<List<AsrLogEntry>>() {}.type
        logs.clear()
        logs.addAll(
            try {
                gson.fromJson<List<AsrLogEntry>>(raw, type) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        )
    }

    fun getAll(): List<AsrLogEntry> = logs.sortedByDescending { it.timestamp }

    @Synchronized
    fun append(entry: AsrLogEntry) {
        logs.add(0, entry)
        while (logs.size > MemoConstants.MAX_ASR_LOG_COUNT) {
            logs.removeAt(logs.lastIndex)
        }
        persist()
    }

    @Synchronized
    fun clearAll() {
        logs.clear()
        persist()
    }

    private fun persist() {
        prefs.edit().putString(MemoConstants.PREF_KEY_ASR_LOG_LIST, gson.toJson(logs)).commit()
    }
}
