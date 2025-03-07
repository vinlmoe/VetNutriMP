package fr.vetbrain.vetnutri_mp.DataBase

/**
 * Implémentation Desktop du module de base de données. Cette classe gère l'initialisation, l'accès
 * et la fermeture de la base de données SQLite sur desktop.
 */
class DesktopDatabaseModule : DatabaseModule {
    private var database: Any? = null

    override fun initializeDatabase() {
        // Initialisation de la base de données desktop (probablement SQLite)
        println("Desktop database initialization - not yet fully implemented")
    }

    override fun getDatabase(): Any? {
        return database
    }

    override fun closeDatabase() {
        database = null
        println("Desktop database closure - not yet fully implemented")
    }
}

/**
 * Crée un module de base de données spécifique à la plateforme desktop.
 * @return Une instance de DesktopDatabaseModule
 */
actual fun createPlatformDatabaseModule(): DatabaseModule {
    return DesktopDatabaseModule()
}
