package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Utils.genUUID

data class ConsultationKeyword(
        val uuid: String = genUUID(),
        val label: String
)
