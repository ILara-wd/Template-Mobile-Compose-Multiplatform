<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde .claude/commands/validate-pullrequest/ff-res.md. NO EDITAR A MANO. -->
---
mode: agent
description: 5.8 Revisiones de Recursos de Figma (FF-RES-XX)
---

## 5.8 Revisiones de Recursos de Figma (FF-RES-XX)

Los recursos visuales nacen del prototipo de Figma **Prototipo-Demo-UiKit** (archivo `KqTFktvrRvCzO2idPga5xf`) y se exportan, no se redibujan: SVG → VectorDrawable 1:1 en el módulo que lo usa, fotos en `drawable-nodpi`, reutilizando lo que ya existe en `core` (`CLAUDE.md` §Convenciones → Recursos de Figma y §Flujo Figma → código, paso 3; pestaña **Assets** de `/catalogo-ui-sportkid`).

| ID | Regla | Severidad |
|---|---|---|
| FF-RES-01 | Drawable nuevo fuera de convención: `ic_<feature>_<nombre>.xml` (vector), `img_<pantalla>_<nombre>` o `img_sample_<nombre>` (foto, en `drawable-nodpi`), `sk_ic_<nombre>.xml` en `core:designsystem`, `ic_nav_<tab>` en `:app`; prefijo nuevo en el módulo (🔎) | 🟡 |
| FF-RES-02 | Vector nuevo sin la cabecera `<!-- Generado desde Figma (Prototipo-Demo-UiKit). No editar a mano. -->`, vector generado editado a mano, o vector con elementos que el conversor no maneja (degradados, `aapt:attr`) | 🟠 |
| FF-RES-03 | Drawable nuevo con el mismo `pathData` que uno existente en cualquier módulo (debía reutilizarse desde `core` o subir a `core:ui`); icono de deporte nuevo en lugar de `Sport.iconRes` | 🟡 |
| FF-RES-04 | URL de Figma (`figma.com/api`, `figma-alpha-api`, assets temporales del MCP) en código o recursos | 🟠 |
| FF-RES-05 | Foto nueva > 200 KB, foto fuera de `drawable-nodpi`, o raster donde bastaría un vector | 🟡 |
| FF-RES-06 | Drawable nuevo sin uso en el código | 🟡 |
| FF-RES-07 | Drawable nuevo con el **mismo nombre** que uno de otro módulo (al fusionar recursos se queda uno solo y la otra pantalla muestra la imagen equivocada) | 🟠 |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
DRAWABLE_SPEC=('app/src/main/res/drawable*' 'core/*/src/main/res/drawable*' 'feature/*/src/main/res/drawable*')
NEW_DRAWABLES=$(pr_diff --name-only --diff-filter=A -- "${DRAWABLE_SPEC[@]}")
MOD_VECTORS=$(pr_diff --name-only --diff-filter=M -- "${DRAWABLE_SPEC[@]}" | grep '\.xml$')
ALL_DRAWABLES=$(ls -d app/src/main/res/drawable*/* core/*/src/main/res/drawable*/* feature/*/src/main/res/drawable*/* 2>/dev/null)
REFS=$(grep -rhoE 'R\.drawable\.[a-z0-9_]+|@drawable/[a-z0-9_]+' --include='*.kt' --include='*.xml' app/src/main core/*/src/main feature/*/src/main | sed -E 's/.*[./]//' | sort -u)

echo "ℹ️ FF-RES drawables nuevos: $(printf '%s\n' $NEW_DRAWABLES | grep -c .)"
for d in $NEW_DRAWABLES; do
  name=$(basename "$d"); stem=${name%.*}; dir=$(dirname "$d"); module=${d%%/src/main/*}
  # FF-RES-01 — nombre según el módulo
  case "$module:$name" in
    core/designsystem:sk_ic_*.xml|app:ic_nav_*.xml|app:ic_launcher*) ;;
    *:ic_[a-z0-9]*_[a-z0-9]*.xml|*:img_[a-z0-9]*_[a-z0-9]*.jpg|*:img_[a-z0-9]*_[a-z0-9]*.jpeg|*:img_[a-z0-9]*_[a-z0-9]*.png|*:img_[a-z0-9]*_[a-z0-9]*.webp)
      prefix=$(echo "$stem" | cut -d_ -f1-2)
      ls "$module"/src/main/res/drawable*/ | grep -v "^$name$" | grep -q "^${prefix}_" \
        || echo "🔎 FF-RES-01 $d — prefijo nuevo '${prefix}_' en $module: ¿corresponde al feature o pantalla?" ;;
    *) echo "🟡 FF-RES-01 $d — nombre fuera de convención (ic_<feature>_<nombre>.xml / img_<pantalla>_<nombre> / sk_ic_* en core:designsystem)" ;;
  esac
  # FF-RES-07 — mismo nombre en otro módulo
  other=$(printf '%s\n' $ALL_DRAWABLES | grep -E "/$stem\.[a-z]+$" | grep -v "^$module/" | head -2 | tr '\n' ' ')
  [ -n "$other" ] && echo "🟠 FF-RES-07 $d — mismo nombre que: $other"
  case "$name" in
    *.xml)
      case "$name" in ic_launcher*) ;; *)  # el ícono del launcher no sale de Figma
        head -3 "$d" | grep -q 'Generado desde Figma' || echo "🟠 FF-RES-02 $d — vector sin cabecera 'Generado desde Figma (Prototipo-Demo-UiKit). No editar a mano.'"
        grep -qE '<gradient|aapt:attr' "$d" && echo "🔎 FF-RES-02 $d — el conversor solo maneja path fill/stroke: comparar contra la captura de Figma" ;;
      esac
      p=$(grep -oE 'android:pathData="[^"]{20,}"' "$d" | head -1)
      if [ -n "$p" ]; then
        dup=$(grep -rlF -- "$p" app/src/main/res/drawable* core/*/src/main/res/drawable* feature/*/src/main/res/drawable* 2>/dev/null | grep -vxF "$d" | head -2 | tr '\n' ' ')
        case "$dup" in
          *ic_sport_*) echo "🟡 FF-RES-03 $d — es un icono de deporte: usar Sport.iconRes (core:ui) — igual a: $dup" ;;
          ?*) echo "🟡 FF-RES-03 $d — mismo pathData que: $dup (¿reutilizar?)" ;;
        esac
      fi ;;
    *)
      kb=$(( $(wc -c < "$d") / 1024 ))
      [ "$kb" -gt 200 ] && echo "🟡 FF-RES-05 $d — ${kb} KB (> 200 KB; reducir o convertir a WebP)"
      case "$dir" in */drawable-nodpi) ;; *) echo "🟡 FF-RES-05 $d — foto fuera de drawable-nodpi" ;; esac
      case "$name" in ic_*) echo "🟡 FF-RES-05 $d — icono raster: ¿puede ser vector?" ;; esac ;;
  esac
  # FF-RES-06 — uso
  printf '%s\n' "$REFS" | grep -qxF "$stem" || echo "🟡 FF-RES-06 $d — sin uso"
done

for d in $MOD_VECTORS; do
  base_file "$d" | head -3 | grep -q 'Generado desde Figma' \
    && echo "🔎 FF-RES-02 $d — vector generado desde Figma modificado: ¿se re-exportó o se editó a mano?"
done

# FF-RES-04 — URLs de Figma (los nodos en comentarios, p. ej. "Figma 136:3411", sí se permiten)
added_lines 'app/src/main/*' 'core/*/src/main/*' 'feature/*/src/main/*' \
  | grep -E 'figma\.com/api|figma-alpha-api|s3-[a-z0-9-]+\.amazonaws\.com/figma|figma\.com/(file|design)/[A-Za-z0-9]+/.*\.(png|svg)|mcp.*figma.*/assets?/' \
  | sed 's/^/🟠 FF-RES-04 URL de Figma en código\/recursos /'
true
EOF
```

**Cómo interpretar:**
- **FF-RES-01:** el prefijo es el del feature o la pantalla (`ic_home_trophy`, `ic_swim_delete`, `ic_members_*`); los átomos de `core:designsystem` usan `sk_ic_*` y los de dominio de `core:ui` su concepto (`ic_sport_*`, `ic_medal_*`, `ic_form_*`). Las fotos de diseño son `img_<pantalla>_<nombre>` (`img_login_hero`) y las de datos de ejemplo `img_sample_<nombre>`, siempre en `drawable-nodpi`.
- **FF-RES-03:** "idéntico" significa mismo `pathData`, tamaño y colores. Si el icono ya existe en `core`, se reutiliza (`DsR.drawable.sk_ic_close`, `Sport.iconRes`); si es igual pero con otro fondo, recorte o tamaño, es un recurso nuevo con sufijo descriptivo. Si el mismo icono hace falta en dos features, sube a `core:ui`. Antes de agregar uno, compara con `diff` contra los drawables existentes (CLAUDE.md §Flujo Figma, paso 3).
- **FF-RES-02:** si un vector necesita un cambio, se re-exporta desde Figma y se regenera; editarlo a mano rompe la trazabilidad con el diseño.
- **FF-RES-07:** con varios módulos de recursos, dos drawables con el mismo nombre se fusionan en uno solo. Hoy ya pasa con `img_sample_mateo`/`img_sample_sofia` (en `feature:agenda` y `feature:athlete`, con contenido distinto): no agregues más casos.
- Los ids de nodo de Figma en KDoc o comentarios están permitidos; lo prohibido son las URLs de assets (las del MCP caducan en minutos). Las fuentes viven en `core/designsystem/src/main/res/font`.
- Verificación visual (no automatizable): la pantalla en el emulador (`ANDROID_SERIAL=emulator-5554`, extras `sk_*`) se compara con la captura del nodo de Figma. Si el PR agrega assets, regenera el catálogo (`python3 scripts/ui-catalogo/generar.py`) para que aparezcan en la pestaña Assets.
