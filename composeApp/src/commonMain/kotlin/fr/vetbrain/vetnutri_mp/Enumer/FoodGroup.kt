package fr.vetbrain.vetnutri_mp.Enumer

import kotlinx.serialization.Serializable

@Serializable
enum class FoodGroup {
    CEREALES,
    PROTEINES,
    LEGUMES,
    FRUITS,
    GRAISSES,
    AUTRES;

    fun nameToString(): String {
        return when (this) {
            CEREALES -> "Céréales"
            PROTEINES -> "Protéines"
            LEGUMES -> "Légumes"
            FRUITS -> "Fruits"
            GRAISSES -> "Graisses"
            AUTRES -> "Autres"
        }
    }
}
