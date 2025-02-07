# VetNutri MP

Ceci est un projet Kotlin Multiplatform ciblant Android, iOS et Desktop. L'application est conçue pour la gestion nutritionnelle vétérinaire.

## Structure du Projet

* `/composeApp` contient le code partagé entre les applications Compose Multiplatform.
  Il contient plusieurs sous-dossiers :
  - `commonMain` est destiné au code commun à toutes les plateformes, avec la structure suivante :
    - `Data/` : Classes de données et entités
      - Modèles de données (AnimalEv, BiblioRef, Equation, etc.)
      - Interfaces (Nutrient, Labelable)
      - Classes de transfert (AlimP, AnimP, etc.)
    - `Enumerise/` : Énumérations du domaine
      - Espèces animales (Espece)
      - Types de nutriments (NutrientVitam, MainNutrientEnum)
      - Autres énumérations (Sex, ConditionEnum, etc.)
    - `View/` : Composants d'interface utilisateur
      - Vues principales (CreateAnimalView)
      - Éléments réutilisables (ViewElements)
    - `ViewModel/` : Logique de présentation
      - Gestion des états (AnimalViewModel)
    - `Components/` : Composants réutilisables
      - Affichage de code (CodeView)
    - `Theme/` : Styles et thèmes
      - Couleurs (Colors)
    - `Serializers/` : Sérialiseurs personnalisés
      - Gestion des conversions JSON (DataSerializers)

  - Les autres dossiers (`iosMain`, `androidMain`, `desktopMain`) contiennent le code spécifique à chaque plateforme.

* `/iosApp` contient le point d'entrée iOS et le code SwiftUI spécifique.

## Fonctionnalités Principales

- Gestion des animaux (création, modification)
- Suivi nutritionnel
- Gestion des équations et références bibliographiques
- Interface multiplateforme moderne

## Prérequis Techniques

- Kotlin Multiplatform
- Compose Multiplatform pour l'interface utilisateur
- Room pour la persistance des données
- Kotlinx.serialization pour la sérialisation JSON

## Installation

[Instructions d'installation à venir]

## Développement

Pour contribuer au projet :

1. Clonez le dépôt
2. Ouvrez le projet dans IntelliJ IDEA ou Android Studio
3. Synchronisez les dépendances Gradle

## Documentation

Pour plus d'informations sur les technologies utilisées :
- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)

## Licence

[Informations sur la licence à venir]