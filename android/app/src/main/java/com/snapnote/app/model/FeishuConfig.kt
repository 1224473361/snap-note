package com.snapnote.app.model

data class FeishuConfig(
    val enabled: Boolean = false,
    val appId: String = "",
    val appSecret: String = "",
    val bitableAppToken: String = "",
    val memoTableId: String = "",
    val mealTableId: String = "",
    val feedingTableId: String = "",
    val chronicleTableId: String = "",
    val lastSyncAt: Long = 0L
) {
    val isConfigured: Boolean
        get() = appId.isNotBlank() &&
            appSecret.isNotBlank() &&
            bitableAppToken.isNotBlank()

    val configuredTableCount: Int
        get() = listOf(memoTableId, mealTableId, feedingTableId, chronicleTableId).count { it.isNotBlank() }
}

object FeishuConstants {
    const val PREF_KEY_ENABLED = "feishu_sync_enabled"
    const val PREF_KEY_APP_ID = "feishu_app_id"
    const val PREF_KEY_APP_SECRET = "feishu_app_secret"
    const val PREF_KEY_BITABLE_APP_TOKEN = "feishu_bitable_app_token"
    const val PREF_KEY_MEMO_TABLE_ID = "feishu_memo_table_id"
    const val PREF_KEY_MEAL_TABLE_ID = "feishu_meal_table_id"
    const val PREF_KEY_FEEDING_TABLE_ID = "feishu_feeding_table_id"
    const val PREF_KEY_CHRONICLE_TABLE_ID = "feishu_chronicle_table_id"
    const val PREF_KEY_LAST_SYNC_AT = "feishu_last_sync_at"
}
