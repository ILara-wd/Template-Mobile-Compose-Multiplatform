"""Genera docs/design/kensho_dojo_lottie.json (Lottie / Bodymovin 5.7) a partir del SVG animado de Stitch.

La geometría, colores y animaciones están transcritos a mano de docs/design/code.html (proyecto Stitch
"Kensho Dojo", pantalla "Animated SVG"); si el diseño cambia, actualiza las constantes de build().
El kanji 氣 se convierte a trazos con Noto Sans JP Black (SIL OFL), que se descarga a scripts/lottie/.cache/.

Uso (desde la raíz del repo):
    python3 -m venv scripts/lottie/.venv && scripts/lottie/.venv/bin/pip install fonttools
    scripts/lottie/.venv/bin/python -I scripts/lottie/build_lottie.py [salida.json ...]

Sin argumentos escribe docs/design/kensho_dojo_lottie.json y la copia del splash en
app/src/main/res/raw/lottie_tkd_splash.json (ADR-026).

Marcadores: "intro" (0–144, una vez) y "loop" (144–384, se repite).
"""
import json
import math
import os
import sys
import urllib.request

from fontTools.pens.recordingPen import RecordingPen
from fontTools.ttLib import TTFont
from fontTools.varLib.instancer import instantiateVariableFont

FR = 60
INTRO = 144          # 2.4 s: termina el ensō
LOOP = 240           # 4 s: periodo común de las animaciones infinitas
OP = INTRO + LOOP
EASE_OUT = (0.16, 1, 0.3, 1)
EASE_IN_OUT = (0.42, 0, 0.58, 1)
LINEAR = (0, 0, 1, 1)


# ---------- utilidades ----------
def hex_rgb(h, a=1):
    h = h.lstrip("#")
    return [int(h[i:i + 2], 16) / 255 for i in (0, 2, 4)] + [a]


def static(v):
    return {"a": 0, "k": v}


def cubic_bezier(x1, y1, x2, y2):
    def bez(t, a, b):
        return 3 * a * t * (1 - t) ** 2 + 3 * b * t ** 2 * (1 - t) + t ** 3

    def f(x):
        lo, hi = 0.0, 1.0
        for _ in range(60):
            mid = (lo + hi) / 2
            if bez(mid, x1, x2) < x:
                lo = mid
            else:
                hi = mid
        return bez((lo + hi) / 2, y1, y2)
    return f


def animated(keys, ease):
    """keys: [(frame, valor | [valores])]; ease: curva por tramo (una o lista)."""
    out = []
    for i, (t, v) in enumerate(keys):
        v = v if isinstance(v, list) else [v]
        k = {"t": round(t, 3), "s": v}
        if i < len(keys) - 1:
            e = ease[i] if isinstance(ease, list) else ease
            n = len(v)
            k["o"] = {"x": [e[0]] * n, "y": [e[1]] * n}
            k["i"] = {"x": [e[2]] * n, "y": [e[3]] * n}
        out.append(k)
    return {"a": 1, "k": out}


def css_keys(start, dur, stops, values):
    """Keyframes de un @keyframes CSS: stops en 0..1, values por stop."""
    return [(start + s * dur, v) for s, v in zip(stops, values)]


def periodic(start, period, stops, values, end=OP):
    keys = []
    t = start
    while t < end + period:
        for s, v in zip(stops[:-1], values[:-1]):
            keys.append((t + s * period, v))
        t += period
    keys.append((t, values[-1]))
    return keys


def sampled(start, end, fn):
    return {"a": 1, "k": [
        {"t": f, "s": (lambda v: v if isinstance(v, list) else [v])(fn(f)),
         "o": {"x": [0], "y": [0]}, "i": {"x": [1], "y": [1]}}
        for f in range(start, end + 1)
    ]}


# ---------- trazos ----------
class Path:
    def __init__(self):
        self.contours = []

    def move(self, p):
        self.contours.append({"v": [list(p)], "i": [[0, 0]], "o": [[0, 0]], "c": False})
        return self

    def _cur(self):
        return self.contours[-1]

    def line(self, p):
        c = self._cur()
        c["v"].append(list(p)); c["i"].append([0, 0]); c["o"].append([0, 0])
        return self

    def cubic(self, c1, c2, p):
        c = self._cur()
        last = c["v"][-1]
        c["o"][-1] = [c1[0] - last[0], c1[1] - last[1]]
        c["v"].append(list(p)); c["i"].append([c2[0] - p[0], c2[1] - p[1]]); c["o"].append([0, 0])
        return self

    def quad(self, q, p):
        p0 = self._cur()["v"][-1]
        c1 = (p0[0] + 2 / 3 * (q[0] - p0[0]), p0[1] + 2 / 3 * (q[1] - p0[1]))
        c2 = (p[0] + 2 / 3 * (q[0] - p[0]), p[1] + 2 / 3 * (q[1] - p[1]))
        return self.cubic(c1, c2, p)

    def close(self):
        c = self._cur()
        if len(c["v"]) > 1 and all(abs(a - b) < 1e-6 for a, b in zip(c["v"][0], c["v"][-1])):
            c["i"][0] = c["i"].pop(); c["v"].pop(); c["o"].pop()
        c["c"] = True
        return self

    def shapes(self):
        return [{"ty": "sh", "ks": static({k: c[k] for k in ("i", "o", "v", "c")})} for c in self.contours]


def poly(points, closed=True):
    p = Path().move(points[0])
    for pt in points[1:]:
        p.line(pt)
    return p.close() if closed else p


def circle_path(cx, cy, r):
    """Como <circle> de SVG: empieza a las 3 en punto y gira en sentido horario."""
    k = 0.5522847498 * r
    return (Path().move((cx + r, cy))
            .cubic((cx + r, cy + k), (cx + k, cy + r), (cx, cy + r))
            .cubic((cx - k, cy + r), (cx - r, cy + k), (cx - r, cy))
            .cubic((cx - r, cy - k), (cx - k, cy - r), (cx, cy - r))
            .cubic((cx + k, cy - r), (cx + r, cy - k), (cx + r, cy))
            .close())


def ellipse(cx, cy, r):
    return {"ty": "el", "p": static([cx, cy]), "s": static([2 * r, 2 * r]), "d": 1}


def rect(x, y, w, h, rx=0):
    return {"ty": "rc", "p": static([x + w / 2, y + h / 2]), "s": static([w, h]), "r": static(rx), "d": 1}


# ---------- pintura ----------
def fill(color, opacity=100):
    return {"ty": "fl", "c": static(hex_rgb(color)), "o": static(opacity) if not isinstance(opacity, dict) else opacity, "r": 1}


def stroke(color, w, opacity=100, cap=1, dash=None):
    s = {"ty": "st", "c": static(hex_rgb(color)), "o": static(opacity), "w": static(w), "lc": cap, "lj": 1, "ml": 4}
    if dash:
        s["d"] = [{"n": "d", "nm": "dash", "v": static(dash[0])}, {"n": "g", "nm": "gap", "v": static(dash[1])},
                  {"n": "o", "nm": "offset", "v": static(0)}]
    return s


def gradient_stops(stops):
    colors, alphas = [], []
    for off, col, a in stops:
        colors += [off] + hex_rgb(col)[:3]
        alphas += [off, a]
    return {"p": len(stops), "k": static(colors + alphas)}


def grad(kind, stops, s, e, w=None, cap=1):
    g = {"ty": "gf" if w is None else "gs", "o": static(100), "g": gradient_stops(stops),
         "s": static(list(s)), "e": static(list(e)), "t": kind, "h": static(0), "a": static(0)}
    if w is None:
        g["r"] = 1
    else:
        g.update({"w": static(w), "lc": cap, "lj": 1, "ml": 4})
    return g


GOLD = [(0, "#ffd54f", 1), (1, "#d4af37", 1)]
BELT = [(0, "#9b081a", 1), (0.5, "#c01025", 1), (1, "#800614", 1)]
GI_LEFT = [(0, "#f5f7fc", 1), (0.85, "#e2e8f0", 1), (1, "#cbd5e1", 1)]
GI_RIGHT = [(0, "#ffffff", 1), (0.8, "#e6ecf5", 1), (1, "#94a3b8", 1)]


def transform(a=(0, 0), p=(0, 0), s=(100, 100), r=0, o=100):
    w = lambda v: v if isinstance(v, dict) else static(list(v) if isinstance(v, tuple) else v)
    return {"ty": "tr", "a": w(a), "p": w(p), "s": w(s), "r": w(r), "o": w(o), "sk": static(0), "sa": static(0)}


def group(name, items, tr=None):
    return {"ty": "gr", "nm": name, "it": items + [tr or transform()]}


def stack(*groups):
    """Orden SVG (abajo → arriba) a orden Lottie (arriba primero)."""
    return list(reversed(groups))


def drop_shadow(dx, dy, blur, alpha):
    direction = math.degrees(math.atan2(dx, -dy)) % 360
    ef = [("Shadow Color", 2, [0, 0, 0, 1]), ("Opacity", 0, alpha * 255), ("Direction", 0, direction),
          ("Distance", 0, math.hypot(dx, dy)), ("Softness", 0, blur * 2), ("Shadow Only", 7, 0)]
    return {"ty": 25, "nm": "Drop Shadow", "np": 8, "mn": "ADBE Drop Shadow", "ix": 1, "en": 1,
            "ef": [{"ty": t, "nm": n, "mn": f"ADBE Drop Shadow-000{i + 1}", "ix": i + 1, "v": static(v)}
                   for i, (n, t, v) in enumerate(ef)]}


_ind = [0]


def layer(name, shapes=None, ks=None, parent=None, effects=None, null=False):
    _ind[0] += 1
    base = {"a": static([0, 0]), "p": static([0, 0]), "s": static([100, 100]), "r": static(0), "o": static(100)}
    base.update(ks or {})
    lay = {"ddd": 0, "ind": _ind[0], "ty": 3 if null else 4, "nm": name, "sr": 1, "ks": base, "ao": 0,
           "ip": 0, "op": OP, "st": 0, "bm": 0}
    if not null:
        lay["shapes"] = shapes
    if parent:
        lay["parent"] = parent
    if effects:
        lay["ef"] = effects
    return lay


# ---------- kanji 氣 ----------
def kanji_path(font_path):
    font = instantiateVariableFont(TTFont(font_path), {"wght": 900})
    gs = font.getGlyphSet()
    name = font.getBestCmap()[ord("氣")]
    pen = RecordingPen()
    gs[name].draw(pen)
    scale = 16 / font["head"].unitsPerEm
    adv = gs[name].width * scale + 1            # letter-spacing="1"
    x0, base = 160 - adv / 2, 122
    tx = lambda pt: (round(x0 + pt[0] * scale, 3), round(base - pt[1] * scale, 3))
    path = Path()
    for op, args in pen.value:
        if op == "moveTo":
            path.move(tx(args[0]))
        elif op == "lineTo":
            path.line(tx(args[0]))
        elif op == "qCurveTo":
            offs = list(args[:-1])
            if args[-1] is None:                 # contorno sin puntos sobre la curva
                end = ((offs[-1][0] + offs[0][0]) / 2, (offs[-1][1] + offs[0][1]) / 2)
                path.move(tx(end))
            else:
                end = args[-1]
            # off-curve consecutivos: el punto implícito es el punto medio
            for i, q in enumerate(offs):
                nxt = end if i == len(offs) - 1 else ((q[0] + offs[i + 1][0]) / 2, (q[1] + offs[i + 1][1]) / 2)
                path.quad(tx(q), tx(nxt))
        elif op == "curveTo":
            path.cubic(tx(args[0]), tx(args[1]), tx(args[2]))
        elif op in ("closePath", "endPath"):
            path.close()
    return path


# ---------- composición ----------
def build(font_path):
    layers = []

    # 1. Resplandor carmesí que respira (4 s)
    glow_keys = periodic(0, LOOP, [0, .5, 1], [92, 112, 92])
    layers.append(layer("glow", [group("glow", [ellipse(160, 160, 140), grad(
        2, [(0, "#c01025", .45), (.6, "#c01025", .1), (1, "#c01025", 0)], (160, 160), (300, 160))])],
        ks={"a": static([160, 160]), "p": static([160, 160]),
            "s": animated([(t, [v, v]) for t, v in glow_keys], EASE_IN_OUT),
            "o": animated(periodic(0, LOOP, [0, .5, 1], [35, 70, 35]), EASE_IN_OUT)}))

    # 2. Tatami (patrón 16×8 → rejilla recortada al círculo) + divisiones
    weave = Path()
    for y in range(4, 320, 8):
        d = 132 ** 2 - (y - 160) ** 2
        if d > 0:
            h = math.sqrt(d); weave.move((160 - h, y)).line((160 + h, y))
    for x in range(8, 320, 16):
        d = 132 ** 2 - (x - 160) ** 2
        if d > 0:
            h = math.sqrt(d); weave.move((x, 160 - h)).line((x, 160 + h))
    layers.append(layer("tatami", stack(
        group("piso", [ellipse(160, 160, 132), fill("#111318")]),
        group("tejido", weave.shapes() + [stroke("#1c202a", .8)]),
        group("borde", [ellipse(160, 160, 132), stroke("#2b313e", 2)]),
        group("divisiones", poly([(160, 28), (160, 292)], False).shapes()
              + poly([(28, 160), (292, 160)], False).shapes() + [stroke("#252b37", 1.5, dash=(4, 4))]),
    )))

    # 3. Ensō: stroke-dasharray 680 con dashoffset 680 → 0 y rotate(-90 → 0), 2.4 s
    L = 2 * math.pi * 118
    ease = cubic_bezier(*EASE_OUT)

    def enso(f):
        o = 680 * (1 - ease(min(f / INTRO, 1)))
        if o > 1360 - L:
            return 1360 - L - o, L - 680
        return 0, 680 - o
    layers.append(layer("enso", [group("enso", circle_path(160, 160, 118).shapes() + [
        {"ty": "tm", "s": static(0), "e": sampled(0, INTRO, lambda f: enso(f)[1] / L * 100),
         "o": sampled(0, INTRO, lambda f: enso(f)[0] / L * 360), "m": 1},
        stroke("#c01025", 5, opacity=95, cap=2)])],
        ks={"a": static([160, 160]), "p": static([160, 160]),
            "r": sampled(0, INTRO, lambda f: -90 * (1 - ease(min(f / INTRO, 1))))}))
    layers.append(layer("anillo", [group("anillo", [ellipse(160, 160, 112), stroke("#f1f5f9", 1.2, opacity=15)])]))

    # 4. Pecho
    layers.append(layer("pecho", [group("pecho", poly([(135, 115), (160, 165), (185, 115), (160, 85)]).shapes()
                                        + [fill("#0f172a", 80)])]))

    # 5. Solapas
    def lapel(name, origin, start, tx, rot, items, shadow):
        keys = css_keys(start, 72, [0, .6, 1], [0, 1, 2])
        pos = [(t, [origin[0] + [tx, -2 * tx / 35, 0][v], origin[1]]) for t, v in keys]
        return layer(name, items, effects=[shadow], ks={
            "a": static(list(origin)), "p": animated(pos, EASE_OUT),
            "r": animated([(t, [rot, -rot / 8, 0][v]) for t, v in keys], EASE_OUT),
            "o": animated([(t, [0, 100, 100][v]) for t, v in keys], EASE_OUT)})

    layers.append(lapel("solapa derecha", (220, 120), 24, 35, 8, stack(
        group("tela", poly([(215, 85), (140, 195), (160, 205), (235, 120)]).shapes()
              + [grad(1, GI_RIGHT, (235, 85), (140, 85))]),
        group("borde", poly([(215, 85), (140, 195)], False).shapes() + [stroke("#cbd5e1", 3, cap=2)]),
        group("costura", poly([(222, 90), (148, 198)], False).shapes() + [stroke("#94a3b8", 1, cap=2)]),
    ), drop_shadow(0, 2, 4, .5)))

    patch_tr = transform(a=(112, 132), p=(112, 132), r=-30)
    layers.append(lapel("solapa izquierda", (100, 120), 12, -35, -8, stack(
        group("tela", poly([(105, 85), (180, 195), (160, 205), (85, 120)]).shapes()
              + [grad(1, GI_LEFT, (85, 85), (180, 85))]),
        group("borde", poly([(105, 85), (180, 195)], False).shapes() + [stroke("#ffffff", 4.5, cap=2)]),
        group("costura", poly([(98, 90), (172, 198)], False).shapes() + [stroke("#e2e8f0", 1.5, cap=2)]),
        group("parche", [rect(112, 132, 16, 24, 2), fill("#c01025", 90)], patch_tr),
        group("parche hilo", poly([(113, 140), (125, 133)], False).shapes() + [stroke("#ffd54f", 1.5)],
              transform(a=(112, 132), p=(112, 132), r=-30)),
    ), drop_shadow(2, 6, 8, .6)))

    # 6. Kanji: scale(0.6) translateY(8px) → 1, desde 0.9 s durante 1.2 s
    def kanji_state(f):
        u = (f - 54) / 72
        stops = [(0, 0, .6, 8), (.7, .8, 1.05, -2), (1, 1, 1, 0)]
        if u <= 0:
            return stops[0][1:]
        if u >= 1:
            return stops[-1][1:]
        a, b = (stops[0], stops[1]) if u < .7 else (stops[1], stops[2])
        k = ease((u - a[0]) / (b[0] - a[0]))
        return tuple(a[i] + (b[i] - a[i]) * k for i in (1, 2, 3))
    layers.append(layer("kanji", stack(
        group("sello", [ellipse(160, 115, 18), fill("#1e2430"), stroke("#c01025", 1.5)]),
        group("氣", kanji_path(font_path).shapes() + [fill("#f8fafc")]),
    ), ks={"a": static([160, 105]),
           "p": sampled(54, 126, lambda f: [160, 105 + kanji_state(f)[2] * kanji_state(f)[1]]),
           "s": sampled(54, 126, lambda f: [kanji_state(f)[1] * 100] * 2),
           "o": sampled(54, 126, lambda f: kanji_state(f)[0] * 100)}))

    # 7. Cinturón: scaleX(0.2 → 1.08 → 1) desde 0.7 s, 1 s. Opacidad copiada en cada hijo.
    belt_keys = css_keys(42, 60, [0, .5, 1], [0, 1, 2])
    belt_opacity = lambda: animated([(t, [0, 90, 100][v]) for t, v in belt_keys], EASE_OUT)
    belt = layer("cinturon", null=True, ks={
        "a": static([160, 200]), "p": static([160, 200]),
        "s": animated([(t, [[20, 100], [108, 100], [100, 100]][v]) for t, v in belt_keys], EASE_OUT)})
    belt_ind = belt["ind"]

    tails = layer("puntas", stack(
        group("punta izquierda", Path().move((152, 206)).quad((146, 235), (142, 268)).line((157, 268))
              .quad((161, 235), (163, 206)).close().shapes() + [grad(1, BELT, (142, 206), (163, 206))]),
        group("hilo izquierdo", poly([(148, 212), (148, 265)], False).shapes() + [stroke("#ff4d6d", .8, 40)]),
        group("punta derecha", Path().move((160, 206)).quad((168, 236), (174, 272)).line((189, 270))
              .quad((180, 234), (169, 206)).close().shapes() + [grad(1, BELT, (160, 206), (189, 206))]),
        group("punta negra", poly([(172, 254), (174, 272), (189, 270), (186, 252)]).shapes() + [fill("#090d16")]),
        group("franja", poly([(178, 256), (183, 255)], False).shapes() + [grad(1, GOLD, (178, 255), (183, 256), w=2)]),
    ), parent=belt_ind, effects=[drop_shadow(1, 4.5, 6.5, .55)], ks={
        "a": static([160, 206]), "p": static([160, 206]),
        "r": animated(periodic(72, LOOP, [0, .33, .66, 1], [0, 2.5, -2, 0]), EASE_IN_OUT), "o": belt_opacity()})

    band = layer("banda", stack(
        group("banda", [rect(75, 194, 170, 18, 3), grad(1, BELT, (75, 194), (245, 194))]),
        group("pespunte", poly([(75, 203), (245, 203)], False).shapes() + [stroke("#e11d48", .8, 50, dash=(6, 3))]),
        group("barra de grado", [rect(200, 194, 34, 18), fill("#090d16")]),
        group("franjas", poly([(214, 194), (214, 212)], False).shapes() + poly([(222, 194), (222, 212)], False).shapes()
              + [grad(1, GOLD, (214, 194), (222, 212), w=2.5)]),
    ), parent=belt_ind, effects=[drop_shadow(0, 4, 6, .7)], ks={"o": belt_opacity()})

    knot = layer("nudo", stack(
        group("nudo", [rect(148, 190, 24, 24, 5), fill("#800614"), stroke("#c01025", 1.8)]),
        group("pliegue sup", Path().move((151, 192)).quad((160, 202), (169, 192)).shapes() + [stroke("#e11d48", 1.5)]),
        group("pliegue inf", Path().move((152, 211)).quad((160, 202), (168, 211)).shapes() + [stroke("#4c0510", 2)]),
        group("sello", [ellipse(160, 202, 3.2), grad(1, GOLD, (156.8, 198.8), (163.2, 205.2))]),
    ), parent=belt_ind, effects=[drop_shadow(0, 2, 5, .8)], ks={
        "a": static([160, 200]), "p": static([160, 200]),
        "s": animated([(t, [v, v]) for t, v in periodic(84, LOOP, [0, .5, 1], [100, 106, 100])], EASE_IN_OUT),
        "o": belt_opacity()})
    layers += [belt, band, tails, knot]

    # 8. Chispas (SMIL, lineal): periodos ajustados a 2 s para que el loop de 4 s cierre
    def spark(name, cx, cy, r, color, op_vals=None, cy_vals=None, r_vals=None):
        stops = [0, .5, 1]
        p = animated([(t, [cx, v]) for t, v in periodic(0, 120, stops, cy_vals)], LINEAR) if cy_vals else static([cx, cy])
        s = animated([(t, [2 * v, 2 * v]) for t, v in periodic(0, 120, stops, r_vals)], LINEAR) if r_vals else static([2 * r] * 2)
        o = animated(periodic(0, 120, stops, [v * 100 for v in op_vals]), LINEAR) if op_vals else None
        return layer(name, [group(name, [{"ty": "el", "p": p, "s": s, "d": 1}, fill(color, o or 90)])])
    layers += [spark("chispa 1", 85, 100, 1.8, "#ffd54f", op_vals=[.2, 1, .2], cy_vals=[100, 92, 100]),
               spark("chispa 2", 238, 112, 2, "#ffd54f", op_vals=[.8, .2, .8], cy_vals=[112, 104, 112]),
               spark("chispa 3", 160, 48, 2.2, "#c01025", r_vals=[1.8, 3, 1.8])]

    return {"v": "5.7.0", "fr": FR, "ip": 0, "op": OP, "w": 320, "h": 320, "nm": "Kensho Dojo", "ddd": 0,
            "assets": [], "layers": list(reversed(layers)),
            "markers": [{"tm": 0, "cm": "intro", "dr": INTRO}, {"tm": INTRO, "cm": "loop", "dr": LOOP}]}


HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(os.path.dirname(HERE))
FONT_URL = "https://github.com/google/fonts/raw/main/ofl/notosansjp/NotoSansJP%5Bwght%5D.ttf"
FONT_PATH = os.path.join(HERE, ".cache", "NotoSansJP.ttf")
DEFAULT_OUTS = [os.path.join(ROOT, "docs", "design", "kensho_dojo_lottie.json"),
                os.path.join(ROOT, "app", "src", "main", "res", "raw", "lottie_tkd_splash.json")]


def ensure_font():
    if not os.path.exists(FONT_PATH):
        os.makedirs(os.path.dirname(FONT_PATH), exist_ok=True)
        print(f"Descargando Noto Sans JP → {FONT_PATH}")
        urllib.request.urlretrieve(FONT_URL, FONT_PATH)
    return FONT_PATH


if __name__ == "__main__":
    data = build(ensure_font())
    for out_path in sys.argv[1:] or DEFAULT_OUTS:
        os.makedirs(os.path.dirname(out_path), exist_ok=True)
        with open(out_path, "w") as out:
            json.dump(data, out, ensure_ascii=False, separators=(",", ":"))
        print(f"{out_path}: {len(data['layers'])} capas, {data['op']} cuadros a {data['fr']} fps")
