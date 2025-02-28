package fr.vetbrain.vetnutri_mp.Enumer

enum class NutrientBase(
        private val displayName: String,
        override val coef: Int,
        override val unite: String,
        override val ue: UnitEnum,
        override val label: String,
        val color: String
) : Nutrient {
    HUMIDITE("Humidité", 0, "g", UnitEnum.BUg, "HUM", "#5DFFFA"),
    PROTEINE("Protéines", 1, "g", UnitEnum.BUg, "PROT", "#3358FF"),
    LIPIDE("Lipides", 2, "g", UnitEnum.BUg, "LIP", "#F8FF5D"),
    ENA("ENA", 3, "g", UnitEnum.BUg, "ENA", "#FF5D5D"),
    CELLULOSE("Cellulose brute", 4, "g", UnitEnum.BUg, "CEL", "#4BE715"),
    CENDRE("Cendres", 5, "g", UnitEnum.BUg, "CEN", "#820326"),
    SUCRE("Sucres", 6, "g", UnitEnum.BUg, "SUC", "#C3A900"),
    AMIDON("Amidon", 7, "g", UnitEnum.BUg, "AMID", "#820326"),
    FIBRESOL("Fibre soluble", 8, "g", UnitEnum.BUg, "FIBRSOL", "#20C300"),
    FIBRETOT("Fibre totale", 9, "g", UnitEnum.BUg, "FIBRTOT", "#1A7D07"),
    NDF("Fibre soluble", 10, "g", UnitEnum.BUg, "NDF", "#20C300"),
    ADF("Fibre totale", 11, "g", UnitEnum.BUg, "ADF", "#1A7D07");

    companion object {
        private val coefMap by lazy { entries.associateBy { it.coef } }

        fun getByCoef(coef: Int) = coefMap[coef]
        fun getByLabel(label: String) = entries.firstOrNull { it.label == label }
        fun isByLabel(label: String) = entries.any { it.label == label }
        fun size() = entries.size
    }

    override fun getMNE() = MainNutrientEnum.BASE

    fun nameToString() = displayName
}
