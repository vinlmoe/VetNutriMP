package fr.vetbrain.vetnutri_mp.PlatformFile

import platform.Foundation.*

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual class PlatformFile actual constructor(path: String) {
    private val fullPath: String = path

    actual val path: String get() = fullPath
    actual val name: String get() = (fullPath as NSString).lastPathComponent
    actual val absolutePath: String get() = fullPath
    actual val length: Long get() = 0L
    actual val lastModified: Long get() = 0L

    actual fun exists(): Boolean = (NSFileManager.defaultManager.fileExistsAtPath(fullPath) == true)

    actual fun isDirectory(): Boolean =
        exists() && (NSFileManager.defaultManager.contentsOfDirectoryAtPath(fullPath, null) != null)

    actual fun isFile(): Boolean = exists() && !isDirectory()

    actual fun mkdirs(): Boolean =
        (NSFileManager.defaultManager.createDirectoryAtPath(fullPath, true, null, null) == true)

    actual fun delete(): Boolean =
        (NSFileManager.defaultManager.removeItemAtPath(fullPath, null) == true)

    actual fun renameTo(dest: PlatformFile): Boolean =
        (NSFileManager.defaultManager.moveItemAtPath(fullPath, dest.path, null) == true)

    actual fun copyTo(dest: PlatformFile, overwrite: Boolean) {
        if (overwrite && NSFileManager.defaultManager.fileExistsAtPath(dest.path)) {
            NSFileManager.defaultManager.removeItemAtPath(dest.path, null)
        }
        NSFileManager.defaultManager.copyItemAtPath(fullPath, dest.path, null)
    }

    actual fun listFiles(): List<PlatformFile>? =
        NSFileManager.defaultManager.contentsOfDirectoryAtPath(fullPath, null)
            ?.map { PlatformFile("$fullPath/$it") }

    actual fun readText(): String =
        NSString.stringWithContentsOfFile(fullPath, NSUTF8StringEncoding, null) ?: ""

    actual fun writeText(text: String) {
        (text as NSString).writeToFile(fullPath, true, NSUTF8StringEncoding, null)
    }

    actual companion object {
        actual fun create(path: String): PlatformFile = PlatformFile(path)
    }
}