package com.invex.one.demo

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.invex.one.demo.ui.screens.clientdetail.ClientDetailScreen
import com.invex.one.demo.ui.screens.clientdetail.toDetail
import com.invex.one.demo.ui.screens.clients.ClientsScreen
import com.invex.one.demo.ui.screens.clients.SampleClients
import androidx.compose.ui.tooling.preview.Preview
import com.invex.one.demo.ui.components.organisms.CierreProBottomBar
import com.invex.one.demo.ui.components.organisms.CierreProTopBar
import com.invex.one.demo.ui.components.organisms.CpDestination
import com.invex.one.demo.ui.screens.followups.FollowUpsScreen
import com.invex.one.demo.ui.screens.home.HomeScreen
import com.invex.one.demo.ui.screens.newservice.NewServiceDraft
import com.invex.one.demo.ui.screens.newservice.NewServiceScreen
import com.invex.one.demo.ui.screens.newservice.SampleServiceEquipment
import com.invex.one.demo.ui.screens.newservice.ServiceType
import com.invex.one.demo.ui.screens.worklog.WorkLogScreen
import com.invex.one.demo.ui.screens.profile.BusinessProfile
import com.invex.one.demo.ui.screens.profile.BusinessProfileScreen
import com.invex.one.demo.ui.screens.profile.SampleBusinessProfile
import com.invex.one.demo.ui.screens.report.SampleServiceReport
import com.invex.one.demo.ui.screens.report.ServiceReportScreen
import com.invex.one.demo.ui.screens.newservice.SampleServiceClient
import com.invex.one.demo.ui.screens.newservice.ServiceClientSummary
import com.invex.one.demo.ui.theme.CpBackground
import com.invex.one.demo.ui.theme.CpTextSecondary
import com.invex.one.demo.ui.theme.DemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val transparent = Color.Transparent.toArgb()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(transparent, transparent),
            navigationBarStyle = SystemBarStyle.light(transparent, transparent),
        )
        setContent {
            DemoTheme {
                CierreProApp()
            }
        }
    }
}

private const val FlowKeyNewService = "flow:new-service"
private const val FlowKeyWorkLog = "flow:work-log"

@Composable
fun CierreProApp() {
    var destination by rememberSaveable { mutableStateOf(CpDestination.Inicio) }
    var showNewService by rememberSaveable { mutableStateOf(false) }
    var detailClientId by rememberSaveable { mutableStateOf<String?>(null) }
    var showReport by rememberSaveable { mutableStateOf(false) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    var showWorkLog by rememberSaveable { mutableStateOf(false) }
    // Datos del Paso 1 para el resumen del Paso 2 (en memoria; si se pierden se usan los de ejemplo).
    var serviceDraft by remember { mutableStateOf<NewServiceDraft?>(null) }
    var profile by rememberSaveable(stateSaver = BusinessProfile.Saver) { mutableStateOf(SampleBusinessProfile) }
    // Conserva el estado de cada paso del flujo (Paso 1 / Paso 2) mientras se navega
    // entre ellos, al reporte o al perfil; se descarta al cerrar o completar el flujo.
    val flowState = rememberSaveableStateHolder()
    fun endFlow() {
        showReport = false
        showWorkLog = false
        showNewService = false
        flowState.removeState(FlowKeyNewService)
        flowState.removeState(FlowKeyWorkLog)
    }
    val context = LocalContext.current
    val detailClient = detailClientId?.let { id -> SampleClients.firstOrNull { it.id == id }?.toDetail() }

    fun open(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            // Sin app que atienda la acción (p. ej. WhatsApp no instalado).
        }
    }
    fun dial(phone: String) = open(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone.filter { it.isDigit() || it == '+' })))

    if (showProfile) {
        BackHandler { showProfile = false }
        BusinessProfileScreen(
            saved = profile,
            onSave = { profile = it },
            onBack = { showProfile = false },
        )
        return
    }
    val openProfile = { showProfile = true }

    if (showReport) {
        // TODO: cargar el reporte real de la orden; por ahora muestra el de ejemplo con los datos del cliente
        // (el del flujo en curso, o el de la ficha desde donde se abrió).
        val flowClient = serviceDraft?.client?.takeIf { showWorkLog }
        val report = when {
            flowClient != null -> SampleServiceReport.copy(clientName = flowClient.name, clientPhone = flowClient.phone)
            detailClient != null -> SampleServiceReport.copy(clientName = detailClient.name, clientPhone = detailClient.phone)
            else -> SampleServiceReport
        }
        val reportWithProfile = report.copy(workshopName = profile.businessName)
        BackHandler { showReport = false }
        ServiceReportScreen(
            report = reportWithProfile,
            onProfileClick = openProfile,
            onBack = { showReport = false },
            onSendWhatsApp = {
                val text = "${profile.reportMessage}\n\nFolio: ${report.folio} — ${profile.businessName}"
                val phone = report.clientPhone.filter(Char::isDigit)
                open(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phone?text=" + Uri.encode(text))))
            },
            // TODO: generar el PDF y abrir la hoja de compartir del sistema.
            onSharePdf = {},
            onComplete = { endFlow() },
        )
        return
    }

    if (showWorkLog) {
        BackHandler { showWorkLog = false }
        val draft = serviceDraft
        flowState.SaveableStateProvider(FlowKeyWorkLog) {
            WorkLogScreen(
                clientName = draft?.client?.name ?: SampleServiceClient.name,
                equipmentName = draft?.equipment?.name ?: SampleServiceEquipment.name,
                serviceType = (draft?.type ?: ServiceType.Mantenimiento).let {
                    if (it == ServiceType.Mantenimiento) "Mantenimiento Preventivo" else it.label
                },
                onProfileClick = openProfile,
                onBack = { showWorkLog = false },
                // TODO: alimentar el reporte con el checklist, fotos y notas capturados.
                onContinue = { showReport = true },
            )
        }
        return
    }

    if (showNewService) {
        // Flujo de captura a pantalla completa (sin Bottom Navigation).
        BackHandler { endFlow() }
        flowState.SaveableStateProvider(FlowKeyNewService) {
            NewServiceScreen(
                onProfileClick = openProfile,
                onBack = { endFlow() },
                onCancel = { endFlow() },
                onContinue = { draft ->
                    serviceDraft = draft
                    showWorkLog = true
                },
                client = detailClient?.let {
                    ServiceClientSummary(it.name, it.phone, it.address.substringAfterLast(", ").trim())
                } ?: SampleServiceClient,
            )
        }
        return
    }

    if (detailClient != null) {
        BackHandler { detailClientId = null }
        ClientDetailScreen(
            client = detailClient,
            onProfileClick = openProfile,
            onBack = { detailClientId = null },
            onCall = { dial(detailClient.phone) },
            onWhatsApp = {
                open(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + detailClient.phone.filter(Char::isDigit))))
            },
            onDirections = {
                open(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(detailClient.address.replace("\n", " ")))))
            },
            onNewService = { showNewService = true },
            onViewReport = { showReport = true },
        )
        return
    }

    Scaffold(
        containerColor = CpBackground,
        topBar = { CierreProTopBar(sectionTitle = destination.label, onProfileClick = openProfile) },
        bottomBar = { CierreProBottomBar(selected = destination, onSelect = { destination = it }) },
    ) { innerPadding ->
        when (destination) {
            CpDestination.Inicio -> HomeScreen(
                contentPadding = innerPadding,
                onNewService = { showNewService = true },
            )
            CpDestination.Clientes -> ClientsScreen(
                contentPadding = innerPadding,
                onClientClick = { detailClientId = it.id },
                onCall = { dial(it.phone) },
            )
            CpDestination.Seguimientos -> FollowUpsScreen(
                contentPadding = innerPadding,
                onWhatsApp = { f ->
                    val text = "Hola ${f.clientName}, te contactamos de ${profile.businessName} para programar: ${f.reasonTitle}."
                    open(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://wa.me/${f.phone.filter(Char::isDigit)}?text=" + Uri.encode(text)),
                        ),
                    )
                },
            )
            else -> PlaceholderScreen(destination.label, innerPadding)
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String, padding: PaddingValues) {
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Text("$title — próximamente", style = MaterialTheme.typography.bodyMedium, color = CpTextSecondary)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1024)
@Composable
private fun CierreProAppPreview() {
    DemoTheme { CierreProApp() }
}
