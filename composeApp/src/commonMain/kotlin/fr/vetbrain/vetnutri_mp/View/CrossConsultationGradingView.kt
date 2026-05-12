package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.ExcelPlatform.openCsvFileForImport
import fr.vetbrain.vetnutri_mp.ExcelPlatform.saveCsvFileForExport
import fr.vetbrain.vetnutri_mp.Service.ExamGradingCsvService
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Color

private enum class GradingWorkflowStep(val title: String) {
    LOT("Lot"),
    ANALYSIS("Analyse"),
    COMPOSITION("Composition")
}

@Composable
fun CrossConsultationGradingView(
    analysisViewModel: CrossConsultationAnalysisViewModel,
    gradingViewModel: ExamGradingViewModel,
    onNavigateBack: () -> Unit
) {
    val allItems by analysisViewModel.allItems.collectAsState()
    val rule by gradingViewModel.rule.collectAsState()
    val grades by gradingViewModel.grades.collectAsState()
    val message by gradingViewModel.message.collectAsState()
    val isImportingBins by gradingViewModel.isImportingBins.collectAsState()
    val importProgress by gradingViewModel.importProgress.collectAsState()
    val importLogs by gradingViewModel.importLogs.collectAsState()
    val lastImportSession by gradingViewModel.lastImportSession.collectAsState()
    val importSessionHistory by gradingViewModel.importSessionHistory.collectAsState()
    val ruleSnapshotHistory by gradingViewModel.ruleSnapshotHistory.collectAsState()
    val metricDistributions by gradingViewModel.metricDistributions.collectAsState()
    var examId by remember { mutableStateOf("") }
    var exerciseId by remember { mutableStateOf("") }
    var forceRefreshBins by remember { mutableStateOf(false) }
    var offlineOnly by remember { mutableStateOf(false) }
    var selectedStep by remember { mutableStateOf(GradingWorkflowStep.LOT) }
    var wizardMode by remember { mutableStateOf(false) }
    val lowInputs = remember { mutableStateMapOf<String, String>() }
    val highInputs = remember { mutableStateMapOf<String, String>() }
    var selectedNutrientLabel by remember { mutableStateOf("") }
    var nutrientLow by remember { mutableStateOf("") }
    var nutrientHigh by remember { mutableStateOf("") }

    val filteredItems = remember(allItems, examId, exerciseId) {
        val e = examId.trim()
        val x = exerciseId.trim()
        allItems.filter {
            it.examId?.trim()?.equals(e, ignoreCase = true) == true &&
                it.examExerciseId?.trim()?.equals(x, ignoreCase = true) == true
        }
    }
    val distinctAnimalsCount = remember(filteredItems) { filteredItems.map { it.animalId }.toSet().size }
    val latestByAnimalCount = remember(filteredItems) { filteredItems.groupBy { it.animalId }.size }
    val nutrientLabels = remember(filteredItems) {
        filteredItems
            .flatMap { it.rations }
            .flatMap { it.nutrientValues.keys }
            .toSet()
            .sorted()
    }
    val ingredientPresence = remember(filteredItems) {
        val map = linkedMapOf<String, Int>()
        filteredItems.forEach { consultation ->
            val ration = consultation.rations.firstOrNull { !it.actual } ?: consultation.rations.firstOrNull()
            val names = ration?.ingredients?.map { it.name.trim().lowercase() }?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
            names.forEach { name ->
                map[name] = (map[name] ?: 0) + 1
            }
        }
        map.entries.sortedByDescending { it.value }
    }
    val processChecks = remember(
        examId,
        exerciseId,
        rule,
        filteredItems,
        lastImportSession,
        metricDistributions
    ) {
        val checks = mutableListOf<Pair<String, Boolean>>()
        val hasIds = examId.isNotBlank() && exerciseId.isNotBlank()
        checks += "ID examen/exercice renseignés" to hasIds
        checks += "Grille chargée" to (rule != null)
        checks += "Lot importé (au moins 1 copie)" to ((lastImportSession?.successCount ?: 0) > 0)
        checks += "Copies détectées dans ce lot" to filteredItems.isNotEmpty()
        checks += "Analyse statistique lancée" to metricDistributions.isNotEmpty()
        val hasCustomCriteria = rule?.customCriteria?.isNotEmpty() == true
        checks += "Critères numériques définis" to hasCustomCriteria
        checks += "Critères de composition (interdits) définis" to ((rule?.ingredientRule?.forbidden?.isNotEmpty()) == true)
        checks
    }
    val readyToGrade = remember(processChecks) {
        processChecks.firstOrNull { it.first == "ID examen/exercice renseignés" }?.second == true &&
            processChecks.firstOrNull { it.first == "Grille chargée" }?.second == true &&
            processChecks.firstOrNull { it.first == "Copies détectées dans ce lot" }?.second == true &&
            processChecks.firstOrNull { it.first == "Critères numériques définis" }?.second == true
    }
    val canGoNextStep = remember(selectedStep, processChecks) {
        when (selectedStep) {
            GradingWorkflowStep.LOT -> {
                processChecks.firstOrNull { it.first == "ID examen/exercice renseignés" }?.second == true &&
                    processChecks.firstOrNull { it.first == "Grille chargée" }?.second == true &&
                    processChecks.firstOrNull { it.first == "Copies détectées dans ce lot" }?.second == true
            }
            GradingWorkflowStep.ANALYSIS -> {
                processChecks.firstOrNull { it.first == "Analyse statistique lancée" }?.second == true &&
                    processChecks.firstOrNull { it.first == "Critères numériques définis" }?.second == true
            }
            GradingWorkflowStep.COMPOSITION -> true
        }
    }

    val csvService = remember { ExamGradingCsvService() }
    LaunchedEffect(examId.trim(), exerciseId.trim(), filteredItems.size, selectedStep) {
        if (selectedStep == GradingWorkflowStep.ANALYSIS && examId.isNotBlank() && exerciseId.isNotBlank()) {
            gradingViewModel.runRmdLikeAnalysis(filteredItems)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarSimple(
            title = "Notation examen",
            onNavigateBack = onNavigateBack
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(AppSizes.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = examId,
                    onValueChange = { examId = it },
                    label = { Text("ID examen") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = exerciseId,
                    onValueChange = { exerciseId = it },
                    label = { Text("ID exercice") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (examId.isNotBlank() && exerciseId.isNotBlank()) {
                            gradingViewModel.loadRule(examId.trim(), exerciseId.trim())
                            gradingViewModel.loadGrades(examId.trim(), exerciseId.trim())
                        }
                    }
                ) { Text("Charger règles") }
                Button(
                    onClick = {
                        if (examId.isNotBlank() && exerciseId.isNotBlank()) {
                            gradingViewModel.computeGrades(filteredItems, examId.trim(), exerciseId.trim())
                        }
                    },
                    enabled = readyToGrade
                ) { Text("Calculer notes") }
                Button(
                    onClick = { gradingViewModel.saveRule() },
                    enabled = rule != null
                ) { Text("Sauver règles") }
            }

            TabRow(selectedTabIndex = selectedStep.ordinal) {
                GradingWorkflowStep.entries.forEach { step ->
                    Tab(
                        selected = selectedStep == step,
                        onClick = { selectedStep = step },
                        text = { Text(step.title) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = wizardMode,
                        onCheckedChange = { wizardMode = it }
                    )
                    Text("Mode wizard")
                }
                if (wizardMode) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val prev = (selectedStep.ordinal - 1).coerceAtLeast(0)
                                selectedStep = GradingWorkflowStep.entries[prev]
                            },
                            enabled = selectedStep.ordinal > 0
                        ) {
                            Text("Précédent")
                        }
                        Button(
                            onClick = {
                                val next = (selectedStep.ordinal + 1).coerceAtMost(GradingWorkflowStep.entries.lastIndex)
                                selectedStep = GradingWorkflowStep.entries[next]
                            },
                            enabled = selectedStep.ordinal < GradingWorkflowStep.entries.lastIndex && canGoNextStep
                        ) {
                            Text("Suivant")
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), elevation = 1.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Analyse de cohérence du processus", style = MaterialTheme.typography.subtitle1)
                    processChecks.forEach { (label, ok) ->
                        Text(
                            text = "${if (ok) "✅" else "⚠️"} $label",
                            color = if (ok) Color(0xFF2E7D32) else MaterialTheme.colors.error,
                            style = MaterialTheme.typography.body2
                        )
                    }
                    Divider()
                    Text(
                        if (readyToGrade) "Processus cohérent: prêt pour calcul des notes"
                        else "Processus incomplet: compléter les points ⚠️ avant notation",
                        color = if (readyToGrade) Color(0xFF2E7D32) else MaterialTheme.colors.error,
                        style = MaterialTheme.typography.subtitle2
                    )
                }
            }

            if (selectedStep == GradingWorkflowStep.LOT) Card(modifier = Modifier.fillMaxWidth(), elevation = 1.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Import copies JSONBin (CSV Moodle)", style = MaterialTheme.typography.subtitle1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = forceRefreshBins, onCheckedChange = { forceRefreshBins = it })
                            Text("Forcer refresh")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = offlineOnly, onCheckedChange = { offlineOnly = it })
                            Text("Hors-ligne")
                        }
                    }
                    Button(
                        onClick = {
                            if (examId.isBlank() || exerciseId.isBlank()) return@Button
                            val csvContent = openCsvFileForImport() ?: return@Button
                            gradingViewModel.importFromMoodleCsv(
                                csvContent = csvContent,
                                examId = examId.trim(),
                                exerciseId = exerciseId.trim(),
                                forceRefresh = forceRefreshBins,
                                offlineOnly = offlineOnly,
                                onCompleted = {
                                    analysisViewModel.loadConsultations()
                                    gradingViewModel.loadGrades(examId.trim(), exerciseId.trim())
                                }
                            )
                        },
                        enabled = !isImportingBins
                    ) { Text("Importer CSV de bins") }

                    if (isImportingBins) {
                        LinearProgressIndicator(progress = importProgress.toFloat(), modifier = Modifier.fillMaxWidth())
                    }
                    lastImportSession?.let { session ->
                        Text("Dernier import: ${session.successCount}/${session.total} OK, ${session.errorCount} erreur(s)")
                    }
                    if (importLogs.isNotEmpty()) {
                        Text("Logs import (${importLogs.size})", style = MaterialTheme.typography.caption)
                        Column(modifier = Modifier.fillMaxWidth().heightIn(max = 140.dp).verticalScroll(rememberScrollState())) {
                            importLogs.forEach { line ->
                                Text(line, style = MaterialTheme.typography.caption)
                            }
                        }
                    }
                    if (importSessionHistory.isNotEmpty()) {
                        Text("Imports précédents", style = MaterialTheme.typography.caption)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            importSessionHistory.take(6).forEach { session ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${session.sourceFileName} • ${session.successCount}/${session.total}",
                                        style = MaterialTheme.typography.caption
                                    )
                                    OutlinedButton(onClick = { gradingViewModel.selectImportSession(session.sessionId) }) {
                                        Text("Charger")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (selectedStep == GradingWorkflowStep.COMPOSITION && rule != null) {
                RuleEditor(
                    rule = rule!!,
                    onRuleChange = { gradingViewModel.updateRule(it) }
                )
                if (ruleSnapshotHistory.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth(), elevation = 1.dp) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Grilles précédentes", style = MaterialTheme.typography.subtitle2)
                            ruleSnapshotHistory.take(6).forEach { snapshot ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${snapshot.rule.label.ifBlank { snapshot.rule.exerciseId }} • ${snapshot.savedAtEpochMs}",
                                        style = MaterialTheme.typography.caption
                                    )
                                    OutlinedButton(onClick = { gradingViewModel.selectRuleSnapshot(snapshot.snapshotId) }) {
                                        Text("Charger")
                                    }
                                }
                            }
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth(), elevation = 1.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Analyse composition des rations", style = MaterialTheme.typography.subtitle1)
                        Text(
                            "Présence dans les rations proposées (ou première ration si aucune proposée).",
                            style = MaterialTheme.typography.caption
                        )
                        val forbidden = rule!!.ingredientRule.forbidden.map { it.trim().lowercase() }.toSet()
                        if (ingredientPresence.isEmpty()) {
                            Text("Aucun ingrédient détecté pour ce lot.")
                        } else {
                            ingredientPresence.take(30).forEach { (ingredient, count) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "$ingredient  •  $count copie(s)",
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (forbidden.contains(ingredient)) {
                                        OutlinedButton(onClick = {
                                            val updatedForbidden = rule!!.ingredientRule.forbidden.filterNot { it.trim().lowercase() == ingredient }
                                            gradingViewModel.updateRule(
                                                rule!!.copy(ingredientRule = rule!!.ingredientRule.copy(forbidden = updatedForbidden))
                                            )
                                        }) {
                                            Text("Retirer interdit")
                                        }
                                    } else {
                                        Button(onClick = {
                                            gradingViewModel.updateRule(
                                                rule!!.copy(
                                                    ingredientRule = rule!!.ingredientRule.copy(
                                                        forbidden = rule!!.ingredientRule.forbidden + ingredient
                                                    )
                                                )
                                            )
                                        }) {
                                            Text("Interdire")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (selectedStep == GradingWorkflowStep.ANALYSIS) Card(modifier = Modifier.fillMaxWidth(), elevation = 1.dp) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Analyse type RMD (distribution)", style = MaterialTheme.typography.subtitle1)
                    Text(
                        "Base utilisée: ${filteredItems.size} consultation(s) filtrée(s), $distinctAnimalsCount animal(aux) distinct(s), $latestByAnimalCount dernière(s) consultation(s) retenue(s) pour les graphes.",
                        style = MaterialTheme.typography.body2
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                if (examId.isBlank() || exerciseId.isBlank()) return@Button
                                gradingViewModel.runRmdLikeAnalysis(filteredItems)
                            }
                        ) {
                            Text("Actualiser analyse")
                        }
                        OutlinedButton(
                            onClick = {
                                metricDistributions.forEach { distribution ->
                                    val key = distribution.metricId.name
                                    lowInputs[key] = formatMetric(distribution.p10)
                                    highInputs[key] = formatMetric(distribution.p90)
                                }
                            },
                            enabled = metricDistributions.isNotEmpty()
                        ) {
                            Text("Appliquer seuil robuste")
                        }
                    }

                    metricDistributions.forEach { distribution ->
                        val key = distribution.metricId.name
                        if (!lowInputs.containsKey(key)) lowInputs[key] = formatMetric(distribution.suggestedLow)
                        if (!highInputs.containsKey(key)) highInputs[key] = formatMetric(distribution.suggestedHigh)

                        Card(modifier = Modifier.fillMaxWidth(), elevation = 0.dp) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(distribution.label, style = MaterialTheme.typography.subtitle2)
                                Text(
                                    "n=${distribution.count} | min=${formatMetric(distribution.min)} | p10=${formatMetric(distribution.p10)} | médiane=${formatMetric(distribution.median)} | p90=${formatMetric(distribution.p90)} | max=${formatMetric(distribution.max)}",
                                    style = MaterialTheme.typography.caption
                                )
                                ReflectiveDistributionInsights(distribution)
                                HistogramBars(
                                    distribution = distribution,
                                    criterionLow = parseMetricInput(lowInputs[key]),
                                    criterionHigh = parseMetricInput(highInputs[key])
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = lowInputs[key].orEmpty(),
                                        onValueChange = { lowInputs[key] = it },
                                        label = { Text("Low") },
                                        modifier = Modifier.width(120.dp),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = highInputs[key].orEmpty(),
                                        onValueChange = { highInputs[key] = it },
                                        label = { Text("High") },
                                        modifier = Modifier.width(120.dp),
                                        singleLine = true
                                    )
                                    Button(onClick = {
                                        val low = lowInputs[key]?.toDoubleOrNull()
                                        val high = highInputs[key]?.toDoubleOrNull()
                                        gradingViewModel.applyAnalysisMetricAsCriterion(
                                            metricId = distribution.metricId,
                                            min = low,
                                            max = high,
                                            points = 0.6
                                        )
                                    }) {
                                        Text("Appliquer critère")
                                    }
                                }
                            }
                        }
                    }

                    if (nutrientLabels.isNotEmpty()) {
                        Divider()
                        Text("Critère numérique sur n'importe quel nutriment", style = MaterialTheme.typography.subtitle2)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = selectedNutrientLabel,
                                onValueChange = { selectedNutrientLabel = it },
                                label = { Text("Nutriment") },
                                modifier = Modifier.width(220.dp),
                                singleLine = true
                            )
                            OutlinedButton(onClick = {
                                if (nutrientLabels.isEmpty()) return@OutlinedButton
                                val currentIndex = nutrientLabels.indexOf(selectedNutrientLabel).takeIf { it >= 0 } ?: -1
                                selectedNutrientLabel = nutrientLabels[(currentIndex + 1) % nutrientLabels.size]
                            }) {
                                Text("Nutriment suivant")
                            }
                            OutlinedTextField(
                                value = nutrientLow,
                                onValueChange = { nutrientLow = it },
                                label = { Text("Low") },
                                modifier = Modifier.width(120.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = nutrientHigh,
                                onValueChange = { nutrientHigh = it },
                                label = { Text("High") },
                                modifier = Modifier.width(120.dp),
                                singleLine = true
                            )
                            Button(onClick = {
                                val label = selectedNutrientLabel.trim()
                                if (label.isBlank()) return@Button
                                gradingViewModel.applyNutrientCriterion(
                                    nutrientLabel = label,
                                    min = parseMetricInput(nutrientLow),
                                    max = parseMetricInput(nutrientHigh),
                                    points = 0.6,
                                    scope = RationScope.FIRST_PROPOSED
                                )
                            }) {
                                Text("Ajouter critère nutriment")
                            }
                        }
                    }
                }
            }

            if (grades.isNotEmpty()) {
                Text("Notes", style = MaterialTheme.typography.h6)
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(grades) { grade ->
                        Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("${grade.animalName} — Étudiant: ${grade.studentId}")
                                Text("Auto: ${grade.autoScore} / ${rule?.autoScoreMax ?: 20.0}")
                                val customPts = grade.detail.customCriteriaResults.sumOf { it.pointsAwarded }
                                if (customPts > 0.0 || grade.detail.customCriteriaResults.isNotEmpty()) {
                                    Text("Critères flexibles: $customPts pt")
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = grade.manualScore?.toString() ?: "",
                                        onValueChange = { value ->
                                            val manual = value.toDoubleOrNull()
                                            gradingViewModel.updateManualScore(grade.studentId, manual)
                                        },
                                        label = { Text("Note manuelle") },
                                        modifier = Modifier.width(160.dp),
                                        singleLine = true
                                    )
                                    Text("Final: ${grade.finalScore}")
                                }
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { gradingViewModel.saveGrades() }) { Text("Sauver notes") }
                    Button(
                        onClick = {
                            val csv = csvService.exportGradesToCsv(grades)
                            val success = saveCsvFileForExport(csv, "notes_exam.csv")
                            if (!success) {
                                gradingViewModel.clearMessage()
                            }
                        }
                    ) { Text("Exporter CSV") }
                }
            }

            if (!message.isNullOrBlank()) {
                Text(message!!, color = VetNutriColors.Primary)
            }
        }
    }
}

@Composable
private fun RuleEditor(
    rule: ExamGradingRuleSet,
    onRuleChange: (ExamGradingRuleSet) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Règles", style = MaterialTheme.typography.h6)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = rule.label,
                    onValueChange = { value -> onRuleChange(rule.copy(label = value)) },
                    label = { Text("Libellé exercice") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = rule.autoScoreMax.toString(),
                    onValueChange = { value ->
                        onRuleChange(rule.copy(autoScoreMax = value.toDoubleOrNull() ?: 20.0))
                    },
                    label = { Text("Note max auto") },
                    modifier = Modifier.width(160.dp)
                )
            }

            Text("Ingrédients")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = rule.ingredientRule.required.joinToString(", "),
                    onValueChange = { value ->
                        onRuleChange(rule.copy(ingredientRule = rule.ingredientRule.copy(required = splitCsv(value))))
                    },
                    label = { Text("Présents (séparés par ,)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = rule.ingredientRule.forbidden.joinToString(", "),
                    onValueChange = { value ->
                        onRuleChange(rule.copy(ingredientRule = rule.ingredientRule.copy(forbidden = splitCsv(value))))
                    },
                    label = { Text("Absents (séparés par ,)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = rule.ingredientRule.points.toString(),
                    onValueChange = { value ->
                        onRuleChange(rule.copy(ingredientRule = rule.ingredientRule.copy(points = value.toIntOrNull() ?: 0)))
                    },
                    label = { Text("Pts") },
                    modifier = Modifier.width(80.dp)
                )
            }

            Text("Ration (quantité totale)")
            MinMaxEditor(rule.rationQuantityRule) { updated ->
                onRuleChange(rule.copy(rationQuantityRule = updated))
            }

            Text("Densité énergétique")
            MinMaxEditor(rule.energyDensityRule) { updated ->
                onRuleChange(rule.copy(energyDensityRule = updated))
            }

            Text("Nutriments")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                rule.nutrientRules.forEachIndexed { index, item ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = item.nutrientLabel,
                            onValueChange = { value ->
                                val updated = rule.nutrientRules.toMutableList()
                                updated[index] = item.copy(nutrientLabel = value)
                                onRuleChange(rule.copy(nutrientRules = updated))
                            },
                            label = { Text("Nutriment") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = item.min?.toString() ?: "",
                            onValueChange = { value ->
                                val updated = rule.nutrientRules.toMutableList()
                                updated[index] = item.copy(min = value.toDoubleOrNull())
                                onRuleChange(rule.copy(nutrientRules = updated))
                            },
                            label = { Text("Min") },
                            modifier = Modifier.width(90.dp)
                        )
                        OutlinedTextField(
                            value = item.max?.toString() ?: "",
                            onValueChange = { value ->
                                val updated = rule.nutrientRules.toMutableList()
                                updated[index] = item.copy(max = value.toDoubleOrNull())
                                onRuleChange(rule.copy(nutrientRules = updated))
                            },
                            label = { Text("Max") },
                            modifier = Modifier.width(90.dp)
                        )
                        OutlinedTextField(
                            value = item.points.toString(),
                            onValueChange = { value ->
                                val updated = rule.nutrientRules.toMutableList()
                                updated[index] = item.copy(points = value.toIntOrNull() ?: 0)
                                onRuleChange(rule.copy(nutrientRules = updated))
                            },
                            label = { Text("Pts") },
                            modifier = Modifier.width(80.dp)
                        )
                        IconButton(onClick = {
                            val updated = rule.nutrientRules.toMutableList()
                            updated.removeAt(index)
                            onRuleChange(rule.copy(nutrientRules = updated))
                        }) { Text("X") }
                    }
                }
                OutlinedButton(onClick = {
                    onRuleChange(rule.copy(nutrientRules = rule.nutrientRules + NutrientRule(nutrientLabel = "")))
                }) { Text("Ajouter nutriment") }
            }

            Text("Référence nutritionnelle")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = rule.referenceRule.allowedReferenceIds.joinToString(", "),
                    onValueChange = { value ->
                        onRuleChange(rule.copy(referenceRule = rule.referenceRule.copy(allowedReferenceIds = splitCsv(value))))
                    },
                    label = { Text("ID références (séparés par ,)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = rule.referenceRule.points.toString(),
                    onValueChange = { value ->
                        onRuleChange(rule.copy(referenceRule = rule.referenceRule.copy(points = value.toIntOrNull() ?: 0)))
                    },
                    label = { Text("Pts") },
                    modifier = Modifier.width(80.dp)
                )
            }

            Text("Conseils/ordonnance")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = rule.adviceRule.requiredWords.joinToString(", "),
                    onValueChange = { value ->
                        onRuleChange(rule.copy(adviceRule = rule.adviceRule.copy(requiredWords = splitCsv(value))))
                    },
                    label = { Text("Mots présents") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = rule.adviceRule.forbiddenWords.joinToString(", "),
                    onValueChange = { value ->
                        onRuleChange(rule.copy(adviceRule = rule.adviceRule.copy(forbiddenWords = splitCsv(value))))
                    },
                    label = { Text("Mots absents") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = rule.adviceRule.points.toString(),
                    onValueChange = { value ->
                        onRuleChange(rule.copy(adviceRule = rule.adviceRule.copy(points = value.toIntOrNull() ?: 0)))
                    },
                    label = { Text("Pts") },
                    modifier = Modifier.width(80.dp)
                )
            }

            Divider()
            Text("Critères personnalisés", style = MaterialTheme.typography.subtitle1)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rule.customCriteria.forEachIndexed { index, criterion ->
                    Card(modifier = Modifier.fillMaxWidth(), elevation = 1.dp) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Switch(
                                    checked = criterion.enabled,
                                    onCheckedChange = { checked ->
                                        val updated = rule.customCriteria.toMutableList()
                                        updated[index] = criterion.copy(enabled = checked)
                                        onRuleChange(rule.copy(customCriteria = updated))
                                    }
                                )
                                OutlinedTextField(
                                    value = criterion.label,
                                    onValueChange = { value ->
                                        val updated = rule.customCriteria.toMutableList()
                                        updated[index] = criterion.copy(label = value)
                                        onRuleChange(rule.copy(customCriteria = updated))
                                    },
                                    label = { Text("Nom du critère") },
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    val updated = rule.customCriteria.toMutableList()
                                    updated.removeAt(index)
                                    onRuleChange(rule.copy(customCriteria = updated))
                                }) { Text("X") }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = {
                                        val updated = rule.customCriteria.toMutableList()
                                        updated[index] = criterion.copy(metric = nextEnum(criterion.metric))
                                        onRuleChange(rule.copy(customCriteria = updated))
                                    }
                                ) { Text("Métrique: ${criterion.metric.uiLabel()}") }

                                OutlinedButton(
                                    onClick = {
                                        val updated = rule.customCriteria.toMutableList()
                                        updated[index] = criterion.copy(rationScope = nextEnum(criterion.rationScope))
                                        onRuleChange(rule.copy(customCriteria = updated))
                                    }
                                ) { Text("Portée: ${criterion.rationScope.uiLabel()}") }

                                OutlinedTextField(
                                    value = criterion.points.toString(),
                                    onValueChange = { value ->
                                        val updated = rule.customCriteria.toMutableList()
                                        updated[index] = criterion.copy(points = value.toDoubleOrNull() ?: 0.0)
                                        onRuleChange(rule.copy(customCriteria = updated))
                                    },
                                    label = { Text("Pts") },
                                    modifier = Modifier.width(100.dp)
                                )
                            }

                            if (criterion.metric == FlexibleMetricKind.NUTRIENT_TOTAL) {
                                OutlinedTextField(
                                    value = criterion.nutrientLabel,
                                    onValueChange = { value ->
                                        val updated = rule.customCriteria.toMutableList()
                                        updated[index] = criterion.copy(nutrientLabel = value)
                                        onRuleChange(rule.copy(customCriteria = updated))
                                    },
                                    label = { Text("Nutriment (label exact)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = criterion.min?.toString() ?: "",
                                    onValueChange = { value ->
                                        val updated = rule.customCriteria.toMutableList()
                                        updated[index] = criterion.copy(min = value.toDoubleOrNull())
                                        onRuleChange(rule.copy(customCriteria = updated))
                                    },
                                    label = { Text("Min") },
                                    modifier = Modifier.width(120.dp)
                                )
                                OutlinedTextField(
                                    value = criterion.max?.toString() ?: "",
                                    onValueChange = { value ->
                                        val updated = rule.customCriteria.toMutableList()
                                        updated[index] = criterion.copy(max = value.toDoubleOrNull())
                                        onRuleChange(rule.copy(customCriteria = updated))
                                    },
                                    label = { Text("Max") },
                                    modifier = Modifier.width(120.dp)
                                )
                                if (criterion.metric == FlexibleMetricKind.INGREDIENT_KEYWORDS ||
                                    criterion.metric == FlexibleMetricKind.ADVICE_KEYWORDS ||
                                    criterion.metric == FlexibleMetricKind.REFERENCE_ID
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = criterion.requireAllIncludes,
                                            onCheckedChange = { checked ->
                                                val updated = rule.customCriteria.toMutableList()
                                                updated[index] = criterion.copy(requireAllIncludes = checked)
                                                onRuleChange(rule.copy(customCriteria = updated))
                                            }
                                        )
                                        Text("Tous les mots requis")
                                    }
                                }
                            }

                            if (criterion.metric == FlexibleMetricKind.INGREDIENT_KEYWORDS ||
                                criterion.metric == FlexibleMetricKind.ADVICE_KEYWORDS ||
                                criterion.metric == FlexibleMetricKind.REFERENCE_ID
                            ) {
                                OutlinedTextField(
                                    value = criterion.includes.joinToString(", "),
                                    onValueChange = { value ->
                                        val updated = rule.customCriteria.toMutableList()
                                        updated[index] = criterion.copy(includes = splitCsv(value))
                                        onRuleChange(rule.copy(customCriteria = updated))
                                    },
                                    label = { Text("Mots/IDs requis (csv)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = criterion.excludes.joinToString(", "),
                                    onValueChange = { value ->
                                        val updated = rule.customCriteria.toMutableList()
                                        updated[index] = criterion.copy(excludes = splitCsv(value))
                                        onRuleChange(rule.copy(customCriteria = updated))
                                    },
                                    label = { Text("Mots/IDs interdits (csv)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                OutlinedButton(onClick = {
                    val nextIndex = rule.customCriteria.size + 1
                    val newCriterion = FlexibleCriterionRule(
                        id = "c$nextIndex",
                        label = "Critère $nextIndex",
                        metric = FlexibleMetricKind.TOTAL_RATION_QUANTITY,
                        rationScope = RationScope.ALL,
                        points = 0.0
                    )
                    onRuleChange(rule.copy(customCriteria = rule.customCriteria + newCriterion))
                }) {
                    Text("Ajouter un critère")
                }
            }
        }
    }
}

@Composable
private fun MinMaxEditor(
    rule: MinMaxPointsRule,
    onChange: (MinMaxPointsRule) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = rule.min?.toString() ?: "",
            onValueChange = { onChange(rule.copy(min = it.toDoubleOrNull())) },
            label = { Text("Min") },
            modifier = Modifier.width(90.dp)
        )
        OutlinedTextField(
            value = rule.max?.toString() ?: "",
            onValueChange = { onChange(rule.copy(max = it.toDoubleOrNull())) },
            label = { Text("Max") },
            modifier = Modifier.width(90.dp)
        )
        OutlinedTextField(
            value = rule.points.toString(),
            onValueChange = { onChange(rule.copy(points = it.toIntOrNull() ?: 0)) },
            label = { Text("Pts") },
            modifier = Modifier.width(80.dp)
        )
    }
}

private fun splitCsv(value: String): List<String> {
    return value.split(",").map { it.trim() }.filter { it.isNotBlank() }
}

private fun FlexibleMetricKind.uiLabel(): String {
    return when (this) {
        FlexibleMetricKind.TOTAL_RATION_QUANTITY -> "Qté ration"
        FlexibleMetricKind.ENERGY_DENSITY -> "Densité énergie"
        FlexibleMetricKind.BEE_KCAL -> "BEC"
        FlexibleMetricKind.ENERGY_INTAKE_KCAL -> "Apport énergie"
        FlexibleMetricKind.PROTEIN_INTAKE_G -> "Protéines"
        FlexibleMetricKind.ANIMAL_WEIGHT_KG -> "Poids"
        FlexibleMetricKind.NUTRIENT_TOTAL -> "Total nutriment"
        FlexibleMetricKind.RATION_COUNT -> "Nb rations"
        FlexibleMetricKind.INGREDIENT_KEYWORDS -> "Mots ingrédients"
        FlexibleMetricKind.ADVICE_KEYWORDS -> "Mots conseils"
        FlexibleMetricKind.REFERENCE_ID -> "ID référence"
    }
}

private fun RationScope.uiLabel(): String {
    return when (this) {
        RationScope.ALL -> "Toutes"
        RationScope.CURRENT_ONLY -> "Actuelles"
        RationScope.PROPOSED_ONLY -> "Proposées"
        RationScope.FIRST_PROPOSED -> "1re proposée"
    }
}

private inline fun <reified T : Enum<T>> nextEnum(value: T): T {
    val values = enumValues<T>()
    val index = values.indexOf(value)
    return values[(index + 1) % values.size]
}

@Composable
@OptIn(ExperimentalKoalaPlotApi::class)
private fun HistogramBars(
    distribution: ExamGradingViewModel.MetricDistribution,
    criterionLow: Double?,
    criterionHigh: Double?
) {
    if (distribution.histogram.isEmpty()) return

    val minXRaw = distribution.histogram.first().from.toFloat()
    val maxXRaw = distribution.histogram.last().to.toFloat()
    val minX = if (minXRaw == maxXRaw) minXRaw - 1f else minXRaw
    val maxX = if (minXRaw == maxXRaw) maxXRaw + 1f else maxXRaw
    val xRange = minX..maxX

    val maxCount = (distribution.histogram.maxOfOrNull { it.count } ?: 1).coerceAtLeast(1)
    val yRange = 0f..(maxCount.toFloat() * 1.15f).coerceAtLeast(1f)

    val barData = distribution.histogram.map { bin ->
        val center = ((bin.from + bin.to) / 2.0).toFloat()
        verticalBarPlotEntry(center, 0f, bin.count.toFloat())
    }

    XYGraph(
        xAxisModel = FloatLinearAxisModel(xRange, minimumMajorTickIncrement = ((maxX - minX) / 5f).coerceAtLeast(0.1f)),
        yAxisModel = FloatLinearAxisModel(yRange, minimumMajorTickIncrement = 1f),
        xAxisTitle = distribution.label,
        yAxisTitle = "Nombre de copies",
        modifier = Modifier.fillMaxWidth().height(220.dp)
    ) {
        VerticalBarPlot(
            data = barData,
            bar = {
                DefaultVerticalBar(
                    brush = SolidColor(VetNutriColors.Primary.copy(alpha = 0.70f)),
                    modifier = Modifier.fillMaxSize()
                )
            },
            barWidth = 0.92f
        )

        criterionLow?.toFloat()?.let { low ->
            val line = listOf(Point(low, yRange.start), Point(low, yRange.endInclusive))
            LinePlot(
                data = line,
                lineStyle = LineStyle(
                    brush = SolidColor(MaterialTheme.colors.error),
                    strokeWidth = 2.dp,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
            )
        }
        criterionHigh?.toFloat()?.let { high ->
            val line = listOf(Point(high, yRange.start), Point(high, yRange.endInclusive))
            LinePlot(
                data = line,
                lineStyle = LineStyle(
                    brush = SolidColor(Color(0xFF2E7D32)),
                    strokeWidth = 2.dp,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
            )
        }
    }
}

private fun formatMetric(value: Double): String {
    if (!value.isFinite()) return ""
    val rounded = (value * 100.0).roundToInt() / 100.0
    return rounded.toString()
}

private fun parseMetricInput(value: String?): Double? {
    if (value.isNullOrBlank()) return null
    return value.replace(",", ".").toDoubleOrNull()
}

@Composable
private fun ReflectiveDistributionInsights(
    distribution: ExamGradingViewModel.MetricDistribution
) {
    if (distribution.count < 3) {
        Text("Analyse réflexive: effectif trop faible pour interprétation robuste.", style = MaterialTheme.typography.caption)
        return
    }
    val p10 = distribution.p10
    val p90 = distribution.p90
    val median = distribution.median
    val min = distribution.min
    val max = distribution.max
    val iqrApprox = (p90 - p10).coerceAtLeast(0.0)
    val relSpread = if (kotlin.math.abs(median) > 1e-9) iqrApprox / kotlin.math.abs(median) else Double.POSITIVE_INFINITY
    val lowTail = median - min
    val highTail = max - median
    val skewRatio = if (lowTail > 1e-9) highTail / lowTail else Double.POSITIVE_INFINITY
    val rightSkew = skewRatio > 1.6
    val leftSkew = skewRatio < 0.62
    val hasExtremeHigh = max > (p90 + 1.5 * iqrApprox)
    val hasExtremeLow = min < (p10 - 1.5 * iqrApprox)

    val spreadLabel = when {
        relSpread.isInfinite() -> "dispersion non interprétable (médiane proche de 0)"
        relSpread < 0.25 -> "dispersion faible"
        relSpread < 0.60 -> "dispersion modérée"
        else -> "dispersion élevée"
    }
    val shapeLabel = when {
        rightSkew -> "asymétrie à droite"
        leftSkew -> "asymétrie à gauche"
        else -> "distribution plutôt centrée"
    }
    val outlierLabel = when {
        hasExtremeHigh && hasExtremeLow -> "extrêmes bas et hauts détectés"
        hasExtremeHigh -> "extrêmes hauts détectés"
        hasExtremeLow -> "extrêmes bas détectés"
        else -> "pas d'extrême marqué"
    }

    val suggestedLow = distribution.suggestedLow
    val suggestedHigh = distribution.suggestedHigh
    val robustLow = p10
    val robustHigh = p90

    Text("Analyse réflexive: $spreadLabel, $shapeLabel, $outlierLabel.", style = MaterialTheme.typography.caption)
    Text(
        "Conseil: seuil robuste initial = [p10; p90] = [${formatMetric(robustLow)}; ${formatMetric(robustHigh)}], puis affiner vers [${formatMetric(suggestedLow)}; ${formatMetric(suggestedHigh)}] selon sévérité attendue.",
        style = MaterialTheme.typography.caption
    )
}
