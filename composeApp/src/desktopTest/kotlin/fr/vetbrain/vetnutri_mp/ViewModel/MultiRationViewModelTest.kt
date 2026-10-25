package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.AlimentEvLight
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.RoleExploration
import fr.vetbrain.vetnutri_mp.Data.StatutScenario
import fr.vetbrain.vetnutri_mp.Data.ZoneEquilibre
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.Nutrient
import fr.vetbrain.vetnutri_mp.Enumer.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.Repository.DatabaseBiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseEquationRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodImportResult
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Repository.InMemoryEquationRepository
import fr.vetbrain.vetnutri_mp.Repository.InMemoryMultiRationExplorationRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import java.util.concurrent.Executors
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

/** Catalogue d'aliments en mémoire : seules les lectures par identifiant sont utilisées ici. */
private class FakeFoodRepository(aliments: List<AlimentEv>) : FoodRepository {
    private val foods = aliments.associateBy { it.uuid }.toMutableMap()
    override suspend fun insert(food: AlimentEv) { foods[food.uuid] = food }
    override suspend fun update(food: AlimentEv) { foods[food.uuid] = food }
    override suspend fun delete(food: AlimentEv) { foods.remove(food.uuid) }
    override suspend fun getAllFoods(): List<AlimentEv> = foods.values.toList()
    override suspend fun getFoodById(id: String): AlimentEv? = foods[id]
    override fun observeAllFoods(): Flow<List<AlimentEv>> = flowOf(foods.values.toList())
    override suspend fun getAllFoodsLight(): List<AlimentEvLight> = emptyList()
    override suspend fun importFoods(foods: List<AlimentEvJson>, mergeNutrients: Boolean, importOnlyIfNewer: Boolean): FoodImportResult =
            FoodImportResult(0, 0, 0, 0, 0, 0)
    override suspend fun insertFood(food: AlimentEv) { foods[food.uuid] = food }
    override suspend fun getFood(uuid: String): AlimentEv? = foods[uuid]
    override suspend fun deleteFood(uuid: String) { foods.remove(uuid) }
    override suspend fun updateFood(food: AlimentEv) { foods[food.uuid] = food }
    override suspend fun clearAllFoods(): Int = foods.size.also { foods.clear() }
    override suspend fun getFoodsCount(): Int = foods.size
    override suspend fun getDistinctNutrientLabels(): List<String> =
            foods.values.flatMap { it.valMap.keys.map { n -> n.label } }.distinct()
}

/**
 * [MultiRationViewModel] sur une base Room en mémoire (référentiels, équations, bibliographie)
 * et un catalogue d'aliments en mémoire : filtrage des référentiels par espèce, calcul d'une
 * exploration (valeurs attendues identiques à celles du moteur pur, cf.
 * `ExplorationMultirationTest.explorer_ajusteSuccessivementEtClasseLesCases`), enregistrement et
 * rechargement d'une configuration nommée, et annulation d'un calcul remplacé par un autre.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MultiRationViewModelTest {

    private val thread = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private lateinit var db: AppDatabase
    private lateinit var vm: MultiRationViewModel
    private val explorations = InMemoryMultiRationExplorationRepository()

    // --- Fixture identique au test du moteur pur ------------------------------------------------

    private val bib = BiblioRef(uuid = "bib-test", firstAuthor = "Test", year = 2024, completeRef = "Référence de test")
    private val equationBW = Equation(uuid = "eq-bw", name = "BW", equationScript = "BW ^ 0.75", bib = bib)
    /** BEE = 100 × BW^0.75 : 800 kcal pour 16 kg. */
    private val equationBEE = Equation(uuid = "eq-bee", name = "BEE", equationScript = "100 * BW ^ 0.75", bib = bib)

    private fun reference(uuid: String, nom: String, espece: Espece, maladie: Boolean = false): ReferenceEv =
            ReferenceEv(uuid = uuid, nom = nom, espece = espece, maladie = maladie).apply {
                equationBW = this@MultiRationViewModelTest.equationBW
                equationBEE = this@MultiRationViewModelTest.equationBEE
                fun seuil(n: Nutrient, niveau: Reflevel, valeur: Double) =
                        definirNutriment(valeur, n, niveau, UnitReqEnum.PERKCAL, bib)
                seuil(NutrientMain.PROTEINE, Reflevel.OPTIMIN, 50.0) // 40 g
                seuil(NutrientMain.PROTEINE, Reflevel.MAX, 200.0) // 160 g
                seuil(NutrientMain.CELLULOSE, Reflevel.OPTIMIN, 5.0) // 4 g, ajusté × 5 = 20 g
                seuil(NutrientMacro.CAL, Reflevel.OPTIMIN, 1.0) // 0,8 g
                seuil(NutrientLipid.O6, Reflevel.OPTIMIN, 2.0) // 1,6 g
                seuil(NutrientMacro.NA, Reflevel.OPTIMIN, 0.5) // 0,4 g
            }

    private val refChien = "ref-chien"
    private val refChat = "ref-chat"
    private val refMaladie = "ref-maladie"

    private fun aliment(nom: String, vararg valeurs: Pair<Nutrient, Double>): AlimentEv =
            AlimentEv(uuid = nom, nom = nom).apply { valeurs.forEach { (n, v) -> setNutrient(n, v) } }

    private val proteine = aliment("viande", NutrientMain.PROTEINE to 50.0, NutrientMain.ENERGIE to 100.0)
    private val proteineGrasse = aliment("viande grasse", NutrientMain.PROTEINE to 10.0, NutrientMain.ENERGIE to 500.0)
    private val fibre = aliment("son", NutrientMain.CELLULOSE to 50.0)
    private val calcium = aliment("carbonate", NutrientMacro.CAL to 20.0)
    private val omega6 = aliment("huile", NutrientLipid.O6 to 10.0)
    private val sodium = aliment("sel", NutrientMacro.NA to 10.0)
    private val energie = aliment("riz", NutrientMain.ENERGIE to 400.0)
    private val catalogue = listOf(proteine, proteineGrasse, fibre, calcium, omega6, sodium, energie)

    @BeforeTest
    fun setUp() {
        AppDispatchers.setDispatchers(io = thread, default = thread, main = thread)
        Dispatchers.setMain(thread)
        db = Room.inMemoryDatabaseBuilder<AppDatabase>().setDriver(BundledSQLiteDriver()).build()
    }

    @AfterTest
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
        AppDispatchers.resetDispatchers()
        thread.close()
    }

    /** Attend (au plus 20 s, horloge réelle) qu'une condition observée sur le ViewModel soit vraie. */
    private suspend fun attendre(message: String, condition: () -> Boolean) {
        val limite = System.currentTimeMillis() + 20_000
        while (!condition()) {
            if (System.currentTimeMillis() > limite) fail("Délai dépassé : $message (erreur VM : ${vm.erreur.value})")
            delay(20)
        }
    }

    /** Alimente la base, construit le ViewModel, charge les référentiels puis exécute [block]. */
    private fun run(block: suspend CoroutineScope.() -> Unit) = runBlocking(thread) {
        DatabaseBiblioRefRepository(db.biblioRefDao()).insertBiblioRef(bib)
        val equations = DatabaseEquationRepository(db.equationDao(), db.biblioRefDao())
        equations.saveEquation(equationBW)
        equations.saveEquation(equationBEE)
        val references = DatabaseReferenceEvRepository(db.referenceEvDao(), db.equationDao(), db.biblioRefDao())
        references.saveReferenceEv(reference(refChien, "Adulte test", Espece.CHIEN))
        references.saveReferenceEv(reference(refChat, "Chat adulte", Espece.CHAT))
        references.saveReferenceEv(reference(refMaladie, "Insuffisance rénale", Espece.CHIEN, maladie = true))

        // Comme le test du moteur pur : aucune équation nutritionnelle complémentaire en jeu
        vm = MultiRationViewModel(FakeFoodRepository(catalogue), references, InMemoryEquationRepository(), explorations)
        vm.chargerReferences()
        attendre("chargement des référentiels") { vm.references.value.size == 2 }
        block()
    }

    /** Configuration prête à calculer : référentiel chien, 16 kg, K = 1, un aliment par liste. */
    private fun configurerExplorationComplete() {
        vm.basculerReference(refChien)
        vm.modifierParametres { it.copy(poidsDe = "16", poidsA = "16", poidsPas = "1", kDe = "1", kA = "1", kPas = "1") }
        vm.ajouterAliment(RoleExploration.PROTEINES, proteine)
        vm.ajouterAliment(RoleExploration.PROTEINES, proteineGrasse)
        vm.ajouterAliment(RoleExploration.FIBRES, fibre)
        vm.ajouterAliment(RoleExploration.CALCIUM, calcium)
        vm.ajouterAliment(RoleExploration.OMEGA6, omega6)
        vm.ajouterAliment(RoleExploration.SODIUM, sodium)
        vm.ajouterAliment(RoleExploration.ENERGIE, energie)
    }

    @Test
    fun especeChoisie_filtreLesReferentielsGenerauxEtLaSelection() = run {
        // Les référentiels « maladie » ne sont jamais proposés
        assertEquals(setOf(refChien, refChat), vm.references.value.map { it.uuid }.toSet())
        assertEquals(listOf(Espece.CHIEN, Espece.CHAT), vm.especesDisponibles())

        // Espèce par défaut : chien
        assertEquals(Espece.CHIEN, vm.parametres.value.espece)
        assertEquals(listOf(refChien), vm.referencesEspece().map { it.uuid })

        vm.basculerReference(refChien)
        assertEquals(setOf(refChien), vm.parametres.value.referenceIds)

        // Changer d'espèce retire les référentiels de l'autre espèce de la sélection
        vm.choisirEspece(Espece.CHAT)
        assertEquals(Espece.CHAT, vm.parametres.value.espece)
        assertEquals(listOf(refChat), vm.referencesEspece().map { it.uuid })
        assertTrue(vm.parametres.value.referenceIds.isEmpty(), "sélection vidée des référentiels chien")
        assertTrue(vm.problemes().any { it.contains("référentiel") }, "sans référentiel, le calcul est impossible")

        vm.basculerReference(refChat)
        vm.basculerReference(refChat)
        assertTrue(vm.parametres.value.referenceIds.isEmpty(), "basculer deux fois = désélection")
    }

    @Test
    fun lancerCalcul_sansConfiguration_signaleLesProblemesSansCalculer() = run {
        vm.lancerCalcul()
        val erreur = vm.erreur.value
        assertNotNull(erreur, "les problèmes de configuration sont signalés")
        assertTrue(erreur.contains("référentiel"))
        assertTrue(erreur.contains("Listes vides"))
        assertNull(vm.resultat.value)
        assertNull(vm.progression.value)
        vm.effacerErreur()
        assertNull(vm.erreur.value)
    }

    @Test
    fun lancerCalcul_produitLesMemesRationsQueLeMoteurPur() = run {
        configurerExplorationComplete()
        assertTrue(vm.problemes().isEmpty(), "configuration prête : ${vm.problemes()}")
        assertEquals(2L, vm.nombreScenarios())

        vm.lancerCalcul()
        attendre("résultat du calcul") { vm.resultat.value != null }
        val resultat = vm.resultat.value!!
        assertNull(vm.erreur.value)
        assertNull(vm.progression.value, "progression effacée à la fin du calcul")
        assertEquals(vm.parametres.value, vm.parametresDuResultat.value)

        assertEquals(2, resultat.scenarios.size)
        val s = resultat.scenarios.first { it.combinaison == 1 }
        // 40 g de protéines / 50 % ; 20 g de cellulose / 50 % ; Ca → dose min. 5 g ; 1,6 g d'O6 / 10 % ;
        // 0,4 g de Na / 10 % ; (800 − 80) kcal / 4 kcal/g = 180 g (cf. ExplorationMultirationTest)
        assertEquals(listOf(80.0, 40.0, 5.0, 16.0, 4.0, 180.0), s.ration.alimentMutableList.map { it.quantite })
        assertEquals(800.0, s.besoinTotal!!, 1e-9)
        assertEquals(800.0, s.energie!!, 1e-6)
        assertEquals(StatutScenario.CONFORME, s.statut)
        assertEquals(refChien, s.reference.uuid)
        assertEquals(16.0, s.poids)
        assertEquals(1.0, s.k)

        val grasse = resultat.scenarios.first { it.combinaison == 2 }
        assertEquals(StatutScenario.ENERGIE_DEPASSEE, grasse.statut)

        val case = resultat.cases.single()
        assertEquals(ZoneEquilibre.EQUILIBRABLE, case.zone)
        assertEquals(1, case.conformes)
        assertEquals(StatutScenario.CONFORME, case.meilleurScenario?.statut)

        // Sélection d'une case : son meilleur scénario devient le scénario sélectionné
        vm.selectionnerCase(case)
        assertEquals(case.meilleurScenario?.id, vm.scenarioSelectionne.value?.id)

        // Export CSV : une ligne par scénario, en-tête compris
        assertEquals(3, vm.csvScenarios().lines().size)
        assertEquals(2, vm.csvCases().lines().size)
    }

    @Test
    fun enregistrerExploration_rechargeeDepuisLeRepository() = run {
        configurerExplorationComplete()
        val parametresInitiaux = vm.parametres.value

        vm.enregistrerExploration("  ")
        assertNotNull(vm.erreur.value, "un nom vide est refusé")
        vm.effacerErreur()

        vm.enregistrerExploration("Chien adulte 16 kg")
        attendre("enregistrement") { vm.explorationCourante.value != null }
        val courante = vm.explorationCourante.value!!
        assertEquals("Chien adulte 16 kg", courante.nom)
        assertEquals(Espece.CHIEN.name, courante.espece)
        assertEquals(1, explorations.getAllExplorations().size, "une configuration en base")
        attendre("liste des explorations") { vm.explorations.value.size == 1 }
        assertTrue(vm.message.value?.contains("enregistrée") == true)

        // Nouvelle exploration : configuration vide, plus de configuration courante
        vm.nouvelleExploration()
        assertNull(vm.explorationCourante.value)
        assertTrue(vm.parametres.value.referenceIds.isEmpty())
        assertTrue(vm.parametres.value.listes.values.all { it.isEmpty() })

        // Rechargement : référentiels et aliments retrouvés par identifiant
        vm.chargerExploration(courante.uuid)
        attendre("rechargement") { vm.explorationCourante.value?.uuid == courante.uuid }
        val rechargee = vm.parametres.value
        assertEquals(parametresInitiaux.referenceIds, rechargee.referenceIds)
        assertEquals(parametresInitiaux.poidsDe, rechargee.poidsDe)
        assertEquals(parametresInitiaux.kA, rechargee.kA)
        assertEquals(
                parametresInitiaux.listes.mapValues { (_, l) -> l.map { it.uuid } },
                rechargee.listes.mapValues { (_, l) -> l.map { it.uuid } }
        )
        assertTrue(vm.message.value?.contains("chargée") == true)
        assertTrue(vm.message.value?.contains("Introuvables") != true, "tous les identifiants sont retrouvés")

        // Mise à jour sous le même identifiant, puis « enregistrer sous » un nouveau
        vm.enregistrerExploration("Chien adulte 16 kg (v2)")
        attendre("mise à jour") { vm.explorationCourante.value?.nom == "Chien adulte 16 kg (v2)" }
        assertEquals(courante.uuid, vm.explorationCourante.value?.uuid)
        assertEquals(1, explorations.getAllExplorations().size)
        vm.enregistrerExploration("Copie", commeNouvelle = true)
        attendre("copie") { explorations.getAllExplorations().size == 2 }
        assertTrue(vm.explorationCourante.value?.uuid != courante.uuid)

        // Suppression
        vm.supprimerExploration(courante.uuid)
        attendre("suppression") { explorations.getAllExplorations().size == 1 }
        assertEquals("Copie", explorations.getAllExplorations().single().nom)
    }

    @Test
    fun nouveauCalcul_annuleLeCalculEnCours() = run {
        configurerExplorationComplete()
        // Premier calcul (grille A : un seul poids) immédiatement remplacé par un second (grille B :
        // deux poids) avant même d'avoir démarré : seul le résultat de B doit être publié.
        vm.lancerCalcul()
        vm.modifierParametres { it.copy(poidsDe = "16", poidsA = "20", poidsPas = "4") }
        val parametresB = vm.parametres.value
        vm.lancerCalcul()

        attendre("résultat de B") { vm.resultat.value != null }
        assertEquals(parametresB, vm.parametresDuResultat.value, "le résultat publié est celui du dernier calcul")
        assertEquals(4, vm.resultat.value!!.scenarios.size, "2 poids × 2 combinaisons")
        assertEquals(setOf(16.0, 20.0), vm.resultat.value!!.scenarios.map { it.poids }.toSet())
        assertNull(vm.erreur.value)

        // Un calcul annulé explicitement ne remplace pas le résultat précédent
        vm.modifierParametres { it.copy(poidsDe = "16", poidsA = "16", poidsPas = "1") }
        vm.lancerCalcul()
        vm.annulerCalcul()
        delay(500)
        assertNull(vm.progression.value)
        assertEquals(parametresB, vm.parametresDuResultat.value, "résultat inchangé après annulation")
        assertEquals(4, vm.resultat.value!!.scenarios.size)
    }
}
