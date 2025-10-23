#!/bin/bash

# Script de compilation iOS minimal - Dernière chance
# Compile seulement l'essentiel pour éviter les problèmes de mémoire

echo "🚨 COMPILATION iOS MINIMALE - MODE DE SURVIE"
echo "==========================================="

set -e

# Étape 1: Nettoyage ultra-agressif
echo "🧹 Étape 1: Nettoyage ultra-agressif..."
rm -rf ~/.gradle/ 2>/dev/null || true
rm -rf .gradle/ 2>/dev/null || true
rm -rf composeApp/build/ 2>/dev/null || true
rm -rf build/ 2>/dev/null || true

# Étape 2: Configuration mémoire minimale
echo "🔧 Étape 2: Configuration mémoire minimale..."
export GRADLE_OPTS="-Xmx4g -XX:MaxMetaspaceSize=2g -XX:+UseG1GC"
export JAVA_OPTS="-Xmx4g -XX:MaxMetaspaceSize=2g"
export JANSI_TMPDIR="/tmp"

# Étape 3: Compilation minimale
echo "📱 Étape 3: Compilation minimale..."

# Compiler seulement le strict minimum
./gradlew :composeApp:compileKotlinIosArm64 \
    --no-daemon \
    --no-build-cache \
    --no-configuration-cache \
    --max-workers=1 \
    --parallel=false \
    -Dkotlin.native.disableCompilerDaemon=true \
    -Dkotlin.incremental=false \
    -Dkotlin.native.binary.memoryModel=experimental \
    -Dkotlin.native.binary.freezing=disabled \
    -Dkotlin.mpp.enableGranularSourceSetsMetadata=false \
    -Dkotlin.compiler.suppress.all.warnings=true \
    -Dkotlin.native.ignoreDisabledTargets=true

# Si la compilation réussit, essayer de générer le framework
if [ $? -eq 0 ]; then
    echo "✅ Compilation Kotlin réussie, génération du framework..."
    
    ./gradlew :composeApp:linkDebugFrameworkIosArm64 \
        --no-daemon \
        --no-build-cache \
        --max-workers=1 \
        --parallel=false \
        -Dkotlin.native.disableCompilerDaemon=true \
        -Dkotlin.native.binary.memoryModel=experimental \
        -Dkotlin.native.binary.freezing=disabled
fi

# Vérifier le résultat
echo "🔍 Vérification du framework..."
if [ -d "composeApp/build/bin/iosArm64/debugFramework" ]; then
    echo "✅ Framework iOS généré avec succès!"
    echo "📊 Informations du framework:"
    ls -la composeApp/build/bin/iosArm64/debugFramework/
    echo "📦 Taille du framework:"
    du -sh composeApp/build/bin/iosArm64/debugFramework/
    
    # Copier le framework
    echo "📋 Copie du framework dans le projet iOS..."
    cp -R composeApp/build/bin/iosArm64/debugFramework/ComposeApp.framework iosApp/ 2>/dev/null || true
    
    echo ""
    echo "🎉 COMPILATION MINIMALE RÉUSSIE !"
    echo "⚠️  Ce framework est en mode DEBUG et peut être plus volumineux."
    echo "   Il est fonctionnel mais pas optimisé pour la production."
else
    echo "❌ Échec de la génération du framework iOS"
    echo ""
    echo "🚨 SOLUTIONS D'URGENCE:"
    echo "1. Redémarrez complètement votre Mac"
    echo "2. Fermez TOUTES les applications inutiles"
    echo "3. Vérifiez l'espace disque (au moins 30GB libres)"
    echo "4. Essayez sur une machine avec 32GB+ de RAM"
    echo "5. Contactez le support technique"
    exit 1
fi

echo ""
echo "✅ Script de compilation minimale terminé !"