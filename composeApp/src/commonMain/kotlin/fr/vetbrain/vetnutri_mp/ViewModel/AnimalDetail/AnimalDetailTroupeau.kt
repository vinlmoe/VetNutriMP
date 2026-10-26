package fr.vetbrain.vetnutri_mp.ViewModel

// Helpers troupeau de la fiche animal (extensions extraites de AnimalDetailViewModel).

import fr.vetbrain.vetnutri_mp.Data.CalculMetabolique
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv

internal fun AnimalDetailViewModel.consultationPourAnalyse(c: ConsultationEv?, typeId: String?, a: AnimalEv?): ConsultationEv? {
    if (c == null || typeId == null) return c
    val type = a?.typesTroupeau?.firstOrNull { it.id == typeId } ?: return c
    return c.pourTypeTroupeau(c.parametresType(type))
}
