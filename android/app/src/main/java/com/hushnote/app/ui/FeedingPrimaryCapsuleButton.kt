package com.hushnote.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CapsuleShape = RoundedCornerShape(28.dp)

@Composable
fun FeedingPrimaryCapsuleButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = AppColors.AccentRed
    val containerColor = if (enabled) accent else AppColors.SoftFill
    val labelColor = if (enabled) Color.White else AppColors.Placeholder
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(CapsuleShape)
            .hushNoteAccentFloatingShadow(
                shape = CapsuleShape,
                elevation = if (enabled) HushNoteFloatingStyle.CapsuleElevation else 0.dp,
                enabled = enabled
            )
            .then(
                if (enabled) {
                    Modifier.clickableWithPressScale(onClick = onClick)
                } else {
                    Modifier
                }
            )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CapsuleShape,
            color = containerColor,
            shadowElevation = 0.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = labelColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
