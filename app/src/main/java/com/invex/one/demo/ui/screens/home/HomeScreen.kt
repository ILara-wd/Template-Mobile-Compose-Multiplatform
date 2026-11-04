package com.invex.one.demo.ui.screens.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpButton
import com.invex.one.demo.ui.components.atoms.CpCardShape
import com.invex.one.demo.ui.components.atoms.CpIcon
import com.invex.one.demo.ui.components.atoms.StatusBadge
import com.invex.one.demo.ui.components.atoms.StatusDot
import com.invex.one.demo.ui.components.molecules.MetricCard
import com.invex.one.demo.ui.components.molecules.PriorityFollowUpCard
import com.invex.one.demo.ui.components.molecules.SectionHeader
import com.invex.one.demo.ui.components.molecules.SeeAllLink
import com.invex.one.demo.ui.components.molecules.ServiceItemCard
import com.invex.one.demo.ui.components.molecules.UpcomingFollowUpCard
import com.invex.one.demo.ui.theme.CpBackground
import com.invex.one.demo.ui.theme.CpOnSuccessContainer
import com.invex.one.demo.ui.theme.CpOnSurface
import com.invex.one.demo.ui.theme.CpOnWarningContainer
import com.invex.one.demo.ui.theme.CpPrimary
import com.invex.one.demo.ui.theme.CpPrimaryContainer
import com.invex.one.demo.ui.theme.CpSecondaryContainer
import com.invex.one.demo.ui.theme.CpSuccessContainer
import com.invex.one.demo.ui.theme.CpTextMuted
import com.invex.one.demo.ui.theme.CpTextSecondary
import com.invex.one.demo.ui.theme.CpWarning
import com.invex.one.demo.ui.theme.CpWarningContainer
import com.invex.one.demo.ui.theme.DemoTheme

// ---------- Modelo de UI ----------

enum class ServiceStatus(val label: String) {
    EnCurso("En curso"),
    Completado("Completado"),
    ReporteEntregado("Reporte entregado"),
}

enum class ServiceAction(val label: String) { Continuar("Continuar"), VerReporte("Ver reporte"), Detalles("Detalles") }

data class RecentService(
    val id: String,
    val clientName: String,
    val detail: String,
    val status: ServiceStatus,
    @DrawableRes val dateIcon: Int,
    val dateText: String,
    val action: ServiceAction,
)

data class HomeUiState(
    val workshopStatus: String = "En servicio • Taller ClimaExpress",
    val dateLabel: String = "Jueves, 17 Oct",
    val technicianName: String = "Carlos",
    val weekServices: Int = 12,
    val weekDelta: String = "+3 vs anterior",
    val pendingReports: Int = 2,
    val followUpsNext7Days: Int = 4,
    val recentServices: List<RecentService> = SampleRecentServices,
    val priorityFollowUps: Int = 1,
)

val SampleRecentServices = listOf(
    RecentService(
        "1", "María López", "Minisplit Inverter 1.5 TR • C. Roble 412",
        ServiceStatus.EnCurso, R.drawable.ic_clock, "Hoy, 09:30 AM", ServiceAction.Continuar,
    ),
    RecentService(
        "2", "Restaurante El Fogón", "Mantenimiento preventivo x2 unidades",
        ServiceStatus.Completado, R.drawable.ic_calendar_day, "Ayer, 04:15 PM", ServiceAction.VerReporte,
    ),
    RecentService(
        "3", "Oficinas Grupo Norte", "Reparación fuga de gas R410A • Piso 3",
        ServiceStatus.ReporteEntregado, R.drawable.ic_calendar_event, "14 Oct • 11:20 AM", ServiceAction.Detalles,
    ),
)

// ---------- Pantalla 1 — Inicio / Resumen ----------

@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    state: HomeUiState = HomeUiState(),
    onNewService: () -> Unit = {},
    onSeeAllServices: () -> Unit = {},
    onServiceAction: (RecentService) -> Unit = {},
    onWhatsApp: () -> Unit = {},
    onSnooze: () -> Unit = {},
    onSchedule: () -> Unit = {},
) {
    Column(
        modifier
            .fillMaxSize()
            .background(CpBackground)
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 40.dp),
    ) {
        Greeting(state)

        PrimaryActionButton(
            text = "Nuevo servicio",
            onClick = onNewService,
            modifier = Modifier.padding(top = 16.dp),
        )

        Row(
            Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MetricCard(
                icon = R.drawable.ic_metric_week,
                label = "Semana",
                value = state.weekServices.toString(),
                caption = state.weekDelta,
                captionColor = CpPrimary,
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                icon = R.drawable.ic_metric_pending,
                label = "Pendientes",
                value = state.pendingReports.toString(),
                caption = "Por entregar hoy",
                valueColor = CpWarning,
                captionColor = CpOnWarningContainer,
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                icon = R.drawable.ic_metric_followups,
                label = "Seguimientos",
                value = state.followUpsNext7Days.toString(),
                caption = "Próx. 7 días",
                modifier = Modifier.weight(1f),
            )
        }

        SectionHeader(
            icon = R.drawable.ic_section_services,
            title = "Servicios recientes",
            modifier = Modifier.padding(top = 16.dp),
        ) { SeeAllLink("Ver todos", onSeeAllServices) }

        Column(
            Modifier.padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.recentServices.forEach { service ->
                ServiceItemCard(
                    clientName = service.clientName,
                    detail = service.detail,
                    status = { ServiceStatusBadge(service.status) },
                    dateIcon = service.dateIcon,
                    dateText = service.dateText,
                    action = { ServiceActionButton(service.action) { onServiceAction(service) } },
                )
            }
        }

        SectionHeader(
            icon = R.drawable.ic_section_followups,
            title = "Próximos seguimientos",
            modifier = Modifier.padding(top = 16.dp),
        ) {
            StatusBadge(
                text = "${state.priorityFollowUps} Prioritario",
                containerColor = CpWarningContainer,
                contentColor = CpOnWarningContainer,
            )
        }

        PriorityFollowUpCard(
            clientName = "Roberto Garza",
            reason = "Mantenimiento semestral sugerido",
            dueLabel = "Mañana",
            dueDate = "18 Oct",
            onWhatsApp = onWhatsApp,
            onSnooze = onSnooze,
            modifier = Modifier.padding(top = 10.dp),
        )
        UpcomingFollowUpCard(
            clientName = "Dra. Sofía Morales",
            reason = "Filtros y sanitización clínica",
            inDaysLabel = "En 3 días",
            onSchedule = onSchedule,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

@Composable
private fun Greeting(state: HomeUiState) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusBadge(
                text = state.workshopStatus,
                containerColor = CpSecondaryContainer,
                contentColor = CpTextMuted,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                leading = { StatusDot(CpPrimary) },
            )
            Text(state.dateLabel, style = MaterialTheme.typography.labelSmall, color = CpTextSecondary)
        }
        Text(
            "Buenos días, ${state.technicianName}",
            style = MaterialTheme.typography.headlineSmall,
            color = CpOnSurface,
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            "Este es el resumen de tu actividad",
            style = MaterialTheme.typography.bodyMedium,
            color = CpTextSecondary,
        )
    }
}

/** Botón primario grande (48dp, radio 8dp, Blue-600). */
@Composable
private fun PrimaryActionButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .height(48.dp)
            .shadow(1.dp, CpCardShape, ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.05f))
            .clip(CpCardShape)
            .background(CpPrimaryContainer)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CpIcon(R.drawable.ic_add_circle)
        Text(text, style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}

@Composable
private fun ServiceStatusBadge(status: ServiceStatus) {
    val (container, content) = when (status) {
        ServiceStatus.EnCurso -> CpSecondaryContainer to CpPrimary
        ServiceStatus.Completado, ServiceStatus.ReporteEntregado -> CpSuccessContainer to CpOnSuccessContainer
    }
    StatusBadge(text = status.label, containerColor = container, contentColor = content)
}

@Composable
private fun ServiceActionButton(action: ServiceAction, onClick: () -> Unit) {
    when (action) {
        ServiceAction.Continuar -> CpButton(
            text = action.label,
            onClick = onClick,
            contentColor = CpPrimary,
            containerColor = CpSecondaryContainer,
            horizontalPadding = 12.dp,
            trailingIcon = R.drawable.ic_arrow_forward,
        )
        ServiceAction.VerReporte -> CpButton(
            text = action.label,
            onClick = onClick,
            contentColor = CpTextSecondary,
            trailingIcon = R.drawable.ic_report,
        )
        ServiceAction.Detalles -> CpButton(
            text = action.label,
            onClick = onClick,
            contentColor = CpTextSecondary,
            trailingIcon = R.drawable.ic_chevron_right_small,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1100)
@Composable
private fun HomeScreenPreview() {
    DemoTheme { HomeScreen(contentPadding = PaddingValues(top = 56.dp, bottom = 64.dp)) }
}
