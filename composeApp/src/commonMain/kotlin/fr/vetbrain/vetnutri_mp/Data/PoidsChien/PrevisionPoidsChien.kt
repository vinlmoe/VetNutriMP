package fr.vetbrain.vetnutri_mp.Data.PoidsChien

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.CalculMetabolique
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.serialization.Serializable
import kotlin.math.pow

/** État du module de prévision du poids (drapeau de fonctionnalité). */
@Serializable
enum class EtatModulePrevision {
    /** Aucun calcul. */
    OFF,
    /** Calculs faits et enregistrés, sans être montrés. */
    SILENCIEUX,
    /** Calculs affichés sur la courbe de poids. */
    ACTIF
}

/** Réglages du module, modifiables par le clinicien. */
@Serializable
data class ReglagesPrevisionPoids(
        val etat: EtatModulePrevision = EtatModulePrevision.OFF,
        /** Plancher d'apport : kcal EM par kg^0,75 de poids visé et par jour. */
        val plancherKcalParKgMetabolique: Double = PLANCHER_PAR_DEFAUT
) {
    val calculer: Boolean get() = etat != EtatModulePrevision.OFF
    val afficher: Boolean get() = etat == EtatModulePrevision.ACTIF

    companion object {
        const val PLANCHER_PAR_DEFAUT = 55.0
    }
}

/**
 * Rations retenues pour une consultation : [actuelles] = ration réellement donnée avant la
 * consultation (le passé), [proposees] = ration donnée à partir de la consultation (le futur du
 * modèle). Plusieurs rations sont additionnées. [debutSuivi] marque la consultation qui ouvre le suivi.
 */
@Serializable
data class SelectionRationsPrevision(
        val actuelles: List<String> = emptyList(),
        val proposees: List<String> = emptyList(),
        val debutSuivi: Boolean = false
) {
    companion object {
        /**
         * Sans choix enregistré : rations actuelles ; rations proposées de l'ordonnance, sinon la
         * dernière ration proposée de la consultation (plusieurs propositions : voir [plusieursProposeesSansChoix]).
         */
        fun parDefaut(c: ConsultationEv): SelectionRationsPrevision {
            val principales = c.rations.filter { it.refRationParente == null }
            val actuelles = principales.filter { it.actual }.map { it.uuid }
            val proposees = principales.filter { !it.actual }.map { it.uuid }
            val ordonnance = c.prescriptionSelectedRationIds.filter { it in proposees }
            return SelectionRationsPrevision(actuelles = actuelles, proposees = ordonnance.ifEmpty { proposees.takeLast(1) })
        }

        /** Choix par défaut ambigu : plusieurs rations proposées, aucune dans l'ordonnance ni cochée. */
        fun plusieursProposeesSansChoix(c: ConsultationEv): Boolean {
            if (c.selectionPrevisionPoids != null) return false
            val proposees = c.rations.filter { it.refRationParente == null && !it.actual }.map { it.uuid }
            return proposees.size > 1 && c.prescriptionSelectedRationIds.none { it in proposees }
        }
    }
}

/** Sélection enregistrée de la consultation, ou celle par défaut. */
fun ConsultationEv.selectionPrevision(): SelectionRationsPrevision =
        selectionPrevisionPoids ?: SelectionRationsPrevision.parDefaut(this)

/** Apport d'une ration par jour : énergie (kcal EM) et grammes de protéines et de lipides (null = inconnu). */
data class ApportRation(val kcal: Double, val proteinesG: Double?, val lipidesG: Double?)

/** Raisons d'un refus du calcul (message affiché par l'interface). */
enum class MotifRefus {
    ESPECE, TROUPEAU, AUCUNE_CONSULTATION, REFERENTIEL_ABSENT, STADE_NON_ADULTE, MALADIE, GESTATION_LACTATION,
    POIDS_INITIAL_ABSENT, PRISE_DE_POIDS, DONNEES_NON_PLAUSIBLES, SUIVI_TROP_LONG, RATION_ABSENTE
}

/** Avertissements affichés avec la prévision. */
enum class Avertissement {
    /** Poids initial entre 95 et 100 % du poids idéal : prise de poids hors du domaine calé. */
    EXTRAPOLATION_NON_VALIDEE,
    /** Composition de la ration inconnue : protéines et lipides par défaut. */
    COMPOSITION_PAR_DEFAUT,
    /** Pas de ration proposée à la dernière consultation : la ration actuelle est supposée poursuivie. */
    RATION_ACTUELLE_POURSUIVIE,
    /** Poids idéal non saisi : le poids actuel est pris comme poids idéal. */
    POIDS_IDEAL_NON_SAISI,
    /** Besoin standard du référentiel non calculable : niveaux de déficit rapportés à EI0. */
    BESOIN_STANDARD_INDISPONIBLE,
    /** Plusieurs rations proposées sans choix : la dernière ajoutée est retenue. */
    DERNIERE_RATION_PROPOSEE_RETENUE
}

enum class ModePrevision { ENTRETIEN, PERTE_DE_POIDS }

/** Entrées du modèle, construites à partir de la fiche. Les jours comptent depuis [debut]. */
data class EntreesPrevision(
        val debut: LocalDate,
        val chien: ChienSimule,
        /** Rations réellement données puis ration future (à partir de [jourConsultation]). */
        val rations: List<PeriodeRation>,
        val pesees: List<Pesee>,
        /** Jour de la dernière consultation : début de la ration proposée. */
        val jourConsultation: Int,
        /** Poids visé, kg (poids idéal de la dernière consultation). */
        val poidsVise: Double,
        val mode: ModePrevision,
        /** Ration future : rations de la dernière consultation additionnées. */
        val rationsFutures: List<String>,
        /** Noms des rations futures, pour l'affichage. */
        val nomsRationsFutures: String = "",
        /**
         * Besoin sans déficit, kcal/j : besoin standard × K1, K2, K3, K5 et coefficient d'ajustement
         * de la consultation de début (K4, l'objectif de perte ou de gain de poids, exclu) ; base
         * 100 % des niveaux de déficit. Null s'il n'est pas calculable (EI0 à la place).
         */
        val besoinSansDeficit: Double? = null
)

sealed class PreparationPrevision {
    data class Refus(val motifs: List<MotifRefus>, val detail: String = "") : PreparationPrevision()
    data class Pret(
            val entrees: EntreesPrevision,
            val avertissements: List<Avertissement>,
            /** Non null : estimation du besoin désactivée (date ou ration manquante), avec la raison. */
            val estimationDesactivee: String?
    ) : PreparationPrevision()
}

/**
 * Prépare les entrées du modèle « chien-v2 » à partir de l'animal. Refuse (espèce, troupeau,
 * référentiel non adulte, maladie, gestation ou lactation, prise de poids, données non plausibles).
 * [references] donne le référentiel général de chaque consultation (par identifiant de consultation) ;
 * [apports] l'apport de chaque ration (par identifiant de ration).
 */
object PreparateurPrevision {

    private const val POIDS_MIN = 1.0
    private const val POIDS_MAX = 100.0
    private const val KCAL_MIN = 20.0
    private const val KCAL_MAX = 8000.0

    fun preparer(
            animal: AnimalEv,
            references: Map<String, ReferenceEv?>,
            apports: Map<String, ApportRation>
    ): PreparationPrevision {
        if (animal.estTroupeau) return PreparationPrevision.Refus(listOf(MotifRefus.TROUPEAU))
        if (animal.getEspece() != Espece.CHIEN) return PreparationPrevision.Refus(listOf(MotifRefus.ESPECE))
        val datees = animal.consultations.filter { it.date != null }.sortedBy { it.date }
        if (datees.isEmpty()) return PreparationPrevision.Refus(listOf(MotifRefus.AUCUNE_CONSULTATION))
        val suivi = consultationsDuSuivi(datees)

        val premiere = preparerSuivi(animal, suivi, references, apports, estimationDesactivee = null)
        // Une ration passée manque : estimation désactivée, prévision depuis la dernière consultation
        if (premiere is RationPasseeManquante) {
            val derniere = listOf(suivi.last())
            return when (val r = preparerSuivi(animal, derniere, references, apports, premiere.raison)) {
                is PreparationPrevision -> r
                is RationPasseeManquante -> PreparationPrevision.Refus(listOf(MotifRefus.RATION_ABSENTE))
                else -> PreparationPrevision.Refus(listOf(MotifRefus.DONNEES_NON_PLAUSIBLES))
            }
        }
        return premiere as PreparationPrevision
    }

    /**
     * Consultations du suivi : à partir de la dernière consultation marquée « début du suivi », sinon
     * de la plus ancienne qui reste dans les 52 semaines avant la dernière consultation.
     */
    fun consultationsDuSuivi(datees: List<ConsultationEv>): List<ConsultationEv> {
        val marquee = datees.indexOfLast { it.selectionPrevision().debutSuivi }
        if (marquee >= 0) return datees.drop(marquee)
        val fin = datees.last().date!!
        val debut = datees.indexOfFirst { it.weight != null && it.date!!.daysUntil(fin) <= ConstantesChienV2.HORIZON_MAX_JOURS }
        return datees.drop(if (debut >= 0) debut else datees.lastIndex)
    }

    private class RationPasseeManquante(val raison: String)

    /** Somme des apports des rations [ids] ; null si l'une manque ou si l'énergie est nulle. */
    internal fun somme(ids: List<String>, apports: Map<String, ApportRation>): ApportRation? {
        if (ids.isEmpty()) return null
        val liste = ids.map { apports[it] ?: return null }
        val kcal = liste.sumOf { it.kcal }
        if (kcal <= 0) return null
        val p = if (liste.all { it.proteinesG != null }) liste.sumOf { it.proteinesG!! } else null
        val l = if (liste.all { it.lipidesG != null }) liste.sumOf { it.lipidesG!! } else null
        return ApportRation(kcal, p, l)
    }

    /** Composition en g/100 kcal EM ; null si inconnue. */
    internal fun composition(a: ApportRation?): Composition? {
        if (a == null || a.proteinesG == null || a.lipidesG == null || a.kcal <= 0) return null
        if (a.proteinesG <= 0 && a.lipidesG <= 0) return null
        return Composition(a.proteinesG * 100 / a.kcal, a.lipidesG * 100 / a.kcal)
    }

    private fun preparerSuivi(
            animal: AnimalEv,
            suivi: List<ConsultationEv>,
            references: Map<String, ReferenceEv?>,
            apports: Map<String, ApportRation>,
            estimationDesactivee: String?
    ): Any {
        val motifs = mutableListOf<MotifRefus>()
        for (c in suivi) {
            val reference = references[c.uuid]
            if (reference == null) motifs += MotifRefus.REFERENTIEL_ABSENT
            else {
                if (reference.espece != Espece.CHIEN) motifs += MotifRefus.ESPECE
                if (reference.stadePhysio != StadePhysio.ADULTE) motifs += MotifRefus.STADE_NON_ADULTE
            }
            if (c.referencesMaladies.isNotEmpty()) motifs += MotifRefus.MALADIE
            if ((c.coefGes ?: 0) > 0 || (c.coefLact ?: 0) > 0 || (c.nLittle ?: 0) > 0) motifs += MotifRefus.GESTATION_LACTATION
        }
        if (motifs.isNotEmpty()) return PreparationPrevision.Refus(motifs.distinct())

        val debutC = suivi.first()
        val debut = debutC.date!!
        val bw0 = debutC.weight ?: return PreparationPrevision.Refus(listOf(MotifRefus.POIDS_INITIAL_ABSENT))
        val avertissements = mutableListOf<Avertissement>()
        val idealSaisi = debutC.idealWeight?.takeIf { it > 0 }
        if (idealSaisi == null) avertissements += Avertissement.POIDS_IDEAL_NON_SAISI
        val bwi = idealSaisi ?: bw0
        val derniere = suivi.last()
        val poidsVise = derniere.idealWeight?.takeIf { it > 0 } ?: bwi
        if (bw0 !in POIDS_MIN..POIDS_MAX || bwi !in POIDS_MIN..POIDS_MAX || bw0 / bwi > 2.0 || poidsVise !in POIDS_MIN..POIDS_MAX) {
            return PreparationPrevision.Refus(listOf(MotifRefus.DONNEES_NON_PLAUSIBLES), "poids $bw0 kg, poids idéal $bwi kg")
        }
        if (bw0 < 0.95 * bwi) return PreparationPrevision.Refus(listOf(MotifRefus.PRISE_DE_POIDS))
        if (bw0 < bwi) avertissements += Avertissement.EXTRAPOLATION_NON_VALIDEE

        fun jour(d: LocalDate) = debut.daysUntil(d)
        val jourConsultation = jour(derniere.date!!)
        if (jourConsultation > ConstantesChienV2.HORIZON_MAX_JOURS) return PreparationPrevision.Refus(listOf(MotifRefus.SUIVI_TROP_LONG))

        // Passé : la ration actuelle de la consultation suivante a été donnée depuis la précédente
        val rations = mutableListOf<PeriodeRation>()
        for (i in 0 until suivi.lastIndex) {
            val suivante = suivi[i + 1]
            val apport = somme(suivante.selectionPrevision().actuelles, apports)
                    ?: return RationPasseeManquante(
                            "ration actuelle non renseignée à la consultation du ${suivante.date} : " +
                                    "ration donnée depuis le ${suivi[i].date} inconnue"
                    )
            rations += PeriodeRation(jour(suivi[i].date!!), apport.kcal)
        }
        // Futur : ration proposée de la dernière consultation, sinon sa ration actuelle poursuivie
        val selection = derniere.selectionPrevision()
        var idsFuturs = selection.proposees
        var future = somme(idsFuturs, apports)
        if (future == null) {
            idsFuturs = selection.actuelles
            future = somme(idsFuturs, apports) ?: return PreparationPrevision.Refus(listOf(MotifRefus.RATION_ABSENTE))
            avertissements += Avertissement.RATION_ACTUELLE_POURSUIVIE
        }
        if (idsFuturs == selection.proposees && SelectionRationsPrevision.plusieursProposeesSansChoix(derniere)) {
            avertissements += Avertissement.DERNIERE_RATION_PROPOSEE_RETENUE
        }
        rations += PeriodeRation(jourConsultation, future.kcal)
        val periodes = fusionner(rations)
        if (periodes.any { it.kcalParJour !in KCAL_MIN..KCAL_MAX }) {
            return PreparationPrevision.Refus(listOf(MotifRefus.DONNEES_NON_PLAUSIBLES), "ration hors de $KCAL_MIN-$KCAL_MAX kcal/j")
        }

        val compositionActuelle = composition(future)
                ?: Composition(ConstantesChienV2.P_DEF, ConstantesChienV2.LP_DEF).also { avertissements += Avertissement.COMPOSITION_PAR_DEFAUT }
        val anterieure = composition(somme(debutC.selectionPrevision().actuelles, apports))

        // Pesées : consultations du suivi et historique des poids, après le début du suivi
        val pesees = (suivi.drop(1).mapNotNull { c -> c.weight?.let { Pesee(jour(c.date!!), it) } } +
                animal.weightHistory.filter { it.date > debut }.map { Pesee(jour(it.date), it.value) })
                .filter { it.jour in 1..ConstantesChienV2.HORIZON_MAX_JOURS }
                .distinctBy { it.jour to it.poids }
                .sortedBy { it.jour }
        if (pesees.any { it.poids !in 0.5 * bw0..1.5 * bw0 }) {
            return PreparationPrevision.Refus(listOf(MotifRefus.DONNEES_NON_PLAUSIBLES), "pesée éloignée de plus de 50 % du poids initial")
        }

        val besoinSansDeficit = besoinSansDeficit(debutC, references[debutC.uuid])
        if (besoinSansDeficit == null) avertissements += Avertissement.BESOIN_STANDARD_INDISPONIBLE

        val entrees = EntreesPrevision(
                debut = debut,
                chien = ChienSimule(bw0, bwi, compositionActuelle, anterieure),
                rations = periodes,
                pesees = pesees,
                jourConsultation = jourConsultation,
                poidsVise = poidsVise,
                mode = if (bw0 > bwi) ModePrevision.PERTE_DE_POIDS else ModePrevision.ENTRETIEN,
                rationsFutures = idsFuturs,
                nomsRationsFutures = derniere.rations.filter { it.uuid in idsFuturs }.joinToString(" + ") { it.name },
                besoinSansDeficit = besoinSansDeficit
        )
        return PreparationPrevision.Pret(entrees, avertissements, estimationDesactivee)
    }

    /**
     * Besoin énergétique sans déficit de [c] : besoin standard du référentiel (au poids idéal s'il est
     * saisi, comme l'analyse de ration) × K1, K2, K3, K5 × coefficient d'ajustement, sans K4 (objectif
     * de perte ou de gain de poids). Null sans référentiel, poids ou équation de besoin.
     */
    fun besoinSansDeficit(c: ConsultationEv, reference: ReferenceEv?): Double? {
        val poids = c.effectiveWeight ?: return null
        val standard = reference?.let { CalculMetabolique.besoinEnergetiqueStandard(poids, it, c.suppVarp) } ?: return null
        val k = listOf(c.k1Value, c.k2Value, c.k3Value, c.k5Value).fold(c.coefficientAjustement) { p, v -> p * (v ?: 1.0) }
        return (standard * k).takeIf { it.isFinite() && it > 0 }
    }

    /** Une période par changement : deux périodes successives de même ration ne font qu'une. */
    private fun fusionner(rations: List<PeriodeRation>): List<PeriodeRation> {
        val parJour = rations.groupBy { it.debutJour }.map { (_, p) -> p.last() }.sortedBy { it.debutJour }
        return parJour.fold(mutableListOf()) { acc, p ->
            if (acc.isEmpty() || acc.last().kcalParJour != p.kcalParJour) acc += p
            acc
        }
    }

    /** Plancher d'apport en kcal/j pour le poids visé. */
    fun plancherKcal(reglages: ReglagesPrevisionPoids, poidsVise: Double): Double =
            reglages.plancherKcalParKgMetabolique * poidsVise.pow(0.75)
}

/** Ration de la consultation dont la composition sert au contrôle des apports : rations additionnées. */
fun rationCombinee(rations: List<Ration>): Ration =
        Ration(name = rations.joinToString(" + ") { it.name },
               alimentMutableList = rations.flatMap { r -> r.alimentMutableList.map { it.copy() } }.toMutableList())
