package com.snapnote.app.audio

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

object AudioPcmReader {
    fun readPcm(file: File): ByteArray {
        if (!file.exists() || file.length() == 0L) return ByteArray(0)
        return when (file.extension.lowercase()) {
            "wav" -> WavWriter.readPcmFromWav(file)
            "m4a", "mp4", "aac", "3gp" -> decodeCompressedAudioToPcm(file)
            else -> decodeCompressedAudioToPcm(file)
        }
    }

    private fun decodeCompressedAudioToPcm(file: File): ByteArray {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(file.absolutePath)
            val trackIndex = (0 until extractor.trackCount).firstOrNull { index ->
                extractor.getTrackFormat(index).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: return ByteArray(0)

            extractor.selectTrack(trackIndex)
            val inputFormat = extractor.getTrackFormat(trackIndex)
            val mime = inputFormat.getString(MediaFormat.KEY_MIME) ?: return ByteArray(0)

            val codec = MediaCodec.createDecoderByType(mime)
            codec.configure(inputFormat, null, null, 0)
            codec.start()

            val outputStream = java.io.ByteArrayOutputStream()
            val bufferInfo = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            var outputSampleRate = inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            var outputChannels = inputFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

            try {
                while (!outputDone) {
                    if (!inputDone) {
                        val inputIndex = codec.dequeueInputBuffer(10_000)
                        if (inputIndex >= 0) {
                            val inputBuffer = codec.getInputBuffer(inputIndex) ?: break
                            inputBuffer.clear()
                            val sampleSize = extractor.readSampleData(inputBuffer, 0)
                            if (sampleSize < 0) {
                                codec.queueInputBuffer(
                                    inputIndex,
                                    0,
                                    0,
                                    0,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                                )
                                inputDone = true
                            } else {
                                codec.queueInputBuffer(
                                    inputIndex,
                                    0,
                                    sampleSize,
                                    extractor.sampleTime,
                                    0
                                )
                                extractor.advance()
                            }
                        }
                    }

                    when (val outputIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000)) {
                        MediaCodec.INFO_TRY_AGAIN_LATER -> Unit
                        MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            val newFormat = codec.outputFormat
                            if (newFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                                outputSampleRate = newFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                            }
                            if (newFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                                outputChannels = newFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                            }
                        }
                        else -> if (outputIndex >= 0) {
                            if (bufferInfo.size > 0) {
                                val outputBuffer = codec.getOutputBuffer(outputIndex) ?: break
                                val chunk = ByteArray(bufferInfo.size)
                                outputBuffer.position(bufferInfo.offset)
                                outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                                outputBuffer.get(chunk)
                                outputStream.write(chunk)
                            }
                            codec.releaseOutputBuffer(outputIndex, false)
                            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                                outputDone = true
                            }
                        }
                    }
                }
            } finally {
                codec.runCatching { stop() }
                codec.runCatching { release() }
            }

            var pcm = outputStream.toByteArray()
            if (pcm.isEmpty()) return ByteArray(0)

            pcm = toMono16BitLe(pcm, outputChannels)
            return if (outputSampleRate == 16000) pcm else resampleTo16k(pcm, outputSampleRate)
        } catch (_: Exception) {
            return ByteArray(0)
        } finally {
            extractor.release()
        }
    }

    private fun toMono16BitLe(pcm: ByteArray, channelCount: Int): ByteArray {
        if (channelCount <= 1 || pcm.size < 4) return pcm
        val frameCount = pcm.size / 2 / channelCount
        val mono = ByteArray(frameCount * 2)
        val input = ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN)
        val output = ByteBuffer.wrap(mono).order(ByteOrder.LITTLE_ENDIAN)
        repeat(frameCount) {
            var mixed = 0
            repeat(channelCount) {
                mixed += input.short.toInt()
            }
            output.putShort((mixed / channelCount).toShort())
        }
        return mono
    }

    private fun resampleTo16k(pcm: ByteArray, sourceRate: Int): ByteArray {
        if (sourceRate <= 0 || pcm.size < 2) return pcm
        val shortCount = pcm.size / 2
        val samples = ShortArray(shortCount)
        ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples)
        val ratio = sourceRate.toDouble() / 16000.0
        val targetCount = (shortCount / ratio).toInt().coerceAtLeast(1)
        val output = ShortArray(targetCount)
        for (i in 0 until targetCount) {
            val srcIndex = (i * ratio).toInt().coerceIn(0, shortCount - 1)
            output[i] = samples[srcIndex]
        }
        val bytes = ByteArray(targetCount * 2)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(output)
        return bytes
    }
}
