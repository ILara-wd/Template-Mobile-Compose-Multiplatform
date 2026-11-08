package com.invex.sportkid.feature.settings

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SettingsRow
import com.invex.sportkid.core.designsystem.component.SettingsSection
import com.invex.sportkid.core.designsystem.component.SkAvatar
import com.invex.sportkid.core.designsystem.component.SkButton
import com.invex.sportkid.core.designsystem.component.SkButtonSize
import com.invex.sportkid.core.designsystem.component.SkDetailTopBar
import com.invex.sportkid.core.designsystem.component.SkHelpButton
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkProfileButton
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.AppPalette
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.ThemeMode
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.ui.colors
import com.invex.sportkid.core.ui.iconRes
import com.invex.sportkid.core.ui.labelRes
import kotlinx.coroutines.delay

/** Hijo en el resumen de la cuenta: nombre y deporte principal. */
@Immutable
data class SettingsAthlete(val firstName: String, val sport: Sport)

/** Cuenta del tutor que ve Ajustes. */
@Immutable
data class SettingsAccount(
    val name: String,
    val email: String,
    val familyName: String,
    val athletes: List<SettingsAthlete>,
    val tutors: List<String>,
    @DrawableRes val photoRes: Int? = null,
)

/** Anticipación de los recordatorios de eventos (M12). */
enum class ReminderLead(@StringRes val labelRes: Int) {
    NONE(R.string.settings_lead_none),
    ONE_HOUR(R.string.settings_lead_hour),
    ONE_DAY(R.string.settings_lead_day),
    TWO_DAYS(R.string.settings_lead_two_days),
}

/** Datos de demostración (Figma "SportKid - Ajustes"). TODO: cuenta y preferencias reales. */
object SettingsSamples {
    val account = SettingsAccount(
        name = "Mariana Gómez",
        email = "mariana.gomez@gmail.com",
        familyName = "Gómez Ríos",
        athletes = listOf(SettingsAthlete("Mateo", Sport.TAEKWONDO), SettingsAthlete("Sofía", Sport.SWIMMING)),
        tutors = listOf("Mariana", "Carlos"),
    )
}

private const val MinuteMillis = 60_000L

/**
 * P20. Ajustes (plantilla T5): cuenta y familia, apariencia (tema y modo oscuro), ajustes de
 * Taekwondo (si algún hijo lo practica), notificaciones, nube familiar y acerca de.
 *
 * [palette] y [mode] son los del tema activo; los cambios suben con [onOpenTheme] y [onModeChange].
 * Las demás preferencias viven aquí mientras no exista el repositorio de ajustes.
 */
@Composable
fun SettingsScreen(
    account: SettingsAccount,
    palette: AppPalette,
    mode: ThemeMode,
    versionName: String,
    versionCode: Int,
    onBack: () -> Unit,
    onOpenTheme: () -> Unit,
    onModeChange: (ThemeMode) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    onBeltSequence: () -> Unit = {},
    onTerms: () -> Unit = {},
    onSupport: () -> Unit = {},
    onHelp: () -> Unit = {},
    onProfile: () -> Unit = {},
) {
    var examCosts by rememberSaveable { mutableStateOf(true) }
    var reminders by rememberSaveable { mutableStateOf(true) }
    var lead by rememberSaveable { mutableStateOf(ReminderLead.TWO_DAYS) }
    var qrExpiry by rememberSaveable { mutableStateOf(true) }
    var pickingLead by rememberSaveable { mutableStateOf(false) }
    var confirmSignOut by rememberSaveable { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    Scaffold(
        modifier = modifier,
        containerColor = colors.surface,
        topBar = {
            SkDetailTopBar(title = stringResource(R.string.settings_title), onBack = onBack) {
                SkHelpButton(onClick = onHelp)
                SkProfileButton(onClick = onProfile)
            }
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = SportKidTheme.spacing.screenMargin)
                .padding(top = SportKidTheme.spacing.md, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg),
        ) {
            AccountCard(account, onSignOut = { confirmSignOut = true })

            SettingsSection(stringResource(R.string.settings_appearance), painterResource(R.drawable.ic_settings_palette)) {
                SettingsRow(
                    title = stringResource(R.string.settings_theme),
                    icon = painterResource(R.drawable.ic_settings_palette),
                    subtitle = stringResource(R.string.settings_theme_value, stringResource(palette.labelRes), stringResource(palette.detailRes)),
                    iconContainerColor = colors.primaryContainer,
                    iconTint = colors.primary,
                    onClick = onOpenTheme,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(14.dp).background(colors.primary, CircleShape))
                        Icon(
                            painterResource(com.invex.sportkid.core.designsystem.R.drawable.sk_ic_chevron_right),
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                        )
                    }
                }
                val systemDark = isSystemInDarkTheme()
                val dark = when (mode) {
                    ThemeMode.System -> systemDark
                    ThemeMode.Light -> false
                    ThemeMode.Dark -> true
                }
                SettingsRow(
                    title = stringResource(R.string.settings_dark_mode),
                    icon = painterResource(R.drawable.ic_settings_dark_mode),
                    subtitle = stringResource(if (mode == ThemeMode.System) R.string.settings_dark_mode_system else R.string.settings_dark_mode_manual),
                    checked = dark,
                    onCheckedChange = { onModeChange(if (it) ThemeMode.Dark else ThemeMode.Light) },
                )
            }

            if (account.athletes.any { it.sport == Sport.TAEKWONDO }) {
                SettingsSection(
                    title = stringResource(R.string.settings_tkd),
                    icon = painterResource(Sport.TAEKWONDO.iconRes),
                    iconTint = Sport.TAEKWONDO.colors().content,
                    badge = {
                        SkPill(
                            stringResource(R.string.settings_tkd_federation),
                            containerColor = colors.tertiary,
                            contentColor = colors.onTertiary,
                            contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                        )
                    },
                ) {
                    SettingsRow(
                        title = stringResource(R.string.settings_belts),
                        icon = painterResource(R.drawable.ic_settings_belts),
                        subtitle = stringResource(R.string.settings_belts_detail),
                        iconContainerColor = colors.tertiaryContainer,
                        iconTint = colors.tertiary,
                        onClick = onBeltSequence,
                    )
                    SettingsRow(
                        title = stringResource(R.string.settings_exam_costs),
                        icon = painterResource(R.drawable.ic_settings_payments),
                        subtitle = stringResource(R.string.settings_exam_costs_detail),
                        checked = examCosts,
                        onCheckedChange = { examCosts = it },
                    )
                }
            }

            SettingsSection(stringResource(R.string.settings_notifications), painterResource(R.drawable.ic_settings_notifications)) {
                SettingsRow(
                    title = stringResource(R.string.settings_reminders),
                    icon = painterResource(R.drawable.ic_settings_event),
                    subtitle = stringResource(R.string.settings_reminders_detail),
                    checked = reminders,
                    onCheckedChange = { reminders = it },
                )
                SettingsRow(
                    title = stringResource(R.string.settings_lead),
                    icon = painterResource(R.drawable.ic_settings_alarm),
                    subtitle = stringResource(lead.labelRes),
                    onClick = { pickingLead = true },
                    modifier = Modifier.alpha(if (reminders) 1f else 0.6f),
                )
                SettingsRow(
                    title = stringResource(R.string.settings_qr_expiry),
                    icon = painterResource(R.drawable.ic_settings_qr),
                    subtitle = stringResource(R.string.settings_qr_expiry_detail),
                    checked = qrExpiry,
                    onCheckedChange = { qrExpiry = it },
                )
            }

            CloudSection(account.tutors)

            SettingsSection(stringResource(R.string.settings_about), painterResource(com.invex.sportkid.core.designsystem.R.drawable.sk_ic_info)) {
                SettingsRow(
                    title = stringResource(R.string.settings_version),
                    icon = painterResource(R.drawable.ic_settings_version),
                    iconContainerSize = 36.dp,
                ) {
                    SkPill(
                        stringResource(R.string.settings_version_value, versionName, versionCode),
                        containerColor = colors.surfaceContainerHigh,
                        contentColor = colors.onSurfaceVariant,
                        contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                    )
                }
                SettingsRow(stringResource(R.string.settings_terms), painterResource(R.drawable.ic_settings_privacy), iconContainerSize = 36.dp, onClick = onTerms)
                SettingsRow(stringResource(R.string.settings_support), painterResource(R.drawable.ic_settings_support), iconContainerSize = 36.dp, onClick = onSupport)
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .alpha(0.7f)
                    .padding(vertical = SportKidTheme.spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Sport.entries.forEach { sport ->
                        Icon(painterResource(sport.iconRes), contentDescription = null, modifier = Modifier.size(15.dp), tint = sport.colors().content)
                    }
                }
                Text(stringResource(R.string.settings_footer), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
    }

    if (pickingLead) {
        LeadDialog(selected = lead, onSelect = { lead = it; pickingLead = false }, onDismiss = { pickingLead = false })
    }
    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text(stringResource(R.string.settings_sign_out_title)) },
            text = { Text(stringResource(R.string.settings_sign_out_body)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmSignOut = false
                    onSignOut()
                }) { Text(stringResource(R.string.settings_sign_out), color = colors.error) }
            },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text(stringResource(R.string.settings_cancel)) } },
        )
    }
}

/** Cuenta, rol, familia y resumen de hijos, con el botón para cerrar sesión. */
@Composable
private fun AccountCard(account: SettingsAccount, onSignOut: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = colors.surfaceContainerLowest, shadowElevation = SkElevation.card) {
        Column(Modifier.padding(SportKidTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg)) {
            Row(verticalAlignment = Alignment.Top) {
                SkAvatar(photo = account.photoRes?.let { painterResource(it) }, name = account.name, size = 56.dp) {
                    SkIconCircle(
                        icon = painterResource(R.drawable.ic_settings_verified_user),
                        contentDescription = null,
                        size = 20.dp,
                        containerColor = colors.primary,
                        iconTint = colors.onPrimary,
                        shadowElevation = SkElevation.level1,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(4.dp, 4.dp),
                    )
                }
                Spacer(Modifier.width(SportKidTheme.spacing.md))
                Column(Modifier.weight(1f)) {
                    Text(account.name, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                    SkPill(
                        stringResource(R.string.settings_role),
                        modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
                        containerColor = colors.secondaryContainer,
                        contentColor = colors.secondary,
                        contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                    )
                    Text(account.email, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    Text(
                        stringResource(
                            R.string.settings_family,
                            account.familyName,
                            pluralStringResource(R.plurals.settings_athletes, account.athletes.size, account.athletes.size),
                        ),
                        modifier = Modifier.padding(top = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primary,
                    )
                }
                Surface(
                    onClick = onSignOut,
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = colors.errorContainer,
                    contentColor = colors.error,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(painterResource(R.drawable.ic_settings_logout), contentDescription = stringResource(R.string.settings_sign_out))
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceContainerLow, MaterialTheme.shapes.medium)
                    .padding(SportKidTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Iniciales encimadas (Figma: `primary` y `tertiary`).
                Row {
                    account.athletes.forEachIndexed { index, athlete ->
                        SkAvatar(
                            photo = null,
                            name = athlete.firstName,
                            size = 28.dp,
                            placeholderColor = if (index % 2 == 0) colors.primary else colors.tertiary,
                            placeholderContentColor = colors.onPrimary,
                            modifier = Modifier
                                .offset(x = (-8 * index).dp)
                                .border(2.dp, colors.surfaceContainerLow, CircleShape),
                        )
                    }
                }
                Spacer(Modifier.width(SportKidTheme.spacing.sm - (8 * (account.athletes.size - 1).coerceAtLeast(0)).dp))
                Text(
                    account.athletes
                        .map { stringResource(R.string.settings_athlete_sport, it.firstName, stringResource(it.sport.labelRes)) }
                        .joinToString(stringResource(R.string.settings_and)),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface,
                )
                Spacer(Modifier.width(SportKidTheme.spacing.sm))
                SkPill(
                    stringResource(R.string.settings_athletes_active),
                    containerColor = colors.surfaceContainer,
                    contentColor = colors.secondary,
                    contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 4.dp),
                )
            }
        }
    }
}

/** Estado de la nube familiar con "Sincronizar ahora". TODO: sincronización real. */
@Composable
private fun CloudSection(tutors: List<String>) {
    val colors = MaterialTheme.colorScheme
    var syncing by rememberSaveable { mutableStateOf(false) }
    var lastSync by rememberSaveable { mutableLongStateOf(System.currentTimeMillis() - 2 * MinuteMillis) }
    var now by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(syncing) {
        if (syncing) {
            delay(1_500)
            lastSync = System.currentTimeMillis()
            syncing = false
        }
        while (true) {
            now = System.currentTimeMillis()
            delay(MinuteMillis)
        }
    }
    val minutes = ((now - lastSync) / MinuteMillis).toInt()
    SettingsSection(
        title = stringResource(R.string.settings_cloud),
        icon = painterResource(R.drawable.ic_settings_cloud),
        badge = {
            SkPill(
                stringResource(if (syncing) R.string.settings_cloud_syncing else R.string.settings_cloud_up_to_date),
                containerColor = colors.secondaryContainer,
                contentColor = colors.secondary,
                contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                leading = { Box(Modifier.size(6.dp).background(colors.primary, CircleShape)) },
            )
        },
    ) {
        Column(
            Modifier
                .padding(horizontal = SportKidTheme.spacing.sm)
                .padding(top = 4.dp)
                .fillMaxWidth()
                .background(colors.surfaceContainerLow, MaterialTheme.shapes.medium)
                .padding(SportKidTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkIconCircle(
                    icon = painterResource(if (syncing) R.drawable.ic_settings_refresh else R.drawable.ic_settings_check_circle),
                    contentDescription = null,
                    size = 32.dp,
                    containerColor = colors.primaryContainer,
                    iconTint = colors.primary,
                    modifier = Modifier.rotate(if (syncing) spinAngle() else 0f),
                )
                Spacer(Modifier.width(SportKidTheme.spacing.sm))
                Column {
                    Text(stringResource(if (syncing) R.string.settings_syncing else R.string.settings_synced), style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                    Text(
                        if (minutes < 1) stringResource(R.string.settings_synced_now) else pluralStringResource(R.plurals.settings_synced_minutes, minutes, minutes),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_settings_group), contentDescription = null, tint = colors.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                Text(
                    pluralStringResource(R.plurals.settings_tutors, tutors.size, tutors.size, tutors.joinToString(" y ")),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
                Spacer(Modifier.width(SportKidTheme.spacing.sm))
                SkButton(
                    stringResource(R.string.settings_sync_now),
                    onClick = { syncing = true },
                    modifier = Modifier.height(36.dp),
                    enabled = !syncing,
                    leadingIcon = painterResource(R.drawable.ic_settings_refresh),
                    size = SkButtonSize.Small,
                )
            }
        }
    }
}

@Composable
private fun spinAngle(): Float {
    val transition = rememberInfiniteTransition(label = "cloudSync")
    val angle by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(1_200, easing = LinearEasing), RepeatMode.Restart), label = "cloudSyncAngle")
    return angle
}

/** Diálogo para elegir con cuánta anticipación llegan los recordatorios. */
@Composable
private fun LeadDialog(selected: ReminderLead, onSelect: (ReminderLead) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_lead)) },
        text = {
            Column(Modifier.selectableGroup()) {
                ReminderLead.entries.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(selected = option == selected, role = Role.RadioButton, onClick = { onSelect(option) })
                            .padding(vertical = SportKidTheme.spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = option == selected, onClick = null)
                        Spacer(Modifier.width(SportKidTheme.spacing.sm))
                        Text(stringResource(option.labelRes), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_cancel)) } },
    )
}

@SkPreviews
@Composable
private fun SettingsScreenPreview() = SportKidTheme {
    SettingsScreen(
        account = SettingsSamples.account,
        palette = AppPalette.Ocean,
        mode = ThemeMode.System,
        versionName = "1.0",
        versionCode = 1,
        onBack = {},
        onOpenTheme = {},
        onModeChange = {},
        onSignOut = {},
    )
}
