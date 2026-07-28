package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class ReferenceEvTest {

    private val biblio = BiblioRef(firstAuthor = "Dupont", year = 2020, consistent = 1)

    private fun makeRef(espece: Espece = Espece.CHIEN): ReferenceEv =
        ReferenceEv(nom = "Ref test", espece = espece)

    // ── definirNutriment / contientNutriment / obtenirNutriment ───────────────

    @Test
    fun contientNutriment_beforeDefine_returnsFalse() {
        assertFalse(makeRef().contientNutriment(NutrientMain.PROTEINE, Reflevel.MIN))
    }

    @Test
    fun definirNutriment_thenContientNutriment_returnsTrue() {
        val ref = makeRef()
        ref.definirNutriment(20.0, NutrientMain.PROTEINE, Reflevel.MIN, UnitReqEnum.PERKG, biblio)
        assertTrue(ref.contientNutriment(NutrientMain.PROTEINE, Reflevel.MIN))
    }

    @Test
    fun obtenirNutriment_absent_returnsMinusOne() {
        assertEquals(-1.0, makeRef().obtenirNutriment(NutrientMain.PROTEINE, Reflevel.MIN))
    }

    @Test
    fun obtenirNutriment_afterDefine_returnsStoredValue() {
        val ref = makeRef()
        ref.definirNutriment(20.0, NutrientMain.PROTEINE, Reflevel.MIN, UnitReqEnum.PERKG, biblio)
        assertEquals(20.0, ref.obtenirNutriment(NutrientMain.PROTEINE, Reflevel.MIN))
    }

    @Test
    fun definirNutriment_differentLevels_storedIndependently() {
        val ref = makeRef()
        ref.definirNutriment(10.0, NutrientMain.PROTEINE, Reflevel.MIN, UnitReqEnum.PERKG, biblio)
        ref.definirNutriment(30.0, NutrientMain.PROTEINE, Reflevel.MAX, UnitReqEnum.PERKG, biblio)
        assertEquals(10.0, ref.obtenirNutriment(NutrientMain.PROTEINE, Reflevel.MIN))
        assertEquals(30.0, ref.obtenirNutriment(NutrientMain.PROTEINE, Reflevel.MAX))
    }

    @Test
    fun definirNutriment_differentNutrients_storedIndependently() {
        val ref = makeRef()
        ref.definirNutriment(20.0, NutrientMain.PROTEINE, Reflevel.MIN, UnitReqEnum.PERKG, biblio)
        ref.definirNutriment(1.5, NutrientMacro.CAL, Reflevel.MIN, UnitReqEnum.PERKG, biblio)
        assertEquals(20.0, ref.obtenirNutriment(NutrientMain.PROTEINE, Reflevel.MIN))
        assertEquals(1.5, ref.obtenirNutriment(NutrientMacro.CAL, Reflevel.MIN))
    }

    @Test
    fun definirNutriment_overwrites_previousValue() {
        val ref = makeRef()
        ref.definirNutriment(20.0, NutrientMain.PROTEINE, Reflevel.MIN, UnitReqEnum.PERKG, biblio)
        ref.definirNutriment(35.0, NutrientMain.PROTEINE, Reflevel.MIN, UnitReqEnum.PERKG, biblio)
        assertEquals(35.0, ref.obtenirNutriment(NutrientMain.PROTEINE, Reflevel.MIN))
    }

    // ── supprimerNutriment ────────────────────────────────────────────────────

    @Test
    fun supprimerNutriment_removesIt() {
        val ref = makeRef()
        ref.definirNutriment(20.0, NutrientMain.PROTEINE, Reflevel.MIN, UnitReqEnum.PERKG, biblio)
        ref.supprimerNutriment(NutrientMain.PROTEINE, Reflevel.MIN)
        assertFalse(ref.contientNutriment(NutrientMain.PROTEINE, Reflevel.MIN))
    }

    @Test
    fun supprimerNutriment_onlyAffectsTargetLevel() {
        val ref = makeRef()
        ref.definirNutriment(10.0, NutrientMain.PROTEINE, Reflevel.MIN, UnitReqEnum.PERKG, biblio)
        ref.definirNutriment(30.0, NutrientMain.PROTEINE, Reflevel.MAX, UnitReqEnum.PERKG, biblio)
        ref.supprimerNutriment(NutrientMain.PROTEINE, Reflevel.MIN)
        assertFalse(ref.contientNutriment(NutrientMain.PROTEINE, Reflevel.MIN))
        assertTrue(ref.contientNutriment(NutrientMain.PROTEINE, Reflevel.MAX))
    }

    // ── obtenirBiblioNutriment ────────────────────────────────────────────────

    @Test
    fun obtenirBiblioNutriment_defined_returnsBiblio() {
        val ref = makeRef()
        ref.definirNutriment(20.0, NutrientMain.PROTEINE, Reflevel.MIN, UnitReqEnum.PERKG, biblio)
        val result = ref.obtenirBiblioNutriment(NutrientMain.PROTEINE, Reflevel.MIN)
        assertEquals(biblio, result)
    }

    @Test
    fun obtenirBiblioNutriment_absent_returnsEmptyBiblio() {
        val result = makeRef().obtenirBiblioNutriment(NutrientMain.PROTEINE, Reflevel.MIN)
        assertEquals("", result.firstAuthor)
    }

    // ── obtenirNutrimentRef ───────────────────────────────────────────────────

    @Test
    fun obtenirNutrimentRef_defined_returnsNonNull() {
        val ref = makeRef()
        ref.definirNutriment(20.0, NutrientMain.PROTEINE, Reflevel.MIN, UnitReqEnum.PERKG, biblio)
        assertNotNull(ref.obtenirNutrimentRef(NutrientMain.PROTEINE, Reflevel.MIN))
    }

    @Test
    fun obtenirNutrimentRef_absent_returnsNull() {
        assertEquals(null, makeRef().obtenirNutrimentRef(NutrientMain.PROTEINE, Reflevel.MIN))
    }

    // ── getEquationCount / hasEquations ───────────────────────────────────────

    @Test
    fun hasEquations_noEquations_returnsFalse() {
        assertFalse(makeRef().hasEquations())
    }

    @Test
    fun hasEquations_withEquationBEE_returnsTrue() {
        val ref = makeRef()
        ref.equationBEE = Equation(name = "BEE eq", equationScript = "BW * 70")
        assertTrue(ref.hasEquations())
    }

    @Test
    fun getEquationCount_noEquations_returnsZero() {
        assertEquals(0, makeRef().getEquationCount())
    }

    @Test
    fun getEquationCount_withBEEAndOneNut_returnsTwo() {
        val ref = makeRef()
        ref.equationBEE = Equation(name = "BEE eq", equationScript = "BW * 70")
        ref.equationsNut.add(Equation(name = "Prot eq", equationScript = "BW * 2"))
        assertEquals(2, ref.getEquationCount())
    }

    @Test
    fun getEquationCount_cachedOnRepeatCall() {
        val ref = makeRef()
        ref.equationBEE = Equation(name = "BEE eq", equationScript = "BW * 70")
        val first = ref.getEquationCount()
        val second = ref.getEquationCount()
        assertEquals(first, second)
    }

    // ── obtenirToutesEquations ────────────────────────────────────────────────

    @Test
    fun obtenirToutesEquations_noEquations_returnsEmpty() {
        assertTrue(makeRef().obtenirToutesEquations().isEmpty())
    }

    @Test
    fun obtenirToutesEquations_blankNameEquation_excluded() {
        val ref = makeRef()
        ref.equationBEE = Equation(name = "", equationScript = "BW * 70")
        assertTrue(ref.obtenirToutesEquations().isEmpty())
    }

    @Test
    fun obtenirToutesEquations_namedEquations_allIncluded() {
        val ref = makeRef()
        ref.equationBEE = Equation(name = "BEE", equationScript = "BW * 70")
        ref.equationBW = Equation(name = "BW", equationScript = "BW ^ 0.75")
        ref.equationsNut.add(Equation(name = "Prot", equationScript = "BW * 2"))
        assertEquals(3, ref.obtenirToutesEquations().size)
    }

    // ── deduplicateCoefficients ───────────────────────────────────────────────

    @Test
    fun deduplicateCoefficients_noDuplicates_unchanged() {
        val ref = makeRef()
        ref.modk1 = mutableListOf(
            CoefP(description = "Normal", coef = 1.0, groupUUID = 0),
            CoefP(description = "Élevé", coef = 1.2, groupUUID = 0)
        )
        ref.deduplicateCoefficients()
        assertEquals(2, ref.modk1.size)
    }

    @Test
    fun deduplicateCoefficients_exactDuplicates_onlyOneKept() {
        val ref = makeRef()
        ref.modk1 = mutableListOf(
            CoefP(description = "Normal", coef = 1.0, groupUUID = 0),
            CoefP(description = "Normal", coef = 1.0, groupUUID = 0)
        )
        ref.deduplicateCoefficients()
        assertEquals(1, ref.modk1.size)
    }

    @Test
    fun deduplicateCoefficients_caseInsensitiveDuplicates_onlyOneKept() {
        val ref = makeRef()
        ref.modk1 = mutableListOf(
            CoefP(description = "normal", coef = 1.0, groupUUID = 0),
            CoefP(description = "NORMAL", coef = 1.0, groupUUID = 0)
        )
        ref.deduplicateCoefficients()
        assertEquals(1, ref.modk1.size)
    }

    @Test
    fun deduplicateCoefficients_sameDescDifferentCoef_bothKept() {
        val ref = makeRef()
        ref.modk1 = mutableListOf(
            CoefP(description = "Normal", coef = 1.0, groupUUID = 0),
            CoefP(description = "Normal", coef = 1.2, groupUUID = 0)
        )
        ref.deduplicateCoefficients()
        assertEquals(2, ref.modk1.size)
    }

    @Test
    fun deduplicateCoefficients_appliesAllKLists() {
        val ref = makeRef()
        ref.modk2 = mutableListOf(
            CoefP(description = "A", coef = 1.0, groupUUID = 1),
            CoefP(description = "A", coef = 1.0, groupUUID = 1)
        )
        ref.modk3 = mutableListOf(
            CoefP(description = "B", coef = 1.0, groupUUID = 2),
            CoefP(description = "B", coef = 1.0, groupUUID = 2)
        )
        ref.deduplicateCoefficients()
        assertEquals(1, ref.modk2.size)
        assertEquals(1, ref.modk3.size)
    }
}
