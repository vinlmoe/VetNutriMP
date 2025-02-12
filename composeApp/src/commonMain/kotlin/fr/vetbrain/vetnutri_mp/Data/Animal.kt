package fr.vetbrain.vetnutri_mp.Data

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Animal(
        var id: Long = 0,
        var nom: String = "",
        var espece: Espece = Espece.CHIEN,
        var sexe: Sexe = Sexe.MALE,
        var dateNaissance: LocalDate? = null,
        var mort: Boolean = false,
        var sterilise: Boolean = false,
        var race: String = "",
        var nomProprio: String = "",
        var resume: String = "",
        @Transient var poids: MutableList<WeightDate> = mutableListOf(),
        val uuid: String = Uuid.random().toString()
) {
    @Serializable
    enum class Espece {
        CHIEN,
        CHAT,
        CHEVAL,
        FURET,
        CANIN,
        FELIN,
        FOLIVORE;

        fun nameToString() = name.lowercase()
    }

    @Serializable
    enum class Sexe {
        MALE,
        FEMELLE;

        fun nameToString() = name.lowercase()
    }

    fun addWeight(date: LocalDate, value: Float) {
        poids.add(WeightDate(date = date, value = value, refAnimal = uuid))
    }

    fun updateWeight(weightDate: WeightDate, newValue: Float) {
        val index = poids.indexOfFirst { it.uuid == weightDate.uuid }
        if (index != -1) {
            poids[index] = weightDate.copy(value = newValue)
        }
    }

    fun removeWeight(uuid: String) {
        poids.removeAll { it.uuid == uuid }
    }
}
