package com.hushnote.app.ui

import androidx.appcompat.app.AppCompatDelegate
import com.hushnote.app.model.AppThemeMode
import com.hushnote.app.model.toUiNightMode

fun applyAppNightMode(mode: AppThemeMode) {
    val nightMode = mode.toUiNightMode()
    if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }
}
