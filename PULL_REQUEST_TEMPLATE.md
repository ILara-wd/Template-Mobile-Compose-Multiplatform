# Descripción
<!-- Qué cambia y por qué, en 2-4 líneas. -->

**Tipo de cambio:** <!-- feat | fix | refactor | test | docs | chore | build -->
**Módulos:** <!-- p. ej. feature:home, core:ui -->
**Pantalla:** <!-- P05, O4… (README §4.2) o N/A -->
**Diseño (Figma):** <!-- https://www.figma.com/design/KqTFktvrRvCzO2idPga5xf/Prototipo-Demo-UiKit?node-id=X-Y o N/A -->
**Deuda tocada:** <!-- TD-SK-0XX resuelta / reducida / ninguna -->

# Capturas (si hay UI)
| Figma | Emulador |
|-------|----------|
| <!-- captura del nodo --> | <!-- docs/ui-catalogo/img/<id>-1.jpg o captura manual --> |

# ¿Cómo se probó?
- [ ] `./gradlew :app:assembleDebug`
- [ ] `./gradlew :app:testDebugUnitTest` — tests nuevos: <!-- … o N/A -->
- [ ] `./gradlew lint`
- [ ] `./gradlew :app:connectedDebugAndroidTest` (si hay Compose UI tests)
- [ ] Revisión manual en el emulador (`ANDROID_SERIAL=emulator-5554`): <!-- flujos probados -->
- [ ] Previews `@SkPreviews` revisadas en claro, oscuro y fuente 1.3

# Checklist (CLAUDE.md · README §6–§7)
- [ ] **Módulos:** ningún `feature:*` importa otro feature; `core:designsystem` no depende de `core:model`; la navegación queda en `SportKidApp` vía callbacks.
- [ ] **Componentes:** reutilizados antes de crear (`/catalogo-ui-sportkid consultar`); átomos `Sk*` en `core:designsystem`, piezas con dominio en `core:ui`; `modifier: Modifier = Modifier` primer opcional.
- [ ] **Tokens:** `MaterialTheme.colorScheme` / `LocalSportColors` / `SportKidTheme.spacing` / `MaterialTheme.typography`; sin `Color(0x…)` fuera de `theme/`; los colores de deporte e hijos no cambian con el tema.
- [ ] **Estados:** vocabulario único de README §5; color nunca solo (icono o texto).
- [ ] **Textos:** en `strings.xml` del módulo, español de México, `plurals` para cantidades, sin concatenar; formatos de README §7.
- [ ] **A11Y:** `contentDescription` en iconos informativos; `null` solo decorativo; `Role` en filas seleccionables; objetivos ≥ 48dp.
- [ ] **Recursos:** iconos `ic_<feature>_*` convertidos de Figma; reutilizados de `core` si ya existían.
- [ ] **Datos de ejemplo:** Mateo y Sofía en `Samples` y previews; "hoy" como parámetro.
- [ ] **Docs:** README §4.2 y `docs/ui-catalogo/pantallas.json` actualizados; catálogo regenerado (`generar.py`).
- [ ] **Changelog:** fragmento en `changelog.d/<id>.<tipo>.md` si cambia `src/main`.
- [ ] **Sin** `!!`, `Log`/`println`, `runBlocking`/`GlobalScope` ni código comentado.
- [ ] Si cambió una convención o deuda: `CLAUDE.md` actualizado y `python3 .github/scripts/sync_claude_to_copilot.py` corrido.

# Asistencia de IA (si aplica)
- **Herramienta:** <!-- Claude Code / Copilot / N/A -->
- **Qué generó:** <!-- p. ej. secciones de la pantalla, vectores desde Figma -->
- **Qué se revisó a mano:** <!-- lógica, accesibilidad, fidelidad visual -->
