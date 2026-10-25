package fr.vetbrain.vetnutri_mp.Repository

import androidx.room.RoomDatabase
import androidx.room.immediateTransaction
import androidx.room.useWriterConnection

/**
 * Exécute un bloc de travail dans une transaction de base de données.
 *
 * Les imports (aliments, sauvegardes complètes) enchaînent des milliers d'écritures : les
 * regrouper dans une seule transaction les rend atomiques (rollback en cas d'erreur) et
 * évite un commit par lot. Les tests sur repositories en mémoire utilisent [TransactionRunner.None].
 */
interface TransactionRunner {
    suspend fun <T> inTransaction(block: suspend () -> T): T

    companion object {
        /** Aucune transaction : le bloc est exécuté tel quel. */
        val None: TransactionRunner = object : TransactionRunner {
            override suspend fun <T> inTransaction(block: suspend () -> T): T = block()
        }
    }
}

/**
 * Transaction Room (connexion d'écriture, mode IMMEDIATE). Les appels DAO effectués dans le
 * bloc, y compris dans des `withContext` enfants, utilisent la même connexion ; les transactions
 * imbriquées sont gérées par Room via des points de sauvegarde.
 */
class RoomTransactionRunner(private val database: RoomDatabase) : TransactionRunner {
    override suspend fun <T> inTransaction(block: suspend () -> T): T =
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction { block() }
        }
}
