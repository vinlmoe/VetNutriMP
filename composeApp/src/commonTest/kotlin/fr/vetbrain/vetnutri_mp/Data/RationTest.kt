package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RationTest {

    @Test
    fun testRationConstructor() {
        val ration = Ration()

        assertNotNull(ration.uuid)
        assertEquals(null, ration.idConsult)
        assertEquals("", ration.name)
        assertEquals(1.0f, ration.coef)
        assertEquals(false, ration.actual)
        assertEquals(1, ration.number)
        assertEquals(null, ration.espece)
        assertEquals(false, ration.recette)
        assertEquals("", ration.description)
    }

    @Test
    fun testGetEspece() {
        val ration = Ration()
        ration.espece = "CHIEN"

        val espece = ration.getEspece()
        assertEquals(Espece.CHIEN, espece)
    }

    @Test
    fun testSetEspece() {
        val ration = Ration()
        ration.setEspece(Espece.CHAT)

        assertEquals("CHAT", ration.espece)
    }

    @Test
    fun testGetEspeceWithNullValue() {
        val ration = Ration()
        ration.espece = null

        val espece = ration.getEspece()
        assertEquals(Espece.CHIEN, espece) // Valeur par défaut
    }

    @Test
    fun testGetEspeceWithInvalidValue() {
        val ration = Ration()
        ration.espece = "INVALID"

        val espece = ration.getEspece()
        assertEquals(Espece.CHIEN, espece) // Valeur par défaut
    }
}
