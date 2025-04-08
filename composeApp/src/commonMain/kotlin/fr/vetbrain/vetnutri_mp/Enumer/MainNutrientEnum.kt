package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable

enum class MainNutrientEnum(override val label: String, val coef: Int) : Labelable {
    MIN("Mineraux", 0),
    ANA("Analysis", 1),
    MACRO("Macro", 2),
    VITAM("Vit", 3),
    BASE("Base", 4),
    LIPID("lipide", 5),
    OTHER("autres", 6),
    ENERGIE("Energy", 7),
    NO("autres", 8),
    AMA("amino Acides", 9),
    INGREDIENT("Ingredients", 10),
    INDICAT("Indication", 11);

    val unite: String = ""

    companion object {
        private val coefMap by lazy { values().associateBy { it.coef } }

        fun getByCoef(coef: Int) = coefMap[coef]
        fun size() = 7 // À vérifier (incohérence avec le nombre réel de valeurs)
    }

    fun getNutrient(i: Int): Nutrient? =
            when (this) {
                AMA -> AAEnum.getByCoef(i)
                ANA -> NutrientAnalysis.getByCoef(i)
                BASE -> NutrientMain.getByCoef(i)
                ENERGIE -> NutrientEnergy.getByCoef(i)
                LIPID -> NutrientLipid.getByCoef(i)
                MACRO -> NutrientMacro.getByCoef(i)
                MIN -> NutrientMin.getByCoef(i)
                OTHER -> NutrientOther.getByCoef(i)
                VITAM -> NutrientVitam.getByCoef(i)
                else ->
                        null.also {
                            if (this !in setOf(INGREDIENT, INDICAT, NO)) {
                                error("Unhandled type: $this")
                            }
                        }
            }

    /**
     * Obtient tous les nutriments associés à ce type principal
     *
     * @return Liste des nutriments associés à ce type
     */
    fun getSousNutrients(): List<Nutrient> {
        return when (this) {
            AMA -> AAEnum.entries.toList()
            ANA -> NutrientAnalysis.entries.toList()
            BASE -> NutrientMain.entries.toList()
            ENERGIE -> NutrientEnergy.entries.toList()
            LIPID -> NutrientLipid.entries.toList()
            MACRO -> NutrientMacro.entries.toList()
            MIN -> NutrientMin.entries.toList()
            OTHER -> NutrientOther.entries.toList()
            VITAM -> NutrientVitam.entries.toList()
            else -> emptyList()
        }
    }
}
