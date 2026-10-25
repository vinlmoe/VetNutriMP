package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Utils.ExpressionEvaluator
import kotlin.math.pow

/**
 * Poids métabolique et besoin énergétique standard (BEE) à partir des équations BW et BEE d'un
 * référentiel. Partagé entre l'analyse d'une consultation (AnimalDetailViewModel) et
 * l'exploration multiration, pour que les deux calculent exactement les mêmes besoins.
 */
object CalculMetabolique {

    /**
     * Exposant allométrique utilisé en dernier recours (BW^0,75, Kleiber) lorsqu'aucun référentiel
     * ou aucune équation BW n'est disponible. Dès qu'un référentiel est connu, c'est son équation
     * BW qui fait foi (ex. BW^0,67 chez le chat).
     */
    const val EXPOSANT_PAR_DEFAUT: Double = 0.75

    /** Valeurs par défaut des variables courantes absentes de la consultation. */
    private val valeursParDefaut =
            mapOf(
                    "wG" to 0.0, // Weight gain (gain de poids)
                    "AW" to 0.0, // Adult weight (poids adulte)
                    "L" to 0.0, // Lactation
                    "wL" to 0.0, // Weight loss (perte de poids)
                    "BCS" to 5.0, // Body condition score
                    "REI" to 1.0, // Reproductive efficiency index
                    "AF" to 1.0, // Activity factor
                    "TE" to 1.0, // Thermic effect
                    "GE" to 1.0, // Growth efficiency
                    "ME" to 1.0 // Metabolizable energy
            )

    /**
     * Complète [variables] avec les valeurs par défaut des variables utilisées par l'équation.
     * @return les variables utilisées pour lesquelles aucune valeur n'est connue
     */
    fun ajouterVariablesParDefaut(
            variables: MutableMap<String, Double>,
            equationScript: String
    ): List<String> {
        val variablesManquantes = mutableListOf<String>()
        ExpressionEvaluator.extraireVariables(equationScript).forEach { variable ->
            if (!variables.containsKey(variable)) {
                val defaut = valeursParDefaut[variable]
                if (defaut != null) variables[variable] = defaut else variablesManquantes.add(variable)
            }
        }
        return variablesManquantes
    }

    /** Poids métabolique par l'équation BW du référentiel ; null si l'équation est absente. */
    fun poidsMetabolique(
            poids: Double,
            reference: ReferenceEv,
            variablesSupp: List<SupplementalvariableP> = emptyList()
    ): Double? = poidsMetabolique(poids, reference.equationBW, variablesSupp)

    /** Poids métabolique par une équation BW donnée ; null si l'équation est absente ou invalide. */
    fun poidsMetabolique(
            poids: Double,
            equationBW: Equation?,
            variablesSupp: List<SupplementalvariableP> = emptyList()
    ): Double? = evaluer(equationBW, poids, variablesSupp)

    /**
     * Poids métabolique par l'équation BW si elle est disponible et évaluable, sinon
     * BW^[EXPOSANT_PAR_DEFAUT]. À utiliser dans les contextes où le référentiel peut manquer
     * (consultation sans référentiel, calculateur générique) ; le poids nul ou négatif donne 0.
     */
    fun poidsMetaboliqueOuDefaut(
            poids: Double,
            equationBW: Equation?,
            variablesSupp: List<SupplementalvariableP> = emptyList()
    ): Double {
        if (poids <= 0.0 || poids.isNaN()) return 0.0
        val parEquation = poidsMetabolique(poids, equationBW, variablesSupp)
        return if (parEquation != null && parEquation.isFinite() && parEquation > 0.0) parEquation
        else poids.pow(EXPOSANT_PAR_DEFAUT)
    }

    /** Besoin énergétique standard par l'équation BEE du référentiel ; null si absente. */
    fun besoinEnergetiqueStandard(
            poids: Double,
            reference: ReferenceEv,
            variablesSupp: List<SupplementalvariableP> = emptyList()
    ): Double? = besoinEnergetiqueStandard(poids, reference.equationBEE, variablesSupp)

    /** Besoin énergétique standard par une équation BEE donnée ; null si absente ou invalide. */
    fun besoinEnergetiqueStandard(
            poids: Double,
            equationBEE: Equation?,
            variablesSupp: List<SupplementalvariableP> = emptyList()
    ): Double? = evaluer(equationBEE, poids, variablesSupp)

    /**
     * Besoin énergétique après application du coefficient d'ajustement K (besoin standard × K) ;
     * null si le besoin standard ne peut pas être calculé.
     */
    fun besoinEnergetiqueAjuste(
            poids: Double,
            equationBEE: Equation?,
            coefficientK: Double,
            variablesSupp: List<SupplementalvariableP> = emptyList()
    ): Double? = besoinEnergetiqueStandard(poids, equationBEE, variablesSupp)?.let { it * coefficientK }

    /**
     * Poids idéal estimé (kg) à partir du poids et de la note d'état corporel sur 9 : chaque point
     * au-dessus (au-dessous) de 5 correspond à 10 % de poids en plus (en moins).
     */
    fun estimerPoidsIdeal(poids: Double, nec: Int): Double = poids * 100 / (100 + (nec - 5) * 10)

    private fun evaluer(
            equation: Equation?,
            poids: Double,
            variablesSupp: List<SupplementalvariableP>
    ): Double? {
        if (equation == null || equation.equationScript.isEmpty()) return null
        return try {
            val variables = mutableMapOf("BW" to poids)
            // Variables supplémentaires (nom défini dans les équations : AW, wG, D...)
            VariablesEtape.injecterVariables(variables, variablesSupp)
            ajouterVariablesParDefaut(variables, equation.equationScript)
            ExpressionEvaluator.evaluer(equation.equationScript, variables)
        } catch (e: Exception) {
            null
        }
    }
}
