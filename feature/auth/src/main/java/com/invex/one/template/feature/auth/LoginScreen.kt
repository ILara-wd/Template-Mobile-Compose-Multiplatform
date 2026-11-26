package com.invex.one.template.feature.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.one.template.core.designsystem.atom.ButtonSize
import com.invex.one.template.core.designsystem.atom.InlineIconButton
import com.invex.one.template.core.designsystem.atom.PrimaryButton
import com.invex.one.template.core.designsystem.atom.SecondaryButton
import com.invex.one.template.core.designsystem.atom.SecondaryButtonVariant
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.molecule.AppTextField
import com.invex.one.template.core.designsystem.molecule.CheckboxRow
import com.invex.one.template.core.designsystem.molecule.FieldLabel
import com.invex.one.template.core.designsystem.molecule.LabeledDivider
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.core.designsystem.theme.appShadow
import com.invex.one.template.feature.auth.component.LoginHero
import com.invex.one.template.feature.auth.component.AuthTopBar
import com.invex.one.template.feature.auth.component.StreakBanner
import com.invex.one.template.feature.auth.component.SyncedGamesCard

/** Callbacks for every exit / navigation out of the login screen. */
data class LoginActions(
    /** `null` when login is the root screen: no back button, system back leaves the app. */
    val onBack: (() -> Unit)?,
    val onLogin: () -> Unit,
    val onGoogle: () -> Unit,
    val onGuest: () -> Unit,
    val onForgotPassword: () -> Unit,
    val onSignUp: () -> Unit,
)

/** Stateful entry point: wires the [LoginViewModel] to the stateless [LoginScreen]. */
@Composable
fun LoginRoute(
    gamesCount: Int,
    streakDays: Int,
    actions: LoginActions,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    actions.onBack?.let { BackHandler(onBack = it) }
    LoginScreen(
        state = state,
        gamesCount = gamesCount,
        streakDays = streakDays,
        onIdentifierChange = viewModel::onIdentifierChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onRememberMeChange = viewModel::onRememberMeChange,
        actions = actions,
        modifier = modifier,
    )
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    gamesCount: Int,
    streakDays: Int,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    actions: LoginActions,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    val focusManager = LocalFocusManager.current
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
            StreakBanner(streakDays, Modifier.padding(top = spacing.sm, bottom = spacing.lg))
            LoginHero(gamesCount, Modifier.padding(bottom = spacing.xl))

            // Form
            val cardShape = AppTheme.shapes.medium
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .appShadow(AppTheme.elevation.xs, cardShape, colors.shadow)
                    .background(colors.surfaceContainerLowest, cardShape)
                    .padding(spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.lg),
            ) {
                AppTextField(
                    value = state.identifier,
                    onValueChange = onIdentifierChange,
                    label = FieldLabel(stringResource(R.string.login_email_label)),
                    placeholder = stringResource(R.string.login_email_placeholder),
                    leadingIcon = AppIcons.At,
                    labelAction = {
                        Text(
                            stringResource(R.string.login_level_required, state.requiredLevel),
                            style = AppTheme.typography.label,
                            color = colors.secondary,
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                )
                AppTextField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    label = FieldLabel(stringResource(R.string.login_password_label)),
                    placeholder = stringResource(R.string.login_password_placeholder),
                    leadingIcon = AppIcons.Lock,
                    labelAction = {
                        LinkText(stringResource(R.string.login_forgot_password), onClick = actions.onForgotPassword)
                    },
                    trailing = {
                        InlineIconButton(
                            icon = AppIcons.Eye,
                            contentDescription = stringResource(
                                if (state.passwordVisible) R.string.login_hide_password else R.string.login_show_password,
                            ),
                            onClick = onTogglePasswordVisibility,
                        )
                    },
                    visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); actions.onLogin() }),
                )
                CheckboxRow(
                    checked = state.rememberMe,
                    onCheckedChange = onRememberMeChange,
                    title = stringResource(R.string.login_remember),
                    supportingText = stringResource(R.string.login_remember_supporting),
                    modifier = Modifier.padding(top = spacing.xxs),
                )
                PrimaryButton(
                    text = stringResource(R.string.login_submit),
                    onClick = actions.onLogin,
                    size = ButtonSize.Large,
                    trailingIcon = AppIcons.PlayLarge,
                    modifier = Modifier.padding(top = spacing.xxs),
                )
            }

            LabeledDivider(
                stringResource(R.string.login_or_continue),
                Modifier.padding(top = spacing.lg + spacing.sm, bottom = spacing.sm),
            )
            Column(
                modifier = Modifier.padding(top = spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                SecondaryButton(stringResource(R.string.login_google), AppIcons.Google, onClick = actions.onGoogle)
                SecondaryButton(
                    stringResource(R.string.login_guest),
                    AppIcons.WifiOff,
                    onClick = actions.onGuest,
                    variant = SecondaryButtonVariant.Tonal,
                )
            }
            SyncedGamesCard(gamesCount, Modifier.padding(top = spacing.xl))
            Row(
                modifier = Modifier.padding(top = spacing.xl),
                horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(stringResource(R.string.login_no_account), style = AppTheme.typography.body, color = colors.onSurfaceVariant)
                Text(
                    stringResource(R.string.login_sign_up),
                    style = AppTheme.typography.subtitle,
                    color = colors.primary,
                    modifier = Modifier
                        .clip(AppTheme.shapes.extraSmall)
                        .clickable(role = Role.Button, onClick = actions.onSignUp),
                )
            }
        }
    }
}

/** Small secondary-colored text link (e.g. "¿Olvidaste tu clave?"). */
@Composable
private fun LinkText(text: String, onClick: () -> Unit) {
    Text(
        text,
        style = AppTheme.typography.label,
        color = AppTheme.colors.secondary,
        modifier = Modifier
            .clip(AppTheme.shapes.extraSmall)
            .clickable(role = Role.Button, onClick = onClick),
    )
}

@Preview(widthDp = 390, heightDp = 1000)
@Composable
private fun LoginScreenPreview() {
    AppTheme {
        LoginScreen(
            state = LoginUiState(),
            gamesCount = 7,
            streakDays = 5,
            onIdentifierChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onRememberMeChange = {},
            actions = LoginActions({}, {}, {}, {}, {}, {}),
        )
    }
}
