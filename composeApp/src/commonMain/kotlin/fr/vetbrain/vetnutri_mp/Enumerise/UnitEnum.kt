package fr.vetbrain.vetnutri_mp.Enumerise



enum class UnitEnum(
    val Name: String,
    val ID: Int,
    val IDFamily: Int,
    val refID: Int,
    val conv: Float
) {
    BUg("g", 1, 1, 1, 1f),
    BUmg("mg", 2, 1, 1, 0.001f),
    BUmu("µg", 3, 1, 1, 0.000001f),
    AUui("UI", 4, 2, 4, 1f),
    AUmu("µg", 5, 2, 4, 3.33f),
    DUui("UI", 6, 3, 6, 1f),
    DUmu("µg", 7, 3, 6, 40f),
    EUui("UI", 8, 4, 6, 1f),
    EUmg("mg", 9, 4, 8, 1f),
    NO("", 0, 5, 10, 0f);

    companion object {
        fun byId(id: Int): UnitEnum {
            return entries.firstOrNull { it.ID == id } ?: BUg
        }

        fun getByName(str: String, family: Int): UnitEnum {
            return entries.firstOrNull { it.Name == str && it.IDFamily == family } ?: BUg
        }
    }
}