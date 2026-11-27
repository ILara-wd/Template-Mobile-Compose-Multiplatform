#!/usr/bin/env bash
# Crea el esqueleto de un componente del sistema de diseño Tkd siguiendo
# docs/architecture/03-standards.md (CMP-01, CMP-09, STR-03, STR-05, THM-01/02).
#
# Uso:
#   ./create_component.sh                       # interactivo
#   ./create_component.sh TkdChip molecules     # nombre y capa
#
# Antes de crear, busca si ya existe algo parecido (CMP-07):
#   grep -rn "fun Tkd" app/src/main/java/dev/treecode/kensho/components

set -euo pipefail

BASE_DIR="app/src/main/java/dev/treecode/kensho/components"
BASE_PKG="dev.treecode.kensho.components"

NAME="${1:-}"
LAYER="${2:-}"
[[ -z "$NAME" ]] && read -r -p "Nombre del componente (p. ej. TkdChip): " NAME
[[ -z "$LAYER" ]] && read -r -p "Capa [atoms|molecules] (default: atoms): " LAYER
LAYER="${LAYER:-atoms}"

# Prefijo Tkd obligatorio (STR-03)
[[ "$NAME" == Tkd* ]] || NAME="Tkd${NAME}"
if [[ ! "$NAME" =~ ^Tkd[A-Z][A-Za-z0-9]*$ ]]; then
  echo "❌ Nombre inválido '$NAME': usa PascalCase con prefijo Tkd (p. ej. TkdChip)." >&2
  exit 1
fi
if [[ "$LAYER" != "atoms" && "$LAYER" != "molecules" ]]; then
  echo "❌ Capa inválida '$LAYER': usa atoms (sin dominio) o molecules." >&2
  exit 1
fi

FILE="$BASE_DIR/$LAYER/$NAME.kt"
if [[ -e "$FILE" ]]; then
  echo "❌ Ya existe $FILE" >&2
  exit 1
fi

# Componentes con nombre parecido (CMP-07)
SIMILAR=$(grep -rlE "^fun Tkd[A-Za-z]*${NAME#Tkd}[A-Za-z]*\(" "$BASE_DIR" 2>/dev/null || true)
if [[ -n "$SIMILAR" ]]; then
  echo "⚠️  Ya hay componentes con nombre parecido; revisa si puedes extenderlos:"
  printf '   %s\n' $SIMILAR
fi

mkdir -p "$BASE_DIR/$LAYER"
cat > "$FILE" <<EOF
package $BASE_PKG.$LAYER

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.treecode.kensho.ui.theme.TkdColors
import dev.treecode.kensho.ui.theme.TkdDimens
import dev.treecode.kensho.ui.theme.TkdTypography

/**
 * TODO KEN-XXX: describe en una línea qué es $NAME (no cómo está hecho).
 *
 * Stateless: recibe valores y emite callbacks; el estado vive en el ViewModel de la pantalla.
 */
@Composable
fun $NAME(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.padding(TkdDimens.Space8)) {
        Text(text = text, style = TkdTypography.Body, color = TkdColors.OnSurface)
    }
}

@Preview
@Composable
private fun ${NAME}Preview() {
    $NAME(text = "Texto de ejemplo")
}
EOF

echo "✅ Creado $FILE"
echo "   Siguientes pasos:"
echo "   • Reemplaza el TODO del KDoc y los parámetros de ejemplo."
echo "   • Usa solo tokens de ui/theme (TkdColors, TkdDimens, TkdTypography)."
echo "   • Agrega una @Preview private por estado relevante."
echo "   • Si tiene comportamiento (toggle, selección), agrega su Compose UI test en app/src/androidTest."
[[ "$LAYER" == "atoms" ]] && echo "   • Es un átomo: no importes model/ ni data/ (CMP-03)."
exit 0
