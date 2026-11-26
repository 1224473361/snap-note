package com.hushnote.app.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppThemeModeNightModeTest {
    @Test
    fun darkFollowsAppCompatNightYes() {
        assertEquals(2, AppThemeMode.DARK.toUiNightMode())
    }

    @Test
    fun lightFollowsAppCompatNightNo() {
        assertEquals(1, AppThemeMode.LIGHT.toUiNightMode())
    }

    @Test
    fun systemFollowsAppCompatNightFollowSystem() {
        assertEquals(-1, AppThemeMode.SYSTEM.toUiNightMode())
    }
}
