package com.hushnote.app.feishu

import com.hushnote.app.data.ChronicleRepository
import com.hushnote.app.data.FeedingRepository
import com.hushnote.app.data.FeishuSyncMetaRepository
import com.hushnote.app.data.MealRepository
import com.hushnote.app.data.MemoRepository
import com.hushnote.app.model.ChronicleItem
import com.hushnote.app.model.ChronicleType
import com.hushnote.app.model.FeedingItem
import com.hushnote.app.model.FeishuConfig
import com.hushnote.app.model.HomeMode
import com.hushnote.app.model.MealItem
import com.hushnote.app.model.MealSource
import com.hushnote.app.model.MemoItem
import com.hushnote.app.model.MemoStatus
import com.hushnote.app.util.DateUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

data class FeishuSyncResult(
    val pulled: Int,
    val pushed: Int
)

class FeishuSyncService(
    private val apiClient: FeishuApiClient,
    private val metaRepository: FeishuSyncMetaRepository,
    private val memoRepository: MemoRepository,
    private val mealRepository: MealRepository,
    private val feedingRepository: FeedingRepository,
    private val chronicleRepository: ChronicleRepository
) {
    private val fieldValidatedTableKeys = mutableSetOf<String>()

    private data class PendingPush(
        val localId: String,
        val fields: Map<String, Any?>,
        val remoteRecordId: String?,
        val updatedAt: Long
    )
    suspend fun sync(
        config: FeishuConfig,
        scope: FeishuSyncScope = FeishuSyncScope.Full,
        modules: Set<HomeMode>? = null,
        options: FeishuSyncOptions = FeishuSyncOptions()
    ): FeishuSyncResult = coroutineScope {
        val token = apiClient.getTenantAccessToken(config.appId, config.appSecret)
        if (scope is FeishuSyncScope.Full && options.pullRemote) {
            validateAllTableFields(token, config, modules)
        }
        val jobs = buildList {
            if (shouldSyncModule(config, modules, HomeMode.NOTE)) {
                add(async { syncMemos(token, config.bitableAppToken, config.memoTableId, scope, options) })
            }
            if (shouldSyncModule(config, modules, HomeMode.MEAL)) {
                add(async { syncMeals(token, config.bitableAppToken, config.mealTableId, scope, options) })
            }
            if (shouldSyncModule(config, modules, HomeMode.FEEDING)) {
                add(async { syncFeedings(token, config.bitableAppToken, config.feedingTableId, scope, options) })
            }
            if (shouldSyncModule(config, modules, HomeMode.CHRONICLE)) {
                add(async { syncChronicles(token, config.bitableAppToken, config.chronicleTableId, scope, options) })
            }
        }
        val totals = jobs.awaitAll()
        FeishuSyncResult(
            pulled = totals.sumOf { it.first },
            pushed = totals.sumOf { it.second }
        )
    }

    private fun shouldSyncModule(config: FeishuConfig, modules: Set<HomeMode>?, mode: HomeMode): Boolean {
        if (modules != null && mode !in modules) return false
        return tableIdFor(config, mode) != null
    }

    private fun validateAllTableFields(token: String, config: FeishuConfig, modules: Set<HomeMode>?) {
        if (shouldSyncModule(config, modules, HomeMode.NOTE)) {
            validateTableFields(
                token = token,
                appToken = config.bitableAppToken,
                tableId = config.memoTableId,
                tableLabel = "语音笔记表",
                required = listOf(
                    FeishuFields.ID,
                    FeishuFields.CONTENT,
                    FeishuFields.TIMESTAMP,
                    FeishuFields.DATE_KEY,
                    FeishuFields.UPDATED_AT
                )
            )
        }
        if (shouldSyncModule(config, modules, HomeMode.MEAL)) {
            validateTableFields(
                token = token,
                appToken = config.bitableAppToken,
                tableId = config.mealTableId,
                tableLabel = "饭记表",
                required = listOf(
                    FeishuFields.ID,
                    FeishuFields.DISHES,
                    FeishuFields.NOTE,
                    FeishuFields.AI_COMMENT,
                    FeishuFields.TIMESTAMP,
                    FeishuFields.DATE_KEY,
                    FeishuFields.UPDATED_AT
                )
            )
        }
        if (shouldSyncModule(config, modules, HomeMode.FEEDING)) {
            validateTableFields(
                token = token,
                appToken = config.bitableAppToken,
                tableId = config.feedingTableId,
                tableLabel = "育儿记表",
                required = listOf(
                    FeishuFields.ID,
                    FeishuFields.NOTE,
                    FeishuFields.AMOUNT_ML,
                    FeishuFields.TIMESTAMP,
                    FeishuFields.DATE_KEY,
                    FeishuFields.UPDATED_AT
                )
            )
        }
        if (shouldSyncModule(config, modules, HomeMode.CHRONICLE)) {
            validateTableFields(
                token = token,
                appToken = config.bitableAppToken,
                tableId = config.chronicleTableId,
                tableLabel = "大事记表",
                required = listOf(
                    FeishuFields.ID,
                    FeishuFields.TYPE,
                    FeishuFields.VALUE,
                    FeishuFields.TITLE,
                    FeishuFields.BODY,
                    FeishuFields.TIMESTAMP,
                    FeishuFields.DATE_KEY,
                    FeishuFields.UPDATED_AT
                )
            )
        }
    }

    private fun validateTableFields(
        token: String,
        appToken: String,
        tableId: String,
        tableLabel: String,
        required: List<String>
    ) {
        val cacheKey = "$appToken:$tableId"
        if (cacheKey in fieldValidatedTableKeys) return
        val existing = apiClient.listFieldNames(token, appToken, tableId)
        val missing = required.filter { it !in existing }
        if (missing.isNotEmpty()) {
            val existingHint = if (existing.isEmpty()) {
                "（当前未读取到任何字段）"
            } else {
                "（现有字段：${existing.sorted().joinToString("、")}）"
            }
            throw IllegalStateException(
                "$tableLabel 缺少字段：${missing.joinToString("、")}。请在飞书表头创建同名英文列 $existingHint"
            )
        }
        fieldValidatedTableKeys.add(cacheKey)
    }

    fun deleteRemoteRecords(config: FeishuConfig, mode: HomeMode, localIds: List<String>) {
        if (localIds.isEmpty()) return
        val tableId = tableIdFor(config, mode) ?: return
        val token = apiClient.getTenantAccessToken(config.appId, config.appSecret)
        localIds.forEach { localId ->
            val recordId = metaRepository.get(localId)?.recordId?.takeIf { it.isNotBlank() } ?: return@forEach
            apiClient.deleteRecord(token, config.bitableAppToken, tableId, recordId)
            metaRepository.remove(localId)
        }
    }

    private fun tableIdFor(config: FeishuConfig, mode: HomeMode): String? = when (mode) {
        HomeMode.NOTE -> config.memoTableId.takeIf { it.isNotBlank() }
        HomeMode.MEAL -> config.mealTableId.takeIf { it.isNotBlank() }
        HomeMode.FEEDING -> config.feedingTableId.takeIf { it.isNotBlank() }
        HomeMode.CHRONICLE -> config.chronicleTableId.takeIf { it.isNotBlank() }
    }

    private suspend fun syncMemos(
        token: String,
        appToken: String,
        tableId: String,
        scope: FeishuSyncScope,
        options: FeishuSyncOptions
    ): Pair<Int, Int> {
        var pulled = 0
        var pushed = 0
        val remoteRecords = if (options.pullRemote) {
            apiClient.listRecords(token, appToken, tableId, scope.dateKeysForApi())
        } else {
            emptyList()
        }
        val remoteById = remoteRecords.mapNotNull { record ->
            fieldString(record.fields, FeishuFields.ID)?.let { it to record }
        }.toMap()
        val localItems = memoRepository.getAllMemos()
            .filter { it.status == MemoStatus.DONE && scope.matches(it.dateKey) }
            .filter { item -> options.pushOnlyLocalIds?.contains(item.id) != false }

        for (record in remoteRecords) {
            val localId = fieldString(record.fields, FeishuFields.ID) ?: continue
            val recordDateKey = fieldString(record.fields, FeishuFields.DATE_KEY)
                ?: fieldLong(record.fields, FeishuFields.TIMESTAMP).let { DateUtils.toDateKey(it) }
            if (!scope.matches(recordDateKey)) continue
            val remoteUpdatedAt = fieldLong(record.fields, FeishuFields.UPDATED_AT)
            val localMeta = metaRepository.get(localId)
            val localItem = memoRepository.getMemoById(localId)
            if (localItem == null || remoteUpdatedAt > (localMeta?.updatedAt ?: localItem.timestamp)) {
                val content = fieldString(record.fields, FeishuFields.CONTENT) ?: continue
                val timestamp = fieldLong(record.fields, FeishuFields.TIMESTAMP).takeIf { it > 0 }
                    ?: remoteUpdatedAt
                val dateKey = fieldString(record.fields, FeishuFields.DATE_KEY)
                    ?: DateUtils.toDateKey(timestamp)
                if (localItem == null) {
                    memoRepository.upsert(
                        MemoItem(
                            id = localId,
                            content = content,
                            audioPath = memoRepository.buildAudioPath(localId).absolutePath,
                            timestamp = timestamp,
                            dateKey = dateKey,
                            status = MemoStatus.DONE
                        )
                    )
                } else {
                    memoRepository.upsert(
                        localItem.copy(
                            content = content,
                            timestamp = timestamp,
                            dateKey = dateKey,
                            status = MemoStatus.DONE
                        )
                    )
                }
                metaRepository.set(localId, record.recordId, remoteUpdatedAt)
                pulled++
            }
        }

        val pendingPushes = mutableListOf<PendingPush>()
        for (item in localItems) {
            val meta = metaRepository.get(item.id)
            val remote = remoteById[item.id]
            val localUpdatedAt = maxOf(item.timestamp, meta?.updatedAt ?: 0L)
            val (remoteRecordId, remoteUpdatedAt) = resolveRemoteSyncState(remote, meta)
            if (remoteRecordId == null || localUpdatedAt > remoteUpdatedAt) {
                val now = System.currentTimeMillis()
                val fields = mapOf(
                    FeishuFields.ID to item.id,
                    FeishuFields.CONTENT to item.content,
                    FeishuFields.TIMESTAMP to item.timestamp,
                    FeishuFields.DATE_KEY to item.dateKey,
                    FeishuFields.UPDATED_AT to now
                )
                pendingPushes.add(
                    PendingPush(
                        localId = item.id,
                        fields = fields,
                        remoteRecordId = remoteRecordId,
                        updatedAt = now
                    )
                )
            }
        }
        pushed += flushPushes(token, appToken, tableId, pendingPushes)
        return pulled to pushed
    }

    private suspend fun syncMeals(
        token: String,
        appToken: String,
        tableId: String,
        scope: FeishuSyncScope,
        options: FeishuSyncOptions
    ): Pair<Int, Int> {
        var pulled = 0
        var pushed = 0
        val remoteRecords = if (options.pullRemote) {
            apiClient.listRecords(token, appToken, tableId, scope.dateKeysForApi())
        } else {
            emptyList()
        }
        val remoteById = remoteRecords.mapNotNull { record ->
            fieldString(record.fields, FeishuFields.ID)?.let { it to record }
        }.toMap()
        val localItems = mealRepository.getAllMeals()
            .filter { scope.matches(it.dateKey) }
            .filter { item -> options.pushOnlyLocalIds?.contains(item.id) != false }

        for (record in remoteRecords) {
            val localId = fieldString(record.fields, FeishuFields.ID) ?: continue
            val recordDateKey = fieldString(record.fields, FeishuFields.DATE_KEY)
                ?: fieldLong(record.fields, FeishuFields.TIMESTAMP).let { DateUtils.toDateKey(it) }
            if (!scope.matches(recordDateKey)) continue
            val remoteUpdatedAt = fieldLong(record.fields, FeishuFields.UPDATED_AT)
            val localMeta = metaRepository.get(localId)
            val localItem = mealRepository.getMealById(localId)
            if (localItem == null || remoteUpdatedAt > (localMeta?.updatedAt ?: localItem.timestamp)) {
                val dishes = fieldString(record.fields, FeishuFields.DISHES)
                    ?.split("|")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()
                val timestamp = fieldLong(record.fields, FeishuFields.TIMESTAMP).takeIf { it > 0 }
                    ?: remoteUpdatedAt
                val dateKey = fieldString(record.fields, FeishuFields.DATE_KEY)
                    ?: DateUtils.toDateKey(timestamp)
                val merged = if (localItem == null) {
                    MealItem(
                        id = localId,
                        timestamp = timestamp,
                        dateKey = dateKey,
                        dishes = dishes,
                        note = fieldString(record.fields, FeishuFields.NOTE).orEmpty(),
                        aiComment = fieldString(record.fields, FeishuFields.AI_COMMENT).orEmpty(),
                        imagePath = mealRepository.buildImagePath(localId).absolutePath,
                        thumbnailPath = mealRepository.buildThumbPath(localId).absolutePath,
                        source = MealSource.GALLERY
                    )
                } else {
                    localItem.copy(
                        timestamp = timestamp,
                        dateKey = dateKey,
                        dishes = dishes.ifEmpty { localItem.dishes },
                        note = fieldString(record.fields, FeishuFields.NOTE) ?: localItem.note,
                        aiComment = fieldString(record.fields, FeishuFields.AI_COMMENT) ?: localItem.aiComment
                    )
                }
                mealRepository.upsert(merged)
                metaRepository.set(localId, record.recordId, remoteUpdatedAt)
                pulled++
            }
        }

        val pendingPushes = mutableListOf<PendingPush>()
        for (item in localItems) {
            val meta = metaRepository.get(item.id)
            val remote = remoteById[item.id]
            val localUpdatedAt = maxOf(item.timestamp, meta?.updatedAt ?: 0L)
            val (remoteRecordId, remoteUpdatedAt) = resolveRemoteSyncState(remote, meta)
            if (remoteRecordId == null || localUpdatedAt > remoteUpdatedAt) {
                val now = System.currentTimeMillis()
                val fields = mapOf(
                    FeishuFields.ID to item.id,
                    FeishuFields.DISHES to item.dishes.joinToString("|"),
                    FeishuFields.NOTE to item.note,
                    FeishuFields.AI_COMMENT to item.aiComment,
                    FeishuFields.TIMESTAMP to item.timestamp,
                    FeishuFields.DATE_KEY to item.dateKey,
                    FeishuFields.UPDATED_AT to now
                )
                pendingPushes.add(
                    PendingPush(
                        localId = item.id,
                        fields = fields,
                        remoteRecordId = remoteRecordId,
                        updatedAt = now
                    )
                )
            }
        }
        pushed += flushPushes(token, appToken, tableId, pendingPushes)
        return pulled to pushed
    }

    private suspend fun syncFeedings(
        token: String,
        appToken: String,
        tableId: String,
        scope: FeishuSyncScope,
        options: FeishuSyncOptions
    ): Pair<Int, Int> {
        var pulled = 0
        var pushed = 0
        val remoteRecords = if (options.pullRemote) {
            apiClient.listRecords(token, appToken, tableId, scope.dateKeysForApi())
        } else {
            emptyList()
        }
        val remoteById = remoteRecords.mapNotNull { record ->
            fieldString(record.fields, FeishuFields.ID)?.let { it to record }
        }.toMap()
        val localItems = feedingRepository.getAll()
            .filter { scope.matches(it.dateKey) }
            .filter { item -> options.pushOnlyLocalIds?.contains(item.id) != false }

        for (record in remoteRecords) {
            val localId = fieldString(record.fields, FeishuFields.ID) ?: continue
            val recordDateKey = fieldString(record.fields, FeishuFields.DATE_KEY)
                ?: fieldLong(record.fields, FeishuFields.TIMESTAMP).let { DateUtils.toDateKey(it) }
            if (!scope.matches(recordDateKey)) continue
            val remoteUpdatedAt = fieldLong(record.fields, FeishuFields.UPDATED_AT)
            val localMeta = metaRepository.get(localId)
            val localItem = feedingRepository.getById(localId)
            if (localItem == null || remoteUpdatedAt > (localMeta?.updatedAt ?: localItem.timestamp)) {
                val timestamp = fieldLong(record.fields, FeishuFields.TIMESTAMP).takeIf { it > 0 }
                    ?: remoteUpdatedAt
                val dateKey = fieldString(record.fields, FeishuFields.DATE_KEY)
                    ?: DateUtils.toDateKey(timestamp)
                val amountMl = fieldLong(record.fields, FeishuFields.AMOUNT_ML).takeIf { it > 0 }?.toInt()
                val merged = if (localItem == null) {
                    FeedingItem(
                        id = localId,
                        timestamp = timestamp,
                        dateKey = dateKey,
                        amountMl = amountMl,
                        note = fieldString(record.fields, FeishuFields.NOTE).orEmpty()
                    )
                } else {
                    localItem.copy(
                        timestamp = timestamp,
                        dateKey = dateKey,
                        amountMl = amountMl ?: localItem.amountMl,
                        note = fieldString(record.fields, FeishuFields.NOTE) ?: localItem.note
                    )
                }
                feedingRepository.upsert(merged)
                metaRepository.set(localId, record.recordId, remoteUpdatedAt)
                pulled++
            }
        }

        val pendingPushes = mutableListOf<PendingPush>()
        for (item in localItems) {
            val meta = metaRepository.get(item.id)
            val remote = remoteById[item.id]
            val localUpdatedAt = maxOf(item.timestamp, meta?.updatedAt ?: 0L)
            val (remoteRecordId, remoteUpdatedAt) = resolveRemoteSyncState(remote, meta)
            if (remoteRecordId == null || localUpdatedAt > remoteUpdatedAt) {
                val now = System.currentTimeMillis()
                val fields = buildMap<String, Any?> {
                    put(FeishuFields.ID, item.id)
                    put(FeishuFields.NOTE, item.note)
                    put(FeishuFields.TIMESTAMP, item.timestamp)
                    put(FeishuFields.DATE_KEY, item.dateKey)
                    put(FeishuFields.UPDATED_AT, now)
                    item.amountMl?.let { put(FeishuFields.AMOUNT_ML, it) }
                }
                pendingPushes.add(
                    PendingPush(
                        localId = item.id,
                        fields = fields,
                        remoteRecordId = remoteRecordId,
                        updatedAt = now
                    )
                )
            }
        }
        pushed += flushPushes(token, appToken, tableId, pendingPushes)
        return pulled to pushed
    }

    private suspend fun syncChronicles(
        token: String,
        appToken: String,
        tableId: String,
        scope: FeishuSyncScope,
        options: FeishuSyncOptions
    ): Pair<Int, Int> {
        var pulled = 0
        var pushed = 0
        val remoteRecords = if (options.pullRemote) {
            apiClient.listRecords(token, appToken, tableId, scope.dateKeysForApi())
        } else {
            emptyList()
        }
        val remoteById = remoteRecords.mapNotNull { record ->
            fieldString(record.fields, FeishuFields.ID)?.let { it to record }
        }.toMap()
        val localItems = chronicleRepository.getAll()
            .filter { scope.matches(it.dateKey) }
            .filter { item -> options.pushOnlyLocalIds?.contains(item.id) != false }

        for (record in remoteRecords) {
            val localId = fieldString(record.fields, FeishuFields.ID) ?: continue
            val recordDateKey = fieldString(record.fields, FeishuFields.DATE_KEY)
                ?: fieldLong(record.fields, FeishuFields.TIMESTAMP).let { DateUtils.toDateKey(it) }
            if (!scope.matches(recordDateKey)) continue
            val remoteUpdatedAt = fieldLong(record.fields, FeishuFields.UPDATED_AT)
            val localMeta = metaRepository.get(localId)
            val localItem = chronicleRepository.getById(localId)
            val localUpdatedAt = localItem?.updatedAt ?: localMeta?.updatedAt ?: 0L
            if (localItem == null || remoteUpdatedAt > localUpdatedAt) {
                val type = ChronicleFeishuMapper.typeFromFeishu(fieldString(record.fields, FeishuFields.TYPE))
                    ?: continue
                val timestamp = fieldLong(record.fields, FeishuFields.TIMESTAMP).takeIf { it > 0 }
                    ?: remoteUpdatedAt
                val dateKey = fieldString(record.fields, FeishuFields.DATE_KEY)
                    ?: DateUtils.toDateKey(timestamp)
                val value = fieldDouble(record.fields, FeishuFields.VALUE)
                val title = fieldString(record.fields, FeishuFields.TITLE).orEmpty()
                val body = fieldString(record.fields, FeishuFields.BODY).orEmpty()
                val merged = when (type) {
                    ChronicleType.WEIGHT, ChronicleType.HEIGHT -> {
                        val base = localItem ?: ChronicleItem(
                            id = localId,
                            type = type,
                            timestamp = timestamp,
                            dateKey = dateKey,
                            imagePaths = emptyList()
                        )
                        base.copy(
                            type = type,
                            timestamp = timestamp,
                            dateKey = dateKey,
                            value = value ?: base.value,
                            title = "",
                            body = "",
                            updatedAt = remoteUpdatedAt
                        )
                    }
                    ChronicleType.MILESTONE -> {
                        val base = localItem ?: ChronicleItem(
                            id = localId,
                            type = type,
                            timestamp = timestamp,
                            dateKey = dateKey,
                            imagePaths = emptyList()
                        )
                        base.copy(
                            type = type,
                            timestamp = timestamp,
                            dateKey = dateKey,
                            value = null,
                            title = title,
                            body = body,
                            imagePaths = base.imagePaths,
                            updatedAt = remoteUpdatedAt
                        )
                    }
                }
                chronicleRepository.upsert(merged)
                metaRepository.set(localId, record.recordId, remoteUpdatedAt)
                pulled++
            }
        }

        val pendingPushes = mutableListOf<PendingPush>()
        for (item in localItems) {
            val meta = metaRepository.get(item.id)
            val remote = remoteById[item.id]
            val localUpdatedAt = maxOf(item.updatedAt, meta?.updatedAt ?: 0L)
            val (remoteRecordId, remoteUpdatedAt) = resolveRemoteSyncState(remote, meta)
            if (remoteRecordId == null || localUpdatedAt > remoteUpdatedAt) {
                val now = System.currentTimeMillis()
                val fields = ChronicleFeishuMapper.toFields(item, now)
                pendingPushes.add(
                    PendingPush(
                        localId = item.id,
                        fields = fields,
                        remoteRecordId = remoteRecordId,
                        updatedAt = now
                    )
                )
            }
        }
        pushed += flushPushes(token, appToken, tableId, pendingPushes)
        return pulled to pushed
    }

    /**
     * 按日 search 可能拉不到远端行（如 date_key 为空或与筛选不一致），此时仍应使用本地 meta 里的 recordId 做更新而非重复创建。
     */
    private fun resolveRemoteSyncState(
        remote: FeishuRecord?,
        meta: FeishuSyncMetaRepository.Entry?
    ): Pair<String?, Long> {
        val recordId = remote?.recordId ?: meta?.recordId?.takeIf { it.isNotBlank() }
        val updatedAt = when {
            remote != null -> fieldLong(remote.fields, FeishuFields.UPDATED_AT)
            meta != null -> meta.updatedAt
            else -> 0L
        }
        return recordId to updatedAt
    }

    private fun flushPushes(
        token: String,
        appToken: String,
        tableId: String,
        pending: List<PendingPush>
    ): Int {
        if (pending.isEmpty()) return 0
        val creates = pending.filter { it.remoteRecordId.isNullOrBlank() }
        val updates = pending.filter { !it.remoteRecordId.isNullOrBlank() }
        creates.chunked(500).forEach { chunk ->
            val recordIds = apiClient.batchCreateRecords(token, appToken, tableId, chunk.map { it.fields })
            chunk.zip(recordIds).forEach { (push, recordId) ->
                metaRepository.set(push.localId, recordId, push.updatedAt)
            }
        }
        updates.chunked(500).forEach { chunk ->
            apiClient.batchUpdateRecords(
                token,
                appToken,
                tableId,
                chunk.map { it.remoteRecordId!! to it.fields }
            )
            chunk.forEach { push ->
                metaRepository.set(push.localId, push.remoteRecordId!!, push.updatedAt)
            }
        }
        return pending.size
    }

    private fun fieldString(fields: Map<String, Any?>, key: String): String? {
        return when (val value = fields[key]) {
            null -> null
            is String -> value.takeIf { it.isNotBlank() }
            is Number -> value.toLong().toString()
            else -> value.toString().takeIf { it.isNotBlank() }
        }
    }

    private fun fieldLong(fields: Map<String, Any?>, key: String): Long {
        return when (val value = fields[key]) {
            null -> 0L
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: 0L
            else -> 0L
        }
    }

    private fun fieldDouble(fields: Map<String, Any?>, key: String): Double? {
        return when (val value = fields[key]) {
            null -> null
            is Number -> value.toDouble().takeIf { it > 0 }
            is String -> value.toDoubleOrNull()?.takeIf { it > 0 }
            else -> null
        }
    }
}
