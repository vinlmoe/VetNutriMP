package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Enumer.Reflevel
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import fr.vetbrain.vetnutri_mp.Repository.InMemoryEquationRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

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

    private fun troupeau(quantite: Double = 800.0): Pair<Troupeau, ConsultationTroupeau> {
        val consultation =
                ConsultationTroupeau(
                        id = "c1",
                        titre = "Visite",
                        types =
                                listOf(
                                        ParametresTypeTroupeau("A", nombre = 2, poids = 16.0, referenceId = reference.uuid, k = 1.0),
                                        ParametresTypeTroupeau("B", nombre = 1, poids = 16.0, referenceId = reference.uuid, k = 2.0)
                                ),
                        ration = listOf(LigneRationTroupeau(alimentId = aliment.uuid, nom = "riz", quantite = quantite))
                )
        val t =
                Troupeau(
                        nom = "Meute",
                        espece = Espece.CHIEN.name,
                        contenu = ContenuTroupeau(types = listOf(adultes, actifs), consultations = listOf(consultation))
                )
        return t to consultation
    }

    private suspend fun analyser(t: Troupeau, c: ConsultationTroupeau) =
            AnalyseurTroupeau(InMemoryEquationRepository())
                    .analyser(t, c, mapOf(reference.uuid to reference), mapOf(aliment.uuid to aliment))

    @Test
    fun rationRepartieAuProrataDuBesoinEnergetique() = runTest {
        val (t, c) = troupeau()
        val analyse = analyser(t, c)

        // Besoins : 2 × 800 + 1 × 1600 = 3200 kcal ; la ration (800 g de riz) apporte 3200 kcal
        assertEquals(3200.0, analyse.besoinGroupe!!, 1e-6)
        assertEquals(3200.0, analyse.energieGroupe!!, 1e-6)
        assertEquals(1.0, analyse.facteurAjustementEnergie!!, 1e-9)
        assertEquals(3, analyse.effectif)

        val a = analyse.types.first { it.type.id == "A" }
        val b = analyse.types.first { it.type.id == "B" }
        assertEquals(0.5, a.part!!, 1e-9)
        assertEquals(0.5, b.part!!, 1e-9)
        // Un adulte reçoit 800 × 800 / 3200 = 200 g, un actif 800 × 1600 / 3200 = 400 g
        assertEquals(200.0, a.rationParAnimal!!.alimentMutableList.single().quantite, 1e-9)
        assertEquals(400.0, b.rationParAnimal!!.alimentMutableList.single().quantite, 1e-9)
        assertEquals(100.0, a.couvertureEnergie!!, 1e-6)
        assertEquals(100.0, b.couvertureEnergie!!, 1e-6)
    }

    @Test
    fun chaqueTypeEstCompareASonReferentiel() = runTest {
        val (t, c) = troupeau()
        val analyse = analyser(t, c)
        // Seuil protéines : 50 g / 1000 kcal × 800 kcal (BEE standard) = 40 g par animal
        val a = analyse.types.first { it.type.id == "A" }
        val b = analyse.types.first { it.type.id == "B" }
        val proteinesA = a.lignes.first { it.nutriment == NutrientMain.PROTEINE }
        val proteinesB = b.lignes.first { it.nutriment == NutrientMain.PROTEINE }
        assertEquals(20.0, proteinesA.valeur.valeur, 1e-9)
        assertEquals(40.0, proteinesA.seuils.getValue(Reflevel.OPTIMIN), 1e-9)
        assertEquals(ConformiteStatus.CARENCE, proteinesA.conformite?.status)
        assertEquals(40.0, proteinesB.valeur.valeur, 1e-9)
        assertNull(proteinesB.conformite)
        assertEquals(1, a.nonConformes.size)
        assertTrue(b.nonConformes.isEmpty())
    }

    @Test
    fun facteurAjustementCouvreLeBesoinDuGroupe() = runTest {
        val (t, c) = troupeau(quantite = 400.0)
        val analyse = analyser(t, c)
        assertEquals(1600.0, analyse.energieGroupe!!, 1e-6)
        assertEquals(2.0, analyse.facteurAjustementEnergie!!, 1e-9)
    }

    @Test
    fun referentielManquant_bloqueLaRepartition() = runTest {
        val (t, c) = troupeau()
        val sansReference = c.avecParametres(c.types.first { it.typeId == "B" }.copy(referenceId = null))
        val analyse = analyser(t, sansReference)
        assertNull(analyse.besoinGroupe)
        assertNull(analyse.facteurAjustementEnergie)
        assertTrue(analyse.problemes.any { it.contains("Actifs") })
        assertTrue(analyse.types.all { it.rationParAnimal == null })
    }

    @Test
    fun effectifNul_neRecoitRien() = runTest {
        val (t, c) = troupeau()
        val sansActifs = c.avecParametres(c.types.first { it.typeId == "B" }.copy(nombre = 0))
        val analyse = analyser(t, sansActifs)
        // Seuls les adultes : toute la ration pour eux, 400 g chacun
        assertEquals(1600.0, analyse.besoinGroupe!!, 1e-6)
        val a = analyse.types.first { it.type.id == "A" }
        assertEquals(1.0, a.part!!, 1e-9)
        assertEquals(400.0, a.rationParAnimal!!.alimentMutableList.single().quantite, 1e-9)
        assertEquals(0.0, analyse.types.first { it.type.id == "B" }.part)
    }

    @Test
    fun contenu_allerRetourJsonEtNouvelleConsultation() {
        val (t, _) = troupeau()
        val relu = ContenuTroupeau.depuisJson(t.contenu.versJson())
        assertEquals(t.contenu, relu)
        assertEquals(ContenuTroupeau(), ContenuTroupeau.depuisJson(""))

        // La nouvelle consultation reprend référentiels, K et ration ; effectifs du troupeau
        val modifie = t.copy(contenu = t.contenu.copy(types = listOf(adultes.copy(nombre = 5), actifs)))
        val nouvelle = modifie.nouvelleConsultation(date = 10L, titre = "Suivi")
        val pA = nouvelle.parametres(adultes)
        assertEquals(5, pA.nombre)
        assertEquals(reference.uuid, pA.referenceId)
        assertEquals(2.0, nouvelle.parametres(actifs).k)
        assertEquals(1, nouvelle.ration.size)
        assertTrue(nouvelle.ration.single().id != t.contenu.consultations.single().ration.single().id)

        // Retirer un type le retire des consultations
        val sansB = t.sansType("B")
        assertEquals(listOf("A"), sansB.contenu.consultations.single().types.map { it.typeId })
        assertNotNull(sansB.contenu.types.singleOrNull())
    }
}
