package com.invex.sportkid.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme

/** Ancho de la barra de acento lateral en cards (Figma: 6dp). */
val SkCardAccentWidth: Dp = 6.dp

/**
 * Card base: `surfaceContainerLowest` sobre `surface`, elevación tonal nivel 1 y barra de
 * acento lateral opcional (color del deporte o de la acción).
 */
@Composable
fun SkCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    accentColor: Color? = null,
    accentWidth: Dp = SkCardAccentWidth,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(SportKidTheme.spacing.lg),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val body: @Composable () -> Unit = {
        Column(
            Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (accentColor != null) drawRect(accentColor, size = Size(accentWidth.toPx(), size.height))
                }
                .padding(contentPadding),
            content = content,
        )
    }
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = colors.surfaceContainerLowest,
            shadowElevation = SkElevation.card,
            content = body,
        )
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = colors.surfaceContainerLowest,
            shadowElevation = SkElevation.card,
            content = body,
        )
    }
}

@SkPreviews
@Composable
private fun SkCardPreview() = SportKidTheme {
    SkCard(Modifier.padding(16.dp), accentColor = MaterialTheme.colorScheme.primary, onClick = {}) {
        Text("Crear deportista", style = MaterialTheme.typography.titleLarge)
        Text("Registra a tu hijo y sus deportes.", style = MaterialTheme.typography.bodyMedium)
    }
}
