package fr.vetbrain.vetnutri_mp

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBar
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.BiblioRef
import fr.vetbrain.vetnutri_mp.DataBase.AppDatabase
import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Localization.LocalizationManager
import fr.vetbrain.vetnutri_mp.Repository.*
import fr.vetbrain.vetnutri_mp.Theme.VetNutriTheme
import fr.vetbrain.vetnutri_mp.View.*
import fr.vetbrain.vetnutri_mp.ViewModel.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking

// Fonctions d'importation de fichiers - implémentées par plateforme spécifique
expect fun importAnimalsFromFile(viewModel: AnimalListViewModel)

expect fun importFoodsFromFile(viewModel: SettingsViewModel)

/**
 * Version hybride du repository pour les références bibliographiques qui combine la persistance en
 * base de données et des références de test préremplies pour débogage
 */
class HybridBiblioRefRepository(private val databaseRepo: DatabaseBiblioRefRepository) :
        BiblioRefRepository {
    // Initialiser avec quelques références de test
    private val testRefs =
            listOf(
                    BiblioRef(
                            uuid = "test-1",
                            firstAuthor = "Dupont",
                            year = 2020,
                            completeRef = "Dupont et al., Etude sur les nutriments, 2020",
                            comments = "Étude importante",
                            consistent = 1
                    ),
                    BiblioRef(
                            uuid = "test-2",
                            firstAuthor = "Martin",
                            year = 2021,
                            completeRef = "Martin J., Nutrition canine, 2021",
                            comments = "À vérifier",
                            consistent = 1
                    )
            )

    private val _biblioRefs = MutableStateFlow<List<BiblioRef>>(testRefs)

    init {
        // Ajouter les références de test à la base de données
        runBlocking {
            for (ref in testRefs) {
                databaseRepo.insertBiblioRef(ref)
            }
        }
    }

    override suspend fun getBiblioRefById(uuid: String): BiblioRef? {
        // D'abord chercher dans la base de données
        val dbRef = databaseRepo.getBiblioRefById(uuid)
        if (dbRef != null) return dbRef

        // Sinon chercher dans les références de test
        return _biblioRefs.value.find { it.uuid == uuid }
    }

    override fun getAllBiblioRefs(): Flow<List<BiblioRef>> {
        // Combiner les données de la base et les références de test
        return databaseRepo.getAllBiblioRefs()
    }

    override suspend fun insertBiblioRef(biblioRef: BiblioRef) {
        // Insérer dans la base de données
        databaseRepo.insertBiblioRef(biblioRef)

        // Aussi mettre à jour notre cache local pour être sûr
        val newList = _biblioRefs.value.toMutableList()
        val existingIndex = newList.indexOfFirst { it.uuid == biblioRef.uuid }

        if (existingIndex >= 0) {
            newList[existingIndex] = biblioRef
        } else {
            newList.add(biblioRef)
        }

        _biblioRefs.value = newList
    }

    override suspend fun updateBiblioRef(biblioRef: BiblioRef) {
        databaseRepo.updateBiblioRef(biblioRef)

        // Aussi mettre à jour notre cache local
        val newList = _biblioRefs.value.toMutableList()
        val existingIndex = newList.indexOfFirst { it.uuid == biblioRef.uuid }

        if (existingIndex >= 0) {
            newList[existingIndex] = biblioRef
        }

        _biblioRefs.value = newList
    }

    override suspend fun deleteBiblioRef(biblioRef: BiblioRef) {
        databaseRepo.deleteBiblioRef(biblioRef)

        // Aussi mettre à jour notre cache local
        _biblioRefs.value = _biblioRefs.value.filter { it.uuid != biblioRef.uuid }
    }
}

@Composable
fun App(appDatabase: AppDatabase) {
    // Initialisation de la localisation
    LocalizationManager.initialize()

    // Création des repositories avec la base de données
    val animalRepository = remember {
        DatabaseAnimalRepository(
                        appDatabase.animalDao(),
                        appDatabase.foodDao(),
                        appDatabase.nutrientValueDao()
                )
                .apply {
                    // Ajout de quelques animaux de test uniquement si la base est vide
                    runBlocking {
                        if (getAllAnimals().isEmpty()) {
                            saveAnimal(AnimalEv.createTestAnimal())
                            saveAnimal(
                                    AnimalEv.createTestAnimal()
                                            .copy(
                                                    nom = "Felix",
                                                    specieId = Espece.CHAT.name,
                                            )
                            )
                        }
                    }
                }
    }

    // Création du repository pour les aliments
    val foodRepository = remember {
        DatabaseFoodRepository(appDatabase.foodDao(), appDatabase.nutrientValueDao())
    }

    // Initialiser le repository statique AlimentRepository avec le DatabaseFoodRepository
    AlimentRepository.initializeDatabaseFoodRepository(foodRepository)

    val consultationRepository = remember {
        DatabaseConsultationRepository(appDatabase.consultationDao(), foodRepository)
    }

    // Création du repository pour les références bibliographiques - version hybride
    val databaseBiblioRefRepo = remember { DatabaseBiblioRefRepository(appDatabase.biblioRefDao()) }
    val biblioRefRepository = remember { HybridBiblioRefRepository(databaseBiblioRefRepo) }

    // Création du repository pour les équations (en mémoire pour l'instant)
    val equationRepository = remember { InMemoryEquationRepository() }

    // Création des ViewModels
    val animalListViewModel = remember { AnimalListViewModel(animalRepository) }
    val animalDetailViewModel = remember {
        AnimalDetailViewModel(consultationRepository, animalRepository)
    }
    val createAnimalViewModel = remember { CreateAnimalViewModel(animalRepository) }
    val settingsViewModel = remember { SettingsViewModel(animalRepository, foodRepository) }
    val foodListViewModel = remember { FoodListViewModel(foodRepository) }
    var selectedFoodUuid by remember { mutableStateOf<String?>(null) }

    // ViewModel et état pour les références bibliographiques
    val biblioRefViewModel = remember { BiblioRefViewModel(biblioRefRepository) }
    var selectedBiblioRefId by remember { mutableStateOf<String?>(null) }

    // ViewModel et état pour les équations
    val equationViewModel = remember { EquationViewModel(equationRepository, biblioRefRepository) }
    var selectedEquationId by remember { mutableStateOf<String?>(null) }

    // Création des view models en fonction des besoins de la navigation
    val foodEditViewModel by
            remember(selectedFoodUuid) {
                mutableStateOf(
                        FoodEditViewModel(
                                alimentRepository = AlimentRepository(foodRepository),
                                alimentUuid = selectedFoodUuid
                        )
                )
            }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }
    var selectedAnimal by remember { mutableStateOf<AnimalEv?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    // Observer le résultat de l'importation
    val importResult = animalListViewModel.importResult.collectAsState().value
    var showImportResult by remember { mutableStateOf(false) }

    // Observer le résultat de l'importation des aliments
    val foodImportResult = settingsViewModel.importResult.collectAsState().value
    var showFoodImportResult by remember { mutableStateOf(false) }

    LaunchedEffect(importResult) {
        if (importResult != null) {
            showImportResult = true
        }
    }

    LaunchedEffect(foodImportResult) {
        if (foodImportResult != null) {
            showFoodImportResult = true
        }
    }

    // Effet pour recharger la liste des animaux lorsque l'utilisateur revient à l'écran de liste
    LaunchedEffect(currentScreen) {
        if (currentScreen == Screen.List) {
            animalListViewModel.loadAnimals()
        } else if (currentScreen == Screen.FoodList) {
            // Toujours recharger explicitement la liste des aliments quand on affiche la liste
            println("DEBUG App: Rechargement des aliments lors du changement d'écran vers FoodList")
            foodListViewModel.loadFoods()
        } else if (currentScreen == Screen.EquationList) {
            // Recharger la liste des équations
            equationViewModel.loadEquations()
        }
    }

    // Ajouter un effet pour recharger explicitement la liste des aliments après modification
    LaunchedEffect(selectedFoodUuid) {
        // Si on revient de l'écran d'édition (selectedFoodUuid devient null après avoir été
        // non-null)
        if (selectedFoodUuid == null && currentScreen == Screen.FoodList) {
            // Recharger explicitement la liste des aliments
            println(
                    "DEBUG App: Rechargement des aliments après édition (selectedFoodUuid devient null)"
            )
            foodListViewModel.loadFoods()
        }
    }

    // Fonction locale pour importer les animaux
    val handleImportAnimals: () -> Unit = {
        // Utiliser la méthode du ViewModel pour éviter l'ambiguïté
        animalListViewModel.importAnimalsFromFileUI()
    }

    // Fonction locale pour importer les aliments
    val handleImportFoods: () -> Unit = {
        // Utiliser la méthode du ViewModel pour éviter l'ambiguïté
        settingsViewModel.importFoodsFromFileUI()
    }

    VetNutriTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                when (currentScreen) {
                    Screen.List -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TopBar(
                                    title = "Liste des animaux",
                                    onSettingsClick = { showSettings = true }
                            )

                            // Ajout d'un LaunchedEffect pour recharger la liste lorsque l'écran
                            // devient visible
                            LaunchedEffect(currentScreen) {
                                if (currentScreen == Screen.List) {
                                    animalListViewModel.loadAnimals()
                                }
                            }

                            AnimalListView(
                                    viewModel = animalListViewModel,
                                    onAddAnimal = {
                                        isEditing = false
                                        selectedAnimal = null
                                        createAnimalViewModel.resetAnimal()
                                        currentScreen = Screen.Create
                                    },
                                    onSelectAnimal = { animal: AnimalEv ->
                                        selectedAnimal = animal
                                        animalDetailViewModel.setAnimal(animal)
                                        currentScreen = Screen.Detail
                                    },
                                    onEditAnimal = { animal: AnimalEv ->
                                        selectedAnimal = animal
                                        createAnimalViewModel.updateAnimal(animal)
                                        isEditing = true
                                        currentScreen = Screen.Create
                                    },
                                    onImportAnimals = handleImportAnimals,
                                    onImportFoods = handleImportFoods,
                                    onShowFoodList = { currentScreen = Screen.FoodList },
                                    onShowBiblioRefs = { currentScreen = Screen.BiblioRefList },
                                    onShowEquations = { currentScreen = Screen.EquationList },
                                    modifier = Modifier.fillMaxWidth().weight(1f)
                            )
                        }
                    }
                    Screen.Create -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TopBar(
                                    title =
                                            if (isEditing) "Modifier un animal"
                                            else "Ajouter un animal",
                                    onSettingsClick = { showSettings = true }
                            )
                            CreateAnimalView(
                                    viewModel = createAnimalViewModel,
                                    onNavigateBack = {
                                        isEditing = false
                                        selectedAnimal = null
                                        currentScreen = Screen.List
                                    },
                                    isEditing = isEditing,
                                    modifier = Modifier.fillMaxWidth().weight(1f)
                            )
                        }
                    }
                    Screen.Detail -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            AnimalDetailView(
                                    viewModel = animalDetailViewModel,
                                    settingsViewModel = settingsViewModel,
                                    onNavigateBack = { currentScreen = Screen.List },
                                    onOpenSettings = { showSettings = true },
                                    modifier = Modifier.fillMaxWidth().weight(1f)
                            )
                        }
                    }
                    Screen.FoodList -> {
                        FoodListView(
                                viewModel = foodListViewModel,
                                onNavigateBack = { currentScreen = Screen.List },
                                onOpenSettings = { showSettings = true },
                                onEditFood = { foodUuid ->
                                    selectedFoodUuid = foodUuid
                                    currentScreen = Screen.FoodEdit
                                },
                                onCreateFood = {
                                    selectedFoodUuid = null
                                    currentScreen = Screen.FoodEdit
                                },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    Screen.FoodEdit -> {
                        FoodEditView(
                                viewModel = foodEditViewModel,
                                onNavigateBack = {
                                    selectedFoodUuid = null
                                    currentScreen = Screen.FoodList
                                },
                                onNavigateToSettings = { showSettings = true },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    Screen.BiblioRefList -> {
                        BiblioRefListView(
                                viewModel = biblioRefViewModel,
                                onNavigateBack = { currentScreen = Screen.List },
                                onEditBiblioRef = { biblioRefId ->
                                    selectedBiblioRefId = biblioRefId
                                    currentScreen = Screen.BiblioRefEdit
                                },
                                onCreateBiblioRef = {
                                    selectedBiblioRefId = null
                                    currentScreen = Screen.BiblioRefEdit
                                },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    Screen.BiblioRefEdit -> {
                        BiblioRefEditView(
                                viewModel = biblioRefViewModel,
                                biblioRefId = selectedBiblioRefId,
                                onNavigateBack = {
                                    selectedBiblioRefId = null
                                    currentScreen = Screen.BiblioRefList
                                },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    Screen.EquationList -> {
                        EquationListView(
                                viewModel = equationViewModel,
                                onNavigateBack = { currentScreen = Screen.List },
                                onEditEquation = { equationId ->
                                    selectedEquationId = equationId
                                    currentScreen = Screen.EquationEdit
                                },
                                onCreateEquation = {
                                    selectedEquationId = null
                                    currentScreen = Screen.EquationEdit
                                },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                    Screen.EquationEdit -> {
                        EquationEditView(
                                viewModel = equationViewModel,
                                equationId = selectedEquationId,
                                onNavigateBack = {
                                    selectedEquationId = null
                                    currentScreen = Screen.EquationList
                                },
                                modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            if (showSettings) {
                // Afficher SettingsView dans une boîte de dialogue modale
                AlertDialog(
                        onDismissRequest = { showSettings = false },
                        title = { Text("Paramètres") },
                        text = {
                            // Nous utilisons une Box pour contenir SettingsView avec une taille
                            // maximale
                            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)) {
                                // Utiliser le composant SettingsView à l'intérieur de la boîte de
                                // dialogue
                                SettingsView(
                                        viewModel = settingsViewModel,
                                        onImportAnimals = handleImportAnimals,
                                        onBack = { showSettings = false },
                                        onAnimalListRefresh = { animalListViewModel.loadAnimals() },
                                        onFoodListRefresh = { foodListViewModel.loadFoods() }
                                )
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showSettings = false }) { Text("Fermer") }
                        },
                        // Définir une largeur maximale pour la boîte de dialogue
                        modifier = Modifier.widthIn(max = 800.dp)
                )
            }

            // Afficher le résultat de l'importation des animaux
            if (showImportResult && importResult != null) {
                AlertDialog(
                        onDismissRequest = {
                            showImportResult = false
                            animalListViewModel.resetImportResult()
                        },
                        title = { Text("Résultat de l'importation") },
                        text = {
                            when (importResult) {
                                is AnimalListViewModel.ImportResult.Success -> {
                                    Text(
                                            "${importResult.count} animaux ont été importés avec succès."
                                    )
                                }
                                is AnimalListViewModel.ImportResult.Error -> {
                                    Text("Erreur lors de l'importation : ${importResult.message}")
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                    onClick = {
                                        showImportResult = false
                                        animalListViewModel.resetImportResult()
                                    }
                            ) { Text("OK") }
                        }
                )
            }

            // Afficher le résultat de l'importation des aliments
            if (showFoodImportResult && foodImportResult != null) {
                AlertDialog(
                        onDismissRequest = {
                            showFoodImportResult = false
                            settingsViewModel.resetImportResult()
                        },
                        title = { Text("Résultat de l'importation des aliments") },
                        text = {
                            when (foodImportResult) {
                                is SettingsViewModel.ImportResult.Success -> {
                                    Column {
                                        Text(
                                                "${foodImportResult.count} aliments ont été importés avec succès.",
                                                style = MaterialTheme.typography.subtitle1
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Afficher les statistiques détaillées
                                        Text(
                                                "Détails de l'importation:",
                                                style = MaterialTheme.typography.subtitle2
                                        )
                                        Text(
                                                "• ${foodImportResult.importedCount} nouveaux aliments"
                                        )

                                        if (foodImportResult.updatedCount > 0) {
                                            Text(
                                                    "• ${foodImportResult.updatedCount} aliments mis à jour"
                                            )
                                        }

                                        if (foodImportResult.deletedCount > 0) {
                                            Text(
                                                    "• ${foodImportResult.deletedCount} aliments supprimés"
                                            )
                                        }

                                        if (foodImportResult.errorCount > 0) {
                                            Text(
                                                    "• ${foodImportResult.errorCount} erreurs rencontrées",
                                                    color = MaterialTheme.colors.error
                                            )
                                        }

                                        if (foodImportResult.nonResolvedNutrients > 0) {
                                            Text(
                                                    "• ${foodImportResult.nonResolvedNutrients} nutriments non résolus",
                                                    color =
                                                            MaterialTheme.colors.error.copy(
                                                                    alpha = 0.7f
                                                            )
                                            )
                                        }
                                    }
                                }
                                is SettingsViewModel.ImportResult.Error -> {
                                    Text(
                                            "Erreur lors de l'importation : ${foodImportResult.message}"
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                    onClick = {
                                        showFoodImportResult = false
                                        settingsViewModel.resetImportResult()
                                    }
                            ) { Text("OK") }
                        }
                )
            }
        }
    }
}

private sealed class Screen {
    object List : Screen()
    object Create : Screen()
    object Detail : Screen()
    object FoodList : Screen()
    object FoodEdit : Screen()
    object BiblioRefList : Screen()
    object BiblioRefEdit : Screen()
    object EquationList : Screen()
    object EquationEdit : Screen()
}
