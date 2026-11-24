package com.snapnote.app.util

import com.snapnote.app.model.MemoItem
import com.snapnote.app.model.MemoStatus

object MemoExportFormatter {
    fun format(dateKey: String, items: List<MemoItem>): String {
        val exportItems = items
            .filter { it.status == MemoStatus.DONE }
            .sortedByDescending { it.timestamp }

        if (exportItems.isEmpty()) return dateKey

        return buildString {
            append(dateKey)
            exportItems.forEach { memo ->
                append('\n')
                append(DateUtils.formatTimeWithSeconds(memo.timestamp))
                append(' ')
                append(memo.content)
            }
        }
    }
}
