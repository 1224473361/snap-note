package com.snapnote.app.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/**
 * 弹层窗口可能把 [MaterialTheme] 重置成 Activity 的浅色 XML 主题。
 * 必须沿用调用处已解析的色板，不能按弹层内 Material 背景亮度反推。
 */
@Suppress("UNUSED_PARAMETER")
fun snapNoteOverlayPalette(
    parentPalette: SnapNoteColorPalette,
    dialogMaterialBackground: Color
): SnapNoteColorPalette {
    return parentPalette
}

data class SnapNoteOverlayThemeState(
    val palette: SnapNoteColorPalette,
    val colorScheme: ColorScheme,
    val typography: Typography
)

@Composable
fun rememberSnapNoteOverlayTheme(): SnapNoteOverlayThemeState {
    return SnapNoteOverlayThemeState(
        palette = LocalSnapNoteColors.current,
        colorScheme = MaterialTheme.colorScheme,
        typography = MaterialTheme.typography
    )
}

@Composable
fun ProvideSnapNoteOverlayTheme(
    theme: SnapNoteOverlayThemeState,
    content: @Composable () -> Unit
) {
    val palette = snapNoteOverlayPalette(
        parentPalette = theme.palette,
        dialogMaterialBackground = MaterialTheme.colorScheme.background
    )
    CompositionLocalProvider(LocalSnapNoteColors provides palette) {
        MaterialTheme(
            colorScheme = theme.colorScheme,
            typography = theme.typography,
            content = content
        )
    }
}
