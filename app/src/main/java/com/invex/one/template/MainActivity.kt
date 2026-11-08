package com.invex.one.template

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.invex.sportkid.core.designsystem.theme.AppPalette
import com.invex.sportkid.core.designsystem.theme.SportKidTheme
import com.invex.sportkid.core.designsystem.theme.ThemeMode
import com.invex.sportkid.feature.expenses.ExpensesScreen
import com.invex.sportkid.feature.settings.SettingsSamples
import com.invex.sportkid.feature.settings.SettingsScreen
import com.invex.sportkid.feature.settings.ThemeScreen
import com.invex.sportkid.feature.agenda.AgendaScreen
import com.invex.sportkid.feature.agenda.NewTrainingScreen
import com.invex.sportkid.feature.athlete.AthleteProfileScreen
import com.invex.sportkid.feature.athlete.AthleteSamples
import com.invex.sportkid.feature.athlete.AthletesScreen
import com.invex.sportkid.feature.athlete.CreateAthleteScreen
import com.invex.sportkid.feature.athlete.MemberSamples
import com.invex.sportkid.feature.athlete.MembersScreen
import com.invex.sportkid.feature.athletics.AthleticsScreen
import com.invex.sportkid.feature.athletics.RaceFormScreen
import com.invex.sportkid.feature.auth.LoginScreen
import com.invex.sportkid.feature.auth.SplashScreen
import com.invex.sportkid.feature.auth.WelcomeScreen
import com.invex.sportkid.feature.home.QuickAddAction
import com.invex.sportkid.feature.home.QuickAddSheet
import com.invex.sportkid.feature.sharing.InviteCode
import com.invex.sportkid.feature.sharing.InviteStatus
import com.invex.sportkid.feature.sharing.JoinCodeScreen
import com.invex.sportkid.feature.sharing.JoinPreviewScreen
import com.invex.sportkid.feature.sharing.ShareQrScreen
import com.invex.sportkid.feature.sharing.SharingSamples
import com.invex.sportkid.feature.sharing.rememberSimulatedSync
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
        val debugStart = if (BuildConfig.DEBUG) DebugStart.from(intent) else null
        val prefs = ThemePrefs(this)
        setContent {
            var palette by rememberSaveable { mutableStateOf(prefs.palette) }
            var mode by rememberSaveable { mutableStateOf(prefs.mode) }
            SportKidTheme(palette = palette, themeMode = mode) {
                SportKidApp(
                    onExit = ::finish,
                    debugStart = debugStart,
                    palette = palette,
                    mode = mode,
                    onThemeChange = { newPalette, newMode ->
                        palette = newPalette
                        mode = newMode
                        prefs.save(newPalette, newMode)
                    },
                )
            }
        }
    }
}

/** Flujo P01 → P04 y la app principal (T1). Navegación por estado hasta agregar Navigation Compose. */
private enum class Destination { Splash, Login, Welcome, CreateAthlete, Main, NewTraining, AthleteProfile, Taekwondo, BeltExam, Tournament, Swimming, SwimMeet, Athletics, RaceForm, ShareQr, JoinCode, JoinPreview, Members, Settings, Theme }

/**
 * Solo en debug: abre una pantalla directamente para el catálogo de UI y las capturas
 * (`scripts/ui-catalogo/capturar.py`). Extras: `sk_screen` (nombre de [Destination]),
 * `sk_tab` ([MainTab]), `sk_athlete` (id del hijo), `sk_sport` ([Sport]) y `sk_quickadd` (true).
 */
private data class DebugStart(
    val destination: Destination,
    val tab: MainTab?,
    val athleteId: String?,
    val sport: Sport?,
    val quickAdd: Boolean,
) {
    companion object {
        fun from(intent: Intent): DebugStart? {
            val screen = intent.getStringExtra("sk_screen") ?: return null
            val destination = Destination.entries.firstOrNull { it.name == screen } ?: return null
            return DebugStart(
                destination = destination,
                tab = MainTab.entries.firstOrNull { it.name == intent.getStringExtra("sk_tab") },
                athleteId = intent.getStringExtra("sk_athlete"),
                sport = Sport.entries.firstOrNull { it.name == intent.getStringExtra("sk_sport") },
                quickAdd = intent.getBooleanExtra("sk_quickadd", false),
            )
        }
    }
}

@Composable
private fun SportKidApp(
    onExit: () -> Unit,
    palette: AppPalette,
    mode: ThemeMode,
    onThemeChange: (AppPalette, ThemeMode) -> Unit,
    debugStart: DebugStart? = null,
) {
    var destination by rememberSaveable { mutableStateOf(debugStart?.destination ?: Destination.Splash) }
    var tab by rememberSaveable { mutableStateOf(debugStart?.tab ?: MainTab.Agenda) }
    var showQuickAdd by rememberSaveable { mutableStateOf(debugStart?.quickAdd ?: false) }
    var profileId by rememberSaveable { mutableStateOf(debugStart?.athleteId) }
    // Alta de deportista desde Bienvenida (onboarding) o desde la pestaña Deportistas.
    var createAthleteReturn by rememberSaveable { mutableStateOf(Destination.Welcome) }
    // Los formularios de deporte regresan a donde se abrieron (agenda o vista del deporte).
    var formReturn by rememberSaveable { mutableStateOf(Destination.Main) }
    var profileSport by rememberSaveable { mutableStateOf(debugStart?.sport) }
    // Compartir (P17) se abre desde el perfil o desde Miembros; Unirse (P18) desde Bienvenida.
    var shareReturn by rememberSaveable { mutableStateOf(Destination.AthleteProfile) }
    var inviteValue by rememberSaveable { mutableStateOf(SharingSamples.CODE) }
    var inviteExpiresAt by rememberSaveable { mutableLongStateOf(SharingSamples.code().expiresAtMillis) }
    var joinStatus by rememberSaveable { mutableStateOf<InviteStatus?>(null) }
    var removedMembers by rememberSaveable { mutableStateOf(listOf<String>()) }
    val context = LocalContext.current
    // Ajustes (P20) se abre desde el avatar de la top bar principal.
    var settingsReturn by rememberSaveable { mutableStateOf(Destination.Main) }

    BackHandler(enabled = destination == Destination.CreateAthlete) {
        destination = createAthleteReturn
    }
    BackHandler(enabled = destination == Destination.NewTraining || destination == Destination.AthleteProfile) {
        destination = Destination.Main
    }
    BackHandler(enabled = destination == Destination.SwimMeet || destination == Destination.RaceForm) {
        destination = formReturn
    }
    BackHandler(enabled = destination == Destination.Taekwondo || destination == Destination.Swimming || destination == Destination.Athletics) {
        destination = Destination.AthleteProfile
    }
    // Cambio de deporte desde la cabecera de una vista de deporte; sin vista propia vuelve al perfil.
    val openSport: (Sport) -> Unit = { sport ->
        profileSport = sport
        destination = when (sport) {
            Sport.TAEKWONDO -> Destination.Taekwondo
            Sport.SWIMMING -> Destination.Swimming
            Sport.ATHLETICS -> Destination.Athletics
        }
    }
    BackHandler(enabled = destination == Destination.BeltExam || destination == Destination.Tournament) {
        destination = formReturn
    }
    BackHandler(enabled = destination == Destination.ShareQr) { destination = shareReturn }
    BackHandler(enabled = destination == Destination.Members) { destination = Destination.AthleteProfile }
    BackHandler(enabled = destination == Destination.JoinCode) { destination = Destination.Welcome }
    BackHandler(enabled = destination == Destination.JoinPreview) { destination = Destination.JoinCode }
    BackHandler(enabled = destination == Destination.Settings) { destination = settingsReturn }
    BackHandler(enabled = destination == Destination.Theme) { destination = Destination.Settings }

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
            onScanQr = { destination = Destination.JoinCode },
            onEnterCode = { destination = Destination.JoinCode },
            onExploreDemo = { destination = Destination.Main },
        )

        Destination.CreateAthlete -> CreateAthleteScreen(
            onBack = { destination = createAthleteReturn },
            onSave = { destination = Destination.Main }, // TODO: persistir el deportista.
        )

        Destination.Main -> MainShell(
            tab = tab,
            onTabChange = { tab = it },
            onProfileClick = {
                settingsReturn = Destination.Main
                destination = Destination.Settings
            },
        ) { current, modifier ->
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
                // TODO: registrar gasto, comprobantes y exportar.
                MainTab.Expenses -> ExpensesScreen(modifier = modifier)
                else -> ComingSoon(current, modifier) // TODO: P05 Inicio.
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
                    onOpenSportDetail = openSport,
                    onShareQr = {
                        shareReturn = Destination.AthleteProfile
                        destination = Destination.ShareQr
                    },
                    onTutors = { destination = Destination.Members },
                    // TODO: edición, pago y detalle del examen.
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
                onSelectSport = openSport,
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
                onSelectSport = openSport,
            )
        }

        Destination.Athletics -> athleticsAthleteFor(profileId)?.let { athlete ->
            AthleticsScreen(
                athlete = athlete,
                onBack = { destination = Destination.AthleteProfile },
                onNewRace = {
                    formReturn = Destination.Athletics
                    destination = Destination.RaceForm
                },
                onSelectSport = openSport,
            )
        }

        // TODO: guardar la carrera y actualizar mejores marcas y galería.
        Destination.RaceForm -> athleticsAthleteFor(profileId)?.let { athlete ->
            RaceFormScreen(athlete = athlete, onClose = { destination = formReturn }, onSave = { destination = formReturn })
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

        // TODO: pedir y renovar el código al backend de invitaciones.
        Destination.ShareQr -> athleteFor(profileId)?.let { athlete ->
            ShareQrScreen(
                athlete = athlete.toShareAthlete(),
                code = InviteCode(inviteValue, inviteExpiresAt),
                onBack = { destination = shareReturn },
                onShareLink = { context.shareText(it) },
                onRegenerate = {
                    val code = InviteCode.generate()
                    inviteValue = code.value
                    inviteExpiresAt = code.expiresAtMillis
                },
            )
        }

        // TODO: quitar acceso en el backend; hoy solo se oculta en la sesión.
        Destination.Members -> athleteFor(profileId)?.let { athlete ->
            MembersScreen(
                athlete = athlete,
                members = MemberSamples.members().filterNot { it.id in removedMembers },
                onBack = { destination = Destination.AthleteProfile },
                onInvite = {
                    shareReturn = Destination.Members
                    destination = Destination.ShareQr
                },
                onShareLink = {
                    context.shareText(
                        context.getString(
                            com.invex.sportkid.feature.sharing.R.string.share_link_message,
                            athlete.firstName,
                            InviteCode.format(inviteValue),
                            InviteCode.VALIDITY_HOURS,
                        ),
                    )
                },
                onRemoveMember = { removedMembers = removedMembers + it.id },
            )
        }

        // TODO: secuencia de cintas, términos, soporte y persistir las demás preferencias.
        Destination.Settings -> SettingsScreen(
            account = SettingsSamples.account.copy(photoRes = R.drawable.img_sample_user_avatar),
            palette = palette,
            mode = mode,
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE,
            onBack = { destination = settingsReturn },
            onOpenTheme = { destination = Destination.Theme },
            onModeChange = { onThemeChange(palette, it) },
            onSignOut = { destination = Destination.Login }, // TODO: cerrar sesión en Firebase Auth.
        )

        Destination.Theme -> ThemeScreen(
            palette = palette,
            mode = mode,
            onApply = onThemeChange,
            onBack = { destination = Destination.Settings },
        )

        // TODO: escáner de Google (play-services-code-scanner), linterna y QR desde la galería.
        Destination.JoinCode -> JoinCodeScreen(
            onBack = { destination = Destination.Welcome },
            onValidate = { code ->
                joinStatus = SharingSamples.validate(code)
                if (joinStatus == InviteStatus.VALID) destination = Destination.JoinPreview
            },
            status = joinStatus,
            onCodeChange = { joinStatus = null },
        )

        // TODO: descargar el expediente real; la demo simula el avance.
        Destination.JoinPreview -> athleteFor("mateo")?.let { athlete ->
            JoinPreviewScreen(
                preview = joinPreviewFor(athlete),
                steps = rememberSimulatedSync(),
                onBack = { destination = Destination.JoinCode },
                onOpenProfile = {
                    profileId = athlete.id
                    profileSport = null
                    destination = Destination.AthleteProfile
                },
            )
        }
    }

    if (showQuickAdd && (destination == Destination.Main || destination == Destination.AthleteProfile)) {
        QuickAddSheet(
            onDismiss = { showQuickAdd = false },
            onSelect = { action ->
                showQuickAdd = false
                // Examen, torneo, competencia y carrera usan al hijo abierto o al primero del deporte.
                when (action) {
                    QuickAddAction.Training -> destination = Destination.NewTraining
                    QuickAddAction.Race -> {
                        formReturn = destination
                        destination = Destination.RaceForm
                    }
                    QuickAddAction.SwimMeet -> {
                        formReturn = destination
                        destination = Destination.SwimMeet
                    }
                    QuickAddAction.BeltExam, QuickAddAction.TkdTournament -> {
                        formReturn = destination
                        destination = if (action == QuickAddAction.BeltExam) Destination.BeltExam else Destination.Tournament
                    }
                }
            },
        )
    }
}

/** Abre la hoja de compartir del sistema con [text]. */
private fun Context.shareText(text: String) {
    val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    startActivity(Intent.createChooser(send, null))
}
