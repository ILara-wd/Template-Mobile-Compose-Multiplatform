# Contexto de Desarrollo y Gobernanza de Kensho

Eres un asistente de desarrollo experto que trabaja sobre **Kensho**, la app Android de gestión de dojo (módulo único `:app`, namespace `dev.treecode.kensho`, Jetpack Compose + Material 3 + Navigation Compose). Tu comportamiento, comandos, decisiones técnicas y flujos de documentación están gobernados por `CLAUDE.md` y `docs/architecture/`. Esta skill **no repite** ese contenido: lo orquesta. Las reglas, la tabla de deuda `TD-KEN-XXX`, las cifras y los comandos viven **únicamente** en `CLAUDE.md` y `docs/architecture/`; copiar cifras aquí es lo que hace que las skills se desincronicen.

> Contrato: **MVVM + UDF** (`Tkd<X>ViewModel` con `StateFlow<Tkd<X>UiState>` → `Tkd<X>Screen` stateful → `Tkd<X>Content` stateless + `Tkd<X>Actions`), tokens **`Tkd*`** de `ui/theme/` como única casa de literales, sistema de diseño atómico en `components/atoms|molecules`, repositorios detrás de interfaces con *Fakes* (y `NetworkTkd<X>Repository` al migrar), Figma como fuente de verdad visual. **Hilt (ADR-016) y red/sesión (ADR-017) están aceptados** y se migran por repositorio (TD-KEN-012). Todavía **no** hay Room/caché offline, UseCases ni módulos: no los introduzcas sin el ADR que marca su disparador (`01-governance.md §9`).

## Flujo de esta skill

```
1. Identifica qué tipo de cambio vas a hacer
   (pantalla desde Figma · componente Tkd nuevo o variante · ViewModel/estado · repositorio/datos ·
    tokens/theme · navegación · tests · build/dependencias · deuda TD-KEN)
        ↓
2. ¿Vas a implementar una pantalla desde Figma?
   → Skill `figma:figma-design-to-code` + ADR-007 (recursos ic_tkd_*/img_tkd_* exportados,
     cabecera "Generado desde Figma", reutilizar solo coincidencias exactas, strings con nodo).
     Anatomía: el paquete `payments/` es la referencia (Screen/Content/Actions, Sections internal,
     ViewModel, model/Tkd<X>UiState, data/Tkd<X>Repository + Fake). Verifica en el emulador.
        ↓
3. ¿Vas a crear un componente compartido?
   → Busca antes en components/ (CMP-07). Si no existe: ./create_component.sh (esqueleto con
     KDoc, modifier y previews) en atoms/ (sin dominio) o molecules/.
        ↓
4. ¿Vas a escribir o modificar código?
   → CLAUDE.md §Convenciones + 03-standards.md (ARC/CMP/THM/A11Y/I18N/RES/STR/PERF/TEST/LOG/DEP/GIT).
     Puntos que más se rompen:
       · solo el Screen conoce al ViewModel; navegación solo en ui/TkdApp.kt vía Actions
       · VM: MutableStateFlow privado, StateFlow público, on<Evento>(), sin Context
       · modifier: Modifier = Modifier primer opcional, aplicado una vez a la raíz
       · TkdColors/TkdDimens/TkdTypography; nunca Color(0x…)/N.dp/N.sp fuera de ui/theme
       · contentDescription en todo Icon/Image; null solo decorativo y COMENTADO
       · stringResource, claves tkd_<feature>_<descripcion>, textos nuevos en español (es-MX)
       · sin LocalDate.now() en model/UI; dinero en centavos Int; un feature no importa otro
       · VM nuevo/migrado: @HiltViewModel + @Inject constructor, sin "= Fake…()"; ruta desde SavedStateHandle
       · red: suspend/Flow + TkdResult, relanzar CancellationException, DTO solo en data/remote,
         Loading/Error con @StringRes, @IoDispatcher inyectado, logging HTTP BASIC solo en debug
        ↓
5. ¿Introduces un framework, un módulo o cambias una decisión?
   → Primero el ADR en 01-governance.md §4 (ver disparadores §9), luego el código.
        ↓
6. ¿Vas a escribir o modificar tests?
   → ViewModel/lógica pura: unit test JVM en app/src/test con las Fakes (sin Hilt).
     Red: mappers con fixtures JSON; NetworkTkd<X>Repository con MockWebServer (TEST-06).
     Flujo interactivo: Compose UI Test sobre Content en app/src/androidTest.
     Nombres sujeto_condicion_resultado; sin Thread.sleep ni fecha real (03-standards.md §9).
        ↓
7. ¿Tu cambio resuelve/agrava una TD-KEN o crea/cambia una regla?
   → Skill `kensho-governance:propagacion`
        ↓
8. ¿Necesitas el estado actual de una métrica de todo el proyecto?
   → Skill `kensho-governance:fitness-functions`
        ↓
9. ¿Vas a auditar una rama o un PR?
   → `/validate-pullrequest [PR_ID|rama] [tl|dev]` (por defecto: rama actual contra develop)
        ↓
10. Al terminar → Formato de salida (sección siguiente)
```

## Formato de salida al finalizar cualquier tarea

1. **Código modificado/creado:** archivos y cambios con ruta exacta. Si cambiaste la firma de un componente compartido de `components/`, indica si es compatible (parámetro nuevo al final con default) o lista los usos que actualizaste.
2. **Comandos de Gradle ejecutados** (ver `CLAUDE.md` §Comandos): `./gradlew :app:assembleDebug`, `:app:testDebugUnitTest`, `:app:lintDebug`, `:app:connectedDebugAndroidTest` (requiere emulador), `:app:installDebug` para verificar en el emulador contra Figma. Si no corriste algo, dilo; nunca afirmes que pasa sin haberlo corrido.
3. **Verificación visual:** si hubo UI, cómo se comparó contra el nodo de Figma (captura del emulador) o por qué no se hizo.
4. **Documentación:** `docs/architecture/` y `CLAUDE.md` solo si aplicó el paso 7. Si no aplicó, dilo explícitamente ("no hubo cambio de deuda ni de reglas; no se tocó `docs/architecture/`").
5. **Commit sugerido** (solo si el usuario pide commit): mensaje con la convención GIT-01 `<tipo>(<ámbito>): <descripción>` y ticket opcional `[KEN-<n>]`.
