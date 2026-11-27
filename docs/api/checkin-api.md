# API para el pase de lista (Check-in)

| Dato | Valor |
|------|-------|
| Pantalla | `checkin/TkdCheckInScreen.kt` (pestaña **Registro**, Figma Prototipo-Demo nodo 1:919) |
| Estado actual de la app | Datos de `FakeTkdCheckInRepository` (prototipo en inglés), repositorio síncrono, VM con default a la *Fake* (sin Hilt), `LocalTime.now()` en el `UiState` (TD-KEN-005) |
| Estado del API | **Implementado**: §3.1–3.4 (`netlify/functions/attendance-*.ts`, `netlify/lib/attendance.ts`) |
| Depende de | Horarios de `dojo/config` ([dojo-config-api.md](dojo-config-api.md) §5) y alumnos asignados a cada horario en el alta ([student-registration-api.md](student-registration-api.md)) |
| Alimenta | Asistencia del perfil del alumno (`attendance.attended` en [student-profile-api.md](student-profile-api.md)) y la sesión en vivo del Resumen ([dashboard-api.md](dashboard-api.md) §2.4) |
| Fecha | 2026-11-09 |

---

## 1. Resumen y decisión

Una **sesión** es la ocurrencia de un horario en una fecha: el horario `mon-wed-1730` del lunes 9 de
noviembre es la sesión `mon-wed-1730/2026-11-09`. No hay tabla de "clases": la sesión existe porque el horario
tiene clase ese día de la semana. Solo se guarda una fila de la sesión si el instructor la cierra (§3.4).

| # | Endpoint | Para qué | Estado |
|---|----------|----------|--------|
| 1 | `GET /attendance/today` | Sesiones de hoy y cuál está en curso o es la siguiente (abre la pantalla en la correcta) | ✅ |
| 2 | `GET /attendance/sessions/{scheduleId}/{date}` | Sesión + roster completo con el estado de cada alumno | ✅ |
| 3 | `PUT /attendance/sessions/{scheduleId}/{date}/students/{studentId}` | Marcar presente, ausente, justificado o deshacer (idempotente) | ✅ |
| 4 | `POST /attendance/sessions/{scheduleId}/{date}/close` | "Completar pase de lista": cierra la sesión y deja ausentes a los no marcados | ✅ |
| 5 | Kiosco / QR (`POST /attendance/check-ins` con token del alumno) | Autoregistro en la entrada | P2 (fuera de alcance) |

---

## 2. Mapa pantalla → datos

| Se pinta | Modelo hoy | Viene del API | Notas |
|----------|------------|---------------|-------|
| Encabezado del dojo | `DojoHeader` (Fake) | `GET /me` (ver [README](README.md) §2) | Igual que Resumen, Alumnos y Perfil |
| "Tuesday Youth Karate" | `MatSession.title` | `session.programName` + `session.description` | La app arma el título; el horario no tiene "nombre" propio |
| "Mat A (Main)" | `MatSession.mat` | `session.description` | Texto libre del horario ("Tatami A • Sensei Tanaka") |
| ~~"Age 9–14"~~ | `MatSession.ageRange` | — | **Se quita del diseño** (§6.2) |
| 17:30 – 18:30 | `start`, `end` | `session.startsAt`, `session.endsAt` (`HH:mm`) + `date` | |
| "Sensei Marcus" | `instructor` | `session.instructorName` | Siempre `null` por ahora = el instructor de la cuenta (§6.3) |
| Cronómetro / "en curso" | `now: LocalTime.now()` | `asOf` + `session.status` | Elimina `LocalTime.now()` del `UiState` (TD-KEN-005) |
| Lista de alumnos | `roster: List<Attendee>` | `students[]` | Roster completo (§3.2) |
| Foto del alumno | `photo: @DrawableRes` | `students[].photoUrl` | URL → `TkdImage` (TD-KEN-012) |
| Cinta | `belt: BeltColor` | `students[].rank.belt` (+ `grade`) | Mismo `Rank` que la lista de alumnos |
| Presente 17:28 | `AttendanceStatus.Present(checkedInAt)` | `status: PRESENT`, `checkedInAt` (ISO con offset) | |
| "Not on mat" | `NotOnMat` | `status: PENDING` | Aún sin marcar (solo mientras la clase no termina) |
| "Sick notice via Parent" | `Excused(reason)` | `status: EXCUSED`, `note` | |
| — | — | `status: ABSENT` | Falta marcada, puesta al cerrar, o sin marca al terminar la clase |
| Contadores (presentes, ausentes, ocupación) | Derivados + `unlistedTotal/Present` | `summary.total`, `present`, `excused`, `absent`, `pending` | Con el roster completo, `unlisted*` desaparece |
| Filtros Todos / Presentes / Ausentes | Local | — | Filtran en la app |
| "Completar" | `completed` | `session.status == CLOSED`, `session.closedAt` | §3.4 |

---

## 3. Endpoints

Todos requieren `Authorization: Bearer <accessToken>` y responden `Cache-Control: no-store`: el estado y los
contadores cambian durante la clase. Las fechas son del dojo (`KENSHO_TIME_ZONE`, por defecto `America/Mexico_City`).

### Objeto `Session`

Mismo objeto en §3.1 (más `summary`), §3.2 y §3.4.

```json
{
  "id": "mon-wed-1730/2026-11-09",
  "scheduleId": "mon-wed-1730",
  "date": "2026-11-09",
  "discipline": "KARATE",
  "programName": "Shotokan Karate",
  "description": "Tatami B • Sensei Tanaka",
  "instructorName": null,
  "startsAt": "17:30",
  "endsAt": "18:45",
  "status": "IN_PROGRESS",
  "closedAt": null
}
```

| Campo | Regla |
|-------|-------|
| `status` | `CLOSED` si se cerró con §3.4; si no, según `asOf`: `UPCOMING` (antes de `startsAt` o fecha futura), `IN_PROGRESS` (entre `startsAt` y `endsAt`), `FINISHED` (después de `endsAt` o fecha pasada) |
| `closedAt` | ISO con offset del dojo; `null` si no se cerró a mano |
| `instructorName` | Siempre `null` por ahora: el dojo tiene un solo instructor (la cuenta) |

### 3.1 `GET /attendance/today`

Sesiones del día del dojo, en orden de inicio, con la sugerida para abrir.

```json
{
  "asOf": "2026-11-09T17:38:00-06:00",
  "date": "2026-11-09",
  "currentSessionId": "mon-wed-1730/2026-11-09",
  "sessions": [
    {
      "id": "mon-wed-1730/2026-11-09",
      "scheduleId": "mon-wed-1730",
      "date": "2026-11-09",
      "discipline": "KARATE",
      "programName": "Shotokan Karate",
      "description": "Tatami B • Sensei Tanaka",
      "instructorName": null,
      "startsAt": "17:30",
      "endsAt": "18:45",
      "status": "IN_PROGRESS",
      "closedAt": null,
      "summary": { "total": 22, "present": 16, "excused": 1, "absent": 0, "pending": 5 }
    },
    {
      "id": "mon-wed-fri-1900/2026-11-09",
      "scheduleId": "mon-wed-fri-1900",
      "date": "2026-11-09",
      "discipline": "TAEKWONDO",
      "programName": "Taekwondo WT",
      "description": null,
      "instructorName": null,
      "startsAt": "19:00",
      "endsAt": "20:30",
      "status": "UPCOMING",
      "closedAt": null,
      "summary": { "total": 14, "present": 0, "excused": 0, "absent": 0, "pending": 14 }
    }
  ]
}
```

| Campo | Regla |
|-------|-------|
| `currentSessionId` | La que está en curso; si ninguna, la siguiente de hoy; `null` si no quedan sesiones hoy |
| `sessions` | Vacío si hoy no hay horarios → la app muestra el estado "Sin clases hoy" |

### 3.2 `GET /attendance/sessions/{scheduleId}/{date}`

Roster **completo** de la sesión. Un horario tiene a lo sumo 1000 alumnos (cupo máximo del API), así que no se
pagina: la app ya no necesita `unlistedTotal`/`unlistedPresent`.

```json
{
  "asOf": "2026-11-09T17:38:00-06:00",
  "session": { "id": "mon-wed-1730/2026-11-09", "...": "objeto Session" },
  "summary": { "total": 3, "present": 1, "excused": 1, "absent": 0, "pending": 1 },
  "students": [
    {
      "id": "st-kenji-sato",
      "displayName": "Kenji Sato",
      "photoUrl": null,
      "rank": { "discipline": "KARATE", "belt": "GREEN", "grade": { "system": "KYU", "value": 7 }, "stripes": 2 },
      "status": "PRESENT",
      "checkedInAt": "2026-11-09T17:28:00-06:00",
      "note": null
    },
    {
      "id": "st-9f1c2d3e-4b5a-4c6d-8e7f-0a1b2c3d4e5f",
      "displayName": "Taro Tanaka",
      "photoUrl": null,
      "rank": { "discipline": "KARATE", "belt": "WHITE", "grade": { "system": "KYU", "value": 10 }, "stripes": 0 },
      "status": "PENDING",
      "checkedInAt": null,
      "note": null
    },
    {
      "id": "st-zack-taylor",
      "displayName": "Zack Taylor",
      "photoUrl": null,
      "rank": { "discipline": "KARATE", "belt": "YELLOW", "grade": { "system": "KYU", "value": 9 }, "stripes": 1 },
      "status": "EXCUSED",
      "checkedInAt": null,
      "note": "Aviso de enfermedad del tutor"
    }
  ]
}
```

| Campo | Regla |
|-------|-------|
| `{date}` | `YYYY-MM-DD`; debe caer en un día del horario (`weekdays`). Las fechas futuras se pueden leer (todo `PENDING`), pero no marcar |
| `students` | Alumnos **activos** asignados al horario que ya habían empezado (`firstClassOn`, o la inscripción, `<= date`), **más** cualquier alumno con marca en esa sesión aunque después haya cambiado de horario o de estado (así no se pierde el historial). Orden alfabético por `displayName` (es-MX) |
| `students[].id` | Mismo id de `GET /students` (`st-…`) |
| `status` | `PRESENT`, `ABSENT`, `EXCUSED` o `PENDING`. Sin marca: `PENDING` mientras la sesión es `UPCOMING`/`IN_PROGRESS`; `ABSENT` cuando es `FINISHED`/`CLOSED` |
| `checkedInAt` | Solo con `PRESENT` |
| `note` | Solo con `EXCUSED` |
| `rank` | Mismo objeto que `GET /students` (código desconocido → la app lo descarta, como en la lista) |

### 3.3 `PUT /attendance/sessions/{scheduleId}/{date}/students/{studentId}`

Idempotente: reenviar el mismo estado no duplica ni cambia la hora de llegada. Responde el alumno actualizado y el
`summary` nuevo para que la app no tenga que recargar el roster.

```json
{ "status": "PRESENT" }
```

```json
{ "status": "EXCUSED", "note": "Aviso de enfermedad del tutor" }
```

Respuesta `200`:

```json
{
  "student": {
    "id": "st-9f1c2d3e-4b5a-4c6d-8e7f-0a1b2c3d4e5f",
    "displayName": "Taro Tanaka",
    "photoUrl": null,
    "rank": { "discipline": "KARATE", "belt": "WHITE", "grade": { "system": "KYU", "value": 10 }, "stripes": 0 },
    "status": "PRESENT",
    "checkedInAt": "2026-11-09T17:40:00-06:00",
    "note": null
  },
  "summary": { "total": 3, "present": 2, "excused": 1, "absent": 0, "pending": 0 }
}
```

| Campo | Regla |
|-------|-------|
| `status` | `PRESENT`, `ABSENT`, `EXCUSED`, o `PENDING` para deshacer (borra la marca; si la clase ya terminó, el alumno se muestra `ABSENT`) |
| `checkedInAt` | Lo pone el servidor con su hora al pasar a `PRESENT`; se conserva si ya estaba presente |
| `note` | Opcional, hasta 200 caracteres; solo se guarda con `EXCUSED` (con otro estado se ignora) |
| Fechas | Se puede marcar hoy (aunque la clase no haya empezado: llegadas tempranas) y hasta **30 días atrás**. Futuras → `409 SESSION_NOT_STARTED`; más antiguas → `409 SESSION_LOCKED` |
| Sesión cerrada | Se puede seguir corrigiendo (p. ej. para justificar después) |

**Efecto en el alumno** (en la misma transacción):

- `PRESENT` y `EXCUSED` suman 1 a `attendance.attended` (conteo del examen); `ABSENT` y `PENDING` no. Cambiar de un
  estado a otro ajusta el conteo en consecuencia.
- Al alcanzar `attendance.required`, `examStatus` pasa de `CLASSES_REMAINING` a `READY`. Si se deshace la marca que
  cruzó el umbral, regresa a `CLASSES_REMAINING`. Los demás estados del examen no se tocan.
- La racha (`streakWeeks`), el promedio semanal y las horas del perfil solo cuentan `PRESENT`.

### 3.4 `POST /attendance/sessions/{scheduleId}/{date}/close`

Sin cuerpo. Guarda como `ABSENT` a los no marcados, fija `closedAt` y responde la sesión completa (mismo cuerpo
que §3.2) con `status: CLOSED`. Repetirlo conserva el primer cierre.

- No hace falta cerrar para que los no marcados salgan como ausentes: al terminar la clase el API ya los muestra
  `ABSENT`. El cierre sirve para terminar antes de la hora y para que las faltas queden guardadas.
- No se puede cerrar una clase de hoy que todavía no empieza ni una fecha futura (`409 SESSION_NOT_STARTED`), ni
  una de hace más de 30 días (`409 SESSION_LOCKED`).

### 3.5 Errores

Formato `{ code, message }` de siempre (ADR-020).

| HTTP | `code` | Cuándo | Qué hace la app |
|------|--------|--------|-----------------|
| `400` | `BAD_REQUEST` | Cuerpo que no es JSON | Mensaje genérico |
| `401` | `MISSING_TOKEN` / `INVALID_TOKEN` | Sin token o vencido | Renueva el token |
| `404` | `SESSION_NOT_FOUND` | El horario no existe (o es de otro dojo), `{date}` es inválida o no cae en sus días | Recarga `GET /attendance/today` |
| `404` | `STUDENT_NOT_IN_SESSION` | El alumno no está en el roster de la sesión (o es de otro dojo) | Recarga el roster |
| `409` | `SESSION_NOT_STARTED` | Se marca una fecha futura, o se cierra una sesión que no ha empezado | Mensaje; el control queda deshabilitado |
| `409` | `SESSION_LOCKED` | La sesión es de hace más de 30 días | Mensaje; el control queda deshabilitado |
| `422` | `VALIDATION_FAILED` | `status` o `note` inválidos; trae `fields: [{ field, code }]` | Mensaje genérico |

---

## 4. Implementación en el API

| Pieza | Qué guarda / hace |
|-------|-------------------|
| `class_attendances` (se extendió) | Una fila por alumno y sesión: `schedule_id`, `status` (`PRESENT`/`ABSENT`/`EXCUSED`), `checked_in_at`, `note`, `marked_by`, `updated_at`. Único `(student_id, schedule_id, attended_on)`. Sin fila = `PENDING`. `minutes` = duración del horario. Las filas anteriores (sin `schedule_id`) siguen contando como `PRESENT` en el perfil |
| `attendance_sessions` (nueva) | Solo las sesiones cerradas a mano: `(user_id, schedule_id, session_date)`, `closed_at`, `closed_by` |
| `DELETE dojo/schedules/{id}` | Ahora también responde `409 SCHEDULE_IN_USE` si el horario tiene asistencias o sesiones cerradas |
| Migración | `netlify/database/migrations/20261109074028_checkin_attendance` |
| Seed local | Los alumnos demo quedan asignados a un horario de su disciplina para que el roster no salga vacío |
| Pruebas | `tests/attendance.test.ts` |
| Postman | Carpeta **Pase de lista** de la colección *Kensho API · Alumnos* |

---

## 5. Impacto en la app

| Pieza | Hoy | Cambio |
|-------|-----|--------|
| `TkdCheckInRepository` | 4 funciones síncronas con datos fijos | `suspend fun today()`, `session(scheduleId, date)`, `mark(...)`, `close(...)` → `TkdResult` |
| Implementaciones | `FakeTkdCheckInRepository` | Fake en es-MX con los horarios de `FakeTkdDojoConfigRepository` + `NetworkTkdCheckInRepository` y `@Provides` en `DataModule` |
| Modelo | `MatSession` con textos armados; `Attendee.photo: @DrawableRes`; `ageRange` | `MatSession(id, programName, description, start, end, status, closedAt)`; `photoUrl`; `AttendanceStatus` con `Absent` y `Pending`; `rank` en lugar de `belt`; **sin `ageRange`** |
| `TkdCheckInUiState` | `now = LocalTime.now()`, `unlisted*` | `asOf` del API; quitar `unlisted*`; `Loading/Error` + `onRetry()` |
| `TkdCheckInViewModel` | `= FakeTkdCheckInRepository()` | `@HiltViewModel`, marcado optimista (como "en tatami" del perfil del instructor) que regresa al valor anterior si falla |
| Tests | — | Mapper (fixtures `checkin_today.json`, `checkin_session.json`), repositorio con `MockWebServer`, VM (marcar, deshacer, error, cerrar, `SESSION_LOCKED`) |

---

## 6. Decisiones (preguntas resueltas)

1. **Cierre:** los no marcados pasan a ausentes automáticamente al terminar la clase (el API los muestra `ABSENT`).
   Se conserva "Completar" (§3.4) para que el instructor cierre la clase.
2. **Rango de edad:** se quita del diseño.
3. **Instructor por horario:** por ahora no; `instructorName` es `null` (= el instructor de la cuenta). Si un horario
   lo puede dar otro instructor, faltará `instructorId` en `dojo/schedules`.
4. **Asistencia fuera de horario:** no; el alumno solo aparece en el roster de su horario (ya no se repone la clase).
5. **Cuenta para el examen:** `PRESENT` y `EXCUSED` suman a `attendance.attended`; solo `PRESENT` suma a la racha y a
   las horas. Al completar las clases requeridas el alumno pasa a `READY` automáticamente.
6. **Ventana de corrección:** 30 días hacia atrás.
7. **Borrar un horario con historial:** no se permite (`409 SCHEDULE_IN_USE`).