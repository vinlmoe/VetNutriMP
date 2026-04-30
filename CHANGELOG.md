# Changelog

Toutes les modifications notables de ce projet seront documentées dans ce fichier.

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

### Fixed
- Correction d'un mauvais routage d'export : l'export depuis la prévisualisation CR ne génère plus une analyse de ration.
- Correction d'appels `@Composable` hors contexte dans l'action de copie presse-papiers.

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
