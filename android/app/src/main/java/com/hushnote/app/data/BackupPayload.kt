package com.hushnote.app.data

import com.hushnote.app.model.DeletedMemoItem
import com.hushnote.app.model.MemoItem

data class BackupPayload(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val memos: List<MemoItem> = emptyList(),
    val trash: List<DeletedMemoItem> = emptyList()
)
