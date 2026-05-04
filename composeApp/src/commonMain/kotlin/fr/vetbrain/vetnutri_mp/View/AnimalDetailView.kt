package fr.vetbrain.vetnutri_mp.View

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.DrawerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.vetbrain.vetnutri_mp.Components.ConfirmDialog
import fr.vetbrain.vetnutri_mp.Components.RichTextEditor
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Export.DocumentType
import fr.vetbrain.vetnutri_mp.Export.ExportData
import fr.vetbrain.vetnutri_mp.Export.HtmlDocumentBuilder
import fr.vetbrain.vetnutri_mp.Export.HtmlPreviewDialog
import fr.vetbrain.vetnutri_mp.Export.PdfExporter
import fr.vetbrain.vetnutri_mp.Localization.translateEnum
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.AnimalDetail
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.General
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Settings
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
import fr.vetbrain.vetnutri_mp.Repository.PreferencesRepository
import fr.vetbrain.vetnutri_mp.Repository.RecipeRepository
import fr.vetbrain.vetnutri_mp.Theme.AppIcons
import fr.vetbrain.vetnutri_mp.Theme.AppSizes
import fr.vetbrain.vetnutri_mp.Theme.VetNutriColors
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.copyToClipboardComposable
import fr.vetbrain.vetnutri_mp.Utils.createPreferencesStorage
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailSection
import fr.vetbrain.vetnutri_mp.exportApiEnvelopeToFile
import kotlinx.coroutines.withContext
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import fr.vetbrain.vetnutri_mp.Service.JsonShareService
import fr.vetbrain.vetnutri_mp.Service.ShareOptions
import fr.vetbrain.vetnutri_mp.Components.ShareLinkDialog
import fr.vetbrain.vetnutri_mp.Components.IconButtonWithTooltip
import fr.vetbrain.vetnutri_mp.Components.AnonymizationDialog
import fr.vetbrain.vetnutri_mp.Utils.anonymizeExportJson
import kotlin.math.roundToInt

typealias RecipeRepo = fr.vetbrain.vetnutri_mp.Repository.RecipeRepository

private data class PractitionerContact(
        val nom: String = "",
        val numeroOrdre: String = "",
        val adressePostale: String = "",
        val codePostal: String = "",
        val ville: String = "",
        val telephone: String = "",
        val email: String = ""
)

private fun formatAlimentDisplayName(aliment: AlimentEv?): String {
        if (aliment == null) return "Ingredient"
        fun debugChars(input: String): String =
                input.map { c -> "${c.code.toString(16).padStart(4, '0')}(${c})" }.joinToString(" ")
        fun clean(value: String?): String? {
                if (value == null) return null
                var normalized = value.trim()
                while (normalized.length >= 2 &&
                        ((normalized.startsWith("\"") && normalized.endsWith("\"")) ||
                                (normalized.startsWith("'") && normalized.endsWith("'")))) {
                        normalized = normalized.substring(1, normalized.length - 1).trim()
                }
                normalized =
                        normalized
                                .replace('\u00A0', ' ')
                                .replace(Regex("""^[\s"'`]+|[\s"'`]+$"""), "")
                if (normalized.isBlank()) return null
                val semantic =
                        normalized.lowercase().replace(Regex("""[^\p{L}\p{N}]+"""), "")
                if (semantic == "null" || semantic == "none" || semantic == "na") return null
                return normalized
        }
        if ((aliment.brand ?: "").contains("null", ignoreCase = true) ||
                        (aliment.gamme ?: "").contains("null", ignoreCase = true) ||
                        (aliment.nom ?: "").contains("null", ignoreCase = true)) {
                println(
                        "[CR_ALIMENT_DEBUG] raw brand='${aliment.brand}' gamme='${aliment.gamme}' nom='${aliment.nom}'"
                )
                println(
                        "[CR_ALIMENT_DEBUG] raw gamme chars: ${
                                aliment.gamme?.let { debugChars(it) } ?: "<null>"
                        }"
                )
        }
        val parts =
                listOf(
                                clean(aliment.brand),
                                clean(aliment.gamme),
                                clean(aliment.nom)
                        )
                        .filterNotNull()
                        .map { it.trim() }
                        .filter { value ->
                                val semantic =
                                        value.lowercase().replace(Regex("""[^\p{L}\p{N}]+"""), "")
                                value.isNotBlank() &&
                                        semantic != "null" &&
                                        semantic != "none" &&
                                        semantic != "na"
                        }
        val result = if (parts.isEmpty()) "Ingredient" else parts.joinToString(", ")
        if (result.contains(", null,", ignoreCase = true) || result.contains(" null", ignoreCase = true)) {
                println(
                        "[CR_ALIMENT_DEBUG] cleaned parts=$parts result='$result' for uuid='${aliment.uuid}'"
                )
        }
        return result
}

/**
 * Génère un nom de fichier par défaut pour l'export PDF
 * Format: "ID animal + Nom Animal + date consultation.pdf"
 */
private fun generateDefaultPdfFileName(animal: AnimalEv?, consultation: ConsultationEv?): String {
    val animalId = animal?.id ?: translate(AnimalDetail.UNKNOWN_ID)
    val animalName = animal?.nom ?: translate(AnimalDetail.UNKNOWN_NAME)
    val consultationDate = consultation?.date?.toString() ?: translate(AnimalDetail.UNKNOWN_DATE)
    return "${animalId}_${animalName}_${consultationDate}.pdf"
}

private fun buildCompteRenduText(
        animal: AnimalEv?,
        consultation: ConsultationEv?,
        practitionerContact: PractitionerContact?,
        anamnese: String,
        examenClinique: String,
        facteurNutritionnelClef: String,
        additionalText: String,
        selectedConseils: List<fr.vetbrain.vetnutri_mp.Export.HtmlSection>
): String {
        fun text(value: String?): String = value?.takeIf { it.isNotBlank() } ?: "-"

        fun formatRationDetails(ration: Ration): String {
                val rationName = if (ration.name.isBlank()) "Sans nom" else ration.name
                val ingredients = ration.alimentMutableList.joinToString("\n") { alimentRation ->
                        val ingredientName = formatAlimentDisplayName(alimentRation.aliment)
                        val quantity = (alimentRation.quantite * 10.0).roundToInt() / 10.0
                        "- $ingredientName: $quantity g"
                }
                val details = if (ingredients.isBlank()) "- Aucun ingredient" else ingredients
                return "- $rationName\n$details"
        }

        fun formatConseilDetails(section: fr.vetbrain.vetnutri_mp.Export.HtmlSection): String {
                val body =
                        section.content.blocks.joinToString("\n") { block ->
                                when (block) {
                                        is fr.vetbrain.vetnutri_mp.Export.TextBlock.Paragraph ->
                                                block.text
                                        is fr.vetbrain.vetnutri_mp.Export.TextBlock.Heading ->
                                                block.text
                                        is fr.vetbrain.vetnutri_mp.Export.TextBlock.ListBlock ->
                                                block.items.joinToString("\n") { "- $it" }
                                        is fr.vetbrain.vetnutri_mp.Export.TextBlock.TableBlock -> {
                                                val headers = if (block.headers.isNotEmpty()) {
                                                        block.headers.joinToString(" | ")
                                                } else {
                                                        ""
                                                }
                                                val rows =
                                                        block.rows.joinToString("\n") { row ->
                                                                row.joinToString(" | ")
                                                        }
                                                listOf(headers, rows)
                                                        .filter { it.isNotBlank() }
                                                        .joinToString("\n")
                                        }
                                        is fr.vetbrain.vetnutri_mp.Export.TextBlock.RawHtml ->
                                                block.html
                                }
                        }
                return "- ${section.title}\n${if (body.isBlank()) "-" else body}"
        }

        val currentRations =
                consultation?.rations
                        ?.filter { it.actual }
                        ?.joinToString("\n\n") { formatRationDetails(it) } ?: ""
        val proposedRations =
                consultation?.rations
                        ?.filter { !it.actual }
                        ?.joinToString("\n\n") { formatRationDetails(it) } ?: ""
        val conseilsText = selectedConseils.joinToString("\n\n") { formatConseilDetails(it) }

        return buildString {
                appendLine("Compte rendu nutritionnel")
                appendLine("ID animal: ${animal?.id?.takeIf { it.isNotBlank() } ?: "-"}")
                appendLine("Date de consultation: ${consultation?.date?.toString() ?: "-"}")
                appendLine("Objet de consultation: ${consultation?.objectConsult?.takeIf { it.isNotBlank() } ?: "-"}")
                appendLine()
                appendLine("Identification animal")
                appendLine("Nom: ${text(animal?.nom)}")
                appendLine("Proprietaire: ${text(animal?.ownerName)}")
                appendLine("Sexe: ${text(animal?.getSex()?.displayName)}")
                appendLine("Espece: ${text(animal?.getEspece()?.label)}")
                appendLine("Race: ${text(animal?.race)}")
                appendLine("Date de naissance: ${text(animal?.birthdate?.toString())}")
                appendLine("Poids consultation: ${consultation?.effectiveWeight?.let { "${(it * 10.0).roundToInt() / 10.0} kg" } ?: "-"}")
                appendLine("UUID: ${text(animal?.uuid)}")
                appendLine()
                appendLine("Coordonnees veterinaire")
                appendLine("Nom: ${practitionerContact?.nom?.ifBlank { "-" } ?: "-"}")
                appendLine("N° ordre: ${practitionerContact?.numeroOrdre?.ifBlank { "-" } ?: "-"}")
                appendLine("Adresse: ${practitionerContact?.adressePostale?.ifBlank { "-" } ?: "-"}")
                appendLine("Code postal: ${practitionerContact?.codePostal?.ifBlank { "-" } ?: "-"}")
                appendLine("Ville: ${practitionerContact?.ville?.ifBlank { "-" } ?: "-"}")
                appendLine("Telephone: ${practitionerContact?.telephone?.ifBlank { "-" } ?: "-"}")
                appendLine("Email: ${practitionerContact?.email?.ifBlank { "-" } ?: "-"}")
                appendLine()
                appendLine("Anamnèse")
                appendLine(if (anamnese.isBlank()) "-" else anamnese)
                appendLine()
                appendLine("Examen clinique")
                appendLine(if (examenClinique.isBlank()) "-" else examenClinique)
                appendLine()
                appendLine("Rations actuelles")
                appendLine(if (currentRations.isBlank()) "-" else currentRations)
                appendLine()
                appendLine("Facteur nutritionnel clef")
                appendLine(if (facteurNutritionnelClef.isBlank()) "-" else facteurNutritionnelClef)
                appendLine()
                appendLine("Rations proposees")
                appendLine(if (proposedRations.isBlank()) "-" else proposedRations)
                appendLine()
                appendLine("Conseils ordonnance")
                appendLine(if (conseilsText.isBlank()) "-" else conseilsText)
                appendLine()
                appendLine("Texte additionnel ordonnance")
                appendLine(if (additionalText.isBlank()) "-" else additionalText)
        }
}

private fun escapeHtml(value: String): String =
        value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

private fun buildCompteRenduHtml(
        animal: AnimalEv?,
        consultation: ConsultationEv?,
        practitionerContact: PractitionerContact?,
        anamnese: String,
        examenClinique: String,
        facteurNutritionnelClef: String,
        additionalText: String,
        selectedConseils: List<fr.vetbrain.vetnutri_mp.Export.HtmlSection>
): String {
        fun textOrDash(value: String): String = if (value.isBlank()) "-" else escapeHtml(value)

        fun rationBlock(title: String, rations: List<Ration>): String {
                if (rations.isEmpty()) {
                        return """
                            <div class='section'>
                                <h2>$title</h2>
                                <div class='muted'>-</div>
                            </div>
                        """.trimIndent()
                }
                val cards = rations.joinToString("\n") { ration ->
                        val rationName = if (ration.name.isBlank()) "Sans nom" else escapeHtml(ration.name)
                        val rows = ration.alimentMutableList.joinToString("\n") { ar ->
                                val ingredient = formatAlimentDisplayName(ar.aliment)
                                val quantity = (ar.quantite * 10.0).roundToInt() / 10.0
                                "<tr><td>${escapeHtml(ingredient)}</td><td style='text-align:right'>${quantity} g</td></tr>"
                        }
                        val body =
                                if (rows.isBlank()) {
                                        "<tr><td colspan='2' class='muted'>Aucun ingredient</td></tr>"
                                } else {
                                        rows
                                }
                        """
                            <div class='card'>
                                <h3>$rationName</h3>
                                <table>
                                    <thead><tr><th>Ingredient</th><th>Quantite</th></tr></thead>
                                    <tbody>$body</tbody>
                                </table>
                            </div>
                        """.trimIndent()
                }
                return """
                    <div class='section'>
                        <h2>$title</h2>
                        $cards
                    </div>
                """.trimIndent()
        }

        fun conseilBody(section: fr.vetbrain.vetnutri_mp.Export.HtmlSection): String {
                val lines = section.content.blocks.mapNotNull { block ->
                        when (block) {
                                is fr.vetbrain.vetnutri_mp.Export.TextBlock.Paragraph ->
                                        block.text.takeIf { it.isNotBlank() }?.let { "<p>${escapeHtml(it)}</p>" }
                                is fr.vetbrain.vetnutri_mp.Export.TextBlock.Heading ->
                                        block.text.takeIf { it.isNotBlank() }?.let { "<p><b>${escapeHtml(it)}</b></p>" }
                                is fr.vetbrain.vetnutri_mp.Export.TextBlock.ListBlock -> {
                                        if (block.items.isEmpty()) null
                                        else {
                                                val tag = if (block.isOrdered) "ol" else "ul"
                                                val items = block.items.joinToString("") { "<li>${escapeHtml(it)}</li>" }
                                                "<$tag>$items</$tag>"
                                        }
                                }
                                is fr.vetbrain.vetnutri_mp.Export.TextBlock.TableBlock -> {
                                        val headers = if (block.headers.isNotEmpty()) {
                                                "<tr>${block.headers.joinToString("") { "<th>${escapeHtml(it)}</th>" }}</tr>"
                                        } else ""
                                        val rows = block.rows.joinToString("") { row ->
                                                "<tr>${row.joinToString("") { "<td>${escapeHtml(it)}</td>" }}</tr>"
                                        }
                                        "<table><thead>$headers</thead><tbody>$rows</tbody></table>"
                                }
                                is fr.vetbrain.vetnutri_mp.Export.TextBlock.RawHtml -> block.html
                        }
                }
                return if (lines.isEmpty()) "<p class='muted'>-</p>" else lines.joinToString("\n")
        }

        val conseilsHtml =
                if (selectedConseils.isEmpty()) {
                        "<div class='muted'>-</div>"
                } else {
                        selectedConseils.joinToString("\n") { section ->
                                """
                                    <div class='card'>
                                        <h3>${escapeHtml(section.title)}</h3>
                                        ${conseilBody(section)}
                                    </div>
                                """.trimIndent()
                        }
                }

        return """
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; color:#1f2937; margin:24px; }
                    h1 { margin:0 0 8px 0; color:#0f172a; border-bottom:2px solid #0ea5e9; padding-bottom:8px; }
                    h2 { margin:0 0 10px 0; color:#0f172a; }
                    h3 { margin:0 0 8px 0; color:#1e3a8a; }
                    .meta { color:#475569; margin-bottom:16px; }
                    .section { margin:16px 0; padding:14px; border:1px solid #e2e8f0; border-radius:10px; background:#f8fafc; }
                    .card { margin:10px 0; padding:10px; border:1px solid #cbd5e1; border-radius:8px; background:white; }
                    .muted { color:#64748b; }
                    table { width:100%; border-collapse:collapse; margin-top:6px; }
                    th, td { border:1px solid #e2e8f0; padding:6px 8px; }
                    th { background:#f1f5f9; text-align:left; }
                </style>
            </head>
            <body>
                <h1>Compte rendu nutritionnel</h1>
                <div class='meta'><b>ID animal:</b> ${escapeHtml(animal?.id?.takeIf { it.isNotBlank() } ?: "-")} | <b>Nom:</b> ${escapeHtml(animal?.nom ?: "-")}</div>
                <div class='meta'><b>Date de consultation:</b> ${escapeHtml(consultation?.date?.toString() ?: "-")} | <b>Objet:</b> ${escapeHtml(consultation?.objectConsult?.takeIf { it.isNotBlank() } ?: "-")}</div>

                <div class='section'>
                    <h2>Identification animal</h2>
                    <div><b>Nom:</b> ${escapeHtml(animal?.nom?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>Proprietaire:</b> ${escapeHtml(animal?.ownerName?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>Sexe:</b> ${escapeHtml(animal?.getSex()?.displayName?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>Espece:</b> ${escapeHtml(animal?.getEspece()?.label?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>Race:</b> ${escapeHtml(animal?.race?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>Date de naissance:</b> ${escapeHtml(animal?.birthdate?.toString() ?: "-")}</div>
                    <div><b>Poids consultation:</b> ${escapeHtml(consultation?.effectiveWeight?.let { "${(it * 10.0).roundToInt() / 10.0} kg" } ?: "-")}</div>
                    <div><b>UUID:</b> ${escapeHtml(animal?.uuid ?: "-")}</div>
                </div>

                <div class='section'>
                    <h2>Coordonnees veterinaire</h2>
                    <div><b>Nom:</b> ${escapeHtml(practitionerContact?.nom?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>N° ordre:</b> ${escapeHtml(practitionerContact?.numeroOrdre?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>Adresse:</b> ${escapeHtml(practitionerContact?.adressePostale?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>Code postal:</b> ${escapeHtml(practitionerContact?.codePostal?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>Ville:</b> ${escapeHtml(practitionerContact?.ville?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>Telephone:</b> ${escapeHtml(practitionerContact?.telephone?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>Email:</b> ${escapeHtml(practitionerContact?.email?.ifBlank { "-" } ?: "-")}</div>
                </div>

                <div class='section'>
                    <h2>Anamnese</h2>
                    <p>${textOrDash(anamnese)}</p>
                </div>

                <div class='section'>
                    <h2>Examen clinique</h2>
                    <p>${textOrDash(examenClinique)}</p>
                </div>

                ${rationBlock("Rations actuelles", consultation?.rations?.filter { it.actual } ?: emptyList())}

                <div class='section'>
                    <h2>Facteur nutritionnel clef</h2>
                    <p>${textOrDash(facteurNutritionnelClef)}</p>
                </div>

                ${rationBlock("Rations proposees", consultation?.rations?.filter { !it.actual } ?: emptyList())}

                <div class='section'>
                    <h2>Conseils ordonnance</h2>
                    $conseilsHtml
                </div>

                <div class='section'>
                    <h2>Texte additionnel ordonnance</h2>
                    <p>${textOrDash(additionalText)}</p>
                </div>
            </body>
            </html>
        """.trimIndent()
}

/**
 * Fonction pour exporter un animal complet avec toutes ses données associées (références, rations, consultations, aliments)
 * Utilise un sélecteur de fichier pour choisir l'emplacement de sauvegarde
 */
private suspend fun exporterAnimalComplet(
        animal: AnimalEv,
        viewModel: AnimalDetailViewModel,
        settingsViewModel: SettingsViewModel,
        equationRepository: EquationRepository,
        recipeRepository: RecipeRepository,
        conseilRepository: fr.vetbrain.vetnutri_mp.Repository.ConseilRepository,
        snackbarHostState: SnackbarHostState
) {
        try {
                // Créer le repository d'export/import avec tous les repositories nécessaires depuis settingsViewModel (sur IO)
                val exportImportRepository = withContext(AppDispatchers.IO) {
                        ExportImportRepository(
                                animalRepository = settingsViewModel.animalRepository,
                                foodRepository = settingsViewModel.foodRepository,
                                equationRepository = settingsViewModel.equationRepository ?: equationRepository,
                                referenceRepository = settingsViewModel.referenceEvRepository,
                                biblioRepository = settingsViewModel.biblioRefRepository,
                                consultationRepository = settingsViewModel.consultationRepository,
                                recipeRepository = settingsViewModel.recipeRepository ?: recipeRepository,
                                conseilRepository = settingsViewModel.conseilRepository ?: conseilRepository
                        )
                }
                
                // Collecter tous les aliments utilisés dans les rations de l'animal (sur IO)
                val foodIds = withContext(AppDispatchers.IO) {
                        val ids = mutableSetOf<String>()
                        animal.consultations.forEach { consultation ->
                                consultation.rations.forEach { ration ->
                                        ration.alimentMutableList.forEach { alimentRation ->
                                                // Collecter l'ID depuis refAlimUnif ou depuis l'aliment complet
                                                alimentRation.refAlimUnif?.takeIf { it.isNotBlank() }?.let { ids.add(it) }
                                                alimentRation.aliment?.uuid?.takeIf { it.isNotBlank() }?.let { ids.add(it) }
                                        }
                                }
                        }
                        ids
                }
                
                // Collecter toutes les références nutritionnelles utilisées dans les consultations (sur IO)
                val (referenceIds, equationIds) = withContext(AppDispatchers.IO) {
                        val refIds = mutableSetOf<String>()
                        val eqIds = mutableSetOf<String>()
                        
                        animal.consultations.forEach { consultation ->
                                // Référence générale
                                consultation.referenceGeneraleId?.let { refId ->
                                        refIds.add(refId)
                                        // Charger la référence pour obtenir ses équations
                                        val reference = settingsViewModel.referenceEvRepository?.getReferenceEvById(refId)
                                        reference?.let { ref ->
                                                // Collecter toutes les équations de la référence
                                                ref.obtenirToutesEquations().forEach { equation ->
                                                        eqIds.add(equation.uuid)
                                                }
                                        }
                                }
                                // Références maladies
                                consultation.referencesMaladies.forEach { refId ->
                                        refIds.add(refId)
                                        // Charger la référence pour obtenir ses équations
                                        val reference = settingsViewModel.referenceEvRepository?.getReferenceEvById(refId)
                                        reference?.let { ref ->
                                                // Collecter toutes les équations de la référence
                                                ref.obtenirToutesEquations().forEach { equation ->
                                                        eqIds.add(equation.uuid)
                                                }
                                        }
                                }
                        }
                        Pair(refIds, eqIds)
                }
                
                // Exporter avec sélection (sur IO)
                // Note: Les références bibliographiques sont automatiquement incluses dans l'export
                // car elles sont liées aux références nutritionnelles et équations exportées
                val envelope = withContext(AppDispatchers.IO) {
                        val exportOptions = ExportImportRepository.ExportSelectionOptions(
                                includeAnimals = true,
                                includeFoods = true,
                                includeRations = true,
                                includeRecipes = false,
                                includeEquations = true,
                                includeConseils = false,
                                animalIds = setOf(animal.uuid),
                                foodIds = foodIds,
                                referenceIds = referenceIds,
                                equationIds = equationIds
                        )
                        exportImportRepository.exportWithSelectionEnvelope(exportOptions)
                }
                
                // Générer le nom de fichier
                val fileName = "${animal.id ?: animal.uuid}_${animal.nom}_export.json"
                
                // Sauvegarder le fichier avec sélecteur (sur Main pour l'UI)
                val success = withContext(AppDispatchers.Main) {
                        exportApiEnvelopeToFile(envelope, fileName)
                }
                
                // Afficher le résultat (déjà sur Main)
                if (success) {
                        snackbarHostState.showSnackbar(
                                message = "${translate(AnimalDetail.EXPORT_SUCCESS)}$fileName",
                                duration = SnackbarDuration.Short
                        )
                } else {
                        snackbarHostState.showSnackbar(
                                message = translate(AnimalDetail.EXPORT_CANCELLED),
                                duration = SnackbarDuration.Long
                        )
                }
        } catch (e: Exception) {
                e.printStackTrace()
                snackbarHostState.showSnackbar(
                        message = "${translate(AnimalDetail.EXPORT_ERROR)}${e.message}",
                        duration = SnackbarDuration.Long
                )
        }
}

/**
 * Fonction pour partager un animal complet en ligne via jsonbin.io
 * Génère un lien de partage unique que l'utilisateur peut partager
 * @param shouldAnonymize Si true, anonymise les données avant l'export
 */
private suspend fun partagerAnimalEnLigne(
        animal: AnimalEv,
        viewModel: AnimalDetailViewModel,
        settingsViewModel: SettingsViewModel,
        equationRepository: EquationRepository,
        recipeRepository: RecipeRepository,
        conseilRepository: fr.vetbrain.vetnutri_mp.Repository.ConseilRepository,
        snackbarHostState: SnackbarHostState,
        onShareLinkGenerated: (fr.vetbrain.vetnutri_mp.Service.ShareLink) -> Unit,
        shouldAnonymize: Boolean = false,
        shouldEncrypt: Boolean = true
) {
        try {
                // Créer le repository d'export/import avec tous les repositories nécessaires depuis settingsViewModel (sur IO)
                val exportImportRepository = withContext(AppDispatchers.IO) {
                        ExportImportRepository(
                                animalRepository = settingsViewModel.animalRepository,
                                foodRepository = settingsViewModel.foodRepository,
                                equationRepository = settingsViewModel.equationRepository ?: equationRepository,
                                referenceRepository = settingsViewModel.referenceEvRepository,
                                biblioRepository = settingsViewModel.biblioRefRepository,
                                consultationRepository = settingsViewModel.consultationRepository,
                                recipeRepository = settingsViewModel.recipeRepository ?: recipeRepository,
                                conseilRepository = settingsViewModel.conseilRepository ?: conseilRepository
                        )
                }
                
                // Collecter tous les aliments utilisés dans les rations de l'animal (sur IO)
                val foodIds = withContext(AppDispatchers.IO) {
                        val ids = mutableSetOf<String>()
                        animal.consultations.forEach { consultation ->
                                consultation.rations.forEach { ration ->
                                        ration.alimentMutableList.forEach { alimentRation ->
                                                alimentRation.refAlimUnif?.takeIf { it.isNotBlank() }?.let { ids.add(it) }
                                                alimentRation.aliment?.uuid?.takeIf { it.isNotBlank() }?.let { ids.add(it) }
                                        }
                                }
                        }
                        ids
                }
                
                // Collecter toutes les références nutritionnelles utilisées dans les consultations (sur IO)
                val (referenceIds, equationIds) = withContext(AppDispatchers.IO) {
                        val refIds = mutableSetOf<String>()
                        val eqIds = mutableSetOf<String>()
                        
                        animal.consultations.forEach { consultation ->
                                consultation.referenceGeneraleId?.let { refId ->
                                        refIds.add(refId)
                                        val reference = settingsViewModel.referenceEvRepository?.getReferenceEvById(refId)
                                        reference?.let { ref ->
                                                ref.obtenirToutesEquations().forEach { equation ->
                                                        eqIds.add(equation.uuid)
                                                }
                                        }
                                }
                                consultation.referencesMaladies.forEach { refId ->
                                        refIds.add(refId)
                                        val reference = settingsViewModel.referenceEvRepository?.getReferenceEvById(refId)
                                        reference?.let { ref ->
                                                ref.obtenirToutesEquations().forEach { equation ->
                                                        eqIds.add(equation.uuid)
                                                }
                                        }
                                }
                        }
                        Pair(refIds, eqIds)
                }
                
                // Exporter avec sélection (sur IO)
                var jsonContent = withContext(AppDispatchers.IO) {
                        val exportOptions = ExportImportRepository.ExportSelectionOptions(
                                includeAnimals = true,
                                includeFoods = true,
                                includeRations = true,
                                includeRecipes = false,
                                includeEquations = true,
                                includeConseils = false,
                                animalIds = setOf(animal.uuid),
                                foodIds = foodIds,
                                referenceIds = referenceIds,
                                equationIds = equationIds
                        )
                        exportImportRepository.exportWithSelection(exportOptions)
                }
                
                // Anonymiser le JSON si demandé
                if (shouldAnonymize) {
                        jsonContent = withContext(AppDispatchers.IO) {
                                anonymizeExportJson(jsonContent)
                        }
                }
                
                // Générer le nom de fichier
                val fileName = "${animal.id ?: animal.uuid}_${animal.nom}_export.json"
                
                // IMPORTANT: Recharger l'animal depuis la BDD pour s'assurer d'avoir le jsonbinId à jour
                val animalFromDb: AnimalEv? = withContext(AppDispatchers.IO) {
                        settingsViewModel.animalRepository.getAnimalById(animal.uuid)
                }
                
                // Utiliser l'animal de la BDD si disponible, sinon l'animal du ViewModel
                val animalToUse = animalFromDb ?: animal
                
                // Récupérer le binId existant directement depuis la BDD
                val existingBinId = animalToUse.jsonbinId
                
                // Si l'animal du ViewModel n'a pas le jsonbinId mais qu'il existe en BDD, mettre à jour le ViewModel
                if (animalFromDb != null && animal.jsonbinId != animalFromDb.jsonbinId) {
                        withContext(AppDispatchers.Main) {
                                viewModel.setAnimal(animalFromDb)
                        }
                }
                
                // Uploader sur jsonbin.io (sur IO)
                val shareService = fr.vetbrain.vetnutri_mp.Service.createJsonShareService()
                val shareOptions = fr.vetbrain.vetnutri_mp.Service.ShareOptions(
                        fileName = fileName,
                        expiresInHours = 168, // 7 jours par défaut
                        binName = animalToUse.uuid, // Utiliser l'UUID de l'animal comme nom du bin pour identification
                        binId = existingBinId, // Utiliser le binId existant pour mise à jour
                        encryptJson = shouldEncrypt
                )
                
                val shareResult = withContext(AppDispatchers.IO) {
                        shareService.uploadJson(jsonContent, shareOptions)
                }
                
                // Gérer le résultat (sur Main pour l'UI)
                withContext(AppDispatchers.Main) {
                        shareResult.fold(
                                onSuccess = { shareLink ->
                                        // Mettre à jour le binId de l'animal et le sauvegarder en base (sur IO)
                                        val animalToUpdate = animalToUse.copy(jsonbinId = shareLink.binId)
                                        withContext(AppDispatchers.IO) {
                                                viewModel.updateAnimal(animalToUpdate)
                                                
                                                // Recharger l'animal depuis la BDD pour s'assurer que le jsonbinId est bien présent
                                                val updatedAnimal: AnimalEv? = settingsViewModel.animalRepository.getAnimalById(animalToUse.uuid)
                                                if (updatedAnimal != null) {
                                                        withContext(AppDispatchers.Main) {
                                                                viewModel.setAnimal(updatedAnimal)
                                                        }
                                                }
                                        }
                                        onShareLinkGenerated(shareLink)
                                        val message = if (existingBinId != null) {
                                                translate(AnimalDetail.SHARE_SUCCESS)
                                        } else {
                                                translate(AnimalDetail.SHARE_SUCCESS)
                                        }
                                        snackbarHostState.showSnackbar(
                                                message = message,
                                                duration = SnackbarDuration.Short
                                        )
                                },
                                onFailure = { error ->
                                        snackbarHostState.showSnackbar(
                                                message = "${translate(AnimalDetail.SHARE_ERROR)}${error.message}",
                                                duration = SnackbarDuration.Long
                                        )
                                }
                        )
                }
        } catch (e: Exception) {
                e.printStackTrace()
                withContext(AppDispatchers.Main) {
                        snackbarHostState.showSnackbar(
                                message = "${translate(AnimalDetail.SHARE_ERROR)}${e.message}",
                                duration = SnackbarDuration.Long
                        )
                }
        }
}

/**
 * Fonction commune pour l'export PDF depuis la prévisualisation HTML
 */
private fun handlePdfExport(
    previewHtml: String,
    previewMode: String,
    compteRenduText: String,
    animalDetails: AnimalEv,
    selectedConsultation: ConsultationEv?,
    selectedRation: Ration?,
    selectedRationsForPrescription: List<Ration>?,
    referenceUtilisee: fr.vetbrain.vetnutri_mp.Data.ReferenceEv?,
    additionalText: String,
    getSelectedConseils: () -> List<fr.vetbrain.vetnutri_mp.Export.HtmlSection>,
    besoinEnergetiqueStandard: Double?,
    poidsMetabolique: Double?,
    equationRepository: EquationRepository,
    scope: CoroutineScope
) {
    if (previewMode == "CR") {
        scope.launch(fr.vetbrain.vetnutri_mp.Utils.AppDispatchers.IO) {
            PdfExporter.exportHtmlDocument(
                html = previewHtml,
                defaultFileName = "${generateDefaultPdfFileName(animalDetails, selectedConsultation).removeSuffix(".pdf")}_CR.pdf"
            )
        }
        return
    }

    val isPrescription = previewHtml.contains(translate(AnimalDetail.PRESCRIPTION_TITLE))
    if (isPrescription) {
        val rationsForPrescription: List<Ration> = selectedRationsForPrescription ?: selectedConsultation?.rations?.toList()
                ?: emptyList()
        val prefsStorage = createPreferencesStorage()
        val prefsRepo = PreferencesRepository(prefsStorage)
        scope.launch(fr.vetbrain.vetnutri_mp.Utils.AppDispatchers.IO) {
            try {
                prefsRepo.loadPreferences()
                val prefs = prefsRepo.preferences
                val practitioner = fr.vetbrain.vetnutri_mp.Export.PractitionerInfo(
                    nom = prefs.nomUtilisateur,
                    numeroOrdre = prefs.numeroOrdre,
                    adressePostale = prefs.adressePostale,
                    codePostal = prefs.codePostal,
                    ville = prefs.ville,
                    telephone = prefs.telephone,
                    email = prefs.email
                )
                fr.vetbrain.vetnutri_mp.exportPdfDocument(
                    documentType = DocumentType.PRESCRIPTION,
                    data = ExportData(
                        animal = animalDetails,
                        ration = null,
                        reference = referenceUtilisee,
                        conseils = listOf(translate(AnimalDetail.DEFAULT_ADVICE_HYDRATION)),
                        title = translate(AnimalDetail.PRESCRIPTION_TITLE),
                        additionalText = additionalText,
                        htmlSections = getSelectedConseils(),
                        rations = rationsForPrescription,
                        practitioner = practitioner,
                        preferences = null,
                        poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                        poidsMetabolique = null,
                        besoinEnergetiqueEntretien = null
                    ),
                    defaultFileName = generateDefaultPdfFileName(animalDetails, selectedConsultation)
                )
            } catch (e: Exception) {
                fr.vetbrain.vetnutri_mp.exportPdfDocument(
                    documentType = DocumentType.PRESCRIPTION,
                    data = ExportData(
                        animal = animalDetails,
                        ration = null,
                        reference = referenceUtilisee,
                        conseils = emptyList(),
                        title = translate(AnimalDetail.PRESCRIPTION_TITLE),
                        additionalText = additionalText,
                        htmlSections = getSelectedConseils(),
                        rations = rationsForPrescription,
                        practitioner = null,
                        preferences = null,
                        poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                        poidsMetabolique = null,
                        besoinEnergetiqueEntretien = null
                    ),
                    defaultFileName = generateDefaultPdfFileName(animalDetails, selectedConsultation)
                )
            }
        }
    } else {
        scope.launch(AppDispatchers.Default) {
            // Export analyse de ration avec bullet graphs
            val bulletGraphImages = mutableMapOf<String, Map<String, String>>()

            selectedRation?.let { ration: Ration ->
                try {
                    val prefsStorage = createPreferencesStorage()
                    val prefsRepo = PreferencesRepository(prefsStorage)
                    prefsRepo.loadPreferences()
                    val prefs = prefsRepo.preferences
                    val prefsEspece = prefs?.getPreferencesEspece(animalDetails.getEspece())

                    val ref = referenceUtilisee
                    if (prefsEspece != null && ref != null) {
                        val images =
                            fr.vetbrain.vetnutri_mp.Export.BulletGraphImageCapture
                                .generateRationBulletGraphImages(
                                    ration = ration,
                                    reference = ref,
                                    animal = animalDetails,
                                    preferences = prefsEspece,
                                    poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                                    poidsMetabolique = poidsMetabolique,
                                    besoinEnergetiqueEntretien = besoinEnergetiqueStandard,
                                    equationRepository = equationRepository
                                )

                        val imagePaths =
                            images.mapValues { (_, imageBytes) ->
                                val tempFilePath =
                                    fr.vetbrain.vetnutri_mp.Export.BulletGraphImageCapture
                                        .saveImageToTempFile(imageBytes, "export")
                                "file://$tempFilePath"
                            }

                        bulletGraphImages[ration.uuid] = imagePaths
                    } else {
                        // Générer des images de test
                        val testImages = mutableMapOf<String, ByteArray>()
                        listOf("PROTEINE", "LIPIDE", "ENA", "CELLULOSE", "CENDRE", "CAL", "PHOS")
                            .forEach { nom ->
                                val imageBytes =
                                    fr.vetbrain.vetnutri_mp.Export.BulletGraphImageCapture
                                        .generateBulletGraphImage(
                                            nom, 25.0, 15.0, 40.0, 20.0, 35.0, "g/kg DM"
                                        )
                                testImages[nom] = imageBytes
                            }

                        val testImagePaths =
                            testImages.mapValues { (_, imageBytes) ->
                                val tempFilePath =
                                    fr.vetbrain.vetnutri_mp.Export.BulletGraphImageCapture
                                        .saveImageToTempFile(imageBytes, "export_test")
                                "file://$tempFilePath"
                            }

                        bulletGraphImages[ration.uuid] = testImagePaths
                    }
                } catch (_: Exception) {}
            }

            PdfExporter.exportDocument(
                DocumentType.RATION_ANALYSIS,
                ExportData(
                    animal = animalDetails,
                    ration = selectedRation,
                    reference = referenceUtilisee,
                    title = translate(AnimalDetail.RATION_ANALYSIS_TITLE),
                    additionalText = additionalText,
                    htmlSections = getSelectedConseils(),
                    preferences = null,
                    poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                    poidsMetabolique = null,
                    besoinEnergetiqueEntretien = null,
                    bulletGraphImages = bulletGraphImages
                ),
                defaultFileName = "analyse_ration.pdf"
            )
        }
    }
}

/**
 * Vue principale pour afficher les détails d'un animal
 *
 * @param viewModel ViewModel contenant les données de l'animal
 * @param settingsViewModel ViewModel pour les paramètres
 * @param onNavigateBack Action à exécuter pour revenir à l'écran précédent
 * @param onOpenSettings Action à exécuter pour ouvrir les paramètres
 * @param modifier Modificateur optionnel pour personnaliser l'apparence
 */
@Composable
fun AnimalDetailView(
        viewModel: AnimalDetailViewModel,
        settingsViewModel: SettingsViewModel,
        onNavigateBack: () -> Unit,
        onOpenSettings: () -> Unit = {},
        modifier: Modifier = Modifier,
        equationRepository: EquationRepository,
        recipeRepository: RecipeRepository,
        conseilRepository: fr.vetbrain.vetnutri_mp.Repository.ConseilRepository,
        isExamMode: Boolean = false
) {
        val animal by viewModel.animal.collectAsState()
        val currentSection by viewModel.currentSection.collectAsState()
        val showFullScreenEdit by viewModel.showFullScreenEdit.collectAsState()
        val selectedConsultation by viewModel.selectedConsultation.collectAsState()
        var showConsultationDetail by remember { mutableStateOf(false) }

        // Récupération des préférences pour l'espèce
        val preferencesStorage: fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage = remember {
                createPreferencesStorage()
        }
        val preferencesRepository: PreferencesRepository = remember {
                PreferencesRepository(preferencesStorage)
        }
        var preferencesApplication by remember {
                mutableStateOf<fr.vetbrain.vetnutri_mp.Data.PreferencesApplication?>(null)
        }

        // Charger les préférences au démarrage
        LaunchedEffect(Unit) {
                preferencesRepository.loadPreferences()
                preferencesApplication = preferencesRepository.preferences
        }

        // État du drawer pour les écrans étroits
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        // État pour les messages Snackbar
        val snackbarHostState = remember { SnackbarHostState() }

        // Effet pour détecter les changements de section et sauvegarder automatiquement
        LaunchedEffect(currentSection) {
                // Si on quitte la section consultation et qu'une édition plein écran est en cours
                val currentConsultation = selectedConsultation
                if (currentSection != AnimalDetailSection.CONSULTATIONS &&
                                showFullScreenEdit &&
                                currentConsultation != null
                ) {
                        // Sauvegarder automatiquement la consultation en cours d'édition
                        viewModel.saveFromFullScreen(currentConsultation)
                }
        }

        // Fonction pour gérer la navigation avec sauvegarde automatique
        val handleNavigateBack: () -> Unit = {
                val currentConsultation = selectedConsultation
                if (showFullScreenEdit && currentConsultation != null) {
                        // Sauvegarder avant de naviguer
                        scope.launch {
                                viewModel.saveFromFullScreen(currentConsultation)
                                onNavigateBack()
                        }
                } else {
                        onNavigateBack()
                }
        }

        // Fonction pour gérer l'ouverture des paramètres avec sauvegarde automatique
        val handleOpenSettings: () -> Unit = {
                val currentConsultation = selectedConsultation
                if (showFullScreenEdit && currentConsultation != null) {
                        // Sauvegarder avant d'ouvrir les paramètres
                        scope.launch {
                                viewModel.saveFromFullScreen(currentConsultation)
                                onOpenSettings()
                        }
                } else {
                        onOpenSettings()
                }
        }

        // Options du menu
        val menuOptions =
                listOf(
                        MenuOption(
                                section = AnimalDetailSection.IDENTIFICATION,
                                title = translate(AnimalDetail.IDENTIFICATION),
                                icon = Icons.Default.Person
                        ),
                        MenuOption(
                                section = AnimalDetailSection.CONSULTATIONS,
                                title = translate(AnimalDetail.CONSULTATIONS),
                                icon = Icons.Default.Info
                        ),
                        MenuOption(
                                section = AnimalDetailSection.RATIONS,
                                title = translate(AnimalDetail.RATIONS),
                                icon = Icons.AutoMirrored.Filled.List
                        ),
                        MenuOption(
                                section = AnimalDetailSection.GRAPHIQUE,
                                title = translate(AnimalDetail.GRAPH),
                                icon = AppIcons.Analytics
                        ),
                        MenuOption(
                                section = AnimalDetailSection.GRAPHIQUE_ALIMENTS,
                                title = translate(AnimalDetail.FOOD_GRAPH),
                                icon = AppIcons.Analytics
                        ),
                        MenuOption(
                                section = AnimalDetailSection.EXPORT,
                                title = translate(AnimalDetail.EXPORT),
                                icon = Icons.Default.Settings
                        )
                )

        var showDeleteConfirmation by remember { mutableStateOf(false) }
        var isEditing by remember { mutableStateOf(false) }

        animal?.let { animalDetails ->
                // Utiliser BoxWithConstraints pour déterminer si l'écran est large
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val isWideScreen = maxWidth > AppSizes.breakpointWideScreen

                        if (isWideScreen) {
                                // Layout pour écrans larges avec sidebar permanente
                                WideScreenLayout(
                                        animalDetails = animalDetails,
                                        currentSection = currentSection,
                                        menuOptions = menuOptions,
                                        onNavigateBack = handleNavigateBack,
                                        onOpenSettings = handleOpenSettings,
                                        viewModel = viewModel,
                                        settingsViewModel = settingsViewModel,
                                        isEditing = isEditing,
                                        onIsEditingChange = { isEditing = it },
                                        onShowDeleteConfirmation = {
                                                showDeleteConfirmation = true
                                        },
                                        showConsultationDetail = showConsultationDetail,
                                        onShowConsultationDetail = { showConsultationDetail = it },
                                        equationRepository = equationRepository,
                                        recipeRepository = recipeRepository,
                                        conseilRepository = conseilRepository,
                                        isExamMode = isExamMode
                                )
                        } else {
                                // Layout pour écrans étroits avec drawer
                                NarrowScreenLayout(
                                        animalDetails = animalDetails,
                                        currentSection = currentSection,
                                        menuOptions = menuOptions,
                                        onNavigateBack = handleNavigateBack,
                                        onOpenSettings = handleOpenSettings,
                                        viewModel = viewModel,
                                        settingsViewModel = settingsViewModel,
                                        isEditing = isEditing,
                                        onIsEditingChange = { isEditing = it },
                                        onShowDeleteConfirmation = {
                                                showDeleteConfirmation = true
                                        },
                                        showConsultationDetail = showConsultationDetail,
                                        onShowConsultationDetail = { showConsultationDetail = it },
                                        drawerState = drawerState,
                                        scope = scope,
                                        equationRepository = equationRepository,
                                        recipeRepository = recipeRepository,
                                        conseilRepository = conseilRepository,
                                        isExamMode = isExamMode
                                )
                        }

                        // Boîte de dialogue de confirmation de suppression
                        if (showDeleteConfirmation) {
                                ConfirmDialog(
                                        title = translate(General.CONFIRM_DELETE),
                                        message = translate(AnimalDetail.DELETE_CONFIRM_MESSAGE),
                                        onConfirm = {
                                                // Sauvegarder automatiquement si une édition est en
                                                // cours
                                                val currentConsultation = selectedConsultation
                                                if (showFullScreenEdit &&
                                                                currentConsultation != null
                                                ) {
                                                        scope.launch {
                                                                viewModel.saveFromFullScreen(
                                                                        currentConsultation
                                                                )
                                                                // Puis supprimer l'animal
                                                                val success =
                                                                        viewModel.deleteAnimal()
                                                                if (success) {
                                                                        onNavigateBack()
                                                                }
                                                                showDeleteConfirmation = false
                                                        }
                                                } else {
                                                        // Appeler la fonction de suppression du
                                                        // ViewModel
                                                        val success = viewModel.deleteAnimal()
                                                        if (success) {
                                                                // Naviguer vers la liste des
                                                                // animaux
                                                                onNavigateBack()
                                                        }
                                                        showDeleteConfirmation = false
                                                }
                                        },
                                        onDismiss = { showDeleteConfirmation = false }
                                )
                        }
                }
        }
}

/** Layout pour les écrans larges avec une sidebar permanente */
@Composable
private fun WideScreenLayout(
        animalDetails: AnimalEv,
        currentSection: AnimalDetailSection,
        menuOptions: List<MenuOption>,
        onNavigateBack: () -> Unit,
        onOpenSettings: () -> Unit,
        viewModel: AnimalDetailViewModel,
        settingsViewModel: SettingsViewModel,
        isEditing: Boolean,
        onIsEditingChange: (Boolean) -> Unit,
        onShowDeleteConfirmation: () -> Unit,
        showConsultationDetail: Boolean,
        onShowConsultationDetail: (Boolean) -> Unit,
        equationRepository: EquationRepository,
        recipeRepository: RecipeRepository,
        conseilRepository: fr.vetbrain.vetnutri_mp.Repository.ConseilRepository,
        isExamMode: Boolean = false
) {
        // Scope pour les coroutines
        val scope = rememberCoroutineScope()
        
        // État pour les messages Snackbar
        val snackbarHostState = remember { SnackbarHostState() }
        
        // État pour le partage en ligne
        var shareLink by remember { mutableStateOf<fr.vetbrain.vetnutri_mp.Service.ShareLink?>(null) }
        var showShareDialog by remember { mutableStateOf(false) }
        var showAnonymizationDialog by remember { mutableStateOf(false) }
        val shareLauncher = fr.vetbrain.vetnutri_mp.Utils.rememberShareLauncher()
        
        // État pour l'éditeur de texte enrichi
        var currentHtmlContent by remember {
                mutableStateOf(fr.vetbrain.vetnutri_mp.Export.RichTextContent())
        }
        var showRichTextEditor by remember { mutableStateOf(false) }

        // État pour les conseils personnalisés (sauvegardés)
        var availableConseils by remember {
                mutableStateOf<List<fr.vetbrain.vetnutri_mp.Export.HtmlSection>>(emptyList())
        }
        // État pour les sections HTML créées localement (temporaires)
        var localHtmlSections by remember {
                mutableStateOf<List<fr.vetbrain.vetnutri_mp.Export.HtmlSection>>(emptyList())
        }
        var selectedConseils by remember {
                mutableStateOf<List<fr.vetbrain.vetnutri_mp.Export.HtmlSection>>(emptyList())
        }
        var isLoadingConseils by remember { mutableStateOf(true) }
        var searchQuery by remember { mutableStateOf("") }
        var showSearchDialog by remember { mutableStateOf(false) }
        var pendingCopyText by remember { mutableStateOf<String?>(null) }
        var anamneseText by remember { mutableStateOf("") }
        var examenCliniqueText by remember { mutableStateOf("") }
        var facteurNutritionnelClefText by remember { mutableStateOf("") }
        var additionalText by remember { mutableStateOf("") }
        var practitionerContact by remember { mutableStateOf(PractitionerContact()) }
        val selectedConsultation by viewModel.selectedConsultation.collectAsState()
        var selectedRationIdsForPrescription by remember(selectedConsultation?.uuid) {
                val initialSelection: Set<String> =
                        selectedConsultation?.rations
                                ?.filter { ration: Ration -> !ration.actual }
                                ?.map { ration: Ration -> ration.uuid }
                                ?.toSet() ?: emptySet()
                mutableStateOf(initialSelection)
        }
        var savePrescriptionJob by remember(selectedConsultation?.uuid) {
                mutableStateOf<Job?>(null)
        }

        fun schedulePrescriptionSave() {
                val consultation = selectedConsultation ?: return
                savePrescriptionJob?.cancel()
                savePrescriptionJob =
                        scope.launch {
                                delay(400)
                                val updatedConsultation =
                                        consultation.copy(
                                                prescriptionAnamnese = anamneseText,
                                                prescriptionExamenClinique = examenCliniqueText,
                                                prescriptionFacteurNutritionnelClef =
                                                        facteurNutritionnelClefText,
                                                prescriptionAdditionalText = additionalText,
                                                prescriptionSelectedConseilIds =
                                                        selectedConseils
                                                                .map { it.id }
                                                                .toMutableList(),
                                                prescriptionLocalHtmlSections =
                                                        localHtmlSections.toMutableList(),
                                                prescriptionSelectedRationIds =
                                                        selectedRationIdsForPrescription
                                                                .toMutableList()
                                        )
                                if (updatedConsultation != consultation) {
                                        viewModel.updateConsultation(updatedConsultation)
                                }
                        }
        }

        LaunchedEffect(selectedConsultation?.uuid) {
                val consultation = selectedConsultation
                if (consultation == null) {
                        anamneseText = ""
                        examenCliniqueText = ""
                        facteurNutritionnelClefText = ""
                        additionalText = ""
                        localHtmlSections = emptyList()
                        selectedConseils = emptyList()
                        selectedRationIdsForPrescription = emptySet()
                        return@LaunchedEffect
                }
                anamneseText = consultation.prescriptionAnamnese
                examenCliniqueText = consultation.prescriptionExamenClinique
                facteurNutritionnelClefText = consultation.prescriptionFacteurNutritionnelClef
                additionalText = consultation.prescriptionAdditionalText
                localHtmlSections = consultation.prescriptionLocalHtmlSections
                if (consultation.prescriptionSelectedRationIds.isNotEmpty()) {
                        selectedRationIdsForPrescription =
                                consultation.prescriptionSelectedRationIds.toSet()
                }
                val selectedConseilIds =
                        consultation.prescriptionSelectedConseilIds.toSet()
                selectedConseils =
                        if (selectedConseilIds.isEmpty()) {
                                emptyList()
                        } else {
                                availableConseils.filter { it.id in selectedConseilIds }
                        }
        }

        LaunchedEffect(availableConseils, selectedConsultation?.uuid) {
                val consultation = selectedConsultation ?: return@LaunchedEffect
                if (selectedConseils.isNotEmpty()) {
                        return@LaunchedEffect
                }
                if (consultation.prescriptionSelectedConseilIds.isEmpty()) {
                        return@LaunchedEffect
                }
                val selectedConseilIds =
                        consultation.prescriptionSelectedConseilIds.toSet()
                selectedConseils =
                        availableConseils.filter { it.id in selectedConseilIds }
        }
        // Charger les conseils personnalisés
        LaunchedEffect(Unit) {
                if (isExamMode) {
                        availableConseils = emptyList()
                        isLoadingConseils = false
                        return@LaunchedEffect
                }
                try {
                        val result = conseilRepository.getConseilsActifs()
                        if (result.isSuccess) {
                                availableConseils = result.getOrThrow()
                        }
                } catch (e: Exception) {
                        e.printStackTrace()
                } finally {
                        isLoadingConseils = false
                }
        }
        LaunchedEffect(Unit) {
                try {
                        val prefsStorage = createPreferencesStorage()
                        val prefsRepo = PreferencesRepository(prefsStorage)
                        prefsRepo.loadPreferences()
                        val prefs = prefsRepo.preferences
                        practitionerContact =
                                PractitionerContact(
                                        nom = prefs.nomUtilisateur,
                                        numeroOrdre = prefs.numeroOrdre,
                                        adressePostale = prefs.adressePostale,
                                        codePostal = prefs.codePostal,
                                        ville = prefs.ville,
                                        telephone = prefs.telephone,
                                        email = prefs.email
                                )
                } catch (_: Exception) {}
        }
        
        // Variables pour la prévisualisation et l'export
        var showPreview by remember {
                mutableStateOf(false)
        }
        var previewHtml by remember {
                mutableStateOf("")
        }
        var previewMode by remember { mutableStateOf("PRESCRIPTION") }
        var previewCompteRenduText by remember { mutableStateOf("") }
        
        // Fonction pour récupérer les conseils sélectionnés (conseils + sections locales)
        val getSelectedConseils:
                () -> List<fr.vetbrain.vetnutri_mp.Export.HtmlSection> =
                {
                        selectedConseils + localHtmlSections
                }
        
        val examBorderModifier =
                if (isExamMode) {
                        Modifier.border(1.dp, Color.Red)
                } else {
                        Modifier
                }

        Box(
                modifier =
                        Modifier.fillMaxSize().then(examBorderModifier)
                                .padding(if (isExamMode) 2.dp else 0.dp)
        ) {
        Row(modifier = Modifier.fillMaxSize()) {
                // Sidebar
                Column(
                        modifier =
                                Modifier.width(250.dp)
                                        .fillMaxHeight()
                                        .verticalScroll(rememberScrollState())
                                        .padding(AppSizes.paddingMedium),
                        verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                ) {
                        // En-tête avec nom et espèce de l'animal
                        Column(modifier = Modifier.fillMaxWidth()) {
                                Text(text = animalDetails.nom, style = MaterialTheme.typography.h5)
                                if (isExamMode && !animalDetails.examExerciseId.isNullOrBlank()) {
                                        Text(
                                                text =
                                                        "ID exercice: ${animalDetails.examExerciseId}",
                                                style = MaterialTheme.typography.body2,
                                                color = VetNutriColors.Primary
                                        )
                                }
                                Text(
                                        text = animalDetails.getEspece().translateEnum(),
                                        style = MaterialTheme.typography.subtitle1,
                                        color = Color.Gray
                                )
                        }

                        Divider()

                        // Options du menu
                        menuOptions.forEach { option ->
                                MenuOptionItem(
                                        option = option,
                                        isSelected = currentSection == option.section,
                                        onClick = { viewModel.navigateTo(option.section) }
                                )
                        }

                        Spacer(modifier = Modifier.weight(1.0f))

                        // Bouton exporter animal
                        Button(
                                onClick = {
                                        scope.launch {
                                                exporterAnimalComplet(
                                                        animal = animalDetails,
                                                        viewModel = viewModel,
                                                        settingsViewModel = settingsViewModel,
                                                        equationRepository = equationRepository,
                                                        recipeRepository = recipeRepository,
                                                        conseilRepository = conseilRepository,
                                                        snackbarHostState = snackbarHostState
                                                )
                                        }
                                },
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Primary,
                                                contentColor = VetNutriColors.OnPrimary
                                        ),
                                modifier = Modifier.fillMaxWidth()
                        ) {
                                Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = translate(AnimalDetail.EXPORT_ANIMAL)
                                )
                                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                Text(text = translate(AnimalDetail.EXPORT_ANIMAL))
                        }
                        
                        // Bouton partager en ligne
                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                        Button(
                                onClick = {
                                        showAnonymizationDialog = true
                                },
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Secondary,
                                                contentColor = VetNutriColors.OnSecondary
                                        ),
                                modifier = Modifier.fillMaxWidth()
                        ) {
                                Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = translate(AnimalDetail.SHARE_ONLINE)
                                )
                                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                Text(text = translate(AnimalDetail.SHARE_ONLINE))
                        }

                        // Bouton retour
                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                        Button(
                                onClick = onNavigateBack,
                                colors =
                                        ButtonDefaults.buttonColors(
                                                backgroundColor = VetNutriColors.Secondary,
                                                contentColor = VetNutriColors.OnSecondary
                                        ),
                                modifier = Modifier.fillMaxWidth()
                        ) {
                                Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = translate(AnimalDetail.BACK)
                                )
                                Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                Text(text = translate(AnimalDetail.BACK))
                        }

                        // Ajout de l'option Paramètres en bas du menu
                        Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                        MenuOptionItem(
                                option =
                                        MenuOption(
                                                section = AnimalDetailSection.IDENTIFICATION,
                                                title = translate(Settings.TITLE),
                                                icon = Icons.Default.Settings
                                        ),
                                isSelected = false,
                                onClick = onOpenSettings
                        )
                }

                // Contenu principal avec SnackbarHost
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        SnackbarHost(hostState = snackbarHostState)
                        
                        // Dialog d'anonymisation
                        if (showAnonymizationDialog) {
                                AnonymizationDialog(
                                        onConfirm = { shouldAnonymize, shouldEncrypt ->
                                                showAnonymizationDialog = false
                                                scope.launch {
                                                        partagerAnimalEnLigne(
                                                                animal = animalDetails,
                                                                viewModel = viewModel,
                                                                settingsViewModel = settingsViewModel,
                                                                equationRepository = equationRepository,
                                                                recipeRepository = recipeRepository,
                                                                conseilRepository = conseilRepository,
                                                                snackbarHostState = snackbarHostState,
                                                                onShareLinkGenerated = { link ->
                                                                        shareLink = link
                                                                        showShareDialog = true
                                                                },
                                                                shouldAnonymize = shouldAnonymize,
                                                                shouldEncrypt = shouldEncrypt
                                                        )
                                                }
                                        },
                                        onDismiss = {
                                                showAnonymizationDialog = false
                                        }
                                )
                        }
                        
                        // Dialog de partage avec QR Code
                        shareLink?.let { link ->
                                if (showShareDialog) {
                                        ShareLinkDialog(
                                                shareLink = link,
                                                onDismiss = {
                                                        showShareDialog = false
                                                        shareLink = null
                                                },
                                                onShare = { shareLauncher(link.qrCodeData ?: link.binId) }
                                        )
                                }
                        }
                        when (currentSection) {
                                AnimalDetailSection.IDENTIFICATION -> {
                                        if (isEditing) {
                                                AnimalEditView(
                                                        animal = animalDetails,
                                                        onSave = { updatedAnimal ->
                                                                viewModel.updateAnimal(
                                                                        updatedAnimal
                                                                )
                                                                onIsEditingChange(false)
                                                        },
                                                        onCancel = { onIsEditingChange(false) },
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                AppSizes.paddingMedium
                                                                        )
                                                )
                                        } else {
                                                AnimalIdentificationView(
                                                        animal = animalDetails,
                                                        onEdit = { onIsEditingChange(true) },
                                                        onDelete = onShowDeleteConfirmation,
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                AppSizes.paddingMedium
                                                                        )
                                                )
                                        }
                                }
                                AnimalDetailSection.CONSULTATIONS -> {
                                        ConsultationsView(
                                                viewModel = viewModel,
                                                showConsultationDetail = showConsultationDetail,
                                                onShowConsultationDetail = onShowConsultationDetail,
                                                modifier = Modifier.fillMaxSize()
                                        )
                                }
                                AnimalDetailSection.RATIONS -> {
                                        RationsView(
                                                viewModel = viewModel,
                                                showSnackbar = { message -> },
                                                equationRepository = equationRepository,
                                                recipeRepository = recipeRepository,
                                                isExamMode = isExamMode
                                        )
                                }
                                AnimalDetailSection.GRAPHIQUE -> {
                                        AnalyseGraphiqueView(
                                                viewModel = viewModel,
                                                equationRepository = equationRepository,
                                                modifier = Modifier.fillMaxSize()
                                        )
                                }
                                AnimalDetailSection.GRAPHIQUE_ALIMENTS -> {
                                        val availableFoods by
                                                viewModel.availableFoods.collectAsState()
                                        val isLoadingFoods by
                                                viewModel.isLoadingFoods.collectAsState()

                                        // Récupération des préférences pour l'espèce dans ce
                                        // contexte
                                        val preferencesStorageLocal:
                                                fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage =
                                                remember {
                                                        createPreferencesStorage()
                                                }
                                        val preferencesRepositoryLocal: PreferencesRepository =
                                                remember {
                                                        PreferencesRepository(
                                                                preferencesStorageLocal
                                                        )
                                                }
                                        var preferencesApplicationLocal by remember {
                                                mutableStateOf<
                                                        fr.vetbrain.vetnutri_mp.Data.PreferencesApplication?>(
                                                        null
                                                )
                                        }

                                        // Charger les préférences au démarrage
                                        LaunchedEffect(Unit) {
                                                preferencesRepositoryLocal.loadPreferences()
                                                preferencesApplicationLocal =
                                                        preferencesRepositoryLocal.preferences
                                        }

                                        if (isLoadingFoods) {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                AppSizes.paddingMedium
                                                                        ),
                                                        verticalArrangement = Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        CircularProgressIndicator(
                                                                color = VetNutriColors.Primary
                                                        )
                                                        Spacer(
                                                                modifier =
                                                                        Modifier.height(
                                                                                AppSizes.paddingMedium
                                                                        )
                                                        )
                                                        Text(
                                                                translate(AnimalDetail.LOADING_FOODS),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body1,
                                                                color =
                                                                        MaterialTheme.colors
                                                                                .onSurface.copy(
                                                                                alpha = 0.7f
                                                                        )
                                                        )
                                                }
                                        } else if (availableFoods.isNotEmpty()) {
                                                // ✨ Utiliser les états du ViewModel pour persister
                                                // la sélection
                                                val showAnalyseGraphique by
                                                        viewModel.showAnalyseGraphique
                                                                .collectAsState()
                                                val alimentsSelectionnes by
                                                        viewModel.alimentsSelectionnes
                                                                .collectAsState()
                                                val analyseSelectionFilters by
                                                        viewModel.analyseSelectionFilters
                                                                .collectAsState()

                                                if (showAnalyseGraphique &&
                                                                alimentsSelectionnes.isNotEmpty()
                                                ) {
                                                        // Afficher la vue d'analyse graphique
                                                        // Récupérer les aliments complets avec
                                                        // leurs valeurs nutritionnelles
                                                        var alimentsComplets by remember {
                                                                mutableStateOf<
                                                                        List<
                                                                                fr.vetbrain.vetnutri_mp.Data.AlimentEv>>(
                                                                        emptyList()
                                                                )
                                                        }
                                                        var isLoadingAlimentsComplets by remember {
                                                                mutableStateOf(true)
                                                        }

                                                        LaunchedEffect(alimentsSelectionnes) {
                                                                isLoadingAlimentsComplets = true
                                                                val alimentsAvecValeurs =
                                                                        mutableListOf<
                                                                                fr.vetbrain.vetnutri_mp.Data.AlimentEv>()

                                                                for (aliment in
                                                                        alimentsSelectionnes) {
                                                                        try {

                                                                                // Récupérer
                                                                                // l'aliment complet
                                                                                // depuis le
                                                                                // repository
                                                                                val alimentComplet =
                                                                                        viewModel.getAlimentComplet(
                                                                                                aliment.uuid
                                                                                        )

                                                                                if (alimentComplet !=
                                                                                                null
                                                                                ) {
                                                                                        alimentsAvecValeurs
                                                                                                .add(
                                                                                                        alimentComplet
                                                                                                )
                                                                                } else {

                                                                                        alimentsAvecValeurs
                                                                                                .add(
                                                                                                        aliment
                                                                                                ) // Fallback
                                                                                }
                                                                        } catch (e: Exception) {

                                                                                e.printStackTrace()
                                                                                alimentsAvecValeurs
                                                                                        .add(
                                                                                                aliment
                                                                                        ) // Fallback
                                                                        }
                                                                }

                                                                alimentsComplets =
                                                                        alimentsAvecValeurs
                                                                isLoadingAlimentsComplets = false
                                                        }

                                                        if (isLoadingAlimentsComplets) {
                                                                Box(
                                                                        modifier =
                                                                                Modifier.fillMaxSize(),
                                                                        contentAlignment =
                                                                                Alignment.Center
                                                                ) {
                                                                        Column(
                                                                                horizontalAlignment =
                                                                                        Alignment
                                                                                                .CenterHorizontally,
                                                                                verticalArrangement =
                                                                                        Arrangement
                                                                                                .spacedBy(
                                                                                                        AppSizes.paddingMedium
                                                                                                )
                                                                        ) {
                                                                                CircularProgressIndicator(
                                                                                        color =
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                )
                                                                                Text(
                                                                                        text =
                                                                                                translate(AnimalDetail.LOADING_NUTRITION),
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .body1,
                                                                                        color =
                                                                                                MaterialTheme
                                                                                                        .colors
                                                                                                        .onSurface
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.7f
                                                                                                        )
                                                                                )
                                                                        }
                                                                }
                                                        } else {
                                                                AnalyseGraphiqueAlimentsView(
                                                                        aliments = alimentsComplets,
                                                                        referenceEv =
                                                                                viewModel
                                                                                        .referenceUtilisee
                                                                                        .value,
                                                                        equationRepository =
                                                                                equationRepository,
                                                                        preferencesEspece =
                                                                                animalDetails
                                                                                        ?.let {
                                                                                                animal
                                                                                                ->
                                                                                                preferencesApplicationLocal
                                                                                                        ?.getPreferencesEspece(
                                                                                                                animal.getEspece()
                                                                                                        )
                                                                                        },
                                                                        viewModel = viewModel,
                                                                        onClose = {
                                                                                viewModel
                                                                                        .hideAnalyseGraphique()
                                                                        },
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                )
                                                        }
                                                } else {
                                                        // Utiliser la vue de sélection des aliments
                                                        // avec possibilité d'analyse graphique
                                                        AnalyseSelectionAlimentsView(
                                                                aliments = availableFoods,
                                                                onClose = { /* Retour à la section précédente */
                                                                },
                                                                onAlimentSelected = { /* Gestion de la sélection */
                                                                },
                                                                onPrimaryAction = { aliments ->
                                                                        viewModel
                                                                                .lancerAnalyseGraphique(
                                                                                        aliments
                                                                                )
                                                                },
                                                                primaryActionLabel = "Voir l'analyse graphique",
                                                                alimentsInitialementSelectionnes =
                                                                        alimentsSelectionnes,
                                                                onSelectionChanged = {
                                                                        nouvelleSelection ->
                                                                        viewModel
                                                                                .setAlimentsSelectionnes(
                                                                                        nouvelleSelection
                                                                                )
                                                                }, // ✨ Synchroniser avec le
                                                                // ViewModel
                                                                filtersInitial =
                                                                        analyseSelectionFilters,
                                                                onFiltersChange = {
                                                                        viewModel
                                                                                .setAnalyseSelectionFilters(
                                                                                        it
                                                                                )
                                                                },
                                                                onLoadNutrients = { foodUuids, nutrients ->
                                                                        viewModel.loadNutrientsForFoods(foodUuids, nutrients)
                                                                },
                                                                modifier = Modifier.fillMaxSize()
                                                        )
                                                }
                                        } else {
                                                Column(
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                AppSizes.paddingMedium
                                                                        ),
                                                        verticalArrangement = Arrangement.Center,
                                                        horizontalAlignment =
                                                                Alignment.CenterHorizontally
                                                ) {
                                                        Text(
                                                                translate(AnimalDetail.NO_FOOD_AVAILABLE),
                                                                style = MaterialTheme.typography.h5,
                                                                color = VetNutriColors.Primary
                                                        )
                                                        Text(
                                                                translate(AnimalDetail.NO_FOOD_GRAPH_AVAILABLE),
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .body1,
                                                                color =
                                                                        MaterialTheme.colors
                                                                                .onSurface.copy(
                                                                                alpha = 0.7f
                                                                        )
                                                        )
                                                }
                                        }
                                }
                                AnimalDetailSection.EXPORT -> {
                                        val selectedRation by
                                                viewModel.selectedRation.collectAsState()
                                        val referenceUtilisee by
                                                viewModel.referenceUtilisee.collectAsState()
                                        val besoinEnergetiqueStandard by viewModel.besoinEnergetiqueStandard.collectAsState()
                                        val poidsMetabolique by viewModel.poidsMetabolique.collectAsState()
                                        if (showRichTextEditor) {
                                                // Éditeur de texte enrichi
                                                Column(modifier = Modifier.fillMaxSize()) {
                                                        Row(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .padding(
                                                                                        AppSizes.paddingMedium
                                                                                ),
                                                                horizontalArrangement =
                                                                        Arrangement.SpaceBetween,
                                                                verticalAlignment =
                                                                        Alignment.CenterVertically
                                                        ) {
                                                                Text(
                                                                        translate(AnimalDetail.HTML_EDITOR),
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .h6,
                                                                        color =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                )
                                                                Button(
                                                                        onClick = {
                                                                                showRichTextEditor =
                                                                                        false
                                                                        },
                                                                        colors =
                                                                                ButtonDefaults
                                                                                        .buttonColors(
                                                                                                backgroundColor =
                                                                                                        VetNutriColors
                                                                                                                .Secondary,
                                                                                                contentColor =
                                                                                                        VetNutriColors
                                                                                                                .OnSecondary
                                                                                        )
                                                                ) { Text(translate(AnimalDetail.BACK_TO_EXPORT)) }
                                                        }

                                                        RichTextEditor(
                                                                initialContent = currentHtmlContent,
                                                                onContentChange = { content ->
                                                                        currentHtmlContent = content
                                                                },
                                                                modifier = Modifier.weight(1f)
                                                        )

                                                        // Boutons d'action
                                                        Row(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .padding(
                                                                                        AppSizes.paddingMedium
                                                                                ),
                                                                horizontalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        ) {
                                                                Button(
                                                                        onClick = {
                                                                                // Créer une
                                                                                // nouvelle section
                                                                                // HTML
                                                                                val newSection =
                                                                                        fr.vetbrain
                                                                                                .vetnutri_mp
                                                                                                .Export
                                                                                                .HtmlSection(
                                                                                                        id =
                                                                                                                "section_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}",
                                                                                                        title =
                                                                                                                translate(
                                                                                                                        AnimalDetail.CUSTOM_SECTION_TITLE,
                                                                                                                        (availableConseils.size + 1).toString()
                                                                                                                ),
                                                                                                        content =
                                                                                                                currentHtmlContent,
                                                                                                        category =
                                                                                                                fr.vetbrain
                                                                                                                        .vetnutri_mp
                                                                                                                        .Export
                                                                                                                        .SectionCategory
                                                                                                                        .CUSTOM
                                                                                                )
                                                                                // Ajouter à la
                                                                                // liste des
                                                                                // sections HTML
                                                                                // locales
                                                                                localHtmlSections =
                                                                                        localHtmlSections +
                                                                                                newSection
                                                                                schedulePrescriptionSave()
                                                                                currentHtmlContent =
                                                                                        fr.vetbrain
                                                                                                .vetnutri_mp
                                                                                                .Export
                                                                                                .RichTextContent()
                                                                                showRichTextEditor =
                                                                                        false
                                                                        },
                                                                        enabled =
                                                                                currentHtmlContent
                                                                                        .blocks
                                                                                        .isNotEmpty()
                                                                ) { Text(translate(AnimalDetail.ADD_SECTION)) }

                                                                OutlinedButton(
                                                                        onClick = {
                                                                                currentHtmlContent =
                                                                                        fr.vetbrain
                                                                                                .vetnutri_mp
                                                                                                .Export
                                                                                                .RichTextContent()
                                                                        }
                                                                ) { Text(translate(AnimalDetail.CLEAR_SECTION)) }
                                                        }
                                                }
                                        } else {
                                                // Section export normale
                                                LazyColumn(
                                                        modifier =
                                                                Modifier.fillMaxSize()
                                                                        .padding(
                                                                                AppSizes.paddingMedium
                                                                        ),
                                                        verticalArrangement =
                                                                Arrangement.spacedBy(
                                                                        AppSizes.paddingMedium
                                                                )
                                                ) {
                                                        item {
                                                        Text(
                                                                translate(AnimalDetail.EXPORT_DOCUMENTS_TITLE),
                                                                style = MaterialTheme.typography.h6,
                                                                color = VetNutriColors.Primary
                                                        )
                                                        }
                                                        // Ligne d'information sur la ration sélectionnée supprimée pour alléger l'UI
                                                        item {
                                                        if (selectedConsultation == null) {
                                                                Text(
                                                                        translate(AnimalDetail.NO_CONSULTATION_FOR_PRESCRIPTION),
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .subtitle1,
                                                                        color = VetNutriColors.Primary
                                                                )
                                                        } else {
                                                                Text(
                                                                        translate(AnimalDetail.SELECT_RATIONS_FOR_PRESCRIPTION),
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .subtitle1,
                                                                        color = VetNutriColors.Primary
                                                                )
                                                        }
                                                        }

                                                        val currentConsultation: ConsultationEv? = selectedConsultation
                                                        if (currentConsultation != null && currentConsultation.rations.isNotEmpty()) {
                                                                items(currentConsultation.rations) { ration ->
                                                                Row(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth()
                                                                                        .padding(
                                                                                                vertical =
                                                                                                        0.dp
                                                                                        ),
                                                                        verticalAlignment =
                                                                                Alignment.CenterVertically
                                                                ) {
                                                                        val isSelectedRation: Boolean =
                                                                                selectedRationIdsForPrescription
                                                                                        .contains(
                                                                                                ration.uuid
                                                                                        )
                                                                        Checkbox(
                                                                                checked = isSelectedRation,
                                                                                onCheckedChange = { isChecked: Boolean ->
                                                                                        selectedRationIdsForPrescription =
                                                                                                if (isChecked) {
                                                                                                        selectedRationIdsForPrescription +
                                                                                                                ration.uuid
                                                                                                } else {
                                                                                                        selectedRationIdsForPrescription -
                                                                                                                ration.uuid
                                                                                                }
                                                                                        schedulePrescriptionSave()
                                                                                }
                                                                        )
                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.width(
                                                                                                8.dp
                                                                                        )
                                                                        )
                                                                        Column {
                                                                                val rationLabel: String =
                                                                                        if (ration.actual) {
                                                                                                translate(AnimalDetail.RATION_CURRENT)
                                                                                        } else {
                                                                                                translate(AnimalDetail.RATION_PROPOSED)
                                                                                        }
                                                                                Text(
                                                                                        text =
                                                                                                translate(
                                                                                                        AnimalDetail.RATION_LINE,
                                                                                                        ration.name,
                                                                                                        rationLabel,
                                                                                                        ration.getQuantiteTotale().toString()
                                                                                                ),
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .body2
                                                                                )
                                                                        }
                                                                }
                                                                }
                                                        } else {
                                                                item {
                                                                Text(
                                                                        translate(AnimalDetail.NO_RATION_AVAILABLE),
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
                                                                                                        0.7f
                                                                                        )
                                                                )
                                                                }
                                                        }

                                                        if (isExamMode) {
                                                                item {
                                                                        Text(
                                                                                "Conseils personnalisés indisponibles en mode examen.",
                                                                                style = MaterialTheme.typography.body2,
                                                                                color = Color.Gray
                                                                        )
                                                                }
                                                        } else {
                                                                // Section pour les conseils personnalisés
                                                                item {
                                                                Text(
                                                                        translate(AnimalDetail.CUSTOM_ADVICE_TITLE),
                                                                        style =
                                                                                MaterialTheme.typography
                                                                                        .subtitle1,
                                                                        color = VetNutriColors.Primary
                                                                )
                                                                }

                                                                // Affichage des conseils sélectionnés
                                                                if (selectedConseils.isNotEmpty()) {
                                                                        item {
                                                                        Column(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth(),
                                                                                verticalArrangement =
                                                                                        Arrangement
                                                                                                .spacedBy(
                                                                                                        4.dp
                                                                                                )
                                                                        ) {
                                                                                selectedConseils.forEach {
                                                                                        conseil ->
                                                                                        Card(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxWidth(),
                                                                                                elevation =
                                                                                                        2.dp
                                                                                        ) {
                                                                                                Row(
                                                                                                        modifier =
                                                                                                                Modifier.fillMaxWidth()
                                                                                                                        .padding(
                                                                                                                                8.dp
                                                                                                                        ),
                                                                                                        horizontalArrangement =
                                                                                                                Arrangement
                                                                                                                        .SpaceBetween,
                                                                                                        verticalAlignment =
                                                                                                                Alignment
                                                                                                                        .CenterVertically
                                                                                                ) {
                                                                                                        Column(
                                                                                                                modifier =
                                                                                                                        Modifier.weight(
                                                                                                                                1f
                                                                                                                        )
                                                                                                        ) {
                                                                                                                Text(
                                                                                                                        text =
                                                                                                                                conseil.title,
                                                                                                                        style =
                                                                                                                                MaterialTheme
                                                                                                                                        .typography
                                                                                                                                        .body2,
                                                                                                                        fontWeight =
                                                                                                                                FontWeight
                                                                                                                                        .Medium
                                                                                                                )
                                                                                                                Text(
                                                                                                                        text =
                                                                                                                                translate(
                                                                                                                                        AnimalDetail.CATEGORY_LABEL,
                                                                                                                                        conseil.category.name
                                                                                                                                ),
                                                                                                                        style =
                                                                                                                                MaterialTheme
                                                                                                                                       .typography
                                                                                                                                       .caption,
                                                                                                                        color =
                                                                                                                                Color.Gray
                                                                                                                )
                                                                                                        }
                                                                                                        IconButtonWithTooltip(
                                                                                                                onClick = {
                                                                                                                        selectedConseils =
                                                                                                                                selectedConseils
                                                                                                                                        .filter {
                                                                                                                                                it.id !=
                                                                                                                                                        conseil.id
                                                                                                                                        }
                                                                                                                        schedulePrescriptionSave()
                                                                                                                },
                                                                                                                imageVector = Icons.Default.Delete,
                                                                                                                contentDescription = translate(General.DELETE),
                                                                                                                tooltip = translate(General.DELETE),
                                                                                                                tint = Color.Red
                                                                                                        )
                                                                                                        }
                                                                                                }
                                                                                        }
                                                                                }
                                                                        }
                                                                }

                                                                // Bouton pour ajouter des conseils
                                                                item {
                                                                Button(
                                                                        onClick = {
                                                                                showSearchDialog = true
                                                                        },
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        colors =
                                                                                ButtonDefaults.buttonColors(
                                                                                        backgroundColor =
                                                                                                VetNutriColors
                                                                                                        .Secondary,
                                                                                        contentColor =
                                                                                                VetNutriColors
                                                                                                        .OnSecondary
                                                                                )
                                                                ) {
                                                                        Icon(
                                                                                Icons.Default.Add,
                                                                                translate(General.ADD)
                                                                        )
                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.width(8.dp)
                                                                        )
                                                                        Text(translate(AnimalDetail.ADD_ADVICE))
                                                                        }
                                                                }
                                                        }

                                                        item {
                                                        Spacer(modifier = Modifier.height(16.dp))
                                                        }

                                                        // Section pour les sections HTML créées
                                                        // localement
                                                        if (localHtmlSections.isNotEmpty()) {
                                                                item {
                                                                Text(
                                                                translate(
                                                                        AnimalDetail.LOCAL_HTML_SECTIONS_TITLE,
                                                                        localHtmlSections.size.toString()
                                                                ),
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .subtitle1,
                                                                        color =
                                                                                VetNutriColors
                                                                                        .Primary
                                                                )
                                                                }
                                                                item {
                                                                Column(
                                                                        modifier =
                                                                                Modifier.fillMaxWidth(),
                                                                        verticalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                4.dp
                                                                                        )
                                                                ) {
                                                                        localHtmlSections.forEach {
                                                                                section ->
                                                                                Card(
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth(),
                                                                                        elevation =
                                                                                                2.dp
                                                                                ) {
                                                                                        Row(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxWidth()
                                                                                                                .padding(
                                                                                                                        8.dp
                                                                                                                ),
                                                                                                horizontalArrangement =
                                                                                                        Arrangement
                                                                                                                .SpaceBetween,
                                                                                                verticalAlignment =
                                                                                                        Alignment
                                                                                                                .CenterVertically
                                                                                        ) {
                                                                                                Column(
                                                                                                        modifier =
                                                                                                                Modifier.weight(
                                                                                                                        1f
                                                                                                                )
                                                                                                ) {
                                                                                                        Text(
                                                                                                                text =
                                                                                                                        section.title,
                                                                                                                style =
                                                                                                                        MaterialTheme
                                                                                                                                .typography
                                                                                                                                .body2,
                                                                                                                fontWeight =
                                                                                                                        FontWeight
                                                                                                                                .Medium
                                                                                                        )
                                                                                                        Text(
                                                                                                                text =
                                                                                                                        translate(
                                                                                                                                AnimalDetail.BLOCKS_COUNT,
                                                                                                                                section.content.blocks.size.toString()
                                                                                                                        ),
                                                                                                                style =
                                                                                                                        MaterialTheme
                                                                                                                               .typography
                                                                                                                               .caption,
                                                                                                                color =
                                                                                                                        Color.Gray
                                                                                                        )
                                                                                                }
                                                                                                IconButtonWithTooltip(
                                                                                                        onClick = {
                                                                                                                localHtmlSections =
                                                                                                                        localHtmlSections
                                                                                                                                .filter {
                                                                                                                                        it.id !=
                                                                                                                                                section.id
                                                                                                                                }
                                                                                                                schedulePrescriptionSave()
                                                                                                        },
                                                                                                        imageVector = Icons.Default.Delete,
                                                                                                        contentDescription = translate(General.DELETE),
                                                                                                        tooltip = translate(General.DELETE),
                                                                                                        tint = Color.Red
                                                                                                )
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                                }
                                                                item {
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.height(
                                                                                        16.dp
                                                                                )
                                                                )
                                                                }
                                                        }

                                                        // Bouton pour accéder à l'éditeur de texte
                                                        // enrichi
                                                        item {
                                                        Button(
                                                                onClick = {
                                                                        showRichTextEditor = true
                                                                },
                                                                modifier = Modifier.fillMaxWidth(),
                                                                colors =
                                                                        ButtonDefaults.buttonColors(
                                                                                backgroundColor =
                                                                                        VetNutriColors
                                                                                                .Secondary,
                                                                                contentColor =
                                                                                        VetNutriColors
                                                                                                .OnSecondary
                                                                        )
                                                        ) {
                                                                Icon(
                                                                        Icons.Default.Edit,
                                                                        translate(AnimalDetail.HTML_EDITOR)
                                                                )
                                                                Spacer(
                                                                        modifier =
                                                                                Modifier.width(
                                                                                        AppSizes.paddingSmall
                                                                                )
                                                                )
                                                                Text(
                                                                translate(AnimalDetail.CREATE_CUSTOM_HTML_SECTIONS)
                                                        )
                                                        }
                                                        }

                                                item {
                                                        OutlinedTextField(
                                                                value = anamneseText,
                                                                onValueChange = {
                                                                        anamneseText = it
                                                                        schedulePrescriptionSave()
                                                                },
                                                                modifier = Modifier.fillMaxWidth(),
                                                                label = { Text("Anamnese") },
                                                                maxLines = 6
                                                        )
                                                        }

                                                item {
                                                        OutlinedTextField(
                                                                value = examenCliniqueText,
                                                                onValueChange = {
                                                                        examenCliniqueText = it
                                                                        schedulePrescriptionSave()
                                                                },
                                                                modifier = Modifier.fillMaxWidth(),
                                                                label = { Text("Examen clinique") },
                                                                maxLines = 6
                                                        )
                                                        }

                                                item {
                                                        OutlinedTextField(
                                                                value = facteurNutritionnelClefText,
                                                                onValueChange = {
                                                                        facteurNutritionnelClefText = it
                                                                        schedulePrescriptionSave()
                                                                },
                                                                modifier = Modifier.fillMaxWidth(),
                                                                label = {
                                                                        Text("Facteur nutritionnel clef")
                                                                },
                                                                maxLines = 4
                                                        )
                                                        }

                                                item {
                                                        OutlinedTextField(
                                                                value = additionalText,
                                                                onValueChange = {
                                                                        additionalText = it
                                                                        schedulePrescriptionSave()
                                                                },
                                                                modifier = Modifier.fillMaxWidth(),
                                                                label = {
                                                                        Text(
                                                                                translate(AnimalDetail.ADDITIONAL_TEXT_LABEL)
                                                                        )
                                                                },
                                                                maxLines = 6
                                                        )
                                                        }

                                                        item {
                                                        Row(
                                                                horizontalArrangement =
                                                                        Arrangement.spacedBy(
                                                                                AppSizes.paddingSmall
                                                                        )
                                                        ) {
                                                                val compteRenduText =
                                                                        buildCompteRenduText(
                                                                                animal = animalDetails,
                                                                                consultation = selectedConsultation,
                                                                                practitionerContact = practitionerContact,
                                                                                anamnese = anamneseText,
                                                                                examenClinique = examenCliniqueText,
                                                                                facteurNutritionnelClef = facteurNutritionnelClefText,
                                                                                additionalText = additionalText,
                                                                                selectedConseils = selectedConseils
                                                                        )
                                                                OutlinedButton(
                                                                        onClick = {
                                                                                pendingCopyText = compteRenduText
                                                                                scope.launch {
                                                                                        snackbarHostState.showSnackbar("CR copié dans le presse-papiers")
                                                                                }
                                                                        }
                                                                ) { Text("Copier le CR") }
                                                                OutlinedButton(
                                                                        onClick = {
                                                                                previewMode = "CR"
                                                                                previewCompteRenduText = compteRenduText
                                                                                previewHtml =
                                                                                        buildCompteRenduHtml(
                                                                                                animal = animalDetails,
                                                                                                consultation = selectedConsultation,
                                                                                                practitionerContact = practitionerContact,
                                                                                                anamnese = anamneseText,
                                                                                                examenClinique = examenCliniqueText,
                                                                                                facteurNutritionnelClef = facteurNutritionnelClefText,
                                                                                                additionalText = additionalText,
                                                                                                selectedConseils = selectedConseils
                                                                                        )
                                                                                showPreview = true
                                                                        }
                                                                ) { Text("Compte rendu") }
                                                                Button(
                                                                        onClick = {
                                                                                val prefsStorage = createPreferencesStorage()
                                                                                val prefsRepo = PreferencesRepository(prefsStorage)
                                                                                scope.launch {
                                                                                        try {
                                                                                                prefsRepo.loadPreferences()
                                                                                                val prefs = prefsRepo.preferences
                                                                                                val practitioner = fr.vetbrain.vetnutri_mp.Export.PractitionerInfo(
                                                                                                        nom = prefs.nomUtilisateur,
                                                                                                        numeroOrdre = prefs.numeroOrdre,
                                                                                                        adressePostale = prefs.adressePostale,
                                                                                                        codePostal = prefs.codePostal,
                                                                                                        ville = prefs.ville,
                                                                                                        telephone = prefs.telephone,
                                                                                                        email = prefs.email
                                                                                                )
                                                                                                val selectedRationsForPrescription: List<Ration> =
                                                                                                        selectedConsultation?.rations
                                                                                                                ?.filter { ration: Ration ->
                                                                                                                        selectedRationIdsForPrescription
                                                                                                                                .contains(
                                                                                                                                        ration.uuid
                                                                                                                                )
                                                                                                                }
                                                                                                                ?.toList()
                                                                                                                        ?: emptyList()
                                                                                                previewHtml =
                                                                                                        HtmlDocumentBuilder
                                                                                                                .buildHtml(
                                                                                                                        DocumentType
                                                                                                                                .PRESCRIPTION,
                                                                                                                        ExportData(
                                                                                                                                animal =
                                                                                                                                        animalDetails,
                                                                                                                                ration =
                                                                                                                                        null,
                                                                                                                                reference =
                                                                                                                                        referenceUtilisee,
                                                                                                                                conseils =
                                                                                                                                        listOf(
                                                                                                                                                translate(AnimalDetail.DEFAULT_ADVICE_HYDRATION)
                                                                                                                                        ),
                                                                                                                                title =
                                                                                                                                        translate(AnimalDetail.PRESCRIPTION_TITLE),
                                                                                                                                additionalText =
                                                                                                                                        additionalText,
                                                                                                                                htmlSections =
                                                                                                                                        getSelectedConseils(),
                                                                                                                                rations = selectedRationsForPrescription,
                                                                                                                                practitioner = practitioner,
                                                                                                                                preferences = null,
                                                                                                                                poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                                                                                                                                poidsMetabolique = null,
                                                                                                                                besoinEnergetiqueEntretien = null
                                                                                                                        )
                                                                                                                )
                                                                                                previewMode = "PRESCRIPTION"
                                                                                                previewCompteRenduText = ""
                                                                                                showPreview = true
                                                                                        } catch (e: Exception) {
                                                                                                val selectedRationsForPrescription: List<Ration> =
                                                                                                        selectedConsultation?.rations
                                                                                                                ?.filter { ration: Ration ->
                                                                                                                        selectedRationIdsForPrescription
                                                                                                                                .contains(
                                                                                                                                        ration.uuid
                                                                                                                                )
                                                                                                                }
                                                                                                                ?.toList()
                                                                                                                        ?: emptyList()
                                                                                                previewHtml = HtmlDocumentBuilder.buildHtml(
                                                                                                        DocumentType.PRESCRIPTION,
                                                                                                        ExportData(
                                                                                                                animal = animalDetails,
                                                                                                                ration = null,
                                                                                                                reference = referenceUtilisee,
                                                                                                                conseils = emptyList(),
                                                                                                                title = translate(AnimalDetail.PRESCRIPTION_TITLE),
                                                                                                                additionalText = additionalText,
                                                                                                                htmlSections = getSelectedConseils(),
                                                                                                                rations = selectedRationsForPrescription,
                                                                                                                practitioner = null,
                                                                                                                preferences = null,
                                                                                                                poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                                                                                                                poidsMetabolique = null,
                                                                                                                besoinEnergetiqueEntretien = null
                                                                                                        )
                                                                                                )
                                                                                                previewMode = "PRESCRIPTION"
                                                                                                previewCompteRenduText = ""
                                                                                                showPreview = true
                                                                                        }
                                                                                }
                                                                        }
                                                                ) {
                                                                        Text(
                                                                                translate(AnimalDetail.PREVIEW_PRESCRIPTION)
                                                                        )
                                                                }
                                                        }
                                                        }

                                                        // Texte additionnel
        
                                                }
                                        }

                                        // Dialogue de prévisualisation HTML (en dehors du LazyColumn)
                                                        HtmlPreviewDialog(
                                                                html = previewHtml,
                                                                isVisible = showPreview,
                                                                onConfirmExport = {
                                                                        val selectedRationsForPrescription: List<Ration> =
                                                                                selectedConsultation?.rations
                                                                                        ?.filter { ration: Ration ->
                                                                                                selectedRationIdsForPrescription
                                                                                                        .contains(
                                                                                                                ration.uuid
                                                                                                        )
                                                                                        }
                                                                                        ?.toList()
                                                                                                ?: emptyList()
                                                                        handlePdfExport(
                                                                                previewHtml = previewHtml,
                                                                                previewMode = previewMode,
                                                                                compteRenduText = previewCompteRenduText,
                                                                                animalDetails = animalDetails,
                                                                                selectedConsultation = selectedConsultation,
                                                                                selectedRation = selectedRation,
                                                                                selectedRationsForPrescription = selectedRationsForPrescription,
                                                                                referenceUtilisee = referenceUtilisee,
                                                                                additionalText = additionalText,
                                                                                getSelectedConseils = getSelectedConseils,
                                                                                besoinEnergetiqueStandard = besoinEnergetiqueStandard,
                                                                                poidsMetabolique = poidsMetabolique,
                                                                                equationRepository = equationRepository,
                                                                                scope = scope
                                                                        )
                                                                        showPreview = false
                                                                },
                                                                onDismiss = { showPreview = false }
                                                        )
                                        if (pendingCopyText != null) {
                                                copyToClipboardComposable(pendingCopyText!!)
                                                LaunchedEffect(pendingCopyText) { pendingCopyText = null }
                                        }
                                }
                        }

                        // Dialogue de recherche et sélection des conseils
                        if (showSearchDialog) {
                                AlertDialog(
                                        onDismissRequest = { showSearchDialog = false },
                                        title = { Text(translate(AnimalDetail.ADD_ADVICE)) },
                                        text = {
                                                Column {
                                                        OutlinedTextField(
                                                                value = searchQuery,
                                                                onValueChange = {
                                                                        searchQuery = it
                                                                },
                                                                label = {
                                                                        Text(
                                                                                translate(AnimalDetail.SEARCH_ADVICE_HINT)
                                                                        )
                                                                },
                                                                modifier = Modifier.fillMaxWidth()
                                                        )

                                                        Spacer(modifier = Modifier.height(16.dp))

                                                        val filteredConseils =
                                                                availableConseils.filter { conseil
                                                                        ->
                                                                        conseil.title.contains(
                                                                                searchQuery,
                                                                                ignoreCase = true
                                                                        ) ||
                                                                                conseil.category
                                                                                        .name
                                                                                        .contains(
                                                                                                searchQuery,
                                                                                                ignoreCase =
                                                                                                        true
                                                                                        )
                                                                }

                                                        LazyColumn(
                                                                modifier =
                                                                        Modifier.heightIn(
                                                                                max = 300.dp
                                                                        ),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(4.dp)
                                                        ) {
                                                                items(filteredConseils) { conseil ->
                                                                        val isAlreadySelected =
                                                                                selectedConseils
                                                                                        .any {
                                                                                                it.id ==
                                                                                                        conseil.id
                                                                                        }

                                                                        Card(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth(),
                                                                                elevation =
                                                                                        if (isAlreadySelected
                                                                                        )
                                                                                                4.dp
                                                                                        else 1.dp,
                                                                                backgroundColor =
                                                                                        if (isAlreadySelected
                                                                                        )
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.1f
                                                                                                        )
                                                                                        else
                                                                                                Color.Transparent
                                                                        ) {
                                                                                Row(
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth()
                                                                                                        .padding(
                                                                                                                12.dp
                                                                                                        ),
                                                                                        horizontalArrangement =
                                                                                                Arrangement
                                                                                                        .SpaceBetween,
                                                                                        verticalAlignment =
                                                                                                Alignment
                                                                                                        .CenterVertically
                                                                                ) {
                                                                                        Column(
                                                                                                modifier =
                                                                                                        Modifier.weight(
                                                                                                                1f
                                                                                                        )
                                                                                        ) {
                                                                                                Text(
                                                                                                        text =
                                                                                                                conseil.title,
                                                                                                        style =
                                                                                                                MaterialTheme
                                                                                                                        .typography
                                                                                                                        .body1,
                                                                                                        fontWeight =
                                                                                                                FontWeight
                                                                                                                        .Medium
                                                                                                )
                                                                                                Text(
                                                                                                        text =
                                                                                                                translate(
                                                                                                                        AnimalDetail.CATEGORY_LABEL,
                                                                                                                        conseil.category.name
                                                                                                                ),
                                                                                                        style =
                                                                                                                MaterialTheme
                                                                                                                       .typography
                                                                                                                       .caption,
                                                                                                        color =
                                                                                                                Color.Gray
                                                                                                )
                                                                                        }

                                                                                        if (isAlreadySelected
                                                                                        ) {
                                                                                                Icon(
                                                                                                        Icons.Default
                                                                                                                .Check,
                                                                                                        translate(AnimalDetail.SELECTED),
                                                                                                        tint =
                                                                                                                VetNutriColors
                                                                                                                       .Primary
                                                                                                )
                                                                                        } else {
                                                                                                IconButtonWithTooltip(
                                                                                                        onClick = {
                                                                                                                selectedConseils =
                                                                                                                        selectedConseils +
                                                                                                                                conseil
                                                                                                                schedulePrescriptionSave()
                                                                                                        },
                                                                                                        imageVector = Icons.Default.Add,
                                                                                                        contentDescription = translate(General.ADD),
                                                                                                        tooltip = translate(General.ADD),
                                                                                                        tint = VetNutriColors.Primary
                                                                                                )
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        },
                                        confirmButton = {
                                                TextButton(onClick = { showSearchDialog = false }) {
                                                        Text(translate(General.CLOSE))
                                                }
                                        }
                                )
                        }
                }
        }
        }
}

/** Layout pour les écrans étroits avec un drawer */
@Composable
private fun NarrowScreenLayout(
        animalDetails: AnimalEv,
        currentSection: AnimalDetailSection,
        menuOptions: List<MenuOption>,
        onNavigateBack: () -> Unit,
        onOpenSettings: () -> Unit,
        viewModel: AnimalDetailViewModel,
        settingsViewModel: SettingsViewModel,
        isEditing: Boolean,
        onIsEditingChange: (Boolean) -> Unit,
        onShowDeleteConfirmation: () -> Unit,
        showConsultationDetail: Boolean,
        onShowConsultationDetail: (Boolean) -> Unit,
        drawerState: DrawerState,
        scope: CoroutineScope,
        equationRepository: EquationRepository,
        recipeRepository: RecipeRepository,
        conseilRepository: fr.vetbrain.vetnutri_mp.Repository.ConseilRepository,
        isExamMode: Boolean = false
) {
        // État pour les messages Snackbar
        val snackbarHostState = remember { SnackbarHostState() }
        
        // État pour le partage en ligne
        var shareLink by remember { mutableStateOf<fr.vetbrain.vetnutri_mp.Service.ShareLink?>(null) }
        var showShareDialog by remember { mutableStateOf(false) }
        var showAnonymizationDialog by remember { mutableStateOf(false) }
        val shareLauncher = fr.vetbrain.vetnutri_mp.Utils.rememberShareLauncher()
        
        // État pour l'éditeur de texte enrichi
        var currentHtmlContent by remember {
                mutableStateOf(fr.vetbrain.vetnutri_mp.Export.RichTextContent())
        }
        var showRichTextEditor by remember { mutableStateOf(false) }

        // État pour les conseils personnalisés (sauvegardés)
        var availableConseils by remember {
                mutableStateOf<List<fr.vetbrain.vetnutri_mp.Export.HtmlSection>>(emptyList())
        }
        // État pour les sections HTML créées localement (temporaires)
        var localHtmlSections by remember {
                mutableStateOf<List<fr.vetbrain.vetnutri_mp.Export.HtmlSection>>(emptyList())
        }
        var selectedConseils by remember {
                mutableStateOf<List<fr.vetbrain.vetnutri_mp.Export.HtmlSection>>(emptyList())
        }
        var isLoadingConseils by remember { mutableStateOf(true) }
        var searchQuery by remember { mutableStateOf("") }
        var showSearchDialog by remember { mutableStateOf(false) }
        var pendingCopyText by remember { mutableStateOf<String?>(null) }
        var anamneseText by remember { mutableStateOf("") }
        var examenCliniqueText by remember { mutableStateOf("") }
        var facteurNutritionnelClefText by remember { mutableStateOf("") }
        var additionalText by remember { mutableStateOf("") }
        var practitionerContact by remember { mutableStateOf(PractitionerContact()) }
        val selectedConsultation by viewModel.selectedConsultation.collectAsState()
        var selectedRationIdsForPrescription by remember(selectedConsultation?.uuid) {
                val initialSelection: Set<String> =
                        selectedConsultation?.rations
                                ?.filter { ration: Ration -> !ration.actual }
                                ?.map { ration: Ration -> ration.uuid }
                                ?.toSet() ?: emptySet()
                mutableStateOf(initialSelection)
        }
        var savePrescriptionJob by remember(selectedConsultation?.uuid) {
                mutableStateOf<Job?>(null)
        }

        fun schedulePrescriptionSave() {
                val consultation = selectedConsultation ?: return
                savePrescriptionJob?.cancel()
                savePrescriptionJob =
                        scope.launch {
                                delay(400)
                                val updatedConsultation =
                                        consultation.copy(
                                                prescriptionAnamnese = anamneseText,
                                                prescriptionExamenClinique = examenCliniqueText,
                                                prescriptionFacteurNutritionnelClef =
                                                        facteurNutritionnelClefText,
                                                prescriptionAdditionalText = additionalText,
                                                prescriptionSelectedConseilIds =
                                                        selectedConseils
                                                                .map { it.id }
                                                                .toMutableList(),
                                                prescriptionLocalHtmlSections =
                                                        localHtmlSections.toMutableList(),
                                                prescriptionSelectedRationIds =
                                                        selectedRationIdsForPrescription
                                                                .toMutableList()
                                        )
                                if (updatedConsultation != consultation) {
                                        viewModel.updateConsultation(updatedConsultation)
                                }
                        }
        }

        LaunchedEffect(selectedConsultation?.uuid) {
                val consultation = selectedConsultation
                if (consultation == null) {
                        anamneseText = ""
                        examenCliniqueText = ""
                        facteurNutritionnelClefText = ""
                        additionalText = ""
                        localHtmlSections = emptyList()
                        selectedConseils = emptyList()
                        selectedRationIdsForPrescription = emptySet()
                        return@LaunchedEffect
                }
                anamneseText = consultation.prescriptionAnamnese
                examenCliniqueText = consultation.prescriptionExamenClinique
                facteurNutritionnelClefText = consultation.prescriptionFacteurNutritionnelClef
                additionalText = consultation.prescriptionAdditionalText
                localHtmlSections = consultation.prescriptionLocalHtmlSections
                if (consultation.prescriptionSelectedRationIds.isNotEmpty()) {
                        selectedRationIdsForPrescription =
                                consultation.prescriptionSelectedRationIds.toSet()
                }
                val selectedConseilIds =
                        consultation.prescriptionSelectedConseilIds.toSet()
                selectedConseils =
                        if (selectedConseilIds.isEmpty()) {
                                emptyList()
                        } else {
                                availableConseils.filter { it.id in selectedConseilIds }
                        }
        }

        LaunchedEffect(availableConseils, selectedConsultation?.uuid) {
                val consultation = selectedConsultation ?: return@LaunchedEffect
                if (selectedConseils.isNotEmpty()) {
                        return@LaunchedEffect
                }
                if (consultation.prescriptionSelectedConseilIds.isEmpty()) {
                        return@LaunchedEffect
                }
                val selectedConseilIds =
                        consultation.prescriptionSelectedConseilIds.toSet()
                selectedConseils =
                        availableConseils.filter { it.id in selectedConseilIds }
        }
        // Charger les conseils personnalisés
        LaunchedEffect(Unit) {
                if (isExamMode) {
                        availableConseils = emptyList()
                        isLoadingConseils = false
                        return@LaunchedEffect
                }
                try {
                        val result = conseilRepository.getConseilsActifs()
                        if (result.isSuccess) {
                                availableConseils = result.getOrThrow()
                        }
                } catch (e: Exception) {
                        e.printStackTrace()
                } finally {
                        isLoadingConseils = false
                }
        }
        LaunchedEffect(Unit) {
                try {
                        val prefsStorage = createPreferencesStorage()
                        val prefsRepo = PreferencesRepository(prefsStorage)
                        prefsRepo.loadPreferences()
                        val prefs = prefsRepo.preferences
                        practitionerContact =
                                PractitionerContact(
                                        nom = prefs.nomUtilisateur,
                                        numeroOrdre = prefs.numeroOrdre,
                                        adressePostale = prefs.adressePostale,
                                        codePostal = prefs.codePostal,
                                        ville = prefs.ville,
                                        telephone = prefs.telephone,
                                        email = prefs.email
                                )
                } catch (_: Exception) {}
        }

        // Variables pour la prévisualisation et l'export
        var showPreview by remember {
                mutableStateOf(false)
        }
        var previewHtml by remember {
                mutableStateOf("")
        }

        // Fonction pour récupérer les conseils sélectionnés (conseils + sections locales)
        val getSelectedConseils:
                () -> List<fr.vetbrain.vetnutri_mp.Export.HtmlSection> =
                {
                        selectedConseils + localHtmlSections
                }

        ModalDrawer(
                drawerState = drawerState,
                drawerContent = {
                        Column(
                                modifier =
                                        Modifier.fillMaxHeight()
                                                .width(250.dp)
                                                .verticalScroll(rememberScrollState())
                                                .padding(AppSizes.paddingMedium),
                                verticalArrangement = Arrangement.spacedBy(AppSizes.paddingMedium)
                        ) {
                                // En-tête avec nom et espèce de l'animal
                                Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                                text = animalDetails.nom,
                                                style = MaterialTheme.typography.h5
                                        )
                                        Text(
                                                text = animalDetails.getEspece().translateEnum(),
                                                style = MaterialTheme.typography.subtitle1,
                                                color = Color.Gray
                                        )
                                }

                                Divider()

                                // Options du menu
                                menuOptions.forEach { option ->
                                        MenuOptionItem(
                                                option = option,
                                                isSelected = currentSection == option.section,
                                                onClick = {
                                                        viewModel.navigateTo(option.section)
                                                        scope.launch { drawerState.close() }
                                                }
                                        )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // Bouton exporter animal
                                Button(
                                        onClick = {
                                                scope.launch {
                                                        exporterAnimalComplet(
                                                                animal = animalDetails,
                                                                viewModel = viewModel,
                                                                settingsViewModel = settingsViewModel,
                                                                equationRepository = equationRepository,
                                                                recipeRepository = recipeRepository,
                                                                conseilRepository = conseilRepository,
                                                                snackbarHostState = snackbarHostState
                                                        )
                                                }
                                        },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Primary,
                                                        contentColor = VetNutriColors.OnPrimary
                                                ),
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = translate(AnimalDetail.EXPORT_ANIMAL)
                                        )
                                        Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                        Text(text = translate(AnimalDetail.EXPORT_ANIMAL))
                                }
                                
                                // Bouton partager en ligne
                                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                                Button(
                                        onClick = {
                                                showAnonymizationDialog = true
                                        },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Secondary,
                                                        contentColor = VetNutriColors.OnSecondary
                                                ),
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = translate(AnimalDetail.SHARE_ONLINE)
                                        )
                                        Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                        Text(text = translate(AnimalDetail.SHARE_ONLINE))
                                }

                                // Bouton retour
                                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                                Button(
                                        onClick = onNavigateBack,
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        backgroundColor = VetNutriColors.Secondary,
                                                        contentColor = VetNutriColors.OnSecondary
                                                ),
                                        modifier = Modifier.fillMaxWidth()
                                ) {
                                        Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = translate(AnimalDetail.BACK)
                                        )
                                        Spacer(modifier = Modifier.width(AppSizes.paddingSmall))
                                        Text(text = translate(AnimalDetail.BACK))
                                }

                                // Ajout de l'option Paramètres en bas du menu
                                Spacer(modifier = Modifier.height(AppSizes.paddingSmall))
                                MenuOptionItem(
                                        option =
                                                MenuOption(
                                                        section =
                                                                AnimalDetailSection.IDENTIFICATION,
                                                        title = translate(Settings.TITLE),
                                                        icon = Icons.Default.Settings
                                                ),
                                        isSelected = false,
                                        onClick = {
                                                onOpenSettings()
                                                scope.launch { drawerState.close() }
                                        }
                                )
                        }
                },
                content = {
                        Column(modifier = Modifier.fillMaxSize()) {
                                // En-tête avec bouton menu (remplace la TopAppBar)
                                Row(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(AppSizes.paddingMedium),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        IconButtonWithTooltip(
                                                onClick = { scope.launch { drawerState.open() } },
                                                imageVector = Icons.Default.Menu,
                                                contentDescription = translate(AnimalDetail.MENU),
                                                tooltip = translate(AnimalDetail.MENU),
                                                tint = VetNutriColors.Primary
                                        )

                                        Text(
                                                text = animalDetails.nom,
                                                style = MaterialTheme.typography.h6,
                                                color = VetNutriColors.Primary
                                        )

                                        // Espace vide pour équilibrer la mise en page
                                        Spacer(modifier = Modifier.size(AppSizes.iconSizeLarge))
                                }

                                Divider(
                                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                                        thickness = AppSizes.dividerHeight
                                )

                                // Contenu principal avec SnackbarHost
                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                        SnackbarHost(hostState = snackbarHostState)

                                        // Dialog d'anonymisation
                                        if (showAnonymizationDialog) {
                                                AnonymizationDialog(
                                                        onConfirm = { shouldAnonymize, shouldEncrypt ->
                                                                showAnonymizationDialog = false
                                                                scope.launch {
                                                                        partagerAnimalEnLigne(
                                                                                animal = animalDetails,
                                                                                viewModel = viewModel,
                                                                                settingsViewModel = settingsViewModel,
                                                                                equationRepository = equationRepository,
                                                                                recipeRepository = recipeRepository,
                                                                                conseilRepository = conseilRepository,
                                                                                snackbarHostState = snackbarHostState,
                                                                                onShareLinkGenerated = { link ->
                                                                                        shareLink = link
                                                                                        showShareDialog = true
                                                                                },
                                                                                shouldAnonymize = shouldAnonymize,
                                                                                shouldEncrypt = shouldEncrypt
                                                                        )
                                                                }
                                                        },
                                                        onDismiss = {
                                                                showAnonymizationDialog = false
                                                        }
                                                )
                                        }
                                        
                                        // Dialog de partage avec QR Code
                                        shareLink?.let { link ->
                                                if (showShareDialog) {
                                                        ShareLinkDialog(
                                                                shareLink = link,
                                                                onDismiss = {
                                                                        showShareDialog = false
                                                                        shareLink = null
                                                                },
                                                                onShare = { shareLauncher(link.qrCodeData ?: link.binId) }
                                                        )
                                                }
                                        }

                                        when (currentSection) {
                                                AnimalDetailSection.IDENTIFICATION -> {
                                                        if (isEditing) {
                                                                AnimalEditView(
                                                                        animal = animalDetails,
                                                                        onSave = { updatedAnimal ->
                                                                                viewModel
                                                                                        .updateAnimal(
                                                                                                updatedAnimal
                                                                                        )
                                                                                onIsEditingChange(
                                                                                        false
                                                                                )
                                                                        },
                                                                        onCancel = {
                                                                                onIsEditingChange(
                                                                                        false
                                                                                )
                                                                        },
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                                        .padding(
                                                                                                AppSizes.paddingMedium
                                                                                        )
                                                                )
                                                        } else {
                                                                AnimalIdentificationView(
                                                                        animal = animalDetails,
                                                                        onEdit = {
                                                                                onIsEditingChange(
                                                                                        true
                                                                                )
                                                                        },
                                                                        onDelete =
                                                                                onShowDeleteConfirmation,
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                                        .padding(
                                                                                                AppSizes.paddingMedium
                                                                                        )
                                                                )
                                                        }
                                                }
                                                AnimalDetailSection.CONSULTATIONS -> {
                                                        ConsultationsView(
                                                                viewModel = viewModel,
                                                                showConsultationDetail =
                                                                        showConsultationDetail,
                                                                onShowConsultationDetail =
                                                                        onShowConsultationDetail,
                                                                modifier = Modifier.fillMaxSize()
                                                        )
                                                }
                                                AnimalDetailSection.RATIONS -> {
                                                        RationsView(
                                                                viewModel = viewModel,
                                                                showSnackbar = { message -> },
                                                                equationRepository =
                                                                        equationRepository,
                                                                recipeRepository = recipeRepository,
                                                                isExamMode = isExamMode
                                                        )
                                                }
                                                AnimalDetailSection.GRAPHIQUE -> {
                                                        AnalyseGraphiqueView(
                                                                viewModel = viewModel,
                                                                equationRepository =
                                                                        equationRepository,
                                                                modifier = Modifier.fillMaxSize()
                                                        )
                                                }
                                                AnimalDetailSection.GRAPHIQUE_ALIMENTS -> {
                                                        val availableFoods by
                                                                viewModel.availableFoods
                                                                        .collectAsState()
                                                        val isLoadingFoods by
                                                                viewModel.isLoadingFoods
                                                                        .collectAsState()

                                                        // 🔧 Récupération des préférences pour
                                                        // l'espèce dans ce contexte (même logique
                                                        // que layout large)
                                                        val preferencesStorageLocal:
                                                                fr.vetbrain.vetnutri_mp.Utils.PreferencesStorage =
                                                                remember {
                                                                        createPreferencesStorage()
                                                                }
                                                        val preferencesRepositoryLocal:
                                                                PreferencesRepository =
                                                                remember {
                                                                        PreferencesRepository(
                                                                                preferencesStorageLocal
                                                                        )
                                                                }
                                                        var preferencesApplicationLocal by remember {
                                                                mutableStateOf<
                                                                        fr.vetbrain.vetnutri_mp.Data.PreferencesApplication?>(
                                                                        null
                                                                )
                                                        }

                                                        // Charger les préférences au démarrage
                                                        LaunchedEffect(Unit) {
                                                                preferencesRepositoryLocal
                                                                        .loadPreferences()
                                                                preferencesApplicationLocal =
                                                                        preferencesRepositoryLocal
                                                                                .preferences
                                                        }

                                                        if (isLoadingFoods) {
                                                                Column(
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                                        .padding(
                                                                                                AppSizes.paddingMedium
                                                                                        ),
                                                                        verticalArrangement =
                                                                                Arrangement.Center,
                                                                        horizontalAlignment =
                                                                                Alignment
                                                                                        .CenterHorizontally
                                                                ) {
                                                                        CircularProgressIndicator(
                                                                                color =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )
                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.height(
                                                                                                AppSizes.paddingMedium
                                                                                        )
                                                                        )
                                                                        Text(
                                                                                translate(AnimalDetail.LOADING_FOODS),
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .body1,
                                                                                color =
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .onSurface
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.7f
                                                                                                )
                                                                        )
                                                                }
                                                        } else if (availableFoods.isNotEmpty()) {
                                                                // ✨ MÊME LOGIQUE QUE LE LAYOUT
                                                                // LARGE - Utiliser les états du
                                                                // ViewModel pour persister la
                                                                // sélection
                                                                val showAnalyseGraphique by
                                                                        viewModel
                                                                                .showAnalyseGraphique
                                                                                .collectAsState()
                                                                val alimentsSelectionnes by
                                                                        viewModel
                                                                                .alimentsSelectionnes
                                                                                .collectAsState()
                                                                val analyseSelectionFilters by
                                                                        viewModel
                                                                                .analyseSelectionFilters
                                                                                .collectAsState()

                                                                if (showAnalyseGraphique &&
                                                                                alimentsSelectionnes
                                                                                        .isNotEmpty()
                                                                ) {
                                                                        // Afficher la vue d'analyse
                                                                        // graphique
                                                                        // Récupérer les aliments
                                                                        // complets avec leurs
                                                                        // valeurs nutritionnelles
                                                                        var alimentsComplets by remember {
                                                                                mutableStateOf<
                                                                                        List<
                                                                                                fr.vetbrain.vetnutri_mp.Data.AlimentEv>>(
                                                                                        emptyList()
                                                                                )
                                                                        }
                                                                        var isLoadingAlimentsComplets by remember {
                                                                                mutableStateOf(true)
                                                                        }

                                                                        LaunchedEffect(
                                                                                alimentsSelectionnes
                                                                        ) {
                                                                                isLoadingAlimentsComplets =
                                                                                        true
                                                                                val alimentsAvecValeurs =
                                                                                        mutableListOf<
                                                                                                fr.vetbrain.vetnutri_mp.Data.AlimentEv>()

                                                                                for (aliment in
                                                                                        alimentsSelectionnes) {
                                                                                        try {

                                                                                                // Récupérer l'aliment complet depuis le repository
                                                                                                        val alimentComplet =
                                                                                                                viewModel.getAlimentComplet(
                                                                                                                        aliment.uuid
                                                                                                                )

                                                                                                if (alimentComplet != null) {
                                                                                                        alimentsAvecValeurs
                                                                                                                .add(
                                                                                                                        alimentComplet
                                                                                                                )
                                                                                                } else {

                                                                                                        alimentsAvecValeurs
                                                                                                                .add(
                                                                                                                        aliment
                                                                                                                ) // Fallback
                                                                                                }
                                                                                        } catch (
                                                                                                e:
                                                                                                        Exception) {

                                                                                                e.printStackTrace()
                                                                                                alimentsAvecValeurs
                                                                                                        .add(
                                                                                                                aliment
                                                                                                        ) // Fallback
                                                                                        }
                                                                                }

                                                                                alimentsComplets =
                                                                                        alimentsAvecValeurs
                                                                                isLoadingAlimentsComplets =
                                                                                        false
                                                                        }

                                                                        if (isLoadingAlimentsComplets
                                                                        ) {
                                                                                Box(
                                                                                        modifier =
                                                                                                Modifier.fillMaxSize(),
                                                                                        contentAlignment =
                                                                                                Alignment
                                                                                                        .Center
                                                                                ) {
                                                                                        Column(
                                                                                                horizontalAlignment =
                                                                                                        Alignment
                                                                                                                .CenterHorizontally,
                                                                                                verticalArrangement =
                                                                                                        Arrangement
                                                                                                                .spacedBy(
                                                                                                                        AppSizes.paddingMedium
                                                                                                                )
                                                                                        ) {
                                                                                                CircularProgressIndicator(
                                                                                                        color =
                                                                                                                VetNutriColors
                                                                                                                        .Primary
                                                                                                )
                                                                                                Text(
                                                                                                        text =
                                                                                                                translate(AnimalDetail.LOADING_NUTRITION),
                                                                                                        style =
                                                                                                                MaterialTheme
                                                                                                                        .typography
                                                                                                                        .body1,
                                                                                                        color =
                                                                                                                MaterialTheme
                                                                                                                        .colors
                                                                                                                        .onSurface
                                                                                                                        .copy(
                                                                                                                                alpha =
                                                                                                                                        0.7f
                                                                                                                        )
                                                                                                )
                                                                                        }
                                                                                }
                                                                        } else {
                                                                                AnalyseGraphiqueAlimentsView(
                                                                                        aliments =
                                                                                                alimentsComplets,
                                                                                        referenceEv =
                                                                                                viewModel
                                                                                                        .referenceUtilisee
                                                                                                        .value,
                                                                                        equationRepository =
                                                                                                equationRepository,
                                                                                        preferencesEspece =
                                                                                                animalDetails
                                                                                                        ?.let {
                                                                                                                animal
                                                                                                                ->
                                                                                                                preferencesApplicationLocal
                                                                                                                        ?.getPreferencesEspece(
                                                                                                                                animal.getEspece()
                                                                                                                        )
                                                                                                        },
                                                                                        viewModel = viewModel,
                                                                                        onClose = {
                                                                                                viewModel
                                                                                                        .hideAnalyseGraphique()
                                                                                        },
                                                                                        modifier =
                                                                                                Modifier.fillMaxSize()
                                                                                )
                                                                        }
                                                                } else {
                                                                        // Utiliser la vue de
                                                                        // sélection des aliments
                                                                        // avec possibilité
                                                                        // d'analyse graphique
                                                                        AnalyseSelectionAlimentsView(
                                                                                aliments =
                                                                                        availableFoods,
                                                                                onClose = { /* Retour à la section précédente */
                                                                                },
                                                                                onAlimentSelected = { /* Gestion de la sélection */
                                                                                },
                                                                                onPrimaryAction = {
                                                                                        aliments ->
                                                                                        viewModel
                                                                                                .lancerAnalyseGraphique(
                                                                                                        aliments
                                                                                                )
                                                                                },
                                                                                primaryActionLabel = "Voir l'analyse graphique",
                                                                                alimentsInitialementSelectionnes =
                                                                                        alimentsSelectionnes,
                                                                                onSelectionChanged = {
                                                                                        nouvelleSelection
                                                                                        ->
                                                                                        viewModel
                                                                                                .setAlimentsSelectionnes(
                                                                                                        nouvelleSelection
                                                                                                )
                                                                                },
                                                                                filtersInitial =
                                                                                        analyseSelectionFilters,
                                                                                onFiltersChange = {
                                                                                        viewModel
                                                                                                .setAnalyseSelectionFilters(
                                                                                                        it
                                                                                                )
                                                                                },
                                                                                onLoadNutrients = { foodUuids, nutrients ->
                                                                                        viewModel.loadNutrientsForFoods(foodUuids, nutrients)
                                                                                }, // ✨ Synchroniser
                                                                                // avec le
                                                                                // ViewModel
                                                                                modifier =
                                                                                        Modifier.fillMaxSize()
                                                                        )
                                                                }
                                                        } else {
                                                                Column(
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                                        .padding(
                                                                                                AppSizes.paddingMedium
                                                                                        ),
                                                                        verticalArrangement =
                                                                                Arrangement.Center,
                                                                        horizontalAlignment =
                                                                                Alignment
                                                                                        .CenterHorizontally
                                                                ) {
                                                                        Text(
                                                                                translate(AnimalDetail.NO_FOOD_AVAILABLE),
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .h5,
                                                                                color =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )
                                                                        Text(
                                                                                translate(AnimalDetail.NO_FOOD_GRAPH_AVAILABLE),
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .body1,
                                                                                color =
                                                                                        MaterialTheme
                                                                                                .colors
                                                                                                .onSurface
                                                                                                .copy(
                                                                                                        alpha =
                                                                                                                0.7f
                                                                                                )
                                                                        )
                                                                }
                                                        }
                                                }
                                                AnimalDetailSection.EXPORT -> {
                                                        val selectedRation by
                                                                viewModel.selectedRation
                                                                        .collectAsState()
                                                        val referenceUtilisee by
                                                                viewModel.referenceUtilisee
                                                                        .collectAsState()
                                                        val besoinEnergetiqueStandard by viewModel.besoinEnergetiqueStandard.collectAsState()
                                                        val poidsMetabolique by viewModel.poidsMetabolique.collectAsState()
                                                        // Variables pour la prévisualisation et l'export
                                                        var showPreview by remember {
                                                                mutableStateOf(false)
                                                        }
                                                        var previewHtml by remember {
                                                                mutableStateOf("")
                                                        }
                                                        var previewMode by remember { mutableStateOf("PRESCRIPTION") }
                                                        var previewCompteRenduText by remember { mutableStateOf("") }

                                                        // Dialogue de prévisualisation HTML
                                                        HtmlPreviewDialog(
                                                                html = previewHtml,
                                                                isVisible = showPreview,
                                                                onConfirmExport = {
                                                                        val selectedRationsForPrescription: List<Ration> =
                                                                                selectedConsultation?.rations
                                                                                        ?.filter { ration: Ration ->
                                                                                                selectedRationIdsForPrescription
                                                                                                        .contains(
                                                                                                                ration.uuid
                                                                                                        )
                                                                                        }
                                                                                        ?.toList()
                                                                                                ?: emptyList()
                                                                        handlePdfExport(
                                                                                previewHtml = previewHtml,
                                                                                previewMode = previewMode,
                                                                                compteRenduText = previewCompteRenduText,
                                                                                animalDetails = animalDetails,
                                                                                selectedConsultation = selectedConsultation,
                                                                                selectedRation = selectedRation,
                                                                                selectedRationsForPrescription = selectedRationsForPrescription,
                                                                                referenceUtilisee = referenceUtilisee,
                                                                                additionalText = additionalText,
                                                                                getSelectedConseils = getSelectedConseils,
                                                                                besoinEnergetiqueStandard = besoinEnergetiqueStandard,
                                                                                poidsMetabolique = poidsMetabolique,
                                                                                equationRepository = equationRepository,
                                                                                scope = scope
                                                                        )
                                                                        showPreview = false
                                                                },
                                                                onDismiss = { showPreview = false }
                                                        )

                                                        if (showRichTextEditor) {
                                                                // Éditeur de texte enrichi
                                                                Column(
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                ) {
                                                                        Row(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth()
                                                                                                .padding(
                                                                                                        AppSizes.paddingMedium
                                                                                                ),
                                                                                horizontalArrangement =
                                                                                        Arrangement
                                                                                                .SpaceBetween,
                                                                                verticalAlignment =
                                                                                        Alignment
                                                                                                .CenterVertically
                                                                        ) {
                                                                                Text(
                                                                                        translate(AnimalDetail.HTML_EDITOR),
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                       .typography
                                                                                                       .h6,
                                                                                        color =
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                )
                                                                                Button(
                                                                                        onClick = {
                                                                                                showRichTextEditor =
                                                                                                        false
                                                                                        },
                                                                                        colors =
                                                                                                ButtonDefaults
                                                                                                        .buttonColors(
                                                                                                                backgroundColor =
                                                                                                                        VetNutriColors
                                                                                                                                .Secondary,
                                                                                                                contentColor =
                                                                                                                        VetNutriColors
                                                                                                                                .OnSecondary
                                                                                                        )
                                                                                ) { Text(translate(AnimalDetail.BACK_TO_EXPORT)) }
                                                                        }

                                                                        RichTextEditor(
                                                                                initialContent =
                                                                                        currentHtmlContent,
                                                                                onContentChange = {
                                                                                        content ->
                                                                                        currentHtmlContent =
                                                                                                content
                                                                                },
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        )
                                                                        )

                                                                        // Boutons d'action
                                                                        Row(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth()
                                                                                                .padding(
                                                                                                        AppSizes.paddingMedium
                                                                                                ),
                                                                                horizontalArrangement =
                                                                                        Arrangement
                                                                                                .spacedBy(
                                                                                                        AppSizes.paddingSmall
                                                                                                )
                                                                        ) {
                                                                                Button(
                                                                                        onClick = {
                                                                                                // Créer une nouvelle section HTML
                                                                                                val newSection =
                                                                                                        fr.vetbrain
                                                                                                                .vetnutri_mp
                                                                                                                .Export
                                                                                                                .HtmlSection(
                                                                                                                        id =
                                                                                                                                "section_${kotlinx.datetime.Clock.System.now().toEpochMilliseconds()}",
                                                                                                                        title =
                                                                                                                                translate(
                                                                                                                                        AnimalDetail.CUSTOM_SECTION_TITLE,
                                                                                                                                        (availableConseils.size + 1).toString()
                                                                                                                                ),
                                                                                                                        content =
                                                                                                                                currentHtmlContent,
                                                                                                                        category =
                                                                                                                                fr.vetbrain
                                                                                                                                        .vetnutri_mp
                                                                                                                                        .Export
                                                                                                                                        .SectionCategory
                                                                                                                                        .CUSTOM
                                                                                                                )
                                                                                                // Ajouter à la liste des sections HTML locales
                                                                                                localHtmlSections =
                                                                                                        localHtmlSections +
                                                                                                                newSection
                                                                                                schedulePrescriptionSave()
                                                                                                currentHtmlContent =
                                                                                                        fr.vetbrain
                                                                                                                .vetnutri_mp
                                                                                                                .Export
                                                                                                                .RichTextContent()
                                                                                                showRichTextEditor =
                                                                                                        false
                                                                                        },
                                                                                        enabled =
                                                                                                currentHtmlContent
                                                                                                        .blocks
                                                                                                        .isNotEmpty()
                                                                                ) {
                                                                                        Text(translate(AnimalDetail.ADD_SECTION))
                                                                                }

                                                                                OutlinedButton(
                                                                                        onClick = {
                                                                                                currentHtmlContent =
                                                                                                        fr.vetbrain
                                                                                                                .vetnutri_mp
                                                                                                                .Export
                                                                                                                .RichTextContent()
                                                                                        }
                                                                                ) {
                                                                                        Text(translate(AnimalDetail.CLEAR_SECTION))
                                                                                }
                                                                        }
                                                                }
                                                        } else {
                                                                // Section export normale
                                                                LazyColumn(
                                                                        modifier =
                                                                                Modifier.fillMaxSize()
                                                                                        .padding(
                                                                                                AppSizes.paddingMedium
                                                                                        ),
                                                                        verticalArrangement =
                                                                                Arrangement
                                                                                        .spacedBy(
                                                                                                AppSizes.paddingMedium
                                                                                        )
                                                                ) {
                                                                        item {
                                                                        Text(
                                                                                translate(AnimalDetail.EXPORT_DOCUMENTS_TITLE),
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .h6,
                                                                                color =
                                                                                        VetNutriColors
                                                                                                .Primary
                                                                        )
                                                                        }
                                                                        // Ligne d'information sur la ration sélectionnée supprimée pour alléger l'UI
                                                                        item {
                                                                        if (selectedConsultation == null) {
                                                                                Text(
                                                                                        translate(AnimalDetail.NO_CONSULTATION_FOR_PRESCRIPTION),
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .subtitle1,
                                                                                        color =
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                )
                                                                        } else {
                                                                                Text(
                                                                                        translate(AnimalDetail.SELECT_RATIONS_FOR_PRESCRIPTION),
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .subtitle1,
                                                                                        color =
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                )
                                                                        }
                                                                        }

                                                                        val currentConsultation: ConsultationEv? = selectedConsultation
                                                                        if (currentConsultation != null && currentConsultation.rations.isNotEmpty()) {
                                                                                items(currentConsultation.rations) { ration ->
                                                                                Row(
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth()
                                                                                                        .padding(
                                                                                                                vertical =
                                                                                                                        0.dp
                                                                                                        ),
                                                                                        verticalAlignment =
                                                                                                Alignment
                                                                                                        .CenterVertically
                                                                                ) {
                                                                                        val isSelectedRation: Boolean =
                                                                                                selectedRationIdsForPrescription
                                                                                                        .contains(
                                                                                                                ration.uuid
                                                                                                        )
                                                                                        Checkbox(
                                                                                                checked = isSelectedRation,
                                                                                                onCheckedChange = { isChecked: Boolean ->
                                                                                                        selectedRationIdsForPrescription =
                                                                                                                if (isChecked) {
                                                                                                                        selectedRationIdsForPrescription +
                                                                                                                                ration.uuid
                                                                                                                } else {
                                                                                                                        selectedRationIdsForPrescription -
                                                                                                                                ration.uuid
                                                                                                                }
                                                                                                        schedulePrescriptionSave()
                                                                                                }
                                                                                        )
                                                                                        Spacer(
                                                                                                modifier =
                                                                                                        Modifier.width(
                                                                                                                8.dp
                                                                                                        )
                                                                                        )
                                                                                        Column {
                                                                                                val rationLabel: String =
                                                                                                        if (ration.actual) {
                                                                                                                translate(AnimalDetail.RATION_CURRENT)
                                                                                                        } else {
                                                                                                                translate(AnimalDetail.RATION_PROPOSED)
                                                                                                        }
                                                                                                Text(
                                                                                                        text =
                                                                                                                translate(
                                                                                                                        AnimalDetail.RATION_LINE,
                                                                                                                        ration.name,
                                                                                                                        rationLabel,
                                                                                                                        ration.getQuantiteTotale().toString()
                                                                                                                ),
                                                                                                        style =
                                                                                                                MaterialTheme
                                                                                                                       .typography
                                                                                                                       .body2
                                                                                                )
                                                                                        }
                                                                                }
                                                                                }
                                                                        } else {
                                                                                item {
                                                                                Text(
                                                                                        translate(AnimalDetail.NO_RATION_AVAILABLE),
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
                                                                                                                        0.7f
                                                                                                        )
                                                                                )
                                                                                }
                                                                        }

                                                                        if (isExamMode) {
                                                                                item {
                                                                                Text(
                                                                                        "Conseils personnalisés indisponibles en mode examen.",
                                                                                        style = MaterialTheme.typography.body2,
                                                                                        color = Color.Gray
                                                                                )
                                                                                }
                                                                        } else {
                                                                                // Section pour les conseils
                                                                                // personnalisés
                                                                                item {
                                                                                Text(
                                                                                        translate(AnimalDetail.CUSTOM_ADVICE_TITLE),
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .subtitle1,
                                                                                        color =
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                )
                                                                                }

                                                                                // Affichage des conseils
                                                                                // sélectionnés
                                                                                if (selectedConseils
                                                                                                .isNotEmpty()
                                                                                ) {
                                                                                        item {
                                                                                        Column(
                                                                                                modifier =
                                                                                                        Modifier.fillMaxWidth(),
                                                                                                verticalArrangement =
                                                                                                        Arrangement
                                                                                                                .spacedBy(
                                                                                                                        4.dp
                                                                                                                )
                                                                                        ) {
                                                                                                selectedConseils
                                                                                                        .forEach {
                                                                                                                conseil
                                                                                                                ->
                                                                                                                Card(
                                                                                                                        modifier =
                                                                                                                                Modifier.fillMaxWidth(),
                                                                                                                        elevation =
                                                                                                                                2.dp
                                                                                                                ) {
                                                                                                                        Row(
                                                                                                                                modifier =
                                                                                                                                        Modifier.fillMaxWidth()
                                                                                                                                                .padding(
                                                                                                                                                        8.dp
                                                                                                                                                ),
                                                                                                                                horizontalArrangement =
                                                                                                                                        Arrangement
                                                                                                                                                .SpaceBetween,
                                                                                                                                verticalAlignment =
                                                                                                                                        Alignment
                                                                                                                                                .CenterVertically
                                                                                                                        ) {
                                                                                                                                Column(
                                                                                                                                        modifier =
                                                                                                                                                Modifier.weight(
                                                                                                                                                        1f
                                                                                                                                                )
                                                                                                                                ) {
                                                                                                                                        Text(
                                                                                                                                                text =
                                                                                                                                                        conseil.title,
                                                                                                                                                style =
                                                                                                                                                        MaterialTheme
                                                                                                                                                                .typography
                                                                                                                                                                .body2,
                                                                                                                                                fontWeight =
                                                                                                                                                        FontWeight
                                                                                                                                                                .Medium
                                                                                                                                        )
                                                                                                                                        Text(
                                                                                                                                                text =
                                                                                                                                                        translate(
                                                                                                                                                                AnimalDetail.CATEGORY_LABEL,
                                                                                                                                                                conseil.category.name
                                                                                                                                                        ),
                                                                                                                                                style =
                                                                                                                                                        MaterialTheme
                                                                                                                                                                .typography
                                                                                                                                                                .caption,
                                                                                                                                                color =
                                                                                                                                                        Color.Gray
                                                                                                                                        )
                                                                                                                                }
                                                                                                                                IconButtonWithTooltip(
                                                                                                                                                onClick = {
                                                                                                                                                        selectedConseils =
                                                                                                                                                                selectedConseils
                                                                                                                                                                        .filter {
                                                                                                                                                                                it.id !=
                                                                                                                                                                                        conseil.id
                                                                                                                                                                        }
                                                                                                                                                        schedulePrescriptionSave()
                                                                                                                                                },
                                                                                                                                        imageVector = Icons.Default.Delete,
                                                                                                                                        contentDescription = translate(General.DELETE),
                                                                                                                                        tooltip = translate(General.DELETE),
                                                                                                                                        tint = Color.Red
                                                                                                                                )
                                                                                                                                }
                                                                                                                        }
                                                                                                                }
                                                                                                        }
                                                                                        }
                                                                                }

                                                                                // Bouton pour ajouter des
                                                                                // conseils
                                                                                item {
                                                                                Button(
                                                                                        onClick = {
                                                                                                showSearchDialog =
                                                                                                        true
                                                                                        },
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth(),
                                                                                        colors =
                                                                                                ButtonDefaults
                                                                                                        .buttonColors(
                                                                                                                backgroundColor =
                                                                                                                        VetNutriColors
                                                                                                                                .Secondary,
                                                                                                                contentColor =
                                                                                                                        VetNutriColors
                                                                                                                                .OnSecondary
                                                                                                        )
                                                                                ) {
                                                                                        Icon(
                                                                                                Icons.Default
                                                                                                        .Add,
                                                                                                translate(General.ADD)
                                                                                        )
                                                                                        Spacer(
                                                                                                modifier =
                                                                                                        Modifier.width(
                                                                                                                8.dp
                                                                                                        )
                                                                                        )
                                                                                        Text(translate(AnimalDetail.ADD_ADVICE))
                                                                                        }
                                                                                }
                                                                        }

                                                                        item {
                                                                        Spacer(
                                                                                modifier =
                                                                                        Modifier.height(
                                                                                                16.dp
                                                                                        )
                                                                        )
                                                                        }

                                                                        // Section pour les sections
                                                                        // HTML créées localement
                                                                        if (localHtmlSections
                                                                                        .isNotEmpty()
                                                                        ) {
                                                                                item {
                                                                                Text(
                                                                                        translate(
                                                                                                AnimalDetail.LOCAL_HTML_SECTIONS_TITLE,
                                                                                                localHtmlSections.size.toString()
                                                                                        ),
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .subtitle1,
                                                                                        color =
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                )
                                                                                }
                                                                                item {
                                                                                Column(
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth(),
                                                                                        verticalArrangement =
                                                                                                Arrangement
                                                                                                        .spacedBy(
                                                                                                                4.dp
                                                                                                        )
                                                                                ) {
                                                                                        localHtmlSections
                                                                                                .forEach {
                                                                                                        section
                                                                                                        ->
                                                                                                        Card(
                                                                                                                modifier =
                                                                                                                        Modifier.fillMaxWidth(),
                                                                                                                elevation =
                                                                                                                        2.dp
                                                                                                        ) {
                                                                                                                Row(
                                                                                                                        modifier =
                                                                                                                                Modifier.fillMaxWidth()
                                                                                                                                        .padding(
                                                                                                                                                8.dp
                                                                                                                                        ),
                                                                                                                        horizontalArrangement =
                                                                                                                                Arrangement
                                                                                                                                        .SpaceBetween,
                                                                                                                        verticalAlignment =
                                                                                                                                Alignment
                                                                                                                                        .CenterVertically
                                                                                                                ) {
                                                                                                                        Column(
                                                                                                                                modifier =
                                                                                                                                        Modifier.weight(
                                                                                                                                                1f
                                                                                                                                        )
                                                                                                                        ) {
                                                                                                                                Text(
                                                                                                                                        text =
                                                                                                                                                section.title,
                                                                                                                                        style =
                                                                                                                                                MaterialTheme
                                                                                                                                                        .typography
                                                                                                                                                        .body2,
                                                                                                                                        fontWeight =
                                                                                                                                                FontWeight
                                                                                                                                                        .Medium
                                                                                                                                )
                                                                                                                                Text(
                                                                                                                                        text =
                                                                                                                                                translate(
                                                                                                                                                        AnimalDetail.BLOCKS_COUNT,
                                                                                                                                                        section.content.blocks.size.toString()
                                                                                                                                                ),
                                                                                                                                        style =
                                                                                                                                                MaterialTheme
                                                                                                                                                        .typography
                                                                                                                                                        .caption,
                                                                                                                                        color =
                                                                                                                                                Color.Gray
                                                                                                                                )
                                                                                                                        }
                                                                                                                        IconButtonWithTooltip(
                                                                                                        onClick = {
                                                                                                                localHtmlSections =
                                                                                                                        localHtmlSections
                                                                                                                                .filter {
                                                                                                                                        it.id !=
                                                                                                                                                section.id
                                                                                                                                }
                                                                                                                schedulePrescriptionSave()
                                                                                                        },
                                                                                                                                imageVector = Icons.Default.Delete,
                                                                                                                                contentDescription = translate(General.DELETE),
                                                                                                                                tooltip = translate(General.DELETE),
                                                                                                                                tint = Color.Red
                                                                                                                        )
                                                                                                                }
                                                                                                        }
                                                                                                }
                                                                                }
                                                                                }
                                                                                item {
                                                                                Spacer(
                                                                                        modifier =
                                                                                                Modifier.height(
                                                                                                        16.dp
                                                                                                )
                                                                                )
                                                                                }
                                                                        }

                                                                        // Bouton pour accéder à
                                                                        // l'éditeur de texte
                                                                        // enrichi
                                                                        item {
                                                                        Button(
                                                                                onClick = {
                                                                                        showRichTextEditor =
                                                                                                true
                                                                                },
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth(),
                                                                                colors =
                                                                                        ButtonDefaults
                                                                                                .buttonColors(
                                                                                                        backgroundColor =
                                                                                                                VetNutriColors
                                                                                                                        .Secondary,
                                                                                                        contentColor =
                                                                                                                VetNutriColors
                                                                                                                        .OnSecondary
                                                                                                )
                                                                        ) {
                                                                                Icon(
                                                                                        Icons.Default
                                                                                                .Edit,
                                                                                        translate(AnimalDetail.HTML_EDITOR)
                                                                                )
                                                                                Spacer(
                                                                                        modifier =
                                                                                                Modifier.width(
                                                                                                        AppSizes.paddingSmall
                                                                                                )
                                                                                )
                                                                                Text(translate(AnimalDetail.CREATE_CUSTOM_HTML_SECTIONS))
                                                                                }
                                                                        }


                                                                        item {
                                                                        OutlinedTextField(
                                                                                value = anamneseText,
                                                                                onValueChange = {
                                                                                        anamneseText = it
                                                                                        schedulePrescriptionSave()
                                                                                },
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                label = { Text("Anamnese") },
                                                                                maxLines = 6
                                                                        )
                                                                                }

                                                                        item {
                                                                        OutlinedTextField(
                                                                                value = examenCliniqueText,
                                                                                onValueChange = {
                                                                                        examenCliniqueText = it
                                                                                        schedulePrescriptionSave()
                                                                                },
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                label = { Text("Examen clinique") },
                                                                                maxLines = 6
                                                                        )
                                                                                }

                                                                        item {
                                                                        OutlinedTextField(
                                                                                value = facteurNutritionnelClefText,
                                                                                onValueChange = {
                                                                                        facteurNutritionnelClefText = it
                                                                                        schedulePrescriptionSave()
                                                                                },
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                label = { Text("Facteur nutritionnel clef") },
                                                                                maxLines = 4
                                                                        )
                                                                                }

                                                                        item {
                                                                        OutlinedTextField(
                                                                                value = additionalText,
                                                                                onValueChange = {
                                                                                        additionalText = it
                                                                                        schedulePrescriptionSave()
                                                                                },
                                                                                modifier = Modifier.fillMaxWidth(),
                                                                                label = {
                                                                                        Text(
                                                                                                translate(AnimalDetail.ADDITIONAL_TEXT_LABEL)
                                                                                        )
                                                                                },
                                                                                maxLines = 6
                                                                        )
                                                                                }

                                                                        item {
                                                                        Row(
                                                                                horizontalArrangement =
                                                                                        Arrangement
                                                                                                .spacedBy(
                                                                                                        AppSizes.paddingSmall
                                                                                                )
                                                                        ) {
                                                                                val compteRenduText =
                                                                                        buildCompteRenduText(
                                                                                                animal = animalDetails,
                                                                                                consultation = selectedConsultation,
                                                                                                practitionerContact = practitionerContact,
                                                                                                anamnese = anamneseText,
                                                                                                examenClinique = examenCliniqueText,
                                                                                                facteurNutritionnelClef = facteurNutritionnelClefText,
                                                                                                additionalText = additionalText,
                                                                                                selectedConseils = selectedConseils
                                                                                        )
                                                                                OutlinedButton(
                                                                                        onClick = {
                                                                                                pendingCopyText = compteRenduText
                                                                                                scope.launch {
                                                                                                        snackbarHostState.showSnackbar("CR copié dans le presse-papiers")
                                                                                                }
                                                                                        }
                                                                                ) {
                                                                                        Text("Copier le CR")
                                                                                }
                                                                                OutlinedButton(
                                                                                        onClick = {
                                                                                                previewMode = "CR"
                                                                                                previewCompteRenduText = compteRenduText
                                                                                                previewHtml =
                                                                                                        buildCompteRenduHtml(
                                                                                                                animal = animalDetails,
                                                                                                                consultation = selectedConsultation,
                                                                                                                practitionerContact = practitionerContact,
                                                                                                                anamnese = anamneseText,
                                                                                                                examenClinique = examenCliniqueText,
                                                                                                                facteurNutritionnelClef = facteurNutritionnelClefText,
                                                                                                                additionalText = additionalText,
                                                                                                                selectedConseils = selectedConseils
                                                                                                        )
                                                                                                showPreview = true
                                                                                        }
                                                                                ) {
                                                                                        Text("Compte rendu")
                                                                                }

                                                                                Button(
                                                                                        onClick = {
                                                                                                scope.launch {
                                                                                                        try {
                                                                                                                val prefsStorage = createPreferencesStorage()
                                                                                                                val prefsRepo = PreferencesRepository(prefsStorage)
                                                                                                                prefsRepo.loadPreferences()
                                                                                                                val prefs = prefsRepo.preferences
                                                                                                                val practitioner = fr.vetbrain.vetnutri_mp.Export.PractitionerInfo(
                                                                                                                        nom = prefs.nomUtilisateur,
                                                                                                                        numeroOrdre = prefs.numeroOrdre,
                                                                                                                        adressePostale = prefs.adressePostale,
                                                                                                                        codePostal = prefs.codePostal,
                                                                                                                        ville = prefs.ville,
                                                                                                                        telephone = prefs.telephone,
                                                                                                                        email = prefs.email
                                                                                                                )
                                                                                                                val selectedRationsForPrescription: List<Ration> =
                                                                                                                        selectedConsultation?.rations
                                                                                                                                ?.filter { ration: Ration ->
                                                                                                                                        selectedRationIdsForPrescription
                                                                                                                                                .contains(
                                                                                                                                                        ration.uuid
                                                                                                                                                )
                                                                                                                                }
                                                                                                                                ?.toList()
                                                                                                                                        ?: emptyList()
                                                                                                                previewHtml =
                                                                                                                        HtmlDocumentBuilder
                                                                                                                                .buildHtml(
                                                                                                                                        DocumentType.PRESCRIPTION,
                                                                                                                                        ExportData(
                                                                                                                                                animal = animalDetails,
                                                                                                                                                ration = null,
                                                                                                                                                reference = referenceUtilisee,
                                                                                                                                                conseils = listOf(translate(AnimalDetail.DEFAULT_ADVICE_HYDRATION)),
                                                                                                                                                title = translate(AnimalDetail.PRESCRIPTION_TITLE),
                                                                                                                                                additionalText = additionalText,
                                                                                                                                                htmlSections = getSelectedConseils(),
                                                                                                                                                rations = selectedRationsForPrescription,
                                                                                                                                                practitioner = practitioner,
                                                                                                                                                preferences = null,
                                                                                                                                                poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                                                                                                                                                poidsMetabolique = null,
                                                                                                                                                besoinEnergetiqueEntretien = null
                                                                                                                )
                                                                                                        )
                                                                                                                previewMode = "PRESCRIPTION"
                                                                                                                previewCompteRenduText = ""
                                                                                                                showPreview = true
                                                                                                        } catch (e: Exception) {
                                                                                                                val selectedRationsForPrescription: List<Ration> =
                                                                                                                        selectedConsultation?.rations
                                                                                                                                ?.filter { ration: Ration ->
                                                                                                                                        selectedRationIdsForPrescription
                                                                                                                                                .contains(
                                                                                                                                                        ration.uuid
                                                                                                                                                )
                                                                                                                                }
                                                                                                                                ?.toList()
                                                                                                                                        ?: emptyList()
                                                                                                                previewHtml = HtmlDocumentBuilder.buildHtml(
                                                                                                                        DocumentType.PRESCRIPTION,
                                                                                                                        ExportData(
                                                                                                                                animal = animalDetails,
                                                                                                                                ration = null,
                                                                                                                                reference = referenceUtilisee,
                                                                                                                                conseils = emptyList(),
                                                                                                                                title = translate(AnimalDetail.PRESCRIPTION_TITLE),
                                                                                                                                additionalText = additionalText,
                                                                                                                                htmlSections = getSelectedConseils(),
                                                                                                                                rations = selectedRationsForPrescription,
                                                                                                                                practitioner = null,
                                                                                                                                preferences = null,
                                                                                                                                poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                                                                                                                                poidsMetabolique = null,
                                                                                                                                besoinEnergetiqueEntretien = null
                                                                                                                )
                                                                                                                )
                                                                                                                previewMode = "PRESCRIPTION"
                                                                                                                previewCompteRenduText = ""
                                                                                                                showPreview = true
                                                                                                        }
                                                                                                }
                                                                                        }
                                                                                ) {
                                                                                        Text(translate(AnimalDetail.PREVIEW_PRESCRIPTION))
                                                                                }
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }

                        if (pendingCopyText != null) {
                                copyToClipboardComposable(pendingCopyText!!)
                                LaunchedEffect(pendingCopyText) { pendingCopyText = null }
                        }

                        // Dialogue de recherche et sélection des conseils
                        if (showSearchDialog) {
                                AlertDialog(
                                        onDismissRequest = { showSearchDialog = false },
                                        title = { Text(translate(AnimalDetail.ADD_ADVICE)) },
                                        text = {
                                                Column {
                                                        OutlinedTextField(
                                                                value = searchQuery,
                                                                onValueChange = {
                                                                        searchQuery = it
                                                                },
                                                                label = {
                                                                        Text(
                                                                                translate(AnimalDetail.SEARCH_ADVICE_HINT)
                                                                        )
                                                                },
                                                                modifier = Modifier.fillMaxWidth()
                                                        )

                                                        Spacer(modifier = Modifier.height(16.dp))

                                                        val filteredConseils =
                                                                availableConseils.filter { conseil
                                                                        ->
                                                                        conseil.title.contains(
                                                                                searchQuery,
                                                                                ignoreCase = true
                                                                        ) ||
                                                                                conseil.category
                                                                                        .name
                                                                                        .contains(
                                                                                                searchQuery,
                                                                                                ignoreCase =
                                                                                                        true
                                                                                        )
                                                                }

                                                        LazyColumn(
                                                                modifier =
                                                                        Modifier.heightIn(
                                                                                max = 300.dp
                                                                        ),
                                                                verticalArrangement =
                                                                        Arrangement.spacedBy(4.dp)
                                                        ) {
                                                                items(filteredConseils) { conseil ->
                                                                        val isAlreadySelected =
                                                                                selectedConseils
                                                                                        .any {
                                                                                                it.id ==
                                                                                                        conseil.id
                                                                                        }

                                                                        Card(
                                                                                modifier =
                                                                                        Modifier.fillMaxWidth(),
                                                                                elevation =
                                                                                        if (isAlreadySelected
                                                                                        )
                                                                                                4.dp
                                                                                        else 1.dp,
                                                                                backgroundColor =
                                                                                        if (isAlreadySelected
                                                                                        )
                                                                                                VetNutriColors
                                                                                                        .Primary
                                                                                                        .copy(
                                                                                                                alpha =
                                                                                                                        0.1f
                                                                                                        )
                                                                                        else
                                                                                                Color.Transparent
                                                                        ) {
                                                                                Row(
                                                                                        modifier =
                                                                                                Modifier.fillMaxWidth()
                                                                                                        .padding(
                                                                                                                12.dp
                                                                                                        ),
                                                                                        horizontalArrangement =
                                                                                                Arrangement
                                                                                                        .SpaceBetween,
                                                                                        verticalAlignment =
                                                                                                Alignment
                                                                                                        .CenterVertically
                                                                                ) {
                                                                                        Column(
                                                                                                modifier =
                                                                                                        Modifier.weight(
                                                                                                                1f
                                                                                                        )
                                                                                        ) {
                                                                                                Text(
                                                                                                        text =
                                                                                                                conseil.title,
                                                                                                        style =
                                                                                                                MaterialTheme
                                                                                                                        .typography
                                                                                                                        .body1,
                                                                                                        fontWeight =
                                                                                                                FontWeight
                                                                                                                        .Medium
                                                                                                )
                                                                                                Text(
                                                                                                        text =
                                                                                                                translate(
                                                                                                                        AnimalDetail.CATEGORY_LABEL,
                                                                                                                        conseil.category.name
                                                                                                                ),
                                                                                                        style =
                                                                                                                MaterialTheme
                                                                                                                       .typography
                                                                                                                       .caption,
                                                                                                        color =
                                                                                                                Color.Gray
                                                                                                )
                                                                                        }

                                                                                        if (isAlreadySelected
                                                                                        ) {
                                                                                                Icon(
                                                                                                        Icons.Default
                                                                                                                .Check,
                                                                                                        translate(AnimalDetail.SELECTED),
                                                                                                        tint =
                                                                                                                VetNutriColors
                                                                                                                       .Primary
                                                                                                )
                                                                                        } else {
                                                                                                IconButtonWithTooltip(
                                                                                                        onClick = {
                                                                                                                selectedConseils =
                                                                                                                        selectedConseils +
                                                                                                                                conseil
                                                                                                                schedulePrescriptionSave()
                                                                                                        },
                                                                                                        imageVector = Icons.Default.Add,
                                                                                                        contentDescription = translate(General.ADD),
                                                                                                        tooltip = translate(General.ADD),
                                                                                                        tint = VetNutriColors.Primary
                                                                                                )
                                                                                        }
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        },
                                        confirmButton = {
                                                TextButton(onClick = { showSearchDialog = false }) {
                                                        Text(translate(General.CLOSE))
                                                }
                                        }
                                )
                        }

                }
        )
}
