# 📚 Documentación arquitectónica — Kensho

> **Versión del corpus:** v1.1 · **Última actualización:** 2026-10-30 (ADR-016 Hilt y ADR-017 red/sesión aceptados; línea base 2026-10-27)
> **Proyecto:** app Android de gestión de dojo de Taekwondo · un solo módulo `:app` (`dev.treecode.kensho`) · Jetpack Compose + Material 3
> **Arquitectura:** MVVM con flujo unidireccional — `ViewModel` + `StateFlow<UiState>` → `Screen` → `Content` → componentes `Tkd*`

Este directorio contiene los **8 documentos arquitectónicos** de Kensho, numerados por orden de lectura recomendado.

> **¿Por qué existe este directorio?**
> El proyecto está empezando y el código ya sigue buenas convenciones (separación `Screen`/`Content`, tokens propios, modelos inmutables, recursos generados desde Figma). Este corpus las **deja por escrito con IDs citables** antes de que la app crezca, registra la deuda técnica desde el primer día y define los disparadores para introducir DI, red, persistencia o módulos sin adelantarlos.

---

## Índice por rol

| # | Documento | Rol | Cuándo abrirlo |
|---|-----------|-----|----------------|
| 01 | [`01-governance.md`](01-governance.md) | **Autoritativo** — ADRs, deuda `TD-KEN-0XX`, fitness functions, hoja de ruta | Decisiones de diseño, citar un ADR, saber cuándo dar el siguiente paso |
| 02 | [`02-best-practices.md`](02-best-practices.md) | **Marco conceptual** — el porqué de UDF, tokens, accesibilidad, Figma → código | Onboarding, discusiones de diseño |
| 03 | [`03-standards.md`](03-standards.md) | **Operativo** — reglas 🔒/⚠️ (`ARC`, `CMP`, `THM`, `A11Y`, `I18N`, `RES`, `STR`, `PERF`, `TEST`, `LOG`, `DEP`, `GIT`) | Escribir código, citar una regla en un PR |
| 04 | [`04-detailed-rules.md`](04-detailed-rules.md) | **Verificable** — ejemplos correctos/incorrectos y anti-patrones `AP-XX` | Buscar cómo se ve una regla en código |
| 05 | [`05-code-review-guide.md`](05-code-review-guide.md) | **Guía de campo** — checklists, greps y plantillas de comentario | Revisar un PR paso a paso |
| 06 | [`06-compliance-report.md`](06-compliance-report.md) | **Auditoría empírica** — cifras reales, score por dominio, plan de acción | Ver el estado actual, priorizar deuda |
| 07 | [`07-user-stories.md`](07-user-stories.md) | **Backlog** — historias `TD-KEN-0XX-FIX`, habilitación, DoD, plantilla, uso de IA | Planear un sprint, redactar una historia |
| 08 | [`08-lint-rules.md`](08-lint-rules.md) | **Automatización** — Detekt con las reglas `Kensho*` de [`lint-rules/`](../../lint-rules/README.md) (activo), Android Lint y CI (plan) | Entender un hallazgo de `./gradlew detekt`, añadir una regla |
| — | [`changelog.d/template/TOWNCRIER_README.md`](../../changelog.d/template/TOWNCRIER_README.md) | **Proceso** — fragmentos de changelog y publicación de versiones con towncrier (GIT-05) | Abrir un PR, publicar una versión |
| — | [`FONT_SCALE_ANALISIS.md`](FONT_SCALE_ANALISIS.md) | **Análisis temático** — escala de fuente a 150/200 %, auditoría, herramienta `TkdFontScale` y verificación | Componentes o pantallas con texto en espacio fijo (A11Y-06) |

---

## Orden de lectura recomendado

### Para alguien nuevo en Kensho

```
README.md (raíz)                 ← qué es la app y cómo ejecutarla
    ↓
CLAUDE.md (raíz)                 ← contexto técnico inmediato
    ↓
02-best-practices.md             ← marco mental
    ↓
01-governance.md                 ← decisiones (ADRs) y deuda
    ↓
03-standards.md                  ← reglas del día a día
    ↓
06-compliance-report.md          ← estado real
```

### Para implementar una pantalla nueva desde Figma

```
01-governance.md ADR-002, ADR-007   ← patrón de pantalla y flujo Figma → código
    ↓
03-standards.md §1, §2, §5, §6      ← ARC, CMP, I18N, RES
    ↓
payments/ (código de referencia)     ← ejemplo canónico
    ↓
07-user-stories.md §2               ← Definición de Hecho
```

### Para revisar un PR

```
05-code-review-guide.md             ← entrada principal
    ↓ (citar al rechazar)
03-standards.md                     ← regla 🔒/⚠️ con ID
    ↓ (si hace falta ejemplo)
04-detailed-rules.md (AP-XX)        ← anti-patrón con código
```

### Para registrar una decisión o una deuda

```
06-compliance-report.md   ← registrar el hallazgo con cifra
    ↓
01-governance.md          ← ADR nuevo o TD-KEN-0XX
    ↓
03-standards.md           ← regla derivada
    ↓
04-detailed-rules.md      ← anti-patrón
    ↓
05-code-review-guide.md   ← grep + plantilla de comentario
    ↓
08-lint-rules.md          ← si se puede automatizar
    ↓
07-user-stories.md        ← historia para resolverla
    ↓
CLAUDE.md                 ← contexto consolidado (al final)
```

La skill `/kensho-governance:propagacion` guía este recorrido.

---

## Decisiones arquitectónicas activas (resumen)

| ADR | Decisión | Estado |
|-----|----------|--------|
| ADR-001 | Un solo módulo `:app`, paquetes por feature, sin imports entre features | ✅ Activo |
| ADR-002 | MVVM + UDF con `Screen` / `Content` / `Actions` | ✅ Activo |
| ADR-003 | `UiState` y modelos `@Immutable` en `model/`; dinero en centavos `Int` | ✅ Activo |
| ADR-004 | Tokens `TkdColors` / `TkdDimens` / `TkdTypography` / `TkdBeltPalette` como única casa de literales | ⏳ Casi completo (TD-KEN-004) |
| ADR-005 | Sistema de diseño atómico en `components/` (átomos agnósticos, moléculas, prefijo `Tkd`) | ✅ Activo |
| ADR-006 | Repositorios interface + *Fake*; fecha de referencia inyectada | ⏳ Parcial (TD-KEN-005); default a la *Fake* reemplazado por ADR-016 |
| ADR-007 | Flujo Figma → código (vectores generados, reutilizar solo coincidencias exactas) | ✅ Activo |
| ADR-008 | Accesibilidad como parte del contrato | ⏳ Parcial (TD-KEN-002) |
| ADR-009 | Textos en `strings.xml`, español de México como idioma base | ⏳ Parcial (TD-KEN-011) |
| ADR-010 | Previews como documentación viva | ⏳ Casi completo |
| ADR-011 | Sin framework de DI mientras todo sea *Fake* | 🔁 Reemplazado por ADR-016 |
| ADR-012 | Sin logging en `main` hasta definir un wrapper | ✅ Activo |
| ADR-013 | Estrategia de pruebas: unit para VMs y lógica pura, Compose UI para flujos | ✅ Activo (TD-KEN-001 saldada 2026-11-19) |
| ADR-014 | Navegación centralizada en `TkdApp` | ✅ Activo |
| ADR-015 | Texto escalable hasta 200 % con topes explícitos (`TkdFontScale`) | ✅ Activo |
| ADR-016 | Inyección de dependencias con Hilt (KSP), VMs sin default a la *Fake*, dispatchers inyectados | ✅ Aceptado — implementado (TD-KEN-012 saldada 2026-11-19) |
| ADR-017 | Capa de red (Retrofit + OkHttp + kotlinx.serialization), `TkdResult`, sesión en DataStore + Tink | ✅ Aceptado — implementado; 4 secciones ocultas del resumen esperan su endpoint (TD-KEN-013) |
| ADR-020 | Mensaje de error del API (`{code, message}`) en pantalla con respaldo `tkd_error_*` | ✅ Aceptado |

Detalle completo y alternativas descartadas en [`01-governance.md §4`](01-governance.md#4-decisiones-arquitectónicas-adr).

---

## Deuda técnica (resumen)

| ID | Descripción | Severidad |
|----|-------------|-----------|
| TD-KEN-001 | ✅ Saldada (2026-11-19): 22/22 ViewModels con unit test | ✅ |
| TD-KEN-002 | 65 `contentDescription = null` sin comentario | 🟡 Medio |
| TD-KEN-003 | Restos de plantilla (tema Material, `MainActivity` comentado, dependencia duplicada) | 🟡 Medio |
| TD-KEN-004 | 4 colores, 1 `.dp` y 1 formato sin locale fuera de tokens | 🟢 Bajo |
| TD-KEN-005 | `LocalDate.now()`/`LocalTime.now()` como default en 5 lugares | 🟡 Medio |
| TD-KEN-006 | 4 archivos > 300 líneas y 28 funciones > 60 líneas | 🟡 Medio |
| TD-KEN-007 | Inconsistencias de paquetes y nombres (`ui/present/`, `CustomLinearProgress`) | 🟢 Bajo |
| TD-KEN-008 | Sin CI (Detekt + reglas `Kensho*` ya activos en local) | 🟠 Alto |
| TD-KEN-009 | Dependencias desalineadas, `mavenLocal()`, `uikit` sin uso | 🟡 Medio |
| TD-KEN-010 | `Validate.kt` con regex permisiva y sin tests | 🟢 Bajo |
| TD-KEN-011 | Idioma mixto en `strings.xml` (≈133 cadenas en inglés) | 🟡 Medio |
| TD-KEN-012 | ✅ Saldada (2026-11-19): Hilt y red (ADR-016/017) en 8/9 repositorios, 0 defaults a la *Fake*, fotos por URL con Coil | ✅ |
| TD-KEN-013 | Resumen en parte sin red: lo visible sale de `me/profile`; sesión en vivo, métricas, filtro y exámenes siguen *Fake* y ocultas (`AUTH_STATIC_MOCK`); bloqueada por `GET /dojo/overview` | 🟢 Bajo |

Tabla con evidencia en [`01-governance.md §6`](01-governance.md#6-registro-de-deuda-técnica); historias en [`07-user-stories.md`](07-user-stories.md).

---

## Estado de cumplimiento (línea base 2026-10-27)

```
GLOBAL  ███████░░░  69 %  🟡
```

**Fortalezas:** 0 `!!`, 0 logs, 0 imports entre features, 8/8 pantallas con `Screen`/`Content` y `collectAsStateWithLifecycle`, modelos `@Immutable`, 74 previews.
**Brechas:** pruebas (5 %), proceso/CI (30 %), accesibilidad (55 %), idioma (60 %).

Desglose por dominio en [`06-compliance-report.md`](06-compliance-report.md).

---

## Política de mantenimiento

- **Atomicidad:** un cambio que afecta a 2+ documentos se propaga en el mismo PR.
- **Versiones:** cada documento sube su versión con cada cambio de contenido normativo (menor) o de decisión (mayor).
- **ADRs inmutables:** cambiar una decisión = ADR nuevo que *supersede* al anterior.
- **Cifras con fuente:** toda cifra del corpus sale de un comando del apéndice de `06-compliance-report.md`; al cambiarla, se vuelve a ejecutar.
- **`CLAUDE.md` se actualiza al final** — refleja el estado consolidado para Claude Code (y `AGENTS.md` / `.github/copilot-instructions.md` para otros asistentes).
- Si un cambio no encuentra lugar natural en estos 8 documentos, probablemente no es arquitectónico: va al `README.md` o al KDoc del código.

---

## Enlaces relacionados

- [`../../CLAUDE.md`](../../CLAUDE.md) — guía técnica para Claude Code
- [`../../README.md`](../../README.md) — entrada del repositorio
- Skills del repo: `/kensho-governance`, `/validate-pullrequest`, `/tech-story-architect`
- Figma: archivo **Prototipo-Demo-UiKit** (`KqTFktvrRvCzO2idPga5xf`)
