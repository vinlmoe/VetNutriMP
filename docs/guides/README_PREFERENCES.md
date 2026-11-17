# Système de Préférences VetNutri MP

Ce document explique le système de préférences de l'application VetNutri MP et comment le personnaliser.

## 🎯 Vue d'ensemble

Le système de préférences permet de configurer :
- **Types d'expression des besoins** nutritionnels (par kg, par kcal, par kg métabolique, par kJ)
- **Nutriments sélectionnés** par catégorie pour chaque espèce
- **Équations complémentaires** pour des calculs spécifiques
- **Configurations par défaut** personnalisables

## 🏗️ Architecture

### Fichiers Principaux

- **`DefaultPreferencesConfig.kt`** : Configuration centrale des préférences par défaut
- **`PreferencesEspece.kt`** : Structure des préférences par espèce
- **`PreferencesRepository.kt`** : Gestion de la persistance et des opérations
- **`CustomPreferencesExample.kt`** : Exemples de configurations personnalisées

### Classes Principales

```kotlin
// Préférences pour une espèce spécifique
data class PreferencesEspece(
    val espece: String,
    val typeExpressionBesoinId: Int,
    val nutrimentsSelectionnes: Map<String, List<Int>>,
    val equationsComplementaires: Map<String, String>
)

// Ensemble des préférences de toutes les espèces
data class PreferencesApplication(
    val preferencesParEspece: Map<String, PreferencesEspece>,
    val versionPreferences: Int
)
```

## 🚀 Utilisation Rapide

### 1. Modifier les Préférences par Défaut

Éditez le fichier `DefaultPreferencesConfig.kt` :

```kotlin
object DefaultNutrients {
    /** Vitamines sélectionnées par défaut */
    val VITAM = listOf(
        45, // Vitamine A
        46, // Vitamine D
        47, // Vitamine E
        48, // Vitamine K (ajoutée)
        49  // Vitamine C (ajoutée)
    )
}
```

### 2. Personnaliser une Espèce

```kotlin
/** Préférences par défaut pour les chiens */
val CHIEN = PreferencesEspece(
    espece = Espece.CHIEN.name,
    typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG.id, // Changé de PAR_KCAL (défaut) à PAR_KG
    nutrimentsSelectionnes = mapOf(
        "BASE" to DefaultNutrients.BASE,
        "MACRO" to DefaultNutrients.MACRO,
        // ... autres catégories
    ),
    equationsComplementaires = DEFAULT_EQUATIONS
)
```

### 3. Créer une Configuration Personnalisée

```kotlin
object MaConfigurationPersonnalisee {
    val NUTRIMENTS_SPECIAUX = mapOf(
        "BASE" to listOf(1, 2, 4, 5, 8, 0),
        "MACRO" to listOf(10, 11, 12, 13, 14, 15, 16),
        // ... configuration personnalisée
    )
    
    val CHIEN_PERSONNALISE = PreferencesEspece(
        espece = Espece.CHIEN.name,
        typeExpressionBesoinId = TypeExpressionBesoin.PAR_KG.id,
        nutrimentsSelectionnes = NUTRIMENTS_SPECIAUX,
        equationsComplementaires = emptyMap()
    )
}
```

## 📋 Types d'Expression des Besoins

| Type | Description | Utilisation |
|------|-------------|-------------|
| `PAR_KG` | Par kilogramme de poids corporel | Ancienne valeur par défaut |
| `PAR_KCAL` | Par Mcal de besoin énergétique | **NOUVELLE VALEUR PAR DÉFAUT** |
| `PAR_KG_METABOLIQUE` | Par kg de poids métabolique | Chevaux, herbivores (spécifique) |
| `PAR_KJ` | Par MJ de BEE | Recherche internationale |

## 🧬 Catégories de Nutriments

### Nutriments de Base (BASE)
- **1** : Matière sèche (MS)
- **2** : Protéines brutes (PB)
- **4** : Matières grasses (MG)
- **5** : Fibres brutes (FB)
- **8** : Cendres
- **0** : Extrait non azoté (ENA)

### Macronutriments (MACRO)
- **10** : Calcium (Ca)
- **11** : Phosphore (P)
- **12** : Magnésium (Mg)
- **13** : Sodium (Na)

### Minéraux (MIN)
- **14** : Potassium (K)
- **15** : Chlorure (Cl)
- **16** : Soufre (S)
- **17** : Cuivre
- **18** : Zinc
- **19** : Manganèse
- **20** : Sélénium
- **21** : Iode
- **22** : Fer

### Vitamines (VITAM)
- **45** : Vitamine A
- **46** : Vitamine D
- **47** : Vitamine E
- **48** : Vitamine K
- **49** : Vitamine C
- **50** : Thiamine (B1)
- **51** : Riboflavine (B2)
- **52** : Vitamine B6
- **53** : Vitamine B12
- **54** : Niacine
- **55** : Acide pantothénique
- **56** : Acide folique
- **57** : Biotine
- **58** : Choline

### Acides Gras (LIPID)
- **25** : Acides gras saturés
- **26** : Acides gras insaturés
- **27** : Oméga 3
- **28** : Oméga 6
- **29** : EPA+DHA

### Acides Aminés (AMA)
- **0** : Arginine
- **1** : Alanine
- **2** : Histidine
- **3** : Isoleucine
- **4** : Leucine
- **5** : Lysine
- **6** : Méthionine
- **7** : Méthionine+Cystine
- **8** : Phénylalanine
- **9** : Phénylalanine+Tyrosine
- **10** : Thréonine
- **11** : Tryptophane
- **12** : Valine

## 🔧 Personnalisation Avancée

### Configuration par Environnement

Le fichier `CustomPreferencesExample.kt` contient des exemples pour différents environnements :

- **Recherche** : Tous les nutriments pour une analyse complète
- **Clinique** : Nutriments essentiels uniquement
- **Laboratoire** : Configuration pour rongeurs de recherche
- **Équidés** : Spécialisé pour chevaux et herbivores
- **Exotiques** : Pour furets, lapins, primates

### Ajout d'Équations Complémentaires

```kotlin
val DEFAULT_EQUATIONS = mapOf(
    "calcium" to "uuid_equation_calcium_specifique",
    "vitamine_d" to "uuid_equation_vitamine_d_cheval"
)
```

## 📱 Interface Utilisateur

Les préférences sont accessibles via :
- **SettingsView** : Interface principale des paramètres
- **SpeciesPreferencesView** : Préférences spécifiques par espèce
- **PreferencesView** : Vue détaillée des préférences

## 💾 Persistance des Données

Les préférences sont sauvegardées dans :
- **Android** : SharedPreferences
- **iOS** : NSUserDefaults
- **Desktop** : Fichier .properties

## 🔄 Réinitialisation

Pour réinitialiser les préférences aux valeurs par défaut :

```kotlin
val preferencesRepository = PreferencesRepository(preferencesStorage)
val defaultPreferences = PreferencesApplication.createDefault()
preferencesRepository.savePreferences(defaultPreferences)
```

## ⚠️ Notes Importantes

1. **Compatibilité** : Les modifications des préférences par défaut n'affectent que les nouveaux utilisateurs
2. **Validation** : Vérifiez que les IDs des nutriments existent dans la base de données
3. **Performance** : Les préférences sont chargées au démarrage de l'application
4. **Migration** : Les anciennes préférences sont automatiquement migrées vers le nouveau format

## 📚 Documentation Complète

- **Guide détaillé** : `docs/guide_preferences_par_defaut.md`
- **Référence complète des nutriments** : `docs/reference_nutriments_complete.md`
- **Exemples** : `CustomPreferencesExample.kt`
- **Code source** : `DefaultPreferencesConfig.kt`

## 🆘 Support

Pour toute question sur la personnalisation des préférences :
1. Consultez la documentation
2. Examinez les exemples dans `CustomPreferencesExample.kt`
3. Vérifiez la structure des données dans `MainNutrientEnum.kt`
4. Testez vos modifications sur un environnement de développement
