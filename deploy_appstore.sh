#!/bin/bash

# Script de déploiement iOS vers l'App Store
# Compile le framework, génère l'IPA et prépare pour l'App Store

set -e  # Arrêter en cas d'erreur

echo "🚀 Déploiement iOS vers l'App Store - VetNutri MP"
echo "=================================================="

# Variables de configuration
APP_NAME="VetNutriMP"
BUNDLE_ID="fr.vetbrain.vetnutri_mp"  # À adapter selon votre configuration
SCHEME_NAME="VetNutri"  # Nom du schéma dans Xcode
IOS_PROJECT_PATH="iosApp"  # Chemin vers le projet iOS

# Vérifier les prérequis
echo "🔍 Vérification des prérequis..."

# Vérifier si Xcode est installé
if ! command -v xcodebuild &> /dev/null; then
    echo "❌ Xcode n'est pas installé ou pas dans le PATH"
    exit 1
fi

# Vérifier si les outils de ligne de commande sont installés
if ! command -v xcrun &> /dev/null; then
    echo "❌ Xcode Command Line Tools non installés"
    exit 1
fi

echo "✅ Prérequis vérifiés"

# Étape 1: Compiler le framework Kotlin Native
echo ""
echo "📦 Étape 1: Compilation du framework Kotlin Native..."
./compile_xcode.sh

if [ ! -d "composeApp/build/bin/iosArm64/releaseFramework" ]; then
    echo "❌ Échec de la compilation du framework"
    exit 1
fi

echo "✅ Framework Kotlin Native compilé avec succès"

# Étape 2: Configuration pour l'App Store
echo ""
echo "🏪 Étape 2: Préparation pour l'App Store..."

# Vérifier si le projet iOS existe
if [ ! -d "$IOS_PROJECT_PATH" ]; then
    echo "❌ Projet iOS non trouvé dans $IOS_PROJECT_PATH"
    echo "💡 Assurez-vous d'avoir un projet Xcode dans le dossier iosApp/"
    exit 1
fi

# Étape 3: Générer l'archive pour l'App Store
echo ""
echo "📦 Étape 3: Génération de l'archive iOS..."

cd "$IOS_PROJECT_PATH"

# Nettoyer le projet Xcode
echo "🧹 Nettoyage du projet Xcode..."
xcodebuild clean -project "$APP_NAME.xcodeproj" -scheme "$SCHEME_NAME" -configuration Release

# Compiler pour archive (nécessite un appareil ou simulateur)
echo "🔨 Compilation pour archive..."
xcodebuild archive \
    -project "$APP_NAME.xcodeproj" \
    -scheme "$SCHEME_NAME" \
    -configuration Release \
    -archivePath "../build/$APP_NAME.xcarchive" \
    -destination generic/platform=iOS \
    SKIP_INSTALL=NO \
    BUILD_LIBRARY_FOR_DISTRIBUTION=NO

if [ $? -ne 0 ]; then
    echo "❌ Échec de la compilation pour archive"
    echo "💡 Vérifiez que votre projet Xcode est correctement configuré avec les certificats de signature"
    exit 1
fi

echo "✅ Archive générée avec succès"

# Étape 4: Générer l'IPA pour l'App Store
echo ""
echo "📱 Étape 4: Génération de l'IPA..."

xcodebuild -exportArchive \
    -archivePath "../build/$APP_NAME.xcarchive" \
    -exportPath "../build" \
    -exportOptionsPlist "../exportOptions.plist" \
    -allowProvisioningUpdates \
    -authenticationKeyPath "$HOME/.appstoreconnect/private_keys/AuthKey_XXXXXXXXXX.p8" \
    -authenticationKeyID "XXXXXXXXXX" \
    -authenticationKeyIssuerID "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"

if [ $? -ne 0 ]; then
    echo "❌ Échec de la génération de l'IPA"
    echo "💡 Créez un fichier exportOptions.plist avec vos paramètres de signature"
    echo "📝 Modèle de exportOptions.plist :"
    cat << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>app-store</string>
    <key>teamID</key>
    <string>VOTRE_TEAM_ID</string>
    <key>signingStyle</key>
    <string>manual</string>
    <key>provisioningProfiles</key>
    <dict>
        <key>fr.vetbrain.vetnutri_mp</key>
        <string>NOM_DU_PROVISIONING_PROFILE</string>
    </dict>
</dict>
</plist>
EOF
    exit 1
fi

echo "✅ IPA généré avec succès"

# Étape 5: Upload vers App Store Connect (optionnel)
echo ""
echo "☁️ Étape 5: Upload vers App Store Connect..."
echo "💡 Pour l'upload automatique, configurez vos credentials App Store Connect"

# Vérifier si l'IPA existe
IPA_PATH="../build/$APP_NAME.ipa"
if [ -f "$IPA_PATH" ]; then
    echo "📦 IPA trouvé: $(ls -lh "$IPA_PATH")"

    # Optionnel: Upload avec altool (nécessite configuration)
    # xcrun altool --upload-app -f "$IPA_PATH" -t ios -u "your-apple-id" -p "your-app-specific-password"

    echo ""
    echo "🎉 Déploiement terminé avec succès!"
    echo "📱 Votre application est prête pour l'App Store"
    echo ""
    echo "📋 Prochaines étapes:"
    echo "1. Connectez-vous à App Store Connect"
    echo "2. Créez une nouvelle version de votre app"
    echo "3. Upload l'IPA via 'TestFlight & App Store Connect'"
    echo "4. Soumettez pour review"
    echo ""
    echo "📦 Fichiers générés:"
    echo "   - Archive: build/$APP_NAME.xcarchive"
    echo "   - IPA: build/$APP_NAME.ipa"
    echo "   - Framework Kotlin: composeApp/build/bin/iosArm64/releaseFramework/"

else
    echo "⚠️ IPA non trouvé. Génération peut-être incomplète."
fi

echo ""
echo "🔗 Liens utiles:"
echo "   App Store Connect: https://appstoreconnect.apple.com"
echo "   TestFlight: https://testflight.apple.com"
echo ""
echo "✅ Déploiement iOS terminé!"


