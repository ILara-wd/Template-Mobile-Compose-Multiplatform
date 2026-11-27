#!/usr/bin/env python3
"""Captura las pantallas de docs/ui-catalogo/pantallas.json desde el emulador de Kensho.

Kensho no tiene extras de debug para abrir una pantalla: cada una se alcanza desde el inicio de la app con
`captura.pasos`, que se ejecutan con `uiautomator`:
    {"tocar": "Mi Dojo"}          toca el nodo con ese texto exacto
    {"tocar": "/^Cinta /"}        … o el primero cuyo texto cumpla la expresión regular
    {"tocarDesc": "Perfil"}       toca el nodo con ese contentDescription
    {"deslizar": 2}               desliza hacia arriba esa cantidad de veces (elementos más abajo de la lista)
    {"esperar": 2}                espera segundos
Después toma la primera vista y `scrolls` vistas más deslizando, y guarda JPEG de 360 px de ancho en
docs/ui-catalogo/img/<id>-<n>.jpg. Las pantallas con `"captura": null` se omiten (p. ej. las de login).

Requisitos: sesión iniciada en la app (salvo login y registro), `adb` en el PATH o en
~/Library/Android/sdk/platform-tools y `sips` (macOS). Solo usa emuladores: con varios conectados, define
ANDROID_SERIAL=emulator-5554; nunca captura un teléfono físico (CLAUDE.md, escala de fuente).

Uso:
    ./gradlew :app:installDebug
    python3 .claude/scripts/ui-catalogo/capturar.py            # todas
    python3 .claude/scripts/ui-catalogo/capturar.py P09 P10    # solo algunas
"""
import json
import os
import re
import shutil
import subprocess
import sys
import tempfile
import time
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
CATALOGO = ROOT / "docs" / "ui-catalogo"
IMG = CATALOGO / "img"
ANCHO = 360
BOUNDS = re.compile(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]")


def adb_path() -> str:
    found = shutil.which("adb")
    if found:
        return found
    sdk = Path.home() / "Library" / "Android" / "sdk" / "platform-tools" / "adb"
    if sdk.exists():
        return str(sdk)
    sys.exit("No encontré adb. Agrégalo al PATH.")


ADB = adb_path()


def adb(*args: str, capture: bool = False) -> bytes:
    result = subprocess.run([ADB, *args], check=True, stdout=subprocess.PIPE if capture else subprocess.DEVNULL)
    return result.stdout if capture else b""


def elegir_emulador() -> None:
    """Fija ANDROID_SERIAL a un emulador; se niega a capturar un dispositivo físico."""
    serial = os.environ.get("ANDROID_SERIAL")
    if serial:
        if not serial.startswith("emulator-"):
            sys.exit(f"ANDROID_SERIAL={serial} no es un emulador; el catálogo solo se captura en emuladores.")
        return
    lines = subprocess.run([ADB, "devices"], check=True, capture_output=True, text=True).stdout.splitlines()[1:]
    emuladores = [ln.split()[0] for ln in lines if ln.strip().endswith("device") and ln.startswith("emulator-")]
    if len(emuladores) != 1:
        sys.exit(f"Necesito exactamente un emulador encendido (hay {len(emuladores)}). Define ANDROID_SERIAL.")
    os.environ["ANDROID_SERIAL"] = emuladores[0]


def screen_size() -> tuple[int, int]:
    out = adb("shell", "wm", "size", capture=True).decode()
    w, h = out.strip().splitlines()[-1].split()[-1].split("x")
    return int(w), int(h)


def nodos() -> list[ET.Element]:
    adb("shell", "uiautomator", "dump", "/sdcard/tkd-ui.xml")
    xml = adb("exec-out", "cat", "/sdcard/tkd-ui.xml", capture=True).decode("utf-8", errors="replace")
    return list(ET.fromstring(xml).iter("node"))


def tocar(atributo: str, valor: str) -> None:
    regex = re.compile(valor[1:-1]) if len(valor) > 2 and valor.startswith("/") and valor.endswith("/") else None
    for nodo in nodos():
        texto = nodo.get(atributo, "")
        if (regex and regex.search(texto)) or (not regex and texto == valor):
            x1, y1, x2, y2 = map(int, BOUNDS.match(nodo.get("bounds", "")).groups())
            adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))
            return
    raise LookupError(f"No encontré {atributo}={valor!r} en pantalla")


def exigir_sesion(paquete: str, actividad: str) -> None:
    """Las pantallas del dojo necesitan sesión: si la app abre en el login, no tiene caso seguir."""
    adb("shell", "am", "start", "-S", "-W", "-n", f"{paquete}/{actividad}")
    time.sleep(5)
    if any(n.get("text") == "Iniciar Sesión" for n in nodos()):
        sys.exit("La app está en el login: inicia sesión en el emulador y vuelve a correr el script.")


def guardar(png: bytes, destino: Path) -> None:
    with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as tmp:
        tmp.write(png)
    try:
        subprocess.run(
            ["sips", "-s", "format", "jpeg", "-s", "formatOptions", "80", "--resampleWidth", str(ANCHO), tmp.name, "--out", str(destino)],
            check=True, stdout=subprocess.DEVNULL,
        )
    finally:
        os.unlink(tmp.name)


def deslizar(size: tuple[int, int]) -> None:
    """Desliza ~55 % de la pantalla hacia arriba, dejando contexto entre vistas."""
    w, h = size
    adb("shell", "input", "swipe", str(w // 2), str(int(h * 0.75)), str(w // 2), str(int(h * 0.2)), "600")
    time.sleep(1.2)


def capturar(pantalla: dict, paquete: str, actividad: str, size: tuple[int, int]) -> list[str]:
    spec = pantalla["captura"]
    adb("shell", "am", "start", "-S", "-W", "-n", f"{paquete}/{actividad}")
    time.sleep(spec.get("inicio", 5))
    for paso in spec.get("pasos", []):
        if "tocar" in paso:
            tocar("text", paso["tocar"])
        elif "tocarDesc" in paso:
            tocar("content-desc", paso["tocarDesc"])
        elif "deslizar" in paso:
            for _ in range(paso["deslizar"]):
                deslizar(size)
        time.sleep(paso.get("esperar", 2))
    time.sleep(spec.get("espera", 2))
    archivos = []
    for n in range(spec.get("scrolls", 0) + 1):
        if n > 0:
            deslizar(size)
        nombre = f"{pantalla['id']}-{n + 1}.jpg"
        guardar(adb("exec-out", "screencap", "-p", capture=True), IMG / nombre)
        archivos.append(nombre)
    return archivos


def main() -> None:
    manifest = json.loads((CATALOGO / "pantallas.json").read_text(encoding="utf-8"))
    solo = set(sys.argv[1:])
    elegir_emulador()
    IMG.mkdir(parents=True, exist_ok=True)
    size = screen_size()
    exigir_sesion(manifest["paquete"], manifest["actividad"])
    fallos = 0
    for pantalla in manifest["pantallas"]:
        if (solo and pantalla["id"] not in solo) or not pantalla.get("captura"):
            continue
        for viejo in IMG.glob(f"{pantalla['id']}-*.jpg"):
            viejo.unlink()
        try:
            archivos = capturar(pantalla, manifest["paquete"], manifest["actividad"], size)
            print(f"{pantalla['id']:5} {pantalla['nombre']}: {', '.join(archivos)}")
        except LookupError as error:
            fallos += 1
            print(f"{pantalla['id']:5} {pantalla['nombre']}: SIN CAPTURA ({error})")
    print("Listo. Regenera el catálogo: python3 .claude/scripts/ui-catalogo/generar.py")
    if fallos:
        sys.exit(1)


if __name__ == "__main__":
    main()
