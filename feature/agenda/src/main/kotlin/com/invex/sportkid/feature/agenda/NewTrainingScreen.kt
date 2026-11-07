package com.invex.sportkid.feature.agenda

import com.invex.sportkid.core.designsystem.component.SkAvatar
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkButton
import com.invex.sportkid.core.designsystem.component.SkButtonSize
import com.invex.sportkid.core.designsystem.component.SkChip
import com.invex.sportkid.core.designsystem.component.SkDetailTopBar
import com.invex.sportkid.core.designsystem.component.SkFormLabel
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkMoneyField
import com.invex.sportkid.core.designsystem.component.SkNavigationIcon
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkTextArea
import com.invex.sportkid.core.designsystem.component.SkTextField
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.ui.iconRes
import com.invex.sportkid.core.ui.labelRes
import java.time.LocalDate
import java.time.LocalTime
import com.invex.sportkid.core.ui.R as UiR

private const val MillisPerDay = 86_400_000L

/** Lugar sugerido: [value] se escribe en el campo; [label] es lo que muestra el chip. */
private data class PlaceSuggestion(val label: String, val value: String, @DrawableRes val icon: Int)

/**
 * P07. Formulario "Nuevo entrenamiento" (plantilla T3): deportista, disciplina, fecha, hora y
 * duración, lugar con sugerencias, costo y pago, recordatorio familiar y notas compartidas.
 *
 * Guardar está habilitado con deportista, disciplina y lugar.
 */
@Composable
fun NewTrainingScreen(
    onClose: () -> Unit,
    onSave: (TrainingDraft) -> Unit,
    modifier: Modifier = Modifier,
    athletes: List<AgendaAthlete> = AgendaSamples.athletes,
    state: NewTrainingState = rememberNewTrainingState(
        NewTrainingState.empty(athleteId = athletes.firstOrNull()?.id, sport = athletes.firstOrNull()?.sports?.firstOrNull()),
    ),
    onAttach: () -> Unit = {},
) {
    val save = { onSave(state.toDraft()) }
    Scaffold(
        modifier = modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            SkDetailTopBar(
                title = stringResource(R.string.training_title),
                onBack = onClose,
                navigationIcon = SkNavigationIcon.Close,
            ) {
                SkButton(
                    text = stringResource(R.string.training_save),
                    onClick = save,
                    enabled = state.canSave,
                    size = SkButtonSize.Small,
                    modifier = Modifier.padding(end = SportKidTheme.spacing.sm),
                )
            }
        },
        bottomBar = { SaveBar(enabled = state.canSave, onSave = save) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = SportKidTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xl),
        ) {
            val section = Modifier.padding(horizontal = SportKidTheme.spacing.lg)
            AthleteSection(athletes, state.athleteId, onSelect = { state.athleteId = it }, modifier = section)
            SportSection(state.sport, onSelect = { state.sport = it }, modifier = section)
            ScheduleCard(state, modifier = section)
            PlaceSection(state.place, onPlaceChange = { state.place = it })
            CostCard(state, modifier = section)
            ReminderSection(state.reminder, onSelect = { state.reminder = it }, modifier = section)
            NotesSection(state.notes, onNotesChange = { state.notes = it }, onAttach = onAttach, modifier = section)
        }
    }
}

@Composable
private fun AthleteSection(
    athletes: List<AgendaAthlete>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        SkFormLabel(stringResource(R.string.training_athlete), required = true) {
            Text(
                stringResource(R.string.training_athlete_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
            athletes.forEach { athlete ->
                AthleteCard(
                    athlete = athlete,
                    selected = athlete.id == selectedId,
                    onClick = { onSelect(athlete.id) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun AthleteCard(athlete: AgendaAthlete, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .alpha(if (selected) 1f else 0.75f)
            .semantics { role = Role.RadioButton },
        shape = MaterialTheme.shapes.medium,
        color = colors.surfaceContainerLowest,
        shadowElevation = SkElevation.card,
    ) {
        Box {
            Column(Modifier.padding(SportKidTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.md)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SkAvatar(
                        photo = athlete.avatarRes?.let { painterResource(it) },
                        name = athlete.name,
                        size = 48.dp,
                        ringColor = athlete.color(),
                    )
                    Spacer(Modifier.width(SportKidTheme.spacing.sm))
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            athlete.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        SkPill(
                            stringResource(R.string.training_age, athlete.ageYears),
                            containerColor = colors.primaryContainer,
                            contentColor = colors.primary,
                            contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    athlete.sports.firstOrNull()?.let {
                        Icon(painterResource(it.iconRes), contentDescription = null, modifier = Modifier.size(12.dp), tint = colors.secondary)
                        Spacer(Modifier.width(SportKidTheme.spacing.xs))
                    }
                    Text(
                        athlete.sports.map { stringResource(it.labelRes) }.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (selected) {
                SkIconCircle(
                    icon = painterResource(UiR.drawable.ic_sport_chip_check),
                    contentDescription = null,
                    size = 24.dp,
                    containerColor = colors.primary,
                    iconTint = colors.onPrimary,
                    modifier = Modifier.align(Alignment.TopEnd).padding(SportKidTheme.spacing.sm),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SportSection(selected: Sport?, onSelect: (Sport) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        SkFormLabel(stringResource(R.string.training_sport), required = true)
        FlowRow(
            Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
        ) {
            Sport.entries.forEach { sport ->
                SkChip(
                    text = stringResource(sport.labelRes),
                    selected = sport == selected,
                    onClick = { onSelect(sport) },
                    icon = painterResource(sport.iconRes),
                    iconSize = 16.dp,
                    height = 40.dp,
                    horizontalPadding = SportKidTheme.spacing.lg,
                    selectedContainerColor = colors.tertiary,
                    selectedContentColor = colors.onTertiary,
                    contentColor = colors.secondary,
                    shadowElevation = 0.dp,
                )
            }
        }
    }
}

@Composable
private fun ScheduleCard(state: NewTrainingState, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    var picker by rememberSaveable { mutableStateOf<String?>(null) }
    FormCard(modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.md)) {
            PickerField(
                label = stringResource(R.string.training_date),
                value = state.date.shortLabel(),
                icon = painterResource(R.drawable.ic_training_date),
                onClick = { picker = "date" },
                modifier = Modifier.weight(1f),
            )
            PickerField(
                label = stringResource(R.string.training_time),
                value = state.start.label(),
                icon = painterResource(R.drawable.ic_training_time),
                onClick = { picker = "time" },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.training_duration),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                color = colors.secondary,
            )
            Text(
                stringResource(R.string.training_duration_value, state.durationMinutes),
                style = MaterialTheme.typography.labelSmall,
                color = colors.primary,
            )
        }
        Spacer(Modifier.height(SportKidTheme.spacing.sm))
        val labels = listOf(
            R.string.training_duration_45, R.string.training_duration_60,
            R.string.training_duration_90, R.string.training_duration_120,
        )
        Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
            TrainingDurations.forEachIndexed { index, minutes ->
                SkChip(
                    text = stringResource(labels[index]),
                    selected = state.durationMinutes == minutes,
                    onClick = { state.durationMinutes = minutes },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                    horizontalPadding = SportKidTheme.spacing.xs,
                    containerColor = colors.surfaceContainer,
                    contentColor = colors.onSurfaceVariant,
                )
            }
        }
    }
    when (picker) {
        "date" -> TrainingDatePicker(state.date, onDismiss = { picker = null }) {
            state.date = it
            picker = null
        }
        "time" -> TrainingTimePicker(state.start, onDismiss = { picker = null }) {
            state.start = it
            picker = null
        }
    }
}

@Composable
private fun PickerField(label: String, value: String, icon: Painter, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.secondary)
        Row(
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(colors.surfaceContainerLow)
                .clickable(role = Role.Button, onClickLabel = label, onClick = onClick)
                .padding(horizontal = SportKidTheme.spacing.md, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = colors.primary)
            Spacer(Modifier.width(SportKidTheme.spacing.sm))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PlaceSection(place: String, onPlaceChange: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val suggestions = listOf(
        PlaceSuggestion(stringResource(R.string.training_place_recent, "Doyang Central"), "Doyang Central Polideportivo Norte", R.drawable.ic_training_recent),
        PlaceSuggestion("Club Acuático Olímpico", "Club Acuático Olímpico Los Pinos", R.drawable.ic_training_pool),
        PlaceSuggestion("Pista Revolución", "Pista Revolución", R.drawable.ic_training_track),
    )
    Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        val section = Modifier.padding(horizontal = SportKidTheme.spacing.lg)
        SkFormLabel(stringResource(R.string.training_place), modifier = section, required = true) {
            Icon(painterResource(R.drawable.ic_training_place_label), contentDescription = null, tint = colors.secondary)
        }
        SkTextField(
            value = place,
            onValueChange = onPlaceChange,
            modifier = section,
            placeholder = stringResource(R.string.training_place_placeholder),
            leading = { Icon(painterResource(R.drawable.ic_training_place), contentDescription = null, tint = colors.primary) },
            trailing = if (place.isNotEmpty()) {
                {
                    IconButton(onClick = { onPlaceChange("") }) {
                        Icon(
                            painterResource(R.drawable.ic_training_clear),
                            contentDescription = stringResource(R.string.training_place_clear),
                            tint = colors.outline,
                        )
                    }
                }
            } else {
                null
            },
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
        ) {
            items(suggestions, key = PlaceSuggestion::value) { suggestion ->
                SkChip(
                    text = suggestion.label,
                    selected = place == suggestion.value,
                    onClick = { onPlaceChange(suggestion.value) },
                    icon = painterResource(suggestion.icon),
                    height = 28.dp,
                    textStyle = MaterialTheme.typography.labelSmall,
                    selectedContainerColor = colors.primaryContainer,
                    selectedContentColor = colors.onPrimaryContainer,
                    contentColor = colors.secondary,
                )
            }
        }
    }
}

@Composable
private fun CostCard(state: NewTrainingState, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    FormCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkIconCircle(
                icon = painterResource(R.drawable.ic_training_cost),
                contentDescription = null,
                size = 32.dp,
                containerColor = colors.secondaryContainer,
            )
            Spacer(Modifier.width(SportKidTheme.spacing.sm))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.training_cost), style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                Text(stringResource(R.string.training_cost_hint), style = MaterialTheme.typography.bodySmall, color = colors.secondary)
            }
            SkMoneyField(value = state.cost, onValueChange = { state.cost = it })
        }
        Spacer(Modifier.height(SportKidTheme.spacing.lg))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(colors.surfaceContainerLow)
                .toggleable(value = state.paid, role = Role.Switch, onValueChange = { state.paid = it })
                .padding(SportKidTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f).padding(end = SportKidTheme.spacing.sm)) {
                Text(stringResource(R.string.training_paid_question), style = MaterialTheme.typography.labelMedium, color = colors.onSurface)
                Text(
                    stringResource(if (state.paid) R.string.training_paid_yes else R.string.training_paid_no),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (state.paid) colors.primary else colors.tertiary,
                )
            }
            Switch(checked = state.paid, onCheckedChange = null)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderSection(selected: TrainingReminder, onSelect: (TrainingReminder) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val labels = mapOf(
        TrainingReminder.None to R.string.training_reminder_none,
        TrainingReminder.OneHour to R.string.training_reminder_hour,
        TrainingReminder.OneDay to R.string.training_reminder_day,
        TrainingReminder.TwoDays to R.string.training_reminder_two_days,
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        SkFormLabel(stringResource(R.string.training_reminder), icon = painterResource(R.drawable.ic_training_reminder))
        FlowRow(
            Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
        ) {
            TrainingReminder.entries.forEach { reminder ->
                val isSelected = reminder == selected
                SkChip(
                    text = stringResource(labels.getValue(reminder)),
                    selected = isSelected,
                    onClick = { onSelect(reminder) },
                    icon = if (isSelected && reminder != TrainingReminder.None) painterResource(R.drawable.ic_training_reminder_on) else null,
                    height = 36.dp,
                    horizontalPadding = 14.dp,
                    containerColor = colors.surfaceContainer,
                    contentColor = colors.secondary,
                )
            }
        }
    }
}

@Composable
private fun NotesSection(notes: String, onNotesChange: (String) -> Unit, onAttach: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        SkFormLabel(stringResource(R.string.training_notes)) {
            Icon(painterResource(R.drawable.ic_training_notes), contentDescription = null, tint = colors.secondary)
        }
        SkTextArea(
            value = notes,
            onValueChange = onNotesChange,
            placeholder = stringResource(R.string.training_notes_placeholder),
        ) {
            Icon(painterResource(R.drawable.ic_training_family), contentDescription = null, tint = colors.primary)
            Spacer(Modifier.width(SportKidTheme.spacing.xs))
            Text(
                stringResource(R.string.training_notes_shared),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondary,
            )
            IconButton(onClick = onAttach, modifier = Modifier.size(40.dp)) {
                Icon(
                    painterResource(R.drawable.ic_training_attach),
                    contentDescription = stringResource(R.string.training_notes_attach),
                    tint = colors.primary,
                )
            }
        }
    }
}

@Composable
private fun FormCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = SkElevation.card,
    ) {
        Column(Modifier.padding(SportKidTheme.spacing.lg)) { content() }
    }
}

@Composable
private fun SaveBar(enabled: Boolean, onSave: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier
                .navigationBarsPadding()
                .padding(horizontal = SportKidTheme.spacing.lg, vertical = SportKidTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
        ) {
            SkButton(
                text = stringResource(R.string.training_save_cta),
                onClick = onSave,
                enabled = enabled,
                leadingIcon = painterResource(R.drawable.ic_training_save),
                size = SkButtonSize.Medium,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                stringResource(R.string.training_sync_note),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrainingDatePicker(initial: LocalDate, onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrainingTimePicker(initial: LocalTime, onDismiss: () -> Unit, onConfirm: (LocalTime) -> Unit) {
    val pickerState = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(pickerState.hour, pickerState.minute)) }) {
                Text(stringResource(R.string.action_accept))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
        text = { TimePicker(state = pickerState) },
    )
}

/** Estado exacto del diseño de Figma (Mateo, Taekwondo, 1.5 h, Doyang Central, $250). */
@SkPreviews
@Composable
private fun NewTrainingFigmaPreview() = SportKidTheme {
    NewTrainingScreen(
        onClose = {},
        onSave = {},
        state = rememberNewTrainingState(
            TrainingDraft(
                athleteId = AgendaSamples.MATEO,
                sport = Sport.TAEKWONDO,
                date = LocalDate.of(2025, 11, 15),
                start = LocalTime.of(10, 0),
                durationMinutes = 90,
                place = "Doyang Central Polideportivo Norte",
                costPesos = 250,
                paid = false,
                reminder = TrainingReminder.OneDay,
                notes = "",
            ),
        ),
    )
}
