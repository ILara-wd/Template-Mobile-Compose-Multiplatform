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

Tema por defecto **"Océano"**. Semilla primary `#0061A4`, semilla tertiary `#E65100`, estilo *Tonal Spot*.

| Rol M3 | Claro | Oscuro | Uso en SportKid |
|---|---|---|---|
| `primary` | `#0061A4` | `#9ECAFF` | Acción principal, tab y chip activos, links |
| `onPrimary` | `#FFFFFF` | `#003258` | Texto sobre primary |
| `primaryContainer` | `#D1E4FF` | `#00497D` | Chip seleccionado, indicador de bottom nav, avatar sin foto |
| `onPrimaryContainer` | `#001D36` | `#D1E4FF` | |
| `secondary` | `#535F70` | `#BBC7DB` | Iconos secundarios, metadatos |
| `secondaryContainer` | `#D7E3F7` | `#3B4858` | Botones tonales, chips de filtro, campos rellenos |
| `tertiary` | `#A04100` | `#FFB68F` | **Logros y energía:** FAB "+ Registrar", récords, celebraciones |
| `tertiaryContainer` | `#FFDBCB` | `#7A3000` | Fondo de cards de logro y chip "Nuevo récord" |
| `onTertiaryContainer` | `#341100` | `#FFDBCB` | |
| `error` | `#BA1A1A` | `#FFB4AB` | Validación de formularios y error de sincronización |
| `surface` | `#F8F9FF` | `#111418` | Fondo de pantalla |
| `surfaceContainerLowest` | `#FFFFFF` | `#0C0E12` | Cards |
| `surfaceContainer` | `#ECEEF4` | `#1D2024` | Bottom nav, bottom sheets, cajas de info |
| `onSurface` | `#191C20` | `#E1E2E8` | Texto principal |
| `onSurfaceVariant` | `#43474E` | `#C3C7CF` | Texto secundario y metadatos |
| `outline` | `#73777F` | `#8D9199` | Bordes de inputs outlined y chips no seleccionados |
| `outlineVariant` | `#C3C7CF` | `#43474E` | Divisores |

**Temas seleccionables** (cada uno en claro y oscuro):

| Tema | Semilla | Generación |
|---|---|---|
| Automático (Material You) | Wallpaper del usuario | `dynamicLight/DarkColorScheme` (Android 12+) |
| **Océano** (default) | `#0061A4` | Esquema exportado de Theme Builder |
| Dojang | `#9C2A22` | MaterialKolor en runtime |
| Alberca | `#006A6A` | MaterialKolor en runtime |
| Pista | `#B45309` | MaterialKolor en runtime |
| Bosque | `#386A20` | MaterialKolor en runtime |

### 1.2 Paleta semántica fija (`LocalSportColors`, no cambia con el tema)

| Token | Claro | Oscuro* | Uso |
|---|---|---|---|
| `sport.tkd` | `#C62828` | `#FF8A80` | Icono, acento lateral y tab de Taekwondo |
| `sport.tkdContainer` | `#FFEBEE` | `#4A1414` | Fondo de tile o badge TKD |
| `sport.swimming` | `#00838F` | `#4DD0E1` | Natación |
| `sport.swimmingContainer` | `#E0F7FA` | `#0B3A3E` | |
| `sport.athletics` | `#EF6C00` | `#FFB74D` | Atletismo |
| `sport.athleticsContainer` | `#FFF3E0` | `#4A2A0B` | |
| `medal.gold` | `#D4A017` | `#E6C04A` | Oro |
| `medal.silver` | `#9E9E9E` | `#BDBDBD` | Plata |
| `medal.bronze` | `#A0522D` | `#C98A5E` | Bronce |
| `status.success` / `successContainer` | `#2E7D32` / `#DCF5DD` | `#81C784` / `#1B3A1D` | Pagado, Dominado, Aprobado, Sincronizado |
| `status.warning` / `warningContainer` | `#8A5A00` / `#FFF3D6` | `#FFCC66` / `#3D2E0A` | Pendiente de pago, Aprendiendo, Pendiente |
| `status.neutral` | `onSurfaceVariant` + outline | — | Pendiente (por aprender), Participación |

\* Las variantes oscuras están desaturadas. Se pueden sustituir por `MaterialColors.harmonize(color, primary)` para que combinen con el tema activo.

**Colores para cada hijo** (se eligen en P04 y van en `Athlete.color`):

| Token | Hex | | Token | Hex |
|---|---|---|---|---|
| `athlete.blue` | `#1E88E5` | | `athlete.orange` | `#FB8C00` |
| `athlete.green` | `#43A047` | | `athlete.teal` | `#00ACC1` |
| `athlete.purple` | `#8E24AA` | | `athlete.red` | `#E53935` |
| `athlete.pink` | `#D81B60` | | `athlete.yellow` | `#FDD835` |

> Regla: el color de cada hijo **solo** se usa en el anillo del avatar, los puntos del calendario, el punto junto al nombre y el borde del chip seleccionado. **Nunca** como fondo de texto, porque el amarillo y el naranja no cumplen el contraste de 4.5:1.

**Cintas de TKD** (la secuencia es configurable por escuela, estos son los valores por defecto):

| Cinta | Hex | | Cinta | Hex |
|---|---|---|---|---|
| Blanca | `#FFFFFF` (borde `outline`) | | Azul | `#1565C0` |
| Amarilla | `#FBC02D` | | Marrón | `#6D4C41` |
| Naranja | `#FB8C00` | | Roja | `#C62828` |
| Verde | `#2E7D32` | | Roja‑Negra | `#C62828` / `#212121` (dividida) |
| | | | Negra (Dan) | `#212121` + franjas doradas por Dan |

### 1.3 Tipografía: Plus Jakarta Sans (fallback Roboto)

Se mapea sobre el `Typography` de M3, así que nadie usa tamaños sueltos en el código.

| Token M3 | Tamaño / Peso / Interlineado | Uso en SportKid |
|---|---|---|
| `displaySmall` | 36sp / Bold / 44sp | Cifra hero: total de gastos, "3 de 5" estilos |
| `headlineSmall` | 24sp / Bold / 32sp | Nombre del deportista en el perfil |
| `titleLarge` | 22sp / Bold / 28sp | Títulos de pantalla y de formulario |
| `titleMedium` | 16sp / SemiBold / 24sp | Título de card de evento y de sección |
| `titleSmall` | 14sp / SemiBold / 20sp | Encabezados dentro de cards |
| `bodyLarge` | 16sp / Regular / 24sp | Texto de inputs, contenido |
| `bodyMedium` | 14sp / Regular / 20sp | Descripciones, metadatos (`onSurfaceVariant`) |
| `bodySmall` | 12sp / Regular / 16sp | Ayudas de campo, fechas relativas |
| `labelLarge` | 14sp / SemiBold / 20sp | Botones y tabs |
| `labelMedium` | 12sp / SemiBold / 16sp | Chips, badges |
| `labelSmall` | 11sp / SemiBold / 16sp · MAYÚSCULAS · tracking +0.5 | Overline de sección ("PRÓXIMOS", "ÚLTIMOS LOGROS") |
| **`metric`** (extensión) | 20sp / Bold / 24sp · **tabular figures** | Tiempos (`00:41.23`), montos, distancias |

> Los tiempos y montos usan `fontFeatureSettings = "tnum"` para que los dígitos no "bailen" en las tablas de récords.

### 1.4 Forma, espaciado y elevación

| Token | Valor | Uso |
|---|---|---|
| `shape.extraSmall` | 4dp | Checkbox, mini‑badges |
| `shape.small` | 8dp | Badges de estado, tiles de icono pequeños |
| `shape.medium` | 12dp | Inputs, botones tonales cuadrados, celdas de tabla |
| `shape.large` | 16dp | **Cards** |
| `shape.extraLarge` | 28dp | Bottom sheets, diálogos |
| `shape.full` | pill | Botones principales, chips, FAB extendido |

- **Espaciado base:** 4dp. Escala `xs 4 · sm 8 · md 12 · lg 16 · xl 24 · xxl 32`.
- **Márgenes de pantalla:** 16dp. **Padding de card:** 16dp. **Separación entre cards:** 12dp. **Entre secciones:** 24dp.
- **Elevación tonal** (sin sombras pesadas): las cards van en `surfaceContainerLowest` sobre `surface`, con nivel 1. La bottom nav y los sheets van en `surfaceContainer`, nivel 2. El FAB usa nivel 3.
- **Acento lateral:** barra izquierda de 4dp del color del deporte en cards de evento y en el historial.
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

| Átomo | Composable | Especificación |
|---|---|---|
| Botón primario | `SkButton` | 48dp alto, pill, `primary`/`onPrimary`, `labelLarge`. Estados: normal, presionado (ripple), deshabilitado (38% `onSurface`), cargando (spinner 20dp, ancho fijo). |
| Botón de logro | `SkAccentButton` | Igual al primario con `tertiary`. Solo para "+ Registrar" y CTAs de celebración. |
| Botón tonal | `SkTonalButton` | 48dp, `secondaryContainer`, icono opcional ("Compartir (QR)", "Miembros", "+ Agregar prueba"). |
| Botón outlined | `SkOutlinedButton` | 48dp, borde `outline` ("Generar nuevo", "Cancelar"). |
| Botón de texto | `SkTextButton` | `labelLarge` en `primary` ("Ver detalle ›", "Guardar" en la top bar). |
| Botón de icono | `SkIconButton` | Área de 48dp e icono de 24dp (atrás, cerrar, ⋮, copiar). |
| FAB extendido | `SkExtendedFab` | Pill, `tertiaryContainer`/`onTertiaryContainer`, icono "+" y "Registrar". Se colapsa a FAB al hacer scroll. |
| Campo relleno | `SkTextField` | 56dp, `shape.medium`, fondo `surfaceContainerHighest`, etiqueta flotante, icono líder opcional, texto de ayuda o error. |
| Campo multilínea | `SkTextArea` | Igual, mínimo 96dp ("Notas"). |
| Switch | `SkSwitch` | M3 Switch con icono check en el thumb ("Pagado"). |
| Checkbox | `SkCheckbox` | 24dp con radio 4dp. |
| Chip de filtro | `SkFilterChip` | 32dp, pill. Activo: `secondaryContainer` y check. |
| Chip de entrada / selección | `SkChoiceChip` | 32dp. Seleccionado: `primaryContainer` (distancias, recordatorios, deportes). |
| Badge | `SkBadge` | `shape.small`, padding 4×8dp, `labelMedium`, punto o icono opcional. |
| Avatar | `SkAvatar` | 40/56/96dp. Foto o iniciales sobre `primaryContainer`, con **anillo de 2–3dp del color del hijo**. |
| Punto de color | `SkDot` | 8dp, para el calendario y junto al nombre del hijo. |
| Barra de progreso | `SkLinearProgress` | 6dp, pill, track `surfaceContainerHighest`. |
| Progreso circular | `SkCircularProgress` | 64dp, trazo 6dp ("3 de 5 estilos"). |
| Divisor | `SkDivider` | 1dp `outlineVariant`, con variante con texto. |
| Overline | `SkOverline` | `labelSmall` en mayúsculas `onSurfaceVariant`. |
| Skeleton | `SkSkeleton` | Bloque con *shimmer* sobre `surfaceContainerHigh`. |

---

## 2. Moléculas

Las moléculas **genéricas** viven en `core:designsystem`. Las que conocen modelos de dominio (`Sport`, `MedalResult`, etc.) viven en `core:ui`.

| ID | Molécula | Composable | Módulo | Descripción |
|---|---|---|---|---|
| M1 | Chip de deportista | `AthleteChip` | `core:ui` | Avatar de 24dp con anillo de color y nombre. Seleccionado: `primaryContainer`. |
| M2 | Selector de deportista | `AthleteSelector` | `core:ui` | Fila con scroll de `AthleteChip` y la opción "Todos". Selección única. |
| M3 | Icono de deporte | `SportIconTile` | `core:ui` | Icon Tile de 40dp con los colores del deporte. Variantes S (24dp) y M (40dp). |
| M4 | Badge de medalla | `MedalBadge` | `core:ui` | Icono `military_tech` coloreado (oro, plata o bronce) y etiqueta. "Participación" va en neutral y "Pendiente" en outlined. |
| M5 | Badge de cinta | `BeltBadge` | `core:ui` | Rectángulo de 24×8dp con el color de la cinta y su nombre ("Amarilla"). |
| M6 | Chip de estado | `StatusChip` | `core:ui` | Usa el vocabulario de §5. Icono, texto y contenedor semántico. |
| M7 | Campo de dinero | `MoneyField` | `core:designsystem` | Prefijo "$", sufijo "MXN" y teclado decimal. Formatea `$1,250.00` y emite centavos (`Long`). |
| M8 | Fila de pago | `PaidSwitchRow` | `core:designsystem` | Texto "Pagado" y `SkSwitch`. Si está apagado muestra la ayuda "Se mostrará como pendiente". |
| M9 | Campo de tiempo | `DurationField` | `core:designsystem` | Máscara `mm:ss.cc` (natación) o `hh:mm:ss` (atletismo) con teclado numérico. Emite `Duration`. |
| M10 | Campo fecha y hora | `DateTimeField` | `core:designsystem` | Abre el `DatePicker` y el `TimePicker` de M3. Formato "Sáb 14 nov · 10:00". |
| M11 | Campo de lugar | `PlaceField` | `core:ui` | `SkTextField` con un menú de lugares recientes y un botón de icono para abrir Maps. |
| M12 | Selector de recordatorio | `ReminderChips` | `core:ui` | Chips de opción "Ninguno · 1 h · 1 día · 2 días antes". |
| M13 | Selector de distancia | `DistanceChips` | `core:ui` | Chips predefinidos más "Otra", que abre un campo numérico en metros. |
| M14 | Botón segmentado | `SkSegmentedButton` | `core:designsystem` | De 2 a 3 opciones ("Lisa / Obstáculos", "Pendiente / Aprobado / No aprobado", "Sistema / Claro / Oscuro"). |
| M15 | Área de foto | `PhotoUploadArea` | `core:ui` | Caja punteada de 160dp con dos acciones (Cámara / Galería). Con foto: preview recortado y botón "Cambiar". |
| M16 | Chip de récord | `PersonalRecordChip` | `core:ui` | `tertiaryContainer`, icono `emoji_events` y "¡Nuevo récord personal!". |
| M17 | Encabezado de sección | `SectionHeader` | `core:designsystem` | `SkOverline` más acción opcional a la derecha ("Ver todo ›") o contador. |
| M18 | KPI compacto | `KpiTile` | `core:designsystem` | Etiqueta, valor `metric` e icono ("4 medallas", "3/5 estilos"). |
| M19 | Callout informativo | `InfoCallout` | `core:designsystem` | Caja `secondaryContainer` con icono `info` y texto `bodyMedium` ("Al marcar Aprobado se actualizará la cinta actual"). |
| M20 | Ítem de timeline | `TimelineItem` | `core:designsystem` | Nodo de 12dp (sólido si es el último), línea vertical, fecha, título, slot de badge y metadatos. |
| M21 | Fila de estilo de natación | `SwimStyleRow` | `feature:swimming` | Icono, nombre del estilo, `StatusChip` de 3 estados y "Desde 10 feb 2026". |
| M22 | Fila de prueba | `SwimRaceItem` | `feature:swimming` | Card con estilo, distancia, tiempo y posición, más botón de eliminar. |
| M23 | Chip de código | `CopyableCodeChip` | `core:designsystem` | Texto monoespaciado "K7QM‑4PZX" y botón para copiar. Al copiar muestra un snackbar "Código copiado". |
| M24 | Cuenta regresiva | `CountdownText` | `core:designsystem` | "Expira en 23:59 h". Cambia a `error` cuando faltan menos de 1 h. |
| M25 | Fila de miembro | `MemberRow` | `feature:athlete` | Avatar, nombre, correo, badge de rol y acción "Quitar acceso" (solo para el propietario). |
| M26 | Tarjeta de vista previa de tema | `ThemePreviewCard` | `feature:settings` | Mini mock (top bar, card y FAB) pintado con el esquema del tema, nombre y check si está seleccionado. |
| M27 | Fila de ajustes | `SettingsRow` | `core:designsystem` | Icono, título, subtítulo y trailing (chevron, switch o valor). |
| M28 | Indicador de sincronización | `SyncIndicator` | `core:ui` | Icono de 20dp en la top bar: `cloud_done`, `sync` (animado), `cloud_off` o `sync_problem`. |

---

## 3. Organismos

| ID | Organismo | Composable | Módulo | Composición |
|---|---|---|---|---|
| O1 | Top bar principal | `SkMainTopBar` | `core:ui` | `AthleteSelector` (M2), `SyncIndicator` (M28) y avatar del usuario, que abre Ajustes. |
| O2 | Top bar de detalle o formulario | `SkDetailTopBar` | `core:designsystem` | Atrás o cerrar, título y acción de texto ("Guardar") u overflow. |
| O3 | Bottom navigation | `SkBottomBar` | `app` | 4 destinos: Inicio, Agenda, Deportistas y Gastos. El activo va en pill `secondaryContainer`. |
| O4 | Sheet de registro rápido | `QuickAddSheet` | `feature:home` | Título y grid 2×3 de tiles: Entrenamiento, Examen de cinta, Torneo TKD, Competencia de natación y Carrera. |
| O5 | Card de evento | `EventCard` | `core:ui` | Acento lateral del deporte, `SportIconTile`, título, punto y nombre del hijo, fecha, lugar, costo y `StatusChip` de pago. |
| O6 | Lista de próximos | `UpcomingEventsSection` | `feature:home` | `SectionHeader` con hasta 3 `EventCard` y el link "Ver agenda ›". |
| O7 | Carrusel de logros | `AchievementsCarousel` | `feature:home` | Cards de 160dp en `tertiaryContainer` con foto de medalla, `MedalBadge` o `BeltBadge`, y dato ("5K · 28:41"). |
| O8 | Card de gastos del mes | `MonthlyExpensesCard` | `feature:home` | Overline, total `displaySmall`, barra apilada por color de deporte y leyenda. |
| O9 | Calendario mensual | `MonthCalendar` | `feature:agenda` | Grid de 7 columnas con hasta 3 `SkDot` por día (color del hijo). El día de hoy va en outline y el seleccionado en `primary`. |
| O10 | Lista de agenda | `AgendaDayList` | `feature:agenda` | Encabezados por día y `EventCard` con swipe para "Marcar pagado". |
| O11 | Formulario base de evento | `EventFormScaffold` | `core:ui` | `SkDetailTopBar` con los campos comunes: deportista, deporte, `DateTimeField`, `PlaceField`, `MoneyField`, `PaidSwitchRow`, `ReminderChips` y notas. Tiene un **slot** `details: @Composable` para cada tipo. |
| O12 | Card de deportista | `AthleteCard` | `feature:athlete` | `SkAvatar` de 56dp, nombre, edad, íconos de deportes, `BeltBadge` y "3/5 estilos". |
| O13 | Header de perfil | `AthleteProfileHeader` | `feature:athlete` | Header colapsable con avatar de 96dp, nombre, edad, fila de `KpiTile`, y botones tonales "Compartir (QR)" y "Miembros". |
| O14 | Tabs de deporte | `SportTabs` | `feature:athlete` | `PrimaryTabRow` con el indicador del color del deporte activo. Solo muestra los deportes del hijo. |
| O15 | Progresión de cintas | `BeltProgressionBar` | `feature:tkd` | Segmentos de color por cinta, la actual resaltada (elevada, con borde `primary`) y la siguiente con la etiqueta "Siguiente". |
| O16 | Historial de cintas | `BeltHistoryTimeline` | `feature:tkd` | `TimelineItem` con `BeltBadge`, lugar y costo. |
| O17 | Lista de torneos | `TournamentList` | `feature:tkd` | Cards con nombre, fecha, chip de modalidad (Combate / Formas) y `MedalBadge`. |
| O18 | Checklist de estilos | `SwimStylesChecklist` | `feature:swimming` | `SkCircularProgress` ("3 de 5") y 5 `SwimStyleRow`. Combinado individual muestra la nota "Requiere los 4 estilos". |
| O19 | Tabla de mejores tiempos | `BestTimesTable` | `feature:swimming` | Matriz de estilo × distancia con tiempos en `metric` y "—" si no hay registro. |
| O20 | Lista dinámica de pruebas | `SwimRacesEditor` | `feature:swimming` | Lista de `SwimRaceItem` y botón "+ Agregar prueba". Marca el récord con `PersonalRecordChip`. |
| O21 | Mejores marcas | `BestMarksRow` | `feature:athletics` | Cards horizontales por distancia con tiempo y badge "Lisa" u "Obstáculos". |
| O22 | Galería de medallas | `MedalGallery` | `feature:athletics` | Grid de 3 columnas con fotos cuadradas, fecha superpuesta y visor a pantalla completa. |
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
| P09 | Tab TKD | T2 | O15, O16 y O17 | `feature:tkd` |
| P10 | Examen de cinta | T3 | O11, resultado (M14), M19 y O31 | `feature:tkd` |
| P11 | Torneo TKD | T3 | O11, modalidad, grid de `MedalBadge` y M15 | `feature:tkd` |
| P12 | Tab Natación | T2 | O18, O19 y lista de competencias | `feature:swimming` |
| P13 | Competencia de natación | T3 | O11 y O20 | `feature:swimming` |
| P14 | Tab Atletismo | T2 | O21, O22 y lista de carreras | `feature:athletics` |
| P15 | Carrera | T3 | O11, M13, M14, M9 y M15 | `feature:athletics` |
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
| Estado | Etiqueta | Icono | Color |
|---|---|---|---|
| `PAID` | "Pagado" | `check_circle` | `status.success` |
| `UNPAID` | "Pendiente de pago" | `schedule` | `status.warning` |
| Sin costo | (no se muestra el chip) | — | — |

### 5.2 Estilo de natación
| Estado | Etiqueta | Icono | Color |
|---|---|---|---|
| `MASTERED` | "Dominado" | `check_circle` | `status.success` |
| `LEARNING` | "Aprendiendo" | `pending` | `status.warning` |
| `PENDING` | "Pendiente" | `radio_button_unchecked` | Outlined neutral |

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
│                                 # StatusChip, EventCard, EventFormScaffold, SyncIndicator…
feature/
├── tkd/ui/component/             ← Organismos exclusivos del feature
│                                 # BeltProgressionBar, BeltHistoryTimeline, TournamentList
├── swimming/ui/component/        # SwimStylesChecklist, BestTimesTable, SwimRacesEditor
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

- [ ] Exportar los esquemas exactos de **Océano** desde Material Theme Builder y reemplazar los valores de §1.1
- [ ] Medir las pantallas generadas en Stitch y ajustar radios y espaciados si difieren
- [ ] Diseñar el vector `ic_belt` y el logo de SportKid
- [ ] Confirmar la secuencia de cintas de la escuela de TKD real
- [ ] Ilustraciones para Login, Bienvenida y estados vacíos (estilo plano, colores del tema)
