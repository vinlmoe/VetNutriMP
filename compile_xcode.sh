#!/bin/bash

# Script de compilation XCODE - Configuration ultra-conservatrice
# Résout spécifiquement les problèmes de heap space dans Xcode

echo "🧹 Nettoyage complet pour Xcode..."
./gradlew clean

echo "🗑️ Suppression des caches problématiques..."
rm -rf ~/.gradle/caches/
rm -rf .gradle/
rm -rf composeApp/build/
rm -rf ~/.konan/

echo "🔧 Configuration mémoire minimale pour Xcode..."
export GRADLE_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m -XX:+UseG1GC"
export JAVA_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m"

echo "📱 Compilation iOS pour Xcode..."
./gradlew :composeApp:linkReleaseFrameworkIosArm64 \
    --no-daemon \
    --no-build-cache \
    --no-configuration-cache \
    --max-workers=1 \
    --parallel

echo "✅ Compilation Xcode terminée!"

# Vérification du résultat
if [ -d "composeApp/build/bin/iosArm64/releaseFramework" ]; then
    echo "🎉 Framework iOS généré avec succès pour Xcode!"
    ls -la composeApp/build/bin/iosArm64/releaseFramework/
    echo "📦 Taille du framework:"
    du -sh composeApp/build/bin/iosArm64/releaseFramework/
else
    echo "❌ Échec de la génération du framework iOS"
    exit 1
fi


