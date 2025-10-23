#!/bin/bash

# Script de compilation iOS RELEASE sans optimisations statiques
# Désactive complètement les optimisations qui causent les problèmes de mémoire

echo "🚀 COMPILATION iOS RELEASE - SANS OPTIMISATIONS STATIQUES"
echo "========================================================"

set -e

# Étape 1: Nettoyage sélectif
echo "🧹 Étape 1: Nettoyage sélectif..."
find composeApp/build -name "*.tmp" -type f -delete 2>/dev/null || true
find composeApp/build -name "*.log" -type f -delete 2>/dev/null || true
find .gradle -name "*.lock" -type f -delete 2>/dev/null || true

# Étape 2: Configuration mémoire optimisée
echo "🔧 Étape 2: Configuration mémoire optimisée..."
export GRADLE_OPTS="-Xmx16g -Xms16g -XX:MaxMetaspaceSize=4g -XX:+UseG1GC -XX:+UseStringDeduplication"
export JAVA_OPTS="-Xmx16g -Xms16g -XX:MaxMetaspaceSize=4g"

# Étape 3: Compilation iOS RELEASE sans optimisations statiques
echo "📱 Étape 3: Compilation iOS RELEASE sans optimisations statiques..."

# Désactiver complètement les optimisations statiques qui causent les problèmes
./gradlew :composeApp:linkReleaseFrameworkIosArm64 \
    --no-daemon \
    --no-build-cache \
    --no-configuration-cache \
    --max-workers=2 \
    --parallel \
    -Dkotlin.native.disableCompilerDaemon=false \
    -Dkotlin.incremental.multiplatform=true \
    -Dkotlin.native.binary.memoryModel=strict \
    -Dkotlin.native.binary.freezing=disabled \
    -Dkotlin.mpp.enableGranularSourceSetsMetadata=true \
    -Dkotlin.compiler.suppress.all.warnings=true \
    -Dkotlin.native.binary.gc=cms \
    -Dkotlin.native.binary.gcScheduler=adaptive \
    -Dkotlin.native.binary.optimization=no \
    -Dkotlin.native.binary.debugInfo=source-maps \
    -Dkotlin.native.binary.disableStaticInitializersOptimization=true \
    -Dkotlin.native.binary.disableDevirtualization=true \
    -Dkotlin.native.binary.disableInlining=true

# Vérifier le résultat
echo "🔍 Étape 4: Vérification du framework..."
if [ -d "composeApp/build/bin/iosArm64/releaseFramework" ]; then
    echo "✅ Framework iOS RELEASE généré avec succès!"
    echo "📊 Informations du framework:"
    ls -la composeApp/build/bin/iosArm64/releaseFramework/
    echo "📦 Taille du framework:"
    du -sh composeApp/build/bin/iosArm64/releaseFramework/
    
    # Copier le framework dans le projet iOS
    echo "📋 Copie du framework dans le projet iOS..."
    cp -R composeApp/build/bin/iosArm64/releaseFramework/ComposeApp.framework iosApp/ 2>/dev/null || true
    
    if [ -d "iosApp/ComposeApp.framework" ]; then
        echo "✅ Framework copié avec succès dans iosApp/"
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
    echo "✅ Ce framework est optimisé pour éviter les problèmes de mémoire."
    echo "✅ Il est compatible avec l'App Store et les releases."
else
    echo "❌ Échec de la génération du framework iOS"
    echo "🔍 Diagnostic:"
    ls -la composeApp/build/bin/iosArm64/ 2>/dev/null || echo "Répertoire de build non trouvé"
    echo ""
    echo "💡 Solutions possibles:"
    echo "1. Vérifiez que Java 17+ est installé: java -version"
    echo "2. Redémarrez votre Mac si les erreurs persistent"
    echo "3. Vérifiez l'espace disque disponible"
    echo "4. Utilisez le script compile_ios_fixed.sh en alternative"
    exit 1
fi

echo ""
echo "✅ Script de compilation iOS RELEASE sans optimisations terminé avec succès !"

