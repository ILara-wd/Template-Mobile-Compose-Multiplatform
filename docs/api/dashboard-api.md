# API para la pantalla principal (Resumen del dojo)

| Dato | Valor |
|------|-------|
| Pantalla | `ui/present/TkdDashboardScreen.kt` (pestaña **Overview**, `startDestination` tras el login) |
| Estado actual | Datos de `FakeTkdDashboardRepository` (prototipo de Figma), repositorio síncrono, VM con default a la *Fake* |
| Contrato existente | `docs/api/kensho-api.yaml` (solo `auth`) |
| Fecha | 2026-11-03 |
| Objetivo | Definir lo que el backend debe exponer para pintar el resumen con datos reales (ADR-017, TD-KEN-013) |


> **Actualización 2026-11-07** (ver el plan general en [README.md](README.md)):
> - El encabezado (§2.1) ya no sale de este endpoint: el nombre del dojo y del instructor vienen de `GET /me`, y el
>   logo y la foto se proponen como campos opcionales de `/me` (README §2). `dojo` e `instructor` de §3.1 quedan solo
>   como datos de contexto.
> - Pregunta §7.9 resuelta: el catálogo es `KARATE`, `BJJ`, `TAEKWONDO` (+ `MIXED` solo como disciplina principal).
> - La sesión en vivo (§2.4) y la clase activa (§2.3) deben salir de las sesiones de [checkin-api.md](checkin-api.md)
>   (`GET /attendance/today`), no de datos propios del Resumen.
> - La pantalla ahora vive en `feature/home/dashboard/TkdDashboardScreen.kt` y su VM ya usa Hilt.

> **Actualización 2026-11-22:** el encabezado, las acciones rápidas, la agenda, los avisos y los ingresos ya salen de
> `GET me/profile` (ADR-021). Este endpoint solo hace falta para la sesión en vivo (§2.4), las métricas (§2.5), el
> filtro (§2.6) y los exámenes (§2.7), que hoy están ocultos (`AUTH_STATIC_MOCK`). Ver TD-KEN-013.

---

## 1. Resumen

La pantalla tiene **7 bloques**. Todos menos el buscador se alimentan de datos del dojo de la sesión. La propuesta es:

| # | Endpoint | Para qué | Prioridad |
|---|----------|----------|-----------|
| 1 | `GET /dojo/overview` | Carga inicial de toda la pantalla en **una sola llamada** | **P0** |
| 2 | `GET /dojo/live-session` | Refrescar solo la sesión en curso (asistencia en vivo) | P1 |
| 3 | `GET /belt-tests?status=upcoming&discipline={code}` | "Ver todos" y filtrado por disciplina en servidor | P1 |
| 4 | `GET /search?q=` | Buscador del encabezado (hoy no hace nada) | P2 |

**Recomendación:** empezar con el endpoint agregado (1). Una pantalla de inicio que dispara 6–8 llamadas en paralelo complica la gestión de `Loading/Error` y la latencia en red móvil. Los endpoints 2–4 se agregan cuando existan las pantallas o los comportamientos que los necesitan.

---

## 2. Mapa pantalla → datos

Para cada bloque: qué se pinta, de qué campo del modelo sale hoy y qué debería mandar el API. **Regla:** el API manda **hechos de dominio**; lo visual (colores, tonos, iconos, textos fijos, formatos) lo decide la app.

### 2.1 Barra superior (`TkdTopBar`)

| Se pinta | Modelo hoy (`DojoHeader`) | Debe venir del API | Notas |
|----------|---------------------------|--------------------|-------|
| Nombre del dojo | `name: String` | `dojo.name` | |
| "Sensei Marcus • Black Belt 4th Dan" | `instructor: String` (texto armado) | `instructor.displayName`, `instructor.title`, `instructor.rank.belt`, `instructor.rank.degree` | La app arma la línea con `strings.xml` (sin concatenar) |
| Logo del dojo | `logo: @DrawableRes Int` | `dojo.logoUrl` | URL → `TkdImage` en el mapper (ADR-017) |
| Foto del instructor | `profilePhoto: @DrawableRes Int` | `instructor.photoUrl` | Ídem; `null` → avatar con iniciales |

### 2.2 Buscador (`TkdSearchField`)

| Se pinta | Modelo hoy | Debe venir del API | Notas |
|----------|------------|--------------------|-------|
| Campo de búsqueda | `query: String` (solo se guarda) | — (endpoint 4 aparte) | Hoy `onQueryChange` no filtra ni navega. Hay que definir qué busca (alumnos, clases…) antes de diseñar el endpoint |

### 2.3 Barra de acciones rápidas (`TkdQuickActionBar`)

| Se pinta | Modelo hoy (`ActiveClass?`) | Debe venir del API | Notas |
|----------|-----------------------------|--------------------|-------|
| "Youth Karate (5:30 PM)" | `name`, `startTime: String` | `activeClass.name`, `activeClass.startsAt` (ISO-8601 con offset) | La app formatea la hora con `Locale` es-MX |
| "22 min left" | `minutesLeft: Int` | `activeClass.endsAt` | La app calcula `endsAt − asOf`. Si el servidor manda minutos, quedan viejos en cuanto llegan |
| "Dojo Mat A" | `mat: String` | `activeClass.area.name` | |
| Bloque oculto | `activeClass == null` | `activeClass: null` | Sin clase en curso o próxima |
| "+ Quick Add Student" | — | — | Navega al alta de alumno (fuera de alcance) |

### 2.4 Tarjeta de sesión en vivo (`TkdLiveSessionCard`)

| Se pinta | Modelo hoy (`LiveSession?`) | Debe venir del API | Notas |
|----------|-----------------------------|--------------------|-------|
| Título de la sesión | `title` | `liveSession.title` | |
| "Dojo Floor 1 • Master Marcus presiding" | `location`, `presiding` | `liveSession.area.name`, `liveSession.instructor.displayName` | |
| "24 / 28 Present" | `present`, `capacity` | `liveSession.presentCount`, `liveSession.capacity` | |
| "86% Capacity", "4 spots remaining" | derivados (`occupancy`, `spotsRemaining`) | — | Ya se calculan en el modelo; **no** mandarlos |
| Avatares + "+21" | `attendeePhotos: List<@DrawableRes>`, `extraAttendees` | `liveSession.attendeesPreview[]` (máx. 3: `id`, `displayName`, `photoUrl`) | `extraAttendees = presentCount − attendeesPreview.size` lo calcula la app; hoy el *Fake* manda 21 con 24 presentes y 3 fotos: es coherente |
| "Open Mat Sheet" | — | `liveSession.id` | Para navegar al pase de lista de esa sesión |
| Tarjeta oculta | `liveSession == null` | `liveSession: null` | |

### 2.5 Fila de métricas (`TkdMetricsRow`)

| Se pinta | Modelo hoy (`DojoMetrics`) | Debe venir del API | Notas |
|----------|----------------------------|--------------------|-------|
| "148" Alumnos activos | `activeStudents: Int` | `metrics.activeStudents.value` | |
| Badge "+12" | `activeStudentsDelta: Int` | `metrics.activeStudents.delta` | ⚠️ El string es `+%1$d`: un delta negativo se ve "+-3". Corregir en la app con un formato con signo |
| Sparkline | `ic_tkd_sparkline` (estático) | `metrics.activeStudents.trend: [Int]` (opcional) | Hoy es una imagen fija; si se quiere real, mandar 7–12 puntos |
| "92.4%" Asistencia | `attendanceRate: Float` (0–1) | `metrics.attendance.rate` (0–1) | Fracción, no porcentaje: el formato es de la app |
| Badge "+3.1%" | `attendanceDelta: Float` | `metrics.attendance.delta` (puntos de fracción) | Mismo problema de signo |
| "18" Listos para examen | `readyForExam: Int` | `metrics.readyForExam` | "Action Req." es fijo de la app |
| Periodo de comparación | implícito | `metrics.period` (`from`, `to`) y `comparedTo` | Sin esto el delta no se puede explicar ni probar |

### 2.6 Filtro de disciplinas (`TkdFilterChipRow`)

| Se pinta | Modelo hoy | Debe venir del API | Notas |
|----------|------------|--------------------|-------|
| Chips "Todas · Karate · BJJ · Taekwondo" | `disciplines: List<Discipline(id, name)>` | `disciplines: [DisciplineCode]` del dojo | Reutilizar `DisciplineCode` del contrato; la app traduce el código a `strings.xml`. ⚠️ El enum `DojoDiscipline` de la app tiene 2 valores y el contrato 4 (ver PR #1, V-01) |
| Selección | `selectedDisciplineId` | — | Estado local; filtra la lista de exámenes |

### 2.7 Exámenes de cinturón (`TkdBeltTestsSection` / `TkdBeltTestCard`)

| Se pinta | Modelo hoy (`BeltTest`) | Debe venir del API | Notas |
|----------|-------------------------|--------------------|-------|
| "TKD YOUTH" | `program` | `program.name` | |
| Fecha "28 OCT" | `date: LocalDate` | `scheduledOn` (ISO `yyyy-MM-dd`) | |
| Fecha resaltada | `isUpcomingSoon: Boolean` | — | La app lo deriva: `scheduledOn − asOf ≤ N días`. Que el umbral sea regla de producto, no del backend |
| Título | `title` | `title` | |
| "Green & Blue Belts" + punto de color | `beltLabel: String`, `beltDot: BeltDot` | `belts: [BeltCode]` (p. ej. `["GREEN","BLUE"]`) o `requirement` (`{type: STRIPES, count: 3}`) | La app compone la etiqueta y elige el color con `TkdBeltPalette`. **No** mandar colores ni textos armados |
| "12 Candidates" | `candidates: Int` | `candidates.total` | |
| "100% pre-approved" / "Mat review req." / "Kata assessment" | `candidatesNote: String` | `candidates.preApproved: Int` + `reviewStatus` (`PRE_APPROVED`, `MAT_REVIEW_REQUIRED`, `ASSESSMENT_PENDING`) | Texto libre en inglés → estado tipado + `strings.xml` |
| Tono de la tarjeta | `tone: BeltTestTone` | — | Derivado de `reviewStatus` / cercanía de fecha |
| Icono "Revisar" vs "Abrir" | `action: BeltTestAction` | — | Derivado: `Review` si `reviewStatus = PRE_APPROVED`, si no `Open` |
| "View All (4)" | `totalBeltTests: Int` | `beltTests.total` | La lista trae los próximos N; `total` cuenta todos los del trimestre |
| Subtítulo "Evaluations scheduled for this quarter" | fijo | `beltTests.window` (`from`, `to`) opcional | Si el periodo cambia, que el backend lo diga |

---

## 3. Endpoint principal (P0): `GET /dojo/overview`

- **Autenticación:** `bearerAuth`. El dojo sale del token; no va en la ruta. Si un instructor pudiera administrar varios dojos, usar `GET /dojos/{dojoId}/overview`.
- **Idioma:** `Accept-Language: es-MX`. Los textos libres (nombres de clase, programa, cita) vienen como los capturó el dojo; los textos de UI **no** vienen del API.
- **Zona horaria:** fechas y horas en ISO-8601 **con offset**, más `dojo.timeZone` (IANA) para que la app formatee en la hora del dojo.
- **"Hoy":** `asOf` lo manda el servidor y es el único "ahora" que usa la app (regla de `CLAUDE.md`: nunca `LocalDate.now()` en modelo o UI).
- **Caché:** `ETag` + `Cache-Control: private, max-age=30`. Con `If-None-Match` → `304`.

### 3.1 Respuesta de ejemplo (fixture `dashboard_overview.json`)

```json
{
  "asOf": "2026-10-27T17:38:00-07:00",
  "dojo": {
    "id": "dojo-1",
    "name": "Kensho Dojo",
    "logoUrl": "https://cdn.treecode.dev/dojos/dojo-1/logo.png",
    "timeZone": "America/Hermosillo",
    "disciplines": ["KARATE", "BJJ", "TAEKWONDO"]
  },
  "instructor": {
    "id": "u-1",
    "displayName": "Sensei Marcus",
    "photoUrl": "https://cdn.treecode.dev/users/u-1.jpg",
    "rank": { "belt": "BLACK", "degree": 4 }
  },
  "activeClass": {
    "id": "class-42",
    "name": "Karate infantil",
    "startsAt": "2026-10-27T17:30:00-07:00",
    "endsAt": "2026-10-27T18:00:00-07:00",
    "area": { "id": "mat-a", "name": "Tatami A" }
  },
  "liveSession": {
    "id": "session-981",
    "title": "Kata y kumite intermedio",
    "area": { "id": "floor-1", "name": "Piso 1" },
    "instructor": { "id": "u-1", "displayName": "Maestro Marcus" },
    "presentCount": 24,
    "capacity": 28,
    "attendeesPreview": [
      { "id": "st-1", "displayName": "Ana López", "photoUrl": "https://cdn.treecode.dev/students/st-1.jpg" },
      { "id": "st-2", "displayName": "Luis Pérez", "photoUrl": null },
      { "id": "st-3", "displayName": "Sofía Ruiz", "photoUrl": "https://cdn.treecode.dev/students/st-3.jpg" }
    ]
  },
  "metrics": {
    "period": { "from": "2026-10-01", "to": "2026-10-27" },
    "comparedTo": "PREVIOUS_MONTH",
    "activeStudents": { "value": 148, "delta": 12, "trend": [131, 134, 136, 140, 143, 146, 148] },
    "attendance": { "rate": 0.924, "delta": 0.031 },
    "readyForExam": 18
  },
  "beltTests": {
    "window": { "from": "2026-10-01", "to": "2026-12-31" },
    "total": 4,
    "items": [
      {
        "id": "tkd-youth-ceremony",
        "discipline": "TAEKWONDO",
        "program": { "id": "tkd-youth", "name": "TKD infantil" },
        "scheduledOn": "2026-10-28",
        "title": "Ceremonia de Taekwondo infantil",
        "belts": ["GREEN", "BLUE"],
        "requirement": null,
        "candidates": { "total": 12, "preApproved": 12 },
        "reviewStatus": "PRE_APPROVED"
      },
      {
        "id": "bjj-blue-belt",
        "discipline": "BJJ",
        "program": { "id": "bjj-adult", "name": "BJJ adultos" },
        "scheduledOn": "2026-11-04",
        "title": "Examen de grados y cinta azul",
        "belts": ["BLUE"],
        "requirement": { "type": "STRIPES", "count": 3 },
        "candidates": { "total": 6, "preApproved": 0 },
        "reviewStatus": "MAT_REVIEW_REQUIRED"
      }
    ]
  }
}
```

**Campos opcionales:** `activeClass` y `liveSession` pueden ser `null` (la tarjeta se oculta). `logoUrl`, `photoUrl` y `trend` también pueden ser `null`. Las listas vacías vienen como `[]`, nunca `null`.

### 3.2 Errores

Mismo esquema `Error` del contrato (`code`, `message` en es-MX, ADR-020).

| HTTP | Cuándo | Qué hace la app |
|------|--------|-----------------|
| `200` | OK | Pinta |
| `304` | `ETag` sin cambios | Conserva el estado |
| `401` | Token vencido | `Authenticator` renueva una vez; si falla, `TkdApp` vuelve a `login` |
| `403` | El usuario no administra el dojo | Pantalla de error con `message` |
| `404` | Dojo sin configurar (alta incompleta) | Estado vacío con llamada a completar el dojo (por definir con producto) |
| `5xx` / sin red | — | `Error(tkd_error_*)` + botón "Reintentar" |

---

## 4. Endpoints secundarios

### 4.1 `GET /dojo/live-session` (P1)

Mismo objeto que `overview.liveSession` más `asOf`. Responde `204` si no hay sesión en curso. Sirve para refrescar el contador de asistencia sin recargar la pantalla. Polling sugerido: cada 30 s mientras la pantalla esté en `STARTED` (`repeatOnLifecycle`). Si después se necesita tiempo real, evaluar SSE o WebSocket con su ADR.

### 4.2 `GET /belt-tests` (P1)

| Parámetro | Tipo | Uso |
|-----------|------|-----|
| `status` | `upcoming` \| `past` \| `all` | Pestaña "Ver todos" |
| `discipline` | `DisciplineCode` | Filtrado en servidor cuando la lista sea larga |
| `from`, `to` | `date` | Ventana (por defecto, el trimestre actual) |
| `cursor`, `limit` | `string`, `int` | Paginación por cursor |

Respuesta: `{ "items": [BeltTest], "total": Int, "nextCursor": String|null }`.

Mientras el resumen traiga ≤ 10 exámenes, el filtro por chip puede seguir siendo local (como hoy en `onDisciplineSelected`). Hay que aclarar en la UI que `total` cuenta **todas** las disciplinas.

### 4.3 `GET /search?q=` (P2)

Pendiente de definir con producto qué busca el campo del encabezado. Propuesta mínima: `{ "students": [StudentSummary], "classes": [ClassSummary] }`, con `q` de al menos 2 caracteres y *debounce* de 300 ms en el VM.

### 4.4 Endpoints a los que llevan las acciones (fuera de alcance)

| Acción | Destino | Endpoint esperado |
|--------|---------|-------------------|
| "+ Quick Add Student" | Alta de alumno | `POST /students` |
| Tocar la clase activa | Detalle de clase | `GET /classes/{id}` |
| "Open Mat Sheet" | Pase de lista | `GET /sessions/{id}/attendance` |
| "Ready for Exam" | Alumnos listos | `GET /students?readyForExam=true` |
| Candidatos de un examen | Lista de candidatos | `GET /belt-tests/{id}/candidates` |

---

## 5. Fragmento OpenAPI para `docs/api/kensho-api.yaml`

```yaml
tags:
  - name: dashboard
    description: Resumen del dojo (pantalla principal)

paths:
  /dojo/overview:
    get:
      tags: [dashboard]
      operationId: getDojoOverview
      summary: Datos de la pantalla principal del dojo de la sesión
      parameters:
        - in: header
          name: If-None-Match
          schema: { type: string }
      responses:
        '200':
          description: Resumen del dojo
          headers:
            ETag: { schema: { type: string } }
          content:
            application/json:
              schema: { $ref: '#/components/schemas/DojoOverview' }
        '304': { description: Sin cambios }
        '401': { $ref: '#/components/responses/Unauthorized' }
        '403':
          description: El usuario no administra el dojo
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Error' }

components:
  schemas:
    DojoOverview:
      type: object
      required: [asOf, dojo, instructor, metrics, beltTests]
      properties:
        asOf: { type: string, format: date-time }
        dojo: { $ref: '#/components/schemas/DojoSummary' }
        instructor: { $ref: '#/components/schemas/InstructorSummary' }
        activeClass: { oneOf: [{ $ref: '#/components/schemas/ActiveClass' }, { type: 'null' }] }
        liveSession: { oneOf: [{ $ref: '#/components/schemas/LiveSession' }, { type: 'null' }] }
        metrics: { $ref: '#/components/schemas/DojoMetrics' }
        beltTests: { $ref: '#/components/schemas/BeltTestPage' }

    DojoSummary:
      type: object
      required: [id, name, timeZone, disciplines]
      properties:
        id: { type: string }
        name: { type: string }
        logoUrl: { type: [string, 'null'], format: uri }
        timeZone: { type: string, description: Zona IANA, example: America/Hermosillo }
        disciplines: { type: array, items: { $ref: '#/components/schemas/DisciplineCode' } }

    InstructorSummary:
      type: object
      required: [id, displayName]
      properties:
        id: { type: string }
        displayName: { type: string }
        photoUrl: { type: [string, 'null'], format: uri }
        rank: { $ref: '#/components/schemas/Rank' }

    Rank:
      type: object
      required: [belt]
      properties:
        belt: { $ref: '#/components/schemas/BeltCode' }
        degree: { type: [integer, 'null'], minimum: 1, description: Dan / grado }

    BeltCode:
      type: string
      enum: [WHITE, YELLOW, ORANGE, GREEN, BLUE, PURPLE, BROWN, RED, BLACK]

    Area:
      type: object
      required: [id, name]
      properties:
        id: { type: string }
        name: { type: string }

    ActiveClass:
      type: object
      required: [id, name, startsAt, endsAt, area]
      properties:
        id: { type: string }
        name: { type: string }
        startsAt: { type: string, format: date-time }
        endsAt: { type: string, format: date-time }
        area: { $ref: '#/components/schemas/Area' }

    PersonPreview:
      type: object
      required: [id, displayName]
      properties:
        id: { type: string }
        displayName: { type: string }
        photoUrl: { type: [string, 'null'], format: uri }

    LiveSession:
      type: object
      required: [id, title, area, instructor, presentCount, capacity, attendeesPreview]
      properties:
        id: { type: string }
        title: { type: string }
        area: { $ref: '#/components/schemas/Area' }
        instructor: { $ref: '#/components/schemas/PersonPreview' }
        presentCount: { type: integer, minimum: 0 }
        capacity: { type: integer, minimum: 0 }
        attendeesPreview:
          type: array
          maxItems: 3
          items: { $ref: '#/components/schemas/PersonPreview' }

    DojoMetrics:
      type: object
      required: [period, comparedTo, activeStudents, attendance, readyForExam]
      properties:
        period:
          type: object
          required: [from, to]
          properties:
            from: { type: string, format: date }
            to: { type: string, format: date }
        comparedTo: { type: string, enum: [PREVIOUS_WEEK, PREVIOUS_MONTH, PREVIOUS_QUARTER] }
        activeStudents:
          type: object
          required: [value, delta]
          properties:
            value: { type: integer, minimum: 0 }
            delta: { type: integer, description: Puede ser negativo }
            trend: { type: [array, 'null'], items: { type: integer } }
        attendance:
          type: object
          required: [rate, delta]
          properties:
            rate: { type: number, minimum: 0, maximum: 1 }
            delta: { type: number, description: Diferencia en fracción; puede ser negativa }
        readyForExam: { type: integer, minimum: 0 }

    BeltTestPage:
      type: object
      required: [total, items]
      properties:
        window:
          type: object
          properties:
            from: { type: string, format: date }
            to: { type: string, format: date }
        total: { type: integer, minimum: 0 }
        items: { type: array, items: { $ref: '#/components/schemas/BeltTest' } }

    BeltTest:
      type: object
      required: [id, discipline, program, scheduledOn, title, belts, candidates, reviewStatus]
      properties:
        id: { type: string }
        discipline: { $ref: '#/components/schemas/DisciplineCode' }
        program:
          type: object
          required: [id, name]
          properties:
            id: { type: string }
            name: { type: string }
        scheduledOn: { type: string, format: date }
        title: { type: string }
        belts: { type: array, items: { $ref: '#/components/schemas/BeltCode' } }
        requirement:
          oneOf:
            - type: 'null'
            - type: object
              required: [type, count]
              properties:
                type: { type: string, enum: [STRIPES] }
                count: { type: integer, minimum: 1 }
        candidates:
          type: object
          required: [total, preApproved]
          properties:
            total: { type: integer, minimum: 0 }
            preApproved: { type: integer, minimum: 0 }
        reviewStatus: { type: string, enum: [PRE_APPROVED, MAT_REVIEW_REQUIRED, ASSESSMENT_PENDING] }
```

> Los enums tipados (`BeltCode`, `reviewStatus`, `comparedTo`) obligan a que el mapper tenga un valor `Unknown` como respaldo, para que un código nuevo del servidor no rompa la app.

---

## 6. Impacto en la app (TD-KEN-013)

| Pieza | Hoy | Cambio |
|-------|-----|--------|
| `TkdDashboardRepository` | 8 funciones síncronas | `suspend fun overview(): TkdResult<DojoOverview>` (+ `liveSession()` en P1) |
| Implementaciones | `FakeTkdDashboardRepository` | + `NetworkTkdDashboardRepository` (`withContext(@IoDispatcher)`, relanza `CancellationException`) y `@Binds` en `di/DataModule` |
| API / DTO | — | `data/remote/api/TkdDashboardApi.kt`, `data/remote/dto/TkdDashboardDto.kt`, `data/remote/mapper/TkdDashboardMapper.kt` |
| Modelo | `@DrawableRes` en `DojoHeader`/`LiveSession`; textos armados (`instructor`, `candidatesNote`, `principle`, `beltLabel`); `minutesLeft`, `isUpcomingSoon`, `tone`, `action`, `beltDot` fijos | `TkdImage` (URL); campos tipados (`Rank`, `reviewStatus`, `belts`); `asOf`; derivados como propiedades del modelo o del `UiState` |
| `TkdDashboardUiState` | Solo contenido | `Loading → Content | Error(@StringRes / TkdUiText)` + `onRetry()` (FF-ARC-12) |
| `TkdDashboardViewModel` | `= FakeTkdDashboardRepository()` | `@HiltViewModel @Inject constructor(repository)` y `hiltViewModel()` en el `Screen` (FF-15) |
| Textos | ~20 cadenas en inglés (`tkd_metric_*`, `tkd_belt_tests_*`, `tkd_live_session_*`) | Pasar a es-MX (TD-KEN-011) y agregar `tkd_belt_*`, `tkd_review_status_*`, `tkd_rank_*` |
| Deltas | `+%1$d` / `+%1$.1f%%` | Formato con signo que soporte negativos |
| Tests | — | `TkdDashboardMapperTest` (fixture `dashboard_overview.json`), `NetworkTkdDashboardRepositoryTest` (`MockWebServer`: 200, 304, 401, 5xx, nulos), `TkdDashboardViewModelTest` (carga, error, reintento, filtro) |

---

## 7. Preguntas abiertas para producto y backend

1. **Periodo de las métricas:** ¿contra qué se compara el delta? ¿Mes anterior, semana anterior?
2. **"Alumno activo":** ¿con mensualidad pagada, con asistencia en los últimos N días o simplemente no dado de baja?
3. **"Listo para examen":** ¿lo marca el instructor a mano o se calcula (asistencias + tiempo en el cinturón)?
4. **Clase activa vs. sesión en vivo:** el prototipo muestra las dos con datos distintos ("Youth Karate" en el tatami A y "Kata & Kumite" en el piso 1). ¿Puede haber dos clases simultáneas? ¿`activeClass` es la próxima o la del instructor?
5. **Umbral de "examen próximo":** ¿cuántos días antes se resalta la fecha?
6. **Frase del dojo:** ¿la captura cada dojo, es un catálogo global o rota por día?
7. **Buscador:** ¿qué busca?
8. **Multidojo:** ¿un instructor puede administrar más de un dojo? Esto define si la ruta es `/dojo/...` o `/dojos/{id}/...`.
9. **Disciplinas:** ¿el catálogo es `KARATE, BJJ, TAEKWONDO, MIXED` (contrato) o solo `KARATE, TAEKWONDO` (enum actual de la app)?

---

## 8. Checklist de entrega

- [ ] Resolver las preguntas de §7
- [ ] Agregar §5 a `docs/api/kensho-api.yaml` (tag `dashboard`)
- [ ] Publicar `GET /dojo/overview` en el mock de `dev` (Netlify) con el JSON de §3.1
- [ ] Fixture `app/src/test/resources/fixtures/dashboard_overview.json` igual al ejemplo del contrato
- [ ] DTO + mapper + `NetworkTkdDashboardRepository` + `@Binds`
- [ ] `UiState` con `Loading/Error`, VM con Hilt
- [ ] Textos a es-MX y deltas con signo
- [ ] Tests de mapper, repositorio (MockWebServer) y VM
- [ ] Fragmento `changelog.d/TD-KEN-013.added.md`
