#!/bin/bash

# Script de configuration pour l'App Store Connect
# Configure les outils nécessaires pour le déploiement automatique

echo "🔧 Configuration App Store Connect - VetNutri MP"
echo "================================================"

# Vérifier les prérequis
echo "🔍 Vérification des outils..."

# Vérifier si Xcode est installé
if ! command -v xcodebuild &> /dev/null; then
    echo "❌ Xcode n'est pas installé"
    echo "💡 Installez Xcode depuis le Mac App Store"
    exit 1
fi

# Vérifier si les outils de ligne de commande sont installés
if ! command -v xcrun &> /dev/null; then
    echo "❌ Xcode Command Line Tools non installés"
    echo "💡 Exécutez: xcode-select --install"
    exit 1
fi

echo "✅ Xcode et outils de ligne de commande détectés"

# Étape 1: Configuration des certificats
echo ""
echo "🔐 Étape 1: Configuration des certificats..."
echo "💡 Vous devez avoir:"
echo "   1. Un compte développeur Apple (https://developer.apple.com)"
echo "   2. Un certificat de distribution iOS"
echo "   3. Un profil de provisioning de distribution"

# Étape 2: Création du fichier de configuration App Store Connect
echo ""
echo "📝 Étape 2: Configuration App Store Connect..."

# Créer le répertoire pour les clés API
mkdir -p ~/.appstoreconnect/private_keys

echo "📋 Pour configurer l'upload automatique vers App Store Connect:"
echo ""
echo "1. Générez une clé API sur App Store Connect:"
echo "   - Allez sur: https://appstoreconnect.apple.com/access/api"
echo "   - Créez une nouvelle clé API avec 'App Manager' permissions"
echo "   - Téléchargez le fichier .p8"
echo ""
echo "2. Placez le fichier .p8 dans: ~/.appstoreconnect/private_keys/"
echo "   Exemple: ~/.appstoreconnect/private_keys/AuthKey_XXXXXXXXXX.p8"
echo ""
echo "3. Modifiez deploy_appstore.sh avec vos vraies valeurs:"
echo "   - APPLE_ID: votre Apple ID"
echo "   - KEY_ID: l'ID de votre clé API (8 caractères)"
echo "   - ISSUER_ID: votre Issuer ID (UUID)"
echo "   - BUNDLE_ID: votre Bundle ID"

# Étape 3: Configuration du projet Xcode
echo ""
echo "🏗️ Étape 3: Configuration du projet Xcode..."

if [ -d "iosApp" ]; then
    echo "✅ Projet iOS détecté"

    # Vérifier la configuration du projet
    if [ -f "iosApp/VetNutri/VetNutri.entitlements" ]; then
        echo "✅ Fichier entitlements trouvé"
    else
        echo "⚠️ Fichier entitlements non trouvé"
        echo "💡 Créez un fichier VetNutri.entitlements dans iosApp/VetNutri/"
    fi

    # Vérifier les capacités
    echo ""
    echo "📋 Vérifiez les capacités dans Xcode:"
    echo "   - App Groups: OFF (si non utilisé)"
    echo "   - Background Modes: OFF (si non utilisé)"
    echo "   - Push Notifications: OFF (si non utilisé)"
    echo "   - Sign In with Apple: OFF (si non utilisé)"

else
    echo "❌ Projet iOS non trouvé"
    echo "💡 Assurez-vous d'avoir un dossier iosApp/ avec votre projet Xcode"
fi

# Étape 4: Test de la configuration
echo ""
echo "🧪 Étape 4: Test de la configuration..."

echo "💡 Pour tester la configuration:"
echo "1. Ouvrez Xcode"
echo "2. Sélectionnez votre projet"
echo "3. Allez dans 'Signing & Capabilities'"
echo "4. Vérifiez que le certificat de distribution est sélectionné"
echo "5. Essayez de compiler pour 'Any iOS Device'"

echo ""
echo "📚 Ressources utiles:"
echo "   📖 Guide Apple: https://developer.apple.com/app-store/distribution/"
echo "   🔑 Certificats: https://developer.apple.com/account/resources/certificates/"
echo "   📱 Provisioning: https://developer.apple.com/account/resources/profiles/"
echo "   ☁️ App Store Connect: https://appstoreconnect.apple.com"
echo ""
echo "✅ Configuration App Store terminée!"
echo ""
echo "🚀 Prochaines étapes:"
echo "1. Configurez vos certificats dans Xcode"
echo "2. Modifiez exportOptions.plist avec vos vraies valeurs"
echo "3. Exécutez: ./deploy_appstore.sh"
echo "4. Upload l'IPA via App Store Connect ou Xcode"


