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

### Interface

- Le panneau de gauche est découpé en onglets **Chiens**, **Ingrédients**,
  **Options** et **Sélection** (mémoriser, restaurer, importer ou exporter les
  référentiels et listes ; chemin du catalogue INIT).
- Le bloc en haut du panneau reste visible : nombre de scénarios, problèmes
  bloquants (référentiel absent, liste vide, intervalle ou JSON invalide, limite
  dépassée) et bouton de calcul. Chaque liste d'ingrédients affiche son effectif
  dans son titre, en rouge si elle est vide.
- Après un calcul, l'onglet **Résultats** s'ouvre. Un bandeau signale toute
  modification des paramètres depuis le dernier calcul. Une erreur de calcul est
  affichée en notification au lieu d'interrompre l'application.
- Les scénarios sont paginés par 50 (« page x sur N ») et le filtre de statut
  revient à la page 1. L'onglet **Détail d'une ration** permet de passer au
  scénario précédent ou suivant.

### Éditer une ration depuis la carte

Un clic sur une case de la carte poids × K ouvre l'éditeur de ration pour ce
référentiel, ce poids et ce K :

- le point de départ est la meilleure combinaison de la case (conforme d'abord,
  puis le moins de seuils renseignés non respectés, puis le plus petit écart
  énergétique) ; les autres combinaisons de la case restent sélectionnables ;
- chaque quantité est modifiable (0 g retire l'ingrédient) et un aliment
  quelconque du catalogue peut être ajouté ;
- le bilan est recalculé à chaque modification avec la configuration du calcul
  d'origine (valeurs absentes, OPTIMAX ignorés, nutriments évalués). L'énergie
  est jugée à ± 2 % du besoin (`ENERGIE_DEPASSEE` ou `ENERGIE_INSUFFISANTE`),
  sans tolérance d'arrondi ;
- « Enregistrer » ajoute la ration à l'onglet **Rations éditées** (un nouvel
  enregistrement depuis le même éditeur la met à jour). Une ration peut y être
  rouverte, supprimée et exportée : bilans, quantités et comparaison aux seuils
  en CSV, ou toutes les rations avec leur configuration en JSON.

Les fonctions `vn_evaluate_edited_ration()`, `vn_edited_rations_tables()` et
`vn_edited_rations_json()` réalisent ces calculs hors de l'interface.

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
`ajusterAlimentsPourNutriment`.

### Arrondi et dose minimale

Par défaut (case « Arrondir les quantités comme VetNutri MP »), chaque quantité est
arrondie après son étape, comme `arrondirQuantiteSelonRegles` ; les étapes suivantes
utilisent la quantité arrondie :

| Aliment | Pas |
|---|---|
| Avec contenant (`presentation` ≠ NO et `presentationQuantity` > 0 : dosette, sachet, boîte…) | ½ contenant (dosette de 4 g : 2 g) |
| Sinon, moins de 20 g | 1 g |
| Sinon, de 20 à 200 g | 5 g |
| Sinon, 200 g et plus | 25 g |

**Dose minimale**, réglable **pour chaque type d'ingrédient** (protéines, fibres,
calcium, oméga-6, sodium, énergie ; 5 g par défaut, champ « Dose minimale si
utilisé » dans chaque liste) : un ingrédient utilisé pèse au moins la dose de son
type ; pour un contenant, c'est le premier multiple du pas qui l'atteint
(dosette de 4 g : 6 g). Une quantité calculée inférieure est ramenée à 0 sous la
moitié du minimum, portée au minimum au-delà.

L'énergie ne tombe plus exactement sur le besoin : elle est acceptée à ± ½ pas de
l'ingrédient énergétique (½ dose minimale s'il est au minimum ou à 0). La tolérance
et l'écart sont exportés (`energy_tolerance_kcal`, `energy_gap_kcal`). Décocher la
case revient aux grammes continus (dose minimale ignorée).

Conséquence : la dose minimale et les pas fixes en grammes rendent la carte
**dépendante du poids**. Chez un petit chien, 5 g de CMV ou d'huile représentent
plus que la cible ; ce surplus peut corriger ou dégrader un seuil (Ca/P notamment).
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
`INGREDIENT_INADAPTE`. Par défaut, une absence n'est pas transformée en zéro. Les
replis à zéro explicitement prévus par Kotlin pour certaines équations restent signalés.

### Option « Valeur absente = 0 (comme Kotlin) »

Beaucoup d'aliments INIT n'ont pas toutes les valeurs (O6 n'est renseigné que pour
environ 56 % des aliments ; les compléments minéraux sont souvent très incomplets).
En mode strict, ces aliments bloquent l'ajustement ou rendent des seuils non
évaluables, si bien que `CONFORME` est rarement atteignable.

La case à cocher reproduit le comportement de VetNutri MP
(`valMap[n]?.value ?: 0.0` dans `ajusterAlimentsPourNutriment`) : une valeur
absente compte pour 0, pour l'ajustement comme pour la comparaison aux seuils.

- Elle est décochée par défaut et enregistrée dans la configuration exportée
  (`missing_as_zero`).
- Un aliment sans valeur pour le nutriment de son rôle ne peut pas combler un
  manque : `INGREDIENT_INADAPTE`, jamais une quantité arbitraire.
- `zero_filled` compte les nutriments additifs, évalués par un seuil, dont le
  total a utilisé au moins un zéro de substitution ; `zero_filled_nutrients` les
  liste. Le détail d'une ration marque ces seuils (`zero_filled`).
- Un scénario qui serait conforme mais a utilisé des zéros reçoit
  `CONFORME_ABSENTS_A_ZERO`, jamais `CONFORME`. La carte poids/K compte alors
  les deux statuts et l'indique en sous-titre.
- Les ratios (Ca/P…) restent recalculés sur les totaux ; un ratio non calculable
  (division par zéro) reste `DONNEES_ABSENTES`.

## Bornes hautes : MAX seulement

Par défaut, l'explorateur **ignore les OPTIMAX** : seuls les MAX limitent les
apports ; les MIN et OPTIMIN restent évalués. La case « Ignorer les OPTIMAX » permet
de les réintégrer ; le choix est enregistré (`ignore_levels`) dans la configuration
exportée et appliqué au détail d'une ration. L'application Shiny de ration manuelle
évalue toujours tous les niveaux.

## Ajustement terminal Ca/P

L'option **Ajuster Ca/P en dernier avec l'ingrédient calcium** effectue, après
l'ajustement énergétique, des incréments de l'aliment choisi pour le rôle calcium
jusqu'à atteindre le seuil inférieur Ca/P actif (`MIN` ou `OPTIMIN`). Les autres
doses ne sont pas recalculées : un dépassement énergétique éventuel reste donc
visible. La quantité ajoutée est conservée dans `cap_adjustment_g`, les messages
du scénario et la configuration exportée.

## Nutriments évalués

Par défaut, tous les nutriments ayant un seuil dans le référentiel sont évalués.
Décocher « Évaluer tous les nutriments du référentiel » affiche la liste préremplie :
retirer un nutriment avec sa croix, ou utiliser « Tout cocher » / « Tout décocher ».
Seuls les seuils des nutriments retenus entrent dans la conformité, la carte et la
liste des nutriments non couverts ; les six ajustements de quantité ne changent pas.
Sans nutriment retenu, aucun seuil n'est évalué. Le choix est enregistré
(`nutrients`) et appliqué au détail d'une ration.

## Nutriments non couverts

L'onglet **Nutriments non couverts** liste chaque nutriment et niveau de seuil
manqué par au moins un scénario évalué : sens (insuffisant pour MIN/OPTIMIN, excès
pour MAX/OPTIMAX), nombre et part des scénarios en échec, échecs dus uniquement à
des valeurs absentes comptées à 0, et cases poids × K **jamais couvertes** (toutes
les combinaisons y échouent sur ce seuil). Chaque scénario porte aussi
`not_covered` (nutriments insuffisants) et `in_excess` (nutriments en excès).
Export CSV « Nutriments non couverts ».

## Quantités de chaque ration

Chaque scénario donne la quantité de chaque ingrédient : colonne `composition`
(« Poulet… : 250 g + Son de blé : 15 g + … ») dans le tableau des résultats, et
colonnes `quantity_<rôle>_g` dans l'export des scénarios. L'export des quantités
garde une ligne par ingrédient.

## Carte poids × K : zones non équilibrables

L'onglet **Résultats** affiche une carte par référentiel : le poids en abscisse,
K en ordonnée, une case par couple. Une case regroupe toutes les combinaisons
d'ingrédients testées pour ce couple.

| Zone | Condition | Libellé de la case |
|---|---|---|
| Équilibrable (vert) | au moins une combinaison respecte tous les seuils évalués | n/N combinaisons conformes |
| Sous réserve (jaune, hachures) | aucune conforme, mais au moins une dont seuls des nutriments non renseignés (comptés à 0) échouent | n/N combinaisons |
| Seuils renseignés non respectés (rouge, du clair au foncé) | toutes les combinaisons évaluables manquent au moins un seuil renseigné | nombre minimal de seuils manqués |
| Énergie déjà dépassée (orange, hachures) | les cinq premiers ajustements dépassent le besoin | nombre minimal de seuils manqués |
| Non évaluable (gris, hachures) | cible ou composition absente | – |

Sans combinaison conforme ou sous réserve, la zone est nommée par l'échec le plus
fréquent. Le tableau sous la carte liste, pour chaque case non équilibrable, les
seuils renseignés les plus souvent manqués et les échecs dus à des données
absentes. L'export « Carte poids × K » reprend toutes les colonnes.

Points de lecture :

- Quand tous les seuils d'un référentiel sont exprimés par 1 000 kcal de BEE ou
  par kg métabolique, les quantités sont proportionnelles au BEE et **le poids ne
  change pas le résultat** : les zones forment des bandes horizontales selon K.
  Seuls les seuils par kg vif ou absolus font varier la carte avec le poids.
- **Effet de K.** Comme dans VetNutri MP (`calculerBesoinAbsolu` reçoit le BEE
  standard), tous les seuils par 1 000 kcal, MIN comme MAX, restent calculés sur
  le BEE standard, alors que la ration apporte BEE × K. Les ingrédients ajustés
  (protéine, fibres, calcium, huile, sel) ont donc la même quantité quel que soit
  K ; seul l'ingrédient énergétique varie. K élevé : plus d'apports pour des
  maximums fixes, donc des dépassements d'OPTIMAX/MAX (fer, iode, phosphore…) et,
  en ration ménagère, un Ca/P qui baisse avec le féculent. K faible : la densité
  nutritionnelle requise augmente ; un aliment complet seul dépasse alors
  l'énergie pour couvrir ses minimums (zone orange).
- L'ajustement est séquentiel : une case rouge signifie qu'aucune combinaison
  testée ne convient avec cette méthode, pas qu'aucune ration n'existe.
- Le calcium est ajusté sur son seul seuil ; le réajustement Ca/P de Kotlin n'est
  pas porté. Si **CAP** domine les seuils limitants, augmenter le facteur de la
  cible calcium (par exemple 1,5) puis relancer.
- Les acides aminés sont absents de nombreux aliments (Ciqual) : avec l'option
  « valeur absente = 0 », ils apparaissent comme « échecs sur données absentes »
  et la case passe en « sous réserve », pas en rouge.

## Lire et exporter les résultats

- **Cibles** : niveau, valeur brute, unité, base et facteur utilisés.
- **Résultats** : carte poids × K des zones équilibrables (voir ci-dessus),
  tableau des zones, tableau paginé des scénarios et filtre par statut.
- **Détail d'une ration** : scénario, quantités par rôle, cibles, apports finaux,
  écarts et comparaison à tous les seuils du référentiel. Les seuils non
  conformes affichent la valeur attendue, la valeur observée et l'écart ; les
  ratios tels que Ca/P sont recalculés sur les totaux de la ration.
- **Courbes de doses** : pour une combinaison et un référentiel, une courbe de
  chaque ingrédient selon K à poids fixé, et selon le poids à K fixé.
- **Apports nutritionnels et normes** : jusqu'à quatre nutriments au choix,
  dont les ratios comme Ca/P, sont affichés avec leurs apports et leurs seuils
  MIN, OPTIMIN, OPTIMAX ou MAX selon K à poids fixé et selon le poids à K fixé.
- **Exports** : tous les scénarios, quantités, cibles et zones de la carte en CSV ; configuration
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
personnalisées et absentes, la limite de taille, l'option valeur absente = 0
(blocage par défaut, calcul et traçabilité avec l'option, cohérence avec la ration
manuelle), la comparaison des résultats au
moteur de ration manuelle et le parcours réactif du QMD. Les tests historiques de
parité Kotlin/R contrôlent toujours les données, unités et conversions partagées.
