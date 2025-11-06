#!/bin/bash

# Script de compilation et signature macOS pour distribution hors App Store
# Ce script compile l'application DMG, la signe et la notarise pour distribution

set -e  # Arrêter en cas d'erreur

echo "🍎 Compilation et Signature macOS - VetNutri MP"
echo "================================================"

# Variables de configuration
APP_NAME="VetNutriMP"
PACKAGE_VERSION="3.1.39"
BUNDLE_ID="fr.vetbrain.vetnutri_mp"
VENDOR="VetBrain"

# Variables de signature (à configurer selon votre compte développeur)
# Obtenez ces informations depuis votre compte développeur Apple
TEAM_ID="N8M75AVX29"  # Votre Team ID (visible dans votre compte développeur)

# IMPORTANT: Pour signer hors App Store, vous avez besoin d'un certificat "Developer ID Application"
# Si vous n'avez pas ce certificat, vous pouvez:
# 1. Le créer sur https://developer.apple.com/account/resources/certificates/
# 2. Ou utiliser temporairement "Apple Distribution" (mais ce n'est pas recommandé pour distribution hors App Store)
# Vérifier les certificats disponibles:
#   security find-identity -v -p codesigning

# Essayer de trouver automatiquement le certificat Developer ID
DEVELOPER_ID=$(security find-identity -v -p codesigning | grep "Developer ID Application" | head -1 | sed 's/.*"\(.*\)".*/\1/')

# Si pas trouvé, essayer avec Apple Distribution (temporaire, pas idéal)
if [ -z "$DEVELOPER_ID" ]; then
    DEVELOPER_ID=$(security find-identity -v -p codesigning | grep "Apple Distribution" | head -1 | sed 's/.*"\(.*\)".*/\1/')
    if [ -n "$DEVELOPER_ID" ]; then
        echo "⚠️  Utilisation de 'Apple Distribution' (pas idéal pour distribution hors App Store)"
        echo "💡 Créez un certificat 'Developer ID Application' sur https://developer.apple.com/account/resources/certificates/"
    fi
fi

# Configuration de la notarisation (optionnelle mais recommandée)
# Pour activer la notarisation:
# 1. Allez sur https://appleid.apple.com
# 2. Section "Sécurité" → "Mots de passe spécifiques aux applications"
# 3. Créez un nouveau mot de passe pour "App Store Connect API"
# 4. Configurez les variables ci-dessous

APPLE_ID=""  # Votre Apple ID (ex: votre@email.com)
APP_SPECIFIC_PASSWORD=""  # Mot de passe spécifique généré (format: xxxx-xxxx-xxxx-xxxx)

# Alternative: Utiliser un fichier de configuration pour éviter de mettre les credentials dans le script
# Créez un fichier .notarization_config avec:
# APPLE_ID=votre@email.com
# APP_SPECIFIC_PASSWORD=xxxx-xxxx-xxxx-xxxx
NOTARIZATION_CONFIG_FILE=".notarization_config"

# Charger la configuration depuis le fichier si elle existe
if [ -f "$NOTARIZATION_CONFIG_FILE" ]; then
    source "$NOTARIZATION_CONFIG_FILE"
    echo "✅ Configuration de notarisation chargée depuis $NOTARIZATION_CONFIG_FILE"
fi

# Vérifier les prérequis
echo "🔍 Vérification des prérequis..."

if ! command -v codesign &> /dev/null; then
    echo "❌ codesign n'est pas disponible. Assurez-vous d'avoir Xcode installé."
    exit 1
fi

if ! command -v xcrun &> /dev/null; then
    echo "❌ xcrun n'est pas disponible. Assurez-vous d'avoir Xcode Command Line Tools installés."
    exit 1
fi

echo "✅ Prérequis vérifiés"

# Étape 1: Compilation du DMG
echo ""
echo "📦 Étape 1: Compilation du DMG macOS..."
./gradlew :composeApp:packageDmg --no-daemon

DMG_PATH="composeApp/build/compose/binaries/main/dmg/${APP_NAME}-${PACKAGE_VERSION}.dmg"

if [ ! -f "$DMG_PATH" ]; then
    echo "❌ Échec de la génération du DMG"
    echo "💡 Vérifiez les logs de compilation"
    exit 1
fi

echo "✅ DMG généré: $DMG_PATH"

# Étape 2: Vérification de la configuration de signature
echo ""
echo "🔐 Étape 2: Vérification de la configuration de signature..."

if [ -z "$TEAM_ID" ] || [ "$TEAM_ID" = "VOTRE_TEAM_ID" ]; then
    echo "⚠️  Team ID non configuré"
    echo "💡 Modifiez TEAM_ID dans ce script avec votre Team ID Apple"
    echo "💡 Vous pouvez le trouver sur https://developer.apple.com/account"
    exit 1
fi

# Vérifier si un certificat de signature existe
if [ -z "$DEVELOPER_ID" ]; then
    echo "❌ Aucun certificat de signature trouvé"
    echo ""
    echo "💡 Pour signer votre application macOS, vous avez besoin d'un certificat 'Developer ID Application':"
    echo "   1. Allez sur https://developer.apple.com/account/resources/certificates/"
    echo "   2. Cliquez sur '+' pour créer un nouveau certificat"
    echo "   3. Sélectionnez 'Developer ID Application' (pas 'Apple Distribution')"
    echo "   4. Suivez les instructions pour créer une CSR (Certificate Signing Request)"
    echo "   5. Téléchargez et installez le certificat dans votre Keychain"
    echo ""
    echo "📋 Certificats actuellement installés:"
    security find-identity -v -p codesigning | grep -E "(Developer ID|Apple Distribution)" || echo "   Aucun trouvé"
    exit 1
fi

echo "✅ Certificat de signature trouvé: $DEVELOPER_ID"

# Étape 3: Signature du DMG
echo ""
echo "✍️  Étape 3: Signature du DMG..."

# Créer un DMG temporaire signé
SIGNED_DMG_PATH="${DMG_PATH%.dmg}-signed.dmg"
TEMP_DMG_DIR=$(mktemp -d)

# Monter le DMG original
MOUNT_POINT=$(mktemp -d)
hdiutil attach "$DMG_PATH" -mountpoint "$MOUNT_POINT" -quiet

# Créer un nouveau DMG signé
hdiutil create -srcfolder "$MOUNT_POINT" -volname "$APP_NAME" -fs HFS+ -format UDRW -ov "$TEMP_DMG_DIR/temp.dmg"

# Démontrer le DMG original
hdiutil detach "$MOUNT_POINT" -quiet

# Monter le nouveau DMG pour signature
MOUNT_POINT=$(mktemp -d)
hdiutil attach "$TEMP_DMG_DIR/temp.dmg" -mountpoint "$MOUNT_POINT" -quiet

# Signer l'application dans le DMG
APP_IN_DMG="$MOUNT_POINT/$APP_NAME.app"
if [ -d "$APP_IN_DMG" ]; then
    echo "   Signature de l'application..."
    
    # Créer un fichier d'entitlements temporaire
    ENTITLEMENTS_FILE=$(mktemp)
    cat > "$ENTITLEMENTS_FILE" << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>com.apple.security.cs.allow-jit</key>
    <true/>
    <key>com.apple.security.cs.allow-unsigned-executable-memory</key>
    <true/>
</dict>
</plist>
EOF
    
    codesign --force --deep --sign "$DEVELOPER_ID" \
        --timestamp \
        --options runtime \
        --entitlements "$ENTITLEMENTS_FILE" \
        "$APP_IN_DMG"
    
    # Nettoyer le fichier temporaire
    rm -f "$ENTITLEMENTS_FILE"
    
    # Vérifier la signature
    codesign --verify --verbose "$APP_IN_DMG"
    echo "✅ Application signée"
else
    echo "⚠️  Application non trouvée dans le DMG"
fi

# Démontrer le DMG temporaire
hdiutil detach "$MOUNT_POINT" -quiet

# Convertir en DMG final signé
hdiutil convert "$TEMP_DMG_DIR/temp.dmg" -format UDZO -o "$SIGNED_DMG_PATH" -ov

# Nettoyer
rm -rf "$TEMP_DMG_DIR"

# Signer le DMG lui-même
echo "   Signature du DMG..."
codesign --sign "$DEVELOPER_ID" --timestamp "$SIGNED_DMG_PATH"

# Vérifier la signature du DMG
codesign --verify --verbose "$SIGNED_DMG_PATH"
echo "✅ DMG signé: $SIGNED_DMG_PATH"

# Étape 4: Notarisation (optionnelle mais recommandée)
echo ""
echo "📋 Étape 4: Notarisation..."

# Vérifier si notarytool est disponible
if ! command -v xcrun &> /dev/null || ! xcrun notarytool --version &> /dev/null; then
    echo "⚠️  notarytool non disponible"
    echo "💡 Installez Xcode Command Line Tools: xcode-select --install"
    echo ""
    echo "✅ DMG signé prêt pour distribution (sans notarisation): $SIGNED_DMG_PATH"
    exit 0
fi

if [ -z "$APPLE_ID" ] || [ -z "$APP_SPECIFIC_PASSWORD" ]; then
    echo "⚠️  Notarisation ignorée (Apple ID ou mot de passe non configuré)"
    echo ""
    echo "💡 Pour activer la notarisation, choisissez une option:"
    echo ""
    echo "   Option 1: Créer un fichier .notarization_config"
    echo "   Créez un fichier .notarization_config à la racine du projet avec:"
    echo "   APPLE_ID=votre@email.com"
    echo "   APP_SPECIFIC_PASSWORD=xxxx-xxxx-xxxx-xxxx"
    echo ""
    echo "   Option 2: Modifier directement ce script"
    echo "   Décommentez et configurez APPLE_ID et APP_SPECIFIC_PASSWORD"
    echo ""
    echo "   Pour générer un mot de passe spécifique:"
    echo "   1. Allez sur https://appleid.apple.com"
    echo "   2. Section 'Sécurité' → 'Mots de passe spécifiques aux applications'"
    echo "   3. Créez un nouveau mot de passe pour 'App Store Connect API'"
    echo ""
    echo "✅ DMG signé prêt pour distribution: $SIGNED_DMG_PATH"
    exit 0
fi

echo "   Soumission pour notarisation Apple..."
echo "   Cela peut prendre quelques minutes..."

# Soumettre pour notarisation
NOTARIZATION_OUTPUT=$(xcrun notarytool submit "$SIGNED_DMG_PATH" \
    --apple-id "$APPLE_ID" \
    --team-id "$TEAM_ID" \
    --password "$APP_SPECIFIC_PASSWORD" \
    --wait 2>&1)

NOTARIZATION_EXIT_CODE=$?

if [ $NOTARIZATION_EXIT_CODE -eq 0 ]; then
    echo "✅ Notarisation réussie"
    
    # Agrafer le ticket de notarisation
    echo "   Agrafage du ticket de notarisation..."
    xcrun stapler staple "$SIGNED_DMG_PATH"
    
    # Vérifier la notarisation
    if xcrun stapler validate "$SIGNED_DMG_PATH" &> /dev/null; then
        echo "✅ Ticket de notarisation agrafé avec succès"
    else
        echo "⚠️  Le ticket de notarisation n'a pas pu être agrafé"
    fi
    
    echo ""
    echo "✅ DMG notarisé et prêt pour distribution: $SIGNED_DMG_PATH"
    echo "   Les utilisateurs pourront installer sans aucun avertissement de sécurité"
else
    echo "❌ Échec de la notarisation"
    echo ""
    echo "📋 Détails de l'erreur:"
    echo "$NOTARIZATION_OUTPUT"
    echo ""
    echo "💡 Vérifiez:"
    echo "   1. Vos credentials (APPLE_ID et APP_SPECIFIC_PASSWORD)"
    echo "   2. Que votre compte développeur est actif"
    echo "   3. Que le DMG est correctement signé"
    echo ""
    echo "✅ DMG signé (mais non notarisé) disponible: $SIGNED_DMG_PATH"
    exit 1
fi

echo ""
echo "🎉 Compilation et signature terminées avec succès!"
echo ""
echo "📦 Fichiers générés:"
echo "   - DMG original: $DMG_PATH"
echo "   - DMG signé: $SIGNED_DMG_PATH"
echo ""
echo "📋 Prochaines étapes:"
echo "   1. Testez le DMG signé sur un Mac"
echo "   2. Distribuez le DMG via votre site web"
echo "   3. Les utilisateurs pourront installer l'application sans avertissements"
echo ""
echo "⚠️  Note: Ce DMG ne peut PAS être soumis à l'App Store macOS"
echo "   car il s'agit d'une application JVM. Pour l'App Store, il faudra"
echo "   attendre le support macOS natif de Compose Multiplatform."

