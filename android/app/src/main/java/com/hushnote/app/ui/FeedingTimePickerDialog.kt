package com.hushnote.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun FeedingTimePickerDialog(
    timestamp: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var current by remember(timestamp) { mutableLongStateOf(timestamp) }
    val overlay = rememberHushNoteOverlayTheme()
    Dialog(onDismissRequest = onDismiss) {
        ProvideHushNoteOverlayTheme(overlay) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.CardBackground, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "调整时间",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = AppColors.TextPrimary
            )
            MealDateTimePickerRow(
                timestamp = current,
                onTimestampChange = { current = it }
            )
            TextButton(
                onClick = { onConfirm(current) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("确定", color = AppColors.AccentRed)
            }
        }
        }
    }
}
