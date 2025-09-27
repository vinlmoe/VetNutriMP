#!/bin/bash

# Script pour supprimer tous les logs du projet VetNutriMP

echo "🧹 Nettoyage des logs du projet VetNutriMP..."

# Répertoire de base
BASE_DIR="/Users/slefebvre/VetNutriMP/composeApp/src"

# Fonction pour nettoyer un fichier
clean_file() {
    local file="$1"
    if [[ -f "$file" && "$file" != *.backup && "$file" != *.bak ]]; then
        echo "Nettoyage de: $file"
        
        # Supprimer les lignes avec DEBUG:
        sed -i '' '/DEBUG:/d' "$file"
        
        # Supprimer les lignes avec println("DEBUG
        sed -i '' '/println("DEBUG/d' "$file"
        
        # Supprimer les lignes avec println("🔄
        sed -i '' '/println("🔄/d' "$file"
        
        # Supprimer les lignes avec println("✅
        sed -i '' '/println("✅/d' "$file"
        
        # Supprimer les lignes avec println("❌
        sed -i '' '/println("❌/d' "$file"
        
        # Supprimer les lignes avec console.log
        sed -i '' '/console\.log/d' "$file"
        
        # Supprimer les lignes vides multiples
        sed -i '' '/^$/N;/^\n$/d' "$file"
    fi
}

# Nettoyer tous les fichiers .kt
find "$BASE_DIR" -name "*.kt" -type f | while read -r file; do
    clean_file "$file"
done

echo "✅ Nettoyage terminé!"
