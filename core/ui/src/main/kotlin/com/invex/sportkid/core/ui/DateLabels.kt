package com.invex.sportkid.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.util.Locale

/*
 * Formato de fechas es-MX con los nombres de `dates.xml`, sin depender del idioma del
 * dispositivo. Domingo es el índice 0 (como en el calendario).
 */

private val LocalDate.weekdayIndex: Int get() = dayOfWeek.value % 7

/** "Noviembre 2025". */
@Composable
fun YearMonth.label(): String =
    stringResource(R.string.date_month_year, stringArrayResource(R.array.date_months)[monthValue - 1], year)

/** "Viernes 14 de noviembre". */
@Composable
fun LocalDate.longLabel(): String = stringResource(
    R.string.date_day_long,
    stringArrayResource(R.array.date_weekdays)[weekdayIndex],
    dayOfMonth,
    stringArrayResource(R.array.date_months)[monthValue - 1].lowercase(),
)

/** "Sáb 14 nov". */
@Composable
fun LocalDate.shortLabel(): String = stringResource(
    R.string.date_short,
    stringArrayResource(R.array.date_weekdays_short)[weekdayIndex],
    dayOfMonth,
    stringArrayResource(R.array.date_months_short)[monthValue - 1],
)

/** "Sáb 15 nov, 2025". */
@Composable
fun LocalDate.shortLabelWithYear(): String = stringResource(
    R.string.date_short_year,
    stringArrayResource(R.array.date_weekdays_short)[weekdayIndex],
    dayOfMonth,
    stringArrayResource(R.array.date_months_short)[monthValue - 1],
    year,
)

/** "15 mayo 2017". */
@Composable
fun LocalDate.fullLabel(): String =
    stringResource(R.string.date_full, dayOfMonth, stringArrayResource(R.array.date_months)[monthValue - 1].lowercase(), year)

/** "15 may 2015". */
@Composable
fun LocalDate.compactLabel(): String =
    stringResource(R.string.date_full, dayOfMonth, stringArrayResource(R.array.date_months_short)[monthValue - 1].lowercase(), year)

/** "Ene 2024". */
@Composable
fun LocalDate.monthYearShortLabel(): String =
    stringResource(
        R.string.date_month_year,
        stringArrayResource(R.array.date_months_short)[monthValue - 1].replaceFirstChar { it.titlecase(EsMx) },
        year,
    )

/** "10:00 hrs". */
@Composable
fun LocalTime.label(): String = stringResource(R.string.date_time, hour, minute)

/** "09:00 - 11:30 hrs". */
@Composable
fun timeRangeLabel(start: LocalTime, end: LocalTime): String =
    stringResource(R.string.date_time_range, start.hour, start.minute, end.hour, end.minute)

/** Días naturales de [from] a esta fecha (negativo si ya pasó). */
fun LocalDate.daysFrom(from: LocalDate): Long = ChronoUnit.DAYS.between(from, this)

/** Edad cumplida en [today]. */
fun LocalDate.ageOn(today: LocalDate): Int = ChronoUnit.YEARS.between(this, today).toInt()

private val EsMx = Locale("es", "MX")
