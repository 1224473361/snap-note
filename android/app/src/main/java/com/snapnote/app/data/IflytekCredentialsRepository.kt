package com.snapnote.app.data

import android.content.Context
import com.snapnote.app.BuildConfig
import com.snapnote.app.model.IflytekConstants
import com.snapnote.app.model.IflytekCredentials
import com.snapnote.app.model.MemoConstants

class IflytekCredentialsRepository(context: Context) {
    private val prefs = context.getSharedPreferences(MemoConstants.PREF_NAME, Context.MODE_PRIVATE)

    fun load(): IflytekCredentials = IflytekCredentials(
        appId = effectiveValue(IflytekConstants.PREF_KEY_APP_ID, BuildConfig.IFLYTEK_APP_ID),
        apiKey = effectiveValue(IflytekConstants.PREF_KEY_API_KEY, BuildConfig.IFLYTEK_API_KEY),
        apiSecret = effectiveValue(IflytekConstants.PREF_KEY_API_SECRET, BuildConfig.IFLYTEK_API_SECRET)
    )

    fun save(credentials: IflytekCredentials) {
        prefs.edit()
            .putString(IflytekConstants.PREF_KEY_APP_ID, credentials.appId.trim())
            .putString(IflytekConstants.PREF_KEY_API_KEY, credentials.apiKey.trim())
            .putString(IflytekConstants.PREF_KEY_API_SECRET, credentials.apiSecret.trim())
            .apply()
    }

    /** 安装包内已编译密钥时写入本地，避免仅依赖 BuildConfig 与 prefs 不一致 */
    fun seedFromBuildConfigIfNeeded() {
        if (load().isConfigured()) return
        val fromBuild = IflytekCredentials(
            appId = BuildConfig.IFLYTEK_APP_ID.trim(),
            apiKey = BuildConfig.IFLYTEK_API_KEY.trim(),
            apiSecret = BuildConfig.IFLYTEK_API_SECRET.trim()
        )
        if (fromBuild.isConfigured()) {
            save(fromBuild)
        }
    }

    private fun effectiveValue(prefKey: String, buildConfigValue: String): String {
        val stored = prefs.getString(prefKey, null)?.trim()
        if (!stored.isNullOrBlank()) return stored
        return buildConfigValue.trim()
    }
}
