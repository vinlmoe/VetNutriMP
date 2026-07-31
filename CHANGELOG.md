# Changelog

Toutes les modifications notables de ce projet seront documentées dans ce fichier.

## 3.3.05

### Changed
- Bump de version applicative vers **3.3.05 (305)** sur Android, iOS et Desktop.
- Mise à jour des données initiales intégrées vers la version **3.3.05**.

## À publier — changements depuis la 3.2.40

Cette section consolide les changements fonctionnels introduits depuis la version **3.2.40**
(commit `d57ce09` du 27 mars 2026) jusqu'à l'état courant du projet.

### Added
- **Migration complète depuis VetNutri 2** : import des animaux, consultations, rations,
  aliments, références nutritionnelles, équations et bibliographies, avec conversion des
  anciennes équations vers le moteur de calcul actuel et résolution des espèces, stades
  physiologiques, indications, races et unités historiques.
- **Énergie spécifique par espèce** pour les aliments, propagée dans la base de données,
  les calculs et les imports/exports JSON et Excel.
- **Nutriments personnalisés** : création, persistance, affichage, utilisation dans les
  équations et association à plusieurs références.
- **Édition en masse des références nutritionnelles**, avec filtres, sélection multiple et
  opérations groupées.
- **Gestion bibliographique enrichie** : import automatique par DOI, bibliographies liées
  aux aliments et module d'ajout d'une référence à plusieurs aliments.
- Lors de l'import CSV/Excel d'aliments, les références bibliographiques absentes de la
  bibliothèque locale sont désormais créées automatiquement avant d'être liées aux aliments.
- **Ajustement de ration sous contraintes** par programmation linéaire, afin de proposer
  automatiquement des quantités compatibles avec les objectifs nutritionnels sélectionnés.
- **Export PDF de l'analyse de ration**, comprenant la composition, les apports par
  ingrédient et des barres graphiques de comparaison aux repères nutritionnels.
- **Dialogue de composition détaillée des aliments**, accessible depuis les aliments d'une
  ration et depuis leur icône de consultation.
- **Dossiers d'examen** dans la liste des animaux, avec recherche par identifiant étudiant.
- **Import/export CSV des aliments sur iOS**, incluant la sélection et le partage de fichiers.
- Navigation au clavier améliorée dans les formulaires Compose (`Tab`, `Entrée`, `Échap`).
- Compatibilité Android avec les Chromebook et les appareils à grand écran.
- Infrastructure de tests Kotlin Multiplatform couvrant les modèles nutritionnels, les
  repositories, les migrations, les sauvegardes, la persistance et les protections d'import.

### Changed
- Les graphiques d'énergie et de nutriments utilisent désormais des règles de calcul
  cohérentes entre toutes les vues ; les références nutritionnelles constituent l'unique
  source des repères de calcul.
- Les calculs d'origine de l'énergie ont été harmonisés entre la composition des rations,
  les graphiques énergétiques et les analyses nutritionnelles.
- L'export PDF place la composition avant l'analyse, masque les nutriments sans données et
  colore les apports selon les ingrédients de la ration.
- L'import/export Excel des aliments prend en charge l'énergie par espèce et protège les
  retours chariot contenus dans les cellules.
- La vue d'édition d'une consultation a été réorganisée pour rendre les sections plus
  lisibles.
- L'internationalisation a été étendue aux principaux écrans d'administration, d'analyse,
  de références, d'animaux et de rations, avec compléments des traductions françaises,
  anglaises et chinoises.
- Les traitements de base de données et les repositories ont été optimisés : réduction des
  requêtes N+1, requêtes ciblées pour limiter la mémoire et amélioration des caches.
- L'architecture de l'application et de la navigation a été découpée en composants plus
  petits afin d'améliorer la maintenance et les performances.
- Les secrets d'accès aux services externes sont générés lors du build et ne sont plus
  stockés directement dans le code source.
- Le runtime du paquet Linux `.deb` est contrôlé afin de garantir sa compatibilité avec les
  processeurs ciblés.
- À chaque véritable relance de l'application mobile, l'écran de démarrage complet est de
  nouveau présenté pour permettre la mise à jour de la base ou l'activation du mode examen ;
  un simple passage en arrière-plan conserve l'écran courant.

### Fixed
- Correction de plusieurs incohérences dans les graphiques de ration : calcul des valeurs,
  position et libellé des rations, densité énergétique, répartition des nutriments et
  comparaison aux références.
- Les nutriments calculés ne peuvent plus devenir négatifs et la priorité de la valeur
  énergétique définie par équation est respectée.
- Correction de la persistance des nutriments personnalisés dans les aliments.
- Correction de l'édition des coefficients `K` des rations et amélioration de l'identification
  visuelle des ingrédients.
- Correction de la bibliographie absente dans le dialogue de composition d'un aliment.
- Correction des liens bibliographiques aliment ↔ référence lors des imports et exports.
- Protection des données de référence existantes lors d'une migration VetNutri 2 : les
  coefficients et les liens d'équations ne sont plus écrasés.
- Nombreuses corrections de migration VetNutri 2 concernant les quantités, unités, espèces,
  indications, races, rations orphelines, références de consultation et noms de colonnes des
  anciennes bases.
- Correction d'un risque de mémoire insuffisante lors du chargement de grandes bases
  d'aliments.
- Correction des crashs PDF liés au rendu SVG et remplacement des graphiques incompatibles
  avec iOS par un rendu HTML/CSS.
- Correction d'un crash iOS lors du formatage du nom d'un aliment et affichage du véritable
  logo sur l'écran de démarrage.
- Correction de l'encodage de certains onglets et ajout de traductions manquantes dans les
  vues d'analyse nutritionnelle.
- Correction des échanges avec JSONBin à la suite d'un audit de sécurité.

### Security
- Sauvegarde automatique avant migration, rotation des sauvegardes et contrôle d'intégrité
  SQLite afin de prévenir une perte ou un écrasement silencieux de la base.
- Protection anti-écrasement lors des imports : les données locales plus récentes sont
  conservées lorsque l'option correspondante est activée.
- Durcissement du partage JSON et retrait des clés de service du dépôt source.

### Historique Git analysé
- Point de départ réel : `d57ce09` — version 3.2.40 (27 mars 2026).
- Point d'arrivée : `5391d44` (30 juillet 2026).
- **173 commits** analysés, merges compris ; les commits de fusion, messages intermédiaires
  et doublons ont été regroupés par fonctionnalité.
- Le tag `v3.2.40` existant pointe sur un ancien commit (`a758da3`, 13 septembre 2025) et ne
  correspond pas au véritable commit de publication 3.2.40.

## 3.3.01

### Changed
- Bump de version applicative vers **3.3.01 (301)** sur Android, iOS et Desktop.

## 3.2.46

### Changed
- Harmonisation de l'affichage entre les vues **Apport** et **Composition** dans l'analyse de ration.
- Alignement des catégories et de leur ordre d'affichage entre les deux vues, avec prise en charge de la catégorie **Énergie**.
- Alignement de la logique de filtrage des nutriments: en mode filtré, les ratios d'analyse restent visibles même à 0.
- Uniformisation des libellés de catégories via les mêmes clés de traduction.

## 3.2.45

### Added
- Section Ordonnance enrichie avec des champs persistés par consultation : **Anamnèse**, **Examen clinique**, **Facteur nutritionnel clef**.
- Nouveau **Compte rendu (CR)** depuis la section Ordonnance :
  - bouton **Compte rendu** (prévisualisation HTML),
  - bouton **Copier le CR** (copie texte dans le presse-papiers).
- Contenu CR enrichi avec :
  - identification complète de l'animal (ID, nom, propriétaire, sexe, espèce, race, date de naissance, UUID),
  - date et objet de consultation,
  - poids de consultation,
  - coordonnées du vétérinaire,
  - rations actuelles/proposées détaillées (ingrédients + quantités),
  - conseils d'ordonnance détaillés.

### Changed
- Prévisualisation CR retravaillée avec une mise en forme structurée (sections/cartes/tableaux), inspirée de l'ordonnance.
- Export PDF du CR aligné sur la prévisualisation : export du **même HTML** (fidélité visuelle).
- Recherche de la liste des animaux étendue : prise en charge de la recherche par **ID animal**.
- Bloc "Animal" de l'ordonnance enrichi avec l'**ID animal**.
- Mémorisation du dernier dossier d'enregistrement desktop, avec persistance via la couche KMP de préférences (`PreferencesStorage`).
- Affichage des aliments harmonisé dans CR et ordonnance au format : **marque, gamme, nom aliment** (si les champs existent).

### Fixed
- Correction d'un mauvais routage d'export : l'export depuis la prévisualisation CR ne génère plus une analyse de ration.
- Correction d'appels `@Composable` hors contexte dans l'action de copie presse-papiers.
- Correction du rendu des champs de gamme absents : les valeurs `null` textuelles ne sont plus affichées dans le format des aliments.

## 3.2.40

### Changed
- Bump de version applicative vers **3.2.40 (240)** sur Android, iOS et Desktop.
- Alignement des métadonnées de version dans les ressources d'initialisation (`vetnutri_export_init.json`).

## 3.2.25

### Added
- **Analyse transversale (inter-consultations)** : nouvelle fonctionnalité permettant de comparer et d'analyser les résultats nutritionnels sur plusieurs consultations pour un même animal.
- **Analyse détaillée des aliments** : nouveaux écrans d'analyse (Sélection, Graphique, Détail) permettant une étude approfondie des apports nutritionnels des aliments.
- **Visualisations graphiques** : intégration de nouveaux types de graphiques (barres groupées et empilées) via KoalaPlot pour une meilleure lecture des analyses.
- **Curation nutritionnelle** : amélioration du rendu des nutriments avec des cartes dédiées (`cardNutrient`).
- Flux du mode examen avec capture de l'ID d'examen et de l'identifiant étudiant au démarrage.
- Métadonnées d'examen sur les animaux, stockées en base et incluses dans les exports JSON/API.
- Export examen vers jsonbin depuis la liste des animaux avec affichage et copie du BinID.
- Boîte de dialogue d'import rapide dans la liste des animaux (hors mode examen), avec scanner QR sur Android/iOS.
- Ouverture automatique de l'animal importé après un import rapide lorsqu'un seul animal est présent (y compris les imports « mise à jour uniquement »).
- ID d'exercice d'examen sur les animaux (stocké en base et inclus dans les exports JSON/API), requis à la création en mode examen.
- Les filtres d'analyse transversale prennent désormais en charge le couple ID d'examen + ID d'exercice.
- Ajout d'un module de notation d'examen (règles par exercice, notation par lot, corrections manuelles, stockage en base et export CSV).

### Changed
- Mise à jour de la structure de la base de données (version 29).
- Amélioration de la gestion des nutriments dans `NutrientUtils`.
- Optimisation de l'affichage de la liste et du détail des animaux pour intégrer les nouvelles analyses.
- « Import rapide » reste hors mode examen ; « Export examen » est affiché uniquement en mode examen.
- Recettes et conseils personnalisés restreints en mode examen.
- Les résultats d'import rapide affichent désormais le nombre d'animaux (et non le total des éléments importés).
- Import/Export Excel aliments : ajout de la colonne **Date dernière mise à jour** (prise en compte par l'option "importer seulement si plus récent").
- Import Excel aliments : si la date de dernière mise à jour est absente, elle est définie à la date du jour.
- Export Excel aliments : si la date de dernière mise à jour est absente, elle est définie à la date du jour.

### Fixed
- Le mode d'affichage "bullet" de l'analyse nutritionnelle ne revient plus en mode cartes après la mise à jour d'une quantité.
- Le clic sur une ration sélectionne désormais correctement la ration au premier clic même après édition (perte de focus forcée).
