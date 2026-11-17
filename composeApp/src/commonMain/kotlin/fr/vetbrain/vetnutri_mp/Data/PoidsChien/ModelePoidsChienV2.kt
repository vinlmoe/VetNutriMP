package fr.vetbrain.vetnutri_mp.Data.PoidsChien

import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2.BETA_G
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2.BETA_L
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2.BETA_P
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2.BRUIT
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2.C_ALLO
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2.EM_L
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2.EM_P
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2.ETA
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2.F_IDEAL
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2.GAMMA_F
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2.GAMMA_L
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2.M_PROP
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2.PAS_JOURS
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2.RHO_F
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2.RHO_L
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2.SD_LOG_M
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2.TAU
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.pow

/** Ration constante à partir du jour [debutJour] (jours depuis le début du suivi). */
data class PeriodeRation(val debutJour: Int, val kcalParJour: Double)

/** Pesée au jour [jour] du suivi. */
data class Pesee(val jour: Int, val poids: Double)

/** Composition du régime : protéines et lipides en g/100 kcal EM. */
data class Composition(val proteines: Double, val lipides: Double)

/** État du chien à un jour du suivi ; [adaptation] (AT) reste interne au module. */
data class EtatChien(val jour: Int, val graisse: Double, val maigre: Double, internal val adaptation: Double) {
    val poids: Double get() = graisse + maigre
}

/** Valeur moyenne et intervalle à 95 %. */
data class Intervalle(val moyenne: Double, val bas: Double, val haut: Double) {
    fun contient(v: Double) = v in bas..haut
}

/** Paramètres fixes d'un chien pour la simulation. */
data class ChienSimule(
        /** Poids au début du suivi, kg. */
        val poidsInitial: Double,
        /** Poids idéal, kg (égal au poids initial à l'entretien). */
        val poidsIdeal: Double,
        val composition: Composition,
        /** Régime antérieur au suivi ; null = celui du suivi. */
        val compositionAnterieure: Composition? = null
)

/**
 * Modèle de poids du chien adulte « chien-v2 » : partition graisse / masse maigre, adaptation
 * métabolique, thermogenèse alimentaire. Calcul pur, sans base de données ni interface.
 */
object ModelePoidsChienV2 {

    /** Masse grasse initiale (bissection sur [1e-4, BW0 + 1]). */
    fun graisseInitiale(poidsInitial: Double, poidsIdeal: Double): Double {
        fun ecart(f: Double) =
                f + (1 - F_IDEAL) * poidsIdeal * (f / (F_IDEAL * poidsIdeal)).pow(C_ALLO) - poidsInitial
        var a = 1e-4
        var b = poidsInitial + 1
        repeat(200) {
            val c = (a + b) / 2
            if (ecart(c) > 0) b = c else a = c
        }
        return (a + b) / 2
    }

    /** Besoin initial EI0 = m × BW0^0,75, kcal/j. */
    fun besoinInitial(m: Double, poidsInitial: Double): Double = m * poidsInitial.pow(0.75)

    internal fun thermogenese(c: Composition): Double {
        val fP = EM_P * c.proteines / 100
        val fL = EM_L * c.lipides / 100
        return BETA_P * fP + BETA_L * fL + BETA_G * max(0.0, 1 - fP - fL)
    }

    /** Ration de la dernière période commencée au jour [t]. */
    internal fun rationAu(rations: List<PeriodeRation>, t: Double): Double {
        var e = rations.first().kcalParJour
        for (p in rations) if (p.debutJour <= t + 1e-9) e = p.kcalParJour
        return e
    }

    /**
     * Trajectoire du chien de besoin [m] (kcal/kg^0,75/j) avec les [rations] (triées, la première au
     * jour 0), RK4 à pas de 2 jours ; un état par jour pair de 0 à [horizonJours].
     */
    fun simuler(chien: ChienSimule, m: Double, rations: List<PeriodeRation>, horizonJours: Int): List<EtatChien> {
        require(rations.isNotEmpty() && rations.first().debutJour == 0) { "La première période de ration doit commencer au jour 0" }
        val bw0 = chien.poidsInitial
        val f0 = graisseInitiale(bw0, chien.poidsIdeal)
        val l0 = bw0 - f0
        val ei0 = besoinInitial(m, bw0)
        val bt = thermogenese(chien.composition)
        val bt0 = thermogenese(chien.compositionAnterieure ?: chien.composition)
        val k = ei0 * (1 - bt0) - GAMMA_F * f0.pow(0.75) - GAMMA_L * l0.pow(0.75)

        fun derivees(t: Double, f: Double, l: Double, at: Double): DoubleArray {
            val fc = max(f, 0.001)
            val lc = max(l, 0.001)
            val ei = rationAu(rations, t)
            val ee = k + GAMMA_F * fc.pow(0.75) + GAMMA_L * lc.pow(0.75) + bt * ei + at
            val c = C_ALLO * lc * RHO_L / RHO_F
            val p = c / (c + fc)
            return doubleArrayOf((1 - p) * (ei - ee) / RHO_F, p * (ei - ee) / RHO_L, (ETA * (ei - ei0) - at) / TAU)
        }

        val h = PAS_JOURS.toDouble()
        var f = f0
        var l = l0
        var at = 0.0
        val etats = ArrayList<EtatChien>(horizonJours / PAS_JOURS + 1)
        etats += EtatChien(0, f, l, at)
        var t = 0
        while (t < horizonJours) {
            val k1 = derivees(t.toDouble(), f, l, at)
            val k2 = derivees(t + h / 2, f + h / 2 * k1[0], l + h / 2 * k1[1], at + h / 2 * k1[2])
            val k3 = derivees(t + h / 2, f + h / 2 * k2[0], l + h / 2 * k2[1], at + h / 2 * k2[2])
            val k4 = derivees(t + h, f + h * k3[0], l + h * k3[1], at + h * k3[2])
            f += h / 6 * (k1[0] + 2 * k2[0] + 2 * k3[0] + k4[0])
            l += h / 6 * (k1[1] + 2 * k2[1] + 2 * k3[1] + k4[1])
            at += h / 6 * (k1[2] + 2 * k2[2] + 2 * k3[2] + k4[2])
            t += PAS_JOURS
            etats += EtatChien(t, f, l, at)
        }
        return etats
    }

    /** Jour pair le plus proche. */
    fun jourPair(jour: Int): Int = 2 * floor(jour / 2.0 + 0.5).toInt()

    /**
     * Pesées arrondies au jour pair, sans celles faites de 1 à 13 jours après un changement de
     * ration ou après le début du suivi (eau et contenu digestif). Une pesée du jour même d'un
     * changement reflète encore la ration précédente : elle est gardée.
     */
    fun peseesUtilisables(pesees: List<Pesee>, rations: List<PeriodeRation>, horizonJours: Int): List<Pesee> =
            pesees.map { Pesee(jourPair(it.jour), it.poids) }
                    .filter { p ->
                        val dernierChangement = rations.map { it.debutJour }.filter { it < p.jour }.maxOrNull() ?: 0
                        p.jour in 1..horizonJours && p.jour - dernierChangement >= ConstantesChienV2.DELAI_PESEE_JOURS
                    }

    /** Valeurs u de la grille du log du besoin (m = M_PROP × exp(u)). */
    val grilleU: List<Double> = (0 until ConstantesChienV2.NOMBRE_U).map { i ->
        ConstantesChienV2.U_MIN + (ConstantesChienV2.U_MAX - ConstantesChienV2.U_MIN) * i / (ConstantesChienV2.NOMBRE_U - 1)
    }

    /**
     * Besoin apparent à partir des [pesees] (a priori seul sans pesée) : une trajectoire par valeur
     * de grille avec les [rations] réellement données, pondérée par l'a priori et la vraisemblance
     * des pesées utilisables.
     */
    fun ajuster(chien: ChienSimule, rations: List<PeriodeRation>, pesees: List<Pesee>, horizonJours: Int): Ajustement {
        val utilisables = peseesUtilisables(pesees, rations, horizonJours)
        val sigma = BRUIT * chien.poidsInitial
        val trajectoires = grilleU.map { u -> simuler(chien, M_PROP * exp(u), rations, horizonJours) }
        val logPoids = grilleU.mapIndexed { i, u ->
            var lp = -u * u / (2 * SD_LOG_M * SD_LOG_M)
            for (p in utilisables) {
                val ecart = p.poids - trajectoires[i][p.jour / PAS_JOURS].poids
                lp -= ecart * ecart / (2 * sigma * sigma)
            }
            lp
        }
        val maximum = logPoids.max()
        val bruts = logPoids.map { exp(it - maximum) }
        val total = bruts.sum()
        return Ajustement(chien, rations, utilisables, horizonJours, trajectoires, bruts.map { it / total })
    }

    /** Valeurs triées puis premières dont le poids cumulé atteint 0,025 et 0,975. */
    internal fun intervalle(valeurs: List<Double>, poids: List<Double>): Intervalle {
        val moyenne = valeurs.indices.sumOf { valeurs[it] * poids[it] }
        val tries = valeurs.indices.sortedBy { valeurs[it] }
        var cumul = 0.0
        var bas: Double? = null
        var haut: Double? = null
        for (i in tries) {
            cumul += poids[i]
            if (bas == null && cumul >= 0.025) bas = valeurs[i]
            if (haut == null && cumul >= 0.975) haut = valeurs[i]
        }
        return Intervalle(moyenne, bas ?: valeurs[tries.first()], haut ?: valeurs[tries.last()])
    }

    /**
     * Ration constante à partir du jour [jourDebut] (rations antérieures inchangées) pour que le poids
     * moyen prévu atteigne [poidsCible] au jour [jourCible] ; bissection sur [40, 4000] kcal/j avec
     * les valeurs de grille de poids normalisé supérieur à 1e-6.
     */
    fun rationPourCible(ajustement: Ajustement, jourDebut: Int, jourCible: Int, poidsCible: Double): Double {
        val retenues = ajustement.grillePoids.indices.filter { ajustement.grillePoids[it] > ConstantesChienV2.POIDS_GRILLE_MIN }
        val somme = retenues.sumOf { ajustement.grillePoids[it] }
        val anterieures = ajustement.rations.filter { it.debutJour < jourDebut }
        fun poidsMoyen(e: Double): Double {
            val rations = anterieures + PeriodeRation(jourDebut, e)
            return retenues.sumOf { i ->
                ajustement.grillePoids[i] *
                        simuler(ajustement.chien, M_PROP * exp(grilleU[i]), rations, jourCible).last().poids
            } / somme
        }
        var a = ConstantesChienV2.RATION_MIN
        var b = ConstantesChienV2.RATION_MAX
        repeat(45) {
            val c = (a + b) / 2
            if (poidsMoyen(c) > poidsCible) b = c else a = c
        }
        return (a + b) / 2
    }

    /**
     * Niveaux de déficit pour le choix initial : chien de besoin M_PROP nourri à 40, 50, 60 et
     * 70 % de [besoinReference] (par défaut EI0) ; perte moyenne des semaines 1 à 4 et poids aux
     * semaines 12, 26 et 52 (intervalle tiré de l'a priori).
     */
    fun niveauxDeficit(
            chien: ChienSimule,
            besoinReference: Double = besoinInitial(M_PROP, chien.poidsInitial)
    ): List<NiveauDeficit> {
        return ConstantesChienV2.NIVEAUX_DEFICIT.map { fraction ->
            val e = fraction * besoinReference
            val rations = listOf(PeriodeRation(0, e))
            val typique = simuler(chien, M_PROP, rations, ConstantesChienV2.HORIZON_MAX_JOURS)
            val apriori = ajuster(chien, rations, emptyList(), ConstantesChienV2.HORIZON_MAX_JOURS)
            NiveauDeficit(
                    fractionBesoin = fraction,
                    kcalParJour = e,
                    perteSemaines1a4 = (chien.poidsInitial - typique[28 / PAS_JOURS].poids) / chien.poidsInitial * 100 / 4,
                    poidsSemaine12 = apriori.poidsPrevu(84).copy(moyenne = typique[84 / PAS_JOURS].poids),
                    poidsSemaine26 = apriori.poidsPrevu(182).copy(moyenne = typique[182 / PAS_JOURS].poids),
                    poidsSemaine52 = apriori.poidsPrevu(364).copy(moyenne = typique[364 / PAS_JOURS].poids)
            )
        }
    }
}

/** Résultat de l'ajustement sur la grille du besoin. */
class Ajustement internal constructor(
        val chien: ChienSimule,
        val rations: List<PeriodeRation>,
        /** Pesées retenues (arrondies au jour pair). */
        val peseesUtilisees: List<Pesee>,
        val horizonJours: Int,
        internal val trajectoires: List<List<EtatChien>>,
        /** Poids normalisés des valeurs de grille. */
        val grillePoids: List<Double>
) {
    private val besoins = ModelePoidsChienV2.grilleU.map { M_PROP * exp(it) }

    /** Besoin apparent m (kcal/kg^0,75/j) et son intervalle à 95 %. */
    val mApparent: Intervalle = ModelePoidsChienV2.intervalle(besoins, grillePoids)

    /** Besoin d'entretien apparent en kcal/j : m × BW0^0,75. */
    val besoinApparent: Intervalle
        get() {
            val f = chien.poidsInitial.pow(0.75)
            return Intervalle(mApparent.moyenne * f, mApparent.bas * f, mApparent.haut * f)
        }

    /** Poids prévu au jour [jour] (arrondi au jour pair, borné à l'horizon) et son intervalle. */
    fun poidsPrevu(jour: Int): Intervalle {
        val j = ModelePoidsChienV2.jourPair(jour).coerceIn(0, horizonJours) / PAS_JOURS
        return ModelePoidsChienV2.intervalle(trajectoires.map { it[j].poids }, grillePoids)
    }
}

/** Un niveau de déficit du tableau de choix initial. */
data class NiveauDeficit(
        val fractionBesoin: Double,
        val kcalParJour: Double,
        /** Perte moyenne des semaines 1 à 4, % du poids par semaine. */
        val perteSemaines1a4: Double,
        val poidsSemaine12: Intervalle,
        val poidsSemaine26: Intervalle,
        val poidsSemaine52: Intervalle
)
