<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde .claude/commands/validate-pullrequest/ff-i18n.md. NO EDITAR A MANO. -->
---
mode: agent
description: 5.7 Revisiones de Textos y Formato (FF-I18N-XX)
---

## 5.7 Revisiones de Textos y Formato (FF-I18N-XX)

Todo texto visible sale de recursos, en español de México, con formato explícito (`03-standards.md §5`, ADR-009, TD-KEN-011).

| ID | Regla | Severidad |
|---|---|---|
| FF-I18N-01 | `Text("literal")` / `text = "…"` / `label = "…"` / `placeholder = "…"` en `src/main` fuera de `data/` y previews (I18N-01) | 🟠 |
| FF-I18N-02 | Concatenación o interpolación de textos de usuario (`"$a $b"`, `stringResource(x) + …`) (I18N-03) | 🟡 |
| FF-I18N-03 | Formato sin locale: `String.format(` sin `Locale`, `"…".format(`, `DateTimeFormatter.ofPattern(` sin locale, `NumberFormat.getInstance()` sin locale (I18N-04) | 🟡 |
| FF-I18N-04 | Clave nueva fuera de `tkd_<feature>_<descripcion>` / `tkd_cd_*`, valor duplicado, sin comentario de sección con nodo de Figma, o sin uso (I18N-02) | 🟡 |
| FF-I18N-05 | Cadena nueva en `res/values/strings.xml` en inglés (I18N-05) | 🟡 señal |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
ADDED=$(added_main | grep -v "^$APP_MAIN/data/")
SX="$APP_RES/values/strings.xml"

# FF-I18N-01 — literales de UI (excluye cadenas vacías y líneas dentro de previews: confirmar)
printf '%s\n' "$ADDED" | grep -E '(Text\(\s*"|\b(text|label|title|subtitle|placeholder|hint|supportingText) *= *")' \
  | grep -vE '= *""|Text\(""|animate[A-Za-z]*(AsState|Transition)|Transition\(|label *= *"[a-z][A-Za-z]*"[,)]? *$' \
  | sed 's/^/🟠 FF-I18N-01 (confirmar que no está en una @Preview) /'

# FF-I18N-02 — interpolación/concatenación
printf '%s\n' "$ADDED" | grep -E '(Text\(|text *=).*("[^"]*\$\{?[a-zA-Z]|stringResource\([^)]*\) *\+|\+ *stringResource)' \
  | sed 's/^/🟡 FF-I18N-02 /'

# FF-I18N-03 — formato sin locale
printf '%s\n' "$ADDED" | grep -E '"[^"]*"\.format\(|String\.format\("|DateTimeFormatter\.ofPattern\("[^"]*"\)|NumberFormat\.get[A-Za-z]*Instance\(\)|\.uppercase\(\)|\.lowercase\(\)' \
  | sed 's/^/🟡 FF-I18N-03 usar Locale("es","MX") de los helpers compartidos /'

# FF-I18N-04 / 05 — cadenas nuevas
if git diff --name-only "$BASE_REF"...HEAD -- "$SX" | grep -q .; then
  git diff -U0 "$BASE_REF"...HEAD -- "$SX" | grep -E '^\+ *<(string|plurals) ' | while IFS= read -r l; do
    key=$(echo "$l" | sed -E 's/.*name="([^"]+)".*/\1/')
    val=$(echo "$l" | sed -nE 's/.*>([^<]*)<\/string>.*/\1/p')
    echo "$key" | grep -qE '^(app_name|tkd_[a-z0-9]+(_[a-z0-9]+)+)$' || echo "🟡 FF-I18N-04 clave \"$key\" fuera de tkd_<feature>_<descripcion>"
    if [ -n "$val" ]; then
      dup=$(grep -F ">$val</string>" "$SX" | grep -v "name=\"$key\"" | sed -E 's/.*name="([^"]+)".*/\1/' | head -3 | tr '\n' ' ')
      [ -n "$dup" ] && echo "🟡 FF-I18N-04 \"$key\" repite el valor \"$val\" de: $dup"
      echo "$l" | grep -q 'translatable="false"' || \
        echo "$val" | grep -qiE '\b(the|and|of|your|with|for|you|to|is|are|this|view|all|add|open|close|save|cancel|next|back)\b' \
        && echo "🔎 FF-I18N-05 \"$key\" parece estar en inglés: \"$val\""
    fi
    grep -rqE "R\.(string|plurals)\.$key\b" app/src || echo "🟡 FF-I18N-04 \"$key\" nueva sin uso"
  done
  git diff -U0 "$BASE_REF"...HEAD -- "$SX" | grep -E '^\+ *<!--' | grep -viE 'figma|nodo|accesibilidad|compartid' \
    | sed 's/^/🔎 FF-I18N-04 comentario de sección sin nodo de Figma: /'
fi
true
EOF
```

**Cómo interpretar:**
- Datos de las *Fake* (`data/`: nombres de alumnos, conceptos, fechas) son datos, no textos de UI: no son FF-I18N-01. Literales dentro de `@Preview` tampoco (confírmalo leyendo el archivo).
- Si un componente recibe el texto por parámetro (`label: String`), el literal estaría en el llamador.
- **FF-I18N-03:** usar los helpers de `payments/TkdPaymentsFormat.kt` (`spanish` locale, `asUsd()`, `asShortDate()`) o moverlos a un paquete común si otro feature los necesita (no importarlos entre features: ARC-05). Para dos dígitos, `"%02d"` con `String.format(spanish, …)` o un `DateTimeFormatter` con locale.
- **FF-I18N-05:** el idioma base es español (ADR-009). Las cadenas en inglés heredadas son TD-KEN-011: si el PR toca una pantalla en inglés, sugerir traducir sus cadenas en el mismo PR.
