package com.hushnote.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hushnote.app.model.ChronicleConstants
import com.hushnote.app.model.ChronicleGroup
import com.hushnote.app.model.ChronicleItem
import com.hushnote.app.model.DeletedChronicleItem
import com.hushnote.app.model.MemoConstants
import com.hushnote.app.util.ChronicleImageStore

class ChronicleRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences(MemoConstants.PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private var items: MutableList<ChronicleItem> = mutableListOf()
    private var trash: MutableList<DeletedChronicleItem> = mutableListOf()

    fun getById(id: String): ChronicleItem? = items.find { it.id == id }

    fun getAll(): List<ChronicleItem> = items.toList()

    fun load() {
        val raw = prefs.getString(ChronicleConstants.PREF_KEY_CHRONICLE_LIST, "[]") ?: "[]"
        val type = object : TypeToken<List<ChronicleItem>>() {}.type
        items = try {
            gson.fromJson<List<ChronicleItem>>(raw, type)?.toMutableList() ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
        sortItems()
        loadTrash()
    }

    private fun loadTrash() {
        val raw = prefs.getString(ChronicleConstants.PREF_KEY_CHRONICLE_TRASH_LIST, "[]") ?: "[]"
        val type = object : TypeToken<List<DeletedChronicleItem>>() {}.type
        trash = try {
            gson.fromJson<List<DeletedChronicleItem>>(raw, type)?.toMutableList() ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
        collapseTrashIfNeeded()
    }

    fun getAvailableDateKeys(): List<String> =
        items.map { it.dateKey }.distinct().sortedDescending()

    fun getByDateKey(dateKey: String): List<ChronicleItem> =
        items.filter { it.dateKey == dateKey }.sortedBy { it.timestamp }

    fun getGrouped(): List<ChronicleGroup> =
        items.groupBy { it.dateKey }
            .map { (dateKey, groupItems) ->
                ChronicleGroup(dateKey, groupItems.sortedByDescending { it.timestamp })
            }
            .sortedByDescending { it.dateKey }

    fun search(dateKey: String?, keyword: String): List<ChronicleGroup> {
        val normalizedKeyword = keyword.trim()
        val filtered = items.filter { item ->
            (dateKey.isNullOrBlank() || item.dateKey == dateKey) &&
                ChronicleSearch.matches(item, normalizedKeyword)
        }
        return filtered.groupBy { it.dateKey }
            .map { (key, groupItems) ->
                ChronicleGroup(key, groupItems.sortedByDescending { it.timestamp })
            }
            .sortedByDescending { it.dateKey }
    }

    fun getTrash(): List<DeletedChronicleItem> {
        collapseTrashIfNeeded()
        return trash.sortedByDescending { it.deletedAt }
    }

    suspend fun add(item: ChronicleItem) {
        items.add(0, item)
        sortItems()
        persist()
    }

    suspend fun update(item: ChronicleItem) {
        val index = items.indexOfFirst { it.id == item.id }
        if (index < 0) return
        items[index] = item
        sortItems()
        persist()
    }

    suspend fun upsert(item: ChronicleItem) {
        val index = items.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            items[index] = item
        } else {
            items.add(0, item)
        }
        sortItems()
        persist()
    }

    suspend fun moveToTrash(id: String) {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return
        val item = items.removeAt(index)
        trash.removeAll { it.item.id == id }
        trash.add(0, DeletedChronicleItem(item, System.currentTimeMillis()))
        persist()
        persistTrash()
    }

    suspend fun restoreFromTrash(id: String) {
        val deleted = trash.firstOrNull { it.item.id == id } ?: return
        trash.removeAll { it.item.id == id }
        items.add(0, deleted.item)
        sortItems()
        persist()
        persistTrash()
    }

    suspend fun deletePermanently(id: String) {
        val entry = trash.firstOrNull { it.item.id == id } ?: return
        ChronicleImageStore.deletePaths(entry.item.imagePaths)
        trash.removeAll { it.item.id == id }
        persistTrash()
    }

    suspend fun emptyTrashPermanently() {
        trash.forEach { ChronicleImageStore.deletePaths(it.item.imagePaths) }
        trash.clear()
        persistTrash()
    }

    private fun sortItems() {
        items.sortByDescending { it.timestamp }
    }

    private fun persist() {
        prefs.edit().putString(ChronicleConstants.PREF_KEY_CHRONICLE_LIST, gson.toJson(items)).commit()
    }

    private fun persistTrash() {
        prefs.edit()
            .putString(ChronicleConstants.PREF_KEY_CHRONICLE_TRASH_LIST, gson.toJson(trash))
            .commit()
    }

    private fun collapseTrashIfNeeded() {
        if (trash.collapseDuplicateIds { it.item.id }) persistTrash()
    }
}
