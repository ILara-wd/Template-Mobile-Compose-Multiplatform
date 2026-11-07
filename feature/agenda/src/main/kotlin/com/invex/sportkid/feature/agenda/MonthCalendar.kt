package com.invex.sportkid.feature.agenda

import com.invex.sportkid.core.ui.R as UiR
import com.invex.sportkid.core.ui.label
import com.invex.sportkid.core.ui.longLabel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkChip
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full
import com.invex.sportkid.core.ui.AthleteDot
import java.time.LocalDate
import java.time.YearMonth

private const val MaxDotsPerDay = 3

/**
 * O9. Calendario mensual: encabezado con mes, "Hoy" y flechas; grid de 7 columnas (inicia en
 * domingo) con hasta 3 puntos por día y la leyenda de colores.
 *
 * Puntos: color del hijo por cada evento normal y `tertiary` si hay un torneo o evento especial.
 * Seleccionado: círculo `primary`. Hoy: anillo `primary`.
 */
@Composable
internal fun MonthCalendar(
    month: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    dotsFor: @Composable (LocalDate) -> List<Color>,
    eventCountFor: (LocalDate) -> Int,
    athletes: List<AgendaAthlete>,
    onSelect: (LocalDate) -> Unit,
    onMonthChange: (YearMonth) -> Unit,
    onToday: () -> Unit,
    onPickDate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.surfaceContainerLowest,
        shadowElevation = SkElevation.card,
    ) {
        Column(Modifier.padding(SportKidTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
            CalendarHeader(month, onMonthChange, onToday, onPickDate)
            WeekdayHeader()
            DaysGrid(month, selectedDate, today, dotsFor, eventCountFor, onSelect)
            Legend(athletes, Modifier.padding(top = 14.dp))
        }
    }
}

@Composable
private fun CalendarHeader(
    month: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    onToday: () -> Unit,
    onPickDate: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val pickLabel = stringResource(R.string.agenda_pick_month)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(
            Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable(role = Role.Button, onClickLabel = pickLabel, onClick = onPickDate)
                .semantics { heading() }
                .padding(vertical = SportKidTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(month.label(), style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
            Icon(painterResource(R.drawable.ic_agenda_month_picker), contentDescription = null, tint = colors.primary)
        }
        Spacer(Modifier.weight(1f))
        SkChip(
            text = stringResource(R.string.agenda_today),
            selected = false,
            onClick = onToday,
            height = 24.dp,
            textStyle = MaterialTheme.typography.labelSmall,
            containerColor = colors.secondaryContainer,
            contentColor = colors.onSecondaryContainer,
        )
        IconButton(onClick = { onMonthChange(month.minusMonths(1)) }, modifier = Modifier.size(40.dp)) {
            Icon(painterResource(R.drawable.ic_agenda_prev), stringResource(R.string.agenda_prev_month), tint = colors.onSurfaceVariant)
        }
        IconButton(onClick = { onMonthChange(month.plusMonths(1)) }, modifier = Modifier.size(40.dp)) {
            Icon(painterResource(R.drawable.ic_agenda_next), stringResource(R.string.agenda_next_month), tint = colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun WeekdayHeader() {
    Row(Modifier.fillMaxWidth().padding(top = SportKidTheme.spacing.sm).clearAndSetSemantics {}) {
        stringArrayResource(UiR.array.date_weekday_initials).forEach {
            Text(
                it,
                modifier = Modifier.weight(1f).padding(vertical = SportKidTheme.spacing.xs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DaysGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    today: LocalDate,
    dotsFor: @Composable (LocalDate) -> List<Color>,
    eventCountFor: (LocalDate) -> Int,
    onSelect: (LocalDate) -> Unit,
) {
    val first = month.atDay(1)
    val leading = first.dayOfWeek.value % 7 // domingo = 0
    val weeks = (leading + month.lengthOfMonth() + 6) / 7
    val start = first.minusDays(leading.toLong())
    Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
        repeat(weeks) { week ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { dayIndex ->
                    val date = start.plusDays((week * 7 + dayIndex).toLong())
                    DayCell(
                        date = date,
                        inMonth = YearMonth.from(date) == month,
                        selected = date == selectedDate,
                        isToday = date == today,
                        dots = if (YearMonth.from(date) == month) dotsFor(date) else emptyList(),
                        eventCount = eventCountFor(date),
                        onClick = { onSelect(date) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    selected: Boolean,
    isToday: Boolean,
    dots: List<Color>,
    eventCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val description = "${date.longLabel()}, ${commitmentsLabel(eventCount)}"
    Column(
        modifier
            .height(40.dp)
            .then(
                if (inMonth) {
                    Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable(role = Role.Button, onClick = onClick)
                        .clearAndSetSemantics {
                            contentDescription = description
                            this.selected = selected
                        }
                } else {
                    Modifier.alpha(0.4f).clearAndSetSemantics {}
                },
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val number = date.dayOfMonth.toString()
        when {
            selected -> Box(
                Modifier.size(28.dp).background(colors.primary, MaterialTheme.shapes.full),
                contentAlignment = Alignment.Center,
            ) {
                Text(number, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = colors.onPrimary)
            }

            isToday && inMonth -> Box(
                Modifier.size(28.dp).border(1.dp, colors.primary, MaterialTheme.shapes.full),
                contentAlignment = Alignment.Center,
            ) {
                Text(number, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = colors.primary)
            }

            else -> Text(
                number,
                style = MaterialTheme.typography.bodySmall,
                color = if (inMonth) colors.onSurface else colors.outline,
            )
        }
        if (dots.isNotEmpty()) {
            val dotSize: Dp = if (selected) 6.dp else 4.dp
            Row(
                Modifier.padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(if (selected) 4.dp else 2.dp),
            ) {
                dots.take(MaxDotsPerDay).forEach { AthleteDot(it, size = dotSize) }
            }
        }
    }
}

@Composable
private fun Legend(athletes: List<AgendaAthlete>, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        athletes.forEach { LegendItem(it.color(), it.name) }
        LegendItem(MaterialTheme.colorScheme.tertiary, stringResource(R.string.agenda_legend_special))
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AthleteDot(color)
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
