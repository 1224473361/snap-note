package com.hushnote.app.model

data class DeletedMemoItem(
    val item: MemoItem,
    val deletedAt: Long
)
