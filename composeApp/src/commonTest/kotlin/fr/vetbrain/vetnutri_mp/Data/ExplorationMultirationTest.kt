package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.ContEnum
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.Repository.InMemoryEquationRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class ExplorationMultirationTest {

    /** BEE = 100 × BW^0.75 : 800 kcal pour 16 kg ; seuils par 1000 kcal de BEE standard. */
    private fun reference(): ReferenceEv =
            ReferenceEv(nom = "Adulte test", espece = Espece.CHIEN).apply {
                equationBW = Equation(equationScript = "BW ^ 0.75")
                equationBEE = Equation(equationScript = "100 * BW ^ 0.75")
                fun seuil(n: Nutrient, niveau: Reflevel, valeur: Double) =
                        definirNutriment(valeur, n, niveau, UnitReqEnum.PERKCAL, BiblioRef())
                seuil(NutrientMain.PROTEINE, Reflevel.OPTIMIN, 50.0) // 40 g
                seuil(NutrientMain.PROTEINE, Reflevel.MAX, 200.0) // 160 g
                seuil(NutrientMain.CELLULOSE, Reflevel.OPTIMIN, 5.0) // 4 g, ajusté à 5 × 4 = 20 g
                seuil(NutrientMacro.CAL, Reflevel.OPTIMIN, 1.0) // 0,8 g
                seuil(NutrientLipid.O6, Reflevel.OPTIMIN, 2.0) // 1,6 g
                seuil(NutrientMacro.NA, Reflevel.OPTIMIN, 0.5) // 0,4 g
            }

    private fun aliment(nom: String, vararg valeurs: Pair<Nutrient, Double>): AlimentEv =
            AlimentEv(uuid = nom, nom = nom).apply { valeurs.forEach { (n, v) -> setNutrient(n, v) } }

    private val proteine = aliment("viande", NutrientMain.PROTEINE to 50.0, NutrientMain.ENERGIE to 100.0)
    private val proteineGrasse = aliment("viande grasse", NutrientMain.PROTEINE to 10.0, NutrientMain.ENERGIE to 500.0)
    private val fibre = aliment("son", NutrientMain.CELLULOSE to 50.0)
    private val calcium = aliment("carbonate", NutrientMacro.CAL to 20.0)
    private val omega6 = aliment("huile", NutrientLipid.O6 to 10.0)
    private val sodium = aliment("sel", NutrientMacro.NA to 10.0)
    private val energie = aliment("riz", NutrientMain.ENERGIE to 400.0)

    private fun configuration(k: List<Double> = listOf(1.0, 3.0)) =
            ConfigurationExploration(
                    references = listOf(reference()),
                    poids = listOf(16.0),
                    coefficientsK = k,
                    listes =
                            mapOf(
                                    RoleExploration.PROTEINES to listOf(proteine, proteineGrasse),
                                    RoleExploration.FIBRES to listOf(fibre),
                                    RoleExploration.CALCIUM to listOf(calcium),
                                    RoleExploration.OMEGA6 to listOf(omega6),
                                    RoleExploration.SODIUM to listOf(sodium),
                                    RoleExploration.ENERGIE to listOf(energie)
                            )
            )

    @Test
    fun explorer_ajusteSuccessivementEtClasseLesCases() = runTest {
        val resultat = ExplorateurMultiration(InMemoryEquationRepository()).explorer(configuration())
        assertEquals(4, resultat.scenarios.size)

        val s = resultat.scenarios.first { it.k == 1.0 && it.combinaison == 1 }
        val quantites = s.ration.alimentMutableList.map { it.quantite }
        // 40 g de protéines / 50 % ; 20 g de cellulose / 50 % ; 0,8 g de Ca / 20 % = 4 g → dose min. 5 g ;
        // 1,6 g d'O6 / 10 % ; 0,4 g de Na / 10 % = 4 g → 5 g ; (800 − 80) kcal / 4 kcal/g = 180 g
        assertEquals(listOf(80.0, 40.0, 5.0, 16.0, 5.0, 180.0), quantites)
        assertEquals(800.0, s.besoinTotal!!, 1e-9)
        assertEquals(800.0, s.energie!!, 1e-6)
        assertEquals(StatutScenario.CONFORME, s.statut)

        // 400 g de viande grasse apportent déjà 2000 kcal pour un besoin de 800
        val grasse = resultat.scenarios.first { it.k == 1.0 && it.combinaison == 2 }
        assertEquals(StatutScenario.ENERGIE_DEPASSEE, grasse.statut)
        assertEquals(0.0, grasse.ration.alimentMutableList.last().quantite)

        // K = 3 : besoin 2400 kcal, l'énergie restante est arrondie au pas de 25 g
        val k3 = resultat.scenarios.first { it.k == 3.0 && it.combinaison == 1 }
        assertEquals(575.0, k3.ration.alimentMutableList.last().quantite)
        assertEquals(StatutScenario.CONFORME, k3.statut)

        val cases = resultat.cases
        assertEquals(2, cases.size)
        val case1 = cases.first { it.k == 1.0 }
        assertEquals(ZoneEquilibre.EQUILIBRABLE, case1.zone)
        assertEquals(1, case1.conformes)
        assertEquals(StatutScenario.CONFORME, case1.meilleurScenario?.statut)
    }

    @Test
    fun explorer_cibleAbsente_conserveLeScenario() = runTest {
        val config = configuration(listOf(1.0))
        val sansSodium = config.copy(references = listOf(reference().apply { supprimerNutriment(NutrientMacro.NA, Reflevel.OPTIMIN) }))
        val resultat = ExplorateurMultiration(InMemoryEquationRepository()).explorer(sansSodium)
        assertTrue(resultat.scenarios.all { it.statut == StatutScenario.CIBLE_ABSENTE })
        assertEquals(ZoneEquilibre.NON_EVALUABLE, resultat.cases.single().zone)
    }

    @Test
    fun explorer_refuseLesListesVidesEtLaLimite() = runTest {
        val explorateur = ExplorateurMultiration(InMemoryEquationRepository())
        assertFailsWith<ExplorationException> {
            explorateur.explorer(configuration().copy(listes = configuration().listes - RoleExploration.SODIUM))
        }
        assertFailsWith<ExplorationException> { explorateur.explorer(configuration().copy(maxScenarios = 3)) }
    }

    @Test
    fun arrondi_doseMinimaleEtContenants() {
        val vrac = aliment("vrac")
        assertEquals(0.0, arrondirAvecDoseMinimale(vrac, 2.0, 5.0)) // moins de la moitié : non utilisé
        assertEquals(5.0, arrondirAvecDoseMinimale(vrac, 3.0, 5.0)) // porté à la dose minimale
        assertEquals(12.0, arrondirAvecDoseMinimale(vrac, 12.4, 5.0))
        assertEquals(45.0, arrondirAvecDoseMinimale(vrac, 43.0, 5.0))
        val dosette = AlimentEv(uuid = "dosette", nom = "dosette", cont = ContEnum.SACHET, quantInt = 4.0)
        assertEquals(6.0, doseMinimaleEffective(dosette, 5.0)) // premier multiple de ½ dosette ≥ 5 g
        assertEquals(10.0, arrondirAvecDoseMinimale(dosette, 9.5, 5.0))
    }

    @Test
    fun intervalle_inclutLeMaximum() {
        assertEquals(listOf(5.0, 10.0, 12.0), intervalleExploration(5.0, 12.0, 5.0))
        assertEquals(listOf(0.8, 0.9, 1.0, 1.1, 1.2), intervalleExploration(0.8, 1.2, 0.1))
        assertFailsWith<ExplorationException> { intervalleExploration(10.0, 5.0, 1.0) }
    }
}
