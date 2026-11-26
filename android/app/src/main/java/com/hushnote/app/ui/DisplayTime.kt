package com.hushnote.app.ui

import androidx.compose.runtime.Composable
import com.hushnote.app.util.DateUtils

@Composable
fun formatListTime(timestamp: Long): String {
    return if (LocalShowSecondsInListTime.current) {
        DateUtils.formatTimeWithSeconds(timestamp)
    } else {
        DateUtils.formatTime(timestamp)
    }
}
