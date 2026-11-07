package com.invex.sportkid.feature.agenda

import com.invex.sportkid.core.ui.longLabel
import com.invex.sportkid.core.designsystem.component.SkAvatar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkChip
import com.invex.sportkid.core.designsystem.component.SkExtendedFab
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkSegmentedControl
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.ui.AthleteDot
import com.invex.sportkid.core.ui.EventCard
import com.invex.sportkid.core.ui.colors
import com.invex.sportkid.core.ui.iconRes
import com.invex.sportkid.core.ui.labelRes
import java.time.LocalDate
import java.time.YearMonth

private val FabClearance = 96.dp
private const val MillisPerDay = 86_400_000L

/**
 * P06. Agenda: control "Mes / Lista", filtros por hijo y deporte, calendario mensual (O9), los
 * compromisos del día elegido (O5) y el FAB "Nuevo evento".
 *
 * Es el contenido de la plantilla T1: la top bar (O1) y la bottom nav (O3) las pone `app`.
 */
@Composable
fun AgendaScreen(
    onNewEvent: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    state: AgendaState = rememberAgendaState(today),
    athletes: List<AgendaAthlete> = AgendaSamples.athletes,
    events: List<AgendaEvent> = remember(today) { AgendaSamples.events(YearMonth.from(today)) },
    onEventClick: (AgendaEvent) -> Unit = {},
    onSettlePayment: (AgendaEvent) -> Unit = {},
) {
    val athletesById = remember(athletes) { athletes.associateBy(AgendaAthlete::id) }
    val visible = events.filter(state::matches)
    val byDate = visible.groupBy(AgendaEvent::date)
    val listState = rememberLazyListState()
    val fabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val special = MaterialTheme.colorScheme.tertiary
    val athleteColors = athletes.associate { it.id to it.color() }

    Box(modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = SportKidTheme.spacing.sm, bottom = FabClearance),
        ) {
            item(key = "view") {
                SkSegmentedControl(
                    labels = listOf(stringResource(R.string.agenda_view_month), stringResource(R.string.agenda_view_list)),
                    icons = listOf(painterResource(R.drawable.ic_agenda_month), painterResource(R.drawable.ic_agenda_list)),
                    selectedIndex = state.view.ordinal,
                    onSelect = { state.view = AgendaView.entries[it] },
                    modifier = Modifier.padding(horizontal = SportKidTheme.spacing.lg),
                )
            }
            item(key = "filters") {
                Filters(state, athletes, Modifier.padding(top = SportKidTheme.spacing.md))
            }
            when (state.view) {
                AgendaView.Month -> {
                    item(key = "calendar") {
                        MonthCalendar(
                            month = state.month,
                            selectedDate = state.selectedDate,
                            today = today,
                            dotsFor = { date ->
                                val dayEvents = byDate[date].orEmpty()
                                buildList {
                                    dayEvents.filterNot(AgendaEvent::special)
                                        .mapNotNull { athleteColors[it.athleteId] }
                                        .distinct()
                                        .forEach(::add)
                                    if (dayEvents.any(AgendaEvent::special)) add(special)
                                }
                            },
                            eventCountFor = { byDate[it]?.size ?: 0 },
                            athletes = athletes,
                            onSelect = state::select,
                            onMonthChange = state::showMonth,
                            onToday = { state.select(today) },
                            onPickDate = { showDatePicker = true },
                            modifier = Modifier.padding(start = SportKidTheme.spacing.lg, end = SportKidTheme.spacing.lg, top = SportKidTheme.spacing.md),
                        )
                    }
                    dayItems(
                        date = state.selectedDate,
                        events = byDate[state.selectedDate].orEmpty(),
                        athletesById = athletesById,
                        onEventClick = onEventClick,
                        onSettlePayment = onSettlePayment,
                        emptyText = R.string.agenda_day_empty,
                    )
                }

                AgendaView.List -> {
                    val days = byDate.filterKeys { YearMonth.from(it) == state.month }.toSortedMap()
                    if (days.isEmpty()) {
                        item(key = "empty") { EmptyText(R.string.agenda_month_empty) }
                    }
                    days.forEach { (date, dayEvents) ->
                        dayItems(date, dayEvents, athletesById, onEventClick, onSettlePayment, showTip = false)
                    }
                }
            }
        }
        SkExtendedFab(
            text = stringResource(R.string.agenda_new_event),
            onClick = onNewEvent,
            expanded = fabExpanded,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(SportKidTheme.spacing.lg),
        )
    }

    if (showDatePicker) {
        MonthPickerDialog(
            initial = state.selectedDate,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                state.select(it)
                showDatePicker = false
            },
        )
    }
}

/** Encabezado del día, sus [events] y el recordatorio clave si alguno lo tiene. */
private fun LazyListScope.dayItems(
    date: LocalDate,
    events: List<AgendaEvent>,
    athletesById: Map<String, AgendaAthlete>,
    onEventClick: (AgendaEvent) -> Unit,
    onSettlePayment: (AgendaEvent) -> Unit,
    showTip: Boolean = true,
    emptyText: Int? = null,
) {
    item(key = "day-$date") { DayHeader(date, events.size) }
    if (events.isEmpty() && emptyText != null) {
        item(key = "empty-$date") { EmptyText(emptyText) }
    }
    items(events.sortedBy(AgendaEvent::start), key = { "event-${it.id}-$date" }) { event ->
        val athlete = athletesById.getValue(event.athleteId)
        EventCard(
            sport = event.sport,
            title = event.title,
            time = event.timeLabel(),
            athleteName = athlete.name,
            athleteColor = athlete.color(),
            subtitle = event.subtitle,
            place = event.place,
            costLabel = event.costLabel,
            cost = event.costPesos?.let { amountLabel(it) },
            paymentStatus = event.payment,
            onClick = { onEventClick(event) },
            onSettlePayment = { onSettlePayment(event) },
            modifier = Modifier.padding(start = SportKidTheme.spacing.lg, end = SportKidTheme.spacing.lg, top = 14.dp),
        )
    }
    if (showTip) {
        events.firstOrNull { it.tip != null }?.let { event ->
            item(key = "tip-$date") {
                DayTip(
                    title = stringResource(R.string.agenda_tip_title, athletesById.getValue(event.athleteId).name),
                    body = event.tip.orEmpty(),
                    modifier = Modifier.padding(start = SportKidTheme.spacing.lg, end = SportKidTheme.spacing.lg, top = 18.dp),
                )
            }
        }
    }
}

@Composable
private fun Filters(state: AgendaState, athletes: List<AgendaAthlete>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item(key = "all") {
            SkChip(
                text = stringResource(R.string.agenda_filter_all),
                selected = state.athleteId == null,
                onClick = { state.athleteId = null },
                icon = painterResource(R.drawable.ic_agenda_all),
            )
        }
        items(athletes, key = { "athlete-${it.id}" }) { athlete ->
            SkChip(
                text = athlete.name,
                selected = state.athleteId == athlete.id,
                onClick = { state.athleteId = if (state.athleteId == athlete.id) null else athlete.id },
                leading = {
                    AthleteDot(
                        athlete.color(),
                        size = 10.dp,
                        modifier = Modifier.border(2.dp, colors.surfaceContainerLowest.copy(alpha = 0.6f), CircleShape),
                    )
                    athlete.avatarRes?.let { SkAvatar(photo = painterResource(it), size = 16.dp) }
                },
            )
        }
        item(key = "divider") {
            Box(Modifier.padding(horizontal = SportKidTheme.spacing.xs).width(1.dp).height(16.dp).background(colors.outlineVariant))
        }
        items(Sport.entries, key = { "sport-${it.name}" }) { sport ->
            val sportColor = sport.colors().content
            SkChip(
                text = stringResource(sport.labelRes),
                selected = sport in state.sports,
                onClick = { state.toggleSport(sport) },
                icon = painterResource(sport.iconRes),
                iconSize = 14.dp,
                iconTint = sportColor,
                leading = { AthleteDot(sportColor) },
            )
        }
    }
}

@Composable
private fun DayHeader(date: LocalDate, count: Int) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = SportKidTheme.spacing.lg, end = SportKidTheme.spacing.lg, top = SportKidTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AthleteDot(colors.primary, size = 10.dp)
        Spacer(Modifier.width(SportKidTheme.spacing.sm))
        Text(
            date.longLabel(),
            modifier = Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.titleMedium,
            color = colors.onSurface,
        )
        if (count > 0) SkPill(
            commitmentsLabel(count),
            containerColor = colors.primaryContainer,
            contentColor = colors.onPrimaryContainer,
        )
    }
}

@Composable
private fun DayTip(title: String, body: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, color = colors.surfaceContainer) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            SkIconCircle(
                icon = painterResource(R.drawable.ic_agenda_tip),
                contentDescription = null,
                containerColor = colors.primaryContainer,
                iconTint = colors.primary,
            )
            Spacer(Modifier.width(SportKidTheme.spacing.md))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                Text(body, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EmptyText(text: Int) {
    Text(
        stringResource(text),
        modifier = Modifier.fillMaxWidth().padding(horizontal = SportKidTheme.spacing.lg, vertical = SportKidTheme.spacing.xl),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthPickerDialog(initial: LocalDate, onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit) {
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initial.toEpochDay() * MillisPerDay)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let { onConfirm(LocalDate.ofEpochDay(it / MillisPerDay)) } ?: onDismiss()
            }) { Text(stringResource(R.string.action_accept)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    ) { DatePicker(state = pickerState) }
}

@SkPreviews
@Composable
private fun AgendaScreenPreview() = SportKidTheme {
    val today = LocalDate.of(2025, 11, AgendaSamples.FEATURED_DAY)
    Surface(color = MaterialTheme.colorScheme.surface) {
        AgendaScreen(onNewEvent = {}, today = today)
    }
}
