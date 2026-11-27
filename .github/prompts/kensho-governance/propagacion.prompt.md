<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde .claude/commands/kensho-governance/propagacion.md. NO EDITAR A MANO. -->
---
mode: agent
description: Obligaciones de Actualización de Archivos — propagación de cambios arquitectónicos
---

## Obligaciones de Actualización de Archivos — propagación de cambios arquitectónicos

Se activa cuando la tarea **resuelve, mitiga o agrava** una deuda técnica (`TD-KEN-XXX`), crea o cambia una regla operativa, o toma una decisión de diseño (patrón nuevo, framework nuevo, token nuevo con impacto global, cambio de convención). No se activa para una pantalla o componente nuevo que sigue las reglas existentes sin mover métricas: en ese caso no toques `docs/architecture/`.

El orden del flujo está en `CLAUDE.md` §Flujo 2 — Propagación de cambios; síguelo. Esta sección detalla **cuándo** se activa cada paso y **con qué formato**:

| Documento | Se activa cuando… | Formato de la entrada |
|---|---|---|
| `docs/architecture/06-compliance-report.md` | Cambió la métrica de un `TD-KEN-XXX` o de una fitness function (FF-01..FF-12) | Hallazgo con `archivo:línea`, severidad (🔴/🟠/🟡) y nueva cifra |
| `docs/architecture/01-governance.md` | Se crea un ADR (§4) — p. ej. al cumplirse un disparador de §9 (Hilt, red, persistencia, módulos) —, se abre/cierra un `TD-KEN-XXX` (§6) o cambia una fitness function (§7) | ADR con Contexto/Decisión/Descartado/Consecuencias/Estado; fila de TD con severidad y evidencia. Los ADRs no se editan: uno nuevo *supersede* al anterior |
| `docs/architecture/03-standards.md` | Se fija una regla operativa nueva u obsoleta una existente | Regla en su familia (`ARC`/`CMP`/`THM`/`A11Y`/`I18N`/`RES`/`STR`/`PERF`/`TEST`/`LOG`/`DEP`/`GIT`) con 🔒 o ⚠️ y fila en la tabla §13 si es verificable con grep |
| `docs/architecture/04-detailed-rules.md` | La regla necesita un ejemplo para ser verificable en code review | Anti-patrón con bloque INCORRECTO / CORRECTO usando código real de Kensho |
| `docs/architecture/05-code-review-guide.md` | La regla es auditable con un comando reproducible | Comando de búsqueda + plantilla de comentario de PR |
| `docs/architecture/08-lint-rules.md` | La regla es automatizable (Detekt/Android Lint) | Regla con severidad, configuración y estado |
| `docs/architecture/07-user-stories.md` | La deuda cambió de alcance o se liquidó una historia | Solo si el usuario lo pide en el turno actual |
| `docs/architecture/02-best-practices.md` | Cambia el "por qué" (p. ej. se adopta DI o modularización) | Texto conceptual, sin cifras |
| `CLAUDE.md` | Cualquiera de los anteriores cambia una cifra de "Deuda técnica", una convención o un comando | Actualiza la fila del ID; es la tabla viva, no la repliques en skills |
| `.claude/commands/validate-pullrequest*` | Una regla nueva/modificada debe verificarse en PRs | Ajusta la revisión en la familia `ff-*` correspondiente y su fila en la tabla 5.1 |
| `.claude/agents/kensho-android-dev.md` | Cambia una convención que el agente aplica al escribir código | Ajusta la regla correspondiente |

**Una sola fuente:** `CLAUDE.md` (y `06-compliance-report.md` para el detalle) es la única tabla viva de deuda. Las demás skills referencian (`ver CLAUDE.md §Deuda técnica`), no copian cifras.

**Copilot:** después de modificar `.claude/agents` o `.claude/commands`, regenera los espejos con `python3 .github/scripts/sync_claude_to_copilot.py` (no edites `.github/agents` ni `.github/prompts` a mano).

**Convenciones vigentes (2026-10-27)** — si cambian, se actualizan en todos los documentos a la vez: tokens `TkdColors`/`TkdDimens`/`TkdTypography`/`TkdBeltPalette`; pantallas `Tkd<X>Screen` + `Tkd<X>Content` + `Tkd<X>Actions`; VM con `StateFlow` y defaults *Fake* en constructor; previews `private` con `widthDp = 390`; claves `tkd_<feature>_<descripcion>`; idioma base es-MX; 300 líneas por archivo nuevo (bloquea) y 60 por `@Composable` (advertencia); commits `<tipo>(<ámbito>): <descripción> [KEN-<n>]`; fragmento `changelog.d/<id>.<tipo>.md` por PR con cambios en `app/src/main`; `./gradlew detekt` limpio y baseline que solo se encoge; PRs contra `develop` en Bitbucket.
