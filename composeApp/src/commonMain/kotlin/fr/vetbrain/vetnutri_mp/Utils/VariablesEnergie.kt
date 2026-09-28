package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind

/**
 * Détermine les variables supplémentaires à saisir, à partir des noms réellement écrits dans les
 * équations des références (le nom d'une variable est défini au niveau de l'équation).
 */
object VariablesEnergie {

    /** Variables calculées ou pilotées par le système : jamais saisies. */
    private val variablesCalculees =
            setOf(VariableKind.BW, VariableKind.MW, VariableKind.BEE, VariableKind.BE)

    /**
     * Variables requises par une référence et ses références complémentaires (maladies).
     *
     * @param inclureEquationsNutritionnelles inclut aussi les équations de besoins nutritionnels de
     * la référence générale (saisie au niveau consultation). Faux pour une étape de plan évolutif :
     * seules les équations d'énergie et d'énergie complémentaire (ENERCOMP) comptent.
     */
    fun variablesRequises(
            reference: ReferenceEv?,
            referencesComplementaires: List<ReferenceEv> = emptyList(),
            inclureEquationsNutritionnelles: Boolean = true
    ): List<VariableKind> {
        if (reference == null) return emptyList()

        val scripts = mutableListOf<String>()
        listOfNotNull(
                        reference.equationBW,
                        reference.equationBEE,
                        reference.equationDEcom,
                        reference.equationDEraw,
                        reference.equationME
                )
                .forEach { scripts.add(it.equationScript) }
        if (inclureEquationsNutritionnelles) {
            reference.equationsNut.forEach { scripts.add(it.equationScript) }
        }
        referencesComplementaires.forEach { ref ->
            ref.equationsNut
                    .filter { it.kind == EquationKind.ENERCOMP }
                    .forEach { scripts.add(it.equationScript) }
        }

        val noms =
                scripts.filter { it.isNotBlank() }
                        .flatMap { ExpressionEvaluator.extraireVariables(it) }
                        .toSet()

        return VariableKind.entries
                .filter { it.label in noms && it !in variablesCalculees }
                .sortedBy { it.label }
    }

    /** Variables propres à une étape de plan évolutif (énergie + énergie complémentaire). */
    fun variablesEnergieRequises(
            reference: ReferenceEv?,
            referencesComplementaires: List<ReferenceEv> = emptyList()
    ): List<VariableKind> =
            variablesRequises(
                    reference,
                    referencesComplementaires,
                    inclureEquationsNutritionnelles = false
            )
}
