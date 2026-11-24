package com.snapnote.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.snapnote.app.model.DeletedMealItem
import com.snapnote.app.model.MealConstants
import com.snapnote.app.model.MealGroup
import com.snapnote.app.model.MealItem
import com.snapnote.app.model.MemoConstants
import com.snapnote.app.model.ensureDefaults
import java.io.File

class MealRepository(context: Context) {
    private val prefs = context.getSharedPreferences(MemoConstants.PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val imageDir = File(context.filesDir, MealConstants.IMAGE_DIR).apply { mkdirs() }
    private val thumbDir = File(context.filesDir, MealConstants.THUMB_DIR).apply { mkdirs() }

    private var meals: MutableList<MealItem> = mutableListOf()
    private var trash: MutableList<DeletedMealItem> = mutableListOf()

    fun imageDirectory(): File = imageDir

    fun thumbDirectory(): File = thumbDir

    fun buildImagePath(id: String, extension: String = "jpg"): File =
        File(imageDir, "$id.$extension")

    fun buildThumbPath(id: String): File = File(thumbDir, "$id.jpg")

    fun getMealById(id: String): MealItem? = meals.find { it.id == id }

    fun getAllMeals(): List<MealItem> = meals.toList()

    fun getAllTrash(): List<DeletedMealItem> = trash.toList()

    fun load() {
        val raw = prefs.getString(MealConstants.PREF_KEY_MEAL_LIST, "[]") ?: "[]"
        val type = object : TypeToken<List<MealItem>>() {}.type
        meals = try {
            gson.fromJson<List<MealItem>>(raw, type)
                ?.map { it.ensureDefaults() }
                ?.toMutableList()
                ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
        sortMeals()
        loadTrash()
    }

    private fun loadTrash() {
        val raw = prefs.getString(MealConstants.PREF_KEY_MEAL_TRASH_LIST, "[]") ?: "[]"
        val type = object : TypeToken<List<DeletedMealItem>>() {}.type
        trash = try {
            gson.fromJson<List<DeletedMealItem>>(raw, type)
                ?.map { DeletedMealItem(it.item.ensureDefaults(), it.deletedAt) }
                ?.toMutableList()
                ?: mutableListOf()
        } catch (_: Exception) {
            mutableListOf()
        }
        collapseTrashIfNeeded()
    }

    fun getAvailableDateKeys(): List<String> =
        meals.map { it.dateKey }.distinct().sortedDescending()

    fun getMealsByDate(dateKey: String): List<MealItem> =
        meals.filter { it.dateKey == dateKey }.sortedBy { it.timestamp }

    fun getGrouped(): List<MealGroup> {
        return meals.groupBy { it.dateKey }
            .map { (dateKey, items) ->
                MealGroup(dateKey, items.sortedByDescending { it.timestamp })
            }
            .sortedByDescending { it.dateKey }
    }

    fun search(dateKey: String?, keyword: String): List<MealGroup> {
        val normalizedKeyword = keyword.trim()
        val filtered = meals.filter { meal ->
            (dateKey.isNullOrBlank() || meal.dateKey == dateKey) &&
                (normalizedKeyword.isEmpty() ||
                    MealSearch.matches(meal.dishes, meal.note, normalizedKeyword))
        }
        return filtered.groupBy { it.dateKey }
            .map { (key, items) -> MealGroup(key, items.sortedByDescending { it.timestamp }) }
            .sortedByDescending { it.dateKey }
    }

    fun getTrash(): List<DeletedMealItem> {
        collapseTrashIfNeeded()
        return trash.sortedByDescending { it.deletedAt }
    }

    suspend fun add(item: MealItem) {
        meals.add(0, item)
        sortMeals()
        persist()
    }

    suspend fun update(item: MealItem) {
        val index = meals.indexOfFirst { it.id == item.id }
        if (index < 0) return
        meals[index] = item
        sortMeals()
        persist()
    }

    suspend fun upsert(item: MealItem) {
        val index = meals.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            val existing = meals[index]
            meals[index] = item.copy(
                imagePath = existing.imagePath.takeIf { File(it).exists() } ?: item.imagePath,
                thumbnailPath = existing.thumbnailPath.takeIf { File(it).exists() } ?: item.thumbnailPath,
                source = existing.source
            )
        } else {
            meals.add(0, item)
        }
        sortMeals()
        persist()
    }

    suspend fun moveToTrash(id: String) {
        val index = meals.indexOfFirst { it.id == id }
        if (index < 0) return
        val item = meals.removeAt(index)
        trash.removeAll { it.item.id == id }
        trash.add(0, DeletedMealItem(item, System.currentTimeMillis()))
        persist()
        persistTrash()
    }

    suspend fun restoreFromTrash(id: String) {
        val deleted = trash.firstOrNull { it.item.id == id } ?: return
        trash.removeAll { it.item.id == id }
        meals.add(0, deleted.item)
        sortMeals()
        persist()
        persistTrash()
    }

    suspend fun deletePermanently(id: String) {
        val removed = trash.filter { it.item.id == id }
        if (removed.isEmpty()) return
        trash.removeAll { it.item.id == id }
        persistTrash()
        removed.forEach { deleteImageFiles(it.item) }
    }

    suspend fun emptyTrashPermanently() {
        trash.forEach { deleteImageFiles(it.item) }
        trash.clear()
        persistTrash()
    }

    fun deleteImageFiles(item: MealItem) {
        runCatching { File(item.imagePath).delete() }
        runCatching { File(item.thumbnailPath).delete() }
    }

    private fun sortMeals() {
        meals.sortByDescending { it.timestamp }
    }

    private fun persist() {
        prefs.edit().putString(MealConstants.PREF_KEY_MEAL_LIST, gson.toJson(meals)).commit()
    }

    private fun persistTrash() {
        prefs.edit().putString(MealConstants.PREF_KEY_MEAL_TRASH_LIST, gson.toJson(trash)).commit()
    }

    private fun collapseTrashIfNeeded() {
        if (trash.collapseDuplicateIds { it.item.id }) persistTrash()
    }
}
