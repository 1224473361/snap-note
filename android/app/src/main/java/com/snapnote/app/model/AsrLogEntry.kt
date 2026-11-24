package com.snapnote.app.model

data class AsrLogEntry(
    val id: String,
    val timestamp: Long,
    val audioPath: String,
    val pcmBytes: Int,
    val status: String,
    val resultText: String = "",
    val errorMessage: String = "",
    val elapsedMs: Long = 0
) {
    val isSuccess: Boolean get() = status == "success"
}
