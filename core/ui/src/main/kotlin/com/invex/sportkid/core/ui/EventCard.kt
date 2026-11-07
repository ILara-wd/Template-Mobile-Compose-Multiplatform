package com.invex.sportkid.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkCardAccentWidth
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full
import com.invex.sportkid.core.model.PaymentStatus
import com.invex.sportkid.core.model.Sport

/** Punto con el color del hijo (calendario, chips, leyendas). */
@Composable
fun AthleteDot(color: Color, modifier: Modifier = Modifier, size: Dp = 8.dp) {
    Box(modifier.size(size).background(color, MaterialTheme.shapes.full))
}

/** Etiqueta compacta del hijo: punto de color y nombre sobre `surfaceContainer`. */
@Composable
fun AthleteTag(name: String, color: Color, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.event_athlete_cd, name)
    SkPill(
        text = name,
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
        spacing = 6.dp,
        leading = { AthleteDot(color) },
    )
}

/** M6 (subconjunto de pago). "Pagado" en `secondaryContainer`, "Pendiente de pago" en `tertiaryContainer`. */
@Composable
fun PaymentStatusChip(status: PaymentStatus, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val paid = status == PaymentStatus.PAID
    SkPill(
        text = stringResource(if (paid) R.string.payment_status_paid else R.string.payment_status_pending),
        modifier = modifier,
        containerColor = if (paid) colors.secondaryContainer else colors.tertiaryContainer,
        contentColor = if (paid) colors.primary else colors.tertiary,
        icon = painterResource(if (paid) R.drawable.ic_status_paid else R.drawable.ic_status_pending),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
    )
}

/**
 * O5. Card de evento: acento lateral del deporte, horario, hijo, título, icono del deporte,
 * lugar, costo con estado de pago y, si está pendiente, la franja "Toca para saldar".
 */
@Composable
fun EventCard(
    sport: Sport,
    title: String,
    time: String,
    athleteName: String,
    athleteColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    place: String? = null,
    costLabel: String? = null,
    cost: String? = null,
    paymentStatus: PaymentStatus? = null,
    onClick: (() -> Unit)? = null,
    onSettlePayment: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val sportColors = sport.colors()
    val body: @Composable () -> Unit = {
        Column {
            Row(Modifier.height(IntrinsicSize.Min)) {
                Box(
                    Modifier
                        .width(SkCardAccentWidth)
                        .fillMaxHeight()
                        .background(sportColors.content),
                )
                Column(Modifier.weight(1f)) {
                    Column(
                        Modifier.padding(SportKidTheme.spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(R.drawable.ic_event_time),
                                contentDescription = null,
                                tint = colors.primary,
                            )
                            Spacer(Modifier.width(SportKidTheme.spacing.xs))
                            Text(
                                time,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onSurfaceVariant,
                            )
                            AthleteTag(athleteName, athleteColor)
                        }
                        Row(verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (subtitle != null) {
                                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                }
                            }
                            Spacer(Modifier.width(SportKidTheme.spacing.sm))
                            SkIconCircle(
                                icon = painterResource(sport.iconRes),
                                contentDescription = stringResource(sport.labelRes),
                                size = 36.dp,
                                containerColor = sportColors.container,
                                iconTint = sportColors.content,
                            )
                        }
                        if (place != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painterResource(R.drawable.ic_event_place),
                                    contentDescription = null,
                                    tint = colors.tertiary,
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    place,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                    if (cost != null) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(colors.surfaceContainerLow)
                                .padding(start = SportKidTheme.spacing.lg, end = SportKidTheme.spacing.lg, top = 8.dp, bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                if (costLabel != null) {
                                    Text(costLabel, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                                }
                                Text(
                                    cost,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.onSurface,
                                )
                            }
                            if (paymentStatus != null) PaymentStatusChip(paymentStatus)
                        }
                    }
                }
            }
            if (paymentStatus == PaymentStatus.PENDING && onSettlePayment != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onSettlePayment)
                        .background(colors.surfaceContainer)
                        .padding(horizontal = SportKidTheme.spacing.md, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painterResource(R.drawable.ic_event_touch), contentDescription = null, tint = colors.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.event_settle_payment),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onSurfaceVariant,
                    )
                    Icon(painterResource(R.drawable.ic_event_chevron), contentDescription = null, tint = colors.onSurfaceVariant)
                }
            }
        }
    }
    val shape = MaterialTheme.shapes.large
    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = colors.surfaceContainerLowest,
            shadowElevation = SkElevation.card,
            content = body,
        )
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = colors.surfaceContainerLowest,
            shadowElevation = SkElevation.card,
            content = body,
        )
    }
}

@SkPreviews
@Composable
private fun EventCardPreview() = SportKidTheme {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        EventCard(
            sport = Sport.TAEKWONDO,
            title = "Examen de cinta verde",
            time = "09:00 - 11:30 hrs",
            athleteName = "Mateo",
            athleteColor = SportKidTheme.sportColors.athlete.blue,
            subtitle = "Taekwondo Infantil · Profesor Kim",
            place = "Doyang Central Polideportivo Norte",
            costLabel = "Cuota de graduación",
            cost = "$650 MXN",
            paymentStatus = PaymentStatus.PENDING,
            onSettlePayment = {},
        )
        EventCard(
            sport = Sport.SWIMMING,
            title = "Entrenamiento libre pre-competencia",
            time = "16:00 - 17:30 hrs",
            athleteName = "Sofía",
            athleteColor = SportKidTheme.sportColors.athlete.pink,
            subtitle = "Categoría Infantil B · Carril 4",
            place = "Club Acuático Olímpico Los Pinos",
            costLabel = "Uso de alberca olímpica",
            cost = "$200 MXN",
            paymentStatus = PaymentStatus.PAID,
        )
    }
}
