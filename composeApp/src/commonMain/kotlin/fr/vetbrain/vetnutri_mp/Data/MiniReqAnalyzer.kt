package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import kotlinx.serialization.Serializable

/**
 * Version simplifiée de l'analyseur de besoins Basée sur la classe MiniReqAnalyszer du projet Java
 * original
 */
@Serializable
class MiniReqAnalyzer {

    private val mapRef: MutableMap<String, ListNutrientRef> = mutableMapOf()
    private var bee: Float = 0f
    private var bw: Float = 0f
    private var mw: Float = 0f

    /**
     * Constructeur qui initialise l'analyseur à partir d'un RequirementAnalyzer complet
     *
     * @param analyzer L'analyseur complet à partir duquel initialiser
     */
    constructor(analyzer: RequirementAnalyzer) {
        // Initialisation du mapRef à partir de l'analyzer
        analyzer.getMapRef().forEach { (key, value) ->
            mapRef[key] = ListNutrientRef(value.references)
        }

        // Copier les valeurs de base
        this.bee = analyzer.getBEE()
        this.bw = analyzer.getBW()
        this.mw = analyzer.getMW()
    }

    /**
     * Obtient les références pour un nutriment donné
     *
     * @param nutrient Le nutriment pour lequel obtenir les références
     * @return La liste des références pour ce nutriment
     */
    fun obtenirReferences(nutrient: Nutrient?): List<NutrientRef> {
        // Obtenir les références pour un nutriment spécifique
        return if (nutrient != null && mapRef.containsKey(nutrient.toString())) {
            mapRef[nutrient.toString()]?.getReferences() ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * Récupère la map des références
     *
     * @return La map des références
     */
    fun getMapRef(): Map<String, ListNutrientRef> = mapRef
}
