## Liquidar una deuda técnica o de accesibilidad (`$TIPO = Deuda técnica | Accesibilidad`)

Se activa cuando la historia liquida (total o parcialmente) un `TD-KEN-XXX` o una historia `A11Y-KEN-` ya catalogada. No se activa para una pantalla o componente nuevo sin deuda asociada.

### Reutilizar el ID, nunca crear uno nuevo

Si la deuda está registrada, reutiliza su ID. Para una tanda parcial usa sufijo: `TD-KEN-002-FIX-WAVE1`, `TD-KEN-006-FIX-PAYMENTS`.

```bash
grep -n "$ID" docs/architecture/01-governance.md docs/architecture/06-compliance-report.md docs/architecture/07-user-stories.md
```

### Dónde está el detalle accionable

| Fuente | Qué aporta |
|---|---|
| `01-governance.md §6` | Fila del TD: descripción, severidad, evidencia |
| `06-compliance-report.md` | Hallazgos con `archivo:línea` y conteos verificados |
| `07-user-stories.md` | Historias `TD-KEN-0XX-FIX` ya redactadas: **si existe, parte de ella**, no la reescribas |
| `kensho-governance:fitness-functions` | Conteo **actual** (los documentos son una foto de su fecha) |

Lista además los archivos que violan la regla hoy, porque los documentos pueden estar desactualizados:

```bash
M=app/src/main/java/dev/treecode/kensho
# ejemplos según el TD:
find app/src/test app/src/androidTest -name '*Test.kt'                                   # TD-KEN-001
grep -rn "contentDescription = null" $M | grep -v "//"                                   # TD-KEN-002
grep -rnE "\bColor\(0x|[0-9]\.(dp|sp)\b" $M | grep -v /ui/theme/                         # TD-KEN-004
grep -rnE "LocalDate\.now\(|System\.currentTimeMillis\(" $M | grep -v /data/             # TD-KEN-005
find $M -name '*.kt' -exec wc -l {} + | awk '$2 != "total" && $1 > 300'                  # TD-KEN-006
```

- Si el requerimiento nombra una pantalla o archivo, filtra a ese alcance.
- Si no, agrupa por feature (`payments/`, `profile/`…) o por familia de componentes y propone olas acotadas: un PR de deuda no debería pasar de ~400 líneas cambiadas.

### Cómo usar el detalle en la historia

- **Objetivo:** estado actual (conteo real + archivos) → estado objetivo, citando el ADR.
- **Criterios de aceptación:** un AC por archivo o grupo en alcance:

  ```
  - [ ] AC-N: Dado `<feature>/<Archivo>.kt` con [violación concreta, p. ej. 4 Icon con contentDescription = null sin comentario] cuando se aplica [solución: stringResource / comentario de decorativo / token] entonces el archivo deja de aparecer en [grep de la fitness function].
  ```

- **Regresión visual:** toda deuda de tokens o de división de archivos incluye un AC de "sin cambio visual" (comparar previews o capturas del emulador antes y después).
- **Traducción (TD-KEN-011):** un AC por pantalla traducida, verificando que no queden cadenas en inglés en su bloque de `strings.xml`.
- **Alcance:** "Incluye" = archivos exactos del grep; "No incluye" = el resto de ocurrencias, para otra ola.

### Definition of Done específico

- Los archivos del alcance ya no aparecen en el grep de la fitness function.
- El conteo global del TD baja en la cantidad esperada (recalcular con `kensho-governance:fitness-functions`).
- Actualizar `06-compliance-report.md`, `01-governance.md §6` y `CLAUDE.md` (vía `kensho-governance:propagacion`) es una tarea posterior y solo se hace si el usuario la pide en el turno actual.
