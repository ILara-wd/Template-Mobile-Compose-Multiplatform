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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.invex.one.demo.ui.screens.home.HomeScreen
import com.invex.one.demo.ui.screens.newservice.NewServiceScreen
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

@Composable
fun CierreProApp() {
    var destination by rememberSaveable { mutableStateOf(CpDestination.Inicio) }
    var showNewService by rememberSaveable { mutableStateOf(false) }
    var detailClientId by rememberSaveable { mutableStateOf<String?>(null) }
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

    if (showNewService) {
        // Flujo de captura a pantalla completa (sin Bottom Navigation).
        BackHandler { showNewService = false }
        NewServiceScreen(
            onBack = { showNewService = false },
            onCancel = { showNewService = false },
            // TODO: navegar a Paso 2 — Checklist y Evidencias.
            onContinue = {},
            client = detailClient?.let {
                ServiceClientSummary(it.name, it.phone, it.address.substringAfterLast(", ").trim())
            } ?: SampleServiceClient,
        )
        return
    }

    if (detailClient != null) {
        BackHandler { detailClientId = null }
        ClientDetailScreen(
            client = detailClient,
            onBack = { detailClientId = null },
            onCall = { dial(detailClient.phone) },
            onWhatsApp = {
                open(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/" + detailClient.phone.filter(Char::isDigit))))
            },
            onDirections = {
                open(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(detailClient.address.replace("\n", " ")))))
            },
            onNewService = { showNewService = true },
        )
        return
    }

    Scaffold(
        containerColor = CpBackground,
        topBar = { CierreProTopBar(sectionTitle = destination.label, onProfileClick = {}) },
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
