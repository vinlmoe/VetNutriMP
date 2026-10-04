# Source INIT et module canin

Le module est limité au **chien**, conformément à la demande. Les JSON ne sont
pas modifiés. `vn_load_init()` expose les aliments compatibles et les références
`CHIEN`. `raw` conserve l'enveloppe complète pour l'audit. Les calculs refusent un
référentiel d'une autre espèce ou un aliment réservé à une autre espèce.

## 1. Inventaire et schéma observés

Source principale : `composeApp/src/commonMain/resources/data/vetnutri_export_init.json`.
Copies de distribution :

- `composeApp/src/androidMain/assets/data/vetnutri_export_init.json` ;
- `iosApp/iosApp/vetnutri_export_init.json` ;
- `iosApp/iosApp/Resources/vetnutri_export_init.json`.

À l'implémentation, les quatre fichiers sont identiques (version 3.3.18) :
11 466 aliments, 29 équations, 56 bibliographies, 27 références, 2 recettes,
36 conseils et 30 mots-clés ; `animals` et `rations` sont vides. Les copies de
`build/` et les sauvegardes datées sont exclues de la découverte. Les fichiers ne
sont pas complémentaires : on en charge **un seul**, sans fusionner les copies.
La divergence des copies déclenche un avertissement ; commonMain reste prioritaire.

L'enveloppe porte `version` et `generatedAtEpochMs`. Les sections sont des listes.

| Objet | Champs et relations |
| --- | --- |
| Aliment | `uuid`, `name`, `group`, `kind`, `dataB`, `species`, `indications`, `deprecated`, `consistent`, métadonnées commerciales |
| Composition | `foods[].nutrients` : dictionnaire label → valeur, pour 100 g ; pas de colonne d'unité dans le JSON |
| Énergie | `energyPerSpecies` : nom d'espèce → kcal/100 g ; `nutrients.ENERGIE` : énergie générique |
| Référence | `uuid`, `nom`, `espece`, `stadePhysio`, `maladie`, `nomMaladie`, `nomEnergie` |
| Seuil | `nutrientLabel`, `reflevel`, `quantity`, `uniteReqId`, `biblioRefId` |
| Niveaux | `MIN`, `MAX`, `OPTIMIN` (= optimum_min), `OPTIMAX` (= optimum_max), conservés en format long sans écraser les absences |
| Équations | `uuid`, `name`, `kind`, `specie`, `nutrient`, `script`, `variables`, `ratio` |
| Liens aux équations | `equationBW`, `equationBEE`, `equationDEcom`, `equationDEraw`, `equationME`, `equationsNut[]` → `equations[].uuid` |
| Coefficients | `references[].coefficients` : `uuid`, `groupType` (k1…k5), `description`, `coef`, `groupUUID` |
| Bibliographie | `biblioRefs[].uuid` ; liens depuis `biblioRefIds` et les seuils |
| Recette | `recipes[].aliments[].foodId` → aliment ; quantité en g ; `targetRef` conservé brut |

Les identifiants sont des chaînes opaques : `C49`, UUID, `equation-1`, `1`, etc.
Aucun identifiant n'est régénéré ou converti en nombre. La matière sèche est le
nutriment **DM**, lorsqu'il existe ; HUMIDITE est un autre champ. Une valeur DM
absente n'est pas remplacée automatiquement par 100 − HUMIDITE : il faut une
équation complémentaire explicitement rattachée au référentiel.

## 2. Interprétation Kotlin

Chemins suivants sous `composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/` :

- `Data/ApiModels.kt`, `FoodApi.toDomain` : valeurs `deprecated`/`consistent`
  absentes → false ; énergies par espèce non positives ignorées ; résolution des
  nutriments, espèces et catégories. Pas de conversion numérique des compositions.
- `Data/AlimentEv.kt`, `getNutrient` : valeurs négatives → zéro à la lecture ;
  acides aminés de `dataB == VF24` masqués. Les valeurs brutes restent inchangées.
- `Enumer/Nutrient*.kt`, `AAEnum.kt`, `UnitEnum.kt` : dictionnaire des nutriments
  et unités. Le loader lit ces fichiers directement ; aucune table nutritionnelle
  parallèle n'est maintenue en R. `unit_id` distingue les familles UI, `unit` est
  le symbole physique, `display_unit` conserve celui de l'enum de nutriment.
- `Enumer/UnitReqEnum.kt` : 0/kg vif, 1/1000 kcal, 2/kg métabolique,
  4/1000 kJ, 5/ratio, 6/absolu ; identifiant inconnu → 0 (PERKG).
- `Repository/ExportImportRepository.kt` : niveau inconnu → MIN ; liens
  bibliographiques introuvables → objet BiblioRef vide côté Kotlin. R garde le
  lien original et signale l'absence, sans fabriquer une bibliographie.
  Si un seuil est répété pour le même nutriment/niveau, la dernière entrée fait
  foi comme dans `ReferenceEv.definirNutriment` ; les autres restent conservées
  avec `is_effective=false` et un diagnostic.
- `View/components/FoodSearchComponent.kt`, `matchesEspece` : une liste d'espèces
  vide ou CH/ALL accepte toutes les espèces ; sinon CHIEN doit être présent.
  C'est la règle utilisée pour le catalogue de ration canin. Les aliments
  dépréciés restent conservés et identifiables, sans suppression silencieuse.
- `Data/ConsultationEv.kt`, `effectiveWeight` : poids idéal prioritaire.
- `ViewModel/AnimalDetailViewModel.kt`, `calculerValeursMetaboliques` :
  `equationBW` calcule le poids métabolique ; `equationBEE` calcule le besoin
  standard. Le besoin ajusté est BEE × k1 × k2 × k3 × k4 × k5 × ajustement.
  Les K absents valent 1. Les valeurs de K proposées viennent du JSON.
  Variables manquantes : wG/AW/L/wL → 0 ; BCS → 5 ; REI/AF/TE/GE/ME → 1.
  Ces défauts explicites du Kotlin sont appliqués et listés dans `default_variables`.
  Un calcul non fini (par exemple AW=0 dans une croissance) est refusé en R.
- `Utils/EquationEvaluator.kt`, `calculerEnergiePour100g` : énergie positive pour
  CHIEN prioritaire. Énergie générique seulement s'il n'existe aucune énergie
  positive par espèce. Sinon DEcom pour COMPLET/COMPLEMENTAIRE, DEraw pour les
  autres types ; repli sur la première équation ENERGIE du repository ayant le
  `ratio` commercial correspondant. Variables nutritionnelles absentes → 0
  dans l'équation énergétique, conformément au Kotlin. Échec/non-fini/négatif →
  0, avec avertissement R. ENA est alimenté par les équations du référentiel.
- `Data/AlimentRation.kt`, `getNutrientWithComplementary` : valeurs directes
  prioritaires ; sinon équations complémentaires sélectionnées, espèce CHIEN ou
  CH, sommées sauf `ratio=true` qui remplace l'accumulation ; résultat ≥ 0.
- `Data/NutrientDisplayCalculations.kt`, `calculerBesoinAbsolu` : seuil × BEE/1000,
  poids vif, poids métabolique, ou BEE×4.184/1000 selon l'unité. Le BEE est le
  besoin **standard**, pas le besoin ajusté. Les vrais ratios de NutrientAnalysis
  sont évalués à partir des totaux de la ration, sans pondération par son poids.

Les champs `raw_value`, `value`, `raw_reflevel`, `raw_uniteReqId`, ainsi que
`source_json`, `source_pointer` et `source_kotlin` rendent les transformations
vérifiables. Les résultats portent `food_id`, `nutrient_id`, `reference_id` et les
équations employées. La provenance contient version, date export et empreinte MD5
(détection de changement, pas signature de sécurité).

## 3. Module indépendant

`vetnutriDogR` possède son propre package, ses dépendances, ses interfaces et ses
tests. Il ne charge aucun fichier ni fonction de `vetnutriExamR`. Le moteur de
notation historique reste dans son dossier d'origine. Seuls le checkout VetNutri
MP (JSON et Kotlin) et les dépendances R déclarées sont nécessaires.

## 4. Architecture et utilisation

`init_json_loader.R` découvre et lit ; `init_json_validation.R` valide les types,
identifiants et liens ; `vetnutri_data_model.R` normalise et extrait les enums ;
`init_calculations.R` calcule besoins, apports et conformité. Le calcul des
expressions utilise une liste fermée d'opérations mathématiques, sans exécuter du
code R provenant du JSON. Les avertissements de calcul sont conservés dans
`resultat$diagnostics` et visibles dans un onglet Shiny dédié.

Depuis la racine du dépôt :

```r
# Installer une fois : install.packages(c("jsonlite", "shiny", "knitr"))
# Puis, dans le terminal : R CMD INSTALL vetnutriDogR
library(vetnutriDogR)
Sys.setenv(VETNUTRI_MP_ROOT = normalizePath("."))
m <- vn_load_init()
# Choisir des identifiants dans m$references, m$foods et m$coefficients.
# n <- vn_init_needs(m, reference_id, weight_kg = 10)
# r <- vn_init_ration(m, reference_id,
#       data.frame(food_id = food_ids, quantity_g = quantities), n)
shiny::runApp(system.file("shiny", package = "vetnutriDogR"))
```

L'interface expose poids actuel/idéal, coefficients issus du référentiel,
variables supplémentaires en JSON (`AW`, `wG`, `L`, `wL`…), quantités des aliments,
bilan énergétique, seuils, diagnostics et export CSV traçable. « Charger /
actualiser » relit INIT. L'ajout d'un aliment, d'une composition ou d'un seuil est
visible sans modification du code. La découverte d'un nouveau nutriment dépend
également de sa définition Kotlin ; sinon un diagnostic le signale.

Rapport Quarto : `quarto render vetnutriDogR/inst/quarto/catalogue.qmd`.
Il charge directement les sources R voisines, sans installation du package
`vetnutriDogR` (dépendances : jsonlite et knitr). Après déplacement hors du dépôt,
définir `VETNUTRI_MP_ROOT` ou le paramètre `root`.

## 5. Validation et limites explicites

```sh
bash vetnutriDogR/scripts/validate-kotlin.sh
Rscript vetnutriDogR/tests/init-parity.R
```

Le test Kotlin produit `vetnutriDogR/build/kotlin-parity.json` depuis les
JSON courants, avec leur empreinte. Le test R refuse un oracle obsolète. Il
compare noms, groupes, catégories, composition et DM (y compris absence), énergie,
besoin standard et poids métabolique. Les tests R comparent aussi tous les seuils
canins, leurs unités et conditions aux JSON, vérifient les erreurs de schéma,
la propagation des absences, la sécurité des expressions, les conversions et la
prise en compte d'un nouvel aliment. Sans oracle, la parité de calcul n'est pas
annoncée comme vérifiée.

Écarts volontaires et périmètre restant :

- Une composition absente reste NA dans les totaux/comparaisons R, avec statut
  DONNEES_ABSENTES. Kotlin peut afficher une somme partielle. Les zéros de repli
  des équations énergétiques/complémentaires sont, eux, reproduits et signalés.
- Les références maladies sont chargées pour audit ; leur combinaison, ENERCOMP,
  les plans évolutifs et les recettes imbriquées ne sont pas proposés par ce
  moteur. L'interface utilise une référence générale canine explicite.
- La ration manuelle utilise des quantités saisies. Le QMD propose aussi une
  [exploration séquentielle des combinaisons](EXPLORATION.md), avec calcul des
  quantités par déficit et énergie restante. Aucun profil animal ou coefficient
  nutritionnel n'est inventé.
- Les données canoniques INIT actuelles sont couvertes. Les heuristiques fuzzy
  du résolveur Kotlin pour les anciennes appellations ne sont pas portées : un
  nouveau label inconnu déclenche un diagnostic, puis bloque son calcul.
- L'extraction des enums nécessite le checkout Kotlin. Une syntaxe non reconnue
  doit être signalée, et non remplacée par une table périmée. Une modification
  de logique métier exige de relancer les tests de parité ; seules les mises à
  jour de données sont automatiquement synchronisées à la lecture.


Validation exécutée lors de l'implémentation : **21 comparaisons aliments/besoins**
et **220 seuils convertis** concordent avec l'oracle Kotlin canin (tolérance
relative 1e-8). Installation du package, test du serveur Shiny et rendu HTML
Quarto vérifiés. Cette couverture concerne les échantillons des tests, pas tous
les profils canins ni toutes les combinaisons de ration possibles.
