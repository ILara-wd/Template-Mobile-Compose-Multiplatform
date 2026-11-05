package com.invex.one.template.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Vuelve+ only ships a light theme in Figma, and dynamic color is off so brand colors are respected.
private val VuelveColorScheme = lightColorScheme(
    primary = VuelvePrimary,
    onPrimary = Color.White,
    primaryContainer = VuelvePrimaryContainer,
    onPrimaryContainer = VuelveOnPrimaryContainer,
    secondary = VuelveSuccess,
    onSecondary = Color.White,
    tertiary = VuelveMint,
    background = VuelveBackground,
    onBackground = VuelveOnSurface,
    surface = VuelveSurface,
    onSurface = VuelveOnSurface,
    onSurfaceVariant = VuelveOnSurfaceVariant,
    surfaceContainerLow = VuelveSurfaceContainerLow,
    surfaceContainer = VuelveSurfaceContainer,
    outline = VuelveOutline,
    outlineVariant = VuelveOutlineVariant,
)

private val VuelveShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
)

@Composable
fun TemplateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VuelveColorScheme,
        typography = Typography,
        shapes = VuelveShapes,
        content = content,
    )
}
