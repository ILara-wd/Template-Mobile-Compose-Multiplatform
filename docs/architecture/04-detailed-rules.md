# 🔬 Reglas Detalladas y Anti-patrones — Kensho

> **Versión:** 1.1.0 | **Última actualización:** 2026-10-30 (AP-18…AP-22 y estructura de VM/repositorio por ADR-016/017)
> **Rol en el corpus:** verificable — cómo se ve **correcto e incorrecto** cada regla de [`03-standards.md`](03-standards.md), con código real de `app/src/main`.
> **Stack:** Kotlin 2.2 · Jetpack Compose · Material 3 · Navigation Compose

> **¿Por qué existe este documento?**
> Una regla como "🔒 ARC-01: `Screen` stateful, `Content` stateless" se entiende al leerla, pero se rompe al escribir código con prisa. Este documento da **la forma exacta** de cada archivo, el **orden** dentro de él y un catálogo de **anti-patrones (AP-XX)** con su versión correcta, para que en un PR baste con escribir *"esto es AP-05, ver `04-detailed-rules.md`"*.

---

## Índice

1. [Checklist de code review](#1-checklist-de-code-review)
2. [Nombrado](#2-nombrado)
3. [Estructura de archivos y orden interno](#3-estructura-de-archivos-y-orden-interno)
4. [Reglas de composición por capa](#4-reglas-de-composición-por-capa)
5. [Tokens y recursos](#5-tokens-y-recursos)
6. [Pruebas](#6-pruebas)
7. [Catálogo de anti-patrones (AP-01 … AP-22)](#7-catálogo-de-anti-patrones)
8. [SOLID con código real de Kensho](#8-solid-con-código-real-de-kensho)

---

## 1. Checklist de code review

Versión corta para pegar en el PR. La guía completa está en [`05-code-review-guide.md`](05-code-review-guide.md).

### Arquitectura
- [ ] La pantalla tiene `Tkd<X>Screen` + `Tkd<X>Content`; solo `Screen` toca el ViewModel (ARC-01)
- [ ] El VM expone `StateFlow` desde un `MutableStateFlow` privado y un método por evento (ARC-02)
- [ ] El VM no recibe `Context`, `Resources` ni `NavController` (ARC-03)
- [ ] La navegación sale por `Tkd<X>Actions` hacia `TkdApp` (ARC-04)
- [ ] Ningún feature importa otro feature (ARC-05, FF-08)
- [ ] Datos detrás de `Tkd<X>Repository` + `Fake`; VM nuevo o migrado con `@HiltViewModel` y sin default `= Fake` (ARC-06, FF-15)
- [ ] Repositorio de red `suspend`/`Flow` + `TkdResult`, relanza `CancellationException` (ARC-10)
- [ ] DTO, Retrofit y OkHttp solo en `data/` y `di/` (ARC-11, FF-14); sin `Dispatchers.IO` literal (ARC-13, FF-16)
- [ ] Pantalla con red: estado de carga, error con `@StringRes` y reintento (ARC-12)
- [ ] Sin `LocalDate.now()`/`System.currentTimeMillis()` en `model/` ni UI (ARC-07, FF-07)
- [ ] Montos en centavos `Int` (ARC-09)

### Composables
- [ ] `modifier: Modifier = Modifier` primer opcional, aplicado a la raíz (CMP-01)
- [ ] Átomos sin `model/`/`data/`; moléculas sin `data/` fuera de previews (CMP-03)
- [ ] Secciones `internal`/`private` (CMP-04)
- [ ] Se buscó un componente existente antes de crear uno (CMP-07)
- [ ] Funciones `@Composable` ≤ 60 líneas o justificadas (CMP-08)
- [ ] `@Preview` `private` por estado en todo composable público (CMP-09, FF-11)

### Tokens y recursos
- [ ] Sin `Color(0x…)`/`Color.White`/`N.dp`/`N.sp` fuera de `ui/theme/` (THM-01/02, FF-03/04)
- [ ] Tipografía desde `TkdTypography` (THM-03); tokens nuevos con nombre de rol (THM-04)
- [ ] Iconos `ic_tkd_*` con cabecera de Figma; reutilización solo exacta (RES-01..03)

### Accesibilidad e i18n
- [ ] `contentDescription` informativo o `null` comentado (A11Y-01, FF-06)
- [ ] Filas seleccionables con `selectable`/`toggleable` + `Role` (A11Y-02)
- [ ] Textos en `strings.xml`, en español, sin concatenar (I18N-01/03/05, FF-05)
- [ ] Formato con locale explícito (I18N-04)

### Calidad
- [ ] Test unitario del ViewModel nuevo o modificado (TEST-01)
- [ ] Archivos nuevos ≤ 300 líneas (STR-02, FF-09)
- [ ] Sin código comentado (STR-06); sin logs (LOG-01, FF-01); sin `!!` (FF-02)
- [ ] Dependencias nuevas en el catálogo y justificadas (DEP-01, DEP-03)

---

## 2. Nombrado

### 2.1 Reglas absolutas

| Elemento | Convención | ✅ | ❌ |
|----------|-----------|----|----|
| Composable público | `Tkd` + PascalCase | `TkdPaymentMethodSelector` | `PaymentMethodSelector`, `CustomLinearProgress` (TD-KEN-007) |
| Sección de pantalla | `Tkd` + PascalCase, `internal` | `internal fun TkdChargeCard` | `fun chargeCard` |
| Subcomponente privado | PascalCase sin prefijo | `private fun LoginForm` | `private fun TkdLoginFormInternal` |
| Evento del VM | `on` + verbo | `onMethodSelected`, `onConfirm` | `selectMethod`, `confirmClicked` |
| Callback en `Actions` | `on` + intención | `onCharge`, `onBack` | `chargeButtonClick` |
| Estado booleano | adjetivo/participio | `confirmed`, `sendReceipt`, `registerOnline` | `isConfirmedFlag` |
| Token de color | rol | `WarningContainerSoft` | `Amber50` |
| Token de medida | rol | `AvatarPayer`, `MethodTileHeight` | `Size56`, `Dp112` |
| Preview | `<Nombre><Estado>Preview`, `private` | `TkdSwitchOnPreview` | `Preview1` |

### 2.2 Sufijos por tipo

| Sufijo | Tipo | Archivo |
|--------|------|---------|
| `Screen` | composable stateful | `<feature>/Tkd<X>Screen.kt` |
| `Content` | composable stateless de pantalla | mismo archivo que `Screen` |
| `Actions` | `data class` de callbacks hacia fuera | mismo archivo que `Screen` |
| `Sections` | archivo de secciones `internal` | `<feature>/Tkd<X>Sections.kt` |
| `ViewModel` | `ViewModel` | `<feature>/Tkd<X>ViewModel.kt` |
| `UiState` | estado de pantalla | `model/Tkd<X>UiState.kt` |
| `Repository` / `Fake…Repository` | fuente de datos | `data/Tkd<X>Repository.kt` |
| `Style` / `Defaults` | variantes visuales | junto al componente (`TkdInputFieldStyle`, `TkdInputFieldDefaults`) |
| `Format` | helpers de formato | `<feature>/Tkd<X>Format.kt` |

### 2.3 Cadenas en `strings.xml`

| Uso | Patrón | Ejemplo |
|-----|--------|---------|
| Texto de pantalla | `tkd_<feature>_<desc>` | `tkd_payment_detail_send_receipt` |
| Accesibilidad compartida | `tkd_cd_<desc>` | `tkd_cd_go_back`, `tkd_cd_dojo_logo` |
| Accesibilidad de una pantalla | `tkd_<feature>_cd_<desc>` | `tkd_login_cd_show_password` |
| No traducible | `translatable="false"` | `tkd_signup_brand_glyph` |

Cada bloque abre con `<!-- <Pantalla> (Figma: Prototipo-Demo-UiKit, nodo X:Y) -->`.

---

## 3. Estructura de archivos y orden interno

### 3.1 `Tkd<X>Screen.kt`

Orden fijo (referencia: `payments/TkdPaymentDetailScreen.kt`):

```kotlin
// 1. Actions
/** Acciones del detalle de cobro que se delegan al llamador. */
data class TkdPaymentDetailActions(
    val onBack: () -> Unit = {},
)

// 2. Screen (stateful)
@Composable
fun TkdPaymentDetailScreen(
    transactionId: String,
    modifier: Modifier = Modifier,
    actions: TkdPaymentDetailActions = TkdPaymentDetailActions(),
    viewModel: TkdPaymentDetailViewModel = viewModel(key = transactionId) { TkdPaymentDetailViewModel(transactionId) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TkdPaymentDetailContent(state = state, actions = actions, onConfirm = viewModel::onConfirm, /* … */ modifier = modifier)
}

// 3. Content (stateless): Scaffold + TkdTopBar + secciones
@Composable
fun TkdPaymentDetailContent(state: TkdPaymentDetailUiState, actions: TkdPaymentDetailActions, /* eventos */, modifier: Modifier = Modifier) { … }

// 4. Datos de preview (private) y 5. previews por estado (private)
private fun previewState(confirmed: Boolean = false): TkdPaymentDetailUiState { … }
@Preview(widthDp = 390, heightDp = 1900) @Composable private fun TkdPaymentDetailContentPreview() { … }
```

### 3.2 `Tkd<X>Sections.kt`

1. Secciones `internal` en el orden en que aparecen en pantalla.
2. Subcomponentes `private` de cada sección justo debajo de ella.
3. Helpers `private` (mapeos `enum → @StringRes/@DrawableRes`).
4. Previews `private`.

Si supera 300 líneas, dividir por bloque visual (`TkdPaymentsCashFlow.kt`, `TkdPaymentsTransactions.kt`) — ver TD-KEN-006.

### 3.3 `Tkd<X>ViewModel.kt`

Forma objetivo (ADR-016/017):

```kotlin
/** ViewModel del detalle de cobro: carga el detalle, método de pago, nota de control y confirmación. */
@HiltViewModel
class TkdPaymentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,                                 // 1. argumentos de navegación
    private val repository: TkdPaymentsRepository,                      // 2. repositorios inyectados, sin default
) : ViewModel() {
    private val transactionId: String = checkNotNull(savedStateHandle["transactionId"])
    private val _uiState = MutableStateFlow<TkdPaymentDetailUiState>(TkdPaymentDetailUiState.Loading) // 3. estado privado
    val uiState: StateFlow<TkdPaymentDetailUiState> = _uiState.asStateFlow()                          // 4. estado público

    init { load() }

    fun onRetry() = load()                                              // 5. eventos, en orden de pantalla

    private fun load() = viewModelScope.launch {
        _uiState.value = TkdPaymentDetailUiState.Loading
        _uiState.value = when (val result = repository.paymentDetail(transactionId)) {
            is TkdResult.Success -> TkdPaymentDetailUiState.Content(result.data)
            is TkdResult.Failure -> TkdPaymentDetailUiState.Error(result.error.messageRes())
        }
    }
}
```

Forma anterior a Hilt (TD-KEN-012, saldada el 2026-11-19): ya no queda en el código; se conserva como ❌ para reconocerla en un PR:

```kotlin
/** ViewModel del detalle de cobro: método de pago, nota de control, envío del recibo y confirmación. */
class TkdPaymentDetailViewModel(
    transactionId: String,                                              // 1. argumentos de navegación
    repository: TkdPaymentsRepository = FakeTkdPaymentsRepository(),    // 2. repositorios con default Fake
    dojoRepository: TkdDashboardRepository = FakeTkdDashboardRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(/* estado inicial */)       // 3. estado privado
    val uiState: StateFlow<TkdPaymentDetailUiState> = _uiState.asStateFlow() // 4. estado público

    fun onMethodSelected(method: PaymentMethod) = _uiState.update { if (it.confirmed) it else it.copy(method = method) }
    fun onConfirm() = _uiState.update { it.copy(confirmed = true) }     // 5. eventos, en orden de pantalla
}
```

### 3.4 `model/Tkd<X>UiState.kt`

1. `@Immutable data class Tkd<X>UiState` con defaults (permite `Tkd<X>UiState()` vacío).
2. Propiedades derivadas como `get()` (`visibleTransactions`, `totalCents`).
3. Modelos y `enum`/`sealed` que solo usa esa pantalla.
4. KDoc en los campos cuyo significado no es obvio (unidades, "centavos de dólar", "fecha de corte").

### 3.5 `data/Tkd<X>Repository.kt`

`interface` arriba con KDoc por método; `class FakeTkd<X>Repository : Tkd<X>Repository` abajo, con los datos del prototipo y su fecha de referencia fija. Con red (ADR-017), la *Fake* acepta un error configurable para previews y tests de estado de error:

```kotlin
class FakeTkdPaymentsRepository(private val failWith: TkdDataError? = null) : TkdPaymentsRepository {
    override suspend fun paymentDetail(transactionId: String) =
        failWith?.let { TkdResult.Failure(it) } ?: TkdResult.Success(/* datos del prototipo */)
}
```

### 3.5b `data/NetworkTkd<X>Repository.kt` y `data/remote/`

| Archivo | Contenido |
|---------|-----------|
| `data/NetworkTkd<X>Repository.kt` | `class NetworkTkd<X>Repository @Inject constructor(api, @IoDispatcher io)`; traduce errores a `TkdDataError` (ARC-10) |
| `data/remote/api/Tkd<X>Api.kt` | interfaz Retrofit con funciones `suspend` |
| `data/remote/dto/<X>Dto.kt` | `@Serializable` espejo exacto del JSON; nombres con `@SerialName` |
| `data/remote/mapper/<X>Mappers.kt` | `fun <X>Dto.toModel(): <X>`; único sitio que conoce DTO y modelo; códigos desconocidos → `Unknown` |
| `di/DataModule.kt` | `@Binds` interfaz → implementación (`Network…` o, hasta migrar, `Fake…`) |

### 3.6 Límites

| Métrica | ⚠️ | 🔒 |
|---------|----|----|
| Líneas por archivo nuevo | 250 | 300 (STR-02) |
| Líneas por función `@Composable` | 60 (CMP-08) | — |
| Parámetros de un composable | 8 | — (agrupar en `Actions` o `Style`) |
| Booleanos de apariencia | 2 (CMP-06) | — |

---

## 4. Reglas de composición por capa

| Capa | Puede recibir | No puede recibir | Puede importar |
|------|---------------|------------------|----------------|
| Átomo (`components/atoms`) | primitivos, `Color`, `Painter`, slots | modelos de dominio | `ui/theme` |
| Molécula (`components/molecules`) | lo anterior + modelos de `model/` | `UiState` de pantalla, VM | `ui/theme`, `atoms`, `model`; `data` solo en `@Preview` |
| Sección (`<feature>/…Sections.kt`) | modelos y partes del `UiState` | VM, `NavController` | todo lo anterior |
| `Content` | `UiState`, `Actions`, lambdas | VM, `NavController` | todo lo anterior |
| `Screen` | argumentos de ruta, `Actions` | `NavController` | su VM (`hiltViewModel()`) |
| ViewModel | repositorios, `SavedStateHandle` (inyectados) | DTO, `Tkd<X>Api`, `Context` | `data` (interfaces), `model` |
| `data/remote` | — | — | Retrofit, OkHttp, kotlinx.serialization, `model` |
| `TkdApp` | `NavController` | VMs directos | todos los `Screen` |

Verificación rápida:

```bash
M=app/src/main/java/dev/treecode/kensho
grep -rn "import dev.treecode.kensho\.\(model\|data\)" $M/components/atoms          # esperado: 0
grep -rn "ViewModel\|NavController" $M/components                                  # esperado: 0
grep -rln "NavController" $M | grep -v ui/TkdApp.kt                                # esperado: 0
```

---

## 5. Tokens y recursos

### 5.1 Agregar un token

1. Buscar si existe uno con el mismo rol: `grep -n "val " app/src/main/java/dev/treecode/kensho/ui/theme/TkdColors.kt`.
2. Si no existe, agregarlo en el grupo correcto con nombre de rol y comentario si el rol no es obvio.
3. Usarlo en el mismo PR (un token sin uso es THM-06).

### 5.2 Literales permitidos fuera de `ui/theme/`

| Literal | Permitido | Motivo |
|---------|:---------:|--------|
| `Color.Unspecified` | ✅ | `tint` de iconos multicolor (respeta los colores del vector) |
| `Color.Transparent` | ✅ | extremo de degradados |
| `0.dp` | ❌ | usar `TkdDimens.Space0` |
| `1f`, `0.5f` en `weight`/`alpha` | ✅ | proporciones, no medidas |
| `Modifier.border(1.dp, …)` | ⚠️ | solo con comentario; preferir token |

### 5.3 Recursos de Figma

```
res/drawable/ic_tkd_<nombre>.xml                 ← vector, cabecera "Generado desde Figma"
res/drawable/img_tkd_<pantalla>_<nombre>.jpg     ← foto
```

Comprobar coincidencia exacta antes de reutilizar:

```bash
diff <(grep -o 'pathData="[^"]*"' res/drawable/ic_tkd_shield_check.xml) <(grep -o 'pathData="[^"]*"' /tmp/nuevo.xml)
```

---

## 6. Pruebas

### 6.1 Cobertura esperada

| Tipo de código | Test obligatorio | Regla |
|----------------|------------------|-------|
| ViewModel nuevo/modificado | Unit: estado inicial + cada evento | TEST-01 |
| `core/`, `*Format.kt`, propiedades derivadas | Unit | TEST-02 |
| Pantalla con selección/validación/confirmación | UI sobre `Content` | TEST-03 ⚠️ |
| Átomo/molécula puramente visual | Preview | CMP-09 |
| Mapper `Dto → model` | Unit con fixtures JSON | TEST-06 |
| `NetworkTkd<X>Repository` | `MockWebServer`: 2xx, 401, 404, 409, 5xx, timeout, JSON malformado | TEST-06 |

### 6.2 Estructura de un test de ViewModel

```kotlin
package dev.treecode.kensho.payments

class TkdPaymentsViewModelTest {

    private val repository = FakeTkdPaymentsRepository()

    @Test
    fun init_usesRepositoryClosingDateAsToday() {
        val viewModel = TkdPaymentsViewModel(repository = repository)
        assertEquals(repository.closingDate(), viewModel.uiState.value.today)
    }

    @Test
    fun onFilterSelected_pending_showsOnlyUnpaidTransactions() {
        val viewModel = TkdPaymentsViewModel(repository = repository)
        viewModel.onFilterSelected(TransactionFilter.Pending)
        assertTrue(viewModel.uiState.value.visibleTransactions.none { it.status is PaymentStatus.Paid })
    }
}
```

### 6.3 Estructura de un test de UI

```kotlin
class TkdPaymentDetailContentTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun confirm_click_invokesOnConfirm() {
        var confirmed = false
        composeRule.setContent {
            TkdPaymentDetailContent(state = /* estado con detalle de "kenji" */, actions = TkdPaymentDetailActions(),
                onMethodSelected = {}, onNoteChange = {}, onSendReceiptChange = {}, onConfirm = { confirmed = true })
        }
        composeRule.onNodeWithText(/* texto del botón desde recursos */).performScrollTo().performClick()
        assertTrue(confirmed)
    }
}
```

### 6.4 Herramientas aprobadas

| Herramienta | Uso | Estado |
|-------------|-----|--------|
| JUnit4 | unit | ✅ en el catálogo |
| Compose UI Test (`ui-test-junit4`) | UI | ✅ en el catálogo |
| `kotlinx-coroutines-test` | VMs con corrutinas | ➕ agregar con el piloto de red (ADR-017) |
| `mockwebserver` (OkHttp) | repositorios de red | ➕ agregar con el piloto de red (ADR-017) |
| `hilt-android-testing` | `androidTest` con `@TestInstallIn` | ➕ agregar con Hilt (ADR-016) |
| Mockito / MockK | — | ❌ no mientras las *Fake* basten (ADR-013) |

---

## 7. Catálogo de anti-patrones

### AP-01: ViewModel dentro de una sección o componente
**Viola:** ARC-01, CMP-02.
```kotlin
// ❌
@Composable internal fun TkdTransactionFilters(viewModel: TkdPaymentsViewModel = viewModel()) { … }
// ✅ la sección recibe valor y callback; Screen conecta con viewModel::onFilterSelected
@Composable internal fun TkdTransactionFilters(
    selected: TransactionFilter,
    recentCount: Int,
    pendingCount: Int,
    onSelected: (TransactionFilter) -> Unit,
    modifier: Modifier = Modifier,
) { … }
```

### AP-02: Estado mutable expuesto por el ViewModel
**Viola:** ARC-02.
```kotlin
// ❌
val uiState = MutableStateFlow(TkdPaymentsUiState())
var selectedFilter by mutableStateOf(TransactionFilter.All)
// ✅
private val _uiState = MutableStateFlow(TkdPaymentsUiState())
val uiState: StateFlow<TkdPaymentsUiState> = _uiState.asStateFlow()
```

### AP-03: Navegación desde un feature
**Viola:** ARC-04.
```kotlin
// ❌
@Composable fun TkdPaymentsContent(state: TkdPaymentsUiState, navController: NavController) {
    TkdTransactionCard(onCharge = { navController.navigate("payment/${it.id}") })
}
// ✅ el feature emite la intención; TkdApp decide la ruta
TkdPaymentsScreen(actions = TkdPaymentsActions(onCharge = { navController.navigate("payment/${it.id}") }))
```

### AP-04: Import entre features
**Viola:** ARC-05.
```kotlin
// ❌ en students/
import dev.treecode.kensho.payments.centsAsUsd
// ✅ mover el helper a un paquete común (p. ej. core/ o ui/format/) e importarlo desde ahí en ambos features
```

### AP-05: Reloj del sistema escondido en un default
**Viola:** ARC-07. **Deuda actual:** TD-KEN-005.
```kotlin
// ❌ model/TkdPaymentsUiState.kt
val today: LocalDate = LocalDate.now(),
// ✅ el estado no decide la fecha; la recibe del VM, que la obtiene del repositorio
val today: LocalDate,
// en el VM:
today = repository.closingDate(),
```

### AP-06: Dinero en coma flotante
**Viola:** ARC-09.
```kotlin
// ❌
data class ChargeLine(val label: String, val amount: Double)
// ✅
data class ChargeLine(val label: String, val amountCents: Int)
Text(text = line.amountCents.centsAsUsd())
```

### AP-07: Color o medida literal
**Viola:** THM-01, THM-02. **Deuda actual:** TD-KEN-004.
```kotlin
// ❌ components/molecules/TkdStudentAvatar.kt (preview)
indicatorColor = Color(0xFF059669),
// ❌ components/atoms/CustomLinearProgress.kt
trackHeight: Dp = 4.dp,
// ✅
indicatorColor = TkdColors.Success,
trackHeight: Dp = TkdDimens.ProgressTrack,   // token nuevo con nombre de rol
```

### AP-08: Explosión de booleanos de apariencia
**Viola:** CMP-06.
```kotlin
// ❌
fun TkdInputField(…, isNote: Boolean = false, hideBorder: Boolean = false, smallText: Boolean = false, greyLabel: Boolean = false)
// ✅ estilo con defaults (patrón real)
fun TkdInputField(…, style: TkdInputFieldStyle = TkdInputFieldDefaults.Elevated)
TkdInputField(…, style = TkdInputFieldDefaults.Note)
```

### AP-09: Componente nuevo que duplica uno existente
**Viola:** CMP-07.
Señal: un `Row` con fondo redondeado, padding y texto pequeño que ya es `TkdPill`; un `Box` con `CircleShape` que ya es `TkdCircle`. Antes de crear: `grep -rn "^fun Tkd" app/src/main/java/dev/treecode/kensho/components`.

### AP-10: `contentDescription = null` silencioso
**Viola:** A11Y-01. **Deuda actual:** TD-KEN-002 (65).
```kotlin
// ❌
Icon(painter = painterResource(R.drawable.ic_tkd_trophy), contentDescription = null)
// ✅ decorativo y justificado en la misma línea
Icon(painter = painterResource(R.drawable.ic_tkd_trophy), contentDescription = null) // decorativo: el título del evento ya lo nombra
// ✅ informativo
Icon(painter = painterResource(R.drawable.ic_tkd_arrow_back), contentDescription = stringResource(R.string.tkd_cd_go_back))
```

### AP-11: Control pequeño como único objetivo táctil
**Viola:** A11Y-02, A11Y-03.
```kotlin
// ❌ solo el RadioButton de 20dp es clicable
Row { RadioButton(selected = selected, onClick = onClick); Text(label) }
// ✅ patrón real de TkdPaymentMethodSelector: grupo + baldosa completa seleccionable
Column(modifier = Modifier.selectableGroup()) {
    MethodTile(option = option, selected = selected, enabled = enabled, onClick = onClick)
}
// dentro de MethodTile:
Column(modifier = modifier.selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)) { … }
```

### AP-12: Texto literal o concatenado
**Viola:** I18N-01, I18N-03.
```kotlin
// ❌
Text("Cobrar " + amount)
// ✅
Text(stringResource(R.string.tkd_payment_detail_confirm, amount))
```

### AP-13: Formato sin locale explícito
**Viola:** I18N-04. **Deuda actual:** TD-KEN-004.
```kotlin
// ❌ payments/TkdPaymentDetailSections.kt y TkdPaymentsFormat.kt (asShortDate)
Text(text = "%02d".format(record.date.dayOfMonth))
// ✅ formatter con el locale compartido `spanish` de TkdPaymentsFormat.kt
private val dayFormatter = DateTimeFormatter.ofPattern("dd", spanish)
Text(text = record.date.format(dayFormatter))
```

### AP-14: Formatter o regex creados en cada recomposición
**Viola:** PERF-04.
```kotlin
// ❌
@Composable fun TkdDueDate(date: LocalDate) {
    val formatter = DateTimeFormatter.ofPattern("d MMM", spanish)
    Text(date.format(formatter))
}
// ✅ val de nivel superior (como monthFormatter en TkdPaymentsFormat.kt)
private val dueFormatter = DateTimeFormatter.ofPattern("d MMM", spanish)
```

### AP-15: Lista perezosa sin `key`
**Viola:** PERF-03.
```kotlin
// ❌
items(state.visibleTransactions) { TkdTransactionCard(it, …) }
// ✅ patrón real de TkdPaymentsScreen
items(state.visibleTransactions, key = { it.id }) { transaction -> … }
```

### AP-16: Código comentado o muerto
**Viola:** STR-06. **Deuda actual:** TD-KEN-003.
```kotlin
// ❌ MainActivity.kt
//NavigationSuiteScaffold(
//    navigationSuiteItems = { … }
// ✅ borrarlo; queda en el historial de git. Si es trabajo pendiente, abrir una historia.
```

### AP-17: Texto que no escala o que escala dos veces
**Viola:** A11Y-06 (ADR-015). **Detalle:** [`FONT_SCALE_ANALISIS.md`](FONT_SCALE_ANALISIS.md).
```kotlin
// ❌ alto fijo con texto: a 200 % el chip recorta la etiqueta
TkdPill(color = …, modifier = Modifier.height(TkdDimens.FilterChipHeight)) { Text(label, style = TkdTypography.Label) }
// ❌ nombre y badge en Row: el badge aplasta el nombre ("K…")
Row { Text(name, Modifier.weight(1f, fill = false), maxLines = 1); TkdTextBadge(beltLabel, …) }
// ❌ escalado manual: 2.0 × 2.0 = 400 %
style = TkdTypography.Title.copy(fontSize = TkdTypography.Title.fontSize * LocalDensity.current.fontScale)

// ✅ layout primero
TkdPill(color = …, modifier = Modifier.heightIn(min = TkdDimens.FilterChipHeight)) { … }
FlowRow { Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis); TkdTextBadge(beltLabel, …) }
// ✅ tope solo en espacio fijo, con comentario
// Cinco pestañas de ancho fijo: la etiqueta crece hasta 150 % y se encoge si no cabe en una línea.
val labelStyle = TkdTypography.Overline.withFontScale(TkdFontScale.Limited)
Text(text = label, style = labelStyle, maxLines = 1, autoSize = labelStyle.shrinkToFit())
```

### AP-18: Default a la *Fake* en un VM nuevo
**Viola:** ARC-06 (ADR-016). **FF:** FF-15.
```kotlin
// ❌ la app nunca usa la implementación real; el argumento de ruta llega por constructor
class TkdBeltExamViewModel(examId: String, repository: TkdBeltExamRepository = FakeTkdBeltExamRepository()) : ViewModel()
// ✅
@HiltViewModel
class TkdBeltExamViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TkdBeltExamRepository,
) : ViewModel()
```

### AP-19: Excepción de red que llega al ViewModel
**Viola:** ARC-10 (ADR-017).
```kotlin
// ❌ IOException/HttpException cruzan al VM; además se traga la cancelación
override suspend fun students() = runCatching { api.students().map { it.toModel() } }.getOrNull()
// ✅ error tipado y cancelación respetada
override suspend fun students(): TkdResult<List<Student>> = withContext(io) {
    try { TkdResult.Success(api.students().map { it.toModel() }) }
    catch (e: CancellationException) { throw e }
    catch (e: Exception) { TkdResult.Failure(e.toDataError()) }
}
```

### AP-20: DTO o Retrofit fuera de `data/`
**Viola:** ARC-11 (ADR-017). **FF:** FF-14.
```kotlin
// ❌ el UiState depende del JSON del servidor
@Immutable data class TkdStudentsUiState(val students: List<StudentDto> = emptyList())
// ✅ el mapper traduce en data/remote/mapper; la UI solo conoce model/
@Immutable data class TkdStudentsUiState(val students: List<Student> = emptyList())
```

### AP-21: Mensaje del servidor en pantalla
**Viola:** ARC-12, I18N-01 (ADR-009, ADR-017, ADR-020).
```kotlin
// ❌ texto en inglés o técnico ("HTTP 500 Internal Server Error") y sin respaldo local
is TkdResult.Failure -> it.copy(errorText = throwable.message)
// ✅ message del API (ADR-020) o, si no vino, un recurso; la UI lo resuelve con asString()
is TkdResult.Failure -> TkdStudentsUiState.Error(result.toUiText())   // Plain("…") o Res(R.string.tkd_error_no_connection…)
```

### AP-22: Dispatcher literal o logging HTTP completo
**Viola:** ARC-13, LOG-03 (ADR-016, ADR-017). **FF:** FF-16.
```kotlin
// ❌ no se puede controlar en tests; BODY expone datos de alumnos y el token
withContext(Dispatchers.IO) { … }
HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
// ✅
class NetworkTkdStudentsRepository @Inject constructor(private val api: TkdStudentsApi, @IoDispatcher private val io: CoroutineDispatcher)
if (BuildConfig.DEBUG) addInterceptor(HttpLoggingInterceptor().apply { level = Level.BASIC; redactHeader("Authorization") })
```

### AP-23: Cada pantalla pide su propia "cuenta" para el encabezado
**Viola:** ARC-15 (ADR-021). **FF:** FF-17.
```kotlin
// ❌ el mismo bloque copiado en 7 VMs; una llamada a GET me por pantalla y nada se entera de lo editado en el perfil
class TkdStudentsViewModel @Inject constructor(…, private val authRepository: TkdAuthRepository) : ViewModel() {
    private fun loadAccount() = viewModelScope.launch {
        val result = authRepository.signedInAccount()
        if (result is TkdResult.Success) _uiState.update { it.copy(account = result.data) }
    }
}
// ✅ perfil guardado y compartido; se pide al API solo si no hay uno de hoy y los cambios llegan solos
class TkdStudentsViewModel @Inject constructor(…, profileRepository: TkdSenseiProfileRepository) :
    TkdSenseiViewModel(profileRepository) {
    init { onSenseiProfile { profile -> _uiState.update { it.copy(header = profile.header) } } }
}
```

---

## 8. SOLID con código real de Kensho

### 8.1 S — Responsabilidad única

Cada archivo de `payments/` tiene un solo motivo para cambiar:

| Cambia si… | Archivo |
|------------|---------|
| cambia el diseño de una tarjeta | `TkdPaymentDetailSections.kt` |
| cambia el orden de la pantalla o la barra superior | `TkdPaymentDetailScreen.kt` |
| cambia una regla (no editar tras confirmar) | `TkdPaymentDetailViewModel.kt` |
| cambia la forma de los datos | `model/TkdPaymentDetailUiState.kt` |
| cambia la fuente de datos | `data/TkdPaymentsRepository.kt` |
| cambia el formato de moneda/fecha | `TkdPaymentsFormat.kt` |

❌ Violación: un `TkdPaymentDetailSections.kt` de 590 líneas mezcla ocho tarjetas (TD-KEN-006). Dividirlo por bloque no cambia comportamiento y mejora S.

### 8.2 O — Abierto/cerrado

`TkdInputFieldStyle` se extendió para el detalle de cobro sin modificar ningún uso previo:

```kotlin
data class TkdInputFieldStyle(
    val labelStyle: TextStyle,
    val containerColor: Color,
    val placeholderColor: Color,
    val minHeight: Dp,
    val elevation: Dp,
    val labelColor: Color = TkdColors.OnSurface,            // nuevo, con default
    val textStyle: TextStyle = TkdTypography.BodyLarge,      // nuevo, con default
    val borderColor: Color? = TkdColors.OutlineVariant,      // nuevo, con default
)
```

Regla: un parámetro nuevo en un componente compartido **siempre al final y con default** que preserve el aspecto actual.

### 8.3 L — Sustitución de Liskov

`TkdPaymentDetailViewModel` funciona igual con cualquier `TkdPaymentsRepository`. La *Fake* respeta el contrato completo: `paymentDetail(id)` devuelve `null` para ids sin detalle, y la UI lo maneja mostrando `tkd_payment_detail_not_found`. Una implementación que lanzara excepción en lugar de `null` rompería a sus consumidores: eso es una violación de L.

### 8.4 I — Segregación de interfaces

Seis repositorios pequeños en lugar de uno general:

```
TkdAuthRepository · TkdDashboardRepository · TkdStudentsRepository
TkdStudentProfileRepository (fun interface) · TkdCheckInRepository · TkdPaymentsRepository
```

`TkdPaymentsViewModel` depende solo de `TkdPaymentsRepository` y `TkdDashboardRepository` (para la cabecera del dojo); no ve alumnos ni asistencia.

### 8.5 D — Inversión de dependencias

```kotlin
@HiltViewModel
class TkdPaymentsViewModel @Inject constructor(
    private val repository: TkdPaymentsRepository,
    private val dojoRepository: TkdDashboardRepository,
) : ViewModel()
```

El VM depende de la abstracción y Hilt decide la implementación (`di/DataModule`, ADR-016): `NetworkTkdPaymentsRepository` en la app, `FakeTkdPaymentsRepository` en el test unitario (construcción directa) y en `androidTest` (`@TestInstallIn`). El default `= FakeTkdPaymentsRepository()` que aún tienen los VMs no migrados (TD-KEN-012) desaparece sin cambiar el cuerpo del VM.

---

## Referencias

- Reglas: [`03-standards.md`](03-standards.md) · Decisiones y deuda: [`01-governance.md`](01-governance.md) · Revisión: [`05-code-review-guide.md`](05-code-review-guide.md)
- Validación asistida de PRs: `/validate-pullrequest` · Trabajo de código: `/kensho-governance` o el agente `kensho-android-dev` · Historias: `/tech-story-architect`
