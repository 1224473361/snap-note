package com.snapnote.app.audio

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import java.io.File

object AacM4aEncoder {
    private const val SAMPLE_RATE = 16000
    private const val CHANNEL_COUNT = 1
    private const val BIT_RATE = 48_000
    private const val TIMEOUT_US = 10_000L

    fun encodeWavToM4a(wavFile: File, m4aFile: File): Boolean {
        if (!wavFile.exists() || wavFile.length() <= 44) return false
        val pcm = WavWriter.readPcmFromWav(wavFile)
        if (pcm.size < 2) return false

        m4aFile.parentFile?.mkdirs()
        val tempFile = File(m4aFile.parentFile, "${m4aFile.name}.tmp")
        tempFile.delete()

        val format = MediaFormat.createAudioFormat(
            MediaFormat.MIMETYPE_AUDIO_AAC,
            SAMPLE_RATE,
            CHANNEL_COUNT
        ).apply {
            setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE)
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16_384)
        }

        val encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        var muxer: MediaMuxer? = null
        var trackIndex = -1
        var muxerStarted = false

        return try {
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoder.start()

            muxer = MediaMuxer(tempFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val bufferInfo = MediaCodec.BufferInfo()
            var inputOffset = 0
            var inputDone = false
            var outputDone = false

            while (!outputDone) {
                if (!inputDone) {
                    val inputIndex = encoder.dequeueInputBuffer(TIMEOUT_US)
                    if (inputIndex >= 0) {
                        val inputBuffer = encoder.getInputBuffer(inputIndex) ?: break
                        inputBuffer.clear()
                        val capacity = inputBuffer.remaining()
                        if (inputOffset >= pcm.size) {
                            encoder.queueInputBuffer(
                                inputIndex,
                                0,
                                0,
                                presentationTimeUs(inputOffset),
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            inputDone = true
                        } else {
                            val chunkSize = minOf(capacity, pcm.size - inputOffset)
                            inputBuffer.put(pcm, inputOffset, chunkSize)
                            encoder.queueInputBuffer(
                                inputIndex,
                                0,
                                chunkSize,
                                presentationTimeUs(inputOffset),
                                0
                            )
                            inputOffset += chunkSize
                        }
                    }
                }

                when (val outputIndex = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)) {
                    MediaCodec.INFO_TRY_AGAIN_LATER -> Unit
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        if (muxerStarted) return false
                        trackIndex = muxer.addTrack(encoder.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    }
                    else -> if (outputIndex >= 0) {
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                            bufferInfo.size = 0
                        }
                        if (bufferInfo.size > 0 && muxerStarted) {
                            val outputBuffer = encoder.getOutputBuffer(outputIndex) ?: break
                            outputBuffer.position(bufferInfo.offset)
                            outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                            muxer.writeSampleData(trackIndex, outputBuffer, bufferInfo)
                        }
                        encoder.releaseOutputBuffer(outputIndex, false)
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            outputDone = true
                        }
                    }
                }
            }

            if (!muxerStarted || !tempFile.exists() || tempFile.length() < 512) {
                false
            } else {
                if (m4aFile.exists()) m4aFile.delete()
                tempFile.renameTo(m4aFile)
            }
        } catch (_: Exception) {
            false
        } finally {
            encoder.runCatching { stop() }
            encoder.runCatching { release() }
            muxer?.runCatching {
                if (muxerStarted) stop()
                release()
            }
            if (!m4aFile.exists()) tempFile.delete()
        }
    }

    private fun presentationTimeUs(byteOffset: Int): Long {
        val sampleIndex = byteOffset / 2
        return sampleIndex * 1_000_000L / SAMPLE_RATE
    }
}
