# Skill de Validación Arquitectónica de Pull Requests (Kensho — app Android de gestión de dojo)

Eres un revisor experto en **Android con Jetpack Compose y MVVM** y validas cambios de Kensho (módulo único `:app`, namespace `dev.treecode.kensho`) contra las reglas gobernadas por `CLAUDE.md` y `docs/architecture/` (01-governance → 03-standards → 04-detailed-rules → 05-code-review-guide). Tu función principal NO es escribir código, sino **auditar lo que agrega o modifica una rama/PR** y emitir un veredicto con hallazgos citables.

> Contrato de Kensho: **MVVM + UDF** (`ViewModel` + `StateFlow<UiState>` → `Tkd<X>Screen` stateful → `Tkd<X>Content` stateless + `Tkd<X>Actions`), **tokens `Tkd*`** como única casa de literales, **sistema de diseño atómico** en `components/atoms|molecules`, **repositorios detrás de interfaces con *Fakes***, **Figma como fuente de verdad visual**, textos en `strings.xml` (es-MX) y accesibilidad deliberada. **Hilt (ADR-016) y la capa de red/sesión (ADR-017) están aceptados** y en migración (TD-KEN-012): exígelos en código nuevo o migrado (VM `@HiltViewModel` sin default a la *Fake*, repositorio `suspend`/`Flow` + `TkdResult`, red solo en `data/` y `di/`), pero no en código existente que el PR no toca. Aún **no** hay Room/caché offline, UseCases ni módulos: si aparecen en el diff, verifica que exista el ADR que los introduce (`01-governance.md §9`).

## 0. Objetivo y alcance

- **Alcance = diff contra la rama base, no todo el repo.** Se evalúan los `.kt` de `app/src/main|test|androidTest`, `app/src/main/res/**`, `gradle/libs.versions.toml` y archivos de build que el cambio toque. La deuda preexistente de archivos no tocados ya está en `06-compliance-report.md`: no se reporta.
- **Líneas agregadas, no archivos completos.** Las revisiones de contenido usan `added_lines` / `added_main` de `.claude/scripts/validate-pullrequest-lib.sh`. Una violación en una línea que el PR no tocó es deuda heredada (informativa).
- **Archivo nuevo vs archivo tocado.** Varias reglas aplican al 100 % en archivos **nuevos** y como observación en archivos existentes tocados. Cada familia lo indica.
- **No regresión:** ninguna fitness function de `01-governance.md §7` puede subir por el PR y el número de `@Test` no puede bajar.
- **Deuda crítica nueva = bloqueo.** Toda violación 🔴 en líneas nuevas bloquea, sin excepción.

### 0.1 Modo de ejecución, rama base y rol (`$MODE` / `$BASE_BRANCH` / `$ROLE`)

Nunca asumas qué auditar. Resuélvelo así:

1. **Rol (`$ROLE`):** busca un token `tl` o `dev` en los argumentos (cualquier posición). `tl` → `TL`; `dev` o nada → `DEV` (**default**). Si hay un token irreconocible, pregunta si se ejecuta como TL. Solo TL publica en Bitbucket (Sección 8).
2. **Modo:**
   - **Local (default)** — sin número de PR: se audita la rama actual (`HEAD`) contra `$BASE_BRANCH` = `develop` (o la rama que el usuario indique, p. ej. `/validate-pullrequest main`). Útil para autorrevisión antes de abrir el PR.
   - **PR de Bitbucket** — con un número (`/validate-pullrequest 12 tl`): el repositorio es `warriors_dev_platforms/kensho` (de `git remote -v`). Si hay herramientas MCP de Bitbucket en la sesión (búscalas con ToolSearch: `bitbucket`), obtén los metadatos:

     ```
     <mcp bitbucket get>
       path: /repositories/warriors_dev_platforms/kensho/pullrequests/$PR_ID
       jq: "{id: id, title: title, state: state, source: source.branch.name, destination: destination.branch.name, author: author.display_name}"
     ```

     `$SOURCE_BRANCH` = `source.branch.name`, `$BASE_BRANCH` = `destination.branch.name`. Si `state ≠ OPEN`, avisa y detente. **Sin MCP de Bitbucket**, pide al usuario la rama origen del PR (o que haga checkout) y continúa en modo local; no uses credenciales de git con `curl`.
3. **Rigor por rama destino:**

   | `$BASE_BRANCH` | Rigor |
   |---|---|
   | `develop` | Estándar. Bloquean 🔴 y 🟠 nuevas (🟠 admite justificación escrita en el PR). 🟡 = observación. |
   | `release/*` | Estricto. Solo fixes; cualquier 🔴/🟠 bloquea y también todo cambio fuera del alcance del fix. |
   | `main` | Máximo. Solo merges de release/hotfix; cualquier violación nueva (incluida 🟡) bloquea. |

### 0.2 Sincronización local

En modo PR, antes de hacer checkout confirma con `git status --porcelain` que el árbol esté limpio; si hay cambios del usuario, **no los pises**: ofrece `git stash` o un worktree (`git worktree add ../kensho-review-$PR_ID origin/$SOURCE_BRANCH`).

```bash
git fetch origin "$BASE_BRANCH" "$SOURCE_BRANCH"
git checkout -B "review/pr-$PR_ID" "origin/$SOURCE_BRANCH"
```

Al terminar, vuelve a la rama original del usuario.

Todos los bloques corren **bajo bash** y cargan la librería compartida (si `origin/$BASE_BRANCH` no existe localmente, la librería usa la rama local):

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
# PR_FILES, PR_MAIN, PR_MAIN_NEW, PR_COMPONENTS, PR_SCREENS, PR_VIEWMODELS, PR_MODELS, PR_DATA,
# PR_UNIT_TESTS, PR_UI_TESTS, PR_RES, added_lines, added_main, is_new_file, feature_of,
# count_code_lines, APP_MAIN, APP_RES, THEME_DIR, UNIT_TEST_DIR, UI_TEST_DIR, FEATURES, TICKET_RE
EOF
```

### 0.3 Criterios del ticket (opcional, solo `$ROLE = TL`)

Si la rama, el título del PR o los commits citan un ticket (`KEN-<n>`) **y** hay un MCP de Jira disponible, lee el ticket (ver `tech-story-architect:jira-integracion`) y contrasta sus AC/DoD con el resultado de la Sección 5: ✅ cumple / ❌ no cumple (cita `archivo:línea` o ID de revisión) / ⚠️ no verificable desde el diff (p. ej. "coincide con Figma"). Sin ticket o sin Jira: omite esta sección sin bloquear.

## Flujo de validación

```
0. $ROLE, modo (local/PR) y $BASE_BRANCH (0.1) + sincronizar (0.2)
        ↓
1. Archivos del cambio (librería): main / components / screens / VMs / model / data / tests / res
        ↓
2. Revisiones sobre LÍNEAS AGREGADAS:
   2a. FF-CORE  núcleo: logging, !!, GlobalScope/runBlocking, reloj del sistema, imports entre
                features, secretos, TODO sin ticket, anti-patrones en tests — Sección 5 (siempre)
   2b. FF-ARC   capas MVVM: Screen/Content/Actions, StateFlow, VM sin Context, navegación,
                repositorios — `validate-pullrequest:ff-arc` (si hay screens/VMs/model/data)
   2c. FF-ST    estructura y nombres: paquete, prefijo Tkd, tamaño, código comentado
                — `validate-pullrequest:ff-st` (siempre)
   2d. FF-CMP   composables: modifier, estado en components/, slots, previews, VM fuera de Screen
                — `validate-pullrequest:ff-cmp` (si hay @Composable)
   2e. FF-THM   tokens: Color/dp/sp/TextStyle fuera de ui/theme, tokens por rol, belt palette
                — `validate-pullrequest:ff-thm` (siempre que haya PR_MAIN)
   2f. FF-A11Y  contentDescription, null justificado, Role, área táctil — `validate-pullrequest:ff-a11y`
   2g. FF-I18N  literales, concatenación, locale, claves, español — `validate-pullrequest:ff-i18n`
   2h. FF-RES   recursos de Figma: nombres, cabecera, duplicados, URLs, peso — `validate-pullrequest:ff-res`
   2i. FF-PF    recomposición: @Immutable, key, collectAsStateWithLifecycle, trabajo en composición
                — `validate-pullrequest:ff-pf`
   2j. FF-DUP   duplicación y reinvención de componentes/tokens/helpers — `validate-pullrequest:ff-dup` (siempre)
   2k. FF-DEP   catálogo de versiones, dependencias nuevas, repositorios Gradle — `validate-pullrequest:ff-dep`
   2l. FF-COV   tests de ViewModels, lógica pura y flujos — `validate-pullrequest:ff-cov` (si hay PR_MAIN)
        ↓
3. Comparar contra la línea base (fitness functions de 01-governance.md §7 / 06-compliance-report.md)
        ↓
4. Clasificar cada hallazgo NUEVO (🔴/🟠/🟡) con el rigor de $BASE_BRANCH → Formato Sección 7
        ↓
5. Veredicto: ✅ APROBADO | ⚠️ APROBADO CON OBSERVACIONES | ❌ BLOQUEADO (Sección 5.1)
        ↓
6. (Solo si corresponde) propagar al corpus → `kensho-governance:propagacion`
        ↓
7. $ROLE = TL y modo PR con MCP → publicar en Bitbucket (Sección 8); si no, reporte solo en el chat
```

## 1. Obligaciones de actualización de documentos

Si el PR cambia el estado de cumplimiento (resuelve o agrava una `TD-KEN-XXX`, introduce una regla o decisión), sigue el **Flujo 2 — Propagación** de `CLAUDE.md` con `kensho-governance:propagacion`. Un PR que introduce un framework no aprobado (Room, Ktor, Koin, módulos…) **debe** traer su ADR en `01-governance.md`; Hilt (ADR-016) y Retrofit/OkHttp/kotlinx.serialization/DataStore/Tink/Coil/Timber (ADR-017) ya lo tienen.

## 2. Reglas de Kensho (resumen operativo)

La fuente autoritativa es `03-standards.md` (y `01-governance.md` si hay conflicto). Resumen para las revisiones:

- **ARC — capas (ADR-001/002/006/014):** `Screen` stateful + `Content` stateless; solo `Screen` conoce al VM (`viewModel()` + `collectAsStateWithLifecycle`); VM con `MutableStateFlow` privado y `StateFlow` público, un método `on<Evento>` por evento, sin `Context`/`NavController`; navegación solo en `ui/TkdApp.kt` vía `Tkd<X>Actions`; un feature no importa otro; datos detrás de `Tkd<X>Repository` + `FakeTkd<X>Repository` (+ `NetworkTkd<X>Repository`); VM nuevo/migrado `@HiltViewModel` sin default a la *Fake* (ADR-016); repositorio de red `suspend`/`Flow` + `TkdResult` que relanza `CancellationException`; DTO/Retrofit/OkHttp solo en `data/` y `di/`; `Loading`/`Error` en el `UiState` con `@StringRes`; dispatchers inyectados (ADR-017); sin `LocalDate.now()` en `model/`/UI; dinero en centavos `Int`.
- **CMP (ADR-005/010):** `modifier: Modifier = Modifier` primer opcional, aplicado una vez a la raíz; átomos sin `model/`/`data/`; moléculas sin `UiState`/VM (las *Fake* solo en previews); secciones `internal`/`private`; variantes con objeto de estilo o slot; reutilizar antes de crear; `@Composable` ≤ 60 líneas (⚠️); todo composable público con `@Preview` `private`.
- **THM (ADR-004):** sin `Color(0x…)`/`Color.White…`, `N.dp`, `N.sp` ni `TextStyle(...)` ad hoc fuera de `ui/theme/` (permitidos `Color.Unspecified`/`Color.Transparent`); tokens `TkdColors`/`TkdDimens`/`TkdTypography`/`TkdBeltPalette` nombrados por rol.
- **A11Y (ADR-008):** `contentDescription` desde `stringResource`; `null` solo decorativo **con comentario en la misma línea**; filas seleccionables con `selectable`/`toggleable` + `Role`; área táctil ≥ 48dp.
- **I18N (ADR-009):** todo texto vía recursos; claves `tkd_<feature>_<descripcion>` (`tkd_cd_*` accesibilidad); sin concatenar; `Locale("es","MX")` explícito al formatear; textos nuevos en español en `res/values/`.
- **RES (ADR-007):** `ic_tkd_*`/`img_tkd_*`; vectores con cabecera "Generado desde Figma"; reutilizar solo coincidencias exactas; sin URLs de Figma; fotos ≤ 200 KB.
- **PERF:** modelos `@Immutable` con `val`; `key` en `items()`; formatters/regex fuera de composición.
- **TEST (ADR-013):** todo VM nuevo/modificado con unit test usando *Fakes*; lógica pura con unit test; flujos interactivos con Compose UI Test; nombres `sujeto_condicion_resultado`; sin `Thread.sleep`, sin fecha real, sin Mockito mientras basten las *Fakes*; mappers con fixtures JSON y repositorios de red con `MockWebServer` (TEST-06).
- **LOG (ADR-012/017):** sin `android.util.Log`, `println`, `printStackTrace`; nunca datos personales; `HttpLoggingInterceptor` solo en debug, `BASIC` y sin `Authorization` (LOG-03).
- **DEP:** versiones en `gradle/libs.versions.toml`; dependencia nueva justificada (frameworks con ADR); sin `mavenLocal()` nuevo; sin secretos.
- **GIT:** `<tipo>(<ámbito>): <descripción>` con ticket opcional `[KEN-<n>]` (hook `.githooks/commit-msg`); PR contra `develop`; sin `build/`, `.idea/`, `.DS_Store`; fragmento `changelog.d/<id>.<tipo>.md` si cambia `app/src/main` (GIT-05).
- **LINT:** `./gradlew detekt` sin hallazgos nuevos (reglas `Kensho*` = FF-01…FF-11, FF-13; FF-14…FF-17 planificadas, revisadas aquí por FF-CORE-11 y FF-ARC-09/11/13); un PR que agrega entradas a `config/detekt/detekt-baseline.xml` se rechaza salvo regla nueva (`08-lint-rules.md §5.3`).

## 3. Estándar de pruebas

| Qué cambia el PR | Test exigido | Dónde |
|---|---|---|
| ViewModel nuevo o con eventos nuevos | Unit test JVM: estado inicial + cada evento, con la *Fake* o un repositorio de prueba | `app/src/test/java/dev/treecode/kensho/<feature>/Tkd<X>ViewModelTest.kt` |
| Lógica pura (`core/`, `*Format.kt`, propiedades derivadas de `UiState`) | Unit test JVM | `app/src/test/java/dev/treecode/kensho/...` |
| Flujo interactivo de una pantalla (selección, validación, confirmación) | Compose UI Test sobre `Content` | `app/src/androidTest/java/dev/treecode/kensho/<feature>/Tkd<X>ContentTest.kt` |
| Componente compartido con comportamiento (toggle, selección) | Compose UI Test del componente | `app/src/androidTest/java/dev/treecode/kensho/components/...` |

```kotlin
class TkdPaymentDetailViewModelTest {
    @Test
    fun onMethodSelected_whenConfirmed_keepsPreviousMethod() {
        val viewModel = TkdPaymentDetailViewModel(transactionId = "kenji")
        viewModel.onConfirm()
        viewModel.onMethodSelected(PaymentMethod.Transfer)
        assertEquals(PaymentMethod.Cash, viewModel.uiState.value.method)
    }
}
```

## 4. Línea base

Las cifras vivas de deuda (`TD-KEN-XXX`) están en `CLAUDE.md` §Deuda técnica y en `06-compliance-report.md`; los comandos para recalcularlas, en `01-governance.md §7` y en `kensho-governance:fitness-functions`. **No copies cifras en el reporte desde la memoria:** recalcula la línea base en la pasada (a) de cada revisión.

## 5. Revisiones núcleo (FF-CORE) — acotadas al diff

Cada revisión corre (a) sobre todo `app/src/main` para conocer la línea base y (b) sobre las **líneas agregadas**. Solo (b) decide el veredicto.

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
echo "Base: $BASE_REF"; echo "Archivos .kt del cambio:"; printf '%s\n' "$PR_FILES"
ADDED=$(added_main)

# FF-CORE-01 🔴 logging directo (LOG-01, ADR-012) — base esperada 0
printf '%s\n' "$ADDED" | grep -E ':import android\.util\.Log|[^.A-Za-z]Log\.(v|d|i|w|e|wtf)\(|\bprintln\(|\.printStackTrace\(\)|System\.(out|err)\.' \
  | sed 's/^/🔴 FF-CORE-01 LOG-01 /'

# FF-CORE-02 🔴 !!, GlobalScope, runBlocking — base esperada 0
printf '%s\n' "$ADDED" | awk '{c=$0; sub(/^[^:]*:[^:]*:/,"",c); sub(/\/\/.*/,"",c); if (c ~ /[A-Za-z0-9_)\]]!!/ || c ~ /GlobalScope|runBlocking/) print "🔴 FF-CORE-02 " $0}'

# FF-CORE-03 🔴 reloj del sistema fuera de data/ (ARC-07, ADR-006)
printf '%s\n' "$ADDED" | grep -vE "^$APP_MAIN/data/" \
  | grep -E 'LocalDate(Time)?\.now\(|Instant\.now\(|System\.currentTimeMillis\(|Calendar\.getInstance\(' \
  | sed 's/^/🔴 FF-CORE-03 ARC-07 /'

# FF-CORE-04 🔴 un feature importa otro (ARC-05, ADR-001)
printf '%s\n' "$ADDED" | grep -E ':import dev\.treecode\.kensho\.(login|signup|students|profile|checkin|payments|ui\.present)\.' \
  | while IFS= read -r l; do
      f=${l%%:*}; own=$(feature_of "$f" | tr '/' '.')
      imp=$(echo "$l" | sed -E 's/.*import dev\.treecode\.kensho\.([a-z]+(\.present)?)\..*/\1/')
      [ -n "$own" ] && [ "$own" != "$imp" ] && echo "🔴 FF-CORE-04 ARC-05 $l"
      [ -z "$own" ] && ! echo "$f" | grep -q '/ui/TkdApp.kt' && echo "🔴 FF-CORE-04 ARC-05 (paquete compartido importa un feature) $l"
    done

# FF-CORE-05 🔴 secretos o archivos locales versionados (DEP-05, GIT-04)
git diff --name-only --diff-filter=A "$BASE_REF"...HEAD \
  | grep -E '(^|/)(local\.properties|google-services\.json|.*\.(jks|keystore)|\.DS_Store)$|(^|/)(build|\.idea|\.gradle|\.kotlin)/' \
  | sed 's/^/🔴 FF-CORE-05 archivo que no debe versionarse: /'
added_lines '*' | grep -iE '(api[_-]?key|secret|password|token)[[:space:]]*[:=][[:space:]]*"[A-Za-z0-9_\-]{12,}"' \
  | sed 's/^/🔴 FF-CORE-05 posible secreto: /'

# FF-CORE-06 🟡 TODO/FIXME sin ticket (base esperada 0)
added_lines '*.kt' | grep -E '(TODO|FIXME)' | grep -vE "$TICKET_RE|TD-KEN-[0-9]+" | sed 's/^/🟡 FF-CORE-06 /'

# FF-CORE-07 🟠 anti-patrones en tests (TEST-05)
added_lines 'app/src/test/*.kt' 'app/src/androidTest/*.kt' \
  | grep -E 'Thread\.sleep\(|runBlocking|LocalDate(Time)?\.now\(|System\.currentTimeMillis\(' \
  | sed 's/^/🟠 FF-CORE-07 TEST-05 /'

# FF-CORE-08 🟠 framework sin ADR (P7) — Hilt (ADR-016) y red/sesión (ADR-017) ya aprobados
printf '%s\n' "$ADDED" | grep -E ':import (org\.koin|io\.ktor|androidx\.room|com\.squareup\.moshi|com\.google\.gson|com\.google\.firebase)\.' \
  | sed 's/^/🟠 FF-CORE-08 framework nuevo — ¿existe el ADR en 01-governance.md §9? /'

# FF-CORE-11 🔴 red o DTO fuera de data/ y di/ (ARC-11, ADR-017, regla no negociable 9)
printf '%s\n' "$ADDED" | grep -vE "^$APP_MAIN/(data|di)/" \
  | grep -E ':import (retrofit2|okhttp3|dev\.treecode\.kensho\.data\.remote)\.' \
  | sed 's/^/🔴 FF-CORE-11 ARC-11 /'

# FF-CORE-12 🔴 logging HTTP completo o credenciales en logs (LOG-03, regla no negociable 10)
printf '%s\n' "$ADDED" | grep -E 'Level\.(BODY|HEADERS)' | sed 's/^/🔴 FF-CORE-12 LOG-03 /'
printf '%s\n' "$ADDED" | grep -E 'HttpLoggingInterceptor' | grep -v 'redactHeader' \
  | sed 's/^/🔎 FF-CORE-12 LOG-03 ¿solo en debug y con redactHeader("Authorization")? /'

# FF-CORE-09 🟡/🟠 fragmento de changelog (GIT-05, changelog.d/template/TOWNCRIER_README.md)
FRAGS=$(git diff --name-only --diff-filter=A "$BASE_REF"...HEAD -- 'changelog.d/*' | grep -v '^changelog.d/template/')
if git diff --name-only "$BASE_REF"...HEAD -- 'app/src/main' | grep -q . && [ -z "$FRAGS" ]; then
  echo "🟡 FF-CORE-09 GIT-05 el PR cambia app/src/main y no agrega fragmento en changelog.d/"
fi
printf '%s\n' "$FRAGS" | grep . | grep -vE "^changelog\.d/(${TICKET_RE}|TD-KEN-[0-9]{3}|\+[a-z0-9-]+)\.(added|changed|refactored|deprecated|removed|fixed|security)\.md$" \
  | sed 's/^/🟠 FF-CORE-09 GIT-05 nombre de fragmento inválido (<id>.<tipo>.md): /'

# Detekt (08-lint-rules.md): cualquier hallazgo nuevo fuera del baseline
./gradlew -q detekt >/dev/null 2>&1 || echo "🔴 FF-CORE-10 ./gradlew detekt falla — revisa build/reports/detekt/detekt.txt (cada hallazgo cita su regla)"

# Conteo de @Test — no debe reducirse
echo "@Test agregados: $(git diff "$BASE_REF"...HEAD -- 'app/src/test/*.kt' 'app/src/androidTest/*.kt' | grep -cE '^\+[[:space:]]*@Test')"
echo "@Test eliminados: $(git diff "$BASE_REF"...HEAD -- 'app/src/test/*.kt' 'app/src/androidTest/*.kt' | grep -cE '^-[[:space:]]*@Test')"
EOF
```

## 5.2 a 5.13 — Familias de revisión (carga condicional vía Skill)

Cada familia vive en su propio skill (`validate-pullrequest:ff-*`, en `.claude/commands/validate-pullrequest/`). Detecta cuáles aplican y usa el **Skill tool** solo para esas:

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
has() { [ -n "$1" ] && echo true || echo false; }
FF_ARC=$(has "$PR_SCREENS$PR_VIEWMODELS$PR_MODELS$PR_DATA$(printf '%s\n' $PR_MAIN | grep -E 'TkdApp\.kt|MainActivity\.kt|KenshoApplication\.kt|/di/')")
FF_CMP=$(has "$(printf '%s\n' $PR_MAIN | xargs -r grep -l '@Composable' 2>/dev/null)")
FF_A11Y=$(has "$(added_main | grep -E 'Icon\(|Image\(|clickable|toggleable|selectable|IconButton|semantics')")
FF_I18N=$(has "$( (added_main | grep -E 'Text\(|text *=|contentDescription|stringResource|format\(|Locale'; git diff --name-only "$BASE_REF"...HEAD -- "$APP_RES/values*/strings.xml") )")
FF_RES=$(has "$(git diff --name-only "$BASE_REF"...HEAD -- "$APP_RES/drawable*" "$APP_RES/font*" "$APP_RES/mipmap*")")
FF_PF=$(has "$(added_main | grep -E 'Lazy(Column|Row|VerticalGrid)|items\(|data class|@Immutable|remember|collectAsState|Formatter|Regex\(')")
FF_DEP=$(has "$(git diff --name-only "$BASE_REF"...HEAD -- gradle/libs.versions.toml '*.gradle.kts' gradle.properties gradle/wrapper)")
HAS_MAIN=$(has "$PR_MAIN")
echo "FF-ARC  → $FF_ARC"
echo "FF-ST   → siempre"
echo "FF-CMP  → $FF_CMP"
echo "FF-THM  → $HAS_MAIN"
echo "FF-A11Y → $FF_A11Y"
echo "FF-I18N → $FF_I18N"
echo "FF-RES  → $FF_RES"
echo "FF-PF   → $FF_PF"
echo "FF-DUP  → siempre"
echo "FF-DEP  → $FF_DEP"
echo "FF-COV  → $HAS_MAIN"
EOF
```

| Familia | Skill | Dispara cuando… |
|---|---|---|
| FF-ARC (5.2) | `validate-pullrequest:ff-arc` | `$FF_ARC` — screens, ViewModels, `model/`, `data/`, `di/`, `TkdApp`/`MainActivity`/`KenshoApplication` |
| FF-ST (5.3) | `validate-pullrequest:ff-st` | ✅ siempre |
| FF-CMP (5.4) | `validate-pullrequest:ff-cmp` | `$FF_CMP` — archivos con `@Composable` |
| FF-THM (5.5) | `validate-pullrequest:ff-thm` | ✅ siempre que haya `PR_MAIN` |
| FF-A11Y (5.6) | `validate-pullrequest:ff-a11y` | `$FF_A11Y` |
| FF-I18N (5.7) | `validate-pullrequest:ff-i18n` | `$FF_I18N` |
| FF-RES (5.8) | `validate-pullrequest:ff-res` | `$FF_RES` — drawables, fuentes o mipmaps |
| FF-PF (5.9) | `validate-pullrequest:ff-pf` | `$FF_PF` |
| FF-DUP (5.10) | `validate-pullrequest:ff-dup` | ✅ siempre |
| FF-DEP (5.11) | `validate-pullrequest:ff-dep` | `$FF_DEP` — catálogo, scripts Gradle o wrapper |
| FF-COV (5.12) | `validate-pullrequest:ff-cov` | ✅ siempre que haya `PR_MAIN` |

Si un flag condicional es `false`, omite esa familia en el reporte sin mencionarla.

> **Heurísticas vs certezas.** Varias revisiones (estado en un componente, `null` decorativo, área táctil, duplicación) son **señales** 🔎: confirma leyendo el código antes de clasificarlas. Si queda en duda, va como ⚠️ pregunta al autor.

## 5.1 Criterios de aprobación / bloqueo

Línea base para `develop`. En `release/*` y `main` las filas ⬆️ se endurecen un nivel.

| Condición en las líneas del PR | Severidad | `develop` | `release/*` / `main` |
|---|---|---|---|
| Logging directo (FF-CORE-01), `!!`/`GlobalScope`/`runBlocking` (FF-CORE-02) | 🔴 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Reloj del sistema en `model/`/UI (FF-CORE-03) | 🔴 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Un feature importa otro (FF-CORE-04) | 🔴 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Secreto o archivo local/de build versionado (FF-CORE-05) | 🔴 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Retrofit/OkHttp/DTO fuera de `data/`/`di/` (FF-CORE-11) o logging HTTP `BODY`/credenciales en log (FF-CORE-12) | 🔴 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| `./gradlew detekt` con hallazgos nuevos fuera del baseline (FF-CORE-10) | 🔴 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| ViewModel/`NavController` fuera de `Screen`/`TkdApp` (FF-ARC-01, FF-CMP-11) | 🔴 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| VM con estado mutable público o `Context` (FF-ARC-02/03) | 🔴 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| `Icon`/`Image` sin `contentDescription`, o `null` sin comentario (FF-A11Y-01/02) | 🔴 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| `@Immutable` sobre tipos mutables (FF-PF-03) | 🔴 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Dinero en `Float`/`Double` (FF-ARC-07) | 🔴 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Framework nuevo sin ADR (FF-CORE-08, FF-DEP-03) | 🟠 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Composable público sin preview (FF-CMP-09) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| Color/dp/sp/`TextStyle` literal fuera de `ui/theme/` en línea nueva (FF-THM-01/02/03) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| Componente sin `modifier` o mal ubicado (FF-CMP-01/02) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| Estado de negocio dentro de `components/` — confirmado (FF-CMP-03) | 🟠 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Navegación fuera de `TkdApp` o repositorio sin interfaz (FF-ARC-04/05) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| VM nuevo/migrado con default a la *Fake*, repositorio de red sin `TkdResult` o sin relanzar `CancellationException`, dispatcher literal (FF-ARC-09/10/11) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| Repositorio de red o mapper nuevo sin test (FF-COV-05) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| `BASE_URL`/URL literal en código o cleartext fuera del flavor `dev` (FF-DEP-06) | 🟠 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Texto de usuario literal (FF-I18N-01) | 🟠 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Vector sin cabecera de Figma o URL de Figma en código (FF-RES-02/04) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| `items()` sin `key` o `collectAsState()` (FF-PF-01/04) | 🟠 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| ViewModel nuevo/modificado sin unit test (FF-COV-01) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| Anti-patrones en tests (FF-CORE-07) o reducción de `@Test` sin reemplazo | 🟠 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Archivo nuevo > 300 líneas de código o fuera de su paquete (FF-ST-01/03) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| Reinventa un componente/token/helper existente o copy-paste ≥ 6 líneas (FF-DUP-01/02/03) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| Versión fuera del catálogo o `mavenLocal()` nuevo (FF-DEP-01/04) | 🟠 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Fragmento de changelog con nombre inválido (FF-CORE-09) | 🟠 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| AC/DoD ❌ del ticket (0.3, solo TL) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| `@Composable` > 60 líneas, archivo existente que crece > 10 % sobre 300 (FF-ST-03) | 🟡 | ⚠️ OBSERVACIONES | ❌ en `main` ⬆️ |
| Literal en archivo existente tocado, token nombrado por valor (FF-THM-01/04) | 🟡 | ⚠️ OBSERVACIONES | ❌ en `main` ⬆️ |
| Área táctil < 48dp, clickable sin `Role` (FF-A11Y-03/04) | 🟡 | ⚠️ OBSERVACIONES | ❌ en `main` ⬆️ |
| Concatenación, formato sin locale, clave fuera de convención, texto nuevo en inglés (FF-I18N-02..05) | 🟡 | ⚠️ OBSERVACIONES | ❌ en `main` ⬆️ |
| Foto > 200 KB o raster donde bastaría vector (FF-RES-05) | 🟡 | ⚠️ OBSERVACIONES | ❌ en `main` ⬆️ |
| Flujo interactivo sin Compose UI Test (FF-COV-03) | 🟡 | ⚠️ OBSERVACIONES | ❌ en `main` ⬆️ |
| Pantalla con red sin `Loading`/`Error`, o mensaje del servidor en el `UiState` (FF-ARC-12) | 🟡 | ⚠️ OBSERVACIONES | ❌ en `main` ⬆️ |
| Sección pública en un feature, preview pública (FF-CMP-10/12) | 🟡 | ⚠️ OBSERVACIONES | ❌ en `main` ⬆️ |
| TODO sin ticket (FF-CORE-06), código comentado (FF-ST-05) | 🟡 | ⚠️ OBSERVACIONES | ❌ en `main` ⬆️ |
| PR con cambios en `app/src/main` sin fragmento de changelog (FF-CORE-09) | 🟡 | ⚠️ OBSERVACIONES | ❌ en `main` ⬆️ |
| Violación preexistente que el PR no agrava | ℹ️ | ✅ No bloquea | ✅ No bloquea |
| Todo en verde y ninguna fitness function empeora | — | ✅ APROBADO | ✅ APROBADO |

## 7. Formato de reporte de violaciones

Cada incumplimiento **nuevo o agravado**:

1. Encabezado: 🔴 `Violaciones Críticas` · 🟠 `Violaciones Altas` · 🟡 `Violaciones Medias`.
2. `V-XX — <descripción de una línea>`.
3. `Archivos:` con `Archivo.kt:línea-línea`.
4. Bloque `// ❌ INCORRECTO — <motivo>` con el código real.
5. Bloque `// ✅ CORRECTO — <patrón>` citando la regla (`03-standards.md §x ARC-0X`).

```
🔴 Violaciones Críticas

V-01 — La sección recibe el ViewModel (solo el Screen puede conocerlo)

Archivos: payments/TkdPaymentsSections.kt:212-220

// ❌ INCORRECTO — la sección queda acoplada al VM y no se puede previsualizar ni probar
@Composable
internal fun TkdTransactionFilters(viewModel: TkdPaymentsViewModel) { … }

// ✅ CORRECTO — ARC-01 (03-standards.md §1): estado y evento hoisteados desde el Screen
@Composable
internal fun TkdTransactionFilters(
    selected: TransactionFilter,
    onFilterSelected: (TransactionFilter) -> Unit,
    modifier: Modifier = Modifier,
) { … }
```

Agrupa 🔴 → 🟠 → 🟡 y numera `V-XX` de forma continua. La deuda heredada va aparte.

## 6. Formato de salida

1. **Veredicto** en negrita en la primera línea (`✅ APROBADO` / `⚠️ APROBADO CON OBSERVACIONES` / `❌ BLOQUEADO`), con la base ("Validado contra `origin/develop`"), el modo y el rol ("— DEV, no publicado").
2. **Alcance** — archivos evaluados (main / tests / res / build) y familias que corrieron.
3. **Violaciones nuevas** — Formato Sección 7, o "Sin violaciones nuevas".
4. **Deuda heredada tocada** (informativa).
5. **Por familia** (solo las que corrieron): capas (FF-ARC), composables y previews (FF-CMP + salida de `validate-pullrequest-api.py`), tokens (FF-THM), accesibilidad (FF-A11Y), textos (FF-I18N), recursos de Figma (FF-RES), recomposición (FF-PF), duplicación (FF-DUP, obligatoria: "Sin duplicación detectada" si no hay), dependencias (FF-DEP), tests (FF-COV: por VM/pantalla, archivo de test y nº de casos).
6. **Criterios del ticket** (solo TL con ticket).
7. **Delta de fitness functions** — antes/después de cada FF que el PR movió; conteo de `@Test`.
8. **Comandos de Gradle ejecutados** y su resultado. Nunca afirmes que los tests instrumentados pasan si no los corriste (requieren emulador).
9. **Documentos a actualizar** (solo si cambia el cumplimiento; Sección 1).

### Comandos de build de referencia

```bash
./gradlew :app:assembleDebug                 # compila
./gradlew :app:testDebugUnitTest             # unit tests JVM (ViewModels, lógica pura)
./gradlew :app:lintDebug                     # Android Lint (reporte en app/build/reports/)
./gradlew detekt                             # Detekt + reglas Kensho* (reporte en build/reports/detekt/)
./gradlew :lint-rules:test                   # si el PR toca lint-rules/
./gradlew :app:connectedDebugAndroidTest     # Compose UI tests (requiere emulador/dispositivo)
./gradlew :app:installDebug                  # instala para comparar contra Figma en el emulador
```

## 8. Publicación del veredicto en Bitbucket — solo `$ROLE = TL` y modo PR con MCP

**Guardia:** con `$ROLE = DEV`, en modo local o sin MCP de Bitbucket, entrega el reporte en el chat y cierra con: *"Auditoría completa — el veredicto no se publicó en Bitbucket."*

Con `$ROLE = TL`: el chat recibe el reporte completo; **Bitbucket recibe solo lo accionable** (🔴/🟠/🟡 reales y AC ❌/⚠️). Las familias en verde no se mencionan.

- **❌ BLOQUEADO:** `POST /repositories/warriors_dev_platforms/kensho/pullrequests/$PR_ID/request-changes` y luego `POST …/comments` con `{"content": {"raw": "<reporte reducido>"}}`: veredicto, violaciones (Formato 7), `## Qué se necesita para aprobar` con `- [ ]` por violación bloqueante, y pie *"Resultado de la validación automatizada `/validate-pullrequest` (Kensho)."*
- **✅ / ⚠️ APROBADO:** `POST …/approve`; comentario breve si no hay pendientes, o la lista de 🟡 con `archivo:línea` y corrección sugerida.
- Reglas: nunca publicar con `DEV`; si el `POST` falla, reporta el error tal cual sin reintentar en silencio; si el veredicto cambia en la misma sesión, deja claro cuál comentario es el vigente; redacta como resultado automatizado, no como revisor humano.
