# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

### Added
- **Analyse Transversale (Cross-Consultation Analysis)**: Nouvelle fonctionnalité permettant de comparer et d'analyser les résultats nutritionnels sur plusieurs consultations pour un même animal.
- **Analyse Détaillée des Aliments**: Nouveaux écrans d'analyse (Sélection, Graphique, Détail) permettant une étude approfondie des apports nutritionnels des aliments.
- **Visualisations Graphiques**: Intégration de nouveaux types de graphiques (barres groupées et empilées) via KoalaPlot pour une meilleure lecture des analyses.
- **Curation Nutritionnelle**: Amélioration du rendu des nutriments avec des cartes dédiées (`cardNutrient`).
- Exam mode flow with exam ID and student identifier capture at startup.
- Exam metadata on animals, stored in DB and included in JSON/API exports.
- Exam export to jsonbin from the animal list with BinID display and copy.
- Quick import dialog on animal list (non-exam mode), with QR scanner on Android/iOS.
- Auto-open imported animal after quick import when a single animal is present (including update-only imports).
- Exam exercise ID on animals (stored in DB and included in JSON/API exports); required at creation in exam mode.
- Cross-analysis filters now support the exam ID + exercise ID pair.

### Changed
- Mise à jour de la structure de la base de données (version 29).
- Amélioration de la gestion des nutriments dans `NutrientUtils`.
- Optimisation de l'affichage de la liste et du détail des animaux pour intégrer les nouvelles analyses.
- “Import Rapide” is kept outside exam mode; “Export examen” shown only in exam mode.
- Restricted recipes and personalized advice while in exam mode.
- Quick import result counts now reflect animal count (not total imported items).

### Fixed
- N/A
