# API de configuración del dojo

| Dato | Valor |
|------|-------|
| Para qué | Que un dojo recién registrado configure lo que necesita la ficha de alta: programas con su escalera de rangos, horarios y plan de cobro |
| Depende de | `GET /dojo/registration-setup` y `POST /students` ([student-registration-api.md](student-registration-api.md)): leen exactamente estas tablas |
| Consumidor actual | Panel web (`public/panel/`, pantalla "Mi dojo") |
| Fecha | 2026-11-07 |

## 1. Por qué existe

`POST /auth/register` solo crea la cuenta. El setup de la ficha de alta únicamente lista disciplinas que tienen
un programa (`dojo_programs`) con al menos un rango (`program_ranks`), y el alta exige un horario
(`dojo_schedules`). Antes de estos endpoints esas tablas solo se llenaban con el seed local, así que ningún dojo
nuevo, y ninguno en producción, podía dar de alta alumnos.

## 2. Endpoints

Todos requieren `Authorization: Bearer <accessToken>`, responden **401** sin token válido y **302** con un método
no permitido (igual que el resto del API). Las respuestas 200/201 traen `cache-control: no-store`.

| Método | Ruta | Éxito | Errores |
|--------|------|-------|---------|
| GET | `/api/v1/dojo/config` | 200 `DojoConfig` | — |
| PUT | `/api/v1/dojo/programs/{discipline}` | 201 si se creó, 200 si se reemplazó → `DojoConfig` | 400, 404 `PROGRAM_NOT_FOUND` (disciplina desconocida), 409 `RANK_IN_USE`, 422 |
| DELETE | `/api/v1/dojo/programs/{discipline}` | 204 | 404 `PROGRAM_NOT_FOUND`, 409 `PROGRAM_IN_USE` |
| PUT | `/api/v1/dojo/schedules/{scheduleId}` | 201 si se creó, 200 si se reemplazó → `DojoConfig` | 400, 409 `SCHEDULE_IN_USE`, 422 |
| DELETE | `/api/v1/dojo/schedules/{scheduleId}` | 204 | 404 `SCHEDULE_NOT_FOUND`, 409 `SCHEDULE_IN_USE` |
| PUT | `/api/v1/dojo/settings` | 200 `DojoConfig` | 400, 422 |

Las mutaciones devuelven la configuración completa, porque un cambio de escalera también puede cambiar el
temario de cintas (`belts`). `{discipline}` acepta mayúsculas o minúsculas (`KARATE`, `karate`).

## 3. `DojoConfig`

```json
{
  "asOf": "2026-11-07T10:15:00-06:00",
  "dojo": { "name": "Kensho Dojo", "mainDiscipline": "KARATE" },
  "settings": { "phoneCountryCode": "+52", "monthlyFeeCents": 65000, "currency": "MXN", "enrollmentWaived": true, "billingDay": 5, "enrollmentFeeCents": 50000 },
  "belts": ["WHITE", "YELLOW", "BLACK"],
  "programs": [
    {
      "discipline": "KARATE",
      "name": "Shotokan Karate",
      "minimumAttendancePercent": 85,
      "maxStripes": 3,
      "students": 12,
      "ranks": [
        {
          "belt": "WHITE",
          "grade": { "system": "KYU", "value": 10 },
          "requiredClasses": 24,
          "verifierName": "Sensei Tanaka",
          "requirements": [{ "id": "kata", "title": "Taikyoku Shodan", "description": "Sin pausas" }]
        }
      ]
    }
  ],
  "schedules": [
    {
      "id": "karate-mon-wed-1730",
      "discipline": "KARATE",
      "weekdays": ["MONDAY", "WEDNESDAY"],
      "startsAt": "17:30",
      "endsAt": "18:45",
      "description": "Tatami A",
      "capacity": 20,
      "activeStudents": 8
    }
  ]
}
```

- Sin fila en `dojo_settings`, `settings` trae los valores por defecto (`+52`, 0, `MXN`, `false`, 1).
- `programs[].students` cuenta alumnos de cualquier estado: son los que impiden borrar el programa.
- La disciplina con la que se registró el dojo va primero; las demás, en orden alfabético.

## 4. `PUT /dojo/programs/{discipline}`

```json
{
  "name": "Shotokan Karate",
  "minimumAttendancePercent": 85,
  "maxStripes": 3,
  "ranks": [
    { "belt": "WHITE", "grade": { "system": "KYU", "value": 10 }, "requiredClasses": 24, "verifierName": "Sensei Tanaka",
      "requirements": [{ "id": "kata", "title": "Taikyoku Shodan", "description": "Sin pausas" }] },
    { "belt": "YELLOW", "grade": { "system": "KYU", "value": 9 }, "requiredClasses": 30 },
    { "belt": "BLACK", "grade": { "system": "DAN", "value": 1 } }
  ]
}
```

| Campo | Regla |
|-------|-------|
| `name` | Requerido, hasta 120 caracteres |
| `minimumAttendancePercent` | Entero 0–100 |
| `maxStripes` | Entero 0–10 |
| `ranks` | 1–40 rangos, **en orden de progresión**. Sin repetir cinta + grado (`DUPLICATED`) |
| `ranks[].belt` | `WHITE`, `YELLOW`, `ORANGE`, `GREEN`, `BLUE`, `PURPLE`, `RED`, `BROWN`, `BLACK` |
| `ranks[].grade` | `null`/omitido, o `{ system: KYU\|KUP\|DAN, value: 1–20 }` |
| `ranks[].requiredClasses` | Entero 0–1000 (default 0). Al dar de alta, define `attendance.required` |
| `ranks[].verifierName` | Opcional, hasta 120 |
| `ranks[].requirements` | Hasta 30. `id` es un slug (`^[a-z0-9][a-z0-9-]{0,39}$`) único en el rango; `title` requerido (120); `description` opcional (500) |

**Semántica de reemplazo.** El cuerpo es la escalera completa, pero el servidor reconcilia en lugar de
borrar todo:

- Un rango que sigue (misma cinta y grado) conserva su fila: se actualizan posición, clases y verificador.
- Un requisito que sigue (mismo `id` dentro del rango) conserva su fila **y las verificaciones de los alumnos**.
- Lo que no viene en el cuerpo se borra (un requisito quitado se lleva sus verificaciones).
- Si algún alumno (de cualquier estado) está en un rango que se quita, responde **409 `RANK_IN_USE`** con
  `ranks: [{ belt, grade }]` y no cambia nada.
- `dojo_belts` se recalcula: entran las cintas nuevas de las escaleras (en el orden de la tabla de `belt`, sin
  mover las que ya estaban) y salen las que ya no usa ninguna escalera ni ningún alumno.

`DELETE` borra el programa con su escalera, requisitos y horarios. Si tiene alumnos, responde **409 `PROGRAM_IN_USE`**.

## 5. `PUT /dojo/schedules/{scheduleId}`

El id lo elige el cliente (slug, p. ej. `karate-mon-wed-1730`), así que reintentar un PUT no duplica el horario.
Un id que no es slug responde **422** con `field: "id"`.

```json
{ "discipline": "KARATE", "weekdays": ["MONDAY", "WEDNESDAY"], "startsAt": "17:30", "endsAt": "18:45", "description": "Tatami A", "capacity": 20 }
```

| Campo | Regla |
|-------|-------|
| `discipline` | Debe tener programa en el dojo; si no, 422 `NOT_AVAILABLE` |
| `weekdays` | 1–7 días de `MONDAY`…`SUNDAY` sin repetir; se guardan en orden de la semana |
| `startsAt`, `endsAt` | `HH:mm`; `endsAt` mayor que `startsAt` (`OUT_OF_RANGE`) |
| `description` | Opcional, hasta 200 (vacío → `null`) |
| `capacity` | `null` = sin límite, o entero 1–1000. Un horario lleno no aparece en el setup de la ficha |

- Los horarios nuevos se agregan al final. Si ya tiene alumnos, no se le puede cambiar la disciplina (**409 `SCHEDULE_IN_USE`**).
- `DELETE` responde **409 `SCHEDULE_IN_USE`** si algún alumno (de cualquier estado) lo tiene asignado o si tiene
  pase de lista registrado ([checkin-api.md](checkin-api.md)): el historial de asistencia apunta al horario.

## 6. `PUT /dojo/settings`

```json
{ "phoneCountryCode": "+52", "monthlyFeeCents": 65000, "currency": "MXN", "enrollmentWaived": true, "billingDay": 5, "enrollmentFeeCents": 50000 }
```

Los cinco primeros campos son requeridos: `phoneCountryCode` `^\+[1-9][0-9]{0,2}$`, `monthlyFeeCents` entero ≥ 0,
`currency` ISO 4217 en mayúsculas, `enrollmentWaived` booleano, `billingDay` 1–28 (día en que vence la mensualidad).

`enrollmentFeeCents` (entero ≥ 0, cuota de inscripción) es **opcional** para no romper a los clientes anteriores:
si no viene, se conserva el valor guardado (0 por defecto). Pagos ([payments-api.md](payments-api.md)) la cobra al dar
de alta salvo que `enrollmentWaived` sea `true`. Un cambio de cuota o moneda solo afecta a los cargos nuevos.

## 7. Errores 422

Mismo formato que `POST /students`:

```json
{ "code": "VALIDATION_FAILED", "message": "Revisa los campos marcados.",
  "fields": [{ "field": "ranks[2].requirements[0].id", "code": "INVALID_FORMAT" }] }
```

Códigos: `REQUIRED`, `INVALID_FORMAT`, `OUT_OF_RANGE`, `NOT_AVAILABLE`, `DUPLICATED`.

## 8. Concurrencia

Cada mutación corre en una transacción con `pg_advisory_xact_lock` por dojo, porque reordena posiciones con
llaves únicas. No hay migraciones nuevas: las tablas ya existían desde `20261106094924_student_registration`.

## 9. Pendiente

- Editar el nombre del dojo o la disciplina principal (hoy solo se fijan en `auth/register`).
- Cambiar a un alumno de rango o de horario (necesario antes de quitar un rango o un horario que tiene alumnos).
- Reordenar horarios (`position`) desde el API.
