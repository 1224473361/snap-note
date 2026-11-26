package com.hushnote.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hushnote.app.model.ChronicleValuePoint
import kotlin.math.ceil

private val ChronicleChartEdgeGutter = 22.dp

@Composable
fun ChronicleValueChartCard(
    title: String,
    unit: String,
    points: List<ChronicleValuePoint>,
    modifier: Modifier = Modifier,
    lineColor: androidx.compose.ui.graphics.Color = AppColors.AccentRed
) {
    val maxValue = remember(points) { chartMaxValue(points) }
    val scrollState = rememberScrollState()
    var initialScrollDone by remember(points) { mutableStateOf(false) }

    LaunchedEffect(points, scrollState.maxValue) {
        if (!initialScrollDone && scrollState.maxValue > 0) {
            scrollState.scrollTo(scrollState.maxValue)
            initialScrollDone = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.CardBackground, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Text(
            text = title,
            color = AppColors.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (points.isEmpty()) {
            Text(
                text = "暂无数据",
                color = AppColors.TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            )
            return
        }
        val chartHeight = 200.dp
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(chartHeight)
        ) {
            val chartWidth = chartContentWidth(viewportWidth = maxWidth, pointCount = points.size)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                ChronicleValueChart(
                    points = points,
                    maxValue = maxValue,
                    unit = unit,
                    lineColor = lineColor,
                    modifier = Modifier
                        .width(chartWidth)
                        .height(chartHeight)
                )
            }
        }
    }
}

private fun chartMaxValue(points: List<ChronicleValuePoint>): Double {
    val peak = points.maxOfOrNull { it.value } ?: 0.0
    if (peak <= 0) return 10.0
    val padded = peak * 1.12
    return ceil(padded * 10) / 10.0
}

@Composable
private fun ChronicleValueChart(
    points: List<ChronicleValuePoint>,
    maxValue: Double,
    unit: String,
    lineColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val appFontFamily = LocalAppFontFamily.current
    val axisLineColor = AppColors.Divider
    val axisTextColor = AppColors.TextSecondary
    val axisLabelStyle = remember(appFontFamily, axisTextColor) {
        TextStyle(color = axisTextColor, fontSize = 11.sp, fontFamily = appFontFamily)
    }
    val valueLabelStyle = remember(appFontFamily, lineColor) {
        TextStyle(
            color = lineColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = appFontFamily
        )
    }

    Canvas(modifier = modifier) {
        val edgeGutter = ChronicleChartEdgeGutter.toPx()
        val plotLeft = edgeGutter
        val plotRight = size.width - edgeGutter
        val topPad = 22f
        val bottomPad = 26f
        val chartWidth = plotRight - plotLeft
        val chartHeight = size.height - topPad - bottomPad
        if (chartWidth <= 0f || chartHeight <= 0f) return@Canvas

        val baselineY = topPad + chartHeight
        drawLine(
            axisLineColor,
            Offset(plotLeft, baselineY),
            Offset(plotRight, baselineY),
            strokeWidth = 1f
        )

        val stepX = if (points.size <= 1) chartWidth else chartWidth / (points.size - 1)
        val coords = points.mapIndexed { index, point ->
            val x = plotLeft + stepX * index
            val ratio = if (maxValue == 0.0) 0f else (point.value / maxValue).toFloat()
            val y = topPad + chartHeight * (1f - ratio)
            Offset(x, y)
        }

        val textBoundsLeft = 0f
        val textBoundsRight = size.width

        points.forEachIndexed { index, point ->
            val label = textMeasurer.measure(point.label, axisLabelStyle)
            val x = coords[index].x
            val labelLeft = clampLabelLeft(x, label.size.width.toFloat(), textBoundsLeft, textBoundsRight)
            drawText(label, topLeft = Offset(labelLeft, baselineY + 8f))
        }

        if (coords.size >= 2) {
            val areaPath = Path().apply {
                moveTo(coords.first().x, baselineY)
                lineTo(coords.first().x, coords.first().y)
                appendSmoothCurve(this, coords, moveToStart = false)
                lineTo(coords.last().x, baselineY)
                close()
            }
            drawPath(
                areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.18f), lineColor.copy(alpha = 0f)),
                    startY = topPad,
                    endY = baselineY
                )
            )
            val linePath = Path().apply { appendSmoothCurve(this, coords) }
            drawPath(linePath, color = lineColor, style = Stroke(width = 2f, cap = StrokeCap.Round))
        }

        coords.forEachIndexed { index, offset ->
            drawCircle(lineColor, radius = 3.5f, center = offset)
            val valueLabel = textMeasurer.measure("${points[index].valueLabel}$unit", valueLabelStyle)
            val labelLeft = clampLabelLeft(
                offset.x,
                valueLabel.size.width.toFloat(),
                textBoundsLeft,
                textBoundsRight
            )
            drawText(
                valueLabel,
                topLeft = Offset(labelLeft, offset.y - valueLabel.size.height - 6f)
            )
        }
    }
}

private fun clampLabelLeft(
    anchorX: Float,
    labelWidth: Float,
    boundsLeft: Float,
    boundsRight: Float
): Float {
    if (labelWidth <= 0f) return anchorX
    val ideal = anchorX - labelWidth / 2f
    val maxLeft = (boundsRight - labelWidth).coerceAtLeast(boundsLeft)
    return ideal.coerceIn(boundsLeft, maxLeft)
}
