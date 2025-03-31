package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.Data.AnimalEvJson
import fr.vetbrain.vetnutri_mp.DataBase.TestAnimalDao
import fr.vetbrain.vetnutri_mp.DataBase.TestFoodDao
import fr.vetbrain.vetnutri_mp.DataBase.TestNutrientValueDao
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

class DatabaseAnimalRepositoryTest {
    private lateinit var animalRepository: DatabaseAnimalRepository
    private lateinit var animalDao: TestAnimalDao
    private lateinit var foodDao: TestFoodDao
    private lateinit var nutrientValueDao: TestNutrientValueDao
    private lateinit var foodRepository: DatabaseFoodRepository

    @BeforeTest
    fun setup() {
        animalDao = TestAnimalDao()
        foodDao = TestFoodDao()
        nutrientValueDao = TestNutrientValueDao()
        foodRepository = DatabaseFoodRepository(foodDao, nutrientValueDao)
        animalRepository = DatabaseAnimalRepository(animalDao, foodDao)
    }

    @Test
    fun testImportAnimalsFromOza2() = runTest {
        // Préparer un contenu JSON simplifié basé sur la structure de oza2.json
        val jsonContent =
                """
            [
                {
                    "UUID": "test-animal-123",
                    "nom": "Médor",
                    "espece": "1",
                    "race": "Labrador",
                    "nomProprio": "Dupont",
                    "id": "A001",
                    "sex": 1,
                    "dead": false,
                    "resume": "Chien en bonne santé",
                    "version": "1.0",
                    "dateNaiss": "2018-05-15",
                    "listWeight": [
                        {
                            "UUID": "weight-1",
                            "date": "2023-01-01",
                            "value": 30.5
                        }
                    ],
                    "list": {
                        "consultations": [
                            {
                                "UUID": "consult-1",
                                "date": "2023-02-01",
                                "pdate": "2023-02-01",
                                "objet": "Consultation de routine",
                                "observation": "Bonne santé générale",
                                "CRendu": "",
                                "Poids": 30.5,
                                "PoidsIdeal": 30.0,
                                "PoidsIdealex": true,
                                "Boisson": 1.0,
                                "TauxMG": 20.0,
                                "suivi": false,
                                "bcs": "",
                                "MCS": 3,
                                "k1value": 1.0,
                                "k2value": 1.0,
                                "k3value": 1.0,
                                "k4value": 1.0,
                                "k5value": 1.0,
                                "rationList": {
                                    "ration-1": {
                                        "UUID": "ration-1",
                                        "Nom": "Ration quotidienne",
                                        "actual": true,
                                        "alimentList": [
                                            {
                                                "UUID": "aliment-1",
                                                "UUIDunif": "food-1",
                                                "quantite": 200.0,
                                                "prop": 0.0,
                                                "alime": {
                                                    "UUID": "food-1",
                                                    "nom": "Croquettes Premium",
                                                    "group": "ALIMENT_COMMERCIAL",
                                                    "foodKind": "SEC",
                                                    "espece": 1,
                                                    "Especes": ["1"]
                                                }
                                            }
                                        ]
                                    }
                                },
                                "svp": []
                            }
                        ]
                    }
                }
            ]
        """.trimIndent()

        // Configuration du parser JSON
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
            explicitNulls = false
        }

        // Décoder le JSON
        val animalEvJsonList = json.decodeFromString<List<AnimalEvJson>>(jsonContent)

        // Exécuter l'importation
        val result = animalRepository.importAnimals(animalEvJsonList)

        // Vérifier que l'importation a réussi
        assertEquals(1, result, "Un seul animal devrait être importé")

        // Récupérer l'animal importé
        val importedAnimal = animalDao.getAnimalById("test-animal-123")
        assertNotNull(importedAnimal, "L'animal devrait exister dans la base de données")
        assertEquals("Médor", importedAnimal.nom, "Le nom de l'animal importé devrait être 'Médor'")

        // Vérifier que la consultation a été importée
        val consultations = animalDao.getConsultationsForAnimal("test-animal-123")
        assertEquals(1, consultations.size, "L'animal devrait avoir une consultation")

        // Vérifier que la ration a été importée
        val rations = animalDao.getRationsForConsultation(consultations[0].uuid)
        assertEquals(1, rations.size, "La consultation devrait avoir une ration")

        // Vérifier que l'aliment dans la ration a été importé
        val alimentRations = animalDao.getAlimentRationsForRation(rations[0].uuid)
        assertEquals(1, alimentRations.size, "La ration devrait contenir un aliment")

        // Vérifier que l'aliment référencé existe dans la base de données
        val food = foodDao.getFoodById("food-1")
        assertNotNull(food, "L'aliment devrait exister dans la base de données")

        // Vérifier le nom de l'aliment (ce test échouera si le nom est importé comme "Aliment
        // importé [UUID]")
        assertEquals(
                "Croquettes Premium",
                food.nameDef,
                "Le nom de l'aliment devrait être 'Croquettes Premium'"
        )
    }
}
