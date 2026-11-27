<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde .claude/commands/validate-pullrequest/ff-a11y.md. NO EDITAR A MANO. -->
---
mode: agent
description: 5.6 Revisiones de Accesibilidad (FF-A11Y-XX)
---

## 5.6 Revisiones de Accesibilidad (FF-A11Y-XX)

La accesibilidad es parte del contrato de SportKid (`CLAUDE.md` §Convenciones → Accesibilidad; README §0 "Color nunca solo", §1.4 área táctil y §6.2 punto 6; deuda TD-SK-006). Solo se evalúan llamadas que **empiezan en líneas agregadas** de `app/src/main`, `core/*/src/main` y `feature/*/src/main`.

| ID | Regla | Severidad |
|---|---|---|
| FF-A11Y-01 | `Icon(`/`Image(` sin `contentDescription` (ni nombrado ni como 2.º argumento posicional) | 🔴 |
| FF-A11Y-02 | `contentDescription = null` (o `null` posicional) en un icono que es la **única** señal de una acción (dentro de `IconButton`) → 🔴. En cualquier otro caso es señal: confirmar que es decorativo (texto adyacente que dice lo mismo o contenedor con `semantics(mergeDescendants = true)`) | 🔴 / 🔎 |
| FF-A11Y-03 | `clickable`/`toggleable`/`selectable` sin `role`; control (`SkSwitch`, `Switch`, `Checkbox`, `RadioButton`) con su propio callback en lugar de la fila completa; `selectable` en un archivo sin `selectableGroup()` | 🟡 señal |
| FF-A11Y-04 | Área táctil < 48dp (`SkDimens.minTouchTarget`) en un elemento clicable | 🟡 señal |
| FF-A11Y-05 | Estado comunicado solo por color (pagado/pendiente, dominado/aprendiendo, seleccionado) sin texto ni icono | 🟡 señal |
| FF-A11Y-06 | `contentDescription = "literal"` (debe salir de `stringResource`, ver FF-I18N-01) | 🟡 |
| FF-A11Y-07 | Texto que no soporta `fontScale` 1.3: escalado manual `× fontScale` (🔴); `height(N.dp)` fijo en un contenedor con texto, `maxLines` sin `overflow` (🟡 señal) | 🔴 / 🟡 |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
ADDED=$(added_main)
[ -z "$ADDED" ] && { echo "ℹ️ FF-A11Y: sin líneas nuevas en código productivo"; exit 0; }
TMP_ADDED=$(mktemp); printf '%s\n' "$ADDED" > "$TMP_ADDED"

# FF-A11Y-01 / 02 / 06 — Icon/Image en líneas agregadas (argumento nombrado o posicional)
# FF-A11Y-04 — tamaño < 48dp en la cadena de un elemento clicable (señal)
python3 - "$TMP_ADDED" <<'PY'
import re, sys, collections
added = collections.defaultdict(set)
for l in open(sys.argv[1]):
    if l.count(':') < 2: continue
    f, n, _ = l.split(':', 2); added[f].add(int(n))

def args_of(src, start):
    """Argumentos de primer nivel de la llamada que empieza en src[start]."""
    depth, cur, out, q = 0, '', [], None
    for ch in src[src.index('(', start):]:
        if q:
            cur += ch
            if ch == q: q = None
            continue
        if ch in '([{':
            depth += 1
            if depth == 1: continue
        elif ch in ')]}':
            depth -= 1
            if depth == 0: out.append(cur); break
        elif ch == ',' and depth == 1:
            out.append(cur); cur = ''; continue
        elif ch == '"': q = ch
        cur += ch
    return [a.strip() for a in out if a.strip()]

for f, lines in added.items():
    src = open(f).read(); text = src.split('\n')
    for m in re.finditer(r'(?<![\w.])(Icon|Image)\s*\(', src):
        line = src.count('\n', 0, m.start()) + 1
        if line not in lines: continue
        loc = f"{f}:{line} {m.group(1)}"
        args = args_of(src, m.start())
        named = [a for a in args if re.match(r'contentDescription\s*=', a)]
        positional = [a for a in args if not re.match(r'[a-zA-Z]+\s*=(?!=)', a)]
        if named: value = named[0].split('=', 1)[1].strip()
        elif len(positional) >= 2: value = positional[1]
        else:
            print(f"🔴 FF-A11Y-01 {loc} — sin contentDescription"); continue
        if value == 'null':
            if 'IconButton(' in ' '.join(text[max(0, line - 3):line]):
                print(f"🔴 FF-A11Y-02 {loc} — icono de IconButton con contentDescription null (es la única señal de la acción)")
            else:
                print(f"🔎 FF-A11Y-02 {loc} — contentDescription null: ¿decorativo? (texto adyacente o mergeDescendants)")
        elif value.startswith('"'):
            print(f"🟡 FF-A11Y-06 {loc} — contentDescription literal {value} (usar stringResource)")
    for n in sorted(lines):
        if not re.search(r'\.(clickable|toggleable|selectable)\(|IconButton\(', text[n - 1]): continue
        window = ' '.join(text[max(0, n - 4):n + 3])
        small = [int(v) for v in re.findall(r'\.(?:size|height|width)\((\d+)\.dp\)', window) if int(v) < 48]
        if small:
            print(f"🔎 FF-A11Y-04 {f}:{n} — elemento clicable con {small[0]}dp (< 48dp: sizeIn(min = SkDimens.minTouchTarget) o minimumInteractiveComponentSize())")
PY
rm -f "$TMP_ADDED"

# TD-SK-006 — conteo de contentDescription null en todo el código (no debe crecer sin justificación)
NULL_RE='contentDescription = null|(Icon|Image)\(.*\), *null[,)]'
grep_after() { if [ "${STAGED:-0}" = 1 ]; then git grep --cached -hE "$1" -- "${@:2}"; else git grep -hE "$1" HEAD -- "${@:2}"; fi; }
before=$(git grep -hE "$NULL_RE" "$BASE_SNAPSHOT" -- "${MAIN_KT[@]}" 2>/dev/null | wc -l | tr -d ' ')
after=$(grep_after "$NULL_RE" "${MAIN_KT[@]}" | wc -l | tr -d ' ')
echo "ℹ️ TD-SK-006 contentDescription null: $before → $after"

# FF-A11Y-03 — clickable/selectable sin role, control con callback propio, selectable sin selectableGroup
printf '%s\n' "$ADDED" | grep -E '\.(clickable|toggleable|selectable|combinedClickable)[[:space:]]*(\(|\{)' | grep -vE 'role *=' \
  | sed 's/^/🔎 FF-A11Y-03 ¿role (Role.Button\/Switch\/Checkbox\/RadioButton\/Tab)? /'
printf '%s\n' "$ADDED" | grep -E '(SkSwitch|Switch|Checkbox|RadioButton)\(' | grep -E '(onCheckedChange|onClick) *=' | grep -vE '(onCheckedChange|onClick) *= *null' \
  | sed 's/^/🔎 FF-A11Y-03 control con callback propio — ¿la fila debería ser toggleable\/selectable y el control ir con null? /'
for f in $(printf '%s\n' "$ADDED" | grep -E '\.selectable\(' | cut -d: -f1 | sort -u); do
  grep -q 'selectableGroup' "$f" || echo "🔎 FF-A11Y-03 $f — usa selectable sin selectableGroup() en el contenedor"
done

# FF-A11Y-05 — color que cambia con un estado (señal: confirmar que también cambia texto o icono)
printf '%s\n' "$ADDED" | grep -E '(color|containerColor|tint|background)[^=]*= *if *\([^)]*(paid|Paid|status|Status|selected|mastered|done)[^)]*\)' \
  | sed 's/^/🔎 FF-A11Y-05 color según estado — ¿hay texto o icono que diga lo mismo? /'

# FF-A11Y-07 — escala de fuente
printf '%s\n' "$ADDED" | grep -E 'fontScale[[:space:]]*\*|\*[[:space:]]*[A-Za-z.]*fontScale' \
  | sed 's/^/🔴 FF-A11Y-07 escalado manual de fuente (escala dos veces) /'
printf '%s\n' "$ADDED" | grep -E '\.height\([0-9]+\.dp\)' \
  | sed 's/^/🔎 FF-A11Y-07 alto fijo — si el contenedor tiene texto usar heightIn(min = …) /'
printf '%s\n' "$ADDED" | grep -E 'maxLines *= *[0-9]' | grep -v 'overflow' \
  | sed 's/^/🔎 FF-A11Y-07 maxLines sin overflow en la misma línea — ¿elipsis? (README §7 Truncado) /'
true
EOF
```

**Cómo interpretar:**
- **FF-A11Y-02:** un icono es decorativo solo si hay texto adyacente que dice lo mismo (el icono del deporte junto a "Taekwondo" en `SportTag`) o el contenedor ya expone `semantics(mergeDescendants = true)` con su descripción (`MembersScreen`). Si es la **única** señal de una acción (atrás, cerrar, eliminar) o de un estado (pagado, récord), necesita `contentDescription = stringResource(R.string.<pantalla>_<desc>)`, como `MonthCalendar` (`agenda_prev_month`) o `QuickAddSheet` (`quick_add_close`). Hoy hay ~180 `null` sin revisar (TD-SK-006): el PR no debe sumar `null` nuevos sin confirmar que son decorativos; si el conteo baja, la deuda se reduce (ver `sportkid-governance:propagacion`).
- **FF-A11Y-03:** el patrón de SportKid para selección y toggles es la fila completa con `Modifier.toggleable(value, role = Role.Switch, onValueChange = …)` o `Modifier.selectable(selected, role = Role.RadioButton, onClick = …)` y el control interno sin interacción (`SkSwitch(checked, onCheckedChange = null)`, `RadioButton(onClick = null)`), con `selectableGroup()` en el contenedor. Referencias: `FeeRow` (`core/ui/FormParts.kt`), `SettingsRow` (`core/designsystem/component/SkSettings.kt`), `MedalResultPicker` (`core/ui/MedalUi.kt`), `SkSegmentedControl` (`Role.Tab`). Un `clickable` que abre algo usa `role = Role.Button` y, si el texto visible no basta, `onClickLabel` (`MonthCalendar`, `JoinCodeScreen`).
- **FF-A11Y-04:** amplía el área con `Modifier.sizeIn(minWidth = SkDimens.minTouchTarget, minHeight = SkDimens.minTouchTarget)` o `minimumInteractiveComponentSize()` en el contenedor, no reduciendo el icono. Los `IconButton` de M3 ya miden 48dp: solo revisa si se les fuerza un tamaño menor. El tamaño del icono dentro de un área de 48dp no es hallazgo.
- **FF-A11Y-05:** "Color nunca solo" (README §0): todo estado lleva icono o texto además del color (`PaymentStatusChip`, `MedalBadge`, `StyleRow`). El color de cada hijo solo va en anillos, puntos y bordes, nunca como fondo de texto (README §1.2).
- **FF-A11Y-07:** primero layout (`heightIn(min = …)`, `FlowRow`, `weight`); los nombres largos se truncan con elipsis en una línea y ningún dato numérico (tiempo, costo) se parte en dos líneas (README §7). Si el PR agrega o cambia un componente o pantalla, revisa su preview **"Fuente 1.3"** de `@SkPreviews` o pide una captura del emulador con fuente grande.
