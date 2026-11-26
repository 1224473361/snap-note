package com.hushnote.app.feishu

import com.hushnote.app.model.ChronicleItem
import com.hushnote.app.model.ChronicleType

object ChronicleFeishuMapper {
    fun typeToFeishu(type: ChronicleType): String = when (type) {
        ChronicleType.WEIGHT -> "weight"
        ChronicleType.HEIGHT -> "height"
        ChronicleType.MILESTONE -> "milestone"
    }

    fun typeFromFeishu(raw: String?): ChronicleType? {
        val key = raw?.trim()?.lowercase() ?: return null
        return when (key) {
            "weight" -> ChronicleType.WEIGHT
            "height" -> ChronicleType.HEIGHT
            "milestone" -> ChronicleType.MILESTONE
            else -> null
        }
    }

    fun toFields(item: ChronicleItem, updatedAt: Long): Map<String, Any?> = buildMap {
        put(FeishuFields.ID, item.id)
        put(FeishuFields.TYPE, typeToFeishu(item.type))
        put(FeishuFields.TIMESTAMP, item.timestamp)
        put(FeishuFields.DATE_KEY, item.dateKey)
        put(FeishuFields.UPDATED_AT, updatedAt)
        when (item.type) {
            ChronicleType.WEIGHT, ChronicleType.HEIGHT -> {
                item.value?.let { put(FeishuFields.VALUE, it) }
                put(FeishuFields.TITLE, "")
                put(FeishuFields.BODY, "")
            }
            ChronicleType.MILESTONE -> {
                put(FeishuFields.TITLE, item.title)
                put(FeishuFields.BODY, item.body)
                put(FeishuFields.VALUE, null)
            }
        }
    }
}
