package fr.vetbrain.vetnutri_mp.DataBase

import kotlinx.cinterop.*
import platform.SQLite.*

class IosDatabase(private val database: CPointer<sqlite3>) : AppDatabase {
    init {
        createTables()
    }

    private fun createTables() {
        val createAnimalTable =
                """
            CREATE TABLE IF NOT EXISTS animals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                species TEXT NOT NULL,
                birthDate TEXT NOT NULL
            )
        """.trimIndent()

        val createFoodTable =
                """
            CREATE TABLE IF NOT EXISTS foods (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                category TEXT NOT NULL
            )
        """.trimIndent()

        executeQuery(createAnimalTable)
        executeQuery(createFoodTable)
    }

    private fun executeQuery(query: String) {
        memScoped {
            val stmt = alloc<CPointerVar<sqlite3_stmt>>()
            if (sqlite3_prepare_v2(database, query, -1, stmt.ptr, null) == SQLITE_OK) {
                sqlite3_step(stmt.value)
                sqlite3_finalize(stmt.value)
            }
        }
    }

    override fun animalDao(): CommonAnimalDao = IosAnimalDao(database)
    override fun foodDao(): CommonFoodDao = IosFoodDao(database)

    override fun clearAllTables() {
        executeQuery("DELETE FROM animals")
        executeQuery("DELETE FROM foods")
    }
}
