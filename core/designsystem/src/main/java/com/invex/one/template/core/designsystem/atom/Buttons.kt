package com.invex.one.template.core.designsystem.atom

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.theme.depthShadow
import com.invex.one.template.core.designsystem.token.AppAlpha
import com.invex.one.template.core.designsystem.token.AppSize

/**
 * `Primary`: dark green, white label, soft shadow (home cards).
 * `Accent`: bright green, dark label, hard 3dp depth shadow (game catalog).
 */
enum class ButtonVariant { Primary, Accent }

/** `Medium`: 44dp pill, button text. `Large`: 56dp, radius 12, title text (main CTAs). */
enum class ButtonSize { Medium, Large }

/** Pill button (44dp) filling the available width, with optional leading/trailing icons. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    size: ButtonSize = ButtonSize.Medium,
    @DrawableRes leadingIcon: Int? = null,
    @DrawableRes trailingIcon: Int? = null,
    /** Overrides the size-derived label style (e.g. compact label on a large CTA). */
    textStyle: TextStyle? = null,
) {
    val colors = AppTheme.colors
    // Large primary CTAs use a 12dp radius; accent CTAs stay pill-shaped at every size.
    val shape = if (size == ButtonSize.Large && variant == ButtonVariant.Primary) AppTheme.shapes.small else AppTheme.shapes.pill
    val container: Color
    val content: Color
    val depth: Modifier
    val gap = when (variant) {
        ButtonVariant.Primary -> {
            container = colors.primary
            content = colors.onPrimary
            val elevation = if (size == ButtonSize.Large) AppTheme.elevation.md else AppTheme.elevation.xs
            depth = Modifier.appShadow(elevation, shape, colors.shadow)
            AppTheme.spacing.sm
        }
        ButtonVariant.Accent -> {
            container = colors.primaryContainer
            content = colors.onPrimaryContainer
            depth = if (size == ButtonSize.Large) {
                Modifier.appShadow(AppTheme.elevation.md, shape, colors.primaryContainer.copy(alpha = AppAlpha.Glow))
            } else {
                Modifier.depthShadow(AppSize.buttonDepth, colors.primary.copy(alpha = AppAlpha.Depth), shape)
            }
            if (size == ButtonSize.Large) AppTheme.spacing.sm else AppTheme.spacing.xs
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(if (size == ButtonSize.Large) AppSize.buttonHeightLarge else AppSize.buttonHeight)
            .then(depth)
            .clip(shape)
            .background(container)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon?.let { AppIcon(it, tint = content) }
        Text(
            text = text,
            style = textStyle ?: when {
                size == ButtonSize.Medium -> AppTheme.typography.button
                variant == ButtonVariant.Accent -> AppTheme.typography.buttonLarge
                else -> AppTheme.typography.title
            },
            color = content,
        )
        trailingIcon?.let { AppIcon(it, tint = content) }
    }
}

/** Low-emphasis text action with a trailing chevron ("Ver historial ›"). */
@Composable
fun TextLinkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(AppTheme.shapes.small)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = AppTheme.spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text, style = AppTheme.typography.label, color = AppTheme.colors.secondary)
        AppIcon(AppIcons.ChevronRight, tint = AppTheme.colors.secondary)
    }
}

@Preview
@Composable
private fun ButtonsPreview() {
    AppTheme {
        Column(Modifier.padding(AppTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)) {
            PrimaryButton("Jugar", onClick = {}, trailingIcon = AppIcons.Play)
            PrimaryButton("Jugar", onClick = {}, variant = ButtonVariant.Accent, leadingIcon = AppIcons.PlayFilled)
            TextLinkButton("Ver historial", onClick = {})
        }
    }
}
