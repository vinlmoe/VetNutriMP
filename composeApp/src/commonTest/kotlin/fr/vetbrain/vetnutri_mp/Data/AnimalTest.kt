package fr.vetbrain.vetnutri_mp.Data

import kotlin.test.*
import kotlinx.datetime.LocalDate

class AnimalTest {
    private lateinit var animal: Animal

    @BeforeTest
    fun setup() {
        animal =
                Animal(
                        nom = "Rex",
                        espece = Animal.Espece.CHIEN,
                        sexe = Animal.Sexe.MALE,
                        dateNaissance = LocalDate(2020, 1, 1)
                )
    }

    @Test
    fun `test création animal avec valeurs par défaut`() {
        val animalDefaut = Animal()
        assertEquals("", animalDefaut.nom)
        assertEquals(Animal.Espece.CHIEN, animalDefaut.espece)
        assertEquals(Animal.Sexe.MALE, animalDefaut.sexe)
        assertNull(animalDefaut.dateNaissance)
        assertFalse(animalDefaut.mort)
        assertFalse(animalDefaut.sterilise)
        assertTrue(animalDefaut.poids.isEmpty())
    }

    @Test
    fun `test ajout poids`() {
        val date = LocalDate(2024, 3, 15)
        val valeur = 25.5f
        animal.addWeight(date, valeur)

        assertEquals(1, animal.poids.size)
        val poids = animal.poids.first()
        assertEquals(date, poids.date)
        assertEquals(valeur, poids.value)
        assertEquals(animal.uuid, poids.refAnimal)
    }

    @Test
    fun `test mise à jour poids`() {
        val date = LocalDate(2024, 3, 15)
        val valeurInitiale = 25.5f
        animal.addWeight(date, valeurInitiale)

        val poidsInitial = animal.poids.first()
        val nouvelleValeur = 26.0f
        animal.updateWeight(poidsInitial, nouvelleValeur)

        assertEquals(1, animal.poids.size)
        val poidsModifie = animal.poids.first()
        assertEquals(nouvelleValeur, poidsModifie.value)
    }

    @Test
    fun `test suppression poids`() {
        val date = LocalDate(2024, 3, 15)
        animal.addWeight(date, 25.5f)

        val poids = animal.poids.first()
        animal.removeWeight(poids.uuid)

        assertTrue(animal.poids.isEmpty())
    }
}
