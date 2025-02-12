package fr.vetbrain.vetnutri_mp.Data

import androidx.room.Entity
import androidx.room.Ignore
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
        var name: String? = null,
        var dead: Boolean? = null,
        var id: String? = null,
        var sexId: Int? = null,
        var specieId: String? = null,
        var ownerName: String? = null,
        var birthdate: LocalDate? = null,
        var race: String? = null,
        var summary: String? = null,
        @Ignore var consultations: MutableList<ConsultationEv> = mutableListOf(),
        @Ignore var weight: MutableList<WeightDate> = mutableListOf()
) {
    constructor() : this(uuid = Uuid.random().toString())

    fun getSex(): Sex {
        return Sex.values().firstOrNull { it.id == sexId } ?: Sex.MALE
    }

    fun setSex(sex: Sex) {
        this.sexId = sex.id
    }

    fun getEspece(): Espece {
        return Espece.values().firstOrNull { it.name == specieId } ?: Espece.CHIEN
    }

    fun setEspece(espece: Espece) {
        this.specieId = espece.name
    }
}
