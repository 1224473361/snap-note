package com.snapnote.app.model

import com.snapnote.app.util.ChronicleFormat
import java.text.SimpleDateFormat
import java.util.Locale

data class ChronicleValuePoint(
    val timestamp: Long,
    val label: String,
    val value: Double,
    val valueLabel: String
)

object ChronicleSeriesBuilder {
    private val dayLabelFormat = SimpleDateFormat("M/d", Locale.getDefault())

    fun weightPoints(items: List<ChronicleItem>): List<ChronicleValuePoint> =
        valueSeries(items, ChronicleType.WEIGHT)

    fun heightPoints(items: List<ChronicleItem>): List<ChronicleValuePoint> =
        valueSeries(items, ChronicleType.HEIGHT)

    fun milestones(items: List<ChronicleItem>): List<ChronicleItem> =
        items
            .filter { it.type == ChronicleType.MILESTONE }
            .sortedByDescending { it.timestamp }

    private fun valueSeries(items: List<ChronicleItem>, type: ChronicleType): List<ChronicleValuePoint> {
        return items
            .filter { it.type == type && it.value != null && it.value > 0 }
            .sortedBy { it.timestamp }
            .map { item ->
                val v = item.value!!
                ChronicleValuePoint(
                    timestamp = item.timestamp,
                    label = dayLabelFormat.format(item.timestamp),
                    value = v,
                    valueLabel = ChronicleFormat.formatDecimal(v)
                )
            }
    }
}
