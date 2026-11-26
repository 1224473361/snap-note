package com.hushnote.app.asr

import com.hushnote.app.data.AsrLogRepository

class AsrServiceFactory(
    private val logRepository: AsrLogRepository
) {
    fun create(): SpeechToTextProvider = IflytekAsrProvider(logRepository)
}
