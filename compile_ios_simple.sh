#!/bin/bash

# Script de compilation iOS simple - DEBUG seulement
# Compile le framework DEBUG qui fonctionne et le copie

echo "🚀 COMPILATION iOS SIMPLE - DEBUG SEULEMENT"
echo "=========================================="

set -e

# Étape 1: Nettoyage minimal
echo "🧹 Étape 1: Nettoyage minimal..."
find composeApp/build -name "*.tmp" -type f -delete 2>/dev/null || true

# Étape 2: Configuration mémoire standard
echo "🔧 Étape 2: Configuration mémoire standard..."
export GRADLE_OPTS="-Xmx4g -XX:MaxMetaspaceSize=2g"
export JAVA_OPTS="-Xmx4g -XX:MaxMetaspaceSize=2g"

# Étape 3: Compilation iOS DEBUG simple
echo "📱 Étape 3: Compilation iOS DEBUG simple..."

# Compiler seulement le framework DEBUG
./gradlew :composeApp:linkDebugFrameworkIosArm64 \
    --no-daemon \
    --max-workers=1

# Vérifier le résultat
echo "🔍 Étape 4: Vérification du framework DEBUG..."
if [ -d "composeApp/build/bin/iosArm64/debugFramework" ]; then
    echo "✅ Framework iOS DEBUG généré avec succès!"
    echo "📊 Informations du framework:"
    ls -la composeApp/build/bin/iosArm64/debugFramework/
    echo "📦 Taille du framework:"
    du -sh composeApp/build/bin/iosArm64/debugFramework/
    
    # Copier le framework dans le projet iOS
    echo "📋 Copie du framework dans le projet iOS..."
    cp -R composeApp/build/bin/iosArm64/debugFramework/ComposeApp.framework iosApp/ 2>/dev/null || true
    
    if [ -d "iosApp/ComposeApp.framework" ]; then
        echo "✅ Framework copié avec succès dans iosApp/"
        echo "📊 Taille du framework copié: $(du -sh iosApp/ComposeApp.framework | cut -f1)"
    fi
    
    echo ""
    echo "🎉 COMPILATION RÉUSSIE !"
    echo "📋 Instructions pour Xcode:"
    echo "1. Ouvrez le projet iosApp.xcodeproj dans Xcode"
    echo "2. Sélectionnez le projet 'iosApp' dans le navigateur"
    echo "3. Sélectionnez la target 'VetNutri ios'"
    echo "4. Allez dans l'onglet 'General'"
    echo "5. Dans la section 'Frameworks, Libraries, and Embedded Content'"
    echo "6. Cliquez sur '+' et ajoutez 'ComposeApp.framework'"
    echo "7. Assurez-vous que 'Embed & Sign' est sélectionné"
    echo ""
    echo "🚀 Votre framework iOS DEBUG est prêt !"
    echo "⚠️  Note: Ce framework est en mode DEBUG."
    echo "   Il fonctionne mais n'est pas optimisé pour la production."
else
    echo "❌ Échec de la génération du framework iOS DEBUG"
    echo "🔍 Diagnostic:"
    ls -la composeApp/build/bin/iosArm64/ 2>/dev/null || echo "Répertoire de build non trouvé"
    exit 1
fi

echo ""
echo "✅ Script de compilation iOS simple terminé !"

