package com.rtneg.foxappmemo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = FoxOrange,
    onPrimary = FoxCream,
    primaryContainer = FoxOrangeLight,
    onPrimaryContainer = FoxOrangeDark,
    secondary = FoxAmber,
    onSecondary = FoxBrownDark,
    secondaryContainer = FoxAmberLight,
    onSecondaryContainer = FoxAmberDark,
    tertiary = FoxBrown,
    onTertiary = FoxCream,
    tertiaryContainer = FoxBrownLight,
    onTertiaryContainer = FoxBrownDark,
    background = FoxCream,
    surface = FoxCream,
)

private val DarkColorScheme = darkColorScheme(
    primary = FoxOrangeLight,
    onPrimary = FoxOrangeDark,
    primaryContainer = FoxOrangeDark,
    onPrimaryContainer = FoxOrangeLight,
    secondary = FoxAmberLight,
    onSecondary = FoxAmberDark,
    secondaryContainer = FoxAmberDark,
    onSecondaryContainer = FoxAmberLight,
    tertiary = FoxBrownLight,
    onTertiary = FoxBrownDark,
    tertiaryContainer = FoxBrownDark,
    onTertiaryContainer = FoxBrownLight,
)

@Composable
fun FoxAppMemoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
