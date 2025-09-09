import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class BackupMetadata(
    val fileName: String,
    val filePath: String,
    val createdAt: Long,
    val fileSize: Long,
    val animalCount: Int,
    val foodCount: Int,
    val equationCount: Int,
    val conseilCount: Int,
    val recipeCount: Int,
    val rationCount: Int
)

fun main() {
    val backupDir = File("/Users/slefebvre/.vetnutri_mp/backups")
    val json = Json { 
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    
    backupDir.listFiles { file ->
        file.isFile && file.name.startsWith("vetnutri_backup_") && file.name.endsWith(".json") && !file.name.contains("_metadata")
    }?.forEach { file ->
        val metadataFile = File(file.absolutePath.replace(".json", "_metadata.json"))
        if (!metadataFile.exists()) {
            val metadata = BackupMetadata(
                fileName = file.name,
                filePath = file.absolutePath,
                createdAt = file.lastModified(),
                fileSize = file.length(),
                animalCount = 0, // Valeur par défaut
                foodCount = 8846, // Valeur approximative
                equationCount = 24,
                conseilCount = 1,
                recipeCount = 1,
                rationCount = 0
            )
            
            val metadataJson = json.encodeToString(metadata)
            metadataFile.writeText(metadataJson)
            println("Créé: ${metadataFile.name}")
        }
    }
}
