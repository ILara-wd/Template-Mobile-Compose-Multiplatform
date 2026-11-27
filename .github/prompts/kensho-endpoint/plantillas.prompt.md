<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde .claude/commands/kensho-endpoint/plantillas.md. NO EDITAR A MANO. -->
---
mode: agent
description: Plantillas para implementar un endpoint (Kensho)
---

# Plantillas para implementar un endpoint (Kensho)

Esqueletos para `/kensho-endpoint`. El ejemplo usa `GET /payments/{transactionId}` y `POST /payments/{transactionId}/confirm`; sustituye `Payments`/`payment` por el feature real. Paquete base: `dev.treecode.kensho`. Antes de copiar, compara con el piloto `auth`: si el código real cambió, **manda el código real**.

Entrada de ejemplo:

```json
// GET /payments/{transactionId} → 200
{ "id": "tx-1", "studentName": "Ana Ruiz", "amountCents": 42000, "status": "PENDING",
  "dueDate": "2026-11-10", "method": null }

// POST /payments/{transactionId}/confirm  request
{ "method": "CASH", "paidAt": "2026-11-05T10:00:00Z" }
// → 200 mismo cuerpo que el GET
```

## Fixture — `app/src/test/resources/fixtures/payments_detail.json`

El JSON de response tal cual, más **un campo extra** que la app no conoce (prueba `ignoreUnknownKeys`). Una fixture por variante relevante (`payments_detail_paid.json`) y `error_conflict.json` si el endpoint devuelve 409 con `{code, message}`.

## DTO — `data/remote/dto/TkdPaymentsDto.kt`

```kotlin
package dev.treecode.kensho.data.remote.dto

import kotlinx.serialization.Serializable

/** Cuerpos de los endpoints `payments` según `docs/api/kensho-api.yaml`. */
@Serializable
data class PaymentDetailDto(
    val id: String,
    val studentName: String,
    val amountCents: Int,
    /** Código de estado: `PENDING`, `PAID` u `OVERDUE`. */
    val status: String,
    /** ISO-8601 `yyyy-MM-dd`. */
    val dueDate: String,
    val method: String? = null,
)

@Serializable
data class ConfirmPaymentRequestDto(val method: String, val paidAt: String)
```

## Api — `data/remote/api/TkdPaymentsApi.kt`

```kotlin
package dev.treecode.kensho.data.remote.api

import dev.treecode.kensho.data.remote.dto.ConfirmPaymentRequestDto
import dev.treecode.kensho.data.remote.dto.PaymentDetailDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Cobros del dojo (`docs/api/kensho-api.yaml`, tag `payments`). */
interface TkdPaymentsApi {
    @GET("payments/{transactionId}")
    suspend fun paymentDetail(@Path("transactionId") transactionId: String): PaymentDetailDto

    @POST("payments/{transactionId}/confirm")
    suspend fun confirmPayment(
        @Path("transactionId") transactionId: String,
        @Body body: ConfirmPaymentRequestDto,
    ): PaymentDetailDto
}
```

Paths **sin `/` inicial** (se resuelven contra `BASE_URL`, que termina en `/`).

## Mapper — `data/remote/mapper/TkdPaymentsMapper.kt`

```kotlin
package dev.treecode.kensho.data.remote.mapper

internal fun PaymentDetailDto.toModel() = PaymentDetail(
    id = id,
    studentName = studentName,
    amountCents = amountCents,
    status = status.toPaymentStatus(),
    dueDate = LocalDate.parse(dueDate),
    method = method?.toPaymentMethod(),
)

internal fun TkdPaymentConfirmation.toDto() = ConfirmPaymentRequestDto(
    method = method.toCode(),
    paidAt = paidAt.toString(), // Instant → ISO-8601 UTC
)

internal fun String.toPaymentStatus(): PaymentStatus = when (this) {
    "PENDING" -> PaymentStatus.Pending
    "PAID" -> PaymentStatus.Paid
    "OVERDUE" -> PaymentStatus.Overdue
    else -> PaymentStatus.Unknown
}

internal fun PaymentMethod.toCode(): String = when (this) {
    PaymentMethod.Cash -> "CASH"
    PaymentMethod.Card -> "CARD"
}
```

- Fecha mal formada: `LocalDate.parse` lanza `DateTimeParseException`; dentro de `tkdApiCall` termina en `TkdDataError.Unknown`. No la atrapes en el mapper.
- Mapeo `enum → código` exhaustivo con `when` (sin `else`): al agregar un valor, el compilador obliga a mapearlo.

## Repositorio — `data/TkdPaymentsRepository.kt` + `data/NetworkTkdPaymentsRepository.kt`

```kotlin
interface TkdPaymentsRepository {
    suspend fun paymentDetail(transactionId: String): TkdResult<PaymentDetail>
    suspend fun confirmPayment(transactionId: String, confirmation: TkdPaymentConfirmation): TkdResult<PaymentDetail>
}
```

```kotlin
package dev.treecode.kensho.data

/** [TkdPaymentsRepository] contra los endpoints `payments` del API. */
@Singleton
class NetworkTkdPaymentsRepository @Inject constructor(
    private val api: TkdPaymentsApi,
    @param:IoDispatcher private val io: CoroutineDispatcher,
) : TkdPaymentsRepository {

    override suspend fun paymentDetail(transactionId: String): TkdResult<PaymentDetail> =
        tkdApiCall(io) { api.paymentDetail(transactionId).toModel() }

    override suspend fun confirmPayment(
        transactionId: String,
        confirmation: TkdPaymentConfirmation,
    ): TkdResult<PaymentDetail> = tkdApiCall(io) { api.confirmPayment(transactionId, confirmation.toDto()).toModel() }
}
```

## DI

```kotlin
// di/NetworkModule.kt (companion object)
@Provides
@Singleton
fun providePaymentsApi(retrofit: Retrofit): TkdPaymentsApi = retrofit.create(TkdPaymentsApi::class.java)

// di/DataModule.kt
@Binds
abstract fun bindPaymentsRepository(impl: NetworkTkdPaymentsRepository): TkdPaymentsRepository
```

## Test del mapper — `app/src/test/.../data/remote/mapper/TkdPaymentsMapperTest.kt`

```kotlin
class TkdPaymentsMapperTest {

    @Test
    fun toModel_fixtureDeDetalle_convierteFechaEstadoYNulos() {
        val dto = testJson.decodeFromString(PaymentDetailDto.serializer(), fixture("payments_detail.json"))

        assertEquals(
            PaymentDetail("tx-1", "Ana Ruiz", 42_000, PaymentStatus.Pending, LocalDate.of(2026, 11, 10), method = null),
            dto.toModel(),
        )
    }

    @Test
    fun toPaymentStatus_codigoDesconocido_devuelveUnknown() {
        assertEquals(PaymentStatus.Unknown, "REFUNDED".toPaymentStatus())
    }
}
```

## Test del repositorio — `app/src/test/.../data/NetworkTkdPaymentsRepositoryTest.kt`

```kotlin
class NetworkTkdPaymentsRepositoryTest {

    private val server = MockWebServer().apply { start() }

    @After
    fun tearDown() = server.shutdown()

    private fun TestScope.repository(client: OkHttpClient = OkHttpClient()) = NetworkTkdPaymentsRepository(
        api = server.testRetrofit(client).create(TkdPaymentsApi::class.java),
        io = StandardTestDispatcher(testScheduler),
    )

    @Test
    fun paymentDetail_respuesta200_devuelveModeloYPideElPath() = runTest {
        server.enqueue(jsonResponse("payments_detail.json"))

        val result = repository().paymentDetail("tx-1")

        assertEquals(PaymentStatus.Pending, (result as TkdResult.Success).data.status)
        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/payments/tx-1", request.path)
    }

    @Test
    fun confirmPayment_enviaCuerpoDelContrato() = runTest { /* takeRequest().body.readUtf8() contiene "method":"CASH" */ }

    // Un test por fila: 401 → Unauthorized · 404 → NotFound · 409 (+ serverMessage) → Conflict
    // 500 → Server · timeout (SocketPolicy.NO_RESPONSE + client con readTimeout corto) → Timeout
    // cuerpo "{" → Unknown
    @Test
    fun paymentDetail_respuesta404_devuelveNotFound() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        assertEquals(TkdResult.Failure(TkdDataError.NotFound), repository().paymentDetail("tx-1"))
    }
}
```

## ViewModel (si aplica)

```kotlin
@HiltViewModel
class TkdPaymentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TkdPaymentsRepository,
) : ViewModel() {
    private val transactionId: String = checkNotNull(savedStateHandle["transactionId"])
    private val _uiState = MutableStateFlow(TkdPaymentDetailUiState())
    val uiState: StateFlow<TkdPaymentDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun onRetry() = load()

    private fun load() {
        _uiState.update { it.copy(loadError = null) }
        viewModelScope.launch {
            when (val result = repository.paymentDetail(transactionId)) {
                is TkdResult.Success -> _uiState.update { it.copy(detail = result.data) }
                is TkdResult.Failure -> _uiState.update { it.copy(loadError = result.toUiText()) }
            }
        }
    }
}
```
