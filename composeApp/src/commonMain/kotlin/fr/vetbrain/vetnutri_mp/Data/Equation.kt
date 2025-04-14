package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.NutrientBaseExt
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Utils.ExpressionEvaluator
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

/**
 * Classe représentant une équation dans le système VetNutriMP Utilisée pour calculer les besoins
 * énergétiques, les poids métaboliques et autres valeurs
 */
@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Equation(
        val uuid: String = Uuid.random().toString(),
        var description: String = "",
        var equationScript: String = "",
        var bib: BiblioRef = BiblioRef(),
        var specie: Espece? = Espece.CHIEN,
        var name: String = "",
        var kind: EquationKind = EquationKind.ENERGYNEED,
        var allNutrient: AllNutrient? = null,
        var consistent: Boolean = true,
        var variables: MutableList<VariableKind> = mutableListOf(),
        var correctionFactor: Double = 1.0
) {

        /**
         * Calcule le résultat de l'équation pour un animal donné
         *
         * @param poids Le poids de l'animal en kg
         * @param svp Liste des variables supplémentaires
         * @return Le résultat du calcul ou 0.0 en cas d'erreur
         */
        fun calculerValeurAnimal(poids: Float, svp: List<SupplementalvariableP>): Double {
                if (kind == EquationKind.ENERGYNEED || kind == EquationKind.MW) {
                        return try {
                                // Préparation des variables pour l'expression
                                val expression = createExpressionWithVariables(poids, svp)

                                // Calcul du résultat
                                evaluerExpression(expression)
                        } catch (e: Exception) {
                                println("Erreur dans le calcul de l'équation: ${e.message}")
                                0.0
                        }
                }
                return 0.0
        }

        /**
         * Calcule le besoin en nutriment pour un animal et une ration donnés
         *
         * @param poids Le poids de l'animal en kg
         * @param bee Le besoin énergétique de base
         * @param poidsMetabolique Le poids métabolique
         * @param svp Liste des variables supplémentaires
         * @param ration La ration à évaluer
         * @return Le résultat du calcul ou 0.0 en cas d'erreur
         */
        fun calculerBesoin(
                poids: Float,
                bee: Float,
                poidsMetabolique: Float,
                svp: List<SupplementalvariableP>,
                ration: Ration
        ): Double {
                if (kind == EquationKind.NEED) {
                        return try {
                                // Création d'une expression avec les variables de base
                                val expression = createExpressionWithVariables(poids, svp)

                                // Ajout des variables spécifiques au besoin
                                expression["BEE"] = bee.toDouble()
                                expression["MW"] = poidsMetabolique.toDouble()

                                // Ajout des nutriments de la ration
                                for (nutrient in NutrientBaseExt.entries) {
                                        expression[nutrient.label] =
                                                ration.getNutrient(nutrient)?.toDouble() ?: 0.0
                                }

                                for (nutrient in NutrientLipid.entries) {
                                        expression[nutrient.label] =
                                                ration.getNutrient(nutrient)?.toDouble() ?: 0.0
                                }

                                // Calcul du résultat
                                evaluerExpression(expression)
                        } catch (e: Exception) {
                                println("Erreur dans le calcul du besoin: ${e.message}")
                                0.0
                        }
                }
                return 0.0
        }

        /**
         * Calcule la densité énergétique d'un aliment
         *
         * @param aliment L'aliment à évaluer
         * @return La densité énergétique calculée ou 0.0 en cas d'erreur
         */
        fun calculerDensiteEnergetique(aliment: AlimentRation): Double {
                if (kind == EquationKind.ENERGYDENSITY) {
                        return try {
                                val expression = mutableMapOf<String, Double>()

                                // Ajout des nutriments de l'aliment
                                for (nutrient in NutrientBaseExt.entries) {
                                        expression[nutrient.label] =
                                                aliment.getNutrient(nutrient)?.toDouble() ?: 0.0
                                }

                                // Calcul du résultat
                                evaluerExpression(expression)
                        } catch (e: Exception) {
                                println(
                                        "Erreur dans le calcul de la densité énergétique: ${e.message}"
                                )
                                0.0
                        }
                }
                return 0.0
        }

        /** Crée une map contenant les variables de base pour une expression */
        private fun createExpressionWithVariables(
                poids: Float,
                svp: List<SupplementalvariableP>
        ): MutableMap<String, Double> {
                val expression = mutableMapOf<String, Double>()

                // Ajout du poids
                expression["BW"] = poids.toDouble()

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
                val nouvelUuid = Uuid.random().toString()
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
                this.allNutrient = equation.allNutrient
                this.consistent = equation.consistent
                this.variables.clear()
                this.variables.addAll(equation.variables)
        }
}
