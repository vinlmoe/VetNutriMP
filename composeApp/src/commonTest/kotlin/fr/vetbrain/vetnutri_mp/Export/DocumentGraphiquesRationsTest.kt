package fr.vetbrain.vetnutri_mp.Export

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DocumentGraphiquesRationsTest {

    private val libelles = LibellesExport(
            graphiques = "Graphiques", rations = "Rations", aliment = "Aliment", marque = "Marque",
            quantite = "Quantité (g/j)", part = "Part (%)", total = "Total",
            rationActuelle = "ration actuelle", rationProposee = "ration proposée",
            consultation = "Consultation", virguleDecimale = true, langueLatex = "french"
    )

    private val doc = DocumentGraphiques(
            titre = "Filou — rations",
            sousTitre = "2026-10-31",
            graphiques = listOf(GraphiqueExporte("images/densite.jpg", "Densité énergétique")),
            rations = listOf(
                    RationExportee(
                            "1", "Ration 50% & riz", "2026-10-31", actuelle = true,
                            aliments = listOf(
                                    AlimentExporte("Croquettes | adulte", "Marque_A", 150.0),
                                    AlimentExporte("Riz", "", 50.0)
                            )
                    )
            )
    )

    @Test
    fun markdown_tableauEtImage() {
        val md = DocumentGraphiquesRations.markdown(doc, libelles)
        assertTrue("![Densité énergétique](images/densite.jpg)" in md)
        assertTrue("### 1. Ration 50% & riz" in md)
        assertTrue("| Croquettes \\| adulte | Marque_A | 150,0 | 75,0 |" in md)
        assertTrue("| **Total** | | **200,0** | **100** |" in md)
    }

    @Test
    fun latex_echappeEtCompilable() {
        val tex = DocumentGraphiquesRations.latex(doc, libelles)
        assertTrue(tex.startsWith("\\documentclass"))
        assertTrue(tex.trimEnd().endsWith("\\end{document}"))
        assertTrue("\\includegraphics[width=\\linewidth,height=0.42\\textheight,keepaspectratio]{images/densite.jpg}" in tex)
        assertTrue("1. Ration 50\\% \\& riz" in tex)
        assertTrue("Marque\\_A & 150,0 & 75,0" in tex)
    }

    @Test
    fun latex_somme() {
        assertEquals("\$\\Sigma\$ rations", DocumentGraphiquesRations.tex("Σ rations"))
    }
}
