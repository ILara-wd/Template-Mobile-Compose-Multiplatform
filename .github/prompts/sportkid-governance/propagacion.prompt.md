<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde .claude/commands/sportkid-governance/propagacion.md. NO EDITAR A MANO. -->
---
mode: agent
description: Obligaciones de Actualización de Archivos — propagación de cambios
---

## Obligaciones de Actualización de Archivos — propagación de cambios

Se activa cuando la tarea **resuelve, mitiga o agrava** una deuda técnica (`TD-SK-0XX`), crea o cambia una convención, toma una decisión de diseño (librería nueva, patrón nuevo como `ViewModel` + `UiState` o un módulo `core:data`, token nuevo con impacto global) o cambia algo que documentan el README o el catálogo. Una pantalla o componente nuevo que sigue las reglas existentes **sí** propaga a README y catálogo (filas 1 y 2 del flujo), pero no toca `CLAUDE.md`.

SportKid no tiene corpus de arquitectura ni ADRs: la gobernanza vive en `CLAUDE.md` (convenciones, reglas de dependencia, tabla `TD-SK-0XX` y este flujo) y el sistema de diseño en `README.md`. El orden es el de `CLAUDE.md` §Mapa de documentación → *Propagación de un cambio*; síguelo:

```
Cambio de token o componente → README §1–§3 → catálogo (generar.py)
Pantalla nueva o modificada  → README §4.2 → pantallas.json → capturar.py + generar.py
Convención o deuda nueva     → CLAUDE.md → regenerar espejos (AGENTS.md, .github/)
Cambio visible al usuario    → changelog.d/<fragmento>.md
```

Esta sección detalla **cuándo** se activa cada archivo y **con qué formato**:

| Archivo | Se activa cuando… | Formato de la entrada |
|---|---|---|
| `README.md` §1 | Token nuevo o cambiado (color, tipografía, forma, espaciado, icono) o átomo `Sk*` nuevo (§1.6) | Fila en la tabla de la subsección; nombre por rol, no por valor. Si Figma y el README difieren, **manda Figma** y se corrige el README |
| `README.md` §2–§3 | Molécula (`M<n>`) u organismo (`O<n>`) nuevo, extendido o subido a `core:ui` | Fila con código, nombre, composable y módulo; el código (M9, O21…) es el que muestra el catálogo |
| `README.md` §4.2 | Pantalla nueva o modificada (P01–P21 o siguiente libre) | Fila: plantilla (T1–T5), organismos clave, composable y desde dónde se abre, módulo |
| `README.md` §5 y §7 | Estado de dominio nuevo o formato nuevo (dinero, tiempos, fechas) | Fila del estado con etiqueta, icono y color; el `enum` va en `core:model` y su presentación una vez en `core:ui` |
| `README.md` §8 | Se cierra o abre un pendiente de diseño | Marca `[x]` o agrega `[ ]` |
| `docs/ui-catalogo/pantallas.json` | Pantalla nueva o forma nueva de abrirla (`Destination`, extras `sk_*`) | Entrada en orden de id con nodo de Figma, `captura.extras`, `scrolls`, `espera` |
| `docs/ui-catalogo/index.html` + `img/` | Cualquiera de las dos filas anteriores | **Generado:** `python3 scripts/ui-catalogo/capturar.py <ids>` + `python3 scripts/ui-catalogo/generar.py` (o `/catalogo-ui-sportkid actualizar <ids>`); nunca a mano |
| `CLAUDE.md` §Deuda técnica | Cambia la cifra de un `TD-SK-0XX` (recalcúlala con `sportkid-governance:fitness-functions`), se abre uno nuevo o se liquida | Actualiza la fila (descripción con cifra y evidencia, severidad 🟠/🟡/🟢) y la fecha de la línea base; un ID nuevo toma el siguiente libre (`grep -oE 'TD-SK-[0-9]+' CLAUDE.md \| sort -u`). Una deuda liquidada se quita de la tabla o pasa a "Fortalezas" |
| `CLAUDE.md` §Convenciones / §Arquitectura / §Comandos | Convención nueva u obsoleta, regla de dependencia, librería nueva (debe discutirse antes, §Convenciones "Dependencias"), patrón nuevo (ViewModel, `core:data`, Navigation Compose) o comando nuevo | Viñeta en su bullet; la decisión se registra aquí (no hay ADRs). Si cambia el patrón de pantalla, actualiza también el bloque de código de §Patrón de pantalla |
| `AGENTS.md`, `.github/copilot-instructions.md`, `.github/agents/`, `.github/prompts/` | Cambió `CLAUDE.md`, `.claude/agents/` o `.claude/commands/` | **Generados:** `python3 .github/scripts/sync_claude_to_copilot.py`; nunca a mano |
| `.claude/commands/validate-pullrequest*` | Una regla nueva o modificada debe verificarse en PRs | Ajusta la revisión en la familia `ff-*` correspondiente (`ARC`, `ST`, `CMP`, `THM`, `A11Y`, `I18N`, `RES`, `PF`, `DUP`, `DEP`, `COV`) y su fila en la tabla de familias |
| `.claude/commands/sportkid-governance/fitness-functions.md` | Una regla nueva es medible con un comando sobre todo el repo | Línea `FF-<n>` nueva con la regla de `CLAUDE.md` que mide; sin cifras fijas |
| `.claude/agents/sportkid-android-dev.md` | Cambia una convención que el agente aplica al escribir código | Ajusta la regla correspondiente |
| `.claude/skills/catalogo-ui-sportkid/SKILL.md` | Cambia cómo se abre una pantalla (extras `sk_*`) o el flujo del catálogo | Ajusta el paso |
| `PULL_REQUEST_TEMPLATE.md` | Una convención nueva entra al checklist del PR | Viñeta en el checklist |
| `changelog.d/<id>.<tipo>.md` | El PR cambia `src/main` con efecto visible para el usuario | Una línea en español para quien usa la app; `<id>` = issue de GitHub (`42`), deuda (`TD-SK-004`) o `+<slug>`; `<tipo>` ∈ `added`/`changed`/`refactored`/`deprecated`/`removed`/`fixed`/`security` (`changelog.d/template/TOWNCRIER_README.md`). `CHANGELOG.md` lo genera towncrier, nunca a mano |

**Una sola fuente:** `CLAUDE.md` §Deuda técnica es la única tabla viva de deuda; el README es la única fuente del sistema de diseño. Las skills, el agente y la plantilla de PR referencian (`ver CLAUDE.md §Deuda técnica`, `README §4.2`), no copian cifras ni tablas.

**Espejos:** después de modificar `CLAUDE.md`, `.claude/agents` o `.claude/commands`, corre `python3 .github/scripts/sync_claude_to_copilot.py` (no edites `AGENTS.md`, `.github/agents` ni `.github/prompts` a mano).

**Convenciones vigentes (ver `CLAUDE.md` §Convenciones)** — si cambian, se actualizan en todos los documentos a la vez: átomos `Sk*` en `core:designsystem`, piezas con dominio sin prefijo en `core:ui`, locales `private` en el feature; tokens `MaterialTheme.colorScheme` / `LocalSportColors` / `SportKidTheme.spacing` / `MaterialTheme.typography` / `MaterialTheme.shapes`; pantallas `<X>Screen` stateless con callbacks hacia `SportKidApp` (`app/.../MainActivity.kt`), holders `<X>State` con `rememberSaveable`, datos `<X>Samples` (Mateo y Sofía); `today: LocalDate = LocalDate.now()` solo como default de la pantalla; previews `@SkPreviews`; claves `<pantalla>_<descripcion>` en es-MX; 400 líneas por archivo nuevo y 60 por composable; commits `<tipo>(<ámbito>): <descripción>` con ámbito = módulo; fragmento `changelog.d/` por PR con cambios en `src/main`; PRs en GitHub contra `sport_kids`.
