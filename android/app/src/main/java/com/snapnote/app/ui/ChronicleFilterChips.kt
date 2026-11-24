package com.snapnote.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snapnote.app.model.ChronicleFilter

private val filterLabels = listOf(
    ChronicleFilter.ALL to "全部",
    ChronicleFilter.WEIGHT to "体重",
    ChronicleFilter.HEIGHT to "身高",
    ChronicleFilter.MILESTONE to "里程碑"
)

@Composable
fun ChronicleFilterChips(
    selected: ChronicleFilter,
    onSelected: (ChronicleFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filterLabels.forEach { (filter, label) ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelected(filter) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppColors.AccentRed.copy(alpha = 0.12f),
                    selectedLabelColor = AppColors.AccentRed
                )
            )
        }
    }
}

fun ChronicleFilter.matchesItem(type: com.snapnote.app.model.ChronicleType): Boolean = when (this) {
    ChronicleFilter.ALL -> true
    ChronicleFilter.WEIGHT -> type == com.snapnote.app.model.ChronicleType.WEIGHT
    ChronicleFilter.HEIGHT -> type == com.snapnote.app.model.ChronicleType.HEIGHT
    ChronicleFilter.MILESTONE -> type == com.snapnote.app.model.ChronicleType.MILESTONE
}
