package fr.vetbrain.vetnutri_mp.DataBase

import kotlin.test.*
import kotlinx.datetime.LocalDate

class DatabaseBuildTest {

    @Test
    fun `test création des entités alimentaires`() {
        // Test FoodEntity
        val foodEntity =
                FoodEntity(
                        UUID = "test-uuid",
                        groupAlim = 1,
                        typeAlim = 2,
                        ingredients = "Test ingredients",
                        price = 10.0,
                        categPrice = "Premium",
                        brand = "Test Brand",
                        gamme = "Test Gamme",
                        unitPres = 1,
                        quantityPres = 100f,
                        version = 1,
                        date = LocalDate(2024, 3, 15).toString(),
                        nameDef = "Test Food",
                        consistent = 1,
                        deprecated = 0,
                        DataB = "Test DB"
                )

        assertEquals("test-uuid", foodEntity.UUID)
        assertEquals("Test Food", foodEntity.nameDef)

        // Test AlimentRationEntity avec référence à FoodEntity
        val alimentRationEntity =
                AlimentRationEntity(
                        uuid = "aliment-uuid",
                        refAlimUnif = foodEntity.UUID,
                        refRation = "ration-uuid",
                        quantity = 150f,
                        refTarget = 1
                )

        assertEquals(foodEntity.UUID, alimentRationEntity.refAlimUnif)

        // Test AlimentReferenceEntity
        val alimentReferenceEntity =
                AlimentReferenceEntity(
                        uuid = "ref-uuid",
                        foodId = foodEntity.UUID,
                        referenceType = "UNIFORME",
                        referenceValue = "REF123",
                        version = 1,
                        date = LocalDate(2024, 3, 15).toString()
                )

        assertEquals(foodEntity.UUID, alimentReferenceEntity.foodId)
    }

    @Test
    fun `test création des entités de ration`() {
        // Test RationEntity
        val rationEntity =
                RationEntity(
                        uuid = "ration-uuid",
                        idConsult = "consult-uuid",
                        name = "Test Ration",
                        coef = 1.0f,
                        actual = true,
                        number = 1,
                        espece = "CHIEN",
                        recette = false,
                        description = "Test Description"
                )

        assertEquals("ration-uuid", rationEntity.uuid)
        assertEquals("Test Ration", rationEntity.name)

        // Test AlimentEntity
        val alimentEntity =
                AlimentEntity(
                        uuid = "aliment-base-uuid",
                        group = 1,
                        typeAliment = 2,
                        ingredients = "Test Ingredients",
                        price = 15.0,
                        categPrice = "Standard",
                        brand = "Test Brand",
                        gamme = "Test Gamme",
                        nom = "Test Aliment",
                        consistent = true,
                        cont = 1,
                        quantInt = 200f,
                        deprecated = 0,
                        dataB = "Test DB"
                )

        assertEquals("aliment-base-uuid", alimentEntity.uuid)
        assertEquals("Test Aliment", alimentEntity.nom)

        // Test EspeceAlimentEntity
        val especeAlimentEntity =
                EspeceAlimentEntity(refAliment = alimentEntity.uuid, espece = "CHIEN")

        assertEquals(alimentEntity.uuid, especeAlimentEntity.refAliment)
        assertEquals("CHIEN", especeAlimentEntity.espece)
    }
}
