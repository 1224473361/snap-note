package com.hushnote.app.audio

import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavWriter {
    fun writePcmAsWav(file: File, pcm: ByteArray, sampleRate: Int = 16000) {
        file.parentFile?.mkdirs()
        RandomAccessFile(file, "rw").use { raf ->
            raf.write(buildHeader(pcm.size, sampleRate))
            raf.write(pcm)
        }
    }

    fun readPcmFromWav(file: File): ByteArray {
        val bytes = file.readBytes()
        val headerSize = if (bytes.size > 44) 44 else 0
        return bytes.copyOfRange(headerSize, bytes.size)
    }

    private fun buildHeader(dataLength: Int, sampleRate: Int): ByteArray {
        val channels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        val buffer = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("RIFF".toByteArray())
        buffer.putInt(36 + dataLength)
        buffer.put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16)
        buffer.putShort(1)
        buffer.putShort(channels.toShort())
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort(blockAlign.toShort())
        buffer.putShort(bitsPerSample.toShort())
        buffer.put("data".toByteArray())
        buffer.putInt(dataLength)
        return buffer.array()
    }
}
