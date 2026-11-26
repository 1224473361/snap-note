package com.hushnote.app.util

import com.hushnote.app.model.ChronicleType
import java.math.BigDecimal
import java.math.RoundingMode
object ChronicleFormat {
    fun parseDecimalInput(text: String): Double? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null
        return runCatching {
            BigDecimal(trimmed).setScale(2, RoundingMode.HALF_UP).toDouble()
        }.getOrNull()
    }

    fun formatDecimal(value: Double): String =
        BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString()

    fun filterDecimalInput(raw: String): String {
        val filtered = raw.filter { it.isDigit() || it == '.' }
        if (filtered.isEmpty()) return ""
        val parts = filtered.split('.')
        if (parts.size == 1) return parts[0].take(6)
        val intPart = parts[0].take(6)
        val frac = parts.drop(1).joinToString("").take(2)
        return if (frac.isEmpty() && filtered.endsWith('.')) "$intPart." else "$intPart.$frac"
    }

    fun displayValue(type: ChronicleType, value: Double?): String? {
        val v = value ?: return null
        val num = formatDecimal(v)
        return when (type) {
            ChronicleType.WEIGHT -> "$num kg"
            ChronicleType.HEIGHT -> "$num cm"
            ChronicleType.MILESTONE -> null
        }
    }

    fun typeLabel(type: ChronicleType): String = when (type) {
        ChronicleType.WEIGHT -> "体重"
        ChronicleType.HEIGHT -> "身高"
        ChronicleType.MILESTONE -> "里程碑"
    }
}
