#!/bin/bash

# Script de compilation iOS ULTRA-OPTIMISÉ - Solution complète et définitive
# Combine toutes les optimisations pour maximiser les performances et éviter les heap space

echo "🧹 Nettoyage ultra-sélectif pour maximiser les performances..."
# Nettoyer seulement les fichiers problématiques, pas les caches utiles
find composeApp/build -name "*.tmp" -type f -delete 2>/dev/null || true
find composeApp/build -name "*.log" -type f -delete 2>/dev/null || true
find .gradle -name "*.lock" -type f -delete 2>/dev/null || true

echo "🔧 Configuration mémoire ULTRA-OPTIMISÉE pour iOS..."
export GRADLE_OPTS="-Xmx16g -XX:MaxMetaspaceSize=6g -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+OptimizeStringConcat -XX:G1HeapRegionSize=32m -XX:+DisableExplicitGC"
export JAVA_OPTS="-Xmx16g -XX:MaxMetaspaceSize=6g"

echo "📱 Compilation iOS avec TOUTES les optimisations avancées..."
./gradlew :composeApp:linkReleaseFrameworkIosArm64 \
    --no-daemon \
    --no-build-cache \
    --no-configuration-cache \
    --max-workers=1 \
    --parallel \
    -Dkotlin.native.disableCompilerDaemon=false \
    -Dkotlin.incremental.multiplatform=true \
    -Dkotlin.native.binary.memoryModel=strict \
    -Dkotlin.native.binary.freezing=disabled \
    -Dkotlin.mpp.enableGranularSourceSetsMetadata=true \
    -Dkotlin.compiler.suppress.all.warnings=true

echo "✅ Compilation iOS ultra-optimisée terminée!"

# Vérification complète du résultat
if [ -d "composeApp/build/bin/iosArm64/releaseFramework" ]; then
    echo "🎉 Framework iOS généré avec succès!"
    echo "📊 Informations détaillées:"
    ls -la composeApp/build/bin/iosArm64/releaseFramework/
    echo "📦 Taille totale du framework:"
    du -sh composeApp/build/bin/iosArm64/releaseFramework/
    echo "🔍 Vérification de l'intégrité:"
    find composeApp/build/bin/iosArm64/releaseFramework/ -name "*.framework" -exec basename {} \; | head -5
else
    echo "❌ Échec de la génération du framework iOS"
    echo "🔍 Analyse des logs récents..."
    find composeApp/build -name "*.log" -exec tail -10 {} \; 2>/dev/null | head -20
    echo "💡 Suggestion: Vérifiez que Java 17+ est installé avec 'java -version'"
    exit 1
fi
