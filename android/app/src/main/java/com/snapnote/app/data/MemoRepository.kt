package com.snapnote.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.snapnote.app.model.DeletedMemoItem
import com.snapnote.app.model.MemoConstants
import com.snapnote.app.model.MemoGroup
import com.snapnote.app.model.MemoItem
import com.snapnote.app.model.MemoStatus
import com.snapnote.app.model.StorageInfo
import java.io.File

class MemoRepository(context: Context) {
    private val prefs = context.getSharedPreferences(MemoConstants.PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val audioDir = File(context.filesDir, MemoConstants.AUDIO_DIR).apply { mkdirs() }

    private var memos: MutableList<MemoItem> = mutableListOf()
    private var trash: MutableList<DeletedMemoItem> = mutableListOf()

    fun audioDirectory(): File = audioDir

    fun buildAudioPath(id: String): File = File(audioDir, "$id.m4a")

    fun buildWavPath(id: String): File = File(audioDir, "$id.wav")

    fun getMemoById(id: String): MemoItem? = memos.find { it.id == id }

    fun getAllMemos(): List<MemoItem> = memos.toList()

    fun getAllTrash(): List<DeletedMemoItem> = trash.toList()

    fun getStorageInfo(asrLogCount: Int): StorageInfo {
        val audioBytes = memos.sumOf { File(it.audioPath).takeIf(File::exists)?.length() ?: 0L }
        val trashAudioBytes = trash.sumOf { File(it.item.audioPath).takeIf(File::exists)?.length() ?: 0L }
        return StorageInfo(
            memoCount = memos.count { it.status == MemoStatus.DONE },
            trashCount = trash.size,
            audioBytes = audioBytes,
            trashAudioBytes = trashAudioBytes,
            asrLogCount = asrLogCount
        )
    }

    fun load() {
        val raw = prefs.getString(MemoConstants.PREF_KEY_MEMO_LIST, "[]") ?: "[]"
        val type = object : TypeToken<List<MemoItem>>() {}.type
        memos = try {
            gson.fromJson<List<MemoItem>>(raw, type)?.toMutableList() ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
        sortMemos()
        loadTrash()
    }

    private fun loadTrash() {
        val raw = prefs.getString(MemoConstants.PREF_KEY_TRASH_LIST, "[]") ?: "[]"
        val type = object : TypeToken<List<DeletedMemoItem>>() {}.type
        trash = try {
            gson.fromJson<List<DeletedMemoItem>>(raw, type)?.toMutableList() ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
        collapseTrashIfNeeded()
    }

    fun getGrouped(): List<MemoGroup> {
        return memos.groupBy { it.dateKey }
            .map { (dateKey, items) ->
                MemoGroup(dateKey, items.sortedByDescending { it.timestamp })
            }
            .sortedByDescending { it.dateKey }
    }

    fun getAvailableDateKeys(): List<String> =
        memos.filter { it.status == MemoStatus.DONE }
            .map { it.dateKey }
            .distinct()
            .sortedDescending()

    fun getMemosByDate(dateKey: String): List<MemoItem> =
        memos.filter { it.dateKey == dateKey && it.status == MemoStatus.DONE }
            .sortedByDescending { it.timestamp }

    fun search(dateKey: String?, keyword: String): List<MemoGroup> {
        val normalizedKeyword = keyword.trim()
        val filtered = memos.filter { memo ->
            memo.status == MemoStatus.DONE &&
                (dateKey.isNullOrBlank() || memo.dateKey == dateKey) &&
                (normalizedKeyword.isEmpty() || memo.content.contains(normalizedKeyword, ignoreCase = true))
        }
        return filtered.groupBy { it.dateKey }
            .map { (key, items) -> MemoGroup(key, items.sortedByDescending { it.timestamp }) }
            .sortedByDescending { it.dateKey }
    }

    fun getTrash(): List<DeletedMemoItem> {
        collapseTrashIfNeeded()
        return trash.sortedByDescending { it.deletedAt }
    }

    suspend fun add(item: MemoItem) {
        memos.add(0, item)
        sortMemos()
        persist()
    }

    suspend fun updateContent(id: String, content: String) {
        val index = memos.indexOfFirst { it.id == id }
        if (index < 0) return
        memos[index] = memos[index].copy(content = content.trim(), status = MemoStatus.DONE)
        persist()
    }

    suspend fun replaceAll(newMemos: List<MemoItem>, newTrash: List<DeletedMemoItem>) {
        memos = newMemos.toMutableList()
        trash = newTrash.toMutableList()
        sortMemos()
        persist()
        persistTrash()
    }

    suspend fun mergeAll(newMemos: List<MemoItem>, newTrash: List<DeletedMemoItem>) {
        val existingMemoIds = memos.map { it.id }.toSet()
        newMemos.filter { it.id !in existingMemoIds }.forEach { memos.add(it) }
        val existingTrashIds = trash.map { it.item.id }.toSet()
        newTrash.filter { it.item.id !in existingTrashIds }.forEach { trash.add(it) }
        sortMemos()
        persist()
        persistTrash()
    }

    suspend fun emptyTrashPermanently() {
        trash.forEach { deleted ->
            runCatching { File(deleted.item.audioPath).delete() }
        }
        trash.clear()
        persistTrash()
    }

    suspend fun update(item: MemoItem) {
        val index = memos.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            memos[index] = item
            sortMemos()
            persist()
        }
    }

    suspend fun upsert(item: MemoItem) {
        val index = memos.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            val existing = memos[index]
            val audioPath = existing.audioPath.takeIf { File(it).exists() } ?: item.audioPath
            memos[index] = item.copy(audioPath = audioPath)
        } else {
            memos.add(0, item)
        }
        sortMemos()
        persist()
    }

    suspend fun moveToTrash(id: String) {
        val index = memos.indexOfFirst { it.id == id }
        if (index < 0) return
        val item = memos.removeAt(index)
        trash.removeAll { it.item.id == id }
        trash.add(0, DeletedMemoItem(item, System.currentTimeMillis()))
        persist()
        persistTrash()
    }

    suspend fun restoreFromTrash(id: String) {
        val deleted = trash.firstOrNull { it.item.id == id } ?: return
        trash.removeAll { it.item.id == id }
        memos.add(0, deleted.item)
        sortMemos()
        persist()
        persistTrash()
    }

    suspend fun deletePermanently(id: String) {
        val removed = trash.filter { it.item.id == id }
        if (removed.isEmpty()) return
        trash.removeAll { it.item.id == id }
        persistTrash()
        removed.forEach { deleted ->
            runCatching { File(deleted.item.audioPath).delete() }
        }
    }

    private fun sortMemos() {
        memos.sortByDescending { it.timestamp }
    }

    private fun persist() {
        prefs.edit().putString(MemoConstants.PREF_KEY_MEMO_LIST, gson.toJson(memos)).commit()
    }

    private fun persistTrash() {
        prefs.edit().putString(MemoConstants.PREF_KEY_TRASH_LIST, gson.toJson(trash)).commit()
    }

    private fun collapseTrashIfNeeded() {
        if (trash.collapseDuplicateIds { it.item.id }) persistTrash()
    }
}
