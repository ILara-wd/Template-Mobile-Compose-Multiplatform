package com.invex.one.template.core.designsystem.molecule

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.invex.one.template.core.designsystem.atom.AppIcon
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.token.AppSize

/** Label configuration shared by [AppTextField] and [AppPickerField]. */
data class FieldLabel(
    val text: String,
    @param:DrawableRes val icon: Int? = null,
    /** `true` = Plus Jakarta Bold 16 (registration), `false` = Space Grotesk 14 (login). */
    val prominent: Boolean = false,
)

/** Helper line under the input, optionally with a leading icon. */
data class FieldSupport(
    val text: String,
    @param:DrawableRes val icon: Int? = null,
)

/**
 * Labelled single-line text field: label row (optional icon + trailing action), a filled
 * input with leading icon and optional trailing slot, and an optional supporting line.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: FieldLabel,
    placeholder: String,
    @DrawableRes leadingIcon: Int,
    modifier: Modifier = Modifier,
    labelAction: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    support: FieldSupport? = null,
    height: Dp = AppSize.fieldHeight,
    textStyle: TextStyle = AppTheme.typography.body,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = AppTheme.colors
    FieldLayout(label, labelAction, support, modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = textStyle.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.primary),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = label.text },
            decorationBox = { innerTextField ->
                FieldBox(leadingIcon, height, hasTrailing = trailing != null, trailing = trailing) {
                    if (value.isEmpty()) {
                        Text(placeholder, style = AppTheme.typography.body, color = colors.outline, maxLines = 1)
                    }
                    innerTextField()
                }
            },
        )
    }
}

/** Read-only field that looks like [AppTextField] and opens a picker (date, list…) on tap. */
@Composable
fun AppPickerField(
    value: String?,
    placeholder: String,
    onClick: () -> Unit,
    label: FieldLabel,
    @DrawableRes leadingIcon: Int,
    modifier: Modifier = Modifier,
    support: FieldSupport? = null,
    height: Dp = AppSize.fieldHeight,
) {
    val colors = AppTheme.colors
    FieldLayout(label, null, support, modifier) {
        Box(
            Modifier
                .clip(AppTheme.shapes.small)
                .clickable(role = Role.Button, onClickLabel = label.text, onClick = onClick),
        ) {
            FieldBox(leadingIcon, height, hasTrailing = false, trailing = null) {
                Text(
                    value ?: placeholder,
                    style = AppTheme.typography.body,
                    color = if (value == null) colors.outline else colors.onSurface,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun FieldLayout(
    label: FieldLabel,
    labelAction: (@Composable () -> Unit)?,
    support: FieldSupport?,
    modifier: Modifier,
    input: @Composable () -> Unit,
) {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs), verticalAlignment = Alignment.CenterVertically) {
                label.icon?.let { AppIcon(it) }
                Text(
                    label.text,
                    style = if (label.prominent) AppTheme.typography.subtitle else AppTheme.typography.button,
                    color = colors.onSurface,
                )
            }
            labelAction?.invoke()
        }
        input()
        support?.let {
            Row(
                modifier = Modifier.padding(start = spacing.xxs),
                horizontalArrangement = Arrangement.spacedBy(spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                it.icon?.let { icon -> AppIcon(icon) }
                Text(it.text, style = AppTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun FieldBox(
    @DrawableRes leadingIcon: Int,
    height: Dp,
    hasTrailing: Boolean,
    trailing: (@Composable () -> Unit)?,
    content: @Composable () -> Unit,
) {
    val spacing = AppTheme.spacing
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(AppTheme.colors.surfaceContainerLow, AppTheme.shapes.small),
        contentAlignment = Alignment.CenterStart,
    ) {
        AppIcon(leadingIcon, Modifier.padding(start = spacing.mdLg))
        Box(
            Modifier.padding(
                start = AppSize.fieldTextInset,
                end = if (hasTrailing) AppSize.fieldTrailingInset else spacing.lg,
            ),
        ) { content() }
        if (trailing != null) {
            Box(Modifier.align(Alignment.CenterEnd).padding(end = spacing.sm)) { trailing() }
        }
    }
}

@Preview
@Composable
private fun AppTextFieldPreview() {
    AppTheme {
        Column(Modifier.padding(AppTheme.spacing.lg), verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)) {
            AppTextField(
                value = "",
                onValueChange = {},
                label = FieldLabel("Correo o Nickname"),
                placeholder = "ej. arcade_master@gmail.com",
                leadingIcon = AppIcons.At,
            )
            AppPickerField(
                value = null,
                placeholder = "dd/mm/aaaa",
                onClick = {},
                label = FieldLabel("Fecha de Nacimiento", AppIcons.Cake, prominent = true),
                leadingIcon = AppIcons.Calendar,
                support = FieldSupport("Para premios especiales de cumpleaños.", AppIcons.Gift),
                height = AppSize.fieldHeightCompact,
            )
        }
    }
}
