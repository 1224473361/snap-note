package com.snapnote.app.data

object FeedingSearch {
    fun matches(note: String, amountMl: Int?, keyword: String): Boolean {
        val q = keyword.trim()
        if (q.isEmpty()) return true
        if (note.contains(q, ignoreCase = true)) return true
        val amountText = amountMl?.toString()
        return amountText != null && amountText.contains(q)
    }
}
