## 5.2 Revisiones de Arquitectura y Capas (FF-ARC-XX)

Verifica el contrato MVVM + UDF de Kensho (`03-standards.md §1`, ADR-001/002/003/006/014/016/017): `Screen` stateful / `Content` stateless / `Actions`, ViewModel con `StateFlow` inmutable hacia fuera e inyectado con Hilt, navegación solo en `ui/TkdApp.kt`, datos detrás de interfaces (*Fake* + red) con `TkdResult`, dispatchers inyectados y dinero en centavos. Corre si el cambio toca pantallas, ViewModels, `model/`, `data/`, `di/`, `TkdApp.kt`, `KenshoApplication.kt` o `MainActivity.kt`.

| ID | Regla | Severidad |
|---|---|---|
| FF-ARC-01 | `viewModel()`/`ViewModel` usado fuera de un `Tkd<X>Screen` (ARC-01) | 🔴 |
| FF-ARC-02 | ViewModel expone estado mutable: `MutableStateFlow`/`MutableState`/`var` público, o `mutableStateOf` en el VM (ARC-02) | 🔴 |
| FF-ARC-03 | ViewModel con `Context`/`Activity`/`Resources`/`NavController`/tipos de Compose UI (ARC-03) | 🔴 |
| FF-ARC-04 | `NavController`/`navigate(` fuera de `ui/TkdApp.kt`; pantalla nueva sin `Tkd<X>Actions` cuando tiene callbacks de salida (ARC-04, CMP-05) | 🟠 |
| FF-ARC-05 | Repositorio nuevo en `data/` sin interfaz `Tkd<X>Repository` o sin `FakeTkd<X>Repository`; VM que instancia un repositorio dentro del cuerpo en vez de recibirlo por constructor (ARC-06) | 🟠 |
| FF-ARC-06 | Pantalla nueva sin el par `Screen` + `Content`, o `Screen` que no usa `collectAsStateWithLifecycle` (ARC-01, PERF-02) | 🟠 |
| FF-ARC-07 | Monto de dinero como `Float`/`Double` en `model/` o `data/` (ARC-09) | 🔴 |
| FF-ARC-08 | Lógica de filtrado/totales en la UI en vez de en el `UiState` o el VM (ARC-08) | 🟡 señal |
| FF-ARC-09 | Línea nueva con default `= FakeTkd…()` en un constructor, o VM nuevo sin `@HiltViewModel` + `@Inject constructor` (ARC-06, ADR-016) | 🟠 nuevo/migrado · ℹ️ existente sin tocar |
| FF-ARC-10 | Repositorio de red (`NetworkTkd*`) con funciones no `suspend`/`Flow`, que devuelve `T?` en vez de `TkdResult`, o `catch` genérico/`runCatching` sin relanzar `CancellationException` (ARC-10) | 🟠 |
| FF-ARC-11 | `Dispatchers.IO`/`Dispatchers.Default` fuera de `di/` (ARC-13) | 🟠 |
| FF-ARC-12 | `.message` de una excepción en un VM/`UiState`; pantalla cuyo VM carga datos de red sin estado `Loading`/`Error` (ARC-12) | 🟡 señal |
| FF-ARC-13 | Datos del instructor fuera de `TkdSenseiViewModel`: `GET me`/`signedInAccount`/`currentAccount`, `profile()`/`observeProfile()` desde un VM, o un `UiState` con `header: TkdDojoHeader` cuyo VM no hereda de la base (ARC-15, ADR-021) | 🟠 |

```bash
BASE_BRANCH="$BASE_BRANCH" bash <<'EOF'
source .claude/scripts/validate-pullrequest-lib.sh
ADDED=$(added_main)

# FF-ARC-01 — viewModel() fuera de *Screen.kt (TkdApp no debe crear VMs tampoco)
printf '%s\n' "$ADDED" | grep -E '(viewModel\(|: *Tkd[A-Za-z]*ViewModel\b)' \
  | grep -vE "Screen\.kt:|ViewModel\.kt:" | sed 's/^/🔴 FF-ARC-01 ARC-01 /'

# FF-ARC-02 / FF-ARC-03 — sobre los ViewModels del cambio
for f in $PR_VIEWMODELS; do
  added_lines "$f" | grep -E '^[^:]*:[0-9]+:[[:space:]]*(val|var) [a-z][A-Za-z0-9_]*[[:space:]]*(:[[:space:]]*)?(=[[:space:]]*)?Mutable(StateFlow|State|SharedFlow|LiveData)' \
    | sed 's/^/🔴 FF-ARC-02 estado mutable público /'
  added_lines "$f" | grep -E ':[[:space:]]*var [a-z]' | grep -v 'private' | sed 's/^/🔴 FF-ARC-02 var pública /'
  added_lines "$f" | grep -E 'mutableStateOf|mutableStateListOf' | sed 's/^/🔴 FF-ARC-02 estado de Compose en el VM /'
  added_lines "$f" | grep -E 'import android\.(content\.Context|app\.Activity|content\.res\.Resources)|import androidx\.navigation\.|import androidx\.compose\.ui\.|: *(Context|Activity|Resources|NavController)\b' \
    | sed 's/^/🔴 FF-ARC-03 /'
  added_lines "$f" | grep -E '=[[:space:]]*(Fake)?Tkd[A-Za-z]*Repository\(\)' | grep -vE '^[^:]*:[0-9]+:[[:space:]]*[a-z][A-Za-z]*: *Tkd[A-Za-z]*Repository *=' \
    | sed 's/^/🟠 FF-ARC-05 repositorio instanciado en el cuerpo (debe ser parámetro de constructor inyectado) /'
done

# FF-ARC-04 — navegación fuera de TkdApp
printf '%s\n' "$ADDED" | grep -E 'NavController|rememberNavController|\.navigate\(|popBackStack\(' \
  | grep -v "/ui/TkdApp.kt:" | sed 's/^/🟠 FF-ARC-04 ARC-04 /'

# FF-ARC-05 — repositorios nuevos: interfaz + Fake
for f in $(printf '%s\n' $PR_DATA); do
  is_new_file "$f" || continue
  grep -qE '^interface Tkd[A-Za-z]*Repository' "$f" || echo "🟠 FF-ARC-05 $f — archivo nuevo en data/ sin interface Tkd<X>Repository"
  grep -qE '^class FakeTkd[A-Za-z]*Repository' "$f" || echo "🟠 FF-ARC-05 $f — sin FakeTkd<X>Repository"
done

# FF-ARC-06 — pantallas nuevas: par Screen/Content + lifecycle
for f in $(printf '%s\n' $PR_MAIN_NEW | grep 'Screen\.kt$'); do
  base=$(basename "$f" Screen.kt)
  grep -qE "^fun ${base}Content\(" "$f" || echo "🟠 FF-ARC-06 $f — sin ${base}Content stateless"
  grep -q 'collectAsStateWithLifecycle' "$f" || echo "🟠 FF-ARC-06 $f — no usa collectAsStateWithLifecycle"
  grep -qE "^data class ${base}Actions" "$f" || echo "🔎 FF-ARC-04 $f — sin ${base}Actions (¿la pantalla no tiene salidas?)"
done

# FF-ARC-07 — dinero en coma flotante
added_lines "$APP_MAIN/model/*.kt" "$APP_MAIN/data/*.kt" \
  | grep -iE '(amount|price|total|cost|fee|balance|monto|cobro|pago)[A-Za-z]*[[:space:]]*:[[:space:]]*(Float|Double)\b' \
  | sed 's/^/🔴 FF-ARC-07 ARC-09 dinero en Float\/Double /'

# FF-ARC-09 — default a la Fake o VM nuevo sin Hilt (ADR-016)
printf '%s\n' "$ADDED" | grep -E '=[[:space:]]*FakeTkd[A-Za-z]*Repository\(' \
  | sed 's/^/🟠 FF-ARC-09 ARC-06 default a la Fake — inyectar con Hilt /'
for f in $(printf '%s\n' $PR_MAIN_NEW | grep 'ViewModel\.kt$'); do
  grep -q '@HiltViewModel' "$f" && grep -q '@Inject constructor' "$f" || echo "🟠 FF-ARC-09 $f — VM nuevo sin @HiltViewModel/@Inject constructor"
done

# FF-ARC-10 — contrato de los repositorios de red (ADR-017)
for f in $(printf '%s\n' $PR_DATA | grep -E 'NetworkTkd[A-Za-z]*Repository\.kt$'); do
  added_lines "$f" | grep -E 'override fun ' | grep -vE ': *Flow<' | sed 's/^/🟠 FF-ARC-10 ARC-10 función no suspend \/ Flow /'
  added_lines "$f" | grep -E 'override suspend fun [^=]*\): *[A-Za-z<>, ]+\?' | sed 's/^/🟠 FF-ARC-10 ARC-10 devuelve T? en vez de TkdResult /'
  if grep -qE 'catch *\([a-z]+: *(Exception|Throwable)\)|runCatching' "$f" && ! grep -q 'CancellationException' "$f"; then
    echo "🟠 FF-ARC-10 $f — catch genérico/runCatching sin relanzar CancellationException"
  fi
done

# FF-ARC-11 — dispatchers literales (ADR-016)
printf '%s\n' "$ADDED" | grep -vE "^$APP_MAIN/di/" | grep -E 'Dispatchers\.(IO|Default)\b' \
  | sed 's/^/🟠 FF-ARC-11 ARC-13 inyecta @IoDispatcher /'

# FF-ARC-12 — errores visibles (señal)
for f in $PR_VIEWMODELS $PR_MODELS; do
  added_lines "$f" | grep -E '\.(message|localizedMessage)\b' | sed 's/^/🔎 FF-ARC-12 ARC-12 ¿mensaje del servidor en el estado? /'
done
for f in $PR_VIEWMODELS; do
  grep -q 'TkdResult' "$f" && ! grep -qE 'Loading|isLoading' "$f" && echo "🔎 FF-ARC-12 $f — usa TkdResult pero no modela carga"
done

# FF-ARC-13 — perfil del instructor solo vía TkdSenseiViewModel (ADR-021)
printf '%s\n' "$ADDED" | grep -vE "^$APP_MAIN/(data|ui/TkdSenseiViewModel\.kt|feature/profile/sensei)/?" \
  | grep -E 'signedInAccount|currentAccount\(|@GET\("me"\)|\.observeProfile\(\)|[a-zA-Z]+\.profile\((refresh[^)]*)?\)' \
  | sed 's/^/🟠 FF-ARC-13 ARC-15 hereda de TkdSenseiViewModel y usa onSenseiProfile /'
for f in $PR_VIEWMODELS; do
  grep -q 'header = ' "$f" && ! grep -q ') : TkdSenseiViewModel(' "$f" \
    && echo "🟠 FF-ARC-13 $f — llena header sin heredar de TkdSenseiViewModel"
done

# FF-ARC-08 — cálculos de colección en pantallas (señal)
for f in $PR_SCREENS; do
  added_lines "$f" | grep -E '\.(filter|sumOf|count|groupBy|sortedBy|sortedWith)[[:space:]]*\{' \
    | sed 's/^/🔎 FF-ARC-08 ¿lógica derivada en la UI? /'
done
true
EOF
```

**Cómo interpretar:**
- **FF-ARC-01:** el patrón aceptado es `viewModel: Tkd<X>ViewModel = hiltViewModel()` **solo** en la firma de `Tkd<X>Screen` (los VMs aún no migrados conservan `viewModel()` / `viewModel(key = id) { … }`, TD-KEN-012). `TkdApp` pasa argumentos de ruta, no ViewModels.
- **FF-ARC-02:** lo correcto es `private val _uiState = MutableStateFlow(...)` + `val uiState: StateFlow<…> = _uiState.asStateFlow()` y actualizaciones con `_uiState.update { it.copy(...) }`.
- **FF-ARC-05 / FF-ARC-09:** el VM recibe el repositorio por constructor **sin default** (`@HiltViewModel class … @Inject constructor(private val repository: Tkd<X>Repository)`); los argumentos de ruta salen de `SavedStateHandle`. El test lo construye directamente con la *Fake*. Cualquier default `= FakeTkd…()` es 🟠: FF-15 = 0 desde que se saldó TD-KEN-012 (2026-11-19).
- **FF-ARC-10:** forma correcta en `04-detailed-rules.md` AP-19: `withContext(io) { try { Success(…) } catch (e: CancellationException) { throw e } catch (e: Exception) { Failure(e.toDataError()) } }`.
- **FF-ARC-12:** el VM traduce `TkdDataError` a `@StringRes tkd_error_*`; la UI nunca muestra `exception.message` (AP-21). Cada estado (`Loading`/`Content`/`Error`) tiene preview.
- **FF-ARC-13:** forma correcta en `04-detailed-rules.md` AP-23. Las pantallas que **editan** el perfil (`feature/profile/sensei/`) pueden usar el repositorio directamente; el resto lo lee de la base.
- **FF-ARC-08:** formatear (`asUsd()`, `asShortDate()`) en la UI es correcto; filtrar o sumar no: muévelo a una propiedad del `UiState` (`visibleTransactions`, `totalCents`) o al VM.
