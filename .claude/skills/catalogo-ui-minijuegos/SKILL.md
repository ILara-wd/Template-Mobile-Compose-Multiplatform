---
name: catalogo-ui-minijuegos
description: Catálogo de UI de MiniJuegos Hub (Android, Jetpack Compose, Atomic Design). Úsalo para saber qué tokens, átomos, moléculas, organismos, iconos y pantallas ya existen antes de implementar un diseño de Figma (qué reutilizar de core:designsystem y qué es local del feature), y para actualizar el catálogo HTML (docs/ui-catalogo/index.html) con capturas del emulador después de crear o cambiar pantallas. También cuando pregunten "¿ya tenemos un componente para…?", "¿dónde se usa X?" o pidan regenerar o abrir el catálogo.
argument-hint: "[consultar <pieza o nodo de Figma> | actualizar [P03 P04…]]"
---

# Catálogo de UI de MiniJuegos Hub

Entrada: `$ARGUMENTS`. Puede ser una pieza o un nodo de Figma a consultar, `actualizar` con o sin ids de pantalla, o vacío (en ese caso, deduce qué hace falta por la conversación).

Piezas del catálogo (todas relativas a la raíz del repo):

| Ruta | Qué es |
|---|---|
| `docs/ui-catalogo/index.html` | El catálogo. **Generado: no lo edites a mano.** |
| `docs/ui-catalogo/pantallas.json` | Lista de pantallas: id (P01…), nombre, composable `XScreen`, nodo de Figma (archivo `KqTFktvrRvCzO2idPga5xf`) y cómo abrirla (`captura.extras`, `scrolls`, `espera`). |
| `docs/ui-catalogo/img/` | Capturas `<id>-<n>.jpg` de 360 px de ancho. |
| `scripts/ui-catalogo/capturar.py` | Abre cada pantalla en el emulador con los extras de debug y la captura. |
| `scripts/ui-catalogo/generar.py` + `plantilla.html` | Leen el código de los módulos de `settings.gradle.kts`, el README, el manifiesto y los `res/drawable*`, y escriben `index.html`. |

Capas que muestra el catálogo (salen del paquete, no del nombre):

| Capa | Dónde |
|---|---|
| Átomo / Molécula / Organismo | `core/designsystem/.../{atom,molecule,organism}/` |
| App | `app/` (barra superior compartida, contenedor de juegos, composables de `Screen.kt`) |
| Feature | públicos de un `feature/*` (las `XScreen` se listan como pantallas, no como componentes) |
| Local | `private`/`internal` de un feature (normalmente su `component/`) |

## A. Consultar antes de implementar

Úsalo al empezar un diseño de Figma (junto con el flujo de `figma-design-to-code` y la sección «Cómo implementar una nueva pantalla de Figma» del README), o cuando pregunten si algo ya existe.

1. Para cada pieza del diseño, busca candidatos por nombre y por función:
   - en el README, sección *Design system* (tablas de Tokens, Átomos, Moléculas y Organismos);
   - en el código: `grep -rn "^fun [A-Z]\|^internal fun [A-Z]" core/designsystem feature app --include=*.kt`;
   - en el catálogo (pestaña Componentes), que también lista los **locales** de cada feature;
   - para íconos e imágenes, en la pestaña **Assets** (o `ls core/designsystem/src/main/res/drawable`). Compara por `pathData`; si el ícono ya está en `AppIcons`, reutilízalo (con `tint` si solo cambia el color) en vez de exportarlo otra vez de Figma.
   - para valores sueltos (colores, tamaños, radios, alfas), en la pestaña **Tokens**: `AppTheme.colors/typography/spacing/shapes/elevation`, `AppSize`, `AppAlpha`.
2. Compara al menos dos candidatos leyendo su firma (parámetros, variantes y slots). Elige el que necesite menos cambios aunque su nombre no encaje (p. ej. `StatTile(Compact)`, `SecondaryButton(Tonal)`, `AppTextField` con slot final, `SettingsNavigationRow`/`SettingsSwitchRow`).
3. Clasifica cada pieza:
   - **Reutilizar**: existe en `core:designsystem` y lo cubre. Da el nombre y los parámetros clave.
   - **Extender**: existe, pero falta un parámetro o una variante. Agrega un parámetro opcional (o un valor al enum de variante) con un valor por defecto que no cambie a quienes ya lo usan.
   - **Subir al design system**: hoy es local de un feature y el diseño nuevo lo repite en otro (los features no dependen entre sí). Va al paquete de su capa (`atom/`, `molecule/` u `organism/`) sin conocer modelos de ningún feature.
   - **Local**: solo lo usa esta pantalla; va `internal` en `feature/<x>/.../component/`, compuesto con átomos.
   - **Nuevo token**: solo si el valor no se puede expresar con los actuales (Figma no tiene variables; los tokens se derivaron de los valores del diseño).
4. Reglas del proyecto (README, *Convenciones*):
   - nada de colores, `dp` ni `sp` literales en componentes y pantallas: todo sale de `AppTheme.*`, `AppSize`, `AppAlpha` o `AppIcons`; `ColorPalette` es `internal`;
   - textos en `strings.xml` del módulo (es-MX, con `plurals`); código y KDoc en inglés;
   - números con punto de miles (`formatScore`), fechas en ms UTC (`formatUtcDate`);
   - `Route` (ViewModel + estado) + `Screen` stateless con `@Preview`; estado `@Immutable` en `StateFlow`;
   - accesibilidad: `contentDescription` en íconos interactivos, `Role` en clicables, `heading()` en encabezados;
   - nada de `!!`.

## B. Actualizar el catálogo (después de crear o cambiar pantallas)

1. **Pantalla nueva**: agrégala a `docs/ui-catalogo/pantallas.json` con el siguiente id (P08…), su composable `XScreen` y su nodo de Figma. Para abrirla directo:
   - si es una pantalla nueva de la app, agrega el caso a `DebugStart.StartScreen` en `app/.../MainActivity.kt` y haz que `TemplateApp`/`SignedInApp` la muestren con ese valor; el extra `mj_screen` usa el nombre del enum (`Login`, `Register`, `Home`, `Games`, `Records`, `Profile`, `Game`);
   - para un juego, `"mj_screen": "Game", "mj_game": "<id de GameCatalog>"`;
   - los extras solo funcionan en builds *debuggable*; release los ignora.
2. Instala la app y captura. El emulador debe estar encendido (AVD `Pixel_9a`, arranque en frío lento); si hay varios dispositivos, define `ANDROID_SERIAL` (p. ej. `emulator-5554`):
   ```bash
   ./gradlew :app:installDebug
   python3 scripts/ui-catalogo/capturar.py P03 P04   # solo las que cambiaron; sin ids captura todas
   ```
3. **Mira cada captura** con Read antes de seguir. Si salió a medio cargar, sube `espera`; si el scroll quedó corto o repetido, ajusta `scrolls`.
4. Genera el catálogo con `python3 scripts/ui-catalogo/generar.py`. El script imprime los conteos por capa.
5. Si una pieza aparece "Sin KDoc", agrégale un KDoc (inglés) cuya primera oración la describa. Esa oración es la descripción del catálogo. Después regenera.
6. Opcional: verifica el render con Chrome sin ventana y revisa la imagen:
   ```bash
   "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" --headless=new --window-size=1280,1800 \
     --virtual-time-budget=4000 --screenshot=<scratchpad>/catalogo.png "file://$PWD/docs/ui-catalogo/index.html#componentes"
   ```
7. Si se agregaron o cambiaron componentes del design system, actualiza su fila en el README (tablas de *Átomos*, *Moléculas* u *Organismos*) y, si es pantalla, *Pantallas implementadas*. El catálogo muestra la etiqueta «README · <sección>» solo para lo documentado ahí.

## C. Responder preguntas sobre el catálogo

- "¿Dónde se usa X?": la ficha del componente en el catálogo, o `grep -rn "X(" --include=*.kt core feature app`.
- "¿Qué assets no se usan?": pestaña Assets, filtro «Sin uso».
- "¿Qué pantallas faltan?": compara `pantallas.json` con la tabla *Pantallas implementadas* del README.
- "¿Qué componentes no están documentados?": en la pestaña Componentes, los de core sin etiqueta «README · …».
- Para enseñar el catálogo, ábrelo en el navegador (`open docs/ui-catalogo/index.html`). No lo publiques fuera del repo sin permiso.
