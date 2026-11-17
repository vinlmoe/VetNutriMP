package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Data.ReferenceEv
import fr.vetbrain.vetnutri_mp.Data.StatistiquesBoite
import fr.vetbrain.vetnutri_mp.Enumer.NutrientMain
import fr.vetbrain.vetnutri_mp.Export.ComparaisonExportee
import fr.vetbrain.vetnutri_mp.Export.GroupeExporte
import fr.vetbrain.vetnutri_mp.Export.LibellesStatistiques
import fr.vetbrain.vetnutri_mp.Export.MesureStatistiques
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationsMemorisees
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.GraphFormattingUtils
import fr.vetbrain.vetnutri_mp.Utils.isIosPlatform
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

private val couleurGroupeA = VetNutriColors.Primary
private val couleurGroupeB = Color(0xFFE65100)

/**
 * Une carte de boîtes à moustaches : statistiques des groupes A et B (null = aucune valeur).
 * [cleNutriment] est la clé ([NUTRIMENT_OPTIONS]) d'un nutriment ajouté par l'utilisateur, null
 * pour les mesures proposées d'office.
 */
internal data class MesureBoites(
        val titre: String,
        val unite: String,
        val statsA: StatistiquesBoite?,
        val statsB: StatistiquesBoite?,
        val cleNutriment: String? = null
) {
    val titreComplet: String get() = if (unite.isEmpty()) titre else "$titre ($unite)"
}

/** Valeurs d'un nutriment ajouté pour les aliments des groupes A et B (non renseignées exclues). */
internal data class ValeursGroupes(val groupeA: List<Double>, val groupeB: List<Double>)

/** Nutriments déjà comparés par les mesures proposées d'office : pas proposés à l'ajout. */
private val clesMesuresDeBase = setOf("proteine", "lipide", "ena", "phosphore", "calcium")

/** Nutriments proposés d'office calculés comme les nutriments ajoutés (absents d'[AlimentAnalyseData]). */
internal val clesNutrimentsDeBase = listOf("ena")

private fun suffixeBase(useDryMatterPer100g: Boolean) = baseAliments(useDryMatterPer100g)

/**
 * Mesures comparées : celles proposées d'office (masquées quand aucun aliment n'en a), puis les
 * nutriments ajoutés, dans l'ordre d'ajout (toujours affichés, pour pouvoir les retirer).
 */
internal fun mesuresBoites(
        groupeA: List<AlimentAnalyseData>,
        groupeB: List<AlimentAnalyseData>,
        useDryMatterPer100g: Boolean,
        nutrimentsAjoutes: List<String>,
        valeursAjoutees: Map<String, ValeursGroupes>
): List<MesureBoites> {
    val base = suffixeBase(useDryMatterPer100g)
    // 0 pour la densité, P et Ca : composition non renseignée, pas une vraie valeur
    fun positif(v: Double) = v.takeIf { it > 0 }
    fun mesure(titre: String, unite: String, valeur: (AlimentAnalyseData) -> Double?) =
            MesureBoites(titre, unite, StatistiquesBoite.de(groupeA.mapNotNull(valeur)), StatistiquesBoite.de(groupeB.mapNotNull(valeur)))
    val deBase = listOf(
            mesure(
                    translate("graph.comparaison.density"),
                    translate(if (useDryMatterPer100g) "graphAliments.unit.kcalPer100gDm" else "graphAliments.unit.kcalPer100g")
            ) { positif(it.densiteEnergetique) },
            mesure(translate("graph.comparaison.protein"), "g$base") { it.proteinePer1000Kcal },
            mesure(translate("graph.comparaison.fat"), "g$base") { it.lipidePer1000Kcal },
            MesureBoites(
                    translate("graph.comparaison.nfe"), "g$base",
                    valeursAjoutees["ena"]?.groupeA?.let { StatistiquesBoite.de(it) },
                    valeursAjoutees["ena"]?.groupeB?.let { StatistiquesBoite.de(it) }
            ),
            mesure(translate("graph.comparaison.phosphorus"), "g$base") { positif(it.phosphorePer1000Kcal) },
            mesure(translate("graph.comparaison.calcium"), "g$base") { positif(it.calciumPer1000Kcal) },
            mesure(translate("graph.comparaison.caP"), "") { a ->
                if (a.phosphorePer1000Kcal > 0 && a.calciumPer1000Kcal > 0) a.calciumPer1000Kcal / a.phosphorePer1000Kcal
                else null
            }
    ).filter { it.statsA != null || it.statsB != null }
    val ajoutees = nutrimentsAjoutes.mapNotNull { cle ->
        val option = NUTRIMENT_OPTIONS.find { it.key == cle } ?: return@mapNotNull null
        val valeurs = valeursAjoutees[cle]
        MesureBoites(
                titre = option.displayName,
                unite = option.unit + base,
                statsA = valeurs?.groupeA?.let { StatistiquesBoite.de(it) },
                statsB = valeurs?.groupeB?.let { StatistiquesBoite.de(it) },
                cleNutriment = cle
        )
    }
    return deBase + ajoutees
}

/**
 * Nutriments qu'on peut ajouter aux boîtes : renseignés pour au moins un aliment des deux groupes
 * (les nutriments principaux, calculables par les équations du référentiel, toujours), hors
 * mesures proposées d'office.
 */
internal fun nutrimentsAjoutables(groupes: List<AlimentAnalyseData>): List<NutrimentOption> =
        NUTRIMENT_OPTIONS.filter { option ->
            if (option.key.isEmpty() || option.key in clesMesuresDeBase) return@filter false
            val nutriment = nutrimentDeCle(option.key) ?: return@filter false
            nutriment is NutrientMain || groupes.any { (it.aliment.getNutrient(nutriment) ?: 0.0) > 0 }
        }

/**
 * Valeurs (/1000 kcal ou /100 g MS, comme les mesures de base) des nutriments [cles] pour chaque
 * aliment des deux groupes, hors du fil de l'interface ; 0 = non renseigné, exclu.
 */
internal suspend fun valeursNutrimentsGroupes(
        cles: List<String>,
        groupeA: List<AlimentAnalyseData>,
        groupeB: List<AlimentAnalyseData>,
        referenceEv: ReferenceEv?,
        equationRepository: EquationRepository?,
        useDryMatterPer100g: Boolean
): Map<String, ValeursGroupes> {
    if (cles.isEmpty()) return emptyMap()
    val equations = equationRepository?.let { EquationsMemorisees(it) }
    return withContext(AppDispatchers.Default) {
        suspend fun valeurs(groupe: List<AlimentAnalyseData>, cle: String) =
                groupe.map { it.getNutrimentValue(cle, referenceEv, equations, useDryMatterPer100g) }
                        .filter { it.isFinite() && it > 0 }
        cles.associateWith { cle -> ValeursGroupes(valeurs(groupeA, cle), valeurs(groupeB, cle)) }
    }
}

/** Comparaison exportée avec les graphiques des aliments : statistiques et aliments des groupes. */
internal fun comparaisonPourExport(
        mesures: List<MesureBoites>,
        groupeA: List<AlimentAnalyseData>,
        groupeB: List<AlimentAnalyseData>,
        libelleA: String,
        libelleB: String
): ComparaisonExportee =
        ComparaisonExportee(
                mesures = mesures.map { MesureStatistiques(it.titreComplet, it.statsA, it.statsB) },
                groupeA = GroupeExporte(libelleA, groupeA.map { it.aliment.nom.orEmpty() to it.aliment.brand.orEmpty() }),
                groupeB = GroupeExporte(libelleB, groupeB.map { it.aliment.nom.orEmpty() to it.aliment.brand.orEmpty() })
        )

/**
 * Comparaison du groupe A (aliments analysés) et du groupe B par des boîtes à moustaches, une
 * carte par mesure, sur une échelle commune aux deux groupes. « Ajouter un nutriment » ajoute une
 * carte par nutriment choisi, autant qu'il y a de nutriments renseignés ; la croix d'une carte
 * ajoutée la retire. La zone des boîtes est enregistrée dans [couche] pour l'export commun des
 * graphiques des aliments.
 */
@Composable
internal fun ComparaisonBoitesAMoustaches(
        mesures: List<MesureBoites>,
        libelleA: String,
        libelleB: String,
        groupeBVide: Boolean,
        nutrimentsDisponibles: List<NutrimentOption>,
        nutrimentsAjoutes: List<String>,
        onAjouterNutriment: (String) -> Unit,
        onRetirerNutriment: (String) -> Unit,
        onRetirerTous: () -> Unit,
        couche: GraphicsLayer,
        modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
        Text(
                translate("graph.comparaison.title"),
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = VetNutriColors.Primary
        )
        if (!groupeBVide) {
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                SelecteurNutrimentAjoute(
                        options = nutrimentsDisponibles.filter { it.key !in nutrimentsAjoutes },
                        onChoisi = onAjouterNutriment
                )
                if (nutrimentsAjoutes.isNotEmpty()) {
                    TextButton(onClick = onRetirerTous) { Text(translate("graph.comparaison.removeAll")) }
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
            if (groupeBVide) {
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
                    mesures.chunked(colonnes).forEach { ligne ->
                        Row(horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
                            ligne.forEach { mesure ->
                                CarteMesure(mesure, onRetirerNutriment, Modifier.weight(1f))
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

/** Bouton « Ajouter un nutriment » et liste des nutriments pas encore comparés. */
@Composable
private fun SelecteurNutrimentAjoute(options: List<NutrimentOption>, onChoisi: (String) -> Unit) {
    var ouvert by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { ouvert = true }, enabled = options.isNotEmpty()) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(AppSizes.paddingSmall))
            Text(translate("graph.comparaison.addNutrient"))
        }
        MenuNutriments(
                expanded = ouvert,
                onDismiss = { ouvert = false },
                options = options,
                onChoisi = { onChoisi(it.key) }
        )
    }
}

/** Libellés du tableau des statistiques dans la langue de l'interface. */
internal fun libellesStatistiques(): LibellesStatistiques {
    val langue = LocalizationManager.currentLocale
    return LibellesStatistiques(
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
        mesure: MesureBoites,
        onRetirer: (String) -> Unit,
        modifier: Modifier
) {
    val statsA = mesure.statsA
    val statsB = mesure.statsB
    Card(modifier = modifier, elevation = AppSizes.elevationSmall) {
        Column(modifier = Modifier.padding(AppSizes.paddingMedium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                        mesure.titreComplet,
                        style = MaterialTheme.typography.subtitle2,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                )
                mesure.cleNutriment?.let { cle ->
                    IconButtonWithTooltip(
                            onClick = { onRetirer(cle) },
                            modifier = Modifier.size(28.dp),
                            imageVector = Icons.Default.Close,
                            contentDescription = translate("graph.comparaison.removeNutrient"),
                            tooltip = translate("graph.comparaison.removeNutrient"),
                            iconModifier = Modifier.size(18.dp)
                    )
                }
            }
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
