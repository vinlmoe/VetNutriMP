#!/bin/bash

# Script de test pour vérifier la configuration des icônes

set -e

# Couleurs pour les messages
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Test de la configuration des icônes VetNutri MP ===${NC}"

# Vérification de la structure des dossiers
check_structure() {
    echo -e "${YELLOW}Vérification de la structure des dossiers...${NC}"
    
    local errors=0
    
    # Android
    if [[ -d "composeApp/src/androidMain/res" ]]; then
        echo -e "${GREEN}✓ Dossier Android trouvé${NC}"
        
        # Vérifier les icônes Android
        if [[ -f "composeApp/src/androidMain/res/mipmap-hdpi/ic_launcher.png" ]]; then
            echo -e "${GREEN}  ✓ Icône Android principale trouvée${NC}"
        else
            echo -e "${RED}  ✗ Icône Android principale manquante${NC}"
            ((errors++))
        fi
        
        if [[ -f "composeApp/src/androidMain/AndroidManifest.xml" ]]; then
            echo -e "${GREEN}  ✓ AndroidManifest.xml trouvé${NC}"
        else
            echo -e "${RED}  ✗ AndroidManifest.xml manquant${NC}"
            ((errors++))
        fi
    else
        echo -e "${RED}✗ Dossier Android manquant${NC}"
        ((errors++))
    fi
    
    # iOS
    if [[ -d "iosApp/iosApp/Assets.xcassets/AppIcon.appiconset" ]]; then
        echo -e "${GREEN}✓ Dossier iOS trouvé${NC}"
        
        if [[ -f "iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png" ]]; then
            echo -e "${GREEN}  ✓ Icône iOS trouvée${NC}"
        else
            echo -e "${RED}  ✗ Icône iOS manquante${NC}"
            ((errors++))
        fi
    else
        echo -e "${RED}✗ Dossier iOS manquant${NC}"
        ((errors++))
    fi
    
    # Desktop
    if [[ -d "composeApp/src/desktopMain/resources" ]]; then
        echo -e "${GREEN}✓ Dossier Desktop trouvé${NC}"
        
        local desktop_icons=0
        if [[ -f "composeApp/src/desktopMain/resources/icon.icns" ]]; then
            echo -e "${GREEN}  ✓ Icône macOS (.icns) trouvée${NC}"
            ((desktop_icons++))
        fi
        
        if [[ -f "composeApp/src/desktopMain/resources/icon.ico" ]]; then
            echo -e "${GREEN}  ✓ Icône Windows (.ico) trouvée${NC}"
            ((desktop_icons++))
        fi
        
        if [[ -f "composeApp/src/desktopMain/resources/icon.png" ]]; then
            echo -e "${GREEN}  ✓ Icône Linux (.png) trouvée${NC}"
            ((desktop_icons++))
        fi
        
        if [[ $desktop_icons -eq 0 ]]; then
            echo -e "${YELLOW}  ⚠ Aucune icône desktop trouvée${NC}"
        fi
    else
        echo -e "${RED}✗ Dossier Desktop manquant${NC}"
        ((errors++))
    fi
    
    return $errors
}

# Vérification de la configuration Gradle
check_gradle_config() {
    echo -e "${YELLOW}Vérification de la configuration Gradle...${NC}"
    
    if grep -q "iconFile.set" "composeApp/build.gradle.kts"; then
        echo -e "${GREEN}✓ Configuration des icônes desktop trouvée dans build.gradle.kts${NC}"
    else
        echo -e "${RED}✗ Configuration des icônes desktop manquante dans build.gradle.kts${NC}"
        return 1
    fi
}

# Test de build
test_build() {
    echo -e "${YELLOW}Test de build...${NC}"
    
    if ./gradlew :composeApp:assembleDebug --dry-run &> /dev/null; then
        echo -e "${GREEN}✓ Configuration Gradle valide${NC}"
    else
        echo -e "${RED}✗ Erreur dans la configuration Gradle${NC}"
        return 1
    fi
}

# Vérification des outils
check_tools() {
    echo -e "${YELLOW}Vérification des outils...${NC}"
    
    local tools_available=0
    
    if command -v convert &> /dev/null; then
        echo -e "${GREEN}✓ ImageMagick disponible${NC}"
        ((tools_available++))
    else
        echo -e "${YELLOW}⚠ ImageMagick non disponible (brew install imagemagick)${NC}"
    fi
    
    if command -v iconutil &> /dev/null; then
        echo -e "${GREEN}✓ iconutil disponible (macOS)${NC}"
        ((tools_available++))
    else
        echo -e "${YELLOW}⚠ iconutil non disponible (macOS requis)${NC}"
    fi
    
    if [[ $tools_available -gt 0 ]]; then
        echo -e "${GREEN}✓ Outils de génération d'icônes disponibles${NC}"
    else
        echo -e "${RED}✗ Aucun outil de génération d'icônes disponible${NC}"
    fi
}

# Fonction principale
main() {
    local total_errors=0
    
    check_structure
    total_errors=$((total_errors + $?))
    
    check_gradle_config
    total_errors=$((total_errors + $?))
    
    test_build
    total_errors=$((total_errors + $?))
    
    check_tools
    
    echo ""
    echo -e "${BLUE}=== Résumé du test ===${NC}"
    
    if [[ $total_errors -eq 0 ]]; then
        echo -e "${GREEN}✓ Configuration des icônes valide !${NC}"
        echo "Votre application devrait afficher correctement ses icônes sur toutes les plateformes."
    else
        echo -e "${RED}✗ $total_errors erreur(s) détectée(s)${NC}"
        echo "Consultez la documentation dans docs/configuration_icones.md pour résoudre ces problèmes."
    fi
    
    echo ""
    echo -e "${BLUE}Prochaines étapes :${NC}"
    echo "1. Créez vos icônes personnalisées"
    echo "2. Utilisez le script : ./scripts/generate_icons.sh assets/votre_icone.png"
    echo "3. Testez sur chaque plateforme"
}

main
