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
import fr.vetbrain.vetnutri_mp.Services.AlimentExcelService
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import kotlinx.coroutines.launch

/**
 * Section Import/Export Excel pour les aliments
 */
@Composable
fun ExcelImportExportSection(
        modifier: Modifier = Modifier
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
        var importResult by remember { mutableStateOf<String?>(null) }

        val coroutineScope = rememberCoroutineScope()

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
                                                                // Générer le CSV d'exemple
                                                                val csv = AlimentExcelService.generateExampleCsv()

                                                                // Pour l'instant, on affiche juste un message de succès
                                                                // Dans une vraie implémentation, on sauvegarderait dans un fichier
                                                                exportResult = "✅ Export d'exemple généré avec succès"

                                                        } catch (e: Exception) {
                                                                exportResult = "❌ Erreur d'export: ${e.message}"
                                                        } finally {
                                                                isExporting = false
                                                        }
                                                }
                                        },
                                        enabled = !isExporting && !isImporting,
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
                                                // Simulation de sélection de fichier CSV
                                                // Dans une vraie implémentation, on utiliserait un file picker
                                                coroutineScope.launch {
                                                        isImporting = true
                                                        try {
                                                                // Pour la démonstration, on utilise l'exemple CSV
                                                                val csv = AlimentExcelService.generateExampleCsv()
                                                                csvContent = csv

                                                                // Parser le CSV pour prévisualisation
                                                                val service = AlimentExcelService()
                                                                val importResult = service.importFromCsv(csv)

                                                                previewAliments = importResult.aliments
                                                                previewErrors = importResult.errors

                                                                showPreviewDialog = true

                                                        } catch (e: Exception) {
                                                                importResult = "❌ Erreur lors de la préparation: ${e.message}"
                                                        } finally {
                                                                isImporting = false
                                                        }
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
                                        "Nutriments: 76 nutriments × 2 colonnes (valeur + unité) = 152 colonnes",
                                        style = MaterialTheme.typography.subtitle2
                                )
                        }
                }
        }

        // Dialogue de prévisualisation plein écran
        if (showPreviewDialog) {
                ExcelImportPreviewDialog(
                        aliments = previewAliments,
                        errors = previewErrors,
                        onConfirm = {
                                // Simulation d'import réussi
                                importResult = "✅ Import réussi: ${previewAliments.size} aliments importés"
                                if (previewErrors.isNotEmpty()) {
                                        importResult += " (${previewErrors.size} erreurs ignorées)"
                                }
                                showPreviewDialog = false
                        },
                        onCancel = {
                                showPreviewDialog = false
                                previewAliments = emptyList()
                                previewErrors = emptyList()
                        }
                )
        }
}

/**
 * Dialogue de prévisualisation plein écran pour l'import Excel
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
