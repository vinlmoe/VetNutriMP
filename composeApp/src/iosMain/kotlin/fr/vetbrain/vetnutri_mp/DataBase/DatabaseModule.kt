package fr.vetbrain.vetnutri_mp.DataBase

import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAnimalRepository
import okio.Path
import platform.SQLite.*
import kotlinx.cinterop.*
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSUserDomainMask

class IosDatabaseModule : DatabaseModule {
    private var database: CPointer<sqlite3>? = null
    private var animalDao: CommonAnimalDao? = null
    private var foodDao: CommonFoodDao? = null

    override fun initialize() {
        if (database == null) {
            val dbPath = getDatabasePath()
            memScoped {
                val dbPtr = alloc<CPointerVar<sqlite3>>()
                val result = sqlite3_open_v2(
                    dbPath,
                    dbPtr.ptr,
                    SQLITE_OPEN_READWRITE or SQLITE_OPEN_CREATE,
                    null
                )

                if (result != SQLITE_OK) {
                    throw IllegalStateException("Impossible d'ouvrir la base de données: $result")
                }

                database = dbPtr.value
                database?.let { db ->
                    animalDao = IosAnimalDao(db)
                    foodDao = IosFoodDao(db)
                }
            }
        }
    }

    override fun getAnimalDao(): CommonAnimalDao {
        checkNotNull(animalDao) { "La base de données doit être initialisée d'abord" }
        return animalDao!!
    }

    override fun getFoodDao(): CommonFoodDao {
        checkNotNull(foodDao) { "La base de données doit être initialisée d'abord" }
        return foodDao!!
    }

    override fun getDatabasePath(): String {
        val paths = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        )
        val documentsDirectory = paths.firstOrNull() as? String
            ?: throw IllegalStateException("Impossible de trouver le répertoire Documents")
        
        return "$documentsDirectory/${DatabaseModule.DATABASE_NAME}"
    }
}

actual fun createDatabaseModule(): DatabaseModule = IosDatabaseModule()

actual object DatabaseModule {
    private lateinit var database: AppDatabase
    private lateinit var animalRepository: AnimalRepository

    actual fun initialize(databaseName: String) {
        ensureDatabaseDirectoryExists()
        database = DatabaseBuilder().build()
        animalRepository = DatabaseAnimalRepository(database.animalDao())
    }

    actual fun getAnimalRepository(): AnimalRepository {
        if (!::animalRepository.isInitialized) {
            throw IllegalStateException(
                    "DatabaseModule n'a pas été initialisé. Appelez initialize() d'abord."
            )
        }
        return animalRepository
    }

    actual fun getDatabasePath(): Path {
        return DatabaseBuilder.getDatabasePath()
    }
}
