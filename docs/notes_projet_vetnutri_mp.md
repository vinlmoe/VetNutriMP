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

## Nouveaux composants
### Modèles de nutrition et références
- `NutrientRefP`: Référence nutritionnelle avec propriétés spécifiques (valeur, unité, bibliographie)
- `ReferenceEv`: Gestionnaire de références nutritionnelles avec mappages entre nutriments et valeurs
- `Reflevel`: Énumération des niveaux de référence (MIN, MAX, OPTIMIN, OPTIMAX)
- `UnitP`: Classe encapsulant les unités de mesure avec fonctions de conversion
- `UnitEnum`: Énumération des unités possibles (g, mg, UI, etc.) avec facteurs de conversion

### Interface utilisateur améliorée
- `CalculationTabsView`: Vue à onglets intégrant les équations, références bibliographiques et besoins nutritionnels
- `NutrientRequirementView`: Affichage des besoins nutritionnels pour différentes espèces
- `EquationListView` et `BiblioRefListView`: Vues réutilisables pour les listes d'équations et références

### Structure de navigation
- Menu à onglets permettant de naviguer entre les différentes sections de données nutritionnelles
- Écrans imbriqués avec gestion d'état pour l'édition des éléments

## État actuel du projet
- Projet en cours de migration Java → Kotlin Multiplatform
- Problèmes avec kotlinx.serialization.Serializable résolus en supprimant les annotations redondantes
- Navigation améliorée pour les données nutritionnelles avec une interface à onglets
- Ajout de la gestion des besoins nutritionnels de base pour chiens et chats

## Points d'attention
- Maintenir la cohérence entre les différentes représentations des nutriments et unités
- S'assurer que les relations entre ReferenceEv, NutrientRefP et autres classes sont clairement définies
- Veiller à la cohérence des énumérations entre les différents packages
- S'assurer que les conversions Java → Kotlin respectent les conventions en français

## Gestion des flux asynchrones
- Des problèmes de performances ont été identifiés dans la collecte des Flow
- Plusieurs optimisations ont été implémentées dans BiblioRefRepository et EquationViewModel
- Pattern recommandé: émission initiale du cache puis des données fraîches
- Utiliser firstOrNull() avec timeout plutôt que collect() pour les opérations ponctuelles
- Gérer systématiquement les erreurs avec catch pour éviter les crashs

## Prochaines étapes possibles
- Poursuivre l'intégration des fonctionnalités nutritionnelles avec données réelles
- Compléter la vue des besoins nutritionnels avec des données scientifiques précises
- Implémenter un système de validation des entrées pour les références nutritionnelles
- Améliorer l'ergonomie de la navigation entre les différentes sections de l'application
- Poursuivre la conversion des classes Java restantes 