#!/usr/bin/env python3
"""Genera docs/ui-catalogo/index.html: pantallas construidas, componentes disponibles y tokens.

Todo sale del código, así que el catálogo no se desactualiza a mano:
- Componentes: funciones @Composable con nombre en mayúscula de core:designsystem, core:ui, app y
  features (las privadas de un feature son "locales"). Descripción = su KDoc. Código del README
  (M9, O21…) = la fila del README que nombra al componente. Usos = llamadas en el código.
- Pantallas: docs/ui-catalogo/pantallas.json (id, nombre, composable, nodo de Figma, captura) y las
  imágenes de docs/ui-catalogo/img/ que genera capturar.py.
- Tokens: Color.kt, SportColors.kt, Type.kt, Spacing.kt y Shape.kt.

Uso: python3 scripts/ui-catalogo/generar.py
"""
import datetime as dt
import json
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CATALOGO = ROOT / "docs" / "ui-catalogo"
THEME = ROOT / "core/designsystem/src/main/kotlin/com/invex/sportkid/core/designsystem/theme"

SOURCES = [
    ("core:designsystem", ROOT / "core/designsystem/src/main/kotlin"),
    ("core:ui", ROOT / "core/ui/src/main/kotlin"),
    ("app", ROOT / "app/src/main/java"),
] + [(f"feature:{d.name}", d / "src/main/kotlin") for d in sorted((ROOT / "feature").iterdir()) if (d / "src/main/kotlin").is_dir()]

EXCLUDED = {"SportKidTheme"}
DOC = r"/\*\*(?:[^*]|\*(?!/))*\*/"
FUN = re.compile(
    rf"(?P<doc>{DOC}\s*)?(?P<annots>(?:@[\w.]+(?:\([^()]*(?:\([^()]*\)[^()]*)*\))?\s*)+)"
    r"(?P<mods>(?:(?:private|internal|public|inline)\s+)*)fun\s+(?:<[^>]*>\s*)?(?P<recv>[\w.<>?, ]+\.)?(?P<name>[A-Z]\w*)\s*\(",
)


def clean_doc(doc: str | None) -> str:
    if not doc:
        return ""
    body = re.sub(r"^/\*\*|\*/$", "", doc.strip())
    lines = [re.sub(r"^\s*\*\s?", "", ln) for ln in body.splitlines()]
    text = "\n".join(lines).strip()
    para = re.split(r"\n\s*\n", text)[0]
    para = re.sub(r"\s*\n\s*", " ", para)
    return re.sub(r"\[([^\]]+)\]", r"\1", para).strip()


def params_of(src: str, start: int) -> str:
    depth, i = 1, start
    while i < len(src) and depth:
        depth += {"(": 1, ")": -1}.get(src[i], 0)
        i += 1
    raw = src[start:i - 1]
    return re.sub(r"\s+", " ", raw).strip().rstrip(",")


def scan() -> tuple[list[dict], dict[str, str]]:
    comps, texts = [], {}
    for module, base in SOURCES:
        for path in sorted(base.rglob("*.kt")):
            src = path.read_text(encoding="utf-8")
            rel = str(path.relative_to(ROOT))
            texts[rel] = src
            for m in FUN.finditer(src):
                annots = m.group("annots")
                if "@Composable" not in annots or re.search(r"@(\w+\.)*(Sk)?Previews?\b", annots):
                    continue
                name = m.group("name")
                if name.endswith("Preview"):
                    continue
                mods = m.group("mods")
                local = "private" in mods or "internal" in mods
                # Los privados de core y app son detalles de implementación; el tema no es un componente.
                if (local and not module.startswith("feature:")) or name in EXCLUDED:
                    continue
                comps.append({
                    "name": name,
                    "module": module,
                    "file": rel,
                    "line": src.count("\n", 0, m.start("name")) + 1,
                    "doc": clean_doc(m.group("doc")),
                    "params": params_of(src, m.end()),
                    "receiver": (m.group("recv") or "").rstrip("."),
                    "local": local,
                })
    return comps, texts


def tier(c: dict) -> str:
    if c["module"] == "core:designsystem":
        return "Átomo"
    if c["module"] == "core:ui":
        return "Dominio"
    if c["module"] == "app":
        return "App"
    return "Local" if c["local"] else "Feature"


def readme_codes() -> dict[str, str]:
    codes: dict[str, str] = {}
    readme = (ROOT / "README.md").read_text(encoding="utf-8")
    for line in readme.splitlines():
        cells = [c.strip() for c in line.strip().strip("|").split("|")]
        if len(cells) < 3:
            continue
        code = cells[0]
        if re.fullmatch(r"[MOP]\d+", code):
            for name in re.findall(r"`([A-Z]\w+)`", cells[2]):
                codes.setdefault(name, code)
        elif re.findall(r"`(Sk\w+)`", cells[1]):
            for name in re.findall(r"`(Sk\w+)`", cells[1]):
                codes.setdefault(name, "§1.6")
    return codes


def git(*args: str) -> str:
    try:
        return subprocess.run(["git", *args], cwd=ROOT, check=True, capture_output=True, text=True).stdout.strip()
    except (OSError, subprocess.CalledProcessError):
        return ""


def colors_in(block: str) -> list[list[str]]:
    return [[n, "#" + v[2:].upper()] for n, v in re.findall(r"(\w+)\s*[:=]\s*(?:Color\s*=\s*)?Color\(0x([0-9A-Fa-f]{8})\)", block)]


def block(src: str, header: str) -> str:
    m = re.search(rf"{header}[^\n]*\(\s*\n(.*?)\n\)", src, re.S)
    return m.group(1) if m else ""


def tokens() -> dict:
    color = (THEME / "Color.kt").read_text(encoding="utf-8")
    sport = (THEME / "SportColors.kt").read_text(encoding="utf-8")
    typo = (THEME / "Type.kt").read_text(encoding="utf-8")
    spacing = (THEME / "Spacing.kt").read_text(encoding="utf-8")
    shape = (THEME / "Shape.kt").read_text(encoding="utf-8")
    light, dark = colors_in(block(color, "val OceanLightScheme")), dict(colors_in(block(color, "val OceanDarkScheme")))
    sport_light, sport_dark = colors_in(block(sport, "val LightSportColors")), dict(colors_in(block(sport, "val DarkSportColors")))
    return {
        "scheme": [[n, v, dark.get(n, "")] for n, v in light],
        "sport": [[n, v, sport_dark.get(n, "")] for n, v in sport_light],
        "athlete": colors_in(block(sport, "data class AthleteColors")),
        "belt": colors_in(block(sport, "data class BeltColors")),
        "type": [[n, int(s), w, int(lh)] for n, s, w, lh in re.findall(r"(\w+) = style\((\d+), FontWeight\.(\w+), (\d+)", typo)],
        "spacing": [[n, int(v)] for n, v in re.findall(r"val (\w+): Dp = (\d+)\.dp", spacing)],
        "shape": [[n, int(v)] for n, v in re.findall(r"(\w+) = RoundedCornerShape\((\d+)\.dp\)", shape)],
    }


def main() -> None:
    manifest = json.loads((CATALOGO / "pantallas.json").read_text(encoding="utf-8"))
    comps, texts = scan()
    codes = readme_codes()
    by_name = {c["name"]: c for c in comps}
    screen_names = {p["composable"] for p in manifest["pantallas"]}

    # Pantallas: archivo donde vive el composable de la pantalla y capturas existentes.
    screens = []
    for p in manifest["pantallas"]:
        c = by_name.get(p["composable"])
        imgs = sorted(f.name for f in (CATALOGO / "img").glob(f"{p['id']}-*.jpg")) if (CATALOGO / "img").is_dir() else []
        screens.append({
            "id": p["id"], "nombre": p["nombre"], "composable": p["composable"], "figma": p.get("figma", ""),
            "module": c["module"] if c else "", "file": c["file"] if c else "", "line": c["line"] if c else 0, "imgs": imgs,
        })
    screen_by_file: dict[str, list[str]] = {}
    for s in screens:
        screen_by_file.setdefault(s["file"], []).append(s["id"])

    # Un local con el mismo nombre que un público "tapa" al público en su archivo (p. ej. MedalBadge).
    locals_by_file: dict[str, set[str]] = {}
    for c in comps:
        if c["local"]:
            locals_by_file.setdefault(c["file"], set()).add(c["name"])

    # Componentes (sin las pantallas) con usos por archivo.
    out = []
    for c in comps:
        if c["name"] in screen_names or c["name"].endswith("Screen") and not c["local"]:
            continue
        pattern = re.compile(rf"(?<![\w]){re.escape(c['name'])}\s*[({{]")
        files = [c["file"]] if c["local"] else [f for f in texts if c["name"] not in locals_by_file.get(f, set())]
        uses: dict[str, int] = {}
        for rel in files:
            src = texts[rel]
            n = sum(1 for m in pattern.finditer(src) if not src[max(0, m.start() - 4):m.start()].endswith("fun "))
            if n:
                uses[rel] = n
        used_screens = sorted({sid for rel in uses for sid in screen_by_file.get(rel, [])})
        modules = sorted({next((mod for mod, base in SOURCES if (ROOT / rel).is_relative_to(base)), "") for rel in uses})
        out.append({
            "key": f'{c["name"]}@{c["module"].split(":")[-1]}.{Path(c["file"]).stem}' if c["local"] else c["name"],
            "name": c["name"], "tier": tier(c), "module": c["module"], "file": c["file"], "line": c["line"],
            "doc": c["doc"], "params": c["params"], "receiver": c["receiver"], "code": "" if c["local"] else codes.get(c["name"], ""),
            "uses": sum(uses.values()), "screens": used_screens, "usedIn": modules,
        })
    # Sobrecargas (p. ej. SkInfoCallout con String y con AnnotatedString): una ficha con todas las firmas.
    merged: dict[str, dict] = {}
    for c in out:
        first = merged.get(c["key"])
        if first is None:
            c["overloads"] = [c["params"]]
            merged[c["key"]] = c
        else:
            first["overloads"].append(c["params"])
            first["doc"] = first["doc"] or c["doc"]
    out = list(merged.values())
    order = {"Átomo": 0, "Dominio": 1, "App": 2, "Feature": 3, "Local": 4}
    out.sort(key=lambda c: (order[c["tier"]], c["module"], c["name"]))

    # Componentes por pantalla (lo que se llama dentro del archivo de la pantalla).
    for s in screens:
        s["components"] = [c["key"] for c in out if s["id"] in c["screens"] and c["tier"] != "Local"]
        s["locals"] = [c["key"] for c in out if s["id"] in c["screens"] and c["tier"] == "Local"]

    data = {
        "generated": dt.datetime.now().strftime("%Y-%m-%d %H:%M"),
        "commit": git("log", "-1", "--format=%h"),
        "branch": git("branch", "--show-current"),
        "dirty": bool(git("status", "--porcelain", "--", "core", "feature", "app")),
        "figmaFileKey": manifest["figmaFileKey"],
        "screens": screens,
        "components": out,
        "tokens": tokens(),
    }
    template = (Path(__file__).parent / "plantilla.html").read_text(encoding="utf-8")
    payload = json.dumps(data, ensure_ascii=False).replace("</", "<\\/")
    (CATALOGO / "index.html").write_text(template.replace("/*__DATA__*/null", payload), encoding="utf-8")
    counts = {t: sum(1 for c in out if c["tier"] == t) for t in order}
    print(f"index.html: {len(screens)} pantallas, {len(out)} componentes {counts}")


if __name__ == "__main__":
    main()
