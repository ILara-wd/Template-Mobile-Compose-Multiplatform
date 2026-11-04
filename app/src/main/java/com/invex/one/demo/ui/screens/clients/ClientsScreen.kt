package com.invex.one.demo.ui.screens.clients

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpCardShape
import com.invex.one.demo.ui.components.atoms.CpIcon
import com.invex.one.demo.ui.components.molecules.ClientCard
import com.invex.one.demo.ui.components.molecules.ClientStatusBadge
import com.invex.one.demo.ui.components.molecules.FilterChip
import com.invex.one.demo.ui.components.molecules.InitialsAvatar
import com.invex.one.demo.ui.components.molecules.SearchField
import com.invex.one.demo.ui.theme.CpBackground
import com.invex.one.demo.ui.theme.CpError
import com.invex.one.demo.ui.theme.CpErrorContainer
import com.invex.one.demo.ui.theme.CpOnErrorContainer
import com.invex.one.demo.ui.theme.CpOnPrimaryFixed
import com.invex.one.demo.ui.theme.CpOnSecondaryContainer
import com.invex.one.demo.ui.theme.CpOnSurface
import com.invex.one.demo.ui.theme.CpOnSurfaceVariant
import com.invex.one.demo.ui.theme.CpOnTertiaryFixed
import com.invex.one.demo.ui.theme.CpPrimary
import com.invex.one.demo.ui.theme.CpPrimaryContainer
import com.invex.one.demo.ui.theme.CpPrimaryFixed
import com.invex.one.demo.ui.theme.CpSecondaryContainer
import com.invex.one.demo.ui.theme.CpTertiaryFixed
import com.invex.one.demo.ui.theme.CpTextSecondary
import com.invex.one.demo.ui.theme.DemoTheme

// ---------- Modelo de UI ----------

enum class ClientStatus(
    val label: String,
    @DrawableRes val icon: Int,
    val container: Color,
    val content: Color,
) {
    SeguimientoProgramado("Seguimiento programado", R.drawable.ic_followup_scheduled, CpSecondaryContainer, CpOnSecondaryContainer),
    AlDia("Al día", R.drawable.ic_check_circle, CpTertiaryFixed, CpOnTertiaryFixed),
    MantenimientoPendiente("Mantenimiento pendiente", R.drawable.ic_alert_mark, CpErrorContainer, CpOnErrorContainer),
}

enum class AvatarTone(val container: Color, val content: Color) {
    Blue(CpSecondaryContainer, CpPrimary),
    Indigo(CpPrimaryFixed, CpOnPrimaryFixed),
    Red(CpErrorContainer, CpError),
}

data class Client(
    val id: String,
    val name: String,
    val initials: String,
    val tone: AvatarTone,
    val phone: String,
    val location: String,
    @DrawableRes val serviceIcon: Int,
    val lastService: String,
    val lastServiceWhen: String,
    /** Para ordenar por "Recientes". */
    val lastServiceDaysAgo: Int,
    val status: ClientStatus,
    val statusDetail: String? = null,
    val contactName: String? = null,
) {
    val statusText: String get() = statusDetail?.let { "${status.label} ($it)" } ?: status.label
}

enum class ClientFilter(val label: String) { Todos("Todos"), ConSeguimiento("Con seguimiento"), Recientes("Recientes") }

val SampleClients = listOf(
    Client(
        "1", "María López", "ML", AvatarTone.Blue, "+52 81 1234 5678", "Monterrey, N.L.",
        R.drawable.ic_service_maintenance, "Mantenimiento preventivo", "Hace 2 días", 2,
        ClientStatus.SeguimientoProgramado, statusDetail = "Nov 2024",
    ),
    Client(
        "2", "Restaurante El Fogón", "EF", AvatarTone.Indigo, "+52 81 8765 4321", "San Pedro Garza García",
        R.drawable.ic_service_cleaning, "Limpieza profunda 2 minisplits", "Ayer", 1,
        ClientStatus.AlDia, contactName = "Ing. Arturo Ramos",
    ),
    Client(
        "3", "Roberto Garza", "RG", AvatarTone.Red, "+52 81 9988 7766", "Cumbres 3er Sector",
        R.drawable.ic_service_install, "Instalación Mirage Magnum 2 TR", "Hace 5 meses", 150,
        ClientStatus.MantenimientoPendiente,
    ),
    Client(
        "4", "Dra. Sofía Morales", "SM", AvatarTone.Blue, "+52 81 4455 6677", "Mitras Centro",
        R.drawable.ic_service_leak, "Revisión fuga condensado", "Hace 2 semanas", 14,
        ClientStatus.AlDia,
    ),
)

// ---------- Pantalla 2 — Clientes ----------

@Composable
fun ClientsScreen(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    clients: List<Client> = SampleClients,
    registeredCount: Int = 28,
    followUpCount: Int = 4,
    onNewClient: () -> Unit = {},
    onClientClick: (Client) -> Unit = {},
    onCall: (Client) -> Unit = {},
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(ClientFilter.Todos) }

    val visible = remember(clients, query, filter) {
        val digits = query.filter(Char::isDigit)
        clients
            .filter {
                query.isBlank() ||
                    it.name.contains(query.trim(), ignoreCase = true) ||
                    (digits.isNotEmpty() && it.phone.filter(Char::isDigit).contains(digits))
            }
            .let { list ->
                when (filter) {
                    ClientFilter.Todos -> list
                    ClientFilter.ConSeguimiento -> list.filter { it.status == ClientStatus.SeguimientoProgramado }
                    ClientFilter.Recientes -> list.sortedBy { it.lastServiceDaysAgo }
                }
            }
    }

    val layoutDirection = LocalLayoutDirection.current
    LazyColumn(
        modifier.fillMaxSize().background(CpBackground),
        contentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(layoutDirection),
            end = contentPadding.calculateEndPadding(layoutDirection),
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 32.dp,
        ),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.padding(bottom = 1.5.dp), verticalArrangement = Arrangement.spacedBy(2.5.dp)) {
                    Text(
                        "Clientes",
                        style = MaterialTheme.typography.titleLarge.copy(lineHeight = 28.sp),
                        color = CpOnSurface,
                    )
                    Text(
                        "($registeredCount clientes registrados)",
                        style = MaterialTheme.typography.bodySmall,
                        color = CpOnSurfaceVariant,
                    )
                }
                NewClientButton(onNewClient)
            }
        }
        item {
            SearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Buscar por nombre o teléfono",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ClientFilter.entries) { f ->
                    FilterChip(
                        label = f.label,
                        selected = f == filter,
                        onClick = { filter = f },
                        count = if (f == ClientFilter.ConSeguimiento) followUpCount else null,
                    )
                }
            }
        }
        if (visible.isEmpty()) {
            item {
                Text(
                    "Sin clientes que coincidan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CpTextSecondary,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                )
            }
        }
        items(visible, key = { it.id }) { client ->
            ClientCard(
                name = client.name,
                contactName = client.contactName,
                phone = client.phone,
                location = client.location,
                avatar = { InitialsAvatar(client.initials, client.tone.container, client.tone.content) },
                serviceIcon = client.serviceIcon,
                lastService = client.lastService,
                lastServiceWhen = client.lastServiceWhen,
                status = {
                    ClientStatusBadge(
                        text = client.statusText,
                        icon = client.status.icon,
                        containerColor = client.status.container,
                        contentColor = client.status.content,
                    )
                },
                onClick = { onClientClick(client) },
                onCall = { onCall(client) },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = if (client == visible.first()) 8.dp else 12.dp),
            )
        }
    }
}

@Composable
private fun NewClientButton(onClick: () -> Unit) {
    Row(
        Modifier
            .height(40.dp)
            .shadow(1.dp, CpCardShape, ambientColor = Color.Black.copy(alpha = 0.05f), spotColor = Color.Black.copy(alpha = 0.05f))
            .clip(CpCardShape)
            .background(CpPrimaryContainer)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CpIcon(R.drawable.ic_person_add)
        Text(
            "Nuevo Cliente",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.14.sp),
            color = Color.White,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1100)
@Composable
private fun ClientsScreenPreview() {
    DemoTheme { ClientsScreen(contentPadding = PaddingValues(top = 56.dp, bottom = 64.dp)) }
}
