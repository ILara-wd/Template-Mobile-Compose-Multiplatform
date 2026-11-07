package com.invex.sportkid.feature.swimming

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkAvatar
import com.invex.sportkid.core.designsystem.component.SkButton
import com.invex.sportkid.core.designsystem.component.SkButtonSize
import com.invex.sportkid.core.designsystem.component.SkCard
import com.invex.sportkid.core.designsystem.component.SkChip
import com.invex.sportkid.core.designsystem.component.SkDetailTopBar
import com.invex.sportkid.core.designsystem.component.SkFieldLabel
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkNavigationIcon
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkSelectField
import com.invex.sportkid.core.designsystem.component.SkTextField
import com.invex.sportkid.core.designsystem.component.SkTimeMaskTransformation
import com.invex.sportkid.core.designsystem.component.SkTonalButton
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full
import com.invex.sportkid.core.designsystem.theme.metric
import com.invex.sportkid.core.model.MedalResult
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.model.SwimStyle
import com.invex.sportkid.core.ui.AthleteDot
import com.invex.sportkid.core.ui.AttachedPhotoRow
import com.invex.sportkid.core.ui.FeeRow
import com.invex.sportkid.core.ui.FormCard
import com.invex.sportkid.core.ui.FormDateTimeFields
import com.invex.sportkid.core.ui.FormSaveBar
import com.invex.sportkid.core.ui.PhotoUploadArea
import com.invex.sportkid.core.ui.ageOn
import com.invex.sportkid.core.ui.colors
import com.invex.sportkid.core.ui.iconRes
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
import com.invex.sportkid.core.ui.R as UiR

/** Tipo de alberca de la competencia. */
enum class PoolLength { Short25, Long50 }

/** Resultado de una prueba: podio o finalista. */
enum class RaceOutcome { Gold, Silver, Bronze, Finalist }

/** Prueba capturada. [timeDigits] son los dígitos del campo `mm:ss.cc` (máx. 6). */
@Immutable
data class RaceDraft(val style: SwimStyle, val distance: Int, val timeDigits: String, val outcome: RaceOutcome?) {
    val time: SwimTime? get() = SwimTime.fromDigits(timeDigits)
}

/** Datos capturados en P13 al guardar. */
@Immutable
data class SwimMeetDraft(
    val name: String,
    val venue: String,
    val pool: PoolLength,
    val date: LocalDate,
    val time: LocalTime,
    val feePesos: Int?,
    val paid: Boolean,
    val races: List<RaceDraft>,
)

/** Estado de P13 con la lista dinámica de pruebas (O20). Sobrevive a rotación y muerte de proceso. */
@Stable
class SwimMeetFormState(initial: SwimMeetDraft) {
    var name by mutableStateOf(initial.name)
    var venue by mutableStateOf(initial.venue)
    var pool by mutableStateOf(initial.pool)
    var date by mutableStateOf(initial.date)
    var time by mutableStateOf(initial.time)
    var fee by mutableStateOf(initial.feePesos?.toString().orEmpty())
    var paid by mutableStateOf(initial.paid)
    val races = mutableStateListOf<RaceDraft>().apply { addAll(initial.races) }

    val canSave: Boolean get() = name.isNotBlank() && venue.isNotBlank() && races.isNotEmpty() && races.all { it.time != null }

    fun addRace() {
        races += RaceDraft(SwimStyle.FREESTYLE, 50, "", null)
    }

    fun toDraft() = SwimMeetDraft(name.trim(), venue.trim(), pool, date, time, fee.toIntOrNull(), paid, races.toList())

    companion object {
        fun empty(today: LocalDate = LocalDate.now()) = SwimMeetDraft(
            name = "", venue = "", pool = PoolLength.Short25, date = today.plusDays(7), time = LocalTime.of(8, 0),
            feePesos = null, paid = false, races = listOf(RaceDraft(SwimStyle.FREESTYLE, 50, "", null)),
        )

        val Saver = listSaver<SwimMeetFormState, Any?>(
            save = { s ->
                listOf(
                    s.name, s.venue, s.pool.name, s.date.toEpochDay(), s.time.toSecondOfDay(), s.fee, s.paid,
                    s.races.map { "${it.style.name}|${it.distance}|${it.timeDigits}|${it.outcome?.name.orEmpty()}" },
                )
            },
            restore = {
                @Suppress("UNCHECKED_CAST")
                SwimMeetFormState(
                    SwimMeetDraft(
                        name = it[0] as String,
                        venue = it[1] as String,
                        pool = PoolLength.valueOf(it[2] as String),
                        date = LocalDate.ofEpochDay(it[3] as Long),
                        time = LocalTime.ofSecondOfDay((it[4] as Int).toLong()),
                        feePesos = (it[5] as String).toIntOrNull(),
                        paid = it[6] as Boolean,
                        races = (it[7] as List<String>).map { row ->
                            val (style, distance, digits, outcome) = row.split("|")
                            RaceDraft(SwimStyle.valueOf(style), distance.toInt(), digits, outcome.takeIf(String::isNotEmpty)?.let(RaceOutcome::valueOf))
                        },
                    ),
                )
            },
        )
    }
}

@Composable
fun rememberSwimMeetFormState(initial: SwimMeetDraft = SwimMeetFormState.empty()): SwimMeetFormState =
    rememberSaveable(saver = SwimMeetFormState.Saver) { SwimMeetFormState(initial) }

/**
 * P13. Registrar competencia acuática (plantilla T3): datos del evento con tipo de alberca,
 * cuota, lista dinámica de pruebas con tiempo, récord personal y podio (O20), y foto opcional.
 */
@Composable
fun SwimMeetFormScreen(
    athlete: SwimAthlete,
    onClose: () -> Unit,
    onSave: (SwimMeetDraft) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    state: SwimMeetFormState = rememberSwimMeetFormState(SwimMeetFormState.empty(today)),
    bestTimes: Map<SwimStyle, Map<Int, SwimTime>> = SwimSamples.bestTimes,
    onPickPhoto: () -> Unit = {},
    attachedPhoto: Painter? = null,
    attachedPhotoName: String? = null,
) {
    val save = { onSave(state.toDraft()) }
    Scaffold(
        modifier = modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            SkDetailTopBar(title = stringResource(R.string.meet_title), onBack = onClose, navigationIcon = SkNavigationIcon.Close) {
                SkButton(
                    stringResource(R.string.swim_save),
                    onClick = save,
                    enabled = state.canSave,
                    size = SkButtonSize.Small,
                    modifier = Modifier.padding(end = SportKidTheme.spacing.sm),
                )
            }
        },
        bottomBar = {
            FormSaveBar(
                text = stringResource(R.string.meet_save_cta),
                icon = painterResource(UiR.drawable.ic_form_save),
                enabled = state.canSave,
                onSave = save,
                note = stringResource(R.string.meet_sync_note, athlete.firstName),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SportKidTheme.spacing.lg, vertical = SportKidTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xl),
        ) {
            AthleteStrip(athlete, today)
            EventSection(state)
            RacesSection(state, bestTimes)
            PhotoSection(onPickPhoto, attachedPhoto, attachedPhotoName)
        }
    }
}

@Composable
private fun AthleteStrip(athlete: SwimAthlete, today: LocalDate) {
    val colors = MaterialTheme.colorScheme
    val swim = Sport.SWIMMING.colors().content
    Row(
        Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.large)
            .padding(SportKidTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkAvatar(photo = athlete.photoRes?.let { painterResource(it) }, name = athlete.fullName, size = 48.dp, ringColor = athlete.color()) {
            AthleteDot(swim, size = 14.dp, modifier = Modifier.align(Alignment.BottomEnd))
        }
        Spacer(Modifier.width(SportKidTheme.spacing.sm))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(athlete.fullName, style = MaterialTheme.typography.titleMedium, color = colors.onSurface, maxLines = 1)
                Spacer(Modifier.width(SportKidTheme.spacing.xs))
                SkPill(
                    stringResource(UiR.string.athlete_age, athlete.birthDate.ageOn(today)),
                    containerColor = colors.surfaceContainerHighest,
                    contentColor = colors.onSurfaceVariant,
                    contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(Sport.SWIMMING.iconRes), contentDescription = null, modifier = Modifier.size(14.dp), tint = swim)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.meet_sport_club, athlete.club), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1)
            }
        }
        SkIconCircle(
            painterResource(UiR.drawable.ic_result_participation),
            contentDescription = null,
            size = 32.dp,
            containerColor = colors.secondaryContainer,
            iconTint = colors.primary,
        )
    }
}

@Composable
private fun SectionHeader(title: String, icon: Int? = null, subtitle: String? = null, trailing: @Composable () -> Unit = {}) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, tint = colors.primary)
            Spacer(Modifier.width(SportKidTheme.spacing.xs))
        }
        Column(Modifier.weight(1f)) {
            Text(title, modifier = Modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) }
        }
        trailing()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EventSection(state: SwimMeetFormState) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg)) {
        SectionHeader(stringResource(R.string.meet_event_section), icon = R.drawable.ic_swim_event)
        FormCard {
            SkTextField(
                value = state.name,
                onValueChange = { state.name = it },
                label = stringResource(R.string.meet_name),
                placeholder = stringResource(R.string.meet_name_placeholder),
                leading = { Icon(painterResource(R.drawable.ic_swim_trophy), contentDescription = null, tint = colors.onSurfaceVariant) },
            )
            Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
                SkTextField(
                    value = state.venue,
                    onValueChange = { state.venue = it },
                    label = stringResource(R.string.meet_venue),
                    placeholder = stringResource(R.string.meet_venue_placeholder),
                    leading = { Icon(painterResource(UiR.drawable.ic_event_place), contentDescription = null, tint = colors.onSurfaceVariant) },
                )
                FlowRow(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
                    PoolLength.entries.forEach { pool ->
                        val selected = state.pool == pool
                        SkChip(
                            text = stringResource(if (pool == PoolLength.Short25) R.string.meet_pool_short else R.string.meet_pool_long),
                            selected = selected,
                            onClick = { state.pool = pool },
                            icon = if (selected) painterResource(UiR.drawable.ic_sport_chip_check) else null,
                            iconSize = 10.dp,
                            height = 26.dp,
                            textStyle = MaterialTheme.typography.labelSmall,
                            selectedContainerColor = colors.primaryContainer,
                            selectedContentColor = colors.onPrimaryContainer,
                            containerColor = colors.surfaceContainerHigh,
                            contentColor = colors.onSurfaceVariant,
                        )
                    }
                }
            }
            FormDateTimeFields(
                date = state.date,
                onDateChange = { state.date = it },
                time = state.time,
                onTimeChange = { state.time = it },
                dateLabel = stringResource(R.string.meet_date),
                timeLabel = stringResource(R.string.meet_time),
            )
            FeeRow(
                label = stringResource(R.string.meet_fee),
                cost = state.fee,
                onCostChange = { state.fee = it },
                paid = state.paid,
                onPaidChange = { state.paid = it },
                paidLabel = stringResource(R.string.meet_fee_paid),
            )
        }
    }
}

@Composable
private fun RacesSection(state: SwimMeetFormState, bestTimes: Map<SwimStyle, Map<Int, SwimTime>>) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        SectionHeader(stringResource(R.string.meet_races_title), subtitle = stringResource(R.string.meet_races_hint)) {
            SkPill(
                pluralStringResource(R.plurals.meet_races_count, state.races.size, state.races.size),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            )
        }
        state.races.forEachIndexed { index, race ->
            RaceCard(
                number = index + 1,
                race = race,
                previousBest = bestTimes[race.style]?.get(race.distance),
                canDelete = state.races.size > 1,
                onChange = { state.races[index] = it },
                onDelete = { state.races.removeAt(index) },
            )
        }
        SkTonalButton(
            stringResource(R.string.meet_add_race),
            onClick = state::addRace,
            icon = painterResource(R.drawable.ic_swim_add),
            containerColor = colors.surfaceContainer,
            contentColor = colors.primary,
            size = SkButtonSize.Medium,
            shape = MaterialTheme.shapes.full,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** M22. Prueba editable: estilo, distancia, tiempo con récord personal y podio. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RaceCard(
    number: Int,
    race: RaceDraft,
    previousBest: SwimTime?,
    canDelete: Boolean,
    onChange: (RaceDraft) -> Unit,
    onDelete: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val time = race.time
    // Mejora contra la marca previa: (marca previa, centésimas ganadas).
    val improvement = if (time != null && previousBest != null && time < previousBest) previousBest to previousBest.centis - time.centis else null
    val isRecord = improvement != null
    SkCard(accentColor = if (isRecord) colors.tertiary else SportKidTheme.sportColors.swimming) {
        Column(Modifier.padding(start = SportKidTheme.spacing.xs), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.meet_race, number), style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                if (isRecord) {
                    Spacer(Modifier.width(SportKidTheme.spacing.xs))
                    SkPill(
                        stringResource(R.string.meet_new_record),
                        containerColor = colors.tertiaryContainer,
                        contentColor = colors.onTertiaryContainer,
                        icon = painterResource(R.drawable.ic_swim_record),
                    )
                }
                Spacer(Modifier.weight(1f))
                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                        Icon(painterResource(R.drawable.ic_swim_delete), stringResource(R.string.meet_race_delete, number), tint = colors.error)
                    }
                }
            }
            StyleSelect(race.style) { onChange(race.copy(style = it)) }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SkFieldLabel(stringResource(R.string.meet_distance))
                Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
                    SwimDistances.forEach { d ->
                        SkChip(
                            text = stringResource(R.string.swim_distance, d),
                            selected = race.distance == d,
                            onClick = { onChange(race.copy(distance = d)) },
                            modifier = Modifier.weight(1f),
                            height = 36.dp,
                            horizontalPadding = SportKidTheme.spacing.xs,
                            textStyle = MaterialTheme.typography.labelSmall,
                            containerColor = colors.surfaceContainerHigh,
                            contentColor = colors.onSurfaceVariant,
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SkTextField(
                    value = race.timeDigits,
                    onValueChange = { onChange(race.copy(timeDigits = it.filter(Char::isDigit).take(6))) },
                    label = stringResource(R.string.meet_time_label),
                    placeholder = "00:00.00",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = SkTimeMaskTransformation,
                    textStyle = MaterialTheme.typography.metric,
                    leading = { Icon(painterResource(R.drawable.ic_swim_timer), contentDescription = null, tint = colors.primary) },
                )
                improvement?.let { (previous, diff) ->
                    SkPill(
                        stringResource(R.string.meet_record_diff, String.format(Locale.ROOT, "%d.%02d", diff / 100, diff % 100), previous.format()),
                        containerColor = colors.tertiaryContainer,
                        contentColor = colors.onTertiaryContainer,
                        icon = painterResource(R.drawable.ic_swim_trend),
                        contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SkFieldLabel(stringResource(R.string.meet_result))
                FlowRow(
                    Modifier.selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs),
                ) {
                    RaceOutcome.entries.forEach { outcome ->
                        val medal = when (outcome) {
                            RaceOutcome.Gold -> MedalResult.GOLD
                            RaceOutcome.Silver -> MedalResult.SILVER
                            RaceOutcome.Bronze -> MedalResult.BRONZE
                            RaceOutcome.Finalist -> null
                        }
                        val dot: Color? = medal?.colors()?.accent
                        SkChip(
                            text = stringResource(outcome.labelRes()),
                            selected = race.outcome == outcome,
                            onClick = { onChange(race.copy(outcome = if (race.outcome == outcome) null else outcome)) },
                            height = 30.dp,
                            textStyle = MaterialTheme.typography.labelSmall,
                            leading = dot?.let { { AthleteDot(it, size = 10.dp) } },
                            selectedContainerColor = colors.primaryContainer,
                            selectedContentColor = colors.onPrimaryContainer,
                            containerColor = colors.surfaceContainerHigh,
                            contentColor = colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun RaceOutcome.labelRes(): Int = when (this) {
    RaceOutcome.Gold -> R.string.meet_result_gold
    RaceOutcome.Silver -> R.string.meet_result_silver
    RaceOutcome.Bronze -> R.string.meet_result_bronze
    RaceOutcome.Finalist -> R.string.meet_result_finalist
}

@Composable
private fun StyleSelect(style: SwimStyle, onChange: (SwimStyle) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        SkSelectField(
            value = stringResource(style.labelRes()),
            onClick = { expanded = true },
            label = stringResource(R.string.meet_style),
            emphasized = true,
            trailing = { Icon(painterResource(UiR.drawable.ic_form_dropdown), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SwimStyle.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes())) },
                    leadingIcon = { Icon(painterResource(option.iconRes()), contentDescription = null, tint = SportKidTheme.sportColors.swimming) },
                    onClick = {
                        onChange(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun PhotoSection(onPickPhoto: () -> Unit, attachedPhoto: Painter?, attachedPhotoName: String?) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        SectionHeader(stringResource(R.string.meet_photo_title)) {
            Text(stringResource(R.string.meet_photo_optional), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
        FormCard {
            if (attachedPhoto != null && attachedPhotoName != null) {
                AttachedPhotoRow(
                    photo = attachedPhoto,
                    name = attachedPhotoName,
                    detail = stringResource(R.string.meet_photo_ready),
                    modifier = Modifier.background(colors.surfaceContainerLow, MaterialTheme.shapes.medium).padding(SportKidTheme.spacing.sm),
                )
            }
            PhotoUploadArea(
                title = stringResource(R.string.meet_photo_upload),
                description = stringResource(R.string.meet_photo_hint),
                onPick = onPickPhoto,
            )
        }
    }
}

/** Estado de Figma: Torneo Estatal, piscina corta, $850 pagado, 50 m libre con récord y 50 m dorso. */
@SkPreviews
@Composable
private fun SwimMeetFormPreview() = SportKidTheme {
    SwimMeetFormScreen(
        athlete = SwimSamples.mateo,
        onClose = {},
        onSave = {},
        today = LocalDate.of(2025, 11, 10),
        state = rememberSwimMeetFormState(
            SwimMeetDraft(
                name = "Torneo Estatal Infantil de Curso Corto",
                venue = "Centro Acuático CODE Metropolitano",
                pool = PoolLength.Short25,
                date = LocalDate.of(2025, 11, 29),
                time = LocalTime.of(8, 0),
                feePesos = 850,
                paid = true,
                races = listOf(
                    RaceDraft(SwimStyle.FREESTYLE, 50, "003885", RaceOutcome.Gold),
                    RaceDraft(SwimStyle.BACKSTROKE, 50, "004420", RaceOutcome.Silver),
                ),
            ),
        ),
        attachedPhoto = painterResource(R.drawable.img_sample_swim_podium),
        attachedPhotoName = "podio_estatal_mateo.jpg",
    )
}
