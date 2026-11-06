package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.CaseEquilibre
import fr.vetbrain.vetnutri_mp.Data.CibleExploration
import fr.vetbrain.vetnutri_mp.Data.ConfigurationExploration
import fr.vetbrain.vetnutri_mp.Data.ExplorateurMultiration
import fr.vetbrain.vetnutri_mp.Data.ConfigurationExplorationJson
import fr.vetbrain.vetnutri_mp.Data.ExplorationEnregistree
import fr.vetbrain.vetnutri_mp.Data.ParametresExploration
import fr.vetbrain.vetnutri_mp.Data.versJson
import fr.vetbrain.vetnutri_mp.Data.versParametres
import fr.vetbrain.vetnutri_mp.Data.ExplorationException
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.ResultatExploration
import fr.vetbrain.vetnutri_mp.Data.RoleExploration
import fr.vetbrain.vetnutri_mp.Data.ScenarioExploration
import fr.vetbrain.vetnutri_mp.Data.formaterQuantite
import fr.vetbrain.vetnutri_mp.Data.copierRationExploration
import fr.vetbrain.vetnutri_mp.Data.rolesExplorationActifs
import fr.vetbrain.vetnutri_mp.Data.intervalleExploration
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Repository.MultiRationExplorationRepository
import fr.vetbrain.vetnutri_mp.Utils.instantNow
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import fr.vetbrain.vetnutri_mp.Data.adjustmentNeedMultiplier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum

/**
 * Exploration multiration : configuration de la grille (référentiels × poids × K), des six listes
 * d'ingrédients et des cibles, calcul de toutes les rations et ouverture d'une ration (ou d'une
 * case entière) dans une vue ration autonome en mémoire.
 */
class MultiRationViewModel(
        private val foodRepository: FoodRepository,
        private val referenceEvRepository: DatabaseReferenceEvRepository,
        val equationRepository: EquationRepository,
        private val explorationRepository: MultiRationExplorationRepository,
        preferencesRepository: PreferencesRepository
) : ViewModel() {

    /** Préférences de l'application (mode d'affichage des besoins dans le détail d'un nutriment) */
    val preferences: StateFlow<PreferencesApplication?> = preferencesRepository.preferencesFlow

    init {
        viewModelScope.launch { preferencesRepository.getLoadedPreferences() }
    }

    data class Progression(val faits: Int, val total: Int)

    private val _references = MutableStateFlow<List<ReferenceEv>>(emptyList())
    /** Référentiels généraux (hors maladies). */
    val references: StateFlow<List<ReferenceEv>> = _references.asStateFlow()

    private val _parametres = MutableStateFlow(ParametresExploration())
    val parametres: StateFlow<ParametresExploration> = _parametres.asStateFlow()

    private val _resultat = MutableStateFlow<ResultatExploration?>(null)
    val resultat: StateFlow<ResultatExploration?> = _resultat.asStateFlow()

    /** Paramètres ayant produit le résultat affiché (pour signaler un résultat périmé). */
    private val _parametresDuResultat = MutableStateFlow<ParametresExploration?>(null)
    val parametresDuResultat: StateFlow<ParametresExploration?> = _parametresDuResultat.asStateFlow()

    private val _progression = MutableStateFlow<Progression?>(null)
    val progression: StateFlow<Progression?> = _progression.asStateFlow()

    private val _erreur = MutableStateFlow<String?>(null)
    val erreur: StateFlow<String?> = _erreur.asStateFlow()

    private val _caseSelectionnee = MutableStateFlow<CaseEquilibre?>(null)
    val caseSelectionnee: StateFlow<CaseEquilibre?> = _caseSelectionnee.asStateFlow()

    private val _synthese = MutableStateFlow(SyntheseExploration.VIDE)
    /** Classement des combinaisons et seuils limitants du résultat affiché. */
    val synthese: StateFlow<SyntheseExploration> = _synthese.asStateFlow()

    private val _combinaisonFiltre = MutableStateFlow<Int?>(null)
    /** Combinaison dont la carte affiche les zones (null = toutes les combinaisons). */
    val combinaisonFiltre: StateFlow<Int?> = _combinaisonFiltre.asStateFlow()

    private val _casesAffichees = MutableStateFlow<List<CaseEquilibre>>(emptyList())
    /** Cases de la carte, limitées à [combinaisonFiltre] s'il est choisi. */
    val casesAffichees: StateFlow<List<CaseEquilibre>> = _casesAffichees.asStateFlow()

    private val _scenarioSelectionne = MutableStateFlow<ScenarioExploration?>(null)
    val scenarioSelectionne: StateFlow<ScenarioExploration?> = _scenarioSelectionne.asStateFlow()

    private var corrections = emptyList<CorrectionRationExploration>()
    private val _enregistrementCorrections = MutableStateFlow(false)
    val enregistrementCorrections = _enregistrementCorrections.asStateFlow()

    private val _rationsOuvertes = MutableStateFlow<List<ScenarioExploration>>(emptyList())
    val rationsOuvertes = _rationsOuvertes.asStateFlow()

    private val _explorations = MutableStateFlow<List<ExplorationEnregistree>>(emptyList())
    /** Configurations enregistrées en base, la plus récente d'abord. */
    val explorations: StateFlow<List<ExplorationEnregistree>> = _explorations.asStateFlow()

    private val _explorationCourante = MutableStateFlow<ExplorationEnregistree?>(null)
    /** Configuration enregistrée en cours d'édition (null = nouvelle). */
    val explorationCourante: StateFlow<ExplorationEnregistree?> = _explorationCourante.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    /** Information non bloquante (enregistrement, chargement). */
    val message: StateFlow<String?> = _message.asStateFlow()

    /** Aliments du catalogue (version observée par le repository). */
    val aliments = foodRepository.observeAllFoods()

    private var calcul: Job? = null

    fun chargerReferences() {
        viewModelScope.launch {
            val toutes = referenceEvRepository.getAllReferenceEv()
            _references.value = toutes.filter { !it.maladie }.sortedWith(compareBy({ it.espece.name }, { it.nom }))
        }
        chargerExplorations()
    }

    /** Espèces ayant au moins un référentiel général. */
    fun especesDisponibles(): List<Espece> =
            _references.value.map { it.espece }.distinct().sortedBy { it.categorie }

    /** Référentiels généraux de l'espèce choisie. */
    fun referencesEspece(p: ParametresExploration = _parametres.value): List<ReferenceEv> =
            _references.value.filter { it.espece == p.espece }

    /** Changer d'espèce retire les référentiels d'une autre espèce de la sélection. */
    fun choisirEspece(espece: Espece) = modifierParametres { p ->
        val autorises = _references.value.filter { it.espece == espece }.map { it.uuid }.toSet()
        p.copy(espece = espece, referenceIds = p.referenceIds.intersect(autorises))
    }

    // --- Paramètres -------------------------------------------------------------------------

    fun modifierParametres(transformation: (ParametresExploration) -> ParametresExploration) {
        _parametres.update(transformation)
    }

    fun basculerReference(id: String) = modifierParametres {
        it.copy(referenceIds = if (id in it.referenceIds) it.referenceIds - id else it.referenceIds + id)
    }

    /** Coche (ou décoche) tous les référentiels généraux de l'espèce choisie. */
    fun selectionnerToutesReferences(toutes: Boolean) = modifierParametres { p ->
        p.copy(referenceIds = if (toutes) referencesEspece(p).map { it.uuid }.toSet() else emptySet())
    }

    fun viderListe(role: RoleExploration) = modifierParametres { p ->
        p.copy(listes = p.listes + (role to emptyList()))
    }

    fun ajouterListe(): RoleExploration {
        val role = RoleExploration.supplement()
        modifierParametres { p -> p.copy(
                listes = p.listes + (role to emptyList()),
                cibles = p.cibles + (role to CibleExploration(role.nutrimentParDefaut)),
                doseMinimale = p.doseMinimale + (role to role.doseParDefaut),
                pasArrondiManuel = p.pasArrondiManuel + (role to 0.1)
        ) }
        return role
    }

    fun retirerListe(role: RoleExploration) {
        if (!role.supplementaire) return
        modifierParametres { p -> p.copy(
                listes = p.listes - role, cibles = p.cibles - role,
                doseMinimale = p.doseMinimale - role, pasArrondiManuel = p.pasArrondiManuel - role
        ) }
    }

    fun ajouterAliment(role: RoleExploration, aliment: AlimentEv) = modifierParametres { p ->
        val liste = p.listes[role].orEmpty()
        if (liste.any { it.uuid == aliment.uuid }) p
        else p.copy(listes = p.listes + (role to (liste + aliment)))
    }

    fun retirerAliment(role: RoleExploration, alimentId: String) = modifierParametres { p ->
        p.copy(listes = p.listes + (role to p.listes[role].orEmpty().filterNot { it.uuid == alimentId }))
    }

    /** Changer le nutriment des fibres remet le facteur par défaut (×5 pour la cellulose). */
    fun modifierNutrimentCible(role: RoleExploration, nutriment: Nutrient) = modifierParametres { p ->
        val actuelle = p.cibles[role] ?: CibleExploration(role.nutrimentParDefaut)
        p.copy(cibles = p.cibles + (role to actuelle.copy(nutriment = nutriment, facteur = adjustmentNeedMultiplier(nutriment))))
    }

    fun modifierNiveauCible(role: RoleExploration, niveau: Reflevel) = modifierParametres { p ->
        val actuelle = p.cibles[role] ?: CibleExploration(role.nutrimentParDefaut)
        p.copy(cibles = p.cibles + (role to actuelle.copy(niveau = niveau)))
    }

    fun modifierFacteurCible(role: RoleExploration, facteur: Double) = modifierParametres { p ->
        val actuelle = p.cibles[role] ?: CibleExploration(role.nutrimentParDefaut)
        p.copy(cibles = p.cibles + (role to actuelle.copy(facteur = facteur)))
    }

    fun modifierDoseMinimale(role: RoleExploration, dose: Double) = modifierParametres { p ->
        p.copy(doseMinimale = p.doseMinimale + (role to dose))
    }

    fun modifierPasArrondi(role: RoleExploration, pas: Double) = modifierParametres { p ->
        p.copy(pasArrondiManuel = p.pasArrondiManuel + (role to pas))
    }

    // --- Contrôles avant calcul ------------------------------------------------------------

    /** Grilles de poids et de K, ou message d'erreur. */
    fun grille(p: ParametresExploration = _parametres.value): Pair<List<Double>?, List<Double>?> {
        fun intervalle(a: String, b: String, c: String) =
                try {
                    intervalleExploration(nombre(a), nombre(b), nombre(c))
                } catch (e: ExplorationException) {
                    null
                }
        return intervalle(p.poidsDe, p.poidsA, p.poidsPas) to intervalle(p.kDe, p.kA, p.kPas)
    }

    /** Nombre de combinaisons d'aliments (un aliment par liste active). */
    fun nombreCombinaisons(p: ParametresExploration = _parametres.value): Long =
            rolesExplorationActifs(p.listes, p.cibles, p.besoinsIgnores).fold(1L) { acc, role -> acc * p.listes[role].orEmpty().size }

    fun nombreScenarios(p: ParametresExploration = _parametres.value): Long {
        val (poids, k) = grille(p)
        return nombreCombinaisons(p) * (poids?.size ?: 0) * (k?.size ?: 0) * p.referenceIds.size
    }

    /** Listes actives encore vides (bloquent le calcul). */
    fun listesVides(p: ParametresExploration = _parametres.value): List<RoleExploration> =
            rolesExplorationActifs(p.listes, p.cibles, p.besoinsIgnores).filter { p.listes[it].isNullOrEmpty() }

    /** L'onglet Animaux est incomplet : aucun référentiel ou grille invalide. */
    fun animauxIncomplets(p: ParametresExploration = _parametres.value): Boolean {
        val (poids, k) = grille(p)
        return p.referenceIds.isEmpty() || poids == null || k == null
    }

    /** Ce qui empêche de lancer le calcul (vide = prêt). */
    fun problemes(p: ParametresExploration = _parametres.value): List<String> {
        val liste = mutableListOf<String>()
        if (p.referenceIds.isEmpty()) liste += translate("multiration.probleme.referentiel", p.espece.translateEnum().lowercase())
        val (poids, k) = grille(p)
        if (poids == null) liste += translate("multiration.probleme.poids")
        if (k == null) liste += translate("multiration.probleme.k")
        val vides = rolesExplorationActifs(p.listes, p.cibles, p.besoinsIgnores).filter { p.listes[it].isNullOrEmpty() }
        if (vides.isNotEmpty()) liste += translate("multiration.probleme.listesVides", vides.joinToString { translate("multiration.role." + if (it.supplementaire) "SUPPLEMENT" else it.name) })
        val limite = p.maxScenarios.toIntOrNull()
        if (limite == null || limite <= 0) liste += translate("multiration.probleme.limite")
        else if (nombreScenarios(p) > limite)
            liste += translate("multiration.probleme.depasse", nombreScenarios(p).toString(), limite.toString())
        return liste
    }

    // --- Calcul ---------------------------------------------------------------------------------

    fun lancerCalcul() {
        val p = _parametres.value
        val correctionsDuCalcul = corrections
        val problemes = problemes(p)
        if (problemes.isNotEmpty()) {
            _erreur.value = problemes.joinToString(" ")
            return
        }
        calcul?.cancel()
        calcul =
                viewModelScope.launch {
                    _erreur.value = null
                    _progression.value = Progression(0, nombreScenarios(p).toInt())
                    try {
                        val resultat =
                                withContext(AppDispatchers.Default) {
                                    // Aliments complets (valeurs nutritionnelles) depuis la base
                                    val ids = p.listes.values.flatten().map { it.uuid }.distinct()
                                    val complets = foodRepository.getFoodsByUuids(ids)
                                    val (poids, k) = grille(p)
                                    val config =
                                            ConfigurationExploration(
                                                    references = referencesEspece(p).filter { it.uuid in p.referenceIds },
                                                    poids = poids.orEmpty(),
                                                    coefficientsK = k.orEmpty(),
                                                    listes = p.listes.mapValues { (_, liste) -> liste.map { complets[it.uuid] ?: it } },
                                                    cibles = p.cibles,
                                                    arrondir = p.arrondir,
                                                    doseMinimale = p.doseMinimale,
                                                    pasArrondiManuel = p.pasArrondiManuel,
                                                    ignorerOptimax = p.ignorerOptimax,
                                                    maxScenarios = p.maxScenarios.toInt(),
                                                    besoinsIgnores = p.besoinsIgnores,
                                                    arrondirAuSuperieur = p.arrondirAuSuperieur
                                            )
                                    val explorateur = ExplorateurMultiration(equationRepository)
                                    val initial = explorateur.explorer(config) { faits, total ->
                                        if (faits % 10 == 0 || faits == total) _progression.value = Progression(faits, total)
                                    }
                                    val signature = p.versJson().versJson()
                                    val actives = correctionsDuCalcul.filter { it.configuration == signature }
                                    val alimentsCorriges = foodRepository.getFoodsByUuids(actives.flatMap { it.aliments.map { a -> a.alimentId } }.distinct())
                                    val r = appliquerCorrectionsExploration(initial, signature, actives, alimentsCorriges, explorateur)
                                    r to synthetiserExploration(r)
                                }
                        publierResultat(resultat.first, resultat.second, combinaison = null)
                        _parametresDuResultat.value = p
                        _caseSelectionnee.value = null
                        _scenarioSelectionne.value = null
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: ExplorationException) {
                        _erreur.value = e.message
                    } catch (e: Exception) {
                        e.rethrowIfCancellation()
                        _erreur.value = translate("multiration.erreur.calcul", e.message ?: "")
                    } finally {
                        _progression.value = null
                    }
                }
    }

    fun annulerCalcul() {
        calcul?.cancel()
        _progression.value = null
    }

    fun effacerErreur() {
        _erreur.value = null
    }

    /**
     * Sélectionne une case. Le détail montre toujours toutes les combinaisons de la case ; si la
     * carte est limitée à une combinaison, c'est son scénario qui est présélectionné.
     */
    fun selectionnerCase(case: CaseEquilibre?) {
        val complete = case?.let { c ->
            _resultat.value?.cases?.firstOrNull { it.reference.uuid == c.reference.uuid && it.poids == c.poids && it.k == c.k }
        } ?: case
        _caseSelectionnee.value = complete
        val filtre = _combinaisonFiltre.value
        _scenarioSelectionne.value =
                complete?.scenarios?.firstOrNull { filtre != null && it.combinaison == filtre } ?: complete?.meilleurScenario
    }

    /** Limite la carte à une combinaison d'aliments (null = toutes). */
    fun filtrerCombinaison(combinaison: Int?) {
        val r = _resultat.value ?: return
        _combinaisonFiltre.value = combinaison
        _casesAffichees.value = casesPourCombinaison(r, combinaison)
        _caseSelectionnee.value?.let { selectionnerCase(it) }
    }

    private fun publierResultat(r: ResultatExploration?, synthese: SyntheseExploration, combinaison: Int?) {
        val filtre = combinaison?.takeIf { c -> r?.scenarios?.any { it.combinaison == c } == true }
        _resultat.value = r
        _synthese.value = synthese
        _combinaisonFiltre.value = filtre
        _casesAffichees.value = if (r == null) emptyList() else casesPourCombinaison(r, filtre)
    }

    fun selectionnerScenario(scenario: ScenarioExploration) {
        _scenarioSelectionne.value = scenario
    }

    /** Copies en mémoire uniquement : aucun animal, aucune consultation, aucune écriture en base. */
    fun ouvrirDansAnalyse(scenarios: List<ScenarioExploration>) {
        _rationsOuvertes.value = scenarios.map { it.copy(ration = copierRationExploration(it.ration)) }
    }

    fun fermerRations() {
        _rationsOuvertes.value = emptyList()
    }

    fun modifierRationExploration(id: String, aliments: List<fr.vetbrain.vetnutri_mp.Data.AlimentRation>) {
        _rationsOuvertes.update { scenarios ->
            scenarios.map { s ->
                if (s.id != id) s else s.copy(ration = s.ration.copy(alimentMutableList = aliments.toMutableList()))
            }
        }
    }

    /** Sauvegarde d'abord, puis publication atomique des résultats recalculés. */
    fun enregistrerCorrections() {
        if (_enregistrementCorrections.value) return
        val resultat = _resultat.value ?: return
        val p = _parametresDuResultat.value ?: return
        if (p != _parametres.value) {
            _erreur.value = translate("multiration.erreur.recalculerAvantCorrections")
            return
        }
        val editions = _rationsOuvertes.value
        if (editions.isEmpty()) return
        val signature = p.versJson().versJson()
        val nouvelles = editions.map { s ->
            CorrectionRationExploration(signature, cleScenarioExploration(s),
                    s.ration.alimentMutableList.map { LigneCorrectionExploration(it.aliment?.uuid ?: it.refAlimUnif.orEmpty(), it.quantite) })
        }
        val cles = nouvelles.map { it.cleScenario }.toSet()
        val toutes = corrections.filterNot { it.configuration == signature && it.cleScenario in cles } + nouvelles
        _erreur.value = null
        _enregistrementCorrections.value = true
        viewModelScope.launch {
            try {
                val aliments = foodRepository.getFoodsByUuids(nouvelles.flatMap { it.aliments.map { a -> a.alimentId } }.distinct())
                val (actualise, synthese) = withContext(AppDispatchers.Default) {
                    val r = appliquerCorrectionsExploration(resultat, signature, nouvelles, aliments, ExplorateurMultiration(equationRepository))
                    r to synthetiserExploration(r)
                }
                val courante = _explorationCourante.value
                val exploration = ExplorationEnregistree(
                        uuid = courante?.uuid ?: genUUID(),
                        nom = courante?.nom ?: translate("multiration.explorationCorrigee", instantNow().toString()),
                        espece = p.espece.name,
                        configurationJson = p.versJson().copy(corrections = toutes).versJson(),
                        updatedAt = instantNow().toEpochMilliseconds()
                )
                explorationRepository.saveExploration(exploration)
                corrections = toutes
                _explorationCourante.value = exploration
                publierResultat(actualise, synthese, _combinaisonFiltre.value)
                val ancienneCase = _caseSelectionnee.value
                _caseSelectionnee.value = actualise.cases.firstOrNull {
                    ancienneCase != null && it.reference.uuid == ancienneCase.reference.uuid && it.poids == ancienneCase.poids && it.k == ancienneCase.k
                }
                _scenarioSelectionne.value = actualise.scenarios.firstOrNull { it.id == _scenarioSelectionne.value?.id }
                _rationsOuvertes.value = emptyList()
                _message.value = translate("multiration.message.correctionsEnregistrees")
                chargerExplorations()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                _erreur.value = translate("multiration.erreur.corrections", e.message ?: "")
            } finally {
                _enregistrementCorrections.value = false
            }
        }
    }

    suspend fun alimentComplet(id: String): AlimentEv? = foodRepository.getFoodsByUuids(listOf(id))[id]

    // --- Configurations enregistrées --------------------------------------------------------------

    fun chargerExplorations() {
        viewModelScope.launch {
            _explorations.value =
                    try {
                        explorationRepository.getAllExplorations()
                    } catch (e: Exception) {
                        e.rethrowIfCancellation()
                        _erreur.value = translate("multiration.erreur.lecture", e.message ?: "")
                        emptyList()
                    }
        }
    }

    /**
     * Enregistre la configuration courante sous [nom]. Si une configuration enregistrée est en
     * cours d'édition et que [commeNouvelle] est faux, elle est mise à jour ; sinon une nouvelle
     * est créée.
     */
    fun enregistrerExploration(nom: String, commeNouvelle: Boolean = false) {
        val nomPropre = nom.trim()
        if (nomPropre.isEmpty()) {
            _erreur.value = translate("multiration.erreur.nomManquant")
            return
        }
        val p = _parametres.value
        val courante = _explorationCourante.value
        val exploration =
                ExplorationEnregistree(
                        uuid = if (courante != null && !commeNouvelle) courante.uuid else genUUID(),
                        nom = nomPropre,
                        espece = p.espece.name,
                        configurationJson = p.versJson().copy(corrections = corrections).versJson(),
                        updatedAt = instantNow().toEpochMilliseconds()
                )
        viewModelScope.launch {
            try {
                explorationRepository.saveExploration(exploration)
                _explorationCourante.value = exploration
                _message.value = translate("multiration.message.enregistree", nomPropre)
                _explorations.value = explorationRepository.getAllExplorations()
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                _erreur.value = translate("multiration.erreur.enregistrement", e.message ?: "")
            }
        }
    }

    /** Recharge une configuration : référentiels et aliments retrouvés par identifiant. */
    fun chargerExploration(uuid: String) {
        viewModelScope.launch {
            try {
                val exploration = explorationRepository.getExplorationById(uuid) ?: return@launch
                val json = ConfigurationExplorationJson.depuisJson(exploration.configurationJson)
                if (_references.value.isEmpty()) {
                    _references.value =
                            referenceEvRepository.getAllReferenceEv().filter { !it.maladie }
                                    .sortedWith(compareBy({ it.espece.name }, { it.nom }))
                }
                val ids = json.listes.values.flatten().distinct()
                val aliments = foodRepository.getFoodsByUuids(ids)
                val (parametres, manquants) = json.versParametres( aliments, _references.value.map { it.uuid }.toSet())
                annulerCalcul()
                corrections = json.corrections
                _parametres.value = parametres
                publierResultat(null, SyntheseExploration.VIDE, null)
                _parametresDuResultat.value = null
                _caseSelectionnee.value = null
                _scenarioSelectionne.value = null
                _explorationCourante.value = exploration
                _message.value =
                        translate("multiration.message.chargee", exploration.nom) +
                                if (manquants.isEmpty()) "" else " " + translate("multiration.message.introuvables", manquants.joinToString())
                if (corrections.isNotEmpty() && manquants.isEmpty()) lancerCalcul()
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                _erreur.value = translate("multiration.erreur.chargement", e.message ?: "")
            }
        }
    }

    fun supprimerExploration(uuid: String) {
        viewModelScope.launch {
            try {
                explorationRepository.deleteExploration(uuid)
                if (_explorationCourante.value?.uuid == uuid) _explorationCourante.value = null
                _explorations.value = explorationRepository.getAllExplorations()
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                _erreur.value = translate("multiration.erreur.suppression", e.message ?: "")
            }
        }
    }

    /** Repart d'une configuration vide (nouvelle exploration). */
    fun nouvelleExploration() {
        annulerCalcul()
        corrections = emptyList()
        fermerRations()
        _parametres.value = ParametresExploration(espece = _parametres.value.espece)
        publierResultat(null, SyntheseExploration.VIDE, null)
        _parametresDuResultat.value = null
        _caseSelectionnee.value = null
        _scenarioSelectionne.value = null
        _explorationCourante.value = null
    }

    fun effacerMessage() {
        _message.value = null
    }

    // --- Exports CSV ----------------------------------------------------------------------------

    fun csvScenarios(): String {
        val r = _resultat.value ?: return ""
        val rolesExport = r.scenarios.flatMap { it.roles }.distinct()
        val entete =
                listOf("scenario", "combinaison", "referentiel", "poids_kg", "K", "besoin_standard_kcal", "besoin_total_kcal", "energie_kcal", "ecart_energie_kcal", "statut", "composition") +
                        rolesExport.flatMap { listOf("aliment_${it.name.lowercase()}", "quantite_${it.name.lowercase()}_g") } +
                        listOf("seuils_non_respectes", "seuils_non_renseignes", "message")
        val lignes =
                r.scenarios.map { s ->
                    listOf(
                            s.id, s.combinaison.toString(), s.reference.nom, nombreCsv(s.poids), nombreCsv(s.k),
                            nombreCsv(s.besoinStandard), nombreCsv(s.besoinTotal), nombreCsv(s.energie), nombreCsv(s.ecartEnergie),
                            s.statut.name, s.composition
                    ) +
                            rolesExport.flatMap { role ->
                                val ligne = s.ration.alimentMutableList.getOrNull(s.roles.indexOf(role))
                                listOf(ligne?.aliment?.nom ?: "", nombreCsv(ligne?.quantite))
                            } +
                            listOf(s.seuilsManques.joinToString("; "), s.seuilsNonRenseignes.joinToString("; "), s.message)
                }
        return csv(entete, lignes)
    }

    fun csvCases(): String {
        val r = _resultat.value ?: return ""
        val entete = listOf("referentiel", "poids_kg", "K", "zone", "combinaisons", "conformes", "sous_reserve", "min_seuils_manques", "seuils_limitants", "meilleur_scenario")
        val lignes =
                r.cases.map { c ->
                    listOf(
                            c.reference.nom, nombreCsv(c.poids), nombreCsv(c.k), c.zone.name, c.combinaisons.toString(),
                            c.conformes.toString(), c.sousReserve.toString(), c.minSeuilsManques?.toString() ?: "",
                            c.seuilsLimitants, c.meilleurScenario?.id ?: ""
                    )
                }
        return csv(entete, lignes)
    }

    private fun nombreCsv(v: Double?): String = v?.let { (kotlin.math.round(it * 1000.0) / 1000.0).toString() } ?: ""

    private fun csv(entete: List<String>, lignes: List<List<String>>): String {
        fun champ(v: String) = if (v.any { it == ';' || it == '"' || it == '\n' }) "\"" + v.replace("\"", "\"\"") + "\"" else v
        return (listOf(entete) + lignes).joinToString("\n") { ligne -> ligne.joinToString(";") { champ(it) } }
    }

    companion object {
        fun nombre(texte: String): Double = texte.trim().replace(',', '.').toDoubleOrNull() ?: Double.NaN
    }
}
