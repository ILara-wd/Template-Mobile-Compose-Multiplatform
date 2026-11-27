<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde .claude/commands/validate-pullrequest/ff-cov.md. NO EDITAR A MANO. -->
---
mode: agent
description: 5.12 Revisiones de Tests (FF-COV-XX)
---

## 5.12 Revisiones de Tests (FF-COV-XX)

SportKid todavía no tiene tests propios: solo los `ExampleUnitTest`/`ExampleInstrumentedTest` de la plantilla en `:app` (**TD-SK-001**, `CLAUDE.md` §Deuda técnica; README §6.3 describe la meta: Compose UI Test y screenshots con Roborazzi). Sin ViewModels ni capa de datos, **las unidades probables son los holders de estado `@Stable <X>State`** (`AgendaState`, `NewTrainingState`, `CreateAthleteState`…) y la lógica pura (formatos como `SwimTime`/`RaceTime`, `DateLabels.daysFrom`/`ageOn`, `Belt.next`, filtros como `AgendaState.matches`). La regla es **"TD-SK-001 solo se reduce"**: el código **nuevo** testeable llega con su test; el código existente que el PR toca se señala como observación. No hay Jacoco: la regla es por existencia y contenido de los tests, no por porcentaje.

| ID | Regla | Severidad |
|---|---|---|
| FF-COV-01 | Holder `<X>State` (en `<X>State.kt` o junto a su pantalla, como `RaceFormState`) nuevo, o con métodos públicos nuevos, sin unit test JVM en `<módulo>/src/test` que cubra el estado inicial, cada método y el `Saver` (ida y vuelta) | 🟠 nuevo · 🟡 existente |
| FF-COV-02 | Lógica pura nueva (función o propiedad calculada no `@Composable` en `core:model`, `core:ui` o `<X>Models.kt`, fuera de `object <X>Samples`) sin unit test | 🟠 nuevo · 🟡 existente |
| FF-COV-03 | Pantalla nueva con interacción (callbacks `on…`, formulario, selección) sin Compose UI Test que la monte | 🟡 |
| FF-COV-04 | Test agregado sin aserciones, test de ejemplo de la plantilla extendido en lugar de reemplazado, `Thread.sleep`, o fecha real (`LocalDate.now()`) en lugar de un `today` fijo | 🟡 |
| FF-COV-05 | Número de `@Test` que baja o archivo de test borrado sin reemplazo (TD-SK-001 solo se reduce) | 🟠 |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
UNIT_DIRS=$(ls -d app/src/test core/*/src/test feature/*/src/test 2>/dev/null)
UI_DIRS=$(ls -d app/src/androidTest core/*/src/androidTest feature/*/src/androidTest 2>/dev/null)
in_tests() { [ -n "$2" ] && grep -rqE "$1" $2 2>/dev/null; }    # in_tests <regex> "<dirs>"
sev() { is_new_file "$1" && echo 🟠 || echo 🟡; }
test_dir_of() { echo "${1%%/src/main/*}/src/test/kotlin/$(dirname "${1#*/src/main/kotlin/}")"; }

# FF-COV-05 — conteo de @Test (TD-SK-001 solo se reduce)
grep_after() { if [ "${STAGED:-0}" = 1 ]; then git grep --cached -hE "$1" -- "${@:2}"; else git grep -hE "$1" HEAD -- "${@:2}"; fi; }
before=$(git grep -hE '^[[:space:]]*@Test' "$BASE_SNAPSHOT" -- "${TEST_KT[@]}" 2>/dev/null | wc -l | tr -d ' ')
after=$(grep_after '^[[:space:]]*@Test' "${TEST_KT[@]}" | wc -l | tr -d ' ')
echo "ℹ️ TD-SK-001 @Test: $before → $after"
[ "$after" -lt "$before" ] && echo "🟠 FF-COV-05 el PR reduce el número de @Test ($before → $after)"
pr_diff --name-only --diff-filter=D -- "${TEST_KT[@]}" | sed 's/^/🟠 FF-COV-05 test borrado (¿reemplazado?): /'

[ -z "$PR_MAIN" ] && { echo "ℹ️ FF-COV: sin código productivo en el cambio"; exit 0; }

# FF-COV-01 — holders de estado (<X>State, en <X>State.kt o junto a su pantalla)
for f in $(printf '%s\n' $PR_MAIN | grep '^feature/'); do
  for holder in $(grep -oE '^(internal )?class [A-Z][A-Za-z0-9]*State\b' "$f" | awk '{print $NF}'); do
    start=$(grep -nE "^(internal )?class $holder\b" "$f" | head -1 | cut -d: -f1)
    end=$(awk -v s="$start" 'NR > s && /^}/ { print NR; exit }' "$f")
    lines=$(added_lines "$f" | cut -d: -f2)
    new_decl=$(printf '%s\n' $lines | grep -qx "$start" && echo 1)
    methods=$(added_lines "$f" | awk -F: -v s="$start" -v e="$end" '$2 > s && $2 < e' \
              | grep -oE ':[[:space:]]+fun [a-z][A-Za-z0-9]*\(' | sed -E 's/.*fun ([A-Za-z0-9]+)\(/\1/' | sort -u)
    [ -z "$new_decl$methods" ] && continue
    s=🟡; { is_new_file "$f" || [ -n "$new_decl" ]; } && s=🟠
    t=$(grep -rlE "\b$holder\(" $UNIT_DIRS 2>/dev/null | head -1)
    if [ -z "$t" ]; then
      echo "$s FF-COV-01 $holder ($f) — sin unit test (esperado $(test_dir_of "$f")/${holder}Test.kt)"; continue
    fi
    missing=$(for m in $methods; do grep -q "\.$m(" "$t" || echo "$m"; done | tr '\n' ' ')
    echo "ℹ️ FF-COV-01 $holder → $t ($(grep -c '@Test' "$t") @Test)"
    [ -n "$missing" ] && echo "🟡 FF-COV-01 $holder — métodos nuevos sin test: $missing"
    sed -n "${start},${end}p" "$f" | grep -q 'Saver' && ! grep -q 'Saver' "$t" && echo "🟡 FF-COV-01 $holder — el Saver no tiene test de ida y vuelta"
  done
done

# FF-COV-02 — lógica pura nueva (funciones/propiedades no @Composable, fuera de Samples y de los holders)
for f in $(printf '%s\n' $PR_MAIN | grep -E '^core/(model|ui)/|Models\.kt$'); do
  added_lines "$f" | python3 -c '
import re, sys
f = sys.argv[1]; text = open(f).read().split("\n")
DECL = re.compile(r"^(?:(?:private|internal|public)\s+)?(?:fun|object|val|var|class|data class|enum class|sealed class|interface)\b")
def scope(n):
    i = n - 1
    while i >= 0 and not DECL.match(text[i]): i -= 1
    return text[i] if i >= 0 else ""
for l in sys.stdin:
    _, n, code = l.rstrip("\n").split(":", 2); n = int(n)
    m = re.match(r"\s*(?:internal\s+|public\s+)?(?:fun\s+(?:[A-Z]\w*(?:<[^>]*>)?\.)?([a-z]\w*)\s*\(|val\s+(?:[A-Z]\w*\.)?([a-z]\w*)\s*:[^=]*\bget\(\))", code)
    if not m: continue
    if re.search(r"object\s+\w*Samples\b|class\s+\w*State\b", scope(n)): continue
    above = " ".join(text[max(0, n - 4):n - 1])
    if "@Composable" in above or "@Preview" in above or "@SkPreviews" in above: continue
    print(m.group(1) or m.group(2))
' "$f" | sort -u | while read -r name; do
    case "$name" in remember*) continue ;; esac
    in_tests "\\b$name\\b" "$UNIT_DIRS" || echo "$(sev "$f") FF-COV-02 $f — \`$name\` sin unit test"
  done
done

# FF-COV-03 — pantallas nuevas con interacción
for f in $(printf '%s\n' $PR_MAIN_NEW | grep -E '^feature/.*Screen\.kt$'); do
  scr=$(grep -oE '^fun [A-Z][A-Za-z0-9]*Screen\(' "$f" | sed -E 's/fun ([A-Za-z0-9]+)\(/\1/' | head -1)
  [ -z "$scr" ] && continue
  grep -qE 'on[A-Z][A-Za-z]*: *\(' "$f" || continue
  in_tests "\\b$scr\\(" "$UI_DIRS" || echo "🟡 FF-COV-03 $scr ($f) — sin Compose UI Test (app/src/androidTest o ${f%%/src/main/*}/src/androidTest)"
done

# FF-COV-04 — calidad de los tests agregados o modificados
for t in $PR_UNIT_TESTS $PR_UI_TESTS; do
  case "$(basename "$t")" in Example*) echo "🟡 FF-COV-04 $t — test de ejemplo de la plantilla: reemplazar por tests reales"; continue ;; esac
  grep -qE 'assert|onNode.*\.assert|onNodeWith[A-Za-z]*\(.*\)\.assert' "$t" || echo "🟡 FF-COV-04 $t — sin aserciones"
  added_lines "$t" | grep -E 'Thread\.sleep\(|LocalDate(Time)?\.now\(|System\.currentTimeMillis\(' | sed 's/^/🟡 FF-COV-04 tiempo real en test: /'
done
true
EOF
```

**Cómo interpretar:**
- **Dónde van los tests:** hoy solo `:app` declara `testImplementation(libs.junit)` y las dependencias de Compose UI Test. Un test JVM de un holder de `feature:<x>` va en `feature/<x>/src/test/kotlin/com/invex/sportkid/feature/<x>/<X>StateTest.kt` y el PR agrega `testImplementation(libs.junit)` a ese módulo (alias que ya está en el catálogo: no es una librería nueva, ver FF-DEP-03). `core:model` es Kotlin puro: `./gradlew :core:model:test`. Los Compose UI Tests pueden vivir en `app/src/androidTest` (`:app` ve todos los features) o en el `androidTest` del módulo si se le agregan las dependencias.
- **FF-COV-01:** el holder se construye directo (`AgendaState(month, selectedDate)`) con una fecha fija, se llama cada método y se lee el estado (`state.selectedDate`, `state.matches(event)`); `mutableStateOf` funciona en JVM sin emulador. El `Saver` se prueba guardando y restaurando (`with(AgendaState.Saver) { SaverScope { true }.save(state) }` → `restore(...)`).
- **FF-COV-02:** modelos sin lógica (`data class` solo con campos, `enum` sin funciones, `object <X>Samples`) no exigen test; las funciones y propiedades calculadas sí (`SwimTime.format()`, `RaceTime`, `Belt.next`, `LocalDate.ageOn(today)`). Las funciones `@Composable` de `DateLabels` dependen de recursos: su lógica se cubre con un Compose UI Test o se extrae a una función pura.
- **FF-COV-03:** el Compose UI Test monta la pantalla con datos de `<X>Samples`, un `today` fijo y lambdas que registran los callbacks; verifica la interacción principal (seleccionar, validar, guardar). Es 🟡 mientras TD-SK-001 siga abierta.
- Los tests instrumentados **no corren en este skill** (requieren emulador, `ANDROID_SERIAL=emulator-5554`): si hace falta, `./gradlew :app:connectedDebugAndroidTest` y anexar el resultado. Nunca afirmes que pasan sin haberlos corrido. Los JVM sí: `./gradlew testDebugUnitTest` (todos los módulos Android) y `./gradlew :core:model:test`.
- Si el PR agrega los primeros tests reales o la infraestructura (Roborazzi, `testImplementation` en módulos), reduce TD-SK-001: actualiza su fila en `CLAUDE.md` (ver `sportkid-governance:propagacion`).
