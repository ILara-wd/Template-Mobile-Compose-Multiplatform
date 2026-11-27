# Prompts de Stitch: onboarding "Mi dojo"

Prompts para generar en [Google Stitch](https://stitch.withgoogle.com) el diseño de la configuración del dojo
(programas con escalera de rangos, horarios y plan de cobro). Las reglas salen de
[`docs/api/dojo-config-api.md`](../../api/dojo-config-api.md) y el flujo, de
[`docs/android-integration-guide.md`](../../android-integration-guide.md) §3–6. Los colores y la tipografía son
los tokens de `ui/theme/` (`TkdColors`, `TkdTypography`, `TkdDimens`).

## Cómo usarlos

1. Crea un proyecto **Mobile** en Stitch.
2. Pega el [prompt base](#0-prompt-base) **una sola vez**, al inicio: fija el sistema visual y el contexto.
3. Pide **una pantalla por mensaje**, en orden (1 → 5). Stitch es más fiel con una pantalla a la vez.
4. Al final de cada pantalla, pide sus [estados comunes](#estados-comunes).
5. Exporta a Figma (**Copy to Figma**) dentro de `Prototipo-Demo-UiKit` y comparte el nodo para pasar al flujo
   Figma → código de `CLAUDE.md`.

Si una pantalla no sale bien, corrige con un mensaje corto en lugar de repetir el prompt completo, por ejemplo:
"Usa el rojo #C01025 en el CTA y quita las sombras de las tarjetas".

| # | Pantalla | Para qué |
|---|----------|----------|
| 0 | Prompt base | Sistema visual y contexto (no genera pantalla) |
| 1 | Mi dojo | Resumen y checklist de lo que falta para dar de alta alumnos |
| 2 | Programa | Crear o editar un programa con su escalera de rangos |
| 3 | Rango | Hoja inferior para editar un rango y sus requisitos |
| 4 | Horario | Crear o editar un horario con días, horas y cupo |
| 5 | Plan de cobro | Cuota, moneda, inscripción, día de corte y lada |

---

## 0. Prompt base

```text
App móvil Android (412×915) "Kensho" para que un sensei administre su dojo de artes marciales.
Idioma de toda la UI: español de México. Estilo limpio, profesional, deportivo; Material 3.

Sistema visual (respétalo exacto):
- Fondo #F8F9FF; tarjetas blancas #FFFFFF con borde #E5BDBA al 40 % y radio 16; campos con radio 12 y borde #E5BDBA.
- Primario rojo #C01025 (botones principales, selección, pasos activos), primario oscuro #960017,
  contenedor primario #FFDAD7. Texto principal #0B1C30, secundario #565E74, placeholder #906F6D.
- Superficies secundarias azuladas #EFF4FF / #E5EEFF. Éxito #059669 con fondo #ECFDF5.
- Tipografía Plus Jakarta Sans: títulos 28 ExtraBold, encabezados 20 Bold, títulos de tarjeta 17 SemiBold,
  cuerpo 14, etiquetas 12–13 Medium.
- Barra superior con botón de regreso circular y título; CTA principal ancho completo, alto 56, rojo.
- Objetivos táctiles ≥ 48 dp. Cada campo con etiqueta visible arriba y mensaje de error rojo debajo.
- Cinturones como una barra horizontal redondeada del color de la cinta, con hasta 10 rayas blancas finas
  en el extremo derecho: blanco, amarillo, naranja, verde, azul, morado, rojo, café, negro.

Contexto: un dojo recién registrado NO puede dar de alta alumnos hasta tener (1) al menos un programa
(disciplina) con su escalera de rangos y (2) al menos un horario. El plan de cobro es opcional.
Disciplinas configurables: Karate, Taekwondo, Brazilian Jiu-Jitsu.
```

---

## 1. Mi dojo (resumen y checklist)

```text
Pantalla "Mi dojo". Arriba: nombre del dojo "Kensho Dojo" y chip con la disciplina principal "Karate".
Tarjeta de progreso "Configura tu dojo" con 3 pasos numerados y su estado (check verde o círculo vacío):
  1 "Programa y escalera de rangos" (requerido), 2 "Horarios" (requerido), 3 "Plan de cobro" (opcional).
  Barra de progreso "2 de 3". Aviso cuando falta algo requerido: "Aún no puedes dar de alta alumnos:
  agrega al menos un programa con rangos y un horario."
Sección "Programas": tarjetas por programa con nombre ("Shotokan Karate"), disciplina, "7 rangos",
  "Asistencia mínima 85 %", "12 alumnos", y una mini fila de cinturones de colores. Botón "+ Agregar programa".
Sección "Horarios": tarjetas "Lunes y Miércoles • 17:30 a 18:45", disciplina, descripción "Tatami A",
  ocupación "8 / 20 alumnos" con barra; si está lleno, badge "Sin cupo". Botón "+ Agregar horario".
Sección "Plan de cobro": "Cuota mensual $650.00 MXN", badge verde "Inscripción exenta", "Corte: día 05",
  "Lada +52", acción "Editar".
CTA fijo abajo "Dar de alta alumnos", deshabilitado (gris) mientras falten pasos requeridos.
Muestra también la variante vacía: dojo sin programas ni horarios, con ilustración simple y CTA
"Crear mi primer programa".
```

---

## 2. Programa (crear o editar)

```text
Pantalla "Nuevo programa" (variante editar: "Editar programa").
Campos: selector de disciplina en tarjetas (Karate / Taekwondo / Brazilian Jiu-Jitsu; las que ya tienen
programa aparecen deshabilitadas con "Ya configurado"); "Nombre del programa" (máx. 120, contador);
"Asistencia mínima" con sufijo % (0–100); "Rayas máximas por cinta" con stepper − / + (0–10).
Sección "Escalera de rangos" (1 a 40, en orden de progresión, del primero al más alto):
  botón secundario "Usar escalera sugerida para Karate";
  lista ordenada con asa de arrastre, número de posición, barra del cinturón, grado ("10.º kyu", "1.er dan"
  o "Sin grado"), "24 clases", verificador "Sensei Tanaka", "2 requisitos", y menú ⋮ (editar, eliminar).
  Un rango con alumnos muestra candado y "3 alumnos": no se puede eliminar.
  Botón "+ Agregar rango".
Errores: banner rojo superior para un 409: "No puedes quitar rangos con alumnos: Amarillo 9.º kyu, Verde 6.º kyu".
  Errores por campo bajo cada input ("Este campo es obligatorio", "Fuera del rango permitido", "Está repetido").
Pie: CTA "Guardar programa"; en editar, botón de texto rojo "Eliminar programa", deshabilitado con nota
"Tiene 12 alumnos; no se puede eliminar".
```

---

## 3. Rango (hoja inferior)

```text
Hoja inferior modal "Rango 3 de 7".
Selector de cinturón en cuadrícula de 9 colores con nombre (Blanco … Negro), el elegido con borde rojo.
Grado: segmentado "Kyu / Kup / Dan / Sin grado" + número 1–20 con stepper (oculto si "Sin grado").
"Clases requeridas" numérico (0–1000). "Verificador" opcional (máx. 120), placeholder "Sensei Tanaka".
Sección "Requisitos del examen" (hasta 30): cada uno con título (requerido, 120) y descripción opcional (500),
reordenables y con botón eliminar; "+ Agregar requisito".
Error de duplicado: "Ya existe este cinturón con el mismo grado en la escalera".
Botones: "Cancelar" (texto) y "Guardar rango" (rojo).
```

---

## 4. Horario (crear o editar)

```text
Pantalla "Nuevo horario".
Disciplina: chips solo de las disciplinas con programa; si el horario tiene alumnos, el campo aparece
bloqueado con candado y nota "No se puede cambiar: tiene 8 alumnos".
Días: fila de 7 chips circulares L M M J V S D, selección múltiple (al menos uno).
Hora de inicio y hora de fin con selector de reloj (24 h); error "La hora de fin debe ser mayor que la de inicio".
"Descripción" opcional (máx. 200), placeholder "Tatami A • Sensei Tanaka • Infantil".
Cupo: interruptor "Sin límite de cupo"; al apagarlo, campo numérico 1–1000. Debajo, ocupación actual
"8 alumnos activos".
Vista previa de cómo se verá en la ficha de alta: "Lunes y Miércoles • 17:30 a 18:45".
Pie: CTA "Guardar horario"; en editar, "Eliminar horario" deshabilitado si tiene alumnos.
```

---

## 5. Plan de cobro

```text
Pantalla "Plan de cobro".
Campos: "Cuota mensual" con prefijo $ y teclado decimal ("650.00"); "Moneda" (MXN, USD; 3 letras);
interruptor "Inscripción exenta"; "Día de corte" selector 1–28 ("Día 05 de cada mes");
"Lada telefónica" ("+52", formato + y 1–3 dígitos), explicando que se antepone al teléfono de los alumnos.
Tarjeta de resumen como en la ficha de alta: "Cuota mensual pactada $650.00 MXN/mes", badge
"Inscripción exenta", "Corte: Día 05 de cada mes".
CTA "Guardar plan". Mensajes de error bajo cada campo.
```

---

## Estados comunes

Pídelos después de cada pantalla, cambiando el nombre:

```text
Genera los estados de la pantalla "<nombre>": cargando con skeleton de las tarjetas; error de red con
ilustración simple, mensaje "No pudimos cargar la configuración" y botón "Reintentar"; guardando, con
spinner dentro del CTA y los campos deshabilitados; y snackbar de éxito "Cambios guardados".
```

---

## Qué revisar antes de pasar a código

- Colores, radios y tipografía coinciden con el prompt base (sin sombras fuertes ni otros rojos).
- Todos los textos están en español de México, sin mezclar inglés.
- Cada campo tiene su etiqueta visible y su mensaje de error debajo.
- Los botones deshabilitados explican por qué ("Tiene 12 alumnos…").
- Texto largo (nombres de programa, descripciones) no se corta: la app escala la fuente hasta 200 % (ADR-015).
