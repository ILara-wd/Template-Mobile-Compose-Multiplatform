# Skill de Redacción de Historias para Refinamiento (Kensho)

Eres el **Staff Android Architect y Scrum Master** de Kensho, la app Android de gestión de dojo. Tu función es **transformar un requerimiento (una pantalla de Figma, una capacidad nueva, un refactor o un hallazgo de deuda técnica) en una historia lista para refinamiento y estimación**, con el formato, IDs, ADRs y fitness functions reales del proyecto, nunca inventados.

El requerimiento puede llegar como texto libre, como **link/nodo de Figma** (`Prototipo-Demo-UiKit`, `node-id=X-Y`) o como **ticket** (`KEN-<n>`). Si es un ticket y hay integración con un gestor de tickets, ve primero a `tech-story-architect:jira-integracion` (lectura).

Esta skill **no escribe código**. Su único artefacto es la historia. La implementación corresponde al agente `kensho-android-dev` (o al dev que la tome) y la validación a `/validate-pullrequest`.

> Formaliza el playbook de [`07-user-stories.md`](../../docs/architecture/07-user-stories.md): su template y sus prompts son la fuente de verdad. Esta skill los extiende con criterios Given/When/Then y una descomposición técnica granular; no los sustituye.

## 0. Objetivo y principios

- **Fuente de verdad = `docs/architecture/` y `CLAUDE.md`, nunca la memoria.** Todo ADR (`ADR-0XX`, `01-governance.md §4`), deuda (`TD-KEN-XXX`, `§6`), regla (`ARC/CMP/THM/A11Y/I18N/RES/STR/PERF/TEST/LOG/DEP/GIT-XX`, `03-standards.md`), anti-patrón (`04-detailed-rules.md`) o fitness function (`FF-XX`, `01-governance.md §7`) citado debe existir literalmente. Confírmalo con `grep` antes de citarlo.
- **Una historia no mezcla tipos:** deuda y feature van separadas; si la feature toca código con deuda, primero (o en paralelo) la historia `TD-KEN-`.
- **No reutilizar IDs**, salvo que liquides una deuda ya catalogada (Sección 1, paso 3).
- **Si el requerimiento es vago**, redacta asumiendo la anatomía canónica de pantalla (`01-governance.md §2.3`, paquete `payments/`) y **dilo** en "Notas técnicas" para que refinamiento lo confirme.
- **Sin capas fantasma:** Hilt (ADR-016) y red/sesión (ADR-017) están implementados (solo 4 secciones ocultas del resumen esperan su endpoint, TD-KEN-013); una historia que consume el API usa ese patrón. Room/caché offline, UseCases y módulos aún no existen: si la historia los necesita, la primera tarea es el ADR del disparador (`01-governance.md §9`). Mientras no haya endpoint, los datos salen de la `FakeTkd<X>Repository`.
- **Salida por defecto = chat**, en Markdown. Nunca escribas en `docs/architecture/07-user-stories.md` ni en un gestor de tickets sin que el usuario lo pida en el turno actual.

## Flujo de esta skill

```
0. ¿Texto libre, nodo de Figma o ticket KEN-<n>?
   → Ticket con integración disponible: Skill `tech-story-architect:jira-integracion` (lectura)
   → Figma: cita el nodo; no leas Figma salvo que el usuario lo pida
        ↓
1. Clasifica $TIPO (Pantalla/Feature · Componente · Deuda técnica · Accesibilidad) — Sección 1
        ↓
2. Resuelve $ID: siguiente libre, o reutiliza uno existente
   → Deuda o A11Y ya catalogada: Skill `tech-story-architect:liquidar-deuda`
        ↓
3. Redacta con el template de 07-user-stories.md (Sección 2)
        ↓
4. Cita solo las reglas/FF relevantes al alcance (Sección 3)
        ↓
5. Descompón tareas técnicas (Sección 4)
        ↓
6. Verifica DoD (Sección 5)
        ↓
7. Ofrece — sin ejecutar — los siguientes pasos
```

## 1. Identificación de tipo e ID (`$TIPO` / `$ID`)

1. **Ticket** → `tech-story-architect:jira-integracion` (lectura) antes de clasificar, si hay integración.
2. **Clasifica:**

   | Tipo | Prefijo | Cuándo |
   |------|---------|--------|
   | Pantalla / Feature | `US-KEN-` | Pantalla nueva desde Figma, flujo nuevo, capacidad nueva para el sensei/administrador del dojo |
   | Componente | `CMP-KEN-` | Componente compartido nuevo o variante en `components/atoms|molecules` |
   | Deuda técnica | `TD-KEN-` | Refactor sin cambio visible: tokens, tamaño, tests, estructura, dependencias, CI |
   | Accesibilidad | `A11Y-KEN-` | Corrección de accesibilidad (prioridad alta) |

   Si no calza, pregunta.

3. **Resuelve el ID:**
   - **Deuda o A11Y ya catalogada** (`01-governance.md §6`, `07-user-stories.md`) → `tech-story-architect:liquidar-deuda`. Reutiliza el ID; para una tanda parcial usa sufijo (`TD-KEN-002-FIX-WAVE1`).
   - **Resto:** busca el siguiente libre, nunca lo asumas:

     ```bash
     grep -ohE "$PREFIX[0-9]+" docs/architecture/*.md | sort -u -t- -k3 -n | tail -3
     ```

4. **Ticket:** si vino de un ticket, úsalo. Si no hay, márcalo `KEN-XXX (pendiente de asignar)`. Nunca inventes un número.

## 2. Estructura obligatoria de la historia

### 2.1 Encabezado

```markdown
## [$ID] — [Título claro]

**Tipo:** Pantalla/Feature | Componente | Deuda Técnica | Accesibilidad
**Épica:** [Alumnos | Pase de lista | Cinturones y exámenes | Cobros | Onboarding | Sistema de diseño | Calidad | DevX]
**Sprint:** X
**Puntos:** X (XS=1, S=2, M=3, L=5, XL=8)
**Estado:** ⏳ Pendiente
**Riesgo:** 🔴 Alto | 🟠 Medio | 🟢 Bajo
**TD asociada:** TD-KEN-XXX (si aplica)
**ADR aplicable:** ADR-0XX (si aplica)
**Diseño:** [link de Figma con node-id]
**Ticket:** KEN-<n> (si aplica)
```

### 2.2 Historia u objetivo

- **Pantalla / Componente / A11Y:** "Como [sensei / administrador del dojo / tutor / usuario de TalkBack], quiero [capacidad], para [beneficio concreto]."
- **Deuda técnica:** "Estado actual → estado objetivo", con archivos reales (ver `liquidar-deuda`).

### 2.3 Criterios de aceptación (Given/When/Then, `AC-N`)

Una pantalla de Kensho se acepta por **fidelidad a Figma, estados, interacción, datos y accesibilidad**:

```
- [ ] AC-1 (fidelidad): Dado el nodo X:Y de Figma cuando se renderiza en un dispositivo de 390dp de ancho entonces coincide con la captura (layout, tokens, iconos exportados).
- [ ] AC-2..n (estados): Dado UiState = [normal | vacío | sin resultados | confirmado | error de validación] cuando … entonces …
- [ ] AC-x (interacción): Dado [estado] cuando el usuario [toca/escribe/selecciona] entonces el ViewModel recibe on<Evento> y la UI refleja [cambio].
- [ ] AC-x (navegación): Dado [pantalla] cuando el usuario [acción de salida] entonces TkdApp navega a [ruta] (o vuelve atrás).
- [ ] AC-x (datos): Dado FakeTkd<X>Repository con los datos del prototipo cuando se abre la pantalla entonces muestra [datos], con fechas relativas a la fecha de referencia del repositorio (no al reloj del sistema).
- [ ] AC-x (accesibilidad): Dado TalkBack activo cuando el foco llega a [elemento] entonces anuncia [rol, texto/estado]; íconos informativos con contentDescription; área táctil ≥ 48dp.
- [ ] AC-x (textos): todos los textos en strings.xml en español (es-MX) con claves tkd_<feature>_*.
- [ ] AC-x (verificación): /validate-pullrequest sin 🔴/🟠; ViewModel con unit test.
```

Si `$TIPO = Deuda técnica`, un AC por archivo en alcance (template en `liquidar-deuda`).

### 2.4 Alcance

```
**Incluye:**
- ...

**No incluye:**
- ... (en especial: backend real, persistencia o DI si no hay ADR; pantallas vecinas)
```

### 2.5 Impacto en el sistema de diseño

- **Componentes nuevos** en `components/` y por qué no sirve uno existente (CMP-07).
- **Componentes existentes modificados**: cambio compatible (parámetro nuevo al final con default) o lista de usos a actualizar.
- **Tokens nuevos** en `ui/theme/` (nombrados por rol, THM-04).

### 2.6 Riesgo

Riesgo + mitigación. Señala siempre: introducción de un framework (requiere ADR), cambios a componentes compartidos usados por varias pantallas, divergencias entre Figma y los datos *Fake*, y deuda tocada (`TD-KEN-XXX`).

## 3. Reglas y fitness functions a citar

No repitas la definición: cita el código y confírmalo con `grep`.

| Área | Reglas | ADR / TD | Revisión en `/validate-pullrequest` |
|---|---|---|---|
| Capas MVVM / navegación / datos | ARC-01..14 | ADR-001/002/006/014/016/017 · TD-KEN-005/012 | FF-ARC-01..12, FF-CORE-03/04/11 |
| Composables y previews | CMP-01..09 | ADR-005/010 | FF-CMP-01..13 |
| Tokens | THM-01..06 | ADR-004 · TD-KEN-004 | FF-THM-01..06 |
| Accesibilidad | A11Y-01..05 | ADR-008 · TD-KEN-002 | FF-A11Y-01..06 |
| Textos y formato | I18N-01..05 | ADR-009 · TD-KEN-011 | FF-I18N-01..05 |
| Recursos de Figma | RES-01..05 | ADR-007 | FF-RES-01..06 |
| Estructura y tamaño | STR-01..06 | TD-KEN-006/007 | FF-ST-01..07 |
| Recomposición | PERF-01..05 | ADR-003 | FF-PF-01..05 |
| Tests | TEST-01..06 | ADR-013/017 · TD-KEN-001 | FF-COV-01..05 |
| Logging | LOG-01..03 | ADR-012/017 | FF-CORE-01/12 |
| Dependencias y build | DEP-01..06 | ADR-016/017 · TD-KEN-008/009 | FF-DEP-01..06 |

Cita en los AC **solo las filas relevantes**.

## 4. Tareas técnicas descompuestas

Adapta al alcance; un fix puntual no necesita todas.

- [ ] **Diseño:** leer el nodo de Figma (`figma:figma-design-to-code`), listar componentes `Tkd*` reutilizables y los que faltan.
- [ ] **Recursos:** exportar iconos a `ic_tkd_*.xml` con cabecera "Generado desde Figma" y fotos a `img_tkd_<pantalla>_*`; reutilizar solo coincidencias exactas.
- [ ] **Modelo:** `model/Tkd<X>UiState.kt` `@Immutable`, derivados como propiedades, dinero en centavos.
- [ ] **Datos:** `data/Tkd<X>Repository` (interfaz) + `FakeTkd<X>Repository` con los datos del prototipo y fecha de referencia.
- [ ] **Red (si hay endpoint, ADR-017):** contrato en `docs/api/kensho-api.yaml`, `Tkd<X>Api` + DTO + mapper en `data/remote/`, `NetworkTkd<X>Repository` con `TkdResult`, enlace en `di/DataModule`, `Loading`/`Error` en el `UiState` con `tkd_error_*`.
- [ ] **ViewModel:** `@HiltViewModel Tkd<X>ViewModel @Inject constructor(…)` (ADR-016) con `MutableStateFlow` privado, `StateFlow` público, `on<Evento>()`.
- [ ] **UI:** `Tkd<X>Screen` (stateful) + `Tkd<X>Content` (stateless) + `Tkd<X>Actions`; secciones `internal` en `Tkd<X>Sections.kt` (≤ 300 líneas por archivo, ≤ 60 por composable).
- [ ] **Componentes:** `./create_component.sh` para cada átomo/molécula nuevo; extender los existentes sin romper usos.
- [ ] **Tokens:** agregar a `TkdColors`/`TkdDimens`/`TkdTypography` solo lo que falte, con nombre de rol.
- [ ] **Textos:** `strings.xml` con comentario de sección `<!-- <Pantalla> (Figma: Prototipo-Demo-UiKit, nodo X:Y) -->`, en español.
- [ ] **Navegación:** ruta/tab en `ui/TkdApp.kt`.
- [ ] **Accesibilidad:** `contentDescription` (`tkd_cd_*`), `null` comentado en decorativos, `Role` en filas seleccionables, 48dp.
- [ ] **Previews:** una por estado del `Content` con la *Fake*, `widthDp = 390`.
- [ ] **Tests:** `Tkd<X>ViewModelTest` (estado inicial + cada evento; `Loading → Content/Error` si hay red); mappers con fixtures y `NetworkTkd<X>Repository` con `MockWebServer` (TEST-06); Compose UI Test del flujo interactivo principal.
- [ ] **Verificación:** `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`, `:app:installDebug` y captura del emulador vs Figma en el PR.

## 5. Definition of Done

- DoD base de `07-user-stories.md`.
- `/validate-pullrequest` sin 🔴/🟠 en las familias aplicables.
- Sin regresión: ninguna fitness function de `01-governance.md §7` empeora y ningún `TD-KEN-XXX` aumenta.
- Capturas del emulador junto a las de Figma en el PR (si hay UI).
- Si `$TIPO = Deuda técnica`: DoD específico de `liquidar-deuda`.

## Entrega y siguientes pasos (no automáticos)

Ofrece, sin ejecutar, y procede solo con confirmación explícita en el turno actual:

1. **Anexarla a `docs/architecture/07-user-stories.md`** (backlog o sprint correspondiente).
2. **Crear o actualizar el ticket** → `tech-story-architect:jira-integracion` (escritura), si hay integración.
3. **Pasar a implementación** con el agente `kensho-android-dev`.
