package fr.vetbrain.vetnutri_mp.DataBase

import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteConnection

actual class PlatformDatabaseBuilder :
    DatabaseBuilder {
    override fun build(): AppDatabase {
        val config =
                SQLiteConfig().apply {
                    setSharedCache(true)
                    enableLoadExtension(true)
                }

        val connection =
                SQLiteConnection.createConnection(
                        "jdbc:sqlite:${AppDatabase.DATABASE_NAME}",
                        config.toProperties()
                )

        return DesktopDatabase(connection)
    }
}
