package com.hushnote.app.feishu

sealed class FeishuSyncScope {
    data object Full : FeishuSyncScope()

    data class DateKeys(val keys: Set<String>) : FeishuSyncScope() {
        init {
            require(keys.isNotEmpty()) { "DateKeys 不能为空" }
        }
    }

    fun matches(dateKey: String): Boolean = when (this) {
        Full -> true
        is DateKeys -> dateKey in keys
    }

    fun dateKeysForApi(): Set<String>? = when (this) {
        Full -> null
        is DateKeys -> keys
    }
}
