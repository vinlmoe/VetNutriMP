package fr.vetbrain.vetnutri_mp.Export

import fr.vetbrain.vetnutri_mp.Utils.TextUtils

/** Un aliment d'une ration exportée : quantité par jour, en grammes. */
data class AlimentExporte(val nom: String, val marque: String, val quantite: Double)

/**
 * Une ration exportée. [repere] est le numéro affiché sur les graphiques (« 1 », « 2 »…, ou « Σ »
 * pour une somme), pour relier les tableaux aux points et aux barres.
 */
data class RationExportee(
        val repere: String,
        val nom: String,
        val consultation: String,
        val actuelle: Boolean,
        val aliments: List<AlimentExporte>
)

/** Image d'un graphique, enregistrée dans l'archive sous [fichier] (chemin relatif). */
data class GraphiqueExporte(val fichier: String, val titre: String)

/** Libellés du document, dans la langue de l'interface. */
data class LibellesExport(
        val graphiques: String,
        val rations: String,
        val aliment: String,
        val marque: String,
        val quantite: String,
        val part: String,
        val total: String,
        val rationActuelle: String,
        val rationProposee: String,
        val consultation: String,
        val virguleDecimale: Boolean,
        /** Langue babel du document LaTeX (french, english…). */
        val langueLatex: String
)

data class DocumentGraphiques(
        val titre: String,
        val sousTitre: String,
        val graphiques: List<GraphiqueExporte>,
        val rations: List<RationExportee>
)

/** Document Markdown ou LaTeX des graphiques de rations et des tableaux de leurs aliments. */
object DocumentGraphiquesRations {

    fun markdown(doc: DocumentGraphiques, l: LibellesExport): String = buildString {
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
        appendLine("## ${l.rations}")
        appendLine()
        doc.rations.forEach { r ->
            appendLine("### ${titreRation(r)}")
            appendLine()
            appendLine("${l.consultation} : ${r.consultation} — ${if (r.actuelle) l.rationActuelle else l.rationProposee}")
            appendLine()
            appendLine("| ${l.aliment} | ${l.marque} | ${l.quantite} | ${l.part} |")
            appendLine("|---|---|---:|---:|")
            val total = r.aliments.sumOf { it.quantite }
            r.aliments.forEach { a ->
                appendLine(
                        "| ${md(a.nom)} | ${md(a.marque)} | ${nombre(a.quantite, 1, l)} | ${nombre(part(a.quantite, total), 1, l)} |"
                )
            }
            appendLine("| **${l.total}** | | **${nombre(total, 1, l)}** | **${nombre(partTotale(total), 0, l)}** |")
            appendLine()
        }
    }

    fun latex(doc: DocumentGraphiques, l: LibellesExport): String = buildString {
        appendLine("\\documentclass[11pt,a4paper]{article}")
        appendLine("\\usepackage[utf8]{inputenc}")
        appendLine("\\usepackage[T1]{fontenc}")
        appendLine("\\usepackage[${l.langueLatex}]{babel}")
        appendLine("\\usepackage[margin=2cm]{geometry}")
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
                appendLine("  \\includegraphics[width=\\linewidth,height=0.42\\textheight,keepaspectratio]{${g.fichier}}")
                appendLine("  \\caption*{${tex(g.titre)}}")
                appendLine("\\end{figure}")
                appendLine()
            }
        }
        appendLine("\\section*{${tex(l.rations)}}")
        appendLine()
        doc.rations.forEach { r ->
            appendLine("\\subsection*{${tex(titreRation(r))}}")
            appendLine(
                    "${tex(l.consultation)} : ${tex(r.consultation)} --- ${tex(if (r.actuelle) l.rationActuelle else l.rationProposee)}"
            )
            appendLine()
            appendLine("\\begin{longtable}{@{}p{0.42\\linewidth}p{0.25\\linewidth}rr@{}}")
            appendLine("\\toprule")
            appendLine("${tex(l.aliment)} & ${tex(l.marque)} & ${tex(l.quantite)} & ${tex(l.part)} \\\\")
            appendLine("\\midrule")
            val total = r.aliments.sumOf { it.quantite }
            r.aliments.forEach { a ->
                appendLine(
                        "${tex(a.nom)} & ${tex(a.marque)} & ${nombre(a.quantite, 1, l)} & ${nombre(part(a.quantite, total), 1, l)} \\\\"
                )
            }
            appendLine("\\midrule")
            appendLine("\\textbf{${tex(l.total)}} & & \\textbf{${nombre(total, 1, l)}} & \\textbf{${nombre(partTotale(total), 0, l)}} \\\\")
            appendLine("\\bottomrule")
            appendLine("\\end{longtable}")
            appendLine()
        }
        appendLine("\\end{document}")
    }

    private fun titreRation(r: RationExportee): String =
            if (r.repere == "Σ") r.nom else "${r.repere}. ${r.nom}"

    /** 100 % pour une ration non vide ; 0 pour une ration sans aliment. */
    private fun partTotale(total: Double): Double = if (total > 0) 100.0 else 0.0

    private fun part(quantite: Double, total: Double): Double = if (total > 0) quantite * 100.0 / total else 0.0

    private fun nombre(valeur: Double, decimales: Int, l: LibellesExport): String {
        val texte = TextUtils.formatDecimal(valeur, decimales)
        return if (l.virguleDecimale) texte.replace('.', ',') else texte
    }

    /** Texte sûr dans une cellule de tableau Markdown. */
    internal fun md(texte: String): String = texte.replace("|", "\\|").replace("\n", " ")

    /** Texte sûr en LaTeX (caractères spéciaux échappés). */
    internal fun tex(texte: String): String = buildString {
        texte.forEach { c ->
            append(
                    when (c) {
                        '\\' -> "\\textbackslash{}"
                        '&', '%', '$', '#', '_', '{', '}' -> "\\$c"
                        '~' -> "\\textasciitilde{}"
                        '^' -> "\\textasciicircum{}"
                        'Σ' -> "\$\\Sigma\$"
                        '\n' -> " "
                        else -> c.toString()
                    }
            )
        }
    }
}
