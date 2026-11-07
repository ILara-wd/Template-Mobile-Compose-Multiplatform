# SportKid UI Kit — Guía y Arquitectura Atomic Design

Sistema de diseño para la app móvil nativa Android **SportKid**. Usa Material Design 3 con *dynamic theming*, adaptado al **seguimiento familiar de deportes infantiles**. La app permite a los dos padres llevar la agenda, el progreso y los gastos de sus hijos en **Taekwondo**, **Natación** y **Atletismo**, y compartir el perfil de cada deportista con un QR.

> Los valores de color, tipografía y forma salen de la especificación (`04-prompts-stitch.md`, §2). Hay que validarlos contra las pantallas generadas en Stitch y contra los esquemas exportados de Material Theme Builder antes de congelarlos en código.

**Documentos relacionados:** `01-diseno-pantallas.md` (pantallas P01–P20) · `02-stack-tecnologico.md` (arquitectura) · `03-backend.md` · `04-prompts-stitch.md` (paleta y prompts).

---

## 0. Principios del sistema

| Principio | Regla de diseño |
|---|---|
| **El tema cambia, la información no** | El `ColorScheme` M3 cambia con el tema elegido. Los colores de **deporte**, **medalla**, **estado** y **cada hijo** son fijos porque transmiten significado. |
| **Captura en menos de 30 s** | Valores por defecto (hoy, MXN, último lugar usado) y formularios en una sola columna. |
| **Un hijo, o todos** | Cada pantalla respeta el deportista elegido en el selector global. |
| **Color nunca solo** | Todo estado lleva icono o texto además del color (accesibilidad). |
| **Offline first visible** | El usuario siempre sabe si sus cambios ya se sincronizaron. |

---

## 1. Tokens y Átomos

### 1.1 Paleta: roles M3 (cambian con el tema)

Tema por defecto **"Océano"**. Semilla primary `#0061A4`, semilla tertiary `#E65100`, estilo *Tonal Spot*. Los valores del tema claro están ajustados a los diseños de Figma (archivo `KqTFktvrRvCzO2idPga5xf`), que mandan sobre la semilla. Figma solo tiene tema claro, así que el oscuro conserva los valores de Theme Builder.

| Rol M3 | Claro | Oscuro | Uso en SportKid |
|---|---|---|---|
| `primary` | `#00497D` | `#9ECAFF` | Acción principal, tab y chip activos, links |
| `onPrimary` | `#FFFFFF` | `#003258` | Texto sobre primary |
| `primaryContainer` | `#D1E4FF` | `#00497D` | Chip seleccionado, indicador de bottom nav, avatar sin foto |
| `onPrimaryContainer` | `#001D36` | `#D1E4FF` | |
| `secondary` | `#535F70` | `#BBC7DB` | Iconos secundarios, metadatos |
| `secondaryContainer` | `#D7E3F8` | `#3B4858` | Botones tonales, chips de filtro, campos rellenos |
| `tertiary` | `#A83900` | `#FFB68F` | **Logros y energía:** FAB "Nuevo evento", récords, celebraciones, punto de "Torneo / Especial" en el calendario, texto de "Pendiente de pago" |
| `tertiaryContainer` | `#FFDBCF` | `#7A3000` | Fondo de cards de logro, chip "Nuevo récord" y chip "Pendiente de pago" |
| `onTertiaryContainer` | `#380D00` | `#FFDBCB` | |
| `error` | `#BA1A1A` | `#FFB4AB` | Validación de formularios y error de sincronización |
| `surface` | `#F8F9FF` | `#111418` | Fondo de pantalla |
| `surfaceContainerLowest` | `#FFFFFF` | `#0C0E12` | Cards |
| `surfaceContainer` | `#ECEEF3` | `#1D2024` | Bottom nav, bottom sheets, cajas de info |
| `surfaceContainerHigh` | `#E7E8EE` | `#282A2F` | Fondo de campos rellenos (`SkTextField`, `SkSelectField`) |
| `onSurface` | `#191C20` | `#E1E2E8` | Texto principal |
| `onSurfaceVariant` | `#414750` | `#C3C7CF` | Texto secundario y metadatos |
| `outline` | `#717782` | `#8D9199` | Bordes de inputs outlined y chips no seleccionados |
| `outlineVariant` | `#C1C7D2` | `#43474E` | Divisores |

**Temas seleccionables** (cada uno en claro y oscuro):

| Tema | Semilla | Generación |
|---|---|---|
| Automático (Material You) | Wallpaper del usuario | `dynamicLight/DarkColorScheme` (Android 12+) |
| **Océano** (default) | `#0061A4` | Esquema fijo: claro ajustado a Figma, oscuro de Theme Builder |
| Dojang | `#9C2A22` | MaterialKolor en runtime |
| Alberca | `#006A6A` | MaterialKolor en runtime |
| Pista | `#B45309` | MaterialKolor en runtime |
| Bosque | `#386A20` | MaterialKolor en runtime |

### 1.2 Paleta semántica fija (`LocalSportColors`, no cambia con el tema)

| Token | Claro | Oscuro* | Uso |
|---|---|---|---|
| `sport.tkd` | `#C62828` | `#FF8A80` | Icono, acento lateral y tab de Taekwondo |
| `sport.tkdContainer` | `#FFEBEE` | `#4A1414` | Fondo de tile o badge TKD |
| `sport.onTkdContainer` | `#93000A` | `#FFDAD6` | Texto sobre `tkdContainer` (etiqueta "Taekwondo") |
| `sport.swimming` | `#00838F` | `#4DD0E1` | Natación |
| `sport.swimmingContainer` | `#E0F7FA` | `#0B3A3E` | |
| `sport.onSwimmingContainer` | `#004F56` | `#C4EBF2` | Texto sobre `swimmingContainer` |
| `sport.athletics` | `#EF6C00` | `#FFB74D` | Atletismo |
| `sport.athleticsContainer` | `#FFF3E0` | `#4A2A0B` | |
| `sport.onAthleticsContainer` | `#803800` | `#FFE0B2` | Texto sobre `athleticsContainer` |
| `medal.gold` | `#D4A017` | `#E6C04A` | Oro |
| `medal.silver` | `#9E9E9E` | `#BDBDBD` | Plata |
| `medal.bronze` | `#A0522D` | `#C98A5E` | Bronce |
| `status.success` / `successContainer` | `#2E7D32` / `#DCF5DD` | `#81C784` / `#1B3A1D` | Dominado, Aprobado, Sincronizado |
| `status.warning` / `warningContainer` | `#8A5A00` / `#FFF3D6` | `#FFCC66` / `#3D2E0A` | Aprendiendo, Pendiente |
| `status.neutral` | `onSurfaceVariant` + outline | — | Pendiente (por aprender), Participación |

> El color del deporte (`sport.tkd`, etc.) no cumple 4.5:1 como texto sobre su contenedor en todos los casos. Para etiquetas de texto se usa `sport.on*Container`; en código, `Sport.colors().onContainer`.

\* Las variantes oscuras están desaturadas. Se pueden sustituir por `MaterialColors.harmonize(color, primary)` para que combinen con el tema activo.

**Colores para cada hijo** (se eligen en P04 y van en `Athlete.color`):

| Token | Hex | | Token | Hex |
|---|---|---|---|---|
| `athlete.blue` (Azul) | `#1E88E5` | | `athlete.purple` (Morado) | `#8E24AA` |
| `athlete.pink` (Rosa) | `#D81B60` | | `athlete.yellow` (Amarillo) | `#FDD835` |
| `athlete.mint` (Verde menta) | `#00897B` | | `athlete.sky` (Azul cielo) | `#039BE5` |
| `athlete.orange` (Naranja) | `#FB8C00` | | `athlete.red` (Rojo) | `#E53935` |

El selector de P04 los muestra en una cuadrícula de 4×2 en este orden: azul, rosa, verde menta, naranja, morado, amarillo, azul cielo y rojo (`AthleteColors.all`).

> Regla: el color de cada hijo **solo** se usa en el anillo del avatar, los puntos del calendario, el punto junto al nombre y el borde del chip seleccionado. **Nunca** como fondo de texto, porque el amarillo y el naranja no cumplen el contraste de 4.5:1.

**Cintas de TKD** (la secuencia es configurable por escuela, estos son los valores por defecto):

| Cinta | Hex | Grado | | Cinta | Hex | Grado |
|---|---|---|---|---|---|---|
| Blanca | `#FFFFFF` (borde `outline`) | 10° Kup | | Azul | `#1565C0` | 5° Kup |
| Amarilla | `#FBC02D` | 8° Kup | | Marrón | `#6D4C41` | 4° Kup |
| Naranja | `#FB8C00` | 7° Kup | | Roja | `#C62828` | 3° Kup |
| Verde | `#2E7D32` | 6° Kup | | Roja‑Negra | `#C62828` / `#212121` (dividida) | 1° Kup |
| | | | | Negra (Dan) | `#212121` + franjas doradas por Dan | 1° Dan |

> Solo verde (6° Kup) y azul (5° Kup) vienen de Figma; los demás grados son supuestos hasta confirmar con la escuela (§8). En código: `Belt.gradeRes()`, `Belt.shortLabelRes()` ("Verde"), `Belt.color()` y `Belt.next` en `core:ui`.

### 1.3 Tipografía: Plus Jakarta Sans (fallback Roboto)

Se mapea sobre el `Typography` de M3, así que nadie usa tamaños sueltos en el código.

| Token M3 | Tamaño / Peso / Interlineado | Uso en SportKid |
|---|---|---|
| `displaySmall` | 36sp / Bold / 44sp | Cifra hero: total de gastos, "3 de 5" estilos |
| `headlineMedium` | 28sp / SemiBold / 36sp · tracking −0.7 | Títulos de bienvenida y propuesta de valor (Login, Bienvenida) |
| `headlineSmall` | 24sp / SemiBold / 30sp · tracking −0.6 | Nombre del deportista en el perfil, encabezado de paso en formularios |
| `titleLarge` | 20sp / SemiBold / 26sp | Títulos de card de acción y de formulario |
| `titleMedium` | 16sp / SemiBold / 24sp | Título de card de evento y de sección |
| `titleSmall` | 14sp / SemiBold / 20sp | Encabezados dentro de cards |
| `bodyLarge` | 16sp / Regular / 24sp | Texto de inputs, contenido |
| `bodyMedium` | 14sp / Regular / 20sp | Descripciones, metadatos (`onSurfaceVariant`) |
| `bodySmall` | 12sp / Regular / 16sp | Ayudas de campo, fechas relativas |
| `labelLarge` | 14sp / SemiBold / 20sp | Botones y tabs |
| `labelMedium` | 12sp / SemiBold / 16sp | Chips, badges |
| `labelSmall` | 11sp / SemiBold / 14sp | Pills. Como overline de sección ("PRÓXIMOS", "ÚLTIMOS LOGROS") va en MAYÚSCULAS con tracking +0.55 (`SkPill(overline = true)`) |
| **`metric`** (extensión) | 20sp / Bold / 24sp · **tabular figures** | Tiempos (`00:41.23`), montos, distancias |
| **`brand`** (extensión) | 32sp / Bold / 40sp · tracking −0.8 | Solo el wordmark "SportKid" (Splash) |

> Los tiempos y montos usan `fontFeatureSettings = "tnum"` para que los dígitos no "bailen" en las tablas de récords.

### 1.4 Forma, espaciado y elevación

| Token | Valor | Uso |
|---|---|---|
| `shape.extraSmall` | 4dp | Checkbox, mini‑badges |
| `shape.small` | 8dp | Badges de estado, tiles de icono pequeños |
| `shape.medium` | 12dp | Inputs, botones tonales cuadrados, celdas de tabla |
| `shape.large` | 20dp | **Cards** |
| `shape.extraLarge` | 28dp | Bottom sheets, diálogos |
| `shape.full` | pill | Botones principales, chips, FAB extendido |
| **`shape.hero`** (extensión) | 24dp | Card con ilustración hero (Login) |

- **Espaciado base:** 4dp. Escala `xs 4 · sm 8 · md 12 · lg 16 · xl 24 · xxl 32`.
- **Márgenes de pantalla:** 16dp. **Padding de card:** 16dp. **Separación entre cards:** 12dp. **Entre secciones:** 24dp.
- **Elevación tonal** (sin sombras pesadas): las cards van en `surfaceContainerLowest` sobre `surface`, con nivel 1. La bottom nav y los sheets van en `surfaceContainer`, nivel 2. El FAB usa nivel 3. Valores de `SkElevation`: nivel 0 = 0dp, 1 = 1dp, 2 = 3dp, 3 = 6dp.
- **Acento lateral:** barra izquierda de 4dp del color del deporte en cards de evento y en el historial. Las cards de acción y de sección de deporte (Bienvenida, P04) usan 6dp (`SkCardAccentWidth`).
- **Área táctil mínima:** 48×48dp.

### 1.5 Iconografía

- Set **Material Symbols Rounded**, 24dp, peso 400, *fill* 0 (inactivo) o 1 (activo).
- Íconos de dominio:

| Concepto | Icono | Concepto | Icono |
|---|---|---|---|
| Taekwondo | `sports_martial_arts` | Medalla | `military_tech` |
| Natación | `pool` | Récord personal | `trending_up` / `emoji_events` |
| Atletismo | `directions_run` | Cinta (TKD) | **Vector propio** `ic_belt` (tintable) |
| Obstáculos | `hurdle` | Costo / pago | `payments` |
| Lugar | `location_on` | Recordatorio | `notifications` |
| Calendario | `calendar_month` | QR / Escanear | `qr_code_2` / `qr_code_scanner` |
| Entrenamiento | `fitness_center` | Sincronización | `cloud_done` / `cloud_off` / `sync_problem` |
| Miembros | `group` | Tema | `palette` |

- **Icon Tile:** contenedor de 40dp con radio 12dp, fondo `sport.*Container` e icono `sport.*`.
- No usar **emojis** en la UI de producción (🥋🏊🏃). Solo se usan en documentación. Su render varía por dispositivo, así que en la app siempre van iconos vectoriales.

### 1.6 Átomos básicos (`core:designsystem`, prefijo `Sk`)

Estado: ✅ implementado · ⏳ pendiente (todavía no lo usa ninguna pantalla).

| Átomo | Composable | Especificación | Estado |
|---|---|---|---|
| Botón primario | `SkButton` | Pill, `labelLarge` en una línea, icono líder opcional. Color `primary`/`onPrimary` por defecto, configurable (`containerColor`/`contentColor`: color del deporte en los CTA de TKD, `tertiary` en "Torneo"). Tamaños (`SkButtonSize`): **Small 40dp** (acción "Guardar" en la top bar, elevación nivel 1), **Medium 48dp** (CTA fijo de formularios de evento; padding horizontal 16dp) y **Large 56dp** (CTA principal, default). Deshabilitado: 38% `onSurface`. Falta el estado cargando (spinner 20dp, ancho fijo). | ✅ |
| Botón elevado | `SkElevatedButton` | Pill blanca de 56dp con icono a color sin teñir ("Continuar con Google"). | ✅ |
| Botón de logro | `SkAccentButton` | Igual al primario con `tertiary`. Solo para CTAs de celebración. | ⏳ |
| Botón tonal | `SkTonalButton` | Contenedor y contenido configurables (`secondaryContainer` por defecto), icono opcional, `shape.medium` por defecto y tamaños de `SkButtonSize` (Small 40dp por defecto). Usos: "Compartir (QR)" (`primaryContainer`/`primary`), "Tutores (2)" (`surfaceContainerHigh`), "Pagar cuota" (`tertiary`/`onTertiary`) y "Agregar deportista" (pill de 48dp, `primaryContainer`). | ✅ |
| Botón outlined | `SkOutlinedButton` | 48dp, borde `outline` ("Generar nuevo", "Cancelar"). | ⏳ |
| Botón de texto | `SkTextButton` | `labelMedium` por defecto (`textStyle` configurable), color `onSurfaceVariant` por defecto e icono final opcional ("Explorar demo ›"; "Ver detalles de examen y temario ›" en `labelLarge` `primary`). | ✅ |
| Botón de icono | — | Se usa `IconButton` de M3 directamente: área de 48dp e icono de 24dp (atrás, cerrar, notificaciones, adjuntar). | ✅ |
| Botón de perfil | `SkProfileButton` | Círculo `primary` de 32dp con icono de persona; abre Ajustes. | ✅ |
| FAB extendido | `SkExtendedFab` | Pill de 56dp, `tertiary`/`onTertiary`, icono "+" y "Nuevo evento". Se colapsa a FAB al hacer scroll (`expanded`). | ✅ |
| Etiqueta de campo | `SkFieldLabel` | `labelMedium` `onSurface` con icono `primary` opcional, encima del campo ("Nombre del deportista"). | ✅ |
| Encabezado de sección de formulario | `SkFormLabel` | `titleSmall` `onSurface`, asterisco `tertiary` si es obligatorio (TalkBack lee "obligatorio"), icono líder `primary` opcional y slot a la derecha (ayuda o icono). | ✅ |
| Campo relleno | `SkTextField` | 56dp, `shape.medium`, fondo `surfaceContainerHigh`, **etiqueta arriba** (no flotante), placeholder `onSurfaceVariant`, slots líder y final (icono de lugar, botón borrar), texto de ayuda.; acepta `visualTransformation` (máscaras como `SkTimeMaskTransformation` o `SkDurationMaskTransformation`) y `textStyle` (`metric` para tiempos). | ✅ |
| Campo de selección | `SkSelectField` | Mismo aspecto que `SkTextField`; muestra el valor y abre un menú o diálogo al tocarlo ("Fecha de nacimiento", "Cinta actual"). Variante `emphasized` en `titleMedium`. | ✅ |
| Campo multilínea | `SkTextArea` | Mínimo 96dp, `surfaceContainerHigh`, `shape.medium`, `bodyMedium`, placeholder `outline` y pie opcional para metadatos y acciones ("Visible para todos los tutores", adjuntar). | ✅ |
| Switch | `SkSwitch` | `Switch` de M3 con check en el thumb cuando está encendido. Va dentro de una fila `toggleable` (role Switch) con su etiqueta ("Pagado", "Cuota pagada", "¿Ya está pagado?"). | ✅ |
| Checkbox | `SkCheckbox` | 24dp con radio 4dp. | ⏳ |
| Chip | `SkChip` | Chip único para filtros, opciones y sugerencias. Pill (o `shape` configurable), `labelMedium`, slot `leading` (punto, avatar) e icono opcional. Seleccionado: contenedor sólido (`primary`/`onPrimary` por defecto). Sin seleccionar: `surfaceContainerLowest` con sombra nivel 1, o el contenedor que se pase. Alturas usadas: **32dp** filtros de agenda, **36dp** recordatorio, **40dp** disciplina (seleccionado `tertiary`), **28dp** sugerencias de lugar (seleccionado `primaryContainer`), **24dp** "Hoy" (`secondaryContainer`). La duración usa `shape.small` y ancho igual. | ✅ |
| Pill / badge | `SkPill` | Pill no interactiva, `labelSmall` por defecto (`textStyle` configurable), icono o slot líder opcional; `overline` = MAYÚSCULAS con tracking +0.55 ("ACCESO RÁPIDO", "9 años", "2 compromisos", "Sub-10"). | ✅ |
| Icono en círculo | `SkIconCircle` | 40dp por defecto (también 24, 32 y 36dp), contenedor y tinte configurables (icono de deporte en la card de evento, costo, recordatorio clave). | ✅ |
| Card | `SkCard` | `surfaceContainerLowest`, `shape.large`, elevación nivel 1, acento lateral opcional de 6dp (`SkCardAccentWidth`), clic opcional. | ✅ |
| Selector de color | `SkColorSwatchPicker` | Grid de 4 columnas; el seleccionado crece a 1.05 con check claro u oscuro según la luminancia. | ✅ |
| Avatar | `SkAvatar` | Foto (recorte circular) o, sin foto, iniciales de `name` (o icono de persona) sobre `primaryContainer`. Anillo opcional de 2dp (`ringColor`: color del hijo, o `primaryContainer` para el usuario) o degradado (`ringBrush`, 4dp en el alta de P04). Sombra y slot `badge` opcionales (cámara en P04). Tamaños usados: 16 (chip de filtro), 36 (top bar), 48 (selector de deportista) y 96dp (foto en P04). | ✅ |
| Punto de color | `AthleteDot` (`core:ui`) | 8dp por defecto (4dp en el calendario, 6dp en el día seleccionado, 10dp en filtros). | ✅ |
| Barra de progreso | `SkLinearProgress` | Pill de 8dp por defecto (10dp en la meta de cinta), track `surfaceContainerHighest` configurable, relleno de color o degradado (`brush`: de la cinta actual a la siguiente). Expone el progreso a TalkBack. | ✅ |
| Progreso circular | `SkCircularProgress` | 64dp por defecto (80dp en P12), trazo 6dp con extremos redondeados, inicia arriba; track `surfaceContainerHigh` y contenido centrado ("60%" / "3/5"). Expone el progreso a TalkBack. | ✅ |
| Divisor | `SkDivider` | 1dp `outlineVariant`, con variante con texto. | ⏳ |
| Overline | — | Cubierto por `SkPill(overline = true)`. | ✅ |
| Skeleton | `SkSkeleton` | Bloque con *shimmer* sobre `surfaceContainerHigh`. | ⏳ |


---

## 2. Moléculas

Las moléculas **genéricas** viven en `core:designsystem`. Las que conocen modelos de dominio (`Sport`, `MedalResult`, etc.) viven en `core:ui`.

| ID | Molécula | Composable | Módulo | Descripción |
|---|---|---|---|---|
| M1 | Chip de deportista | `AthleteChip` | `core:ui` | Avatar de 24dp con anillo de color y nombre. Seleccionado: `primaryContainer`. |
| M2 | Selector de deportista | `AthleteSelector` | `core:ui` | Fila con scroll de `AthleteChip` y la opción "Todos". Selección única. |
| M3 | Icono de deporte | `SportIconTile` | `core:ui` | Icon Tile de 40dp con los colores del deporte. Variantes S (24dp) y M (40dp). |
| M4 | Badge de medalla | `MedalBadge` | `core:ui` | Pill con el icono de medalla en el color de `medal.*` sobre ese color al 15%, texto en negrita ("Oro", "Plata", "Bronce"). "Participación" va en `surfaceContainer` con icono de verificado. Colores en `MedalResult.colors()`. |
| M5 | Badge de cinta | `BeltBadge` / `BeltSwatch` | `core:ui` | `BeltBadge`: rectángulo de 16dp de alto (`shape.extraSmall`) del color de la cinta con franja blanca al 40% y el grado en MAYÚSCULAS ("6° KUP"); texto oscuro sobre cintas claras. `BeltSwatch`: muestra de 24×12dp (14×8 en chips). |
| M6 | Chip de estado | `StatusChip` | `core:ui` | Usa el vocabulario de §5. Icono, texto y contenedor semántico. |
| M7 | Campo de dinero | `SkMoneyField` | `core:designsystem` | Caja compacta de 40dp en `surfaceContainerLow`: prefijo "$", monto en `metric` alineado a la derecha y sufijo "MXN". Hoy acepta pesos enteros (máx. 7 dígitos); falta formatear `$1,250.00` y emitir centavos (`Long`). |
| M8 | Fila de pago | `FeeRow` | `core:ui` | Caja `surfaceContainer` con etiqueta y `SkMoneyField` a la izquierda, y "Pagado"/"Pendiente" con `SkSwitch` a la derecha. La usan los formularios de TKD, natación y atletismo. |
| M9 | Campo de tiempo | `SkTextField` + `SkTimeMask` | `core:designsystem` | El campo guarda solo dígitos (máx. 6) y la máscara se llena desde la derecha como cronómetro; teclado numérico. Dos variantes: `SkTimeMaskTransformation` (`mm:ss.cc`, natación: "3885" → "00:38.85", se convierte a `SwimTime` en centésimas) y `SkDurationMaskTransformation` (`hh:mm:ss`, atletismo: "1404" → "00:14:04", se convierte a `RaceTime` en segundos). |
| M10 | Campo fecha y hora | `FormDateTimeFields` + `SkDatePickerDialog` / `SkTimePickerDialog` | `core:ui` | Dos `SkSelectField` lado a lado que abren el `DatePicker` y el `TimePicker` de M3 (24 h), con `LocalDate`/`LocalTime`. Formato "Sáb 14 nov" y "09:00 hrs". La usan la Agenda, P07, P10, P11, P13 y P15. |
| M11 | Campo de lugar | `PlaceField` | `core:ui` | `SkTextField` con un menú de lugares recientes y un botón de icono para abrir Maps. |
| M12 | Selector de recordatorio | `ReminderChips` | `core:ui` | Chips de opción "Ninguno · 1 h · 1 día · 2 días antes". |
| M13 | Selector de distancia | `SkChip` en `DetailsSection` | `feature:athletics` | Chips 100 m · 400 m · 1 K · 3 K · 5 K · Otra (32dp, `labelMedium`); el seleccionado va en `tertiary` con check. "Otra" abre un campo numérico en metros. Sube a `core:ui` si otro deporte lo necesita. |
| M14 | Control segmentado | `SkSegmentedControl` | `core:designsystem` | Pista pill `surfaceContainer`; el segmento activo es pill blanca elevada con texto `primary` por defecto, o sólido (`selectedContainerColor` = `primary`, texto `onPrimary`) como en "Pendiente / Aprobado / No aprobado". Icono opcional por opción ("Mes / Lista", check en "Aprobado", corredor y obstáculo en "Carrera lisa / Con obstáculos" de P15, con texto `tertiary`). |
| M15 | Área de foto | `PhotoUploadArea` / `AttachedPhotoRow` | `core:ui` | Caja punteada (`outline`) en `surfaceContainerLow` con icono de cámara en círculo `primaryContainer`, título, ayuda y botón opcional "Examinar galería"; toda el área abre el selector. Con `onCamera` muestra dos botones pill: "Cámara" (`primary`) y "Galería" (`surfaceContainerHighest`), como en P15. `AttachedPhotoRow`: miniatura de 56dp, nombre, detalle y check. |
| M16 | Chip de récord | `SkPill` | `feature:*` | `tertiaryContainer` con icono de trofeo o tendencia. Se muestra solo si el tiempo mejora la marca previa de esa distancia (y modalidad): "¡Nuevo récord personal en 3 K de Sofía! (-48 s)" en P15, "-0.30 s mejor que tu marca previa" en P13. |
| M17 | Encabezado de sección | `SectionHeader` | `core:designsystem` | `SkOverline` más acción opcional a la derecha ("Ver todo ›") o contador. |
| M18 | KPI compacto | `SkKpiTile` | `core:designsystem` | Valor en `metric` centrado (color configurable) y etiqueta `labelSmall` debajo, sobre `surfaceContainerLow` y `shape.medium` ("14 / 16 Combates", "92% Asistencia del mes"). |
| M19 | Callout informativo | `SkInfoCallout` | `core:designsystem` | Sin título: burbuja compacta (`shape.small`) con icono `info` `primary` y `bodySmall` ("Torneo programado: el resultado se actualizará…"). Con título y `emphasized`: icono en círculo `primary` y texto `primary` ("Actualización automática", sobre `primaryContainer` al 60%). Acepta texto con estilos (resaltar "Cinta Azul"). |
| M20 | Ítem de timeline | `TimelineItem` | `core:designsystem` | Nodo de 12dp (sólido si es el último), línea vertical, fecha, título, slot de badge y metadatos. |
| M21 | Fila de estilo de natación | `StyleRow` | `feature:swimming` | Icono del estilo en círculo de 36dp, nombre, detalle ("Acreditado: 12 marzo 2024", nota o "Requiere los 4 estilos base") y pill de estado. Fondo según estado (ver §5.2). |
| M22 | Fila de prueba | `RaceCard` | `feature:swimming` | Card editable con estilo (menú), distancia (25/50/100/200 m), tiempo (M9) y podio (1° Oro, 2° Plata, 3° Bronce, Finalista). Si el tiempo mejora la marca previa: acento `tertiary`, "¡Nuevo récord!" y la diferencia ("-0.30 s mejor que tu marca previa"). Botón de eliminar cuando hay más de una. |
| M23 | Chip de código | `CopyableCodeChip` | `core:designsystem` | Texto monoespaciado "K7QM‑4PZX" y botón para copiar. Al copiar muestra un snackbar "Código copiado". |
| M24 | Cuenta regresiva | `CountdownText` | `core:designsystem` | "Expira en 23:59 h". Cambia a `error` cuando faltan menos de 1 h. |
| M25 | Fila de miembro | `MemberRow` | `feature:athlete` | Avatar, nombre, correo, badge de rol y acción "Quitar acceso" (solo para el propietario). |
| M26 | Tarjeta de vista previa de tema | `ThemePreviewCard` | `feature:settings` | Mini mock (top bar, card y FAB) pintado con el esquema del tema, nombre y check si está seleccionado. |
| M27 | Fila de ajustes | `SettingsRow` | `core:designsystem` | Icono, título, subtítulo y trailing (chevron, switch o valor). |
| M28 | Indicador de sincronización | `SyncIndicator` | `core:ui` | Icono de 20dp en la top bar: `cloud_done`, `sync` (animado), `cloud_off` o `sync_problem`. |
| M29 | Etiqueta de deporte | `SportTag` | `core:ui` | Pill con icono (color del deporte) y nombre (`sport.on*Container`) sobre el contenedor del deporte ("Taekwondo", "Natación"). |
| M30 | Selector de resultado | `MedalResultPicker` | `core:ui` | 5 tarjetas iguales (oro, plata, bronce, diploma y espera) con icono en círculo de 36dp; la elegida en `primaryContainer`. Selección única (role RadioButton). |
| M31 | Encabezado de deporte | `AthleteSportHeader` | `core:ui` | Card con avatar de 64dp (anillo del color del hijo y badge del deporte), nombre, edad, subtítulo (federación o club), chips de resumen y pestañas de los deportes del hijo (la activa en el color del deporte). Usado en P09, P12 y P14. |

---

## 3. Organismos

| ID | Organismo | Composable | Módulo | Composición |
|---|---|---|---|---|
| O1 | Top bar principal | `SkMainTopBar` | `core:designsystem` | Marca "SportKid" (`titleLarge` `primary`), notificaciones y avatar del usuario con anillo `primaryContainer`, que abre Ajustes. Falta `SyncIndicator` (M28). |
| O2 | Top bar de detalle o formulario | `SkDetailTopBar` | `core:designsystem` | Atrás o cerrar (`SkNavigationIcon`), título `titleMedium` con subtítulo opcional en `primary` ("Taekwondo" / "Mateo Gómez") y acciones a la derecha (`SkButton` Small "Guardar", avatar). |
| O3 | Bottom navigation | `SkBottomBar` | `app` | 4 destinos: Inicio, Agenda, Deportistas y Gastos. El activo va en pill `secondaryContainer`. |
| O4 | Sheet de registro rápido | `QuickAddSheet` | `feature:home` | Título y grid 2×3 de tiles: Entrenamiento, Examen de cinta, Torneo TKD, Competencia de natación y Carrera. |
| O5 | Card de evento | `EventCard` | `core:ui` | Acento lateral de 6dp del deporte, horario, `AthleteTag` (punto y nombre del hijo), título, subtítulo, icono del deporte en círculo de 36dp, lugar, franja de costo en `surfaceContainerLow` con `PaymentStatusChip` y, si está pendiente, la franja "Toca para saldar o adjuntar comprobante". |
| O6 | Lista de próximos | `UpcomingEventsSection` | `feature:home` | `SectionHeader` con hasta 3 `EventCard` y el link "Ver agenda ›". |
| O7 | Carrusel de logros | `AchievementsCarousel` | `feature:home` | Cards de 160dp en `tertiaryContainer` con foto de medalla, `MedalBadge` o `BeltBadge`, y dato ("5K · 28:41"). |
| O8 | Card de gastos del mes | `MonthlyExpensesCard` | `feature:home` | Overline, total `displaySmall`, barra apilada por color de deporte y leyenda. |
| O9 | Calendario mensual | `MonthCalendar` | `feature:agenda` | Grid de 7 columnas con hasta 3 `SkDot` por día (color del hijo). El día de hoy va en outline y el seleccionado en `primary`. |
| O10 | Lista de agenda | `AgendaDayList` | `feature:agenda` | Encabezados por día y `EventCard` con swipe para "Marcar pagado". |
| O11 | Formulario base de evento | `FormCard` / `FormSaveBar` | `core:ui` | No es un scaffold único: cada formulario arma su `Scaffold` con `SkDetailTopBar` (cerrar + "Guardar") y comparte `FormCard`, M10, M8 y `FormSaveBar` (CTA fijo de 48dp con nota opcional; color configurable: el del deporte en TKD, `primary` en natación). |
| O12 | Card de deportista | `AthleteCard` | `feature:athlete` | `SkCard` con acento de 6dp del color del hijo; `SkAvatar` de 56dp con su deporte principal en un círculo de 24dp; nombre, edad y medallas ("3 oros • 1 plata" o un logro destacado); `SportTag` por deporte; estado de cinta (`BeltBadge` y nivel); progreso de estilos de natación; próximo compromiso (franja `tertiaryContainer` con cuenta regresiva si es examen, o fila neutra con etiqueta como "Inscrita"). |
| O13 | Header de perfil | `ProfileHeader` | `feature:athlete` | Avatar de 104dp con anillo degradado del color del hijo y badge de categoría ("Sub-10"), nombre, edad y fecha de nacimiento, chips de resumen (cinta, estilos, medallas) y dos `SkTonalButton`: "Compartir (QR)" y "Tutores (n)". No es colapsable por ahora. |
| O14 | Tabs de deporte | `SportTabs` | `feature:athlete` | Control segmentado sobre `surfaceContainer`; la pestaña activa se rellena con el color del deporte y texto blanco en negrita. Solo muestra los deportes del hijo. |
| O15 | Progresión de cintas | `BeltProgression` / `BeltCard` | `feature:tkd` / `feature:athlete` | En P09: barra de 9 tramos (superados y actual sólidos, siguiente al 40%, resto atenuado; roja‑negra degradada), indicador por tramo (check, actual, flecha a la siguiente, punto) y abreviatura, más "Progreso de cinta: 4 de 9 grados", "44% Ruta" y la franja "Próximo objetivo". En el perfil (P08): `BeltCard` con meta y barra degradada de la cinta actual a la siguiente. |
| O16 | Historial de cintas | `PromotionHistory` / `BeltTimeline` | `feature:tkd` / `feature:athlete` | En P09: línea vertical con nodo de 20dp del color de la cinta (check, o bandera en el ingreso) y una card por promoción con cinta, grado, fecha, lugar y costo ("$650 MXN (Pagado)" o "Sin costo"). En el perfil: versión horizontal compacta con la etiqueta "Actual". |
| O17 | Lista de torneos | `TournamentList` | `feature:tkd` | `SkCard` con acento de 4dp del color de la medalla (neutro en participación), nombre, fecha y sede, `MedalBadge`, y una franja con la modalidad (Combate en `primaryContainer`, Formas en `secondaryContainer`) y la división. |
| O18 | Checklist de estilos | `StylesChecklist` + `ProgressCard` | `feature:swimming` | Tarjeta de progreso técnico con `SkCircularProgress` y conteo de dominados, aprendiendo y pendientes, más la lista de 5 `StyleRow`. |
| O19 | Tabla de mejores tiempos | `BestTimesTable` | `feature:swimming` | Estilo × 25/50/100 m con filas alternadas, tiempos en `metric` y "--:--.--" si no hay marca; el récord destacado va en `tertiaryContainer` con icono. Nota "Medido en competencias oficiales FMN…". |
| O20 | Lista dinámica de pruebas | `RacesSection` | `feature:swimming` | Lista de `RaceCard` (M22), conteo "2 pruebas" y botón tonal "Agregar otra prueba". En P12 las competencias se muestran en modo lectura (`MeetCard`) con lugar y medalla por prueba. |
| O21 | Mejores marcas | `BestMarks` / `BestMarkCard` | `feature:athletics` | `LazyRow` de cards de 190dp con acento del color de atletismo: distancia ("1 K", "500 m"), pill "Lisa" / "Obstáculos", tiempo en `headlineSmall` `tertiary` con "min", fecha y medalla. La marca récord lleva acento `tertiary` de 6dp, fondo `tertiaryContainer` al 40% y pill "Récord". |
| O22 | Galería de medallas | `MedalGallery` | `feature:athletics` | Grid de 3 columnas con fotos cuadradas (degradado inferior, medalla arriba a la derecha, título y año), contador "(4 medallas)" y el mosaico "Próxima meta" en `athleticsContainer` que ocupa las columnas libres de la última fila. Falta el visor a pantalla completa. |
| O23 | Card de compartir QR | `QrShareCard` | `feature:sharing` | QR de 240dp sobre `surfaceContainerLowest`, `CopyableCodeChip`, `CountdownText` y dos botones. |
| O24 | Unirse con código | `JoinWithCodePanel` | `feature:sharing` | CTA "Escanear QR" (scanner de Google) y campo "O escribe el código" con botón "Validar". |
| O25 | Vista previa y descarga | `JoinPreviewCard` / `InitialSyncProgress` | `feature:sharing` | Card del deportista con el botón "Unirme", y después una barra lineal con checklist (Perfil, Eventos, Fotos). |
| O26 | Lista de miembros | `MembersList` | `feature:athlete` | `MemberRow` y un diálogo de confirmación al quitar acceso. |
| O27 | Resumen de gastos | `ExpensesSummary` | `feature:expenses` | Selector de periodo, filtros, total, barras por deporte (Vico), card ámbar de pendientes y lista agrupada. |
| O28 | Grid de temas | `ThemePicker` | `feature:settings` | `SkSegmentedButton` de modo y un grid 2×3 de `ThemePreviewCard`. "Automático" se oculta en Android 11 o menor. |
| O29 | Empty state | `SkEmptyState` | `core:designsystem` | Ilustración de 160dp, título, descripción y CTA. |
| O30 | Banner offline | `OfflineBanner` | `core:ui` | Banda de 32dp en `surfaceContainerHighest`, icono `cloud_off` y "Sin conexión · tus cambios se sincronizarán". |
| O31 | Overlay de celebración | `CelebrationOverlay` | `core:ui` | Confeti en `tertiary` y en los colores de medalla, más la insignia que entra animada (examen aprobado, récord). Respeta "reducir animaciones". |

---

## 4. Plantillas y pantallas del MVP

### 4.1 Plantillas (layouts reutilizables)

| Plantilla | Estructura | Pantallas |
|---|---|---|
| **T1. Principal con navegación** | `SkMainTopBar`, contenido con scroll, `SkBottomBar` y FAB opcional | Inicio, Agenda, Deportistas, Gastos |
| **T2. Detalle con tabs** | Header colapsable, `SportTabs` y contenido por tab | Perfil de deportista |
| **T3. Formulario** | `SkDetailTopBar` (cerrar / Guardar) y una columna de campos con el CTA fijo abajo | P04, P07, P10, P11, P13, P15 |
| **T4. Flujo de paso único** | Top bar mínima y contenido centrado | Splash, Login, Bienvenida, Compartir, Unirse |
| **T5. Lista de ajustes** | `SkDetailTopBar` y secciones de `SettingsRow` | Ajustes, Tema, Miembros |

### 4.2 Pantallas

| # | Pantalla | Plantilla | Organismos clave | Módulo |
|---|---|---|---|---|
| P01 | Splash | T4 | Logo | `feature:auth` |
| P02 | Login | T4 | Ilustración y `SkButton` "Continuar con Google" | `feature:auth` |
| P03 | Bienvenida | T4 | 2 cards de opción: "Crear deportista" y "Tengo un código QR" | `feature:auth` |
| P04 | Alta / edición de deportista | T3 | Avatar picker, selector de color, chips de deporte y secciones por deporte | `feature:athlete` |
| P05 | Inicio | T1 | O6, O7, O8, `SkExtendedFab` y O4 | `feature:home` |
| P06 | Agenda | T1 | O9, O10 y filtros | `feature:agenda` |
| P07 | Evento / entrenamiento | T3 | O11 | `feature:agenda` |
| P08 | Deportistas y perfil | T1 / T2 | O12, O13 y O14 | `feature:athlete` |
| P09 | Tab TKD | T2 | Resumen con conteos y pestañas de deporte, "Examen de cinta" / "Torneo", O15, O16 y O17 (`TaekwondoScreen`). Se abre desde "Ver trayectoria completa" en el perfil | `feature:tkd` |
| P10 | Examen de cinta | T3 | Contexto del deportista, M19, cinta actual de solo lectura, cinta objetivo (`BeltSelectField`), lugar, M10, M8, resultado (M14) con callout y M15 (`BeltExamScreen`). Falta O31 al aprobar | `feature:tkd` |
| P11 | Torneo TKD | T3 | Contexto del deportista, banner, nombre, lugar, M10, modalidad (`SkChip`), categoría, M8, M30 con callout y M15 (`TournamentFormScreen`) | `feature:tkd` |
| P12 | Tab Natación | T2 | M31, "Registrar nueva competencia", O18, O19, competencias y nota del entrenador (`SwimmingScreen`). Se abre desde "Ver progreso completo" en el perfil | `feature:swimming` |
| P13 | Competencia de natación | T3 | Contexto del deportista, datos del evento (nombre, sede, alberca 25/50 m, M10, M8), O20 y M15 (`SwimMeetFormScreen`) | `feature:swimming` |
| P14 | Tab Atletismo | T2 | M31 (categoría y "2 oros · 1 plata"), "Registrar carrera", O21, O22, carreras disputadas (distancia y tipo, tiempo oficial, lugar y pago) y FAB `tertiary` (`AthleticsScreen`). Se abre desde "Ver progreso completo" en el perfil | `feature:athletics` |
| P15 | Carrera | T3 | Contexto del deportista, datos de la carrera (nombre, sede, M10, M8), M13, M14, M9 con M16, posición (1°/2°/3°, Finalista, Por definir) y M15 con cámara (`RaceFormScreen`). CTA y acentos en `tertiary`. También desde "Carrera atlética" en O4 | `feature:athletics` |
| P16 | Gastos | T1 | O27 | `feature:expenses` |
| P17 | Compartir QR | T4 | O23 | `feature:sharing` |
| P18 | Unirse con QR | T4 | O24 y O25 | `feature:sharing` |
| P19 | Miembros | T5 | O26 | `feature:athlete` |
| P20 | Ajustes | T5 | `SettingsRow` por sección | `feature:settings` |
| P21 | Tema | T5 | O28 | `feature:settings` |

---

## 5. Sistema de estados (vocabulario único)

Cada dominio tiene **un solo nombre por estado**. En código, cada estado es un `enum` en `core:model` y su presentación (texto, icono y color) se define **una sola vez** en `core:ui` (`StatusChip`).

### 5.1 Pago del evento

Los colores siguen a Figma (Agenda): el pago usa los roles del tema en lugar de `status.success` / `status.warning`. El enum es `PaymentStatus` en `core:model`.

| Estado | Etiqueta | Icono | Color |
|---|---|---|---|
| `PAID` | "Pagado" | `check_circle` | `secondaryContainer` / `primary` |
| `PENDING` | "Pendiente de pago" | `pending` | `tertiaryContainer` / `tertiary` |
| Sin costo | (no se muestra el chip) | — | — |

### 5.2 Estilo de natación
| Estado | Etiqueta | Icono | Color |
|---|---|---|---|
| `MASTERED` | "Dominado" | Icono del estilo | Pill `primaryContainer`, icono en `secondaryContainer` |
| `LEARNING` | "Aprendiendo" | Icono del estilo | Pill `tertiary` / `onTertiary`, fila en `tertiaryContainer` al 30% |
| `PENDING` | "Pendiente" | Icono del estilo | Pill y icono en `surfaceContainerHighest`, texto `onSurfaceVariant` |

Los colores siguen a Figma (Pestaña Natación) con roles del tema. Los enums son `SwimStyle` y `SwimStyleStatus` en `core:model`.

### 5.3 Examen de cinta
| Estado | Etiqueta | Icono | Color |
|---|---|---|---|
| `PENDING` | "Pendiente" | `schedule` | Outlined neutral |
| `PASSED` | "Aprobado" | `verified` | `status.success` y `CelebrationOverlay` |
| `FAILED` | "No aprobado" | `replay` | `error` (container suave). El tono es motivador, no de error |

### 5.4 Resultado de torneo o carrera
| Estado | Etiqueta | Color |
|---|---|---|
| `GOLD` / `SILVER` / `BRONZE` | "Oro" / "Plata" / "Bronce" | `medal.*` |
| `PARTICIPATION` | "Participación" | Neutral |
| `PENDING` | "Por registrar" | Outlined |

El enum es `MedalResult` en `core:model`. En el selector de P11 se usan etiquetas cortas: "Diploma" para participación y "Espera" para pendiente. En P15: "Finalista" para participación y "Por definir / en proceso" para pendiente.

### 5.5 Sincronización
| Estado | Indicador | Texto en Ajustes |
|---|---|---|
| `SYNCED` | `cloud_done` | "Sincronizado hace 2 min" |
| `SYNCING` | `sync` (girando) | "Sincronizando…" |
| `OFFLINE` | `cloud_off` y `OfflineBanner` | "Sin conexión" |
| `ERROR` | `sync_problem` en `error` | "No se pudo sincronizar · Reintentar" |

### 5.6 Invitación QR
| Estado | Mensaje |
|---|---|
| Válida | Vista previa del deportista y "Unirme" |
| Expirada | "Este código expiró. Pide uno nuevo." |
| Usada | "Este código ya fue usado." |
| Inválida | "Código no válido. Revisa e intenta de nuevo." |

---

## 6. Arquitectura del UI Kit en el código

### 6.1 Dónde vive cada capa

```
core/
├── designsystem/                 ← Tokens + Átomos + Moléculas/Organismos GENÉRICOS (sin dominio)
│   └── src/main/kotlin/.../designsystem/
│       ├── theme/
│       │   ├── Color.kt          # OceanLight/DarkScheme (exportados de Theme Builder)
│       │   ├── SportColors.kt    # LocalSportColors (deporte, medallas, estados, hijos, cintas)
│       │   ├── Type.kt           # SportKidTypography + TextStyle.metric
│       │   ├── Shape.kt          # SportKidShapes
│       │   ├── Spacing.kt        # LocalSpacing (xs…xxl)
│       │   ├── AppPalette.kt     # enum de temas + ThemeMode
│       │   └── SportKidTheme.kt  # Resuelve dynamic / Océano / MaterialKolor
│       ├── icon/SkIcons.kt       # Material Symbols + vectores propios (ic_belt)
│       ├── component/            # SkButton, SkTextField, SkChip, SkAvatar, MoneyField, DurationField…
│       └── preview/              # @SkPreviews (claro/oscuro/fontScale), datos fake
├── ui/                           ← Componentes que CONOCEN el dominio (core:model)
│   └── .../ui/                   # AthleteChip, SportIconTile, MedalBadge, BeltBadge,
│                                 # StatusChip, EventCard, EventFormScaffold, SyncIndicator,
│                                 # SportTag, DateLabels (fechas es-MX compartidas),
│                                 # FormParts (FormCard, FormDateTimeFields, FeeRow, FormSaveBar),
│                                 # AthleteSportHeader, MedalBadge, PhotoUploadArea…
feature/
├── tkd/                          ← P09 TaekwondoScreen, P10 BeltExamScreen, P11 TournamentFormScreen
│                                 # BeltProgression, PromotionHistory, TournamentList.
│                                 # Recibe TkdAthlete; `app` lo arma desde el perfil del hijo.
├── swimming/                     ← P12 SwimmingScreen, P13 SwimMeetFormScreen
│                                 # StylesChecklist, BestTimesTable, RaceCard, SwimTime.
│                                 # Recibe SwimAthlete; `app` lo arma desde el perfil del hijo.
├── athletics/                    ← P14 AthleticsScreen, P15 RaceFormScreen
│                                 # BestMarks, MedalGallery, RaceCard, RaceTime.
│                                 # Recibe AthleticsAthlete; `app` lo arma desde el perfil del hijo.
└── …
```

**Reglas de dependencia** (coherentes con `02-stack-tecnologico.md`):
- `core:designsystem` **no** depende de `core:model`. Es reutilizable en cualquier app.
- `core:ui` depende de `core:designsystem` y `core:model`.
- `feature:*` depende de `core:ui` y `core:designsystem`, **nunca** de otro feature.
- Si un organismo de un feature se necesita en dos features, sube a `core:ui`.

### 6.2 Buenas prácticas de componentes Compose

1. **Stateless y state hoisting:** los componentes reciben `value` y `onValueChange`. **Ningún** componente del kit recibe un ViewModel.
2. **Firma estándar:** los parámetros requeridos van primero, luego `modifier: Modifier = Modifier` (el primer opcional) y al final los slots `@Composable`.
3. **Modelos de UI inmutables:** usar `@Immutable` / `@Stable` y `ImmutableList` (kotlinx.collections.immutable) para evitar recomposiciones.
4. **Tokens, no valores sueltos:** nada de `Color(0xFF…)`, `16.dp` o `fontSize = 14.sp` dentro de features. Se usa `MaterialTheme.colorScheme`, `LocalSportColors`, `LocalSpacing` y `MaterialTheme.typography`.
5. **Textos en `strings.xml`** (es-MX por defecto) con `plurals` ("1 medalla" / "4 medallas"). Formatos con `NumberFormat` y `DateTimeFormatter` en locale `es-MX`.
6. **Accesibilidad:** `contentDescription` en iconos con significado, `Modifier.semantics { stateDescription = … }` en `StatusChip`, objetivos de 48dp y soporte de `fontScale` de 1.3.
7. **Previews obligatorias:** cada componente tiene `@SkPreviews`, que cubre claro, oscuro, fontScale 1.3 y un tema MaterialKolor.
8. **Animaciones:** se revisa `LocalReducedMotion` (o `Settings.Global.ANIMATOR_DURATION_SCALE`) antes de lanzar el confeti.

**Ejemplo de molécula:**

```kotlin
@Composable
fun StatusChip(
    status: UiStatus,                 // sealed: Paid, Unpaid, Mastered, Learning, Pending…
    modifier: Modifier = Modifier,
) {
    val spec = status.spec()          // label (StringRes), icon, container, content: definido UNA vez
    Surface(
        modifier = modifier.semantics { stateDescription = stringResource(spec.label) },
        shape = MaterialTheme.shapes.small,
        color = spec.container,
        contentColor = spec.content,
        border = spec.border,
    ) {
        Row(
            Modifier.padding(horizontal = LocalSpacing.current.sm, vertical = LocalSpacing.current.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(spec.icon, contentDescription = null, Modifier.size(16.dp))
            Spacer(Modifier.width(LocalSpacing.current.xs))
            Text(stringResource(spec.label), style = MaterialTheme.typography.labelMedium)
        }
    }
}
```

### 6.3 Testing del UI Kit

| Tipo | Herramienta | Alcance |
|---|---|---|
| Screenshot | **Roborazzi** | Cada átomo, molécula y organismo en claro, oscuro, Océano y Dojang. Un cambio de tokens se detecta en el PR. |
| UI | Compose UI Test | `MoneyField` (formato y centavos), `DurationField` (máscara), `SwimRacesEditor` (agregar y quitar). |
| Accesibilidad | `composeTestRule` + checks ATF | Contraste y tamaño de los objetivos táctiles. |

### 6.4 Catálogo interno

La variante `dev` incluye una pantalla **"UI Kit"** oculta en Ajustes, que se abre con 7 taps en la versión. Muestra todos los componentes con sus estados y un selector de tema en vivo. Sirve para QA visual y para validar los temas MaterialKolor.

---

## 7. Reglas de consistencia (a cuidar en Stitch y en código)

- **Idioma:** toda la UI va en español de México, incluidas las pantallas de autenticación. No mezclar "Sign in" con "Iniciar sesión".
- **Vocabulario de estados:** usar exclusivamente las etiquetas de §5. Por ejemplo, no alternar "Pendiente de pago" con "Por pagar".
- **Formatos:**
  - Dinero: `$1,250 MXN` (sin decimales si son `.00`).
  - Tiempos de natación: `00:41.23`. Tiempos de atletismo: `28:41` o `1:02:15`.
  - Distancias: `50 m`, `5 K`.
  - Fechas cortas: "Sáb 14 nov"; con año solo si no es el actual.
- **Nombres de ejemplo:** usar siempre los mismos dos hijos en mocks y previews, con su color fijo (p. ej. *Mateo* azul, *Sofía* rosa). Así las pantallas de Stitch y los previews coinciden.
- **Iconos, no emojis:** los emojis de los documentos no llegan a la app.
- **Truncado:** los nombres largos se truncan con elipsis en una línea y los títulos de evento en máximo 2 líneas. Ningún dato numérico (tiempo, costo) se parte en dos líneas.
- **Ayudas de prototipo:** cualquier botón de depuración de Stitch ("Simular estado vacío") se queda en el catálogo `dev`, nunca en producción.
- **Tema:** al probar un tema nuevo hay que verificar que los colores de deporte y de los hijos **no** cambien.

---

## 8. Pendientes por validar

- [x] Ajustar el esquema claro de **Océano** a los valores de Figma (§1.1)
- [ ] Definir el esquema oscuro de **Océano** (Figma no tiene tema oscuro; hoy usa los valores de Theme Builder)
- [x] Medir las pantallas de Figma (Splash, Login, Bienvenida, Crear deportista) y ajustar radios y tipografía
- [ ] Diseñar el vector `ic_belt` y el logo de SportKid
- [ ] Confirmar la secuencia de cintas de la escuela de TKD real
- [ ] Ilustraciones para Login, Bienvenida y estados vacíos (estilo plano, colores del tema)
