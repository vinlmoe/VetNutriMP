# Spécification — Correction automatique de l'examen S4 VetNutri

> Document destiné à un **agent IA** chargé de **réimplémenter** ce système
> de correction dans un autre langage / framework (Python, TypeScript, Go,
> Kotlin, etc.). Cette spec est **autonome** : elle ne suppose pas que vous
> ayez accès au code R d'origine.

---

## 1. Contexte

Examen *« S4 — Élevage des animaux de compagnie — Nutrition avec VetNutri (6 pts) »*.

- Les étudiants utilisent l'application **VetNutri** (web).
- À la fin de l'épreuve, chaque étudiant **publie son travail** dans un *bin*
  JSONBin et **colle l'identifiant du bin** comme réponse à la question
  Moodle. L'enseignant récupère un CSV Moodle qui contient une colonne
  `Réponse 1` = `bin_id`.
- L'examen comporte **deux exercices** :
  - **EX1** : sur l'animal nommé `EX1` (champ `examExerciseId == "EX1"`).
    On évalue 5 items (cf. § 5.1).
  - **EX2** : sur l'animal nommé `EX2`. On évalue la composition de la
    ration proposée en 5 catégories d'ingrédients (cf. § 5.2).

Note finale **/6** = somme pondérée des items.

---

## 2. Format du JSON VetNutri

### 2.1. Wrapper JSONBin

```jsonc
// GET https://api.jsonbin.io/v3/b/<bin_id>/latest
// header X-Master-Key: <api key>
{
  "record": { /* objet VetNutri ci-dessous */ },
  "metadata": { ... }
}
```

→ déballer `.record` avant tout traitement.

### 2.2. Schéma simplifié

```jsonc
{
  "version": "1.0.0",
  "generatedAtEpochMs": 1774250589868,

  "animals": [
    {
      "uuid": "animal-1",
      "name": "Foeb",
      "examExerciseId": "EX1",        // "EX1" ou "EX2"
      "examStudentId":  "...",
      "examStudentNumber": "...",
      "consultations": [
        {
          "uuid": "consult-1",
          "date": "2026-03-23",
          "weightKg": 23,             // poids actuel
          "PoidsIdeal": 25,           // poids idéal (clé canonique)
          "PoidsIdealex": true,       // booléen : poids idéal renseigné
          "k1Value": 0.8,             // coefficients k1..k5 (défaut 1)
          "k2Value": 0.8,
          "k3Value": 0.8,
          "k4Value": 1.0,
          "k5Value": 1.0,
          "coefficientAjustement": 1, // == ky (défaut 1)
          "referenceGeneraleId": "ref-1",
          "rations": [
            {
              "uuid": "ration-1",
              "name": "Ration actuelle",
              "isCurrent": true,      // true=ACTUELLE, false=PROPOSEE
                                      //   (fallback : si name contient
                                      //   "actuelle" ou "current", ACTUELLE)
              "items": [
                { "uuid": "item-1", "foodId": "H20-20", "quantity": 170 }
              ]
            }
          ]
        }
      ]
    }
  ],

  "foods": [
    {
      "uuid": "H20-20",
      "name": "k/d + Mobility Canine",
      "nutrients": {           // alternative : "valMap"
        "HUMIDITE":  8,        // % matière brute
        "PROTEINE":  14.2,     // % MB
        "LIPIDE":    19.2,     // % MB
        "ENA":       52.2,     // % MB (peut être absent → calcul de fermeture)
        "CELLULOSE": 2.3,      // % MB
        "CENDRE":    4.1,      // % MB
        "ENERGIE":   380,      // kcal / 100 g (peut être absent → Atwater)
        "CAL":       0.59,     // % MB (calcium)
        "PHOS":      0.27      // % MB (phosphore)
      }
    }
  ],

  "equations": [
    {
      "uuid": "eq-bee-1",
      "name": "Maintenance Energy requirement Dog (ME)",
      "kind": "ENERGYNEED",      // BEC : besoin énergétique
      "script": "130*BW^0.75"    // évalué avec BW = poids choisi
    },
    {
      "uuid": "eq-de-1",
      "name": "Atwater DE (ME)",
      "kind": "ENERGYDENSITY",   // densité énergétique des aliments
      "script": "4*ENA+4*PROTEINE+9*LIPIDE"
    }
  ],

  "references": [
    {
      "uuid": "ref-1",
      "equationBEE":   "eq-bee-1", // équation BEC associée
      "equationDEcom": "eq-de-1",  // équation densité énergétique
      "nutrients": [
        {"nutrientLabel": "PROTEINE", "reflevel": "MIN", "quantity": 20},
        {"nutrientLabel": "CAL",      "reflevel": "MIN", "quantity": 0.5}
      ]
    }
  ]
}
```

### 2.3. Tolérance aux variantes de noms

Pour rester robuste, accepter ces alias (premier non-vide gagne) :

| Champ logique           | Alias acceptés                                                             |
|-------------------------|----------------------------------------------------------------------------|
| `consultation.uuid`     | `uuid`, `UUID`                                                             |
| `consultation.weightKg` | `weightKg`, `Poids`                                                        |
| `consultation.k1..k5`   | `k1Value`/`k1value`, `k2Value`/`k2value`, …                                 |
| `consultation.ky`       | `coefficientAjustement`, `ky`                                              |
| `consultation.refId`    | `referenceGeneraleId`, `RefString`                                         |
| `ration.items`          | `items`, `alimentList`, `alimentMutableList`                               |
| `ration.isCurrent`      | `isCurrent`, `actual` (sinon : `name` matche `/actuelle\|current/i`)       |
| `food in item`          | `item.alime`, `item.aliment`, sinon lookup `foods` par `item.foodId`       |
| `food.name`             | `name`, `nom`                                                              |
| `food.nutrients`        | `nutrients`, `valMap`                                                      |
| `animal.name`           | `name`, `nom`                                                              |
| Poids idéal             | voir § 4.1 ci-dessous                                                      |

---

## 3. Pipeline global

```
CSV Moodle
   │
   ▼
extraire bin_id (colonne "Réponse 1")
   │
   ▼
┌─────────────────────────────┐
│  Cache local <bin_id>.json  │   (évite de re-télécharger)
└─────────────────────────────┘
   │ miss
   ▼
GET JSONBin → .record → cache
   │
   ▼
HYDRATATION des aliments     (§ 4.0)
   │
   ▼
extract_metrics_one(jsonRecord)
   │
   ▼
notation par item            (§ 5)
   │
   ▼
CSV notes + détails
```

**Modes de fonctionnement à exposer en paramètres :**

| Param                | Effet                                                              |
|----------------------|--------------------------------------------------------------------|
| `FORCE_REFRESH`      | Re-télécharge tous les bins, écrase le cache                       |
| `OFFLINE_ONLY`       | Ne fait aucun appel réseau ; bins absents = erreur                 |
| `HYDRATE_ENA`        | Recalcule `ENA` manquant via formule de fermeture                  |
| `FORCE_ATWATER`      | Ignore `ENERGIE` et force le calcul Atwater pour la densité        |
| `PICK_FIRST_PROPOSED`| Si plusieurs rations PROPOSEE, prend la 1ʳᵉ (ne somme pas)        |
| `IDEAL_WEIGHT_KG`    | Cible pédagogique (25 kg)                                          |
| `WEIGHT_TOL_PCT`     | Tolérance autour du poids idéal (±10 %)                            |
| `TOL_PCT`            | Tolérance par défaut sur les autres bornes (±10 %)                 |

---

## 4. Calculs

### 4.0. Hydratation des aliments (à faire en tout premier)

Pour chaque `food` :

1. Si `FORCE_ATWATER` est vrai et `nutrients.ENERGIE` existe → **supprimer**
   `ENERGIE` (forcera le calcul Atwater plus loin).
2. Si `HYDRATE_ENA` est vrai et `ENA` est manquant ou non-fini :
   - Lire `HUMIDITE, PROTEINE, LIPIDE, CELLULOSE, CENDRE`.
   - Si **toutes** sont disponibles :
     `ENA = max(0, 100 − HUMIDITE − PROTEINE − LIPIDE − CELLULOSE − CENDRE)`
   - Sinon : laisser `ENA` à `NA` et incrémenter `n_foods_missing_ena`
     (diagnostic).

Exposer en sortie : `n_foods_missing_ena`, `cleared_energie`, `fixed_ena`.

### 4.1. Recherche du poids (BW)

Stratégie :

1. Chercher dans `consultation`, puis dans `animal`, le **flag**
   `PoidsIdealex` :
   - Si présent et `== false` → poids idéal explicitement non-saisi → on
     considère qu'il n'y en a pas.
2. Sinon, dans la même séquence (consultation puis animal), chercher la
   **valeur** dans cet ordre de priorité :
   `PoidsIdeal, poidsIdeal, poids_ideal, PoidsIdeal_kg, idealWeightKg,
    weightIdealKg, weight_ideal_kg, idealWeight, targetWeightKg,
    ideal_weight, bcsIdealWeight, bodyConditionIdealWeight`
   Première valeur strictement positive trouvée → `weight_ideal_kg`.
3. Lire en parallèle `weight_actual_kg = consultation.weightKg ?? consultation.Poids`.

**Choix du poids utilisé (`weight_used_kg`) :**

| Cas                                       | `weight_used_kg`     | `weight_source`     |
|-------------------------------------------|----------------------|---------------------|
| `weight_ideal_kg` fini                    | `weight_ideal_kg`    | `"ideal"`           |
| `weight_ideal_kg` absent et `weight_actual_kg` fini | `weight_actual_kg` | `"actual_fallback"` (signaler !) |
| Aucun                                     | `NA`                 | `"none"`            |

L'item « poids idéal = 25 kg » de la grille (§ 5.1) est **noté 0** quand
`weight_ideal_kg` est `NA`, **même si** le BEC a pu être calculé avec le
fallback. Le fallback ne sert qu'à ne pas perdre les autres calculs.

### 4.2. Coefficients k1..k5 + ky

Pour la consultation EX1 :

```
k1 = consultation.k1Value ?? consultation.k1value ?? 1
k2 = consultation.k2Value ?? consultation.k2value ?? 1
k3 = consultation.k3Value ?? consultation.k3value ?? 1
k4 = consultation.k4Value ?? consultation.k4value ?? 1
k5 = consultation.k5Value ?? consultation.k5value ?? 1
ky = consultation.coefficientAjustement ?? consultation.ky ?? 1

k_total = k1 * k2 * k3 * k4 * k5 * ky
```

Exposer chacun individuellement dans la sortie (audit).

### 4.3. BEC (besoin énergétique)

```
1. Trouver la référence : ref = references[uuid == consultation.referenceGeneraleId]
2. Trouver l'équation BEE :
     - eq = equations[uuid == ref.equationBEE]
     - fallback : 1ʳᵉ équation avec kind == "ENERGYNEED"
3. Évaluer le script avec BW = weight_used_kg :
     base_need = eval(eq.script, { BW: weight_used_kg })
4. BEC = base_need * k_total
```

Exemple : équation `130*BW^0.75`, chien 25 kg, k_total = 1
→ BEC = 130 × 25^0.75 = **1452 kcal/j**.

**Évaluateur de script** (cf. § 7) — opérateurs/fonctions autorisés :
arithmétique standard, `^` (puissance), `abs, min, max, log, exp, sqrt`.
Variables injectées : `BW` pour ENERGYNEED, et toutes les clés
`food.nutrients` pour ENERGYDENSITY.

### 4.4. Apport énergétique de la ration proposée

1. Sélectionner la **1ʳᵉ** ration `PROPOSEE` (`isCurrent == false` ou nom
   sans `actuelle/current`) de l'animal EX1.
2. Pour chaque item de cette ration :
   - `food = foods[item.foodId]` (ou `item.alime`/`item.aliment`).
   - **Densité énergétique** (kcal/100 g MB) :
     1. Si `food.nutrients.ENERGIE` existe (et `FORCE_ATWATER` faux) → l'utiliser.
        (Idem pour les alias `EM`, `KCAL`, `ME`.)
     2. Sinon : `eval(equationDEcom.script, food.nutrients)`
        → typiquement `4*ENA + 4*PROTEINE + 9*LIPIDE`.
     3. Sinon (rien) : `NA` (signalé via `n_items_missing_energy`).
   - `energy_intake_item_kcal = quantity_g × energy_density / 100`.
3. **Apport total ration** : somme stricte (`na.rm = false`) sur les items
   uniques `(ration_id, item_uuid)`.

### 4.5. Densité énergétique de la ration proposée

```
mass_g = Σ quantity_g des items uniques de la ration choisie
ex1_dens_kcal_100 = 100 × ex1_energy_intake_kcal / mass_g
```

**Important** : cette densité est calculée **au niveau de la ration**, pas
en pondérant les densités des aliments. C'est mathématiquement
équivalent à la densité pondérée par les masses.

### 4.6. Apport en protéines de la ration proposée

Pour chaque item de la ration choisie :

```
protein_intake_item_g = quantity_g × food.nutrients.PROTEINE / 100
```

(`PROTEINE` est en % MB pour 100 g.)

```
ex1_proteine_g = Σ protein_intake_item_g sur les items
                 (na.rm = false : somme = NA si un item manque PROTEINE)
```

Si `n_items_missing_proteine > 0` pour une copie → la note protéine sera
0, et c'est intentionnel (plutôt qu'un faux positif silencieux).

### 4.7. Liste des ingrédients (EX1 et EX2)

Pour la ration proposée choisie :

```
ex1_food_names = unique(items.food.name)  (joints par " | ")
ex2_food_names = pareil mais pour l'animal EX2 (1ʳᵉ ration PROPOSEE)
```

---

## 5. Notation

### 5.1. EX1 (3 pts) — 5 items à 0.6 pt

| Item                      | Critère                                                          | Pts |
|---------------------------|------------------------------------------------------------------|-----|
| `pts_poids`               | `|ex1_weight_ideal_kg − 25| ≤ 25 × 10 %`                         | 0.6 |
| `pts_bec`                 | `BEC_LOW ≤ ex1_bec_kcal ≤ BEC_HIGH`                              | 0.6 |
| `pts_energie`             | `ENERGIE_INTAKE_LOW ≤ ex1_energy_intake_kcal ≤ ENERGIE_INTAKE_HIGH` | 0.6 |
| `pts_densite`             | `DENSITE_LOW ≤ ex1_dens_kcal_100 ≤ DENSITE_HIGH`                 | 0.6 |
| `pts_proteine`            | `PROTEINE_LOW ≤ ex1_proteine_g ≤ PROTEINE_HIGH`                  | 0.6 |

**Mode auto-bornes** : si une borne `LOW`/`HIGH` est `NA` (non fixée),
calculer automatiquement `[médiane × 0.9, médiane × 1.1]` à partir de
l'ensemble de la promo, pour ne pas bloquer la première exécution.

L'enseignant ajuste ensuite `BEC_LOW/HIGH`, etc., et re-exécute.

### 5.2. EX2 (3 pts) — 5 catégories à 0.6 pt

Sur la liste d'aliments de la 1ʳᵉ ration PROPOSEE de l'animal EX2,
détecter par mots-clés :

```
proteine_animale : poulet, dinde, boeuf, veau, agneau, lapin, porc,
                   jambon, cheval, canard, thon, saumon, poisson,
                   cabillaud, merlu, oeuf, viande, filet
feculent         : riz, pates, blé, semoule, pomme de terre, patate,
                   boulgour, quinoa, flocon, avoine, mais, tapioca, orge
legume           : courgette, carotte, haricot, epinard, brocoli, chou,
                   courge, potiron, concombre, tomate, salade,
                   petits pois, pois, legume
huile            : huile, olive, colza, tournesol, lin, matiere grasse
cmv              : cmv, complement, mineral, vitamine, calcium,
                   carbonate, phosphate, premix, pet-phos, nutri-mix,
                   vetcomplex
```

**Normalisation** : passer en minuscules + supprimer les accents
(NFD/ASCII translit) avant matching. La détection est par sous-chaîne.

`pts_ex2 = nb_catégories_détectées × 0.6`.

### 5.3. Note finale

```
note_sur_6 = pts_poids + pts_bec + pts_energie + pts_densite
           + pts_proteine + pts_ex2
note_sur_6 = 0   si EX1 ET EX2 sont tous deux absents (bin vide ou erreur)
```

---

## 6. Sortie

CSV `notes_examen_S4.csv` (1 ligne par étudiant). Colonnes minimales :

```
nom, prenom, email, bin_id,
ex1_present,
ex1_weight_ideal_kg, ex1_weight_field, ex1_weight_actual_kg,
ex1_weight_used_kg, ex1_weight_source, ex1_poids_ok,
ex1_k1, ex1_k2, ex1_k3, ex1_k4, ex1_k5, ex1_ky, ex1_k_total,
ex1_bec_kcal, ex1_bec_ok,
ex1_energy_intake_kcal, ex1_energie_ok,
ex1_dens_kcal_100, ex1_densite_ok,
ex1_proteine_g, ex1_proteine_ok,
ex1_food_names,
ex2_present,
ex2_cat_proteine_animale, ex2_cat_feculent, ex2_cat_legume,
ex2_cat_huile, ex2_cat_cmv,
ex2_n_categories_ok,
pts_poids, pts_bec, pts_energie, pts_densite, pts_proteine, pts_ex2,
note_sur_6,
n_foods_missing_ena, n_items_missing_energy, n_items_missing_proteine,
ex1_n_proposed_rations
```

CSV `notes_details_examen_S4.csv` = idem + `ex1_food_names`,
`ex2_food_names`.

---

## 7. Évaluateur d'expressions (sandbox)

Les `equations[].script` viennent du JSON utilisateur. À évaluer dans une
**sandbox** sans accès au système de fichiers / réseau.

- Variables injectées :
  - pour `ENERGYNEED` : `BW` (nombre).
  - pour `ENERGYDENSITY` : toutes les clés de `food.nutrients`
    (`ENA, PROTEINE, LIPIDE, HUMIDITE, ...`).
- Opérateurs : `+ - * / ^ %` et parenthèses.
- Fonctions autorisées : `abs, min, max, log, exp, sqrt`.
- Comportement en cas d'erreur (variable manquante, parse error,
  division par zéro) : retourner `NaN`/`null` plutôt que lever une
  exception. Le diagnostic est porté par les compteurs
  `n_foods_missing_ena`, `n_items_missing_energy`,
  `n_items_missing_proteine`.

**Implémentations possibles** :

| Langage    | Approche                                                         |
|------------|------------------------------------------------------------------|
| Python     | `asteval`, ou `ast.parse` + walker custom (refuser `Call`/`Attr`) |
| TypeScript | `expr-eval`, `mathjs` (mode safe, pas de `eval`)                 |
| Go         | `github.com/Knetic/govaluate`                                    |
| Kotlin     | `objecthunter/exp4j`                                             |

⚠️ **Ne jamais** utiliser `eval()` JS / `exec()` Python : les scripts
viennent d'un fichier utilisateur potentiellement hostile.

---

## 8. Audits / bugs corrigés (B1–B5)

Pour reproduire le comportement attendu, ces bugs sont déjà corrigés
dans la spec ci-dessus. Pour mémoire :

- **B1** : ne pas calculer le BEC sur `weightKg` (poids actuel) — utiliser
  `PoidsIdeal`, fallback `weightKg` avec signal explicite (§ 4.1, § 4.3).
- **B2** : `ENA` manquant ⇒ Atwater retourne `NaN` silencieusement.
  Hydrater par formule de fermeture (§ 4.0).
- **B3** : plusieurs rations PROPOSEE ⇒ prendre la **1ʳᵉ**, ne pas
  sommer (§ 4.4–4.6).
- **B4** : si `food.nutrients.ENERGIE` est présent, il court-circuite
  Atwater. Option `FORCE_ATWATER` pour le supprimer avant calcul (§ 4.0).
- **B5** : ne pas masquer les données manquantes via `na.rm=true` dans
  les sommes du dénominateur. Préférer `na.rm=false` + compteurs
  diagnostiques (§ 4.4, § 4.6).

---

## 9. Tests minimaux à implémenter

Cas dorés (golden tests) sur un JSON minimal du type § 2.2 :

1. **BEC nominal** : chien 25 kg, équation `130*BW^0.75`, tous k=1
   → `BEC == 1452.30`.
2. **BEC avec coefficients** : k1=1.4, autres=1
   → `BEC == 1452.30 × 1.4 == 2033.22`.
3. **Densité Atwater** : aliment `PROTEINE=14.2, LIPIDE=19.2, ENA=52.2`
   → `density == 4×52.2 + 4×14.2 + 9×19.2 == 438.4 kcal/100g`.
4. **Apport énergie** : 170 g de cet aliment → `170 × 4.384 == 745.3 kcal`.
5. **Apport protéine** : 170 g × 14.2 % → `24.14 g`.
6. **Hydratation ENA** : aliment sans `ENA`, avec
   `HUMIDITE=8, PROTEINE=14.2, LIPIDE=19.2, CELLULOSE=2.3, CENDRE=4.1`
   → `ENA = 100 − 47.8 == 52.2` (puis tests 3–4 doivent passer).
7. **Fallback poids actuel** : pas de `PoidsIdeal`, `weightKg=23`
   → `weight_source == "actual_fallback"`, `pts_poids == 0`,
   `BEC` calculé sur 23 kg.
8. **Flag PoidsIdealex == false** : même si `PoidsIdeal` est numérique,
   on ignore et on tombe en fallback.
9. **Plusieurs rations PROPOSEE** : 2 rations proposées avec énergies
   différentes → garder la 1ʳᵉ uniquement.
10. **EX2 catégorisation** : `["Poulet", "Riz blanc", "Courgette",
    "Huile d'olive", "Pet-Phos"]` → 5 catégories détectées →
    `pts_ex2 == 3.0`.

---

## 10. Prompt à utiliser

> *Voici ci-après le prompt « clé en main » à donner à l'IA implémenteuse.*

```
Tu es chargé de réimplémenter en <LANGAGE> le système de correction
automatique d'examen décrit dans le fichier joint
SPEC_correction_examen_S4.md.

Contraintes :

1. Lis l'intégralité de la spec avant d'écrire la moindre ligne de code.
2. Reproduis fidèlement la sémantique de chaque calcul (§ 4) — y compris
   les choix d'audit B1–B5 (§ 8) et les tests dorés (§ 9).
3. L'évaluateur d'expressions DOIT être une sandbox (§ 7). N'utilise
   jamais `eval()` natif ou équivalent.
4. Expose les paramètres listés au § 3 sous forme de configuration
   (variables d'environnement, fichier YAML, ou flags CLI).
5. Implémente un cache local des bins JSONBin avec clés
   FORCE_REFRESH / OFFLINE_ONLY (§ 3).
6. Produis deux CSV de sortie conformes au § 6 (mêmes en-têtes, même
   ordre des colonnes).
7. Écris d'abord les tests dorés (§ 9). Ils doivent tous passer avant
   que la solution soit considérée comme finie.
8. Lorsque la spec est ambiguë ou silencieuse, choisis l'option la
   plus sûre (échec explicite plutôt que valeur par défaut silencieuse)
   et documente ton choix dans un README.

Livrable :

- Code source organisé en modules (io, nutrition, grading, cli).
- Tests unitaires couvrant § 9.
- Un README expliquant : installation, exécution sur un CSV Moodle,
  ajustement des bornes, mode hors-ligne, structure des CSV de sortie.
- Un fichier d'exemple JSONBin (cf. § 2.2) servant aux tests.

Ne demande à l'utilisateur aucune information qui figure déjà dans la
spec. Si une dépendance externe (HTTP client, parseur JSON, sandbox
d'expressions) est nécessaire, choisis une lib légère et populaire
dans l'écosystème <LANGAGE> et justifie-la en une phrase.
```

---

*Fin de la spec — version 1.0, 2026-04-27.*
