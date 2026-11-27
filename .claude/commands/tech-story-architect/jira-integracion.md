## Integración con el gestor de tickets (Jira u otro) — lectura y escritura

> **Opcional.** Kensho todavía no tiene un proyecto de tickets configurado. La clave `KEN-<n>` es un **placeholder** de convención (`03-standards.md` GIT-01). Cuando se cree el proyecto real, actualiza la clave aquí, en `TICKET_RE` de `.claude/scripts/validate-pullrequest-lib.sh` y en `.githooks/commit-msg` (`TICKET_PREFIXES`).

### Precondición

Busca con ToolSearch herramientas MCP del gestor (`jira`, `atlassian`, `linear`…). **Si no hay**, no intentes acceder por otros medios (ni `curl` con credenciales): pide al usuario que pegue el título y la descripción del ticket y continúa con ese texto como requerimiento.

### Lectura (si el requerimiento viene de un ticket)

Obtén el ticket real **antes** de clasificar — nunca clasifiques a partir de la sola clave. Con Jira:

```
<mcp jira get>
  path: /rest/api/3/issue/{ISSUE_KEY}
  jq: "{summary: fields.summary, description: fields.description, issuetype: fields.issuetype.name, labels: fields.labels, epicKey: fields.parent.key}"
```

Filtra siempre los campos (traer el issue completo es caro). `summary` y `description` son el **requerimiento crudo** que alimenta la clasificación de `tech-story-architect` — no los reformules antes de clasificar.

Si el ticket menciona un archivo, patrón o síntoma que calza con una fila de `01-governance.md §6` (p. ej. `contentDescription = null`, `LocalDate.now()`, archivo grande, test faltante), reutiliza ese `TD-KEN-XXX` y sigue con `tech-story-architect:liquidar-deuda`. Si trae un link de Figma, cítalo en el campo **Diseño**.

La lectura **no** implica escribir de vuelta.

### Escritura (solo con confirmación explícita del usuario en el turno actual)

Ambas acciones son visibles para el equipo: nunca se ejecutan solo porque se redactó la historia.

1. **Crear el ticket** en el proyecto configurado (`POST /rest/api/3/issue` en Jira).
2. **Rellenar la descripción del ticket de origen** (`PUT /rest/api/3/issue/{ISSUE_KEY}` con solo `fields.description`). Jira espera **ADF**, no Markdown crudo; esto **reemplaza** la descripción actual. Si la escritura falla, reporta el error tal cual y ofrece agregar la historia como comentario (`POST …/comment`).
