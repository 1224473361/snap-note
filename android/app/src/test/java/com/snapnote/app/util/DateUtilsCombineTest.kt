package com.snapnote.app.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class DateUtilsCombineTest {
    @Test
    fun combineDateKeyAndTime_usesLocalCalendarFields() {
        val millis = DateUtils.combineDateKeyAndTime("2026-10-07", 19, 21)
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        assertEquals(2026, cal.get(Calendar.YEAR))
        assertEquals(9, cal.get(Calendar.MONTH))
        assertEquals(7, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(19, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(21, cal.get(Calendar.MINUTE))
    }
}
