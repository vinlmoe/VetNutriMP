package fr.vetbrain.vetnutri_mp.Data


import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable


@Serializable
class ListConsultEv {
    private val consultList: MutableMap<String, ConsultationEv> = mutableMapOf()
    val listConsult: MutableList<ConsultationEv> = mutableListOf()

    companion object {
        private const val serialVersionUID = 1L
    }

    fun convert() {
        if (listConsult.isNotEmpty()) {
            listConsult.forEach { consult ->
                consultList[consult.uuid] = consult
            }
        }
        listConsult.clear()
    }


    fun getConsultByUUID(UUID: String): ConsultationEv? = consultList[UUID]

    fun replaceConsult(ani: ConsultationEv) {
        consultList[ani.uuid] = ani
    }

    fun size(): Int = consultList.size

    fun addConsult(a: ConsultationEv) {
        consultList[a.uuid] = a
    }

    fun removeConsult(UUID: String) {
        consultList.remove(UUID)
    }

    fun getRation(UUIDcons: String, UUIDrat: String): Ration? {
        return getConsult(UUIDcons)?.getRationByID(UUIDrat)
    }

    fun getAliment(UUIDcons: String, UUIDrat: String, UUIDalim: String): AlimentRation? {
        return getConsult(UUIDcons)?.getRationByID(UUIDrat)?.getAlimentByUUID(UUIDalim)
    }

    fun getConsult(UUID: String): ConsultationEv? = consultList[UUID]

    fun getLastConsult(): ConsultationEv? {
        if (!yetConsult()) return null

        return listConsult.maxByOrNull { it.date?: LocalDate(1990,7,9) }
    }

   /* fun getPrevConsult(d: LocalDate): ConsultationEv? {
        if (!yetConsult()) return null

        return getListConsult()
            .filter { it.date.compareTo(d) }
            .maxByOrNull { it.date?: LocalDate(1990,7,9)  }
    }

    fun getPostConsult(d: LocalDate): ConsultationEv? {
        if (!yetConsult()) return null

        return getListConsult()
            .filter { it.date?.isAfter(d) ?: false  }
            .minByOrNull { it.date?: LocalDate(1990,7,9)  }
    }
*/
    fun yetConsult(): Boolean = consultList.isNotEmpty()
}
fun LocalDate.isAfter(d: LocalDate): Boolean{
    return true
}