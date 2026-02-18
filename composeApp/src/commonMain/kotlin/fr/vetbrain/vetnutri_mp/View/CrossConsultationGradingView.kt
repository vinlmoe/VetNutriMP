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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.*
import fr.vetbrain.vetnutri_mp.ExcelPlatform.saveCsvFileForExport
import fr.vetbrain.vetnutri_mp.Services.ExamGradingCsvService
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.CrossConsultationAnalysisViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ExamGradingViewModel
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()
    var examId by remember { mutableStateOf("") }
    var exerciseId by remember { mutableStateOf("") }

    val csvService = remember { ExamGradingCsvService() }

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
                            val items = allItems.filter {
                                it.examId == examId.trim() && it.examExerciseId == exerciseId.trim()
                            }
                            gradingViewModel.computeGrades(items, examId.trim(), exerciseId.trim())
                        }
                    }
                ) { Text("Calculer notes") }
                Button(
                    onClick = { gradingViewModel.saveRule() },
                    enabled = rule != null
                ) { Text("Sauver règles") }
            }

            if (rule != null) {
                RuleEditor(
                    rule = rule!!,
                    onRuleChange = { gradingViewModel.updateRule(it) }
                )
            }

            if (grades.isNotEmpty()) {
                Text("Notes", style = MaterialTheme.typography.h6)
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp, max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(grades) { grade ->
                        Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("${grade.animalName} — Étudiant: ${grade.studentId}")
                                Text("Auto: ${grade.autoScore} / 20")
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
