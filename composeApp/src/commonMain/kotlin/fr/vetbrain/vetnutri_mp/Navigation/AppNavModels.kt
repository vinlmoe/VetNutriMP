package fr.vetbrain.vetnutri_mp.Navigation

import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.ConseilRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import fr.vetbrain.vetnutri_mp.Repository.RecipeRepository
import fr.vetbrain.vetnutri_mp.Utils.PlatformDispatcher
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.BackupRestoreViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.BiblioRefViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.BulkReferenceEditorViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.CreateAnimalViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.CrossConsultationAnalysisViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.EquationViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ExamGradingViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.FoodEditViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.FoodListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ImportViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.NewReferenceEvViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ReferenceEvViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel

internal data class AppNavModels(
    val animalListViewModel: AnimalListViewModel,
    val animalDetailViewModel: AnimalDetailViewModel,
    val createAnimalViewModel: CreateAnimalViewModel,
    val settingsViewModel: SettingsViewModel,
    val foodListViewModel: FoodListViewModel,
    val foodEditViewModel: FoodEditViewModel,
    val biblioRefViewModel: BiblioRefViewModel,
    val referenceEvViewModel: ReferenceEvViewModel,
    val newReferenceEvViewModel: NewReferenceEvViewModel,
    val equationViewModel: EquationViewModel,
    val importViewModel: ImportViewModel,
    val examGradingViewModel: ExamGradingViewModel,
    val crossAnalysisViewModel: CrossConsultationAnalysisViewModel,
    val bulkReferenceEditorViewModel: BulkReferenceEditorViewModel,
    val backupRestoreViewModel: BackupRestoreViewModel?
)

internal data class AppNavRepositories(
    val equationRepository: EquationRepository,
    val recipeRepository: RecipeRepository,
    val conseilRepository: ConseilRepository,
    val biblioRefRepository: BiblioRefRepository,
    val databaseReferenceEvRepository: DatabaseReferenceEvRepository,
    val platformDispatcher: PlatformDispatcher,
    val preferencesRepository: PreferencesRepository
)
