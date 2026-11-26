package com.hushnote.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hushnote.app.model.DailyMilkPoint

@Composable
fun FeedingStatsScreen(
    dailyMilkPoints: List<DailyMilkPoint>,
    onBack: () -> Unit
) {
    SubScreenScaffold(title = "统计", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            DailyMilkChartCard(points = dailyMilkPoints)
        }
    }
}
