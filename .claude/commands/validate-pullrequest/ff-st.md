## 5.3 Revisiones de Estructura y Nombres (FF-ST-XX)

Verifica módulo, paquete, nombres, tamaño y limpieza del código (`CLAUDE.md` → Arquitectura, Convenciones → Componentes y Tamaño; README §6.1). Corre siempre.

| ID | Regla | Severidad |
|---|---|---|
| FF-ST-01 | Archivo nuevo fuera de su lugar: `package` que no coincide con la carpeta; en `core:designsystem` fuera de `theme/`, `component/` o `preview/`; módulo `feature/<x>` nuevo sin `include(":feature:<x>")` en `settings.gradle.kts` | 🟠 |
| FF-ST-02 | Nombre fuera de convención: átomo público de `core:designsystem/component` sin prefijo `Sk`; prefijo `Sk` en `core:ui` o en un feature (reservado a átomos sin dominio); holder `@Stable` que no se llama `<X>State`; `object` de datos de ejemplo que no se llama `<X>Samples`; preview no `private` o sin sufijo `Preview` | 🟡 |
| FF-ST-03 | Archivo nuevo > 400 líneas → 🟠 · archivo existente > 400 que crece > 10 % → 🟡 (TD-SK-005) · `@Composable` > 60 líneas → 🟡 (dividir en secciones `private`) | 🟠/🟡 |
| FF-ST-04 | 2+ componentes públicos no relacionados en un archivo de `core:designsystem/component` o `core:ui` | 🔎 señal |
| FF-ST-05 | Código comentado agregado (prohibido en `src/main`) | 🟡 |
| FF-ST-06 | Composable o clase pública nueva sin KDoc (una línea en español, con el nodo/nombre de Figma si aplica) | 🟡 |
| FF-ST-07 | Import comodín | 🟡 |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh

# FF-ST-01 — ubicación y paquete
for f in $PR_MAIN_NEW; do
  dir=$(dirname "$f" | sed -E 's#^(app|core/[a-z]+|feature/[a-z]+)/src/main/(java|kotlin)/##' | tr '/' '.')
  pkg=$(grep -m1 -E '^package ' "$f" | sed -E 's/^package +//; s/[[:space:]]*$//')
  [ "$pkg" != "$dir" ] && echo "🟠 FF-ST-01 $f — package '$pkg' no coincide con la carpeta '$dir'"
  case "$f" in
    core/designsystem/*/designsystem/theme/*|core/designsystem/*/designsystem/component/*|core/designsystem/*/designsystem/preview/*) ;;
    core/designsystem/*) echo "🟠 FF-ST-01 $f — core:designsystem solo admite theme/, component/ y preview/" ;;
    core/ui/src/main/kotlin/com/invex/sportkid/core/ui/*|core/model/src/main/kotlin/com/invex/sportkid/core/model/*) ;;
    app/src/main/java/com/invex/one/template/*) ;;
    feature/*) x=$(feature_of "$f")
               case "$f" in feature/$x/src/main/kotlin/com/invex/sportkid/feature/$x/*) ;;
                 *) echo "🟠 FF-ST-01 $f — debe vivir en com/invex/sportkid/feature/$x/" ;; esac ;;
    *) echo "🔎 FF-ST-01 $f — ¿módulo nuevo? debe registrarse en settings.gradle.kts y en CLAUDE.md (Módulos Gradle)" ;;
  esac
done
for x in $(printf '%s\n' $PR_MAIN_NEW | grep '^feature/' | cut -d/ -f2 | sort -u); do
  grep -q "include(\":feature:$x\")" settings.gradle.kts || echo "🟠 FF-ST-01 feature/$x no está en settings.gradle.kts"
done

# FF-ST-02 — nombres
added_lines 'core/designsystem/src/main/*/component/*.kt' | grep -E ':fun [A-Z][A-Za-z0-9]*\(' | grep -vE ':fun Sk' \
  | sed 's/^/🟡 FF-ST-02 átomo público sin prefijo Sk /'
added_lines 'core/ui/src/main/*.kt' 'feature/*/src/main/*.kt' 'app/src/main/*.kt' | grep -E ':(internal |private )?(fun|class|object) Sk[A-Z]' \
  | grep -v '/MainShell.kt:' | sed 's/^/🟡 FF-ST-02 prefijo Sk fuera de core:designsystem /'
for f in $PR_MAIN; do
  awk -v F="$f" '
    /^@Stable/ {s=NR; next}
    s && NR<=s+2 && /class / { if ($0 !~ /class [A-Z][A-Za-z0-9]*State([^A-Za-z0-9]|$)/) print "🟡 FF-ST-02 " F ":" NR " holder @Stable debe llamarse <X>State: " $0; s=0 }
    /^object [A-Z][A-Za-z0-9]*/ && /Sample/ && $0 !~ /^object [A-Z][A-Za-z0-9]*Samples([^A-Za-z0-9]|$)/ { print "🟡 FF-ST-02 " F ":" NR " datos de ejemplo: object <X>Samples: " $0 }
    /^@(Preview|SkPreviews)/ {p=1; next}
    p && /fun / { if ($0 !~ /private fun/ || $0 !~ /Preview\(/) print "🟡 FF-ST-02 " F ":" NR " preview debe ser private y terminar en Preview: " $0; p=0 }' "$f"
done

# FF-ST-03 — tamaño (líneas totales, como TD-SK-005)
for f in $PR_MAIN; do
  n=$(wc -l < "$f" | tr -d ' ')
  if is_new_file "$f"; then
    [ "$n" -gt 400 ] && echo "🟠 FF-ST-03 $f — archivo nuevo con $n líneas (> 400)"
  elif [ "$n" -gt 400 ]; then
    before=$(base_file "$f" | wc -l | tr -d ' ')
    [ "$before" -gt 0 ] && [ "$n" -gt $(( before + before / 10 )) ] && echo "🟡 FF-ST-03 $f — creció de $before a $n líneas (> 400 y > 10 %, TD-SK-005)"
  fi
  changed=" $(added_lines "$f" | cut -d: -f2 | tr '\n' ' ') "
  awk -v F="$f" -v C="$changed" '
    /^[[:space:]]*@Composable/ {pending=1; next}
    pending && /fun [A-Za-z]/ {match($0,/fun [A-Za-z0-9_]+/); name=substr($0,RSTART+4,RLENGTH-4); start=NR; depth=0; infn=1; pending=0; touched=0}
    infn { if (index(C, " " NR " ")) touched=1
           o=gsub(/\{/,"{"); c=gsub(/\}/,"}"); depth+=o-c; if (o>0) opened=1
           if (opened && depth<=0) { len=NR-start+1; if (len>60 && touched && name !~ /Preview$/) print "🟡 FF-ST-03 " F ":" start " — @Composable " name " con " len " líneas (> 60): divide en secciones private"; infn=0; opened=0 } }
    !/^[[:space:]]*@/ && !infn {pending=0}' "$f"
done

# FF-ST-04 — componentes públicos por archivo compartido
for f in $PR_COMPONENTS $PR_CORE_UI; do
  pubs=$(awk '/^@Composable/{c=1; next} c && /^fun [A-Z][A-Za-z0-9]*\(/{match($0,/fun [A-Za-z0-9]+/); print substr($0,RSTART+4,RLENGTH-4)} {if ($0 !~ /^@/) c=0}' "$f" | sort -u)
  n=$(printf '%s\n' "$pubs" | grep -c .)
  [ "$n" -gt 1 ] && echo "🔎 FF-ST-04 $f — $n composables públicos: $(echo $pubs) (¿misma familia, como SkButton/SkTonalButton o FormCard/FeeRow?)"
done

# FF-ST-05 — código comentado
added_main | grep -E ':[[:space:]]*//[[:space:]]*(val|var|fun|import|if|when|return|[A-Za-z]+\(|[A-Za-z.]+ *=|\})' \
  | sed 's/^/🟡 FF-ST-05 código comentado /'

# FF-ST-06 — KDoc en públicos nuevos (la línea previa, saltando anotaciones, debe cerrar un KDoc)
for f in $PR_MAIN; do
  added_lines "$f" | grep -E ':(fun [A-Z]|class [A-Z]|data class|interface [A-Z]|object [A-Z]|enum class|sealed (class|interface)|annotation class)' | while IFS= read -r l; do
    line=$(echo "$l" | cut -d: -f2)
    prev=$(awk -v L="$line" 'NR<L && $0 !~ /^[[:space:]]*@/ && $0 !~ /^[[:space:]]*\)?[[:space:]]*$/ {last=$0} NR==L{print last; exit}' "$f")
    echo "$prev" | grep -qE '\*/' || echo "🟡 FF-ST-06 sin KDoc $l"
  done
done

# FF-ST-07 — imports comodín
added_main | grep -E ':import [a-z0-9_.]+\.\*$' | sed 's/^/🟡 FF-ST-07 /'
true
EOF
```

**Cómo interpretar:**
- **FF-ST-01:** estructura de referencia en `CLAUDE.md` → Arquitectura: `:app` en `com.invex.one.template` (TD-SK-007), átomos en `core/designsystem/.../component/`, tokens en `.../theme/`, `@SkPreviews` en `.../preview/`, dominio de UI en `core/ui/.../core/ui/`, modelos puros en `core/model/`, cada feature en `com.invex.sportkid.feature.<x>` con `<X>Screen.kt`, `<X>State.kt` y `<X>Models.kt`. Un módulo nuevo también va en la tabla de módulos de `CLAUDE.md`.
- **FF-ST-02:** `Sk*` = átomo sin dominio en `core:designsystem` (`SkButton`, `SkPill`). Los nombres sin prefijo que ya documenta el README en ese módulo (`SectionHeader` M17, `TimelineItem` M20, `CopyableCodeChip` M23, `CountdownText` M24, `SettingsRow`/`SettingsSection` M27) son preexistentes: no se reportan si el PR no los agrega. Fuera de ese módulo, `core:ui` sin prefijo (`EventCard`, `SportTag`); piezas de una sola pantalla `private` en el feature. `SkBottomBar` en `app/MainShell.kt` es la excepción documentada (README O3).
- **FF-ST-03:** propón concretamente qué extraer: secciones `private` en el mismo archivo, un `<X>State.kt` para el holder, o un archivo hermano del mismo feature (`AgendaScreen.kt` → `MonthCalendar.kt`). Un PR que toca un archivo de TD-SK-005 no lo hace crecer (regla del boy scout).
- **FF-ST-04:** familias del mismo concepto en un archivo están permitidas (`SkButton.kt` con `SkButton`/`SkTonalButton`/`SkTextButton`; `FormParts.kt` con `FormCard`/`FeeRow`/`FormSaveBar`); dos componentes sin relación, no.
- **FF-ST-05:** comentarios que explican el *porqué* están bien; bloques de código desactivado no (`CLAUDE.md` → Prohibido en `src/main`).
- **FF-ST-06:** la primera línea del KDoc es la descripción que muestra el catálogo de UI (`/catalogo-ui-sportkid`); sin KDoc la pieza aparece como "Sin KDoc".
