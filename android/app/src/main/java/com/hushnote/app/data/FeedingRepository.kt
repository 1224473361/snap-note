package com.hushnote.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hushnote.app.model.DeletedFeedingItem
import com.hushnote.app.model.FeedingConstants
import com.hushnote.app.model.FeedingGroup
import com.hushnote.app.model.FeedingItem
import com.hushnote.app.model.MemoConstants
import com.hushnote.app.model.ensureDefaults

class FeedingRepository(context: Context) {
    private val prefs = context.getSharedPreferences(MemoConstants.PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private var items: MutableList<FeedingItem> = mutableListOf()
    private var trash: MutableList<DeletedFeedingItem> = mutableListOf()

    fun getById(id: String): FeedingItem? = items.find { it.id == id }

    fun getAll(): List<FeedingItem> = items.toList()

    fun getAllTrash(): List<DeletedFeedingItem> = trash.toList()

    fun load() {
        val raw = prefs.getString(FeedingConstants.PREF_KEY_FEEDING_LIST, "[]") ?: "[]"
        val type = object : TypeToken<List<FeedingItem>>() {}.type
        items = try {
            gson.fromJson<List<FeedingItem>>(raw, type)
                ?.map { it.ensureDefaults() }
                ?.toMutableList()
                ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
        sortItems()
        loadTrash()
    }

    private fun loadTrash() {
        val raw = prefs.getString(FeedingConstants.PREF_KEY_FEEDING_TRASH_LIST, "[]") ?: "[]"
        val type = object : TypeToken<List<DeletedFeedingItem>>() {}.type
        trash = try {
            gson.fromJson<List<DeletedFeedingItem>>(raw, type)
                ?.map { DeletedFeedingItem(it.item.ensureDefaults(), it.deletedAt) }
                ?.toMutableList()
                ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
        collapseTrashIfNeeded()
    }

    fun getAvailableDateKeys(): List<String> =
        items.map { it.dateKey }.distinct().sortedDescending()

    fun getFeedingsByDate(dateKey: String): List<FeedingItem> =
        items.filter { it.dateKey == dateKey }.sortedBy { it.timestamp }

    fun getGrouped(): List<FeedingGroup> {
        return items.groupBy { it.dateKey }
            .map { (dateKey, groupItems) ->
                FeedingGroup(dateKey, groupItems.sortedByDescending { it.timestamp })
            }
            .sortedByDescending { it.dateKey }
    }

    fun search(dateKey: String?, keyword: String): List<FeedingGroup> {
        val normalizedKeyword = keyword.trim()
        val filtered = items.filter { item ->
            (dateKey.isNullOrBlank() || item.dateKey == dateKey) &&
                FeedingSearch.matches(item.note, item.amountMl, normalizedKeyword)
        }
        return filtered.groupBy { it.dateKey }
            .map { (key, groupItems) ->
                FeedingGroup(key, groupItems.sortedByDescending { it.timestamp })
            }
            .sortedByDescending { it.dateKey }
    }

    fun getTrash(): List<DeletedFeedingItem> {
        collapseTrashIfNeeded()
        return trash.sortedByDescending { it.deletedAt }
    }

    suspend fun add(item: FeedingItem) {
        items.add(0, item)
        sortItems()
        persist()
    }

    suspend fun update(item: FeedingItem) {
        val index = items.indexOfFirst { it.id == item.id }
        if (index < 0) return
        items[index] = item
        sortItems()
        persist()
    }

    suspend fun upsert(item: FeedingItem) {
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
        trash.add(0, DeletedFeedingItem(item, System.currentTimeMillis()))
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
        if (trash.none { it.item.id == id }) return
        trash.removeAll { it.item.id == id }
        persistTrash()
    }

    suspend fun emptyTrashPermanently() {
        trash.clear()
        persistTrash()
    }

    private fun sortItems() {
        items.sortByDescending { it.timestamp }
    }

    private fun persist() {
        prefs.edit().putString(FeedingConstants.PREF_KEY_FEEDING_LIST, gson.toJson(items)).commit()
    }

    private fun persistTrash() {
        prefs.edit().putString(FeedingConstants.PREF_KEY_FEEDING_TRASH_LIST, gson.toJson(trash)).commit()
    }

    private fun collapseTrashIfNeeded() {
        if (trash.collapseDuplicateIds { it.item.id }) persistTrash()
    }
}
