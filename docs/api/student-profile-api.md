# API para el perfil del alumno

| Dato | Valor |
|------|-------|
| Pantalla | `profile/TkdStudentProfileScreen.kt` (ruta `student/{studentId}`) · `TkdProfileHeroCard.kt` · `TkdProfileProgressCards.kt` |
| Estado actual | **Implementado en la app (2026-11-06)**: `NetworkTkdStudentProfileRepository` + VM `@HiltViewModel`; el contrato ya está en `kensho-api.yaml`. Antes: `FakeTkdStudentProfileRepository` (prototipo, nodo 1:673), `fun interface` síncrona que devuelve `StudentProfile?`, VM con default a la *Fake*, `LocalDate.now()` como "hoy", fotos `@DrawableRes`, textos de rango en inglés armados en datos |
| Depende de | `GET /students` (`docs/api/students-api.md`): el `studentId` sale de `items[].id` de la lista |
| Contrato existente | `docs/api/kensho-api.yaml` (`auth`, `me`, `students`) |
| Fecha | 2026-11-06 |
| Objetivo | Definir el endpoint que alimenta `TkdStudentProfileRepository` para construirlo en el backend y consumirlo con `/kensho-endpoint` |

---

## 1. Resumen y decisión

| # | Endpoint | Para qué | Prioridad |
|---|----------|----------|-----------|
| 1 | `GET /students/{studentId}` | Todo lo que muestra el perfil: identidad, rango actual y siguiente, examen, asistencia, temario y última observación | **P0** (este documento; implementado en la app el 2026-11-06) |
| 2 | `POST /students/{studentId}/requirements/{requirementId}/verification` | Botón "Verificar" de un requisito (`onVerifyRequirement`) | P1 (§7) |
| 3 | `PUT /students/{studentId}/graduation-readiness` | Botón "Marcar listo para graduación" (`onMarkReadyForGraduation`) | P1 (§7) |

**Decisiones clave:**

- **Una sola llamada por pantalla.** El perfil necesita datos de 4 dominios (alumno, asistencia, temario y observaciones), pero la pantalla los muestra todos a la vez. El servidor los junta en un solo cuerpo; no hay que encadenar varias peticiones desde el móvil.
- **Mismas piezas que la lista.** `rank` usa el mismo schema `StudentRank` que `GET /students`, y el mapper reutiliza `toBeltColor()` y `toGrade()`. La tarjeta y el perfil muestran la misma cinta con la misma etiqueta.
- **El API manda hechos de dominio, no textos de pantalla** (misma regla que `students-api.md`). Hoy la *Fake* trae `beltName = "Green Belt"`, `rank = "6th Kup"`, `rankShort = "6K"`, `targetLabel = "Blue Belt (5th Kup)"` y `requirementsTitle = "6th Kup → 5th Kup Requirements"`: todo eso lo arma la app con `strings.xml` a partir de `rank` y `nextRank`.
- **Excepción: el contenido del dojo sí llega como texto.** El nombre del programa ("Shotokan Karate & Kobudo"), los títulos y descripciones de los requisitos y el texto de la observación los escribe el dojo, no la app. Se muestran tal cual.
- **"Hoy" viene del servidor.** `asOf` alimenta los "días para el examen", los "hace N días" y los "N meses en el dojo", y elimina el `LocalDate.now()` del `UiState` (TD-KEN-005).
- **Las métricas agregadas las calcula el servidor.** La racha, el promedio semanal y las horas totales requieren todo el historial de asistencia, que la app no descarga.

---

## 2. Mapa pantalla → datos

### 2.1 Tarjeta principal (`TkdProfileHeroCard`)

| En pantalla | Campo de `StudentProfile` hoy | Origen en el API | Nota |
|-------------|-------------------------------|------------------|------|
| Foto con aro del color de la cinta | `photo` (`@DrawableRes`), `belt` | `photoUrl`, `rank.belt` | Sin foto o sin Coil → iniciales (TD-KEN-012) |
| Badge del rango ("6K") | `rankShort` | `rank.grade` | La app lo arma: `tkd_profile_rank_short_kup` = `%1$dK` |
| Nombre, edad | `name`, `age` | `displayName`, `age` | `age`, no `birthDate`: muchos alumnos son menores |
| "Activo" | `isActive` | `membership.status` | Código `ACTIVE` · `PAUSED` · `INACTIVE` |
| "Inscrito sep 2023 · 26 meses" | `enrolledOn` (`YearMonth`) | `membership.enrolledOn` (`yyyy-MM-dd`) | La app toma el mes; los meses se cuentan contra `asOf` |
| Programa ("Shotokan Karate & Kobudo") | `discipline: String` | `program.name` (+ `program.discipline` como código) | Texto del dojo |
| Banner "Cinta verde · 6.º kup" + rayas | `beltName`, `rank`, `stripes` | `rank` | Misma etiqueta que la tarjeta de la lista |
| "Siguiente: Cinta azul (5.º kup)" | `targetBelt`, `targetLabel` | `nextRank` | `null` en la cinta máxima → se oculta la fila |
| Fecha y cuenta atrás del examen | `examDate: LocalDate` | `nextExam.on` | Puede ser `null` → "Sin examen programado" |

### 2.2 Asistencia y estadísticas (`TkdProfileProgressCards`)

| En pantalla | Campo hoy | Origen en el API | Nota |
|-------------|-----------|------------------|------|
| "22/24 clases", %, "faltan N" | `attended`, `requiredClasses`, derivados | `attendance.attended`, `attendance.required` | Mismo ciclo de examen que la lista |
| "Mínimo 90 %" y "va a buen ritmo" | `minimumAttendance: Float` | `attendance.minimumPercent: Int` | Entero 0–100 para no mandar decimales binarios; la app divide entre 100 |
| Racha de semanas | `streakWeeks` | `attendance.streakWeeks` | Calculado por el servidor |
| Clases por semana ("2.8") | `classesPerWeek: Float` | `attendance.classesPerWeek` | Número con un decimal; la app lo formatea con `Locale` es-MX (hoy usa `Locale.US`, deuda) |
| Horas totales | `lifetimeHours` | `attendance.lifetimeHours` | Entero, redondeado por el servidor |

### 2.3 Temario y observación

| En pantalla | Campo hoy | Origen en el API | Nota |
|-------------|-----------|------------------|------|
| Título "6.º kup → 5.º kup" | `requirementsTitle` | `rank` + `nextRank` | La app lo arma |
| "Verifica: Sensei Marcus" | `verifier` | `syllabus.verifierName` | `null` → se oculta |
| Requisitos (título, descripción, verificado el…) | `requirements[]` | `syllabus.requirements[]` | `verifiedOn: null` = pendiente; `id` estable para el POST de §7 |
| Contador "3/5 verificados" | `verifiedCount`, `verifiedRate` | Derivado | Ya son propiedades del modelo |
| Observación del sensei (texto, autor, hace N días) | `observation` | `latestObservation` | Solo la más reciente; `null` → se oculta la tarjeta |
| Miniatura del video | `observation.clipPreview` (`@DrawableRes`) | `latestObservation.clipThumbnailUrl` | Mismo pendiente de imágenes remotas |
| Botón "Marcar listo" ya pulsado | `TkdStudentProfileUiState.markedForGraduation` | `graduation.markedReady` | Hoy solo vive en memoria; con §7 se persiste |

---

## 3. Endpoint `GET /students/{studentId}`

| Aspecto | Valor |
|---------|-------|
| Auth | `Authorization: Bearer <accessToken>` (lo agrega `TkdAuthInterceptor`; 401 lo maneja `TkdTokenAuthenticator`) |
| Path param | `studentId`: el `id` de `GET /students` (opaco, p. ej. `st-kenji-sato`) |
| Alcance | El dojo sale del token. Un alumno de otro dojo responde **404**, no 403, para no revelar que existe |
| Caché | `ETag` + `If-None-Match` → `304`; respuesta con gzip |
| Tamaño esperado | ~2–4 KB (5–15 requisitos) |

### 3.1 Respuesta de ejemplo (fixture `students_profile.json`)

```json
{
  "asOf": "2026-11-05T17:30:00-07:00",
  "id": "st-kenji-sato",
  "displayName": "Kenji Sato",
  "age": 14,
  "photoUrl": "https://cdn.treecode.dev/students/st-kenji-sato.jpg",
  "membership": { "status": "ACTIVE", "enrolledOn": "2023-09-04" },
  "program": { "discipline": "KARATE", "name": "Shotokan Karate & Kobudo" },
  "rank": {
    "discipline": "KARATE",
    "belt": "GREEN",
    "grade": { "system": "KUP", "value": 6 },
    "stripes": 2
  },
  "nextRank": { "belt": "BLUE", "grade": { "system": "KUP", "value": 5 } },
  "nextExam": { "id": "ex-2026-11-28", "on": "2026-11-28" },
  "attendance": {
    "attended": 22,
    "required": 24,
    "minimumPercent": 90,
    "streakWeeks": 6,
    "classesPerWeek": 2.8,
    "lifetimeHours": 84
  },
  "syllabus": {
    "verifierName": "Sensei Marcus",
    "requirements": [
      { "id": "kihon", "title": "Kihon (Fundamentos)", "description": "Gyaku-zuki, shuto-uke, yoko-geri", "verifiedOn": "2026-10-12" },
      { "id": "kata", "title": "Kata", "description": "Heian Sandan", "verifiedOn": "2026-10-15" },
      { "id": "kumite", "title": "Kumite", "description": "Sanbon kumite: defensa y contraataque", "verifiedOn": "2026-10-18" },
      { "id": "dojo-kun", "title": "Disciplina y Dojo Kun", "description": "Recitación oral y conducta", "verifiedOn": "2026-10-05" },
      { "id": "tameshiwari", "title": "Tameshiwari", "description": "Patada lateral a tabla de pino de 1 pulgada", "verifiedOn": null }
    ]
  },
  "latestObservation": {
    "id": "obs-8812",
    "text": "Excelente rotación de cadera en la patada lateral. Le falta un rompimiento limpio antes del examen.",
    "authorName": "Sensei Marcus",
    "observedOn": "2026-10-14",
    "clipThumbnailUrl": "https://cdn.treecode.dev/clips/obs-8812.jpg"
  },
  "graduation": { "markedReady": false }
}
```

Variantes que también van como fixture: `students_profile_minimal.json` (sin foto, sin `grade`, `nextRank`, `nextExam` ni `latestObservation`, requisitos vacíos) y `students_profile_unknown_codes.json` (`belt: "GOLD"`, `membership.status: "SUSPENDED"`).

### 3.2 Campos

| Campo | Tipo | Requerido | Nulo | Descripción |
|-------|------|-----------|------|-------------|
| `asOf` | string `date-time` | Sí | No | "Ahora" del servidor con el offset del dojo; base de todas las cuentas de días y meses |
| `id` | string | Sí | No | Igual al `studentId` del path |
| `displayName` | string | Sí | No | Nombre para mostrar |
| `age` | integer ≥ 0 | Sí | No | Edad en años a la fecha `asOf` |
| `photoUrl` | string `uri` | Sí | **Sí** | Foto del alumno |
| `membership.status` | `MembershipStatus` | Sí | No | Estado de la inscripción |
| `membership.enrolledOn` | string `date` | Sí | No | Fecha de inscripción |
| `program.discipline` | `DisciplineCode` | Sí | No | Mismo catálogo que `/me` |
| `program.name` | string | Sí | No | Nombre del programa que define el dojo |
| `rank` | `StudentRank` | Sí | No | El mismo schema que la lista |
| `nextRank` | `{ belt, grade }` | Sí | **Sí** | Rango al que aspira; `null` si no hay siguiente |
| `nextExam` | `{ id, on }` | Sí | **Sí** | Próximo examen asignado |
| `attendance.attended` / `required` | integer ≥ 0 | Sí | No | Clases del ciclo de examen actual |
| `attendance.minimumPercent` | integer 0–100 | Sí | No | Mínimo exigido por la escuela |
| `attendance.streakWeeks` | integer ≥ 0 | Sí | No | Semanas seguidas con al menos una clase |
| `attendance.classesPerWeek` | number ≥ 0 | Sí | No | Promedio de las últimas 8 semanas, 1 decimal |
| `attendance.lifetimeHours` | integer ≥ 0 | Sí | No | Horas totales en el tatami |
| `syllabus.verifierName` | string | Sí | **Sí** | Quién verifica el temario |
| `syllabus.requirements[]` | array | Sí | No (puede ir vacío) | En el orden del temario |
| `requirements[].id` | string | Sí | No | Estable: es el `requirementId` del POST de §7 |
| `requirements[].title` / `description` | string | Sí / No | No | Texto del dojo; `description` puede omitirse |
| `requirements[].verifiedOn` | string `date` | Sí | **Sí** | `null` = pendiente |
| `latestObservation` | objeto | Sí | **Sí** | Observación más reciente |
| `latestObservation.clipThumbnailUrl` | string `uri` | No | **Sí** | Miniatura del video, si hay |
| `graduation.markedReady` | boolean | Sí | No | Si el instructor ya lo marcó listo para graduación |

### 3.3 Catálogos

| Catálogo | Valores | En la app |
|----------|---------|-----------|
| `BeltCode`, `StudentRank.grade.system`, `DisciplineCode` | Los de `kensho-api.yaml` (compartidos con `/students` y `/me`) | `BeltColor`, `GradeSystem`, `DojoDiscipline` |
| `MembershipStatus` (nuevo) | `ACTIVE`, `PAUSED`, `INACTIVE` | `isActive = status == ACTIVE`; a futuro un enum para el badge |

**Compatibilidad hacia adelante** (igual que la lista):
- **Cinta desconocida:** paleta neutra y "Cinta no reconocida".
- **Grado desconocido:** se muestra la cinta sin el grado.
- **`membership.status` desconocido:** el perfil se muestra como no activo, sin badge.
- **Nunca se descarta todo el perfil** por un código nuevo.

### 3.4 Respuestas de error

| HTTP | Cuándo | En la app |
|------|--------|-----------|
| `200` | Perfil encontrado | Contenido |
| `304` | Sin cambios desde el `ETag` | OkHttp sirve la caché (si se activa `Cache`) |
| `401` | Token inválido o vencido | `Unauthorized`: refresh; si falla, regresa a login |
| `404` | El alumno no existe, fue dado de baja o es de otro dojo | `NotFound`: estado "No encontramos a este alumno" con botón para regresar (sin reintentar) |
| `5xx` | Falla del servidor | `Server`: error con "Reintentar" |

El cuerpo de error es `{code, message}` (schema `Error`, ADR-020).

---

## 4. Contrato OpenAPI (para pegar en `kensho-api.yaml` cuando el backend lo publique)

```yaml
paths:
  /students/{studentId}:
    get:
      tags: [students]
      operationId: studentProfile
      summary: Perfil completo de un alumno del dojo de la sesión
      description: >-
        Un alumno de otro dojo responde 404. Los textos del temario, del programa y de la observación son
        contenido del dojo; los nombres de cinta y grado los arma la app a partir de los códigos.
      parameters:
        - in: path
          name: studentId
          required: true
          schema: { type: string }
        - in: header
          name: If-None-Match
          required: false
          schema: { type: string }
      responses:
        '200':
          description: Perfil del alumno
          headers:
            ETag: { schema: { type: string } }
          content:
            application/json:
              schema: { $ref: '#/components/schemas/StudentProfile' }
        '304': { description: Sin cambios desde el ETag enviado }
        '401': { $ref: '#/components/responses/Unauthorized' }
        '404': { $ref: '#/components/responses/NotFound' }

components:
  responses:
    NotFound:
      description: Recurso inexistente o fuera del dojo de la sesión
      content:
        application/json:
          schema: { $ref: '#/components/schemas/Error' }
  schemas:
    StudentProfile:
      type: object
      required: [asOf, id, displayName, age, photoUrl, membership, program, rank, nextRank, nextExam,
                 attendance, syllabus, latestObservation, graduation]
      properties:
        asOf: { type: string, format: date-time }
        id: { type: string }
        displayName: { type: string, minLength: 1 }
        age: { type: integer, minimum: 0 }
        photoUrl: { type: [string, 'null'], format: uri }
        membership:
          type: object
          required: [status, enrolledOn]
          properties:
            status: { $ref: '#/components/schemas/MembershipStatus' }
            enrolledOn: { type: string, format: date }
        program:
          type: object
          required: [discipline, name]
          properties:
            discipline: { $ref: '#/components/schemas/DisciplineCode' }
            name: { type: string }
        rank: { $ref: '#/components/schemas/StudentRank' }
        nextRank:
          oneOf:
            - type: 'null'
            - type: object
              required: [belt, grade]
              properties:
                belt: { $ref: '#/components/schemas/BeltCode' }
                grade: { $ref: '#/components/schemas/BeltGrade' }
        nextExam:
          oneOf:
            - type: 'null'
            - type: object
              required: [id, on]
              properties:
                id: { type: string }
                on: { type: string, format: date }
        attendance:
          type: object
          required: [attended, required, minimumPercent, streakWeeks, classesPerWeek, lifetimeHours]
          properties:
            attended: { type: integer, minimum: 0 }
            required: { type: integer, minimum: 0 }
            minimumPercent: { type: integer, minimum: 0, maximum: 100 }
            streakWeeks: { type: integer, minimum: 0 }
            classesPerWeek: { type: number, minimum: 0 }
            lifetimeHours: { type: integer, minimum: 0 }
        syllabus:
          type: object
          required: [verifierName, requirements]
          properties:
            verifierName: { type: [string, 'null'] }
            requirements:
              type: array
              items: { $ref: '#/components/schemas/Requirement' }
        latestObservation:
          oneOf:
            - type: 'null'
            - $ref: '#/components/schemas/Observation'
        graduation:
          type: object
          required: [markedReady]
          properties:
            markedReady: { type: boolean }
    Requirement:
      type: object
      required: [id, title, verifiedOn]
      properties:
        id: { type: string }
        title: { type: string }
        description: { type: string }
        verifiedOn: { type: [string, 'null'], format: date }
    Observation:
      type: object
      required: [id, text, authorName, observedOn]
      properties:
        id: { type: string }
        text: { type: string }
        authorName: { type: string }
        observedOn: { type: string, format: date }
        clipThumbnailUrl: { type: [string, 'null'], format: uri }
    MembershipStatus:
      type: string
      enum: [ACTIVE, PAUSED, INACTIVE]
    BeltGrade:
      oneOf:
        - type: 'null'
        - type: object
          required: [system, value]
          properties:
            system: { type: string, enum: [KUP, DAN, KYU] }
            value: { type: integer, minimum: 1 }
```

> Al pegarlo, conviene que `StudentRank.grade` (ya publicado para `/students`) también apunte a `#/components/schemas/BeltGrade`, para no duplicar la definición.

---

## 5. Arquitectura en la app (ADR-016/017)

Se construye con `/kensho-endpoint GET /students/{studentId}` y la respuesta de §3.1.

```
TkdApp  student/{studentId} ──▶ TkdStudentProfileScreen ── hiltViewModel() ──▶ TkdStudentProfileViewModel (@HiltViewModel)
                                                                │  SavedStateHandle["studentId"]
                                                                │  estado: Loading → Content | NotFound | Error(TkdUiText) · onRetry()
                                                                │  today = profile.asOf.toLocalDate()   (adiós LocalDate.now())
                                                                ▼
                                          TkdStudentProfileRepository (suspend + TkdResult)
                                             ├─ NetworkTkdStudentProfileRepository ── tkdApiCall(io) { api.studentProfile(id).toProfile() }
                                             └─ FakeTkdStudentProfileRepository (prototipo: Kenji + derivados de la lista)
                                                                ▼
                        TkdStudentsApi (mismo tag `students`) · dto/TkdStudentProfileDto.kt · mapper/TkdStudentProfileMapper.kt
```

| Capa | Cambio |
|------|--------|
| `data/remote/api/TkdStudentsApi.kt` | Pasa de `fun interface` a `interface` y agrega `@GET("students/{studentId}") suspend fun studentProfile(@Path("studentId") studentId: String): StudentProfileDto`. Mismo `@Provides`, sin Api nueva |
| `data/remote/dto/TkdStudentProfileDto.kt` | `StudentProfileDto`, `MembershipDto`, `ProgramDto`, `NextRankDto`, `NextExamDto`, `ProfileAttendanceDto`, `SyllabusDto`, `RequirementDto`, `ObservationDto`, `GraduationDto`. Reutiliza `StudentRankDto` y `GradeDto` de `TkdStudentsDto.kt` |
| `data/remote/mapper/TkdStudentProfileMapper.kt` | `StudentProfileDto.toProfile()`. Reutiliza `toBeltColor()` y `toGrade()`; las fechas pasan a `LocalDate`, `enrolledOn` a `YearMonth.from(...)`, `minimumPercent / 100f` y `asOf` a `OffsetDateTime` |
| `model/TkdStudentProfileUiState.kt` | En `StudentProfile` (detalle abajo). En el `UiState` se agregan `isLoading`, `loadError: TkdUiText?` y `notFound: Boolean`; `today` deja de tener `LocalDate.now()` como default |
| `data/TkdStudentProfileRepository.kt` | Pasa a `suspend fun profile(studentId: String): TkdResult<StudentProfile>`; `NotFound` reemplaza al `null`. La *Fake* gana `@Inject constructor()` y `failure`, y deja de inventar textos en inglés |
| `data/NetworkTkdStudentProfileRepository.kt` | Nuevo, de una línea por método |
| `di/DataModule.kt` | `@Provides` igual que con la lista: `Network`, o la *Fake* si está `AUTH_STATIC_MOCK` |
| `profile/TkdStudentProfileViewModel.kt` | `@HiltViewModel @Inject constructor(savedStateHandle, repository, dojoRepository)` sin defaults (−3 en FF-15); carga en `init`; `onRetry()`. `onVerifyRequirement` y `onMarkReadyForGraduation` siguen locales hasta §7 |
| `profile/TkdStudentProfileScreen.kt` | `hiltViewModel()` en vez de `viewModel(key = studentId) { … }`. Estados de carga, de "no encontrado" (con `actions.onBack`) y de error con `TkdRetrySnackbarEffect` |
| `TkdProfileHeroCard` / `TkdProfileProgressCards` | Etiquetas armadas con `strings.xml` (§5.2); `nextRank` y `nextExam` opcionales; `classesPerWeek` con `Locale` es-MX |

### 5.1 Cambios en `StudentProfile`

| Hoy | Queda | Motivo |
|-----|-------|--------|
| `photo: @DrawableRes Int?` | `photo` (prototipo) + `photoUrl: String?` | Igual que `Student`, hasta que haya Coil |
| `discipline: String` | `programName: String` | Es el nombre que da el dojo, no el catálogo |
| `belt`, `beltName`, `rank`, `rankShort`, `stripes` | `belt: BeltColor?`, `grade: BeltGrade?`, `stripes` | Los textos los arma la UI |
| `targetBelt`, `targetLabel` | `nextRank: NextRank?` (`belt: BeltColor?`, `grade: BeltGrade?`) | Puede no haber siguiente rango |
| `examDate: LocalDate` | `nextExamOn: LocalDate?` | Puede no haber examen programado |
| `minimumAttendance: Float` | Igual (el mapper divide `minimumPercent` entre 100) | — |
| `requirementsTitle: String` | Se elimina | Se deriva de `grade` y `nextRank` |
| `verifier: String` | `verifier: String?` | Puede no estar asignado |
| `observation.clipPreview: @DrawableRes Int?` | `+ clipThumbnailUrl: String?` | Mismo pendiente de imágenes |
| — | `asOf: OffsetDateTime`, `markedForGraduation: Boolean` | Sustituye `LocalDate.now()`; el estado persistido viene del servidor |

**Etiqueta de la cinta compartida:** la función `Student.beltLabel()` de `TkdStudentCard` se generaliza a una que reciba `(belt: BeltColor?, grade: BeltGrade?)`, para usarla en la tarjeta, el banner del perfil y "Siguiente: …".

### 5.2 Cadenas nuevas (`strings.xml`, bloque del perfil)

| Clave | Valor |
|-------|-------|
| `tkd_profile_rank_short_kup` / `_dan` / `_kyu` | `%1$dK` / `%1$dD` / `%1$dKyu` |
| `tkd_profile_requirements_title` | `Requisitos: %1$s → %2$s` |
| `tkd_profile_requirements_title_single` | `Requisitos de %1$s` (sin `nextRank`) |
| `tkd_profile_no_exam` | `Sin examen programado` |
| `tkd_profile_not_found` | `No encontramos a este alumno.` |

### 5.3 Dependencias que se arrastran

- **`FakeTkdStudentProfileRepository`** hoy busca al alumno en `FakeTkdStudentsRepository`. Cuando la lista venga de red, los ids del API (`st-…`) no existen en la *Fake*. Con el repositorio de red ese hueco desaparece; la *Fake* queda solo para tests, previews y el mock estático.
- **TD-KEN-005:** al tomar "hoy" de `asOf` se elimina el `LocalDate.now()` del `UiState` y del VM del perfil.
- **Fotos y miniatura por URL:** mismo pendiente que la lista (Coil + modelo de imagen, TD-KEN-012).
- **Precarga opcional:** la app puede pintar el encabezado (nombre, foto, cinta) con el `Student` de la lista mientras llega el perfil. Requiere pasar el resumen por navegación o caché, así que queda fuera de la primera versión.

### 5.4 Tests (TEST-06)

- **`TkdStudentProfileMapperTest` (con fixtures):**
  - el perfil completo;
  - el perfil mínimo (sin `nextRank`, `nextExam` ni observación, requisitos vacíos);
  - códigos desconocidos;
  - `minimumPercent` 90 → 0.9;
  - `enrolledOn` → `YearMonth`.
- **`NetworkTkdStudentProfileRepositoryTest` (con `MockWebServer`):**
  - 200 con path `/students/st-kenji-sato` y `Bearer`;
  - un id con caracteres especiales, para verificar la codificación del `@Path`;
  - 401, 404 → `NotFound`, 5xx;
  - timeout y JSON malformado.
- **`TkdStudentProfileViewModelTest` (con la *Fake* y `SavedStateHandle`):**
  - carga correcta, con `today` igual a la fecha de `asOf`;
  - `NotFound` → `notFound`;
  - error y reintento;
  - `onVerifyRequirement` y `onMarkReadyForGraduation`, que siguen locales.
- **Compose UI test:**
  - sin `nextExam` se muestra "Sin examen programado";
  - sin observación se oculta la tarjeta;
  - `NotFound` muestra el mensaje y el botón para regresar.

---

## 6. Decisiones abiertas

| # | Pregunta | Recomendación |
|---|----------|---------------|
| 1 | ¿`attendance` es del ciclo de examen o de un periodo fijo? | Ciclo de examen, igual que la lista; documentarlo en el contrato para ambos endpoints |
| 2 | ¿Promedio semanal sobre qué ventana? | Últimas 8 semanas; el servidor lo fija y lo documenta |
| 3 | ¿Solo la última observación o un historial? | Solo la última en este endpoint. Un historial iría en `GET /students/{id}/observations?cursor=` si se diseña esa pantalla |
| 4 | ¿El temario es por alumno o por rango del dojo? | Por rango del dojo, con el estado de verificación por alumno. El `id` del requisito debe ser estable entre alumnos |
| 5 | ¿Alumno dado de baja: 404 o 200 con `INACTIVE`? | 200 con `INACTIVE` si sigue en la base (el instructor puede consultar su historial); 404 solo si no existe o es de otro dojo |
| 6 | ¿Multi-dojo? | Mismo pendiente que la lista: si un instructor administra varios, `/dojos/{dojoId}/students/{studentId}` |
| 7 | Disciplinas `BJJ` y `MIXED` sin `DojoDiscipline` | Mismo pendiente que `/me` |

---

## 7. Mutaciones del perfil (P1, para diseñar con el backend)

Hoy los dos botones solo cambian el estado en memoria y se pierden al salir de la pantalla. Propuesta:

| Acción | Endpoint | Request | Response | En la app |
|--------|----------|---------|----------|-----------|
| Verificar requisito | `POST /students/{studentId}/requirements/{requirementId}/verification` | Sin cuerpo; el servidor toma la fecha y el instructor del token | `200` con el `Requirement` actualizado (`verifiedOn`) · `409` si ya estaba verificado | Actualización optimista y reversión si falla |
| Marcar listo para graduación | `PUT /students/{studentId}/graduation-readiness` | `{ "markedReady": true }` | `200` con `{ "markedReady": true }` | Igual. Es idempotente, por eso `PUT` |

Usar la fecha del servidor (y no la del teléfono) evita verificaciones con fecha equivocada si el reloj del dispositivo está mal.

---

## 8. Orden sugerido

1. **Backend:** publicar `GET /students/{studentId}` con el contrato de §4.
2. **App:** pegar §4 en `kensho-api.yaml`; correr `/kensho-endpoint GET /students/{studentId}` con la respuesta de §3.1 (DTO, mapper, repositorio de red, DI y tests).
3. **App:** migrar `StudentProfile` y la UI del perfil (§5.1 y §5.2), el VM a Hilt con `SavedStateHandle`, y los estados de carga, error y "no encontrado".
4. **Backend y app:** las mutaciones de §7.
5. **App:** Coil y el modelo de imagen para las fotos de la lista, del perfil y la miniatura de la observación (TD-KEN-012).
