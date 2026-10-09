package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.ContEnum
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Utils.genUUID
import fr.vetbrain.vetnutri_mp.View.AnalNut.adjustmentNeedMultiplier
import fr.vetbrain.vetnutri_mp.View.AnalNut.arrondirQuantiteSelonRegles
import kotlin.math.abs
import kotlin.math.ceil
import kotlinx.coroutines.yield

/**
 * Exploration multiration : pour une grille de profils (référentiel × poids × K), construit toutes
 * les rations obtenues en prenant un aliment dans chacune des six listes (protéines, fibres,
 * calcium, oméga-6, sodium, énergie restante), ajuste les quantités successivement comme
 * l'ajustement multi-nutriments de l'analyse de ration, puis évalue chaque ration avec les mêmes
 * fonctions que l'écran d'analyse (analyseur de ration + calculerConformite).
 *
 * Besoin total = BEE standard × K ; les seuils exprimés par 1000 kcal restent basés sur le BEE
 * standard, comme dans l'analyse d'une consultation.
 */
data class RoleExploration(val name: String, val libelle: String, val nutrimentParDefaut: Nutrient) {
    val estEnergie: Boolean get() = this == ENERGIE
    val supplementaire: Boolean get() = name.startsWith("SUPPLEMENT_")
    val doseParDefaut: Double get() = if (this == SODIUM || supplementaire) 0.1 else 5.0

    companion object {
        val PROTEINES = RoleExploration("PROTEINES", "Protéines", NutrientMain.PROTEINE)
        val FIBRES = RoleExploration("FIBRES", "Fibres", NutrientMain.CELLULOSE)
        val CALCIUM = RoleExploration("CALCIUM", "Calcium", NutrientMacro.CAL)
        val OMEGA6 = RoleExploration("OMEGA6", "Oméga-6", NutrientLipid.O6)
        val SODIUM = RoleExploration("SODIUM", "Sel / sodium", NutrientMacro.NA)
        val ENERGIE = RoleExploration("ENERGIE", "Énergie restante", NutrientMain.ENERGIE)
        val entries = listOf(PROTEINES, FIBRES, CALCIUM, OMEGA6, SODIUM, ENERGIE)
        fun supplement(name: String = "SUPPLEMENT_" + genUUID()) =
                RoleExploration(name, "Ingrédient supplémentaire", NutrientLipid.EPADHA)
        fun ordonner(listes: Map<RoleExploration, List<AlimentEv>>): List<RoleExploration> =
                entries.filterNot { it.estEnergie } + listes.keys.filter { it.supplementaire } + ENERGIE

        /** Nutriments proposés pour l'ajustement des fibres. */
        val nutrimentsFibres: List<Nutrient> =
                listOf(
                        NutrientMain.CELLULOSE,
                        NutrientMain.FIBRETOT,
                        NutrientMain.FIBRESOL,
                        NutrientMain.NDF,
                        NutrientMain.ADF
                )
    }
}

/** Cible d'ajustement d'un rôle : niveau du référentiel × facteur. */
data class CibleExploration(
        val nutriment: Nutrient,
        val niveau: Reflevel = Reflevel.OPTIMIN,
        val facteur: Double = adjustmentNeedMultiplier(nutriment)
)

data class ConfigurationExploration(
        val references: List<ReferenceEv>,
        val poids: List<Double>,
        val coefficientsK: List<Double>,
        val listes: Map<RoleExploration, List<AlimentEv>>,
        val cibles: Map<RoleExploration, CibleExploration> =
                RoleExploration.entries.filter { !it.estEnergie }.associateWith {
                    CibleExploration(it.nutrimentParDefaut)
                },
        /** Arrondi des quantités comme l'application (contenants, 1 / 5 / 25 g). */
        val arrondir: Boolean = true,
        /** Dose minimale d'un ingrédient utilisé, par rôle (g). */
        val doseMinimale: Map<RoleExploration, Double> =
                RoleExploration.entries.associateWith { it.doseParDefaut },
        /** Pas d'arrondi manuel par liste (g) ; le conditionnement garde toujours la priorité. */
        val pasArrondiManuel: Map<RoleExploration, Double> =
                RoleExploration.entries.filter { it == RoleExploration.SODIUM }
                        .associateWith { 0.1 },
        /** Seuls les MAX limitent les apports ; les dépassements d'OPTIMAX sont tolérés. */
        val ignorerOptimax: Boolean = true,
        val maxScenarios: Int = 2000,
        val besoinsIgnores: Set<String> = emptySet(),
        val arrondirAuSuperieur: Boolean = false
) {
    val roles: List<RoleExploration> get() = rolesExplorationActifs(listes, cibles, besoinsIgnores)

    val nombreCombinaisons: Long
        get() = roles.fold(1L) { acc, role -> acc * (listes[role]?.size ?: 0) }

    val nombreScenarios: Long
        get() = nombreCombinaisons * poids.size * coefficientsK.size * references.size
}

enum class StatutScenario(val libelle: String, val rang: Int) {
    CONFORME("Conforme", 0),
    CONFORME_SOUS_RESERVE("Conforme sous réserve (données manquantes)", 1),
    SEUILS_NON_RESPECTES("Seuils non respectés", 2),
    ENERGIE_DEPASSEE("Énergie dépassée", 3),
    CIBLE_ABSENTE("Cible absente du référentiel", 4),
    NON_CALCULABLE("Non calculable", 5)
}

data class ScenarioExploration(
        val id: String,
        val combinaison: Int,
        val reference: ReferenceEv,
        val poids: Double,
        val k: Double,
        val besoinStandard: Double?,
        val besoinTotal: Double?,
        val poidsMetabolique: Double?,
        /** Ration ajustée : un AlimentRation par rôle (quantité éventuellement nulle). */
        val ration: Ration,
        val roles: List<RoleExploration>,
        val energie: Double?,
        val statut: StatutScenario,
        /** Seuils non respectés renseignés (« Nutriment MIN »...). */
        val seuilsManques: List<String> = emptyList(),
        /** Seuils non respectés uniquement à cause de valeurs absentes. */
        val seuilsNonRenseignes: List<String> = emptyList(),
        val message: String = "",
        val valeursNutritionnelles: Map<String, ValeurNutritionnelle> = emptyMap()
) {
    val ecartEnergie: Double?
        get() = if (energie != null && besoinTotal != null) energie - besoinTotal else null

    val composition: String
        get() =
                ration.alimentMutableList.filter { it.quantite > 0.0 }.joinToString(" + ") {
                    "${it.aliment?.nom ?: "?"} : ${formaterQuantite(it.quantite)} g"
                }
}

enum class ZoneEquilibre(val libelle: String) {
    EQUILIBRABLE("Équilibrable"),
    SOUS_RESERVE("Sous réserve : seuls des nutriments non renseignés échouent"),
    SEUILS_NON_RESPECTES("Non équilibrable : seuils renseignés non respectés"),
    ENERGIE_DEPASSEE("Non équilibrable : énergie déjà dépassée"),
    NON_EVALUABLE("Non évaluable (cible ou composition absente)")
}

/** Une case de la carte : référentiel × poids × K, toutes combinaisons confondues. */
data class CaseEquilibre(
        val reference: ReferenceEv,
        val poids: Double,
        val k: Double,
        val zone: ZoneEquilibre,
        val combinaisons: Int,
        val conformes: Int,
        val sousReserve: Int,
        /** Nombre minimal de seuils renseignés manqués par une combinaison évaluable. */
        val minSeuilsManques: Int?,
        /** Seuils renseignés les plus souvent manqués dans la case. */
        val seuilsLimitants: String,
        /** Scénarios de la case, du meilleur au moins bon. */
        val scenarios: List<ScenarioExploration>
) {
    val meilleurScenario: ScenarioExploration?
        get() = scenarios.firstOrNull()
}

data class ResultatExploration(
        val configuration: ConfigurationExploration,
        val scenarios: List<ScenarioExploration>,
        val cases: List<CaseEquilibre>
)

class ExplorationException(message: String) : Exception(message)

/** Pas d'arrondi au ½ contenant (dosette, sachet, boîte), ou null si l'aliment n'en a pas. */
fun pasConditionnement(aliment: AlimentEv): Double? {
    val cont = aliment.cont
    val quantInt = aliment.quantInt
    return if (cont != null && cont != ContEnum.NO && quantInt != null && quantInt > 0.0)
            quantInt / 2.0
    else null
}

/** Pas d'arrondi utilisé par arrondirQuantiteSelonRegles pour une quantité donnée. */
fun pasArrondi(aliment: AlimentEv, quantite: Double): Double =
        pasConditionnement(aliment)
                ?: when {
                    quantite < 20.0 -> 1.0
                    quantite < 200.0 -> 5.0
                    else -> 25.0
                }

/** Dose minimale effective : premier multiple du ½ contenant atteignant la dose demandée. */
fun doseMinimaleEffective(aliment: AlimentEv, doseMinimale: Double): Double {
    if (doseMinimale <= 0.0) return 0.0
    val pas = pasConditionnement(aliment) ?: return doseMinimale
    return ceil(doseMinimale / pas - 1e-9) * pas
}

/**
 * Arrondit comme l'application (arrondirQuantiteSelonRegles), avec une dose minimale : sous cette
 * dose, l'ingrédient n'est pas utilisé (moins de la moitié) ou est porté à cette dose.
 */
fun arrondirAvecDoseMinimale(
        aliment: AlimentEv,
        quantite: Double,
        doseMinimale: Double,
        fin: Boolean = false,
        auSuperieur: Boolean = false,
        pasManuel: Double? = null
): Double {
    if (quantite <= 0.0) return 0.0
    val minimum = doseMinimaleEffective(aliment, doseMinimale)
    if (auSuperieur) {
        val cible = maxOf(quantite, minimum)
        val pas = pasConditionnement(aliment) ?: pasManuel?.takeIf { it > 0.0 }
                ?: if (fin) 0.1 else pasArrondi(aliment, cible)
        return ceil(cible / pas - 1e-9) * pas
    }
    if (quantite < minimum) return if (quantite >= minimum / 2.0) minimum else 0.0
    val pas = pasConditionnement(aliment) ?: pasManuel?.takeIf { it > 0.0 }
            ?: if (fin) 0.1 else null
    if (pas != null) return maxOf(minimum, kotlin.math.round(quantite / pas) * pas)
    return arrondirQuantiteSelonRegles(AlimentRation(aliment = aliment), quantite)
}

fun formaterQuantite(quantite: Double): String {
    val arrondi = kotlin.math.round(quantite * 10.0) / 10.0
    return if (arrondi == kotlin.math.floor(arrondi)) arrondi.toLong().toString() else arrondi.toString()
}

/** Séquence inclusive de bornes (le maximum est toujours inclus). */
fun intervalleExploration(debut: Double, fin: Double, pas: Double): List<Double> {
    if (!debut.isFinite() || !fin.isFinite() || !pas.isFinite() || debut <= 0.0 || fin < debut || pas <= 0.0)
        throw ExplorationException("Intervalle invalide : bornes positives, maximum ≥ minimum, pas positif")
    if ((fin - debut) / pas > 10_000) throw ExplorationException("Intervalle trop fin")
    val valeurs = mutableListOf<Double>()
    var i = 0
    while (true) {
        val v = debut + i * pas
        if (v > fin + 1e-9 * maxOf(1.0, fin)) break
        valeurs += kotlin.math.round(v * 1e6) / 1e6
        i++
    }
    if (valeurs.last() < fin - 1e-9 * maxOf(1.0, fin)) valeurs += fin
    return valeurs.distinct()
}

class ExplorateurMultiration(private val equationRepository: EquationRepository) {

    // Teneurs pour 100 g et énergie pour 100 g, par (aliment, référentiel, nutriment).
    private val cacheTeneurs = mutableMapOf<Triple<String, String, String>, Double>()

    private suspend fun teneur(aliment: AlimentEv, reference: ReferenceEv, nutriment: Nutrient): Double {
        val cle = Triple(aliment.uuid, reference.uuid, nutriment.label)
        cacheTeneurs[cle]?.let { return it }
        val pour100g = AlimentRation(aliment = aliment, quantite = 100.0)
        val valeur =
                if (nutriment == NutrientMain.ENERGIE) {
                    pour100g.getEnergie(reference, equationRepository)
                } else {
                    // Valeur absente = 0, comme l'ajustement multi-nutriments de l'application
                    pour100g.getNutrientWithComplementary(nutriment, equationRepository, reference) ?: 0.0
                }
        cacheTeneurs[cle] = valeur
        return valeur
    }

    /**
     * Calcule toutes les rations. [progression] reçoit (scénarios traités, total). Lance
     * [ExplorationException] si la configuration est incomplète ou dépasse la limite.
     */
    suspend fun explorer(
            config: ConfigurationExploration,
            progression: (Int, Int) -> Unit = { _, _ -> }
    ): ResultatExploration {
        if (config.references.isEmpty()) throw ExplorationException("Choisir au moins un référentiel")
        if (config.poids.isEmpty() || config.coefficientsK.isEmpty())
            throw ExplorationException("Grille de poids ou de K vide")
        val vides = config.roles.filter { config.listes[it].isNullOrEmpty() }
        if (vides.isNotEmpty())
            throw ExplorationException("Listes vides : ${vides.joinToString { it.libelle }}")
        val total = config.nombreScenarios
        if (total > config.maxScenarios)
            throw ExplorationException(
                    "$total scénarios dépassent la limite de ${config.maxScenarios} : réduire les listes ou la grille"
            )

        val roles = config.roles
        val tailles = roles.map { config.listes.getValue(it).size }
        val combinaisons = config.nombreCombinaisons.toInt()
        val scenarios = mutableListOf<ScenarioExploration>()
        var index = 0
        for (reference in config.references) {
            val nutrimentsEvalues = nutrimentsReference(reference)
            for (k in config.coefficientsK) for (poids in config.poids) {
                for (c in 0 until combinaisons) {
                    // Décodage de l'indice de combinaison : un aliment par liste
                    var reste = c
                    val selection =
                            roles.mapIndexed { i, role ->
                                val aliment = config.listes.getValue(role)[reste % tailles[i]]
                                reste /= tailles[i]
                                aliment
                            }
                    index++
                    scenarios +=
                            calculerScenario(
                                    id = "S" + index.toString().padStart(6, '0'),
                                    combinaison = c + 1,
                                    reference = reference,
                                    poids = poids,
                                    k = k,
                                    selection = selection,
                                    config = config,
                                    nutrimentsEvalues = nutrimentsEvalues
                            )
                    progression(index, total.toInt())
                    yield()
                }
            }
        }
        return ResultatExploration(config, scenarios, construireCases(scenarios))
    }

    /** Nutriments ayant au moins un seuil dans le référentiel (hors énergie). */
    private fun nutrimentsReference(reference: ReferenceEv): List<Nutrient> =
            (reference.getRefMapMin().keys + reference.getRefMapOMin().keys +
                            reference.getRefMapMax().keys + reference.getRefMapOMax().keys)
                    .filter { it != NutrientMain.ENERGIE }
                    .distinctBy { it.label }

    private suspend fun calculerScenario(
            id: String,
            combinaison: Int,
            reference: ReferenceEv,
            poids: Double,
            k: Double,
            selection: List<AlimentEv>,
            config: ConfigurationExploration,
            nutrimentsEvalues: List<Nutrient>
    ): ScenarioExploration {
        val roles = config.roles
        val quantites = DoubleArray(roles.size)
        val rationId = genUUID()
        fun ration() =
                Ration(
                        uuid = rationId,
                        name = "Exploration $id",
                        espece = reference.espece.label,
                        alimentMutableList =
                                roles.indices
                                        .map { i ->
                                            AlimentRation(
                                                    uuid = genUUID(),
                                                    aliment = selection[i],
                                                    quantite = quantites[i],
                                                    refRation = rationId,
                                                    refAlimUnif = selection[i].uuid
                                            )
                                        }
                                        .toMutableList()
                )

        val poidsMetabolique = CalculMetabolique.poidsMetabolique(poids, reference)
        val besoinStandard = CalculMetabolique.besoinEnergetiqueStandard(poids, reference)
        fun resultat(statut: StatutScenario, message: String, energie: Double? = null) =
                ScenarioExploration(
                        id, combinaison, reference, poids, k, besoinStandard,
                        besoinStandard?.let { it * k }, poidsMetabolique, ration(), roles, energie,
                        statut, message = message
                )
        if (besoinStandard == null || besoinStandard <= 0.0)
                return resultat(StatutScenario.NON_CALCULABLE, "Équation BEE absente ou invalide dans le référentiel")
        val besoinTotal = besoinStandard * k

        // Ajustements successifs : chaque rôle couvre le manque de son nutriment cible
        for ((i, role) in roles.withIndex()) {
            val aliment = selection[i]
            val (nutriment, cibleAbsolue) =
                    if (role.estEnergie) {
                        NutrientMain.ENERGIE to besoinTotal
                    } else {
                        val cible = config.cibles[role] ?: CibleExploration(role.nutrimentParDefaut)
                        if (!reference.contientNutriment(cible.nutriment, cible.niveau))
                                return resultat(
                                        StatutScenario.CIBLE_ABSENTE,
                                        "Pas de seuil ${cible.niveau.name} pour ${nomTraduitNutriment(cible.nutriment)} dans ${reference.nom}"
                                )
                        val besoin =
                                calculerBesoinAbsolu(
                                        reference.obtenirNutriment(cible.nutriment, cible.niveau),
                                        UnitReqEnum.getById(reference.obtenirUniteNutriment(cible.nutriment, cible.niveau)),
                                        besoinStandard,
                                        poids,
                                        poidsMetabolique
                                )
                                        ?: return resultat(
                                                StatutScenario.CIBLE_ABSENTE,
                                                "Seuil de ${nomTraduitNutriment(cible.nutriment)} non convertible (ratio ou poids métabolique absent)"
                                        )
                        cible.nutriment to besoin * cible.facteur
                    }
            var apport = 0.0
            for (j in 0 until i) if (quantites[j] > 0.0) apport += quantites[j] * teneur(selection[j], reference, nutriment) / 100.0
            val manque = cibleAbsolue - apport
            if (manque > 1e-8 * maxOf(1.0, cibleAbsolue)) {
                val densite = teneur(aliment, reference, nutriment)
                if (densite <= 0.0)
                        return resultat(
                                StatutScenario.NON_CALCULABLE,
                                "${aliment.nom ?: aliment.uuid} ne contient pas de ${nomTraduitNutriment(nutriment)}"
                        )
                quantites[i] = manque * 100.0 / densite
                if (config.arrondir)
                        quantites[i] = arrondirAvecDoseMinimale(
                                aliment, quantites[i], config.doseMinimale[role] ?: role.doseParDefaut,
                                fin = role == RoleExploration.SODIUM || role.supplementaire,
                                auSuperieur = config.arrondirAuSuperieur,
                                pasManuel = config.pasArrondiManuel[role]
                        )
            }
        }

        return reevaluerScenario(
                resultat(StatutScenario.NON_CALCULABLE, ""), ration(), config
        )
    }

    /** Réévalue les quantités éditées sans les réajuster. */
    suspend fun reevaluerScenario(
            scenario: ScenarioExploration,
            rationFinale: Ration,
            config: ConfigurationExploration
    ): ScenarioExploration {
        val reference = scenario.reference
        val poids = scenario.poids
        val k = scenario.k
        val besoinStandard = scenario.besoinStandard
        val besoinTotal = scenario.besoinTotal
        val poidsMetabolique = scenario.poidsMetabolique
        if (besoinStandard == null || besoinTotal == null || besoinStandard <= 0.0) {
            return scenario.copy(ration = rationFinale, statut = StatutScenario.NON_CALCULABLE)
        }
        val nutrimentsEvalues = nutrimentsReference(reference)
        // Repartir de la combinaison calculée : une suppression puis un réajout ne doit
        // pas changer le rôle d'un ingrédient entre l'enregistrement et le rechargement.
        var combinaisonRestante = scenario.combinaison - 1
        val rolesRestants = config.roles.map { role ->
            val liste = config.listes.getValue(role)
            val aliment = liste[combinaisonRestante % liste.size]
            combinaisonRestante /= liste.size
            role to aliment.uuid
        }.toMutableList()
        val roles = rationFinale.alimentMutableList.mapIndexed { i, ligne ->
            val index = rolesRestants.indexOfFirst { it.second == ligne.aliment?.uuid }
            if (index >= 0) rolesRestants.removeAt(index).first
            else RoleExploration.supplement("SUPPLEMENT_EDIT_" + (ligne.aliment?.uuid ?: "inconnu") + "_" + i)
        }
        val labels = nutrimentsEvalues.map { it.label } + NutrientMain.ENERGIE.label
        // Seuls les aliments utilisés entrent dans l'analyse : un aliment à 0 g ne doit pas
        // rendre un nutriment « incomplet »
        val rationAnalysee =
                rationFinale.copy(alimentMutableList = rationFinale.alimentMutableList.filter { it.quantite > 0.0 }.toMutableList())
        val valeurs = analyserValeursNutritionnellesRationSelective(rationAnalysee, labels, equationRepository, reference)
        val energie = valeurs[NutrientMain.ENERGIE.label]?.valeur ?: 0.0

        // Énergie acceptée à ± ½ pas d'arrondi de l'ingrédient énergétique
        val iEnergie = roles.indexOf(RoleExploration.ENERGIE)
        val ligneEnergie = rationFinale.alimentMutableList.getOrNull(iEnergie)
        val alimentEnergie = ligneEnergie?.aliment
        val tolerance =
                if (!config.arrondir || alimentEnergie == null) 1e-8 * maxOf(1.0, besoinTotal)
                else {
                    val q = ligneEnergie.quantite
                    val minimum = doseMinimaleEffective(alimentEnergie, config.doseMinimale[RoleExploration.ENERGIE] ?: 0.0)
                    val pasNormal = pasConditionnement(alimentEnergie)
                            ?: config.pasArrondiManuel[RoleExploration.ENERGIE]?.takeIf { it > 0.0 }
                            ?: pasArrondi(alimentEnergie, q)
                    val pas = maxOf(pasNormal, if (q <= minimum) minimum else 0.0)
                    (if (config.arrondirAuSuperieur) 1.0 else 0.5) * pas * teneur(alimentEnergie, reference, NutrientMain.ENERGIE) / 100.0
                }

        // Conformité : mêmes règles que l'écran d'analyse (maladie exclue)
        val manques = mutableListOf<String>()
        val nonRenseignes = mutableListOf<String>()
        for (nutriment in nutrimentsEvalues) {
            if (nutriment.label in config.besoinsIgnores) continue
            val valeur = valeurs[nutriment.label] ?: continue
            val conformite =
                    calculerConformite(
                            valeur, reference, besoinStandard, poids, poidsMetabolique,
                            besoinEnergetiqueCible = besoinTotal
                    )
                            ?: continue
            if (config.ignorerOptimax && conformite.status == ConformiteStatus.EXCES && !conformite.isCritical) continue
            val niveau =
                    when (conformite.status) {
                        ConformiteStatus.CARENCE -> if (conformite.isCritical) "MIN" else "OPTIMIN"
                        else -> if (conformite.isCritical) "MAX" else "OPTIMAX"
                    }
            val libelle = "${nomTraduitNutriment(nutriment)} $niveau"
            if (valeur.complete) manques += libelle else nonRenseignes += libelle
        }

        val statut =
                when {
                    energie - besoinTotal > tolerance -> StatutScenario.ENERGIE_DEPASSEE
                    manques.isNotEmpty() -> StatutScenario.SEUILS_NON_RESPECTES
                    nonRenseignes.isNotEmpty() -> StatutScenario.CONFORME_SOUS_RESERVE
                    else -> StatutScenario.CONFORME
                }
        val message =
                if (statut == StatutScenario.ENERGIE_DEPASSEE) {
                    if ((ligneEnergie?.quantite ?: 0.0) > 0.0) "Énergie au-dessus du besoin au-delà de la tolérance d'arrondi."
                    else "Les ingrédients des autres ajustements dépassent déjà le besoin énergétique."
                } else ""
        return ScenarioExploration(
                scenario.id, scenario.combinaison, reference, poids, k, besoinStandard, besoinTotal, poidsMetabolique,
                rationFinale, roles, energie, statut, manques, nonRenseignes, message, valeurs
        )
    }

    companion object {
        /** Ordre « du meilleur au moins bon » : statut, seuils manqués, écart énergétique. */
        val ordreScenarios: Comparator<ScenarioExploration> =
                compareBy<ScenarioExploration> { it.statut.rang }
                        .thenBy { it.seuilsManques.size }
                        .thenBy { abs(it.ecartEnergie ?: Double.MAX_VALUE) }

        /** Regroupe les scénarios par case référentiel × poids × K et en déduit la zone. */
        fun construireCases(scenarios: List<ScenarioExploration>): List<CaseEquilibre> =
                scenarios
                        .groupBy { Triple(it.reference.uuid, it.poids, it.k) }
                        .values
                        .map { groupe ->
                            val tries = groupe.sortedWith(ordreScenarios)
                            val conformes = groupe.count { it.statut == StatutScenario.CONFORME }
                            val sousReserve = groupe.count { it.statut == StatutScenario.CONFORME_SOUS_RESERVE }
                            val seuils = groupe.count { it.statut == StatutScenario.SEUILS_NON_RESPECTES }
                            val energie = groupe.count { it.statut == StatutScenario.ENERGIE_DEPASSEE }
                            val nonEvaluables = groupe.size - conformes - sousReserve - seuils - energie
                            // Sans ration conforme, l'échec dominant nomme la zone (égalité : seuils, énergie)
                            val zone =
                                    when {
                                        // Une ration dont tous les seuils renseignés sont respectés est
                                        // équilibrée. Des valeurs inconnues restent signalées au détail,
                                        // mais ne doivent pas faire passer la case au jaune.
                                        conformes + sousReserve > 0 -> ZoneEquilibre.EQUILIBRABLE
                                        seuils >= energie && seuils >= nonEvaluables -> ZoneEquilibre.SEUILS_NON_RESPECTES
                                        energie >= nonEvaluables -> ZoneEquilibre.ENERGIE_DEPASSEE
                                        else -> ZoneEquilibre.NON_EVALUABLE
                                    }
                            val evaluables =
                                    groupe.filter {
                                        it.statut != StatutScenario.CIBLE_ABSENTE && it.statut != StatutScenario.NON_CALCULABLE
                                    }
                            val limitants =
                                    groupe.flatMap { it.seuilsManques }
                                            .groupingBy { it }
                                            .eachCount()
                                            .entries
                                            .sortedByDescending { it.value }
                                            .take(3)
                                            .joinToString(", ") { "${it.key} (${it.value}/${groupe.size})" }
                            val premier = groupe.first()
                            CaseEquilibre(
                                    reference = premier.reference,
                                    poids = premier.poids,
                                    k = premier.k,
                                    zone = zone,
                                    combinaisons = groupe.size,
                                    conformes = conformes,
                                    sousReserve = sousReserve,
                                    minSeuilsManques = evaluables.minOfOrNull { it.seuilsManques.size },
                                    seuilsLimitants = limitants,
                                    scenarios = tries
                            )
                        }
                        .sortedWith(compareBy({ it.reference.nom }, { it.poids }, { it.k }))
    }
}

/** Seuil dans la même unité absolue que l'apport ; les ratios restent sans conversion. */
fun seuilCourbeExploration(scenario: ScenarioExploration, nutriment: Nutrient, niveau: Reflevel): Double? {
    val reference = scenario.reference
    if (!reference.contientNutriment(nutriment, niveau)) return null
    val valeur = reference.obtenirNutriment(nutriment, niveau)
    val unite = UnitReqEnum.getById(reference.obtenirUniteNutriment(nutriment, niveau))
    return (if (unite == UnitReqEnum.RATIO) valeur else calculerBesoinAbsolu(
            valeur, unite, scenario.besoinStandard, scenario.poids, scenario.poidsMetabolique
    ))?.takeIf { it.isFinite() }
}

/** Valeur tracée dans les courbes : l'apport connu est utile même si incomplet. */
fun apportCourbeExploration(scenario: ScenarioExploration, nutriment: Nutrient): Double? =
        scenario.valeursNutritionnelles[nutriment.label]?.valeur

/** Un besoin ignoré ne déclenche ni ajustement ni obligation de remplir sa liste. */
fun rolesExplorationActifs(
        listes: Map<RoleExploration, List<AlimentEv>>,
        cibles: Map<RoleExploration, CibleExploration>,
        besoinsIgnores: Set<String>
): List<RoleExploration> = RoleExploration.ordonner(listes).filter {
    it.estEnergie || (cibles[it]?.nutriment ?: it.nutrimentParDefaut).label !in besoinsIgnores
}

/** Copie éditable indépendante du résultat, sans lien vers une consultation. */
fun copierRationExploration(ration: Ration): Ration {
    val id = genUUID()
    return ration.copy(
            uuid = id, idConsult = "", refRationParente = null,
            suppVarp = ration.suppVarp.toMutableList(),
            alimentMutableList = ration.alimentMutableList.filter { it.quantite > 0.0 }
                    .map { it.copy(uuid = genUUID(), refRation = id) }.toMutableList()
    )
}
