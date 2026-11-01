package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.AnalyseTroupeau
import fr.vetbrain.vetnutri_mp.Data.AnalyseurTroupeau
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.EquationSource
import fr.vetbrain.vetnutri_mp.Data.LigneApportTroupeau
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.TypeAnimalTroupeau
import fr.vetbrain.vetnutri_mp.Data.apportsTroupeauEntier
import fr.vetbrain.vetnutri_mp.Data.parametresType
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.AppLog
import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Répartition de la ration d'un troupeau et synthèse des apports de chaque type d'animaux pour
 * la consultation affichée. Détenu par [AnimalDetailViewModel], qui décide quand recalculer.
 */
internal class AnalyseTroupeauConsultation(
        private val referenceRepository: DatabaseReferenceEvRepository,
        private val equationRepository: EquationSource?,
        private val referencesDisponibles: () -> List<ReferenceEv>,
        private val scope: CoroutineScope
) {
    private val _analyse = MutableStateFlow<AnalyseTroupeau?>(null)
    val analyse: StateFlow<AnalyseTroupeau?> = _analyse.asStateFlow()

    /** Apports au troupeau entier comparés aux besoins sommés (calculés une fois par analyse). */
    val apportsTroupeauEntier: StateFlow<List<LigneApportTroupeau>> =
            _analyse.map { it?.apportsTroupeauEntier().orEmpty() }
                    .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private var calcul: Job? = null

    fun referencesParId(): Map<String, ReferenceEv> = referencesDisponibles().associateBy { it.uuid }

    fun effacer() {
        calcul?.cancel()
        _analyse.value = null
    }

    /** Annule le calcul en cours et relance celui de [rationGroupe] (null : besoins seuls). */
    fun recalculer(types: List<TypeAnimalTroupeau>?, consultation: ConsultationEv?, rationGroupe: Ration?) {
        if (types == null || consultation == null) {
            effacer()
            return
        }
        calcul?.cancel()
        calcul = scope.launch {
            try {
                // Référentiels hors liste chargée (ex. supprimés depuis) : relus en base
                val dispo = referencesParId()
                val references =
                        types.mapNotNull { consultation.parametresType(it).referenceId }
                                .distinct()
                                .mapNotNull { id -> (dispo[id] ?: referenceRepository.getReferenceEvById(id))?.let { id to it } }
                                .toMap()
                val maladies =
                        consultation.referencesMaladies.distinct().mapNotNull { id ->
                            dispo[id] ?: referenceRepository.getReferenceEvById(id)
                        }
                _analyse.value = withContext(AppDispatchers.Default) {
                    AnalyseurTroupeau(equationRepository).analyser(types, consultation, rationGroupe, references, maladies)
                }
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                AppLog.e("AnalyseTroupeauConsultation", e.message ?: "Erreur", e)
            }
        }
    }
}
