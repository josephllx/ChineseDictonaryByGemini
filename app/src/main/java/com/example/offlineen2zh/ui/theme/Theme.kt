package com.example.offlineen2zh.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = OnPrimaryBlue,
    primaryContainer = PrimaryBlueContainer,
    onPrimaryContainer = OnPrimaryBlueContainer,
    secondary = SecondaryTeal,
    onSecondary = OnSecondaryTeal,
    secondaryContainer = SecondaryTealContainer,
    onSecondaryContainer = OnSecondaryTealContainer,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = BackgroundLight,
    onSurface = OnBackgroundLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueDark,
    onPrimary = OnPrimaryBlueDark,
    primaryContainer = PrimaryBlueContainerDark,
    onPrimaryContainer = OnPrimaryBlueContainerDark,
    secondary = SecondaryTealDark,
    onSecondary = OnSecondaryTealDark,
    secondaryContainer = SecondaryTealContainerDark,
    onSecondaryContainer = OnSecondaryTealContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = BackgroundDark,
    onSurface = OnBackgroundDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark
)

@Composable
fun OfflineDictionaryTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
