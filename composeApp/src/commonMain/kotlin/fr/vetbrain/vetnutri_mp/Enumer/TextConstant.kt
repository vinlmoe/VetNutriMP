package fr.vetbrain.vetnutri_mp.Enumer

enum class TextConstant(val value: String) {
    VERSION("3.2.20"),
    NOM("VetNutri"),
    STADE("Beta"),
    NBRATION("7");

    fun nameToString(): String = value
}
