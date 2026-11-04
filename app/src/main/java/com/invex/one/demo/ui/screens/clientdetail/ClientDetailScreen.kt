package com.invex.one.demo.ui.screens.clientdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpButton
import com.invex.one.demo.ui.components.molecules.ClientContextBanner
import com.invex.one.demo.ui.components.molecules.ClientProfileCard
import com.invex.one.demo.ui.components.molecules.DetailSectionHeader
import com.invex.one.demo.ui.components.molecules.EquipmentCard
import com.invex.one.demo.ui.components.molecules.MaintenanceSuggestionCard
import com.invex.one.demo.ui.components.molecules.MetaPill
import com.invex.one.demo.ui.components.molecules.ServiceHistoryCard
import com.invex.one.demo.ui.components.organisms.CierreProDetailTopBar
import com.invex.one.demo.ui.screens.clients.Client
import com.invex.one.demo.ui.theme.CpBackground
import com.invex.one.demo.ui.theme.CpPrimary
import com.invex.one.demo.ui.theme.CpPrimaryContainer
import com.invex.one.demo.ui.theme.CpPrimaryFixed
import com.invex.one.demo.ui.theme.CpSecondaryContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainer
import com.invex.one.demo.ui.theme.CpTextMuted
import com.invex.one.demo.ui.theme.CpTextSecondary
import com.invex.one.demo.ui.theme.DemoTheme

// ---------- Modelo de UI ----------

/** Refrigerante con su código de color en la pill del equipo. */
enum class Refrigerant(val label: String, val container: Color, val content: Color) {
    R410A("R410A", CpSecondaryContainer, CpTextMuted),
    R32("R32", CpPrimaryFixed, CpPrimary),
}

data class RegisteredEquipment(
    val id: String,
    val name: String,
    val location: String,
    val refrigerant: Refrigerant,
    val status: String,
    val serialNumber: String,
)

data class ServiceOrder(
    val number: String,
    val date: String,
    val title: String,
    val technician: String,
    val statusLabel: String,
    val evidenceCount: Int,
)

data class MaintenanceSuggestion(val date: String, val note: String)

data class ClientDetail(
    val id: String,
    val code: String,
    val segment: String,
    val name: String,
    val initials: String,
    val phone: String,
    val address: String,
    val warrantyActive: Boolean,
    val nextMaintenance: MaintenanceSuggestion?,
    val equipment: List<RegisteredEquipment>,
    val history: List<ServiceOrder>,
) {
    val bannerText: String get() = "$segment • ID #$code"
}

val SampleClientDetail = ClientDetail(
    id = "1",
    code = "CL-8821",
    segment = "Cliente frecuente",
    name = "María López",
    initials = "ML",
    phone = "+52 81 1234 5678",
    address = "Av. Paseo de los Leones #2450,\nCumbres 2° Sector, Monterrey",
    warrantyActive = true,
    nextMaintenance = MaintenanceSuggestion(
        date = "15 de Noviembre 2024",
        note = "Servicio preventivo semestral antes de invierno",
    ),
    equipment = listOf(
        RegisteredEquipment(
            "e1", "Minisplit Inverter Mirage 1.5 TR", "Recámara Principal",
            Refrigerant.R410A, "Operativo óptimo (110 PSI)", "MRG-9941-X",
        ),
        RegisteredEquipment(
            "e2", "Minisplit Carrier 2 TR", "Sala Comedor",
            Refrigerant.R32, "Carga reciente (125 PSI)", "CRR-2023-B8",
        ),
    ),
    history = listOf(
        ServiceOrder("1042", "16 Oct 2024", "Mantenimiento Preventivo", "Carlos M.", "Finalizado", 4),
        ServiceOrder("0879", "12 May 2024", "Limpieza de filtros y carga de gas", "Carlos M.", "Finalizado", 2),
        ServiceOrder("0451", "10 Nov 2023", "Instalación de unidad nueva", "Carlos M.", "Finalizado", 6),
    ),
)

/** Ficha de detalle para un cliente del directorio (datos de muestra mientras no hay backend). */
fun Client.toDetail(): ClientDetail =
    if (id == SampleClientDetail.id) {
        SampleClientDetail
    } else {
        ClientDetail(
            id = id,
            code = "CL-${id.padStart(4, '0')}",
            segment = "Cliente",
            name = name,
            initials = initials,
            phone = phone,
            address = location,
            warrantyActive = false,
            nextMaintenance = null,
            equipment = emptyList(),
            history = emptyList(),
        )
    }

// ---------- Pantalla 3 — Detalle del Cliente ----------

@Composable
fun ClientDetailScreen(
    client: ClientDetail,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onCall: () -> Unit = {},
    onWhatsApp: () -> Unit = {},
    onDirections: () -> Unit = {},
    onNewService: () -> Unit = {},
    onScheduleReminder: () -> Unit = {},
    onAddEquipment: () -> Unit = {},
    onViewReport: (ServiceOrder) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = CpBackground,
        topBar = {
            CierreProDetailTopBar(title = "Detalle De Servicio", onBack = onBack, onProfileClick = onProfileClick)
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(bottom = 32.dp),
        ) {
            ClientContextBanner(
                text = client.bannerText,
                badge = if (client.warrantyActive) "Garantía Activa" else null,
            )

            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ClientProfileCard(
                    name = client.name,
                    initials = client.initials,
                    phone = client.phone,
                    addressLabel = "Domicilio de servicio",
                    address = client.address,
                    onDirections = onDirections,
                    onCall = onCall,
                    onWhatsApp = onWhatsApp,
                )

                val primaryShape = RoundedCornerShape(12.dp)
                CpButton(
                    text = "Nuevo servicio para este cliente",
                    onClick = onNewService,
                    contentColor = Color.White,
                    containerColor = CpPrimaryContainer,
                    height = 48.dp,
                    horizontalPadding = 16.dp,
                    gap = 10.dp,
                    textStyle = MaterialTheme.typography.labelLarge,
                    leadingIcon = R.drawable.ic_build_white,
                    shape = primaryShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, primaryShape, ambientColor = Color.Black.copy(0.1f), spotColor = Color.Black.copy(0.1f)),
                )

                client.nextMaintenance?.let {
                    MaintenanceSuggestionCard(
                        label = "Mantenimiento sugerido",
                        date = it.date,
                        note = it.note,
                        actionText = "Agendar Recordatorio",
                        onAction = onScheduleReminder,
                    )
                }

                Column(
                    Modifier.padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    DetailSectionHeader(
                        title = "Equipos Registrados (${client.equipment.size})",
                        icon = R.drawable.ic_hvac,
                        trailing = {
                            CpButton(
                                text = "Equipo",
                                onClick = onAddEquipment,
                                contentColor = CpPrimary,
                                height = 24.dp,
                                horizontalPadding = 8.dp,
                                gap = 2.dp,
                                textStyle = MaterialTheme.typography.labelMedium,
                                leadingIcon = R.drawable.ic_add_small,
                                shape = RoundedCornerShape(4.dp),
                            )
                        },
                    )
                    client.equipment.forEach { eq ->
                        EquipmentCard(
                            name = eq.name,
                            location = eq.location,
                            refrigerant = eq.refrigerant.label,
                            refrigerantContainer = eq.refrigerant.container,
                            refrigerantContent = eq.refrigerant.content,
                            status = eq.status,
                            serialNumber = eq.serialNumber,
                        )
                    }
                }

                Column(
                    Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    val count = client.history.size
                    DetailSectionHeader(
                        title = "Historial de Servicios",
                        icon = R.drawable.ic_history,
                        trailing = {
                            MetaPill(
                                text = if (count == 1) "1 orden" else "$count órdenes",
                                containerColor = CpSurfaceContainer,
                                contentColor = CpTextSecondary,
                            )
                        },
                    )
                    client.history.forEach { order ->
                        ServiceHistoryCard(
                            date = order.date,
                            orderNumber = order.number,
                            title = order.title,
                            technician = order.technician,
                            statusLabel = order.statusLabel,
                            evidenceCount = order.evidenceCount,
                            onViewReport = { onViewReport(order) },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 1500)
@Composable
private fun ClientDetailScreenPreview() {
    DemoTheme { ClientDetailScreen(client = SampleClientDetail, onBack = {}) }
}
