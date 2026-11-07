package com.invex.sportkid.core.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full
import com.invex.sportkid.core.model.MedalResult

/** Colores de un resultado: [accent] (icono, acento lateral) y [container] (fondo del icono o badge). */
@Immutable
data class MedalColors(val accent: Color, val container: Color, val content: Color)

@Composable
fun MedalResult.colors(): MedalColors {
    val sport = SportKidTheme.sportColors
    val scheme = MaterialTheme.colorScheme
    fun tinted(c: Color) = MedalColors(c, c.copy(alpha = 0.15f).compositeOver(scheme.surfaceContainerLowest), scheme.onSurface)
    return when (this) {
        MedalResult.GOLD -> tinted(sport.medalGold)
        MedalResult.SILVER -> tinted(sport.medalSilver)
        MedalResult.BRONZE -> tinted(sport.medalBronze)
        MedalResult.PARTICIPATION -> MedalColors(scheme.outlineVariant, scheme.secondaryContainer, scheme.onSecondaryContainer)
        MedalResult.PENDING -> MedalColors(scheme.primary, scheme.surfaceContainerLowest, scheme.primary)
    }
}

@StringRes
fun MedalResult.labelRes(): Int = when (this) {
    MedalResult.GOLD -> R.string.medal_gold
    MedalResult.SILVER -> R.string.medal_silver
    MedalResult.BRONZE -> R.string.medal_bronze
    MedalResult.PARTICIPATION -> R.string.medal_participation
    MedalResult.PENDING -> R.string.medal_pending
}

@StringRes
private fun MedalResult.shortLabelRes(): Int = when (this) {
    MedalResult.PARTICIPATION -> R.string.medal_short_participation
    MedalResult.PENDING -> R.string.medal_short_pending
    else -> labelRes()
}

@get:DrawableRes
val MedalResult.iconRes: Int
    get() = when (this) {
        MedalResult.GOLD, MedalResult.SILVER, MedalResult.BRONZE -> R.drawable.ic_medal_large
        MedalResult.PARTICIPATION -> R.drawable.ic_result_participation
        MedalResult.PENDING -> R.drawable.ic_result_pending
    }

/** M4. Badge de medalla: icono coloreado y etiqueta sobre el contenedor del resultado. */
@Composable
fun MedalBadge(result: MedalResult, modifier: Modifier = Modifier) {
    val colors = result.colors()
    val podium = result == MedalResult.GOLD || result == MedalResult.SILVER || result == MedalResult.BRONZE
    SkPill(
        text = stringResource(result.labelRes()),
        modifier = modifier,
        containerColor = if (result == MedalResult.PARTICIPATION) MaterialTheme.colorScheme.surfaceContainer else colors.container,
        contentColor = if (result == MedalResult.PARTICIPATION) MaterialTheme.colorScheme.onSurfaceVariant else colors.content,
        emphasized = podium,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        leading = {
            Icon(
                painterResource(if (podium) R.drawable.ic_medal else result.iconRes),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (podium) colors.accent else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

/** Selector del resultado de un torneo: 5 tarjetas (oro, plata, bronce, diploma y pendiente). */
@Composable
fun MedalResultPicker(selected: MedalResult, onSelect: (MedalResult) -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        MedalResult.entries.forEach { result ->
            val isSelected = result == selected
            val colors = result.colors()
            Column(
                Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(if (isSelected) scheme.primaryContainer else scheme.surfaceContainer)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(result) })
                    .padding(vertical = SportKidTheme.spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs),
            ) {
                Surface(Modifier.size(36.dp), shape = MaterialTheme.shapes.full, color = colors.container, shadowElevation = SkElevation.level1) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painterResource(if (result == MedalResult.PARTICIPATION) R.drawable.ic_medal else result.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = when (result) {
                                MedalResult.PARTICIPATION -> scheme.secondary
                                MedalResult.PENDING -> scheme.primary
                                else -> colors.accent
                            },
                        )
                    }
                }
                Text(
                    stringResource(result.shortLabelRes()),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isSelected) scheme.primary else scheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@SkPreviews
@Composable
private fun MedalUiPreview() = SportKidTheme {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MedalResultPicker(selected = MedalResult.PENDING, onSelect = {})
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MedalBadge(MedalResult.GOLD)
            MedalBadge(MedalResult.SILVER)
            MedalBadge(MedalResult.PARTICIPATION)
        }
    }
}
