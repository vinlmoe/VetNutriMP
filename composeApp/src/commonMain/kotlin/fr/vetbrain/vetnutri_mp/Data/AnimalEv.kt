package fr.vetbrain.vetnutri_mp.Data

import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "Animals")
@Serializable
data class AnimalEv(
        @PrimaryKey val uuid: String = Uuid.random().toString(),
        var name: String?,
        var dead: Boolean?,
        var id: String?,
        var sexId: Int?, // Stocke l'ID de l'enum Sex
        var specieId: String, // Stocke l'ID de l'enum Espece
        var ownerName: String?,
        var birthdate: LocalDate?,
        var race: String?,
        var summary: String?,
        var consultations: MutableList<ConsultationEv> = mutableListOf(),
        var weight: MutableList<WeightDate> = mutableListOf()
) {
    fun getSex(): Sex {
        return Sex.byId(sexId ?: 0)
    }

    fun setSex(sex: Sex) {
        this.sexId = sex.id
    }

    fun getEspece(): Espece {
        return Espece.getFromId(specieId)
    }

    fun setEspece(espece: Espece) {
        this.specieId = espece.uuid
    }
}
