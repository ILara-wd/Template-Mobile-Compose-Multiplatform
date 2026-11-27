## 5.11 Revisiones de Dependencias y Build (FF-DEP-XX)

Kensho declara versiones en `gradle/libs.versions.toml` (`03-standards.md §11`, DEP-01..05, TD-KEN-009). Un framework nuevo es una decisión arquitectónica: requiere ADR (`01-governance.md §9`). Ya aprobados: Hilt + KSP (ADR-016); Retrofit, OkHttp, kotlinx.serialization, DataStore, Tink, Coil, Timber y `mockwebserver`/`kotlinx-coroutines-test` (ADR-017).

| ID | Regla | Severidad |
|---|---|---|
| FF-DEP-01 | Versión literal en `build.gradle.kts` (`"group:artifact:1.2.3"`, `version "…"` fuera del catálogo) o versión dinámica (`+`, `latest.release`) (DEP-01) | 🟠 |
| FF-DEP-02 | Cambio de versiones en el catálogo sin mencionarlo en la descripción del PR; bump mayor (x.0.0) mezclado con cambios de código | 🟡 |
| FF-DEP-03 | Dependencia o plugin nuevo sin justificación; framework nuevo sin ADR (DEP-03) | 🟠 |
| FF-DEP-04 | `mavenLocal()` u otro repositorio nuevo en `settings.gradle.kts` / `build.gradle.kts` (DEP-04) | 🟠 |
| FF-DEP-05 | Cambios en `compileSdk`/`minSdk`/`targetSdk`, `isMinifyEnabled`, `proguard`/`keepRules`, `gradle.properties` o el wrapper | 🟡 informar |
| FF-DEP-06 | URL literal (`http(s)://`) en código Kotlin fuera de `buildConfigField`; `cleartextTrafficPermitted="true"` fuera del flavor `dev` (DEP-06) | 🟠 |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
BUILD_FILES='*.gradle.kts'

added_lines "$BUILD_FILES" | grep -E '"[a-z0-9_.-]+:[a-z0-9_.-]+:[0-9][^"]*"|version *"[0-9]|:\+"|latest\.(release|integration)' \
  | sed 's/^/🟠 FF-DEP-01 versión fuera del catálogo o dinámica /'

git diff "$BASE_REF"...HEAD -- gradle/libs.versions.toml | grep -E '^[-+][^-+]' | sed 's/^/🟡 FF-DEP-02 libs.versions.toml: /'
[ -n "$PR_MAIN" ] && git diff "$BASE_REF"...HEAD -- gradle/libs.versions.toml | grep -qE '^\+[a-zA-Z].*= *"' \
  && echo "🔎 FF-DEP-02 el PR mezcla cambios de versiones con código — ¿debería ir aislado?"

NEW_LIBS=$( (git diff "$BASE_REF"...HEAD -- gradle/libs.versions.toml | grep -E '^\+[a-z0-9-]+ *= *\{'; \
             added_lines "$BUILD_FILES" | grep -E '(implementation|api|ksp|kapt|testImplementation|androidTestImplementation)\(|alias\(libs\.plugins') )
[ -n "$NEW_LIBS" ] && printf '%s\n' "$NEW_LIBS" | sed 's/^/🔎 FF-DEP-03 dependencia\/plugin nuevo — exigir justificación en el PR: /'
printf '%s\n' "$NEW_LIBS" | grep -iE 'koin|room|ktor|firebase|crashlytics|moshi|gson|apollo|realm' \
  | sed 's/^/🟠 FF-DEP-03 framework nuevo — ¿ADR en 01-governance.md §9? /'
printf '%s\n' "$NEW_LIBS" | grep -iE 'hilt|dagger|ksp|retrofit|okhttp|serialization|datastore|tink|coil|timber|mockwebserver' \
  | sed 's/^/ℹ️ FF-DEP-03 aprobado por ADR-016\/017 — verificar versión compatible con AGP 9.4 \/ Kotlin 2.2: /'

# FF-DEP-06 — configuración de red (ADR-017)
added_main | grep -E '"https?://' | sed 's/^/🟠 FF-DEP-06 URL literal — usa BuildConfig.BASE_URL /'
added_lines 'app/src/main/res/xml/*.xml' | grep -E 'cleartextTrafficPermitted="true"' | sed 's/^/🟠 FF-DEP-06 cleartext en main /'

added_lines 'settings.gradle.kts' "$BUILD_FILES" | grep -E 'mavenLocal\(\)|maven *\{|maven\(' | sed 's/^/🟠 FF-DEP-04 repositorio nuevo /'

git diff "$BASE_REF"...HEAD -- app/build.gradle.kts | grep -E '^[-+].*(compileSdk|minSdk|targetSdk|release\(|isMinifyEnabled|isShrinkResources|proguard|optimization)' \
  | sed 's/^/🟡 FF-DEP-05 configuración de build: /'
git diff --name-only "$BASE_REF"...HEAD -- gradle.properties gradle/wrapper app/proguard-rules.pro 'app/src/main/keepRules/*' \
  | sed 's/^/🟡 FF-DEP-05 archivo de build modificado: /'
true
EOF
```

**Cómo interpretar:**
- Un bump de dependencias va idealmente en un PR propio, con la lista de cambios relevantes (changelog de AndroidX) en la descripción y `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug` en verde.
- Si el código usa una API cuyo artefacto llega transitivo (p. ej. `collectAsStateWithLifecycle` → `lifecycle-runtime-compose`), declárala explícitamente (DEP-02 de `03-standards.md`).
- Bajar `minSdk` o cambiar R8/keep rules requiere probar un build `release` (`./gradlew :app:assembleRelease`) y mencionarlo en el PR.
