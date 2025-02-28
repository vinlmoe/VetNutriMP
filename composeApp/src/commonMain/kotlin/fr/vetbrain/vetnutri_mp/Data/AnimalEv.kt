package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
@OptIn(ExperimentalUuidApi::class)
data class AnimalEv(
        var uuid: String = Uuid.random().toString(),
        var nom: String = "",
        var dead: Boolean = false,
        var id: String? = null,
        var sexId: Int = Sex.MALEE.id,
        var specieId: String = Espece.CHIEN.name,
        var ownerName: String = "",
        var birthdate: LocalDate? = null,
        var race: String = "",
        var summary: String = "",
        @Contextual var consultations: MutableList<ConsultationEv> = mutableListOf(),
        @Contextual var weightHistory: MutableList<WeightDate> = mutableListOf(),
        @Contextual var rations: MutableList<Ration> = mutableListOf()
) {
    fun getSex(): Sex {
        return Sex.values().find { it.id == sexId } ?: Sex.MALEE
    }

    fun setSex(sex: Sex) {
        this.sexId = sex.id
    }

    fun getEspece(): Espece {
        return Espece.valueOf(specieId)
    }

    fun setEspece(espece: Espece) {
        this.specieId = espece.name
    }

    companion object {
        fun createTestAnimal(): AnimalEv {
            return AnimalEv(
                    nom = "Rex",
                    dead = false,
                    id = "TEST001",
                    sexId = Sex.MALEE.id,
                    specieId = Espece.CHIEN.name,
                    ownerName = "Jean Dupont",
                    birthdate = LocalDate(2020, 1, 1),
                    race = "Labrador",
                    summary = "Animal de test"
            )
        }
    }
}
