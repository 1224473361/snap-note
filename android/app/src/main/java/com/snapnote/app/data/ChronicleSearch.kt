package com.snapnote.app.data

import com.snapnote.app.model.ChronicleItem
import com.snapnote.app.model.ChronicleType
import com.snapnote.app.util.ChronicleFormat

object ChronicleSearch {
    fun matches(item: ChronicleItem, keyword: String): Boolean {
        if (keyword.isEmpty()) return true
        val lower = keyword.lowercase()
        val haystack = buildList {
            when (item.type) {
                ChronicleType.WEIGHT -> add("体重")
                ChronicleType.HEIGHT -> add("身高")
                ChronicleType.MILESTONE -> add("里程碑")
            }
            item.value?.let { add(ChronicleFormat.formatDecimal(it)) }
            if (item.title.isNotBlank()) add(item.title)
            if (item.body.isNotBlank()) add(item.body)
        }.joinToString(" ").lowercase()
        return haystack.contains(lower)
    }
}
