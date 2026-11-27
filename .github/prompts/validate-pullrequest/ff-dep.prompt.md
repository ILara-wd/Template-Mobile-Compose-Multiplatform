<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde .claude/commands/validate-pullrequest/ff-dep.md. NO EDITAR A MANO. -->
---
mode: agent
description: 5.11 Revisiones de Dependencias y Build (FF-DEP-XX)
---

## 5.11 Revisiones de Dependencias y Build (FF-DEP-XX)

SportKid declara versiones solo en `gradle/libs.versions.toml` y su stack es deliberadamente corto: Compose (BOM) + Material 3, MaterialKolor, ZXing y desugaring de `java.time` (`CLAUDE.md` tabla "Qué es SportKid" y §Convenciones → Dependencias). **Cualquier librería nueva (Navigation, Hilt, Room, Retrofit, Firebase, Roborazzi…) se discute antes y se registra en `CLAUDE.md`.** El grafo de módulos también es contrato: `feature:*` → `core:ui` → `core:designsystem` + `core:model`, nunca feature → feature, y `core:designsystem` sin `core:model` (`CLAUDE.md` §Arquitectura → Reglas de dependencia; README §6.1).

| ID | Regla | Severidad |
|---|---|---|
| FF-DEP-01 | Versión literal en un `build.gradle.kts` (`"group:artifact:1.2.3"`, `version "…"` fuera del catálogo) o versión dinámica (`+`, `latest.release`) | 🟠 |
| FF-DEP-02 | Cambio de versiones en el catálogo sin mencionarlo en la descripción del PR; bump mezclado con cambios de código | 🟡 |
| FF-DEP-03 | Librería o plugin nuevo en el catálogo sin registrarlo en `CLAUDE.md` en el mismo PR (framework: Navigation, Hilt/Dagger, Room, Retrofit/OkHttp/Ktor, Firebase, Koin, DataStore, Coil…) | 🟠 |
| FF-DEP-04 | `mavenLocal()` u otro repositorio nuevo en `settings.gradle.kts` (los módulos no pueden declarar repositorios: `FAIL_ON_PROJECT_REPOS`) | 🟠 |
| FF-DEP-05 | Cambios en `compileSdk`/`minSdk`/`targetSdk`/`jvmTarget`, `isMinifyEnabled`, `proguard-rules.pro`, `gradle.properties` o el wrapper; módulo que usa `java.time` sin `isCoreLibraryDesugaringEnabled` | 🟡 informar |
| FF-DEP-06 | Grafo de módulos roto: `project(":feature:…")` dentro de un feature, `core:*` que depende de un feature, `core:designsystem` que depende de `core:model`; módulo nuevo (`include(…)`) sin registrarlo en `CLAUDE.md` | 🔴 / 🟠 |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
BUILD_FILES=('*.gradle.kts')
CHANGED=$(pr_diff --name-only -- 'gradle/libs.versions.toml' "${BUILD_FILES[@]}" gradle.properties 'gradle/wrapper/*' 'app/proguard-rules.pro')
[ -z "$CHANGED" ] && { echo "ℹ️ FF-DEP: sin cambios de build"; exit 0; }
CLAUDE_TOUCHED=$(pr_diff --name-only -- CLAUDE.md)

# FF-DEP-01 — versiones fuera del catálogo o dinámicas
added_lines "${BUILD_FILES[@]}" | grep -E '"[a-z0-9_.-]+:[a-z0-9_.-]+:[0-9][^"]*"|version *"[0-9]|:\+"|latest\.(release|integration)' \
  | sed 's/^/🟠 FF-DEP-01 versión fuera del catálogo o dinámica /'

# FF-DEP-02 — versiones del catálogo
pr_diff -- gradle/libs.versions.toml | grep -E '^[-+][^-+]' | sed 's/^/🟡 FF-DEP-02 libs.versions.toml: /'
[ -n "$PR_MAIN" ] && pr_diff -- gradle/libs.versions.toml | grep -qE '^\+[a-zA-Z].*= *"[0-9]' \
  && echo "🔎 FF-DEP-02 el PR mezcla cambios de versiones con código — ¿debería ir aislado?"

# FF-DEP-03 — librerías y plugins nuevos en el catálogo (deben quedar registrados en CLAUDE.md)
NEW_LIBS=$(pr_diff -- gradle/libs.versions.toml | grep -E '^\+[a-zA-Z0-9-]+ *= *\{' | grep -vE '^\+ *#')
for l in $(printf '%s\n' "$NEW_LIBS" | sed -E 's/^\+([a-zA-Z0-9-]+).*/\1/'); do
  line=$(printf '%s\n' "$NEW_LIBS" | grep -E "^\+$l *=")
  if [ -z "$CLAUDE_TOUCHED" ]; then
    echo "🟠 FF-DEP-03 librería\/plugin nuevo sin registrar en CLAUDE.md: $line"
  else
    echo "🔎 FF-DEP-03 librería nueva y CLAUDE.md cambió — confirmar que la registra (tabla 'Qué es SportKid' o §Dependencias): $line"
  fi
done
printf '%s\n' "$NEW_LIBS" | grep -iE 'navigation|hilt|dagger|koin|room|retrofit|okhttp|ktor|firebase|crashlytics|datastore|serialization|coil|glide|moshi|gson|roborazzi|viewmodel|immutable' \
  | sed 's/^/🟠 FF-DEP-03 framework — decisión de arquitectura: ¿se discutió y se registra en CLAUDE.md? /'
# usar un alias que ya está en el catálogo (p. ej. testImplementation(libs.junit) en un módulo) no es librería nueva
added_lines "${BUILD_FILES[@]}" | grep -E '(implementation|api|testImplementation|androidTestImplementation|debugImplementation|ksp|kapt)\(libs\.' \
  | sed 's/^/ℹ️ FF-DEP-03 alias del catálogo agregado a un módulo: /'

# FF-DEP-04 — repositorios
added_lines settings.gradle.kts "${BUILD_FILES[@]}" | grep -E 'mavenLocal\(\)|maven *\{|maven\(|jcenter\(' \
  | sed 's/^/🟠 FF-DEP-04 repositorio nuevo /'

# FF-DEP-05 — configuración de build
pr_diff -- "${BUILD_FILES[@]}" | grep -E '^[-+].*(compileSdk|minSdk|targetSdk|jvmTarget|JavaVersion|release\(|isMinifyEnabled|isShrinkResources|proguard|isCoreLibraryDesugaringEnabled)' \
  | sed 's/^/🟡 FF-DEP-05 configuración de build: /'
pr_diff --name-only -- gradle.properties 'gradle/wrapper/*' app/proguard-rules.pro | sed 's/^/🟡 FF-DEP-05 archivo de build modificado: /'
for b in app/build.gradle.kts core/*/build.gradle.kts feature/*/build.gradle.kts; do
  m=${b%/build.gradle.kts}
  [ -d "$m/src/main" ] || continue
  grep -q 'kotlin.jvm' "$b" && continue    # core:model es Kotlin puro (JVM)
  grep -rqE '^import java\.time\.' "$m/src/main" 2>/dev/null && ! grep -q 'isCoreLibraryDesugaringEnabled = true' "$b" \
    && echo "🟡 FF-DEP-05 $m usa java.time sin isCoreLibraryDesugaringEnabled (minSdk 24)"
done

# FF-DEP-06 — grafo de módulos
for b in $(pr_diff --name-only -- 'core/*/build.gradle.kts' 'feature/*/build.gradle.kts'); do
  m=${b%/build.gradle.kts}
  added_lines "$b" | grep -E 'project\(":feature:' | while IFS= read -r l; do
    case "$m" in feature/*|core/*) echo "🔴 FF-DEP-06 $m depende de un feature: $l" ;; esac
  done
  [ "$m" = core/designsystem ] && added_lines "$b" | grep -E 'project\(":core:(model|ui)"\)' | sed 's/^/🔴 FF-DEP-06 core:designsystem no conoce el dominio: /'
  [ "$m" = core/model ] && added_lines "$b" | grep -E 'project\(|compose|androidx' | sed 's/^/🔴 FF-DEP-06 core:model es Kotlin puro: /'
done
added_lines settings.gradle.kts | grep -E 'include\(' | while IFS= read -r l; do
  if [ -z "$CLAUDE_TOUCHED" ]; then echo "🟠 FF-DEP-06 módulo nuevo sin registrar en CLAUDE.md (tabla de módulos y §Arquitectura): $l"
  else echo "🔎 FF-DEP-06 módulo nuevo — confirmar namespace com.invex.sportkid.<capa>.<x> y su fila en CLAUDE.md/README §6.1: $l"; fi
done
true
EOF
```

**Cómo interpretar:**
- Hoy no hay Navigation Compose, ViewModel, DI, red, persistencia (salvo `ThemePrefs` con `SharedPreferences`) ni librerías de test más allá de JUnit y Compose UI Test: un PR que agrega cualquiera de ellas cambia la arquitectura. Exige que `CLAUDE.md` (y su espejo `AGENTS.md` vía `python3 .github/scripts/sync_claude_to_copilot.py`) lo registre en el mismo PR y, si liquida deuda (TD-SK-001 tests, TD-SK-002 datos, TD-SK-003 navegación, TD-SK-008 detekt), que actualice la tabla de deuda (ver `sportkid-governance:propagacion`).
- Un bump de dependencias va idealmente en un PR propio, con el changelog relevante (AndroidX/Compose BOM) en la descripción y `./gradlew :app:assembleDebug :app:testDebugUnitTest lint` en verde.
- Si el código usa una API cuyo artefacto llega transitivo, declárala explícitamente en el módulo que la usa vía alias del catálogo.
- **FF-DEP-05:** todos los módulos Android comparten compileSdk 36 / minSdk 24 / JVM 11; un módulo que se sale de eso, o que usa `java.time` sin desugaring, falla en dispositivos con API < 26. Bajar `minSdk` o activar R8 requiere probar un build `release` (`./gradlew :app:assembleRelease`) y mencionarlo en el PR.
- **FF-DEP-06:** la dependencia entre features en Gradle es el mismo error que un `import` cruzado (FF-CORE): la pieza compartida sube a `core:ui`. Solo `:app` conoce a todos los features.
