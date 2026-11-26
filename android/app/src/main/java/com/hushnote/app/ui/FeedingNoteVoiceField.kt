package com.hushnote.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hushnote.app.model.FeedingConstants

@Composable
private fun FeedingVoiceMicButton(
    isRecording: Boolean,
    isTranscribing: Boolean,
    onClick: () -> Unit
) {
    val buttonSize = 40.dp
    val background = when {
        isRecording -> AppColors.AccentRed
        isTranscribing -> AppColors.SoftFill
        else -> AppColors.CardBackground
    }
    val iconTint = when {
        isRecording -> Color.White
        isTranscribing -> AppColors.TextSecondary
        else -> AppColors.AccentRed
    }
    val floatEnabled = !isTranscribing
    Box(
        modifier = Modifier
            .size(buttonSize)
            .clip(CircleShape)
            .hushNoteAccentFloatingShadow(
                shape = CircleShape,
                elevation = if (floatEnabled) HushNoteFloatingStyle.SmallCircleElevation else 0.dp,
                enabled = floatEnabled
            )
            .then(
                if (floatEnabled) {
                    Modifier.clickableWithPressScale(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(buttonSize),
            shape = CircleShape,
            color = background,
            shadowElevation = 0.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isRecording) "停止语音" else "语音输入",
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedingNoteVoiceField(
    note: String,
    onNoteChange: (String) -> Unit,
    quickPhrases: List<String>,
    isRecording: Boolean,
    isTranscribing: Boolean,
    recordingDuration: String,
    onVoiceToggle: () -> Unit,
    notePlaceholder: String = "左母乳、睡觉15:30-16:00、备注…",
    minLines: Int = 2,
    tagsBelowNote: Boolean = false,
    compact: Boolean = false
) {
    val noteMinHeight = if (compact) 56 else 112
    val tagTopGap = if (compact) 6.dp else 12.dp
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FeedingFormLabel(text = "描述")
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isRecording) {
                    Text(
                        text = recordingDuration,
                        color = AppColors.AccentRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                } else if (isTranscribing) {
                    Text(
                        text = "转写中…",
                        color = AppColors.TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                FeedingVoiceMicButton(
                    isRecording = isRecording,
                    isTranscribing = isTranscribing,
                    onClick = onVoiceToggle
                )
            }
        }
        Spacer(modifier = Modifier.height(if (compact) 8.dp else 10.dp))

        if (tagsBelowNote) {
            NoteInputCard(
                note = note,
                onNoteChange = onNoteChange,
                notePlaceholder = notePlaceholder,
                minLines = minLines,
                minHeightDp = noteMinHeight
            )
            if (quickPhrases.isNotEmpty()) {
                Spacer(modifier = Modifier.height(tagTopGap))
                QuickPhraseFlow(
                    quickPhrases = quickPhrases,
                    note = note,
                    onNoteChange = onNoteChange,
                    chipOnCard = true
                )
                if (compact) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppColors.CardBackground)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickPhraseFlow(
                    quickPhrases = quickPhrases,
                    note = note,
                    onNoteChange = onNoteChange,
                    chipOnCard = false
                )
                NoteInputCard(
                    note = note,
                    onNoteChange = onNoteChange,
                    notePlaceholder = notePlaceholder,
                    minLines = minLines,
                    minHeightDp = (minLines * 24).coerceAtLeast(72),
                    plain = true
                )
            }
        }
    }
}

@Composable
private fun NoteInputCard(
    note: String,
    onNoteChange: (String) -> Unit,
    notePlaceholder: String,
    minLines: Int,
    minHeightDp: Int,
    plain: Boolean = false
) {
    val fieldModifier = if (plain) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.CardBackground)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    }
    BasicTextField(
        value = note,
        onValueChange = onNoteChange,
        modifier = fieldModifier
            .height(minHeightDp.dp),
        singleLine = false,
        minLines = minLines,
        textStyle = TextStyle(
            color = AppColors.TextPrimary,
            fontSize = 15.sp,
            lineHeight = 22.sp
        ),
        cursorBrush = SolidColor(AppColors.AccentRed),
        decorationBox = { inner ->
            Box {
                if (note.isEmpty() && notePlaceholder.isNotBlank()) {
                    Text(
                        notePlaceholder,
                        color = AppColors.Placeholder,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
                inner()
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickPhraseFlow(
    quickPhrases: List<String>,
    note: String,
    onNoteChange: (String) -> Unit,
    chipOnCard: Boolean
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        quickPhrases.forEach { chip ->
            val shape = RoundedCornerShape(20.dp)
            val bg = if (chipOnCard) AppColors.CardBackground else AppColors.FeedingStatsFooterBg
            Text(
                text = chip,
                color = AppColors.TextPrimary,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(shape)
                    .background(bg)
                    .border(1.dp, AppColors.ChipBorder, shape)
                    .clickable { onNoteChange(FeedingConstants.appendQuickNote(note, chip)) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}
