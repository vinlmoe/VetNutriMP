package fr.vetbrain.vetnutri_mp.enumerise

import fr.vetbrain.vetnutri_mp.Data.Nutrient
import fr.vetbrain.vetnutri_mp.Enumerise.MainNutrientEnum
import fr.vetbrain.vetnutri_mp.Enumerise.UnitEnum

enum class AAEnum(
    val nom: String,
    override val coef: Int,
    override val ue: UnitEnum,
    override val label: String
) : Nutrient {
    ALANINE("Alanine", 0, UnitEnum.BUg, "ALANINE"),
    ARGININE("Arginine", 1, UnitEnum.BUg, "ARGININE"),
    ASPARAGINE("Asparagine", 2, UnitEnum.BUg, "ASPARAGINE"),
    ASPARATE("Asparate", 3, UnitEnum.BUg, "ASPARATE"),
    CYSTEINE("Cystéine", 4, UnitEnum.BUg, "CYSTEINE"),
    GLUTAMATE("Glutamate", 5, UnitEnum.BUg, "GLUTAMATE"),
    GLUTAMINE("Glutamine", 6, UnitEnum.BUg, "GLUTAMINE"),
    GLYCINE("Glycine", 7, UnitEnum.BUg, "GLYCINE"),
    HISTIDINE("Histidine", 8, UnitEnum.BUg, "HISTIDINE"),
    ISOLEUCINE("Isoleucine", 9, UnitEnum.BUg, "ISOLEUCINE"),
    LEUCINE("Leucine", 10, UnitEnum.BUg, "LEUCINE"),
    LYSINE("Lysine", 11, UnitEnum.BUg, "LYSINE"),
    METHIONINE("Methionine", 12, UnitEnum.BUg, "METHIONINE"),
    PHENYLALANINE("Phénylalanine", 13, UnitEnum.BUg, "PHENYLALANINE"),
    PROLINE("Proline", 14, UnitEnum.BUg, "PROLINE"),
    PYRROLYSINE("Pyrrolysine", 15, UnitEnum.BUg, "PYRROLYSINE"),
    SELENOCYSTEINE("Sélénocystéine", 16, UnitEnum.BUg, "SELENOCYSTEINE"),
    SERINE("Sérine", 17, UnitEnum.BUg, "SERINE"),
    THREONINE("Thréonine", 18, UnitEnum.BUg, "THREONINE"),
    TRYPTOPHANE("Tryptophane", 19, UnitEnum.BUg, "TRYPTOPHANE"),
    TYROSINE("Tyrosine", 20, UnitEnum.BUg, "TYROSINE"),
    VALINE("Valine", 21, UnitEnum.BUg, "VALINE");

    override val unite ="g"

    companion object {
        private val map = values().associateBy { it.coef }

        fun getByLabel(label: String): AAEnum {
            return values().find { it.label == label } ?: ASPARAGINE
        }

        fun getByCoef(i: Int): AAEnum {
            return map[i] ?: ASPARAGINE
        }
    }


   fun getUnite(): String = ue.label
    override fun getMNE(): MainNutrientEnum = MainNutrientEnum.AMA
} 