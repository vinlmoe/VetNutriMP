package fr.vetbrain.vetnutri_mp.Data

import kotlin.test.Test
import kotlin.test.assertEquals

class AjouterOuCumulerAlimentTest {

    private fun aliment(uuid: String, ref: String?, quantite: Double) =
            AlimentRation(uuid = uuid, refAlimUnif = ref, quantite = quantite, refRation = "r1")

    @Test
    fun alimentNouveauAjouteEnFin() {
        val liste = listOf(aliment("a", "riz", 100.0))
        val resultat = ajouterOuCumulerAliment(liste, aliment("b", "poulet", 50.0))
        assertEquals(listOf("riz" to 100.0, "poulet" to 50.0), resultat.map { it.refAlimUnif to it.quantite })
    }

    @Test
    fun alimentDejaPresentQuantiteCumuleeSurLaLigneExistante() {
        val liste = listOf(aliment("a", "riz", 100.0), aliment("b", "poulet", 50.0))
        val resultat = ajouterOuCumulerAliment(liste, aliment("c", "riz", 100.0))
        assertEquals(2, resultat.size)
        assertEquals("a", resultat[0].uuid)
        assertEquals(200.0, resultat[0].quantite)
    }

    @Test
    fun sansReferenceAlimentToujoursAjoute() {
        val resultat = ajouterOuCumulerAliment(listOf(aliment("a", null, 10.0)), aliment("b", null, 5.0))
        assertEquals(2, resultat.size)
    }
}
