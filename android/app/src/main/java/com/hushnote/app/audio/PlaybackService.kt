package com.hushnote.app.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.audiofx.LoudnessEnhancer
import java.io.File

class PlaybackService(
    private val onError: (String) -> Unit = {},
    private val onStateChanged: () -> Unit = {}
) {
    private var player: MediaPlayer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var currentId: String = ""
    private var paused = false

    fun getCurrentId(): String = currentId

    fun isPlaying(id: String): Boolean = currentId == id && player?.isPlaying == true

    fun isPausedOn(id: String): Boolean = currentId == id && paused

    fun toggle(id: String, audioPath: String) {
        if (currentId == id) {
            when {
                player?.isPlaying == true -> {
                    player?.pause()
                    paused = true
                }
                paused -> {
                    player?.start()
                    paused = false
                }
                else -> start(id, audioPath)
            }
            onStateChanged()
            return
        }
        stop()
        start(id, audioPath)
        onStateChanged()
    }

    fun stop() {
        releaseEnhancer()
        player?.runCatching {
            stop()
            release()
        }
        player = null
        currentId = ""
        paused = false
        onStateChanged()
    }

    private fun start(id: String, audioPath: String) {
        val file = File(audioPath)
        if (!file.exists() || file.length() <= 0) {
            onError("音频文件不存在或为空")
            return
        }
        try {
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(audioPath)
                setOnCompletionListener {
                    releaseEnhancer()
                    player?.runCatching {
                        stop()
                        release()
                    }
                    player = null
                    currentId = ""
                    paused = false
                    onStateChanged()
                }
                setOnErrorListener { _, _, _ ->
                    onError("播放失败")
                    stop()
                    true
                }
                prepare()
                setVolume(1f, 1f)
                attachLoudnessEnhancer(audioSessionId)
                start()
            }
            currentId = id
            paused = false
        } catch (e: Exception) {
            onError("播放失败：${e.message ?: "未知错误"}")
            stop()
        }
    }

    private fun attachLoudnessEnhancer(sessionId: Int) {
        releaseEnhancer()
        if (AudioConfig.PLAYBACK_LOUDNESS_GAIN_MB <= 0) return
        loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
            setTargetGain(AudioConfig.PLAYBACK_LOUDNESS_GAIN_MB)
            enabled = true
        }
    }

    private fun releaseEnhancer() {
        loudnessEnhancer?.runCatching { release() }
        loudnessEnhancer = null
    }
}
