#!/usr/bin/env python3
"""
sync_claude_to_copilot.py

Traduce el contenido de `.claude/` al formato que consume GitHub Copilot en
`.github/`, siguiendo el esquema oficial de "custom instructions", "prompt files"
y "agents" (https://docs.github.com/en/copilot/how-tos/configure-custom-instructions).

Mapeo:
  .claude/agents/**/*.md    -> .github/agents/<relpath>.agent.md
  .claude/commands/**/*.md  -> .github/prompts/<relpath>.prompt.md

Las subcarpetas de `.claude/commands/` (sub-comandos namespaced tipo
`/parent:child` en Claude Code) se preservan como subdirectorios paralelos
bajo `.github/prompts/`, ya que Copilot indexa prompts recursivamente.

Además regenera (si no existe editado a mano) un `.github/copilot-instructions.md`
que embebe `CLAUDE.md` (fuente de verdad junto con `README.md`) y el `AGENTS.md`
de la raíz.

Idempotente: solo reescribe destinos si el contenido cambió.

Uso:
  python3 .github/scripts/sync_claude_to_copilot.py                       # todo
  python3 .github/scripts/sync_claude_to_copilot.py --skip-instructions   # solo agents/prompts
    (no regenera .github/copilot-instructions.md ni AGENTS.md desde CLAUDE.md)
"""

from __future__ import annotations

import re
import sys
from pathlib import Path
from typing import Any

REPO_ROOT = Path(__file__).resolve().parents[2]
CLAUDE_DIR = REPO_ROOT / ".claude"
GITHUB_DIR = REPO_ROOT / ".github"
AGENTS_DIR = GITHUB_DIR / "agents"
PROMPTS_DIR = GITHUB_DIR / "prompts"
INSTRUCTIONS_FILE = GITHUB_DIR / "copilot-instructions.md"
CLAUDE_MD = REPO_ROOT / "CLAUDE.md"
AGENTS_MD = REPO_ROOT / "AGENTS.md"

GENERATED_BANNER = (
    "<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py "
    "desde {source}. NO EDITAR A MANO. -->\n"
)

FRONTMATTER_RE = re.compile(r"^---\s*\n(.*?)\n---\s*\n?", re.DOTALL)


def parse_frontmatter(text: str) -> tuple[dict[str, Any], str]:
    """Parser YAML mínimo suficiente para el frontmatter de Claude."""
    m = FRONTMATTER_RE.match(text)
    if not m:
        return {}, text
    body = text[m.end():]
    fm: dict[str, Any] = {}
    current_key: str | None = None
    buffer: list[str] = []

    def flush():
        nonlocal current_key, buffer
        if current_key is not None:
            fm[current_key] = "\n".join(buffer).strip()
        current_key, buffer = None, []

    for raw in m.group(1).splitlines():
        if not raw.strip():
            continue
        if re.match(r"^[A-Za-z_][\w-]*\s*:", raw):
            flush()
            key, _, value = raw.partition(":")
            current_key = key.strip()
            value = value.strip()
            if value in ("", ">-", ">", "|"):
                buffer = []
            else:
                buffer = [value]
        else:
            buffer.append(raw.strip())
    flush()

    for k, v in list(fm.items()):
        if isinstance(v, str) and v.startswith("[") and v.endswith("]"):
            fm[k] = [x.strip() for x in v[1:-1].split(",") if x.strip()]
    return fm, body


def dump_frontmatter(fm: dict[str, Any]) -> str:
    lines = ["---"]
    for k, v in fm.items():
        if isinstance(v, list):
            lines.append(f"{k}: [{', '.join(str(x) for x in v)}]")
        elif isinstance(v, str) and ("\n" in v or len(v) > 90):
            lines.append(f"{k}: >-")
            for ln in v.splitlines():
                lines.append(f"  {ln.strip()}")
        else:
            lines.append(f"{k}: {v}")
    lines.append("---\n")
    return "\n".join(lines)


CLAUDE_TO_COPILOT_TOOLS = {
    "Read": "codebase",
    "Write": "editFiles",
    "Edit": "editFiles",
    "Grep": "search",
    "Glob": "search",
    "Bash": "runCommands",
    "TodoWrite": "todos",
    "Skill": "extensions",
}


def translate_tools(claude_tools: Any) -> list[str]:
    if not claude_tools:
        return []
    if isinstance(claude_tools, str):
        items = [t.strip() for t in claude_tools.split(",") if t.strip()]
    else:
        items = list(claude_tools)
    out: list[str] = []
    for t in items:
        mapped = CLAUDE_TO_COPILOT_TOOLS.get(t, t.lower())
        if mapped not in out:
            out.append(mapped)
    return out


def write_if_changed(path: Path, content: str) -> bool:
    path.parent.mkdir(parents=True, exist_ok=True)
    if path.exists() and path.read_text(encoding="utf-8") == content:
        return False
    path.write_text(content, encoding="utf-8")
    return True


def _dst_path(src: Path, base_src_dir: Path, base_dst_dir: Path, suffix: str) -> Path:
    """Calcula el destino preservando la ruta relativa a la carpeta base.

    Ejemplos:
      .claude/commands/validate-pullrequest.md
        -> .github/prompts/validate-pullrequest.prompt.md
      .claude/commands/validate-pullrequest/ff-a11y.md
        -> .github/prompts/validate-pullrequest/ff-a11y.prompt.md
    """
    rel = src.relative_to(base_src_dir)
    return base_dst_dir / rel.with_name(f"{rel.stem}{suffix}")


def convert_agent(src: Path, base_src_dir: Path) -> tuple[Path, str]:
    fm, body = parse_frontmatter(src.read_text(encoding="utf-8"))
    name = fm.get("name") or src.stem
    description = fm.get("description", "").strip()
    model = fm.get("model", "").strip()
    tools = translate_tools(fm.get("tools"))

    new_fm: dict[str, Any] = {"description": description or f"Agent {name}"}
    if tools:
        new_fm["tools"] = tools
    if model:
        new_fm["model"] = model

    dst = _dst_path(src, base_src_dir, AGENTS_DIR, ".agent.md")
    banner = GENERATED_BANNER.format(source=src.relative_to(REPO_ROOT))
    content = banner + dump_frontmatter(new_fm) + "\n" + body.lstrip()
    return dst, content


def convert_command(src: Path, base_src_dir: Path) -> tuple[Path, str]:
    fm, body = parse_frontmatter(src.read_text(encoding="utf-8"))
    name = fm.get("name") or src.stem
    description = (fm.get("description") or "").strip()
    if not description:
        for line in body.splitlines():
            s = line.strip().lstrip("#").strip()
            if s:
                description = s[:180]
                break

    new_fm: dict[str, Any] = {
        "mode": "agent",
        "description": description or f"Prompt {name}",
    }
    tools = translate_tools(fm.get("tools"))
    if tools:
        new_fm["tools"] = tools

    dst = _dst_path(src, base_src_dir, PROMPTS_DIR, ".prompt.md")
    banner = GENERATED_BANNER.format(source=src.relative_to(REPO_ROOT))
    content = banner + dump_frontmatter(new_fm) + "\n" + body.lstrip()
    return dst, content


COPILOT_HEADER = """<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde CLAUDE.md.
     NO EDITAR A MANO — todo cambio debe hacerse en CLAUDE.md y luego
     ejecutar `python3 .github/scripts/sync_claude_to_copilot.py`. -->

# GitHub Copilot — instrucciones del repositorio

> Este archivo es el espejo íntegro de `CLAUDE.md` para que GitHub Copilot
> (Copilot Chat en Android Studio/VS Code, Copilot CLI y Copilot Code Review
> en Pull Requests) reciba el mismo contexto arquitectónico que Claude Code.
> Los **prompts** (`/validate-pullrequest`, `/sportkid-governance`,
> `/tech-story-architect`) están en `.github/prompts/` y el **agente**
> `sportkid-android-dev` en `.github/agents/`, también generados desde
> `.claude/`. Los sub-comandos namespaced (ej.
> `.claude/commands/validate-pullrequest/ff-a11y.md`) se replican como
> subdirectorios paralelos bajo `.github/prompts/`. El sistema de diseño
> (tokens, componentes `Sk*`, pantallas P01–P21) está en `README.md`.

---

"""


def ensure_copilot_instructions() -> bool:
    """Regenera .github/copilot-instructions.md embebiendo CLAUDE.md completo."""
    if not CLAUDE_MD.exists():
        return False
    body = CLAUDE_MD.read_text(encoding="utf-8")
    content = COPILOT_HEADER + body
    return write_if_changed(INSTRUCTIONS_FILE, content)


AGENTS_HEADER = """<!-- AUTO-GENERADO por .github/scripts/sync_claude_to_copilot.py desde CLAUDE.md.
     NO EDITAR A MANO. -->

# AGENTS.md

> Espejo de `CLAUDE.md`. Leído por Copilot CLI y otros agentes que siguen la
> convención AGENTS.md. Toda edición debe hacerse en `CLAUDE.md`.

---

"""


def ensure_agents_md() -> bool:
    """Regenera AGENTS.md en la raíz para que Copilot CLI también lo lea."""
    if not CLAUDE_MD.exists():
        return False
    content = AGENTS_HEADER + CLAUDE_MD.read_text(encoding="utf-8")
    return write_if_changed(AGENTS_MD, content)


def clean_stale(dst_dir: Path, valid_rel_paths: set[str], suffix: str) -> list[Path]:
    """Elimina destinos huérfanos (recursivo) y luego los directorios vacíos.

    `valid_rel_paths` contiene las rutas relativas a `dst_dir` (posix) de los
    archivos que sí deben existir; cualquier `*{suffix}` fuera de ese set se
    elimina.
    """
    removed: list[Path] = []
    if not dst_dir.exists():
        return removed
    for f in dst_dir.rglob(f"*{suffix}"):
        rel = f.relative_to(dst_dir).as_posix()
        if rel not in valid_rel_paths:
            f.unlink()
            removed.append(f)
    for d in sorted(
        (p for p in dst_dir.rglob("*") if p.is_dir()),
        key=lambda p: len(p.parts),
        reverse=True,
    ):
        try:
            d.rmdir()
        except OSError:
            pass
    return removed


def main() -> int:
    skip_instructions = "--skip-instructions" in sys.argv[1:]
    if not CLAUDE_DIR.exists():
        print(f"[sync] .claude/ no existe en {REPO_ROOT} — nada que hacer.")
        return 0

    changed: list[Path] = []
    valid_agents: set[str] = set()
    valid_prompts: set[str] = set()

    agents_dir = CLAUDE_DIR / "agents"
    if agents_dir.exists():
        for src in sorted(agents_dir.rglob("*.md")):
            dst, content = convert_agent(src, agents_dir)
            valid_agents.add(dst.relative_to(AGENTS_DIR).as_posix())
            if write_if_changed(dst, content):
                changed.append(dst)

    commands_dir = CLAUDE_DIR / "commands"
    if commands_dir.exists():
        for src in sorted(commands_dir.rglob("*.md")):
            dst, content = convert_command(src, commands_dir)
            valid_prompts.add(dst.relative_to(PROMPTS_DIR).as_posix())
            if write_if_changed(dst, content):
                changed.append(dst)

    if not skip_instructions:
        if ensure_copilot_instructions():
            changed.append(INSTRUCTIONS_FILE)
        if ensure_agents_md():
            changed.append(AGENTS_MD)

    removed = clean_stale(AGENTS_DIR, valid_agents, ".agent.md")
    removed += clean_stale(PROMPTS_DIR, valid_prompts, ".prompt.md")

    if changed or removed:
        print(f"[sync] {len(changed)} archivo(s) actualizado(s), "
              f"{len(removed)} eliminado(s):")
        for p in changed:
            print(f"  ~ {p.relative_to(REPO_ROOT)}")
        for p in removed:
            print(f"  - {p.relative_to(REPO_ROOT)}")
    else:
        print("[sync] .github/ ya está sincronizado con .claude/.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
