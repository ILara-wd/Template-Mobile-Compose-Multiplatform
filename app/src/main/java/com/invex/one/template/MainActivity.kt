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
import com.invex.one.template.feature.games.GamesRoute
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

@Composable
fun TemplateApp() {
    var current by rememberSaveable { mutableStateOf(AppDestination.Home) }
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

    Scaffold(
        containerColor = AppTheme.colors.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            MiniJuegosTopBar(
                section = stringResource(current.label),
                player = SamplePlayer,
                onProfileClick = { /* TODO: profile */ },
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
