package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Export.ElementGraphiqueExporte
import fr.vetbrain.vetnutri_mp.Export.LibellesExportAliments
import fr.vetbrain.vetnutri_mp.Localization.translate

// Export commun des graphiques de l'analyse graphique des aliments (même principe que celui des rations)

/** Onglets de l'analyse des aliments exportables : graphiques en image puis tableaux, dans l'ordre du document. */
internal fun ongletsExportablesAliments(avecComparaison: Boolean): List<String> =
        listOf("densite_energetique", "protein_lipid", "phosphore_protein", "calcium_phosphore", "nutriments_perso") +
                (if (avecComparaison) listOf("comparaison") else emptyList()) +
                ONGLETS_TABLEAUX

/** Onglets exportés en tableau (pas en image : ils défilent et ne tiennent pas dans une capture). */
internal val ONGLETS_TABLEAUX = listOf("heatmap", "analyse_detaillee")

/** Titre d'un graphique des aliments, dans le dialogue d'export et sous son image. */
internal fun titreGraphiqueAliments(onglet: String, nutrimentX: String?, nutrimentY: String?): String =
        when (onglet) {
            "densite_energetique" -> translate("chart.energyDensityLabel")
            "protein_lipid" -> translate("auto.view.analysegraphiquealimentsview.proteines_lipides")
            "phosphore_protein" -> translate("auto.view.analysegraphiquealimentsview.phosphore_proteines")
            "calcium_phosphore" -> translate("auto.view.analysegraphiquealimentsview.calcium_phosphore")
            "nutriments_perso" -> {
                val noms = listOfNotNull(nutrimentX, nutrimentY)
                        .filter { it.isNotEmpty() }
                        .mapNotNull { cle -> NUTRIMENT_OPTIONS.find { it.key == cle }?.displayName }
                translate("graphAliments.export.custom", noms.joinToString(" / "))
            }
            "comparaison" -> translate("graph.comparaison.title")
            "heatmap" -> translate("graphAliments.export.asTable", translate("graphAliments.export.heatmapTitle"))
            "analyse_detaillee" ->
                translate("graphAliments.export.asTable", translate("auto.view.analysegraphiquealimentsview.analyse_dataillae"))
            else -> onglet
        }

/** Aliments, rations et sommes représentés sur les graphiques, avec leur numéro. */
internal fun elementsPourExport(donnees: List<AlimentAnalyseData>): List<ElementGraphiqueExporte> =
        donnees.map { d ->
            ElementGraphiqueExporte(
                    repere = d.graphLabel,
                    nom = if (d.isRationSum) d.aliment.nom.orEmpty().removePrefix("Σ").trim() else d.aliment.nom.orEmpty(),
                    detail = when {
                        d.isRationSum -> translate("graphAliments.export.sum")
                        d.rationActual == true -> translate("graph.export.doc.current")
                        d.rationActual == false -> translate("graph.export.doc.proposed")
                        else -> d.aliment.brand.orEmpty()
                    }
            )
        }

internal fun libellesExportAliments(): LibellesExportAliments =
        LibellesExportAliments(
                graphiques = translate("graph.export.doc.charts"),
                elements = translate("graphAliments.export.elements"),
                numero = translate("graphAliments.export.number"),
                detail = translate("graphAliments.export.detail"),
                statistiques = libellesStatistiques()
        )
