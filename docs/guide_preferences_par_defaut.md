# Guide de Modification des Préférences par Défaut

Ce guide explique comment modifier les préférences par défaut de l'application VetNutri MP.

## Vue d'ensemble

Les préférences par défaut de l'application sont maintenant centralisées dans le fichier `DefaultPreferencesConfig.kt`. Cela permet de modifier facilement les valeurs initiales sans avoir à chercher dans plusieurs fichiers.

## Structure des Préférences

### Types d'Expression des Besoins

L'application supporte 4 types d'expression des besoins nutritionnels :

- **PAR_KG** : Par kilogramme de poids corporel
- **PAR_KCAL** : Par Mcal de besoin énergétique d'entretien (BEE) - **VALEUR PAR DÉFAUT**
- **PAR_KG_METABOLIQUE** : Par kilogramme de poids métabolique
- **PAR_KJ** : Par MJ de BEE

### Catégories de Nutriments

Les nutriments sont organisés en 11 catégories principales :

1. **BASE** : Matière sèche, protéines, matières grasses, fibres, cendres, ENA
2. **MACRO** : Calcium, phosphore, magnésium, sodium
3. **MIN** : Potassium, chlorure, soufre
4. **VITAM** : Vitamines A, D, E, K, C, B1, B2, B6, B12, niacine, acide pantothénique, acide folique, biotine, choline
5. **LIPID** : Acides gras saturés, insaturés, oméga 3, oméga 6, EPA+DHA
6. **AMA** : Acides aminés essentiels (arginine, alanine, histidine, etc.)
7. **ANA** : Acides aminés non essentiels
8. **OTHER** : Autres nutriments
9. **INDICAT** : Indicateurs
10. **INGREDIENT** : Ingrédients
11. **ENERGIE** : Énergie

## Modification des Préférences par Défaut

### 1. Modifier les Nutriments Sélectionnés par Défaut

Pour modifier les nutriments sélectionnés par défaut pour toutes les espèces, éditez l'objet `DefaultNutrients` dans `DefaultPreferencesConfig.kt` :

```kotlin
object DefaultNutrients {
    /** Nutriments de base sélectionnés par défaut */
    val BASE = listOf(
        1,  // Matière sèche (MS)
        2,  // Protéines brutes (PB)
        4,  // Matières grasses (MG)
        5,  // Fibres brutes (FB)
        8,  // Cendres
        0   // Extrait non azoté (ENA)
    )
    
    /** Macronutriments sélectionnés par défaut */
    val MACRO = listOf(
        10, // Calcium (Ca)
        11, // Phosphore (P)
        12, // Magnésium (Mg)
        13  // Sodium (Na)
    )
    
    // ... autres catégories
}
```

### 2. Modifier les Préférences Spécifiques par Espèce

Pour modifier les préférences d'une espèce particulière, éditez l'objet `SpeciesSpecificDefaults` :

```kotlin
/** Préférences par défaut pour les chiens */
val CHIEN = PreferencesEspece(
    espece = Espece.CHIEN.name,
    typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG.id, // Changer ici
    nutrimentsSelectionnes = mapOf(
        "BASE" to DefaultNutrients.BASE,
        "MACRO" to DefaultNutrients.MACRO,
        // ... autres catégories
    ),
    equationsComplementaires = DEFAULT_EQUATIONS
)
```

### 3. Modifier le Type d'Expression par Défaut

**Note :** L'expression par défaut est maintenant **PAR_KCAL** (par 1000 kcal) pour toutes les espèces.

Pour changer le type d'expression par défaut pour une espèce :

```kotlin
// Pour utiliser l'expression par kg (ancienne valeur par défaut)
typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG.id

// Pour utiliser l'expression par 1000 kcal (nouvelle valeur par défaut)
typeExpressionBesoinId = TypeExpressionBesoin.PAR_KCAL.id

// Pour utiliser l'expression par kg métabolique
typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG_METABOLIQUE.id

// Pour utiliser l'expression par kJ
typeExpressionBesoinId = TypeExpressionBesoin.PAR_KJ.id
```

### 4. Ajouter des Équations Complémentaires par Défaut

Pour ajouter des équations complémentaires par défaut :

```kotlin
val DEFAULT_EQUATIONS = mapOf(
    "calcium" to "uuid_equation_calcium",
    "vitamine_d" to "uuid_equation_vitamine_d"
)
```

## Exemples de Modifications

### Exemple 1 : Ajouter plus de vitamines par défaut

```kotlin
/** Vitamines sélectionnées par défaut */
val VITAM = listOf(
    45, // Vitamine A
    46, // Vitamine D
    47, // Vitamine E
    48, // Vitamine K
    49, // Vitamine C
    50, // Thiamine (B1)
    51  // Riboflavine (B2)
)
```

### Exemple 2 : Changer l'expression des besoins pour les chevaux

```kotlin
/** Préférences par défaut pour les chevaux */
val CHEVAL = PreferencesEspece(
    espece = Espece.CHEVAL.name,
    typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG_METABOLIQUE.id, // Par kg métabolique au lieu de par kcal (défaut)
    // ... reste des préférences
)
```

### Exemple 3 : Personnaliser les nutriments pour les chats

```kotlin
/** Préférences par défaut pour les chats */
val CHAT = PreferencesEspece(
    espece = Espece.CHAT.name,
    typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG.id,
    nutrimentsSelectionnes = mapOf(
        "BASE" to listOf(1, 2, 4, 5, 8, 0), // Nutriments de base
        "MACRO" to listOf(10, 11, 12, 13), // Ca, P, Mg, Na
        "MIN" to listOf(14, 15, 16), // K, Cl, S
        "VITAM" to listOf(45, 46, 47, 48), // A, D, E, K
        "LIPID" to listOf(25, 26, 27), // AG saturés, insaturés, oméga 3
        "AMA" to listOf(0, 1, 2, 3, 4, 5), // Acides aminés essentiels
        "ANA" to emptyList(),
        "OTHER" to emptyList(),
        "INDICAT" to emptyList(),
        "INGREDIENT" to emptyList(),
        "ENERGIE" to emptyList()
    ),
    equationsComplementaires = DEFAULT_EQUATIONS
)
```

## Réinitialisation des Préférences

Pour réinitialiser les préférences d'un utilisateur aux valeurs par défaut, utilisez :

```kotlin
val preferencesRepository = PreferencesRepository(preferencesStorage)
val defaultPreferences = PreferencesApplication.createDefault()
preferencesRepository.savePreferences(defaultPreferences)
```

## Notes Importantes

1. **IDs des Nutriments** : Les IDs des nutriments correspondent aux indices dans les énumérations `MainNutrientEnum`
2. **Persistance** : Les modifications des préférences par défaut ne s'appliquent qu'aux nouveaux utilisateurs ou lors d'une réinitialisation
3. **Compatibilité** : Les préférences existantes des utilisateurs ne sont pas affectées par les modifications des valeurs par défaut
4. **Validation** : Assurez-vous que les IDs des nutriments et des équations existent dans la base de données

## Fichiers Concernés

- `DefaultPreferencesConfig.kt` : Configuration principale des préférences par défaut
- `PreferencesEspece.kt` : Structure des préférences par espèce
- `PreferencesRepository.kt` : Gestion de la persistance des préférences
- `TypeExpressionBesoin.kt` : Types d'expression des besoins
- `MainNutrientEnum.kt` : Catégories et nutriments disponibles
- `docs/reference_nutriments_complete.md` : **Référence complète de tous les nutriments avec leurs IDs**
