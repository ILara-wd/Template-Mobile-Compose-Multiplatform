package com.invex.one.template

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.invex.one.template.ui.components.MainTab
import com.invex.one.template.ui.components.VuelveMainScaffold
import com.invex.one.template.ui.home.HomeScreen
import com.invex.one.template.ui.home.SampleHomeUiState
import com.invex.one.template.ui.login.LoginScreen
import com.invex.one.template.ui.register.RegisterScreen
import com.invex.one.template.ui.theme.TemplateTheme
import com.invex.one.template.ui.theme.VuelveOnSurfaceVariant

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TemplateTheme {
                VuelveApp(onExit = { finish() })
            }
        }
    }
}

private enum class AppDestination { Login, Register, Main }

/** Minimal app flow until a navigation library is added. */
@Composable
private fun VuelveApp(onExit: () -> Unit) {
    var destination by rememberSaveable { mutableStateOf(AppDestination.Login) }
    var tab by rememberSaveable { mutableStateOf(MainTab.Home) }

    when (destination) {
        AppDestination.Login -> LoginScreen(
            onBack = onExit,
            onSubmit = { _, _, _ -> destination = AppDestination.Main },
            onRegister = { destination = AppDestination.Register },
        )
        AppDestination.Register -> {
            BackHandler { destination = AppDestination.Login }
            RegisterScreen(
                onBack = { destination = AppDestination.Login },
                onSubmit = { destination = AppDestination.Main },
                onSignIn = { destination = AppDestination.Login },
            )
        }
        AppDestination.Main -> {
            BackHandler(enabled = tab != MainTab.Home) { tab = MainTab.Home }
            when (tab) {
                MainTab.Home -> HomeScreen(
                    state = SampleHomeUiState,
                    onTabSelected = { tab = it },
                    onSeeFollowUps = { tab = MainTab.FollowUps },
                    onSeeAllPriority = { tab = MainTab.FollowUps },
                    onMetrics = { tab = MainTab.Results },
                    onClientList = { tab = MainTab.Clients },
                )
                else -> ComingSoonScreen(tab = tab, onTabSelected = { tab = it })
            }
        }
    }
}

/** Placeholder for tabs whose Figma screens are not implemented yet. */
@Composable
private fun ComingSoonScreen(tab: MainTab, onTabSelected: (MainTab) -> Unit) {
    VuelveMainScaffold(
        title = stringResource(tab.label),
        selectedTab = tab,
        onTabSelected = onTabSelected,
        accountAvatar = painterResource(R.drawable.img_sample_profile),
        onAccountClick = {},
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.nav_coming_soon),
                style = MaterialTheme.typography.bodyMedium,
                color = VuelveOnSurfaceVariant,
            )
        }
    }
}
