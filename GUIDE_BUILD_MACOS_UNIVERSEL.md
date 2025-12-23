# 🍎 Guide: Build macOS Universel (ARM64 + x86_64)

## Problème

Sur un Mac M1/M2/M3, les builds Compose Desktop sont compilés pour ARM64 uniquement, ce qui ne fonctionne pas sur Mac Intel (x86_64).

## Solutions

### Option 1: Compiler les deux versions séparément (Recommandé)

La solution la plus simple est de compiler deux DMG séparés et de les distribuer ensemble.

#### Sur Mac M1 (ARM64):
```bash
./gradlew clean :composeApp:packageDmg --no-daemon
# DMG généré: composeApp/build/compose/binaries/main/dmg/VetNutriMP-3.2.11.dmg
# Renommer en: VetNutriMP-3.2.11-arm64.dmg
```

#### Sur Mac Intel (x86_64):
```bash
./gradlew clean :composeApp:packageDmg --no-daemon
# DMG généré: composeApp/build/compose/binaries/main/dmg/VetNutriMP-3.2.11.dmg
# Renommer en: VetNutriMP-3.2.11-x86_64.dmg
```

#### Distribution:
- Distribuez les deux DMG sur votre site web
- Les utilisateurs choisissent celui qui correspond à leur Mac
- macOS détecte automatiquement l'architecture

### Option 2: Utiliser le script de compilation universelle

Le script `compile_macos_universal.sh` tente de créer un binaire universel:

```bash
./compile_macos_universal.sh
```

**Limitations:**
- La compilation croisée x86_64 sur M1 peut échouer
- Nécessite Rosetta installé
- Les binaires Skiko natifs peuvent ne pas se fusionner correctement

### Option 3: Utiliser CI/CD (GitHub Actions)

Créer un workflow GitHub Actions qui compile les deux architectures:

```yaml
# .github/workflows/build-macos.yml
name: Build macOS Universal

on:
  push:
    tags:
      - 'v*'

jobs:
  build:
    strategy:
      matrix:
        arch: [arm64, x86_64]
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup JDK
        uses: actions/setup-java@v3
        with:
          java-version: '11'
          distribution: 'temurin'
      - name: Build DMG
        run: ./gradlew :composeApp:packageDmg --no-daemon
      - name: Upload DMG
        uses: actions/upload-artifact@v3
        with:
          name: dmg-${{ matrix.arch }}
          path: composeApp/build/compose/binaries/main/dmg/*.dmg
```

### Option 4: Compiler avec Rosetta (Expérimental)

Sur Mac M1, vous pouvez essayer de compiler x86_64 avec Rosetta:

```bash
# Installer Rosetta si nécessaire
softwareupdate --install-rosetta

# Compiler avec arch -x86_64
arch -x86_64 ./gradlew :composeApp:packageDmg --no-daemon
```

**Note:** Cela peut ne pas fonctionner car Gradle et les dépendances natives peuvent avoir des problèmes avec Rosetta.

## Recommandation

**Pour l'instant, utilisez l'Option 1** (deux DMG séparés):
- ✅ Simple et fiable
- ✅ Pas de problèmes de compatibilité
- ✅ Les utilisateurs choisissent leur version
- ✅ Fonctionne à 100%

**Pour le futur:**
- Surveillez les mises à jour de Compose Desktop pour le support natif des binaires universels
- Utilisez CI/CD pour automatiser la compilation des deux architectures

## Vérification de l'architecture d'un DMG

Pour vérifier l'architecture d'un binaire dans le DMG:

```bash
# Monter le DMG
hdiutil attach VetNutriMP-3.2.11.dmg

# Vérifier l'architecture du binaire
file "/Volumes/VetNutriMP/VetNutriMP.app/Contents/MacOS/VetNutriMP"

# Démontage
hdiutil detach "/Volumes/VetNutriMP"
```

Les résultats possibles:
- `arm64`: Mac Apple Silicon uniquement
- `x86_64`: Mac Intel uniquement
- `universal binary`: Les deux architectures (idéal)





