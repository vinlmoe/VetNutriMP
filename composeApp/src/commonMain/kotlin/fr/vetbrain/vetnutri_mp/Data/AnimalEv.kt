package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Sex
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalUuidApi::class)
data class AnimalEv(
        var uuid: String = Uuid.random().toString(),
        var nom: String = "",
        var dead: Boolean = false,
        var id: String? = null,
        var sexId: Int = Sex.MALE_ENTIER.id,
        var specieId: String = Espece.CHIEN.name,
        var ownerName: String = "",
        var birthdate: LocalDate? = null,
        var race: String = "",
        var summary: String = "",
        var consultations: MutableList<ConsultationEv> = mutableListOf(),
        var weightHistory: MutableList<WeightDate> = mutableListOf()
) {
    fun getSex(): Sex {
        return Sex.values().firstOrNull { it.id == sexId } ?: Sex.MALE_ENTIER
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

    companion object {
        fun createTestAnimal(): AnimalEv {
            return AnimalEv(
                    nom = "Rex",
                    dead = false,
                    id = "TEST001",
                    sexId = Sex.MALE_ENTIER.id,
                    specieId = Espece.CHIEN.name,
                    ownerName = "Jean Dupont",
                    birthdate = LocalDate(2020, 1, 1),
                    race = "Labrador",
                    summary = "Animal de test"
            )
        }
    }
}
