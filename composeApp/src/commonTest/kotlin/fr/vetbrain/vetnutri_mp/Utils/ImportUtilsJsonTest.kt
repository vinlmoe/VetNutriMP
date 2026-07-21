package fr.vetbrain.vetnutri_mp.Utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImportUtilsJsonTest {
    private fun foodJson(
        uuid: String = "food-1",
        name: String = "Aliment test",
        valMap: String = """{"PROTEINE":{"value":25.0,"nut":"PROTEINE"}}"""
    ): String = """
        {
          "UUID": "$uuid",
          "nom": "$name",
          "group": "Test",
          "foodKind": "MEN",
          "espece": 0,
          "unknownFutureField": "ignored",
          "valMap": $valMap
        }
    """.trimIndent()

    private fun animalJson(
        directConsultations: Boolean = true,
        duplicatedFood: Boolean = false
    ): String {
        val food = foodJson()
        val secondFood = if (duplicatedFood) food else foodJson("food-2", "Second aliment")
        val consultation = """
            {
              "UUID": "consult-1",
              "date": "2026-01-15",
              "rationList": {
                "ration-1": {
                  "UUID": "ration-1",
                  "Nom": "Ration test",
                  "alimentList": [
                    {
                      "UUID": "item-1",
                      "UUIDunif": "food-1",
                      "quantite": 100.0,
                      "prop": 0.0,
                      "alime": $food
                    },
                    {
                      "UUID": "item-2",
                      "UUIDunif": "food-2",
                      "quantite": 50.0,
                      "prop": 0.0,
                      "alime": $secondFood
                    }
                  ]
                }
              }
            }
        """.trimIndent()
        val consultationContainer = if (directConsultations) {
            """"consultations": [$consultation]"""
        } else {
            """"list": {"consultations": [$consultation]}"""
        }

        return """
            {
              "UUID": "animal-1",
              "nom": "Rex",
              "sex": 0,
              "espece": 0,
              "dateNaiss": "2020-05-12",
              "race": "Labrador",
              "listWeight": [],
              $consultationContainer
            }
        """.trimIndent()
    }

    @Test
    fun isValidJson_rejectsBlankAndMalformedInput() {
        assertFalse(ImportUtils.isValidJson(""))
        assertFalse(ImportUtils.isValidJson("""{"UUID":"""))
    }

    @Test
    fun isValidJson_acceptsObjectsAndArrays() {
        assertTrue(ImportUtils.isValidJson("""{"value":1}"""))
        assertTrue(ImportUtils.isValidJson("""[{"value":1}]"""))
    }

    @Test
    fun isAnimalJsonContent_distinguishesAnimalFromFood() {
        assertTrue(ImportUtils.isAnimalJsonContent(animalJson()))
        assertFalse(ImportUtils.isAnimalJsonContent(foodJson()))
    }

    @Test
    fun importFoodsFromJson_acceptsSingleFoodAndIgnoresUnknownKeys() {
        val foods = ImportUtils.importFoodsFromJson(foodJson())

        assertEquals(1, foods.size)
        assertEquals("food-1", foods.single().UUID)
        assertEquals(25.0, foods.single().valMap.getValue("PROTEINE").value)
    }

    @Test
    fun importFoodsFromJson_acceptsListAndLegacyNumericNutrientValues() {
        val foods = ImportUtils.importFoodsFromJson(
            "[${foodJson(valMap = """{"proteine":25.5,"lipide":"12.25"}""")}]"
        )

        assertEquals(1, foods.size)
        assertEquals(25.5, foods.single().valMap.getValue("PROTEINE").value)
        assertEquals("PROTEINE", foods.single().valMap.getValue("PROTEINE").nut)
        assertEquals(12.25, foods.single().valMap.getValue("LIPIDE").value)
    }

    @Test
    fun importFoodsFromJson_acceptsLegacyNestedValueWithoutNutrientName() {
        val foods = ImportUtils.importFoodsFromJson(
            foodJson(valMap = """{"cal":{"valeur":2.4}}""")
        )

        assertEquals(2.4, foods.single().valMap.getValue("CAL").value)
        assertEquals("CAL", foods.single().valMap.getValue("CAL").nut)
    }

    @Test
    fun importFoodsFromJson_preservesCalnutMinMaxRange() {
        val foods = ImportUtils.importFoodsFromJson(
            foodJson(
                valMap = """{"PROTEINE":{"value":25.0,"nut":"PROTEINE","valueMin":22.0,"valueMax":28.0}}"""
            )
        )

        val proteine = foods.single().valMap.getValue("PROTEINE")
        assertEquals(25.0, proteine.value)
        assertEquals(22.0, proteine.valueMin)
        assertEquals(28.0, proteine.valueMax)
        assertEquals(22.0, proteine.min)
        assertEquals(28.0, proteine.max)
        assertTrue(proteine.hasRange)
    }

    @Test
    fun importFoodsFromJson_singleValueFallsBackToMeanForMinMax() {
        val foods = ImportUtils.importFoodsFromJson(foodJson())

        val proteine = foods.single().valMap.getValue("PROTEINE")
        assertFalse(proteine.hasRange)
        // Sans plage CALNUT, min et max retombent sur la moyenne
        assertEquals(proteine.value, proteine.min)
        assertEquals(proteine.value, proteine.max)
    }

    @Test
    fun importFoodsFromJson_rejectsAnimalPayload() {
        assertTrue(ImportUtils.importFoodsFromJson(animalJson()).isEmpty())
    }

    @Test
    fun importAnimalsFromJson_importsDirectConsultationsAndEmbeddedFoods() {
        val result = ImportUtils.importAnimalsFromJson(animalJson())

        assertEquals(1, result.animals.size)
        assertEquals("animal-1", result.animals.single().UUID)
        assertEquals(2, result.foods.size)
        assertEquals("Labrador", result.animals.single().race)
    }

    @Test
    fun importAnimalsFromJson_importsConsultationsInsideLegacyListContainer() {
        val result = ImportUtils.importAnimalsFromJson(animalJson(directConsultations = false))

        assertEquals(1, result.animals.size)
        assertEquals(1, result.animals.single().list?.consultations?.size)
        assertEquals(2, result.foods.size)
    }

    @Test
    fun importAnimalsFromJson_deduplicatesIdenticalEmbeddedFoods() {
        val result = ImportUtils.importAnimalsFromJson(animalJson(duplicatedFood = true))

        assertEquals(1, result.foods.size)
    }

    @Test
    fun extractSectionsFromVetNutriInit_extractsFoodsAndPreservesReferencesJson() {
        val payload = """
            {
              "formatVersion": 2,
              "foods": [${foodJson()}],
              "references": [{"UUID":"ref-1","name":"Référence test"}]
            }
        """.trimIndent()

        val (foods, referencesJson) = ImportUtils.extractSectionsFromVetNutriInit(payload)

        assertEquals(1, foods.size)
        assertTrue(referencesJson.contains("ref-1"))
    }

    @Test
    fun malformedPayloads_returnEmptyResultsWithoutThrowing() {
        assertTrue(ImportUtils.importFoodsFromJson("not-json").isEmpty())
        assertTrue(ImportUtils.importAnimalsFromJson("not-json").animals.isEmpty())
        assertTrue(ImportUtils.extractFoodsFromAnimalJson("not-json").isEmpty())
    }
}
