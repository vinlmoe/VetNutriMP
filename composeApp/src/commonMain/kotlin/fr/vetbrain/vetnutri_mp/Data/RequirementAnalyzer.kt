package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam

/**
 * Classe pour analyser les besoins nutritionnels Basée sur la classe RequirementAnalyzer du projet
 * Java original
 */
class RequirementAnalyzer {
    private val mapRef = mutableMapOf<String, ListNutrientRef>()
    private var bee: Double = 0.0
    private var bw: Double = 0.0
    private var mw: Double = 0.0

    /**
     * Initialise l'analyseur avec les données nécessaires
     *
     * @param references Liste des références disponibles
     * @param bee Le besoin énergétique de base
     * @param bw Le poids de l'animal
     * @param mw Le poids métabolique
     * @param svp Liste des variables supplémentaires
     * @param ration La ration à analyser
     */
    fun initialiser(
            references: List<ReferenceEv>,
            bee: Double,
            bw: Double,
            mw: Double,
            svp: List<SupplementalvariableP>,
            ration: Ration
    ) {
        this.bee = bee
        this.bw = bw
        this.mw = mw

        // Parcourir tous les types de nutriments
        for (mainNutrient in MainNutrientEnum.entries) {
            when (mainNutrient) {
                MainNutrientEnum.AMA -> {
                    for (nutrient in AAEnum.entries) {
                        definirReference(nutrient, references, bee, bw, mw, svp, ration)
                    }
                }
                MainNutrientEnum.ANA -> {
                    for (nutrient in NutrientAnalysis.entries) {
                        definirReference(nutrient, references, bee, bw, mw, svp, ration)
                    }
                }
                MainNutrientEnum.BASE -> {
                    for (nutrient in NutrientMain.entries) {
                        definirReference(nutrient, references, bee, bw, mw, svp, ration)
                    }
                }
                MainNutrientEnum.LIPID -> {
                    for (nutrient in NutrientLipid.entries) {
                        definirReference(nutrient, references, bee, bw, mw, svp, ration)
                    }
                }
                MainNutrientEnum.MACRO -> {
                    for (nutrient in NutrientMacro.entries) {
                        definirReference(nutrient, references, bee, bw, mw, svp, ration)
                    }
                }
                MainNutrientEnum.MIN -> {
                    for (nutrient in NutrientMin.entries) {
                        definirReference(nutrient, references, bee, bw, mw, svp, ration)
                    }
                }
                MainNutrientEnum.VITAM -> {
                    for (nutrient in NutrientVitam.entries) {
                        definirReference(nutrient, references, bee, bw, mw, svp, ration)
                    }
                }
                MainNutrientEnum.ENERGIE,
                MainNutrientEnum.NO,
                MainNutrientEnum.INGREDIENT,
                MainNutrientEnum.INDICAT,
                MainNutrientEnum.OTHER -> {
                    // Pas de traitement spécifique pour ces types
                }
            }
        }
    }

    /**
     * Définit une référence pour un nutriment donné
     *
     * @param nutrient Le nutriment pour lequel définir la référence
     * @param references Liste des références disponibles
     * @param bee Le besoin énergétique de base
     * @param bw Le poids de l'animal
     * @param mw Le poids métabolique
     * @param svp Liste des variables supplémentaires
     * @param ration La ration à analyser
     */
    private fun definirReference(
            nutrient: Nutrient,
            references: List<ReferenceEv>,
            bee: Double,
            bw: Double,
            mw: Double,
            svp: List<SupplementalvariableP>,
            ration: Ration
    ) {
        // Trouver la référence correspondant au nutriment
        val refEv =
                references.find { ref ->
                    // Vérification simplifiée, adapter selon la structure réelle
                    ref.toString().contains(nutrient.toString())
                }

        if (refEv != null) {
            // Créer une liste de références pour ce nutriment
            val listRef = ListNutrientRef()
            // Ajouter les références spécifiques si disponibles
            // Cette partie doit être adaptée en fonction de la structure réelle
            mapRef[nutrient.toString()] = listRef
        }
    }

    /**
     * Obtient les références pour un nutriment donné
     *
     * @param mne Le code du type principal de nutriment
     * @param kind Le code du type spécifique de nutriment
     * @return La liste des références pour ce nutriment
     */
    fun obtenirReferences(mne: Int, kind: Int): List<NutrientRef> {
        val mainNutrient = MainNutrientEnum.getByCoef(mne)
        val nutrient = mainNutrient?.getNutrient(kind)

        return if (nutrient != null && mapRef.containsKey(nutrient.toString())) {
            mapRef[nutrient.toString()]?.getReferences() ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * Obtient les références pour un nutriment donné
     *
     * @param nutrient Le nutriment pour lequel obtenir les références
     * @return La liste des références pour ce nutriment
     */
    fun obtenirReferences(nutrient: Nutrient): List<NutrientRef> {
        return obtenirReferences(nutrient.toString())
    }

    /**
     * Obtient les références pour un nutriment donné par son nom
     *
     * @param nutrientName Le nom du nutriment pour lequel obtenir les références
     * @return La liste des références pour ce nutriment
     */
    fun obtenirReferences(nutrientName: String): List<NutrientRef> {
        return if (mapRef.containsKey(nutrientName)) {
            mapRef[nutrientName]?.getReferences() ?: emptyList()
        } else {
            emptyList()
        }
    }

    /**
     * Obtient le besoin énergétique de base
     *
     * @return Le besoin énergétique de base
     */
    fun getBEE(): Double {
        return bee
    }

    /**
     * Obtient le poids de l'animal
     *
     * @return Le poids de l'animal
     */
    fun getBW(): Double {
        return bw
    }

    /**
     * Obtient le poids métabolique
     *
     * @return Le poids métabolique
     */
    fun getMW(): Double {
        return mw
    }

    /**
     * Obtient la map des références
     *
     * @return La map des références
     */
    fun getMapRef(): Map<String, ListNutrientRef> {
        return mapRef
    }
}
