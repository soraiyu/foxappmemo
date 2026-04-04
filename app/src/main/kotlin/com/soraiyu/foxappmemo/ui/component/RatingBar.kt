package com.soraiyu.foxappmemo.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.soraiyu.foxappmemo.data.entity.AppRating

/** Interactive 3-option rating selector. Tapping the selected option clears the rating. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RatingSelector(
    rating: Int?,
    onRatingChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppRating.entries.forEach { appRating ->
            val isSelected = rating == appRating.value
            FilterChip(
                selected = isSelected,
                onClick = { onRatingChange(if (isSelected) null else appRating.value) },
                label = { Text(stringResource(appRating.labelResId)) },
            )
        }
    }
}

/** Display-only rating label showing the current rating, or nothing when unrated. */
@Composable
fun RatingLabel(
    rating: Int?,
    modifier: Modifier = Modifier,
) {
    val appRating = AppRating.fromValue(rating)
    if (appRating != null) {
        Text(
            text = stringResource(appRating.labelResId),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = modifier,
        )
    }
}
