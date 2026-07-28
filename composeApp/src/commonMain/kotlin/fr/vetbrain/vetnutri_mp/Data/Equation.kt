package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Utils.ExpressionEvaluator
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import fr.vetbrain.vetnutri_mp.Utils.instantNow
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.serialization.Serializable

/**
 * Classe représentant une équation dans le système VetNutriMP Utilisée pour calculer les besoins
 * énergétiques, les poids métaboliques et autres valeurs
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Equation(
        val uuid: String = genUUID(),
        var description: String = "",
        var equationScript: String = "",
        var bib: BiblioRef = BiblioRef(),
        var specie: Espece? = Espece.CHIEN,
        var name: String = "",
        var kind: EquationKind = EquationKind.ENERGYNEED,
        var nutrient: Nutrient? = null,
        var consistent: Boolean = true,
        var variables: MutableList<VariableKind> = mutableListOf(),
        var correctionFactor: Double = 1.0,
        var ratio: Boolean = false,
        var creationDate: Long = instantNow().toEpochMilliseconds(),
        var lastUpdate: Long = instantNow().toEpochMilliseconds()
) {
        /** Crée une map contenant les variables de base pour une expression */
        private fun createExpressionWithVariables(
                poids: Double,
                svp: List<SupplementalvariableP>
        ): MutableMap<String, Double> {
                val expression = mutableMapOf<String, Double>()

                // Ajout du poids
                expression["BW"] = poids

                // Ajout des variables supplémentaires
                for (variable in svp) {
                        variable.variable?.let { varKind ->
                                expression[varKind.variable] = variable.varue?.toDouble() ?: 0.0
                        }
                }

                return expression
        }

        /** Évalue une expression mathématique en utilisant l'ExpressionEvaluator */
        private fun evaluerExpression(variables: Map<String, Double>): Double {
                // Utilisation de l'ExpressionEvaluator pour évaluer l'expression
                return ExpressionEvaluator.evaluer(equationScript, variables) ?: 0.0
        }

        /** Ajoute une variable à l'équation */
        fun ajouterVariable(variable: VariableKind) {
                if (!variables.contains(variable)) {
                        variables.add(variable)
                }
        }

        /** Supprime toutes les variables de l'équation */
        fun supprimerToutesVariables() {
                variables.clear()
        }

        /** Génère un nouvel UUID pour cette équation */
        @OptIn(ExperimentalUuidApi::class)
        fun genererNouvelUUID() {
                val nouvelUuid = genUUID()
                // Note: Comme l'UUID est un val, cette opération ne modifie pas l'UUID existant
                // Il faudrait créer une nouvelle instance d'Equation avec le nouvel UUID
        }

        /** Met à jour cette équation avec les valeurs d'une autre équation */
        fun mettreAJour(equation: Equation) {
                this.description = equation.description
                this.equationScript = equation.equationScript
                this.bib = equation.bib
                this.specie = equation.specie
                this.name = equation.name
                this.kind = equation.kind
                this.nutrient = equation.nutrient
                this.consistent = equation.consistent
                this.variables.clear()
                this.variables.addAll(equation.variables)
                this.ratio = equation.ratio
                this.creationDate = equation.creationDate
                this.lastUpdate = equation.lastUpdate
        }
}
