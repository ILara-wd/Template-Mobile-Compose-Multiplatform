## 5.4 Revisiones de Composables (FF-CMP-XX)

Solo se revisan los composables cuya firma es nueva o cambió respecto de la base (los demás son deuda heredada). Verifica el contrato de composables de SportKid (`CLAUDE.md` → Convenciones → Componentes y Patrón de pantalla; README §1.6–§3 y §6): `modifier: Modifier = Modifier` como **primer opcional** aplicado a la raíz, slots `@Composable` al final, parámetro nuevo en componente existente al final y con default, átomos `Sk*` de `core:designsystem` sin dominio, piezas con dominio en `core:ui`, secciones `private` en el feature, sin estado de negocio dentro de componentes compartidos, ≥ 1 preview `private` con `@SkPreviews` y ningún ViewModel/`NavController` en firmas.

| ID | Regla | Severidad |
|---|---|---|
| FF-CMP-01 | Composable público o `internal` (átomo, pieza de `core:ui`, `<X>Screen`, sección `internal`) sin `modifier: Modifier = Modifier` | 🟠 |
| FF-CMP-02 | `modifier` fuera de posición (después de otro opcional con default), sin default, no aplicado o aplicado más de una vez | 🟠 |
| FF-CMP-03 | Estado de negocio dentro de `core:designsystem`/`core:ui`: `remember { mutableStateOf(...) }` que guarda un valor que el llamador debería controlar — **señal a confirmar** | 🟠 si se confirma |
| FF-CMP-04 | Pieza en el módulo equivocado: átomo de `core:designsystem` que importa dominio (`core.model`, `core.ui`, `feature.*`) o el `R` de otro módulo; pieza nueva de `core:ui` que no usa ningún tipo de `core:model` (¿átomo `Sk*`?) | 🟠 / 🔎 |
| FF-CMP-05 | 3+ parámetros `Boolean` de apariencia/visibilidad en un componente | 🟠 nuevo · 🟡 existente |
| FF-CMP-06 | `MutableState`/`MutableStateFlow`/`SnapshotStateList` como parámetro de un composable (pasa valor + `onXChange`, o el holder `<X>State` completo) | 🟠 |
| FF-CMP-07 | Componente nuevo con nombre/rol parecido a uno existente — ver FF-DUP y `/catalogo-ui-sportkid consultar` | 🟠 señal |
| FF-CMP-08 | Composable **nuevo** con un slot `@Composable … -> Unit` seguido de parámetros que no son slots (los slots van al final). En uno existente, el parámetro nuevo va al final con default aunque quede después de un slot | 🟡 |
| FF-CMP-15 | Composable público que propaga un tipo experimental (`@ExperimentalMaterial3Api` en la firma o parámetros `SheetState`/`DatePickerState`/`TimePickerState`/`TopAppBarScrollBehavior`) | 🟡 |
| FF-CMP-09..14 | Superficie de composables: sin preview, preview pública, ViewModel/`NavController` en la firma, composable público de feature que `:app` no usa, firma incompatible de componente compartido, preview sin `@SkPreviews` — **script** `validate-pullrequest-api.py` | 🟠/🔴/🟡 |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
TARGETS=$(printf '%s\n' $PR_MAIN | grep . | xargs -r grep -l '@Composable' 2>/dev/null || true)
[ -z "$TARGETS" ] && { echo "ℹ️ FF-CMP: sin composables en el cambio"; exit 0; }

python3 - $TARGETS <<'PY'
import os, re, subprocess, sys
before = os.environ["BASE_SNAPSHOT"]

def signatures(src):
    for m in re.finditer(r'@Composable\s+(?:(public|internal|private)\s+)?fun\s+(?:[A-Za-z_.]+\.)?([A-Z]\w*)\s*\(', src):
        ctx = src[max(0, m.start()-200):m.start()]
        if m.group(1) == 'private' or re.search(r'@(Preview|SkPreviews)\b', ctx.split('\n}')[-1]):
            continue
        i, depth = m.end(), 1
        while i < len(src) and depth:
            depth += {'(':1, ')':-1}.get(src[i], 0); i += 1
        raw, params, buf, d, prev = src[m.end():i-1], [], '', 0, ''
        for ch in raw:
            if ch in '(<{[': d += 1
            if ch in ')}]' or (ch == '>' and prev != '-'): d -= 1   # el '>' de '->' no cierra genéricos
            prev = ch
            if ch == ',' and d == 0: params.append(buf.strip()); buf = ''
            else: buf += ch
        if buf.strip(): params.append(buf.strip())
        params = [re.sub(r'\s+', ' ', re.sub(r'//[^\n]*', '', p)).strip() for p in params]
        yield src[:m.start()].count('\n')+1, m.group(2), [p for p in params if p], i, ctx

for f in sys.argv[1:]:
    src = open(f).read()
    old_src = subprocess.run(['git','show',f'{before}:{f}'], capture_output=True, text=True).stdout
    new = not old_src
    old_sigs = {n: p for _, n, p, _, _ in signatures(old_src)}
    for line, name, params, body_start, ctx in signatures(src):
        loc = f"{f}:{line} {name}"
        if name == 'SportKidApp' or name.endswith(('Theme', 'Dialog')):
            continue   # raíz de la app, wrappers de tema y diálogos no llevan modifier
        if old_sigs.get(name) == params:
            continue   # firma sin cambios: deuda heredada, no se reporta
        new_fn = new or name not in old_sigs
        mod_idx = next((k for k,p in enumerate(params) if re.match(r'modifier\s*:\s*Modifier\b', p)), None)
        if mod_idx is None:
            print(f"🟠 FF-CMP-01 {loc} — sin `modifier: Modifier = Modifier`")
        else:
            if not re.search(r'=\s*Modifier\s*$', params[mod_idx]):
                print(f"🟠 FF-CMP-02 {loc} — modifier sin default `= Modifier`")
            before_mod = params[:mod_idx]
            if any('=' in p for p in before_mod):
                print(f"🟠 FF-CMP-02 {loc} — opcionales antes de `modifier`: {[p.split(':')[0] for p in before_mod if '=' in p]}")
            j = src.find('{', body_start); body = ''
            if j != -1:
                d, k = 0, j
                while k < len(src):
                    d += {'{':1, '}':-1}.get(src[k], 0); k += 1
                    if d == 0: break
                body = re.sub(r'//[^\n]*', '', src[j:k])
            uses = len(re.findall(r'(?<![.\w])modifier\b(?!\s*(?::|=(?!=)))', body))
            if uses == 0: print(f"🟠 FF-CMP-02 {loc} — `modifier` no se usa en el cuerpo")
            elif uses > 1: print(f"🔎 FF-CMP-02 {loc} — `modifier` aparece {uses} veces (confirmar que solo se aplica a la raíz)")
        bools = [p for p in params if re.search(r':\s*Boolean\b', p)]
        if len(bools) >= 3:
            print(f"{'🟠' if new_fn else '🟡'} FF-CMP-05 {loc} — {len(bools)} Boolean: {[b.split(':')[0].strip() for b in bools]} → ¿enum/objeto de estilo o slot?")
        for p in params:
            if re.search(r':\s*(MutableState|MutableStateFlow|SnapshotStateList|SnapshotStateMap)\b', p):
                print(f"🟠 FF-CMP-06 {loc} — parámetro mutable: {p}")
        is_slot = [bool(re.search(r':\s*\(?\s*@Composable[^=]*->\s*Unit\b', p)) for p in params]
        if new_fn and True in is_slot:
            first = is_slot.index(True)
            late = [params[k].split(':')[0] for k in range(first, len(params)) if not is_slot[k]]
            if late:
                print(f"🟡 FF-CMP-08 {loc} — parámetros después del primer slot: {late} (los slots van al final)")
        if 'ExperimentalMaterial3Api' in ctx.split('\n\n')[-1] and '@OptIn' not in ctx.split('\n\n')[-1] or \
           any(re.search(r':\s*(SheetState|DatePickerState|TimePickerState|TopAppBarScrollBehavior)\b', p) for p in params):
            print(f"🟡 FF-CMP-15 {loc} — tipo experimental en la firma pública (encapsúlalo o usa @OptIn dentro)")
PY

# FF-CMP-03 — estado interno en componentes compartidos (señal)
for f in $PR_COMPONENTS $PR_CORE_UI; do
  added_lines "$f" | grep -E 'remember(Saveable)?[[:space:]]*(\([^)]*\))?[[:space:]]*\{[[:space:]]*mutableState(Of|ListOf|MapOf)' \
    | sed 's/^/🔎 FF-CMP-03 señal (¿estado visual o de negocio?) /'
done

# FF-CMP-04 — módulo correcto
for f in $PR_COMPONENTS; do
  added_lines "$f" | grep -E ':import com\.invex\.sportkid\.(core\.(model|ui)|feature)\.|:import com\.invex\.(one\.template|sportkid\.(core\.(model|ui)|feature\.[a-z]+))\.R$' \
    | sed 's/^/🟠 FF-CMP-04 átomo de core:designsystem acoplado al dominio /'
done
for f in $PR_CORE_UI; do
  is_new_file "$f" || continue
  grep -q 'import com\.invex\.sportkid\.core\.model\.' "$f" \
    || echo "🔎 FF-CMP-04 $f — pieza nueva de core:ui sin tipos de core:model: ¿átomo Sk* en core:designsystem?"
done
EOF

# FF-CMP-09..14 — superficie de composables (script)
BASE_BRANCH="$BASE_BRANCH" python3 .claude/scripts/validate-pullrequest-api.py
```

**Cómo confirmar FF-CMP-03:**
- ✅ Estado **visual** permitido: menú expandido (`BeltSelectField` en `BeltUi.kt`), diálogo de fecha/hora abierto (`FormDateTimeFields` en `FormParts.kt`), foco, animaciones, `InteractionSource`.
- ❌ Estado de **negocio**: el texto ingresado, el ítem seleccionado, el monto, un valor que la pantalla necesita guardar → `value` + `onValueChange` hoisteados al holder `<X>State` de la pantalla.
- Copiar un parámetro a `remember { mutableStateOf(param) }` duplica la fuente de verdad: también es FF-CMP-03.

**FF-CMP-04:** `core:designsystem` es reutilizable en cualquier app (no conoce `Sport`, `Belt` ni `Athlete`); lo que necesita colores de deporte o textos de dominio va a `core:ui` (`SportTag`, `EventCard`). Al revés, si la pieza nueva de `core:ui` solo recibe `String`/`Color`/`Painter`, probablemente es un átomo `Sk*`.

**FF-CMP-05:** los `Boolean` de estado hoisteado (`enabled`, `checked`, `selected`, `isError`, `expanded`) son legítimos. La señal son flags de apariencia (`showIcon`, `compact`, `outlined`) → un enum de tamaño/variante como `SkButtonSize`, parámetros de color con default (`containerColor`/`contentColor` en `SkButton`) o un slot `(@Composable () -> Unit)?`.

**FF-CMP-09:** cada composable público debe tener en su archivo una preview `private` con `@SkPreviews` (claro, oscuro, fuente 1.3) que lo renderice, envuelta en `SportKidTheme { … }`, con los datos de `Samples` (Mateo y Sofía) y "hoy" fijo (`LocalDate.of(…)`) para que sea determinista. Para revisar un tema MaterialKolor, `SportKidTheme(palette = AppPalette.Dojang)`.

**FF-CMP-13:** un componente de `core:designsystem` o `core:ui` se extiende agregando el parámetro **al final y con default** para no romper a quienes ya lo usan (`CLAUDE.md` → Componentes; `/catalogo-ui-sportkid` → "Extender").
