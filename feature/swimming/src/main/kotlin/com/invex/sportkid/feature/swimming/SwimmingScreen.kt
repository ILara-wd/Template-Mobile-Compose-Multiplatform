package com.invex.sportkid.feature.swimming

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.sportkid.core.designsystem.component.SkButton
import com.invex.sportkid.core.designsystem.component.SkButtonSize
import com.invex.sportkid.core.designsystem.component.SkCard
import com.invex.sportkid.core.designsystem.component.SkCircularProgress
import com.invex.sportkid.core.designsystem.component.SkDetailTopBar
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkTextButton
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.metric
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.model.SwimStyle
import com.invex.sportkid.core.model.SwimStyleStatus
import com.invex.sportkid.core.ui.AthleteDot
import com.invex.sportkid.core.ui.AthleteSportHeader
import com.invex.sportkid.core.ui.FormCard
import com.invex.sportkid.core.ui.PaymentStatusChip
import com.invex.sportkid.core.ui.ageOn
import com.invex.sportkid.core.ui.colors
import com.invex.sportkid.core.ui.fullLabel
import com.invex.sportkid.core.ui.shortLabelWithYear
import java.time.LocalDate
import com.invex.sportkid.core.designsystem.R as DsR
import com.invex.sportkid.core.ui.R as UiR

/**
 * P12. Natación de un deportista: resumen, "Registrar nueva competencia", progreso técnico,
 * checklist de estilos (O18), mejores tiempos (O19), competencias y nota del entrenador.
 */
@Composable
fun SwimmingScreen(
    athlete: SwimAthlete,
    onBack: () -> Unit,
    onNewMeet: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    styles: List<StyleProgress> = SwimSamples.styles,
    bestTimes: Map<SwimStyle, Map<Int, SwimTime>> = SwimSamples.bestTimes,
    meets: List<SwimMeet> = SwimSamples.meets,
    coachNote: CoachNote? = SwimSamples.coachNote,
    onSelectSport: (Sport) -> Unit = {},
) {
    val mastered = styles.count { it.status == SwimStyleStatus.MASTERED }
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = { SkDetailTopBar(title = stringResource(R.string.swim_screen_title), subtitle = athlete.fullName, onBack = onBack) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(start = SportKidTheme.spacing.lg, end = SportKidTheme.spacing.lg, top = SportKidTheme.spacing.sm, bottom = SportKidTheme.spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg),
        ) {
            item(key = "header") {
                val colors = MaterialTheme.colorScheme
                AthleteSportHeader(
                    name = athlete.fullName,
                    photo = athlete.photoRes?.let { painterResource(it) },
                    ringColor = athlete.color(),
                    age = athlete.birthDate.ageOn(today),
                    subtitle = athlete.club,
                    subtitleIcon = painterResource(R.drawable.ic_swim_club),
                    sport = Sport.SWIMMING,
                    sports = athlete.sports,
                    onSelectSport = onSelectSport,
                ) {
                    SkPill(stringResource(R.string.swim_category, athlete.category))
                    SkPill(
                        stringResource(R.string.swim_styles_summary, mastered, styles.size),
                        containerColor = colors.surfaceContainer,
                        contentColor = colors.onSurfaceVariant,
                    )
                }
            }
            item(key = "cta") {
                SkButton(
                    stringResource(R.string.swim_new_meet),
                    onClick = onNewMeet,
                    leadingIcon = painterResource(R.drawable.ic_swim_add),
                    size = SkButtonSize.Medium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item(key = "progress") { ProgressCard(styles, coachNote) }
            item(key = "styles") { StylesChecklist(styles) }
            item(key = "times") { BestTimesTable(bestTimes, styles) }
            item(key = "meets") { MeetList(meets, onNewMeet) }
            coachNote?.let { note -> item(key = "coach") { CoachNoteCard(note) } }
        }
    }
}

@Composable
private fun ProgressCard(styles: List<StyleProgress>, coachNote: CoachNote?) {
    val colors = MaterialTheme.colorScheme
    val mastered = styles.count { it.status == SwimStyleStatus.MASTERED }
    val learning = styles.count { it.status == SwimStyleStatus.LEARNING }
    val pending = styles.size - mastered - learning
    val fraction = if (styles.isEmpty()) 0f else mastered / styles.size.toFloat()
    FormCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.swim_progress_overline).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.6.sp),
                    color = colors.primary,
                )
                Text(
                    stringResource(R.string.swim_progress_title, mastered, styles.size),
                    modifier = Modifier.padding(top = 2.dp).semantics { heading() },
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                )
                coachNote?.let {
                    Text(
                        stringResource(R.string.swim_coach_goal, it.goal),
                        modifier = Modifier.padding(top = SportKidTheme.spacing.xs),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(SportKidTheme.spacing.lg))
            SkCircularProgress(progress = fraction, size = 80.dp, color = SportKidTheme.sportColors.swimming) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.swim_progress_percent, (fraction * 100).toInt()),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.onSurface,
                    )
                    Text(stringResource(R.string.swim_progress_fraction, mastered, styles.size), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .background(colors.surfaceContainerLow, MaterialTheme.shapes.medium)
                .padding(horizontal = 10.dp, vertical = SportKidTheme.spacing.sm),
        ) {
            listOf(mastered to R.string.swim_stat_mastered, learning to R.string.swim_stat_learning, pending to R.string.swim_stat_pending).forEach { (count, label) ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$count", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
                    Text(stringResource(label), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(icon: Int, iconTint: Color, title: String, trailing: @Composable () -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(icon), contentDescription = null, tint = iconTint)
        Spacer(Modifier.width(6.dp))
        Text(title, modifier = Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        trailing()
    }
}

/** O18. Checklist de los 5 estilos con su estado (M21). */
@Composable
private fun StylesChecklist(styles: List<StyleProgress>) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
        SectionTitle(R.drawable.ic_swim_checklist, colors.primary, stringResource(R.string.swim_styles_title)) {
            Text(stringResource(R.string.swim_styles_hint), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
        Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = colors.surfaceContainerLowest, shadowElevation = 1.dp) {
            Column(Modifier.padding(SportKidTheme.spacing.sm), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
                styles.forEach { StyleRow(it) }
            }
        }
    }
}

@Composable
private fun StyleRow(item: StyleProgress) {
    val colors = MaterialTheme.colorScheme
    val (rowColor, iconContainer, iconTint) = when (item.status) {
        SwimStyleStatus.MASTERED -> Triple(colors.surfaceContainerLow, colors.secondaryContainer, colors.primary)
        SwimStyleStatus.LEARNING -> Triple(colors.tertiaryContainer.copy(alpha = 0.3f), colors.tertiaryContainer, colors.tertiary)
        SwimStyleStatus.PENDING -> Triple(colors.surfaceContainerHigh.copy(alpha = 0.6f), colors.surfaceContainerHighest, colors.outline)
    }
    val (pillContainer, pillContent) = when (item.status) {
        SwimStyleStatus.MASTERED -> colors.primaryContainer to colors.onPrimaryContainer
        SwimStyleStatus.LEARNING -> colors.tertiary to colors.onTertiary
        SwimStyleStatus.PENDING -> colors.surfaceContainerHighest to colors.onSurfaceVariant
    }
    Row(
        Modifier
            .fillMaxWidth()
            .background(rowColor, MaterialTheme.shapes.medium)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkIconCircle(painterResource(item.style.iconRes()), contentDescription = null, size = 36.dp, containerColor = iconContainer, iconTint = iconTint)
        Spacer(Modifier.width(SportKidTheme.spacing.md))
        Column(Modifier.weight(1f)) {
            Text(stringResource(item.style.labelRes()), style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
            val detail = when {
                item.since != null -> stringResource(R.string.swim_accredited, item.since.fullLabel())
                item.style == SwimStyle.MEDLEY && item.status != SwimStyleStatus.MASTERED -> stringResource(R.string.swim_medley_requirement)
                else -> item.note
            }
            detail?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) }
        }
        Spacer(Modifier.width(SportKidTheme.spacing.sm))
        SkPill(
            stringResource(item.status.labelRes()),
            containerColor = pillContainer,
            contentColor = pillContent,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

/** O19. Mejores tiempos: estilo × distancia, con el récord destacado y "--:--.--" si no hay marca. */
@Composable
private fun BestTimesTable(bestTimes: Map<SwimStyle, Map<Int, SwimTime>>, styles: List<StyleProgress>) {
    val colors = MaterialTheme.colorScheme
    val distances = listOf(25, 50, 100)
    val rows = SwimStyle.entries.filter { bestTimes[it]?.isNotEmpty() == true }
    val (recordStyle, recordDistance) = SwimSamples.highlight
    FormCard(spacing = 8) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkIconCircle(painterResource(R.drawable.ic_swim_timer), contentDescription = null, size = 32.dp, containerColor = colors.secondaryContainer, iconTint = colors.primary)
            Spacer(Modifier.width(SportKidTheme.spacing.sm))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.swim_best_times_title), modifier = Modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Text(stringResource(R.string.swim_best_times_pool), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Text(
                stringResource(R.string.swim_best_times_badge),
                modifier = Modifier.background(colors.surfaceContainer, MaterialTheme.shapes.small).padding(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onSurfaceVariant,
            )
        }
        Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(colors.surfaceContainerLow)) {
            Row(Modifier.fillMaxWidth().background(colors.surfaceContainer).padding(horizontal = SportKidTheme.spacing.md, vertical = 10.dp)) {
                Text(stringResource(R.string.swim_best_times_style), Modifier.weight(1.4f), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = colors.onSurfaceVariant)
                distances.forEachIndexed { i, d ->
                    Text(
                        stringResource(R.string.swim_distance_compact, d),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.onSurfaceVariant,
                        textAlign = if (i == distances.lastIndex) TextAlign.End else TextAlign.Center,
                    )
                }
            }
            rows.forEachIndexed { index, style ->
                val learning = styles.firstOrNull { it.style == style }?.status == SwimStyleStatus.LEARNING
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(if (index % 2 == 1) colors.surfaceContainerLowest else Color.Transparent)
                        .padding(horizontal = SportKidTheme.spacing.md, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(Modifier.weight(1.4f), verticalAlignment = Alignment.CenterVertically) {
                        AthleteDot(if (learning) colors.tertiary else SportKidTheme.sportColors.swimming)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(style.shortLabelRes()), style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                    }
                    distances.forEachIndexed { i, d ->
                        val time = bestTimes[style]?.get(d)
                        val record = time?.takeIf { style == recordStyle && d == recordDistance }
                        Box(Modifier.weight(1f), contentAlignment = if (i == distances.lastIndex) Alignment.CenterEnd else Alignment.Center) {
                            if (record != null) {
                                Row(
                                    Modifier.background(colors.tertiaryContainer, MaterialTheme.shapes.extraSmall).padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(record.format(), style = MaterialTheme.typography.metric.copy(fontSize = 11.sp, lineHeight = 16.sp), color = colors.onTertiaryContainer)
                                    Spacer(Modifier.width(2.dp))
                                    Icon(
                                        painterResource(R.drawable.ic_swim_record),
                                        contentDescription = stringResource(R.string.swim_best_times_record_cd),
                                        modifier = Modifier.size(10.dp),
                                        tint = colors.onTertiaryContainer,
                                    )
                                }
                            } else {
                                Text(
                                    time?.format() ?: stringResource(R.string.swim_no_time),
                                    style = MaterialTheme.typography.metric.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = if (i == 0) FontWeight.SemiBold else FontWeight.Normal),
                                    color = if (time == null) colors.outline else colors.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(DsR.drawable.sk_ic_info), contentDescription = null, modifier = Modifier.size(12.dp), tint = colors.onSurfaceVariant)
            Spacer(Modifier.width(SportKidTheme.spacing.xs))
            Text(stringResource(R.string.swim_best_times_note), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}

/** Competencias con sus pruebas nadadas (O20 en modo lectura). */
@Composable
private fun MeetList(meets: List<SwimMeet>, onNewMeet: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        SectionTitle(R.drawable.ic_swim_trophy, colors.tertiary, stringResource(R.string.swim_meets_title, meets.size)) {
            SkTextButton(
                stringResource(R.string.swim_meets_add),
                onClick = onNewMeet,
                color = colors.primary,
                textStyle = MaterialTheme.typography.labelSmall,
            )
        }
        meets.forEach { MeetCard(it) }
    }
}

@Composable
private fun MeetCard(meet: SwimMeet) {
    val colors = MaterialTheme.colorScheme
    SkCard(accentColor = SportKidTheme.sportColors.swimming) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
                SkPill(meet.level, containerColor = colors.secondaryContainer, contentColor = colors.onSecondaryContainer)
                Text(meet.name, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(UiR.drawable.ic_form_date), contentDescription = null, modifier = Modifier.size(12.dp), tint = colors.onSurfaceVariant)
                    Spacer(Modifier.width(SportKidTheme.spacing.xs))
                    Text("${meet.date.shortLabelWithYear()} · ${meet.venue}", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(SportKidTheme.spacing.sm))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
                PaymentStatusChip(meet.payment)
                Text(amountLabel(meet.costPesos), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
            }
        }
        Column(
            Modifier
                .padding(top = SportKidTheme.spacing.lg)
                .fillMaxWidth()
                .background(colors.surfaceContainerLow, MaterialTheme.shapes.medium)
                .padding(SportKidTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
        ) {
            Text(stringResource(R.string.swim_meet_results).uppercase(), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.55.sp), color = colors.onSurfaceVariant)
            meet.results.forEach { ResultRow(it) }
        }
    }
}

@Composable
private fun ResultRow(result: RaceResult) {
    val colors = MaterialTheme.colorScheme
    val style = stringResource(result.style.shortLabelRes())
    val gold = result.place == 1
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            if (result.relay) stringResource(R.string.swim_relay, result.distance / 4, style) else stringResource(R.string.swim_race, result.distance, style),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface,
        )
        result.time?.let {
            Text(stringResource(R.string.swim_seconds, it.seconds()), style = MaterialTheme.typography.labelSmall, color = if (gold) colors.primary else colors.onSurface)
            Spacer(Modifier.width(SportKidTheme.spacing.xs))
        }
        result.place?.let { place ->
            val medal = medalFor(place)
            Row(
                Modifier.background(colors.surfaceContainer, MaterialTheme.shapes.extraSmall).padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (result.relay && gold) stringResource(R.string.swim_relay_champions, place) else stringResource(R.string.swim_place, place),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (gold) colors.primary else colors.onSurface,
                )
                medal?.let {
                    Spacer(Modifier.width(2.dp))
                    Icon(painterResource(UiR.drawable.ic_medal), contentDescription = null, modifier = Modifier.size(12.dp), tint = it.colors().accent)
                }
            }
        }
    }
}

@Composable
private fun CoachNoteCard(note: CoachNote) {
    val colors = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = colors.surfaceContainerLowest, shadowElevation = 1.dp) {
        Column {
            Box(Modifier.fillMaxWidth().height(128.dp)) {
                Image(painterResource(R.drawable.img_swim_pool_banner), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(0.5f to Color.Transparent, 1f to colors.surfaceContainerLowest)),
                )
            }
            Column(Modifier.padding(SportKidTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SkIconCircle(painterResource(R.drawable.ic_swim_coach), contentDescription = null, size = 32.dp, containerColor = colors.primary, iconTint = colors.onPrimary)
                    Spacer(Modifier.width(SportKidTheme.spacing.sm))
                    Column {
                        Text(stringResource(R.string.swim_coach_note, note.coach), style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                        Text(stringResource(R.string.swim_coach_goal, note.goal), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    }
                }
                Text(note.text, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }
}

@SkPreviews
@Composable
private fun SwimmingScreenPreview() = SportKidTheme {
    SwimmingScreen(athlete = SwimSamples.mateo, onBack = {}, onNewMeet = {}, today = LocalDate.of(2025, 11, 10))
}
