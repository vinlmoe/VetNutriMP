package fr.vetbrain.vetnutri_mp.DataBase

/**
 * Interface définissant le module de base de données à être implémenté selon les plateformes. Cette
 * interface permet de gérer les différentes implémentations spécifiques à chaque plateforme.
 */
interface DatabaseModule {
    /** Initialise la base de données pour la plateforme spécifique. */
    fun initializeDatabase()

    /**
     * Récupère l'instance de la base de données pour la plateforme spécifique.
     * @return L'instance de la base de données
     */
    fun getDatabase(): Any?

    /** Ferme la connexion à la base de données. */
    fun closeDatabase()
}

/**
 * Crée un module de base de données spécifique à la plateforme. Cette fonction est implémentée
 * différemment sur chaque plateforme.
 */
expect fun createPlatformDatabaseModule(): DatabaseModule
