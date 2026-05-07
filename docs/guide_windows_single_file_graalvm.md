# VetNutriMP Windows Single-File (GraalVM Native Image)

Ce guide décrit une tentative de build d'un `.exe` unique Windows via GraalVM Native Image.

## Statut

- Mode: expérimental
- Plateforme: Windows uniquement pour le build natif Windows
- Risque: élevé (Compose Desktop + Skiko + JNI peuvent casser le build ou le runtime)

## Prérequis (Windows)

1. Installer GraalVM JDK (version 21 recommandée).
2. Installer l'outil native-image:
   - `gu install native-image`
3. Vérifier:
   - `java -version`
   - `native-image --version`
4. Ouvrir un terminal à la racine du projet.

## Étape 1: Générer la configuration Native Image (agent)

Lancer l'application JVM avec l'agent pour capturer les besoins runtime (réflexion, JNI, ressources):

```powershell
.\gradlew.bat :composeApp:runDesktopWithNativeAgent
```

Puis manipuler l'application sur les écrans principaux avant de la fermer.

Les fichiers de config sont générés dans:

- `composeApp/build/native-image/config`

## Étape 2: Construire l'exécutable single-file

```powershell
.\gradlew.bat :composeApp:buildWindowsNativeImage
```

Sortie attendue:

- `composeApp/build/native/native-image/VetNutriMP.exe`

## Dépannage rapide

1. `native-image` introuvable:
   - vérifier que GraalVM est bien le JDK actif (`JAVA_HOME`) et que `native-image` est dans le `PATH`.
2. Erreurs réflexion/JNI:
   - relancer `runDesktopWithNativeAgent`, parcourir plus de fonctionnalités, puis rebuild.
3. Erreurs liées à Skiko/awt/swing:
   - limitation connue des applications UI complexes avec Native Image.
   - fallback recommandé: distribution portable dossier (`createDistributable`).

## Commandes utiles

```powershell
.\gradlew.bat :composeApp:desktopJar
.\gradlew.bat :composeApp:createDistributable
.\gradlew.bat :composeApp:runDesktopWithNativeAgent
.\gradlew.bat :composeApp:buildWindowsNativeImage --stacktrace
```

