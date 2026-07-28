package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Repository.InMemoryEquationRepository
import kotlinx.coroutines.test.runTest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
class RationNutrientAnalyzerTest {

    private fun assertNear(expected: Double, actual: Double, tol: Double = 0.001) {
        assertTrue(abs(expected - actual) <= tol, "Expected $expected ± $tol but was $actual")
    }

    private fun referenceAvecEquationsEnergie(): ReferenceEv {
        val reference = ReferenceEv(espece = Espece.CHIEN)
        reference.equationDEcom = Equation(equationScript = "PROTEINE*2")
        reference.equationDEraw = Equation(equationScript = "PROTEINE*3")
        return reference
    }

    private fun rationAvecUnAliment(typeAliment: FoodKind?, proteine: Double, quantite: Double = 100.0): Ration {
        val aliment = AlimentEv(typeAliment = typeAliment)
        aliment.setNutrient(NutrientMain.PROTEINE, proteine)
        val alimentRation = AlimentRation(aliment = aliment, quantite = quantite, weight = 1.0)
        return Ration(alimentMutableList = mutableListOf(alimentRation))
    }

    // ── Fix 2 : ENERGIE cohérente entre AvecEquations et Selective ────────────

    @Test
    fun energie_selectiveEtAvecEquations_memeSelectionDEquation() = runTest {
        val ration = rationAvecUnAliment(FoodKind.COMPLET, proteine = 10.0)
        val reference = referenceAvecEquationsEnergie()
        val preferences = PreferencesEspece(espece = Espece.CHIEN.name)
        val equationRepository = InMemoryEquationRepository()

        val avecEquations = analyserValeursNutritionnellesRationAvecEquations(
                ration = ration,
                preferencesEspece = preferences,
                equationRepository = equationRepository,
                referenceEv = reference
        )
        val selective = analyserValeursNutritionnellesRationSelective(
                ration = ration,
                nutrimentsSelectionnes = listOf(NutrientMain.ENERGIE.label),
                preferencesEspece = preferences,
                equationRepository = equationRepository,
                referenceEv = reference
        )

        val energieAvecEquations = avecEquations[NutrientMain.ENERGIE.label]?.valeur ?: -1.0
        val energieSelective = selective[NutrientMain.ENERGIE.label]?.valeur ?: -1.0

        // DEcom (PROTEINE*2 = 20 pour 100g) et non DEraw (PROTEINE*3 = 30) : COMPLET.
        assertNear(20.0, energieAvecEquations)
        assertNear(energieAvecEquations, energieSelective)
    }

    @Test
    fun energie_selective_alimentBrut_utiliseDEraw() = runTest {
        val ration = rationAvecUnAliment(null, proteine = 10.0)
        val reference = referenceAvecEquationsEnergie()
        val preferences = PreferencesEspece(espece = Espece.CHIEN.name)
        val equationRepository = InMemoryEquationRepository()

        val selective = analyserValeursNutritionnellesRationSelective(
                ration = ration,
                nutrimentsSelectionnes = listOf(NutrientMain.ENERGIE.label),
                preferencesEspece = preferences,
                equationRepository = equationRepository,
                referenceEv = reference
        )

        assertNear(30.0, selective[NutrientMain.ENERGIE.label]?.valeur ?: -1.0)
    }

    // ── Fix 4 : estNutrimentAnalysisRatio ──────────────────────────────────────

    @Test
    fun estNutrimentAnalysisRatio_vraisRatios_returnsTrue() {
        listOf(
                NutrientAnalysis.NaK,
                NutrientAnalysis.PCa,
                NutrientAnalysis.o6o3,
                NutrientAnalysis.ZnCu,
                NutrientAnalysis.PhosphProt,
                NutrientAnalysis.nonOsPP
        ).forEach { nutrient ->
            assertTrue(estNutrimentAnalysisRatio(nutrient), "$nutrient devrait être classé ratio")
        }
    }

    @Test
    fun estNutrimentAnalysisRatio_valeursAbsolues_returnsFalse() {
        listOf(
                NutrientAnalysis.nonOsPhos,
                NutrientAnalysis.nonOsProt,
                NutrientAnalysis.MethCys,
                NutrientAnalysis.PhenTyr
        ).forEach { nutrient ->
            assertTrue(!estNutrimentAnalysisRatio(nutrient), "$nutrient ne devrait pas être classé ratio")
        }
    }

    @Test
    fun estNutrimentAnalysisRatio_nutrimentNonAnalysis_returnsFalse() {
        assertTrue(!estNutrimentAnalysisRatio(NutrientMain.PROTEINE))
    }

    @Test
    fun analyserValeursAvecEquations_methCys_estSommePondereeNonNulle() = runTest {
        // METHCYS est une valeur absolue (g) : avant le fix, la classification "ratio" la faisait
        // toujours retourner 0.0 via le fallback calculerRatioGlobalRation. Après le fix, elle
        // doit passer par la sommation pondérée normale comme un nutriment classique.
        val aliment = AlimentEv(typeAliment = FoodKind.COMPLET)
        aliment.setNutrient(NutrientAnalysis.MethCys, 5.0)
        val alimentRation = AlimentRation(aliment = aliment, quantite = 200.0, weight = 1.0)
        val ration = Ration(alimentMutableList = mutableListOf(alimentRation))

        val preferences = PreferencesEspece(espece = Espece.CHIEN.name)
        val equationRepository = InMemoryEquationRepository()

        val resultat = analyserValeursNutritionnellesRationAvecEquations(
                ration = ration,
                preferencesEspece = preferences,
                equationRepository = equationRepository,
                referenceEv = null
        )

        // 5.0 g/100g * 200g / 100 = 10.0 g dans la ration
        assertNear(10.0, resultat[NutrientAnalysis.MethCys.label]?.valeur ?: -1.0)
    }

    @Test
    fun analyserValeursAvecEquations_cap_resteUnRatioGlobal() = runTest {
        // Témoin : CAP (Ca/P) est un vrai ratio, doit continuer à passer par
        // calculerRatioGlobalRation (pas de régression sur les 6 vrais ratios).
        val aliment = AlimentEv()
        aliment.setNutrient(NutrientMacro.CAL, 2.0)
        aliment.setNutrient(NutrientMacro.PHOS, 1.0)
        val alimentRation = AlimentRation(aliment = aliment, quantite = 100.0, weight = 1.0)
        val ration = Ration(alimentMutableList = mutableListOf(alimentRation))

        val preferences = PreferencesEspece(espece = Espece.CHIEN.name)
        val equationRepository = InMemoryEquationRepository()

        val resultat = analyserValeursNutritionnellesRationAvecEquations(
                ration = ration,
                preferencesEspece = preferences,
                equationRepository = equationRepository,
                referenceEv = null
        )

        assertNear(2.0, resultat[NutrientAnalysis.PCa.label]?.valeur ?: -1.0)
    }
}
