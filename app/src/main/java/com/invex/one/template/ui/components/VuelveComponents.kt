package com.invex.one.template.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.invex.one.template.R
import com.invex.one.template.ui.theme.VuelveBackground
import com.invex.one.template.ui.theme.VuelveOnSurface
import com.invex.one.template.ui.theme.VuelveOutline
import com.invex.one.template.ui.theme.VuelvePrimary
import com.invex.one.template.ui.theme.VuelveSurfaceContainerLow

val VuelveHeaderHeight = 56.dp

private val SubtleShadowColor = Color.Black.copy(alpha = 0.05f)

/** Figma `drop-shadow(0 1px 1px rgba(0,0,0,.05))`, used on most cards, chips and fields. */
fun Modifier.subtleShadow(shape: Shape): Modifier =
    shadow(elevation = 1.dp, shape = shape, ambientColor = SubtleShadowColor, spotColor = SubtleShadowColor)

/**
 * Screen frame for the auth flow: lavender background, scrolling content and a translucent
 * top bar drawn over it. Content starts below the bar and respects system bars and the IME.
 */
@Composable
fun VuelveFlowScaffold(
    title: String,
    onBack: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VuelveBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(top = VuelveHeaderHeight),
            content = content,
        )
        VuelveFlowTopBar(title = title, onBack = onBack, onProfile = onProfile)
    }
}

/** O2 — Top bar for detail / auth flows: back, blue dot + title, round profile button. */
@Composable
fun VuelveFlowTopBar(
    title: String,
    onBack: () -> Unit,
    onProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.04f),
            )
            .background(VuelveBackground.copy(alpha = 0.8f))
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(VuelveHeaderHeight)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(modifier = Modifier.width(36.dp)) {
                Box(
                    modifier = Modifier
                        .offset(x = (-8).dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onBack, role = Role.Button),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_vuelve_arrow_back),
                        contentDescription = stringResource(R.string.vuelve_back),
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(VuelvePrimary, CircleShape),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = VuelveOnSurface,
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(VuelvePrimary)
                    .clickable(onClick = onProfile, role = Role.Button),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_vuelve_person),
                    contentDescription = stringResource(R.string.vuelve_profile),
                )
            }
        }
    }
}

/** Full-width 48dp button with 12dp corners (primary, WhatsApp, tonal variants). */
@Composable
fun VuelveButton(
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 10.dp,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor, contentColor = contentColor),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            content = content,
        )
    }
}

/**
 * Filled 48dp single-line input with a placeholder and optional leading / trailing slots.
 * Shape, padding and slot spacing are parameters because each Figma screen tunes them.
 */
@Composable
fun VuelveTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    withShadow: Boolean = false,
    startPadding: Dp = 12.dp,
    endPadding: Dp = 12.dp,
    leadingSpacing: Dp = 8.dp,
    trailingSpacing: Dp = 8.dp,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val inputStyle = textStyle.copy(color = VuelveOnSurface)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = inputStyle,
        cursorBrush = SolidColor(VuelvePrimary),
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        modifier = modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .then(if (withShadow) Modifier.subtleShadow(shape) else Modifier)
                    .background(VuelveSurfaceContainerLow, shape)
                    .padding(start = startPadding, end = endPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leading != null) {
                    leading()
                    Spacer(Modifier.width(leadingSpacing))
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = inputStyle, color = VuelveOutline, maxLines = 1)
                    }
                    innerTextField()
                }
                if (trailing != null) {
                    Spacer(Modifier.width(trailingSpacing))
                    trailing()
                }
            }
        },
    )
}
