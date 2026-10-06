package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.CaseEquilibre
import fr.vetbrain.vetnutri_mp.Data.CibleExploration
import fr.vetbrain.vetnutri_mp.Data.ConfigurationExploration
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
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
import fr.vetbrain.vetnutri_mp.Data.intervalleExploration
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.ConsultationRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Repository.MultiRationExplorationRepository
import fr.vetbrain.vetnutri_mp.Utils.instantNow
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import fr.vetbrain.vetnutri_mp.Utils.today
import fr.vetbrain.vetnutri_mp.View.AnalNut.adjustmentNeedMultiplier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Exploration multiration : configuration de la grille (référentiels × poids × K), des six listes
 * d'ingrédients et des cibles, calcul de toutes les rations et ouverture d'une ration (ou d'une
 * case entière) dans l'analyse de ration d'une consultation de travail.
 */
class MultiRationViewModel(
        private val foodRepository: FoodRepository,
        private val referenceEvRepository: DatabaseReferenceEvRepository,
        private val equationRepository: EquationRepository,
        private val animalRepository: AnimalRepository,
        private val consultationRepository: ConsultationRepository,
        private val explorationRepository: MultiRationExplorationRepository
) : ViewModel() {

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

    private val _scenarioSelectionne = MutableStateFlow<ScenarioExploration?>(null)
    val scenarioSelectionne: StateFlow<ScenarioExploration?> = _scenarioSelectionne.asStateFlow()

    private val _ouvertureEnCours = MutableStateFlow(false)
    val ouvertureEnCours: StateFlow<Boolean> = _ouvertureEnCours.asStateFlow()

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

    /** Animal de travail regroupant les rations ouvertes pendant cette session d'exploration. */
    private var animalDeTravail: AnimalEv? = null
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

    fun ajouterAliment(role: RoleExploration, aliment: AlimentEv) = modifierParametres { p ->
        val liste = p.listes[role].orEmpty()
        if (liste.any { it.uuid == aliment.uuid }) p
        else p.copy(listes = p.listes + (role to (liste + aliment)))
    }

    fun retirerAliment(role: RoleExploration, alimentId: String) = modifierParametres { p ->
        p.copy(listes = p.listes + (role to p.listes[role].orEmpty().filterNot { it.uuid == alimentId }))
    }

    fun modifierCible(role: RoleExploration, cible: CibleExploration) = modifierParametres { p ->
        p.copy(cibles = p.cibles + (role to cible))
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

    fun nombreScenarios(p: ParametresExploration = _parametres.value): Long {
        val (poids, k) = grille(p)
        val combinaisons = RoleExploration.entries.fold(1L) { acc, role -> acc * p.listes[role].orEmpty().size }
        return combinaisons * (poids?.size ?: 0) * (k?.size ?: 0) * p.referenceIds.size
    }

    /** Ce qui empêche de lancer le calcul (vide = prêt). */
    fun problemes(p: ParametresExploration = _parametres.value): List<String> {
        val liste = mutableListOf<String>()
        if (p.referenceIds.isEmpty()) liste += "Choisir au moins un référentiel (${p.espece.name.lowercase()})."
        val (poids, k) = grille(p)
        if (poids == null) liste += "Poids : bornes positives, maximum ≥ minimum, pas positif."
        if (k == null) liste += "K : bornes positives, maximum ≥ minimum, pas positif."
        val vides = RoleExploration.entries.filter { p.listes[it].isNullOrEmpty() }
        if (vides.isNotEmpty()) liste += "Listes vides : ${vides.joinToString { it.libelle }}."
        val limite = p.maxScenarios.toIntOrNull()
        if (limite == null || limite <= 0) liste += "Limite de scénarios invalide."
        else if (nombreScenarios(p) > limite)
            liste += "${nombreScenarios(p)} scénarios dépassent la limite de $limite : réduire les listes ou la grille, ou relever la limite."
        return liste
    }

    // --- Calcul ---------------------------------------------------------------------------------

    fun lancerCalcul() {
        val p = _parametres.value
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
                                                    ignorerOptimax = p.ignorerOptimax,
                                                    maxScenarios = p.maxScenarios.toInt()
                                            )
                                    ExplorateurMultiration(equationRepository).explorer(config) { faits, total ->
                                        if (faits % 10 == 0 || faits == total) _progression.value = Progression(faits, total)
                                    }
                                }
                        _resultat.value = resultat
                        _parametresDuResultat.value = p
                        _caseSelectionnee.value = null
                        _scenarioSelectionne.value = null
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: ExplorationException) {
                        _erreur.value = e.message
                    } catch (e: Exception) {
                        _erreur.value = "Calcul impossible : ${e.message}"
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

    fun selectionnerCase(case: CaseEquilibre?) {
        _caseSelectionnee.value = case
        _scenarioSelectionne.value = case?.meilleurScenario
    }

    fun selectionnerScenario(scenario: ScenarioExploration) {
        _scenarioSelectionne.value = scenario
    }

    // --- Ouverture dans l'analyse de ration ---------------------------------------------------

    /**
     * Enregistre le scénario (ou toutes les combinaisons de la case, la meilleure d'abord) comme
     * consultation de travail — référentiel, poids et K (coefficient d'ajustement) de la case — puis
     * appelle [ouvrir] avec l'animal et la consultation, pour les analyser et les éditer avec les
     * écrans habituels (analyse détaillée, ajustement multi-nutriments, export PDF).
     */
    fun ouvrirDansAnalyse(
            case: CaseEquilibre,
            scenarios: List<ScenarioExploration>,
            ouvrir: (AnimalEv, String) -> Unit
    ) {
        if (scenarios.isEmpty() || _ouvertureEnCours.value) return
        viewModelScope.launch {
            _ouvertureEnCours.value = true
            try {
                val animal = obtenirAnimalDeTravail(case.reference.espece)
                val consultationId = genUUID()
                val rations =
                        scenarios.mapIndexed { i, s -> copierRation(s, consultationId, i + 1, actuelle = i == 0) }
                val consultation =
                        ConsultationEv(
                                uuid = consultationId,
                                idAnim = animal.uuid,
                                date = today().date,
                                objectConsult =
                                        "Exploration multiration — ${case.reference.nom} — ${formaterQuantite(case.poids)} kg — K ${formaterQuantite(case.k)}",
                                observation =
                                        scenarios.joinToString("\n") { "${it.id} : ${it.statut.libelle}${if (it.seuilsManques.isNotEmpty()) " — " + it.seuilsManques.joinToString() else ""}" },
                                weight = case.poids,
                                referenceGeneraleId = case.reference.uuid,
                                // Besoin total = BEE standard × K, comme dans l'exploration
                                coefficientAjustement = case.k,
                                rations = rations.toMutableList()
                        )
                consultationRepository.saveConsultation(consultation)
                ouvrir(animal, consultation.uuid)
            } catch (e: Exception) {
                _erreur.value = "Ouverture impossible : ${e.message}"
            } finally {
                _ouvertureEnCours.value = false
            }
        }
    }

    private suspend fun obtenirAnimalDeTravail(espece: Espece): AnimalEv {
        animalDeTravail?.let { existant ->
            if (existant.getEspece() == espece && animalRepository.getAnimalById(existant.uuid) != null) return existant
        }
        val animal =
                AnimalEv(
                        nom = "Exploration multiration ${today().date}",
                        specieId = espece.label,
                        summary = "Animal de travail créé par l'exploration multiration : chaque consultation regroupe les rations d'une case référentiel × poids × K."
                )
        animalRepository.saveAnimal(animal)
        animalDeTravail = animal
        return animal
    }

    private fun copierRation(s: ScenarioExploration, consultationId: String, numero: Int, actuelle: Boolean): Ration {
        val rationId = genUUID()
        return s.ration.copy(
                uuid = rationId,
                idConsult = consultationId,
                name = "${s.id} — ${s.statut.libelle}",
                number = numero,
                actual = actuelle,
                description = s.composition,
                alimentMutableList =
                        s.ration.alimentMutableList
                                .filter { it.quantite > 0.0 }
                                .map { it.copy(uuid = genUUID(), refRation = rationId) }
                                .toMutableList()
        )
    }

    // --- Configurations enregistrées --------------------------------------------------------------

    fun chargerExplorations() {
        viewModelScope.launch {
            _explorations.value =
                    try {
                        explorationRepository.getAllExplorations()
                    } catch (e: Exception) {
                        _erreur.value = "Lecture des explorations enregistrées impossible : ${e.message}"
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
            _erreur.value = "Donner un nom à l'exploration avant de l'enregistrer."
            return
        }
        val p = _parametres.value
        val courante = _explorationCourante.value
        val exploration =
                ExplorationEnregistree(
                        uuid = if (courante != null && !commeNouvelle) courante.uuid else genUUID(),
                        nom = nomPropre,
                        espece = p.espece.name,
                        configurationJson = p.versJson().versJson(),
                        updatedAt = instantNow().toEpochMilliseconds()
                )
        viewModelScope.launch {
            try {
                explorationRepository.saveExploration(exploration)
                _explorationCourante.value = exploration
                _message.value = "Exploration « $nomPropre » enregistrée."
                _explorations.value = explorationRepository.getAllExplorations()
            } catch (e: Exception) {
                _erreur.value = "Enregistrement impossible : ${e.message}"
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
                _parametres.value = parametres
                _resultat.value = null
                _parametresDuResultat.value = null
                _caseSelectionnee.value = null
                _scenarioSelectionne.value = null
                _explorationCourante.value = exploration
                _message.value =
                        "Exploration « ${exploration.nom} » chargée." +
                                if (manquants.isEmpty()) "" else " Introuvables (ignorés) : ${manquants.joinToString()}."
            } catch (e: Exception) {
                _erreur.value = "Chargement impossible : ${e.message}"
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
                _erreur.value = "Suppression impossible : ${e.message}"
            }
        }
    }

    /** Repart d'une configuration vide (nouvelle exploration). */
    fun nouvelleExploration() {
        annulerCalcul()
        _parametres.value = ParametresExploration(espece = _parametres.value.espece)
        _resultat.value = null
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
        val entete =
                listOf("scenario", "combinaison", "referentiel", "poids_kg", "K", "besoin_standard_kcal", "besoin_total_kcal", "energie_kcal", "ecart_energie_kcal", "statut", "composition") +
                        RoleExploration.entries.flatMap { listOf("aliment_${it.name.lowercase()}", "quantite_${it.name.lowercase()}_g") } +
                        listOf("seuils_non_respectes", "seuils_non_renseignes", "message")
        val lignes =
                r.scenarios.map { s ->
                    listOf(
                            s.id, s.combinaison.toString(), s.reference.nom, nombreCsv(s.poids), nombreCsv(s.k),
                            nombreCsv(s.besoinStandard), nombreCsv(s.besoinTotal), nombreCsv(s.energie), nombreCsv(s.ecartEnergie),
                            s.statut.name, s.composition
                    ) +
                            s.ration.alimentMutableList.flatMap { listOf(it.aliment?.nom ?: "", nombreCsv(it.quantite)) } +
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
