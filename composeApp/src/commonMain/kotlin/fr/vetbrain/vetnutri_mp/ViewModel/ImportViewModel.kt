package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.ImportUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Import de données (animaux, références nutritionnelles).
 * - Orchestration multiplateforme sans `ViewModel` Android, scope AppDispatchers.Main.
 * - Délègue l'import animaux à `AnimalListViewModel` et expose messages/flags d'état.
 * - Peut purger les aliments avant import selon le flag `shouldClearFoodsBeforeImport`.
 */
class ImportViewModel(
        private val animalListViewModel: AnimalListViewModel,
        val databaseReferenceEvRepository: DatabaseReferenceEvRepository? = null,
        val equationRepository: EquationRepository? = null,
        val biblioRefRepository: BiblioRefRepository? = null
) {
    // Scope des coroutines pour les opérations suspend
    private val job = SupervisorJob()
    private val coroutineScope = CoroutineScope(AppDispatchers.Main + job)

    // Flag pour indiquer si les aliments doivent être supprimés avant l'importation
    var shouldClearFoodsBeforeImport: Boolean = false

    val isImporting: StateFlow<Boolean> = animalListViewModel.isImportingAnimals

    private val _isImportingNutritionalRequirements = MutableStateFlow(false)
    val isImportingNutritionalRequirements: StateFlow<Boolean> =
            _isImportingNutritionalRequirements.asStateFlow()

    private val _nutritionalRequirementImportResultMessage = MutableStateFlow<String?>(null)
    val nutritionalRequirementImportResultMessage: StateFlow<String?> =
            _nutritionalRequirementImportResultMessage.asStateFlow()

    /**
     * Importe des références nutritionnelles à partir d'une chaîne JSON avec sauvegarde automatique
     *
     * @param jsonContent Le contenu JSON à désérialiser (.vbnr.json format)
     */
    fun importNutritionalRequirementsFromJson(jsonContent: String) {
        _isImportingNutritionalRequirements.value = true
        _nutritionalRequirementImportResultMessage.value = "🔄 Importation en cours..."

        coroutineScope.launch {
            try {
                // Vider les résolutions problématiques précédentes
                ImportUtils.clearResolutionsProblematiques()

                // Utiliser la version suspend avec sauvegarde automatique
                val references =
                        ImportUtils.importNutritionalRequirementsFromJson(
                                jsonContent = jsonContent,
                                databaseReferenceEvRepository = databaseReferenceEvRepository,
                                equationRepository = equationRepository,
                                biblioRefRepository = biblioRefRepository,
                                sauvegarderEnBase = true // Sauvegarde automatique activée
                        )

                if (references.isNotEmpty()) {
                    // Générer un message détaillé de succès
                    val message = StringBuilder()
                    message.append(
                            "✅ ${references.size} référence(s) nutritionnelle(s) importée(s) avec succès!\n\n"
                    )

                    // Détails des références importées
                    message.append("📋 Références importées :\n")
                    references.forEachIndexed { index, ref ->
                        message.append(
                                "  ${index + 1}. ${ref.nom} (${ref.espece} - ${ref.stadePhysio})"
                        )
                        if (ref.maladie) {
                            message.append(" - 🏥 ${ref.nomMaladie}")
                        }
                        message.append("\n")

                        // Compter et afficher les équations pour cette référence
                        var equationsCount = 0
                        if (ref.equationBEE != null) equationsCount++
                        if (ref.equationBW != null) equationsCount++
                        if (ref.equationDEcom != null) equationsCount++
                        if (ref.equationDEraw != null) equationsCount++
                        equationsCount += ref.equationsNut.size

                        if (equationsCount > 0) {
                            message.append("    🔧 ${equationsCount} équation(s) liée(s)\n")
                        }

                        // Compter les bibliographies pour cette référence
                        val biblioCount = ref.getAllBiblioRefs().size
                        if (biblioCount > 0) {
                            message.append(
                                    "    📚 ${biblioCount} référence(s) bibliographique(s) liée(s)\n"
                            )
                        }
                    }

                    // Message de sauvegarde réussie
                    message.append("\n💾 Données sauvegardées en base de données :")
                    message.append("\n  • Références : ${references.size} importées")

                    // Compter le total d'équations et de bibliographies importées
                    val totalEquations =
                            references.sumOf { ref ->
                                var count = 0
                                if (ref.equationBEE != null) count++
                                if (ref.equationBW != null) count++
                                if (ref.equationDEcom != null) count++
                                if (ref.equationDEraw != null) count++
                                count += ref.equationsNut.size
                                count
                            }

                    val totalBibliographies =
                            references.sumOf { ref -> ref.getAllBiblioRefs().size }

                    if (totalEquations > 0) {
                        message.append("\n  • Équations : ${totalEquations} sauvegardées")
                    }

                    if (totalBibliographies > 0) {
                        message.append("\n  • Bibliographies : ${totalBibliographies} sauvegardées")
                    }

                    // Afficher les problèmes de résolution s'il y en a
                    val rapportProblemes = ImportUtils.genererRapportResolutionsProblematiques()
                    if (rapportProblemes.contains("Aucune résolution problématique")) {
                        message.append("\n\n🎯 Tous les nutriments ont été résolus correctement")
                    } else {
                        message.append(
                                "\n\n⚠️ Voir les logs pour les détails des résolutions de nutriments"
                        )
                    }

                    _nutritionalRequirementImportResultMessage.value = message.toString()
                } else {
                    _nutritionalRequirementImportResultMessage.value =
                            "❌ Aucune référence nutritionnelle trouvée dans le fichier"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _nutritionalRequirementImportResultMessage.value =
                        "❌ Erreur lors de l'importation: ${e.message ?: "Erreur inconnue"}\n\nVérifiez que le fichier est au format .vbnr.json valide."
            } finally {
                _isImportingNutritionalRequirements.value = false
            }
        }
    }

    /** Réinitialise les résultats d'importation */
    fun resetImportResult() {
        _nutritionalRequirementImportResultMessage.value = null
        animalListViewModel.resetImportResult()
    }

    /**
     * Définit une erreur d'importation pour les références nutritionnelles
     *
     * @param message Le message d'erreur à afficher
     */
    fun setNutritionalRequirementImportError(message: String) {
        _nutritionalRequirementImportResultMessage.value = "❌ $message"
        _isImportingNutritionalRequirements.value = false
    }

    /** Délègue l'importation des animaux à la fonction de plateforme spécifique */
    fun importAnimalsFromFileUI() {
        fr.vetbrain.vetnutri_mp.importAnimalsFromFile(
                animalListViewModel,
                clearFoodsBeforeImport = shouldClearFoodsBeforeImport
        )
        shouldClearFoodsBeforeImport = false
    }

    /**
     * Délègue l'importation des références nutritionnelles à la fonction de plateforme spécifique
     */
    fun importNutritionalRequirementsFromFileUI() {
        fr.vetbrain.vetnutri_mp.importNutritionalRequirementsFromFile(this)
    }

    /**
     * Met à jour le message de résultat d'importation des références nutritionnelles
     *
     * @param message Le nouveau message à afficher
     */
    fun updateNutritionalRequirementImportResultMessage(message: String) {
        _nutritionalRequirementImportResultMessage.value = message
    }

    fun clear() {
        coroutineScope.cancel()
    }
}
