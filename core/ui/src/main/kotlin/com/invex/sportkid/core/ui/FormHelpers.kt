package com.invex.sportkid.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkTonalButton
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full
import java.time.LocalDate
import java.time.LocalTime

private const val MillisPerDay = 86_400_000L

/** Diálogo de fecha de M3 sobre [LocalDate] (el `DatePicker` trabaja en milisegundos UTC). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkDatePickerDialog(initial: LocalDate, onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initial.toEpochDay() * MillisPerDay)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { onConfirm(LocalDate.ofEpochDay(it / MillisPerDay)) } ?: onDismiss()
            }) { Text(stringResource(R.string.dialog_accept)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } },
    ) { DatePicker(state = state) }
}

/** Diálogo de hora de M3 (formato de 24 h). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkTimePickerDialog(initial: LocalTime, onDismiss: () -> Unit, onConfirm: (LocalTime) -> Unit) {
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) { Text(stringResource(R.string.dialog_accept)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) } },
        text = { TimePicker(state = state) },
    )
}

/**
 * M15. Área para subir una foto: caja punteada con icono, título, ayuda y botón opcional
 * ("Examinar galería"). Toda el área abre el selector.
 */
@Composable
fun PhotoUploadArea(
    title: String,
    description: String,
    onPick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonLabel: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    val dash = colors.outline
    Column(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainerLow)
            .drawBehind {
                val r = 12.dp.toPx()
                drawRoundRect(
                    color = dash,
                    cornerRadius = CornerRadius(r, r),
                    style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx()))),
                )
            }
            .clickable(role = Role.Button, onClick = onPick)
            .padding(SportKidTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SkIconCircle(
            icon = painterResource(R.drawable.ic_photo_add),
            contentDescription = null,
            size = 48.dp,
            containerColor = colors.primaryContainer,
            iconTint = colors.primary,
        )
        Text(
            title,
            modifier = Modifier.padding(top = SportKidTheme.spacing.sm),
            style = MaterialTheme.typography.titleSmall,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            description,
            modifier = Modifier.padding(top = SportKidTheme.spacing.xs),
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (buttonLabel != null) {
            SkTonalButton(
                buttonLabel,
                onClick = onPick,
                containerColor = colors.surfaceContainerLowest,
                contentColor = colors.primary,
                shape = MaterialTheme.shapes.full,
                modifier = Modifier.padding(top = SportKidTheme.spacing.md),
            )
        }
    }
}

/** Foto ya adjunta: miniatura de 56dp, nombre, detalle y check. */
@Composable
fun AttachedPhotoRow(photo: Painter, name: String, detail: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Image(
            photo,
            contentDescription = stringResource(R.string.photo_attached_cd),
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.small),
        )
        Spacer(Modifier.width(SportKidTheme.spacing.sm))
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleSmall, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        Icon(painterResource(R.drawable.ic_photo_attached), contentDescription = null, tint = colors.primary)
    }
}

@SkPreviews
@Composable
private fun PhotoUploadAreaPreview() = SportKidTheme {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PhotoUploadArea(
            title = "Sube una foto del podio, medalla o diploma",
            description = "Comparte el recuerdo con la familia (PNG, JPG hasta 10 MB)",
            onPick = {},
            buttonLabel = "Examinar galería",
        )
    }
}
