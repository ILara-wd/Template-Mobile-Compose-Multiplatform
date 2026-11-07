package com.invex.sportkid.feature.tkd

import com.invex.sportkid.core.ui.FormCard
import com.invex.sportkid.core.ui.FormDateTimeFields
import com.invex.sportkid.core.ui.FeeRow
import com.invex.sportkid.core.ui.FormSaveBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.sportkid.core.designsystem.component.SkButton
import com.invex.sportkid.core.designsystem.component.SkButtonSize
import com.invex.sportkid.core.designsystem.component.SkDetailTopBar
import com.invex.sportkid.core.designsystem.component.SkInfoCallout
import com.invex.sportkid.core.designsystem.component.SkNavigationIcon
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkSegmentedControl
import com.invex.sportkid.core.designsystem.component.SkTextField
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full
import com.invex.sportkid.core.model.Belt
import com.invex.sportkid.core.model.PaymentStatus
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.ui.BeltSelectField
import com.invex.sportkid.core.ui.BeltSwatch
import com.invex.sportkid.core.ui.PaymentStatusChip
import com.invex.sportkid.core.ui.PhotoUploadArea
import com.invex.sportkid.core.ui.colors
import com.invex.sportkid.core.ui.gradeRes
import com.invex.sportkid.core.ui.monthYearShortLabel
import com.invex.sportkid.core.ui.next
import com.invex.sportkid.core.ui.shortLabelRes
import java.time.LocalDate
import java.time.LocalTime
import com.invex.sportkid.core.ui.R as UiR

/** Datos capturados en P10 al guardar. */
@Immutable
data class BeltExamDraft(
    val targetBelt: Belt?,
    val place: String,
    val date: LocalDate,
    val time: LocalTime,
    val feePesos: Int?,
    val paid: Boolean,
    val result: ExamResult,
)

/** Estado de P10. Sobrevive a rotación y muerte de proceso. */
@Stable
class BeltExamState(initial: BeltExamDraft) {
    var targetBelt by mutableStateOf(initial.targetBelt)
    var place by mutableStateOf(initial.place)
    var date by mutableStateOf(initial.date)
    var time by mutableStateOf(initial.time)
    var fee by mutableStateOf(initial.feePesos?.toString().orEmpty())
    var paid by mutableStateOf(initial.paid)
    var result by mutableStateOf(initial.result)

    val canSave: Boolean get() = targetBelt != null && place.isNotBlank()

    fun toDraft() = BeltExamDraft(targetBelt, place.trim(), date, time, fee.toIntOrNull(), paid, result)

    companion object {
        /** Siguiente cinta, sede de la escuela, en una semana a las 9:00 y resultado pendiente. */
        fun empty(athlete: TkdAthlete, today: LocalDate = LocalDate.now()) = BeltExamDraft(
            targetBelt = athlete.belt.next,
            place = athlete.school,
            date = today.plusDays(7),
            time = LocalTime.of(9, 0),
            feePesos = null,
            paid = false,
            result = ExamResult.Pending,
        )

        val Saver = listSaver<BeltExamState, Any?>(
            save = { listOf(it.targetBelt?.name, it.place, it.date.toEpochDay(), it.time.toSecondOfDay(), it.fee, it.paid, it.result.name) },
            restore = {
                BeltExamState(
                    BeltExamDraft(
                        targetBelt = (it[0] as String?)?.let(Belt::valueOf),
                        place = it[1] as String,
                        date = LocalDate.ofEpochDay(it[2] as Long),
                        time = LocalTime.ofSecondOfDay((it[3] as Int).toLong()),
                        feePesos = (it[4] as String).toIntOrNull(),
                        paid = it[5] as Boolean,
                        result = ExamResult.valueOf(it[6] as String),
                    ),
                )
            },
        )
    }
}

@Composable
fun rememberBeltExamState(initial: BeltExamDraft): BeltExamState =
    rememberSaveable(saver = BeltExamState.Saver) { BeltExamState(initial) }

/**
 * P10. Registrar examen de cinta (plantilla T3): cinta actual (solo lectura), cinta objetivo,
 * lugar, fecha y hora, cuota y pago, resultado (M14) con callout (M19) y certificado opcional.
 * Al guardar como Aprobado, `app` actualiza la cinta del perfil.
 */
@Composable
fun BeltExamScreen(
    athlete: TkdAthlete,
    onClose: () -> Unit,
    onSave: (BeltExamDraft) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    state: BeltExamState = rememberBeltExamState(BeltExamState.empty(athlete, today)),
    onPickPhoto: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val save = { onSave(state.toDraft()) }
    Scaffold(
        modifier = modifier.imePadding(),
        containerColor = colors.surface,
        topBar = {
            SkDetailTopBar(title = stringResource(R.string.exam_title), onBack = onClose, navigationIcon = SkNavigationIcon.Close) {
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
                text = stringResource(R.string.exam_save_cta),
                icon = painterResource(UiR.drawable.ic_form_save),
                enabled = state.canSave,
                onSave = save,
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
            TkdAthleteStrip(athlete, today, showAge = true) {
                SkPill(
                    stringResource(R.string.exam_official),
                    icon = painterResource(R.drawable.ic_tkd_official),
                    textStyle = MaterialTheme.typography.labelMedium,
                    contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = SportKidTheme.spacing.xs),
                )
            }
            SkInfoCallout(
                stringResource(R.string.exam_auto_update_body, athlete.firstName),
                title = stringResource(R.string.exam_auto_update_title),
                containerColor = colors.primaryContainer.copy(alpha = 0.6f),
                contentColor = colors.onPrimaryContainer,
                emphasized = true,
            )
            CurrentBeltCard(athlete)
            GoalCard(athlete, state)
            FeeCard(state)
            ResultCard(athlete, state)
            FormCard(spacing = 8) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.exam_photo), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                    Text(stringResource(R.string.exam_photo_optional), style = MaterialTheme.typography.labelSmall, color = colors.outline)
                }
                PhotoUploadArea(
                    title = stringResource(R.string.exam_photo_title),
                    description = stringResource(R.string.exam_photo_hint),
                    onPick = onPickPhoto,
                )
            }
        }
    }
}

/** Cinta actual de solo lectura; se actualiza al aprobar. */
@Composable
private fun CurrentBeltCard(athlete: TkdAthlete) {
    val colors = MaterialTheme.colorScheme
    FormCard(spacing = 8) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.exam_current_belt).uppercase(),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.6.sp),
                color = colors.onSurfaceVariant,
            )
            SkPill(
                stringResource(R.string.exam_current_badge),
                containerColor = colors.surfaceContainerHigh,
                contentColor = colors.onSurfaceVariant,
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .background(colors.surfaceContainerLow, MaterialTheme.shapes.small)
                .padding(SportKidTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BeltSwatch(athlete.belt, width = 48.dp, height = 24.dp)
            Spacer(Modifier.width(SportKidTheme.spacing.sm))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(UiR.string.belt_name, stringResource(athlete.belt.shortLabelRes())),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                )
                Text(
                    athlete.beltSince?.let { stringResource(R.string.exam_current_since, stringResource(athlete.belt.gradeRes()), it.monthYearShortLabel()) }
                        ?: stringResource(athlete.belt.gradeRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
            Icon(painterResource(R.drawable.ic_tkd_lock), contentDescription = stringResource(R.string.exam_locked_cd), tint = colors.outline)
        }
    }
}

@Composable
private fun GoalCard(athlete: TkdAthlete, state: BeltExamState) {
    val colors = MaterialTheme.colorScheme
    FormCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.size(width = 8.dp, height = 16.dp).background(Sport.TAEKWONDO.colors().content, MaterialTheme.shapes.full))
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(R.string.exam_goal),
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
            )
        }
        BeltSelectField(
            belt = state.targetBelt,
            onBeltChange = { state.targetBelt = it },
            label = stringResource(R.string.exam_target_belt),
            trailingIcon = painterResource(UiR.drawable.ic_form_dropdown),
            options = Belt.entries.drop(athlete.belt.ordinal + 1),
            required = true,
        )
        SkTextField(
            value = state.place,
            onValueChange = { state.place = it },
            label = stringResource(R.string.tkd_required_suffix, stringResource(R.string.exam_place)),
            placeholder = stringResource(R.string.exam_place_placeholder),
            helperText = athlete.schoolAddress.takeIf { state.place == athlete.school },
            leading = { Icon(painterResource(R.drawable.ic_tkd_place), contentDescription = null, tint = colors.onSurfaceVariant) },
        )
        FormDateTimeFields(
            date = state.date,
            onDateChange = { state.date = it },
            time = state.time,
            onTimeChange = { state.time = it },
            dateLabel = stringResource(R.string.tkd_required_suffix, stringResource(R.string.exam_date)),
            timeLabel = stringResource(R.string.tkd_required_suffix, stringResource(R.string.exam_time)),
        )
    }
}

@Composable
private fun FeeCard(state: BeltExamState) {
    val colors = MaterialTheme.colorScheme
    FormCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.exam_fee_title), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
            PaymentStatusChip(if (state.paid) PaymentStatus.PAID else PaymentStatus.PENDING)
        }
        FeeRow(
            label = stringResource(R.string.exam_fee),
            cost = state.fee,
            onCostChange = { state.fee = it },
            paid = state.paid,
            onPaidChange = { state.paid = it },
            paidLabel = stringResource(R.string.exam_fee_paid),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_tkd_receipt), contentDescription = null, tint = colors.primary)
            Spacer(Modifier.width(SportKidTheme.spacing.xs))
            Text(
                stringResource(if (state.paid) R.string.exam_fee_paid_note else R.string.exam_fee_pending_note),
                style = MaterialTheme.typography.bodySmall,
                color = colors.primary,
            )
        }
    }
}

@Composable
private fun ResultCard(athlete: TkdAthlete, state: BeltExamState) {
    val colors = MaterialTheme.colorScheme
    val target = state.targetBelt
    FormCard(spacing = 8) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.exam_result), modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
            Text(stringResource(R.string.exam_result_hint), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
        SkSegmentedControl(
            labels = listOf(
                stringResource(R.string.exam_result_pending),
                stringResource(R.string.exam_result_passed),
                stringResource(R.string.exam_result_failed),
            ),
            selectedIndex = state.result.ordinal,
            onSelect = { state.result = ExamResult.entries[it] },
            icons = ExamResult.entries.map {
                if (it == state.result && it == ExamResult.Passed) painterResource(UiR.drawable.ic_status_paid) else null
            },
            selectedContainerColor = colors.primary,
            selectedContentColor = colors.onPrimary,
            textStyle = MaterialTheme.typography.labelMedium,
        )
        val beltName = target?.let { stringResource(UiR.string.belt_name, stringResource(it.shortLabelRes())) }.orEmpty()
        val message = when (state.result) {
            ExamResult.Pending -> buildAnnotatedString { append(stringResource(R.string.exam_result_pending_note)) }
            ExamResult.Failed -> buildAnnotatedString { append(stringResource(R.string.exam_result_failed_note, athlete.firstName)) }
            ExamResult.Passed -> {
                val full = stringResource(R.string.exam_result_passed_note, athlete.firstName, beltName)
                buildAnnotatedString {
                    val start = full.indexOf(beltName)
                    if (beltName.isEmpty() || start < 0) {
                        append(full)
                    } else {
                        append(full.substring(0, start))
                        withStyle(SpanStyle(color = colors.primary)) { append(beltName) }
                        append(full.substring(start + beltName.length))
                    }
                }
            }
        }
        SkInfoCallout(
            message,
            icon = painterResource(UiR.drawable.ic_medal),
            containerColor = colors.surfaceContainerLow,
        )
    }
}

/** Estado de Figma con la cinta de Mateo: verde a azul, $650 pagado y Aprobado. */
@SkPreviews
@Composable
private fun BeltExamPreview() = SportKidTheme {
    val athlete = TkdSamples.mateo
    BeltExamScreen(
        athlete = athlete,
        onClose = {},
        onSave = {},
        today = LocalDate.of(2025, 11, 10),
        state = rememberBeltExamState(
            BeltExamDraft(
                targetBelt = Belt.BLUE,
                place = athlete.school,
                date = LocalDate.of(2025, 11, 14),
                time = LocalTime.of(9, 0),
                feePesos = 650,
                paid = true,
                result = ExamResult.Passed,
            ),
        ),
    )
}
