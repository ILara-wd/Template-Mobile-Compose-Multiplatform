# 📏 STANDARDS — Kensho

> **Versión:** 1.4.0 | **Última actualización:** 2026-11-16 (ARC-15 por ADR-021); 2026-10-30 (ARC-06 y ARC-10…ARC-14, TEST-06, LOG-03, DEP-06 por ADR-016/017; 2026-10-28: A11Y-06, GIT-05)
> Normas **concretas y verificables** derivadas de los ADRs de [`01-governance.md`](01-governance.md). Aplican a todo código nuevo y a todo archivo que se modifique.
> **Stack:** Kotlin 2.2 · Compose (BOM 2025.12) · Material 3 · Navigation Compose · AGP 9.4 · compileSdk 37 · minSdk 26

> **¿Por qué existe este documento?**
> Es lo que se cita en un comentario de PR: *"viola 🔒 `THM-01` de `03-standards.md §3`"*. Sin un catálogo con IDs, cada revisión interpreta los ADRs de forma distinta y las convenciones se diluyen justo cuando el proyecto empieza a crecer.

> 🚦 **Notación:** 🔒 bloquea el PR · ⚠️ advertencia: se permite con justificación escrita en el PR o en un comentario del código.

> **Familias:** `ARC` arquitectura y capas · `CMP` composables · `THM` tokens · `A11Y` accesibilidad · `I18N` textos y formato · `RES` recursos de Figma · `STR` estructura y nombres · `PERF` recomposición · `TEST` pruebas · `LOG` logging · `DEP` dependencias y build · `GIT` control de versiones

---

## Índice

1. [Arquitectura y capas (ARC)](#1-arquitectura-y-capas-arc)
2. [Composables (CMP)](#2-composables-cmp)
3. [Tokens de diseño (THM)](#3-tokens-de-diseño-thm)
4. [Accesibilidad (A11Y)](#4-accesibilidad-a11y)
5. [Textos y formato (I18N)](#5-textos-y-formato-i18n)
6. [Recursos de Figma (RES)](#6-recursos-de-figma-res)
7. [Estructura y nombres (STR)](#7-estructura-y-nombres-str)
8. [Recomposición y rendimiento (PERF)](#8-recomposición-y-rendimiento-perf)
9. [Pruebas (TEST)](#9-pruebas-test)
10. [Logging (LOG)](#10-logging-log)
11. [Dependencias y build (DEP)](#11-dependencias-y-build-dep)
12. [Git y PRs (GIT)](#12-git-y-prs-git)
13. [Tabla de verificación rápida](#13-tabla-de-verificación-rápida)

---

## 1. Arquitectura y capas (ARC)

### ARC-01 — `Screen` stateful, `Content` stateless 🔒 (ADR-002)

Cada pantalla tiene `Tkd<X>Screen` (obtiene el VM y recolecta el estado) y `Tkd<X>Content` (recibe `UiState`, `Actions` y lambdas). Solo `Screen` conoce al ViewModel.

```kotlin
// ✅
@Composable
fun TkdPaymentsScreen(modifier: Modifier = Modifier, actions: TkdPaymentsActions = TkdPaymentsActions(),
                      viewModel: TkdPaymentsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TkdPaymentsContent(state = state, actions = actions, onFilterSelected = viewModel::onFilterSelected, modifier = modifier)
}

// ❌ el ViewModel baja a una sección
@Composable internal fun TkdTransactionFilters(viewModel: TkdPaymentsViewModel) { … }
```

### ARC-02 — ViewModel: estado privado mutable, público inmutable 🔒

`private val _uiState = MutableStateFlow(...)` + `val uiState: StateFlow<…> = _uiState.asStateFlow()`. Un método público por evento; las actualizaciones usan `_uiState.update { it.copy(...) }`. Nada de `var` públicos ni `mutableStateOf` en el ViewModel.

### ARC-03 — Sin Android framework en el ViewModel 🔒

El ViewModel no recibe ni guarda `Context`, `Activity`, `Resources`, `NavController` ni tipos de Compose UI. Los textos se resuelven en la UI (`@StringRes` en el estado si hace falta elegir texto).

### ARC-04 — Navegación solo en `TkdApp` 🔒 (ADR-014)

Las pantallas exponen callbacks en `Tkd<X>Actions`; `TkdApp` decide la ruta. Ningún composable de feature recibe `NavController`. Rutas con argumentos como constantes privadas (`"payment/{transactionId}"`) con `navArgument`.

### ARC-05 — Un feature no importa otro feature 🔒 (ADR-001)

`payments/` no importa de `students/`. Lo compartido se mueve a `components/`, `model/` o a un archivo `*Format.kt` en un paquete común. Verificación: FF-08.

### ARC-06 — Datos detrás de interfaces, inyectados con Hilt 🔒 (ADR-006, ADR-016)

Toda fuente de datos es `interface Tkd<X>Repository` en `data/` con su `FakeTkd<X>Repository` (previews y tests) y, cuando hay API, `NetworkTkd<X>Repository`. El VM la recibe por constructor **sin default** y Hilt la enlaza en `di/DataModule`:

```kotlin
// ✅ (ADR-016)
@HiltViewModel
class TkdPaymentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,                 // argumentos de ruta
    private val repository: TkdPaymentsRepository,
    private val dojoRepository: TkdDashboardRepository,
) : ViewModel()

// ❌ default a la Fake: la app nunca usaría la implementación real
class TkdPaymentsViewModel(repository: TkdPaymentsRepository = FakeTkdPaymentsRepository()) : ViewModel()
```

Transición cerrada (TD-KEN-012 saldada el 2026-11-19): FF-15 = 0, todo ViewModel es `@HiltViewModel` sin default a la *Fake*. Un default nuevo es regresión y bloquea el PR.

### ARC-07 — Tiempo determinista 🔒 (ADR-006)

Prohibido `LocalDate.now()`, `LocalDateTime.now()`, `Instant.now()` y `System.currentTimeMillis()` en `model/`, composables y como default de parámetros de UI. "Hoy" llega del repositorio (`closingDate()`, `asOf`) o de un `Clock` inyectado al VM.

### ARC-08 — Lógica derivada en el modelo o el VM, no en la UI ⚠️

Filtros, totales y conteos son propiedades del `UiState` (`visibleTransactions`, `totalCents`) o se calculan en el VM. La UI solo formatea.

### ARC-09 — Dinero en centavos `Int` 🔒 (ADR-003)

Nunca `Float`/`Double` para montos. Se formatean con `Int.centsAsUsd()`/`asUsd()` de `TkdPaymentsFormat.kt` (o su sucesor común). El JSON del API también viaja en centavos enteros: el mapper no convierte unidades (ADR-017).

### ARC-10 — Contrato de repositorio: `suspend`/`Flow` + `TkdResult` 🔒 (ADR-017)

Las operaciones de un repositorio con red son `suspend` (lecturas puntuales y comandos) o `Flow` (datos que cambian, p. ej. la sesión) y devuelven `TkdResult<T>`; el fallo es un `TkdDataError`, nunca `null` ni una excepción. El repositorio traduce `IOException`, `HttpException` y `SerializationException` y **relanza `CancellationException`**.

```kotlin
// ✅
override suspend fun paymentDetail(transactionId: String): TkdResult<PaymentDetail> = withContext(io) {
    try { TkdResult.Success(api.paymentDetail(transactionId).toModel()) }
    catch (e: CancellationException) { throw e }
    catch (e: Exception) { TkdResult.Failure(e.toDataError()) }
}

// ❌ la excepción llega al VM / el fallo se confunde con "no existe"
override suspend fun paymentDetail(transactionId: String): PaymentDetail? = api.paymentDetail(transactionId).toModel()
```

### ARC-11 — Red y DTO solo en `data/` 🔒 (ADR-017)

`retrofit2`, `okhttp3` y `data/remote/` (DTO, `Tkd<X>Api`, mappers) solo se importan desde `data/` y `di/`. `model/`, `ui/`, `components/` y los features trabajan con modelos de `model/`. Verificación: FF-14.

### ARC-12 — Carga y error en el `UiState`; mensaje del API o de recursos 🔒 (ADR-017, ADR-020, ADR-009)

Toda pantalla con datos de red modela carga y error (`sealed interface Tkd<X>UiState { Loading; Content; Error }` o campos `isLoading`/`error`) y ofrece reintento. El error es un `TkdUiText` armado con `TkdResult.Failure.toUiText(fallback)`: el `message` del cuerpo `Error` del API si vino, y si no el `@StringRes` `tkd_error_*` (o el propio de la pantalla). La UI **nunca** muestra `exception.message` ni cuerpos de error que no sean del API propio.

### ARC-13 — Dispatchers inyectados 🔒 (ADR-016)

Los repositorios reciben `@IoDispatcher`/`@DefaultDispatcher` de `di/DispatchersModule`; prohibido `Dispatchers.IO`/`Dispatchers.Default` literal fuera de `di/`. Los tests pasan un `StandardTestDispatcher`. Verificación: FF-16.

### ARC-14 — Sesión centralizada ⚠️ (ADR-017)

El token solo lo leen `TkdSessionStore` (DataStore + Tink) y los interceptores de OkHttp. `TkdAuthRepository.session` es la única fuente del estado de sesión; `TkdApp` lo observa para volver a `login`. Los catálogos visibles llegan como códigos y se traducen a enums de `model/` con variante `Unknown`; las fotos remotas son `TkdImage.Remote` cargadas con Coil.

### ARC-15 — Datos del instructor desde `TkdSenseiViewModel` 🔒 (ADR-021)

Un VM cuya pantalla muestra datos del instructor (encabezado del dojo, nombre, disciplina, agenda) **hereda de `ui/TkdSenseiViewModel`** y reacciona desde su `init` con `onSenseiProfile { }`. El encabezado va en el `UiState` como `header: TkdDojoHeader?` (`SenseiProfile.header`), y el reintento usa `loadSenseiProfile()`. Prohibido:

- llamar a `GET me` o volver a crear una "cuenta" para el encabezado;
- que el VM llame a `TkdSenseiProfileRepository.profile()`/`observeProfile()` por su cuenta (salvo las pantallas que editan el perfil, en `feature/profile/sensei/`);
- guardar el perfil en otro sitio que no sea `TkdProfileCache` (lo escribe solo `NetworkTkdSenseiProfileRepository`);
- heredar de la base en más de un nivel, o que la base llame métodos `open` de la subclase.

```kotlin
// ✅
@HiltViewModel
class TkdStudentsViewModel @Inject constructor(
    private val repository: TkdStudentsRepository,
    profileRepository: TkdSenseiProfileRepository,
) : TkdSenseiViewModel(profileRepository) {
    init {
        load()
        onSenseiProfile { profile -> _uiState.update { it.copy(header = profile.header) } }
    }
}
```

Verificación: FF-17.

---

## 2. Composables (CMP)

### CMP-01 — `modifier` primer parámetro opcional, aplicado una vez a la raíz 🔒

```kotlin
// ✅ orden: obligatorios → modifier → opcionales → content/slots al final
@Composable
fun TkdPill(
    color: Color,                          // obligatorio
    modifier: Modifier = Modifier,         // primer opcional
    shape: Shape = CircleShape,
    contentPadding: PaddingValues = PaddingValues(horizontal = TkdDimens.Space16, vertical = TkdDimens.Space8),
    content: @Composable RowScope.() -> Unit, // slot al final
) { … }
```

### CMP-02 — Componentes de `components/` sin estado de negocio 🔒 (ADR-005)

Átomos y moléculas reciben valores y emiten callbacks. `remember` solo para estado visual efímero (animaciones, `TextLayoutResult`, `InteractionSource`).

### CMP-03 — Átomos agnósticos del dominio 🔒

`components/atoms/` no importa `model/` ni `data/`. Las moléculas pueden recibir modelos de `model/`; `data/` (las *Fake*) solo dentro de `@Preview`.

### CMP-04 — Secciones de pantalla `internal` o `private` 🔒

Lo que solo usa una pantalla vive en su paquete (`Tkd<X>Sections.kt`) con visibilidad mínima. Si dos features lo necesitan, se promueve a `components/`.

### CMP-05 — `Actions` para callbacks hacia fuera ⚠️

Con más de 2 callbacks de navegación/salida se agrupan en `data class Tkd<X>Actions(val onBack: () -> Unit = {}, …)` con defaults vacíos (facilitan previews).

### CMP-06 — Variantes con estilo o slot, no con booleanos ⚠️

Para una variante visual nueva de un componente existente: un objeto de estilo con defaults (`TkdInputFieldStyle` / `TkdInputFieldDefaults.Note`) o un slot `@Composable () -> Unit`. Más de 2 booleanos de apariencia es señal de diseño equivocado.

### CMP-07 — Reutilizar antes de crear 🔒 (ADR-005)

Antes de un componente nuevo, buscar en `components/` (`grep -rn "fun Tkd" app/src/main/java/dev/treecode/kensho/components`). Si existe uno que cubre el 80 %, se extiende sin romper sus usos actuales (parámetros nuevos al final y con default).

### CMP-08 — Composable ≤ 60 líneas ⚠️

Una función `@Composable` de más de 60 líneas se divide en sub-secciones `private`. (Línea base: ~28 funciones exceden — TD-KEN-006.)

### CMP-09 — Previews 🔒 (ADR-010)

Todo composable público tiene ≥ 1 `@Preview` `private` en el mismo archivo. Cada `Content` tiene una preview por estado relevante con datos de la *Fake*; usar `widthDp = 390` (ancho del prototipo). Componentes con texto: `@TkdFontScalePreviews` (A11Y-06).

---

## 3. Tokens de diseño (THM)

### THM-01 — Sin colores literales fuera de `ui/theme/` 🔒 (ADR-004)

```kotlin
// ❌
color = Color(0xFF059669)
// ✅
color = TkdColors.Success
```

Permitidos: `Color.Unspecified` (iconos multicolor) y `Color.Transparent` (extremo de degradado). Aplica también a previews.

### THM-02 — Sin `.dp` / `.sp` literales fuera de `ui/theme/` 🔒

Medidas desde `TkdDimens` (`Space16`, `IconCircleSmall`); tamaños de texto solo vía `TkdTypography`. Un valor estructural sin token (borde de 1dp) requiere comentario en la línea.

### THM-03 — Tipografía solo desde `TkdTypography` 🔒

No `TextStyle(...)` ad hoc ni `MaterialTheme.typography` en componentes. Para un estilo nuevo se agrega a `TkdTypography` con nombre de rol (`HeadlineRelaxed`, `OverlineTight`).

### THM-04 — Tokens nombrados por rol 🔒

`WarningContainerSoft`, `AvatarPayer`, `MethodTileHeight` ✅ — `Amber50`, `Size56`, `Gray400` ❌. Antes de crear un token, comprobar que no exista uno con el mismo valor y rol.

### THM-05 — Colores de cinturón desde `TkdBeltPalette` 🔒

`BeltColor.palette()` provee `container/content/badgeContainer/badgeContent`. No se decide el color de un cinturón con `when` en la UI.

### THM-06 — Sin tokens huérfanos ⚠️

Un token que nadie usa se elimina en el mismo PR que lo dejó sin uso.

---

## 4. Accesibilidad (A11Y)

### A11Y-01 — `contentDescription` deliberado 🔒 (ADR-008)

```kotlin
// ✅ informativo
Icon(painterResource(R.drawable.ic_tkd_arrow_back), contentDescription = stringResource(R.string.tkd_cd_go_back))
// ✅ decorativo, justificado en la misma línea
Icon(painterResource(R.drawable.ic_tkd_check_small), contentDescription = null) // decorativo: el texto "Al día" ya lo dice
// ❌ null silencioso
Icon(painterResource(R.drawable.ic_tkd_trophy), contentDescription = null)
```

### A11Y-02 — Filas seleccionables completas con `Role` 🔒

Selección y toggles se aplican a la fila entera con `Modifier.selectable(…, role = Role.RadioButton)` / `toggleable(…, role = Role.Switch)`, y el control visual interno no es interactivo (patrón de `TkdSwitch` + `TkdReceiptDeliveryCard`). Grupos con `selectableGroup()`.

### A11Y-03 — Objetivo táctil ≥ 48dp 🔒

Elementos clicables con área táctil mínima de 48dp (`minimumInteractiveComponentSize()` o tamaño propio). Un ícono de 24dp clicable sin padding no pasa.

### A11Y-04 — Agrupar semántica cuando el render se fragmenta ⚠️

Tarjetas con varias líneas que se leen como una unidad (`monto + estado + fecha`) usan `Modifier.semantics(mergeDescendants = true)` o `clearAndSetSemantics` con una descripción completa.

### A11Y-05 — El color no es el único portador de información ⚠️

Estados (pendiente, vencido, pagado) llevan texto o icono además del color.

### A11Y-06 — Texto escalable hasta 200 % 🔒 (ADR-015)

- Contenedores con texto usan `heightIn(min = …)`, no `height(…)`. Nombre + badge van en `FlowRow`; un elemento lateral que crece con la fuente lleva `widthIn(max = …)`.
- Prohibido multiplicar `fontSize` por `fontScale` y congelar texto al 100 % (FF-13).
- Topes solo con `ui/theme/TkdFontScale.kt` (`withFontScale`, `TkdFontScaleLimit`, `shrinkToFit`) y **comentario en la línea** que diga por qué el espacio es fijo.
- Un componente con texto que se cree o modifique usa `@TkdFontScalePreviews` en su preview principal; una pantalla nueva se revisa en emulador a 150 % y 200 % (procedimiento en [`FONT_SCALE_ANALISIS.md §7.4`](FONT_SCALE_ANALISIS.md#74-en-emulador-obligatorio-para-pantallas-nuevas)).

```kotlin
// ❌ alto fijo: a 200 % el texto se recorta
modifier = Modifier.height(TkdDimens.FilterChipHeight)
// ✅
modifier = Modifier.heightIn(min = TkdDimens.FilterChipHeight)

// ✅ tope justificado
// Monto en columna junto al alumno: hasta 150 % para no dejar sin espacio el nombre.
style = TkdTypography.Headline.withFontScale(TkdFontScale.Limited)
```

---

## 5. Textos y formato (I18N)

### I18N-01 — Todo texto visible vía recursos 🔒 (ADR-009)

`stringResource` / `pluralStringResource`. Prohibido `Text("…")` y texto literal en `contentDescription`. Los datos de las *Fake* (nombres, conceptos) son datos, no textos de UI, y pueden ir literales en `data/`.

### I18N-02 — Claves `tkd_<feature>_<descripcion>` 🔒

snake_case, en inglés técnico: `tkd_payment_detail_confirm`; las de accesibilidad con `tkd_cd_<descripcion>` (compartidas) o `tkd_<feature>_cd_<descripcion>` (de una pantalla). Cada bloque de `strings.xml` abre con comentario `<!-- <Pantalla> (Figma: Prototipo-Demo-UiKit, nodo X:Y) -->`.

### I18N-03 — Sin concatenación de textos traducibles 🔒

Usar argumentos de formato (`%1$s`, `%1$d`) y plurales. Para resaltar parte del texto, `buildAnnotatedString` sobre un recurso con argumentos.

### I18N-04 — Locale explícito al formatear ⚠️

Números, fechas y moneda con el `Locale.forLanguageTag("es-MX")` (`spanish`) de los helpers compartidos (`TkdPaymentsFormat.kt`). Prohibido `String.format` sin locale y `"%02d".format(...)` (usa el locale por defecto).

### I18N-05 — Español de México en `res/values/` 🔒 (ADR-009)

Todo texto nuevo en `res/values/strings.xml` va en español con ortografía y acentos correctos (`Cobro registrado`, `Fecha límite`), aunque el prototipo de Figma esté en inglés. El inglés, si se quiere, va en `res/values-en/`. Las cadenas en inglés existentes son deuda (TD-KEN-011): quien toque una pantalla traduce sus cadenas.

---

## 6. Recursos de Figma (RES)

### RES-01 — Nombres de recursos 🔒 (ADR-007)

`ic_tkd_<nombre>.xml` para vectores, `img_tkd_<pantalla>_<nombre>.(jpg|png|webp)` para fotos, `ic_tkd_nav_<tab>` para la barra inferior.

### RES-02 — Vectores generados, no editados 🔒

Todo `ic_tkd_*.xml` nuevo lleva la cabecera `<!-- Generado desde Figma (<archivo>, nodo X:Y). No editar a mano. -->` y conserva `width`/`height`/`viewport` del SVG original.

### RES-03 — Reutilizar solo coincidencias exactas 🔒

Un drawable existente se reutiliza solo si su path data es idéntico al exportado (`diff`). Distinto tamaño, color de fondo o recorte → recurso nuevo con sufijo descriptivo (`_small`, `_tile`).

### RES-04 — Sin URLs de Figma en el código 🔒

Ninguna referencia a `figma.com/api` ni URLs temporales de assets; todo se descarga a `res/`.

### RES-05 — Fotos de prototipo optimizadas ⚠️

Fotos ≤ 200 KB, preferible WebP; tamaño acorde al mayor uso en pantalla (×3 para xxhdpi).

---

## 7. Estructura y nombres (STR)

### STR-01 — Paquetes por feature 🔒 (ADR-001)

```
dev.treecode.kensho/
├── MainActivity.kt
├── ui/TkdApp.kt            # NavHost
├── ui/theme/               # tokens (únicos literales)
├── ui/present/             # resumen (Overview)   ← TD-KEN-007: mover a dashboard/
├── login/ signup/ students/ profile/ checkin/ payments/
├── components/atoms/ components/molecules/
├── model/                  # UiState + modelos @Immutable
├── data/                   # Tkd<X>Repository + Fake<X>Repository
└── core/                   # utilidades puras
```

Un feature nuevo crea su paquete con `Tkd<X>Screen.kt`, `Tkd<X>Sections.kt`, `Tkd<X>ViewModel.kt` y su `model/Tkd<X>UiState.kt`.

### STR-02 — Tamaño de archivo 🔒

≤ 300 líneas para archivos nuevos (bloquea). Un archivo existente que ya excede no puede crecer más de 10 % sin dividirse. (TD-KEN-006.)

### STR-03 — Nombres por tipo 🔒

| Tipo | Patrón | Ejemplo |
|------|--------|---------|
| Composable público | `Tkd<Nombre>` | `TkdPaymentMethodSelector` |
| Pantalla | `Tkd<X>Screen` / `Tkd<X>Content` | `TkdPaymentDetailScreen` |
| ViewModel | `Tkd<X>ViewModel` | `TkdCheckInViewModel` |
| Estado | `Tkd<X>UiState` | `TkdStudentsUiState` |
| Acciones | `Tkd<X>Actions` | `TkdPaymentsActions` |
| Repositorio | `Tkd<X>Repository` / `FakeTkd<X>Repository` | `FakeTkdPaymentsRepository` |
| Preview | `<Componente><Estado>Preview` (`private`) | `TkdSwitchOnPreview` |
| Evento de VM | `on<Algo>` | `onMethodSelected` |

### STR-04 — Un componente público por archivo ⚠️

Excepción: familias pequeñas del mismo concepto (`TkdTiles.kt`, `TkdFilterChips.kt`).

### STR-05 — KDoc en español en lo público ⚠️

Toda clase y composable público lleva una línea de KDoc en español que diga *qué es* (no cómo). Los comentarios explican el *porqué* de lo no obvio.

### STR-06 — Sin código comentado ni muerto 🔒

No se suben bloques comentados ni funciones sin uso; para eso está git. (TD-KEN-003: `MainActivity`.)

---

## 8. Recomposición y rendimiento (PERF)

### PERF-01 — Parámetros estables 🔒 (ADR-003)

Modelos que entran a composables son `@Immutable` con `val`. No pasar `MutableList`, `ArrayList` ni clases con `var`.

### PERF-02 — `collectAsStateWithLifecycle` 🔒

Nunca `collectAsState()` para estado de un ViewModel.

### PERF-03 — `key` en listas perezosas 🔒

`items(list, key = { it.id })` en `LazyColumn`/`LazyRow` con elementos identificables.

### PERF-04 — Sin trabajo pesado en composición ⚠️

Formatters (`DateTimeFormatter`), regex y listas derivadas se crean una vez (top-level `val` o `remember`), no en cada recomposición.

### PERF-05 — Lambdas estables ⚠️

Pasar referencias de método del VM (`viewModel::onConfirm`) en `Screen`; evitar lambdas que capturan estado cambiante en listas grandes.

---

## 9. Pruebas (TEST)

### TEST-01 — Todo ViewModel nuevo o modificado con unit test 🔒 (ADR-013)

En `app/src/test/java/dev/treecode/kensho/<feature>/Tkd<X>ViewModelTest.kt`, construyendo el VM directamente (sin Hilt) con la *Fake* o una implementación de prueba del repositorio. Cubre estado inicial y cada evento; si el VM carga datos (ADR-017), también `Loading → Content` y `Loading → Error` con un `StandardTestDispatcher`.

### TEST-02 — Lógica pura con unit test 🔒

`core/`, helpers de formato y propiedades derivadas de `UiState` (filtros, totales).

### TEST-03 — Flujos interactivos con Compose UI Test ⚠️

Selección, validación de formularios y confirmaciones se prueban sobre `Content` con `createComposeRule()`, localizando nodos por texto de recurso o `testTag`.

### TEST-04 — Nombres `sujeto_condicion_resultado` 🔒

`onConfirm_whenAlreadyConfirmed_ignoresMethodChange()`.

### TEST-05 — Prohibido en tests 🔒

`Thread.sleep`, dependencias de fecha real, orden entre tests, tests de ejemplo (`ExampleUnitTest`) junto a tests reales, llamadas a un servidor real.

### TEST-06 — Red probada con fixtures y `MockWebServer` 🔒 (ADR-017)

Todo mapper `Dto → model` tiene unit test con fixtures JSON de `src/test/resources/fixtures/` (salidas del contrato OpenAPI `docs/api/kensho-api.yaml`). Todo `NetworkTkd<X>Repository` tiene test con `MockWebServer` para 2xx, 401, 404, 409, 5xx, timeout y JSON malformado, verificando el `TkdDataError` resultante.

---

## 10. Logging (LOG)

### LOG-01 — Sin logging directo en `src/main` 🔒 (ADR-012)

Prohibidos `android.util.Log`, `println`, `printStackTrace`. Si se necesita, se introduce primero el wrapper del ADR-012.

### LOG-02 — Sin datos personales en logs 🔒

Nombres de alumnos o tutores, teléfonos, correos y montos no se registran nunca.

### LOG-03 — Tráfico HTTP solo en debug y sin credenciales 🔒 (ADR-017)

`HttpLoggingInterceptor` se agrega solo en debug, con nivel `BASIC` y `redactHeader("Authorization")`. Nunca `BODY` (los cuerpos llevan datos de alumnos y montos) ni tokens en un log.

---

## 11. Dependencias y build (DEP)

### DEP-01 — Versiones en el catálogo 🔒

Toda dependencia y plugin va en `gradle/libs.versions.toml`; nada de versiones en `build.gradle.kts` ni rangos dinámicos (`+`, `latest.release`).

### DEP-02 — Declarar lo que se usa directamente ⚠️

Si el código importa una API (`collectAsStateWithLifecycle`, `viewModel()`), su artefacto se declara aunque llegue transitivo (`lifecycle-runtime-compose`, `lifecycle-viewmodel-compose`). (TD-KEN-009.)

### DEP-03 — Una dependencia nueva requiere justificación 🔒

En el PR: para qué, alternativas y peso. Un framework nuevo requiere ADR (ver `01-governance.md §9`): Hilt (ADR-016) y Retrofit/OkHttp/kotlinx.serialization/DataStore/Tink/Coil/Timber (ADR-017) ya están aprobados; Room, Ktor o cualquier otro cliente/caché necesitan el suyo.

### DEP-04 — Sin `mavenLocal()` en builds compartidas ⚠️

Solo para pruebas locales; no debe quedar en `settings.gradle.kts` del repositorio. (TD-KEN-009.)

### DEP-05 — Sin secretos en el repo 🔒

`local.properties`, keystores, `google-services.json` y claves de API fuera de git (ya en `.gitignore`).

### DEP-06 — Configuración de red por build 🔒 (ADR-017)

`BASE_URL` como `buildConfigField` por buildType/flavor (`dev`, `prod`); nunca una URL literal en código. `network_security_config.xml` sin cleartext salvo el host local del flavor `dev`. Timeouts explícitos en OkHttp; reintentos solo en GET idempotentes, nunca en cobros.

---

## 12. Git y PRs (GIT)

### GIT-01 — Mensaje de commit 🔒

```
<tipo>(<ámbito>): <descripción en imperativo y minúscula>
```

- `tipo` ∈ `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `build`, `revert`.
- `ámbito` opcional = feature o área (`payments`, `theme`, `components`, `docs`).
- Ticket opcional al final entre corchetes: `[KEN-12]`.
- Ejemplos: `feat(payments): agregar detalle de cobro`, `fix(students): corregir filtro por cinturón [KEN-7]`.
- Validado por `.githooks/commit-msg` (instalar con `git config core.hooksPath .githooks`).

### GIT-02 — Ramas 🔒

`develop` es la rama de integración; `main` solo recibe releases. Ramas de trabajo: `feature/<ámbito>-<descripcion>`, `fix/<…>`, `chore/<…>`, `docs/<…>`.

### GIT-03 — PR pequeño y completo ⚠️

Un PR = una pantalla, un componente o una deuda. Incluye capturas (emulador vs. Figma) si hay UI, y la sección de IA de la plantilla si se usó asistencia.

### GIT-04 — Sin artefactos de build ni del IDE 🔒

`build/`, `.idea/`, `.DS_Store`, `.gradle/`, `.kotlin/` fuera del commit.

### GIT-05 — Fragmento de changelog ⚠️

Todo PR que cambia `app/src/main` agrega un fragmento de una línea en `changelog.d/<id>.<tipo>.md` (`id` = `KEN-<n>`, `TD-KEN-0XX` o `+<slug>`; `tipo` = `added|changed|refactored|deprecated|removed|fixed|security`). `CHANGELOG.md` no se edita a mano: se genera con towncrier al publicar. Guía: [`changelog.d/template/TOWNCRIER_README.md`](../../changelog.d/template/TOWNCRIER_README.md).

---

## 13. Tabla de verificación rápida

| Regla | Verificación | FF |
|-------|-------------|----|
| THM-01 | `grep -rnE "\bColor\(0x\|\bColor\.(White\|Black\|…)\b" $M \| grep -v /ui/theme/` | FF-03 |
| THM-02 | `grep -rnE "[0-9]\.(dp\|sp)\b" $M \| grep -v /ui/theme/` | FF-04 |
| I18N-01 | `grep -rnE 'Text\(\s*(text\s*=\s*)?"' $M` | FF-05 |
| A11Y-01 | `grep -rn "contentDescription = null" $M \| grep -v "//"` | FF-06 |
| ARC-07 | `grep -rnE "LocalDate\.now\(\|System\.currentTimeMillis\(" $M \| grep -v /data/` | FF-07 |
| ARC-05 | imports cruzados entre paquetes de feature | FF-08 |
| STR-02 | archivos > 300 líneas | FF-09 |
| PERF-02 | `grep -rn "collectAsState()" $M` | FF-10 |
| CMP-09 | componentes públicos sin `@Preview` | FF-11 |
| LOG-01 | `grep -rnE "android\.util\.Log\|println\(\|printStackTrace\(" $M` | FF-01 |
| A11Y-06 | `grep -rnE "fontScale\s*\*" $M \| grep -v TkdFontScale.kt` | FF-13 |
| ARC-11 | `grep -rnE "import (retrofit2\|okhttp3)\.\|import dev\.treecode\.kensho\.data\.remote\." $M \| grep -vE "/data/\|/di/"` | FF-14 |
| ARC-06 | `grep -rnE "Repository = Fake" $M` (línea base 12, solo baja) | FF-15 |
| ARC-13 | `grep -rnE "Dispatchers\.(IO\|Default)" $M \| grep -v /di/` | FF-16 |
| ARC-15 | `grep -rnE 'signedInAccount\|currentAccount\(\|@GET\("me"\)\|\.observeProfile\(\)\|[a-zA-Z]+\.profile\((refresh[^)]*)?\)' $M \| grep -vE "/data/\|/ui/TkdSenseiViewModel.kt\|/feature/profile/sensei/"` | FF-17 |
| GIT-05 | `git diff --name-only --diff-filter=A origin/develop...HEAD -- changelog.d/` | — |

Las filas FF-01…FF-11 y FF-13 también las verifica `./gradlew detekt` con las reglas `Kensho*` ([`08-lint-rules.md`](08-lint-rules.md)); FF-14…FF-17 son reglas planificadas y por ahora se miden con grep.

Comandos completos en [`01-governance.md §7`](01-governance.md#7-fitness-functions).
