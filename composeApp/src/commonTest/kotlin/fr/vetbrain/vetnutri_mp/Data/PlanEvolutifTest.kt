package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/** Plan évolutif rangé sous une ration existante (ration parente + étapes). */
class PlanEvolutifTest {

    private fun aliment(ref: String, quantite: Double) =
            AlimentRation(uuid = "a-$ref-$quantite", refAlimUnif = ref, quantite = quantite)

    private fun parent(uuid: String, vararg aliments: AlimentRation) =
            Ration(uuid = uuid, idConsult = "c", name = "Ration $uuid", alimentMutableList = aliments.toMutableList())

    private fun etape(uuid: String, parent: String, poids: Double?, vararg aliments: AlimentRation) =
            Ration(
                    uuid = uuid,
                    idConsult = "c",
                    etapeEvolutive = true,
                    poids = poids,
                    refRationParente = parent,
                    alimentMutableList = aliments.toMutableList()
            )

    private fun consultation(vararg rations: Ration) =
            ConsultationEv(uuid = "c", weight = 5.0, rations = rations.toMutableList())

    @Test
    fun planTrie_parentAtRealWeight_stepsByWeight_otherPlanIgnored() {
        val c =
                consultation(
                        parent("p"),
                        etape("e12", "p", 12.0),
                        etape("e3", "p", 3.0),
                        parent("autre"),
                        etape("x8", "autre", 8.0)
                )
        val p = c.rations.first { it.uuid == "p" }
        assertEquals(listOf("e3", "p", "e12"), PlanEvolutif.planTrie(c, p).map { it.uuid })
        assertEquals(listOf("p", "autre"), PlanEvolutif.rationsPrincipales(c).map { it.uuid })
        assertTrue(PlanEvolutif.aUnPlan(c, p))
        assertFalse(PlanEvolutif.aUnPlan(c, c.rations.first { it.uuid == "e3" }))
        assertEquals(p, PlanEvolutif.parentDe(c, c.rations.first { it.uuid == "e12" }))
    }

    @Test
    fun sameWeight_sortedAndLabelledByDistinctiveVariable() {
        val d20 = etape("d20", "p", null).apply { suppVarp = mutableListOf(SupplementalvariableP(VariableKind.D, 20.0)) }
        val d5 = etape("d5", "p", null).apply { suppVarp = mutableListOf(SupplementalvariableP(VariableKind.D, 5.0)) }
        val p = parent("p")
        val c = consultation(p, d20, d5)

        assertEquals(listOf(VariableKind.D), PlanEvolutif.variablesDistinctives(c, p))
        // La ration parente n'a pas de D : elle vient en premier (0), puis D 5, puis D 20
        assertEquals(listOf("p", "d5", "d20"), PlanEvolutif.planTrie(c, p).map { it.uuid })
        assertEquals("5.0 kg · D 5.0", PlanEvolutif.libelleEtape(c, d5))
    }

    @Test
    fun nouvelleEtape_copiesParentFoods_withFixedNameAndParentLink() {
        val p = parent("p", aliment("croq", 120.0))
        val vars = listOf(SupplementalvariableP(VariableKind.AdultWeight, 30.0))

        val e = PlanEvolutif.nouvelleEtape(p, 8.0, vars)

        assertEquals("p", e.refRationParente)
        assertTrue(e.etapeEvolutive)
        assertFalse(e.actual)
        assertEquals(8.0, e.poids)
        assertEquals(vars, e.suppVarp)
        assertEquals(PlanEvolutif.nomAutomatique(8.0, vars), e.name)
        assertEquals(listOf(120.0), e.alimentMutableList.map { it.quantite })
        assertEquals(e.uuid, e.alimentMutableList.single().refRation)
        assertNotEquals(p.alimentMutableList.single().uuid, e.alimentMutableList.single().uuid)
    }

    @Test
    fun estActuelle_stepInheritsTheColorStatusOfItsParent() {
        val parentActuelle = parent("p").apply { actual = true }
        val etapeProposee = etape("e", "p", 3.0).apply { actual = false }
        val rationProposee = parent("autre")
        val c = consultation(parentActuelle, etapeProposee, rationProposee)

        assertTrue(PlanEvolutif.estActuelle(c, etapeProposee))
        assertTrue(PlanEvolutif.estActuelle(c, parentActuelle))
        assertFalse(PlanEvolutif.estActuelle(c, rationProposee))
    }

    @Test
    fun propagerAliments_withinThePlanOnly_parentIncluded() {
        val p = parent("p", aliment("croq", 100.0))
        val source = etape("s", "p", 3.0, aliment("croq", 80.0), aliment("huile", 5.0))
        val autre = etape("o", "p", 8.0, aliment("croq", 150.0))
        val horsPlan = parent("x", aliment("croq", 50.0))
        val c = consultation(p, source, autre, horsPlan)

        val (rations, ajouts) = PlanEvolutif.propagerAliments(c, source)

        assertEquals(2, ajouts)
        fun quantites(id: String) = rations.first { it.uuid == id }.alimentMutableList.associate { it.refAlimUnif to it.quantite }
        assertEquals(mapOf<String?, Double>("croq" to 150.0, "huile" to 0.0), quantites("o"))
        assertEquals(mapOf<String?, Double>("croq" to 100.0, "huile" to 0.0), quantites("p"))
        assertEquals(mapOf<String?, Double>("croq" to 50.0), quantites("x"))
    }

    @Test
    fun matriceSynthese_ingredientsByPlanColumn_inSortedOrder() {
        val p = parent("p", aliment("croq", 100.0))
        val c = consultation(p, etape("e8", "p", 8.0, aliment("croq", 150.0)), etape("e3", "p", 3.0, aliment("croq", 80.0), aliment("huile", 5.0)))

        val lignes = PlanEvolutif.matriceSynthese(c, p)

        // Colonnes : 3 kg, parent (5 kg), 8 kg
        assertEquals(listOf("croq", "huile"), lignes.map { it.refAlimUnif })
        assertEquals(listOf(80.0, 100.0, 150.0), lignes[0].quantites)
        assertEquals(listOf(5.0, null, null), lignes[1].quantites)
    }

    @Test
    fun avecQuantite_updatesOnlyTheTargetedFood() {
        val e = etape("e", "p", 3.0, aliment("croq", 80.0), aliment("huile", 5.0))
        val maj = PlanEvolutif.avecQuantite(e, "huile", 7.5)
        assertEquals(listOf(80.0, 7.5), maj.alimentMutableList.map { it.quantite })
        assertEquals(listOf(80.0, 5.0), e.alimentMutableList.map { it.quantite })
    }

    @Test
    fun variableDansToutesLesEtapes() {
        val avec = etape("a", "p", 3.0).apply { suppVarp = mutableListOf(SupplementalvariableP(VariableKind.D, 1.0)) }
        val sans = etape("b", "p", 4.0)
        assertTrue(PlanEvolutif.variableDansToutesLesEtapes(consultation(parent("p"), avec), VariableKind.D))
        assertFalse(PlanEvolutif.variableDansToutesLesEtapes(consultation(parent("p"), avec, sans), VariableKind.D))
        assertFalse(PlanEvolutif.variableDansToutesLesEtapes(consultation(parent("p")), VariableKind.D))
    }
}
