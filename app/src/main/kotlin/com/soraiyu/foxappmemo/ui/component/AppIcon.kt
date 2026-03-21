package com.soraiyu.foxappmemo.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Displays the launcher icon for [packageName] loaded asynchronously via
 * [PackageManager][android.content.pm.PackageManager].
 *
 * While the icon is loading, or when the package is not found on this device,
 * an [Android][Icons.Filled.Android] placeholder icon is shown instead.
 *
 * @param packageName The package whose icon to display.
 * @param size        Side length of the icon in dp (default 40 dp).
 */
@Composable
fun AppIcon(
    packageName: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val context = LocalContext.current
    val icon: ImageBitmap? by produceState<ImageBitmap?>(initialValue = null, packageName) {
        value = withContext(Dispatchers.IO) {
            try {
                context.packageManager
                    .getApplicationIcon(packageName)
                    .toBitmap()
                    .asImageBitmap()
            } catch (_: Exception) {
                null
            }
        }
    }

    val resolvedIcon = icon
    if (resolvedIcon != null) {
        Image(
            bitmap = resolvedIcon,
            contentDescription = null,
            modifier = modifier.size(size),
            contentScale = ContentScale.Fit,
        )
    } else {
        Box(
            modifier = modifier.size(size),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Android,
                contentDescription = null,
                modifier = Modifier.size(size * 0.75f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
