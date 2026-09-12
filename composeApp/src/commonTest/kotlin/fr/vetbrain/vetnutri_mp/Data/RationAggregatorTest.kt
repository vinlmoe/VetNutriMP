package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.RationAnalysisScope
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RationAggregatorTest {

    private fun assertNear(expected: Double, actual: Double, tolerance: Double = 0.0001) {
        assertTrue(
            abs(expected - actual) <= tolerance,
            "Expected $expected +/- $tolerance but was $actual"
        )
    }

    private fun aliment(
        foodUuid: String,
        nom: String,
        quantite: Double,
        nutrients: Map<Nutrient, Double> = emptyMap()
    ): AlimentRation {
        val food = AlimentEv(uuid = foodUuid, nom = nom)
        nutrients.forEach { (nutrient, value) -> food.setNutrient(nutrient, value) }
        return AlimentRation(
            uuidUnif = foodUuid,
            quantite = quantite,
            aliment = food
        )
    }

    private fun consultation(vararg rations: Ration): ConsultationEv =
        ConsultationEv(uuid = "consult-1", rations = rations.toMutableList())

    @Test
    fun agregerCumuleLesQuantitesPondereesParLeCoefficient() {
        val r1 = Ration(
            uuid = "r1",
            name = "Matin",
            coef = 1.0,
            actual = true,
            alimentMutableList = mutableListOf(aliment("f1", "Croquettes", 100.0))
        )
        val r2 = Ration(
            uuid = "r2",
            name = "Soir",
            coef = 2.0,
            actual = true,
            alimentMutableList = mutableListOf(aliment("f2", "Pâtée", 50.0))
        )

        val agregat =
            RationAggregator.agreger(
                consultation(r1, r2),
                RationAnalysisScope.GROUPE_ACTUELLES
            )

        assertNotNull(agregat)
        assertEquals(RationAggregator.UUID_GROUPE_ACTUELLES, agregat.uuid)
        assertTrue(agregat.actual)
        assertEquals(2, agregat.alimentMutableList.size)
        // 100 * 1 + 50 * 2
        assertNear(200.0, agregat.getQuantiteTotale())
    }

    @Test
    fun agregerFusionneUnMemeAlimentPresentDansPlusieursRations() {
        val r1 = Ration(
            uuid = "r1",
            coef = 1.0,
            actual = false,
            alimentMutableList = mutableListOf(aliment("f1", "Croquettes", 100.0))
        )
        val r2 = Ration(
            uuid = "r2",
            coef = 0.5,
            actual = false,
            alimentMutableList = mutableListOf(aliment("f1", "Croquettes", 200.0))
        )

        val agregat =
            RationAggregator.agreger(
                consultation(r1, r2),
                RationAnalysisScope.GROUPE_PROPOSEES
            )

        assertNotNull(agregat)
        assertEquals(1, agregat.alimentMutableList.size)
        // 100 * 1 + 200 * 0.5
        assertNear(200.0, agregat.alimentMutableList.first().quantite)
        assertNear(100.0, agregat.alimentMutableList.first().proportion)
    }

    @Test
    fun agregerSommeLesApportsNutritionnelsPonderes() {
        val nutriments = mapOf<Nutrient, Double>(NutrientMain.PROTEINE to 30.0)
        val r1 = Ration(
            uuid = "r1",
            coef = 1.0,
            actual = true,
            alimentMutableList = mutableListOf(aliment("f1", "A", 100.0, nutriments))
        )
        val r2 = Ration(
            uuid = "r2",
            coef = 3.0,
            actual = true,
            alimentMutableList = mutableListOf(aliment("f2", "B", 100.0, nutriments))
        )

        val agregat =
            RationAggregator.agreger(
                consultation(r1, r2),
                RationAnalysisScope.GROUPE_ACTUELLES
            )

        assertNotNull(agregat)
        // 30 g/100 g sur 100 g puis sur 300 g => 30 + 90
        assertNear(120.0, agregat.getNutrient(NutrientMain.PROTEINE) ?: 0.0)
    }

    @Test
    fun agregerNeRetientQueLesRationsDuGroupeDemande() {
        val actuelle = Ration(
            uuid = "r1",
            coef = 1.0,
            actual = true,
            alimentMutableList = mutableListOf(aliment("f1", "A", 100.0))
        )
        val proposee = Ration(
            uuid = "r2",
            coef = 1.0,
            actual = false,
            alimentMutableList = mutableListOf(aliment("f2", "B", 400.0))
        )
        val consult = consultation(actuelle, proposee)

        val groupeActuelles =
            RationAggregator.agreger(consult, RationAnalysisScope.GROUPE_ACTUELLES)
        val groupeProposees =
            RationAggregator.agreger(consult, RationAnalysisScope.GROUPE_PROPOSEES)

        assertNotNull(groupeActuelles)
        assertNotNull(groupeProposees)
        assertNear(100.0, groupeActuelles.getQuantiteTotale())
        assertNear(400.0, groupeProposees.getQuantiteTotale())
        assertEquals(RationAggregator.UUID_GROUPE_PROPOSEES, groupeProposees.uuid)
    }

    @Test
    fun coefficientNulExclutLaRationDuCumul() {
        val r1 = Ration(
            uuid = "r1",
            coef = 0.0,
            actual = true,
            alimentMutableList = mutableListOf(aliment("f1", "A", 100.0))
        )
        val r2 = Ration(
            uuid = "r2",
            coef = 1.0,
            actual = true,
            alimentMutableList = mutableListOf(aliment("f2", "B", 250.0))
        )

        val agregat =
            RationAggregator.agreger(
                consultation(r1, r2),
                RationAnalysisScope.GROUPE_ACTUELLES
            )

        assertNotNull(agregat)
        assertNear(250.0, agregat.getQuantiteTotale())
    }

    @Test
    fun coefficientInvalideRetombeSurUn() {
        val ration = Ration(uuid = "r1", coef = -2.0, actual = true)
        assertNear(1.0, RationAggregator.coefficientEffectif(ration))
        assertNear(1.5, RationAggregator.coefficientEffectif(ration.copy(coef = 1.5)))
    }

    @Test
    fun agregerRenvoieNullQuandLeGroupeEstVide() {
        val proposee = Ration(uuid = "r1", coef = 1.0, actual = false)
        assertNull(
            RationAggregator.agreger(
                consultation(proposee),
                RationAnalysisScope.GROUPE_ACTUELLES
            )
        )
        assertNull(RationAggregator.agreger(null, RationAnalysisScope.GROUPE_PROPOSEES))
    }

    @Test
    fun scopeRationUniqueNeProduitAucunGroupe() {
        val ration = Ration(uuid = "r1", coef = 1.0, actual = true)
        assertTrue(
            RationAggregator.rationsDuGroupe(
                consultation(ration),
                RationAnalysisScope.RATION_UNIQUE
            ).isEmpty()
        )
        assertNull(RationAggregator.uuidPour(RationAnalysisScope.RATION_UNIQUE))
    }

    @Test
    fun estRationGroupeeReconnaitLesUuidVirtuels() {
        assertTrue(RationAggregator.estRationGroupee(RationAggregator.UUID_GROUPE_ACTUELLES))
        assertTrue(RationAggregator.estRationGroupee(RationAggregator.UUID_GROUPE_PROPOSEES))
        assertTrue(!RationAggregator.estRationGroupee("r1"))
        assertTrue(!RationAggregator.estRationGroupee(null))
    }
}
