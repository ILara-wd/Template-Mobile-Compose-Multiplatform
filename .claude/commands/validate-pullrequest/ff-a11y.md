## 5.6 Revisiones de Accesibilidad (FF-A11Y-XX)

La accesibilidad es parte del contrato (`03-standards.md §4`, ADR-008, TD-KEN-002). Solo se evalúan llamadas que **empiezan en líneas agregadas**.

| ID | Regla | Severidad |
|---|---|---|
| FF-A11Y-01 | `Icon(`/`Image(` sin argumento `contentDescription` (A11Y-01) | 🔴 |
| FF-A11Y-02 | `contentDescription = null` sin comentario en la misma línea (o la anterior) que justifique que es decorativo (A11Y-01) | 🔴 |
| FF-A11Y-03 | `clickable`/`toggleable`/`selectable` sin `role`, o control seleccionable que no está sobre la fila completa (A11Y-02) | 🟡 señal |
| FF-A11Y-04 | Área táctil < 48dp en un elemento clicable (A11Y-03) | 🟡 señal |
| FF-A11Y-05 | Estado comunicado solo por color (pendiente/vencido/pagado sin texto ni icono) (A11Y-05) | 🟡 señal |
| FF-A11Y-06 | `contentDescription = "literal"` (I18N-01) | 🟡 |
| FF-A11Y-07 | Texto que no escala o escala dos veces: `× fontScale` (🔴) · `.height(TkdDimens.…)` nuevo en un contenedor con texto, tope `TkdFontScale` sin comentario (🟡 señal) (A11Y-06) | 🔴 / 🟡 |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
[ -z "$PR_MAIN" ] && { echo "ℹ️ FF-A11Y: sin código productivo en el cambio"; exit 0; }
TMP_ADDED=$(mktemp); added_main > "$TMP_ADDED"

python3 - "$TMP_ADDED" <<'PY'
import re, sys, collections
added = collections.defaultdict(set)
for l in open(sys.argv[1]):
    f, n, _ = l.split(':', 2); added[f].add(int(n))

def call_at(src, start):
    i, d = src.index('(', start), 0
    for k in range(i, len(src)):
        d += {'(':1, ')':-1}.get(src[k], 0)
        if d == 0: return src[i:k+1]
    return src[i:]

for f, lines in added.items():
    src = open(f).read(); text_lines = src.split('\n')
    for m in re.finditer(r'(?<![\w.])(Icon|Image)\s*\(', src):
        line = src.count('\n', 0, m.start()) + 1
        if line not in lines: continue
        call = call_at(src, m.start()); loc = f"{f}:{line} {m.group(1)}"
        if 'contentDescription' not in call:
            print(f"🔴 FF-A11Y-01 {loc} — sin contentDescription"); continue
        cd = re.search(r'contentDescription\s*=\s*([^,\n)]*)', call)
        if not cd: continue
        value = cd.group(1).strip()
        if value == 'null':
            cd_line = line - 1 + call[:cd.start()].count('\n')
            ctx = text_lines[cd_line] + (text_lines[cd_line - 1] if cd_line else '')
            if '//' not in ctx and '/*' not in ctx:
                print(f"🔴 FF-A11Y-02 {loc} — contentDescription = null sin comentario que justifique que es decorativo")
        elif value.startswith('"'):
            print(f"🟡 FF-A11Y-06 {loc} — contentDescription literal {value} (usar stringResource)")
PY
rm -f "$TMP_ADDED"

# FF-A11Y-03 — clickable/selectable sin role (señal)
added_main | grep -E '\.(clickable|toggleable|selectable|combinedClickable)[[:space:]]*(\(|\{)' | grep -vE 'role *=' \
  | sed 's/^/🔎 FF-A11Y-03 ¿role\/onClickLabel? /'
added_main | grep -E '(RadioButton|Checkbox|Switch)\(' | grep -E 'onCheckedChange *= *\{|onClick *= *\{' \
  | sed 's/^/🔎 FF-A11Y-03 control interactivo propio — ¿debería ser la fila toggleable\/selectable con el control en null? /'

# FF-A11Y-04 — área táctil pequeña (señal)
added_main | grep -E '(IconButton|clickable|toggleable|selectable).*\.size\(TkdDimens\.(Icon|Space)(1[0-9]|2[0-9]|3[0-9]|4[0-7]|Small)?' \
  | sed 's/^/🔎 FF-A11Y-04 ¿área táctil < 48dp? /'

# FF-A11Y-07 — escala de fuente (A11Y-06, ADR-015)
added_main | grep -E 'fontScale[[:space:]]*\*|\*[[:space:]]*[A-Za-z.]*fontScale' | grep -v 'ui/theme/TkdFontScale.kt' \
  | sed 's/^/🔴 FF-A11Y-07 escalado manual de fuente (escala dos veces) /'
added_main | grep -E '\.height\(TkdDimens\.' \
  | sed 's/^/🔎 FF-A11Y-07 alto fijo — si el contenedor tiene texto usar heightIn(min = …) /'
added_main | grep -E 'withFontScale\(|TkdFontScaleLimit\(|shrinkToFit\(' | grep -v '//' | grep -v 'ui/theme/TkdFontScale.kt' \
  | sed 's/^/🔎 FF-A11Y-07 tope de fuente — ¿hay comentario en la línea anterior que justifique el espacio fijo? /'
EOF
```

**Cómo interpretar:**
- **FF-A11Y-02 bloquea aunque el ícono sea decorativo:** se exige que la decisión quede escrita en el código, p. ej. `contentDescription = null, // decorativo: el texto "Al día" ya lo dice`.
- Un ícono es decorativo solo si hay texto adyacente que dice lo mismo o el contenedor ya expone `semantics(mergeDescendants = true)`. Si es la **única** señal de una acción (volver, cerrar, enviar) o de un estado (vencido, verificado), necesita `contentDescription = stringResource(R.string.tkd_cd_…)`.
- **FF-A11Y-03:** el patrón de Kensho para selección y toggles es la fila completa con `Modifier.selectable(…, role = Role.RadioButton)` / `toggleable(…, role = Role.Switch)` y el control interno sin interacción (`TkdSwitch`, `RadioButton(onClick = null)`), con `selectableGroup()` en el contenedor.
- **FF-A11Y-04:** amplía el área con `Modifier.minimumInteractiveComponentSize()` en el contenedor, no reduciendo el ícono.
- **FF-A11Y-07:** primero layout (`heightIn(min)`, `FlowRow`, `widthIn(max)`); el tope `TkdFontScale.Limited` o `shrinkToFit()` solo en espacio realmente fijo. Si el PR agrega o cambia una pantalla, pide capturas a 150 % y 200 % (`docs/architecture/FONT_SCALE_ANALISIS.md §7.4`).
