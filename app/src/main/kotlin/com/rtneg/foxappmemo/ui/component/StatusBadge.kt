package com.rtneg.foxappmemo.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rtneg.foxappmemo.data.entity.AppStatus
import com.rtneg.foxappmemo.ui.theme.StatusColorAvoid
import com.rtneg.foxappmemo.ui.theme.StatusColorBlacklist
import com.rtneg.foxappmemo.ui.theme.StatusColorMain
import com.rtneg.foxappmemo.ui.theme.StatusColorOngoing
import com.rtneg.foxappmemo.ui.theme.StatusColorReconsider
import com.rtneg.foxappmemo.ui.theme.StatusColorTrying

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
            text = stringResource(appStatus.labelResId).uppercase(),
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
    AppStatus.ONGOING -> StatusColorOngoing
    AppStatus.MAIN -> StatusColorMain
    AppStatus.AVOID -> StatusColorAvoid
    AppStatus.BLACKLIST -> StatusColorBlacklist
    AppStatus.RECONSIDER -> StatusColorReconsider
}
