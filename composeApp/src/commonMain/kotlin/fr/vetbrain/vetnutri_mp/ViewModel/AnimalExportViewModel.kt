package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Data.AnimalEv
import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.ConseilRepository
import fr.vetbrain.vetnutri_mp.Repository.ConsultationRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Repository.ExportImportRepository
import fr.vetbrain.vetnutri_mp.Repository.FoodRepository
import fr.vetbrain.vetnutri_mp.Repository.RecipeRepository
import fr.vetbrain.vetnutri_mp.Repository.TransactionRunner
import fr.vetbrain.vetnutri_mp.Service.JsonShareService
import fr.vetbrain.vetnutri_mp.Service.ShareLink
import fr.vetbrain.vetnutri_mp.Service.ShareOptions
import fr.vetbrain.vetnutri_mp.Service.createJsonShareService
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.anonymizeExportJson
import fr.vetbrain.vetnutri_mp.exportApiEnvelopeToFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Export / partage d'un animal complet (animal, consultations, rations, aliments, référentiels,
 * équations) : construit l'`ExportImportRepository`, sélectionne les identifiants à exporter et
 * délègue l'écriture du fichier ou l'envoi en ligne (jsonbin.io).
 *
 * Logique déplacée depuis View/AnimalDetailView.kt (`exporterAnimalComplet`,
 * `partagerAnimalEnLigne`) : la vue n'accède plus aux repositories, elle appelle les fonctions
 * suspend ci-dessous et affiche le résultat.
 */
class AnimalExportViewModel(
        private val animalRepository: AnimalRepository,
        private val foodRepository: FoodRepository? = null,
        private val equationRepository: EquationRepository? = null,
        private val referenceRepository: DatabaseReferenceEvRepository? = null,
        private val biblioRepository: BiblioRefRepository? = null,
        private val consultationRepository: ConsultationRepository? = null,
        private val recipeRepository: RecipeRepository? = null,
        private val conseilRepository: ConseilRepository? = null,
        private val transactionRunner: TransactionRunner = TransactionRunner.None,
        private val shareServiceFactory: () -> JsonShareService = { createJsonShareService() }
) : BaseViewModel() {

    /** Résultat d'un export vers un fichier. */
    sealed class ExportResult {
        data class Success(val fileName: String) : ExportResult()
        object Cancelled : ExportResult()
        data class Error(val message: String) : ExportResult()
    }

    /** Résultat d'un partage en ligne. */
    sealed class ShareResult {
        /** [animal] : animal rechargé depuis la base, avec le `jsonbinId` à jour. */
        data class Success(val link: ShareLink, val animal: AnimalEv) : ShareResult()
        data class Error(val message: String) : ShareResult()
    }

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _lastExportResult = MutableStateFlow<ExportResult?>(null)
    val lastExportResult: StateFlow<ExportResult?> = _lastExportResult.asStateFlow()

    private val _lastShareResult = MutableStateFlow<ShareResult?>(null)
    val lastShareResult: StateFlow<ShareResult?> = _lastShareResult.asStateFlow()

    private val exportImportRepository: ExportImportRepository by lazy {
        ExportImportRepository(
                animalRepository = animalRepository,
                foodRepository = foodRepository,
                equationRepository = equationRepository,
                referenceRepository = referenceRepository,
                biblioRepository = biblioRepository,
                consultationRepository = consultationRepository,
                recipeRepository = recipeRepository,
                conseilRepository = conseilRepository,
                transactionRunner = transactionRunner
        )
    }

    fun resetResults() {
        _lastExportResult.value = null
        _lastShareResult.value = null
    }

    /** Nom de fichier proposé pour l'export d'un animal. */
    fun defaultExportFileName(animal: AnimalEv): String =
            "${animal.id ?: animal.uuid}_${animal.nom}_export.json"

    /** Identifiants des aliments utilisés dans les rations de l'animal. */
    private fun collecterIdsAliments(animal: AnimalEv): Set<String> {
        val ids = mutableSetOf<String>()
        animal.consultations.forEach { consultation ->
            consultation.rations.forEach { ration ->
                ration.alimentMutableList.forEach { alimentRation ->
                    alimentRation.refAlimUnif?.takeIf { it.isNotBlank() }?.let { ids.add(it) }
                    alimentRation.aliment?.uuid?.takeIf { it.isNotBlank() }?.let { ids.add(it) }
                }
            }
        }
        return ids
    }

    /** Identifiants des référentiels (généraux + maladies) et de leurs équations. */
    private suspend fun collecterIdsReferencesEtEquations(animal: AnimalEv): Pair<Set<String>, Set<String>> {
        val refIds = mutableSetOf<String>()
        val eqIds = mutableSetOf<String>()
        suspend fun ajouter(refId: String) {
            refIds.add(refId)
            val reference = referenceRepository?.getReferenceEvById(refId)
            reference?.obtenirToutesEquations()?.forEach { equation -> eqIds.add(equation.uuid) }
        }
        animal.consultations.forEach { consultation ->
            consultation.referenceGeneraleId?.let { ajouter(it) }
            consultation.referencesMaladies.forEach { ajouter(it) }
        }
        return Pair(refIds, eqIds)
    }

    private suspend fun optionsExport(animal: AnimalEv): ExportImportRepository.ExportSelectionOptions {
        val foodIds = collecterIdsAliments(animal)
        val (referenceIds, equationIds) = collecterIdsReferencesEtEquations(animal)
        // Les références bibliographiques sont incluses automatiquement (liées aux référentiels
        // et équations exportés).
        return ExportImportRepository.ExportSelectionOptions(
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
    }

    /**
     * Exporte l'animal complet dans un fichier choisi par l'utilisateur (sélecteur de plateforme).
     */
    suspend fun exporterAnimalComplet(animal: AnimalEv): ExportResult {
        _isExporting.value = true
        val result =
                try {
                    val fileName = defaultExportFileName(animal)
                    // Le sélecteur Desktop doit être demandé hors du dispatcher Compose ;
                    // l'implémentation de plateforme transfère ensuite le dialogue sur l'EDT.
                    val success =
                            withContext(AppDispatchers.IO) {
                                val envelope = exportImportRepository.exportWithSelectionEnvelope(optionsExport(animal))
                                exportApiEnvelopeToFile(envelope, fileName)
                            }
                    if (success) ExportResult.Success(fileName) else ExportResult.Cancelled
                } catch (e: Exception) {
                    e.printStackTrace()
                    ExportResult.Error(e.message ?: "")
                } finally {
                    _isExporting.value = false
                }
        _lastExportResult.value = result
        return result
    }

    /**
     * Partage l'animal complet en ligne (jsonbin.io) et mémorise le `binId` sur l'animal.
     * @param shouldAnonymize anonymise le JSON avant l'envoi
     * @param shouldEncrypt chiffre le JSON envoyé
     */
    suspend fun partagerAnimalEnLigne(
            animal: AnimalEv,
            shouldAnonymize: Boolean = false,
            shouldEncrypt: Boolean = true
    ): ShareResult {
        _isExporting.value = true
        val result =
                try {
                    withContext(AppDispatchers.IO) {
                        var jsonContent = exportImportRepository.exportWithSelection(optionsExport(animal))
                        if (shouldAnonymize) jsonContent = anonymizeExportJson(jsonContent)

                        // Recharger l'animal depuis la BDD pour disposer du jsonbinId à jour
                        val animalToUse = animalRepository.getAnimalById(animal.uuid) ?: animal
                        val shareOptions =
                                ShareOptions(
                                        fileName = defaultExportFileName(animal),
                                        expiresInHours = 168, // 7 jours par défaut
                                        binName = animalToUse.uuid,
                                        binId = animalToUse.jsonbinId,
                                        encryptJson = shouldEncrypt
                                )
                        val shareResult = shareServiceFactory().uploadJson(jsonContent, shareOptions)
                        shareResult.fold(
                                onSuccess = { shareLink ->
                                    animalRepository.updateAnimal(animalToUse.copy(jsonbinId = shareLink.binId))
                                    val updated = animalRepository.getAnimalById(animalToUse.uuid)
                                            ?: animalToUse.copy(jsonbinId = shareLink.binId)
                                    ShareResult.Success(shareLink, updated)
                                },
                                onFailure = { error -> ShareResult.Error(error.message ?: "") }
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    ShareResult.Error(e.message ?: "")
                } finally {
                    _isExporting.value = false
                }
        _lastShareResult.value = result
        return result
    }
}
