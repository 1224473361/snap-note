package com.hushnote.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Modifier.feedingFormWarmBackground(): Modifier {
    val top = AppColors.FeedingStatsGradientTop
    val bottom = AppColors.FeedingStatsGradientBottom
    return background(brush = Brush.verticalGradient(colors = listOf(top, bottom)))
}

@Composable
fun FeedingFormLabel(text: String) {
    Text(
        text = text,
        color = AppColors.TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun FeedingFormTimeRow(
    timeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasizeTime: Boolean = false,
    showChangeButton: Boolean = true,
    compact: Boolean = false
) {
    val verticalPad = if (compact) 10.dp else 16.dp
    val horizontalPad = if (compact) 14.dp else 16.dp
    val iconSize = if (compact) 18.dp else 20.dp
    val fontSize = when {
        emphasizeTime -> 20.sp
        compact -> 15.sp
        else -> 16.sp
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (compact) 14.dp else 16.dp))
            .background(AppColors.CardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = horizontalPad, vertical = verticalPad),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Schedule,
            contentDescription = null,
            tint = AppColors.TextSecondary,
            modifier = Modifier.size(iconSize)
        )
        Spacer(modifier = Modifier.width(if (compact) 10.dp else 12.dp))
        Text(
            text = timeText,
            color = AppColors.TextPrimary,
            fontSize = fontSize,
            fontWeight = if (emphasizeTime) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        if (showChangeButton) {
            Text(
                text = "更改",
                color = AppColors.AccentRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, AppColors.AccentRed.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun FeedingRecordFormFields(
    amountText: String,
    onAmountChange: (String) -> Unit,
    lastAmountMl: Int?,
    note: String,
    onNoteChange: (String) -> Unit,
    quickPhrases: List<String>,
    isRecording: Boolean,
    isTranscribing: Boolean,
    recordingDuration: String,
    onVoiceToggle: () -> Unit,
    notePlaceholder: String,
    minLines: Int,
    timeSection: @Composable () -> Unit,
    showTimeLabel: Boolean = true,
    amountLabel: String = "奶量（可选）",
    tagsBelowNote: Boolean = false,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val sectionGap = if (compact) 10.dp else 20.dp
    val labelGap = if (compact) 6.dp else 10.dp
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(sectionGap)
    ) {
        if (showTimeLabel) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FeedingFormLabel(text = "时间")
                timeSection()
            }
        } else {
            timeSection()
        }

        Column(verticalArrangement = Arrangement.spacedBy(labelGap)) {
            FeedingFormLabel(text = amountLabel)
            Row(verticalAlignment = Alignment.CenterVertically) {
                FeedingFormAmountField(
                    value = amountText,
                    onValueChange = onAmountChange,
                    modifier = Modifier.width(112.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("ml", color = AppColors.TextSecondary, fontSize = 15.sp)
                if (lastAmountMl != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "上次 $lastAmountMl",
                        color = AppColors.AccentRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onAmountChange(lastAmountMl.toString()) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }

        FeedingNoteVoiceField(
            note = note,
            onNoteChange = onNoteChange,
            quickPhrases = quickPhrases,
            isRecording = isRecording,
            isTranscribing = isTranscribing,
            recordingDuration = recordingDuration,
            onVoiceToggle = onVoiceToggle,
            notePlaceholder = notePlaceholder,
            minLines = minLines,
            tagsBelowNote = tagsBelowNote,
            compact = compact
        )
    }
}
