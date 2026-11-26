package com.invex.one.template.feature.auth.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.invex.one.template.core.designsystem.atom.AppIcon
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppSize
import com.invex.one.template.feature.auth.R

/** "🔥 TUS RÉCORDS TE ESPERAN • 5 Días en Racha" pill. */
@Composable
internal fun StreakBanner(streakDays: Int, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.pill
    Row(
        modifier = modifier
            .appShadow(AppTheme.elevation.xs, shape, colors.shadow)
            .background(colors.surfaceContainerHigh, shape)
            .padding(horizontal = AppTheme.spacing.lg, vertical = AppTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(AppIcons.FlameSmall)
        Text(stringResource(R.string.login_records_waiting).uppercase(), style = AppTheme.typography.overline, color = colors.onSurface)
        Box(Modifier.size(AppSize.statusDotSmall).background(colors.outlineVariant, shape))
        Text(
            pluralStringResource(R.plurals.login_streak, streakDays, streakDays),
            style = AppTheme.typography.label,
            color = colors.primary,
        )
    }
}

/** Arcade badge (80dp tile + star) with greeting and value proposition. */
@Composable
internal fun LoginHero(gamesCount: Int, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.padding(bottom = spacing.lg)) {
            val tileShape = AppTheme.shapes.medium
            Box(
                Modifier
                    .size(AppSize.heroTile)
                    .appShadow(AppTheme.elevation.lg, tileShape, colors.shadow)
                    .background(colors.primaryContainer, tileShape),
                contentAlignment = Alignment.Center,
            ) { AppIcon(AppIcons.GamepadLarge) }
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = spacing.xxs, y = spacing.xxs)
                    .appShadow(AppTheme.elevation.md, AppTheme.shapes.pill, colors.shadow)
                    .background(colors.tertiaryContainer, AppTheme.shapes.pill)
                    .padding(spacing.xxs),
            ) { AppIcon(AppIcons.StarBadge) }
        }
        AuthHeadline(
            title = stringResource(R.string.login_title),
            subtitle = stringResource(R.string.login_subtitle, gamesCount),
        )
    }
}

/** Centered display title + readable-width subtitle used by the auth screens. */
@Composable
internal fun AuthHeadline(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            title,
            style = AppTheme.typography.display,
            color = AppTheme.colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            subtitle,
            style = AppTheme.typography.body,
            color = AppTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = AppTheme.spacing.xxs)
                .widthIn(max = AppSize.readableWidth),
        )
    }
}
