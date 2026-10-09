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
    fun donneesInconnues_restentEquilibrablesEtLapportConnuEstTrace() = runTest {
        val resultat = ExplorateurMultiration(InMemoryEquationRepository()).explorer(configuration(listOf(1.0)))
        val scenario = resultat.scenarios.first()
        val valeurInconnue = ValeurNutritionnelle(
                NutrientLipid.O6,
                scenario.valeursNutritionnelles.getValue(NutrientLipid.O6.label).unite,
                1.6,
                "partiel",
                complete = false
        )
        val sousReserve = scenario.copy(
                statut = StatutScenario.CONFORME_SOUS_RESERVE,
                valeursNutritionnelles = scenario.valeursNutritionnelles + (NutrientLipid.O6.label to valeurInconnue)
        )
        val cases = ExplorateurMultiration.construireCases(listOf(sousReserve))
        assertEquals(ZoneEquilibre.EQUILIBRABLE, cases.single().zone)
        assertEquals(1, cases.single().sousReserve)
        assertEquals(1.6, apportCourbeExploration(sousReserve, NutrientLipid.O6), 1e-9)
    }

    @Test
    fun corrections_recalculentRationCarteEtSurviventAuJson() = runTest {
        val moteur = ExplorateurMultiration(InMemoryEquationRepository())
        val config = configuration(listOf(1.0))
        val initial = moteur.explorer(config)
        val cible = initial.scenarios.first()
        val lignes = cible.ration.alimentMutableList.map {
            LigneCorrectionExploration(it.aliment!!.uuid, if (it.aliment!!.uuid == proteine.uuid) 0.0 else it.quantite)
        } + LigneCorrectionExploration(proteineGrasse.uuid, 500.0)
        val correction = CorrectionRationExploration("configuration test", cleScenarioExploration(cible), lignes)
        val json = ConfigurationExplorationJson(corrections = listOf(correction)).versJson()
        val rechargees = ConfigurationExplorationJson.depuisJson(json).corrections
        assertEquals(listOf(correction), rechargees)
        val catalogue = config.listes.values.flatten().associateBy { it.uuid }
        val resultat = appliquerCorrectionsExploration(initial, "configuration test", rechargees, catalogue, moteur)
        val corrige = resultat.scenarios.first()
        assertEquals(StatutScenario.ENERGIE_DEPASSEE, corrige.statut)
        assertTrue(corrige.energie!! > cible.energie!!)
        assertEquals(corrige.ration.alimentMutableList.size, corrige.roles.size)
        assertEquals(0, resultat.cases.first().conformes)
        val rejoue = appliquerCorrectionsExploration(initial, "configuration test", rechargees, catalogue, moteur)
        assertEquals(corrige.roles, rejoue.scenarios.first().roles)
        assertEquals(corrige.energie, rejoue.scenarios.first().energie)
        val params = ParametresExploration()
        val sauvegarde = ConfigurationExplorationJson.depuisJson(params.versJson().copy(corrections = rechargees).versJson())
        assertEquals(params.versJson().versJson(), sauvegarde.versParametres(emptyMap(), emptySet()).first.versJson().versJson())
        assertEquals(1, initial.cases.first().conformes)
        assertEquals(80.0, initial.scenarios.first().ration.alimentMutableList.first().quantite)
        val autreConfig = appliquerCorrectionsExploration(initial, "autre configuration", rechargees, catalogue, moteur)
        assertEquals(initial.scenarios, autreConfig.scenarios)
        assertFailsWith<ExplorationException> {
            appliquerCorrectionsExploration(initial, "configuration test", rechargees, emptyMap(), moteur)
        }
    }

    @Test
    fun correction_sansIngredientEnergetiqueNeDecalePasLesRoles() = runTest {
        val moteur = ExplorateurMultiration(InMemoryEquationRepository())
        val config = configuration(listOf(1.0))
        val scenario = moteur.explorer(config).scenarios.first()
        val ration = scenario.ration.copy(alimentMutableList = scenario.ration.alimentMutableList
                .filterNot { it.aliment?.uuid == energie.uuid || it.aliment?.uuid == fibre.uuid }.toMutableList())
        val corrige = moteur.reevaluerScenario(scenario, ration, config)
        assertTrue(RoleExploration.ENERGIE !in corrige.roles)
        assertTrue(RoleExploration.FIBRES !in corrige.roles)
        assertEquals(80.0, corrige.energie!!, 1e-9)
        assertTrue(corrige.seuilsManques.isNotEmpty() || corrige.seuilsNonRenseignes.isNotEmpty())
    }

    @Test
    fun rationExploration_copieSansConsultationEtSansModifierLeScenario() {
        val source = Ration(
                uuid = "ration-source", idConsult = "consultation-source",
                refRationParente = "parent",
                alimentMutableList = mutableListOf(
                        AlimentRation(uuid = "aliment-source", aliment = proteine, quantite = 80.0, refRation = "ration-source"),
                        AlimentRation(aliment = energie, quantite = 0.0)
                )
        )
        val copie = copierRationExploration(source)
        assertEquals("", copie.idConsult)
        assertEquals(null, copie.refRationParente)
        assertTrue(copie.uuid != source.uuid)
        assertEquals(1, copie.alimentMutableList.size)
        assertEquals(copie.uuid, copie.alimentMutableList.first().refRation)
        assertTrue(copie.alimentMutableList.first().uuid != source.alimentMutableList.first().uuid)
        copie.alimentMutableList[0] = copie.alimentMutableList[0].copy(quantite = 20.0)
        copie.alimentMutableList.clear()
        assertEquals(2, source.alimentMutableList.size)
        assertEquals(80.0, source.alimentMutableList.first().quantite)
        assertEquals("consultation-source", source.idConsult)
    }

    @Test
    fun arrondiSuperieur_tolereUnPasEnergetiqueEtRespecteDesactivation() = runTest {
        val initiale = configuration(listOf(1.0))
        val riz = aliment("riz 399", NutrientMain.ENERGIE to 399.0)
        val config = initiale.copy(listes = initiale.listes + (RoleExploration.ENERGIE to listOf(riz)),
                arrondirAuSuperieur = true)
        val scenario = ExplorateurMultiration(InMemoryEquationRepository()).explorer(config).scenarios.first()
        assertEquals(185.0, scenario.ration.alimentMutableList.last().quantite)
        assertEquals(StatutScenario.CONFORME, scenario.statut)
        val sansArrondi = ExplorateurMultiration(InMemoryEquationRepository())
                .explorer(config.copy(arrondir = false)).scenarios.first()
        assertEquals(800.0, sansArrondi.energie!!, 1e-6)
    }

    @Test
    fun besoinsIgnores_exclusDesListesEtDeLaConformite() = runTest {
        val initiale = configuration(listOf(1.0))
        val config = initiale.copy(
                listes = initiale.listes - RoleExploration.SODIUM,
                besoinsIgnores = setOf(NutrientMacro.NA.label)
        )
        assertEquals(2L, config.nombreScenarios)
        val scenario = ExplorateurMultiration(InMemoryEquationRepository()).explorer(config).scenarios.first()
        assertTrue(RoleExploration.SODIUM !in scenario.roles)
        assertEquals(StatutScenario.CONFORME, scenario.statut)
        assertTrue(scenario.seuilsManques.none { it.contains("Sodium") })
        // Même sans seuil cible dans le référentiel, le besoin ignoré ne bloque pas le calcul.
        val supplement = RoleExploration.supplement()
        val sansCible = config.copy(listes = config.listes + (supplement to emptyList()),
                besoinsIgnores = config.besoinsIgnores + NutrientLipid.EPADHA.label)
        assertEquals(StatutScenario.CONFORME,
                ExplorateurMultiration(InMemoryEquationRepository()).explorer(sansCible).scenarios.first().statut)
    }

    @Test
    fun arrondiSuperieur_respectePasDoseMinimaleEtConditionnement() {
        val vrac = aliment("vrac")
        assertEquals(5.0, arrondirAvecDoseMinimale(vrac, 0.2, 5.0, auSuperieur = true))
        assertEquals(13.0, arrondirAvecDoseMinimale(vrac, 12.1, 0.0, auSuperieur = true))
        assertEquals(45.0, arrondirAvecDoseMinimale(vrac, 41.0, 0.0, auSuperieur = true))
        assertEquals(225.0, arrondirAvecDoseMinimale(vrac, 201.0, 0.0, auSuperieur = true))
        assertEquals(0.4, arrondirAvecDoseMinimale(vrac, 0.31, 0.1, fin = true, auSuperieur = true), 1e-9)
        assertEquals(0.0, arrondirAvecDoseMinimale(vrac, 0.0, 5.0, auSuperieur = true))
        assertEquals(20.0, arrondirAvecDoseMinimale(vrac, 20.0, 0.0, auSuperieur = true))
        val dosette = vrac.copy(cont = ContEnum.entries.first { it != ContEnum.NO }, quantInt = 4.0)
        assertEquals(6.0, arrondirAvecDoseMinimale(dosette, 0.2, 5.0, auSuperieur = true))
    }

    @Test
    fun pasArrondiManuel_estAppliqueEtEnregistre() {
        val vrac = aliment("vrac")
        assertEquals(0.25, arrondirAvecDoseMinimale(vrac, 0.31, 0.0, pasManuel = 0.25), 1e-9)
        assertEquals(0.5, arrondirAvecDoseMinimale(vrac, 0.31, 0.0, auSuperieur = true, pasManuel = 0.25), 1e-9)
        val dosette = vrac.copy(cont = ContEnum.entries.first { it != ContEnum.NO }, quantInt = 4.0)
        assertEquals(2.0, arrondirAvecDoseMinimale(dosette, 1.6, 0.0, pasManuel = 0.25), 1e-9)

        val p = ParametresExploration(pasArrondiManuel = mapOf(RoleExploration.SODIUM to 0.25))
        val restaure = ConfigurationExplorationJson.depuisJson(p.versJson().versJson())
                .versParametres(emptyMap(), emptySet()).first
        assertEquals(0.25, restaure.pasArrondiManuel[RoleExploration.SODIUM])
    }

    @Test
    fun options_sontEnregistreesEtAnciensFichiersRestentCompatibles() {
        val p = ParametresExploration(besoinsIgnores = setOf(NutrientMacro.NA.label), arrondirAuSuperieur = true)
        val restaure = ConfigurationExplorationJson.depuisJson(p.versJson().versJson())
                .versParametres(emptyMap(), emptySet()).first
        assertEquals(p, restaure)
        val ancien = ConfigurationExplorationJson.depuisJson("{}").versParametres(emptyMap(), emptySet()).first
        assertTrue(ancien.besoinsIgnores.isEmpty())
        assertEquals(false, ancien.arrondirAuSuperieur)
    }

    @Test
    fun courbes_conserventApportsEtSeuilsAbsolusDuReferentiel() = runTest {
        val resultat = ExplorateurMultiration(InMemoryEquationRepository()).explorer(configuration())
        val scenarios = resultat.scenarios.filter { it.combinaison == 1 }
        for (scenario in scenarios) {
            assertEquals(40.0, scenario.valeursNutritionnelles.getValue(NutrientMain.PROTEINE.label).valeur, 1e-9)
            assertEquals(40.0, seuilCourbeExploration(scenario, NutrientMain.PROTEINE, Reflevel.OPTIMIN)!!, 1e-9)
            assertEquals(null, seuilCourbeExploration(scenario, NutrientMain.PROTEINE, Reflevel.MIN))
        }
        val scenario = scenarios.first()
        scenario.reference.definirNutriment(1.2, NutrientMacro.CAL, Reflevel.MIN, UnitReqEnum.RATIO, BiblioRef())
        assertEquals(1.2, seuilCourbeExploration(scenario, NutrientMacro.CAL, Reflevel.MIN))
    }

    @Test
    fun sel_conserveLesPetitesDoses() = runTest {
        val sel = aliment("sel concentré", NutrientMacro.NA to 40.0)
        val config = configuration(listOf(1.0)).let {
            it.copy(listes = it.listes + (RoleExploration.SODIUM to listOf(sel)))
        }
        val scenario = ExplorateurMultiration(InMemoryEquationRepository()).explorer(config).scenarios.first()
        assertEquals(1.0, scenario.ration.alimentMutableList[scenario.roles.indexOf(RoleExploration.SODIUM)].quantite)
        assertEquals(0.3, arrondirAvecDoseMinimale(sel, 0.34, 0.1, fin = true))
    }

    @Test
    fun supplement_epaDhaEstAjusteAvantEnergieEtEnregistre() = runTest {
        val role = RoleExploration.supplement()
        val huile = aliment("huile poisson", NutrientLipid.EPADHA to 20.0, NutrientMain.ENERGIE to 900.0)
        val ref = reference().apply {
            definirNutriment(0.25, NutrientLipid.EPADHA, Reflevel.OPTIMIN, UnitReqEnum.PERKCAL, BiblioRef())
        }
        val config = configuration(listOf(1.0)).let {
            it.copy(references = listOf(ref), listes = it.listes + (role to listOf(huile)), arrondir = false)
        }
        assertEquals(2L, config.nombreScenarios)
        val scenario = ExplorateurMultiration(InMemoryEquationRepository()).explorer(config).scenarios.first()
        assertEquals(RoleExploration.ENERGIE, scenario.roles.last())
        assertEquals(1.0, scenario.ration.alimentMutableList[scenario.roles.indexOf(role)].quantite, 1e-9)
        assertEquals(800.0, scenario.energie!!, 1e-6)
        val params = ParametresExploration(
                listes = config.listes,
                cibles = config.cibles + (role to CibleExploration(NutrientLipid.EPADHA)),
                doseMinimale = config.doseMinimale + (role to 0.2)
        )
        val restored = ConfigurationExplorationJson.depuisJson(params.versJson().versJson())
                .versParametres(config.listes.values.flatten().associateBy { it.uuid }, emptySet()).first
        assertEquals(params.listes, restored.listes)
        assertEquals(params.cibles, restored.cibles)
        assertEquals(params.doseMinimale, restored.doseMinimale)
    }

    @Test
    fun explorer_ajusteSuccessivementEtClasseLesCases() = runTest {
        val resultat = ExplorateurMultiration(InMemoryEquationRepository()).explorer(configuration())
        assertEquals(4, resultat.scenarios.size)

        val s = resultat.scenarios.first { it.k == 1.0 && it.combinaison == 1 }
        val quantites = s.ration.alimentMutableList.map { it.quantite }
        // 40 g de protéines / 50 % ; 20 g de cellulose / 50 % ; 0,8 g de Ca / 20 % = 4 g → dose min. 5 g ;
        // 1,6 g d'O6 / 10 % ; 0,4 g de Na / 10 % = 4 g ; (800 − 80) kcal / 4 kcal/g = 180 g
        assertEquals(listOf(80.0, 40.0, 5.0, 16.0, 4.0, 180.0), quantites)
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
