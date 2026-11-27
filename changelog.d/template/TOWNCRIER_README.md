# Fragmentos del changelog (towncrier)

`CHANGELOG.md` no se edita a mano. Cada PR que cambia `src/main` deja **un archivo por cambio** en `changelog.d/`
y towncrier los junta al cerrar la versión. Configuración: [`config/towncrier.toml`](../../config/towncrier.toml).

## Nombre del archivo

```
changelog.d/<id>.<tipo>.md
```

- `<id>`: número de issue de GitHub (`42`), deuda (`TD-SK-004`) o, sin ticket, `+<slug>` (`+inicio-banner`).
- `<tipo>`: `added` · `changed` · `refactored` · `deprecated` · `removed` · `fixed` · `security`.

## Contenido

Una línea en español, en pasado o presente, escrita para quien usa la app (qué ve o qué puede hacer), no para quien
lee el código:

```bash
echo "Inicio muestra el próximo compromiso de la semana en un banner." > changelog.d/+inicio-banner.added.md
```

## Generar la versión

```bash
pip install towncrier                                                       # una vez
towncrier build --config config/towncrier.toml --version 2026.w.48 --draft  # vista previa
towncrier build --config config/towncrier.toml --version 2026.w.48 --yes    # escribe CHANGELOG.md y borra los fragmentos
```
