package fr.vetbrain.vetnutri_mp.PlatformFile

/**
 * Interface multiplateforme pour représenter un fichier Remplace java.io.File pour la compatibilité
 * iOS
 */
expect class PlatformFile(path: String) {
    val path: String
    val name: String
    val absolutePath: String
    val length: Long
    val lastModified: Long

    fun exists(): Boolean
    fun isFile(): Boolean
    fun isDirectory(): Boolean
    fun mkdirs(): Boolean
    fun delete(): Boolean
    fun renameTo(dest: PlatformFile): Boolean
    fun copyTo(dest: PlatformFile, overwrite: Boolean = false)
    fun listFiles(): List<PlatformFile>?
    fun readText(): String
    fun writeText(text: String)

    companion object {
        fun create(path: String): PlatformFile
    }
}
