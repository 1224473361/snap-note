package com.snapnote.app.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

object HapticUtils {
    /** 类似微信按住说话时的轻微点按反馈；华为/鸿蒙机型增加 Vibrator 兜底 */
    fun performRecordPressFeedback(context: Context, view: View? = null) {
        var triggered = false
        view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)?.let { triggered = it }
        if (!triggered) {
            view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
        vibrateFallback(context)
    }

    private fun vibrateFallback(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!vibrator.hasVibrator()) return
            vibrator.vibrate(
                VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(20)
        }
    }
}
