package com.snapnote.app.util

import com.snapnote.app.model.FeedingItem

object FeedingExportFormatter {
    fun format(dateKey: String, items: List<FeedingItem>): String {
        if (items.isEmpty()) return dateKey

        val totalAmount = items.sumOf { it.amountMl ?: 0 }

        return buildString {
            append(dateKey)
            items.forEach { item ->
                append('\n')
                append(DateUtils.formatTime(item.timestamp))
                if (item.amountMl != null) {
                    append(' ')
                    append(item.amountMl)
                }
                if (item.note.isNotBlank()) {
                    append(' ')
                    append(item.note)
                }
            }
            if (totalAmount > 0) {
                append('\n')
                append("总计 $totalAmount ml")
            }
        }
    }
}
