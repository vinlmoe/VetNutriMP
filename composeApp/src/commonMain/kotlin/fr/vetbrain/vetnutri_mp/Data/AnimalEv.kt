package fr.vetbrain.vetnutri_mp.Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import fr.vetbrain.vetnutri_mp.Enumerise.Espece
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "Animals")
@Serializable
data class AnimalEv(
    @PrimaryKey val uuid: String =  Uuid.random().toString(),
    var name: String?,
    var dead: Boolean?,
    var id: String?,
    var sex: Int?, // Using Int to store enum id
    var specie: String, // Using String to store enum uuid
    var ownerName: String?,
    var birthdate: LocalDate?,
    var race: String?,
    var summary: String?
){
    fun getEspece():Espece{ return Espece.getEnumFromString(specie)}

    fun setEspece(espece: Espece){ this.specie=espece.nameToString()}
}

