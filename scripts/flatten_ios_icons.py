#!/usr/bin/env python3
"""
Retire le canal alpha des icônes iOS (aplatissement sur fond opaque).

Apple interdit le canal alpha sur TOUTES les icônes de l'appiconset (pas seulement
la marketing 1024x1024) : Xcode peut faire échouer l'archive/la validation App Store
Connect si une seule icône, quelle que soit sa taille, contient de la transparence.
Ce script traite l'ensemble des PNG du dossier AppIcon.appiconset.

Prérequis :
  - git lfs pull déjà exécuté (les PNG de l'appiconset sont versionnés en Git LFS ;
    sans ça ce script trouvera des pointeurs texte, pas de vraies images).
  - Pillow : pip install pillow

Usage :
  python3 scripts/flatten_ios_icons.py                # fond blanc par défaut
  python3 scripts/flatten_ios_icons.py "#0B5FFF"       # fond personnalisé (hex)
"""

import glob
import os
import sys

try:
    from PIL import Image
except ImportError:
    print("Pillow n'est pas installé. Installez-le avec : pip install pillow", file=sys.stderr)
    sys.exit(1)

APPICONSET = "iosApp/iosApp/Assets.xcassets/AppIcon.appiconset"


def hex_to_rgb(color: str) -> tuple[int, int, int]:
    color = color.lstrip("#")
    return tuple(int(color[i:i + 2], 16) for i in (0, 2, 4))


def main() -> int:
    background_arg = sys.argv[1] if len(sys.argv) > 1 else "white"
    background_rgb = hex_to_rgb(background_arg) if background_arg.startswith("#") else background_arg

    if not os.path.isdir(APPICONSET):
        print(f"Dossier introuvable : {APPICONSET}", file=sys.stderr)
        return 1

    flattened = already_opaque = skipped_lfs = errors = 0

    for path in sorted(glob.glob(os.path.join(APPICONSET, "*.png"))):
        try:
            with open(path, "rb") as f:
                head = f.read(7)
            if head == b"version":
                print(f"⚠ {os.path.basename(path)} est un pointeur Git LFS non résolu — exécutez 'git lfs pull' d'abord")
                skipped_lfs += 1
                continue

            img = Image.open(path)
            img.load()

            has_alpha = img.mode in ("RGBA", "LA") or (img.mode == "P" and "transparency" in img.info)
            if has_alpha:
                rgba = img.convert("RGBA")
                canvas = Image.new("RGB", rgba.size, background_rgb)
                canvas.paste(rgba, mask=rgba.split()[3])
                canvas.save(path, "PNG")
                print(f"✓ Canal alpha retiré : {os.path.basename(path)}")
                flattened += 1
            else:
                already_opaque += 1
        except Exception as e:
            print(f"✗ Erreur sur {path} : {e}", file=sys.stderr)
            errors += 1

    print()
    print(f"Aplaties : {flattened} — déjà opaques : {already_opaque} — "
          f"ignorées (LFS non résolu) : {skipped_lfs} — erreurs : {errors}")

    if skipped_lfs:
        print("Exécutez 'git lfs pull' puis relancez ce script pour traiter les fichiers ignorés.")

    return 1 if errors else 0


if __name__ == "__main__":
    raise SystemExit(main())
