## 5.5 Revisiones de Tokens de Diseño (FF-THM-XX)

Verifica que color, medida y tipografía salgan de los tokens `Tkd*` de `ui/theme/` (`03-standards.md §3`, ADR-004, THM-01..06). `ui/theme/` (`TkdColors`, `TkdDimens`, `TkdTypography`, `TkdBeltPalette`) es la **única** casa de literales.

| ID | Regla | Severidad |
|---|---|---|
| FF-THM-01 | `Color(0x…)`, `Color(r, g, b)` o `Color.White|Black|Red|Gray|…` fuera de `ui/theme/` — también en previews (THM-01). Permitidos `Color.Unspecified` y `Color.Transparent` | 🟠 archivo nuevo · 🟡 archivo tocado |
| FF-THM-02 | `N.dp`/`N.sp` literal fuera de `ui/theme/` sin comentario que lo justifique (THM-02) | 🟠 |
| FF-THM-03 | `TextStyle(`, `fontSize =`, `fontWeight =`, `MaterialTheme.typography` o `MaterialTheme.colorScheme` en componentes/pantallas (THM-03) | 🟠 |
| FF-THM-04 | Token nuevo nombrado por valor (`Amber50`, `Size56`, `Gray400`) o con el mismo valor que uno existente (THM-04) | 🟡 |
| FF-THM-05 | Color de cinturón decidido con `when` en la UI en vez de `BeltColor.palette()` (THM-05) | 🟠 |
| FF-THM-06 | Token que queda sin uso (THM-06) | 🟡 |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
sev() { is_new_file "$1" && echo 🟠 || echo 🟡; }

echo "Base — Color fuera de ui/theme: $(grep -rnE '\bColor\(0x|\bColor\.(White|Black|Red|Gray|Green|Blue|Yellow|Cyan|Magenta)\b' $APP_MAIN | grep -v /ui/theme/ | wc -l | tr -d ' ') · dp/sp literales: $(grep -rnE '[0-9]\.(dp|sp)\b' $APP_MAIN | grep -v /ui/theme/ | wc -l | tr -d ' ')"

added_main | grep -v "^$THEME_DIR/" | while IFS= read -r l; do
  f=${l%%:*}; code=${l#*:*:}; nocomment=${code%%//*}
  echo "$nocomment" | grep -qE '\bColor\(0x|\bColor\([0-9]+, *[0-9]+, *[0-9]+|\bColor\.(White|Black|Red|Green|Blue|Gray|LightGray|DarkGray|Yellow|Cyan|Magenta)\b' \
    && echo "$(sev "$f") FF-THM-01 $l"
  if echo "$nocomment" | grep -qE '[0-9]+(\.[0-9]+)?\.(dp|sp)\b' && ! echo "$code" | grep -q '//'; then
    echo "🟠 FF-THM-02 $l"
  fi
  echo "$nocomment" | grep -qE 'TextStyle\(|fontSize *=|fontWeight *=|MaterialTheme\.(typography|colorScheme)' \
    && echo "🟠 FF-THM-03 $l"
  echo "$nocomment" | grep -qE 'when *\(.*(belt|BeltColor)' && echo "🔎 FF-THM-05 ¿color de cinturón decidido en la UI? $l"
done

# FF-THM-04 — tokens nuevos en ui/theme
added_lines "$THEME_DIR/*.kt" | grep -E ':[[:space:]]*val [A-Z][A-Za-z0-9]*[[:space:]]*=' | while IFS= read -r l; do
  f=${l%%:*}; name=$(echo "$l" | sed -E 's/.*val ([A-Za-z0-9]+).*/\1/'); value=$(echo "$l" | sed -E 's/.*= *//')
  echo "$name" | grep -qE '^(Gray|Grey|Red|Blue|Green|Amber|Slate|Size|Space)?[0-9]{2,3}$|[A-Za-z]+(50|100|200|300|400|500|600|700|800|900)$' \
    && echo "🟡 FF-THM-04 token nombrado por valor: $l"
  dup=$(grep -nF "= $value" "$f" | grep -v "val $name " | head -2 | tr '\n' ' ')
  [ -n "$dup" ] && echo "🔎 FF-THM-04 $name repite el valor de: $dup"
  # FF-THM-06 — el token nuevo debe usarse
  obj=$(basename "$f" .kt)
  grep -rqE "$obj\.$name\b" "$APP_MAIN" --exclude-dir=theme || grep -rqE "\b$name\b" "$APP_MAIN" --exclude="$(basename "$f")" \
    || echo "🟡 FF-THM-06 token nuevo sin uso: $obj.$name"
done
true
EOF
```

**Cómo interpretar:**
- La corrección de FF-THM-01/02 es usar un token existente (`grep -n "val " app/src/main/java/dev/treecode/kensho/ui/theme/TkdColors.kt`) o crear uno con nombre de rol. Un valor estructural (borde de 1dp, grosor de un trazo en `Canvas`) se permite con comentario en la misma línea.
- Coordenadas de dibujo en `Canvas` o `viewport` de vectores no son tokens de layout: no las reportes.
- **FF-THM-03:** los estilos de texto nuevos van a `TkdTypography` con nombre de rol (`HeadlineRelaxed`, `OverlineTight`); `.copy(color = …)` sobre un estilo de `TkdTypography` es aceptable.
- **FF-THM-05:** `TkdBeltPalette` expone `container/content/badgeContainer/badgeContent` por cinturón; un `when (belt)` que devuelve colores en una pantalla duplica esa fuente.
