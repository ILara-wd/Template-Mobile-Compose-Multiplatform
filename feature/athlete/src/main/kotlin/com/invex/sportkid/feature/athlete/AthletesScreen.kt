package com.invex.sportkid.feature.athlete

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkAvatar
import com.invex.sportkid.core.designsystem.component.SkButtonSize
import com.invex.sportkid.core.designsystem.component.SkCard
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkLinearProgress
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkTonalButton
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full
import com.invex.sportkid.core.ui.AthleteDot
import com.invex.sportkid.core.ui.BeltBadge
import com.invex.sportkid.core.ui.SportTag
import com.invex.sportkid.core.ui.ageOn
import com.invex.sportkid.core.ui.colors
import com.invex.sportkid.core.ui.daysFrom
import com.invex.sportkid.core.ui.iconRes
import com.invex.sportkid.core.ui.shortLabel
import com.invex.sportkid.core.ui.shortLabelRes
import java.time.LocalDate
import com.invex.sportkid.core.designsystem.R as DsR
import com.invex.sportkid.core.ui.R as UiR

/**
 * P08. Deportistas: encabezado con el conteo de hijos, una card por hijo (O12), el botón
 * "Agregar deportista" y el estado de sincronización familiar.
 *
 * Contenido de la plantilla T1: la top bar y la bottom nav las pone `app`.
 */
@Composable
fun AthletesScreen(
    onOpenProfile: (AthleteProfile) -> Unit,
    onAddAthlete: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    athletes: List<AthleteProfile> = remember(today) { AthleteSamples.athletes(today) },
    familyName: String = AthleteSamples.FAMILY,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = SportKidTheme.spacing.lg, end = SportKidTheme.spacing.lg, top = SportKidTheme.spacing.sm, bottom = SportKidTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg),
    ) {
        item(key = "header") { Header(athletes.size) }
        items(athletes, key = AthleteProfile::id) { athlete ->
            AthleteCard(athlete, today, onClick = { onOpenProfile(athlete) })
        }
        item(key = "add") {
            SkTonalButton(
                text = stringResource(R.string.athletes_add),
                onClick = onAddAthlete,
                icon = painterResource(R.drawable.ic_athletes_add),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                size = SkButtonSize.Medium,
                shape = MaterialTheme.shapes.full,
                modifier = Modifier.fillMaxWidth().padding(top = SportKidTheme.spacing.sm),
            )
        }
        item(key = "sync") { FamilySyncBanner(familyName) }
    }
}

@Composable
private fun Header(count: Int) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.athletes_title),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineSmall,
                color = colors.onSurface,
            )
            Text(stringResource(R.string.athletes_subtitle), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        SkPill(
            pluralStringResource(R.plurals.athletes_active_count, count, count),
            containerColor = colors.secondaryContainer,
            contentColor = colors.onSecondaryContainer,
            icon = painterResource(R.drawable.ic_athletes_count),
            textStyle = MaterialTheme.typography.labelMedium,
            spacing = 6.dp,
            shadowElevation = SkElevation.level1,
            contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.md, vertical = 6.dp),
        )
    }
}

/** O12. Card de deportista: identidad, medallas, deportes, cinta, estilos y próximo compromiso. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AthleteCard(athlete: AthleteProfile, today: LocalDate, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    SkCard(
        shape = MaterialTheme.shapes.medium,
        accentColor = athlete.color(),
        onClick = onClick,
    ) {
        Column(Modifier.padding(start = SportKidTheme.spacing.xs), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarWithSport(athlete)
                Spacer(Modifier.width(SportKidTheme.spacing.md))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            athlete.fullName,
                            modifier = Modifier.weight(1f, fill = false),
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.width(SportKidTheme.spacing.sm))
                        SkPill(
                            stringResource(R.string.athletes_age, athlete.birthDate.ageOn(today)),
                            containerColor = colors.surfaceContainer,
                            contentColor = colors.onSurfaceVariant,
                            contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                        )
                    }
                    MedalStrip(athlete)
                }
                IconButton(
                    onClick = onClick,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = colors.surfaceContainerLow),
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        painterResource(DsR.drawable.sk_ic_chevron_right),
                        contentDescription = stringResource(R.string.athletes_open_profile, athlete.firstName),
                        tint = colors.onSurfaceVariant,
                    )
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
                athlete.sports.forEach { SportTag(it) }
            }
            athlete.tkd?.let { BeltStatus(it) }
            athlete.swim?.let { SwimProgressPanel(it) }
            athlete.nextCommitment?.let { NextCommitmentRow(it, today) }
        }
    }
}

/** Avatar de 56dp con anillo del color del hijo y su deporte principal en un círculo de 24dp. */
@Composable
private fun AvatarWithSport(athlete: AthleteProfile) {
    val ring = athlete.color()
    SkAvatar(
        photo = athlete.avatarRes?.let { painterResource(it) },
        name = athlete.fullName,
        size = 56.dp,
        ringColor = ring,
    ) {
        athlete.sports.firstOrNull()?.let { sport ->
            SkIconCircle(
                icon = painterResource(sport.iconRes),
                contentDescription = null,
                size = 24.dp,
                containerColor = ring,
                iconTint = Color.White,
                shadowElevation = SkElevation.level1,
                modifier = Modifier.align(Alignment.BottomEnd).offset(4.dp, 4.dp),
            )
        }
    }
}

/** "3 oros • 1 plata" o "2 oros • Debutante del mes". */
@Composable
private fun MedalStrip(athlete: AthleteProfile) {
    val sport = SportKidTheme.sportColors
    val parts = buildList<@Composable () -> Unit> {
        val m = athlete.medals
        if (m.gold > 0) add { MedalCount(sport.medalGold, pluralStringResource(R.plurals.athletes_gold, m.gold, m.gold)) }
        if (m.silver > 0) add { MedalCount(sport.medalSilver, pluralStringResource(R.plurals.athletes_silver, m.silver, m.silver)) }
        if (m.bronze > 0) add { MedalCount(sport.medalBronze, pluralStringResource(R.plurals.athletes_bronze, m.bronze, m.bronze)) }
        athlete.highlight?.let { add { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        parts.forEachIndexed { index, part ->
            if (index > 0) AthleteDot(MaterialTheme.colorScheme.outlineVariant, size = 4.dp)
            part()
        }
    }
}

@Composable
private fun MedalCount(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
        Icon(painterResource(UiR.drawable.ic_medal), contentDescription = null, tint = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun BeltStatus(tkd: TkdProfile) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.medium)
            .padding(SportKidTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BeltBadge(tkd.belt)
        Spacer(Modifier.width(10.dp))
        Text(
            stringResource(UiR.string.belt_name, stringResource(tkd.belt.shortLabelRes())),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurface,
        )
        Icon(painterResource(R.drawable.ic_athletes_level), contentDescription = null, tint = SportKidTheme.sportColors.success)
        Spacer(Modifier.width(SportKidTheme.spacing.xs))
        Text(tkd.levelLabel, style = MaterialTheme.typography.labelSmall, color = SportKidTheme.sportColors.success)
    }
}

/** Progreso de los 5 estilos de natación. */
@Composable
internal fun SwimProgressPanel(swim: SwimProgress, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val swimColor = SportKidTheme.sportColors.swimming
    val styles = buildString {
        append(swim.mastered.joinToString(", "))
        swim.inProgress?.let { append(" · ").append(stringResource(R.string.athletes_swim_in_progress, it)) }
    }
    Column(
        modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.medium)
            .padding(SportKidTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(com.invex.sportkid.core.model.Sport.SWIMMING.iconRes), contentDescription = null, modifier = Modifier.size(14.dp), tint = swimColor)
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.athletes_swim_progress), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = colors.onSurface)
            Text(
                stringResource(R.string.athletes_swim_mastered, swim.mastered.size, swim.total),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
        SkLinearProgress(progress = swim.fraction, color = swimColor, trackColor = colors.surfaceContainerHigh)
        Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                styles,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                stringResource(R.string.athletes_percent, (swim.fraction * 100).toInt()),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                color = swimColor,
            )
        }
    }
}

/** "En 4 días", "Mañana", "Hoy" o `null` si ya pasó. */
@Composable
private fun countdownLabel(date: LocalDate, today: LocalDate): String? = when (val days = date.daysFrom(today)) {
    0L -> stringResource(R.string.athletes_today)
    1L -> stringResource(R.string.athletes_tomorrow)
    in 2L..Long.MAX_VALUE -> stringResource(R.string.athletes_in_days, days.toInt())
    else -> null
}

@Composable
private fun NextCommitmentRow(commitment: NextCommitment, today: LocalDate) {
    val colors = MaterialTheme.colorScheme
    val sportColors = commitment.sport.colors()
    if (commitment.emphasized) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(colors.tertiaryContainer.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                .padding(horizontal = SportKidTheme.spacing.md, vertical = SportKidTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(R.drawable.ic_athletes_event), contentDescription = null, tint = colors.tertiary)
            Spacer(Modifier.width(SportKidTheme.spacing.sm))
            Text(
                "${commitment.title} · ${commitment.date.shortLabel()}",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onTertiaryContainer,
            )
            countdownLabel(commitment.date, today)?.let {
                SkPill(
                    it,
                    containerColor = colors.surfaceContainerLowest.copy(alpha = 0.6f),
                    contentColor = colors.tertiary,
                    emphasized = true,
                    contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                )
            }
        }
    } else {
        Row(
            Modifier
                .fillMaxWidth()
                .background(colors.surfaceContainerLow, MaterialTheme.shapes.small)
                .padding(horizontal = SportKidTheme.spacing.md, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkIconCircle(
                icon = painterResource(commitment.sport.iconRes),
                contentDescription = null,
                size = 32.dp,
                containerColor = sportColors.container,
                iconTint = sportColors.content,
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(commitment.title, style = MaterialTheme.typography.labelSmall, color = colors.onSurface)
                Text(
                    listOfNotNull(commitment.date.shortLabel(), commitment.place).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                    color = colors.onSurfaceVariant,
                )
            }
            commitment.badge?.let {
                SkPill(
                    it,
                    containerColor = sportColors.container,
                    contentColor = sportColors.onContainer,
                    emphasized = true,
                    contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun FamilySyncBanner(familyName: String) {
    val colors = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = colors.surfaceContainer) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            SkIconCircle(
                icon = painterResource(R.drawable.ic_athletes_sync),
                contentDescription = null,
                containerColor = colors.surfaceContainerLowest,
                iconTint = colors.primary,
            )
            Spacer(Modifier.width(SportKidTheme.spacing.md))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.athletes_family_synced, familyName), style = MaterialTheme.typography.labelMedium, color = colors.onSurface)
                    Spacer(Modifier.width(6.dp))
                    AthleteDot(SportKidTheme.sportColors.success)
                }
                Text(stringResource(R.string.athletes_family_synced_detail), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }
}

@SkPreviews
@Composable
private fun AthletesScreenPreview() = SportKidTheme {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Box { AthletesScreen(onOpenProfile = {}, onAddAthlete = {}, today = LocalDate.of(2025, 11, 10)) }
    }
}
