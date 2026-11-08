package fr.vetbrain.vetnutri_mp.Export

import fr.vetbrain.vetnutri_mp.Data.StatistiquesBoite
import fr.vetbrain.vetnutri_mp.Export.DocumentGraphiquesRations.md
import fr.vetbrain.vetnutri_mp.Export.DocumentGraphiquesRations.tex
import fr.vetbrain.vetnutri_mp.Utils.TextUtils
import kotlin.math.abs

/** Statistiques d'une mesure (densité, protéines…) pour les deux groupes ; null = aucune valeur. */
data class MesureStatistiques(val titre: String, val groupeA: StatistiquesBoite?, val groupeB: StatistiquesBoite?)

/** Aliments d'un groupe : nom et marque. */
data class GroupeExporte(val nom: String, val aliments: List<Pair<String, String>>)

data class LibellesStatistiques(
        val statistiques: String,
        val mesure: String,
        val groupe: String,
        val moyenne: String,
        val ecartType: String,
        val minimum: String,
        val mediane: String,
        val maximum: String,
        val aliments: String,
        val aliment: String,
        val marque: String,
        val virguleDecimale: Boolean,
        val langueLatex: String
)

/** Comparaison de deux groupes d'aliments : statistiques de chaque mesure des boîtes à moustaches. */
data class ComparaisonExportee(
        val mesures: List<MesureStatistiques>,
        val groupeA: GroupeExporte,
        val groupeB: GroupeExporte
)

/**
 * Sections Markdown ou LaTeX de la comparaison de deux groupes d'aliments (tableau des statistiques,
 * puis les aliments de chaque groupe), insérées dans le document des graphiques des aliments.
 */
object DocumentComparaisonGroupes {

    private fun colonnes(l: LibellesStatistiques) =
            listOf(l.mesure, l.groupe, "n", l.moyenne, l.ecartType, l.minimum, "Q1", l.mediane, "Q3", l.maximum)

    /** Lignes du tableau : une par mesure et par groupe ; « — » quand le groupe n'a pas de valeur. */
    private fun lignes(c: ComparaisonExportee, l: LibellesStatistiques): List<List<String>> =
            c.mesures.flatMap { m ->
                listOf(c.groupeA.nom to m.groupeA, c.groupeB.nom to m.groupeB).map { (groupe, s) ->
                    listOf(m.titre, groupe) +
                            if (s == null) List(8) { "—" }
                            else listOf(
                                    s.n.toString(),
                                    nombre(s.moyenne, l), nombre(s.ecartType, l), nombre(s.minimum, l),
                                    nombre(s.q1, l), nombre(s.mediane, l), nombre(s.q3, l), nombre(s.maximum, l)
                            )
                }
            }

    fun markdown(c: ComparaisonExportee, l: LibellesStatistiques): String = buildString {
        appendLine("## ${l.statistiques}")
        appendLine()
        appendLine("| " + colonnes(l).joinToString(" | ") + " |")
        appendLine("|---|---|" + "---:|".repeat(8))
        lignes(c, l).forEach { ligne -> appendLine("| " + ligne.joinToString(" | ") { md(it) } + " |") }
        appendLine()
        listOf(c.groupeA, c.groupeB).forEach { g ->
            appendLine("## ${l.aliments} — ${g.nom}")
            appendLine()
            appendLine("| ${l.aliment} | ${l.marque} |")
            appendLine("|---|---|")
            g.aliments.forEach { (nom, marque) -> appendLine("| ${md(nom)} | ${md(marque)} |") }
            appendLine()
        }
    }

    fun latex(c: ComparaisonExportee, l: LibellesStatistiques): String = buildString {
        appendLine("\\section*{${tex(l.statistiques)}}")
        appendLine("{\\small")
        appendLine("\\begin{longtable}{@{}p{0.2\\linewidth}l" + "r".repeat(8) + "@{}}")
        appendLine("\\toprule")
        appendLine(colonnes(l).joinToString(" & ") { tex(it) } + " \\\\")
        appendLine("\\midrule")
        appendLine("\\endhead")
        lignes(c, l).forEach { ligne -> appendLine(ligne.joinToString(" & ") { if (it == "—") "---" else tex(it) } + " \\\\") }
        appendLine("\\bottomrule")
        appendLine("\\end{longtable}")
        appendLine("}")
        appendLine()
        listOf(c.groupeA, c.groupeB).forEach { g ->
            appendLine("\\section*{${tex(l.aliments)} --- ${tex(g.nom)}}")
            appendLine("\\begin{longtable}{@{}p{0.6\\linewidth}p{0.35\\linewidth}@{}}")
            appendLine("\\toprule")
            appendLine("${tex(l.aliment)} & ${tex(l.marque)} \\\\")
            appendLine("\\midrule")
            g.aliments.forEach { (nom, marque) -> appendLine("${tex(nom)} & ${tex(marque)} \\\\") }
            appendLine("\\bottomrule")
            appendLine("\\end{longtable}")
            appendLine()
        }
    }

    /** Trois chiffres significatifs environ : 1234, 123, 12,3, 1,23. */
    internal fun nombre(valeur: Double, l: LibellesStatistiques): String {
        val decimales = when {
            abs(valeur) >= 100 -> 0
            abs(valeur) >= 10 -> 1
            else -> 2
        }
        val texte = TextUtils.formatDecimal(valeur, decimales)
        return if (l.virguleDecimale) texte.replace('.', ',') else texte
    }
}
