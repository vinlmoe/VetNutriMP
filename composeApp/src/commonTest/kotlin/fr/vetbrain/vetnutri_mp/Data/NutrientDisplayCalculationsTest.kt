package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Repository.InMemoryEquationRepository
import kotlinx.coroutines.test.runTest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class NutrientDisplayCalculationsTest {

    private fun assertNear(expected: Double, actual: Double, tol: Double = 0.001) {
        assertTrue(abs(expected - actual) <= tol, "Expected $expected ± $tol but was $actual")
    }

    private fun alimentAvecProteine(proteine: Double = 20.0) =
        AlimentEv().also { it.setNutrient(NutrientMain.PROTEINE, proteine) }

    // ── calculerContributionIngredient : seule ReferenceEv pilote les équations ─

    @Test
    fun calculerContributionIngredient_sansReferenceEv_aucuneEquationComplementaire_estZero() = runTest {
        // Sans ReferenceEv, aucune équation complémentaire n'est appliquée : un nutriment sans
        // valeur directe en table reste à 0, même si un repository d'équations est fourni.
        val aliment = alimentAvecProteine()
        val alimentRation = AlimentRation(aliment = aliment, quantite = 200.0, weight = 1.0)
        val equationRepository = InMemoryEquationRepository()
        equationRepository.saveEquation(Equation(uuid = "eq-mg", equationScript = "PROTEINE*0.1"))

        val contribution = calculerContributionIngredient(
                alimentRation, NutrientMacro.MG, reference = null, equationRepository = equationRepository
        )
        assertNear(0.0, contribution)
    }

    @Test
    fun calculerContributionIngredient_avecReferenceEv_utiliseEquationComplementaire() = runTest {
        val aliment = alimentAvecProteine()
        val alimentRation = AlimentRation(aliment = aliment, quantite = 200.0, weight = 1.0)
        val equationRepository = InMemoryEquationRepository()
        val equation = Equation(
                uuid = "eq-mg",
                equationScript = "PROTEINE*0.1",
                kind = EquationKind.COMPLEMENTARY_NUTRIENT,
                nutrient = NutrientMacro.MG,
                ratio = false,
                specie = Espece.CHIEN
        )
        equationRepository.saveEquation(equation)
        val reference = ReferenceEv(espece = Espece.CHIEN)
        reference.equationsNut = mutableListOf(equation)

        val contribution = calculerContributionIngredient(
                alimentRation,
                NutrientMacro.MG,
                reference = reference,
                equationRepository = equationRepository
        )
        // valeurPour100g = PROTEINE(20) * 0.1 = 2.0 ; contribution = 2.0 * 200 / 100 = 4.0
        assertNear(4.0, contribution)
    }

    @Test
    fun calculerContributionsIngredients_sommeEgaleApportTotal() = runTest {
        val equationRepository = InMemoryEquationRepository()
        val equation = Equation(
                uuid = "eq-mg",
                equationScript = "PROTEINE*0.1",
                kind = EquationKind.COMPLEMENTARY_NUTRIENT,
                nutrient = NutrientMacro.MG,
                ratio = false,
                specie = Espece.CHIEN
        )
        equationRepository.saveEquation(equation)
        val reference = ReferenceEv(espece = Espece.CHIEN)
        reference.equationsNut = mutableListOf(equation)

        val ration = Ration(alimentMutableList = mutableListOf(
                AlimentRation(aliment = alimentAvecProteine(20.0), quantite = 200.0, weight = 1.0),
                AlimentRation(aliment = alimentAvecProteine(30.0), quantite = 100.0, weight = 1.0)
        ))

        val apportTotal = analyserValeursNutritionnellesRationAvecEquations(
                ration = ration,
                equationRepository = equationRepository,
                referenceEv = reference
        )[NutrientMacro.MG.label]?.valeur ?: -1.0

        val sommeContributions = calculerContributionsIngredients(
                ration, NutrientMacro.MG, reference = reference, equationRepository = equationRepository
        ).sumOf { it.contribution }

        assertTrue(apportTotal > 0.0, "L'apport total devrait être non nul")
        assertNear(apportTotal, sommeContributions)
    }

    // ── estNutrimentAnalysisRatio : voir aussi RationNutrientAnalyzerTest ─────

    @Test
    fun calculerContributionsIngredients_vraiRatio_returnsEmptyList() = runTest {
        val ration = Ration(alimentMutableList = mutableListOf(
                AlimentRation(aliment = alimentAvecProteine(), quantite = 100.0, weight = 1.0)
        ))
        val contributions = calculerContributionsIngredients(
                ration,
                fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis.PCa,
                reference = null,
                equationRepository = null
        )
        assertTrue(contributions.isEmpty())
    }
}
