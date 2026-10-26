package fr.vetbrain.vetnutri_mp.Navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBar
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ExamSession
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.View.*

@Composable
internal fun AppNavHost(
    nav: AppNavController,
    models: AppNavModels,
    /**
     * Seul repository encore transmis à une vue : les écrans d'analyse (rations, graphiques,
     * ajustement multi-nutriments, carte thermique) le passent aux calculs de la couche Data
     * (`AlimentRation.getNutrientWithComplementary`, `getEnergie`). Son remplacement complet par
     * des méthodes de ViewModel reste à faire (≈ 200 points d'appel).
     */
    equationRepository: EquationRepository,
    examSession: ExamSession?
) {
    // Retour système (Android) : dépile, ou revient à la liste depuis un écran racine autre que la liste.
    PlatformBackHandler(enabled = nav.canGoBack || nav.screen != Screen.List) { nav.backOr(Screen.List) }

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
                    onShowMultiRation = { nav.navigate(Screen.MultiRation) },
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
                        nav.backOr(Screen.List)
                    },
                    onSettingsClick = { nav.navigate(Screen.Settings) }
                )
                CreateAnimalView(
                    viewModel = models.createAnimalViewModel,
                    onNavigateBack = {
                        nav.isEditing = false
                        nav.selectedAnimal = null
                        nav.backOr(Screen.List)
                    },
                    onAnimalCreated = { animal ->
                        nav.isEditing = false
                        nav.selectedAnimal = animal
                        models.animalDetailViewModel.setAnimal(animal)
                        // Le formulaire est remplacé par la fiche : « retour » depuis la fiche revient à la liste.
                        nav.replace(Screen.Detail)
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
                    animalExportViewModel = models.animalExportViewModel,
                    recipeDialogViewModel = models.recipeDialogViewModel,
                    onNavigateBack = { nav.backOr(Screen.List) },
                    onOpenSettings = { nav.navigate(Screen.Settings) },
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    equationRepository = equationRepository,
                    isExamMode = examSession != null
                )
            }
        }

        Screen.FoodList -> {
            FoodListView(
                viewModel = models.foodListViewModel,
                onNavigateBack = { nav.backOr(Screen.List) },
                onOpenSettings = { nav.navigate(Screen.Settings) },
                onEditFood = { foodUuid ->
                    nav.selectedFoodUuid = foodUuid
                    nav.navigate(Screen.FoodEdit)
                },
                onCreateFood = {
                    nav.clearFoodSelection()
                    nav.navigate(Screen.FoodEdit)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.FoodEdit -> {
            FoodEditView(
                viewModel = models.foodEditViewModel,
                onNavigateBack = {
                    nav.clearFoodSelection()
                    nav.backOr(Screen.FoodList)
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
                conseilsViewModel = models.conseilsViewModel,
                newReferenceEvViewModel = models.newReferenceEvViewModel,
                onNavigateBack = { nav.backOr(Screen.List) },
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
                isExamMode = examSession != null
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
                    nav.backOr(Screen.BiblioRefList)
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
                    nav.backOr(Screen.EquationList)
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
                nutrientRefViewModel = models.nutrientRefViewModel,
                referenceEvId = nav.selectedReferenceEvId ?: "",
                onNavigateBack = { nav.backOr(Screen.EquationList) },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.BulkReferenceEditor -> {
            BulkReferenceEditorView(
                viewModel = models.bulkReferenceEditorViewModel,
                onNavigateBack = {
                    nav.selectedCalculationTab = 2
                    nav.backOr(Screen.CalculationTabs)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.ReferenceEvTabs -> {
            NewReferenceEvEditView(
                viewModel = models.newReferenceEvViewModel,
                referenceId = nav.selectedReferenceEvId,
                onNavigateBack = { nav.backOr(Screen.ReferenceEvList) },
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
                    nav.backOr(Screen.CalculationTabs)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.ConseilEdit -> {
            ConseilEditView(
                conseilsViewModel = models.conseilsViewModel,
                conseilId = nav.selectedConseilId,
                onNavigateBack = {
                    nav.selectedConseilId = null
                    nav.backOr(Screen.CalculationTabs)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.Settings -> {
            Scaffold(
                topBar = {
                    TopBarSimple(
                        title = translate("settings.title"),
                        onNavigateBack = { nav.backOr(Screen.List) }
                    )
                }
            ) { paddingValues ->
                SettingsView(
                    viewModel = models.settingsViewModel,
                    importViewModel = models.importViewModel,
                    preferencesViewModel = models.preferencesViewModel,
                    onImportAnimals = { models.importViewModel.importAnimalsFromFileUI() },
                    onBack = { nav.backOr(Screen.List) },
                    onAnimalListRefresh = { models.animalListViewModel.loadAnimals() },
                    onFoodListRefresh = {},
                    onShowCrossAnalysis = { nav.navigate(Screen.CrossAnalysis) },
                    modifier = Modifier.padding(paddingValues),
                    onSpeciesClick = { species ->
                        nav.selectedSpecies = species
                        nav.navigate(Screen.SpeciesPreferences)
                    },
                    onBackupClick = { nav.navigate(Screen.BackupRestore) },
                    onLegacyMigrationClick = { nav.navigate(Screen.LegacyMigration) },
                    onBulkBiblioClick = { nav.navigate(Screen.BulkAddBiblioToFoods) },
                    isExamMode = examSession != null
                )
            }
        }

        Screen.BulkAddBiblioToFoods -> {
            BulkAddBiblioToFoodsView(
                viewModel = models.bulkAddBiblioToFoodsViewModel,
                onNavigateBack = { nav.backOr(Screen.Settings) },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.SpeciesPreferences -> {
            val species = nav.selectedSpecies
            if (species != null) {
                Scaffold(
                    topBar = {
                        TopBarSimple(
                            title = "${"preferences.title".translate()} ${species.translateEnum()}",
                            onNavigateBack = { nav.backOr(Screen.Settings) }
                        )
                    }
                ) { paddingValues ->
                    fr.vetbrain.vetnutri_mp.View.SpeciesPreferencesView(
                        species = species,
                        preferencesViewModel = models.preferencesViewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            } else {
                nav.replace(Screen.Settings)
            }
        }

        Screen.MultiRation -> {
            MultiRationExplorerView(
                viewModel = models.multiRationViewModel,
                onNavigateBack = { nav.backOr(Screen.List) },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.CrossAnalysis -> {
            CrossConsultationAnalysisView(
                viewModel = models.crossAnalysisViewModel,
                onNavigateBack = { nav.backOr(Screen.List) },
                onOpenResults = { nav.navigate(Screen.CrossAnalysisResults) },
                onOpenGrading = { nav.navigate(Screen.CrossAnalysisGrading) },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.CrossAnalysisGrading -> {
            CrossConsultationGradingView(
                analysisViewModel = models.crossAnalysisViewModel,
                gradingViewModel = models.examGradingViewModel,
                onNavigateBack = { nav.backOr(Screen.CrossAnalysis) }
            )
        }

        Screen.CrossAnalysisResults -> {
            CrossConsultationResultsView(
                viewModel = models.crossAnalysisViewModel,
                onNavigateBack = { nav.backOr(Screen.CrossAnalysis) },
                modifier = Modifier.fillMaxSize()
            )
        }

        Screen.BackupRestore -> {
            val backupVm = models.backupRestoreViewModel
            if (backupVm != null) {
                BackupRestoreView(
                    viewModel = backupVm,
                    onBack = { nav.backOr(Screen.Settings) }
                )
            } else {
                nav.replace(Screen.Settings)
            }
        }

        Screen.LegacyMigration -> {
            LegacyMigrationView(
                viewModel = models.legacyMigrationViewModel,
                onNavigateBack = { nav.backOr(Screen.Settings) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

