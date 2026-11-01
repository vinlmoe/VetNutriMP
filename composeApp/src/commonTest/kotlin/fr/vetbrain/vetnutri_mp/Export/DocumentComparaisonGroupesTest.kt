package fr.vetbrain.vetnutri_mp.Export

import fr.vetbrain.vetnutri_mp.Data.StatistiquesBoite
import kotlin.test.Test
import kotlin.test.assertTrue

class DocumentComparaisonGroupesTest {

    private val libelles = LibellesStatistiques(
            graphique = "Comparaison", statistiques = "Statistiques", mesure = "Mesure", groupe = "Groupe",
            moyenne = "Moyenne", ecartType = "Écart-type", minimum = "Min", mediane = "Médiane", maximum = "Max",
            aliments = "Aliments", aliment = "Aliment", marque = "Marque", virguleDecimale = true, langueLatex = "french"
    )

    private val doc = DocumentComparaison(
            titre = "Comparaison des groupes d'aliments",
            sousTitre = "Groupe A (8) / Groupe B (0)",
            image = GraphiqueExporte("images/comparaison.jpg", "Comparaison"),
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

    @Test
    fun markdown_tableauDesStatistiques() {
        val md = DocumentComparaisonGroupes.markdown(doc, libelles)
        assertTrue("| Mesure | Groupe | n | Moyenne | Écart-type | Min | Q1 | Médiane | Q3 | Max |" in md)
        // n, moyenne, écart-type, min, Q1, médiane, Q3, max
        assertTrue("| Protéines (g/1000 kcal) | Groupe A (8) | 8 | 4,50 | 2,45 | 1,00 | 2,75 | 4,50 | 6,25 | 8,00 |" in md)
        assertTrue("| Protéines (g/1000 kcal) | Groupe B (0) | — | — | — | — | — | — | — | — |" in md)
        assertTrue("| Renal | Royal Canin |" in md)
    }

    @Test
    fun latex_tableauDesStatistiques() {
        val tex = DocumentComparaisonGroupes.latex(doc, libelles)
        assertTrue("Protéines (g/1000 kcal) & Groupe A (8) & 8 & 4,50 & 2,45" in tex)
        assertTrue("Groupe B (0) & --- & ---" in tex)
        assertTrue(tex.trimEnd().endsWith("\\end{document}"))
    }
}
