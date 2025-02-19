package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.Model.Food
import kotlinx.cinterop.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import platform.SQLite.*

class IosFoodDao(private val database: CPointer<sqlite3>) : CommonFoodDao {

    override fun getAllFoods(): Flow<List<Food>> = flow {
        val foods = mutableListOf<Food>()

        val query = "SELECT * FROM foods"
        memScoped {
            val stmt = alloc<CPointerVar<sqlite3_stmt>>()
            if (sqlite3_prepare_v2(database, query, -1, stmt.ptr, null) == SQLITE_OK) {
                while (sqlite3_step(stmt.value) == SQLITE_ROW) {
                    val id = sqlite3_column_int64(stmt.value, 0)
                    val name = sqlite3_column_text(stmt.value, 1)?.toKString() ?: ""
                    val description = sqlite3_column_text(stmt.value, 2)?.toKString() ?: ""
                    val category = sqlite3_column_text(stmt.value, 3)?.toKString() ?: ""

                    foods.add(
                            Food(
                                    id = id,
                                    name = name,
                                    description = description,
                                    category = category
                            )
                    )
                }
                sqlite3_finalize(stmt.value)
            }
        }
        emit(foods)
    }

    override fun getFoodById(id: Long): Flow<Food?> = flow {
        var food: Food? = null

        val query = "SELECT * FROM foods WHERE id = ?"
        memScoped {
            val stmt = alloc<CPointerVar<sqlite3_stmt>>()
            if (sqlite3_prepare_v2(database, query, -1, stmt.ptr, null) == SQLITE_OK) {
                sqlite3_bind_int64(stmt.value, 1, id)

                if (sqlite3_step(stmt.value) == SQLITE_ROW) {
                    val name = sqlite3_column_text(stmt.value, 1)?.toKString() ?: ""
                    val description = sqlite3_column_text(stmt.value, 2)?.toKString() ?: ""
                    val category = sqlite3_column_text(stmt.value, 3)?.toKString() ?: ""

                    food =
                            Food(
                                    id = id,
                                    name = name,
                                    description = description,
                                    category = category
                            )
                }
                sqlite3_finalize(stmt.value)
            }
        }
        emit(food)
    }

    override suspend fun insertFood(food: Food): Long {
        var newId: Long = -1

        val query = "INSERT INTO foods (name, description, category) VALUES (?, ?, ?)"
        memScoped {
            val stmt = alloc<CPointerVar<sqlite3_stmt>>()
            if (sqlite3_prepare_v2(database, query, -1, stmt.ptr, null) == SQLITE_OK) {
                sqlite3_bind_text(stmt.value, 1, food.name, -1, SQLITE_TRANSIENT)
                sqlite3_bind_text(stmt.value, 2, food.description, -1, SQLITE_TRANSIENT)
                sqlite3_bind_text(stmt.value, 3, food.category, -1, SQLITE_TRANSIENT)

                if (sqlite3_step(stmt.value) == SQLITE_DONE) {
                    newId = sqlite3_last_insert_rowid(database)
                }
                sqlite3_finalize(stmt.value)
            }
        }
        return newId
    }

    override suspend fun updateFood(food: Food) {
        val query = "UPDATE foods SET name = ?, description = ?, category = ? WHERE id = ?"
        memScoped {
            val stmt = alloc<CPointerVar<sqlite3_stmt>>()
            if (sqlite3_prepare_v2(database, query, -1, stmt.ptr, null) == SQLITE_OK) {
                sqlite3_bind_text(stmt.value, 1, food.name, -1, SQLITE_TRANSIENT)
                sqlite3_bind_text(stmt.value, 2, food.description, -1, SQLITE_TRANSIENT)
                sqlite3_bind_text(stmt.value, 3, food.category, -1, SQLITE_TRANSIENT)
                sqlite3_bind_int64(stmt.value, 4, food.id)

                sqlite3_step(stmt.value)
                sqlite3_finalize(stmt.value)
            }
        }
    }

    override suspend fun deleteFood(id: Long) {
        val query = "DELETE FROM foods WHERE id = ?"
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
fun testInsertAndGetFood() = runTest {
    val dao = IosFoodDao(database)
    val food =
            Food(
                    name = "Croquettes Premium",
                    description = "Croquettes haut de gamme pour chiens",
                    category = "Croquettes"
            )

    val id = dao.insertFood(food)
    assertTrue(id > 0)

    val retrievedFood = dao.getFoodById(id).first()
    assertNotNull(retrievedFood)
    assertEquals(food.name, retrievedFood?.name)
    assertEquals(food.description, retrievedFood?.description)
    assertEquals(food.category, retrievedFood?.category)
}

@Test
fun testUpdateFood() = runTest {
    val dao = IosFoodDao(database)
    val food =
            Food(
                    name = "Croquettes Premium",
                    description = "Croquettes haut de gamme pour chiens",
                    category = "Croquettes"
            )

    val id = dao.insertFood(food)
    val updatedFood = food.copy(id = id, name = "Croquettes Premium Plus")

    dao.updateFood(updatedFood)

    val retrievedFood = dao.getFoodById(id).first()
    assertNotNull(retrievedFood)
    assertEquals("Croquettes Premium Plus", retrievedFood?.name)
}

@Test
fun testDeleteFood() = runTest {
    val dao = IosFoodDao(database)
    val food =
            Food(
                    name = "Croquettes Premium",
                    description = "Croquettes haut de gamme pour chiens",
                    category = "Croquettes"
            )

    val id = dao.insertFood(food)
    dao.deleteFood(id)

    val retrievedFood = dao.getFoodById(id).first()
    assertNull(retrievedFood)
}
