export function formatTime(timestamp: number): string {
  const d = new Date(timestamp)
  const h = String(d.getHours()).padStart(2, '0')
  const m = String(d.getMinutes()).padStart(2, '0')
  return `${h}:${m}`
}

export function formatDateKey(dateKey: string): string {
  const [y, mo, d] = dateKey.split('-')
  if (!y || !mo || !d) return dateKey
  const today = toDateKey(Date.now())
  const yesterday = toDateKey(Date.now() - 86_400_000)
  if (dateKey === today) return '今天'
  if (dateKey === yesterday) return '昨天'
  return `${y}年${mo}月${d}日`
}

export function toDateKey(ms: number): string {
  const d = new Date(ms)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

export function formatFetchedAt(ms: number): string {
  const d = new Date(ms)
  return `${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}:${d.getSeconds().toString().padStart(2, '0')}`
}
