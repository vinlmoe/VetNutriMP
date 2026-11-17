package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.BasicNumberTextField
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.Avertissement
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ConstantesChienV2
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.EtatModulePrevision
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ModePrevision
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.MotifRefus
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.PreparateurPrevision
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ReglagesPrevisionPoids
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.ResultatPrevision
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.SelectionRationsPrevision
import fr.vetbrain.vetnutri_mp.Data.PoidsChien.selectionPrevision
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.GraphFormattingUtils
import fr.vetbrain.vetnutri_mp.Utils.enTexte
import fr.vetbrain.vetnutri_mp.ViewModel.EtatPrevision
import fr.vetbrain.vetnutri_mp.ViewModel.PrevisionPoidsViewModel
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus

private val couleurBande = VetNutriColors.Primary
private val couleurAlerte = Color(0xFFC62828)

private fun f1(v: Double) = GraphFormattingUtils.formatDecimal(v, 1)
private fun f0(v: Double) = GraphFormattingUtils.formatDecimal(v, 0)

/**
 * Prévision du poids du chien adulte (modèle chien-v2), sous la courbe de poids : bouton
 * d'activation et de réglage, puis, module actif, bande de prévision, besoin apparent, ration
 * proposée (jamais appliquée) et niveaux de déficit. En mode silencieux, les calculs sont faits et
 * journalisés sans être montrés.
 */
@Composable
fun PrevisionPoidsChienView(
        viewModel: PrevisionPoidsViewModel,
        animal: AnimalEv?,
        onEnregistrerConsultation: (ConsultationEv) -> Unit,
        modifier: Modifier = Modifier
) {
    if (animal == null || animal.estTroupeau || animal.getEspece() != Espece.CHIEN) return
    val reglages by viewModel.reglages.collectAsState()
    val etat by viewModel.etat.collectAsState()
    val semaines by viewModel.semainesCible.collectAsState()
    var dialogueReglages by remember { mutableStateOf(false) }
    var dialogueRations by remember { mutableStateOf(false) }

    LaunchedEffect(animal, reglages, semaines) { viewModel.calculer(animal) }

    if (dialogueReglages) {
        DialogueReglagesPrevision(reglages, onDismiss = { dialogueReglages = false }) {
            viewModel.modifierReglages(it)
            dialogueReglages = false
        }
    }
    if (dialogueRations) {
        DialogueRationsSuivi(animal, onDismiss = { dialogueRations = false }) { c, sel ->
            onEnregistrerConsultation(viewModel.avecSelection(c, sel))
        }
    }

    Card(modifier = modifier.fillMaxWidth(), elevation = AppSizes.elevationSmall) {
        Column(Modifier.padding(AppSizes.paddingMedium), verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                        translate("prevision.title"),
                        style = MaterialTheme.typography.h6,
                        color = VetNutriColors.Primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                )
                if (reglages.afficher) {
                    TextButton(onClick = { dialogueRations = true }) { Text(translate("prevision.rations.button")) }
                }
                OutlinedButton(onClick = { dialogueReglages = true }) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(Modifier.width(AppSizes.paddingSmall))
                    Text(translate(if (reglages.calculer) "prevision.settings" else "prevision.enable"))
                }
            }
            when {
                !reglages.calculer -> Text(translate("prevision.off"), style = MaterialTheme.typography.caption)
                !reglages.afficher -> Text(translate("prevision.silent"), style = MaterialTheme.typography.caption)
                else -> {
                    Text(
                            translate("prevision.experimental"),
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Bold,
                            color = couleurAlerte
                    )
                    ContenuPrevision(etat, reglages, semaines, onSemaines = viewModel::choisirSemainesCible)
                }
            }
        }
    }
}

@Composable
private fun ContenuPrevision(
        etat: EtatPrevision,
        reglages: ReglagesPrevisionPoids,
        semaines: Int,
        onSemaines: (Int) -> Unit
) {
    when (etat) {
        EtatPrevision.Inactif -> {}
        EtatPrevision.Calcul -> Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(AppSizes.paddingSmall))
            Text(translate("prevision.computing"), style = MaterialTheme.typography.body2)
        }
        is EtatPrevision.Erreur -> Text(translate("prevision.error", etat.message), color = couleurAlerte)
        is EtatPrevision.Refus -> Column {
            Text(translate("prevision.refused"), fontWeight = FontWeight.Bold, color = couleurAlerte)
            etat.preparation.motifs.forEach { Text("• " + translate("prevision.refus.${it.name}"), style = MaterialTheme.typography.body2) }
            if (etat.preparation.detail.isNotBlank()) Text(etat.preparation.detail, style = MaterialTheme.typography.caption)
        }
        is EtatPrevision.Resultat -> ResultatPrevisionView(etat.resultat, reglages, semaines, onSemaines)
    }
}

@Composable
private fun ResultatPrevisionView(
        r: ResultatPrevision,
        reglages: ReglagesPrevisionPoids,
        semaines: Int,
        onSemaines: (Int) -> Unit
) {
    val e = r.entrees
    var montrerProposition by remember(r) { mutableStateOf(false) }
    r.avertissements.forEach { a ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(translate("prevision.warning.${a.name}"), style = MaterialTheme.typography.caption)
        }
    }
    Text(
            translate(
                    if (e.mode == ModePrevision.PERTE_DE_POIDS) "prevision.mode.loss" else "prevision.mode.maintenance",
                    e.debut.enTexte(), f1(e.chien.poidsInitial), f1(e.poidsVise)
            ),
            style = MaterialTheme.typography.body2
    )
    Text(
            translate(
                    "prevision.currentRation",
                    (e.debut + DatePeriod(days = e.jourConsultation)).enTexte(),
                    e.nomsRationsFutures.ifBlank { "—" },
                    f0(e.rations.last().kcalParJour)
            ),
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.Bold
    )
    GraphiquePrevision(r, montrerProposition)

    // Encart « Besoin apparent »
    Card(backgroundColor = VetNutriColors.Primary.copy(alpha = 0.08f), elevation = 0.dp) {
        Column(Modifier.padding(AppSizes.paddingSmall).fillMaxWidth()) {
            Text(translate("prevision.need.title"), fontWeight = FontWeight.Bold)
            if (r.estimationDesactivee != null) {
                Text(translate("prevision.need.disabled", r.estimationDesactivee), style = MaterialTheme.typography.body2, color = couleurAlerte)
            } else {
                val b = r.besoinApparent
                Text(
                        translate("prevision.need.value", f0(b.moyenne), f0(b.bas), f0(b.haut), r.peseesUtilisees.size.toString()),
                        style = MaterialTheme.typography.body2
                )
                if (r.peseesUtilisees.isEmpty()) Text(translate("prevision.need.priorOnly"), style = MaterialTheme.typography.caption)
            }
            Text(translate("prevision.need.note"), style = MaterialTheme.typography.caption)
        }
    }

    r.peseesHorsIntervalle.forEach { p ->
        Text(
                translate("prevision.observance", (e.debut + DatePeriod(days = p.jour)).enTexte(), f1(p.poids)),
                style = MaterialTheme.typography.body2,
                color = couleurAlerte
        )
    }

    if (e.mode == ModePrevision.PERTE_DE_POIDS) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(translate("prevision.targetWeeks"), style = MaterialTheme.typography.body2)
            Spacer(Modifier.width(AppSizes.paddingSmall))
            var texte by remember(semaines) { mutableStateOf(semaines.toString()) }
            BasicNumberTextField(
                    value = texte,
                    onValueChange = { v -> texte = v; v.toIntOrNull()?.let(onSemaines) },
                    modifier = Modifier.width(80.dp),
                    allowDecimals = false,
                    allowNegative = false
            )
        }
        r.atteinte?.let { a ->
            Text(
                    when {
                        a.auPlusTot == null -> translate("prevision.reach.beyond")
                        a.auPlusTard == null -> translate("prevision.reach.from", a.auPlusTot.toString())
                        else -> translate("prevision.reach.between", a.auPlusTot.toString(), a.auPlusTard.toString())
                    },
                    style = MaterialTheme.typography.body2
            )
        }
    }

    // Ration proposée : une proposition au clinicien
    Button(onClick = { montrerProposition = true }) { Text(translate("prevision.propose")) }
    if (montrerProposition) {
        val p = r.proposition
        when {
            r.estimationDesactivee != null || r.peseesUtilisees.isEmpty() ->
                Text(translate("prevision.propose.noWeighing"), style = MaterialTheme.typography.body2)
            p == null -> {}
            p.bloquee -> Column {
                Text(translate("prevision.propose.blocked", f0(p.kcalParJour)), fontWeight = FontWeight.Bold, color = couleurAlerte)
                if (p.sousPlancher) {
                    Text(
                            translate("prevision.propose.floor", f0(p.plancherKcal), f0(reglages.plancherKcalParKgMetabolique)),
                            style = MaterialTheme.typography.body2, color = couleurAlerte
                    )
                }
                if (p.carences.isNotEmpty()) {
                    Text(translate("prevision.propose.deficits", p.carences.joinToString(", ")), style = MaterialTheme.typography.body2, color = couleurAlerte)
                }
                if (e.mode == ModePrevision.PERTE_DE_POIDS) Text(translate("prevision.propose.lengthen"), style = MaterialTheme.typography.body2)
            }
            else -> Column {
                Text(
                        translate(
                                "prevision.propose.value", f0(p.kcalParJour),
                                (e.debut + DatePeriod(days = p.jourDebut)).enTexte(), f1(p.poidsCible),
                                (e.debut + DatePeriod(days = p.jourCible)).enTexte()
                        ),
                        fontWeight = FontWeight.Bold
                )
                Text(translate("prevision.propose.validate"), style = MaterialTheme.typography.caption)
            }
        }
    }

    TableauDeficits(r)
}

/** Tableau des niveaux de déficit (choix initial), chien de besoin de population. */
@Composable
private fun TableauDeficits(r: ResultatPrevision) {
    Text(translate("prevision.deficits.title"), fontWeight = FontWeight.Bold)
    Text(
            r.entrees.besoinSansDeficit?.let { translate("prevision.deficits.note", f0(it)) }
                    ?: translate("prevision.deficits.noteEi0"),
            style = MaterialTheme.typography.caption
    )
    val entetes = listOf(translate("prevision.deficits.share"), "kcal/j", translate("prevision.deficits.loss"), "S12", "S26", "S52")
    Row(Modifier.fillMaxWidth()) {
        entetes.forEach { Text(it, Modifier.weight(1f), style = MaterialTheme.typography.caption, fontWeight = FontWeight.Bold) }
    }
    r.niveauxDeficit.forEach { n ->
        Row(Modifier.fillMaxWidth()) {
            listOf(
                    f0(n.fractionBesoin * 100),
                    f0(n.kcalParJour),
                    GraphFormattingUtils.formatDecimal(n.perteSemaines1a4, 2),
                    "${f1(n.poidsSemaine12.moyenne)} [${f1(n.poidsSemaine12.bas)}–${f1(n.poidsSemaine12.haut)}]",
                    "${f1(n.poidsSemaine26.moyenne)} [${f1(n.poidsSemaine26.bas)}–${f1(n.poidsSemaine26.haut)}]",
                    "${f1(n.poidsSemaine52.moyenne)} [${f1(n.poidsSemaine52.bas)}–${f1(n.poidsSemaine52.haut)}]"
            ).forEach { Text(it, Modifier.weight(1f), style = MaterialTheme.typography.caption) }
        }
    }
}

/**
 * Pesées (pleines si utilisées, creuses sinon, rouges hors intervalle), périodes de ration, bande de
 * prévision à 95 % avec la ration en cours, poids visé et date de la consultation.
 */
@Composable
private fun GraphiquePrevision(r: ResultatPrevision, montrerProposition: Boolean) {
    val e = r.entrees
    val mesureur = rememberTextMeasurer()
    val style = MaterialTheme.typography.caption.copy(fontSize = 10.sp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.75f))
    val grille = MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
    val texteCouleur = MaterialTheme.colors.onSurface
    val jourMax = (listOfNotNull(
            e.jourConsultation + ConstantesChienV2.DELAI_ENTRETIEN_JOURS,
            r.proposition?.jourCible,
            e.pesees.maxOfOrNull { it.jour },
            r.atteinte?.auPlusTard?.let { e.jourConsultation + 7 * it }
    ).max() + 14).coerceAtMost(ConstantesChienV2.HORIZON_MAX_JOURS)
    val bande = r.bande.filter { it.first <= jourMax }
    val valeurs = bande.flatMap { listOf(it.second.bas, it.second.haut) } + e.pesees.map { it.poids } + e.poidsVise
    val graduationsY = graduations(valeurs.min(), valeurs.max())
    val yMin = graduationsY.first()
    val yMax = graduationsY.last()
    val utilisees = r.peseesUtilisees.map { it.jour }.toSet()
    val hors = r.peseesHorsIntervalle.map { it.jour }.toSet()

    Canvas(Modifier.fillMaxWidth().height(260.dp)) {
        val gauche = 40.dp.toPx()
        val bas = size.height - 22.dp.toPx()
        val haut = 16.dp.toPx()
        val largeur = size.width - gauche - 8.dp.toPx()
        fun x(j: Double) = gauche + (j / jourMax).toFloat() * largeur
        fun y(v: Double) = bas - ((v - yMin) / (yMax - yMin)).toFloat() * (bas - haut)

        // Graduations
        graduationsY.forEach { g ->
            drawLine(grille, Offset(gauche, y(g)), Offset(gauche + largeur, y(g)))
            val t = mesureur.measure(GraphFormattingUtils.formatDecimal(g, 1), style)
            drawText(t, topLeft = Offset(gauche - t.size.width - 4f, y(g) - t.size.height / 2f))
        }
        (0..jourMax / 28).map { it * 28 }.forEach { j ->
            val t = mesureur.measure("S${j / 7}", style)
            drawText(t, topLeft = Offset(x(j.toDouble()) - t.size.width / 2f, bas + 4f))
        }
        // Périodes de ration (fond alterné et kcal/j)
        e.rations.forEachIndexed { i, p ->
            val fin = e.rations.getOrNull(i + 1)?.debutJour ?: jourMax
            if (p.debutJour >= jourMax) return@forEachIndexed
            if (i % 2 == 1) {
                drawRect(grille, Offset(x(p.debutJour.toDouble()), haut), androidx.compose.ui.geometry.Size(x(fin.toDouble()) - x(p.debutJour.toDouble()), bas - haut))
            }
            val t = mesureur.measure("${f0(p.kcalParJour)} kcal/j", style)
            drawText(t, topLeft = Offset(x(p.debutJour.toDouble()) + 3f, 0f))
        }
        // Bande à 95 % et moyenne
        val contour = Path().apply {
            bande.forEachIndexed { i, (j, iv) -> if (i == 0) moveTo(x(j.toDouble()), y(iv.haut)) else lineTo(x(j.toDouble()), y(iv.haut)) }
            bande.asReversed().forEach { (j, iv) -> lineTo(x(j.toDouble()), y(iv.bas)) }
            close()
        }
        drawPath(contour, couleurBande.copy(alpha = 0.18f))
        val moyenne = Path().apply {
            bande.forEachIndexed { i, (j, iv) -> if (i == 0) moveTo(x(j.toDouble()), y(iv.moyenne)) else lineTo(x(j.toDouble()), y(iv.moyenne)) }
        }
        drawPath(moyenne, couleurBande, style = Stroke(2.dp.toPx()))
        // Poids visé et consultation
        drawLine(
                Color(0xFF2E7D32), Offset(gauche, y(e.poidsVise)), Offset(gauche + largeur, y(e.poidsVise)),
                strokeWidth = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
        )
        drawLine(texteCouleur.copy(alpha = 0.4f), Offset(x(e.jourConsultation.toDouble()), haut), Offset(x(e.jourConsultation.toDouble()), bas))
        // Pesées (poids initial compris)
        drawCircle(texteCouleur, 4.dp.toPx(), Offset(x(0.0), y(e.chien.poidsInitial)))
        e.pesees.forEach { p ->
            val jour = ModelePoidsJourPair(p.jour)
            val couleur = if (jour in hors) couleurAlerte else texteCouleur
            val centre = Offset(x(p.jour.toDouble()), y(p.poids))
            if (jour in utilisees) drawCircle(couleur, 4.dp.toPx(), centre)
            else drawCircle(couleur, 4.dp.toPx(), centre, style = Stroke(1.5.dp.toPx()))
        }
        if (montrerProposition) {
            r.proposition?.takeIf { !it.bloquee }?.let { p ->
                drawCircle(Color(0xFF2E7D32), 5.dp.toPx(), Offset(x(p.jourCible.toDouble()), y(p.poidsCible)), style = Stroke(2.dp.toPx()))
            }
        }
    }
    Text(translate("prevision.chart.legend"), style = MaterialTheme.typography.caption)
}

private fun ModelePoidsJourPair(jour: Int) = fr.vetbrain.vetnutri_mp.Data.PoidsChien.ModelePoidsChienV2.jourPair(jour)

/** État du module (désactivé, silencieux, actif) et plancher d'apport. */
@Composable
private fun DialogueReglagesPrevision(
        reglages: ReglagesPrevisionPoids,
        onDismiss: () -> Unit,
        onValider: (ReglagesPrevisionPoids) -> Unit
) {
    var etat by remember { mutableStateOf(reglages.etat) }
    var plancher by remember { mutableStateOf(f0(reglages.plancherKcalParKgMetabolique)) }
    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(translate("prevision.settings.title")) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
                    EtatModulePrevision.entries.forEach { e ->
                        Row(Modifier.fillMaxWidth().clickable { etat = e }, verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = etat == e, onClick = { etat = e })
                            Column {
                                Text(translate("prevision.state.${e.name}"))
                                Text(translate("prevision.state.${e.name}.info"), style = MaterialTheme.typography.caption)
                            }
                        }
                    }
                    Text(translate("prevision.floor.label"), style = MaterialTheme.typography.subtitle2)
                    BasicNumberTextField(value = plancher, onValueChange = { plancher = it }, modifier = Modifier.width(120.dp))
                    Text(translate("prevision.floor.info"), style = MaterialTheme.typography.caption)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val valeur = plancher.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }
                            ?: ReglagesPrevisionPoids.PLANCHER_PAR_DEFAUT
                    onValider(ReglagesPrevisionPoids(etat, valeur))
                }) { Text(translate("general.save")) }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text(translate("general.cancel")) } }
    )
}

/**
 * Rations du suivi, consultation par consultation : ration réellement donnée avant (actuelle, le
 * passé) et ration donnée à partir de la consultation (proposée, le futur du modèle) ; plusieurs
 * rations cochées sont additionnées. « Début du suivi » fixe la première consultation du suivi.
 */
@Composable
private fun DialogueRationsSuivi(
        animal: AnimalEv,
        onDismiss: () -> Unit,
        onEnregistrer: (ConsultationEv, SelectionRationsPrevision) -> Unit
) {
    val datees = animal.consultations.filter { it.date != null }.sortedBy { it.date }
    val suivi = PreparateurPrevision.consultationsDuSuivi(datees).map { it.uuid }.toSet()
    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(translate("prevision.rations.title")) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
                    Text(translate("prevision.rations.info"), style = MaterialTheme.typography.caption)
                    datees.asReversed().forEach { c ->
                        var selection by remember(c.uuid, c.selectionPrevisionPoids) { mutableStateOf(c.selectionPrevision()) }
                        fun changer(s: SelectionRationsPrevision) {
                            selection = s
                            onEnregistrer(c, s)
                        }
                        Card(elevation = 0.dp, backgroundColor = if (c.uuid in suivi) VetNutriColors.Primary.copy(alpha = 0.06f) else Color.Transparent) {
                            Column(Modifier.padding(AppSizes.paddingSmall)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                            "${c.date?.enTexte()} — ${c.weight?.let { f1(it) + " kg" } ?: "?"}",
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                    )
                                    Checkbox(checked = selection.debutSuivi, onCheckedChange = { changer(selection.copy(debutSuivi = it)) })
                                    Text(translate("prevision.rations.start"), style = MaterialTheme.typography.caption)
                                }
                                val rations = c.rations.filter { it.refRationParente == null }
                                if (rations.isEmpty()) Text(translate("prevision.rations.none"), style = MaterialTheme.typography.caption)
                                else {
                                    Text(translate("prevision.rations.past"), style = MaterialTheme.typography.caption, fontWeight = FontWeight.Bold)
                                    rations.forEach { ration ->
                                        LigneRation(ration.name, ration.uuid in selection.actuelles) { coche ->
                                            changer(selection.copy(actuelles = if (coche) selection.actuelles + ration.uuid else selection.actuelles - ration.uuid))
                                        }
                                    }
                                    Text(translate("prevision.rations.future"), style = MaterialTheme.typography.caption, fontWeight = FontWeight.Bold)
                                    rations.forEach { ration ->
                                        LigneRation(ration.name, ration.uuid in selection.proposees) { coche ->
                                            changer(selection.copy(proposees = if (coche) selection.proposees + ration.uuid else selection.proposees - ration.uuid))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text(translate("general.close")) } }
    )
}

@Composable
private fun LigneRation(nom: String, coche: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onChange(!coche) }, verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = coche, onCheckedChange = onChange)
        Text(nom, style = MaterialTheme.typography.body2)
    }
}

/** Libellé d'un motif de refus (tests et messages). */
internal fun cleMotifRefus(m: MotifRefus) = "prevision.refus.${m.name}"

/** Libellé d'un avertissement. */
internal fun cleAvertissement(a: Avertissement) = "prevision.warning.${a.name}"
