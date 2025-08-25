# Analyse Graphique des Aliments Sélectionnés

## Vue d'ensemble

La vue `AnalyseGraphiqueAlimentsView` permet d'analyser graphiquement les caractéristiques nutritionnelles d'une liste d'aliments sélectionnés. Elle offre une visualisation interactive avec des graphiques en nuage de points et une liste détaillée des aliments.

## Fonctionnalités

### 1. Interface Responsive
- **Écran large** : Affichage côte à côte (liste à gauche, graphiques à droite)
- **Écran compact** : Affichage vertical (graphiques puis liste)

### 2. Liste des Aliments
- Numérotation automatique (1, 2, 3...)
- Tri par densité énergétique décroissante
- Affichage : Numéro, Nom, Gamme, Marque
- Largeur : 1/4 de l'écran sur grand écran

### 3. Graphiques KoalaPlot
- **Nuage de points** : % énergie protéines vs % énergie lipides
- Chaque point représente un aliment avec son numéro
- Grille de référence et axes gradués
- Légende explicative

### 4. Calculs Nutritionnels
- **Densité énergétique** : Calculée selon les équations de référence ou méthode simple
- **% Énergie protéines** : (Protéines × 3.5 kcal/g) / Densité totale × 100
- **% Énergie lipides** : (Lipides × 8.5 kcal/g) / Densité totale × 100

## Utilisation

### Depuis AnalyseSelectionAlimentsView
```kotlin
AnalyseSelectionAlimentsView(
    aliments = listeAliments,
    referenceEv = reference,
    equationRepository = equationRepo,
    onClose = { /* fermer */ },
    onAlimentSelected = { aliment -> /* action */ }
)
```

### Bouton d'Analyse Graphique
- Apparaît uniquement si des aliments sont sélectionnés
- Couleur secondaire pour le distinguer
- Icône d'analytics pour l'identifier

### Navigation
- **Bouton retour** : Retour à la vue de sélection
- **Vue plein écran** : Optimisée pour l'analyse

## Structure des Données

### AlimentAnalyseData
```kotlin
data class AlimentAnalyseData(
    val aliment: AlimentEv,           // Aliment original
    val numero: Int,                  // Numéro d'ordre (1, 2, 3...)
    val densiteEnergetique: Double,   // kcal/g
    val pourcentageProteines: Double, // % de l'énergie totale
    val pourcentageLipides: Double    // % de l'énergie totale
)
```

## Calculs Techniques

### Densité Énergétique
1. **Méthode équations** : Utilise `ReferenceEv` et `EquationRepository` si disponibles
2. **Méthode simple** : Protéines × 3.5 + Lipides × 8.5 + Glucides × 3.5 kcal/g

### Pourcentages Énergétiques
- **Protéines** : `(proteines * 3.5 / densiteTotale) * 100`
- **Lipides** : `(lipides * 8.5 / densiteTotale) * 100`

## Responsive Design

### Breakpoint : 800dp
- **≥ 800dp** : Layout horizontal (25% liste + 75% graphiques)
- **< 800dp** : Layout vertical (graphiques puis liste)

### Adaptations
- **Grand écran** : Optimisé pour l'analyse comparative
- **Petit écran** : Optimisé pour la lisibilité mobile

## Intégration

### Dépendances
- `KoalaPlot` : Graphiques interactifs
- `Compose Material` : Interface utilisateur
- `Data classes` : Modèles de données

### Compatibilité
- Fonctionne avec ou sans `ReferenceEv`
- Gestion d'erreurs robuste
- Fallback sur méthodes de calcul simples

## Tests

### Couverture
- Affichage avec données
- Affichage liste vide
- Navigation (bouton retour)
- Calculs nutritionnels

### Validation
- Compilation Kotlin ✓
- Tests unitaires ✓
- Interface responsive ✓

## Évolutions Futures

### Graphiques Additionnels
- **Radar chart** : Profil nutritionnel complet
- **Histogramme** : Distribution des nutriments
- **Scatter 3D** : 3 variables nutritionnelles

### Interactions
- **Zoom** : Focus sur zones d'intérêt
- **Filtres** : Sélection par gamme/marque
- **Export** : Sauvegarde des graphiques

### Analyses
- **Clustering** : Groupes d'aliments similaires
- **Corrélations** : Relations entre nutriments
- **Recommandations** : Suggestions d'optimisation
