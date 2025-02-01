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
                consultList[consult.UUID] = consult
            }
        }
        listConsult.clear()
    }

    fun getListConsult(): List<ConsultationEv> = consultList.values.toList()

    fun getConsultByUUID(UUID: String): ConsultationEv? = consultList[UUID]

    fun replaceConsult(ani: ConsultationEv) {
        consultList[ani.UUID] = ani
    }

    fun size(): Int = consultList.size

    fun addConsult(a: ConsultationEv) {
        consultList[a.UUID] = a
    }

    fun removeConsult(UUID: String) {
        consultList.remove(UUID)
    }

    fun getRation(UUIDcons: String, UUIDrat: String): Ration? {
        return getConsult(UUIDcons)?.getRationByUUID(UUIDrat)
    }

    fun getAliment(UUIDcons: String, UUIDrat: String, UUIDalim: String): AlimentRation? {
        return getConsult(UUIDcons)?.getRationByUUID(UUIDrat)?.getAlimentByUUID(UUIDalim)
    }

    fun getConsult(UUID: String): ConsultationEv? = consultList[UUID]

    fun getLastConsult(): ConsultationEv? {
        if (!yetConsult()) return null

        return getListConsult().maxByOrNull { it.date }
    }

    fun getPrevConsult(d: LocalDate): ConsultationEv? {
        if (!yetConsult()) return null

        return getListConsult()
            .filter { it.date.isBefore(d) }
            .maxByOrNull { it.date }
    }

    fun getPostConsult(d: LocalDate): ConsultationEv? {
        if (!yetConsult()) return null

        return getListConsult()
            .filter { it.date.isAfter(d) }
            .minByOrNull { it.date }
    }

    fun yetConsult(): Boolean = consultList.isNotEmpty()
}