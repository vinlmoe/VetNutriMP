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
    fun agregerMoyenneLesQuantitesPondereesParLeCoefficient() {
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
        // Moyenne pondérée, pas somme : (100 * 1 + 50 * 2) / (1 + 2)
        assertNear(66.6667, agregat.getQuantiteTotale(), 0.001)
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
        // (100 * 1 + 200 * 0.5) / (1 + 0.5)
        assertNear(133.3333, agregat.alimentMutableList.first().quantite, 0.001)
        assertNear(100.0, agregat.alimentMutableList.first().proportion)
    }

    @Test
    fun agregerMoyenneLesApportsNutritionnelsPonderes() {
        // 100 g à 20 g/100 g => 20 g d'apport ; 100 g à 40 g/100 g => 40 g d'apport
        val r1 = Ration(
            uuid = "r1",
            coef = 1.0,
            actual = true,
            alimentMutableList = mutableListOf(
                aliment("f1", "A", 100.0, mapOf(NutrientMain.PROTEINE to 20.0))
            )
        )
        val r2 = Ration(
            uuid = "r2",
            coef = 3.0,
            actual = true,
            alimentMutableList = mutableListOf(
                aliment("f2", "B", 100.0, mapOf(NutrientMain.PROTEINE to 40.0))
            )
        )

        val agregat =
            RationAggregator.agreger(
                consultation(r1, r2),
                RationAnalysisScope.GROUPE_ACTUELLES
            )

        assertNotNull(agregat)
        // (20 * 1 + 40 * 3) / (1 + 3) : l'apport reste à l'échelle d'une ration journalière
        assertNear(35.0, agregat.getNutrient(NutrientMain.PROTEINE) ?: 0.0)
    }

    @Test
    fun rationsIdentiquesDonnentUnAgregatIdentiqueAUneSeuleRation() {
        // Deux rations équivalentes ne doivent pas doubler la couverture du besoin
        fun ration(uuid: String) = Ration(
            uuid = uuid,
            coef = 1.0,
            actual = true,
            alimentMutableList = mutableListOf(
                aliment("f-$uuid", "A", 300.0, mapOf(NutrientMain.PROTEINE to 25.0))
            )
        )

        val agregat =
            RationAggregator.agreger(
                consultation(ration("r1"), ration("r2")),
                RationAnalysisScope.GROUPE_ACTUELLES
            )

        assertNotNull(agregat)
        assertNear(300.0, agregat.getQuantiteTotale())
        assertNear(75.0, agregat.getNutrient(NutrientMain.PROTEINE) ?: 0.0)
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
    fun coefficientNonRenseigneEstTraiteCommeUnPoidsNeutre() {
        // Les rations importées de la V2 arrivent avec coef = 0 : poids neutre, pas d'exclusion
        val r1 = Ration(
            uuid = "r1",
            coef = 0.0,
            actual = true,
            alimentMutableList = mutableListOf(aliment("f1", "A", 100.0))
        )
        val r2 = Ration(
            uuid = "r2",
            coef = 0.0,
            actual = true,
            alimentMutableList = mutableListOf(aliment("f2", "B", 300.0))
        )

        val agregat =
            RationAggregator.agreger(
                consultation(r1, r2),
                RationAnalysisScope.GROUPE_ACTUELLES
            )

        assertNotNull(agregat)
        // Moyenne simple : (100 + 300) / 2
        assertNear(200.0, agregat.getQuantiteTotale())
    }

    @Test
    fun coefficientInvalideRetombeSurUn() {
        val ration = Ration(uuid = "r1", coef = -2.0, actual = true)
        assertNear(1.0, RationAggregator.coefficientEffectif(ration))
        assertNear(1.0, RationAggregator.coefficientEffectif(ration.copy(coef = 0.0)))
        assertNear(1.5, RationAggregator.coefficientEffectif(ration.copy(coef = 1.5)))
    }

    @Test
    fun laNormalisationNAffectePasLaCompositionRelative() {
        val r1 = Ration(
            uuid = "r1",
            coef = 1.0,
            actual = false,
            alimentMutableList = mutableListOf(
                aliment("f1", "A", 100.0, mapOf(NutrientMain.PROTEINE to 30.0))
            )
        )
        val r2 = Ration(
            uuid = "r2",
            coef = 4.0,
            actual = false,
            alimentMutableList = mutableListOf(
                aliment("f1", "A", 400.0, mapOf(NutrientMain.PROTEINE to 30.0))
            )
        )

        val agregat =
            RationAggregator.agreger(
                consultation(r1, r2),
                RationAnalysisScope.GROUPE_PROPOSEES
            )

        assertNotNull(agregat)
        val quantite = agregat.getQuantiteTotale()
        val proteine = agregat.getNutrient(NutrientMain.PROTEINE) ?: 0.0
        // Un seul aliment à 30 g/100 g : la teneur pour 100 g reste 30 quelle que soit la pondération
        assertNear(30.0, proteine / quantite * 100.0)
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
