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

// V2 stadePhysio int → VetNutriMP StadePhysio.label
private val STADE_MAP = mapOf(
    0 to "ADULTE", 1 to "CROISSANCE", 2 to "LACTATION", 3 to "GESTATION", 4 to "HOSPIT"
)

// V2 EquationKind int → VetNutriMP EquationKind.name
private val EQUATION_KIND_MAP = mapOf(
    0 to "ENERGYNEED", 1 to "ENERGYDENSITY", 2 to "MW",
    3 to "INDICATOR", 4 to "NEED", 5 to "COMPLEMENTARY_NUTRIENT", 6 to "ENERCOMP"
)

// V2 Reflevel int → VetNutriMP Reflevel.name
private val REFLEVEL_MAP = mapOf(0 to "MIN", 1 to "MAX", 2 to "OPTIMIN", 3 to "OPTIMAX")

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

private fun Connection.tableNames(): List<String> = runCatching {
    queryAll("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name")
        .mapNotNull { it["name"] as? String }
}.getOrDefault(emptyList())

private fun Connection.columnExists(table: String, column: String): Boolean = runCatching {
    createStatement().executeQuery("PRAGMA table_info($table)").use { rs ->
        while (rs.next()) {
            if (rs.getString("name").equals(column, ignoreCase = true)) return@runCatching true
        }
        false
    }
}.getOrDefault(false)

private fun Connection.tableColumns(table: String): List<String> = runCatching {
    val cols = mutableListOf<String>()
    createStatement().executeQuery("PRAGMA table_info($table)").use { rs ->
        while (rs.next()) cols.add(rs.getString("name"))
    }
    cols
}.getOrDefault(emptyList())

// Case-insensitive lookup: first matching key wins
private fun Map<String, Any?>.field(vararg keys: String): Any? {
    val lower = entries.associate { it.key.lowercase() to it.value }
    return keys.firstNotNullOfOrNull { lower[it.lowercase()] }
}
private fun Map<String, Any?>.str(vararg keys: String): String? = field(*keys) as? String
private fun Map<String, Any?>.num(vararg keys: String): Number? = field(*keys) as? Number

private fun Any?.asNonBlankString(): String? = when (this) {
    is String -> trim().takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
    is Number -> toString()
    else -> null
}

private fun Map<String, Any?>.stringValue(vararg keys: String): String? =
    field(*keys).asNonBlankString()

private fun Map<String, Any?>.stringList(vararg keys: String): List<String> {
    val raw = stringValue(*keys) ?: return emptyList()
    return raw
        .trim()
        .removePrefix("[")
        .removeSuffix("]")
        .split(',', ';')
        .map { it.trim().trim('"', '\'') }
        .filter { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
        .distinct()
}

private val CONSULTATION_GENERAL_REF_KEYS = arrayOf(
    "referenceGeneraleId",
    "referenceId",
    "referenceEvId",
    "referenceUuid",
    "referenceUUID",
    "uuidReference",
    "UUIDReference",
    "refId",
    "refUUID",
    "refUuid",
    "RefString",
    "refString",
    "ref",
    "Ref",
    "refRef",
    "RefRef",
    "dataRef",
    "DataRef",
    "refData",
    "dataRefUUID",
    "dataRefUuid"
)

private val CONSULTATION_DISEASE_REF_KEYS = arrayOf(
    "referencesMaladies",
    "referencesMaladiesJson",
    "diseaseReferences",
    "diseaseReferenceIds",
    "diseaseRef",
    "DiseaseRef",
    "refMaladie",
    "refMaladies",
    "refDisease",
    "refDiseases"
)

private data class LegacyReferenceInfo(
    val uuid: String,
    val name: String,
    val disease: Boolean
)

private data class LegacyReferenceTranslation(
    val mpUuid: String,
    val label: String
)

private val LEGACY_REFERENCE_TRANSLATIONS = mapOf(
    "a1e31f4d-ea50-4865-b371-c6c06ebcc02b" to LegacyReferenceTranslation("a4c16357-5ca0-40e0-b6a7-10fb2891c77a", "Adulte"),
    "a4c16357-5ca0-40e0-b6a7-10fb2891c77a" to LegacyReferenceTranslation("a4c16357-5ca0-40e0-b6a7-10fb2891c77a", "Adulte"),
    "a96c263d-9571-4748-b7b7-a9d3b85620fa" to LegacyReferenceTranslation("54ffe7ad-ca54-4af9-ab9a-c159f018d2ec", "Adulte 9-25kg"),
    "1f7cdc6e-d4e3-4169-a3a8-d0a4bedc9908" to LegacyReferenceTranslation("28d0462e-1662-4374-bcbe-d1e31be1ba6c", "Adulte <9kg"),
    "105a39b1-c797-4b72-a4db-79035df1c19d" to LegacyReferenceTranslation("105a39b1-c797-4b72-a4db-79035df1c19d", "Adulte >25kg"),
    "936a07a3-239a-4414-967e-eeb8906984bf" to LegacyReferenceTranslation("936a07a3-239a-4414-967e-eeb8906984bf", "Arthrosis"),
    "202af15d-c0fa-4ed9-81d5-1ca5e9e94a8a" to LegacyReferenceTranslation("202af15d-c0fa-4ed9-81d5-1ca5e9e94a8a", "Avant sevrage"),
    "84f4633d-cfeb-4c03-96a6-1d6d7c0eccd2" to LegacyReferenceTranslation("84f4633d-cfeb-4c03-96a6-1d6d7c0eccd2", "CKD"),
    "e494287e-5532-4f89-85ad-aaa8248053b4" to LegacyReferenceTranslation("e494287e-5532-4f89-85ad-aaa8248053b4", "CKD"),
    "03f44571-9b09-4406-bdd4-21cb3ba235ea" to LegacyReferenceTranslation("03f44571-9b09-4406-bdd4-21cb3ba235ea", "Cardio (>C1)"),
    "77e8d58b-e78a-4f1e-902f-1b60e96f4141" to LegacyReferenceTranslation("77e8d58b-e78a-4f1e-902f-1b60e96f4141", "Cardio (>C1)"),
    "5ac72656-f825-47b9-a719-a845bd88ad19" to LegacyReferenceTranslation("5ac72656-f825-47b9-a719-a845bd88ad19", "Croissance"),
    "b2f03b6e-014f-49f2-9e08-a644cb1bec17" to LegacyReferenceTranslation("b2f03b6e-014f-49f2-9e08-a644cb1bec17", "Croissance"),
    "16fefe59-a87e-4bf8-9c12-1535c1532bad" to LegacyReferenceTranslation("16fefe59-a87e-4bf8-9c12-1535c1532bad", "Croissance grandes races <7 mois"),
    "9560d6ce-89de-471a-91bf-93932a594b2f" to LegacyReferenceTranslation("9560d6ce-89de-471a-91bf-93932a594b2f", "DCM"),
    "c252af95-fb7f-4cd7-b5af-a6b098f118c4" to LegacyReferenceTranslation("c252af95-fb7f-4cd7-b5af-a6b098f118c4", "DCM"),
    "1d17d4fc-16ba-4f61-9e9c-13004b93e57e" to LegacyReferenceTranslation("1d17d4fc-16ba-4f61-9e9c-13004b93e57e", "Gestation"),
    "8cac807d-d110-404e-91b3-150d68826180" to LegacyReferenceTranslation("8cac807d-d110-404e-91b3-150d68826180", "Gestation"),
    "d05ad591-a2d7-4253-8f42-cd03e3f18334" to LegacyReferenceTranslation("d05ad591-a2d7-4253-8f42-cd03e3f18334", "Hospitalisation"),
    "e9ce41d1-f3d9-4981-9934-3abdc7109825" to LegacyReferenceTranslation("e9ce41d1-f3d9-4981-9934-3abdc7109825", "Hospitalisation"),
    "839e3bb9-2b4f-445c-aed7-4879e4a9c8a5" to LegacyReferenceTranslation("839e3bb9-2b4f-445c-aed7-4879e4a9c8a5", "Hypercalcemia"),
    "da67dbba-2583-4c34-b6d2-a92755af37f4" to LegacyReferenceTranslation("da67dbba-2583-4c34-b6d2-a92755af37f4", "Lactation"),
    "f9d12195-41f7-4170-864b-92e32569496e" to LegacyReferenceTranslation("f9d12195-41f7-4170-864b-92e32569496e", "Lactation"),
    "2744b605-11ab-41aa-a488-913fe819008a" to LegacyReferenceTranslation("2744b605-11ab-41aa-a488-913fe819008a", "Pancreatitis"),
    "9edcb377-3648-41e3-befc-1188fabe2994" to LegacyReferenceTranslation("9edcb377-3648-41e3-befc-1188fabe2994", "Primate")
)

private fun String.normalizedUuidCandidate(): String =
    trim()
        .trim('{', '}')
        .lowercase()

private fun String.translateLegacyReferenceUuid(): String =
    LEGACY_REFERENCE_TRANSLATIONS[normalizedUuidCandidate()]?.mpUuid ?: this

private fun Map<String, Any?>.debugValuesForKeys(keys: Array<String>): String =
    keys.mapNotNull { key ->
        field(key).asNonBlankString()?.let { value -> "$key=$value" }
    }.joinToString(", ")

private fun Map<String, Any?>.knownReferenceUuidMatches(
    knownReferences: Map<String, LegacyReferenceInfo>
): List<Pair<String, LegacyReferenceInfo>> {
    if (knownReferences.isEmpty()) return emptyList()
    val normalizedReferenceIds = knownReferences.values.associateBy { it.uuid.normalizedUuidCandidate() }
    return entries.mapNotNull { (key, value) ->
        if (key.equals("UUID", ignoreCase = true)) {
            null
        } else {
            val normalizedValue = value.asNonBlankString()?.normalizedUuidCandidate()
            val reference = normalizedValue?.let { normalizedReferenceIds[it] }
            if (reference != null) key to reference else null
        }
    }.distinctBy { it.first to it.second.uuid }
}

private fun Map<String, Any?>.resolveKnownReferenceUuid(
    knownReferences: Map<String, LegacyReferenceInfo>,
    preferNonDisease: Boolean
): String? {
    val exactCandidates = knownReferenceUuidMatches(knownReferences)
        .map { it.second }
        .distinctBy { it.uuid }
    if (exactCandidates.isEmpty()) return null
    return if (preferNonDisease) {
        (exactCandidates.firstOrNull { !it.disease } ?: exactCandidates.first()).uuid
    } else {
        (exactCandidates.firstOrNull { it.disease } ?: exactCandidates.first()).uuid
    }
}

private fun loadLegacyReferenceInfo(refDbFile: File?): Map<String, LegacyReferenceInfo> {
    if (refDbFile == null || !refDbFile.exists()) return emptyMap()
    return runCatching {
        connectV2(refDbFile).use { conn ->
            val refEvTable = conn.findTable(*REF_TABLE_NAMES.toTypedArray()) ?: return@use emptyMap()
            conn.queryAll("SELECT * FROM $refEvTable").mapNotNull { row ->
                val uuid = row.str("UUID", "uuid") ?: return@mapNotNull null
                val disease = row.num("disease", "maladie")?.toInt() == 1
                uuid to LegacyReferenceInfo(
                    uuid = uuid,
                    name = row.str("nom", "name") ?: "",
                    disease = disease
                )
            }.toMap()
        }
    }.getOrDefault(emptyMap())
}

// ---- Ref-DB discovery -------------------------------------------------------

// Noms possibles du fichier de références (V2 selon version/OS)
private val REF_DB_NAMES = listOf(
    "ref.db", "Ref.db", "REF.db",
    "Data-Ref.db", "data-ref.db",
    "references.db", "refs.db"
)

// Noms possibles selon la version de VetNutri 2
private val BIBLIO_TABLE_NAMES  = listOf("BIBLIO_REFS", "BIBLIO_REF", "Biblio", "biblio")
private val EQUATION_TABLE_NAMES = listOf("EQUATIONS", "equation", "Equation", "EQUATION")
private val REF_TABLE_NAMES     = listOf("REFERENCE_EV", "dataRef", "DataRef", "DATAREF")
private val COEF_TABLE_NAMES    = listOf("REFERENCE_EV_COEFFICIENTS", "coef", "Coef", "COEF")

private fun Connection.findTable(vararg candidates: String): String? {
    val actualTables = tableNames()
    return candidates.firstNotNullOfOrNull { candidate ->
        actualTables.firstOrNull { it.equals(candidate, ignoreCase = true) }
    }
}

/** Trouve la base contenant les tables de référence.
 *  Cherche d'abord par nom connu, puis dans tous les .db du dossier. */
private fun findRefDb(folder: File): File? {
    fun hasRefTables(f: File) = runCatching {
        connectV2(f).use { c ->
            c.findTable(*BIBLIO_TABLE_NAMES.toTypedArray()) != null ||
            c.findTable(*EQUATION_TABLE_NAMES.toTypedArray()) != null ||
            c.findTable(*REF_TABLE_NAMES.toTypedArray()) != null
        }
    }.getOrDefault(false)

    // 1. Noms candidats classiques
    REF_DB_NAMES.mapNotNull { name -> File(folder, name).takeIf { it.exists() } }
        .firstOrNull { hasRefTables(it) }?.let { return it }

    // 2. Scan exhaustif de tous les .db présents. Certaines versions stockent dataRef dans Data-Anim.db.
    return folder.listFiles { f -> f.isFile && f.name.endsWith(".db", ignoreCase = true) }
        ?.firstOrNull { f -> hasRefTables(f) }
}

/** Compte les enregistrements dans les tables de référence d'un fichier DB donné. */
private fun countRefTables(f: File): Triple<Int, Int, Int> {
    var biblioRefs = 0; var equations = 0; var references = 0
    runCatching {
        connectV2(f).use { conn ->
            conn.findTable(*BIBLIO_TABLE_NAMES.toTypedArray())?.let  { biblioRefs  = conn.count(it) }
            conn.findTable(*EQUATION_TABLE_NAMES.toTypedArray())?.let { equations   = conn.count(it) }
            conn.findTable(*REF_TABLE_NAMES.toTypedArray())?.let     { references  = conn.count(it) }
        }
    }
    return Triple(biblioRefs, equations, references)
}

// ---- Preview ----------------------------------------------------------------

suspend fun previewV2Migration(dbFolderPath: String): LegacyMigrationViewModel.MigrationCounts =
    withContext(AppDispatchers.IO) {
        val folder = File(dbFolderPath)
        val animDb = File(folder, "Data-Anim.db")
        val foodDb = File(folder, "Data-Food.db")

        println("[VetNutriMigration] Dossier: $dbFolderPath")
        val allFiles = folder.listFiles { f -> f.isFile && f.name.endsWith(".db", ignoreCase = true) }
            ?.map { it.name } ?: emptyList()
        println("[VetNutriMigration] Fichiers .db trouvés: ${allFiles.joinToString()}")

        var animals = 0; var consultations = 0; var rations = 0; var weights = 0
        var foods = 0; var biblioRefs = 0; var equations = 0; var references = 0

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
                if (conn.tableExists("FOOD")) {
                    val sql = if (conn.columnExists("FOOD", "RefRation"))
                        "SELECT COUNT(*) FROM FOOD WHERE (RefRation IS NULL OR RefRation = '')"
                    else
                        "SELECT COUNT(*) FROM FOOD"
                    foods = conn.createStatement().executeQuery(sql)
                        .use { rs -> if (rs.next()) rs.getInt(1) else 0 }
                }
            }
        }

        val refDbFile = findRefDb(folder)
        if (refDbFile != null) {
            println("[VetNutriMigration] Base de références trouvée: ${refDbFile.name}")
            val (b, e, r) = countRefTables(refDbFile)
            biblioRefs = b; equations = e; references = r
        } else {
            println("[VetNutriMigration] Aucune base de références trouvée dans $dbFolderPath")
        }

        LegacyMigrationViewModel.MigrationCounts(
            animals, consultations, rations, weights, foods, biblioRefs, equations, references
        )
    }

// ---- Migration ---------------------------------------------------------------

suspend fun runV2Migration(
    dbFolderPath: String,
    appDatabase: AppDatabase,
    onLog: suspend (String) -> Unit
): LegacyMigrationViewModel.MigrationResult = withContext(AppDispatchers.IO) {
    suspend fun log(msg: String) {
        println("[VetNutriMigration] $msg")
        onLog(msg)
    }
    fun logError(msg: String, e: Exception) {
        System.err.println("[VetNutriMigration][ERROR] $msg")
        e.printStackTrace(System.err)
    }

    val folder = File(dbFolderPath)
    val animDb = File(folder, "Data-Anim.db")
    val foodDb = File(folder, "Data-Food.db")
    val refDbFile = findRefDb(folder)
    val legacyReferencesByUuid = loadLegacyReferenceInfo(refDbFile)

    var impAnimals = 0; var skipAnimals = 0
    var impConsults = 0; var skipConsults = 0
    var impRations = 0; var skipRations = 0
    var impWeights = 0; var skipWeights = 0
    var impFoods = 0; var skipFoods = 0
    var impBiblioRefs = 0; var skipBiblioRefs = 0
    var impEquations = 0; var skipEquations = 0
    var impReferences = 0; var skipReferences = 0
    val errors = mutableListOf<String>()

    val animalDao = appDatabase.animalDao()
    val consultationDao = appDatabase.consultationDao()
    val foodDao = appDatabase.foodDao()
    val nutrientValueDao = appDatabase.nutrientValueDao()
    val biblioRefDao = appDatabase.biblioRefDao()
    val equationDao = appDatabase.equationDao()
    val referenceEvDao = appDatabase.referenceEvDao()

    if (refDbFile != null) {
        log("Base de références utilisée: ${refDbFile.name} (${legacyReferencesByUuid.size} UUID de références préchargés)")
        if (legacyReferencesByUuid.isNotEmpty()) {
            log(
                "Exemples UUID références V2: " +
                    legacyReferencesByUuid.values.take(8).joinToString { ref ->
                        "${ref.uuid}${if (ref.name.isNotBlank()) " (${ref.name})" else ""}${if (ref.disease) " [maladie]" else ""}"
                    }
            )
        }
    } else {
        log("Aucune base de références trouvée avant import des consultations")
    }
    log("Traducteur UUID références VetNutri 2 -> VetNutri MP actif: ${LEGACY_REFERENCE_TRANSLATIONS.size} correspondances")

    suspend fun validateAndRepairConsultationReferences() {
        val importedReferences = referenceEvDao.getAllReferenceEv()
        val referencesByUuid = importedReferences.associateBy { it.uuid }
        val referencesByNormalizedUuid = importedReferences.associateBy { it.uuid.normalizedUuidCandidate() }
        log("Validation post-import: ${importedReferences.size} références MP disponibles")
        if (importedReferences.isNotEmpty()) {
            log(
                "Exemples UUID références MP: " +
                    importedReferences.take(8).joinToString { ref ->
                        "${ref.uuid}${if (ref.nom.isNotBlank()) " (${ref.nom})" else ""}${if (ref.maladie) " [maladie]" else ""}"
                    }
            )
        }

        val consultations = animalDao.getAllAnimals().flatMap { animal ->
            consultationDao.getConsultationsForAnimal(animal.uuid)
        }
        var withGeneralRef = 0
        var repairedGeneralRef = 0
        var translatedGeneralRef = 0
        var missingGeneralRef = 0
        val missingSamples = mutableListOf<String>()

        consultations.forEach { consultation ->
            val refId = consultation.referenceGeneraleId?.takeIf { it.isNotBlank() }
            if (refId == null) return@forEach
            withGeneralRef++
            val translatedRefId = refId.translateLegacyReferenceUuid()
            if (translatedRefId != refId) {
                consultationDao.update(consultation.copy(referenceGeneraleId = translatedRefId))
                translatedGeneralRef++
            }

            val effectiveRefId = translatedRefId
            val exactMatch = referencesByUuid[effectiveRefId]
            if (exactMatch != null) return@forEach

            val normalizedMatch = referencesByNormalizedUuid[effectiveRefId.normalizedUuidCandidate()]
            if (normalizedMatch != null) {
                consultationDao.update(consultation.copy(referenceGeneraleId = normalizedMatch.uuid))
                repairedGeneralRef++
                return@forEach
            }

            missingGeneralRef++
            if (missingSamples.size < 12) {
                missingSamples.add("${consultation.uuid} -> $effectiveRefId")
            }
        }

        log(
            "Validation post-import consultations: $withGeneralRef avec référence générale, " +
                "$translatedGeneralRef UUID legacy traduits, " +
                "$repairedGeneralRef UUID réparés, $missingGeneralRef références introuvables"
        )
        if (missingSamples.isNotEmpty()) {
            log("Références générales introuvables exemples: ${missingSamples.joinToString()}")
        }
    }

    // --- Animaux + consultations + rations + poids ---
    if (animDb.exists()) {
        log("Lecture de Data-Anim.db...")
        connectV2(animDb).use { conn ->
            val animTables = conn.queryAll(
                "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name"
            ).mapNotNull { it["name"] as? String }
            log("Tables Data-Anim.db: ${animTables.joinToString()}")

            // 1. ANIMALS
            if (conn.tableExists("ANIMALS")) {
                log("Colonnes ANIMALS: ${conn.tableColumns("ANIMALS").joinToString()}")
                val rows = conn.queryAll("SELECT * FROM ANIMALS")
                log("${rows.size} animaux trouvés")
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
                        val msg = "Animal ${row["UUID"]}: ${e.message}"
                        errors.add(msg); logError(msg, e)
                    }
                }
                log("Animaux importés: $impAnimals, ignorés: $skipAnimals")
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
                        val msg = "Poids ${row["UUID"]}: ${e.message}"
                        errors.add(msg); logError(msg, e)
                    }
                }
                log("Poids importés: $impWeights")
            }

            // 3. CONSULTATIONS
            if (conn.tableExists("CONSULTATIONS")) {
                val consultationColumns = conn.tableColumns("CONSULTATIONS")
                log("Colonnes CONSULTATIONS: ${consultationColumns.joinToString()}")
                log(
                    "Colonnes CONSULTATIONS contenant 'ref' ou 'uuid': " +
                        consultationColumns.filter {
                            it.contains("ref", ignoreCase = true) ||
                                it.contains("uuid", ignoreCase = true)
                        }
                            .joinToString()
                            .ifBlank { "aucune" }
                )
                val referenceLinksByConsult = if (conn.tableExists("ReferenceDisease")) {
                    log("Colonnes ReferenceDisease: ${conn.tableColumns("ReferenceDisease").joinToString()}")
                    conn.queryAll("SELECT * FROM ReferenceDisease")
                        .mapNotNull { linkRow ->
                            val consultId = linkRow.stringValue("idCons", "idConsult", "consultationId", "refConsult")
                                ?: return@mapNotNull null
                            val refId = linkRow.stringValue("refRef", "referenceId", "referenceEvId", "refId")
                                ?: return@mapNotNull null
                            consultId to refId
                        }
                        .groupBy({ it.first }, { it.second })
                } else {
                    emptyMap()
                }
                if (referenceLinksByConsult.isNotEmpty()) {
                    log("Liens ReferenceDisease détectés: ${referenceLinksByConsult.values.sumOf { it.size }}")
                }
                val rows = conn.queryAll("SELECT * FROM CONSULTATIONS")
                log("${rows.size} consultations trouvées")
                var consultsWithGeneralRef = 0
                var consultsWithoutGeneralRef = 0
                var skippedWithGeneralRef = 0
                var skippedWithoutGeneralRef = 0
                var updatedExistingConsultRefs = 0
                var debugConsultationRows = 0
                rows.forEach { row ->
                    try {
                        val uuid = row["UUID"] as? String ?: return@forEach
                        val linkedRefs = referenceLinksByConsult[uuid]
                            .orEmpty()
                            .map { it.translateLegacyReferenceUuid() }
                            .distinct()
                        val linkedGeneralRef = linkedRefs.firstOrNull {
                            legacyReferencesByUuid[it]?.disease == false
                        }
                        val linkedDiseaseRefs = linkedRefs.filter {
                            legacyReferencesByUuid[it]?.disease != false
                        }
                        val referenceGeneraleId =
                            row.stringValue(*CONSULTATION_GENERAL_REF_KEYS)
                                ?.translateLegacyReferenceUuid()
                                ?: linkedGeneralRef
                                ?: row.resolveKnownReferenceUuid(
                                    legacyReferencesByUuid,
                                    preferNonDisease = true
                                )?.translateLegacyReferenceUuid()
                        val referencesMaladiesJson =
                            (row.stringList(*CONSULTATION_DISEASE_REF_KEYS)
                                .map { it.translateLegacyReferenceUuid() } + linkedDiseaseRefs)
                                .distinct()
                                .joinToString(",")
                                .takeIf { it.isNotBlank() }
                        if (referenceGeneraleId != null) {
                            consultsWithGeneralRef++
                        } else {
                            consultsWithoutGeneralRef++
                        }
                        if (debugConsultationRows < 10) {
                            val generalDebug = row.debugValuesForKeys(CONSULTATION_GENERAL_REF_KEYS)
                                .ifBlank { "aucune valeur detectee" }
                            val diseaseDebug = row.debugValuesForKeys(CONSULTATION_DISEASE_REF_KEYS)
                                .ifBlank { "aucune valeur detectee" }
                            val uuidMatches = row.knownReferenceUuidMatches(legacyReferencesByUuid)
                                .joinToString { (column, ref) ->
                                    "$column=${ref.uuid}${if (ref.name.isNotBlank()) " (${ref.name})" else ""}"
                                }
                                .ifBlank { "aucun" }
                            log(
                                "Consultation $uuid refs candidates: " +
                                    "generale=[$generalDebug], maladies=[$diseaseDebug], " +
                                    "uuidRefsConnus=[$uuidMatches], " +
                                    "liensReferenceDisease=${linkedRefs.joinToString().ifBlank { "aucun" }}, " +
                                    "retenueGenerale=${referenceGeneraleId ?: "null"}, " +
                                    "retenuesMaladies=${referencesMaladiesJson ?: "null"}"
                            )
                            debugConsultationRows++
                        }
                        val existingConsultation = consultationDao.getConsultationById(uuid)
                        if (existingConsultation != null) {
                            val shouldUpdateExisting =
                                (existingConsultation.referenceGeneraleId.isNullOrBlank() && referenceGeneraleId != null) ||
                                    (existingConsultation.referencesMaladiesJson.isNullOrBlank() && referencesMaladiesJson != null)
                            if (shouldUpdateExisting) {
                                consultationDao.update(
                                    existingConsultation.copy(
                                        referenceGeneraleId =
                                            existingConsultation.referenceGeneraleId
                                                ?.takeIf { it.isNotBlank() }
                                                ?: referenceGeneraleId,
                                        referencesMaladiesJson =
                                            existingConsultation.referencesMaladiesJson
                                                ?.takeIf { it.isNotBlank() }
                                                ?: referencesMaladiesJson
                                    )
                                )
                                updatedExistingConsultRefs++
                            }
                            if (referenceGeneraleId != null) {
                                skippedWithGeneralRef++
                            } else {
                                skippedWithoutGeneralRef++
                            }
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
                            MCS = (row["MCS"] as? Number)?.toInt() ?: 0,
                            referenceGeneraleId = referenceGeneraleId,
                            referencesMaladiesJson = referencesMaladiesJson,
                            coefficientAjustement = row.num(
                                "coefficientAjustement",
                                "coefAjustement",
                                "ky"
                            )?.toDouble() ?: 1.0
                        )
                        consultationDao.insert(entity)
                        impConsults++
                    } catch (e: Exception) {
                        val msg = "Consultation ${row["UUID"]}: ${e.message}"
                        errors.add(msg); logError(msg, e)
                    }
                }
                log(
                    "Références générales détectées dans CONSULTATIONS: " +
                        "$consultsWithGeneralRef oui, $consultsWithoutGeneralRef non"
                )
                log(
                    "Consultations ignorées déjà présentes: $skipConsults " +
                        "(avec ref detectee=$skippedWithGeneralRef, sans ref detectee=$skippedWithoutGeneralRef)"
                )
                if (updatedExistingConsultRefs > 0) {
                    log("Consultations déjà présentes complétées avec références: $updatedExistingConsultRefs")
                }
                log("Consultations importées: $impConsults, ignorées: $skipConsults")
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
                        if (conn.tableExists("FOOD") && conn.columnExists("FOOD", "RefRation")) {
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
                                    val msg = "AlimentRation ${fRow["UUID"]}: ${e.message}"
                                    errors.add(msg); logError(msg, e)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // FK constraint = ration orpheline (consultation absente) → skip silencieux
                        if (e.message?.contains("FOREIGN KEY", ignoreCase = true) == true ||
                            e.message?.contains("constraint", ignoreCase = true) == true) {
                            skipRations++
                        } else {
                            val msg = "Ration ${row["UUID"]}: ${e.message}"
                            errors.add(msg); logError(msg, e)
                        }
                    }
                }
                log("Rations importées: $impRations, ignorées (dont orphelines): $skipRations")
            }
        }
    }

    // --- Aliments (base alimentaire) ---
    if (foodDb.exists()) {
        log("Lecture de Data-Food.db...")
        connectV2(foodDb).use { conn ->
            val tables = conn.queryAll(
                "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name"
            ).mapNotNull { it["name"] as? String }
            log("Tables Data-Food.db: ${tables.joinToString()}")

            if (conn.tableExists("FOOD")) {
                val foodCols = conn.tableColumns("FOOD")
                log("Colonnes FOOD: ${foodCols.joinToString()}")

                val hasRefRation = conn.columnExists("FOOD", "RefRation")
                log("Colonne RefRation présente: $hasRefRation")

                val sql = if (hasRefRation)
                    "SELECT * FROM FOOD WHERE (RefRation IS NULL OR RefRation = '')"
                else
                    "SELECT * FROM FOOD"
                val rows = conn.queryAll(sql)
                log("${rows.size} aliments base trouvés")

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
                        val msg = "Aliment ${row["UUID"]}: ${e.message}"
                        errors.add(msg); logError(msg, e)
                    }
                }
                log("Aliments importés: $impFoods, ignorés: $skipFoods")
            }
        }
    }

    // --- Références (ref.db / Data-Ref.db / autre base détectée) ---
    if (refDbFile != null && refDbFile.exists()) {
        log("Lecture de ${refDbFile.name}...")
        connectV2(refDbFile).use { conn ->
            val refTables = conn.queryAll(
                "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name"
            ).mapNotNull { it["name"] as? String }
            log("Tables ${refDbFile.name}: ${refTables.joinToString()}")

            // 6. BIBLIO_REFS / Biblio
            val biblioTable = conn.findTable(*BIBLIO_TABLE_NAMES.toTypedArray())
            if (biblioTable != null) {
                log("Colonnes $biblioTable: ${conn.tableColumns(biblioTable).joinToString()}")
                val rows = conn.queryAll("SELECT * FROM $biblioTable")
                log("${rows.size} références bibliographiques trouvées")
                rows.forEach { row ->
                    try {
                        val uuid = row.str("UUID", "uuid") ?: return@forEach
                        if (biblioRefDao.getBiblioRefById(uuid) != null) {
                            skipBiblioRefs++; return@forEach
                        }
                        val entity = fr.vetbrain.vetnutri_mp.DataBase.BiblioRefEntity(
                            uuid = uuid,
                            firstAuthor = row.str("fAuthor", "firstAuthor", "first_author", "author") ?: "",
                            year = row.num("year")?.toInt() ?: 0,
                            completeRef = row.str("fullRef", "completeRef", "complete_ref", "reference") ?: "",
                            comments = row.str("comments", "comment") ?: "",
                            bibtex = "",
                            consistent = row.num("consistent")?.toInt() ?: 1
                        )
                        biblioRefDao.insertBiblioRef(entity)
                        impBiblioRefs++
                    } catch (e: Exception) {
                        val msg = "BiblioRef ${row.str("UUID", "uuid")}: ${e.message}"
                        errors.add(msg); logError(msg, e)
                    }
                }
                log("BiblioRefs importées: $impBiblioRefs, ignorées: $skipBiblioRefs")
            } else {
                log("Aucune table bibliographique reconnue parmi: ${BIBLIO_TABLE_NAMES.joinToString()}")
            }

            // 7. EQUATIONS / equation
            val equationTable = conn.findTable(*EQUATION_TABLE_NAMES.toTypedArray())
            if (equationTable != null) {
                log("Colonnes $equationTable: ${conn.tableColumns(equationTable).joinToString()}")
                val rows = conn.queryAll("SELECT * FROM $equationTable")
                log("${rows.size} équations trouvées")
                rows.forEach { row ->
                    try {
                        val uuid = row.str("UUID", "uuid") ?: return@forEach
                        if (equationDao.getEquationById(uuid) != null) {
                            skipEquations++; return@forEach
                        }
                        val kindInt = row.num("kind")?.toInt() ?: 0
                        val kindName = EQUATION_KIND_MAP[kindInt] ?: "ENERGYNEED"
                        // speciesRef est une String : "ALL", "0", "1", "4", "6"…
                        val specieStr = row.str("speciesRef", "Specie", "specie", "species")?.let { raw ->
                            when {
                                raw.uppercase() == "ALL" -> "ALL"
                                raw.toIntOrNull() != null -> SPECIE_MAP[raw.toInt()]
                                raw.isNotBlank() -> raw
                                else -> null
                            }
                        }
                        // refBiblio peut être un UUID ou un entier "1","2" → on ne garde que les UUID
                        val bibRefUuid = row.str("refBiblio", "bibRef", "bib_ref", "biblio")
                            ?.takeIf { it.length >= 32 && it.contains("-") }
                        val nutrientInt = row.num("nutrient")?.toInt() ?: 0
                        val entity = fr.vetbrain.vetnutri_mp.DataBase.EquationEntity(
                            uuid = uuid,
                            name = row.str("name") ?: "Équation importée",
                            description = row.str("description") ?: "",
                            equationScript = row.str("script", "equationScript") ?: "0",
                            specie = specieStr,
                            kind = kindName,
                            consistent = row.num("consistent")?.toInt() != 0,
                            bibRef = bibRefUuid,
                            variables = "[]",
                            nutrient = if (nutrientInt == 0) null else nutrientInt.toString(),
                            ratio = false
                        )
                        equationDao.insertEquation(entity)
                        impEquations++
                    } catch (e: Exception) {
                        val msg = "Equation ${row.str("UUID", "uuid")}: ${e.message}"
                        errors.add(msg); logError(msg, e)
                    }
                }
                log("Équations importées: $impEquations, ignorées: $skipEquations")
            } else {
                log("Aucune table d'équations reconnue parmi: ${EQUATION_TABLE_NAMES.joinToString()}")
            }

            // 8. REFERENCE_EV / dataRef
            val refEvTable = conn.findTable(*REF_TABLE_NAMES.toTypedArray())
            if (refEvTable != null) {
                log("Colonnes $refEvTable: ${conn.tableColumns(refEvTable).joinToString()}")
                val rows = conn.queryAll("SELECT * FROM $refEvTable")
                log("${rows.size} références nutritionnelles trouvées")
                rows.forEach { row ->
                    try {
                        val uuid = row.str("UUID", "uuid") ?: return@forEach
                        if (referenceEvDao.getReferenceEvById(uuid) != null) {
                            skipReferences++; return@forEach
                        }
                        val especeRaw = row.str("specie", "species", "espece")
                        val especeStr = when {
                            especeRaw == null || especeRaw.isBlank() -> "ALL"
                            especeRaw.toIntOrNull() != null -> SPECIE_MAP[especeRaw.toInt()] ?: "ALL"
                            else -> especeRaw
                        }
                        val entity = fr.vetbrain.vetnutri_mp.DataBase.ReferenceEvEntity(
                            uuid = uuid,
                            nom = row.str("name", "nom") ?: "",
                            description = row.str("description") ?: "",
                            maladie = row.num("disease", "maladie")?.toInt() == 1,
                            nomMaladie = "",
                            nomEnergie = row.str("SERname", "nomEnergie", "nameEnergy") ?: "",
                            consistent = row.num("consistent")?.toInt() ?: 1,
                            espece = especeStr,
                            stadePhysio = "ADULTE",
                            nomk1 = row.str("k1Name", "nomk1") ?: "",
                            nomk2 = row.str("k2Name", "nomk2") ?: "",
                            nomk3 = row.str("k3Name", "nomk3") ?: "",
                            nomk4 = row.str("k4Name", "nomk4") ?: "",
                            nomk5 = row.str("k5Name", "nomk5") ?: ""
                        )
                        referenceEvDao.insertReferenceEv(entity)
                        impReferences++

                        // Relations référence ↔ équation depuis BWeqRef, SERRef, DEcomRef, DErawRef
                        listOf(
                            "BW"    to row.str("BWeqRef"),
                            "BEE"   to row.str("SERRef"),
                            "DEcom" to row.str("DEcomRef"),
                            "DEraw" to row.str("DErawRef")
                        ).forEach { (eqType, eqId) ->
                            if (!eqId.isNullOrBlank()) runCatching {
                                referenceEvDao.insertEquationRelation(
                                    fr.vetbrain.vetnutri_mp.DataBase.ReferenceEvEquationEntity(
                                        referenceEvId = uuid, equationId = eqId, equationType = eqType
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        val msg = "ReferenceEv ${row.str("UUID", "uuid")}: ${e.message}"
                        errors.add(msg); logError(msg, e)
                    }
                }
                log("Références nutritionnelles importées: $impReferences, ignorées: $skipReferences")
            } else {
                log("Aucune table de références nutritionnelles reconnue parmi: ${REF_TABLE_NAMES.joinToString()}")
            }

            // 9. speReqEq (liens référence ↔ équation supplémentaires)
            if (conn.tableExists("speReqEq")) {
                val speRows = conn.queryAll("SELECT * FROM speReqEq")
                var impSpeRel = 0
                speRows.forEach { row ->
                    val refId = row.str("refRef") ?: return@forEach
                    val eqId  = row.str("refEq")  ?: return@forEach
                    runCatching {
                        referenceEvDao.insertEquationRelation(
                            fr.vetbrain.vetnutri_mp.DataBase.ReferenceEvEquationEntity(
                                referenceEvId = refId, equationId = eqId, equationType = "NUT"
                            )
                        )
                        impSpeRel++
                    }
                }
                if (impSpeRel > 0) log("Liens speReqEq importés: $impSpeRel")
            }

            // 10. REFERENCE_EV_COEFFICIENTS / coef
            val coefTable = conn.findTable(*COEF_TABLE_NAMES.toTypedArray())
            if (coefTable != null) {
                log("Colonnes $coefTable: ${conn.tableColumns(coefTable).joinToString()}")
                val coefRows = conn.queryAll("SELECT * FROM $coefTable")
                var impCoef = 0
                coefRows.forEach { row ->
                    try {
                        val refId = row.str("refRef", "referenceEvId") ?: return@forEach
                        val groupUUID = row.num("groupUUID", "group_uuid")?.toInt() ?: 0
                        val coefName = row.str("coefName", "description") ?: "Normal"
                        val coefValue = row.num("value", "coef")?.toDouble() ?: 1.0
                        val groupType = "k${groupUUID + 1}"
                        // UUID V2 si disponible, sinon UUID déterministe
                        val uuid = row.str("UUID", "uuid")?.takeIf { it.isNotBlank() }
                            ?: "${refId}_${groupUUID}_${coefName.take(16).replace(" ", "_")}"
                        referenceEvDao.insertCoefficient(
                            fr.vetbrain.vetnutri_mp.DataBase.ReferenceEvCoefficientEntity(
                                uuid = uuid,
                                referenceEvId = refId,
                                groupType = groupType,
                                description = coefName,
                                coef = coefValue,
                                groupUUID = groupUUID
                            )
                        )
                        impCoef++
                    } catch (_: Exception) {}
                }
                if (impCoef > 0) log("Coefficients importés: $impCoef")
            }

            // 11. VALUE tables → besoins nutritionnels par référence (VALUEBASE, VALUEAA, etc.)
            var impNut = 0
            NUTRIENT_TABLE_MAP.forEach { (table, labels) ->
                if (conn.tableExists(table)) runCatching {
                    conn.queryAll("SELECT * FROM $table").forEach { row ->
                        try {
                            val refId   = row.str("refRef") ?: return@forEach
                            val kindIdx = row.num("kind")?.toInt() ?: return@forEach
                            if (kindIdx >= labels.size) return@forEach
                            val nutrientCode = labels[kindIdx]
                            val kindRel  = row.num("kindrelative")?.toInt() ?: 0
                            val reflevel = REFLEVEL_MAP[kindRel] ?: "MIN"
                            val quantite = row.num("value")?.toDouble() ?: return@forEach
                            val unitKind = row.num("unitKind")?.toInt() ?: 0
                            val bibRefId = row.str("refBiblio")
                                ?.takeIf { it.length >= 32 && it.contains("-") }
                            val uuid = "${refId}_${table}_${kindIdx}_${kindRel}"
                            referenceEvDao.insertNutrient(
                                fr.vetbrain.vetnutri_mp.DataBase.ReferenceEvNutrientEntity(
                                    uuid = uuid,
                                    referenceEvId = refId,
                                    nutrientCode = nutrientCode,
                                    reflevel = reflevel,
                                    quantite = quantite,
                                    uniteId = 0,
                                    uniteReqId = unitKind,
                                    biblioRefId = bibRefId
                                )
                            )
                            impNut++
                        } catch (_: Exception) {}
                    }
                }
            }
            if (impNut > 0) log("Besoins nutritionnels importés: $impNut")
        }
    } else {
        log("Base de références introuvable dans $dbFolderPath — références non importées")
    }

    validateAndRepairConsultationReferences()

    if (errors.isNotEmpty()) {
        log("${errors.size} erreur(s) rencontrée(s) :")
        errors.forEach { log("  • $it") }
    }
    log("Migration terminée.")

    LegacyMigrationViewModel.MigrationResult(
        imported = LegacyMigrationViewModel.MigrationCounts(
            animals = impAnimals,
            consultations = impConsults,
            rations = impRations,
            weights = impWeights,
            foods = impFoods,
            biblioRefs = impBiblioRefs,
            equations = impEquations,
            references = impReferences
        ),
        skipped = LegacyMigrationViewModel.MigrationCounts(
            animals = skipAnimals,
            consultations = skipConsults,
            rations = skipRations,
            weights = 0,
            foods = skipFoods,
            biblioRefs = skipBiblioRefs,
            equations = skipEquations,
            references = skipReferences
        ),
        errors = errors
    )
}
