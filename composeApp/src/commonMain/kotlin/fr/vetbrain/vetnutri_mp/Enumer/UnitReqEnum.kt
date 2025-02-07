package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

enum class UnitReqEnum(
    val id: Int,
    override val label: String?,
    val conv: Float = 1f
) : Labelable {
    MCAL(0, "mcal"),
    KGBW(1, "kgBw"),
    KGMW(2, "kgMw"),
    NO(3, ""),
    PERC(4, "percentage");

    companion object {
        fun getById(id: Int): UnitReqEnum = entries.find { it.id == id } ?: NO

        private val idMap = entries.associateBy { it.id }
    }

    override fun toString() = label ?: ""
} 