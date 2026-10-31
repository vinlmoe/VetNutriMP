package fr.vetbrain.vetnutri_mp.View

import fr.vetbrain.vetnutri_mp.Utils.rethrowIfCancellation
import fr.vetbrain.vetnutri_mp.Utils.AppLog
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.runtime.*
import fr.vetbrain.vetnutri_mp.Data.AlimentEv
import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Data.ConsultationEv
import fr.vetbrain.vetnutri_mp.Data.Ration
import fr.vetbrain.vetnutri_mp.Export.DocumentType
import fr.vetbrain.vetnutri_mp.Export.ExportData
import fr.vetbrain.vetnutri_mp.Export.PdfExporter
import fr.vetbrain.vetnutri_mp.Localization.translate
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.AnimalDetail
import fr.vetbrain.vetnutri_mp.Localization.LocalizationKeys.Animal
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.RecipeRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.exportApiEnvelopeToFile
import kotlinx.coroutines.withContext
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalDetailViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import fr.vetbrain.vetnutri_mp.Service.ShareOptions
import fr.vetbrain.vetnutri_mp.Utils.anonymizeExportJson
import kotlin.math.roundToInt

// Compte rendu et ordonnance de la fiche animal : texte, HTML et export PDF (sans UI)

internal data class PractitionerContact(
        val nom: String = "",
        val numeroOrdre: String = "",
        val adressePostale: String = "",
        val codePostal: String = "",
        val ville: String = "",
        val telephone: String = "",
        val email: String = ""
)

internal fun formatAlimentDisplayName(aliment: AlimentEv?): String {
        val fallback = "Ingredient"
        if (aliment == null) return fallback
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
                val semantic = normalized.lowercase().filter { it.isLetterOrDigit() }
                if (semantic == "null" || semantic == "none" || semantic == "na") return null
                return normalized
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
                                val semantic = value.lowercase().filter { it.isLetterOrDigit() }
                                value.isNotBlank() &&
                                        semantic != "null" &&
                                        semantic != "none" &&
                                        semantic != "na"
                        }
        val result = if (parts.isEmpty()) fallback else parts.joinToString(", ")
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

internal fun buildCompteRenduText(
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
                val rationName = if (ration.name.isBlank()) translate(AnimalDetail.CR_NO_NAME) else ration.name
                val ingredients = ration.alimentMutableList.joinToString("\n") { alimentRation ->
                        val ingredientName = formatAlimentDisplayName(alimentRation.aliment)
                        val quantity = (alimentRation.quantite * 10.0).roundToInt() / 10.0
                        "- $ingredientName: $quantity g"
                }
                val details = if (ingredients.isBlank()) "- ${translate(AnimalDetail.CR_NO_INGREDIENT)}" else ingredients
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
                appendLine(translate(AnimalDetail.CR_TITLE))
                appendLine("${translate(AnimalDetail.CR_LABEL_ANIMAL_ID)}: ${animal?.id?.takeIf { it.isNotBlank() } ?: "-"}")
                appendLine("${translate(AnimalDetail.CR_LABEL_CONSULT_DATE)}: ${consultation?.date?.toString() ?: "-"}")
                appendLine("${translate(AnimalDetail.CR_LABEL_CONSULT_OBJECT)}: ${consultation?.objectConsult?.takeIf { it.isNotBlank() } ?: "-"}")
                appendLine()
                appendLine(translate(AnimalDetail.CR_SECTION_IDENTIFICATION))
                appendLine("${translate(AnimalDetail.CR_LABEL_NAME)}: ${text(animal?.nom)}")
                appendLine("${translate(AnimalDetail.CR_LABEL_OWNER)}: ${text(animal?.ownerName)}")
                appendLine("${translate(AnimalDetail.CR_LABEL_SEX)}: ${text(animal?.getSex()?.displayName)}")
                appendLine("${translate(AnimalDetail.CR_LABEL_SPECIES)}: ${text(animal?.getEspece()?.label)}")
                appendLine("${translate(AnimalDetail.CR_LABEL_BREED)}: ${text(animal?.race)}")
                appendLine("${translate(AnimalDetail.CR_LABEL_BIRTHDATE)}: ${text(animal?.birthdate?.toString())}")
                appendLine("${translate(AnimalDetail.CR_LABEL_CONSULT_WEIGHT)}: ${consultation?.effectiveWeight?.let { translate(AnimalDetail.WEIGHT_KG_FORMAT, ((it * 10.0).roundToInt() / 10.0).toString()) } ?: "-"}")
                appendLine("${translate(AnimalDetail.CR_LABEL_UUID)}: ${text(animal?.uuid)}")
                appendLine()
                appendLine(translate(AnimalDetail.CR_SECTION_VET_CONTACT))
                appendLine("${translate(AnimalDetail.CR_LABEL_NAME)}: ${practitionerContact?.nom?.ifBlank { "-" } ?: "-"}")
                appendLine("${translate(AnimalDetail.CR_LABEL_ORDER_NUMBER)}: ${practitionerContact?.numeroOrdre?.ifBlank { "-" } ?: "-"}")
                appendLine("${translate(AnimalDetail.CR_LABEL_ADDRESS)}: ${practitionerContact?.adressePostale?.ifBlank { "-" } ?: "-"}")
                appendLine("${translate(AnimalDetail.CR_LABEL_POSTAL_CODE)}: ${practitionerContact?.codePostal?.ifBlank { "-" } ?: "-"}")
                appendLine("${translate(AnimalDetail.CR_LABEL_CITY)}: ${practitionerContact?.ville?.ifBlank { "-" } ?: "-"}")
                appendLine("${translate(AnimalDetail.CR_LABEL_PHONE)}: ${practitionerContact?.telephone?.ifBlank { "-" } ?: "-"}")
                appendLine("${translate(AnimalDetail.CR_LABEL_EMAIL)}: ${practitionerContact?.email?.ifBlank { "-" } ?: "-"}")
                appendLine()
                appendLine(translate(AnimalDetail.CR_TEXT_ANAMNESE_HEADER))
                appendLine(if (anamnese.isBlank()) "-" else anamnese)
                appendLine()
                appendLine(translate(AnimalDetail.CR_EXAM_CLINIQUE))
                appendLine(if (examenClinique.isBlank()) "-" else examenClinique)
                appendLine()
                appendLine(translate(AnimalDetail.CR_CURRENT_RATIONS))
                appendLine(if (currentRations.isBlank()) "-" else currentRations)
                appendLine()
                appendLine(translate(AnimalDetail.CR_KEY_NUTRITIONAL_FACTOR))
                appendLine(if (facteurNutritionnelClef.isBlank()) "-" else facteurNutritionnelClef)
                appendLine()
                appendLine(translate(AnimalDetail.CR_PROPOSED_RATIONS))
                appendLine(if (proposedRations.isBlank()) "-" else proposedRations)
                appendLine()
                appendLine(translate(AnimalDetail.CR_ADVICE_SECTION))
                appendLine(if (conseilsText.isBlank()) "-" else conseilsText)
                appendLine()
                appendLine(translate(AnimalDetail.CR_ADDITIONAL_TEXT_SECTION))
                appendLine(if (additionalText.isBlank()) "-" else additionalText)
        }
}

private fun escapeHtml(value: String): String =
        value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

internal fun buildCompteRenduHtml(
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
                        val rationName = if (ration.name.isBlank()) translate(AnimalDetail.CR_NO_NAME) else escapeHtml(ration.name)
                        val rows = ration.alimentMutableList.joinToString("\n") { ar ->
                                val ingredient = formatAlimentDisplayName(ar.aliment)
                                val quantity = (ar.quantite * 10.0).roundToInt() / 10.0
                                "<tr><td>${escapeHtml(ingredient)}</td><td style='text-align:right'>${quantity} g</td></tr>"
                        }
                        val body =
                                if (rows.isBlank()) {
                                        "<tr><td colspan='2' class='muted'>${translate(AnimalDetail.CR_NO_INGREDIENT)}</td></tr>"
                                } else {
                                        rows
                                }
                        """
                            <div class='card'>
                                <h3>$rationName</h3>
                                <table>
                                    <thead><tr><th>${translate(AnimalDetail.CR_INGREDIENT_LABEL)}</th><th>${translate(AnimalDetail.CR_QUANTITY_LABEL)}</th></tr></thead>
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
                <h1>${translate(AnimalDetail.CR_TITLE)}</h1>
                <div class='meta'><b>${translate(AnimalDetail.CR_LABEL_ANIMAL_ID)}:</b> ${escapeHtml(animal?.id?.takeIf { it.isNotBlank() } ?: "-")} | <b>${translate(AnimalDetail.CR_LABEL_NAME)}:</b> ${escapeHtml(animal?.nom ?: "-")}</div>
                <div class='meta'><b>${translate(AnimalDetail.CR_LABEL_CONSULT_DATE)}:</b> ${escapeHtml(consultation?.date?.toString() ?: "-")} | <b>${translate(AnimalDetail.CR_LABEL_OBJECT_SHORT)}:</b> ${escapeHtml(consultation?.objectConsult?.takeIf { it.isNotBlank() } ?: "-")}</div>

                <div class='section'>
                    <h2>${translate(AnimalDetail.CR_SECTION_IDENTIFICATION)}</h2>
                    <div><b>${translate(AnimalDetail.CR_LABEL_NAME)}:</b> ${escapeHtml(animal?.nom?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>${translate(AnimalDetail.CR_LABEL_OWNER)}:</b> ${escapeHtml(animal?.ownerName?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>${translate(AnimalDetail.CR_LABEL_SEX)}:</b> ${escapeHtml(animal?.getSex()?.displayName?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>${translate(AnimalDetail.CR_LABEL_SPECIES)}:</b> ${escapeHtml(animal?.getEspece()?.label?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>${translate(AnimalDetail.CR_LABEL_BREED)}:</b> ${escapeHtml(animal?.race?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>${translate(AnimalDetail.CR_LABEL_BIRTHDATE)}:</b> ${escapeHtml(animal?.birthdate?.toString() ?: "-")}</div>
                    <div><b>${translate(AnimalDetail.CR_LABEL_CONSULT_WEIGHT)}:</b> ${escapeHtml(consultation?.effectiveWeight?.let { translate(AnimalDetail.WEIGHT_KG_FORMAT, ((it * 10.0).roundToInt() / 10.0).toString()) } ?: "-")}</div>
                    <div><b>${translate(AnimalDetail.CR_LABEL_UUID)}:</b> ${escapeHtml(animal?.uuid ?: "-")}</div>
                </div>

                <div class='section'>
                    <h2>${translate(AnimalDetail.CR_SECTION_VET_CONTACT)}</h2>
                    <div><b>${translate(AnimalDetail.CR_LABEL_NAME)}:</b> ${escapeHtml(practitionerContact?.nom?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>${translate(AnimalDetail.CR_LABEL_ORDER_NUMBER)}:</b> ${escapeHtml(practitionerContact?.numeroOrdre?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>${translate(AnimalDetail.CR_LABEL_ADDRESS)}:</b> ${escapeHtml(practitionerContact?.adressePostale?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>${translate(AnimalDetail.CR_LABEL_POSTAL_CODE)}:</b> ${escapeHtml(practitionerContact?.codePostal?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>${translate(AnimalDetail.CR_LABEL_CITY)}:</b> ${escapeHtml(practitionerContact?.ville?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>${translate(AnimalDetail.CR_LABEL_PHONE)}:</b> ${escapeHtml(practitionerContact?.telephone?.ifBlank { "-" } ?: "-")}</div>
                    <div><b>${translate(AnimalDetail.CR_LABEL_EMAIL)}:</b> ${escapeHtml(practitionerContact?.email?.ifBlank { "-" } ?: "-")}</div>
                </div>

                <div class='section'>
                    <h2>${translate(AnimalDetail.CR_SECTION_ANAMNESE)}</h2>
                    <p>${textOrDash(anamnese)}</p>
                </div>

                <div class='section'>
                    <h2>${translate(AnimalDetail.CR_EXAM_CLINIQUE)}</h2>
                    <p>${textOrDash(examenClinique)}</p>
                </div>

                ${rationBlock(translate(AnimalDetail.CR_CURRENT_RATIONS), consultation?.rations?.filter { it.actual } ?: emptyList())}

                <div class='section'>
                    <h2>${translate(AnimalDetail.CR_KEY_NUTRITIONAL_FACTOR)}</h2>
                    <p>${textOrDash(facteurNutritionnelClef)}</p>
                </div>

                ${rationBlock(translate(AnimalDetail.CR_PROPOSED_RATIONS), consultation?.rations?.filter { !it.actual } ?: emptyList())}

                <div class='section'>
                    <h2>${translate(AnimalDetail.CR_ADVICE_SECTION)}</h2>
                    $conseilsHtml
                </div>

                <div class='section'>
                    <h2>${translate(AnimalDetail.CR_ADDITIONAL_TEXT_SECTION)}</h2>
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
internal suspend fun exporterAnimalComplet(
        animal: AnimalEv,
        viewModel: AnimalDetailViewModel,
        settingsViewModel: SettingsViewModel,
        equationRepository: EquationRepository,
        recipeRepository: RecipeRepository,
        conseilRepository: fr.vetbrain.vetnutri_mp.Repository.ConseilRepository,
        snackbarHostState: SnackbarHostState
) {
        try {
                // Animal, aliments de ses rations, référentiels et équations utilisés
                val envelope = settingsViewModel.exportAnimalEnvelope(
                        animal, equationRepository, recipeRepository, conseilRepository
                )
                
                // Générer le nom de fichier
                val fileName = "${animal.id ?: animal.uuid}_${animal.nom}_export.json"
                
                // Le sélecteur Desktop doit être demandé hors du dispatcher Compose.
                // L'implémentation de plateforme transfère ensuite le dialogue sur l'EDT.
                val success = withContext(AppDispatchers.IO) {
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
                e.rethrowIfCancellation()
                AppLog.e("AnimalDetailView", e.message ?: "Erreur", e)
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
internal suspend fun partagerAnimalEnLigne(
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
                // Animal, aliments de ses rations, référentiels et équations utilisés
                var jsonContent = settingsViewModel.exportAnimalJson(
                        animal, equationRepository, recipeRepository, conseilRepository
                )
                
                // Anonymiser le JSON si demandé
                if (shouldAnonymize) {
                        jsonContent = withContext(AppDispatchers.IO) {
                                anonymizeExportJson(jsonContent)
                        }
                }
                
                // Générer le nom de fichier
                val fileName = "${animal.id ?: animal.uuid}_${animal.nom}_export.json"
                
                // IMPORTANT: Recharger l'animal depuis la BDD pour s'assurer d'avoir le jsonbinId à jour
                val animalFromDb: AnimalEv? = settingsViewModel.reloadAnimal(animal.uuid)
                
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
                                                val updatedAnimal: AnimalEv? = settingsViewModel.reloadAnimal(animalToUse.uuid)
                                                if (updatedAnimal != null) {
                                                        withContext(AppDispatchers.Main) {
                                                                viewModel.setAnimal(updatedAnimal)
                                                        }
                                                }
                                        }
                                        onShareLinkGenerated(shareLink)
                                        snackbarHostState.showSnackbar(
                                                message = translate(AnimalDetail.SHARE_SUCCESS),
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
                e.rethrowIfCancellation()
                AppLog.e("AnimalDetailView", e.message ?: "Erreur", e)
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
internal fun handlePdfExport(
    previewHtml: String,
    previewMode: String,
    animalDetails: AnimalEv,
    selectedConsultation: ConsultationEv?,
    selectedRationsForPrescription: List<Ration>?,
    referenceUtilisee: fr.vetbrain.vetnutri_mp.Data.ReferenceEv?,
    additionalText: String,
    getSelectedConseils: () -> List<fr.vetbrain.vetnutri_mp.Export.HtmlSection>,
    loadPreferences: suspend () -> fr.vetbrain.vetnutri_mp.Data.PreferencesApplication,
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
        scope.launch(fr.vetbrain.vetnutri_mp.Utils.AppDispatchers.IO) {
            try {
                val prefs = loadPreferences()
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
                        poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                        poidsMetabolique = null,
                        besoinEnergetiqueEntretien = null,
                        // Plan évolutif exporté en tableau ingrédients × étapes
                        consultation = selectedConsultation
                    ),
                    defaultFileName = generateDefaultPdfFileName(animalDetails, selectedConsultation)
                )
            } catch (e: Exception) {
                e.rethrowIfCancellation()
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
                        poidsAnimal = selectedConsultation?.effectiveWeight?.toDouble(),
                        poidsMetabolique = null,
                        besoinEnergetiqueEntretien = null,
                        // Plan évolutif exporté en tableau ingrédients × étapes
                        consultation = selectedConsultation
                    ),
                    defaultFileName = generateDefaultPdfFileName(animalDetails, selectedConsultation)
                )
            }
        }
    }
    // L'export PDF de l'analyse de ration se fait désormais directement depuis RationsView.kt
    // (bouton dédié), avec les vraies valeurs de l'écran plutôt qu'en devinant le mode via
    // previewMode/contenu HTML.
}
