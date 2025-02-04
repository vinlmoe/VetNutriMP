package fr.vetbrain.vetnutri_mp.Data

import androidx.room.*
import kotlinx.serialization.Serializable

@Serializable
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


@Entity(tableName = "DataDef")
@Serializable
data class AlimDB(
    @PrimaryKey val uuid: String,
    @ColumnInfo(name = "sNAME") val sNom: String?,
    @ColumnInfo(name = "compNAME") val compNom: String?,
     var number: Int = 0 // Ignored by Room, for transient use
)
