#!/bin/bash

# Script pour mettre à jour le framework ComposeApp dans le projet iOS

echo "🔄 Mise à jour du framework ComposeApp pour iOS..."

# Copier le framework release
echo "📦 Copie du framework release..."
cp -R composeApp/build/fat-framework/release/ComposeApp.framework iosApp/

# Vérifier que le framework a été copié
if [ -d "iosApp/ComposeApp.framework" ]; then
    echo "✅ Framework ComposeApp copié avec succès"
    echo "📊 Taille du framework: $(du -sh iosApp/ComposeApp.framework | cut -f1)"
else
    echo "❌ Erreur: Framework non trouvé"
    exit 1
fi

echo ""
echo "📋 Instructions pour Xcode:"
echo "1. Ouvrez le projet iosApp.xcodeproj dans Xcode"
echo "2. Sélectionnez le projet 'iosApp' dans le navigateur"
echo "3. Sélectionnez la target 'VetNutri ios'"
echo "4. Allez dans l'onglet 'General'"
echo "5. Dans la section 'Frameworks, Libraries, and Embedded Content'"
echo "6. Cliquez sur '+' et ajoutez 'ComposeApp.framework'"
echo "7. Assurez-vous que 'Embed & Sign' est sélectionné"
echo ""
echo "🎯 Le framework optimisé est maintenant prêt pour iOS !"