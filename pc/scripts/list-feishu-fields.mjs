import { readFileSync } from 'fs'

const BASE_URL = 'https://open.feishu.cn/open-apis'

async function getTenantAccessToken(appId, appSecret) {
  const response = await fetch(`${BASE_URL}/auth/v3/tenant_access_token/internal`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json; charset=utf-8' },
    body: JSON.stringify({ app_id: appId, app_secret: appSecret }),
  })
  const root = JSON.parse(await response.text())
  if (root.code !== 0) throw new Error(root.msg || 'token failed')
  const token = root.tenant_access_token
  if (!token) throw new Error('no tenant_access_token')
  return token
}

function loadEnv() {
  const raw = readFileSync(new URL('../.env', import.meta.url), 'utf8')
  const env = {}
  for (const line of raw.split(/\r?\n/)) {
    const t = line.trim()
    if (!t || t.startsWith('#')) continue
    const i = t.indexOf('=')
    if (i < 0) continue
    let v = t.slice(i + 1).trim()
    if (
      (v.startsWith('"') && v.endsWith('"')) ||
      (v.startsWith("'") && v.endsWith("'"))
    ) {
      v = v.slice(1, -1)
    }
    env[t.slice(0, i).trim()] = v
  }
  return env
}

const TABLE = process.argv[2] || 'tblQxPrt42Difrmb'
const env = loadEnv()
const appId = env.FEISHU_APP_ID
const appSecret = env.FEISHU_APP_SECRET
const appToken = env.FEISHU_BITABLE_APP_TOKEN
if (!appId || !appSecret || !appToken) {
  console.error('缺少 FEISHU_APP_ID / SECRET / BITABLE_APP_TOKEN')
  process.exit(1)
}

const token = await getTenantAccessToken(appId, appSecret)
const BASE = BASE_URL
const items = []
let pageToken = ''
do {
  const params = new URLSearchParams({ page_size: '100' })
  if (pageToken) params.set('page_token', pageToken)
  const res = await fetch(
    `${BASE}/bitable/v1/apps/${appToken}/tables/${TABLE}/fields?${params}`,
    { headers: { Authorization: `Bearer ${token}` } },
  )
  const json = JSON.parse(await res.text())
  if (json.code !== 0) {
    console.error('API error:', json.code, json.msg)
    process.exit(1)
  }
  const data = json.data || {}
  for (const it of data.items || []) {
    items.push({ name: it.field_name, type: it.type, ui_type: it.ui_type })
  }
  pageToken = data.has_more ? (data.page_token || '') : ''
} while (pageToken)

const expected = [
  { name: 'id' },
  { name: 'type' },
  { name: 'value' },
  { name: 'title' },
  { name: 'body' },
  { name: 'timestamp' },
  { name: 'date_key' },
  { name: 'updated_at' },
  { name: 'image_refs', optional: true },
]
console.log('表 ID:', TABLE)
console.log('字段数:', items.length)
console.log('--- 实际字段 ---')
for (const it of [...items].sort((a, b) => a.name.localeCompare(b.name))) {
  console.log(`${it.name} | type=${it.type} ui=${it.ui_type ?? ''}`)
}
console.log('--- 与文档 §4.4 对照 ---')
for (const e of expected) {
  const found = items.find((x) => x.name === e.name)
  if (!found) {
    console.log(`${e.optional ? '可选缺失' : '缺失'}: ${e.name}`)
    continue
  }
  console.log(`OK: ${e.name} (飞书 type=${found.type}, ui=${found.ui_type ?? ''})`)
}
const extra = items.filter((x) => !expected.some((e) => e.name === x.name))
if (extra.length) {
  console.log('--- 多余字段 ---')
  for (const it of extra) console.log(it.name)
}
