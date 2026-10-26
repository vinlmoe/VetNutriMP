package fr.vetbrain.vetnutri_mp.ViewModel

// Helpers consultations / rations de la fiche animal (extensions extraites de AnimalDetailViewModel).

import fr.vetbrain.vetnutri_mp.Data.CalculMetabolique
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv

/** Consultation sélectionnée par défaut : la plus récente datée, sinon la première. */
internal fun AnimalDetailViewModel.consultationParDefaut(consultations: List<ConsultationEv>): ConsultationEv? =
        consultations.filter { it.date != null }.maxByOrNull { it.date!! }
                ?: consultations.firstOrNull()

/** Copie profonde d'une ration (ration + liste d'aliments). */
internal fun AnimalDetailViewModel.copieProfonde(ration: Ration): Ration =
        ration.copy(
                alimentMutableList =
                        ration.alimentMutableList.map { it.copy() }.toMutableList(),
                suppVarp = ration.suppVarp.toMutableList()
        )
