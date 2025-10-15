package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Services.ExcelFoodService
import fr.vetbrain.vetnutri_mp.ExcelPlatform.*
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlinx.coroutines.launch

/**
 * Section Import/Export Excel pour les aliments
 */
@Composable
fun ExcelImportExportSection(
        modifier: Modifier = Modifier,
        excelFoodService: ExcelFoodService? = null
) {
        // États pour la gestion des dialogues
        var showPreviewDialog by remember { mutableStateOf(false) }
        var csvContent by remember { mutableStateOf<String?>(null) }
        var previewAliments by remember { mutableStateOf<List<AlimentEv>>(emptyList()) }
        var previewErrors by remember { mutableStateOf<List<String>>(emptyList()) }

        // États pour l'import/export
        var isExporting by remember { mutableStateOf(false) }
        var isImporting by remember { mutableStateOf(false) }
        var exportResult by remember { mutableStateOf<String?>(null) }
        var importResult by remember { mutableStateOf<ExcelFoodService.ExcelImportResult?>(null) }
        var showImportDialog by remember { mutableStateOf(false) }

        val coroutineScope = rememberCoroutineScope()
        
        // Vérifier si les opérations CSV sont supportées
        val csvSupported = isCsvFileOperationsSupported()

        Column(
                modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                // Informations générales
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f),
                        elevation = 2.dp
                ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                        "Import/Export Excel Aliments",
                                        style = MaterialTheme.typography.h6,
                                        color = VetNutriColors.Primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                        "Cette section vous permet d'importer et exporter des aliments via des fichiers CSV compatibles Excel. Chaque aliment peut avoir jusqu'à 76 nutriments différents avec leurs unités individuelles.",
                                        style = MaterialTheme.typography.body2
                                )
                                
                                if (!csvSupported) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                                "⚠️ Les opérations de fichiers CSV ne sont pas supportées sur cette plateforme.",
                                                style = MaterialTheme.typography.body2,
                                                color = Color(0xFFFF6B35)
                                        )
                                }
                        }
                }

                // Section Export
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 4.dp
                ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                        "Export des aliments",
                                        style = MaterialTheme.typography.h6,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text(
                                        "Exporter tous les aliments de la base de données vers un fichier CSV compatible Excel.",
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Button(
                                        onClick = {
                                                coroutineScope.launch {
                                                        isExporting = true
                                                        try {
                                                                if (excelFoodService != null) {
                                                                        // Export réel avec le service
                                                                        val csv = excelFoodService.exportAllFoodsToCsv()
                                                                        val success = saveCsvFileForExport(csv, "aliments_export.csv")
                                                                        exportResult = if (success) 
                                                                                "✅ Export réussi: fichier sauvegardé" 
                                                                        else 
                                                                                "❌ Erreur lors de la sauvegarde du fichier"
                                                                } else {
                                                                        // Fallback: générer un exemple
                                                                        val csv = excelFoodService?.generateExampleCsv() ?: "Exemple CSV"
                                                                        exportResult = "✅ Export d'exemple généré (service non disponible)"
                                                                }
                                                        } catch (e: Exception) {
                                                                exportResult = "❌ Erreur d'export: ${e.message}"
                                                        } finally {
                                                                isExporting = false
                                                        }
                                                }
                                        },
                                        enabled = !isExporting && !isImporting && csvSupported,
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        if (isExporting) {
                                                CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        color = Color.White,
                                                        strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                        } else {
                                                Icon(Icons.Default.Download, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text("Exporter vers CSV")
                                }
                        }
                }

                // Résultat d'export
                exportResult?.let { result ->
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = if (result.startsWith("✅"))
                                        Color(0xFFE8F5E8) else Color(0xFFFFEBEE),
                                elevation = 2.dp
                        ) {
                                Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                result,
                                                style = MaterialTheme.typography.body2,
                                                color = if (result.startsWith("✅")) Color(0xFF2E7D32) else Color(0xFFC62828),
                                                modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = { exportResult = null }) {
                                                Icon(Icons.Default.Close, contentDescription = "Fermer")
                                        }
                                }
                        }
                }

                // Section Import
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 4.dp
                ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                        "Import des aliments",
                                        style = MaterialTheme.typography.h6,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text(
                                        "Importer des aliments depuis un fichier CSV. Une prévisualisation vous permettra de vérifier les données avant confirmation.",
                                        style = MaterialTheme.typography.body2,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Button(
                                        onClick = {
                                                // Ouvrir le fichier CSV en dehors du contexte coroutine (comme les autres file browsers)
                                                val csv = openCsvFileForImport()
                                                if (csv != null) {
                                                        coroutineScope.launch {
                                                                isImporting = true
                                                                try {
                                                                        if (excelFoodService != null && csvSupported) {
                                                                                // Import réel avec le contenu du fichier
                                                                                csvContent = csv
                                                                                val result = excelFoodService.importFoodsFromCsv(csv)
                                                                                importResult = result
                                                                                showImportDialog = true
                                                                        } else {
                                                                                // Fallback: utiliser l'exemple
                                                                                val csvExample = excelFoodService?.generateExampleCsv() ?: "Exemple CSV"
                                                                                csvContent = csvExample
                                                                                val result = excelFoodService?.previewCsvImport(csvExample) ?: ExcelFoodService.ExcelImportResult(
                                                                                        success = false,
                                                                                        message = "Service non disponible"
                                                                                )
                                                                                importResult = result
                                                                                showImportDialog = true
                                                                        }
                                                                } catch (e: Exception) {
                                                                        importResult = ExcelFoodService.ExcelImportResult(
                                                                                success = false,
                                                                                message = "Erreur lors de la préparation: ${e.message}"
                                                                        )
                                                                } finally {
                                                                        isImporting = false
                                                                }
                                                        }
                                                } else {
                                                        // Aucun fichier sélectionné
                                                        importResult = ExcelFoodService.ExcelImportResult(
                                                                success = false,
                                                                message = "Aucun fichier sélectionné"
                                                        )
                                                }
                                        },
                                        enabled = !isExporting && !isImporting,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Secondary
                                        )
                                ) {
                                        if (isImporting) {
                                                CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        color = Color.White,
                                                        strokeWidth = 2.dp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                        } else {
                                                Icon(Icons.Default.Upload, contentDescription = null)
                                                Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text("Importer depuis CSV")
                                }
                        }
                }

                // Résultat d'import
                importResult?.let { result ->
                        Card(
                                modifier = Modifier.fillMaxWidth(),
                                backgroundColor = if (result.success)
                                        Color(0xFFE8F5E8) else Color(0xFFFFEBEE),
                                elevation = 2.dp
                        ) {
                                Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                        result.message,
                                                style = MaterialTheme.typography.body2,
                                                        color = if (result.success) Color(0xFF2E7D32) else Color(0xFFC62828)
                                                )
                                                if (result.importedCount > 0 || result.updatedCount > 0) {
                                                        Text(
                                                                "Importés: ${result.importedCount}, Mis à jour: ${result.updatedCount}",
                                                                style = MaterialTheme.typography.caption,
                                                                color = Color.Gray
                                                        )
                                                }
                                                if (result.errorCount > 0) {
                                                        Text(
                                                                "Erreurs: ${result.errorCount}",
                                                                style = MaterialTheme.typography.caption,
                                                                color = Color(0xFFFF6B35)
                                                        )
                                                }
                                        }
                                        IconButton(onClick = { importResult = null }) {
                                                Icon(Icons.Default.Close, contentDescription = "Fermer")
                                        }
                                }
                        }
                }

                // Informations sur le format
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colors.surface,
                        elevation = 2.dp
                ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                        "Format CSV requis",
                                        style = MaterialTheme.typography.subtitle1,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text("• Séparateur: point-virgule (;)", style = MaterialTheme.typography.body2)
                                Text("• Encodage: UTF-8", style = MaterialTheme.typography.body2)
                                Text("• Première ligne: en-têtes des colonnes", style = MaterialTheme.typography.body2)
                                Text("• Listes: séparées par des virgules (,)", style = MaterialTheme.typography.body2)
                                Text("• Valeurs vides: cellules vides ou absence de valeur", style = MaterialTheme.typography.body2)

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                        "Structure des colonnes:",
                                        style = MaterialTheme.typography.subtitle2,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                )

                                val mainColumns = listOf(
                                        "UUID", "Nom", "Marque", "Gamme", "Ingrédients",
                                        "Groupe Alimentaire", "Type Aliment", "Conditionnement",
                                        "Prix", "Catégorie Prix", "Quantité Interne",
                                        "Consistant", "Obsolète", "Espèces", "Indications"
                                )

                                mainColumns.forEach { column ->
                                        Text("• $column", style = MaterialTheme.typography.caption)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                        "Nutriments: 76 nutriments × 1 colonne (valeur avec unité dans l'en-tête) = 76 colonnes",
                                        style = MaterialTheme.typography.subtitle2
                                )
                        }
                }
        }

        // Dialogue d'import amélioré
        if (showImportDialog && importResult != null) {
                ExcelImportDialog(
                        result = importResult!!,
                        onConfirm = {
                                if (csvContent != null && excelFoodService != null) {
                                        coroutineScope.launch {
                                                try {
                                                        val finalResult = excelFoodService.importFoodsFromCsv(csvContent!!)
                                                        importResult = finalResult
                                                        showImportDialog = false
                                                } catch (e: Exception) {
                                                        importResult = ExcelFoodService.ExcelImportResult(
                                                                success = false,
                                                                message = "Erreur lors de l'import final: ${e.message}"
                                                        )
                                                }
                                        }
                                } else {
                                        showImportDialog = false
                                }
                        },
                        onCancel = {
                                showImportDialog = false
                                importResult = null
                        }
                )
        }
}

/**
 * Dialogue d'import Excel amélioré
 */
@Composable
fun ExcelImportDialog(
        result: ExcelFoodService.ExcelImportResult,
        onConfirm: () -> Unit,
        onCancel: () -> Unit
) {
        AlertDialog(
                onDismissRequest = onCancel,
                title = {
                        Text(
                                if (result.previewMode) "Prévisualisation de l'import" else "Résultat de l'import",
                                style = MaterialTheme.typography.h6,
                                color = if (result.success) VetNutriColors.Primary else Color(0xFFFF6B35)
                        )
                },
                text = {
                        Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                                Text(
                                        result.message,
                                        style = MaterialTheme.typography.body1
                                )
                                
                                if (result.importedCount > 0 || result.updatedCount > 0) {
                                        Row(
                                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                                if (result.importedCount > 0) {
                                                        Text(
                                                                "Importés: ${result.importedCount}",
                                                                style = MaterialTheme.typography.body2,
                                                                color = VetNutriColors.Primary
                                                        )
                                                }
                                                if (result.updatedCount > 0) {
                                                        Text(
                                                                "Mis à jour: ${result.updatedCount}",
                                                                style = MaterialTheme.typography.body2,
                                                                color = VetNutriColors.Secondary
                                                        )
                                                }
                                        }
                                }
                                
                                if (result.errorCount > 0) {
                                        Text(
                                                "Erreurs: ${result.errorCount}",
                                                style = MaterialTheme.typography.body2,
                                                color = Color(0xFFFF6B35)
                                        )
                                }
                                
                                if (result.errors.isNotEmpty()) {
                                        Text(
                                                "Détails des erreurs:",
                                                style = MaterialTheme.typography.subtitle2,
                                                modifier = Modifier.padding(top = 8.dp)
                                        )
                                        LazyColumn(
                                                modifier = Modifier.height(120.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                                items(result.errors.take(5)) { error ->
                                                        Text(
                                                                "• $error",
                                                                style = MaterialTheme.typography.caption,
                                                                color = Color(0xFFFF6B35)
                                                        )
                                                }
                                                if (result.errors.size > 5) {
                                                        item {
                                                                Text(
                                                                        "... et ${result.errors.size - 5} autres erreurs",
                                                                        style = MaterialTheme.typography.caption,
                                                                        color = Color.Gray
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                },
                confirmButton = {
                        if (result.previewMode) {
                                Button(
                                        onClick = onConfirm,
                                        colors = ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary
                                        )
                                ) {
                                        Text("Confirmer l'import", color = Color.White)
                                }
                        } else {
                                Button(
                                        onClick = onCancel,
                                        colors = ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary
                                        )
                                ) {
                                        Text("Fermer", color = Color.White)
                                }
                        }
                },
                dismissButton = {
                        if (result.previewMode) {
                                OutlinedButton(onClick = onCancel) {
                                        Text("Annuler")
                                }
                        }
                }
        )
}

/**
 * Dialogue de prévisualisation plein écran pour l'import Excel (legacy)
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ExcelImportPreviewDialog(
        aliments: List<AlimentEv>,
        errors: List<String>,
        onConfirm: () -> Unit,
        onCancel: () -> Unit
) {
        val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Expanded)

        ModalBottomSheetLayout(
                sheetState = bottomSheetState,
                sheetContent = {
                        Column(
                                modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                verticalArrangement = Arrangement.Top
                        ) {
                                // En-tête
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        Text(
                                                "Prévisualisation de l'import",
                                                style = MaterialTheme.typography.h5,
                                                color = VetNutriColors.Primary
                                        )
                                        IconButton(onClick = onCancel) {
                                                Icon(Icons.Default.Close, contentDescription = "Fermer")
                                        }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Statistiques
                                Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        backgroundColor = VetNutriColors.Primary.copy(alpha = 0.1f)
                                ) {
                                        Row(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                                aliments.size.toString(),
                                                                style = MaterialTheme.typography.h4,
                                                                color = VetNutriColors.Primary
                                                        )
                                                        Text("Aliments valides", style = MaterialTheme.typography.caption)
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                                errors.size.toString(),
                                                                style = MaterialTheme.typography.h4,
                                                                color = if (errors.isEmpty()) VetNutriColors.Primary else Color(0xFFFF6B35)
                                                        )
                                                        Text("Erreurs", style = MaterialTheme.typography.caption)
                                                }
                                        }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Liste des aliments
                                if (aliments.isNotEmpty()) {
                                        Text(
                                                "Aperçu des aliments (${aliments.size})",
                                                style = MaterialTheme.typography.h6,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        LazyColumn(
                                                modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                                items(aliments.take(10)) { aliment ->
                                                        Card(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                elevation = 2.dp
                                                        ) {
                                                                Column(modifier = Modifier.padding(12.dp)) {
                                                                        Text(
                                                                                aliment.nom ?: "Nom non défini",
                                                                                style = MaterialTheme.typography.subtitle1,
                                                                                fontWeight = FontWeight.Bold
                                                                        )
                                                                        Row(
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                horizontalArrangement = Arrangement.SpaceBetween
                                                                        ) {
                                                                                Text(
                                                                                        "Marque: ${aliment.brand ?: "N/A"}",
                                                                                        style = MaterialTheme.typography.caption
                                                                                )
                                                                                Text(
                                                                                        "Prix: ${aliment.price ?: "N/A"}",
                                                                                        style = MaterialTheme.typography.caption
                                                                                )
                                                                        }
                                                                        Text(
                                                                                "Nutriments: ${aliment.valMap.size}",
                                                                                style = MaterialTheme.typography.caption,
                                                                                color = VetNutriColors.Primary
                                                                        )
                                                                }
                                                        }
                                                }

                                                if (aliments.size > 10) {
                                                        item {
                                                                Text(
                                                                        "... et ${aliments.size - 10} autres aliments",
                                                                        style = MaterialTheme.typography.caption,
                                                                        color = Color.Gray,
                                                                        modifier = Modifier.padding(8.dp)
                                                                )
                                                        }
                                                }
                                        }
                                }

                                // Erreurs
                                if (errors.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(16.dp))

                                        Text(
                                                "Erreurs détectées (${errors.size})",
                                                style = MaterialTheme.typography.h6,
                                                color = Color(0xFFFF6B35),
                                                modifier = Modifier.padding(bottom = 8.dp)
                                        )

                                        LazyColumn(
                                                modifier = Modifier
                                                        .height(120.dp)
                                                        .fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                                items(errors.take(5)) { error ->
                                                        Card(
                                                                backgroundColor = Color(0xFFFFEBEE),
                                                                elevation = 1.dp
                                                        ) {
                                                                Text(
                                                                        error,
                                                                        style = MaterialTheme.typography.caption,
                                                                        color = Color(0xFFC62828),
                                                                        modifier = Modifier.padding(8.dp)
                                                                )
                                                        }
                                                }

                                                if (errors.size > 5) {
                                                        item {
                                                                Text(
                                                                        "... et ${errors.size - 5} autres erreurs",
                                                                        style = MaterialTheme.typography.caption,
                                                                        color = Color.Gray
                                                                )
                                                        }
                                                }
                                        }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Boutons d'action
                                Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                        OutlinedButton(
                                                onClick = onCancel,
                                                modifier = Modifier.weight(1f)
                                        ) {
                                                Text("Annuler")
                                        }

                                        Button(
                                                onClick = onConfirm,
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Primary
                                                )
                                        ) {
                                                Text("Confirmer l'import")
                                        }
                                }
                        }
                }
        ) {
                // Contenu principal vide pour le ModalBottomSheetLayout
                Box(modifier = Modifier.fillMaxSize())
        }
}
