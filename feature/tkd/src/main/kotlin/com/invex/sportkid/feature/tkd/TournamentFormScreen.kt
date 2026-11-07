package com.invex.sportkid.feature.tkd

import com.invex.sportkid.core.ui.FormCard
import com.invex.sportkid.core.ui.FormDateTimeFields
import com.invex.sportkid.core.ui.FeeRow
import com.invex.sportkid.core.ui.FormSaveBar
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkButton
import com.invex.sportkid.core.designsystem.component.SkButtonSize
import com.invex.sportkid.core.designsystem.component.SkChip
import com.invex.sportkid.core.designsystem.component.SkDetailTopBar
import com.invex.sportkid.core.designsystem.component.SkFieldLabel
import com.invex.sportkid.core.designsystem.component.SkInfoCallout
import com.invex.sportkid.core.designsystem.component.SkNavigationIcon
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkTextField
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.MedalResult
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.ui.AttachedPhotoRow
import com.invex.sportkid.core.ui.MedalResultPicker
import com.invex.sportkid.core.ui.PhotoUploadArea
import com.invex.sportkid.core.ui.colors
import com.invex.sportkid.core.ui.iconRes
import com.invex.sportkid.core.ui.shortLabelRes
import java.time.LocalDate
import java.time.LocalTime
import com.invex.sportkid.core.ui.R as UiR

/** Datos capturados en P11 al guardar. */
@Immutable
data class TournamentDraft(
    val name: String,
    val place: String,
    val date: LocalDate,
    val time: LocalTime,
    val modality: TournamentModality,
    val category: String,
    val costPesos: Int?,
    val paid: Boolean,
    val result: MedalResult,
)

/** Estado de P11. Sobrevive a rotación y muerte de proceso. */
@Stable
class TournamentFormState(initial: TournamentDraft) {
    var name by mutableStateOf(initial.name)
    var place by mutableStateOf(initial.place)
    var date by mutableStateOf(initial.date)
    var time by mutableStateOf(initial.time)
    var modality by mutableStateOf(initial.modality)
    var category by mutableStateOf(initial.category)
    var cost by mutableStateOf(initial.costPesos?.toString().orEmpty())
    var paid by mutableStateOf(initial.paid)
    var result by mutableStateOf(initial.result)

    val canSave: Boolean get() = name.isNotBlank() && place.isNotBlank() && category.isNotBlank()

    fun toDraft() = TournamentDraft(name.trim(), place.trim(), date, time, modality, category.trim(), cost.toIntOrNull(), paid, result)

    companion object {
        fun empty(today: LocalDate = LocalDate.now()) = TournamentDraft(
            name = "", place = "", date = today.plusDays(7), time = LocalTime.of(8, 30),
            modality = TournamentModality.Combat, category = "", costPesos = null, paid = false, result = MedalResult.PENDING,
        )

        val Saver = listSaver<TournamentFormState, Any?>(
            save = {
                listOf(it.name, it.place, it.date.toEpochDay(), it.time.toSecondOfDay(), it.modality.name, it.category, it.cost, it.paid, it.result.name)
            },
            restore = {
                TournamentFormState(
                    TournamentDraft(
                        name = it[0] as String,
                        place = it[1] as String,
                        date = LocalDate.ofEpochDay(it[2] as Long),
                        time = LocalTime.ofSecondOfDay((it[3] as Int).toLong()),
                        modality = TournamentModality.valueOf(it[4] as String),
                        category = it[5] as String,
                        costPesos = (it[6] as String).toIntOrNull(),
                        paid = it[7] as Boolean,
                        result = MedalResult.valueOf(it[8] as String),
                    ),
                )
            },
        )
    }
}

@Composable
fun rememberTournamentFormState(initial: TournamentDraft = TournamentFormState.empty()): TournamentFormState =
    rememberSaveable(saver = TournamentFormState.Saver) { TournamentFormState(initial) }

/**
 * P11. Registrar torneo TKD (plantilla T3): deportista, banner, datos del torneo, modalidad,
 * categoría, costo y pago, resultado (medalla o pendiente) y foto opcional.
 */
@Composable
fun TournamentFormScreen(
    athlete: TkdAthlete,
    onClose: () -> Unit,
    onSave: (TournamentDraft) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    state: TournamentFormState = rememberTournamentFormState(TournamentFormState.empty(today)),
    onPickPhoto: () -> Unit = {},
    attachedPhoto: Painter? = null,
    attachedPhotoName: String? = null,
) {
    val save = { onSave(state.toDraft()) }
    Scaffold(
        modifier = modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            SkDetailTopBar(title = stringResource(R.string.tournament_title), onBack = onClose, navigationIcon = SkNavigationIcon.Close) {
                SkButton(
                    stringResource(R.string.tkd_save),
                    onClick = save,
                    enabled = state.canSave,
                    size = SkButtonSize.Small,
                    modifier = Modifier.padding(end = SportKidTheme.spacing.sm),
                )
            }
        },
        bottomBar = {
            FormSaveBar(
                containerColor = Sport.TAEKWONDO.colors().content,
                text = stringResource(R.string.tournament_save_cta),
                icon = painterResource(Sport.TAEKWONDO.iconRes),
                enabled = state.canSave,
                onSave = save,
                note = stringResource(R.string.tkd_sync_note),
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SportKidTheme.spacing.lg, vertical = SportKidTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg),
        ) {
            FormCard {
                TkdAthleteStrip(athlete, today) {
                    SkPill(
                        stringResource(UiR.string.belt_name, stringResource(athlete.belt.shortLabelRes())),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            TournamentBanner()
            DetailsCard(state)
            ResultCard(state.result, onSelect = { state.result = it }, athleteName = athlete.firstName)
            PhotoCard(onPickPhoto, attachedPhoto, attachedPhotoName, athlete.firstName)
        }
    }
}

/** Banner con foto de un torneo y degradado para el texto. */
@Composable
private fun TournamentBanner() {
    val overlay = MaterialTheme.colorScheme.inverseSurface
    Box(
        Modifier
            .fillMaxWidth()
            .height(128.dp)
            .clip(MaterialTheme.shapes.medium),
    ) {
        Image(
            painterResource(R.drawable.img_tkd_tournament_banner),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(overlay.copy(alpha = 0f), overlay.copy(alpha = 0.4f), overlay.copy(alpha = 0.9f))))
                .padding(SportKidTheme.spacing.lg),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_tkd_banner_trophy), contentDescription = null, tint = MaterialTheme.colorScheme.inversePrimary)
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.tournament_banner_overline).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.inversePrimary,
                )
            }
            Text(
                stringResource(R.string.tournament_banner_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.inverseOnSurface,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsCard(state: TournamentFormState) {
    val colors = MaterialTheme.colorScheme
    val iconTint = colors.onSurfaceVariant
    val tkd = Sport.TAEKWONDO.colors().content
    FormCard {
        SkTextField(
            value = state.name,
            onValueChange = { state.name = it },
            label = stringResource(R.string.tkd_required_suffix, stringResource(R.string.tournament_name)),
            placeholder = stringResource(R.string.tournament_name_placeholder),
            leading = { Icon(painterResource(R.drawable.ic_tkd_name), contentDescription = null, tint = iconTint) },
        )
        SkTextField(
            value = state.place,
            onValueChange = { state.place = it },
            label = stringResource(R.string.tkd_required_suffix, stringResource(R.string.tournament_place)),
            placeholder = stringResource(R.string.tournament_place_placeholder),
            leading = { Icon(painterResource(R.drawable.ic_tkd_place), contentDescription = null, tint = tkd) },
        )
        FormDateTimeFields(
            date = state.date,
            onDateChange = { state.date = it },
            time = state.time,
            onTimeChange = { state.time = it },
            dateLabel = stringResource(R.string.tkd_required_suffix, stringResource(R.string.tournament_date)),
            timeLabel = stringResource(R.string.tournament_time),
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SkFieldLabel(stringResource(R.string.tkd_required_suffix, stringResource(R.string.tournament_modality)))
            FlowRow(
                Modifier.selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
            ) {
                TournamentModality.entries.forEach { modality ->
                    SkChip(
                        text = stringResource(modality.labelRes()),
                        selected = state.modality == modality,
                        onClick = { state.modality = modality },
                        icon = painterResource(modality.iconRes()),
                        iconSize = 16.dp,
                        height = 36.dp,
                        horizontalPadding = SportKidTheme.spacing.lg,
                        selectedContainerColor = tkd,
                        selectedContentColor = Color.White,
                        containerColor = colors.surfaceContainerHigh,
                        contentColor = colors.onSurfaceVariant,
                    )
                }
            }
        }
        SkTextField(
            value = state.category,
            onValueChange = { state.category = it },
            label = stringResource(R.string.tkd_required_suffix, stringResource(R.string.tournament_category)),
            placeholder = stringResource(R.string.tournament_category_placeholder),
            leading = { Icon(painterResource(R.drawable.ic_tkd_category), contentDescription = null, tint = iconTint) },
        )
        FeeRow(
            label = stringResource(R.string.tournament_cost),
            cost = state.cost,
            onCostChange = { state.cost = it },
            paid = state.paid,
            onPaidChange = { state.paid = it },
        )
    }
}

internal fun TournamentModality.labelRes(): Int = when (this) {
    TournamentModality.Combat -> R.string.tournament_modality_combat
    TournamentModality.Forms -> R.string.tournament_modality_forms
}

internal fun TournamentModality.iconRes(): Int = when (this) {
    TournamentModality.Combat -> Sport.TAEKWONDO.iconRes
    TournamentModality.Forms -> R.drawable.ic_tkd_poomsae
}

@Composable
private fun ResultCard(result: MedalResult, onSelect: (MedalResult) -> Unit, athleteName: String) {
    val colors = MaterialTheme.colorScheme
    FormCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.tournament_result), style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                Text(stringResource(R.string.tournament_result_hint), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Icon(painterResource(UiR.drawable.ic_medal), contentDescription = null, tint = colors.primary)
        }
        MedalResultPicker(selected = result, onSelect = onSelect)
        SkInfoCallout(
            when (result) {
                MedalResult.PENDING -> stringResource(R.string.tournament_result_pending_note)
                MedalResult.PARTICIPATION -> stringResource(R.string.tournament_result_participation_note, athleteName)
                else -> stringResource(R.string.tournament_result_podium_note, athleteName)
            },
        )
    }
}

@Composable
private fun PhotoCard(
    onPickPhoto: () -> Unit,
    attachedPhoto: Painter?,
    attachedPhotoName: String?,
    athleteName: String,
) {
    val colors = MaterialTheme.colorScheme
    FormCard(spacing = 8) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.tournament_photo))
                    append(" ")
                    withStyle(SpanStyle(color = colors.onSurfaceVariant, fontWeight = FontWeight.Normal, fontSize = MaterialTheme.typography.bodySmall.fontSize)) {
                        append(stringResource(R.string.tournament_photo_optional))
                    }
                },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
            )
            Icon(painterResource(R.drawable.ic_tkd_photo_header), contentDescription = null, tint = colors.secondary)
        }
        PhotoUploadArea(
            title = stringResource(R.string.tournament_photo_title),
            description = stringResource(R.string.tournament_photo_hint),
            onPick = onPickPhoto,
            buttonLabel = stringResource(UiR.string.photo_upload_browse),
        )
        if (attachedPhoto != null && attachedPhotoName != null) {
            AttachedPhotoRow(
                photo = attachedPhoto,
                name = attachedPhotoName,
                detail = stringResource(R.string.tournament_photo_album, athleteName),
                modifier = Modifier.padding(top = SportKidTheme.spacing.sm),
            )
        }
    }
}

/** Estado de Figma: Copa Metropolitana, combate, $750 pagado, resultado pendiente y foto adjunta. */
@SkPreviews
@Composable
private fun TournamentFormPreview() = SportKidTheme {
    val today = LocalDate.of(2025, 11, 10)
    TournamentFormScreen(
        athlete = TkdSamples.mateo,
        onClose = {},
        onSave = {},
        today = today,
        state = rememberTournamentFormState(
            TournamentDraft(
                name = "Copa Metropolitana de Taekwondo Infantil",
                place = "Gimnasio Olímpico Juan de la Barrera",
                date = LocalDate.of(2025, 11, 22),
                time = LocalTime.of(8, 30),
                modality = TournamentModality.Combat,
                category = "Infantil menor · -32 kg · Cintas verdes",
                costPesos = 750,
                paid = true,
                result = MedalResult.PENDING,
            ),
        ),
        attachedPhoto = painterResource(R.drawable.img_sample_tournament_photo),
        attachedPhotoName = "recuerdo_torneo_previo.jpg",
    )
}
