package com.hushnote.app.model

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class MealItemJsonTest {
    @Test
    fun missingAiCommentDeserializesEmpty() {
        val json = """
        {"id":"1","timestamp":1,"dateKey":"2026-09-04","dishes":["粥"],
         "note":"n","imagePath":"/a","thumbnailPath":"/b","source":"CAMERA"}
        """.trimIndent()
        val item = Gson().fromJson(json, MealItem::class.java).ensureDefaults()
        assertEquals("", item.aiComment)
        assertEquals("n", item.note)
    }
}
