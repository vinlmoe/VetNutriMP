#!/bin/bash

# Script de compilation iOS ULTRA-MINIMAL
# Approche la plus simple possible pour éviter les problèmes de mémoire

echo "🧹 Nettoyage minimal..."
./gradlew clean

echo "🔧 Configuration mémoire ultra-minimale..."
export GRADLE_OPTS="-Xmx1g -XX:MaxMetaspaceSize=256m"
export JAVA_OPTS="-Xmx1g -XX:MaxMetaspaceSize=256m"

echo "📱 Compilation iOS avec configuration minimale..."

# Compiler seulement pour iOS ARM64 avec les paramètres les plus simples
./gradlew :composeApp:linkReleaseFrameworkIosArm64 \
    --no-daemon \
    --no-build-cache \
    --no-configuration-cache \
    --max-workers=1 \
    --parallel \
    -Dkotlin.native.ignoreDisabledTargets=true \
    -Dkotlin.native.binary.memoryModel=strict \
    -Dkotlin.native.binary.freezing=disabled

# Vérifier le résultat
if [ -f "composeApp/build/bin/iosArm64/releaseFramework/ComposeApp.framework/ComposeApp" ]; then
    echo ""
    echo "🎉 SUCCÈS ! Framework iOS généré !"
    echo ""
    ls -lh composeApp/build/bin/iosArm64/releaseFramework/ComposeApp.framework/ComposeApp
    echo ""
    echo "📦 Informations du binaire:"
    file composeApp/build/bin/iosArm64/releaseFramework/ComposeApp.framework/ComposeApp || echo "Binaire généré avec succès"
    echo ""
    echo "✅ Votre application iOS peut maintenant être intégrée dans Xcode !"
else
    echo ""
    echo "💥 ÉCHEC - Le binaire n'a pas été généré"
    echo ""
    echo "🔍 Diagnostic rapide:"
    ls -la composeApp/build/bin/iosArm64/releaseFramework/ComposeApp.framework/ || echo "Framework incomplet"
    echo ""
    echo "🚨 SOLUTION: Redémarrez complètement votre Mac et réessayez"
    exit 1
fi


