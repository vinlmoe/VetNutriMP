package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.Enumer.VariableKind
import fr.vetbrain.vetnutri_mp.Repository.InMemoryEquationRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class TroupeauTest {

    /** BEE = 100 × BW^0.75 : 800 kcal pour 16 kg ; protéines OPTIMIN 50 g / 1000 kcal de BEE standard. */
    private val reference =
            ReferenceEv(uuid = "ref-adulte", nom = "Adulte test", espece = Espece.CHIEN).apply {
                equationBW = Equation(equationScript = "BW ^ 0.75")
                equationBEE = Equation(equationScript = "100 * BW ^ 0.75")
                definirNutriment(50.0, NutrientMain.PROTEINE, Reflevel.OPTIMIN, UnitReqEnum.PERKCAL, BiblioRef())
            }

    /** 400 kcal et 10 g de protéines pour 100 g. */
    private val aliment =
            AlimentEv(uuid = "riz", nom = "riz").apply {
                setNutrient(NutrientMain.ENERGIE, 400.0)
                setNutrient(NutrientMain.PROTEINE, 10.0)
            }

    private val adultes = TypeAnimalTroupeau(id = "A", nom = "Adultes", nombre = 2, poids = 16.0)
    private val actifs = TypeAnimalTroupeau(id = "B", nom = "Actifs", nombre = 1, poids = 16.0)
    private val types = listOf(adultes, actifs)

    private fun ration(quantite: Double) =
            Ration(uuid = "r1", name = "Ration", alimentMutableList = mutableListOf(AlimentRation(uuid = "l1", aliment = aliment, quantite = quantite)))

    private fun consultation(quantite: Double = 800.0) =
            ConsultationEv(
                    uuid = "c1",
                    weight = 500.0,
                    referenceGeneraleId = "autre",
                    coefficientAjustement = 3.0,
                    rations = mutableListOf(ration(quantite)),
                    parametresTroupeau =
                            mutableListOf(
                                    ParametresTypeTroupeau("A", nombre = 2, poids = 16.0, referenceId = reference.uuid, k = 1.0),
                                    ParametresTypeTroupeau("B", nombre = 1, poids = 16.0, referenceId = reference.uuid, k = 2.0)
                            )
            )

    private suspend fun analyser(c: ConsultationEv, r: Ration? = c.rations.first()) =
            AnalyseurTroupeau(InMemoryEquationRepository()).analyser(types, c, r, mapOf(reference.uuid to reference))

    @Test
    fun rationRepartieAuProrataDuBesoinEnergetique() = runTest {
        val analyse = analyser(consultation())

        // Besoins : 2 × 800 + 1 × 1600 = 3200 kcal ; la ration (800 g de riz) apporte 3200 kcal
        assertEquals(3200.0, analyse.besoinGroupe!!, 1e-6)
        assertEquals(3200.0, analyse.energieGroupe!!, 1e-6)
        assertEquals(1.0, analyse.facteurAjustementEnergie!!, 1e-9)
        assertEquals(3, analyse.effectif)

        val a = analyse.type("A")!!
        val b = analyse.type("B")!!
        assertEquals(0.5, a.part!!, 1e-9)
        assertEquals(0.5, b.part!!, 1e-9)
        // Un adulte reçoit 800 × 800 / 3200 = 200 g, un actif 800 × 1600 / 3200 = 400 g
        assertEquals(200.0, a.rationParAnimal!!.alimentMutableList.single().quantite, 1e-9)
        assertEquals(400.0, b.rationParAnimal!!.alimentMutableList.single().quantite, 1e-9)
        assertEquals(100.0, a.couvertureEnergie!!, 1e-6)
        assertEquals(100.0, b.couvertureEnergie!!, 1e-6)
    }

    @Test
    fun facteursSynchronesIdentiquesALAnalyse_etIndependantsDesKCommuns() = runTest {
        val c = consultation()
        val facteurs = facteursRepartitionTroupeau(types, c, mapOf(reference.uuid to reference))!!
        assertEquals(800.0 / 3200.0, facteurs.getValue("A"), 1e-12)
        assertEquals(1600.0 / 3200.0, facteurs.getValue("B"), 1e-12)
        // K1…K5 de la consultation : communs à tous les types, la répartition ne change pas
        val avecK1 = c.copy(k1Value = 1.5)
        assertEquals(facteurs, facteursRepartitionTroupeau(types, avecK1, mapOf(reference.uuid to reference)))
        assertEquals(4800.0, analyser(avecK1).besoinGroupe!!, 1e-6)
    }

    @Test
    fun chaqueTypeEstCompareASonReferentiel() = runTest {
        val analyse = analyser(consultation())
        // Seuil protéines : 50 g / 1000 kcal × 800 kcal (BEE standard) = 40 g par animal
        val proteinesA = analyse.type("A")!!.lignes.first { it.nutriment == NutrientMain.PROTEINE }
        val proteinesB = analyse.type("B")!!.lignes.first { it.nutriment == NutrientMain.PROTEINE }
        assertEquals(20.0, proteinesA.valeur.valeur, 1e-9)
        assertEquals(40.0, proteinesA.seuils.getValue(Reflevel.OPTIMIN), 1e-9)
        assertEquals(ConformiteStatus.CARENCE, proteinesA.conformite?.status)
        assertEquals(40.0, proteinesB.valeur.valeur, 1e-9)
        assertNull(proteinesB.conformite)
        assertEquals(1, analyse.type("A")!!.nonConformes.size)
        assertTrue(analyse.type("B")!!.nonConformes.isEmpty())
    }

    @Test
    fun facteurAjustementCouvreLeBesoinDuGroupe() = runTest {
        val analyse = analyser(consultation(quantite = 400.0))
        assertEquals(1600.0, analyse.energieGroupe!!, 1e-6)
        assertEquals(2.0, analyse.facteurAjustementEnergie!!, 1e-9)
    }

    @Test
    fun referentielManquant_bloqueLaRepartition() = runTest {
        val c = consultation().let { it.avecParametresType(it.parametresType(actifs).copy(referenceId = null)) }
        val analyse = analyser(c)
        assertNull(analyse.besoinGroupe)
        assertNull(analyse.facteurAjustementEnergie)
        assertTrue(analyse.problemes.any { it.contains("Actifs") })
        assertTrue(analyse.types.all { it.rationParAnimal == null })
        assertNull(facteursRepartitionTroupeau(types, c, mapOf(reference.uuid to reference)))
    }

    @Test
    fun effectifNul_neRecoitRien() = runTest {
        val c = consultation().let { it.avecParametresType(it.parametresType(actifs).copy(nombre = 0)) }
        val analyse = analyser(c)
        // Seuls les adultes : toute la ration pour eux, 400 g chacun
        assertEquals(1600.0, analyse.besoinGroupe!!, 1e-6)
        assertEquals(1.0, analyse.type("A")!!.part!!, 1e-9)
        assertEquals(400.0, analyse.type("A")!!.rationParAnimal!!.alimentMutableList.single().quantite, 1e-9)
        assertEquals(0.0, analyse.type("B")!!.part)
    }

    @Test
    fun consultationVueDepuisUnType() {
        val c = consultation().copy(k1Value = 1.2)
        val p = c.parametresType(actifs).copy(variables = mapOf("AW" to 30.0))
        val vue = c.pourTypeTroupeau(p)
        assertEquals(16.0, vue.weight)
        assertNull(vue.idealWeight)
        assertEquals(reference.uuid, vue.referenceGeneraleId)
        assertEquals(2.0, vue.coefficientAjustement)
        assertEquals(1.2, vue.k1Value)
        assertEquals(30.0, vue.suppVarp.single { it.variable == VariableKind.AdultWeight }.varue)
        // Paramètres absents : valeurs par défaut du troupeau
        val vide = ConsultationEv()
        assertEquals(ParametresTypeTroupeau("A", nombre = 2, poids = 16.0), vide.parametresType(adultes))
    }

    @Test
    fun rationParAnimal_garderLUuidEtNePasModifierLaRationDuGroupe() {
        val groupe = ration(800.0)
        val parAnimal = rationParAnimalTroupeau(groupe, 0.25, "Adultes")
        assertEquals(groupe.uuid, parAnimal.uuid)
        assertEquals(200.0, parAnimal.alimentMutableList.single().quantite, 1e-9)
        assertEquals(800.0, groupe.alimentMutableList.single().quantite, 1e-9)
    }

    @Test
    fun json_allerRetour() {
        val json = TroupeauJson.typesVersJson(types)
        assertEquals<List<TypeAnimalTroupeau>?>(types, TroupeauJson.typesDepuisJson(json))
        assertNull(TroupeauJson.typesVersJson(null))
        assertNull(TroupeauJson.typesDepuisJson(null))
        val parametres = consultation().parametresTroupeau
        assertEquals(parametres, TroupeauJson.parametresDepuisJson(TroupeauJson.parametresVersJson(parametres)))
        assertTrue(TroupeauJson.parametresDepuisJson(null).isEmpty())
        // Un animal sans types est un individu
        assertTrue(AnimalEv(typesTroupeau = types.toMutableList()).estTroupeau)
        assertEquals(3, AnimalEv(typesTroupeau = types.toMutableList()).effectifTroupeau)
        assertTrue(!AnimalEv().estTroupeau)
    }

    @Test
    fun besoinAnimal_kCommunsEtKDuType() {
        val c = consultation().copy(k1Value = 1.5, k3Value = 2.0)
        assertEquals(3.0, c.kCommunTroupeau(), 1e-12)
        assertEquals(1.0, ConsultationEv().kCommunTroupeau(), 1e-12)
        // 800 kcal (16 kg) × 3 (K1·K3) × 2 (K du type)
        assertEquals(4800.0, besoinAnimalTroupeau(c.parametresType(actifs), reference, c.kCommunTroupeau())!!, 1e-6)
        // Incalculable : sans référentiel, poids nul ou K nul
        assertNull(besoinAnimalTroupeau(c.parametresType(actifs), null, 1.0))
        assertNull(besoinAnimalTroupeau(c.parametresType(actifs).copy(poids = 0.0), reference, 1.0))
        assertNull(besoinAnimalTroupeau(c.parametresType(actifs).copy(k = 0.0), reference, 1.0))
    }

    @Test
    fun variablesDuType_entrentDansLeBesoinStandard() {
        // BEE dépendant du poids adulte (AW) : chaque type a ses propres variables
        val refCroissance =
                ReferenceEv(uuid = "ref-croissance", nom = "Croissance", espece = Espece.CHIEN).apply {
                    equationBW = Equation(equationScript = "BW ^ 0.75")
                    equationBEE = Equation(equationScript = "100 * BW ^ 0.75 * AW / 10")
                }
        assertEquals(listOf("AW"), variablesEquationsTroupeau(refCroissance))
        assertTrue(variablesEquationsTroupeau(reference).isEmpty())
        assertTrue(variablesEquationsTroupeau(null).isEmpty())
        val p = ParametresTypeTroupeau("A", nombre = 1, poids = 16.0, referenceId = refCroissance.uuid, variables = mapOf("AW" to 20.0))
        assertEquals(1600.0, besoinAnimalTroupeau(p, refCroissance, 1.0)!!, 1e-6)
        // Variable inconnue ignorée
        assertTrue(p.copy(variables = mapOf("XYZ" to 3.0)).variablesSupplementaires().isEmpty())
    }

    @Test
    fun avecParametresType_remplaceSansDupliquer() {
        val c = consultation()
        val maj = c.avecParametresType(c.parametresType(adultes).copy(nombre = 12))
        assertEquals(2, maj.parametresTroupeau.size)
        assertEquals(12, maj.parametresType(adultes).nombre)
        // La consultation d'origine n'est pas modifiée
        assertEquals(2, c.parametresType(adultes).nombre)
        // Type sans paramètres : ajouté
        val vide = ConsultationEv().avecParametresType(ParametresTypeTroupeau("A", nombre = 4))
        assertEquals(listOf("A"), vide.parametresTroupeau.map { it.typeId })
    }

    @Test
    fun pourTypeTroupeau_conserveLesVariablesDeLaConsultationNonRedefinies() {
        val c =
                consultation().copy(
                        suppVarp =
                                mutableListOf(
                                        SupplementalvariableP(VariableKind.AdultWeight, 10.0),
                                        SupplementalvariableP(VariableKind.LitterSize, 4.0)
                                )
                )
        val vue = c.pourTypeTroupeau(c.parametresType(actifs).copy(variables = mapOf("AW" to 30.0)))
        assertEquals(2, vue.suppVarp.size)
        assertEquals(30.0, vue.suppVarp.single { it.variable == VariableKind.AdultWeight }.varue)
        assertEquals(4.0, vue.suppVarp.single { it.variable == VariableKind.LitterSize }.varue)
        // Poids nul : pas de poids (besoins non calculés plutôt que faux)
        assertNull(c.pourTypeTroupeau(c.parametresType(actifs).copy(poids = 0.0)).weight)
    }

    @Test
    fun repartition_effectifNulEtTousNuls() = runTest {
        val c = consultation().let { it.avecParametresType(it.parametresType(actifs).copy(nombre = 0)) }
        val facteurs = facteursRepartitionTroupeau(types, c, mapOf(reference.uuid to reference))!!
        assertEquals(1.0 / 2.0, facteurs.getValue("A"), 1e-12) // 2 adultes se partagent tout
        assertEquals(0.0, facteurs.getValue("B"))
        // Effectif nul : son référentiel manquant ne bloque pas la répartition
        val sansRef = c.avecParametresType(c.parametresType(actifs).copy(referenceId = null))
        assertEquals(facteurs, facteursRepartitionTroupeau(types, sansRef, mapOf(reference.uuid to reference)))
        // Aucun animal
        val aucun = c.avecParametresType(c.parametresType(adultes).copy(nombre = 0))
        assertNull(facteursRepartitionTroupeau(types, aucun, mapOf(reference.uuid to reference)))
        assertTrue(analyser(aucun).problemes.any { it.contains("Aucun animal") })
    }

    @Test
    fun repartition_sommeDesQuantitesParAnimalEgaleLaRationDuGroupe() = runTest {
        val analyse = analyser(consultation(quantite = 1234.5))
        val total = analyse.types.sumOf { t -> t.parametres.nombre * t.rationParAnimal!!.alimentMutableList.sumOf { it.quantite } }
        assertEquals(1234.5, total, 1e-9)
        assertEquals(1.0, analyse.types.sumOf { it.part!! }, 1e-12)
    }

    @Test
    fun analyse_sansRationOuRationVide() = runTest {
        val sansRation = analyser(consultation(), null)
        assertTrue(sansRation.problemes.any { it.contains("Sélectionner une ration") })
        assertTrue(sansRation.types.all { it.rationParAnimal == null })
        val vide = analyser(consultation(quantite = 0.0))
        assertEquals(0.0, vide.energieGroupe!!, 1e-12)
        assertNull(vide.facteurAjustementEnergie)
        assertEquals("Ration vide.", vide.type("A")!!.message)
    }

    @Test
    fun analyse_troupeauSansType() = runTest {
        val analyse = AnalyseurTroupeau(InMemoryEquationRepository()).analyser(emptyList(), consultation(), ration(100.0), emptyMap())
        assertEquals(0, analyse.effectif)
        assertNull(analyse.besoinGroupe)
        assertTrue(analyse.problemes.any { it.contains("au moins un groupe") })
    }

    @Test
    fun seuils_convertisEnValeursAbsoluesParAnimal() {
        // 50 g / 1000 kcal de BEE standard (800 kcal) = 40 g ; absents : pas de seuil
        val seuils = seuilsTroupeau(reference, NutrientMain.PROTEINE, 800.0, 16.0, 8.0)
        assertEquals(mapOf(Reflevel.OPTIMIN to 40.0), seuils)
        assertTrue(seuilsTroupeau(reference, NutrientMain.CELLULOSE, 800.0, 16.0, 8.0).isEmpty())
        // BEE inconnu : seuil par kcal non convertible
        assertTrue(seuilsTroupeau(reference, NutrientMain.PROTEINE, null, 16.0, 8.0).isEmpty())
    }

    // --- Troupeau entier ------------------------------------------------------------------------

    @Test
    fun troupeauEntier_sommeLesApportsEtLesBesoinsDesIndividus() = runTest {
        val lignes = analyser(consultation()).apportsTroupeauEntier()
        val proteines = lignes.first { it.nutriment == NutrientMain.PROTEINE }
        // Apport : 2 × 20 g + 1 × 40 g = 80 g = 800 g de riz à 10 %
        assertEquals(80.0, proteines.apport, 1e-9)
        // Besoin : 3 individus × 40 g (même BEE standard) = 120 g → sous l'optimum
        assertEquals(120.0, proteines.seuils.getValue(Reflevel.OPTIMIN), 1e-9)
        assertEquals(ConformiteStatus.CARENCE, proteines.statutTroupeau)
        assertTrue(!proteines.critique)
        // Seuls les adultes sont hors normes individuellement
        assertEquals(listOf("Adultes"), proteines.groupesHorsNormes.map { it.first })

        val energie = lignes.first { it.nutriment == NutrientMain.ENERGIE }
        assertEquals(3200.0, energie.apport, 1e-6)
        assertEquals(0.9 * 3200.0, energie.seuils.getValue(Reflevel.MIN), 1e-6)
        assertEquals(1.1 * 3200.0, energie.seuils.getValue(Reflevel.MAX), 1e-6)
        assertNull(energie.statutTroupeau)
    }

    @Test
    fun troupeauEntier_seuilAbsentDansUnReferentiel_pasDeSomme() = runTest {
        // Les actifs ont un référentiel sans OPTIMIN protéines mais avec un MAX
        val refActifs =
                ReferenceEv(uuid = "ref-actifs", nom = "Actifs", espece = Espece.CHIEN).apply {
                    equationBW = Equation(equationScript = "BW ^ 0.75")
                    equationBEE = Equation(equationScript = "100 * BW ^ 0.75")
                    definirNutriment(100.0, NutrientMain.PROTEINE, Reflevel.MAX, UnitReqEnum.PERKCAL, BiblioRef())
                }
        val c = consultation().let { it.avecParametresType(it.parametresType(actifs).copy(referenceId = refActifs.uuid)) }
        val analyse =
                AnalyseurTroupeau(InMemoryEquationRepository())
                        .analyser(types, c, c.rations.first(), mapOf(reference.uuid to reference, refActifs.uuid to refActifs))
        val proteines = analyse.apportsTroupeauEntier().first { it.nutriment == NutrientMain.PROTEINE }
        // L'apport compte tous les groupes ; aucun niveau n'est commun aux deux référentiels
        assertEquals(80.0, proteines.apport, 1e-9)
        assertTrue(proteines.seuils.isEmpty())
        assertNull(proteines.statutTroupeau)
        assertEquals(listOf("Adultes"), proteines.groupesHorsNormes.map { it.first })
    }

    @Test
    fun troupeauEntier_videTantQueLaRepartitionEstImpossible() = runTest {
        val c = consultation().let { it.avecParametresType(it.parametresType(actifs).copy(referenceId = null)) }
        assertTrue(analyser(c).apportsTroupeauEntier().isEmpty())
        assertTrue(analyser(consultation(), null).apportsTroupeauEntier().isEmpty())
    }

    @Test
    fun troupeauEntier_groupeSansIndividuIgnore() = runTest {
        val c = consultation().let { it.avecParametresType(it.parametresType(actifs).copy(nombre = 0, referenceId = null)) }
        val proteines = analyser(c).apportsTroupeauEntier().first { it.nutriment == NutrientMain.PROTEINE }
        // Toute la ration pour les 2 adultes : 80 g pour un besoin de 2 × 40 g
        assertEquals(80.0, proteines.apport, 1e-9)
        assertEquals(80.0, proteines.seuils.getValue(Reflevel.OPTIMIN), 1e-9)
        assertNull(proteines.statutTroupeau)
        assertTrue(proteines.groupesHorsNormes.isEmpty())
    }

    // --- Référentiels maladies, ration analysée, nouvelle consultation, ajustement -------------

    @Test
    fun referencesMaladies_prioritairesDansLaSyntheseDuTroupeau() = runTest {
        // Maladie : protéines MIN 100 g / 1000 kcal (80 g par animal) et fibres MAX absentes du
        // référentiel général
        val maladie =
                ReferenceEv(uuid = "ref-maladie", nom = "Maladie test", espece = Espece.CHIEN, maladie = true).apply {
                    definirNutriment(100.0, NutrientMain.PROTEINE, Reflevel.MIN, UnitReqEnum.PERKCAL, BiblioRef())
                    definirNutriment(1.0, NutrientMain.CELLULOSE, Reflevel.MAX, UnitReqEnum.ABSOLUTE, BiblioRef())
                }
        aliment.setNutrient(NutrientMain.CELLULOSE, 1.0)
        val c = consultation()
        val analyse =
                AnalyseurTroupeau(InMemoryEquationRepository())
                        .analyser(types, c, c.rations.first(), mapOf(reference.uuid to reference), listOf(maladie))
        // Les actifs (40 g, conformes au référentiel général) sont carencés au regard de la maladie
        val proteinesB = analyse.type("B")!!.lignes.first { it.nutriment == NutrientMain.PROTEINE }
        assertEquals(ConformiteStatus.CARENCE_MALADIE, proteinesB.conformite?.status)
        // Nutriment présent seulement dans le référentiel maladie : évalué (4 g de fibres > 1 g)
        val fibresB = analyse.type("B")!!.lignes.first { it.nutriment == NutrientMain.CELLULOSE }
        assertEquals(ConformiteStatus.EXCES_MALADIE, fibresB.conformite?.status)
        val proteines = analyse.apportsTroupeauEntier().first { it.nutriment == NutrientMain.PROTEINE }
        assertEquals(listOf("Adultes", "Actifs"), proteines.groupesHorsNormes.map { it.first })
        // Sans maladie : comportement inchangé
        assertNull(analyser(c).type("B")!!.lignes.first { it.nutriment == NutrientMain.PROTEINE }.conformite)
    }

    @Test
    fun analyse_retientLaRationAnalysee() = runTest {
        assertEquals("r1", analyser(consultation()).rationId)
        assertNull(analyser(consultation(), null).rationId)
    }

    @Test
    fun analyse_quantitesNegativesIgnorees() = runTest {
        val c = consultation()
        val avecNegatif =
                c.rations.first().let { r ->
                    r.copy(alimentMutableList = (r.alimentMutableList + AlimentRation(uuid = "l2", aliment = aliment, quantite = -50.0)).toMutableList())
                }
        val analyse = analyser(c, avecNegatif)
        assertEquals(800.0, analyse.quantiteTotale, 1e-9)
        assertEquals(3200.0, analyse.energieGroupe!!, 1e-6)
        assertEquals(0.0, analyse.type("A")!!.rationParAnimal!!.alimentMutableList.first { it.uuid == "l2" }.quantite)
    }

    @Test
    fun nouvelleConsultation_reprendReferentielKEtVariablesDeLaPlusRecente() {
        val ancienne =
                ConsultationEv(
                        uuid = "c-ancienne", date = LocalDate(2026, 1, 1),
                        parametresTroupeau = mutableListOf(ParametresTypeTroupeau("A", referenceId = "ref-ancienne", k = 0.8))
                )
        val recente =
                ConsultationEv(
                        uuid = "c-recente", date = LocalDate(2026, 6, 1),
                        parametresTroupeau =
                                mutableListOf(
                                        ParametresTypeTroupeau(
                                                "A", nombre = 9, poids = 99.0, referenceId = reference.uuid, k = 1.3,
                                                variables = mapOf("AW" to 20.0), bcs = 3.0
                                        )
                                )
                )
        val animal = AnimalEv(typesTroupeau = types.toMutableList(), consultations = mutableListOf(recente, ancienne))
        val parametres = animal.parametresTroupeauNouvelleConsultation()
        val a = parametres.single { it.typeId == "A" }
        // Effectif et poids : ceux de la fiche ; référentiel, K, variables : consultation la plus récente
        assertEquals(2, a.nombre)
        assertEquals(16.0, a.poids)
        assertEquals(reference.uuid, a.referenceId)
        assertEquals(1.3, a.k)
        assertEquals(mapOf("AW" to 20.0), a.variables)
        assertNull(a.bcs)
        // Groupe absent de la consultation précédente : valeurs par défaut
        assertEquals(ParametresTypeTroupeau("B", nombre = 1, poids = 16.0), parametres.single { it.typeId == "B" })
        // Individu : aucun paramètre
        assertTrue(AnimalEv().parametresTroupeauNouvelleConsultation().isEmpty())
    }

    @Test
    fun rationAjustee_multiplieEtArrondiAuDixiemeDeGramme() {
        val groupe = ration(123.45)
        val ajustee = rationAjusteeTroupeau(groupe, 1.5)
        assertEquals(185.2, ajustee.alimentMutableList.single().quantite, 1e-9)
        assertEquals(groupe.uuid, ajustee.uuid)
        assertEquals(123.45, groupe.alimentMutableList.single().quantite, 1e-9)
    }
}
