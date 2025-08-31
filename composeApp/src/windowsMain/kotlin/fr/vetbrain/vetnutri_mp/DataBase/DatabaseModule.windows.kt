package fr.vetbrain.vetnutri_mp.DataBase

/**
 * Implémentation Windows du module de base de données. Cette classe gère l'initialisation, l'accès
 * et la fermeture de la base de données SQLite sur Windows.
 */
class WindowsDatabaseModule : DatabaseModule {
    private var database: Any? = null

    override fun initializeDatabase() {
        // Initialisation de la base de données Windows (SQLite)
    }

    override fun getDatabase(): Any? {
        return database
    }

    override fun closeDatabase() {
        database = null
    }
}

/**
 * Crée un module de base de données spécifique à la plateforme Windows.
 * @return Une instance de WindowsDatabaseModule
 */
actual fun createPlatformDatabaseModule(): DatabaseModule {
    return WindowsDatabaseModule()
}
