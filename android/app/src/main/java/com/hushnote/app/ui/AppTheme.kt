package com.hushnote.app.ui

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import com.hushnote.app.model.AppFontPreset
import com.hushnote.app.model.AppThemeMode
import com.hushnote.app.model.FeedingStatsCardStyle

private fun lightMaterialScheme(palette: HushNoteColorPalette) = lightColorScheme(
    primary = palette.accentRed,
    onPrimary = palette.cardBackground,
    background = palette.pageBackground,
    onBackground = palette.textPrimary,
    surface = palette.cardBackground,
    onSurface = palette.textPrimary,
    onSurfaceVariant = palette.textSecondary,
    outline = palette.divider,
    error = palette.errorRed
)

private fun darkMaterialScheme(palette: HushNoteColorPalette) = darkColorScheme(
    primary = palette.accentRed,
    onPrimary = palette.pageBackground,
    background = palette.pageBackground,
    onBackground = palette.textPrimary,
    surface = palette.cardBackground,
    onSurface = palette.textPrimary,
    onSurfaceVariant = palette.textSecondary,
    outline = palette.divider,
    error = palette.errorRed
)

private fun Typography.withFontFamily(family: FontFamily): Typography {
    fun TextStyle.withFamily() = copy(fontFamily = family)
    return copy(
        displayLarge = displayLarge.withFamily(),
        displayMedium = displayMedium.withFamily(),
        displaySmall = displaySmall.withFamily(),
        headlineLarge = headlineLarge.withFamily(),
        headlineMedium = headlineMedium.withFamily(),
        headlineSmall = headlineSmall.withFamily(),
        titleLarge = titleLarge.withFamily(),
        titleMedium = titleMedium.withFamily(),
        titleSmall = titleSmall.withFamily(),
        bodyLarge = bodyLarge.withFamily(),
        bodyMedium = bodyMedium.withFamily(),
        bodySmall = bodySmall.withFamily(),
        labelLarge = labelLarge.withFamily(),
        labelMedium = labelMedium.withFamily(),
        labelSmall = labelSmall.withFamily()
    )
}

@Composable
fun AppTheme(
    fontSizeScale: Float = 1f,
    fontPreset: AppFontPreset = AppFontPreset.APP,
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    reduceMotion: Boolean = false,
    showSecondsInListTime: Boolean = false,
    hapticFeedbackEnabled: Boolean = true,
    feedingStatsCardStyle: FeedingStatsCardStyle = FeedingStatsCardStyle.STANDARD,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
    val palette = if (darkTheme) darkHushNoteColorPalette else lightHushNoteColorPalette
    val materialScheme = if (darkTheme) darkMaterialScheme(palette) else lightMaterialScheme(palette)
    val fontFamily = remember(fontPreset) { fontFamilyForPreset(fontPreset) }
    val typography = remember(fontFamily) { Typography().withFontFamily(fontFamily) }
    val baseDensity = LocalDensity.current
    val scaledDensity = remember(baseDensity, fontSizeScale) {
        Density(
            density = baseDensity.density,
            fontScale = baseDensity.fontScale * fontSizeScale
        )
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            applyAppNightMode(themeMode)
            val window = (view.context as Activity).window
            val backgroundArgb = palette.pageBackground.toArgb()
            window.setBackgroundDrawable(ColorDrawable(backgroundArgb))
            window.navigationBarColor = backgroundArgb
            val insets = WindowCompat.getInsetsController(window, view)
            insets.isAppearanceLightStatusBars = !darkTheme
            insets.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalHushNoteColors provides palette,
        LocalDensity provides scaledDensity,
        LocalReduceMotion provides reduceMotion,
        LocalShowSecondsInListTime provides showSecondsInListTime,
        LocalAppFontFamily provides fontFamily,
        LocalHapticFeedbackEnabled provides hapticFeedbackEnabled,
        LocalFeedingStatsCardStyle provides feedingStatsCardStyle
    ) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = typography,
            content = content
        )
    }
}

/** 全屏页面根容器，避免转场滑动时露出窗口默认底色。 */
@Composable
fun AppScreenRoot(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        content()
    }
}
