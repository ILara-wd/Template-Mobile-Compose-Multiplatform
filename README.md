# Vuelve+ UI Kit — Guía y Arquitectura Atomic Design

Sistema de diseño móvil nativo Android (Material Design 3 adaptado a **retención de clientes para estéticas caninas, grooming y veterinarias**). Producto: **Vuelve • Pet Retention** (plan **Vuelve PRO**). Su objetivo es detectar qué clientes "ya les toca volver" y facilitar el recontacto por WhatsApp en un toque.

> Los valores de color, tamaño y radio se obtuvieron midiendo las capturas de pantalla. Deben validarse contra el archivo fuente de diseño antes de pasarlos a código.

---

## 1. Tokens y Átomos

### 1.1 Paleta Semántica
- **Fondo Principal de Aplicación:** `#F6F7FD` (Lavanda muy claro)
- **Superficie / Contenedores Card:** `#FFFFFF` (White)
- **Superficie Tonal / Contenedor secundario:** `#E8ECFB` (Lavanda; chips, botones tonales, cajas de info, campos rellenos)
- **Acción Primaria y Activa:** `#2563EB` (Blue-600)
- **Primaria Intensa (CTA de acceso, chip activo sólido, badge "Hoy"):** `#1D4ED8` (Blue-700)
- **Texto Principal (High Contrast):** `#0F172A` (Slate-900)
- **Texto Secundario / Metadatos:** `#64748B` (Slate-500)
- **Placeholder:** `#94A3B8` (Slate-400)
- **Bordes y Delimitadores:** `#E2E8F0` (Slate-200)
- **Acento de Marca (Menta):** `#4ADE80` (Green-400) — badge de marca, círculos de icono de éxito y consejos
- **WhatsApp Vivo (CTA principal de contacto):** `#25D366`
- **WhatsApp Profundo (CTA en listas y login):** `#0E7A36` (Green-700/800)
- **Estado Al día / Completado:** Texto `#15803D` / Fondo `#DCFCE7`
- **Estado Toca contactar / Pendiente:** Texto `#92400E` / Fondo `#FEF3C7` (punto `#D97706`)
- **Estado Atrasado / Sin regresar:** Texto `#B91C1C` / Fondo `#FEE2E2` (punto y acento `#DC2626`)
- **Estado Seguimiento / Hoy sugerido:** Fondo sólido `#1D4ED8` con texto blanco, o fondo `#DBEAFE` con texto `#1D4ED8`
- **Indicador positivo de tendencia:** `#16A34A` (↑ +33%, potencial MXN)

### 1.2 Tipografía (Inter / Roboto — Sans-serif Android)
- **Display Number:** 32sp / Bold / Interlineado 36sp / `#0F172A` (ej. "8" Citas por reactivar)
- **Title Large:** 22sp / Bold / Interlineado 28sp / `#0F172A` (títulos de pantalla y de formulario)
- **Title Medium:** 18sp / Semi-Bold / Interlineado 24sp / `#0F172A` (nombre de cliente, títulos de sección)
- **Metric Value:** 18–20sp / Bold / `#0F172A` (ej. "28 a 30 días", "33%")
- **Body Large:** 16sp / Regular / Interlineado 24sp / `#0F172A`
- **Body Medium:** 14sp / Regular / Interlineado 20sp / `#64748B`
- **Label Large:** 14–16sp / Semi-Bold / Interlineado 20sp / `#FFFFFF` (botones) o `#2563EB` (links)
- **Label Small / Badge:** 12sp / Semi-Bold / Interlineado 16sp / color semántico
- **Overline:** 11–12sp / Semi-Bold / MAYÚSCULAS / tracking +0.5 / `#64748B` (ej. "VUELVE • PET RETENTION", "ACCIONES RÁPIDAS")
- **Caption / Meta:** 12sp / Medium / Interlineado 16sp / `#64748B`
- **Preview de mensaje:** 13–14sp / Italic / `#64748B` (vista previa truncada del WhatsApp sugerido)

### 1.3 Forma, Espaciado y Elevación
- **Radios:** Cards 16dp · Botones 12dp · Inputs 12dp · Badges 6dp / pill · Chips y FAB extendido pill 9999px · Tiles de icono 12–16dp
- **Espaciado base:** 4dp. Márgenes laterales de pantalla 16dp, padding interno de card 16dp, separación entre cards 12dp.
- **Elevación:** Cards planas en blanco sobre fondo lavanda, sombra muy sutil (y=1, blur 3, 6% negro). Barra inferior y bottom bar de acciones con sombra superior suave.
- **Acento lateral:** Barra izquierda de 4dp en cards de seguimiento con el color del estado (rojo = atrasado, azul = para hoy).

### 1.4 Iconografía
- Set lineal de 20–24dp, trazo 1.5–2dp (estilo Material Symbols Rounded / Lucide).
- Íconos de dominio: huella (mascota), tijeras (corte), historial/reloj (última visita), campana (por contactar), calendario con flecha (seguimientos), tendencia (resultados), bombilla (consejos), rayo (objetivo del turno), escudo/check (al día), robot (cálculo automático).
- **Icon Circle:** Contenedor circular de 36–40dp con fondo menta `#4ADE80` o tonal `#E8ECFB` e icono centrado.

### 1.5 Átomos Básicos
- **Botón Primario:** Altura 48–52dp, radio 12dp, fondo `#2563EB`, texto blanco 16sp Semi-Bold, flecha → opcional al final. Estados: Normal, Presionado (90% opacidad), Deshabilitado (`#94A3B8`).
- **Botón Primario Intenso:** Igual al primario con fondo `#1D4ED8` (ej. "Iniciar sesión").
- **Botón WhatsApp:** Altura 48–56dp, radio 12dp, fondo `#25D366` (CTA principal) o `#0E7A36` (en listas/login), icono de chat a la izquierda, texto blanco. Variante con subtítulo de 12sp.
- **Botón Tonal / Secundario:** Altura 48dp, fondo `#E8ECFB`, sin borde, texto `#0F172A` o `#1D4ED8` con icono (ej. "Posponer 7d", "Ver perfil", "Registrar visita", "Continuar con Google").
- **Botón Pill Extendido:** Radio pill, fondo `#2563EB`, icono + texto (ej. "+ Nuevo" en Clientes).
- **Botón de Texto / Link:** 14sp Semi-Bold `#2563EB` (ej. "Ver todos (8) ›", "Métricas", "Ciclo sugerido", "¿Olvidaste tu contraseña?").
- **Botón de Icono:** Área táctil 44x44dp (atrás, cerrar, menú ⋮, mostrar contraseña).
- **Campo de Texto Relleno:** Altura 52–56dp, fondo `#EEF1FB`, sin borde visible, radio 12dp, icono líder `#64748B`, placeholder `#94A3B8`. Etiqueta superior persistente 14sp Semi-Bold con asterisco rojo (*) si es obligatoria o texto "Opcional" alineado a la derecha.
- **Campo Multilínea:** Mismo estilo, altura mínima 96dp.
- **Selector de Lada:** Bloque separado a la izquierda del teléfono con bandera 🇲🇽 y "+52".
- **Checkbox:** 24x24dp, radio 4dp; marcado con relleno `#1D4ED8` y check blanco.
- **Chip de Filtro:** Altura 36dp, radio pill, fondo `#E8ECFB`. Activo: fondo `#DBEAFE` con check y texto `#1D4ED8`, o fondo sólido `#1D4ED8` con texto blanco (Seguimientos). Puede llevar contador o punto de estado rojo.
- **Chip de Selección (con icono):** Altura 40dp, radio pill; seleccionado sólido `#1D4ED8` con texto blanco (ej. "Baño y corte", "Estética canina / Grooming").
- **Badge de Estado:** Radio 6dp o pill, padding 4dp 8dp, 12sp Semi-Bold, punto opcional (ver §5).
- **Badge de Marca:** Pill menta `#4ADE80` con icono de huella y "VUELVE • PET RETENTION" en Overline oscuro.
- **Avatar de Iniciales:** Círculo 48–56dp, fondo `#DBEAFE`/lavanda (verde `#4ADE80` si está al día), iniciales 16sp Bold `#0F172A`.
- **Mini-badge de Mascota:** Círculo de 18–20dp sobre el avatar (esquina inferior derecha) con huella coloreada por estado: azul normal, rojo atrasado, verde al día.
- **Avatar con Foto de Mascota:** Círculo 44dp con foto de la mascota (Inicio, Seguimientos).
- **Barra de Progreso:** Altura 6dp, pill, track `#DBEAFE`, relleno `#2563EB`.
- **Punto de Sección:** Círculo de 8dp de color (azul, verde, gris, rojo) antes de títulos de sección.
- **Divisor con Texto:** Línea `#E2E8F0` con texto centrado "o continúa con".

---

## 2. Moléculas

- **M1. Buscador Táctil:** Input blanco de 52dp, radio 12dp, icono lupa líder, placeholder "Buscar cliente o mascota…" y botón ✕ de borrado rápido.
- **M2. Campo con Validación:** Etiqueta + indicador (*/Opcional/Requerido) + input relleno con icono + texto auxiliar con icono (ej. "ⓘ Usaremos este número para enviar los recordatorios…").
- **M3. Campo de Teléfono WhatsApp:** Selector de lada 🇲🇽 +52 + input numérico + ayuda con icono WhatsApp verde.
- **M4. Campo de Contraseña:** Icono candado + input + toggle de visibilidad (ojo) + link de recuperación en la etiqueta.
- **M5. Mini-Card de Métrica (KPI compacto):** Etiqueta 12sp + valor 18sp Bold + Icon Circle a la derecha (ej. "Por contactar hoy · 8 peluditos" con campana menta; "Al día / En fecha · 28 activos").
- **M6. Tile de Estadística:** Etiqueta, cifra grande, subtítulo o variación (↑ +33%, "Objetivo: 25%") sobre fondo tonal (Contactados / Recuperadas / Conversión).
- **M7. Tarjeta de Indicador de Cliente:** Icono + etiqueta + valor 18sp Bold + estado con check (ej. "Frecuencia habitual · 28 a 30 días · ✓ Cliente constante").
- **M8. Fila de Cliente:** Avatar de iniciales con mini-badge de mascota, nombre, mascota en azul + raza, badge de estado, "Sugerida: [fecha]", línea de historial (🕘 26 Sep (hace 4 sem) • Baño y corte) y chevron ›.
- **M9. Tira de Información de Visita:** Caja tonal con icono, tiempo transcurrido y servicio alineado a la derecha (ej. "🕘 Hace 4 sem (26 Sep) · Baño y corte completo").
- **M10. Bloque de Fechas de Retorno:** Caja tonal a dos columnas "Última visita" / "Retorno sugerido" (rojo si vencido, azul si es hoy) + fila "Motivo" con icono.
- **M11. Par de Acciones de Seguimiento:** Botón WhatsApp (≈65% ancho) + botón tonal secundario ("Posponer 7d" o "Ver perfil").
- **M12. Chip de Filtro con Contador:** Ej. "Todos (48)", "Por contactar (8)" con punto rojo, "Para hoy 4".
- **M13. Selector Segmentado:** 2–3 opciones de igual ancho; la activa sólida azul o blanca elevada (Perro/Gato/Otro; WhatsApp/Tel | Correo).
- **M14. Grilla de Frecuencia:** 2x2 tiles seleccionables (Cada 3 semanas, Cada 4 semanas (1 mes), Cada 6 semanas, Personalizado); seleccionado con fondo `#DBEAFE`, borde azul y check circular.
- **M15. Selector de Canal de Contacto:** Dos toggles; seleccionado verde profundo "WhatsApp (Recomendado)", alternativo tonal "Llamada telefónica".
- **M16. Fila de Opción con Checkbox:** Checkbox + título + subtítulo + chevron (ej. "Registrar primera visita de inmediato · Abre el ticket de atención al guardar").
- **M17. Callout Informativo:** Caja `#DBEAFE`/tonal con icono (robot, info) y texto 13sp (ej. "Vuelve calculará automáticamente el momento óptimo…").
- **M18. Ítem de Timeline de Visitas:** Nodo circular (sólido azul = última), fecha en azul, badge "Última", monto "$480 MXN", título del servicio y nota en caja tonal con icono.
- **M19. Vista Previa de Mensaje:** Burbuja blanca con encabezado "✓ WhatsApp Automático" en verde y texto del recordatorio personalizado con nombre de mascota.
- **M20. Banner de Prueba Social / Confianza:** Icon Circle menta + título + subtítulo + pill de métrica opcional (ej. "Recordatorios automáticos · 8 citas reactivadas hoy en tu zona · +28%"; "Prueba 100% libre de riesgo").
- **M21. Encabezado de Sección con Contador:** Punto de color + título + subtítulo de color + pill de conteo a la derecha (ej. "● Atrasados (Atención prioritaria) · 2 clientes").
- **M22. Fila Compacta de Próximo Seguimiento:** Avatar foto + "Nombre • Mascota" + servicio sugerido + fecha en azul + botón tonal "Ver ›".

---

## 3. Organismos

- **O1. Top Bar de Aplicación:** Logo cuadrado azul de Vuelve (flecha de retorno), Overline "VUELVE • PET RETENTION" sobre el título de pantalla y avatar del usuario con chevron de menú de cuenta.
- **O2. Top Bar de Detalle / Flujo:** Flecha atrás + logo + título ("Detalle Cliente") + avatar. Variante de autenticación: atrás + punto azul + título centrado + botón circular de perfil.
- **O3. Bottom Navigation Bar persistente (4 destinos):** Inicio, Clientes, Seguimientos y Resultados. Indicador activo en pill lavanda con icono y etiqueta azul `#1D4ED8`.
- **O4. Bottom Action Bar (sticky):** En Detalle de Cliente: "⊕ Nueva visita" tonal + "WhatsApp" verde a ancho completo dividido.
- **O5. Hero de Oportunidad del Día:** Overline azul "● CLIENTES POR CONTACTAR HOY", cifra Display + "Citas por reactivar", potencial económico en verde ("↗ Potencial aprox. $3,450 MXN"), descripción y botón primario "Ver seguimientos (8) →".
- **O6. Tarjeta de Cliente Prioritario:** Avatar foto, nombre, mascota • raza, badge de estado, Tira de Información de Visita (M9), preview del mensaje sugerido truncado y botón verde "Contactar".
- **O7. Panel de Resultados del Mes:** Título con icono + link "Métricas", tres Tiles de Estadística (M6) y nota al pie con fuente de datos.
- **O8. Acciones Rápidas:** Overline + dos botones tonales con icono ("Registrar visita", "Lista de clientes").
- **O9. Encabezado de Directorio de Clientes:** Título + conteo de registros + subtítulo, botón pill "+ Nuevo", dos Mini-Cards KPI (M5), Buscador (M1) y fila de Chips de Filtro con scroll horizontal.
- **O10. Lista de Clientes:** Pila de Filas de Cliente (M8) en cards individuales.
- **O11. Tarjeta de Seguimiento:** Card con acento lateral de estado, avatar foto, nombre + huella, mascota • raza, badge de retraso o "Hoy sugerido", Bloque de Fechas (M10) y Par de Acciones (M11).
- **O12. Lista de Seguimientos Agrupada:** Chips de filtro (Todos, Para hoy, Atrasados, Próximos), Banner de Objetivo del Turno (rayo + "5/12 contactados" + tasa de respuesta estimada + barra de progreso) y secciones "Atrasados", "Para hoy" y "Próximos días".
- **O13. Ficha de Cliente:** Avatar con mini-badge, nombre, chip "Cliente VIP", botón "Editar", menú ⋮, mascota (link) + raza y edad, y bloque de teléfono con botón "Llamar".
- **O14. Módulo de Estado de Retención:** Pill sólido "● Corresponde contactar hoy" + chip "Frecuencia puntual", explicación con dato resaltado ("Cumplió **4 semanas** desde su último baño y corte…"), último contacto y CTA WhatsApp con subtítulo "Preparar mensaje sugerido para Luna".
- **O15. Historial de Visitas:** Encabezado con chip "3 recientes" y botón tonal "+ Registrar visita", seguido del timeline vertical (M18).
- **O16. Formulario de Alta de Cliente:** Banner de cabecera con icono y descripción, y tres cards de sección: "● Datos principales" (tutor, teléfono, mascota, tipo, raza), "● Preferencias de servicio y retorno" (chips de servicio, Grilla de Frecuencia, callout) y "● Detalles adicionales" (notas, canal de contacto). Cierra con fila de opción y doble CTA.
- **O17. Formulario de Registro de Negocio:** Badge de marca, título, subtítulo, campos (negocio, responsable, WhatsApp, contraseña), chips de servicios ofrecidos, aceptación de términos, CTA y link a inicio de sesión.
- **O18. Formulario de Inicio de Sesión:** Logo de huella con check, "Vuelve PRO", bienvenida, banner de prueba social, selector segmentado de método, campos, CTA, divisor y accesos alternativos (código por WhatsApp, Google).
- **O19. Tarjeta de Consejo / Insight:** Fondo tonal, Icon Circle menta con bombilla, título y dato educativo ("Los recordatorios a los 28 días recuperan un 35% más de citas…").
- **O20. Estado de Logro / Empty State:** Icon Circle verde grande con check, título "¡Mantén el ritmo del día!", explicación y beneficio en verde.

---

## 4. Plantillas y Pantallas del Flujo MVP

1. **Pantalla 1 — Iniciar Sesión:** Acceso por teléfono/WhatsApp o correo, código por WhatsApp y Google, con prueba social de recordatorios.
2. **Pantalla 2 — Registro de Negocio:** Alta de la estética ("Patitas Felices Grooming") con número de WhatsApp del negocio, servicios ofrecidos y vista previa del mensaje automático.
3. **Pantalla 3 — Inicio / Resumen:** Saludo ("Buenos días, Ana"), hero de citas por reactivar con potencial en MXN, clientes prioritarios con contacto inmediato, resultados del mes y acciones rápidas.
4. **Pantalla 4 — Clientes:** Directorio de 48 clientes con KPIs, buscador por cliente o mascota, filtros por estado de retención y consejo de retorno.
5. **Pantalla 5 — Detalle del Cliente:** Ficha de "María López" y su mascota "Luna", estado de retención con CTA de WhatsApp, frecuencia habitual, total de visitas e historial con montos.
6. **Pantalla 6 — Nuevo Cliente Recurrente:** Formulario por secciones con frecuencia ideal de visita y opción de registrar la primera visita al guardar.
7. **Pantalla 7 — Seguimientos:** Cola de trabajo diaria agrupada por urgencia con objetivo del turno, contacto por WhatsApp y opción de posponer 7 días.
8. **Pantalla 8 — Resultados:** Destino de la barra inferior (no incluido en las capturas); se espera que amplíe el panel O7 con métricas de contactados, recuperados y conversión.

---

## 5. Sistema de Estados de Retención

El estado de cada cliente se calcula comparando la fecha de la última visita con su frecuencia habitual. Es el eje semántico de todo el kit.

| Estado | Color | Etiquetas observadas | Uso del icono de huella |
|---|---|---|---|
| Al día | Verde `#DCFCE7` / `#15803D` | "Al día", "Cliente constante" | Huella verde |
| Toca contactar | Ámbar `#FEF3C7` / `#92400E` | "Toca contactar", "Toca servicio" | Huella azul |
| Seguimiento hoy | Azul `#1D4ED8` / blanco | "Seguimiento hoy", "Hoy sugerido", "Corresponde contactar hoy" | Huella azul |
| Atrasado | Rojo `#FEE2E2` / `#B91C1C` | "Atrasado 14 días", "14d retraso", "Sin regresar" | Huella roja |

---

## 6. Notas de Consistencia Detectadas

Puntos encontrados en las capturas que conviene resolver antes de pasar a desarrollo:

- **Idioma de títulos:** Las pantallas de autenticación usan "Sign In" y "Register Business"; el resto de la app está en español ("Iniciar sesión", "Registrar negocio").
- **Título de alta de cliente:** El formulario "Nuevo cliente recurrente" muestra "Detalle Cliente" en la top bar; debería decir "Nuevo cliente".
- **Etiquetas de estado:** Un mismo estado aparece con nombres distintos ("Toca contactar" / "Toca servicio"; "Atrasado 14 días" / "14d retraso" / "Sin regresar"). Se recomienda un vocabulario único (§5).
- **Truncado y saltos de línea:** En Detalle de Cliente el nombre se corta ("María Lópe") bajo el chip "Cliente VIP", la raza se parte en varias líneas y el teléfono se divide en dos; el texto del banner de alta tiene saltos manuales.
- **Bandera de lada:** En Registro e Inicio de sesión la bandera se muestra como glifo roto (cuadro "MX"); usar asset SVG en lugar de emoji.
- **Banner de depuración:** "Simular búsqueda vacía · Ver Empty State" en Clientes es una ayuda de prototipo y no debe llegar a producción.
- **Fechas de historial:** El timeline muestra 2023 mientras el resto del flujo usa fechas relativas recientes; unificar datos de ejemplo.
