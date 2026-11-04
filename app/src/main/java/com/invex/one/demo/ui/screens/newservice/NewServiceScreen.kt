package com.invex.one.demo.ui.screens.newservice

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpButton
import com.invex.one.demo.ui.components.molecules.FormSectionCard
import com.invex.one.demo.ui.components.molecules.NotesField
import com.invex.one.demo.ui.components.molecules.PickerField
import com.invex.one.demo.ui.components.molecules.RequiredFieldsHint
import com.invex.one.demo.ui.components.molecules.SectionMeta
import com.invex.one.demo.ui.components.molecules.SelectorField
import com.invex.one.demo.ui.components.molecules.ServiceStepper
import com.invex.one.demo.ui.components.molecules.ServiceTypeOption
import com.invex.one.demo.ui.components.organisms.CierreProDetailTopBar
import com.invex.one.demo.ui.components.organisms.FlowActionBar
import com.invex.one.demo.ui.components.organisms.FlowHeaderStrip
import com.invex.one.demo.ui.theme.CpBackground
import com.invex.one.demo.ui.theme.CpPrimary
import com.invex.one.demo.ui.theme.CpSurfaceContainerHigh
import com.invex.one.demo.ui.theme.DemoTheme
import java.util.Calendar
import java.util.TimeZone

// ---------- Modelo de UI ----------

enum class ServiceType(val label: String, @DrawableRes val icon: Int) {
    Mantenimiento("Mantenimiento", R.drawable.ic_service_type_maintenance),
    Instalacion("Instalación", R.drawable.ic_service_type_install),
    Reparacion("Reparación", R.drawable.ic_service_type_repair),
    Inspeccion("Inspección", R.drawable.ic_service_type_inspection),
}

data class ServiceClientSummary(val name: String, val phone: String, val location: String) {
    val detail: String get() = "$phone · $location"
}

data class ServiceEquipment(val name: String, val detail: String, val refrigerant: String)

val NewServiceSteps = listOf("Datos", "Trabajo", "Reporte")

val SampleServiceClient = ServiceClientSummary("María López", "+52 81 1234 5678", "San Pedro Garza García")

val SampleServiceEquipment = ServiceEquipment(
    name = "Minisplit Mirage 1.5 TR",
    detail = "Inverter · Recámara Principal (S/N: MRG-9982)",
    refrigerant = "R-410A",
)

/** Datos capturados en el Paso 1 que se entregan al continuar. */
data class NewServiceDraft(
    val client: ServiceClientSummary?,
    val equipment: ServiceEquipment?,
    val type: ServiceType,
    /** Fecha en milisegundos UTC a medianoche (formato de DatePicker). */
    val dateUtcMillis: Long,
    val hour: Int,
    val minute: Int,
    val notes: String,
)

// ---------- Pantalla 4 — Nuevo Servicio (Paso 1: Datos) ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewServiceScreen(
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onContinue: (NewServiceDraft) -> Unit,
    modifier: Modifier = Modifier,
    client: ServiceClientSummary? = SampleServiceClient,
    equipment: ServiceEquipment? = SampleServiceEquipment,
    onProfileClick: () -> Unit = {},
    onSelectClient: () -> Unit = {},
    onRegisterClient: () -> Unit = {},
    onSelectEquipment: () -> Unit = {},
) {
    var type by rememberSaveable { mutableStateOf(ServiceType.Mantenimiento) }
    var dateUtcMillis by rememberSaveable { mutableLongStateOf(todayUtcMillis()) }
    var hour by rememberSaveable { mutableIntStateOf(10) }
    var minute by rememberSaveable { mutableIntStateOf(0) }
    var notes by rememberSaveable { mutableStateOf("Instalado en 2do piso, acceso por azotea") }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = CpBackground,
        topBar = {
            CierreProDetailTopBar(title = "Detalle De Servicio", onBack = onBack, onProfileClick = onProfileClick)
        },
        bottomBar = {
            FlowActionBar(
                primaryText = "Checklist y Evidencias",
                onPrimary = {
                    onContinue(NewServiceDraft(client, equipment, type, dateUtcMillis, hour, minute, notes))
                },
                onSecondary = onCancel,
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(bottom = 24.dp),
        ) {
            FlowHeaderStrip(title = "Nuevo servicio", onClose = onCancel)

            Column(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ServiceStepper(steps = NewServiceSteps, currentIndex = 0)

                // Grupo 1: Cliente
                FormSectionCard(
                    title = "Cliente",
                    trailing = {
                        CpButton(
                            text = "+ Registrar nuevo",
                            onClick = onRegisterClient,
                            contentColor = CpPrimary,
                            height = 20.dp,
                            horizontalPadding = 0.dp,
                            textStyle = MaterialTheme.typography.labelMedium,
                            leadingIcon = R.drawable.ic_person_add_small,
                        )
                    },
                ) {
                    SelectorField(
                        title = client?.name ?: "Seleccionar cliente",
                        subtitle = client?.detail ?: "Busca un cliente registrado",
                        icon = R.drawable.ic_account_circle,
                        onClick = onSelectClient,
                        onClickLabel = "Cambiar cliente",
                    )
                }

                // Grupo 2: Tipo de servicio
                FormSectionCard(
                    title = "Tipo de servicio",
                    trailing = { SectionMeta("Selección obligatoria") },
                ) {
                    Column(
                        Modifier.fillMaxWidth().selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ServiceType.entries.chunked(2).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { option ->
                                    ServiceTypeOption(
                                        label = option.label,
                                        icon = option.icon,
                                        selected = option == type,
                                        onClick = { type = option },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }
                }

                // Grupo 3: Fecha y hora
                FormSectionCard(title = "Programación de visita") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PickerField(
                            label = "Fecha",
                            value = formatServiceDate(dateUtcMillis),
                            icon = R.drawable.ic_calendar_month,
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f),
                        )
                        PickerField(
                            label = "Hora estimada",
                            value = formatServiceTime(hour, minute),
                            icon = R.drawable.ic_schedule,
                            onClick = { showTimePicker = true },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Grupo 4: Equipo
                FormSectionCard(
                    title = "Equipo o unidad atendida",
                    trailing = { equipment?.let { SectionMeta(it.refrigerant) } },
                ) {
                    SelectorField(
                        title = equipment?.name ?: "Seleccionar equipo",
                        subtitle = equipment?.detail ?: "Equipos registrados del cliente",
                        icon = R.drawable.ic_ac_unit,
                        onClick = onSelectEquipment,
                        iconContainerColor = CpSurfaceContainerHigh,
                        iconContainerShape = RoundedCornerShape(8.dp),
                        onClickLabel = "Cambiar equipo",
                    )
                    NotesField(
                        label = "Ubicación o notas de acceso",
                        value = notes,
                        onValueChange = { notes = it },
                    )
                }

                RequiredFieldsHint(
                    prefix = "Campos con (",
                    suffix = ") son obligatorios para generar el reporte técnico.",
                )
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = dateUtcMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { dateUtcMillis = it }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } },
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showTimePicker) {
        val pickerState = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = false)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    hour = pickerState.hour
                    minute = pickerState.minute
                    showTimePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") } },
            text = { TimePicker(state = pickerState) },
        )
    }
}

// ---------- Formato ----------

private val MonthsShort = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")
private val WeekdaysShort = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
private const val DayMillis = 24L * 60 * 60 * 1000

/** Hoy (fecha local) expresado como medianoche UTC, igual que el DatePicker. */
private fun todayUtcMillis(): Long {
    val local = Calendar.getInstance()
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

/** "Hoy, 18 Oct" · "Mañana, 19 Oct" · "Sáb, 26 Oct". */
fun formatServiceDate(utcMillis: Long): String {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    val prefix = when ((utcMillis - todayUtcMillis()) / DayMillis) {
        0L -> "Hoy"
        1L -> "Mañana"
        else -> WeekdaysShort[cal.get(Calendar.DAY_OF_WEEK) - 1]
    }
    return "$prefix, ${cal.get(Calendar.DAY_OF_MONTH)} ${MonthsShort[cal.get(Calendar.MONTH)]}"
}

/** "10:00 AM". */
fun formatServiceTime(hour: Int, minute: Int): String {
    val h12 = if (hour % 12 == 0) 12 else hour % 12
    return "%d:%02d %s".format(h12, minute, if (hour < 12) "AM" else "PM")
}

@Preview(showBackground = true, widthDp = 375, heightDp = 1137)
@Composable
private fun NewServiceScreenPreview() {
    DemoTheme { NewServiceScreen(onBack = {}, onCancel = {}, onContinue = {}) }
}
