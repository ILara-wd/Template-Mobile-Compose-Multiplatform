package com.invex.one.demo.ui.components.molecules

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpButton
import com.invex.one.demo.ui.components.atoms.CpIcon
import com.invex.one.demo.ui.components.atoms.StatusBadge
import com.invex.one.demo.ui.components.atoms.StatusDot
import com.invex.one.demo.ui.components.atoms.cpCard
import com.invex.one.demo.ui.theme.CpBackground
import com.invex.one.demo.ui.theme.CpOnSuccessContainer
import com.invex.one.demo.ui.theme.CpOnSurface
import com.invex.one.demo.ui.theme.CpOnSurfaceVariant
import com.invex.one.demo.ui.theme.CpOutline
import com.invex.one.demo.ui.theme.CpPrimary
import com.invex.one.demo.ui.theme.CpPrimaryContainer
import com.invex.one.demo.ui.theme.CpPrimaryFixed
import com.invex.one.demo.ui.theme.CpSecondaryContainer
import com.invex.one.demo.ui.theme.CpSuccess
import com.invex.one.demo.ui.theme.CpSuccessContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainerHigh
import com.invex.one.demo.ui.theme.CpSurfaceContainerLow
import com.invex.one.demo.ui.theme.CpTextMuted
import com.invex.one.demo.ui.theme.CpTextSecondary
import com.invex.one.demo.ui.theme.CpWhatsApp

private val Radius8 = RoundedCornerShape(8.dp)

// ---------- Micro-banner de contexto ----------

/** Franja azul tonal con estado del cliente y pill de garantía. */
@Composable
fun ClientContextBanner(
    text: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .shadow(1.dp, RectangleShape, ambientColor = Color.Black.copy(0.05f), spotColor = Color.Black.copy(0.05f))
            .background(CpSecondaryContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusDot(CpPrimaryContainer)
            Text(
                text.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.55.sp),
                color = CpTextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (badge != null) {
            StatusBadge(
                text = badge,
                containerColor = Color.White.copy(alpha = 0.8f),
                contentColor = CpPrimary,
            )
        }
    }
}

// ---------- Ficha principal ----------

/**
 * Tarjeta principal del cliente: avatar 56dp, nombre verificado, teléfono,
 * domicilio con acceso a mapa y acciones rápidas Llamar / WhatsApp.
 */
@Composable
fun ClientProfileCard(
    name: String,
    initials: String,
    phone: String,
    addressLabel: String,
    address: String,
    onDirections: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    modifier: Modifier = Modifier,
    verified: Boolean = true,
) {
    Column(
        modifier.fillMaxWidth().cpCard(CpSectionShape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
            InitialsAvatar(
                initials = initials,
                containerColor = CpPrimaryFixed,
                contentColor = CpPrimary,
                size = 56.dp,
                textStyle = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                ),
                modifier = Modifier.shadow(1.dp, CircleShape, spotColor = Color.Black.copy(0.05f)),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, lineHeight = 28.sp),
                        color = CpOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).semantics { heading() },
                    )
                    if (verified) CpIcon(R.drawable.ic_verified)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    CpIcon(R.drawable.ic_smartphone)
                    Text(phone, style = MaterialTheme.typography.bodyMedium, color = CpOnSurfaceVariant, maxLines = 1)
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().background(CpSurfaceContainerLow, Radius8).padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            CpIcon(R.drawable.ic_location_pin)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.25.dp)) {
                Text(addressLabel.uppercase(), style = MaterialTheme.typography.labelSmall, color = CpTextSecondary)
                Text(
                    address,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.5.sp),
                    color = CpOnSurface,
                )
            }
            Box(
                Modifier
                    .clip(CircleShape)
                    .background(CpSurfaceContainer)
                    .clickable(role = Role.Button, onClickLabel = "Abrir en mapa", onClick = onDirections)
                    .padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 11.dp),
            ) {
                CpIcon(R.drawable.ic_directions)
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val quickActionStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.14.sp,
            )
            CpButton(
                text = "Llamar",
                onClick = onCall,
                contentColor = CpOnSurface,
                containerColor = CpSurfaceContainerLow,
                height = 48.dp,
                horizontalPadding = 12.dp,
                gap = 8.dp,
                textStyle = quickActionStyle,
                leadingIcon = R.drawable.ic_call,
                modifier = Modifier.weight(1f),
            )
            CpButton(
                text = "WhatsApp",
                onClick = onWhatsApp,
                contentColor = Color.White,
                containerColor = CpWhatsApp,
                height = 48.dp,
                horizontalPadding = 12.dp,
                gap = 8.dp,
                textStyle = quickActionStyle,
                leadingIcon = R.drawable.ic_whatsapp_white,
                modifier = Modifier
                    .weight(1f)
                    .shadow(1.dp, Radius8, spotColor = Color.Black.copy(0.05f)),
            )
        }
    }
}

// ---------- Mantenimiento sugerido ----------

/** Tarjeta azul tonal con la próxima fecha de mantenimiento recomendada. */
@Composable
fun MaintenanceSuggestionCard(
    label: String,
    date: String,
    note: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val glow = CpPrimaryFixed.copy(alpha = 0.4f)
    Box(
        modifier
            .fillMaxWidth()
            .shadow(1.dp, CpSectionShape, ambientColor = Color.Black.copy(0.05f), spotColor = Color.Black.copy(0.05f))
            .background(CpPrimaryFixed.copy(alpha = 0.3f).compositeOver(CpBackground), CpSectionShape)
            .clip(CpSectionShape),
    ) {
        // Halo decorativo difuminado (80dp, esquina superior derecha).
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 12.dp, y = (-12).dp)
                .size(104.dp)
                .background(Brush.radialGradient(listOf(glow, glow, Color.Transparent))),
        )
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Box(
                    Modifier
                        .padding(top = 2.dp)
                        .size(40.dp)
                        .shadow(1.dp, Radius8, spotColor = Color.Black.copy(0.05f))
                        .background(CpPrimaryContainer, Radius8),
                    contentAlignment = Alignment.Center,
                ) {
                    CpIcon(R.drawable.ic_event_upcoming_white)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        label.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.275.sp),
                        color = CpPrimary,
                    )
                    Text(
                        date,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = CpOnSurface,
                    )
                    Row(
                        Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CpIcon(R.drawable.ic_ac_unit_small)
                        Text(
                            note,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 15.sp),
                            color = CpOnSurfaceVariant,
                        )
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.End) {
                CpButton(
                    text = actionText,
                    onClick = onAction,
                    contentColor = CpPrimary,
                    containerColor = Color.White,
                    height = 28.dp,
                    horizontalPadding = 12.dp,
                    textStyle = MaterialTheme.typography.labelMedium,
                    leadingIcon = R.drawable.ic_alarm_add,
                    modifier = Modifier.shadow(1.dp, Radius8, spotColor = Color.Black.copy(0.05f)),
                )
            }
        }
    }
}

// ---------- Secciones ----------

/** Encabezado de sección con icono, título en negritas y acción/meta a la derecha. */
@Composable
fun DetailSectionHeader(
    title: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.semantics(mergeDescendants = true) { heading() },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CpIcon(icon)
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = CpOnSurface,
            )
        }
        trailing?.invoke(this)
    }
}

/** Pill compacta de metadato (refrigerante, "3 órdenes"). */
@Composable
fun MetaPill(text: String, containerColor: Color, contentColor: Color) {
    StatusBadge(
        text = text,
        containerColor = containerColor,
        contentColor = contentColor,
        textStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
    )
}

/** Tarjeta de equipo registrado con estado operativo y número de serie. */
@Composable
fun EquipmentCard(
    name: String,
    location: String,
    refrigerant: String,
    refrigerantContainer: Color,
    refrigerantContent: Color,
    status: String,
    serialNumber: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth().cpCard(CpSectionShape).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            Row(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    Modifier.size(36.dp).background(CpSurfaceContainerHigh, Radius8),
                    contentAlignment = Alignment.Center,
                ) {
                    CpIcon(R.drawable.ic_equipment_unit)
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        name,
                        style = MaterialTheme.typography.titleSmall.copy(lineHeight = 17.5.sp),
                        color = CpOnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(location, style = MaterialTheme.typography.bodySmall, color = CpOnSurfaceVariant, maxLines = 1)
                }
            }
            MetaPill(refrigerant, refrigerantContainer, refrigerantContent)
        }
        Row(
            Modifier.fillMaxWidth().background(CpSurfaceContainerLow, Radius8).padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CpIcon(R.drawable.ic_check_circle_success)
                Text(
                    status,
                    style = MaterialTheme.typography.labelMedium,
                    color = CpOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                "SN: $serialNumber",
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                color = CpTextSecondary,
                maxLines = 1,
            )
        }
    }
}

/** Tarjeta de orden de servicio en el historial del cliente. */
@Composable
fun ServiceHistoryCard(
    date: String,
    orderNumber: String,
    title: String,
    technician: String,
    statusLabel: String,
    evidenceCount: Int,
    onViewReport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth().cpCard(CpSectionShape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.5.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(CpSuccess)
                    Text(date, style = MaterialTheme.typography.labelSmall, color = CpTextSecondary)
                    Text("• OS #$orderNumber", style = MaterialTheme.typography.labelSmall, color = CpOutline)
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = CpOnSurface,
                    modifier = Modifier.padding(top = 2.5.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    CpIcon(R.drawable.ic_technician)
                    Text("Técnico: $technician", style = MaterialTheme.typography.bodySmall, color = CpOnSurfaceVariant)
                }
            }
            StatusBadge(text = statusLabel, containerColor = CpSuccessContainer, contentColor = CpOnSuccessContainer)
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                CpIcon(R.drawable.ic_photo_camera)
                Text(
                    if (evidenceCount == 1) "1 evidencia" else "$evidenceCount evidencias",
                    style = MaterialTheme.typography.labelSmall,
                    color = CpTextSecondary,
                )
            }
            CpButton(
                text = "Ver reporte PDF",
                onClick = onViewReport,
                contentColor = CpPrimary,
                containerColor = CpSurfaceContainerLow,
                height = 28.dp,
                horizontalPadding = 12.dp,
                gap = 6.dp,
                textStyle = MaterialTheme.typography.labelMedium,
                leadingIcon = R.drawable.ic_picture_as_pdf,
            )
        }
    }
}
