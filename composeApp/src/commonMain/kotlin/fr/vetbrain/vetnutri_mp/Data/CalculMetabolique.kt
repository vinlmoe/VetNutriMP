package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Utils.ExpressionEvaluator

/**
 * Poids métabolique et besoin énergétique standard (BEE) à partir des équations BW et BEE d'un
 * référentiel. Partagé entre l'analyse d'une consultation (AnimalDetailViewModel) et
 * l'exploration multiration, pour que les deux calculent exactement les mêmes besoins.
 */
object CalculMetabolique {

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
    ): Double? = evaluer(reference.equationBW, poids, variablesSupp)

    /** Besoin énergétique standard par l'équation BEE du référentiel ; null si absente. */
    fun besoinEnergetiqueStandard(
            poids: Double,
            reference: ReferenceEv,
            variablesSupp: List<SupplementalvariableP> = emptyList()
    ): Double? = evaluer(reference.equationBEE, poids, variablesSupp)

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
