# Modifications apportées pour la gestion des UnitEnum

## Résumé des modifications

Cette mise à jour ajoute la possibilité de définir et visualiser les `UnitEnum` dans l'éditeur de `ReferenceEvNutrientView.kt`. L'unité par défaut est celle du nutriment et seules les unités ayant la même `idFamily` peuvent être sélectionnées.

## Fichiers modifiés

### 1. `NutrientRef.kt`
- **Ajout du champ** : `unitEnum: UnitEnum = UnitEnum.BUg`
- **Description** : Représente l'unité physique du nutriment (g, mg, µg, UI, kcal)

### 2. `NutrientRefEditView.kt`
- **Ajout d'une colonne** : "Unité physique" entre la valeur et l'unité de besoin
- **Logique de filtrage** : Seules les unités avec la même `idFamily` sont disponibles
- **Fonctionnalité** :
  - Dropdown pour sélectionner l'unité physique
  - Filtrage automatique basé sur l'`idFamily` de l'unité actuelle
  - Interface intuitive avec labels clairs

### 3. `NutrientRefViewModel.kt`
- **Nouvelle méthode** : `updateNutrientUnitEnum(nutrientId: String, newUnitEnum: UnitEnum)`
- **Modification** : Chargement des nutriments avec l'`unitEnum` par défaut du nutriment (`nutrient.ue`)

## Fonctionnalités ajoutées

### Sélection d'unités compatibles
La fonction `getCompatibleUnits(defaultUnitEnum: UnitEnum)` :
- Récupère toutes les unités ayant la même `idFamily`
- Assure la cohérence des conversions entre unités de même famille

### Interface utilisateur améliorée
- **Colonne "Unité physique"** : Affiche et permet de modifier l'`UnitEnum`
- **Colonne "Unité besoin"** : Conserve la fonctionnalité existante pour `UnitReqEnum`
- **Filtrage intelligent** : Limite les choix aux unités compatibles

## Structure des familles d'unités (UnitEnum)

| idFamily | Unités disponibles | Description |
|----------|-------------------|-------------|
| 1 | BUg, BUmg, BUmu | Unités de base (grammes) |
| 2 | AUui, AUmu | Vitamine A (UI/µg) |
| 3 | DUui, DUmu | Vitamine D (UI/µg) |
| 4 | EUui, EUmg | Vitamine E (UI/mg) |
| 5 | NO | Pas d'unité |
| 6 | KCAL | Énergie (kcal) |

## Avantages

1. **Cohérence** : L'unité par défaut correspond au nutriment
2. **Sécurité** : Impossible de sélectionner des unités incompatibles
3. **Flexibilité** : Possibilité de changer d'unité dans la même famille
4. **Intuitivité** : Interface claire avec colonnes distinctes

## Utilisation

1. Sélectionner une catégorie de nutriments (BASE, MACRO, MIN, VITAM, etc.)
2. Pour chaque nutriment :
   - Modifier la valeur
   - Choisir l'unité physique (parmi les unités compatibles)
   - Définir l'unité de besoin
   - Associer une référence bibliographique si nécessaire
3. Enregistrer les modifications

## Compatibilité

Les modifications sont rétro-compatibles et n'affectent pas les données existantes. Les nutriments sans `unitEnum` défini utiliseront la valeur par défaut `UnitEnum.BUg`. 