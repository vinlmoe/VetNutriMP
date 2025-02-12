package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalUuidApi::class)
class ConsultationEvTest {
    private lateinit var consultation: ConsultationEv

    @BeforeTest
    fun setup() {
        consultation =
                ConsultationEv(
                        date = LocalDate(2024, 1, 1),
                        objectConsult = "Contrôle de routine",
                        observation = "RAS",
                        cRendu = "Animal en bonne santé",
                        weight = 25.5f,
                        idealWeight = 24.0f,
                        water = 60.0f,
                        bodyFat = 15.0f,
                        methodAnalysis = "Méthode standard",
                        BCS = 3,
                        k1Id = null,
                        k1Value = null,
                        k2Id = null,
                        k2Value = null,
                        k3Id = null,
                        k3Value = null,
                        k4Id = null,
                        k4Value = null,
                        k5Id = null,
                        k5Value = null,
                        nLittle = null,
                        pAdult = null,
                        coefGes = null,
                        coefLact = null,
                        idAnim = "test-animal",
                        MCS = null,
                        rationMutableList = mutableListOf(),
                        suppVarp = mutableListOf(),
                        diseaseRef = mutableListOf()
                )
    }

    @Test
    fun `test get ration by ID`() {
        val ration1 =
                Ration(
                        uuid = "ration1",
                        name = "Ration 1",
                        coef = 1.0f,
                        actual = true,
                        number = 1,
                        espece = "CHIEN",
                        recette = false,
                        description = "Test ration 1",
                        idConsult = consultation.uuid,
                        alimentMutableList = mutableListOf()
                )
        val ration2 =
                Ration(
                        uuid = "ration2",
                        name = "Ration 2",
                        coef = 1.2f,
                        actual = false,
                        number = 2,
                        espece = "CHIEN",
                        recette = false,
                        description = "Test ration 2",
                        idConsult = consultation.uuid,
                        alimentMutableList = mutableListOf()
                )

        consultation.rationMutableList.add(ration1)
        consultation.rationMutableList.add(ration2)

        val retrieved = consultation.getRationByID("ration1")
        assertNotNull(retrieved)
        assertEquals("Ration 1", retrieved.name)
        assertEquals(1.0f, retrieved.coef)
    }

    @Test
    fun `test get non-existent ration`() {
        val ration =
                Ration(
                        uuid = "ration1",
                        name = "Ration 1",
                        coef = 1.0f,
                        actual = true,
                        number = 1,
                        espece = "CHIEN",
                        recette = false,
                        description = "Test ration",
                        idConsult = consultation.uuid,
                        alimentMutableList = mutableListOf()
                )
        consultation.rationMutableList.add(ration)

        assertFailsWith<NoSuchElementException> { consultation.getRationByID("non-existent") }
    }

    @Test
    fun `test consultation creation with minimal data`() {
        val minimalConsultation =
                ConsultationEv(
                        date = LocalDate(2024, 1, 1),
                        objectConsult = "Test",
                        observation = null,
                        cRendu = null,
                        weight = null,
                        idealWeight = null,
                        water = null,
                        bodyFat = null,
                        methodAnalysis = null,
                        BCS = null,
                        k1Id = null,
                        k1Value = null,
                        k2Id = null,
                        k2Value = null,
                        k3Id = null,
                        k3Value = null,
                        k4Id = null,
                        k4Value = null,
                        k5Id = null,
                        k5Value = null,
                        nLittle = null,
                        pAdult = null,
                        coefGes = null,
                        coefLact = null,
                        idAnim = "test-animal",
                        MCS = null,
                        rationMutableList = mutableListOf(),
                        suppVarp = mutableListOf(),
                        diseaseRef = mutableListOf()
                )

        assertEquals(LocalDate(2024, 1, 1), minimalConsultation.date)
        assertEquals("Test", minimalConsultation.objectConsult)
        assertTrue(minimalConsultation.rationMutableList.isEmpty())
    }
}
