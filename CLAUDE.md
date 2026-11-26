# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> **¿Por qué existe este archivo?**
> Resume lo que Claude Code necesita para trabajar en **MiniJuegos Hub** sin tener que deducirlo leyendo todo el
> código: comandos, módulos, design system, navegación, juegos, convenciones, deuda conocida y dónde está la
> documentación. El detalle vive en el [`README.md`](README.md); este archivo es el mapa. Se actualiza **al final
> de cada cambio de arquitectura o de convenciones**.

> ⚠️ **Proyecto Android-only, aunque el repo se llame "Multiplatform".** No hay KMP, `commonMain`, Hilt,
> Navigation Compose, red ni persistencia. No inventes esas capas: el estado vive en `ViewModel` + `StateFlow`
> y la navegación es estado `rememberSaveable` en `MainActivity.kt`.

---

## Qué es

App de minijuegos en **Jetpack Compose** construida desde el prototipo de Figma
[Prototipo-Demo-UiKit](https://www.figma.com/design/KqTFktvrRvCzO2idPga5xf/Prototipo-Demo-UiKit) (archivo
`KqTFktvrRvCzO2idPga5xf`), con **Atomic Design**: tokens → átomos → moléculas → organismos → pantallas.

| Dato | Valor |
|------|-------|
| Rama de trabajo | `multi_game_compose` (PRs contra `main`) |
| Paquete / applicationId | `com.invex.one.template` (nombre de app: *Template*) |
| Módulos Gradle | `:app` · `:core:designsystem` · `:feature:{home,games,records,auth,profile}` |
| compileSdk / targetSdk / minSdk | 36 / 36 / 24 |
| Kotlin / AGP / JVM | 2.0.21 (plugin Compose) / 8.13.2 / target 11, Gradle en JDK 17 (`gradle/gradle-daemon-jvm.properties`) |
| Compose BOM | 2024.09.00 (Material 3) · Lifecycle 2.9.4 |
| Dependencias | Solo AndroidX (`gradle/libs.versions.toml`) |
| Archivos Kotlin (`src/main`) | ~100: 42 design system · 26 games (17 juegos en `hub/`) · 8 app · 8 auth · 8 home · 6 records · 5 profile (2026-11-26) |
| Tema | Solo claro (Figma no define oscuro) |

> Las carpetas sin seguimiento `core/model`, `core/ui` y `feature/{agenda,athlete,athletics,expenses,settings,sharing,swimming,tkd}`
> son restos de compilación de la rama `sport_kids`. **No están en `settings.gradle.kts`; ignóralas.**

---

## Comandos principales

```bash
# Compilar / instalar (emulador: AVD Pixel_9a; con varios dispositivos, export ANDROID_SERIAL=emulator-5554)
./gradlew :app:assembleDebug
./gradlew :app:installDebug
adb shell am start -n com.invex.one.template/.MainActivity

# Abrir cualquier pantalla directo (solo builds debug; ver DebugStart en MainActivity.kt)
adb shell am start -S -n com.invex.one.template/.MainActivity --es mj_screen Profile
#   mj_screen: Login | Register | Home | Games | Records | Profile | Game   (+ --es mj_game <id> para un juego)

# Compilar solo un módulo (más rápido para validar un cambio)
./gradlew :feature:profile:compileDebugKotlin

# Catálogo de UI (capturas del emulador + HTML generado)
python3 scripts/ui-catalogo/capturar.py P03 P04     # sin ids captura todas (P01–P23)
python3 scripts/ui-catalogo/generar.py              # escribe docs/ui-catalogo/index.html
open docs/ui-catalogo/index.html

# Tests (hoy solo existen los de ejemplo de la plantilla)
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest
```

Notas del entorno: zsh no separa `$VAR` en palabras (usa `$(…)` o funciones); el emulador tarda ~8 s en arrancar
en frío; `adb input keyevent BACK` puede navegar en vez de cerrar el teclado (usa la tecla *Done* del IME).

---

## Arquitectura

### Módulos

```
            ┌──────────────────────────────────────────────┐
            │ :app  MainActivity · TemplateApp (navegación) │
            │       MiniJuegosTopBar · GameRegistry ·       │
            │       GameHostScreen · Screen.kt (entradas)   │
            └──┬──────────┬──────────┬──────────┬─────────┬─┘
               ▼          ▼          ▼          ▼         ▼
          :feature:  :feature:  :feature:  :feature:  :feature:
            home       games     records     auth      profile
               └──────────┴────┬─────┴──────────┴─────────┘
                               ▼
                     :core:designsystem   ← tokens, tema, átomos, moléculas, organismos, iconos, fuentes
```

| Regla | Detalle |
|-------|---------|
| **Features aislados** | Un feature **no** depende de otro. Se comunican vía `:app` con callbacks e ids de juego compartidos (`"2048"`, `"whack"`, `"snake"`…). |
| **Design system ciego** | `:core:designsystem` no conoce ningún feature ni modelo de dominio. |
| **Fuente única de juegos** | `GameCatalog` (`:feature:games/data`): id, nombre, descripción y categoría. |

### Estructura de un feature

```
feature/<x>/src/main/
├── java/com/invex/one/template/feature/<x>/
│   ├── <X>Screen.kt        # <X>Route (stateful) + <X>Screen (stateless, @Preview)
│   ├── <X>ViewModel.kt     # StateFlow<UiState>, eventos como funciones
│   ├── model/              # UiState y modelos @Immutable
│   ├── data/               # Datos de muestra (no hay repositorios)
│   └── component/          # Organismos propios de la pantalla (internal)
└── res/values/strings.xml  # Textos es-MX, con plurals
```

```kotlin
// Route: obtiene el ViewModel y recoge estado; Screen: solo UI, se previsualiza
@Composable
fun ProfileRoute(actions: ProfileActions, modifier: Modifier = Modifier, viewModel: ProfileViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(onBack = actions.onBack)
    ProfileScreen(
        state = state,
        callbacks = ProfileCallbacks(onEditModeChange = viewModel::onEditModeChange, /* … */),
        actions = actions,
        modifier = modifier,
    )
}
```

- Muchos eventos → data classes de callbacks (`LoginActions`, `ProfileActions`, `RegisterCallbacks`…).
- Las pestañas reciben `contentPadding` desde `:app` para dibujar bajo las barras translúcidas.

### Navegación (`app/.../MainActivity.kt`)

`TemplateApp` → `AuthFlow` (Login raíz, sin *Volver* ⇄ Register) hasta entrar; luego `SignedInApp`: pestañas
Inicio / Juegos / Récords, `GameHostScreen` a pantalla completa y Perfil (avatar de la barra). Todo es estado
`rememberSaveable`; la sesión se pierde al cerrar la app. `DebugStart` (solo debug) fija la pantalla inicial.

### Design system (`:core:designsystem`, paquete `com.invex.one.template.core.designsystem`)

| Paquete | Contenido |
|---------|-----------|
| `token/` | `ColorPalette` (**internal**, primitivos) → `AppColors` (roles M3, solo `LightAppColors`) · `AppAlpha` · `AppTypography` (Outfit, Plus Jakarta Sans, Space Grotesk) · `AppSpacing` · `AppSize` · `AppShapes` · `AppElevation` |
| `theme/` | `AppTheme { }` (CompositionLocals + `MaterialTheme` equivalente) · `Modifier.appShadow` · `Modifier.depthShadow` |
| `atom/` · `molecule/` · `organism/` | 22 átomos, 15 moléculas, 4 organismos (tablas en README) |
| `icon/AppIcons` | VectorDrawables de Figma (`res/drawable/ic_*.xml`), conservan su color; `tint` si depende del estado |
| `format/` | `formatScore` (punto de miles), `formatUtcDate` vive en `molecule/AppDatePickerDialog` |

```kotlin
// ✅ Todo sale del tema
Text("Hola", style = AppTheme.typography.title, color = AppTheme.colors.onSurface)
Spacer(Modifier.height(AppTheme.spacing.lg))
PrimaryButton(text = "Jugar", onClick = {}, trailingIcon = AppIcons.Play)   // en pantallas, el texto va en strings.xml

// ❌ Prohibido en componentes y pantallas
Color(0xFF006E2F); Modifier.padding(16.dp); fontSize = 14.sp; ColorPalette.Green40
```

### Juegos

- Implementación en `feature/games/.../hub/` (`Game2048Screen`, `SnakeScreen`, `OrbitGuardScreen`…; sprites en `hub/character/`).
- `app/.../Screen.kt`: 17 composables de entrada que envuelven a los de `hub` (`PeleasGame` sigue vacío).
- `GameRegistry` (`:app`) mapea id → entrada; `GameHostScreen` lo ejecuta con barra de *Volver*.
- Agregar un juego: `hub/` → entrada en `Screen.kt` → `GameCatalog.games` → `GameRegistry` → (opcional) arte en `AppIcons` / `Game.artwork()` → entrada P24+ en `docs/ui-catalogo/pantallas.json`.
- Los juegos de `hub/` llegaron con estilo propio y **no siguen** aún las reglas del design system (ver deuda).

---

## Convenciones

- **Idioma:** UI en español (es-MX); código, nombres y KDoc en inglés.
- **Tokens:** ni colores, ni `dp`, ni `sp` literales; todo de `AppTheme.*`, `AppSize`, `AppAlpha`, `AppIcons`. Un token nuevo solo si el diseño no se puede expresar con los actuales (Figma no tiene variables).
- **Firmas:** parámetros obligatorios, luego `modifier: Modifier = Modifier` como primer opcional; variantes con enums (`ButtonVariant`, `StatTileStyle`…) mejor que booleanos; extender con parámetros opcionales que no cambien a los usuarios actuales.
- **Estado:** `@Immutable data class` expuesta como `StateFlow`; la UI no muta estado; `Screen` stateless.
- **Textos:** `strings.xml` del módulo con `plurals`; nombres propios de datos (juegos, avatares) en los modelos.
- **Formatos:** `formatScore(4096) == "4.096"`; fechas en ms UTC mostradas con `formatUtcDate`.
- **Accesibilidad:** `contentDescription` en íconos interactivos, `Role` en clicables, filas completas como objetivo táctil, `semantics { heading() }` en encabezados, área mínima `AppSize.minTouchTarget` (44dp).
- **Previews:** `private`, `@Preview(widthDp = 390, heightDp = 884)` envuelta en `AppTheme { }` para pantallas.
- **KDoc:** una primera oración descriptiva en todo componente; el catálogo la usa como descripción.
- **Edge-to-edge:** barras del sistema transparentes con iconos oscuros.
- **Sin `!!`**, sin `android.util.Log`/`println` en código de producción.
- **Commits:** uno por pantalla/cambio funcional; `.idea/` y `CLAUDE.md` solo si se pidió.

---

## Flujos de trabajo

**Implementar una pantalla de Figma** (README §«Cómo implementar una nueva pantalla de Figma»):
1. Usa la skill `catalogo-ui-minijuegos` (sección A) para decidir qué reutilizar, extender o crear.
2. `get_design_context` del nodo; descarga los assets **en el momento** y añade la extensión (`.svg`/`.png`) a las URLs del MCP o devuelven 404.
3. Reutiliza íconos idénticos por `pathData`; convierte los nuevos a VectorDrawable y regístralos en `AppIcons`.
4. `Route` + `Screen` + `ViewModel` + `strings.xml`; conéctala en `MainActivity.kt` (y en `DebugStart`).
5. Verifica en el emulador contra Figma y actualiza el catálogo (skill, sección B) y el README.

**Particularidades de Figma:** sin variables; algunos exports rotos (p. ej. check con `opacity 0`, raíz 32×32);
los frames repiten la barra con subtítulo "Inicio" y sin pestaña seleccionada (en código siguen la navegación real).

---

## Deuda técnica conocida (`TD-MJ-0XX`, métricas 2026-11-26)

| ID | Descripción | Severidad |
|----|-------------|-----------|
| TD-MJ-001 | Sin backend: login, registro, Google, recuperación/cambio de contraseña, foto y guardado de perfil son locales o `TODO` | 🟠 Alto |
| TD-MJ-002 | Sesión no persistente: "Recordar en este equipo" no guarda nada (candidato: DataStore) | 🟡 Medio |
| TD-MJ-003 | Juegos de `hub/` fuera del design system: 226 `Color(0x…)`, ~409 `dp`/`sp` literales y ~120 textos literales (no `strings.xml`) | 🟡 Medio |
| TD-MJ-004 | Archivos grandes: 12 juegos > 300 líneas (`DodgeBoard.kt` 1479, `Slash.kt` 1178, `SmashBoard.kt` 1139, `OrbiGuard.kt` 1137…); `RegisterScreen.kt` y `ProfileScreen.kt` ~300 | 🟡 Medio |
| TD-MJ-005 | Sin tests reales (solo `ExampleUnitTest` / `ExampleInstrumentedTest`) | 🟠 Alto |
| TD-MJ-006 | `PeleasGame()` vacío en `Screen.kt` aunque `hub/PeleasGame.kt` define `LandscapeFightingGame` (usado por `Figth`) | 🟢 Bajo |
| TD-MJ-007 | 1 `!!` en `format/NumberFormat.kt:7` (`ThreadLocal.get()!!`) | 🟢 Bajo |
| TD-MJ-008 | ~57 composables de juegos sin KDoc; assets sin uso en `core/designsystem/res/drawable`: `img`, `img_2`, `outline_{energy,orbit,shield}_24` (y `img*.png` con nombres genéricos, fuera de `AppIcons`) | 🟢 Bajo |
| TD-MJ-009 | 10 juegos sin arte ni textos definitivos en Figma (nombres/descripciones provisionales en `GameCatalog`); avatares del registro son capturas de relleno | 🟢 Bajo |

**Verificado limpio:** 0 `Color(0x…)` fuera de `token/` en design system, app y pantallas de features; 0 `Log`/`println`.

---

## Mapa de documentación

| Necesito… | Dónde |
|-----------|-------|
| Visión general, stack, módulos, navegación, design system, pantallas, convenciones, pendientes | [`README.md`](README.md) |
| Saber si un componente/ícono/token ya existe, ver dónde se usa, capturas de cada pantalla y juego | [`docs/ui-catalogo/index.html`](docs/ui-catalogo/index.html) (generado, no editar) |
| Lista de pantallas del catálogo y cómo se abren | [`docs/ui-catalogo/pantallas.json`](docs/ui-catalogo/pantallas.json) |
| Flujo de consulta antes de implementar y de actualización del catálogo | [`.claude/skills/catalogo-ui-minijuegos/SKILL.md`](.claude/skills/catalogo-ui-minijuegos/SKILL.md) |
| Scripts del catálogo | [`scripts/ui-catalogo/`](scripts/ui-catalogo/) (`capturar.py`, `generar.py`, `plantilla.html`) |

### Mantenimiento

- Un cambio de arquitectura, convención o deuda se refleja en el **README** y luego aquí, en el mismo commit.
- Pantalla o componente nuevo → fila en el README + catálogo regenerado.
- Las métricas de la tabla de deuda llevan fecha; actualízalas al cerrar o abrir un `TD-MJ`.
