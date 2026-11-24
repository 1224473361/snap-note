package com.snapnote.app.domain

import com.snapnote.app.asr.AsrServiceFactory
import com.snapnote.app.asr.IflytekConfig
import com.snapnote.app.data.MemoRepository
import com.snapnote.app.model.MemoConstants
import com.snapnote.app.model.MemoItem
import com.snapnote.app.model.MemoStatus
import com.snapnote.app.util.DateUtils

import java.io.File

class MemoProcessService(
    private val repository: MemoRepository,
    private val asrFactory: AsrServiceFactory,
    private val onUpdated: () -> Unit
) {
    suspend fun processNewRecording(
        memoId: String,
        audioPath: String,
        durationSeconds: Int,
        asrAudioPath: String? = null
    ): String? {
        val transcribePath = asrAudioPath?.takeIf { File(it).exists() } ?: audioPath
        val timestamp = System.currentTimeMillis()
        val processingItem = MemoItem(
            id = memoId,
            content = MemoConstants.PROCESSING,
            audioPath = audioPath,
            timestamp = timestamp,
            dateKey = DateUtils.toDateKey(timestamp),
            durationSeconds = durationSeconds,
            status = MemoStatus.PROCESSING
        )
        repository.add(processingItem)
        onUpdated()

        var content = MemoConstants.UNRECOGNIZED
        var errorMessage: String? = null
        try {
            if (!IflytekConfig.isConfigured()) {
                throw IllegalStateException(IflytekConfig.NOT_CONFIGURED_HINT)
            }
            val text = asrFactory.create().transcribe(transcribePath)
            if (text.isNotBlank()) {
                content = text
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "转写失败"
        } finally {
            asrAudioPath?.let { sidecar ->
                if (sidecar != audioPath) File(sidecar).delete()
            }
        }

        repository.update(
            processingItem.copy(content = content, status = MemoStatus.DONE)
        )
        onUpdated()
        return errorMessage
    }
}
