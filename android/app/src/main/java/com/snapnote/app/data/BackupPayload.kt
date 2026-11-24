package com.snapnote.app.data

import com.snapnote.app.model.DeletedMemoItem
import com.snapnote.app.model.MemoItem

data class BackupPayload(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val memos: List<MemoItem> = emptyList(),
    val trash: List<DeletedMemoItem> = emptyList()
)
