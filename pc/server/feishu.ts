import {
  FeishuFields,
  getTenantAccessToken,
  listAllRecords,
  type FeishuRecord,
} from './feishuApiClient'

export interface FeedingItem {
  id: string
  timestamp: number
  dateKey: string
  amountMl: number | null
  note: string
}

function trimEnv(value: string | undefined): string {
  if (!value) return ''
  let v = value.trim()
  if (
    (v.startsWith('"') && v.endsWith('"')) ||
    (v.startsWith("'") && v.endsWith("'"))
  ) {
    v = v.slice(1, -1).trim()
  }
  return v
}

export function isFeishuConfigured(env: Record<string, string>): boolean {
  const c = readConfig(env)
  return Boolean(c.appId && c.appSecret && c.appToken && c.tableId)
}

function readConfig(env: Record<string, string>) {
  return {
    appId: trimEnv(env.FEISHU_APP_ID),
    appSecret: trimEnv(env.FEISHU_APP_SECRET),
    appToken: trimEnv(env.FEISHU_BITABLE_APP_TOKEN),
    tableId: trimEnv(env.FEISHU_FEEDING_TABLE_ID),
  }
}

function requireConfig(env: Record<string, string>) {
  const c = readConfig(env)
  if (!c.appId || !c.appSecret || !c.appToken || !c.tableId) {
    throw new Error(
      '请在 pc/.env 填写与手机「飞书同步」相同的 App ID、App Secret、Base App Token、育儿记表 ID',
    )
  }
  return c
}

function fieldString(fields: Record<string, unknown>, key: string): string | null {
  const value = fields[key]
  if (value == null) return null
  if (typeof value === 'string') return value.trim() || null
  if (typeof value === 'number') return String(Math.trunc(value))
  return String(value).trim() || null
}

function fieldLong(fields: Record<string, unknown>, key: string): number {
  const value = fields[key]
  if (value == null) return 0
  if (typeof value === 'number') return Math.trunc(value)
  if (typeof value === 'string') {
    const n = Number(value)
    return Number.isFinite(n) ? Math.trunc(n) : 0
  }
  return 0
}

function toDateKey(timestamp: number): string {
  const d = new Date(timestamp)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/** 与 FeishuSyncService.syncFeedings 拉取远端记录时的字段解析一致 */
function recordToFeedingItem(record: FeishuRecord): FeedingItem | null {
  const localId = fieldString(record.fields, FeishuFields.ID)
  if (!localId) return null
  const remoteUpdatedAt = fieldLong(record.fields, FeishuFields.UPDATED_AT)
  const timestamp =
    fieldLong(record.fields, FeishuFields.TIMESTAMP) ||
    remoteUpdatedAt ||
    Date.now()
  const dateKey =
    fieldString(record.fields, FeishuFields.DATE_KEY) ?? toDateKey(timestamp)
  const amountMl = takeIfPositive(fieldLong(record.fields, FeishuFields.AMOUNT_ML))
  const note = fieldString(record.fields, FeishuFields.NOTE) ?? ''
  return { id: localId, timestamp, dateKey, amountMl, note }
}

function takeIfPositive(n: number): number | null {
  return n > 0 ? n : null
}

export async function fetchFeedingRecords(env: Record<string, string>): Promise<FeedingItem[]> {
  const { appId, appSecret, appToken, tableId } = requireConfig(env)
  const token = await getTenantAccessToken(appId, appSecret)
  const records = await listAllRecords(token, appToken, tableId)
  const items = records
    .map(recordToFeedingItem)
    .filter((item): item is FeedingItem => item != null)
  items.sort((a, b) => b.timestamp - a.timestamp)
  return items
}

export type FeishuDiagnoseResult = {
  appIdHint: string
  bitableAppTokenPrefix: string
  feedingTableId: string
  tenantToken: 'ok' | 'failed'
  tenantTokenError?: string
  listRecords: { ok: true; sampleCount: number } | { ok: false; message: string }
  hints: string[]
}

function maskAppId(appId: string): string {
  if (appId.length <= 10) return appId
  return `${appId.slice(0, 6)}…${appId.slice(-4)}`
}

export async function diagnoseFeishu(env: Record<string, string>): Promise<FeishuDiagnoseResult> {
  const { appId, appSecret, appToken, tableId } = requireConfig(env)
  const result: FeishuDiagnoseResult = {
    appIdHint: maskAppId(appId),
    bitableAppTokenPrefix: appToken.slice(0, 6) + '…',
    feedingTableId: tableId,
    tenantToken: 'failed',
    listRecords: { ok: false, message: '未请求' },
    hints: [],
  }

  try {
    await getTenantAccessToken(appId, appSecret)
    result.tenantToken = 'ok'
  } catch (e) {
    result.tenantTokenError = e instanceof Error ? e.message : String(e)
    result.hints.push('与 App 相同：检查 App ID / App Secret')
    return result
  }

  try {
    const token = await getTenantAccessToken(appId, appSecret)
    const records = await listAllRecords(token, appToken, tableId)
    result.listRecords = { ok: true, sampleCount: records.length }
  } catch (e) {
    const message = e instanceof Error ? e.message : String(e)
    result.listRecords = { ok: false, message }
    if (message.includes('99991672')) {
      result.hints.push('读表权限与手机飞书同步相同：开放平台权限 + 发布版本 + 多维表格添加文档应用')
      result.hints.push('请在手机试「全量同步」：结果应与 PC 一致')
    }
  }
  return result
}
