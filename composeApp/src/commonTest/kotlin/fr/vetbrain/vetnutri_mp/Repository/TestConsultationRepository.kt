package fr.vetbrain.vetnutri_mp.Repository

import fr.vetbrain.vetnutri_mp.BaseTest
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.DataBase.TestConsultationDao
import fr.vetbrain.vetnutri_mp.DataBase.TestFoodDao
import fr.vetbrain.vetnutri_mp.DataBase.TestFoodRepository
import fr.vetbrain.vetnutri_mp.DataBase.TestNutrientValueDao
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.TestDispatchers
import kotlin.test.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
class TestConsultationRepository : BaseTest() {
    private lateinit var consultationRepository: DatabaseConsultationRepository
    private lateinit var testConsultationDao: TestConsultationDao
    private lateinit var testFoodRepository: TestFoodRepository
    private lateinit var testFoodDao: TestFoodDao
    private lateinit var testNutrientValueDao: TestNutrientValueDao
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testDispatchers: TestDispatchers

    @BeforeTest
    override fun setUp() {
        super.setUp()
        testDispatchers = TestDispatchers(testDispatcher)
        AppDispatchers.setDispatchers(
                io = testDispatchers.io,
                default = testDispatchers.default,
                main = testDispatchers.main
        )

        // Initialisation des DAOs de test
        testConsultationDao = TestConsultationDao()
        testFoodDao = TestFoodDao()
        testNutrientValueDao = TestNutrientValueDao()
        testFoodRepository = TestFoodRepository(testFoodDao, testNutrientValueDao)

        // Initialisation du repository à tester
        consultationRepository =
                DatabaseConsultationRepository(testConsultationDao, testFoodRepository)
    }

    /** Test le chargement des aliments dans les rations liées aux consultations */
    @Test
    fun `test chargement des aliments dans les rations liées aux consultations`() =
            runTest(testDispatcher) {
                // Given - Création d'aliments de test
                val aliment1 =
                        AlimentEv(
                                uuid = Uuid.random().toString(),
                                nom = "Aliment test 1",
                                group = GroupAlim.CEREALPD, // Céréales pour petit-déjeuner
                                typeAliment = FoodKind.COMPLET,
                                rationUUID = null
                        )

                val aliment2 =
                        AlimentEv(
                                uuid = Uuid.random().toString(),
                                nom = "Aliment test 2",
                                group = GroupAlim.DESSERTLAIT, // Dessert lacté
                                typeAliment = FoodKind.COMPLET,
                                rationUUID = null
                        )

                // Ajout des aliments au FoodRepository
                testFoodRepository.insert(aliment1)
                testFoodRepository.insert(aliment2)

                // Création d'une consultation
                val consultation =
                        ConsultationEv(
                                uuid = Uuid.random().toString(),
                                idAnim = Uuid.random().toString(),
                                date = LocalDate(2024, 3, 15),
                                objectConsult = "Test alimentation"
                        )

                // Création d'une ration avec les aliments
                val ration =
                        Ration(
                                uuid = Uuid.random().toString(),
                                idConsult = consultation.uuid,
                                name = "Ration de test",
                                alimentMutableList =
                                        mutableListOf(
                                                AlimentRation(
                                                        uuid = Uuid.random().toString(),
                                                        refRation = "", // Sera défini lors de la
                                                        // sauvegarde
                                                        refAlimUnif = aliment1.uuid,
                                                        quantity = 100.0f,
                                                        proportion = 0.7f
                                                ),
                                                AlimentRation(
                                                        uuid = Uuid.random().toString(),
                                                        refRation = "", // Sera défini lors de la
                                                        // sauvegarde
                                                        refAlimUnif = aliment2.uuid,
                                                        quantity = 50.0f,
                                                        proportion = 0.3f
                                                )
                                        )
                        )

                // Ajout de la ration à la consultation
                consultation.rations.add(ration)

                // When - Sauvegarder la consultation et la récupérer
                consultationRepository.saveConsultation(consultation)
                testDispatcher.scheduler.advanceUntilIdle()

                // Récupérer la consultation avec ses rations et aliments
                val consultations =
                        consultationRepository.getConsultationsForAnimal(consultation.idAnim)
                val loadedConsultation = consultations.firstOrNull()

                // Then - Vérifier que la consultation et ses rations ont bien été chargées
                assertNotNull(loadedConsultation)
                assertEquals(consultation.uuid, loadedConsultation.uuid)
                assertEquals(consultation.idAnim, loadedConsultation.idAnim)
                assertEquals(consultation.date, loadedConsultation.date)
                assertEquals(consultation.objectConsult, loadedConsultation.objectConsult)

                // Vérifier que la ration a bien été chargée
                assertEquals(1, loadedConsultation.rations.size)
                val loadedRation = loadedConsultation.rations.first()
                assertEquals(ration.uuid, loadedRation.uuid)
                assertEquals(ration.name, loadedRation.name)

                // Vérifier que les aliments ont bien été chargés
                assertEquals(2, loadedRation.alimentMutableList.size)

                // Vérifier que les quantités des aliments sont préservées
                val loadedAliment1 =
                        loadedRation.alimentMutableList.find { it.refAlimUnif == aliment1.uuid }
                val loadedAliment2 =
                        loadedRation.alimentMutableList.find { it.refAlimUnif == aliment2.uuid }

                assertNotNull(loadedAliment1)
                assertNotNull(loadedAliment2)
                assertEquals(100.0f, loadedAliment1.quantity)
                assertEquals(50.0f, loadedAliment2.quantity)
            }

    /** Test le chargement d'une consultation avec plusieurs rations */
    @Test
    fun `test chargement d'une consultation avec plusieurs rations`() =
            runTest(testDispatcher) {
                // Given - Création d'un aliment de test
                val aliment =
                        AlimentEv(
                                uuid = Uuid.random().toString(),
                                nom = "Aliment test",
                                group = GroupAlim.CEREALPD, // Céréales pour petit-déjeuner
                                typeAliment = FoodKind.COMPLET,
                                rationUUID = null
                        )

                // Ajout de l'aliment au FoodRepository
                testFoodRepository.insert(aliment)

                // Création d'une consultation avec plusieurs rations
                val consultation =
                        ConsultationEv(
                                uuid = Uuid.random().toString(),
                                idAnim = Uuid.random().toString(),
                                date = LocalDate(2024, 3, 15),
                                objectConsult = "Test multiple rations"
                        )

                // Création de deux rations
                val ration1 =
                        Ration(
                                uuid = Uuid.random().toString(),
                                idConsult = consultation.uuid,
                                name = "Ration actuelle",
                                actual = true,
                                alimentMutableList =
                                        mutableListOf(
                                                AlimentRation(
                                                        uuid = Uuid.random().toString(),
                                                        refAlimUnif = aliment.uuid,
                                                        quantity = 200.0f
                                                )
                                        )
                        )

                val ration2 =
                        Ration(
                                uuid = Uuid.random().toString(),
                                idConsult = consultation.uuid,
                                name = "Ration proposée",
                                actual = false,
                                alimentMutableList =
                                        mutableListOf(
                                                AlimentRation(
                                                        uuid = Uuid.random().toString(),
                                                        refAlimUnif = aliment.uuid,
                                                        quantity = 150.0f
                                                )
                                        )
                        )

                // Ajout des rations à la consultation
                consultation.rations.add(ration1)
                consultation.rations.add(ration2)

                // When - Sauvegarder la consultation et la récupérer
                consultationRepository.saveConsultation(consultation)
                testDispatcher.scheduler.advanceUntilIdle()

                // Récupérer la consultation avec ses rations et aliments
                val consultations =
                        consultationRepository.getConsultationsForAnimal(consultation.idAnim)
                val loadedConsultation = consultations.firstOrNull()

                // Then - Vérifier que la consultation et ses rations ont bien été chargées
                assertNotNull(loadedConsultation)
                assertEquals(consultation.uuid, loadedConsultation.uuid)
                assertEquals(consultation.idAnim, loadedConsultation.idAnim)
                assertEquals(consultation.date, loadedConsultation.date)
                assertEquals(consultation.objectConsult, loadedConsultation.objectConsult)

                // Vérifier que les deux rations ont bien été chargées
                assertEquals(2, loadedConsultation.rations.size)

                // Vérifier les caractéristiques de chaque ration
                val loadedActualRation =
                        loadedConsultation.rations.find { it.actual }
                                ?: throw AssertionError("Ration actuelle non trouvée")
                val loadedProposedRation =
                        loadedConsultation.rations.find { !it.actual }
                                ?: throw AssertionError("Ration proposée non trouvée")

                assertEquals(ration1.uuid, loadedActualRation.uuid)
                assertEquals(ration1.name, loadedActualRation.name)
                assertEquals(ration2.uuid, loadedProposedRation.uuid)
                assertEquals(ration2.name, loadedProposedRation.name)

                // Vérifier que les aliments ont bien été chargés
                assertEquals(1, loadedActualRation.alimentMutableList.size)
                assertEquals(1, loadedProposedRation.alimentMutableList.size)

                // Vérifier les quantités d'aliments
                assertEquals(200.0f, loadedActualRation.alimentMutableList.first().quantity)
                assertEquals(150.0f, loadedProposedRation.alimentMutableList.first().quantity)
            }
}
