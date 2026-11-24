package com.snapnote.app.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.snapnote.app.MainActivity
import com.snapnote.app.R

object ShortcutHelper {
    fun pinRecordShortcut(context: Context) {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            Toast.makeText(context, "当前系统不支持添加快捷方式", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(MainActivity.EXTRA_QUICK_RECORD, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val shortcut = ShortcutInfoCompat.Builder(context, "quick_record")
            .setShortLabel("snap-note 录音")
            .setLongLabel("打开 snap-note 快速录音")
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_mic_shortcut))
            .setIntent(intent)
            .build()
        ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
        Toast.makeText(context, "请在系统弹窗中确认添加到桌面", Toast.LENGTH_SHORT).show()
    }
}
