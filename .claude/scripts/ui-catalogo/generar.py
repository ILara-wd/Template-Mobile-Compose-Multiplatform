#!/usr/bin/env python3
"""Genera docs/ui-catalogo/index.html de Kensho: pantallas construidas, componentes disponibles y tokens.

Todo sale del código, así que el catálogo no se desactualiza a mano:
- Componentes: funciones @Composable con nombre en mayúscula del módulo :app (dev.treecode.kensho).
  Capa según el paquete: components/atoms → Átomo, components/molecules → Molécula, ui/ y la raíz → App,
  el resto (feature/*, students/, payments/…) → Feature, o Local si es private/internal. Descripción = su KDoc.
  Usos = llamadas en el código. Se omiten ui/theme (tokens), las previews y las pantallas.
- Pantallas: docs/ui-catalogo/pantallas.json (id, nombre, composable, nodo de Figma, captura) y las imágenes de
  docs/ui-catalogo/img/ que genera capturar.py.
- Tokens: TkdColors, TkdTypography, TkdDimens y el color de cada cinta en TkdBeltPalette.

Uso: python3 .claude/scripts/ui-catalogo/generar.py
"""
import datetime as dt
import json
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
CATALOGO = ROOT / "docs" / "ui-catalogo"
BASE = ROOT / "app/src/main/java/dev/treecode/kensho"
THEME = BASE / "ui/theme"

DOC = r"/\*\*(?:[^*]|\*(?!/))*\*/"
FUN = re.compile(
    rf"(?P<doc>{DOC}\s*)?(?P<annots>(?:@[\w.]+(?:\([^()]*(?:\([^()]*\)[^()]*)*\))?\s*)+)"
    r"(?P<mods>(?:(?:private|internal|public|inline)\s+)*)fun\s+(?:<[^>]*>\s*)?(?P<recv>[\w.<>?, ]+\.)?(?P<name>[A-Z]\w*)\s*\(",
)
PREVIEW = re.compile(r"@(\w+\.)*(\w*Previews?)\b")


def clean_doc(doc: str | None) -> str:
    if not doc:
        return ""
    body = re.sub(r"^/\*\*|\*/$", "", doc.strip())
    lines = [re.sub(r"^\s*\*\s?", "", ln) for ln in body.splitlines()]
    text = "\n".join(lines).strip()
    para = re.split(r"\n\s*\n|\n\s*@", text)[0]
    para = re.sub(r"\s*\n\s*", " ", para)
    return re.sub(r"\[([^\]]+)\]", r"\1", para).strip()


def params_of(src: str, start: int) -> str:
    depth, i = 1, start
    while i < len(src) and depth:
        depth += {"(": 1, ")": -1}.get(src[i], 0)
        i += 1
    return re.sub(r"\s+", " ", src[start:i - 1]).strip().rstrip(",")


def module_of(path: Path) -> str:
    """Paquete relativo a dev.treecode.kensho: components.atoms, feature.dojo, students, ui…"""
    rel = path.relative_to(BASE).parent.parts
    if not rel:
        return "app"
    if rel[0] == "components" and len(rel) > 1:
        return f"components.{rel[1]}"
    if rel[0] == "feature" and len(rel) > 1:
        return ".".join(rel[:3])
    return rel[0]


def tier_of(module: str, local: bool) -> str:
    if module == "components.atoms":
        return "Átomo"
    if module.startswith("components."):
        return "Molécula"
    if module in ("app", "ui", "nav"):
        return "App"
    return "Local" if local else "Feature"


def scan() -> tuple[list[dict], dict[str, str]]:
    comps, texts = [], {}
    for path in sorted(BASE.rglob("*.kt")):
        if THEME in path.parents:
            continue
        src = path.read_text(encoding="utf-8")
        rel = str(path.relative_to(ROOT))
        texts[rel] = src
        module = module_of(path)
        for m in FUN.finditer(src):
            annots = m.group("annots")
            name = m.group("name")
            if "@Composable" not in annots or PREVIEW.search(annots) or name.endswith("Preview"):
                continue
            local = "private" in m.group("mods") or "internal" in m.group("mods")
            tier = tier_of(module, local)
            # Los privados de components/ y ui/ son detalles de implementación, no piezas reutilizables.
            if local and tier in ("Átomo", "Molécula", "App"):
                continue
            comps.append({
                "name": name, "module": module, "file": rel,
                "line": src.count("\n", 0, m.start("name")) + 1,
                "doc": clean_doc(m.group("doc")), "params": params_of(src, m.end()),
                "receiver": (m.group("recv") or "").rstrip("."), "local": local, "tier": tier,
            })
    return comps, texts


def git(*args: str) -> str:
    try:
        return subprocess.run(["git", *args], cwd=ROOT, check=True, capture_output=True, text=True).stdout.strip()
    except (OSError, subprocess.CalledProcessError):
        return ""


def argb(value: str) -> str:
    """0xAARRGGBB → #RRGGBB o #RRGGBBAA (CSS) si tiene transparencia."""
    a, rgb = value[:2].upper(), value[2:].upper()
    return f"#{rgb}" if a == "FF" else f"#{rgb}{a}"


def tokens() -> dict:
    colors = (THEME / "TkdColors.kt").read_text(encoding="utf-8")
    typo = (THEME / "TkdTypography.kt").read_text(encoding="utf-8")
    dimens = (THEME / "TkdDimens.kt").read_text(encoding="utf-8")
    belts = (THEME / "TkdBeltPalette.kt").read_text(encoding="utf-8")
    color_rows = []
    for name, value, note in re.findall(r"val (\w+) = Color\(0x([0-9A-Fa-f]{8})\)(?:[ \t]*//[ \t]*([^\n]*))?", colors):
        color_rows.append([name, argb(value), note.strip()])
    type_rows = [
        [n, w, int(s), float(lh), float(tr or 0)]
        for n, w, s, lh, tr in re.findall(
            r"val (\w+) = style\(FontWeight\.(\w+), (\d+), ([\d.]+)f(?:, tracking = (-?[\d.]+)f)?\)", typo
        )
    ]
    dimen_rows = [[n, float(v)] for n, v in re.findall(r"val (\w+) = \(?(-?[\d.]+)\)?\.dp", dimens)]
    belt_rows = [[b, argb(v)] for b, v in re.findall(r"BeltColor\.(\w+) -> TkdBeltPalette\(\s*dot = Color\(0x([0-9A-Fa-f]{8})\)", belts)]
    return {
        "colors": color_rows,
        "type": type_rows,
        "radius": [r for r in dimen_rows if "Radius" in r[0]],
        "spacing": [r for r in dimen_rows if re.fullmatch(r"Space\d+", r[0])],
        "dimens": [r for r in dimen_rows if "Radius" not in r[0] and not re.fullmatch(r"Space\d+", r[0])],
        "belts": belt_rows,
    }


def main() -> None:
    manifest = json.loads((CATALOGO / "pantallas.json").read_text(encoding="utf-8"))
    comps, texts = scan()
    by_name = {c["name"]: c for c in comps}
    screen_names = {p["composable"] for p in manifest["pantallas"]}
    files = manifest["figmaArchivos"]

    screens = []
    for p in manifest["pantallas"]:
        c = by_name.get(p["composable"])
        imgs = sorted(f.name for f in (CATALOGO / "img").glob(f"{p['id']}-*.jpg")) if (CATALOGO / "img").is_dir() else []
        figma = p.get("figma") or {}
        archivo = files.get(figma.get("archivo", ""), {})
        screens.append({
            "id": p["id"], "nombre": p["nombre"], "composable": p["composable"], "nota": p.get("nota", ""),
            "figmaNodo": figma.get("nodo", ""), "figmaArchivo": archivo.get("nombre", ""), "figmaKey": archivo.get("key") or "",
            "module": c["module"] if c else "", "file": c["file"] if c else "", "line": c["line"] if c else 0, "imgs": imgs,
        })
    screen_by_file: dict[str, list[str]] = {}
    for s in screens:
        screen_by_file.setdefault(s["file"], []).append(s["id"])

    # Un local con el mismo nombre que un público "tapa" al público en su archivo.
    locals_by_file: dict[str, set[str]] = {}
    for c in comps:
        if c["local"]:
            locals_by_file.setdefault(c["file"], set()).add(c["name"])

    out = []
    for c in comps:
        if c["name"] in screen_names or (c["name"].endswith("Screen") and not c["local"]):
            continue
        pattern = re.compile(rf"(?<![\w]){re.escape(c['name'])}\s*[({{]")
        # Un local solo se usa en su paquete; un público, en cualquier archivo donde no lo tape un local.
        package = str(Path(c["file"]).parent)
        candidates = [f for f in texts if str(Path(f).parent) == package] if c["local"] else [
            f for f in texts if c["name"] not in locals_by_file.get(f, set())
        ]
        uses: dict[str, int] = {}
        for rel in candidates:
            src = texts[rel]
            n = sum(1 for m in pattern.finditer(src) if not src[max(0, m.start() - 4):m.start()].endswith("fun "))
            if n:
                uses[rel] = n
        used_screens = sorted({sid for rel in uses for sid in screen_by_file.get(rel, [])})
        # Las secciones de una pantalla viven en otros archivos del mismo paquete: cuentan para esa pantalla.
        for rel in uses:
            for s in screens:
                if s["file"] and str(Path(s["file"]).parent) == str(Path(rel).parent) and s["id"] not in used_screens:
                    used_screens.append(s["id"])
        modules = sorted({module_of(ROOT / rel) for rel in uses})
        out.append({
            "key": f'{c["name"]}@{c["module"]}.{Path(c["file"]).stem}' if c["local"] else c["name"],
            "name": c["name"], "tier": c["tier"], "module": c["module"], "file": c["file"], "line": c["line"],
            "doc": c["doc"], "params": c["params"], "receiver": c["receiver"],
            "uses": sum(uses.values()), "screens": sorted(used_screens), "usedIn": modules,
        })
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
    order = {"Átomo": 0, "Molécula": 1, "App": 2, "Feature": 3, "Local": 4}
    out.sort(key=lambda c: (order[c["tier"]], c["module"], c["name"]))

    for s in screens:
        s["components"] = [c["key"] for c in out if s["id"] in c["screens"] and c["tier"] != "Local"]
        s["locals"] = [c["key"] for c in out if s["id"] in c["screens"] and c["tier"] == "Local"]

    data = {
        "generated": dt.datetime.now().strftime("%Y-%m-%d %H:%M"),
        "commit": git("log", "-1", "--format=%h"),
        "branch": git("branch", "--show-current"),
        "dirty": bool(git("status", "--porcelain", "--", "app/src/main")),
        "screens": screens,
        "components": out,
        "tokens": tokens(),
    }
    template = (Path(__file__).parent / "plantilla.html").read_text(encoding="utf-8")
    payload = json.dumps(data, ensure_ascii=False).replace("</", "<\\/")
    (CATALOGO / "index.html").write_text(template.replace("/*__DATA__*/null", payload), encoding="utf-8")
    counts = {t: sum(1 for c in out if c["tier"] == t) for t in order}
    sin_doc = [c["name"] for c in out if not c["doc"] and c["tier"] != "Local"]
    print(f"index.html: {len(screens)} pantallas, {len(out)} componentes {counts}")
    if sin_doc:
        print(f"Sin KDoc ({len(sin_doc)}): {', '.join(sin_doc)}")


if __name__ == "__main__":
    main()
