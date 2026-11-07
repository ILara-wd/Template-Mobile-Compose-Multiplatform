package com.invex.sportkid.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full

/**
 * Etiqueta pill de 11sp con icono opcional (Figma: "Entrenamientos", "Activo", "PASO 1 DE 1").
 *
 * @param overline MAYÚSCULAS con tracking, para etiquetas de sección o marca.
 * @param emphasized texto en Bold (ej. "PASO 1 DE 1", "Azul Royal").
 * @param leading contenido previo al icono, p. ej. un punto de color.
 */
@Composable
fun SkPill(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    icon: Painter? = null,
    iconTint: Color = contentColor,
    overline: Boolean = false,
    emphasized: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
    spacing: Dp = SportKidTheme.spacing.xs,
    shadowElevation: Dp = 0.dp,
    leading: (@Composable () -> Unit)? = null,
) {
    val base = MaterialTheme.typography.labelSmall
    val style = base.copy(
        letterSpacing = if (overline) 0.55.sp else base.letterSpacing,
        fontWeight = if (emphasized) FontWeight.Bold else base.fontWeight,
    )
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.full,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = shadowElevation,
    ) {
        Row(
            Modifier.padding(contentPadding),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading?.invoke()
            if (icon != null) Icon(icon, contentDescription = null, tint = iconTint)
            Text(if (overline) text.uppercase() else text, style = style)
        }
    }
}

@SkPreviews
@Composable
private fun SkPillPreview() = SportKidTheme {
    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SkPill("Entrenamientos")
        SkPill(
            "Primeros pasos",
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            overline = true,
        )
        SkPill(
            "Activo",
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
