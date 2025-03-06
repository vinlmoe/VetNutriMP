package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class AlimentEv(
        val uuid: String = Uuid.random().toString(),
        val group: GroupAlim? = null,
        val typeAliment: FoodKind? = null,
        val ingredients: String? = null,
        val price: Double? = null,
        val categPrice: String? = null,
        val brand: String? = null,
        val gamme: String? = null,
        val nom: String? = null,
        val consistent: Boolean = false,
        val cont: ContEnum? = null,
        var quantInt: Float? = null,
        var deprecated: Boolean = false,
        var dataB: String? = null,
        var especes: MutableList<String> = mutableListOf(),
        var indicat: MutableList<AlimIndic> = mutableListOf(),
        var valMap: MutableMap<Nutrient, NutrientQuantity> = mutableMapOf(),
        val rationUUID: String?
)
