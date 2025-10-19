#!/bin/bash

# Script de compilation optimisée pour résoudre les problèmes de heap space
# Utilise des optimisations mémoire et de compilation

echo "🧹 Nettoyage des caches et builds précédents..."
./gradlew clean

echo "🗑️ Nettoyage des caches Gradle..."
rm -rf ~/.gradle/caches/
rm -rf .gradle/
rm -rf build/

echo "📦 Nettoyage des caches Kotlin..."
rm -rf composeApp/build/

echo "🔧 Configuration de la mémoire JVM..."
export GRADLE_OPTS="-Xmx16g -XX:MaxMetaspaceSize=6144m -XX:+UseG1GC -XX:+UseStringDeduplication"

echo "🚀 Compilation optimisée en cours..."
./gradlew assembleDebug --no-daemon --no-build-cache --no-configuration-cache --max-workers=2

echo "✅ Compilation terminée!"