package fr.vetbrain.vetnutri_mp.Export

import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AlimentRation
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Enumer.ContEnum
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Document HTML (ordonnance et analyse de ration) construit par [HtmlDocumentBuilder] sur une
 * petite fiche animal / consultation / ration : fragments clés présents (nom de l'animal, lignes
 * de la ration, quantités formatées), caractères spéciaux échappés, et jamais de « null » ni de
 * « NaN » dans le rendu.
 */
class HtmlDocumentBuilderTest {

    private val croquettes =
            AlimentEv(uuid = "croq", nom = "Croquettes adulte", brand = "Marque", gamme = "Gamme").apply {
                setNutrient(NutrientMain.PROTEINE, 25.0)
                setNutrient(NutrientMain.ENERGIE, 350.0)
            }

    /** Aliment en sachets de 85 g : la quantité est aussi exprimée en nombre de sachets. */
    private val patee = AlimentEv(uuid = "patee", nom = "Pâtée", cont = ContEnum.SACHET, quantInt = 85.0)

    private val huile = AlimentEv(uuid = "huile", nom = "Huile de colza")

    private val animal =
            AnimalEv(uuid = "animal-rex", nom = "Rex & Co", id = "ID-42", specieId = Espece.CHIEN.label)

    private fun ration(nom: String = "Actuelle", actual: Boolean = true) =
            Ration(
                    uuid = "r-$nom",
                    idConsult = "c",
                    name = nom,
                    actual = actual,
                    alimentMutableList =
                            mutableListOf(
                                    AlimentRation(uuid = "a1", refAlimUnif = "croq", quantite = 150.0, aliment = croquettes),
                                    AlimentRation(uuid = "a2", refAlimUnif = "patee", quantite = 170.0, aliment = patee),
                                    AlimentRation(uuid = "a3", refAlimUnif = "huile", quantite = 2.3, aliment = huile)
                            )
            )

    private val consultation =
            ConsultationEv(uuid = "c", idAnim = "animal-rex", weight = 12.0, rations = mutableListOf(ration()))

    private fun assertSansNullNiNaN(html: String) {
        assertFalse(Regex("\\bnull\\b").containsMatchIn(html), "le document ne doit pas contenir « null »")
        assertFalse(html.contains("NaN"), "le document ne doit pas contenir « NaN »")
    }

    @Test
    fun ordonnance_contientAnimalRationEtQuantitesFormatees() = runTest {
        val html =
                HtmlDocumentBuilder.buildHtml(
                        DocumentType.PRESCRIPTION,
                        ExportData(
                                animal = animal,
                                ration = null,
                                reference = null,
                                rations = listOf(ration()),
                                consultation = consultation,
                                conseils = listOf("Fractionner en 2 repas")
                        )
                )

        assertTrue(html.startsWith("<!DOCTYPE html>") || html.contains("<html"), "document HTML complet")
        assertTrue(html.contains("Ordonnance nutritionnelle"), "titre par défaut de l'ordonnance")
        // Animal : nom échappé, identifiant, espèce
        assertTrue(html.contains("Rex &amp; Co"), "le nom de l'animal est échappé")
        assertFalse(html.contains("Rex & Co"), "le « & » brut ne doit pas apparaître")
        assertTrue(html.contains("ID-42"))
        assertTrue(html.contains(Espece.CHIEN.label))
        // Ration : en-tête et lignes d'aliments
        assertTrue(html.contains("Ration: Actuelle"))
        assertTrue(html.contains("Marque, Gamme, Croquettes adulte"), "marque, gamme et nom de l'aliment")
        assertTrue(html.contains("150.0 g"))
        assertTrue(html.contains("170.0 g"))
        // 2.3 g : un seul chiffre décimal, sans arrondi parasite (NumberUtils / TextUtils)
        assertTrue(html.contains("2.3 g"), "la quantité 2.3 doit être rendue « 2.3 g »")
        assertFalse(html.contains("2.30 g"))
        assertFalse(html.contains("2.299"))
        // Sachets : 170 g / 85 g par sachet = 2 sachets
        assertTrue(html.contains("2.0 sachets"), "équivalent en sachets")
        // Conseils
        assertTrue(html.contains("Fractionner en 2 repas"))
        assertSansNullNiNaN(html)
    }

    @Test
    fun ordonnance_titreEtTextePersonnalises() = runTest {
        val html =
                HtmlDocumentBuilder.buildHtml(
                        DocumentType.PRESCRIPTION,
                        ExportData(
                                animal = animal,
                                ration = null,
                                reference = null,
                                rations = listOf(ration("Proposée", actual = false)),
                                consultation = consultation,
                                title = "Plan <alimentaire>",
                                additionalText = "Revoir dans 1 mois"
                        )
                )
        assertTrue(html.contains("Plan &lt;alimentaire&gt;"), "le titre est échappé")
        assertTrue(html.contains("Revoir dans 1 mois"))
        assertTrue(html.contains("Ration: Proposée"))
        assertEquals(1, Regex("Composition de la ration").findAll(html).count(), "un seul bloc de ration")
        assertSansNullNiNaN(html)
    }

    @Test
    fun ordonnance_sansRationNiAnimal_resteUnDocumentValide() = runTest {
        val html =
                HtmlDocumentBuilder.buildHtml(
                        DocumentType.PRESCRIPTION,
                        ExportData(animal = null, ration = null, reference = null)
                )
        assertTrue(html.contains("<html"))
        assertTrue(html.contains("</html>"))
        assertFalse(html.contains("Composition de la ration"))
        assertFalse(html.contains("<h2>Animal</h2>"))
        assertSansNullNiNaN(html)
    }

    @Test
    fun analyseDeRation_contientBilanEnergetiqueEtRation() = runTest {
        val html =
                HtmlDocumentBuilder.buildHtml(
                        DocumentType.RATION_ANALYSIS,
                        ExportData(
                                animal = animal,
                                ration = ration(),
                                reference = null,
                                poidsAnimal = 12.0,
                                poidsMetabolique = 6.45,
                                besoinEnergetiqueStandard = 645.0,
                                besoinEnergetiqueTotal = 645.0,
                                energieApportee = 525.0,
                                pourcentageCouverture = 81.4
                        )
                )
        assertTrue(html.contains("Analyse de ration"), "titre par défaut de l'analyse")
        assertTrue(html.contains("Rex &amp; Co"))
        assertTrue(html.contains("Composition de la ration"))
        assertTrue(html.contains("150.0 g"))
        assertTrue(html.contains("2.3 g"))
        // Bilan énergétique : kcal/j arrondis à l'entier
        assertTrue(html.contains("645 kcal/j"))
        assertTrue(html.contains("525 kcal/j"))
        assertSansNullNiNaN(html)
    }

    @Test
    fun analyseDeRation_valeursEnergetiquesAbsentes_remplaceesParUnTiretSansNull() = runTest {
        // Seule l'énergie apportée est connue : poids métabolique, BEE, BE, K... sont absents
        val html =
                HtmlDocumentBuilder.buildHtml(
                        DocumentType.RATION_ANALYSIS,
                        ExportData(animal = animal, ration = ration(), reference = null, energieApportee = 525.0)
                )
        assertTrue(html.contains("Bilan énergétique"))
        assertTrue(html.contains("525 kcal/j"))
        assertTrue(html.contains("— kg<sup>p</sup>"), "poids métabolique absent → tiret")
        assertTrue(html.contains("—%"), "couverture absente → tiret")
        assertSansNullNiNaN(html)
    }

    @Test
    fun analyseDeRation_sansAucuneValeurEnergetique_omettLeBilan() = runTest {
        val html =
                HtmlDocumentBuilder.buildHtml(
                        DocumentType.RATION_ANALYSIS,
                        ExportData(animal = animal, ration = ration(), reference = null)
                )
        assertFalse(html.contains("Bilan énergétique"))
        assertTrue(html.contains("Composition de la ration"))
        assertSansNullNiNaN(html)
    }
}
