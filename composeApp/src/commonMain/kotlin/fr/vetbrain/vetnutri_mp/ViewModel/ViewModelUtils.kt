package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import fr.vetbrain.vetnutri_mp.DataBase.createPlatformDatabaseModule
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseBiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseEquationRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository

/** Fonction utilitaire pour obtenir une instance d'ImportViewModel avec tous les repositories */
@Composable
fun rememberViewModel(): ImportViewModel {
    val coroutineScope = rememberCoroutineScope()
    val databaseModule = createPlatformDatabaseModule()
    val appDatabase = databaseModule.getDatabase() as fr.vetbrain.vetnutri_mp.DataBase.AppDatabase

    // Créer tous les repositories nécessaires
    val animalRepository = remember {
        DatabaseAnimalRepository(appDatabase.animalDao(), appDatabase.foodDao())
    }

    val databaseReferenceEvRepository = remember {
        DatabaseReferenceEvRepository(
                appDatabase.referenceEvDao(),
                appDatabase.equationDao(),
                appDatabase.biblioRefDao()
        )
    }

    val equationRepository = remember {
        DatabaseEquationRepository(appDatabase.equationDao(), appDatabase.biblioRefDao())
    }

    val biblioRefRepository = remember { DatabaseBiblioRefRepository(appDatabase.biblioRefDao()) }

    return remember {
        ImportViewModel(
                animalRepository = animalRepository,
                databaseReferenceEvRepository = databaseReferenceEvRepository,
                equationRepository = equationRepository,
                biblioRefRepository = biblioRefRepository
        )
    }
}
