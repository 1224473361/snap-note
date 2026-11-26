package com.hushnote.app.model

enum class HomeMode {
    NOTE,
    MEAL,
    FEEDING,
    CHRONICLE
}

enum class MealSource {
    CAMERA,
    GALLERY
}

data class MealItem(
    val id: String,
    val timestamp: Long,
    val dateKey: String,
    val dishes: List<String>,
    val note: String = "",
    val aiComment: String = "",
    val imagePath: String,
    val thumbnailPath: String,
    val source: MealSource
)

data class MealGroup(
    val dateKey: String,
    val items: List<MealItem>
)

data class DeletedMealItem(
    val item: MealItem,
    val deletedAt: Long
)

/** Gson 反序列化可能把带默认值的字段写成 null，加载后统一压成安全值。 */
@Suppress("UNCHECKED_CAST")
fun MealItem.ensureDefaults(): MealItem {
    val safeDishes = (dishes as List<String>?) ?: emptyList()
    val safeNote = (note as String?) ?: ""
    val safeAi = (aiComment as String?) ?: ""
    if (safeDishes === dishes && safeNote == note && safeAi == aiComment) return this
    return copy(dishes = safeDishes, note = safeNote, aiComment = safeAi)
}

