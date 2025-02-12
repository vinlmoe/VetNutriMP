package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.Ration
import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class RationTest {
    private lateinit var ration: Ration

    @BeforeTest
    fun setup() {
        ration =
                Ration(
                        name = "Ration test",
                        coef = 1.0f,
                        actual = true,
                        number = 1,
                        espece = "CHIEN",
                        recette = false,
                        description = "Ration de test",
                        idConsult = "test-consult",
                        alimentMutableList = mutableListOf()
                )
    }

    @Test
    fun `test get aliment by UUID`() {
        val aliment1 =
                AlimentRation(uuid = "alim1", refAlimUnif = "ref1", quantity = 100f, refTarget = 1)
        val aliment2 =
                AlimentRation(uuid = "alim2", refAlimUnif = "ref2", quantity = 200f, refTarget = 2)

        ration.alimentMutableList.add(aliment1)
        ration.alimentMutableList.add(aliment2)

        val retrieved = ration.getAlimentByUUID("alim1")
        assertNotNull(retrieved)
        assertEquals("ref1", retrieved.refAlimUnif)
        assertEquals(100f, retrieved.quantity)
    }

    @Test
    fun `test get non-existent aliment`() {
        val aliment =
                AlimentRation(uuid = "alim1", refAlimUnif = "ref1", quantity = 100f, refTarget = 1)
        ration.alimentMutableList.add(aliment)

        assertFailsWith<NoSuchElementException> { ration.getAlimentByUUID("non-existent") }
    }

    @Test
    fun `test ration creation with minimal data`() {
        val minimalRation =
                Ration(
                        name = "Test",
                        idConsult = "test-consult",
                        coef = null,
                        actual = null,
                        number = null,
                        espece = null,
                        recette = null,
                        description = null,
                        alimentMutableList = mutableListOf()
                )

        assertEquals("Test", minimalRation.name)
        assertNull(minimalRation.coef)
        assertNull(minimalRation.actual)
        assertNull(minimalRation.number)
        assertNull(minimalRation.espece)
        assertNull(minimalRation.recette)
        assertNull(minimalRation.description)
        assertTrue(minimalRation.alimentMutableList.isEmpty())
    }
}
