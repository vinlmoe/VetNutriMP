package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.DatabaseChangeNotifier
import fr.vetbrain.vetnutri_mp.Utils.DatabaseVersionManager
import fr.vetbrain.vetnutri_mp.Utils.TermsAcceptanceStorage
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel.ImportResult
import kotlinx.coroutines.launch

private fun journaliserMiseAJour(message: String): Unit {
        println("[StartupScreen] ${message}")
}

private fun extraireVersionJson(contenu: String): String? {
        val motif: Regex = "\"version\"\\s*:\\s*\"([^\"]+)\"".toRegex()
        return motif.find(contenu)?.groupValues?.getOrNull(1)
}

/**
 * Écran de démarrage qui affiche l'état de la base de données et permet à l'utilisateur de choisir
 * s'il souhaite la mettre à jour
 */
@Composable
fun StartupScreen(
        foodRepository: FoodRepository,
        referenceRepository: DatabaseReferenceEvRepository?,
        settingsViewModel: SettingsViewModel,
        onDatabaseReady: () -> Unit,
        conseilRepository: fr.vetbrain.vetnutri_mp.Repository.ConseilRepository? = null,
        onShowBackupDialog: () -> Unit = {},
        modifier: Modifier = Modifier
) {
        var showStartupScreen by remember { mutableStateOf(true) }
        var isCheckingDatabase by remember { mutableStateOf(true) }
        var isUpdatingDatabase by remember { mutableStateOf(false) }
        var databaseStatus by remember { mutableStateOf<DatabaseStatus?>(null) }
        var showUpdateDialog by remember { mutableStateOf(false) }
        var showTermsDialog by remember { mutableStateOf(false) }

        // Gestionnaire de stockage des CGU
        val termsStorage = remember { TermsAcceptanceStorage() }
        var hasAcceptedTerms by remember { mutableStateOf(false) }

        // Gestionnaire de versions de la base de données
        val databaseVersionManager = remember { DatabaseVersionManager() }
        var currentDatabaseVersion by remember { mutableStateOf("1.0.0") }
        var lastUpdateDate by remember { mutableStateOf<String?>(null) }
        var showVersionUpdateDialog by remember { mutableStateOf(false) }
        var newVersionAvailable by remember { mutableStateOf<String?>(null) }

        // Variable pour afficher le bouton de mise à jour par défaut au démarrage
        // Désactivé par défaut pour minimiser le bouton quand aucune mise à jour n'est requise
        var showUpdateButtonByDefault by remember { mutableStateOf(false) }

        // Informations sur les versions JSON
        var currentJsonVersion by remember { mutableStateOf<String?>(null) }
        var embeddedJsonVersion by remember { mutableStateOf<String?>(null) }
        var jsonUpdateAvailable by remember { mutableStateOf(false) }
        var showJsonUpdateDialog by remember { mutableStateOf(false) }

        val coroutineScope = rememberCoroutineScope()

        // Vérifier l'état de la base de données, des CGU et des versions au démarrage
        LaunchedEffect(Unit) {
                try {
                        val foodCount = foodRepository.getAllFoods().size
                        val referenceCount = referenceRepository?.getAllReferenceEv()?.size ?: 0
                        val conseilsCount = try {
                                conseilRepository?.getConseilsCount()?.getOrThrow() ?: 0
                        } catch (e: Exception) {
                                0
                        }

                        databaseStatus =
                                DatabaseStatus(
                                        foodCount = foodCount,
                                        referenceCount = referenceCount,
                                        conseilsCount = conseilsCount,
                                        needsUpdate = foodCount == 0 || referenceCount == 0
                                )

                        // Vérifier si les CGU ont déjà été acceptées
                        hasAcceptedTerms = termsStorage.checkTermsAcceptance()

                        // Charger les informations de version de la base de données
                        currentDatabaseVersion = databaseVersionManager.getCurrentDatabaseVersion()
                        lastUpdateDate = databaseVersionManager.getLastUpdateDate()

                        // Charger les informations de version JSON
                        currentJsonVersion = databaseVersionManager.getStoredJsonVersion()
                        val versionStockee: String = currentJsonVersion ?: "Aucune"
                        journaliserMiseAJour("Version JSON stockée=" + versionStockee)

                        // Lire et vérifier la version du JSON intégré de manière optimisée
                        try {
                                val resourceReader = fr.vetbrain.vetnutri_mp.Localization.ResourceReader()
                                // Essayer de lire seulement la version d'abord (plus efficace)
                                val localEmbeddedJsonVersion: String? = try {
                                        resourceReader.readJsonVersion("vetnutri_export_init.json")
                                } catch (e: Exception) {
                                        try {
                                                resourceReader.readJsonVersion("data/vetnutri_export_init.json")
                                        } catch (e2: Exception) {
                                                null
                                        }
                                }
                                embeddedJsonVersion = localEmbeddedJsonVersion
                                if (localEmbeddedJsonVersion != null) {
                                        journaliserMiseAJour("Version JSON intégrée=" + localEmbeddedJsonVersion)
                                        val currentStoredVersion: String? = databaseVersionManager.getStoredJsonVersion()
                                        val isUpdate = currentStoredVersion == null || databaseVersionManager.compareVersions(localEmbeddedJsonVersion, currentStoredVersion) > 0
                                        jsonUpdateAvailable = isUpdate
                                        if (jsonUpdateAvailable && !isUpdatingDatabase) {
                                                showJsonUpdateDialog = true
                                                val formattedIntegrated: String = databaseVersionManager.formatVersion(localEmbeddedJsonVersion)
                                                val stored: String = currentStoredVersion ?: "Aucune"
                                                journaliserMiseAJour("Détection nouvelle version JSON intégrée=" + formattedIntegrated + "; version stockée=" + stored + "; déclenchement du popup JSON=true")
                                        }
                                        val stored: String = currentStoredVersion ?: "Aucune"
                                        val integrated: String = localEmbeddedJsonVersion
                                        val triggered: Boolean = jsonUpdateAvailable && !isUpdatingDatabase
                                        journaliserMiseAJour("Version JSON stockée=" + stored + "; Version JSON intégrée=" + integrated + "; popupDéclenché=" + triggered)
                                } else {
                                        // Fallback: lire le contenu complet et extraire la version
                                        val candidats: List<String> = listOf(
                                                "data/vetnutri_export_init.json",
                                                "vetnutri_export_init.json"
                                        )
                                        var versionTrouvee: String? = null
                                        for (nom in candidats) {
                                                try {
                                                        val contenu: String = resourceReader.readResourceOptimized(nom)
                                                        versionTrouvee = extraireVersionJson(contenu)
                                                        if (versionTrouvee != null) {
                                                                embeddedJsonVersion = versionTrouvee
                                                                break
                                                        }
                                                } catch (_: Exception) {
                                                }
                                        }
                                        if (versionTrouvee != null) {
                                                journaliserMiseAJour("Version JSON intégrée (fallback)=" + versionTrouvee)
                                                // Aligner le comportement: recalcul de la mise à jour et popup éventuel
                                                val currentStoredVersion: String? = databaseVersionManager.getStoredJsonVersion()
                                                val isUpdate: Boolean = currentStoredVersion == null || databaseVersionManager.compareVersions(versionTrouvee, currentStoredVersion) > 0
                                                jsonUpdateAvailable = isUpdate
                                                if (jsonUpdateAvailable && !isUpdatingDatabase) {
                                                        showJsonUpdateDialog = true
                                                        val formattedIntegrated: String = databaseVersionManager.formatVersion(versionTrouvee)
                                                        val stored: String = currentStoredVersion ?: "Aucune"
                                                        journaliserMiseAJour("Détection nouvelle version JSON intégrée=" + formattedIntegrated + "; version stockée=" + stored + "; déclenchement du popup JSON=true")
                                                }
                                                val stored: String = currentStoredVersion ?: "Aucune"
                                                val triggered: Boolean = jsonUpdateAvailable && !isUpdatingDatabase
                                                journaliserMiseAJour("Version JSON stockée=" + stored + "; Version JSON intégrée=" + versionTrouvee + "; popupDéclenché=" + triggered)
                                        } else {
                                                journaliserMiseAJour("Version JSON intégrée introuvable")
                                        }
                                }
                        } catch (e: Exception) {
                                // En cas d'erreur, on considère qu'aucune mise à jour n'est
                                // nécessaire
                                jsonUpdateAvailable = false
                                journaliserMiseAJour("Erreur lors de la lecture de la version JSON intégrée: ${e.message}")
                        }

                        // Attendre un peu pour montrer l'écran de démarrage
                        kotlinx.coroutines.delay(2000)
                } catch (e: Exception) {
                        databaseStatus =
                                DatabaseStatus(
                                        foodCount = 0,
                                        referenceCount = 0,
                                        needsUpdate = true,
                                        error = e.message
                                )
                } finally {
                        isCheckingDatabase = false
                }
        }

        // Écouter les changements de base de données pour mettre à jour l'interface
        LaunchedEffect(Unit) {
                DatabaseChangeNotifier.changeEvents.collect { event ->
                        event?.let { changeEvent ->
                                when (changeEvent.type) {
                                        DatabaseChangeNotifier.ChangeType.FOOD_IMPORTED,
                                        DatabaseChangeNotifier.ChangeType.ANIMAL_IMPORTED,
                                        DatabaseChangeNotifier.ChangeType.REFERENCE_IMPORTED -> {
                                                // Mettre à jour le statut de la base de données
                                                try {
                                                        val newFoodCount =
                                                                foodRepository.getAllFoods().size
                                                        val newReferenceCount =
                                                                referenceRepository
                                                                        ?.getAllReferenceEv()
                                                                        ?.size
                                                                        ?: 0

                                                        databaseStatus =
                                                                DatabaseStatus(
                                                                        foodCount = newFoodCount,
                                                                        referenceCount =
                                                                                newReferenceCount,
                                                                        needsUpdate =
                                                                                newFoodCount == 0 ||
                                                                                        newReferenceCount ==
                                                                                                0
                                                                )

                                                        // Si la base est maintenant complète,
                                                        // permettre de continuer
                                                        if (newFoodCount > 0 &&
                                                                        newReferenceCount > 0
                                                        ) {
                                                                // Attendre un peu pour que
                                                                // l'utilisateur voie la mise à jour
                                                                kotlinx.coroutines.delay(1000)
                                                        }
                                                } catch (e: Exception) {
                                                        // Gérer l'erreur silencieusement
                                                }
                                        }
                                        DatabaseChangeNotifier.ChangeType
                                                .DATABASE_VERSION_UPDATED -> {
                                                // Mettre à jour la version affichée
                                                currentDatabaseVersion =
                                                        databaseVersionManager
                                                                .getCurrentDatabaseVersion()
                                                lastUpdateDate =
                                                        databaseVersionManager.getLastUpdateDate()
                                        }
                                        else -> {
                                                // Autres types de changements
                                        }
                                }

                                // Effacer l'événement pour éviter les notifications multiples
                                DatabaseChangeNotifier.clearLastEvent()
                        }
                }
        }

        if (showStartupScreen) {
                Surface(
                        modifier = modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                ) {
                        Column(
                                modifier =
                                        Modifier.fillMaxSize()
                                                .padding(24.dp)
                                                .verticalScroll(rememberScrollState()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                        ) {
                                // Logo et titre
                                Icon(
                                        imageVector = Icons.Default.Storage,
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp),
                                        tint = VetNutriColors.Primary
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                        text = "VetNutri MP",
                                        style = MaterialTheme.typography.h4,
                                        fontWeight = FontWeight.Bold,
                                        color = VetNutriColors.Primary
                                )

                                Text(
                                        text = "Gestionnaire de Nutrition Vétérinaire",
                                        style = MaterialTheme.typography.subtitle1,
                                        color = VetNutriColors.Secondary,
                                        textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Informations sur l'auteur
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        elevation = 2.dp,
                                        backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f)
                                ) {
                                        Column(
                                                modifier = Modifier.padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                                Text(
                                                        text = "Développé par",
                                                        style = MaterialTheme.typography.caption,
                                                        color = VetNutriColors.Secondary
                                                )
                                                Text(
                                                        text = "S. Lefebvre",
                                                        style = MaterialTheme.typography.h6,
                                                        fontWeight = FontWeight.Bold,
                                                        color = VetNutriColors.Primary
                                                )
                                                Text(
                                                        text = "Dr Vétérinaire, PhD, HDR",
                                                        style = MaterialTheme.typography.body2,
                                                        color = VetNutriColors.Secondary,
                                                        textAlign = TextAlign.Center
                                                )
                                                Text(
                                                        text = "Maître de conférence en nutrition",
                                                        style = MaterialTheme.typography.body2,
                                                        color = VetNutriColors.Secondary,
                                                        textAlign = TextAlign.Center
                                                )
                                                Text(
                                                        text = "VetAgro Sup",
                                                        style = MaterialTheme.typography.body2,
                                                        fontWeight = FontWeight.Medium,
                                                        color = VetNutriColors.Primary,
                                                        textAlign = TextAlign.Center
                                                )
                                        }
                                }

                                Spacer(modifier = Modifier.height(32.dp))

                                // État de la base de données
                                if (isCheckingDatabase) {
                                        CircularProgressIndicator(
                                                color = VetNutriColors.Primary,
                                                modifier = Modifier.size(48.dp)
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                                text = "Vérification de la base de données...",
                                                style = MaterialTheme.typography.body1,
                                                textAlign = TextAlign.Center
                                        )
                                } else {
                                        databaseStatus?.let { status ->
                                                DatabaseStatusCard(status = status)

                                                // Informations sur les versions JSON
                                                if (currentJsonVersion != null ||
                                                                embeddedJsonVersion != null
                                                ) {
                                                        Spacer(modifier = Modifier.height(16.dp))

                                                        Card(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                elevation = 2.dp
                                                        ) {
                                                                Column(
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        16.dp
                                                                                )
                                                                ) {
                                                                        Text(
                                                                                text =
                                                                                        "Versions des données",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .subtitle1,
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold,
                                                                                color =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )

                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.height(
                                                                                                8.dp
                                                                                        )
                                                                        )

                                                                        // Version actuelle importée
                                                                        if (currentJsonVersion !=
                                                                                        null
                                                                        ) {
                                                                                Text(
                                                                                        text =
                                                                                                "Version actuelle : ${databaseVersionManager.formatVersion(currentJsonVersion!!)}",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .body2
                                                                                )
                                                                        } else {
                                                                                Text(
                                                                                        text =
                                                                                                "Aucune version importée",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .body2,
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colors
                                                                                                        .onSurface
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.6f
                                                                                                        )
                                                                                )
                                                                        }

                                                                        // Version intégrée
                                                                        if (embeddedJsonVersion !=
                                                                                        null
                                                                        ) {
                                                                                Text(
                                                                                        text =
                                                                                                "Version intégrée : ${databaseVersionManager.formatVersion(embeddedJsonVersion!!)}",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .body2
                                                                                )
                                                                        }

                                                                        // Indicateur de mise à jour
                                                                        // disponible
                                                                        if (jsonUpdateAvailable) {
                                                                                Spacer(
                                                                                        modifier =
                                                                                                Modifier.height(
                                                                                                        8.dp
                                                                                                )
                                                                                )
                                                                                Row(
                                                                                        verticalAlignment =
                                                                                                Alignment
                                                                                                        .CenterVertically,
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth()
                                                                                ) {
                                                                                        Icon(
                                                                                                imageVector =
                                                                                                        Icons.Default
                                                                                                                .Info,
                                                                                                contentDescription =
                                                                                                        null,
                                                                                                tint =
                                                                                                        MaterialTheme
                                                                                                                .colors
                                                                                                                .secondary,
                                                                                                modifier =
                                                                                                        Modifier.size(
                                                                                                                16.dp
                                                                                                        )
                                                                                        )
                                                                                        Spacer(
                                                                                                modifier =
                                                                                                        Modifier.width(
                                                                                                                8.dp
                                                                                                        )
                                                                                        )
                                                                                        Text(
                                                                                                text =
                                                                                                        "Nouvelle version disponible",
                                                                                                style =
                                                                                                        MaterialTheme
                                                                                                                .typography
                                                                                                                .body2,
                                                                                                color =
                                                                                                        MaterialTheme
                                                                                                                .colors
                                                                                                                .secondary,
                                                                                                fontWeight =
                                                                                                        FontWeight
                                                                                                                .Medium
                                                                                        )
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }

                                                Spacer(modifier = Modifier.height(32.dp))

                                                // Boutons d'action
                                                if (status.needsUpdate ||
                                                                jsonUpdateAvailable ||
                                                                showUpdateButtonByDefault
                                                ) {
                                                        Button(
                                                                onClick = {
                                                                        showUpdateDialog = true
                                                                        journaliserMiseAJour("Ouverture du popup de mise à jour de la base (manuel)")
                                                                        // Désactiver l'affichage
                                                                        // par défaut une fois que
                                                                        // l'utilisateur a cliqué
                                                                        showUpdateButtonByDefault =
                                                                                false
                                                                },
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .height(56.dp),
                                                                colors =
                                                                        ButtonDefaults.buttonColors(
                                                                                backgroundColor =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )
                                                        ) {
                                                                Icon(
                                                                        imageVector =
                                                                                Icons.Default
                                                                                        .Refresh,
                                                                        contentDescription = null,
                                                                        modifier =
                                                                                Modifier.size(20.dp)
                                                                )
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.width(8.dp)
                                                                )
                                                                Text(
                                                                        text =
                                                                                if (jsonUpdateAvailable
                                                                                )
                                                                                        "Mettre à jour les données"
                                                                                else
                                                                                        "Mettre à jour la base de données",
                                                                        fontSize = 16.sp
                                                                )
                                                        }

                                                        Spacer(modifier = Modifier.height(16.dp))

                                                        // ⚠️ IMPORTANT : Le bouton "Continuer"
                                                        // n'est affiché que si les CGU
                                                        // sont acceptées
                                                        if (hasAcceptedTerms) {
                                                                OutlinedButton(
                                                                        onClick = {
                                                                                showStartupScreen =
                                                                                        false
                                                                                // Désactiver
                                                                                // l'affichage par
                                                                                // défaut quand
                                                                                // l'utilisateur
                                                                                // choisit de
                                                                                // continuer
                                                                                showUpdateButtonByDefault =
                                                                                        false
                                                                                onDatabaseReady()
                                                                        },
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                                        .height(
                                                                                                48.dp
                                                                                        )
                                                                ) {
                                                                        Text(
                                                                                "Continuer sans mise à jour"
                                                                        )
                                                                }
                                                                
                                                                Spacer(modifier = Modifier.height(8.dp))
                                                                
                                                                // Bouton pour restaurer une sauvegarde
                                                                OutlinedButton(
                                                                        onClick = onShowBackupDialog,
                                                                        modifier = Modifier.fillMaxWidth()
                                                                                .height(48.dp),
                                                                        colors = ButtonDefaults.outlinedButtonColors(
                                                                                contentColor = VetNutriColors.Secondary
                                                                        )
                                                                ) {
                                                                        Icon(
                                                                                imageVector = Icons.Default.Download,
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(18.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(8.dp))
                                                                        Text("Restaurer une sauvegarde")
                                                                }
                                                        }
                                                } else {
                                                        // Base complète : le bouton "Continuer"
                                                        // n'est affiché que si les CGU
                                                        // sont acceptées
                                                        if (hasAcceptedTerms) {
                                                                Button(
                                                                        onClick = {
                                                                                showStartupScreen =
                                                                                        false
                                                                                // Désactiver
                                                                                // l'affichage par
                                                                                // défaut quand
                                                                                // l'utilisateur
                                                                                // choisit de
                                                                                // continuer
                                                                                showUpdateButtonByDefault =
                                                                                        false
                                                                                onDatabaseReady()
                                                                        },
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                                        .height(
                                                                                                56.dp
                                                                                        ),
                                                                        colors =
                                                                                ButtonDefaults
                                                                                        .buttonColors(
                                                                                                backgroundColor =
                                                                                                        VetNutriColors
                                                                                                                .Primary
                                                                                        )
                                                                ) {
                                                                        Text(
                                                                                text = "Continuer",
                                                                                fontSize = 16.sp
                                                                        )
                                                                }
                                                                
                                                                Spacer(modifier = Modifier.height(8.dp))
                                                                
                                                                // Bouton pour restaurer une sauvegarde
                                                                OutlinedButton(
                                                                        onClick = onShowBackupDialog,
                                                                        modifier = Modifier.fillMaxWidth()
                                                                                .height(48.dp),
                                                                        colors = ButtonDefaults.outlinedButtonColors(
                                                                                contentColor = VetNutriColors.Secondary
                                                                        )
                                                                ) {
                                                                        Icon(
                                                                                imageVector = Icons.Default.Download,
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(18.dp)
                                                                        )
                                                                        Spacer(modifier = Modifier.width(8.dp))
                                                                        Text("Restaurer une sauvegarde")
                                                                }
                                                        }
                                                }

                                                // ⚠️ IMPORTANT : Validation des CGU obligatoire à
                                                // chaque démarrage
                                                Spacer(modifier = Modifier.height(16.dp))

                                                if (!hasAcceptedTerms) {
                                                        Card(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                elevation = 2.dp,
                                                                backgroundColor =
                                                                        MaterialTheme.colors.error
                                                                                .copy(alpha = 0.1f)
                                                        ) {
                                                                Column(
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        16.dp
                                                                                ),
                                                                        horizontalAlignment =
                                                                                Alignment
                                                                                        .CenterHorizontally
                                                                ) {
                                                                        Icon(
                                                                                imageVector =
                                                                                        Icons.Default
                                                                                                .Warning,
                                                                                contentDescription =
                                                                                        null,
                                                                                tint =
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .error,
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                24.dp
                                                                                        )
                                                                        )

                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.height(
                                                                                                8.dp
                                                                                        )
                                                                        )

                                                                        Text(
                                                                                text =
                                                                                        "Acceptation des conditions requise",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .subtitle2,
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Bold,
                                                                                color =
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .error,
                                                                                textAlign =
                                                                                        TextAlign
                                                                                                .Center
                                                                        )

                                                                        Text(
                                                                                text =
                                                                                        "Vous devez accepter les conditions générales d'utilisation pour continuer",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .caption,
                                                                                color =
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .onSurface
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.7f
                                                                                                ),
                                                                                textAlign =
                                                                                        TextAlign
                                                                                                .Center
                                                                        )

                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.height(
                                                                                                12.dp
                                                                                        )
                                                                        )

                                                                        Button(
                                                                                onClick = {
                                                                                        showTermsDialog =
                                                                                                true
                                                                                },
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth()
                                                                                                .height(
                                                                                                        40.dp
                                                                                                ),
                                                                                colors =
                                                                                        ButtonDefaults
                                                                                                .buttonColors(
                                                                                                        backgroundColor =
                                                                                                                MaterialTheme
                                                                                                                        .colors
                                                                                                                        .error
                                                                                                )
                                                                        ) {
                                                                                Text(
                                                                                        "Lire et accepter les conditions"
                                                                                )
                                                                        }
                                                                }
                                                        }
                                                } else {
                                                        // CGU acceptées
                                                        Card(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                elevation = 2.dp,
                                                                backgroundColor =
                                                                        MaterialTheme.colors.primary
                                                                                .copy(alpha = 0.1f)
                                                        ) {
                                                                Row(
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        16.dp
                                                                                ),
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically,
                                                                        horizontalArrangement =
                                                                                Arrangement.Center
                                                                ) {
                                                                        Icon(
                                                                                imageVector =
                                                                                        Icons.Default
                                                                                                .Info,
                                                                                contentDescription =
                                                                                        null,
                                                                                tint =
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .primary,
                                                                                modifier =
                                                                                        Modifier.size(
                                                                                                20.dp
                                                                                        )
                                                                        )

                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.width(
                                                                                                8.dp
                                                                                        )
                                                                        )

                                                                        Text(
                                                                                text =
                                                                                        "Conditions générales acceptées",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .body2,
                                                                                color =
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .primary,
                                                                                fontWeight =
                                                                                        FontWeight
                                                                                                .Medium
                                                                        )
                                                                }
                                                        }
                                                }
                                        }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Lien vers les conditions générales
                                TextButton(
                                        onClick = { showTermsDialog = true },
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Text(
                                                text = "Conditions générales d'utilisation",
                                                style = MaterialTheme.typography.caption,
                                                color = VetNutriColors.Secondary
                                        )
                                }
                        }
                }

                // Dialogue automatique de mise à jour JSON (priorité haute)
                if (showJsonUpdateDialog && !isUpdatingDatabase && !showUpdateDialog) {
                        journaliserMiseAJour("Affichage du popup de mise à jour JSON")
                        JsonUpdateDialog(
                                currentJsonVersion = currentJsonVersion,
                                newJsonVersion = embeddedJsonVersion,
                                onConfirm = {
                                        showJsonUpdateDialog = false
                                        journaliserMiseAJour("Confirmation de la mise à jour JSON")
                                        // Désactiver l'affichage par défaut quand l'utilisateur
                                        // confirme la mise à jour JSON
                                        showUpdateButtonByDefault = false
                                        isUpdatingDatabase = true
                                        journaliserMiseAJour("Début d'import JSON (isUpdatingDatabase=true)")

                                        coroutineScope.launch {
                                                try {
                                                        // Lancer la mise à jour automatique des
                                                        // données JSON
                                                        val result =
                                                                settingsViewModel
                                                                        .relaunchAutomaticImport()

                                                        // Mettre à jour le statut
                                                        when (result) {
                                                                is ImportResult.Success -> {
                                                                        databaseStatus =
                                                                                databaseStatus
                                                                                        ?.copy(
                                                                                                foodCount =
                                                                                                        result.count,
                                                                                                referenceCount =
                                                                                                        result.count,
                                                                                                needsUpdate =
                                                                                                        false
                                                                                        )
                                                                        isUpdatingDatabase = false

                                                                        // Recharger les
                                                                        // informations de version
                                                                        // après la mise à jour
                                                                        currentJsonVersion =
                                                                                databaseVersionManager
                                                                                        .getStoredJsonVersion()
                                                                        jsonUpdateAvailable = false
                                                                }
                                                                is ImportResult.Error -> {
                                                                        databaseStatus =
                                                                                databaseStatus
                                                                                        ?.copy(
                                                                                                error =
                                                                                                        "Erreur lors de la mise à jour JSON : ${result.message}"
                                                                                        )
                                                                        isUpdatingDatabase = false
                                                                        return@launch
                                                                }
                                                        }

                                                        // Passer à l'application après la mise à
                                                        // jour
                                                        showStartupScreen = false
                                                        onDatabaseReady()
                                                } catch (e: Exception) {
                                                        // En cas d'erreur, afficher le message et
                                                        // permettre de continuer
                                                        databaseStatus =
                                                                databaseStatus?.copy(
                                                                        error =
                                                                                "Erreur lors de la mise à jour JSON : ${e.message}"
                                                                )
                                                        isUpdatingDatabase = false
                                                }
                                        }
                                },
                                onDismiss = {
                                        showJsonUpdateDialog = false
                                        journaliserMiseAJour("Annulation du popup de mise à jour JSON")
                                        // Désactiver l'affichage par défaut quand l'utilisateur
                                        // rejette la mise à jour JSON
                                        showUpdateButtonByDefault = false
                                        // L'utilisateur peut continuer sans mettre à jour
                                }
                        )
                }

                // Dialogue de confirmation de mise à jour
                if (showUpdateDialog) {
                        journaliserMiseAJour("Affichage du popup de mise à jour de la base")
                        UpdateConfirmationDialog(
                                onConfirm = {
                                        showUpdateDialog = false
                                        journaliserMiseAJour("Confirmation de la mise à jour de la base")
                                        // Désactiver l'affichage par défaut quand l'utilisateur
                                        // confirme la mise à jour
                                        showUpdateButtonByDefault = false
                                        isUpdatingDatabase = true
                                        journaliserMiseAJour("Début d'import base (isUpdatingDatabase=true)")

                                        coroutineScope.launch {
                                                try {
                                                        // Lancer la mise à jour de la base de
                                                        // données (forcée)
                                                        val result =
                                                                settingsViewModel
                                                                        .relaunchAutomaticImport(forceImport = true)

                                                        // Mettre à jour le statut
                                                        when (result) {
                                                                is SettingsViewModel.ImportResult.Success -> {
                                                                        databaseStatus =
                                                                                databaseStatus
                                                                                        ?.copy(
                                                                                                foodCount =
                                                                                                        result.count,
                                                                                                referenceCount =
                                                                                                        result.count,
                                                                                                needsUpdate =
                                                                                                        false
                                                                                        )
                                                                        isUpdatingDatabase = false

                                                                        // Passer à l'application
                                                                        // après la mise à jour
                                                                        showStartupScreen = false
                                                                        onDatabaseReady()
                                                                }
                                                                is SettingsViewModel.ImportResult.Error -> {
                                                                        databaseStatus =
                                                                                databaseStatus
                                                                                        ?.copy(
                                                                                                error =
                                                                                                        "Erreur lors de la mise à jour : ${result.message}"
                                                                                        )
                                                                        isUpdatingDatabase = false
                                                                        return@launch
                                                                }
                                                        }
                                                } catch (e: Exception) {
                                                        // En cas d'erreur, afficher le message et
                                                        // permettre de continuer
                                                        databaseStatus =
                                                                databaseStatus?.copy(
                                                                        error =
                                                                                "Erreur lors de la mise à jour : ${e.message}"
                                                                )
                                                        isUpdatingDatabase = false
                                                }
                                        }
                                },
                                onDismiss = {
                                        showUpdateDialog = false
                                        journaliserMiseAJour("Annulation du popup de mise à jour de la base")
                                        // Désactiver l'affichage par défaut quand l'utilisateur
                                        // annule la mise à jour
                                        showUpdateButtonByDefault = false
                                }
                        )
                }

                // Dialogue des conditions générales
                if (showTermsDialog) {
                        TermsAndConditionsDialog(
                                onAccept = {
                                        coroutineScope.launch {
                                                // Sauvegarder l'acceptation des CGU
                                                termsStorage.acceptTerms()
                                                hasAcceptedTerms = true
                                                showTermsDialog = false
                                        }
                                },
                                onDismiss = { showTermsDialog = false }
                        )
                }

                // Indicateur de progression pendant la mise à jour
                if (isUpdatingDatabase) {
                        Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                        ) {
                                Surface(
                                        modifier = Modifier.padding(32.dp),
                                        color = MaterialTheme.colors.surface,
                                        elevation = 8.dp
                                ) {
                                        Column(
                                                modifier = Modifier.padding(32.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                                CircularProgressIndicator(
                                                        color = VetNutriColors.Primary,
                                                        modifier = Modifier.size(48.dp)
                                                )

                                                Spacer(modifier = Modifier.height(16.dp))

                                                Text(
                                                        text =
                                                                "Mise à jour de la base de données...",
                                                        style = MaterialTheme.typography.h6,
                                                        textAlign = TextAlign.Center
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                        text =
                                                                "Veuillez patienter pendant l'importation des données.",
                                                        style = MaterialTheme.typography.body2,
                                                        textAlign = TextAlign.Center,
                                                        color =
                                                                MaterialTheme.colors.onSurface.copy(
                                                                        alpha = 0.7f
                                                                )
                                                )
                                        }
                                }
                        }
                }
        }
}

/** Carte affichant l'état de la base de données */
@Composable
private fun DatabaseStatusCard(status: DatabaseStatus, modifier: Modifier = Modifier) {
        Card(
                modifier = modifier.fillMaxWidth(),
                elevation = 4.dp,
                backgroundColor =
                        if (status.needsUpdate) {
                                MaterialTheme.colors.error.copy(alpha = 0.1f)
                        } else {
                                MaterialTheme.colors.surface
                        }
        ) {
                Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                        imageVector =
                                                if (status.needsUpdate) Icons.Default.Warning
                                                else Icons.Default.Info,
                                        contentDescription = null,
                                        tint =
                                                if (status.needsUpdate) MaterialTheme.colors.error
                                                else VetNutriColors.Primary
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                        text =
                                                if (status.needsUpdate) "Base de données incomplète"
                                                else "Base de données prête",
                                        style = MaterialTheme.typography.h6,
                                        fontWeight = FontWeight.Bold,
                                        color =
                                                if (status.needsUpdate) MaterialTheme.colors.error
                                                else VetNutriColors.Primary
                                )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Statistiques de la base
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                                StatisticItem(
                                        label = "Aliments",
                                        value = status.foodCount.toString(),
                                        color =
                                                if (status.foodCount > 0) VetNutriColors.Primary
                                                else MaterialTheme.colors.error
                                )

                                StatisticItem(
                                        label = "Références",
                                        value = status.referenceCount.toString(),
                                        color =
                                                if (status.referenceCount > 0)
                                                        VetNutriColors.Primary
                                                else MaterialTheme.colors.error
                                )

                                StatisticItem(
                                        label = "Conseils",
                                        value = status.conseilsCount.toString(),
                                        color = VetNutriColors.Primary
                                )
                        }

                        // Message d'erreur si présent
                        if (status.error != null) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                        text = "⚠️ ${status.error}",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.error,
                                        textAlign = TextAlign.Center
                                )
                        }

                        // Recommandation
                        if (status.needsUpdate) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                        text =
                                                "Il est recommandé de mettre à jour la base de données pour avoir accès à toutes les fonctionnalités.",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                )
                        }
                }
        }
}

/** Élément de statistique */
@Composable
private fun StatisticItem(
        label: String,
        value: String,
        color: Color,
        modifier: Modifier = Modifier
) {
        Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                        text = value,
                        style = MaterialTheme.typography.h4,
                        fontWeight = FontWeight.Bold,
                        color = color
                )

                Text(
                        text = label,
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )
        }
}

/** Dialogue automatique de mise à jour des données JSON */
@Composable
private fun JsonUpdateDialog(
        currentJsonVersion: String?,
        newJsonVersion: String?,
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
) {
        AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colors.primary,
                                        modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                        text = "Nouvelle version des données disponible",
                                        style = MaterialTheme.typography.h6
                                )
                        }
                },
                text = {
                        Column {
                                Text(
                                        text =
                                                "Une nouvelle version du fichier de données a été détectée dans l'application :",
                                        style = MaterialTheme.typography.body1,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Informations sur les versions
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        backgroundColor = MaterialTheme.colors.surface,
                                        elevation = 2.dp
                                ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                        text = "Versions :",
                                                        style = MaterialTheme.typography.subtitle2,
                                                        fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))

                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.SpaceBetween
                                                ) {
                                                        Text(
                                                                text = "Actuelle :",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2
                                                        )
                                                        Text(
                                                                text = currentJsonVersion
                                                                                ?: "Aucune",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2,
                                                                fontWeight = FontWeight.Medium
                                                        )
                                                }

                                                Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement =
                                                                Arrangement.SpaceBetween
                                                ) {
                                                        Text(
                                                                text = "Nouvelle :",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2,
                                                                color = MaterialTheme.colors.primary
                                                        )
                                                        Text(
                                                                text = newJsonVersion ?: "Inconnue",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body2,
                                                                fontWeight = FontWeight.Medium,
                                                                color = MaterialTheme.colors.primary
                                                        )
                                                }
                                        }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                        text =
                                                "Cette mise à jour inclut les dernières données (aliments, références nutritionnelles, etc.) et améliore les fonctionnalités de l'application.",
                                        style = MaterialTheme.typography.body2
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                        text =
                                                "Voulez-vous installer cette mise à jour maintenant ?",
                                        style = MaterialTheme.typography.body2,
                                        fontWeight = FontWeight.Medium
                                )
                        }
                },
                confirmButton = {
                        Button(
                                onClick = onConfirm,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary
                                        )
                        ) {
                                Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Installer la mise à jour")
                        }
                },
                dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Plus tard") } }
        )
}

/** Dialogue de confirmation de mise à jour */
@Composable
private fun UpdateConfirmationDialog(
        onConfirm: () -> Unit,
        onDismiss: () -> Unit,
        isJsonUpdate: Boolean = false,
        currentJsonVersion: String? = null,
        newJsonVersion: String? = null
) {
        val title =
                if (isJsonUpdate) "Mise à jour des données" else "Mise à jour de la base de données"
        val message =
                if (isJsonUpdate) {
                        "Une nouvelle version des données est disponible :\n" +
                                "• Version actuelle : ${currentJsonVersion ?: "Aucune"}\n" +
                                "• Nouvelle version : ${newJsonVersion ?: "Inconnue"}\n\n" +
                                "Cette action va importer la nouvelle version des données. Cela peut prendre quelques instants.\n\n" +
                                "Voulez-vous continuer ?"
                } else {
                        "Cette action va importer les données de base (aliments et références nutritionnelles) " +
                                "depuis le fichier de ressources. Cela peut prendre quelques instants.\n\n" +
                                "Voulez-vous continuer ?"
                }

        AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(text = title, style = MaterialTheme.typography.h6) },
                text = { Text(text = message, style = MaterialTheme.typography.body1) },
                confirmButton = {
                        Button(
                                onClick = onConfirm,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary
                                        )
                        ) { Text("Confirmer") }
                },
                dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Annuler") } }
        )
}

/** Dialogue des conditions générales d'utilisation */
@Composable
private fun TermsAndConditionsDialog(onAccept: () -> Unit, onDismiss: () -> Unit) {
        AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                        Text(
                                text = "Conditions Générales d'Utilisation",
                                style = MaterialTheme.typography.h6,
                                fontWeight = FontWeight.Bold
                        )
                },
                text = {
                        Column {
                                Text(
                                        text =
                                                "VetNutri MP - Gestionnaire de Nutrition Vétérinaire",
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text(
                                        text =
                                                "⚠️ IMPORTANT : Ces conditions doivent être acceptées à CHAQUE démarrage",
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.error,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Text(
                                        text =
                                                "En utilisant ce logiciel, vous acceptez les conditions suivantes :",
                                        style = MaterialTheme.typography.body1,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Text(
                                        text =
                                                "• Ce logiciel est destiné aux professionnels de santé vétérinaire",
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                        text =
                                                "• Les calculs et recommandations sont fournis à titre informatif",
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                        text =
                                                "• La responsabilité de l'utilisateur reste entière dans l'application",
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                        text =
                                                "• Les données saisies restent confidentielles et locales",
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Text(
                                        text =
                                                "Développé par S. Lefebvre, Dr Vétérinaire, PhD, HDR, Maître de conférence en nutrition à VetAgro Sup.",
                                        style = MaterialTheme.typography.caption,
                                        color = VetNutriColors.Secondary,
                                        textAlign = TextAlign.Center
                                )
                        }
                },
                confirmButton = {
                        Button(
                                onClick = onAccept,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary
                                        )
                        ) { Text("J'accepte les conditions") }
                },
                dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Fermer") } }
        )
}

/** État de la base de données */
data class DatabaseStatus(
        val foodCount: Int,
        val referenceCount: Int,
        val conseilsCount: Int = 0,
        val needsUpdate: Boolean,
        val error: String? = null
)
