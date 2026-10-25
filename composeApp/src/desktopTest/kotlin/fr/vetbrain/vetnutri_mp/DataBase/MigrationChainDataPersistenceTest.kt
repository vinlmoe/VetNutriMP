package fr.vetbrain.vetnutri_mp.DataBase

import androidx.room.Room
import java.io.File
import java.nio.file.Files
import java.sql.DriverManager
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Vérifie que la chaîne complète de migrations Room (17 -> 41, cf. `getRoomDatabase` dans
 * AppDatabase.kt) ne perd aucune donnée existante lors de l'ouverture d'une base réelle et
 * ancienne, comme le vivrait un utilisateur qui met à jour l'application après plusieurs
 * versions sans jamais l'avoir relancée entre-temps.
 *
 * La base de départ est construite en SQL brut (via le driver JDBC xerial déjà utilisé côté
 * desktop) à partir du schéma exporté par Room pour la version 17
 * (`composeApp/schemas/.../17.json`), colonne pour colonne. On évite ainsi toute dépendance à
 * `androidx.room:room-testing` (artefact hébergé exclusivement sur le dépôt Google Maven, non
 * utilisé ailleurs dans le projet) : on exerce directement `getRoomDatabase`, la fonction
 * réellement utilisée en production.
 */
class MigrationChainDataPersistenceTest {

    private lateinit var tempDir: File
    private lateinit var dbPath: String

    @BeforeTest
    fun setUp() {
        tempDir = Files.createTempDirectory("vetnutri-migration-test").toFile()
        dbPath = File(tempDir, "vetnutri.db").absolutePath
    }

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    /** Schéma exact de la version 17 de AppDatabase, extrait de schemas/17.json. */
    private val v17SchemaSql: List<String> = listOf(
        "CREATE TABLE IF NOT EXISTS `ANIMALS` (`uuid` TEXT NOT NULL, `nom` TEXT, `dead` INTEGER NOT NULL, `id` TEXT, `sexId` INTEGER NOT NULL, `specieId` TEXT, `ownerName` TEXT, `birthdate` TEXT, `race` TEXT, `summary` TEXT, PRIMARY KEY(`uuid`))",
        "CREATE TABLE IF NOT EXISTS `CONSULTATIONS` (`uuid` TEXT NOT NULL, `idAnim` TEXT NOT NULL, `date` TEXT, `objectConsult` TEXT, `observation` TEXT, `cRendu` TEXT, `weight` REAL NOT NULL, `idealWeight` REAL NOT NULL, `water` REAL NOT NULL, `bodyFat` REAL NOT NULL, `methodAnalysis` TEXT, `BCS` INTEGER NOT NULL, `k1Id` TEXT, `k1Value` REAL NOT NULL, `k2Id` TEXT, `k2Value` REAL NOT NULL, `k3Id` TEXT, `k3Value` REAL NOT NULL, `k4Id` TEXT, `k4Value` REAL NOT NULL, `k5Id` TEXT, `k5Value` REAL NOT NULL, `nLittle` INTEGER NOT NULL, `pAdult` REAL NOT NULL, `coefGes` INTEGER NOT NULL, `coefLact` INTEGER NOT NULL, `MCS` INTEGER NOT NULL, `referenceGeneraleId` TEXT, `referencesMaladiesJson` TEXT, `coefficientAjustement` REAL NOT NULL, PRIMARY KEY(`uuid`), FOREIGN KEY(`idAnim`) REFERENCES `ANIMALS`(`uuid`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        "CREATE INDEX IF NOT EXISTS `index_CONSULTATIONS_idAnim` ON `CONSULTATIONS` (`idAnim`)",
        "CREATE TABLE IF NOT EXISTS `WEIGHT` (`uuid` TEXT NOT NULL, `refAnimal` TEXT NOT NULL, `date` TEXT NOT NULL, `value` REAL NOT NULL, PRIMARY KEY(`uuid`), FOREIGN KEY(`refAnimal`) REFERENCES `ANIMALS`(`uuid`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        "CREATE INDEX IF NOT EXISTS `index_WEIGHT_refAnimal` ON `WEIGHT` (`refAnimal`)",
        "CREATE TABLE IF NOT EXISTS `RATIONS` (`uuid` TEXT NOT NULL, `idConsult` TEXT NOT NULL, `name` TEXT, `coef` REAL NOT NULL, `actual` INTEGER NOT NULL, `number` INTEGER NOT NULL, `espece` TEXT, `recette` INTEGER NOT NULL, `description` TEXT, PRIMARY KEY(`uuid`), FOREIGN KEY(`idConsult`) REFERENCES `CONSULTATIONS`(`uuid`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        "CREATE INDEX IF NOT EXISTS `index_RATIONS_idConsult` ON `RATIONS` (`idConsult`)",
        "CREATE TABLE IF NOT EXISTS `ALIMENTS` (`uuid` TEXT NOT NULL, `refAlimUnif` TEXT NOT NULL, `refRation` TEXT NOT NULL, `quantity` REAL NOT NULL, `refTarget` INTEGER NOT NULL, PRIMARY KEY(`uuid`), FOREIGN KEY(`refRation`) REFERENCES `RATIONS`(`uuid`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`refAlimUnif`) REFERENCES `FOOD`(`uuid`) ON UPDATE NO ACTION ON DELETE SET NULL )",
        "CREATE INDEX IF NOT EXISTS `index_ALIMENTS_refRation` ON `ALIMENTS` (`refRation`)",
        "CREATE INDEX IF NOT EXISTS `index_ALIMENTS_refAlimUnif` ON `ALIMENTS` (`refAlimUnif`)",
        "CREATE UNIQUE INDEX IF NOT EXISTS `index_ALIMENTS_refAlimUnif_refRation` ON `ALIMENTS` (`refAlimUnif`, `refRation`)",
        "CREATE TABLE IF NOT EXISTS `ESPECES_ALIMENTS` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `refAliment` TEXT NOT NULL, `espece` TEXT NOT NULL, FOREIGN KEY(`refAliment`) REFERENCES `FOOD`(`uuid`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        "CREATE INDEX IF NOT EXISTS `index_ESPECES_ALIMENTS_refAliment` ON `ESPECES_ALIMENTS` (`refAliment`)",
        "CREATE TABLE IF NOT EXISTS `INDICATIONS_ALIMENTS` (`refAliment` TEXT NOT NULL, `indication` INTEGER NOT NULL, PRIMARY KEY(`refAliment`, `indication`), FOREIGN KEY(`refAliment`) REFERENCES `FOOD`(`uuid`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        "CREATE INDEX IF NOT EXISTS `index_INDICATIONS_ALIMENTS_refAliment` ON `INDICATIONS_ALIMENTS` (`refAliment`)",
        "CREATE TABLE IF NOT EXISTS `SUPPLEMENTAL_VARIABLES` (`idConsult` TEXT NOT NULL, `variableKind` INTEGER NOT NULL, `value` REAL NOT NULL, PRIMARY KEY(`idConsult`, `variableKind`), FOREIGN KEY(`idConsult`) REFERENCES `CONSULTATIONS`(`uuid`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        "CREATE INDEX IF NOT EXISTS `index_SUPPLEMENTAL_VARIABLES_idConsult` ON `SUPPLEMENTAL_VARIABLES` (`idConsult`)",
        "CREATE TABLE IF NOT EXISTS `FOOD` (`uuid` TEXT NOT NULL, `groupAlim` INTEGER NOT NULL, `typeAlim` INTEGER NOT NULL, `ingredients` TEXT NOT NULL, `price` REAL NOT NULL, `categPrice` TEXT NOT NULL, `brand` TEXT NOT NULL, `gamme` TEXT NOT NULL, `cont` TEXT NOT NULL, `unitPres` INTEGER NOT NULL, `quantityPres` REAL NOT NULL, `version` INTEGER NOT NULL, `date` TEXT NOT NULL, `nameDef` TEXT NOT NULL, `consistent` INTEGER NOT NULL, `deprecated` INTEGER NOT NULL, `DataB` TEXT NOT NULL, `RefRation` TEXT, `RefAlimUnif` TEXT, `especesJson` TEXT, `indicationsJson` TEXT, `name` TEXT, `quantite` REAL NOT NULL, PRIMARY KEY(`uuid`), FOREIGN KEY(`RefRation`) REFERENCES `RATIONS`(`uuid`) ON UPDATE NO ACTION ON DELETE SET NULL )",
        "CREATE INDEX IF NOT EXISTS `index_FOOD_RefRation` ON `FOOD` (`RefRation`)",
        "CREATE UNIQUE INDEX IF NOT EXISTS `index_FOOD_uuid` ON `FOOD` (`uuid`)",
        "CREATE TABLE IF NOT EXISTS `NUTRIENT_VALUES` (`refAliment` TEXT NOT NULL, `nutrientLabel` TEXT NOT NULL, `value` REAL NOT NULL, PRIMARY KEY(`refAliment`, `nutrientLabel`), FOREIGN KEY(`refAliment`) REFERENCES `FOOD`(`uuid`) ON UPDATE NO ACTION ON DELETE NO ACTION )",
        "CREATE INDEX IF NOT EXISTS `index_NUTRIENT_VALUES_refAliment` ON `NUTRIENT_VALUES` (`refAliment`)",
        "CREATE TABLE IF NOT EXISTS `BIBLIO_REFS` (`uuid` TEXT NOT NULL, `firstAuthor` TEXT NOT NULL, `year` INTEGER NOT NULL, `completeRef` TEXT NOT NULL, `comments` TEXT NOT NULL, `bibtex` TEXT NOT NULL, `consistent` INTEGER NOT NULL, PRIMARY KEY(`uuid`))",
        "CREATE TABLE IF NOT EXISTS `EQUATIONS` (`uuid` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `equationScript` TEXT NOT NULL, `specie` TEXT, `kind` TEXT NOT NULL, `consistent` INTEGER NOT NULL, `bibRef` TEXT, `variables` TEXT NOT NULL, PRIMARY KEY(`uuid`), FOREIGN KEY(`bibRef`) REFERENCES `BIBLIO_REFS`(`uuid`) ON UPDATE NO ACTION ON DELETE SET NULL )",
        "CREATE INDEX IF NOT EXISTS `index_EQUATIONS_bibRef` ON `EQUATIONS` (`bibRef`)",
        "CREATE UNIQUE INDEX IF NOT EXISTS `index_EQUATIONS_uuid` ON `EQUATIONS` (`uuid`)",
        "CREATE TABLE IF NOT EXISTS `REFERENCE_EV` (`uuid` TEXT NOT NULL, `nom` TEXT NOT NULL, `description` TEXT NOT NULL, `maladie` INTEGER NOT NULL, `nomMaladie` TEXT NOT NULL, `nomEnergie` TEXT NOT NULL, `consistent` INTEGER NOT NULL, `espece` TEXT NOT NULL, `stadePhysio` TEXT NOT NULL, `nomk1` TEXT NOT NULL, `nomk2` TEXT NOT NULL, `nomk3` TEXT NOT NULL, `nomk4` TEXT NOT NULL, `nomk5` TEXT NOT NULL, PRIMARY KEY(`uuid`))",
        "CREATE UNIQUE INDEX IF NOT EXISTS `index_REFERENCE_EV_uuid` ON `REFERENCE_EV` (`uuid`)",
        "CREATE TABLE IF NOT EXISTS `REFERENCE_EV_EQUATIONS` (`referenceEvId` TEXT NOT NULL, `equationId` TEXT NOT NULL, `equationType` TEXT NOT NULL, PRIMARY KEY(`referenceEvId`, `equationType`), FOREIGN KEY(`referenceEvId`) REFERENCES `REFERENCE_EV`(`uuid`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`equationId`) REFERENCES `EQUATIONS`(`uuid`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        "CREATE INDEX IF NOT EXISTS `index_REFERENCE_EV_EQUATIONS_referenceEvId` ON `REFERENCE_EV_EQUATIONS` (`referenceEvId`)",
        "CREATE INDEX IF NOT EXISTS `index_REFERENCE_EV_EQUATIONS_equationId` ON `REFERENCE_EV_EQUATIONS` (`equationId`)",
        "CREATE TABLE IF NOT EXISTS `REFERENCE_EV_COEFFICIENTS` (`uuid` TEXT NOT NULL, `referenceEvId` TEXT NOT NULL, `groupType` TEXT NOT NULL, `description` TEXT NOT NULL, `coef` REAL NOT NULL, `groupUUID` INTEGER NOT NULL, PRIMARY KEY(`uuid`), FOREIGN KEY(`referenceEvId`) REFERENCES `REFERENCE_EV`(`uuid`) ON UPDATE NO ACTION ON DELETE CASCADE )",
        "CREATE INDEX IF NOT EXISTS `index_REFERENCE_EV_COEFFICIENTS_referenceEvId` ON `REFERENCE_EV_COEFFICIENTS` (`referenceEvId`)",
        "CREATE TABLE IF NOT EXISTS `REFERENCE_EV_NUTRIENTS` (`uuid` TEXT NOT NULL, `referenceEvId` TEXT NOT NULL, `nutrientCode` TEXT NOT NULL, `reflevel` TEXT NOT NULL, `quantite` REAL NOT NULL, `uniteId` INTEGER NOT NULL, `uniteReqId` INTEGER NOT NULL, `biblioRefId` TEXT, PRIMARY KEY(`uuid`), FOREIGN KEY(`referenceEvId`) REFERENCES `REFERENCE_EV`(`uuid`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`biblioRefId`) REFERENCES `BIBLIO_REFS`(`uuid`) ON UPDATE NO ACTION ON DELETE SET NULL )",
        "CREATE INDEX IF NOT EXISTS `index_REFERENCE_EV_NUTRIENTS_referenceEvId` ON `REFERENCE_EV_NUTRIENTS` (`referenceEvId`)",
        "CREATE INDEX IF NOT EXISTS `index_REFERENCE_EV_NUTRIENTS_biblioRefId` ON `REFERENCE_EV_NUTRIENTS` (`biblioRefId`)",
    )

    /** Construit un fichier SQLite v17 "réaliste" en SQL brut, avec quelques lignes de données. */
    private fun seedV17Database() {
        Class.forName("org.sqlite.JDBC")
        DriverManager.getConnection("jdbc:sqlite:$dbPath").use { conn ->
            conn.createStatement().use { stmt ->
                v17SchemaSql.forEach { sql -> stmt.execute(sql) }

                stmt.execute(
                    "INSERT INTO ANIMALS (uuid, nom, dead, id, sexId, specieId, ownerName, birthdate, race, summary) " +
                        "VALUES ('animal-1', 'Rex', 0, NULL, 0, 'CHIEN', 'Jean Dupont', '2020-01-01', 'Labrador', NULL)"
                )

                stmt.execute(
                    "INSERT INTO FOOD (uuid, groupAlim, typeAlim, ingredients, price, categPrice, brand, gamme, cont, unitPres, quantityPres, version, date, nameDef, consistent, deprecated, DataB, RefRation, RefAlimUnif, especesJson, indicationsJson, name, quantite) " +
                        "VALUES ('food-1', 0, 0, 'Poulet, riz', 12.5, 'i', 'MarqueTest', 'GammeTest', 'NO', 0, 0.0, 1, '', 'Croquettes historiques', 0, 0, '6', NULL, NULL, '[]', '[]', 'Croquettes historiques', 0.0)"
                )

                stmt.execute(
                    "INSERT INTO NUTRIENT_VALUES (refAliment, nutrientLabel, value) VALUES ('food-1', 'Énergie', 350.0)"
                )
                stmt.execute(
                    "INSERT INTO NUTRIENT_VALUES (refAliment, nutrientLabel, value) VALUES ('food-1', 'PROT', 25.0)"
                )

                stmt.execute(
                    "INSERT INTO ESPECES_ALIMENTS (refAliment, espece) VALUES ('food-1', 'CHIEN')"
                )

                stmt.execute("PRAGMA user_version = 17")
            }
        }
    }

    @Test
    fun getRoomDatabase_migratingFromV17_preservesAllExistingData() = runTest {
        seedV17Database()

        val db = getRoomDatabase(Room.databaseBuilder<AppDatabase>(name = dbPath), dbPath)

        val animal = db.animalDao().getAnimalById("animal-1")
        assertNotNull(animal, "l'animal existant avant migration doit survivre à la chaîne 17 -> 41")
        assertEquals("Rex", animal.nom)
        assertEquals("Jean Dupont", animal.ownerName)

        val food = db.foodDao().getFoodById("food-1")
        assertNotNull(food, "l'aliment existant avant migration doit survivre à la chaîne 17 -> 41")
        assertEquals("Croquettes historiques", food.nameDef)
        assertEquals("MarqueTest", food.brand)

        val nutrients = db.nutrientValueDao().getNutrientValues("food-1")
        assertEquals(2, nutrients.size, "les valeurs nutritionnelles doivent survivre à la recréation de NUTRIENT_VALUES (migration 32->33)")
        assertTrue(nutrients.any { it.nutrientLabel == "Énergie" && it.value == 350.0 })
        assertTrue(nutrients.any { it.nutrientLabel == "PROT" && it.value == 25.0 })

        assertTrue(db.checkIntegrity(), "la base migrée doit rester structurellement saine")

        db.close()
    }

    @Test
    fun getRoomDatabase_migratingFromV17_backfillsEnergyPerSpeciesFromLegacyData() = runTest {
        // La migration 35->36 doit propager rétroactivement l'énergie générique historique
        // (NUTRIENT_VALUES.'Énergie') vers ENERGY_PER_SPECIES pour chaque espèce déclarée.
        seedV17Database()

        val db = getRoomDatabase(Room.databaseBuilder<AppDatabase>(name = dbPath), dbPath)

        val energyPerSpecies = db.energyPerSpeciesDao().getForAliment("food-1")
        assertEquals(1, energyPerSpecies.size)
        assertEquals("CHIEN", energyPerSpecies.first().espece)
        assertEquals(350.0, energyPerSpecies.first().value)

        db.close()
    }

    @Test
    fun getRoomDatabase_migratingFromV17_createsMultiRationExplorationsTable() = runTest {
        // La migration 39->40 crée la table des configurations d'exploration multiration
        seedV17Database()

        val db = getRoomDatabase(Room.databaseBuilder<AppDatabase>(name = dbPath), dbPath)
        val dao = db.multiRationExplorationDao()
        dao.upsert(MultiRationExplorationEntity("expl-1", "Chats adultes", "CHAT", "{}", 1L))
        dao.upsert(MultiRationExplorationEntity("expl-1", "Chats adultes v2", "CHAT", "{\"version\":1}", 2L))

        val enregistree = dao.getById("expl-1")
        assertNotNull(enregistree)
        assertEquals("Chats adultes v2", enregistree.nom)
        assertEquals(1, dao.getAll().size)
        dao.deleteById("expl-1")
        assertTrue(dao.getAll().isEmpty())
        assertTrue(db.checkIntegrity())

        db.close()
    }

    @Test
    fun getRoomDatabase_migratingFromV17_storesHerdTypesAndParameters() = runTest {
        // La migration 40->41 ajoute les types d'un troupeau (ANIMALS) et leurs paramètres par
        // consultation (CONSULTATIONS) ; un animal existant reste un individu
        seedV17Database()

        val db = getRoomDatabase(Room.databaseBuilder<AppDatabase>(name = dbPath), dbPath)
        val existant = db.animalDao().getAllAnimals().single { it.uuid == "animal-1" }
        assertEquals(null, existant.herdTypesJson)

        val typesJson = "[{\"id\":\"t1\",\"nom\":\"Vaches\",\"nombre\":40,\"poids\":650.0}]"
        db.animalDao().update(existant.copy(herdTypesJson = typesJson))
        assertEquals(typesJson, db.animalDao().getAllAnimals().single { it.uuid == "animal-1" }.herdTypesJson)

        val paramsJson = "[{\"typeId\":\"t1\",\"nombre\":38,\"poids\":640.0,\"referenceId\":null,\"k\":1.1,\"variables\":{}}]"
        db.consultationDao().insert(
                ConsultationEntity(
                        uuid = "consult-herd", idAnim = "animal-1", date = "2026-10-10", objectConsult = null,
                        observation = null, cRendu = null, methodAnalysis = null, k1Id = null, k2Id = null,
                        k3Id = null, k4Id = null, k5Id = null, herdParamsJson = paramsJson
                )
        )
        assertEquals(paramsJson, db.consultationDao().getConsultationById("consult-herd")?.herdParamsJson)
        assertTrue(db.checkIntegrity())

        db.close()
    }

    /** Colonnes d'une table, lues en SQL brut (JDBC) dans le fichier migré, hors de Room. */
    private fun colonnes(table: String): Set<String> {
        Class.forName("org.sqlite.JDBC")
        DriverManager.getConnection("jdbc:sqlite:$dbPath").use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("PRAGMA table_info(`$table`)")
                val noms = mutableSetOf<String>()
                while (rs.next()) noms += rs.getString("name")
                return noms
            }
        }
    }

    private fun objetsSqlite(type: String): Set<String> {
        Class.forName("org.sqlite.JDBC")
        DriverManager.getConnection("jdbc:sqlite:$dbPath").use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type = '$type'")
                val noms = mutableSetOf<String>()
                while (rs.next()) noms += rs.getString("name")
                return noms
            }
        }
    }

    private fun scalaire(sql: String): String? {
        Class.forName("org.sqlite.JDBC")
        DriverManager.getConnection("jdbc:sqlite:$dbPath").use { conn ->
            conn.createStatement().use { stmt ->
                val rs = stmt.executeQuery(sql)
                return if (rs.next()) rs.getString(1) else null
            }
        }
    }

    @Test
    fun getRoomDatabase_migratingFromV17_appliesMigrations37to41Explicitly() = runTest {
        // Les cinq dernières migrations (36→37 … 40→41) : colonnes et tables introduites, valeurs
        // par défaut des lignes préexistantes, version finale du fichier.
        seedV17Database()
        Class.forName("org.sqlite.JDBC")
        DriverManager.getConnection("jdbc:sqlite:$dbPath").use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute(
                    "INSERT INTO CONSULTATIONS (uuid, idAnim, date, objectConsult, observation, cRendu, weight, idealWeight, water, bodyFat, methodAnalysis, BCS, k1Id, k1Value, k2Id, k2Value, k3Id, k3Value, k4Id, k4Value, k5Id, k5Value, nLittle, pAdult, coefGes, coefLact, MCS, referenceGeneraleId, referencesMaladiesJson, coefficientAjustement) " +
                        "VALUES ('consult-17', 'animal-1', '2021-05-05', 'Bilan', NULL, NULL, 30.0, 0.0, 0.0, 0.0, NULL, 5, NULL, 0.0, NULL, 0.0, NULL, 0.0, NULL, 0.0, NULL, 0.0, 0, 0.0, 0, 0, 0, NULL, NULL, 1.0)"
                )
                stmt.execute(
                    "INSERT INTO RATIONS (uuid, idConsult, name, coef, actual, number, espece, recette, description) " +
                        "VALUES ('ration-17', 'consult-17', 'Actuelle', 1.0, 1, 1, 'CHIEN', 0, '')"
                )
            }
        }

        val db = getRoomDatabase(Room.databaseBuilder<AppDatabase>(name = dbPath), dbPath)
        // Les lignes d'avant migration restent lisibles par Room avec les valeurs par défaut
        val consultation = db.consultationDao().getConsultationById("consult-17")
        assertNotNull(consultation, "la consultation v17 doit survivre aux migrations 36→41")
        assertEquals("STANDARD", consultation.typeConsultation, "36→37 : type de consultation par défaut")
        assertEquals(null, consultation.profilEvolutif, "36→37 : pas de profil évolutif")
        assertEquals(null, consultation.herdParamsJson, "40→41 : pas de paramètres de troupeau")
        val ration = db.consultationDao().getRationsForConsultation("consult-17").single()
        assertEquals(false, ration.etapeEvolutive, "36→37 : une ration existante n'est pas une étape")
        assertEquals(null, ration.poids, "36→37 : pas de poids d'étape")
        assertEquals(null, ration.refRationParente, "37→38 : pas de ration parente")
        assertEquals(null, ration.nomLibre, "38→39 : pas de nom libre")
        assertTrue(db.checkIntegrity())
        db.close()

        // Schéma du fichier migré, vérifié hors de Room
        assertEquals("41", scalaire("PRAGMA user_version"), "version finale du schéma")
        val consultations = colonnes("CONSULTATIONS")
        assertTrue(consultations.containsAll(listOf("typeConsultation", "profilEvolutif")), "36→37 : $consultations")
        assertTrue("herdParamsJson" in consultations, "40→41 : $consultations")
        val rations = colonnes("RATIONS")
        assertTrue(rations.containsAll(listOf("etapeEvolutive", "poids")), "36→37 : $rations")
        assertTrue("refRationParente" in rations, "37→38 : $rations")
        assertTrue("nomLibre" in rations, "38→39 : $rations")
        assertTrue("herdTypesJson" in colonnes("ANIMALS"), "40→41 : ${colonnes("ANIMALS")}")
        assertEquals(setOf("idRation", "variableKind", "value"), colonnes("RATION_SUPPLEMENTAL_VARIABLES"), "36→37 : table des variables d'étape")
        assertEquals(
            setOf("uuid", "nom", "espece", "configurationJson", "updatedAt"),
            colonnes("MULTI_RATION_EXPLORATIONS"),
            "39→40 : table des explorations multiration"
        )
        val tables = objetsSqlite("table")
        assertTrue(tables.containsAll(listOf("RATION_SUPPLEMENTAL_VARIABLES", "MULTI_RATION_EXPLORATIONS")), "tables : $tables")
        assertTrue("index_RATION_SUPPLEMENTAL_VARIABLES_idRation" in objetsSqlite("index"), "36→37 : index des variables d'étape")
        assertEquals("STANDARD", scalaire("SELECT typeConsultation FROM CONSULTATIONS WHERE uuid = 'consult-17'"))
        assertEquals("0", scalaire("SELECT etapeEvolutive FROM RATIONS WHERE uuid = 'ration-17'"))
    }
}
