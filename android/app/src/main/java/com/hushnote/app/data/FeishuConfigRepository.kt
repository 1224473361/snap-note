package com.hushnote.app.data

import android.content.Context
import com.hushnote.app.model.FeishuConfig
import com.hushnote.app.model.FeishuConstants
import com.hushnote.app.model.MemoConstants

class FeishuConfigRepository(context: Context) {
    private val prefs = context.getSharedPreferences(MemoConstants.PREF_NAME, Context.MODE_PRIVATE)

    fun load(): FeishuConfig = FeishuConfig(
        enabled = prefs.getBoolean(FeishuConstants.PREF_KEY_ENABLED, false),
        appId = prefs.getString(FeishuConstants.PREF_KEY_APP_ID, "").orEmpty(),
        appSecret = prefs.getString(FeishuConstants.PREF_KEY_APP_SECRET, "").orEmpty(),
        bitableAppToken = prefs.getString(FeishuConstants.PREF_KEY_BITABLE_APP_TOKEN, "").orEmpty(),
        memoTableId = prefs.getString(FeishuConstants.PREF_KEY_MEMO_TABLE_ID, "").orEmpty(),
        mealTableId = prefs.getString(FeishuConstants.PREF_KEY_MEAL_TABLE_ID, "").orEmpty(),
        feedingTableId = prefs.getString(FeishuConstants.PREF_KEY_FEEDING_TABLE_ID, "").orEmpty(),
        chronicleTableId = prefs.getString(FeishuConstants.PREF_KEY_CHRONICLE_TABLE_ID, "").orEmpty(),
        lastSyncAt = prefs.getLong(FeishuConstants.PREF_KEY_LAST_SYNC_AT, 0L)
    )

    fun save(config: FeishuConfig) {
        prefs.edit()
            .putBoolean(FeishuConstants.PREF_KEY_ENABLED, config.enabled)
            .putString(FeishuConstants.PREF_KEY_APP_ID, config.appId.trim())
            .putString(FeishuConstants.PREF_KEY_APP_SECRET, config.appSecret.trim())
            .putString(FeishuConstants.PREF_KEY_BITABLE_APP_TOKEN, config.bitableAppToken.trim())
            .putString(FeishuConstants.PREF_KEY_MEMO_TABLE_ID, config.memoTableId.trim())
            .putString(FeishuConstants.PREF_KEY_MEAL_TABLE_ID, config.mealTableId.trim())
            .putString(FeishuConstants.PREF_KEY_FEEDING_TABLE_ID, config.feedingTableId.trim())
            .putString(FeishuConstants.PREF_KEY_CHRONICLE_TABLE_ID, config.chronicleTableId.trim())
            .putLong(FeishuConstants.PREF_KEY_LAST_SYNC_AT, config.lastSyncAt)
            .apply()
    }
}
