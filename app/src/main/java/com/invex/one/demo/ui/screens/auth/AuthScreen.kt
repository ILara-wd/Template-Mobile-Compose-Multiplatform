package com.invex.one.demo.ui.screens.auth

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.one.demo.R
import com.invex.one.demo.ui.components.atoms.CpButton
import com.invex.one.demo.ui.components.atoms.CpIcon
import com.invex.one.demo.ui.components.atoms.StatusBadge
import com.invex.one.demo.ui.components.atoms.cpCard
import com.invex.one.demo.ui.components.molecules.CpSectionShape
import com.invex.one.demo.ui.components.molecules.ProfileTextField
import com.invex.one.demo.ui.components.organisms.CierreProAuthTopBar
import com.invex.one.demo.ui.components.organisms.CpToastHost
import com.invex.one.demo.ui.components.organisms.rememberCpToastState
import com.invex.one.demo.ui.theme.CpBackground
import com.invex.one.demo.ui.theme.CpOnSecondaryFixed
import com.invex.one.demo.ui.theme.CpOnSurface
import com.invex.one.demo.ui.theme.CpOutline
import com.invex.one.demo.ui.theme.CpPrimary
import com.invex.one.demo.ui.theme.CpPrimaryContainer
import com.invex.one.demo.ui.theme.CpSecondaryContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainer
import com.invex.one.demo.ui.theme.CpSurfaceContainerHigh
import com.invex.one.demo.ui.theme.CpSurfaceContainerLow
import com.invex.one.demo.ui.theme.CpTextMuted
import com.invex.one.demo.ui.theme.CpTextSecondary
import com.invex.one.demo.ui.theme.DemoTheme
import kotlinx.coroutines.launch

enum class AuthMode(val label: String, @DrawableRes val icon: Int) {
    Login("Iniciar Sesión", R.drawable.ic_login),
    Register("Crear Cuenta", R.drawable.ic_person_add_outline),
}

private const val Soon = "Disponible próximamente"

// ---------- Acceso (Auth) ----------

/**
 * Acceso técnico: iniciar sesión o crear la cuenta del taller. Sin backend
 * todavía; valida campos y entrega el usuario a [onSignedIn].
 *
 * @param rememberedAccount correo/teléfono recordado del último acceso, si existe.
 */
@Composable
fun AuthScreen(
    onSignedIn: (account: String, remember: Boolean) -> Unit,
    onRegistered: (workshopName: String, account: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    rememberedAccount: String? = null,
) {
    var mode by rememberSaveable { mutableStateOf(AuthMode.Login) }
    var account by rememberSaveable { mutableStateOf(rememberedAccount ?: "carlos@climaexpress.mx") }
    var password by rememberSaveable { mutableStateOf("") }
    var workshop by rememberSaveable { mutableStateOf("") }
    var rememberWorkshop by rememberSaveable { mutableStateOf(true) }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    val toast = rememberCpToastState()
    val focusManager = LocalFocusManager.current
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()

    fun submit() {
        focusManager.clearFocus()
        val missing = account.isBlank() || password.isBlank() || (mode == AuthMode.Register && workshop.isBlank())
        when {
            missing -> {
                showErrors = true
                toast.show("Completa los campos obligatorios")
            }
            password.length < 6 -> {
                showErrors = true
                toast.show("La contraseña debe tener al menos 6 caracteres")
            }
            mode == AuthMode.Login -> onSignedIn(account.trim(), rememberWorkshop)
            else -> onRegistered(workshop.trim(), account.trim())
        }
    }

    Box(modifier.fillMaxSize()) {
        Scaffold(
            containerColor = CpBackground,
            topBar = { CierreProAuthTopBar(onBack = onBack, onProfileClick = { toast.show("Inicia sesión para ver tu negocio") }) },
        ) { innerPadding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding)
                    .imePadding()
                    .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
                    .verticalScroll(scroll)
                    .padding(innerPadding)
                    .padding(bottom = 32.dp),
            ) {
                Hero()

                ModeSwitcher(mode, onSelect = { mode = it; showErrors = false }, Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp))

                Column(
                    Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        .fillMaxWidth()
                        .cpCard(CpSectionShape)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (mode == AuthMode.Register) {
                            ProfileTextField(
                                label = "Nombre del negocio o taller",
                                value = workshop,
                                onValueChange = { workshop = it },
                                required = true,
                                labelWeight = FontWeight.SemiBold,
                                leadingIcon = R.drawable.ic_letterhead,
                                error = if (showErrors && workshop.isBlank()) "Campo obligatorio" else null,
                            )
                        }
                        ProfileTextField(
                            label = "Correo electrónico o Teléfono",
                            value = account,
                            onValueChange = { account = it },
                            required = true,
                            labelWeight = FontWeight.SemiBold,
                            labelTrailing = { Meta("Taller o Matrícula") },
                            leadingIcon = R.drawable.ic_alternate_email,
                            keyboardType = KeyboardType.Email,
                            error = if (showErrors && account.isBlank()) "Campo obligatorio" else null,
                        )
                        ProfileTextField(
                            label = "Contraseña de acceso",
                            value = password,
                            onValueChange = { password = it },
                            required = true,
                            labelWeight = FontWeight.SemiBold,
                            labelTrailing = {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    CpIcon(R.drawable.ic_key)
                                    Meta("PIN / Alfanumérico")
                                }
                            },
                            leadingIcon = R.drawable.ic_lock_field,
                            keyboardType = KeyboardType.Password,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.7.sp),
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingAction = {
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .clickable(role = Role.Button, onClick = { showPassword = !showPassword })
                                        .semantics { contentDescription = if (showPassword) "Ocultar contraseña" else "Ver contraseña" },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CpIcon(R.drawable.ic_visibility_toggle, tint = if (showPassword) CpPrimary else null)
                                }
                            },
                            error = when {
                                showErrors && password.isBlank() -> "Campo obligatorio"
                                showErrors && password.length < 6 -> "Mínimo 6 caracteres"
                                else -> null
                            },
                        )

                        if (mode == AuthMode.Login) {
                            Row(
                                Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RememberCheckbox(rememberWorkshop, onChange = { rememberWorkshop = it })
                                Text(
                                    "¿Olvidaste tu contraseña?",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = CpPrimary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable(role = Role.Button) { toast.show(Soon) }
                                        .padding(horizontal = 4.dp, vertical = 8.dp),
                                )
                            }
                        }

                        val shape = RoundedCornerShape(12.dp)
                        CpButton(
                            text = if (mode == AuthMode.Login) "Iniciar sesión en mi taller" else "Crear mi cuenta de taller",
                            onClick = ::submit,
                            contentColor = Color.White,
                            containerColor = CpPrimaryContainer,
                            height = 48.dp,
                            gap = 8.dp,
                            textStyle = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.14.sp),
                            trailingIcon = R.drawable.ic_arrow_forward_white,
                            shape = shape,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .fillMaxWidth()
                                .shadow(1.dp, shape, spotColor = Color.Black.copy(0.05f)),
                        )
                    }

                    LabeledDivider("O INGRESA CON")

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickAccessButton("Google", R.drawable.ic_google, CpSurfaceContainerLow, CpOnSurface, Modifier.weight(1f)) {
                            toast.show(Soon)
                        }
                        QuickAccessButton("Huella Rápida", R.drawable.ic_fingerprint, CpSecondaryContainer, CpTextMuted, Modifier.weight(1f)) {
                            toast.show(Soon)
                        }
                    }
                }

                if (mode == AuthMode.Login) {
                    OnboardingCard(
                        onCreate = {
                            mode = AuthMode.Register
                            showErrors = false
                            scope.launch { scroll.animateScrollTo(0) }
                        },
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    )
                }

                OfflineBanner(Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp))

                LegalFooter(onLink = { toast.show(Soon) }, modifier = Modifier.padding(top = 24.dp))
            }
        }
        CpToastHost(toast, Modifier.align(Alignment.BottomCenter))
    }
}

// ---------- Secciones ----------

@Composable
private fun Hero() {
    Column(
        Modifier.fillMaxWidth().padding(top = 16.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.padding(bottom = 12.dp)) {
            val shape = RoundedCornerShape(16.dp)
            Box(
                Modifier
                    .size(64.dp)
                    .shadow(4.dp, shape, ambientColor = Color.Black.copy(0.1f), spotColor = Color.Black.copy(0.1f))
                    .background(CpPrimaryContainer, shape),
                contentAlignment = Alignment.Center,
            ) {
                CpIcon(R.drawable.ic_ac_unit_hero)
            }
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(4.dp, 4.dp)
                    .size(24.dp)
                    .shadow(1.dp, CircleShape, spotColor = Color.Black.copy(0.05f))
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                CpIcon(R.drawable.ic_verified_badge)
            }
        }
        StatusBadge(
            text = "VERSIÓN PROFESIONAL • TREECODEDEV",
            containerColor = CpSecondaryContainer,
            contentColor = CpOnSecondaryFixed,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            textStyle = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.275.sp),
            leading = { CpIcon(R.drawable.ic_check_circle_primary) },
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Text(
            "Acceso Técnico Especializado",
            style = MaterialTheme.typography.headlineSmall.copy(letterSpacing = (-0.6).sp),
            color = CpOnSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            "Plataforma técnica para especialistas en climatización, refrigeración y minisplits",
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 19.25.sp),
            color = CpTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 320.dp).padding(top = 4.dp, start = 26.dp, end = 26.dp),
        )
    }
}

/** Control segmentado Iniciar Sesión / Crear Cuenta. */
@Composable
private fun ModeSwitcher(mode: AuthMode, onSelect: (AuthMode) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .cpCard(CpSectionShape)
            .background(CpSurfaceContainerHigh, CpSectionShape)
            .padding(4.dp)
            .selectableGroup(),
    ) {
        AuthMode.entries.forEach { m ->
            val selected = m == mode
            val shape = RoundedCornerShape(8.dp)
            Row(
                Modifier
                    .weight(1f)
                    .height(48.dp)
                    .then(if (selected) Modifier.shadow(1.dp, shape, spotColor = Color.Black.copy(0.05f)) else Modifier)
                    .clip(shape)
                    .background(if (selected) Color.White else Color.Transparent)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(m) }),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CpIcon(m.icon, tint = if (selected) CpPrimary else CpTextSecondary)
                Text(
                    m.label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        letterSpacing = 0.14.sp,
                    ),
                    color = if (selected) CpPrimary else CpTextSecondary,
                )
            }
        }
    }
}

@Composable
private fun Meta(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = CpTextSecondary)
}

/** Casilla "Recordar taller" (20dp, primario al marcar, borde gris al desmarcar). */
@Composable
private fun RememberCheckbox(checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(4.dp))
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onChange)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val shape = RoundedCornerShape(2.5.dp)
        Box(
            Modifier
                .size(20.dp)
                .then(
                    if (checked) Modifier.background(CpPrimary, shape)
                    else Modifier.border(2.dp, CpOutline, shape),
                ),
        )
        Text(
            "Recordar taller",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = CpOnSurface,
        )
    }
}

/** Línea divisoria con etiqueta centrada. */
@Composable
private fun LabeledDivider(label: String) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        HorizontalDivider(thickness = 1.dp, color = CpSurfaceContainerHigh)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.55.sp),
            color = CpTextSecondary,
            modifier = Modifier.background(Color.White).padding(horizontal = 12.dp),
        )
    }
}

@Composable
private fun QuickAccessButton(
    text: String,
    @DrawableRes icon: Int,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    CpButton(
        text = text,
        onClick = onClick,
        contentColor = content,
        containerColor = container,
        height = 48.dp,
        gap = 8.dp,
        textStyle = MaterialTheme.typography.labelMedium,
        leadingIcon = icon,
        modifier = modifier,
    )
}

/** Tarjeta de propuesta de valor para nuevos talleres. */
@Composable
private fun OnboardingCard(onCreate: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .shadow(1.dp, CpSectionShape, ambientColor = Color.Black.copy(0.05f), spotColor = Color.Black.copy(0.05f))
            .background(CpSurfaceContainerLow, CpSectionShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(40.dp).background(CpSecondaryContainer, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                CpIcon(R.drawable.ic_nav_services, tint = CpPrimary)
            }
            Column {
                Text("¿Nuevo en CierrePro?", style = MaterialTheme.typography.titleSmall, color = CpOnSurface)
                Text(
                    "Registra tu taller en 1 minuto, gestiona órdenes HVAC y genera tu primer reporte técnico sin costo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CpTextSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
        val shape = RoundedCornerShape(8.dp)
        CpButton(
            text = "Crear cuenta de taller gratis",
            onClick = onCreate,
            contentColor = CpPrimary,
            containerColor = Color.White,
            height = 44.dp,
            gap = 6.dp,
            textStyle = MaterialTheme.typography.labelMedium,
            leadingIcon = R.drawable.ic_add_business,
            modifier = Modifier.fillMaxWidth().shadow(1.dp, shape, spotColor = Color.Black.copy(0.05f)),
        )
    }
}

/** Aviso de almacenamiento local y sincronización sin señal. */
@Composable
private fun OfflineBanner(modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().background(CpSurfaceContainer, RoundedCornerShape(8.dp)).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(32.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
            CpIcon(R.drawable.ic_offline_pin)
        }
        Column {
            Text("Almacenamiento Local FIPS 140-3", style = MaterialTheme.typography.labelSmall, color = CpOnSurface)
            Text(
                "Tus bitácoras se guardan en el dispositivo y sincronizan automáticamente al recuperar señal.",
                style = MaterialTheme.typography.bodySmall,
                color = CpTextSecondary,
            )
        }
    }
}

@Composable
private fun LegalFooter(onLink: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf("Términos de\nservicio", "Aviso de\nprivacidad", "Soporte\ntécnico").forEachIndexed { i, link ->
                if (i > 0) Text("•", style = MaterialTheme.typography.labelSmall, color = CpTextSecondary)
                Text(
                    link,
                    style = MaterialTheme.typography.labelSmall,
                    color = CpTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(role = Role.Button) { onLink(link) }
                        .padding(horizontal = 20.dp),
                )
            }
        }
        Text(
            "CierrePro Mobile Engine v3.4.1 (Build 240) • HVAC Edition",
            style = MaterialTheme.typography.labelSmall,
            color = CpOutline,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1400)
@Composable
private fun AuthScreenPreview() {
    DemoTheme { AuthScreen(onSignedIn = { _, _ -> }, onRegistered = { _, _ -> }, onBack = {}) }
}
