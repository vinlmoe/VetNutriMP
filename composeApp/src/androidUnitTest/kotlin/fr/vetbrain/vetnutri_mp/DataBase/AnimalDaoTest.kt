package fr.vetbrain.vetnutri_mp.DataBase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class AnimalDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var animalDao: AnimalDao

    @Before
    fun setupDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
                Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                        .allowMainThreadQueries()
                        .build()
        animalDao = database.animalDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertAndGetAnimal() = runTest {
        val animal =
                AnimalEntity(
                        UUID = "test-uuid",
                        name = "Test Animal",
                        dead = 0,
                        id = "A123",
                        sex = 1,
                        specie = "Dog",
                        ownerName = "John Doe",
                        birthdate = "2020-01-01",
                        race = "Labrador",
                        summary = "Test summary"
                )

        animalDao.insertAnimal(animal)

        val retrievedAnimal = animalDao.getAnimalById("test-uuid")
        assertNotNull(retrievedAnimal)
        assertEquals(animal.UUID, retrievedAnimal.UUID)
        assertEquals(animal.name, retrievedAnimal.name)
    }

    @Test
    fun deleteAnimal() = runTest {
        val animal =
                AnimalEntity(
                        UUID = "test-uuid",
                        name = "Rex",
                        dead = 0,
                        id = "A123",
                        sex = 1,
                        specie = "Dog",
                        ownerName = "John Doe",
                        birthdate = "2020-01-01",
                        race = "Labrador",
                        summary = "Test summary"
                )

        animalDao.insertAnimal(animal)
        animalDao.deleteAnimal(animal)

        val retrievedAnimal = animalDao.getAnimalById("test-uuid")
        assertNull(retrievedAnimal)
    }

    @Test
    fun insertAndGetConsultation() = runTest {
        val consultation =
                ConsultationEntity(
                        UUID = "consult-uuid",
                        date = "2024-03-20",
                        subject = "Check-up",
                        observation = "RAS",
                        cRendu = "Normal",
                        weight = 25f,
                        idealWeight = 23f,
                        water = 60f,
                        bodyFat = 15f,
                        methodAnalysis = "Standard",
                        BCS = 3,
                        k1Id = "k1",
                        k1Value = 1.0f,
                        k2Id = "k2",
                        k2Value = 1.0f,
                        k3Id = "k3",
                        k3Value = 1.0f,
                        k4Id = "k4",
                        k4Value = 1.0f,
                        k5Id = "k5",
                        k5Value = 1.0f,
                        nLittle = 0,
                        pAdult = 1.0f,
                        coefGes = 0,
                        coefLact = 0,
                        idAnim = "test-uuid",
                        MCS = 3
                )

        animalDao.insertConsultation(consultation)

        val retrievedConsultation = animalDao.getConsultationById("consult-uuid")
        assertNotNull(retrievedConsultation)
        assertEquals(consultation.UUID, retrievedConsultation.UUID)
    }

    @Test
    fun getConsultationsForAnimal() = runTest {
        val consultations =
                listOf(
                        ConsultationEntity(
                                UUID = "consult1",
                                date = "2024-01-01",
                                subject = "Check-up 1",
                                observation = "RAS",
                                cRendu = "Normal",
                                weight = 25.5f,
                                idealWeight = 23f,
                                water = 60f,
                                bodyFat = 15f,
                                methodAnalysis = "Standard",
                                BCS = 3,
                                k1Id = "k1",
                                k1Value = 1.0f,
                                k2Id = "k2",
                                k2Value = 1.0f,
                                k3Id = "k3",
                                k3Value = 1.0f,
                                k4Id = "k4",
                                k4Value = 1.0f,
                                k5Id = "k5",
                                k5Value = 1.0f,
                                nLittle = 0,
                                pAdult = 1.0f,
                                coefGes = 0,
                                coefLact = 0,
                                idAnim = "animal1",
                                MCS = 3
                        ),
                        ConsultationEntity(
                                UUID = "consult2",
                                date = "2024-02-01",
                                subject = "Check-up 2",
                                observation = "RAS",
                                cRendu = "Normal",
                                weight = 26.0f,
                                idealWeight = 23f,
                                water = 60f,
                                bodyFat = 15f,
                                methodAnalysis = "Standard",
                                BCS = 3,
                                k1Id = "k1",
                                k1Value = 1.0f,
                                k2Id = "k2",
                                k2Value = 1.0f,
                                k3Id = "k3",
                                k3Value = 1.0f,
                                k4Id = "k4",
                                k4Value = 1.0f,
                                k5Id = "k5",
                                k5Value = 1.0f,
                                nLittle = 0,
                                pAdult = 1.0f,
                                coefGes = 0,
                                coefLact = 0,
                                idAnim = "animal1",
                                MCS = 3
                        )
                )

        consultations.forEach { animalDao.insertConsultation(it) }

        val retrievedConsultations = animalDao.getConsultationsForAnimal("animal1")
        assertEquals(2, retrievedConsultations.size)
    }

    @Test
    fun insertAndGetWeight() = runTest {
        val weight =
                WeightEntity(
                        id = 0,
                        refAnimal = "test-uuid",
                        date = "2024-03-20",
                        value = 25f,
                        UUID = "weight-uuid"
                )

        animalDao.insertWeight(weight)

        val retrievedWeights = animalDao.getWeightsForAnimal("test-uuid")
        assertEquals(1, retrievedWeights.size)
        assertEquals(25f, retrievedWeights[0].value)
    }
}
