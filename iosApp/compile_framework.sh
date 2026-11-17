#!/bin/bash

# Script de compilation simple pour iOS
# Ce script génère un framework pour iOS

if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
  exit 0
fi

echo "🔄 Compilation du framework ComposeApp pour iOS..."

# Se déplacer vers le répertoire racine du projet
if [ -n "$SRCROOT" ]; then
    cd "$SRCROOT/.."
else
    cd "$(dirname "$0")/.."
fi

# Générer le framework pour iOS en mode DEBUG pour éviter les optimisations
echo "📦 Génération du framework iOS en mode DEBUG..."
./gradlew :composeApp:linkDebugFrameworkIosFat -no-daemon --max-workers=1

# Vérifier que le framework a été généré
if [ -d "composeApp/build/fat-framework/debug/ComposeApp.framework" ]; then
    echo "✅ Framework généré avec succès"
    
    # Copier le framework dans le projet iOS
    echo "📋 Copie du framework dans le projet iOS..."
    cp -R composeApp/build/fat-framework/debug/ComposeApp.framework iosApp/
    
    # Vérifier la copie
    if [ -d "iosApp/ComposeApp.framework" ]; then
        echo "✅ Framework copié avec succès dans iosApp/"
        echo "📊 Taille du framework: $(du -sh iosApp/ComposeApp.framework | cut -f1)"
        echo "🎯 Framework prêt pour Xcode !"
    else
        echo "❌ Erreur: Échec de la copie du framework"
        exit 1
    fi
else
    echo "❌ Erreur: Framework non généré"
    exit 1
fi

echo ""
echo "📋 Instructions pour Xcode:"
echo "1. Ouvrez le projet iosApp.xcodeproj dans Xcode"
echo "2. Sélectionnez le projet 'iosApp' dans le navigateur"
echo "3. Sélectionnez la target 'VetNutri ios'"
echo "4. Allez dans l'onglet 'General'"
echo "5. Dans la section 'Frameworks, Libraries, and Embedded Content'"
echo "6. Cliquez sur '+' et ajoutez 'ComposeApp.framework'"
echo "7. Assurez-vous que 'Embed & Sign' est sélectionné"
echo ""
echo "🚀 Framework prêt !"