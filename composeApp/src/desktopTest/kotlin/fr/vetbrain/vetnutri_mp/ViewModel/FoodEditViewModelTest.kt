package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentEvJson
import fr.vetbrain.vetnutri_mp.Data.AlimentEvLight
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.Enumer.CustomNutrient
import fr.vetbrain.vetnutri_mp.Enumer.CustomNutrientRegistry
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumer.NutrientVitam
import fr.vetbrain.vetnutri_mp.Repository.FoodImportResult
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Repository.InMemoryBiblioRefRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import java.util.concurrent.Executors
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** Catalogue d'aliments en mémoire qui trace les insertions et les mises à jour. */
private class FakeFoodRepositoryForEdit(aliments: List<AlimentEv> = emptyList()) : FoodRepository {
    private val foods = aliments.associateBy { it.uuid }.toMutableMap()
    private val flux = MutableStateFlow(foods.values.toList())
    val inserts = mutableListOf<String>()
    val updates = mutableListOf<String>()
    var labelsSupplementaires: List<String> = emptyList()

    private fun publier() { flux.value = foods.values.toList() }
    override suspend fun insert(food: AlimentEv) = insertFood(food)
    override suspend fun update(food: AlimentEv) = updateFood(food)
    override suspend fun delete(food: AlimentEv) = deleteFood(food.uuid)
    override suspend fun getAllFoods(): List<AlimentEv> = foods.values.toList()
    override suspend fun getFoodById(id: String): AlimentEv? = foods[id]
    override fun observeAllFoods(): Flow<List<AlimentEv>> = flux
    override suspend fun getAllFoodsLight(): List<AlimentEvLight> = emptyList()
    override suspend fun importFoods(foods: List<AlimentEvJson>, mergeNutrients: Boolean, importOnlyIfNewer: Boolean): FoodImportResult =
            FoodImportResult(0, 0, 0, 0, 0, 0)
    override suspend fun insertFood(food: AlimentEv) { inserts += food.uuid; foods[food.uuid] = food; publier() }
    override suspend fun getFood(uuid: String): AlimentEv? = foods[uuid]
    override suspend fun deleteFood(uuid: String) { foods.remove(uuid); publier() }
    override suspend fun updateFood(food: AlimentEv) { updates += food.uuid; foods[food.uuid] = food; publier() }
    override suspend fun clearAllFoods(): Int = foods.size.also { foods.clear(); publier() }
    override suspend fun getFoodsCount(): Int = foods.size
    override suspend fun getDistinctNutrientLabels(): List<String> =
            (foods.values.flatMap { it.valMap.keys.map { n -> n.label } } + labelsSupplementaires).distinct()
}

/**
 * [FoodEditViewModel] sur un catalogue en mémoire : chargement d'un aliment existant (état,
 * bibliographie, nutriments personnalisés), enregistrement (mise à jour vs insertion, date,
 * bibliographie sélectionnée), liste des nutriments avec les nutriments personnalisés de la base,
 * et règles des nutriments personnalisés (nom vide, unité inconnue, doublon).
 *
 * Le ViewModel n'effectue pas de validation de saisie (nom vide, valeur négative) : ces contrôles
 * sont faits par l'écran, ils ne sont donc pas testés ici.
 */
class FoodEditViewModelTest {

    private val thread = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val labelsAjoutes = mutableSetOf<String>()

    /** Nutriment personnalisé porté par l'aliment existant. */
    private val taurine = CustomNutrient.fromLabel("Taurine spéciale")
    private val bib = BiblioRef(uuid = "bib-1", firstAuthor = "Dupont", year = 2020, completeRef = "Dupont 2020")

    private fun croquettes(): AlimentEv =
            AlimentEv(uuid = "croq", nom = "Croquettes adulte", brand = "Marque", biblioRefs = listOf(bib)).apply {
                setNutrient(NutrientMain.PROTEINE, 25.0)
                setNutrient(NutrientMain.ENERGIE, 350.0)
                setNutrient(taurine, 0.5)
            }

    @BeforeTest
    fun setUp() {
        AppDispatchers.setDispatchers(io = thread, default = thread, main = thread)
        labelsAjoutes += taurine.label
    }

    @AfterTest
    fun tearDown() {
        // Le registre des nutriments personnalisés est global : ne rien laisser derrière soi
        labelsAjoutes.forEach { CustomNutrientRegistry.removeByLabel(it) }
        AppDispatchers.resetDispatchers()
        thread.close()
    }

    private suspend fun idle() {
        repeat(100) { yield() }
        delay(100)
    }

    private fun run(block: suspend CoroutineScope.() -> Unit) = runBlocking(thread) { block() }

    @Test
    fun chargementAlimentExistant_rempliEtatBibliographieEtNutriments() = run {
        val repo = FakeFoodRepositoryForEdit(listOf(croquettes()))
        val vm = FoodEditViewModel(repo, "croq", InMemoryBiblioRefRepository().also { it.insertBiblioRef(bib) })
        idle()

        val etat = vm.alimentState.value
        assertEquals("croq", etat.uuid)
        assertEquals("Croquettes adulte", etat.nom)
        assertEquals("Marque", etat.brand)
        assertEquals(25.0, etat.valMap[NutrientMain.PROTEINE]?.value)
        assertEquals(listOf(bib), vm.selectedBiblioRefs.value, "les références de l'aliment sont sélectionnées")
        assertEquals(listOf(bib), vm.availableBiblioRefs.value, "les références disponibles sont observées")

        val nutriments = vm.getAllNutrients()
        assertTrue(nutriments.containsAll(NutrientMain.entries), "nutriments principaux")
        assertTrue(nutriments.containsAll(NutrientMin.entries), "minéraux")
        assertTrue(nutriments.containsAll(NutrientVitam.entries), "vitamines")
        assertTrue(nutriments.any { it.label == taurine.label }, "le nutriment personnalisé de l'aliment est proposé")
        assertEquals(nutriments.size, nutriments.map { it.label }.distinct().size, "aucun doublon de label")
        assertNotNull(CustomNutrientRegistry.getByLabel(taurine.label), "enregistré dans le registre de session")
        vm.clear()
    }

    @Test
    fun loadNutrients_inclutLesNutrimentsPersonnalisesDeLaBase() = run {
        val repo = FakeFoodRepositoryForEdit().apply { labelsSupplementaires = listOf("Polyphénols totaux") }
        val vm = FoodEditViewModel(repo)
        idle()
        val custom = CustomNutrient.fromLabel("Polyphénols totaux")
        labelsAjoutes += custom.label
        assertTrue(
                vm.getAllNutrients().any { it.label == custom.label },
                "un label inconnu présent en base devient un nutriment personnalisé proposé à l'édition"
        )
        assertTrue(vm.getAllNutrients().contains(NutrientMain.PROTEINE))
        vm.clear()
    }

    @Test
    fun saveAliment_existant_metAJourAvecDateEtBibliographie() = run {
        val repo = FakeFoodRepositoryForEdit(listOf(croquettes()))
        val vm = FoodEditViewModel(repo, "croq")
        idle()
        val bib2 = BiblioRef(uuid = "bib-2", firstAuthor = "Martin", year = 2021, completeRef = "Martin 2021")
        vm.addBiblioRef(bib2)
        vm.addBiblioRef(bib2)
        assertEquals(listOf(bib, bib2), vm.selectedBiblioRefs.value, "pas de doublon de référence")
        vm.removeBiblioRef(bib)
        assertEquals(listOf(bib2), vm.selectedBiblioRefs.value)

        // Édition : nouveau nom et protéines à 30 %
        val modifie = AlimentEv(uuid = "croq", nom = "Croquettes modifiées", brand = "Marque").apply {
            setNutrient(NutrientMain.PROTEINE, 30.0)
            setNutrient(NutrientMain.ENERGIE, 350.0)
        }
        vm.saveAliment(modifie)

        assertEquals(listOf("croq"), repo.updates, "un aliment existant est mis à jour")
        assertTrue(repo.inserts.isEmpty(), "jamais inséré une seconde fois")
        val enBase = repo.getFood("croq")!!
        assertEquals("Croquettes modifiées", enBase.nom)
        assertEquals(30.0, enBase.valMap[NutrientMain.PROTEINE]?.value)
        assertEquals(listOf(bib2), enBase.biblioRefs, "la bibliographie enregistrée est celle sélectionnée")
        assertEquals(Clock.System.todayIn(TimeZone.currentSystemDefault()).toString(), enBase.lastUpdateDate)
        assertEquals("Croquettes modifiées", vm.alimentState.value.nom, "l'état local suit l'enregistrement")
        vm.clear()
    }

    @Test
    fun saveAliment_nouveau_insereDansLeRepository() = run {
        val repo = FakeFoodRepositoryForEdit()
        val vm = FoodEditViewModel(repo)
        idle()
        val defaut = vm.alimentState.value
        assertTrue(defaut.uuid.isNotBlank(), "un identifiant est généré pour un nouvel aliment")
        assertNull(repo.getFood(defaut.uuid))

        val nouveau = AlimentEv(uuid = defaut.uuid, nom = "Nouveau").apply { setNutrient(NutrientMain.PROTEINE, 12.0) }
        vm.saveAliment(nouveau)

        assertEquals(listOf(defaut.uuid), repo.inserts)
        assertTrue(repo.updates.isEmpty())
        assertEquals("Nouveau", repo.getFood(defaut.uuid)?.nom)
        assertEquals(12.0, repo.getFood(defaut.uuid)?.valMap?.get(NutrientMain.PROTEINE)?.value)
        assertEquals(1, repo.getFoodsCount())
        vm.clear()
    }

    @Test
    fun addOrGetCustomNutrient_refuseVideUniteInconnueEtDoublon() = run {
        val vm = FoodEditViewModel(FakeFoodRepositoryForEdit())
        idle()
        assertNull(vm.addOrGetCustomNutrient("   ", "g"), "nom vide refusé")
        assertNull(vm.addOrGetCustomNutrient("Lycopene", ""), "unité vide refusée")
        assertNull(vm.addOrGetCustomNutrient("Lycopene", "unité-inconnue"), "unité inconnue refusée")
        assertNull(vm.addOrGetCustomNutrient(NutrientMain.PROTEINE.label, "g"), "doublon d'un nutriment standard refusé")

        val cree = vm.addOrGetCustomNutrient("Lycopene", "mg")
        assertNotNull(cree, "nutriment personnalisé créé")
        labelsAjoutes += cree.label
        assertTrue(vm.getAllNutrients().any { it.label == cree.label })
        assertNull(vm.addOrGetCustomNutrient("Lycopene", "mg"), "doublon refusé")

        assertTrue(vm.deleteCustomNutrient(cree as CustomNutrient))
        assertTrue(vm.getAllNutrients().none { it.label == cree.label })
        vm.clear()
    }
}
