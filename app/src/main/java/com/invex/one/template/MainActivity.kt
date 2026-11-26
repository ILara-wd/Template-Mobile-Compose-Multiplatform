package com.invex.one.template

import android.content.Intent
import android.content.pm.ApplicationInfo
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
        val debuggable = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        val start = if (debuggable) DebugStart.from(intent) else DebugStart.Default
        setContent {
            AppTheme {
                TemplateApp(start = start)
            }
        }
    }
}

/**
 * Screen the app opens on. Debug builds read it from intent extras so tools (the UI catalog in
 * `scripts/ui-catalogo`) can open any screen without tapping through the flow:
 * `adb shell am start -S -n com.invex.one.template/.MainActivity --es mj_screen Profile`.
 * Use `--es mj_game <id>` with `mj_screen Game` to open a game. Release builds ignore extras.
 */
data class DebugStart(val screen: StartScreen = StartScreen.Login, val gameId: String? = null) {
    enum class StartScreen { Login, Register, Home, Games, Records, Profile, Game }

    companion object {
        val Default = DebugStart()

        fun from(intent: Intent?): DebugStart {
            val screen = intent?.getStringExtra("mj_screen")
                ?.let { name -> StartScreen.entries.firstOrNull { it.name.equals(name, ignoreCase = true) } }
                ?: return Default
            return DebugStart(screen, intent.getStringExtra("mj_game") ?: GameCatalog.games.first().id)
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
fun TemplateApp(start: DebugStart = DebugStart.Default) {
    val startScreen = start.screen
    var signedIn by rememberSaveable {
        mutableStateOf(startScreen != DebugStart.StartScreen.Login && startScreen != DebugStart.StartScreen.Register)
    }
    var authScreen by rememberSaveable {
        mutableStateOf(if (startScreen == DebugStart.StartScreen.Register) AuthScreen.Register else AuthScreen.Login)
    }

    // The debug start only applies to the first session; signing in again starts at Home.
    var startConsumed by rememberSaveable { mutableStateOf(false) }

    if (signedIn) {
        SignedInApp(
            start = if (startConsumed) DebugStart.Default else start,
            onSignOut = {
                startConsumed = true
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
private fun SignedInApp(start: DebugStart, onSignOut: () -> Unit) {
    var current by rememberSaveable {
        mutableStateOf(
            when (start.screen) {
                DebugStart.StartScreen.Games -> AppDestination.Games
                DebugStart.StartScreen.Records -> AppDestination.Records
                else -> AppDestination.Home
            },
        )
    }
    var showProfile by rememberSaveable { mutableStateOf(start.screen == DebugStart.StartScreen.Profile) }
    // Game currently being played (id + title); null shows the tabs.
    val startGame = start.gameId
        ?.takeIf { start.screen == DebugStart.StartScreen.Game }
        ?.let { id -> GameCatalog.games.firstOrNull { it.id == id } }
    var activeGameId by rememberSaveable { mutableStateOf(startGame?.id) }
    var activeGameTitle by rememberSaveable { mutableStateOf(startGame?.name.orEmpty()) }
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
