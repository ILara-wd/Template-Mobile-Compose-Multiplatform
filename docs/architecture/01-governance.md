# 📐 GOVERNANCE — Kensho (app Android de gestión de dojo)

> **Versión:** 1.2.0 | **Última actualización:** 2026-10-30 (ADR-016 Hilt y ADR-017 red/sesión aceptados; ADR-015 escala de fuente 2026-10-28; primera versión 2026-10-27, adaptada del corpus del `:uikit` de Invex)
> **Stack:** Kotlin 2.2 · AGP 9.4 · Jetpack Compose (BOM 2025.12) · Material 3 · Navigation Compose 2.8 · compileSdk/targetSdk 37 · minSdk 26 · JVM 11
> **Arquitectura objetivo:** **MVVM con flujo unidireccional (UDF)** — `ViewModel` + `StateFlow<UiState>` → `Screen` (stateful) → `Content` (stateless) → componentes del sistema de diseño `Tkd*`
> **Naturaleza del proyecto:** app de un solo módulo (`:app`, namespace `dev.treecode.kensho`) para administrar un dojo de Taekwondo: alumnos, pase de lista, cinturones, exámenes y cobros
> **Etapa:** **inicio → Fase 1 (datos reales)** — pantallas implementadas desde el prototipo de Figma con repositorios *Fake*; DI (ADR-016) y red/sesión (ADR-017) decididas y en migración (TD-KEN-012); aún sin CI

> **¿Por qué existe este documento?**
> Es la **fuente de verdad autoritativa** de Kensho. Registra con ADRs numerados *qué* se decidió, *por qué* y *qué se descartó*, la deuda técnica `TD-KEN-0XX` y las fitness functions que la miden. Si dos documentos se contradicen, este gana. Los ADRs son **inmutables**: para cambiar una decisión se crea un ADR nuevo que *supersede* al anterior.
>
> El proyecto va empezando: el objetivo de este corpus es **fijar las convenciones que el código ya sigue** (son buenas) antes de que crezca, y dejar escritos los **disparadores** que indican cuándo dar el siguiente paso (DI, módulos, backend, CI) en lugar de adelantarlo.

---

## Índice

1. [Principios rectores](#1-principios-rectores)
2. [Arquitectura objetivo](#2-arquitectura-objetivo)
3. [Lo que Kensho todavía NO es](#3-lo-que-kensho-todavía-no-es)
4. [Decisiones arquitectónicas (ADR)](#4-decisiones-arquitectónicas-adr)
5. [Reglas no negociables](#5-reglas-no-negociables)
6. [Registro de deuda técnica](#6-registro-de-deuda-técnica)
7. [Fitness functions](#7-fitness-functions)
8. [Proceso de revisión y criterios de aprobación](#8-proceso-de-revisión-y-criterios-de-aprobación)
9. [Hoja de ruta de evolución](#9-hoja-de-ruta-de-evolución)

---

## 1. Principios rectores

| # | Principio | En Kensho significa… |
|---|-----------|----------------------|
| P1 | **Figma es la fuente de verdad visual** | Cada pantalla nace de un nodo de Figma; los recursos se exportan, no se redibujan (ADR-007). |
| P2 | **El estado fluye en una sola dirección** | El `ViewModel` posee el estado; la UI lo pinta y devuelve eventos (ADR-002). |
| P3 | **Tokens, no literales** | Colores, medidas y tipografía viven solo en `ui/theme/` (ADR-004). |
| P4 | **Componer antes que crear** | Se busca en `components/` antes de escribir un átomo o molécula nueva (ADR-005). |
| P5 | **Determinismo** | La misma entrada pinta lo mismo: nada de `LocalDate.now()` escondido en modelos (ADR-006). |
| P6 | **Accesible y en español por defecto** | Texto en `strings.xml` (es-MX), `contentDescription` deliberado (ADR-008, ADR-009). |
| P7 | **YAGNI con disparadores** | No se agrega Room, caché offline ni módulos hasta que se cumpla su disparador (§9). Hilt y Retrofit ya cumplieron el suyo (ADR-016, ADR-017). |

---

## 2. Arquitectura objetivo

### 2.1 Capas dentro de `:app`

```
┌───────────────────────────────────────────────────────────────┐
│ MainActivity → KenshoApp → TkdApp (NavHost, rutas, tabs)      │  navegación
├───────────────────────────────────────────────────────────────┤
│ <feature>/Tkd<Feature>Screen      stateful: viewModel() +     │
│                                   collectAsStateWithLifecycle │
│ <feature>/Tkd<Feature>Content     stateless: UiState + Actions│  presentación
│ <feature>/Tkd<Feature>Sections    secciones internal/private  │
│ <feature>/Tkd<Feature>ViewModel   StateFlow<UiState> + eventos│
├───────────────────────────────────────────────────────────────┤
│ components/atoms, components/molecules  (sistema de diseño)   │  UI reutilizable
│ ui/theme  (TkdColors, TkdDimens, TkdTypography, TkdBeltPalette)│ tokens
├───────────────────────────────────────────────────────────────┤
│ model/   UiState y modelos @Immutable                         │  modelo
│ data/    interfaces Tkd<X>Repository + Fake + Network<X>      │  datos
│ data/remote  api · dto · mapper (Retrofit, ADR-017)           │
│ data/local   TkdSessionStore (DataStore + Tink, ADR-017)      │
│ di/      módulos Hilt: Data, Network, Storage, Dispatchers    │  DI (ADR-016)
│ core/    utilidades puras (Validate)                          │
└───────────────────────────────────────────────────────────────┘
```

**Dirección de dependencias permitida** (verificable con grep, ver FF-08):

| Paquete | Puede importar | NO puede importar |
|---------|----------------|-------------------|
| `ui/theme` | Compose | nada del proyecto salvo `R` |
| `components/atoms` | `ui/theme` | `model`, `data`, ViewModels, features |
| `components/molecules` | `ui/theme`, `atoms`, `model` (modelos de dominio: `BeltColor`, `Student`, `BeltTest`) | ViewModels, `UiState` de pantalla, features; `data` solo dentro de `@Preview` |
| `model` | Kotlin, `java.time`, anotaciones Compose (`@Immutable`), `R` para `@DrawableRes` | Compose UI, `data`, features |
| `data` | `model` | Compose, ViewModels, features |
| `data/remote` | `model`, Retrofit, OkHttp, kotlinx.serialization | nada fuera de `data/` y `di/` lo importa (FF-14) |
| `di` | `data`, `core`, Hilt | features, Compose |
| `<feature>/` | todo lo anterior salvo `data/remote` y `di` | **otro feature** (ver ADR-001) |
| `ui/TkdApp.kt` | todos los `Screen` | ViewModels directos |

### 2.2 Flujo de datos de una pantalla

```
Repository ──▶ ViewModel ──StateFlow<UiState>──▶ Screen ──state──▶ Content ──▶ Sections ──▶ Tkd* components
                   ▲                                │                  │
                   └────── onX(...) eventos ────────┘◀── Actions ──────┘ (navegación sale a TkdApp)
```

### 2.3 Anatomía de referencia

`payments/` es el ejemplo canónico más reciente:

| Archivo | Rol |
|---------|-----|
| `TkdPaymentDetailScreen.kt` | `TkdPaymentDetailActions` (callbacks hacia navegación), `Screen` stateful, `Content` stateless, previews por estado |
| `TkdPaymentDetailSections.kt` | secciones `internal` de la pantalla |
| `TkdPaymentDetailViewModel.kt` | `MutableStateFlow` privado, `StateFlow` público, un método por evento |
| `model/TkdPaymentDetailUiState.kt` | `UiState` y modelos `@Immutable` |
| `data/TkdPaymentsRepository.kt` | interfaz + `FakeTkdPaymentsRepository` con los datos del prototipo |
| `TkdPaymentsFormat.kt` | formateo compartido (moneda, fechas en es-MX) |

---

## 3. Lo que Kensho todavía NO es

| Capacidad | Estado | Disparador para introducirla (ver §9) |
|-----------|--------|---------------------------------------|
| Backend / red | ✅ Implementado: Retrofit + OkHttp + kotlinx.serialization (ADR-017) en 8 / 9 repositorios (2026-11-19); del resumen, lo visible sale de `me/profile` y 4 secciones ocultas esperan su endpoint (TD-KEN-013) | ✅ Cumplido (2026-10-30) |
| Persistencia de sesión (DataStore + Tink) | ✅ Implementado en el piloto `auth` (2026-10-31): `TkdSessionStore` | ✅ Cumplido: la sesión y "Recordarme" sobreviven al cierre |
| Persistencia de datos / offline (Room) | ❌ No hay. El perfil del instructor (ADR-021), la lista de alumnos (ADR-022) y las fichas de alta sin terminar (ADR-023) se guardan en el dispositivo como registros cifrados en DataStore; no es modo offline | Un flujo debe funcionar sin conexión (p. ej. pase de lista en el tatami) |
| Inyección de dependencias | ✅ Implementado: Hilt con KSP (ADR-016); 0 defaults a la *Fake* en ViewModels (2026-11-19) | ✅ Cumplido (2026-10-30) |
| Multi-módulo | ❌ No — un solo `:app` | >~15 pantallas, build >2 min, o un segundo equipo |
| Capa de dominio (UseCases) | ❌ No hay. El perfil compartido por 7 VMs se resolvió en el repositorio + `TkdSenseiViewModel`, sin UseCase (ADR-021) | Lógica de negocio reutilizada por 2+ ViewModels |
| CI / análisis estático | 🟡 Detekt + reglas `Kensho*` locales; sin pipeline | **Ya** — es deuda (TD-KEN-008) |
| Modo oscuro / multi-marca | ❌ No — solo paleta clara de Figma | El diseño lo defina |

> No inventes estas capas "por si acaso" (P7). Cuando se cumpla un disparador, se registra un ADR nuevo antes de escribir código.

---

## 4. Decisiones arquitectónicas (ADR)

Formato: **Contexto · Decisión · Alternativas descartadas · Consecuencias · Estado**.

### ADR-001: Un solo módulo `:app`, paquetes por feature

- **Contexto:** proyecto nuevo, un desarrollador, ~10 pantallas.
- **Decisión:** un único módulo Gradle. Dentro, un paquete por feature (`login`, `signup`, `students`, `profile`, `checkin`, `payments`, `ui/present` = resumen) más paquetes compartidos (`components`, `model`, `data`, `ui/theme`, `core`). Un feature **no importa** otro feature; lo compartido se mueve a `components/` o `model/`.
- **Descartado:** multi-módulo desde el día uno (coste de build y configuración sin beneficio todavía).
- **Consecuencias:** el aislamiento entre features es por convención y grep (FF-08), no por el compilador. Se revisa cuando se cumpla el disparador de §9.
- **Estado:** ✅ Activo. ⚠️ El resumen vive en `ui/present/` en lugar de `dashboard/` (TD-KEN-007).

### ADR-002: MVVM + UDF con separación `Screen` / `Content` / `Actions`

- **Contexto:** las pantallas deben poder previsualizarse y probarse sin ViewModel.
- **Decisión:**
  - El `ViewModel` expone `val uiState: StateFlow<Tkd<X>UiState>` (respaldado por un `MutableStateFlow` privado) y un método público por evento (`onFilterSelected`, `onConfirm`).
  - `Tkd<X>Screen` es el único composable que conoce al ViewModel: lo obtiene con `viewModel()` (con `key` si depende de un argumento) y recolecta con `collectAsStateWithLifecycle()`.
  - `Tkd<X>Content` es stateless: recibe `UiState`, un `Tkd<X>Actions` (data class de callbacks hacia fuera, normalmente navegación) y lambdas de evento del VM.
  - La navegación nunca ocurre dentro de un feature; sale por `Actions` hacia `TkdApp`.
- **Descartado:** MVI con `sealed interface Intent` (más ceremonia de la que pide el tamaño actual); estado en `remember` dentro de la pantalla (no sobrevive a recomposición/rotación y no se prueba).
- **Consecuencias:** cada pantalla tiene previews de `Content` con datos *Fake* y se puede probar con Compose UI Test sin VM.
- **Estado:** ✅ Activo en las 8 pantallas.

### ADR-003: `UiState` y modelos inmutables en `model/`

- **Decisión:** todo `UiState` y modelo de presentación es `data class` (o `enum`/`sealed`) **`@Immutable`** con `val`. Los valores derivados son propiedades calculadas del estado (p. ej. `visibleTransactions`, `totalCents`), no estado duplicado. Montos de dinero en **centavos `Int`**, nunca `Float`/`Double`.
- **Descartado:** `kotlinx.collections.immutable` (se reevaluará si aparecen problemas de recomposición medidos).
- **Estado:** ✅ Activo (32/32 data classes de `model/` llevan `@Immutable`).

### ADR-004: Design tokens propios `Tkd*` como única casa de literales

- **Contexto:** el prototipo de Figma tiene su propia paleta, escala de espacios y tipografía (Plus Jakarta Sans). El `MaterialTheme` de plantilla (`Color.kt`, `Theme.kt`, `Type.kt`) no la representa.
- **Decisión:** colores en `TkdColors`, medidas en `TkdDimens`, estilos de texto en `TkdTypography`, colores de cinturón en `TkdBeltPalette`. Fuera de `ui/theme/` no hay `Color(0x…)`, `Color.White`, `N.dp` ni `N.sp`. Un token nuevo se nombra por **rol** (`WarningContainerSoft`, `AvatarPayer`), no por valor (`Amber50`, `Size56`).
- **Descartado:** `dimens.xml`/`colors.xml` (los tokens en Kotlin se usan directo en Compose y se navegan mejor); mapear todo a `MaterialTheme.colorScheme` (el diseño tiene más roles que Material).
- **Consecuencias:** el tema Material de plantilla queda como contenedor y debe limpiarse (TD-KEN-003).
- **Estado:** ⏳ Casi completo — 4 colores y 1 `.dp` fuera de `theme/` (TD-KEN-004).

### ADR-005: Sistema de diseño atómico dentro de `components/`

- **Decisión:**
  - `components/atoms/`: piezas sin conocimiento del dominio del dojo (`TkdPill`, `TkdSwitch`, `TkdAvatar`). Reciben texto, colores del tema y *slots*.
  - `components/molecules/`: combinaciones reutilizables; pueden recibir modelos de `model/` (`BeltColor`, `Student`, `BeltTest`) pero nunca un `UiState` de pantalla ni un ViewModel; las *Fake* de `data/` solo se usan dentro de previews.
  - Secciones específicas de una pantalla viven en el paquete del feature como `internal`/`private`.
  - Todo composable público lleva prefijo `Tkd`, `modifier: Modifier = Modifier` como primer parámetro opcional y al menos una `@Preview`.
  - Para variantes visuales se prefiere un objeto de estilo con defaults (`TkdInputFieldStyle` + `TkdInputFieldDefaults.Note`) o un *slot* antes que una colección de booleanos.
- **Descartado:** depender del `uikit` de Invex (`libs.uikit` está comentado): impone la marca Invex y no reproduce el diseño de Kensho.
- **Estado:** ✅ Activo. ⚠️ `CustomLinearProgress` no sigue el prefijo `Tkd` (TD-KEN-007).

### ADR-006: Datos detrás de interfaces de repositorio con implementaciones *Fake* y fechas inyectadas

- **Decisión:**
  - Cada fuente de datos es una `interface Tkd<X>Repository` en `data/` con una `Fake<X>Repository` que contiene los datos del prototipo de Figma.
  - El ViewModel recibe el repositorio por constructor con default a la *Fake* (`repository: TkdPaymentsRepository = FakeTkdPaymentsRepository()`); así el test inyecta otra.
  - La **fecha de referencia** ("hoy") sale del repositorio (`closingDate()`, `asOf`) o se inyecta; los modelos y la UI no llaman a `LocalDate.now()` / `System.currentTimeMillis()`.
- **Descartado:** DI con framework ahora (ver ADR-011); `LocalDate.now()` como default (los datos del prototipo son de 2024 y las cuentas regresivas salían negativas).
- **Estado:** ⏳ Parcial — 1 default con `LocalDate.now()` (TD-KEN-005). 🔁 El *default a la Fake* en el constructor queda **reemplazado por ADR-016** (las interfaces y las *Fake* siguen vigentes); los contratos pasan a `suspend`/`Flow` + `TkdResult` por ADR-017.

### ADR-007: Flujo Figma → código

- **Decisión:**
  - Toda pantalla referencia su nodo de Figma (archivo `Prototipo-Demo-UiKit`) en el comentario de sección de `strings.xml`.
  - Iconos: `res/drawable/ic_tkd_*.xml`, VectorDrawable convertido 1:1 desde el SVG exportado, con cabecera `<!-- Generado desde Figma (...). No editar a mano. -->`. Fotos: `img_tkd_*`.
  - Se reutiliza un drawable existente **solo si es idéntico** (mismo path data); si cambia tamaño, recorte o color, es un recurso nuevo.
  - Nunca se dejan URLs temporales de Figma en el código.
  - La implementación se verifica contra la captura de Figma en emulador antes de cerrar.
- **Estado:** ✅ Activo (107 `ic_*`, 26 `img_*`).

### ADR-008: Accesibilidad como parte del contrato

- **Decisión:** todo `Icon`/`Image` informativo tiene `contentDescription` desde `stringResource` (`tkd_cd_*`). `contentDescription = null` solo para elementos decorativos o cuyo significado ya da el texto vecino, **con comentario en la misma línea**. Filas seleccionables usan `selectable`/`toggleable` con `Role` sobre la fila completa; objetivos táctiles ≥ 48dp.
- **Estado:** ⏳ Parcial — 65 `null` sin comentario (TD-KEN-002).

### ADR-009: Texto de usuario en `strings.xml`, español de México

- **Decisión:** el idioma base de la app (`res/values/`) es **español de México**; si se quiere inglés, va en `res/values-en/`. Todo texto visible vía `stringResource`/`pluralStringResource`, claves `tkd_<feature>_<descripcion>` en snake_case, agrupadas con comentario de sección (feature + nodo de Figma). Números, monedas y fechas se formatean con `Locale.forLanguageTag("es-MX")` (`spanish`) explícito mediante helpers compartidos (`TkdPaymentsFormat.kt`), nunca con el locale por defecto ni concatenando.
- **Estado:** ⏳ Parcial — 278 usos de `stringResource`/`pluralStringResource` y 2 formatos sin locale (TD-KEN-004), pero resumen, alumnos, perfil, pase de lista y barra inferior siguen en inglés porque así venía el primer prototipo (TD-KEN-011).

### ADR-010: Previews como documentación viva

- **Decisión:** todo composable público tiene ≥ 1 `@Preview` `private`. Cada `Content` de pantalla tiene una preview por estado relevante (vacío, normal, confirmado, error) construida con la *Fake* del repositorio.
- **Estado:** ⏳ Casi completo (`TkdSurface`, `CustomLinearProgress` sin preview).

### ADR-011: Sin framework de DI mientras todo sea *Fake*

- **Decisión:** dependencias por parámetros de constructor con default. Al cumplirse el disparador (primer repositorio real o VM con >2 dependencias) se adopta **Hilt** en un ADR nuevo.
- **Estado:** 🔁 **Reemplazado por ADR-016** (2026-10-30). Los defaults siguen en el código hasta migrar cada VM (TD-KEN-012).

### ADR-012: Sin logging en `main` hasta definir un logger

- **Decisión:** no se usa `android.util.Log`, `println` ni `printStackTrace` en `src/main`. Cuando haga falta registrar algo se introduce **un único wrapper** (p. ej. Timber detrás de `AppLogger`) que no emita en release y nunca registre datos personales (nombres, teléfonos, montos de alumnos).
- **Estado:** ✅ Activo (0 ocurrencias). ADR-017 fija el wrapper: `AppLogger` sobre Timber y `HttpLoggingInterceptor` `BASIC` solo en debug.

### ADR-013: Estrategia de pruebas

- **Decisión:**
  - **Unit (JVM, `src/test`)**: todo ViewModel y toda función pura (`Validate`, formateo, propiedades derivadas del `UiState`). JUnit4 + `kotlinx-coroutines-test` cuando haya corrutinas. Repositorios *Fake* en lugar de mocks.
  - **UI (`src/androidTest`)**: Compose UI Test sobre `Content` para flujos interactivos (selección, validación, confirmación).
  - Sin Mockito mientras las *Fake* basten; sin `Thread.sleep`.
- **Estado:** ✅ Activo (2026-11-19, TD-KEN-001 saldada): todo ViewModel tiene unit test con su *Fake*; Compose UI tests sobre `Content` en `androidTest`. Ampliado por ADR-017: mappers con fixtures JSON y repositorios de red con `MockWebServer`; en `androidTest`, enlaces sustituidos con `@TestInstallIn` (ADR-016).

### ADR-014: Navegación centralizada en `TkdApp`

- **Decisión:** un `NavHost` en `ui/TkdApp.kt`. Las tabs son `TkdTab`; las rutas con argumentos se declaran como constantes privadas (`PAYMENT_DETAIL_ROUTE = "payment/{transactionId}"`) con `navArgument`. Las pantallas reciben callbacks, nunca el `NavController`.
- **Descartado (por ahora):** rutas tipadas con `@Serializable` (requiere el plugin de serialización; se reevaluará al subir Navigation).
- **Estado:** ✅ Activo. ⚠️ `MainActivity` mantiene `AppDestinations` duplicando `TkdTab` y código comentado (TD-KEN-003). Con ADR-017 el plugin de serialización entra al build: las rutas tipadas se pueden reevaluar en un ADR nuevo. `TkdApp` además observa la sesión (ADR-017) para volver a `login`.

### ADR-015: Texto escalable hasta 200 % con topes explícitos

- **Contexto:** al medir la app con tamaño de fuente del sistema al 150 % y 200 % (emulador API 33) se recortaban barras, chips, nombres de alumno y montos: alturas fijas, filas mal repartidas y huecos de ancho fijo. Ver [`FONT_SCALE_ANALISIS.md`](FONT_SCALE_ANALISIS.md).
- **Decisión:**
  - Todo texto escala con la preferencia del usuario (los estilos de `TkdTypography` están en `sp`); nunca se multiplica `fontSize` por `fontScale` a mano.
  - Primero se resuelve con layout: `heightIn(min)` en lugar de `height` en contenedores con texto, `FlowRow` para nombre + badge, `widthIn(max)` para elementos laterales, más líneas con elipsis.
  - Solo donde el espacio es realmente fijo se usa `ui/theme/TkdFontScale.kt`: `TkdFontScale.Limited` (150 %) con `withFontScale`/`TkdFontScaleLimit`, o `shrinkToFit()` para una línea en ancho fijo (nunca por debajo del 100 %). Cada tope lleva comentario en la línea.
  - Los componentes con texto que se creen o modifiquen usan `@TkdFontScalePreviews` (100/150/200 %).
- **Descartado:** congelar el texto al 100 % (incumple WCAG 1.4.4); multiplicar tamaños por `fontScale` (escala dos veces); limitar todo el árbol de la app (castiga a quien necesita texto grande).
- **Estado:** ✅ Activo. Las 13 roturas medidas el 2026-10-28 están corregidas.

### ADR-016: Inyección de dependencias con Hilt

- **Contexto:** ADR-011 dejó la DI con framework para cuando se cumpliera su disparador: primer repositorio real **o** un VM con >2 dependencias. La integración de red (ADR-017) cumple el primero. Hoy 5 de 8 ViewModels reciben ya 2 repositorios con default a la *Fake* (`repository` + `dojoRepository`), `TkdStudentProfileRepository` crea su propia `FakeTkdStudentsRepository` y las pantallas construyen el VM con `viewModel(key = …) { … }`. Con implementaciones reales (cliente HTTP, almacén de sesión, dispatchers), el cableado manual por defaults deja de escalar y esconde qué implementación usa la app.
- **Decisión:**
  - Se adopta **Hilt** (con **KSP**, no kapt). `KenshoApplication` lleva `@HiltAndroidApp` y `MainActivity` `@AndroidEntryPoint`.
  - Todo ViewModel es `@HiltViewModel class Tkd<X>ViewModel @Inject constructor(…)`; **se eliminan los defaults a la *Fake*** del constructor. Los argumentos de ruta (`transactionId`, `studentId`) se leen de `SavedStateHandle`, no se pasan por constructor.
  - `Tkd<X>Screen` obtiene el VM con `hiltViewModel()`; el parámetro `viewModel` se conserva para que el llamador pueda sustituirlo.
  - Los enlaces viven en `di/` (paquete compartido, como `data/`):
    - `DataModule` (`@Binds`, `@Singleton`): interfaz `Tkd<X>Repository` → implementación de red (ADR-017).
    - `DispatchersModule`: `CoroutineDispatcher` con calificadores `@IoDispatcher` / `@DefaultDispatcher`. Ningún repositorio usa `Dispatchers.IO` literal.
    - `NetworkModule` y `StorageModule`: los objetos de ADR-017.
  - Las *Fake* se quedan en `src/main/.../data/` para previews y tests unitarios de VM (instanciación directa, sin Hilt). En `androidTest` se sustituyen enlaces con `@TestInstallIn`.
  - Un feature sigue sin importar otro feature (ADR-001): los módulos Hilt solo enlazan tipos de `data/`, `core/` y `di/`.
- **Descartado:**
  - **Koin:** resuelve en tiempo de ejecución; un enlace faltante falla al abrir la pantalla, no al compilar.
  - **DI manual con `AppContainer`:** viable con 6 repositorios, pero hay que reescribir a mano la fábrica de cada VM con `SavedStateHandle` y el alcance de sesión, que Hilt ya da.
  - **kapt:** más lento y en modo mantenimiento frente a KSP.
- **Consecuencias:**
  - Nuevos plugins (`com.google.devtools.ksp`, `com.google.dagger.hilt.android`) y dependencias `hilt-android`, `hilt-compiler`, `androidx-hilt-navigation-compose` vía `libs.versions.toml`, con versiones compatibles con AGP 9.4 / Kotlin 2.2 fijadas en el PR de adopción.
  - Los tests unitarios de VM no cambian de estrategia (ADR-013): construyen el VM con la *Fake* y un `StandardTestDispatcher`.
  - Las previews de `Content` no dependen de Hilt (ya no reciben VM, ADR-002).
  - ADR-011 pasa a **Reemplazado por ADR-016**; ADR-006 se actualiza: "el VM recibe el repositorio por constructor" sigue vigente, el *default a la Fake* deja de estarlo.
  - Nuevas reglas candidatas para `:lint-rules`: sin `= Fake` en parámetros de constructor de `*ViewModel` en `src/main`; sin `Dispatchers.IO`/`Dispatchers.Default` fuera de `di/`.
- **Estado:** ✅ Aceptado (2026-10-30). En implementación: Hilt 2.59 + KSP instalados y `di/` creado (2026-10-31); `TkdLoginViewModel` y `TkdSignUpViewModel` ya son `@HiltViewModel`. Quedan 12 parámetros `= Fake…()` en 6 ViewModels y 1 repositorio (FF-15, TD-KEN-012).

### ADR-017: Capa de red y almacenamiento de sesión

- **Contexto:** Kensho empieza a consumir un API propio del dojo (autenticación, alumnos, pase de lista, pagos). Las interfaces `Tkd<X>Repository` de ADR-006 son la frontera, pero hoy son **síncronas** (`fun cashFlow(): CashFlow?`, salvo `TkdAuthRepository`) y devuelven `null` como único caso de fallo. Los `UiState` no tienen estados de carga ni de error. La sesión ("Recordarme" y el token) debe sobrevivir al cierre de la app, lo que también cumple el disparador de persistencia (§3).
- **Decisión:**
  - **Cliente:** Retrofit + OkHttp + kotlinx.serialization (plugin `org.jetbrains.kotlin.plugin.serialization`). JSON con `ignoreUnknownKeys = true` y `explicitNulls = false`.
  - **Estructura** (dentro de `data/`, sin módulos nuevos, ADR-001):
    ```
    data/
    ├── Tkd<X>Repository.kt          # interfaz (contrato de la UI)
    ├── FakeTkd<X>Repository.kt      # previews y tests
    ├── NetworkTkd<X>Repository.kt   # implementación real
    ├── TkdResult.kt                 # TkdResult + TkdDataError
    ├── remote/api/                  # interfaces Retrofit: Tkd<X>Api
    ├── remote/dto/                  # @Serializable <X>Dto, espejo del JSON
    ├── remote/mapper/               # <X>Dto.toModel(): único sitio que conoce DTO y modelo
    └── local/                       # TkdSessionStore (DataStore)
    ```
    Los DTO y las clases Retrofit/OkHttp **no salen de `data/remote/`**: `model/`, `ui/`, `components/` y los features no los importan.
  - **Contratos de repositorio:** las operaciones pasan a `suspend` (lecturas puntuales y comandos) o `Flow` (datos que cambian, p. ej. la sesión). Los fallos se devuelven tipados, no como excepción ni `null`:
    ```kotlin
    sealed interface TkdResult<out T> {
        data class Success<T>(val data: T) : TkdResult<T>
        data class Failure(val error: TkdDataError) : TkdResult<Nothing>
    }
    enum class TkdDataError { NoConnection, Timeout, Unauthorized, NotFound, Conflict, Server, Unknown }
    ```
    El repositorio traduce `IOException`, `HttpException` y `SerializationException` a `TkdDataError` y **relanza `CancellationException`**. Ninguna excepción de red llega al ViewModel.
  - **Estado de pantalla:** cada `UiState` afectado modela carga y error, con `sealed interface Tkd<X>UiState { Loading; Content(…); Error(@StringRes message) }` o con los campos `isLoading` / `error`, según la pantalla. El VM mapea `TkdDataError` a un `@StringRes` `tkd_error_*` (ADR-009); la UI nunca muestra mensajes del servidor tal cual.
  - **Contrato de datos:**
    - Dinero en **centavos enteros** en el JSON (ADR-003); el mapper no convierte unidades.
    - Fechas en ISO-8601 (`LocalDate`/`Instant`) parseadas en el mapper. La fecha de referencia ("hoy", `closingDate()`, `asOf`) la entrega el servidor (ADR-006).
    - Catálogos visibles (categorías, cinturones, métodos de pago, estados) llegan como **códigos** y se traducen a enums de `model/` y a `strings.xml`. Un código desconocido se mapea a una variante `Unknown`, no rompe la pantalla.
    - Las imágenes de alumnos llegan como URL. `model/` deja de llevar `@DrawableRes` para fotos y usa un tipo `TkdImage` (`Resource`/`Remote`); la carga remota se hace con **Coil** (placeholder e error con `img_tkd_*`, `contentDescription` desde recursos).
  - **Configuración y seguridad:**
    - `BASE_URL` por `buildConfigField` según buildType/flavor (`dev`, `prod`). Ningún secreto ni token en el repositorio.
    - `network_security_config.xml` sin cleartext (`cleartextTrafficPermitted="false"`), salvo el host local de desarrollo en el flavor `dev`.
    - Timeouts explícitos (conexión 15 s, lectura/escritura 30 s). Reintentos solo en GET idempotentes y nunca en cobros.
  - **Autenticación y sesión:**
    - `TkdSessionStore` sobre **DataStore** con el token cifrado con **Tink** (clave en Android Keystore). No se usa `EncryptedSharedPreferences` (deprecado).
    - Un `Interceptor` de OkHttp agrega `Authorization: Bearer`; un `Authenticator` renueva el token ante 401 una sola vez y, si falla, limpia la sesión.
    - `TkdAuthRepository` expone `session: StateFlow<TkdSession>`; `TkdApp` lo observa y navega a `login` cuando expira. Ningún feature recibe `NavController` (ADR-014).
    - `rememberedEmail()` se lee de DataStore, no de memoria.
  - **Logging:** se cumple ADR-012 con un único wrapper (`AppLogger` sobre Timber). `HttpLoggingInterceptor` solo en debug, con nivel `BASIC` y `redactHeader("Authorization")`; nunca `BODY`, porque los cuerpos llevan datos de alumnos y montos.
  - **Pruebas** (amplía ADR-013):
    - Mappers: unit tests con fixtures JSON en `src/test/resources/fixtures/`.
    - `NetworkTkd<X>Repository`: `MockWebServer` para 2xx, 401, 404, 409, 5xx, timeout y JSON malformado, verificando el `TkdDataError` resultante.
    - ViewModels: con la *Fake* (ahora configurable para devolver `Failure`) y `StandardTestDispatcher`, cubriendo `Loading` → `Content` y `Loading` → `Error`.
    - Compose UI: estado de error y reintento sobre `Content`.
  - **Contrato del API:** el API se describe en un documento OpenAPI versionado en el repositorio (`docs/api/kensho-api.yaml`). Las fixtures y el mock de desarrollo salen de ese contrato, lo que permite avanzar la app antes de que el backend esté desplegado.
  - **Migración:** un repositorio por PR, empezando por `TkdAuthRepository` (ya es `suspend` y obliga a resolver sesión y token), luego `payments`. Mientras una interfaz no migre, sigue enlazada a su *Fake* en `DataModule`.
- **Descartado:**
  - **Ktor Client:** sin beneficio mientras la app sea solo Android. Se reconsidera si se adopta Kotlin Multiplatform.
  - **Gson/Moshi:** kotlinx.serialization ya será necesario para rutas tipadas (ADR-014) y respeta valores por defecto y nulabilidad de Kotlin.
  - **`Result<T>` de la librería estándar:** el error queda como `Throwable` sin tipo; la UI tendría que inspeccionar excepciones.
  - **Caché offline con Room ahora:** nada exige todavía trabajar sin conexión. Su disparador es que un flujo (p. ej. el pase de lista en el tatami) deba funcionar offline, y requerirá su propio ADR.
  - **Certificate pinning:** se evalúa en ADR-019 (release) junto con la política de rotación de certificados.
- **Consecuencias:**
  - Cambian las firmas de las 6 interfaces de `data/` y de sus *Fake*; los VM lanzan las cargas en `viewModelScope` en `init` o en un `onRetry()`.
  - Los `UiState` de las 8 pantallas ganan variantes de carga y error, con su preview correspondiente (ADR-010).
  - Los 6 campos de foto con `@DrawableRes` en `model/` (`TkdPaymentsUiState`, `TkdPaymentDetailUiState`, `TkdStudentProfileUiState`, `TkdDashboardUiState`) cambian a `TkdImage`. Los iconos de la app siguen como `@DrawableRes`.
  - Dependencias nuevas vía `libs.versions.toml`: Retrofit, converter de kotlinx.serialization, OkHttp (+ logging-interceptor solo debug), kotlinx-serialization-json, DataStore, Tink, Coil 3, Timber, mockwebserver y kotlinx-coroutines-test (test). Antes conviene cerrar TD-KEN-009 (versiones desalineadas y `mavenLocal()`).
  - El manifiesto declara `INTERNET` y `ACCESS_NETWORK_STATE` (este último para distinguir `NoConnection` antes de llamar).
  - ADR-014 puede reevaluar las rutas tipadas, porque el plugin de serialización ya estará presente.
  - Reglas candidatas para `:lint-rules`: `KenshoNoRemoteOutsideData` (imports de `retrofit2`, `okhttp3` y `data.remote` fuera de `data/` y `di/`) y `KenshoNoServerMessageInUi`.
- **Estado:** ✅ Aceptado (2026-10-30). Piloto `auth` implementado (2026-10-31): `NetworkTkdAuthRepository`, `TkdSessionStore` (DataStore + Tink), `TkdAuthInterceptor` + `TkdTokenAuthenticator`, sesión observada en `TkdApp` y contrato `docs/api/kensho-api.yaml`. Desde 2026-10-31 debug apunta al API remoto de pruebas (`https://api-kensho.netlify.app/api/v1/`, `kensho.authStaticMock.debug=false`); el adaptador `StaticMockTkdAuthApi` queda sin uso. 🔁 El punto "la UI nunca muestra mensajes del servidor" queda **modificado por ADR-020**. Pendiente (2026-11-07): 4/9 repositorios sin red (`auth`, `students`, el perfil, el alta de alumnos y la configuración del dojo ya migrados), 3/10 `UiState` sin carga/error, 6 fotos `@DrawableRes` en `model/` (TD-KEN-012). Piloto: `TkdAuthRepository`. ✏️ Precisado (2026-11-07, `docs/android-integration-guide.md` §2): "si falla, limpia la sesión" aplica solo a un **401 de `auth/refresh`** (o sin refresh token); un fallo de red, timeout o 5xx conserva la sesión y la petición original falla con `TkdRefreshUnavailableException` → `NoConnection`/`Timeout`/`Server`. El cliente no sigue redirecciones (`followRedirects(false)`: el API responde 302 a un método no permitido), el interceptor no registra el token y `POST auth/logout` revoca solo la sesión del dispositivo.

### ADR-020: Mensaje de error del API en pantalla

- **Contexto:** ADR-017 prohibió mostrar el texto del servidor porque podía llegar en inglés o con detalles técnicos. El API propio ya responde los errores con `{code, message}` (`docs/api/kensho-api.yaml`, `Error`) y `message` está escrito para el sensei, en español. El mensaje local por código HTTP es más pobre: un 401 o un 422 no distinguen la causa.
- **Decisión:**
  - `tkdApiCall` lee el `message` del cuerpo de error de un `HttpException` y lo guarda en `TkdResult.Failure.serverMessage`. Si falta, viene vacío o el cuerpo no es ese JSON, queda `null`.
  - El VM arma el texto con `TkdResult.Failure.toUiText(fallback)`: `TkdUiText.Plain(serverMessage)` si existe; si no, `TkdUiText.Res(fallback)` con el `tkd_error_*` o el mensaje propio de la pantalla. La UI lo resuelve con `TkdUiText.asString()`.
  - Solo se acepta `message` del API propio con el esquema `Error`. Nunca `exception.message`, ni cuerpos de error de terceros (proxy, CDN).
  - Contrato para el backend: `message` en español de México, sin datos técnicos (trazas, SQL, IDs internos) y apto para mostrarse tal cual.
- **Descartado:** traducir cada `code` a un recurso local. Es más estricto, pero duplica en la app el catálogo de errores del backend y obliga a publicar una versión por cada código nuevo. Se reconsidera si la app necesita otro idioma además de es-MX.
- **Consecuencias:** `submitError`/`error` de los `UiState` pasan de `@StringRes Int` a `TkdUiText`. Sin conexión, timeout o un error sin cuerpo siguen mostrando `tkd_error_*`. La regla candidata `KenshoNoServerMessageInUi` de ADR-017 se limita a `exception.message`.
- **Estado:** ✅ Aceptado (2026-10-31). Aplicado en login y registro. ✏️ Ampliado (2026-11-06, alta de alumnos): `Failure` también guarda `serverCode` (el `code` del cuerpo) y `fieldErrors` (rutas de `fields[].field` de un `422`); se agregan `TkdDataError.Expired` (410) e `Invalid` (422). El texto sigue saliendo solo de `message`; `code` y `fields` sirven para decidir qué hacer (recargar el folio, marcar campos), nunca se muestran.

### ADR-021: Perfil del instructor compartido: guardado en el dispositivo y `TkdSenseiViewModel`

- **Contexto:** siete pantallas (Resumen, Alumnos, Perfil del alumno, Mi dojo, Pase de lista, Pagos y Detalle de cobro) mostraban el encabezado del dojo. Cada ViewModel pedía `GET me` con el mismo bloque `loadAccount()` copiado, y el Resumen además pedía `GET me/profile`, que ya trae el nombre del dojo, el instructor y las disciplinas. Las ediciones hechas en el perfil del sensei ("En el tatami", cargo, grado) no llegaban a las demás pantallas hasta recrearlas. El perfil cambia poco durante el día, pero su agenda e ingresos sí cambian de un día a otro.
- **Decisión:**
  - **Una sola fuente: `GET me/profile`.** La app deja de llamar a `GET me`. Se borran `TkdAuthRepository.currentAccount()`, `signedInAccount()`, `TkdAuthApi.me()` y el modelo `TkdInstructorAccount`. `MeDto` se conserva porque es la respuesta de `PATCH me`.
  - **Perfil guardado en el dispositivo (`data/local/TkdProfileCache`).** Guarda el `SenseiProfileDto` en el DataStore de la sesión, cifrado con el mismo `TkdTokenCipher` (Tink) de ADR-017 y excluido del respaldo. Se guarda junto con el correo de la sesión: otra cuenta no lo ve, y se borra al pasar a `SignedOut` por cualquier vía, incluido el cierre forzado del `Authenticator`. **Vigencia:** vale mientras `asOf` sea de hoy en la zona del dojo (el offset del propio `asOf`), comparado con un `Clock` inyectado que solo usa `data/` (ARC-07 sigue vigente para UI y modelos). Solo se guarda una respuesta que ya se pudo mapear.
  - **`TkdSenseiProfileRepository` es el dueño.** `profile(refresh)` lee lo guardado y solo pide el API si no hay un perfil de hoy; un `Mutex` deja salir una sola petición cuando varias pantallas abren a la vez. `observeProfile(): Flow<SenseiProfile?>` emite con cada guardado. `setOnTatami` y `updateDetails` aplican al guardado lo que confirmó el API (la respuesta de `PATCH me`, no lo enviado).
  - **ViewModel base `ui/TkdSenseiViewModel`.** Todo VM cuya pantalla muestra datos del instructor hereda de `TkdSenseiViewModel(profileRepository)`. La base observa el perfil, lo pide si falta y expone `senseiProfile` y `senseiProfileError` como `StateFlow`. La subclase reacciona desde su `init` con `onSenseiProfile { }` / `onSenseiProfileError { }` y reintenta con `loadSenseiProfile()`. La base **no** llama métodos `open` de la subclase: en `init` el estado de la subclase todavía no existe.
  - **Encabezado:** los `UiState` llevan `header: TkdDojoHeader?` (dojo, instructor y disciplina) derivado con `SenseiProfile.header`. La disciplina solo se muestra si el dojo imparte una: "Mixto / MMA" no describe, por ejemplo, un dojo de Karate y BJJ.
  - Las pantallas que **editan** el perfil (`feature/profile/sensei/`) siguen usando el repositorio directamente para sus cambios optimistas.
- **Descartado:**
  - **Seguir con `GET me` + caché en memoria:** dos endpoints con datos que se solapan y que pueden no coincidir; además, la caché en memoria se perdía al cerrar la app.
  - **UseCase (`ObserveSenseiHeader`), aunque se cumple el disparador de §3** (lógica compartida por 2+ VMs): la lógica de negocio (vigencia, dueño, una sola petición) vive en el repositorio y su caché. Lo compartido entre VMs es solo cómo conectarse a ese repositorio desde `viewModelScope`, que no es dominio. Se reconsidera si aparece una regla de negocio sobre el perfil fuera de `data/`.
  - **Composición con un helper inyectado en cada VM:** cada VM repetiría el cableado de `viewModelScope` y los dos `StateFlow`. La herencia se limita a **un nivel** y a esta base.
  - **Room:** un solo registro por cuenta y sin consultas; no se cumple el disparador de persistencia offline (§3).
  - **Disciplina `Mixed` cuando hay varias:** la etiqueta "Mixto / MMA" es engañosa.
- **Consecuencias:**
  - Al abrir la app hay como máximo una llamada a `GET me/profile` por día y cuenta. Si la app queda abierta pasada la medianoche, se muestra el perfil del día anterior hasta la siguiente carga.
  - `TkdAuthRepository` vuelve a ser solo autenticación y sesión.
  - Los tests de VM pasan `FakeTkdSenseiProfileRepository`, cuyo `observeProfile()` simula lo guardado. El comportamiento del guardado se prueba en `NetworkTkdSenseiProfileCacheTest` con `MockWebServer` y un `Clock` de prueba.
  - Regla nueva ARC-15 y fitness function FF-17.
- **Estado:** ✅ Aceptado e implementado (2026-11-16).

### ADR-022: Lista de alumnos guardada en el dispositivo

- **Contexto:** la lista de alumnos pedía `GET students` cada vez que se abría y otra vez al cambiar entre Activos, En pausa y Bajas. Mientras llegaba, la pantalla mostraba un esqueleto, y sin conexión no mostraba nada. La lista cambia poco durante el día y el contrato ya permite pedir los tres estados en una sola llamada, con `membership.status` en cada alumno.
- **Decisión:**
  - **Una sola lista completa.** `TkdStudentsRepository.refreshRoster()` pide `GET students?status=ACTIVE&status=PAUSED&status=INACTIVE`. `Student.membership` sale de `membership.status`; si no viene, el alumno es activo. El estado, la búsqueda, el cinturón y el orden se filtran en el ViewModel. `roster(status)` se conserva sin guardar nada para los formularios de eventos, que necesitan los activos del momento.
  - **Guardado en el dispositivo (`data/local/TkdStudentsCache`).** Igual que ADR-021: guarda el `StudentListDto` en el DataStore de la sesión, cifrado con `TkdTokenCipher`, junto con el correo de la sesión. Otra cuenta no lo ve, y se borra al pasar a `SignedOut`, también al arrancar ya sin sesión. Solo se guarda una respuesta que ya se pudo mapear.
  - **Guardado y refresco.** `TkdStudentsViewModel` muestra al instante `savedRoster()` y en segundo plano llama a `refreshRoster()`, que reemplaza la lista y lo guardado. No hay vigencia: lo guardado siempre se refresca al abrir. Si el refresco falla y ya hay lista, se conserva sin error. Sin lista, el error se muestra con reintento. Un alta o una edición recargan con `onRetry()`, como antes.
- **Descartado:**
  - **Solo consultar si no hay nada guardado:** los cambios hechos desde otro dispositivo nunca llegarían.
  - **Vigencia de un día (como ADR-021):** la lista cambia con altas y bajas a lo largo del día, y el refresco en segundo plano no bloquea la pantalla.
  - **Room:** un solo registro por cuenta, sin consultas; el filtrado ocurre en memoria (§3). Se reconsidera si la lista crece a miles de alumnos o si un flujo debe editar sin conexión.
  - **Un archivo de DataStore propio:** se usa el de la sesión, ya excluido del respaldo. Se reconsidera si el tamaño de la lista afecta la lectura de la sesión.
- **Consecuencias:**
  - Al abrir Alumnos hay una llamada a `GET students` (antes, una por cada cambio de estado). Cambiar de estado ya no espera al API.
  - Sin conexión se ve la última lista guardada, sin aviso de que puede estar desactualizada.
  - Pruebas: `NetworkTkdStudentsRepositoryTest` cubre la consulta con los tres estados, el guardado, otra cuenta y el cierre de sesión; `TkdStudentsViewModelTest`, lo guardado con y sin conexión y el filtro de estado local.
- **Estado:** ✅ Aceptado e implementado (2026-11-17).

### ADR-023: Fichas de alta sin terminar guardadas en el dispositivo

- **Contexto:** "Guardar como borrador" en la ficha de nuevo alumno solo guardaba el formulario en una variable en memoria. Se perdía al cerrar la app y no había dónde retomarlo. El API de borradores (`PUT /student-drafts/{registrationId}`, `GET /student-drafts`) está planeado como P1 en `docs/api/student-registration-api.md` §8, pero no existe.
- **Decisión:**
  - **Guardado en el dispositivo (`data/local/TkdStudentDraftStore`).** Mismo esquema que ADR-021 y ADR-022: lista de `StudentDraftDto` cifrada con `TkdTokenCipher` en el DataStore de la sesión, del correo que la guardó; se borra al cerrar sesión. El DTO es local (enums por nombre, fechas ISO) y no viaja al API. Cada ficha tiene un id local (UUID) y la fecha del dispositivo al guardar, que solo se muestra.
  - **Repositorio.** `TkdStudentRegistrationRepository` agrega `saveDraft(form, draftId)` (devuelve el id; con id, reemplaza), `observeDrafts()`, `draft(id)` y `deleteDraft(id)`.
  - **Retomar.** La ruta de alta acepta `?draftId=`. `TkdStudentRegistrationViewModel` lo lee del `SavedStateHandle`, llena la ficha con lo guardado y la ajusta al setup vigente. Guardar otra vez reemplaza la misma ficha, y un alta correcta la descarta.
  - **Lista de alumnos.** `TkdStudentsViewModel` observa las fichas y, en Activos, muestra la tarjeta "Fichas sin terminar" con "Retomar ficha" y "Descartar" (con confirmación).
- **Descartado:**
  - **Esperar el endpoint:** el botón seguiría prometiendo algo que no pasa.
  - **Reservar el folio (`registrationId`) con la ficha:** vence. Al retomar se pide un setup nuevo.
- **Consecuencias:** las fichas no se ven desde otro teléfono. Cuando exista el API de borradores, el repositorio de red cambia a `PUT`/`GET` sin tocar las pantallas y lo guardado se puede subir una vez. Pruebas en `NetworkTkdStudentRegistrationRepositoryTest`, en los tests de ambos ViewModels y en `TkdStudentsContentTest` (UI).
- **Estado:** ✅ Aceptado e implementado (2026-11-17).

### ADR-024: Reporte de crashes con Firebase Crashlytics

- **Contexto:** para el piloto no había forma de enterarse de un crash en el teléfono de un sensei: Timber solo se activa en debug y `AppLogger` no escribe nada en release. Ya hubo un crash exclusivo de release por R8 en la prueba cerrada (2026-11-18, `proguard-rules.pro`). La fase 3 de la hoja de ruta pide "Crashlytics/analytics con política de PII".
- **Decisión:**
  - **Firebase Crashlytics** (BOM `firebase-bom`, plugins `com.google.gms.google-services` y `com.google.firebase.crashlytics`) vía `gradle/libs.versions.toml`. Sin Analytics.
  - **Configuración fuera del repo.** `app/google-services.json` sigue en `.gitignore`. Si falta, debug compila sin Firebase y cualquier tarea `*Release*` falla con un mensaje claro: no se publica sin reporte de crashes.
  - **Solo release recolecta.** `firebase_crashlytics_collection_enabled` en el manifiesto con el placeholder `crashlyticsEnabled` (`false` en debug, `true` en release). El plugin sube el `mapping.txt` de R8 para leer las trazas.
  - **Errores manejados.** `AppLogger` tiene un `crashReporter` (`core/TkdCrashReporter`) que `KenshoApplication` instala solo en release: `w`/`e` quedan como rastro (`log`), `e(…, throwable)` y `wtf` como no fatales. En debug y en tests es `null`.
  - **PII.** No se llama a `setUserId` ni se manda correo, nombre, teléfono, token ni monto. Los mensajes de `AppLogger` ya cumplen esa regla (su KDoc); un log nuevo con datos personales es bloqueante en revisión.
- **Descartado:**
  - **Sentry:** SDK y cuenta aparte; Crashlytics es gratis y vive en la misma consola de Play/Firebase.
  - **Solo Play Console (ANR y crashes de Android vitals):** no trae errores manejados ni rastro, y llega con retraso.
  - **Versionar `google-services.json`:** no es un secreto, pero ata el repo a un proyecto de Firebase; se mantiene la decisión del `.gitignore`.
- **Consecuencias:** quien genere un release necesita el `google-services.json` del proyecto de Firebase. Los datos de crashes se declaran en el formulario de seguridad de datos de Play ("Diagnóstico de la app / registros de fallos"). Cuando exista CI, el archivo se inyecta como secreto del pipeline.
- **Estado:** ✅ Aceptado e implementado (2026-11-20).

### ADR-025: Eventos de uso del piloto con Firebase Analytics

- **Contexto:** el piloto necesita responder "¿la app se usa?" sin preguntarle a cada dojo. ADR-024 dejó Analytics fuera; Firebase Analytics no tiene costo y ya está el proyecto de Firebase.
- **Decisión:**
  - **Firebase Analytics** (`firebase-analytics` del mismo BOM que Crashlytics). El BOM queda en 34.19.0: la 35.x trae Analytics 24, compilado con Kotlin 2.4, y el proyecto usa Kotlin 2.2.
  - **Cuatro eventos, nada más** (`core/TkdAnalyticsEvent`): `login` (`method=email`), `check_in_completed`, `payment_registered` (`payment_method`) y `student_registered`. Se disparan desde el ViewModel solo cuando el API confirmó la operación (`TkdResult.Success`); un reintento no duplica el evento y una ficha guardada como borrador no cuenta como alta.
  - **Interfaz propia.** Los ViewModels reciben `TkdAnalytics` (`fun interface`) por Hilt; `di/AnalyticsModule` da `FirebaseTkdAnalytics` en release y `TkdAnalytics.None` en debug. Los tests usan `RecordingTkdAnalytics`.
  - **Solo release recolecta** (`firebase_analytics_collection_enabled` con el placeholder `analyticsEnabled`).
  - **Sin publicidad.** Se quitan del manifiesto `com.google.android.gms.permission.AD_ID`, `ACCESS_ADSERVICES_AD_ID` y `ACCESS_ADSERVICES_ATTRIBUTION`, y se apagan `google_analytics_adid_collection_enabled` y `google_analytics_default_allow_ad_personalization_signals`.
  - **PII.** Los eventos miden lo que hace el sensei: nunca nombres, correos, teléfonos, ids de alumnos ni montos; sin `setUserId` ni propiedades de usuario. Un evento o parámetro nuevo se agrega en `TkdAnalyticsEvent` y se revisa contra esta regla.
- **Descartado:**
  - **Llamar a `FirebaseAnalytics` desde los ViewModels:** ataría los tests JVM a Firebase.
  - **Registrar en los repositorios:** el repositorio no sabe si la acción fue del usuario (el alta borra la ficha y reintenta el setup).
  - **Medir pantallas vistas (`screen_view` automático):** ruido para el piloto; se puede activar después.
- **Consecuencias:** el formulario de seguridad de datos de Play declara "Actividad en la app / interacciones" e "Identificadores del dispositivo u otros (ID de instancia de Firebase)", sin uso para publicidad; el aviso de privacidad lo menciona. Los datos no se ven en debug (sin DebugView): se revisan en release o en una prueba interna.
- **Estado:** ✅ Aceptado e implementado (2026-11-20).

### ADR-026: Splash del sistema y animaciones con Lottie

- **Contexto:** el diseño del splash llega de Stitch (proyecto "Kensho Dojo", pantalla "Animated SVG") como un SVG con 8 animaciones CSS y 3 SMIL: degradados, trazo que se dibuja (ensō), sombras y un loop de 4 s. Pasarlo a mano a Compose o a `AnimatedVectorDrawable` es trabajo por cada cambio de diseño, y el mismo archivo sirve a iOS.
- **Decisión:**
  - **`com.airbnb.android:lottie-compose`** vía `gradle/libs.versions.toml` (6.7.1). Solo para animaciones decorativas que vienen del diseño; la UI y sus estados se siguen haciendo en Compose.
  - **Origen versionado.** El JSON se genera con `scripts/lottie/build_lottie.py` a `docs/design/kensho_dojo_lottie.json`, nunca se edita a mano; la copia que use la app va en `res/raw/` (`lottie_tkd_*.json`).
  - **Marcadores:** `intro` se reproduce una vez y `loop` se repite (`LottieClipSpec.Markers`).
  - **Accesibilidad.** Decorativa: `contentDescription = null` con comentario. Se respeta "Quitar animaciones" del sistema (no se pasa `ignoreSystemAnimatorScale = true`).
  - **Colores.** Los del archivo son los de la marca en el diseño; si cambian en `TkdColors`, se regenera el JSON.
  - **Splash del sistema con `androidx.core:core-splashscreen`** (1.2.0): `Theme.Kensho.Starting` (fondo `TkdColors.Primary`, kanji `ic_tkd_splash_icon`) en `MainActivity` con `installSplashScreen()`, sin `setKeepOnScreenCondition`: se quita en el primer cuadro. `TkdSplashScreen` arranca idéntico (kanji de `SplashLogoStart` en el centro, barras del sistema en rojo) y luego sube el kanji y muestra la animación; así no hay salto entre los dos. El kanji del sistema cabe en su círculo de 192dp.
- **Descartado:**
  - **`AnimatedVectorDrawable`:** sin dependencia, pero sin sombras y con la conversión manual en cada cambio; no sirve a iOS.
  - **Compose (`Canvas` + `rememberInfiniteTransition`):** cientos de líneas de geometría por animación, fuera del límite de tamaño de archivo.
  - **Video/GIF:** pesado, no escala con la densidad y no admite el loop por marcadores.
  - **Solo el splash del sistema retenido con `setKeepOnScreenCondition` hasta `app/settings`:** más simple, pero solo admite un ícono estático o un `AnimatedVectorDrawable` de ~1 s; se pierde la animación.
- **Consecuencias:** la librería agrega ~0.5 MB antes de R8 (trae sus reglas de consumidor). El efecto *Drop Shadow* depende del renderizador: si lottie-android no lo dibuja, la animación se ve sin sombras. Una animación nueva entra como JSON generado por script y revisado contra su diseño.
- **Estado:** ✅ Aceptado e implementado (2026-11-23).

### ADR-027: Notificaciones push con Firebase Cloud Messaging

- **Contexto:** hoy la app solo le avisa al sensei cuando la abre. Para el piloto hace falta avisarle de su membresía de Kensho (por vencer, vencida, pagada), de funciones nuevas y de versiones nuevas. En una fase 2 la app se abrirá a tutores, con avisos del dojo y de sus alumnos. El backend lo desarrolla el mismo equipo, y el proyecto de Firebase ya existe (ADR-024/025).
- **Decisión:**
  - **`com.google.firebase:firebase-messaging`** del mismo `firebase-bom` (34.19.0, por la restricción de Kotlin 2.2 de ADR-025) vía `gradle/libs.versions.toml`. El backend envía con la API HTTP v1 de FCM y una cuenta de servicio que nunca entra a ningún repo.
  - **Contrato primero.** `PUT`/`DELETE /me/devices/{installationId}` y el bloque `data` (`PushData`, `PushType`) en `docs/api/kensho-api.yaml`; diseño, tipos, canales y lo que le toca al backend en `docs/api/push-api.md`.
  - **Registro por instalación, no por token.** `installationId` es un UUID de la app que sobrevive al cierre de sesión; el `PUT` es idempotente y reasigna el dispositivo a la cuenta que inició sesión. Al cerrar sesión, `DELETE` antes de `auth/logout`. El rol y el usuario salen del token, así que la fase 2 no cambia el contrato.
  - **Envío por token, no por topics.** Una sola vía para todo (membresía, novedades, versión), filtrable por rol, dojo y versión en el servidor.
  - **`notification` + `data` con `click_action` `dev.treecode.kensho.OPEN_PUSH`.** El sistema muestra la notificación aunque la app esté cerrada; la app solo la pinta en primer plano. `MainActivity` es `singleTop` y filtra esa acción, así que el toque llega por `onCreate` o `onNewIntent` también con la app abierta. `data` trae `v`, `id`, `type` y `url` opcional. Un `type` desconocido abre la pantalla inicial (compatibilidad hacia adelante); `url` solo se abre si es `https` de `treecode.dev`.
  - **Tres canales desde el inicio:** `account` (Cuenta, canal por defecto), `news` (Novedades) y `updates` (Actualizaciones), con nombres en `strings.xml`. El sensei puede silenciar novedades sin perder los avisos de su cuenta.
  - **`APP_UPDATE` solo invita a abrir.** La decisión de actualizar sigue en `GET app/settings` (ADR-026); el push no duplica esa lógica.
  - **Permiso `POST_NOTIFICATIONS` (Android 13+)** pedido una vez después del login, con explicación previa; si se niega, la app funciona igual y no vuelve a pedirlo. El estado viaja en `notificationsEnabled`.
  - **Capas.** `FirebaseMessaging` y el servicio (`TkdMessagingService`, `@AndroidEntryPoint`) solo en `data/` y `di/`; la UI recibe un destino ya resuelto. `installationId` y el último token enviado en DataStore; el token **nunca** en logs (regla 10).
  - **PII.** Título y cuerpo los escribe el servidor sin montos, correos, teléfonos ni nombres de alumnos (se ven en la pantalla de bloqueo). Eventos `push_opened` (`type`) y `push_permission` (`granted`) en `TkdAnalyticsEvent`, con la regla de ADR-025.
- **Descartado:**
  - **Solo `data` (la app arma la notificación):** control total del texto, pero Android retrasa o descarta mensajes de datos con la app cerrada en varios fabricantes.
  - **Topics de FCM para novedades:** la suscripción vive en el teléfono y no se puede filtrar por versión ni revocar desde el servidor al cerrar sesión.
  - **OneSignal u otro proveedor:** otra cuenta y otro SDK con analítica propia; FCM es gratis y ya está el proyecto.
  - **Token de FCM como llave del registro:** cambia al reinstalar o restaurar y deja registros huérfanos.
  - **Notificaciones locales programadas (`WorkManager`) para la membresía:** el vencimiento y el pago los conoce el servidor; además sumaría otra dependencia.
- **Consecuencias:** el backend necesita las tablas `devices` y `notification_log`, el sender v1, la limpieza de tokens inválidos y el cron de membresía (`push-api.md §5`). Sin `google-services.json`, debug compila sin push. El formulario de seguridad de datos de Play agrega el token de FCM como identificador del dispositivo enviado al servidor propio. Pendiente de producto: a dónde lleva la notificación de membresía (`url` de pago o pantalla nueva; hoy no hay pantalla de membresía).
- **Estado:** ✅ Aceptado (2026-11-24). Implementado en la app (`data/push/`, `NetworkTkdPushRepository`, `ui/TkdPushPermissionScreen`; probado en emulador API 33). Pendiente en el API: `me/devices`, sender y cron (`push-api.md §5`).

---

## 5. Reglas no negociables

Bloquean el merge sin excepción documentada:

1. Un feature no importa otro feature (ADR-001).
2. Ningún composable fuera de `Screen` recibe un ViewModel ni un `NavController` (ADR-002, ADR-014).
3. Ningún `Color(0x…)`, `Color.<Nombre>`, `N.dp` o `N.sp` fuera de `ui/theme/` en código nuevo (ADR-004).
4. Ningún texto visible literal (ADR-009).
5. Ningún `!!`, `GlobalScope`, `runBlocking`, `android.util.Log`, `println` en `src/main` (ADR-012).
6. Ningún `LocalDate.now()`/`System.currentTimeMillis()` en `model/` ni en composables (ADR-006).
7. Ningún secreto, keystore, `google-services.json` ni `local.properties` en el repo.
8. Un archivo nuevo no supera 300 líneas de código (ver `03-standards.md` STR-02).
9. Ningún import de `retrofit2`, `okhttp3` ni `data.remote` (DTO) fuera de `data/` y `di/`; ninguna excepción de red llega al ViewModel (ADR-017).
10. Ningún token, cuerpo HTTP ni header `Authorization` en logs; `HttpLoggingInterceptor` solo en debug (ADR-012, ADR-017).

---

## 6. Registro de deuda técnica

### Política

- Prefijo **`TD-KEN-0XX`**; el número no se reutiliza.
- 🔴 bloquea el siguiente release · 🟠 se resuelve en el sprint que toque el área · 🟡/🟢 backlog.
- Cada TD tiene una historia `TD-KEN-0XX-FIX` en `07-user-stories.md` y su medición en `06-compliance-report.md`.
- Un PR que toca un archivo con deuda **no la empeora** ("regla del boy scout": si lo tocas, déjalo igual o mejor).

### Deuda registrada (línea base 2026-10-27)

| ID | Problema | Severidad | Evidencia |
|----|----------|-----------|-----------|
| **TD-KEN-001** | ✅ **Saldada (2026-11-19).** Sin pruebas reales: solo `ExampleUnitTest` y `ExampleInstrumentedTest` (línea base). Al cierre: 22/22 ViewModels con unit test (incluida la base `TkdSenseiViewModel`) y 468 unit tests; `SplashViewModel` se quitó con el splash, que era inalcanzable (V-07 de PR-1). Los tests de `Validate` siguen en TD-KEN-010 | ✅ | `app/src/test`, `app/src/androidTest` |
| **TD-KEN-002** | 65 `contentDescription = null` en 31 archivos, ninguno con comentario que justifique que es decorativo | 🟡 Medio | `grep -rn "contentDescription = null" app/src/main` |
| **TD-KEN-003** | Restos de plantilla: `ui/theme/Color.kt`, `Theme.kt` (colores Purple, color dinámico) y `Type.kt` no reflejan el diseño; `MainActivity` tiene ~30 líneas comentadas, `AppDestinations` duplica `TkdTab`, `StatusBarProtection` sin uso; `material3-adaptive-navigation-suite` declarado dos veces en `app/build.gradle.kts`; drawables de plantilla y la cadena `custom_linear_progress` sin uso; `startDestination` fijo en Overview, por lo que login y registro no son alcanzables | 🟡 Medio | `MainActivity.kt`, `ui/theme/`, `app/build.gradle.kts` |
| **TD-KEN-004** | Literales fuera de tokens: `Color(0xFF059669)` y `Color(0xFF94A3B8)` en `TkdStudentAvatar.kt` (previews), `Color(0xFF059669)` en `TkdFilterChips.kt` (preview), `Color.White` en `MainActivity.kt` (código muerto), `4.dp` en `CustomLinearProgress.kt`; `"%02d".format(...)` sin locale en `TkdPaymentDetailSections.kt:542` y `TkdPaymentsFormat.kt:25` (`asShortDate`) | 🟢 Bajo | FF-03, FF-04 |
| **TD-KEN-005** | Fechas no deterministas: `LocalDate.now()` como default en `TkdPaymentsUiState.today` (el perfil del alumno usa el `asOf` del API desde 2026-11-06); `LocalTime.now()` en `TkdCheckInUiState` y `TkdCheckInViewModel` (detectado por Detekt, 2026-10-28) | 🟡 Medio | FF-07 |
| **TD-KEN-006** | Archivos y funciones grandes: `TkdPaymentsSections.kt` (667 líneas), `TkdPaymentDetailSections.kt` (590), `TkdProfileProgressCards.kt` (352), `TkdSignUpScreen.kt` (310); ~28 composables > 60 líneas (máx. `TkdProfileAttendanceCard`, 162) | 🟡 Medio | FF-09 |
| **TD-KEN-007** | Inconsistencias de estructura/nombres: resumen en `ui/present/` (los demás features están en la raíz), `CustomLinearProgress` sin prefijo `Tkd`, `TkdTab.label` en inglés literal, README raíz desactualizado (`feature/tkd/`, `strings_tkd.xml`) | 🟢 Bajo | — |
| **TD-KEN-008** | Sin CI: Detekt + 15 reglas `Kensho*` activos con baseline desde 2026-10-28 (`./gradlew detekt`, `:lint-rules`), pero faltan ktlint/compose-rules, Android Lint con baseline y `bitbucket-pipelines.yml` | 🟠 Alto | raíz del repo |
| **TD-KEN-009** | Dependencias desalineadas: `core-ktx 1.10.1`, `lifecycle 2.6.1`, `activity-compose 1.8.0` frente a BOM 2025.12 y compileSdk 37; `lifecycle-runtime-compose`/`lifecycle-viewmodel-compose` se usan pero llegan transitivas; `mavenLocal()` en `settings.gradle.kts`; `libs.uikit` declarado sin uso | 🟡 Medio | `gradle/libs.versions.toml` |
| **TD-KEN-010** | `core/Validate.kt`: regex de email permisiva (`^[A-Za-z](.*)@(.+)\.(.+)`), indentación anómala y sin tests | 🟢 Bajo | `core/Validate.kt` |
| **TD-KEN-011** | Idioma mixto en `res/values/strings.xml`: login, registro y pagos en español; resumen, alumnos, perfil, pase de lista, `tkd_cd_*` y barra inferior en inglés (≈130 cadenas) | 🟡 Medio | `strings.xml` líneas 3–158 y 326+ |
| **TD-KEN-012** | ✅ **Saldada (2026-11-19).** Migración a ADR-016/017 (abierta 2026-10-30): 0 defaults `= Fake…()` (FF-15), 8/9 repositorios con red, fotos e imágenes por URL con `TkdImage` + Coil (`TkdPhoto`), encabezado con logo del dojo y foto del instructor de `me/profile` y sin `dojo: DojoHeader` fuera del resumen. Lo pendiente del resumen pasa a TD-KEN-013 | ✅ | FF-15, FF-17; `06-compliance-report.md` (Actualización 2026-11-19) |
| **TD-KEN-013** | Resumen del dojo en parte sin red (abierta 2026-11-19, separada de TD-KEN-012; revisada 2026-11-22): lo visible (encabezado, acciones rápidas, agenda, avisos e ingresos) sale de `me/profile` (ADR-021). Sesión en vivo, métricas, filtro y exámenes siguen en `FakeTkdDashboardRepository` (síncrona), con `TkdDashboardUiState` sin carga/error y 2 `@DrawableRes` de foto (`DojoHeader.profilePhoto`, `attendeePhotos`); solo se pintan con `AUTH_STATIC_MOCK` (apagada en debug y release), que también controla el mock del login. Bloqueada: el contrato no tiene `GET /dojo/overview` (propuesta en `docs/api/dashboard-api.md`). Baja de 🟡 a 🟢 porque ya no llega a los usuarios | 🟢 Bajo | `data/TkdDashboardRepository.kt`, `di/DataModule.kt:52`, `model/TkdDashboardUiState.kt` |

> **Fortalezas (no son deuda):** 0 `!!` · 0 `android.util.Log`/`println` · 0 `GlobalScope`/`runBlocking` · 0 TODO/FIXME · 8/8 pantallas recolectan con `collectAsStateWithLifecycle` · 32/32 modelos de `model/` `@Immutable` · 74 `@Preview` · 278 usos de `stringResource` y ningún `Text("…")` con texto de usuario · separación `Screen`/`Content` en las 8 pantallas.

---

## 7. Fitness functions

Se ejecutan desde la raíz del repo. `M=app/src/main/java/dev/treecode/kensho`. La línea base está en `06-compliance-report.md`; un PR no puede subir ninguna cifra.

```bash
M=app/src/main/java/dev/treecode/kensho

# FF-01: sin logging directo (ADR-012) — esperado 0
grep -rnE "android\.util\.Log|println\(|printStackTrace\(" $M | wc -l

# FF-02: sin !!, GlobalScope ni runBlocking — esperado 0
grep -rnE '!!|GlobalScope|runBlocking' $M | wc -l

# FF-03: colores literales fuera de ui/theme (ADR-004) — línea base 4
#   (Color.Unspecified para iconos multicolor y Color.Transparent en degradados están permitidos)
grep -rnE "\bColor\(0x|\bColor\.(White|Black|Red|Gray|Green|Blue|Yellow|Cyan|Magenta)\b" $M | grep -v /ui/theme/ | wc -l

# FF-04: dp/sp literales fuera de ui/theme (ADR-004) — línea base 2 (1 comentado)
grep -rnE "[0-9]\.(dp|sp)\b" $M | grep -v /ui/theme/ | wc -l

# FF-05: texto literal en Text() (ADR-009) — línea base 1
grep -rnE 'Text\(\s*(text\s*=\s*)?"' $M | wc -l

# FF-06: contentDescription = null sin comentario (ADR-008) — línea base 65
grep -rn "contentDescription = null" $M | grep -v "//" | wc -l

# FF-07: reloj del sistema fuera de data/ (ADR-006) — línea base 5 (3 LocalDate + 2 LocalTime, medido 2026-10-28)
grep -rnE "Local(Date|DateTime|Time)\.now\(|Instant\.now\(|System\.currentTimeMillis\(" $M | grep -v /data/ | wc -l

# FF-08: un feature no importa otro (ADR-001) — esperado 0
for f in login signup students profile checkin payments ui.present; do
  grep -rn "import dev.treecode.kensho\.\(login\|signup\|students\|profile\|checkin\|payments\|ui.present\)\." $M/${f//.//} \
    | grep -v "import dev.treecode.kensho.$f\." ; done | wc -l

# FF-09: archivos > 300 líneas — línea base 4
find $M -name '*.kt' -exec wc -l {} + | awk '$2 != "total" && $1 > 300' | wc -l

# FF-10: ViewModels que leen el estado sin lifecycle — esperado 0
grep -rn "collectAsState()" $M | wc -l

# FF-11: componentes públicos sin preview — línea base 2 (TkdSurface, CustomLinearProgress)
for f in $(grep -rlE "^fun [A-Z]" $M/components); do grep -qE "@Preview|@TkdFontScalePreviews" $f || echo $f; done | wc -l

# FF-12: tests — línea base 2 unit (1 de ejemplo + TkdFontScaleTest) / 1 instrumentado (de ejemplo)
find app/src/test app/src/androidTest -name '*Test.kt' | wc -l
# FF-13: escalado manual de fuente (ADR-015) — esperado 0
grep -rnE "fontScale\s*\*|\*\s*[A-Za-z.]*fontScale" $M | grep -v /ui/theme/TkdFontScale.kt | wc -l

# FF-14: red fuera de data/ y di/ (ADR-017) — esperado 0
grep -rnE "import (retrofit2|okhttp3)\.|import dev\.treecode\.kensho\.data\.remote\." $M | grep -vE "/data/|/di/" | wc -l

# FF-15: defaults a la Fake en constructores (ADR-016) — línea base 12 (2026-10-31; 14 el 2026-10-30), objetivo 0
grep -rnE "Repository = Fake" $M | wc -l

# FF-16: dispatchers literales fuera de di/ (ADR-016) — esperado 0
grep -rnE "Dispatchers\.(IO|Default)" $M | grep -v /di/ | wc -l

# FF-17: perfil del instructor fuera de TkdSenseiViewModel o GET me (ADR-021) — esperado 0 (2026-11-16)
grep -rnE 'signedInAccount|currentAccount\(|@GET\("me"\)|\.observeProfile\(\)|[a-zA-Z]+\.profile\((refresh[^)]*)?\)' $M \
  | grep -vE "/data/|/ui/TkdSenseiViewModel.kt|/feature/profile/sensei/" | wc -l
```

> FF-01…FF-11 y FF-13 ya son reglas de Detekt (`Kensho*`, `08-lint-rules.md §2.1`): `./gradlew detekt` falla con cualquier violación nueva. FF-14…FF-17 están planificadas como reglas `Kensho*` (`08-lint-rules.md §4`) y se miden con grep mientras tanto. Los greps siguen siendo la forma de **medir** la línea base; faltan los checks de CI (TD-KEN-008).

---

## 8. Proceso de revisión y criterios de aprobación

- Todo cambio entra por Pull Request en Bitbucket contra `develop`; `main` solo recibe merges de release.
- El PR usa la plantilla `.bitbucket/PULL_REQUEST_TEMPLATE.md` e incluye capturas del emulador junto a la de Figma cuando hay UI.
- Checklist completo en `05-code-review-guide.md §8`. Resumen de aprobación:
  - ✅ `./gradlew :app:assembleDebug` y `./gradlew :app:testDebugUnitTest` pasan.
  - ✅ Ninguna fitness function empeora respecto a la línea base.
  - ✅ Reglas 🔒 de `03-standards.md` cumplidas o con excepción justificada en el PR.
  - ✅ Si cambió una decisión o una deuda, se propagó al corpus (`/kensho-governance:propagacion`).
- Validación asistida: `/validate-pullrequest <PR_ID|rama>`.

---

## 9. Hoja de ruta de evolución

| Fase | Disparador | Acción | ADR a crear |
|------|------------|--------|-------------|
| **0 — Higiene (ahora)** | — | Resolver TD-KEN-003/004/005/007; Detekt + Android Lint + pipeline de Bitbucket (TD-KEN-008); tests de ViewModels (TD-KEN-001) | — |
| **1 — Datos reales (en curso desde 2026-10-30)** | ✅ Primer endpoint y sesión persistente | Hilt, `Dispatchers` inyectados, repositorio real + *Fake* para tests, manejo de carga/error en `UiState` (`sealed` o campos `isLoading`/`error`). Orden: TD-KEN-009 → piloto `auth` → `payments` → resto (TD-KEN-012 ✅ 2026-11-19) → resumen cuando exista su endpoint (TD-KEN-013) | ✅ ADR-016 DI, ✅ ADR-017 red/sesión |
| **2 — Crecimiento** | >15 pantallas o build >2 min | Separar `:core:designsystem` (tokens + components) y módulos por feature | ADR-018 modularización |
| **3 — Producto** | Primer release a Play Store | Firma, `versionCode` automatizado, Crashlytics/analytics con política de PII (✅ Crashlytics, ADR-024, y eventos de uso, ADR-025, 2026-11-20), R8 verificado | ADR-019 release |
