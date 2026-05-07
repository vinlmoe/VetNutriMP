# Guide de compatibilité avec les pages mémoire de 16KB

## Problème
Google Play rejette l'application avec le message : "Votre appli ne prend pas en charge les tailles de page de mémoire de 16 ko"

## Solutions appliquées

### 1. Configuration NDK
- NDK version 27.0.12077987 (compatible 16KB)
- Configuration dans `gradle.properties`

### 2. Configuration du packaging
- `useLegacyPackaging = false` pour le nouveau format de packaging
- `keepDebugSymbols` pour préserver l'alignement ELF des bibliothèques natives

### 3. Mise à jour des dépendances
- `androidx.sqlite:sqlite-bundled` mis à jour vers la version stable 2.5.0

### 4. Filtrage des ABI
- ABI compatibles configurés : `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`

## Vérification de l'alignement

### Méthode 1 : Script automatique
```bash
./check_16kb_alignment.sh
```

Le script va :
1. Trouver automatiquement votre APK release
2. Extraire et analyser toutes les bibliothèques natives (.so)
3. Vérifier l'alignement de chaque bibliothèque
4. Identifier les bibliothèques problématiques

### Méthode 2 : Android Studio APK Analyzer
1. Build → Analyze APK
2. Sélectionnez votre APK release
3. Vérifiez les fichiers `.so` dans `lib/`
4. Identifiez les bibliothèques non alignées

### Méthode 3 : zipalign
```bash
zipalign -c -P 16 -v composeApp/build/outputs/apk/release/composeApp-release.apk
```

## Si le problème persiste

### Option 1 : Exclure armeabi-v7a
Si `armeabi-v7a` cause des problèmes, modifiez `composeApp/build.gradle.kts` :

```kotlin
ndk {
    // Exclure armeabi-v7a si les bibliothèques ne sont pas alignées
    abiFilters += listOf("arm64-v8a", "x86", "x86_64")
    // Retirer "armeabi-v7a" de la liste
}
```

**Note** : Cela réduira la compatibilité avec les anciens appareils 32-bit ARM, mais la plupart des appareils modernes utilisent `arm64-v8a`.

### Option 2 : Identifier et mettre à jour les dépendances problématiques
1. Exécutez `./check_16kb_alignment.sh` pour identifier les bibliothèques problématiques
2. Vérifiez si des versions plus récentes sont disponibles
3. Contactez les mainteneurs si nécessaire

### Option 3 : Vérifier les dépendances natives
Bibliothèques qui peuvent contenir du code natif :
- `androidx.sqlite:sqlite-bundled` ✅ (mis à jour)
- `network.chaintech:qr-kit` ⚠️ (vérifier la compatibilité)
- `androidx.room` (utilise sqlite-bundled)

## Test sur appareil 16KB

### Vérifier la taille de page d'un appareil
```bash
adb shell getconf PAGE_SIZE
```
Doit retourner `16384` pour les pages de 16KB.

### Tester l'application
1. Utilisez un émulateur Android 15+ configuré avec 16KB
2. Ou testez sur un appareil physique compatible (Pixel 8+, etc.)

## Références
- [Documentation Android - 16KB Page Sizes](https://developer.android.com/guide/practices/page-sizes)
- [Blog Android Developers - Transition to 16KB](https://android-developers.googleblog.com/2025/07/transition-to-16-kb-page-sizes-android-apps-games-android-studio.html)

