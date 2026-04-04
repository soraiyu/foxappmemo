package com.soraiyu.foxappmemo.ui.screen.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.soraiyu.foxappmemo.R
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    @StringRes val titleResId: Int,
    @StringRes val descResId: Int,
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Default.Apps,
        titleResId = R.string.onboarding_title_1,
        descResId = R.string.onboarding_desc_1,
    ),
    OnboardingPage(
        icon = Icons.Default.AddCircle,
        titleResId = R.string.onboarding_title_2,
        descResId = R.string.onboarding_desc_2,
    ),
    OnboardingPage(
        icon = Icons.Default.Star,
        titleResId = R.string.onboarding_title_3,
        descResId = R.string.onboarding_desc_3,
    ),
    OnboardingPage(
        icon = Icons.Default.FilterList,
        titleResId = R.string.onboarding_title_4,
        descResId = R.string.onboarding_desc_4,
    ),
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            OnboardingPageContent(pages[page])
        }

        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(pages.size) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == pagerState.currentPage) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                        ),
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!isLastPage) {
                TextButton(onClick = onComplete) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            Button(
                onClick = {
                    if (isLastPage) {
                        onComplete()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
            ) {
                Text(stringResource(if (isLastPage) R.string.onboarding_start else R.string.onboarding_next))
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = page.icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(page.titleResId),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(page.descResId),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
