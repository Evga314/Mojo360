#!/usr/bin/env python3
"""
Готовые LTW + Mesa из официального nightly MojoLauncher -> app_pojavlauncher/libs/prebuilt-renderers.aar.
Зачем: в CI их качают из MojoLauncher/LTW и приватного mesa-unified2, локально их нет ->
в выборе рендерера только GL4ES.
Запуск: python scripts/fetch_prebuilt_renderers.py [путь_к_apk]   (без аргумента качает nightly)
"""
import io
import os
import sys
import urllib.request
import zipfile

NIGHTLY_URL = "https://github.com/MojoLauncher/MojoLauncher/releases/download/nightly/MojoLauncher-release.apk"
# ровно то, чего нет в локальной сборке (сверено diff'ом APK)
LIBS = ("libltw.so", "libEGL_mesa.so", "libgallium_dri.so", "libdrm.so", "libvulkan_freedreno.so")

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "app_pojavlauncher", "libs", "prebuilt-renderers.aar")

MANIFEST = """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="git.mojo.prebuilt.renderers" />
"""


def load_apk(arg):
    if arg:
        with open(arg, "rb") as f:
            return f.read()
    print("download", NIGHTLY_URL)
    with urllib.request.urlopen(NIGHTLY_URL) as r:
        return r.read()


def main():
    apk = zipfile.ZipFile(io.BytesIO(load_apk(sys.argv[1] if len(sys.argv) > 1 else None)))
    found = [n for n in apk.namelist() if n.startswith("lib/") and n.rsplit("/", 1)[-1] in LIBS]
    if not any(n.endswith("/libEGL_mesa.so") for n in found) or not any(n.endswith("/libltw.so") for n in found):
        sys.exit("в APK нет libltw.so / libEGL_mesa.so, ничего не пишу")

    # пустой classes.jar: AGP хочет его в любом aar
    jar = io.BytesIO()
    zipfile.ZipFile(jar, "w").close()

    with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED) as aar:
        aar.writestr("AndroidManifest.xml", MANIFEST)
        aar.writestr("classes.jar", jar.getvalue())
        for name in sorted(found):
            # lib/<abi>/x.so -> jni/<abi>/x.so
            aar.writestr("jni/" + name[len("lib/"):], apk.read(name))
            print("  +", name)
    print("ok ->", OUT)


if __name__ == "__main__":
    main()
