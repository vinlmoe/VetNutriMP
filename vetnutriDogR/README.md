# vetnutriDogR

Module autonome de nutrition **canine**, indépendant de `vetnutriExamR`.
Il regroupe le package R, l'application Shiny, le rapport Quarto, la documentation
et les tests de parité Kotlin. Les données sont lues directement dans les JSON
INIT de VetNutri MP ; les dictionnaires sont lus dans son code Kotlin.

## Lancement sans installer le package

Depuis la racine de VetNutri MP :

```sh
Rscript -e 'install.packages(c("jsonlite", "shiny"))'
Rscript vetnutriDogR/scripts/run-app.R
```

Le dossier peut être déplacé hors du dépôt. Indiquer alors le checkout source :

```sh
Rscript /chemin/vetnutriDogR/scripts/run-app.R /chemin/VetNutriMP
```

Aucun fichier de `vetnutriExamR` n'est nécessaire. L'autonomie concerne le module ;
le checkout VetNutri MP reste nécessaire pour lire les données et règles de
référence sans les dupliquer. `VETNUTRI_MP_ROOT` permet aussi de fixer son chemin.

## Document Quarto interactif

Le document `inst/quarto/ration_interactive.qmd` explore toutes les combinaisons
d'ingrédients sélectionnés pour les protéines, fibres, calcium, oméga-6, sodium
et énergie restante, sur des intervalles de poids et de K. Les cibles du
référentiel sont modifiables. L'option « Valeur absente = 0 (comme Kotlin) »
permet de calculer les aliments à composition incomplète, en signalant les zéros
utilisés. Il fournit quantités, conformité, diagnostics,
comparaisons et exports pour chaque scénario.

[Guide de l'exploration : méthode, cibles, unités et résultats](docs/EXPLORATION.md).
L'application Shiny classique conserve la saisie manuelle d'une ration ; les
deux interfaces utilisent le même moteur de composition et d'évaluation.

Depuis la racine du dépôt, sans installer le package `vetnutriDogR` :

```sh
Rscript -e 'install.packages(c("jsonlite", "shiny", "knitr", "rmarkdown"))'
cd vetnutriDogR/inst/quarto
quarto serve ration_interactive.qmd
```

Ce document nécessite une session R active : utiliser **`quarto serve`** pour
les calculs interactifs. Un fichier HTML ouvert seul ne permet pas ces calculs.
Après déplacement du module, définir `VETNUTRI_MP_ROOT` comme pour Shiny.
Le document `catalogue.qmd` reste le rapport statique du catalogue.

## Utilisation comme package R

```sh
R CMD INSTALL vetnutriDogR
```

```r
library(vetnutriDogR)
Sys.setenv(VETNUTRI_MP_ROOT = "/chemin/VetNutriMP")
catalogue <- vn_load_init()
shiny::runApp(system.file("shiny", package = "vetnutriDogR"))
```

## Organisation

```text
vetnutriDogR/
├── DESCRIPTION, NAMESPACE, LICENSE
├── R/                  # Chargement, validation, normalisation, calculs
├── inst/shiny/         # Application canine
├── inst/quarto/        # Rapport du catalogue
├── man/                # Aide du package R
├── docs/               # Analyse du schéma et des règles Kotlin
├── tests/              # Tests des données, des calculs et de Shiny
├── tools/              # Oracle Kotlin et intégration Gradle temporaire
└── scripts/            # Lancement et validation
```

## Validation et rapport

Depuis la racine de VetNutri MP :

```sh
bash vetnutriDogR/scripts/validate-kotlin.sh
Rscript vetnutriDogR/tests/init-parity.R
Rscript vetnutriDogR/tests/shiny-smoke.R # shiny installé ; package facultatif
quarto render vetnutriDogR/inst/quarto/catalogue.qmd # jsonlite et knitr installés
```

Le rapport charge directement les fichiers du dossier `R/` voisin : il ne
nécessite pas l'installation du package `vetnutriDogR`. Conserver le rapport
dans le module pour ce mode de lancement.

Le test Kotlin compile l'oracle depuis ce dossier, sans ajouter de source au
projet Kotlin de façon permanente. Il produit `build/kotlin-parity.json` dans
le module. Après déplacement, passer le chemin du dépôt à `validate-kotlin.sh`,
puis exécuter les tests R depuis le dossier du module avec `VETNUTRI_MP_ROOT`
défini. Le rapport et Shiny acceptent la même variable d'environnement.

[Schéma INIT, règles de calcul, traçabilité et limites](docs/INIT_REFERENCE.md).
Les combinaisons de références maladies et les plans évolutifs ne sont pas
implémentés ; les données absentes sont signalées.
