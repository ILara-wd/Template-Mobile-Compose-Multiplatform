<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde .claude/commands/validate-pullrequest/ff-dup.md. NO EDITAR A MANO. -->
---
mode: agent
description: 5.10 Revisiones de Duplicación y Reinvención (FF-DUP-XX)
---

## 5.10 Revisiones de Duplicación y Reinvención (FF-DUP-XX)

El sistema de diseño `Tkd*` existe para no repetir UI (`03-standards.md` CMP-07, THM-04, ADR-005). Esta familia detecta copy-paste en el diff y componentes, helpers o tokens que el cambio reimplementa cuando ya existen en `components/`, `ui/theme/`, `core/` o los `*Format.kt`. Corre siempre.

| ID | Regla | Severidad |
|---|---|---|
| FF-DUP-01 | Bloque de ≥ 6 líneas idéntico repetido dentro del diff (previews incluidas) | 🟠 |
| FF-DUP-02 | Composable nuevo cuyo nombre/rol coincide con uno existente (`*Chip*`, `*Card*`, `*Badge*`, `*Button*`, `*Avatar*`, `*Pill*`…) | 🟠 señal |
| FF-DUP-03 | Helper nuevo que ya existe (formateo de moneda/fecha, validación, `Modifier.*`) | 🟠 señal |
| FF-DUP-04 | Mismo mapeo `when` (estado → color/texto/icono) repetido en varias funciones | 🟡 |
| FF-DUP-05 | Token, string o drawable nuevo que duplica uno existente (ver también FF-THM-04, FF-I18N-04, FF-RES-03) | 🟡 |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh

# FF-DUP-01 — bloques repetidos en las líneas agregadas (ventana de 6 líneas normalizadas)
added_lines "$APP_MAIN/*.kt" | python3 -c '
import sys, collections
rows = [l.rstrip("\n").split(":", 2) for l in sys.stdin if l.count(":") >= 2]
norm = [(f, n, " ".join(c.split())) for f, n, c in rows]
W, seen = 6, collections.defaultdict(list)
for i in range(len(norm) - W + 1):
    win = norm[i:i+W]
    if len({f for f, _, _ in win}) > 1: continue
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

# FF-DUP-02 — composables nuevos con nombre parecido a componentes existentes
added_main | grep -oE '^[^:]+:[0-9]+:(internal |private )?fun Tkd[A-Za-z0-9]+' | while IFS= read -r l; do
  f=${l%%:*}; comp=$(echo "$l" | grep -oE 'Tkd[A-Za-z0-9]+$')
  for token in $(echo "${comp#Tkd}" | sed -E 's/([a-z])([A-Z])/\1 \2/g'); do
    case "$token" in Card|Chip|Badge|Button|Avatar|Pill|Tile|Row|Header|Field|Bar|Banner|Switch|Progress|Divider|Fab|Ring|Dot) ;; *) continue ;; esac
    hits=$(grep -rlE "^fun Tkd[A-Za-z]*$token[A-Za-z]*\(" "$APP_MAIN/components" 2>/dev/null | grep -vxF "$f" | head -4)
    [ -n "$hits" ] && echo "🔎 FF-DUP-02 $comp ($f) ↔ componentes con '$token': $(echo $hits | xargs -n1 basename | tr '\n' ' ')"
  done
done | sort -u

# FF-DUP-03 — helpers top-level nuevos cuyo nombre ya existe
added_main | grep -E ':(private |internal )?fun ([A-Za-z]+\.)?[a-z][A-Za-z0-9_]*\(' | grep -vE 'fun preview[A-Z]?[A-Za-z]*\(' | while IFS= read -r l; do
  f=${l%%:*}; name=$(echo "$l" | grep -oE 'fun ([A-Za-z]+\.)?[a-z][A-Za-z0-9_]*' | sed -E 's/fun ([A-Za-z]+\.)?//')
  other=$(grep -rlE "fun ([A-Za-z]+\.)?$name\(" "$APP_MAIN" | grep -vxF "$f" | head -3)
  [ -n "$other" ] && echo "🔎 FF-DUP-03 $l ↔ ya existe en: $(echo $other | tr '\n' ' ')"
done
added_main | grep -E 'NumberFormat|DecimalFormat|"\$%|/ *100(\.0)?\b.*(USD|\$)|Regex\(".*@' \
  | grep -v "Format.kt:" | sed 's/^/🔎 FF-DUP-03 ¿formateo\/validación ya resuelto en *Format.kt o core\/Validate? /'

# FF-DUP-04 — mismas ramas when en varias funciones del diff
added_main | grep -E '^\S+:[0-9]+:\s+[A-Za-z!].*->' | awk -F: '{c=$0; sub(/^[^:]*:[^:]*:/,"",c); gsub(/^[ \t]+/,"",c); print c "\t" $1 ":" $2}' \
  | sort | awk -F'\t' '{n[$1]++; loc[$1]=loc[$1] " " $2} END {for (k in n) if (n[k]>1 && length(k)>25) print "🟡 FF-DUP-04 rama repetida \"" k "\" en" loc[k]}'
true
EOF
```

**Cómo interpretar:**
- **FF-DUP-01** bloquea: extrae el bloque a una `private fun`/subcomposable o, en previews, a un estado de ejemplo compartido (`private fun previewState(...)` como en `TkdPaymentDetailScreen.kt`).
- **FF-DUP-02/03 son señales:** abre el componente/helper existente. Si hace lo mismo (o lo haría con un parámetro nuevo con default), la corrección es **reutilizarlo o extenderlo** sin romper sus usos (CMP-07), no crear uno paralelo. Si son distintos, no lo reportes.
- Cita siempre **las dos ubicaciones** y la refactorización concreta.
- Si dos features necesitan el mismo helper (p. ej. formateo de moneda de `payments/TkdPaymentsFormat.kt`), la corrección es moverlo a un paquete común, no copiarlo ni importarlo entre features (ARC-05).
