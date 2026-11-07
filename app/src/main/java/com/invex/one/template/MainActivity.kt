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
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.feature.athlete.CreateAthleteScreen
import com.invex.sportkid.feature.auth.LoginScreen
import com.invex.sportkid.feature.auth.SplashScreen
import com.invex.sportkid.feature.auth.WelcomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SportKidTheme {
                SportKidApp(onExit = ::finish)
            }
        }
    }
}

/** Flujo de onboarding P01 → P04. Navegación simple por estado hasta agregar Navigation Compose. */
private enum class Destination { Splash, Login, Welcome, CreateAthlete }

@Composable
private fun SportKidApp(onExit: () -> Unit) {
    var destination by rememberSaveable { mutableStateOf(Destination.Splash) }

    BackHandler(enabled = destination == Destination.CreateAthlete) {
        destination = Destination.Welcome
    }

    when (destination) {
        Destination.Splash -> SplashScreen(
            versionName = BuildConfig.VERSION_NAME,
            onFinished = { destination = Destination.Login },
        )

        Destination.Login -> LoginScreen(
            onBack = onExit,
            // TODO: Credential Manager + Firebase Auth.
            onGoogleSignIn = { destination = Destination.Welcome },
            onContinueAsGuest = { destination = Destination.Welcome },
            onOpenTerms = {},
            onOpenPrivacy = {},
        )

        Destination.Welcome -> WelcomeScreen(
            onCreateAthlete = { destination = Destination.CreateAthlete },
            onScanQr = {}, // TODO: P18 Unirse con QR.
            onEnterCode = {},
            onExploreDemo = {},
        )

        Destination.CreateAthlete -> CreateAthleteScreen(
            onBack = { destination = Destination.Welcome },
            onSave = { destination = Destination.Welcome }, // TODO: persistir y abrir P05 Inicio.
        )
    }
}
