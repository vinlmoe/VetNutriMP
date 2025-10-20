#!/bin/bash

# Script de nettoyage ULTRA-AGRESSIF pour résoudre les problèmes de mémoire
# Supprime absolument TOUT avant de recompiler

echo "🧹 NETTOYAGE ULTRA-COMPLET - Mode PANIQUE"
echo "=========================================="

set -e

# Étape 1: Arrêter tous les processus Gradle/Java
echo "🔪 Étape 1: Arrêt des processus..."
pkill -f gradle || true
pkill -f kotlin || true
pkill -f java || true
sleep 3

# Étape 2: Supprimer TOUS les caches
echo "🗑️ Étape 2: Suppression des caches système..."
rm -rf ~/.gradle/
rm -rf ~/.kotlin/
rm -rf ~/.konan/
rm -rf ~/Library/Caches/JetBrains/
rm -rf ~/Library/Application\ Support/JetBrains/

# Étape 3: Supprimer TOUS les builds
echo "💥 Étape 3: Suppression des builds..."
rm -rf .gradle/
rm -rf build/
rm -rf composeApp/build/
rm -rf iosApp/build/
rm -rf androidApp/build/

# Étape 4: Supprimer les fichiers temporaires
echo "🧽 Étape 4: Nettoyage fichiers temporaires..."
find . -name "*.tmp" -delete || true
find . -name "*.temp" -delete || true
find . -name ".DS_Store" -delete || true

# Étape 5: Vider la corbeille
echo "♻️ Étape 5: Vidage de la corbeille..."
osascript -e 'tell application "Finder" to empty trash' || true

# Étape 6: Redémarrer le système de fichiers
echo "🔄 Étape 6: Synchronisation du système de fichiers..."
sync

echo ""
echo "✅ NETTOYAGE ULTRA-COMPLET TERMINÉ"
echo ""
echo "📊 Espace libéré:"
df -h ~ | grep -E "(Filesystem|Users)"

echo ""
echo "🚀 Le système est maintenant propre. Lancez:"
echo "   ./compile_xcode.sh"
echo ""
echo "💡 Si ça ne marche toujours pas, redémarrez votre Mac complètement."


