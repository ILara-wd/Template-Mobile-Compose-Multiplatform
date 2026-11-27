# API para el alta de alumnos

| Dato | Valor |
|------|-------|
| Pantalla | `feature/students/register/TkdStudentRegistrationScreen.kt` (ruta `students/register`) · secciones `Personal`, `Discipline`, `Guardian`, `Plan` · `TkdSendCredentialCard` |
| Estado actual | **Implementado en la app (2026-11-06)** con el JSON de este documento: `NetworkTkdStudentRegistrationRepository`, contrato en `kensho-api.yaml`; los borradores siguen en memoria hasta §8. Antes: `TkdStudentRegistrationRepository` ya era `suspend` + `TkdResult` y el VM ya es `@HiltViewModel`, pero `DataModule` lo enlaza a `FakeTkdStudentRegistrationRepository` (prototipo, nodo 65:2). Los catálogos traen textos armados ("10° Kyu (Mukyu)", "Lunes y Miércoles") y la cuota se formatea en USD con `Locale.US` |
| Depende de | `GET /students` (`docs/api/students-api.md`) y `GET /students/{studentId}` (`docs/api/student-profile-api.md`): la respuesta del alta **es** un `StudentProfile` |
| Contrato existente | `docs/api/kensho-api.yaml` (`auth`, `me`, `students`, `students/{studentId}`) |
| Fecha | 2026-11-06 |
| Objetivo | Definir los endpoints que alimentan la ficha de nuevo alumno para construirlos en el backend y consumirlos con `/kensho-endpoint` |

---

## 1. Resumen y decisión

| # | Endpoint | Para qué | Prioridad |
|---|----------|----------|-----------|
| 1 | `GET /dojo/registration-setup` | Catálogos de la ficha: disciplinas con sus cintas, horarios, plan de cobro, lada y folio temporal (`repository.setup()`) | **P0** |
| 2 | `POST /students` | Dar de alta al alumno (`repository.register(form)`) | **P0** |
| 3 | `PUT /student-drafts/{registrationId}` · `GET /student-drafts` | Guardar y retomar la ficha incompleta (`repository.saveDraft(form)`) | P1 (§8) |
| 4 | `PUT /students/{studentId}/photo` | Fotografía oficial ("Cámara Dojo" / "Subir Archivo") | P1 (§8) |

**Decisiones clave:**

- **El alta responde con el perfil completo.** `POST /students` devuelve `201` con el mismo schema `StudentProfile` de `GET /students/{studentId}` y el header `Location`. Así:
  - el mapper `toProfile()` se reutiliza sin cambios;
  - la app puede abrir el perfil del alumno recién creado, y la ficha y el perfil nunca discrepan sobre cómo quedó guardado.
- **Mismas reglas de catálogo que la lista y el perfil.** El API manda códigos (`belt: "WHITE"`, `grade: {system: KYU, value: 10}`, `weekdays: ["MONDAY", "WEDNESDAY"]`, `gender: "MALE"`) y la app arma los textos con `strings.xml` y `Locale` es-MX. Hoy la *Fake* trae "10° Kyu (Mukyu)" y "Lunes y Miércoles" ya escritos: eso desaparece.
- **Excepción, igual que en el perfil:** el texto que escribe el dojo (detalle del horario: tatami, sensei, categoría) llega como texto y se muestra tal cual.
- **Las cintas dependen de la disciplina.** Karate usa *kyu* y Taekwondo *kup*: el setup agrupa las cintas, el máximo de rayas y los horarios por disciplina. Hoy la lista de cintas es única para cualquier disciplina.
- **Alta idempotente.** El setup reserva un folio (`registrationId`, el "ID Temporal: #DOJO-9402" de la intro) y el `POST` lo manda como header `Idempotency-Key`. Si la red se cae después de que el servidor guardó, el reintento devuelve el mismo `201` en vez de duplicar al alumno. El mismo folio es la llave del borrador (§8).
- **"Hoy" viene del servidor.** `asOf` sustituye a `RegistrationSetup.today`: alimenta la edad, el cálculo de "Menor" y la fecha de primera clase por defecto.
- **La cuota no se captura, se informa.** "Cuota mensual pactada", "Inscripción Exenta" y el día de corte salen del plan del dojo; el request no los manda. El monto viene en centavos con su moneda (`MXN`), no en USD fijo.
- **Errores por campo.** Un `422` trae la lista de campos inválidos para marcarlos en la ficha (los mismos `RegistrationField` que ya valida el VM), además del `message` para el snackbar (ADR-020).

---

## 2. Mapa pantalla → datos

### 2.1 Lo que lee la ficha (`RegistrationSetup` ← `GET /dojo/registration-setup`)

| En pantalla | Campo hoy | Origen en el API | Nota |
|-------------|-----------|------------------|------|
| "ID Temporal: #DOJO-9402" | `temporaryId` | `registrationId` | Folio reservado; también es `Idempotency-Key` y llave del borrador |
| Edad calculada, toggle "Menor", "Hoy, 20 nov" | `today` | `asOf` | La app toma la fecha local del dojo |
| Chips de disciplina | `disciplines` | `disciplines[].code` | Códigos `DisciplineCode`; `BJJ`/`MIXED` se descartan hasta tener `DojoDiscipline` |
| Selector de cinturón y su grado | `belts[]` (`BeltRank(belt, rankLabel)`) | `disciplines[].belts[]` (`belt`, `grade`) | La etiqueta la arma `beltRankLabel()` / `gradeLabel()` (los mismos helpers de la lista y el perfil) |
| "Rayas / Franjas Previas" | `maxStripes` | `disciplines[].maxStripes` | Por disciplina |
| Lada del teléfono ("+52") | `countryCode` | `phoneCountryCode` | El request manda el teléfono en E.164 |
| Tarjetas de horario | `schedules[]` (`days: String`, `start`, `end`, `details`) | `schedules[]` (`id`, `discipline`, `weekdays[]`, `startsAt`, `endsAt`, `description`) | La app arma "Lunes y Miércoles" desde `DayOfWeek`; filtra por la disciplina elegida |
| "$65.00 USD/mes" | `monthlyFeeCents` | `plan.monthlyFeeCents` + `plan.currency` | Formato con `Locale` es-MX y la moneda del API |
| "Inscripción Exenta" | `enrollmentWaived` | `plan.enrollmentWaived` | — |
| "Corte: Día 05 de cada mes" | `billingDay` | `plan.billingDay` | 1–28 |

### 2.2 Lo que envía la ficha (`StudentRegistrationForm` → `POST /students`)

| Campo de la ficha | Campo hoy | Request | Nota |
|-------------------|-----------|---------|------|
| Nombre completo | `fullName` | `fullName` | Recortado (`trimmed()`) |
| Fecha de nacimiento | `birthDate` | `birthDate` (`yyyy-MM-dd`) | Requerido |
| Categoría | `gender` | `gender` (`MALE` · `FEMALE`) | Requerido |
| Notas médicas | `medicalNotes` | `medicalNotes` | Se omite si está vacío |
| Disciplina, cinturón, rayas | `discipline`, `belt`, `stripes` | `enrollment.discipline`, `enrollment.belt`, `enrollment.stripes` | El servidor valida que la cinta exista en esa disciplina |
| Primera clase | `firstClassDate` | `enrollment.firstClassOn` | Si no se eligió, el VM manda `asOf` (ya lo hace `trimmed(today)`) |
| Horario | `scheduleId` | `enrollment.scheduleId` | Requerido; de `schedules[].id` |
| Toggle "Menor" + tutor | `isMinor`, `guardianName`, `relation` | `guardian` (`name`, `relation`) | Se omite cuando no es menor |
| Teléfono | `phone` (10 dígitos) | `contact.phone` (E.164: `+525512345678`) | La app antepone `phoneCountryCode` |
| Correo | `email` | `contact.email` | Opcional |
| Contacto de emergencia | `emergencyName`, `emergencyPhone` | `emergencyContact` (`name`, `phone` en E.164) | Se omite si falta el nombre o el teléfono |
| "Generar y enviar credencial digital" | `sendCredential` | `sendCredential` | El servidor la envía de forma asíncrona (§7) |
| Folio | `setup.temporaryId` | header `Idempotency-Key` | No va en el cuerpo |

---

## 3. Endpoint `GET /dojo/registration-setup`

| Aspecto | Valor |
|---------|-------|
| Auth | `Authorization: Bearer <accessToken>` (lo agrega `TkdAuthInterceptor`) |
| Alcance | El dojo sale del token |
| Efecto | Reserva un `registrationId` nuevo en cada llamada (vigencia sugerida: 7 días). No es un `GET` puro, pero reservar un folio sin uso no tiene efecto visible; la alternativa es `POST /student-registrations` (§7, decisión 1) |
| Caché | Sin caché (el folio cambia) |

> **Por qué `/dojo/...` y no `/students/registration-setup`:** bajo `/students/` el segmento chocaría con `/students/{studentId}` (el router no distingue `registration-setup` de un id).

### 3.1 Respuesta de ejemplo (fixture `students_registration_setup.json`)

```json
{
  "asOf": "2026-11-06T10:15:00-06:00",
  "registrationId": "DOJO-9402",
  "phoneCountryCode": "+52",
  "disciplines": [
    {
      "code": "KARATE",
      "maxStripes": 4,
      "belts": [
        { "belt": "WHITE", "grade": { "system": "KYU", "value": 10 } },
        { "belt": "YELLOW", "grade": { "system": "KYU", "value": 9 } },
        { "belt": "ORANGE", "grade": { "system": "KYU", "value": 8 } },
        { "belt": "GREEN", "grade": { "system": "KYU", "value": 6 } },
        { "belt": "BLUE", "grade": { "system": "KYU", "value": 4 } },
        { "belt": "BROWN", "grade": { "system": "KYU", "value": 2 } },
        { "belt": "BLACK", "grade": { "system": "DAN", "value": 1 } }
      ]
    },
    {
      "code": "TAEKWONDO",
      "maxStripes": 3,
      "belts": [
        { "belt": "WHITE", "grade": { "system": "KUP", "value": 10 } },
        { "belt": "YELLOW", "grade": { "system": "KUP", "value": 8 } },
        { "belt": "GREEN", "grade": { "system": "KUP", "value": 6 } },
        { "belt": "BLUE", "grade": { "system": "KUP", "value": 4 } },
        { "belt": "RED", "grade": { "system": "KUP", "value": 2 } },
        { "belt": "BLACK", "grade": { "system": "DAN", "value": 1 } }
      ]
    }
  ],
  "schedules": [
    {
      "id": "mon-wed-1730",
      "discipline": "KARATE",
      "weekdays": ["MONDAY", "WEDNESDAY"],
      "startsAt": "17:30",
      "endsAt": "18:45",
      "description": "Tatami B • Sensei Tanaka • Cadetes (12-16 años)"
    },
    {
      "id": "tue-thu-1830",
      "discipline": "KARATE",
      "weekdays": ["TUESDAY", "THURSDAY"],
      "startsAt": "18:30",
      "endsAt": "19:45",
      "description": "Tatami Principal • Formación General"
    }
  ],
  "plan": {
    "monthlyFeeCents": 65000,
    "currency": "MXN",
    "enrollmentWaived": true,
    "billingDay": 5
  }
}
```

Variantes como fixture: `students_registration_setup_unknown_codes.json` (disciplina `BJJ`, cinta `GOLD`, día `HOLIDAY`) y `students_registration_setup_empty.json` (sin horarios).

### 3.2 Campos

| Campo | Tipo | Requerido | Nulo | Descripción |
|-------|------|-----------|------|-------------|
| `asOf` | string `date-time` | Sí | No | "Ahora" del dojo con su offset |
| `registrationId` | string | Sí | No | Folio reservado para esta ficha; `Idempotency-Key` del alta y llave del borrador |
| `phoneCountryCode` | string (`+` y 1–3 dígitos) | Sí | No | Lada del dojo |
| `disciplines[]` | array, ≥ 1 | Sí | No | En el orden en que se muestran; el primero es el default |
| `disciplines[].code` | `DisciplineCode` | Sí | No | — |
| `disciplines[].maxStripes` | integer 0–10 | Sí | No | Rayas posibles antes del siguiente grado |
| `disciplines[].belts[]` | array, ≥ 1 | Sí | No | En orden del temario; la primera es el default |
| `belts[].belt` / `belts[].grade` | `BeltCode` / `BeltGrade` | Sí | `grade` sí | Mismos schemas que `StudentRank` |
| `schedules[]` | array | Sí | No (puede ir vacío) | Grupos con cupo |
| `schedules[].discipline` | `DisciplineCode` | Sí | No | La app muestra solo los de la disciplina elegida |
| `schedules[].weekdays` | array de `Weekday` | Sí | No | `MONDAY` … `SUNDAY` (mismo nombre que `java.time.DayOfWeek`) |
| `schedules[].startsAt` / `endsAt` | string `HH:mm` | Sí | No | Hora local del dojo |
| `schedules[].description` | string | No | Sí | Texto del dojo (tatami, sensei, categoría) |
| `plan.monthlyFeeCents` | integer ≥ 0 | Sí | No | Centavos |
| `plan.currency` | string ISO 4217 | Sí | No | `MXN` |
| `plan.enrollmentWaived` | boolean | Sí | No | — |
| `plan.billingDay` | integer 1–28 | Sí | No | Día de corte |

---

## 4. Endpoint `POST /students`

| Aspecto | Valor |
|---------|-------|
| Auth | Bearer (interceptor) |
| Headers | `Idempotency-Key: <registrationId>` (requerido) · `Content-Type: application/json` |
| Respuesta | `201 Created` · `Location: /api/v1/students/{studentId}` · cuerpo `StudentProfile` |
| Idempotencia | Misma llave + mismo cuerpo → el mismo `201` (sin crear otro). Misma llave + cuerpo distinto → `409` `IDEMPOTENCY_KEY_REUSED` |

### 4.1 Request de ejemplo (fixture `students_create_request.json`)

```json
{
  "fullName": "Mateo Morales García",
  "birthDate": "2014-03-12",
  "gender": "MALE",
  "medicalNotes": "Asma leve controlada. Porta inhalador en el vestidor.",
  "enrollment": {
    "discipline": "KARATE",
    "belt": "WHITE",
    "stripes": 0,
    "firstClassOn": "2026-11-06",
    "scheduleId": "mon-wed-1730"
  },
  "guardian": { "name": "Ricardo Morales", "relation": "FATHER" },
  "contact": { "phone": "+525512345678", "email": "ricardo.morales@correo.mx" },
  "emergencyContact": { "name": "Elena Morales (Tía)", "phone": "+525599324410" },
  "sendCredential": true
}
```

Un adulto **omite** `guardian`; sin contacto de emergencia (nombre y teléfono) se omite `emergencyContact`; sin correo, `contact.email`; sin notas, `medicalNotes`. La app no manda `null` (`explicitNulls = false`), así que el contrato los declara opcionales (fixture `students_create_request_adult.json`).

### 4.2 Respuesta `201` (fixture `students_create.json`)

El mismo `StudentProfile` de `GET /students/{studentId}`, ya con el rango inicial y el temario del primer grado:

```json
{
  "asOf": "2026-11-06T10:16:02-06:00",
  "id": "st-mateo-morales",
  "displayName": "Mateo Morales García",
  "age": 12,
  "photoUrl": null,
  "membership": { "status": "ACTIVE", "enrolledOn": "2026-11-06" },
  "program": { "discipline": "KARATE", "name": "Shotokan Karate & Kobudo" },
  "rank": { "discipline": "KARATE", "belt": "WHITE", "grade": { "system": "KYU", "value": 10 }, "stripes": 0 },
  "nextRank": { "belt": "YELLOW", "grade": { "system": "KYU", "value": 9 } },
  "nextExam": null,
  "attendance": { "attended": 0, "required": 24, "minimumPercent": 90, "streakWeeks": 0, "classesPerWeek": 0, "lifetimeHours": 0 },
  "syllabus": { "verifierName": null, "requirements": [] },
  "latestObservation": null,
  "graduation": { "markedReady": false }
}
```

### 4.3 Reglas de validación (servidor; la app ya valida las mismas antes de enviar)

| Campo | Regla | `fields[].field` en el 422 | `RegistrationField` |
|-------|-------|----------------------------|---------------------|
| `fullName` | 1–120 caracteres tras recortar | `fullName` | `FullName` |
| `birthDate` | Fecha válida, no futura, edad 3–100 a `asOf` | `birthDate` | `BirthDate` |
| `gender` | `MALE` · `FEMALE` | `gender` | `Gender` |
| `guardian` | Requerido si la edad a `asOf` es < 18; `name` 1–120 | `guardian.name` | `GuardianName` |
| `contact.phone` | E.164, 10 dígitos nacionales para `+52` | `contact.phone` | `Phone` |
| `contact.email` | Correo válido si viene | `contact.email` | `Email` |
| `enrollment.scheduleId` | Existe, es de esa disciplina y tiene cupo | `enrollment.scheduleId` | `Schedule` |
| `enrollment.belt` / `stripes` | La cinta existe en la disciplina; `stripes` ≤ `maxStripes` | `enrollment.belt` / `enrollment.stripes` | — (snackbar con `message`) |
| `emergencyContact.phone` | E.164 si viene | `emergencyContact.phone` | — (snackbar) |

### 4.4 Respuestas de error

| HTTP | `code` | Cuándo | En la app |
|------|--------|--------|-----------|
| `201` | — | Alumno creado (o reintento con la misma llave) | `RegistrationOutcome.Registered` |
| `400` | `BAD_REQUEST` | JSON malformado o falta `Idempotency-Key` | `Unknown` (es un bug de la app) |
| `401` | `TOKEN_EXPIRED` … | Token inválido | Refresh; si falla, login |
| `403` | `FORBIDDEN` | El usuario no puede dar de alta alumnos (rol) | `Unknown` con el `message` del API |
| `409` | `STUDENT_ALREADY_EXISTS` | Mismo nombre + fecha de nacimiento en el dojo | `Conflict`: `message` del API o `tkd_error_conflict` ("Ya existe un registro con esos datos.") |
| `409` | `IDEMPOTENCY_KEY_REUSED` | El folio ya se usó con otros datos | `Conflict`; la app pide un setup nuevo (§6) |
| `410` | `REGISTRATION_EXPIRED` | El folio venció | Igual que el anterior |
| `422` | `VALIDATION_FAILED` | Alguna regla de §4.3 | `Invalid` + `fields` → `state.errors` y `message` en el snackbar |
| `5xx` | — | Falla del servidor | `Server`: snackbar; el botón "Registrar" sirve para reintentar con la misma llave |

Cuerpo de error (schema `Error` extendido, compatible hacia atrás):

```json
{
  "code": "VALIDATION_FAILED",
  "message": "Revisa los campos marcados.",
  "fields": [
    { "field": "contact.phone", "code": "INVALID_FORMAT" },
    { "field": "guardian.name", "code": "REQUIRED" }
  ]
}
```

---

## 5. Contrato OpenAPI (para pegar en `kensho-api.yaml` cuando el backend lo publique)

```yaml
paths:
  /dojo/registration-setup:
    get:
      tags: [students]
      operationId: registrationSetup
      summary: Catálogos para dar de alta un alumno y folio reservado
      description: >-
        Cada llamada reserva un `registrationId` (vigencia 7 días) que el alta usa como `Idempotency-Key`.
        Los nombres de cinta, grado y días los arma la app a partir de los códigos.
      responses:
        '200':
          description: Catálogos del dojo
          content:
            application/json:
              schema: { $ref: '#/components/schemas/RegistrationSetup' }
        '401': { $ref: '#/components/responses/Unauthorized' }
  /students:
    post:
      tags: [students]
      operationId: createStudent
      summary: Da de alta un alumno en el dojo de la sesión
      parameters:
        - in: header
          name: Idempotency-Key
          required: true
          description: El `registrationId` de `GET /dojo/registration-setup`
          schema: { type: string }
      requestBody:
        required: true
        content:
          application/json:
            schema: { $ref: '#/components/schemas/CreateStudentRequest' }
      responses:
        '201':
          description: Alumno creado (o reintento idempotente)
          headers:
            Location: { schema: { type: string } }
          content:
            application/json:
              schema: { $ref: '#/components/schemas/StudentProfile' }
        '401': { $ref: '#/components/responses/Unauthorized' }
        '409': { $ref: '#/components/responses/Conflict' }
        '410': { $ref: '#/components/responses/Gone' }
        '422': { $ref: '#/components/responses/ValidationFailed' }

components:
  responses:
    Conflict:
      description: Ya existe un recurso con esos datos o la llave de idempotencia se usó con otro cuerpo
      content:
        application/json:
          schema: { $ref: '#/components/schemas/Error' }
    Gone:
      description: El folio de registro venció
      content:
        application/json:
          schema: { $ref: '#/components/schemas/Error' }
    ValidationFailed:
      description: Campos inválidos; `fields` dice cuáles
      content:
        application/json:
          schema: { $ref: '#/components/schemas/Error' }
  schemas:
    RegistrationSetup:
      type: object
      required: [asOf, registrationId, phoneCountryCode, disciplines, schedules, plan]
      properties:
        asOf: { type: string, format: date-time }
        registrationId: { type: string }
        phoneCountryCode: { type: string, pattern: '^\+[0-9]{1,3}$' }
        disciplines:
          type: array
          minItems: 1
          items:
            type: object
            required: [code, maxStripes, belts]
            properties:
              code: { $ref: '#/components/schemas/DisciplineCode' }
              maxStripes: { type: integer, minimum: 0, maximum: 10 }
              belts:
                type: array
                minItems: 1
                items:
                  type: object
                  required: [belt, grade]
                  properties:
                    belt: { $ref: '#/components/schemas/BeltCode' }
                    grade: { $ref: '#/components/schemas/BeltGrade' }
        schedules:
          type: array
          items: { $ref: '#/components/schemas/TrainingSchedule' }
        plan:
          type: object
          required: [monthlyFeeCents, currency, enrollmentWaived, billingDay]
          properties:
            monthlyFeeCents: { type: integer, minimum: 0 }
            currency: { type: string, example: MXN }
            enrollmentWaived: { type: boolean }
            billingDay: { type: integer, minimum: 1, maximum: 28 }
    TrainingSchedule:
      type: object
      required: [id, discipline, weekdays, startsAt, endsAt]
      properties:
        id: { type: string }
        discipline: { $ref: '#/components/schemas/DisciplineCode' }
        weekdays:
          type: array
          minItems: 1
          items: { type: string, enum: [MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY] }
        startsAt: { type: string, pattern: '^[0-2][0-9]:[0-5][0-9]$' }
        endsAt: { type: string, pattern: '^[0-2][0-9]:[0-5][0-9]$' }
        description: { type: [string, 'null'] }
    CreateStudentRequest:
      type: object
      required: [fullName, birthDate, gender, enrollment, guardian, contact, emergencyContact, sendCredential]
      properties:
        fullName: { type: string, minLength: 1, maxLength: 120 }
        birthDate: { type: string, format: date }
        gender: { type: string, enum: [MALE, FEMALE] }
        medicalNotes: { type: string, maxLength: 1000 }
        enrollment:
          type: object
          required: [discipline, belt, stripes, firstClassOn, scheduleId]
          properties:
            discipline: { $ref: '#/components/schemas/DisciplineCode' }
            belt: { $ref: '#/components/schemas/BeltCode' }
            stripes: { type: integer, minimum: 0 }
            firstClassOn: { type: string, format: date }
            scheduleId: { type: string }
        guardian:
          oneOf:
            - type: 'null'
            - type: object
              required: [name, relation]
              properties:
                name: { type: string, minLength: 1, maxLength: 120 }
                relation: { type: string, enum: [FATHER, MOTHER, LEGAL_GUARDIAN] }
        contact:
          type: object
          required: [phone]
          properties:
            phone: { type: string, pattern: '^\+[1-9][0-9]{7,14}$' }
            email: { type: string, format: email }
        emergencyContact:
          oneOf:
            - type: 'null'
            - type: object
              required: [name, phone]
              properties:
                name: { type: string }
                phone: { type: string, pattern: '^\+[1-9][0-9]{7,14}$' }
        sendCredential: { type: boolean }
    Error:
      # Se agrega `fields` (opcional): los clientes que no lo conocen lo ignoran.
      properties:
        fields:
          type: array
          items:
            type: object
            required: [field, code]
            properties:
              field: { type: string, description: Ruta del campo en el request (`contact.phone`) }
              code: { type: string, enum: [REQUIRED, INVALID_FORMAT, OUT_OF_RANGE, NOT_AVAILABLE] }
```

---

## 6. Arquitectura en la app (ADR-016/017)

Se construye con `/kensho-endpoint GET /dojo/registration-setup` y `/kensho-endpoint POST /students` con los JSON de §3.1 y §4.

```
TkdApp  students/register ──▶ TkdStudentRegistrationScreen ── hiltViewModel() ──▶ TkdStudentRegistrationViewModel (ya @HiltViewModel)
                                                                │  setup → form → validate → register(form, setup)
                                                                │  errores: submitError (snackbar) + errors (campos del 422)
                                                                ▼
                                       TkdStudentRegistrationRepository (ya suspend + TkdResult)
                                          ├─ NetworkTkdStudentRegistrationRepository   (nuevo)
                                          │     setup()          = tkdApiCall(io) { api.registrationSetup().toSetup() }
                                          │     register(...)    = tkdApiCall(io) { api.createStudent(key, form.toRequest(cc)).toProfile() }
                                          │     saveDraft(...)   = P1 (§8); mientras tanto la Fake o local
                                          └─ FakeTkdStudentRegistrationRepository  (catálogos con códigos)
                                                                ▼
                     TkdStudentsApi (+2 métodos) · dto/TkdStudentRegistrationDto.kt · mapper/TkdStudentRegistrationMapper.kt
                     (la respuesta del alta reutiliza StudentProfileDto + toProfile())
```

| Capa | Cambio |
|------|--------|
| `data/remote/api/TkdStudentsApi.kt` | `@GET("dojo/registration-setup") suspend fun registrationSetup(): RegistrationSetupDto` y `@POST("students") suspend fun createStudent(@Header("Idempotency-Key") key: String, @Body body: CreateStudentRequestDto): StudentProfileDto`. Sin Api nueva |
| `data/remote/dto/TkdStudentRegistrationDto.kt` | Respuesta: `RegistrationSetupDto`, `DisciplineOptionDto`, `BeltOptionDto` (reutiliza `GradeDto`), `TrainingScheduleDto`, `PlanDto`. Request: `CreateStudentRequestDto`, `EnrollmentDto`, `GuardianDto`, `ContactDto`, `EmergencyContactDto` |
| `data/remote/mapper/TkdStudentRegistrationMapper.kt` | `RegistrationSetupDto.toSetup()`: descarta disciplinas, cintas y días desconocidos; `weekdays` → `DayOfWeek`, `HH:mm` → `LocalTime`. `StudentRegistrationForm.toRequest(countryCode)`: enums → códigos (`BeltColor.toCode()` inverso de `toBeltColor()`, `StudentGender`, `GuardianRelation`, `DojoDiscipline`), fechas ISO, teléfono E.164, vacíos → `null` |
| `data/remote/TkdApiCall.kt` + `TkdResult` | `422` → nuevo `TkdDataError.Invalid`; `Failure` gana `fieldErrors: List<String> = emptyList()` con las rutas de `fields[].field`. `toUiText()` sin cambios (usa `message` o `tkd_error_invalid`). Toca ADR-020: anotarlo como modificación |
| `data/TkdStudentRegistrationRepository.kt` | `register(form)` pasa a `register(form, setup)` (necesita `registrationId` y `phoneCountryCode`) y devuelve `TkdResult<StudentProfile>` en vez del id. La *Fake* manda catálogos con códigos |
| `data/NetworkTkdStudentRegistrationRepository.kt` | Nuevo, una línea por método |
| `di/DataModule.kt` | El `@Binds` a la *Fake* pasa a `@Provides` Network/Fake según `AUTH_STATIC_MOCK`, igual que la lista y el perfil |
| `model/TkdStudentRegistrationUiState.kt` | Ver §6.1 |
| `TkdStudentRegistrationViewModel` | `register(form, setup)`. Cintas y horarios dependen de la disciplina: al cambiarla, la cinta vuelve a la primera de esa disciplina, las rayas se recortan a `maxStripes` y el horario se limpia si no es de esa disciplina. Con `Failure(Invalid)` mapea `fieldErrors` a `RegistrationField` (tabla §4.3). Con `409 IDEMPOTENCY_KEY_REUSED` o `410`, recarga el setup conservando el formulario |
| `Discipline`/`Plan` sections | Etiqueta de cinta con `beltRankLabel()`/`gradeLabel()`; días con `DayOfWeek.getDisplayName(TextStyle.FULL, es-MX)`; cuota con la moneda del API en lugar de `centsAsUsd()` |
| `TkdStudentRegistrationActions` / `TkdApp` | `onRegistered: (studentId: String) -> Unit`. `TkdApp` navega a `student/{id}` sacando la ficha de la pila (`popUpTo(REGISTER_STUDENT_ROUTE) { inclusive = true }`) y avisa a la lista para recargar (§6.3) |

### 6.1 Cambios en el modelo

| Hoy | Queda | Motivo |
|-----|-------|--------|
| `RegistrationSetup.temporaryId` | `registrationId` | Es el folio reservado y la llave de idempotencia |
| `RegistrationSetup.today: LocalDate` | `asOf: OffsetDateTime` + `val today get() = asOf.toLocalDate()` | Igual que `StudentProfile`; los usos de `setup.today` no cambian |
| `disciplines: List<DojoDiscipline>` + `belts: List<BeltRank>` + `maxStripes` | `disciplines: List<DisciplineOption>` con `discipline`, `belts: List<BeltOption>`, `maxStripes` | Cintas y rayas por disciplina |
| `BeltRank(belt, rankLabel: String)` | `BeltOption(belt: BeltColor, grade: BeltGrade?)` | La UI arma la etiqueta; `selectedRank` pasa a devolver `BeltGrade?` |
| `countryCode` | `phoneCountryCode` | Mismo nombre que el API |
| `TrainingSchedule.days: String` | `discipline: DojoDiscipline`, `weekdays: List<DayOfWeek>` | La UI formatea los días en es-MX |
| `TrainingSchedule.details: String` | `description: String?` | Texto del dojo, puede faltar |
| `monthlyFeeCents`, `enrollmentWaived`, `billingDay` | `plan: RegistrationPlan(monthlyFeeCents, currency: Currency, enrollmentWaived, billingDay)` | Cuota con su moneda |
| `StudentRegistrationForm` | Sin cambios | El request se arma en el mapper |

Derivados nuevos en `TkdStudentRegistrationUiState`: `beltOptions` (de la disciplina elegida), `maxStripes` y `visibleSchedules` (filtrados por disciplina).

### 6.2 Cadenas nuevas

| Clave | Valor |
|-------|-------|
| `tkd_register_schedule_days` | `%1$s y %2$s` (y una variante con coma para 3+ días) |
| `tkd_register_fee_period` | `%1$s/mes` (sustituye "USD/mes"; la moneda va en el monto) |
| `tkd_error_invalid` | `Revisa los campos marcados.` |
| `tkd_register_registration_expired` | `La ficha venció. Cargamos un folio nuevo; tus datos se conservan.` |

### 6.3 Dependencias que se arrastran

- **La lista no se entera del alta.** `TkdStudentsViewModel` carga en `init` y sobrevive en la pestaña. Propuesta: `TkdApp` deja `student_registered = true` en el `SavedStateHandle` de la entrada de la lista y `TkdStudentsScreen` llama `onRetry()` al leerlo. Alternativa a futuro: una señal compartida en la capa de datos cuando haya caché.
- **ADR-020 y `TkdApiCall`:** el `422` con `fields` es el primer error por campo del API; se documenta como modificación de ADR-020 y aplica también a `auth/register` si después lo necesita.
- **Moneda:** el `Locale.US` de `centsAsUsd()` se reemplaza por un helper con `Locale` es-MX y `Currency` del API (mismo pendiente que `payments/TkdPaymentsFormat.kt`).
- **Foto:** se sube después del `201` con el `studentId` (§8). Mientras no exista, los botones "Cámara Dojo"/"Subir Archivo" siguen sin acción.

### 6.4 Tests (TEST-06)

- **`TkdStudentRegistrationMapperTest`:**
  - setup completo, con cintas por disciplina, días y moneda;
  - códigos desconocidos (`BJJ`, `GOLD`, `HOLIDAY`) que se descartan sin fallar;
  - `toRequest()` para un menor, para un adulto (`guardian: null`), sin correo, sin emergencia y con teléfono E.164, comparado contra `students_create_request.json`.
- **`NetworkTkdStudentRegistrationRepositoryTest` (con `MockWebServer`):**
  - setup 200 con `Bearer`;
  - alta 201 con método `POST`, path `/students`, header `Idempotency-Key` y cuerpo igual a la fixture;
  - `401`, `409` (`Conflict` con `message`), `410`, `422` (`Invalid` con `fieldErrors`), `5xx`;
  - timeout y JSON malformado.
- **`TkdStudentRegistrationViewModelTest` (con la *Fake*):**
  - cambiar disciplina reinicia la cinta, recorta las rayas y limpia el horario ajeno;
  - un `422` marca los campos y muestra el mensaje;
  - reintentar tras un timeout reusa el mismo `registrationId`;
  - un `410` recarga el setup sin perder el formulario;
  - el alta correcta entrega el `studentId`.
- **Compose UI test:**
  - la etiqueta de la cinta y los días se arman en español;
  - solo aparecen los horarios de la disciplina elegida.

---

## 7. Decisiones abiertas

| # | Pregunta | Recomendación |
|---|----------|---------------|
| 1 | ¿El folio se reserva en un `GET`? | Aceptable para la primera versión. Si el backend prefiere no tener efectos en `GET`, `POST /student-registrations` → `{registrationId}` y el setup queda como `GET` puro y cacheable |
| 2 | ¿La credencial digital es síncrona? | No: el `201` no espera a WhatsApp. Si el envío falla, el backend lo registra y el perfil podría mostrar un aviso en una versión futura. La app solo muestra "Alumno registrado" |
| 3 | Toggle "Menor" en un alumno de 18+ o al revés | El servidor manda la edad a `asOf`: < 18 exige `guardian`; ≥ 18 acepta `guardian` opcional (un adulto puede tener tutor). La app ya ajusta el toggle al elegir la fecha |
| 4 | ¿Duplicados? | `409` por nombre normalizado + fecha de nacimiento dentro del dojo. Un "dar de alta de todas formas" (`?allowDuplicate=true`) queda fuera de la primera versión |
| 5 | ¿La cuota se negocia por alumno? | Hoy "pactada" es solo lectura. Si el dojo da descuentos, se agrega `plan.monthlyFeeCents` al request con permiso de rol |
| 6 | ¿Cupo por horario? | El setup solo lista horarios con cupo; si se llena entre el setup y el alta, `422` en `enrollment.scheduleId` con `NOT_AVAILABLE` |
| 7 | Teléfono de emergencia hoy es texto libre | Validarlo como el principal (10 dígitos + lada) en la app antes de enviar |
| 8 | `BJJ` y `MIXED` sin `DojoDiscipline` | Mismo pendiente que `/me` y la lista: se descartan del setup hasta agregarlos al enum |

---

## 8. Borradores y foto (P1, para diseñar con el backend)

| Acción | Endpoint | Request | Response | En la app |
|--------|----------|---------|----------|-----------|
| Guardar borrador | `PUT /student-drafts/{registrationId}` | El mismo `CreateStudentRequest`, pero todos los campos opcionales | `200` `{ registrationId, savedAt }` | `saveDraft(form, setup)`; idempotente (sobrescribe) |
| Listar borradores | `GET /student-drafts` | — | `200` `{ items: [{ registrationId, fullName, savedAt }] }` | Entrada "Retomar ficha" en la lista de alumnos (pantalla por diseñar) |
| Retomar borrador | `GET /student-drafts/{registrationId}` | — | `200` request parcial + `RegistrationSetup` vigente | La ficha arranca con el formulario lleno |
| Foto oficial | `PUT /students/{studentId}/photo` | `multipart/form-data` (`image/jpeg`, ≤ 2 MB) | `200` `{ photoUrl }` | Después del `201`; si falla, el alta ya quedó y se reintenta desde el perfil |

Al dar de alta con un `registrationId` que tenía borrador, el servidor borra el borrador.

Mientras no exista el endpoint de borradores, `saveDraft` guarda la ficha cifrada en el dispositivo (ADR-023, `TkdStudentDraftStore`) y la lista de alumnos ofrece "Retomar ficha". No se ve desde otro dispositivo.

---

## 9. Orden sugerido

1. **Backend:** publicar `GET /dojo/registration-setup` y `POST /students` con el contrato de §5. Incluye `Idempotency-Key` y el `422` con `fields`.
2. **App:** pegar §5 en `kensho-api.yaml`, agregar `TkdDataError.Invalid` + `fieldErrors` en `TkdApiCall` y anotar la modificación de ADR-020.
3. **App:** correr `/kensho-endpoint GET /dojo/registration-setup` y migrar el modelo (§6.1) y las secciones de disciplina y plan.
4. **App:** correr `/kensho-endpoint POST /students`, con la navegación al perfil y la recarga de la lista (§6.3).
5. **Backend y app:** borradores y foto (§8).
