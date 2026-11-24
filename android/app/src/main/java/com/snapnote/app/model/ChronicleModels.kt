package com.snapnote.app.model

enum class ChronicleType {
    WEIGHT,
    HEIGHT,
    MILESTONE
}

enum class ChronicleFilter {
    ALL,
    WEIGHT,
    HEIGHT,
    MILESTONE
}

data class ChronicleItem(
    val id: String,
    val type: ChronicleType,
    val timestamp: Long,
    val dateKey: String,
    val value: Double? = null,
    val title: String = "",
    val body: String = "",
    val imagePaths: List<String> = emptyList(),
    val updatedAt: Long = timestamp
)

data class ChronicleGroup(
    val dateKey: String,
    val items: List<ChronicleItem>
)

data class DeletedChronicleItem(
    val item: ChronicleItem,
    val deletedAt: Long
)
