package com.invex.one.template

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.invex.one.template.core.designsystem.icon.AppIcons
import com.invex.one.template.core.designsystem.molecule.NavBarItem
import com.invex.one.template.core.designsystem.organism.AppBottomBar
import com.invex.one.template.core.designsystem.theme.AppTheme
import com.invex.one.template.feature.home.HomeRoute

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
    Scaffold(
        containerColor = AppTheme.colors.background,
        contentWindowInsets = WindowInsets(0),
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
                onPlayGame = { /* TODO: navigate to game */ },
                onSeeHistory = { current = AppDestination.Records },
                onProfileClick = { /* TODO: profile */ },
                contentPadding = innerPadding,
            )
            else -> Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(stringResource(current.label), style = AppTheme.typography.title, color = AppTheme.colors.onSurface)
            }
        }
    }
}
