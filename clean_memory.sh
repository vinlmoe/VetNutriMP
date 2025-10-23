#!/bin/bash

# Script de nettoyage mémoire pour éviter les OutOfMemoryError
echo "🧹 Nettoyage mémoire avant compilation iOS..."

# Nettoyer les caches Gradle
echo "📦 Nettoyage des caches Gradle..."
./gradlew clean --no-daemon

# Nettoyer les caches Kotlin
echo "🔧 Nettoyage des caches Kotlin..."
rm -rf ~/.gradle/caches/
rm -rf ~/.gradle/daemon/

# Nettoyer les builds précédents
echo "🗑️ Nettoyage des builds précédents..."
rm -rf composeApp/build/
rm -rf build/

# Nettoyer les frameworks iOS existants
echo "📱 Nettoyage des frameworks iOS..."
rm -rf iosApp/ComposeApp.framework

# Configuration JVM optimisée pour éviter OutOfMemoryError
echo "♻️ Configuration JVM optimisée..."
export GRADLE_OPTS="-XX:+UseG1GC -Xmx16g -Xms6g -XX:MaxMetaspaceSize=3g -XX:+UseStringDeduplication -XX:+UnlockExperimentalVMOptions -XX:+UseZGC -XX:+UseLargePages -XX:+UseCompressedOops -XX:+UseCompressedClassPointers -XX:MaxDirectMemorySize=4g"

echo "✅ Nettoyage terminé. Prêt pour la compilation iOS !"

