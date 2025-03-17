package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.AAEnum
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientOther
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import kotlinx.serialization.Serializable

/**
 * Classe représentant tous les types de nutriments Basée sur la classe AllNutrient du projet Java
 * original
 */
@Serializable
class AllNutrient {

    var label: String = ""
        private set
    var unit: String = ""
        private set
    var mne: Int = 0
        private set
    var kindnut: Int = 0
        private set

    /**
     * Constructeur privé avec paramètres
     *
     * @param label Le label du nutriment
     * @param mne Le code du type principal de nutriment
     * @param kind Le code du type spécifique de nutriment
     */
    private constructor(label: String, mne: Int, kind: Int) {
        this.label = label
        this.mne = mne
        this.kindnut = kind
    }

    /**
     * Constructeur avec un nutriment
     *
     * @param nutrient Le nutriment à partir duquel initialiser
     */
    constructor(nutrient: Nutrient) {
        this.label = nutrient.label
        this.unit = nutrient.unite
        this.mne = nutrient.getMNE().coef
        this.kindnut = nutrient.coef
    }

    /**
     * Obtient l'identifiant unique de ce nutriment
     *
     * @return L'identifiant unique
     */
    val id: Int
        get() {
            println("ALLnut ID $label ${mne * 1000 + kindnut}")
            return mne * 1000 + kindnut
        }

    companion object {
        /**
         * Obtient tous les nutriments disponibles
         *
         * @return Une map des nutriments par identifiant
         */
        fun values(): Map<Int, AllNutrient> {
            val result = mutableMapOf<Int, AllNutrient>()

            // Ajouter tous les types de nutriments
            for (nutrient in NutrientMain.entries) {
                val allNutrient = AllNutrient(nutrient)
                result[allNutrient.id] = allNutrient
            }

            for (nutrient in NutrientMacro.entries) {
                val allNutrient = AllNutrient(nutrient)
                result[allNutrient.id] = allNutrient
            }

            for (nutrient in NutrientMin.entries) {
                val allNutrient = AllNutrient(nutrient)
                result[allNutrient.id] = allNutrient
            }

            for (nutrient in NutrientAnalysis.entries) {
                val allNutrient = AllNutrient(nutrient)
                result[allNutrient.id] = allNutrient
            }

            for (nutrient in NutrientLipid.entries) {
                val allNutrient = AllNutrient(nutrient)
                result[allNutrient.id] = allNutrient
            }

            for (nutrient in NutrientVitam.entries) {
                val allNutrient = AllNutrient(nutrient)
                result[allNutrient.id] = allNutrient
            }

            for (nutrient in AAEnum.entries) {
                val allNutrient = AllNutrient(nutrient)
                result[allNutrient.id] = allNutrient
            }

            for (nutrient in NutrientOther.entries) {
                val allNutrient = AllNutrient(nutrient)
                result[allNutrient.id] = allNutrient
            }

            return result
        }

        /**
         * Obtient un nutriment par son identifiant
         *
         * @param id L'identifiant du nutriment
         * @return Le nutriment correspondant ou null si non trouvé
         */
        fun getById(id: Int): AllNutrient? {
            return values()[id]
        }

        /**
         * Obtient un nutriment par son type principal et son type spécifique
         *
         * @param mne Le code du type principal de nutriment
         * @param kind Le code du type spécifique de nutriment
         * @return Le nutriment correspondant ou null si non trouvé
         */
        fun getByMneAndKind(mne: Int, kind: Int): AllNutrient? {
            return getById(mne * 1000 + kind)
        }
    }
}
