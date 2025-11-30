#!/bin/bash

# Script pour vérifier la compatibilité 16KB des bibliothèques natives dans l'APK
# Utilise readelf pour vérifier l'alignement des segments ELF

echo "🔍 Vérification de la compatibilité 16KB pour l'APK"
echo "=================================================="

APK_PATH="$1"

if [ -z "$APK_PATH" ]; then
    # Chercher l'APK release le plus récent
    APK_PATH=$(find composeApp/build/outputs/apk/release -name "*.apk" -type f -exec ls -t {} + | head -1)
fi

if [ -z "$APK_PATH" ] || [ ! -f "$APK_PATH" ]; then
    echo "❌ Erreur: APK introuvable"
    echo "Usage: $0 [chemin_vers_apk]"
    echo "Ou compilez d'abord: ./gradlew composeApp:assembleRelease"
    exit 1
fi

echo "📦 APK analysé: $APK_PATH"
echo ""

# Créer un répertoire temporaire pour extraire l'APK
TEMP_DIR=$(mktemp -d)
trap "rm -rf $TEMP_DIR" EXIT

echo "📂 Extraction de l'APK..."
unzip -q "$APK_PATH" -d "$TEMP_DIR"

# Trouver toutes les bibliothèques natives
SO_FILES=$(find "$TEMP_DIR" -name "*.so" -type f)

if [ -z "$SO_FILES" ]; then
    echo "✅ Aucune bibliothèque native trouvée - L'application est compatible 16KB"
    exit 0
fi

echo "📚 Bibliothèques natives trouvées:"
echo "$SO_FILES" | sed 's|.*/||' | sort -u
echo ""

# Vérifier l'alignement de chaque bibliothèque
ISSUES=0
for SO_FILE in $SO_FILES; do
    LIB_NAME=$(basename "$SO_FILE")
    ABI=$(echo "$SO_FILE" | grep -oE "(arm64-v8a|armeabi-v7a|x86|x86_64)" | head -1)
    
    # Vérifier l'alignement avec readelf
    if command -v readelf >/dev/null 2>&1; then
        # Extraire l'alignement des segments LOAD
        ALIGNMENT=$(readelf -l "$SO_FILE" 2>/dev/null | grep -A 1 "LOAD" | grep -oP "Align\s+\K[0-9]+" | head -1)
        
        if [ -n "$ALIGNMENT" ]; then
            if [ "$ALIGNMENT" -lt 16384 ]; then
                echo "❌ $LIB_NAME ($ABI): Alignement = $ALIGNMENT (requis: 16384)"
                ISSUES=$((ISSUES + 1))
            else
                echo "✅ $LIB_NAME ($ABI): Alignement = $ALIGNMENT"
            fi
        else
            echo "⚠️  $LIB_NAME ($ABI): Impossible de déterminer l'alignement"
        fi
    else
        echo "⚠️  readelf non disponible - Impossible de vérifier l'alignement"
        echo "   Installez binutils pour vérifier: brew install binutils"
        break
    fi
done

echo ""
if [ $ISSUES -eq 0 ]; then
    echo "✅ Toutes les bibliothèques natives sont compatibles avec les pages de 16KB"
    exit 0
else
    echo "❌ $ISSUES bibliothèque(s) native(s) ne sont pas alignées sur 16KB"
    echo ""
    echo "💡 Solutions:"
    echo "   1. Mettez à jour les dépendances vers des versions compatibles 16KB"
    echo "   2. Contactez les mainteneurs des bibliothèques problématiques"
    echo "   3. Excluez les ABI problématiques si possible (armeabi-v7a peut causer des problèmes)"
    echo "   4. Utilisez zipalign pour vérifier: zipalign -c -P 16 -v $APK_PATH"
    exit 1
fi

