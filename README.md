# VetNutri MP

VetNutri MP est une application multiplateforme (Android, iOS, Desktop) pour la gestion des rations alimentaires des animaux.

## Architecture

L'application est construite avec Kotlin Multiplatform et utilise :

- **Compose Multiplatform** pour l'interface utilisateur
- **SQLite** pour le stockage des données (via Room sur Android)
- **Kotlin Coroutines** pour la gestion de l'asynchrone
- **Kotlin Serialization** pour le traitement des données JSON

## Structure du Projet

```
fr.vetbrain.vetnutri_mp/
├── Data/                 # Classes de données (AlimentEv, AnimalEv, etc.)
├── DataBase/            # Couche d'accès aux données
│   ├── Dao/            # Interfaces DAO
│   └── Entity/         # Entités Room
├── Enumer/             # Énumérations (Espece, Sex, etc.)
├── Localization/       # Gestion de la localisation
├── Repository/         # Couche repository
├── Theme/              # Thème de l'application
├── View/               # Composants d'interface utilisateur
└── ViewModel/          # ViewModels
```

## Base de Données

### Tables Principales

- **animals** : Stockage des informations sur les animaux
- **foods** : Stockage des aliments
- **food_species** : Relations entre aliments et espèces
- **food_indications** : Relations entre aliments et indications

### DAOs

- **AnimalDao** : Gestion des opérations CRUD pour les animaux
- **FoodDao** : Gestion des opérations CRUD pour les aliments

## Tests

Les tests unitaires sont disponibles pour :

- Les entités de données
- Les DAOs
- Les ViewModels
- Les convertisseurs de données

## Localisation

L'application supporte la localisation avec :

- Fichiers de traduction au format JSON
- Gestion des langues par défaut (fr)
- Fallback automatique vers le français

## Contribution

Pour contribuer au projet :

1. Créer une branche pour votre fonctionnalité
2. Ajouter des tests unitaires
3. Vérifier que tous les tests passent
4. Soumettre une pull request

## Licence

Ce projet est la propriété de VetBrain. Tous droits réservés.