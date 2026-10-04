# Exploration de populations canines

Le QMD `inst/quarto/ration_interactive.qmd` explore des rations pour une grille de
profils canins, sans créer un dossier animal individuel. L'application Shiny
classique reste disponible pour la saisie manuelle d'une ration.

## Définir les scénarios

1. Charger le catalogue INIT et choisir un ou plusieurs référentiels généraux
   canins. Les références maladies ne sont pas combinées.
2. Fixer minimum, maximum et pas pour le poids en kg et pour **K global**.
   Le maximum est inclus, même si le dernier intervalle est plus court.
3. Choisir une liste d'ingrédients pour chacun des rôles : protéines, fibres,
   calcium, oméga-6, sodium et énergie restante. Un ingrédient est pris dans
   chaque liste pour chaque combinaison. Un même aliment peut figurer dans
   plusieurs listes ; ses quantités par rôle sont conservées et ses apports
   sont additionnés normalement.
4. Vérifier les cibles : par défaut OPTIMIN du référentiel. Le niveau, le facteur
   et, si nécessaire, une valeur et sa base peuvent être modifiés. Le nutriment
   des fibres peut être CELLULOSE, FIBRETOT, FIBRESOL, NDF ou ADF.
5. Vérifier le nombre total de scénarios puis lancer le calcul.

Le nombre de scénarios vaut :

```text
produit des tailles des six listes × nombre de poids × nombre de K × références
```

La limite initiale de 5 000 scénarios est modifiable. Si elle est dépassée, le
calcul est refusé avant l'allocation de la grille : il n'y a ni échantillonnage,
ni suppression de combinaisons. Les résultats affichés sont ceux du dernier
calcul, même si les contrôles sont ensuite modifiés. Il faut relancer le calcul
pour appliquer ces modifications.

Les mêmes variables supplémentaires (AW, wG, L, wL…) s'appliquent à tous les
profils. Aucune valeur physiologique n'est déduite du poids. Toutes les références
sélectionnées sont croisées avec tous les poids : le logiciel ne décide pas
qu'un référentiel adulte ou de croissance est approprié à un profil.

## Cibles et unités

- Le besoin total vaut **BEE standard × K**. K représente le produit global des
  coefficients, pas un coefficient supplémentaire appliqué à des K implicites.
- Les seuils exprimés par 1 000 kcal restent basés sur le BEE standard, comme dans
  `NutrientDisplayCalculations.calculerBesoinAbsolu`. Les seuils par kg vif,
  kg métabolique, 1 000 kJ ou absolus utilisent la conversion Kotlin existante.
- L'ajustement CELLULOSE applique par défaut le facteur **5** défini par
  `adjustmentNeedMultiplier` dans `View/AnalNut/MultiNutrientAdjustmentDialog.kt`.
  Il est visible et modifiable. Les autres facteurs valent 1. Changer le type de
  fibres remet le facteur à 1 (ou à 5 pour CELLULOSE).
- Une cible personnalisée est une entrée explicite de l'utilisateur ; aucune
  valeur nutritionnelle personnelle n'est ajoutée aux INIT.
- Les modifications des cibles servent à construire les rations. La conformité
  reste évaluée séparément contre **les seuils INIT originaux**.
- Un niveau absent n'est pas remplacé par un autre : le scénario est conservé
  avec `CIBLE_ABSENTE`. Le référentiel « Calcul sur RER », par exemple, ne possède
  pas de seuil CELLULOSE dans l'INIT actuel.

## Calcul des quantités

L'ordre est : protéines → fibres → calcium → oméga-6 → sodium → énergie restante.
Toutes les quantités commencent à zéro. À chaque étape :

```text
manque = cible absolue − apport des ingrédients déjà ajoutés
quantité ajoutée (g) = max(0, manque) / densité du nutriment par gramme
```

Cette opération reprend le principe manque/densité de
`ajusterAlimentsPourNutriment`. Les quantités sont continues, sans arrondi caché.
Les compositions, nutriments dérivés et énergies sont calculés par les fonctions
INIT déjà utilisées pour les rations manuelles. Les contributions croisées des
aliments précédents sont donc prises en compte.

L'ajustement énergétique complète les apports des cinq autres étapes. Si ces
apports dépassent déjà le besoin, l'ingrédient énergétique reste à zéro et le
scénario porte `ENERGIE_DEPASSEE`. On ne génère jamais une quantité négative.

Les aliments ajoutés plus tard peuvent augmenter un nutriment déjà ajusté.
Le moteur ne revient pas diminuer les quantités des étapes précédentes. Il s'agit
**d'une exploration séquentielle**, pas d'une résolution simultanée de six
égalités ni du port complet du solveur Kotlin par contraintes (ou de son
réajustement final Ca/P). La vérification finale de tous les seuils, notamment
Ca/P, permet de repérer ces écarts. Aucun ingrédient n'est ajouté en dehors des
six listes choisies.

Une composition manquante nécessaire au calcul d'un déficit bloque ce scénario
avec `COMPOSITION_ABSENTE` ; une densité nulle pour couvrir un manque donne
`INGREDIENT_INADAPTE`. Une absence n'est pas transformée en zéro. Les replis à
zéro explicitement prévus par Kotlin pour certaines équations restent signalés.

## Lire et exporter les résultats

- **Cibles** : niveau, valeur brute, unité, base et facteur utilisés.
- **Résultats** : tableau paginé, filtre par statut et carte poids/K. Le
  pourcentage représente les scénarios entièrement conformes parmi toutes les
  combinaisons et références demandées à ce poids/K ; les échecs restent dans
  le dénominateur.
- **Détail d'une ration** : scénario, quantités par rôle, cibles, apports finaux,
  écarts et comparaison à tous les seuils du référentiel.
- **Exports** : tous les scénarios, quantités et cibles en CSV ; configuration
  et provenance en JSON. Les exports ne sont pas limités à la page affichée.

`CONFORME` signifie que l'énergie correspond au besoin et que tous les seuils
évalués sont respectés, sans donnée absente ni avertissement de calcul. Les
statuts `SEUILS_NON_RESPECTES`, `DONNEES_INCOMPLETES` et les erreurs sont conservés,
avec compteurs et messages. Ce statut ne constitue pas une validation des
conditions physiologiques de choix du référentiel.

Identifiants conservés : scenario_id, combination_id, reference_id, food_id,
nutrient_id, source_json et, pour les seuils d'origine, source_pointer. Les
résultats utilisent une copie logique du catalogue du dernier calcul ; recharger
INIT n'altère pas rétrospectivement une grille calculée.

## Lancement et tests

```sh
cd vetnutriDogR/inst/quarto
quarto serve ration_interactive.qmd --browser
```

Pas d'installation de `vetnutriDogR` nécessaire ; les dépendances sont jsonlite,
shiny, knitr et rmarkdown. Après déplacement hors du dépôt, définir
`VETNUTRI_MP_ROOT`.

Depuis la racine du dépôt :

```sh
Rscript vetnutriDogR/tests/exploration.R
Rscript vetnutriDogR/tests/exploration-shiny.R
Rscript vetnutriDogR/tests/init-parity.R
```

Les tests couvrent les contributions croisées, l'énergie restante, l'absence de
quantités négatives, les intervalles, le produit cartésien exhaustif, les cibles
personnalisées et absentes, la limite de taille, la comparaison des résultats au
moteur de ration manuelle et le parcours réactif du QMD. Les tests historiques de
parité Kotlin/R contrôlent toujours les données, unités et conversions partagées.
