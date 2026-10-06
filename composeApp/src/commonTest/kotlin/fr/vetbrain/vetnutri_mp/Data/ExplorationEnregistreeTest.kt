package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExplorationEnregistreeTest {

    private val riz = AlimentEv(uuid = "riz", nom = "Riz")
    private val poulet = AlimentEv(uuid = "poulet", nom = "Poulet")

    private fun parametres() =
            ParametresExploration(
                    espece = Espece.CHAT,
                    referenceIds = setOf("ref-chat"),
                    poidsDe = "3",
                    poidsA = "6",
                    poidsPas = "1",
                    listes =
                            RoleExploration.entries.associateWith { emptyList<AlimentEv>() } +
                                    (RoleExploration.PROTEINES to listOf(poulet)) +
                                    (RoleExploration.ENERGIE to listOf(riz)),
                    cibles =
                            ParametresExploration().cibles +
                                    (RoleExploration.FIBRES to CibleExploration(NutrientMain.FIBRETOT, Reflevel.MIN, 2.0)),
                    doseMinimale = ParametresExploration().doseMinimale + (RoleExploration.CALCIUM to 12.0),
                    arrondir = false,
                    maxScenarios = "500"
            )

    @Test
    fun configuration_allerRetourJson_conserveLesParametres() {
        val p = parametres()
        val json = ConfigurationExplorationJson.depuisJson(p.versJson().versJson())
        val (relu, manquants) = json.versParametres(mapOf("riz" to riz, "poulet" to poulet), setOf("ref-chat"))
        assertTrue(manquants.isEmpty())
        assertEquals(p, relu)
    }

    @Test
    fun configuration_elementsDisparus_sontIgnoresEtSignales() {
        val (relu, manquants) = parametres().versJson().versParametres(mapOf("riz" to riz), emptySet())
        assertTrue(relu.referenceIds.isEmpty())
        assertTrue(relu.listes.getValue(RoleExploration.PROTEINES).isEmpty())
        assertEquals(listOf(riz), relu.listes.getValue(RoleExploration.ENERGIE))
        assertEquals(2, manquants.size)
        assertEquals(Espece.CHAT, relu.espece)
    }

    @Test
    fun configuration_jsonIncomplet_prendLesValeursParDefaut() {
        val json = ConfigurationExplorationJson.depuisJson("""{"espece":"CHAT","champInconnu":1}""")
        val (relu, _) = json.versParametres(emptyMap(), emptySet())
        assertEquals(ParametresExploration(espece = Espece.CHAT), relu)
    }
}
