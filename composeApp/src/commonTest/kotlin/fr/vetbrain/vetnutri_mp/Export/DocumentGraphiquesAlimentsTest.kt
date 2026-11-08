package fr.vetbrain.vetnutri_mp.Export

import fr.vetbrain.vetnutri_mp.Data.StatistiquesBoite
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DocumentGraphiquesAlimentsTest {

    private val statistiques = LibellesStatistiques(
            statistiques = "Statistiques", mesure = "Mesure", groupe = "Groupe",
            moyenne = "Moyenne", ecartType = "Écart-type", minimum = "Min", mediane = "Médiane", maximum = "Max",
            aliments = "Aliments", aliment = "Aliment", marque = "Marque", virguleDecimale = true, langueLatex = "french"
    )

    private val libelles = LibellesExportAliments(
            graphiques = "Graphiques", elements = "Aliments et rations représentés", numero = "N°",
            detail = "Marque / type", statistiques = statistiques
    )

    private val comparaison = ComparaisonExportee(
            mesures = listOf(
                    MesureStatistiques(
                            "Protéines (g/1000 kcal)",
                            StatistiquesBoite.de((1..8).map { it.toDouble() }),
                            null
                    )
            ),
            groupeA = GroupeExporte("Groupe A (8)", listOf("Renal" to "Royal Canin")),
            groupeB = GroupeExporte("Groupe B (0)", emptyList())
    )

    private val doc = DocumentAliments(
            titre = "Graphiques des aliments",
            sousTitre = "/1000 kcal",
            graphiques = listOf(
                    GraphiqueExporte("images/1_densite_energetique.jpg", "Densité énergétique"),
                    GraphiqueExporte("images/2_comparaison.jpg", "Comparaison")
            ),
            elements = listOf(
                    ElementGraphiqueExporte("1", "Renal", "Royal Canin"),
                    ElementGraphiqueExporte("R1", "Ration du matin", "ration actuelle")
            ),
            comparaison = comparaison
    )

    @Test
    fun markdown_graphiquesElementsEtStatistiques() {
        val md = DocumentGraphiquesAliments.markdown(doc, libelles)
        assertTrue("![Densité énergétique](images/1_densite_energetique.jpg)" in md)
        assertTrue("![Comparaison](images/2_comparaison.jpg)" in md)
        assertTrue("| R1 | Ration du matin | ration actuelle |" in md)
        assertTrue("| Mesure | Groupe | n | Moyenne | Écart-type | Min | Q1 | Médiane | Q3 | Max |" in md)
        // n, moyenne, écart-type, min, Q1, médiane, Q3, max
        assertTrue("| Protéines (g/1000 kcal) | Groupe A (8) | 8 | 4,50 | 2,45 | 1,00 | 2,75 | 4,50 | 6,25 | 8,00 |" in md)
        assertTrue("| Protéines (g/1000 kcal) | Groupe B (0) | — | — | — | — | — | — | — | — |" in md)
        assertTrue("| Renal | Royal Canin |" in md)
    }

    @Test
    fun markdown_sansComparaison_pasDeStatistiques() {
        val md = DocumentGraphiquesAliments.markdown(doc.copy(comparaison = null), libelles)
        assertFalse("Statistiques" in md)
        assertTrue("| 1 | Renal | Royal Canin |" in md)
    }

    @Test
    fun latex_tableauDesStatistiques() {
        val tex = DocumentGraphiquesAliments.latex(doc, libelles)
        assertTrue("\\includegraphics" in tex)
        assertTrue("R1 & Ration du matin & ration actuelle" in tex)
        assertTrue("Protéines (g/1000 kcal) & Groupe A (8) & 8 & 4,50 & 2,45" in tex)
        assertTrue("Groupe B (0) & --- & ---" in tex)
        assertTrue(tex.trimEnd().endsWith("\\end{document}"))
    }

    private val tableau = TableauExporte(
            titre = "Heatmap",
            note = "Rapport apport / besoin",
            entetes = listOf("Nutriment") + (1..10).map { it.toString() },
            lignes = listOf(listOf("Calcium (MIN)") + (1..9).map { "1,2" } + "-")
    )

    @Test
    fun markdown_tableau() {
        val md = DocumentGraphiquesAliments.markdown(doc.copy(tableaux = listOf(tableau)), libelles)
        assertTrue("## Heatmap" in md)
        assertTrue("| Nutriment | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 |" in md)
        assertTrue("| Calcium (MIN) | 1,2 | 1,2 | 1,2 | 1,2 | 1,2 | 1,2 | 1,2 | 1,2 | 1,2 | - |" in md)
    }

    @Test
    fun latex_tableauCoupeEnBlocs() {
        val tex = DocumentGraphiquesAliments.latex(doc.copy(tableaux = listOf(tableau)), libelles)
        // 10 colonnes de valeurs : un bloc de 8 puis un bloc de 2, libellés répétés
        assertTrue("Nutriment & 1 & 2 & 3 & 4 & 5 & 6 & 7 & 8 \\\\" in tex)
        assertTrue("Nutriment & 9 & 10 \\\\" in tex)
        assertTrue("Calcium (MIN) & 1,2 & -- \\\\" in tex)
    }
}
