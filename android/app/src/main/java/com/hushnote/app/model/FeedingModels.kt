package com.hushnote.app.model

data class FeedingItem(
    val id: String,
    val timestamp: Long,
    val dateKey: String,
    val amountMl: Int? = null,
    val note: String = ""
)

data class FeedingGroup(
    val dateKey: String,
    val items: List<FeedingItem>
) {
    val totalAmountMl: Int
        get() = items.sumOf { it.amountMl ?: 0 }
}

data class DeletedFeedingItem(
    val item: FeedingItem,
    val deletedAt: Long
)

@Suppress("UNCHECKED_CAST")
fun FeedingItem.ensureDefaults(): FeedingItem {
    val safeNote = (note as String?) ?: ""
    if (safeNote == note) return this
    return copy(note = safeNote)
}
