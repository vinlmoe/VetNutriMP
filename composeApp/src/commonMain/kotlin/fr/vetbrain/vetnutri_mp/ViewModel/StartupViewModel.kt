package fr.vetbrain.vetnutri_mp.ViewModel

import androidx.compose.runtime.*
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.ConseilRepository
import fr.vetbrain.vetnutri_mp.Utils.DatabaseChangeNotifier
import fr.vetbrain.vetnutri_mp.Utils.DatabaseVersionManager
import fr.vetbrain.vetnutri_mp.Utils.TermsAcceptanceStorage
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers

/**
 * ViewModel optimisé pour l'écran de démarrage
 * Centralise la gestion des états pour améliorer les performances iOS
 */
@Stable
class StartupViewModel(
    private val settingsViewModel: SettingsViewModel,
    private val referenceRepository: DatabaseReferenceEvRepository?,
    private val conseilRepository: ConseilRepository?
) {
    
    // États centralisés avec optimisation iOS
    private val _uiState = mutableStateOf(StartupUiState())
    val uiState: State<StartupUiState> = _uiState
    
    // Gestionnaires optimisés
    private val termsStorage = TermsAcceptanceStorage()
    private val databaseVersionManager = DatabaseVersionManager()
    
    // Coroutine scope pour les opérations asynchrones
    private val coroutineScope = CoroutineScope(SupervisorJob() + AppDispatchers.Main)
    
    init {
        initializeStartup()
        observeDatabaseChanges()
    }
    
    /**
     * Initialise l'écran de démarrage de manière optimisée
     */
    private fun initializeStartup() {
        coroutineScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isCheckingDatabase = true)
                
                // Chargement optimisé des données
                val databaseStatus = loadDatabaseStatus()
                val hasAcceptedTerms = termsStorage.checkTermsAcceptance()
                val currentVersion = databaseVersionManager.getCurrentDatabaseVersion()
                val lastUpdate = databaseVersionManager.getLastUpdateDate()
                
                _uiState.value = _uiState.value.copy(
                    isCheckingDatabase = false,
                    databaseStatus = databaseStatus,
                    hasAcceptedTerms = hasAcceptedTerms,
                    currentDatabaseVersion = currentVersion,
                    lastUpdateDate = lastUpdate
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCheckingDatabase = false,
                    error = e.message ?: "Erreur inconnue"
                )
            }
        }
    }
    
    /**
     * Charge le statut de la base de données de manière optimisée
     */
    private suspend fun loadDatabaseStatus(): DatabaseStatus {
        val foodCount = settingsViewModel.foodRepository.getFoodsCount()
        val referenceCount = referenceRepository?.getAllReferenceEv()?.size ?: 0
        val conseilsCount = try {
            conseilRepository?.getConseilsCount()?.getOrThrow() ?: 0
        } catch (e: Exception) {
            0
        }
        
        return DatabaseStatus(
            foodCount = foodCount,
            referenceCount = referenceCount,
            conseilsCount = conseilsCount,
            needsUpdate = foodCount == 0 || referenceCount == 0
        )
    }
    
    /**
     * Observe les changements de base de données de manière optimisée
     */
    private fun observeDatabaseChanges() {
        coroutineScope.launch {
            DatabaseChangeNotifier.changeEvents.collect { event ->
                event?.let { changeEvent ->
                    when (changeEvent.type) {
                        DatabaseChangeNotifier.ChangeType.FOOD_IMPORTED,
                        DatabaseChangeNotifier.ChangeType.ANIMAL_IMPORTED,
                        DatabaseChangeNotifier.ChangeType.REFERENCE_IMPORTED -> {
                            updateDatabaseStatus()
                        }
                        DatabaseChangeNotifier.ChangeType.DATABASE_VERSION_UPDATED -> {
                            updateVersionInfo()
                        }
                        else -> {
                            // Autres types de changements
                        }
                    }
                    DatabaseChangeNotifier.clearLastEvent()
                }
            }
        }
    }
    
    /**
     * Met à jour le statut de la base de données
     */
    private suspend fun updateDatabaseStatus() {
        val newStatus = loadDatabaseStatus()
        _uiState.value = _uiState.value.copy(databaseStatus = newStatus)
    }
    
    /**
     * Met à jour les informations de version
     */
    private fun updateVersionInfo() {
        coroutineScope.launch {
            _uiState.value = _uiState.value.copy(
                currentDatabaseVersion = databaseVersionManager.getCurrentDatabaseVersion(),
                lastUpdateDate = databaseVersionManager.getLastUpdateDate()
            )
        }
    }
    
    /**
     * Actions utilisateur optimisées
     */
    fun acceptTerms() {
        coroutineScope.launch {
            termsStorage.acceptTerms()
            _uiState.value = _uiState.value.copy(hasAcceptedTerms = true)
        }
    }
    
    fun showUpdateDialog() {
        _uiState.value = _uiState.value.copy(showUpdateDialog = true)
    }
    
    fun hideUpdateDialog() {
        _uiState.value = _uiState.value.copy(showUpdateDialog = false)
    }
    
    fun showTermsDialog() {
        _uiState.value = _uiState.value.copy(showTermsDialog = true)
    }
    
    fun hideTermsDialog() {
        _uiState.value = _uiState.value.copy(showTermsDialog = false)
    }
    
    fun startDatabaseUpdate() {
        _uiState.value = _uiState.value.copy(isUpdatingDatabase = true)
    }
    
    fun finishDatabaseUpdate() {
        _uiState.value = _uiState.value.copy(isUpdatingDatabase = false)
    }
    
    fun hideStartupScreen() {
        _uiState.value = _uiState.value.copy(showStartupScreen = false)
    }
    
    fun hideJsonUpdateDialog() {
        _uiState.value = _uiState.value.copy(showJsonUpdateDialog = false)
    }
}

/**
 * État centralisé optimisé pour iOS
 */
@Stable
data class StartupUiState(
    val showStartupScreen: Boolean = true,
    val isCheckingDatabase: Boolean = true,
    val isUpdatingDatabase: Boolean = false,
    val databaseStatus: DatabaseStatus? = null,
    val hasAcceptedTerms: Boolean = false,
    val showUpdateDialog: Boolean = false,
    val showTermsDialog: Boolean = false,
    val showVersionUpdateDialog: Boolean = false,
    val showJsonUpdateDialog: Boolean = false,
    val currentDatabaseVersion: String = "1.0.0",
    val lastUpdateDate: String? = null,
    val newVersionAvailable: String? = null,
    val currentJsonVersion: String? = null,
    val embeddedJsonVersion: String? = null,
    val jsonUpdateAvailable: Boolean = false,
    val showUpdateButtonByDefault: Boolean = false,
    val error: String? = null
)

/**
 * État de la base de données optimisé
 */
@Stable
data class DatabaseStatus(
    val foodCount: Int,
    val referenceCount: Int,
    val conseilsCount: Int = 0,
    val needsUpdate: Boolean,
    val error: String? = null
)
