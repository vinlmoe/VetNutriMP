package fr.vetbrain.vetnutri_mp.PlatformFile

import platform.Foundation.*

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual class PlatformFile actual constructor(path: String) {
    private val fullPath: String = path

    actual val path: String
        get() = fullPath
    actual val name: String
        get() = (fullPath as NSString).lastPathComponent
    actual val absolutePath: String
        get() = fullPath
    actual val length: Long
        get() {
            val attrs = NSFileManager.defaultManager.attributesOfItemAtPath(fullPath, null)
            val size = attrs?.get(NSFileSize) as? NSNumber
            return size?.longLongValue ?: 0L
        }
    actual val lastModified: Long
        get() {
            val attrs = NSFileManager.defaultManager.attributesOfItemAtPath(fullPath, null)
            val date = attrs?.get(NSFileModificationDate) as? NSDate
            return date?.timeIntervalSince1970?.times(1000)?.toLong() ?: 0L
        }

    actual fun exists(): Boolean = (NSFileManager.defaultManager.fileExistsAtPath(fullPath) == true)

    actual fun isDirectory(): Boolean =
            if (!exists()) false
            else {
                val attrs = NSFileManager.defaultManager.attributesOfItemAtPath(fullPath, null)
                val type = attrs?.get(NSFileType) as? String
                type == NSFileTypeDirectory
            }

    actual fun isFile(): Boolean = exists() && !isDirectory()

    actual fun mkdirs(): Boolean =
            (NSFileManager.defaultManager.createDirectoryAtPath(fullPath, true, null, null) == true)

    actual fun delete(): Boolean =
            (NSFileManager.defaultManager.removeItemAtPath(fullPath, null) == true)

    actual fun renameTo(dest: PlatformFile): Boolean =
            (NSFileManager.defaultManager.moveItemAtPath(fullPath, dest.path, null) == true)

    actual fun copyTo(dest: PlatformFile, overwrite: Boolean) {
        check(isFile()) { "Cannot read source $fullPath" }
        check(overwrite || !dest.exists()) { "Destination already exists: ${dest.path}" }
        if (fullPath == dest.path) return
        // Copy first, then atomically publish on the same filesystem. A failed copy must
        // never remove an existing destination (e.g. a database recovery file).
        val temporary = "${dest.path}.copy-${NSUUID().UUIDString}"
        try {
            check(NSFileManager.defaultManager.copyItemAtPath(fullPath, temporary, null)) {
                "Cannot copy $fullPath to ${dest.path}"
            }
            check(platform.posix.rename(temporary, dest.path) == 0) { "Cannot replace ${dest.path}" }
        } finally {
            NSFileManager.defaultManager.removeItemAtPath(temporary, null)
        }
    }

    actual fun listFiles(): List<PlatformFile>? =
            NSFileManager.defaultManager.contentsOfDirectoryAtPath(fullPath, null)?.map {
                PlatformFile("$fullPath/$it")
            }

    actual fun readText(): String =
            NSString.stringWithContentsOfFile(fullPath, NSUTF8StringEncoding, null)
                ?: error("Cannot read $fullPath as UTF-8")

    actual fun writeText(text: String) {
        val parent = (fullPath as NSString).stringByDeletingLastPathComponent
        if (parent.isNotEmpty()) {
            check(NSFileManager.defaultManager.createDirectoryAtPath(parent, true, null, null)) { "Cannot create $parent" }
        }
        check((text as NSString).writeToFile(fullPath, true, NSUTF8StringEncoding, null)) { "Cannot write $fullPath" }
    }

    actual companion object {
        actual fun create(path: String): PlatformFile = PlatformFile(path)
    }
}
