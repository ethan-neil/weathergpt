package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Sophisticated Dark Color Scheme (Primary design theme)
private val SophisticatedDarkColorScheme = darkColorScheme(
    primary = SophisticatedIceBluePrimary,
    onPrimary = SophisticatedOnPrimaryNavy,
    primaryContainer = SophisticatedPrimaryContainer,
    onPrimaryContainer = SophisticatedOnPrimaryContainer,
    secondary = SophisticatedCyanSecondary,
    onSecondary = Color(0xFF00363C),
    secondaryContainer = SophisticatedSecondaryContainer,
    onSecondaryContainer = SophisticatedOnSecondaryContainer,
    tertiary = SophisticatedAmberTertiary,
    onTertiary = Color(0xFF432C00),
    tertiaryContainer = SophisticatedTertiaryContainer,
    onTertiaryContainer = SophisticatedOnTertiaryContainer,
    background = SophisticatedDarkBackground,
    onBackground = SophisticatedTextPrimary,
    surface = SophisticatedDarkSurface,
    onSurface = SophisticatedTextPrimary,
    surfaceVariant = SophisticatedDarkSurfaceVariant,
    onSurfaceVariant = SophisticatedTextSecondary,
    outline = SophisticatedDarkBorder,
    outlineVariant = SophisticatedDarkBorder.copy(alpha = 0.5f)
)

private val LightColorScheme = lightColorScheme(
    primary = SkyBluePrimary,
    onPrimary = OnSkyBluePrimary,
    primaryContainer = SkyBluePrimaryContainer,
    onPrimaryContainer = OnSkyBluePrimaryContainer,
    secondary = WeatherCyanSecondary,
    onSecondary = OnWeatherCyanSecondary,
    secondaryContainer = WeatherCyanSecondaryContainer,
    onSecondaryContainer = OnWeatherCyanSecondaryContainer,
    tertiary = StormAmberTertiary,
    onTertiary = OnStormAmberTertiary,
    tertiaryContainer = StormAmberTertiaryContainer,
    onTertiaryContainer = OnStormAmberTertiaryContainer,
    background = WeatherBackgroundLight,
    onBackground = WeatherTextPrimaryLight,
    surface = WeatherSurfaceLight,
    onSurface = WeatherTextPrimaryLight,
    surfaceVariant = WeatherSurfaceVariantLight,
    onSurfaceVariant = WeatherTextSecondaryLight,
    outline = Color(0xFFC4C7D0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Sophisticated Dark
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SophisticatedDarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

