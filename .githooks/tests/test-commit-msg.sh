#!/usr/bin/env bash
# ==============================================================================
# test-commit-msg.sh — suite de pruebas del hook `commit-msg`.
#
# Ejecuta ~60 casos (válidos e inválidos) contra el hook usando archivos
# temporales — NO hace commits reales, NO toca el repo, NO modifica git state.
#
# Uso:
#   ./.githooks/tests/test-commit-msg.sh              # ejecuta toda la suite
#   ./.githooks/tests/test-commit-msg.sh -v           # modo verbose (imprime la
#                                                       salida real del hook por
#                                                       cada caso)
#
# Exit code: 0 si todo pasó, 1 si al menos un caso falló.
# ==============================================================================

set -u

# Resolver rutas absolutas independientemente del cwd desde donde se invoca.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
HOOK="${SCRIPT_DIR}/../commit-msg"

if [[ ! -f "$HOOK" ]]; then
  echo "❌ No se encontró el hook en: $HOOK" >&2
  exit 2
fi

VERBOSE=0
if [[ "${1:-}" == "-v" || "${1:-}" == "--verbose" ]]; then
  VERBOSE=1
fi

# ------------------------------------------------------------------------------
# Framework mínimo de aserción
# ------------------------------------------------------------------------------
PASS=0
FAIL=0
FAILED_TESTS=()

# Ejecuta el hook contra `msg` y verifica que el exit-code coincida con `expected`
# ('PASS' = hook aceptó / exit 0, 'FAIL' = hook rechazó / exit != 0).
run_case() {
  local name="$1" expected="$2" msg="$3"
  local tmp
  tmp="$(mktemp)"
  # -e permite el uso de secuencias \n, \r, etc. en el mensaje.
  printf '%b' "$msg" > "$tmp"

  local output exit_code
  output="$(bash "$HOOK" "$tmp" 2>&1)"
  exit_code=$?
  rm -f "$tmp"

  local got
  if [[ "$exit_code" -eq 0 ]]; then got="PASS"; else got="FAIL"; fi

  if [[ "$got" == "$expected" ]]; then
    printf "  ✅ %-6s  %s\n" "$got" "$name"
    PASS=$((PASS + 1))
  else
    printf "  ❌ esperado=%s got=%s  %s\n" "$expected" "$got" "$name"
    FAIL=$((FAIL + 1))
    FAILED_TESTS+=("$name")
    if [[ "$VERBOSE" -eq 0 ]]; then
      # En modo no-verbose, imprimimos siempre la salida cuando algo falla,
      # para que el error se pueda diagnosticar sin re-ejecutar con -v.
      echo "     ┌─ salida del hook:"
      echo "$output" | sed 's/^/     │ /'
      echo "     └─"
    fi
  fi

  if [[ "$VERBOSE" -eq 1 ]]; then
    echo "     ┌─ input:"
    echo "$msg" | sed 's/^/     │ /'
    echo "     ├─ output:"
    echo "$output" | sed 's/^/     │ /'
    echo "     └─ exit=$exit_code"
  fi
}

section() {
  echo ""
  echo "━━━ $1 ━━━"
}

# ------------------------------------------------------------------------------
# 1. Casos VÁLIDOS — el hook debe aceptar
# ------------------------------------------------------------------------------
section "Casos VÁLIDOS (debe aceptar)"

# Tipos individuales
run_case "tipo feat"                PASS "feat: agregar pantalla de perfil"
run_case "tipo fix"                 PASS "fix: corregir filtro de búsqueda en alumnos"
run_case "tipo refactor"            PASS "refactor: extraer secciones de PaymentsSections"
run_case "tipo test"                PASS "test: cubrir PaymentDetailViewModel"
run_case "tipo docs"                PASS "docs: actualizar guía de contribución"
run_case "tipo chore"               PASS "chore: eliminar código comentado de MainActivity"
run_case "tipo build"               PASS "build: subir lifecycle a la versión 2.9.0"
run_case "tipo revert"              PASS "revert: revertir cambio de navegación de pagos"

# Ámbito opcional
run_case "con ámbito"               PASS "feat(payments): agregar detalle de cobro"
run_case "ámbito con guion"         PASS "fix(check-in): corregir conteo de asistentes"
run_case "ámbito con dígitos"       PASS "refactor(m3): migrar chips a material 3"

# Ticket opcional al final
run_case "ticket al final"          PASS "fix(students): corregir filtro de búsqueda [APP-7]"
run_case "ticket sin ámbito"        PASS "build: subir lifecycle a la versión 2.9.0 [APP-21]"
run_case "ticket con id largo"      PASS "feat: agregar pantalla de perfil [APP-999999]"

# Auto-normalización: EXACTAMENTE 1 espacio tras "<tipo>(<ámbito>):"
run_case "auto-normaliza 0 espacios"   PASS "feat:agregar pantalla de perfil"
run_case "auto-normaliza N espacios"   PASS "feat(payments):   agregar detalle de cobro"
run_case "auto-normaliza tab"          PASS "feat:\tagregar pantalla de perfil"

# Estructura con cuerpo separado
run_case "con body separado por línea vacía" \
    PASS "fix(payments): evitar fecha del sistema en el resumen\n\nLa fecha de referencia ahora llega del repositorio.\nResuelve TD-005.\n\nRefs: APP-12"

# Caracteres especiales en la descripción
run_case "descripción con acentos"       PASS "feat: añadir sección de exámenes próximos"
run_case "descripción con emoji"         PASS "feat: agregar 🏆 insignia de logro"

# Casos que el hook debe SALTAR automáticamente
run_case "merge commit (Git)"            PASS "Merge branch 'develop' into feature/payments-detail"
run_case "revert generado por Git"       PASS "Revert \"feat: agregar algo nuevo aquí\"\n\nThis reverts commit abc123."
run_case "fixup!"                        PASS "fixup! feat: agregar algo nuevo aquí"
run_case "squash!"                       PASS "squash! feat: agregar algo nuevo aquí"
run_case "amend!"                        PASS "amend! feat: agregar algo nuevo aquí"

# Robustez del parser
run_case "comentarios Git iniciales"     PASS "# comentario git\nfeat: agregar pantalla de perfil\n# otro comentario"
run_case "líneas blancas iniciales"      PASS "\n\nfeat: agregar pantalla de perfil"
run_case "CRLF (Windows/WSL)"            PASS "feat: agregar pantalla de perfil\r\n"

# Bypass por marca
run_case "bypass por marca [skip-validation]" \
    PASS "mensaje raro sin formato [skip-validation]"

# ------------------------------------------------------------------------------
# 2. Casos INVÁLIDOS — el hook debe rechazar
# ------------------------------------------------------------------------------
section "Casos INVÁLIDOS (debe rechazar)"

# Vacíos / mal formados
run_case "mensaje vacío"                       FAIL ""
run_case "solo comentarios"                    FAIL "# comentario\n# otro"
run_case "sin tipo"                            FAIL "Add payment detail feature with ViewModel"
run_case "sin dos puntos"                      FAIL "feat agregar pantalla de perfil"

# Tipos inválidos
run_case "tipo inválido 'feature'"             FAIL "feature: agregar pantalla de perfil"
run_case "tipo inválido 'update'"              FAIL "update: actualizar pantalla de perfil"
run_case "tipo en mayúscula 'FEAT'"            FAIL "FEAT: agregar pantalla de perfil"
run_case "tipo fuera de lista 'perf'"          FAIL "perf: cachear formatters de moneda"
run_case "tipo fuera de lista 'style'"         FAIL "style: aplicar formato de ktlint"
run_case "tipo fuera de lista 'ci'"            FAIL "ci: agregar pipeline de bitbucket"

# Sufijo ! de breaking change
run_case "feat! rechazado"                     FAIL "feat!: cambiar contrato de InputField"
run_case "feat(scope)! rechazado"              FAIL "feat(components)!: cambiar contrato de InputField"

# Ámbito mal formado
run_case "ámbito en mayúscula"                 FAIL "feat(Payments): agregar detalle de cobro"
run_case "ámbito con espacio"                  FAIL "feat(pay ments): agregar detalle de cobro"
run_case "ámbito vacío"                        FAIL "feat(): agregar detalle de cobro"
run_case "espacio antes del ámbito"            FAIL "feat (payments): agregar detalle de cobro"

# Ticket
run_case "ticket al inicio (formato antiguo)"  FAIL "feat: [APP-1] agregar pantalla de perfil"
run_case "ticket en medio"                     FAIL "fix: corregir [APP-7] filtro de búsqueda"
run_case "prefijo no aceptado 'JIRA'"          FAIL "feat: agregar pantalla de perfil [JIRA-123]"
run_case "prefijo no aceptado 'INVEXONE'"      FAIL "feat: agregar pantalla de perfil [INVEXONE-1]"
run_case "prefijo en minúscula 'app'"          FAIL "feat: agregar pantalla de perfil [app-1]"
run_case "ticket sin dígitos"                  FAIL "feat: agregar pantalla de perfil [APP-]"

# Descripción
run_case "descripción < 10 chars"              FAIL "feat: corto"
run_case "descripción empieza mayúscula"       FAIL "feat: Agregar pantalla de perfil"
run_case "descripción termina con punto"       FAIL "feat: agregar pantalla de perfil."
run_case "punto antes del ticket"              FAIL "feat: agregar pantalla de perfil. [APP-2]"

# Formato del subject
run_case "asunto > 100 caracteres"             FAIL "feat: esta es una descripción extremadamente larga que supera los cien caracteres permitidos aaaaaaaaaaa"
run_case "body sin línea separadora"           FAIL "feat: agregar pantalla de perfil\ncuerpo pegado sin línea en blanco"
run_case "espacio antes del tipo"              FAIL " feat: agregar pantalla de perfil"

# ------------------------------------------------------------------------------
# 3. Bypass por variable de entorno — caso especial (usa `env`, no `bash`)
# ------------------------------------------------------------------------------
section "Bypass por variable de entorno"

tmp="$(mktemp)"
echo "mensaje totalmente inválido sin formato" > "$tmp"
if SKIP_COMMIT_MSG_HOOK=1 bash "$HOOK" "$tmp" >/dev/null 2>&1; then
  echo "  ✅ PASS    SKIP_COMMIT_MSG_HOOK=1 salta la validación"
  PASS=$((PASS + 1))
else
  echo "  ❌ FAIL    SKIP_COMMIT_MSG_HOOK=1 no está saltando"
  FAIL=$((FAIL + 1))
  FAILED_TESTS+=("SKIP_COMMIT_MSG_HOOK=1")
fi
rm -f "$tmp"

# ------------------------------------------------------------------------------
# 4. Auto-normalización del subject — verifica que el archivo SE REESCRIBE
#    cuando el commit trae 0, N espacios o un tab tras "<tipo>(<ámbito>):".
# ------------------------------------------------------------------------------
section "Auto-normalización del archivo de commit"

assert_normalized_subject() {
  local name="$1" input="$2" expected_line="$3" line_no="${4:-1}"
  local tmp
  tmp="$(mktemp)"
  printf '%b' "$input" > "$tmp"
  bash "$HOOK" "$tmp" >/dev/null 2>&1
  local actual_line
  actual_line="$(sed -n "${line_no}p" "$tmp")"
  if [[ "$actual_line" == "$expected_line" ]]; then
    printf "  ✅ PASS    %s\n" "$name"
    PASS=$((PASS + 1))
  else
    printf "  ❌ FAIL    %s\n" "$name"
    echo "     esperado (línea $line_no): $expected_line"
    echo "     obtenido (línea $line_no): $actual_line"
    FAIL=$((FAIL + 1))
    FAILED_TESTS+=("$name (normalización)")
  fi
  rm -f "$tmp"
}

assert_normalized_subject "0 espacios → se agrega 1" \
  "feat:agregar pantalla de perfil" \
  "feat: agregar pantalla de perfil"

assert_normalized_subject "N espacios → se colapsan a 1" \
  "fix(students):   corregir filtro de búsqueda [APP-7]" \
  "fix(students): corregir filtro de búsqueda [APP-7]"

assert_normalized_subject "tab → se convierte a 1 espacio" \
  "refactor(payments):\textraer secciones de pagos" \
  "refactor(payments): extraer secciones de pagos"

assert_normalized_subject "1 espacio (canónico) → archivo intacto" \
  "feat: agregar pantalla de perfil" \
  "feat: agregar pantalla de perfil"

assert_normalized_subject "body → solo se normaliza el subject" \
  "feat:agregar pantalla de perfil\n\ncuerpo con feat:x no se toca" \
  "feat: agregar pantalla de perfil"

assert_normalized_subject "body → la línea del cuerpo NO se toca" \
  "feat:agregar pantalla de perfil\n\ncuerpo con feat:x no se toca" \
  "cuerpo con feat:x no se toca" 3

assert_normalized_subject "comentarios Git iniciales → subject en línea 2 normalizado" \
  "# comentario git\nfeat:agregar pantalla de perfil" \
  "feat: agregar pantalla de perfil" 2

# ------------------------------------------------------------------------------
# Reporte final
# ------------------------------------------------------------------------------
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
TOTAL=$((PASS + FAIL))
if [[ "$FAIL" -eq 0 ]]; then
  echo "  ✅ TODOS LOS TESTS PASARON ($PASS/$TOTAL)"
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  exit 0
else
  echo "  ❌ ${FAIL} TEST(S) FALLARON — total $PASS/$TOTAL correctos"
  echo ""
  echo "  Tests fallidos:"
  for t in "${FAILED_TESTS[@]}"; do
    echo "    - $t"
  done
  echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
  exit 1
fi
