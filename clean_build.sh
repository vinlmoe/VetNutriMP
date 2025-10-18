#!/bin/bash

# Script de nettoyage pour résoudre les problèmes de mémoire
echo "🧹 Nettoyage du projet pour résoudre les problèmes de mémoire..."

# Nettoyer les builds
echo "Suppression des dossiers de build..."
rm -rf build/
rm -rf composeApp/build/
rm -rf iosApp/build/

# Nettoyer les caches Gradle
echo "Nettoyage des caches Gradle..."
./gradlew clean
./gradlew --stop

# Nettoyer les caches Kotlin
echo "Nettoyage des caches Kotlin..."
rm -rf ~/.gradle/caches/
rm -rf ~/.kotlin/

# Nettoyer les fichiers temporaires
echo "Suppression des fichiers temporaires..."
find . -name "*.tmp" -delete
find . -name "*.log" -delete
find . -name ".DS_Store" -delete

# Nettoyer les ressources dupliquées (garder seulement les versions optimisées)
echo "Optimisation des ressources..."
# Garder seulement la version optimisée du fichier principal
if [ -f "iosApp/iosApp/Resources/vetnutri_export_init.json" ]; then
    echo "Suppression des doublons de vetnutri_export_init.json..."
    rm -f "iosApp/iosApp/vetnutri_export_init.json"
fi

echo "✅ Nettoyage terminé !"
echo "💡 Vous pouvez maintenant relancer la compilation avec:"
echo "   ./gradlew build"

