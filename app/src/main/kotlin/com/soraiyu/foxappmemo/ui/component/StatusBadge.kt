package com.soraiyu.foxappmemo.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soraiyu.foxappmemo.data.entity.AppStatus
import com.soraiyu.foxappmemo.ui.theme.StatusColorAvoid
import com.soraiyu.foxappmemo.ui.theme.StatusColorBlacklist
import com.soraiyu.foxappmemo.ui.theme.StatusColorMain
import com.soraiyu.foxappmemo.ui.theme.StatusColorReconsider
import com.soraiyu.foxappmemo.ui.theme.StatusColorTrying

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val appStatus = AppStatus.fromLabel(status)
    val color = statusColor(appStatus)
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
    ) {
        Text(
            text = appStatus.label.uppercase(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        )
    }
}

private fun statusColor(status: AppStatus): Color = when (status) {
    AppStatus.TRYING -> StatusColorTrying
    AppStatus.MAIN -> StatusColorMain
    AppStatus.AVOID -> StatusColorAvoid
    AppStatus.BLACKLIST -> StatusColorBlacklist
    AppStatus.RECONSIDER -> StatusColorReconsider
}
