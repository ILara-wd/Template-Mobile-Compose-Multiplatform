package com.invex.one.demo.ui.components.molecules

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpButton
import com.invex.one.demo.ui.components.atoms.CpIcon
import com.invex.one.demo.ui.components.atoms.StatusBadge
import com.invex.one.demo.ui.components.atoms.cpCard
import com.invex.one.demo.ui.theme.CpOnSurface
import com.invex.one.demo.ui.theme.CpPrimary
import com.invex.one.demo.ui.theme.CpSurfaceContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainerHigh
import com.invex.one.demo.ui.theme.CpTextSecondary
import com.invex.one.demo.ui.theme.CpWarning
import com.invex.one.demo.ui.theme.CpWhatsApp

/** M3. Tarjeta de métrica resumen (compacta, 1/3 de ancho). */
@Composable
fun MetricCard(
    @DrawableRes icon: Int,
    label: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier,
    valueColor: Color = CpOnSurface,
    captionColor: Color = CpTextSecondary,
) {
    Column(modifier.cpCard().padding(10.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CpIcon(icon)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(lineHeight = 11.sp),
                color = CpTextSecondary,
                maxLines = 1,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, color = valueColor)
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    lineHeight = 12.5.sp,
                    letterSpacing = 0.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = captionColor,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

/** Encabezado de sección con icono, título y acción a la derecha. */
@Composable
fun SectionHeader(
    @DrawableRes icon: Int,
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CpIcon(icon)
            Text(title, style = MaterialTheme.typography.titleMedium, color = CpOnSurface)
        }
        trailing()
    }
}

/** Enlace de texto "Ver todos ›". */
@Composable
fun SeeAllLink(text: String, onClick: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = CpPrimary)
        CpIcon(R.drawable.ic_chevron_right)
    }
}

/** Fila de servicio reciente: cliente, detalle, estado, fecha y acción. */
@Composable
fun ServiceItemCard(
    clientName: String,
    detail: String,
    status: @Composable () -> Unit,
    @DrawableRes dateIcon: Int,
    dateText: String,
    action: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().cpCard().padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    clientName,
                    style = MaterialTheme.typography.titleSmall,
                    color = CpOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = CpTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            status()
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CpIcon(dateIcon)
                Text(dateText, style = MaterialTheme.typography.labelSmall, color = CpTextSecondary)
            }
            action()
        }
    }
}

/** M7. Seguimiento prioritario con acciones rápidas (WhatsApp / Posponer). */
@Composable
fun PriorityFollowUpCard(
    clientName: String,
    reason: String,
    dueLabel: String,
    dueDate: String,
    onWhatsApp: () -> Unit,
    onSnooze: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth().cpCard().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        clientName,
                        style = MaterialTheme.typography.titleSmall,
                        color = CpOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    CpIcon(R.drawable.ic_priority)
                }
                Text(reason, style = MaterialTheme.typography.bodySmall, color = CpTextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(dueLabel, style = MaterialTheme.typography.labelSmall, color = CpWarning)
                Text(
                    dueDate,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, lineHeight = 20.sp),
                    color = CpTextSecondary,
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CpButton(
                text = "WhatsApp",
                onClick = onWhatsApp,
                contentColor = Color.White,
                containerColor = CpWhatsApp,
                height = 40.dp,
                gap = 6.dp,
                textStyle = MaterialTheme.typography.labelMedium,
                leadingIcon = R.drawable.ic_whatsapp_chat,
                modifier = Modifier.weight(1f),
            )
            CpButton(
                text = "Posponer",
                onClick = onSnooze,
                contentColor = CpOnSurface,
                containerColor = CpSurfaceContainer,
                height = 40.dp,
                gap = 6.dp,
                textStyle = MaterialTheme.typography.labelMedium,
                leadingIcon = R.drawable.ic_snooze,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** Seguimiento próximo compacto con botón de agendar. */
@Composable
fun UpcomingFollowUpCard(
    clientName: String,
    reason: String,
    inDaysLabel: String,
    onSchedule: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().cpCard().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    clientName,
                    style = MaterialTheme.typography.titleSmall,
                    color = CpOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                StatusBadge(
                    text = inDaysLabel,
                    containerColor = CpSurfaceContainerHigh,
                    contentColor = CpTextSecondary,
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, lineHeight = 20.sp),
                )
            }
            Text(
                reason,
                style = MaterialTheme.typography.bodySmall,
                color = CpTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CpSurfaceContainer)
                .clickable(role = Role.Button, onClickLabel = "Agendar", onClick = onSchedule),
            contentAlignment = Alignment.Center,
        ) {
            CpIcon(R.drawable.ic_calendar_add)
        }
    }
}
