# 📊 Informe de Cumplimiento — Kensho

> **Versión:** 1.0.0 | **Fecha de auditoría:** 2026-10-27 (línea base, primera auditoría)
> **Alcance:** módulo `:app` (`dev.treecode.kensho`), rama `develop` en `3fc6cc4` + corpus de documentación
> **Método:** fitness functions de [`01-governance.md §7`](01-governance.md#7-fitness-functions) y conteos adicionales (ver [Apéndice](#apéndice--comandos-de-verificación)). Todas las cifras de este documento se obtuvieron ejecutando esos comandos; ninguna es estimada salvo que se indique "≈".

> **¿Por qué existe este documento?**
> Es la foto **empírica** del código. Los ADRs (`01`) dicen cómo debería ser; las normas (`03`) dicen cómo se verifica; este informe dice **cómo está**. Fija la línea base contra la que se mide cada PR ("ninguna fitness function empeora") y ordena la deuda por impacto y esfuerzo. Se re-audita al cerrar cada sprint o cuando cambia una TD.

---

## Índice

1. [Contexto de la evaluación](#1-contexto-de-la-evaluación)
2. [Resumen ejecutivo](#2-resumen-ejecutivo)
3. [Métricas verificadas](#3-métricas-verificadas)
4. [Hallazgos por deuda técnica](#4-hallazgos-por-deuda-técnica)
5. [Fortalezas confirmadas](#5-fortalezas-confirmadas)
6. [Plan de acción priorizado](#6-plan-de-acción-priorizado)
7. [Indicadores para la próxima auditoría](#7-indicadores-para-la-próxima-auditoría)
8. [Apéndice — comandos de verificación](#apéndice--comandos-de-verificación)

---

## 1. Contexto de la evaluación

| Dato | Valor |
|------|-------|
| Módulos Gradle | 1 (`:app`) |
| Archivos Kotlin en `src/main` | 88 (10,505 líneas) |
| Pantallas (`Tkd<X>Screen`) | 8 — login, registro, resumen, alumnos, perfil, pase de lista, pagos, detalle de pago |
| ViewModels | 8 (uno por pantalla) |
| Repositorios | 6 interfaces, 6 implementaciones *Fake*, 0 reales |
| Componentes | 11 archivos en `atoms/`, 25 en `molecules/` |
| Funciones `@Composable` | 224 (67 públicas, 46 `internal`, 105 `private` con prefijo en mayúscula) |
| Recursos de Figma | 107 `ic_*` vectoriales (102 con cabecera "Generado desde Figma"), 26 `img_*` (20 WebP, 6 JPG, 136 KB en total) |
| Cadenas | 259 `<string>` + 13 `<plurals>` en `res/values/strings.xml` |
| Tests | 1 unitario y 1 instrumentado, ambos de ejemplo de la plantilla |

**Premisas:**

- La app está en etapa de prototipo funcional: no hay backend, persistencia, DI ni CI. Esas ausencias **no se penalizan** (ver `01-governance.md §3`), salvo CI/análisis estático, registrado como deuda (TD-KEN-008).
- Las previews y los datos *Fake* son código de producción a efectos de tokens y textos (THM-01 aplica también a previews).
- Score por dominio: 100 % = todas las reglas 🔒 del dominio cumplidas en todo el código; se descuenta en proporción a archivos/ocurrencias afectadas y a la severidad.

---

## 2. Resumen ejecutivo

### Tabla maestra por dominio

| Dominio | Reglas | Score | Estado | Deuda |
|---------|--------|:-----:|:------:|-------|
| Arquitectura y capas | ARC-01..09 | 90 % | 🟢 | TD-KEN-005 |
| Composables / sistema de diseño | CMP-01..09 | 80 % | 🟢 | TD-KEN-006, TD-KEN-007 |
| Tokens de diseño | THM-01..06 | 92 % | 🟢 | TD-KEN-003, TD-KEN-004 |
| Accesibilidad | A11Y-01..05 | 55 % | 🟡 | TD-KEN-002 |
| Textos y formato | I18N-01..05 | 60 % | 🟡 | TD-KEN-004, TD-KEN-011 |
| Recursos de Figma | RES-01..05 | 90 % | 🟢 | restos de plantilla (TD-KEN-003) |
| Estructura y nombres | STR-01..06 | 70 % | 🟡 | TD-KEN-003, TD-KEN-006, TD-KEN-007 |
| Recomposición | PERF-01..05 | 95 % | 🟢 | — |
| Pruebas | TEST-01..05 | 5 % | 🔴 | TD-KEN-001, TD-KEN-010 |
| Logging y seguridad | LOG-01..02, DEP-05 | 100 % | 🟢 | — |
| Dependencias y build | DEP-01..04 | 60 % | 🟡 | TD-KEN-009 |
| Proceso (CI, lint, git) | GIT-01..04 | 30 % | 🔴 | TD-KEN-008 |

```
GLOBAL  ███████░░░  69 %  🟡  (media simple de los 12 dominios)

Arquitectura      █████████░  90 %
Composables       ████████░░  80 %
Tokens            █████████░  92 %
Accesibilidad     █████░░░░░  55 %
Textos/formato    ██████░░░░  60 %
Recursos Figma    █████████░  90 %
Estructura        ███████░░░  70 %
Recomposición     █████████░  95 %
Pruebas           ░░░░░░░░░░   5 %
Logging/seguridad ██████████ 100 %
Dependencias      ██████░░░░  60 %
Proceso           ███░░░░░░░  30 %
```

**Lectura:** el código de presentación está en muy buen estado para la etapa (arquitectura UDF consistente, tokens casi sin fugas, cero `!!` y cero logs). Las brechas son de **red de seguridad**: no hay tests, ni lint, ni CI, y la accesibilidad y el idioma quedaron a medias. Son baratas de cerrar ahora y caras de cerrar con 30 pantallas.

---

## 3. Métricas verificadas

| FF | Métrica | Regla | Valor | Objetivo |
|----|---------|-------|:-----:|:--------:|
| FF-01 | `android.util.Log` / `println` / `printStackTrace` en `main` | LOG-01 | **0** | 0 |
| FF-02 | `!!` / `GlobalScope` / `runBlocking` | — | **0** | 0 |
| FF-03 | Colores literales fuera de `ui/theme/` | THM-01 | **4** | 0 |
| FF-04 | `.dp` / `.sp` literales fuera de `ui/theme/` | THM-02 | **2** (1 en comentario) | 0 |
| FF-05 | `Text("…")` literal | I18N-01 | **1** (`"%02d".format`) | 0 |
| FF-06 | `contentDescription = null` sin comentario | A11Y-01 | **65** en 31 archivos | 0 |
| FF-07 | `LocalDate.now()` / reloj del sistema fuera de `data/` | ARC-07 | **3** | 0 |
| FF-08 | Imports cruzados entre features | ARC-05 | **0** | 0 |
| FF-09 | Archivos > 300 líneas | STR-02 | **4** | 0 |
| FF-10 | `collectAsState()` sin lifecycle | PERF-02 | **0** | 0 |
| FF-11 | Componentes públicos sin `@Preview` | CMP-09 | **2** | 0 |
| FF-12 | Archivos de test | TEST-01 | **11** (medida 2026-10-31; 1 de ejemplo + 1 instrumentado de ejemplo) | ≥ 9 |
| FF-13 | Escalado manual de fuente (`× fontScale`) | A11Y-06 | **0** (medida 2026-10-28) | 0 |
| FF-14 | Red/DTO importados fuera de `data/` y `di/` | ARC-11 | **0** (medida 2026-10-30) | 0 |
| FF-15 | Defaults `Repository = Fake…()` en constructores | ARC-06 | **12** (medida 2026-10-31) | 0 |
| FF-16 | `Dispatchers.IO`/`Default` fuera de `di/` | ARC-13 | **0** (medida 2026-10-30) | 0 |

**Métricas complementarias**

| Métrica | Valor |
|---------|-------|
| Llamadas a `collectAsStateWithLifecycle()` | 8 (una por pantalla) |
| `@Immutable` | 35 (32 en `model/`, 1 en `TkdInputField`, 1 en `TkdBeltPalette`, 1 en `TkdColors`); todos los `data class` e interfaces `sealed` de primer nivel en `model/` lo llevan |
| `@Preview` | 74 en 44 archivos |
| `stringResource` / `pluralStringResource` | 278 usos |
| Cadenas por idioma (por sección de `strings.xml`) | español: 138 (login 24, registro 33, pagos 43, detalle de pago 38) · inglés: ≈133 (resumen 29, alumnos 29, perfil 44, pase de lista 23, accesibilidad 7, barra inferior incluida en resumen) |
| Listas perezosas con `key` | 4 de 4 |
| Funciones de nivel superior > 60 líneas | 28 |
| Archivo más grande | `payments/TkdPaymentsSections.kt` (667 líneas) |
| TODO / FIXME | 0 |
| Recursos de plantilla sin uso | `ic_home`, `ic_favorite`, `ic_account_box`, `_ic_launcher_foreground` (0 referencias) y la cadena `custom_linear_progress` |

---

## 4. Hallazgos por deuda técnica

### ✅ TD-KEN-001 — Sin pruebas reales

> **Saldada el 2026-11-19** (ver "Actualización 2026-11-19 (2)" al final). Lo de abajo es la medición de apertura.

- `app/src/test` solo tiene `ExampleUnitTest` (`2 + 2 == 4`); `app/src/androidTest` solo `ExampleInstrumentedTest`.
- 0 de 8 ViewModels con test; `core/Validate.kt` sin test; propiedades derivadas (`visibleTransactions`, `totalCents`, `daysLeft()`) sin test.
- `kotlinx-coroutines-test` no está en el catálogo.
- **Impacto:** cualquier refactor (TD-KEN-005, TD-KEN-006) se hace sin red. Es la deuda que más encarece a las demás.

### 🟡 TD-KEN-002 — `contentDescription = null` sin justificar

- 65 ocurrencias en 31 archivos, 0 con comentario. Muchas son decorativas legítimas (iconos junto a un texto que ya dice lo mismo), pero no se puede distinguir un olvido de una decisión.
- Concentración: secciones de pagos, perfil, registro e inicio de sesión.

### 🟡 TD-KEN-003 — Restos de la plantilla de Android Studio

- `ui/theme/Color.kt` (Purple80…Pink40), `Theme.kt` (`KenshoTheme` con color dinámico) y `Type.kt`: ninguna pantalla usa `MaterialTheme.*`; solo `MainActivity` envuelve la app en `KenshoTheme`.
- `MainActivity.kt`: ~30 líneas comentadas del `NavigationSuiteScaffold`, `enum AppDestinations` que duplica `TkdTab`, `StatusBarProtection` sin uso (con `Color.White`).
- `app/build.gradle.kts` declara `material3-adaptive-navigation-suite` dos veces.
- 4 drawables y 1 cadena de plantilla sin uso.

### 🟢 TD-KEN-004 — Literales fuera de tokens

| Archivo | Literal | Contexto |
|---------|---------|----------|
| `components/molecules/TkdStudentAvatar.kt:84,96` | `Color(0xFF059669)`, `Color(0xFF94A3B8)` | previews (equivalen a `TkdColors.Success` y un gris existente) |
| `components/molecules/TkdFilterChips.kt:135` | `Color(0xFF059669)` | preview |
| `MainActivity.kt:109` | `Color.White` | código muerto (TD-KEN-003) |
| `components/atoms/CustomLinearProgress.kt:33` | `4.dp` | default de parámetro |
| `payments/TkdPaymentDetailSections.kt:542` | `"%02d".format(...)` | formato sin locale (I18N-04) |

### 🟡 TD-KEN-005 — Fechas no deterministas

`LocalDate.now()` como default en `model/TkdPaymentsUiState.kt:17`, `model/TkdStudentProfileUiState.kt:13` y `profile/TkdStudentProfileViewModel.kt:23`. Pagos ya lee la fecha de `repository.closingDate()` en el VM, pero el default del modelo sigue ahí; perfil depende del reloj real con datos de prototipo de 2024.

### 🟡 TD-KEN-006 — Archivos y funciones grandes

| Archivo | Líneas |
|---------|:------:|
| `payments/TkdPaymentsSections.kt` | 667 |
| `payments/TkdPaymentDetailSections.kt` | 590 |
| `profile/TkdProfileProgressCards.kt` | 352 |
| `signup/TkdSignUpScreen.kt` | 310 |

28 funciones superan 60 líneas; las mayores: `TkdProfileAttendanceCard` (162), `TkdTransactionCard` (116), `TkdCashFlowCard` (108), `TkdProfileHeroCard` (103).

### 🟢 TD-KEN-007 — Inconsistencias de estructura y nombres

- El resumen vive en `ui/present/` y los demás features en la raíz del paquete.
- `components/atoms/CustomLinearProgress.kt` sin prefijo `Tkd` y sin preview; su cadena `custom_linear_progress` es un `testTag`/descripción de plantilla.
- `TkdTab.label` con texto literal en inglés (además de `tkd_nav_*`).
- README raíz desactualizado — **resuelto en esta auditoría** (se reescribió junto con el corpus).

### 🟠 TD-KEN-008 — Sin análisis estático ni CI

No hay Detekt, ktlint, configuración de Android Lint (`lint.xml`/baseline) ni `bitbucket-pipelines.yml`. El hook `.githooks/commit-msg` existe pero requiere `git config core.hooksPath .githooks` manual. Las fitness functions solo se ejecutan a mano.

### 🟡 TD-KEN-009 — Dependencias desalineadas

- `core-ktx 1.10.1`, `lifecycle-runtime-ktx 2.6.1`, `activity-compose 1.8.0`, `espresso 3.5.1` frente a Compose BOM `2025.12.00`, AGP `9.4.1` y compileSdk 37.
- `collectAsStateWithLifecycle` y `viewModel()` se usan pero `lifecycle-runtime-compose` / `lifecycle-viewmodel-compose` no están declarados (llegan transitivos vía Navigation).
- `mavenLocal()` en `pluginManagement` y `dependencyResolutionManagement`.
- `libs.uikit` (`com.invex.one:uikit-qa`) en el catálogo sin uso.

### 🟢 TD-KEN-010 — `Validate.kt`

Regex de email `^[A-Za-z](.*)([@]{1})(.+)(\.)(.+)` acepta `a@@b.c` y espacios; el objeto está indentado como si estuviera anidado; sin tests. Lo usan `TkdLoginViewModel` y `TkdSignUpViewModel`.

### 🟡 TD-KEN-011 — Idioma mixto

≈133 cadenas en inglés (resumen, alumnos, perfil, pase de lista, `tkd_cd_*` comunes y barra inferior) frente a 138 en español (login, registro, pagos, detalle). El usuario ve la app cambiar de idioma al pasar del login al resumen.

### ✅ TD-KEN-012 — Migración pendiente a Hilt y capa de red (ADR-016, ADR-017)

> **Saldada el 2026-11-19** (ver "Actualización 2026-11-19" al final). El resto pendiente, el resumen del dojo sin
> endpoint, pasa a **TD-KEN-013**. Lo de abajo es la medición de apertura.

Abierta el 2026-10-30 al aceptar ADR-016/017. No es una regresión: es la distancia entre el código actual y la arquitectura aceptada para la Fase 1.

- **FF-15 = 12** defaults a la *Fake* (2026-10-31; login y registro ya migraron a `@HiltViewModel`): 11 en 6 ViewModels (`ui/present/TkdDashboardViewModel.kt:18`, `payments/TkdPaymentsViewModel.kt:17-18`, `payments/TkdPaymentDetailViewModel.kt:18-19`, `students/TkdStudentsViewModel.kt:21-22`, `profile/TkdStudentProfileViewModel.kt:21-22`, `checkin/TkdCheckInViewModel.kt:23-24`) y 1 en `data/TkdStudentProfileRepository.kt:21`.
- **Contratos síncronos:** 5 de 6 interfaces de `data/` son síncronas y usan `null` como fallo; `TkdAuthRepository` ya devuelve `TkdResult` y expone `session: StateFlow<TkdSession>`.
- **Implementación de red:** 1 / 6 `NetworkTkd<X>Repository` (`auth`). En debug apunta al mock estático `GET login.json` mediante `data/remote/mock/StaticMockTkdAuthApi.kt` hasta que el backend implemente `POST auth/login`.
- **Carga/error:** 2 / 8 `UiState` (login y registro, con `isSubmitting` + `submitError`).
- **Fotos como recurso:** 6 campos `@DrawableRes` de foto en `model/` (`TkdPaymentsUiState.kt:79`, `TkdPaymentDetailUiState.kt:39`, `TkdStudentProfileUiState.kt:31,39`, `TkdDashboardUiState.kt:27-28`) deben pasar a `TkdImage`.
- **Sesión en memoria:** `FakeTkdAuthRepository.remembered` se pierde al cerrar la app.
- Manifiesto: el último commit no declara `INTERNET`; hay un cambio local sin commit que agrega `INTERNET`, `ACCESS_NETWORK_STATE` y `ACCESS_WIFI_STATE` (este último no lo pide ADR-017).

### 🟢 TD-KEN-013 — Resumen del dojo en parte sin red (bloqueada por `GET /dojo/overview`; revisada 2026-11-22)

Abierta el 2026-11-19 al saldar TD-KEN-012: es lo único de esa migración que depende del backend. El contrato
(`docs/api/kensho-api.yaml`) no tiene `GET /dojo/overview`; la propuesta está en `docs/api/dashboard-api.md`.

- **Repositorio sin red:** `TkdDashboardRepository` (`data/TkdDashboardRepository.kt`) es síncrono y `di/DataModule.kt:52` enlaza siempre `FakeTkdDashboardRepository`; 1 / 9 interfaces de `data/` sin `NetworkTkd<X>Repository`.
- **Sin carga/error:** `TkdDashboardUiState` no tiene `isLoading`/`loadError`.
- **Fotos como recurso:** `TkdDashboardUiState.kt:37` (`DojoHeader.profilePhoto`) y `:57` (`attendeePhotos`) siguen con `@DrawableRes`; deben pasar a `TkdImage`.
- **Impacto:** la sesión en vivo, las métricas, el filtro y los exámenes del resumen solo se muestran con `AUTH_STATIC_MOCK`; con datos reales el resumen muestra lo que sale del perfil del instructor (agenda, avisos, ingresos).

---

## 5. Fortalezas confirmadas

- **UDF consistente:** las 8 pantallas siguen `Screen` → `Content` con `Actions`, `StateFlow` privado/público y `collectAsStateWithLifecycle` (FF-10 = 0).
- **Aislamiento entre features:** 0 imports cruzados (FF-08).
- **Tokens casi completos:** solo 4 colores y 1 dimensión fuera de `ui/theme/`, todos en previews o código muerto salvo uno.
- **Código limpio:** 0 `!!`, 0 `GlobalScope`/`runBlocking`, 0 logs, 0 TODO/FIXME.
- **Modelos estables:** todos los `UiState` y modelos de `model/` son `@Immutable`; dinero en centavos `Int` en pagos.
- **Listas con `key`:** 4 de 4.
- **Figma trazable:** cada bloque de `strings.xml` cita su nodo; 102 de 107 vectores llevan la cabecera de generación (los 5 restantes son de plantilla/launcher).
- **Previews abundantes:** 74, con todas las pantallas previsualizables con datos *Fake*.

---

## 6. Plan de acción priorizado

Orden por **impacto ÷ esfuerzo**. Historias detalladas en [`07-user-stories.md`](07-user-stories.md).

### 🚨 Sprint 1 — Red de seguridad

| # | Acción | TD | Esfuerzo |
|---|--------|----|:--------:|
| 1 ✅ (2026-11-19, salvo `Validate`) | Infraestructura de tests + tests de los 8 ViewModels y `Validate` | TD-KEN-001, TD-KEN-010 | M |
| 2 | Detekt + Android Lint + pipeline de Bitbucket con las fitness functions | TD-KEN-008 | M |
| 3 | Limpiar plantilla (`MainActivity`, tema Material, drawables y dependencia duplicada) | TD-KEN-003 | S |
| 4 | Inyectar la fecha de referencia en perfil y quitar defaults `LocalDate.now()` | TD-KEN-005 | S |

### Sprint 2 — Calidad visible

| # | Acción | TD | Esfuerzo |
|---|--------|----|:--------:|
| 5 | Auditar los 65 `contentDescription = null`: comentar decorativos, describir informativos | TD-KEN-002 | M |
| 6 | Traducir al español las ≈133 cadenas en inglés | TD-KEN-011 | M |
| 7 | Literales a tokens y formato con locale | TD-KEN-004 | S |
| 8 | Actualizar dependencias y declarar las directas | TD-KEN-009 | S |

### Sprint 3 — Estructura

| # | Acción | TD | Esfuerzo |
|---|--------|----|:--------:|
| 9 | Dividir los 4 archivos > 300 líneas y las funciones > 60 líneas de pagos y perfil | TD-KEN-006 | M |
| 10 | Mover `ui/present/` → `dashboard/`, renombrar `CustomLinearProgress` | TD-KEN-007 | S |

### Fase 1 — Datos reales (ADR-016, ADR-017)

Arranca cuando haya API disponible (o su contrato OpenAPI con mock). Requiere TD-KEN-009 cerrada.

| # | Acción | TD | Esfuerzo |
|---|--------|----|:--------:|
| 11 ✅ (2026-10-31) | Instalar Hilt + KSP, `di/` con `DispatchersModule` y `DataModule` enlazando las *Fake* | TD-KEN-012 | S |
| 12 ✅ (2026-10-31) | Piloto `auth`: `TkdResult`, `NetworkTkdAuthRepository`, `TkdSessionStore` (DataStore + Tink), interceptores, sesión observada en `TkdApp`, tests con `MockWebServer` | TD-KEN-012 | L |
| 13 ✅ (2026-11-09) | `payments` en red: carga/error en `UiState`, mappers con fixtures (`TkdImage` + Coil: 2026-11-19) | TD-KEN-012 | M |
| 14 ✅ (2026-11-19) | Resto de repositorios (alumnos, perfil, pase de lista) y fotos por URL | TD-KEN-012 | L |
| 15 | Resumen en red cuando exista `GET /dojo/overview` | TD-KEN-013 | M |

> Regla de mezcla: cada sprint lleva al menos una historia de producto (ver `07-user-stories.md §1`).

---

## 7. Indicadores para la próxima auditoría

| Indicador | Línea base (2026-10-27) | Meta fin de Sprint 1 | Meta fin de Sprint 3 |
|-----------|:-----------------------:|:--------------------:|:--------------------:|
| FF-03 colores literales | 4 | 4 | 0 |
| FF-04 dp/sp literales | 2 | 0 | 0 |
| FF-05 textos literales | 1 | 1 | 0 |
| FF-06 `null` sin comentario | 65 | 65 | 0 |
| FF-07 reloj del sistema | 3 | 0 | 0 |
| FF-09 archivos > 300 | 4 | 4 | 0 |
| FF-11 componentes sin preview | 2 | 2 | 0 |
| FF-12 archivos de test | 11 (2026-10-31) | ≥ 9 reales | ≥ 14 |
| FF-13 escalado manual de fuente | 0 | 0 | 0 |
| FF-14 red fuera de `data/` | 0 (2026-10-30) | 0 | 0 |
| FF-15 defaults a la *Fake* | 12 (2026-10-31) | — | 0 al cerrar la Fase 1 |
| FF-16 dispatchers literales | 0 (2026-10-30) | 0 | 0 |
| ViewModels con test | 0 / 8 | 8 / 8 | 8 / 8 |
| Cadenas en inglés en `values/` | ≈133 | ≈133 | 0 |
| Pipeline de CI | no | sí | sí, bloqueante |
| Score global | 69 % | ≥ 75 % | ≥ 85 % |

---

## Apéndice — Comandos de verificación

Desde la raíz del repo:

```bash
M=app/src/main/java/dev/treecode/kensho

# Fitness functions FF-01..FF-13: ver 01-governance.md §7

# Tamaño
find $M -name '*.kt' | wc -l
find $M -name '*.kt' -exec cat {} + | wc -l
find $M -name '*.kt' -exec wc -l {} + | sort -n | tail -6

# Arquitectura
grep -rlE "^fun Tkd[A-Za-z]+Screen\(" $M | wc -l          # pantallas
grep -rl ": ViewModel()" $M | wc -l                         # ViewModels
grep -rh "^class Fake" $M/data | wc -l                      # repositorios Fake
grep -rn "collectAsStateWithLifecycle()" $M | wc -l

# Composables y previews
grep -rn "@Composable" $M | wc -l
grep -rhE "^fun [A-Z]" $M | wc -l                           # públicos
grep -rn "@Preview" $M | wc -l

# Funciones de nivel superior > 60 líneas (aproximado: mide hasta la siguiente fun)
awk 'FNR==1{fn="";start=0} /^(private |internal )?fun /{if(fn!="" && FNR-start>60) print FILENAME": "fn" "FNR-start; fn=$0; start=FNR}' $(find $M -name '*.kt')

# Estabilidad
grep -rn "@Immutable" $M | wc -l
grep -rn "items(" $M | grep -c "key ="

# Textos
grep -rn "stringResource\|pluralStringResource" $M | wc -l
awk '/<!--/{sec=$0} /<string |<plurals /{c[sec]++} END{for(k in c) print c[k]"\t"k}' app/src/main/res/values/strings.xml

# Recursos
ls app/src/main/res/drawable | grep -c '^ic_'
ls app/src/main/res/drawable | grep -c '^img_'
grep -L "Generado desde Figma" app/src/main/res/drawable/ic_*.xml
find app/src/main/res/drawable -name 'img_*' -size +200k

# Recursos sin uso (ejemplo)
for r in ic_home ic_favorite ic_account_box; do echo "$r $(grep -rn "R.drawable.$r\b" app/src/main | wc -l)"; done

# Tests
find app/src/test app/src/androidTest -name '*Test.kt'
```

---

## Actualización 2026-10-28 — Escala de fuente (ADR-015)

- Auditoría a 150 % y 200 % en emulador API 33: **13 roturas** (barras, chips, tarjetas de alumno, pase de lista, pagos, detalle de cobro, métricas, login). Todas corregidas; detalle en [`FONT_SCALE_ANALISIS.md`](FONT_SCALE_ANALISIS.md).
- Nueva regla 🔒 A11Y-06 y fitness function FF-13 (línea base **0**).
- FF-12 pasa de 2 a **3** archivos de test (`TkdFontScaleTest`, 7 casos en verde); FF-11 sin cambio.
- Dominio de accesibilidad: mejora la cobertura de texto escalable; el pendiente principal sigue siendo TD-KEN-002 (`contentDescription = null` sin comentario).

---

## Actualización 2026-10-28 — Detekt, reglas `Kensho*` y changelog (TD-KEN-008 parcial, GIT-05)

**Qué cambió:** módulo `:lint-rules` con 15 reglas Detekt propias (FF-01…FF-11, FF-13 y ARC-02, 39 tests), Detekt 1.23.8 aplicado en el root con `config/detekt/detekt.yml` y baseline de 126 entradas; changelog con towncrier (`config/towncrier.toml`, `changelog.d/`) y regla GIT-05.

| Métrica | Grep (FF) | Detekt | Nota |
|---------|-----------|--------|------|
| FF-03 colores literales | 4 | 4 | — |
| FF-04 dp/sp literales | 2 | 1 | el grep cuenta una línea comentada |
| FF-05 texto literal | 1 | 0 | el hallazgo es formato sin locale (I18N-04), no texto |
| FF-06 `contentDescription = null` sin comentario | 65 | 65 | — |
| FF-07 reloj del sistema | 3 | **5** | **hallazgo nuevo:** `LocalTime.now()` en `model/TkdCheckInUiState.kt:17` y `checkin/TkdCheckInViewModel.kt:25`; se suman a TD-KEN-005 |
| FF-09 archivos > 300 | 4 | 4 | — |
| FF-11 componentes sin preview | 2 | 2 | — |

- `./gradlew detekt` pasa con el baseline y falla con cualquier violación nueva (verificado con un archivo de prueba: color literal + import entre features → 2 hallazgos).
- TD-KEN-008 **sigue abierta**: faltan `detekt-formatting`/compose-rules, Android Lint con baseline y `bitbucket-pipelines.yml`.
- FF-07 de `01-governance.md §7` ampliada a `LocalTime`/`Instant` (línea base 5) y TD-KEN-005 actualizada.

---

## Actualización 2026-10-30 — ADR-016 (Hilt) y ADR-017 (red y sesión) aceptados

**Qué cambió:** se cumplió el disparador de la Fase 1 (`01-governance.md §9`) y se aceptaron ADR-016 y ADR-017. No cambia código de `app/src/main`; cambian el estándar y la línea base.

- Reglas nuevas en `03-standards.md`: 🔒 ARC-10 (contrato `suspend`/`Flow` + `TkdResult`), 🔒 ARC-11 (red solo en `data/`), 🔒 ARC-12 (carga/error con recursos), 🔒 ARC-13 (dispatchers inyectados), ⚠️ ARC-14 (sesión centralizada), 🔒 TEST-06, 🔒 LOG-03, 🔒 DEP-06. ARC-06 pasa a "inyectados con Hilt".
- Fitness functions nuevas: **FF-14 = 0**, **FF-15 = 14**, **FF-16 = 0** (comandos en `01-governance.md §7`).
- Deuda nueva: **TD-KEN-012** 🟠 (ver §4). ADR-011 queda reemplazado por ADR-016.
- La premisa de §1 "sin backend, persistencia ni DI no se penaliza" deja de aplicar a DI y red: su ausencia se mide ahora en TD-KEN-012. Persistencia de datos offline (Room) sigue sin penalizarse.
- Dominio de arquitectura: el score de §2 no se recalcula hasta la próxima auditoría; ARC-10…14 aún no tienen código al que aplicarse.

---

## Actualización 2026-11-16 — Perfil del instructor compartido (ADR-021)

**Qué cambió:** el perfil de `GET me/profile` se guarda cifrado en el dispositivo (`data/local/TkdProfileCache.kt`) y vale mientras su `asOf` sea de hoy. Las 7 pantallas con encabezado heredan de `ui/TkdSenseiViewModel.kt` y la app deja de llamar a `GET me`.

| Métrica | Antes | Después | Evidencia |
|---------|-------|---------|-----------|
| Llamadas a `GET me` desde la app | 7 VMs (`loadAccount()` copiado) | 0 | FF-17 = 0 |
| VMs con datos del instructor vía `TkdSenseiViewModel` | 0 / 7 | 7 / 7 | `grep -rl ') : TkdSenseiViewModel(' $M` |
| `UiState` con `header: TkdDojoHeader?` | 0 (`account: TkdInstructorAccount?`) | 7 | `model/*UiState.kt` |
| Detekt (hallazgos totales) | 115 | 114 | `TkdDashboardViewModel.loadAccount` sin uso ya no existe |

- Fitness function nueva: **FF-17 = 0** (`01-governance.md §7`), regla 🔒 **ARC-15**, anti-patrón **AP-23** y regla planificada `KenshoSenseiProfileViaBase`.
- 🟡 **Hallazgo previo corregido:** `TkdDashboardViewModel` tenía `loadAccount()` sin llamar, así que el encabezado del Resumen no se cargaba (3 tests en rojo en `TkdDashboardViewModelTest`). `app/src/androidTest/.../TkdDashboardContentTest.kt` no compilaba porque no pasaba `stateProfile`.
- TD-KEN-012 sigue 🟠: cambia la forma de obtener el encabezado, no las cifras de migración.
- Riesgo aceptado: con la app abierta pasada la medianoche se ve el perfil del día anterior hasta la siguiente carga (ADR-021, Consecuencias).

## Actualización 2026-11-19 — TD-KEN-012 saldada; resumen en TD-KEN-013

Medición al cierre:

- **FF-15 = 0** defaults `= Fake…()` en ViewModels.
- **Red:** 8 / 9 interfaces de `data/` con `NetworkTkd<X>Repository` (auth, alumnos, perfil del alumno, ficha, alta, configuración del dojo, pase de lista, pagos y perfil del instructor); falta solo `TkdDashboardRepository` → TD-KEN-013.
- **Fotos:** `model/TkdImage` (`Resource`/`Remote`) + Coil 3.2.0; átomo `components/atoms/TkdPhoto.kt` con respaldo (iniciales o el dibujo de siempre). Fotos de alumnos, pagador, asistentes, instructor y miniatura de clips por URL; el encabezado (`TkdDojoHeader`) lleva el logo del dojo (`me/profile` → `dojo.logoUrl`) y la foto del instructor, y `TkdTopBar` los pinta en las 7 pantallas con barra. 0 `@DrawableRes` de foto en `model/` fuera de `TkdDashboardUiState`.
- **Encabezado:** `dojo: DojoHeader` (datos del prototipo) salió de 5 `UiState`; `TkdDashboardRepository` solo lo inyecta `TkdDashboardViewModel`.
- **Carga/error:** todos los `UiState` con datos del API la tienen salvo `TkdDashboardUiState` (→ TD-KEN-013).
- Verificado en el emulador con una imagen de prueba temporal; el API de pruebas manda las URLs en `null` (no hay endpoint para subir fotos), así que hoy se ven las iniciales y el logo de Kensho.

## Actualización 2026-11-19 (2) — TD-KEN-001 saldada

- **22 / 22 ViewModels con unit test** en `app/src/test`, cada uno con su *Fake* y `MainDispatcherRule`. El último en
  entrar fue la base `TkdSenseiViewModel` (`ui/TkdSenseiViewModelTest.kt`): carga inicial, error con reintento y un
  cambio hecho desde otra pantalla que llega por `observeProfile()` sin volver a pedir el perfil (ADR-021).
- `SplashViewModel` no se cubrió: se **quitó** con `SplashScreen` y la ruta `splash`, que ninguna pantalla abría y
  cuyo estado nunca cambiaba (V-07 de `docs/reviews/PR-1-feature-improvement_auth.md`). También se borró lo que solo
  usaba el splash: los diálogos de bloqueo, actualización forzada y mantenimiento (`components/logicComponents/`),
  `LoaderComponent`, `Context.openUrl` y los DTO de `data/remote/remoteConfig/` (8 hallazgos menos de Detekt).
- 468 unit tests y Compose UI tests sobre `Content` en `androidTest`. Sigue pendiente `Validate` (TD-KEN-010) y los
  flujos completos (TEST-UI-001).

## Actualización 2026-11-22 — TD-KEN-013 revisada: el resumen visible ya es real

Revisión para el piloto (checklist PILOTO-6):

- **Visible con datos reales:** encabezado, acciones rápidas (`profile.dojos`), agenda de la semana, avisos e ingresos
  salen de `GET me/profile` vía `TkdSenseiViewModel` (ADR-021).
- **Oculto:** sesión en vivo, métricas, filtro de disciplinas y exámenes siguen en `FakeTkdDashboardRepository` y solo se
  pintan con `BuildConfig.AUTH_STATIC_MOCK` (`feature/home/dashboard/TkdDashboardScreen.kt:150`), que está en `false` en
  debug y release (`gradle.properties`). Ningún usuario ve datos del prototipo.
- **Lo que queda de la deuda:** `di/DataModule.kt` sigue enlazando la *Fake*, `TkdDashboardViewModel` arma su estado con
  ella aunque no se muestre, y `AUTH_STATIC_MOCK` tiene dos usos (mock del login y secciones del prototipo): activarla
  para probar el login vuelve a mostrar las cifras falsas.
- **Severidad:** baja de 🟡 Medio a 🟢 Bajo porque ya no llega a los usuarios. Sigue bloqueada por `GET /dojo/overview`.
