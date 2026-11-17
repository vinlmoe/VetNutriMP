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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.layer.GraphicsLayer
import fr.vetbrain.vetnutri_mp.Export.ArchiveZip
import fr.vetbrain.vetnutri_mp.Export.GraphiqueExporte
import fr.vetbrain.vetnutri_mp.Export.encoderJpeg
import fr.vetbrain.vetnutri_mp.Export.exporterZip
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import androidx.compose.animation.core.snap
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import fr.vetbrain.vetnutri_mp.Utils.AppLog
import io.github.koalaplot.core.style.KoalaPlotTheme
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.withContext

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
    DialogueChoixGraphiques(
            titre = translate("graph.export.title"),
            info = translate("graph.export.info"),
            graphiques = graphiquesExportables,
            libelle = { translate(it.displayName) },
            choisisParDefaut = graphiquesExportables.filter { it != ChartType.EVOLUTION_POIDS }.toSet(),
            onDismiss = onDismiss,
            onExport = onExport
    )
}

/**
 * Choix du format et des graphiques à exporter, commun aux graphiques des rations et des aliments.
 * [graphiques] sont proposés dans leur ordre d'export.
 */
@Composable
internal fun <T> DialogueChoixGraphiques(
        titre: String,
        info: String,
        graphiques: List<T>,
        libelle: (T) -> String,
        choisisParDefaut: Set<T>,
        onDismiss: () -> Unit,
        onExport: (FormatExport, List<T>) -> Unit
) {
    var format by remember { mutableStateOf(FormatExport.MARKDOWN) }
    var choisis by remember { mutableStateOf(choisisParDefaut) }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(titre) },
            text = {
                Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                ) {
                    Text(info, style = MaterialTheme.typography.caption)
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
                    graphiques.forEach { type ->
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
                            Text(libelle(type))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = { onExport(format, graphiques.filter { it in choisis }) },
                        enabled = choisis.isNotEmpty()
                ) {
                    Text(translate("graph.export.start"))
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text(translate("general.cancel")) } }
    )
}

/**
 * Calculs en cours (données de graphiques calculées en tâche de fond) dans la zone exportée :
 * l'export attend qu'il n'y en ait plus avant de capturer un graphique.
 */
@Stable
internal class SuiviCalculsGraphiques {
    var enCours by mutableIntStateOf(0)
        internal set
}

internal val LocalSuiviCalculsGraphiques = staticCompositionLocalOf<SuiviCalculsGraphiques?> { null }

/** Signale à l'export, tant que [enCours] est vrai, qu'un calcul de graphique n'est pas terminé. */
@Composable
internal fun SignalerCalculGraphique(enCours: Boolean) {
    val suivi = LocalSuiviCalculsGraphiques.current ?: return
    DisposableEffect(suivi, enCours) {
        if (enCours) suivi.enCours++
        onDispose { if (enCours) suivi.enCours-- }
    }
}

/**
 * Zone des graphiques exportables : fournit [suivi] aux graphiques et, pendant l'export, coupe les
 * animations des tracés pour que l'image capturée soit celle du graphique terminé.
 */
@Composable
internal fun ZoneGraphiquesExportables(
        suivi: SuiviCalculsGraphiques,
        exportEnCours: Boolean,
        content: @Composable () -> Unit
) {
    // Thème des tracés repris tel quel (KoalaPlotTheme.*), seule l'animation change pendant l'export
    KoalaPlotTheme(
            sizes = KoalaPlotTheme.sizes,
            animationSpec = if (exportEnCours) snap() else KoalaPlotTheme.animationSpec,
            axis = KoalaPlotTheme.axis,
            legendLocation = KoalaPlotTheme.legendLocation
    ) {
        CompositionLocalProvider(LocalSuiviCalculsGraphiques provides suivi, content = content)
    }
}

/**
 * Attend que le graphique affiché ait fini ses calculs : aucun calcul signalé pendant quelques
 * images consécutives (le temps qu'un graphique qui vient d'apparaître signale le sien). Au-delà de
 * [delaiMaxMs], capture quand même (et le journalise) plutôt que de bloquer l'export.
 */
internal suspend fun attendreGraphiquePret(suivi: SuiviCalculsGraphiques, delaiMaxMs: Long = 30_000) {
    val pret = withTimeoutOrNull(delaiMaxMs) {
        var imagesSansCalcul = 0
        while (imagesSansCalcul < 3) {
            withFrameNanos {}
            imagesSansCalcul = if (suivi.enCours == 0) imagesSansCalcul + 1 else 0
        }
    }
    if (pret == null) AppLog.w("ExportGraphiques", "Graphique capturé avant la fin de ses calculs (délai dépassé)")
}

/**
 * Affiche tour à tour chaque graphique de [types] (l'écran change pendant l'export), attend la fin
 * de ses calculs, puis l'enregistre en JPEG depuis [couche], la zone du graphique affiché.
 */
internal suspend fun <T> capturerGraphiques(
        types: List<T>,
        couche: GraphicsLayer,
        suivi: SuiviCalculsGraphiques,
        afficher: (T) -> Unit,
        fichier: (Int, T) -> String,
        titre: (T) -> String
): List<Pair<GraphiqueExporte, ByteArray>> =
        types.mapIndexed { index, type ->
            afficher(type)
            attendreGraphiquePret(suivi)
            val image = couche.toImageBitmap()
            val jpeg = withContext(AppDispatchers.Default) { encoderJpeg(image) }
            GraphiqueExporte(fichier = fichier(index, type), titre = titre(type)) to jpeg
        }

/**
 * Enregistre l'archive ZIP « [base].zip » (document + images) et rend le message à afficher :
 * enregistrée ou annulée.
 */
internal suspend fun enregistrerArchiveGraphiques(
        base: String,
        format: FormatExport,
        texte: String,
        images: List<Pair<GraphiqueExporte, ByteArray>>
): String {
    val archive = withContext(AppDispatchers.Default) {
        ArchiveZip.creer(
                listOf("$base.${format.extension}" to texte.encodeToByteArray()) +
                        images.map { (g, octets) -> g.fichier to octets }
        )
    }
    // Le dialogue d'enregistrement de bureau doit être ouvert hors du fil de Compose
    val enregistre = withContext(AppDispatchers.IO) { exporterZip(archive, "$base.zip") }
    return if (enregistre) translate("graph.export.success", "$base.zip") else translate("graph.export.cancelled")
}

/** Fenêtre bloquante pendant la capture des graphiques (l'écran change de graphique). */
@Composable
internal fun DialogueExportEnCours(titre: String = translate("graph.export.title")) {
    AlertDialog(
            onDismissRequest = {},
            title = { Text(titre) },
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
