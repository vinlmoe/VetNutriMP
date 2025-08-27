# Configuration des Icônes - Kotlin Multiplatform

Ce document explique comment configurer les icônes de votre application VetNutri MP sur toutes les plateformes supportées.

## Vue d'ensemble

Votre application Kotlin Multiplatform supporte trois plateformes principales :
- **Android** : Icônes dans `composeApp/src/androidMain/res/`
- **iOS** : Icônes dans `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/`
- **Desktop** : Icônes dans `composeApp/src/desktopMain/resources/`

## 1. Android ✅ (Déjà configuré)

### Structure actuelle
```
composeApp/src/androidMain/res/
├── drawable/
│   └── ic_launcher_background.xml
├── mipmap-hdpi/
│   ├── ic_launcher.png
│   └── ic_launcher_round.png
├── mipmap-mdpi/
├── mipmap-xhdpi/
├── mipmap-xxhdpi/
└── mipmap-xxxhdpi/
```

### Configuration dans AndroidManifest.xml
```xml
<application
    android:icon="@drawable/ic_launcher_background"
    android:roundIcon="@mipmap/ic_launcher"
    ...>
```

### Pour modifier l'icône Android
1. Remplacez les fichiers dans chaque dossier `mipmap-*`
2. Utilisez Android Studio pour générer automatiquement toutes les tailles
3. Ou utilisez des outils comme [Android Asset Studio](https://romannurik.github.io/AndroidAssetStudio/)

## 2. iOS ✅ (Déjà configuré)

### Structure actuelle
```
iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/
├── Contents.json
└── app-icon-1024.png
```

### Configuration
- **Format** : PNG 1024x1024 pixels
- **Fichier** : `app-icon-1024.png`

### Pour modifier l'icône iOS
1. Remplacez `app-icon-1024.png` par votre nouvelle icône
2. Assurez-vous qu'elle fait exactement 1024x1024 pixels
3. Utilisez Xcode pour prévisualiser l'icône

## 3. Desktop ❌ (À configurer)

### Structure à créer
```
composeApp/src/desktopMain/resources/
├── icon.icns    (macOS)
├── icon.ico     (Windows)
└── icon.png     (Linux)
```

### Configuration dans build.gradle.kts
```kotlin
compose.desktop {
    application {
        nativeDistributions {
            macOS {
                iconFile.set(project.file("src/desktopMain/resources/icon.icns"))
            }
            windows {
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
            }
            linux {
                iconFile.set(project.file("src/desktopMain/resources/icon.png"))
            }
        }
    }
}
```

## Génération automatique des icônes

### Prérequis
- **ImageMagick** : `brew install imagemagick`
- **macOS** : Pour générer les fichiers `.icns`

### Utilisation du script
```bash
# Rendre le script exécutable
chmod +x scripts/generate_icons.sh

# Générer toutes les icônes à partir d'une image source
./scripts/generate_icons.sh chemin/vers/votre/icone.png
```

### Exemple
```bash
./scripts/generate_icons.sh assets/logo_vetnutri.png
```

## Formats d'icônes par plateforme

| Plateforme | Format | Taille recommandée | Outils |
|------------|--------|-------------------|---------|
| Android    | PNG    | Multiple (mdpi, hdpi, xhdpi, etc.) | Android Studio, Asset Studio |
| iOS        | PNG    | 1024x1024        | Xcode, Preview |
| macOS      | ICNS   | 1024x1024        | Icon Composer, iconutil |
| Windows    | ICO    | 256x256          | IcoFX, GIMP |
| Linux      | PNG    | 512x512          | GIMP, Inkscape |

## Vérification et test

### 1. Reconstruire le projet
```bash
./gradlew clean
./gradlew :composeApp:build
```

### 2. Tester sur chaque plateforme
- **Android** : Déployer sur émulateur/appareil
- **iOS** : Déployer sur simulateur/appareil
- **Desktop** : Exécuter `./gradlew :composeApp:run`

### 3. Vérifier les icônes
- **Android** : Icône visible dans le launcher
- **iOS** : Icône visible sur l'écran d'accueil
- **Desktop** : Icône visible dans le dock/taskbar

## Dépannage

### Problèmes courants

#### Icône Android non visible
- Vérifiez que `ic_launcher.png` existe dans tous les dossiers `mipmap-*`
- Assurez-vous que le manifest pointe vers les bons fichiers

#### Icône iOS non visible
- Vérifiez que `app-icon-1024.png` fait exactement 1024x1024 pixels
- Nettoyez et reconstruisez le projet dans Xcode

#### Icône Desktop non visible
- Vérifiez que les fichiers d'icônes existent dans `desktopMain/resources/`
- Assurez-vous que le `build.gradle.kts` pointe vers les bons fichiers

### Logs et erreurs
```bash
# Voir les erreurs de build
./gradlew :composeApp:build --info

# Nettoyer le projet
./gradlew clean
```

## Ressources utiles

- [Android Asset Studio](https://romannurik.github.io/AndroidAssetStudio/)
- [Icon Composer](https://developer.apple.com/library/archive/documentation/GraphicsAnimation/Conceptual/HighResolutionOSX/Optimization/Optimization.html)
- [IcoFX](https://icofx.ro/)
- [GIMP](https://www.gimp.org/)
- [Inkscape](https://inkscape.org/)

## Support

Pour toute question ou problème avec la configuration des icônes, consultez :
1. Ce document
2. Les logs de build Gradle
3. La documentation officielle de Compose Multiplatform
