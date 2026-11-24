package com.snapnote.app.ui

import androidx.compose.ui.unit.dp
import com.snapnote.app.model.compactChartDateLabel
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyMilkChartPagesTest {
    @Test
    fun compactDateLabelOmitsYear() {
        assertEquals("1001", compactChartDateLabel("2026-10-01"))
        assertEquals("0930", compactChartDateLabel("2026-09-30"))
    }

    @Test
    fun scrollableChartIsTwiceViewportForFourteenDays() {
        val width = chartContentWidth(viewportWidth = 300.dp, pointCount = 14)
        assertEquals(644.dp, width)
    }

    @Test
    fun chartFitsViewportWhenSevenOrFewerDays() {
        assertEquals(344.dp, chartContentWidth(300.dp, 7))
        assertEquals(344.dp, chartContentWidth(300.dp, 3))
    }
}
