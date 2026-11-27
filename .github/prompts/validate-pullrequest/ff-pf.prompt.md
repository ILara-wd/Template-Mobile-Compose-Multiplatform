<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde .claude/commands/validate-pullrequest/ff-pf.md. NO EDITAR A MANO. -->
---
mode: agent
description: 5.9 Revisiones de Recomposición y Rendimiento (FF-PF-XX)
---

## 5.9 Revisiones de Recomposición y Rendimiento (FF-PF-XX)

Estabilidad de parámetros y trabajo en composición (`03-standards.md §8`, ADR-003, PERF-01..05).

| ID | Regla | Severidad |
|---|---|---|
| FF-PF-01 | `items(...)`/`itemsIndexed(...)` sin `key =` en listas perezosas (PERF-03) | 🟠 |
| FF-PF-02 | `data class` nueva en `model/` (o usada como parámetro de composable) sin `@Immutable` (PERF-01) | 🟠 |
| FF-PF-03 | `@Immutable`/`@Stable` sobre una clase con `var`, `MutableList`, `ArrayList`, `MutableMap` (PERF-01) | 🔴 |
| FF-PF-04 | `collectAsState()` en lugar de `collectAsStateWithLifecycle()` (PERF-02) | 🟠 |
| FF-PF-05 | Trabajo pesado en composición sin `remember`: `DateTimeFormatter.ofPattern`, `Regex(`, `NumberFormat`, `Path()`, `.filter/.map/.sortedBy` sobre listas grandes (PERF-04) | 🟡 señal |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh

# FF-PF-01
added_main | grep -E '\b(items|itemsIndexed)\(' | grep -v 'key *=' | sed 's/^/🟠 FF-PF-01 /'

# FF-PF-02 / 03
for f in $PR_MAIN; do
  awk -v F="$f" '
    /^[[:space:]]*@(Immutable|Stable)/ {ann=NR; next}
    /data class [A-Z]/ {
      match($0, /data class [A-Za-z0-9_]+/); name=substr($0, RSTART+11, RLENGTH-11)
      annotated = (ann == NR-1); start=NR; body=""; depth=0; inc=1
    }
    inc { body = body $0 "\n"; o=gsub(/\(/,"("); c=gsub(/\)/,")"); depth+=o-c
          if (depth<=0 && NR>=start) { inc=0
            if (!annotated) print "PF02\t" F ":" start "\t" name
            else if (body ~ /(Mutable(List|Map|Set|State)|ArrayList|HashMap|[( ,]var )/) print "PF03\t" F ":" start "\t" name } }
    !/^[[:space:]]*@/ && !/data class/ {ann=0}' "$f"
done | while IFS=$'\t' read -r kind loc name; do
  f=${loc%%:*}; line=${loc##*:}
  added_lines "$f" | grep -q "^$f:$line:" || continue
  case $kind in
    PF02) case "$f" in */model/*) echo "🟠 FF-PF-02 $loc — data class $name sin @Immutable" ;;
                       *) echo "🔎 FF-PF-02 $loc — data class $name sin @Immutable (¿es parámetro de un composable? Actions con lambdas también conviene marcarlas)" ;; esac ;;
    PF03) echo "🔴 FF-PF-03 $loc — @Immutable/@Stable sobre $name con tipos mutables" ;;
  esac
done

# FF-PF-04
added_main | grep -E '\.collectAsState\(' | sed 's/^/🟠 FF-PF-04 /'

# FF-PF-05 (señal: confirmar que está en el cuerpo de un @Composable y fuera de remember)
added_main | grep -E '(DateTimeFormatter\.ofPattern|Regex\(|NumberFormat\.get|= *Path\(\)|\.(filter|sortedBy|sortedWith|groupBy)[[:space:]]*\{)' \
  | grep -vE 'remember|^[^:]*:[0-9]+:(private |internal )?val ' | grep -v "/model/\|/data/\|ViewModel.kt:" \
  | sed 's/^/🔎 FF-PF-05 señal /'
true
EOF
```

**Cómo interpretar:**
- **FF-PF-02:** la convención de Kensho es `@Immutable` en todo modelo de `model/` (ADR-003), aunque el compilador con *strong skipping* lo infiera: documenta el contrato.
- **FF-PF-05:** un formatter como `val` top-level (`monthFormatter` en `TkdPaymentsFormat.kt`) es el patrón correcto. Listas derivadas van como propiedad del `UiState` (`visibleTransactions`) o en `remember(lista) { … }`.
