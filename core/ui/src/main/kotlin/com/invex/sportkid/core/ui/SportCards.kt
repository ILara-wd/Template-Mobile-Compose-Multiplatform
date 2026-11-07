package com.invex.sportkid.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkCard
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkDimens
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.Sport

/**
 * Opción de deporte en selección múltiple (Figma: "Deportes que practica").
 * Seleccionado: card blanca con acento de 4dp y check del color del deporte.
 * No seleccionado: fondo `surfaceContainerLow` atenuado e icono gris con "+".
 */
@Composable
fun SportToggleCard(
    sport: Sport,
    subtitle: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val sportColor = sport.colors().content
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (selected) 1f else 0.8f),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) colors.surfaceContainerLowest else colors.surfaceContainerLow,
    ) {
        Row(
            Modifier
                .toggleable(value = selected, role = Role.Checkbox, onValueChange = onSelectedChange)
                .drawBehind {
                    if (selected) drawRect(sportColor, size = Size(SkDimens.sportAccentBar.toPx(), size.height))
                }
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkIconCircle(
                icon = painterResource(sport.iconRes),
                contentDescription = null,
                containerColor = if (selected) sportColor else colors.surfaceContainerHighest,
                iconTint = if (selected) colors.surfaceContainerLowest else colors.onSurfaceVariant,
                shadowElevation = if (selected) SkElevation.level1 else 0.dp,
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(sport.labelRes), style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            SkIconCircle(
                icon = painterResource(if (selected) R.drawable.ic_sport_chip_check else R.drawable.ic_sport_chip_add),
                contentDescription = null,
                size = 28.dp,
                containerColor = if (selected) sportColor else colors.surfaceContainerHighest,
                iconTint = if (selected) colors.surfaceContainerLowest else colors.outline,
                shadowElevation = if (selected) SkElevation.level1 else 0.dp,
            )
        }
    }
}

/**
 * Sección de datos específicos de un deporte (Figma: "Datos de Taekwondo").
 * Acento lateral de 6dp, punto de color, título, pill "Activo" y campos en [content].
 */
@Composable
fun SportSectionCard(
    sport: Sport,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val sportColor = sport.colors().content
    SkCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        accentColor = sportColor,
        contentPadding = PaddingValues(start = 24.dp, end = 16.dp, top = 32.dp, bottom = 16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(12.dp)
                        .background(sportColor, CircleShape),
                )
                Text(
                    title,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = SportKidTheme.spacing.sm),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                )
                SkPill(
                    stringResource(R.string.sport_status_active),
                    containerColor = colors.surfaceContainerHigh,
                    contentColor = colors.onSurfaceVariant,
                )
            }
            content()
        }
    }
}

@SkPreviews
@Composable
private fun SportCardsPreview() = SportKidTheme {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SportToggleCard(Sport.TAEKWONDO, "Artes marciales y combate", selected = true, onSelectedChange = {})
        SportToggleCard(Sport.ATHLETICS, "Pista, velocidad y fondo", selected = false, onSelectedChange = {})
        SportSectionCard(Sport.SWIMMING, "Datos de Natación") {
            Text("Contenido", modifier = Modifier.fillMaxWidth())
        }
    }
}
