#!/bin/bash

# Script de synchronisation des fichiers de localisation
# Copie les fichiers strings_*.json de commonMain/resources vers androidMain/assets

echo "🔄 Synchronisation des fichiers de localisation..."

# Vérifier que le dossier source existe
if [ ! -d "composeApp/src/commonMain/resources" ]; then
    echo "❌ Erreur: Dossier source composeApp/src/commonMain/resources introuvable"
    exit 1
fi

# Créer le dossier de destination s'il n'existe pas
mkdir -p composeApp/src/androidMain/assets

# Copier tous les fichiers strings_*.json
echo "📁 Copie des fichiers de localisation..."
cp composeApp/src/commonMain/resources/strings_*.json composeApp/src/androidMain/assets/

# Vérifier le nombre de fichiers copiés
source_count=$(ls composeApp/src/commonMain/resources/strings_*.json | wc -l)
dest_count=$(ls composeApp/src/androidMain/assets/strings_*.json | wc -l)

echo "✅ Synchronisation terminée !"
echo "📊 Fichiers source: $source_count"
echo "📊 Fichiers copiés: $dest_count"

# Lister les fichiers copiés
echo "📋 Fichiers copiés:"
ls -la composeApp/src/androidMain/assets/strings_*.json
