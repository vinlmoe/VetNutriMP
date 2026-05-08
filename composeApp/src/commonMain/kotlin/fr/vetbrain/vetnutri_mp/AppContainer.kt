package fr.vetbrain.vetnutri_mp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.ConseilRepository
import fr.vetbrain.vetnutri_mp.Repository.ConsultationRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseBiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseConsultationRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseEquationRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseFoodRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import fr.vetbrain.vetnutri_mp.Repository.RecipeRepository
import fr.vetbrain.vetnutri_mp.Repository.ExamGradingRepository
import fr.vetbrain.vetnutri_mp.Service.FileService
import fr.vetbrain.vetnutri_mp.Service.StartupService
import fr.vetbrain.vetnutri_mp.Utils.createPreferencesStorage

data class AppContainer(
    val animalRepository: DatabaseAnimalRepository,
    val foodRepository: DatabaseFoodRepository,
    val consultationRepository: ConsultationRepository,
    val examGradingRepository: ExamGradingRepository,
    val recipeRepository: RecipeRepository,
    val biblioRefRepository: BiblioRefRepository,
    val equationRepository: EquationRepository,
    val conseilRepository: ConseilRepository,
    val referenceRepository: DatabaseReferenceEvRepository,
    val exportImportRepository: ExportImportRepository,
    val fileService: FileService,
    val startupService: StartupService,
    val preferencesRepository: PreferencesRepository
)

@Composable
fun rememberAppContainer(appDatabase: AppDatabase): AppContainer {
    val animalRepository =
        remember {
            DatabaseAnimalRepository(
                appDatabase.animalDao(),
                appDatabase.foodDao(),
                appDatabase.nutrientValueDao()
            )
        }
    val foodRepository =
        remember { DatabaseFoodRepository(appDatabase.foodDao(), appDatabase.nutrientValueDao(), appDatabase.customNutrientDao()) }
    val consultationRepository =
        remember { DatabaseConsultationRepository(appDatabase.consultationDao(), foodRepository) }
    val examGradingRepository = remember { ExamGradingRepository(appDatabase.examGradingDao()) }
    val recipeRepository = remember { RecipeRepository(appDatabase.recipeDao(), appDatabase.foodDao()) }
    val biblioRefRepository = remember { DatabaseBiblioRefRepository(appDatabase.biblioRefDao()) }
    val equationRepository =
        remember { DatabaseEquationRepository(appDatabase.equationDao(), appDatabase.biblioRefDao()) }
    val conseilRepository = remember { ConseilRepository(appDatabase.htmlSectionDao()) }
    val referenceRepository =
        remember {
            DatabaseReferenceEvRepository(
                appDatabase.referenceEvDao(),
                appDatabase.equationDao(),
                appDatabase.biblioRefDao()
            )
        }
    val exportImportRepository =
        remember {
            ExportImportRepository(
                animalRepository = animalRepository,
                foodRepository = foodRepository,
                equationRepository = equationRepository,
                referenceRepository = referenceRepository,
                biblioRepository = biblioRefRepository,
                consultationRepository = consultationRepository,
                recipeRepository = recipeRepository,
                conseilRepository = conseilRepository
            )
        }
    val fileService = remember { createFileService() }
    val startupService = remember { StartupService(exportImportRepository, fileService) }
    val preferencesRepository = remember { PreferencesRepository(createPreferencesStorage()) }

    return AppContainer(
        animalRepository = animalRepository,
        foodRepository = foodRepository,
        consultationRepository = consultationRepository,
        examGradingRepository = examGradingRepository,
        recipeRepository = recipeRepository,
        biblioRefRepository = biblioRefRepository,
        equationRepository = equationRepository,
        conseilRepository = conseilRepository,
        referenceRepository = referenceRepository,
        exportImportRepository = exportImportRepository,
        fileService = fileService,
        startupService = startupService,
        preferencesRepository = preferencesRepository
    )
}
