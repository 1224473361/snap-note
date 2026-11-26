package com.hushnote.app.asr

interface SpeechToTextProvider {
    suspend fun transcribe(audioPath: String): String
}
