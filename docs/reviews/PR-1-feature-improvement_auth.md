# Validación arquitectónica — PR #1 `feature/improvement_auth` → `develop`

| Dato | Valor |
|------|-------|
| Veredicto | **❌ BLOQUEADO** |
| Base | `origin/develop` |
| Rama origen | `origin/feature/improvement_auth` (`29c4552`) |
| Modo / rol | Local (sin MCP de Bitbucket) · TL — **no publicado en Bitbucket** |
| Fecha | 2026-11-03 |
| Herramienta | `/validate-pullrequest 1 tl` |

**Motivo del bloqueo:** el build está en rojo (`./gradlew detekt` con 64 hallazgos nuevos y 1 unit test fallando) y hay violaciones 🔴 nuevas de capas, logging y accesibilidad.

---

## 1. Alcance

- 21 commits · 132 archivos.
- `app/src/main`: 71 `.kt` (48 nuevos) · `app/src/test`: 11 nuevos · 5 recursos · build (`app/build.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `libs.versions.toml`, `AndroidManifest.xml`).
- Familias ejecutadas: FF-CORE, FF-ARC, FF-ST, FF-CMP, FF-THM, FF-A11Y, FF-I18N, FF-RES, FF-PF, FF-DUP, FF-DEP, FF-COV.
- Sin ticket `KEN-<n>` en rama ni commits: se omite la validación de AC/DoD.

---

## 2. 🔴 Violaciones críticas

### V-01 — Detekt falla y hay un unit test en rojo (FF-CORE-10)

**Detekt (64 hallazgos ponderados fuera del baseline):**

| Regla | Nº |
|-------|----|
| `KenshoNoDpLiteral` | 13 |
| `KenshoPublicComposableNeedsPreview` | 11 |
| `KenshoNoLiteralText` | 7 |
| `KenshoNoColorLiteral` | 6 |
| `UnusedPrivateMember` | 6 |
| `LongMethod` | 4 |
| `KenshoMaxFileLines` | 3 |
| `MatchingDeclarationName` | 3 |
| `MaxLineLength` | 3 |
| `NewLineAtEndOfFile` | 3 |
| `KenshoNoLog`, `KenshoNullContentDescriptionNeedsComment`, `PrintStackTrace`, `TooGenericExceptionCaught`, `ExplicitItLambdaParameter` | 1 c/u |

**Unit test:** `TkdAuthMapperTest.toCode_cadaDisciplina_usaCodigoDelContrato` falla.

Archivos: `data/remote/mapper/TkdAuthMapper.kt:19-22`, `test/.../TkdAuthMapperTest.kt:20-25`, `docs/api/kensho-api.yaml:145`

```kotlin
// ❌ INCORRECTO — el enum se redujo a 2 valores; test y contrato del API siguen con 4
internal fun DojoDiscipline.toCode(): String = when (this) {
    DojoDiscipline.Karate -> "KARATE"
    DojoDiscipline.Taekwondo -> "TAEKWONDO"
}
// expected:<[KARATE, BJJ, TAEKWONDO, MIXED]> but was:<[KARATE, TAEKWONDO]>
```

```kotlin
// ✅ CORRECTO — TEST-06: enum, mapper, test y docs/api/kensho-api.yaml deben coincidir.
// Si el producto quitó BJJ/MIXED, actualizar el test y el contrato en el mismo PR.
```

### V-02 — Red/DTO importados fuera de `data/` y `di/` (FF-CORE-11, ARC-11, ADR-017)

Archivos:
- `components/logicComponents/ShowForceUpdateDialog.kt:5`
- `components/logicComponents/ShowMaintenanceDialog.kt:8`
- `feature/auth/splash/SplashScreen.kt:23`
- `feature/auth/splash/SplashViewModel.kt:4`

```kotlin
// ❌ INCORRECTO — la UI y el VM consumen el DTO @Serializable de data/remote
import dev.treecode.kensho.data.remote.remoteConfig.ForceUpdate
import dev.treecode.kensho.data.remote.remoteConfig.ConfigVersionAppRemote
```

```kotlin
// ✅ CORRECTO — ARC-11: DTO solo en data/remote; mapper → modelo @Immutable en model/
@Immutable
data class TkdForceUpdate(val title: String, val body: String, val actionLabel: String, val secondaryActionLabel: String)
// data/remote/mapper: fun ForceUpdate.toModel() = TkdForceUpdate(...)
```

### V-03 — Paquete compartido importa un feature (FF-CORE-04, ARC-05)

Archivo: `components/molecules/TdkWebPage.kt:14`

```kotlin
// ❌ INCORRECTO — una molécula depende de una sección de signup
import dev.treecode.kensho.feature.auth.signup.TkdSignUpHeader
```

```kotlin
// ✅ CORRECTO — mover el header a components/molecules (TkdBackHeader) o recibirlo como slot
@Composable
fun TkdWebPage(url: String, modifier: Modifier = Modifier, topBar: @Composable () -> Unit = {}) { … }
```

### V-04 — Logging directo (FF-CORE-01, LOG-01, ADR-012)

Archivo: `commons/Ext.kt:20-22`

```kotlin
// ❌ INCORRECTO
} catch (e: Exception) {
    e.printStackTrace()
}
```

```kotlin
// ✅ CORRECTO — LOG-01: único punto de logging y excepción específica
} catch (e: ActivityNotFoundException) {
    KenshoLogger.w(TAG, "No hay app para abrir la URL", e)
}
```

### V-05 — `contentDescription` nulo sin justificar o ausente (FF-A11Y-01/02, ADR-008)

Archivos:
- `feature/auth/login/TkdLoginSections.kt:174, 189, 279`
- `feature/auth/signup/TkdSignUpSections.kt:126, 267`
- `components/molecules/FloatingDialog.kt:83`
- `nav/AppNavigation.kt:46, 56` (sin `contentDescription`)

```kotlin
// ❌ INCORRECTO
Icon(painter = painterResource(R.drawable.ic_tkd_enso), contentDescription = null)
Icon(Icons.Default.Home, null)
```

```kotlin
// ✅ CORRECTO — A11Y: null solo si es decorativo, con comentario en la misma línea
Icon(painter = painterResource(R.drawable.ic_tkd_enso), contentDescription = null) // decorativo: logo junto al título
Icon(Icons.Default.Home, contentDescription = stringResource(R.string.tkd_cd_nav_home))
```

### V-06 — `NavHostController` fuera de `TkdApp` (FF-CMP-11, ARC-04)

Archivo: `nav/AppNavigation.kt:32-119`

```kotlin
// ❌ INCORRECTO — segundo grafo de navegación con placeholders y textos literales; ningún archivo lo usa
@Composable
fun MainNavHost(navController: androidx.navigation.NavHostController) { … }
```

```kotlin
// ✅ CORRECTO — ARC-04: la navegación vive solo en ui/TkdApp.kt. Eliminar el archivo (código muerto).
```

---

## 3. 🟠 Violaciones altas (bloquean en `develop` salvo justificación escrita)

### V-07 — Splash no funcional e inalcanzable

Archivos: `feature/auth/splash/SplashScreen.kt:44-133`, `SplashViewModel.kt:25-28`, `ui/TkdApp.kt:216`

- El VM nunca actualiza el estado: `isLoading = true` y `config = null` para siempre → loader infinito.
- `navToHome()` y las asignaciones `isBlocked/isForceUpdate = true` se ejecutan **durante la composición** (efecto secundario en cada recomposición).
- `isMaintenance` nunca se pone en `true`.
- `uiState.collectAsStateWithLifecycle()` se llama 4 veces en el mismo composable.
- Ninguna ruta navega a `SPLASH_ROUTE` (`startDestination = LOGIN_ROUTE`).

```kotlin
// ❌ INCORRECTO — navegación y mutación de estado en composición
if (viewModel.uiState.collectAsStateWithLifecycle().value.isNetworkAvailable?.or(false) != true && …) {
    navToHome()
}
```

```kotlin
// ✅ CORRECTO — ARC-01/02: el VM calcula el estado (Loading | UpToDate | ForceUpdate | Blocked | Maintenance);
// el Screen reacciona en LaunchedEffect(state) y delega en un SplashContent stateless.
```

### V-08 — VM nuevo sin Hilt, sin `Content` y sin test (FF-ARC-06/09, FF-COV-01)

Archivos: `SplashViewModel.kt:25`, `SplashScreen.kt:44`

```kotlin
// ❌ INCORRECTO
class SplashViewModel @Inject constructor() : ViewModel()
val viewModel = viewModel { SplashViewModel() }
```

```kotlin
// ✅ CORRECTO — ADR-016
@HiltViewModel
class TkdSplashViewModel @Inject constructor(private val repository: TkdAppConfigRepository) : ViewModel()
// Screen: viewModel: TkdSplashViewModel = hiltViewModel() · test en app/src/test/.../TkdSplashViewModelTest.kt
```

### V-09 — URL de Play Store de otra aplicación

Archivo: `gradle.properties`

```properties
# ❌ INCORRECTO
kensho.playstore=https://play.google.com/store/apps/details?id=com.warriorsdev.travel.guide.sonora
# ✅ CORRECTO
kensho.playstore=https://play.google.com/store/apps/details?id=dev.treecode.kensho
```

### V-10 — Código copiado de otro proyecto

Archivos:
- `commons/Constants.kt` (295 líneas): constantes sin uso de límite de crédito, RFC, montos demo, *skeletons*, *steppers*; `lateinit var appContext: Context` que nunca se asigna (crash si se lee y fuga de `Context` si se asigna); texto de usuario `"Todos los estatus disponibles"`, `"Compartir imagen"`.
- `core/AppLogger.kt`: KDoc menciona AppDome y Crashlytics (ausentes en Kensho); la tabla de build types está invertida (`debug → DEBUG false`, `release → true`); duplica a `KenshoLogger` (dos wrappers sobre Timber).
- `components/atoms/WebViewPageComponent.kt:21`: KDoc cita `https://invex.com/`.

```kotlin
// ✅ CORRECTO — conservar solo lo que se usa; un único logger (KenshoLogger, ADR-012); sin Context global.
```

### V-11 — `WebView` inseguro e ineficiente

Archivos: `components/atoms/WebViewPageComponent.kt:29-47`, `components/molecules/TdkWebPage.kt:31, 40`

```kotlin
// ❌ INCORRECTO
settings.javaScriptEnabled = true          // sin restringir dominio; WebViewClient() permite navegar a cualquier sitio
settings.domStorageEnabled = true
settings.useWideViewPort = true            // duplicado
update = { it.loadUrl(url) }               // recarga en cada recomposición
WebViewPageComponent(modifier = modifier.verticalScroll(rememberScrollState()), …)  // modifier aplicado 2 veces + WebView en scroll
```

```kotlin
// ✅ CORRECTO — JS solo si la página lo requiere; WebViewClient que limite a treecode.dev y abra lo demás fuera;
// update = { if (it.url != url) it.loadUrl(url) }; modifier solo en la raíz; renombrar TdkWebPage → TkdWebPage.
```

### V-12 — Literales de tema fuera de `ui/theme/` (FF-THM-01/02/03, ADR-004)

Archivos: `components/molecules/FloatingDialog.kt:77-111`, `components/atoms/PrimaryButton.kt:42, 65, 83, 92`, `components/atoms/TextIcon.kt:38, 78-80`, `components/atoms/HtmlText.kt:86, 121`, `ui/TkdApp.kt:100, 123`, `ui/theme/Color.kt`

```kotlin
// ❌ INCORRECTO
.background(Color.White, shape = RoundedCornerShape(16.dp))
style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 22.sp)
modifier = Modifier.size(24.dp)
containerColor = neutralWhite   // Color.kt de plantilla reactivado (empeora TD-KEN-003)
```

```kotlin
// ✅ CORRECTO — TkdColors / TkdDimens / TkdTypography por rol
.background(TkdColors.Surface, shape = RoundedCornerShape(TkdDimens.RadiusLarge))
style = TkdTypography.TitleLarge
```

### V-13 — Texto de usuario literal (FF-I18N-01, ADR-009)

Archivos: `components/logicComponents/BlockedDialog.kt:34, 42, 54`, `NoInternetDialog.kt:22, 29`, `model/TkdDashboardUiState.kt:97-100`, `ui/TkdApp.kt:101, 105`

```kotlin
// ❌ INCORRECTO
title = "Sin conexión a internet"
Overview(label = "Overview", icon = R.drawable.ic_tkd_nav_overview)   // inglés y usado como contentDescription
```

```kotlin
// ✅ CORRECTO
title = stringResource(R.string.tkd_network_offline_title)
Overview(labelRes = R.string.tkd_nav_overview, icon = R.drawable.ic_tkd_nav_overview)
```

### V-14 — Componentes públicos sin preview, sin `modifier` o con orden incorrecto (FF-CMP-01/02/09)

- Sin `@Preview`: `CenterComponent`, `LoaderComponent`, `HtmlText`, `StyledText`, `TextIcon`, `WebViewPageComponent`, `BlockedDialog`, `NetworkAvailable`, `ShowForceUpdateDialog`, `ShowMaintenanceDialog`, `FloatingDialog`, `TdkWebPage`, `SplashScreen`.
- Sin `modifier: Modifier = Modifier`: `CenterComponent.kt:18`, `LoaderComponent.kt:11`.
- Opcional antes de `modifier`: `feature/auth/signup/TkdSignUpSections.kt:47` (`onBack`).
- Previews públicas: `HtmlText.kt:199`, `PrimaryButton.kt:105`, `NoInternetDialog.kt:38`, `MainActivity.kt:41`.

### V-15 — Estructura y nombres (FF-ST-01/02)

- `components/logicComponents/` no existe en la convención (`atoms/` | `molecules/`).
- 20 composables públicos nuevos sin prefijo `Tkd` (`PrimaryButton`, `FloatingDialog`, `HtmlText`, `SplashScreen`…).
- 3 archivos > 300 líneas (`KenshoMaxFileLines`).

### V-16 — Duplicación (FF-DUP-01)

- `BlockedDialog.kt:5-54` ↔ `ShowForceUpdateDialog.kt:6-50` ↔ `ShowMaintenanceDialog.kt` (bloques de 6–10 líneas).
- `nav/ColorsNavScaffold.kt:30` ↔ `:60`.
- `feature/auth/login/TkdLoginScreen.kt:162` ↔ `feature/auth/signup/TkdSignUpScreen.kt:239`.

### V-17 — Dependencias (FF-DEP)

- `androidx-material-icons-extended` (librería pesada) solo se usa en una preview (`PrimaryButton.kt:109`) y en el código muerto `AppNavigation.kt`.
- Líneas comentadas en `libs.versions.toml` (`#androidx-material3…`, `#lottie-compose`) y en `app/build.gradle.kts`; versión `lottie` declarada sin uso.

### V-18 — Lógica pura sin test (FF-COV-02)

Archivo: `data/remote/remoteConfig/VersionPolicy.kt` — `getUpdateStatus`, `compareVersions`, `isVersionBlocked`.

---

## 4. 🟡 Observaciones

- **Commits:** los 21 no siguen `<tipo>(<ámbito>): <descripción>` (p. ej. *"Add styled text component…"*). Recomendado squash con mensaje válido.
- **`feature/auth`:** `TkdLoginScreen.kt:35` importa `feature.auth.signup.TkdSignUpHeader`. Confirmar y documentar si login/signup son un solo feature `auth`.
- **Tests en paquete antiguo:** `app/src/test/.../login/` y `.../signup/` mientras el código está en `feature/auth/…`.
- **Composables > 60 líneas:** `SignUpForm` (114), `SplashScreen` (100), `TkdTermsConsent` (86), `LoginForm` (80), `TkdDashboardContent` (89), `TkdTopBar` (78), `TkdPaymentsContent` (77).
- **Eventos sin test:** `TkdLoginViewModel` (3: `onPasswordChange`, `onRememberMeChange`, `onTogglePasswordVisibility`), `TkdSignUpViewModel` (6).
- **Recursos:** `ic_logo_kensho.xml` fuera de `ic_tkd_*` y sin cabecera *"Generado desde Figma"* (FF-RES-02, 🟠).
- **Claves de strings:** 8 fuera de `tkd_<feature>_…` (`text_label_style_*`, `html_text_redimir`).
- **`contentDescription` literal:** `PrimaryButton.kt:78, 93` (`"Button Icon"`), `TextIcon.kt:106` (`""`).
- **Sin uso:** `StyledText`, `TextIcon`, `NetworkAvailable`, `feature/home/.gitkeep`, previews privadas no referenciadas (`UnusedPrivateMember`).
- **Token sin uso:** `TkdTypography.BodyXLarge`.

---

## 5. ✅ Fortalezas

- Capa de red/sesión conforme a ADR-016/017: `NetworkTkdAuthRepository` detrás de interfaz con `@Binds`; `TkdResult` + `TkdApiCall` con mapeo de errores HTTP; `Authenticator` que cierra sesión si no renueva; token cifrado con Tink en DataStore.
- `BASE_URL` por `gradleProperty`; cleartext apagado en `main` (solo `debug` lo sobrescribe); `logging-interceptor` como `debugImplementation`.
- 39 `@Test` nuevos, 0 eliminados; `MockWebServer` y fixtures JSON.
- Fragmentos de `changelog.d/` con nombre válido.
- Sin `!!`, `runBlocking`, `GlobalScope` ni `LocalDate.now()` en líneas nuevas.

---

## 6. Comandos ejecutados

| Comando | Resultado |
|---------|-----------|
| `./gradlew detekt` | ❌ 64 hallazgos ponderados |
| `./gradlew :app:testDebugUnitTest` | ❌ 47 tests, 1 fallo |
| `./gradlew :app:assembleDebug` | ⏭️ no ejecutado (falló la tarea previa en la misma invocación) |
| `./gradlew :app:connectedDebugAndroidTest` | ⏭️ no ejecutado (requiere emulador) |

---

## 7. Qué se necesita para aprobar

- [ ] V-01 — Detekt sin hallazgos nuevos; alinear `DojoDiscipline` con el test y `docs/api/kensho-api.yaml`
- [ ] V-02 — DTO de remote config detrás de mapper → modelo en `model/`
- [ ] V-03 — `TkdSignUpHeader` a `components/molecules/` o como slot
- [ ] V-04 — Quitar `printStackTrace` y usar `KenshoLogger`
- [ ] V-05 — Justificar o etiquetar los 8 `contentDescription`
- [ ] V-06 — Eliminar `nav/AppNavigation.kt`
- [ ] V-07 / V-08 — Completar el Splash (`@HiltViewModel`, `Content`, efectos en `LaunchedEffect`, test) o sacarlo del PR
- [ ] V-09 — Corregir la URL de Play Store
- [ ] V-10 — Limpiar `Constants.kt`, `AppLogger.kt` y la referencia a `invex.com`
- [ ] V-11 — Asegurar el `WebView`
- [ ] V-12 / V-13 / V-14 — Tokens, `strings.xml`, previews y `modifier` en componentes nuevos
- [ ] V-15 — Ubicar `logicComponents` en `atoms/`/`molecules/` con prefijo `Tkd`
- [ ] V-16 — Unificar los diálogos repetidos
- [ ] V-17 — Quitar `material-icons-extended` y líneas comentadas del catálogo
- [ ] V-18 — Unit test de `VersionPolicy`

---

## 8. Recomendación

Dividir el PR:

1. **PR de auth (ADR-016/017):** la migración está bien resuelta; para aprobar basta con V-01, V-04 y V-05.
2. **PR de versionado/mantenimiento:** Splash, `logicComponents/`, `AppNavigation`, `Constants`, `AppLogger` y `WebView`, que hoy están incompletos o copiados de otro proyecto.

---

*Resultado de la validación automatizada `/validate-pullrequest` (Kensho). No publicado en Bitbucket: la sesión no tenía MCP de Bitbucket configurado.*
