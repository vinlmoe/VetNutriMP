package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import fr.vetbrain.vetnutri_mp.DataBase.createPlatformDatabaseModule
import fr.vetbrain.vetnutri_mp.Repository.DatabaseAnimalRepository

/** Fonction utilitaire pour obtenir une instance d'ImportViewModel */
@Composable
fun rememberViewModel(): ImportViewModel {
    val coroutineScope = rememberCoroutineScope()
    val databaseModule = createPlatformDatabaseModule()
    val appDatabase = databaseModule.getDatabase() as fr.vetbrain.vetnutri_mp.DataBase.AppDatabase

    val animalRepository = remember {
        DatabaseAnimalRepository(appDatabase.animalDao(), appDatabase.foodDao())
    }

    return remember { ImportViewModel(animalRepository, coroutineScope) }
}
