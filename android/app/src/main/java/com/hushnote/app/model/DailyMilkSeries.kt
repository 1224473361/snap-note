package com.hushnote.app.model

import com.hushnote.app.util.DateUtils
import java.util.Calendar

data class DailyMilkPoint(
    val dateKey: String,
    val label: String,
    val totalMl: Int
)

/** 最近 N 天（含今天）的每日奶量合计，无记录日为 0；逻辑与 PC `buildDailyMilkSeries` 一致 */
object DailyMilkSeriesBuilder {
    fun build(items: List<FeedingItem>, days: Int = 14): List<DailyMilkPoint> {
        val totals = mutableMapOf<String, Int>()
        for (item in items) {
            val ml = item.amountMl ?: continue
            if (ml <= 0) continue
            totals[item.dateKey] = (totals[item.dateKey] ?: 0) + ml
        }
        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val result = ArrayList<DailyMilkPoint>(days)
        for (i in days - 1 downTo 0) {
            val day = (end.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, -i)
            }
            val dateKey = DateUtils.toDateKey(day.timeInMillis)
            val label = compactChartDateLabel(dateKey)
            result.add(
                DailyMilkPoint(
                    dateKey = dateKey,
                    label = label,
                    totalMl = totals[dateKey] ?: 0
                )
            )
        }
        return result
    }
}

/** 图表横轴：1001、0930（无年份、无分隔符） */
fun compactChartDateLabel(dateKey: String): String {
    val parts = dateKey.split("-")
    if (parts.size != 3) return dateKey
    return parts[1] + parts[2]
}
