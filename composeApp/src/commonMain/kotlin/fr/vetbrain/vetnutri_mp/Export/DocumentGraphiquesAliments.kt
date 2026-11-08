package fr.vetbrain.vetnutri_mp.Export

import fr.vetbrain.vetnutri_mp.Export.DocumentGraphiquesRations.md
import fr.vetbrain.vetnutri_mp.Export.DocumentGraphiquesRations.tex

/**
 * Aliment, ration ou somme représenté sur les graphiques : [repere] est le numéro des points et des
 * barres (« 1 », « R2 », « Σ »), [detail] la marque d'un aliment ou le type d'une ration.
 */
data class ElementGraphiqueExporte(val repere: String, val nom: String, val detail: String)

/**
 * Tableau exporté (heatmap, analyse détaillée) : [entetes] commence par l'en-tête de la colonne
 * des libellés ; chaque ligne a autant de cellules que d'en-têtes. [note] explique les valeurs.
 */
data class TableauExporte(
        val titre: String,
        val note: String,
        val entetes: List<String>,
        val lignes: List<List<String>>
)

data class LibellesExportAliments(
        val graphiques: String,
        val elements: String,
        val numero: String,
        val detail: String,
        val statistiques: LibellesStatistiques
)

data class DocumentAliments(
        val titre: String,
        val sousTitre: String,
        val graphiques: List<GraphiqueExporte>,
        val elements: List<ElementGraphiqueExporte>,
        val tableaux: List<TableauExporte> = emptyList(),
        /** Null quand la comparaison A/B n'est pas exportée. */
        val comparaison: ComparaisonExportee?
)

/**
 * Document Markdown ou LaTeX des graphiques des aliments : les images, la liste des aliments et
 * rations numérotés comme sur les graphiques, et les statistiques des boîtes à moustaches quand la
 * comparaison A/B est exportée.
 */
object DocumentGraphiquesAliments {

    fun markdown(doc: DocumentAliments, l: LibellesExportAliments): String = buildString {
        appendLine("# ${doc.titre}")
        appendLine()
        if (doc.sousTitre.isNotBlank()) {
            appendLine(doc.sousTitre)
            appendLine()
        }
        if (doc.graphiques.isNotEmpty()) {
            appendLine("## ${l.graphiques}")
            appendLine()
            doc.graphiques.forEach { g ->
                appendLine("![${g.titre}](${g.fichier})")
                appendLine()
                appendLine("*${g.titre}*")
                appendLine()
            }
        }
        if (doc.elements.isNotEmpty()) {
            appendLine("## ${l.elements}")
            appendLine()
            appendLine("| ${l.numero} | ${l.statistiques.aliment} | ${l.detail} |")
            appendLine("|---:|---|---|")
            doc.elements.forEach { e -> appendLine("| ${md(e.repere)} | ${md(e.nom)} | ${md(e.detail)} |") }
            appendLine()
        }
        doc.tableaux.forEach { t ->
            appendLine("## ${t.titre}")
            appendLine()
            if (t.note.isNotBlank()) {
                appendLine(t.note)
                appendLine()
            }
            appendLine("| " + t.entetes.joinToString(" | ") { md(it) } + " |")
            appendLine("|---|" + "---:|".repeat(t.entetes.size - 1))
            t.lignes.forEach { ligne -> appendLine("| " + ligne.joinToString(" | ") { md(it) } + " |") }
            appendLine()
        }
        doc.comparaison?.let { append(DocumentComparaisonGroupes.markdown(it, l.statistiques)) }
    }

    /** Colonnes de valeurs par bloc en LaTeX : au-delà, le tableau est coupé en plusieurs blocs. */
    internal const val COLONNES_PAR_BLOC_LATEX = 8

    /**
     * Tableau LaTeX : les colonnes de valeurs sont réparties en blocs de [COLONNES_PAR_BLOC_LATEX]
     * (la colonne des libellés est répétée) pour tenir dans la largeur de la page.
     */
    private fun tableauLatex(t: TableauExporte): String = buildString {
        appendLine("\\section*{${tex(t.titre)}}")
        if (t.note.isNotBlank()) {
            appendLine(tex(t.note))
            appendLine()
        }
        val valeurs = (1 until t.entetes.size).toList()
        valeurs.chunked(COLONNES_PAR_BLOC_LATEX).ifEmpty { listOf(emptyList()) }.forEach { bloc ->
            val colonnes = listOf(0) + bloc
            appendLine("{\\small")
            appendLine("\\begin{longtable}{@{}p{0.28\\linewidth}" + "r".repeat(bloc.size) + "@{}}")
            appendLine("\\toprule")
            appendLine(colonnes.joinToString(" & ") { tex(t.entetes[it]) } + " \\\\")
            appendLine("\\midrule")
            appendLine("\\endhead")
            t.lignes.forEach { ligne ->
                appendLine(colonnes.joinToString(" & ") { i -> ligne.getOrElse(i) { "" }.let { if (it == "-") "--" else tex(it) } } + " \\\\")
            }
            appendLine("\\bottomrule")
            appendLine("\\end{longtable}")
            appendLine("}")
            appendLine()
        }
    }

    fun latex(doc: DocumentAliments, l: LibellesExportAliments): String = buildString {
        appendLine("\\documentclass[11pt,a4paper]{article}")
        appendLine("\\usepackage[utf8]{inputenc}")
        appendLine("\\usepackage[T1]{fontenc}")
        appendLine("\\usepackage[${l.statistiques.langueLatex}]{babel}")
        appendLine("\\usepackage[margin=1.8cm]{geometry}")
        appendLine("\\usepackage{graphicx}")
        appendLine("\\usepackage{booktabs}")
        appendLine("\\usepackage{longtable}")
        appendLine("\\usepackage{float}")
        appendLine()
        appendLine("\\title{${tex(doc.titre)}}")
        appendLine("\\author{}")
        appendLine("\\date{${tex(doc.sousTitre)}}")
        appendLine()
        appendLine("\\begin{document}")
        appendLine("\\maketitle")
        appendLine()
        if (doc.graphiques.isNotEmpty()) {
            appendLine("\\section*{${tex(l.graphiques)}}")
            appendLine()
            doc.graphiques.forEach { g ->
                appendLine("\\begin{figure}[H]")
                appendLine("  \\centering")
                appendLine("  \\includegraphics[width=\\linewidth,height=0.8\\textheight,keepaspectratio]{${g.fichier}}")
                appendLine("  \\caption*{${tex(g.titre)}}")
                appendLine("\\end{figure}")
                appendLine()
            }
        }
        if (doc.elements.isNotEmpty()) {
            appendLine("\\section*{${tex(l.elements)}}")
            appendLine("\\begin{longtable}{@{}rp{0.55\\linewidth}p{0.3\\linewidth}@{}}")
            appendLine("\\toprule")
            appendLine("${tex(l.numero)} & ${tex(l.statistiques.aliment)} & ${tex(l.detail)} \\\\")
            appendLine("\\midrule")
            appendLine("\\endhead")
            doc.elements.forEach { e -> appendLine("${tex(e.repere)} & ${tex(e.nom)} & ${tex(e.detail)} \\\\") }
            appendLine("\\bottomrule")
            appendLine("\\end{longtable}")
            appendLine()
        }
        doc.tableaux.forEach { t -> append(tableauLatex(t)) }
        doc.comparaison?.let { append(DocumentComparaisonGroupes.latex(it, l.statistiques)) }
        appendLine("\\end{document}")
    }
}
