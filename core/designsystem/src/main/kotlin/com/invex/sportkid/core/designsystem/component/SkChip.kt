package com.invex.sportkid.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full

/**
 * Chip de selección (filtros de la agenda, disciplina, recordatorio, sugerencias de lugar).
 * Seleccionado: contenedor sólido. Sin seleccionar: [containerColor], con sombra si es una
 * superficie blanca sobre `surface`.
 *
 * [leading] se dibuja antes del [icon] (punto de color, avatar).
 */
@Composable
fun SkChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    iconTint: Color? = null,
    iconSize: Dp? = null,
    leading: (@Composable () -> Unit)? = null,
    height: Dp = 32.dp,
    horizontalPadding: Dp = SportKidTheme.spacing.md,
    shape: Shape = MaterialTheme.shapes.full,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primary,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shadowElevation: Dp = if (containerColor == MaterialTheme.colorScheme.surfaceContainerLowest) SkElevation.level1 else 0.dp,
) {
    val content = if (selected) selectedContentColor else contentColor
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier.height(height),
        shape = shape,
        color = if (selected) selectedContainerColor else containerColor,
        contentColor = content,
        shadowElevation = if (selected) SkElevation.level1 else shadowElevation,
    ) {
        Row(
            Modifier.padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading?.invoke()
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = if (iconSize != null) Modifier.size(iconSize) else Modifier,
                    tint = if (selected) content else iconTint ?: content,
                )
            }
            Text(text, style = textStyle, maxLines = 1)
        }
    }
}

/**
 * M14. Control segmentado tipo pill (Figma: "Mes / Lista" en la agenda). El segmento activo es
 * una pill blanca elevada con texto `primary`.
 */
@Composable
fun SkSegmentedControl(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    icons: List<Painter?> = emptyList(),
    selectedContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    selectedContentColor: Color = MaterialTheme.colorScheme.primary,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.full,
        color = colors.surfaceContainer,
        shadowElevation = SkElevation.level1,
    ) {
        Row(Modifier.padding(SportKidTheme.spacing.xs).selectableGroup()) {
            labels.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(MaterialTheme.shapes.full)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) }),
                    shape = MaterialTheme.shapes.full,
                    color = if (selected) selectedContainerColor else Color.Transparent,
                    contentColor = if (selected) selectedContentColor else colors.onSurfaceVariant,
                    shadowElevation = if (selected) SkElevation.level1 else 0.dp,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        icons.getOrNull(index)?.let { Icon(it, contentDescription = null) }
                        Text(label, style = textStyle, maxLines = 1)
                    }
                }
            }
        }
    }
}

@SkPreviews
@Composable
private fun SkChipPreview() = SportKidTheme {
    androidx.compose.foundation.layout.Column(
        Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SkSegmentedControl(labels = listOf("Mes", "Lista"), selectedIndex = 0, onSelect = {})
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkChip("Todos", selected = true, onClick = {})
            SkChip("Mateo", selected = false, onClick = {}, leading = {
                Surface(Modifier.size(10.dp), shape = MaterialTheme.shapes.full, color = Color(0xFF1E88E5)) {}
            })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkChip(
                "1 día antes",
                selected = true,
                onClick = {},
                height = 36.dp,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
            )
            SkChip(
                "Ninguno",
                selected = false,
                onClick = {},
                height = 36.dp,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}
