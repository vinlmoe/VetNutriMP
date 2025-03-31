package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.BaseTest
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import kotlin.test.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.datetime.LocalDate

class AnimalEvTest : BaseTest() {
    private lateinit var animalTest: AnimalEv
    private val defaultBirthDate = LocalDate(2020, 1, 1)

    @BeforeTest
    override fun setUp() {
        super.setUp()
        animalTest = AnimalEv()
    }

    @Test
    fun `test création d'un animal avec constructeur par défaut`() {
        with(animalTest) {
            assertNotNull(uuid)
            assertEquals("", nom)
            assertFalse(dead)
            assertNull(id)
            assertEquals(Sex.MALE_ENTIER.id, sexId)
            assertEquals(Espece.CHIEN.label, specieId)
            assertEquals("", ownerName)
            assertNull(birthdate)
            assertEquals("", race)
            assertEquals("", summary)
            assertTrue(consultations.isEmpty())
            assertTrue(weightHistory.isEmpty())
        }
    }

    @Test
    fun `test création d'un animal avec paramètres spécifiques`() {
        val animal =
                AnimalEv(
                        nom = "Rex",
                        dead = false,
                        id = "TEST001",
                        sexId = Sex.MALE_ENTIER.id,
                        specieId = Espece.CHIEN.name,
                        ownerName = "Jean Dupont",
                        birthdate = defaultBirthDate,
                        race = "Labrador",
                        summary = "Animal de test"
                )

        with(animal) {
            assertNotNull(uuid)
            assertEquals("Rex", nom)
            assertFalse(dead)
            assertEquals("TEST001", id)
            assertEquals(Sex.MALE_ENTIER.id, sexId)
            assertEquals(Espece.CHIEN.name, specieId)
            assertEquals("Jean Dupont", ownerName)
            assertEquals(defaultBirthDate, birthdate)
            assertEquals("Labrador", race)
            assertEquals("Animal de test", summary)
            assertTrue(consultations.isEmpty())
            assertTrue(weightHistory.isEmpty())
        }
    }

    @Test
    fun `test gestion du sexe de l'animal`() {
        // Test par défaut
        assertEquals(Sex.MALE_ENTIER, animalTest.getSex())

        // Test avec un sexe spécifique
        animalTest.setSex(Sex.FEMELLE_ENTIERE)
        assertEquals(Sex.FEMELLE_ENTIERE.id, animalTest.sexId)
        assertEquals(Sex.FEMELLE_ENTIERE, animalTest.getSex())

        // Test avec un ID invalide
        val animalInvalide = AnimalEv(sexId = -1)
        assertEquals(
                Sex.MALE_ENTIER,
                animalInvalide.getSex(),
                "Un ID de sexe invalide devrait retourner MALE_ENTIER"
        )
    }

    @Test
    fun `test gestion de l'espèce de l'animal`() {
        // Test par défaut
        assertEquals(Espece.CHIEN, animalTest.getEspece())

        // Test avec une espèce spécifique
        animalTest.setEspece(Espece.CHAT)
        assertEquals(Espece.CHAT.name, animalTest.specieId)
        assertEquals(Espece.CHAT, animalTest.getEspece())

        // Test avec un nom invalide
        val animalInvalide = AnimalEv(specieId = "INVALID")
        assertEquals(
                Espece.CHIEN,
                animalInvalide.getEspece(),
                "Un nom d'espèce invalide devrait retourner CHIEN"
        )
    }

    @Test
    fun `test création d'un animal de test`() {
        val testAnimal = AnimalEv.createTestAnimal()

        with(testAnimal) {
            assertNotNull(uuid)
            assertEquals("Rex", nom)
            assertFalse(dead)
            assertEquals("TEST001", id)
            assertEquals(Sex.MALE_ENTIER.id, sexId)
            assertEquals(Espece.CHIEN.name, specieId)
            assertEquals("Jean Dupont", ownerName)
            assertEquals(defaultBirthDate, birthdate)
            assertEquals("Labrador", race)
            assertEquals("Animal de test", summary)
            assertTrue(consultations.isEmpty())
            assertTrue(weightHistory.isEmpty())
        }
    }

    @Test
    fun `test modification des données de l'animal`() {
        // Test modification du nom
        animalTest = animalTest.copy(nom = "Nouveau nom")
        assertEquals("Nouveau nom", animalTest.nom)

        // Test modification du statut de vie
        animalTest = animalTest.copy(dead = true)
        assertTrue(animalTest.dead)

        // Test modification de l'ID
        animalTest = animalTest.copy(id = "NOUVEAU001")
        assertEquals("NOUVEAU001", animalTest.id)

        // Test modification du propriétaire
        animalTest = animalTest.copy(ownerName = "Nouveau propriétaire")
        assertEquals("Nouveau propriétaire", animalTest.ownerName)

        // Test modification de la date de naissance
        val nouvelleDateNaissance = LocalDate(2021, 6, 15)
        animalTest = animalTest.copy(birthdate = nouvelleDateNaissance)
        assertEquals(nouvelleDateNaissance, animalTest.birthdate)

        // Test modification de la race
        animalTest = animalTest.copy(race = "Nouvelle race")
        assertEquals("Nouvelle race", animalTest.race)

        // Test modification du résumé
        animalTest = animalTest.copy(summary = "Nouveau résumé")
        assertEquals("Nouveau résumé", animalTest.summary)
    }

    @Test
    fun `test gestion des chaînes vides et nulles`() {
        val animal =
                AnimalEv(
                        nom = "",
                        ownerName = "", // ownerName ne peut pas être null
                        race = "  ",
                        summary = "",
                        id = null
                )

        with(animal) {
            assertEquals("", nom, "Le nom vide devrait être préservé")
            assertEquals("", ownerName, "Le nom du propriétaire vide devrait être préservé")
            assertEquals("  ", race, "Les espaces dans la race devraient être préservés")
            assertEquals("", summary, "Le résumé vide devrait être préservé")
            assertNull(id, "L'ID null devrait rester null")
        }
    }
}
