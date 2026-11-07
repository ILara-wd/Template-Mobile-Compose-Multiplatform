package com.invex.sportkid.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkDimens
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full

private val LargeButtonHeight = 56.dp

/** Alto del botón primario según el contexto. */
enum class SkButtonSize(internal val height: Dp, internal val horizontalPadding: Dp, internal val iconGap: Dp) {
    /** 40dp: acción "Guardar" en la top bar. */
    Small(40.dp, 16.dp, 8.dp),

    /** 48dp: CTA fijo de formularios de evento. */
    Medium(48.dp, 24.dp, 8.dp),

    /** 56dp: CTA principal (Figma: "Guardar deportista"). */
    Large(LargeButtonHeight, 24.dp, 10.dp),
}

/** Botón primario pill (Figma: "Guardar deportista", "Guardar entrenamiento"). */
@Composable
fun SkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: Painter? = null,
    size: SkButtonSize = SkButtonSize.Large,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(size.height),
        enabled = enabled,
        shape = MaterialTheme.shapes.full,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (size == SkButtonSize.Small) SkElevation.level1 else SkElevation.level3,
        ),
        contentPadding = PaddingValues(horizontal = size.horizontalPadding),
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null)
            Spacer(Modifier.width(size.iconGap))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Botón pill blanco elevado de 56dp con icono a color sin teñir
 * (Figma: "Continuar con Google").
 */
@Composable
fun SkElevatedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(LargeButtonHeight),
        shape = MaterialTheme.shapes.full,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = SkElevation.level2,
    ) {
        Row(
            Modifier.padding(horizontal = SportKidTheme.spacing.xl),
            horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) Icon(icon, contentDescription = null, tint = Color.Unspecified)
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Link de acción secundaria con icono final opcional (Figma: "Explorar como invitado →"). */
@Composable
fun SkTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: Painter? = null,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = SkDimens.minTouchTarget),
        colors = ButtonDefaults.textButtonColors(contentColor = color),
        contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.lg, vertical = SportKidTheme.spacing.xs),
    ) {
        Text(text, style = textStyle)
        if (trailingIcon != null) {
            Spacer(Modifier.width(SportKidTheme.spacing.xs))
            Icon(trailingIcon, contentDescription = null)
        }
    }
}

/**
 * Botón tonal: contenedor de color suave, `shape.medium` por defecto ("botones tonales
 * cuadrados", README §1.4) e icono opcional. Figma: "Compartir (QR)", "Tutores (2)",
 * "Pagar cuota" y, en pill de 48dp, "Agregar deportista".
 */
@Composable
fun SkTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    size: SkButtonSize = SkButtonSize.Small,
    shape: Shape = MaterialTheme.shapes.medium,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(size.height),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = SkElevation.level1),
        contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.lg),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(size.iconGap))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

@SkPreviews
@Composable
private fun SkButtonPreview() = SportKidTheme {
    androidx.compose.foundation.layout.Column(
        Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SkButton("Guardar deportista", onClick = {}, Modifier.fillMaxWidth())
        SkElevatedButton("Continuar con Google", onClick = {}, Modifier.fillMaxWidth())
        SkTextButton("Explorar como invitado", onClick = {})
        SkTonalButton(
            "Compartir (QR)",
            onClick = {},
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
        )
    }
}
