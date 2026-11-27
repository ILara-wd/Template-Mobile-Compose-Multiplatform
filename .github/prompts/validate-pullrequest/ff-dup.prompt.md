<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde .claude/commands/validate-pullrequest/ff-dup.md. NO EDITAR A MANO. -->
---
mode: agent
description: 5.10 Revisiones de Duplicación y Reinvención (FF-DUP-XX)
---

## 5.10 Revisiones de Duplicación y Reinvención (FF-DUP-XX)

El sistema de diseño existe para no repetir UI: se busca antes de crear (`/catalogo-ui-sportkid consultar <pieza>`), los átomos sin dominio viven en `core:designsystem` (`Sk*`), las piezas con dominio en `core:ui`, y **una pieza que necesitan dos features sube a `core:ui`** porque los features no dependen entre sí (`CLAUDE.md` §Arquitectura → Reglas de dependencia y §Convenciones → Componentes y Estados de dominio; README §1.6–§3 y §6.1). Esta familia detecta copy-paste en el diff y componentes, helpers, mapeos o recursos que el cambio reimplementa cuando ya existen. Corre siempre.

| ID | Regla | Severidad |
|---|---|---|
| FF-DUP-01 | Bloque de ≥ 6 líneas idéntico repetido dentro del diff (previews incluidas) | 🟠 |
| FF-DUP-02 | Composable nuevo con el **mismo nombre** que uno de otro módulo (🟠 señal), o cuyo rol coincide con un átomo/pieza de `core` (`*Chip*`, `*Card*`, `*Badge*`, `*Button*`, `*Header*`, `*Pill*`…) (🔎) | 🟠 señal |
| FF-DUP-03 | Helper nuevo que ya existe en otro módulo (mismo nombre) o que reimplementa un formato resuelto: fechas (`DateLabels`), dinero (`grouped()`/`*_amount`), tiempos (`SwimTime`, `RaceTime`) | 🟠 señal |
| FF-DUP-04 | Presentación de un `enum` de `core:model` (`Sport`, `MedalResult`, `Belt`, `PaymentStatus`, `SwimStyleStatus`, `FamilyRole`) mapeada en un feature o en `:app` cuando ya existe en `core:ui`; misma rama `when` repetida en varias funciones | 🟡 |
| FF-DUP-05 | Token, string o drawable nuevo que duplica uno existente (ver FF-THM, FF-I18N-04, FF-RES-03) | 🟡 |
| FF-DUP-06 | Componente público nuevo de `core` sin fila en README §1.6/§2/§3, o pantalla nueva (`<X>Screen`) sin entrada en `docs/ui-catalogo/pantallas.json` (el catálogo no la mostrará) | 🟡 |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
ADDED=$(added_main)
[ -z "$ADDED" ] && { echo "ℹ️ FF-DUP: sin líneas nuevas en código productivo"; exit 0; }
CORE_KT=(core/designsystem/src/main core/ui/src/main)

# FF-DUP-01 — bloques repetidos en las líneas agregadas (ventana de 6 líneas normalizadas)
printf '%s\n' "$ADDED" | python3 -c '
import sys, collections
rows = [l.rstrip("\n").split(":", 2) for l in sys.stdin if l.count(":") >= 2]
norm = [(f, n, " ".join(c.split())) for f, n, c in rows]
W, seen = 6, collections.defaultdict(list)
for i in range(len(norm) - W + 1):
    win = norm[i:i+W]
    if len({f for f, _, _ in win}) > 1: continue
    if any(int(win[k+1][1]) != int(win[k][1]) + 1 for k in range(W - 1)): continue
    if any(c.startswith(("import ", "package ")) for _, _, c in win): continue
    text = "\n".join(c for _, _, c in win)
    if sum(len(c) for _, _, c in win) < 120 or all(c in ("", "}", ")", "{", "),") for _, _, c in win): continue
    seen[text].append((win[0][0], int(win[0][1])))
dups = sorted(tuple(locs) for locs in seen.values() if len(locs) > 1)
merged = []
for locs in dups:
    if merged:
        prev, length = merged[-1]
        if len(prev) == len(locs) and all(a[0] == b[0] and b[1] - a[1] == length - W + 1 for a, b in zip(prev, locs)):
            merged[-1] = (prev, length + 1); continue
    merged.append((locs, W))
for locs, length in merged:
    print(f"🟠 FF-DUP-01 bloque de ~{length} líneas repetido {len(locs)} veces: " + ", ".join(f"{f}:{n}" for f, n in locs[:4]))
'

# FF-DUP-02 — composables nuevos: mismo nombre en otro módulo, o rol que ya cubre core
printf '%s\n' "$ADDED" | python3 -c '
import re, sys, glob, collections
mod = lambda p: p.split("/src/main/")[0]
words = lambda n: set(re.findall(r"[A-Z][a-z0-9]*", n[2:] if n.startswith("Sk") else n))
defs = collections.defaultdict(set)
for p in glob.glob("app/src/main/**/*.kt", recursive=True) + glob.glob("core/*/src/main/**/*.kt", recursive=True) + glob.glob("feature/*/src/main/**/*.kt", recursive=True):
    for n in re.findall(r"^(?:internal |private )?fun ([A-Z]\w*)\(", open(p).read(), re.M): defs[n].add(p)
core = {n for n, ps in defs.items() if not n.endswith("Preview") and any(q.startswith(("core/designsystem/", "core/ui/")) for q in ps)}
ROLE = {"Card", "Chip", "Badge", "Button", "Avatar", "Pill", "Tile", "Header", "Title", "Field", "Banner", "Switch", "Progress", "Callout", "Picker", "Selector", "Tag", "Sheet", "Fab"}
seen = set()
for l in sys.stdin:
    m = re.match(r"^([^:]+):(\d+):(?:internal |private )?fun ([A-Z]\w*)\(", l)
    if not m or m.group(3).endswith("Preview") or m.group(3) in seen: continue
    f, comp = m.group(1), m.group(3); seen.add(comp)
    same = sorted(q for q in defs[comp] if mod(q) != mod(f))
    if same: print(f"🟠 FF-DUP-02 {comp} ({f}) — ya existe con el mismo nombre en: {chr(32).join(same[:3])} (¿misma pieza? → core:ui)")
    body = open(f).read()
    for role in words(comp) & ROLE:
        atom = "Sk" + role
        if atom in core and atom != comp and atom + "(" not in body:
            print(f"🔎 FF-DUP-02 {comp} ({f}) — no usa {atom} de core:designsystem: ¿reinventa el átomo?")
    near = sorted(n for n in core if n != comp and len(words(n) & words(comp)) >= 2)
    if near: print(f"🔎 FF-DUP-02 {comp} ({f}) ↔ en core: {chr(32).join(near[:5])}")
'

# FF-DUP-03 — helpers top-level nuevos cuyo nombre ya existe en otro módulo
printf '%s\n' "$ADDED" | grep -E '^[^:]+:[0-9]+:(private |internal )?fun ([A-Za-z<>]+\.)?[a-z][A-Za-z0-9_]*\(' | while IFS= read -r l; do
  f=${l%%:*}; mod=$(module_of "$f")
  name=$(echo "$l" | grep -oE 'fun ([A-Za-z<>]+\.)?[a-z][A-Za-z0-9_]*' | head -1 | sed -E 's/fun ([A-Za-z<>]+\.)?//')
  other=$(grep -rlE "^(private |internal )?fun ([A-Za-z<>]+\.)?$name\(" --include='*.kt' "${MAIN_SRC[@]}" 2>/dev/null \
          | while read -r o; do [ "$(module_of "$o")" != "$mod" ] && echo "$o"; done | head -3 | tr '\n' ' ')
  [ -n "$other" ] && echo "🔎 FF-DUP-03 $l ↔ ya existe en: $other"
done
# formatos ya resueltos (fechas → DateLabels; dinero → grouped()/*_amount; tiempos → SwimTime/RaceTime)
printf '%s\n' "$ADDED" | grep -vE 'DateLabels\.kt:' \
  | grep -E 'DateTimeFormatter|SimpleDateFormat|getDisplayName\(|"%,d"|%,\.2f|NumberFormat|DecimalFormat|MXN|"%0?2d:%02d' \
  | sed 's/^/🔎 FF-DUP-03 ¿formato ya resuelto en DateLabels (core:ui), grouped()\/*_amount o SwimTime\/RaceTime? /'

# FF-DUP-04 — presentación de enums de core:model fuera de core:ui
printf '%s\n' "$ADDED" | grep -vE '^core/' \
  | grep -E '(Sport|MedalResult|Belt|PaymentStatus|SwimStyleStatus|FamilyRole)\.[A-Z_]+ *(,[^-]*)?->.*(R\.(string|drawable)|[Cc]olor|colors\.|sportColors)' \
  | sed 's/^/🟡 FF-DUP-04 mapeo de enum de dominio a texto\/icono\/color fuera de core:ui — ¿existe Sport.colors()\/iconRes\/labelRes, MedalResult.colors()\/labelRes()\/iconRes, Belt.labelRes()\/color(), FamilyRole.labelRes, PaymentStatusChip? /'
printf '%s\n' "$ADDED" | grep -E '^[^:]+:[0-9]+:[[:space:]]+[A-Za-z!].*->' \
  | awk -F: '{c=$0; sub(/^[^:]*:[^:]*:/,"",c); gsub(/^[ \t]+/,"",c); print c "\t" $1 ":" $2}' \
  | sort | awk -F'\t' '{n[$1]++; loc[$1]=loc[$1] " " $2} END {for (k in n) if (n[k]>1 && length(k)>25) print "🟡 FF-DUP-04 rama repetida \"" k "\" en" loc[k]}'

# FF-DUP-06 — propagación al README y al catálogo
for f in $PR_MAIN; do
  case "$f" in core/designsystem/*/component/*|core/ui/*) ;; *) continue ;; esac
  added_lines "$f" | grep -oE ':(@Composable )?fun [A-Z][A-Za-z0-9]*\(' | sed -E 's/.*fun ([A-Za-z0-9]+)\(/\1/' | while read -r comp; do
    case "$comp" in *Preview) continue ;; esac
    grep -q "\`$comp\`" README.md || echo "🟡 FF-DUP-06 $comp ($f) — componente público sin fila en README §1.6/§2/§3"
  done
done
for f in $PR_SCREENS; do
  for scr in $(added_lines "$f" | grep -oE ':fun [A-Z][A-Za-z0-9]*Screen\(' | sed -E 's/.*fun ([A-Za-z0-9]+)\(/\1/'); do
    grep -q "\"$scr\"" docs/ui-catalogo/pantallas.json || echo "🟡 FF-DUP-06 $scr ($f) — pantalla sin entrada en docs/ui-catalogo/pantallas.json"
  done
done
true
EOF
```

**Cómo interpretar:**
- **FF-DUP-01** bloquea: extrae el bloque a una `private fun`/subcomposable o, en previews, a datos de `<X>Samples`.
- **FF-DUP-02/03 son señales:** abre el componente o helper existente. Si hace lo mismo (o lo haría con un parámetro nuevo al final y con default), la corrección es **reutilizarlo o extenderlo** sin romper sus usos (`/catalogo-ui-sportkid consultar`: Reutilizar → Extender → Subir a core:ui → Local → Nuevo átomo). Ejemplos ya resueltos que sirven para cualquier deporte: `FormCard`, `FeeRow`, `FormDateTimeFields`, `FormSaveBar`, `PhotoUploadArea(onCamera)`, `AthleteSportHeader`. Si son distintos, no lo reportes.
- **Mismo nombre en dos features** (🟠): como los features no se importan entre sí, la corrección no es copiar sino subir la pieza a `core:ui` (o a `core:designsystem` si no tiene dominio). Hoy ya hay copias privadas de `SectionTitle`/`SectionHeader` en home, tkd, swimming, athletics y expenses, y `RaceCard` en swimming y athletics: no agregues otra; si el PR toca una, es buen momento para unificarlas (README M17 describe `SectionHeader` en `core:designsystem`).
- **FF-DUP-03:** las fechas salen de `DateLabels` (`core:ui`). El formato de dinero hoy está repetido por feature (`grouped()` en `HomeModels`/`ExpensesModels`, `*_amount` en `AgendaModels`/`SwimModels`/`TkdModels`/`AthleticsModels`, más un `"%,.2f"` en `AthleteProfileScreen`): no agregues una variante más; si el PR necesita formatear dinero en otro módulo, propón subir un helper único a `core:ui`.
- **FF-DUP-04:** "Estados de dominio: un `enum` en `core:model` y su presentación (texto, icono, color) una sola vez en `core:ui`" (CLAUDE.md). Un `when (sport)` en un feature que devuelve el color o el icono del deporte reinventa `Sport.colors()`/`Sport.iconRes`.
- **FF-DUP-06:** el catálogo (`docs/ui-catalogo/index.html`) se genera desde el código, el README y `pantallas.json`; sin la fila del README no muestra el código (M9, O21…) y sin la entrada de `pantallas.json` la pantalla no se captura. Corrección: fila en README y `/catalogo-ui-sportkid actualizar <id>`.
- Cita siempre **las dos ubicaciones** y la refactorización concreta.
