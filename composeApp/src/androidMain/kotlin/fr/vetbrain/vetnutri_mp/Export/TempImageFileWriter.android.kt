package fr.vetbrain.vetnutri_mp.Export

import okio.FileSystem

actual fun writeTempImageFile(fileName: String, bytes: ByteArray): String {
    val tempPath = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.resolve(fileName)
    FileSystem.SYSTEM.write(tempPath) {
        write(bytes)
    }
    return tempPath.toString()
}
