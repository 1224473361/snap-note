package com.hushnote.app.audio

import java.nio.ByteBuffer
import java.nio.ByteOrder

object AudioConfig {
    /** 播放响度增强（millibels），100 mB ≈ 1 dB，约 12 dB */
    const val PLAYBACK_LOUDNESS_GAIN_MB = 1200

    /** 录音 PCM 数字增益，1.0 为原样，2.0 约翻倍音量 */
    const val RECORDING_GAIN = 1.5f
}

object PcmGain {
    fun apply(pcm: ByteArray, gain: Float): ByteArray {
        if (gain == 1f || pcm.size < 2) return pcm
        val input = ByteBuffer.wrap(pcm).order(ByteOrder.LITTLE_ENDIAN)
        val output = ByteArray(pcm.size)
        val out = ByteBuffer.wrap(output).order(ByteOrder.LITTLE_ENDIAN)
        while (input.remaining() >= 2) {
            val amplified = (input.short * gain).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            out.putShort(amplified.toShort())
        }
        return output
    }
}
