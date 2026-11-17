# 🚀 Déploiement iOS vers l'App Store - VetNutri MP

Ce guide explique comment utiliser les scripts de déploiement automatique pour envoyer votre application VetNutri MP vers l'App Store.

## 📋 Prérequis

### 1. Compte Développeur Apple
- Compte développeur Apple actif (99€/an)
- Accès à App Store Connect

### 2. Certificats et Profils de Provisioning
- **Certificat de distribution iOS** (iOS Distribution Certificate)
- **Profil de provisioning de distribution** (Distribution Provisioning Profile)
- Configurés dans votre projet Xcode

### 3. Clé API App Store Connect (pour upload automatique)
- Générée sur App Store Connect avec permissions "App Manager"
- Fichier `.p8` téléchargé

## 🛠️ Scripts Disponibles

### `compile_xcode.sh` - ✅ Compilation Kotlin Native (Fonctionnel)
```bash
./compile_xcode.sh
```
- Compile le framework Kotlin Native pour iOS
- Résout les problèmes de mémoire heap space
- Génère le framework dans `composeApp/build/bin/iosArm64/releaseFramework/`

### `setup_appstore.sh` - ⚙️ Configuration App Store
```bash
./setup_appstore.sh
```
- Vérifie les prérequis (Xcode, certificats)
- Guide la configuration des certificats
- Explique la configuration App Store Connect

### `deploy_appstore.sh` - 🚀 Déploiement Complet
```bash
./deploy_appstore.sh
```
- Compile le framework Kotlin Native
- Génère l'archive Xcode (.xcarchive)
- Crée l'IPA pour l'App Store
- Prépare pour l'upload

## 📱 Utilisation Étape par Étape

### Étape 1: Configuration Initiale
```bash
# Configuration des outils App Store
./setup_appstore.sh
```

### Étape 2: Configuration Xcode
1. Ouvrez Xcode
2. Sélectionnez votre projet `iosApp/VetNutri.xcodeproj`
3. Allez dans **"Signing & Capabilities"**
4. Sélectionnez votre équipe de développement
5. Choisissez **"iOS Distribution"** comme certificat de signature
6. Sélectionnez votre profil de provisioning de distribution

### Étape 3: Modifier les Configurations

#### `exportOptions.plist`
Modifiez ce fichier avec vos vraies valeurs :
```xml
<key>teamID</key>
<string>VOTRE_TEAM_ID</string>
<key>provisioningProfiles</key>
<dict>
    <key>fr.vetbrain.vetnutri_mp</key>
    <string>VetNutri MP Distribution</string>
</dict>
```

#### `deploy_appstore.sh` (optionnel pour upload automatique)
Décommentez et configurez cette section :
```bash
xcrun altool --upload-app -f "$IPA_PATH" -t ios \
    -u "your-apple-id@example.com" \
    -p "your-app-specific-password"
```

### Étape 4: Déploiement
```bash
# Déploiement complet
./deploy_appstore.sh
```

## 📦 Fichiers Générés

Après exécution réussie :

```
build/
├── VetNutriMP.xcarchive          # Archive Xcode
├── VetNutriMP.ipa               # IPA pour App Store
└── iosApp/                      # Projet Xcode modifié

composeApp/build/bin/iosArm64/
└── releaseFramework/
    └── ComposeApp.framework     # Framework Kotlin Native
```

## ☁️ Upload vers App Store Connect

### Option 1: Interface Web (Recommandé)
1. Allez sur [App Store Connect](https://appstoreconnect.apple.com)
2. Sélectionnez votre app
3. Cliquez sur **"+"** pour ajouter une nouvelle version
4. Upload l'IPA via **"TestFlight & App Store Connect"**

### Option 2: Ligne de Commande (Avancé)
```bash
# Avec altool (nécessite configuration)
xcrun altool --upload-app -f "build/VetNutriMP.ipa" -t ios \
    -u "your-apple-id" -p "your-password"

# Avec Transporter (application Apple)
open -a Transporter "build/VetNutriMP.ipa"
```

## 🔧 Configuration Avancée

### Variables à Personnaliser

Dans `deploy_appstore.sh`, modifiez ces variables selon votre projet :

```bash
APP_NAME="VetNutriMP"                    # Nom de votre app
BUNDLE_ID="fr.vetbrain.vetnutri_mp"      # Bundle ID
SCHEME_NAME="VetNutri"                   # Nom du schéma Xcode
IOS_PROJECT_PATH="iosApp"                # Chemin projet iOS
```

### Mémoire et Performance

Si vous avez encore des problèmes de mémoire :

```bash
# Modifier dans deploy_appstore.sh
export GRADLE_OPTS="-Xmx3g -XX:MaxMetaspaceSize=1g"
export JAVA_OPTS="-Xmx3g -XX:MaxMetaspaceSize=1g"
```

## 🚨 Dépannage

### Problème: "No signing certificate found"
**Solution**: Vérifiez votre certificat de distribution dans Xcode

### Problème: "Provisioning profile doesn't match"
**Solution**: Vérifiez que votre profil de provisioning correspond à votre certificat

### Problème: "Authentication failed" (App Store Connect)
**Solution**: Vérifiez vos credentials API et les permissions de la clé

### Problème: Framework Kotlin Native non trouvé
**Solution**: Exécutez d'abord `./compile_xcode.sh` manuellement

## 📚 Ressources Utiles

- [Guide de Distribution Apple](https://developer.apple.com/app-store/distribution/)
- [App Store Connect](https://appstoreconnect.apple.com)
- [Certificats Apple](https://developer.apple.com/account/resources/certificates/)
- [Profils de Provisioning](https://developer.apple.com/account/resources/profiles/)
- [TestFlight](https://testflight.apple.com)

## ✅ Résumé

1. ✅ Configurez vos certificats dans Xcode
2. ✅ Modifiez `exportOptions.plist` avec vos vraies valeurs
3. ✅ Exécutez `./deploy_appstore.sh`
4. ✅ Upload l'IPA via App Store Connect
5. ✅ Soumettez pour review

**Votre application VetNutri MP est maintenant prête pour l'App Store !** 🎉


