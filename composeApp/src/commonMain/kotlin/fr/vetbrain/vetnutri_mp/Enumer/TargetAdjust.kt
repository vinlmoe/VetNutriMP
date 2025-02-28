package fr.vetbrain.vetnutri_mp.Enumer

import fr.vetbrain.vetnutri_mp.Data.Labelable
import fr.vetbrain.vetnutri_mp.Data.Nutrient
import kotlinx.serialization.Serializable

@Serializable
enum class TargetAdjust(
        private val nameFr: String,
        private val nameEn: String,
        val coef: Int,
        val mne: Int,
        val kind: Int
) : Labelable {
        PROT("Protéines", "Proteins", 0, MainNutrientEnum.BASE.coef, 0),
        O3("Oméga 3", "Omega 3", 1, MainNutrientEnum.LIPID.coef, 0),
        O6("Oméga 6", "Omega 6", 2, MainNutrientEnum.LIPID.coef, 0),
        CALCIUM("Calcium", "Calcium", 3, MainNutrientEnum.MACRO.coef, 0),
        CALCIUMPHOS(
                "Calcium et Phosphore",
                "Calcium and Phosphorus",
                4,
                MainNutrientEnum.ANA.coef,
                0
        ),
        FIBER("Fibres", "Fiber", 5, MainNutrientEnum.BASE.coef, 0),
        VITA("Vitamine A", "Vitamin A", 6, MainNutrientEnum.VITAM.coef, 0),
        VITD("Vitamine D", "Vitamin D", 7, MainNutrientEnum.VITAM.coef, 0),
        VITE("Vitamine E", "Vitamin E", 8, MainNutrientEnum.VITAM.coef, 0),
        NA("Sodium", "Sodium", 9, MainNutrientEnum.MACRO.coef, 0),
        MG("Magnésium", "Magnesium", 10, MainNutrientEnum.MACRO.coef, 0),
        EPA("EPA + DHA", "EPA + DHA", 11, MainNutrientEnum.LIPID.coef, 0),
        ENERGIE("Energie 2", "Energy 2", 12, MainNutrientEnum.ENERGIE.coef, 0),
        COMP("Energie 1", "Energy 1", 13, MainNutrientEnum.ENERGIE.coef, 0),
        LIP("Matières grasses", "Crude fat", 14, MainNutrientEnum.BASE.coef, 0),
        NO("Non", "No", 15, MainNutrientEnum.NO.coef, 0);

        override val label: String = nameFr

        fun nameToString(lang: Lang): String {
                return when (lang) {
                        Lang.FR -> nameFr
                        Lang.EN -> nameEn
                }
        }

        companion object {
                private val coefMap = values().associateBy { it.coef }

                fun getByName(label: String, lang: Lang): TargetAdjust {
                        return values().find { it.nameToString(lang) == label } ?: PROT
                }

                fun getByCoef(coef: Int): TargetAdjust {
                        return coefMap[coef] ?: PROT
                }

                fun calculateTargetFromNutrients(
                        msContent: Float,
                        nutrientValues: Map<Nutrient, Float>
                ): TargetAdjust {
                        if (msContent <= 0) return NO

                        return when {
                                msContent > 0 &&
                                        (100f * (nutrientValues[NutrientMain.CENDRE] ?: 0f) /
                                                msContent > 10) &&
                                        nutrientValues.containsKey(NutrientMain.CENDRE) ->
                                        CALCIUMPHOS
                                msContent > 0 &&
                                        (100f * (nutrientValues[NutrientMain.PROTEINE] ?: 0f) /
                                                msContent > 30) -> PROT
                                msContent > 0 &&
                                        (100f * (nutrientValues[NutrientMain.LIPIDE] ?: 0f) /
                                                msContent > 30) &&
                                        (100f * (nutrientValues[NutrientLipid.O6] ?: 0f) /
                                                msContent > 15) -> O6
                                msContent > 0 &&
                                        (100f * (nutrientValues[NutrientMain.LIPIDE] ?: 0f) /
                                                msContent > 30) &&
                                        (nutrientValues[NutrientLipid.EPADHA] ?: 0f) > 0 -> EPA
                                msContent > 0 &&
                                        (100f * (nutrientValues[NutrientMain.LIPIDE] ?: 0f) /
                                                msContent > 40) -> LIP
                                msContent > 0 &&
                                        (100f * (nutrientValues[NutrientMain.CELLULOSE] ?: 0f) /
                                                msContent > 20) -> FIBER
                                msContent > 0 &&
                                        (100f * (nutrientValues[NutrientMain.ENA] ?: 0f) /
                                                msContent > 50) -> ENERGIE
                                else -> NO
                        }
                }

                const val SIZE = 16
        }
}
