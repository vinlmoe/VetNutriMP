package fr.vetbrain.vetnutri_mp.Utils

/**
 * Guide pratique pour contrôler les décimales dans VetNutriMP
 * 
 * Ce guide montre comment utiliser les différentes méthodes disponibles
 * pour contrôler le formatage des décimales dans votre application.
 */
object DecimalControlGuide {

    /**
     * 🎯 MÉTHODE 1: Formatage intelligent automatique
     * 
     * Utilisez GraphFormattingUtils.formatSmartDecimal() pour un formatage adaptatif
     * Le nombre de décimales s'ajuste automatiquement selon la valeur
     */
    fun methodeFormatageIntelligent() {
        // ✅ Recommandé pour la plupart des cas
        val valeur = 12.3456789
        val formatage = GraphFormattingUtils.formatSmartDecimal(valeur)
        // Résultat: "12.3" (1 décimale pour les valeurs entre 10 et 100)
    }

    /**
     * 🎯 MÉTHODE 2: Formatage spécifique par type de données
     * 
     * Utilisez les fonctions spécialisées selon le contexte
     */
    fun methodeFormatageSpecifique() {
        val valeur = 12.3456789
        
        // Pour les pourcentages (toujours 1 décimale)
        val pourcentage = GraphFormattingUtils.formatPercentage(valeur)
        // Résultat: "12.3%"
        
        // Pour les énergies (adaptatif)
        val energie = GraphFormattingUtils.formatEnergy(valeur)
        // Résultat: "12.3 kcal"
        
        // Pour les densités (adaptatif)
        val densite = GraphFormattingUtils.formatSmartDecimal(valeur)
        // Résultat: "12.3" (à utiliser avec "kcal/100g")
    }

    /**
     * 🎯 MÉTHODE 3: Contrôle manuel du nombre de décimales
     * 
     * Utilisez GraphFormattingUtils.formatDecimal() avec un nombre spécifique
     */
    fun methodeControleManuel() {
        val valeur = 12.3456789
        
        // Forcer un nombre spécifique de décimales
        val uneDecimale = GraphFormattingUtils.formatDecimal(valeur, 1)    // "12.3"
        val deuxDecimales = GraphFormattingUtils.formatDecimal(valeur, 2)  // "12.35"
        val troisDecimales = GraphFormattingUtils.formatDecimal(valeur, 3) // "12.346"
    }

    /**
     * 🎯 MÉTHODE 4: Contrôle des ticks d'axes avec KoalaPlotExtensions
     * 
     * Utilisez les modèles d'axes optimisés pour contrôler les ticks
     */
    fun methodeControleTicks() {
        // Dans vos graphiques XY, utilisez:
        // xAxisModel = KoalaPlotExtensions.createSmartPercentageAxisModel(range)
        // yAxisModel = KoalaPlotExtensions.createSmartDensityAxisModel(range)
        
        // Ces modèles optimisent automatiquement les incréments de ticks
        // pour réduire le nombre de décimales affichées sur les axes
    }

    /**
     * 🎯 MÉTHODE 5: Formatage dans les bullet graphs
     * 
     * Les bullet graphs supportent le formatage personnalisé des étiquettes
     */
    fun methodeBulletGraphs() {
        // Dans un bullet graph, vous pouvez utiliser:
        // axis { labels { GraphFormattingUtils.formatSmartDecimal(it.toDouble()) } }
        
        // Exemple d'utilisation dans DetailNutrimentAnalysis.kt:
        // axis { labels { GraphFormattingUtils.formatSmartDecimal(it.toDouble()) } }
    }

    /**
     * 🎯 MÉTHODE 6: Formatage dans les légendes et informations contextuelles
     * 
     * C'est là où vous verrez le plus d'impact du formatage intelligent
     */
    fun methodeLegendes() {
        // Dans les légendes de graphiques:
        val pointX = 25.6789
        val pointY = 45.1234
        
        val legendeX = "X: ${GraphFormattingUtils.formatSmartDecimal(pointX)}" // "X: 25.7"
        val legendeY = "Y: ${GraphFormattingUtils.formatSmartDecimal(pointY)}" // "Y: 45.1"
        
        // Dans les cartes de nutriments:
        val valeurNutriment = 0.123456
        val valeurFormatee = GraphFormattingUtils.formatSmartDecimal(valeurNutriment) // "0.123"
    }

    /**
     * 📋 RÉSUMÉ DES RECOMMANDATIONS
     */
    fun recommandations() {
        /*
        ✅ UTILISEZ:
        - GraphFormattingUtils.formatSmartDecimal() pour le formatage général
        - GraphFormattingUtils.formatPercentage() pour les pourcentages
        - GraphFormattingUtils.formatEnergy() pour les énergies
        - GraphFormattingUtils.formatSmartDecimal() pour les densités
        - KoalaPlotExtensions.createSmart*AxisModel() pour les axes de graphiques
        
        ❌ ÉVITEZ:
        - Les paramètres xAxisLabels et yAxisLabels dans XYGraph (causent des erreurs)
        - Le formatage manuel avec String.format()
        - Les valeurs avec trop de décimales dans les légendes
        
        🎯 RÉSULTAT:
        - Formatage intelligent et adaptatif
        - Meilleure lisibilité des graphiques
        - Cohérence dans toute l'application
        */
    }
}
