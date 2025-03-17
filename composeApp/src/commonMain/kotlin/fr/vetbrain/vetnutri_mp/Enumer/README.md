# Dossier Enumer

Ce dossier contient les énumérations (types énumérés) utilisées dans l'application VetNutri_MP. Ces énumérations fournissent des ensembles de valeurs prédéfinies pour différents aspects du domaine nutritionnel vétérinaire.

## Énumérations principales

### Types d'éléments nutritionnels

- **Nutrient** (interface) : Interface de base pour tous les types de nutriments
- **NutrientMain** : Nutriments principaux (protéines, lipides, etc.)
- **NutrientLipid** : Types de lipides et acides gras
- **NutrientMin** : Minéraux (calcium, phosphore, etc.)
- **NutrientVitam** : Vitamines (A, B, C, D, etc.)
- **NutrientOther** : Autres nutriments
- **AAEnum** : Acides aminés

### Aliments et espèces

- **AlimIndic** : Indications alimentaires (diététique, thérapeutique, etc.)
- **ContEnum** : Types de contenants pour les aliments
- **Espece** : Espèces animales (chien, chat, cheval, etc.)
- **FoodKind** : Types d'aliments (croquettes, pâtée, etc.)
- **GroupAlim** : Groupes alimentaires (céréales, viandes, etc.)

### Unités et mesures

- **UnitNutrient** : Unités de mesure pour les nutriments
- **UnitReqEnum** : Unités pour les besoins nutritionnels (par kg de poids, par Mcal, etc.)

### Classes utilitaires

- **NutrientResolver** : Classe utilitaire pour résoudre les nutriments à partir de leurs noms ou identifiants
  - **AllNutrientResolver** : Fonction pour résoudre un nutriment à partir de son nom quelle que soit sa catégorie

## Utilisation

1. **Dans les modèles de données** : Ces énumérations sont utilisées comme types pour les propriétés des classes de données
2. **Pour le filtrage** : Utilisées pour filtrer des aliments par type, groupe, espèce, etc.
3. **Pour l'analyse nutritionnelle** : Servent à identifier et calculer les valeurs nutritionnelles
4. **Pour l'importation/exportation** : Permettent de convertir entre les noms de nutriments dans les fichiers JSON et les types internes

## Relations avec d'autres modules

- **Data** : Les modèles de données utilisent ces énumérations pour typer leurs propriétés
- **Utils** : Les fonctions d'importation/exportation utilisent ces énumérations pour la conversion
- **View** : L'interface utilisateur présente ces valeurs énumérées sous forme de sélections
- **ViewModel** : La logique métier manipule ces valeurs pour les calculs et traitements 