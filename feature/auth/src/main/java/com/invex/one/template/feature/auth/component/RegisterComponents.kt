package com.invex.one.template.feature.auth.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import com.invex.one.template.core.designsystem.atom.AppCheckbox
import com.invex.one.template.core.designsystem.atom.AppIcon
import com.invex.one.template.core.designsystem.atom.CheckboxSize
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.molecule.AvatarOptionCard
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppAlpha
import com.invex.one.template.feature.auth.GamerAvatar
import com.invex.one.template.feature.auth.R

/** "★ NUEVO JUGADOR • +250 FICHAS DE BIENVENIDA ⬡" pill. */
@Composable
internal fun WelcomeBonusBanner(bonus: Int, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.pill
    Row(
        modifier = modifier
            .appShadow(AppTheme.elevation.xs, shape, colors.shadow)
            .background(colors.tertiaryFixed, shape)
            .padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(AppIcons.StarCircle)
        Text(
            stringResource(R.string.register_welcome_bonus, bonus).uppercase(),
            style = AppTheme.typography.overline,
            color = colors.onTertiaryFixed,
        )
        AppIcon(AppIcons.Token)
    }
}

/** Card with the avatar choices and the step indicator. */
@Composable
internal fun AvatarPickerCard(
    selected: GamerAvatar,
    onSelected: (GamerAvatar) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    val shape = AppTheme.shapes.large
    Column(
        modifier = modifier
            .fillMaxWidth()
            .appShadow(AppTheme.elevation.xs, shape, colors.shadow)
            .background(colors.surfaceContainerLowest, shape)
            .padding(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                AppIcon(AppIcons.Face)
                Text(stringResource(R.string.register_avatar_title), style = AppTheme.typography.subtitle, color = colors.onSurface)
            }
            Text(
                stringResource(R.string.register_step, 1, 2),
                style = AppTheme.typography.label,
                color = colors.secondary,
                modifier = Modifier
                    .background(colors.secondaryFixed, AppTheme.shapes.pill)
                    .padding(horizontal = spacing.sm, vertical = spacing.xxxs),
            )
        }
        Row(
            modifier = Modifier.padding(vertical = spacing.xxs),
            horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
        ) {
            GamerAvatar.entries.forEach { avatar ->
                AvatarOptionCard(
                    image = avatar.image,
                    name = avatar.displayName,
                    circleColor = avatar.circleColor(),
                    selected = avatar == selected,
                    onClick = { onSelected(avatar) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun GamerAvatar.circleColor(): Color {
    val c = AppTheme.colors
    return when (this) {
        GamerAvatar.CiberBot -> c.secondaryFixed
        GamerAvatar.Valeria -> c.tertiaryFixed
        GamerAvatar.Draco -> c.primaryFixed
        GamerAvatar.Astro -> c.tertiaryContainer
    }
}

/** "Disponible" badge next to the gamertag label. */
@Composable
internal fun AvailableBadge(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .background(colors.primaryContainer.copy(alpha = AppAlpha.Tint), AppTheme.shapes.pill)
            .padding(horizontal = AppTheme.spacing.sm, vertical = AppTheme.spacing.xxxs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(AppIcons.CheckCircle)
        Text(stringResource(R.string.register_gamertag_available), style = AppTheme.typography.label, color = colors.primary)
    }
}

/** Small status text with optional leading icon (strength, match). */
@Composable
internal fun FieldStatus(text: String, color: Color, showCheck: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs), verticalAlignment = Alignment.CenterVertically) {
        if (showCheck) AppIcon(AppIcons.CheckTiny)
        Text(text, style = AppTheme.typography.label, color = color)
    }
}

/** Terms checkbox: the box toggles, the two links open the legal documents. */
@Composable
internal fun TermsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val service = stringResource(R.string.register_terms_service)
    val privacy = stringResource(R.string.register_terms_privacy)
    val template = stringResource(R.string.register_terms, "\u0000", "\u0001")
    val linkStyle = TextLinkStyles(
        SpanStyle(fontWeight = FontWeight.Bold, color = colors.primary, textDecoration = TextDecoration.Underline),
    )
    val text = buildAnnotatedString {
        template.forEach { char ->
            when (char) {
                '\u0000' -> withLink(LinkAnnotation.Clickable("terms", linkStyle) { onTermsClick() }) { append(service) }
                '\u0001' -> withLink(LinkAnnotation.Clickable("privacy", linkStyle) { onPrivacyClick() }) { append(privacy) }
                else -> append(char)
            }
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = AppTheme.spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        AppCheckbox(
            checked = checked,
            size = CheckboxSize.Small,
            modifier = Modifier
                .padding(top = AppTheme.spacing.xxxs)
                .clip(AppTheme.shapes.tiny)
                .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange),
        )
        Text(text, style = AppTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}

/** "¿Ya tienes una cuenta? Inicia sesión aquí ›" */
@Composable
internal fun SignInLink(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.register_has_account), style = AppTheme.typography.body, color = colors.onSurfaceVariant)
        Row(
            modifier = Modifier
                .clip(AppTheme.shapes.extraSmall)
                .clickable(role = Role.Button, onClick = onClick),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.register_sign_in), style = AppTheme.typography.subtitle, color = colors.primary)
            AppIcon(AppIcons.ChevronRight, tint = colors.primary)
        }
    }
}
