package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Enumer.*

private val couleursCourbes = listOf(
        Color(0xFF1565C0), Color(0xFF008577), Color(0xFFAD1457),
        Color(0xFF795548), Color(0xFF6A1B9A), Color(0xFFEF6C00),
        Color(0xFF455A64), Color(0xFF558B2F)
)

private data class SerieExploration(
        val nom: String,
        val valeurs: List<Double?>,
        val couleur: Color,
        val pointilles: Boolean = false
)

@Composable
internal fun CourbesMultiration(resultat: ResultatExploration) {
    if (resultat.scenarios.isEmpty()) return
    var ouvert by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth().padding(vertical = 10.dp), elevation = 2.dp) {
        Column(Modifier.padding(12.dp)) {
            TextButton(onClick = { ouvert = !ouvert }) {
                Text(if (ouvert) "Masquer les courbes" else "Afficher les courbes de doses et d’apports")
            }
            if (!ouvert) return@Column
            val references = resultat.configuration.references
            var referenceId by remember(resultat) { mutableStateOf(references.first().uuid) }
            val reference = references.first { it.uuid == referenceId }
            val scenariosReference = remember(resultat, referenceId) {
                resultat.scenarios.filter { it.reference.uuid == referenceId }
            }
            val combinaisons = scenariosReference.map { it.combinaison }.distinct()
            var combinaison by remember(resultat, referenceId) { mutableStateOf(combinaisons.first()) }
            val scenarios = remember(resultat, referenceId, combinaison) {
                scenariosReference.filter { it.combinaison == combinaison }
            }
            val poids = scenarios.map { it.poids }.distinct().sorted()
            val ks = scenarios.map { it.k }.distinct().sorted()
            var poidsFixe by remember(resultat, referenceId, combinaison) { mutableStateOf(poids.first()) }
            var kFixe by remember(resultat, referenceId, combinaison) { mutableStateOf(ks.first()) }
            DropdownField(label = "Référentiel", selectedValue = reference, options = references,
                    onValueChange = { referenceId = it.uuid }, valueToString = { it.nom },
                    modifier = Modifier.fillMaxWidth())
            DropdownField(label = "Combinaison d’aliments", selectedValue = combinaison, options = combinaisons,
                    onValueChange = { combinaison = it }, valueToString = { "Combinaison $it" },
                    modifier = Modifier.fillMaxWidth())
            Text(scenarios.first().ration.alimentMutableList.joinToString(" + ") { it.aliment?.nom ?: "?" }, fontSize = 12.sp)
            DropdownField(label = "Poids fixé (kg) — courbes selon K", selectedValue = poidsFixe, options = poids,
                    onValueChange = { poidsFixe = it }, valueToString = { formaterQuantite(it) },
                    modifier = Modifier.fillMaxWidth())
            DropdownField(label = "K fixé — courbes selon le poids", selectedValue = kFixe, options = ks,
                    onValueChange = { kFixe = it }, valueToString = { formaterQuantite(it) },
                    modifier = Modifier.fillMaxWidth())
            val selonK = scenarios.filter { it.poids == poidsFixe }.sortedBy { it.k }
            val selonPoids = scenarios.filter { it.k == kFixe }.sortedBy { it.poids }
            Text("Les courbes utilisent les scénarios du dernier calcul. Les scénarios non calculables ou sans cible sont laissés en interruption.", fontSize = 12.sp)
            CourbeDoses(selonK, true, "Doses selon K — poids fixé à ${formaterQuantite(poidsFixe)} kg")
            CourbeDoses(selonPoids, false, "Doses selon le poids — K fixé à ${formaterQuantite(kFixe)}")
            val nutriments = remember(resultat, referenceId) {
                (reference.getRefMapMin().keys + reference.getRefMapOMin().keys +
                        reference.getRefMapMax().keys + reference.getRefMapOMax().keys)
                        .filter { it != NutrientMain.ENERGIE && it.label !in resultat.configuration.besoinsIgnores }.distinctBy { it.label }.sortedBy { nomTraduitNutriment(it) }
            }
            var choisis by remember(resultat, referenceId) { mutableStateOf(nutriments.take(4)) }
            Text("Apports nutritionnels et normes", style = MaterialTheme.typography.h6)
            Text("Jusqu’à quatre nutriments. Les apports incomplets sont interrompus ; les seuils utilisent le BEE standard comme l’analyse. Les ratios sont calculés sur la ration entière.", fontSize = 12.sp)
            choisis.forEach { n ->
                TextButton(onClick = { choisis = choisis - n }) { Text("Retirer : ${nomTraduitNutriment(n)}") }
            }
            val disponibles = nutriments.filterNot { it in choisis }
            if (choisis.size < 4 && disponibles.isNotEmpty()) {
                DropdownField<Nutrient>(label = "Ajouter un nutriment", selectedValue = null, options = disponibles,
                        onValueChange = { choisis = choisis + it }, valueToString = { nomTraduitNutriment(it) },
                        modifier = Modifier.fillMaxWidth())
            }
            choisis.forEach { n ->
                CourbeNutriment(selonK, n, true, "Selon K — ${formaterQuantite(poidsFixe)} kg")
                CourbeNutriment(selonPoids, n, false, "Selon le poids — K ${formaterQuantite(kFixe)}")
            }
        }
    }
}

private fun ScenarioExploration.estTraceable() =
        statut != StatutScenario.NON_CALCULABLE && statut != StatutScenario.CIBLE_ABSENTE

@Composable
private fun CourbeDoses(scenarios: List<ScenarioExploration>, selonK: Boolean, titre: String) {
    val aliments = scenarios.flatMap { it.ration.alimentMutableList }.mapNotNull { it.aliment }.distinctBy { it.uuid }
    val series = aliments.mapIndexed { i, aliment ->
        SerieExploration(aliment.nom ?: aliment.uuid,
                scenarios.map { s -> if (s.estTraceable())
                    s.ration.alimentMutableList.filter { it.aliment?.uuid == aliment.uuid }.sumOf { it.quantite } else null },
                couleursCourbes[i % couleursCourbes.size])
    }
    GraphiqueExploration(titre, "Dose (g)", scenarios.map { if (selonK) it.k else it.poids }, if (selonK) "K" else "Poids (kg)", series)
}

@Composable
private fun CourbeNutriment(scenarios: List<ScenarioExploration>, nutriment: Nutrient, selonK: Boolean, titre: String) {
    val unite = scenarios.firstNotNullOfOrNull { it.valeursNutritionnelles[nutriment.label]?.unite }?.displayName ?: ""
    // Une valeur incomplète est un apport partiel connu, pas une absence de valeur. Elle doit
    // donc rester visible, par exemple pour l'oméga-6 lorsqu'un seul ingrédient le renseigne.
    val series = listOf(SerieExploration("Apport connu (peut être incomplet)", scenarios.map {
        apportCourbeExploration(it, nutriment)
    }, MaterialTheme.colors.onSurface)) +
            listOf(Reflevel.MIN, Reflevel.OPTIMIN, Reflevel.OPTIMAX, Reflevel.MAX).mapIndexedNotNull { i, niveau ->
                val valeurs = scenarios.map { seuilCourbeExploration(it, nutriment, niveau) }
                if (valeurs.all { it == null }) null else SerieExploration(niveau.name, valeurs,
                        listOf(Color(0xFF1B7837), Color(0xFF5AAE61), Color(0xFFEF8A62), Color(0xFFB2182B))[i], true)
            }
    GraphiqueExploration("${nomTraduitNutriment(nutriment)} — $titre", unite,
            scenarios.map { if (selonK) it.k else it.poids }, if (selonK) "K" else "Poids (kg)", series)
}

@Composable
private fun GraphiqueExploration(titre: String, unite: String, xs: List<Double>, axe: String, series: List<SerieExploration>) {
    Text(titre, style = MaterialTheme.typography.subtitle1, modifier = Modifier.padding(top = 16.dp))
    val valeurs = series.flatMap { it.valeurs }.filterNotNull().filter { it.isFinite() }
    if (valeurs.isEmpty()) {
        Text("Aucune donnée calculable pour cette sélection.", fontSize = 12.sp)
        return
    }
    val ymax = (valeurs.maxOrNull() ?: 1.0).coerceAtLeast(0.001) * 1.08
    val xmin = xs.minOrNull() ?: 0.0
    val xmax = xs.maxOrNull() ?: xmin
    Text(unite, fontSize = 11.sp)
    Row(Modifier.fillMaxWidth().height(210.dp)) {
        Column(Modifier.width(64.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
            (4 downTo 0).forEach { Text(chiffreCourbe(ymax * it / 4), fontSize = 10.sp) }
        }
        Canvas(Modifier.weight(1f).fillMaxHeight().padding(vertical = 7.dp, horizontal = 5.dp)) {
            fun point(i: Int, y: Double) = Offset(
                    if (xmax == xmin) size.width / 2 else ((xs[i] - xmin) / (xmax - xmin) * size.width).toFloat(),
                    ((1 - y / ymax) * size.height).toFloat())
            for (i in 0..4) {
                val y = size.height * i / 4
                drawLine(Color.LightGray, Offset(0f, y), Offset(size.width, y))
            }
            series.forEach { serie ->
                var precedent: Offset? = null
                serie.valeurs.forEachIndexed { i, valeur ->
                    if (valeur == null || !valeur.isFinite()) precedent = null
                    else {
                        val p = point(i, valeur)
                        precedent?.let { drawLine(serie.couleur, it, p, strokeWidth = 2.dp.toPx(),
                                pathEffect = if (serie.pointilles) PathEffect.dashPathEffect(floatArrayOf(8f, 6f)) else null) }
                        drawCircle(serie.couleur, 3.dp.toPx(), p)
                        precedent = p
                    }
                }
            }
        }
    }
    Row(Modifier.fillMaxWidth().padding(start = 64.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        if (xmin == xmax) Text("$axe : ${chiffreCourbe(xmin)} (un seul point)", fontSize = 11.sp)
        else (0..4).forEach { Text(chiffreCourbe(xmin + (xmax - xmin) * it / 4), fontSize = 10.sp) }
    }
    if (xmin != xmax) Text(axe, fontSize = 11.sp)
    series.forEach { serie ->
        Row(Modifier.padding(top = 3.dp)) {
            Box(Modifier.padding(top = 4.dp).size(10.dp).background(serie.couleur))
            Text(" ${serie.nom}${if (serie.pointilles) " (pointillés)" else ""}", fontSize = 11.sp)
        }
    }
}

private fun chiffreCourbe(v: Double): String =
        if (v != 0.0 && kotlin.math.abs(v) < 0.01) v.toString()
        else (kotlin.math.round(v * 100.0) / 100.0).toString()
