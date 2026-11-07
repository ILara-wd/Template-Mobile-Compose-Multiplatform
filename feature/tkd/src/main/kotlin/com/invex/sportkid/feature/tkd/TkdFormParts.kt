package com.invex.sportkid.feature.tkd

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkAvatar
import com.invex.sportkid.core.designsystem.component.SkButton
import com.invex.sportkid.core.designsystem.component.SkButtonSize
import com.invex.sportkid.core.designsystem.component.SkMoneyField
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkSelectField
import com.invex.sportkid.core.designsystem.component.SkSwitch
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.ui.AthleteDot
import com.invex.sportkid.core.ui.SkDatePickerDialog
import com.invex.sportkid.core.ui.SkTimePickerDialog
import com.invex.sportkid.core.ui.ageOn
import com.invex.sportkid.core.ui.colors
import com.invex.sportkid.core.ui.label
import com.invex.sportkid.core.ui.shortLabel
import java.time.LocalDate
import java.time.LocalTime

/**
 * Contexto del deportista en los formularios de TKD: avatar con el punto del deporte, nombre,
 * escuela y un elemento a la derecha (cinta o "Kup oficial").
 */
@Composable
internal fun TkdAthleteStrip(
    athlete: TkdAthlete,
    today: LocalDate,
    modifier: Modifier = Modifier,
    showAge: Boolean = false,
    trailing: @Composable () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val tkd = Sport.TAEKWONDO.colors().content
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        SkAvatar(
            photo = athlete.photoRes?.let { painterResource(it) },
            name = athlete.fullName,
            size = 44.dp,
            ringColor = athlete.color(),
        ) {
            AthleteDot(tkd, size = 14.dp, modifier = Modifier.align(Alignment.BottomEnd))
        }
        Spacer(Modifier.width(SportKidTheme.spacing.sm))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(athlete.fullName, style = MaterialTheme.typography.titleSmall, color = colors.onSurface, maxLines = 1)
                if (showAge) {
                    Spacer(Modifier.width(SportKidTheme.spacing.xs))
                    SkPill(
                        stringResource(R.string.tkd_age, athlete.birthDate.ageOn(today)),
                        containerColor = colors.surfaceContainerHigh,
                        contentColor = colors.onSurfaceVariant,
                        contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                    )
                }
            }
            Text(
                stringResource(R.string.tkd_sport_school, athlete.school),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(SportKidTheme.spacing.sm))
        trailing()
    }
}

/** Card blanca de formulario (`shape.medium`, elevación nivel 1). */
@Composable
internal fun TkdFormCard(modifier: Modifier = Modifier, spacing: Int = 16, content: @Composable ColumnScope.() -> Unit) {
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
internal fun DateTimeFields(
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
            leading = { Icon(painterResource(R.drawable.ic_tkd_date), contentDescription = null, tint = iconTint) },
        )
        SkSelectField(
            value = time.label(),
            onClick = { picker = "time" },
            label = timeLabel,
            modifier = Modifier.weight(1f),
            leading = { Icon(painterResource(R.drawable.ic_tkd_time), contentDescription = null, tint = iconTint) },
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
internal fun FeeRow(
    label: String,
    cost: String,
    onCostChange: (String) -> Unit,
    paid: Boolean,
    onPaidChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    paidLabel: String = stringResource(R.string.tkd_paid),
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
                if (paid) paidLabel else stringResource(R.string.tkd_pending),
                style = MaterialTheme.typography.labelMedium,
                color = if (paid) colors.primary else colors.onSurfaceVariant,
            )
            Spacer(Modifier.width(SportKidTheme.spacing.sm))
            SkSwitch(checked = paid, onCheckedChange = null)
        }
    }
}

/** CTA fijo de los formularios de TKD, en el color del deporte, con nota opcional. */
@Composable
internal fun TkdSaveBar(text: String, icon: Painter, enabled: Boolean, onSave: () -> Unit, note: String? = null) {
    val tkd = Sport.TAEKWONDO.colors().content
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
                containerColor = tkd,
                modifier = Modifier.fillMaxWidth(),
            )
            if (note != null) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_tkd_sync), contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
