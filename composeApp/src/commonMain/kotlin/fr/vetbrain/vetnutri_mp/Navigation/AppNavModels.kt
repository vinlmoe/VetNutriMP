package fr.vetbrain.vetnutri_mp.Navigation

import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalExportViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.BackupRestoreViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.BiblioRefViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.BulkAddBiblioToFoodsViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.BulkReferenceEditorViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ConseilsViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.CreateAnimalViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.CrossConsultationAnalysisViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.EquationViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ExamGradingViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.FoodEditViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.FoodListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ImportViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.NewReferenceEvViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.NutrientRefViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.PreferencesViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.RecipeDialogViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ReferenceEvViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.LegacyMigrationViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.MultiRationViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel

/**
 * ViewModels partagés par les écrans de `AppNavHost` (créés dans `App.kt`).
 * Les écrans ne reçoivent plus de repository : l'ancien `AppNavRepositories` a été supprimé ;
 * seul `equationRepository` reste transmis explicitement par `App.kt` à `AppNavHost` pour les
 * calculs nutritionnels des vues d'analyse (voir `AppNavHost`).
 */
internal data class AppNavModels(
    val animalListViewModel: AnimalListViewModel,
    val animalDetailViewModel: AnimalDetailViewModel,
    val animalExportViewModel: AnimalExportViewModel,
    val recipeDialogViewModel: RecipeDialogViewModel,
    val createAnimalViewModel: CreateAnimalViewModel,
    val settingsViewModel: SettingsViewModel,
    val preferencesViewModel: PreferencesViewModel,
    val foodListViewModel: FoodListViewModel,
    val foodEditViewModel: FoodEditViewModel,
    val biblioRefViewModel: BiblioRefViewModel,
    val referenceEvViewModel: ReferenceEvViewModel,
    val newReferenceEvViewModel: NewReferenceEvViewModel,
    val nutrientRefViewModel: NutrientRefViewModel,
    val equationViewModel: EquationViewModel,
    val conseilsViewModel: ConseilsViewModel,
    val importViewModel: ImportViewModel,
    val examGradingViewModel: ExamGradingViewModel,
    val crossAnalysisViewModel: CrossConsultationAnalysisViewModel,
    val bulkReferenceEditorViewModel: BulkReferenceEditorViewModel,
    val backupRestoreViewModel: BackupRestoreViewModel?,
    val legacyMigrationViewModel: LegacyMigrationViewModel,
    val bulkAddBiblioToFoodsViewModel: BulkAddBiblioToFoodsViewModel,
    val multiRationViewModel: MultiRationViewModel
)
