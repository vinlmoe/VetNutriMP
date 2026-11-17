package fr.vetbrain.vetnutri_mp.Data.PoidsChien

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Equation
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.TypeAnimalTroupeau
import fr.vetbrain.vetnutri_mp.Data.WeightDate
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.StadePhysio
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Préparation des entrées depuis la fiche, refus, plancher et ration proposée. */
class PrevisionPoidsChienTest {

    private val debut = LocalDate(2026, 1, 5)
    private val adulte = ReferenceEv(uuid = "adulte", nom = "Chien adulte", espece = Espece.CHIEN, stadePhysio = StadePhysio.ADULTE)

    /** Ration du cas A : 620 kcal/j, 10,4 g de protéines et 3,3 g de lipides pour 100 kcal. */
    private val apports = mapOf("rA" to ApportRation(620.0, 10.4 * 6.2, 3.3 * 6.2))

    private fun consultation(jour: Int, poids: Double?, ideal: Double? = 22.0, ref: String = "adulte") = ConsultationEv(
            uuid = "c$jour", date = debut + DatePeriod(days = jour), weight = poids, idealWeight = ideal,
            referenceGeneraleId = ref,
            rations = mutableListOf(Ration(uuid = "rA", name = "Ration A", actual = true)),
            selectionPrevisionPoids = SelectionRationsPrevision(actuelles = listOf("rA"), proposees = listOf("rA"))
    )

    /** Chien du cas C : 30 kg, poids idéal 22 kg, pesées à J28 et J56. */
    private fun chienCasC(vararg jours: Pair<Int, Double>) = AnimalEv(uuid = "chien", nom = "Rex").apply {
        consultations = (listOf(consultation(0, 30.0)) + jours.map { (j, p) -> consultation(j, p) }).toMutableList()
    }

    private fun references(animal: AnimalEv, ref: ReferenceEv? = adulte) = animal.consultations.associate { it.uuid to ref }

    private fun pret(animal: AnimalEv, refs: Map<String, ReferenceEv?> = references(animal)): PreparationPrevision.Pret {
        val p = PreparateurPrevision.preparer(animal, refs, apports)
        assertIs<PreparationPrevision.Pret>(p, "préparation refusée : $p")
        return p
    }

    private fun refus(animal: AnimalEv, refs: Map<String, ReferenceEv?> = references(animal)): List<MotifRefus> {
        val p = PreparateurPrevision.preparer(animal, refs, apports)
        assertIs<PreparationPrevision.Refus>(p)
        return p.motifs
    }

    @Test
    fun casC_depuisLaFiche_rationProposeeEtBlocageSousPlancher() {
        val p = pret(chienCasC(28 to 28.4, 56 to 27.0))
        assertEquals(listOf(PeriodeRation(0, 620.0)), p.entrees.rations)
        assertEquals(56, p.entrees.jourConsultation)
        assertEquals(ModePrevision.PERTE_DE_POIDS, p.entrees.mode)
        val semaines = (182 - 56) / 7
        val r = CalculPrevision.calculer(p, ReglagesPrevisionPoids(EtatModulePrevision.ACTIF, 55.0), semaines)
        assertEquals(2, r.peseesUtilisees.size)
        assertTrue(abs(r.mApparent.moyenne - 97.3726) <= 0.01)
        val proposition = assertNotNull(r.proposition)
        assertEquals(182, proposition.jourCible)
        assertTrue(abs(proposition.kcalParJour - 444.80) <= 0.5, "ration ${proposition.kcalParJour}")
        // 55 × 22^0,75 ≈ 559 kcal/j : la ration à 445 kcal/j est bloquée
        assertTrue(proposition.sousPlancher)
        assertTrue(proposition.bloquee)

        val permissif = CalculPrevision.calculer(p, ReglagesPrevisionPoids(EtatModulePrevision.ACTIF, 30.0), semaines)
        assertTrue(!permissif.proposition!!.bloquee)
    }

    @Test
    fun sansPeseeUtilisable_aucuneRationProposee() {
        // Seule la consultation de début : prévision et déficits, mais pas de ration calculée
        val r = CalculPrevision.calculer(pret(chienCasC()), ReglagesPrevisionPoids(), 26)
        assertTrue(r.peseesUtilisees.isEmpty())
        assertNull(r.proposition)
        assertEquals(4, r.niveauxDeficit.size)
        assertTrue(r.bande.isNotEmpty())
        // Une pesée à J10 (moins de 14 j après le début) n'est pas utilisable non plus
        val animal = chienCasC().apply { weightHistory = mutableListOf(WeightDate(refAnimal = "chien", date = debut + DatePeriod(days = 10), value = 29.5)) }
        val r2 = CalculPrevision.calculer(pret(animal), ReglagesPrevisionPoids(), 26)
        assertTrue(r2.peseesUtilisees.isEmpty())
        assertNull(r2.proposition)
    }

    @Test
    fun peseeMoinsDe14JoursApresUnChangement_exclueDepuisLaFiche() {
        val animal = chienCasC(28 to 28.4, 56 to 27.0).apply {
            // À J28, la ration passe à 530 kcal/j (ration actuelle de J56) ; pesée à J40 dans l'historique
            consultations[2].rations = mutableListOf(Ration(uuid = "rB", name = "B", actual = true))
            consultations[2].selectionPrevisionPoids = SelectionRationsPrevision(actuelles = listOf("rB"), proposees = listOf("rB"))
            weightHistory = mutableListOf(WeightDate(refAnimal = "chien", date = debut + DatePeriod(days = 40), value = 27.6))
        }
        val p = PreparateurPrevision.preparer(animal, references(animal), apports + ("rB" to ApportRation(530.0, 10.4 * 5.3, 3.3 * 5.3)))
        assertIs<PreparationPrevision.Pret>(p)
        assertEquals(listOf(PeriodeRation(0, 620.0), PeriodeRation(28, 530.0)), p.entrees.rations)
        val r = CalculPrevision.calculer(p, ReglagesPrevisionPoids(), 26)
        assertEquals(listOf(28, 56), r.peseesUtilisees.map { it.jour })
    }

    @Test
    fun rationPasseeNonDatee_estimationDesactivee() {
        val animal = chienCasC(28 to 28.4, 56 to 27.0).apply {
            consultations[1].selectionPrevisionPoids = SelectionRationsPrevision(actuelles = emptyList(), proposees = listOf("rA"))
        }
        val p = pret(animal)
        assertNotNull(p.estimationDesactivee)
        val r = CalculPrevision.calculer(p, ReglagesPrevisionPoids(), 26)
        assertNull(r.proposition)
    }

    @Test
    fun poidsIdealNonSaisi_entretienAuPoidsActuel() {
        val animal = AnimalEv(uuid = "chien").apply { consultations = mutableListOf(consultation(0, 20.0, ideal = null)) }
        val p = pret(animal)
        assertEquals(20.0, p.entrees.chien.poidsIdeal)
        assertEquals(ModePrevision.ENTRETIEN, p.entrees.mode)
        assertTrue(Avertissement.POIDS_IDEAL_NON_SAISI in p.avertissements)
    }

    @Test
    fun refus_especeAutreQueChien() {
        val chat = chienCasC().apply { specieId = Espece.CHAT.label }
        assertEquals(listOf(MotifRefus.ESPECE), refus(chat))
    }

    @Test
    fun refus_troupeau() {
        val troupeau = chienCasC().apply { typesTroupeau = mutableListOf(TypeAnimalTroupeau()) }
        assertEquals(listOf(MotifRefus.TROUPEAU), refus(troupeau))
    }

    @Test
    fun refus_referentielNonAdulte() {
        val animal = chienCasC()
        for (stade in listOf(StadePhysio.CROISSANCE, StadePhysio.GESTATION, StadePhysio.LACTATION, StadePhysio.HOSPIT)) {
            val ref = ReferenceEv(uuid = "r", espece = Espece.CHIEN, stadePhysio = stade)
            assertEquals(listOf(MotifRefus.STADE_NON_ADULTE), refus(animal, references(animal, ref)), stade.name)
        }
    }

    @Test
    fun refus_referentielAbsent() {
        val animal = chienCasC()
        assertEquals(listOf(MotifRefus.REFERENTIEL_ABSENT), refus(animal, references(animal, null)))
    }

    @Test
    fun refus_maladieRenseignee() {
        val animal = chienCasC().apply { consultations[0].referencesMaladies = mutableListOf("renale") }
        assertEquals(listOf(MotifRefus.MALADIE), refus(animal))
    }

    @Test
    fun refus_gestanteOuAllaitante() {
        val gestante = chienCasC().apply { consultations[0].coefGes = 1 }
        assertEquals(listOf(MotifRefus.GESTATION_LACTATION), refus(gestante))
        val allaitante = chienCasC().apply { consultations[0].coefLact = 1 }
        assertEquals(listOf(MotifRefus.GESTATION_LACTATION), refus(allaitante))
    }

    @Test
    fun refus_priseDePoids_etExtrapolation() {
        val maigre = AnimalEv(uuid = "chien").apply { consultations = mutableListOf(consultation(0, 20.0, ideal = 22.0)) }
        assertEquals(listOf(MotifRefus.PRISE_DE_POIDS), refus(maigre))
        val limite = AnimalEv(uuid = "chien").apply { consultations = mutableListOf(consultation(0, 21.5, ideal = 22.0)) }
        assertTrue(Avertissement.EXTRAPOLATION_NON_VALIDEE in pret(limite).avertissements)
    }

    @Test
    fun refus_donneesManquantesOuNonPlausibles() {
        val sansPoids = AnimalEv(uuid = "chien").apply { consultations = mutableListOf(consultation(0, null)) }
        assertEquals(listOf(MotifRefus.POIDS_INITIAL_ABSENT), refus(sansPoids))
        val geant = AnimalEv(uuid = "chien").apply { consultations = mutableListOf(consultation(0, 300.0, ideal = 250.0)) }
        assertEquals(listOf(MotifRefus.DONNEES_NON_PLAUSIBLES), refus(geant))
        val peseeAberrante = chienCasC(28 to 3.0)
        assertEquals(listOf(MotifRefus.DONNEES_NON_PLAUSIBLES), refus(peseeAberrante))
        val sansConsultation = AnimalEv(uuid = "chien")
        assertEquals(listOf(MotifRefus.AUCUNE_CONSULTATION), refus(sansConsultation))
        val sansRation = AnimalEv(uuid = "chien").apply {
            consultations = mutableListOf(consultation(0, 30.0).apply { selectionPrevisionPoids = SelectionRationsPrevision() })
        }
        assertEquals(listOf(MotifRefus.RATION_ABSENTE), refus(sansRation))
    }

    @Test
    fun suiviPlusLongQue52Semaines_refuseSiDebutMarque() {
        val animal = chienCasC(400 to 25.0).apply {
            consultations[0].selectionPrevisionPoids = SelectionRationsPrevision(listOf("rA"), listOf("rA"), debutSuivi = true)
        }
        assertEquals(listOf(MotifRefus.SUIVI_TROP_LONG), refus(animal))
        // Sans début marqué, le suivi commence à la plus ancienne consultation des 52 dernières semaines
        val libre = chienCasC(400 to 25.0)
        assertEquals("c400", PreparateurPrevision.consultationsDuSuivi(libre.consultations.sortedBy { it.date }).first().uuid)
    }

    @Test
    fun selectionParDefaut_actuellesEtOrdonnance() {
        val c = ConsultationEv(
                rations = mutableListOf(Ration(uuid = "a", actual = true), Ration(uuid = "p", actual = false), Ration(uuid = "q", actual = false)),
                prescriptionSelectedRationIds = mutableListOf("p")
        )
        assertEquals(SelectionRationsPrevision(actuelles = listOf("a"), proposees = listOf("p")), c.selectionPrevision())
        // Sans ordonnance : la dernière ration proposée
        c.prescriptionSelectedRationIds = mutableListOf()
        assertEquals(SelectionRationsPrevision(actuelles = listOf("a"), proposees = listOf("q")), c.selectionPrevision())
    }

    @Test
    fun sommeDeRations_additionneEnergieEtComposition() {
        val s = PreparateurPrevision.somme(listOf("x", "y"), mapOf("x" to ApportRation(300.0, 30.0, 10.0), "y" to ApportRation(100.0, 10.0, null)))!!
        assertEquals(400.0, s.kcal)
        assertEquals(40.0, s.proteinesG)
        assertNull(s.lipidesG)
        assertNull(PreparateurPrevision.composition(s))
    }

    @Test
    fun besoinSansDeficit_besoinStandardFoisTousLesKSaufK4() {
        val ref = ReferenceEv(uuid = "adulte", espece = Espece.CHIEN, stadePhysio = StadePhysio.ADULTE).apply {
            equationBEE = Equation(equationScript = "100 * BW ^ 0.75")
        }
        val animal = chienCasC().apply {
            consultations[0].apply { k1Value = 1.1; k2Value = 1.2; k3Value = 0.9; k4Value = 0.8; k5Value = 1.05; coefficientAjustement = 0.95 }
        }
        val p = pret(animal, references(animal, ref))
        // Besoin standard au poids idéal (22 kg) × K1 × K2 × K3 × K5 × ajustement, sans K4
        val attendu = 100 * 22.0.pow(0.75) * 1.1 * 1.2 * 0.9 * 1.05 * 0.95
        assertEquals(attendu, p.entrees.besoinSansDeficit!!, 1e-6)
        assertTrue(Avertissement.BESOIN_STANDARD_INDISPONIBLE !in p.avertissements)
        val r = CalculPrevision.calculer(p, ReglagesPrevisionPoids(), 26)
        listOf(0.4, 0.5, 0.6, 0.7).zip(r.niveauxDeficit).forEach { (f, n) -> assertEquals(f * attendu, n.kcalParJour, 1e-6) }
        // Sans équation de besoin : niveaux rapportés à EI0, avec avertissement
        val sans = pret(chienCasC())
        assertNull(sans.entrees.besoinSansDeficit)
        assertTrue(Avertissement.BESOIN_STANDARD_INDISPONIBLE in sans.avertissements)
    }

    @Test
    fun rationProposeeHorsOrdonnance_retenueParDefaut_laCourbeDescend() {
        fun animal(vararg proposees: String) = AnimalEv(uuid = "x").apply {
            consultations = mutableListOf(ConsultationEv(
                    uuid = "c", date = debut, weight = 30.0, idealWeight = 22.0, referenceGeneraleId = "adulte",
                    rations = (listOf(Ration(uuid = "act", name = "Actuelle", actual = true)) +
                            proposees.map { Ration(uuid = it, name = "Proposée $it", actual = false) }).toMutableList()
            ))
        }
        val apportsDeficit = mapOf(
                "act" to ApportRation(1250.0, 130.0, 44.0),
                "p1" to ApportRation(620.0, 64.5, 20.5),
                "p2" to ApportRation(700.0, 72.8, 23.1)
        )
        val une = PreparateurPrevision.preparer(animal("p1"), mapOf("c" to adulte), apportsDeficit)
        assertIs<PreparationPrevision.Pret>(une)
        assertEquals(listOf(PeriodeRation(0, 620.0)), une.entrees.rations)
        assertEquals("Proposée p1", une.entrees.nomsRationsFutures)
        assertTrue(Avertissement.RATION_ACTUELLE_POURSUIVIE !in une.avertissements)
        val r = CalculPrevision.calculer(une, ReglagesPrevisionPoids(), 26)
        assertTrue(r.bande.first { it.first == 84 }.second.moyenne < 27.0, "la ration déficitaire doit faire baisser le poids")

        // Plusieurs propositions sans choix : la dernière, avec un avertissement
        val deux = PreparateurPrevision.preparer(animal("p1", "p2"), mapOf("c" to adulte), apportsDeficit)
        assertIs<PreparationPrevision.Pret>(deux)
        assertEquals(700.0, deux.entrees.rations.single().kcalParJour)
        assertTrue(Avertissement.DERNIERE_RATION_PROPOSEE_RETENUE in deux.avertissements)
    }
}
