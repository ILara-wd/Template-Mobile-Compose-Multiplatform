<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde .claude/commands/validate-pullrequest/ff-res.md. NO EDITAR A MANO. -->
---
mode: agent
description: 5.8 Revisiones de Recursos de Figma (FF-RES-XX)
---

## 5.8 Revisiones de Recursos de Figma (FF-RES-XX)

Los recursos visuales nacen del prototipo de Figma (`Prototipo-Demo-UiKit`) y se exportan, no se redibujan (`03-standards.md §6`, ADR-007, RES-01..05).

| ID | Regla | Severidad |
|---|---|---|
| FF-RES-01 | Drawable nuevo fuera de convención: `ic_tkd_<nombre>.xml` (vector), `img_tkd_<pantalla>_<nombre>.(jpg|png|webp)` (foto), `ic_tkd_nav_<tab>` (barra inferior) (RES-01) | 🟡 |
| FF-RES-02 | Vector nuevo sin la cabecera `<!-- Generado desde Figma (…). No editar a mano. -->`, o vector generado editado a mano (RES-02) | 🟠 |
| FF-RES-03 | Drawable nuevo con el mismo `pathData` que uno existente (debía reutilizarse), o drawable reutilizado que no es idéntico al del diseño (RES-03) | 🟡 |
| FF-RES-04 | URL de Figma (`figma.com/api`, `figma-alpha-api`, `s3-…figma`) en código o recursos (RES-04) | 🟠 |
| FF-RES-05 | Foto nueva > 200 KB, o raster donde bastaría un vector (RES-05) | 🟡 |
| FF-RES-06 | Drawable nuevo sin uso en el código | 🟡 |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
NEW_DRAWABLES=$(git diff --name-only --diff-filter=A "$BASE_REF"...HEAD -- "$APP_RES/drawable*")
MOD_VECTORS=$(git diff --name-only --diff-filter=M "$BASE_REF"...HEAD -- "$APP_RES/drawable*/*.xml")

echo "ℹ️ FF-RES drawables nuevos: $(printf '%s\n' $NEW_DRAWABLES | grep -c .)"
for d in $NEW_DRAWABLES; do
  name=$(basename "$d"); stem=${name%.*}
  case "$name" in
    ic_tkd_*.xml|img_tkd_*.jpg|img_tkd_*.jpeg|img_tkd_*.png|img_tkd_*.webp) ;;
    ic_launcher*) ;;
    *) echo "🟡 FF-RES-01 $d — nombre fuera de convención (ic_tkd_*.xml / img_tkd_<pantalla>_*)" ;;
  esac
  case "$name" in
    *.xml)
      head -3 "$d" | grep -q 'Generado desde Figma' || echo "🟠 FF-RES-02 $d — vector sin cabecera 'Generado desde Figma (…). No editar a mano.'"
      p=$(grep -oE 'android:pathData="[^"]{20,}"' "$d" | head -1)
      if [ -n "$p" ]; then
        other=$(grep -rlF "$p" "$APP_RES"/drawable* | grep -vxF "$d" | head -2 | tr '\n' ' ')
        [ -n "$other" ] && echo "🟡 FF-RES-03 $d — mismo pathData que: $other (¿reutilizar?)"
      fi ;;
    *)
      kb=$(( $(wc -c < "$d") / 1024 ))
      [ "$kb" -gt 200 ] && echo "🟡 FF-RES-05 $d — ${kb} KB (> 200 KB; convertir a WebP o reducir)"
      case "$name" in ic_*) echo "🟡 FF-RES-05 $d — ícono raster: ¿puede ser vector?" ;; esac ;;
  esac
  grep -rqE "R\.drawable\.$stem\b|@drawable/$stem\b" app/src/main || echo "🟡 FF-RES-06 $d — sin uso"
done

for d in $MOD_VECTORS; do
  git show "$BASE_REF:$d" 2>/dev/null | head -3 | grep -q 'Generado desde Figma' \
    && echo "🔎 FF-RES-02 $d — vector generado desde Figma modificado: ¿se re-exportó o se editó a mano?"
done

added_lines '*' | grep -E 'figma\.com/api|figma-alpha-api|s3-[a-z0-9-]+\.amazonaws\.com/figma|figma\.com/(file|design)/[A-Za-z0-9]+/.*\.(png|svg)' \
  | sed 's/^/🟠 FF-RES-04 URL de Figma en código\/recursos /'
true
EOF
```

**Cómo interpretar:**
- **FF-RES-03:** "idéntico" significa mismo `pathData`, tamaño y colores. Un ícono igual pero con fondo, recorte o tamaño distinto es un recurso nuevo con sufijo descriptivo (`ic_tkd_exam_badge_tile`, `ic_tkd_check_small`). Si se reutilizó un drawable, compara con `diff` contra el SVG exportado antes de aprobar.
- **FF-RES-02:** si un vector necesita un cambio, se re-exporta desde Figma y se regenera; editarlo a mano rompe la trazabilidad con el diseño.
- Los enlaces al **nodo** de Figma en comentarios de `strings.xml` (`Figma: Prototipo-Demo-UiKit, nodo 46:325`) están permitidos; lo prohibido son URLs de assets temporales.
- Verificación visual (no automatizable): la pantalla en el emulador se compara con la captura del nodo de Figma; pide las capturas en el PR si faltan.
