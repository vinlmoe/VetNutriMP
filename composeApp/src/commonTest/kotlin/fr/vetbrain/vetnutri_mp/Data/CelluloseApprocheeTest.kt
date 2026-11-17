package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.EquationKind
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Repository.InMemoryEquationRepository
import kotlinx.coroutines.test.runTest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Cellulose absente d'un aliment : approchée par la fibre totale (équation complémentaire des
 * données initiales), dans l'énergie (DEcom chien) comme dans l'ENA calculée, sans compter la
 * fibre deux fois. Scripts identiques à vetnutri_export_init.json.
 */
class CelluloseApprocheeTest {

    private fun assertNear(expected: Double, actual: Double?, tol: Double = 0.01) {
        assertTrue(actual != null && abs(expected - actual) <= tol, "attendu $expected ± $tol, obtenu $actual")
    }

    private val decom =
            "(5.7*PROTEINE + 9.4*LIPIDE + 4.1*(ENA + CELLULOSE)) * (91.2 - (1.43*CELLULOSE*100/(100 - HUMIDITE)))/100 - 1.04*PROTEINE"

    private fun complementaire(nutriment: NutrientMain, script: String) =
            Equation(
                    equationScript = script,
                    specie = Espece.CH,
                    kind = EquationKind.COMPLEMENTARY_NUTRIENT,
                    nutrient = nutriment
            )

    private suspend fun contexte(): Pair<ReferenceEv, InMemoryEquationRepository> {
        val equations =
                listOf(
                        complementaire(NutrientMain.CELLULOSE, "FIBRETOT"),
                        complementaire(NutrientMain.ENA, "100-PROTEINE-HUMIDITE-CELLULOSE-CENDRE-LIPIDE")
                )
        val depot = InMemoryEquationRepository()
        equations.forEach { depot.saveEquation(it) }
        val reference = ReferenceEv(espece = Espece.CHIEN)
        reference.equationDEcom = Equation(equationScript = decom)
        reference.equationsNut = equations.toMutableList()
        return reference to depot
    }

    private fun ration(quantite: Double, vararg valeurs: Pair<NutrientMain, Double>): Ration {
        val aliment = AlimentEv(typeAliment = FoodKind.COMPLET)
        valeurs.forEach { (n, v) -> aliment.setNutrient(n, v) }
        return Ration(alimentMutableList = mutableListOf(AlimentRation(aliment = aliment, quantite = quantite, weight = 1.0)))
    }

    private fun energieDecom(p: Double, l: Double, ena: Double, cell: Double, h: Double) =
            (5.7 * p + 9.4 * l + 4.1 * (ena + cell)) * (91.2 - 1.43 * cell * 100 / (100 - h)) / 100 - 1.04 * p

    @Test
    fun laitChiot_fibreTotaleSeule_entreDansLEnergie() = runTest {
        // 200 g de Baby dog milk : ENA donnée, fibre totale 0,5 %, cellulose non renseignée
        val (reference, depot) = contexte()
        val r = ration(
                200.0,
                NutrientMain.HUMIDITE to 3.0, NutrientMain.PROTEINE to 33.0, NutrientMain.LIPIDE to 39.0,
                NutrientMain.ENA to 19.5, NutrientMain.CENDRE to 6.0, NutrientMain.FIBRETOT to 0.5
        )
        val valeurs = analyserValeursNutritionnellesRationAvecEquations(r, depot, reference)

        // 541,66 kcal/100 g (et non 544,48 avec une cellulose nulle)
        assertNear(2 * energieDecom(33.0, 39.0, 19.5, 0.5, 3.0), valeurs[NutrientMain.ENERGIE.label]?.valeur)
        assertNear(1083.3, valeurs[NutrientMain.ENERGIE.label]?.valeur, 0.1)
        assertNear(1.0, valeurs[NutrientMain.CELLULOSE.label]?.valeur)
    }

    @Test
    fun enaCalculee_retireLaCelluloseApprochee() = runTest {
        // Ni ENA ni cellulose : ENA = 100 − 25 − 8 − 10 − 7 − 15 = 35, fibre comptée une seule fois
        val (reference, depot) = contexte()
        val r = ration(
                100.0,
                NutrientMain.HUMIDITE to 8.0, NutrientMain.PROTEINE to 25.0, NutrientMain.LIPIDE to 15.0,
                NutrientMain.CENDRE to 7.0, NutrientMain.FIBRETOT to 10.0
        )
        val valeurs = analyserValeursNutritionnellesRationAvecEquations(r, depot, reference)

        assertNear(35.0, valeurs[NutrientMain.ENA.label]?.valeur)
        assertNear(10.0, valeurs[NutrientMain.CELLULOSE.label]?.valeur)
        assertNear(energieDecom(25.0, 15.0, 35.0, 10.0, 8.0), valeurs[NutrientMain.ENERGIE.label]?.valeur)
    }

    @Test
    fun celluloseRenseignee_resteUtilisee() = runTest {
        val (reference, depot) = contexte()
        val r = ration(
                100.0,
                NutrientMain.HUMIDITE to 8.0, NutrientMain.PROTEINE to 25.0, NutrientMain.LIPIDE to 15.0,
                NutrientMain.CENDRE to 7.0, NutrientMain.CELLULOSE to 3.0, NutrientMain.FIBRETOT to 10.0
        )
        val valeurs = analyserValeursNutritionnellesRationAvecEquations(r, depot, reference)

        assertNear(3.0, valeurs[NutrientMain.CELLULOSE.label]?.valeur)
        assertNear(42.0, valeurs[NutrientMain.ENA.label]?.valeur)
        assertNear(energieDecom(25.0, 15.0, 42.0, 3.0, 8.0), valeurs[NutrientMain.ENERGIE.label]?.valeur)
    }
}
