package com.hushnote.app.model

data class IflytekCredentials(
    val appId: String = "",
    val apiKey: String = "",
    val apiSecret: String = ""
) {
    fun isConfigured(): Boolean =
        appId.isNotBlank() && apiKey.isNotBlank() && apiSecret.isNotBlank()
}

object IflytekConstants {
    const val PREF_KEY_APP_ID = "iflytek_app_id"
    const val PREF_KEY_API_KEY = "iflytek_api_key"
    const val PREF_KEY_API_SECRET = "iflytek_api_secret"
}
