/**
 * 与 android/.../feishu/FeishuApiClient.kt 保持同一套飞书 Open API 调用。
 */
import { FeishuFields } from './feishuFields'

const BASE_URL = 'https://open.feishu.cn/open-apis'
const BATCH_SIZE = 500

export type FeishuRecord = {
  recordId: string
  fields: Record<string, unknown>
}

type JsonObject = Record<string, unknown>

function parseRoot(raw: string | undefined, httpCode: number): JsonObject {
  if (!raw?.trim()) {
    throw new Error(`飞书 API 无响应 (HTTP ${httpCode})`)
  }
  const json = JSON.parse(raw) as JsonObject
  const code = (json.code as number | undefined) ?? -1
  if (code !== 0) {
    const msg = (json.msg as string | undefined) ?? '未知错误'
    throw new Error(`飞书 API 错误 (${code}): ${msg}`)
  }
  return json
}

function parseData(raw: string | undefined, httpCode: number): JsonObject {
  const root = parseRoot(raw, httpCode)
  const data = root.data as JsonObject | undefined
  if (!data) {
    throw new Error('飞书 API 响应缺少 data 字段')
  }
  return data
}

function parseFields(fieldsJson: JsonObject | undefined): Record<string, unknown> {
  if (!fieldsJson) return {}
  const map: Record<string, unknown> = {}
  for (const [key, value] of Object.entries(fieldsJson)) {
    if (value == null) {
      map[key] = null
      continue
    }
    if (typeof value === 'number' || typeof value === 'boolean' || typeof value === 'string') {
      map[key] = value
      continue
    }
    map[key] = JSON.stringify(value)
  }
  return map
}

function parseRecordItems(data: JsonObject): FeishuRecord[] {
  const records: FeishuRecord[] = []
  const items = data.items as Array<{ record_id?: string; fields?: JsonObject }> | undefined
  for (const item of items ?? []) {
    const recordId = item.record_id
    if (!recordId) continue
    records.push({
      recordId,
      fields: parseFields(item.fields),
    })
  }
  return records
}

function nextPageToken(data: JsonObject): string | undefined {
  if (data.has_more !== true) return undefined
  const token = data.page_token
  return typeof token === 'string' && token.length > 0 ? token : undefined
}

export function buildDateKeyFilter(dateKeys: Set<string> | string[]): JsonObject {
  const keys = [...dateKeys].filter((k) => k.trim().length > 0).sort()
  if (keys.length === 0) {
    throw new Error('dateKeys 不能为空')
  }
  const conditions = keys.map((key) => ({
    field_name: FeishuFields.DATE_KEY,
    operator: 'is',
    value: [key],
  }))
  return {
    conjunction: keys.length === 1 ? 'and' : 'or',
    conditions,
  }
}

export async function getTenantAccessToken(appId: string, appSecret: string): Promise<string> {
  const response = await fetch(`${BASE_URL}/auth/v3/tenant_access_token/internal`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json; charset=utf-8' },
    body: JSON.stringify({ app_id: appId, app_secret: appSecret }),
  })
  const root = parseRoot(await response.text(), response.status)
  const token = root.tenant_access_token as string | undefined
  if (!token) {
    throw new Error('获取 tenant_access_token 失败')
  }
  return token
}

async function listAllRecordsPaged(
  token: string,
  appToken: string,
  tableId: string,
): Promise<FeishuRecord[]> {
  const records: FeishuRecord[] = []
  let pageToken: string | undefined
  do {
    const params = new URLSearchParams({ page_size: '500' })
    if (pageToken) params.set('page_token', pageToken)
    const response = await fetch(
      `${BASE_URL}/bitable/v1/apps/${appToken}/tables/${tableId}/records?${params}`,
      { headers: { Authorization: `Bearer ${token}` } },
    )
    const data = parseData(await response.text(), response.status)
    records.push(...parseRecordItems(data))
    pageToken = nextPageToken(data)
  } while (pageToken)
  return records
}

async function searchRecordsByDateKeys(
  token: string,
  appToken: string,
  tableId: string,
  dateKeys: Set<string>,
): Promise<FeishuRecord[]> {
  const filter = buildDateKeyFilter(dateKeys)
  const records: FeishuRecord[] = []
  let pageToken: string | undefined
  do {
    const body: JsonObject = {
      page_size: 500,
      filter,
    }
    if (pageToken) body.page_token = pageToken
    const response = await fetch(
      `${BASE_URL}/bitable/v1/apps/${appToken}/tables/${tableId}/records/search`,
      {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json; charset=utf-8',
        },
        body: JSON.stringify(body),
      },
    )
    const data = parseData(await response.text(), response.status)
    records.push(...parseRecordItems(data))
    pageToken = nextPageToken(data)
  } while (pageToken)
  return records
}

export async function listRecords(
  token: string,
  appToken: string,
  tableId: string,
  dateKeys?: Set<string> | null,
): Promise<FeishuRecord[]> {
  const filtered = dateKeys
    ? new Set([...dateKeys].filter((k) => k.trim().length > 0))
    : null
  if (filtered && filtered.size > 0) {
    return searchRecordsByDateKeys(token, appToken, tableId, filtered)
  }
  return listAllRecordsPaged(token, appToken, tableId)
}

export async function listAllRecords(
  token: string,
  appToken: string,
  tableId: string,
): Promise<FeishuRecord[]> {
  return listRecords(token, appToken, tableId, null)
}

export async function batchCreateRecords(
  token: string,
  appToken: string,
  tableId: string,
  fieldsList: Record<string, unknown>[],
): Promise<string[]> {
  if (fieldsList.length === 0) return []
  const recordIds: string[] = []
  for (let i = 0; i < fieldsList.length; i += BATCH_SIZE) {
    const chunk = fieldsList.slice(i, i + BATCH_SIZE)
    const response = await fetch(
      `${BASE_URL}/bitable/v1/apps/${appToken}/tables/${tableId}/records/batch_create`,
      {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json; charset=utf-8',
        },
        body: JSON.stringify({
          records: chunk.map((fields) => ({ fields })),
        }),
      },
    )
    const data = parseData(await response.text(), response.status)
    const created = data.records as Array<{ record_id?: string; id?: string }> | undefined
    if (!created) {
      throw new Error('批量创建记录失败：响应缺少 records')
    }
    for (const item of created) {
      const recordId = item.record_id ?? item.id
      if (!recordId) {
        throw new Error('批量创建记录失败：无 record_id')
      }
      recordIds.push(recordId)
    }
  }
  return recordIds
}

export async function batchUpdateRecords(
  token: string,
  appToken: string,
  tableId: string,
  updates: Array<{ recordId: string; fields: Record<string, unknown> }>,
): Promise<void> {
  if (updates.length === 0) return
  for (let i = 0; i < updates.length; i += BATCH_SIZE) {
    const chunk = updates.slice(i, i + BATCH_SIZE)
    const response = await fetch(
      `${BASE_URL}/bitable/v1/apps/${appToken}/tables/${tableId}/records/batch_update`,
      {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json; charset=utf-8',
        },
        body: JSON.stringify({
          records: chunk.map((item) => ({
            record_id: item.recordId,
            fields: item.fields,
          })),
        }),
      },
    )
    parseData(await response.text(), response.status)
  }
}

export { FeishuFields }
