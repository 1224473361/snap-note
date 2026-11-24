package com.snapnote.app.vision

import com.snapnote.app.asr.IflytekConfig

object IflytekVisionConfig {
    const val HOST = "spark-api.cn-huabei-1.xf-yun.com"
    const val PATH = "/v2.1/image"
    const val DOMAIN = "imagev3"

    val appId: String get() = IflytekConfig.appId
    val apiKey: String get() = IflytekConfig.apiKey
    val apiSecret: String get() = IflytekConfig.apiSecret

    fun isConfigured(): Boolean = IflytekConfig.isConfigured()
}
