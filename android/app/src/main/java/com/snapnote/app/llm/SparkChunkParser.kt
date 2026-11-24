package com.snapnote.app.llm

import com.google.gson.Gson
import com.google.gson.JsonObject

data class SparkChunk(
    val code: Int,
    val message: String,
    val contentDelta: String,
    val isLast: Boolean
)

object SparkChunkParser {
    private val gson = Gson()

    fun parse(rawJson: String): SparkChunk {
        val root = gson.fromJson(rawJson, JsonObject::class.java)
        val header = root.getAsJsonObject("header")
        val code = header?.get("code")?.asInt ?: -1
        val message = header?.get("message")?.asString ?: ""
        if (code != 0) {
            return SparkChunk(code, message, "", true)
        }
        val choices = root.getAsJsonObject("payload")?.getAsJsonObject("choices")
        val texts = choices?.getAsJsonArray("text")
        val delta = StringBuilder()
        if (texts != null) {
            for (element in texts) {
                val content = element.asJsonObject.get("content")?.asString
                if (!content.isNullOrEmpty()) delta.append(content)
            }
        }
        val headerStatus = header?.get("status")?.asInt
        val choicesStatus = choices?.get("status")?.asInt
        val isLast = headerStatus == 2 || choicesStatus == 2
        return SparkChunk(code, message, delta.toString(), isLast)
    }
}
