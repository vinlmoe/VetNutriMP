package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.AuthState
import fr.vetbrain.vetnutri_mp.Data.SyncResult
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.ViewModel.AuthViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SyncViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun SyncView(
    authViewModel: AuthViewModel,
    syncViewModel: SyncViewModel,
    onNavigateBack: () -> Unit
) {
    val authState  by authViewModel.state.collectAsState()
    val syncResult by syncViewModel.syncResult.collectAsState()
    val syncConfig by syncViewModel.syncConfig.collectAsState()
    val remoteManifest by syncViewModel.remoteManifest.collectAsState()
    val isLoading  by syncViewModel.isLoading.collectAsState()

    var showConflictDialog by remember { mutableStateOf(false) }
    var showSchemaDialog   by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) syncViewModel.loadConfig()
    }

    LaunchedEffect(syncResult) {
        when (syncResult) {
            is SyncResult.ConflictDetected  -> showConflictDialog = true
            is SyncResult.SchemaIncompatible -> showSchemaDialog = true
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Synchronisation") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                backgroundColor = VetNutriColors.Primary,
                contentColor = androidx.compose.ui.graphics.Color.White
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (authState) {
                is AuthState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is AuthState.Unauthenticated, is AuthState.Error -> {
                    LoginPanel(
                        authState  = authState,
                        onSignIn   = authViewModel::signIn,
                        onSignUp   = authViewModel::signUp
                    )
                }
                is AuthState.Authenticated -> {
                    SyncPanel(
                        auth       = authState as AuthState.Authenticated,
                        config     = syncConfig,
                        remoteManifest = remoteManifest,
                        isLoading  = isLoading,
                        syncResult = syncResult,
                        onSignOut  = authViewModel::signOut,
                        onPush     = syncViewModel::push,
                        onCheck    = syncViewModel::checkRemoteManifest,
                        onPull     = { syncViewModel.pull(false) },
                        onSaveName = syncViewModel::saveDeviceName,
                        onClearResult = syncViewModel::clearResult
                    )
                }
            }
        }
    }

    if (showConflictDialog) {
        val conflict = syncResult as? SyncResult.ConflictDetected
        AlertDialog(
            onDismissRequest = {
                showConflictDialog = false
                syncViewModel.clearResult()
            },
            title   = { Text("Conflit détecté") },
            text    = {
                Text(
                    "L'appareil « ${conflict?.remoteDeviceName} » a poussé des données plus récentes " +
                    "(${conflict?.remoteLastPushMs?.let { formatEpochMs(it) }}).\n\n" +
                    "Vous avez aussi poussé depuis cet appareil (${conflict?.localLastPushMs?.let { formatEpochMs(it) }}).\n\n" +
                    "Écraser vos données locales avec la version distante ?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConflictDialog = false
                    syncViewModel.clearResult()
                    syncViewModel.pull(forceOverwrite = true)
                }) { Text("Écraser", color = VetNutriColors.Error) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConflictDialog = false
                    syncViewModel.clearResult()
                }) { Text("Annuler") }
            }
        )
    }

    if (showSchemaDialog) {
        val schema = syncResult as? SyncResult.SchemaIncompatible
        AlertDialog(
            onDismissRequest = {
                showSchemaDialog = false
                syncViewModel.clearResult()
            },
            title = { Text("Mise à jour requise") },
            text  = {
                Text(
                    "La version de base de données distante (${schema?.remote}) est incompatible " +
                    "avec cette version de l'application (${schema?.local}).\n\n" +
                    "Mettez à jour l'application sur tous les appareils avant de synchroniser."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSchemaDialog = false
                    syncViewModel.clearResult()
                }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun LoginPanel(
    authState: AuthState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit
) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CloudSync,
            contentDescription = null,
            tint = VetNutriColors.Primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Synchronisation cloud",
            style = MaterialTheme.typography.h6,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Connectez-vous pour synchroniser vos données entre appareils.",
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (authState is AuthState.Error) {
            Card(
                backgroundColor = VetNutriColors.Error.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Text(
                    authState.message,
                    color = VetNutriColors.Error,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onSignIn(email, password) },
            enabled = email.isNotBlank() && password.isNotBlank(),
            colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = androidx.compose.ui.graphics.Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Connexion", color = androidx.compose.ui.graphics.Color.White)
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { onSignUp(email, password) },
            enabled = email.isNotBlank() && password.length >= 6 && authState !is AuthState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Créer un compte")
        }
    }
}

@Composable
private fun SyncPanel(
    auth: AuthState.Authenticated,
    config: fr.vetbrain.vetnutri_mp.Data.SyncConfig?,
    remoteManifest: fr.vetbrain.vetnutri_mp.Data.SyncManifest?,
    isLoading: Boolean,
    syncResult: SyncResult?,
    onSignOut: () -> Unit,
    onPush: () -> Unit,
    onCheck: () -> Unit,
    onPull: () -> Unit,
    onSaveName: (String) -> Unit,
    onClearResult: () -> Unit
) {
    var deviceName by remember(config?.deviceName) { mutableStateOf(config?.deviceName ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Compte connecté
        Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Connecté", style = MaterialTheme.typography.caption)
                    Text(auth.email, fontWeight = FontWeight.Medium)
                }
                TextButton(onClick = onSignOut) { Text("Déconnexion") }
            }
        }

        // Nom de l'appareil
        OutlinedTextField(
            value = deviceName,
            onValueChange = { deviceName = it },
            label = { Text("Nom de cet appareil") },
            placeholder = { Text("ex. Desktop Cabinet, iPad Urgences") },
            singleLine = true,
            trailingIcon = {
                if (deviceName != (config?.deviceName ?: "")) {
                    IconButton(onClick = { onSaveName(deviceName) }) {
                        Icon(Icons.Default.Check, contentDescription = "Enregistrer")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Divider()

        // Boutons de synchronisation
        Button(
            onClick = onPush,
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(backgroundColor = VetNutriColors.Primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = androidx.compose.ui.graphics.Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
            } else {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text("Pousser vers le cloud", color = androidx.compose.ui.graphics.Color.White)
        }

        OutlinedButton(
            onClick = onCheck,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Vérifier les mises à jour")
        }

        // Info manifest distant
        if (remoteManifest != null) {
            Card(
                backgroundColor = VetNutriColors.Primary.copy(alpha = 0.07f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Version distante", fontWeight = FontWeight.SemiBold)
                    Text("Appareil : ${remoteManifest.deviceName}")
                    Text("Poussé le : ${formatEpochMs(remoteManifest.pushedAtMs)}")
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = onPull,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tirer ces données")
                    }
                }
            }
        }

        // Timestamps locaux
        if (config != null && (config.lastPushMs > 0 || config.lastPullMs > 0)) {
            Divider()
            if (config.lastPushMs > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f))
                    Text("Dernier push : ${formatEpochMs(config.lastPushMs)}", style = MaterialTheme.typography.caption)
                }
            }
            if (config.lastPullMs > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f))
                    Text("Dernier pull : ${formatEpochMs(config.lastPullMs)}", style = MaterialTheme.typography.caption)
                }
            }
        }

        // Résultat de la dernière opération
        when (val result = syncResult) {
            is SyncResult.PushSuccess -> {
                SuccessCard("Données poussées le ${formatEpochMs(result.pushedAtMs)}", onClearResult)
            }
            is SyncResult.PullSuccess -> {
                SuccessCard(
                    "Données importées depuis « ${result.fromDevice} » — " +
                    "${result.animalsImported} animaux, ${result.foodsImported} aliments",
                    onClearResult
                )
            }
            is SyncResult.AlreadyUpToDate -> {
                SuccessCard("Déjà à jour — aucune donnée plus récente dans le cloud.", onClearResult)
            }
            is SyncResult.Error -> {
                ErrorCard(result.message, onClearResult)
            }
            else -> {}
        }
    }
}

@Composable
private fun SuccessCard(message: String, onDismiss: () -> Unit) {
    Card(
        backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("✅ $message", style = MaterialTheme.typography.body2, modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Fermer", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String, onDismiss: () -> Unit) {
    Card(
        backgroundColor = VetNutriColors.Error.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("❌ $message", style = MaterialTheme.typography.body2, modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Fermer", modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun formatEpochMs(ms: Long): String {
    return try {
        val instant = Instant.fromEpochMilliseconds(ms)
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "%02d/%02d/%04d %02d:%02d".format(
            local.dayOfMonth, local.monthNumber, local.year,
            local.hour, local.minute
        )
    } catch (_: Exception) {
        ms.toString()
    }
}
