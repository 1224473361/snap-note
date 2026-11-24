import type { FeedingsResponse } from '../types'

export async function fetchFeedings(): Promise<FeedingsResponse> {
  const response = await fetch('/api/feedings')
  const body = (await response.json()) as FeedingsResponse & { error?: string }
  if (!response.ok) {
    throw new Error(body.error ?? `请求失败 (${response.status})`)
  }
  return body
}

export async function checkHealth(): Promise<{ configured: boolean }> {
  const response = await fetch('/api/health')
  return (await response.json()) as { configured: boolean }
}
