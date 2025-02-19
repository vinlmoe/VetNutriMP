package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toData
import fr.vetbrain.vetnutri_mp.DataBase.Mappers.toEntity
import kotlinx.datetime.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class MappersTest {
    private lateinit var animal: AnimalEv
    private lateinit var consultation: ConsultationEv
    private lateinit var ration: Ration
    private lateinit var aliment: AlimentEv

    @BeforeTest
    fun setup() {
        // Initialisation des objets de test
        animal = AnimalEv(
            nom = "Rex",
            dead = false,
            id = "TEST001",
            sexId = Sex.MALE.id,
            specieId = Espece.CHIEN.name,
            ownerName = "Jean Dupont",
            birthdate = LocalDate(2020, 1, 1),
            race = "Labrador",
            summary = "Animal de test"
        )

        consultation = ConsultationEv(
            idAnim = animal.uuid,
            date = LocalDate(2024, 3, 15),
            objectConsult = "Consultation de routine",
            observation = "RAS",
            cRendu = "Animal en bonne santé",
            weight = 25.5f,
            methodAnalysis = "Examen clinique"
        )

        ration = Ration(
            idConsult = consultation.uuid,
            name = "Ration test",
            coef = 1.2f,
            actual = true,
            number = 1,
            espece = Espece.CHIEN.name,
            recette = false,
            description = "Ration d'entretien"
        )

        aliment = AlimentEv(
            group = GroupAlim.FLAIT,
            typeAliment = FoodKind.COMPLET,
            ingredients = "Poulet, riz",
            price = 10.99,
            categPrice = "Premium",
            brand = "TestBrand",
            gamme = "Premium",
            nom = "Aliment test",
            consistent = true,
            cont = 1,
            quantInt = 400f,
            deprecated = 0,
            dataB = "test-db",
            especes = mutableListOf("CHIEN", "CHAT"),
            indicat = mutableListOf()
        )
    }

    @Test
    fun `test conversion AnimalEv vers AnimalEntity et retour`() {
        // Conversion vers Entity
        val entity = animal.toEntity()

        // Vérifications
        assertEquals(animal.uuid, entity.uuid)
        assertEquals(animal.nom, entity.nom)
        assertEquals(animal.dead, entity.dead)
        assertEquals(animal.id, entity.id)
        assertEquals(animal.sexId, entity.sexId)
        assertEquals(animal.specieId, entity.specieId)
        assertEquals(animal.ownerName, entity.ownerName)
        assertEquals(animal.birthdate.toString(), entity.birthdate)
        assertEquals(animal.race, entity.race)
        assertEquals(animal.summary, entity.summary)

        // Conversion retour vers Data
        val reconverted = entity.toData()

        // Vérifications
        assertEquals(animal.uuid, reconverted.uuid)
        assertEquals(animal.nom, reconverted.nom)
        assertEquals(animal.dead, reconverted.dead)
        assertEquals(animal.id, reconverted.id)
        assertEquals(animal.sexId, reconverted.sexId)
        assertEquals(animal.specieId, reconverted.specieId)
        assertEquals(animal.ownerName, reconverted.ownerName)
        assertEquals(animal.birthdate, reconverted.birthdate)
        assertEquals(animal.race, reconverted.race)
        assertEquals(animal.summary, reconverted.summary)
    }

    @Test
    fun `test conversion ConsultationEv vers ConsultationEntity et retour`() {
        // Conversion vers Entity
        val entity = consultation.toEntity()

        // Vérifications
        assertEquals(consultation.uuid, entity.uuid)
        assertEquals(consultation.idAnim, entity.idAnim)
        assertEquals(consultation.date.toString(), entity.date)
        assertEquals(consultation.objectConsult, entity.objectConsult)
        assertEquals(consultation.observation, entity.observation)
        assertEquals(consultation.cRendu, entity.cRendu)
        assertEquals(consultation.weight, entity.weight)
        assertEquals(consultation.methodAnalysis, entity.methodAnalysis)

        // Conversion retour vers Data
        val reconverted = entity.toData()

        // Vérifications
        assertEquals(consultation.uuid, reconverted.uuid)
        assertEquals(consultation.idAnim, reconverted.idAnim)
        assertEquals(consultation.date, reconverted.date)
        assertEquals(consultation.objectConsult, reconverted.objectConsult)
        assertEquals(consultation.observation, reconverted.observation)
        assertEquals(consultation.cRendu, reconverted.cRendu)
        assertEquals(consultation.weight, reconverted.weight)
        assertEquals(consultation.methodAnalysis, reconverted.methodAnalysis)
    }

    @Test
    fun `test conversion Ration vers RationEntity et retour`() {
        // Conversion vers Entity
        val entity = ration.toEntity()

        // Vérifications
        assertEquals(ration.uuid, entity.uuid)
        assertEquals(ration.idConsult, entity.idConsult)
        assertEquals(ration.name, entity.name)
        assertEquals(ration.coef, entity.coef)
        assertEquals(ration.actual, entity.actual)
        assertEquals(ration.number, entity.number)
        assertEquals(ration.espece, entity.espece)
        assertEquals(ration.recette, entity.recette)
        assertEquals(ration.description, entity.description)

        // Conversion retour vers Data
        val reconverted = entity.toData()

        // Vérifications
        assertEquals(ration.uuid, reconverted.uuid)
        assertEquals(ration.idConsult, reconverted.idConsult)
        assertEquals(ration.name, reconverted.name)
        assertEquals(ration.coef, reconverted.coef)
        assertEquals(ration.actual, reconverted.actual)
        assertEquals(ration.number, reconverted.number)
        assertEquals(ration.espece, reconverted.espece)
        assertEquals(ration.recette, reconverted.recette)
        assertEquals(ration.description, reconverted.description)
    }

    @Test
    fun `test conversion AlimentEv vers AlimentEntity et retour`() {
        // Conversion vers Entity
        val entity = aliment.toEntity()

        // Vérifications
        assertEquals(aliment.uuid, entity.uuid)
        assertEquals(aliment.group?.id, entity.group)
        assertEquals(aliment.typeAliment?.coef, entity.typeAliment)
        assertEquals(aliment.ingredients, entity.ingredients)
        assertEquals(aliment.price, entity.price)
        assertEquals(aliment.categPrice, entity.categPrice)
        assertEquals(aliment.brand, entity.brand)
        assertEquals(aliment.gamme, entity.gamme)
        assertEquals(aliment.nom, entity.nom)
        assertEquals(aliment.consistent, entity.consistent)
        assertEquals(aliment.cont, entity.cont)
        assertEquals(aliment.quantInt, entity.quantInt)
        assertEquals(aliment.deprecated, entity.deprecated)
        assertEquals(aliment.dataB, entity.dataB)

        // Conversion retour vers Data
        val reconverted = entity.toData()

        // Vérifications
        assertEquals(aliment.uuid, reconverted.uuid)
        assertEquals(aliment.group, reconverted.group)
        assertEquals(aliment.typeAliment, reconverted.typeAliment)
        assertEquals(aliment.ingredients, reconverted.ingredients)
        assertEquals(aliment.price, reconverted.price)
        assertEquals(aliment.categPrice, reconverted.categPrice)
        assertEquals(aliment.brand, reconverted.brand)
        assertEquals(aliment.gamme, reconverted.gamme)
        assertEquals(aliment.nom, reconverted.nom)
        assertEquals(aliment.consistent, reconverted.consistent)
        assertEquals(aliment.cont, reconverted.cont)
        assertEquals(aliment.quantInt, reconverted.quantInt)
        assertEquals(aliment.deprecated, reconverted.deprecated)
        assertEquals(aliment.dataB, reconverted.dataB)
    }

    @Test
    fun `test gestion des valeurs nulles`() {
        // Test avec un animal avec des valeurs nulles
        val animalWithNulls = AnimalEv()
        val entityFromNull = animalWithNulls.toEntity()
        val reconvertedFromNull = entityFromNull.toData()

        // Vérification des valeurs par défaut
        assertNotNull(reconvertedFromNull.uuid)
        assertEquals("", reconvertedFromNull.nom)
        assertFalse(reconvertedFromNull.dead)
        assertNull(reconvertedFromNull.id)
        assertEquals(0, reconvertedFromNull.sexId)
        assertEquals("", reconvertedFromNull.specieId)
        assertEquals("", reconvertedFromNull.ownerName)
        assertNull(reconvertedFromNull.birthdate)
        assertEquals("", reconvertedFromNull.race)
        assertEquals("", reconvertedFromNull.summary)
    }
}
