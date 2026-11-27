#!/usr/bin/env bash
# Prueba de humo del backend para el piloto (checklist PILOTO-5): confirma que lo que registra el sensei
# se guarda de verdad y que cada dojo solo ve sus alumnos. Usa el contrato de docs/api/kensho-api.yaml.
#
# Uso:
#   scripts/backend-smoke.sh crear        # inicia sesión, da de alta un alumno de prueba y lo vuelve a leer
#   scripts/backend-smoke.sh revisar      # (minutos u horas después) sesión nueva y busca al mismo alumno
#   scripts/backend-smoke.sh aislamiento  # con otra cuenta (otro dojo): el alumno NO debe verse (404)
#   scripts/backend-smoke.sh limpiar      # da de baja (INACTIVE) al alumno de prueba
#
# Credenciales: KENSHO_EMAIL / KENSHO_PASSWORD (y KENSHO_EMAIL_B / KENSHO_PASSWORD_B para aislamiento);
# si faltan, se piden sin mostrarlas. Nunca se imprimen ni se guardan; el token solo vive en memoria.
# URL: KENSHO_BASE_URL, o kensho.baseUrl.release de gradle.properties.
# Estado: el id del alumno de prueba se guarda en ~/.cache/kensho-smoke/student_id (fuera del repo).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
STATE_DIR="${XDG_CACHE_HOME:-$HOME/.cache}/kensho-smoke"
STATE_FILE="$STATE_DIR/student_id"
BASE_URL="${KENSHO_BASE_URL:-$(grep '^kensho.baseUrl.release=' "$ROOT/gradle.properties" | cut -d= -f2-)}"
BASE_URL="${BASE_URL%/}/"

ok() { printf '  \033[32m✔\033[0m %s\n' "$*"; }
fail() { printf '  \033[31m✘\033[0m %s\n' "$*" >&2; exit 1; }
info() { printf '\n\033[1m%s\033[0m\n' "$*"; }

command -v jq >/dev/null || fail "Falta jq (brew install jq)."
command -v curl >/dev/null || fail "Falta curl."

# api MÉTOD0 RUTA [CUERPO] [ENCABEZADO EXTRA] → deja el cuerpo en $BODY y el código en $CODE.
api() {
  local method="$1" path="$2" data="${3:-}" extra="${4:-}" out
  local args=(-sS --connect-timeout 10 --max-time 60 -X "$method" -H 'Accept: application/json' -w '\n%{http_code}')
  [[ -n "${TOKEN:-}" ]] && args+=(-H "Authorization: Bearer $TOKEN")
  [[ -n "$data" ]] && args+=(-H 'Content-Type: application/json' --data-binary "$data")
  [[ -n "$extra" ]] && args+=(-H "$extra")
  out="$(curl "${args[@]}" "${BASE_URL}${path}")" || fail "Sin conexión con ${BASE_URL}${path}"
  CODE="${out##*$'\n'}"
  BODY="${out%$'\n'*}"
}

# Muestra el error del API sin datos sensibles (code, message y campos del 422).
api_error() { jq -c '{code, message, fields}' <<<"$BODY" 2>/dev/null || printf '%.300s\n' "$BODY"; }

# login SUFIJO: usa KENSHO_EMAIL$SUFIJO / KENSHO_PASSWORD$SUFIJO y deja el token en $TOKEN.
login() {
  local suffix="${1:-}" email_var="KENSHO_EMAIL${1:-}" pass_var="KENSHO_PASSWORD${1:-}" email password
  email="${!email_var:-}"
  password="${!pass_var:-}"
  [[ -n "$email" ]] || read -r -p "Correo de la cuenta${suffix:+ B (otro dojo)}: " email
  [[ -n "$password" ]] || { read -r -s -p "Contraseña: " password; echo; }
  TOKEN=""
  api POST auth/login "$(jq -nc --arg e "$email" --arg p "$password" '{email: $e, password: $p}')"
  [[ "$CODE" == 200 ]] || { api_error; fail "Login respondió $CODE"; }
  TOKEN="$(jq -r '.accessToken // empty' <<<"$BODY")"
  [[ -n "$TOKEN" ]] || fail "El login no devolvió accessToken"
  ok "Sesión iniciada como $(jq -r '.user.instructorName // .user.id' <<<"$BODY")"
}

saved_id() {
  [[ -s "$STATE_FILE" ]] || fail "No hay alumno de prueba guardado; corre primero: $0 crear"
  cat "$STATE_FILE"
}

# Lee el alumno por id y en la lista (activos, pausados y bajas). Falla si no aparece en alguno.
expect_student() {
  local id="$1"
  api GET "students/$id"
  [[ "$CODE" == 200 ]] || { api_error; fail "GET students/$id respondió $CODE"; }
  ok "GET students/$id → 200 ($(jq -r '.. | .fullName? // .displayName? // empty' <<<"$BODY" | head -1))"
  api GET "students?status=ACTIVE&status=PAUSED&status=INACTIVE"
  [[ "$CODE" == 200 ]] || { api_error; fail "GET students respondió $CODE"; }
  jq -e --arg id "$id" 'any(.items[]; .id == $id)' <<<"$BODY" >/dev/null \
    || fail "El alumno no aparece en GET students ($(jq '.items | length' <<<"$BODY") alumnos)"
  ok "Aparece en GET students ($(jq '.items | length' <<<"$BODY") alumnos en total)"
}

crear() {
  info "1. Alta de un alumno de prueba en ${BASE_URL}"
  login
  api GET dojo/registration-setup
  [[ "$CODE" == 200 ]] || { api_error; fail "GET dojo/registration-setup respondió $CODE"; }
  local setup="$BODY" key schedule_id
  key="$(jq -r '.registrationId' <<<"$setup")"
  schedule_id="$(jq -r '(.schedules // [])[0].id // empty' <<<"$setup")"
  [[ -n "$schedule_id" ]] || fail "El dojo no tiene horarios; crea uno en la app antes de la prueba"
  ok "Setup recibido (folio $key, horario $schedule_id)"

  # Adulto (sin tutor), sin correo ni envío de credencial; "hoy" sale del asOf del API.
  local request
  request="$(jq -c --arg name "Prueba Piloto $(date +%Y%m%d-%H%M%S)" --arg schedule "$schedule_id" '
    ([.schedules[] | select(.id == $schedule)][0].discipline) as $sd
    | ([.disciplines[] | select(.code == $sd)][0] // .disciplines[0]) as $disc
    | {
        fullName: $name,
        birthDate: "1990-01-01",
        gender: "MALE",
        enrollment: {
          discipline: $disc.code,
          belt: $disc.belts[0].belt,
          stripes: 0,
          firstClassOn: (.asOf[0:10]),
          scheduleId: $schedule
        },
        contact: { phone: (.phoneCountryCode + "5500000000") },
        sendCredential: false
      }' <<<"$setup")"
  api POST students "$request" "Idempotency-Key: $key"
  [[ "$CODE" == 201 ]] || { api_error; fail "POST students respondió $CODE"; }
  local id
  id="$(jq -r '.id // .student.id // empty' <<<"$BODY")"
  [[ -n "$id" ]] || fail "El alta no devolvió id: $(printf '%.200s' "$BODY")"
  mkdir -p "$STATE_DIR" && printf '%s' "$id" >"$STATE_FILE"
  ok "Alumno creado: $id (guardado en $STATE_FILE)"

  info "2. Lectura inmediata con la misma sesión"
  expect_student "$id"

  info "3. Reintento idempotente (misma llave y cuerpo → mismo alumno, sin duplicar)"
  api POST students "$request" "Idempotency-Key: $key"
  if [[ "$CODE" == 201 && "$(jq -r '.id // .student.id // empty' <<<"$BODY")" == "$id" ]]; then
    ok "Mismo 201 y mismo id"
  else
    printf '  \033[33m!\033[0m Respondió %s con otro id o error (revisar idempotencia): ' "$CODE"; api_error
  fi

  printf '\nSiguiente: espera unos minutos (idealmente más de 15, para que el servidor se reinicie) y corre:\n  %s revisar\n' "$0"
}

revisar() {
  local id
  id="$(saved_id)"
  info "Persistencia: sesión nueva y búsqueda de $id"
  login
  expect_student "$id"
  ok "El alumno sigue guardado: el backend persiste los datos"
}

aislamiento() {
  local id
  id="$(saved_id)"
  info "Aislamiento: otra cuenta (otro dojo) no debe ver $id"
  login _B
  api GET "students/$id"
  [[ "$CODE" == 404 ]] || fail "GET students/$id con la cuenta B respondió $CODE (se esperaba 404)"
  ok "GET students/$id → 404"
  api GET "students?status=ACTIVE&status=PAUSED&status=INACTIVE"
  [[ "$CODE" == 200 ]] || { api_error; fail "GET students respondió $CODE"; }
  jq -e --arg id "$id" 'any(.items[]; .id == $id)' <<<"$BODY" >/dev/null \
    && fail "El alumno de la cuenta A aparece en la lista de la cuenta B"
  ok "No aparece en la lista de la cuenta B"
}

limpiar() {
  local id
  id="$(saved_id)"
  info "Baja del alumno de prueba $id"
  login
  api PUT "students/$id/membership" '{"status":"INACTIVE","reason":"Prueba de persistencia del piloto"}'
  [[ "$CODE" == 200 ]] || { api_error; fail "PUT membership respondió $CODE"; }
  ok "Dado de baja (el API no tiene DELETE; queda como baja en el historial del dojo)"
  rm -f "$STATE_FILE"
}

case "${1:-}" in
  crear) crear ;;
  revisar) revisar ;;
  aislamiento) aislamiento ;;
  limpiar) limpiar ;;
  *) sed -n '2,14p' "$0" | sed 's/^# \{0,1\}//'; exit 2 ;;
esac
