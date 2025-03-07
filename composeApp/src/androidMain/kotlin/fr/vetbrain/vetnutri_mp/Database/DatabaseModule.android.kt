package fr.vetbrain.vetnutri_mp.DataBase

import android.content.Context

/**
 * Implémentation Android du module de base de données. Cette classe gère l'initialisation, l'accès
 * et la fermeture de la base de données Room sur Android.
 */
class AndroidDatabaseModule(private val context: Context) : DatabaseModule {
    private var database: AppDatabase? = null

    override fun initializeDatabase() {
        if (database == null) {
            database = getRoomDatabase(getDatabaseBuilder(context))
        }
    }

    override fun getDatabase(): Any? {
        return database
    }

    override fun closeDatabase() {
        database?.close()
        database = null
    }
}

/**
 * Crée un module de base de données spécifique à la plateforme Android.
 * @return Une instance de AndroidDatabaseModule
 */
actual fun createPlatformDatabaseModule(): DatabaseModule {
    // Nous avons besoin du contexte d'application pour initialiser Room.
    // Idéalement, il faut récupérer ce contexte d'une façon plus propre.
    val context =
            fr.vetbrain.vetnutri_mp.Localization.AndroidContext.appContext
                    ?: throw IllegalStateException("Le contexte Android n'est pas initialisé")

    return AndroidDatabaseModule(context)
}
