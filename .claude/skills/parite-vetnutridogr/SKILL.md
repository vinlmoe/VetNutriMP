---
name: parite-vetnutridogr
description: Garder le module R vetnutriDogR (package R, Shiny, Quarto d'exploration de rations canines) cohérent avec le code Kotlin de VetNutriMP — enums de nutriments lus dans les sources Kotlin, JSON INIT partagé, oracle de parité Kotlin/R. À utiliser quand on modifie un calcul nutritionnel Kotlin (besoins, BEE, poids métabolique, arrondis, conformité, exploration multiration), un enum de nutriment ou d'unité, le JSON INIT, ou quand on travaille dans vetnutriDogR/.
---

# Parité vetnutriDogR ↔ Kotlin

`vetnutriDogR/` est un module R autonome (package, application Shiny, documents Quarto) qui refait les
calculs canins de VetNutri MP **sans dupliquer les données** :

- il lit le JSON INIT (`composeApp/src/commonMain/resources/data/vetnutri_export_init.json`) ;
- il lit les enums de nutriments et d'unités **dans les sources Kotlin**
  (`composeApp/src/commonMain/kotlin/fr/vetbrain/vetnutri_mp/Enumer/*.kt`) par expressions régulières
  (`vetnutriDogR/R/vetnutri_data_model.R`, `vn_kotlin_dictionaries`) ;
- il reproduit la logique de calcul (`vetnutriDogR/R/init_calculations.R`, `ration_exploration.R`,
  `exploration_balance.R`), vérifiée contre un oracle Kotlin.

Documentation de référence : `vetnutriDogR/README.md`, `vetnutriDogR/docs/INIT_REFERENCE.md`
(écarts volontaires), `vetnutriDogR/docs/EXPLORATION.md`.

## Ce qui casse quoi

| Changement côté Kotlin | Effet côté R | À faire |
|------------------------|--------------|---------|
| Données du JSON INIT | relues automatiquement | `Rscript vetnutriDogR/tests/init-parity.R` (l'oracle refuse une empreinte périmée : relancer d'abord `validate-kotlin.sh`) |
| Nouvelle entrée d'enum de nutriment / `UnitEnum` / `UnitReqEnum` | lue automatiquement **si la syntaxe est respectée** | garder la forme des entrées voisines (skill `ajouter-nutriment`) ; sinon erreur « Schéma Kotlin non reconnu » |
| Nouvelle famille d'enum de nutriment | ignorée | l'ajouter à `classes` dans `vn_kotlin_dictionaries` avec la position de son label |
| Logique de calcul (besoins, BEE, `Data/CalculMetabolique.kt`, conversions d'unités, arrondis `arrondirQuantiteSelonRegles` (`View/AnalNut/MultiNutrientAdjustmentDialog.kt`), `calculerConformite`, ordre d'ajustement de `Data/ExplorationMultiration.kt`) | **non synchronisée** | porter la modification dans `vetnutriDogR/R/` puis relancer toute la parité |

## Lancer la parité

Depuis la racine du dépôt :

```bash
bash vetnutriDogR/scripts/validate-kotlin.sh       # oracle Kotlin → vetnutriDogR/build/kotlin-parity.json
Rscript vetnutriDogR/tests/init-parity.R            # compare R à l'oracle (tolérance relative 1e-8)
Rscript vetnutriDogR/tests/exploration.R
Rscript vetnutriDogR/tests/ajusteur-exploration.R
Rscript vetnutriDogR/tests/ajusteur-init.R
Rscript vetnutriDogR/tests/shiny-smoke.R            # nécessite shiny
```

`validate-kotlin.sh` compile `vetnutriDogR/tools/kotlin/InitRParityTest.kt` via un init script Gradle
(`tools/kotlin-parity.init.gradle`) et lance `:composeApp:desktopTest --tests
fr.vetbrain.vetnutri_mp.Interop.InitRParityTest` ; aucun fichier n'est ajouté durablement au projet
Kotlin. Dépendances R : `jsonlite` (obligatoire), `shiny`, `knitr`, `rmarkdown` ; Quarto pour les `.qmd`.

Si R n'est pas installé dans l'environnement : le dire explicitement, ne pas annoncer la parité
vérifiée, et indiquer les commandes ci-dessus à lancer. `validate-kotlin.sh` seul (Gradle) reste
utile : il vérifie que l'oracle compile et tourne.

## Modifier le code R

- Conventions du module : fonctions préfixées `vn_`, commentaires et documentation roxygen en anglais,
  messages et libellés d'interface en français, pas de table de nutriments recopiée en R (lire Kotlin,
  échouer clairement si la syntaxe n'est pas reconnue).
- Une donnée absente reste `NA` (statut `DONNEES_ABSENTES`) : écart volontaire avec Kotlin, documenté
  dans `INIT_REFERENCE.md`. L'option « Valeur absente = 0 (comme Kotlin) » de l'explorateur reproduit
  le comportement Kotlin.
- Toute nouvelle règle portée de Kotlin : citer le fichier Kotlin source dans un commentaire, ajouter un
  cas à l'oracle (`tools/kotlin/InitRParityTest.kt`) et à `tests/init-parity.R`.
- Mettre à jour `docs/INIT_REFERENCE.md` (écarts et périmètre) ou `docs/EXPLORATION.md`.
- Messages de commit du module : préfixe `vetnutriDogR : …` (convention de l'historique).

`vetnutriExamR/` est un module distinct (correction d'examens) : ne pas le modifier pour vetnutriDogR.
