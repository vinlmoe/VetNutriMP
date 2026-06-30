package fr.vetbrain.vetnutri_mp.Service

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import fr.vetbrain.vetnutri_mp.DataBase.*
import fr.vetbrain.vetnutri_mp.DataBase.getDatabasePath
import fr.vetbrain.vetnutri_mp.Utils.NasDatabaseChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object NasSyncService {

    sealed class SyncResult {
        data class Ok(val syncedAt: Long, val pushed: Int, val pulled: Int) : SyncResult()
        data class NasVersionTooHigh(val nasVersion: Int) : SyncResult()
        data class NasIncompatible(val reason: String) : SyncResult()
        object NasNotConfigured : SyncResult()
        data class Error(val exception: Exception) : SyncResult()
    }

    suspend fun syncIfConfigured(localDb: AppDatabase): SyncResult = withContext(Dispatchers.IO) {
        val nasPath = NasDatabaseChecker.readNasDbPathSync().trim()
        if (nasPath.isBlank()) return@withContext SyncResult.NasNotConfigured
        val lastSync = NasDatabaseChecker.readLastSyncTimestampSync()
        val result = sync(nasPath, localDb, lastSync)
        if (result is SyncResult.Ok) {
            NasDatabaseChecker.saveLastSyncTimestampSync(result.syncedAt)
        }
        result
    }

    suspend fun sync(nasPath: String, localDb: AppDatabase, lastSync: Long): SyncResult =
        withContext(Dispatchers.IO) {
            try {
                val compat = NasDatabaseChecker.checkNasCompatibility(nasPath)
                when (compat) {
                    is NasDatabaseChecker.NasCompatibility.Inaccessible ->
                        return@withContext SyncResult.NasIncompatible("NAS inaccessible : répertoire non accessible")
                    is NasDatabaseChecker.NasCompatibility.VersionTooHigh ->
                        return@withContext SyncResult.NasVersionTooHigh(compat.fileVersion)
                    NasDatabaseChecker.NasCompatibility.NoPathConfigured ->
                        return@withContext SyncResult.NasNotConfigured
                    NasDatabaseChecker.NasCompatibility.Ok -> {}
                }

                val nasFile = File(nasPath)
                val now = System.currentTimeMillis()
                val driver = BundledSQLiteDriver()

                if (!nasFile.exists()) {
                    nasFile.parentFile?.mkdirs()
                    val localConn = driver.open(getDatabasePath())
                    try {
                        localConn.prepare("VACUUM INTO ?").use { stmt ->
                            stmt.bindText(1, nasPath)
                            stmt.step()
                        }
                    } finally {
                        localConn.close()
                    }
                    return@withContext SyncResult.Ok(syncedAt = now, pushed = 0, pulled = 0)
                }

                val nasConn = driver.open(nasPath)
                try {
                    val nasVersion = getNasUserVersion(nasConn)
                    if (nasVersion < 37) migrateNasTo37(nasConn)

                    var pushed = 0
                    var pulled = 0

                    pulled += pullAnimals(nasConn, localDb, lastSync)
                    pushed += pushAnimals(nasConn, localDb, lastSync)

                    pulled += pullConsultations(nasConn, localDb, lastSync)
                    pushed += pushConsultations(nasConn, localDb, lastSync)

                    pulled += pullRations(nasConn, localDb, lastSync)
                    pushed += pushRations(nasConn, localDb, lastSync)

                    pulled += pullAlimentRations(nasConn, localDb, lastSync)
                    pushed += pushAlimentRations(nasConn, localDb, lastSync)

                    pulled += pullWeights(nasConn, localDb, lastSync)
                    pushed += pushWeights(nasConn, localDb, lastSync)

                    pulled += pullRecettes(nasConn, localDb, lastSync)
                    pushed += pushRecettes(nasConn, localDb, lastSync)

                    pulled += pullAlimentRecettes(nasConn, localDb, lastSync)
                    pushed += pushAlimentRecettes(nasConn, localDb, lastSync)

                    pulled += pullConsultationKeywords(nasConn, localDb, lastSync)
                    pushed += pushConsultationKeywords(nasConn, localDb, lastSync)

                    pulled += pullFoods(nasConn, localDb, lastSync)
                    pushed += pushFoods(nasConn, localDb, lastSync)

                    pulled += pullEquations(nasConn, localDb, lastSync)
                    pushed += pushEquations(nasConn, localDb, lastSync)

                    pulled += pullBiblioRefs(nasConn, localDb, lastSync)
                    pushed += pushBiblioRefs(nasConn, localDb, lastSync)

                    pulled += pullReferenceEvs(nasConn, localDb, lastSync)
                    pushed += pushReferenceEvs(nasConn, localDb, lastSync)

                    pulled += pullCustomNutrients(nasConn, localDb, lastSync)
                    pushed += pushCustomNutrients(nasConn, localDb, lastSync)

                    applyTombstonesFromNas(nasConn, localDb, lastSync)
                    pushTombstonesToNas(nasConn, localDb, lastSync)

                    SyncResult.Ok(syncedAt = now, pushed = pushed, pulled = pulled)
                } finally {
                    nasConn.close()
                }
            } catch (e: Exception) {
                SyncResult.Error(e)
            }
        }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun SQLiteConnection.exec(sql: String) = prepare(sql).use { it.step() }
    private fun SQLiteConnection.beginTx() = exec("BEGIN TRANSACTION")
    private fun SQLiteConnection.commitTx() = exec("COMMIT")

    private fun SQLiteConnection.queryTs(table: String, pkCol: String, pkVal: String): Long =
        prepare("SELECT updatedAtMs FROM $table WHERE $pkCol = ?").use { stmt ->
            stmt.bindText(1, pkVal)
            if (stmt.step()) stmt.getLong(0) else -1L
        }

    private fun getNasUserVersion(conn: SQLiteConnection): Int =
        conn.prepare("PRAGMA user_version").use { if (it.step()) it.getLong(0).toInt() else 0 }

    private fun SQLiteConnection.bindTextOrNull(stmt: androidx.sqlite.SQLiteStatement, idx: Int, v: String?) {
        if (v == null) stmt.bindNull(idx) else stmt.bindText(idx, v)
    }

    private fun migrateNasTo37(conn: SQLiteConnection) {
        conn.beginTx()
        listOf(
            "ALTER TABLE ANIMALS ADD COLUMN updatedAtMs INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE CONSULTATIONS ADD COLUMN updatedAtMs INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE RATIONS ADD COLUMN updatedAtMs INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE WEIGHT ADD COLUMN updatedAtMs INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE ALIMENTS ADD COLUMN updatedAtMs INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE FOOD ADD COLUMN updatedAtMs INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE EQUATIONS ADD COLUMN updatedAtMs INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE BIBLIO_REFS ADD COLUMN updatedAtMs INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE REFERENCE_EV ADD COLUMN updatedAtMs INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE RECETTES ADD COLUMN updatedAtMs INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE ALIMENTS_RECETTES ADD COLUMN updatedAtMs INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE CONSULTATION_KEYWORDS ADD COLUMN updatedAtMs INTEGER NOT NULL DEFAULT 0",
            "ALTER TABLE CUSTOM_NUTRIENTS ADD COLUMN updatedAtMs INTEGER NOT NULL DEFAULT 0"
        ).forEach { try { conn.exec(it) } catch (_: Exception) {} }
        try {
            conn.exec("""CREATE TABLE IF NOT EXISTS SYNC_TOMBSTONES (
                uuid TEXT NOT NULL, entityType TEXT NOT NULL, deletedAtMs INTEGER NOT NULL,
                PRIMARY KEY (uuid, entityType))""")
        } catch (_: Exception) {}
        conn.commitTx()
        conn.prepare("PRAGMA user_version = 37").use { it.step() }
    }

    private fun <T> SQLiteConnection.readAll(
        sql: String,
        since: Long,
        map: (androidx.sqlite.SQLiteStatement) -> T
    ): List<T> {
        val result = mutableListOf<T>()
        prepare(sql).use { stmt ->
            stmt.bindLong(1, since)
            while (stmt.step()) result.add(map(stmt))
        }
        return result
    }

    // ─── ANIMALS ─────────────────────────────────────────────────────────────

    private suspend fun pullAnimals(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val rows = nasConn.readAll(
            "SELECT uuid,nom,dead,id,sexId,specieId,ownerName,birthdate,race,summary,jsonbinId,exam,examStudentId,examStudentNumber,examExerciseId,updatedAtMs FROM ANIMALS WHERE updatedAtMs > ?",
            lastSync
        ) { s -> AnimalEntity(
            uuid = s.getText(0),
            nom = if (s.isNull(1)) null else s.getText(1),
            dead = s.getLong(2) != 0L,
            id = if (s.isNull(3)) null else s.getText(3),
            sexId = s.getLong(4).toInt(),
            specieId = if (s.isNull(5)) null else s.getText(5),
            ownerName = if (s.isNull(6)) null else s.getText(6),
            birthdate = if (s.isNull(7)) null else s.getText(7),
            race = if (s.isNull(8)) null else s.getText(8),
            summary = if (s.isNull(9)) null else s.getText(9),
            jsonbinId = if (s.isNull(10)) null else s.getText(10),
            exam = s.getLong(11) != 0L,
            examStudentId = if (s.isNull(12)) null else s.getText(12),
            examStudentNumber = if (s.isNull(13)) null else s.getText(13),
            examExerciseId = if (s.isNull(14)) null else s.getText(14),
            updatedAtMs = s.getLong(15)
        )}
        var count = 0
        rows.forEach { entity ->
            val local = localDb.animalDao().getAnimalByUuid(entity.uuid)
            if (local == null || local.updatedAtMs < entity.updatedAtMs) {
                localDb.animalDao().upsert(entity)
                count++
            }
        }
        return count
    }

    private suspend fun pushAnimals(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val entities = localDb.animalDao().getAnimalsUpdatedAfter(lastSync)
        var count = 0
        nasConn.beginTx()
        entities.forEach { e ->
            val nasTs = nasConn.queryTs("ANIMALS", "uuid", e.uuid)
            if (e.updatedAtMs > nasTs) {
                nasConn.prepare("INSERT OR REPLACE INTO ANIMALS (uuid,nom,dead,id,sexId,specieId,ownerName,birthdate,race,summary,jsonbinId,exam,examStudentId,examStudentNumber,examExerciseId,updatedAtMs) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)").use { s ->
                    s.bindText(1, e.uuid)
                    if (e.nom == null) s.bindNull(2) else s.bindText(2, e.nom)
                    s.bindLong(3, if (e.dead) 1L else 0L)
                    if (e.id == null) s.bindNull(4) else s.bindText(4, e.id)
                    s.bindLong(5, e.sexId.toLong())
                    if (e.specieId == null) s.bindNull(6) else s.bindText(6, e.specieId)
                    if (e.ownerName == null) s.bindNull(7) else s.bindText(7, e.ownerName)
                    if (e.birthdate == null) s.bindNull(8) else s.bindText(8, e.birthdate)
                    if (e.race == null) s.bindNull(9) else s.bindText(9, e.race)
                    if (e.summary == null) s.bindNull(10) else s.bindText(10, e.summary)
                    if (e.jsonbinId == null) s.bindNull(11) else s.bindText(11, e.jsonbinId)
                    s.bindLong(12, if (e.exam) 1L else 0L)
                    if (e.examStudentId == null) s.bindNull(13) else s.bindText(13, e.examStudentId)
                    if (e.examStudentNumber == null) s.bindNull(14) else s.bindText(14, e.examStudentNumber)
                    if (e.examExerciseId == null) s.bindNull(15) else s.bindText(15, e.examExerciseId)
                    s.bindLong(16, e.updatedAtMs)
                    s.step()
                }
                count++
            }
        }
        nasConn.commitTx()
        return count
    }

    // ─── CONSULTATIONS ────────────────────────────────────────────────────────

    private suspend fun pullConsultations(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val rows = nasConn.readAll(
            "SELECT uuid,idAnim,date,objectConsult,observation,cRendu,weight,idealWeight,water,bodyFat,methodAnalysis,BCS,k1Id,k1Value,k2Id,k2Value,k3Id,k3Value,k4Id,k4Value,k5Id,k5Value,nLittle,pAdult,coefGes,coefLact,MCS,referenceGeneraleId,referencesMaladiesJson,keywordsJson,coefficientAjustement,prescriptionAnamnese,prescriptionExamenClinique,prescriptionFacteurNutritionnelClef,prescriptionAdditionalText,prescriptionSelectedConseilIdsJson,prescriptionLocalHtmlSectionsJson,prescriptionSelectedRationIdsJson,updatedAtMs FROM CONSULTATIONS WHERE updatedAtMs > ?",
            lastSync
        ) { s -> ConsultationEntity(
            uuid = s.getText(0), idAnim = s.getText(1),
            date = if (s.isNull(2)) null else s.getText(2),
            objectConsult = if (s.isNull(3)) null else s.getText(3),
            observation = if (s.isNull(4)) null else s.getText(4),
            cRendu = if (s.isNull(5)) null else s.getText(5),
            weight = s.getDouble(6), idealWeight = s.getDouble(7),
            water = s.getDouble(8), bodyFat = s.getDouble(9),
            methodAnalysis = if (s.isNull(10)) null else s.getText(10),
            BCS = s.getLong(11).toInt(),
            k1Id = if (s.isNull(12)) null else s.getText(12), k1Value = s.getDouble(13),
            k2Id = if (s.isNull(14)) null else s.getText(14), k2Value = s.getDouble(15),
            k3Id = if (s.isNull(16)) null else s.getText(16), k3Value = s.getDouble(17),
            k4Id = if (s.isNull(18)) null else s.getText(18), k4Value = s.getDouble(19),
            k5Id = if (s.isNull(20)) null else s.getText(20), k5Value = s.getDouble(21),
            nLittle = s.getLong(22).toInt(), pAdult = s.getDouble(23),
            coefGes = s.getLong(24).toInt(), coefLact = s.getLong(25).toInt(),
            MCS = s.getLong(26).toInt(),
            referenceGeneraleId = if (s.isNull(27)) null else s.getText(27),
            referencesMaladiesJson = if (s.isNull(28)) null else s.getText(28),
            keywordsJson = if (s.isNull(29)) null else s.getText(29),
            coefficientAjustement = s.getDouble(30),
            prescriptionAnamnese = if (s.isNull(31)) null else s.getText(31),
            prescriptionExamenClinique = if (s.isNull(32)) null else s.getText(32),
            prescriptionFacteurNutritionnelClef = if (s.isNull(33)) null else s.getText(33),
            prescriptionAdditionalText = if (s.isNull(34)) null else s.getText(34),
            prescriptionSelectedConseilIdsJson = if (s.isNull(35)) null else s.getText(35),
            prescriptionLocalHtmlSectionsJson = if (s.isNull(36)) null else s.getText(36),
            prescriptionSelectedRationIdsJson = if (s.isNull(37)) null else s.getText(37),
            updatedAtMs = s.getLong(38)
        )}
        var count = 0
        rows.forEach { entity ->
            val local = localDb.consultationDao().getConsultationByUuid(entity.uuid)
            if (local == null || local.updatedAtMs < entity.updatedAtMs) {
                localDb.consultationDao().upsertConsultation(entity)
                count++
            }
        }
        return count
    }

    private suspend fun pushConsultations(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val entities = localDb.consultationDao().getConsultationsUpdatedAfter(lastSync)
        var count = 0
        nasConn.beginTx()
        entities.forEach { e ->
            val nasTs = nasConn.queryTs("CONSULTATIONS", "uuid", e.uuid)
            if (e.updatedAtMs > nasTs) {
                nasConn.prepare("INSERT OR REPLACE INTO CONSULTATIONS (uuid,idAnim,date,objectConsult,observation,cRendu,weight,idealWeight,water,bodyFat,methodAnalysis,BCS,k1Id,k1Value,k2Id,k2Value,k3Id,k3Value,k4Id,k4Value,k5Id,k5Value,nLittle,pAdult,coefGes,coefLact,MCS,referenceGeneraleId,referencesMaladiesJson,keywordsJson,coefficientAjustement,prescriptionAnamnese,prescriptionExamenClinique,prescriptionFacteurNutritionnelClef,prescriptionAdditionalText,prescriptionSelectedConseilIdsJson,prescriptionLocalHtmlSectionsJson,prescriptionSelectedRationIdsJson,updatedAtMs) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)").use { s ->
                    s.bindText(1, e.uuid); s.bindText(2, e.idAnim)
                    if (e.date == null) s.bindNull(3) else s.bindText(3, e.date)
                    if (e.objectConsult == null) s.bindNull(4) else s.bindText(4, e.objectConsult)
                    if (e.observation == null) s.bindNull(5) else s.bindText(5, e.observation)
                    if (e.cRendu == null) s.bindNull(6) else s.bindText(6, e.cRendu)
                    s.bindDouble(7, e.weight); s.bindDouble(8, e.idealWeight)
                    s.bindDouble(9, e.water); s.bindDouble(10, e.bodyFat)
                    if (e.methodAnalysis == null) s.bindNull(11) else s.bindText(11, e.methodAnalysis)
                    s.bindLong(12, e.BCS.toLong())
                    if (e.k1Id == null) s.bindNull(13) else s.bindText(13, e.k1Id); s.bindDouble(14, e.k1Value)
                    if (e.k2Id == null) s.bindNull(15) else s.bindText(15, e.k2Id); s.bindDouble(16, e.k2Value)
                    if (e.k3Id == null) s.bindNull(17) else s.bindText(17, e.k3Id); s.bindDouble(18, e.k3Value)
                    if (e.k4Id == null) s.bindNull(19) else s.bindText(19, e.k4Id); s.bindDouble(20, e.k4Value)
                    if (e.k5Id == null) s.bindNull(21) else s.bindText(21, e.k5Id); s.bindDouble(22, e.k5Value)
                    s.bindLong(23, e.nLittle.toLong()); s.bindDouble(24, e.pAdult)
                    s.bindLong(25, e.coefGes.toLong()); s.bindLong(26, e.coefLact.toLong())
                    s.bindLong(27, e.MCS.toLong())
                    if (e.referenceGeneraleId == null) s.bindNull(28) else s.bindText(28, e.referenceGeneraleId)
                    if (e.referencesMaladiesJson == null) s.bindNull(29) else s.bindText(29, e.referencesMaladiesJson)
                    if (e.keywordsJson == null) s.bindNull(30) else s.bindText(30, e.keywordsJson)
                    s.bindDouble(31, e.coefficientAjustement)
                    if (e.prescriptionAnamnese == null) s.bindNull(32) else s.bindText(32, e.prescriptionAnamnese)
                    if (e.prescriptionExamenClinique == null) s.bindNull(33) else s.bindText(33, e.prescriptionExamenClinique)
                    if (e.prescriptionFacteurNutritionnelClef == null) s.bindNull(34) else s.bindText(34, e.prescriptionFacteurNutritionnelClef)
                    if (e.prescriptionAdditionalText == null) s.bindNull(35) else s.bindText(35, e.prescriptionAdditionalText)
                    if (e.prescriptionSelectedConseilIdsJson == null) s.bindNull(36) else s.bindText(36, e.prescriptionSelectedConseilIdsJson)
                    if (e.prescriptionLocalHtmlSectionsJson == null) s.bindNull(37) else s.bindText(37, e.prescriptionLocalHtmlSectionsJson)
                    if (e.prescriptionSelectedRationIdsJson == null) s.bindNull(38) else s.bindText(38, e.prescriptionSelectedRationIdsJson)
                    s.bindLong(39, e.updatedAtMs)
                    s.step()
                }
                count++
            }
        }
        nasConn.commitTx()
        return count
    }

    // ─── RATIONS ─────────────────────────────────────────────────────────────

    private suspend fun pullRations(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val rows = nasConn.readAll(
            "SELECT uuid,idConsult,name,coef,actual,number,espece,recette,description,updatedAtMs FROM RATIONS WHERE updatedAtMs > ?",
            lastSync
        ) { s -> RationEntity(
            uuid = s.getText(0), idConsult = s.getText(1),
            name = if (s.isNull(2)) null else s.getText(2),
            coef = s.getDouble(3), actual = s.getLong(4) != 0L,
            number = s.getLong(5).toInt(),
            espece = if (s.isNull(6)) null else s.getText(6),
            recette = s.getLong(7) != 0L,
            description = if (s.isNull(8)) null else s.getText(8),
            updatedAtMs = s.getLong(9)
        )}
        var count = 0
        rows.forEach { entity ->
            val local = localDb.consultationDao().getRationByUuid(entity.uuid)
            if (local == null || local.updatedAtMs < entity.updatedAtMs) {
                localDb.consultationDao().upsertRation(entity)
                count++
            }
        }
        return count
    }

    private suspend fun pushRations(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val entities = localDb.consultationDao().getRationsUpdatedAfter(lastSync)
        var count = 0
        nasConn.beginTx()
        entities.forEach { e ->
            val nasTs = nasConn.queryTs("RATIONS", "uuid", e.uuid)
            if (e.updatedAtMs > nasTs) {
                nasConn.prepare("INSERT OR REPLACE INTO RATIONS (uuid,idConsult,name,coef,actual,number,espece,recette,description,updatedAtMs) VALUES (?,?,?,?,?,?,?,?,?,?)").use { s ->
                    s.bindText(1, e.uuid); s.bindText(2, e.idConsult)
                    if (e.name == null) s.bindNull(3) else s.bindText(3, e.name)
                    s.bindDouble(4, e.coef); s.bindLong(5, if (e.actual) 1L else 0L)
                    s.bindLong(6, e.number.toLong())
                    if (e.espece == null) s.bindNull(7) else s.bindText(7, e.espece)
                    s.bindLong(8, if (e.recette) 1L else 0L)
                    if (e.description == null) s.bindNull(9) else s.bindText(9, e.description)
                    s.bindLong(10, e.updatedAtMs)
                    s.step()
                }
                count++
            }
        }
        nasConn.commitTx()
        return count
    }

    // ─── ALIMENTS RATION ─────────────────────────────────────────────────────

    private suspend fun pullAlimentRations(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val rows = nasConn.readAll(
            "SELECT uuid,refAlimUnif,refRation,quantity,refTarget,updatedAtMs FROM ALIMENTS WHERE updatedAtMs > ?",
            lastSync
        ) { s -> AlimentRationEntity(
            uuid = s.getText(0), refAlimUnif = s.getText(1), refRation = s.getText(2),
            quantity = s.getDouble(3), refTarget = s.getLong(4).toInt(), updatedAtMs = s.getLong(5)
        )}
        var count = 0
        rows.forEach { entity ->
            val local = localDb.consultationDao().getAlimentRationByUuid(entity.uuid)
            if (local == null || local.updatedAtMs < entity.updatedAtMs) {
                localDb.consultationDao().upsertAlimentRation(entity)
                count++
            }
        }
        return count
    }

    private suspend fun pushAlimentRations(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val entities = localDb.consultationDao().getAlimentRationsUpdatedAfter(lastSync)
        var count = 0
        nasConn.beginTx()
        entities.forEach { e ->
            val nasTs = nasConn.queryTs("ALIMENTS", "uuid", e.uuid)
            if (e.updatedAtMs > nasTs) {
                nasConn.prepare("INSERT OR REPLACE INTO ALIMENTS (uuid,refAlimUnif,refRation,quantity,refTarget,updatedAtMs) VALUES (?,?,?,?,?,?)").use { s ->
                    s.bindText(1, e.uuid); s.bindText(2, e.refAlimUnif); s.bindText(3, e.refRation)
                    s.bindDouble(4, e.quantity); s.bindLong(5, e.refTarget.toLong()); s.bindLong(6, e.updatedAtMs)
                    s.step()
                }
                count++
            }
        }
        nasConn.commitTx()
        return count
    }

    // ─── WEIGHT ──────────────────────────────────────────────────────────────

    private suspend fun pullWeights(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val rows = nasConn.readAll(
            "SELECT uuid,refAnimal,date,value,updatedAtMs FROM WEIGHT WHERE updatedAtMs > ?",
            lastSync
        ) { s -> WeightEntity(
            uuid = s.getText(0), refAnimal = s.getText(1), date = s.getText(2),
            value = s.getDouble(3), updatedAtMs = s.getLong(4)
        )}
        var count = 0
        rows.forEach { entity ->
            val local = localDb.consultationDao().getWeightByUuid(entity.uuid)
            if (local == null || local.updatedAtMs < entity.updatedAtMs) {
                localDb.consultationDao().upsertWeight(entity)
                count++
            }
        }
        return count
    }

    private suspend fun pushWeights(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val entities = localDb.consultationDao().getWeightsUpdatedAfter(lastSync)
        var count = 0
        nasConn.beginTx()
        entities.forEach { e ->
            val nasTs = nasConn.queryTs("WEIGHT", "uuid", e.uuid)
            if (e.updatedAtMs > nasTs) {
                nasConn.prepare("INSERT OR REPLACE INTO WEIGHT (uuid,refAnimal,date,value,updatedAtMs) VALUES (?,?,?,?,?)").use { s ->
                    s.bindText(1, e.uuid); s.bindText(2, e.refAnimal); s.bindText(3, e.date)
                    s.bindDouble(4, e.value); s.bindLong(5, e.updatedAtMs)
                    s.step()
                }
                count++
            }
        }
        nasConn.commitTx()
        return count
    }

    // ─── RECETTES ────────────────────────────────────────────────────────────

    private suspend fun pullRecettes(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val rows = nasConn.readAll(
            "SELECT uuid,name,number,espece,description,updatedAtMs FROM RECETTES WHERE updatedAtMs > ?",
            lastSync
        ) { s -> RecetteEntity(
            uuid = s.getText(0),
            name = if (s.isNull(1)) null else s.getText(1),
            number = s.getLong(2).toInt(),
            espece = if (s.isNull(3)) null else s.getText(3),
            description = if (s.isNull(4)) null else s.getText(4),
            updatedAtMs = s.getLong(5)
        )}
        var count = 0
        rows.forEach { entity ->
            val local = localDb.recipeDao().getRecetteByUuid(entity.uuid)
            if (local == null || local.updatedAtMs < entity.updatedAtMs) {
                localDb.recipeDao().upsertRecipe(entity)
                count++
            }
        }
        return count
    }

    private suspend fun pushRecettes(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val entities = localDb.recipeDao().getRecettesUpdatedAfter(lastSync)
        var count = 0
        nasConn.beginTx()
        entities.forEach { e ->
            val nasTs = nasConn.queryTs("RECETTES", "uuid", e.uuid)
            if (e.updatedAtMs > nasTs) {
                nasConn.prepare("INSERT OR REPLACE INTO RECETTES (uuid,name,number,espece,description,updatedAtMs) VALUES (?,?,?,?,?,?)").use { s ->
                    s.bindText(1, e.uuid)
                    if (e.name == null) s.bindNull(2) else s.bindText(2, e.name)
                    s.bindLong(3, e.number.toLong())
                    if (e.espece == null) s.bindNull(4) else s.bindText(4, e.espece)
                    if (e.description == null) s.bindNull(5) else s.bindText(5, e.description)
                    s.bindLong(6, e.updatedAtMs)
                    s.step()
                }
                count++
            }
        }
        nasConn.commitTx()
        return count
    }

    // ─── ALIMENTS RECETTES ───────────────────────────────────────────────────

    private suspend fun pullAlimentRecettes(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val rows = nasConn.readAll(
            "SELECT uuid,refAlimUnif,refRecipe,quantity,refTarget,updatedAtMs FROM ALIMENTS_RECETTES WHERE updatedAtMs > ?",
            lastSync
        ) { s -> AlimentRecetteEntity(
            uuid = s.getText(0), refAlimUnif = s.getText(1), refRecipe = s.getText(2),
            quantity = s.getDouble(3), refTarget = s.getLong(4).toInt(), updatedAtMs = s.getLong(5)
        )}
        var count = 0
        rows.forEach { entity ->
            val local = localDb.recipeDao().getAlimentRecetteByUuid(entity.uuid)
            if (local == null || local.updatedAtMs < entity.updatedAtMs) {
                localDb.recipeDao().upsertAlimentRecette(entity)
                count++
            }
        }
        return count
    }

    private suspend fun pushAlimentRecettes(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val entities = localDb.recipeDao().getAlimentRecettesUpdatedAfter(lastSync)
        var count = 0
        nasConn.beginTx()
        entities.forEach { e ->
            val nasTs = nasConn.queryTs("ALIMENTS_RECETTES", "uuid", e.uuid)
            if (e.updatedAtMs > nasTs) {
                nasConn.prepare("INSERT OR REPLACE INTO ALIMENTS_RECETTES (uuid,refAlimUnif,refRecipe,quantity,refTarget,updatedAtMs) VALUES (?,?,?,?,?,?)").use { s ->
                    s.bindText(1, e.uuid); s.bindText(2, e.refAlimUnif); s.bindText(3, e.refRecipe)
                    s.bindDouble(4, e.quantity); s.bindLong(5, e.refTarget.toLong()); s.bindLong(6, e.updatedAtMs)
                    s.step()
                }
                count++
            }
        }
        nasConn.commitTx()
        return count
    }

    // ─── CONSULTATION KEYWORDS ────────────────────────────────────────────────

    private suspend fun pullConsultationKeywords(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val rows = nasConn.readAll(
            "SELECT uuid,label,updatedAtMs FROM CONSULTATION_KEYWORDS WHERE updatedAtMs > ?",
            lastSync
        ) { s -> ConsultationKeywordEntity(uuid = s.getText(0), label = s.getText(1), updatedAtMs = s.getLong(2)) }
        var count = 0
        rows.forEach { entity ->
            val existing = localDb.consultationDao().getConsultationKeywordByLabel(entity.label)
            if (existing == null) {
                localDb.consultationDao().insertConsultationKeyword(entity)
                count++
            }
        }
        return count
    }

    private suspend fun pushConsultationKeywords(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val entities = localDb.consultationDao().getKeywordsUpdatedAfter(lastSync)
        var count = 0
        nasConn.beginTx()
        entities.forEach { e ->
            nasConn.prepare("INSERT OR IGNORE INTO CONSULTATION_KEYWORDS (uuid,label,updatedAtMs) VALUES (?,?,?)").use { s ->
                s.bindText(1, e.uuid); s.bindText(2, e.label); s.bindLong(3, e.updatedAtMs)
                s.step()
            }
            count++
        }
        nasConn.commitTx()
        return count
    }

    // ─── FOOD ─────────────────────────────────────────────────────────────────

    private suspend fun pullFoods(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val rows = nasConn.readAll(
            "SELECT uuid,groupAlim,typeAlim,ingredients,price,categPrice,brand,gamme,cont,unitPres,quantityPres,version,date,nameDef,consistent,deprecated,DataB,RefRation,RefAlimUnif,especesJson,indicationsJson,name,quantite,lastUpdateDate,imageRef,updatedAtMs FROM FOOD WHERE updatedAtMs > ?",
            lastSync
        ) { s -> FoodEntity(
            uuid = s.getText(0), groupAlim = s.getLong(1).toInt(), typeAlim = s.getLong(2).toInt(),
            ingredients = s.getText(3), price = s.getDouble(4), categPrice = s.getText(5),
            brand = s.getText(6), gamme = s.getText(7), cont = s.getText(8),
            unitPres = s.getLong(9).toInt(), quantityPres = s.getDouble(10),
            version = s.getLong(11).toInt(), date = s.getText(12), nameDef = s.getText(13),
            consistent = s.getLong(14).toInt(), deprecated = s.getLong(15).toInt(),
            DataB = s.getText(16),
            RefRation = if (s.isNull(17)) null else s.getText(17),
            RefAlimUnif = if (s.isNull(18)) null else s.getText(18),
            especesJson = if (s.isNull(19)) null else s.getText(19),
            indicationsJson = if (s.isNull(20)) null else s.getText(20),
            name = if (s.isNull(21)) null else s.getText(21),
            quantite = s.getDouble(22),
            lastUpdateDate = if (s.isNull(23)) null else s.getText(23),
            imageRef = if (s.isNull(24)) null else s.getText(24),
            updatedAtMs = s.getLong(25)
        )}
        var count = 0
        rows.forEach { entity ->
            val local = localDb.foodDao().getFoodById(entity.uuid)
            if (local == null || local.updatedAtMs < entity.updatedAtMs) {
                localDb.foodDao().insertFood(entity)
                pullFoodChildren(nasConn, localDb, entity.uuid)
                count++
            }
        }
        return count
    }

    private suspend fun pullFoodChildren(nasConn: SQLiteConnection, localDb: AppDatabase, foodUuid: String) {
        localDb.nutrientValueDao().deleteAllNutrientValuesForAliment(foodUuid)
        val nutrients = mutableListOf<NutrientValueEntity>()
        nasConn.prepare("SELECT refAliment,nutrientLabel,value FROM NUTRIENT_VALUES WHERE refAliment = ?").use { s ->
            s.bindText(1, foodUuid)
            while (s.step()) nutrients.add(NutrientValueEntity(s.getText(0), s.getText(1), s.getDouble(2)))
        }
        if (nutrients.isNotEmpty()) localDb.nutrientValueDao().insertNutrientValues(nutrients)

        localDb.energyPerSpeciesDao().deleteForAliment(foodUuid)
        val energies = mutableListOf<EnergyPerSpeciesEntity>()
        nasConn.prepare("SELECT refAliment,espece,value FROM ENERGY_PER_SPECIES WHERE refAliment = ?").use { s ->
            s.bindText(1, foodUuid)
            while (s.step()) energies.add(EnergyPerSpeciesEntity(s.getText(0), s.getText(1), s.getDouble(2)))
        }
        if (energies.isNotEmpty()) localDb.energyPerSpeciesDao().insert(energies)
    }

    private suspend fun pushFoods(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val entities = localDb.foodDao().getFoodsUpdatedAfter(lastSync)
        var count = 0
        nasConn.beginTx()
        entities.forEach { e ->
            val nasTs = nasConn.queryTs("FOOD", "uuid", e.uuid)
            if (e.updatedAtMs > nasTs) {
                nasConn.prepare("INSERT OR REPLACE INTO FOOD (uuid,groupAlim,typeAlim,ingredients,price,categPrice,brand,gamme,cont,unitPres,quantityPres,version,date,nameDef,consistent,deprecated,DataB,RefRation,RefAlimUnif,especesJson,indicationsJson,name,quantite,lastUpdateDate,imageRef,updatedAtMs) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)").use { s ->
                    s.bindText(1, e.uuid); s.bindLong(2, e.groupAlim.toLong()); s.bindLong(3, e.typeAlim.toLong())
                    s.bindText(4, e.ingredients); s.bindDouble(5, e.price); s.bindText(6, e.categPrice)
                    s.bindText(7, e.brand); s.bindText(8, e.gamme); s.bindText(9, e.cont)
                    s.bindLong(10, e.unitPres.toLong()); s.bindDouble(11, e.quantityPres)
                    s.bindLong(12, e.version.toLong()); s.bindText(13, e.date); s.bindText(14, e.nameDef)
                    s.bindLong(15, e.consistent.toLong()); s.bindLong(16, e.deprecated.toLong())
                    s.bindText(17, e.DataB)
                    if (e.RefRation == null) s.bindNull(18) else s.bindText(18, e.RefRation)
                    if (e.RefAlimUnif == null) s.bindNull(19) else s.bindText(19, e.RefAlimUnif)
                    if (e.especesJson == null) s.bindNull(20) else s.bindText(20, e.especesJson)
                    if (e.indicationsJson == null) s.bindNull(21) else s.bindText(21, e.indicationsJson)
                    if (e.name == null) s.bindNull(22) else s.bindText(22, e.name)
                    s.bindDouble(23, e.quantite)
                    if (e.lastUpdateDate == null) s.bindNull(24) else s.bindText(24, e.lastUpdateDate)
                    if (e.imageRef == null) s.bindNull(25) else s.bindText(25, e.imageRef)
                    s.bindLong(26, e.updatedAtMs)
                    s.step()
                }
                pushFoodChildrenToNas(nasConn, localDb, e.uuid)
                count++
            }
        }
        nasConn.commitTx()
        return count
    }

    private suspend fun pushFoodChildrenToNas(nasConn: SQLiteConnection, localDb: AppDatabase, foodUuid: String) {
        nasConn.prepare("DELETE FROM NUTRIENT_VALUES WHERE refAliment = ?").use { s -> s.bindText(1, foodUuid); s.step() }
        localDb.nutrientValueDao().getNutrientValues(foodUuid).forEach { nv ->
            nasConn.prepare("INSERT INTO NUTRIENT_VALUES (refAliment,nutrientLabel,value) VALUES (?,?,?)").use { s ->
                s.bindText(1, nv.refAliment); s.bindText(2, nv.nutrientLabel); s.bindDouble(3, nv.value); s.step()
            }
        }
        nasConn.prepare("DELETE FROM ENERGY_PER_SPECIES WHERE refAliment = ?").use { s -> s.bindText(1, foodUuid); s.step() }
        localDb.energyPerSpeciesDao().getForAliment(foodUuid).forEach { ep ->
            nasConn.prepare("INSERT INTO ENERGY_PER_SPECIES (refAliment,espece,value) VALUES (?,?,?)").use { s ->
                s.bindText(1, ep.refAliment); s.bindText(2, ep.espece); s.bindDouble(3, ep.value); s.step()
            }
        }
    }

    // ─── EQUATIONS ───────────────────────────────────────────────────────────

    private suspend fun pullEquations(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val rows = nasConn.readAll(
            "SELECT uuid,name,description,equationScript,specie,kind,consistent,bibRef,variables,nutrient,ratio,updatedAtMs FROM EQUATIONS WHERE updatedAtMs > ?",
            lastSync
        ) { s -> EquationEntity(
            uuid = s.getText(0), name = s.getText(1), description = s.getText(2),
            equationScript = s.getText(3),
            specie = if (s.isNull(4)) null else s.getText(4),
            kind = s.getText(5), consistent = s.getLong(6) != 0L,
            bibRef = if (s.isNull(7)) null else s.getText(7),
            variables = s.getText(8),
            nutrient = if (s.isNull(9)) null else s.getText(9),
            ratio = s.getLong(10) != 0L,
            updatedAtMs = s.getLong(11)
        )}
        var count = 0
        rows.forEach { entity ->
            val local = localDb.equationDao().getEquationById(entity.uuid)
            if (local == null || local.updatedAtMs < entity.updatedAtMs) {
                localDb.equationDao().insertEquation(entity)
                count++
            }
        }
        return count
    }

    private suspend fun pushEquations(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val entities = localDb.equationDao().getEquationsUpdatedAfter(lastSync)
        var count = 0
        nasConn.beginTx()
        entities.forEach { e ->
            val nasTs = nasConn.queryTs("EQUATIONS", "uuid", e.uuid)
            if (e.updatedAtMs > nasTs) {
                nasConn.prepare("INSERT OR REPLACE INTO EQUATIONS (uuid,name,description,equationScript,specie,kind,consistent,bibRef,variables,nutrient,ratio,updatedAtMs) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)").use { s ->
                    s.bindText(1, e.uuid); s.bindText(2, e.name); s.bindText(3, e.description)
                    s.bindText(4, e.equationScript)
                    if (e.specie == null) s.bindNull(5) else s.bindText(5, e.specie)
                    s.bindText(6, e.kind); s.bindLong(7, if (e.consistent) 1L else 0L)
                    if (e.bibRef == null) s.bindNull(8) else s.bindText(8, e.bibRef)
                    s.bindText(9, e.variables)
                    if (e.nutrient == null) s.bindNull(10) else s.bindText(10, e.nutrient)
                    s.bindLong(11, if (e.ratio) 1L else 0L); s.bindLong(12, e.updatedAtMs)
                    s.step()
                }
                count++
            }
        }
        nasConn.commitTx()
        return count
    }

    // ─── BIBLIO REFS ─────────────────────────────────────────────────────────

    private suspend fun pullBiblioRefs(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val rows = nasConn.readAll(
            "SELECT uuid,firstAuthor,year,completeRef,comments,bibtex,consistent,updatedAtMs FROM BIBLIO_REFS WHERE updatedAtMs > ?",
            lastSync
        ) { s -> BiblioRefEntity(
            uuid = s.getText(0), firstAuthor = s.getText(1), year = s.getLong(2).toInt(),
            completeRef = s.getText(3), comments = s.getText(4), bibtex = s.getText(5),
            consistent = s.getLong(6).toInt(), updatedAtMs = s.getLong(7)
        )}
        var count = 0
        rows.forEach { entity ->
            val local = localDb.biblioRefDao().getBiblioRefById(entity.uuid)
            if (local == null || local.updatedAtMs < entity.updatedAtMs) {
                localDb.biblioRefDao().insertBiblioRef(entity)
                count++
            }
        }
        return count
    }

    private suspend fun pushBiblioRefs(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val entities = localDb.biblioRefDao().getBiblioRefsUpdatedAfter(lastSync)
        var count = 0
        nasConn.beginTx()
        entities.forEach { e ->
            val nasTs = nasConn.queryTs("BIBLIO_REFS", "uuid", e.uuid)
            if (e.updatedAtMs > nasTs) {
                nasConn.prepare("INSERT OR REPLACE INTO BIBLIO_REFS (uuid,firstAuthor,year,completeRef,comments,bibtex,consistent,updatedAtMs) VALUES (?,?,?,?,?,?,?,?)").use { s ->
                    s.bindText(1, e.uuid); s.bindText(2, e.firstAuthor); s.bindLong(3, e.year.toLong())
                    s.bindText(4, e.completeRef); s.bindText(5, e.comments); s.bindText(6, e.bibtex)
                    s.bindLong(7, e.consistent.toLong()); s.bindLong(8, e.updatedAtMs)
                    s.step()
                }
                count++
            }
        }
        nasConn.commitTx()
        return count
    }

    // ─── REFERENCE EV ────────────────────────────────────────────────────────

    private suspend fun pullReferenceEvs(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val rows = nasConn.readAll(
            "SELECT uuid,nom,description,maladie,nomMaladie,nomEnergie,consistent,espece,stadePhysio,nomk1,nomk2,nomk3,nomk4,nomk5,updatedAtMs FROM REFERENCE_EV WHERE updatedAtMs > ?",
            lastSync
        ) { s -> ReferenceEvEntity(
            uuid = s.getText(0), nom = s.getText(1), description = s.getText(2),
            maladie = s.getLong(3) != 0L, nomMaladie = s.getText(4), nomEnergie = s.getText(5),
            consistent = s.getLong(6).toInt(), espece = s.getText(7), stadePhysio = s.getText(8),
            nomk1 = s.getText(9), nomk2 = s.getText(10), nomk3 = s.getText(11),
            nomk4 = s.getText(12), nomk5 = s.getText(13), updatedAtMs = s.getLong(14)
        )}
        var count = 0
        rows.forEach { entity ->
            val local = localDb.referenceEvDao().getReferenceEvById(entity.uuid)
            if (local == null || local.updatedAtMs < entity.updatedAtMs) {
                localDb.referenceEvDao().insertReferenceEv(entity)
                pullReferenceEvChildren(nasConn, localDb, entity.uuid)
                count++
            }
        }
        return count
    }

    private suspend fun pullReferenceEvChildren(nasConn: SQLiteConnection, localDb: AppDatabase, refId: String) {
        localDb.referenceEvDao().deleteEquationsForReference(refId)
        nasConn.prepare("SELECT referenceEvId,equationId,equationType FROM REFERENCE_EV_EQUATIONS WHERE referenceEvId = ?").use { s ->
            s.bindText(1, refId)
            while (s.step()) {
                localDb.referenceEvDao().insertEquationRelation(
                    ReferenceEvEquationEntity(s.getText(0), s.getText(1), s.getText(2))
                )
            }
        }
        localDb.referenceEvDao().deleteCoefficientsForReference(refId)
        nasConn.prepare("SELECT uuid,referenceEvId,groupType,description,coef,groupUUID FROM REFERENCE_EV_COEFFICIENTS WHERE referenceEvId = ?").use { s ->
            s.bindText(1, refId)
            while (s.step()) {
                localDb.referenceEvDao().insertCoefficient(
                    ReferenceEvCoefficientEntity(s.getText(0), s.getText(1), s.getText(2), s.getText(3), s.getDouble(4), s.getLong(5).toInt())
                )
            }
        }
        localDb.referenceEvDao().deleteNutrientsForReference(refId)
        nasConn.prepare("SELECT uuid,referenceEvId,nutrientCode,reflevel,quantite,uniteId,uniteReqId,biblioRefId FROM REFERENCE_EV_NUTRIENTS WHERE referenceEvId = ?").use { s ->
            s.bindText(1, refId)
            while (s.step()) {
                localDb.referenceEvDao().insertNutrient(
                    ReferenceEvNutrientEntity(s.getText(0), s.getText(1), s.getText(2), s.getText(3), s.getDouble(4), s.getLong(5).toInt(), s.getLong(6).toInt(), if (s.isNull(7)) null else s.getText(7))
                )
            }
        }
    }

    private suspend fun pushReferenceEvs(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val entities = localDb.referenceEvDao().getReferenceEvsUpdatedAfter(lastSync)
        var count = 0
        nasConn.beginTx()
        entities.forEach { e ->
            val nasTs = nasConn.queryTs("REFERENCE_EV", "uuid", e.uuid)
            if (e.updatedAtMs > nasTs) {
                nasConn.prepare("INSERT OR REPLACE INTO REFERENCE_EV (uuid,nom,description,maladie,nomMaladie,nomEnergie,consistent,espece,stadePhysio,nomk1,nomk2,nomk3,nomk4,nomk5,updatedAtMs) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)").use { s ->
                    s.bindText(1, e.uuid); s.bindText(2, e.nom); s.bindText(3, e.description)
                    s.bindLong(4, if (e.maladie) 1L else 0L); s.bindText(5, e.nomMaladie); s.bindText(6, e.nomEnergie)
                    s.bindLong(7, e.consistent.toLong()); s.bindText(8, e.espece); s.bindText(9, e.stadePhysio)
                    s.bindText(10, e.nomk1); s.bindText(11, e.nomk2); s.bindText(12, e.nomk3)
                    s.bindText(13, e.nomk4); s.bindText(14, e.nomk5); s.bindLong(15, e.updatedAtMs)
                    s.step()
                }
                pushReferenceEvChildrenToNas(nasConn, localDb, e.uuid)
                count++
            }
        }
        nasConn.commitTx()
        return count
    }

    private suspend fun pushReferenceEvChildrenToNas(nasConn: SQLiteConnection, localDb: AppDatabase, refId: String) {
        nasConn.prepare("DELETE FROM REFERENCE_EV_EQUATIONS WHERE referenceEvId = ?").use { s -> s.bindText(1, refId); s.step() }
        localDb.referenceEvDao().getEquationsForReference(refId).forEach { rel ->
            nasConn.prepare("INSERT OR REPLACE INTO REFERENCE_EV_EQUATIONS (referenceEvId,equationId,equationType) VALUES (?,?,?)").use { s ->
                s.bindText(1, rel.referenceEvId); s.bindText(2, rel.equationId); s.bindText(3, rel.equationType); s.step()
            }
        }
        nasConn.prepare("DELETE FROM REFERENCE_EV_COEFFICIENTS WHERE referenceEvId = ?").use { s -> s.bindText(1, refId); s.step() }
        localDb.referenceEvDao().getCoefficientsForReference(refId).forEach { c ->
            nasConn.prepare("INSERT OR REPLACE INTO REFERENCE_EV_COEFFICIENTS (uuid,referenceEvId,groupType,description,coef,groupUUID) VALUES (?,?,?,?,?,?)").use { s ->
                s.bindText(1, c.uuid); s.bindText(2, c.referenceEvId); s.bindText(3, c.groupType)
                s.bindText(4, c.description); s.bindDouble(5, c.coef); s.bindLong(6, c.groupUUID.toLong()); s.step()
            }
        }
        nasConn.prepare("DELETE FROM REFERENCE_EV_NUTRIENTS WHERE referenceEvId = ?").use { s -> s.bindText(1, refId); s.step() }
        localDb.referenceEvDao().getNutrientsForReference(refId).forEach { n ->
            nasConn.prepare("INSERT OR REPLACE INTO REFERENCE_EV_NUTRIENTS (uuid,referenceEvId,nutrientCode,reflevel,quantite,uniteId,uniteReqId,biblioRefId) VALUES (?,?,?,?,?,?,?,?)").use { s ->
                s.bindText(1, n.uuid); s.bindText(2, n.referenceEvId); s.bindText(3, n.nutrientCode)
                s.bindText(4, n.reflevel); s.bindDouble(5, n.quantite); s.bindLong(6, n.uniteId.toLong())
                s.bindLong(7, n.uniteReqId.toLong())
                if (n.biblioRefId == null) s.bindNull(8) else s.bindText(8, n.biblioRefId)
                s.step()
            }
        }
    }

    // ─── CUSTOM NUTRIENTS ────────────────────────────────────────────────────

    private suspend fun pullCustomNutrients(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val rows = nasConn.readAll(
            "SELECT label,displayName,unite,ueCode,categoryCode,updatedAtMs FROM CUSTOM_NUTRIENTS WHERE updatedAtMs > ?",
            lastSync
        ) { s -> CustomNutrientEntity(
            label = s.getText(0), displayName = s.getText(1), unite = s.getText(2),
            ueCode = s.getText(3), categoryCode = s.getText(4), updatedAtMs = s.getLong(5)
        )}
        var count = 0
        rows.forEach { entity ->
            localDb.customNutrientDao().insertOrReplace(entity)
            count++
        }
        return count
    }

    private suspend fun pushCustomNutrients(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long): Int {
        val entities = localDb.customNutrientDao().getCustomNutrientsUpdatedAfter(lastSync)
        var count = 0
        nasConn.beginTx()
        entities.forEach { e ->
            val nasTs: Long = nasConn.prepare("SELECT updatedAtMs FROM CUSTOM_NUTRIENTS WHERE label = ?").use { s ->
                s.bindText(1, e.label); if (s.step()) s.getLong(0) else -1L
            }
            if (e.updatedAtMs > nasTs) {
                nasConn.prepare("INSERT OR REPLACE INTO CUSTOM_NUTRIENTS (label,displayName,unite,ueCode,categoryCode,updatedAtMs) VALUES (?,?,?,?,?,?)").use { s ->
                    s.bindText(1, e.label); s.bindText(2, e.displayName); s.bindText(3, e.unite)
                    s.bindText(4, e.ueCode); s.bindText(5, e.categoryCode); s.bindLong(6, e.updatedAtMs)
                    s.step()
                }
                count++
            }
        }
        nasConn.commitTx()
        return count
    }

    // ─── TOMBSTONES ──────────────────────────────────────────────────────────

    private suspend fun applyTombstonesFromNas(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long) {
        val tombstones = mutableListOf<SyncTombstoneEntity>()
        nasConn.prepare("SELECT uuid,entityType,deletedAtMs FROM SYNC_TOMBSTONES WHERE deletedAtMs > ?").use { s ->
            s.bindLong(1, lastSync)
            while (s.step()) tombstones.add(SyncTombstoneEntity(s.getText(0), s.getText(1), s.getLong(2)))
        }
        tombstones.forEach { t ->
            val localTombstone = localDb.syncTombstoneDao().find(t.uuid, t.entityType)
            when (t.entityType) {
                "ANIMAL" -> {
                    val animal = localDb.animalDao().getAnimalByUuid(t.uuid)
                    if (animal == null || animal.updatedAtMs <= t.deletedAtMs) {
                        localDb.consultationDao().deleteAnimalByUuid(t.uuid)
                        localDb.syncTombstoneDao().insert(t)
                    }
                }
                "CONSULTATION" -> {
                    val c = localDb.consultationDao().getConsultationByUuid(t.uuid)
                    if (c == null || c.updatedAtMs <= t.deletedAtMs) {
                        localDb.consultationDao().deleteConsultationByUuid(t.uuid)
                        localDb.syncTombstoneDao().insert(t)
                    }
                }
                "RATION" -> {
                    val r = localDb.consultationDao().getRationByUuid(t.uuid)
                    if (r == null || r.updatedAtMs <= t.deletedAtMs) {
                        localDb.consultationDao().deleteRationByUuid(t.uuid)
                        localDb.syncTombstoneDao().insert(t)
                    }
                }
                "WEIGHT" -> {
                    val w = localDb.consultationDao().getWeightByUuid(t.uuid)
                    if (w == null || w.updatedAtMs <= t.deletedAtMs) {
                        localDb.consultationDao().deleteWeightByUuid(t.uuid)
                        localDb.syncTombstoneDao().insert(t)
                    }
                }
                "FOOD" -> {
                    val f = localDb.foodDao().getFoodById(t.uuid)
                    if (f == null || f.updatedAtMs <= t.deletedAtMs) {
                        localDb.foodDao().deleteFoodByUuid(t.uuid)
                        localDb.syncTombstoneDao().insert(t)
                    }
                }
                "RECETTE" -> {
                    val r = localDb.recipeDao().getRecetteByUuid(t.uuid)
                    if (r == null || r.updatedAtMs <= t.deletedAtMs) {
                        localDb.recipeDao().deleteRecetteByUuid(t.uuid)
                        localDb.syncTombstoneDao().insert(t)
                    }
                }
                "EQUATION" -> {
                    val e = localDb.equationDao().getEquationById(t.uuid)
                    if (e == null || e.updatedAtMs <= t.deletedAtMs) {
                        localDb.equationDao().deleteEquationByUuid(t.uuid)
                        localDb.syncTombstoneDao().insert(t)
                    }
                }
                "BIBLIO_REF" -> {
                    val b = localDb.biblioRefDao().getBiblioRefById(t.uuid)
                    if (b == null || b.updatedAtMs <= t.deletedAtMs) {
                        localDb.biblioRefDao().deleteBiblioRefByUuid(t.uuid)
                        localDb.syncTombstoneDao().insert(t)
                    }
                }
            }
        }
    }

    private suspend fun pushTombstonesToNas(nasConn: SQLiteConnection, localDb: AppDatabase, lastSync: Long) {
        val tombstones = localDb.syncTombstoneDao().getTombstonesAfter(lastSync)
        nasConn.beginTx()
        tombstones.forEach { t ->
            nasConn.prepare("INSERT OR REPLACE INTO SYNC_TOMBSTONES (uuid,entityType,deletedAtMs) VALUES (?,?,?)").use { s ->
                s.bindText(1, t.uuid); s.bindText(2, t.entityType); s.bindLong(3, t.deletedAtMs); s.step()
            }
        }
        nasConn.commitTx()
    }
}
