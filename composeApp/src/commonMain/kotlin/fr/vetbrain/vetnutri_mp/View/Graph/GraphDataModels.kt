package fr.vetbrain.vetnutri_mp.View.Graph

import fr.vetbrain.vetnutri_mp.Data.AlimentEv

/**
 * Types de graphiques disponibles
 */
enum class GraphType {
    SCATTER_PLOT, // Nuage de points
    LINE_PLOT,    // Graphique linéaire
    BAR_CHART     // Graphique en barres
}

/**
 * Configuration d'un axe de graphique
 */
data class AxisConfig(
    val label: String,           // Label de l'axe (ex: "% énergie protéines")
    val unit: String? = null,    // Unité (ex: "%", "mg/1000kcal", "g/1000kcal")
    val minValue: Float? = null, // Valeur minimale (null = auto)
    val maxValue: Float? = null  // Valeur maximale (null = auto)
)

/**
 * Point de données pour un graphique
 */
data class GraphPoint(
    val x: Float,                     // Valeur X
    val y: Float,                     // Valeur Y
    val alimentUuid: String,          // UUID de l'aliment pour l'identification
    val alimentNom: String,           // Nom de l'aliment pour l'affichage
    val numero: Int,                  // Numéro d'ordre dans la liste
    val isSelected: Boolean = false   // État de sélection
)

/**
 * Configuration complète d'un graphique
 */
data class GraphConfig(
    val title: String,                         // Titre du graphique
    val type: GraphType,                       // Type de graphique
    val xAxis: AxisConfig,                     // Configuration axe X
    val yAxis: AxisConfig,                     // Configuration axe Y
    val calculateX: suspend (AlimentEv) -> Float,    // Fonction de calcul X
    val calculateY: suspend (AlimentEv) -> Float,    // Fonction de calcul Y
    val showNumbers: Boolean = true,           // Afficher les numéros sur les points
    val allowSelection: Boolean = true         // Permettre la sélection
)

/**
 * Données complètes pour un graphique
 */
data class GraphData(
    val config: GraphConfig,                   // Configuration du graphique
    val points: List<GraphPoint>,              // Points de données
    val selectedAlimentUuid: String? = null    // UUID de l'aliment sélectionné
)

/**
 * Configuration d'un onglet de graphique
 */
data class GraphTab(
    val id: String,          // Identifiant unique
    val title: String,       // Titre de l'onglet
    val config: GraphConfig  // Configuration du graphique
)

/**
 * Gestionnaire d'onglets de graphiques
 */
data class GraphTabManager(
    val tabs: List<GraphTab>,                  // Liste des onglets
    val activeTabId: String,                   // Onglet actif
    val selectedAlimentUuid: String? = null    // Aliment sélectionné globalement
)
