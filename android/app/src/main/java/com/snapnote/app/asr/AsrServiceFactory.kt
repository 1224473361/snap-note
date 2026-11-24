package com.snapnote.app.asr

import com.snapnote.app.data.AsrLogRepository

class AsrServiceFactory(
    private val logRepository: AsrLogRepository
) {
    fun create(): SpeechToTextProvider = IflytekAsrProvider(logRepository)
}
