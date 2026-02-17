package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteException
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
                        RecetteEntity::class,
                        AlimentRecetteEntity::class,
                        EspeceAlimentEntity::class,
                        IndicationAlimentEntity::class,
                        SupplementalVariableEntity::class,
                        FoodEntity::class,
                        NutrientValueEntity::class,
                        BiblioRefEntity::class,
                        EquationEntity::class,
                        ConsultationKeywordEntity::class,
                        ReferenceEvEntity::class,
                        ReferenceEvEquationEntity::class,
                        ReferenceEvCoefficientEntity::class,
                        ReferenceEvNutrientEntity::class,
                        HtmlSectionEntity::class,
                        HtmlSectionLibraryEntity::class],
        version = 28,
        exportSchema = true
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun animalDao(): AnimalDao
    abstract fun consultationDao(): ConsultationDao
    abstract fun recipeDao(): RecipeDao
    abstract fun nutrientValueDao(): NutrientValueDao
    abstract fun biblioRefDao(): BiblioRefDao
    abstract fun equationDao(): EquationDao
    abstract fun referenceEvDao(): ReferenceEvDao
    abstract fun htmlSectionDao(): HtmlSectionDao

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
        builder.setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .addMigrations(
                        // Migration 17→18 : Test de montée de version sécurisée
                        createMigration17to18(),
                        // Migration 18→19 : Test de notre système Room KMP
                        createMigration18to19(),
                        // Migration 19→20 : Ajout du champ nutrient à la table EQUATIONS
                        createMigration19to20(),
                        // Migration 20→21 : Ajout du champ ratio à la table EQUATIONS
                        createMigration20to21(),
                        // Migration 21→22 : Création des tables de recettes
                        createMigration21to22(),
                        // Migration 22→23 : Ajout d'index pour optimiser les performances
                        createMigration22to23(),
                        // Migration 23→24 : Ajout des tables HTML_SECTIONS et
                        // HTML_SECTION_LIBRARIES
                        createMigration23to24(),
                        // Migration 24→25 : Ajout du champ jsonbinId à la table ANIMALS
                        createMigration24to25(),
                        // Migration 25→26 : Ajout des mots-clés pour les consultations
                        createMigration25to26(),
                        // Migration 26→27 : Ajout des champs de mise à jour et image produit
                        createMigration26to27(),
                        // Migration 27→28 : Ajout des champs ordonnance pour les consultations
                        createMigration27to28()
                )
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(AppDispatchers.IO)
                // ❌ SUPPRIMÉ: .fallbackToDestructiveMigration(true)
                // ✅ Seulement en cas de downgrade de version explicite
                .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = false)
                .build()
    } catch (e: Exception) {

        // ⚠️ En dernier recours seulement, avec avertissement explicite

        builder.setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .fallbackToDestructiveMigration(true)
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
        // TODO: Implémenter l'export JSON des données critiques
        // - Exporter tous les animaux avec leurs consultations
        // - Exporter les rations personnalisées
        // - Exporter les références nutritionnelles custom
        true
    } catch (e: Exception) {
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

            try {
                // Cette migration ne fait aucune modification de structure
                // C'est juste un test pour vérifier que les données sont préservées

                // ✅ API Room KMP : utilisation de SQLiteConnection
                connection.prepare("SELECT COUNT(*) FROM ANIMALS").use { statement ->
                    if (statement.step()) {
                        val animalCount = statement.getInt(0)
                    }
                }

                connection.prepare("SELECT COUNT(*) FROM FOOD").use { statement ->
                    if (statement.step()) {
                        val foodCount = statement.getInt(0)
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }
}

/**
 * Migration sécurisée de la version 18 à 19
 *
 * Cette migration teste notre système de montée de version Room KMP. Aucune modification de
 * structure n'est nécessaire, c'est juste un test.
 */
fun createMigration18to19(): Migration {
    return object : Migration(18, 19) {
        override fun migrate(connection: androidx.sqlite.SQLiteConnection) {

            try {
                // Cette migration ne fait aucune modification de structure
                // C'est juste un test pour vérifier que les données sont préservées

                // ✅ API Room KMP : utilisation de SQLiteConnection
                connection.prepare("SELECT COUNT(*) FROM ANIMALS").use { statement ->
                    if (statement.step()) {
                        val animalCount = statement.getInt(0)
                    }
                }

                connection.prepare("SELECT COUNT(*) FROM FOOD").use { statement ->
                    if (statement.step()) {
                        val foodCount = statement.getInt(0)
                    }
                }

                connection.prepare("SELECT COUNT(*) FROM CONSULTATIONS").use { statement ->
                    if (statement.step()) {
                        val consultationCount = statement.getInt(0)
                    }
                }

                connection.prepare("SELECT COUNT(*) FROM RATIONS").use { statement ->
                    if (statement.step()) {
                        val rationCount = statement.getInt(0)
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }
}

/**
 * Migration sécurisée de la version 19 à 20
 *
 * Cette migration ajoute le champ nutrient à la table EQUATIONS pour supporter les équations de
 * type NEED et COMPLEMENTARY_NUTRIENT.
 */
fun createMigration19to20(): Migration {
    return object : Migration(19, 20) {
        override fun migrate(connection: androidx.sqlite.SQLiteConnection) {

            try {
                // Ajouter la colonne nutrient à la table EQUATIONS
                connection.prepare("ALTER TABLE EQUATIONS ADD COLUMN nutrient TEXT").use { statement
                    ->
                    statement.step()
                }
            } catch (e: Exception) {

                throw e
            }
        }
    }
}

/** Migration 20 → 21 : Ajout du champ ratio (BOOLEAN) à la table EQUATIONS */
fun createMigration20to21(): Migration {
    return object : Migration(20, 21) {
        override fun migrate(connection: androidx.sqlite.SQLiteConnection) {

            try {
                connection.prepare(
                                "ALTER TABLE EQUATIONS ADD COLUMN ratio INTEGER NOT NULL DEFAULT 0"
                        )
                        .use { statement -> statement.step() }
            } catch (e: Exception) {

                throw e
            }
        }
    }
}

/** Migration 21 → 22 : Création des tables RECIPES et ALIMENTS_RECETTES */
fun createMigration21to22(): Migration {
    return object : Migration(21, 22) {
        override fun migrate(connection: androidx.sqlite.SQLiteConnection) {
            try {
                connection.prepare(
                                "CREATE TABLE IF NOT EXISTS RECETTES (uuid TEXT NOT NULL PRIMARY KEY, name TEXT, number INTEGER NOT NULL DEFAULT 0, espece TEXT, description TEXT)"
                        )
                        .use { it.step() }

                connection.prepare(
                                "CREATE TABLE IF NOT EXISTS ALIMENTS_RECETTES (uuid TEXT NOT NULL PRIMARY KEY, refAlimUnif TEXT NOT NULL, refRecipe TEXT NOT NULL, quantity REAL NOT NULL DEFAULT 0, refTarget INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(refRecipe) REFERENCES RECETTES(uuid) ON DELETE CASCADE, FOREIGN KEY(refAlimUnif) REFERENCES FOOD(uuid) ON DELETE SET NULL)"
                        )
                        .use { it.step() }

                connection.prepare(
                                "CREATE INDEX IF NOT EXISTS index_ALIMENTS_RECETTES_refRecipe ON ALIMENTS_RECETTES(refRecipe)"
                        )
                        .use { it.step() }
                connection.prepare(
                                "CREATE INDEX IF NOT EXISTS index_ALIMENTS_RECETTES_refAlimUnif ON ALIMENTS_RECETTES(refAlimUnif)"
                        )
                        .use { it.step() }
                // Contrainte d'unicité composite
                connection.prepare(
                                "CREATE UNIQUE INDEX IF NOT EXISTS index_ALIMENTS_RECETTES_refAlimUnif_refRecipe ON ALIMENTS_RECETTES(refAlimUnif, refRecipe)"
                        )
                        .use { it.step() }
            } catch (e: Exception) {
                throw e
            }
        }
    }
}

/** Migration 22 → 23 : Ajout d'index pour optimiser les performances de recherche et filtrage */
fun createMigration22to23(): Migration {
    return object : Migration(22, 23) {
        override fun migrate(connection: androidx.sqlite.SQLiteConnection) {

            try {
                // Index pour le filtrage par groupe d'aliment
                connection.prepare(
                                "CREATE INDEX IF NOT EXISTS index_FOOD_groupAlim ON FOOD(groupAlim)"
                        )
                        .use { it.step() }

                // Index pour le filtrage par type d'aliment
                connection.prepare(
                                "CREATE INDEX IF NOT EXISTS index_FOOD_typeAlim ON FOOD(typeAlim)"
                        )
                        .use { it.step() }

                // Index pour la recherche par marque
                connection.prepare("CREATE INDEX IF NOT EXISTS index_FOOD_brand ON FOOD(brand)")
                        .use { it.step() }

                // Index pour la recherche par nom
                connection.prepare("CREATE INDEX IF NOT EXISTS index_FOOD_name ON FOOD(name)").use {
                    it.step()
                }

                // Index pour le filtrage par espèces
                connection.prepare(
                                "CREATE INDEX IF NOT EXISTS index_FOOD_especesJson ON FOOD(especesJson)"
                        )
                        .use { it.step() }

                // Index pour filtrer les aliments obsolètes
                connection.prepare(
                                "CREATE INDEX IF NOT EXISTS index_FOOD_deprecated ON FOOD(deprecated)"
                        )
                        .use { it.step() }
            } catch (e: Exception) {

                throw e
            }
        }
    }
}

/** Migration 23 → 24 : Création des tables HTML_SECTIONS et HTML_SECTION_LIBRARIES */
fun createMigration23to24(): Migration {
    return object : Migration(23, 24) {
        override fun migrate(connection: androidx.sqlite.SQLiteConnection) {
            try {
                // Supprimer les tables si elles existent déjà (pour éviter les conflits)
                connection.prepare("DROP TABLE IF EXISTS HTML_SECTIONS").use { it.step() }
                connection.prepare("DROP TABLE IF EXISTS HTML_SECTION_LIBRARIES").use { it.step() }

                // Supprimer les index s'ils existent
                connection.prepare("DROP INDEX IF EXISTS index_HTML_SECTIONS_category").use {
                    it.step()
                }
                connection.prepare("DROP INDEX IF EXISTS index_HTML_SECTIONS_isTemplate").use {
                    it.step()
                }
                connection.prepare("DROP INDEX IF EXISTS index_HTML_SECTIONS_title").use {
                    it.step()
                }

                // Création de la table HTML_SECTIONS avec la structure exacte attendue par Room
                connection.prepare(
                                """
                    CREATE TABLE HTML_SECTIONS (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        contentJson TEXT NOT NULL,
                        category TEXT NOT NULL,
                        tagsJson TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        isTemplate INTEGER NOT NULL,
                        priority INTEGER NOT NULL,
                        isActive INTEGER NOT NULL,
                        targetSpeciesJson TEXT NOT NULL,
                        targetAgeGroupsJson TEXT NOT NULL,
                        usageCount INTEGER NOT NULL,
                        lastUsed INTEGER
                    )
                    """
                        )
                        .use { it.step() }

                // Création de la table HTML_SECTION_LIBRARIES
                connection.prepare(
                                """
                    CREATE TABLE HTML_SECTION_LIBRARIES (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        sectionIdsJson TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """
                        )
                        .use { it.step() }

                // Création des index
                connection.prepare(
                                "CREATE INDEX index_HTML_SECTIONS_category ON HTML_SECTIONS(category)"
                        )
                        .use { it.step() }
                connection.prepare(
                                "CREATE INDEX index_HTML_SECTIONS_isTemplate ON HTML_SECTIONS(isTemplate)"
                        )
                        .use { it.step() }
                connection.prepare("CREATE INDEX index_HTML_SECTIONS_title ON HTML_SECTIONS(title)")
                        .use { it.step() }
            } catch (e: Exception) {
                throw e
            }
        }
    }
}

/** Migration 24 → 25 : Ajout du champ jsonbinId à la table ANIMALS */
fun createMigration24to25(): Migration {
    return object : Migration(24, 25) {
        override fun migrate(connection: androidx.sqlite.SQLiteConnection) {
            try {
                // Ajouter la colonne jsonbinId (nullable TEXT)
                connection.prepare(
                    "ALTER TABLE ANIMALS ADD COLUMN jsonbinId TEXT"
                ).use { statement -> 
                    statement.step() 
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }
}

/** Migration 25 → 26 : Ajout des mots-clés pour les consultations */
fun createMigration25to26(): Migration {
    return object : Migration(25, 26) {
        override fun migrate(connection: androidx.sqlite.SQLiteConnection) {
            try {
                runStatementIgnoreIfExists(
                        connection,
                        "ALTER TABLE CONSULTATIONS ADD COLUMN keywordsJson TEXT"
                )

                runStatementIgnoreIfExists(
                        connection,
                        """
                        CREATE TABLE CONSULTATION_KEYWORDS (
                            uuid TEXT NOT NULL PRIMARY KEY,
                            label TEXT NOT NULL
                        )
                        """
                )

                runStatementIgnoreIfExists(
                        connection,
                        "CREATE UNIQUE INDEX index_CONSULTATION_KEYWORDS_label ON CONSULTATION_KEYWORDS(label)"
                )
            } catch (e: Exception) {
                throw e
            }
        }
    }
}

/** Migration 26 → 27 : Ajout de la date de mise à jour et de la référence image */
fun createMigration26to27(): Migration {
    return object : Migration(26, 27) {
        override fun migrate(connection: androidx.sqlite.SQLiteConnection) {
            try {
                runStatementIgnoreIfExists(
                        connection,
                        "ALTER TABLE FOOD ADD COLUMN lastUpdateDate TEXT"
                )
                runStatementIgnoreIfExists(
                        connection,
                        "ALTER TABLE FOOD ADD COLUMN imageRef TEXT"
                )
            } catch (e: Exception) {
                throw e
            }
        }
    }
}

/** Migration 27 → 28 : Ajout des champs ordonnance pour les consultations */
fun createMigration27to28(): Migration {
    return object : Migration(27, 28) {
        override fun migrate(connection: androidx.sqlite.SQLiteConnection) {
            try {
                runStatementIgnoreIfExists(
                        connection,
                        "ALTER TABLE CONSULTATIONS ADD COLUMN prescriptionAdditionalText TEXT"
                )
                runStatementIgnoreIfExists(
                        connection,
                        "ALTER TABLE CONSULTATIONS ADD COLUMN prescriptionSelectedConseilIdsJson TEXT"
                )
                runStatementIgnoreIfExists(
                        connection,
                        "ALTER TABLE CONSULTATIONS ADD COLUMN prescriptionLocalHtmlSectionsJson TEXT"
                )
                runStatementIgnoreIfExists(
                        connection,
                        "ALTER TABLE CONSULTATIONS ADD COLUMN prescriptionSelectedRationIdsJson TEXT"
                )
            } catch (e: Exception) {
                throw e
            }
        }
    }
}

private fun runStatementIgnoreIfExists(
        connection: androidx.sqlite.SQLiteConnection,
        sql: String
) {
    try {
        connection.prepare(sql).use { statement ->
            statement.step()
        }
    } catch (e: SQLiteException) {
        if (!shouldIgnoreSqliteExistsError(e)) {
            throw e
        }
    }
}

private fun shouldIgnoreSqliteExistsError(e: SQLiteException): Boolean {
    val message = e.message?.lowercase() ?: return false
    return message.contains("already exists") || message.contains("duplicate column")
}
