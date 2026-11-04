package com.invex.one.demo.ui.screens.report

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpButton
import com.invex.one.demo.ui.components.atoms.CpIcon
import com.invex.one.demo.ui.components.atoms.StatusBadge
import com.invex.one.demo.ui.components.molecules.ChecklistResultItem
import com.invex.one.demo.ui.components.molecules.CpSectionShape
import com.invex.one.demo.ui.components.molecules.DiagnosisBlock
import com.invex.one.demo.ui.components.molecules.DocBlock
import com.invex.one.demo.ui.components.molecules.DocMeta
import com.invex.one.demo.ui.components.molecules.DocSectionHeader
import com.invex.one.demo.ui.components.molecules.EvidencePhase
import com.invex.one.demo.ui.components.molecules.EvidencePhotoTile
import com.invex.one.demo.ui.components.molecules.SignatureBlock
import com.invex.one.demo.ui.components.molecules.SuccessBanner
import com.invex.one.demo.ui.components.organisms.CierreProDetailTopBar
import com.invex.one.demo.ui.theme.CpBackground
import com.invex.one.demo.ui.theme.CpOnSurface
import com.invex.one.demo.ui.theme.CpOnSurfaceVariant
import com.invex.one.demo.ui.theme.CpPrimary
import com.invex.one.demo.ui.theme.CpPrimaryContainer
import com.invex.one.demo.ui.theme.CpSecondaryContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainerLow
import com.invex.one.demo.ui.theme.CpTextMuted
import com.invex.one.demo.ui.theme.CpTextSecondary
import com.invex.one.demo.ui.theme.DemoTheme

// ---------- Modelo de UI ----------

data class ChecklistResult(val title: String, val detail: String, val reading: String? = null)

data class EvidencePhoto(@DrawableRes val image: Int, val phase: EvidencePhase, val caption: String)

data class ServiceReport(
    val folio: String,
    val date: String,
    val technician: String,
    val workshopName: String,
    val workshopTagline: String,
    val certification: String,
    val clientName: String,
    val clientPhone: String,
    val location: String,
    val equipmentName: String,
    val equipmentDetail: String,
    val checklist: List<ChecklistResult>,
    val photos: List<EvidencePhoto>,
    /** Texto del diagnóstico; [diagnosisHighlight] se resalta en primario. */
    val diagnosis: String,
    val diagnosisHighlight: String,
    @DrawableRes val signature: Int,
    val approval: String,
    val docId: String,
)

val SampleServiceReport = ServiceReport(
    folio = "CP-2024-0418",
    date = "18 Octubre 2024",
    technician = "Carlos Méndez (Certificado HVAC-R)",
    workshopName = "ClimaExpress Climatización & Confort",
    workshopTagline = "Soporte Residencial e Industrial Autorizado",
    certification = "CierrePro HVAC Cert",
    clientName = "María López",
    clientPhone = "+52 81 1234 5678",
    location = "Av. Paseo de los Leones #2450, Cumbres 2° Sector",
    equipmentName = "Minisplit Inverter Mirage 1.5 TR",
    equipmentDetail = "Serie: MRG-9941-X • R410A Ecológico",
    checklist = listOf(
        ChecklistResult("Limpieza profunda de turbina y filtros", "Desinfección con agente biocida neutro"),
        ChecklistResult("Desazolve químico de línea de drenaje", "Flujo libre verificado, trampa purgada"),
        ChecklistResult("Medición de presión de refrigerante", "Manómetro digital Testo calibrado • R410A", "115 PSI"),
        ChecklistResult(
            "Prueba de temperatura de inyección",
            "Inyección 13°C / Retorno 24°C (Diferencial óptimo)",
            "ΔT 11°C",
        ),
    ),
    photos = listOf(
        EvidencePhoto(R.drawable.img_report_before_filters, EvidencePhase.Antes, "Filtros con exceso de polvo"),
        EvidencePhoto(R.drawable.img_report_before_drain, EvidencePhase.Antes, "Drenaje con sedimentos"),
        EvidencePhoto(R.drawable.img_report_after_unit, EvidencePhase.Despues, "Unidad sanitizada 100%"),
        EvidencePhoto(R.drawable.img_report_after_drain, EvidencePhase.Despues, "Flujo continuo sin goteo"),
    ),
    diagnosis = "Equipo operando en rangos nominales de fábrica. Se sugiere programar el próximo servicio preventivo para Abril 2025.",
    diagnosisHighlight = "Abril 2025",
    signature = R.drawable.img_signature_sample,
    approval = "Aprobado en sitio • 10:14 AM",
    docId = "#CP-88219-MX",
)

// ---------- Pantalla 6 — Reporte del Servicio y Entrega (Paso 3) ----------

@Composable
fun ServiceReportScreen(
    report: ServiceReport,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onClose: () -> Unit = onBack,
    onSendWhatsApp: () -> Unit = {},
    onSharePdf: () -> Unit = {},
    onComplete: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = CpBackground,
        topBar = { CierreProDetailTopBar(title = "Reporte Y Firma", onBack = onBack, onProfileClick = onProfileClick) },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(bottom = 32.dp),
        ) {
            StepStatusBar(step = 3, total = 3, label = "Entrega & Cierre", onClose = onClose)

            SuccessBanner(
                title = "Reporte generado exitosamente",
                subtitle = "Listo para compartir con el cliente",
                badge = "Oficial",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            )

            ReportDocument(report, Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp))

            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val shape = RoundedCornerShape(12.dp)
                CpButton(
                    text = "Enviar reporte por WhatsApp",
                    onClick = onSendWhatsApp,
                    contentColor = Color.White,
                    containerColor = CpPrimary,
                    height = 48.dp,
                    gap = 10.dp,
                    textStyle = MaterialTheme.typography.labelLarge,
                    leadingIcon = R.drawable.ic_send_white,
                    shape = shape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, shape, ambientColor = Color.Black.copy(0.1f), spotColor = Color.Black.copy(0.1f)),
                )
                CpButton(
                    text = "Descargar / Compartir PDF",
                    onClick = onSharePdf,
                    contentColor = CpOnSurface,
                    containerColor = Color.White,
                    height = 48.dp,
                    gap = 10.dp,
                    textStyle = MaterialTheme.typography.labelLarge,
                    leadingIcon = R.drawable.ic_picture_as_pdf_primary,
                    shape = shape,
                    modifier = Modifier.fillMaxWidth(),
                )
                CpButton(
                    text = "Guardar y marcar como completado",
                    onClick = onComplete,
                    contentColor = CpTextSecondary,
                    height = 44.dp,
                    gap = 8.dp,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, letterSpacing = 0.14.sp),
                    leadingIcon = R.drawable.ic_task_alt,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Barra de progreso del flujo: "PASO 3 DE 3 · Entrega & Cierre" + Cerrar. */
@Composable
private fun StepStatusBar(step: Int, total: Int, label: String, onClose: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            StatusBadge(
                text = "PASO $step DE $total",
                containerColor = CpSecondaryContainer,
                contentColor = CpTextMuted,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                textStyle = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.55.sp),
            )
            Text(label, style = MaterialTheme.typography.labelMedium, color = CpTextSecondary)
        }
        CpButton(
            text = "Cerrar",
            onClick = onClose,
            contentColor = CpOnSurfaceVariant,
            height = 24.dp,
            horizontalPadding = 8.dp,
            textStyle = MaterialTheme.typography.labelMedium,
            leadingIcon = R.drawable.ic_close_small,
        )
    }
}

/** O5. Documento técnico membretado listo para el cliente. */
@Composable
private fun ReportDocument(report: ServiceReport, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .shadow(4.dp, CpSectionShape, ambientColor = Color.Black.copy(0.1f), spotColor = Color.Black.copy(0.1f))
            .background(Color.White, CpSectionShape)
            .clip(CpSectionShape),
    ) {
        // Barra de acento (marca de agua).
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Brush.horizontalGradient(listOf(CpPrimary, CpPrimaryContainer, CpSecondaryContainer))),
        )
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // Membrete del taller + metadatos.
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            report.certification.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.275.sp),
                            color = CpPrimary,
                        )
                        Text(
                            report.workshopName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 20.sp),
                            color = CpOnSurface,
                        )
                        Text(report.workshopTagline, style = MaterialTheme.typography.bodySmall, color = CpTextSecondary)
                    }
                    Box(
                        Modifier.size(40.dp).background(CpSurfaceContainerLow, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CpIcon(R.drawable.ic_hvac_large)
                    }
                }
                Column(
                    Modifier.fillMaxWidth().background(CpSurfaceContainerLow, RoundedCornerShape(8.dp)).padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        // Proporción 1 : 0.75 ≈ fecha alineada a x=200 en el diseño (375dp).
                        DocMeta(R.drawable.ic_tag, "Folio: ${report.folio}", Modifier.weight(1f), fontWeight = FontWeight.Medium)
                        DocMeta(R.drawable.ic_calendar_event, report.date, Modifier.weight(0.75f), color = CpTextSecondary)
                    }
                    DocMeta(R.drawable.ic_engineering, "Técnico: ${report.technician}")
                }
            }

            // Sección 1: cliente y unidad.
            DocBlock(spacing = 10.dp) {
                DocSectionHeader("Datos del servicio & unidad") { CpIcon(R.drawable.ic_qr_code) }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column {
                        Text("Cliente", style = MaterialTheme.typography.bodySmall, color = CpTextSecondary)
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(report.clientName, style = MaterialTheme.typography.titleSmall, color = CpOnSurface)
                            Text(report.clientPhone, style = MaterialTheme.typography.labelMedium, color = CpPrimary)
                        }
                    }
                    Column {
                        Text("Ubicación", style = MaterialTheme.typography.bodySmall, color = CpTextSecondary)
                        Text(report.location, style = MaterialTheme.typography.bodyMedium, color = CpOnSurface)
                    }
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 8.dp),
                    ) {
                        Text("Equipo Intervenido", style = MaterialTheme.typography.labelSmall, color = CpTextSecondary)
                        Text(report.equipmentName, style = MaterialTheme.typography.titleSmall, color = CpOnSurface)
                        Text(report.equipmentDetail, style = MaterialTheme.typography.labelSmall, color = CpOnSurfaceVariant)
                    }
                }
            }

            // Sección 2: protocolo técnico.
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DocSectionHeader("Protocolo técnico ejecutado") {
                    val n = report.checklist.size
                    StatusBadge(text = "$n / $n Completado", containerColor = CpSurfaceContainer, contentColor = CpPrimary)
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    report.checklist.forEach { ChecklistResultItem(it.title, it.detail, reading = it.reading) }
                }
            }

            // Sección 3: evidencias 2×2.
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DocSectionHeader("Evidencias fotográficas certificadas") {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        CpIcon(R.drawable.ic_photo_camera)
                        Text("${report.photos.size} tomas", style = MaterialTheme.typography.bodySmall, color = CpTextSecondary)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    report.photos.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { EvidencePhotoTile(it.image, it.phase, it.caption, Modifier.weight(1f)) }
                            if (row.size == 1) Box(Modifier.weight(1f))
                        }
                    }
                }
            }

            // Sección 4: diagnóstico.
            DiagnosisBlock(
                title = "Diagnóstico & Recomendaciones",
                body = buildAnnotatedString {
                    val i = report.diagnosis.indexOf(report.diagnosisHighlight)
                    if (i < 0) {
                        append(report.diagnosis)
                    } else {
                        append(report.diagnosis.substring(0, i))
                        withStyle(SpanStyle(color = CpPrimary, fontWeight = FontWeight.SemiBold)) {
                            append(report.diagnosisHighlight)
                        }
                        append(report.diagnosis.substring(i + report.diagnosisHighlight.length))
                    }
                },
            )

            // Sección 5: firma.
            SignatureBlock(
                label = "Conformidad del cliente",
                mode = "Firma en Pantalla",
                signature = report.signature,
                signerName = report.clientName,
                approval = report.approval,
            )

            // Sello de pie de documento.
            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    CpIcon(R.drawable.ic_lock)
                    Text("Cifrado SHA-256", style = MaterialTheme.typography.labelSmall, color = CpTextSecondary)
                }
                Text("Doc ID: ${report.docId}", style = MaterialTheme.typography.labelSmall, color = CpTextSecondary)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 375, heightDp = 2000)
@Composable
private fun ServiceReportScreenPreview() {
    DemoTheme { ServiceReportScreen(report = SampleServiceReport, onBack = {}) }
}
