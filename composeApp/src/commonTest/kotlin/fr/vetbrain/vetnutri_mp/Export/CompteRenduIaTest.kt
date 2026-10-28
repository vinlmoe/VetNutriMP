package fr.vetbrain.vetnutri_mp.Export

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CompteRenduIaTest {

    private val animal = AnimalEv(nom = "Rex", ownerName = "Jean Dupont", id = "DOS-4521", race = "Labrador")

    @Test
    fun anonymiserMasqueNomsEtCoordonnees() {
        val texte = "REX vit chez M. Dupont (jean.dupont@mail.fr, 06 12 34 56 78). Rexona n'est pas un nom."
        val resultat = CompteRenduIa.anonymiser(texte, animal)

        assertEquals(
                "[ANIMAL] vit chez M. [PROPRIÉTAIRE] ([COORDONNÉES], [COORDONNÉES]). Rexona n'est pas un nom.",
                resultat
        )
    }

    @Test
    fun promptSansDonneesIdentifiantes() {
        val consultation =
                ConsultationEv(
                        date = LocalDate(2026, 10, 1),
                        objectConsult = "Surpoids de Rex",
                        weight = 38.24,
                        BCS = 7,
                        rations =
                                mutableListOf(
                                        Ration(
                                                name = "Ration proposée",
                                                actual = false,
                                                alimentMutableList =
                                                        mutableListOf(AlimentRation(quantite = 312.46, aliment = AlimentEv()))
                                        )
                                )
                )
        val prompt =
                CompteRenduIa.construirePrompt(
                        DonneesPromptIa(
                                animal = animal.copy(birthdate = LocalDate(2020, 4, 1)),
                                consultation = consultation,
                                referentielGeneral = "Chien adulte",
                                referentielsMaladies = listOf("Obésité"),
                                anamnese = "Jean Dupont donne des restes de table à Rex.",
                                examenClinique = "",
                                facteurNutritionnelClef = "",
                                texteAdditionnel = "",
                                conseils = listOf("Transition alimentaire 7 jours"),
                                nomAliment = { "Croquettes light" }
                        )
                )

        assertFalse(prompt.contains("Rex"))
        assertFalse(prompt.contains("Dupont"))
        assertFalse(prompt.contains("DOS-4521"))
        assertTrue(prompt.contains("[PROPRIÉTAIRE] donne des restes de table à [ANIMAL]."))
        assertTrue(prompt.contains("\"Croquettes light\""))
        assertTrue(prompt.contains("312.5"))
        assertTrue(prompt.contains("\"6 an(s) 6 mois\""))
        assertTrue(prompt.contains("\"Obésité\""))
    }

    @Test
    fun lireReponseDansUnBlocDeCode() {
        val texte =
                """
                Voici le compte rendu :
                ```json
                {"anamnese": "Ligne 1\nLigne 2", "examenClinique": null,
                 "facteurNutritionnelClef": ["Énergie", "Fibres"], "texteAdditionnel": "  ", "autre": 3}
                ```
                """.trimIndent()

        val reponse = CompteRenduIa.lireReponse(texte)

        assertEquals("Ligne 1\nLigne 2", reponse?.anamnese)
        assertNull(reponse?.examenClinique)
        assertEquals("Énergie\nFibres", reponse?.facteurNutritionnelClef)
        assertNull(reponse?.texteAdditionnel)
        assertEquals(2, reponse?.nombreChamps)
    }

    @Test
    fun lireReponseInexploitable() {
        assertNull(CompteRenduIa.lireReponse("Je ne peux pas répondre."))
        assertNull(CompteRenduIa.lireReponse("{ pas du json }"))
        assertNull(CompteRenduIa.lireReponse("{\"autre\": \"x\"}"))
    }

    private fun extrait(id: String, titre: String, vararg blocs: TextBlock) =
            HtmlSection(id = id, title = titre, content = RichTextContent(blocs.toList()), category = SectionCategory.LIVRE_COURS)

    private val livre =
            listOf(
                    extrait("obe-intro", "Surpoids, obésité et arthrose — Introduction"),
                    extrait("obe-acc", "Surpoids, obésité et arthrose — Accompagnement nutritionnel"),
                    extrait("mrc-acc", "La maladie rénale chronique — Éléments clefs de l’accompagnement nutritionnel"),
                    extrait("coeur-fc", "Les affections cardiovasculaires — Facteurs clefs")
            )

    @Test
    fun suggererExtraitsSelonReferentielsEtObjet() {
        assertEquals(setOf("obe-acc"), CompteRenduIa.suggererExtraits(livre, listOf("Obèse chien")))
        assertEquals(
                setOf("mrc-acc", "coeur-fc"),
                CompteRenduIa.suggererExtraits(livre, listOf("Maladie rénale chronique stade 2", "Cardiopathie"))
        )
        assertEquals(emptySet<String>(), CompteRenduIa.suggererExtraits(livre, listOf("Maladie du chat")))
    }

    @Test
    fun extraitsJointsAuPromptEnTexteBrut() {
        val section =
                extrait(
                        "obe-acc", "Surpoids, obésité et arthrose — Accompagnement nutritionnel",
                        TextBlock.Heading("h1", 2, "Protéines"),
                        TextBlock.Paragraph("p1", "Assurer le besoin d'entretien."),
                        TextBlock.ListBlock("l1", listOf("EPA", "DHA")),
                        TextBlock.TableBlock("t1", listOf("Nutriment", "Min"), listOf(listOf("Protéines", "20")))
                )

        assertEquals(
                "PROTÉINES\n\nAssurer le besoin d'entretien.\n\n- EPA\n- DHA\n\nNutriment | Min\nProtéines | 20",
                CompteRenduIa.texteBrut(section)
        )
        val prompt =
                CompteRenduIa.construirePrompt(
                        DonneesPromptIa(
                                animal = animal, consultation = null, referentielGeneral = null,
                                referentielsMaladies = emptyList(), anamnese = "", examenClinique = "",
                                facteurNutritionnelClef = "", texteAdditionnel = "", conseils = emptyList(),
                                nomAliment = { "" }, extraitsLivre = listOf(section)
                        )
                )
        assertTrue(prompt.contains("\"extraitsLivreCours\""))
        assertTrue(prompt.contains("\"source\": \"Surpoids, obésité et arthrose — Accompagnement nutritionnel\""))
    }
}
