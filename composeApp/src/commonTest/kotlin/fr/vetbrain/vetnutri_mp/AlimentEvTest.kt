package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class AlimentEvTest {
    private lateinit var aliment: AlimentEv

    @BeforeTest
    fun setup() {
        aliment =
                AlimentEv(
                        group = GroupAlim.FLAIT,
                        typeAliment = FoodKind.COMPLET,
                        ingredients = "Poulet, riz",
                        price = 10.99,
                        categPrice = "Premium",
                        brand = "TestBrand",
                        gamme = "Premium",
                        nom = "Aliment test",
                        consistent = true,
                        cont = 1,
                        quantInt = 400f,
                        deprecated = 0,
                        dataB = "test-db",
                        especes = mutableListOf("CHIEN", "CHAT"),
                        indicat = mutableListOf()
                )
    }

    @Test
    fun `test aliment creation with all fields`() {
        assertEquals(GroupAlim.FLAIT, aliment.group)
        assertEquals(FoodKind.COMPLET, aliment.typeAliment)
        assertEquals("Poulet, riz", aliment.ingredients)
        assertEquals(10.99, aliment.price)
        assertEquals("Premium", aliment.categPrice)
        assertEquals("TestBrand", aliment.brand)
        assertEquals("Premium", aliment.gamme)
        assertEquals("Aliment test", aliment.nom)
        assertTrue(aliment.consistent)
        assertEquals(1, aliment.cont)
        assertEquals(400f, aliment.quantInt)
        assertEquals(0, aliment.deprecated)
        assertEquals("test-db", aliment.dataB)
        assertEquals(2, aliment.especes.size)
        assertTrue(aliment.especes.contains("CHIEN"))
        assertTrue(aliment.especes.contains("CHAT"))
        assertTrue(aliment.indicat.isEmpty())
    }

    @Test
    fun `test aliment creation with minimal fields`() {
        val minimalAliment =
                AlimentEv(
                        group = null,
                        typeAliment = null,
                        ingredients = null,
                        price = null,
                        categPrice = null,
                        brand = null,
                        gamme = null,
                        nom = "Test minimal",
                        consistent = false,
                        cont = null,
                        quantInt = null,
                        deprecated = null,
                        dataB = null,
                        especes = mutableListOf(),
                        indicat = mutableListOf()
                )

        assertNull(minimalAliment.group)
        assertNull(minimalAliment.typeAliment)
        assertNull(minimalAliment.ingredients)
        assertNull(minimalAliment.price)
        assertEquals("Test minimal", minimalAliment.nom)
        assertFalse(minimalAliment.consistent)
        assertTrue(minimalAliment.especes.isEmpty())
        assertTrue(minimalAliment.indicat.isEmpty())
    }

    @Test
    fun `test add and remove species`() {
        val testAliment =
                AlimentEv(
                        group = null,
                        typeAliment = null,
                        ingredients = null,
                        price = null,
                        categPrice = null,
                        brand = null,
                        gamme = null,
                        nom = "Test espèces",
                        consistent = false,
                        cont = null,
                        quantInt = null,
                        deprecated = null,
                        dataB = null,
                        especes = mutableListOf(),
                        indicat = mutableListOf()
                )

        assertTrue(testAliment.especes.isEmpty())

        testAliment.especes.add("CHIEN")
        assertEquals(1, testAliment.especes.size)
        assertTrue(testAliment.especes.contains("CHIEN"))

        testAliment.especes.remove("CHIEN")
        assertTrue(testAliment.especes.isEmpty())
    }

    @Test
    fun `test add and remove indications`() {
        val indic = AlimIndic.PHYS
        assertTrue(aliment.indicat.isEmpty())

        aliment.indicat.add(indic)
        assertEquals(1, aliment.indicat.size)

        aliment.indicat.remove(indic)
        assertTrue(aliment.indicat.isEmpty())
    }
}
