package com.hushnote.app.util

import com.hushnote.app.model.MealItem

object MealExportFormatter {
    fun format(dateKey: String, items: List<MealItem>): String {
        if (items.isEmpty()) return dateKey

        return buildString {
            append(dateKey)
            items.forEach { meal ->
                append('\n')
                append(DateUtils.formatTime(meal.timestamp))
                append(' ')
                append(meal.dishes.joinToString("、").ifBlank { "（未填写菜品）" })
                if (meal.note.isNotBlank()) {
                    append(' ')
                    append(meal.note)
                }
            }
        }
    }
}
