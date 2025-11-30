package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.*

/** Filtre par nutriment avec opérateur et valeur */
data class NutrientFilter(
        val nutrient: Nutrient? = null,
        val operator: NutrientOperator = NutrientOperator.GREATER_OR_EQUAL,
        val value: Double? = null
)

/** Opérateur pour les filtres de nutriments */
enum class NutrientOperator(val displayName: String) {
        GREATER_OR_EQUAL("≥"),
        LESS_OR_EQUAL("≤")
}

/** Critère de tri */
enum class SortCriteria(val displayName: String) {
        NAME("Nom"),
        PROTEIN("Protéines"),
        FAT("Lipides"),
        CARBOHYDRATE("Glucides"),
        FIBER("Fibres"),
        ENERGY("Énergie"),
        CALCIUM("Calcium"),
        PHOSPHORUS("Phosphore")
}

/** Ordre de tri */
enum class SortOrder(val displayName: String) {
        ASCENDING("Croissant"),
        DESCENDING("Décroissant")
}

/** État partagé pour les filtres de recherche d'aliments */
data class FoodSearchFilters(
        val searchQuery: String = "",
        val selectedFoodType: FoodKind? = null,
        val selectedFoodGroup: GroupAlim? = null,
        val selectedEspece: Espece? = null,
        val selectedIndications: Set<AlimIndic> = emptySet(),
        val dataB: String? = null,
        val aminoOnly: Boolean = false,
        val nutrientFilters: List<NutrientFilter> = emptyList(),
        val sortCriteria: SortCriteria? = null,
        val sortOrder: SortOrder = SortOrder.ASCENDING
)
