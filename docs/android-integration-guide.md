# Guía de integración Android: del login al alta de alumnos

Cómo consumir desde la app Android los endpoints de autenticación, configuración del dojo y alta de alumnos,
y cómo se validan. Es el mismo flujo que siguen la carpeta **"Configuración del dojo"** de la colección de
Postman y la pantalla **"Mi dojo"** del panel web.

Contratos completos:

- [dojo-config-api.md](dojo-config-api.md): programas, escaleras, horarios y plan.
- [student-registration-api.md](student-registration-api.md): ficha de alta y `POST /students`.

Índice:

1. [El flujo](#1-el-flujo)
2. [Autenticación](#2-autenticación)
3. [¿El dojo ya puede dar de alta alumnos?](#3-el-dojo-ya-puede-dar-de-alta-alumnos)
4. [Crear el programa con su escalera](#4-crear-el-programa-con-su-escalera)
5. [Crear un horario](#5-crear-un-horario)
6. [Plan de cobro](#6-plan-de-cobro-opcional)
7. [Alta del alumno](#7-alta-del-alumno)
8. [Validaciones](#8-validaciones)
9. [Cómo encaja en la arquitectura de la app](#9-cómo-encaja-en-la-arquitectura-de-la-app)
10. [Cómo probarlo](#10-cómo-probarlo)
11. [Probar contra el servidor local desde la red](#11-probar-contra-el-servidor-local-desde-la-red)

---

## 1. El flujo

```
login / register ──► GET dojo/config ──► ¿hay programa con rangos Y al menos un horario?
                                              │ no                              │ sí
                                              ▼                                 ▼
                              PUT dojo/programs/{discipline}        GET dojo/registration-setup
                              PUT dojo/schedules/{id}                       │ (folio)
                              PUT dojo/settings (opcional)                  ▼
                                              │                    POST students
                                              └──────► (vuelve a evaluar) ──┘   (Idempotency-Key = folio)
```

`POST /auth/register` solo crea la cuenta. Un dojo nuevo no puede dar de alta alumnos hasta tener al menos un
programa con escalera de rangos y un horario.

Base URL: `https://<tu-sitio>/api/v1` (en local, `http://localhost:8888/api/v1`).

---

## 2. Autenticación

### Login y registro

| Endpoint | Body |
|---|---|
| `POST /auth/login` | `{ "email", "password" }` |
| `POST /auth/register` | `{ "instructorName", "dojoName", "discipline", "email", "password", "acceptedTerms": true }` |

`discipline` en el registro: `KARATE`, `BJJ`, `TAEKWONDO` o `MIXED`. Es la disciplina principal del dojo.

Ambos devuelven el mismo objeto `Session` (login: 200; registro: 201):

```json
{
  "accessToken": "eyJ…",
  "refreshToken": "…",
  "expiresIn": 3600,
  "user": { "id": "…", "email": "…", "instructorName": "…" }
}
```

| Token | Dura | Dónde guardarlo |
|---|---|---|
| `accessToken` | 1 hora | En memoria y en almacenamiento cifrado |
| `refreshToken` | 30 días | En almacenamiento cifrado (EncryptedSharedPreferences o DataStore + Tink) |

### Reglas

1. **Cada request protegido lleva** `Authorization: Bearer <accessToken>`.
2. **Un token vencido o inválido siempre da 401, nunca 403.** Ese 401 es la señal para renovar.
3. **`POST /auth/refresh` con `{ "refreshToken" }` rota el token.** Devuelve una `Session` nueva con otro
   `refreshToken`, y el anterior queda revocado. Por eso:
    - guarda siempre el refresh token nuevo;
    - si varios requests reciben 401 a la vez, **solo uno renueva** y los demás esperan. Si dos renuevan con el
      mismo token, el segundo recibe 401 y cierras sesión sin motivo.
4. **Si el refresh falla con 401**, borra la sesión y manda al usuario al login.
5. **Cerrar sesión:** `POST /auth/logout` con `{ "refreshToken" }` cierra esta sesión; `POST /auth/logout-all`
   (con Bearer) cierra todas las sesiones del usuario.

### Renovación con OkHttp

La pieza adecuada es un `Authenticator`, que OkHttp invoca justo ante un 401, protegido con un lock:

```kotlin
class TokenAuthenticator(
    private val store: SessionStore,
    private val authApi: AuthApi, // Retrofit SIN este authenticator, para no entrar en bucle
) : Authenticator {
    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("Authorization") == null) return null
        if (responseCount(response) >= 2) return null // ya se reintentó una vez

        synchronized(lock) {
            val sent = response.request.header("Authorization")?.removePrefix("Bearer ")
            val current = store.accessToken()
            // Otro hilo ya renovó mientras esperábamos: reintenta con el token nuevo.
            if (current != null && current != sent) return response.request.withToken(current)

            val refresh = store.refreshToken() ?: return null.also { store.clear() }
            val session = authApi.refresh(RefreshRequest(refresh)).execute().body()
                ?: return null.also { store.clear() } // 401 → al login
            store.save(session) // guarda el refreshToken NUEVO
            return response.request.withToken(session.accessToken)
        }
    }

    private fun Request.withToken(t: String) =
        newBuilder().header("Authorization", "Bearer $t").build()

    private fun responseCount(r: Response): Int =
        generateSequence(r) { it.priorResponse }.count()
}
```

Configuración del cliente:

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(BearerInterceptor(store)) // agrega Authorization a cada request
    .authenticator(TokenAuthenticator(store, authApi))
    .followRedirects(false)
    .build()
```

> **¿Por qué `followRedirects(false)`?** El API responde **302** cuando usas un método no permitido (por
> ejemplo, un GET a un endpoint que solo acepta PUT). Si OkHttp sigue la redirección, terminas parseando una
> página web en lugar de ver el error.

---

## 3. ¿El dojo ya puede dar de alta alumnos?

Después del login, llama a `GET /dojo/config` y decide:

```kotlin
val ready = config.programs.any { it.ranks.isNotEmpty() } && config.schedules.isNotEmpty()
```

- **No está listo:** muestra un onboarding con los pasos que faltan (en el panel es "Mi dojo").
- **Está listo:** la pantalla de alta funciona normal.

Un caso extra: puede haber horarios pero todos llenos (`capacity` alcanzado). En ese caso
`registration-setup` los omite y la lista de horarios llega vacía; muestra el aviso "no hay horarios con cupo".

### Respuesta de `GET /dojo/config`

```json
{
  "asOf": "2026-11-07T10:15:00-06:00",
  "dojo": { "name": "Kensho Dojo", "mainDiscipline": "KARATE" },
  "settings": { "phoneCountryCode": "+52", "monthlyFeeCents": 65000, "currency": "MXN", "enrollmentWaived": true, "billingDay": 5 },
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

> **Consejo:** todas las mutaciones de configuración (los PUT) devuelven este `DojoConfig` completo.
> Reemplaza tu estado con esa respuesta y no hagas otro GET. Así recibes también el temario de cintas
> (`belts`), que el servidor recalcula solo.

---

## 4. Crear el programa con su escalera

`PUT /dojo/programs/{discipline}`. La disciplina va en la URL: `KARATE`, `TAEKWONDO`, `BJJ` o `MIXED`.

```json
{
  "name": "Shotokan Karate",
  "minimumAttendancePercent": 85,
  "maxStripes": 3,
  "ranks": [
    { "belt": "WHITE", "grade": { "system": "KYU", "value": 10 }, "requiredClasses": 24,
      "verifierName": "Sensei Tanaka",
      "requirements": [{ "id": "kata", "title": "Taikyoku Shodan", "description": "Sin pausas" }] },
    { "belt": "YELLOW", "grade": { "system": "KYU", "value": 9 }, "requiredClasses": 30 },
    { "belt": "BLACK", "grade": { "system": "DAN", "value": 1 } }
  ]
}
```

- **201** si lo creó y **200** si lo reemplazó; ambos devuelven `DojoConfig`.
- **Siempre mandas la escalera completa, en orden** (del primer rango al más alto). Lo que no venga se borra.
- **Conserva los `id` de los requisitos existentes al editar.** Así los alumnos no pierden sus verificaciones.
  Para requisitos nuevos, genera el `id` desde el título (slug: `"Kata Heian"` → `"kata-heian"`).
- **No se puede quitar un rango que tiene alumnos:** responde 409 `RANK_IN_USE` y no cambia nada.
- **Escalera sugerida:** el panel trae una por disciplina para no empezar en blanco (`LADDER_TEMPLATES` en
  `public/panel/app.js`). Es solo del cliente; puedes copiarla a la app.

`DELETE /dojo/programs/{discipline}` borra el programa con su escalera y sus horarios: **204**, o
409 `PROGRAM_IN_USE` si tiene alumnos.

---

## 5. Crear un horario

`PUT /dojo/schedules/{scheduleId}`, por ejemplo `PUT /dojo/schedules/karate-mon-wed-1730`:

```json
{
  "discipline": "KARATE",
  "weekdays": ["MONDAY", "WEDNESDAY"],
  "startsAt": "17:30",
  "endsAt": "18:45",
  "description": "Tatami A",
  "capacity": 20
}
```

- **El `id` lo genera la app**, una sola vez al crear el horario. El panel usa `disciplina-días-hora`
  (`karate-mon-wed-1730`). Al editar, reutiliza el id existente.
- Como es PUT, si la red falla puedes **reintentar con el mismo id sin duplicar** el horario.
- `capacity: null` significa sin límite.
- **201** si lo creó y **200** si lo reemplazó.
- Si el horario tiene alumnos activos o en pausa, no se le puede cambiar la disciplina (409 `SCHEDULE_IN_USE`).

`DELETE /dojo/schedules/{scheduleId}`: **204**, o 409 `SCHEDULE_IN_USE` si tiene alumnos activos o en pausa asignados, o asistencias registradas. Las bajas no bloquean: se quedan sin horario.

---

## 6. Plan de cobro (opcional)

`PUT /dojo/settings`:

```json
{ "phoneCountryCode": "+52", "monthlyFeeCents": 65000, "currency": "MXN", "enrollmentWaived": true, "billingDay": 5 }
```

- Los cinco campos son requeridos.
- El monto va en **centavos**: si el usuario captura "650.00", mandas `65000`.
- Sin plan guardado, el dojo usa `+52`, 0, `MXN`, `false` y día 1.

---

## 7. Alta del alumno

1. **`GET /dojo/registration-setup`** devuelve los catálogos de la ficha (disciplinas con sus cintas, horarios
   con cupo, plan y lada) y un folio `registrationId` válido por 7 días. Llámalo **al abrir la ficha**, no al
   guardar.
2. **`POST /students`** con el header `Idempotency-Key: <registrationId>`. Responde **201** con el
   `StudentProfile` completo y el header `Location`.

Reintentos y errores:

- Si la red se cae y reintentas con **el mismo folio y el mismo cuerpo**, recibes el mismo 201 y no se duplica
  el alumno.
- **410 `REGISTRATION_EXPIRED`** (folio vencido) o **409 `IDEMPOTENCY_KEY_REUSED`** (folio usado con otros
  datos): pide otro setup y **conserva lo que el usuario ya capturó**.
- **409 `STUDENT_ALREADY_EXISTS`:** ya hay un alumno con el mismo nombre y fecha de nacimiento.

---

## 8. Validaciones

**El servidor es la autoridad.** Valida en la app para dar feedback inmediato, pero siempre maneja el 422.

### Formato del 422 (configuración y alta)

```json
{
  "code": "VALIDATION_FAILED",
  "message": "Revisa los campos marcados.",
  "fields": [
    { "field": "ranks[2].requirements[0].id", "code": "INVALID_FORMAT" },
    { "field": "endsAt", "code": "OUT_OF_RANGE" }
  ]
}
```

`field` es la ruta exacta del campo en el JSON que mandaste. En Compose, asocia cada campo de la UI a su ruta
y marca los que lleguen:

```kotlin
val errors: Map<String, FieldCode> = body.fields.associate { it.field to it.code }

OutlinedTextField(
    value = name,
    onValueChange = { … },
    isError = "name" in errors,
    supportingText = { errors["name"]?.let { Text(stringResource(it.messageRes)) } },
)
```

| `code` | Texto sugerido |
|---|---|
| `REQUIRED` | Este campo es obligatorio |
| `INVALID_FORMAT` | El formato no es válido |
| `OUT_OF_RANGE` | Fuera del rango permitido |
| `NOT_AVAILABLE` | No está disponible para este dojo |
| `DUPLICATED` | Está repetido |

> **Excepción en autenticación:** `auth/login`, `auth/register` y `auth/refresh` responden 422 con
> `code: "VALIDATION_ERROR"` y solo un `message`, sin `fields`. Ahí muestra el mensaje general.

### Reglas que conviene replicar en la app

| Dónde | Regla |
|---|---|
| Programa | `name` requerido (máx. 120); asistencia 0–100; `maxStripes` 0–10; 1–40 rangos; sin repetir cinta + grado; grado 1–20 o sin grado; `requiredClasses` 0–1000 |
| Requisito | `id` con el formato `^[a-z0-9][a-z0-9-]{0,39}$` y único en el rango; `title` requerido (máx. 120); `description` opcional (máx. 500) |
| Horario | id con el formato `^[a-z0-9][a-z0-9-]{0,39}$`; al menos un día, sin repetir; `HH:mm` con fin mayor que inicio; `description` máx. 200; cupo vacío o 1–1000 |
| Plan | lada `+` y 1–3 dígitos (p. ej. `+52`); centavos ≥ 0; moneda de 3 letras en mayúsculas; día de corte 1–28 |
| Registro | contraseña de 8–256 caracteres; `acceptedTerms` en `true` |

Cintas válidas: `WHITE`, `YELLOW`, `ORANGE`, `GREEN`, `BLUE`, `PURPLE`, `RED`, `BROWN`, `BLACK`.
Sistemas de grado: `KYU`, `KUP`, `DAN`.
Días: `MONDAY` … `SUNDAY`.

### Otros códigos que la app debe manejar

| Status | Código | Qué hacer en la app |
|---|---|---|
| 401 | `MISSING_TOKEN` / `INVALID_TOKEN` | Renovar el token (lo hace el `Authenticator`) |
| 401 | `INVALID_REFRESH_TOKEN` | Borrar la sesión e ir al login |
| 401 | `INVALID_CREDENTIALS` | Mostrar "Correo o contraseña incorrectos" |
| 409 | `EMAIL_TAKEN` | En el registro: el correo ya existe |
| 409 | `RANK_IN_USE` | Trae `ranks: [{ belt, grade }]`; mostrar qué rangos tienen alumnos y no se pueden quitar |
| 409 | `PROGRAM_IN_USE` / `SCHEDULE_IN_USE` | No se puede borrar (o cambiar de disciplina) porque tiene alumnos; deshabilitar el botón si `students > 0` o `activeStudents > 0` |
| 404 | `PROGRAM_NOT_FOUND` / `SCHEDULE_NOT_FOUND` | Ya no existe; recargar la configuración |
| 400 | `BAD_REQUEST` / `INVALID_JSON` | Error de programación: JSON mal formado o falta un header |
| 500 | `INTERNAL_ERROR` | Mensaje genérico y opción de reintentar |

---

## 9. Cómo encaja en la arquitectura de la app

Siguiendo lo que ya usa la app (Hilt, repositorios con `suspend` y `TkdResult`):

- **`AuthRepository`:** login, registro, logout y el `SessionStore`.
- **`DojoConfigRepository`:** `config()`, `saveProgram(discipline, body)`, `deleteProgram(discipline)`,
  `saveSchedule(id, body)`, `deleteSchedule(id)` y `saveSettings(body)`. Cada mutación devuelve
  `TkdResult<DojoConfig>`.
- **Mapeo de errores:** convierte 422 en un `ValidationError(fields)` y 409 en un `Conflict(code, data)`, para
  que el ViewModel marque campos o muestre el aviso.
- **`TkdStudentRegistrationRepository`:** cambia el `FakeTkdStudentRegistrationRepository` por el real, que
  llama a `registration-setup` y `POST students`.

Interfaz de Retrofit de referencia:

```kotlin
interface DojoConfigApi {
    @GET("dojo/config")
    suspend fun config(): DojoConfig

    @PUT("dojo/programs/{discipline}")
    suspend fun saveProgram(@Path("discipline") discipline: String, @Body body: ProgramRequest): Response<DojoConfig>

    @DELETE("dojo/programs/{discipline}")
    suspend fun deleteProgram(@Path("discipline") discipline: String): Response<Unit>

    @PUT("dojo/schedules/{scheduleId}")
    suspend fun saveSchedule(@Path("scheduleId") id: String, @Body body: ScheduleRequest): Response<DojoConfig>

    @DELETE("dojo/schedules/{scheduleId}")
    suspend fun deleteSchedule(@Path("scheduleId") id: String): Response<Unit>

    @PUT("dojo/settings")
    suspend fun saveSettings(@Body body: DojoSettings): Response<DojoConfig>
}

interface StudentRegistrationApi {
    @GET("dojo/registration-setup")
    suspend fun setup(): RegistrationSetup

    @POST("students")
    suspend fun register(
        @Header("Idempotency-Key") registrationId: String,
        @Body body: CreateStudentRequest,
    ): Response<StudentProfile>
}
```

Los endpoints que pueden responder 409 o 422 usan `Response<T>` para poder leer el status y el `errorBody()`
y convertirlos a `TkdResult`.

---

## 10. Cómo probarlo

La carpeta **"Configuración del dojo"** de `postman/kensho-students.postman_collection.json` recorre
exactamente este flujo con un dojo recién registrado (no toca al dojo demo):

registro → config vacía → setup sin disciplinas → programa (422, 404, 201, 200) → horario (422, 201) → plan
(422, 200) → setup habilitado → alta del alumno (201) → conflictos 409 → borrar programa sin alumnos (204, 404).

```bash
postman collection run postman/kensho-students.postman_collection.json -i "Configuración del dojo"
```

Cada request trae las aserciones de lo que debe responder, así que sirve como referencia de los cuerpos y las
respuestas esperadas al implementar cada pantalla.

---

## 11. Probar contra el servidor local desde la red

Antes de desplegar a Netlify, la app puede usar el servidor de desarrollo de tu Mac.

### En la Mac

```bash
npm run dev                 # netlify dev en el puerto 8888, escucha en todas las interfaces
ipconfig getifaddr en0      # IP de la Mac en la red (Wi-Fi), p. ej. 192.168.1.12
```

Comprueba desde otro dispositivo de la misma red (o desde el navegador del celular):
`http://<IP-de-la-Mac>:8888/api/health` debe responder `{"status":"ok","db":"ok",…}`.

- Usa la **base de datos local** (con los usuarios demo del seed), no la de producción.
- El firewall de macOS debe permitir conexiones entrantes a `node` (Ajustes del Sistema → Red → Firewall →
  Opciones). Si macOS pregunta "¿Permitir que node acepte conexiones entrantes?", responde Permitir.
- La IP la asigna el router y puede cambiar; si la app deja de conectar, vuelve a revisarla.

### Base URL según dónde corre la app

| Dónde | Base URL |
|---|---|
| Celular físico en la misma Wi-Fi | `http://<IP-de-la-Mac>:8888/api/v1/` |
| Celular físico por cable USB (`adb reverse`) | `http://127.0.0.1:8888/api/v1/` |
| Emulador de Android Studio | `http://10.0.2.2:8888/api/v1/` (`10.0.2.2` es la Mac vista desde el emulador) |
| Producción | `https://<tu-sitio>.netlify.app/api/v1/` |

Ponla por build type para no tocar código al cambiar de entorno:

```kotlin
// app/build.gradle.kts
android {
    buildFeatures { buildConfig = true }
    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.12:8888/api/v1/\"")
        }
        release {
            buildConfigField("String", "API_BASE_URL", "\"https://<tu-sitio>.netlify.app/api/v1/\"")
        }
    }
}
```

```kotlin
Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL)
```

### Permitir HTTP (sin TLS) solo en debug

Desde Android 9, las apps bloquean HTTP en claro y la llamada falla con
`CLEARTEXT communication to 192.168.1.12 not permitted`. Permítelo **solo en debug** y solo para tus hosts
locales:

`app/src/debug/res/xml/network_security_config.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">192.168.1.12</domain>
        <domain includeSubdomains="false">10.0.2.2</domain>
        <domain includeSubdomains="false">127.0.0.1</domain>
    </domain-config>
</network-security-config>
```

`app/src/debug/AndroidManifest.xml` (se combina con el manifest principal solo en debug)

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application android:networkSecurityConfig="@xml/network_security_config" />
</manifest>
```

Así el build de release sigue exigiendo HTTPS. Con la app apuntando a la Mac, entra con
`demo@kenshodojo.test` / `kensho1234` (dojo ya configurado) o registra un dojo nuevo para recorrer el
onboarding completo.

### Alternativa por cable USB (`adb reverse`)

Si la Wi-Fi no deja pasar la conexión (firewall de un antivirus, red corporativa o de invitados), conecta el
celular por USB con depuración activada y redirige el puerto:

```bash
~/Library/Android/sdk/platform-tools/adb reverse tcp:8888 tcp:8888
```

En el celular, `127.0.0.1:8888` pasa a ser el `8888` de la Mac a través del cable. No depende de la red ni del
firewall, ni de que cambie la IP. La redirección se pierde al desconectar el cable o reiniciar adb: vuelve a
ejecutar el comando.

### Si no conecta

| Síntoma | Causa probable |
|---|---|
| `CLEARTEXT communication … not permitted` | Falta el `network_security_config` de debug o la IP no coincide |
| Timeout o `failed to connect` | Celular en otra red (datos móviles, Wi-Fi de invitados), IP cambiada, servidor apagado o reiniciándose, o un firewall bloqueando el puerto (el de macOS o el de un antivirus como ESET). Prueba `adb reverse` |
| Funciona en el emulador pero no en el celular | El celular no usa `10.0.2.2`: usa la IP de la Mac |
| `{"db":"error"}` en `/api/health` | La BD local no está arrancada o falta `npm run db:migrate` |

