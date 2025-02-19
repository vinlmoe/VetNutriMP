package fr.vetbrain.vetnutri_mp.DataBase

expect class SqliteDatabase private constructor() {
    fun executeQuery(query: String)
    fun executeQueryWithResult(query: String): List<Map<String, Any?>>
    fun beginTransaction()
    fun commitTransaction()
    fun rollbackTransaction()
    fun close()

    companion object {
        fun open(path: String): SqliteDatabase
    }
}
