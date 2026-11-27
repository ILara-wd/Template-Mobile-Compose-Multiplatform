# Git hooks de SportKid

Este directorio contiene los hooks de Git versionados del repositorio. Se
distribuyen con el código para que todos compartan la misma validación
automática de la convención de commits de `CLAUDE.md` (Convenciones → Commits).

---

## Instalación

Una sola vez por clon:

```bash
git config core.hooksPath .githooks
chmod +x .githooks/commit-msg .githooks/tests/test-commit-msg.sh
```

Verificar que quedó activo:

```bash
git config --get core.hooksPath
# → .githooks
```

Para desactivarlo (volver al comportamiento por defecto de Git):

```bash
git config --unset core.hooksPath
```

> Pendiente (TD-SK-008): instalarlo automáticamente desde Gradle y validarlo
> en un workflow de GitHub Actions.

---

## Hooks disponibles

### `commit-msg`

Valida que cada mensaje de commit siga la convención del proyecto:

```
<tipo>(<ámbito>): <descripción imperativa en minúscula> (#<n>)

[cuerpo opcional]

[footer opcional]
```

- **`<tipo>`** obligatorio.
- **`(<ámbito>)`** opcional: módulo o área, en minúscula y sin espacios.
- **`(#<n>)`** opcional: issue o PR de GitHub, siempre **al final** del asunto.

> El hook deja **exactamente un espacio** después de `<tipo>(<ámbito>):`. Si
> escribes `feat:agregar…`, `feat:   agregar…` o `feat:\tagregar…`, reescribe el
> mensaje a `feat: agregar…` y avisa.

#### Tipos válidos

| Tipo | Uso |
|------|-----|
| `feat` | Nueva funcionalidad o pantalla |
| `fix` | Corrección de bug |
| `refactor` | Cambio interno sin alterar comportamiento |
| `test` | Agregar o corregir tests |
| `docs` | Documentación (`docs/`, `CLAUDE.md`, README, catálogo) |
| `chore` | Mantenimiento (limpieza, configuración del repo, hooks) |
| `build` | Gradle, dependencias, CI |
| `revert` | Revertir un commit previo |

#### Ámbitos sugeridos

Módulos: `app`, `designsystem`, `model`, `ui`, `auth`, `athlete`, `agenda`, `home`,
`tkd`, `swimming`, `athletics`, `sharing`, `settings`, `expenses`.
Áreas: `theme`, `navigation`, `catalogo`, `docs`, `deps`, `hooks`.

#### Ticket

Número de issue o PR de GitHub entre paréntesis al final: `(#12)`. Es el mismo
sufijo que agrega GitHub al hacer *squash merge*, así que esos commits pasan sin
cambios. Si el formato cambia, edita la sección del ticket en `commit-msg` y
`TICKET_RE` en `.claude/scripts/validate-pullrequest-lib.sh`, y actualiza la
suite de tests.

#### Ejemplos válidos

```
feat(home): agregar banner de la semana
fix(agenda): corregir filtro por deportista (#7)
refactor: extraer secciones de AthleteProfileScreen
test(agenda): cubrir AgendaState con filtros por deporte
docs(catalogo): agregar pestaña de assets
build: subir lifecycle a la versión 2.9.0 (#21)
```

#### Ejemplos inválidos

```
Add home screen                                 ← sin tipo
feature: agregar pantalla                       ← tipo no válido
feat: (#1) agregar pantalla de cinturones       ← ticket al inicio (va al final)
feat(Home): agregar banner de la semana         ← ámbito en mayúscula
feat: Agregar pantalla de cinturones            ← descripción en mayúscula
feat: agregar pantalla de cinturones.           ← punto final
feat!: cambiar contrato de SkTextField          ← no se usa el sufijo !
feat: agregar pantalla [JIRA-12]                ← formato de ticket no aceptado (usa (#n))
```

Además: descripción de al menos 10 caracteres, asunto de máximo 100 (recomendado
72) y, si hay cuerpo, una línea en blanco entre asunto y cuerpo.

#### Casos que el hook NO valida (se saltan automáticamente)

- `Merge …` generados por Git.
- `Revert "…"` generados por `git revert`.
- `fixup!`, `squash!`, `amend!` (se consolidan con `rebase --autosquash`).

#### Bypass (usar con criterio)

```bash
# 1. Bypass nativo de Git (no ejecuta ningún hook)
git commit --no-verify -m "..."

# 2. Variable de entorno (útil en scripts puntuales)
SKIP_COMMIT_MSG_HOOK=1 git commit -m "..."

# 3. Marca en el mensaje (deja huella explícita en el historial)
git commit -m "chore: mensaje especial [skip-validation]"
```

---

## Testing del hook (sin afectar el repo)

La suite versionada ejecuta 68 casos (válidos e inválidos) contra el hook con
archivos temporales: **no hace commits, no toca el estado de git**.

```bash
./.githooks/tests/test-commit-msg.sh          # ejecuta la suite
./.githooks/tests/test-commit-msg.sh -v       # imprime input/output por caso
```

Exit code `0` si todo pasa, `1` si al menos un caso falla (los fallidos
imprimen la salida real del hook).

Probar un mensaje puntual:

```bash
tmp=$(mktemp)
echo "feat(home): agregar banner de la semana (#3)" > "$tmp"
bash .githooks/commit-msg "$tmp"; echo "exit code: $?"
rm -f "$tmp"
```

### Casos cubiertos por la suite

| Categoría | Ejemplos |
|-----------|----------|
| Tipos válidos | `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `build`, `revert` |
| Ámbito y ticket opcionales | `feat(home): …`, `fix(ui-catalogo): …`, `… (#7)`, `[beta]` no es ticket |
| Auto-normalización | `feat:x`, `feat:   x`, `feat:\tx` → `feat: x` (y el archivo se reescribe) |
| Estructura y caracteres | cuerpo separado, acentos, emojis, CRLF, comentarios `#`, líneas blancas |
| Generados por Git (se saltan) | merge, revert, `fixup!`, `squash!`, `amend!` |
| Bypasses | `[skip-validation]`, `SKIP_COMMIT_MSG_HOOK=1` |
| Tipos inválidos | `feature`, `update`, `FEAT`, `perf`, `style`, `ci`, sufijo `!` |
| Ámbito inválido | mayúscula, espacio, vacío, espacio antes del paréntesis |
| Ticket inválido | al inicio, en medio, `[#n]`, `[JIRA-n]`, `[KEN-n]`, sin dígitos |
| Descripción inválida | < 10 caracteres, mayúscula inicial, punto final |
| Formato del asunto | > 100 caracteres, cuerpo sin línea en blanco, espacio inicial |

### ⚠️ Qué NO hacer para testear

- **No uses `git commit --allow-empty -m "..."`**: crea commits reales que luego
  hay que deshacer.
- **No modifiques `commit-msg` sin correr la suite.**

---

## Compatibilidad

- **macOS / Linux:** `bash` 3.2+ (preinstalado). Sin dependencias externas.
- **Windows:** Git Bash / WSL. El hook normaliza CRLF automáticamente.

---

## ¿Por qué esta convención?

- **Tipo y ámbito al inicio** → `git log --oneline` se lee por área
  (`feat(home)`, `fix(agenda)`), y facilita generar notas de versión.
- **Ticket opcional al final** → coincide con el `(#n)` que agrega GitHub al
  hacer squash merge y no estorba la lectura del asunto.
- **Descripción imperativa en minúscula** → estilo consistente con la guía de Git
  ("If applied, this commit will…").
