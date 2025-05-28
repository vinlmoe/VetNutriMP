package fr.vetbrain.vetnutri_mp.Utils

import kotlin.math.*

/**
 * Parser d'expressions mathématiques pour VetNutriMP Supporte les opérations arithmétiques de base,
 * les fonctions mathématiques et les variables
 */
class MathParser {

    private var variables: Map<String, Double> = emptyMap()
    private var position: Int = 0
    private var expression: String = ""

    /**
     * Évalue une expression mathématique avec les variables fournies
     *
     * @param expr L'expression mathématique à évaluer
     * @param vars Les variables à utiliser dans l'évaluation
     * @return Le résultat de l'évaluation
     * @throws MathParserException En cas d'erreur de syntaxe ou d'évaluation
     */
    fun evaluer(expr: String, vars: Map<String, Double> = emptyMap()): Double {
        this.expression = expr.replace(" ", "") // Supprimer les espaces
        this.variables = vars
        this.position = 0

        if (expression.isEmpty()) {
            throw MathParserException("Expression vide")
        }

        val result = parseExpression()

        if (position < expression.length) {
            throw MathParserException(
                    "Caractères inattendus à la fin de l'expression: '${expression.substring(position)}'"
            )
        }

        return result
    }

    /**
     * Vérifie si une expression est syntaxiquement valide
     *
     * @param expr L'expression à vérifier
     * @param vars Les variables disponibles
     * @return true si l'expression est valide, false sinon
     */
    fun estValide(expr: String, vars: Map<String, Double> = emptyMap()): Boolean {
        return try {
            evaluer(expr, vars)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Extrait toutes les variables utilisées dans une expression
     *
     * @param expr L'expression à analyser
     * @return Liste des noms de variables trouvées
     */
    fun extraireVariables(expr: String): List<String> {
        val variables = mutableSetOf<String>()
        val cleanExpr = expr.replace(" ", "")
        var i = 0

        while (i < cleanExpr.length) {
            if (cleanExpr[i].isLetter()) {
                val start = i
                while (i < cleanExpr.length &&
                        (cleanExpr[i].isLetterOrDigit() || cleanExpr[i] == '_')) {
                    i++
                }
                val variable = cleanExpr.substring(start, i)

                // Vérifier que ce n'est pas une fonction mathématique
                if (!estFonctionMathematique(variable)) {
                    variables.add(variable)
                }
            } else {
                i++
            }
        }

        return variables.toList()
    }

    // Parsing des expressions
    private fun parseExpression(): Double {
        var result = parseTerm()

        while (position < expression.length) {
            when (currentChar()) {
                '+' -> {
                    nextChar()
                    result += parseTerm()
                }
                '-' -> {
                    nextChar()
                    result -= parseTerm()
                }
                else -> break
            }
        }

        return result
    }

    private fun parseTerm(): Double {
        var result = parseFactor()

        while (position < expression.length) {
            when (currentChar()) {
                '*' -> {
                    nextChar()
                    result *= parseFactor()
                }
                '/' -> {
                    nextChar()
                    val divisor = parseFactor()
                    if (divisor == 0.0) {
                        throw MathParserException("Division par zéro")
                    }
                    result /= divisor
                }
                else -> break
            }
        }

        return result
    }

    private fun parseFactor(): Double {
        var result = parseBase()

        while (position < expression.length) {
            when (currentChar()) {
                '^' -> {
                    nextChar()
                    result = result.pow(parseBase())
                }
                else -> break
            }
        }

        return result
    }

    private fun parseBase(): Double {
        // Gestion du signe unaire
        if (currentChar() == '-') {
            nextChar()
            return -parseBase()
        }

        if (currentChar() == '+') {
            nextChar()
            return parseBase()
        }

        // Parenthèses
        if (currentChar() == '(') {
            nextChar()
            val result = parseExpression()
            if (currentChar() != ')') {
                throw MathParserException("Parenthèse fermante manquante")
            }
            nextChar()
            return result
        }

        // Nombres
        if (currentChar().isDigit() || currentChar() == '.') {
            return parseNumber()
        }

        // Variables et fonctions
        if (currentChar().isLetter()) {
            return parseVariableOrFunction()
        }

        throw MathParserException("Caractère inattendu: '${currentChar()}'")
    }

    private fun parseNumber(): Double {
        val start = position

        while (position < expression.length && (currentChar().isDigit() || currentChar() == '.')) {
            position++
        }

        val numberStr = expression.substring(start, position)
        return numberStr.toDoubleOrNull()
                ?: throw MathParserException("Nombre invalide: '$numberStr'")
    }

    private fun parseVariableOrFunction(): Double {
        val start = position

        while (position < expression.length &&
                (currentChar().isLetterOrDigit() || currentChar() == '_')) {
            position++
        }

        val name = expression.substring(start, position)

        // Vérifier si c'est une fonction
        if (position < expression.length && currentChar() == '(') {
            return parseFunction(name)
        }

        // C'est une variable
        return variables[name] ?: throw MathParserException("Variable non définie: '$name'")
    }

    private fun parseFunction(functionName: String): Double {
        nextChar() // Consommer '('

        val args = mutableListOf<Double>()

        if (currentChar() != ')') {
            args.add(parseExpression())

            while (currentChar() == ',') {
                nextChar()
                args.add(parseExpression())
            }
        }

        if (currentChar() != ')') {
            throw MathParserException(
                    "Parenthèse fermante manquante pour la fonction '$functionName'"
            )
        }
        nextChar()

        return evaluerFonction(functionName, args)
    }

    private fun evaluerFonction(name: String, args: List<Double>): Double {
        return when (name.lowercase()) {
            "sin" -> {
                verifierNombreArguments(name, args, 1)
                sin(args[0])
            }
            "cos" -> {
                verifierNombreArguments(name, args, 1)
                cos(args[0])
            }
            "tan" -> {
                verifierNombreArguments(name, args, 1)
                tan(args[0])
            }
            "asin" -> {
                verifierNombreArguments(name, args, 1)
                asin(args[0])
            }
            "acos" -> {
                verifierNombreArguments(name, args, 1)
                acos(args[0])
            }
            "atan" -> {
                verifierNombreArguments(name, args, 1)
                atan(args[0])
            }
            "sqrt" -> {
                verifierNombreArguments(name, args, 1)
                if (args[0] < 0) {
                    throw MathParserException("Racine carrée d'un nombre négatif")
                }
                sqrt(args[0])
            }
            "abs" -> {
                verifierNombreArguments(name, args, 1)
                abs(args[0])
            }
            "ln" -> {
                verifierNombreArguments(name, args, 1)
                if (args[0] <= 0) {
                    throw MathParserException("Logarithme d'un nombre négatif ou nul")
                }
                ln(args[0])
            }
            "log" -> {
                verifierNombreArguments(name, args, 1)
                if (args[0] <= 0) {
                    throw MathParserException("Logarithme d'un nombre négatif ou nul")
                }
                log10(args[0])
            }
            "exp" -> {
                verifierNombreArguments(name, args, 1)
                exp(args[0])
            }
            "pow" -> {
                verifierNombreArguments(name, args, 2)
                args[0].pow(args[1])
            }
            "min" -> {
                if (args.isEmpty()) {
                    throw MathParserException("La fonction 'min' nécessite au moins un argument")
                }
                args.minOrNull()!!
            }
            "max" -> {
                if (args.isEmpty()) {
                    throw MathParserException("La fonction 'max' nécessite au moins un argument")
                }
                args.maxOrNull()!!
            }
            "floor" -> {
                verifierNombreArguments(name, args, 1)
                floor(args[0])
            }
            "ceil" -> {
                verifierNombreArguments(name, args, 1)
                ceil(args[0])
            }
            "round" -> {
                verifierNombreArguments(name, args, 1)
                round(args[0])
            }
            else -> throw MathParserException("Fonction inconnue: '$name'")
        }
    }

    private fun verifierNombreArguments(functionName: String, args: List<Double>, expected: Int) {
        if (args.size != expected) {
            throw MathParserException(
                    "La fonction '$functionName' attend $expected argument(s), mais ${args.size} fourni(s)"
            )
        }
    }

    private fun estFonctionMathematique(name: String): Boolean {
        val fonctions =
                setOf(
                        "sin",
                        "cos",
                        "tan",
                        "asin",
                        "acos",
                        "atan",
                        "sqrt",
                        "abs",
                        "ln",
                        "log",
                        "exp",
                        "pow",
                        "min",
                        "max",
                        "floor",
                        "ceil",
                        "round"
                )
        return fonctions.contains(name.lowercase())
    }

    // Utilitaires de navigation dans l'expression
    private fun currentChar(): Char {
        return if (position < expression.length) expression[position] else '\u0000'
    }

    private fun nextChar() {
        position++
    }
}

/** Exception levée lors d'erreurs de parsing ou d'évaluation d'expressions mathématiques */
class MathParserException(message: String) : Exception(message)

/** Objet singleton pour faciliter l'utilisation du parser */
object ExpressionMathematique {
    private val parser = MathParser()

    /**
     * Évalue une expression mathématique
     *
     * @param expression L'expression à évaluer
     * @param variables Les variables disponibles
     * @return Le résultat de l'évaluation ou null en cas d'erreur
     */
    fun evaluer(expression: String, variables: Map<String, Double> = emptyMap()): Double? {
        return try {
            parser.evaluer(expression, variables)
        } catch (e: Exception) {
            println("Erreur lors de l'évaluation de l'expression '$expression': ${e.message}")
            null
        }
    }

    /**
     * Vérifie si une expression est valide
     *
     * @param expression L'expression à vérifier
     * @param variables Les variables disponibles
     * @return true si l'expression est valide, false sinon
     */
    fun estValide(expression: String, variables: Map<String, Double> = emptyMap()): Boolean {
        return parser.estValide(expression, variables)
    }

    /**
     * Extrait les variables d'une expression
     *
     * @param expression L'expression à analyser
     * @return Liste des variables trouvées
     */
    fun extraireVariables(expression: String): List<String> {
        return parser.extraireVariables(expression)
    }
}
