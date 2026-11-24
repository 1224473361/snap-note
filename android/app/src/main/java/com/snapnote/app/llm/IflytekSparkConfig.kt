package com.snapnote.app.llm

import com.snapnote.app.asr.IflytekConfig

object IflytekSparkConfig {
    const val HOST = "spark-api.xf-yun.com"
    const val PATH = "/v4.0/chat"
    const val DOMAIN = "4.0Ultra"
    const val MAX_TOKENS = 256
    const val UID = "snap-note-meal"

    val appId: String get() = IflytekConfig.appId
    val apiKey: String get() = IflytekConfig.apiKey
    val apiSecret: String get() = IflytekConfig.apiSecret

    fun isConfigured(): Boolean = IflytekConfig.isConfigured()
}
