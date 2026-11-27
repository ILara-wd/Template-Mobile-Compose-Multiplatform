#!/usr/bin/env bash
# Utilidades compartidas por /validate-pullrequest y sus familias ff-* (SportKid, multimódulo:
# :app · :core:{designsystem,model,ui} · :feature:*). Cada llamada Bash del agente es un shell
# nuevo (y el shell del usuario puede ser zsh, que no parte variables por espacios), así que cada
# bloque de FF corre bajo bash y hace:
#
#   BASE_BRANCH=sport_kids bash <<'EOF'
#   source .claude/scripts/validate-pullrequest-lib.sh
#   ...
#   EOF
#
# BASE_BRANCH es la rama destino del PR (Sección 0.1 del skill). Por defecto `sport_kids`
# (`main` es la plantilla compartida, no recibe cambios de SportKid).
# BASE_REF puede sobrescribirse (p. ej. BASE_REF=sport_kids para comparar contra la rama local,
# o BASE_REF=HEAD~3 para auditar los últimos commits).
# STAGED=1 audita lo que está en el índice (git diff --cached) en lugar de BASE_REF...HEAD.

BASE_BRANCH="${BASE_BRANCH:-sport_kids}"
BASE_REF="${BASE_REF:-origin/$BASE_BRANCH}"
git rev-parse --verify --quiet "$BASE_REF" >/dev/null || BASE_REF="$BASE_BRANCH"
export BASE_BRANCH BASE_REF

# Rango de diff: "<base>...HEAD" o, con STAGED=1, el índice contra HEAD.
# BASE_SNAPSHOT es el árbol "antes" (para git show / git cat-file).
if [ "${STAGED:-0}" = 1 ]; then DIFF_RANGE=(--cached); BASE_SNAPSHOT=HEAD
else DIFF_RANGE=("$BASE_REF...HEAD"); BASE_SNAPSHOT=$(git merge-base "$BASE_REF" HEAD 2>/dev/null || echo "$BASE_REF"); fi
export BASE_SNAPSHOT
pr_diff() { git diff "${DIFF_RANGE[@]}" "$@"; }

# Rutas del proyecto
APP_MAIN="app/src/main/java/com/invex/one/template"      # :app (namespace heredado, TD-SK-007)
APP_RES="app/src/main/res"
THEME_DIR="core/designsystem/src/main/kotlin/com/invex/sportkid/core/designsystem/theme"
DS_COMPONENT_DIR="core/designsystem/src/main/kotlin/com/invex/sportkid/core/designsystem/component"
CORE_UI_DIR="core/ui/src/main/kotlin/com/invex/sportkid/core/ui"
CORE_MODEL_DIR="core/model/src/main/kotlin/com/invex/sportkid/core/model"
FEATURES=$(ls -d feature/*/src/main 2>/dev/null | cut -d/ -f2 | tr '\n' ' ')   # solo módulos con código
# Código productivo de todos los módulos (regex sobre rutas y pathspecs de git; '*' cruza carpetas)
MAIN_RE='^(app|core/[a-z]+|feature/[a-z]+)/src/main/'
MAIN_KT=('app/src/main/*.kt' 'core/*/src/main/*.kt' 'feature/*/src/main/*.kt')
MAIN_SRC=('app/src/main' 'core/*/src/main' 'feature/*/src/main')
RES_SPEC=('app/src/main/res' 'core/*/src/main/res' 'feature/*/src/main/res')
TEST_KT=('app/src/test/*.kt' 'core/*/src/test/*.kt' 'feature/*/src/test/*.kt'
         'app/src/androidTest/*.kt' 'core/*/src/androidTest/*.kt' 'feature/*/src/androidTest/*.kt')
# Ticket opcional: issue/PR de GitHub como " (#<n>)" al final del asunto (igual que .githooks/commit-msg)
TICKET_RE='#[0-9]+'
DEBT_RE='TD-SK-[0-9]{3}'
COMMIT_RE='^(feat|fix|refactor|test|docs|chore|build|revert)(\([a-z0-9][a-z0-9-]*\))?: .+$'

# Archivos .kt agregados/modificados/copiados por el PR
PR_FILES=$(pr_diff --name-only --diff-filter=ACMR -- '*.kt')
PR_NEW_FILES=$(pr_diff --name-only --diff-filter=A -- '*.kt')
PR_DELETED_FILES=$(pr_diff --name-only --diff-filter=D -- '*.kt')
PR_MAIN=$(printf '%s\n' "$PR_FILES" | grep -E "$MAIN_RE" || true)
PR_MAIN_NEW=$(printf '%s\n' "$PR_NEW_FILES" | grep -E "$MAIN_RE" || true)
PR_UNIT_TESTS=$(printf '%s\n' "$PR_FILES" | grep -E '/src/test/' || true)
PR_UI_TESTS=$(printf '%s\n' "$PR_FILES" | grep -E '/src/androidTest/' || true)
# Átomos Sk* sin dominio (core:designsystem/component) y piezas de dominio (core:ui)
PR_COMPONENTS=$(printf '%s\n' "$PR_MAIN" | grep -E '^core/designsystem/.*/component/' || true)
PR_CORE_UI=$(printf '%s\n' "$PR_MAIN" | grep -E '^core/ui/' || true)
PR_THEME=$(printf '%s\n' "$PR_MAIN" | grep -E '^core/designsystem/.*/theme/' || true)
# Código de features y de :app
PR_FEATURES=$(printf '%s\n' "$PR_MAIN" | grep -E '^feature/' || true)
PR_APP=$(printf '%s\n' "$PR_MAIN" | grep -E '^app/' || true)
# Pantallas (<X>Screen.kt y organismos de un feature: *Sheet, *Calendar, *Card…)
PR_SCREENS=$(printf '%s\n' "$PR_FEATURES" | grep -E '(Screen|Sheet|Sections?)\.kt$' || true)
# Holders de estado (@Stable <X>State) y modelos (core:model, <X>Models.kt con <X>Samples)
PR_STATES=$(printf '%s\n' "$PR_MAIN" | grep -E 'State\.kt$' || true)
PR_MODELS=$(printf '%s\n' "$PR_MAIN" | grep -E '^core/model/|Models\.kt$' || true)
# Capas que SportKid aún no tiene (ViewModel, datos): su sola aparición pide decisión en CLAUDE.md
PR_VIEWMODELS=$(printf '%s\n' "$PR_MAIN" | grep -E 'ViewModel\.kt$' || true)
PR_DATA=$(printf '%s\n' "$PR_MAIN" | grep -E '/data/|Repository\.kt$|^core/data/' || true)
PR_RES=$(pr_diff --name-only --diff-filter=ACMR -- "${RES_SPEC[@]}" || true)

# Líneas agregadas por el PR → "archivo:línea:contenido".
# Uso: added_lines 'feature/*/src/main/*.kt'   (pathspecs de git; '*' cruza carpetas)
added_lines() {
  pr_diff -U0 -- "$@" | awk '
    /^\+\+\+ /   { file = substr($0, 7); next }
    /^@@/        { split($3, a, ","); line = substr(a[1], 2) + 0; next }
    /^\+/        { print file ":" line ":" substr($0, 2); line++ }'
}

# Líneas agregadas en código productivo de todos los módulos
added_main() { added_lines "${MAIN_KT[@]}"; }

# ¿El archivo es nuevo en el PR? (para decidir 🟠 archivo nuevo vs 🟡 archivo tocado)
is_new_file() { printf '%s\n' "$PR_NEW_FILES" | grep -qxF "$1"; }

# Módulo Gradle de un archivo: app · core:designsystem · core:ui · feature:home …
module_of() {
  case "$1" in
    app/*) echo "app" ;;
    core/*/*|feature/*/*) echo "$1" | cut -d/ -f1-2 | tr '/' ':' ;;
  esac
}

# Feature al que pertenece un archivo (vacío si es :app o core)
feature_of() { case "$1" in feature/*/*) echo "$1" | cut -d/ -f2 ;; esac; }

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

# Versión del archivo en la base (vacío si es nuevo): base_file <ruta>
base_file() { git show "$BASE_SNAPSHOT:$1" 2>/dev/null; }
