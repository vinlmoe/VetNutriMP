package fr.vetbrain.vetnutri_mp.Enumer



enum class KindData(private val nom: String, private val unite: String) {
    SER("SER", "Mcal"),
    DM("DM", "100g DM"),
    BW("BW", "kg"),
    MW("BW", "kg MW"),
    FENER("BW", "Mcal"),
    FDESC("BW", "100g"),
    NO("no", ""),
    AMINO("", ""),
    INGRED("", ""),
    PP("", ""),
    INDICAT("", ""),
    LIP("", "");

    fun nameToString() = nom
    fun getUnit() = unite
}
