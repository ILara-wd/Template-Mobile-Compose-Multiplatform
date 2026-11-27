## 5.7 Revisiones de Textos y Formato (FF-I18N-XX)

Todo texto visible sale de recursos del módulo dueño, en español de México, con el vocabulario único de estados y los formatos de README §7 (`CLAUDE.md` §Convenciones → Textos y Formatos; README §5, §6.2 punto 5 y §7).

| ID | Regla | Severidad |
|---|---|---|
| FF-I18N-01 | `Text("literal")` / `text = "…"` / `label = "…"` / `title = "…"` / `placeholder = "…"` en `src/main` fuera de `object <X>Samples` y de funciones `@SkPreviews`/`@Preview` | 🟠 |
| FF-I18N-02 | Concatenación o interpolación de textos de usuario (`"$a $b"`, `stringResource(x) + …`) en lugar de un recurso con argumentos (`%1$s`) | 🟡 |
| FF-I18N-03 | Formato fuera de las reglas de README §7: `String.format("…")`/`"…".format(…)` sin `Locale`, `DateTimeFormatter`/`SimpleDateFormat`/`getDisplayName` fuera de `DateLabels` (`core:ui`), `NumberFormat` sin locale, dinero con `.00` (`%,.2f`) | 🟡 |
| FF-I18N-04 | Clave nueva fuera de `<pantalla>_<descripcion>` (snake_case), con el **mismo nombre** que una clave de otro módulo (colisión al fusionar recursos → 🟠), valor ya existente en el módulo o en `core:ui`, sin uso, o bloque sin comentario `<!-- P05 Inicio -->` | 🟡 / 🟠 |
| FF-I18N-05 | Cadena nueva en inglés, fuera del vocabulario de README §5 ("Por pagar" en lugar de "Pendiente de pago"), o cantidad con `%d` que debería ser `plurals` | 🟡 señal |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
VALUES_SPEC=('app/src/main/res/values/*.xml' 'core/*/src/main/res/values/*.xml' 'feature/*/src/main/res/values/*.xml')
TMP_ADDED=$(mktemp); added_main > "$TMP_ADDED"

# FF-I18N-01 / 02 / 03 — Kotlin (descarta líneas dentro de object *Samples y de previews)
python3 - "$TMP_ADDED" <<'PY'
import re, sys
DECL = re.compile(r'^(?:(?:private|internal|public)\s+)?(?:fun|object|val|var|class|data class|enum class|sealed class|interface)\b')
cache = {}
def scope(f, n):
    """Declaración top-level que contiene la línea n: (texto, anotaciones)."""
    lines = cache.setdefault(f, open(f).read().split('\n'))
    i = n - 1
    while i >= 0 and not DECL.match(lines[i]): i -= 1
    if i < 0: return '', ''
    j, ann = i - 1, []
    while j >= 0 and lines[j].startswith('@'): ann.append(lines[j]); j -= 1
    return lines[i], ' '.join(ann)

def excluded(f, n):
    decl, ann = scope(f, n)
    return ('/preview/' in f or re.search(r'object\s+\w*Samples\b', decl) or re.search(r'@(SkPreviews|Preview)\b', ann)
            or re.search(r'fun\s+\w+Preview\s*\(', decl))

LIT = re.compile(r'(Text\(\s*"|\b(text|label|title|subtitle|placeholder|supportingText|overline|message) *= *")')
LIT_SKIP = re.compile(r'= *""|Text\(""|animate[A-Za-z]*(AsState|Transition)|Transition\(|label *= *"[a-z][A-Za-z]*"[,)]? *$')
CONCAT = re.compile(r'(Text\(|\btext *=).*("[^"]*\$\{?[a-zA-Z]|stringResource\([^)]*\) *\+|\+ *stringResource)')
FMT = [
    (re.compile(r'String\.format\("|"[^"]*"\.format\((?!\s*(Locale|EsMx|esMx))'), 'formato sin Locale (Locale("es", "MX") o Locale.ROOT solo para dígitos)'),
    (re.compile(r'DateTimeFormatter\.|SimpleDateFormat\(|\.getDisplayName\('), 'fecha formateada fuera de DateLabels (core:ui): usa shortLabel()/longLabel()/label()… o extiende DateLabels'),
    (re.compile(r'NumberFormat\.get[A-Za-z]*Instance\(\)'), 'NumberFormat sin locale'),
    (re.compile(r'%,\.2f|\.00 *MXN'), 'dinero con decimales: README §7 pide "$1,250 MXN" sin .00'),
]
for l in open(sys.argv[1]):
    if l.count(':') < 2: continue
    f, n, code = l.rstrip('\n').split(':', 2); n = int(n)
    c = re.sub(r'(^|\s)//\s.*$', '', code)
    if f.endswith('DateLabels.kt'): continue
    first = re.search(r'"([^"]*)"', c)
    has_words = first and re.search(r'[A-Za-zÁÉÍÓÚáéíóúñÑ]', re.sub(r'\$\{[^}]*\}|\$\w+', '', first.group(1)))
    if LIT.search(c) and has_words and not LIT_SKIP.search(c) and not excluded(f, n):
        print(f"🟠 FF-I18N-01 {f}:{n}:{code.strip()}")
    single = first and re.fullmatch(r'\$\{[^}]*\}|\$\w+', first.group(1))  # "$count": un solo valor, sin texto
    if CONCAT.search(c) and not single and not excluded(f, n):
        print(f"🟡 FF-I18N-02 {f}:{n}:{code.strip()}")
    for rx, msg in FMT:
        if rx.search(c):
            print(f"🟡 FF-I18N-03 {f}:{n} — {msg}: {code.strip()}"); break
PY
rm -f "$TMP_ADDED"

# FF-I18N-04 / 05 — cadenas nuevas en los values/*.xml de cada módulo
TMP_DIFF=$(mktemp); pr_diff -U0 -- "${VALUES_SPEC[@]}" > "$TMP_DIFF"
python3 - "$TMP_DIFF" <<'PY'
import re, subprocess, sys, glob, os, collections
diff = open(sys.argv[1]).read()
ITEM = re.compile(r'<(string|plurals|string-array) name="([^"]+)"[^>]*>(?:([^<]*)</string>)?')
module_of = lambda p: p.split('/src/main/')[0]

# Inventario actual: clave → módulos; valor → claves (por módulo)
keys, values = collections.defaultdict(set), collections.defaultdict(list)
for p in glob.glob('app/src/main/res/values/*.xml') + glob.glob('core/*/src/main/res/values/*.xml') + glob.glob('feature/*/src/main/res/values/*.xml'):
    for kind, k, v in ITEM.findall(open(p).read()):
        keys[k].add(module_of(p))
        if v: values[v].append((module_of(p), k))

refs = subprocess.run(['grep', '-rhoE', r'R\.(string|plurals|array)\.[a-z0-9_]+|@string/[a-z0-9_]+', 'app/src/main', 'core', 'feature',
                       '--include=*.kt', '--include=*.xml', '--exclude-dir=build'], capture_output=True, text=True).stdout
used = {re.split(r'[./]', r)[-1] for r in refs.split()}

ENGLISH = re.compile(r'\b(the|and|of|your|with|for|you|to|is|are|this|view|all|add|open|close|save|cancel|next|back|sign|log)\b', re.I)
cur = None
for line in diff.split('\n'):
    if line.startswith('+++ '): cur = line[6:]; continue
    if not line.startswith('+') or cur is None: continue
    body = line[1:]
    if re.match(r'\s*<!--', body):
        if not re.search(r'<!--\s*([PMOT]\d+|README|Comunes|Estados|Fechas)', body):
            print(f"🔎 FF-I18N-04 {cur} — comentario de bloque sin id de pantalla/componente (<!-- P05 Inicio -->): {body.strip()}")
        continue
    m = ITEM.search(body)
    if not m: continue
    kind, key, val = m.groups(); mod = module_of(cur)
    if key != 'app_name' and not re.fullmatch(r'[a-z][a-z0-9]*(_[a-z0-9]+)+', key):
        print(f"🟡 FF-I18N-04 {cur} — clave \"{key}\" fuera de <pantalla>_<descripcion>")
    others = sorted(keys[key] - {mod})
    if others:
        print(f"🟠 FF-I18N-04 {cur} — \"{key}\" ya existe en {', '.join(others)}: al fusionar recursos una pantalla mostrará el texto de la otra")
    if val:
        dups = [k for mm, k in values[val] if k != key and (mm == mod or mm == 'core/ui')]
        if dups: print(f"🟡 FF-I18N-04 {cur} — \"{key}\" repite el valor \"{val}\" de: {' '.join(dups[:3])} (reutiliza; si está en core:ui, usa esa)")
        if 'translatable="false"' not in body and ENGLISH.search(val):
            print(f"🔎 FF-I18N-05 {cur} — \"{key}\" parece estar en inglés: \"{val}\"")
        if re.search(r'Por pagar', val, re.I):
            print(f"🟡 FF-I18N-05 {cur} — \"{key}\": vocabulario de README §5 es \"Pendiente de pago\"")
        if kind == 'string' and re.search(r'%(\d\$)?d\s+\w+s\b', val):
            print(f"🔎 FF-I18N-05 {cur} — \"{key}\" = \"{val}\": ¿plurals?")
    if key not in used and key != 'app_name':
        print(f"🟡 FF-I18N-04 {cur} — \"{key}\" nueva sin uso")
PY
rm -f "$TMP_DIFF"
true
EOF
```

**Cómo interpretar:**
- **FF-I18N-01:** los datos de `object <X>Samples` (nombres de Mateo y Sofía, títulos de eventos de ejemplo) son datos, no textos de UI, mientras no haya capa de datos (TD-SK-002); tampoco cuentan los literales dentro de previews. Si un componente recibe el texto por parámetro (`label: String`), el literal estaría en el llamador. Un texto de UI nuevo va en el `strings.xml` **del módulo que lo usa** (CLAUDE.md §Textos), o en `core:ui` si lo comparten varios features.
- **FF-I18N-02:** "Sin concatenar": el orden de las palabras va en el recurso (`<string name="home_week_next">Próximo compromiso %1$s a las %2$s.</string>`). Unidades y sufijos ("s", "min", "MXN") también.
- **FF-I18N-03:** fechas solo con `DateLabels` de `core:ui` (`shortLabel()` → "Sáb 14 nov", `shortLabelWithYear()`, `longLabel()`, `LocalTime.label()` → "10:00 hrs", `timeRangeLabel`); si falta un formato, se agrega ahí con sus nombres de `dates.xml`, no en el feature. Los tiempos deportivos (`00:41.23`, `28:41`) usan `Locale.ROOT` porque solo son dígitos (`SwimTime.format()`, `RaceTime`). Dinero: `$1,250 MXN`, sin `.00` (README §7); hoy cada feature tiene su `grouped()`/`*_amount` (ver FF-DUP-03): no crees otra variante.
- **FF-I18N-04:** las claves llevan el prefijo de la pantalla o componente (`quick_add_title`, `swim_meet_*`, `agenda_prev_month`) y van en bloques con comentario (`<!-- P05 Inicio -->`, `<!-- O4 QuickAddSheet -->`). El 🟠 de colisión es real: con varios módulos de recursos, dos claves con el mismo nombre se fusionan en una y una de las pantallas muestra el texto equivocado.
- **FF-I18N-05:** toda la UI va en español de México (Figma trae títulos en inglés: se traducen) y usa solo las etiquetas de README §5. Cantidades con `plurals` ("1 medalla" / "4 medallas").
