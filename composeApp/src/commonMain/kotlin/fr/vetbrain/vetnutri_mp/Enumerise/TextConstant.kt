package fr.vetbrain.vetnutri_mp.Enumerise

enum class TextConstant(val value: String) {
    VERSION("0.1.30"),
    NOM("VetNutri"),
    STADE("Beta"),
    NBRATION("7");

    fun nameToString(): String = value
}