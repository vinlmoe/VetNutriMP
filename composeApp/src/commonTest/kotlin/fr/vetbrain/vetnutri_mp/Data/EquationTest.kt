package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class EquationTest {

    private fun makeEquation(
        script: String = "BW * 70",
        kind: EquationKind = EquationKind.ENERGYNEED
    ) = Equation(uuid = "eq-uuid", name = "TestEq", equationScript = script, kind = kind)

    // ── ajouterVariable ───────────────────────────────────────────────────────

    @Test
    fun ajouterVariable_addsVariable() {
        val eq = makeEquation()
        eq.ajouterVariable(VariableKind.BW)
        assertTrue(eq.variables.contains(VariableKind.BW))
    }

    @Test
    fun ajouterVariable_noDuplicates() {
        val eq = makeEquation()
        eq.ajouterVariable(VariableKind.BW)
        eq.ajouterVariable(VariableKind.BW)
        assertEquals(1, eq.variables.size)
    }

    @Test
    fun ajouterVariable_multipleDistinct_allAdded() {
        val eq = makeEquation()
        eq.ajouterVariable(VariableKind.BW)
        eq.ajouterVariable(VariableKind.MW)
        eq.ajouterVariable(VariableKind.BEE)
        assertEquals(3, eq.variables.size)
    }

    // ── supprimerToutesVariables ───────────────────────────────────────────────

    @Test
    fun supprimerToutesVariables_clearsAll() {
        val eq = makeEquation()
        eq.ajouterVariable(VariableKind.BW)
        eq.ajouterVariable(VariableKind.MW)
        eq.supprimerToutesVariables()
        assertTrue(eq.variables.isEmpty())
    }

    @Test
    fun supprimerToutesVariables_emptyList_doesNotThrow() {
        val eq = makeEquation()
        eq.supprimerToutesVariables()
        assertTrue(eq.variables.isEmpty())
    }

    // ── mettreAJour ───────────────────────────────────────────────────────────

    @Test
    fun mettreAJour_updatesDescription() {
        val eq = makeEquation()
        eq.mettreAJour(eq.copy(description = "Updated description"))
        assertEquals("Updated description", eq.description)
    }

    @Test
    fun mettreAJour_updatesScript() {
        val eq = makeEquation()
        eq.mettreAJour(eq.copy(equationScript = "BW ^ 0.75"))
        assertEquals("BW ^ 0.75", eq.equationScript)
    }

    @Test
    fun mettreAJour_updatesKind() {
        val eq = makeEquation(kind = EquationKind.ENERGYNEED)
        eq.mettreAJour(eq.copy(kind = EquationKind.MW))
        assertEquals(EquationKind.MW, eq.kind)
    }

    @Test
    fun mettreAJour_updatesVariables() {
        val eq = makeEquation()
        eq.ajouterVariable(VariableKind.BW)
        val source = makeEquation().also { it.ajouterVariable(VariableKind.MW) }
        eq.mettreAJour(source)
        assertEquals(1, eq.variables.size)
        assertTrue(eq.variables.contains(VariableKind.MW))
        assertFalse(eq.variables.contains(VariableKind.BW))
    }

    @Test
    fun mettreAJour_updatesRatioFlag() {
        val eq = makeEquation()
        assertFalse(eq.ratio)
        eq.mettreAJour(eq.copy(ratio = true))
        assertTrue(eq.ratio)
    }

    @Test
    fun mettreAJour_updatesConsistentFlag() {
        val eq = makeEquation()
        assertTrue(eq.consistent)
        eq.mettreAJour(eq.copy(consistent = false))
        assertFalse(eq.consistent)
    }

    @Test
    fun mettreAJour_updatesSpecie() {
        val eq = makeEquation()
        eq.mettreAJour(eq.copy(specie = Espece.CHAT))
        assertEquals(Espece.CHAT, eq.specie)
    }

    // ── default values ────────────────────────────────────────────────────────

    @Test
    fun defaultEquation_consistent_isTrue() {
        assertTrue(makeEquation().consistent)
    }

    @Test
    fun defaultEquation_specie_isChien() {
        assertEquals(Espece.CHIEN, makeEquation().specie)
    }

    @Test
    fun defaultEquation_correctionFactor_isOne() {
        assertEquals(1.0, makeEquation().correctionFactor)
    }

    @Test
    fun defaultEquation_ratio_isFalse() {
        assertFalse(makeEquation().ratio)
    }

    @Test
    fun defaultEquation_variables_isEmpty() {
        assertTrue(makeEquation().variables.isEmpty())
    }
}
