package com.invex.sportkid.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SportKidTheme

private val FieldHeight = 56.dp

/** Etiqueta superior de campo: icono `primary` opcional y texto 12sp SemiBold. */
@Composable
fun SkFieldLabel(
    text: String,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun FieldScaffold(
    label: String?,
    labelIcon: Painter?,
    helperText: String?,
    modifier: Modifier,
    field: @Composable () -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (label != null) SkFieldLabel(label, icon = labelIcon)
        field()
        if (helperText != null) {
            Text(
                helperText,
                modifier = Modifier.padding(start = SportKidTheme.spacing.xs),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Campo relleno de 56dp con etiqueta arriba (Figma: "Nombre del deportista").
 * Fondo `surfaceContainerHigh`, radio `shape.medium`.
 */
@Composable
fun SkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    labelIcon: Painter? = null,
    placeholder: String? = null,
    helperText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    FieldScaffold(label, labelIcon, helperText, modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            keyboardOptions = keyboardOptions,
            decorationBox = { inner ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(FieldHeight)
                        .background(colors.surfaceContainerHigh, MaterialTheme.shapes.medium)
                        .padding(start = SportKidTheme.spacing.lg, end = if (trailing != null) SportKidTheme.spacing.xs else SportKidTheme.spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder != null) {
                            Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                        }
                        inner()
                    }
                    trailing?.invoke()
                }
            },
        )
    }
}

/**
 * Campo de selección de 56dp: muestra el valor elegido y abre un menú o diálogo al tocarlo
 * (Figma: "Cinta actual", "Fecha de nacimiento").
 */
@Composable
fun SkSelectField(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    labelIcon: Painter? = null,
    placeholder: String? = null,
    helperText: String? = null,
    emphasized: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    FieldScaffold(label, labelIcon, helperText, modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(FieldHeight)
                .clip(MaterialTheme.shapes.medium)
                .background(colors.surfaceContainerHigh)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = SportKidTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading?.invoke()
            val showPlaceholder = value.isEmpty() && placeholder != null
            Text(
                if (showPlaceholder) placeholder.orEmpty() else value,
                modifier = Modifier.weight(1f),
                style = if (emphasized && !showPlaceholder) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                color = if (showPlaceholder) colors.onSurfaceVariant else colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            trailing?.invoke()
        }
    }
}

@SkPreviews
@Composable
private fun SkTextFieldPreview() = SportKidTheme {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SkTextField(value = "Mateo Gómez", onValueChange = {}, label = "Nombre del deportista")
        SkTextField(value = "", onValueChange = {}, label = "Escuela / Dojang", placeholder = "Ej. Doyang Central")
        SkSelectField(
            value = "15 de mayo de 2015",
            onClick = {},
            label = "Fecha de nacimiento",
            helperText = "Utilizada para clasificar sus categorías por edad (ej. Sub-10)",
        )
    }
}
