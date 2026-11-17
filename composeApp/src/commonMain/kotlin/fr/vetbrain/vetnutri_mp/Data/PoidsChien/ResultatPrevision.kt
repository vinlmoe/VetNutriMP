package fr.vetbrain.vetnutri_mp.Data.PoidsChien

import kotlinx.serialization.Serializable
import kotlin.math.ceil

/** Ration constante proposée à partir de la dernière consultation : une proposition, jamais appliquée. */
data class PropositionRation(
        val kcalParJour: Double,
        val jourDebut: Int,
        val jourCible: Int,
        val poidsCible: Double,
        val plancherKcal: Double,
        /** Nutriments essentiels non couverts (seuils MIN du référentiel), remplis par le contrôle d'apports. */
        val carences: List<String> = emptyList()
) {
    val sousPlancher: Boolean get() = kcalParJour < plancherKcal
    val bloquee: Boolean get() = sousPlancher || carences.isNotEmpty()
}

/** Intervalle de semaines (depuis la dernière consultation) où le poids visé est atteint ; null = au-delà de l'horizon. */
data class SemainesAtteinte(val auPlusTot: Int?, val auPlusTard: Int?)

data class ResultatPrevision(
        val entrees: EntreesPrevision,
        val avertissements: List<Avertissement>,
        /** Non null : estimation du besoin désactivée et pourquoi. */
        val estimationDesactivee: String?,
        /** Poids prévu (moyenne et intervalle à 95 %) par jour pair, de 0 à l'horizon. */
        val bande: List<Pair<Int, Intervalle>>,
        val mApparent: Intervalle,
        val besoinApparent: Intervalle,
        val peseesUtilisees: List<Pesee>,
        /** Pesées hors de l'intervalle prévu par les pesées précédentes (vérifier l'observance). */
        val peseesHorsIntervalle: List<Pesee>,
        /** Null sans pesée utilisable ou quand l'estimation est désactivée. */
        val proposition: PropositionRation?,
        val niveauxDeficit: List<NiveauDeficit>,
        /** Perte de poids seulement. */
        val atteinte: SemainesAtteinte?
)

/** Calcul complet de la prévision à partir d'entrées prêtes (pur, sans base de données). */
object CalculPrevision {

    /** Délai par défaut de l'objectif de perte de poids, semaines après la dernière consultation. */
    const val SEMAINES_CIBLE_PAR_DEFAUT = 26

    fun calculer(pret: PreparationPrevision.Pret, reglages: ReglagesPrevisionPoids, semainesCible: Int): ResultatPrevision {
        val e = pret.entrees
        val horizon = ConstantesChienV2.HORIZON_MAX_JOURS
        val ajustement = ModelePoidsChienV2.ajuster(e.chien, e.rations, e.pesees, horizon)
        val bande = (0..horizon step ConstantesChienV2.PAS_JOURS).map { it to ajustement.poidsPrevu(it) }
        val utilisees = ajustement.peseesUtilisees

        // Une pesée hors de l'intervalle prévu par les pesées précédentes
        val horsIntervalle = utilisees.filterIndexed { i, p ->
            val precedent = ModelePoidsChienV2.ajuster(e.chien, e.rations, utilisees.take(i), horizon)
            !precedent.poidsPrevu(p.jour).contient(p.poids)
        }

        val estimation = pret.estimationDesactivee == null
        val proposition =
                if (!estimation || utilisees.isEmpty()) null
                else {
                    val jourCible = when (e.mode) {
                        ModePrevision.ENTRETIEN -> e.jourConsultation + ConstantesChienV2.DELAI_ENTRETIEN_JOURS
                        ModePrevision.PERTE_DE_POIDS -> e.jourConsultation + 7 * semainesCible
                    }.coerceAtMost(horizon)
                    PropositionRation(
                            kcalParJour = ModelePoidsChienV2.rationPourCible(ajustement, e.jourConsultation, jourCible, e.poidsVise),
                            jourDebut = e.jourConsultation,
                            jourCible = jourCible,
                            poidsCible = e.poidsVise,
                            plancherKcal = PreparateurPrevision.plancherKcal(reglages, e.poidsVise)
                    )
                }

        val atteinte =
                if (e.mode != ModePrevision.PERTE_DE_POIDS) null
                else {
                    val futur = bande.filter { it.first >= e.jourConsultation }
                    fun semaines(jour: Int?) = jour?.let { ceil((it - e.jourConsultation) / 7.0).toInt() }
                    SemainesAtteinte(
                            auPlusTot = semaines(futur.firstOrNull { it.second.bas <= e.poidsVise }?.first),
                            auPlusTard = semaines(futur.firstOrNull { it.second.haut <= e.poidsVise }?.first)
                    )
                }

        return ResultatPrevision(
                entrees = e,
                avertissements = pret.avertissements,
                estimationDesactivee = pret.estimationDesactivee,
                bande = bande,
                mApparent = ajustement.mApparent,
                besoinApparent = ajustement.besoinApparent,
                peseesUtilisees = utilisees,
                peseesHorsIntervalle = horsIntervalle,
                proposition = proposition,
                niveauxDeficit =
                        e.besoinSansDeficit?.let { ModelePoidsChienV2.niveauxDeficit(e.chien, it) }
                                ?: ModelePoidsChienV2.niveauxDeficit(e.chien),
                atteinte = atteinte
        )
    }
}

// --- Journal des calculs (traçabilité) ------------------------------------------------------

@Serializable
data class EntreesJournal(
        val debut: String,
        val poidsInitial: Double,
        val poidsIdeal: Double,
        val poidsVise: Double,
        val mode: String,
        val proteines: Double,
        val lipides: Double,
        val proteinesAnterieures: Double?,
        val lipidesAnterieurs: Double?,
        val rations: List<List<Double>>,
        val pesees: List<List<Double>>,
        val jourConsultation: Int,
        val besoinSansDeficit: Double?,
        val semainesCible: Int,
        val plancherKcalParKgMetabolique: Double,
        val avertissements: List<String>,
        val estimationDesactivee: String?
)

@Serializable
data class ResultatsJournal(
        val mApparent: List<Double>,
        val besoinApparent: List<Double>,
        val peseesUtilisees: Int,
        val peseesHorsIntervalle: List<Int>,
        /** Poids prévu toutes les deux semaines : jour, moyenne, bas, haut. */
        val prevision: List<List<Double>>,
        val rationProposee: Double?,
        val rationBloquee: Boolean?,
        val carences: List<String>
)

fun ResultatPrevision.entreesJournal(semainesCible: Int, reglages: ReglagesPrevisionPoids) = EntreesJournal(
        debut = entrees.debut.toString(),
        poidsInitial = entrees.chien.poidsInitial,
        poidsIdeal = entrees.chien.poidsIdeal,
        poidsVise = entrees.poidsVise,
        mode = entrees.mode.name,
        proteines = entrees.chien.composition.proteines,
        lipides = entrees.chien.composition.lipides,
        proteinesAnterieures = entrees.chien.compositionAnterieure?.proteines,
        lipidesAnterieurs = entrees.chien.compositionAnterieure?.lipides,
        rations = entrees.rations.map { listOf(it.debutJour.toDouble(), it.kcalParJour) },
        pesees = entrees.pesees.map { listOf(it.jour.toDouble(), it.poids) },
        jourConsultation = entrees.jourConsultation,
        besoinSansDeficit = entrees.besoinSansDeficit,
        semainesCible = semainesCible,
        plancherKcalParKgMetabolique = reglages.plancherKcalParKgMetabolique,
        avertissements = avertissements.map { it.name },
        estimationDesactivee = estimationDesactivee
)

fun ResultatPrevision.resultatsJournal() = ResultatsJournal(
        mApparent = listOf(mApparent.moyenne, mApparent.bas, mApparent.haut),
        besoinApparent = listOf(besoinApparent.moyenne, besoinApparent.bas, besoinApparent.haut),
        peseesUtilisees = peseesUtilisees.size,
        peseesHorsIntervalle = peseesHorsIntervalle.map { it.jour },
        prevision = bande.filter { it.first % 14 == 0 }.map { (j, i) -> listOf(j.toDouble(), i.moyenne, i.bas, i.haut) },
        rationProposee = proposition?.kcalParJour,
        rationBloquee = proposition?.bloquee,
        carences = proposition?.carences.orEmpty()
)
