package com.invex.one.template.core.designsystem.molecule

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.invex.one.template.core.designsystem.atom.AppIcon
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.token.AppSize

/**
 * Labelled single-line text field: label row (with optional trailing action) above a
 * 52dp filled input with a leading icon and an optional trailing slot.
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    @DrawableRes leadingIcon: Int,
    modifier: Modifier = Modifier,
    labelAction: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    val textStyle = AppTheme.typography.body
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = AppTheme.typography.button, color = colors.onSurface)
            labelAction?.invoke()
        }
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
                .semantics { contentDescription = label },
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AppSize.fieldHeight)
                        .background(colors.surfaceContainerLow, AppTheme.shapes.small),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    AppIcon(leadingIcon, Modifier.padding(start = spacing.mdLg))
                    Box(
                        Modifier.padding(
                            start = AppSize.fieldTextInset,
                            end = if (trailing != null) AppSize.fieldTrailingInset else spacing.lg,
                        ),
                    ) {
                        if (value.isEmpty()) {
                            Text(placeholder, style = textStyle, color = colors.outline, maxLines = 1)
                        }
                        innerTextField()
                    }
                    if (trailing != null) {
                        Box(Modifier.align(Alignment.CenterEnd).padding(end = spacing.sm)) { trailing() }
                    }
                }
            },
        )
    }
}

@Preview
@Composable
private fun AppTextFieldPreview() {
    AppTheme {
        AppTextField(
            value = "",
            onValueChange = {},
            label = "Correo o Nickname",
            placeholder = "ej. arcade_master@gmail.com",
            leadingIcon = AppIcons.At,
            modifier = Modifier.padding(AppTheme.spacing.lg),
        )
    }
}
