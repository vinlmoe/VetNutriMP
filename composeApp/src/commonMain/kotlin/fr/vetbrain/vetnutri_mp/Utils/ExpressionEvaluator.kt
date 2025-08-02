package fr.vetbrain.vetnutri_mp.Utils

/**
 * Classe utilitaire pour évaluer des expressions mathématiques Utilise le nouveau MathParser pour
 * une évaluation complète des expressions
 */
object ExpressionEvaluator {

    /**
     * Évalue une expression mathématique avec les variables fournies
     *
     * @param expression L'expression mathématique à évaluer
     * @param variables Les variables à utiliser dans l'évaluation
     * @return Le résultat de l'évaluation ou null en cas d'erreur
     */
    fun evaluer(expression: String, variables: Map<String, Double>): Double? {
        return try {
            // Utiliser le nouveau parser mathématique
            ExpressionMathematique.evaluer(expression, variables)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Vérifie si une expression est valide
     *
     * @param expression L'expression à vérifier
     * @param variables Les variables disponibles pour la validation
     * @return true si l'expression est valide, false sinon
     */
    fun estExpressionValide(
            expression: String,
            variables: Map<String, Double> = emptyMap()
    ): Boolean {
        return ExpressionMathematique.estValide(expression, variables)
    }

    /**
     * Extrait toutes les variables utilisées dans une expression
     *
     * @param expression L'expression à analyser
     * @return Liste des noms de variables trouvées dans l'expression
     */
    fun extraireVariables(expression: String): List<String> {
        return ExpressionMathematique.extraireVariables(expression)
    }

    /**
     * Valide une expression avec un ensemble de variables connues et retourne les variables
     * manquantes
     *
     * @param expression L'expression à valider
     * @param variablesDisponibles Les variables disponibles
     * @return Liste des variables manquantes, vide si toutes sont disponibles
     */
    fun validerVariables(expression: String, variablesDisponibles: Set<String>): List<String> {
        val variablesUtilisees = extraireVariables(expression)
        return variablesUtilisees.filter { it !in variablesDisponibles }
    }

    /**
     * Teste une expression avec des valeurs par défaut pour toutes les variables
     *
     * @param expression L'expression à tester
     * @param valeurParDefaut La valeur par défaut à utiliser pour toutes les variables
     * @return Le résultat du test ou null en cas d'erreur
     */
    fun testerExpression(expression: String, valeurParDefaut: Double = 1.0): Double? {
        val variables = extraireVariables(expression)
        val variablesTest = variables.associateWith { valeurParDefaut }
        return evaluer(expression, variablesTest)
    }
}
