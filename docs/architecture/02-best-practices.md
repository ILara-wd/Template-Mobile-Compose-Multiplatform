# 📐 Buenas Prácticas — Kensho

> **Versión:** 1.1.0 | **Última actualización:** 2026-10-30 (§15 datos reales: Hilt y capa de red, ADR-016/017)
> **Rol en el corpus:** marco conceptual — explica el *porqué* de las decisiones de [`01-governance.md`](01-governance.md) y de las reglas de [`03-standards.md`](03-standards.md).
> **Stack:** Kotlin 2.2 · Jetpack Compose (BOM 2025.12) · Material 3 · Navigation Compose · AGP 9.4 · minSdk 26

> **¿Por qué existe este documento?**
> Las reglas sin su razón se cumplen a medias o se discuten en cada PR. Este documento explica **por qué** Kensho separa `Screen` de `Content`, por qué los colores viven solo en `TkdColors`, por qué no se usa `LocalDate.now()` o por qué Hilt y la red llegaron justo cuando llegaron. Está pensado para la incorporación de alguien nuevo y para resolver discusiones de diseño: si entiendes el porqué, puedes aplicar la regla a casos que ningún documento previó.

---

## Cómo leer estos documentos

| Documento | Pregunta que responde |
|-----------|-----------------------|
| `02-best-practices.md` (este) | ¿Por qué lo hacemos así? |
| `01-governance.md` | ¿Qué se decidió (ADR) y qué deuda hay (TD-KEN)? |
| `03-standards.md` | ¿Qué regla aplico hoy (🔒/⚠️)? |
| `04-detailed-rules.md` | ¿Cómo se ve correcto/incorrecto (AP-XX)? |
| `05-code-review-guide.md` | ¿Cómo reviso un PR? |

Si este documento contradice a `01-governance.md`, manda `01-governance.md`.

---

## Índice

1. [Principios rectores](#1-principios-rectores)
2. [Qué es (y qué todavía no es) Kensho](#2-qué-es-y-qué-todavía-no-es-kensho)
3. [MVVM + UDF: `Screen`, `Content` y `Actions`](#3-mvvm--udf-screen-content-y-actions)
4. [Tokens de diseño `Tkd*`](#4-tokens-de-diseño-tkd)
5. [Diseño atómico en `components/`](#5-diseño-atómico-en-components)
6. [Figma como fuente de verdad](#6-figma-como-fuente-de-verdad)
7. [Determinismo: el tiempo es una dependencia](#7-determinismo-el-tiempo-es-una-dependencia)
8. [Accesibilidad y textos](#8-accesibilidad-y-textos)
9. [SOLID aplicado a Kensho](#9-solid-aplicado-a-kensho)
10. [DRY, KISS, YAGNI y Demeter](#10-dry-kiss-yagni-y-demeter)
11. [Recomposición y estabilidad](#11-recomposición-y-estabilidad)
12. [Testing con *Fakes*](#12-testing-con-fakes)
13. [Estructura de paquetes](#13-estructura-de-paquetes)
14. [Uso de IA en el desarrollo](#14-uso-de-ia-en-el-desarrollo)
15. [Datos reales: Hilt y la capa de red](#15-datos-reales-hilt-y-la-capa-de-red)

---

## 1. Principios rectores

Los siete principios de [`01-governance.md §1`](01-governance.md#1-principios-rectores) se resumen en una idea: **el proyecto va empezando y queremos que crecer sea barato**. Cada convención existe porque reduce el coste de un cambio futuro:

| Principio | Cambio futuro que abarata |
|-----------|---------------------------|
| P1 Figma es la fuente de verdad | Actualizar una pantalla cuando cambia el diseño |
| P2 Estado unidireccional | Conectar un backend real sin tocar la UI |
| P3 Tokens, no literales | Ajustar la paleta o agregar modo oscuro |
| P4 Componer antes que crear | Cambiar el aspecto de todas las tarjetas a la vez |
| P5 Determinismo | Escribir tests que no fallen según el día |
| P6 Accesible y en español | Publicar en Play Store sin reescribir textos |
| P7 YAGNI con disparadores | No pagar hoy la complejidad de un problema que aún no existe |

---

## 2. Qué es (y qué todavía no es) Kensho

Kensho es una app Android de **un solo módulo** (`:app`, `dev.treecode.kensho`) para administrar un dojo de Taekwondo: resumen del dojo, alumnos, perfil y temario, pase de lista, cobros y su detalle, inicio de sesión y registro. Las pantallas se implementan desde el prototipo de Figma y se alimentan con repositorios *Fake*.

**Lo que contiene:** pantallas Compose, ViewModels con `StateFlow`, modelos inmutables, repositorios *Fake*, un sistema de diseño propio (`components/` + `ui/theme/`) y navegación en `TkdApp`.

**Lo que todavía no contiene, a propósito** (ver [`01-governance.md §3`](01-governance.md#3-lo-que-kensho-todavía-no-es)): persistencia de datos offline, capa de dominio, módulos separados, CI. Cada una tiene un **disparador** escrito. La razón: sin el problema real, Room o UseCases solo agregan indirección.

**Lo que acaba de cumplir su disparador:** red, sesión persistente y DI (ADR-016, ADR-017, 2026-10-30). Se decidieron cuando apareció el primer API que consumir, no antes; la separación `interface` + `Fake` ya había preparado el terreno, así que el salto es pequeño (ver §15).

> La pregunta correcta no es "¿cuál es la arquitectura más completa?", sino "¿qué es lo mínimo que hace que el siguiente paso sea barato?".

---

## 3. MVVM + UDF: `Screen`, `Content` y `Actions`

### 3.1 El flujo

```
Repository ──▶ ViewModel ──StateFlow<UiState>──▶ Screen ──▶ Content ──▶ Sections ──▶ Tkd*
                   ▲                                          │
                   └──────────── onX(...) ◀───────────────────┘   Actions ──▶ TkdApp (navegación)
```

El estado baja; los eventos suben. Nadie modifica el estado excepto el ViewModel.

### 3.2 Por qué separar `Screen` de `Content`

`TkdPaymentsScreen` hace dos cosas y nada más: obtiene el `TkdPaymentsViewModel` y recolecta su estado con `collectAsStateWithLifecycle()`. `TkdPaymentsContent` recibe el estado ya resuelto. Esa frontera da tres beneficios:

1. **Previews reales.** `Content` se previsualiza con datos de `FakeTkdPaymentsRepository` sin crear un ViewModel.
2. **Tests de UI sin infraestructura.** Compose UI Test monta `Content` con el estado que quiera probar.
3. **Un solo lugar que cambia** con Hilt (ADR-016): solo `Screen` cambia `viewModel()` por `hiltViewModel()`; `Content`, sus previews y sus tests no se enteran.

### 3.3 Por qué `Actions`

`TkdPaymentsActions` agrupa los callbacks que **salen** de la pantalla (`onTabSelected`, `onCharge`, `onDownloadReport`). Con defaults vacíos, una preview no necesita pasar siete lambdas. Y deja claro qué decide la pantalla (eventos al VM: `onFilterSelected`) y qué decide la app (navegación: `onCharge` → `payment/{transactionId}` en `TkdApp`).

### 3.4 El ViewModel

```kotlin
class TkdPaymentsViewModel(
    repository: TkdPaymentsRepository = FakeTkdPaymentsRepository(),
    dojoRepository: TkdDashboardRepository = FakeTkdDashboardRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(TkdPaymentsUiState(/* … */ today = repository.closingDate()))
    val uiState: StateFlow<TkdPaymentsUiState> = _uiState.asStateFlow()

    fun onFilterSelected(filter: TransactionFilter) = _uiState.update { it.copy(filter = filter) }
}
```

- **Estado privado mutable, público inmutable:** nadie fuera del VM puede escribir.
- **Un método por evento:** se lee como la lista de cosas que el usuario puede hacer.
- **Sin `Context`:** el VM no sabe de recursos; la UI resuelve textos (ARC-03).

**Datos del instructor compartidos (ADR-021).** El encabezado del dojo y la agenda salen del mismo perfil en todas las pantallas. Ese perfil se guarda una vez en el dispositivo y lo administra su repositorio (vigencia por `asOf`, una sola petición). Los VM que lo muestran heredan de `TkdSenseiViewModel`, que solo los conecta a ese repositorio. Es la única herencia permitida entre ViewModels, porque lo repetido era el cableado y no reglas de negocio; si aparecen reglas de negocio sobre el perfil, el lugar es una capa de dominio, no una base más grande.

### 3.5 Lo derivado no se guarda dos veces

`TkdPaymentsUiState.visibleTransactions` se calcula a partir de `transactions` y `filter`. Si fuera otro campo, habría que mantener ambos sincronizados y algún día no lo estarían. Lo mismo `Charge.totalCents`. Regla práctica: **si se puede calcular, no se almacena** (ARC-08).

### 3.6 Por qué no MVI (todavía)

Un `sealed interface Intent` con un `reduce` central aporta trazabilidad cuando hay muchos efectos asíncronos. Hoy los eventos son síncronos y pocos; un método por evento es más directo. Con la red (ADR-017) llegan cargas y reintentos, pero se modelan como estados del `UiState` (`Loading`/`Content`/`Error`) y un método `onRetry()`; MVI se reevaluará con un ADR solo si aparecen muchos efectos de un solo disparo que no caben en el estado.

---

## 4. Tokens de diseño `Tkd*`

### 4.1 El problema

El prototipo de Figma tiene su propia paleta (rojo de dojo, verdes de éxito, ámbar de advertencia), una escala de espacios y la tipografía Plus Jakarta Sans. Si cada pantalla escribe `Color(0xFF059669)`, el día que el diseño ajuste el verde habrá que encontrar cada aparición — y alguna se escapará.

### 4.2 La solución

| Token | Archivo | Ejemplos |
|-------|---------|----------|
| Colores | `ui/theme/TkdColors.kt` | `Primary`, `Success`, `WarningContainerSoft`, `OnSurfaceVariant` |
| Medidas | `ui/theme/TkdDimens.kt` | `Space16`, `IconCircleSmall`, `AvatarPayer`, `SwitchWidth` |
| Tipografía | `ui/theme/TkdTypography.kt` | `Body`, `HeadlineRelaxed`, `OverlineTight` |
| Cinturones | `ui/theme/TkdBeltPalette.kt` | `BeltColor.palette()` → `container`, `badgeContainer`… |

`ui/theme/` es **la única casa de literales** (ADR-004). Fuera de ahí, el código habla en roles.

### 4.3 Nombrar por rol, no por valor

`WarningContainerSoft` sobrevive a un cambio de paleta; `Amber50` miente en cuanto el ámbar deja de ser 50. El nombre de rol también evita duplicados: antes de crear `AvatarPayer` (56dp), se pregunta "¿ya existe un token con este rol?" y no "¿ya existe un 56?". Dos tokens pueden tener el mismo valor y roles distintos; eso está bien (THM-04).

### 4.4 ¿Y `MaterialTheme`?

`KenshoTheme` (en `Theme.kt`) es la plantilla de Android Studio con colores Purple y color dinámico; ningún componente `Tkd*` lo usa. Es deuda (TD-KEN-003): se limpiará para que el `MaterialTheme` refleje `TkdColors` en lugar de contradecirlo. Mientras tanto, la regla es usar `Tkd*` y nunca `MaterialTheme.colorScheme` en componentes.

---

## 5. Diseño atómico en `components/`

### 5.1 Tres niveles

| Nivel | Dónde | Sabe del dojo | Ejemplos |
|-------|-------|:-------------:|----------|
| Átomo | `components/atoms/` | ❌ | `TkdPill`, `TkdSwitch`, `TkdAvatar`, `TkdProgressRing` |
| Molécula | `components/molecules/` | ✅ modelos simples | `TkdStudentCard`, `TkdBeltBadge`, `TkdInputField`, `TkdTopBar` |
| Sección | `<feature>/Tkd<X>Sections.kt` (`internal`) | ✅ de su pantalla | `TkdCashFlowCard`, `TkdReceiptDeliveryCard` |

**Por qué los átomos no conocen el dominio:** un `TkdPill` que recibe `BeltColor` solo sirve para cinturones; uno que recibe `color` y un slot sirve para cinturones, estados de pago y filtros. La especialización se hace arriba, componiendo.

**Por qué las secciones son `internal`:** una sección que se usa en una sola pantalla y es pública invita a reutilizarla en otra donde no encaja. Si dos features la necesitan, se promueve a `components/` conscientemente (CMP-04).

### 5.2 Variantes: estilo con defaults, no booleanos

Cuando el detalle de cobro necesitó una caja de nota sin borde, con texto de 14sp y etiqueta gris en mayúsculas, no se agregaron `isNote`, `hideBorder`, `smallText`. Se extendió `TkdInputFieldStyle` con parámetros opcionales (`labelColor`, `textStyle`, `borderColor`) y se añadió `TkdInputFieldDefaults.Note`. Los usos previos (`TkdInputFieldDefaults.Elevated`) no cambiaron. Ese es el patrón (CMP-06, CMP-07).

### 5.3 Controles visuales sin interacción propia

`TkdSwitch` solo dibuja. La fila que lo contiene (`TkdReceiptDeliveryCard`) es `toggleable(role = Role.Switch)`. Así la etiqueta también es objetivo táctil y TalkBack anuncia "Enviar recibo, interruptor, activado" como una unidad (A11Y-02).

---

## 6. Figma como fuente de verdad

- Cada pantalla tiene un nodo en Figma (`Prototipo-Demo-UiKit`), referenciado en el comentario de su bloque de `strings.xml`. Quien llega a una pantalla sabe dónde está su diseño.
- **Los recursos se exportan, no se redibujan.** Un icono redibujado "parecido" acumula diferencias; uno convertido 1:1 del SVG es exacto. Por eso los `ic_tkd_*.xml` llevan la cabecera "Generado desde Figma… No editar a mano." (RES-02).
- **Reutilizar solo coincidencias exactas.** El icono de tarjeta del listado y el del selector de método se ven iguales pero tienen otro tamaño; son recursos distintos (`ic_tkd_credit_card`, `ic_tkd_method_saved_card`). Forzar uno en el lugar del otro distorsiona (RES-03).
- **Se verifica en emulador** contra la captura de Figma antes de cerrar: la preview no muestra insets, fuentes del sistema ni densidad real.

---

## 7. Determinismo: el tiempo es una dependencia

Los datos del prototipo son de 2024. Si `TkdPaymentsUiState.today` fuera `LocalDate.now()`, la cuenta regresiva del torneo diría "−700 días". Peor: un test que calcula "vence en 3 días" pasaría hoy y fallaría mañana.

Por eso el tiempo se trata como cualquier otra dependencia (ADR-006, ARC-07):

- `FakeTkdPaymentsRepository.closingDate()` devuelve `LocalDate.of(2024, 11, 19)` y el VM lo pasa al estado.
- `PaymentDetail.asOf` fija la fecha desde la que se mide el límite de pago.
- `TkdStudentProfileViewModel` ya recibe `today` por parámetro — bien — pero su default es `LocalDate.now()`, igual que `TkdPaymentsUiState.today` y `TkdStudentProfileUiState.today`. Es TD-KEN-005.

Cuando haya backend, "hoy" vendrá de un `java.time.Clock` inyectado; los tests pasarán un `Clock.fixed(...)`.

---

## 8. Accesibilidad y textos

### 8.1 Accesibilidad

Accesibilidad no es un extra para el final: rehacer una tarjeta para que TalkBack la lea bien cuesta más que hacerla bien desde el inicio.

- **`contentDescription` deliberado.** `null` es válido para lo decorativo, pero un `null` sin comentario es indistinguible de un olvido. Hoy hay 65 sin justificar (TD-KEN-002); cada PR que toca un archivo los comenta o los describe.
- **Filas completas como objetivo.** El usuario toca la etiqueta, no el círculo de 20dp del radio.
- **El color no basta.** "Vencido" lleva texto e icono además del rojo.

### 8.2 Textos

- **Base en español de México** (ADR-009). El primer prototipo venía en inglés y por eso resumen, alumnos, perfil y pase de lista siguen en inglés (TD-KEN-011). Quien toca una pantalla traduce sus cadenas.
- **Nunca concatenar.** `"Cobrar " + monto` no se puede traducir ni reordenar; `stringResource(R.string.tkd_…, monto)` sí.
- **Locale explícito.** `TkdPaymentsFormat.kt` formatea moneda y fechas con `es-MX`; `"%02d".format(...)` usa el locale del dispositivo (TD-KEN-004).

---

## 9. SOLID aplicado a Kensho

| Principio | Cómo se manifiesta | Ejemplo real |
|-----------|--------------------|--------------|
| **S** — Responsabilidad única | Un archivo = un rol: Screen, Sections, ViewModel, UiState, Repository | `TkdPaymentDetailScreen.kt` no contiene secciones; están en `TkdPaymentDetailSections.kt` |
| **O** — Abierto/cerrado | Extender componentes con parámetros opcionales y estilos, sin romper usos | `TkdInputFieldStyle` ganó `borderColor: Color? = …` sin tocar `Elevated` |
| **L** — Sustitución de Liskov | Cualquier `TkdPaymentsRepository` sirve al VM; la *Fake* y la futura real son intercambiables | `FakeTkdPaymentsRepository` implementa todo el contrato, incluido `paymentDetail()` devolviendo `null` si no existe |
| **I** — Segregación de interfaces | Un repositorio por área, no un `TkdRepository` gigante | `TkdPaymentsRepository`, `TkdStudentsRepository`, `TkdCheckInRepository` |
| **D** — Inversión de dependencias | El VM depende de la interfaz; Hilt elige la implementación (ADR-016) | `@Inject constructor(private val repository: TkdPaymentsRepository)`; hasta migrar, `= FakeTkdPaymentsRepository()` (TD-KEN-012) |

Detalle con código en [`04-detailed-rules.md §8`](04-detailed-rules.md#8-solid-con-código-real-de-kensho).

---

## 10. DRY, KISS, YAGNI y Demeter

### DRY
Lo compartido entre las dos pantallas de pagos (moneda, fechas) vive en `TkdPaymentsFormat.kt`. Pero **DRY no justifica acoplar features**: si `students/` necesita formatear dinero, el helper se mueve a un paquete común; no se importa de `payments/` (ARC-05).

### KISS
Un `MutableStateFlow` con `update { it.copy(...) }` resuelve el estado de todas las pantallas actuales. No hace falta un store genérico, un reductor ni una librería de estado.

### YAGNI
No se agregan Room, UseCases ni módulos hasta que se cumpla su disparador ([`01-governance.md §9`](01-governance.md#9-hoja-de-ruta-de-evolución)). YAGNI con disparadores no es "nunca": es "cuando el problema exista, con un ADR". Hilt y Retrofit son el ejemplo: esperaron a que existiera un API real y entraron con ADR-016 y ADR-017.

### Ley de Demeter
Las secciones reciben lo que pintan, no el estado completo: `TkdChargeCard(charge = detail.charge, asOf = detail.asOf)` en vez de `TkdChargeCard(state)`. La sección no depende de la forma del `UiState` y se puede previsualizar sola.

---

## 11. Recomposición y estabilidad

Compose omite recomponer un composable cuando sus parámetros son **estables** y no cambiaron. Para aprovecharlo:

- **Modelos `@Immutable` con `val`** (ADR-003). Los 35 modelos de `model/` lo cumplen.
- **`collectAsStateWithLifecycle`** deja de recolectar cuando la pantalla no está visible (PERF-02).
- **`key` en listas perezosas** (`items(state.visibleTransactions, key = { it.id })`): sin clave, filtrar reutiliza filas equivocadas y anima mal (PERF-03).
- **Referencias de método** (`viewModel::onFilterSelected`) son estables entre recomposiciones (PERF-05).
- **Nada caro en composición:** `DateTimeFormatter` como `val` de nivel superior, no dentro del composable (PERF-04).

No se optimiza por adelantado: si aparece un problema de rendimiento, se mide con Layout Inspector (conteo de recomposiciones) antes de cambiar código.

---

## 12. Testing con *Fakes*

### 12.1 Pirámide

| Nivel | Qué | Dónde | Herramienta |
|-------|-----|-------|-------------|
| Unit | ViewModels, `Validate`, helpers de formato, propiedades derivadas | `app/src/test` | JUnit4 (+ `kotlinx-coroutines-test` cuando haya corrutinas) |
| UI | Flujos interactivos sobre `Content` | `app/src/androidTest` | Compose UI Test (`createComposeRule`) |
| Visual | Cada estado de cada pantalla | previews | `@Preview` + verificación en emulador |

### 12.2 Por qué *Fakes* y no mocks

Las *Fake* ya existen (son los datos del prototipo) y cumplen el contrato completo. Un mock verifica llamadas; una *Fake* verifica comportamiento. Para un caso particular se escribe una implementación de prueba de la interfaz en `src/test`.

### 12.3 Ejemplo de test de ViewModel

```kotlin
class TkdPaymentDetailViewModelTest {
    @Test
    fun onMethodSelected_whenConfirmed_keepsPreviousMethod() {
        val viewModel = TkdPaymentDetailViewModel(transactionId = "kenji")
        viewModel.onConfirm()
        viewModel.onMethodSelected(PaymentMethod.Transfer)
        assertEquals(PaymentMethod.Cash, viewModel.uiState.value.method)
    }
}
```

### 12.4 Estado actual

Cada ViewModel tiene su unit test con la *Fake* (TD-KEN-001, saldada el 2026-11-19), los mappers se prueban con fixtures y los repositorios de red con `MockWebServer`. Falta `Validate` (TD-KEN-010) y los flujos completos en Compose UI (TEST-UI-001).

---

## 13. Estructura de paquetes

```
dev.treecode.kensho/
├── MainActivity.kt            # setContent { KenshoTheme { KenshoApp() } }
├── ui/TkdApp.kt               # NavHost, rutas y tabs
├── ui/theme/                  # TkdColors, TkdDimens, TkdTypography, TkdBeltPalette
├── ui/present/                # resumen (Overview) — TD-KEN-007: mover a dashboard/
├── login/ signup/ students/ profile/ checkin/ payments/
├── components/atoms/          # sin dominio
├── components/molecules/      # reutilizables con modelos simples
├── model/                     # UiState + modelos @Immutable
├── data/                      # interfaces + Fakes + Network<X> (ADR-017)
│   ├── remote/                # api · dto · mapper — nunca sale de data/
│   └── local/                 # TkdSessionStore (DataStore + Tink)
├── di/                        # módulos Hilt (ADR-016)
└── core/                      # utilidades puras (Validate)
```

**Por qué por feature y no por capa** (`viewmodels/`, `screens/`): un cambio de pantalla toca un paquete, no cinco. Y el día de modularizar, cada paquete de feature ya es casi un módulo.

---

## 14. Uso de IA en el desarrollo

### 14.1 Flujo

1. Contexto: `CLAUDE.md` + el documento de `docs/architecture/` que aplique.
2. Para trabajo de código usar `/kensho-governance` o el agente `kensho-android-dev`; para historias, `/tech-story-architect`; para revisar, `/validate-pullrequest`.
3. Para diseño, partir del nodo de Figma con el flujo de ADR-007.
4. Revisión humana de todo el diff; compilar y verificar en emulador.
5. Registrar la asistencia en la sección de IA de la plantilla de PR.

### 14.2 Qué delegar

- Implementar una pantalla desde Figma siguiendo la anatomía de `payments/`.
- Convertir SVGs a VectorDrawable, extraer cadenas a `strings.xml`.
- Escribir tests de ViewModel con las *Fake*.
- Resolver deuda mecánica (comentar `contentDescription = null`, mover literales a tokens).

### 14.3 Qué no delegar sin revisión cuidadosa

- Decisiones de arquitectura nuevas (requieren ADR).
- Agregar dependencias (DEP-03).
- Textos visibles al usuario (revisar ortografía y tono).
- Cualquier cosa con datos personales de alumnos o tutores.

### 14.4 Plantilla de prompt

```
Contexto: Kensho, app Android de un solo módulo (dev.treecode.kensho), Compose + MVVM/UDF.
Lee CLAUDE.md y docs/architecture/03-standards.md antes de escribir código.

Tarea: <pantalla/componente/deuda> — Figma: <URL con node-id> (si aplica)

Restricciones:
- Screen (stateful) + Content (stateless) + Tkd<X>Actions; ViewModel con StateFlow<UiState> @Immutable.
- Datos desde Tkd<X>Repository (Fake + Network); VM @HiltViewModel sin default a la Fake; "hoy" desde el repositorio, nunca LocalDate.now().
- Red: suspend/Flow + TkdResult, DTO solo en data/remote, Loading/Error en el UiState con @StringRes.
- Solo tokens TkdColors/TkdDimens/TkdTypography; nada de Color(0x…)/N.dp/N.sp fuera de ui/theme.
- Textos en res/values/strings.xml en español (es-MX), claves tkd_<feature>_<desc>.
- contentDescription en iconos informativos; null solo decorativo y comentado.
- Reutiliza components/ antes de crear; secciones internal en el paquete del feature.
- Previews private por estado; test unitario del ViewModel.
Verifica: ./gradlew :app:assembleDebug :app:testDebugUnitTest
```

---

## 15. Datos reales: Hilt y la capa de red

### 15.1 Por qué ahora y no antes

Mientras todos los datos eran del prototipo, un framework de DI solo movía el `= FakeTkd…()` de un lugar a otro. Con el primer API aparecen dependencias que la app sí necesita elegir (cliente HTTP, almacén de sesión, dispatchers) y el default a la *Fake* pasa a ser un riesgo: una pantalla podría quedarse con datos de prueba sin que nadie lo note. Ese es el disparador que ADR-011 dejó escrito.

### 15.2 Por qué Hilt

Hilt valida el grafo **al compilar**: si falta un enlace, el build falla en vez de la pantalla. Además resuelve dos cosas que a mano son repetitivas: `SavedStateHandle` para los argumentos de ruta (adiós a `viewModel(key = transactionId) { … }`) y el alcance de la sesión. Koin habría sido más ligero, pero el error llega en tiempo de ejecución.

### 15.3 La interfaz sigue siendo la frontera

La red entra **detrás** de `Tkd<X>Repository`. La UI no sabe si los datos vienen de la *Fake* o del servidor; por eso las previews y los tests de `Content` no cambian. Lo que sí cambia es el contrato: una llamada de red puede tardar y puede fallar, así que la interfaz pasa a `suspend`/`Flow` y devuelve `TkdResult`. Un `null` no distingue "no existe" de "no hay conexión"; una excepción obliga al VM a conocer Retrofit.

### 15.4 Por qué los DTO no salen de `data/`

El JSON del servidor cambia por razones del backend; el `UiState` cambia por razones del diseño. Si comparten clase, cada cambio de uno rompe al otro. El mapper es el único sitio que conoce ambos y es fácil de probar con fixtures. Por la misma razón los catálogos llegan como códigos: el texto visible vive en `strings.xml` (es-MX), no en el servidor.

### 15.5 El error es un estado, no un mensaje

Una pantalla con red tiene tres caras: cargando, contenido y error. Se modelan en el `UiState` para que cada una tenga su preview y su test. El VM muestra el `message` que el API propio escribió para el sensei (ADR-020) y, cuando no hay (sin red, timeout), traduce `TkdDataError` a un recurso. Nunca muestra `exception.message`, que llega en inglés y con detalles técnicos que no le sirven al sensei.

### 15.6 Seguridad mínima desde el primer endpoint

El token vive cifrado (DataStore + Tink), solo los interceptores lo tocan y nunca se registra. Los cuerpos HTTP llevan nombres de alumnos y montos: por eso el log de red es `BASIC` y solo en debug. Es barato hacerlo bien con un endpoint y caro auditarlo con veinte.

### 15.7 Lo que sigue sin hacerse

Caché offline (Room), UseCases y modularización siguen esperando su disparador. Que la red haya llegado no los adelanta.

---

## Decisiones relacionadas

ADR-001 a ADR-017 en [`01-governance.md §4`](01-governance.md#4-decisiones-arquitectónicas-adr) (ADR-011 reemplazado por ADR-016). Deuda TD-KEN-001 a TD-KEN-012 en [`01-governance.md §6`](01-governance.md#6-registro-de-deuda-técnica).
