# Changelog

Cambios relevantes de SportKid, del más reciente al más antiguo.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/). Mientras la app no se publique, las
versiones se nombran por semana ISO (`2026.w.NN`); al publicar se usará el `versionName` de `app/build.gradle.kts`.

**No se edita a mano.** Cada PR agrega un fragmento en `changelog.d/` y la versión se genera con towncrier;
ver [`changelog.d/template/TOWNCRIER_README.md`](changelog.d/template/TOWNCRIER_README.md).

<!-- towncrier release notes start -->

## [2026.w.47] - 2026-11-19

### Agregado

- El catálogo de UI (`docs/ui-catalogo/index.html`) tiene una pestaña Assets con los iconos e imágenes de cada módulo, para reutilizarlos antes de exportarlos otra vez de Figma.

## [2026.w.46] - 2026-11-09

### Agregado

- Inicio (P05) es la pestaña inicial: chips por hijo que filtran todo, banner con el próximo compromiso de la semana, resumen de logros y gastos, y el botón «Registrar» que abre «¿Qué quieres registrar?».

### Cambiado

- La app se llama «Sport Kids».

## [2026.w.45] - 2026-11-07

### Agregado

- Sistema de diseño SportKid en `core:designsystem`: tema «Océano» ajustado a Figma, temas MaterialKolor, colores fijos de deporte, medallas, estados, hijos y cintas, tipografía Plus Jakarta Sans y átomos `Sk*` con previews claro, oscuro y fuente 1.3.
- Splash, inicio de sesión, bienvenida y alta de deportista (P01–P04).
- Agenda del deportista con vista de mes y lista, filtros por hijo y deporte, y registro de entrenamiento (P06, P07).
- Lista de deportistas y perfil con pestañas por deporte (P08).
- Taekwondo: trayectoria de cintas, historial de promociones, torneos, registro de examen de cinta y de torneo (P09–P11).
- Natación: estilos dominados, mejores tiempos, competencias y registro de competencia acuática (P12, P13).
- Atletismo: mejores marcas, medallero, carreras y registro de carrera (P14, P15).
- Gastos del mes por hijo y categoría (P16).
- Compartir el perfil con un QR, unirse con código y vista previa con sincronización (P17, P18).
- Miembros y accesos de cada deportista (P19).
- Ajustes y selección de tema y modo oscuro, que se aplican al momento y se recuerdan (P20, P21).
- Catálogo de UI generado desde el código con capturas del emulador; en debug, los extras `sk_*` abren cualquier pantalla directo.

### Refactorizado

- Las piezas de formulario (`FormCard`, `FormDateTimeFields`, `FeeRow`, `FormSaveBar`, `AthleteSportHeader`) se comparten desde `core:ui` entre Taekwondo, Natación, Atletismo y Agenda.
- Los avatares de todas las pantallas usan `SkAvatar`.
