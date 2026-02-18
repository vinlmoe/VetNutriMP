package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.CrossConsultationAnalysis
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal as AnimalKeys
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Startup as StartupKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.CrossConsultationAnalysisViewModel
import kotlinx.coroutines.launch

/**
 * Sélection multi-consultations (Phase 1) : recherche, filtre espèce, sélection, compteur.
 */
@Composable
fun CrossConsultationAnalysisView(
        viewModel: CrossConsultationAnalysisViewModel,
        onNavigateBack: () -> Unit,
        onOpenResults: () -> Unit,
        onOpenGrading: () -> Unit,
        modifier: Modifier = Modifier
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val consultations by viewModel.consultations.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val speciesFilter by viewModel.speciesFilter.collectAsState()
    val keywordFilter by viewModel.keywordFilter.collectAsState()
    val examIdFilter by viewModel.examIdFilter.collectAsState()
    val examExerciseIdFilter by viewModel.examExerciseIdFilter.collectAsState()
    val availableKeywords by viewModel.availableKeywords.collectAsState()
    val scope = rememberCoroutineScope()
    var aggregatesText by remember { mutableStateOf<String?>(null) }
    val summary by viewModel.summary.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadConsultations() }

    Column(modifier = modifier.fillMaxSize()) {
        TopBarSimple(
                title = CrossConsultationAnalysis.TITLE.translate(),
                onNavigateBack = onNavigateBack
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)) {
                OutlinedButton(
                        onClick = { viewModel.clearSelection() },
                        enabled = selectedIds.isNotEmpty()
                ) { Text(CrossConsultationAnalysis.RESET.translate()) }
                OutlinedButton(
                        onClick = { viewModel.selectAllVisible(consultations.map { it.consultationId }) },
                        enabled = consultations.isNotEmpty()
                ) { Text(CrossConsultationAnalysis.SELECT_ALL.translate()) }
                OutlinedButton(
                        onClick = onOpenGrading
                ) { Text("Notation") }
            }
        }

        Divider()

        Row(
                modifier = Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
        ) {
            // Panneau sélection
            Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(CrossConsultationAnalysis.SEARCH_LABEL.translate()) },
                        leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) },
                        singleLine = true
                )

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                            value = examIdFilter,
                            onValueChange = { viewModel.setExamIdFilter(it) },
                            modifier = Modifier.weight(1f),
                            label = { Text(StartupKeys.EXAM_STUDENT_NUMBER.translate()) },
                            singleLine = true
                    )
                    OutlinedTextField(
                            value = examExerciseIdFilter,
                            onValueChange = { viewModel.setExamExerciseIdFilter(it) },
                            modifier = Modifier.weight(1f),
                            label = { Text(AnimalKeys.EXAM_EXERCISE_ID.translate()) },
                            singleLine = true
                    )
                }

                // Filtre espèce
                Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                            selected = speciesFilter == null,
                            onClick = { viewModel.setSpeciesFilter(null) },
                            label = { Text(CrossConsultationAnalysis.SPECIES_ALL.translate()) }
                    )
                    Espece.entries.forEach { e ->
                        FilterChip(
                                selected = speciesFilter == e,
                                onClick = { viewModel.setSpeciesFilter(e) },
                                label = { Text(e.label) }
                        )
                    }
                }

                Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(CrossConsultationAnalysis.KEYWORDS_LABEL.translate())
                    if (availableKeywords.isEmpty()) {
                        Text(
                                CrossConsultationAnalysis.KEYWORDS_EMPTY.translate(),
                                style = androidx.compose.material.MaterialTheme.typography.caption,
                                color = androidx.compose.ui.graphics.Color.Gray
                        )
                    } else {
                        Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                    selected = keywordFilter.isEmpty(),
                                    onClick = { viewModel.clearKeywordFilter() },
                                    label = { Text(CrossConsultationAnalysis.KEYWORDS_ALL.translate()) }
                            )
                            availableKeywords.forEach { keyword ->
                                FilterChip(
                                        selected = keywordFilter.contains(keyword.uuid),
                                        onClick = { viewModel.toggleKeywordFilter(keyword.uuid) },
                                        label = { Text(keyword.label) }
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (consultations.isEmpty()) {
                    Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = VetNutriColors.Primary
                            )
                            Text(CrossConsultationAnalysis.EMPTY.translate())
                        }
                    }
                } else {
                    LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
                    ) {
                        items(consultations) { item ->
                            Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = 2.dp
                            ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth().padding(AppSizes.paddingSmall),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.animalName, style = androidx.compose.material.MaterialTheme.typography.subtitle1)
                                        Text(
                                                "${item.dateLabel} • ${item.objective}",
                                                style = androidx.compose.material.MaterialTheme.typography.body2
                                        )
                                        val refLabel =
                                                item.referenceLabel
                                                        ?: CrossConsultationAnalysis.REF_NONE.translate()
                                        Text(
                                                "${CrossConsultationAnalysis.REF_LABEL.translate()}: $refLabel | ${CrossConsultationAnalysis.SPECIES_LABEL.translate()}: ${item.speciesLabel} | ${CrossConsultationAnalysis.RATIONS_LABEL.translate()}: ${item.rationCount}",
                                                style = androidx.compose.material.MaterialTheme.typography.caption
                                        )
                                    }
                                    val selected = selectedIds.contains(item.consultationId)
                                    OutlinedButton(
                                            onClick = { viewModel.toggleSelection(item.consultationId) }
                                    ) {
                                        if (selected) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(CrossConsultationAnalysis.REMOVE.translate())
                                        } else {
                                            Text(CrossConsultationAnalysis.SELECT.translate())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Panneau résumé simple
            Column(
                    modifier =
                            Modifier.width(300.dp)
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(AppSizes.paddingSmall)
            ) {
                Text(
                        CrossConsultationAnalysis.SUMMARY_TITLE.translate(),
                        style = androidx.compose.material.MaterialTheme.typography.h6
                )
                Divider()
                Text(
                        CrossConsultationAnalysis.VISIBLE_COUNT.translate(
                                consultations.size.toString()
                        )
                )
                Text(
                        CrossConsultationAnalysis.SELECTED_COUNT.translate(
                                summary.selectedCount.toString()
                        )
                )
                Text(
                        CrossConsultationAnalysis.DISTINCT_ANIMALS.translate(
                                summary.distinctAnimals.toString()
                        )
                )
                Text(
                        CrossConsultationAnalysis.DISTINCT_REFERENCES.translate(
                                summary.distinctReferences.toString()
                        )
                )
                Text(
                        CrossConsultationAnalysis.TOTAL_RATIONS.translate(
                                summary.totalRations.toString()
                        )
                )

                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                OutlinedButton(
                        onClick = {
                            scope.launch {
                                val agg = viewModel.computeEnergyAggregates()
                                aggregatesText =
                                        agg.joinToString("\n") {
                                            "- ${it.animalName}: ${it.value} (placeholder énergie)"
                                        }
                            }
                        },
                        enabled = selectedIds.isNotEmpty()
                ) { Text(CrossConsultationAnalysis.QUICK_CALC.translate()) }

                aggregatesText?.let { text ->
                    Divider()
                    Text(
                            CrossConsultationAnalysis.CALC_PREVIEW_TITLE.translate(),
                            style = androidx.compose.material.MaterialTheme.typography.subtitle1
                    )
                    Text(text, style = androidx.compose.material.MaterialTheme.typography.body2)
                }

                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                OutlinedButton(
                        onClick = onOpenResults,
                        enabled = selectedIds.isNotEmpty()
                ) { Text(CrossConsultationAnalysis.OPEN_ANALYSIS.translate()) }
            }
        }
    }
}
