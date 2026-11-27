# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> **Fuente única de instrucciones para agentes.** `AGENTS.md` y `.github/copilot-instructions.md` se generan desde
> este archivo con `python3 .github/scripts/sync_claude_to_copilot.py`; no se editan a mano.

> **Este repo es una plantilla.** Es el punto de partida de apps Android con Jetpack Compose. Cada app nueva se crea
> copiando la plantilla y, desde ese momento, es un proyecto **independiente**: tiene su propio nombre, paquete,
> repositorio y ciclo de versiones. Lo que agregues aquí debe servir a **cualquier** app futura, no a una en
> particular.

---

## Estado actual

Hoy la plantilla es el proyecto *Empty Activity* de Android Studio con Material 3 y una barra de navegación
adaptable. No hay features, capa de datos, inyección de dependencias, red ni persistencia. **No las inventes**: si
un cambio las necesita, se proponen y se agregan de forma explícita.

| Dato | Valor |
|------|-------|
| Nombre de app / proyecto Gradle | `Template` (`app_name`, `rootProject.name`) |
| Paquete / applicationId | `com.invex.one.template` |
| Módulos Gradle | `:app` |
| compileSdk / targetSdk / minSdk | 36 / 36 / 24 |
| Kotlin / AGP / JVM | 2.0.21 (plugin Compose) / 8.13.2 / 11 |
| UI | Compose BOM 2024.09.00 · Material 3 · `material3-adaptive-navigation-suite` |
| Dependencias | Solo AndroidX, declaradas en `gradle/libs.versions.toml` |
| Versión | `versionName = "1.0"`, `versionCode = 1` |

```
app/src/main/java/com/invex/one/template/
├── MainActivity.kt      # TemplateApp: NavigationSuiteScaffold con AppDestinations (Home, Favorites, Profile) y Greeting
└── ui/theme/            # Color.kt · Theme.kt (TemplateTheme, color dinámico en Android 12+) · Type.kt
```

---

## Comandos

```bash
./gradlew :app:assembleDebug               # compilar
./gradlew :app:installDebug                # instalar (emulador: AVD Pixel_9a)
./gradlew :app:testDebugUnitTest           # unit tests (JVM)
./gradlew :app:connectedDebugAndroidTest   # tests instrumentados (requiere emulador)
./gradlew :app:lintDebug                   # Android Lint

git config core.hooksPath .githooks        # activar la validación de mensajes de commit (una vez)
towncrier build --config config/towncrier.toml --version <x.y.z> --draft   # vista previa del changelog
```

Con varios dispositivos conectados, fija el emulador con `export ANDROID_SERIAL=emulator-5554` antes de instalar.

---

## Arquitectura recomendada al crecer

La plantilla no impone todavía estas capas, pero es la forma en que deben crecer las apps creadas a partir de ella:

- **MVVM + flujo unidireccional.** `ViewModel` expone `StateFlow<UiState>`; la UI recoge con
  `collectAsStateWithLifecycle` y manda eventos como funciones del ViewModel.
- **Pantalla en dos partes.** `<X>Screen` (stateful, conoce al ViewModel) y `<X>Content` (stateless, recibe estado
  y callbacks, tiene `@Preview`). La navegación sale por callbacks hacia el nivel de la app; un feature no recibe
  `NavController` ni importa otro feature.
- **Paquetes por feature** dentro de `:app` (`feature/<x>/`, `ui/theme/`, `components/`, `data/`, `model/`). Divide
  en módulos Gradle cuando el tamaño o los tiempos de compilación lo pidan, no antes.
- **Datos detrás de una interfaz** (`<X>Repository`) con una implementación *Fake* para previews y tests.

Agregar Hilt, Navigation Compose, Retrofit/Ktor, Room/DataStore u otro framework es una decisión del proyecto
derivado. En la plantilla solo entra si sirve a todas las apps futuras y se documenta aquí.

---

## Convenciones

- **Idioma:** textos de UI en español de México (`strings.xml`); código e identificadores en inglés.
- **Textos:** siempre `stringResource`; nada de texto literal en composables (incluidas las etiquetas de
  `AppDestinations`, hoy en inglés y literales: son parte del código de ejemplo a reemplazar).
- **Tema:** colores, tipografía y medidas salen de `MaterialTheme` / `ui/theme/`. Nada de `Color(0x…)` fuera de
  `ui/theme/`. Si el diseño necesita medidas propias, se agregan como tokens en `ui/theme/`.
- **Componentes:** `modifier: Modifier = Modifier` como primer parámetro opcional, aplicado a la raíz; variantes con
  enums u objetos de estilo, no con booleanos; al menos un `@Preview` `private`.
- **Estado:** `UiState` como `@Immutable data class` con `val`; `key` en `items(...)` de listas.
- **Accesibilidad:** `contentDescription` desde recursos en íconos interactivos (`null` solo si es decorativo),
  `Role` en elementos clicables, objetivos táctiles de al menos 48dp, el texto escala con la fuente del sistema.
- **Prohibido en `src/main`:** `!!`, `GlobalScope`, `runBlocking`, `android.util.Log`, `println`, código comentado.
- **Dependencias:** solo vía `gradle/libs.versions.toml`.
- **Tests:** todo ViewModel con unit test en `app/src/test`; flujos interactivos con Compose UI Test sobre
  `Content`. Nombres `sujeto_condicion_resultado`.
- **Commits:** `<tipo>(<ámbito>): <descripción en imperativo>` con tipo
  `feat|fix|refactor|test|docs|chore|build|revert` (lo valida `.githooks/commit-msg`). Commit solo cuando se pida.
- **Changelog:** un PR que cambia `app/src/main` agrega un fragmento `changelog.d/<id>.<tipo>.md`; `CHANGELOG.md`
  lo genera towncrier, nunca a mano. Guía: [`changelog.d/template/TOWNCRIER_README.md`](changelog.d/template/TOWNCRIER_README.md).

---

## Herramientas del repo

| Ruta | Qué es |
|------|--------|
| `.githooks/` | Hook `commit-msg` que valida la convención de commits (tickets `APP-<n>`, `TICKET_PREFIXES`); tests en `.githooks/tests/test-commit-msg.sh` |
| `config/towncrier.toml`, `config/towncrier.md.jinja2`, `changelog.d/`, `CHANGELOG.md` | Changelog con towncrier |
| `PULL_REQUEST_TEMPLATE.md` (copia en `.bitbucket/`) | Plantilla de PR con el checklist de estas convenciones |
| `.github/scripts/sync_claude_to_copilot.py` | Genera `AGENTS.md`, `.github/copilot-instructions.md` y los prompts/agentes de Copilot desde `CLAUDE.md` y `.claude/` |

No hay Detekt, ktlint ni CI todavía; cada app derivada decide si los agrega.

---

## Convertir la plantilla en una app independiente

Al crear una app nueva desde la plantilla, en este orden:

1. **Identidad:** `rootProject.name` (`settings.gradle.kts`), `app_name` (`strings.xml`), `namespace` y
   `applicationId` (`app/build.gradle.kts`), y el paquete `com.invex.one.template` (mover carpetas en `src/main`,
   `src/test` y `src/androidTest`). Renombrar `TemplateTheme` y `TemplateApp`.
2. **Código de ejemplo:** reemplazar `Greeting`, `AppDestinations` y los colores `Purple*`/`Pink*` de `ui/theme/`
   por los del diseño de la app. Decidir si se mantiene el color dinámico.
3. **Ícono y recursos:** `ic_launcher*` en `mipmap-*` y `drawable/`.
4. **Herramientas:** cambiar el prefijo de tickets `APP` en `.githooks/commit-msg` (y sus tests), en la plantilla de
   PR y en la guía de towncrier; el nombre en `config/towncrier.toml` y `CHANGELOG.md`.
5. **Documentación:** reescribir este `CLAUDE.md` para la app (y regenerar `AGENTS.md` con el script): qué es, módulos, decisiones y deuda.
   Quitar la nota de «Este repo es una plantilla».
6. **Versión inicial:** dejar `versionName = "1.0"` / `versionCode = 1` y `CHANGELOG.md` sin versiones;
   publicar la primera con towncrier.

---

## Mantenimiento

- Un cambio en la arquitectura, las dependencias o las convenciones de la plantilla se refleja aquí en el mismo PR.
- Después de editar este archivo (o agregar comandos y agentes en `.claude/`), ejecuta `python3 .github/scripts/sync_claude_to_copilot.py` para
  regenerar `AGENTS.md`, `.github/copilot-instructions.md`, `.github/prompts/` y `.github/agents/`.
