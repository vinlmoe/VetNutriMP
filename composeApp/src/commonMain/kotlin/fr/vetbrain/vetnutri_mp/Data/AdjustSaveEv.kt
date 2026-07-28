package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class AdjustSaveEv(
        val uuid: String = genUUID(),
        var name: String?,
        var species: String?,
        var description: String?,
        var MutableList: MutableList<TargetDefinitionEv>,
        var esp: Espece? = null
)
