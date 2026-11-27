## Liquidar una deuda técnica o de accesibilidad (`$TIPO = Deuda técnica | Accesibilidad`)

Se activa cuando la historia liquida (total o parcialmente) un `TD-SK-0XX` ya catalogado en `CLAUDE.md` §Deuda técnica, o una historia `A11Y-SK-` ya redactada (issue de GitHub). No se activa para una pantalla o componente nuevo sin deuda asociada.

### Reutilizar el ID, nunca crear uno nuevo

Si la deuda está registrada, reutiliza su ID. Para una tanda parcial usa sufijo: `TD-SK-004-FIX-WAVE1`, `TD-SK-006-FIX-ATHLETE`.

```bash
grep -n "$ID" CLAUDE.md README.md
gh issue list --state all --search "$ID" --json number,title,state   # si gh está instalado y autenticado
```

### Dónde está el detalle accionable

| Fuente | Qué aporta |
|---|---|
| `CLAUDE.md` §Deuda técnica | Fila del TD: descripción, cifra de la línea base (fecha en el título), severidad |
| `CLAUDE.md` §Convenciones / §Arquitectura | La regla que la deuda incumple y el estado objetivo |
| `README.md` (§1 tokens, §6.2 buenas prácticas, §6.3 testing, §8 pendientes) | Estado objetivo de diseño (p. ej. `LocalSpacing`, Roborazzi, `ic_belt`) |
| Issues de GitHub con el ID | Historias `TD-SK-0XX-FIX` ya redactadas: **si existe, parte de ella**, no la reescribas |
| `sportkid-governance:fitness-functions` | Conteo **actual** (la tabla de `CLAUDE.md` es una foto de su fecha) |

Lista además los archivos que violan la regla hoy, porque la tabla puede estar desactualizada. Desde la raíz del repo (los bloques corren bajo bash):

```bash
bash <<'EOF'
SRC="app/src/main core/designsystem/src/main core/model/src/main core/ui/src/main $(ls -d feature/*/src/main)"
THEME=core/designsystem/src/main/kotlin/com/invex/sportkid/core/designsystem/theme
# TD-SK-001 — tests existentes
find app core feature -path '*/build' -prune -o \( -path '*/src/test/*' -o -path '*/src/androidTest/*' \) -name '*Test.kt' -print
# TD-SK-002 — TODO por archivo (guardar en formularios, datos de Samples)
grep -rc --include='*.kt' -E '\bTODO\b' $SRC | grep -v ':0$' | sort -t: -k2 -rn
# TD-SK-003 — navegación por estado
wc -l app/src/main/java/com/invex/one/template/MainActivity.kt; grep -n 'enum class Destination' app/src/main/java/com/invex/one/template/MainActivity.kt
# TD-SK-004 — líneas con N.dp por archivo (feature/ y core:ui) y Color(0x…) fuera de theme/
grep -rc --include='*.kt' -E '\b[0-9]+(\.[0-9]+)?\.dp\b' $(ls -d feature/*/src/main) core/ui/src/main | grep -v ':0$' | sort -t: -k2 -rn
grep -rn --include='*.kt' -E '\bColor\(0x' $SRC | grep -v "^$THEME/"
# TD-SK-005 — archivos > 400 líneas
find $SRC -name '*.kt' -exec wc -l {} + | awk '$2 != "total" && $1 > 400' | sort -rn
# TD-SK-006 — contentDescription = null por archivo
grep -rc --include='*.kt' -E 'contentDescription\s*=\s*null' $SRC | grep -v ':0$' | sort -t: -k2 -rn
# TD-SK-007 — restos de plantilla
grep -n 'com\.invex\.one\.template' app/build.gradle.kts; for d in feature/*/; do grep -q "\":feature:$(basename $d)\"" settings.gradle.kts || echo "sin módulo: $d"; done
# TD-SK-008 — análisis estático y CI
ls config/detekt; grep -ln detekt build.gradle.kts app/build.gradle.kts gradle/libs.versions.toml; ls .github/workflows 2>/dev/null
# TD-SK-010 — reloj fuera del default `today` de las pantallas
grep -rn --include='*.kt' -E 'LocalDate\.now\(\)|YearMonth\.now\(\)|LocalTime\.now\(\)|System\.currentTimeMillis\(\)' $SRC | grep -vE 'Screen\.kt:[0-9]+:\s*today: LocalDate = LocalDate\.now\(\),'
EOF
```

TD-SK-009 (esquema oscuro de Océano, `ic_belt`, logo) es de diseño: su detalle está en README §1.1 y §8, no en un grep.

- Si el requerimiento nombra una pantalla, archivo o módulo, filtra a ese alcance (`feature/<x>/src/main`).
- Si no, agrupa por módulo (`feature:athlete`, `feature:tkd`, `core:ui`…) o por familia de componentes y propone olas acotadas: un PR de deuda no debería pasar de ~400 líneas cambiadas.
- Antes de reemplazar literales `N.dp` (TD-SK-004), revisa qué ofrece `LocalSpacing` / `SportKidTheme.spacing` (`core/designsystem/.../theme/Spacing.kt`): si el valor no existe como token de rol, la historia incluye la decisión de agregarlo (README §1.4) o de dejar el literal justificado (tamaños de icono, anchos de Figma).

### Cómo usar el detalle en la historia

- **Objetivo:** estado actual (conteo real + archivos) → estado objetivo, citando la regla de `CLAUDE.md` §Convenciones (o el README) que se cumple al terminar.
- **Criterios de aceptación:** un AC por archivo o grupo en alcance:

  ```
  - [ ] AC-N: Dado `feature/<x>/.../<Archivo>.kt` con [violación concreta, p. ej. 12 líneas con N.dp / 4 Icon con contentDescription = null en iconos con significado] cuando se aplica [solución: token de SportKidTheme.spacing / stringResource / división en secciones private] entonces el archivo deja de aparecer (o baja a N) en el grep de la línea FF-XX de `sportkid-governance:fitness-functions`.
  ```

- **Regresión visual:** toda deuda de tokens o de división de archivos incluye un AC de "sin cambio visual" (comparar `@SkPreviews` y capturas del emulador antes y después con `python3 scripts/ui-catalogo/capturar.py <ids>`).
- **Accesibilidad (TD-SK-006):** un AC por pantalla revisada: cada `contentDescription = null` que queda es decorativo; los iconos con significado reciben texto desde `strings.xml` del módulo.
- **Tests (TD-SK-001):** un AC por holder o formateador cubierto, con el comando exacto (`./gradlew :feature:<x>:testDebugUnitTest`); agregar `testImplementation(libs.junit)` al módulo si falta.
- **Capa de datos o navegación (TD-SK-002/003):** la primera tarea es la decisión registrada en `CLAUDE.md` (librería, módulo `core:data`, Navigation Compose con rutas tipadas); sin ella, la historia no se estima.
- **Alcance:** "Incluye" = archivos exactos del grep; "No incluye" = el resto de ocurrencias, para otra ola.

### Definition of Done específico

- Los archivos del alcance ya no aparecen en el grep (o bajan a la cifra comprometida).
- La cifra global del TD baja en la cantidad esperada (recalcular con `sportkid-governance:fitness-functions`) y ninguna otra métrica empeora.
- Fragmento `changelog.d/TD-SK-0XX.refactored.md` (o `fixed` si corrige algo visible).
- Actualizar la fila en `CLAUDE.md` §Deuda técnica y regenerar los espejos (vía `sportkid-governance:propagacion`) es una tarea posterior y solo se hace si el usuario la pide en el turno actual.
