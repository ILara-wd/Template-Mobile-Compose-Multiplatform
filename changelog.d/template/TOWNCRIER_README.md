# 📋 Changelog con towncrier — Template

> `CHANGELOG.md` se genera con [towncrier](https://towncrier.readthedocs.io/) a partir de **fragmentos**: archivos de una línea en `changelog.d/`, uno por cambio. Cada PR trae su fragmento; nadie edita `CHANGELOG.md` a mano.

> **Plantilla base.** Esta guía usa nombres genéricos: `Template` como nombre del proyecto y `APP` como prefijo de tickets. Al crear un proyecto nuevo desde la plantilla, sigue la [sección 7](#7-adaptar-la-plantilla-a-un-proyecto-nuevo).

---

## 1. Instalación

```bash
pipx install towncrier      # o: pip install towncrier
towncrier --version         # probado con 26.9
```

---

## 2. Estructura

```
<raíz del repo>/
├── CHANGELOG.md                 # lo escribe towncrier debajo de <!-- towncrier release notes start -->
├── config/
│   ├── towncrier.toml           # configuración (rutas relativas a config/)
│   └── towncrier.md.jinja2      # plantilla: ## [versión] - fecha + secciones con contenido
└── changelog.d/                 # fragmentos, en la raíz de la carpeta (sin subcarpetas)
    ├── APP-12.added.md
    ├── TD-004.refactored.md
    ├── +fuente-escalable.fixed.md
    └── template/
        └── TOWNCRIER_README.md  # esta guía (ignorada por towncrier)
```

---

## 3. Crear un fragmento

En el **mismo PR** que el código. Nombre: `changelog.d/<id>.<tipo>.md`.

### Identificador

| `<id>` | Cuándo | Ejemplo |
|--------|--------|---------|
| `APP-<n>` | El cambio tiene ticket en el gestor del proyecto | `APP-12.added.md` |
| `TD-0XX` | Liquida o reduce una deuda técnica registrada | `TD-004.refactored.md` |
| `+<slug>` | Sin ticket | `+fuente-escalable.fixed.md` |

El `<slug>` va en minúsculas con guiones y debe ser único en `changelog.d/`. Un mismo ticket puede tener varios fragmentos de tipos distintos (`APP-12.added.md` y `APP-12.deprecated.md`).

### Tipo

| `<tipo>` | Sección | Cuándo | Commit típico |
|----------|---------|--------|---------------|
| `added` | Agregado | Pantalla, componente, variante, token o herramienta nueva | `feat` |
| `changed` | Cambiado | Cambio visible en algo existente (diseño, flujo, texto) | `feat` / `fix` |
| `refactored` | Refactorizado | Sin cambio visible: deuda, tokens, división de archivos, tests | `refactor` / `test` |
| `deprecated` | Obsoleto | Componente o parámetro público marcado con `@Deprecated` | `refactor` |
| `removed` | Eliminado | Componente, pantalla o recurso quitado | `refactor` / `chore` |
| `fixed` | Corregido | Corrección de un error | `fix` |
| `security` | Seguridad | Parche de seguridad o de datos personales | `fix` |

### Contenido

**Una línea**, en español, describiendo el cambio para quien usa o mantiene la app (no el detalle de implementación). Sin el ticket: towncrier lo agrega desde el nombre del archivo.

```
Pantalla de perfil con edición de nombre y foto
```

### No necesitan fragmento

PRs que solo tocan documentación (`docs/`, `README.md`, `CLAUDE.md`), tooling del repo (`.claude/`, `.github/`, `.githooks/`) o tests sin cambio de comportamiento. Ante la duda, agrégalo.

---

## 4. Revisar antes del PR

```bash
towncrier check --config config/towncrier.toml --compare-with origin/main      # ¿la rama trae fragmento?
towncrier build --config config/towncrier.toml --version 1.1.0 --draft         # vista previa, no escribe nada
```

Cambia `origin/main` por la rama de integración del proyecto (p. ej. `origin/develop`).

---

## 5. Publicar una versión

Quien publica, desde la raíz del repo y en la rama de release:

```bash
# 1. Subir versionName (y versionCode) en app/build.gradle.kts — Versionado Semántico
# 2. Generar: escribe en CHANGELOG.md y hace git rm de los fragmentos consumidos
towncrier build --config config/towncrier.toml --version 1.1.0 --date 2026-11-26

# 3. Commit
git add CHANGELOG.md changelog.d/ app/build.gradle.kts
git commit -m "chore(release): publicar versión 1.1.0"
```

Resultado en `CHANGELOG.md` (solo aparecen las secciones con fragmentos):

```markdown
## [1.1.0] - 2026-11-26

### Agregado

- Pantalla de perfil con edición de nombre y foto (APP-12)

### Corregido

- Barras y tarjetas legibles con fuente del sistema a 150 % y 200 %
```

Si el proyecto publica por semana en vez de por versión, usa la semana como versión: `--version 2026.w.48`.

---

## 6. Preguntas frecuentes

**¿Olvidé el fragmento y la versión ya se publicó?** Agrégalo en un PR nuevo; saldrá en la siguiente versión. No edites la sección ya publicada de `CHANGELOG.md`.

**`towncrier build` falla con `Invalid news fragment name`.** Hay un archivo con nombre o tipo inválido en `changelog.d/` (p. ej. `APP-12.feat.md` o `APP-12.md`). Renómbralo con un tipo de la tabla. `.DS_Store` y `template/` ya están ignorados.

**¿Puedo escribir varias líneas?** Evítalo: cada fragmento es una viñeta. Si el cambio necesita explicación, va en la descripción del PR.

---

## 7. Adaptar la plantilla a un proyecto nuevo

1. **Nombre:** cambia `Template` por el nombre de la app en `config/towncrier.toml` (`name`), en el encabezado de `CHANGELOG.md` y en el título de esta guía.
2. **Prefijo de tickets:** reemplaza `APP` por la clave del proyecto en el gestor (Jira, Linear…) en esta guía. Las deudas técnicas pueden llevar el mismo prefijo (`TD-<CLAVE>-0XX`).
3. **Links a tickets:** cuando exista el gestor, cambia `issue_format` en `config/towncrier.toml` por el link, p. ej. `"[{issue}](https://<gestor>/browse/{issue})"`.
4. **Historial limpio:** `CHANGELOG.md` arranca solo con el encabezado y el marcador `<!-- towncrier release notes start -->`, y `changelog.d/` sin fragmentos (solo `template/`).

---

_Última actualización: 2026-11-26_
