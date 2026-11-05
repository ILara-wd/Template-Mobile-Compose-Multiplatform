package com.invex.one.template

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.invex.one.template.ui.login.LoginScreen
import com.invex.one.template.ui.register.RegisterScreen
import com.invex.one.template.ui.theme.TemplateTheme

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

private enum class AuthDestination { Login, Register }

/** Minimal auth flow until a navigation library is added. */
@Composable
private fun VuelveApp(onExit: () -> Unit) {
    var destination by rememberSaveable { mutableStateOf(AuthDestination.Login) }

    when (destination) {
        AuthDestination.Login -> LoginScreen(
            onBack = onExit,
            onRegister = { destination = AuthDestination.Register },
        )
        AuthDestination.Register -> {
            BackHandler { destination = AuthDestination.Login }
            RegisterScreen(
                onBack = { destination = AuthDestination.Login },
                onSignIn = { destination = AuthDestination.Login },
            )
        }
    }
}
