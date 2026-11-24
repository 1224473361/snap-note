package com.snapnote.app.llm

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SparkPromptBuilderTest {
    @Test
    fun userPromptIncludesDishesNoteAndPreviousComment() {
        val text = SparkPromptBuilder.userPrompt(
            dateTime = "2026-09-04 12:30",
            dishes = listOf("小米粥", "西红柿鸡蛋"),
            note = "有点咸",
            previousAiComment = "上一篇短评"
        )
        assertTrue(text.contains("2026-09-04 12:30"))
        assertTrue(text.contains("小米粥"))
        assertTrue(text.contains("西红柿鸡蛋"))
        assertTrue(text.contains("有点咸"))
        assertTrue(text.contains("上一篇短评"))
    }

    @Test
    fun userPromptOmitsEmptyNoteAndPrevious() {
        val text = SparkPromptBuilder.userPrompt(
            dateTime = "2026-09-04 12:30",
            dishes = listOf("馒头"),
            note = "  ",
            previousAiComment = ""
        )
        assertTrue(text.contains("馒头"))
        assertFalse(text.contains("上一篇短评：上一"))
        assertTrue(text.contains("备注：（无）"))
        assertTrue(text.contains("上一篇短评：（无）"))
    }
}
