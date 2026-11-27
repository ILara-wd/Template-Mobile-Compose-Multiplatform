# 🛡️ Lint y análisis estático — Kensho

> **Versión:** 1.2.0 | **Última actualización:** 2026-10-30 (reglas planificadas FF-14…FF-16 por ADR-016/017)
> **Estado:** 🟡 **Parcial.** Pasos 2 (Detekt con baseline) y 4 (módulo `:lint-rules` con 15 reglas `Kensho*`) implementados el 2026-10-28; faltan Android Lint en CI, `detekt-formatting`, compose-rules y el pipeline (TD-KEN-008 sigue abierta).
> **Herramientas:** Detekt 1.23.8 + reglas propias `Kensho*` (✅) · `detekt-formatting` y compose-rules (pendiente) · Android Lint · Bitbucket Pipelines (pendiente)

> **¿Por qué existe este documento?**
> Las fitness functions de `01-governance.md §7` son greps que alguien tiene que acordarse de correr. Mientras el proyecto es pequeño eso funciona; en cuanto haya más pantallas o más personas, una regla que no se comprueba sola deja de cumplirse. Aquí se define cómo convertir cada FF en una regla que falle en el IDE, en un pre-commit y en el pipeline, y cómo adoptarla sin bloquear el trabajo por la deuda existente (baseline).

---

## Índice

0. [Estado y plan de adopción](#0-estado-y-plan-de-adopción)
1. [Filosofía](#1-filosofía)
2. [Catálogo de reglas](#2-catálogo-de-reglas)
3. [Configuración propuesta](#3-configuración-propuesta)
4. [Reglas propias `Kensho*`](#4-reglas-propias-kensho)
5. [Baseline y migración progresiva](#5-baseline-y-migración-progresiva)
6. [Pre-commit opcional](#6-pre-commit-opcional)
7. [Pipeline de Bitbucket](#7-pipeline-de-bitbucket)
8. [Excepciones y supresiones](#8-excepciones-y-supresiones)

---

## 0. Estado y plan de adopción

| Paso | Entregable | Historia |
|------|-----------|----------|
| 1 | Android Lint con `lint-baseline.xml` y `warningsAsErrors` solo para código nuevo | `LINT-001` |
| 2 | ✅ Detekt con `detekt-baseline.xml` (2026-10-28) · pendiente: `detekt-formatting` + reglas Compose | `LINT-001` |
| 3 | `bitbucket-pipelines.yml`: build, unit tests, lint, detekt en cada PR a `develop` | `LINT-001` |
| 4 | ✅ Módulo `:lint-rules` con 15 reglas `Kensho*` que cubren FF-01…FF-11 y FF-13 (2026-10-28) | `LINT-002` |
| 5 | Pre-commit opcional y reducción del baseline a medida que se paga la deuda | continuo |
| 6 | Reglas `Kensho*` de DI y red (FF-14…FF-16, ARC-10, LOG-03) — se implementan en el PR que instala Hilt (FF-15, FF-16) y en el piloto de red (FF-14, ARC-10, LOG-03) | TD-KEN-012 |

Hasta que exista el paso 3, `./gradlew detekt` se corre a mano antes del PR (y lo corre `/validate-pullrequest`); los greps de `01-governance.md §7` siguen siendo la medición de la línea base.

Cuando se complete cada paso: actualizar el estado de TD-KEN-008 en `01-governance.md §6`, la métrica en `06-compliance-report.md` y la sección "Comandos" de `CLAUDE.md` (propagación con `/kensho-governance:propagacion`).

---

## 1. Filosofía

- **Automatizar lo que ya es regla.** No se crean reglas de lint que no estén en `03-standards.md`; si hace falta una nueva, primero se documenta (ADR o regla) y luego se automatiza.
- **Baseline, no big-bang.** La deuda existente queda congelada en un baseline; el código nuevo cumple desde el primer día. El baseline solo puede encogerse.
- **Mismo resultado en todos lados.** El IDE, el pre-commit y el pipeline corren la misma configuración (`config/detekt/detekt.yml`, `app/lint.xml`).
- **Rápido.** El análisis completo debe tardar menos de 1 minuto en el pipeline; las reglas propias son de AST, no de tipos, salvo que sea imprescindible.
- **Mensajes que enseñan.** Cada regla propia cita su ID (`THM-01`) y propone la corrección.

---

## 2. Catálogo de reglas

### 2.1 Mapeo fitness function → regla automática

| FF | Regla de `03-standards.md` | Automatización | Severidad en CI |
|----|---------------------------|--------------------------|-----------------|
| FF-01 | LOG-01 | ✅ `KenshoNoLog` | error |
| FF-02 | ADR-012 | ✅ `KenshoNoNullAssertion`, `KenshoNoGlobalScope`, `KenshoNoRunBlocking` (las de Detekt `ForbiddenImport`/`ForbiddenMethodCall` exigen resolución de tipos; las propias son de AST) | error |
| FF-03 | THM-01 | ✅ `KenshoNoColorLiteral` | error (baseline 4) |
| FF-04 | THM-02 | ✅ `KenshoNoDpLiteral` | error (baseline 2) |
| FF-05 | I18N-01 | ✅ `KenshoNoLiteralText` (también `contentDescription = "…"`) + Android Lint `SetTextI18n`/`HardcodedText` (XML) | error (baseline 1) |
| FF-06 | A11Y-01 | ✅ `KenshoNullContentDescriptionNeedsComment` | warning → error al cerrar TD-KEN-002 |
| FF-07 | ARC-07 | ✅ `KenshoNoSystemClock` | error (baseline 3) |
| FF-08 | ARC-05 | ✅ `KenshoNoCrossFeatureImport` | error |
| FF-09 | STR-02 | ✅ `KenshoMaxFileLines` (300; `LargeClass` de Detekt no aplica a archivos de funciones) | error en archivos nuevos (baseline 4) |
| FF-10 | PERF-02 | ✅ `KenshoNoCollectAsState` | error |
| FF-11 | CMP-09 | ✅ `KenshoPublicComposableNeedsPreview` (acepta `@TkdFontScalePreviews`; solo `components/`) | error (baseline 2) |
| FF-12 | TEST-01 | No es lint: el pipeline publica el conteo de tests y la cobertura (Kover, fase posterior) | informativo |
| FF-13 | A11Y-06 | ✅ `KenshoNoFontScaleMultiplication` | error |
| — | ARC-02 | ✅ `KenshoNoPublicMutableStateFlow` | error |
| FF-14 | ARC-11 | 📝 `KenshoNoRemoteOutsideData` (planificada) | error |
| FF-15 | ARC-06 | 📝 `KenshoNoFakeDefault` (planificada) | error en archivos nuevos (baseline 14, solo baja) |
| FF-16 | ARC-13 | 📝 `KenshoNoDispatcherLiteral` (planificada) | error |
| — | ARC-10 | 📝 `KenshoRethrowCancellation` (planificada; heurística de AST: `catch (… : Exception/Throwable)` o `runCatching` en `data/` sin `catch (… : CancellationException)` previo) | warning |
| — | LOG-03 | 📝 `KenshoNoHttpBodyLogging` (planificada) | error |
| — | ARC-12 | 📝 `KenshoNoServerMessageInUi` (planificada; `.message` de una excepción asignado en un `*ViewModel`) | warning |

### 2.2 Reglas de terceros activadas

| Regla | Fuente | Cubre |
|-------|--------|-------|
| `ModifierMissing`, `ModifierNotUsedAtRoot`, `ModifierReused`, `ModifierWithoutDefault` | compose-rules (`io.nlopez.compose.rules:detekt`) | CMP-01 |
| `ViewModelForwarding`, `ViewModelInjection` | compose-rules | ARC-01 |
| `ComposableParamOrder`, `ContentEmitterReturningValues` | compose-rules | CMP-01 |
| `MutableParams`, `UnstableCollections` (warning) | compose-rules | PERF-01 |
| `PreviewPublic` | compose-rules | CMP-09 (previews `private`) |
| `LambdaParameterInRestartableEffect` | compose-rules | PERF-05 |
| `LongMethod` (threshold 60, solo `@Composable`) | Detekt | CMP-08 (warning) |
| `CommentedOutCode` (vía `detekt-formatting`/ktlint no la tiene → regla propia ligera en fase 2) | — | STR-06 |
| `MaxLineLength` 120, `NoWildcardImports`, `ImportOrdering` | `detekt-formatting` (ktlint) | estilo |
| `ForbiddenComment` (`TODO:` sin ticket) | Detekt | higiene |

### 2.3 Android Lint

Ya viene con AGP; solo falta configurarlo y correrlo en CI. Reglas a elevar a error:

| ID de Android Lint | Cubre |
|--------------------|-------|
| `HardcodedText`, `SetTextI18n` | I18N-01 (vistas XML, por si aparecen) |
| `UnusedResources` | THM-06 / recursos huérfanos (warning, muchos falsos positivos con previews) |
| `ContentDescription` | A11Y-01 en XML |
| `MissingTranslation` | I18N-05 cuando exista `values-en/` |
| `VectorPath`, `VectorRaster` | RES-02/RES-05 |
| `UnusedMaterial3ScaffoldPaddingParameter`, `ComposeModifierMissing` (lint de Compose) | ARC-01/CMP-01 |

---

## 3. Configuración

> ✅ **Implementado (2026-10-28):** catálogo, `build.gradle.kts` raíz, `config/detekt/detekt.yml` y `:lint-rules`. Los archivos del repo son la fuente de verdad; los bloques de abajo son extractos. Pendiente: `detekt-formatting`, compose-rules (§2.2) y el bloque `lint` de §3.3.
>
> **Compatibilidad (verificada):** AGP 9.4.1 + Kotlin 2.2.10 + Gradle 9.6 sobre JDK 25. Detekt 1.23.8 (última estable; 2.0 sigue en alpha) trae su propio Kotlin 2.0.21, así que `:lint-rules` compila con `apiVersion = 2.0` y el root lo agrega con `isTransitive = false` para no meter el stdlib 2.2 en el classpath de Detekt. El plugin de Kotlin JVM se aplica sin versión porque AGP 9 ya lo pone en el classpath.

### 3.1 Catálogo (`gradle/libs.versions.toml`)

```toml
[versions]
detekt = "1.23.8"            # última estable; 2.0 en alpha (API de reglas distinta)
junitJupiter = "5.10.2"
junitPlatform = "1.10.2"
assertj = "3.25.3"
# pendiente: composeRules = "0.4.27"

[libraries]
detekt-api = { module = "io.gitlab.arturbosch.detekt:detekt-api", version.ref = "detekt" }
detekt-test = { module = "io.gitlab.arturbosch.detekt:detekt-test", version.ref = "detekt" }
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junitJupiter" }
junit-platform-launcher = { module = "org.junit.platform:junit-platform-launcher", version.ref = "junitPlatform" }
assertj-core = { module = "org.assertj:assertj-core", version.ref = "assertj" }
# pendiente: detekt-formatting, compose-rules-detekt

[plugins]
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
```

### 3.2 `build.gradle.kts` raíz

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.detekt)
}

detekt {
    source.setFrom("app/src/main/java", "app/src/test/java", "app/src/androidTest/java")
    config.setFrom("config/detekt/detekt.yml")
    baseline = file("config/detekt/detekt-baseline.xml")
    buildUponDefaultConfig = true
    parallel = true
}

dependencies {
    // Sin transitivas: Detekt 1.23 corre sobre su propio Kotlin 2.0.21.
    detektPlugins(project(":lint-rules")) { isTransitive = false }
    // pendiente: detektPlugins(libs.detekt.formatting) · detektPlugins(libs.compose.rules.detekt)
}
```

### 3.3 `app/build.gradle.kts` — bloque `lint` propuesto

```kotlin
android {
    lint {
        baseline = file("lint-baseline.xml")
        lintConfig = file("lint.xml")
        abortOnError = true
        warningsAsErrors = false
        checkDependencies = false
        xmlReport = true
        htmlReport = true
    }
}
```

### 3.4 `config/detekt/detekt.yml`

El archivo del repo es la referencia (comentado por FF). Decisiones:

- `buildUponDefaultConfig = true`: reglas por defecto de Detekt + ajustes; `maxIssues: 0` con baseline.
- Sección `kensho:` con las 15 reglas activas; `config.excludes: 'kensho.*'` porque la validación de Detekt no conoce sus claves.
- Exclusiones por ruta: tests y `androidTest` fuera de tokens, texto, `!!`, reloj y tamaño; `KenshoPublicComposableNeedsPreview` solo en `**/components/**` (igual que FF-11). `ui/theme/` y `data/` se excluyen dentro de las reglas por paquete.
- Ajustes a Compose: `LongMethod` 60 (CMP-08), `LongParameterList` 10 ignorando `@Composable`, `FunctionNaming` y `CyclomaticComplexMethod` ignoran `@Composable`, `TooManyFunctions` apagada, `MagicNumber` fuera de `ui/theme`/`data`/previews, `UnusedPrivateMember` ignora previews, `MaxLineLength` 140.

### 3.5 `app/lint.xml` — extracto

```xml
<?xml version="1.0" encoding="UTF-8"?>
<lint>
    <issue id="HardcodedText" severity="error" />
    <issue id="SetTextI18n" severity="error" />
    <issue id="ContentDescription" severity="error" />
    <issue id="VectorPath" severity="warning" />
    <issue id="UnusedResources" severity="warning" />
    <issue id="MissingTranslation" severity="ignore" /> <!-- activar al crear values-en (TD-KEN-011) -->
</lint>
```

---

## 4. Reglas propias `Kensho*`

✅ Viven en el módulo JVM `:lint-rules` (paquete `dev.treecode.kensho.lint`, `compileOnly` de `detekt-api`), con `KenshoRuleSetProvider` registrado en `META-INF/services/io.gitlab.arturbosch.detekt.api.RuleSetProvider` y `ruleSetId = "kensho"`. Todas extienden `rules/KenshoRule.kt` y razonan por **paquete** (`ui.theme`, `data`, features), no por ruta, para probarse con código en memoria; las exclusiones por ruta (`**/test/**`) van en `detekt.yml`. Detalle en [`lint-rules/README.md`](../../lint-rules/README.md).

| Regla | Detecta | Mensaje | Origen |
|-------|---------|---------|--------|
| `KenshoNoColorLiteral` | `Color(0x…)` y `Color.<White\|Black\|Red\|…>` fuera de `ui/theme/`; permite `Color.Unspecified` y `Color.Transparent` | "THM-01: usa un token de TkdColors" | FF-03 |
| `KenshoNoDpLiteral` | literal numérico seguido de `.dp`/`.sp` fuera de `ui/theme/` | "THM-02: usa TkdDimens / TkdTypography" | FF-04 |
| `KenshoNoLiteralText` | primer argumento o `text =` de `Text(...)` que es un string literal; `"…".format(` en composables | "I18N-01: usa stringResource" | FF-05 |
| `KenshoNullContentDescriptionNeedsComment` | argumento `contentDescription = null` sin comentario `//` en la misma línea | "A11Y-01: justifica el null (decorativo: …)" | FF-06 |
| `KenshoNoSystemClock` | `LocalDate.now()`, `LocalDateTime.now()`, `Instant.now()`, `System.currentTimeMillis()` fuera de `data/` | "ARC-07: la fecha de referencia viene del repositorio" | FF-07 |
| `KenshoNoCrossFeatureImport` | import de `dev.treecode.kensho.<featureA>.` desde un archivo de `<featureB>/` | "ARC-05: mueve lo compartido a components/ o model/" | FF-08 |
| `KenshoMaxFileLines` | archivo con más de 300 líneas | "STR-02: divide el archivo" | FF-09 |
| `KenshoPublicComposableNeedsPreview` | archivo con `@Composable fun` público sin ninguna `@Preview` | "CMP-09: agrega una @Preview private" | FF-11 |
| `KenshoNoLog` | `Log.*`, `println`, `printStackTrace` | "LOG-01" | FF-01 |
| `KenshoNoNullAssertion` / `KenshoNoGlobalScope` / `KenshoNoRunBlocking` | `!!` fuera de tests, `GlobalScope`, `runBlocking` | "FF-02: …" | FF-02 |
| `KenshoNoCollectAsState` | `collectAsState()` | "PERF-02: usa collectAsStateWithLifecycle" | FF-10 |
| `KenshoNoFontScaleMultiplication` | `… * fontScale` | "A11Y-06: usa withFontScale / shrinkToFit" | FF-13 |
| `KenshoNoPublicMutableStateFlow` | `MutableStateFlow`/`MutableSharedFlow` público | "ARC-02" | — |

**Planificadas (ADR-016/017, TD-KEN-012).** Misma base `KenshoRule`, razonando por paquete:

| Regla | Detecta | Mensaje | Origen |
|-------|---------|---------|--------|
| `KenshoNoRemoteOutsideData` | import de `retrofit2.`, `okhttp3.` o `dev.treecode.kensho.data.remote.` desde un paquete que no es `data` ni `di` | "ARC-11: expón un modelo de model/ desde el repositorio" | FF-14 |
| `KenshoNoFakeDefault` | parámetro de constructor con default `Fake…()` en `src/main` (excluye funciones `@Preview`) | "ARC-06: inyecta el repositorio con Hilt" | FF-15 |
| `KenshoNoDispatcherLiteral` | `Dispatchers.IO` / `Dispatchers.Default` fuera de `di` | "ARC-13: inyecta @IoDispatcher" | FF-16 |
| `KenshoSenseiProfileViaBase` | llamada a `signedInAccount`/`currentAccount`, `@GET("me")`, o `.profile(`/`.observeProfile()` de `TkdSenseiProfileRepository` fuera de `data`, `ui.TkdSenseiViewModel` y `feature.profile.sensei` | "ARC-15: hereda de TkdSenseiViewModel y usa onSenseiProfile" | FF-17 |
| `KenshoRethrowCancellation` | `catch` genérico o `runCatching` en `data` sin relanzar `CancellationException` | "ARC-10: relanza CancellationException" | — |
| `KenshoNoHttpBodyLogging` | `HttpLoggingInterceptor.Level.BODY` / `Level.HEADERS` | "LOG-03: usa BASIC solo en debug" | — |
| `KenshoNoServerMessageInUi` | `.message` de un `Throwable` leído en un `*ViewModel` | "ARC-12: mapea TkdDataError a @StringRes" | — |

### 4.1 Ejemplo de implementación

```kotlin
/** THM-01 / FF-03 (extracto de lint-rules/…/KenshoNoColorLiteral.kt). */
class KenshoNoColorLiteral(config: Config) : KenshoRule(
    config,
    id = "KenshoNoColorLiteral",
    description = "THM-01: los colores salen de TkdColors / TkdBeltPalette (ADR-004).",
    severity = Severity.Style,
) {

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        if (expression.isInPackage("ui.theme")) return          // la única casa de literales
        val receiver = expression.receiverExpression.text
        if (receiver == "Color" && expression.selectorExpression?.text in NAMED_COLORS) {
            report(expression, "THM-01: `${expression.text}` es un color literal; usa un token de `TkdColors` (por rol).")
        }
    }
    // + visitCallExpression para Color(0x…) / Color(r, g, b)
}
```

`KenshoRule` fija el `Issue` y expone `report(element, message)`; `isInPackage` compara contra `dev.treecode.kensho.<subpaquete>`.

### 4.2 Tests de cada regla

JUnit 5 + AssertJ + `detekt-test` (`rule.lint(code)`), nombres `sujeto_condicion_resultado` (TEST-04):

```kotlin
class KenshoNoColorLiteralTest {

    private val rule = KenshoNoColorLiteral(Config.empty)

    @Test
    fun hexAndNamedColors_outsideTheme_areReported() {
        val code = """
            package dev.treecode.kensho.components.atoms
            val badge = Color(0xFF059669)
            val text = Color.White
        """.trimIndent()

        assertThat(rule.lint(code)).hasSize(2)
    }

    @Test
    fun colors_insideTheme_areAllowed() {
        val code = """
            package dev.treecode.kensho.ui.theme
            object TkdColors { val Success = Color(0xFF059669) }
        """.trimIndent()

        assertThat(rule.lint(code)).isEmpty()
    }
}
```

Casos mínimos por regla: un positivo con un ejemplo real de la deuda (p. ej. `TkdStudentAvatar.kt:84`), un negativo con el patrón correcto y uno de exclusión (`ui/theme/`).

### 4.3 Cómo añadir una regla nueva

1. La regla existe en `03-standards.md` (si no, primero propagación: 06 → 01 → 03).
2. Clase en `:lint-rules`, registrada en `KenshoRuleSetProvider`.
3. Tests positivo/negativo/exclusión.
4. Activación en `detekt.yml` y regeneración del baseline solo si hay violaciones existentes.
5. Fila en §2.1 y en la tabla de verificación de `03-standards.md §13`.

---

## 5. Baseline y migración progresiva

### 5.1 Qué es

Un archivo (`config/detekt/detekt-baseline.xml`, `app/lint-baseline.xml`) con las violaciones existentes al activar la herramienta. Esas no fallan el build; cualquier violación nueva sí.

### 5.2 Generarlo

```bash
./gradlew detektBaseline                 # ✅ generado 2026-10-28
./gradlew :app:updateLintBaseline        # pendiente (paso 1)
git add config/detekt/detekt-baseline.xml app/lint-baseline.xml
```

**Baseline real (2026-10-28): 126 entradas** en `config/detekt/detekt-baseline.xml`.

| Regla | Hallazgos | Entradas | Comentario |
|-------|-----------|----------|------------|
| `KenshoNullContentDescriptionNeedsComment` | 65 | 31 | = FF-06 (TD-KEN-002); Detekt agrupa hallazgos con la misma firma |
| `KenshoNoSystemClock` | 5 | 5 | FF-07 contaba 3: el grep no veía `LocalTime.now()` en `TkdCheckInUiState`/`TkdCheckInViewModel` (TD-KEN-005) |
| `KenshoNoColorLiteral` | 4 | 4 | = FF-03 (TD-KEN-004) |
| `KenshoMaxFileLines` | 4 | 4 | = FF-09 (TD-KEN-006) |
| `KenshoPublicComposableNeedsPreview` | 2 | 2 | = FF-11 (`TkdSurface`, `CustomLinearProgress`) |
| `KenshoNoDpLiteral` | 1 | 1 | = FF-04 sin la línea comentada |
| `MaxLineLength` (140) | 35 | 35 | estilo |
| `LongMethod` (60) | 22 | 22 | CMP-08 (TD-KEN-006) |
| `MatchingDeclarationName`, `NewLineAtEndOfFile`, `MagicNumber`, `WildcardImport`, `UnusedPrivateMember` | 22 | 22 | higiene |

FF-05 da 0 en Detekt y 1 en el grep: el hallazgo del grep es `Text(text = "%02d".format(…))`, que no es texto literal sino formato sin locale (I18N-04, TD-KEN-004).

> ⚠️ **Límite del baseline:** Detekt identifica cada entrada por archivo + función + texto. Un `contentDescription = null` nuevo e idéntico a uno ya registrado **en la misma función** no fallará; el reviewer (FF-A11Y-02) y el grep FF-06 siguen siendo la red para ese caso.

### 5.3 Reglas del baseline

- **Solo se encoge.** Un PR que agrega entradas al baseline se rechaza salvo regla nueva (§4.3).
- Cada historia `TD-KEN-0XX-FIX` elimina sus entradas del baseline en el mismo PR.
- El conteo de entradas por regla se publica en `06-compliance-report.md` en cada auditoría.

---

## 6. Pre-commit opcional

Rápido y local; el pipeline sigue siendo la fuente de verdad. Se instala junto al `commit-msg` existente (`git config core.hooksPath .githooks`):

```bash
#!/usr/bin/env bash
# .githooks/pre-commit — detekt solo sobre los .kt staged (opcional)
set -euo pipefail
FILES=$(git diff --cached --name-only --diff-filter=ACM -- '*.kt' | paste -sd, -)
[[ -z "$FILES" ]] && exit 0
./gradlew -q detekt -Pdetekt.input="$FILES" || {
  echo "❌ detekt encontró problemas. Corrige o usa --no-verify (queda en el historial)."; exit 1; }
```

> Requiere que la configuración de Detekt lea la propiedad `detekt.input`; si no, usar `./gradlew detekt` completo (≈ 30 s en el tamaño actual).

---

## 7. Pipeline de Bitbucket

`bitbucket-pipelines.yml` propuesto (no existe todavía):

```yaml
image: eclipse-temurin:17-jdk   # AGP 9 requiere JDK 17+ para ejecutar Gradle aunque el bytecode sea JVM 11

definitions:
  caches:
    gradle-wrapper: ~/.gradle/wrapper
    gradle-caches: ~/.gradle/caches
  steps:
    - step: &android
        name: Build, tests y análisis
        size: 2x
        caches: [gradle-wrapper, gradle-caches]
        script:
          - export ANDROID_HOME=/opt/android-sdk
          - mkdir -p $ANDROID_HOME/cmdline-tools
          - curl -sSL -o tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
          - unzip -q tools.zip -d $ANDROID_HOME/cmdline-tools && mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/latest
          - yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses > /dev/null
          - ./gradlew --no-daemon :app:assembleDebug
          - ./gradlew --no-daemon :app:testDebugUnitTest
          - ./gradlew --no-daemon :app:lintDebug
          - ./gradlew --no-daemon detekt
        artifacts:
          - app/build/reports/**
          - build/reports/detekt/**

pipelines:
  pull-requests:
    '**':
      - step: *android
  branches:
    develop:
      - step: *android
    main:
      - step: *android
```

Notas:

- El SDK (`platforms;android-37`, `build-tools`) lo descarga AGP automáticamente al aceptar licencias; si el agente no tiene red suficiente, usar una imagen con SDK preinstalado.
- `local.properties` no está en el repo; el pipeline usa `ANDROID_HOME`.
- `mavenLocal()` en `settings.gradle.kts` (TD-KEN-009) no rompe el pipeline mientras no haya dependencias que solo existan en local (`libs.uikit` está comentado).
- Activar en Bitbucket la condición de merge "builds verdes" para `develop` y `main`.
- Los tests instrumentados (`connectedDebugAndroidTest`) no corren en este pipeline: requieren emulador; se evaluará Firebase Test Lab o Gradle Managed Devices cuando existan.

---

## 8. Excepciones y supresiones

### 8.1 Supresión puntual

```kotlin
@Suppress("KenshoNoColorLiteral") // THM-01: color de marca de un tercero (logo de Google) sin rol en TkdColors
@Composable
private fun GoogleMark() { … }
```

Toda supresión lleva comentario con la regla y el motivo; el reviewer la valida (`05-code-review-guide.md §3`).

### 8.2 Exclusiones por configuración

- `ui/theme/**` excluido de las reglas de tokens (es la única casa de literales).
- `data/**` excluido de `KenshoNoSystemClock` y `MagicNumber` (datos de prototipo).
- `src/test/**`, `src/androidTest/**` excluidos de reglas de tokens, texto y preview.

### 8.3 Lo que no se suprime nunca

`KenshoNoCrossFeatureImport`, `KenshoNoLog`, `ForbiddenImport` de `GlobalScope` y secretos. Si parece necesario, la conversación es sobre la arquitectura, no sobre la supresión.
