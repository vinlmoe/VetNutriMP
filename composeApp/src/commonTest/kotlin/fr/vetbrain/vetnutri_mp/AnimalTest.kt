package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Data.Animal
import fr.vetbrain.vetnutri_mp.Data.WeightDate
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import kotlin.test.*
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalUuidApi::class)
class AnimalTest {
    private lateinit var animal: Animal

    @BeforeTest
    fun setup() {
        animal = Animal(nom = "Rex", espece = Espece.CHIEN, dateNaiss = LocalDate(2020, 1, 1))
    }

    @Test
    fun `test add weight`() {
        val weight =
                WeightDate(date = LocalDate(2024, 1, 1), value = 25.5f, refAnimal = animal.UUID)
        animal.addWeight(weight)

        assertTrue(animal.listWeight.contains(weight))
        assertEquals(1, animal.listWeight.size)
    }

    @Test
    fun `test update weight`() {
        val weight =
                WeightDate(date = LocalDate(2024, 1, 1), value = 25.5f, refAnimal = animal.UUID)
        animal.addWeight(weight)

        val newDate = LocalDate(2024, 1, 2)
        val newValue = 26.0f
        animal.updateWeight(weight.uuid, newDate, newValue)

        val updatedWeight = animal.listWeight.first()
        assertEquals(newDate, updatedWeight.date)
        assertEquals(newValue, updatedWeight.value)
    }

    @Test
    fun `test remove weight`() {
        val weight =
                WeightDate(date = LocalDate(2024, 1, 1), value = 25.5f, refAnimal = animal.UUID)
        animal.addWeight(weight)

        animal.removeWeight(weight.uuid)
        assertTrue(animal.listWeight.isEmpty())
    }

    @Test
    fun `test default values`() {
        val newAnimal = Animal(espece = Espece.CHIEN)

        assertFalse(newAnimal.dead)
        assertEquals("", newAnimal.nom)
        assertEquals("", newAnimal.nomProprio)
        assertEquals("", newAnimal.race)
        assertEquals("", newAnimal.resume)
        assertTrue(newAnimal.listWeight.isEmpty())
        assertTrue(newAnimal.list.isEmpty())
    }
}
