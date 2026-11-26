package com.invex.one.template.feature.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.one.template.core.designsystem.atom.ButtonSize
import com.invex.one.template.core.designsystem.atom.ButtonVariant
import com.invex.one.template.core.designsystem.atom.InlineIconButton
import com.invex.one.template.core.designsystem.atom.PrimaryButton
import com.invex.one.template.core.designsystem.atom.SecondaryButton
import com.invex.one.template.core.designsystem.atom.StrengthMeter
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.molecule.AppDatePickerDialog
import com.invex.one.template.core.designsystem.molecule.AppPickerField
import com.invex.one.template.core.designsystem.molecule.formatUtcDate
import com.invex.one.template.core.designsystem.molecule.AppTextField
import com.invex.one.template.core.designsystem.molecule.FieldLabel
import com.invex.one.template.core.designsystem.molecule.FieldSupport
import com.invex.one.template.core.designsystem.molecule.LabeledDivider
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.core.designsystem.token.AppSize
import com.invex.one.template.feature.auth.component.AuthHeadline
import com.invex.one.template.feature.auth.component.AuthTopBar
import com.invex.one.template.feature.auth.component.AvailableBadge
import com.invex.one.template.feature.auth.component.AvatarPickerCard
import com.invex.one.template.feature.auth.component.FieldStatus
import com.invex.one.template.feature.auth.component.SignInLink
import com.invex.one.template.feature.auth.component.TermsCheckbox
import com.invex.one.template.feature.auth.component.WelcomeBonusBanner

/** Callbacks for every exit / navigation out of the registration screen. */
data class RegisterActions(
    val onBack: () -> Unit,
    val onRegister: () -> Unit,
    val onGoogle: () -> Unit,
    val onSignIn: () -> Unit,
    val onTermsClick: () -> Unit,
    val onPrivacyClick: () -> Unit,
)

/** Stateful entry point: wires the [RegisterViewModel] to the stateless [RegisterScreen]. */
@Composable
fun RegisterRoute(
    actions: RegisterActions,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(onBack = actions.onBack)
    RegisterScreen(
        state = state,
        callbacks = RegisterCallbacks(
            onAvatarSelected = viewModel::onAvatarSelected,
            onGamertagChange = viewModel::onGamertagChange,
            onEmailChange = viewModel::onEmailChange,
            onBirthdateSelected = viewModel::onBirthdateSelected,
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onTogglePassword = viewModel::onTogglePassword,
            onToggleConfirm = viewModel::onToggleConfirm,
            onTermsChange = viewModel::onTermsChange,
        ),
        actions = actions,
        modifier = modifier,
    )
}

/** Form events handled by the ViewModel. */
data class RegisterCallbacks(
    val onAvatarSelected: (GamerAvatar) -> Unit,
    val onGamertagChange: (String) -> Unit,
    val onEmailChange: (String) -> Unit,
    val onBirthdateSelected: (Long?) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onConfirmPasswordChange: (String) -> Unit,
    val onTogglePassword: () -> Unit,
    val onToggleConfirm: () -> Unit,
    val onTermsChange: (Boolean) -> Unit,
)

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    callbacks: RegisterCallbacks,
    actions: RegisterActions,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    val focusManager = LocalFocusManager.current
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val nextField = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })

    Scaffold(
        modifier = modifier,
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0),
        topBar = { AuthTopBar(onBack = actions.onBack) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding()
                .padding(PaddingValues(start = spacing.lg, end = spacing.lg, bottom = spacing.xxl)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WelcomeBonusBanner(state.welcomeBonus, Modifier.padding(top = spacing.lg, bottom = spacing.sm))
            AuthHeadline(
                title = stringResource(R.string.register_title),
                subtitle = stringResource(R.string.register_subtitle),
                modifier = Modifier.padding(bottom = spacing.xl),
            )
            AvatarPickerCard(state.avatar, callbacks.onAvatarSelected, Modifier.padding(bottom = spacing.lg))

            val cardShape = AppTheme.shapes.large
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .appShadow(AppTheme.elevation.xs, cardShape, colors.shadow)
                    .background(colors.surfaceContainerLowest, cardShape)
                    .padding(spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.lg),
            ) {
                AppTextField(
                    value = state.gamertag,
                    onValueChange = callbacks.onGamertagChange,
                    label = FieldLabel(stringResource(R.string.register_gamertag_label), AppIcons.GamepadLabel, prominent = true),
                    placeholder = stringResource(R.string.register_gamertag_placeholder),
                    leadingIcon = AppIcons.IdBadge,
                    labelAction = if (state.gamertagAvailable) ({ AvailableBadge() }) else null,
                    support = FieldSupport(stringResource(R.string.register_gamertag_support)),
                    height = AppSize.fieldHeightCompact,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = nextField,
                )
                AppTextField(
                    value = state.email,
                    onValueChange = callbacks.onEmailChange,
                    label = FieldLabel(stringResource(R.string.register_email_label), AppIcons.Mail, prominent = true),
                    placeholder = stringResource(R.string.register_email_placeholder),
                    leadingIcon = AppIcons.At,
                    height = AppSize.fieldHeightCompact,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = nextField,
                )
                AppPickerField(
                    value = state.birthdateMillis?.let { formatUtcDate(it, "dd/MM/yyyy") },
                    placeholder = stringResource(R.string.register_birthdate_placeholder),
                    onClick = { showDatePicker = true },
                    label = FieldLabel(stringResource(R.string.register_birthdate_label), AppIcons.Cake, prominent = true),
                    leadingIcon = AppIcons.Calendar,
                    support = FieldSupport(stringResource(R.string.register_birthdate_support), AppIcons.Gift),
                    height = AppSize.fieldHeightCompact,
                )
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    val strength = state.strength
                    AppTextField(
                        value = state.password,
                        onValueChange = callbacks.onPasswordChange,
                        label = FieldLabel(stringResource(R.string.register_password_label), AppIcons.LockLabel, prominent = true),
                        placeholder = stringResource(R.string.register_password_placeholder),
                        leadingIcon = AppIcons.Key,
                        labelAction = if (strength.level > 0) ({ FieldStatus(strength.label(), strength.color()) }) else null,
                        trailing = {
                            InlineIconButton(AppIcons.Eye, stringResource(R.string.register_toggle_password), callbacks.onTogglePassword)
                        },
                        height = AppSize.fieldHeightCompact,
                        textStyle = AppTheme.typography.secret,
                        visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        keyboardActions = nextField,
                    )
                    StrengthMeter(
                        filled = strength.level,
                        color = strength.color(),
                        modifier = Modifier.padding(top = spacing.xxs),
                    )
                }
                AppTextField(
                    value = state.confirmPassword,
                    onValueChange = callbacks.onConfirmPasswordChange,
                    label = FieldLabel(stringResource(R.string.register_confirm_label), AppIcons.ShieldLabel, prominent = true),
                    placeholder = stringResource(R.string.register_confirm_placeholder),
                    leadingIcon = AppIcons.Shield,
                    labelAction = state.passwordsMatch?.let { match ->
                        {
                            if (match) {
                                FieldStatus(stringResource(R.string.register_confirm_match), colors.primary, showCheck = true)
                            } else {
                                FieldStatus(stringResource(R.string.register_confirm_mismatch), colors.onErrorContainer)
                            }
                        }
                    },
                    trailing = {
                        InlineIconButton(AppIcons.Eye, stringResource(R.string.register_toggle_confirm), callbacks.onToggleConfirm)
                    },
                    height = AppSize.fieldHeightCompact,
                    textStyle = AppTheme.typography.secret,
                    visualTransformation = if (state.confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                )
                TermsCheckbox(
                    checked = state.acceptedTerms,
                    onCheckedChange = callbacks.onTermsChange,
                    onTermsClick = actions.onTermsClick,
                    onPrivacyClick = actions.onPrivacyClick,
                )
                PrimaryButton(
                    text = stringResource(R.string.register_submit),
                    onClick = actions.onRegister,
                    variant = ButtonVariant.Accent,
                    size = ButtonSize.Large,
                    trailingIcon = AppIcons.Rocket,
                    modifier = Modifier.padding(top = spacing.sm),
                )
            }

            LabeledDivider(
                stringResource(R.string.register_or),
                Modifier.padding(vertical = spacing.xl),
                thickness = AppSize.avatarRing,
                lineColor = colors.surfaceContainerHigh,
            )
            SecondaryButton(stringResource(R.string.login_google), AppIcons.Google, onClick = actions.onGoogle)
            SignInLink(actions.onSignIn, Modifier.padding(top = spacing.xl))
        }
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialMillis = state.birthdateMillis,
            confirmLabel = stringResource(R.string.register_birthdate_confirm),
            dismissLabel = stringResource(R.string.register_birthdate_cancel),
            onConfirm = { callbacks.onBirthdateSelected(it); showDatePicker = false },
            onDismiss = { showDatePicker = false },
        )
    }
}

@Composable
private fun PasswordStrength.label(): String = stringResource(
    when (level) {
        1 -> R.string.register_strength_weak
        2 -> R.string.register_strength_fair
        3 -> R.string.register_strength_good
        else -> R.string.register_strength_strong
    },
    percent,
)

@Composable
private fun PasswordStrength.color(): Color = when (level) {
    0, 1 -> AppTheme.colors.onErrorContainer
    2 -> AppTheme.colors.tertiary
    else -> AppTheme.colors.primary
}

@Preview(widthDp = 390, heightDp = 1400)
@Composable
private fun RegisterScreenPreview() {
    AppTheme {
        RegisterScreen(
            state = RegisterUiState(gamertag = "PixelMaster_99", password = "ArcadeMaster#2025", confirmPassword = "ArcadeMaster#2025", acceptedTerms = true),
            callbacks = RegisterCallbacks({}, {}, {}, {}, {}, {}, {}, {}, {}),
            actions = RegisterActions({}, {}, {}, {}, {}, {}),
        )
    }
}
