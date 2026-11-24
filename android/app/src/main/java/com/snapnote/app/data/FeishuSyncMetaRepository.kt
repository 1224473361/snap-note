package com.snapnote.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.snapnote.app.model.MemoConstants

class FeishuSyncMetaRepository(context: Context) {
    data class Entry(
        val recordId: String = "",
        val updatedAt: Long = 0L
    )

    private val prefs = context.getSharedPreferences(MemoConstants.PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun get(localId: String): Entry? = loadAll()[localId]

    fun set(localId: String, recordId: String, updatedAt: Long) {
        val all = loadAll().toMutableMap()
        all[localId] = Entry(recordId, updatedAt)
        persist(all)
    }

    fun remove(localId: String) {
        val all = loadAll().toMutableMap()
        if (all.remove(localId) != null) {
            persist(all)
        }
    }

    fun getAll(): Map<String, Entry> = loadAll()

    private fun loadAll(): Map<String, Entry> {
        val raw = prefs.getString(PREF_KEY, "{}") ?: "{}"
        val type = object : TypeToken<Map<String, Entry>>() {}.type
        return try {
            gson.fromJson<Map<String, Entry>>(raw, type) ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun persist(all: Map<String, Entry>) {
        prefs.edit().putString(PREF_KEY, gson.toJson(all)).apply()
    }

    companion object {
        private const val PREF_KEY = "feishu_sync_meta"
    }
}
