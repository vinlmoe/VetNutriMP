package fr.vetbrain.vetnutri_mp.DataBase

import java.io.File
import kotlin.test.*
import platform.Foundation.NSTemporaryDirectory

class SqliteDatabaseTest {
    private lateinit var db: SqliteDatabase
    private val dbPath = NSTemporaryDirectory() + "test.db"

    @BeforeTest
    fun setup() {
        db = SqliteDatabase.open(dbPath)
    }

    @AfterTest
    fun tearDown() {
        db.close()
        File(dbPath).delete()
    }

    @Test
    fun testCreateTable() {
        db.executeQuery(
                """
            CREATE TABLE IF NOT EXISTS test_table (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            )
        """.trimIndent()
        )

        // Vérifier que la table a été créée
        val result =
                db.executeQueryWithResult(
                        "SELECT name FROM sqlite_master WHERE type='table' AND name='test_table'"
                )
        assertEquals(1, result.size)
        assertEquals("test_table", result[0]["name"])
    }

    @Test
    fun testInsertAndSelect() {
        // Créer la table
        db.executeQuery(
                """
            CREATE TABLE IF NOT EXISTS test_table (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            )
        """.trimIndent()
        )

        // Insérer des données
        db.executeQuery("INSERT INTO test_table (name) VALUES ('test1')")
        db.executeQuery("INSERT INTO test_table (name) VALUES ('test2')")

        // Sélectionner et vérifier les données
        val results = db.executeQueryWithResult("SELECT * FROM test_table ORDER BY id")
        assertEquals(2, results.size)
        assertEquals("test1", results[0]["name"])
        assertEquals("test2", results[1]["name"])
    }

    @Test
    fun testTransaction() {
        // Créer la table
        db.executeQuery(
                """
            CREATE TABLE IF NOT EXISTS test_table (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL
            )
        """.trimIndent()
        )

        // Test de transaction réussie
        db.beginTransaction()
        try {
            db.executeQuery("INSERT INTO test_table (name) VALUES ('test1')")
            db.executeQuery("INSERT INTO test_table (name) VALUES ('test2')")
            db.commitTransaction()
        } catch (e: Exception) {
            db.rollbackTransaction()
            fail("La transaction n'aurait pas dû échouer")
        }

        val results = db.executeQueryWithResult("SELECT * FROM test_table ORDER BY id")
        assertEquals(2, results.size)

        // Test de rollback
        db.beginTransaction()
        try {
            db.executeQuery("INSERT INTO test_table (name) VALUES ('test3')")
            throw Exception("Test rollback")
        } catch (e: Exception) {
            db.rollbackTransaction()
        }

        val resultsAfterRollback = db.executeQueryWithResult("SELECT * FROM test_table ORDER BY id")
        assertEquals(
                2,
                results.size,
                "Le nombre d'enregistrements ne devrait pas avoir changé après le rollback"
        )
    }

    @Test
    fun testInvalidQuery() {
        assertFailsWith<SQLiteException> { db.executeQuery("INVALID SQL QUERY") }
    }
}
