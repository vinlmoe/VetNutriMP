package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CalculMetaboliqueTest {

    private fun assertNear(expected: Double, actual: Double?, tol: Double = 0.001) {
        assertNotNull(actual, "Valeur attendue $expected mais null")
        assertTrue(abs(expected - actual) <= tol, "Attendu $expected ± $tol mais $actual")
    }

    private fun reference(bw: String? = "BW ^ 0.75", bee: String? = "130 * BW ^ 0.75"): ReferenceEv =
            ReferenceEv(nom = "Test", espece = Espece.CHIEN).apply {
                equationBW = bw?.let { Equation(equationScript = it, kind = EquationKind.MW) }
                equationBEE = bee?.let { Equation(equationScript = it, kind = EquationKind.ENERGYNEED) }
            }

    @Test
    fun poidsMetabolique_exposantParDefaut() {
        // 10^0.75 ≈ 5.6234
        assertNear(5.6234, CalculMetabolique.poidsMetabolique(10.0, reference()))
        assertEquals(0.75, CalculMetabolique.EXPOSANT_PAR_DEFAUT)
    }

    @Test
    fun poidsMetabolique_equationBWPersonnalisee() {
        // Chat : BW^0.67 → 4^0.67 ≈ 2.5321
        val ref = reference(bw = "BW ^ 0.67")
        assertNear(4.0.pow(0.67), CalculMetabolique.poidsMetabolique(4.0, ref))
        assertNear(4.0.pow(0.67), CalculMetabolique.poidsMetabolique(4.0, ref.equationBW))
    }

    @Test
    fun poidsMetabolique_sansEquation_null() {
        assertNull(CalculMetabolique.poidsMetabolique(10.0, reference(bw = null)))
        assertNull(CalculMetabolique.poidsMetabolique(10.0, null as Equation?))
        // Script vide : équivalent à une équation absente
        assertNull(CalculMetabolique.poidsMetabolique(10.0, Equation(equationScript = "")))
    }

    @Test
    fun poidsMetaboliqueOuDefaut_repliSurKleiber() {
        // Sans équation : BW^0.75
        assertNear(10.0.pow(0.75), CalculMetabolique.poidsMetaboliqueOuDefaut(10.0, null))
        // Avec équation : l'équation fait foi
        assertNear(10.0.pow(0.67), CalculMetabolique.poidsMetaboliqueOuDefaut(10.0, Equation(equationScript = "BW^0.67")))
    }

    @Test
    fun poidsMetaboliqueOuDefaut_poidsNul() {
        assertEquals(0.0, CalculMetabolique.poidsMetaboliqueOuDefaut(0.0, null))
        assertEquals(0.0, CalculMetabolique.poidsMetaboliqueOuDefaut(-3.0, Equation(equationScript = "BW ^ 0.75")))
        assertEquals(0.0, CalculMetabolique.poidsMetaboliqueOuDefaut(Double.NaN, null))
    }

    @Test
    fun besoinEnergetique_standardEtAvecK() {
        val ref = reference()
        // 130 × 16^0.75 = 130 × 8 = 1040 kcal
        assertNear(1040.0, CalculMetabolique.besoinEnergetiqueStandard(16.0, ref))
        assertNear(1040.0 * 1.6, CalculMetabolique.besoinEnergetiqueAjuste(16.0, ref.equationBEE, 1.6))
        assertNear(1040.0 * 0.8, CalculMetabolique.besoinEnergetiqueAjuste(16.0, ref.equationBEE, 0.8))
        assertNull(CalculMetabolique.besoinEnergetiqueAjuste(16.0, null, 1.2))
    }

    @Test
    fun besoinEnergetique_variablesSupplementairesEtDefauts() {
        // AW fourni par la consultation ; BCS absent → valeur par défaut 5
        val ref = reference(bee = "BW ^ 0.75 * AW + BCS")
        val vars = listOf(SupplementalvariableP(VariableKind.AdultWeight, 2.0))
        assertNear(16.0.pow(0.75) * 2.0 + 5.0, CalculMetabolique.besoinEnergetiqueStandard(16.0, ref, vars))
    }

    @Test
    fun ajouterVariablesParDefaut_signaleLesVariablesInconnues() {
        val variables = mutableMapOf("BW" to 10.0)
        val manquantes = CalculMetabolique.ajouterVariablesParDefaut(variables, "BW * BCS * XYZ")
        assertEquals(5.0, variables["BCS"])
        assertEquals(listOf("XYZ"), manquantes)
    }

    @Test
    fun rationCalculator_utiliseLEquationBWDuReferentiel() {
        val calc = RationCalculator(4.0, 4.0, Espece.CHAT)
        calc.setEquationPM(Equation(equationScript = "BW ^ 0.67", kind = EquationKind.MW))
        calc.setEquationBEE(Equation(equationScript = "100 * BW ^ 0.67", kind = EquationKind.ENERGYNEED))
        calc.calculer(emptyList())
        assertNear(4.0.pow(0.67), calc.getPM())
        assertNear(100.0 * 4.0.pow(0.67), calc.getBEE())
    }

    @Test
    fun estimerPoidsIdeal() {
        assertNear(10.0, CalculMetabolique.estimerPoidsIdeal(10.0, 5))
        assertNear(10.0, CalculMetabolique.estimerPoidsIdeal(12.0, 7))
    }

    @Test
    fun diagnostiquerEquation_nullQuandEvaluable() {
        assertNull(CalculMetabolique.diagnostiquerEquation("BW", reference().equationBW, 10.0))
        assertTrue(CalculMetabolique.avertissementsReference(10.0, reference()).isEmpty())
    }

    @Test
    fun diagnostiquerEquation_equationAbsente() {
        val motif = CalculMetabolique.diagnostiquerEquation("BW", null, 10.0)
        assertNotNull(motif)
        assertTrue(motif.contains("absente"), motif)
        val vide = Equation(equationScript = "", kind = EquationKind.MW)
        assertTrue(CalculMetabolique.diagnostiquerEquation("BW", vide, 10.0)!!.contains("absente"))
    }

    @Test
    fun diagnostiquerEquation_variableSansValeur() {
        // iBW (poids idéal) n'a pas de valeur par défaut : il doit venir de la consultation
        val eq = Equation(equationScript = "iBW ^ 0.75", kind = EquationKind.MW)
        val motif = CalculMetabolique.diagnostiquerEquation("BW", eq, 10.0)
        assertNotNull(motif)
        assertTrue(motif.contains("iBW"), motif)
        // Une valeur fournie par la consultation lève l'avertissement
        val svp = listOf(SupplementalvariableP(VariableKind.iBW, 9.0))
        assertNull(CalculMetabolique.diagnostiquerEquation("BW", eq, 10.0, svp))
    }

    @Test
    fun avertissementsReference_listeLesDeuxEquations() {
        val ref = reference(bw = null, bee = "130 * iBW ^ 0.75")
        val avertissements = CalculMetabolique.avertissementsReference(10.0, ref)
        assertEquals(2, avertissements.size, avertissements.toString())
        assertTrue(avertissements[0].contains("BW") && avertissements[0].contains("poids métabolique"))
        assertTrue(avertissements[1].contains("iBW") && avertissements[1].contains("besoin énergétique"))
    }

    @Test
    fun rationCalculator_signaleLeRepliSurKleiber() {
        val sans = RationCalculator().apply { setPoids(10.0); calculer(emptyList()) }
        assertNotNull(sans.avertissementPoidsMetabolique)
        assertTrue(sans.avertissementPoidsMetabolique!!.contains("0.75"))
        val avec = RationCalculator().apply {
            setPoids(10.0)
            setEquationPM(reference(bw = "BW ^ 0.67").equationBW)
            calculer(emptyList())
        }
        assertNull(avec.avertissementPoidsMetabolique)
        assertNull(avec.clone().avertissementPoidsMetabolique)
    }
}
