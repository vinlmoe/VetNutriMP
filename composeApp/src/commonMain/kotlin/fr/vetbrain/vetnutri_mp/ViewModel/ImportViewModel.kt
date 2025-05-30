package fr.vetbrain.vetnutri_mp.ViewModel

import fr.vetbrain.vetnutri_mp.Repository.AnimalRepository
import fr.vetbrain.vetnutri_mp.Repository.BiblioRefRepository
import fr.vetbrain.vetnutri_mp.Repository.DatabaseReferenceEvRepository
import fr.vetbrain.vetnutri_mp.Repository.EquationRepository
import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.ImportUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ImportViewModel(
        private val animalRepository: AnimalRepository,
        val databaseReferenceEvRepository: DatabaseReferenceEvRepository? = null,
        val equationRepository: EquationRepository? = null,
        val biblioRefRepository: BiblioRefRepository? = null
) {
    // Scope des coroutines pour les opérations suspend
    private val coroutineScope = CoroutineScope(AppDispatchers.Main)

    // Flag pour indiquer si les aliments doivent être supprimés avant l'importation
    var shouldClearFoodsBeforeImport: Boolean = false

    // Créer une instance d'AnimalListViewModel pour utiliser ses fonctions d'importation
    private val animalListViewModel = AnimalListViewModel(animalRepository)

    // Variables simples pour l'état d'importation
    var isImporting: Boolean = false
        private set

    var isImportingNutritionalRequirements: Boolean = false
        private set

    var importResultMessage: String? = null
        private set

    var nutritionalRequirementImportResultMessage: String? = null
        private set

    /**
     * Importe des références nutritionnelles à partir d'une chaîne JSON avec sauvegarde automatique
     *
     * @param jsonContent Le contenu JSON à désérialiser (.vbnr.json format)
     */
    fun importNutritionalRequirementsFromJson(jsonContent: String) {
        isImportingNutritionalRequirements = true
        nutritionalRequirementImportResultMessage = "🔄 Importation en cours..."

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

                    nutritionalRequirementImportResultMessage = message.toString()
                    println(
                            "🎉 ${references.size} références nutritionnelles importées avec succès"
                    )
                } else {
                    nutritionalRequirementImportResultMessage =
                            "❌ Aucune référence nutritionnelle trouvée dans le fichier"
                }
            } catch (e: Exception) {
                println(
                        "❌ Erreur lors de l'importation des références nutritionnelles: ${e.message}"
                )
                e.printStackTrace()
                nutritionalRequirementImportResultMessage =
                        "❌ Erreur lors de l'importation: ${e.message ?: "Erreur inconnue"}\n\nVérifiez que le fichier est au format .vbnr.json valide."
            } finally {
                isImportingNutritionalRequirements = false
            }
        }
    }

    /** Réinitialise les résultats d'importation */
    fun resetImportResult() {
        importResultMessage = null
        nutritionalRequirementImportResultMessage = null
        animalListViewModel.resetImportResult()
    }

    /**
     * Définit une erreur d'importation pour les références nutritionnelles
     *
     * @param message Le message d'erreur à afficher
     */
    fun setNutritionalRequirementImportError(message: String) {
        nutritionalRequirementImportResultMessage = "❌ $message"
        isImportingNutritionalRequirements = false
    }

    /** Délègue l'importation des animaux à la fonction de plateforme spécifique */
    fun importAnimalsFromFileUI() {
        fr.vetbrain.vetnutri_mp.importAnimalsFromFile(animalListViewModel)
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
        nutritionalRequirementImportResultMessage = message
    }
}
