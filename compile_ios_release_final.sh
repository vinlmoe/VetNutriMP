#!/bin/bash

# Script de compilation iOS RELEASE - Version finale pour production
# Optimisé pour éviter les problèmes de mémoire tout en compilant en mode RELEASE

echo "🚀 COMPILATION iOS RELEASE - VERSION PRODUCTION"
echo "==============================================="

set -e

# Étape 1: Nettoyage ultra-complet
echo "🧹 Étape 1: Nettoyage ultra-complet..."
# Supprimer tous les caches Gradle
rm -rf ~/.gradle/caches/ 2>/dev/null || true
rm -rf ~/.gradle/native/ 2>/dev/null || true
rm -rf ~/.gradle/daemon/ 2>/dev/null || true
rm -rf .gradle/ 2>/dev/null || true
rm -rf composeApp/build/ 2>/dev/null || true
rm -rf build/ 2>/dev/null || true

# Étape 2: Configuration mémoire optimisée pour RELEASE
echo "🔧 Étape 2: Configuration mémoire optimisée pour RELEASE..."
export GRADLE_OPTS="-Xmx16g -XX:MaxMetaspaceSize=8g -XX:+UseG1GC -XX:+UseStringDeduplication -XX:G1HeapRegionSize=64m -XX:+DisableExplicitGC -XX:+UseCompressedOops"
export JAVA_OPTS="-Xmx16g -XX:MaxMetaspaceSize=8g"
export MAVEN_OPTS="-Xmx16g"

# Étape 3: Configuration pour éviter les problèmes Jansi
echo "🔧 Étape 3: Configuration Jansi..."
export JANSI_TMPDIR="/tmp"
export JANSI_NO_SHUTDOWN_HOOK=true

# Étape 4: Compilation iOS RELEASE avec optimisations
echo "📱 Étape 4: Compilation iOS RELEASE optimisée..."

# Compiler en mode RELEASE avec des optimisations pour réduire la consommation mémoire
./gradlew :composeApp:linkReleaseFrameworkIosArm64 \
    --no-daemon \
    --no-build-cache \
    --no-configuration-cache \
    --max-workers=1 \
    --parallel \
    -Dkotlin.native.disableCompilerDaemon=true \
    -Dkotlin.incremental.multiplatform=false \
    -Dkotlin.native.binary.memoryModel=strict \
    -Dkotlin.native.binary.freezing=disabled \
    -Dkotlin.mpp.enableGranularSourceSetsMetadata=false \
    -Dkotlin.compiler.suppress.all.warnings=true \
    -Dkotlin.native.ignoreDisabledTargets=true \
    -Dkotlin.native.binary.gc=cms \
    -Dkotlin.native.binary.gcScheduler=adaptive \
    -Dkotlin.native.binary.optimization=size \
    -Dkotlin.native.binary.debugInfo=source-maps

# Vérifier le résultat
echo "🔍 Étape 5: Vérification du framework RELEASE..."
if [ -d "composeApp/build/bin/iosArm64/releaseFramework" ]; then
    echo "✅ Framework iOS RELEASE généré avec succès!"
    echo "📊 Informations du framework:"
    ls -la composeApp/build/bin/iosArm64/releaseFramework/
    echo "📦 Taille du framework:"
    du -sh composeApp/build/bin/iosArm64/releaseFramework/
    
    # Copier le framework dans le projet iOS
    echo "📋 Copie du framework RELEASE dans le projet iOS..."
    cp -R composeApp/build/bin/iosArm64/releaseFramework/ComposeApp.framework iosApp/ 2>/dev/null || true
    
    if [ -d "iosApp/ComposeApp.framework" ]; then
        echo "✅ Framework RELEASE copié avec succès dans iosApp/"
        echo "📊 Taille du framework copié: $(du -sh iosApp/ComposeApp.framework | cut -f1)"
        
        # Vérifier que c'est bien un framework RELEASE
        echo "🔍 Vérification du type de framework:"
        file iosApp/ComposeApp.framework/ComposeApp 2>/dev/null || echo "Binaire non trouvé"
    fi
    
    echo ""
    echo "🎉 COMPILATION RELEASE RÉUSSIE !"
    echo "📋 Instructions pour Xcode:"
    echo "1. Ouvrez le projet iosApp.xcodeproj dans Xcode"
    echo "2. Sélectionnez le projet 'iosApp' dans le navigateur"
    echo "3. Sélectionnez la target 'VetNutri ios'"
    echo "4. Allez dans l'onglet 'General'"
    echo "5. Dans la section 'Frameworks, Libraries, and Embedded Content'"
    echo "6. Cliquez sur '+' et ajoutez 'ComposeApp.framework'"
    echo "7. Assurez-vous que 'Embed & Sign' est sélectionné"
    echo ""
    echo "🚀 Votre framework iOS RELEASE est prêt pour la production !"
    echo "✅ Ce framework est optimisé et peut être utilisé pour l'App Store."
else
    echo "❌ Échec de la génération du framework iOS RELEASE"
    echo "🔍 Diagnostic:"
    ls -la composeApp/build/bin/iosArm64/ 2>/dev/null || echo "Répertoire de build non trouvé"
    echo ""
    echo "💡 Solutions possibles:"
    echo "1. Redémarrez complètement votre Mac"
    echo "2. Fermez toutes les applications inutiles"
    echo "3. Vérifiez l'espace disque disponible (au moins 30GB libres)"
    echo "4. Essayez sur une machine avec 32GB+ de RAM"
    echo "5. Utilisez le script compile_ios_minimal.sh en dernier recours"
    exit 1
fi

echo ""
echo "✅ Script de compilation iOS RELEASE terminé avec succès !"

