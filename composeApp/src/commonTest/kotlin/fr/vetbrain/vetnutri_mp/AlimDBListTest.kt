package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Data.AlimDB
import fr.vetbrain.vetnutri_mp.Data.AlimDBList
import kotlin.test.*

class AlimDBListTest {
    private lateinit var alimDBList: AlimDBList
    private lateinit var testAlimDB: AlimDB

    @BeforeTest
    fun setup() {
        alimDBList = AlimDBList()
        testAlimDB = AlimDB(uuid = "test-uuid", sNom = "Test Aliment", compNom = "Test Complet")
    }

    @Test
    fun `test add aliment to database`() {
        alimDBList.add(testAlimDB)

        val retrieved = alimDBList["test-uuid"]
        assertNotNull(retrieved)
        assertEquals(testAlimDB.sNom, retrieved.sNom)
        assertEquals(testAlimDB.compNom, retrieved.compNom)
    }

    @Test
    fun `test set number for aliment`() {
        alimDBList.add(testAlimDB)
        alimDBList.setNumber("test-uuid", 42)

        val retrieved = alimDBList["test-uuid"]
        assertNotNull(retrieved)
        assertEquals(42, retrieved.number)
    }

    @Test
    fun `test get non-existent aliment`() {
        val retrieved = alimDBList["non-existent"]
        assertNull(retrieved)
    }

    @Test
    fun `test values returns all aliments`() {
        val alim1 = AlimDB("uuid1", "Alim1", "Comp1")
        val alim2 = AlimDB("uuid2", "Alim2", "Comp2")

        alimDBList.add(alim1)
        alimDBList.add(alim2)

        val values = alimDBList.values()
        assertEquals(2, values.size)
        assertTrue(values.contains(alim1))
        assertTrue(values.contains(alim2))
    }

    @Test
    fun `test overwrite existing aliment`() {
        alimDBList.add(testAlimDB)

        val updatedAlim =
                AlimDB(uuid = "test-uuid", sNom = "Updated Name", compNom = "Updated Complete")
        alimDBList.add(updatedAlim)

        val retrieved = alimDBList["test-uuid"]
        assertNotNull(retrieved)
        assertEquals("Updated Name", retrieved.sNom)
        assertEquals("Updated Complete", retrieved.compNom)
    }
}
