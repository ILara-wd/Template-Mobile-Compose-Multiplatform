# 📖 Historias de Usuario — Kensho

> **Versión:** 1.0.0 | **Última actualización:** 2026-10-27
> **Contexto:** proyecto en etapa inicial. 8 pantallas implementadas desde Figma con repositorios *Fake*; sin backend, tests reales ni CI. Las historias técnicas salen de la línea base de [`06-compliance-report.md`](06-compliance-report.md) y de la deuda `TD-KEN-0XX` de [`01-governance.md §6`](01-governance.md#6-registro-de-deuda-técnica).
> **Clave de ticket:** `KEN-<n>` (marcador de posición hasta que exista el proyecto en el gestor de tareas).
> Documento vivo: se actualiza al iniciar o cerrar cada historia.

> **¿Por qué existe este documento?**
> Al inicio de un proyecto todo empuja a construir pantallas nuevas, y la deuda pequeña de hoy (tests, lint, idioma, accesibilidad) se vuelve la deuda grande de mañana. Este backlog convierte cada `TD-KEN` en una historia con criterios verificables y la intercala con el trabajo de producto, para que la red de seguridad crezca al mismo ritmo que la app.

---

## Índice

1. [Guía de historias](#1-guía-de-historias)
2. [Definición de Hecho (DoD)](#2-definición-de-hecho-dod)
3. [Plantilla de historia](#3-plantilla-de-historia)
4. [Protocolo de uso de IA](#4-protocolo-de-uso-de-ia)
5. [Sprint 1 — Red de seguridad](#5-sprint-1--red-de-seguridad)
6. [Sprint 2 — Calidad visible](#6-sprint-2--calidad-visible)
7. [Sprint 3 — Estructura](#7-sprint-3--estructura)
8. [Backlog](#8-backlog)
9. [Registro de velocidad](#9-registro-de-velocidad)

---

## 1. Guía de historias

### Tipos

| Tipo | Prefijo | Descripción |
|------|---------|-------------|
| **Producto** | `US-` | Pantalla o capacidad nueva visible para el sensei o el administrador del dojo |
| **Deuda técnica** | `TD-KEN-0XX-FIX` | Resuelve una deuda registrada en `01-governance.md §6` |
| **Habilitación** | `LINT-`, `TEST-BASE-`, `I18N-`, `CI-` | Infraestructura que hace posible cumplir una regla (lint, tests, traducción) |

### Regla de mezcla

1. **Cada sprint lleva al menos una historia de producto y al menos una de deuda/habilitación.** Mientras TD-KEN-001 y TD-KEN-008 sigan abiertas, la capacidad técnica no baja de **40 %**; después, de **20 %**.
2. **Boy scout:** una historia de producto que toca un archivo con deuda no la empeora y, si es barato (≤ 30 min), la resuelve en el mismo PR (p. ej. traducir las cadenas de la pantalla que modifica — TD-KEN-011).
3. **Toda pantalla nueva nace limpia:** `Screen`/`Content`/`Actions` + ViewModel con test (TEST-01), tokens (THM-01/02), textos en español (I18N-05), `contentDescription` deliberado (A11Y-01) y previews por estado (CMP-09).
4. **Una deuda nueva se registra antes de mergear:** si un PR introduce deuda consciente, abre su `TD-KEN-0XX` con `/kensho-governance:propagacion`.

### Estimación

Puntos de Fibonacci (1, 2, 3, 5, 8). Una historia de 8 se divide antes de entrar al sprint.

| Talla | Puntos | Referencia en Kensho |
|-------|:------:|----------------------|
| S | 1–2 | Mover un token, traducir una sección de `strings.xml` |
| M | 3–5 | Tests de 3–4 ViewModels, dividir un archivo de secciones |
| L | 8 | Pantalla nueva completa desde Figma |

---

## 2. Definición de Hecho (DoD)

Una historia está **hecha** cuando:

- [ ] `./gradlew :app:assembleDebug` compila sin warnings nuevos.
- [ ] `./gradlew :app:testDebugUnitTest` pasa (y `connectedDebugAndroidTest` si la historia agrega tests de UI).
- [ ] Ninguna fitness function de `01-governance.md §7` empeora respecto a `06-compliance-report.md`.
- [ ] Reglas 🔒 de `03-standards.md` cumplidas o con excepción justificada en el PR.
- [ ] Si hay UI: verificada en emulador contra la captura de Figma; capturas en el PR.
- [ ] Si toca una pantalla: sus cadenas están en español y sus `contentDescription = null` comentados.
- [ ] Si resuelve o crea una TD o cambia una regla: propagado al corpus (`/kensho-governance:propagacion`) y `06-compliance-report.md` actualizado.
- [ ] PR revisado con `/validate-pullrequest` y aprobado.
- [ ] Commit con formato `GIT-01`.

---

## 3. Plantilla de historia

```markdown
## [KEN-<n>] <TIPO>-<ID> — <Título en imperativo>

**Tipo:** Producto | Deuda técnica | Habilitación
**Prioridad:** 🔴 / 🟠 / 🟡 / 🟢       **Puntos:** <n>       **Sprint:** <n>
**Deuda relacionada:** TD-KEN-0XX (si aplica)      **Figma:** <archivo, nodo X:Y> (si aplica)

### Historia (producto)
Como <sensei | administrador del dojo | alumno>, quiero <capacidad> para <beneficio>.

### Objetivo (deuda / habilitación)
<Qué cambia en el código y qué métrica de 06-compliance-report.md mejora.>

### Criterios de aceptación
- [ ] Dado <contexto>, cuando <acción>, entonces <resultado verificable>.
- [ ] <Métrica FF-XX pasa de A a B.>

### Notas técnicas
<Archivos, reglas de 03-standards.md, decisiones de 01-governance.md.>

### Archivos afectados
- `app/src/main/java/dev/treecode/kensho/...`

### Registro de IA
- Herramienta: <Claude Code / Copilot / ninguna>
- Qué generó: <...>      Qué se revisó/corrigió a mano: <...>
```

---

## 4. Protocolo de uso de IA

### Principio rector

La IA acelera; **la responsabilidad es de quien hace el commit**. Todo código generado se lee completo, se compila, se ejecuta en emulador y se revisa contra `03-standards.md` como si lo hubiera escrito otra persona.

### Qué delegar

| Tarea | Herramienta / skill |
|-------|---------------------|
| Implementar una pantalla desde un nodo de Figma | Claude Code + MCP de Figma (skill `figma-design-to-code`), siguiendo ADR-007 |
| Tests de ViewModel y de lógica pura | Agente `kensho-android-dev` |
| Revisar un PR contra las reglas | `/validate-pullrequest <rama>` |
| Medir el estado del proyecto | `/kensho-governance:fitness-functions` |
| Redactar una historia o liquidar una deuda | `/tech-story-architect`, `/tech-story-architect:liquidar-deuda` |
| Propagar un cambio de regla o deuda al corpus | `/kensho-governance:propagacion` |

### Qué NO delegar sin revisión cuidadosa

- Decisiones que crean un ADR (DI, persistencia, red, modularización).
- Textos de usuario: la IA tiende a dejar inglés o calcos; revisa ortografía es-MX.
- Recursos de Figma: verificar que el vector exportado no se "redibujó" ni se sustituyó por uno parecido (RES-03).
- Cualquier cosa que maneje datos personales de alumnos o tutores.

### Prompts de referencia

```text
Implementa la pantalla <nombre> desde Figma <URL nodo>. Sigue CLAUDE.md y
docs/architecture/03-standards.md: Screen/Content/Actions, ViewModel con StateFlow,
UiState @Immutable en model/, repositorio interface + Fake en data/, tokens Tkd*,
textos en español en strings.xml con comentario del nodo, ic_tkd_* generados desde
Figma. Agrega tests del ViewModel y previews por estado. Verifica en emulador.
```

```text
Resuelve TD-KEN-0XX según docs/architecture/07-user-stories.md. No cambies
comportamiento visible. Actualiza 06-compliance-report.md con la nueva cifra
de la fitness function correspondiente.
```

### Registro en el PR

```markdown
## 🤖 Asistencia de IA
- Herramienta: Claude Code (<modelo>)
- Alcance: <qué generó>
- Revisión humana: <qué se cambió, qué se verificó en emulador>
```

---

## 5. Sprint 1 — Red de seguridad

**Objetivo:** que los siguientes cambios tengan tests y un pipeline que los ejecute. Capacidad técnica ≥ 40 %.

### TEST-BASE-001 — Infraestructura de pruebas

**Prioridad:** 🟠 · **Puntos:** 2 · **Deuda:** TD-KEN-001

**Objetivo:** dejar listo el esqueleto para que escribir un test sea trivial.

**Criterios de aceptación**
- [ ] `kotlinx-coroutines-test` agregado al catálogo y a `testImplementation`.
- [ ] `MainDispatcherRule` en `app/src/test/java/dev/treecode/kensho/testing/`.
- [ ] `ExampleUnitTest` y `ExampleInstrumentedTest` eliminados.
- [ ] Un test de ejemplo real (p. ej. `TkdPaymentsUiStateTest` sobre `visibleTransactions`) pasa con `./gradlew :app:testDebugUnitTest`.
- [ ] `03-standards.md §9` enlaza la regla de despachador de test.

### TD-KEN-001-FIX — Tests de los 8 ViewModels

**Prioridad:** 🟠 · **Puntos:** 5 · **Depende de:** TEST-BASE-001

**Objetivo:** FF-12 pasa de 2 archivos de ejemplo a ≥ 8 archivos reales; 8/8 ViewModels con test.

**Criterios de aceptación**
- [ ] Un `Tkd<X>ViewModelTest` por ViewModel: `TkdDashboardViewModel`, `TkdStudentsViewModel`, `TkdStudentProfileViewModel`, `TkdCheckInViewModel`, `TkdLoginViewModel`, `TkdSignUpViewModel`, `TkdPaymentsViewModel`, `TkdPaymentDetailViewModel`.
- [ ] Cada test cubre estado inicial y cada evento público (`onX`).
- [ ] Casos de borde mínimos: login con email inválido y contraseña vacía; registro sin aceptar términos; detalle de pago ignora cambios tras `onConfirm`; filtro de pagos `Pending`.
- [ ] Nombres `sujeto_condicion_resultado` (TEST-04); sin `Thread.sleep` ni fecha real (TEST-05).

### TD-KEN-010-FIX — Endurecer y probar `Validate`

**Prioridad:** 🟢 · **Puntos:** 2

**Criterios de aceptación**
- [ ] `ValidateTest` con casos válidos e inválidos (`a@@b.c`, espacios, sin dominio, sin TLD).
- [ ] Regex de email corregida para rechazar los inválidos sin romper los válidos de las *Fake*.
- [ ] Indentación normalizada; KDoc en español.

### LINT-001 — Detekt, Android Lint y pipeline de Bitbucket

**Prioridad:** 🟠 · **Puntos:** 5 · **Deuda:** TD-KEN-008

**Objetivo:** que las reglas 🔒 automatizables bloqueen el PR sin depender de la memoria del revisor.

**Criterios de aceptación**
- [ ] Plugin Detekt en el catálogo; `config/detekt/detekt.yml` con las reglas de `08-lint-rules.md` y baseline inicial.
- [ ] `lint { warningsAsErrors = false; baseline = file("lint-baseline.xml") }` en `app/build.gradle.kts`.
- [ ] `bitbucket-pipelines.yml` ejecuta en cada PR: `assembleDebug`, `testDebugUnitTest`, `detekt`, `lintDebug` y el script de fitness functions.
- [ ] El pipeline falla si una fitness function supera la línea base de `06-compliance-report.md`.
- [ ] `README.md` explica `git config core.hooksPath .githooks`.

### TD-KEN-003-FIX — Limpiar restos de la plantilla

**Prioridad:** 🟡 · **Puntos:** 2

**Criterios de aceptación**
- [ ] `MainActivity` sin código comentado, sin `AppDestinations` ni `StatusBarProtection`; solo `setContent { KenshoTheme { TkdApp(...) } }`.
- [ ] `KenshoTheme` sin colores Purple ni color dinámico: mapea los roles básicos de `TkdColors` a `lightColorScheme` (o se elimina si no aporta) y `Color.kt`/`Type.kt` desaparecen o se alinean con `TkdTypography`.
- [ ] Dependencia `material3-adaptive-navigation-suite` declarada una sola vez (o retirada si ya no se usa).
- [ ] Borrados `ic_home`, `ic_favorite`, `ic_account_box`, `_ic_launcher_foreground` y la cadena `custom_linear_progress` si no tiene uso.
- [ ] La app arranca igual en emulador (login → resumen).

### TD-KEN-005-FIX — Fecha de referencia inyectada

**Prioridad:** 🟡 · **Puntos:** 2

**Objetivo:** FF-07 de 3 a 0.

**Criterios de aceptación**
- [ ] `TkdPaymentsUiState.today` y `TkdStudentProfileUiState.today` sin default `LocalDate.now()`.
- [ ] `TkdStudentProfileViewModel` obtiene la fecha del repositorio (como `closingDate()` en pagos) o de un `Clock` inyectado.
- [ ] Test de perfil que fija la fecha y verifica cálculos derivados (edad, días al examen).

### US-001 — (historia de producto del sprint)

Reservado para la siguiente pantalla de Figma (p. ej. pantalla **Cinturones**, única tab sin pantalla). Debe cumplir la regla de mezcla §1.3.

---

## 6. Sprint 2 — Calidad visible

### TD-KEN-002-FIX — Auditoría de `contentDescription`

**Prioridad:** 🟡 · **Puntos:** 5

**Objetivo:** FF-06 de 65 a 0.

**Criterios de aceptación**
- [ ] Cada uno de los 65 `contentDescription = null` es: (a) decorativo → comentario en la misma línea con el motivo, o (b) informativo → `stringResource(R.string.tkd_cd_…)` en español.
- [ ] Recorrido con TalkBack en emulador por resumen, alumnos, pagos y detalle de pago sin elementos "sin etiqueta".
- [ ] Iconos clicables con área ≥ 48dp (A11Y-03).

### I18N-001 — Traducir la app al español

**Prioridad:** 🟡 · **Puntos:** 5 · **Deuda:** TD-KEN-011

**Objetivo:** ≈133 cadenas en inglés → 0 en `res/values/`.

**Criterios de aceptación**
- [ ] Secciones resumen, alumnos, perfil, pase de lista, accesibilidad y barra inferior traducidas al español de México con acentos.
- [ ] `TkdTab.label` deja de tener texto literal; la barra usa `tkd_nav_*`.
- [ ] Términos del dominio consistentes (glosario abajo) y revisados por alguien del dojo.
- [ ] Si se decide conservar inglés, las cadenas originales pasan a `res/values-en/strings.xml`.
- [ ] Capturas de las 4 pantallas traducidas en el PR (verificar que ningún texto se trunca).

**Glosario mínimo**

| Inglés | Español |
|--------|---------|
| Belt / Belt Test | Cinturón / Examen de cinturón |
| Check-in / Mat session | Pase de lista / Sesión en tatami |
| Overview | Resumen |
| Students | Alumnos |
| Kup / Dan | Kup / Dan (sin traducir) |

### TD-KEN-004-FIX — Literales a tokens

**Prioridad:** 🟢 · **Puntos:** 1

**Criterios de aceptación**
- [ ] `TkdStudentAvatar` y `TkdFilterChips` usan `TkdColors.Success` y el token gris existente en previews.
- [ ] `CustomLinearProgress` usa un token de `TkdDimens` para `trackHeight`.
- [ ] `"%02d".format(...)` sustituido por un formatter con `Locale.forLanguageTag("es-MX")` (`spanish`) en `TkdPaymentsFormat.kt`.
- [ ] FF-03 = 0 (tras TD-KEN-003-FIX), FF-04 = 0, FF-05 = 0.

### TD-KEN-009-FIX — Alinear dependencias

**Prioridad:** 🟡 · **Puntos:** 2

**Criterios de aceptación**
- [ ] `core-ktx`, `lifecycle`, `activity-compose`, `espresso`, `androidx-junit` en versiones compatibles con el BOM y compileSdk 37.
- [ ] `lifecycle-runtime-compose` y `lifecycle-viewmodel-compose` declarados explícitamente (DEP-02).
- [ ] `mavenLocal()` retirado de `settings.gradle.kts` (DEP-04) y entrada `uikit` eliminada del catálogo.
- [ ] Build, tests y recorrido en emulador sin regresiones.

### US-002 — (historia de producto del sprint)

---

## 7. Sprint 3 — Estructura

### TD-KEN-006-FIX — Dividir archivos y funciones grandes

**Prioridad:** 🟡 · **Puntos:** 5 (dividir por feature si excede)

**Objetivo:** FF-09 de 4 a 0; funciones > 60 líneas de 28 a ≤ 10.

**Criterios de aceptación**
- [ ] `TkdPaymentsSections.kt` (667) dividido, p. ej. `TkdCashFlowCard.kt`, `TkdEventCollectionCard.kt`, `TkdTransactionCard.kt`, `TkdPaymentsSections.kt`.
- [ ] `TkdPaymentDetailSections.kt` (590) dividido por tarjeta (pagador, cargo, método, recibo, historial).
- [ ] `TkdProfileProgressCards.kt` (352) y `TkdSignUpScreen.kt` (310) bajo 300 líneas.
- [ ] `TkdProfileAttendanceCard` (162), `TkdTransactionCard` (116), `TkdCashFlowCard` (108) y `TkdProfileHeroCard` (103) divididas en sub-secciones `private`.
- [ ] Sin cambio visual: previews y emulador idénticos antes/después.

### TD-KEN-007-FIX — Consistencia de paquetes y nombres

**Prioridad:** 🟢 · **Puntos:** 2

**Criterios de aceptación**
- [ ] `ui/present/` → `dashboard/` (imports actualizados, FF-08 sigue en 0).
- [ ] `CustomLinearProgress` → `TkdLinearProgress` con `@Preview` (FF-11 a 1).
- [ ] `TkdSurface` con `@Preview` (FF-11 a 0).
- [ ] `CLAUDE.md`, `README.md` y `03-standards.md §7` reflejan la estructura nueva.

### US-003 — (historia de producto del sprint)

---

## 8. Backlog

Estado revisado contra el código el 2026-11-19. Las abiertas también están en la base **Mejoras Kensho** de Notion.

| ID | Título | Tipo | Prioridad | Disparador | Estado (2026-11-19) |
|----|--------|------|:---------:|------------|---------------------|
| TEST-UI-001 | Compose UI tests de flujos: login inválido, selección de método de pago, confirmación de cobro, pase de lista | Habilitación | 🟡 | Tras TD-KEN-001-FIX | Abierta: los `Tkd<X>ContentTest` cubren carga y error, no estos flujos; no hay test de login |
| LINT-002 | Reglas Detekt propias `Kensho*` que reemplazan los greps de FF-03…FF-11 (`08-lint-rules.md` §4) | Habilitación | 🟡 | Tras LINT-001 | ✅ Hecha (2026-10-28): 15 reglas `Kensho*` en `:lint-rules` |
| CI-002 | Fitness functions como script versionado (`scripts/fitness.sh`) usado por CI y por la skill | Habilitación | 🟡 | Con LINT-001 | Abierta: no hay `scripts/` ni pipeline (va con TD-KEN-008) |
| PAY-001 | Persistir la confirmación de cobro y reflejarla en la lista de pagos | Producto | 🟡 | Primer almacenamiento local (ADR-017) | ✅ Hecha: el cobro va a `POST payments/charges/{chargeId}/payments` y la lista se recarga al volver (`ui/TkdPaymentRoutes.kt`) |
| PAY-002 | Detalle de cobro para todos los alumnos (hoy solo Kenji tiene datos *Fake*) | Producto | 🟢 | — | ✅ Hecha: `GET payments/charges/{chargeId}` para cualquier cargo |
| PAY-003 | Acciones "Recibo" y "Enviar aviso" en la lista de pagos | Producto | 🟢 | — | ✅ Hecha (2026-11-19): "Recibo" abre el detalle; "Enviar recordatorios" abre `payment-reminders` con los pendientes y cada uno se manda por WhatsApp o SMS desde el teléfono (el API no tiene recordatorios: el texto lo arma la app) |
| PAY-004 | "+ Registrar cobro" de la lista de pagos no hacía nada (`onRegisterPayment = { }` en `ui/TkdPaymentRoutes.kt`) | Producto | 🟡 | — | ✅ Hecha (2026-11-19): elige el filtro "Pendientes" y baja a los cargos; cada uno se cobra en su detalle |
| PAY-005 | "Descargar reporte del mes" del corte en Pagos no hacía nada | Producto | 🟡 | — | ✅ Hecha (2026-11-19): "Ver pagos del mes" abre los pagos cobrados en el mes con su total (`payment-report`, renglón `TkdPaymentHistoryRow`); el API no filtra por mes, la app filtra `status=PAID` por el mes de `asOf` |
| PAY-006 | Estado de cuenta del mes en PDF desde "Pagos de <mes>" | Producto | 🟡 | — | ✅ Hecha (2026-11-19): `PdfDocument` de Android (sin dependencias), guardado con el selector del sistema (`CreateDocument`, sin permisos); dojo e instructor del perfil guardado, pendiente y esperado del resumen de Pagos |
| NAV-001 | Unificar fechas del prototipo (lista de pagos 19-nov vs. detalle 22-oct) | Producto | 🟢 | — | Obsoleta: las fechas vienen del API (`asOf`) |
| ARCH-001 | Adoptar Hilt (ADR-016) | Habilitación | — | Primer repositorio real o VM con > 2 dependencias | ✅ Hecha (TD-KEN-012, saldada 2026-11-19) |
| ARCH-002 | Capa de red/persistencia (ADR-017) | Habilitación | — | Primer endpoint o dato que deba sobrevivir al cierre | ✅ Hecha; 4 secciones ocultas del resumen esperan su endpoint (TD-KEN-013) |
| ARCH-003 | Separar `:core:designsystem` (ADR-018) | Habilitación | — | > 15 pantallas o build > 2 min | Abierta, disparador cumplido: 19 pantallas `Tkd<X>Screen`; falta el ADR-018 |
| REL-001 | Preparar primer release a Play Store (ADR-019) | Habilitación | — | Decisión de producto | En curso: rama `release/v0.0.1` con firma y R8 (`mapping.txt`); falta el ADR-019 |

---

## 9. Registro de velocidad

| Sprint | Fechas | Puntos comprometidos | Puntos completados | % técnico | Score global (06) | Notas |
|--------|--------|:--------------------:|:------------------:|:---------:|:-----------------:|-------|
| 0 | hasta 2026-10-27 | — | — | — | 69 % | Línea base: 8 pantallas, corpus de gobernanza creado |
| 1 | | | | | | |
| 2 | | | | | | |
| 3 | | | | | | |
