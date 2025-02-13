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
    - `Localization/` : Système de localisation
      - Gestion des traductions multilingues
      - Clés de localisation constantes
      - Gestionnaire de ressources

  - Les autres dossiers (`iosMain`, `androidMain`, `desktopMain`) contiennent le code spécifique à chaque plateforme.

* `/iosApp` contient le point d'entrée iOS et le code SwiftUI spécifique.

## Fonctionnalités Principales

- Gestion des animaux (création, modification)
- Suivi nutritionnel
- Gestion des équations et références bibliographiques
- Interface multiplateforme moderne
- Support multilingue complet

## Prérequis Techniques

- Kotlin Multiplatform
- Compose Multiplatform pour l'interface utilisateur
- Room pour la persistance des données
- Kotlinx.serialization pour la sérialisation JSON
- Kotlin Test pour les tests unitaires

## Installation

[Instructions d'installation à venir]

## Développement

Pour contribuer au projet :

1. Clonez le dépôt
2. Ouvrez le projet dans IntelliJ IDEA ou Android Studio
3. Synchronisez les dépendances Gradle

### Tests

Le projet inclut une suite complète de tests unitaires, notamment :

- Tests de localisation vérifiant la présence et la cohérence des traductions
- Tests pour chaque catégorie de clés de localisation :
  - Clés générales
  - Clés liées aux animaux
  - Clés des espèces
  - Clés de nutrition
  - Clés des vitamines
  - Clés des minéraux
  - Clés des unités
  - Clés de consultation
  - Clés de ration
  - Clés d'erreur

Pour exécuter les tests :
```bash
./gradlew test
```

### Localisation

Le système de localisation supporte plusieurs langues et utilise un système de clés constantes pour éviter les erreurs de frappe. Les fichiers de traduction sont au format JSON et se trouvent dans le dossier `resources`. Les principales catégories de traduction incluent :

- Interface générale
- Termes liés aux animaux
- Espèces animales
- Termes nutritionnels
- Vitamines et minéraux
- Unités de mesure
- Termes de consultation
- Messages d'erreur

Pour ajouter une nouvelle langue :
1. Créez un nouveau fichier `strings_[code_langue].json` dans le dossier resources
2. Copiez la structure du fichier `strings_fr.json`
3. Traduisez toutes les valeurs
4. Ajoutez la langue dans le `LocalizationManager`

## Documentation

Pour plus d'informations sur les technologies utilisées :
- [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)

## Licence

[Informations sur la licence à venir]