package com.hushnote.app.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontFamily

val LocalReduceMotion = compositionLocalOf { false }

val LocalShowSecondsInListTime = compositionLocalOf { false }

val LocalAppFontFamily = compositionLocalOf<FontFamily> { FontFamily.Default }

val LocalHapticFeedbackEnabled = compositionLocalOf { true }

val LocalFeedingStatsCardStyle = compositionLocalOf { com.hushnote.app.model.FeedingStatsCardStyle.STANDARD }
