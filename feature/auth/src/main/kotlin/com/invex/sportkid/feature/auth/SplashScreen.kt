package com.invex.sportkid.feature.auth

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.brand
import kotlinx.coroutines.delay

private const val SplashDurationMillis = 1_800L

/** P01. Splash: muestra la marca mientras se sincronizan los perfiles y luego continúa. */
@Composable
fun SplashScreen(
    versionName: String,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        delay(SplashDurationMillis)
        currentOnFinished()
    }
    SplashContent(versionName, modifier)
}

@Composable
internal fun SplashContent(versionName: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val spacing = SportKidTheme.spacing
    Box(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .splashAura(colors.primaryContainer, colors.secondaryContainer, colors.tertiaryContainer),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = spacing.xl, vertical = spacing.xxl + spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            SkPill(
                stringResource(R.string.splash_season),
                containerColor = colors.surfaceContainerHigh.copy(alpha = 0.8f),
                contentColor = colors.secondary,
                overline = true,
                contentPadding = PaddingValues(horizontal = spacing.md, vertical = spacing.xs),
                spacing = spacing.sm,
                shadowElevation = SkElevation.level1,
                leading = { Dot(colors.primary, 8.dp) },
                modifier = Modifier.padding(top = spacing.sm),
            )

            BrandBlock()

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SyncProgress()
                Text(
                    stringResource(R.string.splash_syncing),
                    modifier = Modifier.padding(top = 10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
                Row(
                    Modifier.padding(top = spacing.xl),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(shape = MaterialTheme.shapes.extraSmall, color = colors.surfaceContainerHigh) {
                        Text(
                            stringResource(R.string.splash_version, versionName),
                            modifier = Modifier.padding(horizontal = spacing.sm, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    Dot(colors.outlineVariant, 4.dp)
                    SkPill(
                        stringResource(R.string.splash_platform),
                        containerColor = Color.Transparent,
                        contentColor = colors.outline,
                        overline = true,
                        contentPadding = PaddingValues(0.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BrandBlock() {
    val colors = MaterialTheme.colorScheme
    val spacing = SportKidTheme.spacing
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Emblem()
        Row(
            Modifier.padding(top = spacing.xxl),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.splash_brand), style = MaterialTheme.typography.brand, color = colors.primary)
            Dot(colors.tertiary, 8.dp, Modifier.offset(y = spacing.xs))
        }
        Text(
            stringResource(R.string.splash_tagline),
            modifier = Modifier.padding(top = spacing.xs),
            style = MaterialTheme.typography.titleSmall,
            color = colors.secondary,
            textAlign = TextAlign.Center,
        )
        Row(
            Modifier.padding(top = spacing.xl),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            SkPill(
                stringResource(R.string.splash_pill_training),
                icon = painterResource(R.drawable.ic_splash_training),
            )
            SkPill(
                stringResource(R.string.splash_pill_goals),
                containerColor = colors.tertiaryContainer,
                contentColor = colors.onTertiaryContainer,
                icon = painterResource(R.drawable.ic_splash_goals),
            )
            SkPill(
                stringResource(R.string.splash_pill_agenda),
                containerColor = colors.secondaryContainer,
                contentColor = colors.onSecondaryContainer,
                icon = painterResource(R.drawable.ic_splash_agenda),
            )
        }
    }
}

/** Emblema circular de 128dp con insignias flotantes. El logo (96dp) está pendiente (README §8). */
@Composable
private fun Emblem() {
    val colors = MaterialTheme.colorScheme
    Box(
        Modifier
            .size(128.dp)
            .drawBehind {
                // Halo suave detrás del emblema (Figma: gradiente primary → tertiary con blur 24).
                drawCircle(
                    Brush.radialGradient(
                        listOf(colors.primary.copy(alpha = 0.2f), colors.tertiary.copy(alpha = 0.08f), Color.Transparent),
                        center = center,
                        radius = size.minDimension / 2 + 36.dp.toPx(),
                    ),
                    radius = size.minDimension / 2 + 36.dp.toPx(),
                )
            },
    ) {
        Surface(
            Modifier.fillMaxSize(),
            shape = CircleShape,
            color = colors.surfaceContainerLowest,
            shadowElevation = 12.dp,
        ) {
            Box(Modifier.padding(SportKidTheme.spacing.sm)) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(colors.surfaceContainerLow, CircleShape),
                )
            }
        }
        SkIconCircle(
            icon = painterResource(R.drawable.ic_splash_award),
            contentDescription = null,
            size = 32.dp,
            containerColor = colors.tertiary,
            iconTint = colors.tertiaryContainer,
            shadowElevation = SkElevation.level2,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp),
        )
        SkIconCircle(
            icon = painterResource(R.drawable.ic_splash_sports),
            contentDescription = null,
            size = 28.dp,
            containerColor = colors.primary,
            iconTint = colors.onPrimary,
            shadowElevation = SkElevation.level1,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-4).dp, y = 4.dp),
        )
    }
}

@Composable
private fun SyncProgress() {
    val description = stringResource(R.string.splash_syncing)
    val rotation by rememberInfiniteTransition(label = "sync").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1_000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation",
    )
    Surface(
        Modifier
            .size(40.dp)
            .semantics { contentDescription = description },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = SkElevation.level1,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painterResource(R.drawable.ic_splash_progress),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.rotate(rotation),
            )
        }
    }
}

@Composable
private fun Dot(color: Color, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .background(color, CircleShape),
    )
}

/** Auras difusas del fondo. Se dibujan con gradientes radiales (blur real requiere Android 12+). */
private fun Modifier.splashAura(top: Color, middle: Color, bottom: Color) = drawBehind {
    fun DrawScope.aura(color: Color, alpha: Float, center: Offset, radius: Float) = drawCircle(
        Brush.radialGradient(listOf(color.copy(alpha = alpha), Color.Transparent), center, radius),
        radius = radius,
        center = center,
    )
    val dp = 1.dp.toPx()
    aura(top, 0.6f, Offset(size.width / 2, 96 * dp), 256 * dp)
    aura(middle, 0.4f, Offset(size.width / 2, 373 * dp), 200 * dp)
    aura(bottom, 0.3f, Offset(size.width / 2, size.height - 64 * dp), 208 * dp)
}

@SkPreviews
@Composable
private fun SplashPreview() = SportKidTheme {
    SplashContent(versionName = "1.0")
}
