#!/usr/bin/env bash
# Regenera AGENTS.md como espejo de CLAUDE.md (Copilot CLI y otros agentes leen AGENTS.md).
# Uso: scripts/sync-agents.sh   (desde cualquier directorio del repo)
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"
{
  cat <<'EOF'
<!-- AUTO-GENERADO por scripts/sync-agents.sh desde CLAUDE.md.
     NO EDITAR A MANO. -->

# AGENTS.md

> Espejo de `CLAUDE.md`. Leído por Copilot CLI y otros agentes que siguen la
> convención AGENTS.md. Toda edición debe hacerse en `CLAUDE.md`.

---

EOF
  cat CLAUDE.md
} > AGENTS.md
echo "✅ AGENTS.md regenerado desde CLAUDE.md"
