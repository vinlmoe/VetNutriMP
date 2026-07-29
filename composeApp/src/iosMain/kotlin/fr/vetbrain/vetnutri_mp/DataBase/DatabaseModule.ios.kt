package fr.vetbrain.vetnutri_mp.DataBase

/**
 * Implémentation iOS du module de base de données. Cette classe gère l'initialisation, l'accès et
 * la fermeture de la base de données SQLite sur iOS.
 */
class IOSDatabaseModule : DatabaseModule {
    private var database: Any? = null

    override fun initializeDatabase() {
        // Initialisation de la base de données iOS (SQLite ou autre)
    }

    override fun getDatabase(): Any? {
        return database
    }

    override fun closeDatabase() {
        database = null
    }
}

/**
 * Crée un module de base de données spécifique à la plateforme iOS.
 * @return Une instance de IOSDatabaseModule
 */
actual fun createPlatformDatabaseModule(): DatabaseModule {
    return IOSDatabaseModule()
}
