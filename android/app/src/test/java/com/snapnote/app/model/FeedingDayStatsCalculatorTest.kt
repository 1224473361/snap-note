package com.snapnote.app.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FeedingDayStatsCalculatorTest {
    @Test
    fun countsOnlyRecordsWithMilkVolume() {
        val items = listOf(
            FeedingItem("1", 1000L, "2026-10-01", 70, "a"),
            FeedingItem("2", 2000L, "2026-10-01", null, "醒了"),
            FeedingItem("3", 3000L, "2026-10-01", 100, "b")
        )
        val stats = FeedingDayStatsCalculator.fromItems(items)
        assertEquals(170, stats.totalMl)
        assertEquals(2, stats.recordCount)
        assertEquals(85, stats.averageMl)
        assertEquals(3000L, stats.lastFeedingTimestamp)
    }
}
