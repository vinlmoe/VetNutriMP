package fr.vetbrain.vetnutri_mp.Data

import kotlin.test.*

class RationCopyTest {
    @Test
    fun copiedRationBelongsToDestinationAndEditsPreserveSource() {
        val source = Ration(
            uuid = "source", idConsult = "previous", name = "Menu", actual = true,
            coef = 0.5, description = "Deux repas", etapeEvolutive = true,
            refRationParente = "parent", poids = 12.0,
            alimentMutableList = mutableListOf(
                AlimentRation(uuid = "food", refRation = "source", uuidUnif = "reference", quantite = 125.0)
            )
        )
        val copy = source.copyToConsultation("current", 3)
        assertNotEquals(source.uuid, copy.uuid)
        assertEquals("current", copy.idConsult)
        assertEquals(3, copy.number)
        assertFalse(copy.actual)
        assertFalse(copy.etapeEvolutive)
        assertNull(copy.refRationParente)
        assertNull(copy.poids)
        assertEquals(source.name, copy.name)
        assertEquals(source.coef, copy.coef)
        assertEquals(source.description, copy.description)
        assertNotEquals(source.alimentMutableList.single().uuid, copy.alimentMutableList.single().uuid)
        assertEquals(copy.uuid, copy.alimentMutableList.single().refRation)
        assertEquals("reference", copy.alimentMutableList.single().uuidUnif)
        assertEquals(125.0, copy.alimentMutableList.single().quantite)
        copy.alimentMutableList[0] = copy.alimentMutableList[0].copy(quantite = 80.0)
        copy.alimentMutableList.clear()
        assertEquals(125.0, source.alimentMutableList.single().quantite)
        assertEquals("source", source.alimentMutableList.single().refRation)
    }
}
