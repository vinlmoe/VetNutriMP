package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.BaseTest
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.NutrientQuantity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toAlimentEv
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toFoodEntity
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toNutrientValueEntities
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.ContEnum
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.TestDispatchers
import kotlin.test.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class AlimentEvDatabaseTest : BaseTest() {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testDispatchers: TestDispatchers
    private lateinit var mockFoodDao: MockFoodDao
    private lateinit var mockNutrientValueDao: MockNutrientValueDao
    private lateinit var dataSource: LocalAlimentDataSource
    private lateinit var testAliment: AlimentEv

    @BeforeTest
    override fun setUp() {
        super.setUp()
        // Configurer les dispatchers de test
        testDispatchers = TestDispatchers(testDispatcher)
        AppDispatchers.setDispatchers(
                io = testDispatchers.io,
                default = testDispatchers.default,
                main = testDispatchers.main
        )

        // Initialiser les mocks
        mockFoodDao = MockFoodDao()
        mockNutrientValueDao = MockNutrientValueDao()

        // Initialiser la source de données à tester
        dataSource = LocalAlimentDataSource(mockFoodDao, mockNutrientValueDao)

        // Créer un aliment de test
        testAliment = createTestAliment()
    }

    @AfterTest
    override fun tearDown() {
        super.tearDown()
        AppDispatchers.resetDispatchers()
    }

    @Test
    fun `test conversion d'AlimentEv vers FoodEntity`() =
            runTest(testDispatcher) {
                // When - Conversion en FoodEntity
                val entity = testAliment.toFoodEntity()

                // Then - Vérification des propriétés
                with(entity) {
                    assertEquals(testAliment.uuid, uuid)
                    assertEquals(testAliment.group?.ordinal ?: 0, groupAlim)
                    assertEquals(testAliment.typeAliment?.ordinal ?: 0, typeAlim)
                    assertEquals(testAliment.ingredients ?: "", ingredients)
                    assertValeurPositive(price)
                    assertEquals(testAliment.price ?: 0.0, price)
                    assertEquals(testAliment.categPrice ?: "", categPrice)
                    assertEquals(testAliment.brand ?: "", brand)
                    assertEquals(testAliment.gamme ?: "", gamme)
                    assertEquals(testAliment.cont?.name ?: "NO", cont)
                    assertValeurPositive(quantityPres)
                    assertEquals(testAliment.quantInt ?: 0f, quantityPres)
                    assertEquals(testAliment.nom ?: "", nameDef)
                    assertEquals(if (testAliment.consistent) 1 else 0, consistent)
                    assertEquals(if (testAliment.deprecated) 1 else 0, deprecated)
                    assertEquals(testAliment.dataB ?: "", DataB)
                    assertEquals(testAliment.rationUUID ?: "", RefRation)

                    // Vérification des listes
                    val expectedEspecesJson = testAliment.especes.joinToString(",")
                    assertEquals(expectedEspecesJson, especesJson)
                    assertListeSansDoublons(testAliment.especes)

                    val expectedIndicationsJson = testAliment.indicat.joinToString(",") { it.name }
                    assertEquals(expectedIndicationsJson, indicationsJson)
                    assertListeSansDoublons(testAliment.indicat)
                }
            }

    @Test
    fun `test conversion de FoodEntity vers AlimentEv`() =
            runTest(testDispatcher) {
                // Given - Création d'une FoodEntity complète
                val entity = createTestFoodEntity()
                val nutrientValues =
                        listOf(
                                NutrientValueEntity(
                                        refAliment = entity.uuid,
                                        nutrientLabel = NutrientMain.PROTEINE.label,
                                        value = 25.5f
                                ),
                                NutrientValueEntity(
                                        refAliment = entity.uuid,
                                        nutrientLabel = NutrientMain.LIPIDE.label,
                                        value = 12.3f
                                )
                        )

                // Vérification des valeurs nutritionnelles
                nutrientValues.forEach {
                    assertValeurPositive(
                            it.value,
                            "La valeur nutritionnelle ${it.nutrientLabel} doit être positive"
                    )
                }

                // When - Conversion en AlimentEv
                val aliment = entity.toAlimentEv(nutrientValues = nutrientValues)

                // Then - Vérification des propriétés
                with(aliment) {
                    assertEquals(entity.uuid, uuid)
                    assertEquals(GroupAlim.entries.getOrNull(entity.groupAlim), group)
                    assertEquals(FoodKind.entries.getOrNull(entity.typeAlim), typeAliment)
                    assertEquals(entity.ingredients, ingredients)
                    assertValeurPositive(price!!)
                    assertEquals(entity.price, price)
                    assertEquals(entity.categPrice, categPrice)
                    assertEquals(entity.brand, brand)
                    assertEquals(entity.gamme, gamme)
                    assertValeurPositive(quantInt!!)
                    assertEquals(entity.quantityPres, quantInt)
                    assertEquals(entity.nameDef, nom)
                    assertEquals(entity.consistent == 1, consistent)
                    assertEquals(entity.deprecated == 1, deprecated)
                    assertEquals(entity.DataB, dataB)
                    assertEquals(entity.RefRation, rationUUID)

                    // Vérification des espèces
                    val expectedEspeces = entity.especesJson?.split(",") ?: emptyList()
                    assertEquals(expectedEspeces.size, especes.size)
                    assertListeSansDoublons(especes)
                    expectedEspeces.forEach { assertTrue(especes.contains(it)) }

                    // Vérification des indications
                    val expectedIndications =
                            entity.indicationsJson?.split(",")?.mapNotNull {
                                try {
                                    AlimIndic.valueOf(it)
                                } catch (e: Exception) {
                                    null
                                }
                            }
                                    ?: emptyList()
                    assertEquals(expectedIndications.size, indicat.size)
                    assertListeSansDoublons(indicat)
                    expectedIndications.forEach { assertTrue(indicat.contains(it)) }

                    // Vérification des valeurs nutritionnelles
                    assertEquals(2, valMap.size)
                    assertTrue(valMap.containsKey(NutrientMain.PROTEINE))
                    assertTrue(valMap.containsKey(NutrientMain.LIPIDE))
                    assertValeurPositive(valMap[NutrientMain.PROTEINE]?.value!!)
                    assertValeurPositive(valMap[NutrientMain.LIPIDE]?.value!!)
                    assertEquals(25.5f, valMap[NutrientMain.PROTEINE]?.value)
                    assertEquals(12.3f, valMap[NutrientMain.LIPIDE]?.value)
                }
            }

    @Test
    fun `test conversion des nutriments entre AlimentEv et NutrientValueEntity`() =
            runTest(testDispatcher) {
                // Given - Ajout de valeurs nutritionnelles à l'aliment de test
                testAliment.valMap[NutrientMain.PROTEINE] =
                        NutrientQuantity(20.5f, NutrientMain.PROTEINE.label)
                testAliment.valMap[NutrientMain.LIPIDE] =
                        NutrientQuantity(15.2f, NutrientMain.LIPIDE.label)

                // Vérification des valeurs nutritionnelles initiales
                testAliment.valMap.values.forEach {
                    assertValeurPositive(
                            it.value,
                            "La valeur nutritionnelle ${it.nutrientLabel} doit être positive"
                    )
                }

                // When - Conversion en entités de valeurs nutritionnelles
                val nutrientEntities = testAliment.valMap.toNutrientValueEntities(testAliment.uuid)

                // Then - Vérification des entités générées
                assertEquals(2, nutrientEntities.size)
                assertListeSansDoublons(nutrientEntities.map { it.nutrientLabel })

                val proteineEntity =
                        nutrientEntities.find { it.nutrientLabel == NutrientMain.PROTEINE.label }
                assertNotNull(proteineEntity)
                assertEquals(testAliment.uuid, proteineEntity.refAliment)
                assertValeurPositive(proteineEntity.value)
                assertEquals(20.5f, proteineEntity.value)

                val lipideEntity =
                        nutrientEntities.find { it.nutrientLabel == NutrientMain.LIPIDE.label }
                assertNotNull(lipideEntity)
                assertEquals(testAliment.uuid, lipideEntity.refAliment)
                assertValeurPositive(lipideEntity.value)
                assertEquals(15.2f, lipideEntity.value)
            }

    @Test
    fun `test insertion d'un aliment dans la base de données`() =
            runTest(testDispatcher) {
                // When - Insertion dans la base de données
                dataSource.insertFood(testAliment)

                // Then - Vérification que l'aliment a été inséré
                val insertedEntity = mockFoodDao.foods[testAliment.uuid]
                assertNotNull(
                        insertedEntity,
                        "L'aliment devrait être présent dans la base de données"
                )

                with(insertedEntity) {
                    assertEquals(testAliment.uuid, uuid)
                    assertEquals(testAliment.nom, nameDef)
                    assertValeurPositive(price)
                    assertValeurPositive(quantityPres)
                }

                // Vérification des valeurs nutritionnelles
                val nutrientEntities =
                        mockNutrientValueDao.nutrientValues[testAliment.uuid] ?: emptyList()
                assertEquals(testAliment.valMap.size, nutrientEntities.size)
                nutrientEntities.forEach {
                    assertValeurPositive(
                            it.value,
                            "La valeur nutritionnelle ${it.nutrientLabel} doit être positive"
                    )
                }
            }

    @Test
    fun `test mise à jour d'un aliment dans la base de données`() =
            runTest(testDispatcher) {
                // Given - Insertion de l'aliment initial
                dataSource.insertFood(testAliment)

                // Modification de l'aliment
                val updatedAliment =
                        testAliment.copy(
                                nom = "Aliment Modifié",
                                brand = "Marque Modifiée",
                                consistent = !testAliment.consistent
                        )

                // Ajout d'une valeur nutritionnelle
                updatedAliment.valMap[NutrientMain.CENDRE] =
                        NutrientQuantity(3.5f, NutrientMain.CENDRE.label)
                assertValeurPositive(updatedAliment.valMap[NutrientMain.CENDRE]?.value!!)

                // When - Mise à jour dans la base de données
                dataSource.updateFood(updatedAliment)

                // Then - Vérification que l'aliment a été mis à jour
                val updatedEntity = mockFoodDao.foods[updatedAliment.uuid]
                assertNotNull(
                        updatedEntity,
                        "L'aliment mis à jour devrait être présent dans la base de données"
                )

                with(updatedEntity) {
                    assertEquals("Aliment Modifié", nameDef)
                    assertEquals("Marque Modifiée", brand)
                    assertEquals(if (!testAliment.consistent) 1 else 0, consistent)
                    assertValeurPositive(price)
                    assertValeurPositive(quantityPres)
                }

                // Vérification des valeurs nutritionnelles
                val nutrientEntities =
                        mockNutrientValueDao.nutrientValues[updatedAliment.uuid] ?: emptyList()
                assertEquals(updatedAliment.valMap.size, nutrientEntities.size)
                assertTrue(nutrientEntities.any { it.nutrientLabel == NutrientMain.CENDRE.label })
                nutrientEntities.forEach {
                    assertValeurPositive(
                            it.value,
                            "La valeur nutritionnelle ${it.nutrientLabel} doit être positive"
                    )
                }
            }

    @Test
    fun `test suppression d'un aliment de la base de données`() =
            runTest(testDispatcher) {
                // Given - Insertion de l'aliment
                dataSource.insertFood(testAliment)
                assertTrue(mockFoodDao.foods.containsKey(testAliment.uuid))

                // When - Suppression de l'aliment
                dataSource.deleteFood(testAliment.uuid)

                // Then - Vérification que l'aliment a été supprimé
                assertFalse(mockFoodDao.foods.containsKey(testAliment.uuid))
                assertTrue(mockNutrientValueDao.nutrientValues[testAliment.uuid].isNullOrEmpty())
            }

    // Fonctions utilitaires pour créer des objets de test

    private fun createTestAliment(): AlimentEv {
        return AlimentEv(
                uuid = "test-uuid-aliment",
                group = GroupAlim.ABATS,
                typeAliment = FoodKind.COMPLET,
                ingredients = "Ingrédients de test",
                price = 10.5,
                categPrice = "Premium",
                brand = "Marque Test",
                gamme = "Gamme Test",
                nom = "Aliment Test",
                consistent = true,
                cont = ContEnum.CAN,
                quantInt = 500f,
                deprecated = false,
                dataB = "DataB test",
                especes = mutableListOf("CHIEN", "CHAT"),
                indicat = mutableListOf(AlimIndic.PHYS, AlimIndic.OBES),
                valMap =
                        mutableMapOf(
                                NutrientMain.PROTEINE to
                                        NutrientQuantity(20.0f, NutrientMain.PROTEINE.label),
                                NutrientMain.LIPIDE to
                                        NutrientQuantity(10.0f, NutrientMain.LIPIDE.label)
                        ),
                rationUUID = "test-uuid-ration"
        )
    }

    private fun createTestFoodEntity(): FoodEntity {
        return FoodEntity(
                uuid = "test-uuid-entity",
                groupAlim = GroupAlim.ABATS.ordinal,
                typeAlim = FoodKind.COMPLET.ordinal,
                ingredients = "Ingrédients de test entity",
                price = 15.0,
                categPrice = "Standard",
                brand = "Marque Entity",
                gamme = "Gamme Entity",
                cont = ContEnum.CAN.name,
                unitPres = 1,
                quantityPres = 300f,
                version = 1,
                date = "2024-01-01",
                nameDef = "Entity Test",
                consistent = 1,
                deprecated = 0,
                DataB = "DataB entity",
                RefRation = "entity-ration-uuid",
                name = "Entity Test",
                quantite = 300f,
                especesJson = "CHIEN,CHAT,FURET",
                indicationsJson = "PHYS,OBES,DERM"
        )
    }
}

// Classe mock pour FoodDao
class MockFoodDao : FoodDao {
    val foods = mutableMapOf<String, FoodEntity>()

    override suspend fun getFoodById(id: String): FoodEntity? {
        return foods[id]
    }

    override suspend fun findAll(): List<FoodEntity> {
        return foods.values.toList()
    }

    override suspend fun insert(food: FoodEntity) {
        foods[food.uuid] = food
    }

    override suspend fun update(food: FoodEntity) {
        foods[food.uuid] = food
    }

    override suspend fun delete(food: FoodEntity) {
        foods.remove(food.uuid)
    }

    override suspend fun insertFood(food: FoodEntity) {
        insert(food)
    }

    override suspend fun updateFood(food: FoodEntity) {
        update(food)
    }

    override suspend fun deleteFood(uuid: String) {
        foods.remove(uuid)
    }

    override suspend fun deleteAllFoods() {
        foods.clear()
    }

    override suspend fun getFood(uuid: String): FoodEntity? {
        return getFoodById(uuid)
    }

    override suspend fun getAllFoods(): List<FoodEntity> {
        return findAll()
    }

    override suspend fun insertIndications(indications: List<IndicationAlimentEntity>) {
        // Non implémenté pour les tests
    }

    override suspend fun deleteIndicationsForAliment(alimentUuid: String) {
        // Non implémenté pour les tests
    }

    override suspend fun getIndicationsForAliment(
            alimentUuid: String
    ): List<IndicationAlimentEntity> {
        return emptyList() // Non implémenté pour les tests
    }
}

// Classe mock pour NutrientValueDao
class MockNutrientValueDao : NutrientValueDao {
    val nutrientValues = mutableMapOf<String, MutableList<NutrientValueEntity>>()

    override suspend fun getNutrientValues(alimentUuid: String): List<NutrientValueEntity> {
        return nutrientValues[alimentUuid] ?: emptyList()
    }

    suspend fun getNutrientValuesForFood(foodId: String): List<NutrientValueEntity> {
        return getNutrientValues(foodId)
    }

    suspend fun insertNutrientValue(nutrientValue: NutrientValueEntity) {
        val list = nutrientValues.getOrPut(nutrientValue.refAliment) { mutableListOf() }
        list.add(nutrientValue)
    }

    suspend fun deleteNutrientValuesForFood(foodId: String) {
        nutrientValues.remove(foodId)
    }

    override suspend fun insertNutrientValues(values: List<NutrientValueEntity>) {
        values.forEach { nutrientValue ->
            val list = nutrientValues.getOrPut(nutrientValue.refAliment) { mutableListOf() }
            list.add(nutrientValue)
        }
    }

    override suspend fun deleteNutrientValues(values: List<NutrientValueEntity>) {
        values.forEach { value ->
            val list = nutrientValues[value.refAliment]
            if (list != null) {
                nutrientValues[value.refAliment] =
                        list.filter { it.nutrientLabel != value.nutrientLabel }.toMutableList()
            }
        }
    }

    override suspend fun deleteAllNutrientValuesForAliment(alimentUuid: String) {
        nutrientValues.remove(alimentUuid)
    }
}
