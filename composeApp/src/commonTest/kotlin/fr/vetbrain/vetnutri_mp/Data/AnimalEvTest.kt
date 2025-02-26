package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.BaseTest
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class AnimalEvTest : BaseTest() {
    @Test
    fun `test création d'un animal avec constructeur par défaut`() {
        val animal = AnimalEv()

        assertNotNull(animal.uuid)
        assertEquals("", animal.nom)
        assertEquals(false, animal.dead)
        assertEquals(null, animal.id)
        assertEquals(Sex.MALE.id, animal.sexId)
        assertEquals(Espece.CHIEN.name, animal.specieId)
        assertEquals("", animal.ownerName)
        assertEquals(null, animal.birthdate)
        assertEquals("", animal.race)
        assertEquals("", animal.summary)
        assertTrue(animal.consultations.isEmpty())
        assertTrue(animal.weightHistory.isEmpty())
    }

    @Test
    fun `test création d'un animal avec paramètres spécifiques`() {
        val birthDate = LocalDate(2020, 1, 1)
        val animal =
                AnimalEv(
                        nom = "Rex",
                        dead = false,
                        id = "TEST001",
                        sexId = Sex.MALE.id,
                        specieId = Espece.CHIEN.name,
                        ownerName = "Jean Dupont",
                        birthdate = birthDate,
                        race = "Labrador",
                        summary = "Animal de test"
                )

        assertNotNull(animal.uuid)
        assertEquals("Rex", animal.nom)
        assertEquals(false, animal.dead)
        assertEquals("TEST001", animal.id)
        assertEquals(Sex.MALE.id, animal.sexId)
        assertEquals(Espece.CHIEN.name, animal.specieId)
        assertEquals("Jean Dupont", animal.ownerName)
        assertEquals(birthDate, animal.birthdate)
        assertEquals("Labrador", animal.race)
        assertEquals("Animal de test", animal.summary)
    }

    @Test
    fun `test getSex retourne le bon sexe`() {
        val animal = AnimalEv(sexId = Sex.FEMALE.id)
        assertEquals(Sex.FEMALE, animal.getSex())
    }

    @Test
    fun `test getSex retourne MALE par défaut pour un ID invalide`() {
        val animal = AnimalEv(sexId = -1)
        assertEquals(Sex.MALE, animal.getSex())
    }

    @Test
    fun `test setSex met à jour correctement le sexId`() {
        val animal = AnimalEv()
        animal.setSex(Sex.FEMALE)
        assertEquals(Sex.FEMALE.id, animal.sexId)
    }

    @Test
    fun `test getEspece retourne la bonne espèce`() {
        val animal = AnimalEv(specieId = Espece.CHAT.name)
        assertEquals(Espece.CHAT, animal.getEspece())
    }

    @Test
    fun `test getEspece retourne CHIEN par défaut pour un nom invalide`() {
        val animal = AnimalEv(specieId = "INVALID")
        assertEquals(Espece.CHIEN, animal.getEspece())
    }

    @Test
    fun `test setEspece met à jour correctement le specieId`() {
        val animal = AnimalEv()
        animal.setEspece(Espece.CHAT)
        assertEquals(Espece.CHAT.name, animal.specieId)
    }

    @Test
    fun `test createTestAnimal crée un animal de test valide`() {
        val testAnimal = AnimalEv.createTestAnimal()

        assertEquals("Rex", testAnimal.nom)
        assertEquals(false, testAnimal.dead)
        assertEquals("TEST001", testAnimal.id)
        assertEquals(Sex.MALE.id, testAnimal.sexId)
        assertEquals(Espece.CHIEN.name, testAnimal.specieId)
        assertEquals("Jean Dupont", testAnimal.ownerName)
        assertEquals(LocalDate(2020, 1, 1), testAnimal.birthdate)
        assertEquals("Labrador", testAnimal.race)
        assertEquals("Animal de test", testAnimal.summary)
    }

    @Test
    fun testAnimalEvCreation() {
        val animal =
                AnimalEv(
                        id = 1,
                        nom = "Rex",
                        espece = "Chien",
                        race = "Labrador",
                        poids = 30.0,
                        age = 5,
                        sexe = "M",
                        sterilise = true,
                        stade = "Adulte",
                        activite = "Modérée",
                        nec = 3,
                        pathologie = "Aucune"
                )

        assertEquals(1, animal.id)
        assertEquals("Rex", animal.nom)
        assertEquals("Chien", animal.espece)
        assertEquals("Labrador", animal.race)
        assertEquals(30.0, animal.poids)
        assertEquals(5, animal.age)
        assertEquals("M", animal.sexe)
        assertEquals(true, animal.sterilise)
        assertEquals("Adulte", animal.stade)
        assertEquals("Modérée", animal.activite)
        assertEquals(3, animal.nec)
        assertEquals("Aucune", animal.pathologie)
    }
}
