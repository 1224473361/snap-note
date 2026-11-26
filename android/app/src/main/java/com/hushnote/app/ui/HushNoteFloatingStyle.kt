package com.hushnote.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 与首页底部「+」FAB 一致的红色光晕阴影参数 */
object HushNoteFloatingStyle {
    val HomeFabElevation = 12.dp
    val HomeFabElevationPressed = 16.dp
    val CapsuleElevation = 12.dp
    val SmallCircleElevation = 8.dp
    const val AmbientAlpha = 0.2f
    const val SpotAlpha = 0.34f
}

@Composable
fun Modifier.hushNoteAccentFloatingShadow(
    shape: Shape,
    elevation: Dp = HushNoteFloatingStyle.HomeFabElevation,
    enabled: Boolean = true
): Modifier {
    if (!enabled) return this
    val accent = AppColors.AccentRed
    return shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = accent.copy(alpha = HushNoteFloatingStyle.AmbientAlpha),
        spotColor = accent.copy(alpha = HushNoteFloatingStyle.SpotAlpha)
    )
}
