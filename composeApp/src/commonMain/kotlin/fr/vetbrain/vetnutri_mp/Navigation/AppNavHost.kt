package fr.vetbrain.vetnutri_mp.Navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBar
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ExamSession
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.View.*

@Composable
internal fun AppNavHost(
    nav: AppNavController,
    models: AppNavModels,
    repos: AppNavRepositories,
    examSession: ExamSession?
) {
    when (nav.screen) {
        Screen.List -> {
            val examBorderModifier =
                if (examSession != null) Modifier.border(1.dp, Color.Red) else Modifier
            Column(
                modifier = Modifier.fillMaxSize()
                    .then(examBorderModifier)
                    .padding(if (examSession != null) 2.dp else 0.dp)
            ) {
                val examInfoTitle = examSession?.let { session ->
                    "Liste des animaux — ID examen: ${session.studentNumber} | Étudiant: ${session.studentId}"
                } ?: "Liste des animaux"
                TopBar(title = examInfoTitle, onSettingsClick = { nav.navigate(Screen.Settings) })

                AnimalListView(
                    viewModel = models.animalListViewModel,
                    onAddAnimal = {
                        nav.isEditing = false
                        nav.selectedAnimal = null
                        models.createAnimalViewModel.resetAnimal()
                        nav.navigate(Screen.Create)
                    },
                    onSelectAnimal = { animal: AnimalEv ->
                        nav.selectedAnimal = animal
                        models.animalDetailViewModel.setAnimal(animal)
                        nav.navigate(Screen.Detail)
                    },
                    onEditAnimal = { animal: AnimalEv ->
                        nav.selectedAnimal = animal
                        models.createAnimalViewModel.updateAnimal(animal)
                        nav.isEditing = true
                        nav.navigate(Screen.Create)
                    },
                    onShowFoodList = { nav.navigate(Screen.FoodList) },
                    onShowCalculationTabs = { nav.navigate(Screen.CalculationTabs) },
                    examSession = examSession,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
        }

        Screen.Create -> {
            Column(modifier = Modifier.fillMaxSize()) {
                TopBar(
                    title = if (nav.isEditing) "Modifier un animal" else "Ajouter un animal",
                    onBackClick = {
                        nav.isEditing = false
                        nav.selectedAnimal = null
                        nav.navigate(Screen.List)
                    },
                    onSettingsClick = { nav.navigate(Screen.Settings) }
                )
                CreateAnimalView(
                    viewModel = models.createAnimalViewModel,
                    onNavigateBack = {
                        nav.isEditing = false
                        nav.selectedAnimal = null
                        nav.navigate(Screen.List)
                    },
                    onAnimalCreated = { animal ->
                        nav.isEditing = false
                        nav.selectedAnimal = animal
                        models.animalDetailViewModel.setAnimal(animal)
                        nav.navigate(Screen.Detail)
                    },
                    isEditing = nav.isEditing,
                    examSession = examSession,
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )
            }
        }

        Screen.Detail -> {
            Column(modifier = Modifier.fillMaxSize()) {
                AnimalDetailView(
                    viewModel = models.animalDetailViewModel,
                    settingsViewModel = models.settingsViewModel,
                    onNavigateBack = { nav.navigate(Screen.List) },
                    onOpenSettings = { nav.navigate(Screen.Settings) },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    equationRepository = repos.equationRepository,
                    recipeRepository = repos.recipeRepository,
                    conseilRepository = repos.conseilRepository,
                    isExamMode = examSession != null
                )
            }
        }

        Screen.FoodList -> {
            FoodListView(
                viewModel = models.foodListViewModel,
                onNavigateBack = { nav.navigate(Screen.List) },
                onOpenSettings = { nav.navigate(Screen.Settings) },
                onEditFood = { foodUuid ->
                    nav.selectedFoodUuid = foodUuid
                    nav.navigate(Screen.FoodEdit)
                },
                onCreateFood = {
                    nav.selectedFoodUuid = null
                    nav.navigate(Screen.FoodEdit)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.FoodEdit -> {
            FoodEditView(
                viewModel = models.foodEditViewModel,
                onNavigateBack = {
                    nav.selectedFoodUuid = null
                    nav.navigate(Screen.FoodList)
                },
                onNavigateToSettings = { nav.navigate(Screen.Settings) },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.CalculationTabs -> {
            CalculationTabsView(
                equationViewModel = models.equationViewModel,
                biblioRefViewModel = models.biblioRefViewModel,
                referenceEvViewModel = models.referenceEvViewModel,
                conseilRepository = repos.conseilRepository,
                onNavigateBack = { nav.navigate(Screen.List) },
                onEditReferenceEv = { referenceEvId ->
                    nav.selectedReferenceEvId = referenceEvId
                    nav.navigate(Screen.NewReferenceEvEdit)
                },
                onCreateReferenceEv = {
                    nav.selectedReferenceEvId = null
                    nav.navigate(Screen.NewReferenceEvEdit)
                },
                onBulkEditReferences = { ids ->
                    nav.selectedReferenceIdsForBulk = ids
                    models.bulkReferenceEditorViewModel.loadReferences(ids)
                    models.bulkReferenceEditorViewModel.loadAvailableBiblioRefs()
                    nav.navigate(Screen.BulkReferenceEditor)
                },
                onEditConseil = { conseilId ->
                    nav.selectedConseilId = conseilId
                    nav.navigate(Screen.ConseilEdit)
                },
                onCreateConseil = {
                    nav.selectedConseilId = null
                    nav.navigate(Screen.ConseilEdit)
                },
                selectedTab = nav.selectedCalculationTab,
                onTabChanged = { nav.selectedCalculationTab = it },
                modifier = Modifier.fillMaxSize(),
                isExamMode = examSession != null,
                biblioRefRepository = repos.biblioRefRepository,
                equationRepository = repos.equationRepository,
                referenceEvRepository = repos.databaseReferenceEvRepository,
                platformDispatcher = repos.platformDispatcher
            )
        }

        Screen.BiblioRefList -> {
            BiblioRefListView(
                viewModel = models.biblioRefViewModel,
                onEditBiblioRef = { biblioRefId ->
                    nav.selectedBiblioRefId = biblioRefId
                    nav.navigate(Screen.BiblioRefEdit)
                },
                onCreateBiblioRef = {
                    nav.selectedBiblioRefId = null
                    nav.navigate(Screen.BiblioRefEdit)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.BiblioRefEdit -> {
            BiblioRefEditView(
                viewModel = models.biblioRefViewModel,
                biblioRefId = nav.selectedBiblioRefId,
                onNavigateBack = {
                    nav.selectedBiblioRefId = null
                    nav.navigate(Screen.BiblioRefList)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.EquationList -> {
            EquationListView(
                viewModel = models.equationViewModel,
                onEditEquation = { equationId ->
                    nav.selectedEquationId = equationId
                    models.equationViewModel.clearOperationMessage()
                    nav.navigate(Screen.EquationEdit)
                },
                onCreateEquation = {
                    nav.selectedEquationId = null
                    models.equationViewModel.clearOperationMessage()
                    nav.navigate(Screen.EquationEdit)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.EquationEdit -> {
            EquationEditView(
                viewModel = models.equationViewModel,
                equationId = nav.selectedEquationId,
                onNavigateBack = {
                    nav.selectedEquationId = null
                    nav.navigate(Screen.EquationList)
                },
                modifier = Modifier.fillMaxSize()
            )
        }


        Screen.ReferenceEvList -> {
            NutrientRequirementView(
                viewModel = models.referenceEvViewModel,
                onEditReference = { referenceEvId ->
                    nav.selectedReferenceEvId = referenceEvId
                    nav.navigate(Screen.NewReferenceEvEdit)
                },
                onCreateReference = {
                    nav.selectedReferenceEvId = null
                    nav.navigate(Screen.NewReferenceEvEdit)
                },
                onEditNutrients = { referenceEvId ->
                    nav.selectedReferenceEvId = referenceEvId
                    nav.navigate(Screen.ReferenceEvNutrient)
                },
                onBulkEdit = { ids ->
                    nav.selectedReferenceIdsForBulk = ids
                    models.bulkReferenceEditorViewModel.loadReferences(ids)
                    models.bulkReferenceEditorViewModel.loadAvailableBiblioRefs()
                    nav.navigate(Screen.BulkReferenceEditor)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.ReferenceEvNutrient -> {
            ReferenceEvNutrientView(
                referenceEvViewModel = models.referenceEvViewModel,
                biblioRefRepository = repos.biblioRefRepository,
                referenceEvRepository = repos.databaseReferenceEvRepository,
                platformDispatcher = repos.platformDispatcher,
                referenceEvId = nav.selectedReferenceEvId ?: "",
                onNavigateBack = { nav.navigate(Screen.EquationList) },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.BulkReferenceEditor -> {
            BulkReferenceEditorView(
                viewModel = models.bulkReferenceEditorViewModel,
                onNavigateBack = {
                    nav.selectedCalculationTab = 2
                    nav.navigate(Screen.CalculationTabs)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.ReferenceEvTabs -> {
            NewReferenceEvEditView(
                viewModel = models.newReferenceEvViewModel,
                referenceId = nav.selectedReferenceEvId,
                onNavigateBack = { nav.navigate(Screen.ReferenceEvList) },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.NewReferenceEvEdit -> {
            NewReferenceEvEditView(
                viewModel = models.newReferenceEvViewModel,
                referenceId = nav.selectedReferenceEvId,
                onNavigateBack = {
                    nav.selectedReferenceEvId = null
                    nav.selectedCalculationTab = 2
                    nav.navigate(Screen.CalculationTabs)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.ConseilEdit -> {
            ConseilEditView(
                conseilRepository = repos.conseilRepository,
                conseilId = nav.selectedConseilId,
                onNavigateBack = {
                    nav.selectedConseilId = null
                    nav.navigate(Screen.CalculationTabs)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.Settings -> {
            Scaffold(
                topBar = {
                    TopBarSimple(
                        title = "Paramètres",
                        onNavigateBack = { nav.navigate(Screen.List) }
                    )
                }
            ) { paddingValues ->
                SettingsView(
                    viewModel = models.settingsViewModel,
                    importViewModel = models.importViewModel,
                    onImportAnimals = { models.importViewModel.importAnimalsFromFileUI() },
                    onBack = { nav.navigate(Screen.List) },
                    onAnimalListRefresh = { models.animalListViewModel.loadAnimals() },
                    onFoodListRefresh = {},
                    onShowCrossAnalysis = { nav.navigate(Screen.CrossAnalysis) },
                    modifier = Modifier.padding(paddingValues),
                    onSpeciesClick = { species ->
                        nav.selectedSpecies = species
                        nav.navigate(Screen.SpeciesPreferences)
                    },
                    onBackupClick = { nav.navigate(Screen.BackupRestore) },
                    isExamMode = examSession != null
                )
            }
        }

        Screen.SpeciesPreferences -> {
            val species = nav.selectedSpecies
            if (species != null) {
                Scaffold(
                    topBar = {
                        TopBarSimple(
                            title = "${"preferences.title".translate()} ${species.translateEnum()}",
                            onNavigateBack = { nav.navigate(Screen.Settings) }
                        )
                    }
                ) { paddingValues ->
                    fr.vetbrain.vetnutri_mp.View.SpeciesPreferencesView(
                        species = species,
                        preferencesRepository = repos.preferencesRepository,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            } else {
                nav.navigate(Screen.Settings)
            }
        }

        Screen.CrossAnalysis -> {
            CrossConsultationAnalysisView(
                viewModel = models.crossAnalysisViewModel,
                onNavigateBack = { nav.navigate(Screen.List) },
                onOpenResults = { nav.navigate(Screen.CrossAnalysisResults) },
                onOpenGrading = { nav.navigate(Screen.CrossAnalysisGrading) },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.CrossAnalysisGrading -> {
            CrossConsultationGradingView(
                analysisViewModel = models.crossAnalysisViewModel,
                gradingViewModel = models.examGradingViewModel,
                onNavigateBack = { nav.navigate(Screen.CrossAnalysis) }
            )
        }

        Screen.CrossAnalysisResults -> {
            CrossConsultationResultsView(
                viewModel = models.crossAnalysisViewModel,
                onNavigateBack = { nav.navigate(Screen.CrossAnalysis) },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.BackupRestore -> {
            val backupVm = models.backupRestoreViewModel
            if (backupVm != null) {
                BackupRestoreView(
                    viewModel = backupVm,
                    onBack = { nav.navigate(Screen.Settings) }
                )
            } else {
                nav.navigate(Screen.Settings)
            }
        }

        Screen.TestYellowBox -> {
            Box(modifier = Modifier.fillMaxSize().background(Color.Yellow)) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "TEST BOÎTE JAUNE\nCeci est un test de visibilité",
                        style = MaterialTheme.typography.h4,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { nav.navigate(Screen.List) },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
                    ) { Text("Retour à la liste", color = Color.White) }
                }
            }
        }
    }
}

