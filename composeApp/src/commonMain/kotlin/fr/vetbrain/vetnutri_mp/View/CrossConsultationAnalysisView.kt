package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Components.FlowRow
import fr.vetbrain.vetnutri_mp.Components.PuceFiltre
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal as AnimalKeys
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.CrossConsultationAnalysis
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Startup as StartupKeys
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.CrossConsultationAnalysisViewModel

private val bordureCroisee = Color(0x1F000000)

@Composable
private fun discret(): Color = MaterialTheme.colors.onSurface.copy(alpha = 0.62f)

/**
 * Sélection des consultations à analyser ensemble : recherche, filtres (espèce, mots-clés,
 * examen), liste à cocher et résumé de la sélection avec le bouton d'analyse.
 *
 * Grand écran : résumé à droite. Sous [AppSizes.breakpointCompact] : résumé en barre fixe en bas.
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
    val toutes by viewModel.allItems.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val summary by viewModel.summary.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadConsultations() }

    Column(modifier = modifier.fillMaxSize()) {
        TopBarSimple(title = CrossConsultationAnalysis.TITLE.translate(), onNavigateBack = onNavigateBack) {
            TextButton(onClick = onOpenGrading) {
                Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, modifier = Modifier.size(18.dp), tint = VetNutriColors.Primary)
                Spacer(Modifier.width(6.dp))
                Text(CrossConsultationAnalysis.OPEN_GRADING.translate(), color = VetNutriColors.PrimaryVariant)
            }
        }
        Divider()

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val largeur = maxWidth
            val compact = largeur < AppSizes.breakpointCompact
            if (compact) {
                Column(Modifier.fillMaxSize()) {
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Filtres(viewModel)
                        ListeConsultations(viewModel, isLoading, consultations, toutes.size, selectedIds, Modifier.weight(1f))
                    }
                    BarreSelection(summary, onOpenResults, onReset = { viewModel.clearSelection() })
                }
            } else {
                Row(
                        Modifier.fillMaxSize().padding(AppSizes.paddingMedium),
                        horizontalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        Filtres(viewModel)
                        ListeConsultations(viewModel, isLoading, consultations, toutes.size, selectedIds, Modifier.weight(1f))
                    }
                    PanneauSelection(
                            summary = summary,
                            onOpenResults = onOpenResults,
                            onReset = { viewModel.clearSelection() },
                            modifier = Modifier.width(if (largeur < 1000.dp) 260.dp else 300.dp).fillMaxHeight()
                    )
                }
            }
        }
    }
}

/** Recherche et filtres (espèce, mots-clés, examen), repliables. */
@Composable
private fun Filtres(viewModel: CrossConsultationAnalysisViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val speciesFilter by viewModel.speciesFilter.collectAsState()
    val keywordFilter by viewModel.keywordFilter.collectAsState()
    val examIdFilter by viewModel.examIdFilter.collectAsState()
    val examExerciseIdFilter by viewModel.examExerciseIdFilter.collectAsState()
    val availableKeywords by viewModel.availableKeywords.collectAsState()
    // Repliés par défaut : la liste des consultations reste visible ; le bouton indique les filtres actifs
    var ouverts by remember { mutableStateOf(false) }
    val actifs = (if (speciesFilter != null) 1 else 0) + keywordFilter.size +
            (if (examIdFilter.isNotBlank()) 1 else 0) + (if (examExerciseIdFilter.isNotBlank()) 1 else 0)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(CrossConsultationAnalysis.SEARCH_LABEL.translate()) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty())
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = translate("crossConsultation.effacer"))
                            }
                    },
                    singleLine = true
            )
            Spacer(Modifier.width(6.dp))
            TextButton(onClick = { ouverts = !ouverts }) {
                Icon(if (ouverts) Icons.Default.ExpandLess else Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                        if (actifs > 0) translate("crossConsultation.filtresActifs", actifs.toString()) else translate("crossConsultation.filtres"),
                        fontSize = 13.sp
                )
            }
        }
        if (!ouverts) return@Column

        Card(elevation = 0.dp, border = BorderStroke(1.dp, bordureCroisee), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(CrossConsultationAnalysis.SPECIES_LABEL.translate(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = discret())
                FlowRow(mainAxisSpacing = 6.dp, crossAxisSpacing = 6.dp) {
                    PuceFiltre(CrossConsultationAnalysis.SPECIES_ALL.translate(), selectionnee = speciesFilter == null,
                            onClick = { viewModel.setSpeciesFilter(null) })
                    Espece.valuesExcept().forEach { e ->
                        PuceFiltre(e.translateEnum(), selectionnee = speciesFilter == e, onClick = { viewModel.setSpeciesFilter(e) })
                    }
                }
                Text(CrossConsultationAnalysis.KEYWORDS_LABEL.translate(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = discret())
                if (availableKeywords.isEmpty()) {
                    Text(CrossConsultationAnalysis.KEYWORDS_EMPTY.translate(), style = MaterialTheme.typography.caption, color = discret())
                } else {
                    FlowRow(mainAxisSpacing = 6.dp, crossAxisSpacing = 6.dp) {
                        PuceFiltre(CrossConsultationAnalysis.KEYWORDS_ALL.translate(), selectionnee = keywordFilter.isEmpty(),
                                onClick = { viewModel.clearKeywordFilter() })
                        availableKeywords.forEach { keyword ->
                            PuceFiltre(keyword.label, selectionnee = keywordFilter.contains(keyword.uuid),
                                    onClick = { viewModel.toggleKeywordFilter(keyword.uuid) })
                        }
                    }
                }
                Text(translate("crossConsultation.examen"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = discret())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                            value = examIdFilter,
                            onValueChange = { viewModel.setExamIdFilter(it) },
                            modifier = Modifier.weight(1f),
                            label = { Text(StartupKeys.EXAM_STUDENT_NUMBER.translate(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            singleLine = true
                    )
                    OutlinedTextField(
                            value = examExerciseIdFilter,
                            onValueChange = { viewModel.setExamExerciseIdFilter(it) },
                            modifier = Modifier.weight(1f),
                            label = { Text(AnimalKeys.EXAM_EXERCISE_ID.translate(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            singleLine = true
                    )
                }
            }
        }
    }
}

@Composable
private fun ListeConsultations(
        viewModel: CrossConsultationAnalysisViewModel,
        isLoading: Boolean,
        consultations: List<CrossConsultationAnalysisViewModel.ConsultationItem>,
        total: Int,
        selectedIds: Set<String>,
        modifier: Modifier
) {
    val visibles = consultations.map { it.consultationId }
    val toutesCochees = visibles.isNotEmpty() && visibles.all { it in selectedIds }

    Column(modifier) {
        // En-tête de liste : nombre et case « tout cocher » des consultations visibles
        Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                        .background(VetNutriColors.Primary.copy(alpha = 0.06f), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .padding(end = 8.dp)
        ) {
            Checkbox(
                    checked = toutesCochees,
                    enabled = visibles.isNotEmpty(),
                    onCheckedChange = { if (toutesCochees) viewModel.deselectAllVisible(visibles) else viewModel.selectAllVisible(visibles) },
                    colors = CheckboxDefaults.colors(checkedColor = VetNutriColors.Primary)
            )
            Text(
                    if (consultations.size == total) translate("crossConsultation.nbConsultations", total.toString())
                    else translate("crossConsultation.nbConsultationsFiltrees", consultations.size.toString(), total.toString()),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
            )
            Text(
                    translate("crossConsultation.nbCochees", visibles.count { it in selectedIds }.toString()),
                    fontSize = 12.sp,
                    color = discret()
            )
        }
        Divider()

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VetNutriColors.Primary)
            }
            consultations.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SearchOff, contentDescription = null, tint = discret(), modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(CrossConsultationAnalysis.EMPTY.translate(), color = discret())
                }
            }
            else -> LazyColumn(Modifier.fillMaxSize()) {
                items(consultations, key = { it.consultationId }) { item ->
                    LigneConsultation(item, item.consultationId in selectedIds) { viewModel.toggleSelection(item.consultationId) }
                    Divider(color = bordureCroisee)
                }
            }
        }
    }
}

@Composable
private fun LigneConsultation(item: CrossConsultationAnalysisViewModel.ConsultationItem, cochee: Boolean, onBasculer: () -> Unit) {
    Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                    .background(if (cochee) VetNutriColors.Primary.copy(alpha = 0.08f) else Color.Transparent)
                    .clickable(onClick = onBasculer)
                    .padding(end = 10.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Checkbox(checked = cochee, onCheckedChange = { onBasculer() }, colors = CheckboxDefaults.colors(checkedColor = VetNutriColors.Primary))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.animalName, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false))
                Spacer(Modifier.width(8.dp))
                Text(item.dateLabel, fontSize = 12.sp, color = discret())
            }
            Text(item.objective, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            FlowRow(mainAxisSpacing = 6.dp, crossAxisSpacing = 4.dp) {
                Etiquette(item.speciesLabel)
                Etiquette(item.referenceLabel ?: CrossConsultationAnalysis.REF_NONE.translate())
                Etiquette(translate("crossConsultation.nbRations", item.rationCount.toString()))
            }
        }
    }
}

@Composable
private fun Etiquette(texte: String) {
    Text(
            texte,
            fontSize = 11.sp,
            color = VetNutriColors.PrimaryVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 260.dp)
                    .background(VetNutriColors.Primary.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
    )
}

/** Résumé de la sélection (grand écran). */
@Composable
private fun PanneauSelection(
        summary: CrossConsultationAnalysisViewModel.SelectionSummary,
        onOpenResults: () -> Unit,
        onReset: () -> Unit,
        modifier: Modifier
) {
    Card(modifier, elevation = 0.dp, border = BorderStroke(1.dp, bordureCroisee)) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(translate("crossConsultation.selectionTitre"), style = MaterialTheme.typography.h6)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(summary.selectedCount.toString(), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = VetNutriColors.PrimaryVariant)
                Spacer(Modifier.width(6.dp))
                Text(translate("crossConsultation.consultationsCochees"), fontSize = 13.sp, modifier = Modifier.padding(bottom = 7.dp))
            }
            Divider()
            LigneResume(translate("crossConsultation.resume.animaux"), summary.distinctAnimals)
            LigneResume(translate("crossConsultation.resume.referentiels"), summary.distinctReferences)
            LigneResume(translate("crossConsultation.resume.rations"), summary.totalRations)
            Spacer(Modifier.height(4.dp))
            Button(
                    onClick = onOpenResults,
                    enabled = summary.selectedCount > 0,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = VetNutriColors.OnPrimary)
            ) {
                Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(CrossConsultationAnalysis.OPEN_ANALYSIS.translate())
            }
            if (summary.selectedCount == 0) {
                Text(translate("crossConsultation.aideSelection"), fontSize = 12.sp, color = discret())
            } else {
                TextButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
                    Text(translate("crossConsultation.viderSelection"), color = discret())
                }
            }
        }
    }
}

@Composable
private fun LigneResume(libelle: String, valeur: Int) {
    Row(Modifier.fillMaxWidth()) {
        Text(libelle, fontSize = 13.sp, color = discret(), modifier = Modifier.weight(1f))
        Text(valeur.toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

/** Résumé de la sélection (téléphone) : barre fixe sous la liste. */
@Composable
private fun BarreSelection(
        summary: CrossConsultationAnalysisViewModel.SelectionSummary,
        onOpenResults: () -> Unit,
        onReset: () -> Unit
) {
    Surface(elevation = 8.dp, color = MaterialTheme.colors.surface) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            Column(Modifier.weight(1f)) {
                Text(translate("crossConsultation.nbSelectionnees", summary.selectedCount.toString()), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                        translate("crossConsultation.resumeCourt", summary.distinctAnimals.toString(), summary.totalRations.toString()),
                        fontSize = 12.sp,
                        color = discret()
                )
            }
            if (summary.selectedCount > 0) {
                IconButton(onClick = onReset) { Icon(Icons.Default.Close, contentDescription = translate("crossConsultation.viderSelection")) }
            }
            Button(
                    onClick = onOpenResults,
                    enabled = summary.selectedCount > 0,
                    colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary, contentColor = VetNutriColors.OnPrimary)
            ) { Text(translate("crossConsultation.analyser")) }
        }
    }
}
