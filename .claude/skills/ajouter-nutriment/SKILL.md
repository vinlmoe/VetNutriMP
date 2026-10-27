---
name: ajouter-nutriment
description: Ajouter un nutriment à VetNutriMP — nouvelle entrée d'enum (NutrientMain, NutrientMacro, NutrientMin, NutrientVitam, NutrientLipid, NutrientOther, AAEnum, NutrientEnergy, NutrientAnalysis), alias du NutrientResolver et de l'import, traduction du nom, nutriment calculé (rapport) ou nouvelle famille d'enum. À utiliser dès qu'on touche aux fichiers Enumer/Nutrient*.kt, AAEnum.kt ou NutrientResolver.kt, ou qu'un label de nutriment n'est pas reconnu à l'import.
---

# Ajouter un nutriment (VetNutriMP)

Préfixe : `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/`.

## Ce qu'il faut savoir avant

- **Le `label` est la clé persistée.** Il est stocké tel quel dans `NUTRIENT_VALUES.nutrientLabel`,
  dans les JSON INIT (`"nutrients": { "CAL": 0.24, … }`), les sauvegardes, les référentiels et les
  équations. Un label publié ne se renomme **jamais** : on ajoute un alias dans le résolveur.
  Labels : MAJUSCULES sans espace ni accent (`FIBRETOT`, `VITB12`, `AG183`), uniques toutes familles
  confondues.
- **`coef`** = rang dans la famille ; `getByCoef` le cherche, il doit donc être unique dans l'enum
  (seul `NutrientEnergy` a des doublons historiques). Prendre le premier entier libre, ne pas
  renuméroter les entrées existantes.
- **Le module R `vetnutriDogR` lit ces enums par expressions régulières**
  (`vetnutriDogR/R/vetnutri_data_model.R`, `vn_kotlin_dictionaries`). Une entrée doit reprendre
  exactement l'ordre d'arguments de ses voisines, sur une ligne commençant par `NOM("` : premier
  argument = nom affiché en chaîne littérale, uniquement des littéraux (pas de constante ni
  d'expression), label à la même position parmi les chaînes (3ᵉ en général, 4ᵉ pour `NutrientVitam`,
  2ᵉ pour `AAEnum`), unité `UnitEnum.Xxx` existante. Sinon `vn_kotlin_dictionaries` s'arrête avec « Schéma Kotlin non reconnu » ou
  « Dictionnaire Kotlin ambigu ».
- Toute résolution d'un texte vers un `Nutrient` passe par `NutrientResolver.AllNutrientResolver(label)`
  (ou `resolveStoredLabel` pour un label déjà en base). Ne pas appeler `XxxEnum.getByLabel()` depuis
  Repository/Service.
- Un label inconnu n'est pas une erreur : il devient un `CustomNutrient` (registre
  `CustomNutrientRegistry`, table `CUSTOM_NUTRIENTS`). Ajouter un nutriment « système » n'est donc utile
  que s'il doit être calculé, traduit, ordonné ou porté par les référentiels livrés.

## Choisir la famille

| Famille | `MainNutrientEnum` | Exemples | Unité typique |
|---------|-------------------|----------|---------------|
| `NutrientMain` | BASE | HUMIDITE, PROTEINE, FIBRETOT, DM | g |
| `NutrientMacro` | MACRO | CAL, PHOS, NA, K | g |
| `NutrientMin` | MIN | FE, CU, ZN, SE | mg, µg |
| `NutrientVitam` | VITAM | VITA, VITD, VITB12 (+ `altLabels`) | UI, µg, mg |
| `NutrientLipid` | LIPID | AG182, O3, O6, EPADHA | g |
| `AAEnum` | AMA | LYSINE, METHIONINE, TRYPTOPHANE | g |
| `NutrientOther` | OTHER | TAURINE, CARNITINE, FOS, SUCR | selon l'entrée |
| `NutrientEnergy` | ENERGIE | TOT, BEE, MW (grandeurs énergétiques calculées) | kcal |
| `NutrientAnalysis` | ANA | KNA, CAP, O6O3, METHCYS (**calculés**) | "" pour un rapport |

## Étapes — nutriment mesuré (valeur saisie pour 100 g d'aliment)

1. **Enum** : ajouter l'entrée en fin de liste, même forme que ses voisines, `coef` libre suivant.
2. **Alias** : si la donnée source peut l'écrire autrement (anglais, accents, abréviations Excel),
   ajouter les variantes dans les tables d'alias, qui ne se partagent pas :
   - `Enumer/NutrientResolver.kt` → `normalizeLabel()` (`"ALIAS1", "ALIAS2" -> "LABEL"`) ;
   - `Utils/ImportUtils.kt` → `normaliserNomNutrient()` (import JSON/Excel) ;
   - pour une vitamine, aussi le paramètre `altLabels` de l'entrée (`listOf("VIT_X", "VITAMINE_X")`).
   Vérifier qu'aucun alias n'est déjà pris par un autre nutriment (`grep -n '"ALIAS"'` dans les deux fichiers).
3. **Traduction du nom** : `translateEnum()` (`Localization/EnumLocalization.kt`) cherche d'abord la clé
   `enum.<Classe>.<LABEL>` (`buildEnumKey`), puis une clé heuristique de `computeNutrientHeuristicKey`
   (`minerals.zinc`, `vitamins.b12`, label en minuscules pour lipides/AA/autres). Ajouter la clé
   correspondante dans les fichiers de chaînes (skill `traduire-interface`) et, si besoin, la branche
   dans `computeNutrientHeuristicKey`.
4. **Ordre d'affichage / classification** : vérifier `Data/NutrientDisplayCalculations.kt`
   (ordre par famille ; `estNutrimentAnalysisRatio` est le seul point de détection des rapports) — en général rien à faire pour une entrée
   d'une famille existante.
5. **Tests** : dans `composeApp/src/commonTest/kotlin/fr/vetbrain/vetnutri_mp/Enumer/NutrientResolverTest.kt`,
   un test par alias : `assertEquals("LABEL", NutrientResolver.normalizeLabel("ALIAS"))`, et un test
   `AllNutrientResolver("LABEL")` qui renvoie la bonne entrée.

## Étapes supplémentaires — nutriment calculé (`NutrientAnalysis`)

Un rapport n'a pas de valeur stockée : il est calculé à partir d'autres nutriments.
- Unité `""` pour un rapport sans dimension (c'est ce qui le classe comme rapport,
  `estNutrimentAnalysisRatio` dans `Data/NutrientDisplayCalculations.kt`), sinon son unité (`"g"`, `"%"`).
- Ajouter le calcul dans **chacun** des endroits qui font un `when (label)` sur ces labels :
  - `Data/RationNutrientAnalyzer.kt` (repli de calcul des rapports d'une ration) ;
  - `ViewModel/CrossConsultationAnalysisViewModel.kt` (analyse croisée) ;
  - `grep -rn '"KNA"' composeApp/src/commonMain` pour retrouver les autres.
- Division : renvoyer `0.0` (ou `null` selon le voisinage) quand le dénominateur est nul, comme `KNA`.
- Test dans `commonTest/.../Data/RationNutrientAnalyzerTest.kt` avec une ration à valeurs connues.

## Nouvelle famille d'enum (rare)

En plus de ce qui précède, l'ajouter partout où les familles sont énumérées :
`grep -rln "NutrientAnalysis.entries" composeApp/src/commonMain` (résolveur : `labelToNutrient`,
`resolveNutrient`, `findBestFuzzyMatch`, `getAllNutrientLabels` ; `FoodEditViewModel.loadNutrients()` ;
`preloadCustomNutrientsFromRepository()` ; ViewModels de référentiels et d'équations ; analyseurs),
une valeur dans `MainNutrientEnum`, et la classe dans `classes` de `vn_kotlin_dictionaries` (R).

## Vérifier

```bash
./gradlew :composeApp:desktopTest --tests "*NutrientResolverTest*"
./gradlew :composeApp:desktopTest
```
Si R est installé : `bash vetnutriDogR/scripts/validate-kotlin.sh && Rscript vetnutriDogR/tests/init-parity.R`
(sinon le signaler, voir le skill `parite-vetnutridogr`).

## Liste de contrôle

- [ ] Label nouveau, unique, en majuscules ; aucun label existant renommé
- [ ] `coef` libre ; syntaxe de l'entrée lisible par le module R
- [ ] Alias dans `NutrientResolver.normalizeLabel` **et** `ImportUtils.normaliserNomNutrient`
- [ ] Clé de traduction du nom (fr, en, zh au minimum)
- [ ] Calcul ajouté partout si nutriment calculé
- [ ] Tests du résolveur (et de l'analyseur si calculé), `desktopTest` vert
