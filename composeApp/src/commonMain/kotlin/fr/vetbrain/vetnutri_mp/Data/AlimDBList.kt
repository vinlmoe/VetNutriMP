package fr.vetbrain.vetnutri_mp.Data

import androidx.room.*


data class AlimDBList(
    val db: MutableMap<String, alimDB> = mutableMapOf() // Using MutableMap for mutability
) {
    fun add(dbEntry: alimDB) {
        db[dbEntry.uuid] = dbEntry
    }

    fun setNumber(uuid: String, number: Int) {
        db[uuid]?.number = number // Assuming alimDB has a 'number' property
    }

    operator fun get(key: String): alimDB? = db[key]
    fun values(): Collection<alimDB> = db.values
}

@Entity(tableName = "alim_db")
data class alimDB(
    @PrimaryKey val uuid: String,
    val sNom: String?,
    val compNom: String?,
    var number: Int = 0
)