package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.BufferedNumberTextField
import fr.vetbrain.vetnutri_mp.Components.DropdownField
import fr.vetbrain.vetnutri_mp.Components.FlowRow
import fr.vetbrain.vetnutri_mp.Components.PuceFiltre
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.Enumer.NutrientResolver
import fr.vetbrain.vetnutri_mp.ExcelPlatform.isCsvFileOperationsSupported
import fr.vetbrain.vetnutri_mp.ExcelPlatform.openCsvFileForImport
import fr.vetbrain.vetnutri_mp.ExcelPlatform.saveCsvFileForExport
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Service.ExamGradingCsvService
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.GraphFormattingUtils
import fr.vetbrain.vetnutri_mp.Utils.orUserError
import fr.vetbrain.vetnutri_mp.ViewModel.CrossConsultationAnalysisViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ExamGradingViewModel
import io.github.koalaplot.core.bar.DefaultVerticalBar
import io.github.koalaplot.core.bar.VerticalBarPlot
import io.github.koalaplot.core.bar.verticalBarPlotEntry
import io.github.koalaplot.core.line.LinePlot
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.Point
import io.github.koalaplot.core.xygraph.XYGraph
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/** Étapes de la notation, dans l'ordre de travail. */
private enum class EtapeNotation { COPIES, ANALYSE, BAREME, NOTES }

private val bordureNotation = Color(0x1F000000)
private val VertNotation = Color(0xFF2E7D32)
private val RougeNotation = Color(0xFFC62828)
private val AmbreNotation = Color(0xFF8A6D1B)

@Composable
private fun discretNotation(): Color = MaterialTheme.colors.onSurface.copy(alpha = 0.62f)

/** Points sans décimales inutiles : 5, 0.6, 12.75 */
private fun pts(v: Double): String =
        if (v == kotlin.math.floor(v) && kotlin.math.abs(v) < 1e9) v.toLong().toString()
        else ((v * 100.0).roundToInt() / 100.0).toString()

/**
 * Notation d'un exercice d'examen : choix de l'examen parmi les copies présentes, import des copies
 * (CSV Moodle de liens jsonbin), analyse des distributions pour fixer les seuils, barème et notes.
 */
@Composable
fun CrossConsultationGradingView(
        analysisViewModel: CrossConsultationAnalysisViewModel,
        gradingViewModel: ExamGradingViewModel,
        onNavigateBack: () -> Unit,
        /** Ouvre la copie (fiche de l'animal) en lecture seule ; son retour ramène ici. */
        onOpenCopie: (animalId: String, consultationId: String?) -> Unit = { _, _ -> }
) {
    val allItems by analysisViewModel.allItems.collectAsState()
    val rule by gradingViewModel.rule.collectAsState()
    val grades by gradingViewModel.grades.collectAsState()
    val message by gradingViewModel.message.collectAsState()
    val metricDistributions by gradingViewModel.metricDistributions.collectAsState()
    val pointsDisponibles by gradingViewModel.pointsDisponibles.collectAsState()

    // État gardé par le ViewModel : il survit à l'ouverture d'une copie puis au retour
    val examId by gradingViewModel.examIdCourant.collectAsState()
    val exerciseId by gradingViewModel.exerciseIdCourant.collectAsState()
    val etapeIndex by gradingViewModel.etape.collectAsState()
    val etape = EtapeNotation.entries[etapeIndex.coerceIn(0, EtapeNotation.entries.lastIndex)]
    val sauvegardeEnCours by gradingViewModel.sauvegardeEnCours.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { if (allItems.isEmpty()) analysisViewModel.loadConsultations() }

    val examens = remember(allItems) { examensDisponibles(allItems.map { Triple(it.examId, it.examExerciseId, it.animalId) }) }
    // Un seul examen présent : il est choisi d'office
    LaunchedEffect(examens) {
        if (examId.isBlank() && exerciseId.isBlank() && examens.size == 1) {
            gradingViewModel.choisirExamen(examens.first().examId, examens.first().exerciseId)
        }
    }
    val copies = remember(allItems, examId, exerciseId) {
        val e = examId.trim()
        val x = exerciseId.trim()
        if (e.isEmpty() || x.isEmpty()) emptyList()
        else allItems.filter {
            it.examId?.trim()?.equals(e, ignoreCase = true) == true && it.examExerciseId?.trim()?.equals(x, ignoreCase = true) == true
        }
    }
    val idsValides = examId.isNotBlank() && exerciseId.isNotBlank()

    // Changer d'examen recharge son barème et ses notes ; revenir d'une copie ne recharge rien
    LaunchedEffect(examId.trim(), exerciseId.trim()) {
        gradingViewModel.chargerExamenSiNouveau(examId, exerciseId)
    }
    LaunchedEffect(etape, copies.size) {
        if (etape == EtapeNotation.ANALYSE && copies.isNotEmpty()) gradingViewModel.runRmdLikeAnalysis(copies)
    }

    val nbCopies = remember(copies) { copies.map { it.animalId }.distinct().size }
    val etapeFaite: (EtapeNotation) -> Boolean = { e ->
        when (e) {
            EtapeNotation.COPIES -> nbCopies > 0
            EtapeNotation.ANALYSE -> metricDistributions.isNotEmpty()
            EtapeNotation.BAREME -> pointsDisponibles > 0.0
            EtapeNotation.NOTES -> grades.isNotEmpty()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarSimple(title = translate("auto.view.crossconsultationgradingview.notation_examen"), onNavigateBack = onNavigateBack)
        // En-tête fixe : examen, message et étapes restent visibles quand le contenu défile
        Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = AppSizes.paddingMedium).padding(top = 4.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SelecteurExamen(
                    examens = examens,
                    examId = examId,
                    exerciseId = exerciseId,
                    nbCopies = nbCopies,
                    onChoix = { e, x -> gradingViewModel.choisirExamen(e, x) },
                    occupe = sauvegardeEnCours,
                    onSauvegarder = {
                        scope.launch {
                            val json = gradingViewModel.dossierNotationJson(copies.map { it.animalId }.toSet()) ?: return@launch
                            gradingViewModel.signalerSauvegarde(
                                    fr.vetbrain.vetnutri_mp.exportJsonToFile(json, "notation_${examId.trim()}_${exerciseId.trim()}.json"))
                        }
                    },
                    onReprendre = {
                        scope.launch {
                            val texte = fr.vetbrain.vetnutri_mp.openJsonFileContent() ?: return@launch
                            gradingViewModel.restaurerDossier(texte) { analysisViewModel.loadConsultations() }
                        }
                    }
            )
            message?.takeIf { it.isNotBlank() }?.let { texte ->
                BandeauNotation(texte, VetNutriColors.PrimaryVariant, Icons.Default.Info) { gradingViewModel.clearMessage() }
            }
            if (idsValides) BarreEtapes(etape, etapeFaite) { gradingViewModel.choisirEtape(it.ordinal) }
        }
        Divider()
        if (!idsValides) {
            Text(translate("notation.choisirExamen"), color = discretNotation(), fontSize = 13.sp, modifier = Modifier.padding(AppSizes.paddingMedium))
            return@Column
        }
        // Une position de défilement par étape : changer d'étape repart du haut
        key(etape) { Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = AppSizes.paddingMedium, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (etape) {
                EtapeNotation.COPIES -> EtapeCopies(analysisViewModel, gradingViewModel, examId.trim(), exerciseId.trim(), copies, nbCopies, onOpenCopie)
                EtapeNotation.ANALYSE -> EtapeAnalyse(gradingViewModel, copies, nbCopies)
                EtapeNotation.BAREME -> EtapeBareme(gradingViewModel, copies)
                EtapeNotation.NOTES -> EtapeNotes(gradingViewModel, copies, examId.trim(), exerciseId.trim(), onOpenCopie)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                if (etape.ordinal > 0) {
                    OutlinedButton(onClick = { gradingViewModel.choisirEtape(etape.ordinal - 1) }) {
                        Text(translate("auto.view.crossconsultationgradingview.precedent"))
                    }
                } else Spacer(Modifier.width(1.dp))
                if (etape.ordinal < EtapeNotation.entries.lastIndex) {
                    Button(
                            onClick = { gradingViewModel.choisirEtape(etape.ordinal + 1) },
                            colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = VetNutriColors.OnPrimary)
                    ) { Text(translate("notation.suivant", translate("notation.etape.${EtapeNotation.entries[etape.ordinal + 1].name}"))) }
                }
            }
            if (rule == null) Text(translate("notation.chargementBareme"), color = discretNotation(), fontSize = 12.sp)
        } }
    }
}

// --- Choix de l'examen -----------------------------------------------------------------------

@Composable
private fun SelecteurExamen(
        examens: List<ExamenDisponible>,
        examId: String,
        exerciseId: String,
        nbCopies: Int,
        onChoix: (String, String) -> Unit,
        occupe: Boolean,
        onSauvegarder: () -> Unit,
        onReprendre: () -> Unit
) {
    // Saisie libre : pour importer les copies d'un examen absent de la base
    var saisieLibre by remember { mutableStateOf(examens.isEmpty()) }
    LaunchedEffect(examens.isEmpty()) { if (examens.isEmpty()) saisieLibre = true }

    Card(Modifier.fillMaxWidth(), elevation = 0.dp, border = BorderStroke(1.dp, bordureNotation)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(translate("notation.examen"), fontWeight = FontWeight.Bold, color = VetNutriColors.PrimaryVariant, modifier = Modifier.weight(1f))
                if (occupe) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = VetNutriColors.Primary)
                TextButton(onClick = onSauvegarder, enabled = !occupe && examId.isNotBlank() && exerciseId.isNotBlank()) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(translate("notation.sauvegarder"), fontSize = 12.sp)
                }
                TextButton(onClick = onReprendre, enabled = !occupe) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(translate("notation.reprendre"), fontSize = 12.sp)
                }
                if (examens.isNotEmpty()) {
                    TextButton(onClick = { saisieLibre = !saisieLibre }) {
                        Text(if (saisieLibre) translate("notation.choisirDansListe") else translate("notation.autreExamen"), fontSize = 12.sp)
                    }
                }
            }
            if (!saisieLibre) {
                val choisi = examens.firstOrNull { it.examId.equals(examId.trim(), true) && it.exerciseId.equals(exerciseId.trim(), true) }
                DropdownField(
                        label = translate("notation.examenExercice"),
                        selectedValue = choisi,
                        options = examens,
                        onValueChange = { onChoix(it.examId, it.exerciseId) },
                        valueToString = { translate("notation.examenOption", it.examId, it.exerciseId, it.copies.toString()) },
                        modifier = Modifier.fillMaxWidth()
                )
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                            value = examId,
                            onValueChange = { onChoix(it, exerciseId) },
                            label = { Text(translate("auto.view.crossconsultationgradingview.id_examen")) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                    )
                    OutlinedTextField(
                            value = exerciseId,
                            onValueChange = { onChoix(examId, it) },
                            label = { Text(translate("animal.examExerciseId")) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                    )
                }
                if (examens.isEmpty()) Text(translate("notation.aucunExamen"), fontSize = 12.sp, color = discretNotation())
            }
            if (examId.isNotBlank() && exerciseId.isNotBlank()) {
                Text(translate("notation.nbCopies", nbCopies.toString()), fontSize = 12.sp,
                        color = if (nbCopies > 0) VertNotation else AmbreNotation)
            }
        }
    }
}

@Composable
private fun BandeauNotation(texte: String, couleur: Color, icone: ImageVector, onClose: (() -> Unit)? = null) {
    Row(
            Modifier.fillMaxWidth()
                    .background(couleur.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                    .border(1.dp, couleur.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icone, contentDescription = null, tint = couleur, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(texte, color = couleur, fontSize = 13.sp, modifier = Modifier.weight(1f))
        if (onClose != null) IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = translate("notation.fermer"), tint = couleur, modifier = Modifier.size(16.dp))
        }
    }
}

/** Étapes numérotées, cliquables ; une coche marque une étape faite. */
@Composable
private fun BarreEtapes(courante: EtapeNotation, faite: (EtapeNotation) -> Boolean, onChoix: (EtapeNotation) -> Unit) {
    FlowRow(mainAxisSpacing = 6.dp, crossAxisSpacing = 6.dp) {
        EtapeNotation.entries.forEach { e ->
            val active = e == courante
            val ok = faite(e)
            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(RoundedCornerShape(18.dp))
                            .background(if (active) VetNutriColors.Primary else MaterialTheme.colors.surface)
                            .border(1.dp, if (active) VetNutriColors.Primary else bordureNotation, RoundedCornerShape(18.dp))
                            .clickable { onChoix(e) }
                            .padding(start = 6.dp, end = 12.dp, top = 5.dp, bottom = 5.dp)
            ) {
                Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(22.dp).clip(CircleShape)
                                .background(if (active) Color.White else if (ok) VertNotation else VetNutriColors.Primary.copy(alpha = 0.12f))
                ) {
                    if (ok && !active) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    else Text((e.ordinal + 1).toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VetNutriColors.PrimaryVariant)
                }
                Spacer(Modifier.width(8.dp))
                Text(translate("notation.etape.${e.name}"), fontSize = 13.sp, color = if (active) Color.White else MaterialTheme.colors.onSurface)
            }
        }
    }
}

@Composable
private fun CarteNotation(titre: String, aide: String? = null, action: @Composable RowScope.() -> Unit = {}, contenu: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), elevation = 0.dp, border = BorderStroke(1.dp, bordureNotation)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(titre, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = VetNutriColors.PrimaryVariant, modifier = Modifier.weight(1f))
                action()
            }
            if (aide != null) Text(aide, fontSize = 12.sp, color = discretNotation())
            contenu()
        }
    }
}

// --- 1. Copies --------------------------------------------------------------------------------

@Composable
private fun EtapeCopies(
        analysisViewModel: CrossConsultationAnalysisViewModel,
        gradingViewModel: ExamGradingViewModel,
        examId: String,
        exerciseId: String,
        copies: List<CrossConsultationAnalysisViewModel.ConsultationItem>,
        nbCopies: Int,
        onOpenCopie: (String, String?) -> Unit
) {
    val isImportingBins by gradingViewModel.isImportingBins.collectAsState()
    val importProgress by gradingViewModel.importProgress.collectAsState()
    val importLogs by gradingViewModel.importLogs.collectAsState()
    val lastImportSession by gradingViewModel.lastImportSession.collectAsState()
    val importSessionHistory by gradingViewModel.importSessionHistory.collectAsState()
    var forceRefreshBins by remember { mutableStateOf(false) }
    var offlineOnly by remember { mutableStateOf(false) }
    var journalOuvert by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    CarteNotation(translate("auto.view.crossconsultationgradingview.import_copies_jsonbin_csv_moodle"), aide = translate("notation.importAide")) {
        FlowRow(mainAxisSpacing = 16.dp, crossAxisSpacing = 4.dp) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { forceRefreshBins = !forceRefreshBins }) {
                Checkbox(checked = forceRefreshBins, onCheckedChange = { forceRefreshBins = it }, colors = CheckboxDefaults.colors(checkedColor = VetNutriColors.Primary))
                Text(translate("auto.view.crossconsultationgradingview.forcer_refresh"), fontSize = 13.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { offlineOnly = !offlineOnly }) {
                Checkbox(checked = offlineOnly, onCheckedChange = { offlineOnly = it }, colors = CheckboxDefaults.colors(checkedColor = VetNutriColors.Primary))
                Text(translate("auto.view.crossconsultationgradingview.hors_ligne"), fontSize = 13.sp)
            }
        }
        Button(
                onClick = {
                    scope.launch {
                        val csvContent = openCsvFileForImport() ?: return@launch
                        gradingViewModel.importFromMoodleCsv(
                                csvContent = csvContent, examId = examId, exerciseId = exerciseId,
                                forceRefresh = forceRefreshBins, offlineOnly = offlineOnly,
                                onCompleted = {
                                    analysisViewModel.loadConsultations()
                                    gradingViewModel.loadGrades(examId, exerciseId)
                                }
                        )
                    }
                },
                enabled = !isImportingBins,
                colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = VetNutriColors.OnPrimary)
        ) {
            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(translate("auto.view.crossconsultationgradingview.importer_csv_de_bins"))
        }
        if (isImportingBins) LinearProgressIndicator(progress = importProgress.toFloat(), color = VetNutriColors.Primary, modifier = Modifier.fillMaxWidth())
        lastImportSession?.let { session ->
            Text(
                    translate("auto.view.crossconsultationgradingview.last_import_summary", session.successCount.toString(), session.total.toString(), session.errorCount.toString()),
                    fontSize = 13.sp,
                    color = if (session.errorCount > 0) AmbreNotation else VertNotation
            )
            session.entries.filter { !it.success }.take(10).forEach { e ->
                Text("• ${listOf(e.prenom, e.nom).filter { it.isNotBlank() }.joinToString(" ").ifBlank { e.binId }} : ${e.error}",
                        fontSize = 12.sp, color = RougeNotation)
            }
        }
        if (importLogs.isNotEmpty()) {
            TextButton(onClick = { journalOuvert = !journalOuvert }) {
                Text(translate("auto.view.crossconsultationgradingview.logs_import_arg", importLogs.size.toString()), fontSize = 12.sp)
                Icon(if (journalOuvert) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            if (journalOuvert) Column(Modifier.fillMaxWidth().heightIn(max = 160.dp).verticalScroll(rememberScrollState())) {
                importLogs.forEach { Text(it, fontSize = 11.sp, color = discretNotation()) }
            }
        }
        if (importSessionHistory.isNotEmpty()) {
            Text(translate("auto.view.crossconsultationgradingview.imports_precedents"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            importSessionHistory.take(6).forEach { session ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${formatBackupDate(session.importedAtEpochMs)} • ${session.sourceFileName} • ${session.successCount}/${session.total}",
                            fontSize = 12.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { gradingViewModel.selectImportSession(session.sessionId) }) {
                        Text(translate("auto.view.crossconsultationgradingview.charger"), fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Copies présentes dans la base pour cet exercice
    CarteNotation(translate("notation.copiesTitre", nbCopies.toString()), aide = translate("notation.copiesAide")) {
        if (copies.isEmpty()) {
            Text(translate("notation.aucuneCopie"), fontSize = 13.sp, color = AmbreNotation)
        } else {
            val parAnimal = copies.groupBy { it.animalId }.values.mapNotNull { c -> c.maxByOrNull { it.rawDate ?: kotlinx.datetime.LocalDate(1900, 1, 1) } }
                    .sortedBy { it.animalName.lowercase() }
            val doublons = parAnimal.mapNotNull { it.studentIdentifier?.trim()?.takeIf { s -> s.isNotEmpty() } }.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
            val sansNumero = parAnimal.count { it.studentIdentifier.isNullOrBlank() }
            if (sansNumero > 0) BandeauNotation(translate("notation.sansNumero", sansNumero.toString()), AmbreNotation, Icons.Default.Warning)
            if (doublons.isNotEmpty()) BandeauNotation(translate("notation.doublons", doublons.joinToString()), AmbreNotation, Icons.Default.Warning)
            parAnimal.forEach { c ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(c.animalName, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(c.studentIdentifier?.ifBlank { null } ?: "—", fontSize = 12.sp, color = discretNotation(), modifier = Modifier.width(110.dp))
                    Text(translate("crossConsultation.nbRations", c.rationCount.toString()), fontSize = 12.sp, color = discretNotation(), modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                    IconButton(onClick = { onOpenCopie(c.animalId, c.consultationId) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Visibility, contentDescription = translate("notation.voirCopie"), tint = VetNutriColors.Primary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// --- 2. Analyse -------------------------------------------------------------------------------

@Composable
private fun EtapeAnalyse(
        gradingViewModel: ExamGradingViewModel,
        copies: List<CrossConsultationAnalysisViewModel.ConsultationItem>,
        nbCopies: Int
) {
    val metricDistributions by gradingViewModel.metricDistributions.collectAsState()
    val lowInputs = remember { mutableStateMapOf<String, String>() }
    val highInputs = remember { mutableStateMapOf<String, String>() }
    val pointsInputs = remember { mutableStateMapOf<String, String>() }

    CarteNotation(
            translate("auto.view.crossconsultationgradingview.analyse_type_rmd_distribution"),
            aide = translate("notation.analyseAide", nbCopies.toString()),
            action = {
                TextButton(
                        onClick = {
                            metricDistributions.forEach { d ->
                                lowInputs[d.metricId.name] = formatMetric(d.p10)
                                highInputs[d.metricId.name] = formatMetric(d.p90)
                            }
                        },
                        enabled = metricDistributions.isNotEmpty()
                ) { Text(translate("auto.view.crossconsultationgradingview.appliquer_seuil_robuste"), fontSize = 12.sp) }
            }
    ) {
        if (copies.isEmpty()) Text(translate("notation.aucuneCopie"), fontSize = 13.sp, color = AmbreNotation)
        metricDistributions.forEach { distribution ->
            val key = distribution.metricId.name
            if (!lowInputs.containsKey(key)) lowInputs[key] = formatMetric(distribution.suggestedLow)
            if (!highInputs.containsKey(key)) highInputs[key] = formatMetric(distribution.suggestedHigh)
            if (!pointsInputs.containsKey(key)) pointsInputs[key] = "1"
            Divider()
            Text(distribution.label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            // Valeur identique sur toutes les copies : rien à départager, pas de graphique
            if (distribution.max - distribution.min <= 1e-9 * (1.0 + kotlin.math.abs(distribution.max))) {
                Text(translate("notation.valeurUnique", formatMetric(distribution.min), distribution.count.toString()), fontSize = 12.sp, color = discretNotation())
                return@forEach
            }
            Text(
                    translate("notation.resumeDistribution", distribution.count.toString(), formatMetric(distribution.min), formatMetric(distribution.p10),
                            formatMetric(distribution.median), formatMetric(distribution.p90), formatMetric(distribution.max)),
                    fontSize = 12.sp, color = discretNotation()
            )
            ReflectiveDistributionInsights(distribution)
            HistogramBars(distribution, parseMetricInput(lowInputs[key]), parseMetricInput(highInputs[key]))
            val dans = distribution.values.count { v ->
                val lo = parseMetricInput(lowInputs[key]); val hi = parseMetricInput(highInputs[key])
                (lo == null || v >= lo) && (hi == null || v <= hi)
            }
            FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 6.dp) {
                ChampSeuil(translate("notation.min"), lowInputs[key].orEmpty()) { lowInputs[key] = it }
                ChampSeuil(translate("notation.max"), highInputs[key].orEmpty()) { highInputs[key] = it }
                ChampSeuil(translate("auto.view.crossconsultationgradingview.pts"), pointsInputs[key].orEmpty(), largeur = 80) { pointsInputs[key] = it }
                Button(
                        onClick = {
                            gradingViewModel.applyAnalysisMetricAsCriterion(
                                    metricId = distribution.metricId,
                                    min = parseMetricInput(lowInputs[key]),
                                    max = parseMetricInput(highInputs[key]),
                                    points = parseMetricInput(pointsInputs[key]) ?: 1.0
                            )
                        },
                        modifier = Modifier.padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = VetNutriColors.OnPrimary)
                ) { Text(translate("auto.view.crossconsultationgradingview.appliquer_critere"), fontSize = 12.sp) }
            }
            // Effet immédiat du seuil : combien de copies le valideraient
            Text(translate("notation.copiesDansSeuils", dans.toString(), distribution.count.toString()), fontSize = 12.sp,
                    color = if (dans == 0 || dans == distribution.count) AmbreNotation else VertNotation)
        }
    }

    CritereNutrimentLibre(gradingViewModel, copies)
}

@Composable
private fun ChampSeuil(label: String, valeur: String, largeur: Int = 110, onChange: (String) -> Unit) {
    OutlinedTextField(
            value = valeur,
            onValueChange = onChange,
            label = { Text(label, fontSize = 11.sp) },
            singleLine = true,
            isError = valeur.isNotBlank() && parseMetricInput(valeur) == null,
            modifier = Modifier.width(largeur.dp)
    )
}

/** Labels de nutriments présents dans les copies, avec leur nom traduit. */
private fun nutrimentsDesCopies(copies: List<CrossConsultationAnalysisViewModel.ConsultationItem>): List<Pair<String, String>> =
        copies.flatMap { it.rations }.flatMap { it.nutrientValues.keys }.toSet()
                .map { label -> label to (NutrientResolver.AllNutrientResolver(label)?.let { n -> "${nomTraduitNutriment(n)} (${n.unite})" } ?: label) }
                .sortedBy { it.second.lowercase() }

@Composable
private fun CritereNutrimentLibre(gradingViewModel: ExamGradingViewModel, copies: List<CrossConsultationAnalysisViewModel.ConsultationItem>) {
    val nutriments = remember(copies) { nutrimentsDesCopies(copies) }
    if (nutriments.isEmpty()) return
    var choisi by remember { mutableStateOf<Pair<String, String>?>(null) }
    var bas by remember { mutableStateOf("") }
    var haut by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("1") }
    CarteNotation(translate("auto.view.crossconsultationgradingview.critere_numerique_sur_n_importe_quel_nutriment"), aide = translate("notation.nutrimentAide")) {
        DropdownField(
                label = translate("nutrientComponents.columnNutrient"),
                selectedValue = choisi,
                options = nutriments,
                onValueChange = { choisi = it },
                valueToString = { it.second },
                modifier = Modifier.fillMaxWidth()
        )
        FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 6.dp) {
            ChampSeuil(translate("notation.min"), bas) { bas = it }
            ChampSeuil(translate("notation.max"), haut) { haut = it }
            ChampSeuil(translate("auto.view.crossconsultationgradingview.pts"), points, largeur = 80) { points = it }
            Button(
                    onClick = {
                        val n = choisi ?: return@Button
                        gradingViewModel.applyNutrientCriterion(n.first, parseMetricInput(bas), parseMetricInput(haut), parseMetricInput(points) ?: 1.0, RationScope.FIRST_PROPOSED)
                    },
                    enabled = choisi != null && (parseMetricInput(bas) != null || parseMetricInput(haut) != null),
                    modifier = Modifier.padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = VetNutriColors.OnPrimary)
            ) { Text(translate("auto.view.crossconsultationgradingview.ajouter_critere_nutriment"), fontSize = 12.sp) }
        }
    }
}

// --- 3. Barème --------------------------------------------------------------------------------

@Composable
private fun EtapeBareme(gradingViewModel: ExamGradingViewModel, copies: List<CrossConsultationAnalysisViewModel.ConsultationItem>) {
    val rule by gradingViewModel.rule.collectAsState()
    val pointsDisponibles by gradingViewModel.pointsDisponibles.collectAsState()
    val ruleSnapshotHistory by gradingViewModel.ruleSnapshotHistory.collectAsState()
    val regle = rule ?: return
    val nutriments = remember(copies) { nutrimentsDesCopies(copies) }

    BilanBareme(regle, pointsDisponibles)

    RuleEditor(regle, nutriments) { gradingViewModel.updateRule(it) }
    CompositionCopies(gradingViewModel, regle, copies)

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
                onClick = { gradingViewModel.saveRule() },
                colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = VetNutriColors.OnPrimary)
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(translate("auto.view.crossconsultationgradingview.sauver_regles"))
        }
    }
    if (ruleSnapshotHistory.isNotEmpty()) {
        CarteNotation(translate("auto.view.crossconsultationgradingview.grilles_precedentes"), aide = translate("notation.grillesAide")) {
            ruleSnapshotHistory.take(6).forEach { snapshot ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${formatBackupDate(snapshot.savedAtEpochMs)} • ${snapshot.rule.label.ifBlank { snapshot.rule.exerciseId }} • ${pts(pointsBareme(snapshot.rule))} pts",
                            fontSize = 12.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { gradingViewModel.selectRuleSnapshot(snapshot.snapshotId) }) {
                        Text(translate("auto.view.crossconsultationgradingview.charger"), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/** Cohérence du total des points avec la note maximale (rappelé à l'étape Notes). */
@Composable
private fun BilanBareme(regle: ExamGradingRuleSet, pointsDisponibles: Double) {
    val ecart = pointsDisponibles - regle.autoScoreMax
    when {
        pointsDisponibles <= 0.0 -> BandeauNotation(translate("notation.baremeVide"), RougeNotation, Icons.Default.Warning)
        regle.notationProportionnelle -> BandeauNotation(translate("notation.baremeProportionnel", pts(pointsDisponibles), pts(regle.autoScoreMax)), VertNotation, Icons.Default.CheckCircle)
        kotlin.math.abs(ecart) < 1e-9 -> BandeauNotation(translate("notation.baremeOk", pts(pointsDisponibles), pts(regle.autoScoreMax)), VertNotation, Icons.Default.CheckCircle)
        ecart > 0 -> BandeauNotation(translate("notation.baremeTrop", pts(pointsDisponibles), pts(regle.autoScoreMax)), AmbreNotation, Icons.Default.Warning)
        else -> BandeauNotation(translate("notation.baremeManque", pts(pointsDisponibles), pts(regle.autoScoreMax)), AmbreNotation, Icons.Default.Warning)
    }
}

/** Ingrédients présents dans les copies : un clic les exige ou les interdit dans le barème. */
@Composable
private fun CompositionCopies(gradingViewModel: ExamGradingViewModel, regle: ExamGradingRuleSet, copies: List<CrossConsultationAnalysisViewModel.ConsultationItem>) {
    val presence = remember(copies) {
        val map = linkedMapOf<String, Int>()
        copies.groupBy { it.animalId }.values.mapNotNull { it.lastOrNull() }.forEach { consultation ->
            val ration = consultation.rations.firstOrNull { !it.actual } ?: consultation.rations.firstOrNull()
            ration?.ingredients?.map { it.name.trim() }?.filter { it.isNotBlank() }?.toSet()?.forEach { map[it] = (map[it] ?: 0) + 1 }
        }
        map.entries.sortedByDescending { it.value }.map { it.key to it.value }
    }
    var tout by remember { mutableStateOf(false) }
    CarteNotation(
            translate("auto.view.crossconsultationgradingview.analyse_composition_des_rations"),
            aide = translate("auto.view.crossconsultationgradingview.presence_dans_les_rations_proposees_ou_premiere")
    ) {
        if (presence.isEmpty()) {
            Text(translate("auto.view.crossconsultationgradingview.aucun_ingredient_detecte_pour_ce_lot"), fontSize = 13.sp, color = discretNotation())
            return@CarteNotation
        }
        val total = copies.map { it.animalId }.distinct().size.coerceAtLeast(1)
        fun memeNom(a: String, b: String) = fr.vetbrain.vetnutri_mp.Utils.Recherche.normaliser(a) == fr.vetbrain.vetnutri_mp.Utils.Recherche.normaliser(b)
        (if (tout) presence else presence.take(15)).forEach { (ingredient, nombre) ->
            val exige = regle.ingredientRule.required.any { memeNom(it, ingredient) }
            val interdit = regle.ingredientRule.forbidden.any { memeNom(it, ingredient) }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(ingredient, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Box(Modifier.fillMaxWidth(0.9f).height(4.dp).background(VetNutriColors.Primary.copy(alpha = 0.1f), RoundedCornerShape(2.dp))) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(nombre.toFloat() / total).background(VetNutriColors.Primary, RoundedCornerShape(2.dp)))
                    }
                }
                Text(translate("notation.copiesSur", nombre.toString(), total.toString()), fontSize = 11.sp, color = discretNotation(), modifier = Modifier.width(76.dp), textAlign = TextAlign.End)
                Spacer(Modifier.width(6.dp))
                PuceFiltre(translate("notation.exiger"), selectionnee = exige, onClick = {
                    val r = gradingViewModel.rule.value.orUserError(translate("notation.aucunBareme")) ?: return@PuceFiltre
                    val req = if (exige) r.ingredientRule.required.filterNot { memeNom(it, ingredient) } else r.ingredientRule.required + ingredient
                    gradingViewModel.updateRule(r.copy(ingredientRule = r.ingredientRule.copy(required = req, forbidden = r.ingredientRule.forbidden.filterNot { memeNom(it, ingredient) })))
                })
                Spacer(Modifier.width(4.dp))
                PuceFiltre(translate("auto.view.crossconsultationgradingview.interdire"), selectionnee = interdit, onClick = {
                    val r = gradingViewModel.rule.value.orUserError(translate("notation.aucunBareme")) ?: return@PuceFiltre
                    val interdits = if (interdit) r.ingredientRule.forbidden.filterNot { memeNom(it, ingredient) } else r.ingredientRule.forbidden + ingredient
                    gradingViewModel.updateRule(r.copy(ingredientRule = r.ingredientRule.copy(forbidden = interdits, required = r.ingredientRule.required.filterNot { memeNom(it, ingredient) })))
                })
            }
        }
        if (presence.size > 15) TextButton(onClick = { tout = !tout }) {
            Text(if (tout) translate("notation.reduire") else translate("notation.toutAfficher", presence.size.toString()), fontSize = 12.sp)
        }
    }
}

/** En-tête d'une section du barème : interrupteur actif / inactif, titre et points. */
@Composable
private fun EnteteSection(titre: String, actif: Boolean, onActif: (Boolean) -> Unit, points: String, configure: Boolean, onPoints: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(checked = actif, onCheckedChange = onActif, colors = SwitchDefaults.colors(checkedThumbColor = VetNutriColors.Primary))
        Column(Modifier.weight(1f)) {
            Text(titre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (actif) MaterialTheme.colors.onSurface else discretNotation())
            // Actif sans condition : la section ne rapporte aucun point, il faut le dire
            if (actif && !configure) Text(translate("notation.sansCondition"), fontSize = 11.sp, color = AmbreNotation)
            if (!actif) Text(translate("notation.inactif"), fontSize = 11.sp, color = discretNotation())
        }
        BufferedNumberTextField(value = points, onValueChange = onPoints, label = { Text(translate("auto.view.crossconsultationgradingview.pts")) }, modifier = Modifier.width(80.dp))
    }
}

@Composable
private fun RuleEditor(rule: ExamGradingRuleSet, nutriments: List<Pair<String, String>>, onRuleChange: (ExamGradingRuleSet) -> Unit) {
    val nomNutriment: (String) -> String = { label -> nutriments.firstOrNull { it.first == label }?.second
            ?: NutrientResolver.AllNutrientResolver(label)?.let { nomTraduitNutriment(it) } ?: label }

    CarteNotation(translate("auto.view.crossconsultationgradingview.regles")) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                    value = rule.label,
                    onValueChange = { onRuleChange(rule.copy(label = it)) },
                    label = { Text(translate("auto.view.crossconsultationgradingview.libelle_exercice")) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
            )
            BufferedNumberTextField(
                    value = pts(rule.autoScoreMax),
                    onValueChange = { onRuleChange(rule.copy(autoScoreMax = it.replace(',', '.').toDoubleOrNull() ?: 20.0)) },
                    label = { Text(translate("auto.view.crossconsultationgradingview.note_max_auto")) },
                    modifier = Modifier.width(140.dp)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onRuleChange(rule.copy(notationProportionnelle = !rule.notationProportionnelle)) }) {
            Column(Modifier.weight(1f)) {
                Text(translate("notation.proportionnelle"), fontSize = 13.sp)
                Text(translate("notation.proportionnelleAide", pts(rule.autoScoreMax)), fontSize = 11.sp, color = discretNotation())
            }
            Switch(checked = rule.notationProportionnelle, onCheckedChange = { onRuleChange(rule.copy(notationProportionnelle = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = VetNutriColors.Primary))
        }

        Divider()
        EnteteSection(translate("food_edit.field.ingredients"), rule.ingredientRule.enabled, { onRuleChange(rule.copy(ingredientRule = rule.ingredientRule.copy(enabled = it))) },
                rule.ingredientRule.points.toString(), rule.ingredientRule.required.isNotEmpty() || rule.ingredientRule.forbidden.isNotEmpty()) { onRuleChange(rule.copy(ingredientRule = rule.ingredientRule.copy(points = it.toIntOrNull() ?: 0))) }
        if (rule.ingredientRule.enabled) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChampListe(translate("auto.view.crossconsultationgradingview.presents_separes_par"), rule.ingredientRule.required, Modifier.weight(1f)) {
                onRuleChange(rule.copy(ingredientRule = rule.ingredientRule.copy(required = it)))
            }
            ChampListe(translate("auto.view.crossconsultationgradingview.absents_separes_par"), rule.ingredientRule.forbidden, Modifier.weight(1f)) {
                onRuleChange(rule.copy(ingredientRule = rule.ingredientRule.copy(forbidden = it)))
            }
        }

        Divider()
        EnteteSection(translate("auto.view.crossconsultationgradingview.ration_quantite_totale") + " (g)", rule.rationQuantityRule.enabled,
                { onRuleChange(rule.copy(rationQuantityRule = rule.rationQuantityRule.copy(enabled = it))) },
                rule.rationQuantityRule.points.toString(), rule.rationQuantityRule.min != null || rule.rationQuantityRule.max != null) { onRuleChange(rule.copy(rationQuantityRule = rule.rationQuantityRule.copy(points = it.toIntOrNull() ?: 0))) }
        if (rule.rationQuantityRule.enabled) MinMaxEditor(rule.rationQuantityRule) { onRuleChange(rule.copy(rationQuantityRule = it)) }

        Divider()
        EnteteSection(translate("chart.energyDensityLabel") + " (kcal/g)", rule.energyDensityRule.enabled,
                { onRuleChange(rule.copy(energyDensityRule = rule.energyDensityRule.copy(enabled = it))) },
                rule.energyDensityRule.points.toString(), rule.energyDensityRule.min != null || rule.energyDensityRule.max != null) { onRuleChange(rule.copy(energyDensityRule = rule.energyDensityRule.copy(points = it.toIntOrNull() ?: 0))) }
        if (rule.energyDensityRule.enabled) MinMaxEditor(rule.energyDensityRule) { onRuleChange(rule.copy(energyDensityRule = it)) }

        Divider()
        Text(translate("new_reference.tab.nutrients"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(translate("notation.nutrimentsAide"), fontSize = 12.sp, color = discretNotation())
        rule.nutrientRules.forEachIndexed { index, item ->
            fun maj(n: NutrientRule) = onRuleChange(rule.copy(nutrientRules = rule.nutrientRules.toMutableList().also { it[index] = n }))
            FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 4.dp) {
                Switch(checked = item.enabled, onCheckedChange = { maj(item.copy(enabled = it)) }, colors = SwitchDefaults.colors(checkedThumbColor = VetNutriColors.Primary))
                DropdownField(
                        label = translate("nutrientComponents.columnNutrient"),
                        selectedValue = item.nutrientLabel.takeIf { it.isNotBlank() },
                        options = (nutriments.map { it.first } + listOfNotNull(item.nutrientLabel.takeIf { it.isNotBlank() })).distinct(),
                        onValueChange = { maj(item.copy(nutrientLabel = it)) },
                        valueToString = nomNutriment,
                        height = 56.dp,
                        modifier = Modifier.width(240.dp)
                )
                BufferedNumberTextField(value = item.min?.let { pts(it) } ?: "", onValueChange = { maj(item.copy(min = parseMetricInput(it))) },
                        label = { Text(translate("notation.min")) }, modifier = Modifier.width(100.dp))
                BufferedNumberTextField(value = item.max?.let { pts(it) } ?: "", onValueChange = { maj(item.copy(max = parseMetricInput(it))) },
                        label = { Text(translate("notation.max")) }, modifier = Modifier.width(100.dp))
                BufferedNumberTextField(value = item.points.toString(), onValueChange = { maj(item.copy(points = it.toIntOrNull() ?: 0)) },
                        label = { Text(translate("auto.view.crossconsultationgradingview.pts")) }, modifier = Modifier.width(80.dp))
                IconButton(onClick = { onRuleChange(rule.copy(nutrientRules = rule.nutrientRules.toMutableList().also { it.removeAt(index) })) }) {
                    Icon(Icons.Default.Close, contentDescription = translate("notation.supprimer"), tint = RougeNotation)
                }
            }
        }
        TextButton(onClick = { onRuleChange(rule.copy(nutrientRules = rule.nutrientRules + NutrientRule(nutrientLabel = ""))) }) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(" " + translate("auto.view.crossconsultationgradingview.ajouter_nutriment"), fontSize = 12.sp)
        }

        Divider()
        EnteteSection(translate("auto.view.crossconsultationgradingview.reference_nutritionnelle"), rule.referenceRule.enabled,
                { onRuleChange(rule.copy(referenceRule = rule.referenceRule.copy(enabled = it))) },
                rule.referenceRule.points.toString(), rule.referenceRule.allowedReferenceIds.isNotEmpty()) { onRuleChange(rule.copy(referenceRule = rule.referenceRule.copy(points = it.toIntOrNull() ?: 0))) }
        if (rule.referenceRule.enabled) ChampListe(translate("auto.view.crossconsultationgradingview.id_references_separes_par"), rule.referenceRule.allowedReferenceIds, Modifier.fillMaxWidth()) {
            onRuleChange(rule.copy(referenceRule = rule.referenceRule.copy(allowedReferenceIds = it)))
        }

        Divider()
        EnteteSection(translate("auto.view.crossconsultationgradingview.conseils_ordonnance"), rule.adviceRule.enabled,
                { onRuleChange(rule.copy(adviceRule = rule.adviceRule.copy(enabled = it))) },
                rule.adviceRule.points.toString(), rule.adviceRule.requiredWords.isNotEmpty() || rule.adviceRule.forbiddenWords.isNotEmpty()) { onRuleChange(rule.copy(adviceRule = rule.adviceRule.copy(points = it.toIntOrNull() ?: 0))) }
        if (rule.adviceRule.enabled) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChampListe(translate("auto.view.crossconsultationgradingview.mots_presents"), rule.adviceRule.requiredWords, Modifier.weight(1f)) {
                onRuleChange(rule.copy(adviceRule = rule.adviceRule.copy(requiredWords = it)))
            }
            ChampListe(translate("auto.view.crossconsultationgradingview.mots_absents"), rule.adviceRule.forbiddenWords, Modifier.weight(1f)) {
                onRuleChange(rule.copy(adviceRule = rule.adviceRule.copy(forbiddenWords = it)))
            }
        }

        Divider()
        Text(translate("auto.view.crossconsultationgradingview.criteres_personnalises"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
        rule.customCriteria.forEachIndexed { index, criterion ->
            CritereEditeur(criterion, nutriments, nomNutriment,
                    onChange = { c -> onRuleChange(rule.copy(customCriteria = rule.customCriteria.toMutableList().also { it[index] = c })) },
                    onSupprimer = { onRuleChange(rule.copy(customCriteria = rule.customCriteria.toMutableList().also { it.removeAt(index) })) })
        }
        TextButton(onClick = {
            val n = rule.customCriteria.size + 1
            onRuleChange(rule.copy(customCriteria = rule.customCriteria + FlexibleCriterionRule(
                    id = "c$n", label = translate("auto.view.crossconsultationgradingview.criterion_label", n.toString()),
                    metric = FlexibleMetricKind.TOTAL_RATION_QUANTITY, rationScope = RationScope.ALL, points = 1.0)))
        }) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(" " + translate("auto.view.crossconsultationgradingview.ajouter_un_critere"), fontSize = 12.sp)
        }
    }
}

private val metriquesTexte = setOf(FlexibleMetricKind.INGREDIENT_KEYWORDS, FlexibleMetricKind.ADVICE_KEYWORDS, FlexibleMetricKind.REFERENCE_ID)

@Composable
private fun CritereEditeur(
        criterion: FlexibleCriterionRule,
        nutriments: List<Pair<String, String>>,
        nomNutriment: (String) -> String,
        onChange: (FlexibleCriterionRule) -> Unit,
        onSupprimer: () -> Unit
) {
    Card(Modifier.fillMaxWidth(), elevation = 0.dp, border = BorderStroke(1.dp, bordureNotation)) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = criterion.enabled, onCheckedChange = { onChange(criterion.copy(enabled = it)) }, colors = SwitchDefaults.colors(checkedThumbColor = VetNutriColors.Primary))
                OutlinedTextField(
                        value = criterion.label,
                        onValueChange = { onChange(criterion.copy(label = it)) },
                        label = { Text(translate("auto.view.crossconsultationgradingview.nom_du_critere")) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                BufferedNumberTextField(value = pts(criterion.points), onValueChange = { onChange(criterion.copy(points = parseMetricInput(it) ?: 0.0)) },
                        label = { Text(translate("auto.view.crossconsultationgradingview.pts")) }, modifier = Modifier.width(80.dp))
                IconButton(onClick = onSupprimer) { Icon(Icons.Default.Close, contentDescription = translate("notation.supprimer"), tint = RougeNotation) }
            }
            FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 6.dp) {
                DropdownField(
                        label = translate("notation.mesure"),
                        selectedValue = criterion.metric,
                        options = FlexibleMetricKind.entries,
                        onValueChange = { onChange(criterion.copy(metric = it)) },
                        valueToString = { translate("notation.metrique.${it.name}") },
                        modifier = Modifier.width(250.dp)
                )
                DropdownField(
                        label = translate("notation.rations"),
                        selectedValue = criterion.rationScope,
                        options = RationScope.entries,
                        onValueChange = { onChange(criterion.copy(rationScope = it)) },
                        valueToString = { translate("notation.portee.${it.name}") },
                        modifier = Modifier.width(220.dp)
                )
                if (criterion.metric == FlexibleMetricKind.NUTRIENT_TOTAL) {
                    DropdownField(
                            label = translate("nutrientComponents.columnNutrient"),
                            selectedValue = criterion.nutrientLabel.takeIf { it.isNotBlank() },
                            options = (nutriments.map { it.first } + listOfNotNull(criterion.nutrientLabel.takeIf { it.isNotBlank() })).distinct(),
                            onValueChange = { onChange(criterion.copy(nutrientLabel = it)) },
                            valueToString = nomNutriment,
                            modifier = Modifier.width(240.dp)
                    )
                }
            }
            if (criterion.metric in metriquesTexte) {
                ChampListe(translate("auto.view.crossconsultationgradingview.mots_ids_requis_csv"), criterion.includes, Modifier.fillMaxWidth()) { onChange(criterion.copy(includes = it)) }
                ChampListe(translate("auto.view.crossconsultationgradingview.mots_ids_interdits_csv"), criterion.excludes, Modifier.fillMaxWidth()) { onChange(criterion.copy(excludes = it)) }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onChange(criterion.copy(requireAllIncludes = !criterion.requireAllIncludes)) }) {
                    Checkbox(checked = criterion.requireAllIncludes, onCheckedChange = { onChange(criterion.copy(requireAllIncludes = it)) },
                            colors = CheckboxDefaults.colors(checkedColor = VetNutriColors.Primary))
                    Text(translate("auto.view.crossconsultationgradingview.tous_les_mots_requis"), fontSize = 13.sp)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BufferedNumberTextField(value = criterion.min?.let { pts(it) } ?: "", onValueChange = { onChange(criterion.copy(min = parseMetricInput(it))) },
                            label = { Text(translate("notation.min")) }, modifier = Modifier.width(110.dp))
                    BufferedNumberTextField(value = criterion.max?.let { pts(it) } ?: "", onValueChange = { onChange(criterion.copy(max = parseMetricInput(it))) },
                            label = { Text(translate("notation.max")) }, modifier = Modifier.width(110.dp))
                }
            }
        }
    }
}

/** Liste de mots saisie séparée par des virgules ; le texte en cours est conservé pendant la saisie. */
@Composable
private fun ChampListe(label: String, valeurs: List<String>, modifier: Modifier, onChange: (List<String>) -> Unit) {
    var texte by remember(valeurs) { mutableStateOf(valeurs.joinToString(", ")) }
    OutlinedTextField(
            value = texte,
            onValueChange = { texte = it; onChange(splitCsv(it)) },
            label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            modifier = modifier
    )
}

@Composable
private fun MinMaxEditor(rule: MinMaxPointsRule, onChange: (MinMaxPointsRule) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BufferedNumberTextField(value = rule.min?.let { pts(it) } ?: "", onValueChange = { onChange(rule.copy(min = parseMetricInput(it))) },
                label = { Text(translate("notation.min")) }, modifier = Modifier.width(110.dp))
        BufferedNumberTextField(value = rule.max?.let { pts(it) } ?: "", onValueChange = { onChange(rule.copy(max = parseMetricInput(it))) },
                label = { Text(translate("notation.max")) }, modifier = Modifier.width(110.dp))
    }
}

// --- 4. Notes ---------------------------------------------------------------------------------

private enum class TriNotes { NOM, NOTE_DESC, NOTE_ASC }

@Composable
private fun EtapeNotes(
        gradingViewModel: ExamGradingViewModel,
        copies: List<CrossConsultationAnalysisViewModel.ConsultationItem>,
        examId: String,
        exerciseId: String,
        onOpenCopie: (String, String?) -> Unit
) {
    val rule by gradingViewModel.rule.collectAsState()
    val grades by gradingViewModel.grades.collectAsState()
    val stats by gradingViewModel.statistiques.collectAsState()
    val pointsDisponibles by gradingViewModel.pointsDisponibles.collectAsState()
    val scope = rememberCoroutineScope()
    val csvService = remember { ExamGradingCsvService() }
    var tri by remember { mutableStateOf(TriNotes.NOM) }
    val ouverte by gradingViewModel.copieOuverte.collectAsState()
    val noteMax = rule?.autoScoreMax ?: 20.0

    FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
        Button(
                onClick = { gradingViewModel.computeGrades(copies, examId, exerciseId) },
                enabled = copies.isNotEmpty() && pointsDisponibles > 0.0,
                colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = VetNutriColors.OnPrimary)
        ) { Text(if (grades.isEmpty()) translate("auto.view.crossconsultationgradingview.calculer_notes") else translate("notation.recalculer")) }
        OutlinedButton(onClick = { gradingViewModel.saveGrades() }, enabled = grades.isNotEmpty()) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(translate("auto.view.crossconsultationgradingview.sauver_notes"))
        }
        if (isCsvFileOperationsSupported()) OutlinedButton(
                onClick = { scope.launch { saveCsvFileForExport(csvService.exportGradesToCsv(grades), "notes_${examId}_$exerciseId.csv") } },
                enabled = grades.isNotEmpty()
        ) {
            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(translate("auto.view.crossconsultationgradingview.exporter_csv"))
        }
    }
    when {
        copies.isEmpty() -> Text(translate("notation.aucuneCopie"), fontSize = 13.sp, color = AmbreNotation)
        grades.isEmpty() && pointsDisponibles > 0.0 -> Text(translate("notation.notesAide"), fontSize = 13.sp, color = discretNotation())
    }
    rule?.let { BilanBareme(it, pointsDisponibles) }

    stats?.let { s ->
        FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
            Tuile(translate("notation.kpi.copies"), s.copies.toString())
            Tuile(translate("notation.kpi.moyenne"), "${pts(s.moyenne)} / ${pts(noteMax)}", translate("notation.kpi.ecartType", pts(s.ecartType)))
            Tuile(translate("notation.kpi.mediane"), pts(s.mediane))
            Tuile(translate("notation.kpi.etendue"), "${pts(s.min)} – ${pts(s.max)}")
            Tuile(translate("notation.kpi.moyenneAtteinte"), "${s.auDessusMoyenne} / ${s.copies}")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            CarteNotation(translate("notation.repartition"), modifier = Modifier.weight(1f)) { HistogrammeNotes(s.histogramme) }
        }
        if (s.reussites.isNotEmpty()) {
            CarteNotation(translate("notation.reussiteCriteres"), aide = translate("notation.reussiteAide")) {
                s.reussites.forEach { r -> LigneReussite(r, rule) }
            }
        }
    }

    if (grades.isNotEmpty()) {
        CarteNotation(translate("auto.view.crossconsultationgradingview.notes"), action = {
            TriNotes.entries.forEach { t ->
                PuceFiltre(translate("notation.tri.${t.name}"), selectionnee = tri == t, onClick = { tri = t }, modifier = Modifier.padding(start = 4.dp))
            }
        }) {
            val triees = when (tri) {
                TriNotes.NOM -> grades.sortedBy { it.animalName.lowercase() }
                TriNotes.NOTE_DESC -> grades.sortedByDescending { it.finalScore }
                TriNotes.NOTE_ASC -> grades.sortedBy { it.finalScore }
            }
            triees.forEach { grade ->
                LigneNote(gradingViewModel, grade, rule, noteMax, ouverte == grade.studentId, onOpenCopie) {
                    gradingViewModel.basculerCopie(grade.studentId)
                }
            }
        }
    }
}

/** Carte avec un modificateur (pour la mise en ligne). */
@Composable
private fun CarteNotation(titre: String, modifier: Modifier, contenu: @Composable ColumnScope.() -> Unit) {
    Card(modifier, elevation = 0.dp, border = BorderStroke(1.dp, bordureNotation)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titre, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = VetNutriColors.PrimaryVariant)
            contenu()
        }
    }
}

@Composable
private fun Tuile(titre: String, valeur: String, detail: String? = null) {
    Column(
            Modifier.widthIn(min = 130.dp)
                    .background(MaterialTheme.colors.surface, RoundedCornerShape(8.dp))
                    .border(1.dp, bordureNotation, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(titre, fontSize = 11.sp, color = discretNotation())
        Text(valeur, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VetNutriColors.PrimaryVariant)
        if (detail != null) Text(detail, fontSize = 11.sp, color = discretNotation())
    }
}

@Composable
private fun HistogrammeNotes(classes: List<ClasseNotes>) {
    val max = (classes.maxOfOrNull { it.copies } ?: 1).coerceAtLeast(1)
    Row(Modifier.fillMaxWidth().height(140.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        classes.forEach { c ->
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
                if (c.copies > 0) Text(c.copies.toString(), fontSize = 10.sp, color = discretNotation())
                Box(
                        Modifier.fillMaxWidth()
                                .fillMaxHeight((c.copies.toFloat() / max * 0.85f).coerceAtLeast(0.01f))
                                .background(if (c.copies > 0) VetNutriColors.Primary else bordureNotation, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                )
            }
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        classes.forEach { c -> Text(pts(c.de), fontSize = 10.sp, color = discretNotation(), textAlign = TextAlign.Center, modifier = Modifier.weight(1f)) }
    }
}

private fun libelleCritere(c: CritereBareme, rule: ExamGradingRuleSet?): String = when (c.type) {
    TypeCritereNotation.NUTRIMENT -> NutrientResolver.AllNutrientResolver(c.libelle)?.let { nomTraduitNutriment(it) } ?: c.libelle
    TypeCritereNotation.PERSONNALISE -> c.libelle
    else -> translate("notation.critere.${c.type.name}")
}

@Composable
private fun LigneReussite(r: ReussiteCritere, rule: ExamGradingRuleSet?) {
    // Un critère validé par (presque) toutes les copies, ou par aucune, ne départage pas les étudiants
    val peuDiscriminant = r.copies >= 3 && (r.taux >= 0.95 || r.taux <= 0.05)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(libelleCritere(r.critere, rule), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(190.dp))
        Box(Modifier.weight(1f).height(10.dp).background(VetNutriColors.Primary.copy(alpha = 0.1f), RoundedCornerShape(3.dp))) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(r.taux.toFloat()).background(if (peuDiscriminant) AmbreNotation else VetNutriColors.Primary, RoundedCornerShape(3.dp)))
        }
        Text("${r.valides}/${r.copies} · ${pts(r.critere.points)} pts", fontSize = 11.sp, color = discretNotation(), modifier = Modifier.width(110.dp), textAlign = TextAlign.End)
    }
    if (peuDiscriminant) Text(translate(if (r.taux >= 0.95) "notation.critereTropFacile" else "notation.critereJamaisValide"), fontSize = 11.sp, color = AmbreNotation,
            modifier = Modifier.padding(start = 4.dp))
}

@Composable
private fun LigneNote(
        gradingViewModel: ExamGradingViewModel,
        grade: ExamGrade,
        rule: ExamGradingRuleSet?,
        noteMax: Double,
        ouverte: Boolean,
        onOpenCopie: (String, String?) -> Unit,
        onBasculer: () -> Unit
) {
    Column(Modifier.fillMaxWidth().border(1.dp, bordureNotation, RoundedCornerShape(6.dp))) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable(onClick = onBasculer).padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(grade.animalName, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (grade.studentId.startsWith("animal:")) translate("notation.sansNumeroCourt") else grade.studentId, fontSize = 11.sp, color = discretNotation())
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${pts(grade.finalScore)} / ${pts(noteMax)}", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        color = if (grade.finalScore >= noteMax / 2) VertNotation else RougeNotation)
                Text(if (grade.manualScore != null) translate("notation.noteManuelleAuto", pts(grade.autoScore)) else translate("notation.noteAuto"),
                        fontSize = 10.sp, color = if (grade.manualScore != null) AmbreNotation else discretNotation())
            }
            Icon(if (ouverte) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
        }
        if (!ouverte) return@Column
        Divider()
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            rule?.let { r ->
                criteresBareme(r).forEach { c ->
                    val ok = critereValide(c, grade.detail, r)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (ok) Icons.Default.Check else Icons.Default.Close, contentDescription = null, tint = if (ok) VertNotation else RougeNotation, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(libelleCritere(c, r), fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text(mesureCritere(c, grade.detail, r), fontSize = 11.sp, color = discretNotation())
                        Text(if (ok) "+${pts(c.points)}" else "0 / ${pts(c.points)}", fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(70.dp), textAlign = TextAlign.End)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BufferedNumberTextField(
                        value = grade.manualScore?.let { pts(it) } ?: "",
                        onValueChange = { gradingViewModel.updateManualScore(grade.studentId, parseMetricInput(it)) },
                        label = { Text(translate("auto.view.crossconsultationgradingview.note_manuelle")) },
                        modifier = Modifier.width(160.dp)
                )
                Text(translate("notation.noteManuelleAide"), fontSize = 11.sp, color = discretNotation(), modifier = Modifier.weight(1f))
                grade.animalId?.let { animalId ->
                    OutlinedButton(onClick = { onOpenCopie(animalId, grade.consultationId) }) {
                        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(translate("notation.voirCopie"), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

/** Valeur mesurée sur la copie, quand le critère est numérique. */
private fun mesureCritere(c: CritereBareme, d: ExamGradeDetail, r: ExamGradingRuleSet): String = when (c.type) {
    TypeCritereNotation.QUANTITE -> "${pts(d.rationQuantity)} g"
    TypeCritereNotation.DENSITE -> d.energyDensity?.let { "${pts(it)} kcal/g" } ?: "—"
    TypeCritereNotation.NUTRIMENT -> d.nutrientResults.firstOrNull { it.nutrientLabel == c.libelle }?.let { pts(it.value) } ?: "—"
    TypeCritereNotation.PERSONNALISE -> d.customCriteriaResults.firstOrNull { it.label == c.libelle }?.measuredNumber?.let { pts(it) } ?: ""
    else -> ""
}

// --- Outils ------------------------------------------------------------------------------------

private fun splitCsv(value: String): List<String> = value.split(",").map { it.trim() }.filter { it.isNotBlank() }

@Composable
@OptIn(ExperimentalKoalaPlotApi::class)
private fun HistogramBars(distribution: ExamGradingViewModel.MetricDistribution, criterionLow: Double?, criterionHigh: Double?) {
    if (distribution.histogram.isEmpty()) return
    val minXRaw = distribution.histogram.first().from.toFloat()
    val maxXRaw = distribution.histogram.last().to.toFloat()
    val minX = if (minXRaw == maxXRaw) minXRaw - 1f else minXRaw
    val maxX = if (minXRaw == maxXRaw) maxXRaw + 1f else maxXRaw
    val xRange = minX..maxX
    val maxCount = (distribution.histogram.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)
    val yRange = 0f..(maxCount.toFloat() * 1.15f).coerceAtLeast(1f)
    val barData = distribution.histogram.map { bin -> verticalBarPlotEntry(((bin.from + bin.to) / 2.0).toFloat(), 0f, bin.count.toFloat()) }

    XYGraph(
            xAxisLabels = { GraphFormattingUtils.formatAxisTick(it, xRange) },
            yAxisLabels = { GraphFormattingUtils.formatAxisTick(it, yRange) },
            xAxisModel = FloatLinearAxisModel(xRange, minimumMajorTickIncrement = ((maxX - minX) / 5f).coerceAtLeast(0.1f)),
            yAxisModel = FloatLinearAxisModel(yRange, minimumMajorTickIncrement = 1f),
            xAxisTitle = distribution.label,
            yAxisTitle = translate("notation.nombreCopies"),
            modifier = Modifier.fillMaxWidth().height(200.dp)
    ) {
        VerticalBarPlot(data = barData, bar = {
            DefaultVerticalBar(brush = SolidColor(VetNutriColors.Primary.copy(alpha = 0.70f)), modifier = Modifier.fillMaxSize())
        }, barWidth = 0.92f)
        listOfNotNull(criterionLow, criterionHigh).forEach { x ->
            LinePlot(
                    data = listOf(Point(x.toFloat(), yRange.start), Point(x.toFloat(), yRange.endInclusive)),
                    lineStyle = LineStyle(brush = SolidColor(AmbreNotation), strokeWidth = 2.dp, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f))
            )
        }
    }
}

private fun formatMetric(value: Double): String {
    if (!value.isFinite()) return ""
    return ((value * 100.0).roundToInt() / 100.0).toString()
}

private fun parseMetricInput(value: String?): Double? {
    if (value.isNullOrBlank()) return null
    return value.trim().replace(",", ".").toDoubleOrNull()
}

@Composable
private fun ReflectiveDistributionInsights(distribution: ExamGradingViewModel.MetricDistribution) {
    if (distribution.count < 3) {
        Text(translate("auto.view.crossconsultationgradingview.analyse_reflexive_effectif_trop_faible_pour_inte"), fontSize = 12.sp, color = discretNotation())
        return
    }
    val p10 = distribution.p10
    val p90 = distribution.p90
    val median = distribution.median
    val iqrApprox = (p90 - p10).coerceAtLeast(0.0)
    val relSpread = if (kotlin.math.abs(median) > 1e-9) iqrApprox / kotlin.math.abs(median) else Double.POSITIVE_INFINITY
    val lowTail = median - distribution.min
    val highTail = distribution.max - median
    val skewRatio = if (lowTail > 1e-9) highTail / lowTail else Double.POSITIVE_INFINITY
    val hasExtremeHigh = distribution.max > (p90 + 1.5 * iqrApprox)
    val hasExtremeLow = distribution.min < (p10 - 1.5 * iqrApprox)
    val spread = when {
        relSpread.isInfinite() -> "notation.dispersion.inconnue"
        relSpread < 0.25 -> "notation.dispersion.faible"
        relSpread < 0.60 -> "notation.dispersion.moderee"
        else -> "notation.dispersion.elevee"
    }
    val shape = when {
        skewRatio > 1.6 -> "notation.forme.droite"
        skewRatio < 0.62 -> "notation.forme.gauche"
        else -> "notation.forme.centree"
    }
    val outliers = when {
        hasExtremeHigh && hasExtremeLow -> "notation.extremes.deux"
        hasExtremeHigh -> "notation.extremes.hauts"
        hasExtremeLow -> "notation.extremes.bas"
        else -> "notation.extremes.aucun"
    }
    Text(translate("auto.view.crossconsultationgradingview.reflective_analysis", translate(spread), translate(shape), translate(outliers)), fontSize = 12.sp, color = discretNotation())
    Text(translate("auto.view.crossconsultationgradingview.threshold_advice", formatMetric(p10), formatMetric(p90), formatMetric(distribution.suggestedLow), formatMetric(distribution.suggestedHigh)),
            fontSize = 12.sp, color = discretNotation())
}
