package fr.vetbrain.vetnutri_mp.PlatformFile

import java.io.File

actual class PlatformFile actual constructor(path: String) {
    private val file: File = File(path)
    actual val path: String
        get() = file.path
    actual val name: String
        get() = file.name
    actual val absolutePath: String
        get() = file.absolutePath
    actual val length: Long
        get() = file.length()
    actual val lastModified: Long
        get() = file.lastModified()

    actual fun exists(): Boolean = file.exists()
    actual fun isFile(): Boolean = file.isFile
    actual fun isDirectory(): Boolean = file.isDirectory
    actual fun mkdirs(): Boolean = file.mkdirs()
    actual fun delete(): Boolean = file.delete()
    actual fun renameTo(dest: PlatformFile): Boolean = file.renameTo(File(dest.path))
    actual fun copyTo(dest: PlatformFile, overwrite: Boolean): Unit {
        file.copyTo(File(dest.path), overwrite)
    }
    actual fun listFiles(): List<PlatformFile>? = file.listFiles()?.map { PlatformFile(it.path) }
    actual fun readText(): String = file.readText()
    actual fun writeText(text: String): Unit = file.writeText(text)

    actual companion object {
        actual fun create(path: String): PlatformFile = PlatformFile(path)
    }
}
