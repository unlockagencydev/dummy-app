package com.dosemate.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealContainer,
    onPrimaryContainer = TealDark,
    secondary = SoftCoral,
    onSecondary = Color.White,
    secondaryContainer = SoftCoralContainer,
    onSecondaryContainer = SoftCoral,
    tertiary = EveningIconBg,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = InactiveButton,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderSubtle
)

@Composable
fun DoseMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Design is light-first; keep a single light scheme matching screenshots
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = DoseMateTypography,
        content = content
    )
}
