package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.AlimIndic
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class AlimentEv(
        val uuid: String = Uuid.random().toString(),
        val group: GroupAlim?,
        val typeAliment: FoodKind?,
        val ingredients: String?,
        val price: Double?,
        val categPrice: String?,
        val brand: String?,
        val gamme: String?,
        val nom: String?,
        val consistent: Boolean,
        val cont: Int?,
        val quantInt: Float?,
        val deprecated: Int?,
        val dataB: String?,
        var especes: MutableList<String>,
        var indicat: MutableList<AlimIndic>
)
