package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.TypeConsultation
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlanEvolutifTest {

    private fun aliment(ref: String, quantite: Double) =
            AlimentRation(uuid = "a-$ref-$quantite", refAlimUnif = ref, quantite = quantite)

    private fun etape(uuid: String, poids: Double?, vararg aliments: AlimentRation) =
            Ration(
                    uuid = uuid,
                    idConsult = "c",
                    etapeEvolutive = true,
                    poids = poids,
                    alimentMutableList = aliments.toMutableList()
            )

    private fun plan(vararg rations: Ration) =
            ConsultationEv(
                    uuid = "c",
                    weight = 5.0,
                    typeConsultation = TypeConsultation.EVOLUTIVE,
                    rations = rations.toMutableList()
            )

    @Test
    fun etapesTriees_byWeight_realWeightPlacedAtItsValue() {
        val c =
                plan(
                        etape("e12", 12.0),
                        etape("reel", null),
                        etape("e3", 3.0),
                        Ration(uuid = "std", idConsult = "c")
                )
        assertEquals(listOf("e3", "reel", "e12"), PlanEvolutif.etapesTriees(c).map { it.uuid })
    }

    @Test
    fun etapesTriees_sameWeight_sortedByDistinctiveVariable() {
        val d20 = etape("d20", null).apply { suppVarp = mutableListOf(SupplementalvariableP(VariableKind.D, 20.0)) }
        val d5 = etape("d5", null).apply { suppVarp = mutableListOf(SupplementalvariableP(VariableKind.D, 5.0)) }
        val c = plan(d20, d5)

        assertEquals(listOf(VariableKind.D), PlanEvolutif.variablesDistinctives(c))
        assertEquals(listOf("d5", "d20"), PlanEvolutif.etapesTriees(c).map { it.uuid })
        assertEquals("5.0 kg · D 5.0", PlanEvolutif.libelleEtape(c, d5))
    }

    @Test
    fun peutSupprimer_protectsLastRealWeightStep() {
        val reel = etape("reel", null)
        val e8 = etape("e8", 8.0)
        val c = plan(reel, e8)
        assertFalse(PlanEvolutif.peutSupprimer(c, reel))
        assertTrue(PlanEvolutif.peutSupprimer(c, e8))
        assertTrue(PlanEvolutif.peutSupprimer(plan(reel, etape("reel2", null)), reel))
    }

    @Test
    fun creerPlan_copiesStartingRationAtRealWeight_withNewIds() {
        val depart = Ration(uuid = "dep", idConsult = "c", name = "Actuelle", actual = true,
                alimentMutableList = mutableListOf(aliment("croq", 120.0)))
        val c = plan().apply { rations.add(depart) }

        val premiere = PlanEvolutif.creerPlan(c, depart)

        assertTrue(premiere.etapeEvolutive)
        assertNull(premiere.poids)
        assertFalse(premiere.actual)
        assertNotEquals("dep", premiere.uuid)
        assertEquals(listOf(120.0), premiere.alimentMutableList.map { it.quantite })
        assertEquals(premiere.uuid, premiere.alimentMutableList.single().refRation)
        assertNotEquals(depart.alimentMutableList.single().uuid, premiere.alimentMutableList.single().uuid)
    }

    @Test
    fun propagerAliments_addsMissingFoodsAtZero() {
        val source = etape("s", 3.0, aliment("croq", 80.0), aliment("huile", 5.0))
        val autre = etape("o", 8.0, aliment("croq", 150.0))
        val c = plan(source, autre)

        val (rations, ajouts) = PlanEvolutif.propagerAliments(c, source)

        assertEquals(1, ajouts)
        val maj = rations.first { it.uuid == "o" }
        assertEquals(mapOf<String?, Double>("croq" to 150.0, "huile" to 0.0), maj.alimentMutableList.associate { it.refAlimUnif to it.quantite })
        assertEquals(source, rations.first { it.uuid == "s" })
    }

    @Test
    fun matriceSynthese_ingredientsByStep_inSortedOrder() {
        val c = plan(etape("e8", 8.0, aliment("croq", 150.0)), etape("e3", 3.0, aliment("croq", 80.0), aliment("huile", 5.0)))

        val lignes = PlanEvolutif.matriceSynthese(c)

        assertEquals(listOf("croq", "huile"), lignes.map { it.refAlimUnif })
        assertEquals(listOf(80.0, 150.0), lignes[0].quantites)
        assertEquals(listOf(5.0, null), lignes[1].quantites)
    }

    @Test
    fun avecQuantite_updatesOnlyTheTargetedFood() {
        val e = etape("e", 3.0, aliment("croq", 80.0), aliment("huile", 5.0))
        val maj = PlanEvolutif.avecQuantite(e, "huile", 7.5)
        assertEquals(listOf(80.0, 7.5), maj.alimentMutableList.map { it.quantite })
        assertEquals(listOf(80.0, 5.0), e.alimentMutableList.map { it.quantite })
    }
}
