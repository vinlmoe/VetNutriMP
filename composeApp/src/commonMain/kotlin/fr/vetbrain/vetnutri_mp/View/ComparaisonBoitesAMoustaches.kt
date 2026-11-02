package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import fr.vetbrain.vetnutri_mp.Export.ArchiveZip
import fr.vetbrain.vetnutri_mp.Export.DocumentComparaison
import fr.vetbrain.vetnutri_mp.Export.DocumentComparaisonGroupes
import fr.vetbrain.vetnutri_mp.Export.GraphiqueExporte
import fr.vetbrain.vetnutri_mp.Export.GroupeExporte
import fr.vetbrain.vetnutri_mp.Export.LibellesStatistiques
import fr.vetbrain.vetnutri_mp.Export.MesureStatistiques
import fr.vetbrain.vetnutri_mp.Export.encoderJpeg
import fr.vetbrain.vetnutri_mp.Export.exporterZip
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.AppLog
import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import fr.vetbrain.vetnutri_mp.View.AnalyseGraphique.FormatExport
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Data.StatistiquesBoite
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.GraphFormattingUtils
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

private val couleurGroupeA = VetNutriColors.Primary
private val couleurGroupeB = Color(0xFFE65100)

/** Mesure comparée entre les groupes ; null = valeur absente (exclue de la boîte). */
private class MesureComparee(
        val titre: String,
        val unite: String,
        val valeur: (AlimentAnalyseData) -> Double?
)

private fun mesuresComparees(useDryMatterPer100g: Boolean): List<MesureComparee> {
    val base = if (useDryMatterPer100g) "/100 g MS" else "/1000 kcal"
    // 0 pour la densité, les glucides, P et Ca : composition non renseignée, pas une vraie valeur
    fun positif(v: Double) = v.takeIf { it > 0 }
    return listOf(
            MesureComparee(
                    translate("graph.comparaison.density"),
                    if (useDryMatterPer100g) "kcal/100 g MS" else "kcal/100 g"
            ) { positif(it.densiteEnergetique) },
            MesureComparee(translate("graph.comparaison.protein"), "g$base") { it.proteinePer1000Kcal },
            MesureComparee(translate("graph.comparaison.fat"), "g$base") { it.lipidePer1000Kcal },
            MesureComparee(translate("graph.comparaison.nfe"), "g$base") { positif(it.glucidePer1000Kcal) },
            MesureComparee(translate("graph.comparaison.phosphorus"), "g$base") { positif(it.phosphorePer1000Kcal) },
            MesureComparee(translate("graph.comparaison.calcium"), "g$base") { positif(it.calciumPer1000Kcal) },
            MesureComparee(translate("graph.comparaison.caP"), "") { a ->
                if (a.phosphorePer1000Kcal > 0 && a.calciumPer1000Kcal > 0) a.calciumPer1000Kcal / a.phosphorePer1000Kcal
                else null
            },
            MesureComparee(translate("graph.comparaison.proteinEnergy"), "%") { positif(it.pourcentageProteines) },
            MesureComparee(translate("graph.comparaison.fatEnergy"), "%") { positif(it.pourcentageLipides) }
    )
}

/**
 * Comparaison du groupe A (aliments analysés) et du groupe B par des boîtes à moustaches, une
 * carte par mesure, sur une échelle commune aux deux groupes. « Exporter » enregistre l'image des
 * boîtes et le tableau des statistiques (Markdown ou LaTeX, archive ZIP).
 */
@Composable
internal fun ComparaisonBoitesAMoustaches(
        groupeA: List<AlimentAnalyseData>,
        groupeB: List<AlimentAnalyseData>,
        useDryMatterPer100g: Boolean,
        modifier: Modifier = Modifier,
        nomGroupeA: String = "",
        nomGroupeB: String = ""
) {
    val libelleA = libelleGroupe("A", nomGroupeA, groupeA.size)
    val libelleB = libelleGroupe("B", nomGroupeB, groupeB.size)
    val mesures = remember(useDryMatterPer100g) { mesuresComparees(useDryMatterPer100g) }
    val statistiques = remember(groupeA, groupeB, mesures) {
        mesures.map { m ->
            Triple(m, StatistiquesBoite.de(groupeA.mapNotNull(m.valeur)), StatistiquesBoite.de(groupeB.mapNotNull(m.valeur)))
        }
                // Mesure renseignée pour aucun aliment des deux groupes (ex. glucides) : masquée
                .filter { (_, a, b) -> a != null || b != null }
    }
    val couche = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    var dialogue by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    fun exporter(format: FormatExport) {
        scope.launch {
            try {
                val image = couche.toImageBitmap()
                val jpeg = withContext(AppDispatchers.Default) { encoderJpeg(image) }
                val nomA = libelleA
                val nomB = libelleB
                val document = DocumentComparaison(
                        titre = translate("graph.comparaison.title"),
                        sousTitre = "$nomA / $nomB",
                        image = GraphiqueExporte("images/comparaison.jpg", translate("graph.comparaison.title")),
                        mesures = statistiques.map { (m, a, b) ->
                            MesureStatistiques(if (m.unite.isEmpty()) m.titre else "${m.titre} (${m.unite})", a, b)
                        },
                        groupeA = GroupeExporte(nomA, groupeA.map { it.aliment.nom.orEmpty() to it.aliment.brand.orEmpty() }),
                        groupeB = GroupeExporte(nomB, groupeB.map { it.aliment.nom.orEmpty() to it.aliment.brand.orEmpty() })
                )
                val texte = when (format) {
                    FormatExport.MARKDOWN -> DocumentComparaisonGroupes.markdown(document, libellesStatistiques())
                    FormatExport.LATEX -> DocumentComparaisonGroupes.latex(document, libellesStatistiques())
                }
                val archive = withContext(AppDispatchers.Default) {
                    ArchiveZip.creer(
                            listOf(
                                    "comparaison_groupes.${format.extension}" to texte.encodeToByteArray(),
                                    "images/comparaison.jpg" to jpeg
                            )
                    )
                }
                // Le dialogue d'enregistrement de bureau doit être ouvert hors du fil de Compose
                val enregistre = withContext(AppDispatchers.IO) { exporterZip(archive, "comparaison_groupes.zip") }
                message =
                        if (enregistre) translate("graph.export.success", "comparaison_groupes.zip")
                        else translate("graph.export.cancelled")
            } catch (e: Exception) {
                e.rethrowIfCancellation()
                AppLog.e("ComparaisonBoitesAMoustaches", e.message ?: "Erreur", e)
                message = translate("graph.export.error", e.message.orEmpty())
            }
        }
    }

    if (dialogue) {
        DialogueFormatExport(
                onDismiss = { dialogue = false },
                onExport = { format ->
                    dialogue = false
                    exporter(format)
                }
        )
    }
    message?.let { texte ->
        AlertDialog(
                onDismissRequest = { message = null },
                text = { Text(texte) },
                confirmButton = { TextButton(onClick = { message = null }) { Text(translate("general.close")) } }
        )
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                    translate("graph.comparaison.title"),
                    style = MaterialTheme.typography.h6,
                    fontWeight = FontWeight.Bold,
                    color = VetNutriColors.Primary,
                    modifier = Modifier.weight(1f)
            )
            if (groupeB.isNotEmpty()) {
                OutlinedButton(onClick = { dialogue = true }) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(Modifier.width(AppSizes.paddingSmall))
                    Text(translate("graph.export.button"))
                }
            }
        }
        // Zone enregistrée dans « couche » pour l'image exportée (fond blanc à l'encodage)
        Column(
                modifier = Modifier.fillMaxWidth().drawWithContent {
                    couche.record { this@drawWithContent.drawContent() }
                    drawLayer(couche)
                },
                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
        ) {
            Text(translate("graph.comparaison.subtitle"), style = MaterialTheme.typography.caption)
            Row(horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)) {
                PastilleGroupe(couleurGroupeA, libelleA)
                PastilleGroupe(couleurGroupeB, libelleB)
            }
            if (groupeB.isEmpty()) {
                Text(
                        translate("graph.comparaison.empty"),
                        style = MaterialTheme.typography.body2,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
                return@Column
            }
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val colonnes = if (maxWidth >= 900.dp) 2 else 1
                Column(verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
                    statistiques.chunked(colonnes).forEach { ligne ->
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
                            ligne.forEach { (mesure, statsA, statsB) ->
                                CarteMesure(mesure, statsA, statsB, Modifier.weight(1f))
                            }
                            // Dernière ligne incomplète : garder la largeur des cartes
                            repeat(colonnes - ligne.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

/** Libellés du tableau des statistiques dans la langue de l'interface. */
private fun libellesStatistiques(): LibellesStatistiques {
    val langue = LocalizationManager.currentLocale
    return LibellesStatistiques(
            graphique = translate("graph.comparaison.title"),
            statistiques = translate("graph.stats.title"),
            mesure = translate("graph.stats.measure"),
            groupe = translate("graph.stats.group"),
            moyenne = translate("graph.stats.mean"),
            ecartType = translate("graph.stats.sd"),
            minimum = translate("graph.stats.min"),
            mediane = translate("graph.stats.median"),
            maximum = translate("graph.stats.max"),
            aliments = translate("graph.stats.foods"),
            aliment = translate("graph.export.doc.food"),
            marque = translate("graph.export.doc.brand"),
            virguleDecimale = langue == "fr",
            langueLatex = if (langue == "fr") "french" else "english"
    )
}

@Composable
private fun DialogueFormatExport(onDismiss: () -> Unit, onExport: (FormatExport) -> Unit) {
    var format by remember { mutableStateOf(FormatExport.MARKDOWN) }
    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(translate("graph.stats.exportTitle")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
                    Text(translate("graph.stats.exportInfo"), style = MaterialTheme.typography.caption)
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)) {
                        FormatExport.entries.forEach { f ->
                            Row(modifier = Modifier.clickable { format = f }, verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = format == f, onClick = { format = f })
                                Text(f.libelle)
                            }
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { onExport(format) }) { Text(translate("graph.export.start")) } },
            dismissButton = { TextButton(onClick = onDismiss) { Text(translate("general.cancel")) } }
    )
}

/**
 * Libellé d'un groupe : « Groupe A (32) », ou « A — Croquettes rénales (32) » quand il a un nom
 * (la lettre reste celle des lignes des boîtes).
 */
internal fun libelleGroupe(lettre: String, nom: String, nombre: Int): String =
        if (nom.isBlank()) translate(if (lettre == "A") "graph.groupe.a" else "graph.groupe.b", nombre.toString())
        else "$lettre — ${nom.trim()} ($nombre)"

@Composable
private fun PastilleGroupe(couleur: Color, libelle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(12.dp)) { drawRect(couleur) }
        Spacer(Modifier.width(AppSizes.paddingSmall))
        Text(libelle, style = MaterialTheme.typography.body2, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CarteMesure(
        mesure: MesureComparee,
        statsA: StatistiquesBoite?,
        statsB: StatistiquesBoite?,
        modifier: Modifier
) {
    Card(modifier = modifier, elevation = AppSizes.elevationSmall) {
        Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
            Text(
                    if (mesure.unite.isEmpty()) mesure.titre else "${mesure.titre} (${mesure.unite})",
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold
            )
            val bornes = listOfNotNull(statsA, statsB)
            if (bornes.isEmpty()) {
                Text("—", style = MaterialTheme.typography.body2)
                return@Column
            }
            val min = bornes.minOf { it.minimum }
            val max = bornes.maxOf { it.maximum }
            val graduations = graduations(min, max)
            val echelle = graduations.first()..graduations.last()
            BoitesSurEchelle(statsA, statsB, echelle, graduations)
            listOf(Triple("A", statsA, couleurGroupeA), Triple("B", statsB, couleurGroupeB)).forEach { (nom, s, couleur) ->
                Text(
                        if (s == null) "$nom : —"
                        else "$nom : " + translate(
                                "graph.comparaison.summary",
                                formater(s.mediane, echelle), formater(s.moyenne, echelle), s.n.toString()
                        ),
                        style = MaterialTheme.typography.caption,
                        color = couleur
                )
            }
        }
    }
}

@Composable
private fun BoitesSurEchelle(
        statsA: StatistiquesBoite?,
        statsB: StatistiquesBoite?,
        echelle: ClosedFloatingPointRange<Double>,
        graduations: List<Double>
) {
    val mesureur = rememberTextMeasurer()
    val styleGraduation = MaterialTheme.typography.caption.copy(fontSize = 10.sp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f))
    val styleGroupe = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold)
    val grille = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
    Canvas(modifier = Modifier.fillMaxWidth().height(96.dp)) {
        val marge = 20.dp.toPx()
        val largeur = size.width - marge - 8.dp.toPx()
        val hauteurLigne = 30.dp.toPx()
        fun x(v: Double): Float =
                marge + ((v - echelle.start) / (echelle.endInclusive - echelle.start)).toFloat() * largeur

        graduations.forEach { g ->
            drawLine(grille, Offset(x(g), 0f), Offset(x(g), hauteurLigne * 2), strokeWidth = 1f)
            val texte = mesureur.measure(formater(g, echelle), styleGraduation)
            drawText(
                    texte,
                    topLeft = Offset(
                            (x(g) - texte.size.width / 2f).coerceIn(0f, size.width - texte.size.width),
                            hauteurLigne * 2 + 4.dp.toPx()
                    )
            )
        }
        listOf(Triple("A", statsA, couleurGroupeA), Triple("B", statsB, couleurGroupeB)).forEachIndexed { i, (nom, s, couleur) ->
            val centre = hauteurLigne * i + hauteurLigne / 2
            val lettre = mesureur.measure(nom, styleGroupe.copy(color = couleur))
            drawText(lettre, topLeft = Offset(0f, centre - lettre.size.height / 2f))
            if (s == null) return@forEachIndexed
            val demi = hauteurLigne * 0.32f
            val trait = 1.5.dp.toPx()
            // Moustaches et leurs extrémités
            drawLine(couleur, Offset(x(s.moustacheBasse), centre), Offset(x(s.q1), centre), strokeWidth = trait)
            drawLine(couleur, Offset(x(s.q3), centre), Offset(x(s.moustacheHaute), centre), strokeWidth = trait)
            listOf(s.moustacheBasse, s.moustacheHaute).forEach { v ->
                drawLine(couleur, Offset(x(v), centre - demi / 2), Offset(x(v), centre + demi / 2), strokeWidth = trait)
            }
            // Boîte (quartiles) et médiane
            val gauche = x(s.q1)
            val boite = Size((x(s.q3) - gauche).coerceAtLeast(1f), demi * 2)
            drawRect(couleur.copy(alpha = 0.22f), Offset(gauche, centre - demi), boite)
            drawRect(couleur, Offset(gauche, centre - demi), boite, style = Stroke(trait))
            drawLine(couleur, Offset(x(s.mediane), centre - demi), Offset(x(s.mediane), centre + demi), strokeWidth = 3.dp.toPx())
            // Moyenne : losange blanc cerclé
            val xm = x(s.moyenne)
            val r = 5.dp.toPx()
            val losange = Path().apply {
                moveTo(xm, centre - r); lineTo(xm + r, centre); lineTo(xm, centre + r); lineTo(xm - r, centre); close()
            }
            drawPath(losange, Color.White)
            drawPath(losange, couleur, style = Stroke(trait))
            // Valeurs isolées
            s.isolees.forEach { v -> drawCircle(couleur, radius = 3.dp.toPx(), center = Offset(x(v), centre), style = Stroke(trait)) }
        }
    }
}

/** Graduations « rondes » (1, 2, 5 × 10ⁿ) couvrant [min, max], quatre à six en général. */
internal fun graduations(min: Double, max: Double): List<Double> {
    val etendue = (max - min).takeIf { it > 0 } ?: maxOf(kotlin.math.abs(max), 1.0)
    val brut = etendue / 4
    val puissance = 10.0.pow(floor(log10(brut)))
    val pas = listOf(1.0, 2.0, 5.0, 10.0).map { it * puissance }.first { it >= brut }
    val debut = floor(min / pas) * pas
    val fin = ceil(max / pas) * pas
    val nombre = ((fin - debut) / pas).toInt().coerceAtLeast(1)
    return (0..nombre).map { debut + it * pas }
}

private fun formater(v: Double, echelle: ClosedFloatingPointRange<Double>): String =
        GraphFormattingUtils.formatAxisTick(v.toFloat(), echelle.start.toFloat()..echelle.endInclusive.toFloat())
