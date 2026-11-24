package com.snapnote.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snapnote.app.model.FeedingItem
@Composable
fun FeedingListItem(
    item: FeedingItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .floatingListCard()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.CardBackground)
            .clickableWithPressScale(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatListTime(item.timestamp),
            color = AppColors.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.width(64.dp)
        )
        if (item.amountMl != null) {
            Text(
                text = "${item.amountMl}ml",
                color = AppColors.AccentRed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(56.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(56.dp))
        }
        Text(
            text = item.note.ifBlank { "（无描述）" },
            color = AppColors.TextPrimary,
            fontSize = 15.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
