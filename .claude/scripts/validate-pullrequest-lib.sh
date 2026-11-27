#!/usr/bin/env bash
# Utilidades compartidas por /validate-pullrequest y sus familias ff-* (Kensho, módulo :app).
# Cada llamada Bash del agente es un shell nuevo (y el shell del usuario puede ser zsh, que no
# parte variables por espacios), así que cada bloque de FF corre bajo bash y hace:
#
#   BASE_BRANCH=develop bash <<'EOF'
#   source .claude/scripts/validate-pullrequest-lib.sh
#   ...
#   EOF
#
# BASE_BRANCH es la rama destino del PR (Sección 0.1 del skill). Por defecto `develop`.
# BASE_REF puede sobrescribirse (p. ej. BASE_REF=develop para comparar contra la rama local
# cuando no hay remoto actualizado).

BASE_BRANCH="${BASE_BRANCH:-develop}"
BASE_REF="${BASE_REF:-origin/$BASE_BRANCH}"
git rev-parse --verify --quiet "$BASE_REF" >/dev/null || BASE_REF="$BASE_BRANCH"
export BASE_BRANCH BASE_REF

APP_MAIN="app/src/main/java/dev/treecode/kensho"
APP_RES="app/src/main/res"
UNIT_TEST_DIR="app/src/test/java/dev/treecode/kensho"
UI_TEST_DIR="app/src/androidTest/java/dev/treecode/kensho"
THEME_DIR="$APP_MAIN/ui/theme"
FEATURES="login signup students profile checkin payments ui/present"
TICKET_RE="KEN-[0-9]+"

# Archivos .kt agregados/modificados/copiados por el PR
PR_FILES=$(git diff --name-only --diff-filter=ACMR "$BASE_REF"...HEAD -- '*.kt')
PR_NEW_FILES=$(git diff --name-only --diff-filter=A "$BASE_REF"...HEAD -- '*.kt')
PR_DELETED_FILES=$(git diff --name-only --diff-filter=D "$BASE_REF"...HEAD -- '*.kt')
PR_MAIN=$(printf '%s\n' "$PR_FILES" | grep "^$APP_MAIN/" || true)
PR_MAIN_NEW=$(printf '%s\n' "$PR_NEW_FILES" | grep "^$APP_MAIN/" || true)
PR_UNIT_TESTS=$(printf '%s\n' "$PR_FILES" | grep '^app/src/test/' || true)
PR_UI_TESTS=$(printf '%s\n' "$PR_FILES" | grep '^app/src/androidTest/' || true)
# Sistema de diseño (átomos y moléculas)
PR_COMPONENTS=$(printf '%s\n' "$PR_MAIN" | grep -E "/components/(atoms|molecules)/" || true)
# Pantallas, secciones y ViewModels de features
PR_SCREENS=$(printf '%s\n' "$PR_MAIN" | grep -E '(Screen|Sections?|Content)\.kt$|/[A-Za-z]+(Card|Cards|Controls|Consent)\.kt$' | grep -v "/components/" || true)
PR_VIEWMODELS=$(printf '%s\n' "$PR_MAIN" | grep -E 'ViewModel\.kt$' || true)
PR_MODELS=$(printf '%s\n' "$PR_MAIN" | grep "/model/" || true)
PR_DATA=$(printf '%s\n' "$PR_MAIN" | grep "/data/" || true)
PR_RES=$(git diff --name-only --diff-filter=ACMR "$BASE_REF"...HEAD -- "$APP_RES" || true)

# Líneas agregadas por el PR → "archivo:línea:contenido".
# Uso: added_lines 'app/src/main/*.kt'   (pathspec de git; '*' cruza carpetas)
added_lines() {
  git diff -U0 "$BASE_REF"...HEAD -- "$@" | awk '
    /^\+\+\+ /   { file = substr($0, 7); next }
    /^@@/        { split($3, a, ","); line = substr(a[1], 2) + 0; next }
    /^\+/        { print file ":" line ":" substr($0, 2); line++ }'
}

# Líneas agregadas en código productivo
added_main() { added_lines "$APP_MAIN/*.kt"; }

# ¿El archivo es nuevo en el PR? (para decidir 🟠 archivo nuevo vs 🟡 archivo tocado)
is_new_file() { printf '%s\n' "$PR_NEW_FILES" | grep -qxF "$1"; }

# Feature al que pertenece un archivo de main (vacío si es compartido)
feature_of() {
  local rel=${1#$APP_MAIN/}
  for f in $FEATURES; do
    case "$rel" in "$f"/*) echo "$f"; return ;; esac
  done
}

# Líneas de código efectivo: excluye blancos, // y bloques /* */ /** */
count_code_lines() {
  awk '
    { l = $0; gsub(/^[ \t]+|[ \t]+$/, "", l) }
    inblk  { if (index(l, "*/")) inblk = 0; next }
    l == "" { next }
    l ~ /^\/\// { next }
    l ~ /^\/\*/ { if (!index(l, "*/")) inblk = 1; next }
    { n++ }
    END { print n + 0 }' "$1"
}
