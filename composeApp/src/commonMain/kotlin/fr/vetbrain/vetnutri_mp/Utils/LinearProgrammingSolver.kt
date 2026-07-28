package fr.vetbrain.vetnutri_mp.Utils

import kotlin.math.abs
import kotlin.math.max

/** Sens d'une contrainte linéaire : somme(coefficients[j] * x[j]) <sense> rhs */
enum class LpConstraintSense {
    LE,
    GE,
    EQ
}

/** Une ligne de contrainte du modèle LP. */
data class LpConstraint(
        val name: String,
        val coefficients: DoubleArray,
        val sense: LpConstraintSense,
        val rhs: Double
)

/** Une variable de décision, avec bornes et coefficient dans la fonction objectif (minimisation). */
data class LpVariable(
        val name: String,
        val lowerBound: Double = 0.0,
        val upperBound: Double = Double.POSITIVE_INFINITY,
        val objectiveCoefficient: Double = 0.0
)

/** Modèle LP complet : minimiser objectiveCoefficient·x sous les contraintes, bornes incluses. */
data class LpModel(val variables: List<LpVariable>, val constraints: List<LpConstraint>)

sealed class LpSolution {
    /** Solution optimale trouvée : une valeur par variable, dans l'ordre de [LpModel.variables]. */
    data class Optimal(val values: DoubleArray, val objectiveValue: Double) : LpSolution()

    /**
     * Aucune solution ne satisfait toutes les contraintes simultanément.
     * [violatedConstraints] contient les noms des [LpConstraint] en conflit.
     */
    data class Infeasible(val violatedConstraints: List<String>) : LpSolution()
}

/**
 * Solveur de programmation linéaire pure-Kotlin (aucune dépendance native), pour des modèles de
 * petite taille (quelques dizaines de variables/contraintes) — utilisé pour l'ajustement de
 * ration sous contraintes MIN/MAX. Implémente un simplex primal à variables bornées
 * (bounded-variable simplex) avec pénalité Big-M pour amorcer la faisabilité des lignes qui ne
 * sont pas satisfaites à l'origine (équivalent à un two-phase mais en une seule boucle de
 * pivotage, ce qui suffit à cette échelle).
 */
object LinearProgrammingSolver {

    private const val TOLERANCE = 1e-6

    fun solve(model: LpModel, maxIterations: Int = 10_000): LpSolution {
        val tableau = Tableau.build(model)
        return tableau.solve(maxIterations)
    }

    /**
     * Représentation interne en forme standard : chaque contrainte devient une égalité en lui
     * ajoutant une colonne d'écart (slack/surplus) et, si nécessaire, une colonne artificielle.
     * Toutes les colonnes (structurelles + écarts + artificielles) sont des variables bornées
     * `[lb, ub]`, gérées sans ligne supplémentaire dans le tableau.
     */
    private class Tableau(
            val numRows: Int,
            val numCols: Int,
            val a: Array<DoubleArray>, // numRows x numCols
            val rhs: DoubleArray, // numRows
            val lower: DoubleArray, // numCols
            val upper: DoubleArray, // numCols
            val cost: DoubleArray, // numCols (coût réel, sans Big-M)
            val isArtificial: BooleanArray, // numCols
            val colNames: Array<String>,
            val rowNames: Array<String>,
            val structuralCount: Int
    ) {
        // État courant de la résolution
        lateinit var basis: IntArray // numRows -> index de colonne basique de cette ligne
        lateinit var atUpperBound: BooleanArray // numCols -> la variable non basique est-elle à sa borne sup ?
        lateinit var value: DoubleArray // numCols -> valeur courante de chaque variable
        var bigM: Double = 0.0

        companion object {
            fun build(model: LpModel): Tableau {
                val numStructural = model.variables.size
                // 2 colonnes réservées par contrainte (slack/surplus + artificielle potentielle) :
                // une ligne LE peut elle aussi nécessiter une artificielle si elle est déjà
                // violée à l'origine (ex. un aliment verrouillé dépassant déjà seul une borne
                // MAX), pas seulement les lignes GE/EQ.
                val numExtra = model.constraints.size * 2
                val numRows = model.constraints.size
                val numCols = numStructural + numExtra

                val a = Array(numRows) { DoubleArray(numCols) }
                val rhs = DoubleArray(numRows)
                val lower = DoubleArray(numCols)
                val upper = DoubleArray(numCols) { Double.POSITIVE_INFINITY }
                val cost = DoubleArray(numCols)
                val isArtificial = BooleanArray(numCols)
                val colNames = Array(numCols) { "" }
                val rowNames = Array(numRows) { "" }

                for (j in 0 until numStructural) {
                    val v = model.variables[j]
                    lower[j] = v.lowerBound
                    upper[j] = v.upperBound
                    cost[j] = v.objectiveCoefficient
                    colNames[j] = v.name
                }

                var nextCol = numStructural
                val basisCols = IntArray(numRows)

                for (i in model.constraints.indices) {
                    val c = model.constraints[i]
                    rowNames[i] = c.name
                    for (j in 0 until numStructural) {
                        a[i][j] = c.coefficients.getOrElse(j) { 0.0 }
                    }
                    // Colonne d'écart : +s pour LE, -s (surplus) pour GE, aucune pour EQ.
                    when (c.sense) {
                        LpConstraintSense.LE -> {
                            val slackCol = nextCol++
                            a[i][slackCol] = 1.0
                            lower[slackCol] = 0.0
                            upper[slackCol] = Double.POSITIVE_INFINITY
                            colNames[slackCol] = "slack_${c.name}"
                            // Faisable à l'origine (x=lb) si le slack calculé reste >= 0.
                        }
                        LpConstraintSense.GE -> {
                            val surplusCol = nextCol++
                            a[i][surplusCol] = -1.0
                            lower[surplusCol] = 0.0
                            upper[surplusCol] = Double.POSITIVE_INFINITY
                            colNames[surplusCol] = "surplus_${c.name}"
                        }
                        LpConstraintSense.EQ -> {
                            // Pas de colonne d'écart pour une égalité.
                        }
                    }
                    rhs[i] = c.rhs
                }

                // Deuxième passe : calcule la contribution des variables structurelles à leur
                // borne inférieure, pour voir si la ligne est déjà faisable avec le seul écart
                // naturel, sinon ajoute une variable artificielle.
                for (i in model.constraints.indices) {
                    val c = model.constraints[i]
                    var lhsAtLowerBounds = 0.0
                    for (j in 0 until numStructural) {
                        lhsAtLowerBounds += a[i][j] * lower[j].let { if (it.isFinite()) it else 0.0 }
                    }
                    val neededSlackValue = rhs[i] - lhsAtLowerBounds

                    val slackOrSurplusCol =
                            (numStructural until nextCol).firstOrNull { col ->
                                colNames[col] == "slack_${c.name}" || colNames[col] == "surplus_${c.name}"
                            }

                    val needsArtificial =
                            when (c.sense) {
                                LpConstraintSense.LE -> neededSlackValue < -TOLERANCE
                                LpConstraintSense.GE -> neededSlackValue > TOLERANCE
                                LpConstraintSense.EQ -> abs(neededSlackValue) > TOLERANCE
                            }

                    if (needsArtificial) {
                        val artCol = nextCol++
                        colNames[artCol] = "artificial_${c.name}"
                        isArtificial[artCol] = true
                        lower[artCol] = 0.0
                        upper[artCol] = Double.POSITIVE_INFINITY
                        a[i][artCol] = if (neededSlackValue >= 0) 1.0 else -1.0
                        basisCols[i] = artCol
                    } else if (slackOrSurplusCol != null) {
                        basisCols[i] = slackOrSurplusCol
                    } else {
                        // Ligne EQ exactement satisfaite par les bornes inférieures : toujours
                        // besoin d'une variable de base -> ajoute une artificielle "muette" à 0.
                        val artCol = nextCol++
                        colNames[artCol] = "artificial_${c.name}"
                        isArtificial[artCol] = true
                        lower[artCol] = 0.0
                        upper[artCol] = Double.POSITIVE_INFINITY
                        a[i][artCol] = 1.0
                        basisCols[i] = artCol
                    }

                    // Invariant requis par le reste de l'algorithme (mise à jour des valeurs,
                    // test de ratio, pivotage) : le coefficient de la variable de base choisie
                    // doit valoir exactement 1 dans sa propre ligne. La colonne surplus (GE) et
                    // certaines artificielles ont un coefficient de -1 : on normalise la ligne
                    // entière (et son second membre) en conséquence.
                    val pivotCoeff = a[i][basisCols[i]]
                    if (abs(pivotCoeff - 1.0) > 1e-12) {
                        for (j in 0 until nextCol) {
                            a[i][j] = a[i][j] / pivotCoeff
                        }
                        rhs[i] = rhs[i] / pivotCoeff
                    }
                }

                val finalNumCols = nextCol
                val aFinal =
                        Array(numRows) { i -> DoubleArray(finalNumCols) { j -> a[i][j] } }
                val lowerFinal = DoubleArray(finalNumCols) { j -> lower[j] }
                val upperFinal = DoubleArray(finalNumCols) { j -> upper[j] }
                val costFinal = DoubleArray(finalNumCols) { j -> cost[j] }
                val isArtificialFinal = BooleanArray(finalNumCols) { j -> isArtificial[j] }
                val colNamesFinal = Array(finalNumCols) { j -> colNames[j] }

                val maxAbsObjective = model.variables.maxOfOrNull { abs(it.objectiveCoefficient) } ?: 0.0
                val maxAbsRhs = model.constraints.maxOfOrNull { abs(it.rhs) } ?: 0.0
                val bigM = 1e7 * (1.0 + max(maxAbsObjective, maxAbsRhs))

                val tableau =
                        Tableau(
                                numRows = numRows,
                                numCols = finalNumCols,
                                a = aFinal,
                                rhs = rhs,
                                lower = lowerFinal,
                                upper = upperFinal,
                                cost = costFinal,
                                isArtificial = isArtificialFinal,
                                colNames = colNamesFinal,
                                rowNames = rowNames,
                                structuralCount = numStructural
                        )
                tableau.basis = basisCols
                tableau.bigM = bigM
                return tableau
            }
        }

        private fun effectiveCost(col: Int): Double =
                if (isArtificial[col]) bigM else cost[col]

        fun solve(maxIterations: Int): LpSolution {
            // Initialisation : variables non basiques posées à leur borne inférieure finie,
            // sinon 0.0 (les colonnes structurelles de cette app ont toutes une lb finie >= 0).
            value = DoubleArray(numCols)
            atUpperBound = BooleanArray(numCols)
            for (j in 0 until numCols) {
                value[j] = if (lower[j].isFinite()) lower[j] else 0.0
            }
            recomputeBasicValues()

            var iterations = 0
            while (iterations < maxIterations) {
                iterations++

                val enteringInfo = selectEnteringColumn() ?: return classifyOptimal()

                val (enteringCol, increasing) = enteringInfo
                pivotOrBoundFlip(enteringCol, increasing)
            }

            // Non convergence : traité comme une infaisabilité générique.
            return LpSolution.Infeasible(listOf("__non_convergent__"))
        }

        /** Recalcule value[] des variables de base à partir des non-basiques (après build initial). */
        private fun recomputeBasicValues() {
            for (i in 0 until numRows) {
                var sum = rhs[i]
                for (j in 0 until numCols) {
                    if (j == basis[i]) continue
                    if (value[j] != 0.0) sum -= a[i][j] * value[j]
                }
                // Ne pas supposer que le coefficient de la variable de base dans sa propre ligne
                // vaut 1 : la colonne surplus (GE) ou une artificielle peut valoir -1 (cf.
                // construction ci-dessus), d'où la division plutôt qu'une simple affectation.
                value[basis[i]] = sum / a[i][basis[i]]
            }
        }

        /** Coût réduit de la colonne j : c_j - somme_i(cB_i * a[i][j]) */
        private fun reducedCost(col: Int): Double {
            var reduced = effectiveCost(col)
            for (i in 0 until numRows) {
                val basicCol = basis[i]
                val cB = effectiveCost(basicCol)
                if (cB != 0.0) reduced -= cB * a[i][col]
            }
            return reduced
        }

        /**
         * Sélectionne la colonne entrante selon le test d'optimalité bornée :
         * - non basique à sa borne inf, candidate si coût réduit < 0 (faire croître la variable réduit le coût)
         * - non basique à sa borne sup, candidate si coût réduit > 0 (faire décroître la variable réduit le coût)
         * Règle de Bland (index le plus petit) pour garantir la terminaison.
         */
        private fun selectEnteringColumn(): Pair<Int, Boolean>? {
            for (j in 0 until numCols) {
                if (basis.contains(j)) continue
                val rc = reducedCost(j)
                val atUpper = atUpperBound[j]
                if (!atUpper && rc < -TOLERANCE) {
                    return Pair(j, true)
                }
                if (atUpper && rc > TOLERANCE) {
                    return Pair(j, false)
                }
            }
            return null
        }

        /**
         * Fait entrer/varier [enteringCol] ([increasing] = true si on l'augmente depuis sa
         * borne inférieure, false si on la diminue depuis sa borne supérieure), en choisissant
         * entre un simple "bound flip" (pas de changement de base) et un pivot classique, selon
         * la plus petite limite atteinte (borne opposée de la variable entrante, ou borne d'une
         * variable de base). Bland's rule pour les égalités de ratio.
         */
        private fun pivotOrBoundFlip(enteringCol: Int, increasing: Boolean) {
            val direction = if (increasing) 1.0 else -1.0
            val enteringRange =
                    if (increasing) upper[enteringCol] - value[enteringCol]
                    else value[enteringCol] - lower[enteringCol]

            var minRatio = enteringRange
            var leavingRow = -1
            var leavingGoesToUpper = false

            for (i in 0 until numRows) {
                val coeff = a[i][enteringCol] * direction
                if (abs(coeff) < TOLERANCE) continue
                val basicCol = basis[i]
                val basicValue = value[basicCol]

                // coeff > 0: la variable de base décroît quand l'entrante croît dans sa
                // direction -> limitée par sa borne inférieure.
                // coeff < 0: la variable de base croît -> limitée par sa borne supérieure.
                val ratio: Double
                val goesToUpper: Boolean
                if (coeff > 0) {
                    ratio =
                            if (lower[basicCol].isFinite()) (basicValue - lower[basicCol]) / coeff
                            else Double.POSITIVE_INFINITY
                    goesToUpper = false
                } else {
                    ratio =
                            if (upper[basicCol].isFinite()) (upper[basicCol] - basicValue) / (-coeff)
                            else Double.POSITIVE_INFINITY
                    goesToUpper = true
                }

                if (ratio < minRatio - TOLERANCE) {
                    minRatio = ratio
                    leavingRow = i
                    leavingGoesToUpper = goesToUpper
                } else if (ratio < minRatio + TOLERANCE) {
                    // Égalité (dans la tolérance) : règle de Bland — préfère la ligne dont la
                    // variable de base a l'index de colonne le plus petit, pour éviter le cyclage.
                    if (leavingRow == -1 || basicCol < basis[leavingRow]) {
                        minRatio = minOf(minRatio, ratio)
                        leavingRow = i
                        leavingGoesToUpper = goesToUpper
                    }
                }
            }

            if (minRatio < 0) minRatio = 0.0

            if (leavingRow == -1) {
                // Bound flip pur : l'entrante va jusqu'à sa borne opposée sans qu'aucune
                // variable de base ne devienne limitante.
                applyDelta(enteringCol, direction * minRatio)
                atUpperBound[enteringCol] = increasing
                return
            }

            // Applique le déplacement à toutes les variables de base + l'entrante.
            applyDelta(enteringCol, direction * minRatio)

            val leavingCol = basis[leavingRow]
            value[leavingCol] = if (leavingGoesToUpper) upper[leavingCol] else lower[leavingCol]
            atUpperBound[leavingCol] = leavingGoesToUpper

            // Pivot Gauss-Jordan sur (leavingRow, enteringCol).
            val pivotElement = a[leavingRow][enteringCol]
            for (j in 0 until numCols) {
                a[leavingRow][j] = a[leavingRow][j] / pivotElement
            }
            for (i in 0 until numRows) {
                if (i == leavingRow) continue
                val factor = a[i][enteringCol]
                if (abs(factor) < TOLERANCE) continue
                for (j in 0 until numCols) {
                    a[i][j] -= factor * a[leavingRow][j]
                }
            }
            basis[leavingRow] = enteringCol
            atUpperBound[enteringCol] = false
        }

        /** Applique un déplacement [delta] à la variable entrante et répercute sur les variables de base via les coefficients de colonne. */
        private fun applyDelta(enteringCol: Int, delta: Double) {
            for (i in 0 until numRows) {
                val coeff = a[i][enteringCol]
                if (coeff != 0.0) {
                    value[basis[i]] -= coeff * delta
                }
            }
            value[enteringCol] += delta
        }

        private fun classifyOptimal(): LpSolution {
            val violated = mutableListOf<String>()
            for (i in 0 until numRows) {
                val col = basis[i]
                if (isArtificial[col] && abs(value[col]) > TOLERANCE) {
                    violated.add(rowNames[i])
                }
            }
            if (violated.isNotEmpty()) {
                return LpSolution.Infeasible(violated.distinct())
            }

            val values = DoubleArray(structuralCount) { j -> value[j] }
            var objective = 0.0
            for (j in 0 until structuralCount) {
                objective += cost[j] * value[j]
            }
            return LpSolution.Optimal(values, objective)
        }
    }
}
