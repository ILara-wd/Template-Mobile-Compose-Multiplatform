# Git hooks

Este directorio contiene los hooks de Git versionados del repositorio. Se
distribuyen con el código para que todos compartan la misma validación
automática de la convención de commits (sección «Convenciones» de `CLAUDE.md`).

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

> Pendiente: instalarlo automáticamente desde Gradle y validarlo en el pipeline de CI.

---

## Hooks disponibles

### `commit-msg`

Valida que cada mensaje de commit siga la convención del proyecto:

```
<tipo>(<ámbito>): <descripción imperativa en minúscula> [APP-<n>]

[cuerpo opcional]

[footer opcional]
```

- **`<tipo>`** obligatorio.
- **`(<ámbito>)`** opcional: feature o área, en minúscula y sin espacios.
- **`[APP-<n>]`** opcional: ticket, siempre **al final** del asunto.

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
| `docs` | Documentación (`docs/`, `CLAUDE.md`, README) |
| `chore` | Mantenimiento (limpieza, configuración del repo, hooks) |
| `build` | Gradle, dependencias, CI |
| `revert` | Revertir un commit previo |

#### Ámbitos sugeridos

`login`, `signup`, `students`, `profile`, `check-in`, `payments`, `dashboard`,
`components`, `theme`, `navigation`, `data`, `docs`, `deps`.

#### Ticket

`APP-<n>` es un **placeholder** hasta que exista el proyecto de tickets. Para
cambiar o agregar prefijos, edita `TICKET_PREFIXES` en `commit-msg` y
actualiza la suite de tests.

#### Ejemplos válidos

```
feat(payments): agregar detalle de cobro
fix(students): corregir filtro de búsqueda [APP-7]
refactor: extraer secciones de PaymentsSections
test(payments): cubrir PaymentDetailViewModel
docs(architecture): actualizar guía de contribución
build: subir lifecycle a la versión 2.9.0 [APP-21]
```

#### Ejemplos inválidos

```
Add payment detail feature                      ← sin tipo
feature: agregar pantalla                       ← tipo no válido
feat: [APP-1] agregar pantalla de perfil    ← ticket al inicio (va al final)
feat(Payments): agregar detalle de cobro        ← ámbito en mayúscula
feat: Agregar pantalla de perfil            ← descripción en mayúscula
feat: agregar pantalla de perfil.           ← punto final
feat!: cambiar contrato de InputField        ← no se usa el sufijo !
feat: agregar pantalla [JIRA-12]                ← prefijo de ticket no aceptado
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

La suite versionada ejecuta ~66 casos (válidos e inválidos) contra el hook con
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
echo "feat(payments): agregar detalle de cobro [APP-3]" > "$tmp"
bash .githooks/commit-msg "$tmp"; echo "exit code: $?"
rm -f "$tmp"
```

### Casos cubiertos por la suite

| Categoría | Ejemplos |
|-----------|----------|
| Tipos válidos | `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `build`, `revert` |
| Ámbito y ticket opcionales | `feat(payments): …`, `fix(check-in): …`, `… [APP-7]` |
| Auto-normalización | `feat:x`, `feat:   x`, `feat:\tx` → `feat: x` (y el archivo se reescribe) |
| Estructura y caracteres | cuerpo separado, acentos, emojis, CRLF, comentarios `#`, líneas blancas |
| Generados por Git (se saltan) | merge, revert, `fixup!`, `squash!`, `amend!` |
| Bypasses | `[skip-validation]`, `SKIP_COMMIT_MSG_HOOK=1` |
| Tipos inválidos | `feature`, `update`, `FEAT`, `perf`, `style`, `ci`, sufijo `!` |
| Ámbito inválido | mayúscula, espacio, vacío, espacio antes del paréntesis |
| Ticket inválido | al inicio, en medio, prefijo no aceptado, minúscula, sin dígitos |
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
  (`feat(payments)`, `fix(students)`), y facilita generar notas de versión.
- **Ticket opcional al final** → el proyecto aún no tiene gestor de tickets; cuando
  lo tenga, la referencia no estorba la lectura del asunto.
- **Descripción imperativa en minúscula** → estilo consistente con la guía de Git
  ("If applied, this commit will…").
