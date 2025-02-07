package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumerise.Espece
import fr.vetbrain.vetnutri_mp.Enumerise.TextConstant
import kotlinx.datetime.LocalDate


import kotlinx.serialization.*
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@ExperimentalUuidApi
@Serializable
data class Animal(
    var UUID: String =Uuid.random().toString(),
    var nom: String = "",
    var dead: Boolean = false,
    var id: String? = null,
    var sex: Int = 0,
    var espece: Espece,
    var nomProprio: String = "",
    var dateNaiss: LocalDate = LocalDate(1990,7,9),
    var race: String = "",
    var resume: String = "",
    var listWeight: MutableList<WeightDate> = mutableListOf(),
    var version: String = TextConstant.VERSION.nameToString(),
    var list: MutableList<ConsultationEv> = mutableListOf()
) {
    fun addWeight(w: WeightDate) {
        listWeight.add(w)
    }

    fun updateWeight(uuid: String, d: LocalDate, v: Float) {
        listWeight.find { it.uuid == uuid }?.apply {
            date = d
            value = v
        }
    }

    fun removeWeight(UUIDwd: String) {
        listWeight = listWeight.filterNot { it.uuid == UUIDwd }.toMutableList()
    }
    fun setDateNaiss(dateNaisso: LocalDate) {

        dateNaiss = dateNaisso
    }
    }




