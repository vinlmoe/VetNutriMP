# Plan — Consultation évolutive (plan de ration multi-étapes)

## Objectif

Une **consultation évolutive** porte **un seul plan** de rations (une ration par *étape*). Chaque étape a
son propre poids et ses propres variables d'énergie. Les besoins sont recalculés pour chaque étape. On
peut éditer et ajuster les masses des ingrédients, et voir l'analyse des besoins, étape par étape.

Cas couverts : **croissance** (poids, poids adulte), **gestation** (semaine de gestation, taille de
portée), **lactation** (semaine de lactation, taille de portée) et **activité** (distance, charge
portée), ainsi que toute évolution décrite par des variables d'équation.

## Décisions validées

| Sujet | Décision |
|---|---|
| Nombre de plans | **1 plan par consultation** |
| Poids réel | `consultation.weight` reste le **seul poids réel**. Il est **toujours une colonne du plan** (étape avec `poids = null`) |
| Poids des autres étapes | Stockés **au niveau de la ration** uniquement (hors historique des poids) |
| Type | `STANDARD` / `EVOLUTIVE`, plus un profil facultatif (croissance, gestation, lactation, activité) servant de préréglage |
| Variables par étape | Variables des **équations d'énergie** de la référence, plus celles des équations **ENERCOMP** des références complémentaires chargées |
| Ordonnance | Tableau : ingrédients en lignes, étapes en colonnes. **Pas d'interpolation** |

## 1. Modèle de données (lot 1 — ✅ réalisé)

### Consultation
- `ConsultationEv` / `ConsultationEntity` :
  - `typeConsultation: String = "STANDARD"` (enum `TypeConsultation { STANDARD, EVOLUTIVE }`) ;
  - `profilEvolutif: String? = null` (enum `ProfilEvolutif { CROISSANCE, GESTATION, LACTATION, ACTIVITE, AUTRE }`).
- Le profil sert seulement au préréglage : les variables proposées par défaut et le libellé. Aucun calcul
  n'en dépend ; seules les équations de la référence pilotent le calcul.

### Ration (= étape du plan)
- `Ration` / `RationEntity` :
  - `etapeEvolutive: Boolean = false` : la ration appartient au plan de la consultation. Il n'y a qu'un
    plan par consultation, donc pas besoin d'identifiant de groupe ;
  - `poids: Double? = null` : `null` signifie qu'on prend le **poids réel** de la consultation ;
  - `suppVarp: MutableList<SupplementalvariableP>` : les variables propres à l'étape.
- Nouvelle table `RATION_SUPPLEMENTAL_VARIABLES(idRation, variableKind, value)` :
  - clé primaire `(idRation, variableKind)` ;
  - clé étrangère vers `RATIONS.uuid` avec `ON DELETE CASCADE` ;
  - c'est la même structure que `SUPPLEMENTAL_VARIABLES`.

### Migration et persistance
- `createMigration36to37()` :
  - 2 `ALTER TABLE CONSULTATION ADD COLUMN` ;
  - 2 `ALTER TABLE RATIONS ADD COLUMN` ;
  - `CREATE TABLE RATION_SUPPLEMENTAL_VARIABLES` et son index.
- Passer à `version = 37` dans `AppDatabase`, ajouter un DAO et les mappers (`Mappers.kt`), puis mettre à
  jour le chargement et l'enregistrement dans `DatabaseAnimalRepository` (avec `withContext(AppDispatchers.IO)`).
- `BackupService` : les anciennes sauvegardes restent compatibles grâce aux valeurs par défaut. Il faut
  exporter la nouvelle table.
- `duplicateRation()` recopie `poids`, `etapeEvolutive` et `suppVarp`. Les recettes (`recette = true`)
  n'ont jamais de `poids` ni de variables.

### Contrainte « poids réel toujours présent »
- À la création du plan, une étape avec `poids = null` est créée automatiquement.
- La dernière étape au poids réel ne peut pas être supprimée.
- Plusieurs étapes peuvent être au poids réel si seules leurs variables diffèrent (activité : même
  poids, distances différentes).

## 2. Résolution des variables et calculs par étape (lot 2 — ✅ réalisé)

C'est le lot structurant. Il doit être **iso-fonctionnel** pour les consultations standard.

### 2.1 Résolution unique : `VariablesEtape` (nouveau, `Utils/` ou `Data/`)
```kotlin
fun poidsEtape(c: ConsultationEv, r: Ration?): Double? =
    if (c.isEvolutive) (r?.poids ?: c.weight) else c.effectiveWeight

fun variablesEtape(c: ConsultationEv, r: Ration?): Map<String, Double>
    // ordre de priorité : ration.suppVarp  >  consultation.suppVarp  >  valeurs par défaut
    // + BW = poidsEtape(c, r)
```
- En consultation évolutive, `idealWeight` ne doit **pas** remplacer le poids de l'étape.
- **Nommage des variables** : le nom d'une variable est celui qui est **écrit dans l'équation**
  (`VariableKind.label` : `AW`, `wG`, `wL`, `L`, `D`, `CW`...). Aujourd'hui,
  `calculerPoidsMetabolique` et `calculerBesoinEnergetiqueStandard` injectent `varKind.variable`
  (`adultWeight`) puis passent par une table de traduction (`mapperVariablesEquation`), alors que
  `EquationEvaluator.calculerEnergieAdditionnelle` injecte directement `varKind.label`.
  `variablesEtape` injectera uniquement les noms des équations, et la table de traduction sera
  supprimée.

### 2.2 Variables éditables par étape
- Déplacer `extraireVariablesRequises()` depuis `View/ConsultationFullScreenEditView.kt:2119` vers
  `Utils/VariablesEnergie.kt` : c'est une règle de calcul, elle ne doit pas vivre dans un Composable.
- Créer une variante `variablesEnergieRequises(reference, referencesComplementaires)` limitée à :
  - `equationBW`, `equationBEE`, `equationDEcom`, `equationDEraw`, `equationME` ;
  - les équations `ENERCOMP` des références complémentaires sélectionnées ;
  - en excluant les variables calculées : `BW` (porté par `poids`), `MW`, `BEE`, `BE`.
- Exemples de variables éditables selon la référence : `AW`, `wG`, `wL`, `L`, `D`, `CW`.

### 2.3 Calculs paramétrés par étape (`AnimalDetailViewModel`)
- Faire recevoir `(poids, variables)` à `calculerPoidsMetabolique`, `calculerBesoinEnergetiqueStandard`
  et `updateEnergieAdditionnelle`, au lieu de lire `consultation.effectiveWeight` et `suppVarp`.
- Ajouter `calculerValeursMetaboliques(consultation, ration)`, appelé depuis `selectRation()` et à chaque
  modification du poids ou des variables de l'étape.
- Ajouter un `StateFlow` `poidsEffectif`. Il remplace les `selectedConsultation?.effectiveWeight` passés en
  `poidsAnimal` : `RationsView.kt` l.1518, 1721, 1748, 2034 et `AnimalDetailView.kt` l.784, 803.
- `RationAnalyzer.analyserRation(ration, consultation)` : lui passer une consultation *virtuelle*,
  `consultation.copy(weight = poidsEtape, idealWeight = null, suppVarp = variablesFusionnées)`. On évite
  ainsi de modifier l'analyseur.
- Clé du cache d'analyse : `"${ration.uuid}:${consult.uuid}:${poids}:${hash(variables)}"`.
- `RationAggregator` : exclure les étapes évolutives des analyses groupées (actuelles et proposées).
- `CrossConsultationAnalysisViewModel` (l.396 et 485) : passer par `poidsEtape`.

Exemple : les équations de croissance se recalculent d'elles-mêmes, puisque `BW` change à chaque étape :

$$BEE = 130\,BW^{0{,}75}\times 3{,}2\left[e^{-0{,}87\,p}-0{,}1\right],\qquad p=\frac{BW}{AW}$$

C'est la même chose pour la gestation ($wG$) ou la lactation ($L$, $wL$) : chaque étape porte ses propres
valeurs.

## 3. Saisie du type de consultation (lot 3 — ✅ réalisé)
- Dans `ConsultationFullScreenEditView` et `ConsultationEditDialog`, ajouter un sélecteur
  « Standard / Évolutive » et, si « Évolutive », le choix du profil.
- Les variables communes à toutes les étapes restent sur la consultation (par exemple `AW` en
  croissance). Le profil ne fait que suggérer quelles variables seront définies par étape.
- Ajouter un badge « Évolutive · Croissance » (ou autre profil) dans `ConsultationsView` et
  `AnimalDetailComponents`.
- Repasser une consultation en « Standard » : les étapes redeviennent des rations ordinaires
  (`etapeEvolutive = false`). On demande confirmation et on ne supprime aucune donnée.

## 4. Vue `PlanEvolutifView` (lot 4 — ✅ réalisé)

Nouvelle section `AnimalDetailSection.PLAN_EVOLUTIF` :
- visible seulement si la consultation est évolutive ;
- branchée dans les deux mises en page de `AnimalDetailView` (l.1472 et 3168) ;
- logique dans `PlanEvolutifViewModel` (ou une extension d'`AnimalDetailViewModel`), conformément à
  CLAUDE.md.

```
┌ Plan évolutif — Croissance                 [+ Étape] [Propager aliments] [Ajuster tout] ┐
│ Étapes :  ( 3 kg )  ( 5,2 kg ● réel )  ( 8 kg )  ( 12 kg )  ( 18 kg )    ← triées ↑     │
│ Étape sélectionnée : Poids [8 kg]   AW [25]   (variables requises par les équations)    │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│ Onglet [Étape]     → RationsView restreinte à la ration de l'étape                      │
│ Onglet [Synthèse]  → matrice ingrédients × étapes éditable + BE requis / apporté / %    │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

- **Tri** : par `poids` croissant (le poids réel est placé à sa valeur), puis par les valeurs des
  variables qui diffèrent d'une étape à l'autre (par exemple $wG$, $D$).
- **Libellé d'étape** : le poids, suivi des variables qui diffèrent entre étapes (par exemple
  « 32 kg · wG 6 », « 20 kg · D 15 »).
- **Création du plan**, depuis une ration existante de `RationsView` : action « Créer le plan
  évolutif ». La ration est dupliquée pour chaque étape saisie (liste de poids ou de valeurs de
  variables), et l'étape au poids réel est ajoutée d'office.
- **Onglet « Étape »** : `RationsView` est réutilisée avec un paramètre `rationsFiltre`. On garde
  l'édition des masses, `MultiNutrientAdjustmentView` et l'analyse des besoins, qui sont justes
  pour l'étape grâce au lot 2.
- **Onglet « Synthèse »** :
  - lignes : ingrédients, rassemblés par `refAlimUnif` ;
  - colonnes : étapes ;
  - cellules éditables, via `updateRationAliments` ;
  - lignes de pied : BE requis, énergie apportée, pourcentage de couverture, nombre de nutriments hors
    cible.
- **Propager aliments** : un aliment ajouté à une étape est proposé aux autres étapes à 0 g.
- **Ajuster tout** : lance l'ajustement multi-nutriments sur chaque étape, l'une après l'autre.

## 5. Ordonnance (lot 5 — ✅ réalisé)
- `ExportData` : ajouter `typeConsultation`, `profilEvolutif` et `planEvolutif: List<Ration>` (déjà
  trié).
- `HtmlDocumentBuilder.buildPrescriptionHtml` : si la consultation est évolutive, appeler
  `buildPlanEvolutifTableBlock(plan)` à la place de `buildRationsBlocks`.

| Ingrédient | 3 kg | 5,2 kg (actuel) | 8 kg | 12 kg | 18 kg |
|---|---|---|---|---|---|
| Aliment A | 85 g | 120 g | … | … | … |
| Complément B | ½ c. | 1 c. | … | … | … |
| **Total (g/j)** | … | … | … | … | … |
| **Énergie (kcal/j)** | … | … | … | … | … |

- En-têtes de colonnes : le libellé d'étape défini au §4.
- Unités : réutiliser `calculerQuantiteEnUnites()` pour afficher les sachets et cuillères entre
  parenthèses.
- Mise en page paysage automatique au-delà de 5 colonnes.
- **Aucune interpolation** et aucune note sur les étapes intermédiaires.
- Écran ordonnance : le plan se sélectionne d'un bloc, et non étape par étape.

## 6. Tests et finitions (lot 6)
- Tests unitaires :
  - `poidsEtape` et `variablesEtape` (priorités, cas `null`, standard contre évolutive) ;
  - `variablesEnergieRequises` (avec et sans ENERCOMP) ;
  - tri et libellés des étapes ;
  - `buildPlanEvolutifTableBlock` ;
  - migration 36 → 37 ;
  - **non-régression** des BEE et du BE total en consultation standard.
- Ordre de livraison conseillé : **1 → 2** (valider seul, c'est le plus risqué) **→ 3 → 4 → 5 → 6**.
