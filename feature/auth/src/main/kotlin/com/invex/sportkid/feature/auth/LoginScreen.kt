package com.invex.sportkid.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.sportkid.core.designsystem.component.SkDetailTopBar
import com.invex.sportkid.core.designsystem.component.SkElevatedButton
import com.invex.sportkid.core.designsystem.component.SkPill
import com.invex.sportkid.core.designsystem.component.SkProfileButton
import com.invex.sportkid.core.designsystem.component.SkTextButton
import com.invex.sportkid.core.designsystem.preview.SkPreviews
import com.invex.sportkid.core.designsystem.theme.SkElevation
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.hero

private val ContentMaxWidth = 384.dp

/** P02. Iniciar sesión con Google, o explorar como invitado. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val spacing = SportKidTheme.spacing
    Scaffold(
        modifier = modifier,
        containerColor = colors.surface,
        topBar = {
            SkDetailTopBar(title = stringResource(R.string.login_title), onBack = onBack) {
                SkProfileButton(onClick = onProfileClick)
            }
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .navigationBarsPadding()
                .padding(horizontal = spacing.xl)
                .padding(top = spacing.lg, bottom = spacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BrandBadge(Modifier.padding(top = spacing.sm, bottom = spacing.lg))
            HeroCard(Modifier.padding(vertical = spacing.sm))

            Column(
                Modifier
                    .widthIn(max = ContentMaxWidth)
                    .padding(horizontal = spacing.sm, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.login_headline),
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
                Text(
                    stringResource(R.string.login_subtitle),
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .padding(top = spacing.sm),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                FlowRow(
                    Modifier.padding(top = spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    val padding = PaddingValues(horizontal = 10.dp, vertical = spacing.xs)
                    SkPill(
                        stringResource(R.string.login_pill_calendar),
                        contentColor = colors.primary,
                        icon = painterResource(R.drawable.ic_login_calendar),
                        contentPadding = padding,
                    )
                    SkPill(
                        stringResource(R.string.login_pill_medals),
                        containerColor = colors.tertiaryContainer,
                        contentColor = colors.onTertiaryContainer,
                        icon = painterResource(R.drawable.ic_login_medals),
                        contentPadding = padding,
                    )
                    SkPill(
                        stringResource(R.string.login_pill_finances),
                        containerColor = colors.secondaryContainer,
                        contentColor = colors.onSecondaryContainer,
                        icon = painterResource(R.drawable.ic_login_finances),
                        contentPadding = padding,
                    )
                }
            }

            Column(
                Modifier
                    .widthIn(max = ContentMaxWidth)
                    .padding(top = spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.lg),
            ) {
                SkElevatedButton(
                    text = stringResource(R.string.login_google),
                    onClick = onGoogleSignIn,
                    icon = painterResource(R.drawable.ic_login_google),
                    modifier = Modifier.fillMaxWidth(),
                )
                SkTextButton(
                    text = stringResource(R.string.login_guest),
                    onClick = onContinueAsGuest,
                    trailingIcon = painterResource(R.drawable.ic_login_arrow_forward),
                )
                LegalText(onOpenTerms, onOpenPrivacy)
            }
        }
    }
}

/** Badge de marca: medalla, "SPORTKID", punto y "Familia & Logros". */
@Composable
private fun BrandBadge(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val spacing = SportKidTheme.spacing
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = colors.surfaceContainerHigh.copy(alpha = 0.6f),
        shadowElevation = SkElevation.level1,
    ) {
        Row(
            Modifier.padding(horizontal = spacing.md, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(R.drawable.ic_login_brand), contentDescription = null, tint = colors.primary)
            Text(
                stringResource(R.string.login_brand).uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.35.sp),
                color = colors.primary,
            )
            Box(
                Modifier
                    .size(6.dp)
                    .background(colors.tertiary, CircleShape),
            )
            Text(
                stringResource(R.string.login_brand_tagline),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal),
                color = colors.onSurfaceVariant,
            )
        }
    }
}

/** Ilustración hero con degradado inferior y badge "¡Momento de Oro!". */
@Composable
private fun HeroCard(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier
            .widthIn(max = ContentMaxWidth)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.hero,
        color = colors.surfaceContainerLowest,
        shadowElevation = SkElevation.level2,
    ) {
        Box {
            Image(
                painterResource(R.drawable.img_login_hero),
                contentDescription = stringResource(R.string.login_hero_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(256.dp),
            )
            Box(
                Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.5f to Color.Transparent,
                            1f to colors.surfaceContainerLowest.copy(alpha = 0.3f),
                        ),
                    ),
            )
            SkPill(
                stringResource(R.string.login_hero_badge),
                containerColor = colors.surfaceContainerLowest.copy(alpha = 0.9f),
                contentColor = colors.onSurface,
                icon = painterResource(R.drawable.ic_login_celebration),
                iconTint = colors.tertiary,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = SportKidTheme.spacing.xs),
                spacing = 6.dp,
                shadowElevation = SkElevation.level1,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(SportKidTheme.spacing.md),
            )
        }
    }
}

@Composable
private fun LegalText(onOpenTerms: () -> Unit, onOpenPrivacy: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val linkStyle = TextLinkStyles(SpanStyle(textDecoration = TextDecoration.Underline))
    val terms = stringResource(R.string.login_legal_terms)
    val privacy = stringResource(R.string.login_legal_privacy)
    val text = buildAnnotatedString {
        append(stringResource(R.string.login_legal_prefix))
        withLink(LinkAnnotation.Clickable("terms", linkStyle) { onOpenTerms() }) { append(terms) }
        append(stringResource(R.string.login_legal_middle))
        withLink(LinkAnnotation.Clickable("privacy", linkStyle) { onOpenPrivacy() }) { append(privacy) }
        append(stringResource(R.string.login_legal_suffix))
    }
    Text(
        text,
        modifier = Modifier.padding(horizontal = 20.dp),
        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 19.5.sp),
        color = colors.outline,
        textAlign = TextAlign.Center,
    )
}

@SkPreviews
@Composable
private fun LoginPreview() = SportKidTheme {
    LoginScreen(onBack = {}, onGoogleSignIn = {}, onContinueAsGuest = {}, onOpenTerms = {}, onOpenPrivacy = {})
}
