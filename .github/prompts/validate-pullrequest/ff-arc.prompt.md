<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde .claude/commands/validate-pullrequest/ff-arc.md. NO EDITAR A MANO. -->
---
mode: agent
description: 5.2 Revisiones de Arquitectura y Capas (FF-ARC-XX)
---

## 5.2 Revisiones de Arquitectura y Capas (FF-ARC-XX)

Verifica el contrato de SportKid (`CLAUDE.md` → Arquitectura y Patrón de pantalla; README §6.1): multimódulo por capas + features, **pantallas que reciben datos + callbacks** (sin ViewModel, sin `NavController`), navegación por estado en `SportKidApp` (`enum Destination` en `app/.../MainActivity.kt`), estado de formularios en holders `@Stable <X>State` con `rememberSaveable(saver = …)`, datos de ejemplo en `object <X>Samples` y estados de dominio como `enum` de `core:model` con su presentación **una sola vez** en `core:ui`. Corre si el cambio toca pantallas, holders de estado, modelos, `:app` o algún `build.gradle.kts` de módulo.

> Las reglas de importación entre módulos (feature → feature, `core:designsystem` → dominio) se revisan siempre en FF-CORE-04; aquí se cubre su contraparte en Gradle y el patrón de pantalla.

| ID | Regla | Severidad |
|---|---|---|
| FF-ARC-01 | Aparece una capa que SportKid aún no tiene: `ViewModel`/`viewModel()`/`hiltViewModel()`, `NavController`/`NavHost`/`navigate(`, repositorio o carpeta `data/`. Requiere decisión explícita registrada en `CLAUDE.md` (Patrón de pantalla; TD-SK-002/003) en el mismo PR | 🟠 |
| FF-ARC-02 | `build.gradle.kts` de módulo que rompe las reglas de dependencia: `feature:*` → otro `feature:*`; `core:designsystem` → `core:model`/`core:ui`; `core:*` → `feature:*`; `core:model` con Compose/Android (es Kotlin puro) | 🔴 |
| FF-ARC-03 | Pantalla `<X>Screen` pública nueva que **no** está conectada a `SportKidApp` (no se usa en `:app`), o sin callbacks de salida (`onBack`/`onClose`/`onSave`…) cuando tiene navegación | 🟡 |
| FF-ARC-04 | Navegación o efectos de plataforma desde un feature: `Intent(`, `startActivity`, `BackHandler`, `Activity`/`finish()`. Van a `:app` vía callback (como `shareText` en `MainActivity.kt`) | 🟠 |
| FF-ARC-05 | Holder de estado `@Stable class <X>State` sin `Saver` + `remember<X>State` con `rememberSaveable(saver = …)` (no sobrevive rotación ni muerte de proceso) | 🟠 |
| FF-ARC-06 | Estado de formulario suelto en la pantalla: 3+ `rememberSaveable`/`remember { mutableStateOf }` nuevos en un mismo `*Screen.kt` en lugar de un holder `<X>State` | 🔎 señal |
| FF-ARC-07 | Monto de dinero como `Float`/`Double` (hoy pesos enteros `Int`, a futuro centavos `Long`, README M7) | 🔴 |
| FF-ARC-08 | Lógica de filtrado/totales en el cuerpo de un composable en vez de en el holder `<X>State`, en `<X>Models.kt` o en una extensión | 🔎 señal |
| FF-ARC-09 | Estado de dominio fuera de lugar: `enum class …(Status|Result|Role)` nuevo fuera de `core:model` cuando lo usan 2+ features, o presentación (texto/icono/color) de un enum de `core:model` (`Sport`, `Belt`, `MedalResult`, `PaymentStatus`, `SwimStyleStatus`, `FamilyRole`) resuelta con `when` en un feature en vez de reutilizar la de `core:ui` | 🟡 |
| FF-ARC-10 | `*Samples` usado dentro del cuerpo de una pantalla (no como default de parámetro ni en preview): la pantalla deja de recibir sus datos; `object <X>Samples` nuevo fuera de `<X>Models.kt` | 🔎 señal / 🟡 |
| FF-ARC-11 | Persistencia nueva fuera de `ThemePrefs.kt` (`getSharedPreferences`, DataStore, Room, archivos): requiere decisión en `CLAUDE.md` (TD-SK-002) | 🟠 |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
ADDED=$(added_main)

# FF-ARC-01 — capas que aún no existen (ViewModel, Navigation Compose, datos)
printf '%s\n' "$ADDED" | grep -E 'import androidx\.lifecycle\.(ViewModel|viewmodel)|import androidx\.navigation\.|import dagger\.hilt|\bviewModel\(|hiltViewModel\(|: *ViewModel\(\)|NavController|NavHost\(|\.navigate\(' \
  | sed 's/^/🟠 FF-ARC-01 capa nueva (ViewModel\/Navigation) — ¿decisión registrada en CLAUDE.md? /'
for f in $PR_VIEWMODELS $PR_DATA; do
  is_new_file "$f" && echo "🟠 FF-ARC-01 $f — ViewModel/repositorio nuevo: SportKid aún no tiene esa capa (CLAUDE.md → Patrón de pantalla, TD-SK-002)"
done
pr_diff --name-only -- 'CLAUDE.md' | grep -q . && echo "ℹ️ FF-ARC-01 el PR modifica CLAUDE.md: confirma si registra la decisión"

# FF-ARC-02 — reglas de dependencia en Gradle
for f in $(pr_diff --name-only --diff-filter=ACMR -- 'core/*/build.gradle.kts' 'feature/*/build.gradle.kts'); do
  mod=$(module_of "$f")
  added_lines "$f" | grep -E 'project\("[^"]+"\)' | while IFS= read -r l; do
    dep=$(echo "$l" | sed -E 's/.*project\(":?([^"]+)"\).*/\1/' | tr '/' ':')
    case "$mod $dep" in     # separador ASCII: bash 3.2 (macOS) no casa patrones con multibyte
      "feature:"*" feature:"*)                     echo "🔴 FF-ARC-02 $mod depende de $dep (un feature nunca depende de otro) $l" ;;
      "core:designsystem core:"*)                  echo "🔴 FF-ARC-02 core:designsystem no depende de $dep (sin dominio) $l" ;;
      "core:"*" feature:"*|"core:"*" app")         echo "🔴 FF-ARC-02 $mod depende de $dep (core no conoce features) $l" ;;
      "core:model "*)                              echo "🔴 FF-ARC-02 core:model es Kotlin puro y no depende de $dep $l" ;;
    esac
  done
done
added_lines 'core/model/*' | grep -E 'import (androidx|android)\.|compose' | sed 's/^/🔴 FF-ARC-02 core:model debe ser Kotlin puro /'

# FF-ARC-03 — pantallas nuevas conectadas a SportKidApp
for f in $(printf '%s\n' $PR_MAIN_NEW | grep -E '^feature/.*Screen\.kt$'); do
  for s in $(grep -oE '^fun [A-Z][A-Za-z0-9]*Screen\(' "$f" | sed -E 's/fun (.*)\(/\1/'); do
    grep -rqE "\b$s\(" app/src/main || echo "🟡 FF-ARC-03 $f — $s no se usa en :app (falta Destination/callbacks en SportKidApp)"
  done
done

# FF-ARC-04 — navegación o efectos de plataforma en features/core
printf '%s\n' "$ADDED" | grep -E '^(feature|core)/' \
  | grep -E 'Intent\(|startActivity|BackHandler|\bfinish\(\)|as\??[[:space:]]*(Component)?Activity\b' \
  | sed 's/^/🟠 FF-ARC-04 navegación\/efecto de plataforma fuera de :app — entrégalo por callback /'

# FF-ARC-05 — holders de estado con Saver
for f in $PR_MAIN; do
  added_lines "$f" | grep -qE '^[^:]*:[0-9]+:@Stable' || continue
  grep -A2 '^@Stable' "$f" | grep -oE 'class [A-Z][A-Za-z0-9]*State\b' | sed 's/class //' | while read -r st; do
    grep -qE "Saver" "$f" || echo "🟠 FF-ARC-05 $f — $st sin Saver (listSaver/mapSaver)"
    grep -qE "fun remember$st\(" "$f" || echo "🟠 FF-ARC-05 $f — falta remember$st() con rememberSaveable(saver = …)"
  done
done

# FF-ARC-06 — estado de formulario suelto (señal)
for f in $PR_SCREENS; do
  n=$(added_lines "$f" | grep -cE 'rememberSaveable[^{]*\{[[:space:]]*mutableState|remember[[:space:]]*\{[[:space:]]*mutableState')
  [ "$n" -ge 3 ] && echo "🔎 FF-ARC-06 $f — $n estados nuevos con remember*/mutableStateOf: ¿holder <X>State?"
done

# FF-ARC-07 — dinero en coma flotante
printf '%s\n' "$ADDED" \
  | grep -iE '(amount|price|total|cost|fee|balance|pesos|cents|monto|cuota|pago)[A-Za-z]*[[:space:]]*:[[:space:]]*(Float|Double)\b' \
  | sed 's/^/🔴 FF-ARC-07 dinero en Float\/Double /'

# FF-ARC-08 — cálculos de colección en pantallas (señal)
for f in $PR_SCREENS; do
  added_lines "$f" | grep -E '\.(filter|sumOf|count|groupBy|sortedBy|sortedWith)[[:space:]]*\{' \
    | sed 's/^/🔎 FF-ARC-08 ¿lógica derivada en la UI? /'
done

# FF-ARC-09 — estados de dominio
printf '%s\n' "$ADDED" | grep -vE '^core/model/' | grep -E 'enum class [A-Z][A-Za-z]*(Status|Result|Role)\b' \
  | sed 's/^/🔎 FF-ARC-09 ¿enum de estado de dominio? si lo usan 2+ features va en core:model /'
printf '%s\n' "$ADDED" | grep -E '^feature/' \
  | grep -E 'when *\([^)]*\b(sport|belt|medal|result|status|role|paymentStatus)\b[^)]*\)' \
  | sed 's/^/🔎 FF-ARC-09 ¿presentación de un enum de core:model en el feature? (Sport.colors(), Belt.color(), MedalResult.colors()… en core:ui) /'

# FF-ARC-10 — Samples dentro del cuerpo de una pantalla (fuera de defaults y previews)
for f in $PR_SCREENS; do
  lines=$(added_lines "$f" | cut -d: -f2 | tr '\n' ' ')
  [ -z "$lines" ] && continue
  awk -v F="$f" -v L=" $lines " '
    /^(private |internal )?fun / { fn=$0 }
    index(L, " " NR " ") && /Samples\./ && fn !~ /Preview\(/ && $0 !~ /^[[:space:]]*[a-z][A-Za-z0-9]*:[^=]*=/ {
      print "🔎 FF-ARC-10 " F ":" NR " datos de ejemplo dentro de la pantalla — ¿parámetro con default o desde :app?: " $0 }' "$f"
done
printf '%s\n' "$ADDED" | grep -E 'object [A-Z][A-Za-z]*Samples\b' | grep -vE '^[^:]*Models\.kt:' \
  | sed 's/^/🟡 FF-ARC-10 Samples fuera de <X>Models.kt /'

# FF-ARC-11 — persistencia nueva
printf '%s\n' "$ADDED" | grep -v '/ThemePrefs\.kt:' \
  | grep -E 'getSharedPreferences|import androidx\.datastore|import androidx\.room|openFileOutput|FileOutputStream' \
  | sed 's/^/🟠 FF-ARC-11 persistencia nueva — ¿decisión en CLAUDE.md (TD-SK-002)? /'
true
EOF
```

**Cómo interpretar:**
- **FF-ARC-01:** el siguiente paso documentado (`CLAUDE.md` → Patrón de pantalla) es `ViewModel` + `UiState` `@Immutable` por pantalla y repositorios en `core:data`, pero **requiere decisión explícita**. Si el PR la trae, debe actualizar `CLAUDE.md` (y `AGENTS.md` con `python3 .github/scripts/sync_claude_to_copilot.py`), agregar la dependencia al catálogo (FF-DEP) y saldar o reducir TD-SK-002/003; si no, 🟠.
- **FF-ARC-02:** la salida correcta cuando una pieza se necesita en dos features es subirla a `core:ui` (con dominio) o a `core:designsystem` (sin dominio), nunca depender del otro feature.
- **FF-ARC-03 / FF-ARC-04:** patrón correcto: `fun RaceFormScreen(athlete, onClose: () -> Unit, onSave: (RaceDraft) -> Unit, modifier: Modifier = Modifier, today: LocalDate = LocalDate.now(), state = rememberRaceFormState(…))`; `SportKidApp` agrega el `Destination`, decide a dónde volver (`*Return`) y ejecuta los efectos (compartir, abrir Maps). Si la pantalla se agrega al catálogo, `Destination` también habilita `sk_screen`.
- **FF-ARC-05:** referencia: `AgendaState`/`NewTrainingState` (feature:agenda) y `RaceFormState` (feature:athletics) — `@Stable class`, `companion object { val Saver = listSaver<…>(…) }` y `@Composable fun remember<X>State(…) = rememberSaveable(saver = <X>State.Saver) { <X>State(…) }`.
- **FF-ARC-07:** hoy los montos son pesos enteros (`costPesos: Int`); README M7 deja pendiente emitir centavos como `Long`. Nunca `Float`/`Double`.
- **FF-ARC-08:** formatear (`DateLabels`, `amountLabel()`) en la UI es correcto; filtrar o sumar no: muévelo a una propiedad del holder (`AgendaState`) o a una función de `<X>Models.kt`.
- **FF-ARC-09:** vocabulario y presentación únicos (README §5, `CLAUDE.md` → Estados de dominio): un `enum` en `core:model` y su `label`/`icon`/`color` una sola vez en `core:ui` (`BeltUi.kt`, `MedalUi.kt`, `SportColorsExt.kt`, `FamilyRoleUi.kt`). Un enum que solo vive en una pantalla (`RaceType`, `AgendaView`) puede quedarse en el feature.
- **FF-ARC-10:** `athletes: List<AgendaAthlete> = AgendaSamples.athletes` como default de parámetro y los `Samples` en previews son el patrón vigente; leerlos dentro del cuerpo no.
