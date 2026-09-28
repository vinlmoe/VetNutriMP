package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.DataBase.*
import fr.vetbrain.vetnutri_mp.Enumer.*
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Repository.*
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage
import java.lang.reflect.Proxy
import java.util.concurrent.Executors
import kotlin.math.pow
import kotlin.test.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOf

/** DAO de consultations en mémoire, avec les suppressions en cascade de SQLite. */
private class FakeConsultationDao : ConsultationDao {
    val consultations = linkedMapOf<String, ConsultationEntity>()
    val rations = linkedMapOf<String, RationEntity>()
    val aliments = linkedMapOf<String, AlimentRationEntity>()
    val suppVars = mutableListOf<SupplementalVariableEntity>()
    val rationVars = mutableListOf<RationSupplementalVariableEntity>()
    var saves = 0
    // Latence simulée (ms aléatoires) : permet aux enregistrements concurrents de se chevaucher
    @Volatile var latence = false
    private suspend fun attendre() { if (latence) delay((0L..15L).random()) }

    override suspend fun insert(consultation: ConsultationEntity) { consultations[consultation.uuid] = consultation }
    override suspend fun update(consultation: ConsultationEntity) { attendre(); synchronized(this) { consultations[consultation.uuid] = consultation; saves++ } }
    override suspend fun delete(consultation: ConsultationEntity) {
        consultations.remove(consultation.uuid); deleteRationsForConsultation(consultation.uuid)
        deleteSupplementalVariablesForConsultation(consultation.uuid)
    }
    override suspend fun getConsultationsForAnimal(animalId: String) = consultations.values.filter { it.idAnim == animalId }
    override suspend fun getConsultationById(id: String) = consultations[id]
    override suspend fun getSupplementalVariablesForConsultation(consultationId: String) = suppVars.filter { it.idConsult == consultationId }
    override suspend fun getRationsForConsultation(consultationId: String) = synchronized(this) { rations.values.toList() }.filter { it.idConsult == consultationId }
    override suspend fun getAlimentsForRation(rationId: String) = synchronized(this) { aliments.values.toList() }.filter { it.refRation == rationId }
    override suspend fun getSupplementalVariablesForRation(rationId: String) = rationVars.filter { it.idRation == rationId }
    override suspend fun insertRationSupplementalVariable(supplementalVariable: RationSupplementalVariableEntity) {
        require(rations.containsKey(supplementalVariable.idRation)) { "FK RATIONS" }
        rationVars.removeAll { it.idRation == supplementalVariable.idRation && it.variableKind == supplementalVariable.variableKind }
        rationVars.add(supplementalVariable)
    }
    override suspend fun insertSupplementalVariable(supplementalVariable: SupplementalVariableEntity) {
        suppVars.removeAll { it.idConsult == supplementalVariable.idConsult && it.variableKind == supplementalVariable.variableKind }
        suppVars.add(supplementalVariable)
    }
    override suspend fun insertRation(ration: RationEntity) { attendre(); synchronized(this) { rations[ration.uuid] = ration } }
    override suspend fun insertAlimentRation(aliment: AlimentRationEntity) { attendre(); synchronized(this) { aliments[aliment.uuid] = aliment } }
    override suspend fun deleteRationsForConsultation(consultationId: String): Unit = synchronized(this) {
        val ids = rations.values.filter { it.idConsult == consultationId }.map { it.uuid }.toSet()
        ids.forEach { rations.remove(it) }
        aliments.values.removeAll { it.refRation in ids }          // CASCADE ALIMENTS
        rationVars.removeAll { it.idRation in ids }                  // CASCADE RATION_SUPPLEMENTAL_VARIABLES
    }
    override suspend fun deleteSupplementalVariablesForConsultation(consultationId: String) { suppVars.removeAll { it.idConsult == consultationId } }
    override suspend fun getAllConsultationKeywords() = emptyList<ConsultationKeywordEntity>()
    override suspend fun getConsultationKeywordByLabel(label: String): ConsultationKeywordEntity? = null
    override suspend fun insertConsultationKeyword(keyword: ConsultationKeywordEntity) {}
}

/** Proxy d'interface : répond aux méthodes nommées, valeurs neutres sinon (suspend compris). */
@Suppress("UNCHECKED_CAST")
private fun <T> proxy(cls: Class<T>, handlers: Map<String, (Array<Any?>?) -> Any?>): T =
    Proxy.newProxyInstance(cls.classLoader, arrayOf(cls)) { p, m, args ->
        handlers[m.name]?.invoke(args) ?: when {
            m.name == "toString" -> "proxy"
            m.name == "hashCode" -> System.identityHashCode(p)
            m.name == "equals" -> p === args?.get(0)
            m.name.startsWith("get") || m.name.startsWith("search") -> when (m.returnType) {
                java.util.List::class.java -> emptyList<Any>()
                java.util.Map::class.java -> emptyMap<Any, Any>()
                else -> if (m.returnType.name == "kotlinx.coroutines.flow.Flow") flowOf(emptyList<Any>()) else null
            }
            m.name.startsWith("observe") -> flowOf(emptyList<Any>())
            else -> if (m.returnType == java.lang.Boolean.TYPE) false else if (m.returnType == Integer.TYPE) 0 else Unit
        }
    } as T

/**
 * Interactions de bout en bout du plan évolutif sur le vrai [AnimalDetailViewModel] : création du
 * plan, ajout/modification/suppression d'étapes, besoins recalculés au poids et aux variables de
 * l'étape, persistance (vrai DatabaseConsultationRepository sur un DAO en mémoire avec cascades),
 * propagation des aliments, rafales d'éditions (E/S parallèles et latence) et ordonnance.
 */
class PlanEvolutifInteractionTest {
    private val thread = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val dao = FakeConsultationDao()
    private val refId = "ref-croissance"
    private val beeScript = "(254-135*BW/AW)*BW^0.75"

    private fun bee(bw: Double, aw: Double) = (254 - 135 * bw / aw) * bw.pow(0.75)

    private fun referenceRepository(): DatabaseReferenceEvRepository {
        val ctor = DatabaseReferenceEvRepository::class.java.constructors.single()
        val t = ctor.parameterTypes
        val refEntity = ReferenceEvEntity(refId, "Croissance", "", false, "", "", 1, "CHIEN", "ADULTE", "", "", "", "", "")
        val relations = listOf(
            ReferenceEvEquationEntity(refId, "eqBW", "BW"),
            ReferenceEvEquationEntity(refId, "eqBEE", "BEE")
        )
        fun eq(id: String, script: String) = EquationEntity(
            uuid = id, name = id, description = "", equationScript = script, specie = "CHIEN",
            kind = "ENERGYNEED", consistent = true, bibRef = null, variables = "[]", nutrient = null, ratio = false)
        val refDao = proxy(t[0], mapOf(
            "getReferenceEvById" to { a -> if (a!![0] == refId) refEntity else null },
            "getAllReferenceEv" to { _ -> listOf(refEntity) },
            "getEquationsForReference" to { a -> if (a!![0] == refId) relations else emptyList() },
            "getCoefficientsForReference" to { _ -> emptyList<Any>() },
            "getNutrientsForReference" to { _ -> emptyList<Any>() }))
        val eqDao = proxy(t[1], mapOf("getEquationById" to { a ->
            when (a!![0]) { "eqBW" -> eq("eqBW", "BW^0.75"); "eqBEE" -> eq("eqBEE", beeScript); else -> null } }))
        val biblioDao = proxy(t[2], emptyMap())
        return ctor.newInstance(refDao, eqDao, biblioDao) as DatabaseReferenceEvRepository
    }

    private lateinit var vm: AnimalDetailViewModel
    private lateinit var repo: DatabaseConsultationRepository
    private val croq = AlimentEv(uuid = "croq", nom = "Croquettes")
    private val huile = AlimentEv(uuid = "huile", nom = "Huile")

    private var ioParallele = false
    private suspend fun idle() { if (ioParallele) delay(1500) else repeat(200) { yield() } }

    private fun run(parallele: Boolean = false, block: suspend CoroutineScope.() -> Unit) = runBlocking(thread) {
        ioParallele = parallele
        AppDispatchers.setDispatchers(io = if (parallele) Dispatchers.IO else thread, default = thread, main = thread)
        val foods = proxy(FoodRepository::class.java, mapOf(
            "getFoodsByUuids" to { a -> (a!![0] as List<*>).mapNotNull { id -> listOf(croq, huile).firstOrNull { it.uuid == id } }.associateBy { it.uuid } }))
        repo = DatabaseConsultationRepository(dao, foods)
        val animals = InMemoryAnimalRepository()
        val animal = AnimalEv(uuid = "a", nom = "Rex")
        animals.saveAnimal(animal)
        vm = AnimalDetailViewModel(repo, animals, referenceRepository(), PreferencesRepository(PreferencesStorage()), foods)
        vm.setAnimal(animal); idle()
        // Comme l'écran (collectAsState) : les StateFlow « WhileSubscribed » doivent être collectés
        val abonnements = listOf(vm.poidsEffectif, vm.besoinEnergetiqueStandard, vm.poidsMetabolique,
            vm.referenceUtilisee, vm.besoinEnergetiqueTotal).map { f -> launch { f.collect {} } }
        idle()
        block()
        idle()
        abonnements.forEach { it.cancel() }
    }

    private suspend fun consultationEnBase(idealWeight: Double? = null): ConsultationEv {
        val c = ConsultationEv(
            uuid = "c", idAnim = "a", weight = 5.0, idealWeight = idealWeight,
            referenceGeneraleId = refId,
            suppVarp = mutableListOf(SupplementalvariableP(VariableKind.AdultWeight, 25.0)),
            rations = mutableListOf(Ration(uuid = "actuelle", idConsult = "c", name = "Actuelle", actual = true,
                alimentMutableList = mutableListOf(AlimentRation(uuid = "a1", refAlimUnif = "croq", quantite = 100.0, aliment = croq))))
        )
        repo.saveConsultation(c)
        vm.selectConsultation(c); idle()
        return c
    }

    private fun consultation() = vm.selectedConsultation.value!!
    private fun ration(uuid: String) = consultation().rations.first { it.uuid == uuid }
    private fun etapes() = consultation().etapesEvolutives.sortedBy { it.poids ?: 0.0 }
    private fun assertProche(attendu: Double, obtenu: Double?, msg: String) =
        assertTrue(obtenu != null && kotlin.math.abs(attendu - obtenu) < 1e-6, "$msg : attendu $attendu, obtenu $obtenu")

    @Test
    fun rationSansPlan_inchangee() = run {
        consultationEnBase(idealWeight = 6.0)
        assertEquals("actuelle", vm.selectedRation.value?.uuid)
        assertProche(6.0, vm.poidsEffectif.value, "poids idéal prioritaire hors plan")
        assertProche(bee(6.0, 25.0), vm.besoinEnergetiqueStandard.value, "BEE (AW injecté sous son nom d'équation)")
        assertEquals(TypeConsultation.STANDARD, consultation().typeConsultation)
    }

    @Test
    fun planSousUneRation_deBoutEnBout() = run {
        consultationEnBase()
        // 1. Première étape ajoutée sous la ration : le plan existe sans changer le type de consultation.
        vm.ajouterEtape(ration("actuelle"), 8.0, emptyList()); idle()
        val e8 = etapes().single()
        assertEquals("actuelle", e8.refRationParente)
        assertEquals(PlanEvolutif.nomAutomatique(8.0, emptyList()), e8.name)
        assertEquals(listOf(100.0), e8.alimentMutableList.map { it.quantite })
        assertEquals(e8.uuid, vm.selectedRation.value?.uuid)
        assertEquals(TypeConsultation.STANDARD, consultation().typeConsultation)
        assertProche(8.0, vm.poidsEffectif.value, "étape 8 kg")
        assertProche(bee(8.0, 25.0), vm.besoinEnergetiqueStandard.value, "BEE 8 kg")

        // 2. Retour sur la ration parente : poids de la consultation
        vm.selectRation(ration("actuelle")); idle()
        assertProche(5.0, vm.poidsEffectif.value, "ration parente")
        assertProche(bee(5.0, 25.0), vm.besoinEnergetiqueStandard.value, "BEE ration parente")

        // 3. Édition de l'étape : poids + variable ; le nom fixe suit
        vm.selectRation(ration(e8.uuid)); idle()
        val aw30 = listOf(SupplementalvariableP(VariableKind.AdultWeight, 30.0))
        vm.mettreAJourEtape(ration(e8.uuid), 10.0, aw30); idle()
        assertEquals(PlanEvolutif.nomAutomatique(10.0, aw30), ration(e8.uuid).name)
        assertProche(10.0, vm.poidsEffectif.value, "poids modifié")
        assertProche(bee(10.0, 30.0), vm.besoinEnergetiqueStandard.value, "BEE avec AW de l'étape")

        // 4. Persistance
        val relue = repo.getConsultationById("c")!!
        val e10Relue = relue.rations.first { it.uuid == e8.uuid }
        assertEquals(10.0, e10Relue.poids)
        assertEquals("actuelle", e10Relue.refRationParente)
        assertEquals(aw30, e10Relue.suppVarp)
        assertEquals(TypeConsultation.STANDARD, relue.typeConsultation)

        // 5. Deuxième étape ; bilans de toutes les rations du plan
        vm.ajouterEtape(ration("actuelle"), 3.0, emptyList()); idle()
        val e3 = etapes().first()
        assertEquals(listOf(e3.uuid, "actuelle", e8.uuid), PlanEvolutif.planTrie(consultation(), ration("actuelle")).map { it.uuid })
        val bilans = vm.bilansEtapes.value
        assertEquals(setOf("actuelle", e3.uuid, e8.uuid), bilans.keys)
        assertProche(bee(10.0, 30.0), bilans[e8.uuid]?.besoinTotal, "bilan 10 kg")
        assertProche(bee(3.0, 25.0), bilans[e3.uuid]?.besoinTotal, "bilan 3 kg")
        assertProche(bee(5.0, 25.0), bilans["actuelle"]?.besoinTotal, "bilan ration parente")

        // 6. Propagation depuis la ration parente
        val parentAvecHuile = ration("actuelle").let {
            it.copy(alimentMutableList = (it.alimentMutableList + AlimentRation(uuid = "h1", refAlimUnif = "huile", quantite = 5.0, aliment = huile)).toMutableList())
        }
        vm.updateRationInConsultation(parentAvecHuile); idle()
        assertEquals(2, vm.propagerAlimentsEtape(ration("actuelle"))); idle()
        assertEquals(0.0, repo.getConsultationById("c")!!.rations.first { it.uuid == e8.uuid }
            .alimentMutableList.first { it.refAlimUnif == "huile" }.quantite)

        // 7. Rafale d'éditions depuis la synthèse (sans attendre)
        vm.mettreAJourQuantiteEtape(ration(e8.uuid), "croq", 150.0)
        vm.mettreAJourQuantiteEtape(ration(e8.uuid), "huile", 7.0)
        vm.mettreAJourQuantiteEtape(ration("actuelle"), "croq", 90.0)
        idle()
        val apresRafale = repo.getConsultationById("c")!!
        assertEquals(mapOf<String?, Double>("croq" to 150.0, "huile" to 7.0),
            apresRafale.rations.first { it.uuid == e8.uuid }.alimentMutableList.associate { it.refAlimUnif to it.quantite })
        assertEquals(90.0, apresRafale.rations.first { it.uuid == "actuelle" }.alimentMutableList.first { it.refAlimUnif == "croq" }.quantite)

        // 8. Poids d'étape et poids réel ensemble (résumé métabolique) : un seul enregistrement
        vm.selectRation(ration(e8.uuid)); idle()
        vm.mettreAJourEtape(ration(e8.uuid), 12.0, aw30, poidsReel = 6.0); idle()
        val apresPoids = repo.getConsultationById("c")!!
        assertEquals(6.0, apresPoids.weight)
        assertEquals(12.0, apresPoids.rations.first { it.uuid == e8.uuid }.poids)
        assertProche(bee(12.0, 30.0), vm.besoinEnergetiqueStandard.value, "BEE après double modification")
        assertProche(bee(6.0, 25.0), vm.bilansEtapes.value["actuelle"]?.besoinTotal, "la ration parente suit le poids réel")

        // 9. Groupes d'analyse : les étapes en sont exclues
        assertEquals(listOf("actuelle"), RationAggregator.rationsDuGroupe(consultation(), RationAnalysisScope.GROUPE_ACTUELLES).map { it.uuid })
        assertEquals(emptyList<String>(), RationAggregator.rationsDuGroupe(consultation(), RationAnalysisScope.GROUPE_PROPOSEES).map { it.uuid })

        // 10. Suppression d'une étape (variables supprimées en cascade), puis de la ration parente
        vm.removeRationFromConsultation(ration(e3.uuid)); idle()
        assertEquals(listOf(e8.uuid), repo.getConsultationById("c")!!.etapesEvolutives.map { it.uuid })
        vm.removeRationFromConsultation(ration("actuelle")); idle()
        assertTrue(repo.getConsultationById("c")!!.rations.isEmpty(), "le plan part avec sa ration")
        assertTrue(dao.rationVars.isEmpty())
        assertTrue(vm.bilansEtapes.value.isEmpty())
    }

    /** Rafale d'éditions avec E/S parallèles et latence : l'état final doit contenir toutes les éditions. */
    @Test
    fun rafaleEditions_ioParallele_aucunePerte() = run(parallele = true) {
        consultationEnBase()
        vm.ajouterEtape(ration("actuelle"), 8.0, emptyList()); idle()
        vm.ajouterEtape(ration("actuelle"), 12.0, emptyList()); idle()
        dao.latence = true
        val attendu = mutableMapOf<String, Double>()
        repeat(6) { tour ->
            PlanEvolutif.planTrie(consultation(), ration("actuelle")).forEach { e ->
                val q = 100.0 + tour * 10 + (e.poids ?: 0.0)
                vm.mettreAJourQuantiteEtape(ration(e.uuid), "croq", q)
                attendu[e.uuid] = q
                yield()
            }
        }
        idle()
        dao.latence = false
        val base = repo.getConsultationById("c")!!
        assertEquals(attendu, base.rations.associate { it.uuid to it.alimentMutableList.single().quantite }, "base")
        assertEquals(attendu, consultation().rations.associate { it.uuid to it.alimentMutableList.single().quantite }, "mémoire")
    }

    @Test
    fun ordonnance_rationAvecPlan() = run {
        consultationEnBase()
        vm.ajouterEtape(ration("actuelle"), 8.0, emptyList()); idle()
        val c = consultation()
        suspend fun html(selection: List<Ration>) = fr.vetbrain.vetnutri_mp.Export.HtmlDocumentBuilder.buildHtml(
            fr.vetbrain.vetnutri_mp.Export.DocumentType.PRESCRIPTION,
            fr.vetbrain.vetnutri_mp.Export.ExportData(animal = null, ration = null, reference = null,
                rations = selection, consultation = c))
        assertTrue(html(c.rations).contains(translate(LocalizationKeys.Evolutive.PRESCRIPTION_TITLE)))
        assertFalse(html(c.etapesEvolutives).contains(translate(LocalizationKeys.Evolutive.PRESCRIPTION_TITLE)))
    }
}
