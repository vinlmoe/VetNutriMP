package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.lifecycle.ViewModel
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.CalculMetabolique
import fr.vetbrain.vetnutri_mp.Data.ConformiteStatus
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ApportRation
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.CalculPrevision
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.PreparateurPrevision
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.PreparationPrevision
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.PropositionRation
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ReglagesPrevisionPoids
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ResultatPrevision
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.SelectionRationsPrevision
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.entreesJournal
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.rationCombinee
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.resultatsJournal
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.analyserValeursNutritionnellesRationSelective
import fr.vetbrain.vetnutri_mp.Data.calculerConformite
import fr.vetbrain.vetnutri_mp.Data.nomTraduitNutriment
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Repository.CalculPrevisionJournalise
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationsMemorisees
import fr.vetbrain.vetnutri_mp.Repository.PrevisionPoidsRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.AppLog
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

/** État de la prévision affichée. */
sealed class EtatPrevision {
    /** Module désactivé, ou pas encore calculé. */
    data object Inactif : EtatPrevision()
    data object Calcul : EtatPrevision()
    data class Refus(val preparation: PreparationPrevision.Refus) : EtatPrevision()
    data class Resultat(val resultat: ResultatPrevision) : EtatPrevision()
    data class Erreur(val message: String) : EtatPrevision()
}

/**
 * Prévision du poids et besoin apparent du chien adulte (modèle chien-v2) sur la courbe de poids.
 * Les calculs se font hors du fil de l'interface, seulement si le module n'est pas désactivé ;
 * chaque calcul aux entrées nouvelles est enregistré dans le journal local.
 */
class PrevisionPoidsViewModel(
        /** Lecture d'un référentiel par identifiant. */
        private val lireReference: suspend (String) -> ReferenceEv?,
        private val equationRepository: EquationRepository?,
        private val depot: PrevisionPoidsRepository,
        dispatcherPrincipal: CoroutineDispatcher = AppDispatchers.Main,
        private val dispatcherCalcul: CoroutineDispatcher = AppDispatchers.Default
) : ViewModel() {
    private val scope = CoroutineScope(dispatcherPrincipal + SupervisorJob())
    private var calculEnCours: Job? = null
    private val json = Json { encodeDefaults = true }

    private val _reglages = MutableStateFlow(ReglagesPrevisionPoids())
    val reglages: StateFlow<ReglagesPrevisionPoids> = _reglages.asStateFlow()

    private val _etat = MutableStateFlow<EtatPrevision>(EtatPrevision.Inactif)
    val etat: StateFlow<EtatPrevision> = _etat.asStateFlow()

    private val _semainesCible = MutableStateFlow(CalculPrevision.SEMAINES_CIBLE_PAR_DEFAUT)
    /** Délai de l'objectif de perte de poids choisi par le clinicien, semaines après la dernière consultation. */
    val semainesCible: StateFlow<Int> = _semainesCible.asStateFlow()

    /** Dernières entrées journalisées par animal : un même calcul n'est pas enregistré deux fois. */
    private val derniersJournaux = mutableMapOf<String, String>()

    init {
        scope.launch {
            try {
                _reglages.value = depot.lireReglages()
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                AppLog.e("PrevisionPoidsViewModel", "Réglages illisibles", e)
            }
        }
    }

    fun modifierReglages(reglages: ReglagesPrevisionPoids) {
        _reglages.value = reglages
        if (!reglages.calculer) {
            calculEnCours?.cancel()
            _etat.value = EtatPrevision.Inactif
        }
        scope.launch {
            try {
                depot.enregistrerReglages(reglages)
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                AppLog.e("PrevisionPoidsViewModel", "Réglages non enregistrés", e)
            }
        }
    }

    fun choisirSemainesCible(semaines: Int) {
        _semainesCible.value = semaines.coerceIn(1, ConstantesChienV2.HORIZON_MAX_JOURS / 7)
    }

    /** Recalcule la prévision de [animal] (sans effet si le module est désactivé). */
    fun calculer(animal: AnimalEv) {
        val reglages = _reglages.value
        calculEnCours?.cancel()
        if (!reglages.calculer) {
            _etat.value = EtatPrevision.Inactif
            return
        }
        val semaines = _semainesCible.value
        _etat.value = EtatPrevision.Calcul
        calculEnCours = scope.launch {
            _etat.value = try {
                calculerPour(animal, reglages, semaines)
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                AppLog.e("PrevisionPoidsViewModel", "Échec du calcul de prévision", e)
                EtatPrevision.Erreur(e.message.orEmpty())
            }
        }
    }

    internal suspend fun calculerPour(animal: AnimalEv, reglages: ReglagesPrevisionPoids, semaines: Int): EtatPrevision {
        val equations = equationRepository?.let { EquationsMemorisees(it) }
        val refs = animal.consultations.associate { c -> c.uuid to referenceGenerale(c) }
        val apports = withContext(dispatcherCalcul) { apportsDesRations(animal.consultations, refs, equations) }
        return when (val preparation = PreparateurPrevision.preparer(animal, refs, apports)) {
            is PreparationPrevision.Refus -> EtatPrevision.Refus(preparation)
            is PreparationPrevision.Pret -> {
                var resultat = withContext(dispatcherCalcul) { CalculPrevision.calculer(preparation, reglages, semaines) }
                val proposition = resultat.proposition
                if (proposition != null) {
                    val derniere = animal.consultations.filter { it.date != null }.maxByOrNull { it.date!! }
                    val carences = withContext(dispatcherCalcul) {
                        carencesDeLaRation(derniere, resultat.entrees.rationsFutures, refs[derniere?.uuid], equations, proposition)
                    }
                    resultat = resultat.copy(proposition = proposition.copy(carences = carences))
                }
                journaliser(animal, resultat, semaines, reglages)
                EtatPrevision.Resultat(resultat)
            }
        }
    }

    private suspend fun referenceGenerale(c: ConsultationEv): ReferenceEv? =
            c.referenceGeneraleId?.let { id ->
                try {
                    lireReference(id)
                } catch (e: Exception) {
                    e.rethrowIfCancellation()
                    AppLog.w("PrevisionPoidsViewModel", "Référentiel $id illisible : ${e.message}")
                    null
                }
            }

    /** Énergie (kcal EM/j) et grammes de protéines et de lipides de chaque ration, avec le référentiel de sa consultation. */
    private suspend fun apportsDesRations(
            consultations: List<ConsultationEv>,
            refs: Map<String, ReferenceEv?>,
            equations: EquationRepository?
    ): Map<String, ApportRation> {
        val resultat = mutableMapOf<String, ApportRation>()
        for (c in consultations) {
            val ref = refs[c.uuid]
            for (ration in c.rations) {
                var kcal = 0.0
                var proteines: Double? = 0.0
                var lipides: Double? = 0.0
                for (ligne in ration.alimentMutableList) {
                    if (ligne.aliment == null || ligne.quantite <= 0) continue
                    kcal += ligne.getEnergie(ref, equations)
                    val p = ligne.getNutrientWithComplementary(NutrientMain.PROTEINE, equations, ref)
                    val l = ligne.getNutrientWithComplementary(NutrientMain.LIPIDE, equations, ref)
                    proteines = if (p == null || proteines == null) null else proteines + p * ligne.quantite / 100
                    lipides = if (l == null || lipides == null) null else lipides + l * ligne.quantite / 100
                }
                resultat[ration.uuid] = ApportRation(kcal, proteines, lipides)
            }
        }
        return resultat
    }

    /**
     * Nutriments essentiels (seuils MIN du référentiel) que la ration future, ramenée à l'énergie
     * proposée, ne couvre pas : mêmes contrôles que l'analyse de ration (besoins au poids visé).
     */
    private suspend fun carencesDeLaRation(
            consultation: ConsultationEv?,
            ids: List<String>,
            reference: ReferenceEv?,
            equations: EquationRepository?,
            proposition: PropositionRation
    ): List<String> {
        if (consultation == null || reference == null) return emptyList()
        val rations = consultation.rations.filter { it.uuid in ids }
        if (rations.isEmpty()) return emptyList()
        val combinee = rationCombinee(rations)
        val kcal = combinee.alimentMutableList.sumOf { it.getEnergie(reference, equations) }
        if (kcal <= 0) return emptyList()
        val facteur = proposition.kcalParJour / kcal
        val ration = combinee.copy(
                alimentMutableList = combinee.alimentMutableList.map { it.copy(quantite = it.quantite * facteur) }.toMutableList()
        )
        val nutriments = (reference.getRefMapMin().keys).filter { it != NutrientMain.ENERGIE }.distinctBy { it.label }
        if (nutriments.isEmpty()) return emptyList()
        val valeurs = analyserValeursNutritionnellesRationSelective(ration, nutriments.map { it.label }, equations, reference)
        val poids = proposition.poidsCible
        val besoinStandard = CalculMetabolique.besoinEnergetiqueStandard(poids, reference) ?: return emptyList()
        val poidsMetabolique = CalculMetabolique.poidsMetabolique(poids, reference)
        return nutriments.mapNotNull { n ->
            val valeur = valeurs[n.label] ?: return@mapNotNull null
            val conformite = calculerConformite(valeur, reference, besoinStandard, poids, poidsMetabolique) ?: return@mapNotNull null
            if (conformite.status == ConformiteStatus.CARENCE && conformite.isCritical && valeur.complete) nomTraduitNutriment(n)
            else null
        }
    }

    private suspend fun journaliser(animal: AnimalEv, resultat: ResultatPrevision, semaines: Int, reglages: ReglagesPrevisionPoids) {
        try {
            val entrees = json.encodeToString(resultat.entreesJournal(semaines, reglages))
            val resultats = json.encodeToString(resultat.resultatsJournal())
            val signature = entrees + resultats
            if (derniersJournaux[animal.uuid] == null) {
                depot.dernierCalcul(animal.uuid)?.let { derniersJournaux[animal.uuid] = it.inputsJson + it.resultsJson }
            }
            if (derniersJournaux[animal.uuid] == signature) return
            depot.journaliser(
                    CalculPrevisionJournalise(
                            uuid = genUUID(),
                            animalUuid = animal.uuid,
                            createdAt = Clock.System.now().toEpochMilliseconds(),
                            modelVersion = ConstantesChienV2.VERSION,
                            moduleState = reglages.etat.name,
                            inputsJson = entrees,
                            resultsJson = resultats
                    )
            )
            derniersJournaux[animal.uuid] = signature
        } catch (e: Exception) {
            e.rethrowIfCancellation()
            AppLog.e("PrevisionPoidsViewModel", "Calcul non journalisé", e)
        }
    }

    /** Consultation avec la sélection de rations modifiée (à enregistrer par l'appelant). */
    fun avecSelection(consultation: ConsultationEv, selection: SelectionRationsPrevision): ConsultationEv =
            consultation.copy(selectionPrevisionPoids = selection)

    override fun onCleared() {
        scope.cancel()
    }
}
