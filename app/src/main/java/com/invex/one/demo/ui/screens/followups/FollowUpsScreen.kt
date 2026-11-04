package com.invex.one.demo.ui.screens.followups

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpButton
import com.invex.one.demo.ui.components.atoms.CpIcon
import com.invex.one.demo.ui.components.atoms.StatusBadge
import com.invex.one.demo.ui.components.molecules.CountFilterChip
import com.invex.one.demo.ui.components.molecules.FollowUpCard
import com.invex.one.demo.ui.components.molecules.FollowUpGroupHeader
import com.invex.one.demo.ui.components.molecules.GoalProgressBanner
import com.invex.one.demo.ui.components.molecules.IconLine
import com.invex.one.demo.ui.theme.CpBackground
import com.invex.one.demo.ui.theme.CpError
import com.invex.one.demo.ui.theme.CpErrorContainer
import com.invex.one.demo.ui.theme.CpOnSurface
import com.invex.one.demo.ui.theme.CpOnSurfaceVariant
import com.invex.one.demo.ui.theme.CpPrimary
import com.invex.one.demo.ui.theme.CpPrimaryContainer
import com.invex.one.demo.ui.theme.CpSecondaryContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainerHigh
import com.invex.one.demo.ui.theme.CpTextMuted
import com.invex.one.demo.ui.theme.CpWarning
import com.invex.one.demo.ui.theme.CpWarningAccent
import com.invex.one.demo.ui.theme.CpWarningStrong
import com.invex.one.demo.ui.theme.DemoTheme

// ---------- Modelo de UI ----------

/** O7. Grupos de la lista de seguimientos con su código de color. */
enum class FollowUpGroup(val chipLabel: String, val dot: Color, val countContainer: Color, val countContent: Color) {
    Hoy("Para hoy", CpWarningAccent, CpWarningAccent.copy(alpha = 0.15f), CpWarningStrong),
    Proximos("Próximos", CpPrimary, CpSecondaryContainer, CpTextMuted),
    Atrasados("Atrasados", CpError, CpErrorContainer, CpError),
}

/** Distribución de acciones de la tarjeta según el diseño. */
enum class FollowUpActions { WhatsAppOrMarkContacted, WhatsAppOrSnooze, Reminder, OverdueReminder }

data class FollowUp(
    val id: String,
    val clientName: String,
    val phone: String,
    val group: FollowUpGroup,
    @DrawableRes val avatarIcon: Int,
    val avatarContainer: Color,
    val reasonTitle: String,
    val reasonDetail: String,
    val actions: FollowUpActions,
    val tag: String? = null,
    /** Icono del tipo de equipo (esquina superior derecha) para "Para hoy". */
    @DrawableRes val equipmentTag: Int? = null,
    @DrawableRes val reasonIcon: Int? = null,
    val note: String? = null,
    @DrawableRes val noteIcon: Int? = null,
    /** "Fecha sugerida: 21 Octubre" para próximos. */
    val suggestedDate: String? = null,
    /** Días de vencimiento para atrasados. */
    val overdueDays: Int? = null,
)

private val AvatarPrimaryTint = CpPrimary.copy(alpha = 0.1f)

val SampleFollowUps = listOf(
    FollowUp(
        "f1", "Roberto Garza", "+52 81 9988 7766", FollowUpGroup.Hoy,
        R.drawable.ic_ac_unit_large, AvatarPrimaryTint,
        reasonTitle = "Mantenimiento semestral sugerido",
        reasonDetail = "Minisplit Mirage 2 TR • Sala Principal",
        actions = FollowUpActions.WhatsAppOrMarkContacted,
        equipmentTag = R.drawable.ic_equipment_tag_ac,
        reasonIcon = R.drawable.ic_notification_amber,
        note = "Último servicio: Hace 6 meses (Instalación)",
        noteIcon = R.drawable.ic_history_muted,
    ),
    FollowUp(
        "f2", "Pastelería Suspiros", "+52 81 2233 4455", FollowUpGroup.Hoy,
        R.drawable.ic_storefront, CpSecondaryContainer,
        reasonTitle = "Revisión preventiva antes de calor",
        reasonDetail = "Cámara fría + 2 Minisplits Inverter (Área Cocina)",
        actions = FollowUpActions.WhatsAppOrSnooze,
        tag = "Comercial",
        equipmentTag = R.drawable.ic_equipment_tag_thermo,
        reasonIcon = R.drawable.ic_sunny,
        note = "Equipo crítico para operaciones",
        noteIcon = R.drawable.ic_warning_outline,
    ),
    FollowUp(
        "f3", "Dra. Sofía Morales", "+52 81 4455 6677", FollowUpGroup.Proximos,
        R.drawable.ic_medical, AvatarPrimaryTint,
        reasonTitle = "Sanitización de ductos y cambio de filtro HEPA",
        reasonDetail = "Consultorio Médico San Pedro • Torre Médica 401",
        actions = FollowUpActions.Reminder,
        suggestedDate = "21 Octubre",
    ),
    FollowUp(
        "f4", "Restaurante El Fogón", "+52 81 8765 4321", FollowUpGroup.Proximos,
        R.drawable.ic_storefront, CpSecondaryContainer,
        reasonTitle = "Limpieza de condensadoras de cocina",
        reasonDetail = "2 Minisplits 2 TR • Área de comedor",
        actions = FollowUpActions.Reminder,
        tag = "Comercial",
        suggestedDate = "23 Octubre",
    ),
    FollowUp(
        "f5", "María López", "+52 81 1234 5678", FollowUpGroup.Proximos,
        R.drawable.ic_ac_unit_large, AvatarPrimaryTint,
        reasonTitle = "Mantenimiento preventivo semestral",
        reasonDetail = "Minisplit Carrier 2 TR • Sala Comedor",
        actions = FollowUpActions.Reminder,
        suggestedDate = "24 Octubre",
    ),
    FollowUp(
        "f6", "Lic. Fernando Villarreal", "+52 81 3344 5566", FollowUpGroup.Atrasados,
        R.drawable.ic_priority_high, CpErrorContainer,
        reasonTitle = "Mantenimiento preventivo anual",
        reasonDetail = "Equipo Central York 5 TR • Domicilio Residencial",
        actions = FollowUpActions.OverdueReminder,
        overdueDays = 4,
    ),
)

// ---------- Pantalla 7 — Seguimientos ----------

@Composable
fun FollowUpsScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    followUps: List<FollowUp> = SampleFollowUps,
    todayLabel: String = "18 de Octubre",
    dailyGoal: Int = 3,
    initialContacted: Int = 1,
    onHistory: () -> Unit = {},
    onWhatsApp: (FollowUp) -> Unit = {},
) {
    // null = "Todos".
    var filter by rememberSaveable { mutableStateOf<FollowUpGroup?>(null) }
    var contactedIds by rememberSaveable { mutableStateOf(listOf<String>()) }
    var snoozedIds by rememberSaveable { mutableStateOf(listOf<String>()) }

    val pending = followUps
        .filterNot { it.id in contactedIds }
        .map { if (it.id in snoozedIds) it.snoozed() else it }
    val byGroup = FollowUpGroup.entries.associateWith { g -> pending.filter { it.group == g } }
    val contacted = initialContacted + contactedIds.size

    val layoutDirection = LocalLayoutDirection.current
    LazyColumn(
        modifier.fillMaxSize().background(CpBackground),
        contentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(layoutDirection),
            end = contentPadding.calculateEndPadding(layoutDirection),
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 32.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item(key = "header") { Header(onHistory, Modifier.padding(horizontal = 16.dp)) }

        item(key = "filters") {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    CountFilterChip(
                        label = "Todos",
                        count = pending.size,
                        selected = filter == null,
                        onClick = { filter = null },
                        countContainer = CpSurfaceContainerHigh,
                        countContent = CpOnSurfaceVariant,
                    )
                }
                items(FollowUpGroup.entries) { g ->
                    CountFilterChip(
                        label = g.chipLabel,
                        count = byGroup.getValue(g).size,
                        selected = filter == g,
                        onClick = { filter = g },
                        countContainer = g.countContainer,
                        countContent = g.countContent,
                    )
                }
            }
        }

        item(key = "goal") {
            GoalProgressBanner(
                title = "Meta de Re-contacto",
                subtitle = "${contacted.coerceAtMost(dailyGoal)} de $dailyGoal clientes reactivados hoy",
                progress = contacted.toFloat() / dailyGoal,
                trailingIcon = R.drawable.ic_goal_check,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        if (filter != null && byGroup.getValue(filter!!).isEmpty()) {
            item(key = "empty") {
                Text(
                    "Sin seguimientos pendientes en este grupo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CpOnSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                )
            }
        }

        FollowUpGroup.entries
            .filter { filter == null || filter == it }
            .forEach { group ->
                val list = byGroup.getValue(group)
                if (list.isEmpty()) return@forEach
                item(key = "group-${group.name}") {
                    Column(
                        Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        GroupHeader(group, list.size, todayLabel)
                        list.forEach { f ->
                            FollowUpItem(
                                f,
                                onWhatsApp = { onWhatsApp(f) },
                                onMarkContacted = { contactedIds = contactedIds + f.id },
                                onSnooze = { snoozedIds = snoozedIds + f.id },
                            )
                        }
                    }
                }
            }
    }
}

/** Pospone una semana: pasa al grupo "Próximos". */
private fun FollowUp.snoozed() = copy(
    group = FollowUpGroup.Proximos,
    actions = FollowUpActions.Reminder,
    suggestedDate = "en 1 semana",
    equipmentTag = null,
)

@Composable
private fun Header(onHistory: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(32.dp).background(CpPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    CpIcon(R.drawable.ic_followups_title)
                }
                Text(
                    "Seguimientos",
                    style = MaterialTheme.typography.titleLarge.copy(lineHeight = 28.sp),
                    color = CpOnSurface,
                )
            }
            CpButton(
                text = "Historial",
                onClick = onHistory,
                contentColor = CpOnSurfaceVariant,
                containerColor = CpSurfaceContainerHigh,
                height = 26.dp,
                horizontalPadding = 12.dp,
                gap = 6.dp,
                leadingIcon = R.drawable.ic_history_small,
                shape = CircleShape,
            )
        }
        Text(
            "Gestiona mantenimientos preventivos programados de tus clientes",
            style = MaterialTheme.typography.bodySmall,
            color = CpOnSurfaceVariant,
        )
    }
}

@Composable
private fun GroupHeader(group: FollowUpGroup, count: Int, todayLabel: String) {
    when (group) {
        FollowUpGroup.Hoy -> FollowUpGroupHeader("Para hoy — $todayLabel", group.dot) {
            StatusBadge("Urgente", CpWarningAccent.copy(alpha = 0.15f), CpWarning)
        }
        FollowUpGroup.Proximos -> FollowUpGroupHeader("Próximos 7 días", group.dot) {
            Text(
                if (count == 1) "1 cliente" else "$count clientes",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = CpPrimary,
            )
        }
        FollowUpGroup.Atrasados -> FollowUpGroupHeader("Atrasados", group.dot, titleColor = CpError) {
            StatusBadge("Atención requerida", CpErrorContainer, CpError)
        }
    }
}

@Composable
private fun FollowUpItem(
    f: FollowUp,
    onWhatsApp: () -> Unit,
    onMarkContacted: () -> Unit,
    onSnooze: () -> Unit,
) {
    FollowUpCard(
        name = f.clientName,
        avatarIcon = f.avatarIcon,
        avatarContainer = f.avatarContainer,
        tag = f.tag,
        accentColor = if (f.group == FollowUpGroup.Atrasados) CpError else null,
        subline = {
            when {
                f.overdueDays != null -> IconLine(
                    R.drawable.ic_calendar_overdue,
                    if (f.overdueDays == 1) "Vencido hace 1 día" else "Vencido hace ${f.overdueDays} días",
                    CpError,
                )
                f.suggestedDate != null -> IconLine(
                    R.drawable.ic_calendar_primary_small,
                    "Fecha sugerida: ${f.suggestedDate}",
                    CpOnSurface,
                    fontWeight = FontWeight.Medium,
                )
                else -> IconLine(R.drawable.ic_phone_tiny, f.phone, CpPrimary)
            }
        },
        trailing = when {
            f.equipmentTag != null -> ({ CpIcon(f.equipmentTag) })
            f.group == FollowUpGroup.Proximos -> ({
                StatusBadge(
                    "Programado", CpSecondaryContainer, CpTextMuted,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                )
            })
            f.group == FollowUpGroup.Atrasados -> ({ StatusBadge("Sin contactar", CpErrorContainer, CpError) })
            else -> null
        },
        reason = {
            if (f.reasonIcon != null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        CpIcon(f.reasonIcon)
                        Text(f.reasonTitle, style = MaterialTheme.typography.labelMedium, color = CpOnSurface)
                    }
                    Text(f.reasonDetail, style = MaterialTheme.typography.bodySmall, color = CpOnSurfaceVariant)
                    if (f.note != null && f.noteIcon != null) {
                        IconLine(f.noteIcon, f.note, CpOnSurfaceVariant, Modifier.padding(top = 2.dp))
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(f.reasonTitle, style = MaterialTheme.typography.titleSmall, color = CpOnSurface)
                    Text(f.reasonDetail, style = MaterialTheme.typography.bodySmall, color = CpOnSurfaceVariant)
                }
            }
        },
        actions = { FollowUpActionsRow(f.actions, onWhatsApp, onMarkContacted, onSnooze) },
    )
}

@Composable
private fun FollowUpActionsRow(
    layout: FollowUpActions,
    onWhatsApp: () -> Unit,
    onMarkContacted: () -> Unit,
    onSnooze: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    val subtleShadow = Modifier.shadow(1.dp, shape, spotColor = Color.Black.copy(0.05f))
    val actionStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.14.sp)
    when (layout) {
        FollowUpActions.WhatsAppOrMarkContacted -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CpButton(
                "Contactar por WhatsApp", onWhatsApp, Color.White,
                containerColor = CpPrimaryContainer, height = 48.dp, gap = 8.dp, textStyle = actionStyle,
                leadingIcon = R.drawable.ic_whatsapp_white, modifier = Modifier.fillMaxWidth().then(subtleShadow),
            )
            CpButton(
                "Marcar como contactado", onMarkContacted, CpOnSurface,
                containerColor = CpSurfaceContainer, height = 44.dp, gap = 8.dp, textStyle = actionStyle,
                leadingIcon = R.drawable.ic_done_all, modifier = Modifier.fillMaxWidth(),
            )
        }
        FollowUpActions.WhatsAppOrSnooze -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CpButton(
                "WhatsApp", onWhatsApp, Color.White,
                containerColor = CpPrimaryContainer, height = 48.dp, gap = 6.dp,
                textStyle = MaterialTheme.typography.labelMedium,
                leadingIcon = R.drawable.ic_whatsapp_white_small, modifier = Modifier.weight(1f).then(subtleShadow),
            )
            CpButton(
                "Posponer 1 sem", onSnooze, CpOnSurface,
                containerColor = CpSurfaceContainer, height = 48.dp, horizontalPadding = 20.dp, gap = 6.dp,
                textStyle = MaterialTheme.typography.labelMedium, leadingIcon = R.drawable.ic_snooze_dark,
            )
        }
        FollowUpActions.Reminder -> CpButton(
            "Recordatorio WhatsApp", onWhatsApp, CpOnSurface,
            containerColor = CpSurfaceContainerHigh, height = 44.dp, gap = 8.dp, textStyle = actionStyle,
            leadingIcon = R.drawable.ic_mark_email, modifier = Modifier.fillMaxWidth(),
        )
        FollowUpActions.OverdueReminder -> CpButton(
            "Enviar recordatorio", onWhatsApp, Color.White,
            containerColor = CpError, height = 48.dp, gap = 8.dp, textStyle = actionStyle,
            leadingIcon = R.drawable.ic_send_white_small, modifier = Modifier.fillMaxWidth().then(subtleShadow),
        )
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 1800)
@Composable
private fun FollowUpsScreenPreview() {
    DemoTheme { FollowUpsScreen(contentPadding = PaddingValues()) }
}
