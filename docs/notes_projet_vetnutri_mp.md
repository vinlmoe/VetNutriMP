# Note sur le projet VetNutri MP

## Architecture générale
- Application Kotlin Multiplatform avec Compose Multiplatform
- Architecture MVVM (Model-View-ViewModel)
- Base de données SQLite avec Room sur Android 
- Structure modulaire avec séparation claire des responsabilités

## Principaux répertoires
- `Data/`: Modèles de données (AnimalEv, AlimentEv, BiblioRef, etc.)
- `Repository/`: Accès aux données (DatabaseAnimalRepository, DatabaseFoodRepository)
- `DataBase/`: Couche de persistance (Entity, AppDatabase, Mappers)
- `ViewModel/`: Logique de présentation
- `View/`: Interface utilisateur avec Compose
- `Enumer/`, `Theme/`, `Components/`, `Utils/`: Support et éléments communs

## État actuel du projet
- Projet en cours de migration Java → Kotlin Multiplatform
- Problème identifié avec la classe BiblioRef, nouvellement convertie
- Erreurs du compilateur signalant des problèmes avec kotlinx.serialization.Serializable

## Points d'attention
- Résoudre l'erreur d'importation Serializable (vérifier les dépendances)
- Maintenir la cohérence entre JSON (BiblioRefJson) et modèles (BiblioRef)
- S'assurer que les conversions Java → Kotlin respectent les conventions en français

## Prochaines étapes possibles
- Corriger les erreurs de compilation (BiblioRef et dépendances)
- Poursuivre la conversion des classes Java restantes
- Vérifier l'architecture multiplateforme (Android, Desktop, iOS) 