## Integración con el gestor de tickets (GitHub Issues) — lectura y escritura

> SportKid no tiene un proyecto de Jira ni otro gestor configurado: los tickets son **issues de GitHub** del repositorio `ILara-wd/Template-Mobile-Compose-Multiplatform` (rama de trabajo `sport_kids`) y se citan como `#<n>`. El nombre del archivo se conserva por compatibilidad con el comando. Si algún día se adopta Jira u otro gestor, actualiza esta sección, la referencia `#<n>` de `tech-story-architect` y, si aplica, el patrón de ticket de `.githooks/commit-msg` y `.claude/scripts/validate-pullrequest-lib.sh`.

### Precondición

Usa el CLI `gh` (`gh auth status` debe indicar sesión activa). **Si `gh` no está instalado o no está autenticado**, no intentes acceder por otros medios (ni `curl` con tokens, ni credenciales de git): pide al usuario que pegue el título y la descripción del issue y continúa con ese texto como requerimiento. Puede haber un MCP de Atlassian disponible en la sesión, pero no hay un proyecto de SportKid ahí: no lo uses salvo que el usuario dé la clave de un proyecto real.

### Lectura (si el requerimiento viene de un issue)

Obtén el issue real **antes** de clasificar — nunca clasifiques a partir del solo número:

```bash
gh issue view <n> --json number,title,body,labels,milestone,state,url
gh issue view <n> --comments        # solo si el cuerpo remite a la discusión
```

Pide solo los campos necesarios. `title` y `body` son el **requerimiento crudo** que alimenta la clasificación de `tech-story-architect` — no los reformules antes de clasificar.

Si el issue menciona un archivo, patrón o síntoma que calza con una fila de `CLAUDE.md` §Deuda técnica (p. ej. `contentDescription = null`, literales `N.dp`, archivo grande, `TODO` al guardar, test faltante, navegación en `MainActivity.kt`), reutiliza ese `TD-SK-0XX` y sigue con `tech-story-architect:liquidar-deuda`. Si trae un link de Figma (archivo `KqTFktvrRvCzO2idPga5xf`), cítalo en el campo **Diseño**.

La lectura **no** implica escribir de vuelta.

### Escritura (solo con confirmación explícita del usuario en el turno actual)

Ambas acciones son visibles para el equipo: nunca se ejecutan solo porque se redactó la historia. Escribe la historia en un archivo temporal del scratchpad y pásalo con `--body-file` (el Markdown de GitHub se respeta tal cual).

1. **Crear el issue:**

   ```bash
   gh issue create --title "[$ID] <título>" --body-file <scratchpad>/historia.md [--label <etiqueta>]
   ```

   Usa solo etiquetas que ya existan (`gh label list`); no crees etiquetas sin pedirlo.

2. **Rellenar el issue de origen:** `gh issue edit <n> --body-file <scratchpad>/historia.md` **reemplaza** la descripción actual; confírmalo antes. Si el usuario prefiere conservarla, o la edición falla, agrega la historia como comentario (`gh issue comment <n> --body-file …`) y reporta el error tal cual.

Tras crear el issue, usa su número en el encabezado de la historia (`**Issue:** #<n>`), en el commit (`feat(<módulo>): … (#<n>)` si el usuario lo pide) y en el fragmento `changelog.d/<n>.<tipo>.md`.
