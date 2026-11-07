package com.invex.sportkid.feature.athletics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkAvatar
import com.invex.sportkid.core.designsystem.component.SkButton
import com.invex.sportkid.core.designsystem.component.SkButtonSize
import com.invex.sportkid.core.designsystem.component.SkChip
import com.invex.sportkid.core.designsystem.component.SkDetailTopBar
import com.invex.sportkid.core.designsystem.component.SkFieldLabel
import com.invex.sportkid.core.designsystem.component.SkDurationMaskTransformation
import com.invex.sportkid.core.designsystem.component.SkNavigationIcon
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkSegmentedControl
import com.invex.sportkid.core.designsystem.component.SkTextField
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.metric
import com.invex.sportkid.core.model.MedalResult
import com.invex.sportkid.core.model.Sport
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
import com.invex.sportkid.core.ui.labelRes
import java.time.LocalDate
import java.time.LocalTime
import com.invex.sportkid.core.ui.R as UiR

/** Datos capturados en P15 al guardar. [distance] en metros; [result] `PENDING` = por definir. */
@Immutable
data class RaceDraft(
    val name: String,
    val venue: String,
    val date: LocalDate,
    val time: LocalTime,
    val feePesos: Int?,
    val paid: Boolean,
    val distance: Int?,
    val type: RaceType,
    val timeDigits: String,
    val result: MedalResult?,
) {
    val raceTime: RaceTime? get() = RaceTime.fromDigits(timeDigits)
}

/** Estado de P15. Sobrevive a rotación y muerte de proceso. */
@Stable
class RaceFormState(initial: RaceDraft) {
    var name by mutableStateOf(initial.name)
    var venue by mutableStateOf(initial.venue)
    var date by mutableStateOf(initial.date)
    var time by mutableStateOf(initial.time)
    var fee by mutableStateOf(initial.feePesos?.toString().orEmpty())
    var paid by mutableStateOf(initial.paid)

    /** Distancia rápida elegida, o `null` con "Otra" (se usa [customDistance]). */
    var distance by mutableStateOf(initial.distance?.takeIf { it in RaceDistances })
    var customDistance by mutableStateOf(initial.distance?.takeIf { it !in RaceDistances }?.toString().orEmpty())
    var type by mutableStateOf(initial.type)
    var timeDigits by mutableStateOf(initial.timeDigits)
    var result by mutableStateOf(initial.result)

    val meters: Int? get() = distance ?: customDistance.toIntOrNull()?.takeIf { it > 0 }
    val canSave: Boolean get() = name.isNotBlank() && venue.isNotBlank() && meters != null && RaceTime.fromDigits(timeDigits) != null

    fun toDraft() = RaceDraft(name.trim(), venue.trim(), date, time, fee.toIntOrNull(), paid, meters, type, timeDigits, result)

    companion object {
        fun empty(today: LocalDate = LocalDate.now()) = RaceDraft(
            name = "", venue = "", date = today, time = LocalTime.of(8, 0), feePesos = null, paid = false,
            distance = 1000, type = RaceType.Flat, timeDigits = "", result = null,
        )

        val Saver = listSaver<RaceFormState, Any?>(
            save = { s ->
                listOf(
                    s.name, s.venue, s.date.toEpochDay(), s.time.toSecondOfDay(), s.fee, s.paid,
                    s.distance ?: -1, s.customDistance, s.type.name, s.timeDigits, s.result?.name.orEmpty(),
                )
            },
            restore = {
                RaceFormState(
                    RaceDraft(
                        name = it[0] as String,
                        venue = it[1] as String,
                        date = LocalDate.ofEpochDay(it[2] as Long),
                        time = LocalTime.ofSecondOfDay((it[3] as Int).toLong()),
                        feePesos = (it[4] as String).toIntOrNull(),
                        paid = it[5] as Boolean,
                        distance = (it[6] as Int).takeIf { d -> d > 0 } ?: (it[7] as String).toIntOrNull(),
                        type = RaceType.valueOf(it[8] as String),
                        timeDigits = it[9] as String,
                        result = (it[10] as String).takeIf(String::isNotEmpty)?.let(MedalResult::valueOf),
                    ),
                )
            },
        )
    }
}

@Composable
fun rememberRaceFormState(initial: RaceDraft = RaceFormState.empty()): RaceFormState =
    rememberSaveable(saver = RaceFormState.Saver) { RaceFormState(initial) }

/**
 * P15. Registrar carrera (plantilla T3): deportista, datos de la carrera con cuota, distancia,
 * modalidad, tiempo oficial con detección de récord personal, posición y foto de la medalla.
 */
@Composable
fun RaceFormScreen(
    athlete: AthleticsAthlete,
    onClose: () -> Unit,
    onSave: (RaceDraft) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    state: RaceFormState = rememberRaceFormState(RaceFormState.empty(today)),
    previousBest: (distance: Int, type: RaceType) -> RaceTime? = AthleticsSamples::previousBest,
    onSwitchAthlete: (() -> Unit)? = null,
    onPickPhoto: () -> Unit = {},
    onCamera: () -> Unit = {},
    attachedPhoto: Painter? = null,
    attachedPhotoName: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    val save = { onSave(state.toDraft()) }
    Scaffold(
        modifier = modifier.imePadding(),
        containerColor = colors.surface,
        topBar = {
            SkDetailTopBar(title = stringResource(R.string.race_title), onBack = onClose, navigationIcon = SkNavigationIcon.Close) {
                SkButton(
                    stringResource(R.string.ath_save),
                    onClick = save,
                    enabled = state.canSave,
                    size = SkButtonSize.Small,
                    containerColor = colors.tertiary,
                    contentColor = colors.onTertiary,
                    modifier = Modifier.padding(end = SportKidTheme.spacing.sm),
                )
            }
        },
        bottomBar = {
            FormSaveBar(
                text = stringResource(R.string.race_save_cta),
                icon = painterResource(R.drawable.ic_ath_save),
                enabled = state.canSave,
                onSave = save,
                note = stringResource(R.string.race_sync_note, athlete.firstName),
                containerColor = colors.tertiary,
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
            AthleteBar(athlete, today, onSwitchAthlete)
            RaceDataSection(state)
            DetailsSection(state, athlete.firstName, previousBest)
            PhotoSection(athlete.firstName, onPickPhoto, onCamera, attachedPhoto, attachedPhotoName)
        }
    }
}

@Composable
private fun AthleteBar(athlete: AthleticsAthlete, today: LocalDate, onSwitchAthlete: (() -> Unit)?) {
    val colors = MaterialTheme.colorScheme
    val accent = Sport.ATHLETICS.colors().content
    Row(
        Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.large)
            .padding(SportKidTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkAvatar(photo = athlete.photoRes?.let { painterResource(it) }, name = athlete.fullName, size = 48.dp, ringColor = athlete.color()) {
            AthleteDot(accent, size = 14.dp, modifier = Modifier.align(Alignment.BottomEnd))
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
                Icon(painterResource(Sport.ATHLETICS.iconRes), contentDescription = null, modifier = Modifier.size(14.dp), tint = accent)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.race_sport_club, athlete.club), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1)
            }
        }
        onSwitchAthlete?.let {
            IconButton(onClick = it) {
                Icon(painterResource(R.drawable.ic_ath_swap), contentDescription = stringResource(R.string.race_switch_athlete), tint = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        AthleteDot(colors.tertiary, size = 8.dp)
        Spacer(Modifier.width(SportKidTheme.spacing.sm))
        Text(title, modifier = Modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
    }
}

@Composable
private fun RaceDataSection(state: RaceFormState) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        SectionHeader(stringResource(R.string.race_data_section))
        FormCard {
            SkTextField(
                value = state.name,
                onValueChange = { state.name = it },
                label = stringResource(R.string.race_name),
                placeholder = stringResource(R.string.race_name_placeholder),
                leading = { Icon(painterResource(R.drawable.ic_ath_trophy), contentDescription = null, tint = colors.onSurfaceVariant) },
            )
            SkTextField(
                value = state.venue,
                onValueChange = { state.venue = it },
                label = stringResource(R.string.race_venue),
                placeholder = stringResource(R.string.race_venue_placeholder),
                leading = { Icon(painterResource(UiR.drawable.ic_event_place), contentDescription = null, tint = colors.onSurfaceVariant) },
            )
            FormDateTimeFields(
                date = state.date,
                onDateChange = { state.date = it },
                time = state.time,
                onTimeChange = { state.time = it },
                dateLabel = stringResource(R.string.race_date),
                timeLabel = stringResource(R.string.race_time),
            )
            FeeRow(
                label = stringResource(R.string.race_fee),
                cost = state.fee,
                onCostChange = { state.fee = it },
                paid = state.paid,
                onPaidChange = { state.paid = it },
                paidLabel = stringResource(R.string.race_fee_paid),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsSection(state: RaceFormState, firstName: String, previousBest: (Int, RaceType) -> RaceTime?) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        SectionHeader(stringResource(R.string.race_details_section))
        FormCard {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SkFieldLabel(stringResource(R.string.race_distance))
                FlowRow(
                    Modifier.selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs),
                ) {
                    val other = state.distance == null
                    (RaceDistances.map<Int, Int?> { it } + null).forEach { d ->
                        val selected = if (d == null) other else state.distance == d
                        SkChip(
                            text = d?.let { distanceLabel(it) } ?: stringResource(R.string.race_distance_other),
                            selected = selected,
                            onClick = { state.distance = d },
                            icon = if (selected) painterResource(UiR.drawable.ic_sport_chip_check) else null,
                            iconSize = 10.dp,
                            height = 32.dp,
                            horizontalPadding = 10.dp,
                            textStyle = MaterialTheme.typography.labelMedium,
                            selectedContainerColor = colors.tertiary,
                            selectedContentColor = colors.onTertiary,
                            containerColor = colors.surfaceContainerHigh,
                            contentColor = colors.onSurfaceVariant,
                        )
                    }
                }
                if (state.distance == null) {
                    SkTextField(
                        value = state.customDistance,
                        onValueChange = { state.customDistance = it.filter(Char::isDigit).take(5) },
                        label = stringResource(R.string.race_distance_custom),
                        placeholder = "800",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SkFieldLabel(stringResource(R.string.race_modality))
                SkSegmentedControl(
                    labels = RaceType.entries.map { it.label(long = true) },
                    selectedIndex = state.type.ordinal,
                    onSelect = { state.type = RaceType.entries[it] },
                    icons = listOf(painterResource(Sport.ATHLETICS.iconRes), painterResource(R.drawable.ic_ath_hurdles)),
                    selectedContentColor = colors.tertiary,
                    textStyle = MaterialTheme.typography.labelMedium,
                )
            }
            TimeField(state, firstName, previousBest)
            ResultPicker(state.result) { state.result = it }
        }
    }
}

@Composable
private fun TimeField(state: RaceFormState, firstName: String, previousBest: (Int, RaceType) -> RaceTime?) {
    val colors = MaterialTheme.colorScheme
    val time = RaceTime.fromDigits(state.timeDigits)
    val meters = state.meters
    val best = meters?.let { previousBest(it, state.type) }
    val gain = if (time != null && best != null && time < best) best.seconds - time.seconds else null
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SkTextField(
            value = state.timeDigits,
            onValueChange = { state.timeDigits = it.filter(Char::isDigit).take(6) },
            label = stringResource(R.string.race_official_time),
            placeholder = stringResource(R.string.race_time_format),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = SkDurationMaskTransformation,
            textStyle = MaterialTheme.typography.metric,
            leading = { Icon(painterResource(R.drawable.ic_ath_timer), contentDescription = null, tint = colors.tertiary) },
        )
        if (gain != null && meters != null) {
            SkPill(
                stringResource(R.string.race_new_record, distanceLabel(meters), firstName, gainLabel(gain)),
                containerColor = colors.tertiaryContainer,
                contentColor = colors.onTertiaryContainer,
                icon = painterResource(R.drawable.ic_ath_trophy),
                contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 4.dp),
            )
        }
    }
}

/** "48 s" o "1:05 min". */
private fun gainLabel(seconds: Int): String = if (seconds < 60) "$seconds s" else "%d:%02d min".format(seconds / 60, seconds % 60)

/** Posición: podio en 3 columnas; "Finalista" y "Por definir" debajo. */
@Composable
private fun ResultPicker(selected: MedalResult?, onSelect: (MedalResult?) -> Unit) {
    Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SkFieldLabel(stringResource(R.string.race_result))
        Row(horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
            listOf(MedalResult.GOLD, MedalResult.SILVER, MedalResult.BRONZE).forEachIndexed { i, medal ->
                ResultTile(
                    title = stringResource(R.string.race_place, i + 1),
                    subtitle = stringResource(medal.labelRes()),
                    medal = medal,
                    selected = selected == medal,
                    onClick = { onSelect(if (selected == medal) null else medal) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
            ResultTile(
                title = stringResource(R.string.race_finalist),
                subtitle = null,
                medal = MedalResult.PARTICIPATION,
                selected = selected == MedalResult.PARTICIPATION,
                onClick = { onSelect(if (selected == MedalResult.PARTICIPATION) null else MedalResult.PARTICIPATION) },
                modifier = Modifier.weight(1f),
            )
            ResultTile(
                title = stringResource(R.string.race_pending),
                subtitle = null,
                medal = MedalResult.PENDING,
                selected = selected == MedalResult.PENDING,
                onClick = { onSelect(if (selected == MedalResult.PENDING) null else MedalResult.PENDING) },
                modifier = Modifier.weight(2f),
            )
        }
    }
}

@Composable
private fun ResultTile(title: String, subtitle: String?, medal: MedalResult, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val medalColors = medal.colors()
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier
            .height(64.dp)
            .clip(shape)
            .background(if (selected) colors.tertiaryContainer else colors.surfaceContainerLow)
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.tertiary else colors.outlineVariant, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = SportKidTheme.spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painterResource(medal.iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp).background(medalColors.container, CircleShape).padding(2.dp),
            tint = medalColors.accent,
        )
        Text(
            title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = if (selected) colors.onTertiaryContainer else colors.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        subtitle?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1) }
    }
}

@Composable
private fun PhotoSection(firstName: String, onPickPhoto: () -> Unit, onCamera: () -> Unit, attachedPhoto: Painter?, attachedPhotoName: String?) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        SectionHeader(stringResource(R.string.race_photo_section))
        FormCard {
            if (attachedPhoto != null && attachedPhotoName != null) {
                AttachedPhotoRow(
                    photo = attachedPhoto,
                    name = attachedPhotoName,
                    detail = stringResource(R.string.race_photo_ready),
                    modifier = Modifier.background(colors.surfaceContainerLow, MaterialTheme.shapes.medium).padding(SportKidTheme.spacing.sm),
                )
            }
            PhotoUploadArea(
                title = stringResource(R.string.race_photo_title, firstName),
                description = stringResource(R.string.race_photo_hint),
                onPick = onPickPhoto,
                onCamera = onCamera,
            )
        }
    }
}

/** Estado de Figma: Carrera Infantil 3K, $350 pagado, 3 K lisa en 14:52 con récord y oro. */
@SkPreviews
@Composable
private fun RaceFormPreview() = SportKidTheme {
    RaceFormScreen(
        athlete = AthleticsSamples.sofia,
        onClose = {},
        onSave = {},
        today = LocalDate.of(2025, 11, 10),
        state = rememberRaceFormState(
            RaceDraft(
                name = "Carrera Infantil 3K Primavera",
                venue = "Pista Olímpica CODE",
                date = LocalDate.of(2025, 11, 22),
                time = LocalTime.of(8, 0),
                feePesos = 350,
                paid = true,
                distance = 3000,
                type = RaceType.Flat,
                timeDigits = "1404",
                result = MedalResult.GOLD,
            ),
        ),
        attachedPhoto = painterResource(R.drawable.img_sample_ath_podium),
        attachedPhotoName = "podio_3k_sofia.jpg",
    )
}
