package com.invex.one.demo.ui.screens.worklog

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpButton
import com.invex.one.demo.ui.components.atoms.CpIcon
import com.invex.one.demo.ui.components.atoms.StatusBadge
import com.invex.one.demo.ui.components.atoms.StatusDot
import com.invex.one.demo.ui.components.atoms.cpCard
import com.invex.one.demo.ui.components.atoms.rememberUriPainter
import com.invex.one.demo.ui.components.molecules.AddPhotoButton
import com.invex.one.demo.ui.components.molecules.AddPhotoTile
import com.invex.one.demo.ui.components.molecules.ChecklistTaskItem
import com.invex.one.demo.ui.components.molecules.CpSectionShape
import com.invex.one.demo.ui.components.molecules.EvidenceSectionHeader
import com.invex.one.demo.ui.components.molecules.EvidenceThumbnail
import com.invex.one.demo.ui.components.molecules.ServiceStepperCompact
import com.invex.one.demo.ui.components.organisms.CierreProDetailTopBar
import com.invex.one.demo.ui.screens.newservice.NewServiceSteps
import com.invex.one.demo.ui.theme.CpBackground
import com.invex.one.demo.ui.theme.CpOnSurface
import com.invex.one.demo.ui.theme.CpOnSurfaceVariant
import com.invex.one.demo.ui.theme.CpOutline
import com.invex.one.demo.ui.theme.CpPrimary
import com.invex.one.demo.ui.theme.CpPrimaryContainer
import com.invex.one.demo.ui.theme.CpSecondaryContainer
import com.invex.one.demo.ui.theme.CpSuccessSoft
import com.invex.one.demo.ui.theme.CpSuccessStrong
import com.invex.one.demo.ui.theme.CpSurfaceContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainerLow
import com.invex.one.demo.ui.theme.CpTextMuted
import com.invex.one.demo.ui.theme.DemoTheme
import java.io.File
import java.util.Calendar
import java.util.TimeZone

// ---------- Modelo ----------

enum class PhotoPhase { Antes, Despues }

/**
 * Foto de evidencia. [source] es "res:<clave>" para las muestras del diseño o
 * un content:// de la cámara / galería.
 */
data class EvidenceShot(val source: String, val caption: String)

data class ChecklistTask(val title: String, val done: Boolean)

/** Resultado del Paso 2, entregado al continuar al reporte. */
data class WorkLog(
    val tasks: List<ChecklistTask>,
    val before: List<EvidenceShot>,
    val after: List<EvidenceShot>,
    val observations: String,
    val recommendations: String,
    /** Próximo mantenimiento (medianoche UTC, formato de DatePicker). */
    val nextMaintenanceUtcMillis: Long,
)

private val SamplePhotoRes = mapOf(
    "res:before_filter" to R.drawable.img_work_before_filter,
    "res:before_coil" to R.drawable.img_work_before_coil,
    "res:after_filters" to R.drawable.img_work_after_filters,
)

val SampleTasks = listOf(
    ChecklistTask("Limpieza de filtros", true),
    ChecklistTask("Revisión del drenaje", true),
    ChecklistTask("Revisión de conexiones eléctricas", true),
    ChecklistTask("Limpieza profunda de serpentín / unidad", false),
    ChecklistTask("Prueba de funcionamiento y amperaje", false),
)
val SampleBefore = listOf(
    EvidenceShot("res:before_filter", "Filtro saturado"),
    EvidenceShot("res:before_coil", "Serpentín con polvo"),
)
val SampleAfter = listOf(EvidenceShot("res:after_filters", "Filtros lavados y secos"))

// Persistencia en estado guardado (listas planas de Strings / Booleans).
private fun List<ChecklistTask>.flat() = flatMap { listOf(it.title, it.done.toString()) }
private fun List<String>.toTasks() = chunked(2).map { ChecklistTask(it[0], it[1].toBoolean()) }
private fun List<EvidenceShot>.flatShots() = flatMap { listOf(it.source, it.caption) }
private fun List<String>.toShots() = chunked(2).map { EvidenceShot(it[0], it[1]) }

// ---------- Pantalla 5 — Checklist y Evidencias (Paso 2) ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkLogScreen(
    clientName: String,
    equipmentName: String,
    serviceType: String,
    onBack: () -> Unit,
    onContinue: (WorkLog) -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onServiceInfo: () -> Unit = {},
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var taskData by rememberSaveable { mutableStateOf(SampleTasks.flat()) }
    var beforeData by rememberSaveable { mutableStateOf(SampleBefore.flatShots()) }
    var afterData by rememberSaveable { mutableStateOf(SampleAfter.flatShots()) }
    var observations by rememberSaveable {
        mutableStateOf("Presión de gas verificada a 115 PSI en baja. Se desobstruyó manguera de drenaje.")
    }
    var recommendations by rememberSaveable {
        mutableStateOf("Mantener temperatura recomendada en 24°C para ahorro energético.")
    }
    var nextMaintenance by rememberSaveable { mutableLongStateOf(monthsFromTodayUtc(6)) }

    var showAddTask by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var photoTarget by rememberSaveable { mutableStateOf<String?>(null) }   // fase con diálogo de origen abierto
    var pendingPhase by rememberSaveable { mutableStateOf<String?>(null) }  // fase esperando resultado
    var pendingCameraUri by rememberSaveable { mutableStateOf<String?>(null) }

    val tasks = taskData.toTasks()
    val before = beforeData.toShots()
    val after = afterData.toShots()

    fun addShot(phase: String?, uri: Uri) {
        when (phase) {
            PhotoPhase.Antes.name -> beforeData = (before + EvidenceShot(uri.toString(), "Estado inicial")).flatShots()
            PhotoPhase.Despues.name -> afterData = (after + EvidenceShot(uri.toString(), "Trabajo terminado")).flatShots()
        }
    }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) addShot(pendingPhase, uri)
        pendingPhase = null
    }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = pendingCameraUri
        if (ok && uri != null) addShot(pendingPhase, Uri.parse(uri))
        pendingPhase = null
        pendingCameraUri = null
    }

    Scaffold(
        modifier = modifier,
        containerColor = CpBackground,
        topBar = { CierreProDetailTopBar(title = "Detalle De Servicio", onBack = onBack, onProfileClick = onProfileClick) },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(bottom = 32.dp),
        ) {
            FlowSubHeader(onBack)

            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ServiceSummary(clientName, equipmentName, serviceType, onServiceInfo)
                    ServiceStepperCompact(NewServiceSteps, currentIndex = 1)
                }

                // Sección 1: checklist.
                SectionCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Checklist de mantenimiento",
                                style = MaterialTheme.typography.titleMedium,
                                color = CpOnSurface,
                                modifier = Modifier.semantics { heading() },
                            )
                            Text(equipmentName, style = MaterialTheme.typography.bodySmall, color = CpOnSurfaceVariant)
                        }
                        StatusBadge(
                            "${tasks.count { it.done }} / ${tasks.size} hechos",
                            CpSuccessSoft,
                            CpSuccessStrong,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        tasks.forEachIndexed { i, t ->
                            ChecklistTaskItem(t.title, t.done, onToggle = { checked ->
                                taskData = tasks.mapIndexed { j, x -> if (j == i) x.copy(done = checked) else x }.flat()
                            })
                        }
                    }
                    CpButton(
                        text = "Agregar tarea técnica",
                        onClick = { showAddTask = true },
                        contentColor = CpPrimary,
                        containerColor = CpSurfaceContainerLow,
                        height = 40.dp,
                        gap = 6.dp,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.14.sp),
                        leadingIcon = R.drawable.ic_add_primary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Sección 2: antes.
                SectionCard {
                    EvidenceSectionHeader(
                        "ANTES", CpOnSurface, CpBackground,
                        "Estado inicial", "Registra el estado inicial del equipo", before.size,
                    )
                    PhotoGrid(
                        shots = before,
                        captionIcon = R.drawable.ic_photo_camera,
                        onRemove = { i -> beforeData = before.filterIndexed { j, _ -> j != i }.flatShots() },
                        onAdd = { photoTarget = PhotoPhase.Antes.name },
                    )
                }

                // Sección 3: después.
                SectionCard {
                    EvidenceSectionHeader(
                        "DESPUÉS", CpPrimaryContainer, Color.White,
                        "Evidencia de finalización", "Evidencia del trabajo finalizado", after.size,
                    )
                    PhotoGrid(
                        shots = after,
                        captionIcon = R.drawable.ic_check_circle_white,
                        onRemove = { i -> afterData = after.filterIndexed { j, _ -> j != i }.flatShots() },
                        onAdd = { photoTarget = PhotoPhase.Despues.name },
                    )
                }

                // Sección 4: diagnóstico y notas.
                SectionCard(spacing = 16.dp) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        CpIcon(R.drawable.ic_assignment)
                        Text(
                            "Diagnóstico y notas",
                            style = MaterialTheme.typography.titleMedium,
                            color = CpOnSurface,
                            modifier = Modifier.semantics { heading() },
                        )
                    }
                    NoteField(
                        label = "Observaciones del técnico",
                        value = observations,
                        onValueChange = { observations = it },
                        trailing = { Text("Datos de operación", style = MaterialTheme.typography.labelSmall, color = CpOutline) },
                        minHeight = 101.dp,
                    )
                    NoteField(
                        label = "Recomendaciones para el cliente",
                        value = recommendations,
                        onValueChange = { recommendations = it },
                        trailing = { CpIcon(R.drawable.ic_tips) },
                    )
                    Column(Modifier.padding(top = 2.5.dp), verticalArrangement = Arrangement.spacedBy(7.5.dp)) {
                        Text(
                            "Próximo mantenimiento recomendado",
                            style = MaterialTheme.typography.labelMedium,
                            color = CpOnSurface,
                        )
                        Row(
                            Modifier.fillMaxWidth().background(CpSurfaceContainerLow, RoundedCornerShape(8.dp)).padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(32.dp).background(CpPrimaryContainer.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CpIcon(R.drawable.ic_calendar_month_accent)
                                }
                                Column {
                                    Text(formatLongDate(nextMaintenance), style = MaterialTheme.typography.titleSmall, color = CpOnSurface)
                                    Text(
                                        "Intervalo sugerido: ${monthsBetween(todayUtc(), nextMaintenance)} meses",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CpOnSurfaceVariant,
                                    )
                                }
                            }
                            CpButton(
                                text = "Modificar",
                                onClick = { showDatePicker = true },
                                contentColor = CpPrimary,
                                containerColor = CpSurfaceContainer,
                                height = 22.dp,
                                horizontalPadding = 10.dp,
                                shape = RoundedCornerShape(6.dp),
                            )
                        }
                    }
                }

                // Acción principal.
                Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val shape = RoundedCornerShape(8.dp)
                    CpButton(
                        text = "Revisar reporte de servicio",
                        onClick = {
                            onContinue(WorkLog(tasks, before, after, observations, recommendations, nextMaintenance))
                        },
                        contentColor = Color.White,
                        containerColor = CpPrimaryContainer,
                        height = 48.dp,
                        gap = 8.dp,
                        textStyle = MaterialTheme.typography.labelLarge,
                        trailingIcon = R.drawable.ic_arrow_forward_white,
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, shape, ambientColor = Color.Black.copy(0.1f), spotColor = Color.Black.copy(0.1f)),
                    )
                    Text(
                        "Los cambios se guardan localmente de forma automática",
                        style = MaterialTheme.typography.bodySmall,
                        color = CpOutline,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    // ---------- Diálogos ----------

    if (showAddTask) {
        var newTask by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddTask = false },
            title = { Text("Agregar tarea técnica") },
            text = {
                OutlinedTextField(
                    value = newTask,
                    onValueChange = { newTask = it },
                    singleLine = true,
                    placeholder = { Text("Ej. Revisión de capacitor") },
                )
            },
            confirmButton = {
                TextButton(
                    enabled = newTask.isNotBlank(),
                    onClick = {
                        taskData = (tasks + ChecklistTask(newTask.trim(), false)).flat()
                        showAddTask = false
                    },
                ) { Text("Agregar") }
            },
            dismissButton = { TextButton(onClick = { showAddTask = false }) { Text("Cancelar") } },
        )
    }

    photoTarget?.let { phase ->
        AlertDialog(
            onDismissRequest = { photoTarget = null },
            title = { Text(if (phase == PhotoPhase.Antes.name) "Foto de estado inicial" else "Foto de finalización") },
            text = { Text("Toma una foto con la cámara o elige una de la galería.") },
            confirmButton = {
                TextButton(onClick = {
                    photoTarget = null
                    pendingPhase = phase
                    val uri = newCameraUri(context)
                    pendingCameraUri = uri.toString()
                    takePhoto.launch(uri)
                }) { Text("Tomar foto") }
            },
            dismissButton = {
                TextButton(onClick = {
                    photoTarget = null
                    pendingPhase = phase
                    pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) { Text("Galería") }
            },
        )
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = nextMaintenance)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { nextMaintenance = it }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } },
        ) { DatePicker(state = state) }
    }
}

// ---------- Secciones ----------

/** Sub-encabezado "FLUJO DE SERVICIO · Registrar trabajo" con estado En curso. */
@Composable
private fun FlowSubHeader(onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .cpCard(RectangleShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(0.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .offset(x = (-8).dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClickLabel = "Volver al paso 1", onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                CpIcon(R.drawable.ic_arrow_back_small)
            }
            Column {
                Text(
                    "FLUJO DE SERVICIO",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.55.sp),
                    color = CpPrimary,
                )
                Text(
                    "Registrar trabajo",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, lineHeight = 24.sp),
                    color = CpOnSurface,
                    modifier = Modifier.semantics { heading() },
                )
            }
        }
        StatusBadge(
            "En curso", CpSecondaryContainer, CpTextMuted,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            leading = { StatusDot(CpPrimaryContainer) },
        )
    }
}

/** Resumen compacto del cliente y servicio en curso. */
@Composable
private fun ServiceSummary(clientName: String, equipmentName: String, serviceType: String, onInfo: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(CpSurfaceContainerLow, CpSectionShape).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(40.dp)
                .shadow(1.dp, RoundedCornerShape(8.dp), spotColor = Color.Black.copy(0.05f))
                .background(CpPrimaryContainer, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            CpIcon(R.drawable.ic_ac_unit, tint = Color.White)
        }
        Column(Modifier.weight(1f)) {
            Text(clientName, style = MaterialTheme.typography.titleSmall, color = CpOnSurface, maxLines = 1)
            Text(
                buildAnnotatedString {
                    append("$equipmentName • ")
                    withStyle(SpanStyle(color = CpPrimary, fontWeight = FontWeight.SemiBold, letterSpacing = 0.24.sp)) {
                        append(serviceType)
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = CpOnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            Modifier.size(32.dp).clip(CircleShape).clickable(role = Role.Button, onClickLabel = "Detalles del servicio", onClick = onInfo),
            contentAlignment = Alignment.Center,
        ) {
            CpIcon(R.drawable.ic_info_outline)
        }
    }
}

@Composable
private fun SectionCard(spacing: Dp = 12.dp, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().cpCard(CpSectionShape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content,
    )
}

/**
 * Grilla de 2 columnas: si el número de fotos es impar, la celda libre es el
 * botón de agregar; si es par, se agrega un botón ancho debajo.
 */
@Composable
private fun PhotoGrid(
    shots: List<EvidenceShot>,
    @DrawableRes captionIcon: Int,
    onRemove: (Int) -> Unit,
    onAdd: () -> Unit,
) {
    val rows = shots.withIndex().chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (i, shot) ->
                    EvidenceThumbnail(
                        image = rememberShotPainter(shot.source),
                        caption = shot.caption,
                        captionIcon = captionIcon,
                        onRemove = { onRemove(i) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) AddPhotoTile(onAdd, Modifier.weight(1f))
            }
        }
        if (shots.size % 2 == 0) AddPhotoButton(onAdd)
    }
}

@Composable
private fun rememberShotPainter(source: String): Painter? =
    SamplePhotoRes[source]?.let { painterResource(it) } ?: rememberUriPainter(source)

/** Campo de nota multilínea con etiqueta y accesorio a la derecha. */
@Composable
private fun NoteField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    trailing: @Composable () -> Unit,
    minHeight: Dp = 81.dp,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = CpOnSurface)
            trailing()
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = CpOnSurface),
            cursorBrush = SolidColor(CpPrimaryContainer),
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = minHeight)
                .background(CpSurfaceContainerLow, RoundedCornerShape(8.dp))
                .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 17.dp),
        )
    }
}

// ---------- Utilidades ----------

private val MonthsLong = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
)

private fun utc() = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

private fun todayUtc(): Long {
    val local = Calendar.getInstance()
    return utc().apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

private fun monthsFromTodayUtc(months: Int): Long =
    utc().apply { timeInMillis = todayUtc(); add(Calendar.MONTH, months) }.timeInMillis

private fun monthsBetween(fromUtc: Long, toUtc: Long): Int {
    val a = utc().apply { timeInMillis = fromUtc }
    val b = utc().apply { timeInMillis = toUtc }
    return ((b.get(Calendar.YEAR) - a.get(Calendar.YEAR)) * 12 + b.get(Calendar.MONTH) - a.get(Calendar.MONTH)).coerceAtLeast(0)
}

/** "15 de Abril 2025". */
private fun formatLongDate(utcMillis: Long): String {
    val c = utc().apply { timeInMillis = utcMillis }
    return "${c.get(Calendar.DAY_OF_MONTH)} de ${MonthsLong[c.get(Calendar.MONTH)]} ${c.get(Calendar.YEAR)}"
}

/** Archivo temporal en caché compartido vía FileProvider para la cámara. */
private fun newCameraUri(context: Context): Uri {
    val dir = File(context.cacheDir, "evidence").apply { mkdirs() }
    val file = File.createTempFile("evidencia_", ".jpg", dir)
    return FileProvider.getUriForFile(context, "${context.packageName}.evidence", file)
}

@Preview(showBackground = true, widthDp = 375, heightDp = 1900)
@Composable
private fun WorkLogScreenPreview() {
    DemoTheme {
        WorkLogScreen(
            clientName = "María López",
            equipmentName = "Minisplit Mirage 1.5 TR",
            serviceType = "Mantenimiento Preventivo",
            onBack = {},
            onContinue = {},
        )
    }
}
