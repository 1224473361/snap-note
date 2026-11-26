package com.hushnote.app.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MealSearchTest {
    @Test
    fun matchesDishOrNoteNotAiComment() {
        assertTrue(MealSearch.matches(listOf("红烧肉"), "好吃", "红烧"))
        assertTrue(MealSearch.matches(listOf("粥"), "有点咸", "咸"))
        assertFalse(MealSearch.matches(listOf("粥"), "有点咸", "日记感"))
    }
}
