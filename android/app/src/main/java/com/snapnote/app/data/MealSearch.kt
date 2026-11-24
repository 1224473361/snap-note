package com.snapnote.app.data

object MealSearch {
    fun matches(dishes: List<String>, note: String, keyword: String): Boolean {
        val q = keyword.trim()
        if (q.isEmpty()) return true
        return dishes.any { it.contains(q, ignoreCase = true) } ||
            note.contains(q, ignoreCase = true)
    }
}
