package com.invex.sportkid.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkButton
import com.invex.sportkid.core.designsystem.component.SkButtonSize
import com.invex.sportkid.core.designsystem.component.SkMoneyField
import com.invex.sportkid.core.designsystem.component.SkSelectField
import com.invex.sportkid.core.designsystem.component.SkSwitch
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full
import java.time.LocalDate
import java.time.LocalTime

/*
 * Piezas comunes de los formularios de evento (plantilla T3) que comparten TKD, natación y
 * atletismo: card, fecha y hora, cuota con pagado y CTA fijo.
 */

/** Card blanca de formulario (`shape.medium`, elevación nivel 1). */
@Composable
fun FormCard(modifier: Modifier = Modifier, spacing: Int = 16, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = SkElevation.card,
    ) {
        Column(Modifier.padding(SportKidTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(spacing.dp), content = content)
    }
}

/** Fecha y hora lado a lado; cada campo abre su diálogo. */
@Composable
fun FormDateTimeFields(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    dateLabel: String,
    timeLabel: String,
    modifier: Modifier = Modifier,
) {
    var picker by rememberSaveable { mutableStateOf<String?>(null) }
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
        SkSelectField(
            value = date.shortLabel(),
            onClick = { picker = "date" },
            label = dateLabel,
            modifier = Modifier.weight(1f),
            leading = { Icon(painterResource(R.drawable.ic_form_date), contentDescription = null, tint = iconTint) },
        )
        SkSelectField(
            value = time.label(),
            onClick = { picker = "time" },
            label = timeLabel,
            modifier = Modifier.weight(1f),
            leading = { Icon(painterResource(R.drawable.ic_form_time), contentDescription = null, tint = iconTint) },
        )
    }
    when (picker) {
        "date" -> SkDatePickerDialog(date, onDismiss = { picker = null }) {
            onDateChange(it)
            picker = null
        }
        "time" -> SkTimePickerDialog(time, onDismiss = { picker = null }) {
            onTimeChange(it)
            picker = null
        }
    }
}

/** Monto (pesos) y switch de pagado en una caja `surfaceContainer`. */
@Composable
fun FeeRow(
    label: String,
    cost: String,
    onCostChange: (String) -> Unit,
    paid: Boolean,
    onPaidChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    paidLabel: String = stringResource(R.string.form_paid),
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .background(colors.surfaceContainer, MaterialTheme.shapes.small)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            SkMoneyField(value = cost, onValueChange = onCostChange)
        }
        Row(
            Modifier
                .clip(MaterialTheme.shapes.full)
                .toggleable(value = paid, role = Role.Switch, onValueChange = onPaidChange)
                .padding(SportKidTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (paid) paidLabel else stringResource(R.string.form_pending),
                style = MaterialTheme.typography.labelMedium,
                color = if (paid) colors.primary else colors.onSurfaceVariant,
            )
            Spacer(Modifier.width(SportKidTheme.spacing.sm))
            SkSwitch(checked = paid, onCheckedChange = null)
        }
    }
}

/** CTA fijo de formularios de evento (plantilla T3) con nota opcional; color configurable (deporte). */
@Composable
fun FormSaveBar(
    text: String,
    icon: Painter,
    enabled: Boolean,
    onSave: () -> Unit,
    note: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier
                .navigationBarsPadding()
                .padding(horizontal = SportKidTheme.spacing.lg, vertical = SportKidTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
        ) {
            SkButton(
                text = text,
                onClick = onSave,
                enabled = enabled,
                leadingIcon = icon,
                size = SkButtonSize.Medium,
                containerColor = containerColor,
                modifier = Modifier.fillMaxWidth(),
            )
            if (note != null) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_form_sync), contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
