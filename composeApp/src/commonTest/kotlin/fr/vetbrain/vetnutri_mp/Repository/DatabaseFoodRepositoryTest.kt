package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.NutrientQuantity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toAlimentEv
import fr.vetbrain.vetnutri_mp.DataBase.TestFoodDao
import fr.vetbrain.vetnutri_mp.DataBase.TestNutrientValueDao
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class DatabaseFoodRepositoryTest {
    private lateinit var foodRepository: DatabaseFoodRepository
    private lateinit var foodDao: TestFoodDao
    private lateinit var nutrientValueDao: TestNutrientValueDao

    @BeforeTest
    fun setup() {
        foodDao = TestFoodDao()
        nutrientValueDao = TestNutrientValueDao()
        foodRepository = DatabaseFoodRepository(foodDao, nutrientValueDao)
    }

    @Test
    fun `test importation espèces avec différents formats`() = runTest {
        // Préparation des aliments avec différents formats d'espèces
        val aliments =
                listOf(
                        // Cas 1: Espèce au format énumération standard
                        createTestFood(
                                uuid = "food-1",
                                nom = "Aliment 1",
                                especes = listOf("CHIEN")
                        ),
                        // Cas 2: Espèce avec crochets et guillemets
                        createTestFood(
                                uuid = "food-2",
                                nom = "Aliment 2",
                                especes = listOf("[\"CHAT\"]")
                        ),
                        // Cas 3: Espèce en minuscules
                        createTestFood(
                                uuid = "food-3",
                                nom = "Aliment 3",
                                especes = listOf("chien")
                        ),
                        // Cas 4: Espèce avec espaces
                        createTestFood(
                                uuid = "food-4",
                                nom = "Aliment 4",
                                especes = listOf(" CHIEN ")
                        ),
                        // Cas 5: Espèce par ID numérique
                        createTestFood(uuid = "food-5", nom = "Aliment 5", especes = listOf("0"))
                )

        // Importation des aliments
        val result = foodRepository.importFoods(aliments)

        // Vérification du résultat
        assertEquals(5, result, "Tous les aliments doivent être importés")

        // Vérification des espèces pour chaque aliment
        val importedFoods = foodDao.getAllFoods()
        assertEquals(
                5,
                importedFoods.size,
                "Tous les aliments doivent être dans la base de données"
        )

        importedFoods.forEach { foodEntity ->
            val aliment = foodEntity.toAlimentEv(nutrientValues = emptyList())

            when (foodEntity.uuid) {
                "food-1" -> {
                    assertEquals(1, aliment.especes.size)
                    assertEquals("CHIEN", aliment.especes[0])
                }
                "food-2" -> {
                    assertEquals(1, aliment.especes.size)
                    assertEquals("CHAT", aliment.especes[0])
                }
                "food-3" -> {
                    assertEquals(1, aliment.especes.size)
                    assertEquals("CHIEN", aliment.especes[0])
                }
                "food-4" -> {
                    assertEquals(1, aliment.especes.size)
                    assertEquals("CHIEN", aliment.especes[0])
                }
                "food-5" -> {
                    assertEquals(1, aliment.especes.size)
                    assertEquals("CHIEN", aliment.especes[0])
                }
            }
        }
    }

    @Test
    fun `test importation indications avec différents formats`() = runTest {
        // Préparation des aliments avec différents formats d'indications
        val aliments =
                listOf(
                        // Cas 1: Indication au format énumération standard
                        createTestFood(
                                uuid = "food-1",
                                nom = "Aliment 1",
                                indications = listOf("OBES")
                        ),
                        // Cas 2: Indication avec crochets et guillemets
                        createTestFood(
                                uuid = "food-2",
                                nom = "Aliment 2",
                                indications = listOf("[\"URO\"]")
                        ),
                        // Cas 3: Indication en minuscules
                        createTestFood(
                                uuid = "food-3",
                                nom = "Aliment 3",
                                indications = listOf("diab")
                        ),
                        // Cas 4: Indication avec espaces
                        createTestFood(
                                uuid = "food-4",
                                nom = "Aliment 4",
                                indications = listOf(" OBES ")
                        ),
                        // Cas 5: Indication par index numérique
                        createTestFood(
                                uuid = "food-5",
                                nom = "Aliment 5",
                                indications = listOf("3")
                        )
                )

        // Importation des aliments
        val result = foodRepository.importFoods(aliments)

        // Vérification du résultat
        assertEquals(5, result, "Tous les aliments doivent être importés")

        // Récupération des aliments importés pour vérification
        val aliment1 = foodDao.getFoodById("food-1")?.toAlimentEv(nutrientValues = emptyList())
        val aliment2 = foodDao.getFoodById("food-2")?.toAlimentEv(nutrientValues = emptyList())
        val aliment3 = foodDao.getFoodById("food-3")?.toAlimentEv(nutrientValues = emptyList())
        val aliment4 = foodDao.getFoodById("food-4")?.toAlimentEv(nutrientValues = emptyList())
        val aliment5 = foodDao.getFoodById("food-5")?.toAlimentEv(nutrientValues = emptyList())

        // Vérification des indications
        assertNotNull(aliment1, "L'aliment 1 doit exister")
        assertEquals(1, aliment1.indicat.size, "L'aliment 1 doit avoir une indication")
        assertEquals(AlimIndic.OBES, aliment1.indicat[0], "L'indication doit être OBES")

        assertNotNull(aliment2, "L'aliment 2 doit exister")
        assertEquals(1, aliment2.indicat.size, "L'aliment 2 doit avoir une indication")
        assertEquals(AlimIndic.URO, aliment2.indicat[0], "L'indication doit être URO")

        assertNotNull(aliment3, "L'aliment 3 doit exister")
        assertEquals(1, aliment3.indicat.size, "L'aliment 3 doit avoir une indication")
        assertEquals(AlimIndic.DIAB, aliment3.indicat[0], "L'indication doit être DIAB")

        assertNotNull(aliment4, "L'aliment 4 doit exister")
        assertEquals(1, aliment4.indicat.size, "L'aliment 4 doit avoir une indication")
        assertEquals(AlimIndic.OBES, aliment4.indicat[0], "L'indication doit être OBES")

        assertNotNull(aliment5, "L'aliment 5 doit exister")
        assertEquals(1, aliment5.indicat.size, "L'aliment 5 doit avoir une indication")
        assertEquals(AlimIndic.SEN, aliment5.indicat[0], "L'indication avec ID 3 doit être SEN")
    }

    @Test
    fun `test importation avec espèces et indications multiples`() = runTest {
        // Aliment avec plusieurs espèces et indications
        val aliment =
                createTestFood(
                        uuid = "food-multi",
                        nom = "Aliment Multi",
                        especes = listOf("CHIEN", "CHAT", "[\"LAPIN\"]"),
                        indications = listOf("OBES", "URO", "[\"DIAB\"]")
                )

        // Importation de l'aliment
        val result = foodRepository.importFoods(listOf(aliment))

        // Vérification du résultat
        assertEquals(1, result, "L'aliment doit être importé")

        // Récupération de l'aliment importé
        val importedAliment =
                foodDao.getFoodById("food-multi")?.toAlimentEv(nutrientValues = emptyList())
        assertNotNull(importedAliment, "L'aliment doit exister dans la base de données")

        // Vérification des espèces
        assertEquals(3, importedAliment.especes.size, "L'aliment doit avoir trois espèces")
        assertTrue(importedAliment.especes.contains("CHIEN"), "Les espèces doivent contenir CHIEN")
        assertTrue(importedAliment.especes.contains("CHAT"), "Les espèces doivent contenir CHAT")
        assertTrue(importedAliment.especes.contains("LAPIN"), "Les espèces doivent contenir LAPIN")

        // Vérification des indications
        assertEquals(3, importedAliment.indicat.size, "L'aliment doit avoir trois indications")
        assertTrue(
                importedAliment.indicat.contains(AlimIndic.OBES),
                "Les indications doivent contenir OBES"
        )
        assertTrue(
                importedAliment.indicat.contains(AlimIndic.URO),
                "Les indications doivent contenir URO"
        )
        assertTrue(
                importedAliment.indicat.contains(AlimIndic.DIAB),
                "Les indications doivent contenir DIAB"
        )
    }

    // Fonction utilitaire pour créer un aliment de test
    private fun createTestFood(
            uuid: String,
            nom: String,
            especes: List<String> = emptyList(),
            indications: List<String> = emptyList()
    ): AlimentEvJson {
        return AlimentEvJson(
                UUID = uuid,
                nom = nom,
                Especes = especes,
                indication = indications,
                group = "ALIMENT_COMMERCIAL",
                foodKind = "SEC",
                ingredients = "Ingrédients test",
                prix = 10.5,
                categoriePrix = "Premium",
                marque = "Marque Test",
                gamme = "Gamme Test",
                quantInt = 500.0f,
                cont = "CAN",
                deprecated = false,
                DataB = "DataB test",
                valMap =
                        mutableMapOf(
                                "proteine" to NutrientQuantity(20.5f, "proteine"),
                                "lipide" to NutrientQuantity(12.3f, "lipide")
                        ),
                espece = 0 // Utilisation de l'ID d'espèce = 0 (CHIEN)
        )
    }
}
