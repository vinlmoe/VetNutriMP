package fr.vetbrain.vetnutri_mp.View.AnalyseGraphique

import fr.vetbrain.vetnutri_mp.Utils.enTexte

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Export.AlimentExporte
import fr.vetbrain.vetnutri_mp.Export.LibellesExport
import fr.vetbrain.vetnutri_mp.Export.RationExportee
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppSizes

/** Format du document exporté avec les images des graphiques. */
enum class FormatExport(val extension: String, val libelle: String) {
    MARKDOWN("md", "Markdown"),
    LATEX("tex", "LaTeX")
}

/** Graphiques proposés à l'export ; ceux des rations sont cochés par défaut. */
internal val graphiquesExportables = listOf(
        ChartType.RATIONS_ENERGIE,
        ChartType.DENSITE_RATIONS,
        ChartType.NUTRIMENTS_RATIONS,
        ChartType.EVOLUTION_POIDS
)

/**
 * Rations des consultations affichées, numérotées comme sur les graphiques ([numeroRationGraphique]) ;
 * les sommes « Σ » sont calculées, pas saisies : elles n'ont pas de tableau d'aliments.
 */
internal fun rationsPourExport(consultations: List<ConsultationEv>): List<RationExportee> =
        consultations.flatMapIndexed { index, consultation ->
            consultation.rations
                    .filterNot { estSommeGraphique(it.uuid) }
                    .map { ration ->
                        RationExportee(
                                repere = numeroRationGraphique(index, consultation.rations, ration).toString(),
                                nom = ration.name,
                                consultation = consultation.date?.enTexte().orEmpty(),
                                actuelle = ration.actual,
                                aliments = ration.alimentMutableList.map { a ->
                                    AlimentExporte(
                                            nom = a.aliment?.nom.orEmpty(),
                                            marque = a.aliment?.brand.orEmpty(),
                                            quantite = a.quantite
                                    )
                                }
                        )
                    }
        }

/** Libellés du document dans la langue de l'interface. */
internal fun libellesExport(): LibellesExport {
    val langue = LocalizationManager.currentLocale
    return LibellesExport(
            graphiques = translate("graph.export.doc.charts"),
            rations = translate("graph.export.doc.rations"),
            aliment = translate("graph.export.doc.food"),
            marque = translate("graph.export.doc.brand"),
            quantite = translate("graph.export.doc.quantity"),
            part = translate("graph.export.doc.share"),
            total = translate("graph.export.doc.total"),
            rationActuelle = translate("graph.export.doc.current"),
            rationProposee = translate("graph.export.doc.proposed"),
            consultation = translate("graph.export.doc.consultation"),
            virguleDecimale = langue == "fr",
            langueLatex = if (langue == "fr") "french" else "english"
    )
}

/** Nom de fichier sans espace ni caractère réservé. */
internal fun nomFichierSur(texte: String): String =
        texte.map { if (it.isLetterOrDigit() && it.code < 128) it else '_' }
                .joinToString("")
                .replace(Regex("_+"), "_")
                .trim('_')
                .ifEmpty { "animal" }

@Composable
internal fun DialogueExportGraphiques(
        onDismiss: () -> Unit,
        onExport: (FormatExport, List<ChartType>) -> Unit
) {
    var format by remember { mutableStateOf(FormatExport.MARKDOWN) }
    var choisis by remember { mutableStateOf(graphiquesExportables.filter { it != ChartType.EVOLUTION_POIDS }.toSet()) }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(translate("graph.export.title")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
                    Text(translate("graph.export.info"), style = MaterialTheme.typography.caption)
                    Text(translate("graph.export.format"), style = MaterialTheme.typography.subtitle2)
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)) {
                        FormatExport.entries.forEach { f ->
                            Row(
                                    modifier = Modifier.clickable { format = f },
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = format == f, onClick = { format = f })
                                Text(f.libelle)
                            }
                        }
                    }
                    Text(translate("graph.export.charts"), style = MaterialTheme.typography.subtitle2)
                    graphiquesExportables.forEach { type ->
                        Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    choisis = if (type in choisis) choisis - type else choisis + type
                                },
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                    checked = type in choisis,
                                    onCheckedChange = { choisis = if (it) choisis + type else choisis - type }
                            )
                            Text(translate(type.displayName))
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { onExport(format, graphiquesExportables.filter { it in choisis }) }) {
                    Text(translate("graph.export.start"))
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text(translate("general.cancel")) } }
    )
}

/** Fenêtre bloquante pendant la capture des graphiques (l'écran change de graphique). */
@Composable
internal fun DialogueExportEnCours() {
    AlertDialog(
            onDismissRequest = {},
            title = { Text(translate("graph.export.title")) },
            text = {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    Text(translate("graph.export.inProgress"))
                }
            },
            confirmButton = {}
    )
}
