#!/usr/bin/env python3
"""Captura las pantallas de docs/ui-catalogo/pantallas.json desde un emulador o dispositivo.

Abre cada pantalla directo con los extras de debug de MainActivity (sk_screen, sk_tab, sk_athlete,
sk_sport, sk_quickadd), toma la primera vista y `scrolls` vistas más deslizando, y guarda JPEG de
360 px de ancho en docs/ui-catalogo/img/<id>-<n>.jpg.

Uso:
    ./gradlew :app:installDebug
    python3 scripts/ui-catalogo/capturar.py            # todas
    python3 scripts/ui-catalogo/capturar.py P14 P15    # solo algunas
Requiere `adb` en el PATH (o en ~/Library/Android/sdk/platform-tools) y `sips` (macOS).
Con varios dispositivos conectados, define ANDROID_SERIAL.
"""
import json
import os
import shutil
import subprocess
import sys
import tempfile
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CATALOGO = ROOT / "docs" / "ui-catalogo"
IMG = CATALOGO / "img"
ANCHO = 360


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


def screen_size() -> tuple[int, int]:
    out = adb("shell", "wm", "size", capture=True).decode()
    w, h = out.strip().split()[-1].split("x")
    return int(w), int(h)


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


def capturar(pantalla: dict, paquete: str, size: tuple[int, int]) -> list[str]:
    spec = pantalla["captura"]
    extras: list[str] = []
    for key, value in spec.get("extras", {}).items():
        extras += ["--ez", key, value] if value in ("true", "false") else ["--es", key, value]
    adb("shell", "am", "start", "-S", "-W", "-n", f"{paquete}/.MainActivity", *extras)
    time.sleep(spec.get("espera", 3))
    w, h = size
    archivos = []
    for n in range(spec.get("scrolls", 0) + 1):
        if n > 0:
            # Desliza ~55% de la pantalla, dejando contexto entre capturas.
            adb("shell", "input", "swipe", str(w // 2), str(int(h * 0.75)), str(w // 2), str(int(h * 0.2)), "600")
            time.sleep(1.2)
        nombre = f"{pantalla['id']}-{n + 1}.jpg"
        guardar(adb("exec-out", "screencap", "-p", capture=True), IMG / nombre)
        archivos.append(nombre)
    return archivos


def main() -> None:
    manifest = json.loads((CATALOGO / "pantallas.json").read_text(encoding="utf-8"))
    solo = set(sys.argv[1:])
    IMG.mkdir(parents=True, exist_ok=True)
    size = screen_size()
    for pantalla in manifest["pantallas"]:
        if solo and pantalla["id"] not in solo:
            continue
        for viejo in IMG.glob(f"{pantalla['id']}-*.jpg"):
            viejo.unlink()
        archivos = capturar(pantalla, manifest["paquete"], size)
        print(f"{pantalla['id']:5} {pantalla['nombre']}: {', '.join(archivos)}")
    print("Listo. Regenera el catálogo: python3 scripts/ui-catalogo/generar.py")


if __name__ == "__main__":
    main()
