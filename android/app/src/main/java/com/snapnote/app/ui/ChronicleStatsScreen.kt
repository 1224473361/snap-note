package com.snapnote.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapnote.app.model.ChronicleItem
import com.snapnote.app.model.ChronicleValuePoint
import com.snapnote.app.util.DateUtils

@Composable
fun ChronicleStatsScreen(
    weightPoints: List<ChronicleValuePoint>,
    heightPoints: List<ChronicleValuePoint>,
    milestones: List<ChronicleItem>,
    onBack: () -> Unit
) {
    SubScreenScaffold(title = "成长统计", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ChronicleValueChartCard(
                title = "体重曲线",
                unit = "kg",
                points = weightPoints,
                lineColor = AppColors.AccentRed
            )
            ChronicleValueChartCard(
                title = "身高曲线",
                unit = "cm",
                points = heightPoints,
                lineColor = Color(0xFF2E7D6B)
            )
            MilestoneTimelineSection(milestones = milestones)
        }
    }
}

@Composable
private fun MilestoneTimelineSection(milestones: List<ChronicleItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "里程碑",
            color = AppColors.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (milestones.isEmpty()) {
            Text(
                text = "暂无里程碑记录",
                color = AppColors.TextSecondary,
                fontSize = 14.sp
            )
            return
        }
        milestones.forEachIndexed { index, item ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = item.title.ifBlank { "（无标题）" },
                    color = AppColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.dateKey} ${DateUtils.formatTime(item.timestamp)}",
                    color = AppColors.AccentRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (item.body.isNotBlank()) {
                    Text(
                        text = item.body,
                        color = AppColors.TextSecondary,
                        fontSize = 14.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                if (index < milestones.lastIndex) {
                    HorizontalDivider(
                        color = AppColors.Divider,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}
