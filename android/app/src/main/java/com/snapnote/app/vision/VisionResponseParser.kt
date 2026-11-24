package com.snapnote.app.vision

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken

object VisionResponseParser {
    private val gson = Gson()
    private val arrayType = object : TypeToken<List<String>>() {}.type

    /**
     * 从助手完整回复中提取菜品列表。容忍 markdown 代码块与前后说明文字。
     */
    fun parseDishList(raw: String): List<String> {
        val text = raw.trim()
        if (text.isEmpty()) return emptyList()

        extractJsonArray(text)?.let { return normalize(it) }

        // 尝试直接当 JSON 数组解析
        runCatching {
            val list: List<String>? = gson.fromJson(text, arrayType)
            if (list != null) return normalize(list)
        }

        return emptyList()
    }

    private fun extractJsonArray(text: String): List<String>? {
        val fenced = Regex("""```(?:json)?\s*(\[[\s\S]*?])\s*```""", RegexOption.IGNORE_CASE)
            .find(text)
            ?.groupValues
            ?.getOrNull(1)
        if (fenced != null) {
            return runCatching {
                gson.fromJson<List<String>>(fenced, arrayType)
            }.getOrNull()
        }

        val start = text.indexOf('[')
        val end = text.lastIndexOf(']')
        if (start < 0 || end <= start) return null
        val candidate = text.substring(start, end + 1)
        return runCatching {
            val element = JsonParser.parseString(candidate)
            if (!element.isJsonArray) return null
            gson.fromJson<List<String>>(element, arrayType)
        }.getOrNull()
    }

    private fun normalize(list: List<String>): List<String> =
        list.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
}
