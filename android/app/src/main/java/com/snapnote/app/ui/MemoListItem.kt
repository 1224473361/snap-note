package com.snapnote.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapnote.app.model.MemoConstants
import com.snapnote.app.model.MemoItem
import com.snapnote.app.model.MemoStatus
import com.snapnote.app.util.DateUtils

@Composable
fun MemoListItem(
    item: MemoItem,
    isPlaying: Boolean,
    isPaused: Boolean,
    onPlayToggle: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    onEdit: ((MemoItem) -> Unit)? = null
) {
    val content = if (item.status == MemoStatus.PROCESSING) MemoConstants.PROCESSING else item.content
    val isProcessing = item.status == MemoStatus.PROCESSING
    val isActivePlayback = isPlaying && !isPaused
    val playPulse = if (isActivePlayback) {
        rememberInfiniteTransition(label = "playPulse").animateFloat(
            initialValue = 1f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(700),
                repeatMode = RepeatMode.Reverse
            ),
            label = "playScale"
        ).value
    } else {
        1f
    }
    val durationSeconds = item.durationSeconds.takeIf { it > 0 }
        ?: DateUtils.audioDurationSeconds(item.audioPath)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.PageBackground)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier
                .width(64.dp)
                .padding(top = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatListTime(item.timestamp),
                color = AppColors.TextSecondary,
                fontSize = 14.sp
            )
            if (durationSeconds > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = DateUtils.formatDuration(durationSeconds),
                    color = AppColors.TextSecondary.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                IconButton(
                    onClick = { onPlayToggle(item.id, item.audioPath) },
                    enabled = !isProcessing,
                    modifier = Modifier
                        .scale(playPulse)
                        .size(36.dp)
                        .background(
                            color = if (isProcessing) {
                                AppColors.TextSecondary.copy(alpha = 0.5f)
                            } else {
                                AppColors.AccentRed
                            },
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isPlaying && !isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying && !isPaused) "暂停" else "播放",
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = content,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 6.dp)
                        .then(
                            if (onEdit != null && !isProcessing) {
                                Modifier.clickable { onEdit(item) }
                            } else {
                                Modifier
                            }
                        ),
                    color = if (isProcessing) AppColors.TextSecondary else AppColors.TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    maxLines = Int.MAX_VALUE,
                    overflow = TextOverflow.Visible
                )
            }
        }
    }
}
