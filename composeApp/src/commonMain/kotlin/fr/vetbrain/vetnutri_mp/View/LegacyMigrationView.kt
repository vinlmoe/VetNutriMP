package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.TopBarSimple
import fr.vetbrain.vetnutri_mp.browseLegacyV2DbFolder
import fr.vetbrain.vetnutri_mp.ViewModel.LegacyMigrationViewModel

@Composable
fun LegacyMigrationView(
    viewModel: LegacyMigrationViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val step by viewModel.step.collectAsState()
    val log by viewModel.log.collectAsState()

    LaunchedEffect(Unit) {
        if (step is LegacyMigrationViewModel.Step.Idle) viewModel.detect()
    }

    Scaffold(
        topBar = {
            TopBarSimple(
                title = "Importer depuis VetNutri 2",
                onNavigateBack = {
                    viewModel.reset()
                    onNavigateBack()
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val s = step) {
                is LegacyMigrationViewModel.Step.Idle,
                is LegacyMigrationViewModel.Step.Detecting -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text("Recherche de VetNutri 2...")
                        }
                    }
                }

                is LegacyMigrationViewModel.Step.NotDetected -> {
                    NotDetectedPanel(
                        onBrowse = {
                            val path = browseLegacyV2DbFolder()
                            if (path != null) viewModel.useCustomPath(path)
                        }
                    )
                }

                is LegacyMigrationViewModel.Step.Detected -> {
                    DetectedPanel(
                        path = s.path,
                        onContinue = { viewModel.loadPreview(s.path) },
                        onBrowse = {
                            val path = browseLegacyV2DbFolder()
                            if (path != null) viewModel.useCustomPath(path)
                        }
                    )
                }

                is LegacyMigrationViewModel.Step.Previewing -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text("Lecture des données...")
                        }
                    }
                }

                is LegacyMigrationViewModel.Step.Preview -> {
                    PreviewPanel(
                        path = s.path,
                        counts = s.counts,
                        onMigrate = { viewModel.startMigration(s.path) },
                        onBack = { viewModel.detect() }
                    )
                }

                is LegacyMigrationViewModel.Step.Migrating -> {
                    MigratingPanel(log = log)
                }

                is LegacyMigrationViewModel.Step.Done -> {
                    ResultPanel(
                        result = s.result,
                        log = log,
                        onClose = {
                            viewModel.reset()
                            onNavigateBack()
                        }
                    )
                }

                is LegacyMigrationViewModel.Step.Error -> {
                    ErrorPanel(
                        message = s.message,
                        onRetry = { viewModel.reset() }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotDetectedPanel(onBrowse: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFF57C00))
                    Spacer(Modifier.width(8.dp))
                    Text("VetNutri 2 non détecté automatiquement", fontWeight = FontWeight.Bold)
                }
                Text(
                    "Sélectionnez manuellement le dossier 'db' de votre installation VetNutri 2 " +
                    "(contient Data-Anim.db).",
                    style = MaterialTheme.typography.body2
                )
            }
        }
        Button(
            onClick = onBrowse,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.FolderOpen, null)
            Spacer(Modifier.width(8.dp))
            Text("Parcourir...")
        }
    }
}

@Composable
private fun DetectedPanel(path: String, onContinue: () -> Unit, onBrowse: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF388E3C))
                    Spacer(Modifier.width(8.dp))
                    Text("VetNutri 2 trouvé", fontWeight = FontWeight.Bold)
                }
                Text(path, style = MaterialTheme.typography.caption)
            }
        }
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("Analyser les données")
        }
        OutlinedButton(onClick = onBrowse, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.FolderOpen, null)
            Spacer(Modifier.width(8.dp))
            Text("Choisir un autre dossier")
        }
    }
}

@Composable
private fun PreviewPanel(
    path: String,
    counts: LegacyMigrationViewModel.MigrationCounts,
    onMigrate: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Données à importer", style = MaterialTheme.typography.h6)
        Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PreviewRow("Animaux", counts.animals)
                PreviewRow("Consultations", counts.consultations)
                PreviewRow("Rations", counts.rations)
                PreviewRow("Pesées", counts.weights)
                PreviewRow("Aliments", counts.foods)
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = Color(0xFFF3E5F5),
            elevation = 0.dp
        ) {
            Text(
                "Les données existantes (même UUID) ne seront pas écrasées.",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.body2
            )
        }
        Button(
            onClick = onMigrate,
            modifier = Modifier.fillMaxWidth(),
            enabled = counts.animals > 0 || counts.foods > 0 || counts.consultations > 0
        ) {
            Text("Importer les données")
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Retour")
        }
    }
}

@Composable
private fun PreviewRow(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text("$count", fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MigratingPanel(log: List<String>) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text("Migration en cours...", fontWeight = FontWeight.Medium)
        }
        Card(modifier = Modifier.fillMaxWidth().weight(1f), elevation = 1.dp) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                reverseLayout = false
            ) {
                items(log) { msg ->
                    Text(msg, style = MaterialTheme.typography.caption)
                }
            }
        }
    }
}

@Composable
private fun ResultPanel(
    result: LegacyMigrationViewModel.MigrationResult,
    log: List<String>,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF388E3C))
            Spacer(Modifier.width(8.dp))
            Text("Migration terminée", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)
        }

        Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Importés", fontWeight = FontWeight.Bold)
                PreviewRow("Animaux", result.imported.animals)
                PreviewRow("Consultations", result.imported.consultations)
                PreviewRow("Rations", result.imported.rations)
                PreviewRow("Pesées", result.imported.weights)
                PreviewRow("Aliments", result.imported.foods)
                if (result.skipped.animals + result.skipped.consultations + result.skipped.foods > 0) {
                    Divider(Modifier.padding(vertical = 4.dp))
                    Text("Ignorés (déjà présents)", fontWeight = FontWeight.Bold)
                    if (result.skipped.animals > 0) PreviewRow("Animaux", result.skipped.animals)
                    if (result.skipped.consultations > 0) PreviewRow("Consultations", result.skipped.consultations)
                    if (result.skipped.foods > 0) PreviewRow("Aliments", result.skipped.foods)
                }
                if (result.errors.isNotEmpty()) {
                    Divider(Modifier.padding(vertical = 4.dp))
                    Text("Erreurs : ${result.errors.size}", color = MaterialTheme.colors.error, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (log.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().weight(1f), elevation = 1.dp) {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    items(log) { msg -> Text(msg, style = MaterialTheme.typography.caption) }
                }
            }
        }

        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("Fermer")
        }
    }
}

@Composable
private fun ErrorPanel(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth(), backgroundColor = Color(0xFFFFEBEE), elevation = 2.dp) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colors.error)
                    Spacer(Modifier.width(8.dp))
                    Text("Erreur", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.error)
                }
                Spacer(Modifier.height(8.dp))
                Text(message, style = MaterialTheme.typography.body2)
            }
        }
        OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Text("Recommencer")
        }
    }
}
