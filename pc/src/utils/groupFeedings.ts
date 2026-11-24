import type { FeedingGroup, FeedingItem } from '../types'

export function groupFeedings(items: FeedingItem[]): FeedingGroup[] {
  const map = new Map<string, FeedingItem[]>()
  for (const item of items) {
    const list = map.get(item.dateKey) ?? []
    list.push(item)
    map.set(item.dateKey, list)
  }
  return [...map.entries()]
    .map(([dateKey, groupItems]) => {
      const sorted = [...groupItems].sort((a, b) => b.timestamp - a.timestamp)
      const totalAmountMl = sorted.reduce((sum, i) => sum + (i.amountMl ?? 0), 0)
      return { dateKey, items: sorted, totalAmountMl }
    })
    .sort((a, b) => b.dateKey.localeCompare(a.dateKey))
}

export interface DailyMilkPoint {
  dateKey: string
  label: string
  totalMl: number
}

/** 最近 N 天（含今天）的每日奶量合计，无记录日为 0 */
export function buildDailyMilkSeries(items: FeedingItem[], days = 14): DailyMilkPoint[] {
  const totals = new Map<string, number>()
  for (const item of items) {
    if (item.amountMl == null || item.amountMl <= 0) continue
    totals.set(item.dateKey, (totals.get(item.dateKey) ?? 0) + item.amountMl)
  }
  const result: DailyMilkPoint[] = []
  const end = new Date()
  end.setHours(0, 0, 0, 0)
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date(end)
    d.setDate(end.getDate() - i)
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const dateKey = `${y}-${m}-${day}`
    result.push({
      dateKey,
      label: `${m}-${day}`,
      totalMl: totals.get(dateKey) ?? 0,
    })
  }
  return result
}
