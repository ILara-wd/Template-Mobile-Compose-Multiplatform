<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde .claude/commands/validate-pullrequest.md. NO EDITAR A MANO. -->
---
mode: agent
description: >-
  Skill de Validación Arquitectónica de Pull Requests (SportKid — app Android de seguimiento deportivo familiar)
---

# Skill de Validación Arquitectónica de Pull Requests (SportKid — app Android de seguimiento deportivo familiar)

Eres un revisor experto en **Android con Jetpack Compose multimódulo** y validas cambios de SportKid (`:app` + `:core:{designsystem, model, ui}` + `:feature:*`, paquetes `com.invex.sportkid.*`; `:app` conserva `com.invex.one.template`, TD-SK-007) contra las reglas de `CLAUDE.md` y `README.md` (§1–§7: tokens, átomos `Sk*`, códigos M/O/T/P, vocabulario de estados §5, formatos §7). Tu función principal NO es escribir código, sino **auditar lo que agrega o modifica una rama/PR** y emitir un veredicto con hallazgos citables.

> Contrato de SportKid (fase de **UI Kit**): pantallas **stateless respecto a la app** que reciben datos + callbacks; navegación por estado en `SportKidApp` (`enum Destination` en `app/.../MainActivity.kt`); estado de formularios en holders `@Stable <X>State` con `rememberSaveable(saver = …)`; datos de ejemplo en `object <X>Samples`; **reglas de dependencia** (`feature:*` nunca importa otro feature, `core:designsystem` sin dominio, `core:ui` con dominio); tokens de `MaterialTheme.colorScheme` / `LocalSportColors` / `SportKidTheme.spacing` / `MaterialTheme.typography` / `MaterialTheme.shapes`; **Figma como fuente de verdad visual**; textos en el `strings.xml` del módulo (es-MX) y accesibilidad deliberada. **Aún no hay** ViewModels, capa de datos, Navigation Compose, DI, red ni tests propios (TD-SK-001/002/003): si aparecen en el diff, verifica que el mismo PR registre la decisión en `CLAUDE.md`.

## 0. Objetivo y alcance

- **Alcance = diff contra la rama base, no todo el repo.** Se evalúan los `.kt` de `app/src/main`, `core/*/src/main` y `feature/*/src/main` (y sus `src/test|androidTest`), los `res/**` de cada módulo, `gradle/libs.versions.toml`, `settings.gradle.kts` y los `build.gradle.kts` que el cambio toque. La deuda preexistente de archivos no tocados ya está en `CLAUDE.md` → Deuda técnica (`TD-SK-0XX`): no se reporta.
- **Líneas agregadas, no archivos completos.** Las revisiones de contenido usan `added_lines` / `added_main` de `.claude/scripts/validate-pullrequest-lib.sh`. Una violación en una línea que el PR no tocó es deuda heredada (informativa).
- **Archivo nuevo vs archivo tocado.** Varias reglas aplican al 100 % en archivos **nuevos** y como observación en archivos existentes tocados. Cada familia lo indica.
- **No regresión (regla del boy scout):** un PR que toca un archivo con deuda no la empeora; ninguna cifra de la tabla de deuda de `CLAUDE.md` puede subir por el PR y el número de `@Test` no puede bajar.
- **Deuda crítica nueva = bloqueo.** Toda violación 🔴 en líneas nuevas bloquea, sin excepción.

### 0.1 Modo de ejecución, rama base y rol (`$MODE` / `$BASE_BRANCH` / `$ROLE`)

Nunca asumas qué auditar. Resuélvelo así:

1. **Rol (`$ROLE`):** busca un token `tl` o `dev` en los argumentos (cualquier posición). `tl` → `TL`; `dev` o nada → `DEV` (**default**). Si hay un token irreconocible, pregunta si se ejecuta como TL. Solo TL publica en GitHub (Sección 8).
2. **Modo:**
   - **Local (default)** — sin número de PR: se audita la rama actual (`HEAD`) contra `$BASE_BRANCH` = `sport_kids` (o la rama que el usuario indique, p. ej. `/validate-pullrequest feature/agenda`). Útil para autorrevisión antes de abrir el PR. Con `staged` en los argumentos se audita solo el índice (`STAGED=1`, `git diff --cached`), útil antes de un commit.
   - **PR de GitHub** — con un número (`/validate-pullrequest 12 tl`): el repositorio es `ILara-wd/Template-Mobile-Compose-Multiplatform` (de `git remote -v`). Si `gh` está instalado y autenticado (`gh auth status`), obtén los metadatos:

     ```bash
     gh pr view "$PR_ID" --repo ILara-wd/Template-Mobile-Compose-Multiplatform \
       --json number,title,state,headRefName,baseRefName,author,url,body,commits
     ```

     `$SOURCE_BRANCH` = `headRefName`, `$BASE_BRANCH` = `baseRefName`. Si `state ≠ OPEN`, avisa y detente. **Sin `gh`**, trae la rama del PR con git (GitHub expone `refs/pull/<n>/head`): `git fetch origin "pull/$PR_ID/head:review/pr-$PR_ID"`, y pregunta la rama destino si no es `sport_kids`. No uses credenciales con `curl`.
3. **Rigor por rama destino:**

   | `$BASE_BRANCH` | Rigor |
   |---|---|
   | `sport_kids` | Estándar. Bloquean 🔴 y 🟠 nuevas (🟠 admite justificación escrita en el PR). 🟡 = observación. |
   | otra rama de trabajo (PR apilado, `feature/*`) | Estándar, igual que `sport_kids`. |
   | `main` | **Detente y avisa:** `main` es la plantilla compartida; los cambios de SportKid no se integran ahí. Solo si el usuario confirma que es un cambio de plantilla, audita con rigor máximo (cualquier violación nueva, incluida 🟡, bloquea). |

### 0.2 Sincronización local

En modo PR, antes de hacer checkout confirma con `git status --porcelain` que el árbol esté limpio; si hay cambios del usuario, **no los pises**: ofrece `git stash` o un worktree (`git worktree add ../sportkid-review-$PR_ID "review/pr-$PR_ID"`).

```bash
git fetch origin "$BASE_BRANCH"
git fetch origin "$SOURCE_BRANCH" 2>/dev/null && git checkout -B "review/pr-$PR_ID" "origin/$SOURCE_BRANCH" \
  || { git fetch origin "pull/$PR_ID/head:review/pr-$PR_ID" && git checkout "review/pr-$PR_ID"; }
```

Al terminar, vuelve a la rama original del usuario.

Todos los bloques corren **bajo bash** desde la raíz del repo y cargan la librería compartida (si `origin/$BASE_BRANCH` no existe localmente, la librería usa la rama local; `BASE_REF=<commit>` o `STAGED=1` cambian el rango):

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
# BASE_REF, BASE_SNAPSHOT, pr_diff, PR_FILES, PR_NEW_FILES, PR_MAIN, PR_MAIN_NEW, PR_COMPONENTS (átomos Sk*),
# PR_CORE_UI, PR_THEME, PR_FEATURES, PR_APP, PR_SCREENS, PR_STATES, PR_MODELS, PR_VIEWMODELS, PR_DATA,
# PR_UNIT_TESTS, PR_UI_TESTS, PR_RES, added_lines, added_main, is_new_file, module_of, feature_of,
# count_code_lines, base_file, APP_MAIN, APP_RES, THEME_DIR, DS_COMPONENT_DIR, CORE_UI_DIR, CORE_MODEL_DIR,
# FEATURES, MAIN_RE, MAIN_KT, MAIN_SRC, RES_SPEC, TEST_KT, TICKET_RE, DEBT_RE, COMMIT_RE
EOF
```

### 0.3 Criterios del ticket (opcional, solo `$ROLE = TL`)

El ticket es opcional y es un issue de GitHub: ` (#<n>)` al **final** del asunto de los commits o del título del PR (el mismo sufijo que agrega GitHub al hacer squash merge), o `Closes #<n>` en la descripción. Si existe **y** `gh` está disponible, lee el issue (`gh issue view <n> --json title,body`; ver `tech-story-architect:jira-integracion`) y contrasta sus AC/DoD con el resultado de la Sección 5: ✅ cumple / ❌ no cumple (cita `archivo:línea` o ID de revisión) / ⚠️ no verificable desde el diff (p. ej. "coincide con Figma"). Sin ticket o sin `gh`: omite esta sección sin bloquear.

## Flujo de validación

```
0. $ROLE, modo (local/staged/PR) y $BASE_BRANCH (0.1) + sincronizar (0.2)
        ↓
1. Archivos del cambio (librería): main por módulo / átomos / core:ui / screens / holders / modelos / tests / res
        ↓
2. Revisiones sobre LÍNEAS AGREGADAS:
   2a. FF-CORE  núcleo: logging, !!, GlobalScope/runBlocking, "hoy" fuera de parámetro, imports entre
                módulos, secretos, TODO sin ticket, tests, framework nuevo, changelog, emojis, commits
                — Sección 5 (siempre)
   2b. FF-ARC   patrón de pantalla: datos + callbacks, Destination, holders <X>State, Samples, Gradle
                entre módulos, capas que aún no existen — `validate-pullrequest:ff-arc`
   2c. FF-ST    estructura y nombres: módulo/paquete, prefijo Sk, tamaño, código comentado, KDoc
                — `validate-pullrequest:ff-st` (siempre)
   2d. FF-CMP   composables: modifier, slots, estado en core, módulo correcto, previews @SkPreviews
                — `validate-pullrequest:ff-cmp` (si hay @Composable)
   2e. FF-THM   tokens: Color/dp/sp/TextStyle/formas fuera de theme/, colores de dominio
                — `validate-pullrequest:ff-thm` (siempre que haya PR_MAIN)
   2f. FF-A11Y  contentDescription, null decorativo, Role, área táctil, color nunca solo — `validate-pullrequest:ff-a11y`
   2g. FF-I18N  literales, concatenación, formatos README §7, claves, español, plurals — `validate-pullrequest:ff-i18n`
   2h. FF-RES   recursos de Figma: nombres, cabecera, duplicados, URLs, peso — `validate-pullrequest:ff-res`
   2i. FF-PF    recomposición: @Immutable, key, trabajo en composición — `validate-pullrequest:ff-pf`
   2j. FF-DUP   duplicación y reinvención de componentes/tokens/helpers — `validate-pullrequest:ff-dup` (siempre)
   2k. FF-DEP   catálogo de versiones, dependencias nuevas, Gradle — `validate-pullrequest:ff-dep`
   2l. FF-COV   tests de holders, lógica pura y flujos — `validate-pullrequest:ff-cov` (si hay PR_MAIN)
        ↓
3. Comparar contra la línea base (tabla de deuda TD-SK de CLAUDE.md / sportkid-governance:fitness-functions)
        ↓
4. Clasificar cada hallazgo NUEVO (🔴/🟠/🟡) con el rigor de $BASE_BRANCH → Formato Sección 7
        ↓
5. Veredicto: ✅ APROBADO | ⚠️ APROBADO CON OBSERVACIONES | ❌ BLOQUEADO (Sección 5.1)
        ↓
6. (Solo si corresponde) propagar a CLAUDE.md / README / catálogo → `sportkid-governance:propagacion`
        ↓
7. $ROLE = TL y modo PR con gh → publicar en GitHub (Sección 8); si no, reporte solo en el chat
```

## 1. Obligaciones de actualización de documentos

Si el PR cambia el estado de cumplimiento (resuelve o agrava una `TD-SK-0XX`, introduce una convención o decisión), sigue la propagación de `CLAUDE.md` (Mapa de documentación → Propagación de un cambio) con `sportkid-governance:propagacion`:

```
Cambio de token o componente → README §1–§3 → catálogo (generar.py)
Pantalla nueva o modificada  → README §4.2 → docs/ui-catalogo/pantallas.json → capturar.py + generar.py
Convención o deuda nueva     → CLAUDE.md → python3 .github/scripts/sync_claude_to_copilot.py (AGENTS.md y .github/)
Comando o agente de .claude/ → python3 .github/scripts/sync_claude_to_copilot.py (.github/prompts, .github/agents)
Cambio visible al usuario    → changelog.d/<fragmento>.md
```

Un PR que introduce una librería o capa nueva (Navigation, Hilt, Room, Retrofit, Firebase, ViewModel…) **debe** registrarla en `CLAUDE.md` (Convenciones → Dependencias) en el mismo PR. `AGENTS.md`, `.github/copilot-instructions.md`, `.github/prompts/`, `.github/agents/` y `docs/ui-catalogo/index.html` son generados: un PR que los edita a mano sin tocar su fuente es 🟡.

## 2. Reglas de SportKid (resumen operativo)

La fuente autoritativa es `CLAUDE.md` (y `README.md` para el sistema de diseño). Resumen para las revisiones:

- **ARC — módulos y pantalla (`CLAUDE.md` → Arquitectura, README §6.1):** `core:designsystem` no depende de `core:model`; `core:ui` depende de `designsystem` y `model`; `feature:*` depende de `core:ui` y **nunca** de otro feature; solo `:app` conoce a todos. Pantallas con datos + callbacks (`onClose`, `onSave: (Draft) -> Unit`), sin `NavController` ni ViewModel; `Destination` y `*Return` en `SportKidApp`; holders `@Stable <X>State` + `Saver` + `remember<X>State`; `object <X>Samples` en `<X>Models.kt`; dinero en pesos `Int` (nunca `Float`/`Double`); estados de dominio como `enum` de `core:model` con presentación única en `core:ui`.
- **"Hoy" (`CLAUDE.md` → Fechas):** `today: LocalDate = LocalDate.now()` solo como default de parámetro de la pantalla (o de `remember<X>State`), nunca dentro de modelos o lógica; previews con fecha fija.
- **CMP (`CLAUDE.md` → Componentes):** buscar antes de crear (`/catalogo-ui-sportkid consultar <pieza>`); átomos sin dominio `Sk*` en `core:designsystem`, piezas con dominio en `core:ui` sin prefijo, piezas de una pantalla `private` en el feature; KDoc de una línea en español; `modifier: Modifier = Modifier` primer opcional aplicado a la raíz; slots `@Composable` al final; parámetro nuevo al final con default; ≥ 1 preview `private` con `@SkPreviews`; composables > 60 líneas se dividen en secciones `private`.
- **THM (`CLAUDE.md` → Tokens, README §1):** color de `MaterialTheme.colorScheme` o `LocalSportColors` (deporte, medallas, estados, hijos, cintas: no cambian con el tema); espaciado de `SportKidTheme.spacing`; texto de `MaterialTheme.typography` (+ `metric`, `brand`); formas de `MaterialTheme.shapes`. Nunca `Color(0x…)` fuera de `core/designsystem/.../theme/`. Token nuevo = nombre por rol. Si Figma y el README no coinciden, manda Figma.
- **A11Y:** `contentDescription` desde recursos en iconos con significado; `null` solo si es decorativo; selección/toggle sobre la fila completa con `Role`; objetivos ≥ 48dp (`SkDimens.minTouchTarget`); texto que soporta `fontScale` 1.3; color nunca solo.
- **I18N (README §5 y §7):** `stringResource` siempre, en el `strings.xml` del módulo dueño; claves `<pantalla>_<descripcion>`; bloques con `<!-- P05 Inicio -->`; español de México; `plurals`; sin concatenar; dinero `$1,250 MXN`, natación `00:41.23`, atletismo `28:41`/`1:02:15`, fechas "Sáb 14 nov" vía `DateLabels` (`Locale` es-MX); vocabulario único de estados.
- **RES (`CLAUDE.md` → Recursos de Figma):** SVG → VectorDrawable 1:1 en el módulo que lo usa, `ic_<feature>_<nombre>.xml`; fotos `img_<pantalla>_<nombre>` en `drawable-nodpi`; reutilizar de `core` si ya existe; un icono por deporte vía `Sport.iconRes`; nunca URLs de Figma en el código.
- **Prohibido en `src/main`:** `!!`, `GlobalScope`, `runBlocking`, `android.util.Log`, `println`, código comentado, emojis en la UI.
- **Tamaño:** archivos nuevos ≤ 400 líneas (TD-SK-005); composables ≤ 60 líneas.
- **DEP:** solo vía `gradle/libs.versions.toml`; librería nueva discutida antes y registrada en `CLAUDE.md`; sin secretos.
- **GIT:** asunto `<tipo>(<ámbito>): <descripción imperativa en minúscula>` con tipo `feat|fix|refactor|test|docs|chore|build|revert`, ámbito = módulo en minúscula opcional (`home`, `agenda`, `tkd`, `designsystem`, `ui`, `model`, `app`, `catalogo`, `docs`…) y ticket opcional ` (#<n>)` **al final** (hook `.githooks/commit-msg`); PR contra `sport_kids` con `PULL_REQUEST_TEMPLATE.md`; sin `build/`, `.idea/`, `.DS_Store`; fragmento `changelog.d/<id|+slug>.<tipo>.md` si cambia algún `src/main`.
- **Análisis estático:** `./gradlew lint` sin errores nuevos. Detekt (`config/detekt/detekt.yml`) aún no está aplicado en Gradle (TD-SK-008): mientras tanto sus reglas (LongMethod 60, MaxLineLength 140, `FIXME:`/`STOPSHIP:` prohibidos, WildcardImport) se revisan aquí a mano.

## 3. Estándar de pruebas

Hoy solo existen `ExampleUnitTest`/`ExampleInstrumentedTest` de la plantilla en `:app` (TD-SK-001) y los módulos `core:*`/`feature:*` no declaran dependencias de test. Cuando un PR agregue lógica, este es el estándar (detalle y severidades en `validate-pullrequest:ff-cov`):

| Qué cambia el PR | Test esperado | Dónde |
|---|---|---|
| Lógica pura (`DateLabels`, formatos, funciones de `<X>Models.kt`, propiedades derivadas de un holder) | Unit test JVM | `<módulo>/src/test/kotlin/com/invex/sportkid/<módulo>/…Test.kt` (agrega `testImplementation(libs.junit)` al módulo) |
| Holder `<X>State` con validación o `Saver` | Unit test del holder (estado inicial, cada evento, ida y vuelta del `Saver`) | idem |
| Flujo interactivo de una pantalla (selección, máscara de tiempo, agregar/quitar pruebas) | Compose UI Test con `createComposeRule` sobre la pantalla con `Samples` y "hoy" fijo | `<módulo>/src/androidTest/kotlin/…` o `app/src/androidTest/java/com/invex/one/template/…` |
| Átomo con comportamiento (`SkMoneyField`, `SkTextField` + máscara, `SkSegmentedControl`) | Compose UI Test del componente; screenshots con Roborazzi cuando se adopte (README §6.3) | `core/designsystem/src/androidTest/…` |

```kotlin
class RaceFormStateTest {
    private val today = LocalDate.of(2026, 11, 14)   // "hoy" fijo, nunca LocalDate.now()

    @Test
    fun canSave_whenNameVenueAndTimeFilled_isTrue() {
        val state = RaceFormState(RaceFormState.empty(today))
        state.name = "Carrera del Bosque"
        state.venue = "Chapultepec"
        state.timeDigits = "1404"
        assertTrue(state.canSave)
        assertEquals(1000, state.toDraft().distance)
    }
}
```

> `RaceFormState` vive en `feature/athletics/.../RaceFormScreen.kt`; verifica los nombres reales del holder antes de citar un ejemplo en un hallazgo.

## 4. Línea base

Las cifras vivas de deuda (`TD-SK-0XX`) están en `CLAUDE.md` → Deuda técnica; los comandos para recalcularlas, en `sportkid-governance:fitness-functions`. **No copies cifras en el reporte desde la memoria:** recalcula la línea base en la pasada (a) de cada revisión.

## 5. Revisiones núcleo (FF-CORE) — acotadas al diff

Cada revisión corre (a) sobre todo el código de `src/main` para conocer la línea base y (b) sobre las **líneas agregadas**. Solo (b) decide el veredicto.

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
echo "Base: $([ "${STAGED:-0}" = 1 ] && echo 'índice (git diff --cached)' || echo "$BASE_REF") · árbol previo: $BASE_SNAPSHOT"; echo "Archivos .kt del cambio:"; printf '%s\n' "$PR_FILES"
ADDED=$(added_main)

# FF-CORE-01 🔴 logging directo — base esperada 0
printf '%s\n' "$ADDED" | grep -E ':import android\.util\.Log|[^.A-Za-z]Log\.(v|d|i|w|e|wtf)\(|\bprintln\(|\.printStackTrace\(\)|System\.(out|err)\.' \
  | sed 's/^/🔴 FF-CORE-01 /'

# FF-CORE-02 🔴 !!, GlobalScope, runBlocking — base esperada 0
printf '%s\n' "$ADDED" | awk '{c=$0; sub(/^[^:]*:[^:]*:/,"",c); sub(/\/\/.*/,"",c); if (c ~ /[A-Za-z0-9_)\]]!!/ || c ~ /GlobalScope|runBlocking/) print "🔴 FF-CORE-02 " $0}'

# FF-CORE-03 🟠/🟡 "hoy" fuera de parámetro (CLAUDE.md → Fechas)
NOW='(LocalDate|LocalDateTime|LocalTime|YearMonth|Instant)\.now\(|System\.currentTimeMillis\(|Calendar\.getInstance\('
DEFAULT_PARAM='[a-zA-Z]+: *(LocalDate|LocalDateTime|LocalTime|YearMonth) *= *(LocalDate|LocalDateTime|LocalTime|YearMonth)\.now\(\)'
printf '%s\n' "$ADDED" | grep -E "$NOW" | while IFS= read -r l; do
  f=${l%%:*}
  if echo "$l" | grep -qE "$DEFAULT_PARAM"; then
    case "$f" in *Screen.kt|*State.kt|*Sheet.kt) ;;   # default de la pantalla o de remember<X>State: patrón vigente
      *) echo "🟡 FF-CORE-03 'hoy' como default fuera de la pantalla — que entre desde el Screen: $l" ;; esac
  else
    echo "🟠 FF-CORE-03 reloj del sistema en lógica/cuerpo — recibe 'today' por parámetro: $l"
  fi
done

# FF-CORE-04 🔴 imports que rompen las reglas de dependencia entre módulos
printf '%s\n' "$ADDED" | grep -E ':import com\.invex\.sportkid\.(feature|core)\.' | while IFS= read -r l; do
  f=${l%%:*}; imp=$(echo "$l" | sed -E 's/.*import com\.invex\.sportkid\.//')
  case "$f" in
    feature/*) own=$(feature_of "$f")
      case "$imp" in feature.*) dep=$(echo "$imp" | cut -d. -f2)
        [ "$dep" != "$own" ] && echo "🔴 FF-CORE-04 feature:$own importa feature:$dep $l" ;; esac ;;
    core/designsystem/*) case "$imp" in core.designsystem.*) ;; *) echo "🔴 FF-CORE-04 core:designsystem no conoce el dominio $l" ;; esac ;;
    core/model/*)        case "$imp" in core.model.*) ;; *) echo "🔴 FF-CORE-04 core:model es Kotlin puro $l" ;; esac ;;
    core/*)              case "$imp" in feature.*) echo "🔴 FF-CORE-04 core no importa features $l" ;; esac ;;
  esac
done

# FF-CORE-05 🔴 secretos o archivos locales versionados
pr_diff --name-only --diff-filter=A \
  | grep -E '(^|/)(local\.properties|google-services\.json|.*\.(jks|keystore)|\.DS_Store)$|(^|/)(build|\.idea|\.gradle|\.kotlin)/' \
  | sed 's/^/🔴 FF-CORE-05 archivo que no debe versionarse: /'
added_lines '*' | grep -iE '(api[_-]?key|secret|password|token)[[:space:]]*[:=][[:space:]]*"[A-Za-z0-9_\-]{12,}"' \
  | sed 's/^/🔴 FF-CORE-05 posible secreto: /'

# FF-CORE-06 🟡 TODO/FIXME sin issue (#<n>) ni deuda (TD-SK-0XX); FIXME:/STOPSHIP: prohibidos (detekt.yml)
added_lines '*.kt' | grep -E '(TODO|FIXME)' | grep -vE "$TICKET_RE|$DEBT_RE" \
  | sed 's/^/🟡 FF-CORE-06 TODO sin (#<n>) ni TD-SK-0XX (los de datos reales son TD-SK-002): /'
added_lines '*.kt' | grep -E 'FIXME:|STOPSHIP:' | sed 's/^/🟠 FF-CORE-06 FIXME:\/STOPSHIP: — abre una deuda TD-SK-0XX /'

# FF-CORE-07 🟠 anti-patrones en tests
added_lines "${TEST_KT[@]}" \
  | grep -E 'Thread\.sleep\(|runBlocking|(LocalDate|LocalDateTime|YearMonth)\.now\(|System\.currentTimeMillis\(' \
  | sed 's/^/🟠 FF-CORE-07 /'

# FF-CORE-08 🟠 librería o capa nueva sin decisión registrada en CLAUDE.md
printf '%s\n' "$ADDED" | grep -E ':import (dagger\.hilt|javax\.inject|org\.koin|androidx\.navigation|androidx\.room|androidx\.datastore|androidx\.lifecycle\.(ViewModel|viewmodel)|retrofit2|okhttp3|io\.ktor|kotlinx\.serialization|com\.squareup\.moshi|com\.google\.gson|com\.google\.firebase|coil3?)\b' \
  | sed 's/^/🟠 FF-CORE-08 librería\/capa nueva — ¿registrada en CLAUDE.md (Dependencias) en este PR? /'

# FF-CORE-09 🟡/🟠 fragmento de changelog (changelog.d/template/TOWNCRIER_README.md)
FRAGS=$(pr_diff --name-only --diff-filter=A -- 'changelog.d/*' | grep -v '^changelog.d/template/')
if pr_diff --name-only -- "${MAIN_SRC[@]}" | grep -q . && [ -z "$FRAGS" ]; then
  echo "🟡 FF-CORE-09 el PR cambia src/main y no agrega fragmento en changelog.d/"
fi
printf '%s\n' "$FRAGS" | grep . | grep -vE "^changelog\.d/([0-9]+|$DEBT_RE|\+[a-z0-9-]+)\.(added|changed|refactored|deprecated|removed|fixed|security)\.md$" \
  | sed 's/^/🟠 FF-CORE-09 nombre de fragmento inválido (<n>|TD-SK-0XX|+slug).<tipo>.md: /'
pr_diff --name-only -- CHANGELOG.md | grep -q . && echo "🟡 FF-CORE-09 CHANGELOG.md editado a mano (lo genera towncrier)"

# FF-CORE-10 análisis estático — Detekt aún no aplicado (TD-SK-008)
if grep -qs 'detekt' build.gradle.kts gradle/libs.versions.toml; then
  ./gradlew -q detekt >/dev/null 2>&1 || echo "🔴 FF-CORE-10 ./gradlew detekt falla — revisa build/reports/detekt/"
else
  echo "ℹ️ FF-CORE-10 Detekt no está aplicado en Gradle (TD-SK-008): corre ./gradlew lint y revisa a mano las reglas de config/detekt/detekt.yml"
fi

# FF-CORE-11 🟠 emojis en la UI (código o strings.xml)
added_lines "${MAIN_KT[@]}" '*/src/main/res/values*/strings.xml' \
  | perl -CSD -ne 'print if /[\x{1F000}-\x{1FAFF}\x{2600}-\x{27BF}\x{2B50}\x{2B55}]/' \
  | sed 's/^/🟠 FF-CORE-11 emoji en la UI (usa un icono vectorial): /'

# FF-CORE-12 🟡 asunto de commit fuera de convención (.githooks/commit-msg)
if [ "${STAGED:-0}" != 1 ]; then
  git log --no-merges --format='%h %s' "$BASE_REF..HEAD" | while read -r h s; do
    echo "$s" | grep -q '\[skip-validation\]' && continue
    echo "$s" | grep -qE "$COMMIT_RE" || echo "🟡 FF-CORE-12 $h '$s' — <tipo>(<ámbito>): <descripción> (#<n>)"
    echo "$s" | grep -qE "$TICKET_RE" && ! echo "$s" | grep -qE " \\($TICKET_RE\\)$" \
      && echo "🟡 FF-CORE-12 $h '$s' — el issue va al final como ' (#<n>)'"
  done
fi

# Conteo de @Test — no debe reducirse
echo "@Test agregados: $(pr_diff -- "${TEST_KT[@]}" | grep -cE '^\+[[:space:]]*@Test')"
echo "@Test eliminados: $(pr_diff -- "${TEST_KT[@]}" | grep -cE '^-[[:space:]]*@Test')"
true
EOF
```

## 5.2 a 5.12 — Familias de revisión (carga condicional vía Skill)

Cada familia vive en su propio skill (`validate-pullrequest:ff-*`, en `.claude/commands/validate-pullrequest/`). Detecta cuáles aplican y usa el **Skill tool** solo para esas:

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
has() { [ -n "$1" ] && echo true || echo false; }
GRADLE_MODULES=$(pr_diff --name-only -- 'core/*/build.gradle.kts' 'feature/*/build.gradle.kts' settings.gradle.kts)
FF_ARC=$(has "$PR_SCREENS$PR_STATES$PR_MODELS$PR_APP$PR_VIEWMODELS$PR_DATA$GRADLE_MODULES")
FF_CMP=$(has "$(printf '%s\n' $PR_MAIN | grep . | xargs grep -l '@Composable' 2>/dev/null)")
FF_A11Y=$(has "$(added_main | grep -E 'Icon\(|Image\(|clickable|toggleable|selectable|IconButton|semantics|contentDescription')")
FF_I18N=$(has "$( (added_main | grep -E 'Text\(|text *=|contentDescription|stringResource|format\(|Locale|DateTimeFormatter'; pr_diff --name-only -- '*/src/main/res/values*/strings.xml') )")
FF_RES=$(has "$(pr_diff --name-only -- '*/src/main/res/drawable*' '*/src/main/res/font*' '*/src/main/res/mipmap*')")
FF_PF=$(has "$(added_main | grep -E 'Lazy(Column|Row|VerticalGrid)|items\(|data class|@Immutable|@Stable|remember|Formatter|Regex\(')")
FF_DEP=$(has "$(pr_diff --name-only -- gradle/libs.versions.toml '*.gradle.kts' gradle.properties gradle/wrapper)")
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
| FF-ARC (5.2) | `validate-pullrequest:ff-arc` | `$FF_ARC` — pantallas, holders `<X>State`, modelos, `:app`, `build.gradle.kts` de módulos, ViewModel/datos |
| FF-ST (5.3) | `validate-pullrequest:ff-st` | ✅ siempre |
| FF-CMP (5.4) | `validate-pullrequest:ff-cmp` | `$FF_CMP` — archivos con `@Composable` |
| FF-THM (5.5) | `validate-pullrequest:ff-thm` | ✅ siempre que haya `PR_MAIN` |
| FF-A11Y (5.6) | `validate-pullrequest:ff-a11y` | `$FF_A11Y` |
| FF-I18N (5.7) | `validate-pullrequest:ff-i18n` | `$FF_I18N` |
| FF-RES (5.8) | `validate-pullrequest:ff-res` | `$FF_RES` — drawables, fuentes o mipmaps de cualquier módulo |
| FF-PF (5.9) | `validate-pullrequest:ff-pf` | `$FF_PF` |
| FF-DUP (5.10) | `validate-pullrequest:ff-dup` | ✅ siempre |
| FF-DEP (5.11) | `validate-pullrequest:ff-dep` | `$FF_DEP` — catálogo, scripts Gradle o wrapper |
| FF-COV (5.12) | `validate-pullrequest:ff-cov` | ✅ siempre que haya `PR_MAIN` |

Si un flag condicional es `false`, omite esa familia en el reporte sin mencionarla.

> **Heurísticas vs certezas.** Varias revisiones (estado en un componente, `null` decorativo, área táctil, duplicación, medidas literales) son **señales** 🔎: confirma leyendo el código antes de clasificarlas. Si queda en duda, va como ⚠️ pregunta al autor.

## 5.1 Criterios de aprobación / bloqueo

Línea base para `sport_kids`. En `main` (solo cambios de plantilla confirmados) las filas ⬆️ se endurecen un nivel.

| Condición en las líneas del PR | Severidad | `sport_kids` | `main` |
|---|---|---|---|
| Logging directo (FF-CORE-01), `!!`/`GlobalScope`/`runBlocking` (FF-CORE-02) | 🔴 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Import o dependencia Gradle que rompe las reglas entre módulos (FF-CORE-04, FF-ARC-02) | 🔴 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Secreto o archivo local/de build versionado (FF-CORE-05) | 🔴 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| ViewModel/`NavController` en la firma de un composable (FF-CMP-11) | 🔴 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Dinero en `Float`/`Double` (FF-ARC-07) | 🔴 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Detekt con hallazgos nuevos, una vez aplicado (FF-CORE-10) | 🔴 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Violaciones 🔴 de las familias (según su skill; p. ej. `IconButton` con `contentDescription = null` FF-A11Y-02, `var`/`MutableList` en un holder FF-PF-03, grafo de módulos roto FF-DEP-06) | 🔴 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Librería/capa nueva (ViewModel, Navigation, Hilt, Room, red…) sin decisión en `CLAUDE.md` (FF-CORE-08, FF-ARC-01/11) | 🟠 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Reloj del sistema en lógica o cuerpo de composable (FF-CORE-03) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| Navegación/efecto de plataforma en un feature (FF-ARC-04) o holder sin `Saver` (FF-ARC-05) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| Composable público sin preview `@SkPreviews` (FF-CMP-09) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| Componente sin `modifier` o mal ubicado; átomo con dominio (FF-CMP-01/02/04) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| Estado de negocio dentro de `core:designsystem`/`core:ui` — confirmado (FF-CMP-03) | 🟠 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| `Color(0x…)` en archivo nuevo, espaciado/`sp`/`TextStyle` literal fuera de `theme/` en `feature/`/`core:ui`/`:app` (FF-THM-01/02/03) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| Archivo nuevo > 400 líneas o fuera de su módulo/paquete (FF-ST-01/03) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| Emoji en la UI (FF-CORE-11), `FIXME:`/`STOPSHIP:` (FF-CORE-06) | 🟠 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Anti-patrones en tests (FF-CORE-07) o reducción de `@Test` sin reemplazo | 🟠 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| Fragmento de changelog con nombre inválido (FF-CORE-09) | 🟠 | ❌ BLOQUEADO | ❌ BLOQUEADO |
| 🟠 de A11Y, I18N, RES, PF, DUP, DEP y COV (según su skill; p. ej. texto literal FF-I18N-01, clave de string repetida en otro módulo FF-I18N-04, vector sin cabecera o URL de Figma FF-RES-02/04, drawable con el mismo nombre en otro módulo FF-RES-07, `items()` sin `key` FF-PF-01, reinvención FF-DUP-01..03, versión fuera del catálogo FF-DEP-01, librería o `include(` nuevo sin `CLAUDE.md` FF-DEP-03/06, menos `@Test` FF-COV-05) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| AC/DoD ❌ del issue (0.3, solo TL) | 🟠 | ❌ BLOQUEADO salvo justificación | ❌ BLOQUEADO ⬆️ |
| `@Composable` > 60 líneas, archivo existente > 400 que crece > 10 % (FF-ST-03), literales `N.dp` que crecen (FF-THM-08) | 🟡 | ⚠️ OBSERVACIONES | ❌ ⬆️ |
| `Color(0x…)`/`Color.X` en archivo tocado, token por valor, forma literal (FF-THM-01/04/07) | 🟡 | ⚠️ OBSERVACIONES | ❌ ⬆️ |
| Nombres fuera de convención, código comentado, sin KDoc, import comodín (FF-ST-02/05/06/07) | 🟡 | ⚠️ OBSERVACIONES | ❌ ⬆️ |
| Pantalla nueva sin conectar a `SportKidApp` (FF-ARC-03), estado de dominio fuera de lugar (FF-ARC-09) | 🟡 | ⚠️ OBSERVACIONES | ❌ ⬆️ |
| Preview pública o sin `@SkPreviews`, sección pública en feature, firma incompatible, slots antes de parámetros, tipo experimental (FF-CMP-08/10/12/13/14/15) | 🟡 | ⚠️ OBSERVACIONES | ❌ ⬆️ |
| 🟡 de A11Y, I18N, RES, PF, DUP y COV (según su skill; p. ej. scroll sin `derivedStateOf` FF-PF-04, componente de `core` sin fila en el README o pantalla fuera de `pantallas.json` FF-DUP-06) | 🟡 | ⚠️ OBSERVACIONES | ❌ ⬆️ |
| TODO sin issue/deuda (FF-CORE-06), "hoy" como default fuera de la pantalla (FF-CORE-03), commit fuera de convención (FF-CORE-12) | 🟡 | ⚠️ OBSERVACIONES | ❌ ⬆️ |
| PR con cambios en `src/main` sin fragmento de changelog (FF-CORE-09); documentos de la Sección 1 sin propagar | 🟡 | ⚠️ OBSERVACIONES | ❌ ⬆️ |
| Violación preexistente que el PR no agrava | ℹ️ | ✅ No bloquea | ✅ No bloquea |
| Todo en verde y ninguna cifra de deuda empeora | — | ✅ APROBADO | ✅ APROBADO |

## 7. Formato de reporte de violaciones

Cada incumplimiento **nuevo o agravado**:

1. Encabezado: 🔴 `Violaciones Críticas` · 🟠 `Violaciones Altas` · 🟡 `Violaciones Medias`.
2. `V-XX — <descripción de una línea>`.
3. `Archivos:` con `Archivo.kt:línea-línea` (ruta desde el módulo, p. ej. `feature/home/.../HomeScreen.kt:94-96`).
4. Bloque `// ❌ INCORRECTO — <motivo>` con el código real.
5. Bloque `// ✅ CORRECTO — <patrón>` citando la regla (`CLAUDE.md` → <sección> o `README.md` §x).

```
🔴 Violaciones Críticas

V-01 — Un feature importa otro feature

Archivos: feature/home/src/main/kotlin/com/invex/sportkid/feature/home/HomeScreen.kt:12

// ❌ INCORRECTO — feature:home queda acoplado a feature:agenda (y su build.gradle.kts ya no compila sin él)
import com.invex.sportkid.feature.agenda.AgendaEvent

// ✅ CORRECTO — CLAUDE.md → Reglas de dependencia: la pieza compartida sube a core:ui
//    (o :app arma el dato y lo pasa por parámetro, como SportMapping.kt)
import com.invex.sportkid.core.ui.EventCard
```

Agrupa 🔴 → 🟠 → 🟡 y numera `V-XX` de forma continua. La deuda heredada va aparte.

## 6. Formato de salida

1. **Veredicto** en negrita en la primera línea (`✅ APROBADO` / `⚠️ APROBADO CON OBSERVACIONES` / `❌ BLOQUEADO`), con la base ("Validado contra `origin/sport_kids`"), el modo y el rol ("— DEV, no publicado").
2. **Alcance** — archivos evaluados por módulo (main / tests / res / build) y familias que corrieron.
3. **Violaciones nuevas** — Formato Sección 7, o "Sin violaciones nuevas".
4. **Deuda heredada tocada** (informativa, con su `TD-SK-0XX`).
5. **Por familia** (solo las que corrieron): arquitectura (FF-ARC), composables y previews (FF-CMP + salida de `validate-pullrequest-api.py`), tokens (FF-THM), accesibilidad (FF-A11Y), textos (FF-I18N), recursos de Figma (FF-RES), recomposición (FF-PF), duplicación (FF-DUP, obligatoria: "Sin duplicación detectada" si no hay), dependencias (FF-DEP), tests (FF-COV: por holder/pantalla, archivo de test y nº de casos).
6. **Criterios del issue** (solo TL con ticket).
7. **Delta de deuda** — antes/después de cada cifra de `CLAUDE.md` que el PR movió (TD-SK-004 `N.dp`, TD-SK-005 archivos > 400, TD-SK-006 `contentDescription = null`…); conteo de `@Test`.
8. **Comandos de Gradle ejecutados** y su resultado. Nunca afirmes que los tests instrumentados pasan si no los corriste (requieren emulador).
9. **Documentos a actualizar** (solo si cambia el cumplimiento o hay pantalla nueva; Sección 1).

### Comandos de build de referencia

```bash
./gradlew :app:assembleDebug                 # compila todos los módulos
./gradlew :app:testDebugUnitTest             # unit tests JVM (hoy solo ExampleUnitTest, TD-SK-001)
./gradlew lint                               # Android Lint de todos los módulos (reportes en <módulo>/build/reports/)
./gradlew :app:connectedDebugAndroidTest     # Compose UI tests (requiere emulador: ANDROID_SERIAL=emulator-5554)
./gradlew :app:installDebug                  # instala para comparar contra Figma en el emulador
adb shell am start -S -n com.invex.one.template/.MainActivity --es sk_screen <Destination>   # abrir una pantalla (debug)
```

## 8. Publicación del veredicto en GitHub — solo `$ROLE = TL` y modo PR con `gh`

**Guardia:** con `$ROLE = DEV`, en modo local o sin `gh` autenticado, entrega el reporte en el chat y cierra con: *"Auditoría completa — el veredicto no se publicó en GitHub."*

Con `$ROLE = TL`: el chat recibe el reporte completo; **GitHub recibe solo lo accionable** (🔴/🟠/🟡 reales y AC ❌/⚠️). Las familias en verde no se mencionan. Escribe el cuerpo en un archivo del scratchpad y publícalo con `--body-file`:

- **❌ BLOQUEADO:** `gh pr review "$PR_ID" --request-changes --body-file <reporte.md>` con: veredicto, violaciones (Formato 7), `## Qué se necesita para aprobar` con `- [ ]` por violación bloqueante, y pie *"Resultado de la validación automatizada `/validate-pullrequest` (SportKid)."* Para hallazgos puntuales puedes agregar `gh pr comment "$PR_ID" --body-file …`.
- **✅ / ⚠️ APROBADO:** `gh pr review "$PR_ID" --approve --body-file <reporte.md>`, breve si no hay pendientes, o con la lista de 🟡 con `archivo:línea` y corrección sugerida.
- GitHub no permite aprobar ni pedir cambios en un PR propio: si `gh` lo rechaza por eso, publica el mismo cuerpo con `gh pr review "$PR_ID" --comment --body-file …` y dilo en el chat.
- Reglas: nunca publicar con `DEV`; si `gh` falla, reporta el error tal cual sin reintentar en silencio; si el veredicto cambia en la misma sesión, deja claro cuál comentario es el vigente; redacta como resultado automatizado, no como revisor humano.
