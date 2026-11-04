# CierrePro UI Kit — Guía y Arquitectura Atomic Design

Sistema de diseño móvil nativo Android (Material Design 3 adaptado a operaciones técnicas de campo para minisplits y climatización B2B). Desarrollado para **CierrePro** (TreeCodeDev).

---

## 1. Tokens y Átomos

### 1.1 Paleta Semántica
- **Fondo Principal de Aplicación:** `#F8FAFC` (Slate-50)
- **Superficie / Contenedores Card:** `#FFFFFF` (White)
- **Acción Primaria y Activa:** `#2563EB` (Blue-600)
- **Texto Principal (High Contrast):** `#0F172A` (Slate-900)
- **Texto Secundario / Metadatos:** `#64748B` (Slate-500)
- **Bordes y Delimitadores:** `#E2E8F0` (Slate-200)
- **Estado de Éxito / Completado:** `#16A34A` (Green-600) / Fondo `#DCFCE7`
- **Estado de Atención / Pendiente:** `#D97706` (Amber-600) / Fondo `#FEF3C7`
- **Estado de Error / Alerta:** `#DC2626` (Red-600) / Fondo `#FEE2E2`

### 1.2 Tipografía (Roboto / Sans-serif Android)
- **Title Large:** 22sp / Bold / Interlineado 28sp / `#0F172A`
- **Title Medium:** 18sp / Semi-Bold / Interlineado 24sp / `#0F172A`
- **Body Large:** 16sp / Regular / Interlineado 24sp / `#0F172A`
- **Body Medium:** 14sp / Regular / Interlineado 20sp / `#64748B`
- **Label Large:** 14sp / Semi-Bold / Interlineado 20sp / `#2563EB` o `#FFFFFF`
- **Caption / Meta:** 12sp / Medium / Interlineado 16sp / `#64748B`

### 1.3 Átomos Básicos
- **Botón Primario:** Altura mínima 48dp, radio 12dp, color `#2563EB`, texto blanco 14sp Semi-Bold. Estados: Normal, Presionado (Opacidad 90%), Deshabilitado (`#94A3B8`).
- **Botón Secundario:** Altura 48dp, fondo `#F1F5F9`, borde `#E2E8F0`, texto `#0F172A`.
- **Botón de Icono / Acción rápida:** Área táctil 44x44dp con radio 8dp y padding interno de 10dp.
- **Campo de Texto y Multilínea:** Borde 1px `#CBD5E1`, fondo `#FFFFFF` o `#F8FAFC`, radio 10dp. Etiqueta superior persistente con indicador obligatorio (*).
- **Checkboxes Técnicos:** Contenedor de 24x24dp con check SVG, estados no marcado (borde 2px `#94A3B8`) y completado (relleno `#16A34A` con checkmark blanco).
- **Chips de Filtro:** Altura 36dp, radio pill 9999px, borde `#E2E8F0`, estado activo con fondo `#DBEAFE` y texto `#1D4ED8`.
- **Badges de Estado:** Etiquetas compactas con radio 6dp, padding 4dp 8dp, código semántico (Verde: Completado/Al día; Ámbar: Pendiente; Azul: En curso; Rojo: Atrasado).
- **Contenedores de Fotografía:** Marco con borde discontinuo `#CBD5E1`, fondo `#F8FAFC`, botón de captura táctil e indicador de miniatura con badge "ANTES" / "DESPUÉS".

---

## 2. Moléculas

- **M1. Buscador Táctil:** Input integrado con icono de búsqueda (Leading Icon), texto reactivo y botón de borrado rápido (Trailing Icon).
- **M2. Campo con Validación:** Etiqueta clara, input de altura ergonómica (48dp) y texto auxiliar de ayuda o error.
- **M3. Tarjeta de Métrica Resumen:** Cifra destacada en 22sp, icono temático, etiqueta descriptiva y comparativo semanal.
- **M4. Fila de Lista de Clientes:** Avatar con iniciales en alto contraste, nombre, domicilio, último servicio y botón de llamada / WhatsApp inmediato.
- **M5. Elemento de Checklist de Mantenimiento:** Checkbox accesible, título de tarea técnica (ej. "Limpieza de filtros"), estado y selector de completado con toque simple.
- **M6. Selector de Fotos Antes/Después:** Grilla de 2 columnas con previsualización de imagen, badge de fase técnica y botón de remoción inmediata.
- **M7. Fila de Seguimiento:** Datos de cliente, equipo que requiere mantenimiento, fecha límite y botón de mensaje directo por WhatsApp.
- **M8. Stepper Móvil de 3 Pasos:** Indicador horizontal compacto (1. Datos → 2. Trabajo → 3. Reporte) que guía el flujo de captura sin saturar la pantalla.

---

## 3. Organismos

- **O1. Top Bar de Aplicación:** Muestra el logo oficial de CierrePro, estado de conectividad/taller y avatar de perfil de acceso a "Mi Negocio".
- **O2. Bottom Navigation Bar persistente (4 destinos):** Inicio, Clientes, Servicios y Seguimientos, con destinos accesibles con el pulgar.
- **O3. Formulario de Creación de Servicio:** Ensambla selector de cliente registrado, matriz de tipos de trabajo (Preventivo, Instalación, Reparación, Inspección) y fecha programada.
- **O4. Módulo de Evidencias y Checklist:** Consolida el protocolo de mantenimiento para minisplits, toma fotográfica antes/después y captura de diagnóstico técnico.
- **O5. Documento Previsualizador de Reporte:** Maquetación documental técnica lista para cliente, con logotipo de taller, checklist verificado, fotos rotuladas, recomendaciones y bloque de firma en pantalla.
- **O6. Panel de Compartición:** Botones de alta visibilidad para "Enviar reporte por WhatsApp" y "Descargar / Compartir PDF".
- **O7. Lista de Seguimientos Agrupada:** Organización en secciones "Para hoy", "Próximos 7 días" y "Atrasados".

---

## 4. Plantillas y Pantallas del Flujo MVP

1. **Pantalla 1 — Inicio / Resumen:** Cuadro de mando del día, acción rápida "Nuevo servicio", métricas de carga y recordatorios inmediatos.
2. **Pantalla 2 — Clientes:** Directorio telefónico y residencial con filtros de seguimiento y acceso a histórico.
3. **Pantalla 3 — Detalle del Cliente:** Ficha de "María López", equipos instalados, historial de órdenes y botón de servicio directo.
4. **Pantalla 4 — Nuevo Servicio (Paso 1):** Configuración de orden, selección de cliente y equipo asignado.
5. **Pantalla 5 — Checklist y Evidencias (Paso 2):** Documentación en sitio con fotos comparativas antes/después y notas técnicas.
6. **Pantalla 6 — Reporte del Servicio y Entrega (Paso 3):** Hoja técnica profesional generada al instante con envío directo por WhatsApp y PDF.
7. **Pantalla 7 — Seguimientos:** Sistema de retención y recontacto preventivo semestral para clientes recurrentes.
8. **Pantalla 8 — Perfil del Negocio:** Configuración del membrete comercial ("ClimaExpress"), técnico certificado y mensaje de WhatsApp.
