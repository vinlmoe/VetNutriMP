package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

enum class AlimIndic(val coef: Int, override val label: String?) : Labelable {
    ALL(999, "all"),
    PED(0, "pediatric"),
    NEUT(1, "neutered"),
    PHYS(2, "physiological"),
    SEN(3, "senior"),
    CALM(4, "felineStress"),
    OBES(5, "obesity"),
    GESTATION(6, "gestation"),
    SONDE(7, "tubeFeeding"),
    LACT(8, "lactation"),
    CROISSANCE(9, "growth"),
    DENT(11, "dentalHygiene"),
    DIAB(12, "diabetes"),
    INSHEP(25, "hepaticInsufficiency"),
    HYPO(26, "hypoallergenic"),
    ART(27, "jointSupport"),
    MRC(28, "renalSupport"),
    CONV(30, "convalescence"),
    MBAUF(32, "mbauf"),
    URO(33, "urolithiasis"),
    DERM(34, "dermatologicSupport"),
    GI(35, "gastrointestinalDisease"),
    CAR(36, "cardiacDisease"),
    END(37, "endocrineDisorder"),
    IPE(38, "pancreaticInsufficiency"),
    DISTRU(39, "struviteDissolution"),
    REDSTRU(40, "struviteReduction"),
    REDURA(41, "urateReduction"),
    REDOXA(42, "oxalateReduction"),
    REDCYST(43, "cystineReduction"),
    ACT(45, "sport"),
    AUTRE(44, "others");

    companion object {
        fun byCoef(coef: Int): AlimIndic = entries.find { it.coef == coef } ?: PHYS

        fun isPresent(indic: AlimIndic): Boolean = entries.contains(indic)

        fun byName(name: String): AlimIndic =
                entries.find { it.label?.equals(name, ignoreCase = true) == true } ?: AUTRE

        fun valuesExcept(): List<AlimIndic> = entries.filter { it != ALL }
    }

    override fun toString() = label ?: "Unknown"
}
