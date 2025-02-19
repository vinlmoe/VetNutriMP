package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.Model.Animal
import kotlinx.cinterop.*
import platform.SQLite.*

class IosAnimalDao(private val database: CPointer<sqlite3>) : CommonAnimalDao {

    override suspend fun getAllAnimals(): List<Animal> {
        val animals = mutableListOf<Animal>()

        val query = "SELECT * FROM animals"
        memScoped {
            val stmt = alloc<CPointerVar<sqlite3_stmt>>()
            if (sqlite3_prepare_v2(database, query, -1, stmt.ptr, null) == SQLITE_OK) {
                while (sqlite3_step(stmt.value) == SQLITE_ROW) {
                    val id = sqlite3_column_int64(stmt.value, 0)
                    val name = sqlite3_column_text(stmt.value, 1)?.toKString() ?: ""
                    val species = sqlite3_column_text(stmt.value, 2)?.toKString() ?: ""
                    val birthDate = sqlite3_column_text(stmt.value, 3)?.toKString() ?: ""

                    animals.add(
                            Animal(id = id, name = name, species = species, birthDate = birthDate)
                    )
                }
                sqlite3_finalize(stmt.value)
            }
        }
        return animals
    }

    override suspend fun getAnimalById(id: Long): Animal? {
        var animal: Animal? = null

        val query = "SELECT * FROM animals WHERE id = ?"
        memScoped {
            val stmt = alloc<CPointerVar<sqlite3_stmt>>()
            if (sqlite3_prepare_v2(database, query, -1, stmt.ptr, null) == SQLITE_OK) {
                sqlite3_bind_int64(stmt.value, 1, id)

                if (sqlite3_step(stmt.value) == SQLITE_ROW) {
                    val name = sqlite3_column_text(stmt.value, 1)?.toKString() ?: ""
                    val species = sqlite3_column_text(stmt.value, 2)?.toKString() ?: ""
                    val birthDate = sqlite3_column_text(stmt.value, 3)?.toKString() ?: ""

                    animal = Animal(id = id, name = name, species = species, birthDate = birthDate)
                }
                sqlite3_finalize(stmt.value)
            }
        }
        return animal
    }

    override suspend fun insertAnimal(animal: Animal): Long {
        var id: Long = -1

        val query = "INSERT INTO animals (name, species, birthDate) VALUES (?, ?, ?)"
        memScoped {
            val stmt = alloc<CPointerVar<sqlite3_stmt>>()
            if (sqlite3_prepare_v2(database, query, -1, stmt.ptr, null) == SQLITE_OK) {
                sqlite3_bind_text(stmt.value, 1, animal.name, -1, SQLITE_TRANSIENT)
                sqlite3_bind_text(stmt.value, 2, animal.species, -1, SQLITE_TRANSIENT)
                sqlite3_bind_text(stmt.value, 3, animal.birthDate, -1, SQLITE_TRANSIENT)

                if (sqlite3_step(stmt.value) == SQLITE_DONE) {
                    id = sqlite3_last_insert_rowid(database)
                }
                sqlite3_finalize(stmt.value)
            }
        }
        return id
    }

    override suspend fun updateAnimal(animal: Animal) {
        val query = "UPDATE animals SET name = ?, species = ?, birthDate = ? WHERE id = ?"
        memScoped {
            val stmt = alloc<CPointerVar<sqlite3_stmt>>()
            if (sqlite3_prepare_v2(database, query, -1, stmt.ptr, null) == SQLITE_OK) {
                sqlite3_bind_text(stmt.value, 1, animal.name, -1, SQLITE_TRANSIENT)
                sqlite3_bind_text(stmt.value, 2, animal.species, -1, SQLITE_TRANSIENT)
                sqlite3_bind_text(stmt.value, 3, animal.birthDate, -1, SQLITE_TRANSIENT)
                sqlite3_bind_int64(stmt.value, 4, animal.id)

                sqlite3_step(stmt.value)
                sqlite3_finalize(stmt.value)
            }
        }
    }

    override suspend fun deleteAnimal(id: Long) {
        val query = "DELETE FROM animals WHERE id = ?"
        memScoped {
            val stmt = alloc<CPointerVar<sqlite3_stmt>>()
            if (sqlite3_prepare_v2(database, query, -1, stmt.ptr, null) == SQLITE_OK) {
                sqlite3_bind_int64(stmt.value, 1, id)
                sqlite3_step(stmt.value)
                sqlite3_finalize(stmt.value)
            }
        }
    }
}

// Tests unitaires
@Test
fun testInsertAndGetAnimal() = runTest {
    val dao = IosAnimalDao(database)
    val animal = Animal(name = "Rex", species = "Chien", birthDate = "2020-01-01")

    val id = dao.insertAnimal(animal)
    assertTrue(id > 0)

    val retrievedAnimal = dao.getAnimalById(id)
    assertNotNull(retrievedAnimal)
    assertEquals(animal.name, retrievedAnimal?.name)
    assertEquals(animal.species, retrievedAnimal?.species)
    assertEquals(animal.birthDate, retrievedAnimal?.birthDate)
}

@Test
fun testUpdateAnimal() = runTest {
    val dao = IosAnimalDao(database)
    val animal = Animal(name = "Rex", species = "Chien", birthDate = "2020-01-01")

    val id = dao.insertAnimal(animal)
    val updatedAnimal = animal.copy(id = id, name = "Max")

    dao.updateAnimal(updatedAnimal)

    val retrievedAnimal = dao.getAnimalById(id)
    assertNotNull(retrievedAnimal)
    assertEquals("Max", retrievedAnimal?.name)
}

@Test
fun testDeleteAnimal() = runTest {
    val dao = IosAnimalDao(database)
    val animal = Animal(name = "Rex", species = "Chien", birthDate = "2020-01-01")

    val id = dao.insertAnimal(animal)
    dao.deleteAnimal(id)

    val retrievedAnimal = dao.getAnimalById(id)
    assertNull(retrievedAnimal)
}
