package com.invex.one.demo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CierreProColorScheme = lightColorScheme(
    primary = CpPrimary,
    onPrimary = Color.White,
    primaryContainer = CpPrimaryContainer,
    onPrimaryContainer = Color.White,
    secondaryContainer = CpSecondaryContainer,
    onSecondaryContainer = CpPrimary,
    background = CpBackground,
    onBackground = CpOnSurface,
    surface = CpSurface,
    onSurface = CpOnSurface,
    onSurfaceVariant = CpOnSurfaceVariant,
    surfaceContainer = CpSurfaceContainer,
    surfaceContainerHigh = CpSurfaceContainerHigh,
    surfaceContainerHighest = CpSurfaceContainerHighest,
)

/**
 * Tema CierrePro. Solo modo claro y sin color dinámico para respetar la marca
 * definida en el UI Kit.
 */
@Composable
fun DemoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CierreProColorScheme,
        typography = Typography,
        content = content,
    )
}
