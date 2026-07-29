package fr.vetbrain.vetnutri_mp.Utils

/**
 * Exemples d'utilisation du formatage intelligent des décimales
 * 
 * Ce fichier montre comment utiliser GraphFormattingUtils pour contrôler
 * le nombre de décimales affichées dans différents contextes.
 */
object DecimalControlExamples {

    /**
     * Exemple 1: Formatage automatique intelligent
     * Le nombre de décimales s'adapte automatiquement selon la valeur
     */
    fun exempleFormatageAutomatique() {
        val valeurs = listOf(0.001, 0.05, 0.5, 5.0, 50.0, 500.0)
        
        valeurs.forEach { valeur ->
            val formatage = GraphFormattingUtils.formatSmartDecimal(valeur)
        }
        // Résultat:
        // 0.001 -> 0.0010
        // 0.05 -> 0.050
        // 0.5 -> 0.50
        // 5.0 -> 5.0
        // 50.0 -> 50
        // 500.0 -> 500
    }

    /**
     * Exemple 2: Formatage spécifique par type de données
     */
    fun exempleFormatageSpecifique() {
        val pourcentage = 12.3456
        val energie = 150.789
        val densite = 3.4567
        
        // Pour les pourcentages (toujours 1 décimale)
        val pourcentageFormate = GraphFormattingUtils.formatPercentage(pourcentage)
        
        // Pour les énergies (adaptatif)
        val energieFormatee = GraphFormattingUtils.formatEnergy(energie)
        
        // Pour les densités (adaptatif)
        val densiteFormatee = GraphFormattingUtils.formatSmartDecimal(densite)
    }

    /**
     * Exemple 3: Contrôle manuel du nombre de décimales
     */
    fun exempleControleManuel() {
        val valeur = 12.3456789
        
        // Forcer un nombre spécifique de décimales
        val uneDecimale = GraphFormattingUtils.formatDecimal(valeur, 1)
        val deuxDecimales = GraphFormattingUtils.formatDecimal(valeur, 2)
        val troisDecimales = GraphFormattingUtils.formatDecimal(valeur, 3)
        
    }

    /**
     * Exemple 4: Formatage pour les bullet graphs
     * Les bullet graphs supportent le formatage personnalisé des étiquettes
     */
    fun exempleBulletGraph() {
        // Dans un bullet graph, vous pouvez utiliser:
        // axis { labels { GraphFormattingUtils.formatSmartDecimal(it.toDouble()) } }
        
        val valeurBullet = 85.6789
        val etiquetteFormatee = GraphFormattingUtils.formatSmartDecimal(valeurBullet)
    }

    /**
     * Exemple 5: Formatage dans les légendes de graphiques
     */
    fun exempleLegendeGraphique() {
        val pointX = 25.6789
        val pointY = 45.1234
        
        // Dans une légende de graphique XY
        val legendeX = "X: ${GraphFormattingUtils.formatSmartDecimal(pointX)}"
        val legendeY = "Y: ${GraphFormattingUtils.formatSmartDecimal(pointY)}"
        
    }

    /**
     * Exemple 6: Formatage conditionnel selon le contexte
     */
    fun exempleFormatageConditionnel() {
        val valeur = 12.3456
        
        // Formatage différent selon le type de graphique
        val pourGraphiquePourcentage = GraphFormattingUtils.formatPercentage(valeur)
        val pourGraphiqueDensite = GraphFormattingUtils.formatSmartDecimal(valeur)
        val pourGraphiqueGeneral = GraphFormattingUtils.formatSmartDecimal(valeur)
        
    }
}
