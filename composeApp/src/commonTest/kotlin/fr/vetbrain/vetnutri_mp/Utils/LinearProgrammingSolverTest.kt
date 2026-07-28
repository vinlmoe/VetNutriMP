package fr.vetbrain.vetnutri_mp.Utils

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LinearProgrammingSolverTest {

    private fun assertNear(expected: Double, actual: Double, tolerance: Double = 1e-4) {
        assertTrue(
            abs(expected - actual) <= tolerance,
            "Expected $expected +/- $tolerance but was $actual"
        )
    }

    @Test
    fun solve_trivialGreaterThan_returnsLowerBoundOfFeasibleRegion() {
        val model = LpModel(
            variables = listOf(LpVariable(name = "x", lowerBound = 0.0, upperBound = 100.0)),
            constraints = listOf(
                LpConstraint("x_ge_10", doubleArrayOf(1.0), LpConstraintSense.GE, 10.0)
            )
        )

        val solution = LinearProgrammingSolver.solve(model)
        assertTrue(solution is LpSolution.Optimal)
        assertNear(10.0, (solution as LpSolution.Optimal).values[0])
    }

    @Test
    fun solve_twoVariablesWithLeAndGe_findsExpectedVertex() {
        // minimize x + y s.t. x + y >= 10, x <= 6, y <= 6, x,y >= 0
        // Feasible region requires x+y>=10; minimizing x+y drives the objective to exactly 10.
        val model = LpModel(
            variables = listOf(
                LpVariable(name = "x", lowerBound = 0.0, upperBound = 6.0, objectiveCoefficient = 1.0),
                LpVariable(name = "y", lowerBound = 0.0, upperBound = 6.0, objectiveCoefficient = 1.0)
            ),
            constraints = listOf(
                LpConstraint("sum_ge_10", doubleArrayOf(1.0, 1.0), LpConstraintSense.GE, 10.0)
            )
        )

        val solution = LinearProgrammingSolver.solve(model)
        assertTrue(solution is LpSolution.Optimal)
        val opt = solution as LpSolution.Optimal
        assertNear(10.0, opt.objectiveValue)
        assertNear(10.0, opt.values[0] + opt.values[1])
    }

    @Test
    fun solve_conflictingBoundsOnSameVariable_returnsInfeasibleWithConstraintNames() {
        val model = LpModel(
            variables = listOf(LpVariable(name = "x", lowerBound = 0.0, upperBound = 100.0)),
            constraints = listOf(
                LpConstraint("x_le_5", doubleArrayOf(1.0), LpConstraintSense.LE, 5.0),
                LpConstraint("x_ge_10", doubleArrayOf(1.0), LpConstraintSense.GE, 10.0)
            )
        )

        val solution = LinearProgrammingSolver.solve(model)
        assertTrue(solution is LpSolution.Infeasible)
        val infeasible = solution as LpSolution.Infeasible
        assertTrue(infeasible.violatedConstraints.contains("x_ge_10"))
    }

    @Test
    fun solve_respectsExplicitUpperBoundOnVariable() {
        // minimize -x (i.e. maximize x) s.t. x <= 3 (variable bound), x can be pushed by a
        // constraint that would otherwise want x = 10.
        val model = LpModel(
            variables = listOf(LpVariable(name = "x", lowerBound = 0.0, upperBound = 3.0, objectiveCoefficient = -1.0)),
            constraints = listOf(
                LpConstraint("x_le_10", doubleArrayOf(1.0), LpConstraintSense.LE, 10.0)
            )
        )

        val solution = LinearProgrammingSolver.solve(model)
        assertTrue(solution is LpSolution.Optimal)
        assertNear(3.0, (solution as LpSolution.Optimal).values[0])
    }

    @Test
    fun solve_threeFoodsTwoMinOneMax_matchesHandComputedVertex() {
        // Three foods (grams as decision vars), coefficients = nutrient per gram.
        // Food A: [protein=0.3, fiber=0.05], Food B: [protein=0.1, fiber=0.2], Food C: [protein=0.0, fiber=0.0]
        // Constraints: protein >= 60, fiber >= 8, fiber <= 30. Minimize total mass (A+B+C).
        val model = LpModel(
            variables = listOf(
                LpVariable(name = "A", objectiveCoefficient = 1.0),
                LpVariable(name = "B", objectiveCoefficient = 1.0),
                LpVariable(name = "C", objectiveCoefficient = 1.0)
            ),
            constraints = listOf(
                LpConstraint("protein_min", doubleArrayOf(0.3, 0.1, 0.0), LpConstraintSense.GE, 60.0),
                LpConstraint("fiber_min", doubleArrayOf(0.05, 0.2, 0.0), LpConstraintSense.GE, 8.0),
                LpConstraint("fiber_max", doubleArrayOf(0.05, 0.2, 0.0), LpConstraintSense.LE, 30.0)
            )
        )

        val solution = LinearProgrammingSolver.solve(model)
        assertTrue(solution is LpSolution.Optimal)
        val opt = solution as LpSolution.Optimal
        // Hand-checked optimum: use only Food A at 200g -> protein=60, fiber=10 (both satisfied,
        // fiber within [8,30]), total mass 200 which is minimal since A alone reaches the
        // protein target with the least mass (0.3 protein/g is the most protein-dense option).
        assertNear(200.0, opt.values[0])
        assertNear(0.0, opt.values[1])
        assertNear(0.0, opt.values[2])
        assertNear(200.0, opt.objectiveValue)
    }

    @Test
    fun solve_lockedFoodAloneViolatesMax_stillDetectedAsInfeasible() {
        // A single free variable with a MAX constraint whose RHS is already negative
        // (simulating a locked food alone exceeding the bound) must be reported infeasible.
        val model = LpModel(
            variables = listOf(LpVariable(name = "x", objectiveCoefficient = 1.0)),
            constraints = listOf(
                LpConstraint("already_over_max", doubleArrayOf(1.0), LpConstraintSense.LE, -5.0)
            )
        )

        val solution = LinearProgrammingSolver.solve(model)
        assertTrue(solution is LpSolution.Infeasible)
        assertTrue((solution as LpSolution.Infeasible).violatedConstraints.contains("already_over_max"))
    }

    @Test
    fun solve_degenerateTie_terminatesWithinIterationBudget() {
        // Two constraints tie exactly at the same ratio limit to exercise Bland's rule tie-break.
        val model = LpModel(
            variables = listOf(
                LpVariable(name = "x", objectiveCoefficient = -1.0),
                LpVariable(name = "y", objectiveCoefficient = -1.0)
            ),
            constraints = listOf(
                LpConstraint("c1", doubleArrayOf(1.0, 1.0), LpConstraintSense.LE, 10.0),
                LpConstraint("c2", doubleArrayOf(1.0, 1.0), LpConstraintSense.LE, 10.0)
            )
        )

        val solution = LinearProgrammingSolver.solve(model, maxIterations = 500)
        assertTrue(solution is LpSolution.Optimal)
        val opt = solution as LpSolution.Optimal
        assertNear(10.0, opt.values[0] + opt.values[1])
    }

    @Test
    fun solve_equalityConstraint_isRespected() {
        val model = LpModel(
            variables = listOf(LpVariable(name = "x", upperBound = 50.0)),
            constraints = listOf(
                LpConstraint("x_eq_7", doubleArrayOf(1.0), LpConstraintSense.EQ, 7.0)
            )
        )

        val solution = LinearProgrammingSolver.solve(model)
        assertTrue(solution is LpSolution.Optimal)
        assertNear(7.0, (solution as LpSolution.Optimal).values[0])
    }
}
