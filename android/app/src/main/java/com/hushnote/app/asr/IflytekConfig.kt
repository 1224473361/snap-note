package com.hushnote.app.asr

import com.hushnote.app.data.IflytekCredentialsRepository
import com.hushnote.app.model.IflytekCredentials

object IflytekConfig {
    private var repository: IflytekCredentialsRepository? = null

    const val HOST = "iat-api.xfyun.cn"
    const val PATH = "/v2/iat"
    const val CHUNK_SIZE = 1280

    const val NOT_CONFIGURED_HINT =
        "讯飞密钥未配置，请在「我的 → 讯飞语音」填写，或在 local.properties 配置后重新编译安装"

    fun bind(repository: IflytekCredentialsRepository) {
        this.repository = repository
        repository.seedFromBuildConfigIfNeeded()
    }

    private fun credentials(): IflytekCredentials = repository?.load() ?: IflytekCredentials()

    val appId: String get() = credentials().appId
    val apiKey: String get() = credentials().apiKey
    val apiSecret: String get() = credentials().apiSecret

    fun isConfigured(): Boolean = credentials().isConfigured()
}
