# VetNutri MP

**Application multiplateforme de gestion nutritionnelle vétérinaire**

VetNutri MP permet aux vétérinaires et nutritionnistes de calculer et d'analyser les rations alimentaires des animaux de compagnie. Elle couvre la sélection des aliments, le calcul des besoins énergétiques, l'analyse nutritionnelle détaillée et l'export de rapports.

> Version actuelle : **3.3.18**

---

## Plateformes

| Plateforme | Minimum | Détail |
|------------|---------|--------|
| Android | API 28 (Android 9) | arm64-v8a · armeabi-v7a · x86_64 · 16 KB page size |
| iOS | — | iosArm64 · iosSimulatorArm64 · iosX64 |
| Desktop | JVM 11 | DMG (macOS) · EXE (Windows) · DEB (Linux) |

---

## Nouveautés 3.3.18

- Mise à jour de 12 fiches alimentaires Royal Canin, Hill’s et Osalia, avec références sources.
- Vérification des unités et comparaison des produits proches ; les écarts non résolus sont
  documentés dans le [rapport de cohérence](docs/audit_nutrition_3.3.18.md).
- Police des cartes de ration et d’ingrédients réduite de 10 %.
- Versions Android, iOS, Desktop et jeux de données harmonisées en **3.3.18 (318)**.

## Nouveautés 3.3.15

- **Plans évolutifs par ration** : déclinez une ration en étapes au poids et aux variables
  énergétiques propres, puis comparez-les dans une synthèse et une ordonnance communes.
- **Analyse plus fiable** : comparez les rations actuelles et proposées avec une moyenne
  pondérée, et visualisez la couverture des objectifs nutritionnels par référence.
- **Ajustement optimisé** : conservez, si nécessaire, une part cible d'énergie issue des
  aliments complets lors de l'ajustement automatique des quantités.

---

## Stack technique

| Domaine | Bibliothèque | Version |
|---------|-------------|---------|
| UI | Compose Multiplatform | 1.7.0 |
| Langage | Kotlin Multiplatform | 2.2.20 |
| UI Material | Material 1 + Material 3 | 1.7.8 / 1.3.1 |
| Base de données | Room + SQLite Bundled | 2.7.0 / 2.5.0 |
| Préférences | DataStore | 1.1.2 |
| Réseau | Ktor Client | 3.0.3 |
| Sérialisation | Kotlin Serialization JSON | 1.8.0 |
| Coroutines | kotlinx.coroutines | 1.7.3 |
| ViewModel | AndroidX Lifecycle | 2.8.4 |
| Graphiques | KoalaPlot | 0.9.0 |
| QR Code | QR-Kit | 3.0.0 |
| PDF (Desktop) | OpenHTML to PDF | 1.0.10 |
| Tests | Kotlin Test · JUnit 5 · AssertK | — |

---

## Fonctionnalités

- **Gestion des animaux** — création, édition, suivi multi-espèces (chien, chat, lapin, NAC…)
- **Base d'aliments** — base locale filtrée par espèce, groupe, indication et nutriments
- **Calcul énergétique** — poids métabolique, BEE, besoin total selon références bibliographiques
- **Analyse nutritionnelle** — tableaux détaillés et graphiques (macros, minéraux, vitamines, lipides, ratios)
- **Plans évolutifs** — une ration parent peut décliner plusieurs étapes avec poids, besoins et variables énergétiques propres ; synthèse et ordonnance par étape
- **Analyse groupée** — comparaison pondérée des rations actuelles et proposées
- **Ajustement sous contraintes** — optimisation des quantités nutritionnelles, avec contrôle de la part énergétique des aliments complets
- **Recettes** — création et comparaison de rations
- **Consultations & examens** — suivi des consultations, notation des exercices
- **Export / Import** — JSON (format API), PDF (Desktop), QR Code, backup/restore
- **Localisation** — français par défaut, fichiers JSON extensibles
- **Chromebook** — fenêtrage libre, navigation clavier (Tab/Enter/Escape), indicateurs de focus

---

## Architecture

```
fr.vetbrain.vetnutri_mp/
├── View/            # Écrans Compose (20+ vues)
│   └── components/  # Composants réutilisables (FoodSearchComponent, …)
├── ViewModel/       # ViewModels MVVM + StateFlow
├── Repository/      # Couche d'abstraction données
├── DataBase/        # Room — AppDatabase, DAOs, Entités, Converters
├── Data/            # Modèles métier (AlimentEv, AnimalEv, RationAnalyzer, …)
├── Service/         # Services applicatifs (démarrage, calculs)
├── Components/      # Composants Compose partagés (AppTextField, AutocompleteTextField, …)
├── Enumer/          # Enums domaine (Espece, Sex, FoodKind, AlimIndic, …)
├── Theme/           # Couleurs, tailles, icônes
├── Localization/    # Gestion i18n
├── Utils/           # Utilitaires multiplateforme
└── Export/          # Types et logique d'export
```

---

## Build

### Prérequis

- JDK 11+
- Android SDK API 36
- Xcode (iOS uniquement)
- Gradle via wrapper (`./gradlew`)

### Configuration JsonBin (Android / iOS / Desktop)

Les clés API JsonBin sont injectées au build dans `commonMain`, donc partagées par toutes les plateformes.

1. Créer/éditer `local.properties` (à la racine du repo, fichier ignoré par Git):

```properties
jsonbin.create.key=VOTRE_CLE_ECRITURE
jsonbin.read.key=VOTRE_CLE_LECTURE
```

2. Alternative CI/CD (sans `local.properties`):

```bash
export JSONBIN_CREATE_KEY="VOTRE_CLE_ECRITURE"
export JSONBIN_READ_KEY="VOTRE_CLE_LECTURE"
```

Priorité actuelle:
- `local.properties`
- puis variables d’environnement (`JSONBIN_CREATE_KEY`, `JSONBIN_READ_KEY`)

### Commandes

```bash
# Android
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:assembleRelease

# Desktop (run)
./gradlew :composeApp:run

# Desktop (bundle)
./gradlew :composeApp:packageReleaseDistributable

# iOS (framework pour Xcode)
./gradlew :composeApp:packForXcode

# macOS universel (binaire signé)
./compile_and_sign_macos.sh

# Tests
./gradlew :composeApp:testDebugUnitTest
```

---

## Documentation complémentaire

| Fichier | Contenu |
|---------|---------|
| `CHANGELOG.md` | Historique des versions |
| `PERFORMANCE_IMPROVEMENTS.md` | Optimisations runtime et build |
| `GUIDE_BUILD_MACOS_UNIVERSEL.md` | Build universel macOS |
| `GUIDE_SIGNATURE_MACOS.md` | Signature et notarisation |
| `README_APPSTORE_DEPLOYMENT.md` | Déploiement App Store iOS |
| `GUIDE_COMPATIBILITE_16KB.md` | Compatibilité Android 15+ (16 KB pages) |

---

## Contribution

1. Créer une branche depuis `develop`
2. Implémenter la fonctionnalité avec tests unitaires
3. Vérifier que `./gradlew :composeApp:testDebugUnitTest` passe
4. Ouvrir une pull request

---

## Licence

Ce projet est la propriété de **VetBrain**. Tous droits réservés.
