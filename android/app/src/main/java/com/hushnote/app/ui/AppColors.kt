package com.hushnote.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class HushNoteColorPalette(
    val pageBackground: Color,
    val cardBackground: Color,
    val accentRed: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val successGreen: Color,
    val errorRed: Color,
    val feedingStatsGradientTop: Color,
    val feedingStatsGradientBottom: Color,
    val feedingStatsFooterBg: Color,
    val softFill: Color,
    val chipBackground: Color,
    val chipBorder: Color,
    val placeholder: Color,
    val scrim: Color
)

val LocalHushNoteColors = staticCompositionLocalOf { lightHushNoteColorPalette }

val lightHushNoteColorPalette = HushNoteColorPalette(
    pageBackground = Color(0xFFF5F5F5),
    cardBackground = Color.White,
    accentRed = Color(0xFFE84026),
    textPrimary = Color(0xFF333333),
    textSecondary = Color(0xFF999999),
    divider = Color(0xFFEEEEEE),
    successGreen = Color(0xFF43A047),
    errorRed = Color(0xFFD32F2F),
    feedingStatsGradientTop = Color(0xFFFFFBF9),
    feedingStatsGradientBottom = Color(0xFFFFEFE8),
    feedingStatsFooterBg = Color(0xFFFFF7F4),
    softFill = Color(0xFFF7F7F7),
    chipBackground = Color(0xFFF3F3F3),
    chipBorder = Color(0xFFFFE4DC),
    placeholder = Color(0xFFBBBBBB),
    scrim = Color.Black.copy(alpha = 0.4f)
)

val darkHushNoteColorPalette = HushNoteColorPalette(
    pageBackground = Color(0xFF121212),
    cardBackground = Color(0xFF1E1E1E),
    accentRed = Color(0xFFFF6B52),
    textPrimary = Color(0xFFE8E8E8),
    textSecondary = Color(0xFF9E9E9E),
    divider = Color(0xFF2E2E2E),
    successGreen = Color(0xFF66BB6A),
    errorRed = Color(0xFFEF5350),
    feedingStatsGradientTop = Color(0xFF2A2422),
    feedingStatsGradientBottom = Color(0xFF1C1816),
    feedingStatsFooterBg = Color(0xFF2E2624),
    softFill = Color(0xFF2A2A2A),
    chipBackground = Color(0xFF2C2C2C),
    chipBorder = Color(0xFF4A3530),
    placeholder = Color(0xFF6E6E6E),
    scrim = Color.Black.copy(alpha = 0.65f)
)

/** 主题色板访问入口；需在 [AppTheme] 内使用。 */
object AppColors {
    private val palette: HushNoteColorPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalHushNoteColors.current

    val PageBackground: Color
        @Composable @ReadOnlyComposable get() = palette.pageBackground
    val CardBackground: Color
        @Composable @ReadOnlyComposable get() = palette.cardBackground
    val AccentRed: Color
        @Composable @ReadOnlyComposable get() = palette.accentRed
    val TextPrimary: Color
        @Composable @ReadOnlyComposable get() = palette.textPrimary
    val TextSecondary: Color
        @Composable @ReadOnlyComposable get() = palette.textSecondary
    val Divider: Color
        @Composable @ReadOnlyComposable get() = palette.divider
    val SuccessGreen: Color
        @Composable @ReadOnlyComposable get() = palette.successGreen
    val ErrorRed: Color
        @Composable @ReadOnlyComposable get() = palette.errorRed
    val FeedingStatsGradientTop: Color
        @Composable @ReadOnlyComposable get() = palette.feedingStatsGradientTop
    val FeedingStatsGradientBottom: Color
        @Composable @ReadOnlyComposable get() = palette.feedingStatsGradientBottom
    val FeedingStatsFooterBg: Color
        @Composable @ReadOnlyComposable get() = palette.feedingStatsFooterBg
    val SoftFill: Color
        @Composable @ReadOnlyComposable get() = palette.softFill
    val ChipBackground: Color
        @Composable @ReadOnlyComposable get() = palette.chipBackground
    val ChipBorder: Color
        @Composable @ReadOnlyComposable get() = palette.chipBorder
    val Placeholder: Color
        @Composable @ReadOnlyComposable get() = palette.placeholder
    val Scrim: Color
        @Composable @ReadOnlyComposable get() = palette.scrim
}
