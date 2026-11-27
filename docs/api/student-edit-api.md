# API para editar los datos del alumno

| Dato | Valor |
|------|-------|
| Pantalla | Ficha del alumno en modo edición (desde el perfil, `feature/students/profile`) y panel web (`#/alumnos/{id}/ficha`, acciones de estado en `#/alumnos/{id}`). Todavía no hay diseño en Figma |
| Estado | **Implementado** (backend, tests, Postman «Editar alumno» y panel web) |
| Depende de | Alta ([student-registration-api.md](student-registration-api.md)), perfil ([student-profile-api.md](student-profile-api.md)), lista ([students-api.md](students-api.md)), pase de lista ([checkin-api.md](checkin-api.md)), pagos ([payments-api.md](payments-api.md)), configuración del dojo ([dojo-config-api.md](dojo-config-api.md)) |
| Fecha | 2026-11-10 |

---

## 1. Resumen

| # | Endpoint | Para qué |
|---|----------|----------|
| 1 | `GET /students/{studentId}/record` | Leer la ficha editable (lo capturado en el alta, que el perfil no devuelve) |
| 2 | `PATCH /students/{studentId}/record` | Corregir datos personales, tutor, contacto, emergencia y horario |
| 3 | `PUT /students/{studentId}/membership` | Pausar, dar de baja o reactivar |
| 4 | `GET /students?status=…` | Encontrar a los pausados y dados de baja (sin el parámetro, solo activos, como antes) |

**Fuera del alcance:**
- **Corrección de rango** (cinta, grado, rayas): queda para después.
- **Cambio de disciplina o programa:** se hace con una baja y un alta nueva.
- **Primera clase (`firstClassOn`):** no se edita.
- **Promoción por examen:** es el flujo de graduación.
- **Borrado definitivo:** se usa la baja.
- **Foto:** pendiente hasta tener almacenamiento de imágenes.
- **Historial de estados:** solo se guarda el último cambio (fecha y motivo).

### Decisiones

| Tema | Decisión |
|------|----------|
| Recurso | La ficha es un recurso aparte (`/record`, `no-store`). Así los datos médicos y de contacto no entran en el perfil, que se cachea 60 s |
| Horario | Se cambia en el mismo `PATCH` que los datos personales |
| Primera clase | No se edita |
| Mensualidad al reactivar | Se prorratea como la de un alumno nuevo (incluida la regla de la última semana) |
| Exámenes y eventos futuros | La baja cancela sus cargos pendientes. La pausa solo los cancela si se pide (`cancelUpcomingEvents: true`) |
| Bajas | Una baja no se edita hasta reactivarla. Los pausados sí se editan. Las bajas no bloquean el borrado de un horario: se quedan sin horario |
| Historial | Solo el último cambio |

---

## 2. `GET /students/{studentId}/record`

`200`, `Cache-Control: no-store`, `ETag` débil.

```json
{
  "asOf": "2026-11-10T18:05:00-06:00",
  "id": "st-6f1c...",
  "fullName": "Kenji Sato",
  "birthDate": "2012-01-15",
  "age": 14,
  "gender": "MALE",
  "medicalNotes": "Asma leve; trae inhalador.",
  "photoUrl": null,
  "membership": { "status": "ACTIVE", "enrolledOn": "2026-09-01", "changedOn": null, "reason": null },
  "enrollment": {
    "discipline": "KARATE",
    "programName": "Shotokan Karate",
    "scheduleId": "karate-mon-wed-1730",
    "firstClassOn": "2026-09-03"
  },
  "guardian": { "name": "Carlos Sato", "relation": "FATHER" },
  "contact": { "phone": "+525543219876", "email": "familia.sato@correo.mx" },
  "emergencyContact": { "name": "Yumi Sato", "phone": "+525598765432" },
  "scheduleOptions": [
    { "id": "karate-mon-wed-1730", "weekdays": ["MONDAY", "WEDNESDAY"], "startsAt": "17:30", "endsAt": "18:45", "description": "Tatami B • Cadetes", "available": true },
    { "id": "karate-tue-thu-1830", "weekdays": ["TUESDAY", "THURSDAY"], "startsAt": "18:30", "endsAt": "19:45", "description": null, "available": false }
  ]
}
```

| Campo | Regla |
|-------|-------|
| `birthDate` | Se expone aquí porque se edita. La lista y el perfil siguen mandando solo la edad |
| `gender`, `contact.phone` | Pueden ser `null` en alumnos creados antes de `POST /students` |
| `membership.changedOn`, `reason` | Último cambio de estado (§4); `null` si nunca ha cambiado |
| `scheduleOptions` | Horarios de su disciplina. `available` cuenta a los alumnos activos sin contarlo a él. Un alumno activo siempre ve su horario actual como disponible |
| `ETag` | Cubre los datos del alumno, no el cupo de los horarios: un `If-Match` solo falla si alguien cambió esta ficha |

`404 STUDENT_NOT_FOUND` si no existe o es de otro dojo. Un alumno en pausa o de baja sí se puede leer.

---

## 3. `PATCH /students/{studentId}/record`

Solo cambian los campos presentes. `null` borra los opcionales.

```json
{
  "contact": { "phone": "+525511112222", "email": null },
  "scheduleId": "karate-tue-thu-1830"
}
```

| Campo | Regla (igual que el alta salvo lo marcado) |
|-------|--------------------------------------------|
| `fullName` | Texto 1-120 |
| `birthDate` | Fecha válida, edad 3-100 |
| `gender` | `MALE` o `FEMALE` |
| `medicalNotes` | Texto ≤ 1000; `null` o `""` lo borra |
| `guardian` | `{ name, relation }` completo, o `null` |
| `contact` | Objeto completo: `phone` obligatorio (E.164; +52 con 10 dígitos). `email` es opcional y, si no viene, se borra |
| `emergencyContact` | `{ name, phone }` completo, o `null` |
| `scheduleId` | **Nuevo.** Horario de su disciplina con cupo (sin contarlo a él); si no, `422 scheduleId NOT_AVAILABLE`. Mandar el actual no se valida. Un alumno en pausa no ocupa lugar, así que el cupo se revisa al reactivarlo |
| `enrollment`, `discipline`, `belt`, `stripes`, `rank`, `firstClassOn`, `status`, `membership` | **No se aceptan** → `422 NOT_EDITABLE` |

**Tutor por edad:** si cambia `birthDate` o `guardian`, se revisa con la ficha resultante. Si es menor de 18 sin tutor, responde `422 guardian.name REQUIRED`. Un adulto puede quitar el tutor en el mismo cambio en que corrige la fecha.

**Duplicados:** si cambia el nombre o la fecha de nacimiento y otro alumno del dojo queda con el mismo nombre normalizado y la misma fecha, responde `409 STUDENT_ALREADY_EXISTS`.

**Bajas:** un alumno `INACTIVE` no se edita: `409 STUDENT_INACTIVE`. Primero se reactiva.

**Concurrencia:** `If-Match` con el `ETag` del `GET record`, opcional. Si no coincide, responde `412 PRECONDITION_FAILED`: la app recarga la ficha y vuelve a aplicar sus cambios. Sin `If-Match`, gana el último en guardar.

**Respuesta:** `200` con el mismo cuerpo del `GET record` y el nuevo `ETag`. Un cuerpo vacío (`{}`) responde 200 sin cambios.

**Efectos en otras APIs:**
- **Pase de lista:** el cambio de horario aplica desde hoy. Las marcas pasadas se quedan en el horario anterior.
- **Pagos:** el recibo usa el teléfono al momento de leerlo, así que un teléfono corregido aparece en los siguientes recibos.

---

## 4. `PUT /students/{studentId}/membership`

```json
{ "status": "PAUSED", "reason": "Lesión de rodilla", "cancelUpcomingEvents": false }
```

| Campo | Regla |
|-------|-------|
| `status` | `ACTIVE`, `PAUSED` (se espera que vuelva) o `INACTIVE` (baja) |
| `reason` | Opcional, 1-200 caracteres (más largo → `OUT_OF_RANGE`). Se guarda con la fecha del cambio |
| `scheduleId` | Solo al reactivar, para pasarlo a otro horario con cupo. Con otro estado → `422 INVALID_FORMAT` |
| `cancelUpcomingEvents` | Booleano, opcional. Solo aplica a `PAUSED`; la baja siempre cancela |

**Idempotente:** mandar el estado actual responde `200` sin cambiar nada (ni la fecha ni el motivo).

**Respuesta:** `200` con el `GET record` más `canceledCharges` (número de cargos de eventos cancelados).

| Transición | Efectos |
|------------|---------|
| → `PAUSED` / `INACTIVE` | Antes del cambio se generan las mensualidades que ya le tocaban. Desde hoy sale de la lista de alumnos, del pase de lista, del cupo del horario y de los conteos de alumnos activos, y deja de generar mensualidades. Lo que ya debía se queda pendiente |
| → `INACTIVE`, o `PAUSED` con `cancelUpcomingEvents: true` | Se cancelan sus cargos **pendientes** de exámenes y eventos de hoy en adelante; los pagados se quedan. Pierde su próximo examen, salvo que ya lo haya pagado |
| → `ACTIVE` | Vuelve a la lista y al pase de lista desde hoy. Necesita cupo en su horario o un `scheduleId` con cupo. Sin cupo y sin `scheduleId` → `409 SCHEDULE_FULL`; un `scheduleId` lleno o de otra disciplina → `422 scheduleId NOT_AVAILABLE`. Si se quedó sin horario (se borró mientras estaba de baja) y su disciplina tiene horarios → `422 scheduleId REQUIRED`. La mensualidad del mes en que regresa se prorratea desde ese día, como la de un alumno nuevo (si regresa en la última semana, ese mes no paga) |

**Horarios y bajas:** `DELETE /dojo/schedules/{id}` y el cambio de disciplina de un horario solo se bloquean (`409 SCHEDULE_IN_USE`) por alumnos activos o en pausa, o por asistencias registradas en ese horario. Las bajas asignadas quedan con `scheduleId: null`. `DELETE /dojo/programs/{discipline}` sigue bloqueado por alumnos de cualquier estado: la baja conserva su disciplina y su rango, que viven en el programa.

**Columnas en `students`:** `status_changed_on` (date) y `status_reason` (text, 1-200).

### `GET /students?status=`

`status` es repetible (`ACTIVE`, `PAUSED`, `INACTIVE`). Sin el parámetro devuelve solo `ACTIVE`, como antes. Un valor desconocido responde `422 status INVALID_FORMAT`. Cada `StudentSummary` trae ahora `membership: { status }`.

---

## 5. Errores

| HTTP | `code` | Cuándo |
|------|--------|--------|
| `400` | `BAD_REQUEST` | El cuerpo no es un objeto JSON |
| `401` | `MISSING_TOKEN` / `INVALID_TOKEN` | Token ausente o vencido |
| `404` | `STUDENT_NOT_FOUND` | No existe o es de otro dojo |
| `409` | `STUDENT_ALREADY_EXISTS` | El nuevo nombre y fecha chocan con otro alumno |
| `409` | `STUDENT_INACTIVE` | `PATCH record` a un alumno dado de baja |
| `409` | `SCHEDULE_FULL` | Reactivar sin cupo en su horario |
| `412` | `PRECONDITION_FAILED` | `If-Match` no coincide con la ficha actual |
| `422` | `VALIDATION_FAILED` | `fields[]` con las rutas del alta (`contact.phone`, `guardian.name`, `scheduleId`…). Hay un código nuevo: `NOT_EDITABLE` |

---

## 6. Pendientes

- ~~**App Android:**~~ Hecho (2026-11-10): `TkdStudentRecordRepository` (`record()`, `update()`, `setMembership()`), pantalla «Editar ficha» (desde el perfil) que reutiliza las tarjetas del alta, y «Pausar», «Dar de baja» y «Reactivar» en la tarjeta «Estado del alumno» de esa pantalla (no en el perfil). La lista filtra por Activos / En pausa / Bajas con `?status=`.
