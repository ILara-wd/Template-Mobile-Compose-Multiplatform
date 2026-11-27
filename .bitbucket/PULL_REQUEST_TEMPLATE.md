# Descripción
<!-- Qué cambia y por qué, en 2-4 líneas. -->

**Tipo de cambio:** <!-- feat | fix | refactor | test | docs | chore | build -->
**Ticket:** <!-- APP-<n> o N/A -->
**Diseño:** <!-- link al nodo de Figma o N/A -->
**Deuda tocada:** <!-- TD-0XX resuelta / reducida / ninguna -->

# Capturas (si hay UI)
| Diseño | Emulador |
|--------|----------|
| <!-- captura del diseño --> | <!-- captura de ./gradlew :app:installDebug --> |

# ¿Cómo se probó?
- [ ] `./gradlew :app:assembleDebug`
- [ ] `./gradlew :app:testDebugUnitTest` — tests nuevos: <!-- <X>ViewModelTest … -->
- [ ] `./gradlew :app:lintDebug`
- [ ] `./gradlew :app:connectedDebugAndroidTest` (si hay Compose UI tests)
- [ ] Revisión manual en el emulador: <!-- flujos probados -->

# Checklist (convenciones de CLAUDE.md)
- [ ] **Arquitectura:** solo el `Screen` conoce al ViewModel; la navegación sale por callbacks; ningún feature importa otro.
- [ ] **Componentes:** `modifier: Modifier = Modifier` primer opcional; componentes reutilizados antes de crear; previews `private`.
- [ ] **Tema:** sin `Color(0x…)`, `N.dp` ni `N.sp` fuera de `ui/theme/`.
- [ ] **Accesibilidad:** `contentDescription` en iconos informativos; `null` solo si es decorativo; `Role` en elementos clicables.
- [ ] **Textos:** en `strings.xml` (español de México), sin concatenar.
- [ ] **Tests:** ViewModels nuevos o modificados con unit test.
- [ ] **Tamaño:** archivos nuevos ≤ 300 líneas; sin código comentado.
- [ ] **Changelog:** fragmento en `changelog.d/<id>.<tipo>.md` si cambia `app/src/main`.
- [ ] Si cambió la arquitectura, una dependencia o una convención: `CLAUDE.md` actualizado y sincronizado.

# Asistencia de IA (si aplica)
- **Herramienta:** <!-- Claude Code / Copilot / N/A -->
- **Qué generó:** <!-- p. ej. pantalla, componentes, tests -->
- **Qué se revisó a mano:** <!-- lógica, accesibilidad, fidelidad visual, tests ejecutados -->
