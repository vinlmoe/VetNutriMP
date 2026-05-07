# 🔐 Guide de Configuration de la Notarisation macOS

## ✅ Oui, la notarisation est déjà intégrée !

Le script `compile_and_sign_macos.sh` supporte déjà la notarisation. Il suffit de configurer vos credentials.

## 🎯 Pourquoi Notariser ?

La notarisation permet à macOS de vérifier automatiquement que votre application est sûre :

- ✅ **Évite les avertissements Gatekeeper** ("App non vérifiée")
- ✅ **Installation fluide** pour les utilisateurs
- ✅ **Confiance accrue** dans votre application
- ✅ **Recommandé par Apple** pour toutes les applications distribuées hors App Store

## 📋 Étapes pour Activer la Notarisation

### Étape 1: Générer un Mot de Passe Spécifique à l'Application

1. Allez sur https://appleid.apple.com
2. Connectez-vous avec votre Apple ID (celui de votre compte développeur)
3. Allez dans la section **"Sécurité"**
4. Cliquez sur **"Mots de passe spécifiques aux applications"**
5. Cliquez sur **"Générer un mot de passe"**
6. Donnez-lui un nom (ex: "VetNutri MP Notarization")
7. **Copiez le mot de passe généré** (format: `xxxx-xxxx-xxxx-xxxx`)
   - ⚠️ Vous ne pourrez le voir qu'une seule fois !

### Étape 2: Configurer les Credentials

Vous avez **deux options** pour configurer vos credentials :

#### Option 1: Fichier de Configuration (Recommandé) ✅

Créez un fichier `.notarization_config` à la racine du projet :

```bash
# .notarization_config
APPLE_ID=votre@email.com
APP_SPECIFIC_PASSWORD=xxxx-xxxx-xxxx-xxxx
```

**Avantages:**
- ✅ Credentials pas dans le script (plus sûr)
- ✅ Facile à ignorer dans Git (ajoutez `.notarization_config` au `.gitignore`)
- ✅ Réutilisable

**Ajoutez au .gitignore:**
```bash
echo ".notarization_config" >> .gitignore
```

#### Option 2: Modifier le Script Directement

Modifiez les variables dans `compile_and_sign_macos.sh` :

```bash
APPLE_ID="votre@email.com"
APP_SPECIFIC_PASSWORD="xxxx-xxxx-xxxx-xxxx"
```

### Étape 3: Tester la Notarisation

```bash
# Le script détectera automatiquement la configuration
./compile_and_sign_macos.sh
```

Le script va :
1. ✅ Compiler le DMG
2. ✅ Signer l'application et le DMG
3. ✅ Soumettre pour notarisation Apple
4. ✅ Attendre la validation (peut prendre 5-15 minutes)
5. ✅ Agrafer le ticket de notarisation

## 🔍 Vérification de la Notarisation

Après la notarisation, vous pouvez vérifier :

```bash
# Vérifier le ticket agrafé
xcrun stapler validate composeApp/build/compose/binaries/main/dmg/VetNutriMP-3.1.39-signed.dmg

# Voir les détails de la notarisation
spctl -a -vv -t install composeApp/build/compose/binaries/main/dmg/VetNutriMP-3.1.39-signed.dmg
```

## ⚠️ Dépannage

### Erreur: "Invalid credentials"

**Solution:** Vérifiez que :
- Votre Apple ID est correct
- Le mot de passe spécifique est correct (copié sans espaces)
- Votre compte développeur est actif

### Erreur: "notarytool not found"

**Solution:** Installez Xcode Command Line Tools :
```bash
xcode-select --install
```

### Erreur: "The signature is invalid"

**Solution:** Assurez-vous que :
- Le certificat "Developer ID Application" est installé
- L'application est correctement signée avant la notarisation

### La notarisation prend trop de temps

C'est normal ! La notarisation peut prendre **5 à 15 minutes**. Le script attend automatiquement avec `--wait`.

## 📝 Exemple de Fichier .notarization_config

```bash
# Configuration de notarisation pour VetNutri MP
# Ne pas commiter ce fichier dans Git !

APPLE_ID=sebastien.lefebvre@example.com
APP_SPECIFIC_PASSWORD=abcd-efgh-ijkl-mnop
```

## 🚀 Utilisation Complète

```bash
# 1. Créer le fichier de configuration (une seule fois)
cat > .notarization_config << EOF
APPLE_ID=votre@email.com
APP_SPECIFIC_PASSWORD=xxxx-xxxx-xxxx-xxxx
EOF

# 2. Ajouter au .gitignore
echo ".notarization_config" >> .gitignore

# 3. Compiler, signer et notariser
./compile_and_sign_macos.sh
```

## ✅ Résultat Final

Une fois notarisé, votre DMG :
- ✅ S'installe sans avertissements
- ✅ Passe Gatekeeper automatiquement
- ✅ Est vérifié par Apple comme sûr
- ✅ Prêt pour distribution professionnelle

## 📚 Ressources

- [Apple Notarization Guide](https://developer.apple.com/documentation/security/notarizing_macos_software_before_distribution)
- [App-Specific Passwords](https://appleid.apple.com)
- [Notarytool Documentation](https://developer.apple.com/documentation/security/notarizing_macos_software_before_distribution)

**Votre application sera signée ET notarisée, prête pour une distribution professionnelle !** 🎉

