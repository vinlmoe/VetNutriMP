package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

enum class VariableKind(
    val uuid: Int,
    val dup: String,
    val variable: String,
    override val label: String?
) : Labelable {
    AdultWeight(0, "AdultWeight", "AW", "adultWeight"),
    LitterSize(1, "LitterSize", "L", "litterSize"),
    WeekGestation(2, "WeekGestation", "wG", "gestationWeek"),
    WeekLactation(3, "WeekLactation", "wL", "lactationWeek");

    companion object {
        fun getById(id: Int): VariableKind = entries.find { it.uuid == id } ?: AdultWeight
    }

    override fun toString() = label ?: ""
} 