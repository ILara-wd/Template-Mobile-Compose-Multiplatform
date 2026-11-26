package com.invex.one.template.feature.home.component

import com.invex.one.template.core.designsystem.format.formatScore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import com.invex.one.template.core.designsystem.atom.AppProgressBar
import com.invex.one.template.core.designsystem.atom.IconTile
import com.invex.one.template.core.designsystem.atom.InlineBadge
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppAlpha
import com.invex.one.template.feature.home.R
import com.invex.one.template.feature.home.model.DailyChallenge

/** "Reto del día" banner: goal, XP reward and progress towards the target score. */
@Composable
internal fun DailyChallengeCard(
    challenge: DailyChallenge,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    val type = AppTheme.typography
    val shape = AppTheme.shapes.medium
    Row(
        modifier = modifier
            .fillMaxWidth()
            .appShadow(AppTheme.elevation.xs, shape, colors.shadow)
            .background(
                Brush.horizontalGradient(
                    listOf(colors.surfaceContainerHighest, colors.surfaceContainer, colors.surfaceContainerHigh),
                ),
                shape,
            )
            .padding(spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        IconTile(icon = AppIcons.Target, containerColor = colors.tertiaryContainer)
        Column(Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.home_daily_challenge).uppercase(),
                    style = type.overline,
                    color = colors.tertiary,
                )
                InlineBadge(
                    text = stringResource(R.string.home_xp_reward, challenge.xpReward),
                    containerColor = colors.primary.copy(alpha = AppAlpha.TintSubtle),
                    contentColor = colors.primary,
                )
            }
            Text(
                stringResource(R.string.home_daily_challenge_goal, formatScore(challenge.targetScore), challenge.gameName),
                style = type.subtitle,
                color = colors.onSurface,
                modifier = Modifier.padding(top = spacing.xxxs),
            )
            AppProgressBar(progress = challenge.progress, modifier = Modifier.padding(top = spacing.smd))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.xxs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(
                        R.string.home_score_progress,
                        formatScore(challenge.currentScore),
                        formatScore(challenge.targetScore),
                    ),
                    style = type.bodySmall,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.home_percent, challenge.percent),
                    style = type.label,
                    color = colors.primary,
                )
            }
        }
    }
}
