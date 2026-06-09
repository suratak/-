package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CustomDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    secondary = EmeraldSecondary,
    tertiary = AmberAccent,
    background = SlateBackground,
    surface = SlateSurface,
    onPrimary = SlateBackground,
    onSecondary = SlateBackground,
    onBackground = AlabasterBackground,
    onSurface = AlabasterBackground,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = AlabasterBackground,
    error = CrimsonError
)

private val CustomLightColorScheme = lightColorScheme(
    primary = ForestPrimary,
    secondary = ForestSecondary,
    tertiary = AmberAccent,
    background = AlabasterBackground,
    surface = AlabasterSurface,
    onPrimary = AlabasterSurface,
    onSecondary = AlabasterSurface,
    onBackground = SlateBackground,
    onSurface = SlateBackground,
    surfaceVariant = AlabasterSurfaceVariant,
    onSurfaceVariant = SlateBackground,
    error = CoralError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CustomDarkColorScheme else CustomLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
