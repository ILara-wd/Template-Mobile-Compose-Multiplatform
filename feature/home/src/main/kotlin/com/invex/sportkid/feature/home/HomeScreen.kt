package com.invex.sportkid.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.sportkid.core.designsystem.component.SkAvatar
import com.invex.sportkid.core.designsystem.component.SkCard
import com.invex.sportkid.core.designsystem.component.SkExtendedFab
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkTextButton
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full
import com.invex.sportkid.core.ui.AthleteTag
import com.invex.sportkid.core.ui.PaymentStatusChip
import com.invex.sportkid.core.ui.ageOn
import com.invex.sportkid.core.ui.colors
import com.invex.sportkid.core.ui.daysFrom
import com.invex.sportkid.core.ui.iconRes
import com.invex.sportkid.core.ui.label
import com.invex.sportkid.core.ui.labelRes
import com.invex.sportkid.core.ui.shortLabel
import java.time.LocalDate
import kotlin.math.roundToInt
import com.invex.sportkid.core.designsystem.R as DsR
import com.invex.sportkid.core.ui.R as UiR

/**
 * P05. Inicio (plantilla T1, pestaña): chips por hijo, resumen de la semana, próximos eventos
 * (O6), últimos logros (O7), gastos del mes (O8) y el FAB "Registrar" que abre O4.
 * El chip de un hijo filtra todas las secciones.
 */
@Composable
fun HomeScreen(
    onRegister: () -> Unit,
    onOpenAgenda: () -> Unit,
    onOpenExpenses: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    data: HomeData = remember(today) { HomeSamples.data(today) },
) {
    var athleteId by rememberSaveable { mutableStateOf<String?>(null) }
    val athletes = data.athletes.associateBy { it.id }
    val events = data.events.filter { athleteId == null || it.athleteId == athleteId }.sortedWith(compareBy({ it.date }, { it.time }))
    val achievements = data.achievements.filter { athleteId == null || it.athleteId == athleteId }
    val spends = data.spends.filter { athleteId == null || it.athleteId == athleteId }

    Box(modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp),
        ) {
            AthleteChips(data.athletes, data.events.size, athleteId, { athleteId = it }, today)
            WeekBanner(events, today, Modifier.padding(horizontal = SportKidTheme.spacing.lg).padding(bottom = 20.dp))
            Upcoming(events.take(3), athletes, onOpenAgenda, Modifier.padding(horizontal = SportKidTheme.spacing.lg).padding(bottom = SportKidTheme.spacing.xl))
            if (achievements.isNotEmpty()) {
                Achievements(achievements, athletes, today, Modifier.padding(bottom = SportKidTheme.spacing.xl))
            }
            ExpensesCard(spends, today, onOpenExpenses, Modifier.padding(horizontal = SportKidTheme.spacing.lg))
        }
        SkExtendedFab(
            text = stringResource(R.string.home_register),
            onClick = onRegister,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(SportKidTheme.spacing.lg),
        )
    }
}

/** Chips "Todos" (con el número de eventos) y uno por hijo con foto, punto de su color y edad. */
@Composable
private fun AthleteChips(athletes: List<HomeAthlete>, total: Int, selected: String?, onSelect: (String?) -> Unit, today: LocalDate) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = SportKidTheme.spacing.lg)
            .padding(top = SportKidTheme.spacing.sm, bottom = SportKidTheme.spacing.md)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
    ) {
        val allSelected = selected == null
        Surface(
            modifier = Modifier
                .height(44.dp)
                .selectable(allSelected, role = Role.RadioButton, onClick = { onSelect(null) }),
            shape = MaterialTheme.shapes.full,
            color = if (allSelected) colors.primary else colors.surfaceContainerLowest,
            contentColor = if (allSelected) colors.onPrimary else colors.onSurface,
            shadowElevation = SkElevation.level1,
        ) {
            Row(Modifier.padding(horizontal = SportKidTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
                if (allSelected) Icon(painterResource(R.drawable.ic_home_check), contentDescription = null)
                Text(stringResource(R.string.home_filter_all), style = MaterialTheme.typography.labelLarge)
                SkPill(
                    total.toString(),
                    containerColor = if (allSelected) colors.onPrimary.copy(alpha = 0.2f) else colors.secondaryContainer,
                    contentColor = if (allSelected) colors.onPrimary else colors.onSecondaryContainer,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                )
            }
        }
        athletes.forEach { athlete ->
            val isSelected = selected == athlete.id
            Surface(
                modifier = Modifier
                    .height(44.dp)
                    .selectable(isSelected, role = Role.RadioButton, onClick = { onSelect(if (isSelected) null else athlete.id) }),
                shape = MaterialTheme.shapes.full,
                color = if (isSelected) colors.primary else colors.surfaceContainerLowest,
                contentColor = if (isSelected) colors.onPrimary else colors.onSurface,
                shadowElevation = SkElevation.level1,
            ) {
                Row(Modifier.padding(start = 6.dp, end = SportKidTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                    SkAvatar(
                        photo = athlete.photoRes?.let { painterResource(it) },
                        name = athlete.firstName,
                        size = 32.dp,
                        ringColor = colors.secondaryContainer,
                    ) {
                        Box(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .size(10.dp)
                                .background(colors.surfaceContainerLowest, CircleShape)
                                .padding(1.dp)
                                .background(athlete.color(), CircleShape),
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(athlete.firstName, style = MaterialTheme.typography.labelLarge)
                        Text(
                            stringResource(R.string.home_athlete_age, athlete.birthDate.ageOn(today)),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, lineHeight = 10.sp),
                            color = if (isSelected) colors.onPrimary.copy(alpha = 0.8f) else colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/** Banner de la semana: cuántos eventos vienen y cuándo es el próximo. */
@Composable
private fun WeekBanner(events: List<HomeEvent>, today: LocalDate, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val next = events.firstOrNull()
    val onBanner = colors.onPrimary
    Box(
        modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(colors.primary, lerp(colors.primary, colors.primaryContainer, 0.12f))), MaterialTheme.shapes.medium),
    ) {
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .offset(24.dp, 24.dp)
                .size(112.dp)
                .background(onBanner.copy(alpha = 0.05f), CircleShape),
        )
        Row(Modifier.padding(SportKidTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(end = SportKidTheme.spacing.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(painterResource(R.drawable.ic_home_stars), contentDescription = null, tint = colors.primaryContainer)
                    Text(
                        stringResource(R.string.home_week_overline).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.55.sp),
                        color = colors.primaryContainer,
                    )
                }
                Text(
                    if (next == null) stringResource(R.string.home_week_empty_title) else pluralStringResource(R.plurals.home_week_title, events.size, events.size),
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.titleMedium.copy(lineHeight = 20.sp),
                    color = onBanner,
                )
                Text(
                    if (next == null) stringResource(R.string.home_week_empty_body) else stringResource(R.string.home_week_next, whenLabel(next.date, today), next.time.label()),
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.primaryContainer.copy(alpha = 0.9f),
                )
            }
            SkIconCircle(
                icon = painterResource(R.drawable.ic_home_sports),
                contentDescription = null,
                size = 56.dp,
                containerColor = onBanner.copy(alpha = 0.1f),
                iconTint = onBanner,
            )
        }
    }
}

/** "hoy", "mañana", "este sábado" o "el Sáb 14 nov". */
@Composable
private fun whenLabel(date: LocalDate, today: LocalDate): String {
    val days = date.daysFrom(today)
    return when {
        days <= 0L -> stringResource(R.string.home_when_today)
        days == 1L -> stringResource(R.string.home_when_tomorrow)
        days < 7L -> stringResource(R.string.home_when_this_week, stringArrayResource(UiR.array.date_weekdays)[date.dayOfWeek.value % 7].lowercase())
        else -> stringResource(R.string.home_when_date, date.shortLabel())
    }
}

/** Encabezado de sección con icono opcional, contador y acción a la derecha. */
@Composable
private fun SectionTitle(title: String, modifier: Modifier = Modifier, icon: Painter? = null, count: Int? = null, trailing: @Composable () -> Unit = {}) {
    val colors = MaterialTheme.colorScheme
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        if (icon != null) Icon(icon, contentDescription = null, tint = colors.tertiary)
        Text(title, modifier = Modifier.semantics { heading() }, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
        if (count != null) {
            SkPill(
                count.toString(),
                containerColor = colors.secondaryContainer,
                contentColor = colors.secondary,
                contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
            )
        }
        Spacer(Modifier.weight(1f))
        trailing()
    }
}

/** O6. Próximos eventos: hasta 3 cards y el acceso a la agenda. */
@Composable
private fun Upcoming(events: List<HomeEvent>, athletes: Map<String, HomeAthlete>, onOpenAgenda: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.md)) {
        SectionTitle(stringResource(R.string.home_upcoming), count = events.size) {
            SkTextButton(
                stringResource(R.string.home_see_calendar),
                onClick = onOpenAgenda,
                modifier = Modifier.offset(x = SportKidTheme.spacing.lg),
                trailingIcon = painterResource(DsR.drawable.sk_ic_chevron_right),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (events.isEmpty()) {
            Text(stringResource(R.string.home_no_events), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        events.forEach { event -> UpcomingEventCard(event, athletes[event.athleteId], onClick = onOpenAgenda) }
    }
}

/** Card de evento próximo: deporte, hijo, título, fecha y lugar, costo y estado de pago. */
@Composable
private fun UpcomingEventCard(event: HomeEvent, athlete: HomeAthlete?, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val sport = event.sport.colors()
    SkCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, accentColor = sport.content, onClick = onClick) {
        Column(Modifier.padding(start = 4.dp), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.md)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(Modifier.size(44.dp).background(sport.container, MaterialTheme.shapes.medium), contentAlignment = Alignment.Center) {
                    Icon(painterResource(event.sport.iconRes), contentDescription = null, tint = sport.content)
                }
                Spacer(Modifier.width(SportKidTheme.spacing.md))
                Column(Modifier.weight(1f)) {
                    Row(Modifier.padding(bottom = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
                        Text(
                            stringResource(event.sport.labelRes).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.55.sp),
                            color = sport.content,
                        )
                        if (athlete != null) AthleteTag(athlete.firstName, athlete.color())
                    }
                    Text(event.title, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceContainerLow.copy(alpha = 0.6f), MaterialTheme.shapes.small)
                    .padding(start = 4.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                DetailLine(painterResource(UiR.drawable.ic_event_time), stringResource(R.string.home_event_when, event.date.shortLabel(), event.time.label()), primary = true)
                DetailLine(painterResource(UiR.drawable.ic_event_place), event.place, primary = false)
            }
            Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(event.costLabel, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                    Text(
                        stringResource(R.string.home_amount_mxn, event.costPesos.grouped()),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.onSurface,
                    )
                }
                PaymentStatusChip(event.payment)
            }
        }
    }
}

/** Renglón de fecha o lugar con su icono. */
@Composable
private fun DetailLine(icon: Painter, text: String, primary: Boolean) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp), tint = colors.onSurfaceVariant)
        Text(
            text,
            style = if (primary) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
            color = if (primary) colors.onSurface else colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** O7. Carrusel de últimos logros del mes. */
@Composable
private fun Achievements(items: List<HomeAchievement>, athletes: Map<String, HomeAthlete>, today: LocalDate, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.md)) {
        SectionTitle(stringResource(R.string.home_achievements), Modifier.padding(horizontal = SportKidTheme.spacing.lg), icon = painterResource(UiR.drawable.ic_medal)) {
            Text(
                stringArrayResource(UiR.array.date_months)[today.monthValue - 1],
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(items, key = { it.id }) { AchievementCard(it, athletes[it.athleteId]) }
        }
    }
}

/** Tarjeta de logro: icono por tipo, hijo, título, lugar y franja con el resultado. */
@Composable
private fun AchievementCard(item: HomeAchievement, athlete: HomeAthlete?) {
    val colors = MaterialTheme.colorScheme
    data class Style(val icon: Int, val tile: Color, val tint: Color, val footerIcon: Int, val footerBg: Color, val footerColor: Color)
    val style = when (item.kind) {
        AchievementKind.MEDAL -> Style(R.drawable.ic_home_trophy, colors.tertiaryContainer, colors.tertiary, R.drawable.ic_home_award, colors.surfaceContainerLow, colors.tertiary)
        AchievementKind.BELT -> Style(R.drawable.ic_home_belt_badge, colors.surfaceContainerHigh, colors.onSurfaceVariant, UiR.drawable.ic_medal, colors.surfaceContainerLow, colors.onSurfaceVariant)
        AchievementKind.RECORD -> Style(R.drawable.ic_home_timer, colors.primaryContainer, colors.primary, R.drawable.ic_home_trending, colors.primaryContainer.copy(alpha = 0.4f), colors.primary)
    }
    Surface(Modifier.width(256.dp).height(176.dp), shape = MaterialTheme.shapes.medium, color = colors.surfaceContainerLowest, shadowElevation = SkElevation.card) {
        Column(Modifier.padding(SportKidTheme.spacing.lg), verticalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SkIconCircle(icon = painterResource(style.icon), contentDescription = null, containerColor = style.tile, iconTint = style.tint)
                    Spacer(Modifier.weight(1f))
                    if (athlete != null) AthleteTag(athlete.firstName, athlete.color())
                }
                Text(
                    item.title,
                    modifier = Modifier.padding(top = SportKidTheme.spacing.sm),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(style.footerBg, MaterialTheme.shapes.small)
                    .padding(horizontal = 10.dp, vertical = SportKidTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(painterResource(style.footerIcon), contentDescription = null, tint = style.footerColor)
                Text(item.footer, style = MaterialTheme.typography.labelSmall, color = style.footerColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** O8. Gastos del mes: total, barra apilada por deporte y una tarjeta por deporte. */
@Composable
private fun ExpensesCard(spends: List<HomeSpend>, today: LocalDate, onOpenExpenses: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val total = spends.sumOf { it.pesos }
    val bySport = spends.groupBy { it.sport }.mapValues { (_, v) -> v.sumOf { it.pesos } }.toList().sortedByDescending { it.second }
    val month = stringArrayResource(UiR.array.date_months)[today.monthValue - 1].lowercase()
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = colors.surfaceContainerLowest, shadowElevation = SkElevation.card) {
        Column(Modifier.padding(SportKidTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(32.dp).background(colors.surfaceContainer, MaterialTheme.shapes.small), contentAlignment = Alignment.Center) {
                    Icon(painterResource(R.drawable.ic_home_wallet), contentDescription = null, tint = colors.onSurfaceVariant)
                }
                Spacer(Modifier.width(SportKidTheme.spacing.sm))
                Text(
                    stringResource(R.string.home_expenses_title, month),
                    modifier = Modifier.weight(1f).semantics { heading() },
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurface,
                )
                SkTextButton(
                    stringResource(R.string.home_see_detail),
                    onClick = onOpenExpenses,
                    modifier = Modifier.offset(x = SportKidTheme.spacing.lg),
                    trailingIcon = painterResource(R.drawable.ic_home_arrow_forward),
                    color = colors.primary,
                )
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    stringResource(R.string.home_amount, total.grouped()),
                    style = MaterialTheme.typography.displaySmall.copy(letterSpacing = (-0.9).sp),
                    color = colors.onSurface,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(R.string.home_currency),
                    modifier = Modifier.padding(bottom = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                SkPill(
                    pluralStringResource(R.plurals.home_activities, spends.size, spends.size),
                    modifier = Modifier.padding(bottom = 6.dp),
                    containerColor = colors.surfaceContainerHigh,
                    contentColor = colors.onSurfaceVariant,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            if (total > 0) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceContainer),
                ) {
                    bySport.forEach { (sport, pesos) ->
                        Box(Modifier.weight(pesos.toFloat()).fillMaxHeight().background(sport.colors().content))
                    }
                }
                Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
                    bySport.forEach { (sport, pesos) ->
                        Column(
                            Modifier
                                .weight(1f)
                                .background(colors.surfaceContainerLow.copy(alpha = 0.7f), MaterialTheme.shapes.small)
                                .padding(SportKidTheme.spacing.sm),
                        ) {
                            Row(Modifier.padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(Modifier.size(8.dp).background(sport.colors().content, CircleShape))
                                Text(stringResource(sport.labelRes), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Text(stringResource(R.string.home_amount, pesos.grouped()), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = colors.onSurface)
                            Text(
                                stringResource(R.string.home_percent_total, (pesos * 100f / total).roundToInt()),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                                color = colors.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@SkPreviews
@Composable
private fun HomeScreenPreview() = SportKidTheme {
    HomeScreen(onRegister = {}, onOpenAgenda = {}, onOpenExpenses = {}, today = LocalDate.of(2026, 11, 9))
}
