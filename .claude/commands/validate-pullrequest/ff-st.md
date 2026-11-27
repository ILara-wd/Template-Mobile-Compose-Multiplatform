## 5.3 Revisiones de Estructura y Nombres (FF-ST-XX)

Verifica paquete, nombres, tamaño y limpieza del código (`03-standards.md §7`, ADR-001, STR-01..06). Corre siempre.

| ID | Regla | Severidad |
|---|---|---|
| FF-ST-01 | Archivo nuevo fuera de un paquete canónico (`ui/`, `ui/theme/`, un feature, `components/atoms|molecules/`, `model/`, `data/`, `core/`) (STR-01) | 🟠 |
| FF-ST-02 | Nombre fuera de convención: composable público sin prefijo `Tkd`, `*ViewModel`/`*UiState`/`*Actions`/`*Repository` sin `Tkd`, preview no `private` o sin sufijo `Preview` (STR-03) | 🟡 |
| FF-ST-03 | Archivo nuevo > 300 líneas de código → 🟠 · archivo existente > 300 que crece > 10 % → 🟡 · `@Composable` > 60 líneas → 🟡 (STR-02, CMP-08) | 🟠/🟡 |
| FF-ST-04 | 2+ componentes públicos no relacionados en un archivo de `components/` (STR-04) | 🟡 |
| FF-ST-05 | Código comentado agregado (STR-06) | 🟡 |
| FF-ST-06 | Composable o clase pública nueva sin KDoc (STR-05) | 🟡 |
| FF-ST-07 | Import comodín | 🟡 |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh

# FF-ST-01 — paquete canónico
for f in $PR_MAIN_NEW; do
  rel=${f#$APP_MAIN/}
  case "$rel" in
    MainActivity.kt|ui/TkdApp.kt|ui/theme/*|components/atoms/*|components/molecules/*|model/*|data/*|core/*) ;;
    login/*|signup/*|students/*|profile/*|checkin/*|payments/*|ui/present/*) ;;
    components/*) echo "🟠 FF-ST-01 $f — components/ solo admite atoms/ y molecules/" ;;
    */*) echo "🔎 FF-ST-01 $f — ¿feature nuevo? Confirmar paquete por feature (STR-01) y agregarlo a FEATURES en la librería" ;;
    *)   echo "🟠 FF-ST-01 $f — archivo en la raíz del paquete" ;;
  esac
done

# FF-ST-02 — nombres
added_main | grep -E ':(@Composable[[:space:]]+)?fun [A-Z][A-Za-z0-9]*\(' | grep -vE 'fun Tkd|private fun|internal fun' \
  | sed 's/^/🟡 FF-ST-02 composable público sin prefijo Tkd /'
added_main | grep -E ':(data )?class [A-Z][A-Za-z]*(ViewModel|UiState|Actions|Repository)\b' | grep -vE 'class (Fake)?Tkd' \
  | sed 's/^/🟡 FF-ST-02 nombre sin prefijo Tkd /'
for f in $PR_MAIN; do
  awk -v F="$f" '/^@Preview/{p=NR; next} p && /fun /{ if ($0 !~ /private fun/ || $0 !~ /Preview\(/) print "🟡 FF-ST-02 " F ":" NR " preview debe ser private y terminar en Preview: " $0; p=0 }' "$f"
done

# FF-ST-03 — tamaño
for f in $PR_MAIN; do
  n=$(count_code_lines "$f")
  if is_new_file "$f"; then
    [ "$n" -gt 300 ] && echo "🟠 FF-ST-03 $f — archivo nuevo con $n líneas de código (> 300)"
  elif [ "$n" -gt 300 ]; then
    before=$(git show "$BASE_REF:$f" 2>/dev/null > /tmp/kensho-st-before.kt && count_code_lines /tmp/kensho-st-before.kt); rm -f /tmp/kensho-st-before.kt
    [ -n "$before" ] && [ "$n" -gt $(( before + before / 10 )) ] && echo "🟡 FF-ST-03 $f — creció de $before a $n líneas de código (> 300 y > 10 %)"
  fi
  awk -v F="$f" '
    /^[[:space:]]*@Composable/ {pending=1; next}
    pending && /fun [A-Za-z]/ {match($0,/fun [A-Za-z0-9_]+/); name=substr($0,RSTART+4,RLENGTH-4); start=NR; depth=0; infn=1; pending=0}
    infn { o=gsub(/\{/,"{"); c=gsub(/\}/,"}"); depth+=o-c; if (o>0) opened=1
           if (opened && depth<=0) { len=NR-start+1; if (len>60) print "🟡 FF-ST-03 " F ":" start " — @Composable " name " con " len " líneas (> 60, CMP-08)"; infn=0; opened=0 } }
    !/^[[:space:]]*@/ && !infn {pending=0}' "$f"
done

# FF-ST-04 — componentes públicos por archivo en components/
for f in $PR_COMPONENTS; do
  pubs=$(grep -oE '^fun Tkd[A-Za-z0-9]*' "$f" | sed 's/fun //' | sort -u)
  n=$(printf '%s\n' "$pubs" | grep -c .)
  [ "$n" -gt 1 ] && echo "🔎 FF-ST-04 $f — $n componentes públicos: $(echo $pubs) (¿misma familia, como TkdTiles/TkdFilterChips?)"
done

# FF-ST-05 — código comentado
added_main | grep -E ':[[:space:]]*//[[:space:]]*(val|var|fun|import|if|when|return|[A-Za-z]+\(|[A-Za-z.]+ *=|\})' \
  | sed 's/^/🟡 FF-ST-05 código comentado /'

# FF-ST-06 — KDoc en públicos nuevos (línea anterior debe cerrar un KDoc o ser anotación precedida de KDoc)
for f in $PR_MAIN; do
  added_lines "$f" | grep -E ':(@Composable )?(fun Tkd|class Tkd|data class Tkd|interface Tkd|enum class|sealed (class|interface))' | while IFS= read -r l; do
    line=$(echo "$l" | cut -d: -f2)
    prev=$(awk -v L="$line" 'NR<L && $0 !~ /^@/ {last=$0} NR==L{print last; exit}' "$f")
    echo "$prev" | grep -qE '\*/' || echo "🟡 FF-ST-06 sin KDoc $l"
  done
done

# FF-ST-07 — imports comodín
added_main | grep -E ':import [a-z0-9_.]+\.\*$' | sed 's/^/🟡 FF-ST-07 /'
true
EOF
```

**Cómo interpretar:**
- **FF-ST-03:** propón concretamente qué extraer (p. ej. `Tkd<X>Sections.kt` → `Tkd<X>HeaderSection.kt` + `Tkd<X>ListSection.kt` en el mismo paquete; subcomposables `private`).
- **FF-ST-04:** familias del mismo concepto en un archivo están permitidas (`TkdTiles.kt`, `TkdFilterChips.kt`); dos componentes sin relación, no.
- **FF-ST-05:** comentarios que explican el *porqué* están bien; bloques de código desactivado no (STR-06, ver `MainActivity.kt` en TD-KEN-003).
