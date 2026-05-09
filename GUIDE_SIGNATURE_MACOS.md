# 🔐 Guide de Signature macOS - VetNutri MP

## ✅ Oui, vous pouvez signer votre application !

Même si votre application ne peut pas aller sur l'App Store macOS (car c'est une application JVM), vous pouvez **absolument la signer** pour une distribution hors App Store. Cela permet:

- ✅ **Éviter les avertissements de sécurité** macOS
- ✅ **Installer sans "App non vérifiée"**
- ✅ **Distribution professionnelle** via votre site web
- ✅ **Notarisation** par Apple (optionnelle mais recommandée)

## 📋 Types de Certificats

### Pour Distribution Hors App Store (Votre Cas)

**Certificat requis:** `Developer ID Application`

- ✅ Permet de signer des applications pour distribution hors App Store
- ✅ Compatible avec la notarisation Apple
- ✅ Évite les avertissements de sécurité macOS
- ❌ Ne permet PAS de soumettre à l'App Store

### Pour App Store (Pas Votre Cas)

**Certificat:** `Apple Distribution` ou `Mac App Distribution`

- ✅ Pour soumettre à l'App Store uniquement
- ❌ Ne fonctionne pas pour distribution hors App Store

## 🛠️ Étapes pour Obtenir le Certificat

### 1. Créer une CSR (Certificate Signing Request)

Sur votre Mac:

```bash
# Ouvrir l'application Trousseaux d'accès (Keychain Access)
open /Applications/Utilities/Keychain\ Access.app
```

1. Menu: **Trousseaux d'accès** → **Assistant de certification** → **Demander un certificat à une autorité de certification**
2. Entrez votre **adresse email** (celle de votre compte développeur)
3. Choisissez **"Enregistré sur le disque"**
4. Sauvegardez le fichier `.certSigningRequest`

### 2. Créer le Certificat sur le Portail Développeur

1. Allez sur https://developer.apple.com/account/resources/certificates/
2. Cliquez sur **"+"** pour créer un nouveau certificat
3. Sélectionnez **"Developer ID Application"** (⚠️ PAS "Apple Distribution")
4. Téléversez votre fichier `.certSigningRequest`
5. Téléchargez le certificat `.cer`

### 3. Installer le Certificat

Double-cliquez sur le fichier `.cer` téléchargé. Il s'installera automatiquement dans votre Trousseaux d'accès.

### 4. Vérifier l'Installation

```bash
security find-identity -v -p codesigning | grep "Developer ID"
```

Vous devriez voir quelque chose comme:
```
Developer ID Application: Sébastien Lefebvre (N8M75AVX29)
```

## 🚀 Utilisation du Script de Signature

Une fois le certificat installé, utilisez le script fourni:

```bash
# Le script détectera automatiquement votre certificat
./compile_and_sign_macos.sh
```

Le script va:
1. ✅ Compiler le DMG macOS
2. ✅ Signer l'application avec votre certificat
3. ✅ Signer le DMG lui-même
4. ✅ (Optionnel) Notariser le DMG si vous configurez APPLE_ID

## 📝 Configuration du Script

Modifiez ces variables dans `compile_and_sign_macos.sh` si nécessaire:

```bash
TEAM_ID="N8M75AVX29"  # Votre Team ID (déjà configuré)
APPLE_ID=""  # Pour la notarisation (optionnel)
APP_SPECIFIC_PASSWORD=""  # Pour la notarisation (optionnel)
```

Le script détectera automatiquement votre certificat "Developer ID Application".

## 🔄 Notarisation (Optionnelle mais Recommandée)

La notarisation permet à macOS de vérifier que votre application est sûre:

1. **Créer un mot de passe spécifique à l'application:**
   - Allez sur https://appleid.apple.com
   - Section "Sécurité" → "Mots de passe spécifiques aux applications"
   - Créez un nouveau mot de passe pour "App Store Connect API"

2. **Configurer dans le script:**
   ```bash
   APPLE_ID="votre@email.com"
   APP_SPECIFIC_PASSWORD="xxxx-xxxx-xxxx-xxxx"
   ```

3. **Le script notarisera automatiquement** votre DMG

## ⚠️ Utilisation Temporaire d'Apple Distribution

Si vous n'avez pas encore de certificat "Developer ID Application", le script peut utiliser temporairement "Apple Distribution", mais ce n'est **pas recommandé** pour distribution hors App Store.

## 📚 Ressources

- [Certificats Apple](https://developer.apple.com/account/resources/certificates/)
- [Code Signing Guide](https://developer.apple.com/documentation/security/code_signing_services)
- [Notarization Guide](https://developer.apple.com/documentation/security/notarizing_macos_software_before_distribution)

## ✅ Résumé

1. ✅ **Oui, vous pouvez signer** votre application macOS
2. ✅ Créez un certificat **"Developer ID Application"**
3. ✅ Utilisez le script `compile_and_sign_macos.sh`
4. ✅ Distribuez votre DMG signé via votre site web
5. ✅ Les utilisateurs pourront installer sans avertissements

**Votre application sera signée et prête pour distribution professionnelle !** 🎉

