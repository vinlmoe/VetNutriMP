#!/bin/bash

# Script de compilation iOS FINALE - Dernière tentative
# Utilise tous les moyens possibles pour résoudre le problème de mémoire

echo "🚨 COMPILATION iOS - MODE DE SURVIE"
echo "=================================="

set -e

# Étape 1: Nettoyage ultra-agressif
echo "🧹 Étape 1: Nettoyage ABSOLUMENT TOUT..."
./clean_all.sh

# Étape 2: Configuration mémoire MINIMALE
echo ""
echo "🔧 Étape 2: Configuration mémoire ultra-minimale..."
export GRADLE_OPTS="-Xmx1g -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -XX:+UseStringDeduplication"
export JAVA_OPTS="-Xmx1g -XX:MaxMetaspaceSize=256m"
export MAVEN_OPTS="-Xmx1g"

# Étape 3: Compilation avec paramètres ultra-conservateurs
echo ""
echo "📱 Étape 3: Compilation avec paramètres de survie..."

# Compiler seulement pour iOS ARM64 avec le minimum vital
./gradlew :composeApp:linkReleaseFrameworkIosArm64 \
    --no-daemon \
    --no-build-cache \
    --no-configuration-cache \
    --max-workers=1 \
    --parallel \
    --rerun-tasks \
    -Dkotlin.native.ignoreDisabledTargets=true \
    -Dkotlin.native.binary.memoryModel=strict \
    -Dkotlin.native.binary.freezing=disabled \
    -Dkotlin.incremental=false \
    -Dkotlin.compiler.execution.strategy=in-process

# Vérifier le résultat
if [ -f "composeApp/build/bin/iosArm64/releaseFramework/ComposeApp.framework/ComposeApp" ]; then
    echo ""
    echo "🎉 SUCCÈS CRITIQUE !"
    echo "📦 Framework iOS généré avec succès !"
    echo ""
    ls -lh composeApp/build/bin/iosArm64/releaseFramework/ComposeApp.framework/ComposeApp
    echo ""
    echo "📊 Informations du binaire:"
    file composeApp/build/bin/iosArm64/releaseFramework/ComposeApp.framework/ComposeApp
    echo ""
    echo "✅ Votre application iOS peut maintenant être intégrée dans Xcode !"
else
    echo ""
    echo "💥 ÉCHEC CRITIQUE"
    echo "❌ Le binaire ComposeApp n'a pas été généré"
    echo ""
    echo "🔍 Diagnostic:"
    ls -la composeApp/build/bin/iosArm64/releaseFramework/ComposeApp.framework/ || echo "Framework incomplet"
    echo ""
    echo "🚨 SOLUTIONS D'URGENCE:"
    echo "1. Redémarrez complètement votre Mac"
    echo "2. Augmentez la mémoire swap: sudo sysctl vm.swapusage"
    echo "3. Fermez TOUTES les applications inutiles"
    echo "4. Essayez sur une autre machine avec plus de RAM"

    exit 1
fi


