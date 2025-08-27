#!/bin/bash

# Script de génération automatique des icônes pour Kotlin Multiplatform
# Nécessite ImageMagick et iconutil (macOS)

set -e

# Couleurs pour les messages
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Vérification des dépendances
check_dependencies() {
    echo -e "${YELLOW}Vérification des dépendances...${NC}"
    
    if ! command -v convert &> /dev/null; then
        echo -e "${RED}ImageMagick n'est pas installé. Installez-le avec :${NC}"
        echo "brew install imagemagick"
        exit 1
    fi
    
    if [[ "$OSTYPE" == "darwin"* ]]; then
        if ! command -v iconutil &> /dev/null; then
            echo -e "${RED}iconutil n'est pas disponible (macOS requis)${NC}"
            exit 1
        fi
    fi
    
    echo -e "${GREEN}✓ Toutes les dépendances sont installées${NC}"
}

# Génération des icônes
generate_icons() {
    local source_image="$1"
    local output_dir="composeApp/src/desktopMain/resources"
    
    if [[ ! -f "$source_image" ]]; then
        echo -e "${RED}Image source non trouvée : $source_image${NC}"
        echo "Usage: $0 <chemin_vers_image_source>"
        exit 1
    fi
    
    echo -e "${YELLOW}Génération des icônes à partir de : $source_image${NC}"
    
    # Création du dossier de sortie
    mkdir -p "$output_dir"
    
    # Icône PNG pour Linux
    echo -e "${YELLOW}Génération de l'icône PNG pour Linux...${NC}"
    convert "$source_image" -resize 512x512 "$output_dir/icon.png"
    echo -e "${GREEN}✓ Icône PNG générée : $output_dir/icon.png${NC}"
    
    # Icône ICO pour Windows
    echo -e "${YELLOW}Génération de l'icône ICO pour Windows...${NC}"
    convert "$source_image" -resize 256x256 "$output_dir/icon.ico"
    echo -e "${GREEN}✓ Icône ICO générée : $output_dir/icon.ico${NC}"
    
    # Icône ICNS pour macOS
    if [[ "$OSTYPE" == "darwin"* ]]; then
        echo -e "${YELLOW}Génération de l'icône ICNS pour macOS...${NC}"
        
        # Création du dossier temporaire pour les icônes
        local temp_iconset="$output_dir/icon.iconset"
        mkdir -p "$temp_iconset"
        
        # Génération des différentes tailles
        convert "$source_image" -resize 16x16 "$temp_iconset/icon_16x16.png"
        convert "$source_image" -resize 32x32 "$temp_iconset/icon_16x16@2x.png"
        convert "$source_image" -resize 32x32 "$temp_iconset/icon_32x32.png"
        convert "$source_image" -resize 64x64 "$temp_iconset/icon_32x32@2x.png"
        convert "$source_image" -resize 128x128 "$temp_iconset/icon_128x128.png"
        convert "$source_image" -resize 256x256 "$temp_iconset/icon_128x128@2x.png"
        convert "$source_image" -resize 256x256 "$temp_iconset/icon_256x256.png"
        convert "$source_image" -resize 512x512 "$temp_iconset/icon_256x256@2x.png"
        convert "$source_image" -resize 512x512 "$temp_iconset/icon_512x512.png"
        convert "$source_image" -resize 1024x1024 "$temp_iconset/icon_512x512@2x.png"
        
        # Création du fichier ICNS
        iconutil -c icns "$temp_iconset" -o "$output_dir/icon.icns"
        
        # Nettoyage
        rm -rf "$temp_iconset"
        
        echo -e "${GREEN}✓ Icône ICNS générée : $output_dir/icon.icns${NC}"
    else
        echo -e "${YELLOW}⚠ Icône ICNS non générée (macOS requis)${NC}"
    fi
    
    echo -e "${GREEN}✓ Génération des icônes terminée !${NC}"
}

# Fonction principale
main() {
    echo -e "${GREEN}=== Générateur d'icônes Kotlin Multiplatform ===${NC}"
    
    check_dependencies
    generate_icons "$1"
    
    echo -e "${GREEN}=== Icônes générées avec succès ! ===${NC}"
    echo "Vous pouvez maintenant reconstruire votre projet :"
    echo "  ./gradlew :composeApp:build"
}

# Vérification des arguments
if [[ $# -eq 0 ]]; then
    echo -e "${RED}Erreur : Image source manquante${NC}"
    echo "Usage: $0 <chemin_vers_image_source>"
    echo ""
    echo "Exemple :"
    echo "  $0 assets/logo.png"
    echo "  $0 ~/Desktop/mon_icone.jpg"
    exit 1
fi

main "$1"
