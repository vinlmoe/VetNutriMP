#!/bin/bash

# Retire le canal alpha des icônes App Store iOS (aplatissement sur fond opaque).
# L'App Store Connect rejette toute icône marketing (1024x1024) contenant un canal alpha.
#
# Prérequis :
#   - git-lfs installé et `git lfs pull` déjà exécuté (les PNG de l'appiconset sont versionnés
#     en Git LFS ; sans ça ce script trouvera des pointeurs texte, pas de vraies images).
#   - ImageMagick (`convert`, `identify`).
#
# Usage :
#   ./scripts/flatten_ios_icons.sh [couleur_de_fond]
#   ./scripts/flatten_ios_icons.sh            # fond blanc par défaut
#   ./scripts/flatten_ios_icons.sh "#0B5FFF"  # fond personnalisé

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

APPICONSET="iosApp/iosApp/Assets.xcassets/AppIcon.appiconset"
BACKGROUND="${1:-white}"

if ! command -v convert &> /dev/null; then
    echo -e "${RED}ImageMagick n'est pas installé (brew install imagemagick / apt install imagemagick).${NC}"
    exit 1
fi

if [[ ! -d "$APPICONSET" ]]; then
    echo -e "${RED}Dossier introuvable : $APPICONSET${NC}"
    exit 1
fi

flattened=0
skipped_lfs=0
already_opaque=0

for png in "$APPICONSET"/*.png; do
    [[ -f "$png" ]] || continue

    if head -c 7 "$png" | grep -q "version"; then
        echo -e "${YELLOW}⚠ $(basename "$png") est un pointeur Git LFS non résolu — exécutez 'git lfs pull' d'abord${NC}"
        skipped_lfs=$((skipped_lfs + 1))
        continue
    fi

    alpha=$(identify -format '%A' "$png" 2>/dev/null || echo "Unknown")
    if [[ "$alpha" == "True" ]]; then
        convert "$png" -background "$BACKGROUND" -alpha remove -alpha off "$png"
        echo -e "${GREEN}✓ Canal alpha retiré : $(basename "$png")${NC}"
        flattened=$((flattened + 1))
    else
        already_opaque=$((already_opaque + 1))
    fi
done

echo ""
echo "Aplaties : $flattened — déjà opaques : $already_opaque — ignorées (LFS non résolu) : $skipped_lfs"

if [[ $skipped_lfs -gt 0 ]]; then
    echo -e "${YELLOW}Exécutez 'git lfs pull' puis relancez ce script pour traiter les fichiers ignorés.${NC}"
fi
