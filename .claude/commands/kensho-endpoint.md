# Implementar un endpoint del API en Kensho

Eres el desarrollador Android de **Kensho**. Recibes **el path de un endpoint, el JSON de request (opcional) y el JSON de response** y lo implementas de punta a punta en la capa de datos siguiendo ADR-016/017, Clean Code y la forma del piloto `auth`. Las reglas viven en `CLAUDE.md` (§Datos reales, §Convenciones) y `docs/architecture/03-standards.md` (ARC-10…14, TEST-06, LOG-03); esta skill las **orquesta**, no las copia.

> Referencia viva (léela antes de escribir): `data/remote/api/TkdAuthApi.kt`, `data/remote/dto/TkdAuthDto.kt`, `data/remote/mapper/TkdAuthMapper.kt`, `data/NetworkTkdAuthRepository.kt`, `data/remote/TkdApiCall.kt`, `di/NetworkModule.kt`, `di/DataModule.kt`, `app/src/test/java/dev/treecode/kensho/data/NetworkTkdAuthRepositoryTest.kt`, `TkdDataTestSupport.kt`. Plantillas: `/kensho-endpoint:plantillas`.

## Entrada

`$ARGUMENTS` trae, en texto libre:

| Dato | Obligatorio | Ejemplo |
|------|-------------|---------|
| Método + path | Sí | `GET /dojo/overview`, `POST /students/{studentId}/payments` |
| JSON de request | Solo POST/PUT/PATCH | `{ "amountCents": 42000, "method": "CASH" }` |
| JSON de response | Sí (o "sin cuerpo" / 204) | `{ "id": "tx-1", "status": "PAID", "paidAt": "2026-11-05T10:00:00Z" }` |
| Feature / repositorio destino | No: se deduce del path | `payments` → `TkdPaymentsRepository` |
| Query params, auth | No | `?status=upcoming`; por defecto **con** token |

Si falta el método, asume `GET` sin request y `POST` con request. **Solo pregunta** si no puedes deducir el feature/repositorio, o si el JSON es ambiguo de forma que cambia el modelo (p. ej. un campo `null` en el ejemplo sin saber su tipo: pregunta o tipa como `String?` y dilo en el reporte).

## Flujo

```
1. Analizar   path → feature, operationId, repositorio · JSON → tipos · campos de dominio vs. visuales
2. Contrato   docs/api/kensho-api.yaml: path + schemas (fuente de verdad de las fixtures)
3. Fixtures   app/src/test/resources/fixtures/<feature>_<operacion>.json (response, y error si aplica)
4. DTO        data/remote/dto/Tkd<Feature>Dto.kt          @Serializable, espejo 1:1 del JSON
5. Api        data/remote/api/Tkd<Feature>Api.kt          interfaz Retrofit suspend
6. Mapper     data/remote/mapper/Tkd<Feature>Mapper.kt    Dto ↔ model, internal, funciones puras
7. Modelo     model/ (o data/ si es petición)             @Immutable, tipos de dominio
8. Repositorio interface (suspend + TkdResult) · NetworkTkd<Feature>Repository · Fake actualizada
9. DI         NetworkModule @Provides Api · DataModule @Binds Network
10. ViewModel  solo si el feature ya consume el repositorio: Loading/Content/Error + onRetry
11. Tests      mapper (fixture) · repositorio (MockWebServer) · VM (Fake) si cambió
12. Cierre     changelog.d · build + tests + detekt · reporte
```

### 1. Analizar la entrada

- **Feature**: primer segmento del path (`/payments/...` → `payments`). Busca el repositorio existente (`grep -l "interface Tkd" app/src/main/java/dev/treecode/kensho/data/`). Si existe, **agrega el método a esa interfaz**; no crees un repositorio paralelo.
- **operationId** en camelCase a partir de verbo + recurso: `GET /dojo/overview` → `dojoOverview`, `POST /payments/{id}/confirm` → `confirmPayment`. Es el nombre del método en la Api y en el repositorio.
- **Tipado del JSON** (en el DTO, siempre lo que manda el cable):

| JSON | DTO | Modelo de dominio |
|------|-----|-------------------|
| `"2026-11-05"` | `String` | `LocalDate` |
| `"2026-11-05T10:00:00Z"` / con offset | `String` | `Instant` / `OffsetDateTime` / `LocalDateTime` según uso |
| `"10:30"` | `String` | `LocalTime` |
| `"PAID"`, `"KARATE"` (códigos) | `String` | `enum class` + mapeo `when` con rama desconocida explícita |
| dinero (`amountCents`, `1234.50`) | `Int` / `Long` / `Double` | **centavos `Int`** (convertir si viene en decimal) |
| URL de imagen | `String?` | `TkdImage?` con `toTkdImage()` (`data/remote/mapper/TkdImageMapper.kt`); la UI la pinta con `TkdPhoto` |
| campo que puede faltar o ser `null` | `T? = null` | `T?` o default de dominio justificado |
| lista | `List<XDto> = emptyList()` | `List<X>` |
| objeto anidado | `data class` propia | modelo propio |

- **Dominio vs. visual**: colores, iconos, textos fijos y formatos los decide la app; si el JSON los trae, el mapper los convierte a enums de dominio, nunca a `Color`/`@DrawableRes`.

### 2. Contrato OpenAPI

Agrega el path y los `components/schemas` a `docs/api/kensho-api.yaml` (mismo estilo que `auth`: `tags`, `operationId`, `requestBody`, respuestas `2xx` + `$ref` a `Unauthorized`/`NotFound`/`Conflict` cuando apliquen; `security: []` solo si es público). Si el tag no existe, créalo en `tags:`. Los ejemplos del usuario van como `example`.

### 3–7. Código de datos

Usa `/kensho-endpoint:plantillas`. Reglas que **no** se negocian:

- DTO y Api **solo** en `data/remote/` (FF-14); nunca salen de `data/`: la interfaz del repositorio expone modelos de dominio.
- Nombres de propiedad del DTO = claves del JSON; si la clave no es camelCase válida, `@SerialName("snake_key")`.
- Campos opcionales con default (`= null`, `= emptyList()`): el `Json` del proyecto tiene `ignoreUnknownKeys` y `explicitNulls = false`.
- Endpoint público → `@Headers(NO_AUTH_HEADER)`; el resto lleva token por `TkdAuthInterceptor`, no agregues `Authorization` a mano.
- Path params con `@Path`, query con `@Query`; respuesta sin cuerpo → `suspend fun x(...): Response<Unit>` y valida `isSuccessful` en el repositorio, o `Unit` si el server responde 204 siempre.
- Mapper: funciones de extensión `internal` puras, una por tipo (`XDto.toModel()`, `Request.toDto()`), sin lógica de UI ni `LocalDate.now()`. Un código desconocido → valor `Unknown` del enum o `TkdDataError.Unknown`, nunca `!!` ni `valueOf` sin control.
- Repositorio de red: `@Singleton class NetworkTkd<Feature>Repository @Inject constructor(api, @param:IoDispatcher io)`; cada método es **una línea** `tkdApiCall(io) { api.x(...).toModel() }`. Nada de `try/catch` propio, `Dispatchers.IO` ni logs (ya los hace `tkdApiCall`).
- La *Fake* existente implementa el método nuevo con los datos del prototipo y `failure` para tests (ver `FakeTkdSenseiProfileRepository`).
- Si el repositorio era síncrono, pasa los métodos tocados a `suspend` + `TkdResult` y migra sus llamadores (VM con `viewModelScope.launch`). Si eso arrastra demasiado, detente y propón una historia (`/tech-story-architect`).

### 8–9. Inyección

- `di/NetworkModule.kt`: `@Provides @Singleton fun provide<Feature>Api(retrofit: Retrofit): Tkd<Feature>Api = retrofit.create(Tkd<Feature>Api::class.java)` (una sola por Api).
- `di/DataModule.kt`: `@Binds` cambia de `FakeTkd…` a `NetworkTkd…`, o se agrega si no estaba. Quita el comentario "Sin endpoint… todavía".

### 10. ViewModel (solo si el feature ya usa el repositorio)

- `@HiltViewModel @Inject constructor(private val repository: Tkd<Feature>Repository)`, sin `= Fake…()` (FF-15). Si el VM tenía default a la *Fake*, quítalo y el `Screen` pasa a `hiltViewModel()`.
- `UiState` con carga y error: `isLoading`, `loadError: TkdUiText?` (patrón de `TkdSenseiProfileUiState`). Error con `failure.toUiText()` (ADR-020), nunca `exception.message`. `onRetry()`.
- Si la pantalla no existe aún, **no la crees**: deja el repositorio listo y dilo en el reporte.

### 11. Tests (TEST-06, nombres `sujeto_condicion_resultado`)

- `app/src/test/.../data/remote/mapper/Tkd<Feature>MapperTest.kt`: decodifica la fixture con `testJson` y compara contra el modelo esperado; un caso por código de enum y por campo opcional ausente.
- `app/src/test/.../data/NetworkTkd<Feature>RepositoryTest.kt` con `MockWebServer` + `testRetrofit()` + `jsonResponse()`: **2xx, 401, 404, 409, 5xx, timeout y JSON malformado** → `TkdDataError` esperado; verifica `request.path`, método y cuerpo enviado; `serverMessage` cuando el error trae `{code, message}`.
- VM tocado: test con la *Fake* (`failure`) y `StandardTestDispatcher`: carga ok, error → `loadError`, `onRetry`.

### 12. Cierre

1. `changelog.d/<KEN-n|+slug>.added.md` (o `changed`): una línea en español, p. ej. "Se consume `GET /dojo/overview` para el resumen del dojo."
2. Si es `GET /dojo/overview` (el resumen queda en red), salda TD-KEN-013 siguiendo `/kensho-governance:propagacion` (no copies cifras aquí).
3. Verifica: `./gradlew :app:assembleDebug :app:testDebugUnitTest detekt`. Arregla lo nuevo; un fallo previo se reporta, no se esconde.
4. **No hagas commit** salvo que se pida.

## Reporte final (en español)

```
Endpoint: <MÉTODO> <path> → Tkd<Feature>Api.<operationId>
Archivos: creados … / modificados …
Modelo: <tipos de dominio y decisiones de mapeo (fechas, enums, dinero, nulos)>
DI: <Network o Fake en DataModule>
Tests: <n mapper, n repositorio, n VM> — resultado
Pendientes: <supuestos sobre el JSON, pantalla aún sin conectar, deuda tocada>
```
