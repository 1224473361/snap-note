package com.hushnote.app.feishu

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.hushnote.app.util.DateUtils
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

data class FeishuRecord(
    val recordId: String,
    val fields: Map<String, Any?>
)

class FeishuApiClient {
    private data class TokenCache(
        val appId: String,
        val token: String,
        val expiresAtMs: Long
    )

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private var tokenCache: TokenCache? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /** 飞书 tenant_access_token 有效期由服务端决定（通常约 2 小时），客户端只能缓存并提前刷新。 */
    fun getTenantAccessToken(appId: String, appSecret: String): String = synchronized(this) {
        val now = System.currentTimeMillis()
        val cached = tokenCache
        if (cached != null && cached.appId == appId && now < cached.expiresAtMs - TOKEN_REFRESH_LEEWAY_MS) {
            return cached.token
        }
        val body = gson.toJson(mapOf("app_id" to appId, "app_secret" to appSecret))
            .toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$BASE_URL/auth/v3/tenant_access_token/internal")
            .post(body)
            .build()
        val response = client.newCall(request).execute()
        val root = parseRoot(response.body?.string(), response.code)
        val token = root.get("tenant_access_token")?.asString
            ?: throw IllegalStateException("获取 tenant_access_token 失败")
        val expireSec = root.get("expire")?.asInt ?: 7200
        tokenCache = TokenCache(appId, token, now + expireSec * 1000L)
        token
    }

    /** 进入首页等场景预取 token，保存/同步时尽量命中内存缓存。 */
    fun warmUpTenantToken(appId: String, appSecret: String) {
        if (appId.isBlank() || appSecret.isBlank()) return
        getTenantAccessToken(appId, appSecret)
    }

    fun listAllRecords(token: String, appToken: String, tableId: String): List<FeishuRecord> {
        return listRecords(token, appToken, tableId, dateKeys = null)
    }

    fun listRecords(
        token: String,
        appToken: String,
        tableId: String,
        dateKeys: Set<String>?
    ): List<FeishuRecord> {
        val dateKeyFilter = dateKeys?.filter { it.isNotBlank() }?.toSet()?.takeIf { it.isNotEmpty() }
        if (dateKeyFilter != null) {
            return listRecordsForDateKeys(token, appToken, tableId, dateKeyFilter)
        }
        return listAllRecordsPaged(token, appToken, tableId)
    }

    /** 按日同步：先按 date_key 搜索；无结果时再按 timestamp 当日范围搜索（不再全表翻页）。 */
    private fun listRecordsForDateKeys(
        token: String,
        appToken: String,
        tableId: String,
        dateKeys: Set<String>
    ): List<FeishuRecord> {
        val byDateKey = searchRecordsByDateKeys(token, appToken, tableId, dateKeys)
        if (byDateKey.isNotEmpty()) return byDateKey
        return searchRecordsByTimestampDateKeys(token, appToken, tableId, dateKeys)
    }

    private fun listAllRecordsPaged(token: String, appToken: String, tableId: String): List<FeishuRecord> {
        val records = mutableListOf<FeishuRecord>()
        var pageToken: String? = null
        do {
            val urlBuilder = "$BASE_URL/bitable/v1/apps/$appToken/tables/$tableId/records"
                .toHttpUrl()
                .newBuilder()
                .addQueryParameter("page_size", "500")
            if (!pageToken.isNullOrBlank()) {
                urlBuilder.addQueryParameter("page_token", pageToken)
            }
            val request = Request.Builder()
                .url(urlBuilder.build())
                .header("Authorization", "Bearer $token")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                val data = parseData(response.body?.string(), response.code)
                records.addAll(parseRecordItems(data))
                pageToken = nextPageToken(data)
            }
        } while (!pageToken.isNullOrBlank())
        return records
    }

    private fun searchRecordsByDateKeys(
        token: String,
        appToken: String,
        tableId: String,
        dateKeys: Set<String>
    ): List<FeishuRecord> {
        return searchRecords(token, appToken, tableId, buildDateKeyFilter(dateKeys))
    }

    private fun searchRecordsByTimestampDateKeys(
        token: String,
        appToken: String,
        tableId: String,
        dateKeys: Set<String>
    ): List<FeishuRecord> {
        val merged = LinkedHashMap<String, FeishuRecord>()
        dateKeys.filter { it.isNotBlank() }.sorted().forEach { dateKey ->
            val start = DateUtils.dateKeyToMillis(dateKey)
            val end = start + DAY_MS - 1
            searchRecordsByTimestampRange(token, appToken, tableId, start, end)
                .forEach { merged[it.recordId] = it }
        }
        return merged.values.toList()
    }

    private fun searchRecordsByTimestampRange(
        token: String,
        appToken: String,
        tableId: String,
        startInclusive: Long,
        endInclusive: Long
    ): List<FeishuRecord> {
        val filter = mapOf(
            "conjunction" to "and",
            "conditions" to listOf(
                mapOf(
                    "field_name" to FeishuFields.TIMESTAMP,
                    "operator" to "isGreaterEqual",
                    "value" to listOf(startInclusive)
                ),
                mapOf(
                    "field_name" to FeishuFields.TIMESTAMP,
                    "operator" to "isLessEqual",
                    "value" to listOf(endInclusive)
                )
            )
        )
        return searchRecords(token, appToken, tableId, filter)
    }

    private fun searchRecords(
        token: String,
        appToken: String,
        tableId: String,
        filter: Map<String, Any>
    ): List<FeishuRecord> {
        val records = mutableListOf<FeishuRecord>()
        var pageToken: String? = null
        do {
            val bodyMap = mutableMapOf<String, Any>(
                "page_size" to 500,
                "filter" to filter
            )
            if (!pageToken.isNullOrBlank()) {
                bodyMap["page_token"] = pageToken
            }
            val body = gson.toJson(bodyMap).toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url("$BASE_URL/bitable/v1/apps/$appToken/tables/$tableId/records/search")
                .header("Authorization", "Bearer $token")
                .post(body)
                .build()
            client.newCall(request).execute().use { response ->
                val data = parseData(response.body?.string(), response.code)
                records.addAll(parseRecordItems(data))
                pageToken = nextPageToken(data)
            }
        } while (!pageToken.isNullOrBlank())
        return records
    }

    fun buildDateKeyFilter(dateKeys: Set<String>): Map<String, Any> {
        val keys = dateKeys.filter { it.isNotBlank() }.sorted()
        require(keys.isNotEmpty()) { "dateKeys 不能为空" }
        val conditions = keys.map { key ->
            mapOf(
                "field_name" to FeishuFields.DATE_KEY,
                "operator" to "is",
                "value" to listOf(key)
            )
        }
        return mapOf(
            "conjunction" to if (keys.size == 1) "and" else "or",
            "conditions" to conditions
        )
    }

    fun batchCreateRecords(
        token: String,
        appToken: String,
        tableId: String,
        fieldsList: List<Map<String, Any?>>
    ): List<String> {
        if (fieldsList.isEmpty()) return emptyList()
        val recordIds = mutableListOf<String>()
        fieldsList.chunked(BATCH_SIZE).forEach { chunk ->
            val body = gson.toJson(
                mapOf("records" to chunk.map { mapOf("fields" to it) })
            ).toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url("$BASE_URL/bitable/v1/apps/$appToken/tables/$tableId/records/batch_create")
                .header("Authorization", "Bearer $token")
                .post(body)
                .build()
            val response = client.newCall(request).execute()
            val data = parseData(response.body?.string(), response.code)
            val created = data.getAsJsonArray("records")
                ?: throw IllegalStateException("批量创建记录失败：响应缺少 records")
            created.forEach { element ->
                val item = element.asJsonObject
                val recordId = item.get("record_id")?.asString
                    ?: item.get("id")?.asString
                    ?: throw IllegalStateException("批量创建记录失败：无 record_id")
                recordIds.add(recordId)
            }
        }
        return recordIds
    }

    fun batchUpdateRecords(
        token: String,
        appToken: String,
        tableId: String,
        updates: List<Pair<String, Map<String, Any?>>>
    ) {
        if (updates.isEmpty()) return
        updates.chunked(BATCH_SIZE).forEach { chunk ->
            val body = gson.toJson(
                mapOf(
                    "records" to chunk.map { (recordId, fields) ->
                        mapOf("record_id" to recordId, "fields" to fields)
                    }
                )
            ).toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url("$BASE_URL/bitable/v1/apps/$appToken/tables/$tableId/records/batch_update")
                .header("Authorization", "Bearer $token")
                .post(body)
                .build()
            val response = client.newCall(request).execute()
            parseData(response.body?.string(), response.code)
        }
    }

    fun createRecord(
        token: String,
        appToken: String,
        tableId: String,
        fields: Map<String, Any?>
    ): String {
        val body = gson.toJson(mapOf("fields" to fields)).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$BASE_URL/bitable/v1/apps/$appToken/tables/$tableId/records")
            .header("Authorization", "Bearer $token")
            .post(body)
            .build()
        val response = client.newCall(request).execute()
        val data = parseData(response.body?.string(), response.code)
        val record = data.getAsJsonObject("record")
            ?: throw IllegalStateException("创建记录失败：响应缺少 record")
        return record.get("record_id")?.asString
            ?: record.get("id")?.asString
            ?: throw IllegalStateException("创建记录失败：无 record_id")
    }

    fun updateRecord(
        token: String,
        appToken: String,
        tableId: String,
        recordId: String,
        fields: Map<String, Any?>
    ) {
        val body = gson.toJson(mapOf("fields" to fields)).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$BASE_URL/bitable/v1/apps/$appToken/tables/$tableId/records/$recordId")
            .header("Authorization", "Bearer $token")
            .put(body)
            .build()
        val response = client.newCall(request).execute()
        parseData(response.body?.string(), response.code)
    }

    fun listFieldNames(token: String, appToken: String, tableId: String): Set<String> {
        val names = mutableSetOf<String>()
        var pageToken: String? = null
        do {
            val url = buildString {
                append("$BASE_URL/bitable/v1/apps/$appToken/tables/$tableId/fields?page_size=100")
                if (!pageToken.isNullOrBlank()) append("&page_token=$pageToken")
            }
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $token")
                .get()
                .build()
            val response = client.newCall(request).execute()
            val data = parseData(response.body?.string(), response.code)
            data.getAsJsonArray("items")?.forEach { element ->
                element.asJsonObject.get("field_name")?.asString?.let { names.add(it) }
            }
            pageToken = data.get("page_token")?.takeIf { !it.isJsonNull }?.asString
            val hasMore = data.get("has_more")?.asBoolean ?: false
            if (!hasMore) pageToken = null
        } while (!pageToken.isNullOrBlank())
        return names
    }

    fun deleteRecord(
        token: String,
        appToken: String,
        tableId: String,
        recordId: String
    ) {
        val request = Request.Builder()
            .url("$BASE_URL/bitable/v1/apps/$appToken/tables/$tableId/records/$recordId")
            .header("Authorization", "Bearer $token")
            .delete()
            .build()
        val response = client.newCall(request).execute()
        parseData(response.body?.string(), response.code)
    }

    private fun parseRoot(raw: String?, httpCode: Int): JsonObject {
        if (raw.isNullOrBlank()) {
            throw IllegalStateException("飞书 API 无响应 (HTTP $httpCode)")
        }
        val json = gson.fromJson(raw, JsonObject::class.java)
        val code = json.get("code")?.asInt ?: -1
        if (code != 0) {
            val msg = json.get("msg")?.asString ?: "未知错误"
            throw IllegalStateException("飞书 API 错误 ($code): $msg")
        }
        return json
    }

    private fun parseData(raw: String?, httpCode: Int): JsonObject {
        val root = parseRoot(raw, httpCode)
        return root.getAsJsonObject("data")
            ?: throw IllegalStateException("飞书 API 响应缺少 data 字段")
    }

    private fun parseFields(fieldsJson: JsonObject?): Map<String, Any?> {
        if (fieldsJson == null) return emptyMap()
        val map = mutableMapOf<String, Any?>()
        fieldsJson.entrySet().forEach { (key, value) ->
            map[key] = when {
                value.isJsonNull -> null
                value.isJsonPrimitive -> {
                    val primitive = value.asJsonPrimitive
                    when {
                        primitive.isNumber -> primitive.asDouble.let { num ->
                            if (num % 1.0 == 0.0) num.toLong() else num
                        }
                        primitive.isBoolean -> primitive.asBoolean
                        else -> primitive.asString
                    }
                }
                value.isJsonArray -> flattenJsonArray(value.asJsonArray)
                else -> value.toString()
            }
        }
        return map
    }

    private fun flattenJsonArray(array: JsonArray): String {
        val parts = mutableListOf<String>()
        array.forEach { element ->
            when {
                element.isJsonObject -> {
                    element.asJsonObject.get("text")?.asString?.let { parts.add(it) }
                }
                element.isJsonPrimitive -> parts.add(element.asString)
            }
        }
        return parts.joinToString("")
    }

    private fun parseRecordItems(data: JsonObject): List<FeishuRecord> {
        val records = mutableListOf<FeishuRecord>()
        data.getAsJsonArray("items")?.forEach { element ->
            val item = element.asJsonObject
            val recordId = item.get("record_id")?.asString ?: return@forEach
            val fields = parseFields(item.getAsJsonObject("fields"))
            records.add(FeishuRecord(recordId, fields))
        }
        return records
    }

    private fun nextPageToken(data: JsonObject): String? {
        val hasMore = data.get("has_more")?.asBoolean ?: false
        if (!hasMore) return null
        return data.get("page_token")?.takeIf { !it.isJsonNull }?.asString
    }

    companion object {
        private const val BASE_URL = "https://open.feishu.cn/open-apis"
        private const val BATCH_SIZE = 500
        /** 距过期不足此时长则主动换新 token（飞书 expire 不可改，只能提前刷新） */
        private const val TOKEN_REFRESH_LEEWAY_MS = 600_000L
        private const val DAY_MS = 24 * 60 * 60 * 1000L
    }
}
