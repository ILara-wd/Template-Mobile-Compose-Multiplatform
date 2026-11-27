# API para el perfil del instructor

| Dato | Valor |
|------|-------|
| Pantalla | `feature/profile/sensei/TkdSenseiProfileScreen.kt` (desde el avatar de Mi Dojo, Figma Prototipo-Demo-UiKit nodo 66:356) |
| Estado | **Implementado** en la API (`me.ts`, `me-profile.ts`, `me-presence.ts`) y en la app (`NetworkTkdSenseiProfileRepository`; `PATCH /me` desde "Editar datos" del perfil). Contrato también en `kensho-api.yaml` (tag `sensei-profile`) |
| Depende de | Cuenta (`users`), horarios y sesiones del pase de lista ([checkin-api.md](checkin-api.md)), pagos ([payments-api.md](payments-api.md)) |
| Fecha | 2026-11-09 |

---

## 1. Resumen

El repositorio de la app ya tiene la forma correcta (`profile()`, `setOnTatami()`), así que basta con escribir el
`NetworkTkdSenseiProfileRepository`. La mayor parte del perfil **se deriva** de datos que ya existen:

| Bloque | Sale de |
|--------|---------|
| Identidad | Cuenta (`users`) + datos del instructor (cargo, grado, año en que empezó a enseñar) |
| Ingresos del mes | El mismo cálculo que `GET /payments/summary` |
| Sedes | Una cuenta es un dojo: una sola sede |
| Agenda semanal | Horarios del dojo expandidos por fecha, con los mismos `id` y estados que el pase de lista |
| Avisos | Cuotas vencidas (pagos) y el próximo examen |

| # | Endpoint | Para qué |
|---|----------|----------|
| 1 | `GET /me/profile` | Todo el perfil en una llamada |
| 2 | `PUT /me/presence` | Switch "En el tatami" |
| 3 | `PATCH /me` | Capturar cargo, grado y año en que empezó a enseñar |
| — | `GET /me` | Igual que antes, con los campos del instructor agregados |

**Fuera del alcance** (decisión del 2026-11-09): honorarios (`feeSharePercent`), meta de ingresos (`goalCents`),
certificaciones, avisos libres del dojo (`ALERT`), cambio de sede (`PUT /me/active-dojo`) y la foto (`PUT /me/photo`,
hasta que haya almacenamiento de imágenes).

---

## 2. Mapa pantalla → datos

| Se pinta | Modelo hoy (`SenseiProfile`) | Viene del API | Notas |
|----------|------------------------------|---------------|-------|
| "Sensei Marcus Ruiz" | `name` | `instructor.name` | Es el `instructorName` de `/me` |
| Foto | `photo: @DrawableRes` | `instructor.photoUrl` | Siempre `null` por ahora → iniciales |
| "Head Coach" | `role` | `instructor.title` | Texto libre (≤ 60); `null` lo oculta |
| "4.º Dan" | `danRank: Int` | `instructor.rank` | Mismo objeto `Rank` que los alumnos (sin `stripes`); `null` lo oculta |
| "16 años" | `yearsOfExperience` | `instructor.teachingSince` (`YYYY`) | La app calcula los años con `asOf`; `null` lo oculta |
| "Karate Shotokan & BJJ" | `disciplines` (texto) | `dojo.disciplines[]` (códigos) | Programas del dojo; la app une las etiquetas |
| "En el tatami" | `isOnTatami` | `instructor.onTatami` | Se cambia con §3.2 |
| Sede principal | `homeDojoName` | `dojo.name` | |
| Ingresos del mes | `MonthlyEarnings` (centavos) | `earnings` | Sin honorarios ni meta (fuera del alcance) |
| Sedes | `dojos`, `activeDojoId` | `dojos[]` (una sola), `activeDojoId` | La sección muestra solo la principal |
| Agenda | `week: List<AgendaDay>` | `week[]`: 7 días desde el lunes de `asOf` | Mismo `id` de sesión que [checkin-api.md](checkin-api.md) |
| Estado de cada clase | `SessionStatus` | `sessions[].status` | `FINISHED`, `IN_PROGRESS`, `UPCOMING`; la app marca la siguiente |
| Avisos | `DojoNotice` | `notices[]` con `type` | Textos armados por la app |
| "Certificaciones" | `certificationsSummary` | — | **Se quita del diseño** |
| "Hoy" | `now: LocalDateTime` | `asOf` | |

---

## 3. Endpoints

Todos requieren `Authorization: Bearer <accessToken>` y responden `401` si falta o venció.

### 3.1 `GET /me/profile`

`200`, `Cache-Control: no-store`.

```json
{
  "asOf": "2026-11-09T17:38:00-06:00",
  "instructor": {
    "id": "b1536619-a5b2-4199-a506-0b72fe2269c4",
    "name": "Marcus Ruiz",
    "title": "Head Coach",
    "photoUrl": null,
    "rank": { "discipline": "KARATE", "belt": "BLACK", "grade": { "system": "DAN", "value": 4 } },
    "teachingSince": 2010,
    "onTatami": true
  },
  "dojo": {
    "id": "b1536619-a5b2-4199-a506-0b72fe2269c4",
    "name": "Kensho Dojo",
    "logoUrl": null,
    "disciplines": ["BJJ", "KARATE"]
  },
  "earnings": {
    "month": "2026-11",
    "currency": "MXN",
    "collectedCents": 524000,
    "previousFeesCents": 321200,
    "activeStudents": 148,
    "pendingCents": 42000,
    "pendingStudents": 6,
    "extrasCents": 75000
  },
  "dojos": [
    { "id": "b1536619-a5b2-4199-a506-0b72fe2269c4", "name": "Kensho Dojo", "address": null, "students": 148, "schedules": 4, "isPrimary": true }
  ],
  "activeDojoId": "b1536619-a5b2-4199-a506-0b72fe2269c4",
  "week": [
    {
      "date": "2026-11-09",
      "sessions": [
        {
          "id": "karate-mon-wed-1730/2026-11-09",
          "scheduleId": "karate-mon-wed-1730",
          "programName": "Shotokan Karate",
          "description": "Tatami B • Cadetes",
          "startsAt": "17:30",
          "endsAt": "18:45",
          "students": 22,
          "status": "IN_PROGRESS"
        }
      ]
    },
    { "date": "2026-11-15", "sessions": [] }
  ],
  "notices": [
    { "id": "overdue-2026-11", "type": "OVERDUE_FEES", "students": 6, "pendingCents": 42000 },
    {
      "id": "exam-ex-3a83e662",
      "type": "GRADING_DEADLINE",
      "examId": "ex-3a83e662",
      "examDate": "2026-11-28",
      "deadline": "2026-11-21",
      "title": "Examen Nacional de Grado"
    }
  ]
}
```

| Campo | Regla |
|-------|-------|
| `instructor.id`, `dojo.id`, `dojos[].id`, `activeDojoId` | El id de la cuenta: una cuenta es un dojo |
| `instructor.photoUrl`, `dojo.logoUrl`, `dojos[].address` | Siempre `null` por ahora |
| `dojo.disciplines` | Disciplinas con programa en el dojo, en orden alfabético; sin programas, la del registro |
| `earnings.collectedCents` | Cobrado en el mes (igual que `collectedCents` de `/payments/summary`) |
| `earnings.previousFeesCents` | Cobrado el mes anterior (`previousCollectedCents` de `/payments/summary`) |
| `earnings.activeStudents` | Alumnos `ACTIVE` |
| `earnings.pendingCents`, `pendingStudents` | Lo que se debe al cierre del mes, **incluidos los adeudos de meses anteriores** (igual que en Pagos) |
| `earnings.extrasCents` | Cobrado en el mes por inscripciones, exámenes y eventos (no mensualidades) |
| `dojos[].students`, `dojos[].schedules` | Alumnos `ACTIVE` y horarios del dojo |
| `week` | Siempre 7 días, del lunes al domingo de la semana de `asOf`; días sin clases con `sessions: []` |
| `sessions[].students` | Alumnos de la lista de esa clase en esa fecha (mismo criterio que el pase de lista) |
| `sessions[].status` | Igual que el pase de lista, salvo que una sesión cerrada a mano (`CLOSED`) sale como `FINISHED` |
| `notices[].type` | `OVERDUE_FEES` o `GRADING_DEADLINE`. Tipo desconocido → la app lo descarta |

Leer el perfil también genera las mensualidades que falten (igual que al abrir Pagos), para que los ingresos cuadren.

#### Avisos

| `type` | Cuándo aparece | Campos |
|--------|----------------|--------|
| `OVERDUE_FEES` | Hay cargos vencidos (`due_on` antes de hoy y sin pagar) | `students`: alumnos con algo vencido · `pendingCents`: lo vencido, con el recargo del 5 % de las mensualidades |
| `GRADING_DEADLINE` | Hay un examen entre hoy y dentro de 30 días (solo el más próximo) | `examId`, `examDate` · `deadline` = fecha del examen − 7 días (puede haber pasado ya) · `title`: el del examen creado en Pagos, o "Examen de grado" |

Los textos ("6 alumnos con cuota vencida") los arma la app con `strings.xml`. `pendingCents` del aviso solo cuenta lo
ya vencido; `earnings.pendingCents` incluye lo que vence más adelante en el mes.

### 3.2 `PUT /me/presence`

```json
{ "onTatami": false }
```

`204`. Es solo un switch que el instructor prende y apaga; nada lo cambia solo. La app lo aplica de forma optimista y
regresa al valor anterior si falla.

### 3.3 `GET /me` y `PATCH /me`

`GET /me` responde lo de siempre más los datos del instructor (campos nuevos, ninguno quitado):

```json
{
  "id": "b1536619-a5b2-4199-a506-0b72fe2269c4",
  "email": "marcus@kenshodojo.mx",
  "instructorName": "Marcus Ruiz",
  "dojoName": "Kensho Dojo",
  "discipline": "KARATE",
  "title": "Head Coach",
  "rank": { "discipline": "KARATE", "belt": "BLACK", "grade": { "system": "DAN", "value": 4 } },
  "teachingSince": 2010,
  "onTatami": false
}
```

`PATCH /me` recibe cualquiera de `title`, `teachingSince` y `rank`, y responde `200` con el mismo `Me`:

```json
{ "title": "Head Coach", "teachingSince": 2010, "rank": { "discipline": "KARATE", "belt": "BLACK", "grade": { "system": "DAN", "value": 4 } } }
```

| Campo | Regla |
|-------|-------|
| (todos) | Solo cambian los campos que se mandan; `null` borra el dato; `{}` no cambia nada |
| `title` | Texto de hasta 60 caracteres; vacío equivale a `null` |
| `teachingSince` | Año entero entre 1900 y el año en curso |
| `rank.discipline` | `KARATE`, `BJJ`, `TAEKWONDO` o `MIXED` |
| `rank.belt` | Una cinta del catálogo (`WHITE` … `BLACK`); no tiene que estar en la escalera de rangos del dojo |
| `rank.grade` | Opcional: `{ system: KUP \| DAN \| KYU, value: 1-20 }` |

### 3.4 Errores

| HTTP | `code` | Cuándo | Qué hace la app |
|------|--------|--------|-----------------|
| `400` | `BAD_REQUEST` | `PATCH /me` o `PUT /me/presence` sin un objeto JSON | Error genérico |
| `401` | `MISSING_TOKEN` / `INVALID_TOKEN` | Token ausente, vencido o de una cuenta borrada | Renovación; si falla, login |
| `422` | `VALIDATION_FAILED` | `PATCH /me` o `PUT /me/presence` inválido; `fields[]` con `field` (`title`, `teachingSince`, `rank.discipline`, `rank.belt`, `rank.grade`, `onTatami`) y `code` (`REQUIRED`, `INVALID_FORMAT`, `OUT_OF_RANGE`) | Marca el campo |

---

## 4. Modelo de datos

Columnas nuevas en `users` (migración `20261110051950_sensei_profile`), todas opcionales:

| Columna | Tipo | Regla |
|---------|------|-------|
| `title` | text | 1-60 caracteres |
| `rank_discipline`, `rank_belt`, `rank_grade_system`, `rank_grade_value` | text / int | Cinta y disciplina van juntas; sistema y valor del grado van juntos (1-20) |
| `teaching_since` | int | ≥ 1900 |
| `on_tatami` | boolean | Default `false` |

---

## 5. Impacto en la app

| Pieza | Hoy | Cambio |
|-------|-----|--------|
| `TkdSenseiProfileRepository` | Firma ya correcta | + `NetworkTkdSenseiProfileRepository` y `@Provides` en `DataModule` (Fake solo con el mock estático); quitar `setActiveDojo` |
| Modelo | `photo: @DrawableRes`; `danRank: Int`; `disciplines` y `certificationsSummary` como texto; `yearsOfExperience` | `photoUrl`; `Rank?`; disciplinas como códigos; `teachingSince`; sin certificaciones ni honorarios |
| `TatamiSession` | `room`, `details` | `programName`, `description` (mismo dato que el horario) |
| `DojoNotice` | 3 tipos con textos de la Fake | `OVERDUE_FEES` y `GRADING_DEADLINE` con datos; textos en `strings.xml` |
| `Me` | 5 campos | + `title`, `rank`, `teachingSince`, `onTatami` (todos opcionales) |
| Tests | VM con la Fake | + mapper (fixture `me_profile.json`) y repositorio con `MockWebServer` |

---

## 6. Decisiones

| Pregunta | Decisión |
|----------|----------|
| Multisede | No: `dojos[]` trae la única sede; no hay `PUT /me/active-dojo` |
| Honorarios (`feeSharePercent`) | Fuera del alcance |
| "En el tatami" | Solo un switch que prende y apaga el instructor |
| Certificaciones | Se quitan del diseño |
| Meta de ingresos (`goalCents`) | Fuera del alcance |
| Aviso de examen | Próximo examen dentro de 30 días; fecha límite = examen − 7 días; título del evento de Pagos o "Examen de grado" |
| Avisos libres del dojo (`ALERT`) | Fuera del alcance |
| Sesión cerrada a mano | Se envía como `FINISHED` |
| `PATCH /me` | Incluido |

## 7. Pendiente

- Foto del instructor y logo del dojo (`PUT /me/photo`) cuando haya almacenamiento de imágenes.
- Honorarios, meta de ingresos, avisos libres y multisede, si se retoman.
