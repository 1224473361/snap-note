package com.hushnote.app.llm

import com.hushnote.app.model.MealConstants

object SparkPromptBuilder {
    fun systemPrompt(): String = MealConstants.SPARK_SYSTEM_PROMPT

    fun userPrompt(
        dateTime: String,
        dishes: List<String>,
        note: String,
        previousAiComment: String
    ): String {
        val dishLine = dishes.joinToString("、")
        val noteLine = note.trim().ifEmpty { "（无）" }
        val prevLine = previousAiComment.trim().ifEmpty { "（无）" }
        return """
时间：$dateTime
菜品：$dishLine
备注：$noteLine
上一篇短评：$prevLine
        """.trimIndent()
    }
}
