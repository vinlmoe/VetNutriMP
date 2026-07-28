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

    // ── calculerContributionIngredient : preferencesEspece ────────────────────

    @Test
    fun calculerContributionIngredient_sansPreferencesEspece_valeurUniquementViaEquationPrefs_estZero() = runTest {
        // Avant le fix : preferences = null en dur → une valeur qui ne vient QUE d'une équation
        // complémentaire des préférences d'espèce donne une contribution nulle.
        val aliment = alimentAvecProteine()
        val alimentRation = AlimentRation(aliment = aliment, quantite = 200.0, weight = 1.0)
        val equationRepository = InMemoryEquationRepository()
        equationRepository.saveEquation(Equation(uuid = "eq-mg", equationScript = "PROTEINE*0.1"))
        val preferences = PreferencesEspece(espece = Espece.CHIEN.name)
                .setEquationComplementaire(NutrientMacro.MG.label, "eq-mg")

        val contributionSansPrefs = calculerContributionIngredient(
                alimentRation, NutrientMacro.MG, reference = null, equationRepository = equationRepository
        )
        assertNear(0.0, contributionSansPrefs)
    }

    @Test
    fun calculerContributionIngredient_avecPreferencesEspece_utiliseEquationComplementaire() = runTest {
        val aliment = alimentAvecProteine()
        val alimentRation = AlimentRation(aliment = aliment, quantite = 200.0, weight = 1.0)
        val equationRepository = InMemoryEquationRepository()
        equationRepository.saveEquation(Equation(uuid = "eq-mg", equationScript = "PROTEINE*0.1"))
        val preferences = PreferencesEspece(espece = Espece.CHIEN.name)
                .setEquationComplementaire(NutrientMacro.MG.label, "eq-mg")

        val contribution = calculerContributionIngredient(
                alimentRation,
                NutrientMacro.MG,
                reference = null,
                equationRepository = equationRepository,
                preferencesEspece = preferences
        )
        // valeurPour100g = PROTEINE(20) * 0.1 = 2.0 ; contribution = 2.0 * 200 / 100 = 4.0
        assertNear(4.0, contribution)
    }

    @Test
    fun calculerContributionsIngredients_sommeEgaleApportTotal() = runTest {
        val equationRepository = InMemoryEquationRepository()
        equationRepository.saveEquation(Equation(uuid = "eq-mg", equationScript = "PROTEINE*0.1"))
        val preferences = PreferencesEspece(espece = Espece.CHIEN.name)
                .setEquationComplementaire(NutrientMacro.MG.label, "eq-mg")

        val ration = Ration(alimentMutableList = mutableListOf(
                AlimentRation(aliment = alimentAvecProteine(20.0), quantite = 200.0, weight = 1.0),
                AlimentRation(aliment = alimentAvecProteine(30.0), quantite = 100.0, weight = 1.0)
        ))

        val apportTotal = analyserValeursNutritionnellesRationAvecEquations(
                ration = ration,
                preferencesEspece = preferences,
                equationRepository = equationRepository,
                referenceEv = null
        )[NutrientMacro.MG.label]?.valeur ?: -1.0

        val sommeContributions = calculerContributionsIngredients(
                ration, NutrientMacro.MG, reference = null, equationRepository = equationRepository, preferencesEspece = preferences
        ).sumOf { it.contribution }

        assertTrue(apportTotal > 0.0, "L'apport total devrait être non nul")
        assertNear(apportTotal, sommeContributions)
    }

    // ── Cas particulier ENA : préférences ignorées si une ReferenceEv est fournie ─

    @Test
    fun calculerContributionIngredient_ena_sansReferenceEv_utiliseEquationPreferences() = runTest {
        val aliment = alimentAvecProteine(20.0)
        val alimentRation = AlimentRation(aliment = aliment, quantite = 100.0, weight = 1.0)
        val equationRepository = InMemoryEquationRepository()
        equationRepository.saveEquation(Equation(uuid = "eq-ena-pref", equationScript = "PROTEINE*5"))
        val preferences = PreferencesEspece(espece = Espece.CHIEN.name)
                .setEquationComplementaire(NutrientMain.ENA.label, "eq-ena-pref")

        val contribution = calculerContributionIngredient(
                alimentRation,
                NutrientMain.ENA,
                reference = null,
                equationRepository = equationRepository,
                preferencesEspece = preferences
        )
        // PROTEINE(20) * 5 = 100 pour 100g ; quantite = 100 -> contribution = 100
        assertNear(100.0, contribution)
    }

    @Test
    fun calculerContributionIngredient_ena_avecReferenceEv_ignorePreferencesUtiliseEquationReference() = runTest {
        val aliment = alimentAvecProteine(20.0)
        val alimentRation = AlimentRation(aliment = aliment, quantite = 100.0, weight = 1.0)

        val equationRepository = InMemoryEquationRepository()
        equationRepository.saveEquation(Equation(uuid = "eq-ena-pref", equationScript = "PROTEINE*5"))
        equationRepository.saveEquation(
                Equation(
                        uuid = "eq-ena-ref",
                        equationScript = "PROTEINE*2",
                        kind = EquationKind.COMPLEMENTARY_NUTRIENT,
                        nutrient = NutrientMain.ENA,
                        ratio = false,
                        specie = Espece.CHIEN
                )
        )
        val preferences = PreferencesEspece(espece = Espece.CHIEN.name)
                .setEquationComplementaire(NutrientMain.ENA.label, "eq-ena-pref")

        val reference = ReferenceEv(espece = Espece.CHIEN)
        reference.equationsNut = mutableListOf(equationRepository.getEquationById("eq-ena-ref")!!)

        val contribution = calculerContributionIngredient(
                alimentRation,
                NutrientMain.ENA,
                reference = reference,
                equationRepository = equationRepository,
                preferencesEspece = preferences
        )
        // Doit utiliser l'équation de la ReferenceEv (PROTEINE*2 = 40), pas celle des préférences
        // (PROTEINE*5 = 100).
        assertNear(40.0, contribution)
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
