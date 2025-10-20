import fr.vetbrain.vetnutri_mp.Utils.ExpressionMathematique

/**
 * Test pour vérifier que les valeurs NA sont correctement gérées dans les calculs d'équations
 */
fun main() {
    println("Test de gestion des valeurs NA dans les calculs d'équations")
    
    // Test 1: Variables avec valeurs NaN
    val variablesAvecNaN = mapOf(
        "PROTEINE" to Double.NaN,
        "LIPIDE" to 10.5,
        "HUMIDITE" to 15.0
    )
    
    val expression1 = "PROTEINE + LIPIDE + HUMIDITE"
    val resultat1 = ExpressionMathematique.evaluer(expression1, variablesAvecNaN)
    println("Test 1 - Expression: $expression1")
    println("Variables: $variablesAvecNaN")
    println("Résultat attendu: 25.5 (0.0 + 10.5 + 15.0)")
    println("Résultat obtenu: $resultat1")
    println("Test réussi: ${resultat1 == 25.5}")
    println()
    
    // Test 2: Variables avec valeurs infinies
    val variablesAvecInfini = mapOf(
        "PROTEINE" to Double.POSITIVE_INFINITY,
        "LIPIDE" to 5.0,
        "HUMIDITE" to 12.0
    )
    
    val expression2 = "PROTEINE * 0.5 + LIPIDE + HUMIDITE"
    val resultat2 = ExpressionMathematique.evaluer(expression2, variablesAvecInfini)
    println("Test 2 - Expression: $expression2")
    println("Variables: $variablesAvecInfini")
    println("Résultat attendu: 17.0 (0.0 * 0.5 + 5.0 + 12.0)")
    println("Résultat obtenu: $resultat2")
    println("Test réussi: ${resultat2 == 17.0}")
    println()
    
    // Test 3: Variables manquantes
    val variablesManquantes = mapOf(
        "LIPIDE" to 8.0,
        "HUMIDITE" to 20.0
    )
    
    val expression3 = "PROTEINE + LIPIDE + HUMIDITE"
    val resultat3 = ExpressionMathematique.evaluer(expression3, variablesManquantes)
    println("Test 3 - Expression: $expression3")
    println("Variables: $variablesManquantes")
    println("Résultat attendu: 28.0 (0.0 + 8.0 + 20.0)")
    println("Résultat obtenu: $resultat3")
    println("Test réussi: ${resultat3 == 28.0}")
    println()
    
    // Test 4: Calcul énergétique avec valeurs NA
    val variablesEnergetiques = mapOf(
        "PROTEINE" to Double.NaN,
        "LIPIDE" to 5.0,
        "HUMIDITE" to 12.0,
        "CENDRE" to 3.0
    )
    
    val expressionEnergetique = "PROTEINE * 4 + LIPIDE * 9 + (100 - PROTEINE - LIPIDE - HUMIDITE - CENDRE) * 4"
    val resultatEnergetique = ExpressionMathematique.evaluer(expressionEnergetique, variablesEnergetiques)
    println("Test 4 - Calcul énergétique avec valeurs NA")
    println("Expression: $expressionEnergetique")
    println("Variables: $variablesEnergetiques")
    println("Résultat attendu: 320.0 (0.0 * 4 + 5.0 * 9 + (100 - 0.0 - 5.0 - 12.0 - 3.0) * 4)")
    println("Résultat obtenu: $resultatEnergetique")
    println("Test réussi: ${resultatEnergetique == 320.0}")
    println()
    
    println("Tous les tests sont terminés.")
}
