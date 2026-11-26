package com.hushnote.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val timeWithSecondsFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val utcTimeZone = TimeZone.getTimeZone("UTC")

    fun toDateKey(timestamp: Long): String = dateKeyFormat.format(Date(timestamp))

    /** 含今天在内的最近 N 天日期键集合（本地时区，用于按日增量同步）。 */
    fun recentDateKeys(days: Int, nowMs: Long = System.currentTimeMillis()): Set<String> {
        val calendar = Calendar.getInstance().apply { timeInMillis = nowMs }
        val keys = LinkedHashSet<String>()
        repeat(days.coerceAtLeast(1)) {
            keys.add(toDateKey(calendar.timeInMillis))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }
        return keys
    }

    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))

    /** 距某时刻经过的时间，不足 1 小时为「XXmin」，否则「XXhXXmin」。 */
    fun formatFeedingElapsed(sinceTimestamp: Long, nowMs: Long = System.currentTimeMillis()): String {
        val diff = nowMs - sinceTimestamp
        if (diff < 0) return "—"
        val totalMinutes = (diff / 60_000L).toInt().coerceAtLeast(1)
        if (totalMinutes < 60) return "${totalMinutes}min"
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return "${hours}h${minutes}min"
    }

    fun formatTimeWithSeconds(timestamp: Long): String = timeWithSecondsFormat.format(Date(timestamp))

    /** 本地时区的日期 + 时分，用于育儿记时间选择器。 */
    fun combineDateKeyAndTime(dateKey: String, hour: Int, minute: Int): Long {
        val parts = dateKey.split("-")
        if (parts.size != 3) return System.currentTimeMillis()
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, parts[0].toInt())
            set(Calendar.MONTH, parts[1].toInt() - 1)
            set(Calendar.DAY_OF_MONTH, parts[2].toInt())
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun dateKeyToMillis(dateKey: String): Long {
        val parts = dateKey.split("-")
        if (parts.size != 3) return System.currentTimeMillis()
        val calendar = Calendar.getInstance(utcTimeZone)
        calendar.set(Calendar.YEAR, parts[0].toInt())
        calendar.set(Calendar.MONTH, parts[1].toInt() - 1)
        calendar.set(Calendar.DAY_OF_MONTH, parts[2].toInt())
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun millisToDateKey(millis: Long): String {
        val calendar = Calendar.getInstance(utcTimeZone)
        calendar.timeInMillis = millis
        return String.format(
            Locale.US,
            "%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun formatDuration(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "$m:${s.toString().padStart(2, '0')}"
    }

    fun audioDurationSeconds(audioPath: String): Int {
        val file = java.io.File(audioPath)
        if (!file.exists() || file.length() <= 0) return 0
        if (file.extension.equals("wav", ignoreCase = true) && file.length() > 44) {
            val pcmBytes = file.length() - 44
            return (pcmBytes / (16000 * 2)).coerceAtLeast(1).toInt()
        }
        return runCatching {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val ms = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            retriever.release()
            (ms / 1000).coerceAtLeast(1).toInt()
        }.getOrDefault(0)
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes < 1024) return "${bytes} B"
        if (bytes < 1024 * 1024) return String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
        return String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
    }
}
