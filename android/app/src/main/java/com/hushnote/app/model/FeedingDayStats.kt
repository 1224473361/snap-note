package com.hushnote.app.model

data class FeedingDayStats(
    val totalMl: Int,
    val recordCount: Int,
    /** 有奶量记录时的平均每次奶量（ml） */
    val averageMl: Int?,
    /** 当天最后一次有奶量的记录时间 */
    val lastFeedingTimestamp: Long?,
    /** 当天最后一次喂奶粉（瓶喂）记录时间 */
    val lastFormulaFeedingTimestamp: Long?
)

object FeedingDayStatsCalculator {
    fun fromItems(items: List<FeedingItem>): FeedingDayStats {
        val withAmount = items.filter { (it.amountMl ?: 0) > 0 }
        val total = withAmount.sumOf { it.amountMl ?: 0 }
        val count = withAmount.size
        val average = if (count > 0) total / count else null
        val formulaItems = withAmount.filter { isFormulaFeeding(it) }
        return FeedingDayStats(
            totalMl = total,
            recordCount = count,
            averageMl = average,
            lastFeedingTimestamp = withAmount.maxByOrNull { it.timestamp }?.timestamp,
            lastFormulaFeedingTimestamp = formulaItems.maxByOrNull { it.timestamp }?.timestamp
        )
    }

    private fun isFormulaFeeding(item: FeedingItem): Boolean {
        val note = item.note
        if (note.contains("配方") || note.contains("奶粉") || note.contains("冲奶")) return true
        val breast = note.contains("母乳") || note.contains("亲喂") ||
            note.contains("左母乳") || note.contains("右母乳")
        val ml = item.amountMl
        return ml != null && ml > 0 && !breast
    }
}
