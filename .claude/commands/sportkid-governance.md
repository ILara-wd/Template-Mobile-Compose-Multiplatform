---
description: Contexto de desarrollo y gobernanza de SportKid (Android, Jetpack Compose multimódulo). Orquesta CLAUDE.md, README y el catálogo de UI al implementar pantallas desde Figma, crear componentes Sk o de core:ui, tocar tokens, navegación, tests o deuda TD-SK.
---

# Contexto de Desarrollo y Gobernanza de SportKid

Eres un asistente de desarrollo experto que trabaja sobre **SportKid**, la app Android para que los padres lleven agenda, progreso y gastos de sus hijos en Taekwondo, Natación y Atletismo (Jetpack Compose + Material 3, multimódulo: `:app`, `core:{designsystem, model, ui}`, `feature:{auth, athlete, agenda, home, tkd, swimming, athletics, sharing, settings, expenses}`). Tu comportamiento, comandos, decisiones técnicas y flujos de documentación están gobernados por `CLAUDE.md` (convenciones, reglas de dependencia, deuda `TD-SK-0XX` y propagación) y `README.md` (sistema de diseño: tokens, átomos, M/O/T/P, estados y formatos). Esta skill **no repite** ese contenido: lo orquesta. Las reglas, la tabla de deuda, las cifras y los comandos viven **únicamente** en `CLAUDE.md` y el README; copiar cifras aquí es lo que hace que las skills se desincronicen.

> Contrato: **fase de UI Kit**. Pantallas `<X>Screen` stateless respecto a la app (datos + callbacks, sin `NavController` ni imports de otro feature), estado local en holders `@Stable` `<X>State` con `rememberSaveable`, datos de ejemplo en `object <X>Samples` (Mateo y Sofía), navegación por estado en `SportKidApp` (`app/.../MainActivity.kt`, `enum Destination` + `BackHandler`), átomos `Sk*` sin dominio en `core:designsystem`, piezas con dominio en `core:ui`, tokens de `MaterialTheme` / `LocalSportColors` / `SportKidTheme.spacing`, Figma (`KqTFktvrRvCzO2idPga5xf`) como fuente de verdad visual. Todavía **no** hay ViewModels, capa de datos, DI, red, Navigation Compose ni tests reales: no los introduzcas sin decisión explícita del usuario y sin registrarla en `CLAUDE.md` (§Patrón de pantalla y §Convenciones "Dependencias").

## Flujo de esta skill

```
1. Identifica qué tipo de cambio vas a hacer
   (pantalla desde Figma · átomo Sk o variante · pieza de core:ui · holder de estado / Samples ·
    tokens/theme · navegación en SportKidApp · tests · build/dependencias · deuda TD-SK)
        ↓
2. ¿Vas a implementar una pantalla desde Figma?
   → CLAUDE.md §Flujo Figma → código: skill `figma:figma-design-to-code` + `get_design_context`
     (descarga los assets de inmediato), SVG → VectorDrawable `ic_<feature>_<nombre>.xml`
     comparado con `diff` contra los existentes. Modelos + Samples → holder si hay formulario →
     Screen con secciones private y @SkPreviews → Destination y callbacks en SportKidApp.
     Verifica en el emulador (ANDROID_SERIAL=emulator-5554, extras sk_*) contra la captura de Figma.
        ↓
3. ¿Vas a crear o extender un componente?
   → Busca antes: skill `/catalogo-ui-sportkid consultar <pieza>` (README §1.6, §2, §3 y el catálogo).
     Reutilizar · extender (parámetro nuevo al final con default) · subir a core:ui · local private ·
     átomo nuevo con ./create_component.sh Sk<Nombre> en core:designsystem.
        ↓
4. ¿Vas a escribir o modificar código?
   → CLAUDE.md §Arquitectura + §Convenciones (y `/ub-mode`). Puntos que más se rompen:
       · feature:* nunca importa otro feature; core:designsystem nunca depende de core:model
       · navegación solo en SportKidApp vía callbacks; ninguna pantalla recibe NavController
       · modifier: Modifier = Modifier primer opcional, aplicado a la raíz; slots @Composable al final
       · colorScheme / LocalSportColors / SportKidTheme.spacing / typography / shapes;
         nunca Color(0x…) fuera de core/designsystem/theme/; nada de N.dp nuevos en features
       · contentDescription desde recursos en iconos con significado; null solo decorativo;
         Role en filas seleccionables; objetivos ≥ 48dp; texto a fontScale 1.3
       · stringResource en el strings.xml del módulo dueño, claves <pantalla>_<descripcion>,
         es-MX, plurals, sin concatenar; vocabulario de estados de README §5 y formatos de §7
       · "hoy" entra como parámetro (today: LocalDate = LocalDate.now() solo como default de la pantalla)
       · sin !!, GlobalScope, runBlocking, Log/println, código comentado ni emojis en la UI
       · archivos nuevos ≤ 400 líneas; composables > 60 líneas → secciones private
        ↓
5. ¿Introduces una librería, un módulo o un patrón nuevo (ViewModel, core:data, Navigation, Hilt…)?
   → Primero se discute con el usuario y se registra en CLAUDE.md (no hay ADRs); la dependencia va en
     gradle/libs.versions.toml. Luego el código.
        ↓
6. ¿Vas a escribir o modificar tests?
   → `/ub-mode` §TDD: primero lo que es Kotlin puro (holders <X>State, Saver, DateLabels, formatos,
     core:model) con JUnit en el src/test del módulo (./gradlew :feature:<x>:testDebugUnitTest);
     interacción con Compose UI test sobre la Screen con Samples (connectedDebugAndroidTest).
     Lo puramente visual se verifica con @SkPreviews + captura del emulador vs Figma.
        ↓
7. ¿Tu cambio toca README, catálogo, una TD-SK o una convención?
   → Skill `sportkid-governance:propagacion`
        ↓
8. ¿Necesitas el estado actual de una métrica de todo el proyecto?
   → Skill `sportkid-governance:fitness-functions`
        ↓
9. ¿Vas a auditar una rama o un PR?
   → `/validate-pullrequest [PR|rama]` (por defecto: rama actual contra sport_kids)
        ↓
10. Al terminar → Formato de salida (sección siguiente)
```

## Formato de salida al finalizar cualquier tarea

1. **Código modificado/creado:** archivos y cambios con ruta exacta. Si cambiaste la firma de un componente compartido de `core:designsystem` o `core:ui`, indica si es compatible (parámetro nuevo al final con default) o lista los usos que actualizaste.
2. **Comandos de Gradle ejecutados** (ver `CLAUDE.md` §Comandos principales): `./gradlew :app:assembleDebug`, `:app:testDebugUnitTest` (o el `testDebugUnitTest` del módulo), `./gradlew lint`, `:app:connectedDebugAndroidTest` (requiere emulador), `:app:installDebug` para verificar en el emulador contra Figma. Si no corriste algo, dilo; nunca afirmes que pasa sin haberlo corrido.
3. **Verificación visual:** si hubo UI, cómo se comparó contra el nodo de Figma (captura del emulador con `ANDROID_SERIAL=emulator-5554` y extras `sk_*`) o por qué no se hizo.
4. **Documentación:** README (§1–§5, §4.2), `docs/ui-catalogo/pantallas.json` + catálogo regenerado, `CLAUDE.md` y `changelog.d/` según el paso 7. Si no aplicó, dilo explícitamente ("no hubo cambio de deuda ni de convenciones; no se tocó `CLAUDE.md`").
5. **Commit sugerido** (solo si el usuario pide commit): `<tipo>(<ámbito>): <descripción en imperativo>` con tipo `feat|fix|refactor|test|docs|chore|build|revert` y ámbito = módulo (`feat(home): …`), y referencia opcional al issue de GitHub (`#<n>`).
