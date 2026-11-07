package com.invex.sportkid.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkAvatar
import com.invex.sportkid.core.designsystem.component.SkChip
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.Sport

/**
 * Encabezado de las vistas por deporte (P09 Taekwondo, P12 Natación): avatar con anillo del
 * color del hijo y el deporte actual en un badge, nombre, edad, subtítulo (federación o club),
 * chips de resumen y las pestañas de los deportes del hijo.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AthleteSportHeader(
    name: String,
    photo: Painter?,
    ringColor: Color,
    age: Int,
    subtitle: String,
    sport: Sport,
    sports: List<Sport>,
    onSelectSport: (Sport) -> Unit,
    modifier: Modifier = Modifier,
    subtitleIcon: Painter? = null,
    chips: @Composable () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    FormCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkAvatar(photo = photo, name = name, size = 64.dp, ringColor = ringColor, ringWidth = 3.dp) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(22.dp)
                        .background(colors.surfaceContainerLowest, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(sport.iconRes), contentDescription = null, modifier = Modifier.size(14.dp), tint = sport.colors().content)
                }
            }
            Spacer(Modifier.width(SportKidTheme.spacing.lg))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    SkPill(
                        stringResource(R.string.athlete_age, age),
                        containerColor = colors.secondaryContainer,
                        contentColor = colors.onSecondaryContainer,
                        contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                    )
                }
                Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (subtitleIcon != null) {
                        Icon(subtitleIcon, contentDescription = null, modifier = Modifier.size(12.dp), tint = colors.primary)
                        Spacer(Modifier.width(SportKidTheme.spacing.xs))
                    }
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                FlowRow(
                    Modifier.padding(top = SportKidTheme.spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs),
                ) { chips() }
            }
        }
        LazyRow(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
            items(sports) { item ->
                SkChip(
                    text = stringResource(item.labelRes),
                    selected = item == sport,
                    onClick = { if (item != sport) onSelectSport(item) },
                    icon = painterResource(item.iconRes),
                    iconSize = 16.dp,
                    height = 36.dp,
                    horizontalPadding = SportKidTheme.spacing.lg,
                    textStyle = MaterialTheme.typography.labelLarge,
                    selectedContainerColor = item.colors().content,
                    selectedContentColor = Color.White,
                    containerColor = colors.surfaceContainerLow,
                    contentColor = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@SkPreviews
@Composable
private fun AthleteSportHeaderPreview() = SportKidTheme {
    AthleteSportHeader(
        name = "Mateo Gómez",
        photo = null,
        ringColor = SportKidTheme.sportColors.athlete.blue,
        age = 9,
        subtitle = "Club Acuático Olímpico",
        sport = Sport.SWIMMING,
        sports = listOf(Sport.TAEKWONDO, Sport.SWIMMING),
        onSelectSport = {},
        modifier = Modifier.padding(16.dp),
    ) {
        SkPill("Cat. Infantil A")
    }
}
