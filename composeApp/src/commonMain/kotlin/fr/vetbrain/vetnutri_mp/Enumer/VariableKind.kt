package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

enum class VariableKind(
        val uuid: Int,
        val dup: String,
        override val label: String,
        val variable: String
) : Labelable {
    AdultWeight(0, "AdultWeight", "AW", "adultWeight"),
    LitterSize(1, "LitterSize", "L", "litterSize"),
    WeekGestation(2, "WeekGestation", "wG", "gestationWeek"),
    WeekLactation(3, "WeekLactation", "wL", "lactationWeek"),
    BEE(4, "SandarfEnergyNeed", "BEE", "sandarfEnergyNeed"),
    BE(5, "EnergyNeed", "BE", "energyNeed"),
    BW(6, "BodyWeight", "BW", "bodyWeight"),
    iBW(7, "IdealBodyWeight", "iBW", "idealBodyWeight"),
    MW(8, "MetabolicWeight", "MW", "metabolicWeight"),
 CW(9, "CarriedWeight", "CW", "CarriedWeiht"),
 D(10, "Distance", "D", "Distance");

    companion object {
        fun getById(id: Int): VariableKind = entries.find { it.uuid == id } ?: AdultWeight
    }

    override fun toString() = label ?: ""
}
