---
description: Catálogo de UI de Kensho (Android, Jetpack Compose). Úsalo para saber qué componentes, pantallas y tokens ya existen antes de implementar un diseño de Figma (qué reutilizar de components/atoms y components/molecules, qué es local del feature), y para actualizar el catálogo HTML (docs/ui-catalogo/index.html) con capturas del emulador después de crear o cambiar pantallas. También cuando pregunten "¿ya tenemos un componente para…?", "¿dónde se usa X?" o pidan regenerar o abrir el catálogo.
argument-hint: "[consultar <pieza o nodo de Figma> | actualizar [P10 P11…]]"
---

# Catálogo de UI de Kensho

Entrada: `$ARGUMENTS`. Puede ser una pieza o un nodo de Figma a consultar, `actualizar` con o sin ids de pantalla, o vacío (en ese caso, deduce qué hace falta por la conversación).

Las reglas de diseño (tokens, componentes, textos, accesibilidad, escala de fuente) viven en `CLAUDE.md` §Convenciones y `docs/architecture/03-standards.md`; esta skill las **orquesta**, no las copia.

Piezas del catálogo (relativas a la raíz del repo):

| Ruta | Qué es |
|---|---|
| `docs/ui-catalogo/index.html` | El catálogo. **Generado: no lo edites a mano.** |
| `docs/ui-catalogo/pantallas.json` | Lista de pantallas: id (P01…), nombre, composable, nodo de Figma (`figma.archivo` + `figma.nodo`) y cómo abrirla (`captura.pasos`, `scrolls`, `espera`). `figmaArchivos` da la clave de cada archivo: `uikit` = Prototipo-Demo-UiKit (`KqTFktvrRvCzO2idPga5xf`); `demo` = Prototipo-Demo (sin clave registrada, se muestra sin enlace). |
| `docs/ui-catalogo/img/` | Capturas `<id>-<n>.jpg` de 360 px de ancho. |
| `.claude/scripts/ui-catalogo/capturar.py` | Abre cada pantalla en el emulador con sus pasos y la captura. |
| `.claude/scripts/ui-catalogo/generar.py` + `plantilla.html` | Leen el código de `:app` y el manifiesto, y escriben `index.html`. |

Capas del catálogo (por paquete de `dev.treecode.kensho`):

| Capa | Paquete | Uso |
|---|---|---|
| Átomo | `components/atoms` | Sin dominio: `TkdPill`, `TkdCard`, `TkdIconTile`… |
| Molécula | `components/molecules` | Composiciones reutilizables, algunas con dominio (cinta, horario) |
| App | `ui/`, `nav/`, raíz | Shell y navegación (`TkdApp`) |
| Feature | `feature/*`, `students/`, `payments/`… | Públicos de un feature (`Tkd<X>Content`) |
| Local | igual, pero `private`/`internal` | Secciones de una pantalla |

## A. Consultar antes de implementar

Úsalo en el paso 2 del flujo Figma → código de `CLAUDE.md` (junto con `figma-design-to-code`), o cuando pregunten si algo ya existe.

1. Para cada pieza del diseño, busca candidatos por nombre y por función:
   - en el código: `grep -rn "^fun Tkd\|^internal fun Tkd" app/src/main/java/dev/treecode/kensho/components`;
   - en el catálogo (pestaña Componentes), que también lista los **locales** de cada feature con su KDoc y su firma;
   - en los tokens (pestaña Tokens): `TkdColors`, `TkdTypography`, `TkdDimens`, cintas de `TkdBeltPalette`.
2. Compara al menos dos candidatos leyendo su firma (parámetros y slots). Elige el que necesite menos cambios aunque su nombre no encaje. Ejemplos ya resueltos: `TkdTextBadge` sirve para badges con ícono (`leading`), `TkdSecondaryButton` para botones con ícono de color propio, `TkdIconTile` para cualquier ícono sobre un cuadro de color.
3. Clasifica cada pieza:
   - **Reutilizar**: existe en `components/` y lo cubre. Da el nombre y los parámetros clave.
   - **Extender**: existe, pero falta un parámetro o una variante. Parámetro nuevo al final y con default que no cambie a quienes ya lo usan; variantes con objeto de estilo o slot, no con booleanos (`CLAUDE.md` §Componentes).
   - **Subir a `components/`**: hoy es local de un feature y el diseño nuevo lo repite en otro (los features no se importan entre sí).
   - **Local**: solo lo usa esta pantalla; va `internal`/`private` en el paquete del feature, compuesto con átomos.
   - **Nuevo**: no existe y se va a reutilizar; va a `components/atoms|molecules` con prefijo `Tkd`, KDoc de una línea en español y `@Preview`.
4. Tokens: si falta un color, medida o estilo, agrégalo a `ui/theme/` con nombre por rol; nunca literales fuera de ahí. Revisa también los drawables `ic_tkd_*` con `diff` del `pathData` antes de agregar uno.

## B. Actualizar el catálogo (después de crear o cambiar pantallas)

1. **Pantalla nueva**: agrégala a `docs/ui-catalogo/pantallas.json` en orden de id, con su composable `Tkd<X>Screen` y su nodo de Figma. En `captura.pasos` describe cómo llegar a ella desde el inicio de la app:
   - `{"tocar": "Mi Dojo"}`: toca el nodo con ese texto (etiquetas de la barra inferior, botones);
   - `{"tocar": "/^Cinta /"}`: el primero cuyo texto cumpla la expresión regular (para abrir un elemento de una lista);
   - `{"tocarDesc": "Perfil"}`: por `contentDescription` (íconos sin texto);
   - `{"deslizar": 2}`: desliza la pantalla hacia arriba esas veces, para llegar a un elemento más abajo de una lista;
   - `"esperar"` en un paso para dar tiempo a la red; `"captura": null` + `"nota"` si no se puede alcanzar sola (login, registro).
2. Instala la app, **inicia sesión** en el emulador y captura. Solo emuladores (`ANDROID_SERIAL=emulator-5554`); el script se niega a usar un teléfono conectado y avisa si la app está en el login:
   ```bash
   ./gradlew :app:installDebug
   ANDROID_SERIAL=emulator-5554 python3 .claude/scripts/ui-catalogo/capturar.py P10 P11   # solo las que cambiaron; sin ids, todas
   ```
3. **Mira cada captura** con Read antes de seguir. Si salió a medio cargar, sube `espera`; si el scroll quedó corto o repetido, ajusta `scrolls`. Las capturas muestran los datos del dojo de la sesión: un dojo vacío o un endpoint sin publicar se ven tal cual (estado vacío o error).
4. Genera el catálogo con `python3 .claude/scripts/ui-catalogo/generar.py`. Imprime los conteos por capa y los componentes sin KDoc.
5. Si una pieza aparece "Sin KDoc", agrégale un KDoc de una línea en el código (es la descripción del catálogo) y regenera.
6. Opcional: verifica el render con Chrome sin ventana y revisa la imagen:
   ```bash
   "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" --headless=new --window-size=1280,1800 \
     --virtual-time-budget=4000 --screenshot=<scratchpad>/catalogo.png "file://$PWD/docs/ui-catalogo/index.html#componentes"
   ```

## C. Responder preguntas sobre el catálogo

- "¿Dónde se usa X?": la ficha del componente en el catálogo, o `grep -rn "X(" app/src/main/java`.
- "¿Qué pantallas faltan?": compara `pantallas.json` con las `Tkd*Screen` del código (`grep -rn "^fun Tkd.*Screen(" app/src/main/java`).
- Limitación conocida: los usos se atribuyen por paquete, así que dos pantallas del mismo paquete (p. ej. `payments/`) comparten la lista de componentes.
- Para enseñar el catálogo, ábrelo en el navegador (`open docs/ui-catalogo/index.html`). Es un documento interno: no lo publiques fuera del repo sin permiso.
