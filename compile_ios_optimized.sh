#!/bin/bash

# Script de compilation iOS optimisé - Configuration progressive
# Évite les problèmes de heap space avec une approche incrémentale

echo "🚀 COMPILATION iOS OPTIMISÉE - APPROCHE PROGRESSIVE"
echo "=================================================="

set -e

# Détection de l'environnement
MEMORY_GB=$(echo "$(sysctl -n hw.memsize) / 1024 / 1024 / 1024" | bc 2>/dev/null || echo "8")
CPU_CORES=$(sysctl -n hw.ncpu 2>/dev/null || echo "4")

echo "📊 Environnement détecté:"
echo "   - Mémoire: ${MEMORY_GB}GB"
echo "   - CPU: ${CPU_CORES} cœurs"

# Configuration adaptative basée sur la mémoire
if [ "$MEMORY_GB" -ge 16 ]; then
    GRADLE_OPTS="-Xmx8g -XX:+UseG1GC -XX:MaxMetaspaceSize=3g"
    MAX_WORKERS=3
    echo "🟢 Configuration haute performance"
elif [ "$MEMORY_GB" -ge 8 ]; then
    GRADLE_OPTS="-Xmx6g -XX:+UseG1GC -XX:MaxMetaspaceSize=2g"
    MAX_WORKERS=2
    echo "🟡 Configuration standard"
else
    GRADLE_OPTS="-Xmx4g -XX:+UseG1GC -XX:MaxMetaspaceSize=1g"
    MAX_WORKERS=1
    echo "🔴 Configuration mémoire réduite"
fi

export GRADLE_OPTS="$GRADLE_OPTS -Dfile.encoding=UTF-8"
export JAVA_OPTS="$GRADLE_OPTS"
export JANSI_TMPDIR="/tmp"

# Étape 1: Nettoyage sélectif
echo "🧹 Étape 1: Nettoyage sélectif..."
rm -rf composeApp/build/bin/ios* 2>/dev/null || true
rm -rf iosApp/ComposeApp.framework/ 2>/dev/null || true

# Étape 2: Compilation Kotlin optimisée
echo "📱 Étape 2: Compilation Kotlin optimisée..."
echo "   Workers: $MAX_WORKERS"
echo "   Gradle: $GRADLE_OPTS"

./gradlew :composeApp:compileKotlinIosArm64 \
    --no-daemon \
    --no-build-cache \
    --no-configuration-cache \
    --max-workers=$MAX_WORKERS \
    --no-parallel \
    -Dkotlin.native.disableCompilerDaemon=true \
    -Dkotlin.incremental=false \
    -Dkotlin.native.binary.memoryModel=experimental \
    -Dkotlin.native.binary.freezing=disabled \
    -Dkotlin.native.binary.optimization=size \
    -Dkotlin.mpp.enableGranularSourceSetsMetadata=false \
    -Dkotlin.compiler.suppress.all.warnings=true \
    -Dkotlin.native.ignoreDisabledTargets=true \
    -Dkotlin.native.binary.objcExportSuspendFunctionLaunchThreadRestriction=none

if [ $? -ne 0 ]; then
    echo "❌ Échec compilation Kotlin, tentative avec configuration minimale..."

    # Tentative avec configuration ultra-conservative
    ./gradlew :composeApp:compileKotlinIosArm64 \
        --no-daemon \
        --no-build-cache \
        --no-configuration-cache \
        --max-workers=1 \
        --no-parallel \
        -Dkotlin.native.disableCompilerDaemon=true \
        -Dkotlin.incremental=false \
        -Dkotlin.native.binary.memoryModel=experimental \
        -Dkotlin.native.binary.freezing=disabled \
        -Dkotlin.native.binary.optimization=no \
        -Dkotlin.native.binary.debugInfo=no \
        -Dkotlin.mpp.enableGranularSourceSetsMetadata=false \
        -Dkotlin.compiler.suppress.all.warnings=true
fi

# Étape 3: Génération du framework optimisée
echo "🔗 Étape 3: Génération du framework..."
./gradlew :composeApp:linkDebugFrameworkIosArm64 \
    --no-daemon \
    --no-build-cache \
    --max-workers=1 \
    --no-parallel \
    -Dkotlin.native.disableCompilerDaemon=true \
    -Dkotlin.native.binary.memoryModel=experimental \
    -Dkotlin.native.binary.freezing=enabled \
    -Dkotlin.native.binary.optimization=size

# Étape 4: Vérification et copie
echo "🔍 Étape 4: Vérification et déploiement..."

if [ -d "composeApp/build/bin/iosArm64/debugFramework" ]; then
    echo "✅ Framework généré avec succès!"

    # Informations sur le framework
    echo "📊 Informations du framework:"
    ls -la composeApp/build/bin/iosArm64/debugFramework/
    echo "📦 Taille du framework:"
    du -sh composeApp/build/bin/iosArm64/debugFramework/

    # Copie dans le projet iOS
    echo "📋 Déploiement vers le projet iOS..."
    cp -R composeApp/build/bin/iosArm64/debugFramework/ComposeApp.framework iosApp/ 2>/dev/null || true

    # Vérification finale
    if [ -d "iosApp/ComposeApp.framework" ]; then
        echo "✅ Framework déployé avec succès dans iosApp/"
        echo ""
        echo "🎉 COMPILATION OPTIMISÉE RÉUSSIE !"
        echo ""
        echo "📋 Résumé:"
        echo "   - Mémoire système: ${MEMORY_GB}GB"
        echo "   - Configuration: $GRADLE_OPTS"
        echo "   - Workers: $MAX_WORKERS"
        echo ""
        echo "🚀 Le framework est prêt pour être utilisé dans Xcode."
    else
        echo "⚠️  Framework généré mais échec de la copie dans iosApp/"
        echo "   Vérifiez les permissions et l'espace disque."
    fi

elif [ -d "composeApp/build/bin/iosArm64/releaseFramework" ]; then
    echo "✅ Framework release trouvé (au lieu de debug)"

    # Copie du framework release
    cp -R composeApp/build/bin/iosArm64/releaseFramework/ComposeApp.framework iosApp/ 2>/dev/null || true

    echo "✅ Framework release déployé avec succès!"

else
    echo "❌ Échec de la génération du framework"
    echo ""
    echo "🔧 SOLUTIONS DE DÉPANNAGE:"
    echo "1. Vérifiez l'espace disque disponible:"
    df -h
    echo ""
    echo "2. Redémarrez complètement votre Mac"
    echo "3. Fermez toutes les applications inutiles"
    echo "4. Augmentez la mémoire swap:"
    echo "   sudo mkdir /Volumes/SWAP"
    echo "   sudo dd if=/dev/zero of=/Volumes/SWAP/swapfile bs=1m count=8192"
    echo ""
    echo "5. Essayez avec une machine plus puissante (16GB+ RAM)"
    echo ""
    echo "6. Contactez le support technique"
    exit 1
fi

echo ""
echo "📈 RECOMMANDATIONS DE PERFORMANCE:"
if [ "$MEMORY_GB" -lt 16 ]; then
    echo "   - Considérez l'archivage des anciennes données"
    echo "   - Utilisez les versions 'Light' des repositories"
    echo "   - Activez le cache LRU dans les repositories"
fi

echo ""
echo "✅ Script de compilation optimisée terminé !"
