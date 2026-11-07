package com.invex.one.template

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.feature.agenda.AgendaScreen
import com.invex.sportkid.feature.agenda.NewTrainingScreen
import com.invex.sportkid.feature.athlete.AthleteProfileScreen
import com.invex.sportkid.feature.athlete.AthleteSamples
import com.invex.sportkid.feature.athlete.AthletesScreen
import com.invex.sportkid.feature.athlete.CreateAthleteScreen
import com.invex.sportkid.feature.auth.LoginScreen
import com.invex.sportkid.feature.auth.SplashScreen
import com.invex.sportkid.feature.auth.WelcomeScreen
import com.invex.sportkid.feature.home.QuickAddAction
import com.invex.sportkid.feature.home.QuickAddSheet
import com.invex.sportkid.feature.swimming.SwimMeetFormScreen
import com.invex.sportkid.feature.swimming.SwimmingScreen
import com.invex.sportkid.feature.tkd.BeltExamScreen
import com.invex.sportkid.feature.tkd.TaekwondoScreen
import com.invex.sportkid.feature.tkd.TournamentFormScreen
import com.invex.sportkid.core.model.Sport

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

/** Flujo P01 → P04 y la app principal (T1). Navegación por estado hasta agregar Navigation Compose. */
private enum class Destination { Splash, Login, Welcome, CreateAthlete, Main, NewTraining, AthleteProfile, Taekwondo, BeltExam, Tournament, Swimming, SwimMeet }

@Composable
private fun SportKidApp(onExit: () -> Unit) {
    var destination by rememberSaveable { mutableStateOf(Destination.Splash) }
    var tab by rememberSaveable { mutableStateOf(MainTab.Agenda) }
    var showQuickAdd by rememberSaveable { mutableStateOf(false) }
    var profileId by rememberSaveable { mutableStateOf<String?>(null) }
    // Alta de deportista desde Bienvenida (onboarding) o desde la pestaña Deportistas.
    var createAthleteReturn by rememberSaveable { mutableStateOf(Destination.Welcome) }
    // Los formularios de deporte regresan a donde se abrieron (agenda o vista del deporte).
    var formReturn by rememberSaveable { mutableStateOf(Destination.Main) }
    var profileSport by rememberSaveable { mutableStateOf<Sport?>(null) }

    BackHandler(enabled = destination == Destination.CreateAthlete) {
        destination = createAthleteReturn
    }
    BackHandler(enabled = destination == Destination.NewTraining || destination == Destination.AthleteProfile) {
        destination = Destination.Main
    }
    BackHandler(enabled = destination == Destination.SwimMeet) {
        destination = formReturn
    }
    BackHandler(enabled = destination == Destination.Taekwondo || destination == Destination.Swimming) {
        destination = Destination.AthleteProfile
    }
    BackHandler(enabled = destination == Destination.BeltExam || destination == Destination.Tournament) {
        destination = formReturn
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
            onCreateAthlete = {
                createAthleteReturn = Destination.Welcome
                destination = Destination.CreateAthlete
            },
            onScanQr = {}, // TODO: P18 Unirse con QR.
            onEnterCode = {},
            onExploreDemo = { destination = Destination.Main },
        )

        Destination.CreateAthlete -> CreateAthleteScreen(
            onBack = { destination = createAthleteReturn },
            onSave = { destination = Destination.Main }, // TODO: persistir el deportista.
        )

        Destination.Main -> MainShell(tab = tab, onTabChange = { tab = it }) { current, modifier ->
            when (current) {
                MainTab.Agenda -> AgendaScreen(onNewEvent = { showQuickAdd = true }, modifier = modifier)
                MainTab.Athletes -> AthletesScreen(
                    onOpenProfile = {
                        profileId = it.id
                        profileSport = null
                        destination = Destination.AthleteProfile
                    },
                    onAddAthlete = {
                        createAthleteReturn = Destination.Main
                        destination = Destination.CreateAthlete
                    },
                    modifier = modifier,
                )
                else -> ComingSoon(current, modifier) // TODO: P05 Inicio, P16 Gastos.
            }
        }

        Destination.NewTraining -> NewTrainingScreen(
            onClose = { destination = Destination.Main },
            onSave = { destination = Destination.Main }, // TODO: guardar en el repositorio de eventos.
        )

        Destination.AthleteProfile -> {
            val athlete = AthleteSamples.athletes().firstOrNull { it.id == profileId }
            if (athlete == null) {
                LaunchedEffect(Unit) { destination = Destination.Main }
            } else {
                AthleteProfileScreen(
                    athlete = athlete,
                    onBack = { destination = Destination.Main },
                    onQuickLog = { showQuickAdd = true },
                    initialSport = profileSport,
                    onOpenSportDetail = { sport ->
                        when (sport) {
                            Sport.TAEKWONDO -> destination = Destination.Taekwondo
                            Sport.SWIMMING -> destination = Destination.Swimming
                            Sport.ATHLETICS -> Unit // TODO: P14 Tab Atletismo.
                        }
                    },
                    // TODO: P17 Compartir QR, P19 Tutores, edición, pago y detalle del examen.
                )
            }
        }

        Destination.Taekwondo -> tkdAthleteFor(profileId)?.let { athlete ->
            TaekwondoScreen(
                athlete = athlete,
                onBack = { destination = Destination.AthleteProfile },
                onNewExam = {
                    formReturn = Destination.Taekwondo
                    destination = Destination.BeltExam
                },
                onNewTournament = {
                    formReturn = Destination.Taekwondo
                    destination = Destination.Tournament
                },
                onSelectSport = { sport ->
                    if (sport == Sport.SWIMMING) {
                        destination = Destination.Swimming
                    } else {
                        profileSport = sport
                        destination = Destination.AthleteProfile
                    }
                },
            )
        }

        Destination.Swimming -> swimAthleteFor(profileId)?.let { athlete ->
            SwimmingScreen(
                athlete = athlete,
                onBack = { destination = Destination.AthleteProfile },
                onNewMeet = {
                    formReturn = Destination.Swimming
                    destination = Destination.SwimMeet
                },
                onSelectSport = { sport ->
                    if (sport == Sport.TAEKWONDO) {
                        destination = Destination.Taekwondo
                    } else {
                        profileSport = sport
                        destination = Destination.AthleteProfile
                    }
                },
            )
        }

        // TODO: guardar la competencia y actualizar mejores tiempos.
        Destination.SwimMeet -> swimAthleteFor(profileId)?.let { athlete ->
            SwimMeetFormScreen(athlete = athlete, onClose = { destination = formReturn }, onSave = { destination = formReturn })
        }

        // TODO: guardar exámenes y torneos; al aprobar, actualizar la cinta del perfil.
        Destination.BeltExam -> tkdAthleteFor(profileId)?.let { athlete ->
            BeltExamScreen(athlete = athlete, onClose = { destination = formReturn }, onSave = { destination = formReturn })
        }

        Destination.Tournament -> tkdAthleteFor(profileId)?.let { athlete ->
            TournamentFormScreen(athlete = athlete, onClose = { destination = formReturn }, onSave = { destination = formReturn })
        }
    }

    if (showQuickAdd && (destination == Destination.Main || destination == Destination.AthleteProfile)) {
        QuickAddSheet(
            onDismiss = { showQuickAdd = false },
            onSelect = { action ->
                showQuickAdd = false
                // TODO: P15 Carrera. Examen, torneo y competencia usan al primer hijo del deporte.
                when (action) {
                    QuickAddAction.Training -> destination = Destination.NewTraining
                    QuickAddAction.SwimMeet -> {
                        formReturn = destination
                        destination = Destination.SwimMeet
                    }
                    QuickAddAction.BeltExam, QuickAddAction.TkdTournament -> {
                        formReturn = destination
                        destination = if (action == QuickAddAction.BeltExam) Destination.BeltExam else Destination.Tournament
                    }
                    else -> Unit
                }
            },
        )
    }
}
