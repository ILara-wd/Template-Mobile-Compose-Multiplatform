<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde .claude/commands/validate-pullrequest/ff-cmp.md. NO EDITAR A MANO. -->
---
mode: agent
description: 5.4 Revisiones de Composables (FF-CMP-XX)
---

## 5.4 Revisiones de Composables (FF-CMP-XX)

Verifica el contrato de composables (`03-standards.md §2`, ADR-002/005/010): `modifier` canónico, componentes de `components/` sin estado de negocio, átomos agnósticos del dominio, secciones con visibilidad mínima, variantes con estilo o slot, previews y que el ViewModel no baje de `Screen`.

| ID | Regla | Severidad |
|---|---|---|
| FF-CMP-01 | Composable público (o `internal` de sección) sin `modifier: Modifier = Modifier` (CMP-01) | 🟠 |
| FF-CMP-02 | `modifier` fuera de posición (tras opcionales con default o slots), sin default, no aplicado a la raíz o aplicado más de una vez (CMP-01) | 🟠 |
| FF-CMP-03 | Estado de negocio en `components/`: `remember { mutableStateOf(...) }` que guarda un valor que el llamador debería controlar (CMP-02) — **señal a confirmar** | 🟠 si se confirma |
| FF-CMP-04 | Átomo que importa `model/` o `data/`; molécula que importa `data/` fuera de previews, un `UiState` o un ViewModel (CMP-03) | 🟠 |
| FF-CMP-05 | 3+ parámetros `Boolean` de apariencia/visibilidad en un componente (CMP-06) | 🟠 nuevo · 🟡 existente |
| FF-CMP-06 | `MutableState`/`MutableStateFlow`/`SnapshotStateList` como parámetro de un composable | 🟠 |
| FF-CMP-07 | Componente nuevo en `components/` con nombre/rol parecido a uno existente (CMP-07) — ver FF-DUP-02 | 🟠 señal |
| FF-CMP-09..13 | Superficie de composables: sin preview, preview pública, VM/NavController fuera de Screen, sección pública en feature, firma incompatible de componente compartido — **script** `validate-pullrequest-api.py` | 🟠/🔴/🟡 |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
TARGETS=$(printf '%s\n' $PR_COMPONENTS $PR_SCREENS | sort -u | grep . || true)
[ -z "$TARGETS" ] && { echo "ℹ️ FF-CMP: sin componentes ni pantallas en el cambio"; exit 0; }

python3 - $TARGETS <<'PY'
import os, re, subprocess, sys
base = os.environ.get("BASE_REF") or f'origin/{os.environ.get("BASE_BRANCH", "develop")}'
if subprocess.run(["git", "rev-parse", "--verify", "--quiet", base], capture_output=True).returncode:
    base = os.environ.get("BASE_BRANCH", "develop")

def signatures(src):
    for m in re.finditer(r'@Composable\s+(?:(public|internal|private)\s+)?fun\s+(?:[A-Za-z_.]+\.)?([A-Z]\w*)\s*\(', src):
        if m.group(1) == 'private' or '@Preview' in src[max(0, m.start()-160):m.start()]:
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
        yield src[:m.start()].count('\n')+1, m.group(2), [p for p in params if p], i

for f in sys.argv[1:]:
    src = open(f).read()
    new = subprocess.run(['git','cat-file','-e',f'{base}:{f}'], capture_output=True).returncode != 0
    for line, name, params, body_start in signatures(src):
        loc = f"{f}:{line} {name}"
        if name.endswith('Screen'):
            continue   # el Screen se revisa en FF-ARC
        mod_idx = next((k for k,p in enumerate(params) if re.match(r'modifier\s*:\s*Modifier\b', p)), None)
        if mod_idx is None:
            print(f"🟠 FF-CMP-01 {loc} — sin `modifier: Modifier = Modifier`")
        else:
            if not re.search(r'=\s*Modifier\s*$', params[mod_idx]):
                print(f"🟠 FF-CMP-02 {loc} — modifier sin default `= Modifier`")
            before = params[:mod_idx]
            if any('=' in p for p in before):
                print(f"🟠 FF-CMP-02 {loc} — opcionales antes de `modifier`: {[p.split(':')[0] for p in before if '=' in p]}")
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
            print(f"{'🟠' if new else '🟡'} FF-CMP-05 {loc} — {len(bools)} Boolean: {[b.split(':')[0].strip() for b in bools]} → estilo con defaults o slot")
        for p in params:
            if re.search(r':\s*(MutableState|MutableStateFlow|SnapshotStateList|SnapshotStateMap)\b', p):
                print(f"🟠 FF-CMP-06 {loc} — parámetro mutable: {p}")
PY

# FF-CMP-03 — estado interno en components/ (señal)
for f in $PR_COMPONENTS; do
  added_lines "$f" | grep -E 'remember(Saveable)?[[:space:]]*(\([^)]*\))?[[:space:]]*\{[[:space:]]*mutableState(Of|ListOf|MapOf)' \
    | sed 's/^/🔎 FF-CMP-03 señal (¿estado visual o de negocio?) /'
done

# FF-CMP-04 — dependencias de átomos y moléculas
for f in $(printf '%s\n' $PR_COMPONENTS | grep '/atoms/'); do
  added_lines "$f" | grep -E ':import dev\.treecode\.kensho\.(model|data|login|signup|students|profile|checkin|payments|ui\.present)\.' \
    | sed 's/^/🟠 FF-CMP-04 átomo acoplado al dominio /'
done
for f in $(printf '%s\n' $PR_COMPONENTS | grep '/molecules/'); do
  added_lines "$f" | grep -E ':import dev\.treecode\.kensho\.(login|signup|students|profile|checkin|payments|ui\.present)\.|ViewModel\b|UiState\b' \
    | sed 's/^/🟠 FF-CMP-04 molécula acoplada a una pantalla /'
  added_lines "$f" | grep -E ':import dev\.treecode\.kensho\.data\.' \
    | sed 's/^/🔎 FF-CMP-04 data\/ en una molécula — permitido solo dentro de @Preview /'
done
EOF

# FF-CMP-09..13 — superficie de composables (script)
BASE_BRANCH="$BASE_BRANCH" python3 .claude/scripts/validate-pullrequest-api.py
```

**Cómo confirmar FF-CMP-03:**
- ✅ Estado **visual** permitido: animaciones, expandido/colapsado, foco, `InteractionSource`, `TextLayoutResult` (como en `TkdTermsConsent`), visibilidad de contraseña.
- ❌ Estado de **negocio**: el texto ingresado, el ítem seleccionado, el método de pago, un valor que la pantalla necesita leer → `value` + `onValueChange` hoisteados al VM.
- Copiar un parámetro a `remember { mutableStateOf(param) }` duplica la fuente de verdad: también es FF-CMP-03.

**FF-CMP-05:** los `Boolean` de estado hoisteado (`enabled`, `checked`, `selected`, `isError`) son legítimos. La señal son flags de apariencia (`showIcon`, `compact`, `outlined`) → objeto de estilo con defaults (`TkdInputFieldStyle`/`TkdInputFieldDefaults.Note`) o slot `(@Composable () -> Unit)?`.

**FF-CMP-09:** el `Screen` se considera documentado si su `Content` tiene preview. Cada `Content` debe tener una preview por estado relevante construida con la *Fake* del repositorio, `widthDp = 390`.
