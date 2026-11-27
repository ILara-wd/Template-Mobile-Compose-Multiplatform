<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde CLAUDE.md.
     NO EDITAR A MANO — todo cambio debe hacerse en CLAUDE.md y luego
     ejecutar `python3 .github/scripts/sync_claude_to_copilot.py`. -->

# GitHub Copilot — instrucciones del repositorio

> Este archivo es el espejo íntegro de `CLAUDE.md` para que GitHub Copilot
> (Copilot Chat en Android Studio/VS Code, Copilot CLI y Copilot Code Review
> en Pull Requests) reciba el mismo contexto arquitectónico que Claude Code.
> Los **prompts** (`/validate-pullrequest`, `/kensho-governance`,
> `/tech-story-architect`) están en `.github/prompts/` y el **agente**
> `kensho-android-dev` en `.github/agents/`, también generados desde
> `.claude/`. Los sub-comandos namespaced (ej.
> `.claude/commands/validate-pullrequest/ff-a11y.md`) se replican como
> subdirectorios paralelos bajo `.github/prompts/`.

---

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> **¿Por qué existe este archivo?**
> Sintetiza en un solo lugar lo que Claude Code necesita para ser productivo de inmediato en **Kensho**: comandos, arquitectura, convenciones de Compose, flujo Figma → código, deuda técnica y el mapa del corpus de `docs/architecture/`. Se actualiza **al final de cada cambio arquitectónico relevante** y siempre refleja el estado consolidado del corpus.

---

## Qué es Kensho

App Android para administrar un dojo de Taekwondo: resumen del dojo, alumnos y su perfil, pase de lista, pagos y detalle de cobro, inicio de sesión y registro. Las pantallas se implementan desde el prototipo de Figma **Prototipo-Demo-UiKit** con datos de repositorios *Fake*. El proyecto está en la **Fase 1 — datos reales**: Hilt (ADR-016) y la capa de red/sesión (ADR-017) están implementados en todos los repositorios; del resumen del dojo, lo visible sale de `me/profile` y 4 secciones ocultas esperan su endpoint (TD-KEN-013); aún no hay CI, caché offline ni módulos.

| Dato | Valor |
|------|-------|
| Módulos Gradle | `:app` · `:lint-rules` (reglas Detekt, JVM) |
| Namespace / applicationId | `dev.treecode.kensho` |
| compileSdk / targetSdk / minSdk | 37 / 37 / 26 |
| Kotlin / AGP / JVM | 2.2 / 9.4 / 11 |
| UI | Jetpack Compose (BOM 2025.12) · Material 3 · Navigation Compose 2.8 |
| Fuente | Plus Jakarta Sans (`res/font/`, SIL OFL) |
| Repositorio | Bitbucket `warriors_dev_platforms/kensho` · integración en `develop`, releases en `main` |

---

## Comandos principales

```bash
./gradlew :app:assembleDebug               # compilar
./gradlew :app:installDebug                # instalar en emulador/dispositivo
./gradlew :app:testDebugUnitTest           # unit tests (JVM)
./gradlew :app:connectedDebugAndroidTest   # Compose UI tests (requiere emulador)
./gradlew :app:lintDebug                   # Android Lint
./gradlew detekt                           # Detekt + reglas Kensho* de :lint-rules (baseline en config/detekt/)
./gradlew :lint-rules:test                 # tests de las reglas Kensho*
towncrier build --config config/towncrier.toml --version <x.y.z> --draft   # vista previa del changelog

git config core.hooksPath .githooks        # activar validación de mensajes de commit (una vez)
```

Verificación visual: emulador `Pixel_8_Pro_API_33`; `adb shell uiautomator dump` para localizar nodos antes de tocar, `adb exec-out screencap -p` para comparar con la captura de Figma.

---

## Arquitectura

**MVVM + flujo unidireccional** dentro de un solo módulo, con paquetes por feature (ADR-001, ADR-002):

```
dev.treecode.kensho/
├── MainActivity.kt → KenshoApp → ui/TkdApp.kt   # NavHost: login ↔ signup → tabs (Overview, Students, CheckIn, Payments)
│                                                #   student/{studentId}, payment/{transactionId}
├── ui/theme/        # TkdColors, TkdDimens, TkdTypography, TkdBeltPalette — ÚNICOS literales
├── ui/TkdSenseiViewModel.kt  # base de los VMs con datos del instructor (ADR-021)
├── ui/present/      # resumen del dojo (Overview)
├── login/ signup/ students/ profile/ checkin/ payments/
│                    # Tkd<X>Screen.kt (Screen + Content + Actions + previews)
│                    # Tkd<X>Sections.kt (secciones internal) · Tkd<X>ViewModel.kt
├── components/atoms/      # piezas sin dominio: TkdPill, TkdSwitch, TkdAvatar, TkdCheckbox…
├── components/molecules/  # TkdTopBar, TkdBottomNavBar, TkdInputField, TkdStudentCard…
├── model/           # Tkd<X>UiState y modelos @Immutable
├── data/            # interface Tkd<X>Repository + FakeTkd<X>Repository (datos del prototipo) + NetworkTkd<X>Repository
│   ├── remote/      # api (Retrofit) · dto (@Serializable) · mapper — nunca sale de data/ (ADR-017)
│   └── local/       # TkdSessionStore (DataStore + Tink) · TkdProfileCache (perfil del instructor, ADR-021)
├── di/              # módulos Hilt: Data, Network, Storage, Dispatchers (ADR-016)
└── core/            # utilidades puras (Validate)
```

### Patrón de pantalla (referencia: `payments/`)

```kotlin
/** Acciones que la pantalla delega al llamador (navegación, otras apps). */
data class TkdPaymentDetailActions(val onBack: () -> Unit = {}, val onChanged: () -> Unit = {}, val onOpenUri: (String) -> Unit = {})

/** Eventos que resuelve el ViewModel. */
data class TkdPaymentDetailCallbacks(val onRetry: () -> Unit = {}, val onConfirm: () -> Unit = {} /* … */)

@Composable
fun TkdPaymentDetailScreen(                       // stateful: único que conoce al VM
    modifier: Modifier = Modifier,
    actions: TkdPaymentDetailActions = TkdPaymentDetailActions(),
    viewModel: TkdPaymentDetailViewModel = hiltViewModel(),   // el id de la ruta llega por SavedStateHandle
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TkdPaymentDetailContent(state, actions, TkdPaymentDetailCallbacks(onConfirm = viewModel::onConfirm /* … */), modifier)
}

@Composable
fun TkdPaymentDetailContent(state: TkdPaymentDetailUiState, actions: TkdPaymentDetailActions, callbacks: TkdPaymentDetailCallbacks, modifier: Modifier = Modifier)
```

- La navegación sale por `Actions` hacia las rutas de `ui/` (`TkdApp`, `Tkd<X>Routes.kt`); ningún feature recibe `NavController` ni importa otro feature. Una pantalla que cambia datos de la anterior le deja una llave en su `SavedStateHandle` para que recargue (`ui/TkdPaymentRoutes.kt`).
- "Hoy" viene del API (`asOf`), nunca de `LocalDate.now()` en modelos, VMs o UI.
- Dinero en **centavos `Int`** con la `Currency` que manda el API; formato con `core/TkdFormat.kt` (`centsAsMoney`) y `payments/TkdPaymentsFormat.kt` (`asMoney`), `Locale` es-MX.

### Datos reales: Hilt + red (ADR-016, ADR-017)

```kotlin
@HiltViewModel
class TkdPaymentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,                 // argumentos de ruta
    private val repository: TkdPaymentsRepository,      // sin default; di/DataModule elige Network o Fake
) : ViewModel() { /* Loading → Content | Error(@StringRes) ; onRetry() */ }

interface TkdPaymentsRepository { suspend fun paymentDetail(id: String): TkdResult<PaymentDetail> }
// NetworkTkdPaymentsRepository: withContext(@IoDispatcher) { try … catch (CancellationException) { throw } catch (Exception) { Failure(toDataError()) } }
```

- `Screen` usa `hiltViewModel()`; los tests de VM lo construyen directo con la *Fake* y `StandardTestDispatcher`.
- Retrofit + OkHttp + kotlinx.serialization; DTO y `Tkd<X>Api` solo en `data/remote/`; el mapper convierte códigos → enums, ISO-8601 → `java.time`, URL → `TkdImage`.
- Errores: `TkdResult.Failure(error, serverMessage)` → `failure.toUiText(fallback)` → `TkdUiText` (el `message` del API si vino, si no `tkd_error_*`, ADR-020); nunca `exception.message` en UI.
- Sesión: token en DataStore + Tink, `Interceptor` + `Authenticator` de OkHttp; `TkdApp` observa `TkdAuthRepository.session` para volver a `login`.
- Perfil del instructor (ADR-021): `GET me/profile` se guarda cifrado en `TkdProfileCache` (por cuenta, se borra al cerrar sesión, vale mientras su `asOf` sea de hoy) y solo lo escribe `NetworkTkdSenseiProfileRepository` (`profile(refresh)`, `observeProfile()`). La app no llama a `GET me`. Un VM con encabezado del dojo o agenda hereda de `TkdSenseiViewModel` y en `init` usa `onSenseiProfile { p -> _uiState.update { it.copy(header = p.header) } }` (ARC-15, FF-17).
- Lista de alumnos (ADR-022): `GET students` con los tres estados se guarda cifrada en `TkdStudentsCache` (por cuenta, se borra al cerrar sesión); `TkdStudentsViewModel` muestra `savedRoster()` al instante y `refreshRoster()` la reemplaza; estado, búsqueda y filtros en local. `roster(status)` sigue sin guardar.
- Fichas de alta sin terminar (ADR-023): `saveDraft` las guarda cifradas en `TkdStudentDraftStore` (por cuenta, sin API aún); la lista muestra "Retomar ficha" y la ruta `students/register?draftId=` las abre llenas; el alta las descarta.
- Crashes (ADR-024): Firebase Crashlytics solo recolecta en release; `app/google-services.json` no se versiona y sin él no compila release. Errores manejados vía `AppLogger` (`crashReporter`): sin datos personales en los mensajes ni `setUserId`.
- Eventos de uso (ADR-025): el VM recibe `TkdAnalytics` y llama `analytics.track(TkdAnalyticsEvent.X)` solo tras `TkdResult.Success`; eventos nuevos solo en `core/TkdAnalyticsEvent`, sin datos personales ni montos. Release → Firebase; debug → `TkdAnalytics.None`; tests → `RecordingTkdAnalytics`. Sin ID de publicidad.
- Push (ADR-027): FCM solo en `data/push/` (`TkdMessagingService`, `TkdNotifier`, `TkdPushChannel` `account`/`news`/`updates`); `TkdPushRegistrar` sigue la sesión y `NetworkTkdPushRepository` hace `PUT me/devices/{installationId}` (huella diaria) y `DELETE` en `signOut`. El toque llega a `MainActivity.openPush` (`singleTop` + `click_action` `OPEN_PUSH`); solo abre `url` `https` de `treecode.dev`. Permiso una vez en el Resumen (`TkdPushPermissionScreen`). El token de FCM nunca en logs ni en disco.
- Splash y configuración de la app: splash del sistema (`Theme.Kensho.Starting`, core-splashscreen) hasta el primer cuadro y luego `feature/splash/TkdSplashScreen`, que arranca igual y reproduce el Lottie `res/raw/lottie_tkd_splash.json` (ADR-026, generado con `scripts/lottie/`), antes de `TkdApp`; `GET app/settings` (público) decide mantenimiento → actualización obligatoria (`min_supported`/`blocked`) → recomendada (se cierra una vez por versión). Sin red o tras 5 s sigue con la última guardada sin su mantenimiento (`TkdAppSettingsStore`). Banderas: `TkdAppSettingsRepository.observeAppSettings()` y `isEnabled("ff_x")` (solo `FTA`).
- Firma de release: keystore y contraseñas en `signing.properties` (raíz, no versionado; plantilla `signing.properties.example`) o variables `KENSHO_RELEASE_*` en CI. Nunca en `gradle.properties` ni en el repo (`signing_app/` está en `.gitignore`).
- `BASE_URL` por `buildConfigField`; sin cleartext; `HttpLoggingInterceptor` `BASIC` solo en debug con `redactHeader("Authorization")`.
- Contrato del API en `docs/api/kensho-api.yaml`; fixtures en `src/test/resources/fixtures/`; repositorios de red con `MockWebServer`.

---

## Convenciones del proyecto

- **Tokens:** color de `TkdColors`, medidas de `TkdDimens`, texto de `TkdTypography`, cinturones de `TkdBeltPalette`. Nunca `Color(0x…)`, `N.dp` o `N.sp` fuera de `ui/theme/` (se permiten `Color.Unspecified` y `Color.Transparent`). Token nuevo = nombre por rol (`WarningContainerSoft`), no por valor.
- **Componentes:** buscar en `components/` antes de crear. Público ⇒ prefijo `Tkd`, KDoc de una línea en español, `modifier: Modifier = Modifier` como primer opcional aplicado a la raíz, ≥ 1 `@Preview` `private`. Variantes con objeto de estilo + defaults (`TkdInputFieldStyle`/`TkdInputFieldDefaults.Note`) o slots, no con booleanos. Parámetro nuevo en componente existente: al final y con default.
- **Secciones** de una sola pantalla: `internal`/`private` en el paquete del feature.
- **Estado:** `UiState` `@Immutable` con `val`; derivados como propiedades (`visibleTransactions`, `totalCents`); `collectAsStateWithLifecycle`; `key` en `items(...)`.
- **Textos:** `stringResource` siempre; claves `tkd_<feature>_<descripcion>`; bloques de `strings.xml` con comentario `<!-- Pantalla (Figma: Prototipo-Demo-UiKit, nodo X:Y) -->`. Idioma base **español de México**; las cadenas en inglés existentes son deuda (TD-KEN-011). Sin concatenar ni `String.format` sin locale.
- **Accesibilidad:** `contentDescription` desde recursos (`tkd_cd_*`); `null` solo decorativo **con comentario en la misma línea**. Selección/toggle sobre la fila completa con `Role`. Objetivos táctiles ≥ 48dp.
- **Escala de fuente (ADR-015):** el texto escala hasta 200 % sin código; nunca multiplicar por `fontScale`. Contenedores con texto en `heightIn(min)`, nombre + badge en `FlowRow`; topes solo con `ui/theme/TkdFontScale.kt` (`withFontScale(TkdFontScale.Limited)`, `TkdFontScaleLimit`, `shrinkToFit()`) y comentario. Componentes con texto: preview `@TkdFontScalePreviews`; pantallas nuevas revisadas en emulador a 150 % y 200 % con `ANDROID_SERIAL=emulator-5554` (no instalar en teléfonos conectados).
- **Recursos de Figma:** `ic_tkd_*.xml` convertidos 1:1 del SVG con cabecera `<!-- Generado desde Figma (...). No editar a mano. -->`; fotos `img_tkd_*`. Reutilizar un drawable solo si su path data es idéntico. Nunca URLs de Figma en el código.
- **Prohibido en `src/main`:** `!!`, `GlobalScope`, `runBlocking`, `android.util.Log`, `println`, código comentado.
- **Tamaño:** archivos nuevos ≤ 300 líneas; composables > 60 líneas se dividen.
- **Tests:** todo ViewModel nuevo o modificado con unit test en `app/src/test` usando la *Fake*; flujos interactivos con Compose UI Test sobre `Content`; mappers con fixtures JSON y repositorios de red con `MockWebServer` (TEST-06). Nombres `sujeto_condicion_resultado`.
- **Dependencias:** solo vía `gradle/libs.versions.toml`; Hilt (ADR-016), Retrofit/OkHttp/kotlinx.serialization/DataStore/Tink/Coil/Timber (ADR-017), Firebase Crashlytics/Analytics/Messaging (ADR-024/025/027), core-splashscreen y Lottie (ADR-026) aprobados; cualquier otro framework (Room, Ktor, Koin…) requiere ADR (ver disparadores en `01-governance.md §9`).
- **DI y red (ADR-016/017):** VM nuevo/migrado `@HiltViewModel` sin `= Fake…()` (FF-15 solo baja); `retrofit2`/`okhttp3`/`data.remote` solo en `data/` y `di/` (FF-14); sin `Dispatchers.IO` literal fuera de `di/` (FF-16). Datos del instructor solo vía `TkdSenseiViewModel`, sin `GET me` ni `profile()` desde el VM ni caché propia; única herencia permitida entre VMs, de un nivel (ADR-021, FF-17).
- **Commits:** `<tipo>(<ámbito>): <descripción en imperativo>` con tipo `feat|fix|refactor|test|docs|chore|build|revert` y ticket opcional `[KEN-<n>]`. Commit solo cuando se pida.
- **Changelog (GIT-05):** un PR que cambia `app/src/main` agrega `changelog.d/<KEN-n|TD-KEN-0XX|+slug>.<added|changed|refactored|deprecated|removed|fixed|security>.md` con una línea en español; `CHANGELOG.md` lo genera towncrier al publicar, nunca a mano (`changelog.d/template/TOWNCRIER_README.md`).
- **Lint:** `./gradlew detekt` sin hallazgos nuevos; el baseline (`config/detekt/detekt-baseline.xml`) solo se encoge. Supresión puntual con `@Suppress("Kensho…")` + comentario con la regla y el motivo.

---

## Flujo Figma → código

1. Cargar la skill `figma:figma-design-to-code`, `create_session` y `get_design_context` del nodo (con captura).
2. Mapear el diseño a componentes existentes de `components/` y a tokens con `/catalogo-ui consultar` (`docs/ui-catalogo/index.html`); agregar tokens/estilos solo si faltan.
3. Descargar assets, convertir SVG → VectorDrawable, comparar contra drawables existentes con `diff`.
4. Implementar `UiState` → `Fake` repositorio → ViewModel → `Screen`/`Content`/`Sections` → ruta en `TkdApp`.
5. Compilar, instalar y comparar en emulador contra la captura de Figma; probar las interacciones. Al terminar, `/catalogo-ui actualizar <id>` agrega la pantalla y sus capturas al catálogo.

---

## Deuda técnica (`TD-KEN-0XX`, línea base 2026-10-27)

| ID | Descripción | Severidad |
|----|-------------|-----------|
| TD-KEN-001 | ✅ Saldada (2026-11-19): 22/22 ViewModels con unit test (incluida `TkdSenseiViewModel`); el splash inalcanzable se quitó. `Validate` sigue en TD-KEN-010 | 🟠 Alto |
| TD-KEN-002 | 65 `contentDescription = null` sin comentario (31 archivos) | 🟡 Medio |
| TD-KEN-003 | Restos de plantilla: `Color.kt`/`Theme.kt`/`Type.kt`, código comentado y `AppDestinations` en `MainActivity`, dependencia duplicada, recursos sin uso, `startDestination` en Overview (login inalcanzable) | 🟡 Medio |
| TD-KEN-004 | 4 colores y 1 `.dp` literales fuera de `ui/theme/`; `"%02d".format` sin locale | 🟢 Bajo |
| TD-KEN-005 | ✅ Saldada (2026-11-09): 0 `LocalDate.now()`/`LocalTime.now()` en `model/`, VMs y UI; Pagos y pase de lista usan el `asOf` del API | 🟢 Bajo |
| TD-KEN-006 | 4 archivos > 300 líneas (máx. `TkdPaymentsSections.kt`, 667) y ~28 composables > 60 líneas | 🟡 Medio |
| TD-KEN-007 | Resumen en `ui/present/`, `CustomLinearProgress` sin prefijo `Tkd`, README raíz desactualizado | 🟢 Bajo |
| TD-KEN-008 | Detekt + 15 reglas `Kensho*` activos con baseline (2026-10-28); faltan ktlint/compose-rules, Android Lint en CI y pipeline | 🟠 Alto |
| TD-KEN-009 | Dependencias desalineadas (core-ktx, lifecycle, activity), artefactos lifecycle-compose transitivos, `mavenLocal()` | 🟡 Medio |
| TD-KEN-010 | `Validate.kt` con regex de email permisiva y sin tests | 🟢 Bajo |
| TD-KEN-011 | Idioma mixto en `strings.xml` (≈130 cadenas en inglés) | 🟡 Medio |
| TD-KEN-012 | ✅ Saldada (2026-11-19): 0 defaults `= Fake…()` (FF-15), 8/9 repositorios con red, fotos, logo del dojo y miniaturas por URL con `TkdImage` + Coil (`TkdPhoto`); el resumen pasa a TD-KEN-013 | 🟠 Alto |
| TD-KEN-013 | Resumen del dojo en parte sin red (2026-11-22): encabezado, acciones rápidas, agenda, avisos e ingresos salen de `me/profile`; sesión en vivo, métricas, filtro y exámenes siguen en `FakeTkdDashboardRepository` (síncrona, sin carga/error, 2 `@DrawableRes`) y solo se pintan con `AUTH_STATIC_MOCK`, apagada en debug y release y que también controla el mock del login. Esas 4 secciones esperan `GET /dojo/overview` (`docs/api/dashboard-api.md`) | 🟢 Bajo |

**Fortalezas:** 0 `!!` · 0 logs directos · 0 `runBlocking`/`GlobalScope` · 8/8 pantallas con `collectAsStateWithLifecycle` · 32/32 modelos de `model/` `@Immutable` · separación `Screen`/`Content` en las 8 pantallas.

> Regla del boy scout: un PR que toca un archivo con deuda no la empeora. Detalle y comandos de medición en `docs/architecture/01-governance.md §6–7`.

---

## Mapa de documentación (`docs/architecture/`)

| Necesito… | Documento |
|-----------|-----------|
| Entender por qué se diseña así | [`02-best-practices.md`](docs/architecture/02-best-practices.md) |
| Las decisiones (ADR), la deuda y las fitness functions | [`01-governance.md`](docs/architecture/01-governance.md) |
| Citar una regla en un PR (🔒/⚠️) | [`03-standards.md`](docs/architecture/03-standards.md) |
| Ejemplo correcto/incorrecto (AP-XX) | [`04-detailed-rules.md`](docs/architecture/04-detailed-rules.md) |
| Revisar un PR paso a paso | [`05-code-review-guide.md`](docs/architecture/05-code-review-guide.md) |
| Estado real del código | [`06-compliance-report.md`](docs/architecture/06-compliance-report.md) |
| Plantilla y backlog de historias | [`07-user-stories.md`](docs/architecture/07-user-stories.md) |
| Lint (Detekt + reglas `Kensho*`) y plan de CI | [`08-lint-rules.md`](docs/architecture/08-lint-rules.md) · [`lint-rules/README.md`](lint-rules/README.md) |
| Changelog y fragmentos | [`changelog.d/template/TOWNCRIER_README.md`](changelog.d/template/TOWNCRIER_README.md) |
| Escala de fuente 150/200 % | [`FONT_SCALE_ANALISIS.md`](docs/architecture/FONT_SCALE_ANALISIS.md) |

### Propagación de un cambio arquitectónico

```
Hallazgo / decisión
  → 06-compliance-report.md   registrar archivo:línea y severidad
  → 01-governance.md          ADR nuevo (inmutables) y/o TD-KEN-0XX
  → 03-standards.md           regla 🔒/⚠️
  → 04-detailed-rules.md      anti-patrón AP-XX
  → 05-code-review-guide.md   grep + plantilla de comentario
  → 08-lint-rules.md          regla automatizable (si aplica)
  → 02-best-practices.md      si cambia el "porqué"
  → CLAUDE.md                 al final; luego python3 .github/scripts/sync_claude_to_copilot.py
```

- Un cambio que afecta a 2+ documentos se propaga en el mismo PR.
- Skills del repo: `/kensho-governance` (contexto y propagación), `/validate-pullrequest` (auditoría de PR por familias `ff-*`), `/tech-story-architect` (historias y liquidación de deuda), `/kensho-endpoint` (endpoint nuevo desde path + JSON: contrato, DTO, Api, mapper, repositorio de red, DI y tests), `/catalogo-ui` (catálogo HTML de pantallas, componentes y tokens con capturas del emulador; scripts en `.claude/scripts/ui-catalogo/`); agente `kensho-android-dev`.
- `AGENTS.md` y `.github/` se regeneran desde `CLAUDE.md` y `.claude/`; no se editan a mano.
