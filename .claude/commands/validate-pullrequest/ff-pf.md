## 5.9 Revisiones de Recomposición y Rendimiento (FF-PF-XX)

Estabilidad de parámetros y trabajo en composición (README §6.2 puntos 1 y 3; `CLAUDE.md` §Arquitectura → Patrón de pantalla: modelos `@Immutable` en `<X>Models.kt`, holders `@Stable` en `<X>State.kt`). SportKid no tiene ViewModels ni `Flow`: el estado vive en holders `@Stable` con `mutableStateOf` y `rememberSaveable`, y los datos son `object <X>Samples`.

| ID | Regla | Severidad |
|---|---|---|
| FF-PF-01 | `items(...)`/`itemsIndexed(...)` sin `key =` en listas perezosas (`LazyColumn`, `LazyRow`, `LazyVerticalGrid`) | 🟠 |
| FF-PF-02 | `data class` nueva en `feature/*` o `core:ui` (modelos de UI, borradores como `TrainingDraft`) sin `@Immutable`; holder `<X>State` nuevo sin `@Stable` | 🟠 |
| FF-PF-03 | `@Immutable` sobre una clase con `var` o colecciones mutables; `@Stable` (holder) con `var` que **no** delega en `mutableStateOf`/`mutableIntStateOf`… o con `MutableList`/`ArrayList`/`HashMap` en lugar de estado de snapshot | 🔴 |
| FF-PF-04 | Estado de scroll o animación (`listState.firstVisibleItemIndex`, `scrollState.value`) leído directo en composición en lugar de `derivedStateOf` | 🟡 señal |
| FF-PF-05 | Trabajo pesado en composición sin `remember`: `DateTimeFormatter`, `Regex(`, `NumberFormat`, `Path()`, `.filter/.sortedBy/.groupBy` sobre listas | 🟡 señal |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
TMP_ADDED=$(mktemp); added_main > "$TMP_ADDED"
[ -s "$TMP_ADDED" ] || { echo "ℹ️ FF-PF: sin líneas nuevas en código productivo"; rm -f "$TMP_ADDED"; exit 0; }

# FF-PF-01 — listas perezosas sin key (la llamada puede ocupar varias líneas)
python3 - "$TMP_ADDED" <<'PY'
import re, sys, collections
added = collections.defaultdict(set)
for l in open(sys.argv[1]):
    if l.count(':') >= 2: f, n, _ = l.split(':', 2); added[f].add(int(n))
for f, lines in added.items():
    src = open(f).read()
    for m in re.finditer(r'(?<![\w.])(items|itemsIndexed)\s*\(', src):
        line = src.count('\n', 0, m.start()) + 1
        if line not in lines: continue
        depth, i = 0, m.end() - 1
        for i in range(m.end() - 1, len(src)):
            depth += {'(': 1, ')': -1}.get(src[i], 0)
            if depth == 0: break
        if 'key' not in src[m.end():i] and 'import ' not in src[src.rfind('\n', 0, m.start()):m.start()]:
            print(f"🟠 FF-PF-01 {f}:{line} — {m.group(1)}( sin key = (usa el id del modelo)")
PY

# FF-PF-02 / 03 — clases anotadas o data classes declaradas en líneas nuevas
python3 - "$TMP_ADDED" <<'PY'
import re, sys, collections
added = collections.defaultdict(set)
for l in open(sys.argv[1]):
    if l.count(':') >= 2: f, n, _ = l.split(':', 2); added[f].add(int(n))
DECL = re.compile(r'^\s*(?:(?:private|internal|public)\s+)?(data\s+)?class\s+(\w+)')
for f, lines in added.items():
    text = open(f).read().split('\n')
    for idx, line in enumerate(text):
        m = DECL.match(line)
        if not m or (idx + 1) not in lines: continue
        is_data, name = bool(m.group(1)), m.group(2)
        j, ann = idx - 1, ''
        while j >= 0 and (text[j].strip().startswith('@') or text[j].strip().startswith('*') or text[j].strip().startswith('/**')):
            if text[j].strip().startswith('@'): ann += text[j]
            j -= 1
        # cuerpo: constructor + llaves hasta cerrar
        depth, body, started = 0, [], False
        for k in range(idx, len(text)):
            body.append(text[k])
            for ch in text[k]:
                if ch in '({': depth += 1; started = True
                elif ch in ')}': depth -= 1
            if started and depth <= 0 and not text[k].rstrip().endswith(('(', '{', ',')) : break
        b = '\n'.join(body)
        loc = f"{f}:{idx + 1}"
        if '@Immutable' in ann:
            if re.search(r'(^|[(\s,])var\s|Mutable(List|Map|Set)\b|ArrayList|HashMap|mutableStateOf', b, re.M):
                print(f"🔴 FF-PF-03 {loc} — @Immutable sobre {name} con estado mutable")
        elif '@Stable' in ann:
            plain_vars = [v for v in re.findall(r'^\s*(?:private\s+|internal\s+)?var\s+(\w+)[^\n]*', b, re.M)
                          if not re.search(rf'var\s+{v}\b[^\n]*\bby\s+mutable\w*StateOf', b)]
            if plain_vars or re.search(r'\bMutable(List|Map|Set)<|ArrayList|HashMap', b):
                print(f"🔴 FF-PF-03 {loc} — @Stable {name}: var sin mutableStateOf o colección no observable ({', '.join(plain_vars) or 'Mutable*/ArrayList'})")
        elif not is_data and name.endswith('State') and f.startswith('feature/'):
            print(f"🟠 FF-PF-02 {loc} — holder {name} sin @Stable")
        elif is_data and (f.startswith('feature/') or f.startswith('core/ui/')):
            print(f"🟠 FF-PF-02 {loc} — data class {name} sin @Immutable")
        elif is_data:
            print(f"🔎 FF-PF-02 {loc} — data class {name} sin @Immutable (¿se usa como parámetro de un composable?)")
PY

# FF-PF-04 — estado de scroll leído en composición
grep -E 'firstVisibleItem(Index|ScrollOffset)|scrollState\.value|\.isScrollInProgress' "$TMP_ADDED" | grep -v 'derivedStateOf' \
  | sed 's/^/🔎 FF-PF-04 ¿derivedStateOf? (patrón: fabExpanded en AgendaScreen) /'

# FF-PF-05 — trabajo en composición (señal: confirmar que está en el cuerpo de un @Composable y fuera de remember)
grep -E '(DateTimeFormatter\.|Regex\(|NumberFormat\.get|= *Path\(\)|\.(filter|sortedBy|sortedWith|groupBy)[[:space:]]*(\{|\())' "$TMP_ADDED" \
  | grep -vE 'remember|^[^:]*:[0-9]+:(private |internal )?(val|fun) |on[A-Z][A-Za-z]* *= *\{' | grep -vE '(Models|State|Samples)\.kt:|/core/model/' \
  | sed 's/^/🔎 FF-PF-05 señal /'
rm -f "$TMP_ADDED"
true
EOF
```

**Cómo interpretar:**
- **FF-PF-02:** la convención de SportKid es `@Immutable` en todo modelo de UI de `<X>Models.kt` y en los borradores de formulario (`TrainingDraft`, `RaceDraft`), con `val` y `List` de solo lectura; el compilador con *strong skipping* lo infiere en parte, pero la anotación documenta el contrato. `core:model` es Kotlin puro (sin Compose): ahí no se puede anotar, así que sus tipos se limitan a `enum`/`val` (aviso 🔎).
- **FF-PF-03:** un holder `@Stable` (`AgendaState`, `NewTrainingState`, `CreateAthleteState`) expone `var x by mutableStateOf(…)` y colecciones como `Set`/`List` inmutables reasignadas (`sports = sports + sport`) o `mutableStateListOf`; un `var` plano o una `MutableList` rompen la promesa de `@Stable` y la UI no se entera del cambio. Un holder nuevo sin `@Stable` (hoy pasa con `CreateAthleteState`) se reporta en FF-PF-02.
- **FF-PF-04:** `val fabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }` (`AgendaScreen`) recompone solo cuando cambia el booleano, no en cada pixel de scroll.
- **FF-PF-05:** un formatter o regex va como `val` top-level o en `remember`; las listas derivadas (`events.sortedBy(...)`, filtros del holder) van en `remember(lista, filtro) { … }` o como función del holder (`AgendaState.matches`). Las funciones de `DateLabels` ya resuelven los formatos de fecha (FF-I18N-03).
