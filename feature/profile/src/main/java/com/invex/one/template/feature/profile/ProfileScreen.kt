package com.invex.one.template.feature.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.one.template.core.designsystem.atom.AppIcon
import com.invex.one.template.core.designsystem.atom.ButtonSize
import com.invex.one.template.core.designsystem.atom.ButtonVariant
import com.invex.one.template.core.designsystem.atom.PrimaryButton
import com.invex.one.template.core.designsystem.atom.SecondaryButton
import com.invex.one.template.core.designsystem.atom.SwitchSize
import com.invex.one.template.core.designsystem.format.formatScore
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.molecule.SettingsNavigationRow
import com.invex.one.template.core.designsystem.molecule.SettingsRowStyle
import com.invex.one.template.core.designsystem.molecule.SettingsSwitchRow
import com.invex.one.template.core.designsystem.molecule.StatTile
import com.invex.one.template.core.designsystem.molecule.StatTileStyle
import com.invex.one.template.core.designsystem.organism.BrandTopBar
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppSize
import com.invex.one.template.feature.profile.component.PersonalDataCallbacks
import com.invex.one.template.feature.profile.component.PersonalDataCard
import com.invex.one.template.feature.profile.component.ProfileHeroCard
import com.invex.one.template.feature.profile.model.PlayerStats
import com.invex.one.template.feature.profile.model.ProfileCharacter
import com.invex.one.template.feature.profile.model.ProfileUiState
import com.invex.one.template.feature.profile.model.ServerRegion

/** Navigation out of the profile screen. */
data class ProfileActions(
    val onBack: () -> Unit,
    val onLogout: () -> Unit,
    val onChangePassword: () -> Unit,
    val onChangePhoto: () -> Unit,
    val onUpdateEmail: () -> Unit,
)

/** Form events handled by the ViewModel. */
data class ProfileCallbacks(
    val onEditModeChange: (Boolean) -> Unit,
    val onCharacterSelected: (ProfileCharacter) -> Unit,
    val onGamertagChange: (String) -> Unit,
    val onFullNameChange: (String) -> Unit,
    val onEmailChange: (String) -> Unit,
    val onBirthdateSelected: (Long?) -> Unit,
    val onRegionSelected: (ServerRegion) -> Unit,
    val onPublicProfileChange: (Boolean) -> Unit,
    val onStreakAlertsChange: (Boolean) -> Unit,
    val onSave: () -> Unit,
    val onDiscard: () -> Unit,
)

/** Stateful entry point: wires the [ProfileViewModel] to the stateless [ProfileScreen]. */
@Composable
fun ProfileRoute(
    actions: ProfileActions,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(onBack = actions.onBack)
    ProfileScreen(
        state = state,
        callbacks = ProfileCallbacks(
            onEditModeChange = viewModel::onEditModeChange,
            onCharacterSelected = viewModel::onCharacterSelected,
            onGamertagChange = viewModel::onGamertagChange,
            onFullNameChange = viewModel::onFullNameChange,
            onEmailChange = viewModel::onEmailChange,
            onBirthdateSelected = viewModel::onBirthdateSelected,
            onRegionSelected = viewModel::onRegionSelected,
            onPublicProfileChange = viewModel::onPublicProfileChange,
            onStreakAlertsChange = viewModel::onStreakAlertsChange,
            onSave = viewModel::onSave,
            onDiscard = viewModel::onDiscard,
        ),
        actions = actions,
        modifier = modifier,
    )
}

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    callbacks: ProfileCallbacks,
    actions: ProfileActions,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    val section = Modifier.padding(start = spacing.lg, end = spacing.lg, top = spacing.lg)
    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            BrandTopBar(
                brandName = stringResource(R.string.profile_brand),
                backContentDescription = stringResource(R.string.profile_back),
                onBack = actions.onBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = spacing.xl + spacing.lg),
        ) {
            ProfileHeroCard(
                stats = state.stats,
                gamertag = state.savedForm.gamertag,
                character = state.form.character,
                onCharacterSelected = callbacks.onCharacterSelected,
                onChangePhoto = actions.onChangePhoto,
                editable = state.editMode,
                modifier = section,
            )
            SettingsSwitchRow(
                icon = AppIcons.Tune,
                title = stringResource(R.string.profile_edit_mode),
                subtitle = stringResource(R.string.profile_edit_mode_supporting),
                checked = state.editMode,
                onCheckedChange = callbacks.onEditModeChange,
                style = SettingsRowStyle(
                    containerColor = colors.surfaceContainer,
                    containerShape = AppTheme.shapes.large,
                    iconContainerColor = colors.surfaceContainerLowest,
                    iconContainerShape = AppTheme.shapes.small,
                    switchSize = SwitchSize.Regular,
                ),
                modifier = section.appShadow(AppTheme.elevation.xs, AppTheme.shapes.large, colors.shadow),
            )
            PerformanceSection(state.stats, section)
            PersonalDataCard(
                form = state.form,
                gamertagAvailable = state.gamertagAvailable,
                editable = state.editMode,
                callbacks = PersonalDataCallbacks(
                    onGamertagChange = callbacks.onGamertagChange,
                    onFullNameChange = callbacks.onFullNameChange,
                    onEmailChange = callbacks.onEmailChange,
                    onUpdateEmail = actions.onUpdateEmail,
                    onBirthdateSelected = callbacks.onBirthdateSelected,
                    onRegionSelected = callbacks.onRegionSelected,
                ),
                modifier = section,
            )
            SecurityCard(state, callbacks, actions, section)
            Column(
                modifier = Modifier.padding(start = spacing.lg, end = spacing.lg, top = spacing.xl),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                PrimaryButton(
                    text = stringResource(R.string.profile_save),
                    onClick = callbacks.onSave,
                    variant = ButtonVariant.Accent,
                    size = ButtonSize.Large,
                    leadingIcon = AppIcons.Save,
                    textStyle = AppTheme.typography.button,
                )
                SecondaryButton(
                    text = stringResource(R.string.profile_discard),
                    leadingIcon = null,
                    onClick = callbacks.onDiscard,
                    shape = AppTheme.shapes.pill,
                    height = AppSize.fieldHeightCompact,
                )
                Row(
                    modifier = Modifier
                        .padding(top = spacing.xxs)
                        .align(Alignment.CenterHorizontally)
                        .clip(AppTheme.shapes.extraSmall)
                        .clickable(role = Role.Button, onClick = actions.onLogout)
                        .padding(horizontal = spacing.lg, vertical = spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppIcon(AppIcons.Logout)
                    Text(stringResource(R.string.profile_logout), style = AppTheme.typography.bodySmall, color = colors.error)
                }
            }
        }
    }
}

@Composable
private fun PerformanceSection(stats: PlayerStats, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    Column(modifier, verticalArrangement = Arrangement.spacedBy(spacing.xxs)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.xxs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.profile_performance).uppercase(), style = AppTheme.typography.overline, color = colors.onSurfaceVariant)
            Text(stringResource(R.string.profile_season, stats.season), style = AppTheme.typography.label, color = colors.primary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            StatTile(
                AppIcons.GamepadMetric, colors.secondaryFixed, formatScore(stats.gamesPlayed),
                stringResource(R.string.profile_metric_games), Modifier.weight(1f), StatTileStyle.Compact,
            )
            StatTile(
                AppIcons.TrophyMetric, colors.tertiaryFixed, formatScore(stats.bestScore),
                stringResource(R.string.profile_metric_record, stats.bestScoreGame), Modifier.weight(1f), StatTileStyle.Compact,
            )
            StatTile(
                AppIcons.StarMetric, colors.primaryFixed,
                stringResource(R.string.profile_fraction, stats.achievements, stats.achievementsTotal),
                stringResource(R.string.profile_metric_achievements), Modifier.weight(1f), StatTileStyle.Compact,
            )
        }
    }
}

@Composable
private fun SecurityCard(state: ProfileUiState, callbacks: ProfileCallbacks, actions: ProfileActions, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.large
    Column(
        modifier = modifier
            .fillMaxWidth()
            .appShadow(AppTheme.elevation.md, shape, colors.shadow)
            .background(colors.surfaceContainerLowest, shape)
            .padding(AppTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm),
    ) {
        Text(
            stringResource(R.string.profile_security_title),
            style = AppTheme.typography.title,
            color = colors.onSurface,
            modifier = Modifier.padding(bottom = AppTheme.spacing.xxs),
        )
        SettingsNavigationRow(
            AppIcons.LockReset,
            stringResource(R.string.profile_change_password),
            stringResource(R.string.profile_change_password_supporting),
            onClick = actions.onChangePassword,
        )
        SettingsSwitchRow(
            AppIcons.Visibility,
            stringResource(R.string.profile_public),
            stringResource(R.string.profile_public_supporting),
            checked = state.form.publicProfile,
            onCheckedChange = callbacks.onPublicProfileChange,
        )
        SettingsSwitchRow(
            AppIcons.Bell,
            stringResource(R.string.profile_alerts),
            stringResource(R.string.profile_alerts_supporting),
            checked = state.form.streakAlerts,
            onCheckedChange = callbacks.onStreakAlertsChange,
        )
    }
}

@Preview(widthDp = 390, heightDp = 2000)
@Composable
private fun ProfileScreenPreview() {
    AppTheme {
        ProfileScreen(
            state = SampleProfile.state,
            callbacks = ProfileCallbacks({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}),
            actions = ProfileActions({}, {}, {}, {}, {}),
        )
    }
}
