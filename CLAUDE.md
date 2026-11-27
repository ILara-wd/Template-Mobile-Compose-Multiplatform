# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> **¿Por qué existe este archivo?**
> Sintetiza en un solo lugar lo que Claude Code necesita para ser productivo de inmediato en **SportKid**: comandos, módulos, convenciones de Compose, flujo Figma → código y deuda técnica. El detalle del sistema de diseño (tokens, átomos M/O, pantallas P01–P21, vocabulario de estados) vive en [`README.md`](README.md); este archivo no lo repite, lo resume y apunta ahí. Se actualiza **al final de cada cambio de arquitectura o de convención**.

---

## Qué es SportKid

App Android para que los dos padres lleven la agenda, el progreso y los gastos de sus hijos en **Taekwondo**, **Natación** y **Atletismo**, y compartan el perfil de cada deportista con un QR. Las pantallas se implementan desde el prototipo de Figma **Prototipo-Demo-UiKit** (archivo `KqTFktvrRvCzO2idPga5xf`, generado con Stitch) con **datos de ejemplo** (`<Feature>Samples`). El proyecto está en la **fase de UI Kit**: P01–P21 están implementadas; aún no hay ViewModels, capa de datos, persistencia (salvo `ThemePrefs`), autenticación, Navigation Compose, DI ni tests.

| Dato | Valor |
|------|-------|
| Módulos Gradle | `:app` · `:core:designsystem` · `:core:model` · `:core:ui` · `:feature:{auth, athlete, agenda, home, tkd, swimming, athletics, sharing, settings, expenses}` |
| Namespace / applicationId de `:app` | `com.invex.one.template` (heredado de la plantilla; los módulos usan `com.invex.sportkid.*`) |
| compileSdk / targetSdk / minSdk | 36 / 36 / 24 (con *core library desugaring* para `java.time`) |
| Kotlin / AGP / JVM | 2.0.21 / 8.13.2 / 11 |
| UI | Jetpack Compose (BOM 2024.09) · Material 3 · MaterialKolor (temas) · ZXing (QR) |
| Fuente | Plus Jakarta Sans (fallback Roboto) |
| Repositorio | GitHub `ILara-wd/Template-Mobile-Compose-Multiplatform` · rama `sport_kids` (cada app de la plantilla vive en su rama; `main` es la plantilla) |

> Los directorios `feature/games`, `feature/profile` y `feature/records` solo tienen `build/` de otras ramas: no son módulos de SportKid.

---

## Comandos principales

```bash
./gradlew :app:assembleDebug               # compilar
./gradlew :app:installDebug                # instalar en emulador
./gradlew :app:testDebugUnitTest           # unit tests (JVM) — hoy solo el ExampleUnitTest de la plantilla
./gradlew :app:connectedDebugAndroidTest   # Compose UI tests (requiere emulador)
./gradlew lint                             # Android Lint de todos los módulos

python3 scripts/ui-catalogo/capturar.py [P14 P15…]   # capturas del emulador para el catálogo
python3 scripts/ui-catalogo/generar.py               # regenera docs/ui-catalogo/index.html
./create_component.sh SkChip                         # esqueleto de un átomo en core:designsystem
python3 .github/scripts/sync_claude_to_copilot.py   # regenera AGENTS.md y .github/ (Copilot) desde CLAUDE.md y .claude/
towncrier build --config config/towncrier.toml --version <x.y.z> --draft   # vista previa del changelog

git config core.hooksPath .githooks                  # activar validación de mensajes de commit (una vez)
./.githooks/tests/test-commit-msg.sh                 # suite del hook commit-msg
```

Verificación visual: hay un teléfono físico conectado además del emulador, así que siempre `ANDROID_SERIAL=emulator-5554`. Para abrir una pantalla directo (solo debug):

```bash
adb shell am start -S -n com.invex.one.template/.MainActivity --es sk_screen <Destination> \
  [--es sk_tab Home|Agenda|Athletes|Expenses] [--es sk_athlete mateo|sofia] [--es sk_sport TAEKWONDO] [--ez sk_quickadd true]
```

`adb shell uiautomator dump` para localizar nodos antes de tocar; `adb exec-out screencap -p` para comparar con la captura de Figma.

---

## Arquitectura

**Multimódulo por capas + features** (README §6.1):

```
app/                       com.invex.one.template
├── MainActivity.kt        # SportKidTheme + SportKidApp: navegación por estado (enum Destination + BackHandler)
│                          #   y DebugStart (extras sk_* solo en debug)
├── MainShell.kt           # T1: top bar, bottom bar (MainTab: Home, Agenda, Athletes, Expenses) y FAB
├── SportMapping.kt        # arma TkdAthlete / SwimAthlete / AthleticsAthlete desde el perfil del hijo
└── ThemePrefs.kt          # paleta y modo guardados (SharedPreferences)
core/
├── designsystem/          # SIN dominio. theme/ (Color, SportColors, Type, Shape, Spacing, AppPalette,
│                          #   SportKidTheme) · component/ (Sk*) · preview/ (@SkPreviews, TokensPreview)
├── model/                 # Kotlin puro: Sport, Belt, MedalResult, PaymentStatus, Swim, FamilyRole
└── ui/                    # CONOCE el dominio: EventCard, SportTag, BeltUi, MedalUi, AthleteSportHeader,
                           #   FormParts (FormCard, FormDateTimeFields, FeeRow, FormSaveBar), DateLabels…
feature/<x>/               com.invex.sportkid.feature.<x>
├── <X>Screen.kt           # pantalla pública + secciones private + previews
├── <X>State.kt            # (formularios / agenda) holder @Stable con rememberSaveable + Saver
└── <X>Models.kt           # modelos @Immutable del feature y object <X>Samples (datos de ejemplo)
```

**Reglas de dependencia:**
- `core:designsystem` **no** depende de `core:model` (reutilizable en cualquier app).
- `core:ui` depende de `core:designsystem` y `core:model`.
- `feature:*` depende de `core:ui` (y por él de `designsystem`/`model`), **nunca** de otro feature.
- Solo `:app` conoce a todos los features y los conecta. Si una pieza se necesita en dos features, sube a `core:ui` (o a `core:designsystem` si no tiene dominio).

### Patrón de pantalla

```kotlin
/** P15: registrar una carrera atlética. */
@Composable
fun RaceFormScreen(
    athlete: AthleticsAthlete,                       // datos ya armados por :app (SportMapping)
    onClose: () -> Unit,                             // navegación: siempre callbacks hacia :app
    onSave: (RaceDraft) -> Unit,                     // entrega el borrador; :app decide qué hacer
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),              // "hoy" inyectable para previews y capturas
    state: RaceFormState = rememberRaceFormState(RaceFormState.empty(today)), // sobrevive rotación/proceso
    // … slots opcionales (onSwitchAthlete, onPickPhoto, onCamera) con default
)
```

- Las pantallas son **stateless respecto a la app**: reciben datos y callbacks; ninguna recibe `NavController` ni importa otro feature. La navegación vive en `SportKidApp` (`Destination`, `*Return` para volver a donde se abrió).
- El estado local de formularios y filtros va en un holder `@Stable` (`AgendaState`, `NewTrainingState`, `CreateAthleteState`) con `rememberSaveable(saver = …)`.
- Datos: `object <X>Samples` en `<X>Models.kt` mientras no haya capa de datos. Mismos dos hijos en todos los mocks: **Mateo** (azul) y **Sofía** (rosa).
- Cuando lleguen datos reales, el siguiente paso es `ViewModel` + `UiState` `@Immutable` por pantalla (Screen stateful → Content stateless) y repositorios en un módulo `core:data`; eso requiere decisión explícita y actualizar este archivo.

---

## Convenciones del proyecto

- **Tokens:** color de `MaterialTheme.colorScheme` (cambia con el tema) o `LocalSportColors` (deporte, medallas, estados, hijos, cintas: **no** cambian con el tema); espaciado de `SportKidTheme.spacing` / `LocalSpacing`; texto de `MaterialTheme.typography`; formas de `MaterialTheme.shapes`. Nunca `Color(0x…)` fuera de `core/designsystem/theme/`. Token nuevo = nombre por rol, no por valor. Si Figma y el README no coinciden en un token, **manda Figma** y se actualiza el README.
- **Componentes:** buscar antes de crear (`/catalogo-ui-sportkid consultar <pieza>`). Átomos sin dominio en `core:designsystem` con prefijo **`Sk`** (`SkButton`, `SkPill`, `SkAvatar`…); piezas con dominio en `core:ui` sin prefijo (`EventCard`, `SportTag`); piezas de una sola pantalla `private` en el feature. KDoc de una línea en español (con el nodo/nombre de Figma si aplica), `modifier: Modifier = Modifier` como primer opcional aplicado a la raíz, slots `@Composable` al final, ≥ 1 preview `private` con `@SkPreviews` (claro, oscuro, fuente 1.3). Parámetro nuevo en componente existente: al final y con default.
- **Estados de dominio:** un `enum` en `core:model` y su presentación (texto, icono, color) **una sola vez** en `core:ui`. Vocabulario único de README §5 (no alternar "Pendiente de pago" con "Por pagar"). Color nunca solo: siempre con icono o texto.
- **Textos:** `stringResource` siempre, en el `strings.xml` del módulo dueño; claves `<pantalla>_<descripcion>` (`quick_add_title`, `swim_meet_*`); bloques con comentario `<!-- P05 Inicio -->` / `<!-- O4 QuickAddSheet -->`. Idioma **español de México** en toda la UI (Figma trae títulos en inglés: se traducen). `plurals` para cantidades. Sin concatenar.
- **Formatos (README §7):** dinero `$1,250 MXN` (sin `.00`), natación `00:41.23`, atletismo `28:41` / `1:02:15`, distancias `50 m` / `5 K`, fechas cortas "Sáb 14 nov" (con año solo si no es el actual) vía `DateLabels` de `core:ui`; `Locale` es-MX.
- **Accesibilidad:** `contentDescription` desde recursos en iconos con significado; `null` solo si es decorativo. Selección/toggle sobre la fila completa con `Role`. Objetivos táctiles ≥ 48dp. El texto debe soportar `fontScale` 1.3.
- **Recursos de Figma:** SVG → VectorDrawable 1:1 (solo path fill/stroke), en el módulo que lo usa: `ic_<feature>_<nombre>.xml` (`ic_home_trophy`); fotos `img_<pantalla>_<nombre>` en `drawable-nodpi`. Si el icono ya existe en `core`, se reutiliza (pestaña Assets del catálogo). Un icono por deporte vía `Sport.iconRes`. Nunca URLs de Figma en el código.
- **Prohibido en `src/main`:** `!!`, `GlobalScope`, `runBlocking`, `android.util.Log`, `println`, código comentado, emojis en la UI.
- **Fechas:** "hoy" entra como parámetro (`today: LocalDate = LocalDate.now()` solo como default de la pantalla, nunca dentro de modelos o lógica), para que previews y capturas sean deterministas.
- **Tamaño:** archivos nuevos ≤ 400 líneas; composables > 60 líneas se dividen en secciones `private`.
- **Dependencias:** solo vía `gradle/libs.versions.toml`. Cualquier librería nueva (Navigation, Hilt, Room, Retrofit, Firebase…) se discute antes y se registra aquí.
- **Commits:** `<tipo>(<ámbito>): <descripción imperativa en minúscula> (#<n>)` con tipo `feat|fix|refactor|test|docs|chore|build|revert`, ámbito = módulo (`feat(home): …`, `docs(catalogo): …`) e issue/PR de GitHub opcional al final. Lo valida `.githooks/commit-msg`. Commit solo cuando se pida.
- **Changelog:** un PR que cambia `src/main` agrega `changelog.d/<id|+slug>.<added|changed|refactored|deprecated|removed|fixed|security>.md` con una línea en español orientada al usuario; `CHANGELOG.md` lo genera towncrier, nunca a mano (`changelog.d/template/TOWNCRIER_README.md`).

---

## Flujo Figma → código

1. Cargar la skill `figma:figma-design-to-code` y `get_design_context` del nodo (con captura). Descargar los assets de inmediato: las URLs del MCP caducan en minutos.
2. Mapear el diseño a componentes y tokens existentes con `/catalogo-ui-sportkid consultar` (README §1.6, §2, §3 y `docs/ui-catalogo/index.html`); agregar tokens/estilos solo si faltan.
3. Convertir SVG → VectorDrawable y comparar contra los drawables existentes con `diff` antes de agregar uno nuevo.
4. Implementar modelos + `Samples` → (holder de estado si hay formulario) → `Screen` con secciones `private` y previews → `Destination` y callbacks en `SportKidApp`.
5. Compilar, instalar y comparar en emulador contra la captura de Figma; probar las interacciones. Al terminar: actualizar la fila de README §4.2, agregar la pantalla a `docs/ui-catalogo/pantallas.json` y correr `/catalogo-ui-sportkid actualizar <id>`.

---

## Deuda técnica (`TD-SK-0XX`, línea base 2026-11-27)

| ID | Descripción | Severidad |
|----|-------------|-----------|
| TD-SK-001 | Sin tests: solo `ExampleUnitTest`/`ExampleInstrumentedTest` de la plantilla. Faltan Compose UI tests de los holders de estado y screenshots (Roborazzi, README §6.3) | 🟠 Alto |
| TD-SK-002 | Sin capa de datos: todo sale de `*Samples`; guardar en formularios es `TODO` (32 `TODO` en el código) | 🟠 Alto |
| TD-SK-003 | Navegación por estado en `MainActivity.kt` (432 líneas, 20 `Destination`); migrar a Navigation Compose con rutas tipadas | 🟡 Medio |
| TD-SK-004 | ~515 literales `N.dp` en `feature/` y 58 en `core:ui` (README §6.2 pide `LocalSpacing`); 3 `Color(0x…)` fuera de `theme/` (`BeltUi.kt`, `ThemeScreen.kt` y una preview de `SkChip.kt`) | 🟡 Medio |
| TD-SK-005 | 19 archivos > 400 líneas (máx. `AthleteProfileScreen.kt`, 726) | 🟡 Medio |
| TD-SK-006 | 180 `contentDescription = null`; revisar que todos sean decorativos | 🟡 Medio |
| TD-SK-007 | Restos de plantilla: namespace/applicationId `com.invex.one.template`, `feature/{games,profile,records}` vacíos | 🟢 Bajo |
| TD-SK-008 | Sin análisis estático en Gradle: `config/detekt/detekt.yml` está listo pero el plugin no está aplicado; sin CI | 🟢 Bajo |
| TD-SK-009 | Esquema oscuro de Océano sin diseño en Figma (usa Theme Builder); falta `ic_belt` y logo (README §8) | 🟢 Bajo |
| TD-SK-010 | Lecturas del reloj fuera del default `today` de las pantallas: `LocalDate.now()` en `*Samples`, `Draft.empty()` y `rememberAgendaState`; `System.currentTimeMillis()` en `SettingsScreen` y `SharingModels` (`/sportkid-governance:fitness-functions`, FF-12) | 🟢 Bajo |

**Fortalezas:** 0 `!!` · 0 logs directos · 0 `runBlocking`/`GlobalScope` · reglas de dependencia entre módulos respetadas · catálogo de UI generado desde el código.

> Regla del boy scout: un PR que toca un archivo con deuda no la empeora.

---

## Mapa de documentación

| Necesito… | Documento |
|-----------|-----------|
| Tokens, átomos (M), organismos (O), plantillas (T) y pantallas (P) | [`README.md`](README.md) §1–§4 |
| Vocabulario de estados y formatos | [`README.md`](README.md) §5 y §7 |
| Qué componentes/pantallas/assets ya existen | [`docs/ui-catalogo/index.html`](docs/ui-catalogo/index.html) · skill `/catalogo-ui-sportkid` |
| Plantilla de PR | [`PULL_REQUEST_TEMPLATE.md`](PULL_REQUEST_TEMPLATE.md) |
| Changelog y fragmentos | [`CHANGELOG.md`](CHANGELOG.md) · [`changelog.d/template/TOWNCRIER_README.md`](changelog.d/template/TOWNCRIER_README.md) |
| Estilo para Copilot | [`copilot-instructions.yml`](copilot-instructions.yml) |
| Principios de trabajo (Make it Work → Make it Right → Practice) | [`.claude/skills/ub-mode/SKILL.md`](.claude/skills/ub-mode/SKILL.md) (`/ub-mode`) |

### Propagación de un cambio

```
Cambio de token o componente → README §1–§3 → catálogo (generar.py)
Pantalla nueva o modificada  → README §4.2 → pantallas.json → capturar.py + generar.py
Convención o deuda nueva     → CLAUDE.md → python3 .github/scripts/sync_claude_to_copilot.py (AGENTS.md y .github/)
Comando o agente de .claude/ → python3 .github/scripts/sync_claude_to_copilot.py (.github/prompts, .github/agents)
Cambio visible al usuario    → changelog.d/<fragmento>.md
```

- `AGENTS.md`, `.github/copilot-instructions.md`, `.github/prompts/` y `.github/agents/` son espejo de `CLAUDE.md` y `.claude/`; `docs/ui-catalogo/index.html` se genera. Ninguno se edita a mano.
