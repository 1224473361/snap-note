package com.hushnote.app.util

import com.hushnote.app.model.ChronicleItem
import com.hushnote.app.model.ChronicleType

object ChronicleExportFormatter {
    fun format(dateKey: String, items: List<ChronicleItem>): String {
        if (items.isEmpty()) return "大事记 $dateKey\n（无记录）\n"
        val header = "大事记 $dateKey\n${"=".repeat(24)}\n"
        val body = items.sortedBy { it.timestamp }.joinToString("\n\n") { item ->
            buildString {
                append(DateUtils.formatTime(item.timestamp))
                append(" · ")
                append(ChronicleFormat.typeLabel(item.type))
                append('\n')
                when (item.type) {
                    ChronicleType.WEIGHT, ChronicleType.HEIGHT -> {
                        ChronicleFormat.displayValue(item.type, item.value)?.let { append(it).append('\n') }
                        if (item.body.isNotBlank()) append(item.body).append('\n')
                    }
                    ChronicleType.MILESTONE -> {
                        if (item.title.isNotBlank()) append(item.title).append('\n')
                        if (item.body.isNotBlank()) append(item.body).append('\n')
                        if (item.imagePaths.isNotEmpty()) {
                            append("（图片 ${item.imagePaths.size} 张，仅本地）\n")
                        }
                    }
                }
            }.trimEnd()
        }
        return header + body + "\n"
    }
}
