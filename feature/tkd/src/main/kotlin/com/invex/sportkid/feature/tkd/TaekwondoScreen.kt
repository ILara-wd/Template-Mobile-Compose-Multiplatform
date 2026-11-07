package com.invex.sportkid.feature.tkd

import com.invex.sportkid.core.ui.FormCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.sportkid.core.designsystem.component.SkButton
import com.invex.sportkid.core.designsystem.component.SkButtonSize
import com.invex.sportkid.core.designsystem.component.SkCard
import com.invex.sportkid.core.designsystem.component.SkDetailTopBar
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full
import com.invex.sportkid.core.model.Belt
import com.invex.sportkid.core.model.MedalResult
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.ui.AthleteDot
import com.invex.sportkid.core.ui.AthleteSportHeader
import com.invex.sportkid.core.ui.MedalBadge
import com.invex.sportkid.core.ui.ageOn
import com.invex.sportkid.core.ui.color
import com.invex.sportkid.core.ui.colors
import com.invex.sportkid.core.ui.gradeRes
import com.invex.sportkid.core.ui.iconRes
import com.invex.sportkid.core.ui.labelRes
import com.invex.sportkid.core.ui.next
import com.invex.sportkid.core.ui.shortLabelRes
import com.invex.sportkid.core.ui.shortLabelWithYear
import java.time.LocalDate
import com.invex.sportkid.core.ui.R as UiR

/**
 * P09. Taekwondo de un deportista: resumen, accesos a "+ Examen de cinta" y "+ Torneo", cinta
 * actual con la progresión de 9 grados (O15), historial de promociones (O16) y torneos (O17).
 */
@Composable
fun TaekwondoScreen(
    athlete: TkdAthlete,
    onBack: () -> Unit,
    onNewExam: () -> Unit,
    onNewTournament: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    promotions: List<BeltPromotion> = TkdSamples.promotions,
    tournaments: List<Tournament> = TkdSamples.tournaments,
    onSelectSport: (Sport) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            SkDetailTopBar(title = stringResource(R.string.tkd_screen_title), subtitle = athlete.fullName, onBack = onBack)
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(start = SportKidTheme.spacing.lg, end = SportKidTheme.spacing.lg, top = SportKidTheme.spacing.sm, bottom = SportKidTheme.spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xl),
        ) {
            item(key = "summary") { SummaryCard(athlete, today, promotions.size, tournaments.size, onSelectSport) }
            item(key = "actions") { QuickActions(onNewExam, onNewTournament) }
            item(key = "belt") { BeltCard(athlete) }
            item(key = "history") { PromotionHistory(promotions.sortedByDescending(BeltPromotion::date), athlete.belt) }
            item(key = "tournaments") { TournamentList(tournaments.sortedByDescending(Tournament::date)) }
        }
    }
}

@Composable
private fun SummaryCard(athlete: TkdAthlete, today: LocalDate, exams: Int, tournaments: Int, onSelectSport: (Sport) -> Unit) {
    val colors = MaterialTheme.colorScheme
    AthleteSportHeader(
        name = athlete.fullName,
        photo = athlete.photoRes?.let { painterResource(it) },
        ringColor = athlete.color(),
        age = athlete.birthDate.ageOn(today),
        subtitle = athlete.federation,
        sport = Sport.TAEKWONDO,
        sports = athlete.sports,
        onSelectSport = onSelectSport,
    ) {
        SkPill(
            pluralStringResource(R.plurals.tkd_exams_count, exams, exams).replaceFirstChar(Char::titlecase),
            icon = painterResource(R.drawable.ic_tkd_exams),
        )
        SkPill(
            pluralStringResource(R.plurals.tkd_tournaments_count, tournaments, tournaments).replaceFirstChar(Char::titlecase),
            containerColor = colors.tertiaryContainer,
            contentColor = colors.onTertiaryContainer,
            icon = painterResource(R.drawable.ic_tkd_tournaments),
        )
    }
}

@Composable
private fun QuickActions(onNewExam: () -> Unit, onNewTournament: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        SkButton(
            stringResource(R.string.tkd_new_exam),
            onClick = onNewExam,
            leadingIcon = painterResource(R.drawable.ic_tkd_exam_action),
            size = SkButtonSize.Medium,
            modifier = Modifier.weight(1f),
        )
        SkButton(
            stringResource(R.string.tkd_new_tournament),
            onClick = onNewTournament,
            leadingIcon = painterResource(R.drawable.ic_tkd_trophy),
            size = SkButtonSize.Medium,
            containerColor = colors.tertiary,
            contentColor = colors.onTertiary,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Cinta actual, siguiente objetivo y progresión de 9 grados (O15). */
@Composable
private fun BeltCard(athlete: TkdAthlete) {
    val colors = MaterialTheme.colorScheme
    val tkd = Sport.TAEKWONDO.colors().content
    val position = athlete.belt.ordinal + 1
    val total = Belt.entries.size
    FormCard(spacing = 0) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.tkd_grade_overline).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.55.sp),
                    color = tkd,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(UiR.string.belt_name, stringResource(athlete.belt.shortLabelRes())),
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.headlineSmall,
                        color = colors.onSurface,
                    )
                    Spacer(Modifier.width(SportKidTheme.spacing.sm))
                    SkPill(stringResource(athlete.belt.gradeRes()), containerColor = colors.surfaceContainerHigh, contentColor = colors.onSurface)
                }
                Text(
                    stringResource(R.string.tkd_school_instructor, athlete.school, athlete.instructor),
                    modifier = Modifier.padding(top = 2.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
            Column(
                Modifier
                    .background(colors.surfaceContainerLow, MaterialTheme.shapes.medium)
                    .padding(horizontal = SportKidTheme.spacing.sm, vertical = SportKidTheme.spacing.xs),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("${position * 100 / total}%", style = MaterialTheme.typography.titleSmall, color = colors.primary)
                Text(stringResource(R.string.tkd_route), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
        }
        athlete.belt.next?.let { next ->
            Row(
                Modifier
                    .padding(vertical = SportKidTheme.spacing.lg)
                    .fillMaxWidth()
                    .background(colors.primaryContainer, MaterialTheme.shapes.medium)
                    .padding(SportKidTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AthleteDot(next.color(), size = 14.dp)
                Spacer(Modifier.width(SportKidTheme.spacing.sm))
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.tkd_next_goal).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.55.sp),
                        color = colors.primary,
                    )
                    Text(
                        stringResource(R.string.tkd_belt_with_grade, stringResource(next.shortLabelRes()), stringResource(next.gradeRes())),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.onPrimaryContainer,
                    )
                }
                SkPill(
                    stringResource(R.string.tkd_next),
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        BeltProgression(athlete.belt, Modifier.padding(top = SportKidTheme.spacing.xs))
    }
}

/** O15. Barra de 9 tramos: superados y actual sólidos, siguiente al 40%, resto atenuado. */
@Composable
private fun BeltProgression(current: Belt, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val tkd = Sport.TAEKWONDO.colors().content
    val success = SportKidTheme.sportColors.success
    val abbr = stringArrayResource(R.array.tkd_belt_abbr)
    val belts = Belt.entries
    val description = stringResource(R.string.tkd_progress, current.ordinal + 1, belts.size)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(description, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            Text(
                stringResource(R.string.tkd_active_grade, stringResource(current.gradeRes())),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = tkd,
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(colors.surfaceContainer, MaterialTheme.shapes.full)
                .padding(2.dp)
                .clearAndSetSemantics { contentDescription = description },
            horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs),
        ) {
            belts.forEach { belt ->
                val alpha = when {
                    belt.ordinal <= current.ordinal -> 1f
                    belt.ordinal == current.ordinal + 1 -> 0.4f
                    else -> 0.25f
                }
                val base = if (belt == Belt.WHITE) colors.surfaceContainerHighest else belt.color()
                val brush = if (belt == Belt.RED_BLACK) {
                    Brush.horizontalGradient(listOf(SportKidTheme.sportColors.belt.red.copy(alpha = alpha), SportKidTheme.sportColors.belt.black.copy(alpha = alpha)))
                } else {
                    Brush.linearGradient(listOf(base.copy(alpha = alpha), base.copy(alpha = alpha)))
                }
                Box(Modifier.weight(1f).fillMaxHeight().background(brush, MaterialTheme.shapes.extraSmall))
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = SportKidTheme.spacing.xs).clearAndSetSemantics {}, horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
            belts.forEach { belt ->
                val isCurrent = belt == current
                val isNext = belt.ordinal == current.ordinal + 1
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.height(14.dp), contentAlignment = Alignment.Center) {
                        when {
                            isCurrent -> Icon(painterResource(R.drawable.ic_tkd_step_current), null, Modifier.size(12.dp), tint = success)
                            belt.ordinal < current.ordinal -> Icon(painterResource(R.drawable.ic_tkd_step_passed), null, Modifier.size(11.dp), tint = success)
                            isNext -> Icon(painterResource(R.drawable.ic_tkd_step_next), null, Modifier.size(9.dp), tint = colors.primary)
                            else -> AthleteDot(colors.outlineVariant, size = 6.dp)
                        }
                    }
                    Text(
                        abbr[belt.ordinal],
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            lineHeight = 15.sp,
                            fontWeight = if (isCurrent || isNext) FontWeight.Bold else FontWeight.Normal,
                        ),
                        color = when {
                            isCurrent -> success
                            isNext -> colors.primary
                            else -> colors.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(icon: Int, iconTint: Color, title: String, trailing: String, trailingColor: Color, trailingBold: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(icon), contentDescription = null, tint = iconTint)
        Spacer(Modifier.width(SportKidTheme.spacing.sm))
        Text(
            title,
            modifier = Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            trailing,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (trailingBold) FontWeight.Bold else FontWeight.SemiBold),
            color = trailingColor,
        )
    }
}

/** O16. Historial vertical de promociones, de la más reciente a la primera. */
@Composable
private fun PromotionHistory(promotions: List<BeltPromotion>, current: Belt) {
    val colors = MaterialTheme.colorScheme
    val line = colors.surfaceContainerHighest
    Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        SectionTitle(
            R.drawable.ic_tkd_history,
            Sport.TAEKWONDO.colors().content,
            stringResource(R.string.tkd_history_title),
            pluralStringResource(R.plurals.tkd_exams_count, promotions.size, promotions.size),
            colors.onSurfaceVariant,
        )
        Box {
            Box(Modifier.matchParentSize().padding(start = 9.dp, top = 12.dp, bottom = 24.dp)) {
                Box(Modifier.width(2.dp).fillMaxHeight().background(line))
            }
            Column(Modifier.padding(start = 24.dp), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg)) {
                promotions.forEach { PromotionItem(it, isCurrent = it.belt == current) }
            }
        }
    }
}

@Composable
private fun PromotionItem(promotion: BeltPromotion, isCurrent: Boolean) {
    val colors = MaterialTheme.colorScheme
    val beltColor = promotion.belt.color()
    Box {
        FormCard(spacing = 6) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkPill(
                    stringResource(UiR.string.belt_name, stringResource(promotion.belt.shortLabelRes())),
                    containerColor = if (promotion.belt == Belt.WHITE) colors.surfaceContainer else beltColor.copy(alpha = 0.15f),
                    contentColor = colors.onSurface,
                    emphasized = true,
                    contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                )
                Spacer(Modifier.width(SportKidTheme.spacing.sm))
                SkPill(
                    stringResource(promotion.belt.gradeRes()),
                    containerColor = if (isCurrent) colors.secondaryContainer else colors.surfaceContainer,
                    contentColor = if (isCurrent) colors.primary else colors.onSurfaceVariant,
                    contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                )
                Spacer(Modifier.weight(1f))
                Text(promotion.date.shortLabelWithYear(), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(if (promotion.enrollment) R.drawable.ic_tkd_enroll else UiR.drawable.ic_event_place),
                    contentDescription = null,
                    tint = colors.onSurfaceVariant,
                )
                Spacer(Modifier.width(6.dp))
                Text(promotion.place, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceContainerLow.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                    .padding(horizontal = 10.dp, vertical = SportKidTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.tkd_exam_cost), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                val cost = promotion.costPesos
                if (cost == null) {
                    Text(stringResource(R.string.tkd_no_cost), style = MaterialTheme.typography.titleSmall, color = colors.onSurfaceVariant)
                } else {
                    Text(amountLabel(cost), style = MaterialTheme.typography.titleSmall, color = colors.primary)
                    Spacer(Modifier.width(SportKidTheme.spacing.xs))
                    Text(
                        stringResource(if (promotion.paid) R.string.tkd_paid_suffix else R.string.tkd_pending_suffix),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (promotion.paid) SportKidTheme.sportColors.success else colors.tertiary,
                    )
                }
            }
        }
        val node = if (promotion.belt == Belt.WHITE) colors.outlineVariant else beltColor
        Box(
            Modifier
                .offset(x = (-24).dp, y = 6.dp)
                .size(20.dp)
                .background(node, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(if (promotion.enrollment) R.drawable.ic_tkd_flag else UiR.drawable.ic_sport_chip_check),
                contentDescription = null,
                modifier = Modifier.size(9.dp),
                tint = if (node.luminance() > 0.5f) colors.onSurface else Color.White,
            )
        }
    }
}

/** O17. Torneos con acento del color de la medalla, modalidad y división. */
@Composable
private fun TournamentList(tournaments: List<Tournament>) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        SectionTitle(
            R.drawable.ic_tkd_trophy,
            colors.tertiary,
            stringResource(R.string.tkd_tournaments_title),
            pluralStringResource(R.plurals.tkd_competitions_count, tournaments.size, tournaments.size),
            colors.primary,
            trailingBold = true,
        )
        tournaments.forEach { tournament ->
            SkCard(shape = MaterialTheme.shapes.medium, accentColor = tournament.result.medalAccent(), accentWidth = 4.dp) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(tournament.name, style = MaterialTheme.typography.titleMedium, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            stringResource(R.string.tkd_date_place, tournament.date.shortLabelWithYear(), tournament.place),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(SportKidTheme.spacing.sm))
                    MedalBadge(tournament.result)
                }
                Row(
                    Modifier
                        .padding(top = SportKidTheme.spacing.md)
                        .fillMaxWidth()
                        .background(colors.surfaceContainerLow.copy(alpha = 0.6f), MaterialTheme.shapes.small)
                        .padding(horizontal = SportKidTheme.spacing.md, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val combat = tournament.modality == TournamentModality.Combat
                    SkPill(
                        stringResource(tournament.modality.labelRes()),
                        containerColor = if (combat) colors.primaryContainer else colors.secondaryContainer,
                        contentColor = if (combat) colors.onPrimaryContainer else colors.onSecondaryContainer,
                        spacing = SportKidTheme.spacing.xs,
                        leading = { Icon(painterResource(tournament.modality.iconRes()), null, Modifier.size(14.dp)) },
                    )
                    Spacer(Modifier.weight(1f))
                    Text(tournament.division, style = MaterialTheme.typography.labelSmall, color = colors.onSurface)
                }
            }
        }
    }
}

/** Acento lateral: color de la medalla, o neutro para participación. */
@Composable
private fun MedalResult.medalAccent(): Color =
    if (this == MedalResult.PARTICIPATION) MaterialTheme.colorScheme.surfaceContainerHighest else colors().accent

@SkPreviews
@Composable
private fun TaekwondoScreenPreview() = SportKidTheme {
    TaekwondoScreen(athlete = TkdSamples.mateo, onBack = {}, onNewExam = {}, onNewTournament = {}, today = LocalDate.of(2025, 11, 10))
}
