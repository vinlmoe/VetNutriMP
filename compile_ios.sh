#!/bin/bash

# Script de compilation iOS ultra-optimisé avec gestion intelligente des ressources
# Évite les problèmes de permissions et optimise la mémoire

echo "🧹 Nettoyage sélectif des builds problématiques..."
# Éviter la suppression des caches Kotlin Native qui causent des problèmes de permissions
find composeApp/build -name "*.klib" -type f -delete 2>/dev/null || true
find composeApp/build -name "*.bc" -type f -delete 2>/dev/null || true

echo "🔧 Configuration mémoire ultra-optimisée pour iOS..."
export GRADLE_OPTS="-Xmx10g -XX:MaxMetaspaceSize=3g -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+OptimizeStringConcat"
export JAVA_OPTS="-Xmx10g -XX:MaxMetaspaceSize=3g"

echo "📱 Compilation iOS avec optimisations avancées..."
./gradlew :composeApp:linkReleaseFrameworkIosArm64 \
    --no-daemon \
    --no-build-cache \
    --max-workers=2 \
    --parallel \
    -Dkotlin.native.disableCompilerDaemon=false \
    -Dkotlin.incremental.multiplatform=true \
    -Dkotlin.native.binary.memoryModel=strict \
    -Dkotlin.native.binary.freezing=disabled \
    -Dkotlin.mpp.enableGranularSourceSetsMetadata=true

echo "✅ Compilation iOS terminée!"

# Vérification du résultat avec gestion d'erreur améliorée
if [ -d "composeApp/build/bin/iosArm64/releaseFramework" ]; then
    echo "🎉 Framework iOS généré avec succès!"
    ls -la composeApp/build/bin/iosArm64/releaseFramework/ || echo "⚠️ Impossible de lister le contenu"
    echo "📦 Informations du framework:"
    du -sh composeApp/build/bin/iosArm64/releaseFramework/ 2>/dev/null || echo "⚠️ Impossible de calculer la taille"
else
    echo "❌ Échec de la génération du framework iOS"
    echo "🔍 Vérification des logs de build..."
    find composeApp/build -name "*.log" -exec tail -20 {} \; 2>/dev/null || echo "⚠️ Aucun log trouvé"
    exit 1
fi
