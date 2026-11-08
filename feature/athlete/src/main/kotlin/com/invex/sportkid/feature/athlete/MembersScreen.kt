package com.invex.sportkid.feature.athlete

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.sportkid.core.designsystem.component.SkAvatar
import com.invex.sportkid.core.designsystem.component.SkButton
import com.invex.sportkid.core.designsystem.component.SkButtonSize
import com.invex.sportkid.core.designsystem.component.SkDetailTopBar
import com.invex.sportkid.core.designsystem.component.SkHelpButton
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkInfoCallout
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkProfileButton
import com.invex.sportkid.core.designsystem.component.SkTextButton
import com.invex.sportkid.core.designsystem.component.SkTonalButton
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.model.FamilyRole
import com.invex.sportkid.core.ui.ageOn
import com.invex.sportkid.core.ui.compactLabel
import com.invex.sportkid.core.ui.iconRes
import com.invex.sportkid.core.ui.labelRes
import java.time.LocalDate

/** Miembro del círculo familiar de un deportista. [linkedOn] = fecha en que aceptó la invitación. */
@Immutable
data class FamilyMember(
    val id: String,
    val name: String,
    val email: String,
    val role: FamilyRole,
    @DrawableRes val photoRes: Int? = null,
    val linkedOn: LocalDate? = null,
    val isCurrentUser: Boolean = false,
)

/** Datos de demostración (Figma "SportKid - Miembros y accesos"). TODO: repositorio de miembros. */
object MemberSamples {
    /** Tutores por expediente en el plan familiar. */
    const val MAX_MEMBERS = 5

    fun members(): List<FamilyMember> = listOf(
        FamilyMember("mariana", "Mariana Gómez", "mariana.gomez@gmail.com", FamilyRole.OWNER, R.drawable.img_sample_member_mariana, isCurrentUser = true),
        FamilyMember("carlos", "Carlos Mendoza", "carlos.mendoza@outlook.com", FamilyRole.EDITOR, R.drawable.img_sample_member_carlos, LocalDate.of(2024, 10, 12)),
        FamilyMember("ramon", "Prof. Ramón Soto", "ramon.soto@doyangtkd.mx", FamilyRole.VIEWER, R.drawable.img_sample_member_ramon, LocalDate.of(2025, 2, 3)),
    )
}

/**
 * P19. Miembros y accesos (plantilla T5): el deportista en contexto, qué puede hacer cada rol,
 * los miembros con su rol y estado (O26), y la invitación por enlace o QR. Solo la propietaria
 * ve "Quitar acceso", que pide confirmación.
 */
@Composable
fun MembersScreen(
    athlete: AthleteProfile,
    members: List<FamilyMember>,
    onBack: () -> Unit,
    onInvite: () -> Unit,
    onShareLink: () -> Unit,
    onRemoveMember: (FamilyMember) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    maxMembers: Int = MemberSamples.MAX_MEMBERS,
    onFamilyPlans: () -> Unit = {},
    onHelp: () -> Unit = {},
    onProfile: () -> Unit = {},
) {
    var pendingRemoval by rememberSaveable { mutableStateOf<String?>(null) }
    val canManage = members.any { it.isCurrentUser && it.role == FamilyRole.OWNER }
    val colors = MaterialTheme.colorScheme
    Scaffold(
        modifier = modifier,
        containerColor = colors.surface,
        topBar = {
            SkDetailTopBar(title = stringResource(R.string.members_title), onBack = onBack) {
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
                .padding(bottom = 40.dp),
        ) {
            SubHeader(onInvite, Modifier.padding(top = SportKidTheme.spacing.sm, bottom = 4.dp))
            AthleteBanner(athlete, today, Modifier.padding(vertical = SportKidTheme.spacing.sm))
            RolesCallout(Modifier.padding(vertical = 4.dp))
            Row(Modifier.padding(top = SportKidTheme.spacing.lg, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
                    Text(
                        stringResource(R.string.members_active),
                        modifier = Modifier.semantics { heading() },
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.onSurface,
                    )
                    Text(stringResource(R.string.members_count, members.size, maxMembers), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                }
                SkTextButton(
                    stringResource(R.string.members_plans),
                    onClick = onFamilyPlans,
                    color = colors.primary,
                    textStyle = MaterialTheme.typography.labelSmall,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
                members.forEach { member ->
                    MemberRow(member, canRemove = canManage && member.role != FamilyRole.OWNER, onRemove = { pendingRemoval = member.id })
                }
            }
            InviteCard(onShareLink = onShareLink, onShowQr = onInvite, modifier = Modifier.padding(top = SportKidTheme.spacing.xl, bottom = SportKidTheme.spacing.sm))
            SecurityNote(Modifier.padding(vertical = SportKidTheme.spacing.sm))
        }
    }

    members.firstOrNull { it.id == pendingRemoval }?.let { member ->
        AlertDialog(
            onDismissRequest = { pendingRemoval = null },
            title = { Text(stringResource(R.string.members_remove_title, member.name)) },
            text = { Text(stringResource(R.string.members_remove_body, athlete.firstName)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingRemoval = null
                    onRemoveMember(member)
                }) { Text(stringResource(R.string.members_remove), color = colors.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemoval = null }) { Text(stringResource(R.string.members_cancel)) }
            },
        )
    }
}

@Composable
private fun SubHeader(onInvite: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).background(colors.primary, CircleShape))
        Spacer(Modifier.width(4.dp))
        Text(
            stringResource(R.string.members_overline).uppercase(),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.3.sp),
            color = colors.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        SkButton(
            stringResource(R.string.members_invite),
            onClick = onInvite,
            modifier = Modifier.height(36.dp),
            leadingIcon = painterResource(R.drawable.ic_members_person_add),
            size = SkButtonSize.Small,
        )
    }
}

/** El deportista cuyo círculo se administra: avatar con su deporte, nombre, edad y propósito. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AthleteBanner(athlete: AthleteProfile, today: LocalDate, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = colors.surfaceContainerLowest, shadowElevation = SkElevation.level1) {
        Box {
            // Halo decorativo (Figma: círculo con blur); no participa en el tamaño de la card.
            Box(Modifier.matchParentSize()) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(40.dp, 40.dp)
                        .size(144.dp)
                        .background(Brush.radialGradient(listOf(colors.primaryContainer.copy(alpha = 0.6f), colors.primaryContainer.copy(alpha = 0f))), CircleShape),
                )
            }
            Row(Modifier.padding(SportKidTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(56.dp).background(colors.surfaceContainer, CircleShape).padding(4.dp)) {
                    SkAvatar(
                        photo = athlete.avatarRes?.let { painterResource(it) },
                        name = athlete.fullName,
                        size = 48.dp,
                        ringColor = athlete.color(),
                    )
                    athlete.sports.firstOrNull()?.let { sport ->
                        Box(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .offset(4.dp, 4.dp)
                                .size(16.dp)
                                .background(colors.primary, CircleShape)
                                .border(2.dp, colors.surfaceContainerLowest, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(painterResource(sport.iconRes), contentDescription = null, modifier = Modifier.size(8.dp), tint = colors.onPrimary)
                        }
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
                        Text(
                            athlete.fullName,
                            modifier = Modifier.align(Alignment.CenterVertically),
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurface,
                        )
                        SkPill(
                            stringResource(com.invex.sportkid.core.ui.R.string.athlete_age, athlete.birthDate.ageOn(today)),
                            modifier = Modifier.align(Alignment.CenterVertically),
                            containerColor = colors.secondaryContainer,
                            contentColor = colors.secondary,
                            contentPadding = PaddingValues(horizontal = SportKidTheme.spacing.sm, vertical = 2.dp),
                        )
                    }
                    Text(stringResource(R.string.members_athlete_subtitle), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun RolesCallout(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val editors = stringResource(R.string.members_roles_editors)
    val owner = stringResource(R.string.members_roles_owner)
    val body = stringResource(R.string.members_roles_body, editors, owner)
    val emphasis = SpanStyle(fontWeight = FontWeight.Bold, color = colors.onSurface)
    SkInfoCallout(
        text = buildAnnotatedString {
            append(body)
            listOf(editors, owner).forEach { word ->
                val start = body.indexOf(word)
                if (start >= 0) addStyle(emphasis, start, start + word.length)
            }
        },
        modifier = modifier,
        containerColor = colors.surfaceContainerLow,
        contentColor = colors.onSurfaceVariant,
        shape = MaterialTheme.shapes.medium,
        contentPadding = 14.dp,
        spacing = SportKidTheme.spacing.md,
        leading = {
            SkIconCircle(
                icon = painterResource(R.drawable.ic_members_shield),
                contentDescription = null,
                size = 32.dp,
                containerColor = colors.primaryContainer,
                iconTint = colors.onPrimaryContainer,
                modifier = Modifier.padding(top = 2.dp),
            )
        },
    )
}

private data class RoleStyle(val container: Color, val content: Color, @DrawableRes val badge: Int, @DrawableRes val pill: Int)

@Composable
private fun FamilyRole.style(): RoleStyle {
    val colors = MaterialTheme.colorScheme
    return when (this) {
        FamilyRole.OWNER -> RoleStyle(colors.primary, colors.onPrimary, R.drawable.ic_members_owner, R.drawable.ic_members_key)
        FamilyRole.EDITOR -> RoleStyle(colors.secondaryContainer, colors.secondary, R.drawable.ic_members_edit, R.drawable.ic_members_flag)
        FamilyRole.VIEWER -> RoleStyle(colors.surfaceContainerHigh, colors.onSurfaceVariant, R.drawable.ic_members_visibility, R.drawable.ic_members_school)
    }
}

/** M25. Avatar con badge del rol, nombre, pill de rol, correo, estado y "Quitar acceso". */
@Composable
private fun MemberRow(member: FamilyMember, canRemove: Boolean, onRemove: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val role = member.role.style()
    Surface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = colors.surfaceContainerLowest, shadowElevation = SkElevation.card) {
        Row(Modifier.padding(SportKidTheme.spacing.lg)) {
            SkAvatar(
                photo = member.photoRes?.let { painterResource(it) },
                name = member.name,
                size = 48.dp,
                placeholderColor = colors.surfaceContainer,
                placeholderContentColor = colors.onSurfaceVariant,
            ) {
                Surface(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(4.dp, 4.dp)
                        .size(20.dp),
                    shape = CircleShape,
                    color = role.container,
                    contentColor = role.content,
                    shadowElevation = SkElevation.level1,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(painterResource(role.badge), contentDescription = null, modifier = Modifier.size(10.dp))
                    }
                }
            }
            Spacer(Modifier.width(SportKidTheme.spacing.md))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        member.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    SkPill(
                        stringResource(member.role.labelRes),
                        containerColor = role.container,
                        contentColor = role.content,
                        icon = painterResource(role.pill),
                    )
                }
                Text(member.email, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Column(Modifier.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
                    MemberStatus(member)
                    if (canRemove) {
                        val description = stringResource(R.string.members_remove_cd, member.name)
                        Row(
                            Modifier
                                .offset(x = (-10).dp)
                                .semantics(mergeDescendants = true) { contentDescription = description },
                        ) {
                            SkTextButton(
                                stringResource(R.string.members_remove),
                                onClick = onRemove,
                                color = colors.error,
                                textStyle = MaterialTheme.typography.labelMedium,
                                leadingIcon = painterResource(R.drawable.ic_members_person_remove),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberStatus(member: FamilyMember) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        when {
            member.isCurrentUser -> {
                Icon(painterResource(R.drawable.ic_members_check_circle), contentDescription = null, tint = colors.primary)
                Text(stringResource(R.string.members_you_created), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                if (member.role == FamilyRole.OWNER) {
                    SkIconCircle(
                        icon = painterResource(R.drawable.ic_members_lock),
                        contentDescription = stringResource(R.string.members_owner_locked_cd),
                        size = 28.dp,
                        containerColor = colors.surfaceContainer,
                        iconTint = colors.onSurfaceVariant,
                    )
                }
            }
            member.role == FamilyRole.VIEWER -> {
                Icon(painterResource(R.drawable.ic_members_event_available), contentDescription = null, tint = colors.onSurfaceVariant)
                Text(stringResource(R.string.members_viewer_access), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            else -> {
                Box(Modifier.size(8.dp).background(colors.primary, CircleShape))
                Text(
                    member.linkedOn?.let { stringResource(R.string.members_linked_on, it.compactLabel()) }.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InviteCard(onShareLink: () -> Unit, onShowQr: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = colors.surfaceContainerLowest, shadowElevation = SkElevation.card) {
        Column(Modifier.padding(SportKidTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkIconCircle(
                    icon = painterResource(R.drawable.ic_members_qr),
                    contentDescription = null,
                    size = 44.dp,
                    containerColor = colors.primaryContainer,
                    iconTint = colors.primary,
                )
                Spacer(Modifier.width(SportKidTheme.spacing.md))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.members_invite_title), style = MaterialTheme.typography.titleSmall, color = colors.onSurface)
                    Text(stringResource(R.string.members_invite_body), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm)) {
                SkTonalButton(
                    stringResource(R.string.members_share_link),
                    onClick = onShareLink,
                    modifier = Modifier.weight(1f),
                    icon = painterResource(R.drawable.ic_members_share),
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                    size = SkButtonSize.Medium,
                )
                val qrDescription = stringResource(R.string.members_show_qr_cd)
                Surface(
                    onClick = onShowQr,
                    modifier = Modifier
                        .size(48.dp)
                        .semantics { contentDescription = qrDescription; role = Role.Button },
                    shape = MaterialTheme.shapes.medium,
                    color = colors.surfaceContainer,
                    contentColor = colors.onSurface,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(painterResource(R.drawable.ic_members_qr_code), contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityNote(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow, MaterialTheme.shapes.medium)
            .padding(SportKidTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(R.drawable.ic_members_shield_filled), contentDescription = null, tint = colors.primary)
        Spacer(Modifier.width(SportKidTheme.spacing.md))
        Text(stringResource(R.string.members_security), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}

@SkPreviews
@Composable
private fun MembersScreenPreview() = SportKidTheme {
    MembersScreen(
        athlete = AthleteSamples.athletes(LocalDate.of(2026, 11, 7)).first(),
        members = MemberSamples.members(),
        onBack = {},
        onInvite = {},
        onShareLink = {},
        onRemoveMember = {},
        today = LocalDate.of(2026, 11, 7),
    )
}
