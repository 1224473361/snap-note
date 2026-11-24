package com.snapnote.app.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapnote.app.model.ChronicleConstants
import com.snapnote.app.model.ChronicleItem
import com.snapnote.app.model.ChronicleType
import com.snapnote.app.util.ChronicleFormat
import com.snapnote.app.util.ChronicleImageStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChronicleRecordSheet(
    editItem: ChronicleItem?,
    onDismiss: () -> Unit,
    onSave: (
        type: ChronicleType,
        value: Double?,
        title: String,
        body: String,
        imagePaths: List<String>,
        timestamp: Long
    ) -> Unit
) {
    val isEdit = editItem != null
    val context = LocalContext.current
    val formKey = editItem?.id ?: "new"

    var selectedType by remember(formKey) {
        mutableStateOf(editItem?.type ?: ChronicleType.WEIGHT)
    }
    var timestamp by remember(formKey) {
        mutableLongStateOf(editItem?.timestamp ?: System.currentTimeMillis())
    }
    var valueText by remember(formKey) {
        mutableStateOf(editItem?.value?.let { ChronicleFormat.formatDecimal(it) } ?: "")
    }
    var title by remember(formKey) { mutableStateOf(editItem?.title ?: "") }
    var body by remember(formKey) { mutableStateOf(editItem?.body ?: "") }
    var imagePaths by remember(formKey) {
        mutableStateOf(editItem?.imagePaths ?: emptyList())
    }
    var closing by remember { mutableStateOf(false) }

    val pickImagesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        val room = ChronicleConstants.MAX_MILESTONE_IMAGES - imagePaths.size
        if (room <= 0) return@rememberLauncherForActivityResult
        val imported = uris.take(room).mapNotNull { ChronicleImageStore.importFromUri(context, it) }
        if (imported.isNotEmpty()) {
            imagePaths = ChronicleImageStore.trimToMax(imagePaths + imported)
        }
    }

    fun closeSheet(runAfterHide: () -> Unit = {}) {
        if (closing) return
        closing = true
        runAfterHide()
        onDismiss()
    }

    val tabIndex = when (selectedType) {
        ChronicleType.WEIGHT -> 0
        ChronicleType.HEIGHT -> 1
        ChronicleType.MILESTONE -> 2
    }

    SnapNoteAppSheet(onDismissRequest = { closeSheet() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEdit) "编辑大事记" else "记一笔",
                    color = AppColors.TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = {
                    closeSheet {
                        onSave(
                            selectedType,
                            ChronicleFormat.parseDecimalInput(valueText),
                            title.trim(),
                            body.trim(),
                            imagePaths,
                            timestamp
                        )
                    }
                }) {
                    Text(
                        "保存",
                        color = AppColors.AccentRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            PrimaryTabRow(
                selectedTabIndex = tabIndex,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { if (!isEdit) selectedType = ChronicleType.WEIGHT },
                    enabled = !isEdit || editItem?.type == ChronicleType.WEIGHT,
                    text = { Text("体重") }
                )
                Tab(
                    selected = tabIndex == 1,
                    onClick = { if (!isEdit) selectedType = ChronicleType.HEIGHT },
                    enabled = !isEdit || editItem?.type == ChronicleType.HEIGHT,
                    text = { Text("身高") }
                )
                Tab(
                    selected = tabIndex == 2,
                    onClick = { if (!isEdit) selectedType = ChronicleType.MILESTONE },
                    enabled = !isEdit || editItem?.type == ChronicleType.MILESTONE,
                    text = { Text("里程碑") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            ChronicleRecordFormFields(
                type = selectedType,
                valueText = valueText,
                onValueChange = { valueText = it },
                title = title,
                onTitleChange = { title = it },
                body = body,
                onBodyChange = { body = it },
                imagePaths = imagePaths,
                onRemoveImage = { path ->
                    imagePaths = imagePaths.filter { it != path }
                    ChronicleImageStore.deletePaths(listOf(path))
                },
                onAddImages = { pickImagesLauncher.launch("image/*") },
                timeSection = {
                    MealDateTimePickerRow(
                        timestamp = timestamp,
                        onTimestampChange = { timestamp = it }
                    )
                }
            )
        }
    }
}
