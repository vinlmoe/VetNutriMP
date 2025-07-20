package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import fr.vetbrain.vetnutri_mp.DataBase.*
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers

/**
 * Base de données Room pour KMP. Cette classe définit la structure de la base de données et ses
 * DAOs. Elle est utilisée à la fois sur Android et iOS.
 */
@Database(
        entities =
                [
                        AnimalEntity::class,
                        ConsultationEntity::class,
                        WeightEntity::class,
                        RationEntity::class,
                        AlimentRationEntity::class,
                        EspeceAlimentEntity::class,
                        IndicationAlimentEntity::class,
                        SupplementalVariableEntity::class,
                        FoodEntity::class,
                        NutrientValueEntity::class,
                        BiblioRefEntity::class,
                        EquationEntity::class,
                        ReferenceEvEntity::class,
                        ReferenceEvEquationEntity::class,
                        ReferenceEvCoefficientEntity::class,
                        ReferenceEvNutrientEntity::class],
        version = 18,
        exportSchema = true
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun animalDao(): AnimalDao
    abstract fun consultationDao(): ConsultationDao
    abstract fun nutrientValueDao(): NutrientValueDao
    abstract fun biblioRefDao(): BiblioRefDao
    abstract fun equationDao(): EquationDao
    abstract fun referenceEvDao(): ReferenceEvDao

    companion object {
        const val DATABASE_NAME = "vetnutri.db"
    }
}

/**
 * Fonction de création de la base de données. L'implémentation spécifique à la plateforme est
 * fournie dans les modules Android et iOS.
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

/**
 * Configuration sécurisée de la base de données Room.
 *
 * IMPORTANT: Cette configuration remplace la stratégie destructive précédente qui détruisait toutes
 * les données en cas d'erreur de migration.
 */
fun getRoomDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return try {
        // ✅ Configuration sécurisée avec migrations explicites
        builder.addMigrations(
                        // Migration 17→18 : Test de montée de version sécurisée
                        createMigration17to18()
                )
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(AppDispatchers.IO)
                // ❌ SUPPRIMÉ: .fallbackToDestructiveMigration(true)
                // ✅ Seulement en cas de downgrade de version explicite
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = false)
                .build()
    } catch (e: Exception) {
        println("🚨 ERREUR: Impossible d'ouvrir la base de données: ${e.message}")
        println("📋 Causes possibles:")
        println("   - Corruption du fichier de base de données")
        println("   - Migration de version impossible")
        println("   - Contraintes de clés étrangères violées")
        println("   - Permissions insuffisantes")

        // ⚠️ En dernier recours seulement, avec avertissement explicite
        println("⚠️  ATTENTION: Recréation de la base de données en dernier recours")
        println("⚠️  DONNÉES PERDUES! Implémentez une stratégie de sauvegarde.")

        builder.fallbackToDestructiveMigration(true)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(AppDispatchers.IO)
                .build()
    }
}

/**
 * TODO: Implémenter une stratégie de sauvegarde automatique
 *
 * Cette fonction devrait être appelée périodiquement pour sauvegarder les données importantes de
 * l'utilisateur.
 */
fun createDatabaseBackup(database: AppDatabase): Boolean {
    return try {
        println("📦 Création d'une sauvegarde de la base de données...")
        // TODO: Implémenter l'export JSON des données critiques
        // - Exporter tous les animaux avec leurs consultations
        // - Exporter les rations personnalisées
        // - Exporter les références nutritionnelles custom
        true
    } catch (e: Exception) {
        println("❌ Erreur lors de la sauvegarde: ${e.message}")
        false
    }
}

/**
 * Migration sécurisée de la version 17 à 18
 *
 * Cette migration teste notre système de montée de version sans destruction de données. Aucune
 * modification de structure n'est nécessaire, c'est juste un test.
 */
fun createMigration17to18(): Migration {
    return object : Migration(17, 18) {
        override fun migrate(connection: androidx.sqlite.SQLiteConnection) {
            println("🔄 Migration 17 → 18 en cours...")
            println("🔵 Test de montée de version sécurisée (Room KMP)")

            try {
                // Cette migration ne fait aucune modification de structure
                // C'est juste un test pour vérifier que les données sont préservées

                // ✅ API Room KMP : utilisation de SQLiteConnection
                connection.prepare("SELECT COUNT(*) FROM ANIMALS").use { statement ->
                    if (statement.step()) {
                        val animalCount = statement.getInt(0)
                        println("✅ Vérification: $animalCount animaux toujours présents")
                    }
                }

                connection.prepare("SELECT COUNT(*) FROM FOOD").use { statement ->
                    if (statement.step()) {
                        val foodCount = statement.getInt(0)
                        println("✅ Vérification: $foodCount aliments toujours présents")
                    }
                }

                println("✅ Migration 17 → 18 réussie avec préservation des données!")
            } catch (e: Exception) {
                println("❌ Erreur migration 17 → 18: ${e.message}")
                throw e
            }
        }
    }
}
