package com.invex.sportkid.feature.athlete

import com.invex.sportkid.core.designsystem.component.SkAvatar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkButton
import com.invex.sportkid.core.designsystem.component.SkCard
import com.invex.sportkid.core.designsystem.component.SkColorSwatchPicker
import com.invex.sportkid.core.designsystem.component.SkDetailTopBar
import com.invex.sportkid.core.designsystem.component.SkFieldLabel
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkProfileButton
import com.invex.sportkid.core.designsystem.component.SkSelectField
import com.invex.sportkid.core.designsystem.component.SkTextField
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.Belt
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.ui.BeltSelectField
import com.invex.sportkid.core.ui.SportSectionCard
import com.invex.sportkid.core.ui.SportToggleCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * P04. Alta de deportista.
 *
 * @param photo foto elegida, o `null` para mostrar el placeholder.
 * @param onPickPhoto abre cámara o galería (pendiente de integrar el Photo Picker).
 */
@Composable
fun CreateAthleteScreen(
    onBack: () -> Unit,
    onSave: (AthleteDraft) -> Unit,
    modifier: Modifier = Modifier,
    state: CreateAthleteState = rememberCreateAthleteState(),
    photo: Painter? = null,
    onPickPhoto: () -> Unit = {},
    onProfileClick: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val spacing = SportKidTheme.spacing
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        // El CTA fijo sube con el teclado para que "Guardar" siga visible.
        modifier = modifier.imePadding(),
        containerColor = colors.surface,
        topBar = {
            SkDetailTopBar(title = stringResource(R.string.athlete_create_title), onBack = onBack) {
                SkProfileButton(onClick = onProfileClick)
            }
        },
        bottomBar = {
            Surface(color = colors.surface.copy(alpha = 0.95f)) {
                SkButton(
                    text = stringResource(R.string.athlete_save),
                    onClick = { onSave(state.toDraft()) },
                    enabled = state.canSave,
                    leadingIcon = painterResource(R.drawable.ic_athlete_save),
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(spacing.lg)
                        .fillMaxWidth(),
                )
            }
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = spacing.lg)
                .padding(top = spacing.sm, bottom = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.xl),
        ) {
            StepHeader()
            PhotoCard(photo = photo, ringColor = SportKidTheme.sportColors.athlete.all[state.colorIndex], onClick = onPickPhoto)

            Column(verticalArrangement = Arrangement.spacedBy(spacing.lg)) {
                SkTextField(
                    value = state.name,
                    onValueChange = { state.name = it },
                    label = stringResource(R.string.athlete_name_label),
                    labelIcon = painterResource(R.drawable.ic_athlete_name),
                    placeholder = stringResource(R.string.athlete_name_placeholder),
                )
                SkSelectField(
                    value = state.birthDateMillis?.let(::formatBirthDate).orEmpty(),
                    onClick = { showDatePicker = true },
                    label = stringResource(R.string.athlete_birthdate_label),
                    labelIcon = painterResource(R.drawable.ic_athlete_birthdate),
                    placeholder = stringResource(R.string.athlete_birthdate_placeholder),
                    helperText = stringResource(R.string.athlete_birthdate_helper),
                    trailing = {
                        Icon(
                            painterResource(R.drawable.ic_athlete_calendar),
                            contentDescription = stringResource(R.string.athlete_birthdate_open),
                            tint = colors.primary,
                        )
                    },
                )
            }

            ColorCard(selectedIndex = state.colorIndex, onSelect = { state.colorIndex = it })

            SportsSelector(selected = state.sports, onToggle = state::toggleSport)

            if (Sport.TAEKWONDO in state.sports) {
                SportSectionCard(Sport.TAEKWONDO, stringResource(R.string.athlete_tkd_section)) {
                    BeltSelectField(
                        belt = state.belt,
                        onBeltChange = { state.belt = it },
                        label = stringResource(R.string.athlete_tkd_belt),
                        placeholder = stringResource(R.string.athlete_tkd_belt_placeholder),
                        trailingIcon = painterResource(R.drawable.ic_athlete_chevron_down),
                    )
                    SkTextField(
                        value = state.tkdSchool,
                        onValueChange = { state.tkdSchool = it },
                        label = stringResource(R.string.athlete_tkd_school),
                        placeholder = stringResource(R.string.athlete_tkd_school_placeholder),
                    )
                }
            }
            if (Sport.SWIMMING in state.sports) {
                SportSectionCard(Sport.SWIMMING, stringResource(R.string.athlete_swimming_section)) {
                    SkTextField(
                        value = state.swimmingClub,
                        onValueChange = { state.swimmingClub = it },
                        label = stringResource(R.string.athlete_swimming_club),
                        placeholder = stringResource(R.string.athlete_swimming_club_placeholder),
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        BirthDatePickerDialog(
            initialMillis = state.birthDateMillis,
            onConfirm = {
                state.birthDateMillis = it
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun StepHeader() {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SkPill(
                stringResource(R.string.athlete_step, 1, 1),
                contentColor = colors.primary,
                overline = true,
                emphasized = true,
                contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.md, vertical = 3.dp),
            )
            Text(
                stringResource(R.string.athlete_step_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        SkIconCircle(
            icon = painterResource(R.drawable.ic_athlete_step),
            contentDescription = null,
            containerColor = colors.secondaryContainer,
            iconTint = colors.secondary,
            shadowElevation = SkElevation.level1,
            modifier = Modifier.padding(start = SportKidTheme.spacing.sm),
        )
    }
}

/** Card de foto: anillo con el color elegido, botón de cámara y texto de ayuda. */
@Composable
private fun PhotoCard(photo: Painter?, ringColor: Color, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    SkCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(192.dp),
        shape = MaterialTheme.shapes.medium,
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-40).dp)
                    .size(112.dp)
                    .background(colors.primaryContainer.copy(alpha = 0.3f), CircleShape),
            )
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-32).dp, y = 32.dp)
                    .size(96.dp)
                    .background(colors.tertiaryContainer.copy(alpha = 0.3f), CircleShape),
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Avatar(photo, ringColor)
                Text(
                    stringResource(R.string.athlete_photo_title),
                    modifier = Modifier.padding(top = SportKidTheme.spacing.md),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                )
                Text(
                    stringResource(R.string.athlete_photo_hint),
                    modifier = Modifier.padding(top = SportKidTheme.spacing.xs),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun Avatar(photo: Painter?, ringColor: Color) {
    val colors = MaterialTheme.colorScheme
    SkAvatar(
        photo = photo,
        size = 96.dp,
        ringBrush = Brush.linearGradient(listOf(ringColor, lerp(ringColor, Color.White, 0.55f))),
        ringWidth = SportKidTheme.spacing.xs,
        placeholderColor = colors.surfaceContainerLowest,
        placeholderContentColor = colors.outline,
        shadowElevation = SkElevation.level2,
    ) {
        SkIconCircle(
            icon = painterResource(R.drawable.ic_athlete_camera),
            contentDescription = stringResource(R.string.athlete_photo_change),
            size = 32.dp,
            containerColor = colors.tertiary,
            iconTint = colors.onTertiary,
            shadowElevation = SkElevation.level2,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}

@Composable
private fun ColorCard(selectedIndex: Int, onSelect: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val names = stringArrayResource(R.array.athlete_color_names).toList()
    SkCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_athlete_palette), contentDescription = null, tint = colors.primary)
                Text(
                    stringResource(R.string.athlete_color_title),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = SportKidTheme.spacing.sm),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                )
                SkPill(
                    names[selectedIndex],
                    contentColor = colors.primary,
                    emphasized = true,
                    contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                )
            }
            Text(
                stringResource(R.string.athlete_color_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
            SkColorSwatchPicker(
                colors = SportKidTheme.sportColors.athlete.all,
                labels = names,
                selectedIndex = selectedIndex,
                onSelect = onSelect,
                modifier = Modifier.padding(top = SportKidTheme.spacing.xs),
            )
        }
    }
}

@Composable
private fun SportsSelector(selected: Set<Sport>, onToggle: (Sport, Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkFieldLabel(
                stringResource(R.string.athlete_sports_label),
                icon = painterResource(R.drawable.ic_athlete_sports),
                modifier = Modifier.weight(1f),
            )
            Text(
                pluralStringResource(R.plurals.athlete_sports_selected, selected.size, selected.size),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Sport.entries.forEach { sport ->
                SportToggleCard(
                    sport = sport,
                    subtitle = stringResource(sport.subtitleRes),
                    selected = sport in selected,
                    onSelectedChange = { onToggle(sport, it) },
                )
            }
        }
    }
}

private val Sport.subtitleRes: Int
    get() = when (this) {
        Sport.TAEKWONDO -> R.string.athlete_sport_tkd_subtitle
        Sport.SWIMMING -> R.string.athlete_sport_swimming_subtitle
        Sport.ATHLETICS -> R.string.athlete_sport_athletics_subtitle
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthDatePickerDialog(initialMillis: Long?, onConfirm: (Long?) -> Unit, onDismiss: () -> Unit) {
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(pickerState.selectedDateMillis) }) {
                Text(stringResource(R.string.athlete_dialog_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.athlete_dialog_cancel)) }
        },
    ) {
        DatePicker(state = pickerState)
    }
}

/** "15 de mayo de 2015". El DatePicker entrega medianoche UTC. */
private fun formatBirthDate(millis: Long): String =
    SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "MX"))
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(millis))

/** Estado de ejemplo igual al diseño de Figma. */
private val FigmaSample = AthleteDraft(
    name = "Mateo Gómez",
    birthDateMillis = 1_431_648_000_000L, // 15 may 2015 UTC
    colorIndex = 0,
    sports = setOf(Sport.TAEKWONDO, Sport.SWIMMING),
    belt = Belt.GREEN,
    tkdSchool = "Doyang Central Polideportivo Norte",
    swimmingClub = "Club Acuático Olímpico Los Pinos",
)

@SkPreviews
@Composable
private fun CreateAthletePreview() = SportKidTheme {
    CreateAthleteScreen(
        onBack = {},
        onSave = {},
        state = remember { CreateAthleteState(FigmaSample) },
        photo = painterResource(R.drawable.img_sample_athlete_avatar),
    )
}

@Preview(name = "Vacío", showBackground = true)
@Composable
private fun CreateAthleteEmptyPreview() = SportKidTheme {
    CreateAthleteScreen(onBack = {}, onSave = {})
}
