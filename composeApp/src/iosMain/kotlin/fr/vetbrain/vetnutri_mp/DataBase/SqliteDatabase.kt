package fr.vetbrain.vetnutri_mp.DataBase

import kotlinx.cinterop.*
import platform.SQLite.*

actual class SqliteDatabase private constructor(private val db: CPointer<sqlite3>) {
    actual fun executeQuery(query: String) {
        memScoped {
            val stmt = alloc<CPointerVar<sqlite3_stmt>>()
            if (sqlite3_prepare_v2(db, query, -1, stmt.ptr, null) == SQLITE_OK) {
                sqlite3_step(stmt.value)
                sqlite3_finalize(stmt.value)
            } else {
                throw SQLiteException("Erreur lors de l'exécution de la requête: ${getLastError()}")
            }
        }
    }

    actual fun executeQueryWithResult(query: String): List<Map<String, Any?>> {
        val results = mutableListOf<Map<String, Any?>>()
        memScoped {
            val stmt = alloc<CPointerVar<sqlite3_stmt>>()
            if (sqlite3_prepare_v2(db, query, -1, stmt.ptr, null) == SQLITE_OK) {
                while (sqlite3_step(stmt.value) == SQLITE_ROW) {
                    val row = mutableMapOf<String, Any?>()
                    val columnCount = sqlite3_column_count(stmt.value)
                    for (i in 0 until columnCount) {
                        val columnName = sqlite3_column_name(stmt.value, i)?.toKString() ?: continue
                        val columnType = sqlite3_column_type(stmt.value, i)
                        val value =
                                when (columnType) {
                                    SQLITE_INTEGER -> sqlite3_column_int64(stmt.value, i)
                                    SQLITE_FLOAT -> sqlite3_column_double(stmt.value, i)
                                    SQLITE_TEXT -> sqlite3_column_text(stmt.value, i)?.toKString()
                                    SQLITE_NULL -> null
                                    else -> null
                                }
                        row[columnName] = value
                    }
                    results.add(row)
                }
                sqlite3_finalize(stmt.value)
            } else {
                throw SQLiteException("Erreur lors de l'exécution de la requête: ${getLastError()}")
            }
        }
        return results
    }

    actual fun beginTransaction() {
        executeQuery("BEGIN TRANSACTION")
    }

    actual fun commitTransaction() {
        executeQuery("COMMIT")
    }

    actual fun rollbackTransaction() {
        executeQuery("ROLLBACK")
    }

    actual fun close() {
        sqlite3_close(db)
    }

    private fun getLastError(): String {
        return sqlite3_errmsg(db)?.toKString() ?: "Erreur inconnue"
    }

    actual companion object {
        actual fun open(path: String): SqliteDatabase {
            val db = memScoped {
                val dbPtr = alloc<CPointerVar<sqlite3>>()
                val result =
                        sqlite3_open_v2(
                                path,
                                dbPtr.ptr,
                                SQLITE_OPEN_READWRITE or SQLITE_OPEN_CREATE,
                                null
                        )
                if (result != SQLITE_OK) {
                    throw SQLiteException("Impossible d'ouvrir la base de données: $result")
                }
                dbPtr.value
            }
            return SqliteDatabase(db!!)
        }
    }
}

class SQLiteException(message: String) : Exception(message)
