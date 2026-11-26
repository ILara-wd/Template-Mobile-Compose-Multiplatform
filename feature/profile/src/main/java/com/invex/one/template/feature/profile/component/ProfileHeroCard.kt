package com.invex.one.template.feature.profile.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.invex.one.template.core.designsystem.atom.AppIcon
import com.invex.one.template.core.designsystem.atom.CategoryTag
import com.invex.one.template.core.designsystem.atom.IconCircle
import com.invex.one.template.core.designsystem.atom.IconTile
import com.invex.one.template.core.designsystem.atom.TagSize
import com.invex.one.template.core.designsystem.format.formatScore
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppAlpha
import com.invex.one.template.core.designsystem.token.AppSize
import com.invex.one.template.feature.profile.R
import com.invex.one.template.feature.profile.model.PlayerStats
import com.invex.one.template.feature.profile.model.ProfileCharacter

/** Hero card: avatar with level badge, gamertag, league, coins/streak and character picker. */
@Composable
internal fun ProfileHeroCard(
    stats: PlayerStats,
    gamertag: String,
    character: ProfileCharacter,
    onCharacterSelected: (ProfileCharacter) -> Unit,
    onChangePhoto: () -> Unit,
    editable: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    val shape = AppTheme.shapes.large
    val glowTop = colors.primary.copy(alpha = AppAlpha.Ambient)
    val glowBottom = colors.tertiaryContainer.copy(alpha = AppAlpha.TintSubtle)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .appShadow(AppTheme.elevation.md, shape, colors.shadow)
            .clip(shape)
            .background(colors.surfaceContainerLowest)
            // Soft ambient glows (Figma: blurred circles); radial gradients render on every API level.
            .drawBehind {
                val radius = 104.dp.toPx()
                drawCircle(Brush.radialGradient(listOf(glowTop, Color.Transparent), Offset(size.width - 16.dp.toPx(), 16.dp.toPx()), radius), radius, Offset(size.width - 16.dp.toPx(), 16.dp.toPx()))
                drawCircle(Brush.radialGradient(listOf(glowBottom, Color.Transparent), Offset(16.dp.toPx(), size.height - 16.dp.toPx()), radius), radius, Offset(16.dp.toPx(), size.height - 16.dp.toPx()))
            }
            .padding(spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProfileAvatar(stats, onChangePhoto, Modifier.padding(top = spacing.xxs, bottom = spacing.sm))
        val verifiedLabel = stringResource(R.string.profile_verified)
        Row(
            modifier = Modifier.padding(bottom = spacing.xxs),
            horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("@$gamertag", style = AppTheme.typography.titleLarge, color = colors.onSurface, maxLines = 1)
            IconCircle(
                AppIcons.VerifiedBadge,
                colors.secondaryContainer,
                AppSize.selectionBadge,
                Modifier.semantics { contentDescription = verifiedLabel },
            )
        }
        Row(
            modifier = Modifier.padding(bottom = spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryTag(stats.league, colors.tertiaryFixed, colors.onTertiaryFixed, size = TagSize.Small)
            Text("•", style = AppTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Text(
                stringResource(R.string.profile_member_since, stats.memberSince),
                style = AppTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        TelemetryPill(stats)
        CharacterPicker(character, onCharacterSelected, editable, Modifier.padding(top = spacing.lg))
    }
}

/** Large ringed avatar with the level badge and the change-photo button. */
@Composable
private fun ProfileAvatar(stats: PlayerStats, onChangePhoto: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val pill = AppTheme.shapes.pill
    Box(modifier) {
        Box(
            Modifier
                .size(AppSize.avatarLarge)
                .appShadow(AppTheme.elevation.md, pill, colors.shadow)
                .background(colors.surfaceContainerHigh, pill)
                .padding(AppTheme.spacing.xxs),
        ) {
            Image(
                painter = painterResource(stats.photo),
                contentDescription = stringResource(R.string.profile_photo_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(pill)
                    .background(colors.surfaceContainer),
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = -AppTheme.spacing.sm, y = AppTheme.spacing.sm)
                .appShadow(AppTheme.elevation.xs, pill, colors.shadow)
                .background(colors.inverseSurface, pill)
                .padding(horizontal = AppTheme.spacing.sm, vertical = AppTheme.spacing.xxxs),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(AppIcons.LevelBolt)
            Text(
                stringResource(R.string.profile_level_badge, stats.level, stats.rank).uppercase(),
                style = AppTheme.typography.overline,
                color = colors.inverseOnSurface,
            )
        }
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .offset(x = AppTheme.spacing.xxs, y = AppTheme.spacing.xxs)
                .appShadow(AppTheme.elevation.xs, pill, colors.primaryContainer.copy(alpha = AppAlpha.Ring))
                .size(AppSize.fieldIconButton)
                .clip(pill)
                .background(colors.primaryContainer)
                .clickable(role = Role.Button, onClickLabel = stringResource(R.string.profile_change_photo), onClick = onChangePhoto),
            contentAlignment = Alignment.Center,
        ) { AppIcon(AppIcons.Camera, contentDescription = stringResource(R.string.profile_change_photo)) }
    }
}

/** Coins and active streak side by side, split by a divider. */
@Composable
private fun TelemetryPill(stats: PlayerStats) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainerLow, AppTheme.shapes.small)
            .padding(AppTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TelemetryItem(
            AppIcons.CoinSquare,
            colors.tertiaryContainer.copy(alpha = AppAlpha.Tint),
            formatScore(stats.coins),
            stringResource(R.string.profile_coins),
        )
        Box(Modifier.width(1.dp).height(AppSize.dividerHeight).background(colors.surfaceContainerHighest))
        TelemetryItem(
            AppIcons.FlameRed,
            colors.errorContainer,
            pluralStringResource(R.plurals.profile_streak_days, stats.streakDays, stats.streakDays).uppercase(),
            stringResource(R.string.profile_streak_active),
        )
    }
}

/** One telemetry value: icon tile, number and caption. */
@Composable
private fun RowScope.TelemetryItem(@DrawableRes icon: Int, iconBackground: Color, value: String, label: String) {
    Row(
        modifier = Modifier
            .weight(1f)
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.xxs, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(icon, iconBackground, size = AppSize.iconSquare, shape = AppTheme.shapes.extraSmall, elevated = false)
        Column {
            Text(value, style = AppTheme.typography.numeric, color = AppTheme.colors.onSurface)
            Text(label.uppercase(), style = AppTheme.typography.label, color = AppTheme.colors.onSurfaceVariant)
        }
    }
}

/** "Elige tu personaje" section with the selectable character tiles. */
@Composable
private fun CharacterPicker(
    selected: ProfileCharacter,
    onSelected: (ProfileCharacter) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceContainer.copy(alpha = AppAlpha.Half), AppTheme.shapes.small)
            .padding(start = spacing.sm, end = spacing.sm, top = spacing.lg, bottom = spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.xxs),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.xxs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.profile_pick_character).uppercase(), style = AppTheme.typography.overline, color = colors.onSurfaceVariant)
            val count = ProfileCharacter.entries.size
            Text(pluralStringResource(R.plurals.profile_unlocked, count, count), style = AppTheme.typography.bodySmallStrong, color = colors.primary)
        }
        Row(Modifier.padding(top = spacing.xxs), horizontalArrangement = Arrangement.spacedBy(spacing.xxs)) {
            ProfileCharacter.entries.forEach { character ->
                CharacterOption(character, character == selected, enabled) { onSelected(character) }
            }
        }
    }
}

/** Selectable character tile; disabled outside edit mode. */
@Composable
private fun CharacterOption(character: ProfileCharacter, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.small
    val tile = when (character) {
        ProfileCharacter.Astro -> colors.primaryFixed.copy(alpha = AppAlpha.Ring)
        ProfileCharacter.Valeria -> colors.secondaryFixed
        ProfileCharacter.CiberBot -> colors.tertiaryFixed
        ProfileCharacter.Draco -> colors.surfaceContainerHighest
    }
    Column(
        modifier = Modifier
            .then(if (selected) Modifier.border(AppSize.avatarRing, colors.primary, shape) else Modifier)
            .clip(shape)
            .background(if (selected) colors.surfaceContainerLowest else colors.surfaceContainerLow)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(AppTheme.spacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconTile(character.icon, tile, size = AppSize.characterTile, shape = AppTheme.shapes.extraSmall, elevated = false)
        Text(
            character.displayName,
            style = AppTheme.typography.label,
            color = colors.onSurface,
            maxLines = 1,
            modifier = Modifier
                .padding(top = AppTheme.spacing.xxs)
                .widthIn(min = AppSize.characterTile),
            textAlign = TextAlign.Center,
        )
    }
}
