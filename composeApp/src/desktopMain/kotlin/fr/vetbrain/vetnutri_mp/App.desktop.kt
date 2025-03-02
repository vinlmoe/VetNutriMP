package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Utils.FileUtils
import fr.vetbrain.vetnutri_mp.Utils.ImportUtils
import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel

/** Fonction d'importation d'animaux spécifique à la plateforme desktop */
actual fun importAnimalsFromFile(viewModel: AnimalListViewModel) {
    try {
        // Ouvrir la boîte de dialogue de sélection de fichier
        val fileContent = FileUtils.openJsonFileDialog()

        if (fileContent != null) {
            println("Fichier JSON sélectionné. Taille du contenu: ${fileContent.length} caractères")

            // Importer les animaux depuis le contenu du fichier
            val animalsJson = ImportUtils.importAnimalsFromJson(fileContent)

            if (animalsJson.isNotEmpty()) {
                println("${animalsJson.size} animaux trouvés dans le fichier JSON")
                viewModel.importAnimals(animalsJson)
            } else {
                println("Aucun animal trouvé dans le fichier JSON")
                viewModel.setImportError("Aucun animal trouvé dans le fichier JSON")
            }
        } else {
            println("Aucun fichier sélectionné ou erreur lors de la lecture du fichier")
        }
    } catch (e: Exception) {
        println("Erreur lors de l'importation: ${e.message}")
        viewModel.setImportError("Erreur lors de l'importation: ${e.message}")
    }
}
