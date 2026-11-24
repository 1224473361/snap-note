<script setup lang="ts">
import { LineChart } from 'echarts/charts'
import {
  GridComponent,
  TooltipComponent,
} from 'echarts/components'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { computed } from 'vue'
import VChart from 'vue-echarts'
import type { DailyMilkPoint } from '../utils/groupFeedings'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent])

const props = defineProps<{
  points: DailyMilkPoint[]
}>()

const option = computed(() => ({
  color: ['#E84026'],
  grid: { left: 48, right: 24, top: 44, bottom: 36 },
  tooltip: {
    trigger: 'axis',
    formatter: (params: unknown) => {
      const list = Array.isArray(params) ? params : [params]
      const first = list[0] as { axisValue?: string; data?: number; name?: string }
      const ml = first?.data ?? 0
      const label = first?.axisValue ?? ''
      return `${label}<br/>奶量合计：<b>${ml} ml</b>`
    },
  },
  xAxis: {
    type: 'category',
    data: props.points.map((p) => p.label),
    axisLine: { lineStyle: { color: '#ddd' } },
    axisLabel: { color: '#999', fontSize: 11 },
  },
  yAxis: {
    type: 'value',
    name: 'ml',
    nameTextStyle: { color: '#999', fontSize: 11 },
    axisLabel: { color: '#999' },
    splitLine: { lineStyle: { color: '#eee' } },
  },
  series: [
    {
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      label: {
        show: true,
        position: 'top',
        distance: 6,
        color: '#E84026',
        fontSize: 11,
        fontWeight: 500,
        formatter: (params: { value?: number }) => {
          const v = params.value ?? 0
          return v > 0 ? String(v) : ''
        },
      },
      lineStyle: { width: 2 },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(232, 64, 38, 0.18)' },
            { offset: 1, color: 'rgba(232, 64, 38, 0)' },
          ],
        },
      },
      data: props.points.map((p) => p.totalMl),
    },
  ],
}))
</script>

<template>
  <section class="card chart-card">
    <h2 class="card-title">每日奶量（近 14 天）</h2>
    <VChart class="chart" :option="option" autoresize />
  </section>
</template>

<style scoped>
.chart-card {
  padding-bottom: 8px;
}

.chart {
  width: 100%;
  height: 300px;
}
</style>
