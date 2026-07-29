# Configuration des Icônes Desktop

Ce dossier contient les icônes nécessaires pour la plateforme desktop de votre application Kotlin Multiplatform.

## Formats requis par plateforme

### macOS (.icns)
- **Format** : ICNS (Icon Container)
- **Taille recommandée** : 1024x1024 pixels
- **Outils** : 
  - Icon Composer (macOS)
  - Image2Icon
  - Online converters

### Windows (.ico)
- **Format** : ICO (Icon)
- **Tailles recommandées** : 16x16, 32x32, 48x48, 64x64, 128x128, 256x256 pixels
- **Outils** :
  - IcoFX
  - GIMP
  - Online converters

### Linux (.png)
- **Format** : PNG
- **Taille recommandée** : 512x512 pixels
- **Outils** : GIMP, Inkscape, ou tout éditeur d'image

## Création des icônes

1. **Créez une image source** de haute qualité (1024x1024 pixels minimum)
2. **Convertissez** vers le format approprié pour chaque plateforme
3. **Placez** les fichiers dans ce dossier :
   - `icon.icns` pour macOS
   - `icon.ico` pour Windows  
   - `icon.png` pour Linux

## Vérification

Après avoir ajouté les icônes, reconstruisez votre projet :
```bash
./gradlew :composeApp:build
```

## Note
Les icônes sont référencées dans `build.gradle.kts` dans la section `compose.desktop.nativeDistributions`.
