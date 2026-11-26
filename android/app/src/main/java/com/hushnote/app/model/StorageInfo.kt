package com.hushnote.app.model

data class StorageInfo(
    val memoCount: Int,
    val trashCount: Int,
    val audioBytes: Long,
    val trashAudioBytes: Long,
    val asrLogCount: Int
)
