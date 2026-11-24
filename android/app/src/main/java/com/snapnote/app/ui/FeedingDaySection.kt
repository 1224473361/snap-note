package com.snapnote.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapnote.app.model.FeedingDayStats
import com.snapnote.app.model.FeedingDayStatsCalculator
import com.snapnote.app.model.FeedingItem
import com.snapnote.app.model.FeedingStatsCardStyle
import com.snapnote.app.util.DateUtils
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

private val StatsCardShape = RoundedCornerShape(18.dp)

@Composable
fun FeedingDaySection(
    dateKey: String,
    items: List<FeedingItem>,
    statsCardStyle: FeedingStatsCardStyle = LocalFeedingStatsCardStyle.current,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    val stats = FeedingDayStatsCalculator.fromItems(items)

    val cardStyle = statsCardStyle
    val sectionTop = if (cardStyle == FeedingStatsCardStyle.COMPACT) 10.dp else 14.dp
    val sectionBottom = if (cardStyle == FeedingStatsCardStyle.COMPACT) 6.dp else 8.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = sectionTop, bottom = sectionBottom)
    ) {
        FeedingDayStatsCard(
            dateKey = dateKey,
            stats = stats,
            statsCardStyle = cardStyle,
            isToday = dateKey == DateUtils.toDateKey(System.currentTimeMillis())
        )
    }
}

private data class StatsCardStyleSpec(
    val shadowElevation: Dp,
    val paddingH: Dp,
    val paddingV: Dp,
    val dateSize: TextUnit,
    val totalSize: TextUnit,
    val totalUnitSize: TextUnit,
    val totalUnitBottom: Dp,
    val gridGap: Dp,
    val cellPaddingV: Dp,
    val labelSize: TextUnit,
    val valueSize: TextUnit,
    val iconSize: Dp,
    val labelValueGap: Dp
)

private fun specFor(style: FeedingStatsCardStyle): StatsCardStyleSpec = when (style) {
    FeedingStatsCardStyle.STANDARD -> StatsCardStyleSpec(
        shadowElevation = 10.dp,
        paddingH = 18.dp,
        paddingV = 16.dp,
        dateSize = 18.sp,
        totalSize = 40.sp,
        totalUnitSize = 15.sp,
        totalUnitBottom = 5.dp,
        gridGap = 14.dp,
        cellPaddingV = 10.dp,
        labelSize = 12.sp,
        valueSize = 15.sp,
        iconSize = 18.dp,
        labelValueGap = 6.dp
    )
    FeedingStatsCardStyle.COMPACT -> StatsCardStyleSpec(
        shadowElevation = 4.dp,
        paddingH = 12.dp,
        paddingV = 8.dp,
        dateSize = 14.sp,
        totalSize = 26.sp,
        totalUnitSize = 12.sp,
        totalUnitBottom = 3.dp,
        gridGap = 6.dp,
        cellPaddingV = 4.dp,
        labelSize = 10.sp,
        valueSize = 12.sp,
        iconSize = 14.dp,
        labelValueGap = 2.dp
    )
}

@Composable
private fun FeedingDayStatsCard(
    dateKey: String,
    stats: FeedingDayStats,
    statsCardStyle: FeedingStatsCardStyle,
    isToday: Boolean
) {
    val sinceLastFormula = rememberFormulaElapsedLabel(
        formulaTimestamp = stats.lastFormulaFeedingTimestamp,
        enabled = isToday
    )
    val highlightSinceLast = isToday && stats.lastFormulaFeedingTimestamp != null
    val spec = specFor(statsCardStyle)
    val gridDividerColor = AppColors.AccentRed.copy(alpha = 0.12f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = spec.shadowElevation,
                shape = StatsCardShape,
                clip = false,
                ambientColor = AppColors.AccentRed.copy(alpha = 0.12f),
                spotColor = AppColors.AccentRed.copy(alpha = 0.22f)
            )
            .clip(StatsCardShape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AppColors.FeedingStatsGradientTop,
                        AppColors.FeedingStatsGradientBottom
                    )
                )
            )
            .border(
                width = 1.dp,
                color = AppColors.AccentRed.copy(alpha = 0.14f),
                shape = StatsCardShape
            )
            .padding(horizontal = spec.paddingH, vertical = spec.paddingV)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = dateKey,
                color = AppColors.TextPrimary,
                fontSize = spec.dateSize,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (stats.totalMl > 0) "${stats.totalMl}" else "0",
                    color = AppColors.AccentRed,
                    fontSize = spec.totalSize,
                    fontWeight = FontWeight.Bold,
                    lineHeight = spec.totalSize
                )
                Text(
                    text = " ml",
                    color = AppColors.TextPrimary,
                    fontSize = spec.totalUnitSize,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 2.dp, bottom = spec.totalUnitBottom)
                )
            }
        }

        Spacer(modifier = Modifier.height(spec.gridGap))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            FeedingStatsGridRow(
                gridDividerColor = gridDividerColor,
                cellPaddingV = spec.cellPaddingV,
                first = {
                    FeedingGridStat(
                        label = "次数",
                        value = "${stats.recordCount}次",
                        icon = Icons.Outlined.FavoriteBorder,
                        spec = spec
                    )
                },
                second = {
                    FeedingGridStat(
                        label = "平均",
                        value = stats.averageMl?.let { "$it ml" } ?: "—",
                        icon = Icons.Outlined.LocalDrink,
                        spec = spec
                    )
                }
            )
            HorizontalDivider(color = gridDividerColor)
            FeedingStatsGridRow(
                gridDividerColor = gridDividerColor,
                cellPaddingV = spec.cellPaddingV,
                first = {
                    FeedingGridStat(
                        label = "最后一次",
                        value = stats.lastFeedingTimestamp?.let { DateUtils.formatTime(it) } ?: "—",
                        icon = Icons.Outlined.Schedule,
                        spec = spec
                    )
                },
                second = {
                    FeedingGridStat(
                        label = "上次喂奶",
                        value = sinceLastFormula,
                        icon = Icons.Outlined.Update,
                        valueAccent = highlightSinceLast,
                        spec = spec
                    )
                }
            )
        }
    }
}

@Composable
private fun FeedingStatsGridRow(
    gridDividerColor: androidx.compose.ui.graphics.Color,
    cellPaddingV: Dp,
    first: @Composable () -> Unit,
    second: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BoxStatCell(modifier = Modifier.weight(1f), paddingV = cellPaddingV, content = first)
        VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            color = gridDividerColor
        )
        BoxStatCell(modifier = Modifier.weight(1f), paddingV = cellPaddingV, content = second)
    }
}

@Composable
private fun BoxStatCell(
    modifier: Modifier = Modifier,
    paddingV: Dp,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.padding(vertical = paddingV, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

@Composable
private fun FeedingGridStat(
    label: String,
    value: String,
    icon: ImageVector,
    spec: StatsCardStyleSpec,
    valueAccent: Boolean = false
) {
    Text(
        text = label,
        color = AppColors.TextSecondary.copy(alpha = 0.95f),
        fontSize = spec.labelSize,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        lineHeight = 14.sp,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(spec.labelValueGap))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.AccentRed,
            modifier = Modifier.size(spec.iconSize)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            color = if (valueAccent) AppColors.AccentRed else AppColors.TextPrimary,
            fontSize = spec.valueSize,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun rememberFormulaElapsedLabel(formulaTimestamp: Long?, enabled: Boolean): String {
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(formulaTimestamp, enabled) {
        if (!enabled || formulaTimestamp == null) return@LaunchedEffect
        nowMs = System.currentTimeMillis()
        while (true) {
            delay(60_000L)
            nowMs = System.currentTimeMillis()
        }
    }
    if (!enabled || formulaTimestamp == null) return "—"
    return DateUtils.formatFeedingElapsed(sinceTimestamp = formulaTimestamp, nowMs = nowMs)
}
