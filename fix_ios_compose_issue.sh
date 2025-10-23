#!/bin/bash

echo "🔧 Correction du problème iOS Compose Multiplatform"
echo "=================================================="

# Nettoyage complet
echo "🧹 Nettoyage du projet..."
./gradlew clean
rm -rf build/
rm -rf composeApp/build/
rm -rf iosApp/ComposeApp.framework/

# Nettoyage des caches
echo "🗑️ Nettoyage des caches..."
rm -rf ~/.gradle/caches/
rm -rf ~/.konan/
rm -rf ~/Library/Developer/Xcode/DerivedData/

# Reconstruction du framework iOS avec optimisations désactivées
echo "🔨 Reconstruction du framework iOS..."
./gradlew :composeApp:linkDebugFrameworkIosArm64
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# Copie du framework vers le projet iOS
echo "📦 Copie du framework..."
cp -R composeApp/build/bin/iosArm64/debugFramework/ComposeApp.framework iosApp/
cp -R composeApp/build/bin/iosSimulatorArm64/debugFramework/ComposeApp.framework iosApp/

echo "✅ Correction terminée !"
echo ""
echo "Prochaines étapes :"
echo "1. Ouvrez le projet dans Xcode"
echo "2. Nettoyez le build (Product > Clean Build Folder)"
echo "3. Recompilez et testez"
echo ""
echo "Si le problème persiste, essayez :"
echo "- Redémarrez Xcode"
echo "- Supprimez l'app du simulateur et réinstallez"
echo "- Vérifiez que le framework ComposeApp.framework est bien présent dans iosApp/"
