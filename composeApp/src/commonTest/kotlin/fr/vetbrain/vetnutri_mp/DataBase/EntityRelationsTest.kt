package fr.vetbrain.vetnutri_mp.DataBase

import kotlin.test.*
import kotlinx.datetime.LocalDate

class EntityRelationsTest {

    @Test
    fun `test relations entre FoodEntity et AlimentRationEntity`() {
        // Création d'un FoodEntity
        val foodEntity =
                FoodEntity(
                        UUID = "food-uuid",
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

        // Création d'une RationEntity
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

        // Création d'un AlimentRationEntity lié au FoodEntity et à la RationEntity
        val alimentRationEntity =
                AlimentRationEntity(
                        uuid = "aliment-uuid",
                        refAlimUnif = foodEntity.UUID,
                        refRation = rationEntity.uuid,
                        quantity = 150f,
                        refTarget = 1
                )

        // Vérification des relations
        assertEquals(foodEntity.UUID, alimentRationEntity.refAlimUnif)
        assertEquals(rationEntity.uuid, alimentRationEntity.refRation)
    }

    @Test
    fun `test relations entre FoodEntity et AlimentReferenceEntity`() {
        // Création d'un FoodEntity
        val foodEntity =
                FoodEntity(
                        UUID = "food-uuid",
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

        // Création d'un AlimentReferenceEntity lié au FoodEntity
        val alimentReferenceEntity =
                AlimentReferenceEntity(
                        uuid = "ref-uuid",
                        foodId = foodEntity.UUID,
                        referenceType = "UNIFORME",
                        referenceValue = "REF123",
                        version = 1,
                        date = LocalDate(2024, 3, 15).toString()
                )

        // Vérification de la relation
        assertEquals(foodEntity.UUID, alimentReferenceEntity.foodId)
    }

    @Test
    fun `test relations entre RationEntity et ConsultationEntity`() {
        // Création d'une ConsultationEntity
        val consultationEntity =
                ConsultationEntity(
                        uuid = "consult-uuid",
                        idAnim = "animal-uuid",
                        date = LocalDate(2024, 3, 15).toString(),
                        objectConsult = "Test Consultation",
                        observation = "Test Observation",
                        cRendu = "Test Compte Rendu",
                        weight = 10.5f,
                        idealWeight = 12.0f,
                        water = 500f,
                        bodyFat = 20f,
                        methodAnalysis = "Test Method",
                        BCS = 3,
                        MCS = 2
                )

        // Création d'une RationEntity liée à la ConsultationEntity
        val rationEntity =
                RationEntity(
                        uuid = "ration-uuid",
                        idConsult = consultationEntity.uuid,
                        name = "Test Ration",
                        coef = 1.0f,
                        actual = true,
                        number = 1,
                        espece = "CHIEN",
                        recette = false,
                        description = "Test Description"
                )

        // Vérification de la relation
        assertEquals(consultationEntity.uuid, rationEntity.idConsult)
    }
}
