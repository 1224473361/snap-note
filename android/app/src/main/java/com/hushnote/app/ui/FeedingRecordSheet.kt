package com.hushnote.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hushnote.app.model.FeedingConstants
import com.hushnote.app.model.FeedingItem

@Composable
fun FeedingRecordSheet(
    editItem: FeedingItem?,
    quickPhrases: List<String>,
    lastAmountMl: Int?,
    feedingNoteRecording: Boolean,
    feedingNoteTranscribing: Boolean,
    feedingNoteRecordingDuration: String,
    feedingNoteVoiceResult: String?,
    onDismiss: () -> Unit,
    onSave: (amountMl: Int?, note: String, timestamp: Long, onComplete: (Boolean) -> Unit) -> Unit,
    onFeedingNoteVoiceToggle: () -> Unit,
    onFeedingNoteVoiceResultConsumed: () -> Unit
) {
    val formKey = editItem?.id ?: "new"
    var timestamp by remember(formKey) {
        mutableLongStateOf(editItem?.timestamp ?: System.currentTimeMillis())
    }
    var amountText by remember(formKey) {
        mutableStateOf(editItem?.amountMl?.toString() ?: "")
    }
    var note by remember(formKey) { mutableStateOf(editItem?.note ?: "") }
    var closing by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    val canSave = remember(amountText, note) {
        amountText.trim().toIntOrNull() != null || note.trim().isNotEmpty()
    }

    LaunchedEffect(feedingNoteVoiceResult) {
        feedingNoteVoiceResult?.let { text ->
            note = FeedingConstants.appendVoiceNote(note, text)
            onFeedingNoteVoiceResultConsumed()
        }
    }

    fun closeSheet() {
        if (closing) return
        closing = true
        onDismiss()
    }

    fun submitSave() {
        if (!canSave || saving || closing) return
        val amount = amountText.trim().toIntOrNull()
        val trimmedNote = note.trim()
        saving = true
        onSave(amount, trimmedNote, timestamp) { success ->
            saving = false
            if (success) closeSheet()
        }
    }

    val screenTitle = if (editItem != null) "编辑记录" else "保存记录"
    val sheetMaxHeight = (LocalConfiguration.current.screenHeightDp * 0.72f).dp

    HushNoteAppSheet(onDismissRequest = { closeSheet() }) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { closeSheet() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "关闭",
                        tint = AppColors.TextPrimary
                    )
                }
                Text(
                    text = screenTitle,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 48.dp),
                    color = AppColors.TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = sheetMaxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                FeedingRecordFormFields(
                    amountText = amountText,
                    onAmountChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    lastAmountMl = lastAmountMl,
                    note = note,
                    onNoteChange = { note = it },
                    quickPhrases = quickPhrases,
                    isRecording = feedingNoteRecording,
                    isTranscribing = feedingNoteTranscribing,
                    recordingDuration = feedingNoteRecordingDuration,
                    onVoiceToggle = onFeedingNoteVoiceToggle,
                    notePlaceholder = "添加备注",
                    minLines = 2,
                    showTimeLabel = false,
                    amountLabel = "奶量",
                    tagsBelowNote = true,
                    compact = true,
                    timeSection = {
                        FeedingDateTimeField(
                            timestamp = timestamp,
                            onTimestampChange = { timestamp = it }
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            FeedingPrimaryCapsuleButton(
                text = if (saving) "保存中…" else "保存",
                enabled = canSave && !saving,
                onClick = { submitSave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 16.dp)
            )
        }
    }
}
