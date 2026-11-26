package com.hushnote.app.ui

import android.view.HapticFeedbackConstants
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView

@Composable
fun rememberClickHapticAction(): () -> Unit {
    val view = LocalView.current
    val enabled = LocalHapticFeedbackEnabled.current
    return {
        if (enabled) {
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        }
    }
}
