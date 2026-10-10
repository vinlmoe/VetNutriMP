package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AnalyseTroupeau
import fr.vetbrain.vetnutri_mp.Data.AnalyseurTroupeau
import fr.vetbrain.vetnutri_mp.Data.ConsultationTroupeau
import fr.vetbrain.vetnutri_mp.Data.ContenuTroupeau
import fr.vetbrain.vetnutri_mp.Data.LigneRationTroupeau
import fr.vetbrain.vetnutri_mp.Data.ParametresTypeTroupeau
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.Troupeau
import fr.vetbrain.vetnutri_mp.Data.TypeAnimalTroupeau
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Repository.HerdRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import fr.vetbrain.vetnutri_mp.Utils.instantNow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Mode troupeau : groupes d'animaux (types × effectifs), consultations de groupe avec un
 * référentiel par type, ration saisie pour l'ensemble du groupe et analyse des apports de chaque
 * type, la ration étant répartie au prorata des besoins énergétiques.
 *
 * Les modifications sont enregistrées automatiquement (avec un court délai pendant la saisie).
 */
class HerdViewModel(
        private val foodRepository: FoodRepository,
        private val referenceEvRepository: DatabaseReferenceEvRepository,
        private val equationRepository: EquationRepository,
        private val herdRepository: HerdRepository
) : ViewModel() {

    private val _references = MutableStateFlow<List<ReferenceEv>>(emptyList())
    /** Référentiels généraux (hors maladies), toutes espèces. */
    val references: StateFlow<List<ReferenceEv>> = _references.asStateFlow()

    private val _troupeaux = MutableStateFlow<List<Troupeau>>(emptyList())
    val troupeaux: StateFlow<List<Troupeau>> = _troupeaux.asStateFlow()

    private val _troupeau = MutableStateFlow<Troupeau?>(null)
    /** Troupeau ouvert (null = liste des troupeaux). */
    val troupeau: StateFlow<Troupeau?> = _troupeau.asStateFlow()

    private val _consultationId = MutableStateFlow<String?>(null)
    val consultationId: StateFlow<String?> = _consultationId.asStateFlow()

    private val _analyse = MutableStateFlow<AnalyseTroupeau?>(null)
    val analyse: StateFlow<AnalyseTroupeau?> = _analyse.asStateFlow()

    private val _calculEnCours = MutableStateFlow(false)
    val calculEnCours: StateFlow<Boolean> = _calculEnCours.asStateFlow()

    private val _erreur = MutableStateFlow<String?>(null)
    val erreur: StateFlow<String?> = _erreur.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    /** Aliments du catalogue (recherche). */
    val aliments = foodRepository.observeAllFoods()

    /** Aliments complets (valeurs nutritionnelles) déjà chargés, par identifiant. */
    private val alimentsComplets = mutableMapOf<String, AlimentEv>()

    private var sauvegarde: Job? = null
    private var calcul: Job? = null

    fun charger() {
        viewModelScope.launch {
            try {
                _references.value =
                        referenceEvRepository.getAllReferenceEv().filter { !it.maladie }
                                .sortedWith(compareBy({ it.espece.name }, { it.nom }))
                _troupeaux.value = herdRepository.getAllHerds()
                if (_troupeau.value != null) recalculer()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _erreur.value = "Chargement impossible : ${e.message}"
            }
        }
    }

    fun espece(troupeau: Troupeau? = _troupeau.value): Espece =
            Espece.entries.firstOrNull { it.name == troupeau?.espece } ?: Espece.CHIEN

    /** Espèces ayant au moins un référentiel général. */
    fun especesDisponibles(): List<Espece> =
            _references.value.map { it.espece }.distinct().sortedBy { it.categorie }.ifEmpty { Espece.entries.toList() }

    /** Référentiels généraux de l'espèce du troupeau. */
    fun referencesEspece(): List<ReferenceEv> = _references.value.filter { it.espece == espece() }

    fun consultationCourante(): ConsultationTroupeau? {
        val id = _consultationId.value ?: return null
        return _troupeau.value?.contenu?.consultations?.firstOrNull { it.id == id }
    }

    // --- Troupeaux ------------------------------------------------------------------------------

    fun creerTroupeau(nom: String, espece: Espece) {
        val nomPropre = nom.trim()
        if (nomPropre.isEmpty()) {
            _erreur.value = "Donner un nom au troupeau."
            return
        }
        val troupeau =
                Troupeau(
                        nom = nomPropre,
                        espece = espece.name,
                        contenu =
                                ContenuTroupeau(
                                        types = listOf(TypeAnimalTroupeau(nom = "Adultes", nombre = 1, poids = 0.0))
                                ),
                        updatedAt = instantNow().toEpochMilliseconds()
                )
        viewModelScope.launch {
            try {
                herdRepository.saveHerd(troupeau)
                _troupeaux.value = herdRepository.getAllHerds()
                ouvrir(troupeau)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _erreur.value = "Création impossible : ${e.message}"
            }
        }
    }

    fun ouvrirTroupeau(uuid: String) {
        viewModelScope.launch {
            try {
                val troupeau = herdRepository.getHerdById(uuid)
                if (troupeau == null) {
                    _erreur.value = "Troupeau introuvable."
                    _troupeaux.value = herdRepository.getAllHerds()
                } else ouvrir(troupeau)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _erreur.value = "Ouverture impossible : ${e.message}"
            }
        }
    }

    private fun ouvrir(troupeau: Troupeau) {
        _troupeau.value = troupeau
        _consultationId.value = troupeau.contenu.consultations.maxByOrNull { it.date }?.id
        _analyse.value = null
        recalculer()
    }

    /** Retour à la liste : la saisie en attente est enregistrée tout de suite. */
    fun fermerTroupeau() {
        val troupeau = _troupeau.value
        sauvegarde?.cancel()
        calcul?.cancel()
        _troupeau.value = null
        _consultationId.value = null
        _analyse.value = null
        viewModelScope.launch {
            try {
                if (troupeau != null) herdRepository.saveHerd(troupeau)
                _troupeaux.value = herdRepository.getAllHerds()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _erreur.value = "Enregistrement impossible : ${e.message}"
            }
        }
    }

    fun supprimerTroupeau(uuid: String) {
        viewModelScope.launch {
            try {
                herdRepository.deleteHerd(uuid)
                if (_troupeau.value?.uuid == uuid) {
                    sauvegarde?.cancel()
                    _troupeau.value = null
                    _consultationId.value = null
                    _analyse.value = null
                }
                _troupeaux.value = herdRepository.getAllHerds()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _erreur.value = "Suppression impossible : ${e.message}"
            }
        }
    }

    fun renommer(nom: String) = modifierTroupeau { it.copy(nom = nom) }

    /** Changer d'espèce retire les référentiels d'une autre espèce des consultations. */
    fun choisirEspece(espece: Espece) = modifierTroupeau { t ->
        val autorises = _references.value.filter { it.espece == espece }.map { it.uuid }.toSet()
        t.copy(
                espece = espece.name,
                contenu =
                        t.contenu.copy(
                                consultations =
                                        t.contenu.consultations.map { c ->
                                            c.copy(types = c.types.map { p -> if (p.referenceId in autorises) p else p.copy(referenceId = null) })
                                        }
                        )
        )
    }

    // --- Types d'animaux ------------------------------------------------------------------------

    fun ajouterType() = modifierTroupeau { t ->
        t.copy(contenu = t.contenu.copy(types = t.contenu.types + TypeAnimalTroupeau(nom = "Type ${t.contenu.types.size + 1}")))
    }

    fun modifierType(type: TypeAnimalTroupeau) = modifierTroupeau { t ->
        t.copy(contenu = t.contenu.copy(types = t.contenu.types.map { if (it.id == type.id) type else it }))
    }

    fun retirerType(typeId: String) = modifierTroupeau { it.sansType(typeId) }

    // --- Consultations --------------------------------------------------------------------------

    fun nouvelleConsultation() {
        val t = _troupeau.value ?: return
        val maintenant = instantNow().toEpochMilliseconds()
        val consultation = t.nouvelleConsultation(maintenant, "Consultation ${t.contenu.consultations.size + 1}")
        modifierTroupeau { it.copy(contenu = it.contenu.copy(consultations = it.contenu.consultations + consultation)) }
        selectionnerConsultation(consultation.id)
    }

    fun dupliquerConsultation(id: String) {
        val source = _troupeau.value?.contenu?.consultations?.firstOrNull { it.id == id } ?: return
        val copie =
                source.copy(
                        id = genUUID(),
                        date = instantNow().toEpochMilliseconds(),
                        titre = source.titre + " (copie)",
                        ration = source.ration.map { it.copy(id = genUUID()) }
                )
        modifierTroupeau { it.copy(contenu = it.contenu.copy(consultations = it.contenu.consultations + copie)) }
        selectionnerConsultation(copie.id)
    }

    fun supprimerConsultation(id: String) {
        modifierTroupeau { t -> t.copy(contenu = t.contenu.copy(consultations = t.contenu.consultations.filterNot { it.id == id })) }
        if (_consultationId.value == id) {
            selectionnerConsultation(_troupeau.value?.contenu?.consultations?.maxByOrNull { it.date }?.id)
        }
    }

    fun selectionnerConsultation(id: String?) {
        _consultationId.value = id
        _analyse.value = null
        recalculer()
    }

    fun modifierConsultation(transformation: (ConsultationTroupeau) -> ConsultationTroupeau) {
        val id = _consultationId.value ?: return
        modifierTroupeau { t ->
            t.copy(contenu = t.contenu.copy(consultations = t.contenu.consultations.map { if (it.id == id) transformation(it) else it }))
        }
    }

    fun modifierParametres(parametres: ParametresTypeTroupeau) = modifierConsultation { it.avecParametres(parametres) }

    // --- Ration du groupe -----------------------------------------------------------------------

    fun ajouterAliment(aliment: AlimentEv) {
        alimentsComplets.remove(aliment.uuid) // rechargé complet au prochain calcul
        modifierConsultation { c ->
            if (c.ration.any { it.alimentId == aliment.uuid }) c
            else c.copy(ration = c.ration + LigneRationTroupeau(alimentId = aliment.uuid, nom = aliment.nom ?: ""))
        }
    }

    fun modifierQuantite(ligneId: String, quantite: Double) = modifierConsultation { c ->
        c.copy(ration = c.ration.map { if (it.id == ligneId) it.copy(quantite = quantite.coerceAtLeast(0.0)) else it })
    }

    fun retirerLigne(ligneId: String) = modifierConsultation { c -> c.copy(ration = c.ration.filterNot { it.id == ligneId }) }

    /**
     * Multiplie toutes les quantités pour que l'énergie apportée couvre le besoin énergétique du
     * groupe (proportions entre aliments conservées).
     */
    fun ajusterRationEnergie() {
        val facteur = _analyse.value?.facteurAjustementEnergie
        if (facteur == null || !facteur.isFinite() || facteur <= 0.0) {
            _erreur.value = "Ajustement impossible : besoins de tous les types et énergie de la ration nécessaires."
            return
        }
        modifierConsultation { c -> c.copy(ration = c.ration.map { it.copy(quantite = arrondiGramme(it.quantite * facteur)) }) }
        _message.value = "Quantités multipliées par ${(kotlin.math.round(facteur * 1000.0) / 1000.0)} pour couvrir le besoin énergétique du groupe."
    }

    private fun arrondiGramme(q: Double): Double = kotlin.math.round(q * 10.0) / 10.0

    // --- Enregistrement et calcul ---------------------------------------------------------------

    private fun modifierTroupeau(transformation: (Troupeau) -> Troupeau) {
        val actuel = _troupeau.value ?: return
        val modifie = transformation(actuel).copy(updatedAt = instantNow().toEpochMilliseconds())
        _troupeau.value = modifie
        planifierSauvegarde()
        recalculer(delai = 300)
    }

    private fun planifierSauvegarde() {
        sauvegarde?.cancel()
        sauvegarde =
                viewModelScope.launch {
                    delay(600)
                    val t = _troupeau.value ?: return@launch
                    try {
                        herdRepository.saveHerd(t)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        _erreur.value = "Enregistrement impossible : ${e.message}"
                    }
                }
    }

    private fun recalculer(delai: Long = 0) {
        calcul?.cancel()
        val troupeau = _troupeau.value
        val consultation = consultationCourante()
        if (troupeau == null || consultation == null) {
            _analyse.value = null
            _calculEnCours.value = false
            return
        }
        calcul =
                viewModelScope.launch {
                    if (delai > 0) delay(delai)
                    _calculEnCours.value = true
                    try {
                        val references = (_references.value.ifEmpty {
                            referenceEvRepository.getAllReferenceEv().filter { !it.maladie }.also { _references.value = it }
                        }).associateBy { it.uuid }
                        val manquants = consultation.ration.map { it.alimentId }.distinct().filter { it !in alimentsComplets }
                        if (manquants.isNotEmpty()) alimentsComplets.putAll(foodRepository.getFoodsByUuids(manquants))
                        val aliments = alimentsComplets.toMap()
                        _analyse.value =
                                withContext(AppDispatchers.Default) {
                                    AnalyseurTroupeau(equationRepository).analyser(troupeau, consultation, references, aliments)
                                }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        _erreur.value = "Analyse impossible : ${e.message}"
                    } finally {
                        _calculEnCours.value = false
                    }
                }
    }

    fun effacerErreur() {
        _erreur.value = null
    }

    fun effacerMessage() {
        _message.value = null
    }
}
