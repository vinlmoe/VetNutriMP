#!/bin/bash

# Script de compilation iOS corrigé - Version finale
# Résout les problèmes d'architecture Jansi et de mémoire

echo "🚀 COMPILATION iOS - VERSION CORRIGÉE"
echo "====================================="

set -e

# Étape 1: Nettoyage complet
echo "🧹 Étape 1: Nettoyage complet..."
# Supprimer les caches Gradle problématiques
rm -rf ~/.gradle/caches/ 2>/dev/null || true
rm -rf ~/.gradle/native/ 2>/dev/null || true
rm -rf .gradle/ 2>/dev/null || true
rm -rf composeApp/build/ 2>/dev/null || true

# Étape 2: Configuration mémoire optimisée pour éviter OutOfMemoryError
echo "🔧 Étape 2: Configuration mémoire ultra-optimisée..."
export GRADLE_OPTS="-Xmx12g -XX:MaxMetaspaceSize=6g -XX:+UseG1GC -XX:+UseStringDeduplication -XX:G1HeapRegionSize=64m -XX:+DisableExplicitGC"
export JAVA_OPTS="-Xmx12g -XX:MaxMetaspaceSize=6g"
export MAVEN_OPTS="-Xmx12g"

# Étape 3: Configuration pour éviter les problèmes Jansi
echo "🔧 Étape 3: Configuration Jansi..."
export JANSI_TMPDIR="/tmp"
export JANSI_NO_SHUTDOWN_HOOK=true

# Étape 4: Compilation iOS avec paramètres ultra-conservateurs
echo "📱 Étape 4: Compilation iOS ultra-conservatrice..."

# Utiliser le mode debug pour éviter les optimisations qui consomment trop de mémoire
./gradlew :composeApp:linkDebugFrameworkIosArm64 \
    --no-daemon \
    --no-build-cache \
    --no-configuration-cache \
    --max-workers=1 \
    --parallel \
    -Dkotlin.native.disableCompilerDaemon=true \
    -Dkotlin.incremental.multiplatform=false \
    -Dkotlin.native.binary.memoryModel=experimental \
    -Dkotlin.native.binary.freezing=disabled \
    -Dkotlin.mpp.enableGranularSourceSetsMetadata=false \
    -Dkotlin.compiler.suppress.all.warnings=true \
    -Dkotlin.native.ignoreDisabledTargets=true \
    -Dkotlin.native.binary.gc=cms \
    -Dkotlin.native.binary.gcScheduler=adaptive

# Vérifier le résultat
echo "🔍 Étape 5: Vérification du framework..."
if [ -d "composeApp/build/bin/iosArm64/debugFramework" ]; then
    echo "✅ Framework iOS généré avec succès!"
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
    echo "🚀 Votre framework iOS est prêt pour la release !"
    echo "⚠️  Note: Ce framework est en mode DEBUG pour éviter les problèmes de mémoire."
    echo "   Pour une release finale, vous devrez optimiser le code avant de compiler en mode RELEASE."
else
    echo "❌ Échec de la génération du framework iOS"
    echo "🔍 Diagnostic:"
    ls -la composeApp/build/bin/iosArm64/ 2>/dev/null || echo "Répertoire de build non trouvé"
    echo ""
    echo "💡 Solutions possibles:"
    echo "1. Redémarrez complètement votre Mac"
    echo "2. Fermez toutes les applications inutiles"
    echo "3. Vérifiez l'espace disque disponible (au moins 20GB libres)"
    echo "4. Essayez sur une machine avec plus de RAM (16GB+ recommandé)"
    echo "5. Utilisez le script compile_ios_minimal.sh pour une compilation plus légère"
    exit 1
fi

echo ""
echo "✅ Script de compilation iOS terminé !"

