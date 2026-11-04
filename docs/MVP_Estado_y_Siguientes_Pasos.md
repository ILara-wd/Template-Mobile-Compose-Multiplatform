# CierrePro — Estado del MVP y siguientes pasos

> Documento de revisión · Rama `cierre_pro_android` · Corte: 4 de noviembre de 2026
> Fuente de diseño: Figma **Prototipo-Demo-UiKit** (`KqTFktvrRvCzO2idPga5xf`) y `CierrePro_UI_Kit_Guía_Arquitectura_Atomic.md`.

---

## 1. Resumen ejecutivo

- **Qué hay hoy:** un prototipo **navegable de punta a punta** en Android nativo (Jetpack Compose + Material 3). Cubre las 8 pantallas del MVP más la pantalla de acceso. Las pantallas son fieles a Figma, se verificaron en el emulador y todas las interacciones locales funcionan (filtros, checklist, cámara, selector de fotos, fechas, validaciones, WhatsApp, marcador telefónico, mapas).
- **Qué falta para un MVP real:** todo lo que va **detrás de la UI**:
  - capa de datos y persistencia local;
  - backend y APIs;
  - autenticación real;
  - generación del PDF;
  - sincronización offline;
  - arquitectura con ViewModels y navegación formal;
  - pruebas y CI.
- **Datos:** hoy son **de ejemplo y viven en memoria**. Al cerrar la app se pierde todo, salvo la cuenta recordada en el login.

| Área | Estado |
|---|---|
| UI / diseño (Atomic Design) | ✅ Completo para el MVP |
| Navegación entre pantallas | 🟡 Funcional, pero manual (banderas en `MainActivity`) |
| Estado de pantalla | 🟡 `rememberSaveable` en cada pantalla; aún sin ViewModels |
| Persistencia local | 🔴 No existe (solo `SharedPreferences` para la cuenta recordada) |
| Backend / APIs | 🔴 No existe |
| Autenticación | 🔴 Simulada (cualquier correo + contraseña de 6 o más caracteres) |
| Reporte PDF | 🔴 No se genera |
| Pruebas automatizadas / CI | 🔴 Solo las plantillas `ExampleUnitTest` / `ExampleInstrumentedTest` |

---

## 2. Stack actual

| Componente | Versión / detalle |
|---|---|
| Plataforma | **Android nativo**. El repo se llama *Multiplatform*, pero aún no usa KMP (ver §7.1) |
| Lenguaje | Kotlin 2.0.21, JVM 11 |
| Build | AGP 8.13.2, Gradle version catalog (`gradle/libs.versions.toml`) |
| UI | Compose BOM 2024.09.00, Material 3, fuente Inter (`res/font`) |
| SDK | `minSdk 24`, `targetSdk/compileSdk 36` |
| Paquete | `com.invex.one.demo` (*pendiente renombrar*, §6) |
| Dependencias de terceros | Ninguna. Las imágenes se decodifican con `BitmapFactory` y la cámara usa `FileProvider` |

---

## 3. Lo que ya está construido

### 3.1 Pantallas (todas verificadas en el emulador)

| # | Pantalla | Archivo | Nodo Figma | Interacciones funcionales |
|---|---|---|---|---|
| 0 | Acceso (login / registro) | `ui/screens/auth/AuthScreen.kt` | `80:1886` | Validación, ver/ocultar contraseña, "Recordar taller" guardado en `SharedPreferences`, cambio a registro, avisos (toast) |
| 1 | Inicio / Resumen | `ui/screens/home/HomeScreen.kt` | (previo) | "Nuevo servicio" |
| 2 | Clientes | `ui/screens/clients/ClientsScreen.kt` | (previo) | Búsqueda, filtros, llamar, abrir ficha |
| 3 | Detalle del cliente | `ui/screens/clientdetail/ClientDetailScreen.kt` | `73:413` | Llamar, WhatsApp, mapa, nuevo servicio con el cliente precargado, ver reporte |
| 4 | Nuevo servicio · Paso 1 | `ui/screens/newservice/NewServiceScreen.kt` | `70:213` | Tipo de servicio, selectores de fecha y hora, notas |
| 5 | Checklist y Evidencias · Paso 2 | `ui/screens/worklog/WorkLogScreen.kt` | `78:1629` | Checklist con alta de tareas, cámara o galería, quitar fotos, notas, próxima fecha |
| 6 | Reporte y Firma · Paso 3 | `ui/screens/report/ServiceReportScreen.kt` | `74:659` | Enviar por WhatsApp con texto del perfil; completar el flujo |
| 7 | Seguimientos | `ui/screens/followups/FollowUpsScreen.kt` | `75:1203` | Filtros con contadores, marcar contactado (meta del día), posponer, WhatsApp |
| 8 | Perfil del negocio | `ui/screens/profile/BusinessProfileScreen.kt` | `76:1465` | Vista previa en vivo, logotipo (selector y límite de 5MB), validación, guardar |

**Flujo principal:** Acceso → Inicio → Nuevo servicio (Paso 1) → Checklist (Paso 2) → Reporte (Paso 3) → "Guardar y marcar como completado" → Inicio.
El estado de los pasos 1 y 2 se conserva al ir y volver entre pasos (`SaveableStateHolder`) y se descarta al cerrar o completar el flujo.

### 3.2 Sistema de diseño (Atomic Design)

```
ui/
├── theme/        Color.kt (tokens Cp*), Type.kt (Inter), Theme.kt (DemoTheme, solo modo claro)
├── components/
│   ├── atoms/        CpIcon (con tint), CpButton, StatusBadge, StatusDot, cpCard, ImageLoading
│   ├── molecules/    Formularios, checklist, evidencias, tarjetas de cliente/equipo/historial,
│   │                 seguimientos, reporte, perfil, stepper (2 variantes)
│   └── organisms/    TopBars (app, detalle, auth), BottomBar, FlowHeaderStrip, FlowActionBar, Toast
└── screens/          9 pantallas (ver tabla)
```

- **Assets:** 147 vector drawables convertidos 1:1 desde los SVG de Figma, más 8 fotos de ejemplo en `drawable-nodpi/`.
- **Tokens:** la paleta del código sale de Figma (esquema Material) y **no coincide** con la paleta Slate de la guía `.md`. Hay que decidir cuál es la fuente de verdad (§6).

### 3.3 Integraciones del sistema operativo ya funcionando

- `ACTION_DIAL` (llamar), `wa.me` (WhatsApp con mensaje), `geo:` (mapas).
- Selector de fotos del sistema (`PickVisualMedia`) y cámara (`TakePicture` + `FileProvider`, authority `${applicationId}.evidence`).
- `windowSoftInputMode="adjustResize"` + `imePadding`, para que los formularios funcionen con el teclado abierto.

---

## 4. Deuda técnica y brechas conocidas

### 4.1 Funcionales (`TODO` en el código)

1. **El reporte no usa lo capturado en el Paso 2:** muestra `SampleServiceReport` (`MainActivity.kt`).
2. **"Descargar / Compartir PDF"** no hace nada.
3. **"Agendar recordatorio", "+ Equipo", "Historial", "Registrar nuevo cliente", "Seleccionar cliente/equipo"** son callbacks vacíos.
4. **Google, "Huella Rápida" y "¿Olvidaste tu contraseña?"** solo muestran "Disponible próximamente".
5. **No hay "Cerrar sesión"** ni una sesión persistente.
6. **El paso 3 tiene firma estática:** falta un panel de firma en pantalla.
7. **Textos fijos en el código:** las fechas de ejemplo ("18 de Octubre"), el folio y los conteos.

### 4.2 De arquitectura

| Problema | Impacto | Solución propuesta |
|---|---|---|
| Navegación con banderas booleanas en `CierreProApp` | Difícil de escalar, sin deep links, el orden de los `if` importa | **Navigation Compose** con rutas tipadas (`@Serializable`) |
| Estado y lógica dentro de los Composables | Imposible de probar, se pierde al cerrar la app | **ViewModel + `StateFlow<UiState>`** por pantalla (UDF) |
| Modelos de UI = modelos de datos (`Client`, `FollowUp`, …) | Acopla la UI al backend | Capas `domain` (entidades) / `data` (DTO y entidades de BD) / `ui` (UiState) |
| Datos de muestra en archivos de pantalla | Mezcla producción y demo | Moverlos a `data/fake/` y usarlos solo en previews y pruebas |
| Strings en español escritos en el código | Sin i18n, difícil de revisar | Pasarlos a `res/values/strings.xml` (o a Compose Resources si se usa KMP) |
| Sin inyección de dependencias | Acoplamiento | **Hilt** (solo Android) o **Koin** (si se usa KMP) |

---

## 5. Arquitectura objetivo (buenas prácticas)

```
app/                         ← UI (Compose), navegación, DI
core/
├── designsystem/            ← tokens, atoms, molecules, organisms (lo que ya existe)
├── model/                   ← entidades de dominio puras (Kotlin)
├── data/                    ← repositorios: fuente única de verdad = BD local
├── database/                ← Room (o SQLDelight si se usa KMP)
├── network/                 ← cliente HTTP, DTOs, auth interceptor
├── datastore/               ← preferencias / sesión (DataStore + cifrado)
└── sync/                    ← WorkManager: outbox, subida de evidencias, pull de cambios
feature/
├── auth/  home/  clients/  services/  followups/  profile/
```

**Principios**
- **Offline-first:** la UI siempre lee de la BD local mediante `Flow`. La red solo sincroniza, porque el técnico trabaja en azoteas y sótanos sin señal.
- **Flujo de datos unidireccional:** `UiState` inmutable + eventos → ViewModel → repositorio.
- **Repositorios con interfaces**, con implementaciones *fake* para previews y pruebas.
- **Errores tipados:** el resultado de cada operación es éxito o un error de dominio (red, validación, autenticación, conflicto).
- **Seguridad:** tokens en DataStore cifrado con Android Keystore, nunca en `SharedPreferences` en claro. HTTPS con *certificate pinning* opcional y R8 activo en release.
- **Accesibilidad:** áreas táctiles de 48dp o más (ya se cumple en casi toda la app), `contentDescription` en iconos accionables y contraste AA.

---

## 6. Decisiones pendientes (revisar con el equipo)

1. **¿Android nativo o Kotlin Multiplatform?** El nombre del repo sugiere KMP (plantilla Android + iOS).
   - Si se va a KMP, conviene decidirlo **antes** de agregar la capa de datos: Ktor, SQLDelight, Koin y Compose Multiplatform en lugar de Retrofit, Room y Hilt.
   - Los Composables actuales se migran casi sin cambios a Compose Multiplatform. Los `R.drawable` pasarían a Compose Resources.
2. **Fuente de verdad del diseño:** los tokens de Figma (actuales) o la paleta de la guía `.md`. Recomendación: Figma, y actualizar la guía.
3. **Identidad del paquete:** cambiar `com.invex.one.demo` / `rootProject.name = "Demo"` a algo como `com.treecodedev.cierrepro`, antes de publicar.
4. **¿Dónde se genera el PDF?** En el cliente (`PdfDocument` de Android, funciona sin conexión) o en el servidor (diseño consistente y firma verificable). Recomendación: **cliente para el MVP** (funciona sin señal) y servidor después para el folio oficial y la validación.
5. **¿WhatsApp vía `wa.me` (actual) o WhatsApp Business API?** `wa.me` basta para el MVP. La Business API tiene costo y requiere plantillas aprobadas.
6. **Multi-técnico por taller:** ¿un taller con varios técnicos (roles) o una cuenta por persona? Afecta el modelo de datos desde el día 1.
7. **Títulos de las barras superiores:** varias pantallas dicen "Detalle De Servicio" porque así viene en Figma (perfil, checklist, cliente). Confirmar con diseño.

---

## 7. APIs a construir

### 7.1 Convenciones

- **REST + JSON**, versionada: `https://api.cierrepro.mx/v1/...`.
- **Autenticación:** JWT de vida corta (*access*, ~15 min) + *refresh token* rotativo. Header `Authorization: Bearer <token>`.
- **Multi-tenant:** cada recurso pertenece a un `workshopId` que se toma del token; nunca se confía en el que envíe el cliente.
- **IDs generados en el cliente (UUID v7):** permiten crear registros sin conexión y evitan duplicados al sincronizar.
- **Idempotencia:** header `Idempotency-Key` en los `POST` que vienen de la cola de sincronización (outbox).
- **Concurrencia optimista:** campo `version` (o `ETag` / `If-Match`); un conflicto responde `409`.
- **Paginación por cursor:** `?cursor=...&limit=50`, con respuesta `{ items, nextCursor }`.
- **Fechas** en ISO-8601 UTC. **Errores** en formato RFC 7807 (`application/problem+json`) con `code` estable para traducirlos en la app.
- **Archivos (fotos, logo, PDF):** URLs prefirmadas (S3/GCS). La app sube directo al almacenamiento y después confirma al API.
- **Documentación:** contrato OpenAPI 3.1 como fuente de verdad, para generar los DTOs del cliente.

### 7.2 Modelo de dominio (entidades principales)

```
Workshop (taller / tenant) ─┬─ User (técnico, rol: OWNER | TECHNICIAN)
                            ├─ BusinessProfile (membrete: nombre, teléfono, correo, logoUrl, mensaje WhatsApp)
                            └─ Client ─┬─ Equipment (marca, modelo, capacidad TR, refrigerante, serie, ubicación)
                                       └─ ServiceOrder ─┬─ ChecklistItem (título, hecho, lectura opcional: "115 PSI")
                                                        ├─ Evidence (fase ANTES|DESPUÉS, url, pie, takenAt)
                                                        ├─ Diagnosis (observaciones, recomendaciones)
                                                        ├─ Signature (imagen/trazo, firmante, firmadoEn)
                                                        └─ Report (folio, pdfUrl, hash SHA-256, enviadoEn)
FollowUp (clientId, equipmentId?, motivo, fechaSugerida, estado: PENDING|CONTACTED|SNOOZED|OVERDUE)
```

Estados de `ServiceOrder`: `DRAFT → IN_PROGRESS → COMPLETED → DELIVERED`, con `CANCELLED` posible desde cualquier estado previo a `DELIVERED`.

### 7.3 Endpoints por módulo

**Auth**
| Método | Ruta | Uso en la app |
|---|---|---|
| POST | `/v1/auth/register` | "Crear Cuenta": crea el taller y su usuario OWNER |
| POST | `/v1/auth/login` | "Iniciar sesión en mi taller" (correo **o** teléfono + contraseña) |
| POST | `/v1/auth/refresh` | Renovar el token *access* |
| POST | `/v1/auth/logout` | Revocar el *refresh token* |
| POST | `/v1/auth/password/forgot` · `/reset` | "¿Olvidaste tu contraseña?" (OTP por SMS o correo) |
| POST | `/v1/auth/google` | Inicio de sesión con Google (intercambio de `idToken`) |
| — | (local) | "Huella Rápida": `BiometricPrompt` que desbloquea el *refresh token* guardado en Keystore. No requiere API |

**Taller / perfil**
| Método | Ruta | Uso |
|---|---|---|
| GET / PUT | `/v1/workshop/profile` | Perfil del negocio (membrete) |
| POST | `/v1/workshop/logo/upload-url` | URL prefirmada para el logotipo (JPG/PNG de 5MB o menos) |
| GET | `/v1/me` | Usuario actual, rol y taller |

**Clientes y equipos**
| Método | Ruta | Uso |
|---|---|---|
| GET | `/v1/clients?query=&filter=followup\|recent&cursor=` | Directorio con búsqueda y filtros |
| POST / GET / PATCH | `/v1/clients` · `/v1/clients/{id}` | Alta, ficha y edición |
| GET | `/v1/clients/{id}/summary` | Ficha completa: equipos, historial, mantenimiento sugerido, garantía |
| GET / POST | `/v1/clients/{id}/equipment` | Equipos registrados, "+ Equipo" |
| PATCH | `/v1/equipment/{id}` | Editar equipo |

**Órdenes de servicio (flujo de 3 pasos)**
| Método | Ruta | Uso |
|---|---|---|
| POST | `/v1/service-orders` | Paso 1: cliente, equipo, tipo, fecha y hora, notas de acceso |
| GET | `/v1/service-orders?status=&from=&to=&cursor=` | Inicio (servicios recientes) e historial |
| GET / PATCH | `/v1/service-orders/{id}` | Leer y actualizar; incluye checklist, diagnóstico y próxima fecha |
| PUT | `/v1/service-orders/{id}/checklist` | Paso 2: reemplazo completo de las tareas |
| POST | `/v1/service-orders/{id}/evidence/upload-url` | URL prefirmada para una foto |
| POST / DELETE | `/v1/service-orders/{id}/evidence` · `/{evidenceId}` | Confirmar o quitar evidencia |
| POST | `/v1/service-orders/{id}/signature` | Firma del cliente |
| POST | `/v1/service-orders/{id}/complete` | "Guardar y marcar como completado": genera folio y el seguimiento sugerido |
| POST | `/v1/service-orders/{id}/report` | Registrar el PDF (hash + URL) generado en el cliente, o pedir que lo genere el servidor |
| GET | `/v1/reports/{folio}/verify` | (Fase 2) Página pública "Validado por CierrePro" |

**Seguimientos**
| Método | Ruta | Uso |
|---|---|---|
| GET | `/v1/follow-ups?group=today\|next7\|overdue` | Lista agrupada con contadores |
| POST | `/v1/follow-ups/{id}/contacted` | "Marcar como contactado" (suma a la meta diaria) |
| POST | `/v1/follow-ups/{id}/snooze` `{ days: 7 }` | "Posponer 1 sem" |
| GET | `/v1/follow-ups/stats/today` | Meta de re-contacto ("1 de 3 clientes reactivados hoy") |

**Dashboard y sincronización**
| Método | Ruta | Uso |
|---|---|---|
| GET | `/v1/dashboard` | Métricas de Inicio (servicios de la semana, reportes pendientes, seguimientos de los próximos 7 días) |
| GET | `/v1/sync/changes?since=<timestamp>` | Descargar los cambios desde la última sincronización |
| POST | `/v1/sync/batch` | (Opcional) enviar en lote las operaciones pendientes |

### 7.4 Sincronización offline (cliente)

1. Cada acción se escribe en la BD local y en una tabla **outbox** (`operation`, `payload`, `idempotencyKey`, `attempts`).
2. **WorkManager** (`NetworkType.CONNECTED`, reintentos con espera exponencial) procesa la outbox en orden. Las fotos se suben primero a la URL prefirmada y después se confirman.
3. Un **pull** periódico (`/sync/changes`) actualiza la BD local. Un `409` se resuelve con la regla "gana el servidor" y se avisa al usuario cuando aplica.
4. La UI muestra el estado: "Guardado" / "Sin guardar" / "Sincronizando" (los chips de estado ya existen en el diseño).

---

## 8. Plan de trabajo sugerido

### Fase 0 — Fundaciones (1 sprint)
- [ ] Decidir **Android nativo o KMP** y la fuente de tokens (§6).
- [ ] Renombrar paquete y `applicationId`; activar R8 y firma de release.
- [ ] Modularizar: `core:designsystem`, `core:model`, `core:data` y `feature:*`.
- [ ] Navigation Compose con rutas tipadas; eliminar las banderas de `CierreProApp`.
- [ ] ViewModel + `UiState` por pantalla; mover los datos de muestra a `data/fake`.
- [ ] Pasar los textos a `strings.xml` (o Compose Resources).
- [ ] Calidad: **ktlint/detekt**, **GitHub Actions** (build, lint, tests) y pruebas de captura de pantalla (Paparazzi o Roborazzi) para el sistema de diseño.

### Fase 1 — MVP funcional sin conexión (1–2 sprints)
- [ ] BD local (Room/SQLDelight) para todas las entidades de §7.2; repositorios con `Flow`.
- [ ] DataStore cifrado para sesión y perfil del negocio.
- [ ] **Reporte con los datos reales del Paso 2** (checklist, fotos, notas, próxima fecha).
- [ ] **Generación del PDF** en el cliente (`PdfDocument`) + compartir (`ACTION_SEND` con `FileProvider`).
- [ ] Panel de **firma en pantalla** (Canvas + trazos → PNG).
- [ ] Altas de cliente y equipo; selectores reales en el Paso 1.
- [ ] Seguimientos generados al completar un servicio (próximo mantenimiento).
- [ ] "Cerrar sesión" (en el menú de perfil).
- [ ] Compresión de fotos antes de guardarlas (≤ 1600 px, JPEG 80%) y borrado de los temporales de la cámara.

### Fase 2 — Backend y sincronización (2–3 sprints)
- [ ] Contrato **OpenAPI** de §7 revisado con backend.
- [ ] Autenticación real (login, registro, refresh, recuperación de contraseña) + interceptor de tokens.
- [ ] Cliente HTTP (Retrofit u OkHttp, o Ktor si es KMP) + `kotlinx.serialization`.
- [ ] Outbox + WorkManager + `/sync/changes`; subida de evidencias con URL prefirmada.
- [ ] Manejo de errores RFC 7807 con mensajes en español en el toast.

### Fase 3 — Pulido y lanzamiento
- [ ] Google Sign-In (Credential Manager) y **biometría** (`androidx.biometric`).
- [ ] Recordatorios locales (notificaciones) para seguimientos y "Agendar recordatorio".
- [ ] Analítica y crash reporting (Firebase Crashlytics u otra herramienta).
- [ ] Revisión de accesibilidad (TalkBack, escala de fuente al 200%) y de rendimiento (Baseline Profiles).
- [ ] Verificación pública del reporte (`/reports/{folio}/verify`), que respalde "Validado por CierrePro".
- [ ] Publicación en Play Store (prueba interna → cerrada → producción).

---

## 9. Criterios de "MVP listo"

- Un técnico puede **registrarse, dar de alta a un cliente y su equipo, ejecutar un servicio completo sin señal** (checklist, fotos, notas, firma) y **enviar el PDF por WhatsApp**.
- Los datos **sobreviven** al cierre de la app y se **sincronizan** al recuperar señal sin duplicados.
- Los **seguimientos** se generan solos y se pueden atender desde la pestaña Seguimientos.
- Hay pruebas unitarias para ViewModels y repositorios, pruebas de captura de pantalla para el sistema de diseño, y CI en verde en cada PR.

---

## 10. Referencias rápidas

- Navegación actual: `app/src/main/java/com/invex/one/demo/MainActivity.kt` (`CierreProApp`).
- Tokens: `ui/theme/Color.kt` · Tipografía: `ui/theme/Type.kt`.
- Guía de diseño: `CierrePro_UI_Kit_Guía_Arquitectura_Atomic.md` (raíz del repo).
- Nodos de Figma por pantalla: tabla §3.1.
