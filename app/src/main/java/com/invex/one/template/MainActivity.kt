package com.invex.one.template

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.molecule.NavBarItem
import com.invex.one.template.core.designsystem.organism.AppBottomBar
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.data.SamplePlayer
import com.invex.one.template.feature.auth.LoginActions
import com.invex.one.template.feature.auth.LoginRoute
import com.invex.one.template.feature.auth.RegisterActions
import com.invex.one.template.feature.auth.RegisterRoute
import com.invex.one.template.feature.games.GamesRoute
import com.invex.one.template.feature.profile.ProfileActions
import com.invex.one.template.feature.profile.ProfileRoute
import com.invex.one.template.feature.games.data.GameCatalog
import com.invex.one.template.feature.home.HomeRoute
import com.invex.one.template.game.GameHostScreen
import com.invex.one.template.feature.records.RecordsRoute
import com.invex.one.template.ui.MiniJuegosTopBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // The design is light-only: keep dark system-bar icons regardless of the system theme.
        val lightBars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = lightBars, navigationBarStyle = lightBars)
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                TemplateApp()
            }
        }
    }
}

enum class AppDestination(@StringRes val label: Int, @DrawableRes val icon: Int) {
    Home(R.string.nav_home, AppIcons.Home),
    Games(R.string.nav_games, AppIcons.Gamepad),
    Records(R.string.nav_records, AppIcons.Trophy),
}

/** Screens shown before the player enters the arcade. */
enum class AuthScreen { Login, Register }

/**
 * App root. Login is the start screen: the tabs are only reachable after entering
 * (login, Google, guest or registration). There is no auth backend yet, so the session
 * lives in saved state and is lost when the process ends.
 */
@Composable
fun TemplateApp() {
    var signedIn by rememberSaveable { mutableStateOf(false) }
    var authScreen by rememberSaveable { mutableStateOf(AuthScreen.Login) }

    if (signedIn) {
        SignedInApp(
            onSignOut = {
                authScreen = AuthScreen.Login
                signedIn = false
            },
        )
    } else {
        AuthFlow(
            screen = authScreen,
            onScreenChange = { authScreen = it },
            onSignedIn = { signedIn = true },
        )
    }
}

/** Login ⇄ Register. Login is the root, so it has no back button. */
@Composable
private fun AuthFlow(
    screen: AuthScreen,
    onScreenChange: (AuthScreen) -> Unit,
    onSignedIn: () -> Unit,
) {
    when (screen) {
        AuthScreen.Login -> LoginRoute(
            gamesCount = GameCatalog.games.size,
            streakDays = SamplePlayer.streakDays,
            actions = LoginActions(
                onBack = null,
                // TODO: no auth backend yet; every entry path signs in directly.
                onLogin = onSignedIn,
                onGoogle = onSignedIn,
                onGuest = onSignedIn,
                onForgotPassword = { /* TODO: password recovery */ },
                onSignUp = { onScreenChange(AuthScreen.Register) },
            ),
        )
        AuthScreen.Register -> RegisterRoute(
            actions = RegisterActions(
                onBack = { onScreenChange(AuthScreen.Login) },
                // TODO: no account backend yet; registering signs in directly.
                onRegister = onSignedIn,
                onGoogle = onSignedIn,
                onSignIn = { onScreenChange(AuthScreen.Login) },
                onTermsClick = { /* TODO: terms of service */ },
                onPrivacyClick = { /* TODO: privacy policy */ },
            ),
        )
    }
}

/** Tabs plus the full-screen flows opened from them (game, profile). */
@Composable
private fun SignedInApp(onSignOut: () -> Unit) {
    var current by rememberSaveable { mutableStateOf(AppDestination.Home) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    // Game currently being played (id + title); null shows the tabs.
    var activeGameId by rememberSaveable { mutableStateOf<String?>(null) }
    var activeGameTitle by rememberSaveable { mutableStateOf("") }
    val openGame: (String, String) -> Unit = { id, title ->
        activeGameId = id
        activeGameTitle = title
    }

    activeGameId?.let { gameId ->
        GameHostScreen(gameId = gameId, title = activeGameTitle, onBack = { activeGameId = null })
        return
    }
    if (showProfile) {
        ProfileRoute(
            actions = ProfileActions(
                onBack = { showProfile = false },
                onLogout = onSignOut,
                onChangePassword = { /* TODO: change password */ },
                onChangePhoto = { /* TODO: photo picker */ },
                onUpdateEmail = { /* TODO: email verification flow */ },
            ),
        )
        return
    }

    Scaffold(
        containerColor = AppTheme.colors.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            MiniJuegosTopBar(
                section = stringResource(current.label),
                player = SamplePlayer,
                onProfileClick = { showProfile = true },
            )
        },
        bottomBar = {
            AppBottomBar {
                AppDestination.entries.forEach { destination ->
                    NavBarItem(
                        icon = destination.icon,
                        label = stringResource(destination.label),
                        selected = destination == current,
                        onClick = { current = destination },
                    )
                }
            }
        },
    ) { innerPadding ->
        when (current) {
            AppDestination.Home -> HomeRoute(
                onPlayGame = openGame,
                onSeeHistory = { current = AppDestination.Records },
                contentPadding = innerPadding,
            )
            AppDestination.Games -> GamesRoute(
                onPlayGame = openGame,
                contentPadding = innerPadding,
            )
            AppDestination.Records -> RecordsRoute(
                onGlobalRankingClick = { /* TODO: global ranking */ },
                contentPadding = innerPadding,
            )
        }
    }
}
