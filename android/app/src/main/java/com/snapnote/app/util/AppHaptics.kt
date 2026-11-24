package com.snapnote.app.util

import android.content.Context
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

object AppHaptics {

    fun performSuccess(context: Context) {
        val view = findDecorView(context) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
        }
    }

    fun performClick(context: Context) {
        findDecorView(context)?.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    private fun findDecorView(context: Context): View? {
        return when (context) {
            is android.app.Activity -> context.window.decorView
            is android.content.ContextWrapper -> findDecorView(context.baseContext)
            else -> null
        }
    }
}
