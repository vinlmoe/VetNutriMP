#!/bin/bash

# Script pour compiler et créer un DMG macOS sans signature
# Usage: ./compile_dmg_unsigned.sh

set -e

echo "🔨 Compilation de l'application macOS (sans signature)..."
echo ""

# Nettoyer les builds précédents (optionnel)
# ./gradlew clean

# Compiler l'application desktop en release
echo "📦 Compilation de l'application..."
./gradlew :composeApp:packageReleaseDmg

echo ""
echo "✅ DMG créé avec succès (non signé)!"
echo ""
echo "📁 Le fichier DMG se trouve dans:"
echo "   composeApp/build/compose/binaries/main-release/dmg/"
echo ""
echo "⚠️  Note: Le DMG n'est pas signé. Les utilisateurs devront peut-être"
echo "   autoriser l'exécution dans les Préférences Système > Sécurité."

