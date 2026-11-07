package com.invex.sportkid.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.invex.sportkid.core.designsystem.component.SkCard
import com.invex.sportkid.core.designsystem.component.SkIconCircle
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.full

/** P03. Bienvenida: crear un deportista nuevo o unirse con el QR de otro tutor. */
@Composable
fun WelcomeScreen(
    onCreateAthlete: () -> Unit,
    onScanQr: () -> Unit,
    onEnterCode: () -> Unit,
    onExploreDemo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val spacing = SportKidTheme.spacing
    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .verticalScroll(rememberScrollState())
            .safeDrawingPadding()
            .padding(start = spacing.lg, end = spacing.lg, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MedalBadge(Modifier.padding(top = spacing.sm, bottom = spacing.xl))

        SkPill(
            stringResource(R.string.welcome_first_steps),
            containerColor = colors.tertiaryContainer,
            contentColor = colors.onTertiaryContainer,
            icon = painterResource(R.drawable.ic_welcome_first_steps),
            iconTint = colors.tertiary,
            overline = true,
            contentPadding = PaddingValues(horizontal = spacing.md, vertical = spacing.xs),
            spacing = 6.dp,
            shadowElevation = SkElevation.level1,
        )
        Text(
            stringResource(R.string.welcome_title),
            modifier = Modifier.padding(top = spacing.md, bottom = spacing.sm),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onPrimaryContainer,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.welcome_subtitle),
            modifier = Modifier.widthIn(max = 320.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Column(
            Modifier.padding(top = spacing.xxl, bottom = spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
        ) {
            CreateAthleteCard(onCreateAthlete)
            JoinWithQrCard(onScanQr, onEnterCode)
        }

        DemoLink(onExploreDemo, Modifier.padding(top = spacing.sm))
        Row(
            Modifier.padding(top = spacing.md),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(R.drawable.ic_welcome_shield), contentDescription = null, tint = colors.secondary)
            Text(
                stringResource(R.string.welcome_security),
                style = MaterialTheme.typography.bodySmall,
                color = colors.outline,
            )
        }
    }
}

/** Medalla decorativa de 96dp con puntos de acento. */
@Composable
private fun MedalBadge(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(modifier.size(96.dp)) {
        Surface(
            Modifier.fillMaxSize(),
            shape = CircleShape,
            color = colors.surfaceContainerHigh,
            shadowElevation = SkElevation.level1,
        ) {
            Box(contentAlignment = Alignment.Center) {
                SkIconCircle(
                    icon = painterResource(R.drawable.ic_welcome_medal),
                    contentDescription = null,
                    size = 80.dp,
                    containerColor = colors.secondaryContainer,
                    iconTint = Color.Unspecified,
                )
            }
        }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
                .size(12.dp)
                .background(colors.tertiary, CircleShape),
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-4).dp, y = (-4).dp)
                .size(10.dp)
                .background(colors.primaryContainer, CircleShape),
        )
    }
}

@Composable
private fun CreateAthleteCard(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val spacing = SportKidTheme.spacing
    SkCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = colors.primary,
        onClick = onClick,
        contentPadding = PaddingValues(start = 24.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            SkIconCircle(
                icon = painterResource(R.drawable.ic_welcome_person_add),
                contentDescription = null,
                size = 48.dp,
                containerColor = colors.secondaryContainer,
                iconTint = colors.primary,
            )
            Box(Modifier.weight(1f), contentAlignment = Alignment.TopEnd) {
                SkPill(stringResource(R.string.welcome_create_badge), contentColor = colors.primary)
            }
        }
        Text(
            stringResource(R.string.welcome_create_title),
            modifier = Modifier.padding(top = spacing.md),
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface,
        )
        Text(
            stringResource(R.string.welcome_create_body),
            modifier = Modifier.padding(top = spacing.xs),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = spacing.xl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.welcome_create_cta),
                style = MaterialTheme.typography.titleSmall,
                color = colors.primary,
            )
            Icon(
                painterResource(R.drawable.ic_welcome_arrow_forward),
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.padding(start = 6.dp),
            )
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                SkIconCircle(
                    icon = painterResource(R.drawable.ic_welcome_chevron_right),
                    contentDescription = null,
                    size = 28.dp,
                    containerColor = colors.surfaceContainerLow,
                    iconTint = colors.secondary,
                )
            }
        }
    }
}

@Composable
private fun JoinWithQrCard(onScanQr: () -> Unit, onEnterCode: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val spacing = SportKidTheme.spacing
    SkCard(
        modifier = Modifier.fillMaxWidth(),
        accentColor = colors.tertiary,
        contentPadding = PaddingValues(start = 24.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            SkIconCircle(
                icon = painterResource(R.drawable.ic_welcome_qr_scan),
                contentDescription = null,
                size = 48.dp,
                containerColor = colors.tertiaryContainer,
                iconTint = colors.tertiary,
            )
            Box(Modifier.weight(1f), contentAlignment = Alignment.TopEnd) {
                // En Figma el texto usa #FFCEBD sobre #FFDBCF (ilegible). Se usa onTertiaryContainer.
                SkPill(
                    stringResource(R.string.welcome_qr_badge),
                    containerColor = colors.tertiaryContainer,
                    contentColor = colors.onTertiaryContainer,
                    icon = painterResource(R.drawable.ic_welcome_coparenting),
                    iconTint = colors.tertiary,
                )
            }
        }
        Text(
            stringResource(R.string.welcome_qr_title),
            modifier = Modifier.padding(top = spacing.md),
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface,
        )
        Text(
            stringResource(R.string.welcome_qr_body),
            modifier = Modifier.padding(top = spacing.xs),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
        )
        Row(
            Modifier.padding(top = spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(R.drawable.ic_welcome_check), contentDescription = null, tint = colors.tertiary)
            Text(
                stringResource(R.string.welcome_qr_note),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = spacing.xl),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onScanQr,
                shape = MaterialTheme.shapes.full,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.tertiary,
                    contentColor = colors.onTertiary,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = SkElevation.level1),
                contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.sm),
            ) {
                Icon(painterResource(R.drawable.ic_welcome_camera), contentDescription = null)
                Text(
                    stringResource(R.string.welcome_qr_cta),
                    modifier = Modifier.padding(start = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                IconButton(onClick = onEnterCode) {
                    SkIconCircle(
                        icon = painterResource(R.drawable.ic_welcome_qr),
                        contentDescription = stringResource(R.string.welcome_qr_enter_code),
                        size = 28.dp,
                        containerColor = colors.surfaceContainerLow,
                        iconTint = colors.secondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoLink(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.full,
        color = colors.surfaceContainer,
    ) {
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(SportKidTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(R.drawable.ic_welcome_eye), contentDescription = null, tint = colors.secondary)
            Text(
                stringResource(R.string.welcome_later),
                style = MaterialTheme.typography.labelMedium,
                color = colors.onSurface,
            )
            Text(
                stringResource(R.string.welcome_demo),
                style = MaterialTheme.typography.labelMedium.copy(textDecoration = TextDecoration.Underline),
                color = colors.primary,
            )
        }
    }
}

@SkPreviews
@Composable
private fun WelcomePreview() = SportKidTheme {
    WelcomeScreen(onCreateAthlete = {}, onScanQr = {}, onEnterCode = {}, onExploreDemo = {})
}
