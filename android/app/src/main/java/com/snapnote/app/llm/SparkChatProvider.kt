package com.snapnote.app.llm

interface SparkChatProvider {
    suspend fun complete(
        system: String,
        user: String,
        onDelta: (accumulated: String) -> Unit
    ): String
}
