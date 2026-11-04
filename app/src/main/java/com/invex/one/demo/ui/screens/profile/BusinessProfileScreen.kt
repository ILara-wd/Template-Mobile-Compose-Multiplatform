package com.invex.one.demo.ui.screens.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpButton
import com.invex.one.demo.ui.components.atoms.CpIcon
import com.invex.one.demo.ui.components.atoms.StatusBadge
import com.invex.one.demo.ui.components.atoms.cpCard
import com.invex.one.demo.ui.components.atoms.rememberUriPainter
import com.invex.one.demo.ui.components.atoms.uriFileSize
import com.invex.one.demo.ui.components.molecules.CpSectionShape
import com.invex.one.demo.ui.components.molecules.FieldHint
import com.invex.one.demo.ui.components.molecules.LogoThumbnail
import com.invex.one.demo.ui.components.molecules.ProfileTextField
import com.invex.one.demo.ui.components.organisms.CierreProDetailTopBar
import com.invex.one.demo.ui.theme.CpBackground
import com.invex.one.demo.ui.theme.CpError
import com.invex.one.demo.ui.theme.CpOnSurface
import com.invex.one.demo.ui.theme.CpOnSurfaceVariant
import com.invex.one.demo.ui.theme.CpOutline
import com.invex.one.demo.ui.theme.CpPrimary
import com.invex.one.demo.ui.theme.CpPrimaryContainer
import com.invex.one.demo.ui.theme.CpSecondaryContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainerLow
import com.invex.one.demo.ui.theme.CpTextMuted
import com.invex.one.demo.ui.theme.CpWarning
import com.invex.one.demo.ui.theme.CpWarningContainer
import com.invex.one.demo.ui.theme.DemoTheme

// ---------- Modelo ----------

/** Membrete comercial que aparece en los reportes PDF y mensajes al cliente. */
data class BusinessProfile(
    val businessName: String,
    val technicianName: String,
    val whatsappPhone: String,
    val email: String,
    val reportMessage: String,
    /** [SampleLogo], un content:// elegido por el usuario o null (sin logotipo). */
    val logoUri: String?,
) {
    val isValid: Boolean
        get() = businessName.isNotBlank() && technicianName.isNotBlank() && whatsappPhone.isNotBlank()

    companion object {
        const val SampleLogo = "res:sample_logo"

        val Saver: Saver<BusinessProfile, Any> = listSaver(
            save = { listOf(it.businessName, it.technicianName, it.whatsappPhone, it.email, it.reportMessage, it.logoUri) },
            restore = { BusinessProfile(it[0]!!, it[1]!!, it[2]!!, it[3]!!, it[4]!!, it[5]) },
        )
    }
}

val SampleBusinessProfile = BusinessProfile(
    businessName = "ClimaExpress Servicios de Climatización",
    technicianName = "Carlos Méndez",
    whatsappPhone = "+52 81 8300 0000",
    email = "contacto@climaexpress.mx",
    reportMessage = "Hola, adjunto el reporte técnico detallado del servicio realizado hoy en su equipo. Quedamos a sus órdenes.",
    logoUri = BusinessProfile.SampleLogo,
)

private const val MaxLogoBytes = 5L * 1024 * 1024

// ---------- Pantalla 8 — Perfil del Negocio ----------

@Composable
fun BusinessProfileScreen(
    saved: BusinessProfile,
    onSave: (BusinessProfile) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by rememberSaveable(saved, stateSaver = BusinessProfile.Saver) { mutableStateOf(saved) }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    val dirty = draft != saved
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val pickLogo = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val size = uriFileSize(context, uri)
        if (size != null && size > MaxLogoBytes) {
            Toast.makeText(context, "El logotipo supera 5MB", Toast.LENGTH_SHORT).show()
        } else {
            draft = draft.copy(logoUri = uri.toString())
        }
    }
    val logo = rememberLogoPainter(draft.logoUri)

    Scaffold(
        modifier = modifier,
        containerColor = CpBackground,
        topBar = { CierreProDetailTopBar(title = "Detalle De Servicio", onBack = onBack, onProfileClick = {}) },
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
            LetterheadStrip(dirty)

            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    "Configura los datos y logotipo que aparecerán en tus reportes para clientes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CpOnSurfaceVariant,
                )

                ReportPreview(draft, logo)

                LogoCard(
                    logo = logo,
                    onChange = { pickLogo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onRemove = { draft = draft.copy(logoUri = null) },
                )

                IdentityCard(draft, showErrors) { draft = it }

                SaveCard(
                    dirty = dirty,
                    onSave = {
                        if (draft.isValid) {
                            showErrors = false
                            onSave(draft.copy(
                                businessName = draft.businessName.trim(),
                                technicianName = draft.technicianName.trim(),
                                whatsappPhone = draft.whatsappPhone.trim(),
                                email = draft.email.trim(),
                            ))
                        } else {
                            showErrors = true
                        }
                    },
                )
            }
        }
    }
}

/** Franja "Configuración de Membrete" con estado de guardado. */
@Composable
private fun LetterheadStrip(dirty: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .shadow(1.dp, RectangleShape, ambientColor = Color.Black.copy(0.05f), spotColor = Color.Black.copy(0.05f))
            .background(CpSurfaceContainerLow)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CpIcon(R.drawable.ic_letterhead)
            Column {
                Text(
                    "Configuración de Membrete",
                    style = MaterialTheme.typography.titleSmall.copy(lineHeight = 17.5.sp),
                    color = CpOnSurface,
                )
                Text("Visible en tus PDF de servicio", style = MaterialTheme.typography.bodySmall, color = CpOnSurfaceVariant)
            }
        }
        if (dirty) {
            StatusBadge(
                text = "Sin guardar",
                containerColor = CpWarningContainer,
                contentColor = CpWarning,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            )
        } else {
            StatusBadge(
                text = "Guardado",
                containerColor = CpSecondaryContainer.copy(alpha = 0.5f),
                contentColor = CpTextMuted,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                leading = { CpIcon(R.drawable.ic_cloud_done) },
            )
        }
    }
}

/** Vista previa en vivo del encabezado del reporte. */
@Composable
private fun ReportPreview(profile: BusinessProfile, logo: Painter?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                "VISTA PREVIA DE TU REPORTE",
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.6.sp),
                color = CpOnSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                CpIcon(R.drawable.ic_visibility)
                Text("En vivo", style = MaterialTheme.typography.labelSmall, color = CpPrimary)
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .shadow(1.dp, CpSectionShape, ambientColor = Color.Black.copy(0.05f), spotColor = Color.Black.copy(0.05f))
                .background(Color.White, CpSectionShape)
                .clip(CpSectionShape),
        ) {
            Column(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        LogoThumbnail(
                            logo,
                            48.dp,
                            RoundedCornerShape(8.dp),
                            Modifier.shadow(1.dp, RoundedCornerShape(8.dp), spotColor = Color.Black.copy(0.05f)),
                        )
                        Column {
                            Text(
                                profile.businessName.ifBlank { "Nombre del negocio" },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = CpOnSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                "Técnico: ${profile.technicianName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CpOnSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Row(
                                Modifier.padding(top = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CpIcon(R.drawable.ic_phone_micro)
                                Text(
                                    profile.whatsappPhone,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = CpPrimary,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        StatusBadge("REPORTE", CpSecondaryContainer, CpTextMuted)
                        Text(
                            "#HVAC-2024",
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                            color = CpOutline,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth().background(CpSurfaceContainerLow, RoundedCornerShape(8.dp)).padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        CpIcon(R.drawable.ic_verified_muted)
                        Text("Validado por CierrePro", style = MaterialTheme.typography.labelSmall, color = CpOnSurface)
                    }
                    Text("Folio oficial", style = MaterialTheme.typography.labelSmall, color = CpOnSurfaceVariant)
                }
            }
            // Barra de marca superior.
            Box(Modifier.fillMaxWidth().height(6.dp).background(CpPrimary))
        }
    }
}

/** Tarjeta de logotipo: miniatura 80dp + Cambiar / Quitar. */
@Composable
private fun LogoCard(logo: Painter?, onChange: () -> Unit, onRemove: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().cpCard(CpSectionShape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Logotipo del negocio", style = MaterialTheme.typography.titleSmall, color = CpOnSurface)
            Text("JPG, PNG máx. 5MB", style = MaterialTheme.typography.labelSmall, color = CpOnSurfaceVariant)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            LogoThumbnail(logo, 80.dp, CpSectionShape, background = CpSurfaceContainerLow)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val shape = RoundedCornerShape(8.dp)
                CpButton(
                    text = if (logo == null) "Subir logotipo" else "Cambiar logotipo",
                    onClick = onChange,
                    contentColor = CpTextMuted,
                    containerColor = CpSecondaryContainer,
                    height = 40.dp,
                    horizontalPadding = 16.dp,
                    gap = 6.dp,
                    textStyle = MaterialTheme.typography.labelMedium,
                    leadingIcon = R.drawable.ic_photo_camera_muted,
                    modifier = Modifier.fillMaxWidth().shadow(1.dp, shape, spotColor = Color.Black.copy(0.05f)),
                )
                if (logo != null) {
                    CpButton(
                        text = "Quitar",
                        onClick = onRemove,
                        contentColor = CpError,
                        containerColor = CpSurfaceContainer,
                        height = 36.dp,
                        textStyle = MaterialTheme.typography.labelMedium,
                        leadingIcon = R.drawable.ic_delete,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        Text(
            "Aparecerá en el encabezado de cada PDF enviado a los clientes.",
            style = MaterialTheme.typography.bodySmall,
            color = CpOnSurfaceVariant,
        )
    }
}

/** Identidad comercial: nombre, técnico, WhatsApp, correo y mensaje. */
@Composable
private fun IdentityCard(draft: BusinessProfile, showErrors: Boolean, onChange: (BusinessProfile) -> Unit) {
    val requiredError = "Campo obligatorio"
    Column(
        Modifier.fillMaxWidth().cpCard(CpSectionShape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CpIcon(R.drawable.ic_badge)
            Text("Identidad Comercial", style = MaterialTheme.typography.titleSmall, color = CpOnSurface)
        }
        ProfileTextField(
            label = "Nombre del negocio o taller",
            value = draft.businessName,
            onValueChange = { onChange(draft.copy(businessName = it)) },
            required = true,
            error = if (showErrors && draft.businessName.isBlank()) requiredError else null,
        )
        ProfileTextField(
            label = "Nombre del técnico responsable",
            value = draft.technicianName,
            onValueChange = { onChange(draft.copy(technicianName = it)) },
            required = true,
            error = if (showErrors && draft.technicianName.isBlank()) requiredError else null,
        )
        ProfileTextField(
            label = "Teléfono de contacto de WhatsApp",
            value = draft.whatsappPhone,
            onValueChange = { onChange(draft.copy(whatsappPhone = it)) },
            required = true,
            leadingIcon = R.drawable.ic_chat_muted,
            keyboardType = KeyboardType.Phone,
            error = if (showErrors && draft.whatsappPhone.isBlank()) requiredError else null,
            helper = { FieldHint(R.drawable.ic_info_primary, "A este número recibirán confirmaciones tus clientes") },
        )
        ProfileTextField(
            label = "Correo electrónico",
            value = draft.email,
            onValueChange = { onChange(draft.copy(email = it)) },
            optional = true,
            leadingIcon = R.drawable.ic_mail,
            keyboardType = KeyboardType.Email,
        )
        ProfileTextField(
            label = "Mensaje predeterminado de reporte",
            value = draft.reportMessage,
            onValueChange = { onChange(draft.copy(reportMessage = it)) },
            singleLine = false,
            helper = {
                Text(
                    "Texto que acompaña el PDF al compartir por WhatsApp",
                    style = MaterialTheme.typography.labelSmall,
                    color = CpOnSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )
    }
}

/** Tarjeta de guardado con estado de sincronización. */
@Composable
private fun SaveCard(dirty: Boolean, onSave: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(top = 4.dp).cpCard(CpSectionShape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val shape = RoundedCornerShape(8.dp)
        CpButton(
            text = "Guardar cambios",
            onClick = onSave,
            contentColor = Color.White,
            containerColor = CpPrimaryContainer,
            height = 48.dp,
            gap = 8.dp,
            textStyle = MaterialTheme.typography.labelLarge,
            leadingIcon = R.drawable.ic_save_white,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, shape, ambientColor = Color.Black.copy(0.1f), spotColor = Color.Black.copy(0.1f)),
        )
        Row(
            Modifier.padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!dirty) CpIcon(R.drawable.ic_check_circle_filled_primary)
            Text(
                if (dirty) "Tienes cambios sin guardar" else "Todos los cambios están sincronizados",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = if (dirty) CpWarning else CpOnSurfaceVariant,
            )
        }
    }
}

// ---------- Logotipo ----------

/** Resuelve el logotipo: recurso de muestra, imagen elegida o null (sin logotipo). */
@Composable
fun rememberLogoPainter(logoUri: String?): Painter? =
    if (logoUri == BusinessProfile.SampleLogo) painterResource(R.drawable.img_business_logo_sample) else rememberUriPainter(logoUri)

@Preview(showBackground = true, widthDp = 375, heightDp = 1500)
@Composable
private fun BusinessProfileScreenPreview() {
    DemoTheme { BusinessProfileScreen(saved = SampleBusinessProfile, onSave = {}, onBack = {}) }
}
