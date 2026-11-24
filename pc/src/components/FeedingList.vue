<script setup lang="ts">
import { formatDateKey, formatTime } from '../utils/date'
import type { FeedingGroup } from '../types'

defineProps<{
  groups: FeedingGroup[]
}>()
</script>

<template>
  <section class="card list-card">
    <h2 class="card-title">记录列表</h2>
    <p v-if="groups.length === 0" class="empty">暂无育儿记数据</p>
    <div v-for="group in groups" :key="group.dateKey" class="day-block">
      <header class="day-header">
        <span class="day-label">{{ formatDateKey(group.dateKey) }}</span>
        <span class="day-meta">{{ group.dateKey }}</span>
        <span v-if="group.totalAmountMl > 0" class="day-total">
          合计 {{ group.totalAmountMl }} ml
        </span>
      </header>
      <ul class="item-list">
        <li v-for="item in group.items" :key="item.id" class="item-row">
          <span class="time">{{ formatTime(item.timestamp) }}</span>
          <span v-if="item.amountMl != null" class="amount">{{ item.amountMl }}ml</span>
          <span v-else class="amount placeholder" />
          <span class="note">{{ item.note || '（无描述）' }}</span>
        </li>
      </ul>
    </div>
  </section>
</template>

<style scoped>
.list-card {
  margin-top: 16px;
}

.empty {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 8px 0 0;
}

.day-block {
  margin-top: 20px;
}

.day-block:first-of-type {
  margin-top: 12px;
}

.day-header {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 8px 12px;
  margin-bottom: 8px;
  padding-bottom: 6px;
  border-bottom: 1px solid var(--divider);
}

.day-label {
  font-weight: 600;
  font-size: 15px;
  color: var(--text-primary);
}

.day-meta {
  font-size: 12px;
  color: var(--text-secondary);
}

.day-total {
  margin-left: auto;
  font-size: 13px;
  color: var(--accent);
  font-weight: 500;
}

.item-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.item-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 10px 12px;
  background: var(--card-bg);
  border-radius: 10px;
  margin-bottom: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.time {
  flex: 0 0 44px;
  font-size: 13px;
  color: var(--text-secondary);
}

.amount {
  flex: 0 0 52px;
  font-size: 14px;
  font-weight: 500;
  color: var(--accent);
}

.amount.placeholder {
  visibility: hidden;
}

.note {
  flex: 1;
  font-size: 15px;
  color: var(--text-primary);
  line-height: 1.45;
  word-break: break-word;
}
</style>
