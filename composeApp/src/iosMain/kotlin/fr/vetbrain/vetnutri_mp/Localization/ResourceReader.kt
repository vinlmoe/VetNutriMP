package fr.vetbrain.vetnutri_mp.Localization

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class)
actual open class ResourceReader actual constructor() {
    actual open fun readResource(name: String): String {
        val bundle = NSBundle.mainBundle
        val resourceName = name.removeSuffix(".json")
        val extension = "json"

        
        
        
        

        val path = bundle.pathForResource(resourceName, extension)

        if (path == null) {
            
            

            // Lister toutes les ressources disponibles
            val allResources = bundle.pathsForResourcesOfType(extension, null)
            
            allResources?.forEachIndexed { index, resourcePath ->
                
            }

            throw IllegalStateException("Resource $name not found")
        }

        
        val content = NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)

        if (content == null) {
            
            throw IllegalStateException("Failed to read resource $name")
        }

        
        return content
    }

    /** Lit un fichier utilisateur dans le répertoire des documents de l'application */
    actual open fun readUserFile(filename: String): String? {
        return try {
            val documentsPath =
                    NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
                            .firstOrNull() as?
                            String
                            ?: return null

            val filePath = "$documentsPath/$filename"
            val fileManager = NSFileManager.defaultManager

            if (fileManager.fileExistsAtPath(filePath)) {
                NSString.stringWithContentsOfFile(filePath, NSUTF8StringEncoding, null)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /** Écrit dans un fichier utilisateur dans le répertoire des documents de l'application */
    actual open fun writeUserFile(filename: String, content: String): Boolean {
        return try {
            val documentsPath =
                    NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
                            .firstOrNull() as?
                            String
                            ?: return false

            val filePath = "$documentsPath/$filename"
            val nsString = content as NSString

            nsString.writeToFile(filePath, true, NSUTF8StringEncoding, null)
        } catch (e: Exception) {
            false
        }
    }
}
