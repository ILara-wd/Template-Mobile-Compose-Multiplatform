#!/usr/bin/env python3
"""FF-CMP-SURFACE — superficie de composables entre $BASE_REF y HEAD en Kensho (:app).

Uso: BASE_BRANCH=develop python3 .claude/scripts/validate-pullrequest-api.py

Kensho es una app de un solo módulo (no publica artefactos), así que aquí no hay "API pública"
que versionar. Lo que sí importa es la superficie de composables:

  FF-CMP-09  composable público nuevo/modificado sin @Preview que lo renderice (CMP-09, ADR-010)
  FF-CMP-10  @Preview pública (debe ser private)
  FF-CMP-11  ViewModel o NavController como parámetro de un composable que no es *Screen /
             TkdApp (ARC-01, ARC-04)
  FF-CMP-12  composable público en el paquete de un feature que no es Screen/Content
             (secciones deben ser internal/private — CMP-04)
  FF-CMP-13  firma de un componente compartido (components/) que cambió de forma incompatible:
             parámetro eliminado/renombrado, sin default nuevo, o reordenado (CMP-07: extender sin
             romper los usos actuales). Informativo si todos los usos se actualizaron en el PR.

Es un parser heurístico de declaraciones top-level de Kotlin (columna 0).
"""
import os
import re
import subprocess
import sys

BRANCH = os.environ.get("BASE_BRANCH", "develop")
BASE = os.environ.get("BASE_REF", f"origin/{BRANCH}")
MAIN = "app/src/main/java/dev/treecode/kensho/"
PREVIEW_ANN = re.compile(r'^@(Preview\w*|TkdFontScalePreviews)\b')  # multipreview de ui/theme/TkdFontScale.kt
DECL = re.compile(
    r'^(?P<mods>(?:(?:public|internal|private|inline|suspend|operator|infix|tailrec|external|'
    r'abstract|open|sealed|data|enum|annotation|value|const|lateinit)\s+)*)'
    r'(?P<kind>fun|class|interface|object|val|var|typealias)\s+'
    r'(?:<[^>]*>\s*)?(?P<name>[\w.]+)')


def git(*args):
    return subprocess.run(["git", *args], capture_output=True, text=True).stdout


def base_ref():
    ok = subprocess.run(["git", "rev-parse", "--verify", "--quiet", BASE], capture_output=True).returncode == 0
    return BASE if ok else BRANCH


def split_params(raw):
    params, buf, depth, prev = [], "", 0, ""
    for ch in raw:
        if ch in "(<{[":
            depth += 1
        elif ch in ")}]" or (ch == ">" and prev != "-"):   # el '>' de '->' no cierra genéricos
            depth -= 1
        prev = ch
        if ch == "," and depth == 0:
            params.append(buf)
            buf = ""
        else:
            buf += ch
    params.append(buf)
    out = []
    for p in params:
        p = re.sub(r"//[^\n]*|/\*.*?\*/", "", p, flags=re.S)
        p = re.sub(r"\s+", " ", p).strip()
        if not p:
            continue
        p = re.sub(r"^(?:@[\w:]+(?:\([^)]*\))?\s+)*", "", p)        # anotaciones de parámetro
        p = re.sub(r"^(?:(?:private|internal|public|override|open)\s+)?(?:val|var)\s+", "", p)
        m = re.match(r"(\w+)\s*:\s*(.+?)(?:\s*=\s*(.+))?$", p)
        if m:
            out.append({"name": m.group(1), "type": m.group(2).strip(), "default": m.group(3)})
    return out


def parse(src):
    """dict: clave → lista de declaraciones no privadas {kind,name,params,line,visibility,...}"""
    lines = src.split("\n")
    decls, ann, offsets = {}, [], [0]
    for ln in lines:
        offsets.append(offsets[-1] + len(ln) + 1)
    for idx, ln in enumerate(lines):
        if ln.startswith("@"):
            ann.append(ln.strip())
            continue
        m = DECL.match(ln)
        if not m:
            if ln.strip() and not ln.startswith((" ", "\t", "//", "/*", "*", "import", "package")):
                ann = []
            continue
        mods = m.group("mods")
        visibility = "private" if "private" in mods else "internal" if "internal" in mods else "public"
        kind, name = m.group("kind"), m.group("name")
        params = None
        start = offsets[idx] + m.end()
        paren = src.find("(", start)
        brace_or_eol = min([x for x in (src.find("{", start), src.find("\n", start)) if x != -1] or [len(src)])
        if kind in ("fun", "class") and paren != -1 and (kind == "fun" or paren <= brace_or_eol):
            depth, k = 0, paren
            while k < len(src):
                depth += {"(": 1, ")": -1}.get(src[k], 0)
                k += 1
                if depth == 0:
                    break
            params = split_params(src[paren + 1:k - 1])
        decls.setdefault(f"{kind} {name}", []).append({
            "kind": kind, "name": name, "params": params, "line": idx + 1, "visibility": visibility,
            "composable": any(a.startswith("@Composable") for a in ann),
            "preview": any(PREVIEW_ANN.match(a) for a in ann),
        })
        ann = []
    return decls


def sig(d):
    if d["params"] is None:
        return d["name"]
    return f'{d["name"]}(' + ", ".join(f'{p["name"]}: {p["type"]}' + (" = …" if p["default"] else "")
                                       for p in d["params"]) + ")"


def incompatible(old, new):
    """None si `new` acepta todas las llamadas de `old`; si no, motivo."""
    if old["params"] is None or new["params"] is None:
        return None
    new_by_name = {p["name"]: (i, p) for i, p in enumerate(new["params"])}
    last = -1
    for p in old["params"]:
        if p["name"] not in new_by_name:
            return f'se eliminó/renombró el parámetro `{p["name"]}`'
        j, q = new_by_name[p["name"]]
        if q["type"].replace(" ", "") != p["type"].replace(" ", ""):
            return f'`{p["name"]}` cambió de tipo `{p["type"]}` → `{q["type"]}`'
        if p["default"] and not q["default"]:
            return f'`{p["name"]}` perdió su valor por defecto'
        if j < last:
            return f'se reordenó el parámetro `{p["name"]}`'
        last = j
    old_names = {p["name"] for p in old["params"]}
    for q in new["params"]:
        if q["name"] not in old_names and not q["default"] and "@Composable" not in q["type"]:
            return f'parámetro nuevo `{q["name"]}` sin valor por defecto'
    return None


def main():
    base = base_ref()
    changed = git("diff", "--name-status", "-M", f"{base}...HEAD", "--", f"{MAIN}*.kt").splitlines()
    findings = 0
    for row in changed:
        parts = row.split("\t")
        status, path = parts[0], parts[-1]
        old_path = parts[1] if status.startswith("R") else path
        if status.startswith("D"):
            continue
        old_src = git("show", f"{base}:{old_path}") if not status.startswith("A") else ""
        new_src = open(path).read() if os.path.exists(path) else ""
        old, new = parse(old_src), parse(new_src)
        base_name = os.path.basename(path)
        is_screen_file = base_name.endswith("Screen.kt") or base_name == "TkdApp.kt"
        in_components = "/components/" in path
        in_feature = not in_components and not any(f"/{d}/" in path for d in ("model", "data", "core", "ui/theme"))

        preview_bodies = re.findall(r'@(?:Preview\w*|TkdFontScalePreviews)\b[\s\S]*?\n}', new_src)
        for key, news in new.items():
            for n in news:
                touched = not any(sig(o) == sig(n) for o in old.get(key, []))
                if not touched:
                    continue
                if n["preview"]:
                    if n["visibility"] != "private":
                        print(f"🟡 FF-CMP-10 {path}:{n['line']} preview `{n['name']}` no es private")
                    continue
                if not n["composable"] or n["kind"] != "fun":
                    continue
                params = n["params"] or []
                # FF-CMP-11 — ViewModel / NavController fuera de Screen
                for p in params:
                    if re.search(r'ViewModel\b|NavController|NavHostController', p["type"]) and not (
                            is_screen_file and n["name"].endswith(("Screen", "App"))):
                        print(f"🔴 FF-CMP-11 {path}:{n['line']} `{n['name']}` recibe `{p['name']}: {p['type']}` (ARC-01/ARC-04)")
                        findings += 1
                if n["visibility"] != "public":
                    continue
                # FF-CMP-12 — secciones públicas en un feature
                if in_feature and not n["name"].endswith(("Screen", "Content", "App")) and "MainActivity" not in path:
                    print(f"🟡 FF-CMP-12 {path}:{n['line']} `{n['name']}` es público en un paquete de feature — ¿internal/private? (CMP-04)")
                # FF-CMP-09 — sin preview que lo renderice
                short = n["name"].split(".")[-1]
                if not any(re.search(rf'(?<![\w.]){re.escape(short)}\s*\(', b) for b in preview_bodies):
                    if n["name"].endswith("Screen") and any(re.search(rf'{re.escape(short[:-6])}Content\s*\(', b) for b in preview_bodies):
                        continue   # el Screen se documenta con la preview de su Content
                    if n["name"] == "TkdApp":
                        continue
                    print(f"🟠 FF-CMP-09 {path}:{n['line']} composable público `{n['name']}` sin @Preview que lo renderice (CMP-09)")
                    findings += 1

        # FF-CMP-13 — cambios incompatibles en componentes compartidos
        if in_components and old_src:
            for key, olds in old.items():
                for o in olds:
                    if o["preview"] or o["visibility"] == "private" or not o["composable"]:
                        continue
                    news = [n for n in new.get(key, []) if n["visibility"] != "private"]
                    if any(sig(n) == sig(o) for n in news):
                        continue
                    reasons = [incompatible(o, n) for n in news]
                    if news and any(r is None for r in reasons):
                        print(f"✅ FF-CMP-13 {path}:{o['line']} {sig(o)} → extensión compatible")
                        continue
                    why = "eliminado o con visibilidad reducida" if not news else "; ".join(sorted({r for r in reasons if r}))
                    print(f"🟡 FF-CMP-13 {path} — `{o['name']}` cambió de forma incompatible: {why}. "
                          f"Confirmar que el PR actualiza todos los usos (grep -rn '{o['name']}(' {MAIN})")

    if not findings:
        print("ℹ️ FF-CMP-SURFACE: sin composables sin preview ni ViewModels fuera de Screen")


if __name__ == "__main__":
    sys.exit(main())
