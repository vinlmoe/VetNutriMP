package fr.vetbrain.vetnutri_mp.Data.PoidsChien

import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2.M_PROP
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Valeurs de référence obtenues par deux implémentations indépendantes en R (deSolve et RK4).
 * Tolérances : ±0,001 kg sur les masses, ±0,01 sur AT, ±0,01 sur m, ±0,5 kcal/j sur les rations.
 */
class ModelePoidsChienV2Test {

    private val masse = 0.001
    private val tolAt = 0.01
    private val tolM = 0.01
    private val tolKcal = 0.5

    private fun proche(attendu: Double, obtenu: Double, tol: Double, quoi: String) =
            assertTrue(abs(attendu - obtenu) <= tol, "$quoi : attendu $attendu, obtenu $obtenu")

    private fun intervalle(attendu: Triple<Double, Double, Double>, obtenu: Intervalle, tol: Double, quoi: String) {
        proche(attendu.first, obtenu.moyenne, tol, "$quoi (moyenne)")
        proche(attendu.second, obtenu.bas, tol, "$quoi (bas)")
        proche(attendu.third, obtenu.haut, tol, "$quoi (haut)")
    }

    private val chienA = ChienSimule(30.0, 22.0, Composition(10.4, 3.3))
    private val rationsA = listOf(PeriodeRation(0, 620.0))
    private val peseesC = listOf(Pesee(28, 28.4), Pesee(56, 27.0))
    private val chienD = ChienSimule(20.0, 20.0, Composition(7.0, 4.0))
    private val rationsD = listOf(PeriodeRation(0, 920.0))

    private fun verifierTrajectoire(
            chien: ChienSimule, kcal: Double, f0: Double, ei0: Double,
            lignes: List<List<Double>>
    ) {
        proche(f0, ModelePoidsChienV2.graisseInitiale(chien.poidsInitial, chien.poidsIdeal), masse, "F0")
        proche(ei0, ModelePoidsChienV2.besoinInitial(M_PROP, chien.poidsInitial), 0.01, "EI0")
        val etats = ModelePoidsChienV2.simuler(chien, M_PROP, listOf(PeriodeRation(0, kcal)), 364)
        lignes.forEach { (jour, bw, f, l, at) ->
            val e = etats[jour.toInt() / 2]
            assertEquals(jour.toInt(), e.jour)
            proche(bw, e.poids, masse, "BW J${jour.toInt()}")
            proche(f, e.graisse, masse, "F J${jour.toInt()}")
            proche(l, e.maigre, masse, "L J${jour.toInt()}")
            proche(at, e.adaptation, tolAt, "AT J${jour.toInt()}")
        }
    }

    @Test
    fun casA_trajectoire() = verifierTrajectoire(
            chienA, 620.0, 14.1047, 1249.74,
            listOf(
                    listOf(28.0, 28.3544, 12.6849, 15.6695, -104.4739),
                    listOf(56.0, 27.0276, 11.5541, 15.4735, -185.8383),
                    listOf(84.0, 25.9533, 10.6490, 15.3043, -249.2050),
                    listOf(182.0, 23.5158, 8.6375, 14.8783, -379.3043),
                    listOf(364.0, 21.5212, 7.0459, 14.4754, -453.9937)
            )
    )

    @Test
    fun casB_trajectoire() = verifierTrajectoire(
            ChienSimule(12.0, 9.0, Composition(11.7, 3.4)), 250.0, 5.5339, 628.59,
            listOf(
                    listOf(28.0, 11.0154, 4.6917, 6.3237, -62.8074),
                    listOf(56.0, 10.2225, 4.0276, 6.1949, -111.7219),
                    listOf(84.0, 9.5826, 3.5031, 6.0795, -149.8165),
                    listOf(182.0, 8.1575, 2.3851, 5.7725, -228.0293),
                    listOf(364.0, 7.1303, 1.6414, 5.4889, -272.9309)
            )
    )

    @Test
    fun entretien_graisseInitialeAuPoidsIdeal() =
            proche(ConstantesChienV2.F_IDEAL * 20.0, ModelePoidsChienV2.graisseInitiale(20.0, 20.0), 1e-6, "F0")

    @Test
    fun casC_sansPesee() {
        val a = ModelePoidsChienV2.ajuster(chienA, rationsA, emptyList(), 182)
        intervalle(Triple(98.2578, 76.3093, 124.5609), a.mApparent, tolM, "m")
        intervalle(Triple(23.3985, 19.7814, 26.3441), a.poidsPrevu(182), masse, "BW J182")
    }

    @Test
    fun casC_deuxPesees_etRationProposee() {
        val a = ModelePoidsChienV2.ajuster(chienA, rationsA, peseesC, 182)
        assertEquals(2, a.peseesUtilisees.size)
        intervalle(Triple(97.3726, 89.5498, 105.0877), a.mApparent, tolM, "m")
        intervalle(Triple(23.5309, 22.4851, 24.5837), a.poidsPrevu(182), masse, "BW J182")
        proche(444.80, ModelePoidsChienV2.rationPourCible(a, 56, 182, 22.0), tolKcal, "ration")
    }

    @Test
    fun casD_entretien() {
        val sans = ModelePoidsChienV2.ajuster(chienD, rationsD, emptyList(), 140)
        intervalle(Triple(98.2578, 76.3093, 124.5609), sans.mApparent, tolM, "m sans pesée")
        proche(929.26, sans.besoinApparent.moyenne, tolKcal, "besoin sans pesée")
        intervalle(Triple(19.9012, 17.4511, 21.8990), sans.poidsPrevu(140), masse, "BW J140 sans pesée")

        val une = ModelePoidsChienV2.ajuster(chienD, rationsD, listOf(Pesee(28, 20.4)), 140)
        intervalle(Triple(86.8383, 75.1732, 98.9678), une.mApparent, tolM, "m une pesée")
        proche(821.27, une.besoinApparent.moyenne, tolKcal, "besoin une pesée")
        intervalle(Triple(20.9489, 19.8451, 22.0008), une.poidsPrevu(140), masse, "BW J140 une pesée")

        val deux = ModelePoidsChienV2.ajuster(chienD, rationsD, listOf(Pesee(28, 20.4), Pesee(56, 20.5)), 140)
        intervalle(Triple(86.8930, 80.2218, 93.6716), deux.mApparent, tolM, "m deux pesées")
        proche(821.78, deux.besoinApparent.moyenne, tolKcal, "besoin deux pesées")
        intervalle(Triple(20.9451, 20.3296, 21.5476), deux.poidsPrevu(140), masse, "BW J140 deux pesées")
        proche(784.46, ModelePoidsChienV2.rationPourCible(deux, 56, 140, 20.0), tolKcal, "ration d'entretien")
        // L'intervalle se resserre au fil des pesées
        assertTrue(deux.mApparent.haut - deux.mApparent.bas < une.mApparent.haut - une.mApparent.bas)
        assertTrue(une.mApparent.haut - une.mApparent.bas < sans.mApparent.haut - sans.mApparent.bas)
    }

    @Test
    fun casE_rationModifieeEnCoursDeSuivi() {
        val rations = listOf(PeriodeRation(0, 620.0), PeriodeRation(28, 530.0))
        val a = ModelePoidsChienV2.ajuster(chienA, rations, peseesC, 182)
        intervalle(Triple(94.6740, 86.9032, 102.4930), a.mApparent, tolM, "m")
        intervalle(Triple(23.0216, 21.9557, 24.0725), a.poidsPrevu(182), masse, "BW J182")
    }

    @Test
    fun coefficientsDePerte() {
        val attendus = listOf(
                listOf(499.90, 1.6344, 25.1684, 22.2424, 19.8541),
                listOf(624.87, 1.3607, 25.9850, 23.5671, 21.5883),
                listOf(749.85, 1.0875, 26.7964, 24.8758, 23.2982),
                listOf(874.82, 0.8148, 27.6032, 26.1714, 24.9906)
        )
        val niveaux = ModelePoidsChienV2.niveauxDeficit(chienA)
        assertEquals(4, niveaux.size)
        niveaux.zip(attendus).forEach { (n, ligne) ->
            val (kcal, perte, s12, s26, s52) = ligne
            proche(kcal, n.kcalParJour, tolKcal, "kcal ${n.fractionBesoin}")
            proche(perte, n.perteSemaines1a4, 1e-4, "perte S1-S4 ${n.fractionBesoin}")
            proche(s12, n.poidsSemaine12.moyenne, masse, "S12 ${n.fractionBesoin}")
            proche(s26, n.poidsSemaine26.moyenne, masse, "S26 ${n.fractionBesoin}")
            proche(s52, n.poidsSemaine52.moyenne, masse, "S52 ${n.fractionBesoin}")
            assertTrue(n.poidsSemaine26.bas < n.poidsSemaine26.moyenne && n.poidsSemaine26.moyenne < n.poidsSemaine26.haut)
        }
    }

    @Test
    fun peseeMoinsDe14JoursApresUnChangement_exclue() {
        val rations = listOf(PeriodeRation(0, 620.0), PeriodeRation(28, 530.0))
        val retenues = ModelePoidsChienV2.peseesUtilisables(
                listOf(Pesee(10, 29.5), Pesee(28, 28.4), Pesee(40, 27.6), Pesee(42, 27.5), Pesee(57, 27.0)),
                rations, 364
        )
        // J10 : début du suivi < 14 j ; J28 : jour du changement (ration précédente) gardée ;
        // J40 : 12 j après le changement, exclue ; J42 : 14 j, gardée ; J57 arrondi à J58
        assertEquals(listOf(Pesee(28, 28.4), Pesee(42, 27.5), Pesee(58, 27.0)), retenues)
    }
}
