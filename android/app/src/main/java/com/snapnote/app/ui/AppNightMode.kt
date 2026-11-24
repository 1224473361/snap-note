package com.snapnote.app.ui

import androidx.appcompat.app.AppCompatDelegate
import com.snapnote.app.model.AppThemeMode
import com.snapnote.app.model.toUiNightMode

fun applyAppNightMode(mode: AppThemeMode) {
    val nightMode = mode.toUiNightMode()
    if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
