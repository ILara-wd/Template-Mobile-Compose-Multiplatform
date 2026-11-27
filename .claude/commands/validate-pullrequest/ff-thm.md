## 5.5 Revisiones de Tokens de Diseño (FF-THM-XX)

Verifica que color, espaciado, tipografía y forma salgan de los tokens de SportKid (`CLAUDE.md` → Convenciones → Tokens; README §1.1–§1.4 y §6.2). La **única** casa de literales es `core/designsystem/.../theme/` (`Color.kt`, `SportColors.kt`, `Type.kt`, `Shape.kt`, `Spacing.kt`, `AppPalette.kt`). En código se consume así:

| Qué | Token |
|---|---|
| Color que cambia con el tema | `MaterialTheme.colorScheme.*` |
| Color con significado (deporte, medalla, estado, hijo, cinta) — **no** cambia con el tema | `LocalSportColors.current` / `SportKidTheme.sportColors`; en `core:ui`: `Sport.colors()`, `MedalResult.colors()`, `Belt.color()` |
| Espaciado | `SportKidTheme.spacing` / `LocalSpacing.current` (`xs 4 · sm 8 · md 12 · lg 16 · xl 24 · xxl 32`, `screenMargin`, `cardPadding`, `cardGap`, `sectionGap`) |
| Medidas fijas y elevación | `SkDimens` (`minTouchTarget`, `sportAccentBar`, `iconTile`…), `SkElevation` |
| Texto | `MaterialTheme.typography.*` + extensiones `metric` (tiempos, montos) y `brand` |
| Forma | `MaterialTheme.shapes.*` + `full` y `hero` |

| ID | Regla | Severidad |
|---|---|---|
| FF-THM-01 | `Color(0x…)` o `Color(r, g, b)` fuera de `theme/` (también en previews) → 🟠 archivo nuevo · 🟡 archivo tocado. `Color.White/Black/Gray…` → 🟡 (usa el rol `on*` del esquema). Permitidos `Color.Unspecified` y `Color.Transparent` | 🟠/🟡 |
| FF-THM-02 | `N.dp` de la escala (4, 8, 12, 16, 24, 32) en `padding`/`spacedBy`/`Spacer` en vez de `SportKidTheme.spacing` → 🟠 en `feature/`, `core:ui` y `:app` (TD-SK-004) · 🟡 en átomos. Otro `N.dp` → 🔎 (¿`SkDimens` o medida propia del átomo?). `N.sp` literal → 🟠 | 🟠/🟡/🔎 |
| FF-THM-03 | `TextStyle(`, `fontSize =` o `fontFamily =` fuera de `theme/` → 🟠 (estilo con rol en `Type.kt`). `.copy(fontWeight = …)` → 🔎 (aceptable si Figma lo pide; si se repite, extensión con rol) | 🟠/🔎 |
| FF-THM-04 | Token nuevo nombrado por valor (`Gray400`, `Size56`, `Blue1E88E5`) o con el mismo valor que uno existente | 🟡 |
| FF-THM-05 | Color de dominio decidido en la UI: `when` sobre `Sport`/`Belt`/`MedalResult` que devuelve colores en vez de `Sport.colors()`/`Belt.color()`/`MedalResult.colors()`; color del hijo como fondo de texto (README §1.2: solo anillo, puntos, punto junto al nombre y borde del chip) | 🟠 / 🔎 |
| FF-THM-06 | Token nuevo que queda sin uso | 🟡 |
| FF-THM-07 | `RoundedCornerShape(N.dp)` fuera de `theme/` en vez de `MaterialTheme.shapes.*` | 🟡 |
| FF-THM-08 | Un archivo tocado aumenta su número de literales `N.dp` respecto de la base (regla del boy scout sobre TD-SK-004) | 🟡 |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
sev() { is_new_file "$1" && echo 🟠 || echo 🟡; }
NOT_THEME='/designsystem/theme/'

DP_RE='[0-9]+(\.[0-9]+)?\.dp([^A-Za-z0-9_]|$)'       # git grep usa ERE POSIX: sin \b
echo "Base — Color(0x…) fuera de theme/: $(git grep -nE 'Color\(0x' -- "${MAIN_KT[@]}" | grep -v "$NOT_THEME" | wc -l | tr -d ' ') · N.dp en feature/: $(git grep -hoE "$DP_RE" -- 'feature/*/src/main/*.kt' | wc -l | tr -d ' ') · en core:ui: $(git grep -hoE "$DP_RE" -- 'core/ui/src/main/*.kt' | wc -l | tr -d ' ') (TD-SK-004)"

ADDED=$(added_main | grep -v "$NOT_THEME")
NC=$(printf '%s\n' "$ADDED" | sed -E 's#[[:space:]]+//.*$##')       # sin comentario final
NEW=" $(echo $PR_NEW_FILES) "
by_new() { awk -v N="$NEW" -v T="$1" '{f=$0; sub(/:.*/,"",f); print (index(N," " f " ") ? "🟠" : "🟡") " " T " " $0}'; }

# FF-THM-01 — colores literales
printf '%s\n' "$NC" | grep -E '\bColor\(0x|\bColor\([0-9]+, *[0-9]+, *[0-9]+' | by_new FF-THM-01
printf '%s\n' "$NC" | grep -E '\bColor\.(White|Black|Red|Green|Blue|Gray|LightGray|DarkGray|Yellow|Cyan|Magenta)\b' \
  | sed 's/^/🟡 FF-THM-01 ¿rol on* del esquema en vez de Color.X? /'

# FF-THM-02 — espaciado y medidas
SPACING='(padding|spacedBy|Spacer|PaddingValues)[^)]*\b(4|8|12|16|24|32)\.dp\b'
printf '%s\n' "$NC" | grep -E "$SPACING" \
  | awk '{print ($0 ~ /^core\/designsystem\// ? "🟡" : "🟠") " FF-THM-02 espaciado literal → SportKidTheme.spacing (4 xs · 8 sm · 12 md · 16 lg · 24 xl · 32 xxl) " $0}'
printf '%s\n' "$ADDED" | grep -v '//' | grep -vE "$SPACING" | grep -E '\b[0-9]+(\.[0-9]+)?\.dp\b' \
  | sed 's/^/🔎 FF-THM-02 medida literal — ¿SkDimens\/token o medida del átomo? /'
printf '%s\n' "$NC" | grep -E '\b[0-9]+(\.[0-9]+)?\.sp\b' | sed 's/^/🟠 FF-THM-02 sp literal → MaterialTheme.typography /'

# FF-THM-03 — tipografía
printf '%s\n' "$NC" | grep -E 'TextStyle\(|fontSize *=|fontFamily *=' | sed 's/^/🟠 FF-THM-03 /'
printf '%s\n' "$NC" | grep -E 'fontWeight *=' | sed 's/^/🔎 FF-THM-03 ¿peso que pide Figma o falta un estilo con rol? /'

# FF-THM-05 — colores de dominio
printf '%s\n' "$NC" | grep -E 'when *\([^)]*\b(sport|belt|medal|result)\b' | sed 's/^/🔎 FF-THM-05 ¿color de dominio decidido en la UI? /'
printf '%s\n' "$NC" | grep -E '(background|containerColor|color *= *)[^,]*athlete[A-Za-z.]*\.color' \
  | sed 's/^/🔎 FF-THM-05 color del hijo como fondo\/contenedor — solo anillo, puntos y borde (README §1.2) /'

# FF-THM-07 — formas literales
printf '%s\n' "$NC" | grep -E 'RoundedCornerShape\([0-9]' | sed 's/^/🟡 FF-THM-07 forma literal → MaterialTheme.shapes /'

# FF-THM-04 / FF-THM-06 — tokens nuevos en theme/
added_lines "$THEME_DIR/*.kt" | grep -E ':[[:space:]]*(internal |private )?val [A-Za-z][A-Za-z0-9]*[[:space:]]*(:[^=]+)?=' | while IFS= read -r l; do
  f=${l%%:*}; name=$(echo "$l" | sed -E 's/.*val ([A-Za-z0-9]+).*/\1/'); value=$(echo "$l" | sed -E 's/^[^=]*= *//; s/,$//')
  echo "$name" | grep -qE '^(Gray|Grey|Red|Blue|Green|Amber|Slate|Size|Space)?[0-9]{2,3}$|[A-Za-z]+(50|100|200|300|400|500|600|700|800|900)$|[0-9A-Fa-f]{6}$' \
    && echo "🟡 FF-THM-04 token nombrado por valor: $l"
  dup=$(grep -nF "= $value" "$f" | grep -vw "val $name" | head -2 | tr '\n' ' ')
  [ -n "$dup" ] && echo "🔎 FF-THM-04 $name repite el valor de: $dup"
  git grep -qwE "$name" -- "${MAIN_KT[@]}" ":(exclude)$f" \
    || grep -cw "$name" "$f" | grep -qv '^1$' \
    || echo "🟡 FF-THM-06 token nuevo sin uso: $(basename "$f" .kt).$name"
done

# FF-THM-08 — literales dp por archivo tocado (no debe crecer)
for f in $PR_MAIN; do
  case "$f" in *$NOT_THEME*) continue ;; esac
  is_new_file "$f" && continue
  b=$(base_file "$f" | grep -oE '\b[0-9]+(\.[0-9]+)?\.dp\b' | wc -l | tr -d ' ')
  a=$(grep -oE '\b[0-9]+(\.[0-9]+)?\.dp\b' "$f" | wc -l | tr -d ' ')
  [ "$a" -gt "$b" ] && echo "🟡 FF-THM-08 $f — literales N.dp: $b → $a (TD-SK-004 no debe empeorar)"
done
true
EOF
```

**Cómo interpretar:**
- La corrección de FF-THM-01/02 es usar un token existente (`grep -n "val " core/designsystem/src/main/kotlin/com/invex/sportkid/core/designsystem/theme/*.kt`) o crear uno con nombre de **rol** (README §1). Si Figma y el README no coinciden en un token, **manda Figma** y se actualiza el README (`CLAUDE.md` → Tokens).
- Medidas propias de un átomo documentadas en el README (alturas de `SkButtonSize`, 56dp del campo, 40dp del icon tile) son parte de su especificación: en `core:designsystem/component` no se reportan como 🟠. Coordenadas de dibujo en `Canvas` o `viewport` de vectores tampoco son tokens.
- Un valor estructural (borde de 1dp, grosor de trazo) se permite con comentario en la misma línea.
- **FF-THM-03:** estilos nuevos van a `Type.kt` con nombre de rol (como `metric` y `brand`); `MaterialTheme.typography.titleMedium.copy(color = …)` es aceptable.
- **FF-THM-05:** el color del deporte para acentos y el del hijo **solo** para anillos y puntos (amarillo y naranja no cumplen 4.5:1 como fondo de texto). Para etiquetas sobre el contenedor del deporte, `Sport.colors().onContainer` (`sport.on*Container`). Al probar un tema nuevo, los colores de deporte e hijos **no** deben cambiar (README §7).
- **FF-THM-08:** la deuda TD-SK-004 (~515 `N.dp` en `feature/`, 58 en `core:ui`, 2 `Color(0x…)` fuera de `theme/`) se paga de a poco: tocar un archivo no la empeora.
