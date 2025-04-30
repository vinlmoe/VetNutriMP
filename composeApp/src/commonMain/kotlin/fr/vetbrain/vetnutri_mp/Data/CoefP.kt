package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Util.UuidUtil

/** Classe représentant un coefficient modificateur */
data class CoefP(
        val uuid: String = UuidUtil.generateUuid(),
        var description: String? = null,
        var coef: Float? = 1.0f,
        var groupUUID: Int? = 0
)
