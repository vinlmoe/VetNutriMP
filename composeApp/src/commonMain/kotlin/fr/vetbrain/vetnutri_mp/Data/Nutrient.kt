package fr.vetbrain.vetnutri_mp.Data

interface Nutrient {
    val label: String
    val unite: String
    val coef: Int
}

enum class NutrientBase(
        override val label: String,
        override val unite: String,
        override val coef: Int
) : Nutrient {
    HUMIDITE("Humidité", "%", 0),
    PROTEINE("Protéines", "%", 1),
    LIPIDE("Lipides", "%", 2),
    CELLULOSE("Cellulose", "%", 3),
    CENDRE("Cendres", "%", 4),
    FIBRETOT("Fibres totales", "%", 5),
    ENA("ENA", "%", 6),
    AMIDON("Amidon", "%", 7),
    SUCRE("Sucres", "%", 8);

    companion object {
        fun getByCoef(coef: Int): NutrientBase? = values().find { it.coef == coef }
    }
}

enum class NutrientMacro(
        override val label: String,
        override val unite: String,
        override val coef: Int
) : Nutrient {
    CAL("Calcium", "g/kg", 0),
    PHOS("Phosphore", "g/kg", 1),
    NA("Sodium", "g/kg", 2),
    MG("Magnésium", "g/kg", 3);

    companion object {
        fun getByCoef(coef: Int): NutrientMacro? = values().find { it.coef == coef }
    }
}

enum class NutrientLipid(
        override val label: String,
        override val unite: String,
        override val coef: Int
) : Nutrient {
    O3("Oméga 3", "%", 0),
    O6("Oméga 6", "%", 1),
    EPADHA("EPA + DHA", "%", 2),
    AG205("EPA", "%", 3),
    AG226("DHA", "%", 4);

    companion object {
        fun getByCoef(coef: Int): NutrientLipid? = values().find { it.coef == coef }
    }
}
