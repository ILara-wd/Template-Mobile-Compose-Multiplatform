package com.invex.sportkid.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.R
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme

/**
 * Grid de colores de selección única (Figma: "Color distintivo para el perfil", 4 columnas).
 *
 * @param labels nombre accesible de cada color, en el mismo orden que [colors].
 */
@Composable
fun SkColorSwatchPicker(
    colors: List<Color>,
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 4,
) {
    require(colors.size == labels.size) { "colors y labels deben tener el mismo tamaño" }
    val gap = SportKidTheme.spacing.md
    Column(modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(gap)) {
        colors.indices.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                row.forEach { index ->
                    Swatch(
                        color = colors[index],
                        label = labels[index],
                        selected = index == selectedIndex,
                        onClick = { onSelect(index) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun Swatch(
    color: Color,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    Surface(
        modifier = modifier
            .scale(if (selected) 1.05f else 1f)
            .height(48.dp)
            .semantics { contentDescription = label }
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = SkElevation.level1,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(28.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        painterResource(R.drawable.sk_ic_swatch_check),
                        contentDescription = null,
                        // Check oscuro sobre colores claros (amarillo) para mantener contraste.
                        tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                    )
                }
            }
        }
    }
}

@SkPreviews
@Composable
private fun SkColorSwatchPickerPreview() = SportKidTheme {
    val palette = SportKidTheme.sportColors.athlete.all
    SkColorSwatchPicker(
        colors = palette,
        labels = palette.indices.map { "Color $it" },
        selectedIndex = 0,
        onSelect = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    )
}
