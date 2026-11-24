package com.snapnote.app.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.snapnote.app.model.MemoConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

data class RecordingResult(
    val path: String,
    val durationSeconds: Int,
    /** 供讯飞转写使用的 wav 侧车文件（若存在） */
    val asrWavPath: String? = null
)

/**
 * 使用 AudioRecord 采集 PCM，落盘 wav 供 ASR，再编码 m4a 供播放。
 * 避免在华为等机型上对 m4a 软解码失败导致「录音为空」。
 */
class RecordingService(
    @Suppress("UNUSED_PARAMETER") private val context: Context,
    private val scope: CoroutineScope,
    private val onDurationTick: (Int) -> Unit
) {
    private var audioRecord: AudioRecord? = null
    private var readJob: Job? = null
    private var timerJob: Job? = null
    private var pcmStream = ByteArrayOutputStream()
    private var recording = false
    private var durationSeconds = 0
    private var wavFile: File? = null
    private var m4aFile: File? = null
    private var minBufferSize = 0

    fun isRecording(): Boolean = recording

    fun start(wavOutput: File, m4aOutput: File): Boolean {
        if (recording) return false
        wavFile = wavOutput
        m4aFile = m4aOutput
        wavOutput.parentFile?.mkdirs()
        m4aOutput.parentFile?.mkdirs()
        pcmStream = ByteArrayOutputStream()
        durationSeconds = 0
        onDurationTick(durationSeconds)

        minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBufferSize <= 0) return false

        val recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize * 2
        )
        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            return false
        }

        return try {
            recorder.startRecording()
            audioRecord = recorder
            recording = true

            readJob = scope.launch(Dispatchers.IO) {
                val buffer = ByteArray(minBufferSize)
                while (isActive && recording) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        synchronized(pcmStream) {
                            pcmStream.write(buffer, 0, read)
                        }
                    }
                }
            }

            timerJob = scope.launch {
                while (isActive && recording) {
                    delay(1000)
                    durationSeconds += 1
                    onDurationTick(durationSeconds)
                }
            }
            true
        } catch (_: Exception) {
            releaseAll(deleteOutput = true)
            false
        }
    }

    fun cancel() {
        recording = false
        timerJob?.cancel()
        readJob?.cancel()
        releaseAll(deleteOutput = true)
        wavFile = null
        m4aFile = null
    }

    fun stop(): RecordingResult? {
        if (!recording) return null
        recording = false
        timerJob?.cancel()

        val wav = wavFile ?: return null
        val m4a = m4aFile ?: return null
        val capturedDuration = durationSeconds

        audioRecord?.runCatching { stop() }
        readJob?.cancel()
        drainRemaining(audioRecord)
        releaseAll(deleteOutput = false)

        wavFile = null
        m4aFile = null

        if (capturedDuration < MemoConstants.MIN_RECORDING_DURATION_SECONDS) {
            wav.delete()
            m4a.delete()
            return null
        }

        val pcm = synchronized(pcmStream) {
            PcmGain.apply(pcmStream.toByteArray(), AudioConfig.RECORDING_GAIN)
        }
        pcmStream = ByteArrayOutputStream()
        if (pcm.isEmpty()) {
            wav.delete()
            m4a.delete()
            return null
        }

        WavWriter.writePcmAsWav(wav, pcm, SAMPLE_RATE)
        if (!wav.exists() || wav.length() <= 44) {
            wav.delete()
            m4a.delete()
            return null
        }

        val playbackPath = if (AacM4aEncoder.encodeWavToM4a(wav, m4a)) {
            m4a.absolutePath
        } else {
            wav.absolutePath
        }

        return RecordingResult(
            path = playbackPath,
            durationSeconds = capturedDuration,
            asrWavPath = if (playbackPath == m4a.absolutePath) wav.absolutePath else null
        )
    }

    private fun drainRemaining(record: AudioRecord?) {
        if (record == null || minBufferSize <= 0) return
        val buffer = ByteArray(minBufferSize)
        repeat(8) {
            val read = record.read(buffer, 0, buffer.size)
            if (read <= 0) return
            synchronized(pcmStream) {
                pcmStream.write(buffer, 0, read)
            }
        }
    }

    private fun releaseAll(deleteOutput: Boolean) {
        audioRecord?.runCatching {
            stop()
            release()
        }
        audioRecord = null
        if (deleteOutput) {
            wavFile?.delete()
            m4aFile?.delete()
        }
    }

    companion object {
        private const val SAMPLE_RATE = 16000
    }
}
