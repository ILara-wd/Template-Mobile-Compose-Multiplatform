# API para la lista de alumnos

| Dato | Valor |
|------|-------|
| Pantalla | `students/TkdStudentsScreen.kt` (pestaña **Alumnos**) · tarjeta `components/molecules/TkdStudentCard.kt` |
| Estado actual | `FakeTkdStudentsRepository` (prototipo, nodo 1:326), repositorio síncrono, VM con default a la *Fake*, fotos `@DrawableRes` |
| Contrato existente | `docs/api/kensho-api.yaml` (`auth`, `me`) |
| Fecha | 2026-11-05 |
| Objetivo | Definir el endpoint que alimenta las tarjetas y el filtro de cintas, para construirlo en el backend y consumirlo con `/kensho-endpoint` |

---

## 1. Resumen y decisión

| # | Endpoint | Para qué | Prioridad |
|---|----------|----------|-----------|
| 1 | `GET /students` | Lista completa de alumnos activos del dojo + cintas del filtro | **P0** |

**Decisión clave:** el buscador, el filtro por cinta y el orden por "Próximo examen" **siguen siendo locales**. Por eso el endpoint devuelve **la lista completa** del dojo en una sola llamada (sin paginación) y la app filtra en memoria, como hoy en `TkdStudentsViewModel.filter()`.

- Un dojo típico tiene decenas o pocos cientos de alumnos: una respuesta de ~150 alumnos pesa < 60 KB sin comprimir (~10 KB con gzip).
- Sin paginación, la búsqueda y el filtro responden al instante y funcionan aunque se caiga la red después de cargar.
- **Umbral de revisión:** si un dojo supera **~500 alumnos**, pasar a paginación por cursor con búsqueda en el servidor (v2). El cuerpo ya envuelve la lista en `items` para poder agregar `nextCursor` sin romper el contrato.

**Regla (igual que `dashboard-api.md`):** el API manda **hechos de dominio** (códigos, números, fechas). Los textos de UI, los colores de las cintas y los formatos los decide la app.

---

## 2. Mapa tarjeta → datos

### 2.1 Tarjeta del alumno (`TkdStudentCard`)

| Se pinta | Modelo hoy (`Student`) | Debe venir del API | Notas |
|----------|------------------------|--------------------|-------|
| Foto o iniciales | `photo: @DrawableRes Int?`, `initials` (derivado) | `photoUrl: string \| null` | `null` → iniciales (ya las calcula el modelo). URL → modelo de imagen (TD-KEN-012) |
| Nombre | `name` | `displayName` | Lo usa el buscador |
| "14 años" | `age: Int` | `age: int` | **Edad calculada por el servidor**, no `birthDate`: muchos alumnos son menores y la lista no necesita su fecha de nacimiento (minimización de datos) |
| Punto de color del avatar y badge | `belt: BeltColor` | `rank.belt` (`BeltCode`) | El color sale de `TkdBeltPalette` en la app |
| "Green Belt • 6th Kup" / "BJJ Blue Belt" | `beltLabel: String` (texto armado, en inglés) | `rank.discipline`, `rank.belt`, `rank.grade` (`{system, value}` o `null`) | La app arma la etiqueta con `strings.xml` ("Cinta verde • 6.º kup"). El buscador busca sobre esa etiqueta ya armada |
| Rayas del badge | `stripes: Int` | `rank.stripes: int` (≥ 0) | |
| Badge de estado | `status: StudentStatus` | `examStatus.code` (+ `examStatus.classesRemaining`) | Estado tipado; el texto es de la app (`tkd_students_status_*`) |
| "Asistencia en el tatami 22 /24 clases (92%)" | `attended`, `totalClasses`; `attendance` e `isAttendanceComplete` derivados | `attendance.attended`, `attendance.required` | El porcentaje y "completo" se derivan en la app; **no** mandarlos |
| Orden por "Próximo examen" | `nextExam: LocalDate` | `nextExamOn: date \| null` | `null` = sin examen programado; la app lo ordena al final |
| Navegación al perfil | `id` | `id` | Ruta `student/{studentId}` en `TkdApp` |

### 2.2 Filtro de cintas (`TkdSwatchChip` en `TkdStudentsFilterBar`)

| Se pinta | Modelo hoy | Debe venir del API | Notas |
|----------|------------|--------------------|-------|
| Chips de color en orden | `studentFilterBelts` (lista fija en `TkdStudentsControls.kt`) | `beltFilters: [BeltCode]` **ordenado** | El orden de progresión cambia por escuela y disciplina (naranja antes o después de amarilla). Incluye cintas del temario aunque hoy no tengan alumnos |
| Nombre y color del chip | `belt.labelRes`, `belt.palette()` | — | Siguen en la app |
| Selección y filtrado | `selectedBelt`, `filter()` | — | **Local.** Tocar la misma cinta la deselecciona |
| Fila oculta | — | `beltFilters: []` | Sin cintas configuradas, la app no muestra la fila |

### 2.3 Encabezado de la lista y buscador

| Se pinta | Modelo hoy | Debe venir del API | Notas |
|----------|------------|--------------------|-------|
| "Mostrando 148 artistas marciales activos" | `totalCount` (del repositorio) | — | **Derivado:** `items.size`. Con la lista completa no hace falta un contador aparte que se pueda desincronizar |
| Conteo "Listo para el examen" (chip oculto hoy) | `readyCount` | — | Derivado: alumnos con `examStatus` `READY` o `READY_FOR_BLACK_TIP` |
| Buscador | `query` | — | **Local**, sobre `displayName` y la etiqueta de la cinta |

---

## 3. Endpoint `GET /students`

- **Autenticación:** `bearerAuth` (`Authorization: Bearer <accessToken>`). El dojo sale del token, no de la ruta. Si un instructor llegara a administrar varios dojos: `GET /dojos/{dojoId}/students`.
- **Ruta:** relativa a `BASE_URL` (`…/api/v1/`), igual que `me`. En Retrofit: `@GET("students")`.
- **Alcance:** solo alumnos **activos** del dojo. Bajas y suspendidos quedan fuera (si se necesitan, `?status=INACTIVE` en una versión posterior).
- **Orden de `items`:** por `displayName` (orden alfabético con collation es-MX). La app no depende de este orden para filtrar, pero da una lista estable.
- **Idioma:** `Accept-Language: es-MX`. Solo `displayName` es texto libre; nada de etiquetas de UI.
- **"Hoy":** `asOf` (ISO-8601 con offset) es el "ahora" con el que el servidor calculó `age`, `attendance` y `examStatus`. La app no usa `LocalDate.now()`.
- **Caché:** `ETag` + `Cache-Control: private, max-age=60`; con `If-None-Match` → `304 Not Modified`. Respuesta con `Content-Encoding: gzip`.
- **Fotos:** `photoUrl` puede ser una URL firmada con expiración. Debe durar más que `max-age` y la app la trata como opaca.

### 3.1 Respuesta de ejemplo (fixture `students_list.json`)

```json
{
  "asOf": "2026-11-05T17:30:00-07:00",
  "beltFilters": ["WHITE", "ORANGE", "YELLOW", "GREEN", "BLUE", "PURPLE", "BROWN", "RED", "BLACK"],
  "items": [
    {
      "id": "st-kenji-sato",
      "displayName": "Kenji Sato",
      "age": 14,
      "photoUrl": "https://cdn.treecode.dev/students/st-kenji-sato.jpg",
      "rank": {
        "discipline": "TAEKWONDO",
        "belt": "GREEN",
        "grade": { "system": "KUP", "value": 6 },
        "stripes": 2
      },
      "examStatus": { "code": "READY" },
      "attendance": { "attended": 22, "required": 24 },
      "nextExamOn": "2026-11-28"
    },
    {
      "id": "st-alex-chen",
      "displayName": "Alex Chen",
      "age": 28,
      "photoUrl": null,
      "rank": { "discipline": "BJJ", "belt": "BLUE", "grade": null, "stripes": 2 },
      "examStatus": { "code": "SPARRING_EVAL_PENDING" },
      "attendance": { "attended": 48, "required": 60 },
      "nextExamOn": "2026-12-04"
    },
    {
      "id": "st-maya-rodriguez",
      "displayName": "Maya Rodríguez",
      "age": 10,
      "photoUrl": "https://cdn.treecode.dev/students/st-maya-rodriguez.jpg",
      "rank": {
        "discipline": "TAEKWONDO",
        "belt": "YELLOW",
        "grade": { "system": "KUP", "value": 8 },
        "stripes": 0
      },
      "examStatus": { "code": "CLASSES_REMAINING", "classesRemaining": 2 },
      "attendance": { "attended": 14, "required": 16 },
      "nextExamOn": null
    }
  ]
}
```

### 3.2 Campos

| Campo | Tipo | Obligatorio | Regla |
|-------|------|-------------|-------|
| `asOf` | string `date-time` con offset | Sí | Momento del cálculo |
| `beltFilters` | `BeltCode[]` | Sí (puede ser `[]`) | Ordenado por progresión del temario del dojo; sin repetidos |
| `items` | `StudentSummary[]` | Sí (puede ser `[]`) | Solo activos |
| `items[].id` | string | Sí | Estable; es el `studentId` de `GET /students/{id}` (perfil, futuro) |
| `items[].displayName` | string | Sí | No vacío |
| `items[].age` | int | Sí | Años cumplidos a `asOf` |
| `items[].photoUrl` | string `uri` \| null | Sí | `null` → iniciales |
| `items[].rank.discipline` | `DisciplineCode` | Sí | Reutiliza el enum del contrato (`KARATE`, `BJJ`, `TAEKWONDO`, `MIXED`) |
| `items[].rank.belt` | `BeltCode` | Sí | Debe estar en `beltFilters` |
| `items[].rank.grade` | `{system: GradeSystem, value: int}` \| null | Sí | `null` cuando la cinta no tiene grado (blanca, BJJ) |
| `items[].rank.stripes` | int ≥ 0 | Sí | |
| `items[].examStatus.code` | `ExamStatusCode` | Sí | Ver §3.3 |
| `items[].examStatus.classesRemaining` | int ≥ 1 | Solo con `CLASSES_REMAINING` | |
| `items[].attendance.attended` | int ≥ 0 | Sí | Clases asistidas en el ciclo de examen actual |
| `items[].attendance.required` | int ≥ 0 | Sí | Clases requeridas del ciclo; `0` = sin requisito (la app muestra 0 %) |
| `items[].nextExamOn` | string `date` \| null | Sí | `yyyy-MM-dd` |

### 3.3 Catálogos

| Enum | Valores | Mapeo en la app |
|------|---------|-----------------|
| `BeltCode` | `WHITE`, `ORANGE`, `YELLOW`, `GREEN`, `BLUE`, `PURPLE`, `BROWN`, `RED`, `BLACK` | `BeltColor` (mismos 9 valores) |
| `GradeSystem` | `KUP`, `DAN`, `KYU` | Plantilla de `strings.xml` por sistema ("6.º kup", "1.er dan") |
| `ExamStatusCode` | `READY`, `CLASSES_REMAINING`, `SPARRING_EVAL_PENDING`, `READY_FOR_BLACK_TIP` | `StudentStatus.ReadyForExam`, `ClassesToTest(n)`, `SparringEvalPending`, `ReadyForBlackTip` |

**Compatibilidad hacia adelante:** agregar un valor a un enum **no** es un cambio que rompa el contrato. La app ignora códigos desconocidos sin fallar toda la lista:
- `beltFilters`: se descarta el chip.
- `rank.belt` desconocido: el alumno se muestra con la paleta neutra y sin entrar en ningún filtro de cinta.
- `examStatus.code` desconocido: se oculta el badge de estado.

### 3.4 Respuestas de error

Cuerpo `Error` (`{code, message}`) del contrato; `message` en español y listo para el usuario (ADR-020).

| HTTP | Cuándo | En la app (`TkdDataError`) |
|------|--------|----------------------------|
| `200` | Lista (puede venir vacía) | Contenido o estado vacío |
| `304` | `If-None-Match` coincide | Se reutiliza la caché de OkHttp |
| `401` | Token vencido o inválido | `Authenticator` renueva; si no puede, vuelve al login |
| `403` | El usuario no tiene permiso sobre los alumnos del dojo | Hoy `Unknown` con el `message` del API (ver §6) |
| `5xx` | Fallo del servidor | `Server` → error con "Reintentar" |

---

## 4. Contrato OpenAPI (para pegar en `kensho-api.yaml` cuando el backend lo publique)

```yaml
tags:
  - name: students
    description: Alumnos del dojo de la sesión

paths:
  /students:
    get:
      tags: [students]
      operationId: listStudents
      summary: Alumnos activos del dojo y cintas del filtro
      description: >-
        Lista completa (sin paginación): la app busca, filtra por cinta y ordena en local.
        Revisar paginación por cursor si un dojo supera ~500 alumnos.
      parameters:
        - in: header
          name: If-None-Match
          required: false
          schema: { type: string }
      responses:
        '200':
          description: Alumnos activos
          headers:
            ETag: { schema: { type: string } }
          content:
            application/json:
              schema: { $ref: '#/components/schemas/StudentList' }
        '304': { description: Sin cambios desde el ETag enviado }
        '401': { $ref: '#/components/responses/Unauthorized' }
        '403':
          description: Sin permiso sobre los alumnos del dojo
          content:
            application/json:
              schema: { $ref: '#/components/schemas/Error' }

components:
  schemas:
    StudentList:
      type: object
      required: [asOf, beltFilters, items]
      properties:
        asOf: { type: string, format: date-time }
        beltFilters:
          type: array
          uniqueItems: true
          items: { $ref: '#/components/schemas/BeltCode' }
        items:
          type: array
          items: { $ref: '#/components/schemas/StudentSummary' }
    StudentSummary:
      type: object
      required: [id, displayName, age, photoUrl, rank, examStatus, attendance, nextExamOn]
      properties:
        id: { type: string }
        displayName: { type: string, minLength: 1 }
        age: { type: integer, minimum: 0 }
        photoUrl: { type: [string, 'null'], format: uri }
        rank: { $ref: '#/components/schemas/StudentRank' }
        examStatus: { $ref: '#/components/schemas/ExamStatus' }
        attendance:
          type: object
          required: [attended, required]
          properties:
            attended: { type: integer, minimum: 0 }
            required: { type: integer, minimum: 0 }
        nextExamOn: { type: [string, 'null'], format: date }
    StudentRank:
      type: object
      required: [discipline, belt, grade, stripes]
      properties:
        discipline: { $ref: '#/components/schemas/DisciplineCode' }
        belt: { $ref: '#/components/schemas/BeltCode' }
        grade:
          oneOf:
            - type: 'null'
            - type: object
              required: [system, value]
              properties:
                system: { type: string, enum: [KUP, DAN, KYU] }
                value: { type: integer, minimum: 1 }
        stripes: { type: integer, minimum: 0 }
    ExamStatus:
      type: object
      required: [code]
      properties:
        code: { type: string, enum: [READY, CLASSES_REMAINING, SPARRING_EVAL_PENDING, READY_FOR_BLACK_TIP] }
        classesRemaining: { type: integer, minimum: 1, description: Solo con CLASSES_REMAINING }
    BeltCode:
      type: string
      enum: [WHITE, ORANGE, YELLOW, GREEN, BLUE, PURPLE, BROWN, RED, BLACK]
```

---

## 5. Arquitectura en la app (ADR-016/017)

Se construye con `/kensho-endpoint GET /students` y esta respuesta. Capas y archivos:

```
TkdStudentsScreen ── hiltViewModel() ──▶ TkdStudentsViewModel (@HiltViewModel)
                                          │  estado: Loading → Content | Error(TkdUiText) · onRetry()
                                          │  query / selectedBelt / sortByNextExam / readyOnly → filter() local
                                          ▼
                              TkdStudentsRepository (interface, suspend + TkdResult)
                                 ├─ NetworkTkdStudentsRepository ── tkdApiCall(io) { api.listStudents().toRoster() }
                                 └─ FakeTkdStudentsRepository (prototipo, tests y previews)
                                          ▼
         data/remote/api/TkdStudentsApi.kt · dto/TkdStudentsDto.kt · mapper/TkdStudentsMapper.kt
```

| Capa | Cambio |
|------|--------|
| `data/remote/dto/TkdStudentsDto.kt` | `StudentListDto`, `StudentSummaryDto`, `StudentRankDto`, `GradeDto`, `ExamStatusDto`, `AttendanceDto`: espejo 1:1 del JSON. Códigos como `String`; `photoUrl`, `grade`, `nextExamOn`, `classesRemaining` con `= null` |
| `data/remote/api/TkdStudentsApi.kt` | `@GET("students") suspend fun listStudents(): StudentListDto` (con token por `TkdAuthInterceptor`) |
| `data/remote/mapper/TkdStudentsMapper.kt` | `toRoster()`; `String.toBeltColor(): BeltColor?`; `ExamStatusDto.toStatus(): StudentStatus?`; `nextExamOn` → `LocalDate?`; `beltFilters` → `List<BeltColor>` sin desconocidos |
| `model/` | `TkdStudentRoster(asOf, beltFilters, students)`. En `Student`: `photo` pasa a URL (modelo de imagen, TD-KEN-012); `beltLabel: String` se sustituye por `rank` tipado (`discipline`, `belt: BeltColor?`, `grade`, `stripes`); `status: StudentStatus?`; `nextExam: LocalDate?` |
| `data/TkdStudentsRepository.kt` | `suspend fun roster(): TkdResult<TkdStudentRoster>`; se quitan `activeCount()`/`readyCount()` (los conteos se derivan) |
| `di/` | `NetworkModule`: `provideStudentsApi`. `DataModule`: `@Binds NetworkTkdStudentsRepository` |
| `students/TkdStudentsViewModel.kt` | `@HiltViewModel`, sin defaults a *Fakes*; carga con `viewModelScope`; `isLoading`/`loadError`/`onRetry`; `filter()` igual; `totalCount`/`readyCount` como propiedades derivadas del `UiState` |
| `students/TkdStudentsControls.kt` | La fila de chips recorre `state.beltFilters` en vez de `studentFilterBelts`; se oculta si está vacía |
| UI | Etiqueta de cinta armada con `strings.xml` (nuevas plantillas por `GradeSystem`); el buscador compara contra la etiqueta ya resuelta, que se calcula en la UI y se pasa al filtro o se precalcula con `Resources` en el `Screen` |
| Encabezado | Puede usar `GET /me` como el resumen del dojo (`TkdDashboardViewModel`) |

**Dependencias que se arrastran (revisar antes de empezar):**
- `TkdStudentProfileRepository` construye el perfil a partir de `FakeTkdStudentsRepository`. Mientras no exista `GET /students/{id}`, debe seguir con su *Fake* propia y **no** depender de la lista de red.
- `TkdStudentCard` y sus previews usan `Student.beltLabel` y `photo` como `@DrawableRes`: se actualizan junto con el modelo.
- Las fotos por URL necesitan Coil (aprobado en ADR-017) y un modelo de imagen en `model/`. Si aún no existe, va en su propia historia (TD-KEN-012) y este endpoint puede llegar primero mostrando iniciales.

**Tests (TEST-06):**
- Mapper con fixtures: `students_list.json`, una con códigos desconocidos (`"belt": "GOLD"`, `"code": "NEW_STATUS"`) y una lista vacía.
- `NetworkTkdStudentsRepositoryTest` con `MockWebServer`: 200 (path `/students`, `Bearer`), 401, 403, 404, 5xx, timeout y JSON malformado.
- `TkdStudentsViewModelTest` con la *Fake*: carga, error y reintento; filtro por cinta, búsqueda y orden con `nextExamOn` nulo al final; conteos derivados.
- Compose UI test: tocar una cinta filtra; `beltFilters` vacío oculta la fila.

---

## 6. Decisiones abiertas

| # | Pregunta | Recomendación |
|---|----------|---------------|
| 1 | ¿Un instructor administra más de un dojo? | Si sí, `GET /dojos/{dojoId}/students` desde el inicio |
| 2 | ¿`attendance` es del ciclo de examen o de un periodo fijo (mes)? | Ciclo de examen: es lo que compara contra `required`. Documentarlo en el contrato |
| 3 | ¿Quién calcula `examStatus`? | El servidor: combina asistencia, evaluaciones y reglas de la escuela que la app no conoce |
| 4 | `403` hoy cae en `TkdDataError.Unknown` | Agregar `Forbidden` a `TkdDataError` cuando el primer endpoint lo use (cambio en `TkdApiCall`) |
| 5 | Disciplinas: el contrato tiene 4 y `DojoDiscipline` 2 | Mismo pendiente que en `/me`: decidir si la app vuelve a ofrecer `BJJ` y `MIXED` |

## 7. Orden sugerido

1. Backend publica `GET /students` según §3–4 (y el `Error` común).
2. Copiar §4 a `docs/api/kensho-api.yaml` y la respuesta de §3.1 a `app/src/test/resources/fixtures/students_list.json`.
3. App: `/kensho-endpoint GET /students` con este documento como entrada (capa de datos + tests).
4. App: migrar `TkdStudentsViewModel` y la UI (cintas desde `beltFilters`, etiqueta tipada, carga/error).
5. Fotos por URL (modelo de imagen + Coil) en una historia aparte si todavía no existe.
