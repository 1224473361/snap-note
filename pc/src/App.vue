<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { checkHealth, fetchFeedings } from './api/feedings'
import DailyMilkChart from './components/DailyMilkChart.vue'
import FeedingList from './components/FeedingList.vue'
import { formatFetchedAt } from './utils/date'
import { buildDailyMilkSeries, groupFeedings } from './utils/groupFeedings'
import type { FeedingItem } from './types'

const items = ref<FeedingItem[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const fetchedAt = ref<number | null>(null)
const configured = ref(false)

const groups = computed(() => groupFeedings(items.value))
const chartPoints = computed(() => buildDailyMilkSeries(items.value, 14))

const lastSyncText = computed(() =>
  fetchedAt.value != null ? `上次刷新 ${formatFetchedAt(fetchedAt.value)}` : '尚未加载',
)

async function refresh() {
  loading.value = true
  error.value = null
  try {
    const data = await fetchFeedings()
    items.value = data.items
    fetchedAt.value = data.fetchedAt
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const health = await checkHealth()
    configured.value = health.configured
  } catch {
    configured.value = false
  }
  if (configured.value) {
    await refresh()
  }
})
</script>

<template>
  <div class="page">
    <header class="top-bar">
      <div class="brand">
        <h1>育儿记</h1>
        <p class="subtitle">只读查看 · 飞书多维表格（与手机「飞书同步」同一套接口）</p>
      </div>
      <div class="actions">
        <span class="sync-hint">{{ lastSyncText }}</span>
        <button
          type="button"
          class="btn-refresh"
          :disabled="loading || !configured"
          @click="refresh"
        >
          {{ loading ? '刷新中…' : '刷新' }}
        </button>
      </div>
    </header>

    <main class="content">
      <div v-if="!configured" class="banner warn">
        请复制 <code>pc/.env.example</code> 为 <code>pc/.env</code>，填写与手机
        <strong>我的 → 飞书同步</strong> 相同的 App ID、App Secret、Base App Token、育儿记表 ID，然后重启
        <code>npm run dev</code>。
      </div>
      <div v-if="error" class="banner error">{{ error }}</div>

      <DailyMilkChart :points="chartPoints" />
      <FeedingList :groups="groups" />
    </main>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
}

.top-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 0 8px;
}

.brand h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
}

.subtitle {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--text-secondary);
}

.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.sync-hint {
  font-size: 12px;
  color: var(--text-secondary);
}

.btn-refresh {
  border: none;
  border-radius: 8px;
  padding: 8px 18px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  color: #fff;
  background: var(--accent);
  transition: opacity 0.15s;
}

.btn-refresh:hover:not(:disabled) {
  opacity: 0.92;
}

.btn-refresh:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.content {
  padding-bottom: 48px;
}

.banner {
  margin: 12px 0;
  padding: 12px 14px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.5;
}

.banner.warn {
  background: #fff8e6;
  color: #8a6d00;
  border: 1px solid #ffe08a;
}

.banner.error {
  background: #ffebee;
  color: #c62828;
  border: 1px solid #ffcdd2;
}

.banner code {
  font-size: 12px;
  background: rgba(0, 0, 0, 0.06);
  padding: 1px 4px;
  border-radius: 4px;
}
</style>
