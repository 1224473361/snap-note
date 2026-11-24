export interface FeedingItem {
  id: string
  timestamp: number
  dateKey: string
  amountMl: number | null
  note: string
}

export interface FeedingGroup {
  dateKey: string
  items: FeedingItem[]
  totalAmountMl: number
}

export interface FeedingsResponse {
  items: FeedingItem[]
  fetchedAt: number
}
