---
name: catalogo-ui-sportkid
description: Catálogo de UI de SportKid (Android, Jetpack Compose). Úsalo para saber qué componentes, pantallas y tokens ya existen antes de implementar un diseño de Figma (qué reutilizar de core:designsystem y core:ui, qué es local del feature), y para actualizar el catálogo HTML (docs/ui-catalogo/index.html) con capturas del emulador después de crear o cambiar pantallas. También cuando pregunten "¿ya tenemos un componente para…?", "¿dónde se usa X?" o pidan regenerar o abrir el catálogo.
argument-hint: "[consultar <pieza o nodo de Figma> | actualizar [P14 P15…]]"
---

# Catálogo de UI de SportKid

Entrada: `$ARGUMENTS`. Puede ser una pieza o un nodo de Figma a consultar, `actualizar` con o sin ids de pantalla, o vacío (en ese caso, deduce qué hace falta por la conversación).

Piezas del catálogo (todas relativas a la raíz del repo):

| Ruta | Qué es |
|---|---|
| `docs/ui-catalogo/index.html` | El catálogo. **Generado: no lo edites a mano.** |
| `docs/ui-catalogo/pantallas.json` | Lista de pantallas: id (P14, O4…), nombre, composable, nodo de Figma (archivo `KqTFktvrRvCzO2idPga5xf`) y cómo abrirla (`captura.extras`, `scrolls`, `espera`). |
| `docs/ui-catalogo/img/` | Capturas `<id>-<n>.jpg` de 360 px de ancho. |
| `scripts/ui-catalogo/capturar.py` | Abre cada pantalla en el emulador y la captura. |
| `scripts/ui-catalogo/generar.py` + `plantilla.html` | Leen el código, el README y el manifiesto, y escriben `index.html`. |

## A. Consultar antes de implementar

Úsalo al empezar un diseño de Figma (junto con el flujo de `figma-design-to-code`), o cuando pregunten si algo ya existe.

1. Para cada pieza del diseño, busca candidatos por nombre y por función:
   - en el README (§1.6 átomos, §2 moléculas M1–M31, §3 organismos O1–O29, §4.2 pantallas);
   - en el código: `grep -rn "fun Sk\|^fun [A-Z]" core/designsystem core/ui --include=*.kt`;
   - en el catálogo (pestaña Componentes), que también lista los **locales** de cada feature.
2. Compara al menos dos candidatos leyendo su firma (parámetros y slots). Elige el que necesite menos cambios aunque su nombre no encaje. Ejemplos ya resueltos: `FormCard`, `FeeRow`, `FormDateTimeFields`, `FormSaveBar`, `PhotoUploadArea(onCamera)` y `AthleteSportHeader` sirven para cualquier deporte.
3. Clasifica cada pieza:
   - **Reutilizar**: existe en `core:designsystem` o `core:ui` y lo cubre. Da el nombre y los parámetros clave.
   - **Extender**: existe, pero falta un parámetro o una variante. Agrega un parámetro opcional con un valor por defecto que no cambie a quienes ya lo usan.
   - **Subir a core:ui**: hoy es local de un feature y el diseño nuevo lo repite en otro (los features no dependen entre sí).
   - **Local**: solo lo usa esta pantalla; va `private` en el feature, compuesto con átomos.
   - **Nuevo átomo**: no tiene dominio y lo van a reutilizar; va a `core:designsystem` con prefijo `Sk`.
4. Reglas del proyecto que siguen vigentes:
   - nada de emojis en la UI, siempre íconos;
   - colores de los tokens: el del deporte para acentos y el del hijo solo para anillos y puntos;
   - textos en `strings.xml` (es-MX, lenguaje inclusivo) y fechas calculadas, no fijas;
   - nada de `!!`;
   - ningún tipo experimental en firmas públicas.

## B. Actualizar el catálogo (después de crear o cambiar pantallas)

1. **Pantalla nueva**: agrégala a `docs/ui-catalogo/pantallas.json` en orden de id, con su nodo de Figma. Si se abre con una ruta nueva:
   - agrega el caso al enum `Destination` en `app/.../MainActivity.kt`; el extra `sk_screen` usa el nombre del enum;
   - si depende del hijo, usa `sk_athlete` (`mateo`, `sofia`);
   - si depende del deporte del perfil, usa `sk_sport`;
   - si es la pestaña principal, `sk_tab` (`Agenda`, `Athletes`);
   - si es el sheet de registro rápido, `sk_quickadd: "true"`.
2. Instala la app y captura. El emulador debe estar encendido; si hay varios dispositivos, define `ANDROID_SERIAL`:
   ```bash
   ./gradlew :app:installDebug
   python3 scripts/ui-catalogo/capturar.py P14 P15   # solo las que cambiaron; sin ids captura todas
   ```
3. **Mira cada captura** con Read antes de seguir. Si salió a medio cargar, sube `espera`; si el scroll quedó corto o repetido, ajusta `scrolls`.
4. Genera el catálogo con `python3 scripts/ui-catalogo/generar.py`. El script imprime los conteos por capa.
5. Si una pieza aparece "Sin KDoc", agrégale un KDoc de una línea en el código. Esa línea es la descripción del catálogo. Después regenera.
6. Opcional: verifica el render con Chrome sin ventana y revisa la imagen:
   ```bash
   "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" --headless=new --window-size=1280,1800 \
     --virtual-time-budget=4000 --screenshot=<scratchpad>/catalogo.png "file://$PWD/docs/ui-catalogo/index.html#componentes"
   ```
7. Si se agregaron o cambiaron componentes, actualiza también su fila en el README (§1.6, §2, §3 o §4.2). Así el catálogo muestra su código (M9, O21…).

## C. Responder preguntas sobre el catálogo

- "¿Dónde se usa X?": la ficha del componente en el catálogo, o `grep -rn "X(" --include=*.kt core feature app`.
- "¿Qué pantallas faltan?": compara `pantallas.json` con README §4.2.
- Para enseñar el catálogo, abre `docs/ui-catalogo/index.html` en el navegador (`open docs/ui-catalogo/index.html`). No lo publiques fuera del repo sin permiso.
