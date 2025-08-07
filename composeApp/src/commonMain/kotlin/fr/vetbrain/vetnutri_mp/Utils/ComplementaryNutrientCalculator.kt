package fr.vetbrain.vetnutri_mp.Utils

import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.PreferencesEspece
import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository

/**
 * Service pour calculer les nutriments complémentaires en utilisant des équations quand un
 * nutriment n'est pas directement disponible dans un aliment
 */
object ComplementaryNutrientCalculator {

    /**
     * Calcule la valeur d'un nutriment en utilisant une équation complémentaire
     *
     * @param nutriment Le nutriment à calculer
     * @param alimentRation L'aliment ration pour lequel calculer le nutriment
     * @param preferences Les préférences de l'espèce
     * @param equationRepository Le repository des équations
     * @return La valeur calculée du nutriment ou null si pas d'équation disponible
     */
    suspend fun calculerNutrimentComplementaire(
            nutriment: Nutrient,
            alimentRation: AlimentRation,
            preferences: PreferencesEspece,
            equationRepository: EquationRepository
    ): Float? {
        // Vérifier si une équation complémentaire est configurée pour ce nutriment
        val equationUuid = preferences.getEquationComplementaire(nutriment.label)
        if (equationUuid == null) {
            return null
        }

        // Récupérer l'équation
        val equation = equationRepository.getEquationById(equationUuid)
        if (equation == null || equation.kind != EquationKind.COMPLEMENTARY_NUTRIENT) {
            return null
        }

        // Calculer la valeur en utilisant l'équation
        return calculerAvecEquation(equation, alimentRation)
    }

    /**
     * Calcule la valeur d'un nutriment en utilisant une équation spécifique
     *
     * @param equation L'équation à utiliser
     * @param alimentRation L'aliment ration
     * @return La valeur calculée ou null en cas d'erreur
     */
    private fun calculerAvecEquation(equation: Equation, alimentRation: AlimentRation): Float? {
        try {
            // Créer les variables pour l'évaluation
            val variables = mutableMapOf<String, Double>()

            // Variables de base de l'aliment
            variables["QUANTITE"] = alimentRation.quantite.toDouble()
            variables["PROPORTION"] = alimentRation.proportion.toDouble()
            variables["WEIGHT"] = alimentRation.weight.toDouble()
            variables["DENSITE_ENERGETIQUE"] = alimentRation.densiteEnergetique

            // Variables des nutriments disponibles dans l'aliment
            alimentRation.aliment?.valMap?.forEach { (nutrient, value) ->
                variables[nutrient.label.uppercase()] = value.value.toDouble()
            }

            // Évaluer l'équation
            val result = ExpressionMathematique.evaluer(equation.equationScript, variables)
            return result?.toFloat()
        } catch (e: Exception) {
            println("Erreur lors du calcul du nutriment complémentaire: ${e.message}")
            return null
        }
    }

    /**
     * Vérifie si un nutriment peut être calculé avec une équation complémentaire
     *
     * @param nutriment Le nutriment à vérifier
     * @param preferences Les préférences de l'espèce
     * @param equationRepository Le repository des équations
     * @return true si une équation complémentaire est disponible
     */
    suspend fun peutCalculerNutriment(
            nutriment: Nutrient,
            preferences: PreferencesEspece,
            equationRepository: EquationRepository
    ): Boolean {
        val equationUuid = preferences.getEquationComplementaire(nutriment.label)
        if (equationUuid == null) {
            return false
        }

        val equation = equationRepository.getEquationById(equationUuid)
        return equation != null && equation.kind == EquationKind.COMPLEMENTARY_NUTRIENT
    }

    /**
     * Obtient toutes les équations complémentaires disponibles
     *
     * @param equationRepository Le repository des équations
     * @return Liste des équations de type COMPLEMENTARY_NUTRIENT
     */
    suspend fun getEquationsComplementaires(
            equationRepository: EquationRepository
    ): List<Equation> {
        return equationRepository.getAllEquations().filter {
            it.kind == EquationKind.COMPLEMENTARY_NUTRIENT
        }
    }
}
