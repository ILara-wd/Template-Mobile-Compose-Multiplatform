#!/usr/bin/env bash
# Crea el esqueleto de un átomo del sistema de diseño SportKid (core:designsystem, prefijo Sk)
# siguiendo CLAUDE.md y README §6.2: stateless, modifier como primer opcional, tokens del tema
# y preview private con @SkPreviews.
#
# Uso (desde la raíz del repo):
#   ./create_component.sh              # interactivo
#   ./create_component.sh SkBadge      # el prefijo Sk se agrega si falta
#
# Solo para piezas SIN dominio. Si la pieza conoce Sport, Belt, PaymentStatus… va en core:ui
# (sin prefijo), y si solo la usa una pantalla, private en su feature.
#
# Antes de crear, busca si ya existe algo parecido (o usa /catalogo-ui-sportkid consultar):
#   grep -rn "fun Sk\|^fun [A-Z]" core/designsystem core/ui --include=*.kt

set -euo pipefail
cd "$(dirname "$0")"

BASE_DIR="core/designsystem/src/main/kotlin/com/invex/sportkid/core/designsystem/component"
BASE_PKG="com.invex.sportkid.core.designsystem"

NAME="${1:-}"
[[ -z "$NAME" ]] && read -r -p "Nombre del componente (p. ej. SkBadge): " NAME

[[ "$NAME" == Sk* ]] || NAME="Sk${NAME}"
if [[ ! "$NAME" =~ ^Sk[A-Z][A-Za-z0-9]*$ ]]; then
  echo "❌ Nombre inválido '$NAME': usa PascalCase con prefijo Sk (p. ej. SkBadge)." >&2
  exit 1
fi

FILE="$BASE_DIR/$NAME.kt"
if [[ -e "$FILE" ]]; then
  echo "❌ Ya existe $FILE" >&2
  exit 1
fi

# Componentes con nombre parecido en core:designsystem y core:ui
STEM="${NAME#Sk}"
SIMILAR=$(grep -rlE "^fun (Sk)?[A-Za-z]*${STEM}[A-Za-z]*\(" core/designsystem core/ui --include='*.kt' 2>/dev/null || true)
if [[ -n "$SIMILAR" ]]; then
  echo "⚠️  Ya hay componentes con nombre parecido; revisa si puedes extenderlos:"
  printf '   %s\n' $SIMILAR
fi

mkdir -p "$BASE_DIR"
cat > "$FILE" <<EOF
package $BASE_PKG.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import $BASE_PKG.preview.SkPreviews
import $BASE_PKG.theme.SportKidTheme

/**
 * TODO: describe en una línea qué es $NAME (Figma: "<nombre de la capa>").
 *
 * Stateless: recibe valores y emite callbacks; el estado vive en la pantalla que lo usa.
 */
@Composable
fun $NAME(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.padding(SportKidTheme.spacing.sm)) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@SkPreviews
@Composable
private fun ${NAME}Preview() = SportKidTheme {
    $NAME(text = "Entrenamientos")
}
EOF

echo "✅ Creado $FILE"
echo "   Siguientes pasos:"
echo "   • Reemplaza el TODO del KDoc y los parámetros de ejemplo."
echo "   • Usa solo tokens: MaterialTheme.colorScheme/typography/shapes, SportKidTheme.spacing, LocalSportColors."
echo "   • No importes core:model: core:designsystem no conoce el dominio."
echo "   • Agrega una @SkPreviews private por estado relevante y revísala en claro, oscuro y fuente 1.3."
echo "   • Descríbelo en README §1.6 y regenera el catálogo: python3 scripts/ui-catalogo/generar.py"
exit 0
