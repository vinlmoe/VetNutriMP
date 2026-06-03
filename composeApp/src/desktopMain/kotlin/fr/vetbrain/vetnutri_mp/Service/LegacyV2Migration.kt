package fr.vetbrain.vetnutri_mp.Service

import fr.vetbrain.vetnutri_mp.DataBase.AlimentRationEntity
import fr.vetbrain.vetnutri_mp.DataBase.AnimalEntity
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.DataBase.ConsultationEntity
import fr.vetbrain.vetnutri_mp.DataBase.FoodEntity
import fr.vetbrain.vetnutri_mp.DataBase.NutrientValueEntity
import fr.vetbrain.vetnutri_mp.DataBase.RationEntity
import fr.vetbrain.vetnutri_mp.DataBase.WeightEntity
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.ViewModel.LegacyMigrationViewModel
import kotlinx.coroutines.withContext
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

// ---- Detector ----------------------------------------------------------------

object LegacyV2Detector {
    fun findDbFolder(): String? {
        val home = System.getProperty("user.home") ?: return null
        val appdata = System.getenv("APPDATA") ?: ""
        val candidates = listOfNotNull(
            File("db"),
            File("../db"),
            File("$home/VetNutri2/db"),
            File("$home/VetNutri/db"),
            File("$home/vetnutri-2-final/db"),
            File("$home/Documents/VetNutri2/db"),
            File("$home/Documents/VetNutri/db"),
            File("$home/Desktop/VetNutri2/db"),
            if (appdata.isNotBlank()) File("$appdata/VetNutri2/db") else null,
            File("C:/Program Files/VetNutri2/db"),
            File("C:/VetNutri2/db"),
        )
        return candidates.firstOrNull { isV2DbFolder(it) }?.absolutePath
    }

    fun isV2DbFolder(folder: File): Boolean =
        folder.isDirectory && File(folder, "Data-Anim.db").exists()
}

// ---- Nutrient label maps (V2 enum index → VetNutriMP label) ----------------

private val BASE_LABELS = arrayOf(
    "HUMIDITE", "PROTEINE", "LIPIDE", "ENA", "CELLULOSE", "CENDRE",
    "SUCRE", "AMIDON", "FIBRESOL", "FIBRETOT", "NDF", "ADF"
)
private val AA_LABELS = arrayOf(
    "ALANINE", "ARGININE", "ASPARAGINE", "ASPARATE", "CYSTEINE",
    "GLUTAMATE", "GLUTAMINE", "GLYCINE", "HISTIDINE", "ISOLEUCINE",
    "LEUCINE", "LYSINE", "METHIONINE", "PHENYLALANINE", "PROLINE",
    "PYRROLYSINE", "SELENOCYSTEINE", "SERINE", "THREONINE", "TRYPTOPHANE",
    "TYROSINE", "VALINE"
)
private val MACRO_LABELS = arrayOf("CAL", "PHOS", "MG", "NA", "K", "CHL")
private val MIN_LABELS = arrayOf("FE", "CU", "ZN", "MN", "I", "SE")
private val VITAM_LABELS = arrayOf(
    "VITA", "VITC", "VITD", "VITE", "VITK", "VITB1", "VITB2", "VITB3",
    "VITB5", "VITB6", "VITB8", "VITB9", "VITB12", "CHOLINE", "RETINOL", "BETACAR"
)
private val LIPID_LABELS = arrayOf(
    "AGSATURE", "AGMONO", "AGPOLY", "AG40", "AG60", "AG80", "AG100",
    "AG120", "AG140", "AG160", "AG180", "AG181", "AG182", "AG183",
    "AG204", "AG205", "AG226", "CHOL", "O3", "O6", "EPADHA"
)
private val OTHER_LABELS = arrayOf(
    "TAURINE", "CARNITINE", "FOS", "MOS", "SUCR", "FRUCT", "LACT",
    "MALT", "AcOx", "GAL", "GLUCOSE", "DEXTROSE"
)

private val NUTRIENT_TABLE_MAP = mapOf(
    "VALUEBASE" to BASE_LABELS,
    "VALUEAA" to AA_LABELS,
    "VALUEMACRO" to MACRO_LABELS,
    "VALUEMIN" to MIN_LABELS,
    "VALUEVITAM" to VITAM_LABELS,
    "VALUELIPID" to LIPID_LABELS,
    "VALUEOTHER" to OTHER_LABELS
)

// V2 specie int (getCategorie()) → VetNutriMP Espece.label
private val SPECIE_MAP = mapOf(
    0 to "DOG", 1 to "CAT", 2 to "ALL", 3 to "PRIMATE",
    4 to "RAT", 5 to "SOURIS", 6 to "FURET", 7 to "LAPIN",
    8 to "CHEVAL", 9 to "FELIN", 10 to "CANIN", 11 to "HERBIVORE", 12 to "FOLIVORE"
)

// ---- JDBC helpers -----------------------------------------------------------

private fun connectV2(dbFile: File): Connection {
    Class.forName("org.sqlite.JDBC")
    return DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
}

private fun Connection.queryAll(sql: String): List<Map<String, Any?>> {
    val results = mutableListOf<Map<String, Any?>>()
    createStatement().executeQuery(sql).use { rs ->
        val meta = rs.metaData
        val colCount = meta.columnCount
        val colNames = (1..colCount).map { meta.getColumnLabel(it) }
        while (rs.next()) {
            val row = mutableMapOf<String, Any?>()
            colNames.forEachIndexed { idx, name -> row[name] = rs.getObject(idx + 1) }
            results.add(row)
        }
    }
    return results
}

private fun Connection.count(table: String): Int {
    createStatement().executeQuery("SELECT COUNT(*) FROM $table").use { rs ->
        return if (rs.next()) rs.getInt(1) else 0
    }
}

private fun Connection.tableExists(table: String): Boolean = runCatching {
    createStatement().executeQuery(
        "SELECT name FROM sqlite_master WHERE type='table' AND name='$table'"
    ).use { rs -> rs.next() }
}.getOrDefault(false)

// ---- Preview ----------------------------------------------------------------

suspend fun previewV2Migration(dbFolderPath: String): LegacyMigrationViewModel.MigrationCounts =
    withContext(AppDispatchers.IO) {
        val folder = File(dbFolderPath)
        val animDb = File(folder, "Data-Anim.db")
        val foodDb = File(folder, "Data-Food.db")

        var animals = 0; var consultations = 0; var rations = 0; var weights = 0; var foods = 0

        if (animDb.exists()) {
            connectV2(animDb).use { conn ->
                if (conn.tableExists("ANIMALS")) animals = conn.count("ANIMALS")
                if (conn.tableExists("CONSULTATIONS")) consultations = conn.count("CONSULTATIONS")
                if (conn.tableExists("RATION")) rations = conn.count("RATION")
                if (conn.tableExists("Weight")) weights = conn.count("Weight")
            }
        }
        if (foodDb.exists()) {
            connectV2(foodDb).use { conn ->
                if (conn.tableExists("FOOD"))
                    foods = conn.createStatement()
                        .executeQuery("SELECT COUNT(*) FROM FOOD WHERE (RefRation IS NULL OR RefRation = '')")
                        .use { rs -> if (rs.next()) rs.getInt(1) else 0 }
            }
        }

        LegacyMigrationViewModel.MigrationCounts(animals, consultations, rations, weights, foods)
    }

// ---- Migration ---------------------------------------------------------------

suspend fun runV2Migration(
    dbFolderPath: String,
    appDatabase: AppDatabase,
    onLog: suspend (String) -> Unit
): LegacyMigrationViewModel.MigrationResult = withContext(AppDispatchers.IO) {
    val folder = File(dbFolderPath)
    val animDb = File(folder, "Data-Anim.db")
    val foodDb = File(folder, "Data-Food.db")

    var impAnimals = 0; var skipAnimals = 0
    var impConsults = 0; var skipConsults = 0
    var impRations = 0; var skipRations = 0
    var impWeights = 0; var skipWeights = 0
    var impFoods = 0; var skipFoods = 0
    val errors = mutableListOf<String>()

    val animalDao = appDatabase.animalDao()
    val consultationDao = appDatabase.consultationDao()
    val foodDao = appDatabase.foodDao()
    val nutrientValueDao = appDatabase.nutrientValueDao()

    // --- Animaux + consultations + rations + poids ---
    if (animDb.exists()) {
        onLog("Lecture de Data-Anim.db...")
        connectV2(animDb).use { conn ->

            // 1. ANIMALS
            if (conn.tableExists("ANIMALS")) {
                val rows = conn.queryAll("SELECT * FROM ANIMALS")
                onLog("${rows.size} animaux trouvés")
                rows.forEach { row ->
                    try {
                        val uuid = row["UUID"] as? String ?: return@forEach
                        if (animalDao.getAnimalById(uuid) != null) {
                            skipAnimals++; return@forEach
                        }
                        val specieInt = (row["specie"] as? Number)?.toInt()
                        val specieId = specieInt?.let { SPECIE_MAP[it] } ?: (row["specie"] as? String)
                        val entity = AnimalEntity(
                            uuid = uuid,
                            nom = row["name"] as? String,
                            dead = (row["dead"] as? Number)?.toInt() == 1 || row["dead"] == true,
                            id = row["id"] as? String,
                            sexId = (row["sex"] as? Number)?.toInt() ?: 0,
                            specieId = specieId,
                            ownerName = row["ownerName"] as? String,
                            birthdate = row["birthdate"] as? String,
                            race = row["race"] as? String,
                            summary = row["summary"] as? String
                        )
                        animalDao.insert(entity)
                        impAnimals++
                    } catch (e: Exception) {
                        errors.add("Animal ${row["UUID"]}: ${e.message}")
                    }
                }
                onLog("Animaux importés: $impAnimals, ignorés: $skipAnimals")
            }

            // 2. Weight
            if (conn.tableExists("Weight")) {
                val rows = conn.queryAll("SELECT * FROM Weight")
                rows.forEach { row ->
                    try {
                        val uuid = row["UUID"] as? String ?: return@forEach
                        val entity = WeightEntity(
                            uuid = uuid,
                            refAnimal = row["refAnimal"] as? String ?: return@forEach,
                            date = row["date"] as? String ?: "",
                            value = (row["value"] as? Number)?.toDouble() ?: 0.0
                        )
                        // Check via direct insert - WeightEntity uses REPLACE strategy
                        animalDao.insertWeight(entity)
                        impWeights++
                    } catch (e: Exception) {
                        errors.add("Poids ${row["UUID"]}: ${e.message}")
                    }
                }
                onLog("Poids importés: $impWeights")
            }

            // 3. CONSULTATIONS
            if (conn.tableExists("CONSULTATIONS")) {
                val rows = conn.queryAll("SELECT * FROM CONSULTATIONS")
                onLog("${rows.size} consultations trouvées")
                rows.forEach { row ->
                    try {
                        val uuid = row["UUID"] as? String ?: return@forEach
                        if (consultationDao.getConsultationById(uuid) != null) {
                            skipConsults++; return@forEach
                        }
                        val entity = ConsultationEntity(
                            uuid = uuid,
                            idAnim = row["idAnim"] as? String ?: return@forEach,
                            date = row["date"] as? String,
                            objectConsult = (row["objet"] ?: row["object"]) as? String,
                            observation = row["observation"] as? String,
                            cRendu = row["cRendu"] as? String,
                            weight = (row["weight"] as? Number)?.toDouble() ?: 0.0,
                            idealWeight = (row["idealWeight"] as? Number)?.toDouble() ?: 0.0,
                            water = (row["water"] as? Number)?.toDouble() ?: 0.0,
                            bodyFat = (row["bodyFat"] as? Number)?.toDouble() ?: 0.0,
                            methodAnalysis = row["methodAnalysis"] as? String,
                            BCS = (row["BCS"] as? Number)?.toInt() ?: 0,
                            k1Id = row["k1Id"] as? String,
                            k1Value = (row["k1Value"] as? Number)?.toDouble() ?: 0.0,
                            k2Id = row["k2Id"] as? String,
                            k2Value = (row["k2Value"] as? Number)?.toDouble() ?: 0.0,
                            k3Id = row["k3Id"] as? String,
                            k3Value = (row["k3Value"] as? Number)?.toDouble() ?: 0.0,
                            k4Id = row["k4Id"] as? String,
                            k4Value = (row["k4Value"] as? Number)?.toDouble() ?: 0.0,
                            k5Id = row["k5Id"] as? String,
                            k5Value = (row["k5Value"] as? Number)?.toDouble() ?: 0.0,
                            nLittle = (row["nLittle"] as? Number)?.toInt() ?: 0,
                            pAdult = (row["pAdult"] as? Number)?.toDouble() ?: 0.0,
                            coefGes = (row["coefGes"] as? Number)?.toInt() ?: 0,
                            coefLact = (row["coefLact"] as? Number)?.toInt() ?: 0,
                            MCS = (row["MCS"] as? Number)?.toInt() ?: 0
                        )
                        consultationDao.insert(entity)
                        impConsults++
                    } catch (e: Exception) {
                        errors.add("Consultation ${row["UUID"]}: ${e.message}")
                    }
                }
                onLog("Consultations importées: $impConsults, ignorées: $skipConsults")
            }

            // 4. RATION
            if (conn.tableExists("RATION")) {
                val rows = conn.queryAll("SELECT * FROM RATION")
                rows.forEach { row ->
                    try {
                        val uuid = row["UUID"] as? String ?: return@forEach
                        val especeRaw = row["espece"]
                        val especeStr = when (especeRaw) {
                            is Number -> SPECIE_MAP[especeRaw.toInt()]
                            is String -> especeRaw
                            else -> null
                        }
                        val entity = RationEntity(
                            uuid = uuid,
                            idConsult = row["idConsult"] as? String ?: return@forEach,
                            name = row["name"] as? String,
                            coef = (row["coef"] as? Number)?.toDouble() ?: 0.0,
                            actual = (row["actual"] as? Number)?.toInt() == 1 || row["actual"] == true,
                            number = (row["number"] as? Number)?.toInt() ?: 0,
                            espece = especeStr,
                            description = row["description"] as? String
                        )
                        consultationDao.insertRation(entity)
                        impRations++

                        // Aliments de cette ration (dans Data-Anim.db FOOD table)
                        if (conn.tableExists("FOOD")) {
                            val rationFoods = conn.queryAll(
                                "SELECT * FROM FOOD WHERE RefRation = '${uuid.replace("'", "''")}'"
                            )
                            rationFoods.forEach { fRow ->
                                try {
                                    val fUuid = fRow["UUID"] as? String ?: return@forEach
                                    val refAlimUnif = fRow["RefAlimUnif"] as? String ?: fUuid
                                    val quantity = (fRow["quantite"] as? Number)?.toDouble() ?: 0.0
                                    val alimentEntity = AlimentRationEntity(
                                        uuid = fUuid,
                                        refAlimUnif = refAlimUnif,
                                        refRation = uuid,
                                        quantity = quantity,
                                        refTarget = 0
                                    )
                                    try {
                                        consultationDao.insertAlimentRation(alimentEntity)
                                    } catch (_: Exception) {}
                                } catch (e: Exception) {
                                    errors.add("AlimentRation ${fRow["UUID"]}: ${e.message}")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        errors.add("Ration ${row["UUID"]}: ${e.message}")
                    }
                }
                onLog("Rations importées: $impRations, ignorées: $skipRations")
            }
        }
    }

    // --- Aliments (base alimentaire) ---
    if (foodDb.exists()) {
        onLog("Lecture de Data-Food.db...")
        connectV2(foodDb).use { conn ->
            if (conn.tableExists("FOOD")) {
                val rows = conn.queryAll(
                    "SELECT * FROM FOOD WHERE (RefRation IS NULL OR RefRation = '')"
                )
                onLog("${rows.size} aliments base trouvés")

                // Pré-charger les IDs existants pour éviter N requêtes
                val existingIds = foodDao.getAllFoodIds().toHashSet()

                rows.forEach { row ->
                    try {
                        val uuid = row["UUID"] as? String ?: return@forEach
                        if (uuid in existingIds) {
                            skipFoods++; return@forEach
                        }

                        // Résoudre le nom : NAME table (lang=FR) ou nameDef
                        val nameFr = runCatching {
                            conn.createStatement().executeQuery(
                                "SELECT value FROM NAME WHERE reffood = '${uuid.replace("'", "''")}' AND lang = 'FR' LIMIT 1"
                            ).use { rs -> if (rs.next()) rs.getString(1) else null }
                        }.getOrNull()

                        val entity = FoodEntity(
                            uuid = uuid,
                            groupAlim = (row["groupAlim"] as? Number)?.toInt() ?: 0,
                            typeAlim = (row["typeAlim"] as? Number)?.toInt() ?: 0,
                            ingredients = row["ingredients"] as? String ?: "",
                            price = (row["price"] as? Number)?.toDouble() ?: 0.0,
                            categPrice = row["categPrice"] as? String ?: "",
                            brand = row["brand"] as? String ?: "",
                            gamme = row["gamme"] as? String ?: "",
                            cont = "",
                            unitPres = (row["unitPres"] as? Number)?.toInt() ?: 0,
                            quantityPres = (row["quantityPres"] as? Number)?.toDouble() ?: 0.0,
                            version = 1,
                            date = "2021-12-20",
                            nameDef = row["nameDef"] as? String ?: "",
                            consistent = (row["consistent"] as? Number)?.toInt() ?: 1,
                            deprecated = (row["deprecated"] as? Number)?.toInt() ?: 0,
                            DataB = row["DataB"] as? String ?: "",
                            name = nameFr ?: row["nameDef"] as? String
                        )
                        foodDao.insertFood(entity)
                        impFoods++

                        // Valeurs nutritionnelles
                        val nutrientValues = mutableListOf<NutrientValueEntity>()
                        NUTRIENT_TABLE_MAP.forEach { (table, labels) ->
                            if (conn.tableExists(table)) {
                                runCatching {
                                    conn.queryAll(
                                        "SELECT kind, value FROM $table WHERE reffood = '${uuid.replace("'", "''")}'"
                                    ).forEach { nRow ->
                                        val kind = (nRow["kind"] as? Number)?.toInt() ?: return@forEach
                                        val value = (nRow["value"] as? Number)?.toDouble() ?: return@forEach
                                        if (kind < labels.size && value != 0.0) {
                                            nutrientValues.add(
                                                NutrientValueEntity(
                                                    refAliment = uuid,
                                                    nutrientLabel = labels[kind],
                                                    value = value
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (nutrientValues.isNotEmpty()) {
                            nutrientValueDao.insertNutrientValues(nutrientValues)
                        }
                    } catch (e: Exception) {
                        errors.add("Aliment ${row["UUID"]}: ${e.message}")
                    }
                }
                onLog("Aliments importés: $impFoods, ignorés: $skipFoods")
            }
        }
    }

    if (errors.isNotEmpty()) onLog("${errors.size} erreur(s) rencontrée(s)")
    onLog("Migration terminée.")

    LegacyMigrationViewModel.MigrationResult(
        imported = LegacyMigrationViewModel.MigrationCounts(
            animals = impAnimals,
            consultations = impConsults,
            rations = impRations,
            weights = impWeights,
            foods = impFoods
        ),
        skipped = LegacyMigrationViewModel.MigrationCounts(
            animals = skipAnimals,
            consultations = skipConsults,
            rations = skipRations,
            weights = 0,
            foods = skipFoods
        ),
        errors = errors
    )
}
