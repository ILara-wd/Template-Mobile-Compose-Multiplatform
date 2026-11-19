#!/usr/bin/env python3
"""Genera docs/ui-catalogo/index.html: pantallas construidas, componentes disponibles y tokens.

Todo sale del código, así que el catálogo no se desactualiza a mano:
- Componentes: funciones @Composable con nombre en mayúscula de core:designsystem, core:ui, app y
  features (las privadas de un feature son "locales"). Descripción = su KDoc. Código del README
  (M9, O21…) = la fila del README que nombra al componente. Usos = llamadas en el código.
- Pantallas: docs/ui-catalogo/pantallas.json (id, nombre, composable, nodo de Figma, captura) y las
  imágenes de docs/ui-catalogo/img/ que genera capturar.py.
- Assets: res/drawable* de cada módulo; los <vector> se convierten a SVG y las imágenes se
  enlazan por ruta relativa (el catálogo se abre desde el repo). Usos = R.drawable.x y @drawable/x.
- Tokens: Color.kt, SportColors.kt, Type.kt, Spacing.kt y Shape.kt.

Uso: python3 scripts/ui-catalogo/generar.py
"""
import datetime as dt
import json
import re
import subprocess
import xml.etree.ElementTree as ET
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


ANDROID = "{http://schemas.android.com/apk/res/android}"
AAPT = "{http://schemas.android.com/aapt}"


def res_dirs() -> list[tuple[str, Path]]:
    """Carpetas drawable* de cada módulo, con el nombre del módulo (core:ui, feature:tkd…)."""
    out = []
    for module, _ in SOURCES:
        kind, _, name = module.partition(":")
        base = ROOT / (f"{kind}/{name}" if name else kind) / "src/main/res"
        out += [(module, d) for d in sorted(base.glob("drawable*")) if d.is_dir()]
    return out


def android_color(value: str | None) -> tuple[str, float] | None:
    """#RGB, #ARGB, #RRGGBB o #AARRGGBB → (#RRGGBB, alfa). Referencias (@color, ?attr) → gris."""
    if not value:
        return None
    if not value.startswith("#"):
        return "#888888", 1.0
    h = value[1:]
    if len(h) in (3, 4):
        h = "".join(c * 2 for c in h)
    alpha = int(h[:2], 16) / 255 if len(h) == 8 else 1.0
    return "#" + h[-6:].upper(), alpha


def vector_to_svg(path: Path) -> tuple[str, bool, str]:
    """Convierte un <vector> de Android a SVG. Devuelve (svg, monocromo, tamaño en dp).

    Si todos los trazos usan un solo color, se cambia por currentColor: así se pintan con el color
    del tema, como hace Icon(tint) en la app.
    """
    root = ET.parse(path).getroot()
    a = lambda el, k, d=None: el.get(ANDROID + k, d)
    vw, vh = a(root, "viewportWidth", "24"), a(root, "viewportHeight", "24")
    dp = lambda k: f'{float(a(root, k, "0").replace("dp", "")):.1f}'.removesuffix(".0")
    size = f'{dp("width")}×{dp("height")} dp'
    paths, defs, colors = [], [], set()

    def walk(el, transform=""):
        for child in el:
            if child.tag == "group":
                tx, ty = a(child, "translateX", "0"), a(child, "translateY", "0")
                sx, sy, rot = a(child, "scaleX", "1"), a(child, "scaleY", "1"), a(child, "rotation", "0")
                px, py = a(child, "pivotX", "0"), a(child, "pivotY", "0")
                t = (f"translate({tx} {ty}) translate({px} {py}) rotate({rot}) scale({sx} {sy}) translate(-{px} -{py})")
                walk(child, f"{transform} {t}".strip())
            elif child.tag == "path":
                attrs = [f'd="{a(child, "pathData", "")}"']
                for kind in ("fill", "stroke"):
                    grad = next((g for at in child.findall(AAPT + "attr") if at.get("name") == f"android:{kind}Color"
                                 for g in at.findall("gradient")), None)
                    if grad is not None:
                        gid = f"g{len(defs)}"
                        stops = "".join(
                            f'<stop offset="{a(s, "offset", "0")}" stop-color="{android_color(a(s, "color"))[0]}" '
                            f'stop-opacity="{android_color(a(s, "color"))[1]:.3g}"/>' for s in grad.findall("item"))
                        defs.append(f'<linearGradient id="{gid}" gradientUnits="userSpaceOnUse" x1="{a(grad, "startX", "0")}" '
                                    f'y1="{a(grad, "startY", "0")}" x2="{a(grad, "endX", "0")}" y2="{a(grad, "endY", "0")}">{stops}</linearGradient>')
                        attrs.append(f'{kind}="url(#{gid})"')
                        colors.add("gradient")
                        continue
                    col = android_color(a(child, f"{kind}Color"))
                    if col is None or col[1] == 0:
                        attrs.append(f'{kind}="none"')
                        continue
                    colors.add(col[0])
                    alpha = col[1] * float(a(child, f"{kind}Alpha", "1"))
                    attrs.append(f'{kind}="{col[0]}"' + (f' {kind}-opacity="{alpha:.3g}"' if alpha < 1 else ""))
                if a(child, "strokeColor"):
                    attrs.append(f'stroke-width="{a(child, "strokeWidth", "0")}"')
                    for k, svg in (("strokeLineCap", "stroke-linecap"), ("strokeLineJoin", "stroke-linejoin")):
                        if a(child, k):
                            attrs.append(f'{svg}="{a(child, k)}"')
                if a(child, "fillType") == "evenOdd":
                    attrs.append('fill-rule="evenodd"')
                if transform:
                    attrs.append(f'transform="{transform}"')
                paths.append(f'<path {" ".join(attrs)}/>')

    walk(root)
    mono = len(colors) == 1 and "gradient" not in colors
    body = "".join(paths)
    if mono:
        body = body.replace(f'"{next(iter(colors))}"', '"currentColor"')
    svg = (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {vw} {vh}">'
           f'{"<defs>" + "".join(defs) + "</defs>" if defs else ""}{body}</svg>')
    return svg, mono, size


def drawables(texts: dict[str, str], screen_by_file: dict[str, list[str]]) -> list[dict]:
    """Assets de res/drawable*: vectores convertidos a SVG y bitmaps referenciados por ruta relativa."""
    items = []
    for module, d in res_dirs():
        for f in sorted(d.iterdir()):
            if f.suffix == ".xml":
                try:
                    svg, mono, size = vector_to_svg(f)
                except ET.ParseError:
                    continue
                items.append({"name": f.stem, "module": module, "kind": "Vector", "svg": svg, "mono": mono, "size": size})
            elif f.suffix.lower() in (".jpg", ".jpeg", ".png", ".webp"):
                rel = Path("../..") / f.relative_to(ROOT)
                items.append({"name": f.stem, "module": module, "kind": "Imagen", "src": rel.as_posix(),
                              "size": f"{f.stat().st_size // 1024} KB · {d.name}"})
            else:
                continue
            items[-1]["file"] = str(f.relative_to(ROOT))

    # R.drawable.x se resuelve al del propio módulo; si no existe ahí, al de core (o al primero que haya).
    by_name: dict[str, list[dict]] = {}
    for it in items:
        by_name.setdefault(it["name"], []).append(it)
        it["uses"], it["files"] = 0, set()
    module_of = lambda rel: next((m for m, base in SOURCES if (ROOT / rel).is_relative_to(base)), "")
    ref = re.compile(r"R\.drawable\.(\w+)\b")
    for rel, src in texts.items():
        mod = module_of(rel)
        for m in ref.finditer(src):
            cands = by_name.get(m.group(1))
            if not cands:
                continue
            target = next((c for c in cands if c["module"] == mod), None) \
                or next((c for c in cands if c["module"].startswith("core:")), cands[0])
            target["uses"] += 1
            target["files"].add(rel)
    # Referencias desde XML de recursos (p. ej. el ícono adaptativo del launcher).
    for xml in (x for res in {d.parent for _, d in res_dirs()} for x in res.rglob("*.xml")):
        for name in re.findall(r"@drawable/(\w+)", xml.read_text(encoding="utf-8")):
            for c in by_name.get(name, []):
                c["uses"] += 1
                c["files"].add(str(xml.relative_to(ROOT)))
    for it in items:
        it["screens"] = sorted({sid for rel in it["files"] for sid in screen_by_file.get(rel, [])})
        it["files"] = sorted(it["files"])
    return items


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
        "assets": drawables(texts, screen_by_file),
    }
    template = (Path(__file__).parent / "plantilla.html").read_text(encoding="utf-8")
    payload = json.dumps(data, ensure_ascii=False).replace("</", "<\\/")
    (CATALOGO / "index.html").write_text(template.replace("/*__DATA__*/null", payload), encoding="utf-8")
    counts = {t: sum(1 for c in out if c["tier"] == t) for t in order}
    assets = {k: sum(1 for a in data["assets"] if a["kind"] == k) for k in ("Vector", "Imagen")}
    print(f"index.html: {len(screens)} pantallas, {len(out)} componentes {counts}, assets {assets}")


if __name__ == "__main__":
    main()
