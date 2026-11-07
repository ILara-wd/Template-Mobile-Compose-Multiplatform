package com.invex.sportkid.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.theme.SkDimens

/**
 * Icono centrado en un círculo de color (Figma: tiles de 48/40/28dp).
 * Con `iconTint = Color.Unspecified` el icono conserva sus colores originales.
 */
@Composable
fun SkIconCircle(
    icon: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = SkDimens.iconTile,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    iconTint: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
    shadowElevation: Dp = 0.dp,
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = containerColor,
        shadowElevation = shadowElevation,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription, tint = iconTint)
        }
    }
}

private fun androidx.compose.material3.ColorScheme.contentColorFor(container: Color): Color = when (container) {
    primary -> onPrimary
    primaryContainer -> onPrimaryContainer
    secondaryContainer -> onSecondaryContainer
    tertiary -> onTertiary
    tertiaryContainer -> onTertiaryContainer
    else -> onSurfaceVariant
}
