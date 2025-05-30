# Structure JSON des Besoins Nutritionnels VetNutri

## Vue d'ensemble

Ce document décrit la structure complète des fichiers JSON d'exportation des besoins nutritionnels de VetNutri. Ces fichiers contiennent :
- Les besoins nutritionnels liés ou non à une maladie
- Toutes les équations associées 
- Les références bibliographiques complètes

## Extension de fichier
**Extension** : `.vbnr.json` (VetBrain Nutritional Requirements)

## Structure principale

Le fichier JSON racine contient un tableau d'objets `NutritionalRequirementData`, chacun représentant une référence nutritionnelle complète :

```json
[
  {
    "reference": { /* ReferenceEv object */ },
    "allEquations": [ /* Array of Equation objects */ ],
    "allBibliographicReferences": [ /* Array of BiblioRef objects */ ],
    "nutrientRequirements": [ /* Array of NutrientRequirementInfo objects */ ]
  }
]
```

## 1. Objet Reference (ReferenceEv)

```json
{
  "UUID": "string",
  "nom": "string",
  "description": "string", 
  "disease": "boolean",
  "nameDisease": "string",
  "nameEnergy": "string",
  "consistent": "integer",
  "species": "Espece",
  "sPhysio": "StadePhysio",
  "refMapMin": { /* Map<Nutrient, Nut4Ref> */ },
  "refMapMax": { /* Map<Nutrient, Nut4Ref> */ },
  "refMapOMin": { /* Map<Nutrient, Nut4Ref> */ },
  "refMapOMax": { /* Map<Nutrient, Nut4Ref> */ },
  "BWEqu": { /* Equation object */ },
  "BEEqu": { /* Equation object */ },
  "DEcomEqu": { /* Equation object */ },
  "DErawEqu": { /* Equation object */ },
  "NutEqu": [ /* Array of Equation objects */ ],
  "modk1": [ /* Array of CoefP objects */ ],
  "modk2": [ /* Array of CoefP objects */ ],
  "modk3": [ /* Array of CoefP objects */ ],
  "modk4": [ /* Array of CoefP objects */ ],
  "modk5": [ /* Array of CoefP objects */ ],
  "namek1": "string",
  "namek2": "string", 
  "namek3": "string",
  "namek4": "string",
  "namek5": "string"
}
```

### Propriétés clés :
- **UUID** : Identifiant unique de la référence
- **nom** : Nom de la référence nutritionnelle
- **disease** : Indique si c'est lié à une maladie spécifique
- **nameDisease** : Nom de la maladie (si applicable)
- **species** : Espèce concernée (CHIEN, CHAT, etc.)
- **sPhysio** : Stade physiologique (ADULTE, CROISSANCE, etc.)

### Objets Nut4Ref (dans les Maps de nutriments) :
```json
{
  "relativekind": "Reflevel",
  "nutrient": "Nutrient",
  "quantity": "float",
  "unit": "UnitEnum",
  "UnitReq": "UnitReqEnum", 
  "Biblio": { /* BiblioRef object */ }
}
```

### Objets CoefP (coefficients de modification) :
```json
{
  "name": "string",
  "value": "float",
  "kind": "integer"
}
```

## 2. Objet Equation

```json
{
  "UUID": "string",
  "name": "string",
  "description": "string",
  "equationScript": "string",
  "kind": "EquationKind",
  "specie": "Espece",
  "bib": { /* BiblioRef object */ },
  "alllNut": { /* AllNutrient object */ },
  "var": [ /* Array of VariableKind */ ],
  "consistent": "boolean",
  "jvscript": "boolean"
}
```

### Types d'équations (EquationKind) :
- **ENERGYNEED** (0) : Besoins énergétiques
- **ENERGYDENSITY** (1) : Densité énergétique  
- **MW** (2) : Poids métabolique
- **INDICATOR** (3) : Indicateur
- **NEED** (4) : Besoins nutritionnels

### Propriétés clés :
- **equationScript** : Formule mathématique de l'équation (utilise mXparser)
- **kind** : Type d'équation selon l'énumération EquationKind
- **alllNut** : Nutriment associé à l'équation
- **var** : Variables utilisées dans l'équation (BW, BEE, MW, etc.)

### Variables courantes dans les équations :
- **BW** : Body Weight (poids corporel)
- **BEE** : Basal Energy Expenditure (dépense énergétique basale)
- **MW** : Metabolic Weight (poids métabolique)
- Variables supplémentaires définies par l'utilisateur

## 3. Objet BiblioRef (Référence bibliographique)

```json
{
  "UUID": "string",
  "author": "string",
  "title": "string", 
  "journal": "string",
  "year": "integer",
  "volume": "string",
  "issue": "string",
  "pages": "string",
  "doi": "string",
  "pmid": "string",
  "url": "string",
  "note": "string"
}
```

## 4. Objet NutrientRequirementInfo

```json
{
  "nutrient": { /* Nutrient object */ },
  "referenceLevel": "Reflevel",
  "quantity": "float",
  "unit": "UnitEnum", 
  "unitRequirement": "UnitReqEnum",
  "bibliographicReference": { /* BiblioRef object */ }
}
```

### Niveaux de référence (Reflevel) :
- **MIN** (0) : Minimum requis
- **MAX** (1) : Maximum toléré
- **OPTIMIN** (2) : Optimal minimum
- **OPTIMAX** (3) : Optimal maximum

### Types de besoins (UnitReqEnum) :
- **NO** (0) : Aucune unité relative
- **KGBW** (1) : Par kg de poids corporel
- **KGMW** (2) : Par kg de poids métabolique  
- **MCAL** (3) : Par Mcal d'énergie métabolisable
- **DM** (4) : Base sèche (Dry Matter)

### Types de nutriments :
- **NutrientBase** : Protéines, lipides, glucides, fibres, etc.
- **NutrientMin** : Minéraux (calcium, phosphore, sodium, etc.)
- **NutrientVitam** : Vitamines (A, D, E, K, B1, B2, etc.)
- **NutrientLipid** : Acides gras spécifiques
- **AAEnum** : Acides aminés
- **NutrientAnalysis** : Éléments d'analyse

## 5. Objets Nutrient

Chaque nutriment contient :
```json
{
  "MNE": "MainNutrientEnum",
  "coef": "integer",
  "label": "string",
  "ue": "UnitEnum"
}
```

### MainNutrientEnum :
- **BASE** (0) : Nutriments de base
- **MIN** (1) : Minéraux
- **VITAM** (2) : Vitamines
- **LIPID** (3) : Lipides
- **AMA** (4) : Acides aminés
- **MACRO** (5) : Macronutriments
- **OTHER** (6) : Autres
- **ANA** (7) : Analyse

## 6. Énumérations importantes

### Espèces (Espece)
- **CHIEN** : Canine
- **CHAT** : Féline
- **LAPIN** : Lagomorphe

### Stades physiologiques (StadePhysio)
- **ADULTE** : Adulte
- **CROISSANCE** : Croissance
- **GESTATION** : Gestation
- **LACTATION** : Lactation
- **SENIOR** : Senior

### Unités (UnitEnum)
- **G** : Grammes
- **MG** : Milligrammes
- **UG** : Microgrammes
- **IU** : Unités internationales
- **KCAL** : Kilocalories
- **KJ** : Kilojoules
- **PERCENT** : Pourcentage

## 7. Gestion des besoins par maladie

Les références peuvent inclure des besoins spécifiques liés à des pathologies :
- **disease** : booléen indiquant si c'est pathologique
- **nameDisease** : nom de la pathologie
- Les besoins sont alors adaptés à la condition médicale

## 8. Exemple complet

```json
[
  {
    "reference": {
      "UUID": "123e4567-e89b-12d3-a456-426614174000",
      "nom": "Besoins nutritionnels chien adulte",
      "description": "Référence FEDIAF pour chien adulte en bonne santé",
      "disease": false,
      "nameDisease": "",
      "species": "CHIEN",
      "sPhysio": "ADULTE",
      "consistent": 1,
      "refMapMin": {
        "PROTEINE": {
          "quantity": 18.0,
          "unit": "PERCENT",
          "UnitReq": "DM",
          "Biblio": {
            "author": "FEDIAF",
            "title": "Nutritional Guidelines",
            "year": 2021
          }
        }
      }
    },
    "allEquations": [
      {
        "UUID": "987fcdeb-51a2-43d7-8c9b-123456789abc",
        "name": "BEE Chien",
        "description": "Besoin énergétique basal pour chien adulte",
        "equationScript": "130 * BW^0.75",
        "kind": "ENERGYNEED",
        "specie": "CHIEN",
        "consistent": true,
        "jvscript": false,
        "var": ["BW"]
      }
    ],
    "allBibliographicReferences": [
      {
        "UUID": "456e7890-a12b-34c5-d678-901234567890", 
        "author": "FEDIAF",
        "title": "Nutritional Guidelines for Complete and Complementary Pet Food",
        "year": 2021,
        "url": "https://fediaf.org/self-regulation/nutrition/"
      }
    ],
    "nutrientRequirements": [
      {
        "nutrient": {
          "MNE": "BASE",
          "coef": 0,
          "label": "Protéines",
          "ue": "PERCENT"
        },
        "referenceLevel": "MIN",
        "quantity": 18.0,
        "unit": "PERCENT",
        "unitRequirement": "DM",
        "bibliographicReference": {
          "author": "FEDIAF",
          "title": "Nutritional Guidelines",
          "year": 2021
        }
      }
    ]
  }
]
```

## 9. Calculs et conversions

### Calcul des besoins totaux :
Le système calcule automatiquement les besoins totaux selon l'unité de référence :
- **KGBW** : quantity × poids corporel (BW)
- **KGMW** : quantity × poids métabolique (MW)  
- **MCAL** : quantity × énergie basale (BEE) / 1000
- **NO** : quantity (valeur absolue)

### Variables d'équations :
Les équations utilisent des variables prédéfinies et peuvent inclure des variables supplémentaires définies par l'utilisateur dans le champ `var`.

## Utilisation et Import

### Structure de données compatible
Les données JSON sont structurées pour être facilement réimportées dans VetNutri en respectant :
- Les classes existantes du modèle (ReferenceEv, Equation, BiblioRef)
- Les énumérations et types définis
- Les relations entre besoins nutritionnels, équations et références
- La hiérarchie des nutriments et niveaux de référence

### Intégrité des données
- Tous les UUID sont préservés pour maintenir les relations
- Les références bibliographiques sont liées aux besoins spécifiques
- Les équations conservent leurs paramètres et variables
- Les unités et conversions sont préservées
- La méthode `getMap(Reflevel)` permet d'accéder aux bonnes Maps de nutriments

### Considérations techniques
- Les équations utilisent la bibliothèque mXparser pour l'évaluation
- Les Maps de nutriments sont séparées par niveau de référence (Min, Max, OptiMin, OptiMax)
- Support des coefficients de modification (modk1-5) pour ajustements contextuels

Ce format JSON permet un export/import complet des connaissances nutritionnelles vétérinaires avec toute leur complexité scientifique.

## Prompt pour l'implémentation de l'import en Kotlin

Pour implémenter l'import de ces fichiers JSON dans une application Kotlin/JavaFX :

"Je dois implémenter l'import de fichiers JSON de besoins nutritionnels vétérinaires dans une application Kotlin. Ces fichiers contiennent des données complexes sur les besoins nutritionnels par espèce et stade physiologique, avec des équations de calcul et des références bibliographiques.

Points critiques de compatibilité :
- Désérialiser vers les classes existantes : ReferenceEv, Equation, BiblioRef, NutrientRequirementInfo
- Respecter les énumérations : Espece, StadePhysio, EquationKind, Reflevel, UnitEnum, UnitReqEnum
- Maintenir les relations : nutriments ↔ équations ↔ références bibliographiques
- Préserver les UUID pour l'intégrité des données
- Gérer les Maps complexes de nutriments par niveau de référence (refMapMin, refMapMax, refMapOMin, refMapOMax)
- Reconstituer les équations avec leurs scripts mathématiques (mXparser) et variables
- Reconstruire la hiérarchie des nutriments (MainNutrientEnum, types de nutriments)
- Gérer la classe interne Nut4Ref avec ses champs : relativekind, nutrient, quantity, unit, UnitReq, Biblio
- Supporter les coefficients de modification (modk1-5) et leurs noms associés
- Calculer automatiquement les besoins totaux selon UnitReqEnum (KGBW, KGMW, MCAL, NO, DM)

Structure attendue : 
- Fichier JSON racine = Array de NutritionalRequirementData
- Chaque élément contient : reference (ReferenceEv) + allEquations + allBibliographicReferences + nutrientRequirements
- Support des équations de types : ENERGYNEED, ENERGYDENSITY, MW, INDICATOR, NEED
- Gestion des niveaux de référence : MIN(0), MAX(1), OPTIMIN(2), OPTIMAX(3)
- Support des besoins pathologiques (disease=true, nameDisease)

L'import doit valider la cohérence des données, reconstituer les Maps de nutriments par niveau, et permettre la fusion avec les données existantes tout en évitant les doublons." 