package com.invex.sportkid.feature.sharing

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.CopyableCodeChip
import com.invex.sportkid.core.designsystem.component.CountdownText
import com.invex.sportkid.core.designsystem.component.SkAvatar
import com.invex.sportkid.core.designsystem.component.SkButton
import com.invex.sportkid.core.designsystem.component.SkButtonSize
import com.invex.sportkid.core.designsystem.component.SkDetailTopBar
import com.invex.sportkid.core.designsystem.component.SkHelpButton
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkInfoCallout
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkProfileButton
import com.invex.sportkid.core.designsystem.component.SkTonalButton
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full
import com.invex.sportkid.core.designsystem.theme.hero
import com.invex.sportkid.core.model.Sport
import com.invex.sportkid.core.ui.ageOn
import com.invex.sportkid.core.ui.iconRes
import com.invex.sportkid.core.ui.labelRes
import kotlinx.coroutines.delay
import java.time.LocalDate
import com.invex.sportkid.core.designsystem.R as DsR

private const val CopiedToastMillis = 2_000L

/**
 * P17. Compartir QR (plantilla T4): pase de acceso con el QR del código de invitación, clave
 * para copiar con su vigencia, permisos que recibe el cotutor y acciones para compartir el
 * enlace o generar un código nuevo.
 *
 * @param onShareLink recibe el texto de la invitación para la hoja de compartir del sistema.
 */
@Composable
fun ShareQrScreen(
    athlete: ShareAthlete,
    code: InviteCode,
    onBack: () -> Unit,
    onShareLink: (message: String) -> Unit,
    onRegenerate: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    onHelp: () -> Unit = {},
    onProfile: () -> Unit = {},
) {
    var copies by remember { mutableIntStateOf(0) }
    LaunchedEffect(copies) {
        if (copies > 0) {
            delay(CopiedToastMillis)
            copies = 0
        }
    }
    val message = stringResource(R.string.share_link_message, athlete.firstName, code.display, InviteCode.VALIDITY_HOURS)
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            SkDetailTopBar(title = stringResource(R.string.share_title, athlete.firstName), onBack = onBack) {
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
                .padding(horizontal = SportKidTheme.spacing.screenMargin),
            verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.lg),
        ) {
            AthletePill(athlete, today, Modifier.padding(top = SportKidTheme.spacing.sm))
            QrPassCard(athlete, code, onCopied = { copies++ })
            SkInfoCallout(
                text = AnnotatedString(stringResource(R.string.share_sync_body, athlete.firstName)),
                title = stringResource(R.string.share_sync_title),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.secondary,
                bodyColor = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(16.dp),
                leading = {
                    SkIconCircle(
                        icon = painterResource(DsR.drawable.sk_ic_info),
                        contentDescription = null,
                        size = 32.dp,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                },
            )
            PermissionsCard()
            Column(Modifier.padding(top = SportKidTheme.spacing.sm), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
                SkButton(
                    stringResource(R.string.share_send_link),
                    onClick = { onShareLink(message) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = painterResource(R.drawable.ic_share_send),
                    size = SkButtonSize.Medium,
                )
                SkTonalButton(
                    stringResource(R.string.share_regenerate),
                    onClick = onRegenerate,
                    modifier = Modifier.fillMaxWidth(),
                    icon = painterResource(R.drawable.ic_share_renew),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    size = SkButtonSize.Medium,
                    shape = MaterialTheme.shapes.full,
                )
            }
            CopiedToast(visible = copies > 0)
            Footer(Modifier.padding(bottom = SportKidTheme.spacing.lg))
        }
    }
}

/** Píldora con el deportista que se comparte: avatar con su deporte, nombre, edad y deportes. */
@Composable
private fun AthletePill(athlete: ShareAthlete, today: LocalDate, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.full, color = colors.surfaceContainerLow, shadowElevation = SkElevation.level1) {
        Row(Modifier.padding(start = 4.dp, end = SportKidTheme.spacing.lg, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            SkAvatar(
                photo = athlete.photoRes?.let { painterResource(it) },
                name = athlete.fullName,
                size = 44.dp,
                ringBrush = Brush.linearGradient(listOf(athlete.color(), colors.primary)),
            ) {
                athlete.sports.firstOrNull()?.let { SportBadge(it, Modifier.align(Alignment.BottomEnd)) }
            }
            Spacer(Modifier.width(SportKidTheme.spacing.sm))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.xs)) {
                    Text(
                        athlete.fullName,
                        modifier = Modifier.weight(1f, fill = false),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        stringResource(R.string.share_athlete_age, athlete.birthDate.ageOn(today)),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = MaterialTheme.typography.bodySmall.fontWeight),
                        color = colors.onSurfaceVariant,
                    )
                }
                Text(
                    athlete.sports.map { stringResource(it.labelRes) }.joinToString(stringResource(R.string.share_sports_separator)),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            SkIconCircle(
                icon = painterResource(R.drawable.ic_share_shield),
                contentDescription = stringResource(R.string.share_secure_cd),
                size = 32.dp,
                containerColor = colors.secondaryContainer,
                iconTint = colors.secondary,
            )
        }
    }
}

/** Badge de 14dp `primary` con el icono del deporte en la esquina del avatar. */
@Composable
private fun SportBadge(sport: Sport, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(14.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painterResource(sport.iconRes), contentDescription = null, modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.onPrimary)
    }
}

/** Card del pase: título, instrucciones, QR, clave copiable y vigencia. */
@Composable
private fun QrPassCard(athlete: ShareAthlete, code: InviteCode, onCopied: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.hero,
        color = colors.surfaceContainerLowest,
        shadowElevation = SkElevation.level2,
    ) {
        Box {
            // Halos decorativos (Figma: círculos con blur; `Modifier.blur` no existe antes de Android 12).
            Glow(
                Brush.radialGradient(listOf(colors.primaryContainer.copy(alpha = 0.45f), colors.primaryContainer.copy(alpha = 0f))),
                Modifier.align(Alignment.TopEnd),
                sizeDp = 176,
                dx = 72,
                dy = -72,
            )
            Glow(
                Brush.radialGradient(listOf(colors.tertiaryContainer.copy(alpha = 0.6f), colors.tertiaryContainer.copy(alpha = 0f))),
                Modifier.align(Alignment.BottomStart),
                sizeDp = 160,
                dx = -64,
                dy = 64,
            )
            Column(Modifier.fillMaxWidth().padding(SportKidTheme.spacing.xl)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.ic_share_qr), contentDescription = null, tint = colors.primary)
                    Spacer(Modifier.width(SportKidTheme.spacing.xs))
                    Text(
                        stringResource(R.string.share_pass_title),
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                    )
                }
                Text(
                    stringResource(R.string.share_pass_body),
                    modifier = Modifier.padding(top = SportKidTheme.spacing.sm),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
                Box(
                    Modifier
                        .padding(top = SportKidTheme.spacing.lg)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .padding(SportKidTheme.spacing.lg),
                ) {
                    InviteQrCode(payload = code.qrPayload, contentDescription = stringResource(R.string.share_qr_cd, code.display))
                }
                CopyableCodeChip(code = code.display, onCopied = onCopied, modifier = Modifier.padding(top = SportKidTheme.spacing.xl))
                CountdownText(
                    expiresAtMillis = code.expiresAtMillis,
                    modifier = Modifier
                        .padding(top = SportKidTheme.spacing.sm)
                        .align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

/** Halo radial de [sizeDp] centrado en la esquina de la card (desplazado con [dx]/[dy]). */
@Composable
private fun Glow(brush: Brush, modifier: Modifier, sizeDp: Int, dx: Int, dy: Int) {
    Box(
        modifier
            .offset(dx.dp, dy.dp)
            .size(sizeDp.dp)
            .background(brush, CircleShape),
    )
}

private data class Permission(@DrawableRes val icon: Int, @StringRes val title: Int, @StringRes val detail: Int)

private val Permissions = listOf(
    Permission(R.drawable.ic_share_calendar, R.string.share_permission_calendar, R.string.share_permission_calendar_detail),
    Permission(R.drawable.ic_share_medal, R.string.share_permission_history, R.string.share_permission_history_detail),
    Permission(R.drawable.ic_share_receipt, R.string.share_permission_expenses, R.string.share_permission_expenses_detail),
)

/** Lo que recibe el cotutor: calendario, historial deportivo y gastos. */
@Composable
private fun PermissionsCard() {
    val colors = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = colors.surfaceContainerLowest, shadowElevation = SkElevation.card) {
        Column(Modifier.padding(SportKidTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.share_permissions_title),
                    modifier = Modifier.weight(1f).semantics { heading() },
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onSurface,
                )
                SkPill(
                    stringResource(R.string.share_permissions_badge),
                    containerColor = colors.primaryContainer,
                    contentColor = colors.primary,
                    contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                )
            }
            Permissions.forEachIndexed { index, permission ->
                PermissionRow(
                    icon = painterResource(permission.icon),
                    iconTint = when (index) {
                        1 -> colors.tertiary
                        2 -> colors.secondary
                        else -> colors.primary
                    },
                    title = stringResource(permission.title),
                    detail = stringResource(permission.detail),
                )
            }
        }
    }
}

/** Permiso que recibe el cotutor: icono en tile, título, detalle y check de incluido. */
@Composable
private fun PermissionRow(icon: Painter, iconTint: Color, title: String, detail: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.medium)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(36.dp).background(colors.surfaceContainer, MaterialTheme.shapes.small), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconTint)
        }
        Spacer(Modifier.width(SportKidTheme.spacing.sm))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(SportKidTheme.spacing.sm))
        Icon(
            painterResource(R.drawable.ic_share_check_circle),
            contentDescription = stringResource(R.string.share_permission_included_cd),
            tint = colors.primary,
        )
    }
}

/** Confirmación breve al copiar (Figma: "Micro Toast"). Reserva su espacio para no mover el contenido. */
@Composable
private fun CopiedToast(visible: Boolean) {
    val alpha by animateFloatAsState(if (visible) 1f else 0f, label = "copiedToast")
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .semantics { liveRegion = LiveRegionMode.Polite },
        contentAlignment = Alignment.Center,
    ) {
        if (alpha > 0f) {
            SkPill(
                stringResource(R.string.share_copied),
                containerColor = colors.inverseSurface,
                contentColor = colors.inverseOnSurface,
                icon = painterResource(R.drawable.ic_share_done),
                iconTint = colors.inversePrimary,
                contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.lg, vertical = 6.dp),
                textStyle = MaterialTheme.typography.bodySmall,
                shadowElevation = SkElevation.level2,
            )
        } else {
            Spacer(Modifier.height(28.dp))
        }
    }
}

/** Nota de seguridad al pie: candado y "Cifrado y protegido…". */
@Composable
private fun Footer(modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(R.drawable.ic_share_lock), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(SportKidTheme.spacing.xs))
        Text(
            stringResource(R.string.share_footer),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

internal val SampleShareAthlete = ShareAthlete(
    id = "mateo",
    firstName = "Mateo",
    fullName = "Mateo Gómez",
    colorIndex = 0,
    birthDate = LocalDate.of(2017, 5, 15),
    sports = listOf(Sport.TAEKWONDO, Sport.SWIMMING),
)

@SkPreviews
@Composable
private fun ShareQrScreenPreview() = SportKidTheme {
    ShareQrScreen(
        athlete = SampleShareAthlete,
        code = SharingSamples.code(0),
        onBack = {},
        onShareLink = {},
        onRegenerate = {},
        today = LocalDate.of(2026, 11, 7),
    )
}
