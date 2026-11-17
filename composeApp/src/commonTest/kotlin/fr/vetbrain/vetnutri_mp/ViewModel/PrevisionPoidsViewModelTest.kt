package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.EtatModulePrevision
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ReglagesPrevisionPoids
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.SelectionRationsPrevision
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import fr.vetbrain.vetnutri_mp.Repository.InMemoryPrevisionPoidsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Les trois états du drapeau et la journalisation des calculs. */
@OptIn(ExperimentalCoroutinesApi::class)
class PrevisionPoidsViewModelTest {

    private val reference = ReferenceEv(uuid = "adulte", espece = Espece.CHIEN, stadePhysio = StadePhysio.ADULTE)

    /** 400 kcal, 26 g de protéines et 8 g de lipides pour 100 g ; 155 g = 620 kcal/j. */
    private val croquettes = AlimentEv(uuid = "croq", nom = "Croquettes").apply {
        setNutrient(NutrientMain.ENERGIE, 400.0)
        setNutrient(NutrientMain.PROTEINE, 26.0)
        setNutrient(NutrientMain.LIPIDE, 8.0)
    }

    private fun animal(): AnimalEv {
        val debut = LocalDate(2026, 1, 5)
        fun c(jour: Int, poids: Double) = ConsultationEv(
                uuid = "c$jour", idAnim = "chien", date = debut + DatePeriod(days = jour), weight = poids, idealWeight = 22.0,
                referenceGeneraleId = "adulte",
                rations = mutableListOf(Ration(uuid = "r$jour", name = "R", actual = true,
                        alimentMutableList = mutableListOf(AlimentRation(aliment = croquettes, quantite = 155.0)))),
                selectionPrevisionPoids = SelectionRationsPrevision(listOf("r$jour"), listOf("r$jour"))
        )
        return AnimalEv(uuid = "chien", nom = "Rex").apply { consultations = mutableListOf(c(0, 30.0), c(28, 28.4), c(56, 27.0)) }
    }

    private fun viewModel(depot: InMemoryPrevisionPoidsRepository) = UnconfinedTestDispatcher().let { d ->
        PrevisionPoidsViewModel({ id -> reference.takeIf { it.uuid == id } }, null, depot, d, d)
    }

    @Test
    fun off_aucunCalcul() = runTest {
        val depot = InMemoryPrevisionPoidsRepository(ReglagesPrevisionPoids(EtatModulePrevision.OFF))
        val vm = viewModel(depot)
        vm.calculer(animal())
        assertEquals(EtatPrevision.Inactif, vm.etat.value)
        assertTrue(depot.calculs.isEmpty())
        assertTrue(!vm.reglages.value.calculer && !vm.reglages.value.afficher)
    }

    @Test
    fun silencieux_calculeEtJournaliseSansAfficher() = runTest {
        val depot = InMemoryPrevisionPoidsRepository(ReglagesPrevisionPoids(EtatModulePrevision.SILENCIEUX))
        val vm = viewModel(depot)
        val etat = vm.calculerPour(animal(), vm.reglages.value, 18)
        assertIs<EtatPrevision.Resultat>(etat)
        assertTrue(!vm.reglages.value.afficher)
        assertEquals(1, depot.calculs.size)
        val calcul = depot.calculs.single()
        assertEquals(ConstantesChienV2.VERSION, calcul.modelVersion)
        assertEquals("SILENCIEUX", calcul.moduleState)
        assertTrue("\"pesees\"" in calcul.inputsJson && "\"rations\"" in calcul.inputsJson)
        assertTrue("\"mApparent\"" in calcul.resultsJson && "\"rationProposee\"" in calcul.resultsJson)
        // Un même calcul n'est pas journalisé deux fois
        vm.calculerPour(animal(), vm.reglages.value, 18)
        assertEquals(1, depot.calculs.size)
    }

    @Test
    fun actif_calculeAfficheEtJournalise() = runTest {
        val depot = InMemoryPrevisionPoidsRepository(ReglagesPrevisionPoids(EtatModulePrevision.ACTIF))
        val vm = viewModel(depot)
        assertTrue(vm.reglages.value.afficher)
        val etat = vm.calculerPour(animal(), vm.reglages.value, 18)
        assertIs<EtatPrevision.Resultat>(etat)
        // 155 g à 400 kcal/100 g = 620 kcal/j ; 26 g/100 g ⇒ 6,5 g de protéines pour 100 kcal
        assertEquals(620.0, etat.resultat.entrees.rations.single().kcalParJour, 1e-9)
        assertEquals(6.5, etat.resultat.entrees.chien.composition.proteines, 1e-9)
        assertTrue(etat.resultat.proposition != null)
        assertEquals("ACTIF", depot.calculs.single().moduleState)
    }

    @Test
    fun desactiver_remetInactif() = runTest {
        val depot = InMemoryPrevisionPoidsRepository(ReglagesPrevisionPoids(EtatModulePrevision.ACTIF))
        val vm = viewModel(depot)
        vm.modifierReglages(ReglagesPrevisionPoids(EtatModulePrevision.OFF))
        assertEquals(EtatPrevision.Inactif, vm.etat.value)
        assertEquals(EtatModulePrevision.OFF, depot.lireReglages().etat)
    }
}
