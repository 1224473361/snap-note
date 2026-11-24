package com.snapnote.app.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapnote.app.model.DailyMilkPoint
import kotlin.math.ceil

private const val CHART_VISIBLE_DAYS = 7
/** 左右留白，避免首尾数值/日期被滚动裁切 */
private val ChartEdgeGutter = 22.dp

@Composable
fun DailyMilkChartCard(
    points: List<DailyMilkPoint>,
    modifier: Modifier = Modifier
) {
    val maxMl = remember(points) { chartMaxMl(points) }
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
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(AppColors.CardBackground, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Text(
            text = "每日奶量",
            color = AppColors.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
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
                DailyMilkChart(
                    points = points,
                    maxMl = maxMl,
                    modifier = Modifier
                        .width(chartWidth)
                        .height(chartHeight)
                )
            }
        }
    }
}

internal fun chartContentWidth(viewportWidth: Dp, pointCount: Int): Dp {
    val plotWidth = if (pointCount <= CHART_VISIBLE_DAYS) {
        viewportWidth
    } else {
        viewportWidth * (pointCount.toFloat() / CHART_VISIBLE_DAYS)
    }
    return plotWidth + ChartEdgeGutter * 2
}

internal fun chartMaxMl(points: List<DailyMilkPoint>): Int {
    val peak = points.maxOfOrNull { it.totalMl } ?: 0
    return if (peak <= 0) 100 else ceil(peak * 1.12 / 50.0).toInt() * 50
}

internal fun appendSmoothCurve(path: Path, coords: List<Offset>, moveToStart: Boolean = true) {
    if (coords.size < 2) return
    if (moveToStart) {
        path.moveTo(coords.first().x, coords.first().y)
    }
    for (i in 0 until coords.size - 1) {
        val p0 = coords[(i - 1).coerceAtLeast(0)]
        val p1 = coords[i]
        val p2 = coords[i + 1]
        val p3 = coords[(i + 2).coerceAtMost(coords.size - 1)]
        val cp1x = p1.x + (p2.x - p0.x) / 6f
        val cp1y = p1.y + (p2.y - p0.y) / 6f
        val cp2x = p2.x - (p3.x - p1.x) / 6f
        val cp2y = p2.y - (p3.y - p1.y) / 6f
        path.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
    }
}

/** 折线连接，避免多日 0 奶量后单日突增时样条曲线甩出屏幕外 */
internal fun appendChartPolyline(path: Path, coords: List<Offset>, moveToStart: Boolean = true) {
    if (coords.size < 2) return
    if (moveToStart) {
        path.moveTo(coords.first().x, coords.first().y)
    }
    for (i in 1 until coords.size) {
        path.lineTo(coords[i].x, coords[i].y)
    }
}

@Composable
private fun DailyMilkChart(
    points: List<DailyMilkPoint>,
    maxMl: Int,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val appFontFamily = LocalAppFontFamily.current
    val accentRed = AppColors.AccentRed
    val axisLineColor = AppColors.Divider
    val axisTextColor = AppColors.TextSecondary
    val axisLabelStyle = remember(appFontFamily, axisTextColor) {
        TextStyle(
            color = axisTextColor,
            fontSize = 11.sp,
            fontFamily = appFontFamily
        )
    }
    val valueLabelStyle = remember(appFontFamily, accentRed) {
        TextStyle(
            color = accentRed,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = appFontFamily
        )
    }

    Canvas(modifier = modifier) {
        val edgeGutter = ChartEdgeGutter.toPx()
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
            val ratio = if (maxMl == 0) 0f else point.totalMl.toFloat() / maxMl
            val y = topPad + chartHeight * (1f - ratio)
            Offset(x, y)
        }

        val textBoundsLeft = 0f
        val textBoundsRight = size.width

        points.forEachIndexed { index, point ->
            val label = textMeasurer.measure(point.label, axisLabelStyle)
            val x = coords[index].x
            val labelLeft = clampLabelLeft(x, label.size.width.toFloat(), textBoundsLeft, textBoundsRight)
            drawText(
                label,
                topLeft = Offset(labelLeft, baselineY + 8f)
            )
        }

        if (coords.size >= 2) {
            val areaPath = Path().apply {
                moveTo(coords.first().x, baselineY)
                lineTo(coords.first().x, coords.first().y)
                appendChartPolyline(this, coords, moveToStart = false)
                lineTo(coords.last().x, baselineY)
                close()
            }
            drawPath(
                areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentRed.copy(alpha = 0.18f),
                        accentRed.copy(alpha = 0f)
                    ),
                    startY = topPad,
                    endY = baselineY
                )
            )

            val linePath = Path().apply { appendChartPolyline(this, coords) }
            drawPath(
                linePath,
                color = accentRed,
                style = Stroke(width = 2f, cap = StrokeCap.Round)
            )
        }

        coords.forEachIndexed { index, offset ->
            drawCircle(accentRed, radius = 3.5f, center = offset)
            val ml = points[index].totalMl
            if (ml > 0) {
                val valueLabel = textMeasurer.measure(ml.toString(), valueLabelStyle)
                val labelLeft = clampLabelLeft(
                    offset.x,
                    valueLabel.size.width.toFloat(),
                    textBoundsLeft,
                    textBoundsRight
                )
                drawText(
                    valueLabel,
                    topLeft = Offset(
                        labelLeft,
                        offset.y - valueLabel.size.height - 6f
                    )
                )
            }
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
