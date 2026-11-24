package com.snapnote.app.asr

interface SpeechToTextProvider {
    suspend fun transcribe(audioPath: String): String
}
