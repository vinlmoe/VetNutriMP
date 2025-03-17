# VetNutri_MP

VetNutri_MP est une application multiplateforme de nutrition vétérinaire développée avec Compose Multiplatform. Elle permet aux vétérinaires de gérer leurs patients, d'analyser leurs besoins nutritionnels et de créer des rations alimentaires adaptées.

## Architecture du projet

L'application est structurée selon le modèle MVVM (Model-View-ViewModel) avec une approche modulaire :

```
fr.vetbrain.vetnutri_mp/
├── View/           # Vues Compose (écrans principaux)
├── ViewModel/      # ViewModels (logique de présentation)
├── Repository/     # Repositories (accès aux données)
├── DataBase/       # Gestion de la base de données
├── Data/           # Modèles de données
├── Components/     # Composants UI réutilisables
├── Theme/          # Styles et thèmes visuels
├── Utils/          # Fonctions utilitaires
├── Enumer/         # Types énumérés
├── Localization/   # Ressources de localisation
└── Interface/      # Interfaces et contrats
```

## Flux de données

Le flux de données dans l'application suit le pattern MVVM :

1. **Vue** : Affiche l'interface utilisateur et capture les interactions
2. **ViewModel** : Traite les événements, exécute la logique métier et maintient l'état
3. **Repository** : Gère l'accès aux données, servant d'abstraction pour les sources de données
4. **DataBase** : Stocke les données de manière persistante

## Principales fonctionnalités

### Gestion des animaux

- Enregistrement des animaux avec leurs informations (espèce, race, âge, etc.)
- Suivi de l'évolution du poids
- Gestion des pathologies et traitements

### Consultations

- Création et modification de consultations
- Évaluation de l'état corporel (BCS/MCS)
- Suivi des paramètres physiologiques

### Rations alimentaires

- Création de rations personnalisées
- Ajout d'aliments avec quantités
- Analyse nutritionnelle en temps réel
- Comparaison avec les besoins spécifiques

### Base de données alimentaire

- Consultation et recherche d'aliments
- Ajout et modification d'aliments
- Visualisation des valeurs nutritionnelles

### Import/Export de données

- Importation d'animaux et d'aliments depuis des fichiers JSON
- Exportation des données pour partage ou sauvegarde

## Architecture multiplateforme

L'application utilise Compose Multiplatform pour partager la majorité du code entre les différentes plateformes :

- **Code partagé** : Logique métier, modèles de données, interface utilisateur
- **Code spécifique à la plateforme** : Fonctionnalités natives, intégrations système

## Modules principaux et responsabilités

### View

Les vues définissent l'interface utilisateur de l'application à l'aide de Compose Multiplatform. Elles observent les données exposées par les ViewModels et leur délèguent les actions utilisateur.

[En savoir plus sur les vues](View/README.md)

### ViewModel

Les ViewModels servent d'intermédiaires entre les vues et les données. Ils exposent les données sous forme d'états observables et implémentent la logique métier liée à l'interface utilisateur.

[En savoir plus sur les ViewModels](ViewModel/README.md)

### Repository

Les repositories fournissent une abstraction de l'accès aux données, centralisant la logique d'accès et simplifiant l'interaction avec les sources de données.

[En savoir plus sur les repositories](Repository/README.md)

### DataBase

Ce module gère la persistance des données à l'aide de SQLite et SQLDelight, définissant les entités, les requêtes et les opérations de base de données.

[En savoir plus sur la base de données](DataBase/README.md)

### Data

Les modèles de données représentent les entités métier manipulées par l'application, comme les animaux, les aliments et les rations.

[En savoir plus sur les modèles de données](Data/README.md)

### Components

Les composants sont des éléments d'interface utilisateur réutilisables qui permettent de construire les vues de manière cohérente et modulaire.

[En savoir plus sur les composants](Components/README.md)

### Theme

Le thème définit l'apparence visuelle de l'application, garantissant une cohérence esthétique sur toutes les plateformes.

[En savoir plus sur le thème](Theme/README.md)

### Utils

Les utilitaires fournissent des fonctionnalités communes et réutilisables dans toute l'application, comme l'importation de données ou les calculs nutritionnels.

[En savoir plus sur les utilitaires](Utils/README.md)

### Enumer

Les types énumérés définissent des ensembles de valeurs prédéfinies pour différents aspects du domaine nutritionnel vétérinaire.

[En savoir plus sur les énumérations](Enumer/README.md)

## Dépendances principales

- **Compose Multiplatform** : Framework UI multiplateforme
- **Kotlin Coroutines & Flow** : Programmation asynchrone et réactive
- **SQLDelight** : ORM SQL multiplateforme
- **Kotlinx Serialization** : Sérialisation JSON
- **Koin** : Injection de dépendances

## Démarrage rapide

Pour exécuter l'application :

```bash
./gradlew run
```

Pour compiler pour une plateforme spécifique :

```bash
# Android
./gradlew :composeApp:assembleDebug

# Desktop
./gradlew :composeApp:packageDistributionForCurrentOS
``` 