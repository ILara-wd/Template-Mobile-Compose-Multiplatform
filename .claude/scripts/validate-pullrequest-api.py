#!/usr/bin/env python3
"""FF-CMP-SURFACE — superficie de composables entre la base y HEAD en SportKid (multimódulo).

Uso (desde la raíz del repo):
  BASE_BRANCH=sport_kids python3 .claude/scripts/validate-pullrequest-api.py
  BASE_REF=HEAD~3 python3 .claude/scripts/validate-pullrequest-api.py      # últimos commits
  STAGED=1 python3 .claude/scripts/validate-pullrequest-api.py             # lo que está en el índice

Solo usa git local (funciona igual para un PR de GitHub una vez que su rama está en checkout;
ver Sección 0.2 del skill). SportKid no publica librerías, pero `core:designsystem` y `core:ui`
son la API que consumen los features, así que su superficie de composables sí importa:

  FF-CMP-09  composable público nuevo/modificado sin una preview del mismo archivo que lo
             renderice (CLAUDE.md → Convenciones → Componentes: ≥ 1 preview private con @SkPreviews)
  FF-CMP-10  preview que no es `private`
  FF-CMP-11  ViewModel o NavController como parámetro de un composable: SportKid no tiene
             ViewModels ni Navigation Compose; las pantallas reciben datos + callbacks y la
             navegación vive en `SportKidApp` (`Destination`, app/MainActivity.kt)
  FF-CMP-12  composable público en un feature que no es `<X>Screen` ni lo usa `:app`
             (las secciones van `private`/`internal`)
  FF-CMP-13  firma de un componente compartido (core:designsystem o core:ui) que cambió de forma
             incompatible: parámetro eliminado/renombrado, sin default nuevo o reordenado
             (CLAUDE.md: "parámetro nuevo en componente existente: al final y con default")
  FF-CMP-14  preview nueva de un componente con @Preview suelto en lugar de @SkPreviews
             (claro, oscuro y fuente 1.3)

Es un parser heurístico de declaraciones top-level de Kotlin (columna 0).
"""
import os
import re
import subprocess
import sys

BRANCH = os.environ.get("BASE_BRANCH", "sport_kids")
BASE = os.environ.get("BASE_REF", f"origin/{BRANCH}")
STAGED = os.environ.get("STAGED") == "1"
MAIN_SPECS = ["app/src/main/*.kt", "core/*/src/main/*.kt", "feature/*/src/main/*.kt"]
SHARED = re.compile(r"^core/(designsystem|ui)/src/main/")
APP_MAIN = "app/src/main"
PREVIEW_ANN = re.compile(r'^@(Preview\w*|SkPreviews)\b')   # multipreview de core:designsystem/preview/SkPreviews.kt
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


def diff_range(base):
    """(argumentos de git diff, árbol "antes" para git show)."""
    if STAGED:
        return ["--cached"], "HEAD"
    mb = git("merge-base", base, "HEAD").strip()
    return [f"{base}...HEAD"], (mb or base)


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
            "skpreviews": any(a.startswith("@SkPreviews") for a in ann),
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


def usages(name):
    """Archivos de main (todos los módulos) que llaman a `name(`."""
    out = subprocess.run(["git", "grep", "-l", "-E", rf"(^|[^\w.]){re.escape(name)}\s*\(", "--", *MAIN_SPECS],
                         capture_output=True, text=True).stdout
    return [l for l in out.splitlines() if l]


def main():
    rng, before = diff_range(base_ref())
    changed = git("diff", "--name-status", "-M", *rng, "--", *MAIN_SPECS).splitlines()
    findings = 0
    for row in changed:
        parts = row.split("\t")
        status, path = parts[0], parts[-1]
        old_path = parts[1] if status.startswith("R") else path
        if status.startswith("D"):
            continue
        old_src = git("show", f"{before}:{old_path}") if not status.startswith("A") else ""
        new_src = open(path).read() if os.path.exists(path) else ""
        old, new = parse(old_src), parse(new_src)
        is_shared = bool(SHARED.match(path))
        in_feature = path.startswith("feature/")
        in_preview_pkg = "/preview/" in path      # SkPreviews y TokensPreview: herramientas de preview

        preview_bodies = re.findall(r'@(?:Preview\w*|SkPreviews)\b[\s\S]*?\n}', new_src)
        old_previews = {k for k, v in old.items() if any(d["preview"] for d in v)}
        for key, news in new.items():
            for n in news:
                touched = not any(sig(o) == sig(n) for o in old.get(key, []))
                if not touched or n["kind"] != "fun":
                    continue
                if n["preview"]:
                    if n["visibility"] != "private":
                        print(f"🟡 FF-CMP-10 {path}:{n['line']} preview `{n['name']}` no es private")
                    if not n["skpreviews"] and key not in old_previews and not in_preview_pkg:
                        print(f"🟡 FF-CMP-14 {path}:{n['line']} preview `{n['name']}` usa @Preview suelto — "
                              f"usa @SkPreviews (claro, oscuro, fuente 1.3)")
                    continue
                if not n["composable"]:
                    continue
                params = n["params"] or []
                # FF-CMP-11 — ViewModel / NavController: no existen en SportKid
                for p in params:
                    if re.search(r'ViewModel\b|NavController|NavHostController', p["type"]):
                        print(f"🔴 FF-CMP-11 {path}:{n['line']} `{n['name']}` recibe `{p['name']}: {p['type']}` — "
                              f"las pantallas reciben datos + callbacks; ViewModel/Navigation requieren decisión en CLAUDE.md")
                        findings += 1
                short = n["name"].split(".")[-1]
                if n["visibility"] != "public" or not short[:1].isupper():
                    continue       # remember<X>State() y helpers en minúscula no son componentes
                # FF-CMP-12 — composables públicos en un feature
                if in_feature and not short.endswith("Screen") and not any(
                        u.startswith(APP_MAIN) for u in usages(short)):
                    print(f"🟡 FF-CMP-12 {path}:{n['line']} `{short}` es público en un feature y :app no lo usa — "
                          f"¿private/internal?")
                # FF-CMP-09 — sin preview que lo renderice
                if in_preview_pkg:
                    continue
                if not any(re.search(rf'(?<![\w.]){re.escape(short)}\s*[({{]', b) for b in preview_bodies):
                    print(f"🟠 FF-CMP-09 {path}:{n['line']} composable público `{short}` sin preview @SkPreviews "
                          f"que lo renderice en el mismo archivo")
                    findings += 1

        # FF-CMP-13 — cambios incompatibles en componentes compartidos (core:designsystem, core:ui)
        if is_shared and old_src:
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
                    users = [u for u in usages(o["name"]) if u != path]
                    print(f"🟡 FF-CMP-13 {path} — `{o['name']}` cambió de forma incompatible: {why}. "
                          f"Confirmar que el PR actualiza todos los usos ({len(users)} archivo(s): "
                          f"git grep -n '{o['name']}(' -- 'app/src/main' 'core/*/src/main' 'feature/*/src/main')")

    if not findings:
        print("ℹ️ FF-CMP-SURFACE: sin composables públicos sin preview ni ViewModel/NavController en firmas")


if __name__ == "__main__":
    sys.exit(main())
