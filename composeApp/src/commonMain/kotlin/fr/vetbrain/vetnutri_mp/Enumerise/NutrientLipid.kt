package fr.vetbrain.vetnutri_mp.Enumerise

import fr.vetbrain.vetnutri_mp.Data.Nutrient

enum class NutrientLipid(
    private val displayName: String,
    override val coef: Int,
    override val ue: UnitEnum,
    override val unite: String,
    override val label: String
) : Nutrient {
    AGSATURE("Acides gras saturés", 0, UnitEnum.BUg, "g", "AGSATURE"),
    AGMONO("Acides gras mono-insaturés", 1, UnitEnum.BUg, "g", "AGMONO"),
    AGPOLY("Acides gras poly-insaturés", 2, UnitEnum.BUg, "g", "AGPOLY"),
    AG40("C4:0", 3, UnitEnum.BUg, "g", "AG40"),
    AG60("C6:0", 4, UnitEnum.BUg, "g", "AG60"),
    AG80("C8:0", 5, UnitEnum.BUg, "g", "AG80"),
    AG100("C10:0", 6, UnitEnum.BUg, "g", "AG100"),
    AG120("C12:0", 7, UnitEnum.BUg, "g", "AG120"),
    AG140("C14:0", 8, UnitEnum.BUg, "g", "AG140"),
    AG160("C16:0", 9, UnitEnum.BUg, "g", "AG160"),
    AG180("C18:0", 10, UnitEnum.BUg, "g", "AG180"),
    AG181("C18:1-n9", 11, UnitEnum.BUg, "g", "AG181"),
    AG182("C18:2-n6", 12, UnitEnum.BUg, "g", "AG182"),
    AG183("C18:3-n3", 13, UnitEnum.BUg, "g", "AG183"),
    AG204("C20:4-n6", 14, UnitEnum.BUg, "g", "AG204"),
    AG205("EPA", 15, UnitEnum.BUg, "g", "AG205"),
    AG226("DHA", 16, UnitEnum.BUg, "g", "AG226"),
    CHOL("Cholesterol", 17, UnitEnum.BUg, "g", "CHOLES"),
    O3("Omega 3", 18, UnitEnum.BUg, "g", "O3"),
    O6("Omega 6", 19, UnitEnum.BUg, "g", "O6"),
    EPADHA("EPA et DHA", 20, UnitEnum.BUg, "g", "EPADHA");

    companion object {
        private val coefMap by lazy { entries.associateBy { it.coef } }
        private val labelMap by lazy { entries.associateBy { it.label } }

        fun getByCoef(coef: Int) = coefMap[coef]
        fun getByLabel(label: String) = labelMap[label] ?: AG100
        fun isByLabel(label: String) = label in labelMap
        fun size() = entries.size
    }

    override fun getMNE() = MainNutrientEnum.LIPID


    fun nameToString() = displayName
}