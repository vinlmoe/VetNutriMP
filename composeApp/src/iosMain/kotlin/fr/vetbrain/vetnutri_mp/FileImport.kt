package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.ViewModel.AnimalListViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.ImportViewModel
import fr.vetbrain.vetnutri_mp.ViewModel.SettingsViewModel

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow

actual fun importAnimalsFromFile(viewModel: AnimalListViewModel) {
        viewModel.setImportError("L'importation de fichiers n'est pas encore implémentée sur iOS.")
}

actual fun importFoodsFromFile(viewModel: SettingsViewModel) {
        viewModel.setImportResult(
                SettingsViewModel.ImportResult.Error(
                        "L'importation de fichiers n'est pas encore implémentée sur iOS."
                )
        )
}

actual fun importNutritionalRequirementsFromFile(viewModel: ImportViewModel) {
        viewModel.setNutritionalRequirementImportError(
                "L'import des besoins n'est pas encore implémenté sur iOS."
        )
}

actual fun importApiFromFile(viewModel: SettingsViewModel) {
        viewModel.setImportResult(
                SettingsViewModel.ImportResult.Error(
                        "L'import API n'est pas encore implémenté sur iOS."
                )
        )
}

actual fun exportJsonToFile(content: String, defaultFileName: String): Boolean {
    try {
        val fileManager = NSFileManager.defaultManager
        // Utiliser le dossier temporaire pour le partage pour éviter de polluer Documents
        val tempDirectory = NSTemporaryDirectory()
        val filePath = "$tempDirectory$defaultFileName"
        
        val nsString = NSString.create(string = content)
        val data = nsString.dataUsingEncoding(NSUTF8StringEncoding)
        
        if (data != null) {
            val success = fileManager.createFileAtPath(filePath, data, null)
            if (success) {
                val url = NSURL.fileURLWithPath(filePath)
                val activityViewController = UIActivityViewController(listOf(url), null)
                
                // Trouver le contrôleur racine pour afficher la feuille de partage
                val window = UIApplication.sharedApplication.keyWindow 
                    ?: UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
                
                val rootViewController = window?.rootViewController
                
                // Sur iPad, le popover est nécessaire
                activityViewController.popoverPresentationController?.sourceView = window
                activityViewController.popoverPresentationController?.sourceRect = window?.bounds ?: platform.CoreGraphics.CGRectMake(0.0, 0.0, 0.0, 0.0)
                
                rootViewController?.presentViewController(activityViewController, true, null)
                return true
            }
        }
        return false
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

actual fun openJsonFileContent(): String? {
        return null
}
