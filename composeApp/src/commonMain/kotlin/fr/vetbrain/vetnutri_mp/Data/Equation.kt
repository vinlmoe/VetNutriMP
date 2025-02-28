package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Equation(
        val uuid: String = Uuid.random().toString(),
        var script: String?,
        var refBiblio: String?,
        var name: String?,
        var description: String?,
        var specie: String?,
        var kind: Int?,
        var consistent: Boolean?,
        var nutrient: Int?,
        var bib: BiblioRef? = null,
        var varMutableList: MutableList<VariableKind>
)
