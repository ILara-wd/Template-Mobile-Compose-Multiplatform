package com.invex.sportkid.feature.athlete

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.sportkid.core.designsystem.component.SkAvatar
import com.invex.sportkid.core.designsystem.component.SkButtonSize
import com.invex.sportkid.core.designsystem.component.SkCard
import com.invex.sportkid.core.designsystem.component.SkDetailTopBar
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkKpiTile
import com.invex.sportkid.core.designsystem.component.SkLinearProgress
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkTextButton
import com.invex.sportkid.core.designsystem.component.SkTonalButton
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full
import com.invex.sportkid.core.model.Belt
import com.invex.sportkid.core.model.PaymentStatus
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.ui.AthleteDot
import com.invex.sportkid.core.ui.BeltSwatch
import com.invex.sportkid.core.ui.PaymentStatusChip
import com.invex.sportkid.core.ui.ageOn
import com.invex.sportkid.core.ui.color
import com.invex.sportkid.core.ui.colors
import com.invex.sportkid.core.ui.daysFrom
import com.invex.sportkid.core.ui.fullLabel
import com.invex.sportkid.core.ui.gradeRes
import com.invex.sportkid.core.ui.iconRes
import com.invex.sportkid.core.ui.label
import com.invex.sportkid.core.ui.labelRes
import com.invex.sportkid.core.ui.longLabel
import com.invex.sportkid.core.ui.monthYearShortLabel
import com.invex.sportkid.core.ui.next
import com.invex.sportkid.core.ui.shortLabel
import com.invex.sportkid.core.ui.shortLabelRes
import java.time.LocalDate
import java.util.Locale
import com.invex.sportkid.core.designsystem.R as DsR
import com.invex.sportkid.core.ui.R as UiR

/**
 * Perfil de deportista (plantilla T2): header con avatar, datos y acciones (O13), tabs de los
 * deportes del hijo (O14) y el contenido de cada deporte. La pestaña de Taekwondo (P09) tiene
 * grado y cinta, próximo evento, estadísticas con la trayectoria de cintas y el equipamiento.
 */
@Composable
fun AthleteProfileScreen(
    athlete: AthleteProfile,
    onBack: () -> Unit,
    onQuickLog: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    onShareQr: () -> Unit = {},
    onTutors: () -> Unit = {},
    onEdit: () -> Unit = {},
    onPayFee: () -> Unit = {},
    onEventDetails: () -> Unit = {},
    initialSport: Sport? = null,
    onOpenSportDetail: (Sport) -> Unit = {},
) {
    var selectedSport by rememberSaveable(athlete.id, initialSport) {
        mutableStateOf(initialSport?.takeIf { it in athlete.sports } ?: athlete.sports.firstOrNull())
    }
    var menuOpen by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            SkDetailTopBar(title = stringResource(R.string.profile_title), onBack = onBack) {
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(painterResource(DsR.drawable.sk_ic_more_vert), stringResource(R.string.profile_more), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.profile_edit)) }, onClick = {
                            menuOpen = false
                            onEdit()
                        })
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onQuickLog,
                shape = MaterialTheme.shapes.full,
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
            ) { Icon(painterResource(DsR.drawable.sk_ic_add), stringResource(R.string.profile_quick_log)) }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp),
        ) {
            ProfileHeader(athlete, today, onShareQr, onTutors)
            if (athlete.sports.isNotEmpty()) {
                SportTabs(
                    sports = athlete.sports,
                    selected = selectedSport,
                    onSelect = { selectedSport = it },
                    modifier = Modifier.padding(start = SportKidTheme.spacing.lg, end = SportKidTheme.spacing.lg, top = 20.dp),
                )
            }
            Column(
                Modifier.padding(start = SportKidTheme.spacing.lg, end = SportKidTheme.spacing.lg, top = SportKidTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg),
            ) {
                val sport = selectedSport
                val tkd = athlete.tkd
                val swim = athlete.swim
                when {
                    sport == Sport.TAEKWONDO && tkd != null -> TkdTab(tkd, today, onPayFee, onEventDetails) { onOpenSportDetail(Sport.TAEKWONDO) }
                    sport == Sport.SWIMMING && swim != null -> {
                        SwimProgressPanel(swim)
                        SkTextButton(
                            stringResource(R.string.profile_swim_detail),
                            onClick = { onOpenSportDetail(Sport.SWIMMING) },
                            trailingIcon = painterResource(DsR.drawable.sk_ic_chevron_right),
                            color = MaterialTheme.colorScheme.primary,
                            textStyle = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.align(Alignment.End),
                        )
                    }
                    sport != null -> TabComingSoon(sport)
                }
            }
        }
    }
}

/** O13. Avatar con anillo del color del hijo, categoría, nombre, edad, resumen y acciones. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileHeader(athlete: AthleteProfile, today: LocalDate, onShareQr: () -> Unit, onTutors: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val ring = athlete.color()
    val age = athlete.birthDate.ageOn(today)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = SportKidTheme.spacing.lg, end = SportKidTheme.spacing.lg, top = SportKidTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SkAvatar(
            photo = athlete.photoRes?.let { painterResource(it) },
            name = athlete.fullName,
            size = 104.dp,
            ringBrush = Brush.linearGradient(listOf(ring, lerp(ring, Color.White, 0.55f))),
            ringWidth = SportKidTheme.spacing.xs,
            shadowElevation = SkElevation.level2,
        ) {
            SkPill(
                stringResource(R.string.profile_category, age + 1),
                modifier = Modifier.align(Alignment.BottomEnd),
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
                icon = painterResource(R.drawable.ic_profile_category),
                shadowElevation = SkElevation.level1,
            )
        }
        Text(
            athlete.fullName,
            modifier = Modifier.padding(top = SportKidTheme.spacing.md).semantics { heading() },
            style = MaterialTheme.typography.headlineSmall.copy(lineHeight = MaterialTheme.typography.headlineSmall.fontSize * 1.33f),
            color = colors.onSurface,
        )
        Text(
            stringResource(R.string.profile_age_birth, age, athlete.birthDate.fullLabel()),
            modifier = Modifier.padding(top = 2.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
        FlowRow(
            Modifier.fillMaxWidth().padding(top = SportKidTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
        ) {
            val chipPadding = PaddingValues(horizontal = SportKidTheme.spacing.md, vertical = 6.dp)
            val chipStyle = MaterialTheme.typography.labelMedium
            athlete.tkd?.let { tkd ->
                SkPill(
                    stringResource(UiR.string.belt_name, stringResource(tkd.belt.shortLabelRes()).lowercase()),
                    containerColor = colors.surfaceContainerHigh,
                    contentColor = colors.onSurface,
                    textStyle = chipStyle,
                    spacing = 6.dp,
                    shadowElevation = SkElevation.level1,
                    contentPadding = chipPadding,
                    leading = { BeltSwatch(tkd.belt, width = 14.dp, height = 8.dp) },
                )
            }
            athlete.swim?.let { swim ->
                SkPill(
                    stringResource(R.string.profile_styles, swim.mastered.size, swim.total),
                    containerColor = colors.secondaryContainer,
                    contentColor = colors.onSecondaryContainer,
                    textStyle = chipStyle,
                    spacing = 6.dp,
                    shadowElevation = SkElevation.level1,
                    contentPadding = chipPadding,
                    leading = {
                        Icon(painterResource(Sport.SWIMMING.iconRes), null, Modifier.size(14.dp), tint = SportKidTheme.sportColors.swimming)
                    },
                )
            }
            if (athlete.medals.total > 0) {
                SkPill(
                    pluralStringResource(R.plurals.profile_medals, athlete.medals.total, athlete.medals.total),
                    containerColor = colors.tertiaryContainer,
                    contentColor = colors.onTertiaryContainer,
                    icon = painterResource(UiR.drawable.ic_medal),
                    iconTint = SportKidTheme.sportColors.medalGold,
                    textStyle = chipStyle,
                    spacing = 6.dp,
                    shadowElevation = SkElevation.level1,
                    contentPadding = chipPadding,
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = SportKidTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.md),
        ) {
            SkTonalButton(
                stringResource(R.string.profile_share_qr),
                onClick = onShareQr,
                icon = painterResource(R.drawable.ic_profile_qr),
                containerColor = colors.primaryContainer,
                contentColor = colors.primary,
                modifier = Modifier.weight(1f),
            )
            SkTonalButton(
                stringResource(R.string.profile_tutors, athlete.tutors),
                onClick = onTutors,
                icon = painterResource(R.drawable.ic_profile_tutors),
                containerColor = colors.surfaceContainerHigh,
                contentColor = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** O14. Tabs con los deportes del hijo; la activa va en el color del deporte. */
@Composable
private fun SportTabs(sports: List<Sport>, selected: Sport?, onSelect: (Sport) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .background(colors.surfaceContainer, MaterialTheme.shapes.large)
            .padding(SportKidTheme.spacing.xs)
            .selectableGroup(),
    ) {
        sports.forEach { sport ->
            val active = sport == selected
            val sportColor = sport.colors().content
            Row(
                Modifier
                    .weight(1f)
                    .height(36.dp)
                    .then(if (active) Modifier.shadow(SkElevation.level1, MaterialTheme.shapes.medium) else Modifier)
                    .clip(MaterialTheme.shapes.medium)
                    .background(if (active) sportColor else Color.Transparent)
                    .selectable(selected = active, role = Role.Tab, onClick = { onSelect(sport) }),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val content = if (active) Color.White else colors.onSurfaceVariant
                Icon(painterResource(sport.iconRes), contentDescription = null, modifier = Modifier.size(16.dp), tint = content)
                Text(
                    stringResource(sport.labelRes),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold),
                    color = content,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun TkdTab(tkd: TkdProfile, today: LocalDate, onPayFee: () -> Unit, onEventDetails: () -> Unit, onOpenHistory: () -> Unit) {
    val accent = Sport.TAEKWONDO.colors().content
    BeltCard(tkd, today, accent, onOpenHistory)
    tkd.nextEvent?.let { NextEventCard(it, accent, onPayFee, onEventDetails) }
    StatsCard(tkd, today, accent)
    EquipmentCard(tkd.equipment, accent)
}

@Composable
private fun ProfileCard(accent: Color, content: @Composable ColumnScope.() -> Unit) {
    SkCard(accentColor = accent) {
        Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.md), content = content)
    }
}

@Composable
private fun CardTitle(icon: Int?, title: String, iconTint: Color, trailing: @Composable () -> Unit = {}) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, tint = iconTint)
            Spacer(Modifier.width(SportKidTheme.spacing.sm))
        }
        Text(
            title,
            modifier = Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        trailing()
    }
}

/** Card 1: cinta actual, instructor y progreso hacia la siguiente cinta (O15). */
@Composable
private fun BeltCard(tkd: TkdProfile, today: LocalDate, accent: Color, onOpenHistory: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val beltColor = tkd.belt.color()
    val next = tkd.belt.next
    ProfileCard(accent) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
                Text(
                    stringResource(R.string.profile_tkd_overline).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.55.sp),
                    color = colors.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.profile_tkd_title),
                    modifier = Modifier.semantics { heading() },
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                )
            }
            SkPill(
                stringResource(UiR.string.belt_with_grade, stringResource(tkd.belt.shortLabelRes()), stringResource(tkd.belt.gradeRes())),
                containerColor = beltColor.copy(alpha = 0.15f),
                contentColor = colors.onSurface,
                textStyle = MaterialTheme.typography.labelMedium,
                spacing = 6.dp,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                leading = { AthleteDot(beltColor, size = 10.dp) },
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .background(colors.surfaceContainerLow, MaterialTheme.shapes.medium)
                .padding(SportKidTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkIconCircle(
                icon = painterResource(Sport.TAEKWONDO.iconRes),
                contentDescription = null,
                containerColor = colors.surfaceContainerHigh,
                iconTint = colors.onSurfaceVariant,
            )
            Spacer(Modifier.width(SportKidTheme.spacing.md))
            Column(Modifier.weight(1f)) {
                Text(tkd.instructor, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                Text(tkd.school, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        if (next != null) {
            val nextColor = next.color()
            Column(Modifier.padding(top = SportKidTheme.spacing.xs), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AthleteDot(nextColor)
                    Spacer(Modifier.width(SportKidTheme.spacing.xs))
                    Text(
                        stringResource(R.string.profile_goal, stringResource(next.shortLabelRes()), stringResource(next.gradeRes())),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurface,
                    )
                    Text(
                        stringResource(R.string.profile_goal_progress, (tkd.progressToNext * 100).toInt()),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = accent,
                    )
                }
                SkLinearProgress(
                    progress = tkd.progressToNext,
                    height = 10.dp,
                    brush = Brush.horizontalGradient(listOf(beltColor, nextColor)),
                    trackColor = colors.surfaceContainer,
                )
                tkd.nextEvent?.let { exam ->
                    Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_profile_exam), contentDescription = null, tint = accent)
                        Spacer(Modifier.width(SportKidTheme.spacing.xs))
                        Text(
                            stringResource(R.string.profile_next_exam, exam.date.shortLabel()),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                        val days = exam.date.daysFrom(today).toInt()
                        if (days > 0) {
                            Text(
                                pluralStringResource(R.plurals.profile_days_left, days, days),
                                modifier = Modifier
                                    .background(colors.surfaceContainer, MaterialTheme.shapes.extraSmall)
                                    .padding(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurface,
                            )
                        }
                    }
                }
            }
        }
        SkTextButton(
            stringResource(R.string.profile_tkd_history),
            onClick = onOpenHistory,
            trailingIcon = painterResource(DsR.drawable.sk_ic_chevron_right),
            color = colors.primary,
            textStyle = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.End),
        )
    }
}

/** Card 2: próximo evento con su cuota y estado de pago. */
@Composable
private fun NextEventCard(event: TkdEvent, accent: Color, onPayFee: () -> Unit, onDetails: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val tkd = Sport.TAEKWONDO.colors()
    ProfileCard(accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.background(tkd.container, MaterialTheme.shapes.small)) {
                Icon(painterResource(R.drawable.ic_profile_event), contentDescription = null, tint = accent)
            }
            Spacer(Modifier.width(SportKidTheme.spacing.sm))
            Text(
                stringResource(R.string.profile_next_event),
                modifier = Modifier.weight(1f).semantics { heading() },
                style = MaterialTheme.typography.titleMedium,
                color = colors.onSurface,
            )
            PaymentStatusChip(event.payment)
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            IconLine(R.drawable.ic_profile_date, stringResource(R.string.profile_event_when, event.date.longLabel(), event.time.label()))
            IconLine(R.drawable.ic_profile_place, event.place)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .background(colors.surfaceContainerLow, MaterialTheme.shapes.medium)
                .padding(SportKidTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(event.costLabel, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                Text(
                    String.format(Locale("es", "MX"), "$%,.2f MXN", event.costPesos.toDouble()),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSurface,
                )
            }
            if (event.payment == PaymentStatus.PENDING) {
                SkTonalButton(
                    stringResource(R.string.profile_pay_fee),
                    onClick = onPayFee,
                    containerColor = colors.tertiary,
                    contentColor = colors.onTertiary,
                    size = SkButtonSize.Small,
                )
            }
        }
        SkTextButton(
            stringResource(R.string.profile_event_details),
            onClick = onDetails,
            trailingIcon = painterResource(DsR.drawable.sk_ic_chevron_right),
            color = colors.primary,
            textStyle = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.End),
        )
    }
}

@Composable
private fun IconLine(icon: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Card 3: KPIs de la temporada y trayectoria de cintas (O16). */
@Composable
private fun StatsCard(tkd: TkdProfile, today: LocalDate, accent: Color) {
    val colors = MaterialTheme.colorScheme
    ProfileCard(accent) {
        CardTitle(R.drawable.ic_profile_stats, stringResource(R.string.profile_stats_title), accent) {
            Text(stringResource(R.string.profile_season, today.year), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
            val tile = Modifier.weight(1f).fillMaxHeight()
            SkKpiTile("${tkd.fightsWon} / ${tkd.fightsTotal}", stringResource(R.string.profile_stat_fights), tile, valueColor = colors.primary)
            SkKpiTile(tkd.formsLabel.replace(' ', '\n'), stringResource(R.string.profile_stat_forms), tile, valueColor = accent)
            SkKpiTile("${tkd.attendancePercent}%", stringResource(R.string.profile_stat_attendance), tile, valueColor = SportKidTheme.sportColors.success)
        }
        Text(
            stringResource(R.string.profile_belt_history).uppercase(),
            modifier = Modifier.padding(top = SportKidTheme.spacing.sm),
            style = MaterialTheme.typography.labelSmall,
            color = colors.onSurfaceVariant,
        )
        BeltTimeline(tkd.history, accent)
    }
}

@Composable
private fun BeltTimeline(steps: List<BeltStep>, accent: Color) {
    val line = MaterialTheme.colorScheme.surfaceContainerHighest
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = SportKidTheme.spacing.sm)
            .drawBehind {
                val y = 16.dp.toPx()
                drawLine(line, Offset(16.dp.toPx(), y), Offset(size.width - 16.dp.toPx(), y), strokeWidth = 2.dp.toPx())
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        steps.forEachIndexed { index, step ->
            if (index > 0) {
                Icon(
                    painterResource(R.drawable.ic_profile_step_arrow),
                    contentDescription = null,
                    modifier = Modifier.padding(top = 11.dp),
                    tint = if (index == steps.lastIndex) accent else line,
                )
            }
            TimelineStep(step, current = index == steps.lastIndex)
        }
    }
}

@Composable
private fun TimelineStep(step: BeltStep, current: Boolean) {
    val colors = MaterialTheme.colorScheme
    val beltColor = step.belt.color()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(32.dp)
                .shadow(SkElevation.level2, CircleShape)
                .background(if (step.belt == Belt.WHITE) colors.surfaceContainerLowest else lerp(colors.surfaceContainerLowest, beltColor, 0.15f), CircleShape)
                .then(if (current) Modifier.border(2.dp, beltColor, CircleShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .size(14.dp)
                    .background(if (step.belt == Belt.WHITE) colors.outlineVariant else beltColor, CircleShape),
            )
        }
        Text(
            stringResource(step.belt.shortLabelRes()),
            modifier = Modifier.padding(top = SportKidTheme.spacing.xs),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (current) FontWeight.Bold else FontWeight.SemiBold),
            color = colors.onSurface,
        )
        if (current) {
            SkPill(
                stringResource(R.string.profile_belt_current),
                containerColor = beltColor.copy(alpha = 0.2f),
                contentColor = colors.onSurface,
                emphasized = true,
                contentPadding = PaddingValues(horizontal = 6.dp),
            )
        } else {
            Text(step.date.monthYearShortLabel(), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
    }
}

/** Card 4: equipamiento registrado y aprobado por la escuela. */
@Composable
private fun EquipmentCard(items: List<Equipment>, accent: Color) {
    val colors = MaterialTheme.colorScheme
    ProfileCard(accent) {
        CardTitle(R.drawable.ic_profile_equipment, stringResource(R.string.profile_equipment_title), accent) {
            Icon(painterResource(R.drawable.ic_profile_verified), contentDescription = null, tint = colors.onSurfaceVariant)
        }
        Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
            items.forEach { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceContainerLow, MaterialTheme.shapes.medium)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(32.dp).background(colors.surfaceContainerHigh, MaterialTheme.shapes.small),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(painterResource(item.icon), contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.onSurface)
                    }
                    Spacer(Modifier.width(SportKidTheme.spacing.md))
                    Column(Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                        Text(item.detail, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                    if (item.approved) {
                        Text(
                            stringResource(R.string.profile_equipment_approved),
                            modifier = Modifier
                                .background(colors.secondaryContainer, MaterialTheme.shapes.small)
                                .padding(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSecondaryContainer,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabComingSoon(sport: Sport) {
    Text(
        stringResource(R.string.profile_tab_empty, stringResource(sport.labelRes)),
        modifier = Modifier.fillMaxWidth().padding(vertical = SportKidTheme.spacing.xl),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@SkPreviews
@Composable
private fun AthleteProfilePreview() = SportKidTheme {
    val today = LocalDate.of(2025, 10, 27)
    AthleteProfileScreen(athlete = AthleteSamples.athletes(today).first(), onBack = {}, onQuickLog = {}, today = today)
}
